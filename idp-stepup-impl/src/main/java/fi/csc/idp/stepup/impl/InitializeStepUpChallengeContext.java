/*
 * The MIT License
 * Copyright (c) 2015-2020 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.impl;

import java.text.ParseException;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minidev.json.JSONObject;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.geant.idpextension.oidc.profile.impl.AbstractOIDCResponseAction;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import com.nimbusds.openid.connect.sdk.ClaimsRequest;

import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;

/**
 * Action initialises {@link StepUpChallengeContext} with a
 * {@link StepUpMethod}. Action assumes the authentication request is a OIDC
 * authentication request. Action extracts the requested claims of the OIDC
 * Authentication request. The extracted claims are used to initialise the
 * {@link StepUpMethod}.
 */

public class InitializeStepUpChallengeContext extends AbstractOIDCResponseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeStepUpChallengeContext.class);

    /** StepUp Method. */
    private StepUpMethod stepUpMethod;

    /**
     * Strategy used to extract, and create if necessary, the
     * {@link AuthenticationContext} from the {@link ProfileRequestContext}.
     */
    @Nonnull
    private Function<ProfileRequestContext, AuthenticationContext> authnCtxLookupStrategy;

    /** AuthenticationContext to operate on. */
    @Nullable
    private AuthenticationContext authnContext;

    /** Initialisation claims. */
    @Nullable
    private Collection<Entry> claims;

    /** Whether the claims must be in request object. */
    @Nonnull
    private boolean acceptOnlyRequestObjectClaims;

    /** Subject for whom the request is made. */
    @Nullable
    private String subject;

    /**
     * Whether the claims must be in request object.
     * 
     * @param acceptOnly whether the claims must be in request
     *                                      object
     */
    public void setAcceptOnlyRequestObjectClaims(boolean acceptOnly) {
        acceptOnlyRequestObjectClaims = acceptOnly;
    }

    /**
     * Set the context lookup strategy.
     * 
     * @param strategy lookup strategy function for {@link AuthenticationContext}.
     */
    public void setAuthenticationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AuthenticationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        authnCtxLookupStrategy = Constraint.isNotNull(strategy, "Strategy cannot be null");
    }

    /** Constructor. */
    public InitializeStepUpChallengeContext() {
        authnCtxLookupStrategy = new ChildContextLookup(AuthenticationContext.class);
    }

    /**
     * Set the step up method used.
     * 
     * @param method step up method used
     */

    public void setStepUpMethod(StepUpMethod method) {
        stepUpMethod = method;
    }

    /**
     * Resolve requested subject value from requested claims.
     * 
     * @param claims requested claims.
     * @return subject value if located, otherwise null.
     */
    private String getSubject(Collection<Entry> claims) {
        if (claims == null) {
            return null;
        }
        for (Entry entry : claims) {
            if (entry.getClaimName().equals(IDTokenClaimsSet.SUB_CLAIM_NAME)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed ", getLogPrefix());
            return false;
        }
        if (stepUpMethod == null) {
            log.error("{} bean not configured correctly, step up method not set", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return false;
        }
        authnContext = authnCtxLookupStrategy.apply(profileRequestContext);
        if (authnContext == null) {
            log.error("{} Authentication Context not available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_AUTHN_CTX);
            return false;
        }
        try {
            if (acceptOnlyRequestObjectClaims && getOidcResponseContext().getRequestObject() != null) {
                Object claimsRequest = getOidcResponseContext().getRequestObject().getJWTClaimsSet().getClaim("claims");
                if (claimsRequest instanceof JSONObject) {
                    log.debug("{} request object containing claims", getLogPrefix());
                    claims = ClaimsRequest.parse((JSONObject) claimsRequest).getIDTokenClaims();
                }

            } else if (!acceptOnlyRequestObjectClaims && getOidcResponseContext().getRequestedClaims() != null) {
                claims = getOidcResponseContext().getRequestedClaims().getIDTokenClaims();
            }
        } catch (ParseException e) {
            log.error("{} Failed parsing claims {}", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
        }
        subject = getSubject(claims);
        if (subject == null) {
            log.error("{} no subject in request, unable to continue", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_NO_USER);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        log.debug("{} Creating StepUpMethodContext", getLogPrefix());
        StepUpMethodContext stepUpMethodContext = (StepUpMethodContext) authnContext
                .addSubcontext(new StepUpMethodContext(), true);
        try {
            stepUpMethod.initialize(claims);
        } catch (Exception e) {
            log.error("{} Failed initializing stepup method {}", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        log.debug("{} Setting method {}  to StepUpMethodContext for user {}", getLogPrefix(), stepUpMethod.getName(),
                subject);
        stepUpMethodContext.setSubject(subject);
        stepUpMethodContext.setStepUpMethod(stepUpMethod);
        stepUpMethodContext.setStepUpAccount(stepUpMethod.getAccount());
    }
}
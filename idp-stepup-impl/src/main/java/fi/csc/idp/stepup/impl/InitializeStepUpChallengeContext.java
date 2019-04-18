/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
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

import com.google.common.base.Function;
import com.nimbusds.openid.connect.sdk.ClaimsRequest;

import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;

@SuppressWarnings("rawtypes")
public class InitializeStepUpChallengeContext extends AbstractOIDCResponseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeStepUpChallengeContext.class);

    /** StepUp Method. */
    private StepUpMethod stepUpMethod;

    /**
     * Strategy used to extract, and create if necessary, the {@link AuthenticationContext} from the
     * {@link ProfileRequestContext}.
     */
    @Nonnull
    private Function<ProfileRequestContext, AuthenticationContext> authnCtxLookupStrategy;

    /** AuthenticationContext to operate on. */
    @Nullable
    private AuthenticationContext authnContext;

    /** Initialization claims. */
    @Nullable
    Collection<Entry> claims;

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
        authnCtxLookupStrategy = new ChildContextLookup<>(AuthenticationContext.class);
    }

    /**
     * Set the stepup method used.
     * 
     * @param methods stepup method used
     */

    public void setStepUpMethod(StepUpMethod method) {
        stepUpMethod = method;
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
            // TODO:We want to store SUB to context to set it as uid
            // uid is used to resolve both attributes (not necessarily ever other than SUB itself to echo it back)
            // TODO: REFACTOR BLOCK
            Object claimsRequest = null;
            if (getOidcResponseContext().getRequestObject() != null) {
                claimsRequest = getOidcResponseContext().getRequestObject().getJWTClaimsSet().getClaim("claims");
            }
            if (claimsRequest instanceof ClaimsRequest) {
                log.debug("{} request object containing claims", getLogPrefix());
                claims = ((ClaimsRequest) claimsRequest).getIDTokenClaims();
            } else if (getOidcResponseContext().getRequestedClaims() != null) {
                log.debug("{} locating claims parameter containing claims, to simplify initial testing",
                        getLogPrefix());
                claims = getOidcResponseContext().getRequestedClaims().getIDTokenClaims();
            }
            if (claims != null) {
                log.debug("{} resolved claims from {}", getLogPrefix(),
                        getOidcResponseContext().getRequestedClaims().toJSONObject().toString());
            }
        } catch (ParseException e) {
            log.error("{} Failed parsing claims {}", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        log.debug("{} Creating StepUpMethodContext", getLogPrefix());
        StepUpMethodContext stepUpMethodContext =
                (StepUpMethodContext) authnContext.addSubcontext(new StepUpMethodContext(), true);
        try {
            stepUpMethod.initialize(claims);
        } catch (Exception e) {
            log.error("{} Failed initializing stepup method {}", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        log.debug("{} Setting method {}  to StepUpMethodContext", getLogPrefix(), stepUpMethod.getName());
        stepUpMethodContext.setStepUpMethod(stepUpMethod);
        // TODO: We will assume there will be only one account in method. Refactor StepUpMethod!
        if (stepUpMethod.getAccounts().size() > 0) {
            stepUpMethodContext.setStepUpAccount(stepUpMethod.getAccounts().get(0));
        }
    }
}
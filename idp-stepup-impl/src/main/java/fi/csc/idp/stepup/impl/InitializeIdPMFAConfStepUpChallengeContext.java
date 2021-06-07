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

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.session.context.navigate.CanonicalUsernameLookupStrategy;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;

/**
 * 
 */

public class InitializeIdPMFAConfStepUpChallengeContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeIdPMFAConfStepUpChallengeContext.class);

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
    
    /** Subject for whom the request is made. */
    @Nullable
    private String subject;

    /**
     * Dummy method for xml wiring.
     * 
     * @param acceptOnly whether the claims must be in request
     *                                      object
     */
    public void setAcceptOnlyRequestObjectClaims(boolean acceptOnly) {
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
    public InitializeIdPMFAConfStepUpChallengeContext() {
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
        AttributeResolutionContext attributeCtx;
        try {
            attributeCtx = (AttributeResolutionContext)profileRequestContext.getSubcontext(
                    "net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext", false);
        } catch (ClassNotFoundException e) {
            log.error("{} Attribute Resolution Context not available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        if (attributeCtx == null) {
            log.error("{} Attribute Resolution Context not available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        claims=new ArrayList<Entry>();
        attributeCtx.getResolvedIdPAttributes().forEach((k,v) ->{
            log.debug("Located initialization attribute {}", v.getId());
            for (IdPAttributeValue value:v.getValues()) {
                if (value.getNativeValue() instanceof String) {
                    log.debug("Initialization attribute {} has value {}", v.getId(), (String)value.getNativeValue());
                    claims.add(new Entry(v.getId(), ClaimRequirement.VOLUNTARY, null, (String)value.getNativeValue()));
                    break;
                }
            }
        });
        CanonicalUsernameLookupStrategy usernameLookup = new CanonicalUsernameLookupStrategy();
        subject = usernameLookup.apply(profileRequestContext);
        if (subject == null) {
            log.error("{} no subject resolved, unable to continue", getLogPrefix());
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
        log.debug("{} Setting method {} to StepUpMethodContext for user {}", getLogPrefix(), stepUpMethod.getName(), subject);
        stepUpMethodContext.setSubject(subject);
        stepUpMethodContext.setStepUpMethod(stepUpMethod);
        stepUpMethodContext.setStepUpAccount(stepUpMethod.getAccount());
    }
}
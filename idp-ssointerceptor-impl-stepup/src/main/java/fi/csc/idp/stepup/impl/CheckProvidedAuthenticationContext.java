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


import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethAuthnContext;

/**
 * An action that sets the requested authentication context value
 * to match what the provider has given.
 * 
 * As a proxy we must set the value to what the provider has actually used.
 * The value may also be mapped to new value here. 
 * 
 */
@SuppressWarnings("rawtypes")
public class CheckProvidedAuthenticationContext extends
        AbstractAuthenticationAction {
    
    
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(CheckProvidedAuthenticationContext.class);

    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @Nonnull
    private Function<ProfileRequestContext, AuthnRequest> authnRequestLookupStrategy;

    /** The request message to read from. */
    @Nullable
    private AuthnRequest authnRequest;
    
    /** Trusted providers and their stepup methods */
    private  Map<String, List<Principal>> trustedStepupProviders;
    
  
    /** Constructor. */
    public CheckProvidedAuthenticationContext() {
        log.trace("Entering");
        authnRequestLookupStrategy = Functions.compose(new MessageLookup<>(
                AuthnRequest.class), new InboundMessageContextLookup());
        log.trace("Leaving");
    }

    /**
     * Set the strategy used to locate the {@link AuthnRequest} to read from.
     * 
     * @param strategy
     *            lookup strategy
     */

    public void setAuthnRequestLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AuthnRequest> strategy) {
        log.trace("Entering");
        ComponentSupport
                .ifInitializedThrowUnmodifiabledComponentException(this);
        authnRequestLookupStrategy = Constraint.isNotNull(strategy,
                "AuthnRequest lookup strategy cannot be null");
        log.trace("Leaving");
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Principal> void setTrustedStepupProviders(@Nonnull Map<String, List<T>> stepupProviders) {
        log.trace("Entering");
        this.trustedStepupProviders = new HashMap<String, List<Principal>>();
        for ( Map.Entry<String, List<T>>entry:stepupProviders.entrySet()){
            this.trustedStepupProviders.put(entry.getKey(), (List<Principal>) entry.getValue());   
        }
        log.trace("Leaving");
    }
    

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            log.trace("Leaving");
            return false;
        }

        authnRequest = authnRequestLookupStrategy.apply(profileRequestContext);
        if (authnRequest == null) {
            log.debug(
                    "{} AuthnRequest message was not returned by lookup strategy",
                    getLogPrefix());
            
            // TODO :Add StepUpEventIds.EXCEPTION to supported errors, map it
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        log.trace("Leaving");
        return true;
    }
    
    
   
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        log.trace("Entering");
        
        if (trustedStepupProviders == null){
            //We continue with stepup as there are no trusted idps defined
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_CONTINUE_STEPUP);
            log.trace("Leaving");
            return;
        }
        final RequestedAuthnContext requestedCtx = authnRequest
                .getRequestedAuthnContext();
        
        if (requestedCtx == null){
            log.debug(
                    "There should be requested context for stepup");
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        final ShibbolethAuthnContext shibbolethContext = authenticationContext
                .getSubcontext(ShibbolethAuthnContext.class);
        if (shibbolethContext == null) {
            log.debug("{} Could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    EventIds.INVALID_PROFILE_CTX);
            log.trace("Leaving");
            return;
        }
        String providerId = shibbolethContext.getHeaders().get(
                "Shib-Identity-Provider");
        if (providerId == null){
            log.debug("{} Could not get provider entitytid ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    EventIds.INVALID_PROFILE_CTX);
            log.trace("Leaving");
            return;
        }
        if (!trustedStepupProviders.containsKey(providerId)){
            //We continue with stepup as the idp is not trusted
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_CONTINUE_STEPUP);
            log.trace("Leaving");
            return;
        }
        // NOW TRY TO MATCH REQUESTED TO PROVIDED 
        if(isTrusted(requestedCtx,trustedStepupProviders.get(providerId))){
            log.debug("authentication method satisfactory");
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_AUTHNCONTEXT_STEPUP);
            log.trace("Leaving");
        }
               
        
        ActionSupport.buildEvent(profileRequestContext,
                StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");
    }
    
    //TODO make following more generic
    
    /**
     * Method tries to locate requested method from the configured set of methods
     * 
     * @param requestedCtx contains the requested methods
     * @param configuredCtxs configured trusted methods
     * @return true if the requested ctx is among trusted ctxs
     */
    private boolean isTrusted(RequestedAuthnContext requestedCtx, List<Principal> trustedCtxs){
        log.trace("Entering");
        if (trustedCtxs == null){
            log.trace("Leaving");
            return false;
        }
        for (AuthnContextClassRef authnContextClassRef : requestedCtx
                .getAuthnContextClassRefs()) {
            for (Principal matchingPrincipal : trustedCtxs) {
                if (matchingPrincipal instanceof AuthnContextClassRefPrincipal
                        && authnContextClassRef
                                .getAuthnContextClassRef()
                                .equals(((AuthnContextClassRefPrincipal) matchingPrincipal)
                                        .getAuthnContextClassRef()
                                        .getAuthnContextClassRef())) {
                    log.debug("stepup trusted {}",authnContextClassRef
                                .getAuthnContextClassRef());
                    log.trace("leaving");
                    return true;
                }

            }
        }
        for (AuthnContextDeclRef authnContextDeclRef : requestedCtx
                .getAuthnContextDeclRefs()) {
            for (Principal matchingPrincipal : trustedCtxs) {
                if (matchingPrincipal instanceof AuthnContextDeclRefPrincipal
                        && authnContextDeclRef
                                .getAuthnContextDeclRef()
                                .equals(((AuthnContextDeclRefPrincipal) matchingPrincipal)
                                        .getAuthnContextDeclRef()
                                        .getAuthnContextDeclRef())) {
                    log.debug("stepup trusted {}",authnContextDeclRef
                            .getAuthnContextDeclRef());
                    log.trace("leaving");
                    return true;
                }

            }
        }
        log.trace("Leaving");
        return false;
    }

}
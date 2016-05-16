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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeSender;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that create step up challenge. The action selects attribute id,
 * challenge generator and sender implementations on the basis of requested 
 * authentication context. Attribute value, if defined, is passed to challenge 
 * generator, challenge is stored and passed with attribute value to challenge 
 * sender. 
 * 
 */

@SuppressWarnings("rawtypes")
public class GenerateStepUpChallenge extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(GenerateStepUpChallenge.class);

    /** Context to look attributes for. */
    @Nonnull
    private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;
    
    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @Nonnull
    private Function<ProfileRequestContext, AuthnRequest> authnRequestLookupStrategy;
    
    /** The request message to read from. */
    @Nullable
    private AuthnRequest authnRequest;

    /** The attribute ID to look for. */
    @Nullable
    private  Map<Principal, String> attributeIds;
    
    /** AttributeContext to filter. */
    @Nullable
    private AttributeContext attributeContext;

    /** Challenge Generators. */
    private  Map<Principal, ChallengeGenerator> challengeGenerators;
    
    /** Challenge Senders. */
    private  Map<Principal, ChallengeSender> challengeSenders;
 
    /** Constructor. */
    public GenerateStepUpChallenge() {
        log.trace("Entering");
        attributeContextLookupStrategy = Functions
                .compose(
                        new ChildContextLookup<>(AttributeContext.class),
                        new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(
                                RelyingPartyContext.class));
        authnRequestLookupStrategy = Functions.compose(new MessageLookup<>(
                AuthnRequest.class), new InboundMessageContextLookup());
        log.trace("Leaving");
    }
    
  
    /**
     * Set the challenge senders keyed by requested authentication context.
     * 
     * @param sender
     *            implementations of challenge sender in a map
     */
    public <T extends Principal> void  setChallengeSenders(@Nonnull Map<T, ChallengeSender> senders) {
        log.trace("Entering");
        this.challengeSenders=new HashMap<Principal, ChallengeSender>();
        for ( Map.Entry<T, ChallengeSender>entry:senders.entrySet()){
            this.challengeSenders.put(entry.getKey(), entry.getValue());   
        }
        log.trace("Leaving");
    }

    /**
     * Set the attribute IDs keyed by requested authentication context
     * 
     * @param ids
     *            attribute IDs to look for in a map
     */

    public <T extends Principal> void setAttributeIds(@Nonnull Map<T, String> ids) {
        log.trace("Entering");
        this.attributeIds=new HashMap<Principal, String>();
        for ( Map.Entry<T, String>entry:ids.entrySet()){
            this.attributeIds.put(entry.getKey(), entry.getValue());   
        }
        log.trace("Leaving");
    }
    
    /**
     * Set the challenge generators keyed by requested authentication context.
     * 
     * @param generator
     *            implementations of challenge generators in a map
     */
    public <T extends Principal> void setChallengeGenerators(@Nonnull Map<T, ChallengeGenerator> generators) {
        log.trace("Entering");
        this.challengeGenerators=new HashMap<Principal, ChallengeGenerator>();
        for ( Map.Entry<T, ChallengeGenerator>entry:generators.entrySet()){
            this.challengeGenerators.put(entry.getKey(), entry.getValue());   
        }
        log.trace("Leaving");
    }

    
    /**
     * Set the lookup strategy for the {@link AttributeContext}.
     * 
     * @param strategy
     *            lookup strategy
     */

    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        log.trace("Entering");
        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
        log.trace("Leaving");
    }

   

    /** {@inheritDoc} */

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        log.trace("Entering");
        attributeContext = attributeContextLookupStrategy
                .apply(profileRequestContext);
        if (attributeContext == null) {
            log.error("{} Unable to locate attribute context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        log.debug("{} Found attributeContext '{}'", getLogPrefix(),
                attributeContext);
        authnRequest = authnRequestLookupStrategy.apply(profileRequestContext);
        if (authnRequest == null) {
            log.debug(
                    "{} AuthnRequest message was not returned by lookup strategy",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        return super.doPreExecute(profileRequestContext, interceptorContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {
        log.trace("Entering");

        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug(
                    "{} Profile action does not contain an HttpServletRequest",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
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
        
        //Resolve attribute value
        String attributeId = null;
        Principal key = null;
        if (attributeIds != null){
            key=findKey(requestedCtx,attributeIds.keySet());
        }
        if (key != null){
            attributeId=attributeIds.get(key);   
        }
        String target = null;
        if (attributeId != null){
            //As attributeId is defined we expect to resolve a string value for target
            IdPAttribute attribute = attributeContext.getIdPAttributes().get(
                    attributeId);
            if (attribute == null){
                log.debug("Attributes do not contain value for " + attributeId,
                        getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext,
                        StepUpEventIds.EVENTID_INVALID_USER);
                log.trace("Leaving");
                return;
            }
            for (final IdPAttributeValue value : attribute.getValues()) {
                if (value instanceof StringAttributeValue) {
                    target = ((StringAttributeValue) value).getValue();
                }
            }
            if (target == null) {
                log.debug("Attributes did not contain String value for "
                        + attributeId, getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext,
                        StepUpEventIds.EVENTID_INVALID_USER);
                log.trace("Leaving");
                return;
            }
        }
        //Resolve challenge generator
        ChallengeGenerator challengeGenerator = null;
        if (challengeGenerators != null){
            challengeGenerator=challengeGenerators.get(findKey(requestedCtx,challengeGenerators.keySet()));
        }
        if (challengeGenerator == null){
            log.debug("no challenge generator defined for requested context");
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
            log.trace("Leaving");
            return;
        }
        //Resolve challenge sender
        ChallengeSender challengeSender = null;
        if (challengeSenders != null){
            challengeSender =challengeSenders.get(findKey(requestedCtx,challengeSenders.keySet()));
        }
        if (challengeSender == null){
            log.debug("no challenge sender defined for requested context");
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
            log.trace("Leaving");
            return;
        }
        
        String challenge;
        try {
            challenge = challengeGenerator.generate(target);
            //TODO: Store the challenge value to context, not session .
            request.getSession().setAttribute(
                    "fi.csc.idp.stepup.impl.GenerateStepUpChallenge.challenge", challenge);
            //TODO: Store the challenge value to context, not session .
            request.getSession().setAttribute(
                    "fi.csc.idp.stepup.impl.GenerateStepUpChallenge.target", target);
            challengeSender.send(challenge, target);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug("Unable to generate/pass challenge", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
       
        ActionSupport.buildEvent(profileRequestContext,
                StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");

    }
    
    /**
     * Method tries to locate requested method from the configured set of methods
     * 
     * @param requestedCtx contains the requested methods
     * @param configuredCtxs configured requested methods
     * @return null or the matching item in the set
     */
    private Principal findKey(RequestedAuthnContext requestedCtx, Set<Principal> configuredCtxs){
        log.trace("Entering");
        if (configuredCtxs == null){
            log.trace("Leaving");
            return null;
        }
        for (AuthnContextClassRef authnContextClassRef : requestedCtx
                .getAuthnContextClassRefs()) {
            for (Principal matchingPrincipal : configuredCtxs) {
                if (matchingPrincipal instanceof AuthnContextClassRefPrincipal
                        && authnContextClassRef
                                .getAuthnContextClassRef()
                                .equals(((AuthnContextClassRefPrincipal) matchingPrincipal)
                                        .getAuthnContextClassRef()
                                        .getAuthnContextClassRef())) {
                    log.debug("stepup requested {}",authnContextClassRef
                                .getAuthnContextClassRef());
                    log.trace("leaving");
                    return matchingPrincipal;
                }

            }
        }
        for (AuthnContextDeclRef authnContextDeclRef : requestedCtx
                .getAuthnContextDeclRefs()) {
            for (Principal matchingPrincipal : configuredCtxs) {
                if (matchingPrincipal instanceof AuthnContextDeclRefPrincipal
                        && authnContextDeclRef
                                .getAuthnContextDeclRef()
                                .equals(((AuthnContextDeclRefPrincipal) matchingPrincipal)
                                        .getAuthnContextDeclRef()
                                        .getAuthnContextDeclRef())) {
                    log.debug("stepup requested {}",authnContextDeclRef
                            .getAuthnContextDeclRef());
                    log.trace("leaving");
                    return matchingPrincipal;
                }

            }
        }
        log.trace("Leaving");
        return null;
    }


}
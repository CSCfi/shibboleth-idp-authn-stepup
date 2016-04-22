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
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.DefaultPrincipalDeterminationStrategy;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;

import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;
import net.shibboleth.idp.saml.profile.config.navigate.SessionLifetimeLookupFunction;
import net.shibboleth.idp.saml.profile.impl.BaseAddAuthenticationStatementToAssertion;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.profile.SAML2ActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import fi.okm.mpass.shibboleth.authn.context.ShibbolethAuthnContext;

/**
 * Action that modifies {@link Assertion} of a {@link AuthnStatement}.
 * This actions modifies the authentication method passed in assertion for Proxy use case.
 * The new value reflects the value passed by originating IdP or if a reserved internal value
 * is defined (like for step-up), that value is used if also requested originally by SP.
 * 
 * 
 * 
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link net.shibboleth.idp.authn.AuthnEventIds#INVALID_AUTHN_CTX}
 */
public class ProxyModifyAuthnStatementOfAssertion extends BaseAddAuthenticationStatementToAssertion {

    /** The authentication methods internal to proxy<->SP. */
    @Nonnull private Subject internalPrincipals;
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ProxyModifyAuthnStatementOfAssertion.class);
    
    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @SuppressWarnings("rawtypes")
    @Nonnull
    private Function<ProfileRequestContext, AuthnRequest> authnRequestLookupStrategy;
    
    /** The request message to read from. */
    @Nullable
    private AuthnRequest authnRequest;
    
    /** Strategy used to locate the {@link Assertion} to operate on. */
    @SuppressWarnings("rawtypes")
    @NonnullAfterInit private Function<ProfileRequestContext,Assertion> assertionLookupStrategy;
    
    /** Strategy used to determine the AuthnContextClassRef. */
    @SuppressWarnings("rawtypes")
    @NonnullAfterInit private Function<ProfileRequestContext,AuthnContextClassRefPrincipal> classRefLookupStrategy;

    /** Strategy used to determine SessionNotOnOrAfter value to set. */
    @SuppressWarnings("rawtypes")
    @Nullable private Function<ProfileRequestContext,Long> sessionLifetimeLookupStrategy;
        
    /** Constructor. */
    public ProxyModifyAuthnStatementOfAssertion() {
        sessionLifetimeLookupStrategy = new SessionLifetimeLookupFunction();
    }
    
    /**
     * Set the strategy used to locate the {@link Assertion} to operate on.
     * 
     * @param strategy strategy used to locate the {@link Assertion} to operate on
     */
    public void setAssertionLookupStrategy(@SuppressWarnings("rawtypes") @Nonnull final Function<ProfileRequestContext,Assertion> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        assertionLookupStrategy = Constraint.isNotNull(strategy, "Assertion lookup strategy cannot be null");
    }
    
    /**
     * Set the strategy function to use to obtain the authentication context class reference to use.
     * 
     * @param strategy  authentication context class reference lookup strategy
     */
    public void setClassRefLookupStrategy(
            @SuppressWarnings("rawtypes") @Nonnull final Function<ProfileRequestContext,AuthnContextClassRefPrincipal> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        classRefLookupStrategy = Constraint.isNotNull(strategy,
                "Authentication context class reference strategy cannot be null");
    }
    
    /**
     * Set the strategy used to locate the {@link AuthnRequest} to read from.
     * 
     * @param strategy
     *            lookup strategy
     */

    public void setAuthnRequestLookupStrategy(
            @SuppressWarnings("rawtypes") @Nonnull final Function<ProfileRequestContext, AuthnRequest> strategy) {
        log.trace("Entering");
        ComponentSupport
                .ifInitializedThrowUnmodifiabledComponentException(this);
        authnRequestLookupStrategy = Constraint.isNotNull(strategy,
                "AuthnRequest lookup strategy cannot be null");
        log.trace("Leaving");
    }

    /**
     * Set the strategy used to locate the SessionNotOnOrAfter value to use.
     * 
     * @param strategy lookup strategy
     */
    public void setSessionLifetimeLookupStrategy(@SuppressWarnings("rawtypes") @Nullable final Function<ProfileRequestContext,Long> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        sessionLifetimeLookupStrategy = strategy;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (authnRequestLookupStrategy == null){
            authnRequestLookupStrategy = Functions.compose(new MessageLookup<>(
                    AuthnRequest.class), new InboundMessageContextLookup());
        }
        
        if (classRefLookupStrategy == null) {
            classRefLookupStrategy = new DefaultPrincipalDeterminationStrategy<>(AuthnContextClassRefPrincipal.class,
                    new AuthnContextClassRefPrincipal(AuthnContext.UNSPECIFIED_AUTHN_CTX));
        }

        if (assertionLookupStrategy == null) {
            assertionLookupStrategy = new AssertionStrategy();
        }
    }
    
    /**
     * Set internal principals used between proxy and SP. 
     * 
     * 
     * @param <T> a type of principal to add, if not generic
     * @param principals supported principals to add
     */
    public <T extends Principal> void setInternalPrincipals(@Nonnull @NonnullElements final Collection<T> principals) {
        log.trace("Entering");
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(principals, "Principal collection cannot be null.");
        if (internalPrincipals==null){
            internalPrincipals=new Subject();
        }
        internalPrincipals.getPrincipals().clear();
        internalPrincipals.getPrincipals().addAll(Collections2.filter(principals, Predicates.notNull()));
        log.trace("Leaving");
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@SuppressWarnings("rawtypes") @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        authnRequest = authnRequestLookupStrategy.apply(profileRequestContext);
        if (authnRequest == null) {
            log.debug(
                    "{} AuthnRequest message was not returned by lookup strategy",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            log.trace("Leaving");
            return;
        }
        final ShibbolethAuthnContext shibbolethContext =
              authenticationContext.getSubcontext(ShibbolethAuthnContext.class);
        if (shibbolethContext == null){
            log.debug(
                    "{} Could not get shib proxy context",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            log.trace("Leaving");
            return;
        }
        final Assertion assertion = assertionLookupStrategy.apply(profileRequestContext);
        if (assertion == null) {
            log.error("Unable to obtain Assertion to modify");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }
        
        
        //We clear current auth methods from statement
        //TODO: statements are indexed, how can we know which one?
        assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextClassRef(null);
        assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextDecl(null);
        
        final RequestedAuthnContext requestedCtx = authnRequest
                .getRequestedAuthnContext();
        
        /*
         * If SP has asked for specific method we set a 'internal method' as such if
         * it has been requested and listed as supported.
         * 
         * This could maybe be done also by shibs conventional means by using weighted map
         * 
         */
        if (internalPrincipals != null && requestedCtx != null
                && (!requestedCtx.getAuthnContextClassRefs().isEmpty() || !requestedCtx.getAuthnContextDeclRefs().isEmpty())) {
            log.debug("Locating proxy internal auth methods");
            
            //TODO: do we have to break from the loops or use weights as shib itself?
            //now we just set the last matching one. Is that ok?
            for (AuthnContextClassRef authnContextClassRef:requestedCtx.getAuthnContextClassRefs()){
                for (Principal matchingPrincipal:internalPrincipals.getPrincipals()){
                    if (matchingPrincipal instanceof AuthnContextClassRefPrincipal &&
                            authnContextClassRef.getAuthnContextClassRef().equals(((AuthnContextClassRefPrincipal) matchingPrincipal).getAuthnContextClassRef().getAuthnContextClassRef())) {
                        log.debug("setting ClassRef "+((AuthnContextClassRefPrincipal)matchingPrincipal).getAuthnContextClassRef());
                        assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextClassRef(((AuthnContextClassRefPrincipal)matchingPrincipal).getAuthnContextClassRef());  
                    } 
                                
                }
            }
            for (AuthnContextDeclRef authnContextDeclRef:requestedCtx.getAuthnContextDeclRefs()){
                for (Principal matchingPrincipal:internalPrincipals.getPrincipals()){
                    if (matchingPrincipal instanceof AuthnContextDeclRefPrincipal &&
                            authnContextDeclRef.getAuthnContextDeclRef().equals(((AuthnContextDeclRefPrincipal) matchingPrincipal).getAuthnContextDeclRef().getAuthnContextDeclRef())) {
                        log.debug("setting DeclRef "+(((AuthnContextDeclRefPrincipal)matchingPrincipal).getAuthnContextDeclRef()));
                        assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextDeclRef((((AuthnContextDeclRefPrincipal)matchingPrincipal).getAuthnContextDeclRef()));  
                    } 
                                
                }
            }
        }
        /*
         * If SP request did not match any of the internal methods we use
         * values provided by the original provider
         * 
         */
        
        if (assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef() == null &&
            assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextDeclRef() == null){
            log.debug("using auth method provided by original provider");
            String declRef=shibbolethContext.getHeaders().get("Shib-AuthnContext-Decl");
            String classRef=shibbolethContext.getHeaders().get("Shib-AuthnContext-Class");
            if (declRef != null){
                log.debug("DeclRef "+declRef);
                AuthnContextDeclRefPrincipal authnContextDeclRefPrincipal = new AuthnContextDeclRefPrincipal(declRef);
                assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextDeclRef(
                        authnContextDeclRefPrincipal.getAuthnContextDeclRef());   
            }
            if (classRef != null){
                log.debug("classRef "+classRef);
                AuthnContextClassRefPrincipal authnContextClassRefPrincipal = new AuthnContextClassRefPrincipal(classRef);
                assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextClassRef(
                        authnContextClassRefPrincipal.getAuthnContextClassRef());   
            }
        }
        
        log.debug("{} Modified AuthenticationStatement of Assertion {}", getLogPrefix(), assertion.getID());
        
    }
    
    

       
    /**
     * Default strategy for obtaining assertion to modify.
     * 
     * <p>If the outbound message is already an assertion, it's returned. If the outbound message
     * is a response, then either
     * an existing or new assertion in the response is returned, depending on the action setting. If the
     * outbound message is anything else, null is returned.</p>
     */
    
    @SuppressWarnings("rawtypes")
    private class AssertionStrategy implements Function<ProfileRequestContext,Assertion> {

        /** {@inheritDoc} */
        @Override
        @Nullable public Assertion apply(@Nullable final ProfileRequestContext input) {
            if (input != null && input.getOutboundMessageContext() != null) {
                final Object outboundMessage = input.getOutboundMessageContext().getMessage();
                if (outboundMessage instanceof Assertion) {
                    return (Assertion) outboundMessage;
                } else if (outboundMessage instanceof Response) {
                    if (isStatementInOwnAssertion() || ((Response) outboundMessage).getAssertions().isEmpty()) {
                        return SAML2ActionSupport.addAssertionToResponse(ProxyModifyAuthnStatementOfAssertion.this,
                                (Response) outboundMessage, getIdGenerator(), getIssuerId());
                    } else {
                        return ((Response) outboundMessage).getAssertions().get(0);
                    } 
                }
            }
            
            return null;
        }
    }

}
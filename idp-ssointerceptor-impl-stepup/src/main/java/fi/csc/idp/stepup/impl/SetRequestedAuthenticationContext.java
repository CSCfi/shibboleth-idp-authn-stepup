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
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

/**
 * An action that sets the requested authentication context value
 * to match what the provider has given.
 * 
 * Normally a matching requested prinvipal is returned if it exists, otherwise
 * a weighted map is used. For proxy case we make following exemption:
 * 
 * If there is a mapping defined for idp,sp, retuned method we set that value to response.
 * Mapping indicates the intention to modify the value. 
 * 
 *
 * 
 */

@SuppressWarnings("rawtypes")
public class SetRequestedAuthenticationContext extends
        AbstractAuthenticationAction {
    
    /** Mapping of authentication methods.
     *
     * idp->sp->method->new method
     * 
     * For matching entry a new value is used
     *  
     * */
    private  Map<String, Map<String, Map<Principal, Principal>>> authMethodMap;

    /** Mapping of default methods.
     * 
     *  idp->sp
     *  
     *  For matching entry a value provided by idp is used.
     *  If there is match in authMethodMap, default mapping is not used.
     *  
     *  
     * */
    private  Map<String,List<String>> defaultValueMap;
    

    
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(SetRequestedAuthenticationContext.class);

    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @Nonnull
    private Function<ProfileRequestContext, AuthnRequest> authnRequestLookupStrategy;

    /** The request message to read from. */
    @Nullable
    private AuthnRequest authnRequest;
    

    /** Constructor. */
    public SetRequestedAuthenticationContext() {
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

    public void setPassThruuEntityLists(Map<String, List<String>> defaultValueMap) {
        this.defaultValueMap = defaultValueMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends Principal> void setAuthenticationMethodMapping(@Nonnull Map<String, Map<String, Map<T, T>>> map) {
        log.trace("Entering");
        if (this.authMethodMap == null){
            this.authMethodMap=new HashMap<String, Map<String, Map<Principal, Principal>>>();
        }
        for ( Map.Entry<String, Map<String, Map<T, T>>> entry:map.entrySet()){
          //new item
           if (!this.authMethodMap.containsKey(entry.getKey())){
                this.authMethodMap.put(entry.getKey(), new HashMap<String, Map<Principal, Principal>>());
           }
           for (Map.Entry<String, Map<T, T>> entry2:entry.getValue().entrySet()){
               if (!this.authMethodMap.get(entry.getKey()).containsKey(entry2)){
                   this.authMethodMap.get(entry.getKey()).put(entry2.getKey(), new HashMap<Principal, Principal>());
               }
               for (Map.Entry<T, T> entry3:entry2.getValue().entrySet()){
                   if (!this.authMethodMap.get(entry.getKey()).get(entry2.getKey()).containsKey(entry3.getKey())){
                       this.authMethodMap.get(entry.getKey()).get(entry2.getKey()).put(entry3.getKey(), entry3.getValue());   
                   }
                   
               }
           }
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
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        return true;
    }
    
    
   
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        log.trace("Entering");
        final ShibbolethSpAuthenticationContext shibbolethContext = authenticationContext
                .getSubcontext(ShibbolethSpAuthenticationContext.class);
        if (shibbolethContext == null) {
            log.debug("{} Could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        if (shibbolethContext.getIdp() == null){
            log.debug("{} Could not get provider entitytid ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        String sp=authnRequest.getIssuer().getSPProvidedID();
        if (sp == null){
            log.debug("{} Could not get client entitytid ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        
        
        Principal providedMethod=null;
        if (shibbolethContext.getContextClass() != null){
            providedMethod=new AuthnContextClassRefPrincipal(shibbolethContext.getContextClass()); 
        }else if (shibbolethContext.getContextDecl() != null){
            providedMethod=new AuthnContextDeclRefPrincipal(shibbolethContext.getContextDecl()); 
        }
        if (providedMethod == null){
            log.debug("{} Could not get authentication method ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        Principal mappedMethod=getDefaultMapping(shibbolethContext.getIdp(), sp, providedMethod);
        mappedMethod=getExactMapping(shibbolethContext.getIdp(), sp, providedMethod);
        if (mappedMethod != null){
            log.debug("Setting matching principal to {}",mappedMethod.getName());
            RequestedPrincipalContext reqPrincipalContext=authenticationContext.getSubcontext(RequestedPrincipalContext.class,true);
            reqPrincipalContext.setMatchingPrincipal(mappedMethod);
        }
        ActionSupport.buildEvent(profileRequestContext,
                StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");
    }
    
    private Principal getExactMapping(String idp, String sp, Principal method){
        log.trace("Entering");
        log.debug("Searching a match for triplet {},{} and {}",idp,sp,method.getName());
        if (authMethodMap.containsKey(idp) &&
                authMethodMap.get(idp) != null &&
                authMethodMap.get(idp).containsKey(sp) &&
                authMethodMap.get(idp).get(sp) != null &&
                authMethodMap.get(idp).get(sp).containsKey(method)){
            log.trace("Leaving");
            return authMethodMap.get(idp).get(sp).get(method);
        }
        log.trace("Leaving");
        return null;
    }
    
    private Principal getDefaultMapping(String idp, String sp, Principal method){
        log.trace("Entering");
        log.debug("Searching a match for pair {} and {}, provided method is {}",idp,sp,method.getName());
        if (defaultValueMap.containsKey(idp) && 
                defaultValueMap.get(idp)!= null &&
                defaultValueMap.get(idp).contains(sp)){
            return method;
        }
        log.trace("Leaving");
        return null;
    }
   
}
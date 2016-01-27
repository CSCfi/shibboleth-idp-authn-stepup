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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that checks if step up authentication is requested.
 * 
 */

@SuppressWarnings("rawtypes")
public class CheckRequestedAuthenticationContext extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CheckRequestedAuthenticationContext.class);
      
    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @Nonnull private Function<ProfileRequestContext,AuthnRequest> authnRequestLookupStrategy;

    /** The request message to read from. */
    @Nullable private AuthnRequest authnRequest;
    
    /** The attribute to match against. */
    @Nullable private IdPAttribute attribute;
    
    
    /** Constructor. */
    public CheckRequestedAuthenticationContext() {
    	log.trace("Entering");
    	authnRequestLookupStrategy =
                Functions.compose(new MessageLookup<>(AuthnRequest.class), new InboundMessageContextLookup());
        log.trace("Leaving");
    }

    /**
     * Set the strategy used to locate the {@link AuthnRequest} to read from.
     * 
     * @param strategy lookup strategy
     */
    
    
    public void setAuthnRequestLookupStrategy(@Nonnull final Function<ProfileRequestContext,AuthnRequest> strategy) {
    	log.trace("Entering");
    	ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        authnRequestLookupStrategy = Constraint.isNotNull(strategy, "AuthnRequest lookup strategy cannot be null");
        log.trace("Leaving");
    }
    
    
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
	@Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
    	log.trace("Entering");
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
        	log.trace("Leaving");
            return false;
        }
    
        authnRequest = authnRequestLookupStrategy.apply(profileRequestContext);
        if (authnRequest == null) {
            log.debug("{} AuthnRequest message was not returned by lookup strategy", getLogPrefix());
            //Add StepUpEventIds.EXCEPTION to supported errors, map it
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        log.trace("Leaving");
        return true;
    }
   
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
    	log.trace("Entering");
 
    	final RequestedAuthnContext requestedCtx = authnRequest.getRequestedAuthnContext();
        if (requestedCtx == null || requestedCtx.getAuthnContextClassRefs().isEmpty()) {
            log.debug("{} AuthnRequest did not contain a RequestedAuthnContext, nothing to do", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_REQUESTED);
            log.trace("Leaving");
            return;
        }
        //TODO : COMPARE requested context class ref
        //Return StepUpEventIds.EVENTID_AUTHNCONTEXT_UNKNOWN if no match
        //Another step will then set the value for bypassed authentication
        log.debug("TBD, logic to compare requested context class:" +requestedCtx.getAuthnContextClassRefs().get(0).getAuthnContextClassRef());
        //There is a match
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");
    }
    
    
}
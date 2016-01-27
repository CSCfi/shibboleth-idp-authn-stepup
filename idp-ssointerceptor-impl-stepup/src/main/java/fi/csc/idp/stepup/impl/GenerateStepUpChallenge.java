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
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeSender;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that create step up challenge
 * 
 */

@SuppressWarnings("rawtypes")
public class GenerateStepUpChallenge extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(GenerateStepUpChallenge.class);
      
       
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;
    
    /** The attribute ID to look for. */
    @Nullable private String attributeId;
    
    /** The attribute to match against. */
    @Nullable private IdPAttribute attribute;
    
    /** AttributeContext to filter. */
    @Nullable private AttributeContext attributeContext;
    
    /** Challenge Generator. */
    private ChallengeGenerator challengeGenerator;
    
    /** Challenge Sender. */
    private ChallengeSender challengeSender;
    
    /**
     * Set the login hint parameter names.
     * 
     * @param sender for sending the challenge 
     */
    public void setChallengeSender(@Nonnull ChallengeSender sender) {
        log.trace("Entering");
        challengeSender = sender;
        log.trace("Leaving");
    }
    
    /**
     * Set the login hint parameter names.
     * 
     * @param sender for sending the challenge 
     */
    public void setChallengeGenerator(@Nonnull ChallengeGenerator generator) {
        log.trace("Entering");
        challengeGenerator = generator;
        log.trace("Leaving");
    }

    
    
    /** Constructor. */
    public GenerateStepUpChallenge() {
    	log.trace("Entering");
    	attributeContextLookupStrategy =
                Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                        new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
        log.trace("Leaving");
    }

    
    
    /**
     * Set the lookup strategy for the {@link AttributeContext}.
     * 
     * @param strategy lookup strategy
     */
    
      public void setAttributeContextLookupStrategy(
    		@Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {
    	log.trace("Entering");
    	attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
        log.trace("Leaving");
    }
    
    
    /**
     * Set the attribute ID to look for.
     * 
     * @param id attribute ID to look for
     */
    
    public void setAttributeId(@Nullable String id) {
    	log.trace("Entering");
        attributeId = StringSupport.trimOrNull(id);
        log.trace("Leaving");
    }
    
    
    /** {@inheritDoc} */
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
	@Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {
        
    	log.trace("Entering");
    	attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null) {
            log.error("{} Unable to locate attribute context", getLogPrefix());
            //Add StepUpEventIds.EXCEPTION to supported errors, map it
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        log.debug("{} Found attributeContext '{}'", getLogPrefix(), attributeContext);
        return super.doPreExecute(profileRequestContext, interceptorContext);
    }
   
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {
    	log.trace("Entering");
 
    	
    	//TODO: Move this to PreExecute, maybe we should return false already from there?
    	final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        
        //Check that user has required attribute
        if (!attributeContext.getIdPAttributes().containsKey(attributeId) || 
        		attributeContext.getIdPAttributes().get(attributeId).getValues().isEmpty()){
        	log.debug("Attributes do not contain value for "+attributeId, getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
            log.trace("Leaving");
            return;
        }
        final IdPAttribute attribute = attributeContext.getIdPAttributes().get(attributeId);
        //We search for first string value
        String target = null;
        for (final IdPAttributeValue value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                  target = ((StringAttributeValue) value).getValue();
            }
        }
        if (target == null){
        	log.debug("Attributes did not contain String value for "+attributeId, getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
            log.trace("Leaving");
            return;
        }
        String challenge=challengeGenerator.generate(target);
        if (challenge == null){
        	log.debug("Unable to generate challenge", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        request.getSession().setAttribute("fi.csc.idp.stepup.impl.GenerateStepUpChallenge", challenge);
        challengeSender.send(challenge, target);
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");
        
    }
    
    
}
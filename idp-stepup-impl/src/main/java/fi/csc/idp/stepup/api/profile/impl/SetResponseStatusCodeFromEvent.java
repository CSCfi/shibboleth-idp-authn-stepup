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

package fi.csc.idp.stepup.api.profile.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.http.HttpStatus;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.CurrentOrPreviousEventLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * This action reads an event from the configured {@link EventContext} lookup strategy and sets the status code for
 * {@link HttpServletResponse} according to the attached configuration.
 */
@SuppressWarnings("rawtypes")
public class SetResponseStatusCodeFromEvent extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(SetResponseStatusCodeFromEvent.class);
    
    /** Strategy function for access to {@link EventContext} to check. */
    @Nonnull private Function<ProfileRequestContext,EventContext> eventContextLookupStrategy;
    
    /** Map of eventIds to status codes. */
    private Map<String, Integer> mappedErrors;
    
    /** The status code for unmapped events. */
    private int defaultCode;
    
    /** Constructor. */
    public SetResponseStatusCodeFromEvent() {
        eventContextLookupStrategy = new CurrentOrPreviousEventLookup();
        mappedErrors = new HashMap<>();
        defaultCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    /**
     * Set lookup strategy for {@link EventContext} to check.
     * 
     * @param strategy  lookup strategy
     */
    public void setEventContextLookupStrategy(@Nonnull final Function<ProfileRequestContext,EventContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        eventContextLookupStrategy = Constraint.isNotNull(strategy, "EventContext lookup strategy cannot be null");
    }
    
    /**
     * Set the status code for unmapped events.
     * 
     * @param code The status code for unmapped events.
     */
    public void setDefaultCode(final int code) {
        defaultCode = code;
    }
    
    /**
     * Set map of eventIds to status codes.
     * 
     * @param errors map of eventIds to status codes.
     */
    public void setMappedErrors(@Nonnull final Map<String, Integer> errors) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        mappedErrors = Constraint.isNotNull(errors, "Mapped errors cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final EventContext eventCtx = eventContextLookupStrategy.apply(profileRequestContext);
        if (eventCtx == null || eventCtx.getEvent() == null) {
            log.error("{} No event to be included in the response, nothing to do", getLogPrefix());
            return;
        }
        final String event = eventCtx.getEvent().toString();
        if (mappedErrors.containsKey(event)) {
            log.debug("{} Found mapped event for {}", getLogPrefix(), event);
            getHttpServletResponse().setStatus(mappedErrors.get(event));
        } else {
            log.debug("{} No mapping found for {}, default status code {} set", getLogPrefix(), event, defaultCode);
            getHttpServletResponse().setStatus(defaultCode);
        }
    }
}

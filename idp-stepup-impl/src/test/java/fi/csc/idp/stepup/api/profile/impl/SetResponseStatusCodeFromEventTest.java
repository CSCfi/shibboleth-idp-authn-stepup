/*
 * The MIT License
 * Copyright (c) 2020 CSC - IT Center for Science, http://www.csc.fi
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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.mockito.Mockito;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpEventIds;
import junit.framework.Assert;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;

public class SetResponseStatusCodeFromEventTest {

    private SetResponseStatusCodeFromEvent action;
    private RequestContext src;
    private MockHttpServletResponse servletResponse;
    private Function<ProfileRequestContext, EventContext> eventLookUp;

    @BeforeMethod
    public void setUp() throws ComponentInitializationException, MessageDecodingException {
        src = new RequestContextBuilder().buildRequestContext();
        action = new SetResponseStatusCodeFromEvent();
        servletResponse = new MockHttpServletResponse();
        action.setHttpServletResponse(servletResponse);
        eventLookUp = Mockito.mock(Function.class);
        action.setEventContextLookupStrategy(eventLookUp);
    }

    @Test
    public void testSuccessNoOpNoEvent() throws ComponentInitializationException {
        action.initialize();
        int original = servletResponse.getStatus();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(original, action.getHttpServletResponse().getStatus());
    }

    @Test
    public void testSuccessDefaultCode() throws ComponentInitializationException {
        action.initialize();
        EventContext eventCtx = new EventContext();
        eventCtx.setEvent(StepUpEventIds.EXCEPTION);
        Mockito.when(eventLookUp.apply(Mockito.any())).thenReturn(eventCtx);
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(500, action.getHttpServletResponse().getStatus());
    }

    @Test
    public void testSuccessMappedCode() throws ComponentInitializationException {
        Map<String, Integer> errors = new HashMap<String, Integer>();
        errors.put(StepUpEventIds.EXCEPTION, 451);
        action.setMappedErrors(errors);
        action.initialize();
        EventContext eventCtx = new EventContext();
        eventCtx.setEvent(StepUpEventIds.EXCEPTION);
        Mockito.when(eventLookUp.apply(Mockito.any())).thenReturn(eventCtx);
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(451, action.getHttpServletResponse().getStatus());
    }
}

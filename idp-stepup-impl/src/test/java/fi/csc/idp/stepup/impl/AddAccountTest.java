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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.mockito.Mockito;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class AddAccountTest {

    private AddAccount action;

    private RequestContext src;
    private ProfileRequestContext prc;
    private StepUpMethod method;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new AddAccount();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        method = Mockito.mock(StepUpMethod.class);
        sumCtx.setStepUpMethod(method);
        Mockito.doReturn(Mockito.mock(StepUpAccount.class)).when(method).addAccount();
    }

    @Test
    public void testSuccess() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    @Test
    public void testNoStepUpMethod() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpMethod(null);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }

    @Test
    public void testNoAccountNull() throws Exception {
        Mockito.doReturn(null).when(method).addAccount();
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
}

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

package fi.csc.idp.stepup.impl;

import java.util.Map;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class AddOrRemoveAccountTest {

    private AddOrRemoveAccount action;
    protected RequestContext src;
    protected ProfileRequestContext prc;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new AddOrRemoveAccount();

    }

    @Test
    public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void testNoStepUpMethodContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_STEPUPMETHODCONTEXT);
    }

    @Test
    public void testNoServletRequest() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        stepUpContext.setStepUpAccount(new MockAccount());
        ctx.addSubcontext(stepUpContext, true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    private void baseInit() {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        stepUpContext.setStepUpAccount(new MockAccount());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("parameter", "failure");
        request.addParameter("parameter2", "::");
        request.addParameter("parameter3", "mockMethodName:");
        request.addParameter("parameter5", "mockMethodName:setname");
        request.addParameter("parameter6", "mockMethodName:nocommand");
        request.addParameter("parameter7", "mockMethodName:addaccount");
        request.addParameter("parameter8", "mockMethodName:removeaccount");
        action.setHttpServletRequest(request);
        ctx.addSubcontext(stepUpContext, true);
    }

    @Test
    public void testNoParameterInServletRequest() throws ComponentInitializationException {
        baseInit();
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    @Test
    public void testWrongParam1() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testWrongParam2() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter2");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testWrongParam3() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter3");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testUnsupportedCommand() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        stepUpContext.setStepUpMethod(method);
        action.setUpdateParameter("parameter6");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testAddAccount() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        stepUpContext.setStepUpMethod(method);
        action.setUpdateParameter("parameter7");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    @Test
    public void testRemoveAccount() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        method.addAccount();
        stepUpContext.setStepUpMethod(method);
        action.setUpdateParameter("parameter8");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    class MockMethod implements StepUpMethod {

        public StepUpAccount account;

        public boolean initialize(AttributeContext attributeContext) throws Exception {
            account = new MockAccount();
            account.setName("initialname");
            return true;
        }

        @Override
        public String getName() {
            return "mockMethodName";
        }

        @Override
        public StepUpAccount getAccount() {
            return account;
        }

        @Override
        public StepUpAccount addAccount() throws Exception {
            account = new MockAccount();
            return account;
        }

        @Override
        public void removeAccount(StepUpAccount account) {
            account = null;
        }

        @Override
        public boolean initialize(Map<String, IdPAttribute> attributes) throws Exception {
            // TODO Auto-generated method stub
            return false;
        }
    }

}

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
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;

public class VerifyPasswordFromFormRequestTest {

    private VerifyPasswordFromFormRequest action;

    protected RequestContext src;
    protected ProfileRequestContext prc;

    AuthnContextClassRefPrincipal class1 = new AuthnContextClassRefPrincipal("test1");

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new VerifyPasswordFromFormRequest();

    }

    /** Test that action copes with no authentication context being present */
    @Test
    public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    /** Test that action copes with no step up method context present */
    @Test
    public void testNoStepUpMethodContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_STEPUPMETHODCONTEXT);
    }

    /** Test that action copes with no step up account present */
    @Test
    public void testNoStepUpAccount() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }

    /** Test that action copes with no servlet request present */
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

    private void baseInit(MockAccount account) {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        if (account == null) {
            account = new MockAccount();
        }
        stepUpContext.setStepUpAccount(account);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("parameter_key", "response_failure");
        request.addParameter("parameter_key2", "response_success");
        action.setHttpServletRequest(request);
        ctx.addSubcontext(stepUpContext, true);
    }

    /**
     * Test that action copes with servlet having no challenge response parameter
     */
    @Test
    public void testNoParameterInServletRequest() throws ComponentInitializationException {
        baseInit(null);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    /** Test that action copes with user entering wrong response */
    @Test
    public void testWrongResponse() throws ComponentInitializationException {
        baseInit(null);
        action.setChallengeResponseParameter("parameter_key");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    /** Test that action copes with user entering correct response */
    @Test
    public void testCorrectResponse() throws ComponentInitializationException {
        baseInit(null);
        action.setChallengeResponseParameter("parameter_key2");
        action.initialize();
        final Event event = action.execute(src);
        Assert.assertNull(event);
    }
}

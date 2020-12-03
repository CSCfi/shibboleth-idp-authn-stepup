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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import junit.framework.Assert;
import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class ValidateStepupAuthenticationTest {

    private ValidateStepupAuthentication action;
    private RequestContext src;
    private ProfileRequestContext prc;
    private AuthenticationContext ctx;
    private StepUpMethodContext stepUpContext;

    @BeforeMethod
    public void setUp() throws ComponentInitializationException, MessageDecodingException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("dummy");
        ctx.setAttemptedFlow(flow);
        stepUpContext = new StepUpMethodContext();
        stepUpContext.setSubject("userprincipal");
        ctx.addSubcontext(stepUpContext, true);
        action = new ValidateStepupAuthentication();
        action.initialize();
    }

    @Test
    public void testSuccess() throws ComponentInitializationException {
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        UsernamePrincipal principal = ctx.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class)
                .iterator().next();
        Assert.assertEquals("userprincipal", principal.getName());
    }

    @Test
    public void testNoSubject() throws ComponentInitializationException {
        stepUpContext.setSubject(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_NO_USER);
    }

    @Test
    public void testNoContext() throws ComponentInitializationException {
        ctx.removeSubcontext(StepUpMethodContext.class);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }
}

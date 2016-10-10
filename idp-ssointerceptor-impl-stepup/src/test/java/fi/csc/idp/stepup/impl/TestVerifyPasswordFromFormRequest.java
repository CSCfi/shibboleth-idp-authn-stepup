package fi.csc.idp.stepup.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;

public class TestVerifyPasswordFromFormRequest {

    private VerifyPasswordFromFormRequest action;

    protected RequestContext src;
    @SuppressWarnings("rawtypes")
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
        ShibbolethSpAuthenticationContext sCtx = new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx, true);
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
        ShibbolethSpAuthenticationContext sCtx = new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx, true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        stepUpContext.setStepUpAccount(new account());
        ctx.addSubcontext(stepUpContext, true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    private void baseInit() {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx = new ShibbolethSpAuthenticationContext();
        List<Principal> requested = new ArrayList<Principal>();
        requested.add(class1);
        sCtx.setInitialRequestedContext(requested);
        ctx.addSubcontext(sCtx, true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        stepUpContext.setStepUpAccount(new account());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("parameter_key", "parameter_value");
        request.addParameter("parameter_key2", "parameter_value2");
        action.setHttpServletRequest(request);
        ctx.addSubcontext(stepUpContext, true);
    }

    /**
     * Test that action copes with servlet having no challenge response
     * parameter
     */
    @Test
    public void testNoParameterInServletRequest() throws ComponentInitializationException {
        baseInit();
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    /** Test that action copes with user entering wrong response */
    @Test
    public void testWrongResponse() throws ComponentInitializationException {
        baseInit();
        action.setChallengeResponseParameter("parameter_key");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    /** Test that action copes with user entering correct response */
    @Test
    public void testCorrectResponse() throws ComponentInitializationException {
        baseInit();
        action.setChallengeResponseParameter("parameter_key2");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }

    class account implements StepUpAccount {

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public void setId(long id) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void setEditable(boolean isEditable) {
        }

        @Override
        public void setEnabled(boolean isEnabled) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void sendChallenge() throws Exception {
        }

        @Override
        public boolean verifyResponse(String response) throws Exception {
            return "parameter_value2".equals(response);
        }

        @Override
        public void setTarget(String target) {
        }

        @Override
        public String getTarget() {
            return null;
        }

    }
}

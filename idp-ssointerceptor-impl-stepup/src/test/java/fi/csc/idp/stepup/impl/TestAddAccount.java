package fi.csc.idp.stepup.impl;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
//import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
//import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;

public class TestAddAccount {

    private AddAccount action;

    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new AddAccount();

    }

    /** Test that action copes with no authentication context being present */
    @Test
    public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    /** Test that action copes with no Step Up Method context being present */
    @Test
    public void testNoStepUpMethodContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_STEPUPMETHODCONTEXT);
    }

    /** Test that action copes with no chosen Step Up Method being present */
    @Test
    public void testNoStepUpMethod() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpMethod(null);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }

    // TODO: Create first initializestepupchallengecontext testscases, after
    // that
    // add rest of the cases here

}

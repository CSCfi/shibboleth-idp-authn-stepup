package fi.csc.idp.stepup.impl;

import java.security.Principal;
import java.util.ArrayList;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class TestCheckRequestedAuthenticationContext {

    private CheckRequestedAuthenticationContext action;
    
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new CheckRequestedAuthenticationContext();
        action.initialize();
    }
    
    /**  Test that action copes with no authentication context being present */
    @Test public void testUninitiailizedContext() throws ComponentInitializationException {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }
    
    /**  Test that action copes with no shibboleth context present */
    @Test public void testNoShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with shibboleth context having no initial requested principals list present */
    @Test public void testPartialShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action copes with partial shibboleth context having empty requested principals */
    @Test public void testNoRequested() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx=(ShibbolethSpAuthenticationContext)ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        shibspCtx.setInitialRequestedContext(new ArrayList<Principal>());
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
}

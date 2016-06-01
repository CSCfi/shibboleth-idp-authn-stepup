package fi.csc.idp.stepup.impl;


import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
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
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;


public class TestCheckProvidedAuthenticationContext {

    private CheckProvidedAuthenticationContext action;
    
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new CheckProvidedAuthenticationContext();
        
    }
    
    /**  Test that action copes with no authentication context being present */
    @Test public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }
    
    /**  Test that action copes with no shibboleth context present */
    @Test public void testNoShibbolethContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);
    }
    
    /**  Test that action copes with shibboleth context having no idp parameter */
    @Test public void testNoIdPShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);
    }
    
    /**  Test that action copes with shibboleth context having idp parameter but nothing else */
    @Test public void testOnlyIdpShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        ctx.addSubcontext(sCtx,true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with shibboleth context being initialized but no trusted parties */
    @Test public void testOnlyShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("authnContextClass");
        ctx.addSubcontext(sCtx,true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    /**  Test a case where there is not any whitelisted values for idp  */
    @Test public void testNoIdPMatch() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx,true);
        Map<String, List<Principal>> stepupProviders = new HashMap<String, List<Principal>>();
        List<Principal> stepupMethods = new ArrayList<Principal>();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("test1");
        stepupMethods.add(new AuthnContextClassRefPrincipal("test1"));
        stepupProviders.put("identityProvider2", stepupMethods);
        action.setTrustedStepupProviders(stepupProviders);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    /**  Test a case where there is not matching value for idp  */
    @Test public void testNoMethodMatch() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx,true);
        Map<String, List<Principal>> stepupProviders = new HashMap<String, List<Principal>>();
        List<Principal> stepupMethods = new ArrayList<Principal>();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("test1");
        stepupMethods.add(new AuthnContextClassRefPrincipal("test2"));
        stepupProviders.put("identityProvider", stepupMethods);
        action.setTrustedStepupProviders(stepupProviders);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    /**  Test a case where there is matching value for idp  */
    @Test public void testMatch() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx,true);
        Map<String, List<Principal>> stepupProviders = new HashMap<String, List<Principal>>();
        List<Principal> stepupMethods = new ArrayList<Principal>();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("test1");
        stepupMethods.add(new AuthnContextClassRefPrincipal("test1"));
        stepupProviders.put("identityProvider", stepupMethods);
        action.setTrustedStepupProviders(stepupProviders);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_STEPUP);
    }
}

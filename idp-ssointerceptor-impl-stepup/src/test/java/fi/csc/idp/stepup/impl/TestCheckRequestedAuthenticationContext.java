package fi.csc.idp.stepup.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
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
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;


public class TestCheckRequestedAuthenticationContext {

    private CheckRequestedAuthenticationContext action;
    
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    
    AuthnContextClassRefPrincipal class1=new AuthnContextClassRefPrincipal("test1");
    AuthnContextClassRefPrincipal class2=new AuthnContextClassRefPrincipal("test2");
    AuthnContextClassRefPrincipal class3=new AuthnContextClassRefPrincipal("test3");
    AuthnContextClassRefPrincipal class4=new AuthnContextClassRefPrincipal("test4");
    
    AuthnContextDeclRefPrincipal decl1=new AuthnContextDeclRefPrincipal("test1");
    AuthnContextDeclRefPrincipal decl2=new AuthnContextDeclRefPrincipal("test2");
    AuthnContextDeclRefPrincipal decl3=new AuthnContextDeclRefPrincipal("test3");
    AuthnContextDeclRefPrincipal decl4=new AuthnContextDeclRefPrincipal("test4");
    
    
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new CheckRequestedAuthenticationContext();
        
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
    
    /**  Test that action copes with shibboleth context having no initial requested principals list present */
    @Test public void testPartialShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx=(ShibbolethSpAuthenticationContext)ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        shibspCtx.setIdp("idp");
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action copes with partial shibboleth context having empty requested principals */
    @Test public void testNoRequested() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx=(ShibbolethSpAuthenticationContext)ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        shibspCtx.setIdp("idp");
        shibspCtx.setInitialRequestedContext(new ArrayList<Principal>());
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action distinguishes classrefs from declrefs */
    @Test public void testNonMatchingPrincipals() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx=(ShibbolethSpAuthenticationContext)ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        List<Principal> requested=new ArrayList<Principal>();
        List<Principal> stepups=new ArrayList<Principal>();
        requested.add(class1);
        requested.add(class2);       
        requested.add(decl3);
        requested.add(decl4);
        stepups.add(decl1);
        stepups.add(decl2);
        stepups.add(class3);
        stepups.add(class4);
        shibspCtx.setIdp("idp");
        shibspCtx.setInitialRequestedContext(requested);
        action.setStepupMethods(stepups);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action is able to find a match */
    @Test public void testMatchingPrincipals() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx=(ShibbolethSpAuthenticationContext)ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        shibspCtx.setIdp("idp");
        AuthnContextClassRefPrincipal localClass4=new AuthnContextClassRefPrincipal("test4");
        List<Principal> requested=new ArrayList<Principal>();
        List<Principal> stepups=new ArrayList<Principal>();
        requested.add(class1);
        requested.add(class4);       
        stepups.add(class2);
        //should match requested class4
        stepups.add(localClass4);
        shibspCtx.setInitialRequestedContext(requested);
        action.setStepupMethods(stepups);
        action.initialize();
        final Event event=action.execute(src);
        Assert.assertNull(event);
    }
    
}

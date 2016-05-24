package fi.csc.idp.stepup.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;


public class TestSetRequestedAuthenticationContext {

    private SetRequestedAuthenticationContext action;
    
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new SetRequestedAuthenticationContext();
        
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
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    
    /**  Test that action copes with shibboleth context having no idp parameter */
    @Test public void testNoIdPShibbolethContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ctx.addSubcontext(new ShibbolethSpAuthenticationContext(),true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with having auth contexct and shibboleth context  but no id in relying party context */
    @Test public void testNoRPContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        ctx.addSubcontext(sCtx,true);
        RelyingPartyContext rpCtx=prc.getSubcontext(RelyingPartyContext.class,false);
        rpCtx.setRelyingPartyId(null);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with having idp nut no method is shib context */
    @Test public void testNoMethod() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass(null);
        sCtx.setContextDecl(null);
        ctx.addSubcontext(sCtx,true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  test action with basic success case, initialized but no data to match to  */
    @Test public void basicSuccess() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("test");
        ctx.addSubcontext(sCtx,true);
        action.initialize();
        RequestedPrincipalContext reqPrincipalContext=ctx.getSubcontext(RequestedPrincipalContext.class,true);
        AuthnContextClassRefPrincipal initialRef=new AuthnContextClassRefPrincipal("InititialRequest");
        reqPrincipalContext.setMatchingPrincipal(initialRef);
        final Event event=action.execute(src);
        Assert.assertEquals(initialRef,reqPrincipalContext.getMatchingPrincipal());      
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    private  Map<String,List<String>> getEmptyDefaultMap(){
        Map<String,List<String>> defaults = new HashMap<String,List<String>>();
        return defaults;
    }
   
    private  Map<String,List<String>> getEmptyNullListDefaultMap(){
        Map<String,List<String>> defaults = new HashMap<String,List<String>>();
        defaults.put("idp1", null);
        return defaults;
    }
    
    private  Map<String,List<String>> getDefaultMap1(){
        Map<String,List<String>> defaults = new HashMap<String,List<String>>();
        List<String> values=new ArrayList<String>();
        values.add("sp1");
        values.add("sp2");
        defaults.put("idp1", values);
        defaults.put("idp2", values);
        return defaults;
    }
    
    /**  test action with basic success case, initialized but only with empty default map  */
    
    @Test public void basicSuccessEmptyMap() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("identityProvider");
        sCtx.setContextClass("test");
        ctx.addSubcontext(sCtx,true);
        action.setPassThruuEntityLists(getEmptyDefaultMap());
        action.initialize();
        RequestedPrincipalContext reqPrincipalContext=ctx.getSubcontext(RequestedPrincipalContext.class,true);
        AuthnContextClassRefPrincipal initialRef=new AuthnContextClassRefPrincipal("InitialRequest");
        reqPrincipalContext.setMatchingPrincipal(initialRef);
        final Event event=action.execute(src);
        Assert.assertEquals(initialRef,reqPrincipalContext.getMatchingPrincipal());      
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    /**  test action with basic success case, initialized but only with broken default map  */
    
    @Test public void basicSuccessBrokenMap() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("idp1");
        sCtx.setContextClass("test");
        ctx.addSubcontext(sCtx,true);
        action.setPassThruuEntityLists(getEmptyNullListDefaultMap());
        action.initialize();
        RequestedPrincipalContext reqPrincipalContext=ctx.getSubcontext(RequestedPrincipalContext.class,true);
        AuthnContextClassRefPrincipal initialRef=new AuthnContextClassRefPrincipal("InitialRequest");
        reqPrincipalContext.setMatchingPrincipal(initialRef);
        final Event event=action.execute(src);
        Assert.assertEquals(initialRef,reqPrincipalContext.getMatchingPrincipal());      
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    
    /**  test action with basic success case, mapping instructs to use the value provided by idp  */
    @Test public void basicSuccessDefaultMap() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        sCtx.setIdp("idp1");
        sCtx.setContextClass("IdPResponse");
        ctx.addSubcontext(sCtx,true);
        action.setPassThruuEntityLists(getDefaultMap1());
        action.initialize();
        RelyingPartyContext rpCtx=prc.getSubcontext(RelyingPartyContext.class,false);
        rpCtx.setRelyingPartyId("sp1");
        RequestedPrincipalContext reqPrincipalContext=ctx.getSubcontext(RequestedPrincipalContext.class,true);
        AuthnContextClassRefPrincipal initialRef=new AuthnContextClassRefPrincipal("InitialRequest");
        reqPrincipalContext.setMatchingPrincipal(initialRef);
        final Event event=action.execute(src);
        Assert.assertEquals(sCtx.getContextClass(),reqPrincipalContext.getMatchingPrincipal().getName());      
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
}

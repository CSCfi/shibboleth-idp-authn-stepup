package fi.csc.idp.stepup.impl;



import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.StepUpContext;
import fi.csc.idp.stepup.api.StepUpEventIds;
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
    
    AuthnContextClassRefPrincipal class1=new AuthnContextClassRefPrincipal("test1");
    
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new VerifyPasswordFromFormRequest();
        
    }
    
    /**  Test that action copes with no authentication context being present */
    @Test public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }
    
    /**  Test that action copes with no shibboleth authentication context present */
    @Test public void testNoAttributeContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with no stepup context present */
    @Test public void testNoStepupContext() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx,true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with no servlet request present */
    @Test public void testNoServletRequest() throws ComponentInitializationException {
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx,true);
        StepUpContext stepUpContext = new StepUpContext();
        ctx.addSubcontext(stepUpContext,true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    private void baseInit(){
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        List<Principal> requested=new ArrayList<Principal>();
        requested.add(class1);
        sCtx.setInitialRequestedContext(requested);
        ctx.addSubcontext(sCtx,true);
        StepUpContext stepUpContext = new StepUpContext();
        stepUpContext.setChallenge("parameter_value2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("parameter_key", "parameter_value");
        request.addParameter("parameter_key2", "parameter_value2");
        action.setHttpServletRequest(request);
        ctx.addSubcontext(stepUpContext,true);
    }

    /**  Test that action copes with servlet having no challenge response parameter */
    @Test public void testNoParameterInServletRequest() throws ComponentInitializationException {
        baseInit();
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }
    
    /**  Test that action copes with having no matching verifier implementation */
    @Test public void testNoVerifier() throws ComponentInitializationException {
        baseInit();
        action.setChallengeResponseParameter("parameter_key");
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action copes with user entering wrong response */
    @Test public void testWrongResponse() throws ComponentInitializationException {
        baseInit();
        Map<Principal, ChallengeVerifier> verifiers = new HashMap<Principal, ChallengeVerifier>();
        verifiers.put(class1, new EqualChallengeResponseVerifier());
        action.setChallengeVerifiers(verifiers);
        action.setChallengeResponseParameter("parameter_key");
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }
    
    /**  success */
    @Test public void testSuccess() throws ComponentInitializationException {
        baseInit();
        Map<Principal, ChallengeVerifier> verifiers = new HashMap<Principal, ChallengeVerifier>();
        verifiers.put(class1, new EqualChallengeResponseVerifier());
        action.setChallengeVerifiers(verifiers);
        action.setChallengeResponseParameter("parameter_key2");
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
}

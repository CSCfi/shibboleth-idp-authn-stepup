package fi.csc.idp.stepup.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeSender;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;


public class TestGenerateStepUpChallenge {

    private GenerateStepUpChallenge action;
    
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
        action = new GenerateStepUpChallenge();
        
    }
    
    
    /**  Test that action copes with no authentication context being present */
    @Test public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }
    
    /**  Test that action copes with no attribute context present */
    @Test public void testNoAttributeContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  Test that action copes with attribute context but no shib context present */
    @Test public void testNoShibbolethContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        final AttributeContext attribCtx = new AttributeContext();
        RelyingPartyContext rpCtx=prc.getSubcontext(RelyingPartyContext.class,true);
        rpCtx.addSubcontext(attribCtx);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);
    }
    
    
    
    private void baseInit(){
        AuthenticationContext ctx=(AuthenticationContext)prc.addSubcontext(new AuthenticationContext(), true);
        final AttributeContext attribCtx = new AttributeContext();
        final IdPAttribute attribute1 = new IdPAttribute("attr1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("foo@bar")));
        final IdPAttribute attribute2 = new IdPAttribute("attr2");
        attribute2.setValues(Arrays.asList(new ByteAttributeValue(new byte[1])));
        attribCtx.setIdPAttributes(Arrays.asList(attribute1,attribute2));
        
        RelyingPartyContext rpCtx=prc.getSubcontext(RelyingPartyContext.class,true);
        rpCtx.addSubcontext(attribCtx);
        ShibbolethSpAuthenticationContext sCtx=new ShibbolethSpAuthenticationContext();
        List<Principal> requested=new ArrayList<Principal>();
        requested.add(class1);
        requested.add(class2);       
        requested.add(decl3);
        requested.add(decl4);
        sCtx.setInitialRequestedContext(requested);
        ctx.addSubcontext(sCtx,true);
    }
    
    /**  Test that action copes with stepup attribute not being found 
     * @throws ComponentInitializationException */
    @Test public void testMismatchAttribute() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        ids.put(class2, "nonexistent");
        action.setAttributeIds(ids);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }
    
    /**  Test that action copes with non string attribute being found 
     * @throws ComponentInitializationException */
    @Test public void testMismatchAttribute2() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        ids.put(class2, "attr2");
        action.setAttributeIds(ids);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }
    
    /**  Test that action copes with no challenge generator implementation 
     * @throws ComponentInitializationException */
    @Test public void testNoGenerator() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        action.setAttributeIds(ids);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action copes with no challenge sender implementation 
     * @throws ComponentInitializationException */
    @Test public void testNoSender() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        Map<Principal, ChallengeGenerator> generators = new HashMap<Principal, ChallengeGenerator>();
        generators.put(class1, new DigestChallengeGenerator());
        action.setChallengeGenerators(generators);
        action.setAttributeIds(ids);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
    }
    
    /**  Test that action copes with challenge sender implementation failure 
     * @throws ComponentInitializationException */
    @Test public void testInvalidSender() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        ids.put(class2, "attr1");
        Map<Principal, ChallengeGenerator> generators = new HashMap<Principal, ChallengeGenerator>();
        generators.put(class1, new DigestChallengeGenerator());
        Map<Principal, ChallengeSender> senders = new HashMap<Principal, ChallengeSender>();
        senders.put(class1, new MailChallengeSender());
        action.setAttributeIds(ids);
        action.setChallengeGenerators(generators);
        action.setChallengeSenders(senders);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }
    
    /**  success case 
     * @throws ComponentInitializationException */
    @Test public void testSuccess() throws ComponentInitializationException  {
        baseInit();
        Map<Principal, String> ids = new HashMap<Principal, String>();
        Map<Principal, ChallengeGenerator> generators = new HashMap<Principal, ChallengeGenerator>();
        generators.put(class1, new DigestChallengeGenerator());
        Map<Principal, ChallengeSender> senders = new HashMap<Principal, ChallengeSender>();
        senders.put(class1, new LogChallengeSender());
        action.setAttributeIds(ids);
        action.setChallengeGenerators(generators);
        action.setChallengeSenders(senders);
        action.initialize();
        final Event event=action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
       
}

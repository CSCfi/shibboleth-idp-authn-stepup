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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;

public class TestInitializeStepUpChallengeContext {

    private InitializeStepUpChallengeContext action;
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new InitializeStepUpChallengeContext();
    }

    /** Test that action copes with no authentication context being present */
    @Test
    public void testUninitiailizedContext() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    /** Test that action copes with no Attribute context being present */
    @Test
    public void testNoAttributeContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_ATTRIBUTECONTEXT);
    }

    /** Test that action copes with no shibboleth sp context present */
    @Test
    public void testNoShibbolethContext() throws ComponentInitializationException {
        prc.addSubcontext(new AuthenticationContext(), true);
        final AttributeContext attributeCtx = new AttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);
    }

    /** Test that action copes with no step up methods defined */
    @Test
    public void testNoStepUpMethods() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        ctx.addSubcontext(
                new ShibbolethSpAuthenticationContext(), true);
        final AttributeContext attributeCtx = new AttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    /** Test that action copes with no matching step up methods defined */
    @Test
    public void testNoMatchingStepUpMethods() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx = (ShibbolethSpAuthenticationContext) ctx.addSubcontext(
                new ShibbolethSpAuthenticationContext(), true);
        final AttributeContext attributeCtx = new AttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        Map<Principal, StepUpMethod> methods = new HashMap<Principal, StepUpMethod>();
        List<Principal> rcl = new ArrayList<Principal>();
        rcl.add(new AuthnContextClassRefPrincipal("test"));
        shibspCtx.setInitialRequestedContext(rcl);
        methods.put(new AuthnContextClassRefPrincipal("not_test"), new method());
        action.setStepUpMethods(methods);
        action.initialize();
        action.execute(src);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.getSubcontext(StepUpMethodContext.class);
        Assert.assertEquals(sumCtx.getStepUpMethods().size(), 1);
        Assert.assertNull(sumCtx.getStepUpMethod());
        Assert.assertNull(sumCtx.getStepUpAccount());
    }

    /** Test that action copes with 2 matching step up methods defined */
    @Test
    public void testMatchingStepUpMethods() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext shibspCtx = (ShibbolethSpAuthenticationContext) ctx.addSubcontext(
                new ShibbolethSpAuthenticationContext(), true);
        final AttributeContext attributeCtx = new AttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        Map<Principal, StepUpMethod> methods = new HashMap<Principal, StepUpMethod>();
        List<Principal> rcl = new ArrayList<Principal>();
        rcl.add(new AuthnContextClassRefPrincipal("test_no_match"));
        rcl.add(new AuthnContextClassRefPrincipal("test"));
        shibspCtx.setInitialRequestedContext(rcl);
        methods.put(new AuthnContextClassRefPrincipal("not_test"), new method());
        methods.put(new AuthnContextClassRefPrincipal("test"), new method());
        methods.put(new AuthnContextClassRefPrincipal("not_test"), new method());
        methods.put(new AuthnContextClassRefPrincipal("test"), new method());
        action.setStepUpMethods(methods);
        action.initialize();
        action.execute(src);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.getSubcontext(StepUpMethodContext.class);
        Assert.assertEquals(sumCtx.getStepUpMethods().size(), 2);
        Assert.assertNotNull(sumCtx.getStepUpMethod());
        Assert.assertNotNull(sumCtx.getStepUpAccount());
    }

    /** helper classes for testing -> */
    class method implements StepUpMethod {

        List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();

        @Override
        public boolean initialize(AttributeContext attributeContext) throws Exception {
            return true;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public List<StepUpAccount> getAccounts() throws Exception {
            if (accounts.isEmpty()) {
                accounts.add(new account());
            }
            return accounts;
        }

        @Override
        public StepUpAccount addAccount() throws Exception {
            return null;
        }

        @Override
        public void removeAccount(StepUpAccount account) {

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
                return true;
            }

            @Override
            public void sendChallenge() throws Exception {
            }

            @Override
            public boolean verifyResponse(String response) throws Exception {
                return false;
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

}

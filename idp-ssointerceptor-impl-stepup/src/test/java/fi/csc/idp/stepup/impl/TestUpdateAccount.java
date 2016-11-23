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
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;

public class TestUpdateAccount {

    private UpdateAccount action;

    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;

    AuthnContextClassRefPrincipal class1 = new AuthnContextClassRefPrincipal("test1");

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new UpdateAccount();

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

    /** Test that action copes with no servlet request present */
    @Test
    public void testNoServletRequest() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        ShibbolethSpAuthenticationContext sCtx = new ShibbolethSpAuthenticationContext();
        ctx.addSubcontext(sCtx, true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        stepUpContext.setStepUpAccount(new MockAccount());
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
        stepUpContext.setStepUpAccount(new MockAccount());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("parameter", "failure");
        request.addParameter("parameter2", "::");
        request.addParameter("parameter3", "mockMethodName:x:");
        request.addParameter("parameter4", "mockMethodName:1:");
        request.addParameter("parameter5", "mockMethodName:1:setname");
        request.addParameter("parameter6", "mockMethodName:1:nocommand");
        request.addParameter("parameter7", "mockMethodName:-1:addaccount");
        request.addParameter("parameter8", "newname");
        action.setHttpServletRequest(request);
        ctx.addSubcontext(stepUpContext, true);
    }

    /**
     * Test that action copes with servlet having no update parameter
     */
    @Test
    public void testNoParameterInServletRequest() throws ComponentInitializationException {
        baseInit();
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_RESPONSE);
    }

    /** Test that action copes with different invalid action parameters */

    @Test
    public void testWrongParam1() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testWrongParam2() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter2");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testWrongParam3() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter3");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testWrongParam4() throws ComponentInitializationException {
        baseInit();
        action.setUpdateParameter("parameter4");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    /**
     * Test that action name update is a success
     * 
     * @throws Exception
     */

    @Test
    public void testUnsupportedCommand() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        Map<StepUpMethod, List<? extends Principal>> stepUpMethods = new HashMap<StepUpMethod, List<? extends Principal>>();
        List<Principal> ctxs = new ArrayList<Principal>();
        ctxs.add(new AuthnContextClassRefPrincipal("test"));
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        stepUpMethods.put(method, ctxs);
        stepUpContext.setStepUpMethods(stepUpMethods);
        action.setUpdateParameter("parameter6");
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    /**
     * Test that action name update is a success
     * 
     * @throws Exception
     */

    @Test
    public void testNameUpdate() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        Map<StepUpMethod, List<? extends Principal>> stepUpMethods = new HashMap<StepUpMethod, List<? extends Principal>>();
        List<Principal> ctxs = new ArrayList<Principal>();
        ctxs.add(new AuthnContextClassRefPrincipal("test"));
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        stepUpMethods.put(method, ctxs);
        stepUpContext.setStepUpMethods(stepUpMethods);
        action.setUpdateParameter("parameter5");
        action.setNameParameter("parameter8");
        action.initialize();
        final Event event = action.execute(src);
        Assert.assertEquals(method.getAccounts().get(0).getName(), "newname");
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }

    /**
     * Test that action name update is a success
     * 
     * @throws Exception
     */

    @Test
    public void testAddAccount() throws Exception {
        baseInit();
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext stepUpContext = new StepUpMethodContext();
        ctx.addSubcontext(stepUpContext, true);
        Map<StepUpMethod, List<? extends Principal>> stepUpMethods = new HashMap<StepUpMethod, List<? extends Principal>>();
        List<Principal> ctxs = new ArrayList<Principal>();
        ctxs.add(new AuthnContextClassRefPrincipal("test"));
        StepUpMethod method = new MockMethod();
        method.initialize(null);
        stepUpMethods.put(method, ctxs);
        stepUpContext.setStepUpMethods(stepUpMethods);
        action.setUpdateParameter("parameter7");
        action.initialize();
        final Event event = action.execute(src);
        Assert.assertEquals(method.getAccounts().size(), 2);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }

    class MockMethod implements StepUpMethod {

        public StepUpAccount account;
        List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();

        @Override
        public boolean initialize(AttributeContext attributeContext) throws Exception {
            account = new MockAccount();
            account.setId(1);
            account.setName("initialname");
            accounts.add(account);
            return true;
        }

        @Override
        public String getName() {
            return "mockMethodName";
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public List<StepUpAccount> getAccounts() {
            return accounts;
        }

        @Override
        public StepUpAccount addAccount() throws Exception {
            MockAccount mockAccount = new MockAccount();
            accounts.add(mockAccount);
            return mockAccount;
        }

        @Override
        public void removeAccount(StepUpAccount account) {
        }

        @Override
        public void updateAccount(StepUpAccount account) throws Exception {
            accounts.get(0).setName(account.getName());
            accounts.get(0).setEditable(account.isEditable());
            accounts.get(0).setEnabled(account.isEnabled());
            accounts.get(0).setId(account.getId());
        }

    }

}

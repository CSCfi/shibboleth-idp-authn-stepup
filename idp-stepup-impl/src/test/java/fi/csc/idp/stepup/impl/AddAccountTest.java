package fi.csc.idp.stepup.impl;

import java.util.ArrayList;
import java.util.List;

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
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class AddAccountTest {

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

    
    /** Test that action copes with account creation failing 
    @Test
    public void testAccountCreationFails() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpMethod(new method());
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    /** Test that action copes with account creation throwing error 
    @Test
    public void testAccountThrowsError() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpMethod(new method2());
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    /** Test that action is able to succeed 
    @Test
    public void testAccountCreationSucceeds() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpMethod(new method3());
        action.initialize();
        final Event event = action.execute(src);
        Assert.assertNull(event);
    }

    */
    /** helper classes for testing -> */
    /*
    class method3 extends method {
        @Override
        public StepUpAccount addAccount() throws Exception {
            return new MockAccount();
        }
    }

    class method2 extends method {
        @Override
        public StepUpAccount addAccount() throws Exception {
            throw new Exception("terrible");
        }
    }
    */

    /*
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
        public List<StepUpAccount> getAccounts() {
            if (accounts.isEmpty()) {
                accounts.add(new MockAccount());
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

        @Override
        public void updateAccount(StepUpAccount account) throws Exception {
            // TODO Auto-generated method stub

        }

    }
    */

}

package fi.csc.idp.stepup.impl;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeSender;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class GenerateStepUpChallengeTest {

    private GenerateStepUpChallenge action;
    private String sentChallenge;
    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        action = new GenerateStepUpChallenge();
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

    /** Test that action copes with no chosen Step Up Account being present */
    @Test
    public void testNoStepUpAccount() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpAccount(null);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_INVALID_USER);
    }

    /** Test that action copes with invalid Step Up Account being present */
    @Test
    public void testInvalidStepUpAccount() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        sumCtx.setStepUpAccount(new ChallengeSenderStepUpAccount());
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
    }

    @Test
    public void testSuccess() throws ComponentInitializationException {
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        StepUpMethodContext sumCtx = (StepUpMethodContext) ctx.addSubcontext(new StepUpMethodContext(), true);
        ChallengeSenderStepUpAccount challengeSenderStepUpAccount = new ChallengeSenderStepUpAccount();
        challengeSenderStepUpAccount.setChallengeGenerator(new ChallengeGen());
        challengeSenderStepUpAccount.setChallengeSender(new ChallengeSen());
        sumCtx.setStepUpAccount(challengeSenderStepUpAccount);
        action.initialize();
        final Event event = action.execute(src);
        Assert.assertEquals("challengeGenerated", sentChallenge);
        Assert.assertNull(event);
    }

    class ChallengeGen implements ChallengeGenerator {

        @Override
        public String generate(String target) throws Exception {
            return "challengeGenerated";
        }

    }

    class ChallengeSen implements ChallengeSender {

        @Override
        public void send(String challenge, String target) throws Exception {
            sentChallenge = challenge;
        }

    }

}

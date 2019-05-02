package fi.csc.idp.stepup.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeSender;

public class ChallengeSenderStepUpAccountTest {

    private ChallengeSenderStepUpAccount challengeSenderStepUpAccount;
    private String sentChallenge;

    @BeforeMethod
    public void setUp() {
        challengeSenderStepUpAccount = new ChallengeSenderStepUpAccount();
    }

    /** test uninitialized behavior */
    @Test
    public void testUnitialized() {
        boolean exception = false;
        try {
            challengeSenderStepUpAccount.setChallengeGenerator(new ChallengeGen());
            challengeSenderStepUpAccount.sendChallenge();
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Bean not configured with ChallengeSender", e.getMessage());
        }
        Assert.assertTrue(exception);
    }

    public void test() throws Exception {
        challengeSenderStepUpAccount.setChallengeSender(new ChallengeSen());
        challengeSenderStepUpAccount.setChallengeGenerator(new ChallengeGen());
        challengeSenderStepUpAccount.sendChallenge();
        Assert.assertEquals("challengeGenerated", sentChallenge);
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

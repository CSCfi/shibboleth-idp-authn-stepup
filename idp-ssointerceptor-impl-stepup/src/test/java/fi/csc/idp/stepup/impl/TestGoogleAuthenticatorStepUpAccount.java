package fi.csc.idp.stepup.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGoogleAuthenticatorStepUpAccount {

    private GoogleAuthenticatorStepUpAccount gaStepUpAccount;

    @BeforeMethod
    public void setUp() {
        gaStepUpAccount = new GoogleAuthenticatorStepUpAccount();
    }

    @Test
    public void testTarget() {
        /** We expect string of 16 characters */
        Assert.assertNotNull(gaStepUpAccount.getTarget());
        Assert.assertEquals(gaStepUpAccount.getTarget().length(), 16);
    }

    @Test
    public void testSending() throws Exception {
        /** dummy method, must work uninitialized */
        gaStepUpAccount.sendChallenge();
    }

    public void testFailingVerification() throws Exception {
        Assert.assertTrue(!gaStepUpAccount.verifyResponse("123"));
        // TODO: success case
    }

}

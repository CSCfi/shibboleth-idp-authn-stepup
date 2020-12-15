/*
 * The MIT License
 * Copyright (c) 2020 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeVerifier;

public class AbstractStepUpAccountTest {

    private TestStepUpAccount testStepUpAccount;
    private ChallengeGen challengeGen;
    private ChallengeVer challengeVer;

    @BeforeMethod
    public void setUp() {
        testStepUpAccount = new TestStepUpAccount();
        challengeGen = new ChallengeGen();
        challengeVer = new ChallengeVer();
    }

    @Test
    public void testUnitialized() throws Exception {
        Assert.assertNull(testStepUpAccount.getChallenge());
        Assert.assertNull(testStepUpAccount.getName());
        Assert.assertNull(testStepUpAccount.getTarget());
    }

    private void setValues() {
        testStepUpAccount.setTarget("target");
        testStepUpAccount.setName("name");
    }

    private void setValues2() {
        testStepUpAccount.setTarget("target2");
        testStepUpAccount.setName("name2");
    }

    @Test
    public void testSetters() throws Exception {
        setValues();
        Assert.assertEquals(testStepUpAccount.getTarget(), "target");
        Assert.assertEquals(testStepUpAccount.getName(), "name");
        setValues2();
        Assert.assertEquals(testStepUpAccount.getTarget(), "target2");
        Assert.assertEquals(testStepUpAccount.getName(), "name2");

    }

    @Test
    public void testSerialization() {
        TestStepUpAccount account = new TestStepUpAccount();
        account.deserializeAccountInformation(testStepUpAccount.serializeAccountInformation());
        Assert.assertEquals(testStepUpAccount.getTarget(), account.getTarget());
        Assert.assertEquals(testStepUpAccount.getName(), account.getName());
        testStepUpAccount.setTarget("Target");
        testStepUpAccount.setName("Name");
        account.deserializeAccountInformation(testStepUpAccount.serializeAccountInformation());
        Assert.assertEquals(testStepUpAccount.getTarget(), account.getTarget());
        Assert.assertEquals(testStepUpAccount.getName(), account.getName());
    }

    @Test
    public void testChallengeVerificator() {
        boolean exceptionOccurred = false;
        try {
            testStepUpAccount.verifyResponse("response");
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);
        exceptionOccurred = false;
        testStepUpAccount.setChallengeGenerator(challengeGen);
        testStepUpAccount.setChallengeVerifier(challengeVer);
        try {
            testStepUpAccount.sendChallenge();
            Assert.assertTrue(testStepUpAccount.verifyResponse("response"));
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(!exceptionOccurred);
    }

    class TestStepUpAccount extends AbstractStepUpAccount {

        @Override
        protected void doSendChallenge() throws Exception {
        }
    }

    class ChallengeGen implements ChallengeGenerator {

        @Override
        public String generate(String target) throws Exception {
            return "challengeGenerated";
        }
    }

    class ChallengeVer implements ChallengeVerifier {

        @Override
        public boolean verify(String challenge, String response, String target) {
            return response.equals("response") && challenge.equals("challengeGenerated");
        }
    }
}

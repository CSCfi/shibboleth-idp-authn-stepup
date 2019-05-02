package fi.csc.idp.stepup.impl;

import java.util.HashMap;
import java.util.Map;

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
        Map<Long, Integer> accountEventLimits=new HashMap<Long, Integer>();
        //5th event within second will hit limits 
        accountEventLimits.put((long)1000, (int)5);
        //2nd fail event within second will hit limits
        Map<Long, Integer> accountFailEventLimits=new HashMap<Long, Integer>();
        accountFailEventLimits.put((long)1000, (int)2);
    }

    /** test default behavior */
    @Test
    public void testUnitialized() throws Exception {
        Assert.assertNull(testStepUpAccount.getChallenge());
        Assert.assertNull(testStepUpAccount.getName());
        Assert.assertEquals(testStepUpAccount.getId(), 0);
        Assert.assertNull(testStepUpAccount.getTarget());
        Assert.assertTrue(testStepUpAccount.isEditable());
        Assert.assertTrue(!testStepUpAccount.isEnabled());
    }

    private void setValues() {
        testStepUpAccount.setTarget("target");
        testStepUpAccount.setName("name");
        testStepUpAccount.setId(1);
        testStepUpAccount.setEnabled(true);
        testStepUpAccount.setEditable(false);
    }

    private void setValues2() {
        testStepUpAccount.setTarget("target2");
        testStepUpAccount.setName("name2");
        testStepUpAccount.setId(2);
        testStepUpAccount.setEnabled(false);
        testStepUpAccount.setEditable(true);
    }

    /** test setters behavior */
    @Test
    public void testSetters() throws Exception {
        setValues();
        Assert.assertEquals(testStepUpAccount.getTarget(), "target");
        Assert.assertEquals(testStepUpAccount.getName(), "name");
        Assert.assertEquals(testStepUpAccount.getId(), 1);
        Assert.assertTrue(testStepUpAccount.isEnabled());
        Assert.assertTrue(!testStepUpAccount.isEditable());
        // editing disabled, should not have any impact
        setValues2();
        Assert.assertEquals(testStepUpAccount.getTarget(), "target");
        Assert.assertEquals(testStepUpAccount.getName(), "name");
        Assert.assertEquals(testStepUpAccount.getId(), 1);
        Assert.assertTrue(testStepUpAccount.isEnabled());
        Assert.assertTrue(!testStepUpAccount.isEditable());

    }

    /** test account serialization */
	@Test
	public void testSerialization() {
		TestStepUpAccount account = new TestStepUpAccount();
		// Uninitialized account
		account.deserializeAccountInformation(testStepUpAccount.serializeAccountInformation());
		Assert.assertEquals(testStepUpAccount.getTarget(), account.getTarget());
		Assert.assertEquals(testStepUpAccount.getName(), account.getName());
		Assert.assertEquals(testStepUpAccount.getId(), account.getId());
		Assert.assertEquals(testStepUpAccount.isEnabled(), account.isEnabled());
		Assert.assertEquals(testStepUpAccount.isEditable(), account.isEditable());
		// Initialized account
		testStepUpAccount.setTarget("Target");
		testStepUpAccount.setName("Name");
		testStepUpAccount.setId(101010L);
		testStepUpAccount.setEnabled(true);
		testStepUpAccount.setEditable(false);
		account.deserializeAccountInformation(testStepUpAccount.serializeAccountInformation());
		Assert.assertEquals(testStepUpAccount.getTarget(), account.getTarget());
		Assert.assertEquals(testStepUpAccount.getName(), account.getName());
		Assert.assertEquals(testStepUpAccount.getId(), account.getId());
		Assert.assertEquals(testStepUpAccount.isEnabled(), account.isEnabled());
		Assert.assertEquals(testStepUpAccount.isEditable(), account.isEditable());
	}
    
    /** test challenge verification behavior */
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

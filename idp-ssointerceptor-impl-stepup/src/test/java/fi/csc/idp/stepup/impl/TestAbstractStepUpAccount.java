package fi.csc.idp.stepup.impl;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.LimitReachedException;
import fi.csc.idp.stepup.event.impl.AccountRestrictor;
import fi.csc.idp.stepup.event.impl.InMemoryEventStore;

public class TestAbstractStepUpAccount {

    private TestStepUpAccount testStepUpAccount;
    private ChallengeGen challengeGen;
    private ChallengeVer challengeVer;
    AccountRestrictor ar=new AccountRestrictor();

    @BeforeMethod
    public void setUp() {
        testStepUpAccount = new TestStepUpAccount();
        challengeGen = new ChallengeGen();
        challengeVer = new ChallengeVer();
        InMemoryEventStore store = new InMemoryEventStore();
        ar.setEventStore(store);
        ar.setType("type_1");
        ar.setKey("key_1");
        Map<Long, Integer> accountEventLimits=new HashMap<Long, Integer>();
        //5th event within second will hit limits 
        accountEventLimits.put((long)1000, (int)5);
        //2nd fail event within second will hit limits
        Map<Long, Integer> accountFailEventLimits=new HashMap<Long, Integer>();
        accountFailEventLimits.put((long)1000, (int)2);
        ar.setAccountEventLimits(accountEventLimits);
        ar.setAccountFailEventLimits(accountFailEventLimits);
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
    
    /** test challenge verification behavior with success limit 
     * @throws Exception */
    @Test
    public void testChallengeVerificatorSuccessLimit() {
        boolean exceptionOccurred = false;
        testStepUpAccount.setAccountRestrictor(ar);
        testStepUpAccount.setChallengeGenerator(challengeGen);
        testStepUpAccount.setChallengeVerifier(challengeVer);
        try {
            for (int i=0;i<4;i++){
                testStepUpAccount.sendChallenge();
                Assert.assertTrue(testStepUpAccount.verifyResponse("response"));
            }
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(!exceptionOccurred);
        //The 6th try will cause exception
        try {
            testStepUpAccount.sendChallenge();
        } catch (LimitReachedException e) {
            exceptionOccurred = true;
        } catch (Exception e) {
        }    
        Assert.assertTrue(exceptionOccurred);
    }
    
    /** test challenge verification behavior with failure limit 
     * @throws Exception */
    @Test
    public void testChallengeVerificatorFailureLimit() {
        boolean exceptionOccurred = false;
        testStepUpAccount.setAccountRestrictor(ar);
        testStepUpAccount.setChallengeGenerator(challengeGen);
        testStepUpAccount.setChallengeVerifier(challengeVer);
        try {
            testStepUpAccount.sendChallenge();
            Assert.assertTrue(!testStepUpAccount.verifyResponse("response_not"));
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(!exceptionOccurred);
        //The second fail will hit limit
        try {
            testStepUpAccount.verifyResponse("response_not");
        } catch (LimitReachedException e) {
            exceptionOccurred = true;
        } catch (Exception e) {
        }    
        Assert.assertTrue(exceptionOccurred);
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

package fi.csc.idp.stepup.impl;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestEqualChallengeResponseVerifier {

    private EqualChallengeResponseVerifier equalChallengeResponseVerifier;  
    
    @BeforeMethod
    public void setUp()  {
        equalChallengeResponseVerifier = new EqualChallengeResponseVerifier();
    }

    @Test
    public void testAllNull() throws Exception  {
        Assert.assertTrue(equalChallengeResponseVerifier.verify(null, null, null));
    }

    @Test
    public void testPartNull() throws Exception  {
        Assert.assertFalse(equalChallengeResponseVerifier.verify(null, "notnull", null));
        Assert.assertFalse(equalChallengeResponseVerifier.verify("notnull", null, null));
        
    }
   
    @Test
    public void testCase() throws Exception  {
        Assert.assertFalse(equalChallengeResponseVerifier.verify("Notnull", "notnull", null));
        Assert.assertTrue(equalChallengeResponseVerifier.verify("notnull", "notnull", null));
        
    }
    
    @Test
    public void testTrim() throws Exception  {
        Assert.assertTrue(equalChallengeResponseVerifier.verify("  notnull", "notnull  ", null));
        
    }
 
}

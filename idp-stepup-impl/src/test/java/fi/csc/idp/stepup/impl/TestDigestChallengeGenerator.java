package fi.csc.idp.stepup.impl;


import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestDigestChallengeGenerator {

    private DigestChallengeGenerator digestChallengeGenerator;  
    
    @BeforeMethod
    public void setUp()  {
        digestChallengeGenerator = new DigestChallengeGenerator();
    }

    @Test
    public void testUnitialized() throws Exception  {
        String digest=digestChallengeGenerator.generate(null);   
        Assert.assertNotNull(digest);
        Assert.assertEquals(digest.length(),8);
    }

    @Test
    public void testMaxLength() throws Exception  {
        digestChallengeGenerator.setMaxLength(100);
        String digest=digestChallengeGenerator.generate(null);   
        Assert.assertNotNull(digest);
        //SHA-256 output length
        Assert.assertEquals(digest.length(),64);
        digestChallengeGenerator.setMaxLength(8);
        digest=digestChallengeGenerator.generate(null);
        Assert.assertNotNull(digest);
        Assert.assertEquals(digest.length(),8);
    }
    
    @Test
    public void testDifferentDigest() throws Exception  {
        digestChallengeGenerator.setDigest("SHA-1");
        digestChallengeGenerator.setMaxLength(100);
        String digest=digestChallengeGenerator.generate(null); 
        Assert.assertNotNull(digest);
        //SHA-1 output length
        Assert.assertEquals(digest.length(),40);
    }
    
    @Test
    public void testDecimalOutput() throws Exception  {
        digestChallengeGenerator.setDecimal(true);
        digestChallengeGenerator.setMaxLength(100);
        String digest=digestChallengeGenerator.generate(null);
        Assert.assertTrue(StringUtils.isNumeric(digest));
    }
 
    @Test
    public void initAll() throws Exception  {
        digestChallengeGenerator.setSalt("notrandomsalt");
        digestChallengeGenerator.setMaxLength(100);
        digestChallengeGenerator.setDigest("SHA-1");
        String digest=digestChallengeGenerator.generate("target");   
        Assert.assertNotNull(digest);
        //SHA-1 output length
        Assert.assertEquals(digest.length(),40);
    }
 
}

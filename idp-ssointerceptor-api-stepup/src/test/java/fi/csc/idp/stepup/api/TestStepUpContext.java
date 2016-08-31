package fi.csc.idp.stepup.api;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestStepUpContext {
   
    /**  Test that context i/o works as expected */
    @Test public void testUninitiailizedContext() {
        StepUpContext ctx=new StepUpContext();
        Assert.assertNull(ctx.getChallenge());
        Assert.assertNull(ctx.getTarget());
        ctx.setChallenge("challenge");
        ctx.setTarget("target");
        ctx.setSharedSecret("sharedSecretValue");
        Assert.assertEquals("challenge", ctx.getChallenge());
        Assert.assertEquals("target", ctx.getTarget());
        Assert.assertEquals("sharedSecretValue", ctx.getSharedSecret());
        
    }
    

}

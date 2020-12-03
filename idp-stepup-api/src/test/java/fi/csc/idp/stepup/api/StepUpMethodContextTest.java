package fi.csc.idp.stepup.api;

import org.testng.Assert;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StepUpMethodContextTest {

    private StepUpMethodContext stepUpMethodContext;

    @BeforeMethod
    public void setUp() {
        stepUpMethodContext = new StepUpMethodContext();
    }

    @Test
    public void testAllNull() {
        Assert.assertNull(stepUpMethodContext.getStepUpAccount());
        Assert.assertNull(stepUpMethodContext.getStepUpMethod());
        Assert.assertNull(stepUpMethodContext.getSubject());
    }

    @Test
    public void testNotNull() {
        stepUpMethodContext.setStepUpAccount(Mockito.mock(StepUpAccount.class));
        stepUpMethodContext.setStepUpMethod(Mockito.mock(StepUpMethod.class));
        stepUpMethodContext.setSubject("sub");
        Assert.assertNotNull(stepUpMethodContext.getStepUpAccount());
        Assert.assertNotNull(stepUpMethodContext.getStepUpMethod());
        Assert.assertEquals(stepUpMethodContext.getSubject(), "sub");

    }

}

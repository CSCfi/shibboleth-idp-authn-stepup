package fi.csc.idp.stepup.impl;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LogChallengeSenderTest {

    private LogChallengeSender logChallengeSender;  
    
    @BeforeMethod
    public void setUp()  {
        logChallengeSender = new LogChallengeSender();
    }

    @Test
    public void testAllNull()  {
        //only really tests that there is no exception thrown
        logChallengeSender.send(null, null);
    }

    @Test
    public void testNotNull()  {
        //only really tests that there is no exception thrown
        logChallengeSender.send("notnull", "notnull");
    }
    
 
}

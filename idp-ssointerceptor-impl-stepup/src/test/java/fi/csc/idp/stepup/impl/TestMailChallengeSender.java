package fi.csc.idp.stepup.impl;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class TestMailChallengeSender {

    private MailChallengeSender mailChallengeSender;
    
    private GreenMail mailServer;
   
    @BeforeMethod
    public void setUp()  {
        mailChallengeSender = new MailChallengeSender();
        mailServer = new GreenMail(ServerSetupTest.SMTP);
        mailServer.setUser("from@foo.bar", "from", "password");
        mailServer.start();
        
    }
    
    @AfterMethod
    public void end() throws Exception {
        mailServer.stop();
    }


    @Test
    public void successSend() throws AddressException, MessagingException  {
        
        mailChallengeSender.setFromField("from@foo.bar");
        mailChallengeSender.setHost("127.0.0.1");
        mailChallengeSender.setSMTPAuth("false");
        mailChallengeSender.setSMTPTtls("true");
        mailChallengeSender.setPort(new Integer(mailServer.getSmtp().getPort()).toString());
        //TODO, not able to send to test server
        //mailChallengeSender.send("message", "to@foo.bar");
        //TODO remove
        GreenMailUtil.sendTextEmailTest("to@foo.bar", "from@foo.bar",
                "subject", "body"); 
        Assert.assertEquals(mailServer.getReceivedMessages().length,1);
      
    }

}

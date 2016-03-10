package fi.csc.idp.stepup.impl;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMailChallengeSender {

    private MailChallengeSender mailChallengeSender = new MailChallengeSender();
    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void success() throws Exception {
        //mailChallengeSender.setTemplateFile(this.getClass().getResource( "/emails/default.vm" ).getPath());
        /*
        mailChallengeSender.setSubjectField("test");
        mailChallengeSender.setFromField("rems-admin@csc.fi");
        mailChallengeSender.setHost("smtp.csc.fi");
        mailChallengeSender.setPort("25");
        mailChallengeSender.setSMTPAuth("false");
        mailChallengeSender.setSMTPTtls("true");
        */
        mailChallengeSender.setSubjectField("test");
        mailChallengeSender.setFromField("janne.lauros@gmail.com");
        mailChallengeSender.setHost("smtp.gmail.com");
        mailChallengeSender.setPort("587");
        mailChallengeSender.setSMTPAuth("true");
        mailChallengeSender.setSMTPTtls("true");
        mailChallengeSender.setUserName("janne.lauros@gmail.com");
        mailChallengeSender.setPassword("L1ndorff1");
        
        mailChallengeSender.send("challenge", "janne.lauros@gmail.com");
        Assert.assertTrue(true);
    }

}

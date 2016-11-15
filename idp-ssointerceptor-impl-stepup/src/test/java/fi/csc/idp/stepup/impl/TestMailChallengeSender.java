package fi.csc.idp.stepup.impl;

import java.io.IOException;

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
    public void setUp() {
        mailChallengeSender = new MailChallengeSender();
        mailServer = new GreenMail(ServerSetupTest.SMTP);
        mailServer.start();
    }

    @AfterMethod
    public void end() throws Exception {
        mailServer.stop();
    }

    @Test
    public void successSendMany() throws AddressException, MessagingException, IOException {

        mailChallengeSender.setFromField("from@foo.bar");
        mailChallengeSender.setHost("127.0.0.1");
        mailChallengeSender.setSMTPAuth("false");
        mailChallengeSender.setSMTPTtls("true");
        mailChallengeSender.setPort(new Integer(mailServer.getSmtp().getPort()).toString());
        mailChallengeSender.setSubjectField("subjectField");
        mailChallengeSender.setUserName("from");
        mailChallengeSender.setPassword("password");

        for (int i = 0; i < 100; i++) {
            mailChallengeSender.send("" + i, "to@foo.bar");
        }
        Assert.assertEquals(mailServer.getReceivedMessages().length, 100);
        Assert.assertEquals(GreenMailUtil.getBody(mailServer.getReceivedMessages()[0]).trim().replaceAll("\\s", ""),
                "Dearrecipient,yourfinalpasswordtoaccesstheserviceis0.Pleasedonotreplytothisautomaticallygeneratedmessage.");
        Assert.assertEquals(mailServer.getReceivedMessages()[0].getSubject(), "subjectField");

    }

}

/*
 * The MIT License
 * Copyright (c) 2015-2020 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.csc.idp.stepup.impl;

import java.io.IOException;


import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

public class MailChallengeSenderTest {

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
        mailChallengeSender.setPort(Integer.valueOf(mailServer.getSmtp().getPort()).toString());
        mailChallengeSender.setSubjectField("subjectField %s");
        mailChallengeSender.setUserName("from");
        mailChallengeSender.setPassword("password");

        
        for (int i = 0; i < 100; i++) {
            mailChallengeSender.send("" + i, "to@foo.bar");
        }
        
        Assert.assertEquals(mailServer.getReceivedMessages().length, 100);
        Assert.assertEquals(GreenMailUtil.getBody(mailServer.getReceivedMessages()[0]).trim().replaceAll("\\s", ""),
                "Dearrecipient,yourfinalpasswordtoaccesstheserviceis0.Pleasedonotreplytothisautomaticallygeneratedmessage.");
        Assert.assertEquals(mailServer.getReceivedMessages()[0].getSubject(), "subjectField 0");
        

    }
}

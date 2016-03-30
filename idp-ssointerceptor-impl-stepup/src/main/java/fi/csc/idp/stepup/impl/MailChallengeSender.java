/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
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

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import fi.csc.idp.stepup.api.ChallengeSender;

/** Class implemented for sending a challenge to mail account of the user. */
public class MailChallengeSender implements ChallengeSender {

    /** Class logger. */
    @Nonnull
    private final Logger log  = LoggerFactory
            .getLogger(MailChallengeSender.class);

    /** Sender address of email. */
    private String from;
    /** Subject of email. */
    private String subject;
    /** username of the email account. */
    private String userName;
    /** password of the email account. */
    private String password;
    /** SMTP authentication property. */
    private String smtpAuth;
    /** Start TLS property. */
    private String smtpTtls;
    /** mail host. */
    private String host;
    /** port of the mail host. */
    private String port;
    /** email template used to override the default.*/
    private String templateFileName;
    
    /** email  address. */
    private String to;
    /** body of the email. */
    private String body;
    
    /**
     * Sets the Sender field of email.
     * @param fromField the value of the sender
     */
    public void setFromField(String fromField) {
        log.trace("Entering");
        log.debug("setting from to "+fromField);
        this.from = fromField;
        log.trace("Leaving");
    }

    /**
     * Sets the Subject field of email.
     * @param subjectField the value of the sender
     */
    public void setSubjectField(String subjectField) {
        log.trace("Entering");
        log.debug("setting subject to "+subjectField);
        this.subject = subjectField;
        log.trace("Leaving");
    }
    
    /**
     * Sets the username of the email account.
     * @param accountUserName is the username
     */
    public void setUserName(String accountUserName) {
        log.trace("Entering");
        log.debug("setting account username to "+accountUserName);
        this.userName = accountUserName;
        log.trace("Leaving");
    }

    /**
     * Sets the password of the email account.
     * @param accountPassword is the Password
     */
    public void setPassword(String accountPassword) {
        log.trace("Entering");
        log.debug("setting account password to "+accountPassword);
        this.password = accountPassword;
        log.trace("Leaving");
    }
    
    /**
     * Sets the SMTP Auth property of the email server.
     * @param serverSMTPAuth SMTP Auth property
     */
    public void setSMTPAuth(String serverSMTPAuth) {
        log.trace("Entering");
        log.debug("setting SMTP Auth to "+serverSMTPAuth);
        this.smtpAuth=serverSMTPAuth;
        log.trace("Leaving");
    }
    
    /**
     * Sets the SMTP TTLS Start property of the email server.
     * @param serverSMTPTtls SMTP TTLS Start property
     */
    public void setSMTPTtls(String serverSMTPTtls) {
        log.trace("Entering");
        log.debug("setting SMTP Start TLS to "+serverSMTPTtls);
        this.smtpTtls=serverSMTPTtls;
        log.trace("Leaving");
    }
    
    /**
     * Sets the email server host.
     * @param hostName email server hostname
     */
    public void setHost(String hostName) {
        log.trace("Entering");
        log.debug("setting host to "+hostName);
        this.host=hostName;
        log.trace("Leaving");
    }
    
    /**
     * Sets the port of email server host.
     * @param serverPort port of email server
     */
    public void setPort(String serverPort) {
        log.trace("Entering");
        log.debug("setting port to "+serverPort);
        this.port=serverPort;
        log.trace("Leaving");
    }
    
    /**
     * Sets a path to a file replacing the default template.
     * @param fileName template file replacing the default one
     */
    public void setTemplateFile(String fileName) {
        log.trace("Entering");
        log.debug("setting template filename to "+fileName);
        this.templateFileName = fileName;
        log.trace("Leaving");
    }

    /**
     * Method loads the template for challenge email.
     * 
     * @return template for challenge email
     */
    private Template getVelocityTemplate() {
        log.trace("Entering");
        VelocityEngine velocityEngine = new VelocityEngine();
        if (templateFileName != null && !templateFileName.isEmpty()) {
            velocityEngine
                    .setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
            velocityEngine.init();
            log.trace("Leaving");
            return velocityEngine.getTemplate(templateFileName);
        }
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
                "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName()); 
        velocityEngine.init();
        log.trace("Leaving");
        return velocityEngine.getTemplate(File.separator + "emails"
                + File.separator + "default.vm");

    }
    
    
    //TODO: Make use of parameters and methods sane.. this is now
    //a mess. What values are static and same for all, what is per
    //execution..
    
    @Override
    public void send(String challenge, String target) throws AddressException, MessagingException {
        log.trace("Entering");
        log.debug("Sending challenge "+challenge+" to "+target);
        to=target;
        Template template = getVelocityTemplate();
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("otp", challenge);
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        body=writer.toString();
        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", smtpTtls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        Session session = null;
        if (userName != null && password != null){
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    userName, password);
                        }
                    });
        }else{
            session = Session.getInstance(props);
        }
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
        log.debug("Challenge sending triggered");
        log.trace("Leaving");
    }
   

}

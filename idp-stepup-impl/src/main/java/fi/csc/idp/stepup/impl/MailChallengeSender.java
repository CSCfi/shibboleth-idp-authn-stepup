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
import java.io.PrintWriter;
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
    private final Logger log = LoggerFactory.getLogger(MailChallengeSender.class);

    /** Sender address of email. */
    private String from;
    /** Subject of email. */
    private String subject;
    /** username of the email account. */
    private String userName;
    /** password of the email account. */
    private String password;
    /** email template used to override the default. */
    private String templateFileName;
    /** email template path used to override the default. */
    private String templateFilePath;
    /** Velocity template. */
    private Template template;
    /** Properties for email. */
    private Properties props = new Properties();
    /** Session information. */
    private Session session;

    /**
     * Sets the Sender field of email.
     * 
     * @param fromField
     *            the value of the sender
     */
    public void setFromField(String fromField) {

        this.from = fromField;

    }

    /**
     * Sets the Subject field of email.
     * 
     * @param subjectField
     *            the value of the sender
     */
    public void setSubjectField(String subjectField) {

        this.subject = subjectField;

    }

    /**
     * Sets the username of the email account.
     * 
     * @param accountUserName
     *            is the username
     */
    public void setUserName(String accountUserName) {

        this.userName = accountUserName;

    }

    /**
     * Sets the password of the email account.
     * 
     * @param accountPassword
     *            is the Password
     */
    public void setPassword(String accountPassword) {

        this.password = accountPassword;

    }

    /**
     * Sets the SMTP Auth property of the email server.
     * 
     * @param serverSMTPAuth
     *            SMTP Auth property
     */
    public void setSMTPAuth(String serverSMTPAuth) {

        props.put("mail.smtp.auth", serverSMTPAuth);

    }

    /**
     * Sets the SMTP TTLS Start property of the email server.
     * 
     * @param serverSMTPTtls
     *            SMTP TTLS Start property
     */
    public void setSMTPTtls(String serverSMTPTtls) {

        props.put("mail.smtp.starttls.enable", serverSMTPTtls);

    }

    /**
     * Sets the email server host.
     * 
     * @param hostName
     *            email server hostname
     */
    public void setHost(String hostName) {

        props.put("mail.smtp.host", hostName);

    }

    /**
     * Sets the port of email server host.
     * 
     * @param serverPort
     *            port of email server
     */
    public void setPort(String serverPort) {

        props.put("mail.smtp.port", serverPort);

    }

    /**
     * Sets a template file name replacing the default template.
     * 
     * @param fileName
     *            template file replacing the default one
     */
    public void setTemplateFile(String fileName) {

        this.templateFileName = fileName;

    }

    /**
     * Sets a loader path to a template file replacing the default template.
     * 
     * @param path
     *            loader path to template file replacing the default one
     */
    public void setTemplatePath(String path) {

        this.templateFilePath = path;

    }

    /**
     * Method loads the template for challenge email.
     * 
     * @return template for challenge email
     */
    private Template getVelocityTemplate() {

        VelocityEngine velocityEngine = new VelocityEngine();
        if (templateFileName != null && !templateFileName.isEmpty() && templateFilePath != null
                && !templateFilePath.isEmpty()) {
            velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
            velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templateFilePath);
            velocityEngine.init();

            return velocityEngine.getTemplate(templateFileName);
        }
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        return velocityEngine.getTemplate(File.separator + "emails" + File.separator + "default.vm");

    }

    /**
     * Initializes velocity template and mail session if not initialized yet.
     */
    private synchronized void init() {

        if (template == null) {
            template = getVelocityTemplate();
        }
        if (session == null) {
            if (userName != null && password != null) {
                session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, password);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }
        }

    }

    @Override
    public void send(String challenge, String target) throws AddressException, MessagingException {

        log.debug("Sending challenge {} to {}", challenge, target);
        init();
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("otp", challenge);
        String subjectToSend = String.format(subject, challenge);
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(target));
            message.setSubject(subjectToSend);
            message.setText(writer.toString());
            Transport.send(message);
        } catch (MessagingException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
            throw e;
        }
        log.debug("Challenge sending triggered");

    }

}

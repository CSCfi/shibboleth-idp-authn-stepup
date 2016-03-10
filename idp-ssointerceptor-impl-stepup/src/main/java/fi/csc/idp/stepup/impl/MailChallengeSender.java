package fi.csc.idp.stepup.impl;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
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

/** Class implemented for sending a challenge to mail account of the use. */
public class MailChallengeSender implements ChallengeSender {

    /** Class logger. */
    @Nonnull
    static final Logger log = LoggerFactory
            .getLogger(LogChallengeSender.class);

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
    private String templateFileName = null;
    
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
    
    @Override
    public void send(String challenge, String target) {
        log.trace("Entering");
        log.debug("Sending challenge "+challenge+" to "+target);
        Template template = getVelocityTemplate();
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("otp", challenge);
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        body=writer.toString();
        Thread t = new Thread(new SendMessage());
        t.start();
        log.debug("Challenge sending triggered");
        log.trace("Leaving");
    }

    private  class SendMessage implements Runnable {
       
        public void run() {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", smtpAuth);
                props.put("mail.smtp.starttls.enable", smtpTtls);
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", port);
                Session session = null;
                if (userName != null && password != null)
                    session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(
                                            userName, password);
                                }
                            });
                else
                    session = Session.getInstance(props);
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
            } catch (SendFailedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}

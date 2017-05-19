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

import javax.annotation.Nonnull;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.Message.Status;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.ChallengeSender;

/** Class implemented for sending a challenge to sms account of the user. */
public class TvilioSMSChallengeSender implements ChallengeSender {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(TvilioSMSChallengeSender.class);

    /** SID of the tvilio account. */
    private String accountSid;
    /** authentication token of the tvilio account. */
    private String authToken;
    /** message sent to the user, including the challenge. */
    private String message;
    /** the FROM number used, must be configured to tvilio. */
    private String senderNumber;

    /**
     * Set the FROM number used.
     * 
     * @param number
     *            FROM number.
     */
    public void setSenderNumber(String number) {
        this.senderNumber = number;
    }

    /**
     * Set the message. Assumed to contain %s for placing the challenge.
     * 
     * @param msg
     *            Message sent to the client.
     */
    public void setMessage(String msg) {
        this.message = msg;
    }

    /**
     * Tvilio account SID.
     * 
     * @param sid
     *            of the account
     */
    public void setAccountSid(String sid) {

        this.accountSid = sid;
    }

    /**
     * Tvilio account authentication token.
     * 
     * @param token
     *            authentication token.
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Send challenge to SMS receiver.
     * 
     * @param challenge
     *            sent to the receiver
     * @target receiver SMS number
     * @throw Exception if something unexpected occurred.
     */
    @Override
    public void send(String challenge, String target) throws Exception {

        if (accountSid == null || authToken == null || accountSid == null || message == null) {
            log.error("tvilio parameters not set");

            throw new Exception("bean not properly initialized");
        }
        log.debug("Sending challenge {} to ", challenge, target);
        Twilio.init(accountSid, authToken);
        Message msg = Message.creator(new PhoneNumber(target), new PhoneNumber(senderNumber),
                String.format(message, challenge)).create();
        log.debug("Message status {}", msg.getStatus());
        if (msg.getStatus() == Status.FAILED || msg.getStatus() == Status.UNDELIVERED) {
            throw new Exception("Message sending failed");
        }
        log.debug("Challenge sending triggered");

    }

}

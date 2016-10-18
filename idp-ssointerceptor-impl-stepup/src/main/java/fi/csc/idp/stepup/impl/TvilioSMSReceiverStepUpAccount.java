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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.Message.Direction;
import com.twilio.type.PhoneNumber;

/**
 * Step Up Account implementation expecting a SMS response from target to be
 * found in tvilio service.
 */
public class TvilioSMSReceiverStepUpAccount extends ChallengeSenderStepUpAccount {

    /** Crude implementation just to test the approach */
    
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(TvilioSMSReceiverStepUpAccount.class);
    
    /** SID of the tvilio account.*/
    private String accountSid;
    /** authentication token of the tvilio account.*/
    private String authToken;
        
    /**
     * Tvilio account SID.
     * 
     * @param sid of the account
     */
    public void setAccountSid(String sid) {
        log.trace("Entering & Leaving");
        this.accountSid = sid;
    }

    /**
     * Tvilio account authentication token.
     * 
     * @param token authentication token.
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Verify targets response can be found from tvilio. Response can be used
     * only once successfully.
     *
     *
     * @param response
     *            is ignored.
     * @return true if response was verified successfully
     * @throws Exception
     *             if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        log.trace("Entering");
        log.debug("Verificating totp response " + response);
        Twilio.init(accountSid, authToken);
        
        //wait loop, just for testing this
        int rounds = Integer.parseInt(response);
        for (int i = 0; i < rounds; i++) {
            ResourceSet<Message> messages = Message.reader().read();
            for (Message message : messages) {
                log.debug("message sid "+message.getSid());
                // has to be received by us
                if (!(message.getDirection() == Direction.INBOUND)) {
                    continue;
                }
                // has to match target
                log.debug("Sent from " + message.getFrom());
                log.debug("Comparing to "+getTarget());
                if (!message.getFrom().equals(new PhoneNumber(getTarget()))) {
                    continue;
                }
                log.trace("Leaving");
                return true;
                // has to be sent within current time-window
                //message.getDateSent();
                // sid is stored to list of used sids. List is cleaned every
                // 10th call
                // list handling must be protected
                //message.getSid();
                // once a match, mark
            }
            Thread.sleep(1000);
        }
        log.trace("Leaving");
        return false;
    }

}

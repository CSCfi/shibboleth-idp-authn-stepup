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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.Message.Direction;
import com.twilio.type.PhoneNumber;

/**
 * Step Up Account implementation expecting a SMS response from target to be found in tvilio service.
 */
public class TvilioSMSReceiverStepUpAccount extends ChallengeSenderStepUpAccount {

    /** contains messages already used for verification. */
    private static Map<String, DateTime> usedMessages = new HashMap<String, DateTime>();

    /** lock to access usedMessages. */
    private static Lock msgLock = new ReentrantLock();

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(TvilioSMSReceiverStepUpAccount.class);

    /** SID of the tvilio account. */
    private String accountSid;

    /** authentication token of the tvilio account. */
    private String authToken;

    /**
     * time window in ms for authentication event to be acceptable. Value is used for filtering sms's and for checking
     * the timestamp of a found message.
     */
    private long eventWindow = 30000;

    /** how many times sms are checked for successful match. */
    private int numberOfChecks = 10;

    /** interval in ms between sms checks. */
    private int intervalOfChecks = 1000;

    /** Maximum number of digits to match (from last one) in phone numbers. 0 means all must match. */
    private int cmpMax = 0;

    /**
     * Sets maximum number of digits to match (from last one) in phone numbers. 0 or under means all must match.
     * 
     * @param cmpMax maximum number of digits to match (from last one) in phone numbers. 0 or under means all must
     *            match.
     */
    public void setCmpMax(int cmpMax) {
        this.cmpMax = cmpMax;
    }

    /**
     * Tvilio account SID.
     * 
     * @param sid of the account
     */
    public void setAccountSid(String sid) {

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
     * Set the event window in ms for authentication response to be acceptable.
     * 
     * @param window in ms
     */
    public void setEventWindow(long window) {
        this.eventWindow = window;
    }

    /**
     * Set the number of times sms reply is searched for.
     * 
     * @param number number of times sms is searched for
     */
    public void setNumberOfChecks(int number) {
        this.numberOfChecks = number;
    }

    /**
     * Set the interval between sms searches in milliseconds.
     * 
     * @param interval interval is ms
     */
    public void setIntervalOfChecks(int interval) {
        this.intervalOfChecks = interval;
    }

    /**
     * Cleans old messages.
     */
    private void cleanMessages() {

        msgLock.lock();
        if (usedMessages.size() < 100) {
            msgLock.unlock();

            return;
        }
        long current = new Date().getTime();
        for (Iterator<Map.Entry<String, DateTime>> it = usedMessages.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, DateTime> usedMessage = it.next();
            long sent = usedMessage.getValue().toDate().getTime();
            if (current - sent > 2 * eventWindow) {
                log.debug("Removing {} {} from the list of used verification messages", usedMessage.getKey(),
                        usedMessage.getValue());
                it.remove();
            }
        }
        msgLock.unlock();

    }

    /**
     * Compare two phone numbers.
     * 
     * @param number1 first phone number to compare.
     * @param number2 second phone number to compare.
     * @return true if both of the numbers are not null and match. cmpMax parameter may restrict the comparison to last
     *         n digits.
     */
    private boolean cmpNumbers(PhoneNumber number1, PhoneNumber number2) {
        if (number1 == null || number2 == null) {
            return false;
        }
        if (cmpMax < 1) {
            return number1.equals(number2);
        }
        String first = number1.toString();
        String second = number2.toString();
        if (cmpMax < first.length()) {
            first = first.substring(first.length() - cmpMax, first.length() - 1);
        }
        if (cmpMax < second.length()) {
            second = second.substring(second.length() - cmpMax, second.length() - 1);
        }
        log.debug("comparing {} to {}", first, second);
        return first.equals(second);

    }

    /**
     * Verify targets response can be found from tvilio. Response can be used only once successfully.
     *
     *
     * @param response is ignored.
     * @return true if response was verified successfully
     * @throws Exception if something unexpected occurred
     */
    @Override
    public boolean doVerifyResponse(String response) throws Exception {

        log.debug("Verificating totp response {}", response);
        Twilio.init(accountSid, authToken);

        // We fetch all messages of past 24h
        DateTime rangeDateSentStart = new DateTime().minusDays(1);
        log.debug("Searching for messages sent since {}", rangeDateSentStart.toString());
        for (int i = 0; i < numberOfChecks; i++) {
            log.debug("Locating messages");
            ResourceSet<Message> messages =
                    Message.reader().setDateSent(rangeDateSentStart).read();
            for (Message message : messages) {
                log.debug("Message sid {}", message.getSid());
                // has to be received by us, doublecheck
                if (!(message.getDirection() == Direction.INBOUND)) {
                    log.debug("Message discarded, not inbound");
                    continue;
                }
                // has to match target, doublecheck
                log.debug("Located message from {}", message.getFrom());
                log.debug("Comparing to {}", getTarget());
                if (!cmpNumbers(message.getFrom(), new PhoneNumber(getTarget()))) {
                    log.debug("Message discarded, not sent by user");
                    continue;
                }
                long sent = message.getDateSent().toDate().getTime();
                long current = new Date().getTime();
                log.debug("Message sent {}", message.getDateSent().toDate());
                // message has to have been sent no more that eventWindow time
                // before check
                if (current - sent > eventWindow) {
                    log.debug("Message discarded, too old message");
                    continue;
                }
                if (getChallenge().length() > 0 && (!getChallenge().equals(message.getBody()))) {
                    log.debug("Message discarded, challenge not replied");
                    continue;
                }

                msgLock.lock();
                if (usedMessages.containsKey(message.getSid())) {
                    msgLock.unlock();
                    log.debug("Message discarded, already used");
                    continue;
                }
                cleanMessages();
                log.debug("Adding {} {} to the list of used verification messages", message.getSid(),
                        message.getDateSent());
                usedMessages.put(message.getSid(), message.getDateSent());
                msgLock.unlock();
                return true;
            }
            if (numberOfChecks > 0) {
                Thread.sleep(intervalOfChecks);
            }

        }
        return false;
    }

}

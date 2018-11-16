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
package fi.csc.idp.stepup.event.impl;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.event.api.AccountRestrictorAction;
import fi.csc.idp.stepup.event.api.AccountRestrictorConfiguration;
import fi.csc.idp.stepup.event.api.EventStore;
import fi.csc.idp.stepup.event.api.EventUnmodiafiableException;

/** Account restrictor. Expects to have event store, type and key always set. */
public class AccountRestrictor implements AccountRestrictorAction, AccountRestrictorConfiguration {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AccountRestrictor.class);

    /** Event Store for event actions. */
    private EventStore eventStore;

    /**
     * Allowed events within timeframe, maps millis to number of allowed events.
     */
    private Map<Long, Integer> accountEventLimits;

    /**
     * Allowed fail events within timeframe, maps millis to number of allowed
     * fail events.
     */
    private Map<Long, Integer> accountFailEventLimits;

    /** Account type for the events. */
    private String type;

    /** Account key for the events. */
    private String key;

    /**
     * Set then Event Store for storing the events.
     * 
     * @param store
     *            Event Store.
     */
    public void setEventStore(EventStore store) {
        this.eventStore = store;
    }

    /**
     * Set allowed events within timeframe, maps millis to number of allowed
     * events.
     * 
     * @param limits
     *            event limits
     */
    public void setAccountEventLimits(Map<Long, Integer> limits) {
        this.accountEventLimits = limits;
    }

    /**
     * Set allowed fail events within timeframe. Maps millis to number of
     * allowed fail events.
     * 
     * @param limits
     *            fail event limits
     */
    public void setAccountFailEventLimits(Map<Long, Integer> limits) {
        this.accountFailEventLimits = limits;
    }

    @Override
    public void setType(String accountType) {
        type = accountType;
    }

    @Override
    public void setKey(String accountKey) {
        key = accountKey;
    }

    @Override
    public void addAttempt() {
        try {
            eventStore.add(new AccountEvent(type, key));
        } catch (EventUnmodiafiableException e) {
            log.error("Unexpected error, {}", e.getMessage());
        }
    }

    @Override
    public void addFailure() {
        try {
            eventStore.add(new AccountFailureEvent(type, key));
        } catch (EventUnmodiafiableException e) {
            log.error("Unexpected error, {}", e.getMessage());
        }
    }

    // Checkstyle: CyclomaticComplexity OFF
    @Override
    public long limitReached() {
        if (accountEventLimits != null) {
            for (Long millis : accountEventLimits.keySet()) {
                List<AccountEvent> events = eventStore.getEvents(AccountEvent.class, millis);
                for (ListIterator<AccountEvent> iter = events.listIterator(); iter.hasNext();) {
                    if (!type.equals(iter.next().getType())) {
                        iter.remove();
                    }
                }
                if (events.size() >= accountEventLimits.get(millis)) {
                    long wait = millis - (System.currentTimeMillis() - events.get(events.size() - 1).getTimestamp());
                    return wait > 0 ? wait : 0;
                }
            }
        }
        if (accountFailEventLimits != null) {
            for (Long millis : accountFailEventLimits.keySet()) {
                List<AccountFailureEvent> events = eventStore.getEvents(AccountFailureEvent.class, millis);
                for (ListIterator<AccountFailureEvent> iter = events.listIterator(); iter.hasNext();) {
                    if (!type.equals(iter.next().getType())) {
                        iter.remove();
                    }
                }
                if (events.size() >= accountFailEventLimits.get(millis)) {
                    long wait = millis - (System.currentTimeMillis() - events.get(events.size() - 1).getTimestamp());
                    return wait > 0 ? wait : 0;
                }
            }
        }
        return 0;
    }

    // Checkstyle: CyclomaticComplexity ON

}

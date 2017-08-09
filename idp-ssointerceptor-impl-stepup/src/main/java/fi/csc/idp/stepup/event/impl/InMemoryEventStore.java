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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.event.api.EventItem;
import fi.csc.idp.stepup.event.api.EventStore;
import fi.csc.idp.stepup.event.api.EventUnmodiafiableException;

/** In memory event store storing events .*/
public class InMemoryEventStore implements EventStore {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InMemoryEventStore.class);
    
    /** simple index counter. */
    private int index;
    /** In memory store. */
    private final LinkedList<EventItem> store = new LinkedList<EventItem>();
    /** lock to access store. */
    private Lock storeLock = new ReentrantLock();
    /** maximum items for the memory store. */
    private int storeSize = 10000;

    /**
     * Maximum number of events in store.
     * 
     * @param items maximum number of items in store.
     */
    public void setStoreSize(int items) {
        storeSize = items;
    }

    @Override
    public synchronized void add(@Nonnull EventItem object) throws EventUnmodiafiableException {
        if (object == null) {
            log.warn("null event passed as argument");
            return;
        }
        long millis = System.currentTimeMillis();
        object.setId(++index < 0 ? (index = 1) : index);
        object.setTimestamp(millis);
        storeLock.lock();
        if (store.size() >= storeSize) {
            store.removeLast();
        }
        store.addFirst(object);
        storeLock.unlock();
        log.debug("Added event with id {} and timestamp {}", object.getId(), object.getTimestamp());
    }

    @Nonnull
    @Override
    public List<EventItem> getEvents() {
        return Arrays.asList(store.toArray(new EventItem[0]));
    }

    @Nonnull
    @Override
    public List<EventItem> getEvents(long noOlderThan) {
        log.debug("searching events not older than {}ms",noOlderThan);
        long treshold = System.currentTimeMillis() - noOlderThan;
        List<EventItem> events = new ArrayList<EventItem>();
        storeLock.lock();
        for (EventItem object : store) {
            if (object.getTimestamp() < treshold){
                break;
            }
            events.add(object);
        }
        storeLock.unlock();
        log.debug("found {} events",events.size());
        return events;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T extends EventItem> List<T> getEvents(Class<T> clazz) {
        log.debug("searching events of type {}",clazz.getName());
        List<T> events = new ArrayList<T>();
        storeLock.lock();
        for (EventItem object : store) {
            if (object.getClass().equals(clazz)) {
                events.add((T) object);
            }
        }
        storeLock.unlock();
        log.debug("found {} events",events.size());
        return events;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T extends EventItem> List<T> getEvents(Class<T> clazz, long noOlderThan) {
        log.debug("searching events of type {} not older than {}ms",clazz.getName(), noOlderThan);
        long treshold = System.currentTimeMillis() - noOlderThan;
        List<T> events = new ArrayList<T>();
        storeLock.lock();
        for (EventItem object : store) {
            if (object.getTimestamp() < treshold){
                break;
            }
            if (object.getClass().equals(clazz)) {
                events.add((T) object);
            }
            
        }
        storeLock.unlock();
        log.debug("found {} events",events.size());
        return events;
    }

}

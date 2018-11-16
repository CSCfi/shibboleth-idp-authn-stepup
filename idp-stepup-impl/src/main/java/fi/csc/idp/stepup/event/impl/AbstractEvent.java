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

import fi.csc.idp.stepup.event.api.EventItem;
import fi.csc.idp.stepup.event.api.EventUnmodiafiableException;

/** Base class for event implementations. */
public abstract class AbstractEvent implements EventItem {

    /** Name of the event. */
    private String name;
    /** Description of the event. */
    private String description;
    /** Id of the event. */
    private int id;
    /** flag to prevent changing the id once set. */
    private boolean idSet;
    /** Timestamp of the event. */
    private long timestamp;
    /** flag to prevent changing the timestamp once set. */
    private boolean timestampSet;

    /**
     * Default constructor.
     */
    public AbstractEvent() {
    }

    /**
     * Constructor setting the name of the event.
     * 
     * @param eventName
     *            of the event.
     */
    public AbstractEvent(String eventName) {
        name = eventName;
    }

    /**
     * Method returns the name of the event.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the event.
     * 
     * @param eventName
     *            of the event.
     */
    public void setName(String eventName) {
        name = eventName;
    }

    /**
     * Method returns the description of the event.
     * 
     * @return description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the event.
     * 
     * @param eventDescription
     *            of the event.
     */
    public void setDescription(String eventDescription) {
        description = eventDescription;
    }

    @Override
    public void setId(int eventId) throws EventUnmodiafiableException {
        if (idSet) {
            throw new EventUnmodiafiableException("id has already been set");
        }
        idSet = true;
        id = eventId;
    }

    @Override
    public int getId() {
        if (idSet) {
            return id;
        }
        return 0;
    }

    @Override
    public void setTimestamp(long millis) throws EventUnmodiafiableException {
        if (timestampSet) {
            throw new EventUnmodiafiableException("timestamp has already been set");
        }
        timestampSet = true;
        timestamp = millis;
    }

    @Override
    public long getTimestamp() {
        if (timestampSet) {
            return timestamp;
        }
        return 0;
    }

}

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
package fi.csc.idp.stepup.event.api;

/** Interface implemented by EventStore Items. */
public interface EventItem {

    /**
     * Method to set the id of the event.
     * 
     * @param id
     *            of the event.
     * @throws EventUnmodiafiableException
     *             if id has already been set.
     */
    public void setId(int id) throws EventUnmodiafiableException;

    /**
     * Returns the id of the event. Valid id is a positive integer. 0 equals to
     * uninitialized event.
     * 
     * @return id of the event.
     */
    public int getId();

    /**
     * Method to set the creation instant of the event.
     * 
     * @param millis
     *            unix epoch in milliseconds.
     * @throws EventUnmodiafiableException
     *             if id has already been set.
     */
    public void setTimestamp(long millis) throws EventUnmodiafiableException;

    /**
     * Returns the creation instant of the event. 0 equals to uninitialized
     * event.
     * 
     * @return unix epoch in milliseconds.
     */
    public long getTimestamp();

}

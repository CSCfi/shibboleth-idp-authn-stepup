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

import java.util.List;

import javax.annotation.Nonnull;

/** Interface implemented by Event Stores. */
public interface EventStore {

    /**
     * Add event to event store.
     * 
     * @param object
     *            representing event.
     * @throws EventUnmodiafiableException
     *             if event has been initialized already.
     */
    public void add(@Nonnull EventItem object) throws EventUnmodiafiableException;

    /**
     * Return all events.
     * 
     * @return events.
     */
    @Nonnull
    public List<EventItem> getEvents();

    /**
     * Return all events that are younger than given parameter in ms.
     * 
     * @param noOlderThan
     *            event age in ms.
     * @return events matching criteria.
     */
    @Nonnull
    public List<EventItem> getEvents(long noOlderThan);

    /**
     * Return all events of given type.
     * 
     * @param <T>
     *            class implementing EventItem
     * @param clazz
     *            event type.
     * @return events.
     */
    @Nonnull
    public <T extends EventItem> List<T> getEvents(@Nonnull Class<T> clazz);

    /**
     * Return all events o given type that are younger than given parameter in
     * ms.
     * 
     * @param <T>
     *            class implementing EventItem
     * @param clazz
     *            event type.
     * @param noOlderThan
     *            event age in ms.
     * @return events matching criteria.
     */
    public <T extends EventItem> List<T> getEvents(@Nonnull Class<T> clazz, long noOlderThan);

}

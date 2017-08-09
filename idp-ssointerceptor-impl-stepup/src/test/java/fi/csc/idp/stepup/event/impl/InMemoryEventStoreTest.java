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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.event.api.EventItem;

public class InMemoryEventStoreTest {

    InMemoryEventStore store;

    @BeforeMethod
    public void setup() {
        store = new InMemoryEventStore();
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(store.getEvents().size(), 0);
        Assert.assertEquals(store.getEvents(50).size(), 0);
        Assert.assertEquals(store.getEvents(MockEvent.class).size(), 0);
        Assert.assertEquals(store.getEvents(MockEvent.class, 50).size(), 0);
    }

    @Test
    public void testItemLimit() throws Exception {
        store.setStoreSize(100);
        for (int i = 0; i < 200; i++) {
            store.add(new MockEvent("" + i));
        }
        List<EventItem> list = store.getEvents();
        Assert.assertEquals(list.size(), 100);
    }

    @Test
    public void testTimeLimit() throws Exception {
        for (int i = 0; i < 200; i++) {
            store.add(new MockEvent("" + i));
        }
        Thread.sleep(101);
        for (int i = 0; i < 50; i++) {
            store.add(new MockEvent("" + i));
        }
        List<EventItem> list = store.getEvents(100);
        Assert.assertEquals(list.size(), 50);
    }

    @Test
    public void testTypeGetter() throws Exception {
        for (int i = 0; i < 10; i++) {
            store.add(new MockEvent(""));
            store.add(new MockEvent2(""));
            store.add(new MockEvent(""));
        }
        Assert.assertEquals(store.getEvents().size(), 30);
        Assert.assertEquals(store.getEvents(MockEvent.class).size(), 20);
        Assert.assertEquals(store.getEvents(MockEvent2.class).size(), 10);
    }

    @Test
    public void testTimeLimitedTypeGetter() throws Exception {
        for (int i = 0; i < 50; i++) {
            store.add(new MockEvent(""));
            store.add(new MockEvent2(""));
            store.add(new MockEvent(""));
        }
        Thread.sleep(101);
        for (int i = 0; i < 10; i++) {
            store.add(new MockEvent(""));
            store.add(new MockEvent2(""));
            store.add(new MockEvent(""));
        }
        Assert.assertEquals(store.getEvents().size(), 180);
        Assert.assertEquals(store.getEvents(MockEvent.class,100).size(), 20);
        Assert.assertEquals(store.getEvents(MockEvent2.class,100).size(), 10);
    }

    class MockEvent extends AbstractEvent {

        MockEvent(String name) {
            super(name);
        }
    }

    class MockEvent2 extends AbstractEvent {

        MockEvent2(String name) {
            super(name);
        }
    }
}
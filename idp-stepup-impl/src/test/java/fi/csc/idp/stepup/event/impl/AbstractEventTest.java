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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.event.api.EventUnmodiafiableException;

public class AbstractEventTest {

    MockEvent event;

    @BeforeTest
    public void setup() {
        event = new MockEvent("name");
    }

    @Test
    public void testInitialState() {
        Assert.assertTrue(event.getName().equals("name"));
        Assert.assertNull(event.getDescription());
        Assert.assertEquals(event.getId(), 0);
        Assert.assertEquals(event.getTimestamp(), 0);
    }

    @Test
    public void testSetters() {
        event.setName("newname");
        Assert.assertTrue(event.getName().equals("newname"));
        event.setDescription("description");
        Assert.assertTrue(event.getDescription().equals("description"));
        try {
            event.setId(1);
            event.setTimestamp(2);
        } catch (EventUnmodiafiableException e) {
        }
        Assert.assertEquals(event.getId(), 1);
        Assert.assertEquals(event.getTimestamp(), 2);
        boolean exceptionId = false;
        try {
            event.setId(1);
        } catch (EventUnmodiafiableException e) {
            exceptionId = true;
        }
        boolean exceptionTS = false;
        try {
            event.setTimestamp(2);
        } catch (EventUnmodiafiableException e) {
            exceptionTS = true;
        }
        Assert.assertTrue(exceptionId);
        Assert.assertTrue(exceptionTS);

    }

    class MockEvent extends AbstractEvent {

        MockEvent(String name) {
            super(name);
        }
    }
}
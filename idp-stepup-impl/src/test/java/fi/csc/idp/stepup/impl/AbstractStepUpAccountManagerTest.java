/*
 * The MIT License
 * Copyright (c) 2020 CSC - IT Center for Science, http://www.csc.fi
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractStepUpAccountManagerTest {

    private TestStepUpAccountManager testStepUpAccountManager;

    @BeforeMethod
    public void setUp() {
        testStepUpAccountManager = new TestStepUpAccountManager();
    }

    @Test
    public void testUnitialized() throws Exception {
        Assert.assertNull(testStepUpAccountManager.getAccountID());
        Assert.assertNull(testStepUpAccountManager.getAccount());
        Assert.assertNull(testStepUpAccountManager.getAppContext());
        Assert.assertNull(testStepUpAccountManager.getName());
    }

    private ApplicationContext getApplicationContext() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        return ctx;
    }

    @Test
    public void testSetters() {
        ApplicationContext ctx = getApplicationContext();
        testStepUpAccountManager.setAccountID("id");
        testStepUpAccountManager.setName("methodName");
        testStepUpAccountManager.setAppContext(ctx);
        Assert.assertEquals("id", testStepUpAccountManager.getAccountID());
        Assert.assertEquals("methodName", testStepUpAccountManager.getName());
        Assert.assertEquals(ctx, testStepUpAccountManager.getAppContext());
    }

    @Test
    public void testDefaultAccountEditing() throws Exception {
        Assert.assertNull(testStepUpAccountManager.addAccount());
        Assert.assertNull(testStepUpAccountManager.getAccount());
    }

    @Test
    public void testInitialize() throws Exception {
        testStepUpAccountManager.setAppContext(getApplicationContext());
        testStepUpAccountManager.setAccountID("ChallengeSender");
        testStepUpAccountManager.initialize(null);
        Assert.assertNotNull(testStepUpAccountManager.getAccount());
    }

    class TestStepUpAccountManager extends AbstractStepUpAccountManager {
    }
}

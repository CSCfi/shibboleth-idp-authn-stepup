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

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccountRestrictorTest {

    AccountRestrictor ar1=new AccountRestrictor();
    AccountRestrictor ar2=new AccountRestrictor();
    AccountRestrictor ar3=new AccountRestrictor();
    
    @BeforeMethod
    public void setup() {
        InMemoryEventStore store = new InMemoryEventStore();
        ar1.setEventStore(store);
        ar2.setEventStore(store);
        ar3.setEventStore(store);
        //ar1 & ar2 represent same type of accounts
        //ar3 is for diffenrent kind of account
        ar1.setType("type_1");
        ar1.setKey("key_1");
        ar2.setType("type_1");
        ar2.setKey("key_2");
        ar3.setType("type_other");
        ar3.setKey("key_1");
        Map<Long, Integer> accountEventLimits=new HashMap<Long, Integer>();
        //10 events within second or 15 within 2 seconds
        accountEventLimits.put((long)1000, (int)10);
        accountEventLimits.put((long)2000, (int)15);
        //2 fail events within second or 3 within 2
        Map<Long, Integer> accountFailEventLimits=new HashMap<Long, Integer>();
        accountFailEventLimits.put((long)1000, (int)2);
        accountFailEventLimits.put((long)2000, (int)3);
        ar1.setAccountEventLimits(accountEventLimits);
        ar1.setAccountFailEventLimits(accountFailEventLimits);
        ar2.setAccountEventLimits(accountEventLimits);
        ar2.setAccountFailEventLimits(accountFailEventLimits);
        ar3.setAccountEventLimits(accountEventLimits);
        ar3.setAccountFailEventLimits(accountFailEventLimits);
    }

    @Test
    public void testFirstLimit() throws InterruptedException {
        for (int i=0;i<10;i++){
            Assert.assertEquals(ar1.limitReached(), 0);
            ar1.addAttempt();
        }
        Assert.assertTrue(ar1.limitReached()>0);
        Thread.sleep(1000);
        Assert.assertEquals(ar1.limitReached(), 0);
    }
    
    @Test
    public void testSecondLimit() throws InterruptedException {
        for (int i=0;i<10;i++){
            Assert.assertEquals(ar1.limitReached(), 0);
            ar1.addAttempt();
        }
        Assert.assertTrue(ar1.limitReached()>0);
        Thread.sleep(1000);
        Assert.assertEquals(ar1.limitReached(), 0);
        for (int i=0;i<5;i++){
            Assert.assertEquals(ar1.limitReached(), 0);
            ar1.addAttempt();
        }
        Assert.assertTrue(ar1.limitReached()>0);
    }
    
    @Test
    public void testMixedLimit() throws InterruptedException {
        for (int i=0;i<5;i++){
            Assert.assertEquals(ar1.limitReached(), 0);
            ar1.addAttempt();
            ar2.addAttempt();
            //not counted to ar1 limits
            ar3.addAttempt();
        }
        Assert.assertTrue(ar1.limitReached()>0);
        Assert.assertEquals(ar3.limitReached(), 0);
        Thread.sleep(1000);
        Assert.assertEquals(ar1.limitReached(), 0);
    }
    
    //TODO: failure events
}
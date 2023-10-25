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

import org.mockito.Mockito;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;

public class AttributeTargetBasedStepUpAccountManagerTest {

    private AttributeTargetBasedStepUpAccountManager manager;
    // Collection<Entry> claims;

    @BeforeMethod
    public void setUp() throws Exception {
        manager = new AttributeTargetBasedStepUpAccountManager();
        StepUpAccountStorage storage = Mockito.mock(StepUpAccountStorage.class);
        Mockito.doReturn(Mockito.mock(StepUpAccount.class)).when(storage).getAccount(Mockito.any(), Mockito.any());
        manager.setAccountID("LogStepUpAccount");
        manager.setAppContext(new ClassPathXmlApplicationContext("applicationContext.xml"));
        // claims = new ArrayList<Entry>();
        // claims.add(new Entry("sub", ClaimRequirement.ESSENTIAL, null, "XYZ"));
        // claims.add(new Entry("mail", ClaimRequirement.ESSENTIAL, null,
        // "mail@example.com"));
        manager.setClaimName("mail");
    }

    // @Test
    public void testInitialisationSuccess() throws Exception {
        // manager.initialize(claims);
        Assert.assertNotNull(manager.getAccount());
    }

    // @Test
    public void testInitialisationSuccessNoAccount() throws Exception {
        manager.setClaimName("not_eppn");
        // manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

    // @Test(expectedExceptions = Exception.class)
    public void testInitialisationFailNoAccountID() throws Exception {
        manager.setAccountID(null);
        // manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

    // @Test(expectedExceptions = Exception.class)
    public void testInitialisationFailNoClaimName() throws Exception {
        manager.setClaimName(null);
        // manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

}

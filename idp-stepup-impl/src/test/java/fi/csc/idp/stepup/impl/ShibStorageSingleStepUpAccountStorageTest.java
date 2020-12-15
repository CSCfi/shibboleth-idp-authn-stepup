/*
 * The MIT License
 * Copyright (c) 2015-2020 CSC - IT Center for Science, http://www.csc.fi
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

import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.Test;
import fi.csc.idp.stepup.api.StepUpAccount;

public class ShibStorageSingleStepUpAccountStorageTest {

    private ShibStorageSingleStepUpAccountStorage shibStepUpAccountStorage;

    @Test
    public void runSequence() throws Exception {
        shibStepUpAccountStorage = new ShibStorageSingleStepUpAccountStorage();
        MemoryStorageService service = new MemoryStorageService();
        service.setId("componentId");
        service.initialize();
        shibStepUpAccountStorage.setStorage(service);
        MockAccount ma1 = new MockAccount();
        ma1.setName("ma1");
        MockAccount ma2 = new MockAccount();
        ma2.setName("ma2");
        shibStepUpAccountStorage.add(ma1, "user1");
        // Overwrite previous
        shibStepUpAccountStorage.add(ma2, "user1");
        shibStepUpAccountStorage.add(new MockAccount(), "user2");
        // Check that accounts may be found from storage
        Assert.assertNotNull(shibStepUpAccountStorage.getAccount("user1", MockAccount.class));
        Assert.assertNotNull(shibStepUpAccountStorage.getAccount("user2", MockAccount.class));
        Assert.assertNull(shibStepUpAccountStorage.getAccount("user3", MockAccount.class));
        StepUpAccount account = shibStepUpAccountStorage.getAccount("user1", MockAccount.class);
        Assert.assertEquals(account.getName(), "ma2");
        shibStepUpAccountStorage.remove(account, "user1");
        Assert.assertNull(shibStepUpAccountStorage.getAccount("user1", MockAccount.class));
    }
}

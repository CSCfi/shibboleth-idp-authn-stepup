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
package fi.csc.idp.stepup.impl;

import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;
import fi.csc.idp.stepup.api.StepUpAccount;

public class TestShibStorageSingleStepUpAccountStorage {

    private ShibStorageSingleStepUpAccountStorage shibStepUpAccountStorage;
    
    
    @Test
    public void runSequence() throws Exception {
        shibStepUpAccountStorage = new ShibStorageSingleStepUpAccountStorage();
        MemoryStorageService service=new MemoryStorageService();
        service.setId("componentId");
        service.initialize();
        shibStepUpAccountStorage.setStorage(service);
        MockAccount ma1 = new MockAccount();
        ma1.setName("ma1");
        MockAccount ma2 = new MockAccount();
        ma2.setName("ma2");
        ma2.setId(2);
        // Insert accounts to storage
        shibStepUpAccountStorage.add(ma1, "user1");
        shibStepUpAccountStorage.add(ma2, "user1");
        shibStepUpAccountStorage.add(new MockAccount(), "user2");
        // Check that accounts may be found from storage
        Assert.assertEquals(shibStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 1);
        Assert.assertEquals(shibStepUpAccountStorage.getAccounts("user2", MockAccount.class).size(), 1);
        Assert.assertEquals(shibStepUpAccountStorage.getAccounts("user3", MockAccount.class).size(), 0);
        List<StepUpAccount> accounts = shibStepUpAccountStorage.getAccounts("user1", MockAccount.class);
        // can we locate account named ma2 and modify it
        long id = -1;
        for (StepUpAccount account : accounts) {
            if ("ma2".equals(account.getName())) {
                id = account.getId();
                account.setName("ma2_updated");
                shibStepUpAccountStorage.update(account, "user1");
            }
        }
        Assert.assertEquals(shibStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 1);
        boolean found = false;
        accounts = shibStepUpAccountStorage.getAccounts("user1", MockAccount.class);
        StepUpAccount acc = null;
        for (StepUpAccount account : accounts) {
            if ("ma2_updated".equals(account.getName())) {
                acc = account;
                found = true;
                Assert.assertEquals(id, account.getId());
            }
        }
        Assert.assertTrue(found);
        // remove the updated account
        found = false;
        shibStepUpAccountStorage.remove(acc, "user1");
        Assert.assertEquals(shibStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 0);
    }

}

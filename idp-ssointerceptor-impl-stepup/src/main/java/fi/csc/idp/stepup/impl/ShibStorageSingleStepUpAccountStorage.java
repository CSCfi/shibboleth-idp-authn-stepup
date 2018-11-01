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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opensaml.storage.StorageService;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.storage.StorageCapabilities;
import org.opensaml.storage.StorageCapabilitiesEx;
import org.opensaml.storage.StorageRecord;

/**
 * Shibboleth storage implementation of Step Up Account storage. Capable of storing only one account per user. Treats
 * add and update operations as the same.
 */
public class ShibStorageSingleStepUpAccountStorage implements StepUpAccountStorage {

    /** Storage service to use. */
    private StorageService storage;

    /** Context. */
    private final String STORAGE_CONTEXT = "fi.csc.idp.stepup.impl.ShibStorageStepUpAccountStorage";

    /**
     * Set the backing storage service for accounts.
     * 
     * @param storageService backing store to use
     */
    public void setStorage(@Nonnull final StorageService storageService) {
        storage = Constraint.isNotNull(storageService, "StorageService cannot be null");
        final StorageCapabilities caps = storage.getCapabilities();
        if (caps instanceof StorageCapabilitiesEx) {
            Constraint.isTrue(((StorageCapabilitiesEx) caps).isServerSide(), "StorageService cannot be client-side");
        }
    }

    @Override
    public void add(StepUpAccount account, String key) throws Exception {
        Constraint.isNotNull(storage, "StorageService cannot be null");
        Constraint.isNotNull(account, "Account cannot be null");
        Constraint.isNotNull(key, "Key cannot be null");
        if (!storage.create(STORAGE_CONTEXT, key, account.serializeAccountInformation(), null)
                && !storage.update(STORAGE_CONTEXT, key, account.serializeAccountInformation(), null)) {
            throw new Exception("Unable to add account");
        }
    }

    @Override
    public void remove(StepUpAccount account, String key) throws Exception {
        Constraint.isNotNull(storage, "StorageService cannot be null");
        Constraint.isNotNull(account, "Account cannot be null");
        Constraint.isNotNull(key, "Key cannot be null");
        storage.delete(STORAGE_CONTEXT, key);
    }

    @Override
    public void update(StepUpAccount account, String key) throws Exception {
        Constraint.isNotNull(storage, "StorageService cannot be null");
        Constraint.isNotNull(account, "Account cannot be null");
        Constraint.isNotNull(key, "Key cannot be null");
        add(account, key);

    }

    @Override
    public <T> List<StepUpAccount> getAccounts(String key, Class<T> aClass) throws Exception {
        Constraint.isNotNull(storage, "StorageService cannot be null");
        Constraint.isNotNull(aClass, "Class cannot be null");
        Constraint.isNotNull(key, "Key cannot be null");
        List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();
        @SuppressWarnings("rawtypes") StorageRecord entry = storage.read(STORAGE_CONTEXT, key);
        if (entry != null) {
            Object obj = aClass.newInstance();
            if (!(obj instanceof StepUpAccount)) {
                throw new Exception("Unable to instantiate StepUpAccount");
            }
            StepUpAccount stepUpAccount = (StepUpAccount) obj;
            if (stepUpAccount.deserializeAccountInformation(entry.getValue())) {
                accounts.add(stepUpAccount);
            } else {
                throw new Exception("Unable to deserialize StepUpAccount from value " + entry.getValue());
            }
        }
        return accounts;
    }

}

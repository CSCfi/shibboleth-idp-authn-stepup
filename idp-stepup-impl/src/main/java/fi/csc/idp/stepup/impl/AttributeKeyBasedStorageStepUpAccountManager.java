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

import java.util.Collection;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;

/**
 * Class implementing step up account manager for accounts stored by key found
 * in attribute values.
 */
public class AttributeKeyBasedStorageStepUpAccountManager extends AbstractStepUpAccountManager {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttributeKeyBasedStorageStepUpAccountManager.class);

    /** The claim name to look for. */
    private String claimName;

    /** The account key. */
    private String key;

    /** remove old account automatically if new one requires space. */
    private boolean autoRemove;

    /** implements the storage functionality. */
    private StepUpAccountStorage stepUpAccountStorage;

    /**
     * Set the implementation of storage functionality.
     * 
     * @param storage functionality
     */
    public void setStepUpAccountStorage(StepUpAccountStorage storage) {
        stepUpAccountStorage = storage;
    }

    /**
     * Set the name for claim containing the value for the key.
     * 
     * @param name of the claim containing the value of key
     */
    public void setClaimName(String name) {
        claimName = name;
    }

    /**
     * Setting automatic removal of accounts on will cause deleting old account if
     * new account requires the space due to account limits.
     * 
     * @param remove true if old accounts make way automatically for new
     */
    public void setAutoRemove(boolean remove) {
        autoRemove = remove;
    }

    /**
     * Initialises account by reading the value for key, using that to read accounts
     * from storage.
     * 
     * @param entry to look for the key value
     */
    @Override
    public boolean initialize(Collection<Entry> entry) throws Exception {

        log.debug("Adding accounts of type {}", getName());
        key = null;
        if (stepUpAccountStorage == null) {
            throw new Exception("repository implementation not set, cannot add accounts");
        }
        if (entry == null) {
            throw new Exception("requested id token claims cannot be null");
        }
        if (claimName == null) {
            throw new Exception("Attribute Id has to be set");
        }
        if (getAccountID() == null) {
            throw new Exception("No account bean defined");
        }
        for (Entry claim : entry) {
            if (claimName.equals(claim.getClaimName())) {
                key = claim.getValue();
                if (key == null) {
                    log.warn("No attribute value for " + claimName);
                    continue;
                }
                setAccount(stepUpAccountStorage.getAccount(key, getAppContext().getBean(getAccountID()).getClass()));
                break;
            }
        }
        return true;
    }

    /**
     * Method checks the operation can be performed.
     * 
     * @param account the account to be operated.
     * @throws Exception if operation cannot be performed.
     */
    private void preCheck(StepUpAccount account) throws Exception {

        if (stepUpAccountStorage == null) {
            throw new Exception("Storage implementation not set, cannot add accounts");
        }
        if (account == null) {
            throw new Exception("Account cannot be null");
        }

    }

    /**
     * Add a new account. Store it.
     * 
     * 
     * @return new account. Null if no new account is added.
     * @throws Exception if something unexpected occurred.
     */
    @Override
    public StepUpAccount addAccount() throws Exception {

        if (stepUpAccountStorage == null) {
            throw new Exception("Storage implementation not set, cannot add accounts");
        }
        if (getAccount() != null) {
            if (!autoRemove) {

                return null;
            }
            StepUpAccount discardedAccount = getAccount();
            stepUpAccountStorage.remove(discardedAccount, key);
        }
        StepUpAccount account = (StepUpAccount) getAppContext().getBean(getAccountID());
        setAccount(account);
        stepUpAccountStorage.add(account, key);
        return account;
    }

    /**
     * Remove account.
     * 
     * @param account to be removd.
     * @throws Exception
     */
    @Override
    public void removeAccount(StepUpAccount account) throws Exception {

        preCheck(account);
        stepUpAccountStorage.remove(account, key);
        setAccount(null);
    }

}

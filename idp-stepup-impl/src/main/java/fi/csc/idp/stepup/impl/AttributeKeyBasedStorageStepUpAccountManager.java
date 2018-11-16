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

import java.util.List;

import javax.annotation.Nonnull;
//import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;

/**
 * Class implementing Step Up Account manager for accounts stored by key found
 * in attribute values.
 * */
public class AttributeKeyBasedStorageStepUpAccountManager extends AbstractStepUpAccountManager {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttributeKeyBasedStorageStepUpAccountManager.class);

    /** The attribute ID to look for. */
    private String attributeId;

    /** The account key. */
    private String key;

    /** maximum number of accounts. */
    private int accountLimit;

    /** remove old account automatically if new one requires space. */
    private boolean autoRemove;

    /** implements the storage functionality. */
    private StepUpAccountStorage stepUpAccountStorage;

    /**
     * Set the implementation of storage functionality.
     * 
     * @param storage
     *            functionality
     */
    public void setStepUpAccountStorage(StepUpAccountStorage storage) {
        this.stepUpAccountStorage = storage;
    }

    /**
     * Set the attribute id containing the value for the key.
     * 
     * @param id
     *            of the attribute containing the value of key
     */
    public void setAttributeId(String id) {
        this.attributeId = id;
    }

    /**
     * Set the limit for maximum number of accounts allowed. Default is no
     * limit. Values below 1 are ignored.
     * 
     * @param limit
     *            maximum number of accounts.
     */
    public void setAccountLimit(int limit) {
        this.accountLimit = limit;
    }

    /**
     * Setting automatic removal of accounts on will cause deleting old account
     * if new account requires the space due to account limits.
     * 
     * @param remove
     *            true if old accounts make way automatically for new
     */
    public void setAutoRemove(boolean remove) {
        this.autoRemove = remove;
    }

    /**
     * Add a new editable account. Store it.
     * 
     * 
     * @return new account. Null if account limits have been reached.
     * @throws Exception
     *             if something unexpected occurred.
     */
    @Override
    public StepUpAccount addAccount() throws Exception {

        if (stepUpAccountStorage == null) {
            throw new Exception("Storage implementation not set, cannot add accounts");
        }
        if (accountLimit > 0 && getAccounts().size() >= accountLimit) {
            if (!autoRemove) {

                return null;
            }
            // we remove old account from the way
            StepUpAccount discardedAccount = getAccounts().get(0);
            stepUpAccountStorage.remove(discardedAccount, key);
            getAccounts().remove(discardedAccount);
        }
        StepUpAccount account = (StepUpAccount) getAppContext().getBean(getAccountID());
        account.setEnabled(true);
        getAccounts().add(account);
        stepUpAccountStorage.add(account, key);

        return account;
    }

    /**
     * Method checks the operation can be performed.
     * 
     * @param account
     *            the account to be operated.
     * @throws Exception
     *             if operation cannot be performed.
     */
    private void preCheck(StepUpAccount account) throws Exception {

        if (stepUpAccountStorage == null) {
            throw new Exception("Storage implementation not set, cannot add accounts");
        }
        if (account == null) {
            throw new Exception("Account cannot be null");
        }
        if (!getAccounts().contains(account)) {
            throw new Exception("Account not managed");
        }

    }

    /**
     * Update a editable account.
     * 
     * @param account
     *            to be updated.
     * @throws Exception
     */
    @Override
    public void updateAccount(StepUpAccount account) throws Exception {

        preCheck(account);
        stepUpAccountStorage.update(account, key);

    }

    /**
     * Remove a editable account.
     * 
     * @param account
     *            to be removd.
     * @throws Exception
     */
    @Override
    public void removeAccount(StepUpAccount account) throws Exception {

        preCheck(account);
        stepUpAccountStorage.remove(account, key);
        getAccounts().remove(account);

    }

    /**
     * Get the editable status.
     * 
     * @return true
     */
    @Override
    public boolean isEditable() {

        return true;
    }

    // Checkstyle: CyclomaticComplexity OFF

    /**
     * Initializes accounts by reading the value for key, using that to read
     * accounts from storage.
     * 
     * @param attributeContext
     *            to look for the key value
     */
    @Override
    public boolean initialize(AttributeContext attributeContext) throws Exception {

        log.debug("Adding accounts of type {}", getName());
        key = null;
        getAccounts().clear();
        if (stepUpAccountStorage == null) {
            throw new Exception("repository implementation not set, cannot add accounts");
        }
        if (attributeContext == null) {
            throw new Exception("Attribute context has to be set");
        }
        if (attributeId == null) {
            throw new Exception("Attribute Id has to be set");
        }
        if (getAccountID() == null) {
            throw new Exception("No account bean defined");
        }
        IdPAttribute attribute = attributeContext.getIdPAttributes().get(attributeId);
        if (attribute == null) {
            log.warn("Not able to create accounts, Attributes do not contain value for " + attributeId);

            return false;
        }
        for (@SuppressWarnings("rawtypes")
        final IdPAttributeValue value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                // We process only the first non null string type attribute
                // value found
                // Key is expected to be a single value string attribute
                key = ((StringAttributeValue) value).getValue();
                if (key == null) {
                    log.warn("No attribute value for " + attributeId);
                    continue;
                }
                log.debug("Adding accounts with key value {}", key);
                List<StepUpAccount> accounts = stepUpAccountStorage.getAccounts(key,
                        getAppContext().getBean(getAccountID()).getClass());
                if (accounts != null && accounts.size() > 0) {
                    log.debug("Adding {} accounts with key value {}", accounts.size(), key);
                    getAccounts().addAll(accounts);
                }
            }
        }

        return true;
    }

    // Checkstyle: CyclomaticComplexity OFF
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;

public class AttributeKeyBasedStorageStepUpAccountManager extends AbstractStepUpAccountManager {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttributeKeyBasedStorageStepUpAccountManager.class);

    /** The attribute ID to look for. */
    private String attributeId;
    
    /** The account key */
    private String key;

    
    private StepUpAccountStorage stepUpAccountStorage;

    
    public void setStepUpAccountStorage(StepUpAccountStorage stepUpAccountStorage) {
        this.stepUpAccountStorage = stepUpAccountStorage;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }
    
    
    /**
     * We add a new GA account to list of accounts.
     * 
     * TODO: ACCOUNT IS NOT STORED TO STORAGE YET
     * @throws Exception 
     */
    @Override
    public StepUpAccount addAccount() throws Exception {
        log.trace("Entering");
        if (stepUpAccountStorage == null) {
            log.error("Storage implementation not set, cannot add accounts");
            log.trace("Leaving");
            return null;
        }
        //Creates a account that is hopefully wired right
        StepUpAccount account = (StepUpAccount) getAppContext().getBean(getAccountID());
        getAccounts().add(account);
        stepUpAccountStorage.add(account, key);
        return account;
    }

    @Override
    public boolean Initialize(AttributeContext attributeContext) {

        log.trace("Entering");
        log.debug("Adding accounts of type " + getName());
        key = null;
        getAccounts().clear();
        if (stepUpAccountStorage == null) {
            log.error("Storage implementation not set, cannot add accounts");
            log.trace("Leaving");
            return false;
        }
        IdPAttribute attribute = attributeContext.getIdPAttributes().get(attributeId);
        if (attribute == null) {
            log.warn("Not able to create accounts, Attributes do not contain value for " + attributeId);
            log.trace("Leaving");
            return false;
        }
        for (@SuppressWarnings("rawtypes")
        final IdPAttributeValue value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                //We process only the first non null string type attribute value found
                //Key is expected to be a single value string attribute
                key = ((StringAttributeValue) value).getValue();
                if (key==null){
                    log.warn("No attribute value for " + attributeId);
                    continue;
                }
                log.debug("Adding accounts with key value " + key);
                List<StepUpAccount> accounts=stepUpAccountStorage.getAccounts(key);
                if (accounts!=null && accounts.size()>0){
                    log.debug("Adding "+accounts.size()+" accounts with key value " + key);
                    getAccounts().addAll(accounts);
                }
            }
        }
        log.trace("Leaving");
        return true;
    }
}

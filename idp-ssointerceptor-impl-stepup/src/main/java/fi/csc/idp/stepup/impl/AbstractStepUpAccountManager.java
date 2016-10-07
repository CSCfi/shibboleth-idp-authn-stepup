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

import net.shibboleth.idp.attribute.context.AttributeContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpMethod;

/** Helper class for Step Up Method implementations. */
public class AbstractStepUpAccountManager implements StepUpMethod {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccountManager.class);
    /** Name for the method. */
    private String name;
    /** Accounts of the method. */
    private List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();
    /** bean id of account bean. */
    private String accountID;
    /** Application context. */
    @Autowired
    private ApplicationContext appContext;

    /**
     * Default constructor.
     */
    public AbstractStepUpAccountManager() {
        super();
    }
    
    /**
     * Set application context for test cases.
     * @param appContext application context
     */
    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }


    /**
     * Get the bean id of the class implementing the account.
     * 
     * @return bean id
     */
    public String getAccountID() {
        return accountID;
    }

    /**
     * Set the bean id of the class implementing the account.
     * 
     * @param id
     *            is bean id
     */
    public void setAccountID(String id) {
        this.accountID = id;
    }

    /**
     * Get the application context.
     * 
     * @return application context
     */
    public ApplicationContext getAppContext() {
        return appContext;
    }

    /**
     * Set the name of the method.
     * 
     * @param methodName
     *            is method name
     */
    public void setName(String methodName) {
        log.trace("Entering & Leaving");
        this.name = methodName;
    }

    /**
     * Get the name of the method.
     * 
     * @return name of the method.
     */
    @Override
    public String getName() {
        log.trace("Entering & Leaving");
        return name;
    }

    /**
     * Get the editable status.
     * 
     * @return false
     */
    @Override
    public boolean isEditable() {
        log.trace("Entering & Leaving");
        return false;
    }

    /**
     * Get the accounts.
     * 
     * @return the accounts
     */
    @Override
    public List<StepUpAccount> getAccounts() throws Exception {
        log.trace("Entering & Leaving");
        return accounts;
    }

    /**
     * Add account.
     * 
     * @return null, adding not supported.
     */
    @Override
    public StepUpAccount addAccount() throws Exception {
        log.trace("Entering & Leaving");
        log.debug("Method not supported");
        return null;
    }

    /**
     * Remove account. Not supported.
     * 
     * @param account
     *            to be removed.
     */
    @Override
    public void removeAccount(StepUpAccount account) {
        log.trace("Entering & Leaving");
        log.debug("Method not supported");
    }

    /**
     * Add a one default account.
     * 
     * @param attributeContext
     *            , not used.
     * @return true if successful
     */
    @Override
    public boolean initialize(AttributeContext attributeContext) throws Exception {
        log.trace("Entering");
        getAccounts().add((StepUpAccount) getAppContext().getBean(getAccountID()));
        log.trace("Leaving");
        return true;
    }

}
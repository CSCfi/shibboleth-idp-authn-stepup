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

import java.util.Map;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpMethod;
import net.shibboleth.idp.attribute.IdPAttribute;

/** Base class for step up method/manager implementations. */
public class AbstractStepUpAccountManager implements StepUpMethod {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccountManager.class);

    /** Name for the method. */
    private String name;

    /** Account of the method. */
    private StepUpAccount account;

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
     * 
     * @param ctx application context
     */
    public void setAppContext(ApplicationContext ctx) {
        this.appContext = ctx;
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
     * @param id is bean id
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
     * @param methodName is method name
     */
    public void setName(String methodName) {

        this.name = methodName;
    }

    /**
     * Get the name of the method.
     * 
     * @return name of the method.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Add account.
     * 
     * @return null, adding not supported.
     */
    @Override
    public StepUpAccount addAccount() throws Exception {

        log.debug("Method not supported");
        return null;
    }

    /**
     * Remove account. Not supported.
     * 
     * @param account to be removed.
     */
    @Override
    public void removeAccount(StepUpAccount account) throws Exception {

        log.debug("Method not supported");
    }

    /**
     * Add a one default account.
     * 
     * @param attributes not used.
     * @return true if successful
     */
    @Override
    public boolean initialize(Map<String, IdPAttribute> attributes) throws Exception {

        account = (StepUpAccount) getAppContext().getBean(getAccountID());
        return true;
    }

    /**
     * Get the default account.
     * 
     * @return the default account, may be null.
     */
    @Override
    public StepUpAccount getAccount() {

        return account;
    }

    protected void setAccount(StepUpAccount account) {

        this.account = account;
    }

}
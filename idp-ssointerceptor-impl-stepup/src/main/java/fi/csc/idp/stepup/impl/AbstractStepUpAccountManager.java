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

public class AbstractStepUpAccountManager implements StepUpMethod {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccountManager.class);
    private String name;
    private List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();
    private String accountID;

    @Autowired
    private ApplicationContext appContext;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public AbstractStepUpAccountManager() {
        super();
    }

    public void setName(String name) {
        log.trace("Entering & Leaving");
        this.name = name;
    }

    @Override
    public String getName() {
        log.trace("Entering & Leaving");
        return name;
    }

    @Override
    public boolean isEditable() {
        log.trace("Entering & Leaving");
        return false;
    }

    @Override
    public List<StepUpAccount> getAccounts() throws Exception{
        log.trace("Entering & Leaving");
        return accounts;
    }

    @Override
    public StepUpAccount addAccount() throws Exception{
        log.trace("Entering & Leaving");
        log.debug("Method not supported");
        return null;
    }

    @Override
    public void removeAccount(StepUpAccount account) {
        log.trace("Entering & Leaving");
        log.debug("Method not supported");
    }

    @Override
    public boolean Initialize(AttributeContext attributeContext) throws Exception {
        log.trace("Entering");
        getAccounts().add((StepUpAccount) getAppContext().getBean(getAccountID()));
        log.trace("Leaving");
        return true;
    }

}
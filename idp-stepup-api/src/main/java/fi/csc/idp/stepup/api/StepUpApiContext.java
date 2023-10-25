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
package fi.csc.idp.stepup.api;

import java.util.Map;
import org.opensaml.messaging.context.BaseContext;

import net.shibboleth.shared.logic.Constraint;

/**
 * Context for that holds information specific to step up API.
 */
public class StepUpApiContext extends BaseContext {

    /** Response map. */
    private Map<String, Object> response;

    /** Step up account. */
    private StepUpAccount account;

    /** Step up account prototype. */
    private StepUpAccount accountPrototype;

    /** Storage for the step up accounts. */
    private StepUpAccountStorage storage;

    /**
     * Constructor.
     *
     * @param accountProto   step up account prototype
     * @param accountStorage storage for the step up accounts
     */
    public StepUpApiContext(StepUpAccount accountProto, StepUpAccountStorage accountStorage) {
        Constraint.isNotNull(accountProto, "Step up proto account must not be null");
        Constraint.isNotNull(accountStorage, "Step up account storage account must not be null");
        accountPrototype = accountProto;
        storage = accountStorage;
    }

    /**
     * Get storage for the step up accounts.
     *
     * @return storage for the step up accounts
     */
    public StepUpAccountStorage getStorage() {
        return storage;
    }

    /**
     * Get response map.
     *
     * @return response map
     */
    public Map<String, Object> getResponse() {
        return response;
    }

    /**
     * Set response parameter map.
     *
     * @param responseParameters response map
     */
    public void setResponse(Map<String, Object> responseParameters) {
        response = responseParameters;
    }

    /**
     * Set account for step up.
     *
     * @param accountStepUp account to be set
     */
    public void setAccount(StepUpAccount accountStepUp) {
        account = accountStepUp;
    }

    /**
     * Get account for step up.
     * 
     * @return Step up account.
     */
    public StepUpAccount getAccount() {
        return account;
    }

    /**
     * Step up account prototype.
     * 
     * @return Step up account prototype.
     */
    public StepUpAccount getAccountPrototype() {
        return accountPrototype;
    }
}

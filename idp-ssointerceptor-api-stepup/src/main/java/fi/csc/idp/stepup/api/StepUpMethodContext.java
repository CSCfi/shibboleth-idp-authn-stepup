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
package fi.csc.idp.stepup.api;

import java.security.Principal;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opensaml.messaging.context.BaseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for passing information of available stepup methods and activated
 * accounts.
 */
public class StepUpMethodContext extends BaseContext {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(StepUpMethodContext.class);

    /** StepUp Methods. */
    private Map<Principal, StepUpMethod> stepupMethods;

    /** Active StepUp account. */
    private StepUpAccount stepUpAccount;

    /** Active StepUp method. */
    private StepUpMethod stepUpMethod;

    public StepUpMethod getStepUpMethod() {
        log.trace("Entering & Leaving");
        return stepUpMethod;
    }

    public void setStepUpMethod(StepUpMethod stepUpMethod) {
        log.trace("Entering & Leaving");
        this.stepUpMethod = stepUpMethod;
    }

    public StepUpAccount getStepUpAccount() {
        log.trace("Entering & Leaving");
        return stepUpAccount;
    }

    public void setStepUpAccount(StepUpAccount stepUpAccount) {
        log.trace("Entering & Leaving");
        this.stepUpAccount = stepUpAccount;
    }

    public Map<Principal, StepUpMethod> getStepUpMethods() {
        log.trace("Entering & Leaving");
        return stepupMethods;
    }

    public void setStepUpMethods(Map<Principal, StepUpMethod> stepUpMethods) {
        log.trace("Entering & Leaving");
        this.stepupMethods = stepUpMethods;
    }

}

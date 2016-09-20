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

import org.opensaml.messaging.context.BaseContext;

/** Context for passing information of avalailable stepup methods
 * and activated accounts. */
public class StepUpMethodContext extends BaseContext{
    
    
    /** StepUp Methods. */
    private Map<Principal, StepUpMethod> stepupMethods;
    
    /** Active StepUp account. */
    private StepUpAccount stepUpAccount; 

    public StepUpAccount getStepUpAccount() {
        return stepUpAccount;
    }

    public void setStepUpAccount(StepUpAccount stepUpAccount) {
        this.stepUpAccount = stepUpAccount;
    }

    public Map<Principal, StepUpMethod> getStepUpMethods() {
        return stepupMethods;
    }

    public void setStepUpMethods(Map<Principal, StepUpMethod> stepUpMethods) {
        this.stepupMethods = stepUpMethods;
    }
    
     
}

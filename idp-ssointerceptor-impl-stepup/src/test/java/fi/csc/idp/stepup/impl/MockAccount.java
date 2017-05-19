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

import fi.csc.idp.stepup.api.FailureLimitReachedException;
import fi.csc.idp.stepup.api.StepUpAccount;

public class MockAccount implements StepUpAccount {

    public String correctResponse = "response_success";
    long id = 0;
    String name;
    
    public boolean noRetries;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean isEditable) {
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void sendChallenge() throws Exception {
    }

    @Override
    public boolean verifyResponse(String response) throws Exception {
        if (response == null && correctResponse == null) {
            return true;
        }
        boolean resp=correctResponse.equals(response);
        if (resp == false && noRetries){
            throw new FailureLimitReachedException("failure limit reached");
        }
        return resp;
    }

    @Override
    public void setTarget(String target) {
    }

    @Override
    public String getTarget() {
        return null;
    }

    @Override
    public boolean isVerified() {
        // TODO Auto-generated method stub
        return false;
    }

}

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

package fi.csc.idp.stepup.api.messaging;

import java.util.Map;

/**
 * An interface for stepup api requests
 */
public interface ApiRequest {

    /**
     * Get request parameter map.
     * 
     * @return request parameter map
     */
    public Map<String, String[]> getRequestParameterMap();

    /**
     * Get token of the request.
     * 
     * @return token of the request
     */
    public String getToken();

    /**
     * Get the user id of the user the action is targeted to.
     * 
     * @return the user id of the user the action is targeted to
     */
    public String getUserId();
    
    /**
     * Get whether to update already existing account.
     * @return whether to update already existing account
     */
    public boolean getForceUpdate();
    
    /**
     * Account limit, default is 1.
     * @return Account account limit, default is 1.
     */
    public int getMaxAccounts();
    
    /**
     * Get the value of the account.
     * @return the value of the account.
     */
    public String getValue();

}

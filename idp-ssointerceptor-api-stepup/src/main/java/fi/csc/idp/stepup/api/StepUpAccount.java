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
/** Interface implemented by accounts. */
public interface StepUpAccount {

    /** string code for setEnabled(true).*/
    public static final String ENABLE = "enable";
    /** string code for setEnabled(false).*/
    public static final String DISABLE = "disable";
    /** string code for setName(string).*/
    public static final String SET_NAME = "setname";
    /** string code for setEditable(true).*/
    public static final String SET_EDITABLE = "seteditable";
    /** string code for setEditable(false).*/
    public static final String SET_NOT_EDITABLE = "setnoteditable";
    
    /**
     * Unique id of the account, may be null.
     * 
     * @return id
     */
    public long getId();

    /**
     * Set id of the account.
     * 
     * @param id
     *            of account
     */
    public void setId(long id);

    /**
     * Name of the account.
     * 
     * @return name
     */
    public String getName();

    /**
     * Set name of the account.
     * 
     * @param name
     *            name
     */
    public void setName(String name);

    /**
     * If account can be modified.
     * 
     * @return true if accounts can be modified
     */
    public boolean isEditable();

    /**
     * Set account to be editable or not.
     * 
     * @param isEditable true if editable.
     */
    public void setEditable(boolean isEditable);

    /**
     * Set the account enabled/disabled.
     * 
     * @param isEnabled
     *            true if enabled
     */
    public void setEnabled(boolean isEnabled);

    /**
     * Status of the account.
     * 
     * @return true if enabled
     */

    public boolean isEnabled();

    /**
     * Invoked when a new fresh challenge should be sent. Not relevant to all
     * implementations.
     *  
     * @throws Exception if something unexpected has occurred.
     */
    public void sendChallenge() throws Exception;

    /**
     * Invoked when user has response to challenge.
     * 
     * @param response
     *            to challenge
     * @return true if user has entered a valid response
     * @throws Exception if something unexpected has occurred.
     */
    public boolean verifyResponse(String response) throws Exception;

    /**
     * If account has been used successfully to verify user.
     * 
     * @return true if account has been used to verify user.
     */
    public boolean isVerified();
    
    /**
     * Target parameter for stepup operations. May be sms number, email address,
     * shared secret or what ever is applicable for the implementation.
     * 
     * @param target
     *            to send challenge to or form of.
     */
    public void setTarget(String target);

    /**
     * Return target parameter.
     * 
     * @return target parameter.
     */
    public String getTarget();

}

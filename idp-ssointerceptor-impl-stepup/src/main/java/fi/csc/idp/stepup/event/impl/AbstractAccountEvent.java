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
package fi.csc.idp.stepup.event.impl;

/** base class for account events. */
public abstract class AbstractAccountEvent extends AbstractEvent {

    /** type of the event, for instance bean id of the account. */
    private final String type;
    /** key of the event, for instance account holder id. */
    private final String key;

    /**
     * Constructor.
     * 
     * @param accountType
     *            type of the event, for instance bean id of the account
     * @param accountKey
     *            key of the event, for instance account holder id
     */
    public AbstractAccountEvent(String accountType, String accountKey) {
        type = accountType;
        key = accountKey;
    }

    /**
     * Get type of the account.
     * 
     * @return account type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get key of the account.
     * 
     * @return account key.
     */
    public String getKey() {
        return key;
    }

}

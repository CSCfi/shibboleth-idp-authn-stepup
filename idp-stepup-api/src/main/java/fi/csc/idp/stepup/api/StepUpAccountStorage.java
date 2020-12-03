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


/** Interface for managing persistent accounts. */
public interface StepUpAccountStorage {

    /**
     * Add new account to storage, store it by key.
     * 
     * @param account
     *            to be stored.
     * @param key
     *            the account belongs to.
     * @throws Exception
     *             if something unexpected occurs.
     */

    void add(final StepUpAccount account, String key) throws Exception;

    /**
     * Remove account stored by key.
     * 
     * @param account
     *            to be removed.
     * @param key
     *            the account has been stored by.
     * @throws Exception
     *             if something unexpected occurs.
     */
    void remove(final StepUpAccount account, String key) throws Exception;

    /**
     * Get account stored by key.
     * 
     * @param key
     *            the account has been stored by
     * @param aClass
     *            The account implementation expected. Must implement
     *            StepUpAccount.
     * @param <T>
     *            Template for the implementation expected.
     * @return account
     * @throws Exception
     *             if something unexpected occurs.
     */
    <T> StepUpAccount getAccount(String key, Class<T> aClass) throws Exception;
}

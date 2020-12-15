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

import java.util.Collection;
import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;

/** Interface for managing step up methods. */
public interface StepUpMethod {

	/** string code for addAccount(). */
	public static final String ADD_ACCOUNT = "addaccount";

	/** string code for removeAccount(). */
	public static final String REMOVE_ACCOUNT = "removeaccount";

	/**
	 * This is called before any other calls to initialise the step up method and
	 * possibly existing accounts.
	 * 
	 * @param attributeContext
	 *            may be used by initialisation.
	 * @return true if initialisation was successful.
	 * @throws Exception
	 *             if something unexpected occurred
	 */

	public boolean initialize(Collection<Entry> entry) throws Exception;

	/**
	 * Name of the step up method.
	 * 
	 * @return name of the method.
	 */
	public String getName();
	
	/**
	 * Account of the method.
	 * 
	 * @return account
	 */
	public StepUpAccount getAccount();

	/**
	 * Adds a new account.
	 * 
	 * @return new account.
	 * @throws Exception
	 *             if something unexpected occurred
	 */
	public StepUpAccount addAccount() throws Exception;

	/**
	 * Remove a account.
	 * 
	 * @param account
	 *            to be removed.
	 * @throws Exception
	 *             if something unexpected occurred
	 */
	public void removeAccount(StepUpAccount account) throws Exception;
}

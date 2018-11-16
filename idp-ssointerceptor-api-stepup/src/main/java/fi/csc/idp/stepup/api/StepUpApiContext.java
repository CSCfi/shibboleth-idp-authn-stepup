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

import java.util.List;
import java.util.Map;

import org.opensaml.messaging.context.BaseContext;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Context for stepup api.
 */
public class StepUpApiContext extends BaseContext {

	/** Response map. */
	private Map<String, Object> response;
	
	/** Stepup account.  */  
	private StepUpAccount account;
	
	/** Storage for the step up accounts. */
    private StepUpAccountStorage storage;

	/**
	 * Constructor.
	 * @param account stepup account
	 * @param storage storage for the step up accounts
	 */
	public StepUpApiContext(StepUpAccount account, StepUpAccountStorage storage) {
		Constraint.isNotNull(account, "Stepup account must not be null");
		Constraint.isNotNull(storage, "StepUpAccountStorage account must not be null");
		this.account=account;
		this.storage=storage;
	}

	/**
     * Get storage for the step up accounts.
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
	 * Set response map.
	 * 
	 * @param response
	 *            response map
	 */
	public void setResponse(Map<String, Object> response) {
		this.response = response;
	}

	/** Accounts of the user. */
	private List<StepUpAccount> accounts;

	/**
	 * Get accounts of the user.
	 * 
	 * @return accounts of the user
	 */
	public List<StepUpAccount> getAccounts() {
		return accounts;
	}

	/**
	 * Set accounts of the user.
	 * 
	 * @param accounts
	 *            accounts of the user
	 */
	public void setAccounts(List<StepUpAccount> accounts) {
		this.accounts = accounts;
	}
	
	/**
	 * Stepup account.
	 * @return Stepup account.
	 */
	public StepUpAccount getAccount() {
		return account;
	}
}

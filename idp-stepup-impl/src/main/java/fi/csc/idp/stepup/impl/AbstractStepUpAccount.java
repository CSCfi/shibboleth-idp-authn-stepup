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

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.LimitReachedException;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.event.api.AccountRestrictorAction;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/** Helper class for StepUpAccount implementations. */
public abstract class AbstractStepUpAccount implements StepUpAccount {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccount.class);
    /** Name of the account. */
    private String name;
    /** Challenge Verifier. */
    private ChallengeVerifier challengeVerifier;
    /** Challenge created. */
    private String challenge;
    /** Target parameter for challenge. **/
    private String target;

    /** id of the account. */
    private long id;
    /** Challenge Generator. */
    private ChallengeGenerator challengeGenerator;
    /** account is editable. */
    private boolean editable;
    /** account is enabled. */
    private boolean enabled;
    /** user has been verified. */
    private boolean verified;
    /** interface to control account usage. */
    private AccountRestrictorAction accountRestrictorAction;

    /** default constructor. */
    public AbstractStepUpAccount() {
        super();

        this.editable = true;
    }

    /**
     * Get the id of the account.
     * 
     * @return id of the account
     */
    @Override
    public long getId() {

        return this.id;
    }

    /**
     * Set the id of the account. Non editable account cannot be modified.
     * 
     * @param idValue
     *            of the account.
     */
    @Override
    public void setId(long idValue) {

        if (this.editable) {
            this.id = idValue;
        } else {
            log.warn("not supported");
        }

    }

    /**
     * Get the challenge created.
     * 
     * @return challenge created
     */
    public String getChallenge() {
        return this.challenge;
    }

    /**
     * Set the challenge generator implementation.
     * 
     * @param generator
     *            implementation
     */
    public void setChallengeGenerator(ChallengeGenerator generator) {

        this.challengeGenerator = generator;
    }

    /**
     * Set the challenge verifier implementation.
     * 
     * @param verifier
     *            implementation
     */
    public void setChallengeVerifier(ChallengeVerifier verifier) {

        this.challengeVerifier = verifier;
    }

    /**
     * Set the name of the account. Non editable account cannot be modified.
     * 
     * @param accountName
     *            name of the account
     */
    public void setName(String accountName) {

        if (this.editable) {
            this.name = accountName;
        } else {
            log.warn("not supported");
        }

    }

    /**
     * Get the name of the account.
     * 
     * @return name of the account
     */
    @Override
    public String getName() {

        return this.name;
    }

    /**
     * Is the account editable.
     * 
     * @return true if editable
     */
    @Override
    public boolean isEditable() {

        return this.editable;
    }

    /**
     * If the account has been used to verify the the user.
     * 
     * @return true if verified.
     */
    @Override
    public boolean isVerified() {

        return this.verified;
    }

    /**
     * Set the account editable/non editable. Non editable account cannot be
     * modified.
     * 
     * @param isEditable
     *            true if editable.
     */
    @Override
    public void setEditable(boolean isEditable) {

        if (this.editable) {
            this.editable = isEditable;
        } else {
            log.warn("not supported");
        }

    }

    /**
     * Set account enabled/disabled. Non editable account cannot be modified.
     * 
     * @param isEnabled
     *            true if enabled
     */
    @Override
    public void setEnabled(boolean isEnabled) {

        if (this.editable) {
            this.enabled = isEnabled;
        } else {
            log.warn("not supported");
        }

    }

    /**
     * Get the account enabled status.
     * 
     * @return true if the account is enabled
     */
    @Override
    public boolean isEnabled() {

        return this.enabled;
    }

    /**
     * Send the challenge.
     * 
     * @throws Exception
     *             if something unexpected occurred
     */
    @Override
    public void sendChallenge() throws Exception {

        if (accountRestrictorAction != null) {
            // We are adding a new "try" event
            accountRestrictorAction.addAttempt();
            // Let's see if account use limit is already reached by that.
            long pause = accountRestrictorAction.limitReached();
            if (pause > 0) {
                log.warn("Account limits reached for account {}, must wait for {}ms", target, pause);
                throw new LimitReachedException("Account verification retry limit reached");
            }
        }
        challenge = null;
        if (challengeGenerator != null) {
            challenge = challengeGenerator.generate(null);
        }
        doSendChallenge();
    }

    /**
     * Override to implement the challenge sending.
     * 
     * @throws Exception
     *             if something unexpected occurs.
     */
    protected abstract void doSendChallenge() throws Exception;

    /**
     * Verify the response to challenge.
     * 
     * @param response
     *            response to be verified.
     * @throws Exception
     *             if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        this.verified = doVerifyResponse(response);
        if (accountRestrictorAction != null) {
            // We add a failure event
            if (!this.verified) {
                accountRestrictorAction.addFailure();
            }
            long pause = accountRestrictorAction.limitReached();
            if (pause > 0) {
                log.warn("Account limits reached for account {}, must wait for {}ms", target, pause);
                throw new LimitReachedException("Account verification retry limit reached");
            }

        }
        return this.verified;
    }

    /**
     * Override to implement different challenge verification.
     * 
     * @param response
     *            response to check against challenge.
     * @return true if challenge response was valid.
     * @throws Exception
     *             if something unexpected occurs.
     */
    protected boolean doVerifyResponse(String response) throws Exception {
        if (challenge == null) {
            throw new Exception("null challenge defies logic");
        }
        if (challengeVerifier == null) {
            throw new Exception("Bean not configured with ChallengeVerifier");
        }
        return challengeVerifier.verify(challenge, response, null);
    }

    /**
     * Set the target parameter. Non editable account cannot be modified.
     * 
     * @param accountTarget
     *            representing the Target
     */
    @Override
    public void setTarget(String accountTarget) {

        if (this.editable) {
            this.target = accountTarget;
        } else {
            log.warn("not supported");
        }

    }

    /**
     * Get the target.
     * 
     * @return target
     */
    @Override
    public String getTarget() {
        return this.target;
    }

    /**
     * Set the restrictor for the account.
     * 
     */
    @Override
    public void setAccountRestrictor(AccountRestrictorAction restrictor) {
        accountRestrictorAction = restrictor;
    }
    
	/**
	 * Serialize the account information to string for storing it.
	 * 
	 * @return serialized account.
	 */
	public String serializeAccountInformation() {
		JSONObject serializedAccount = new JSONObject();
		serializedAccount.put("id", getId());
		serializedAccount.put("name", getName());
		serializedAccount.put("target", getTarget());
		serializedAccount.put("enabled", isEnabled());
		serializedAccount.put("editable", isEditable());
		return serializedAccount.toJSONString();
	}

	/**
	 * Initialize the account from serialized account information.
	 * 
	 * @param serializedAccount
	 *            serialized account information
	 * @return true if information successfully read, otherwise false.
	 * @throws ParseException
	 */
	public boolean deserializeAccountInformation(String serializedAccountInformation) {
		try {
			JSONObject serializedAccount = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
					.parse(serializedAccountInformation);
			setId(((Integer) serializedAccount.get("id")).longValue());
			setName((String) serializedAccount.get("name"));
			setTarget((String) serializedAccount.get("target"));
			setEnabled((boolean) serializedAccount.get("enabled"));
			setEditable((boolean) serializedAccount.get("editable"));
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

}
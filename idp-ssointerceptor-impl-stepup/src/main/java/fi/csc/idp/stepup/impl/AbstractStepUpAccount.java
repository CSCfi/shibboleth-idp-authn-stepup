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
import fi.csc.idp.stepup.api.FailureLimitReachedException;
import fi.csc.idp.stepup.api.StepUpAccount;

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
    /** retry limit for trying to successfully verify. */
    private int retries = -1;

    /**
     * Set 0 or a positive number for retries allowed. Negative number is
     * interpreted as infinite retries. 0 means one try, no retries.
     * 
     * @param retries
     *            allowed.
     */
    public void setRetryLimit(int limit) {
        this.retries = limit;
    }

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
     * Set the status of user being verified.
     */
    protected void setVerified() {
        
        this.verified = true;
    }

    /**
     * Method to check if account failure limit is reached after failed
     * authentication.
     * 
     * 
     * @throws FailureLimitReachedException
     *             if account limit is reached.
     */
    protected void verificationFailedCheck() throws FailureLimitReachedException {
        if (this.retries < 0) {
            return;
        }
        if (this.retries == 0) {
            throw new FailureLimitReachedException("Account verification retry limit reached");
        }
        this.retries--;
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
        
        challenge = null;
        if (challengeGenerator == null) {
            throw new Exception("Bean not configured with ChallengeGenerator");
        }
        challenge = challengeGenerator.generate(null);
        
    }

    /**
     * Verify the response to challenge.
     * 
     * @throws Exception
     *             if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        
        if (challenge == null) {
            
            throw new Exception("null challenge defies logic");
        }
        if (challengeVerifier == null) {
            
            throw new Exception("Bean not configured with ChallengeVerifier");
        }
        this.verified = challengeVerifier.verify(challenge, response, null);
        if (!this.verified) {
            verificationFailedCheck();
        }
        
        return this.verified;
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

}
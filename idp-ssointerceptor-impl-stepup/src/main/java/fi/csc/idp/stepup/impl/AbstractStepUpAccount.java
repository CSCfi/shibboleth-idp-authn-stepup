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
    

    /** default constructor. */
    public AbstractStepUpAccount() {
        super();
        log.trace("Entering & Leaving");
        this.editable = true;
    }

    /**
     * Get the id of the account.
     * 
     * @return id of the account
     */
    @Override
    public long getId() {
        log.trace("Entering & Leaving");
        return id;
    }

    /**
     * Set the id of the account. Non editable account cannot be modified.
     * 
     * @param idValue
     *            of the account.
     */
    @Override
    public void setId(long idValue) {
        log.trace("Entering");
        if (this.editable) {
            this.id = idValue;
        } else {
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    /**
     * Get the challenge created.
     * 
     * @return challenge created
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Set the challenge generator implementation.
     * 
     * @param generator
     *            implementation
     */
    public void setChallengeGenerator(ChallengeGenerator generator) {
        log.trace("Entering & Leaving");
        this.challengeGenerator = generator;
    }

    /**
     * Set the challenge verifier implementation.
     * 
     * @param verifier
     *            implementation
     */
    public void setChallengeVerifier(ChallengeVerifier verifier) {
        log.trace("Entering & Leaving");
        this.challengeVerifier = verifier;
    }

    /**
     * Set the name of the account. Non editable account cannot be modified.
     * 
     * @param accountName
     *            name of the account
     */
    public void setName(String accountName) {
        log.trace("Entering");
        if (this.editable) {
            this.name = accountName;
        } else {
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    /**
     * Get the name of the account.
     * 
     * @return name of the account
     */
    @Override
    public String getName() {
        log.trace("Entering & Leaving");
        return name;
    }

    /**
     * Is the account editable.
     * 
     * @return true if editable
     */
    @Override
    public boolean isEditable() {
        log.trace("Entering & Leaving");
        return this.editable;
    }
    
    /**
     * If the account has been used to verify the 
     * the user.
     * 
     * @return true if verified.
     */
    @Override
    public boolean isVerified() {
        log.trace("Entering & Leaving");
        return this.verified;
    }

    /**
     * Set the status of user being verified. 
     */
    protected void setVerified() {
        log.trace("Entering & Leaving");
        this.verified=true;
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
        log.trace("Entering");
        if (this.editable) {
            this.editable = isEditable;
        } else {
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    /**
     * Set account enabled/disabled. Non editable account cannot be modified.
     * 
     * @param isEnabled
     *            true if enabled
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        log.trace("Entering");
        if (this.editable) {
            this.enabled = isEnabled;
        } else {
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    /**
     * Get the account enabled status.
     * 
     * @return true if the account is enabled
     */
    @Override
    public boolean isEnabled() {
        log.trace("Entering & Leaving");
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
        log.trace("Entering");
        challenge = null;
        if (challengeGenerator == null) {
            throw new Exception("Bean not configured with ChallengeGenerator");
        }
        challenge = challengeGenerator.generate(null);
        log.trace("Leaving");
    }

    /**
     * Verify the response to challenge.
     * 
     * @throws Exception
     *             if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        log.trace("Entering");
        if (challenge == null) {
            log.trace("Leaving");
            throw new Exception("null challenge defies logic");
        }
        if (challengeVerifier == null) {
            log.trace("Leaving");
            throw new Exception("Bean not configured with ChallengeVerifier");
        }
        this.verified = challengeVerifier.verify(challenge, response, null); 
        log.trace("Leaving");
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
        log.trace("Entering");
        if (this.editable) {
            this.target = accountTarget;
        } else {
            log.warn("not supported");
        }
        log.trace("Leaving");
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
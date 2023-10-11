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

package fi.csc.idp.stepup.impl;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.StepUpAccount;

/** Base class for step up account implementations. */
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
    /** Challenge Generator. */
    private ChallengeGenerator challengeGenerator;

    /** default constructor. */
    public AbstractStepUpAccount() {
        super();
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
     * @param generator implementation
     */
    public void setChallengeGenerator(ChallengeGenerator generator) {

        this.challengeGenerator = generator;
    }

    /**
     * Set the challenge verifier implementation.
     * 
     * @param verifier implementation
     */
    public void setChallengeVerifier(ChallengeVerifier verifier) {

        this.challengeVerifier = verifier;
    }

    /**
     * Set the name of the account. Non editable account cannot be modified.
     * 
     * @param accountName name of the account
     */
    public void setName(String accountName) {

        this.name = accountName;

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
     * Send the challenge.
     * 
     * @throws Exception if something unexpected occurred
     */
    @Override
    public void sendChallenge() throws Exception {

        challenge = null;
        if (challengeGenerator != null) {
            challenge = challengeGenerator.generate(null);
        }
        doSendChallenge();
    }

    /**
     * Override to implement the challenge sending.
     * 
     * @throws Exception if something unexpected occurs.
     */
    protected abstract void doSendChallenge() throws Exception;

    /**
     * Verify the response to challenge.
     * 
     * @param response response to be verified.
     * @throws Exception if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        return doVerifyResponse(response);
    }

    /**
     * Override to implement different challenge verification.
     * 
     * @param response response to check against challenge.
     * @return true if challenge response was valid.
     * @throws Exception if something unexpected occurs.
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
     * @param accountTarget representing the Target
     */
    @Override
    public void setTarget(String accountTarget) {

        this.target = accountTarget;

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
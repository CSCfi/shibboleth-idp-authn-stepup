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

abstract public class AbstractStepUpAccount implements StepUpAccount {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccount.class);
    /** Name of the account */
    private String name;
    /** Challenge Verifier. */
    private ChallengeVerifier challengeVerifier;
    /** Challenge created. */
    private String challenge;
    /** Target parameter for challenge **/
    private String target;

    private long id;
    /** Challenge Generator. */
    private ChallengeGenerator challengeGenerator;
    private boolean editable;
    private boolean enabled;
    
    public AbstractStepUpAccount() {
        super();
        this.editable=true;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;

    }

    public String getChallenge() {
        return challenge;
    }

    

    public void setChallengeGenerator(ChallengeGenerator challengeGenerator) {
        log.trace("Entering & Leaving");
        this.challengeGenerator = challengeGenerator;
    }

    public void setChallengeVerifier(ChallengeVerifier challengeVerifier) {
        log.trace("Entering & Leaving");
        this.challengeVerifier = challengeVerifier;
    }

    public void setName(String name) {
        log.trace("Entering & Leaving");
        this.name = name;
    }

    @Override
    public String getName() {
        log.trace("Entering & Leaving");
        return name;
    }

    @Override
    public boolean isEditable() {
        log.trace("Entering & Leaving");
        return this.editable;
    }
    
    @Override
    public void setEditable(boolean isEditable) {
        log.trace("Entering");
        if (this.editable){
            this.editable=isEditable;
        }else{
            log.warn("not supported");
        }
        log.trace("Leaving");
    }


    @Override
    public void setEnabled(boolean isEnabled) {
        log.trace("Entering");
        if (this.editable){
            this.enabled=isEnabled;
        }else{
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    @Override
    public boolean isEnabled() {
        log.trace("Entering & Leaving");
        return this.enabled;
    }

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
        return challengeVerifier.verify(challenge, response, null);
    }

    @Override
    public void setTarget(String target) {
        log.trace("Entering");
        if (this.editable){
            this.target = target;
        }else{
            log.warn("not supported");
        }
        log.trace("Leaving");
    }

    @Override
    public String getTarget() {
        return this.target;
    }

}
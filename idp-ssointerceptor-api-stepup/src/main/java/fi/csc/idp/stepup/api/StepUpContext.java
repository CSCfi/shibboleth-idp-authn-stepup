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

import org.opensaml.messaging.context.BaseContext;

/** Context for passing challenge and target between actions. */
public class StepUpContext extends BaseContext{
    
    /** Challenge for the user. */
    private String challenge;
    /** Challenge target representing the user. */
    private String target;
    /** Challenge for the user. Stored to context only for
     * presenting it to user */
    private String sharedSecret;
    

    /**
     * For presenting the secret to user 
     * in registration phase.
     * 
     * @return shared secret.
     */
    public String getSharedSecret() {
        return sharedSecret;
    }

    /**
     * For setting the secret to 
     * context.
     * 
     * @param sharedSecret
     */
    public void setSharedSecret(String sharedSecretValue) {
        this.sharedSecret = sharedSecretValue;
    }

    /**
     * Getter for target parameter.
     * 
     * @return target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Setter for target parameter.
     * 
     * @param targetValue represents the user
     */
    public void setTarget(String targetValue) {
        this.target = targetValue;
    }

    /**
     * Getter for challenge parameter.
     * 
     * @return challenge
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Setter for challenge parameter.
     * 
     * @param challengeValue presented to user
     */
    public void setChallenge(String challengeValue) {
        this.challenge = challengeValue;
    }

}

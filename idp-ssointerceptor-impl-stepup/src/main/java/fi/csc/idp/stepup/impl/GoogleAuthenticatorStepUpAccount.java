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

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

/** Step Up Account implementation for GA.*/
public class GoogleAuthenticatorStepUpAccount extends AbstractStepUpAccount {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(GoogleAuthenticatorStepUpAccount.class);
    
    @Override
    public String getTarget() {
        
        if (super.getTarget() == null) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            final GoogleAuthenticatorKey key = gAuth.createCredentials();
            log.debug("Secret key with value " + key.getKey() + " created");
            setTarget(key.getKey());
        }
        
        return super.getTarget();
    }

    /**
     * GA does not send challenge as it is totp.
     * 
     */
    @Override
    public void sendChallenge() throws Exception {
        
        log.debug("not supported");
        
    }
    
    /**
     *Verify users response to totp challenge.
     *
     *@param response to totp challenge.
     *@return true if response was verified successfully
     *@throws Exception if something unexpected occurred
     */
    @Override
    public boolean verifyResponse(String response) throws Exception {
        
        log.debug("Verificating totp response " + response);
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int code = Integer.parseInt(response);
        boolean verified=gAuth.authorize(getTarget(), code);
        if (verified){
            setVerified();
        }else{
            verificationFailedCheck();
        }
        
        return verified;
      }

    
}

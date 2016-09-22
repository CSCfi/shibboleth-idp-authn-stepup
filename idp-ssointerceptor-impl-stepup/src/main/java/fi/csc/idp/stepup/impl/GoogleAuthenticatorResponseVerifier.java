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

import fi.csc.idp.stepup.api.ChallengeVerifier;

// GENERATE STEPUP is performed before!!
//CHECKS AND SETS THE KEY BY CHECKING A ATTRIBUTE
//CREATES A CHALLENGE, DUMMY IN THIS CASE
//CALLS SEND METHOD, DUMMY IN THIS CASE
//ALL WE NEED IS THE KEY PROVIDED BY FIRST PHASE??

/** class implementing challenge response verification based on equality. */
public class GoogleAuthenticatorResponseVerifier implements ChallengeVerifier {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(GoogleAuthenticatorResponseVerifier.class);

    @Override
    public boolean verify(String key, String response, String target) {
        log.trace("Entering");
        log.debug("Verificating response " + response + " against key" + key);
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int code = Integer.parseInt(response);
        log.trace("Leaving");
        // TODO: key must be decrypted
        return gAuth.authorize(key, code);
    }

}

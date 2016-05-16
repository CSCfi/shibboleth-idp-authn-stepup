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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeGenerator;

import org.apache.commons.codec.binary.Hex;

/** class implementing challenge generation based on digest.*/
public class DigestChallengeGenerator implements ChallengeGenerator {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(DigestChallengeGenerator.class);

    /** The salt for digest. */
    @Nullable
    private String salt = "replaceme";

    /** The max length for Challenge. */
    @Nullable
    private int maxLength = 8;

    /** The digest. */
    @Nullable
    private String digest = "SHA-256";

    /**
     * Set the salt.
     * 
     * @param newSalt
     *            to replace default one.
     */
    public void setSalt(@Nonnull String newSalt) {
        log.trace("Entering");
        salt = newSalt;
        log.trace("Leaving");
    }

    /**
     * Set the maximum length for challenge. If smaller than four, has no
     * effect.
     * 
     * @param newMaxLength
     *            to replace default one.
     */
    public void setMaxLength(int newMaxLength) {
        log.trace("Entering");
        if (newMaxLength > 3) {
            maxLength = newMaxLength;
        }
        log.trace("Leaving");
    }

    /**
     * Set the digest for generating the challenge.
     * 
     * @param newDigest
     *            to replace default one.
     */
    public void setDigest(@Nonnull String newDigest) {
        log.trace("Entering");
        digest = newDigest;
        log.trace("Leaving");
    }

    @Override
    public String generate(String target) throws Exception {
        log.trace("Entering");
        String challenge = null;
        try {
            String time = "" + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance(digest);
            // to make challenge user specific
            if (target != null){
                md.update(target.getBytes());
            }
            // to make challenge installation specific
            md.update(salt.getBytes());
            // to make challenge time specific
            md.update(time.getBytes());
            challenge = Hex.encodeHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("unable to generate challenge " + e.getMessage());
            return null;
        }
        log.trace("Leaving");
        return challenge
                .substring(0, challenge.length() > maxLength ? maxLength
                        : challenge.length());
    }

}

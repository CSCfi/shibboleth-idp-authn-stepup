package fi.csc.idp.stepup.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeGenerator;

import org.apache.commons.codec.binary.Hex;

public class DigestChallengeGenerator implements ChallengeGenerator {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(DigestChallengeGenerator.class);

    /** The salt for digest. */
    @Nullable
    private String salt = "replaceme";

    /** The max length for Challenge */
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
        if (target == null) {
            log.trace("Leaving");
            throw new Exception("Cannot generate digest for null value");
        }
        try {
            String time = "" + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance(digest);
            // to make challenge user specific
            md.update(target.getBytes());
            // to make challenge installation specific
            md.update(salt.getBytes());
            // to make challenge time specific
            md.update(time.getBytes());
            challenge = Hex.encodeHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("unable to generate challenge " + e.getMessage());
            log.error(e.getStackTrace().toString());
            return null;
        }
        log.trace("Leaving");
        return challenge
                .substring(0, challenge.length() > maxLength ? maxLength
                        : challenge.length());
    }

}

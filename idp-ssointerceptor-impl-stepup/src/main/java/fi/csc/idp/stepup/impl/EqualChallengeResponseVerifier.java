package fi.csc.idp.stepup.impl;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeVerifier;

public class EqualChallengeResponseVerifier implements ChallengeVerifier {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(EqualChallengeResponseVerifier.class);
    @Override
    public boolean verify(String challenge, String response, String target)
            {
        log.trace("Entering");
        if (challenge == null && response == null){
            log.trace("Leaving");
            return true;
        }
        if (challenge == null || response == null){
            log.trace("Leaving");
            return false;
        }
        log.trace("Leaving");    
        return challenge.trim().equals(response.trim());
    }

}

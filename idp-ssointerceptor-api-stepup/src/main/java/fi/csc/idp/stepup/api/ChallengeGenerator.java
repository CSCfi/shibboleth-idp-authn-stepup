package fi.csc.idp.stepup.api;

import javax.annotation.Nonnull;

public interface ChallengeGenerator {

    /**
     * Sends the given challenge to target.
     * 
     * @param target
     *            of the challenge.
     * 
     * @return challenge
     * 
     */
    String generate(@Nonnull final String target);
}

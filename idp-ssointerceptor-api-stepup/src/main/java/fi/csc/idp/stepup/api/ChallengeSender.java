package fi.csc.idp.stepup.api;

import javax.annotation.Nonnull;

public interface ChallengeSender {

    /**
     * Sends the given challenge to target.
     * 
     * @param challenge
     *            to be sent.
     * 
     * @param target
     *            of the challenge.
     * 
     */
    void send(@Nonnull final String challenge, @Nonnull final String target) throws Exception;
}

package fi.csc.idp.stepup.api;

/** interface for passing the challenge to target. */
public interface ChallengeSender {

    /**
     * Sends the given challenge to user represented by the target parameter.
     * 
     * @param challenge
     *            to be sent.
     * @param target
     *            of the challenge. Represents the user.
     * @throws Exception
     *             if the sending of the challenge has failed.
     * 
     */
    void send(final String challenge, final String target) throws Exception;
}

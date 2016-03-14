package fi.csc.idp.stepup.api;

/** Interface for generating the challenge. */
public interface ChallengeGenerator {

    /**
     * Generates a challenge for the target.
     * 
     * @param target
     *            represents the target of the challenge.
     * 
     * @return challenge
     * @throws Exception
     *             if the sending of the challenge has failed.
     */
    String generate(final String target) throws Exception;
}

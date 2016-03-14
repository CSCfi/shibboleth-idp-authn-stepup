package fi.csc.idp.stepup.api;

/** Interface for performing challenge response verification. */
public interface ChallengeVerifier {

    /**
     * Verifies that the response is acceptable for the challenge and target.
     * 
     * 
     * @param challenge
     *            Challenge generated for the target.
     * @param response
     *            Users response to the challenge
     * @param target
     *            input used for generating the challenge. Represents the user
     *            responding to challenge.
     * @return boolean true if the response was acceptable
     */
    boolean verify(final String challenge, final String response,
            final String target);

}

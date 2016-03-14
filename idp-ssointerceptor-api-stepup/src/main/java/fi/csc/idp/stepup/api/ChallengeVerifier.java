package fi.csc.idp.stepup.api;

import javax.annotation.Nonnull;

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
     *            identifier for the user.            
     * 
     */
    boolean verify(final String challenge, final String response, final String target);

}

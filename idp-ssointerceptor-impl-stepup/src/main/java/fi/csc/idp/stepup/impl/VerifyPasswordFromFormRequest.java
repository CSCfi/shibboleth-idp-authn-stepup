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
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that verifies user challenge response.
 * 
 */
@SuppressWarnings("rawtypes")
public class VerifyPasswordFromFormRequest extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(VerifyPasswordFromFormRequest.class);
    
    /** Challenge Verifier. */
    private ChallengeVerifier challengeVerifier;

    /**
     * Set the challenge verifier.
     * 
     * @param verifier
     *            for verifying the challenge
     */
    public void setChallengeVerifier(@Nonnull ChallengeVerifier verifier) {
        log.trace("Entering");
        challengeVerifier = verifier;
        log.trace("Leaving");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        // TODO: Move this to PreExecute, maybe we should return false already
        // from there?
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            // Add StepUpEventIds.EXCEPTION to supported errors, map it?
            log.debug(
                    "{} Profile action does not contain an HttpServletRequest",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        // TODO: parameter name to as init value
        final String challengeResponse = request
                .getParameter("j_challengeResponse");
        if (challengeResponse == null || challengeResponse.isEmpty()) {
            // TODO: This is a case that should result in SAML error right from here
            // make it and verify
            // Add StepUpEventIds.EXCEPTION to supported errors, map it?
            log.debug("User did not present response to challenge",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_INVALID_RESPONSE);
            log.trace("Leaving");
            return;
        }
        log.debug("User challenge response was " + challengeResponse);
        //TODO: Read following from context once supported.
        String challenge = (String) request.getSession().getAttribute(
                "fi.csc.idp.stepup.impl.GenerateStepUpChallenge.challenge");
        String target = (String) request.getSession().getAttribute(
                "fi.csc.idp.stepup.impl.GenerateStepUpChallenge.target");
        if (!challengeVerifier.verify(challenge, challengeResponse, target)){
            // This is a case that should result in SAML error right from here
            // make it and verify
            // Add StepUpEventIds.EXCEPTION to supported errors, map it?
            log.debug("User presented wrong response to  challenge",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    StepUpEventIds.EVENTID_INVALID_RESPONSE);
            log.trace("Leaving");
            return;
        }
        ActionSupport.buildEvent(profileRequestContext,
                StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");

    }

}
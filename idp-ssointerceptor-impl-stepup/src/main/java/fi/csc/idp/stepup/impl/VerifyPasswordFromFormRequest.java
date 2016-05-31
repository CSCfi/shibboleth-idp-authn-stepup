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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeVerifier;
import fi.csc.idp.stepup.api.StepUpContext;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

/**
 * An action that verifies user challenge response.
 * 
 */
@SuppressWarnings("rawtypes")
public class VerifyPasswordFromFormRequest extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(VerifyPasswordFromFormRequest.class);

    /** Challenge Verifiers. */
    private Map<Principal, ChallengeVerifier> challengeVerifiers;

    /** proxy authentication context. */
    private ShibbolethSpAuthenticationContext shibbolethContext;

    /** stepup context. */
    private StepUpContext stepUpContext;

    /** Challenge response parameter. */
    private String challengeResponseParameter = "j_challengeResponse";

    /** Sets the parameter the response is read from. */
    public void setChallengeResponseParameter(@Nonnull @NotEmpty String challengeResponseParameter) {
        this.challengeResponseParameter = challengeResponseParameter;
    }

    /**
     * Set the challenge verifiers.
     * 
     * @param verifiers
     *            for verifying the challenge
     * @param <T>
     *            Principal
     */
    public <T extends Principal> void setChallengeVerifiers(@Nonnull Map<T, ChallengeVerifier> verifiers) {
        log.trace("Entering");
        this.challengeVerifiers = new HashMap<Principal, ChallengeVerifier>();
        for (Map.Entry<T, ChallengeVerifier> entry : verifiers.entrySet()) {
            this.challengeVerifiers.put(entry.getKey(), entry.getValue());
        }
        log.trace("Leaving");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        shibbolethContext = authenticationContext.getSubcontext(ShibbolethSpAuthenticationContext.class);
        if (shibbolethContext == null) {
            log.debug("{} Could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        stepUpContext = authenticationContext.getSubcontext(StepUpContext.class);
        if (stepUpContext == null) {
            log.debug("{} Could not get stepup context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");

        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        final String challengeResponse = request.getParameter(challengeResponseParameter);
        if (challengeResponse == null || challengeResponse.isEmpty()) {
            log.debug("User did not present response to challenge", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
            log.trace("Leaving");
            return;
        }
        // Resolve challenge sender
        ChallengeVerifier challengeVerifier = null;
        if (challengeVerifiers != null) {
            challengeVerifier = challengeVerifiers.get(findKey(shibbolethContext.getInitialRequestedContext(),
                    challengeVerifiers.keySet()));
        }
        if (challengeVerifier == null) {
            log.debug("no challenge sender defined for requested context");
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
            log.trace("Leaving");
            return;
        }
        log.debug("User challenge response was " + challengeResponse);
        if (!challengeVerifier.verify(stepUpContext.getChallenge(), challengeResponse, stepUpContext.getTarget())) {
            log.debug("User presented wrong response to  challenge", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
            log.trace("Leaving");
            return;
        }
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");

    }

    /**
     * Method tries to locate requested method from the configured set of
     * methods.
     * 
     * @param requestedCtx
     *            contains the requested methods
     * @param configuredCtxs
     *            configured requested methods
     * @return null or the matching item in the set
     */
    private Principal findKey(List<Principal> requestedPrincipals, Set<Principal> configuredCtxs) {
        log.trace("Entering");
        if (configuredCtxs == null || requestedPrincipals == null) {
            log.trace("Leaving");
            return null;
        }
        for (Principal requestedPrincipal : requestedPrincipals) {
            if (configuredCtxs.contains(requestedPrincipal)) {
                log.trace("Leaving");
                return requestedPrincipal;
            }
        }
        log.trace("Leaving");
        return null;

    }

}
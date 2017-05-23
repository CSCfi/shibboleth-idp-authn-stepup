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
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.FailureLimitReachedException;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that verifies user challenge response.
 * 
 */
@SuppressWarnings("rawtypes")
public class VerifyPasswordFromFormRequest extends AbstractStepUpMethodAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(VerifyPasswordFromFormRequest.class);

    /** Challenge response parameter. */
    private String challengeResponseParameter = "j_challengeResponse";
    /** Challenge response. */
    private String challengeResponse;

    /**
     * Sets the parameter the response is read from.
     * 
     * @param parameter
     *            name for response
     */
    public void setChallengeResponseParameter(@Nonnull @NotEmpty String parameter) {
        this.challengeResponseParameter = parameter;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        }
        if (getStepUpMethodCtx().getStepUpAccount() == null) {
            log.debug("{} there is no chosen stepup account for user", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
            return false;
        }
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return false;
        }
        challengeResponse = request.getParameter(challengeResponseParameter);
        if (challengeResponse == null) {
            log.debug("{} user did not present response to challenge", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
            return false;
        }
        challengeResponse = challengeResponse.trim();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.debug("{} user challenge response was {}", getLogPrefix(), challengeResponse);
        try {
            if (!getStepUpMethodCtx().getStepUpAccount().verifyResponse(challengeResponse)) {
                log.debug("{} user presented wrong response to  challenge", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
                Thread.sleep(3000);
                return;
            }
        } catch (FailureLimitReachedException e) {
            log.debug("{} user response failed too many times", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_RESPONSE_LIMIT);
            return;
        } catch (Exception e) {
            log.debug("{} user response evaluation failed", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
    }

}
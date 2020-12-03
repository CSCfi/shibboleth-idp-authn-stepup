/*
 * The MIT License
 * Copyright (c) 2015-2020 CSC - IT Center for Science, http://www.csc.fi
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

package fi.csc.idp.stepup.api.profile.impl;

import javax.annotation.Nonnull;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.TokenValidator;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * This action validated access token.
 */
public class ValidateToken extends AbstractApiAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ValidateToken.class);

    /** Token validator. */
    private TokenValidator tokenValidator;

    /** Whether we are validating token for self-service action. */
    private boolean selfServiceAction;

    /**
     * Set whether we are validating token for self-service action.
     * 
     * @param readAction Whether we are validating token for self-service action
     */
    public void setSelfServiceAction(boolean selfServiceAction) {
        this.selfServiceAction = selfServiceAction;
    }

    /**
     * Set token validator.
     * 
     * @param tokenValidator
     */
    public void setTokenValidator(TokenValidator tokenValidator) {
        this.tokenValidator = Constraint.isNotNull(tokenValidator, "TokenValidator must not be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (tokenValidator == null) {
            log.error("{} TokenValidator must not be null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        if (!tokenValidator.validate(getRequest().getToken(), getRequest().getUserId(), selfServiceAction)) {
            // Form response
            response.put("userid", getRequest().getUserId());
            response.put("error", "token not validated to manage userid");
            getCtx().setResponse(response);
            log.error("{} Failed validation token {} user {} combination", getLogPrefix(), getRequest().getToken(),
                    getRequest().getUserId());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_FORBIDDEN);
        }
    }
}

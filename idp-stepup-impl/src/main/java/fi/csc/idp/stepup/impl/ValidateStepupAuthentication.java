/*
 * The MIT License
 * Copyright (c) 2019 CSC - IT Center for Science, http://www.csc.fi
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
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;

/** This action populates principal with the subject value of the request. */
@SuppressWarnings("rawtypes")
public class ValidateStepupAuthentication extends AbstractValidationAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ValidateStepupAuthentication.class);

    /** Subject for whom the request is made. */
    @Nullable
    private String requestSubject;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            log.error("{} pre-execute failed ", getLogPrefix());
            return false;
        }
        StepUpMethodContext stepUpMethodContext =
                (StepUpMethodContext) authenticationContext.getSubcontext(StepUpMethodContext.class, false);
        if (stepUpMethodContext == null) {
            log.error("{} StepUpMethodContext not available under AuthenticationContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_AUTHN_CTX);
            return false;
        }
        requestSubject = stepUpMethodContext.getSubject();
        if (requestSubject == null) {
            log.error("{} no subject in request, unable to continue", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_NO_USER);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        buildAuthenticationResult(profileRequestContext, authenticationContext);
        return;
    }

    @Override
    protected Subject populateSubject(Subject subject) {
        subject.getPrincipals().add(new UsernamePrincipal(requestSubject));
        return subject;
    }

}

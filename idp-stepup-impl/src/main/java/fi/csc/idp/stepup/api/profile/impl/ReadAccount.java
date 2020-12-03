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
import fi.csc.idp.stepup.api.StepUpApiContext;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * This action reads accounts of the user and stores them to
 * {@link StepUpApiContext}.
 */
public class ReadAccount extends AbstractApiAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ReadAccount.class);

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (getRequest().getUserId() == null || getRequest().getUserId().isEmpty()) {
            log.error("{} No userid in request", getLogPrefix());
            response.put("error", "No userid in request");
            getCtx().setResponse(response);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_NO_USER);
            return;
        }
        try {
            getCtx().setAccount(
                    getCtx().getStorage().getAccount(getRequest().getUserId(), getCtx().getAccount().getClass()));
            log.debug("{} Located {} user accounts for user {}", getLogPrefix(),
                    getCtx().getAccount() == null ? 0 : getRequest().getUserId());
        } catch (Exception e) {
            log.error("{} Failed reading user accounts", getLogPrefix(), e);
            response.put("userid", getRequest().getUserId());
            response.put("error", "Internal error");
            getCtx().setResponse(response);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
        }
    }

}

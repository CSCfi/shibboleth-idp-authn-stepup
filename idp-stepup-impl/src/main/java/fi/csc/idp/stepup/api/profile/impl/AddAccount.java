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
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * Actions adds account and sets the response status.
 */
public class AddAccount extends AbstractApiAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AddAccount.class);

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (getCtx().getAccount() != null) {
            if (!getRequest().getForceUpdate()) {
                // Form response
                response.put("userid", getRequest().getUserId());
                response.put("error", "Cannot add more accounts to user");
                getCtx().setResponse(response);
                log.error("{} Cannot add more accounts to user {}", getLogPrefix(), getRequest().getUserId());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_FORBIDDEN);
                return;
            } else {
                try {
                    getCtx().getStorage().remove(getCtx().getAccount(), getRequest().getUserId());
                } catch (Exception e) {
                    log.error("{} Exception occurred while removing account {}", getLogPrefix(), e.getMessage());
                    response.put("userid", getRequest().getUserId());
                    response.put("error", "Internal error");
                    getCtx().setResponse(response);
                    ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
                    return;
                }
            }
        }
        StepUpAccount account;
        try {
            account = getCtx().getAccountPrototype().getClass().getDeclaredConstructor().newInstance();
            if (getRequest().getValue() != null && !getRequest().getValue().isEmpty()) {
                account.setTarget(getRequest().getValue());
            }
            getCtx().getStorage().add(account, getRequest().getUserId());
            getCtx().setAccount(account);
        } catch (Exception e) {
            log.error("{} Exception occurred while adding account {}", getLogPrefix(), e.getMessage());
            response.put("userid", getRequest().getUserId());
            response.put("error", "Internal error");
            getCtx().setResponse(response);
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        // Form response
        response.put("userid", getRequest().getUserId());
        response.put("value", account.getTarget());
        getCtx().setResponse(response);
    }
}

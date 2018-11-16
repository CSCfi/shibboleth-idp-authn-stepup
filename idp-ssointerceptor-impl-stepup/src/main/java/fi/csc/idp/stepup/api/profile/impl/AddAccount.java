/*
 * GÉANT BSD Software License
 *
 * Copyright (c) 2017 - 2020, GÉANT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the GÉANT nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fi.csc.idp.stepup.api.profile.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * Actions adds account if not pre-existing and sets the response status.
 */
@SuppressWarnings("rawtypes")
public class AddAccount extends AbstractApiAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AddAccount.class);

    Map<String, Object> response = new HashMap<String, Object>();

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (getCtx().getAccounts() != null && getCtx().getAccounts().size() > 0) {
            if (getCtx().getAccounts().size() >= getRequest().getMaxAccounts()) {
                if (!getRequest().getForceUpdate()) {
                    // Form response
                    log.debug("{} Cannot add more accounts to user {}", getLogPrefix(), getRequest().getUserId());
                    response.put("userid", getRequest().getUserId());
                    response.put("error", "Cannot add more accounts to user");
                    response.put("success", false);
                    getCtx().setResponse(response);
                    return;
                } else {
                    try {
                        getCtx().getStorage().remove(getCtx().getAccounts().get(0), getRequest().getUserId());
                    } catch (Exception e) {
                        log.error("{} Exception occurred while removing account {}", getLogPrefix(), e.getMessage());
                        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
                        return;
                    }
                }
            }
        }
        StepUpAccount account = getCtx().getAccount();
        if (getRequest().getValue() != null && !getRequest().getValue().isEmpty()) {
            account.setTarget(getRequest().getValue());
        }
        try {
            getCtx().getStorage().add(account, getRequest().getUserId());
        } catch (Exception e) {
            log.error("{} Exception occurred while adding account {}", getLogPrefix(), e.getMessage());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        // Form response
        response.put("userid", getRequest().getUserId());
        response.put("value", account.getTarget());
        response.put("success", true);
        getCtx().setResponse(response);
    }

}

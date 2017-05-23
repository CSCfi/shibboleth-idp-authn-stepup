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
import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;

/**
 * An action that updates a account. 
 * 
 */
@SuppressWarnings("rawtypes")
public class UpdateAccount extends AbstractStepUpMethodAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(UpdateAccount.class);

    /** update parameter. */
    private String updateParameter = "_eventId_update";

    /** name parameter. */
    private String nameParameter = "j_name";
   
    /**
     * Sets the parameter the response is read from.
     * 
     * @param parameter
     *            name for response
     */
    public void setUpdateParameter(@Nonnull @NotEmpty String parameter) {
        this.updateParameter = parameter;
    }

    /**
     * Sets the parameter the name is read from.
     * 
     * @param parameter
     *            name for name
     */
    public void setNameParameter(@Nonnull @NotEmpty String parameter) {
        this.nameParameter = parameter;
    }

    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        final String updateValue = request.getParameter(updateParameter);
        if (updateValue == null) {
            log.debug("{} no update value found", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
            return;
        }
        String[] updateCommand = updateValue.split(":");
        if (updateCommand.length != 3) {
            log.debug("{} the command should have 3 parts", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        String method = updateCommand[0];
        if (method == null || method.isEmpty()) {
            log.debug("{} method cannot be empty or null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        long id = -1;
        try {
            id = Long.parseLong(updateCommand[1]);
        } catch (NumberFormatException e) {
            log.debug("{} the commands second part shoud be interpretable as int", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);

        }
        String command = updateCommand[2];
        if (command == null || command.isEmpty()) {
            log.debug("{} command cannot be empty or null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        // locating account
        StepUpAccount updateAccount = null;
        StepUpMethod updateMethod = null;
        for (StepUpMethod suMethod : getStepUpMethodCtx().getStepUpMethods().keySet()) {
            log.debug("{} comparing method {} to {}", getLogPrefix(), method, suMethod.getName());
            if (method.equals(suMethod.getName())) {
                log.debug("{} located target method {}", getLogPrefix(), method);
                updateMethod = suMethod;
                if (id >= 0) {
                    for (StepUpAccount account : suMethod.getAccounts()) {
                        log.debug("{} comparing account id {} to {}", getLogPrefix(), id, account.getId());
                        if (account.getId() == id) {
                            log.debug("{} located target account {}", getLogPrefix(), id);
                            updateAccount = account;
                            break;
                        }
                    }
                }
                break;
            }
        }
        try {
            log.debug("{} running command {}", getLogPrefix(), command);
            accountCommand(command, updateAccount, updateMethod, request);
        } catch (Exception e) {
            log.debug("{} unexpected exception occurred", getLogPrefix());
            log.error(e.getMessage());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            return;
        }
        log.debug("{} update value to be interpreted is {}", getLogPrefix(), updateValue);
    }

    // Checkstyle: CyclomaticComplexity OFF
    /**
     * Method performs account operations.
     * 
     * @param command
     *            StepUpAccount or StepUpMethod command
     * @param account
     *            the operation is targeting
     * @param method
     *            the operation is targeting
     * @param request
     *            for reading user input
     * @throws Exception
     *             if something unexpected occurs
     */
    private void accountCommand(String command, StepUpAccount account, StepUpMethod method, HttpServletRequest request)
            throws Exception {

        if (method == null) {
            throw new Exception("operations require method");
        }
        if (account == null && !command.equals(StepUpMethod.ADD_ACCOUNT)) {
            throw new Exception("Account operations requires account");
        }
        switch (command) {
        case StepUpAccount.DISABLE:
            account.setEnabled(false);
            method.updateAccount(account);
            break;
        case StepUpAccount.ENABLE:
            account.setEnabled(true);
            method.updateAccount(account);
            break;
        case StepUpAccount.SET_EDITABLE:
            account.setEditable(true);
            method.updateAccount(account);
            break;
        case StepUpAccount.SET_NOT_EDITABLE:
            account.setEditable(false);
            method.updateAccount(account);
            break;
        case StepUpAccount.SET_NAME:
            final String name = request.getParameter(nameParameter);
            account.setName(name);
            method.updateAccount(account);
            break;
        case StepUpMethod.ADD_ACCOUNT:
            method.addAccount();
            break;
        case StepUpMethod.REMOVE_ACCOUNT:
            method.removeAccount(account);
            break;
        default:
            throw new Exception("Unsupported command");
        }

    }
    // Checkstyle: CyclomaticComplexity ON
}
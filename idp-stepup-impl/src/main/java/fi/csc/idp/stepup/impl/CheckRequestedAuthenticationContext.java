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
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that checks if step up authentication is requested.
 * 
 */

@SuppressWarnings("rawtypes")
public class CheckRequestedAuthenticationContext extends AbstractShibSPAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(CheckRequestedAuthenticationContext.class);

    /** The authentication methods indicating step up. */
    @Nonnull
    private Subject stepupPrincipals;

    /**
     * Sets the list of authentication methods requiring step up.
     * 
     * 
     * @param <T>
     *            a type of principal to add, if not generic
     * @param principals
     *            supported principals to add
     */

    public <T extends Principal> void setStepupMethods(@Nonnull @NonnullElements final Collection<T> principals) {

        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(principals, "Principal collection cannot be null.");
        if (stepupPrincipals == null) {
            stepupPrincipals = new Subject();
        }
        stepupPrincipals.getPrincipals().clear();
        stepupPrincipals.getPrincipals().addAll(Collections2.filter(principals, Predicates.notNull()));

    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        if (getShibSPCtx().getInitialRequestedContext() == null
                || getShibSPCtx().getInitialRequestedContext().isEmpty()
                || !stepupRequested(getShibSPCtx().getInitialRequestedContext(), stepupPrincipals)) {
            log.error("{} AuthnRequest did not contain a RequestedAuthnContext matching any StepUp", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_AUTHNCONTEXT_NOT_STEPUP);
            return;
        }
    }

    /**
     * Method checks if any of the requested authentication methods is listed as
     * step up.
     * 
     * @param requestedPrincipals
     *            requested methods
     * @param stPrincipals
     *            stepup methods
     * @return true if the requested method requires step up.
     */
    private boolean stepupRequested(List<Principal> requestedPrincipals, Subject stPrincipals) {

        for (Principal requestedPrincipal : requestedPrincipals) {
            if (stPrincipals.getPrincipals().contains(requestedPrincipal)) {
                return true;
            }
        }
        return false;
    }

}
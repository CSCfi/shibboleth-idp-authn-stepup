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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.apache.commons.collections.CollectionUtils;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.RelyingPartyContext;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

/**
 * An action that initializes step up methods and accounts. Each of the
 * configured methods are initialized. Among the initialized methods a first
 * suitable account and methods are chosen as defaults based on requested
 * authentication context. If there is no suitable account any of the suitable
 * methods is chosen as default method.
 * 
 */

@SuppressWarnings("rawtypes")
public class InitializeStepUpChallengeContext extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeStepUpChallengeContext.class);

    /** Context to look attributes for. */
    @Nonnull
    private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;

    /** AttributeContext to filter. */
    @Nullable
    private AttributeContext attributeContext;

    /** proxy authentication context. */
    private ShibbolethSpAuthenticationContext shibbolethContext;

    /** StepUp Methods. */
    private Map<StepUpMethod, List<? extends Principal>> stepUpMethods;

    /** Constructor. */
    public InitializeStepUpChallengeContext() {
        log.trace("Entering");
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
        log.trace("Leaving");
    }

    /**
     * Set the stepup method and supported authentication contextes.
     * 
     * @param methods
     *            stepup methods in a map
     */

    public void setStepUpMethods(@Nonnull Map<StepUpMethod, List<? extends Principal>> methods) {
        log.trace("Entering");
        this.stepUpMethods = new LinkedHashMap<StepUpMethod, List<? extends Principal>>();
        for (Map.Entry<StepUpMethod, List<? extends Principal>> entry : methods.entrySet()) {
            this.stepUpMethods.put(entry.getKey(), entry.getValue());
        }
        log.trace("Leaving");
    }

    /**
     * Set the lookup strategy for the {@link AttributeContext}.
     * 
     * @param strategy
     *            lookup strategy
     */

    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        log.trace("Entering");
        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
        log.trace("Leaving");
    }

    // Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null) {
            log.error("{} Unable to locate attribute context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_ATTRIBUTECONTEXT);
            log.trace("Leaving");
            return false;
        }

        shibbolethContext = authenticationContext.getSubcontext(ShibbolethSpAuthenticationContext.class);
        if (shibbolethContext == null) {
            log.debug("{} Could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);
            log.trace("Leaving");
            return false;
        }
        if (stepUpMethods == null) {
            log.debug("{} Bean not configured correctly, step up methods not set", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return false;
        }
        log.trace("Leaving");
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        log.trace("Entering");
        StepUpMethodContext stepUpMethodContext = (StepUpMethodContext) authenticationContext.addSubcontext(
                new StepUpMethodContext(), true);
        // We have all possible methods in a map.
        // Initialize all for this user.. so they can be fetched from context
        // later for instance for maintenance operations.
        // The methods that cannot be initialized are dropped.
        for (Iterator<Entry<StepUpMethod, List<? extends Principal>>> it = stepUpMethods.entrySet().iterator(); it
                .hasNext();) {
            Entry<StepUpMethod, List<? extends Principal>> entry = it.next();
            StepUpMethod stepupMethod = entry.getKey();
            log.debug("Initializing StepUp method and accounts for " + stepupMethod.getName());
            try {
                if (!stepupMethod.initialize(attributeContext)) {
                    log.debug("Not able to initialize method " + stepupMethod.getName()
                            + " removed from available methods");
                    it.remove();
                }
            } catch (Exception e) {
                log.debug("Something unexpected happened", getLogPrefix());
                log.error(e.getMessage());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
                log.trace("Leaving");
                return;
            }
        }
        // Set all available initializable methods to context
        log.debug("Setting " + stepUpMethods.size() + " stepup methods to context");
        stepUpMethodContext.setStepUpMethods(stepUpMethods);

        // Pick any non disabled account as the account to be used
        for (Entry<StepUpMethod, List<? extends Principal>> entry : stepUpMethods.entrySet()) {
            if (CollectionUtils.intersection(entry.getValue(), shibbolethContext.getInitialRequestedContext()).size() > 0) {
                // We set the last iterated method as the method
                log.debug("Setting method " + entry.getKey().getName() + " as default method");
                stepUpMethodContext.setStepUpMethod(entry.getKey());
                // That method has accounts
                try {
                    if (entry.getKey().getAccounts() != null) {
                        for (StepUpAccount account : entry.getKey().getAccounts()) {
                            // and the account is enabled
                            if (account.isEnabled()) {
                                log.debug("Setting a default stepup account");
                                log.debug("Account type is " + entry.getKey().getName());
                                log.debug("Account name is " + (account.getName() == null ? "" : account.getName()));
                                stepUpMethodContext.setStepUpAccount(account);
                                log.trace("Leaving");
                                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Something unexpected happened", getLogPrefix());
                    log.error(e.getMessage());
                    ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
                    log.trace("Leaving");
                    return;
                }

            }
        }
        // No default account automatically chosen
        log.trace("Leaving");
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    // Checkstyle: CyclomaticComplexity ON
}
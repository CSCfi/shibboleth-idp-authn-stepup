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

        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));

    }

    /**
     * Set the stepup method and supported authentication contextes.
     * 
     * @param methods
     *            stepup methods in a map
     */

    public void setStepUpMethods(@Nonnull Map<StepUpMethod, List<? extends Principal>> methods) {

        this.stepUpMethods = new LinkedHashMap<StepUpMethod, List<? extends Principal>>();
        for (Map.Entry<StepUpMethod, List<? extends Principal>> entry : methods.entrySet()) {
            this.stepUpMethods.put(entry.getKey(), entry.getValue());
        }

    }

    /**
     * Set the lookup strategy for the {@link AttributeContext}.
     * 
     * @param strategy
     *            lookup strategy
     */

    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {

        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");

    }

    // Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null) {
            log.error("{} unable to locate attribute context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_ATTRIBUTECONTEXT);

            return false;
        }

        shibbolethContext = authenticationContext.getSubcontext(ShibbolethSpAuthenticationContext.class);
        if (shibbolethContext == null) {
            log.debug("{} could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_SHIBSPCONTEXT);

            return false;
        }
        if (stepUpMethods == null) {
            log.debug("{} bean not configured correctly, step up methods not set", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);

            return false;
        }

        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        StepUpMethodContext stepUpMethodContext = (StepUpMethodContext) authenticationContext.addSubcontext(
                new StepUpMethodContext(), true);
        for (Iterator<Entry<StepUpMethod, List<? extends Principal>>> it = stepUpMethods.entrySet().iterator(); it
                .hasNext();) {
            Entry<StepUpMethod, List<? extends Principal>> entry = it.next();
            StepUpMethod stepupMethod = entry.getKey();
            log.debug("{} initializing StepUp method and accounts for {}", getLogPrefix(), stepupMethod.getName());
            try {
                if (!stepupMethod.initialize(attributeContext)) {
                    log.debug("{} not able to initialize method {} removed from available methods", getLogPrefix(),
                            stepupMethod.getName());
                    it.remove();
                }
            } catch (Exception e) {
                log.error("{} something unexpected happened", getLogPrefix());
                log.error(e.getMessage());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);

                return;
            }
        }
        log.debug("{} setting {} stepup methods to context", getLogPrefix(), stepUpMethods.size());
        stepUpMethodContext.setStepUpMethods(stepUpMethods);
        for (Entry<StepUpMethod, List<? extends Principal>> entry : stepUpMethods.entrySet()) {
            if (CollectionUtils.intersection(entry.getValue(), shibbolethContext.getInitialRequestedContext()).size() > 0) {
                // We set the last iterated method as the method
                log.debug("{} setting method {} as default method", getLogPrefix(), entry.getKey().getName());
                stepUpMethodContext.setStepUpMethod(entry.getKey());
                // That method has accounts
                try {
                    if (entry.getKey().getAccounts() != null) {
                        for (StepUpAccount account : entry.getKey().getAccounts()) {
                            // and the account is enabled
                            if (account.isEnabled()) {
                                log.debug("{} setting a default stepup account", getLogPrefix());
                                log.debug("Account type is {}", entry.getKey().getName());
                                log.debug("Account name is {}", (account.getName() == null ? "" : account.getName()));
                                stepUpMethodContext.setStepUpAccount(account);

                                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("{} something unexpected happened", getLogPrefix());
                    log.error(e.getMessage());
                    ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);

                    return;
                }

            }
        }
        // No default account automatically chosen

        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
    }
    // Checkstyle: CyclomaticComplexity ON
}
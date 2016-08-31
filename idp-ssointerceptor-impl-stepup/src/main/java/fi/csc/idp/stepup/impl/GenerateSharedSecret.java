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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.RelyingPartyContext;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.ChallengeGenerator;
import fi.csc.idp.stepup.api.StepUpContext;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

/**
 * An action that generates a shared secret.The action selects attribute id and
 * challenge generator on the basis of requested
 * authentication context. Attribute value, if defined, is passed to challenge
 * generator. Secret is stored to context.
 * 
 * 
 */

@SuppressWarnings("rawtypes")
public class GenerateSharedSecret extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(GenerateSharedSecret.class);

    /** Context to look attributes for. */
    @Nonnull
    private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;

    /** The attribute ID to look for. */
    @Nullable
    private Map<Principal, String> attributeIds;

    /** AttributeContext to filter. */
    @Nullable
    private AttributeContext attributeContext;

    /** proxy authentication context. */
    private ShibbolethSpAuthenticationContext shibbolethContext;

    /** Challenge Generators. */
    private Map<Principal, ChallengeGenerator> challengeGenerators;


    /** Constructor. */
    public GenerateSharedSecret() {
        log.trace("Entering");
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
        log.trace("Leaving");
    }

   
    /**
     * Set the attribute IDs keyed by requested authentication context.
     * 
     * @param ids
     *            attribute IDs to look for in a map
     * @param <T>
     *            Principal
     */

    public <T extends Principal> void setAttributeIds(@Nonnull Map<T, String> ids) {
        log.trace("Entering");
        this.attributeIds = new HashMap<Principal, String>();
        for (Map.Entry<T, String> entry : ids.entrySet()) {
            this.attributeIds.put(entry.getKey(), entry.getValue());
        }
        log.trace("Leaving");
    }

    /**
     * Set the challenge generators keyed by requested authentication context.
     * 
     * @param generators
     *            implementations of challenge generators in a map
     * @param <T>
     *            Principal
     */
    public <T extends Principal> void setChallengeGenerators(@Nonnull Map<T, ChallengeGenerator> generators) {
        log.trace("Entering");
        this.challengeGenerators = new HashMap<Principal, ChallengeGenerator>();
        for (Map.Entry<T, ChallengeGenerator> entry : generators.entrySet()) {
            this.challengeGenerators.put(entry.getKey(), entry.getValue());
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
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        log.trace("Entering");
        String attributeId = null;
        Principal key = null;
        if (attributeIds != null) {
            key = findKey(shibbolethContext.getInitialRequestedContext(), attributeIds.keySet());
        }
        if (key != null) {
            attributeId = attributeIds.get(key);
        }
        String target = null;
        if (attributeId != null) {
            IdPAttribute attribute = attributeContext.getIdPAttributes().get(attributeId);
            if (attribute == null) {
                log.debug("Attributes do not contain value for " + attributeId, getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
                log.trace("Leaving");
                return;
            }
            for (final IdPAttributeValue value : attribute.getValues()) {
                if (value instanceof StringAttributeValue) {
                    target = ((StringAttributeValue) value).getValue();
                }
            }
            if (target == null) {
                log.debug("Attributes did not contain String value for " + attributeId, getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
                log.trace("Leaving");
                return;
            }
        }
        StepUpContext stepUpContext = (StepUpContext) authenticationContext.addSubcontext(new StepUpContext(), true);
        stepUpContext.setTarget(target);
        ChallengeGenerator challengeGenerator = null;
        if (challengeGenerators != null) {
            challengeGenerator = challengeGenerators.get(findKey(shibbolethContext.getInitialRequestedContext(),
                    challengeGenerators.keySet()));
        }
        if (challengeGenerator == null) {
            log.debug("no challenge generator defined for requested context");
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_GENERATORIMPL);
            log.trace("Leaving");
            return;
        }
        try {
            stepUpContext.setSharedSecret(challengeGenerator.generate(target));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug("Unable to generate shared secret", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");
    }

    /**
     * Method tries to locate requested method from the configured set of
     * methods.
     * 
     * @param requestedPrincipals
     *            contains the requested methods
     * @param configuredCtxs
     *            configured requested methods
     * @return null or the matching item in the set
     */
    private Principal findKey(List<Principal> requestedPrincipals, Set<Principal> configuredCtxs) {
        log.trace("Entering");
        if (configuredCtxs == null || requestedPrincipals == null) {
            log.trace("Leaving");
            return null;
        }
        for (Principal requestedPrincipal : requestedPrincipals) {
            if (configuredCtxs.contains(requestedPrincipal)) {
                log.trace("Leaving");
                return requestedPrincipal;
            }
        }
        log.trace("Leaving");
        return null;

    }

}
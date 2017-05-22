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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import fi.csc.idp.stepup.api.StepUpEventIds;

/**
 * An action that sets the requested authentication context value to match what
 * the provider has given or to a new mapping. This is done only if there is a
 * configuration allowing this.
 * 
 * 
 */

@SuppressWarnings("rawtypes")
public class SetRequestedAuthenticationContext extends AbstractShibSPAction {

    /**
     * Mapping of authentication methods.
     *
     * idp->sp->method->new method
     * 
     * For matching entry a new value is used
     * 
     * */
    private Map<String, Map<String, Map<Principal, Principal>>> authMethodMap;

    /**
     * Mapping of default methods.
     * 
     * idp->sp
     * 
     * For matching entry a value provided by idp is used. If there is match in
     * authMethodMap, default mapping is not used.
     * 
     * 
     * */
    private Map<String, List<String>> defaultValueMap;

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(SetRequestedAuthenticationContext.class);

    /** Lookup strategy function for obtaining {@link AuthnRequest}. */
    @Nonnull
    private Function<ProfileRequestContext, AuthnRequest> authnRequestLookupStrategy;

    /** The request message to read from. */
    @Nullable
    private AuthnRequest authnRequest;

    /** Constructor. */
    public SetRequestedAuthenticationContext() {

        authnRequestLookupStrategy = Functions.compose(new MessageLookup<>(AuthnRequest.class),
                new InboundMessageContextLookup());
    }

    /**
     * Set the idp<->sp pairs for which the original authentication method of
     * idp is used for constructing the assertion to sp. If specific mapping
     * exists the value is not set.
     * 
     * @param map
     *            of idp's pointing to list of sp's
     */
    public void setPassThruuEntityLists(Map<String, List<String>> map) {
        this.defaultValueMap = map;
    }

    /**
     * Sets a mapping from triplet idp,sp,authentication method to a new method.
     * if mapped idp provides a mapped value to mapped sp, the new value
     * provided by this mapping is used for constructing the assertion to sp.
     * 
     * @param map
     *            is mapping from triple idp, sp, method to an new method
     * @param <T>
     *            Principal
     */
    // Checkstyle: CyclomaticComplexity OFF
    public <T extends Principal> void setAuthenticationMethodMapping(@Nonnull Map<String, Map<String, Map<T, T>>> map) {

        if (this.authMethodMap == null) {
            this.authMethodMap = new HashMap<String, Map<String, Map<Principal, Principal>>>();
        }
        for (Map.Entry<String, Map<String, Map<T, T>>> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!this.authMethodMap.containsKey(entry.getKey())) {
                this.authMethodMap.put(entry.getKey(), new HashMap<String, Map<Principal, Principal>>());
            }
            for (Map.Entry<String, Map<T, T>> entry2 : entry.getValue().entrySet()) {
                if (entry2.getValue() == null) {
                    continue;
                }
                if (!this.authMethodMap.get(entry.getKey()).containsKey(entry2)) {
                    this.authMethodMap.get(entry.getKey()).put(entry2.getKey(), new HashMap<Principal, Principal>());
                }
                for (Map.Entry<T, T> entry3 : entry2.getValue().entrySet()) {
                    if (entry3.getValue() == null) {
                        continue;
                    }
                    if (!this.authMethodMap.get(entry.getKey()).get(entry2.getKey()).containsKey(entry3.getKey())) {
                        this.authMethodMap.get(entry.getKey()).get(entry2.getKey())
                                .put(entry3.getKey(), entry3.getValue());
                    }

                }
            }
        }

    }

    // Checkstyle: CyclomaticComplexity ON

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        RelyingPartyContext rpCtx = profileRequestContext.getSubcontext(RelyingPartyContext.class, false);
        if (rpCtx == null || rpCtx.getRelyingPartyId() == null) {
            log.error("{} could not get relying party context and sp entity id ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RPCONTEXT);
            return;
        }

        Principal providedMethod = null;
        if (getShibSPCtx().getContextClass() != null) {
            providedMethod = new AuthnContextClassRefPrincipal(getShibSPCtx().getContextClass());
        } else if (getShibSPCtx().getContextDecl() != null) {
            providedMethod = new AuthnContextDeclRefPrincipal(getShibSPCtx().getContextDecl());
        }
        if (providedMethod == null) {
            log.error("{} could not get authentication method ", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_SHIBSPCONTEXT);
            return;
        }

        Principal mappedMethod = getExactMapping(getShibSPCtx().getIdp(), rpCtx.getRelyingPartyId(), providedMethod);
        if (mappedMethod == null) {
            mappedMethod = getDefaultMapping(getShibSPCtx().getIdp(), rpCtx.getRelyingPartyId(), providedMethod);
        }
        if (mappedMethod != null) {
            log.debug("{} setting matching principal to {}", getLogPrefix(), mappedMethod.getName());
            RequestedPrincipalContext reqPrincipalContext = authenticationContext.getSubcontext(
                    RequestedPrincipalContext.class, true);
            reqPrincipalContext.setMatchingPrincipal(mappedMethod);
        }
    }

    /**
     * Method returns the new authentication method value for mapping.
     * 
     * @param idp
     *            is the provider entity id
     * @param sp
     *            is the client sp entity id
     * @param method
     *            is the value provided by provider
     * @return The new mapped value if it exists. If not, null.
     */
    private Principal getExactMapping(String idp, String sp, Principal method) {

        if (idp == null || sp == null || method == null || authMethodMap == null) {
            return null;
        }
        log.debug("{} searching a match for triplet {},{} and {}", getLogPrefix(), idp, sp, method.getName());
        if (authMethodMap.containsKey(idp) && authMethodMap.get(idp) != null && authMethodMap.get(idp).containsKey(sp)
                && authMethodMap.get(idp).get(sp) != null && authMethodMap.get(idp).get(sp).containsKey(method)) {
            return authMethodMap.get(idp).get(sp).get(method);
        }
        return null;
    }

    /**
     * If there is a mapping from idp to sp, the value of method is returned.
     * 
     * @param idp
     *            is the provider entity id
     * @param sp
     *            is the client sp entity id
     * @param method
     *            is the method value
     * @return method if there is a mapping, otherwise a null.
     */
    private Principal getDefaultMapping(String idp, String sp, Principal method) {

        if (idp == null || sp == null || method == null || defaultValueMap == null) {
            return null;
        }
        log.debug("{} searching a match for pair {} and {}, provided method is {}", getLogPrefix(), idp, sp,
                method.getName());
        if (defaultValueMap.containsKey(idp) && defaultValueMap.get(idp) != null
                && defaultValueMap.get(idp).contains(sp)) {
            return method;
        }
        return null;
    }

}
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ACR;

/**
 * 
 * 
 * Build shibboleth contexts based on oidc authentication request. Intention is
 * to direct execution to authentication flow after populating mapped
 * attributes. The authentication flow used assumes attributes are prepopulated.
 * 
 */
public class BuildShibbolethContexts implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(BuildShibbolethContexts.class);

    /** claim to attribute mapping. */
    private Map<String, String> claimToAttribute;

    /**
     * Set mapping of claims to attributes. claim names are keys for attribute
     * names.
     * 
     * @param claimToAttributeMap
     *            map
     */
    public void setClaimToAttribute(Map<String, String> claimToAttributeMap) {
        log.trace("Entering & Leaving");
        this.claimToAttribute = claimToAttributeMap;
    }

    /**
     * Creates a new ProfileRequestContext instance and binds it to
     * SpringRequestContext.
     * 
     * @param springRequestContext
     *            the profilerequestcontext is bind to
     * @return ProfileRequestContext instance
     */
    @SuppressWarnings({ "rawtypes" })
    private ProfileRequestContext createPRC(@Nonnull final RequestContext springRequestContext) {
        log.trace("Entering");
        final ProfileRequestContext profileContext = new ProfileRequestContext();
        springRequestContext.getConversationScope().put(ProfileRequestContext.BINDING_KEY, profileContext);
        log.trace("Leaving");
        return profileContext;
    }

    /**
     * Creates RelyingPartyContext instance and adds it to ProfileRequestContext
     * instance.
     * 
     * @param prc
     *            ProfileRequestContext instance
     * @param req
     *            oidc authentication request
     * @throws ComponentInitializationException
     *             if component is not properly initialized
     */
    private void setRPC(@SuppressWarnings("rawtypes") @Nonnull final ProfileRequestContext prc,
            AuthenticationRequest req) throws ComponentInitializationException {
        log.trace("Entering");
        final RelyingPartyContext rpCtx = prc.getSubcontext(RelyingPartyContext.class, true);
        rpCtx.setVerified(true);
        // we set client id to rp id.
        rpCtx.setRelyingPartyId(req.getClientID().getValue());
        log.trace("Leaving");
    }

    /**
     * Creates AttributeContext, populates it with id token claim values and
     * adds it to RelyingPartyContext. Assumes RelyingPartyContext instance
     * exists already.
     * 
     * @param prc
     *            ProfileRequestContext instance
     * @param oidcCtx
     *            oidc authentication context
     * @return true if context was populated as required.
     * @throws Exception
     *             if fails to build attribute context
     */
    private boolean setAttributeCtx(@SuppressWarnings("rawtypes") @Nonnull final ProfileRequestContext prc,
            OidcStepUpContext oidcCtx) throws Exception {
        log.trace("Entering");
        // check claims and convert them to attributes
        List<IdPAttribute> attributes = new ArrayList<IdPAttribute>();
        for (String key : claimToAttribute.keySet()) {
            if (oidcCtx.getIdToken().getClaims().keySet().contains(key)) {
                String attributeName = claimToAttribute.get(key);
                if (attributeName == null) {
                    // claim is listed but not set as attribute
                    log.warn("claims to attribute map contains null value for key " + key);
                    continue;
                }
                // claim is supported and set as attribute
                List<String> values;
                try {
                    values = oidcCtx.getIdToken().getStringListClaim(key);
                } catch (ParseException e) {
                    values = new ArrayList<String>();
                    values.add(oidcCtx.getIdToken().getStringClaim(key));
                }
                if (values == null || values.size() == 0) {
                    log.warn("claim " + key + " did not contain any values");
                    continue;
                }
                log.debug("Creating attribute " + claimToAttribute.get(key) + " with value(s):");
                IdPAttribute attribute = new IdPAttribute(claimToAttribute.get(key));
                List<StringAttributeValue> stringAttributeValues = new ArrayList<StringAttributeValue>();
                for (String value : values) {
                    log.debug(value);
                    stringAttributeValues.add(new StringAttributeValue(value));
                }
                attribute.setValues(stringAttributeValues);
                attributes.add(attribute);
            } else {
                log.error("required attribute " + key + " not in claims");
                oidcCtx.setErrorCode("invalid_request");
                oidcCtx.setErrorDescription("request does not required claim in id token: " + key);
                log.trace("Leaving");
                return false;
            }
        }
        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        return true;
    }

    /**
     * Creates ShibbolethSpAuthenticationContext instance and adds it to
     * AuthenticationContext instance. Assumes AuthenticationContext instance is
     * valid. Populates ShibbolethSpAuthenticationContext instance with ACR
     * values from oidc authentication request.
     * 
     * @param ctx
     *            AuthenticationContext instance.
     * @param req
     *            oidc authentication request
     */
    private void setShibSPCtx(@Nonnull final AuthenticationContext ctx, AuthenticationRequest req) {
        log.trace("Entering");
        ShibbolethSpAuthenticationContext shibspCtx = (ShibbolethSpAuthenticationContext) ctx.addSubcontext(
                new ShibbolethSpAuthenticationContext(), true);
        List<Principal> requested = new ArrayList<Principal>();
        for (ACR acr : req.getACRValues()) {
            log.debug("Setting acr " + acr + " as requested AuthnContextClassRef");
            requested.add(new AuthnContextClassRefPrincipal(acr.getValue()));
        }
        if (requested.size() > 0) {
            shibspCtx.setInitialRequestedContext(requested);
        }
        log.trace("Leaving");
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Event execute(@Nonnull final RequestContext springRequestContext) throws Exception {
        log.trace("Entering");
        OidcStepUpContext oidcCtx = (OidcStepUpContext) springRequestContext.getConversationScope().get(
                OidcStepUpContext.getContextKey());
        if (oidcCtx == null) {
            log.error("oidc context missing, misconfiguration in flow");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        if (oidcCtx.getIdToken() == null || oidcCtx.getIdToken().getClaims() == null) {
            log.error("id token missing or it has no claims");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        if (oidcCtx.getRequest() == null) {
            log.error("request missing, misconfiguration in flow");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        if (claimToAttribute == null) {
            log.error("claims to attribute map is null, misconfiguration in flow");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        final ProfileRequestContext prc = createPRC(springRequestContext);
        setRPC(prc, oidcCtx.getRequest());
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        if (!setAttributeCtx(prc, oidcCtx)) {
            log.error("unable to set attribute context");
            return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
        }
        setShibSPCtx(ctx, oidcCtx.getRequest());
        log.trace("Leaving");
        return new Event(this, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
    }

}
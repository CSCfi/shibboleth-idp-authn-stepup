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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.minidev.json.JSONObject;
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
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ACR;

/**
 * NOT TO BE USED!! EARLY DRAFT!!
 * 
 * PROCESSES IMPLICIT FLOW REQUEST TO PERFORM MFA. FORMS SHIB CTXs FOR AUTH FLOW
 * TO USE.
 * 
 * DOES NOT CHECK THE REQUEST IS FORMED AS INTENDED ETC.
 * 
 */
public class ProcessOidcStepUpRequest implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessOidcStepUpRequest.class);

    /** redirect uris that are valid per client id. */
    private Map<String, List<String>> redirectUris;

    /** claim to attribute mapping. */
    private Map<String, String> claimToAttribute;

    /**
     * Setter for redirect uris.
     * 
     * @param uris
     *            maps client ids to list of uris
     */
    public void setRedirectUris(Map<String, List<String>> uris) {
        this.redirectUris = uris;
    }

    /**
     * Set mapping of claims to attributes. claim names are keys for attribute
     * names.
     * 
     * @param claimToAttribute
     *            map
     */
    public void setClaimToAttribute(Map<String, String> claimToAttribute) {
        this.claimToAttribute = claimToAttribute;
    }

    /**
     * Verifies the redirect uri is registered to client.
     * 
     * @param req
     *            oidc authentication request.
     * @return true if redirect uri is valid.
     * @throws Exception
     *             if not properly initialized.
     */
    private boolean verifyClientRedirectUri(AuthenticationRequest req) throws Exception {
        log.trace("Entering");
        if (req == null) {
            throw new Exception("Authentication request cannot be null");
        }
        if (redirectUris == null) {
            throw new Exception("redirect uris not set");
        }
        if (req.getClientID() == null) {
            throw new Exception("Client id in authentication request cannot be null");
        }
        if (!redirectUris.containsKey(req.getClientID().getValue())) {
            log.debug("client id " + req.getClientID().getValue() + " is not registered");
            return false;
        }
        for (String uri : redirectUris.get(req.getClientID().getValue())) {
            log.debug("matching to " + uri);
            if (uri.equals(req.getRedirectionURI().toString())) {
                log.debug("redirect uri validated");
                log.trace("Leaving");
                return true;
            }
        }
        log.debug("redirect uri " + req.getRedirectionURI().toString() + " not validated");
        log.trace("Leaving");
        return false;
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
        // sp id ie. client id in this case.
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
     * @param req
     *            oidc authentication request
     * @throws Exception
     *             if SubField is not set
     */
    private void setAttributeCtx(@SuppressWarnings("rawtypes") @Nonnull final ProfileRequestContext prc,
            AuthenticationRequest req) throws Exception {
        log.trace("Entering");
        if (claimToAttribute == null) {
            throw new Exception("request object: claims to attribute map is null");
        }
        // validate request object!
        // TODO: check signature,ts and state of the request object.
        String client_id = (String) req.getRequestObject().getJWTClaimsSet().getClaim("client_id");
        if (client_id == null || !req.getClientID().getValue().equals(client_id)) {
            throw new Exception("request object: client id is mandatory and should match parameter value");
        }
        String responseType = (String) req.getRequestObject().getJWTClaimsSet().getClaim("response_type");
        if (responseType == null || !req.getResponseType().equals(new ResponseType(responseType))) {
            throw new Exception("request object: response type is mandatory and should match parameter value");
        }
        String iss = (String) req.getRequestObject().getJWTClaimsSet().getClaim("iss");
        if (iss == null || !req.getClientID().getValue().equals(iss)) {
            throw new Exception(
                    "request object: signed request object should contain iss claim with client id as value");
        }
        /*
         * MISSING ISSUER VALUE, NEEDS RESTRUCTURING String
         * aud=(String)req.getRequestObject().getJWTClaimsSet().getClaim("aud");
         * if (aud == null || !){ throw new Exception(
         * "request object: signed request object should contain aud claim with op issuer as value"
         * ); }
         */
        // Now we parse id token claims to attributes
        JSONObject claims = (JSONObject) req.getRequestObject().getJWTClaimsSet().getClaim("claims");
        if (claims == null) {
            throw new Exception("request object: signed request object needs to have claims");
        }
        JWTClaimsSet idToken = JWTClaimsSet.parse((JSONObject) claims.get("id_token"));
        if (idToken == null) {
            throw new Exception("request object: signed request object needs to have id token");
        }
        List<IdPAttribute> attributes = new ArrayList<IdPAttribute>();
        for (String key : idToken.getClaims().keySet()) {
            if (claimToAttribute.keySet().contains(key)) {
                String attributeName = claimToAttribute.get(key);
                if (attributeName == null) {
                    log.warn("claims to attribute map contains null value for key " + key);
                    continue;
                }
                List<String> values;
                try{
                    values = idToken.getStringListClaim(key);
                }catch(ParseException e){
                    values = new ArrayList<String>();
                    values.add(idToken.getStringClaim(key));
                }
                if (values == null || values.size() == 0) {
                    log.warn("claim " + key + " did not contain any values");
                    continue;
                }
                log.debug("Creating attribute "+claimToAttribute.get(key)+" with value(s):");
                IdPAttribute attribute = new IdPAttribute(claimToAttribute.get(key));
                List<StringAttributeValue> stringAttributeValues = new ArrayList<StringAttributeValue>();
                for (String value : values) {
                    log.debug(value);
                    stringAttributeValues.add(new StringAttributeValue(value));
                }
                attribute.setValues(stringAttributeValues);
                attributes.add(attribute);
            }
        }
        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        log.trace("Leaving");
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
        ServletExternalContext externalContext = (ServletExternalContext) springRequestContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        // Decode the query string
        AuthenticationRequest req = AuthenticationRequest.parse(request.getQueryString());
        if (!verifyClientRedirectUri(req)) {
            return new Event(this, "error");
        }
        // put it to context
        // TODO:DEFINE PROPER KEY DEF
        springRequestContext.getConversationScope().put("fi.csc.idp.stepup.impl.authenticationRequest", req);
        // TODO: check request parameters!!
        // Set up PRC!
        final ProfileRequestContext prc = createPRC(springRequestContext);
        // Set up RPC!
        setRPC(prc, req);
        // Set up AC!
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        // Set up ATRC!
        setAttributeCtx(prc, req);
        // Set up shibspCtx
        setShibSPCtx(ctx, req);
        log.trace("Leaving");
        return new Event(this, "success");
    }

}
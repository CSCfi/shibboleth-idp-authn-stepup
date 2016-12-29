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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.minidev.json.JSONArray;
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

import fi.csc.idp.stepup.api.OidcStepUpContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ACR;

/**
 * 
 * 
 * PROCESSES IMPLICIT FLOW REQUEST TO PERFORM MFA. FORMS SHIB CTXs FOR AUTH FLOW
 * TO USE.
 * 
 * NOT A GENERIC IMPLEMENTATION OF OIDC PROVIDER! ONE TRICK PONY!
 * 
 */
public class ProcessOidcStepUpRequest implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessOidcStepUpRequest.class);

    /** redirect uris that are valid per client id. */
    private Map<String, List<String>> redirectUris;

    /** jwk set uris that are valid per client id. */
    private Map<String, String> jwkSetUris;

    /** claim to attribute mapping. */
    private Map<String, String> claimToAttribute;

    /** issuer used in response. */
    private String issuer;

    /** context to store and pass information to. */
    OidcStepUpContext oidcCtx;

    /**
     * Set the value for Issuer. Mandatory.
     * 
     * @param iss
     *            value for Issuer
     */
    public void setIssuer(String iss) {
        this.issuer = iss;
    }

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
     * Setter for jwk set uris.
     * 
     * @param uris
     *            maps client ids to jwk set uris
     */
    public void setJwkSetUris(Map<String, String> uris) {
        this.jwkSetUris = uris;
    }

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
     * Parse JWK, RSA public key for signature verification, from stream. This
     * method picks the first available one not checking the key id.
     * 
     * @param is
     *            inputstream containing the key
     * @return RSA publick key as JSON Object. Null if there is no key
     * @throws ParseException
     *             if parsing fails.
     * @throws IOException
     *             if something unexpected happens.
     */
    private JSONObject getProviderRSAJWK(InputStream is) throws ParseException, IOException {
        log.trace("Entering");
        JSONObject json = JSONObjectUtils.parse(IOUtils.readInputStreamToString(is,
                java.nio.charset.Charset.forName("UTF8")));
        // TODO: USE ALSO KEY ID TO IDENTIFY KEY
        JSONArray keyList = (JSONArray) json.get("keys");
        if (keyList == null) {
            log.trace("Leaving");
            return null;
        }
        for (Object key : keyList) {
            JSONObject k = (JSONObject) key;
            if ("sig".equals(k.get("use")) && "RSA".equals(k.get("kty"))) {
                log.debug("verification key " + k.toString());
                log.trace("Leaving");
                return k;
            }
        }
        log.trace("Leaving");
        return null;
    }

    /**
     * Verifies JWT is signed by client.
     * 
     * @param jwt
     *            signed jwt
     * @param clientID
     *            id of the client.
     * @return true if successfully verified, otherwise false
     */
    private boolean verifyJWT(JWT jwt, String clientID) {
        log.trace("Entering");
        // Check jwt is signed jwt
        SignedJWT signedJWT = null;
        try {
            signedJWT = SignedJWT.parse(jwt.serialize());
        } catch (ParseException e) {
            log.error("Error when forming signed JWT " + jwt.toString());
            return false;
        }
        // check we have key
        if (!jwkSetUris.containsKey(clientID)) {
            log.error("No jwk set uri defined for client " + clientID);
            return false;
        }
        URI jwkSetUri;
        try {
            jwkSetUri = new URI(jwkSetUris.get(clientID));
        } catch (URISyntaxException e) {
            log.error("jwk set uri malformed for client " + clientID);
            return false;
        }
        try {
            JSONObject key = getProviderRSAJWK(jwkSetUri.toURL().openStream());
            if (key == null) {
                log.error("jwk not found for " + clientID);
                return false;
            }
            RSAPublicKey providerKey = RSAKey.parse(key).toRSAPublicKey();
            RSASSAVerifier verifier = new RSASSAVerifier(providerKey);
            if (!signedJWT.verify(verifier)) {
                log.error("client " + clientID + " JWT signature verification failed for "
                        + signedJWT.getParsedString());
                log.trace("Leaving");
                return false;
            }
        } catch (ParseException | IOException | JOSEException e) {
            log.error("unable to verify signed jwt " + clientID);
            return false;
        }
        log.debug("jwt signature verified");
        log.trace("Leaving");
        return true;
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
     *             if fails to build attribute context
     */
    private void setAttributeCtx(@SuppressWarnings("rawtypes") @Nonnull final ProfileRequestContext prc,
            AuthenticationRequest req) throws Exception {
        log.trace("Entering");
        if (claimToAttribute == null) {
            log.error("request object: claims to attribute map is null");
            log.trace("Leaving");
            throw new Exception("request object: claims to attribute map is null");
        }
        if (jwkSetUris == null) {
            log.error("request object: jwk set uris map is null");
            log.trace("Leaving");
            throw new Exception("request object: jwk set uris map is null");
        }
        // validate request object!
        if (!verifyJWT(req.getRequestObject(), req.getClientID().getValue())) {
            log.error("request object: signature verify failed");
            log.trace("Leaving");
            throw new Exception("request object: signature verify failed");
        }
        // TODO: check ts and state of the request object.
        
        
        // TODO: check ts and state of the request object.
        String clientID = (String) req.getRequestObject().getJWTClaimsSet().getClaim("client_id");
        if (clientID == null || !req.getClientID().getValue().equals(clientID)) {
            log.error("request object: client id is mandatory and should match parameter value");
            log.trace("Leaving");
            throw new Exception("request object: client id is mandatory and should match parameter value");
        }
        String responseType = (String) req.getRequestObject().getJWTClaimsSet().getClaim("response_type");
        if (responseType == null || !req.getResponseType().equals(new ResponseType(responseType))) {
            log.error("request object: response type is mandatory and should match parameter value");
            log.trace("Leaving");
            throw new Exception("request object: response type is mandatory and should match parameter value");
        }
        String iss = (String) req.getRequestObject().getJWTClaimsSet().getClaim("iss");
        if (iss == null || !req.getClientID().getValue().equals(iss)) {
            log.error("request object: signed request object should contain iss claim with client id as value");
            log.trace("Leaving");
            throw new Exception(
                    "request object: signed request object should contain iss claim with client id as value");
        }
        //TODO: check that there is only aud
        String aud = (String) req.getRequestObject().getJWTClaimsSet().getStringListClaim("aud").get(0);
        if (aud == null || !aud.equals(oidcCtx.getIssuer())) {
            log.error("request object: signed request object should contain aud claim with op issuer as value");
            log.trace("Leaving");
            throw new Exception(
                    "request object: signed request object should contain aud claim with op issuer as value");
        }
        // Now we parse id token claims to attributes
        JSONObject claims = (JSONObject) req.getRequestObject().getJWTClaimsSet().getClaim("claims");
        if (claims == null) {
            log.error("request object: signed request object needs to have claims");
            log.trace("Leaving");
            throw new Exception("request object: signed request object needs to have claims");
        }
        JWTClaimsSet idToken = JWTClaimsSet.parse((JSONObject) claims.get("id_token"));
        if (idToken == null) {
            log.error("request object: signed request object needs to have id token");
            log.trace("Leaving");
            throw new Exception("request object: signed request object needs to have id token");
        }
        // check claims and convert them to attributes
        List<IdPAttribute> attributes = new ArrayList<IdPAttribute>();
        for (String key : idToken.getClaims().keySet()) {
            if (claimToAttribute.keySet().contains(key)) {
                String attributeName = claimToAttribute.get(key);
                if (attributeName == null) {
                    // claim is supported but not set as attribute
                    log.debug("claims to attribute map contains null value for key " + key);
                    continue;
                }
                // claim is supported and set as attribute
                List<String> values;
                try {
                    values = idToken.getStringListClaim(key);
                } catch (ParseException e) {
                    values = new ArrayList<String>();
                    values.add(idToken.getStringClaim(key));
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
                // TODO: claim is not supported
                // set error
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
        oidcCtx = new OidcStepUpContext();
        springRequestContext.getConversationScope().put("fi.csc.idp.stepup.impl.oidcctx", oidcCtx);
        ServletExternalContext externalContext = (ServletExternalContext) springRequestContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        // Decode the query string
        AuthenticationRequest req = AuthenticationRequest.parse(request.getQueryString());
        oidcCtx.setRequest(req);
        oidcCtx.setIssuer(issuer);
        if (!verifyClientRedirectUri(req)) {
            return new Event(this, "error");
        }
        // TODO: check request parameters!!
        // Set up PRC!
        final ProfileRequestContext prc = createPRC(springRequestContext);
        // Set up RPC!
        setRPC(prc, req);
        // Set up AC!
        AuthenticationContext ctx = (AuthenticationContext) prc.addSubcontext(new AuthenticationContext(), true);
        // Set up ATRC!
        try {
            setAttributeCtx(prc, req);
        } catch (Exception e) {
            log.trace("Leaving");
            return new Event(this, "error");
        }
        // Set up shibspCtx
        setShibSPCtx(ctx, req);
        log.trace("Leaving");
        return new Event(this, "success");
    }

}
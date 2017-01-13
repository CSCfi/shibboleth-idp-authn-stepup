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
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

/**
 * Action verifies that the request object is signed and that the signature can
 * be verified. Action also checks request object contains required claims and
 * is not a replayed object. Action requires id token to be in request object.
 * id token is stored to context.
 * 
 */
public class ValidateRequestObjectOfOidcAuthenticationRequest implements org.springframework.webflow.execution.Action {

    /** contains messages already used for verification. */
    private static Map<String, DateTime> usedMessages = new HashMap<String, DateTime>();

    /** lock to access usedMessages. */
    private static Lock msgLock = new ReentrantLock();

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ValidateRequestObjectOfOidcAuthenticationRequest.class);

    /** jwk set uris that are valid per client id. */
    private Map<String, String> jwkSetUris;

    /** demonstration purposes only. */
    private boolean noSignatureVerify;

    /**
     * time window in ms for authentication event to be acceptable. Value is
     * used for defining replay cache time limit.
     */
    private long eventWindow = 300000;

    /**
     * Set the event window in ms for authentication response to be acceptable.
     * 
     * @param window
     *            in ms
     */
    public void setEventWindow(long window) {
        this.eventWindow = window;
    }

    /**
     * incoming request object jwt signature not checked if set true. Only for
     * demonstration purposes.
     * 
     * @param noVerify
     *            true if not checked.
     */
    public void setNoSignatureVerify(boolean noVerify) {
        this.noSignatureVerify = noVerify;
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
     * Cleans old messages.
     */
    private void cleanMessages() {
        log.trace("Entering");
        msgLock.lock();
        if (usedMessages.size() < 100) {
            msgLock.unlock();
            log.trace("Leaving");
            return;
        }
        long current = new Date().getTime();
        for (Iterator<Map.Entry<String, DateTime>> it = usedMessages.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, DateTime> usedMessage = it.next();
            long sent = usedMessage.getValue().toDate().getTime();
            if (current - sent > 2 * eventWindow) {
                log.debug("Removing " + usedMessage.getKey() + " " + usedMessage.getValue()
                        + " from the list of used verification messages");
                it.remove();
            }
        }
        msgLock.unlock();
        log.trace("Leaving");
    }

    /**
     * Parses verification keys from inputstream.
     * 
     * @param is
     *            inputstream containing the key
     * @return RSA public keys as list of JSON Objects.
     * @throws ParseException
     *             if parsing fails.
     * @throws IOException
     *             if something unexpected happens.
     */
    private List<JSONObject> getProviderRSAJWK(InputStream is) throws ParseException, IOException {
        log.trace("Entering");
        List<JSONObject> keys = new ArrayList<JSONObject>();
        JSONObject json = JSONObjectUtils.parse(IOUtils.readInputStreamToString(is,
                java.nio.charset.Charset.forName("UTF8")));
        JSONArray keyList = (JSONArray) json.get("keys");
        if (keyList == null) {
            log.trace("Leaving");
            return null;
        }
        for (Object key : keyList) {
            JSONObject k = (JSONObject) key;
            if ("sig".equals(k.get("use")) && "RSA".equals(k.get("kty"))) {
                log.debug("adding verification key " + k.toString());
                log.trace("Leaving");
                keys.add(k);
            }
        }
        log.trace("Leaving");
        return keys;
    }

    /**
     * Verifies JWT is signed by client.
     * 
     * @param oidcCtx
     *            oidc context
     * @param jwt
     *            signed jwt
     * @param clientID
     *            id of the client.
     * @return true if successfully verified, otherwise false
     */
    private boolean verifyJWT(OidcStepUpContext oidcCtx, JWT jwt, String clientID) {
        log.trace("Entering");
        // Check jwt is signed jwt
        if (this.noSignatureVerify) {
            log.warn("JWT signature not checked, do not use in production");
            return true;
        }
        SignedJWT signedJWT = null;
        try {
            signedJWT = SignedJWT.parse(jwt.serialize());
        } catch (ParseException e) {
            log.error("request does not contain signed request object " + clientID);
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request does not contain signed request object");
            return false;
        }
        // check we have key
        if (!jwkSetUris.containsKey(clientID)) {
            log.error("No jwk set uri defined for client " + clientID);
            oidcCtx.setErrorCode("invalid_client");
            oidcCtx.setErrorDescription("Client has not registered keyset uri");
            return false;
        }
        URI jwkSetUri;
        try {
            jwkSetUri = new URI(jwkSetUris.get(clientID));
        } catch (URISyntaxException e) {
            log.error("Client keyset unreadable " + clientID);
            oidcCtx.setErrorCode("invalid_client");
            oidcCtx.setErrorDescription("Client keyset unreadable");
            return false;
        }
        try {
            List<JSONObject> keys = getProviderRSAJWK(jwkSetUri.toURL().openStream());
            if (keys == null || keys.size() == 0) {
                log.error("Client has no suitable keys in keyset " + clientID);
                oidcCtx.setErrorCode("invalid_client");
                oidcCtx.setErrorDescription("Client has no suitable keys in keyset");
                return false;
            }
            for (JSONObject key : keys) {
                RSAPublicKey providerKey = RSAKey.parse(key).toRSAPublicKey();
                RSASSAVerifier verifier = new RSASSAVerifier(providerKey);
                if (signedJWT.verify(verifier)) {
                    log.trace("Leaving");
                    return true;
                }
            }
            log.error("client " + clientID + " JWT signature verification failed for " + signedJWT.getParsedString());
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object signature verification failed");
            log.trace("Leaving");
            return false;
        } catch (ParseException | IOException | JOSEException e) {
            log.error("unable to verify signed jwt " + clientID);
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("unable to verify signed jwt");
            return false;
        }
    }

    /**
     * Validates request object. Checks object is valid, signed and has id
     * token. id token validity is verified and expected to contain state and
     * iat.
     * 
     * 
     * @param oidcCtx
     *            oidc context
     * @param req
     *            oidc authentication request
     * @return true if the object is valid
     * @throws ParseException
     *             if fails to parse claims
     */
    private boolean validateRequestObject(OidcStepUpContext oidcCtx, AuthenticationRequest req) throws ParseException {
        log.trace("Entering");
        String clientID = (String) req.getRequestObject().getJWTClaimsSet().getClaim("client_id");
        if (clientID == null || !req.getClientID().getValue().equals(clientID)) {
            log.error("request object: client id is mandatory and should match parameter value");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing correct client id");
            log.trace("Leaving");
            return false;
        }
        String responseType = (String) req.getRequestObject().getJWTClaimsSet().getClaim("response_type");
        if (responseType == null || !req.getResponseType().equals(new ResponseType(responseType))) {
            log.error("request object: response type is mandatory and should match parameter value");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing correct response type");
            log.trace("Leaving");
            return false;
        }
        String iss = (String) req.getRequestObject().getJWTClaimsSet().getClaim("iss");
        if (iss == null || !req.getClientID().getValue().equals(iss)) {
            log.error("request object: signed request object should contain iss claim with client id as value");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing iss claim with client id as value");
            log.trace("Leaving");
            return false;
        }
        String aud = (String) req.getRequestObject().getJWTClaimsSet().getStringListClaim("aud").get(0);
        if (aud == null || !aud.equals(oidcCtx.getIssuer())) {
            log.error("request object: signed request object should contain aud claim with op issuer as value");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing aud claim with op issuer as value");
            log.trace("Leaving");
            return false;
        }
        Date iat = req.getRequestObject().getJWTClaimsSet().getDateClaim("iat");
        if (iat == null) {
            log.error("request object: iat is required in request object");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing iat");
            log.trace("Leaving");
            return false;
        }
        // check event window
        long sent = iat.getTime();
        long current = new Date().getTime();
        if (current - sent > eventWindow) {
            log.error("id token too old: " + current + "/" + sent);
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("id token too old");
            log.trace("Leaving");
            return false;
        }
        State state = (State) req.getRequestObject().getJWTClaimsSet().getClaim("state");
        if (state == null) {
            log.error("request object: state is required in request object");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing state");
            log.trace("Leaving");
            return false;
        }
        
        JSONObject claims =(JSONObject) req.getRequestObject().getJWTClaimsSet().getClaim("claims");
        if (claims == null) {
            log.error("request object: signed request object needs to have claims");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing claims");
            log.trace("Leaving");
            return false;
        }
        JWTClaimsSet idToken=null;
        try {
            idToken = JWTClaimsSet.parse((JSONObject) claims.get("id_token"));
        } catch (Exception e) {}
        if (idToken == null) {
            log.error("request object: signed request object needs to have id token");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object not containing idtoken");
            log.trace("Leaving");
            return false;
        }
        // check replay
        msgLock.lock();
        if (usedMessages.containsKey(state.getValue())) {
            msgLock.unlock();
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request object already used");
            log.trace("Leaving");
            return false;
        }
        cleanMessages();
        log.debug("Adding " + state + " " + iat + " to the list of used verification messages");
        usedMessages.put(state.getValue(), new DateTime(iat));
        msgLock.unlock();
        oidcCtx.setIdToken(idToken);
        log.trace("Leaving");
        return true;
        
    }

    @Override
    public Event execute(@Nonnull final RequestContext springRequestContext) {
        log.trace("Entering");
        OidcStepUpContext oidcCtx = (OidcStepUpContext) springRequestContext.getConversationScope().get(
                OidcStepUpContext.getContextKey());
        if (jwkSetUris == null) {
            log.error("jwkset uris are not defined");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        if (oidcCtx == null) {
            log.error("oidc context missing");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        // validate request object!
        if (oidcCtx.getRequest().getRequestObject() == null) {
            log.error("request does not contain request object");
            oidcCtx.setErrorCode("invalid_request");
            oidcCtx.setErrorDescription("request does not contain request object");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
        }
        if (!verifyJWT(oidcCtx, oidcCtx.getRequest().getRequestObject(), oidcCtx.getRequest().getClientID().getValue())) {
            log.error("verify failed");
            // verify is expected to fill reason
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
        }
        try {
            if (!validateRequestObject(oidcCtx, oidcCtx.getRequest())) {
                log.error("validation failed");
                // verify is expected to fill reason
                log.trace("Leaving");
                return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
            }
        } catch (ParseException e) {
            log.error("request object parsing failed");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        return new Event(this, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
    }

}
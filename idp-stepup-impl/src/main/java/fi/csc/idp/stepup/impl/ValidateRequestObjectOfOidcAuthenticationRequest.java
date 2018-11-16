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
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Action verifies that 1) the request object is signed and that the signature
 * can be verified, 2) request object contains required claims and 3) is not a
 * replayed object. Action requires id token to be in request object. As a
 * result verified id token is stored to context.
 * 
 */
@SuppressWarnings("rawtypes")
public class ValidateRequestObjectOfOidcAuthenticationRequest extends AbstractOidcProfileAction {

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

        msgLock.lock();
        if (usedMessages.size() < 100) {
            msgLock.unlock();

            return;
        }
        long current = new Date().getTime();
        for (Iterator<Map.Entry<String, DateTime>> it = usedMessages.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, DateTime> usedMessage = it.next();
            long sent = usedMessage.getValue().toDate().getTime();
            if (current - sent > 2 * eventWindow) {
                log.debug("{} removing {} {} from the list of used verification messages", getLogPrefix(),
                        usedMessage.getKey(), usedMessage.getValue());
                it.remove();
            }
        }
        msgLock.unlock();

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

        List<JSONObject> keys = new ArrayList<JSONObject>();
        JSONObject json = JSONObjectUtils.parse(IOUtils.readInputStreamToString(is,
                java.nio.charset.Charset.forName("UTF8")));
        JSONArray keyList = (JSONArray) json.get("keys");
        if (keyList == null) {
            // not a keyset? If it is is a RSA key we are happy
            JSONObject k = json;
            if ("RSA".equals(k.get("kty"))) {
                log.debug("{} adding verification key {}", getLogPrefix(), k.toString());
                keys.add(k);
            }

            return keys;
        }
        for (Object key : keyList) {
            JSONObject k = (JSONObject) key;
            // in case of many keys, we pick all RSA signature keys
            if ("sig".equals(k.get("use")) && "RSA".equals(k.get("kty"))) {
                log.debug("{} adding verification key {}", getLogPrefix(), k.toString());

                keys.add(k);
            }
        }

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

        // Check jwt is signed jwt
        if (this.noSignatureVerify) {
            log.warn("JWT signature not checked, do not use in production");
            return true;
        }
        SignedJWT signedJWT = null;
        try {
            signedJWT = SignedJWT.parse(jwt.serialize());
        } catch (ParseException e) {
            log.error("{} request does not contain signed request object {}", getLogPrefix(), clientID);
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request does not contain signed request object");
            return false;
        }
        // check we have key
        if (!jwkSetUris.containsKey(clientID)) {
            log.error("{} no jwk set uri defined for client {}", getLogPrefix(), clientID);
            getOidcCtx().setErrorCode("invalid_client");
            getOidcCtx().setErrorDescription("Client has not registered keyset uri");
            return false;
        }
        URI jwkSetUri;
        try {
            jwkSetUri = new URI(jwkSetUris.get(clientID));
        } catch (URISyntaxException e) {
            log.error("{} client keyset unreadable ", getLogPrefix(), clientID);
            getOidcCtx().setErrorCode("invalid_client");
            getOidcCtx().setErrorDescription("Client keyset unreadable");
            return false;
        }
        try {
            List<JSONObject> keys = getProviderRSAJWK(jwkSetUri.toURL().openStream());
            if (keys == null || keys.size() == 0) {
                log.error("{} client has no suitable keys in keyset {}", getLogPrefix(), clientID);
                getOidcCtx().setErrorCode("invalid_client");
                getOidcCtx().setErrorDescription("Client has no suitable keys in keyset");
                return false;
            }
            for (JSONObject key : keys) {
                RSAPublicKey providerKey = RSAKey.parse(key).toRSAPublicKey();
                RSASSAVerifier verifier = new RSASSAVerifier(providerKey);
                if (signedJWT.verify(verifier)) {

                    return true;
                }
            }
            log.error("{} client {} JWT signature verification failed for {}", getLogPrefix(), clientID,
                    signedJWT.getParsedString());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object signature verification failed");

            return false;
        } catch (ParseException | IOException | JOSEException e) {
            log.error("{} unable to verify signed jwt {}", getLogPrefix(), clientID);
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("unable to verify signed jwt");
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

        String clientID = (String) req.getRequestObject().getJWTClaimsSet().getClaim("client_id");
        if (clientID == null || !req.getClientID().getValue().equals(clientID)) {
            log.error("{} request object: client id is mandatory and should match parameter value", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing correct client id");

            return false;
        }
        String responseType = (String) req.getRequestObject().getJWTClaimsSet().getClaim("response_type");
        if (responseType == null || !req.getResponseType().equals(new ResponseType(responseType))) {
            log.error("{} request object: response type is mandatory and should match parameter value", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing correct response type");

            return false;
        }
        String iss = (String) req.getRequestObject().getJWTClaimsSet().getClaim("iss");
        if (iss == null || !req.getClientID().getValue().equals(iss)) {
            log.error("{} request object: signed request object should contain iss claim with client id as value",
                    getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing iss claim with client id as value");

            return false;
        }
        String aud = (String) req.getRequestObject().getJWTClaimsSet().getStringListClaim("aud").get(0);
        if (aud == null || !aud.equals(getOidcCtx().getIssuer())) {
            log.error("{} request object: signed request object should contain aud claim with op issuer as value",
                    getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing aud claim with op issuer as value");

            return false;
        }
        Date iat = req.getRequestObject().getJWTClaimsSet().getDateClaim("iat");
        if (iat == null) {
            log.error("{} request object: iat is required in request object", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing iat");

            return false;
        }
        // check event window
        long sent = iat.getTime();
        long current = new Date().getTime();
        if (current - sent > eventWindow) {
            log.error("{} id token too old: {}/{}", getLogPrefix(), current, sent);
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("id token too old");

            return false;
        }
        State state = State.parse(req.getRequestObject().getJWTClaimsSet().getStringClaim("state"));
        if (state == null) {
            log.error("{} request object: state is required in request object", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing state");

            return false;
        }

        JSONObject claims = (JSONObject) req.getRequestObject().getJWTClaimsSet().getClaim("claims");
        if (claims == null) {
            log.error("{} request object: signed request object needs to have claims", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing claims");

            return false;
        }
        JWTClaimsSet idToken = null;
        try {
            idToken = JWTClaimsSet.parse((JSONObject) claims.get("id_token"));
        } catch (Exception e) {
        }
        if (idToken == null) {
            log.error("{} request object: signed request object needs to have id token", getLogPrefix());
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object not containing idtoken");

            return false;
        }
        // check replay
        msgLock.lock();
        if (usedMessages.containsKey(state.getValue())) {
            msgLock.unlock();
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request object already used");

            return false;
        }
        cleanMessages();
        log.debug("{} adding {} {} to the list of used verification messages", getLogPrefix(), state, iat);
        usedMessages.put(state.getValue(), new DateTime(iat));
        msgLock.unlock();
        getOidcCtx().setIdToken(idToken);

        return true;

    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed", getLogPrefix());
            return false;
        }
        if (jwkSetUris == null) {
            log.error("{} bean not initialized with jwkSetUris uris", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (getOidcCtx().getRequest().getRequestObject() == null) {
            getOidcCtx().setErrorCode("invalid_request");
            getOidcCtx().setErrorDescription("request does not contain request object");

            log.error("{} request does not contain request object", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
            return;
        }
        if (!verifyJWT(getOidcCtx(), getOidcCtx().getRequest().getRequestObject(), getOidcCtx().getRequest()
                .getClientID().getValue())) {
            log.error("{} verify failed {}:{}", getLogPrefix(), getOidcCtx().getErrorCode(), getOidcCtx()
                    .getErrorDescription());
            ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
            return;
        }
        try {
            if (!validateRequestObject(getOidcCtx(), getOidcCtx().getRequest())) {
                log.error("{} validation failed", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
                return;
            }
        } catch (ParseException e) {
            log.error("{} request object parsing failed", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
        }
    }

}
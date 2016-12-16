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

import java.net.URI;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

/**
 * NOT TO BE USED!! EARLY DRAFT!!
 * 
 * FORMS IMPLICIT FLOW RESPONSE AFTER MFA IS PERFORMED.
 * 
 */

public class RespondOidcStepUpRequest implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(RespondOidcStepUpRequest.class);

    /** private key used for JWT signing. */
    private PrivateKey prvKey;
    /** JWT signing algorithm. */
    private JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
    /** id for the signing key. */
    private String keyID = "id";
    /** issuer used in response. */
    private String issuer;

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
     * Set the private key used for signing. Mandatory.
     * 
     * @param key
     *            signing key
     */
    public void setPrivKey(PrivateKey key) {
        this.prvKey = key;
    }

    /**
     * Set the signing algorithm. Default is JWSAlgorithm.RS256.
     * 
     * @param algorithm
     *            for signing
     */
    public void setJwsAlgorithm(JWSAlgorithm algorithm) {
        this.jwsAlgorithm = algorithm;
    }

    /**
     * Set the id for the signing key. Default is "id".
     * 
     * @param id
     *            signing key identifier
     */
    public void setKeyID(String id) {
        this.keyID = id;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Event execute(@Nonnull final RequestContext springRequestContext) throws Exception {
        log.trace("Entering");
        if (issuer == null) {
            throw new Exception("Issuer cannot be null");
        }
        if (prvKey == null) {
            throw new Exception("Privatekey cannot be null");
        }
        // TODO:DEFINE PROPER KEY DEF
        AuthenticationRequest req = (AuthenticationRequest) springRequestContext.getConversationScope().get(
                "fi.csc.idp.stepup.impl.authenticationRequest");
        final ProfileRequestContext prc = (ProfileRequestContext) springRequestContext.getConversationScope().get(
                ProfileRequestContext.BINDING_KEY);
        if (prc == null) {
            throw new Exception("No ProfileRequestContext");
        }
        final AttributeContext attributeCtx = prc.getSubcontext(RelyingPartyContext.class).getSubcontext(
                AttributeContext.class);
        if (attributeCtx == null) {
            throw new Exception("No AttributeContext");
        }
        // TODO: proper name for sub
        IdPAttribute attribute = attributeCtx.getIdPAttributes().get("sub");
        if (attribute == null || attribute.getValues().size() != 1) {
            throw new Exception("There has to be exactly one value for sub");
        }
        IdPAttributeValue value = (IdPAttributeValue) attribute.getValues().get(0);
        if (!(value instanceof StringAttributeValue)) {
            throw new Exception("sub attribute must be string");
        }
        // Set sub, originally received as login hint parameter
        Subject sub = new Subject(((StringAttributeValue) value).getValue());
        URI redirectURI = req.getRedirectionURI();
        // Generate new authorization code
        AuthorizationCode code = new AuthorizationCode();
        List<Audience> aud = new ArrayList<Audience>();
        // Set the requesting client as audience
        aud.add(new Audience(req.getClientID().getValue()));
        // Set Issuer
        Issuer iss = new Issuer(issuer);
        Date dateNow = new Date();
        // Set correct date, read RFC!
        Date iat = dateNow;
        // TODO: set configurable, exp is now + 3min
        Date exp = new Date(dateNow.getTime() + 3 * 60 * 1000L);
        // Create Token
        IDTokenClaimsSet idToken2 = new IDTokenClaimsSet(iss, sub, aud, exp, iat);
        // TODO: SET ACR AS THE VALUE CHOSEN BY MFA AFTER LIST OF REQUESTED
        // NOW IT IS ONLY THE "SOME"
        idToken2.setACR(req.getACRValues().get(0));
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(jwsAlgorithm).keyID(keyID).build(),
                idToken2.toJWTClaimsSet());
        jwt.sign(new RSASSASigner(prvKey));
        State state = req.getState();
        AuthenticationSuccessResponse resp = new AuthenticationSuccessResponse(redirectURI, code, jwt, null, state,
                null, null);
        log.debug("constructed response:" + resp.toURI());
        springRequestContext.getFlowScope().put("redirect", resp.toURI().toString());
        log.trace("Leaving");
        return new Event(this, "success");

    }

}
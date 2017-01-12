package fi.csc.idp.stepup.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

public class TestValidateRequestObjectOfOidcAuthenticationRequest {

    private ValidateRequestObjectOfOidcAuthenticationRequest action;

    protected RequestContext src;

    private OidcStepUpContext oidcCtx;

    void initOidcCtx() {
        oidcCtx = new OidcStepUpContext();
        src.getConversationScope().put(OidcStepUpContext.getContextKey(), oidcCtx);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        action = new ValidateRequestObjectOfOidcAuthenticationRequest();

    }

    /**
     * Test that action copes with no keysets.
     */
    @Test
    public void testNoKeySets() {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /**
     * Test that action copes with no oidc ctx.
     */
    @Test
    public void testNoCtx() {
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    private AuthenticationRequest getAuthReq1() throws URISyntaxException {
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = new ResponseType(ResponseType.Value.CODE);
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI).build();
        return req;
    }

    private AuthenticationRequest getAuthReq2() throws URISyntaxException {
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = new ResponseType(ResponseType.Value.CODE);
        JWT requestObject = new PlainJWT(new PlainHeader(), new JWTClaimsSet.Builder().build());
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI).requestObject(
                requestObject).build();
        return req;
    }

    private AuthenticationRequest getAuthReq3() throws URISyntaxException, JOSEException, NoSuchAlgorithmException {
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = new ResponseType(ResponseType.Value.CODE);
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
        String keyID = "id";
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().build();
        JWT requestObject = new SignedJWT(new JWSHeader.Builder(jwsAlgorithm).keyID(keyID).build(), claimsRequest);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey signPrvKey = pair.getPrivate();
        ((SignedJWT) requestObject).sign((JWSSigner) new RSASSASigner(signPrvKey));
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI).requestObject(
                requestObject).build();
        return req;
    }

    /**
     * Test that action copes with no oidc ctx.
     */
    @Test
    public void testNoRequestObject() throws Exception {
        initOidcCtx();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(getAuthReq1());
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request does not contain request object");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /**
     * Test that action copes with unsigned request object.
     */

    @Test
    public void testNotSignedRequestObject() throws Exception {
        initOidcCtx();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(getAuthReq2());
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request does not contain signed request object");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /**
     * Test that action copes with client not having key registered.
     */

    @Test
    public void testNoClientRegisteredKey() throws Exception {
        initOidcCtx();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("fooNOT", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(getAuthReq3());
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_client");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "Client has not registered keyset uri");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

}

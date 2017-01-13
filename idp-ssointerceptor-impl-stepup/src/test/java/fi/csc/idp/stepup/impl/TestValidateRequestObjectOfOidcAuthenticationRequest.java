package fi.csc.idp.stepup.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

public class TestValidateRequestObjectOfOidcAuthenticationRequest {

    private ValidateRequestObjectOfOidcAuthenticationRequest action;

    protected RequestContext src;

    private OidcStepUpContext oidcCtx;

    private Nonce nonce = new Nonce();
    private State state = new State();
    JSONObject idToken;
    JSONObject claims;
    Calendar calendar;

    void baseInit() {
        idToken = new JSONObject();
        claims = new JSONObject();
        claims.put("id_token", idToken);
        oidcCtx = new OidcStepUpContext();
        action.setEventWindow(11 * 60 * 1000);
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -10);
        oidcCtx.setIssuer("opid");
        src.getConversationScope().put(OidcStepUpContext.getContextKey(), oidcCtx);
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
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

    private AuthenticationRequest buildAuthReq1() throws URISyntaxException {
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = new ResponseType(ResponseType.Value.CODE);
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI).build();
        return req;
    }

    private AuthenticationRequest buildAuthReq2() throws URISyntaxException {
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = new ResponseType(ResponseType.Value.CODE);
        JWT requestObject = new PlainJWT(new PlainHeader(), new JWTClaimsSet.Builder().build());
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI).requestObject(
                requestObject).build();
        return req;
    }

    private AuthenticationRequest buildAuthReq3(JWTClaimsSet claimsRequest) throws URISyntaxException, JOSEException,
            NoSuchAlgorithmException, ParseException {

        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = ResponseType.parse("id_token");
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
        String keyID = "id";
        if (claimsRequest == null) {
            claimsRequest = new JWTClaimsSet.Builder().build();
        }
        JWT requestObject = new SignedJWT(new JWSHeader.Builder(jwsAlgorithm).keyID(keyID).build(), claimsRequest);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey signPrvKey = pair.getPrivate();
        ((SignedJWT) requestObject).sign((JWSSigner) new RSASSASigner(signPrvKey));
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI)
                .requestObject(requestObject).nonce(nonce).build();
        return req;
    }

    /**
     * Test that action copes with no oidc ctx.
     */
    @Test
    public void testNoRequestObject() throws Exception {
        baseInit();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(buildAuthReq1());
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
        baseInit();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("foo", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(buildAuthReq2());
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
        baseInit();
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("fooNOT", "http://bar.foo");
        action.setJwkSetUris(uris);
        oidcCtx.setRequest(buildAuthReq3(null));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_client");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "Client has not registered keyset uri");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    // use NOVERIRIFY to do last tests..then try getting the key and test that

    /*
     * client id mismatch in req obj
     */
    @Test
    public void testRequestObjectClientIdMismatch() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "fooNOT").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing correct client id");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /*
     * response type mismatch in req obj
     */
    @Test
    public void testRequestObjectResponseTypeMismatch() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "code").claim("iss", "foo")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing correct response type");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /*
     * issuer mismatch in req obj
     */
    @Test
    public void testRequestObjectIssuerMismatch() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "fooNOT")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(),
                "request object not containing iss claim with client id as value");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /*
     * op must be the audience in req obj
     */
    @Test
    public void testRequestObjectAudienceMismatch() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo")
                .claim("claims", claims).audience("opidNOT").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(),
                "request object not containing aud claim with op issuer as value");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /*
     * test coping with missing iat section
     */
    @Test
    public void testRequestObjectIdMissingIat() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .claim("response_type", "id_token").claim("iss", "foo").claim("claims", claims).audience("opid")
                .build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing iat");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    /*
     * test coping with old iat
     */
    @Test
    public void testRequestObjectIdOldIat() throws Exception {
        baseInit();
        action.setEventWindow(9 * 60 * 1000);
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "id token too old");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    @Test
    public void testRequestObjectIdNoState() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").issueTime(calendar.getTime())
                .claim("response_type", "id_token").claim("iss", "foo").claim("claims", claims).audience("opid")
                .build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing state");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    @Test
    public void testRequestObjectNoClaims() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo").audience("opid")
                .build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing claims");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    @Test
    public void testRequestObjectNoIdToken() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        claims.remove("id_token");
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object not containing idtoken");
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

    @Test
    public void testRequestObjectReplay() throws Exception {
        baseInit();
        action.setNoSignatureVerify(true);
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo").claim("state", state.getValue())
                .issueTime(calendar.getTime()).claim("response_type", "id_token").claim("iss", "foo")
                .claim("claims", claims).audience("opid").build();
        oidcCtx.setRequest(buildAuthReq3(claimsRequest));
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
        final Event event2 = action.execute(src);
        Assert.assertEquals(oidcCtx.getErrorCode(), "invalid_request");
        Assert.assertEquals(oidcCtx.getErrorDescription(), "request object already used");
        ActionTestingSupport.assertEvent(event2, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

}

package fi.csc.idp.stepup.impl;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

public class TestBuildShibbolethContexts {

    private BuildShibbolethContexts action;

    protected RequestContext src;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        action = new BuildShibbolethContexts();
        Map<String, String> claimToAttributeMap = new HashMap<String, String>();
        claimToAttributeMap.put("eppn", "eppn");
        claimToAttributeMap.put("mobile", "mobile");
        action.setClaimToAttribute(claimToAttributeMap);
        OidcStepUpContext oidcCtx = new OidcStepUpContext();
        src.getConversationScope().put(OidcStepUpContext.getContextKey(), oidcCtx);
        ClientID clientID = new ClientID("foo");
        URI redirectURI = new URI("http://bar");
        Scope scope = new Scope("openid");
        ResponseType rt = ResponseType.parse("id_token");
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
        String keyID = "id";
        State state = new State();
        Nonce nonce = new Nonce();
        JSONObject claims = new JSONObject();
        JSONObject idToken = new JSONObject();
        idToken.put("eppn", "eppnvalue");
        idToken.put("mobile", "mobilevalue");
        claims.put("id_token", idToken);
        Calendar calendar = Calendar.getInstance();
        JWTClaimsSet claimsRequest = new JWTClaimsSet.Builder().claim("client_id", "foo")
                .claim("state", state.getValue()).issueTime(calendar.getTime()).claim("response_type", "id_token")
                .claim("iss", "foo").claim("acr", "foo").claim("claims", claims).audience("opid").build();
        JWT requestObject = new SignedJWT(new JWSHeader.Builder(jwsAlgorithm).keyID(keyID).build(), claimsRequest);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey signPrvKey = pair.getPrivate();
        ((SignedJWT) requestObject).sign((JWSSigner) new RSASSASigner(signPrvKey));
        AuthenticationRequest req = new AuthenticationRequest.Builder(rt, scope, clientID, redirectURI)
                .requestObject(requestObject).nonce(nonce).build();
        oidcCtx.setRequest(req);
        oidcCtx.setIdToken(JWTClaimsSet.parse(idToken));
    }

    /**
     * Test that action is able to build shib contexts
     * 
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
    }

    /**
     * Test that action copes with no OidcStepUpContext set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoOidcCtx() throws Exception {
        src.getConversationScope().put(OidcStepUpContext.getContextKey(), null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /**
     * Test that action copes with no idtoken in OidcStepUpContext set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoIdTokenInOidcCtx() throws Exception {
        ((OidcStepUpContext) src.getConversationScope().get(OidcStepUpContext.getContextKey())).setIdToken(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /**
     * Test that action copes with no request in OidcStepUpContext set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoRequestInOidcCtx() throws Exception {
        ((OidcStepUpContext) src.getConversationScope().get(OidcStepUpContext.getContextKey())).setRequest(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /**
     * Test that action copes with no claims map set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoClaimsMap() throws Exception {
        action.setClaimToAttribute(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /**
     * Test that action copes with no required claim found.
     * 
     * @throws Exception
     */
    @Test
    public void testRequiredClaimsNotFound() throws Exception {
        Map<String, String> claimToAttributeMap = new HashMap<String, String>();
        claimToAttributeMap.put("eppn_notfound", "eppn");
        claimToAttributeMap.put("mobile", "mobile");
        action.setClaimToAttribute(claimToAttributeMap);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

}

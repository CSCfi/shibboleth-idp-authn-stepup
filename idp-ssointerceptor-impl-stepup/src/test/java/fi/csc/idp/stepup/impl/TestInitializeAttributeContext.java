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
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
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

public class TestInitializeAttributeContext {

    private InitializeAttributeContext action;

    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    protected OidcStepUpContext oidcCtx;
    protected Map<String, String> claimToAttributeMap;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        oidcCtx = new OidcStepUpContext();
        prc.addSubcontext(oidcCtx);
        action = new InitializeAttributeContext();
        claimToAttributeMap = new HashMap();
        claimToAttributeMap.put("eppn", "INTeppn");
        claimToAttributeMap.put("mobile", "INTmobile");
        action.setClaimToAttribute(claimToAttributeMap);
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
        action.initialize();

    }

    /**
     * Test that action copes with no claims to attributes set.
     */
    @Test
    public void testNoClaimsToAttributes() {
        action.setClaimToAttribute(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_SEC_CFG);
    }

    /**
     * Test that action is able to build attrib context
     * 
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {
        final Event event = action.execute(src);
        Assert.assertNull(event);
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

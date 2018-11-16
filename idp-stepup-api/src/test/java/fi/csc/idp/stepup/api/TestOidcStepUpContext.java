package fi.csc.idp.stepup.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;

public class TestOidcStepUpContext {

    private OidcStepUpContext oidcStepUpContext;

    @BeforeMethod
    public void setUp() {
        oidcStepUpContext = new OidcStepUpContext();
    }

    @Test
    public void testInitialState() {
        Assert.assertNull(oidcStepUpContext.getErrorCode());
        Assert.assertNull(oidcStepUpContext.getErrorDescription());
        Assert.assertNull(oidcStepUpContext.getIdToken());
        Assert.assertNull(oidcStepUpContext.getIssuer());
        Assert.assertNull(oidcStepUpContext.getRequest());
    }

    @Test
    public void testSetters() throws ParseException, URISyntaxException {
        oidcStepUpContext.setErrorCode("fooCode");
        Assert.assertEquals(oidcStepUpContext.getErrorCode(), "fooCode");
        oidcStepUpContext.setErrorDescription("fooDescription");
        Assert.assertEquals(oidcStepUpContext.getErrorDescription(), "fooDescription");
        JWTClaimsSet token = new JWTClaimsSet.Builder().build();
        oidcStepUpContext.setIdToken(token);
        Assert.assertEquals(oidcStepUpContext.getIdToken(), token);
        oidcStepUpContext.setIssuer("fooIss");
        Assert.assertEquals(oidcStepUpContext.getIssuer(), "fooIss");
        AuthenticationRequest req = new AuthenticationRequest.Builder(ResponseType.getDefault(), Scope.parse("openid"),
                new ClientID("id"), new URI("https://foo.bar")).endpointURI(new URI("https://foo.bar"))
                .responseMode(ResponseMode.QUERY).state(new State()).nonce(new Nonce()).build();
        oidcStepUpContext.setRequest(req);
        Assert.assertEquals(oidcStepUpContext.getRequest(), req);
    }

}

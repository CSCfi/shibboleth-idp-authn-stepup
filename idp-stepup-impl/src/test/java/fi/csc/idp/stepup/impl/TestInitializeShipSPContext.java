package fi.csc.idp.stepup.impl;

import java.util.Map;

import net.shibboleth.idp.authn.context.AuthenticationContext;
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

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

import fi.csc.idp.stepup.api.OidcStepUpContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

public class TestInitializeShipSPContext {

    private InitializeShibSPContext action;

    protected RequestContext src;
    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    protected OidcStepUpContext oidcCtx;
    protected Map<String, String> claimToAttributeMap;

    @BeforeMethod
    public void setUp() throws Exception {
        AuthenticationRequest req = AuthenticationRequest
                .parse("max_age=0&acr_values=foo bar&response_type=code&client_id=s6BhdRkqt3&login_hint=foo&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb&scope=openid%20profile&state=af0ifjsldkj&nonce=n-0S6_WzA2Mj");
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        oidcCtx = new OidcStepUpContext();
        prc.addSubcontext(oidcCtx);
        action = new InitializeShibSPContext();
        oidcCtx.setRequest(req);
        oidcCtx.setRedirectUriValidated(true);
        action.initialize();

    }

    /**
     * Test that action is able to cope with no auth context
     */
    @Test
    public void testNoAuthCtx() {

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    /**
     * Test that action is able to shib sp context
     * 
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {

        prc.getSubcontext(AuthenticationContext.class, true);
        final Event event = action.execute(src);
        Assert.assertNull(event);
        AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class, false);
        Assert.assertNotNull(authCtx);
        ShibbolethSpAuthenticationContext spCtx = (ShibbolethSpAuthenticationContext) authCtx.getSubcontext(
                ShibbolethSpAuthenticationContext.class, false);
        Assert.assertNotNull(spCtx);
        Assert.assertTrue(spCtx.getInitialRequestedContext().size() == 2);
    }

}

package fi.csc.idp.stepup.impl;

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

public class TestAttachOidcAuthenticationRequest {

    private AttachOidcAuthenticationRequest action;

    @SuppressWarnings("rawtypes")
    protected ProfileRequestContext prc;
    protected RequestContext requestCtx;

    @BeforeMethod
    public void setUp() throws Exception {
        AuthenticationRequest req = AuthenticationRequest
                .parse("response_type=code&client_id=s6BhdRkqt3&login_hint=foo&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb&scope=openid%20profile&state=af0ifjsldkj&nonce=n-0S6_WzA2Mj");
        requestCtx = new RequestContextBuilder().setInboundMessage(req).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);
        action = new AttachOidcAuthenticationRequest();
        action.initialize();
    }

    /**
     * Test that action copes with no issuer set.
     * 
     */
    @Test
    public void testNoIssuer() {
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_SEC_CFG);
    }

    /**
     * Test that action copes with no inbound message context.
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNoInboundMessageContext() {
        prc.setInboundMessageContext(null);
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    /**
     * Test that action copes with no inbound message.
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNoInboundMessage() {
        prc.getInboundMessageContext().setMessage(null);
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_MSG_CTX);
    }

    /**
     * Test success
     */
    @Test
    public void testSuccess() {
        action.setIssuer("iss value");
        final Event event = action.execute(requestCtx);
        Assert.assertNull(event);
        OidcStepUpContext oidcCtx = prc.getSubcontext(OidcStepUpContext.class, false);
        Assert.assertEquals(oidcCtx.getIssuer(), "iss value");

    }

}

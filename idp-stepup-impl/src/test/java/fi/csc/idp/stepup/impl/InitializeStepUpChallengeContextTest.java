
package fi.csc.idp.stepup.impl;

import java.security.NoSuchAlgorithmException;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.geant.idpextension.oidc.messaging.context.OIDCAuthenticationResponseContext;
import org.geant.idpextension.oidc.messaging.context.OIDCMetadataContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.ClaimsRequest;
import org.springframework.webflow.execution.Event;

import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import org.mockito.Mockito;

public class InitializeStepUpChallengeContextTest {

    @SuppressWarnings("rawtypes")
    private ProfileRequestContext prc;

    private InitializeStepUpChallengeContext action;

    private RequestContext requestCtx;

    private OIDCAuthenticationResponseContext oidcRespCtx;

    private AuthenticationContext authnCtx;

    @BeforeMethod
    public void setup() throws ComponentInitializationException, NoSuchAlgorithmException, ParseException {
        requestCtx = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);
        authnCtx = prc.getSubcontext(AuthenticationContext.class, true);
        prc.getInboundMessageContext().addSubcontext(new OIDCMetadataContext());
        oidcRespCtx = new OIDCAuthenticationResponseContext();
        oidcRespCtx.setRequestedClaims(ClaimsRequest.parse("{\n" + "   \"id_token\":{\n" + "      \"sub\":{\n"
                + "         \"value\":\"12345678\"\n" + "      },\n" + "      \"otp_key\":{\n"
                + "         \"value\":\"testdude@testdomain.com\"\n" + "      },\n" + "      \"mobile\":{\n"
                + "         \"value\":\"+358503818416\"\n" + "      }\n" + "   }\n" + "}"));
        prc.getOutboundMessageContext().addSubcontext(oidcRespCtx);
        action = new InitializeStepUpChallengeContext();
        action.setStepUpMethod(Mockito.mock(StepUpMethod.class));
        action.initialize();
    }

    @Test
    public void testSuccess() throws ComponentInitializationException {
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);
        StepUpMethodContext stepUpMethodContext = authnCtx.getSubcontext(StepUpMethodContext.class, false);
        Assert.assertNotNull(stepUpMethodContext);
        Assert.assertEquals(stepUpMethodContext.getSubject(), "12345678");
    }

    @Test
    public void testNoAuthnCtx() throws ComponentInitializationException {
        prc.removeSubcontext(AuthenticationContext.class);
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void testFailNoRequestedClaims() throws ComponentInitializationException {
        oidcRespCtx.setRequestedClaims(null);
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_NO_USER);
    }

    @Test
    public void testFailNoSubjectInRequestedClaims() throws ComponentInitializationException, ParseException {
        oidcRespCtx.setRequestedClaims(ClaimsRequest.parse("{\n" + "   \"id_token\":{\n" + "      \"otp_key\":{\n"
                + "         \"value\":\"testdude@testdomain.com\"\n" + "      },\n" + "      \"mobile\":{\n"
                + "         \"value\":\"+358503818416\"\n" + "      }\n" + "   }\n" + "}"));
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_NO_USER);
    }

    @Test
    public void testFailNoSubjectValueInRequestedClaims() throws ComponentInitializationException, ParseException {
        oidcRespCtx.setRequestedClaims(
                ClaimsRequest.parse("{\n" + "   \"id_token\":{\n" + "      \"sub\":null,\n" + "      \"otp_key\":{\n"
                        + "         \"value\":\"testdude@testdomain.com\"\n" + "      },\n" + "      \"mobile\":{\n"
                        + "         \"value\":\"+358503818416\"\n" + "      }\n" + "   }\n" + "}\n" + ""));
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_NO_USER);
    }

    @Test
    public void testFailNoReqObject() throws ComponentInitializationException {
        // TODO: prevent using setters after initialization
        action = new InitializeStepUpChallengeContext();
        action.setAcceptOnlyRequestObjectClaims(true);
        action.setStepUpMethod(Mockito.mock(StepUpMethod.class));
        action.initialize();
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EVENTID_NO_USER);
    }

    @Test
    public void testSuccessReqObject() throws ComponentInitializationException, ParseException {
        ClaimsRequest cr = ClaimsRequest.parse("{\n" + "   \"id_token\":{\n" + "      \"sub\":{\n"
                + "         \"value\":\"87654321\"\n" + "      },\n" + "      \"otp_key\":{\n"
                + "         \"value\":\"testdude@testdomain.com\"\n" + "      },\n" + "      \"mobile\":{\n"
                + "         \"value\":\"+358503818416\"\n" + "      }\n" + "   }\n" + "}");
        JWTClaimsSet ro = new JWTClaimsSet.Builder().claim("claims", cr.toJSONObject()).build();
        oidcRespCtx.setRequestObject(new PlainJWT(ro));
        // TODO: prevent using setters after initialization
        action = new InitializeStepUpChallengeContext();
        action.setAcceptOnlyRequestObjectClaims(true);
        action.setStepUpMethod(Mockito.mock(StepUpMethod.class));
        action.initialize();
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);
        StepUpMethodContext stepUpMethodContext = authnCtx.getSubcontext(StepUpMethodContext.class, false);
        Assert.assertNotNull(stepUpMethodContext);
        Assert.assertEquals(stepUpMethodContext.getSubject(), "87654321");
    }

}

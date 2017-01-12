package fi.csc.idp.stepup.impl;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;


public class TestAttachOidcAuthenticationRequest {

    private AttachOidcAuthenticationRequest action;

    protected RequestContext src;
   
    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        action = new AttachOidcAuthenticationRequest();
       
    }

    /** Test that action copes with no issuer set. 
    */
    @Test
    public void testNoIssuer() {
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EXCEPTION);
    }

    /** Test that action copes with no query parameters. 
     */
     @Test
     public void testNoQuery() {
         
         action.setIssuer("iss value");
         final Event event = action.execute(src);
         ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_INVALID_QUERYSTRING);
     }
     
     /** Test that in case of a(ny) valid oidc request we return success event. 
      */
      @Test
      public void testSuccess() throws ComponentInitializationException {
          action.setIssuer("iss value");
          MockHttpServletRequest msr=new MockHttpServletRequest();
          msr.setQueryString("response_type=code&scope=openid&client_id=client&state=af0ifjsldkj&redirect_uri=https://server.example.com");
          src = new RequestContextBuilder().setHttpRequest(msr).buildRequestContext();
          final Event event = action.execute(src);
          ActionTestingSupport.assertEvent(event, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
      }
   
}

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

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

/**
 * This action creates OidcStepUpContext and initializes it with issuer value
 * and parsed authentication request.
 * 
 */
public class AttachOidcAuthenticationRequest implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttachOidcAuthenticationRequest.class);

    /** issuer stored to be used in verifications and response. */
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

    @Override
    public Event execute(@Nonnull final RequestContext springRequestContext) {
        log.trace("Entering");
        if (issuer == null) {
            log.error("issuer not set");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        OidcStepUpContext oidcCtx = new OidcStepUpContext();
        springRequestContext.getConversationScope().put(OidcStepUpContext.getContextKey(), oidcCtx);
        ServletExternalContext externalContext = (ServletExternalContext) springRequestContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        AuthenticationRequest req;
        try {
            req = AuthenticationRequest.parse(request.getQueryString());
        } catch (com.nimbusds.oauth2.sdk.ParseException e1) {
            return new Event(this, OidcProcessingEventIds.EVENTID_INVALID_QUERYSTRING);
        }
        oidcCtx.setRequest(req);
        oidcCtx.setIssuer(issuer);
        log.trace("Leaving");
        return new Event(this, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
    }

}
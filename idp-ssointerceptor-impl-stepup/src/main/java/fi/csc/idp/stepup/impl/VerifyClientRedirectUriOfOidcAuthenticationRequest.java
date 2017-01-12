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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

/**
 * This actions checks that the redirect uri of the authentication request is
 * whitelisted.
 * 
 */
public class VerifyClientRedirectUriOfOidcAuthenticationRequest 
    implements org.springframework.webflow.execution.Action {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(VerifyClientRedirectUriOfOidcAuthenticationRequest.class);

    /** redirect uris that are valid per client id. */
    private Map<String, List<String>> redirectUris;

    /**
     * Setter for redirect uris.
     * 
     * @param uris
     *            maps client ids to list of uris
     */
    public void setRedirectUris(Map<String, List<String>> uris) {
        this.redirectUris = uris;
    }

    @Override
    public Event execute(@Nonnull final RequestContext springRequestContext) {
        log.trace("Entering");
        if (redirectUris == null) {
            log.error("redirect uri whitelist not set");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        OidcStepUpContext oidcCtx = (OidcStepUpContext) springRequestContext.getConversationScope().get(
                OidcStepUpContext.getContextKey());
        if (oidcCtx == null) {
            log.error("oidc context missing");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        if (oidcCtx.getRequest() == null) {
            log.error("authentication request missing");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EXCEPTION);
        }
        // if request is found it is assumed to be formally valid
        if (!redirectUris.containsKey(oidcCtx.getRequest().getClientID().getValue())) {
            log.error("client id " + oidcCtx.getRequest().getClientID().getValue()
                    + " has not registered redirect uris");
            oidcCtx.setErrorCode("unauthorized_client");
            oidcCtx.setErrorDescription("client has not registered any redirect uri");
            log.trace("Leaving");
            return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);

        }
        for (String uri : redirectUris.get(oidcCtx.getRequest().getClientID().getValue())) {
            log.debug("matching to " + uri);
            if (uri.equals(oidcCtx.getRequest().getRedirectionURI().toString())) {
                log.debug("redirect uri validated");
                log.trace("Leaving");
                return new Event(this, OidcProcessingEventIds.EVENTID_CONTINUE_OIDC);
            }
        }
        log.error("client id " + oidcCtx.getRequest().getClientID().getValue() + " has not registered uri "
                + oidcCtx.getRequest().getRedirectionURI().toString());
        oidcCtx.setErrorCode("unauthorized_client");
        oidcCtx.setErrorDescription("client has not registered redirect uri "
                + oidcCtx.getRequest().getRedirectionURI().toString());
        log.trace("Leaving");
        return new Event(this, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
    }

}
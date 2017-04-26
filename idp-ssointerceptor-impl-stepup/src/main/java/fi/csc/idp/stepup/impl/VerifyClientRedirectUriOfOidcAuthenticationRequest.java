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
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.OidcProcessingEventIds;

/**
 * This actions checks that the redirect uri of the authentication request is
 * whitelisted.
 * 
 */
@SuppressWarnings("rawtypes")
public class VerifyClientRedirectUriOfOidcAuthenticationRequest extends AbstractOidcProfileAction {

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

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed", getLogPrefix());
            return false;
        }
        if (redirectUris == null) {
            log.error("{} bean not initialized with redirect uris", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!redirectUris.containsKey(getOidcCtx().getRequest().getClientID().getValue())) {
            getOidcCtx().setErrorCode("unauthorized_client");
            getOidcCtx().setErrorDescription("client has not registered any redirect uri");
            log.error("{} client {} has not registered redirect uris", getLogPrefix(), getOidcCtx().getRequest()
                    .getClientID().getValue());
            ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_LOCAL_OIDC);
            return;

        }
        for (String uri : redirectUris.get(getOidcCtx().getRequest().getClientID().getValue())) {
            log.debug("matching to " + uri);
            if (uri.equals(getOidcCtx().getRequest().getRedirectionURI().toString())) {
                log.debug("redirect uri validated");
                getOidcCtx().setRedirectUriValidated(true);
                return;
            }
        }
        getOidcCtx().setErrorCode("unauthorized_client");
        getOidcCtx().setErrorDescription(
                "client has not registered redirect uri " + getOidcCtx().getRequest().getRedirectionURI().toString());
        log.error("{} client {} has not registered redirect uri {}", getLogPrefix(), getOidcCtx().getRequest()
                .getClientID().getValue(), getOidcCtx().getRequest().getRedirectionURI().toString());
        ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_LOCAL_OIDC);
    }

}
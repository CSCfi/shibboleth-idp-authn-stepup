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

import net.shibboleth.idp.profile.AbstractProfileAction;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import fi.csc.idp.stepup.api.OidcProcessingEventIds;
import fi.csc.idp.stepup.api.OidcStepUpContext;

/**
 * This actions checks that the redirect uri of the authentication request is
 * whitelisted.
 * 
 */
@SuppressWarnings("rawtypes")
public class VerifyClientRedirectUriOfOidcAuthenticationRequest extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(VerifyClientRedirectUriOfOidcAuthenticationRequest.class);

    /** redirect uris that are valid per client id. */
    private Map<String, List<String>> redirectUris;

    /** OIDC Ctx. */
    private OidcStepUpContext oidcCtx;

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
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed", getLogPrefix());
            return false;
        }
        oidcCtx = profileRequestContext.getSubcontext(OidcStepUpContext.class, false);
        if (oidcCtx == null) {
            //TODO: not causing a failure, fix
            log.error("{} Unable to locate oidc context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        if (redirectUris == null) {
            //TODO: not causing a failure, fix
            log.error("{} bean not initialized with redirect uris", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!redirectUris.containsKey(oidcCtx.getRequest().getClientID().getValue())) {
            oidcCtx.setErrorCode("unauthorized_client");
            oidcCtx.setErrorDescription("client has not registered any redirect uri");
            log.trace("Leaving");
            log.error("{} client {} has not registered redirect uris", getLogPrefix(), oidcCtx.getRequest()
                    .getClientID().getValue());
            ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_LOCAL_OIDC);

        }
        for (String uri : redirectUris.get(oidcCtx.getRequest().getClientID().getValue())) {
            log.debug("matching to " + uri);
            if (uri.equals(oidcCtx.getRequest().getRedirectionURI().toString())) {
                log.debug("redirect uri validated");
                oidcCtx.setRedirectUriValidated(true);
                log.trace("Leaving");
                return;
            }
        }
        oidcCtx.setErrorCode("unauthorized_client");
        oidcCtx.setErrorDescription("client has not registered redirect uri "
                + oidcCtx.getRequest().getRedirectionURI().toString());
        log.trace("Leaving");
        log.error("{} client {} has not registered redirect uri {}", getLogPrefix(), oidcCtx.getRequest().getClientID()
                .getValue(), oidcCtx.getRequest().getRedirectionURI().toString());
        ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_LOCAL_OIDC);
    }

}
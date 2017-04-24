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

import net.shibboleth.idp.profile.AbstractProfileAction;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

import fi.csc.idp.stepup.api.OidcStepUpContext;

/**
 * This action creates OidcStepUpContext and initializes it with issuer value
 * and parsed authentication request.
 * 
 */
@SuppressWarnings("rawtypes")
public class AttachOidcAuthenticationRequest extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttachOidcAuthenticationRequest.class);

    /** issuer stored to be used in verifications and response. */
    private String issuer;

    /** OIDC Authentication request. */
    private AuthenticationRequest request;

    /**
     * Set the value for Issuer. Mandatory.
     * 
     * @param iss
     *            value for Issuer
     */

    public void setIssuer(String iss) {
        this.issuer = iss;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed", getLogPrefix());
            return false;
        }
        if (profileRequestContext.getInboundMessageContext() == null) {
            //TODO: not causing a failure, fix
            log.error("{} Unable to locate inbound message context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return false;
        }
        Object message = profileRequestContext.getInboundMessageContext().getMessage();

        if (message == null || !(message instanceof AuthenticationRequest)) {
            //TODO: not causing a failure, fix
            log.error("{} Unable to locate inbound message", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return false;
        }
        if (issuer == null) {
            //TODO: not causing a failure, fix
            log.error("{} bean not initialized with issuer", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        request = (AuthenticationRequest) message;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        OidcStepUpContext oidcCtx = new OidcStepUpContext();
        profileRequestContext.addSubcontext(oidcCtx);
        log.debug("Attaching inbound message to oidc stepup context {}", request.toQueryString());
        oidcCtx.setRequest(request);
        log.debug("Setting issuer value to oidc stepup context {}", issuer);
        oidcCtx.setIssuer(issuer);
    }

}
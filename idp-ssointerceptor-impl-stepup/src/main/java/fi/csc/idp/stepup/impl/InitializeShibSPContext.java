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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.openid.connect.sdk.claims.ACR;

import fi.csc.idp.stepup.api.OidcStepUpContext;
import fi.okm.mpass.shibboleth.authn.context.ShibbolethSpAuthenticationContext;

/**
 * An action that creates an {@link ShibbolethSpAuthenticationContext} and
 * attaches it to the current {@link AuthenticationContext}.
 * 
 */
@SuppressWarnings("rawtypes")
public class InitializeShibSPContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeShibSPContext.class);

    /** OIDC Ctx. */
    private OidcStepUpContext oidcCtx;

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
            // TODO: not causing a failure, fix
            log.error("{} Unable to locate oidc context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        return true;

    }

    /**
     * Creates ShibbolethSpAuthenticationContext instance and adds it to
     * AuthenticationContext instance. Populates
     * ShibbolethSpAuthenticationContext instance with ACR values from oidc
     * authentication request.
     * 
     */

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        AuthenticationContext authnCtx = profileRequestContext.getSubcontext(AuthenticationContext.class, false);
        if (authnCtx == null) {
            log.error("{} Unable to locate authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }
        ShibbolethSpAuthenticationContext shibspCtx = (ShibbolethSpAuthenticationContext) authnCtx.addSubcontext(
                new ShibbolethSpAuthenticationContext(), true);
        List<Principal> requested = new ArrayList<Principal>();
        if (oidcCtx.getRequest().getACRValues() == null) {
            log.debug("no acr set in request");
            return;
        }
        for (ACR acr : oidcCtx.getRequest().getACRValues()) {
            log.debug("Setting acr " + acr + " as requested AuthnContextClassRef");
            requested.add(new AuthnContextClassRefPrincipal(acr.getValue()));
        }
        if (requested.size() > 0) {
            shibspCtx.setInitialRequestedContext(requested);
        }

    }

}
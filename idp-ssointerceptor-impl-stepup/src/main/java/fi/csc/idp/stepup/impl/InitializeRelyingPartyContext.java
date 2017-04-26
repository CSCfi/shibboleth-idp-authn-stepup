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
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;

/**
 * Action that adds a {@link RelyingPartyContext} to the current
 * {@link ProfileRequestContext} tree via a creation function.
 * 
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link IdPEventIds#INVALID_RELYING_PARTY_CTX}
 * @post ProfileRequestContext.getSubcontext(RelyingPartyContext.class) != null
 *       with relying party id set.
 */
@SuppressWarnings("rawtypes")
public class InitializeRelyingPartyContext extends AbstractOidcProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeRelyingPartyContext.class);

    /** Strategy that will return or create a {@link RelyingPartyContext}. */
    @Nonnull
    private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextCreationStrategy;

    /** Constructor. */
    public InitializeRelyingPartyContext() {
        relyingPartyContextCreationStrategy = new ChildContextLookup<>(RelyingPartyContext.class, true);
    }

    /**
     * Set the strategy used to return or create the {@link RelyingPartyContext}
     * .
     * 
     * @param strategy
     *            creation strategy
     */
    public void setRelyingPartyContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        relyingPartyContextCreationStrategy = Constraint.isNotNull(strategy,
                "RelyingPartyContext creation strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final RelyingPartyContext rpContext = relyingPartyContextCreationStrategy.apply(profileRequestContext);
        if (rpContext == null) {
            log.debug("{} Unable to locate or create RelyingPartyContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return;
        }

        log.debug("{} Attaching RelyingPartyContext for rp {}", getLogPrefix(), getOidcCtx().getRequest().getClientID()
                .getValue());
        rpContext.setVerified(getOidcCtx().isRedirectUriValidated());
        rpContext.setRelyingPartyId(getOidcCtx().getRequest().getClientID().getValue());
    }

}
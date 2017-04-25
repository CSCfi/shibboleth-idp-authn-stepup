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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.OidcProcessingEventIds;

/**
 * Creates AttributeContext, populates it with id token claim values and adds it
 * to RelyingPartyContext.
 */
@SuppressWarnings("rawtypes")
public class InitializeAttributeContext extends AbstractOidcProfileAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(InitializeAttributeContext.class);

    /** claim to attribute mapping. */
    private Map<String, String> claimToAttribute;

    /**
     * Set mapping of claims to attributes. claim names are keys for attribute
     * names.
     * 
     * @param claimToAttributeMap
     *            map
     */
    public void setClaimToAttribute(Map<String, String> claimToAttributeMap) {
        log.trace("Entering & Leaving");
        this.claimToAttribute = claimToAttributeMap;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            log.error("{} pre-execute failed", getLogPrefix());
            return false;
        }
        if (claimToAttribute == null) {
            log.error("{} bean not initialized with claims to attribute mapping", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        return true;

    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final RelyingPartyContext rpContext = profileRequestContext.getSubcontext(RelyingPartyContext.class, false);
        if (rpContext == null) {
            log.error("{} Unable to locate relying party context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }
        List<IdPAttribute> attributes = new ArrayList<IdPAttribute>();
        for (String key : claimToAttribute.keySet()) {
            if (oidcCtx.getIdToken().getClaims().keySet().contains(key)) {
                String attributeName = claimToAttribute.get(key);
                if (attributeName == null) {
                    // claim is listed but not set as attribute
                    log.warn("claims to attribute map contains null value for key " + key);
                    continue;
                }
                // claim is supported and set as attribute
                List<String> values;
                try {
                    values = oidcCtx.getIdToken().getStringListClaim(key);
                } catch (ParseException e) {
                    values = new ArrayList<String>();
                    try {
                        values.add(oidcCtx.getIdToken().getStringClaim(key));
                    } catch (ParseException e1) {
                        oidcCtx.setErrorCode("invalid_request");
                        oidcCtx.setErrorDescription("id token contained a unparsable claim");
                        log.error("{} id token contained a unparsable claim", getLogPrefix());
                        ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
                        log.trace("Leaving");
                    }
                }
                if (values == null || values.size() == 0) {
                    log.warn("claim " + key + " did not contain any values");
                    continue;
                }
                log.debug("Creating attribute " + claimToAttribute.get(key) + " with value(s):");
                IdPAttribute attribute = new IdPAttribute(claimToAttribute.get(key));
                List<StringAttributeValue> stringAttributeValues = new ArrayList<StringAttributeValue>();
                for (String value : values) {
                    log.debug(value);
                    stringAttributeValues.add(new StringAttributeValue(value));
                }
                attribute.setValues(stringAttributeValues);
                attributes.add(attribute);
            } else {
                oidcCtx.setErrorCode("invalid_request");
                oidcCtx.setErrorDescription("request does not have required claim in id token: " + key);
                log.error("{} request does not have required claim in id token: {}", getLogPrefix(), key);
                ActionSupport.buildEvent(profileRequestContext, OidcProcessingEventIds.EVENTID_ERROR_OIDC);
                log.trace("Leaving");
                return;
            }
        }
        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        log.debug("{} setting attribute context to prc", getLogPrefix());
        profileRequestContext.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);
        return;
    }

}
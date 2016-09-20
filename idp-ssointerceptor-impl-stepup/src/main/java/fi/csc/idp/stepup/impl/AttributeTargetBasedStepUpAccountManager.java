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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.StepUpAccount;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;

public class AttributeTargetBasedStepUpAccountManager extends AbstractStepUpAccountManager {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(DefaultStepUpAccountManager.class);

    /** The attribute ID to look for. */
    private String attributeId;

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public void Initialize(AttributeContext attributeContext) {
        log.trace("Entering & Leaving");
        String target = null;
        log.debug("Adding accounts of type " + getName());
        IdPAttribute attribute = attributeContext.getIdPAttributes().get(attributeId);
        if (attribute == null) {
            log.debug("Not able to create accounts, Attributes do not contain value for " + attributeId);
            log.trace("Leaving");
            return;
        }
        for (@SuppressWarnings("rawtypes")
        final IdPAttributeValue value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                target = ((StringAttributeValue) value).getValue();
                if (target != null) {
                    log.debug("Adding account with target value " + target);
                    StepUpAccount account = (StepUpAccount) getAppContext().getBean(getAccountID());
                    account.setTarget(target);
                    getAccounts().add(account);
                }
            }
        }
        log.trace("Leaving");
        return;
    }
}

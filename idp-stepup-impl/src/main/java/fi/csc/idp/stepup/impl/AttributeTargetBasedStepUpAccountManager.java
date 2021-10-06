/*
 * The MIT License
 * Copyright (c) 2015-2020 CSC - IT Center for Science, http://www.csc.fi
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

import java.util.Collection;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import fi.csc.idp.stepup.api.StepUpAccount;

/**
 * Class implementing step up account manager for accounts initialised by key found in attribute values.
 */
public class AttributeTargetBasedStepUpAccountManager extends AbstractStepUpAccountManager {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AttributeTargetBasedStepUpAccountManager.class);

    /** Decryptor for encrypted attribute value. */
    private AttributeDecryptor decryptor;

    /**
     * Set decryptor for encrypted attribute value.
     * 
     * @param decryptor decryptor for encrypted attribute value
     */
    public void setDecryptor(AttributeDecryptor decryptor) {
        this.decryptor = decryptor;
    }

    /** The claim name to look for. */
    private String claimName;

    /**
     * Set the name for claim containing the value for the key.
     * 
     * @param name of the claim containing the value of key
     */
    public void setClaimName(String name) {
        claimName = name;
    }

    /**
     * Initialises account by reading the value for key, using that to instantiate non editable accounts.
     * 
     * @param entry claims to look for the key value
     * @throws Exception if something unexpected occurred.
     */
    @Override
    public boolean initialize(Collection<Entry> entry) throws Exception {

        String target = null;
        log.debug("Adding accounts of type {}", getName());
        if (entry == null) {
            throw new Exception("requested id token claims cannot be null");
        }
        if (claimName == null) {
            throw new Exception("Attribute Id has to be set");
        }
        if (getAccountID() == null) {
            throw new Exception("No account bean defined");
        }
        for (Entry claim : entry) {
            if (claimName.equals(claim.getClaimName())) {
                target = decryptor == null ? claim.getValue() : decryptor.decrypt(claim.getValue());
                if (target != null) {
                    log.debug("Adding account with target value {}", target);
                    StepUpAccount account = (StepUpAccount) getAppContext().getBean(getAccountID());
                    account.setTarget(target);
                    try {
                        setAccount(account);
                        break;
                    } catch (Exception e) {
                        log.debug("Not able to add account during initialization");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

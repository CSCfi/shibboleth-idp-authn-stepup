/*
 * GÉANT BSD Software License
 *
 * Copyright (c) 2017 - 2020, GÉANT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the GÉANT nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fi.csc.idp.stepup.api.messaging.impl;

import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.messaging.ApiRequest;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A simple implementation for {@link ApiRequest}.
 */
public class ApiRequestImpl implements ApiRequest {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ApiRequestImpl.class);

    /** API request parameter map. */
    private final Map<String, String[]> parameterMap;

    /**
     * Constructor.
     * 
     * @param parameterMap Api request parameter map.
     */
    public ApiRequestImpl(Map<String, String[]> parameterMap) {
        Constraint.isNotNull(parameterMap, "Api request parameter map cannot be null");
        this.parameterMap = parameterMap;
    }

    @Override
    public String getToken() {
        return (parameterMap.get("token") != null && parameterMap.get("token").length > 0)
                ? parameterMap.get("token")[0] : null;
    }

    @Override
    public String getUserId() {
        return (parameterMap.get("userid") != null && parameterMap.get("userid").length > 0)
                ? parameterMap.get("userid")[0] : null;
    }

    @Override
    public Map<String, String[]> getRequestParameterMap() {
        return parameterMap;
    }

    @Override
    public boolean getForceUpdate() {
        return (parameterMap.get("forceUpdate") != null && parameterMap.get("forceUpdate").length > 0)
                ? parameterMap.get("forceUpdate")[0].toLowerCase().equals("true") : false;
    }

    @Override
    public int getMaxAccounts() {
        try {
            return (parameterMap.get("accountLimit") != null && parameterMap.get("accountLimit").length > 0)
                    ? Integer.parseInt(parameterMap.get("accountLimit")[0]) : 1;
        } catch (NumberFormatException e) {
            log.warn("Unable to parse accountLimit {}, setting to default value 1",
                    parameterMap.get("accountLimit")[0]);
            return 1;
        }
    }

    @Override
    public String getValue() {
        return (parameterMap.get("value") != null && parameterMap.get("value").length > 0)
                ? parameterMap.get("value")[0] : null;
    }
}

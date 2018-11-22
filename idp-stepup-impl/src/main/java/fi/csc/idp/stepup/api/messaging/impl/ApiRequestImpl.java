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

/*
 * The MIT License
 * Copyright (c) 2020 CSC - IT Center for Science, http://www.csc.fi
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

import java.util.HashMap;
import java.util.Map;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ApiRequestImplTest {

    private ApiRequestImpl request;
    private Map<String, String[]> parameterMap;

    @BeforeMethod
    protected void setUp() throws Exception {
        parameterMap = new HashMap<String, String[]>();
        parameterMap.put("token", new String[] { "token_value" });
        parameterMap.put("userid", new String[] { "userid_value" });
        parameterMap.put("forceUpdate", new String[] { "true" });
        parameterMap.put("value", new String[] { "value_value" });
        request = new ApiRequestImpl(parameterMap);
    }

    @Test
    public void testGetters() throws MessageDecodingException {
        Assert.assertEquals(request.getToken(), "token_value");
        Assert.assertEquals(request.getUserId(), "userid_value");
        Assert.assertTrue(request.getForceUpdate());
        Assert.assertEquals(request.getValue(), "value_value");
    }

    @Test(expectedExceptions = MessageDecodingException.class)
    public void testDoubleValueFail() throws MessageDecodingException {
        parameterMap = new HashMap<String, String[]>();
        parameterMap.put("token", new String[] { "token_value", "double_value" });
        request = new ApiRequestImpl(parameterMap);
    }

    @Test(expectedExceptions = MessageDecodingException.class)
    public void testNoValueFail() throws MessageDecodingException {
        parameterMap = new HashMap<String, String[]>();
        parameterMap.put("token", null);
        request = new ApiRequestImpl(parameterMap);
    }

}
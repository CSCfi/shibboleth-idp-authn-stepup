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

package fi.csc.idp.stepup.api.profile.impl;

import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import fi.csc.idp.stepup.api.StepUpAccountStorage;
import fi.csc.idp.stepup.api.StepUpApiContext;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.messaging.impl.ApiRequestImpl;
import fi.csc.idp.stepup.impl.MockAccount;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

public class RemoveAccountTest {

    private RemoveAccount action;
    private RequestContext src;
    private ProfileRequestContext prc;
    private StepUpAccountStorage storage;

    @BeforeMethod
    public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        storage = Mockito.mock(StepUpAccountStorage.class);
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("userid", new String[] { "userid_value" });
        prc.getInboundMessageContext().setMessage(new ApiRequestImpl(parameterMap));
        prc.addSubcontext(new StepUpApiContext(new MockAccount(), storage));
        prc.getSubcontext(StepUpApiContext.class).setAccount(new MockAccount());
        action = new RemoveAccount();
        action.initialize();
    }

    @Test
    public void testSuccess() throws Exception {
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(action.getCtx().getResponse().get("userid"), "userid_value");
    }

    @Test
    public void testStorageRemoveFailure() throws Exception {
        Mockito.doThrow(new Exception("Error occurred")).when(storage).remove(Mockito.any(), Mockito.any());
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, StepUpEventIds.EXCEPTION);
        Assert.assertEquals(action.getCtx().getResponse().get("userid"), "userid_value");
        Assert.assertEquals(action.getCtx().getResponse().get("error"), "Internal error");
    }
}

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
package fi.csc.idp.stepup.api;

import org.testng.Assert;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StepUpMethodContextTest {

    private StepUpMethodContext stepUpMethodContext;

    @BeforeMethod
    public void setUp() {
        stepUpMethodContext = new StepUpMethodContext();
    }

    @Test
    public void testAllNull() {
        Assert.assertNull(stepUpMethodContext.getStepUpAccount());
        Assert.assertNull(stepUpMethodContext.getStepUpMethod());
        Assert.assertNull(stepUpMethodContext.getSubject());
    }

    @Test
    public void testNotNull() {
        stepUpMethodContext.setStepUpAccount(Mockito.mock(StepUpAccount.class));
        stepUpMethodContext.setStepUpMethod(Mockito.mock(StepUpMethod.class));
        stepUpMethodContext.setSubject("sub");
        Assert.assertNotNull(stepUpMethodContext.getStepUpAccount());
        Assert.assertNotNull(stepUpMethodContext.getStepUpMethod());
        Assert.assertEquals(stepUpMethodContext.getSubject(), "sub");
    }
}

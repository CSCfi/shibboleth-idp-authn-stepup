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

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

public class OIDCDecoderTest {

	private MockHttpServletRequest httpRequest;
	private OIDCDecoder decoder;

	@BeforeMethod
	protected void setUp() throws Exception {
		httpRequest = new MockHttpServletRequest();
		decoder = new OIDCDecoder();
		decoder.setHttpServletRequest(httpRequest);
		decoder.initialize();
	}

	@Test
	public void testRequestDecoding() throws MessageDecodingException {
		httpRequest
				.setQueryString("response_type=code&client_id=s6BhdRkqt3&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb&scope=openid%20profile&state=af0ifjsldkj&nonce=n-0S6_WzA2Mj");
		decoder.decode();
		MessageContext<AuthenticationRequest> messageContext = decoder
				.getMessageContext();
		// We are not testing nimbus itself here, i.e. we are happy to decode
		// one parameter successfully
		Assert.assertEquals(messageContext.getMessage().getResponseType()
				.toString(), ResponseType.Value.CODE.toString());

	}

	@Test
	public void testInvalidRequestDecoding() {

		// Mandatory response_type parameter removed, decoding should fail
		boolean failed = false;
		httpRequest
				.setQueryString("client_id=s6BhdRkqt3&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb&scope=openid%20profile&state=af0ifjsldkj&nonce=n-0S6_WzA2Mj");
		try {
			decoder.decode();
		} catch (MessageDecodingException e) {
			failed = true;
		}
		Assert.assertTrue(failed);

	}

}
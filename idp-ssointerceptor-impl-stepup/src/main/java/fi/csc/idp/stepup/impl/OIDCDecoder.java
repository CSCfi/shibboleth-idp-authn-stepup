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
import javax.servlet.http.HttpServletRequest;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecoder;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.decoder.servlet.AbstractHttpServletRequestMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

public class OIDCDecoder extends AbstractHttpServletRequestMessageDecoder<AuthenticationRequest> implements
        MessageDecoder<AuthenticationRequest> {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(OIDCDecoder.class);

    /** {@inheritDoc} */

    @Override
    protected void doDecode() throws MessageDecodingException {
        MessageContext<AuthenticationRequest> messageContext = new MessageContext<>();
        HttpServletRequest request = getHttpServletRequest();
        AuthenticationRequest req = null;
        try {
            req = AuthenticationRequest.parse(request.getQueryString());
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            log.error("Unable to decode oidc request: {}", e.getMessage());
            throw new MessageDecodingException(e);
        }
        messageContext.setMessage(req);
        log.debug("Decoded oidc request {}", req.toQueryString());
        setMessageContext(messageContext);
    }

}
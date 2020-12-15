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

package fi.csc.idp.stepup.api.decoding.impl;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecoder;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.decoder.servlet.AbstractHttpServletRequestMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.messaging.ApiRequest;
import fi.csc.idp.stepup.api.messaging.impl.ApiRequestImpl;

/**
 * A message decoder for {@link ApiRequest}.
 */
public class ApiRequestDecoder extends AbstractHttpServletRequestMessageDecoder implements MessageDecoder {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ApiRequestDecoder.class);

    /** {@inheritDoc} */
    @Override
    protected void doDecode() throws MessageDecodingException {
        final MessageContext messageContext = new MessageContext();
        final HttpServletRequest httpRequest = getHttpServletRequest();
        final ApiRequestImpl request;
        try {
            request = new ApiRequestImpl(httpRequest.getParameterMap());
        } catch (Exception e) {
            log.error("Unable to decode inbound request", e);
            throw new MessageDecodingException(e);
        }
        log.debug("Decoded api request request with token = {} targeting user = {}", request.getToken(),
                request.getUserId());
        messageContext.setMessage(request);
        setMessageContext(messageContext);
    }
}

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

package fi.csc.idp.stepup.api.profile.impl;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

/**
 * This action builds a response for the ApiRequest.
 */
public class BuildApiResponse extends AbstractApiAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(BuildApiResponse.class);

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final HttpServletResponse servletResponse = getHttpServletResponse();
        if (getCtx().getResponse() == null || getCtx().getResponse().isEmpty()) {
            log.debug("{} No response, nothing to do", getLogPrefix());
            return;
        }
        servletResponse.setContentType("application/jrd+json");
        servletResponse.setCharacterEncoding("UTF-8");
        final Gson gson = new Gson();
        try {
            gson.toJson(gson.toJsonTree(getCtx().getResponse()), gson.newJsonWriter(servletResponse.getWriter()));
        } catch (IOException e) {
            log.error("{} Could not encode the JSON response to the servlet response", getLogPrefix(), e);
            return;
        }
        log.debug("{} Api response successfully applied to the HTTP response", getLogPrefix());
    }
}

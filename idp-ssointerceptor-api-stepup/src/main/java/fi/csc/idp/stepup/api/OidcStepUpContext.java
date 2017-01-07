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
package fi.csc.idp.stepup.api;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

/**
 * Context for passing information between oidc request and response handlers.
 * 
 */
public class OidcStepUpContext {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(OidcStepUpContext.class);

    private AuthenticationRequest request;

    /** error code */
    private String error;

    /** error description */
    private String errorDescription;

    /** issuer used in response. */
    private String issuer;
    
    /** the id token received in authentication request */
    private JWTClaimsSet idToken;

    /**
     * Get the id token of the request.
     * 
     * @return id token if set, otherwise null
     */
    public JWTClaimsSet getIdToken() {
        return idToken;
    }

    /**
     * Set the id token of the authentication request.
     * 
     * @param token idtoken
     */
    public void setIdToken(JWTClaimsSet token) {
        this.idToken = token;
    }

    /**
     * Get the value of Issuer.
     * 
     * @return
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Set the value for Issuer.
     * 
     * @param iss
     *            value for Issuer
     */
    public void setIssuer(String iss) {
        this.issuer = iss;
    }

    /**
     * Get error code.
     * 
     * @return error code if set, otherwise null
     */
    public String getErrorCode() {
        return error;
    }

    /**
     * Set error code.
     * 
     * @param code
     *            for error
     */
    public void setErrorCode(String code) {
        this.error = code;
    }

    /**
     * Get error description.
     * 
     * @return error description if set, otherwise null
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Set error description.
     * 
     * @param description
     *            of error
     */
    public void setErrorDescription(String description) {
        this.errorDescription = description;
    }

    /**
     * Set received authentication request.
     * 
     * @return received authentication request
     */
    public AuthenticationRequest getRequest() {
        return request;
    }

    /**
     * Get received authentication request.
     * 
     * @param request
     *            received authentication request
     */
    public void setRequest(AuthenticationRequest request) {
        this.request = request;
    }

}

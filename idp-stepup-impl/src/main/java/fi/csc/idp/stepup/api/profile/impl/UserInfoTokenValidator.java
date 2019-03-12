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

package fi.csc.idp.stepup.api.profile.impl;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.opensaml.security.httpclient.HttpClientSecurityParameters;
import org.opensaml.security.httpclient.HttpClientSecuritySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.TokenValidator;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Validates access token by performing userinfo request and by comparing the response to a map.
 */
public class UserInfoTokenValidator implements TokenValidator {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(UserInfoTokenValidator.class);

    /** HTTP Client used to post the data. */
    @NonnullAfterInit
    private HttpClient httpClient;

    /** URL to the userinfo endpoint. */
    @NonnullAfterInit
    @NotEmpty
    private String userInfoEndpoint;

    /** HTTP client security parameters. */
    @Nullable
    private HttpClientSecurityParameters httpClientSecurityParameters;

    /** Validation map the response is compared to. */
    private Map<String, String> validationMap;

    /**
     * Set the {@link HttpClient} to use.
     * 
     * @param client client to use
     */
    public void setHttpClient(@Nonnull final HttpClient client) {
        httpClient = Constraint.isNotNull(client, "HttpClient cannot be null");
    }

    /**
     * Set the userinfo endpoint to get response from.
     * 
     * @param url userinfo endpoint to get response from
     */
    public void setUserInfoEndpoint(@Nonnull @NotEmpty final String url) {
        userInfoEndpoint =
                Constraint.isNotNull(StringSupport.trimOrNull(url), "UserInfo endpoint cannot be null or empty");
    }

    /**
     * Set the optional client security parameters.
     * 
     * @param params the new client security parameters
     */
    public void setHttpClientSecurityParameters(@Nullable final HttpClientSecurityParameters params) {
        httpClientSecurityParameters = params;
    }

    /**
     * Validation map the response is compared to.
     * 
     * @param validationMap alidation map the response is compared to.
     */
    public void setValidationMap(Map<String, String> validationMap) {
        this.validationMap = validationMap;
    }

    public JSONObject getUserInfoResponse(String accessToken) throws ClientProtocolException, IOException,
            ParseException, net.minidev.json.parser.ParseException, ComponentInitializationException {
        if (httpClient == null || userInfoEndpoint == null) {
            throw new ComponentInitializationException("HttpClient and UserInfoEndpoint cannot be null");
        }
        final HttpGet httpRequest = new HttpGet(userInfoEndpoint);
        final HttpClientContext httpContext = buildHttpContext(httpRequest);
        httpRequest.setHeader("Authorization", "Bearer " + accessToken);
        final HttpResponse response = httpClient.execute(httpRequest, httpContext);
        HttpClientSecuritySupport.checkTLSCredentialEvaluated(httpContext, httpRequest.getURI().getScheme());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String responseString = EntityUtils.toString(response.getEntity());
            log.debug("success response {}", responseString);
            return (JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(responseString);
        }
        log.error("Endpoint returned with HTTP status {}", response.getStatusLine().getStatusCode());
        return null;
    }

    /**
     * Build the {@link HttpClientContext} instance to be used by the HttpClient.
     * 
     * @param request the HTTP client request
     * @return the client context instance
     */
    @Nonnull
    private HttpClientContext buildHttpContext(@Nonnull final HttpUriRequest request) {
        final HttpClientContext clientContext = HttpClientContext.create();
        HttpClientSecuritySupport.marshalSecurityParameters(clientContext, httpClientSecurityParameters, false);
        HttpClientSecuritySupport.addDefaultTLSTrustEngineCriteria(clientContext, request);
        return clientContext;
    }

    @Override
    public boolean validate(String token, String targetUser, boolean selfServiceAction) {
        JSONObject response = null;
        try {
            response = getUserInfoResponse(token);
        } catch (ParseException | IOException | net.minidev.json.parser.ParseException
                | ComponentInitializationException e) {
            log.error("Error parsing response {}", e.getMessage());
            return false;
        }
        if (response == null) {
            log.debug("No success response to validate");
            return false;
        }
        if (selfServiceAction) {
            if (targetUser.equals(response.get("sub"))){
                log.debug("Self service actions for user {}", targetUser);
                return true;
            }
        }
        if (validationMap != null) {
            for (String validationKey : validationMap.keySet()) {
                if (!response.containsKey(validationKey)) {
                    log.debug("Response not containing required field {}", validationKey);
                    return false;
                }
                if (validationMap.get(validationKey) == null) {
                    log.debug("Response contained required field {}", validationKey);
                    return true;
                }
                Object validationObject = response.get(validationKey);
                if (validationObject instanceof String) {
                    if (validationMap.get(validationKey).equals(validationObject)) {
                        log.debug("claim {} value matches value {}, token validated for action", validationKey, validationMap.get(validationKey));
                        return true;
                    }
                    log.debug("claim {} value {} did not match value {}, token not validated for action", validationKey, validationObject, validationMap.get(validationKey));
                    return false;
                }
                if (validationObject instanceof JSONArray) {
                    if (((JSONArray)validationObject).contains(validationMap.get(validationKey))) {
                        log.debug("claim {} contains value {}, token validated for action", validationKey, validationMap.get(validationKey));
                        return true;
                    }
                    log.debug("claim {} not containing value {}, token validated for action", validationKey, validationMap.get(validationKey));
                    return false;
                }
            }
        }else {
            log.debug("Token validated, no validation map");
            return true;
        }
        //Should not ever come here unless map is empty
        log.debug("Token not validated, undetermined state");
        return false;
    }
}
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.TokenValidator;

/**
 * Validates token by performing regex match to key value.
 */
public class ValueTokenValidator implements TokenValidator {

	/** Class logger. */
	@Nonnull
	private final Logger log = LoggerFactory.getLogger(ValueTokenValidator.class);

	/** Validation map the response is compared to. */
	private Map<String, String> validationMap;

	/**
	 * Validation map the key is compared to.
	 * 
	 * @param validationMap
	 *            validation map the response is compared to.
	 */
	public void setValidationMap(Map<String, String> validationMap) {
		this.validationMap = validationMap;
	}

	@Override
	public boolean validate(String token, String key) {
		if (validationMap == null || token == null || key == null) {
			return false;
		}
		if (!validationMap.containsKey(token)) {
			return false;
		}
		String matchPattern = validationMap.get(token);
		if (matchPattern == null) {
			return true;
		}
		log.debug("Matching {} to {}",matchPattern, key);
		Pattern regex = Pattern.compile(matchPattern);
		Matcher m = regex.matcher(key);
		return m.matches();
	}
}
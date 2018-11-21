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

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ValueTokenValidatorTest {

	ValueTokenValidator valueTokenValidator;
	Map<String, String> validationMap;

	@BeforeTest
	public void setup() {
		valueTokenValidator = new ValueTokenValidator();
		validationMap = new HashMap<String, String>();
	}

	@Test
	public void testInitialStateNoMap() {
		// If map is not set we always get false
		Assert.assertFalse(valueTokenValidator.validate("token", "key"));
	}

	@Test
	public void testNoTokenInMap() {
		validationMap.put("not_token", "value");
		// If there is no hit in the map we should get false
		valueTokenValidator.setValidationMap(validationMap);
		Assert.assertFalse(valueTokenValidator.validate("token", "key"));
	}

	@Test
	public void testNullValueForTokenInMap() {
		validationMap.put("not_token", "value");
		validationMap.put("token", null);
		// If there is hit with null value we should get true always
		valueTokenValidator.setValidationMap(validationMap);
		Assert.assertTrue(valueTokenValidator.validate("token", "key"));
	}
	
	@Test
	public void testNonMatchValueForTokenInMap() {
		validationMap.put("not_token", "value");
		validationMap.put("token", "^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\\.)?[a-zA-Z]+\\.)?(example|example2)\\.com$");
		// If there is hit with null value we should get true always
		valueTokenValidator.setValidationMap(validationMap);
		Assert.assertFalse(valueTokenValidator.validate("token", "eppn@example3.com"));
	}
	
	@Test
	public void testMatchValueForTokenInMap() {
		validationMap.put("not_token", "value");
		validationMap.put("token", "^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\\.)?[a-zA-Z]+\\.)?(example|example2)\\.com$");
		// If there is hit with null value we should get true always
		valueTokenValidator.setValidationMap(validationMap);
		Assert.assertTrue(valueTokenValidator.validate("token", "eppn@example.com"));
	}

}
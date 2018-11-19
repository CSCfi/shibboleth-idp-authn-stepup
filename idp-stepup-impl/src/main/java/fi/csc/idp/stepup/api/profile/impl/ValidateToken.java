/*
 * GÉANT BSD Software License
 *
 * Copyright (c) 2017 - 2020, GÉANT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the GÉANT nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fi.csc.idp.stepup.api.profile.impl;

import javax.annotation.Nonnull;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.TokenValidator;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * This action validated access token.
 */
@SuppressWarnings("rawtypes")
public class ValidateToken extends AbstractApiAction {

	/** Class logger. */
	@Nonnull
	private final Logger log = LoggerFactory.getLogger(ValidateToken.class);

	/** Token validator. */
	private TokenValidator tokenValidator;

	/**
	 * Set token validator.
	 * 
	 * @param tokenValidator
	 */
	public void setTokenValidator(TokenValidator tokenValidator) {
		this.tokenValidator = Constraint.isNotNull(tokenValidator, "TokenValidator must not be null");
	}

	/** {@inheritDoc} */
	@Override
	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
		if (tokenValidator == null) {
			log.error("{} TokenValidator must not be null", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
			return;
		}
		if (!tokenValidator.validate(getRequest().getToken(), getRequest().getUserId())) {
			log.error("{} Failed validation token {} user {} combination", getLogPrefix(), getRequest().getToken(),
					getRequest().getUserId());
			ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
		}
	}
}

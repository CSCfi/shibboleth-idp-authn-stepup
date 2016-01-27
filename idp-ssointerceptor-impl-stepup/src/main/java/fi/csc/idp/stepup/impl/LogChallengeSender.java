package fi.csc.idp.stepup.impl;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.ChallengeSender;

public class LogChallengeSender implements ChallengeSender {

	/** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(LogChallengeSender.class);
	@Override
	public void send(String challenge, String target) {
		log.trace("Entering");
		log.info("Sending Challenge "+challenge+" to "+target);
		log.trace("Leaving");

	}

}

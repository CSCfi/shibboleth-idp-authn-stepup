package fi.csc.idp.stepup.api;

/** Interface for token validators.*/
public interface TokenValidator {

	/** Whether the token can be used to perform operation for key.*/ 
	public boolean validate(String token, String targetUser, boolean selfServiceAction);
}

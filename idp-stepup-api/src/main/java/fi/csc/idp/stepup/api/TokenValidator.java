package fi.csc.idp.stepup.api;

public interface TokenValidator {

	public boolean validate(String token, String key);
}

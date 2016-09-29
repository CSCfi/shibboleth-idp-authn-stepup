package fi.csc.idp.stepup.api;

public interface StepUpAccount {

    /**
     * Unique id of the account, may be null.
     * 
     * @return id
     */
    public long getId();

    /**
     * Set id of the account.
     * 
     * @param id
     *            of account
     */
    public void setId(long id);

    /**
     * Name of the account.
     * 
     * @return name
     */
    public String getName();

    /**
     * Set name of the account.
     * 
     * @param name
     *            name
     */
    public void setName(String name);

    /**
     * If account can be modified.
     * 
     * @return true if accounts can be modified
     */
    public boolean isEditable();
    
    /**
     * If account can be modified.
     * 
     * @return true if accounts can be modified
     */
    public void setEditable(boolean isEditable);
    

    /**
     * Set the account enabled/disabled.
     * 
     * @param isEnabled
     *            true if enabled
     */
    public void setEnabled(boolean isEnabled);

    /**
     * Status of the account
     * 
     * @return true if enabled
     */

    public boolean isEnabled();

    /**
     * Invoked when a new fresh challenge should be sent. Not relevant to all
     * implementations .
     * 
     */
    public void sendChallenge() throws Exception;

    /**
     * Invoked when user has response to challenge. Not applicable for all
     * implementations.
     * 
     * @param response
     *            to challenge
     * @return true if user has entered a valid response
     */
    public boolean verifyResponse(String response) throws Exception;

    /**
     * Target parameter for stepup operations. May be sms number, email address, shared secret or what
     * ever is applicable for the implementation. 
     * 
     * @param target
     *            to send challenge to or form of.
     */
    public void setTarget(String target);

    /**
     * Return target parameter
     * 
     * @return target
     */
    public String getTarget();

}

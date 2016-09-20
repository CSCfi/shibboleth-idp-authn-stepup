package fi.csc.idp.stepup.api;

import java.util.List;

import net.shibboleth.idp.attribute.context.AttributeContext;

public interface StepUpMethod {
    
    /**
     * This is called before any other calls
     * to initialize the Step Up Method 
     * @param attributeContext
     */
    
    public void Initialize(AttributeContext attributeContext);
    
    /**
     * Name of the stepup method.
     * @return
     */
    public String getName();
    
    /**
     * If accounts can be added or removed.
     * 
     * @return true if accounts can be added..
     */
    public boolean isEditable();
    
    /**
     * Existing accounts of the method.
     * @return list of accounts
     */
    public List<StepUpAccount> getAccounts();
    
    /**
     * Adds a new account.
     * 
     * @return new account.
     */
    public StepUpAccount addAccount();
    
    /**
     * Remove a account.
     * 
     * @param account to be removed.
     */
    public void removeAccount(StepUpAccount account);
    
}

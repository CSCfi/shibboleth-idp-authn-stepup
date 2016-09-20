package fi.csc.idp.stepup.api;

public interface StepUpAccountInitialization {
    
    /**
     * Returns path to VM file if such is needed
     * 
     * @return path
     */
    public String getVMTemplate();
    
    /**
     * Returns account initialization data if such exists
     * 
     * @return initialization data
     */
    public Object getInitializationData();
    

}

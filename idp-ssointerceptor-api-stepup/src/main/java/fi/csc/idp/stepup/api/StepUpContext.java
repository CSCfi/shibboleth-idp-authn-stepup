package fi.csc.idp.stepup.api;

import org.opensaml.messaging.context.BaseContext;

public class StepUpContext extends BaseContext{
    
    private String challenge;
    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

}

package fi.csc.idp.stepup.impl;

import fi.csc.idp.stepup.api.StepUpAccount;

public class MockAccount implements StepUpAccount {

    public String correctResponse = "response_success";
    long id = 0;
    String name;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean isEditable) {
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void sendChallenge() throws Exception {
    }

    @Override
    public boolean verifyResponse(String response) throws Exception {
        if (response == null && correctResponse == null) {
            return true;
        }
        return correctResponse.equals(response);
    }

    @Override
    public void setTarget(String target) {
    }

    @Override
    public String getTarget() {
        return null;
    }

    @Override
    public boolean isVerified() {
        // TODO Auto-generated method stub
        return false;
    }

}

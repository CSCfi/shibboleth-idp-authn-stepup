package fi.csc.idp.stepup.api;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.context.AttributeContext;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestStepUpMethodContext {

    private StepUpMethodContext stepUpMethodContext;

    @BeforeMethod
    public void setUp() {
        stepUpMethodContext = new StepUpMethodContext();
    }

    @Test
    public void testAllNull() {
        Assert.assertNull(stepUpMethodContext.getStepUpAccount());
        Assert.assertNull(stepUpMethodContext.getStepUpMethod());
        Assert.assertNull(stepUpMethodContext.getStepUpMethods());
    }

    @Test
    public void testNotNull() {
        Account account = new Account();
        Method method = new Method();
        stepUpMethodContext.setStepUpAccount(account);
        stepUpMethodContext.setStepUpMethod(method);
        Map<Principal, StepUpMethod> methods = new HashMap<Principal, StepUpMethod>();
        stepUpMethodContext.setStepUpMethods(methods);
        Assert.assertEquals(account, stepUpMethodContext.getStepUpAccount());
        Assert.assertEquals(method, stepUpMethodContext.getStepUpMethod());
        Assert.assertEquals(methods, stepUpMethodContext.getStepUpMethods());
    }

    class Method implements StepUpMethod {

        @Override
        public boolean initialize(AttributeContext attributeContext) throws Exception {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isEditable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<StepUpAccount> getAccounts() throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public StepUpAccount addAccount() throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeAccount(StepUpAccount account) {
            // TODO Auto-generated method stub

        }

    }

    class Account implements StepUpAccount {

        @Override
        public long getId() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setId(long id) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setName(String name) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isEditable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setEditable(boolean isEditable) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setEnabled(boolean isEnabled) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isEnabled() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void sendChallenge() throws Exception {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean verifyResponse(String response) throws Exception {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setTarget(String target) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getTarget() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}

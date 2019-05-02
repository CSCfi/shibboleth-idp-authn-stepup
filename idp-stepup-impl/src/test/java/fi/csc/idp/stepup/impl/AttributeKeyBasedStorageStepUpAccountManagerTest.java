package fi.csc.idp.stepup.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;

public class AttributeKeyBasedStorageStepUpAccountManagerTest {

    private AttributeKeyBasedStorageStepUpAccountManager attributeKeyBasedStorageStepUpAccountManager;

    private AttributeContext attribCtx;

    @BeforeMethod
    public void setUp() {
        attributeKeyBasedStorageStepUpAccountManager = new AttributeKeyBasedStorageStepUpAccountManager();
        attribCtx = new AttributeContext();
        final IdPAttribute attribute1 = new IdPAttribute("attr1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("foo@bar")));
        final IdPAttribute attribute2 = new IdPAttribute("attr2");
        attribute2.setValues(Arrays.asList(new ByteAttributeValue(new byte[1])));
        attribCtx.setIdPAttributes(Arrays.asList(attribute1, attribute2));
    }

    private ApplicationContext getApplicationContext() {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        return ctx;
    }

    /*
    @Test
    public void testNoStorage() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.initialize(new AttributeContext());
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("repository implementation not set, cannot add accounts", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testNoAttributeContext() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(new MockStorage());
            attributeKeyBasedStorageStepUpAccountManager.initialize(null);
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Attribute context has to be set", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testNoAttributeId() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(new MockStorage());
            attributeKeyBasedStorageStepUpAccountManager.initialize(new AttributeContext());
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Attribute Id has to be set", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testNoBeanId() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(new MockStorage());
            attributeKeyBasedStorageStepUpAccountManager.setAttributeId("not found");
            attributeKeyBasedStorageStepUpAccountManager.initialize(new AttributeContext());
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("No account bean defined", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testUnitializedAddAccount() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.addAccount();
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Storage implementation not set, cannot add accounts", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testUnitializedUpdateAccount() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.updateAccount(new MockAccount());
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Storage implementation not set, cannot add accounts", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testUpdateNullAccount() {
        boolean exception = false;
        try {
            attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(new MockStorage());
            attributeKeyBasedStorageStepUpAccountManager.updateAccount(null);
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Account cannot be null", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testNoMatchingAttributeId() throws Exception {
        attributeKeyBasedStorageStepUpAccountManager.setAttributeId("not found");
        attributeKeyBasedStorageStepUpAccountManager.setAccountID("id");
        attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(new MockStorage());
        attributeKeyBasedStorageStepUpAccountManager.initialize(new AttributeContext());
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 0);
    }

    @Test
    public void testSuccess() throws Exception {
        attributeKeyBasedStorageStepUpAccountManager.setAppContext(getApplicationContext());
        attributeKeyBasedStorageStepUpAccountManager.setAttributeId("attr1");
        attributeKeyBasedStorageStepUpAccountManager.setAccountID("ChallengeSender");
        MockStorage strg = new MockStorage();
        attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(strg);
        attributeKeyBasedStorageStepUpAccountManager.initialize(attribCtx);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 1);
        StepUpAccount account = attributeKeyBasedStorageStepUpAccountManager.addAccount();
        Assert.assertNotNull(account);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 2);
        Assert.assertEquals(strg.accounts.size(), 2);

    }

    @Test
    public void testAccountLimit1() throws Exception {
        attributeKeyBasedStorageStepUpAccountManager.setAppContext(getApplicationContext());
        attributeKeyBasedStorageStepUpAccountManager.setAttributeId("attr1");
        attributeKeyBasedStorageStepUpAccountManager.setAccountID("ChallengeSender");
        MockStorage strg = new MockStorage();
        attributeKeyBasedStorageStepUpAccountManager.setAccountLimit(1);
        attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(strg);
        attributeKeyBasedStorageStepUpAccountManager.initialize(attribCtx);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 1);
        StepUpAccount account = attributeKeyBasedStorageStepUpAccountManager.addAccount();
        Assert.assertNull(account);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 1);
        Assert.assertEquals(strg.accounts.size(), 1);

    }

    @Test
    public void testAccountLimit2() throws Exception {
        attributeKeyBasedStorageStepUpAccountManager.setAppContext(getApplicationContext());
        attributeKeyBasedStorageStepUpAccountManager.setAttributeId("attr1");
        attributeKeyBasedStorageStepUpAccountManager.setAccountID("ChallengeSender");
        MockStorage strg = new MockStorage();
        attributeKeyBasedStorageStepUpAccountManager.setAccountLimit(1);
        attributeKeyBasedStorageStepUpAccountManager.setAutoRemove(true);
        attributeKeyBasedStorageStepUpAccountManager.setStepUpAccountStorage(strg);
        attributeKeyBasedStorageStepUpAccountManager.initialize(attribCtx);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 1);
        StepUpAccount account = attributeKeyBasedStorageStepUpAccountManager.addAccount();
        Assert.assertNotNull(account);
        Assert.assertEquals(attributeKeyBasedStorageStepUpAccountManager.getAccounts().size(), 1);
        Assert.assertEquals(strg.accounts.size(), 1);
        Assert.assertEquals(account, attributeKeyBasedStorageStepUpAccountManager.getAccounts().get(0));

    }

    class MockStorage implements StepUpAccountStorage {

        public List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();

        @Override
        public void add(StepUpAccount account, String key) throws Exception {
            accounts.add(account);

        }

        @Override
        public void remove(StepUpAccount account, String key) throws Exception {
            accounts.remove(account);

        }

        @Override
        public void update(StepUpAccount account, String key) throws Exception {
        }

        @Override
        public <T> List<StepUpAccount> getAccounts(String key, Class<T> aClass) throws Exception {
            Object obj = aClass.newInstance();
            if (!(obj instanceof StepUpAccount)) {
                throw new Exception("Unable to instantiate StepUpAccount");
            }
            StepUpAccount stepUpAccount = (StepUpAccount) obj;
            stepUpAccount.setId(1);
            stepUpAccount.setName("name");
            stepUpAccount.setEnabled(false);
            stepUpAccount.setTarget("target");
            stepUpAccount.setEditable(false);
            accounts.add(stepUpAccount);
            return accounts;
        }

    }
    */

}

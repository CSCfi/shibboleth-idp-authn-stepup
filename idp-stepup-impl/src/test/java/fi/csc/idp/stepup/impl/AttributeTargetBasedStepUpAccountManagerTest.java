package fi.csc.idp.stepup.impl;

import java.util.Arrays;

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AttributeTargetBasedStepUpAccountManagerTest {

    private AttributeTargetBasedStepUpAccountManager attributeTargetBasedStepUpAccountManager;

    private AttributeContext attribCtx;

    @BeforeMethod
    public void setUp() {
        attributeTargetBasedStepUpAccountManager = new AttributeTargetBasedStepUpAccountManager();
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
    public void testNoAttributeContext() {
        boolean exception = false;
        try {
            attributeTargetBasedStepUpAccountManager.initialize(null);
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("Attribute context has to be set", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }*/

    /*
    @Test
    public void testNoAttributeId() {
        boolean exception = false;
        try {
            attributeTargetBasedStepUpAccountManager.initialize(new AttributeContext());
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
            attributeTargetBasedStepUpAccountManager.setAttributeId("not found");
            attributeTargetBasedStepUpAccountManager.initialize(new AttributeContext());
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals("No account bean defined", e.getMessage());
        }
        Assert.assertEquals(exception, true);
    }

    @Test
    public void testNoMatchingAttributeId() throws Exception {
        attributeTargetBasedStepUpAccountManager.setAttributeId("not found");
        attributeTargetBasedStepUpAccountManager.setAccountID("id");
        attributeTargetBasedStepUpAccountManager.initialize(new AttributeContext());
        Assert.assertEquals(attributeTargetBasedStepUpAccountManager.getAccounts().size(), 0);
    }

    @Test
    public void testAccountInitialized() throws Exception {
        attributeTargetBasedStepUpAccountManager.setAppContext(getApplicationContext());
        attributeTargetBasedStepUpAccountManager.setAttributeId("attr1");
        attributeTargetBasedStepUpAccountManager.setAccountID("ChallengeSender");
        // attribute found but set account implementation missing
        attributeTargetBasedStepUpAccountManager.initialize(attribCtx);
        Assert.assertEquals(attributeTargetBasedStepUpAccountManager.getAccounts().size(), 1);

    }
    */

}

package fi.csc.idp.stepup.impl;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TestAbstractStepUpAccountManager {

    private TestStepUpAccountManager testStepUpAccountManager;
    
    @BeforeMethod
    public void setUp()  {
        testStepUpAccountManager = new TestStepUpAccountManager();
    }

    @Test
    public void testUnitialized() throws Exception   {
        Assert.assertNull(testStepUpAccountManager.getAccountID());
        Assert.assertNotNull(testStepUpAccountManager.getAccounts());
        Assert.assertEquals(testStepUpAccountManager.getAccounts().size(), 0);
        Assert.assertNull(testStepUpAccountManager.getAppContext());
        Assert.assertNull(testStepUpAccountManager.getName());
        Assert.assertTrue(!testStepUpAccountManager.isEditable());        
    }

    private ApplicationContext getApplicationContext(){
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        return ctx;
    }
    
    @Test
    public void testSetters()   {
        ApplicationContext ctx=getApplicationContext();
        testStepUpAccountManager.setAccountID("id");
        testStepUpAccountManager.setName("methodName");
        testStepUpAccountManager.setAppContext(ctx);
        Assert.assertEquals("id",testStepUpAccountManager.getAccountID());
        Assert.assertEquals("methodName",testStepUpAccountManager.getName()); 
        Assert.assertEquals(ctx,testStepUpAccountManager.getAppContext()); 
        
    }
    
    @Test
    public void testDefaultAccountEditing() throws Exception   {
        Assert.assertNull(testStepUpAccountManager.addAccount());
        Assert.assertEquals(testStepUpAccountManager.getAccounts().size(), 0); 
    }
    
    @Test
    public void testInitialize() throws Exception   {
       testStepUpAccountManager.setAppContext(getApplicationContext()); 
       testStepUpAccountManager.setAccountID("ChallengeSender"); 
       testStepUpAccountManager.initialize(null);
       Assert.assertEquals(testStepUpAccountManager.getAccounts().size(), 1);   
    }
    
        
    class TestStepUpAccountManager extends AbstractStepUpAccountManager {
        
        
    }
    
    
}

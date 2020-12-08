package fi.csc.idp.stepup.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.mockito.Mockito;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.openid.connect.sdk.ClaimsRequest.Entry;
import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;

public class AttributeKeyBasedStorageStepUpAccountManagerTest {

    private AttributeKeyBasedStorageStepUpAccountManager manager;
    Collection<Entry> claims;

    @BeforeMethod
    public void setUp() throws Exception {
        manager = new AttributeKeyBasedStorageStepUpAccountManager();
        StepUpAccountStorage storage = Mockito.mock(StepUpAccountStorage.class);
        manager.setStepUpAccountStorage(storage);
        Mockito.doReturn(Mockito.mock(StepUpAccount.class)).when(storage).getAccount(Mockito.any(), Mockito.any());
        manager.setAccountID("LogStepUpAccount");
        manager.setAppContext(new ClassPathXmlApplicationContext("applicationContext.xml"));
        claims = new ArrayList<Entry>();
        claims.add(new Entry("sub", ClaimRequirement.ESSENTIAL, null, "XYZ"));
        claims.add(new Entry("eppn", ClaimRequirement.ESSENTIAL, null, "eppn@example.com"));
        manager.setClaimName("eppn");
    }

    @Test
    public void testInitialisationSuccess() throws Exception {
        manager.initialize(claims);
        Assert.assertNotNull(manager.getAccount());
    }

    @Test
    public void testRemove() throws Exception {
        manager.initialize(claims);
        manager.removeAccount(manager.getAccount());
        Assert.assertNull(manager.getAccount());
    }

    @Test
    public void testAddExisting() throws Exception {
        manager.initialize(claims);
        Assert.assertNull(manager.addAccount());
    }

    @Test
    public void testAddAutoRemove() throws Exception {
        manager.setAutoRemove(true);
        manager.initialize(claims);
        Assert.assertNotNull(manager.addAccount());
    }

    @Test
    public void testInitialisationSuccessNoAccount() throws Exception {
        manager.setClaimName("not_eppn");
        manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

    @Test(expectedExceptions = Exception.class)
    public void testInitialisationFailNoStorage() throws Exception {
        manager.setStepUpAccountStorage(null);
        manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

    @Test(expectedExceptions = Exception.class)
    public void testInitialisationFailNoAccountID() throws Exception {
        manager.setAccountID(null);
        manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

    @Test(expectedExceptions = Exception.class)
    public void testInitialisationFailNoClaimName() throws Exception {
        manager.setClaimName(null);
        manager.initialize(claims);
        Assert.assertNull(manager.getAccount());
    }

}

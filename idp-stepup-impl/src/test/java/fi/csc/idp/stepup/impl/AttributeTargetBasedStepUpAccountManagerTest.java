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

public class AttributeTargetBasedStepUpAccountManagerTest {

    private AttributeTargetBasedStepUpAccountManager manager;
    Collection<Entry> claims;

    @BeforeMethod
    public void setUp() throws Exception {
        manager = new AttributeTargetBasedStepUpAccountManager();
        StepUpAccountStorage storage = Mockito.mock(StepUpAccountStorage.class);
        Mockito.doReturn(Mockito.mock(StepUpAccount.class)).when(storage).getAccount(Mockito.any(), Mockito.any());
        manager.setAccountID("LogStepUpAccount");
        manager.setAppContext(new ClassPathXmlApplicationContext("applicationContext.xml"));
        claims = new ArrayList<Entry>();
        claims.add(new Entry("sub", ClaimRequirement.ESSENTIAL, null, "XYZ"));
        claims.add(new Entry("mail", ClaimRequirement.ESSENTIAL, null, "mail@example.com"));
        manager.setClaimName("mail");
    }

    @Test
    public void testInitialisationSuccess() throws Exception {
        manager.initialize(claims);
        Assert.assertNotNull(manager.getAccount());
    }

    @Test
    public void testInitialisationSuccessNoAccount() throws Exception {
        manager.setClaimName("not_eppn");
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

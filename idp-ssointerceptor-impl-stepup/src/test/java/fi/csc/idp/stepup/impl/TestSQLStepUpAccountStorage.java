package fi.csc.idp.stepup.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import fi.csc.idp.stepup.api.StepUpAccount;

public class TestSQLStepUpAccountStorage {

    private SQLStepUpAccountStorage sqlStepUpAccountStorage;
    private Server server;

    @BeforeMethod
    public void setUp() throws Exception {

        server = new Server();
        HsqlProperties props = new HsqlProperties();
        props.setProperty("connection.url", "jdbc:hsqldb:mem:test");
        props.setProperty("connection.username", "sa");
        props.setProperty("connection.password", "test");
        props.setProperty("connection.pool_size", "100");
        props.setProperty("dialect", "org.hibernate.dialect.HSQLDialect");
        server.setProperties(props);
        server.start();
        Thread.sleep(5000);
        Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "test");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE storage (test_id int IDENTITY PRIMARY KEY, test_user varchar(64), test_name varchar(64), test_enabled boolean, test_editable boolean, test_target varchar(64));");
        stmt.close();
        conn.close();
        sqlStepUpAccountStorage = new SQLStepUpAccountStorage();

    }

    @AfterMethod
    public void close() {
        server.stop();
    }

    private void initStepUpStorageConnection() {
        sqlStepUpAccountStorage.setJdbcUrl("jdbc:hsqldb:mem:test");
        sqlStepUpAccountStorage.setPoolSize(1);
        sqlStepUpAccountStorage.setUserName("sa");
        sqlStepUpAccountStorage.setPassword("test");
    }

    private void initStepUpStorageStatements() {
        sqlStepUpAccountStorage
                .setUpdateStatement("UPDATE storage SET test_name=?, test_enabled=?, test_editable=?, test_target=?, test_user=? WHERE test_id=?");
        sqlStepUpAccountStorage
                .setAddStatement("INSERT INTO storage (test_name,test_enabled,test_editable,test_target,test_user) VALUES(?,?,?,?,?)");
        sqlStepUpAccountStorage.setRemoveStatement("DELETE FROM storage WHERE test_id=?");
        sqlStepUpAccountStorage
                .setListStatement("SELECT test_id id, test_user user, test_name name, test_enabled enabled, test_editable editable, test_target target FROM storage WHERE test_user=?");
    }

    @Test
    public void test() throws Exception {
        initStepUpStorageConnection();
        initStepUpStorageStatements();
        MockAccount ma1 = new MockAccount();
        ma1.setName("ma1");
        MockAccount ma2 = new MockAccount();
        // Insert accounts to storage
        sqlStepUpAccountStorage.add(ma1, "user1");
        sqlStepUpAccountStorage.add(ma2, "user1");
        sqlStepUpAccountStorage.add(new MockAccount(), "user2");
        // Check that accounts may be found from storage
        Assert.assertEquals(sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 2);
        Assert.assertEquals(sqlStepUpAccountStorage.getAccounts("user2", MockAccount.class).size(), 1);
        Assert.assertEquals(sqlStepUpAccountStorage.getAccounts("user3", MockAccount.class).size(), 0);
        List<StepUpAccount> accounts = sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class);
        // can we locate account named ma1 and modify it
        long id = -1;
        for (StepUpAccount account : accounts) {
            if ("ma1".equals(account.getName())) {
                id = account.getId();
                account.setName("ma1_updated");
                sqlStepUpAccountStorage.update(account, "user1");
            }
        }
        Assert.assertEquals(sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 2);
        boolean found = false;
        accounts = sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class);
        StepUpAccount acc = null;
        for (StepUpAccount account : accounts) {
            if ("ma1_updated".equals(account.getName())) {
                acc = account;
                found = true;
                Assert.assertEquals(id, account.getId());
            }
        }
        Assert.assertTrue(found);
        // remove the updated account
        found = false;
        sqlStepUpAccountStorage.remove(acc, "user1");
        Assert.assertEquals(sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class).size(), 1);
        accounts = sqlStepUpAccountStorage.getAccounts("user1", MockAccount.class);
        for (StepUpAccount account : accounts) {
            if ("ma1_updated".equals(account.getName())) {
                found = true;
                Assert.assertEquals(id, account.getId());
            }
        }
        Assert.assertTrue(!found);

    }

}

/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpAccountStorage;

/** class implementing challenge sending by writing it to log. */
public class SQLStepUpAccountStorage implements StepUpAccountStorage {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(SQLStepUpAccountStorage.class);

    private static DataSource datasource;

    private static String jdbcUrl;
    private static String userName;
    private static String password;
    private static int poolSize;
    private static String addStatement;
    private static String updateStatement;
    private static String removeStatement;
    private static String listStatement;

    public void setJdbcUrl(String jdbcUrl) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.jdbcUrl = jdbcUrl;
    }

    public void setPassword(String password) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.password = password;
    }

    public void setUserName(String userName) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.userName = userName;
    }

    public void setPoolSize(int poolSize) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.poolSize = poolSize;
    }

    
    public void setAddStatement(String addStatement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.addStatement = addStatement;
    }

    public void setUpdateStatement(String updateStatement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.updateStatement = updateStatement;
    }

    public void setRemoveStatement(String removeStatement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.removeStatement = removeStatement;
    }

    public void setListStatement(String listStatement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.listStatement = listStatement;
    }

    public DataSource getDataSource() {
        log.trace("Entering");
        if (datasource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(userName);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setAutoCommit(true);
            datasource = new HikariDataSource(config);
        }
        log.trace("Leaving");
        return datasource;
    }

   
    @Override
    public void add(StepUpAccount account, String key) throws Exception {
        log.trace("Entering");
        PreparedStatement add=getDataSource().getConnection().prepareStatement(addStatement);
        add.setString(1, account.getName());
        add.setBoolean(2, account.isEnabled());
        add.setBoolean(3, account.isEditable());
        add.setString(4, account.getTarget());
        add.setString(5, key);
        add.executeUpdate();
        getDataSource().getConnection().commit();
        log.trace("Leaving");
    }

    @Override
    public void remove(StepUpAccount account, String key) throws Exception {
        log.trace("Entering");
        PreparedStatement add=getDataSource().getConnection().prepareStatement(removeStatement);
        add.setLong(1, account.getId());
        add.executeUpdate();
        getDataSource().getConnection().commit();
        log.trace("Leaving");

    }

    @Override
    public void update(StepUpAccount account, String key) throws Exception {
        log.trace("Entering");
        PreparedStatement add=getDataSource().getConnection().prepareStatement(updateStatement);
        add.setString(1, account.getName());
        add.setBoolean(2, account.isEnabled());
        add.setBoolean(3, account.isEditable());
        add.setString(4, account.getTarget());
        add.setString(5, key);
        add.setLong(1, account.getId());
        add.executeUpdate();
        getDataSource().getConnection().commit();
        log.trace("Leaving");
    }

    @Override
    public <T> List<StepUpAccount> getAccounts(String key, Class<T> aClass) throws Exception {
        log.trace("Entering");
        log.debug("About to read accounts for "+key);
        List<StepUpAccount> accounts= new ArrayList<StepUpAccount>();
        PreparedStatement list=getDataSource().getConnection().prepareStatement(listStatement);
        list.setString(1, key);
        ResultSet rs=list.executeQuery();
        while(rs.next()){
            Object obj=aClass.newInstance();
            if (! (obj instanceof StepUpAccount)){
                throw new Exception("Unable to instantiate StepUpAccount");
            }
            StepUpAccount stepUpAccount=(StepUpAccount)obj;
            stepUpAccount.setId(rs.getInt("id"));
            stepUpAccount.setName(rs.getString("name"));
            stepUpAccount.setEnabled(rs.getBoolean("enabled"));
            stepUpAccount.setTarget(rs.getString("target"));
            //This is set last 
            stepUpAccount.setEditable(rs.getBoolean("editable"));
            accounts.add(stepUpAccount);
        }
        log.trace("Leaving");
        return accounts;
    }

}

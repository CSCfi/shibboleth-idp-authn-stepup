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

import java.sql.Connection;
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

/** SQL implementation of Step Up Account storage. */
public class SQLStepUpAccountStorage implements StepUpAccountStorage {

    /** Class logger. */
    @Nonnull
    private static final Logger log = LoggerFactory.getLogger(SQLStepUpAccountStorage.class);
    /** datasource constructed. */
    private static DataSource datasource;
    /** url for the database connection. */
    private static String jdbcUrl;
    /** username for database connection. */
    private static String userName;
    /** password for database connection. */
    private static String password;
    /** pool size for database connection. */
    private static int poolSize;
    /** statement for adding items. */
    private static String addStatement;
    /** statement for updating items. */
    private static String updateStatement;
    /** statement for removing items. */
    private static String removeStatement;
    /** statement for listing items. */
    private static String listStatement;

    /**
     * Setter for database connection url.
     * 
     * @param url
     *            for connection
     */
    public void setJdbcUrl(String url) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.jdbcUrl = url;
    }

    /**
     * Setter for database connection password.
     * 
     * @param psswd
     *            for connection
     */
    public void setPassword(String psswd) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.password = psswd;
    }

    /**
     * Setter for database connection user name.
     * 
     * @param name
     *            user name for connection
     */
    public void setUserName(String name) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.userName = name;
    }

    /**
     * Setter for database connection pool size.
     * 
     * @param size
     *            pool size for database connection
     */
    public void setPoolSize(int size) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.poolSize = size;
    }

    /**
     * Setter for add statement.
     * 
     * @param statement
     *            add statement.
     */
    public void setAddStatement(String statement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.addStatement = statement;
    }

    /**
     * Setter for update statement.
     * 
     * @param statement
     *            update statement.
     */
    public void setUpdateStatement(String statement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.updateStatement = statement;
    }

    /**
     * Setter for remove statement.
     * 
     * @param statement
     *            remove statement.
     */
    public void setRemoveStatement(String statement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.removeStatement = statement;
    }

    /**
     * Setter for list statement.
     * 
     * @param statement
     *            list statement.
     */
    public void setListStatement(String statement) {
        log.trace("Entering & Leaving");
        SQLStepUpAccountStorage.listStatement = statement;
    }

    /**
     * Get the datasource.
     * 
     * @return datasource
     */
    private DataSource getDataSource() {
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
        Connection conn=getDataSource().getConnection();
        PreparedStatement add = conn.prepareStatement(addStatement);
        add.setString(1, account.getName());
        add.setBoolean(2, account.isEnabled());
        add.setBoolean(3, account.isEditable());
        add.setString(4, account.getTarget());
        add.setString(5, key);
        add.executeUpdate();
        conn.commit();
        add.close();
        conn.close();
        log.trace("Leaving");
    }

    @Override
    public void remove(StepUpAccount account, String key) throws Exception {
        log.trace("Entering");
        Connection conn=getDataSource().getConnection();
        PreparedStatement remove = conn.prepareStatement(removeStatement);
        remove.setLong(1, account.getId());
        remove.executeUpdate();
        conn.commit();
        remove.close();
        conn.close();
        log.trace("Leaving");

    }

    @Override
    public void update(StepUpAccount account, String key) throws Exception {
        log.trace("Entering");
        Connection conn=getDataSource().getConnection();
        PreparedStatement update = conn.prepareStatement(updateStatement);
        update.setString(1, account.getName());
        update.setBoolean(2, account.isEnabled());
        update.setBoolean(3, account.isEditable());
        update.setString(4, account.getTarget());
        update.setString(5, key);
        update.setLong(6, account.getId());
        update.executeUpdate();
        conn.commit();
        update.close();
        conn.close();
        log.trace("Leaving");
    }

    @Override
    public <T> List<StepUpAccount> getAccounts(String key, Class<T> aClass) throws Exception {
        log.trace("Entering");
        log.debug("About to read accounts for " + key);
        Connection conn=getDataSource().getConnection();
        List<StepUpAccount> accounts = new ArrayList<StepUpAccount>();
        PreparedStatement list = conn.prepareStatement(listStatement);
        list.setString(1, key);
        ResultSet rs = list.executeQuery();
        while (rs.next()) {
            Object obj = aClass.newInstance();
            if (!(obj instanceof StepUpAccount)) {
                conn.close();
                throw new Exception("Unable to instantiate StepUpAccount");
            }
            StepUpAccount stepUpAccount = (StepUpAccount) obj;
            stepUpAccount.setId(rs.getInt("id"));
            stepUpAccount.setName(rs.getString("name"));
            stepUpAccount.setEnabled(rs.getBoolean("enabled"));
            stepUpAccount.setTarget(rs.getString("target"));
            // This is set last
            stepUpAccount.setEditable(rs.getBoolean("editable"));
            accounts.add(stepUpAccount);
        }
        log.trace("Leaving");
        rs.close();
        list.close();
        conn.close();
        return accounts;
    }

}

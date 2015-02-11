package com.globalsight.selenium.functions;

/*
 * FileName: CommonFuncs.java
 * Author:Jester
 * Methods: initSelenium(),EndSelenium(),loginSystemWithSuperAdmin(),
 * loginSystemWithPM(), loginSystemWithAnyone(),login()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-13  First Version  Jester
 */

import java.sql.Connection;

import jodd.db.pool.CoreConnectionPool;

import com.globalsight.selenium.pages.Login;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class CommonFuncs extends BasicFuncs
{
    public static String LONG_WAIT;
    public static String SHORT_WAIT;
    public static String MEDIUM_WAIT;
    private static CoreConnectionPool dbPool;
    private static Selenium selenium;

    static
    {
        LONG_WAIT = ConfigUtil.getConfigData("longWait");
        SHORT_WAIT = ConfigUtil.getConfigData("shortWait");
        MEDIUM_WAIT = ConfigUtil.getConfigData("middleWait");

        if (dbPool == null) {
        	try {
                dbPool = new CoreConnectionPool();
                dbPool.setDriver("com.mysql.jdbc.Driver");
                dbPool.setMaxConnections(20);
                dbPool.setMinConnections(5);
                dbPool.setUrl(ConfigUtil.getConfigData("databaseUrl"));
                dbPool.setUser(ConfigUtil.getConfigData("databaseUser"));
                dbPool.setPassword(ConfigUtil.getConfigData("databasePassword"));
                dbPool.init();
			} catch (Exception e) {
				dbPool = null;
			}
        }
    }

    /**
     * Initialize the selenium before the test start.
     */
    public static Selenium getSelenium()
    {
        if (selenium == null) {
            Selenium defaultSelenium = new DefaultSelenium(
                    ConfigUtil.getConfigData("seleniumHost"),
                    Integer.parseInt(ConfigUtil.getConfigData("seleniumPort")),
                    ConfigUtil.getConfigData("browser"),
                    ConfigUtil.getConfigData("serverUrl"));
            defaultSelenium.start();
            defaultSelenium.setSpeed(ConfigUtil.getConfigData("delayBetweenOperations"));
            defaultSelenium.windowMaximize();
            
            selenium = defaultSelenium;
        }

        return selenium;
    }

    /**
     * Stop the selenium once the test finished. close the Navigator and
     * selenium session.
     */
    public static void endSelenium(Selenium selenium)
    {
        selenium.close();
        selenium.stop();
    }

    /**
     * Get database connection
     * @return java.sql.Connection Database connection
     */
    public static Connection getConnection() {
        return dbPool == null ? null : dbPool.getConnection();
    }
    
    /**
     * Free database connection
     * @param conn Database connection
     */
    public static void freeConnection(Connection conn) {
        if (dbPool != null)
            dbPool.closeConnection(conn);
    }
    
    /**
     * Login system with superadmin
     * 
     * @param selenium
     */
    public static void loginSystemWithSuperAdmin(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("superAdminName");
        String password = ConfigUtil.getConfigData("superAdminPassword");
        login(selenium, loginName, password);
    }

    /**
     * Login system with admin
     * 
     * @param selenium
     */
    public static void loginSystemWithAdmin(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("adminName");
        String password = ConfigUtil.getConfigData("adminPassword");
        login(selenium, loginName, password);
    }

    /**
     * Login system with pm
     * 
     * @param selenium
     */
    public static void loginSystemWithPM(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("pmName");
        String password = ConfigUtil.getConfigData("pmPassword");
        login(selenium, loginName, password);
    }

    /**
     * Login system with anyone
     * 
     * @param selenium
     */
    public static void loginSystemWithAnyone(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("anyoneName");
        String password = ConfigUtil.getConfigData("anyonePassword");
        login(selenium, loginName, password);
    }

    public static void loginSystemWithReviewer(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("reviewerName");
        String password = ConfigUtil.getConfigData("reviewerPassword");
        login(selenium, loginName, password);
    }

    public static void logoutSystem(Selenium selenium)
    {
        selenium.click(MainFrame.LOG_OUT_LINK);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }
    }

    public static void login(Selenium selenium, String loginName,
            String password)
    {
        selenium.open(ConfigUtil.getConfigData("loginUrl"));
        selenium.type(Login.LOGIN_NAME_TEXT, loginName);
        selenium.type(Login.LOGIN_PASSWORD_TEXT, password);
        selenium.click(Login.LOGIN_SUBMIT_BUTTON);
        selenium.waitForPageToLoad(SHORT_WAIT);
    }
}

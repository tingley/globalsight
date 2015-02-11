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

import com.globalsight.selenium.pages.Login;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class CommonFuncs extends BasicFuncs
{
    public static String LONG_WAIT;
    public static String SHORT_WAIT;
    public static String MEDIUM_WAIT;

    static
    {
        LONG_WAIT = ConfigUtil.getConfigData("LONG_WAIT");
        SHORT_WAIT = ConfigUtil.getConfigData("SHORT_WAIT");
        MEDIUM_WAIT = ConfigUtil.getConfigData("MID_WAIT");
    }

    /**
     * Initialize the selenium before the test start.
     */
    public static Selenium initSelenium()
    {
        Selenium selenium = new DefaultSelenium(ConfigUtil
                .getConfigData("SELENIUM_HOST"), Integer.parseInt(ConfigUtil
                .getConfigData("SELENIUM_PORT")), ConfigUtil.getConfigData("BROWSER"),
                ConfigUtil.getConfigData("SERVER_URL"));  
        selenium.start();
        selenium.setSpeed(ConfigUtil.getConfigData("DELAY_BETWEEN_OPERATIONS"));
        selenium.windowMaximize();

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
     * Login system with superadmin
     * 
     * @param selenium
     */
    public static void loginSystemWithSuperAdmin(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("SUPERADMIN_LOGIN_NAME");
        String password = ConfigUtil.getConfigData("superadmin_password");
        login(selenium, loginName, password);
    }

    
    
    /**
     * Login system with admin
     * 
     * @param selenium
     */
    public static void loginSystemWithAdmin(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("admin_login_name");
        String password = ConfigUtil.getConfigData("admin_password");
        login(selenium, loginName, password);
    }

    /**
     * Login system with pm
     * 
     * @param selenium
     */
    public static void loginSystemWithPM(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("pm_login_name");
        String password = ConfigUtil.getConfigData("pm_password");
        login(selenium, loginName, password);
    }

    /**
     * Login system with anyone
     * 
     * @param selenium
     */
    public static void loginSystemWithAnyone(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("anyone_login_name");
        String password = ConfigUtil.getConfigData("anyone_password");
        login(selenium, loginName, password);
    }

    
    public static void loginSystemWithReviewer(Selenium selenium)
    {
        String loginName = ConfigUtil.getConfigData("reviewer_login_name");
        String password = ConfigUtil.getConfigData("reviewer_password");
        login(selenium, loginName, password);
    }
    
    public static void logoutSystem(Selenium selenium)
    {
        selenium.click(MainFrame.LogOut_LINK);
    	if (selenium.isConfirmationPresent())
    	{
    		selenium.getConfirmation();
    	}
    }

    public static void login(Selenium selenium, String loginName,
            String password)
    {
        selenium.open(ConfigUtil.getConfigData("LOGIN_URL"));
        selenium.type(Login.Name_TEXT_FIELD, loginName);
        selenium.type(Login.Password_TEXT_FIELD, password);
        selenium.click(Login.Login_BUTTON);
        selenium.waitForPageToLoad(SHORT_WAIT);
    }
}

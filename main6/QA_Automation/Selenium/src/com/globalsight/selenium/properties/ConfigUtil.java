package com.globalsight.selenium.properties;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.testng.Reporter;

public class ConfigUtil
{
    /*
     * private static final ResourceBundle CONFIG_BUNDLE = ResourceBundle
     * .getBundle("com.globalsight.selenium.properties.Config");
     */
    private static final ResourceBundle ConfigData_BUNDLE = ResourceBundle
            .getBundle("com.globalsight.selenium.properties.ConfigAndCommonData");

    /**
     * Get the Configure variables from the ConfigAndCommonData.properties file.
     * 
     * There are some configuration and common data in
     * ConfigAndCommonData.properties file.
     */
    public static String getConfigData(String key)
    {
        try
        {
            return ConfigData_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            Reporter.log("Failed to Get the " + key
                    + " from the .properties file!");
            return null;
        }
    }

    /**
     * Get the variables from the specific the .properties file.
     */
    public static String getDataInCase(String testCaseName, String key)
    {
        try
        {
            ResourceBundle vari_Bundle = ResourceBundle.getBundle(testCaseName
                    .replace("testcases", "properties"));
            return vari_Bundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            Reporter.log("Failed to Get the " + key + " from the "
                    + testCaseName + ".properties file!");
            return null;
        }
        catch (Exception e)
        {
            Reporter.log(e.getMessage());
            return null;
        }
    }

    /**
     * Get the variables from the specific the .properties file which need the
     * Path variable.
     */
    public static String getPath(String testCaseName, String key)
    {
        try
        {
            ResourceBundle TestData_BUNDLE2 = ResourceBundle
                    .getBundle(testCaseName.replace("testcases", "properties"));
            return ConfigData_BUNDLE.getString("Base_Path")
                    + TestData_BUNDLE2.getString(key);
        }
        catch (MissingResourceException e)
        {
            Reporter.log("Failed to Get the " + key + " from the "
                    + testCaseName + ".properties file!");
            return null;
        }
        catch (Exception e)
        {
            Reporter.log(e.getMessage());
            return null;
        }
    }
}

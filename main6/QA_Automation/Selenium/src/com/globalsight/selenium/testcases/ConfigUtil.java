package com.globalsight.selenium.testcases;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jodd.util.StringUtil;

import org.testng.Reporter;

public class ConfigUtil
{
    private static final ResourceBundle CONFIG_DATA_BUNDLE = ResourceBundle
            .getBundle(PropertyFileConfiguration.CONFIG_PROPERTIES);
    
    private static String propertiesFile = PropertyFileConfiguration.SMOKE_TEST_PROPERTIES;

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
            return CONFIG_DATA_BUNDLE.getString(key);
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
            return CONFIG_DATA_BUNDLE.getString("Base_Path")
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

    /**
     * Get the variables from the specific the .properties file which need the
     * Path variable.
     */
    public static String getPath(String filePath)
    {
        try
        {
            return CONFIG_DATA_BUNDLE.getString("Base_Path") + filePath;
        }
        catch (MissingResourceException e)
        {
            Reporter.log("Failed to Get the " + filePath + ".properties file!");
            return null;
        }
        catch (Exception e)
        {
            Reporter.log(e.getMessage());
            return null;
        }
    }

    public static String getProperty(String propertyName) {
        if (StringUtil.isEmpty(propertiesFile) || StringUtil.isEmpty(propertyName))
            return null;
        else
            return getDataInCase(propertiesFile, propertyName);
    }

    public static void setPropertyFile(String propertiesFile)
    {
        ConfigUtil.propertiesFile = propertiesFile;
    }

    public static String getPropertyFile()
    {
        return propertiesFile;
    }
}

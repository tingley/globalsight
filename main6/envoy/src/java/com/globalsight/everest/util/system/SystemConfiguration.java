/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.everest.util.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.util.GeneralException;
import com.globalsight.util.system.ConfigException;

/**
 * This class defines the method that client objects can use to access the
 * system configuration parameters. It also provides a factory method for client
 * objects to obtain an instance of a concrete subclass.
 */
public abstract class SystemConfiguration implements SystemConfigParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(SystemConfiguration.class);

    /**
     * Comma delimiter (used in property files)
     */
    public static final String DEFAULT_DELIMITER = ",";

    /**
     * This is the main GlobalSight configuration file -- envoy.properties
     */
    public static final String SYSTEM_CONFIG = "/properties/envoy.properties";

    /**
     * This is the GENERATED GlobalSight configuration file --
     * envoy_generated.properties
     */
    static final String SYSTEM_CONFIG_GENERATED = "/properties/envoy_generated.properties";

    // One and only instance of this class
    static SystemConfiguration m_systemConfig = null;

    /**
     * Keeps a records of all the other SystemConfiguration objects in use.
     */
    private static HashMap s_otherConfigs = new HashMap();

    private static String[] companyProfileNames = null;

    private static final String SQL_QUERY_PARAM = "SELECT value FROM SYSTEM_PARAMETER WHERE name=? AND company_id=?";

    /**
     * Default SystemConfiguration constructor.
     */
    SystemConfiguration()
    {
        super();
    }

    /**
     * Debug hook to replace the standard DB-backed configuration with something
     * else. Use for testing only!
     * 
     * @param config
     */
    public static void setDebugInstance(SystemConfiguration config)
    {
        m_systemConfig = config;
    }

    /**
     * Use for testing only!
     * 
     * @param config
     */
    public static void setDebugMap(HashMap config)
    {
        s_otherConfigs = config;
    }

    /**
     * Get the main System4 SystemConfiguration object.
     * 
     * @return an instance of EnvoySystemConfiguration
     * @exception GeneralException
     */
    public static SystemConfiguration getInstance() throws GeneralException
    {
        if (m_systemConfig == null)
        {
            String[] propFiles =
            { SYSTEM_CONFIG, SYSTEM_CONFIG_GENERATED };
            m_systemConfig = new EnvoySystemConfiguration(propFiles);
        }

        return m_systemConfig;
    }

    /**
     * Gets the SystemConfiguration object associated with the given name
     * 
     * @param p_name
     *            (could be of a propertyFile)
     * @return an instance of DynamicPropertiesSystemConfiguration
     */
    public static SystemConfiguration getInstance(String p_name)
    {
        String key = p_name.toUpperCase();

        SystemConfiguration sc = (DynamicPropertiesSystemConfiguration) s_otherConfigs
                .get(key);
        if (sc == null)
        {
            try
            {
                sc = new DynamicPropertiesSystemConfiguration(p_name);
                s_otherConfigs.put(key, sc);
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "Could not create DynamicPropertiesSystemConfiguration class for name "
                                + p_name, e);
                sc = null;
            }
        }

        return sc;
    }

    /**
     * copy all the properties file into company level folder. For issue
     * "Properties files separation by Company"
     * 
     * @param p_companyName
     */
    public static void copyPropertiesToCompany(String p_companyName)
    {
        setCompanyProfileNames();
        if (companyProfileNames != null)
        {
            for (int i = 0; i < companyProfileNames.length; i++)
            {
                copyPropertiesToCompany(p_companyName, "/properties/"
                        + companyProfileNames[i]);
            }
        }
    }

    /**
     * set companyProfiles. For issue "Properties files separation by Company"
     */
    private static void setCompanyProfileNames()
    {
        if (companyProfileNames == null)
        {
            companyProfileNames = getInstance().getStrings(
                    "profile.level.company");
        }
    }

    /**
     * copy one properties file into company level folder. For issue "Properties
     * files separation by Company"
     * 
     * @param p_companyName
     * @param p_resourceName
     */
    public static void copyPropertiesToCompany(String p_companyName,
            String p_resourceName)
    {
        URL url = SystemConfiguration.class.getResource(p_resourceName);
        if (url != null)
        {
            File oriFile;
            try
            {
                oriFile = new File(url.toURI().getPath());
                String newFileName = getNewResourcePath(
                        oriFile.getAbsolutePath(), p_companyName);
                File newFile = new File(newFileName);
                if (!newFile.exists())
                {
                    File destDir = newFile.getParentFile();
                    if (!destDir.exists())
                    {
                        destDir.mkdirs();
                    }
                    FileCopier.copyFile(oriFile, destDir);
                }
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get file stream for company level file
     * 
     * @param companyFileName
     * @param rootFileName
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getCompanyFileStream(String rootFileName)
            throws FileNotFoundException
    {
        String companyFileName = getCompanyResourcePath(rootFileName);

        InputStream is = SystemConfiguration.class
                .getResourceAsStream(companyFileName);
        if (is == null)
        {
            URL url = SystemConfiguration.class.getResource(rootFileName);

            if (url == null)
            {
                throw new FileNotFoundException(rootFileName);
            }

            String filepath = url.getFile();
            File f1 = new File(filepath);
            String pfile = f1.getParentFile().getParent() + companyFileName;
            is = new FileInputStream(pfile);
        }

        return is;
    }

    /**
     * For issue "Properties files separation by Company"
     * 
     * @param p_name
     *            format between "/properties/MsOfficeAdapter.properties" and
     *            "properties/WordExtractor"
     * @return
     */
    public static String getCompanyResourcePath(String p_name)
    {
        String fileName = getBaseName(p_name);
        String suffix = ".properties";
        String fileNameWithSuffix = fileName;
        if (fileNameWithSuffix.indexOf(".") == -1)
        {
            fileNameWithSuffix += suffix;
        }
        if (isCompanyLevel(fileNameWithSuffix))
        {
            String cName = CompanyWrapper.getCurrentCompanyName();
            if ("Null Company".equals(cName))
            {
                CATEGORY.error("Can not get company name "
                        + "when getting company level properties file "
                        + fileNameWithSuffix);
                return p_name;
            }
            else
            {
                StringBuffer sb = new StringBuffer();
                sb.append("/properties/").append(cName).append("/")
                        .append(fileName);
                String newName = sb.toString();
                // check if the company properties file exists
                sb.delete(0, sb.length());
                sb.append("/properties/").append(cName).append("/")
                        .append(fileNameWithSuffix);
                String urlName = sb.toString();
                URL url = SystemConfiguration.class.getResource(urlName);
                if (url == null)
                {
                    String ori = "/properties/" + fileNameWithSuffix;
                    URL oriurl = SystemConfiguration.class.getResource(ori);

                    if (oriurl != null)
                    {
                        String filepath = oriurl.getFile();
                        File f1 = new File(filepath);
                        String pfile = f1.getParentFile().getParent() + newName;
                        File expectedFile = new File(pfile);

                        if (!expectedFile.exists())
                        {
                            copyPropertiesToCompany(cName, ori);
                        }
                    }
                }
                // end check
                return newName;
            }
        }
        else
        {
            return p_name;
        }
    }

    /**
     * For issue "Properties files separation by Company"
     * 
     * @param p_name
     * @return
     */
    public static String getCompanyBundlePath(String p_name)
    {
        String name = getCompanyResourcePath(p_name);
        while (name.startsWith("/") || name.startsWith("\\"))
        {
            name = name.substring(1);
        }

        return name;
    }

    private static String getBaseName(String p_name)
    {
        int index = p_name.lastIndexOf("/");
        if (index == -1)
        {
            index = p_name.lastIndexOf("\\");
        }
        if (index != -1)
        {
            return p_name.substring(index + 1);
        }
        else
        {
            return p_name;
        }
    }

    private static boolean isCompanyLevel(String p_name)
    {
        setCompanyProfileNames();
        if (companyProfileNames != null)
        {
            for (int i = 0; i < companyProfileNames.length; i++)
            {
                String cp = companyProfileNames[i];
                if (p_name.equalsIgnoreCase(cp))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static String getNewResourcePath(String p_ori, String p_companyName)
    {
        String newName = p_ori.replace('\\', '/');
        int index = newName.lastIndexOf('/') + 1;
        newName = newName.substring(0, index) + p_companyName + "/"
                + newName.substring(index);

        return newName;
    }

    /**
     * Get the specified parameter and return it as an int.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public int getIntParameter(String p_paramName) throws ConfigException
    {
        int value;
        String strParam = getStringParameter(p_paramName);

        try
        {
            value = Integer.parseInt(strParam);
        }
        catch (NumberFormatException nfe)
        {
            throw new ConfigException(ConfigException.EX_BADPARAM, nfe);
        }

        return value;
    }

    /**
     * Get the specified parameter and return it as a boolean. The value can
     * have the values 0, 1, "true" or "false".
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public boolean getBooleanParameter(String p_paramName)
            throws ConfigException
    {
        String str_value = getStringParameter(p_paramName);

        str_value = str_value.trim().toLowerCase();

        if (str_value.equals("true"))
        {
            return true;
        }

        if (str_value.equals("false"))
        {
            return false;
        }

        int value;

        try
        {
            value = Integer.parseInt(str_value);
        }
        catch (NumberFormatException nfe)
        {
            throw new ConfigException(ConfigException.EX_BADPARAM, nfe);
        }

        if (value == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Gets system parameter value from database directly.
     */
    public static String getParameter(String p_paramName)
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String value = getParameter(p_paramName, companyId);
        if (value == null)
        {
            value = getParameter(p_paramName, CompanyWrapper.SUPER_COMPANY_ID);
        }
        return value;
    }

    public static String getParameter(String p_paramName, String p_companyId)
    {
        String value = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet rs = null;
        try
        {
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(SQL_QUERY_PARAM);
            query.setString(1, p_paramName);
            query.setLong(2, Long.parseLong(p_companyId));
            rs = query.executeQuery();
            if (rs.next())
            {
                value = rs.getString(1);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Error getting value of system parameter "
                    + p_paramName, e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(connection);
        }
        return value;
    }

    /**
     * Get the specified parameter and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public abstract String getStringParameter(String p_paramName)
            throws ConfigException;

    /**
     * Get the specified parameter and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public abstract String getStringParameter(String p_paramName,
            String p_companyId) throws ConfigException;

    /**
     * Get an array of the Strings parsed by DEFAULT_DELIMITER from a
     * SystemConfiguration parameter.
     * 
     * @param p_parameterName
     *            name of the SystemConfiguration parameter
     * @return String[] parsed by DEFAULT_DELIMITER
     * @throws ConfigException
     *             if p_parameterName is not found.
     */
    public String[] getStrings(String p_parameterName) throws ConfigException
    {
        String[] strings = null;
        String classString = getStringParameter(p_parameterName);
        StringTokenizer tokenizer = new StringTokenizer(classString,
                DEFAULT_DELIMITER);
        int cnt = tokenizer.countTokens();
        if (cnt > 0)
        {
            strings = new String[cnt];
            for (int i = 0; i < cnt; i++)
            {
                strings[i] = tokenizer.nextToken();
            }
        }
        return strings;
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public abstract String toString();

    /**
     * Looks up the system parameter with the given name and compares its value
     * to the given key. Returns false if any exceptions are encountered
     * 
     * @param p_keyParameter
     *            system parameter name for an install key
     * @param p_keyValue
     *            an actual install key value
     * @return TRUE | FALSE
     */
    static public Boolean isKeyValid(String p_keyParameter, String p_keyValue)
    {
        Boolean rv = Boolean.FALSE;
        try
        {
            String installKey = SystemConfiguration.getInstance()
                    .getStringParameter(p_keyParameter);
            if (installKey.equals(p_keyValue))
            {
                rv = Boolean.TRUE;
                CATEGORY.info("Key " + p_keyParameter + "=" + installKey
                        + " is valid.");
            }
            else
                CATEGORY.info("Key " + p_keyParameter + "=" + installKey
                        + " is NOT valid.");

        }
        catch (Exception e)
        {
            CATEGORY.error("Could not lookup key " + p_keyParameter, e);
        }
        return rv;
    }

    static public boolean isKeyValid(String p_keyParameter)
    {
        return SystemConfiguration.getInstance().getBooleanParameter(
                p_keyParameter);
    }
}

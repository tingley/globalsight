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
package com.config.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.Assert;
import com.util.CodeUtil;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

/**
 * Just let operate installValues.properties more easy.
 * 
 */
public class InstallValues
{
    private static Logger log = Logger.getLogger(InstallValues.class);
    public final static String RESOURCE_FILE = "install/data/installValues.properties";
    private static final String ER_DIR = "/jboss/server/standalone/deployments/globalsight.ear";
    private static final String TEMP_DIRECTORY = System
            .getProperty("java.io.tmpdir");
    private static Properties PROPERTIES = null;
    public static String SERVER_PATH = "gs_home";

    /**
     * Loads installValues.properties from server.
     * 
     * @return Properties includs values of installValues.properties
     */
    public static Properties getProperties()
    {
        if (PROPERTIES == null)
        {
            PROPERTIES = new Properties();
            FileInputStream in = null;
            try
            {
                in = new FileInputStream(ServerUtil.getPath() + File.separator
                        + RESOURCE_FILE);
                PROPERTIES.load(in);
                decode();
                log.debug("Loading installValues from " + ServerUtil.getPath()
                        + File.separator + RESOURCE_FILE);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
            finally
            {
                try
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
                catch (IOException e)
                {
                    log.error(e.getMessage(), e);
                }
            }

            backup();
        }

        return PROPERTIES;
    }
    
    public static void setJavaHome(String home)
    {
    	PROPERTIES.put("java_home", home);
    	saveTo(new File(ServerUtil.getPath() + File.separator
                        + RESOURCE_FILE));
    	saveTo(getTempFile());
    }

    private static void decode()
    {
        final Set<String> keys = new HashSet<String>();
        keys.add("server_ssl_ks_pwd");
        keys.add("jar_sign_pwd");
        keys.add("system4_admin_password");
        keys.add("database_password");
        keys.add("account_password");
        for (String key : keys)
        {
            String value = PROPERTIES.getProperty(key);
            if (value != null && !value.trim().equals(""))
            {
                try
                {
                    value = CodeUtil.getDecryptionString(value);
                    PROPERTIES.put(key, value);
                }
                catch (Exception ignore)
                {
                }
            }
        }

    }

    /**
     * Changes server path to specified path and save to temp file.
     * 
     * @param path
     *            The specified path to change.
     */
    public static void setPath(String path)
    {
        Assert.assertNotNull(path, "Server path");
        getProperties().setProperty(SERVER_PATH, path.replace("\\", "/"));
        addAdditionalInstallValues();
        saveTo(getTempFile());
    }

    /**
     * Try to use "gs_home" as key to load server path from
     * InstallValues.propertes in temp fold.
     * 
     * @return server path or null.
     */
    public static String getServerPath()
    {
        File file = getTempFile();
        if (file.exists())
        {
            String path = PropertyUtil.get(file, SERVER_PATH);

            if (ServerUtil.isServerPath(path))
            {
                return path;
            }
        }
        else
        {
            log.info("Can't find the temp file");
        }

        return null;
    }

    /**
     * Loads value from InstallValues.propertes according to key.
     * 
     * @param key
     *            key of value, can't be null.
     * @return The value matched the key
     */
    public static String get(String key)
    {
        Assert.assertNotNull(key, "Key");
        Assert.assertTrue(getProperties().containsKey(key),
                "No key (" + key + ") found in InstallValues.properties.");

        return getProperties().getProperty(key);
    }

    /**
     * Reloads value from InstallValues.propertes according to key if value is
     * null.
     * 
     * @param key
     *            key of value, can't be null
     * @param value
     *            The value in InstallValues.propertes
     * @return The value matched the key
     */
    public static String getIfNull(String key, String value)
    {
        return value == null ? get(key) : value;
    }

    /**
     * Saves values in InstallValues.propertes file to the specified file.
     * 
     * @param file
     *            specified file to save InstallValues.
     */
    public static void saveTo(File file)
    {
        FileOutputStream out = null;
        try
        {
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            getProperties().store(out, null);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Gets file (installValues.properties) in temp fold.
     * 
     * @return file (installValues.properties)
     */
    public static File getTempFile()
    {
        return new File(TEMP_DIRECTORY, "installValues.properties");
    }

    /**
     * Gets file (installValues.properties.backup) in temp fold.
     * 
     * @return file (installValues.properties.backup)
     */
    private static File getBackupFile()
    {
        return new File(TEMP_DIRECTORY, "installValues.properties.backup");
    }

    /**
     * Backs up file (installValues.properties) in temp fold.
     * 
     * @see #rollback()
     */
    public static void backup()
    {
        try
        {
            FileUtil.copyFile(getTempFile(), getBackupFile());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Jast change "\\" to "/".
     * 
     * @param key
     * @return
     */
    private static String getDir(String key)
    {
        return get(key).replace("\\", "/");
    }

    /**
     * Not all values in installValues.properties are inputed, such as ear_home
     * and gs_ear_root, so we need count out the values according to inputed
     * values, and add to installValues.properties.
     */
    public static void addAdditionalInstallValues()
    {
        // Make some special parameters for certain files.
        Properties values = getProperties();
        String gsHome = getDir(SERVER_PATH);

        String logDir = gsHome + "/logs";
        File logDirFile = new File(logDir);
        if (logDirFile.exists() == false)
        {
            logDirFile.mkdirs();
        }
        values.put("system_log_directory", logDir);
        values.put("system_log_directory_forwardslash", logDir.replace("\\",
                "/"));
        values.put("GS_HOME", gsHome);
        values.put("java_home_forwardslash", getDir("java_home"));
        values.put("msoffice_dir_forwardslash", getDir("msoffice_dir"));
        values.put("file_storage_dir_forwardslash", getDir("file_storage_dir"));
        values.put("gs_home_forwardslash", gsHome);
        values.put("gs_ear_root", gsHome + ER_DIR);
        values.put("gs_ear_root_forwardslash", gsHome + ER_DIR);
        values.put("classpath_separator", ServerUtil.isInLinux() ? ":" : ";");
        values.put("cxe_docsDir_forwardslash", getDir("cxe_docsDir"));

        // figure out what the CAP login Url should be for users
        StringBuffer baseUrl = new StringBuffer();
        baseUrl.append("http://").append(get("server_host")).append(":")
                .append(get("server_port"));
        values.put("cap_login_url", baseUrl.toString() + "/globalsight");
        values.put("install_data_dir_forwardslash", gsHome + "/install/data");

        values.put("canoncial_mysql_path", gsHome + "/install/data/mysql");
        values.put("ear_home", gsHome + "/deployment"); 
         
        // put values for linux jboss service command (service.sh.template)
        values.put("JBOSS_HOME", gsHome + "/jboss/server");
        values.put("SERVICE_NAME", "globalsight");
    }
}

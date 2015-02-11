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
package com.globalsight.everest.util.netegrity;

import java.net.InetAddress;
import java.net.URL;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;

/**
 * The Netegrity class helps with using the Netegrity
 * Single Sign On mechanism. It reads a "Netegrity.properties"
 * file.
 */
public class Netegrity
{
    private static final Logger CATEGORY = Logger.getLogger(Netegrity.class);
    private static boolean s_usingNetegrity = false;
    private static String s_netegrityServerIpAddress = null;
    private static String s_netegrityLogoutUrl = null;
    private static String s_netegrityLoginUrl = null;
    private static String s_netegrityGsPassword = null;

    static
    {
        determineNetegrityUsage();
    }

    /**
     * Returns true if netegrity is being used.
     * 
     * @return true | false
     */
    public static boolean isNetegrityEnabled()
    {
        return s_usingNetegrity;
    }

    /**
     * Gets the server ip address of the netegrity server
     * 
     * @return String
     */
    public static String getNetegrityServerIpAddress()
    {
        return s_netegrityServerIpAddress;
    }

    /**
     * Returns the URL to use for logging out.
     * 
     * @return 
     */
    public static String getNetegrityLogoutUrl()
    {
        return s_netegrityLogoutUrl;
    }

    /**
     * Returns the URL to use for logging in. This page is protected by Netegrity.
     * 
     * @return 
     */
    public static String getNetegrityLoginUrl()
    {
        return s_netegrityLoginUrl;
    }

    /**
     * Returns the password to use for Netegrity System4 users
     * 
     * @return 
     */
    public static String getNetegrityGsPassword()
    {
        return s_netegrityGsPassword;
    }


    /**
     * Reads the Netegrity.properties file and sets values
     * such as the server ip address, and whether netegrity is
     * enabled. If the property file is not found, then
     * netegrity is assumed to be disabled.
     */
    private static void determineNetegrityUsage()
    {
        //try to read in the netegrity property file
        try
        {
            ResourceBundle netegrityProperties = ResourceBundle.getBundle(Netegrity.class.getName());
            CATEGORY.info("Found Netegrity property file.");
            String val = netegrityProperties.getString("netegrity.enabled");
            CATEGORY.info("netegrity.enabled="+val);
            s_usingNetegrity = Boolean.valueOf(val).booleanValue();
            if (!s_usingNetegrity)
                return;

            s_netegrityServerIpAddress = netegrityProperties.getString("netegrity.serverIpAddress");
            CATEGORY.info("netegrity.serverIpAddress="+s_netegrityServerIpAddress);
            s_netegrityLogoutUrl = netegrityProperties.getString("netegrity.logoutUrl");
            CATEGORY.info("netegrity.logoutUrl="+s_netegrityLogoutUrl);
            s_netegrityLoginUrl = netegrityProperties.getString("netegrity.loginUrl");
            CATEGORY.info("netegrity.loginUrl="+s_netegrityLoginUrl);

            s_netegrityGsPassword = netegrityProperties.getString("netegrity.password");
            CATEGORY.debug("netegrity.password="+s_netegrityGsPassword);
        }
        catch (MissingResourceException mre)
        {
            CATEGORY.info("Property file Netegrity.properties not found. Not using Netegrity for single-sign-on");
            s_usingNetegrity = false;
        }
        catch (Exception e)
        {
            CATEGORY.error("Not using Netegrity for single-sign-on.",e);
            s_usingNetegrity = false;
        }
    }
}


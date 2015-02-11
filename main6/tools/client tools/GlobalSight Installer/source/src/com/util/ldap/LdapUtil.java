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
package com.util.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;

import com.config.properties.InstallValues;

public class LdapUtil
{
    public static Logger logger = Logger.getLogger(LdapUtil.class);

    private static String host;
    private static String port;
    private static String base;
    private static String userName;
    private static String password;
    private static String ldapDir;

    public static final String USER_BASE_DN = "ou=People,o=" + getBase();

    public static final String ROLE_BASE_DN = "ou=Groups,o=" + getBase();

    protected static final String LDAP_ATTR_USERID = "uid";

    public static final String LDAP_ATTR_FALSE = "FALSE";

    public static final String LDAP_ATTR_TRUE = "TRUE";

    private static DirContextPool dirContextPool = getConnectionPool();

    private static DirContextPool getConnectionPool()
    {
        try
        {
            if (dirContextPool == null)
            {
                dirContextPool = new DirContextPool(1, 20, getHost(), getPort());
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return dirContextPool;
    }

    private static String getBase()
    {
        if (base == null)
        {
            base = InstallValues.getIfNull("ldap_base", base);
        }
        return base;
    }

    private static String getHost()
    {
        if (host == null)
        {
            host = InstallValues.getIfNull("ldap_host", host);
        }
        return host;
    }

    private static String getPort()
    {
        if (port == null)
        {
            port = InstallValues.getIfNull("ldap_port", port);
        }
        return port;
    }

    public static String getUserName()
    {
        if (userName == null)
        {
            userName = InstallValues.getIfNull("ldap_username", userName);
        }
        return userName;
    }

    public static String getPassword()
    {
        if (password == null)
        {
            password = InstallValues.getIfNull("ldap_password", password);
        }
        return password;
    }

    public static String getLdapDir()
    {
        if (ldapDir == null)
        {
            ldapDir = InstallValues.getIfNull("ldap_install_dir", ldapDir);
        }
        return ldapDir;
    }

    private static void bindUser(DirContext context, String dn, String password)
            throws NamingException
    {
        if (context != null)
        {
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
        }
    }

    private static String getLDAPLoginDN()
    {
        return getUserDN(getUserName());
    }

    public static String getUserDN(String p_uid)
    {
        return LDAP_ATTR_USERID + "=" + p_uid + "," + USER_BASE_DN;
    }

    public static Attribute generateLDAPAttribute(String p_attrName,
            String p_value)
    {
        String attrValue = (p_value == null) ? "" : p_value.trim();
        return new BasicAttribute(p_attrName, attrValue);
    }

    public static Attribute generateLDAPAttribute(String p_attrName,
            boolean p_value)
    {
        String p_attrValue = LDAP_ATTR_FALSE;
        if (p_value)
        {
            p_attrValue = LDAP_ATTR_TRUE;
        }

        return new BasicAttribute(p_attrName, p_attrValue);
    }

    public static List<String> getMultiAttributeValue(Attribute p_attribute)
            throws NamingException
    {
        if (p_attribute == null)
        {
            return null;
        }

        NamingEnumeration attrValues = p_attribute.getAll();
        if (attrValues != null && attrValues.hasMoreElements())
        {
            List<String> l = new ArrayList<String>();
            while (attrValues.hasMoreElements())
            {
                Object strObj = attrValues.nextElement();
                l.add(strObj.toString());
            }

            attrValues.close();
            return l;
        }
        else
        {
            attrValues.close();
            return null;
        }
    }

    public static String getSingleAttributeValue(Attribute p_attribute)
            throws NamingException
    {
        if (p_attribute == null)
        {
            return null;
        }
        NamingEnumeration attrValues = p_attribute.getAll();
        if (attrValues != null && attrValues.hasMoreElements())
        {
            Object strObj = attrValues.nextElement();
            attrValues.close();
            return strObj.toString();
        }
        else
        {
            attrValues.close();
            return null;
        }
    }

    public static String getSinglePasswordAttributeValue(Attribute p_attribute)
            throws NamingException
    {
        if (p_attribute == null)
        {
            return null;
        }
        NamingEnumeration attrValues = p_attribute.getAll();
        if (attrValues != null && attrValues.hasMoreElements())
        {
            Object strObj = attrValues.nextElement();
            attrValues.close();
            return new String((byte[]) strObj);
        }
        else
        {
            attrValues.close();
            return null;
        }
    }

    public static void checkInConnection(DirContext dirContext)
    {
        try
        {
            dirContextPool.closeDirContext(dirContext);
        }
        catch (NamingException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public static DirContext checkOutConnection()
    {
        DirContext dirContext = null;
        try
        {
            // check out a connection from the connection pool
            dirContext = dirContextPool.getDirContext();
            // bind a user to the connection
            bindUser(dirContext, getLDAPLoginDN(), getPassword());

            // unlimited size limit, wait for all results
            dirContext.addToEnvironment(DirContext.BATCHSIZE, "0");
        }
        catch (NamingException e)
        {
            // return the connection to the pool
            checkInConnection(dirContext);

            logger.error(e.getMessage(), e);
        }

        return dirContext;
    }

    public static boolean isStringValid(String value)
    {
        if (value == null || "".equalsIgnoreCase(value))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}

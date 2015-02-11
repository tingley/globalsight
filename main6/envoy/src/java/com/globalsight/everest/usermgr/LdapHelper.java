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
package com.globalsight.everest.usermgr;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralException;
import com.globalsight.util.system.ConfigException;

/**
 * LdapHelper, a LDAP Helper class that holds some LDAP configuration constants
 * as well as some static methods for LDAP operations.
 */
public class LdapHelper
{
    private static final Logger CATEGORY = Logger.getLogger(LdapHelper.class
            .getName());

    public static final String LDAP_HOST; // read from envoy property file

    public static final int LDAP_PORT; // read from envoy property file

    public static final String LDAP_LOGIN; // read from envoy property file

    public static final String LDAP_PASSWORD; // read from envoy property file

    public static final int MINIMUM_CONNECTIONS; // read from envoy property
                                                 // file

    public static final int MAXIMUM_CONNECTIONS; // read from envoy property
                                                 // file

    public static final String LDAP_BASE_PREFIX = "o=";

    public static final String LDAP_BASE; // read from envoy.properties

    public static final String LDAP_ATTR_STATUS = "status";

    // Possible states that a User or a Role can be in
    // These values are written out to LDAP as Strings to the STATUS field

    // maps to User.STATE.CREATED
    public static final String LDAP_CREATED_STATUS = "CREATED";

    // maps to User.State.ACTIVE
    public static final String LDAP_ACTIVE_STATUS = "ACTIVE";

    // maps to User.State.DELETED
    public static final String LDAP_DELETED_STATUS = "DELETED";

    // maps to User.State.DEACTIVE
    public static final String LDAP_DEACTIVE_STATUS = "DEACTIVE";

    // singleton instance of this class
    private static DirContextPool dirContextPool = null;

    public static final String LDAP_ATTR_DEFAULT_VALUE = "";

    public static final String LDAP_ATTR_FALSE = "FALSE";

    public static final String LDAP_ATTR_TRUE = "TRUE";

    //
    // Class Initializing block
    //
    static
    {
        // loading LDAP properties
        SystemConfiguration prop = null;
        String host = null;
        String login = null;
        String passwd = null;
        String ldapBase = null;

        int port = -1;
        int minCons = -1;
        int maxCons = -1;

        try
        {
            prop = SystemConfiguration.getInstance();
            host = prop.getStringParameter(SystemConfigParamNames.LDAP_HOST);

            String tmpStr = prop
                    .getStringParameter(SystemConfigParamNames.LDAP_PORT);
            if (tmpStr != null)
            {
                port = Integer.parseInt(tmpStr);
            }

            login = prop
                    .getStringParameter(SystemConfigParamNames.LDAP_USER_NAME);
            passwd = prop
                    .getStringParameter(SystemConfigParamNames.LDAP_PASSWORD);
            tmpStr = prop
                    .getStringParameter(SystemConfigParamNames.LDAP_MIN_CONNECTIONS);
            if (tmpStr != null)
            {
                minCons = Integer.parseInt(tmpStr);
            }

            tmpStr = prop
                    .getStringParameter(SystemConfigParamNames.LDAP_MAX_CONNECTIONS);
            if (tmpStr != null)
            {
                maxCons = Integer.parseInt(tmpStr);
            }

            ldapBase = LDAP_BASE_PREFIX
                    + prop.getStringParameter(SystemConfigParamNames.LDAP_BASE);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("The ldap base is set to " + ldapBase);
            }
        }
        catch (ConfigException ce)
        {
            CATEGORY.error("A ConfigException was thrown when trying"
                    + " to read LDAP properties from LdapHelper.", ce);
        }
        catch (NumberFormatException ne)
        {
            CATEGORY.error("A NumberFormatException was thrown when trying"
                    + " to read LDAP properties from LdapHelper.", ne);
        }
        catch (GeneralException ge)
        {
            CATEGORY.error(
                    "A general exception was thrown when trying"
                            + " to read the system configuration file from LdapHelper.",
                    ge);
        }

        // Initialize the constants
        LDAP_HOST = host;
        LDAP_PORT = port;
        LDAP_LOGIN = login;
        LDAP_PASSWORD = passwd;
        LDAP_BASE = ldapBase;

        MINIMUM_CONNECTIONS = minCons;
        MAXIMUM_CONNECTIONS = maxCons;
    }

    //
    // Constructor
    //

    public LdapHelper()
    {
        // empty Constructor
    }

    //
    // Public Methods
    //

    /**
     * A unitily method to make sure there is only one connection pool created
     * in the system.
     * 
     * @return A DirContextPool object.
     * 
     * @exception NamingException
     *                - LDAP related exception.
     */
    public static DirContextPool getConnectionPool() throws NamingException
    {
        if (dirContextPool == null)
        {
            dirContextPool = new DirContextPool(MINIMUM_CONNECTIONS,
                    MAXIMUM_CONNECTIONS, LDAP_HOST, LDAP_PORT);
        }
        return dirContextPool;
    }

    /**
     * Get the String value from a single valued Attribute.
     * 
     * @param p_attribute
     *            - Attribute to be read
     * @return A String value
     */
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

    /**
     * Gets the password of the user. <br>
     * The returned value of the <code>Attribute</code> should be byte array.
     * 
     * @param p_attribute
     * @return
     * @throws NamingException
     */
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

    /**
     * Get a Vector of String for an Attribute values.
     * 
     * @param p_attribute
     *            - Attribute to be read
     * @return A Vector of String
     */
    public static Vector getMultiAttributeValue(Attribute p_attribute)
            throws NamingException
    {
        if (p_attribute == null)
        {
            return null;
        }

        NamingEnumeration attrValues = p_attribute.getAll();
        if (attrValues != null && attrValues.hasMoreElements())
        {
            Vector v = new Vector();
            while (attrValues.hasMoreElements())
            {
                Object strObj = attrValues.nextElement();
                v.addElement(strObj.toString());
            }

            attrValues.close();
            return v;
        }
        else
        {
            attrValues.close();
            return null;
        }
    }

    /**
     * Generate a Attribute for the given attribute name and value pair It
     * always fills the attribute with the default value (defined as a blank
     * String) if the value is null.
     * 
     * @param p_attrName
     *            - LDAP attribute name
     * @param p_value
     *            - the value for the above attribute
     * 
     * @return An Attribute object
     */
    public static Attribute generateLDAPAttribute(String p_attrName,
            String p_value)
    {
        String attrValue = (p_value == null) ? LDAP_ATTR_DEFAULT_VALUE
                : p_value.trim();
        return new BasicAttribute(p_attrName, attrValue);
    }

    /**
     * Generate an Attribute with a NULL value.
     * 
     * @param p_attrName
     * 
     * @return A Attribute object
     */
    public static Attribute generateNullValueLDAPAttribute(String p_attrName)
    {
        String value = "";
        return new BasicAttribute(p_attrName, value);
    }

    /**
     * Generate a Attribute for the given attribute name and boolean value pair
     * 
     * @param p_attrName
     *            - LDAP attribute name
     * @param p_value
     *            - the boolean value for the above attribute
     * 
     * @return An Attribute object
     */
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

    /**
     * Convert a Vector of Strings to String arrays
     */
    public static String[] convertVectorToStringArray(Vector p_vec)
    {
        if (p_vec == null)
        {
            return null;
        }

        String[] array = new String[p_vec.size()];

        for (int i = 0; i < p_vec.size(); i++)
        {
            array[i] = (String) p_vec.elementAt(i);
        }

        return array;
    }

    /**
     * Gets the common element set of two String arrays. To meet LDAP search
     * needs, if one array is null, just return the other array.
     */
    public static String[] getCommonSet(String[] p_set1, String[] p_set2)
    {
        String[] commonSet = null;
        String[] smallerSet = null;
        List biggerSet = null;

        if (p_set1 == null)
        {
            commonSet = p_set2;
        }
        else if (p_set2 == null)
        {
            commonSet = p_set1;
        }
        else
        {
            if (p_set1.length >= p_set2.length)
            {
                smallerSet = p_set2;
                biggerSet = Arrays.asList(p_set1);
            }
            else
            {
                smallerSet = p_set1;
                biggerSet = Arrays.asList(p_set2);
            }

            Vector tmpSet = new Vector();

            for (int i = 0; i < smallerSet.length; i++)
            {
                String element = smallerSet[i];

                if (biggerSet.contains(element))
                {
                    tmpSet.addElement(element);
                }
            }

            commonSet = convertVectorToStringArray(tmpSet);
        }

        return commonSet;
    }

    /**
     * Combines two arrays into one array of unique elements.
     */
    public static String[] combineArrays(String[] p_set1, String[] p_set2)
    {
        String[] combinedSet = null;
        String[] smallerSet = null;
        String[] biggerSet = null;
        List biggerList = null;

        Vector tmpSet = new Vector();

        if (p_set1 == null)
        {
            combinedSet = p_set2;
        }
        else if (p_set2 == null)
        {
            combinedSet = p_set1;
        }
        else
        {
            if (p_set1.length >= p_set2.length)
            {
                smallerSet = p_set2;
                biggerSet = p_set1;
                biggerList = Arrays.asList(p_set1);
            }
            else
            {
                smallerSet = p_set1;
                biggerSet = p_set2;
                biggerList = Arrays.asList(p_set2);
            }

            for (int i = 0; i < biggerSet.length; i++)
            {
                tmpSet.addElement(biggerSet[i]);
            }

            for (int i = 0; i < smallerSet.length; i++)
            {
                String element = smallerSet[i];
                if (!biggerList.contains(element))
                {
                    tmpSet.addElement(element);
                }
            }

            combinedSet = convertVectorToStringArray(tmpSet);
        }

        return combinedSet;
    }

    /**
     * Conversion for getting the state as a string to be stored in LDAP.
     */
    static public String getStateAsString(int p_userState)
    {
        String state = null;
        switch (p_userState)
        {
            case User.State.CREATED:
                state = LDAP_CREATED_STATUS;
                break;
            case User.State.ACTIVE:
                state = LDAP_ACTIVE_STATUS;
                break;
            case User.State.DEACTIVE:
                state = LDAP_DEACTIVE_STATUS;
                break;
            case User.State.DELETED:
            default:
                state = LDAP_DELETED_STATUS;
                break;
        }
        return state;
    }

    /**
     * Conversion for changing the state to an int that is used by the UserImpl
     * java class.
     */
    static public int getStateAsInt(String p_userState)
    {
        int state = 0;
        if (p_userState == null)
        {
            state = User.State.DELETED;
        }
        else if (p_userState.equals(LDAP_ACTIVE_STATUS))
        {
            state = User.State.ACTIVE;
        }
        else if (p_userState.equals(LDAP_CREATED_STATUS))
        {
            state = User.State.CREATED;
        }
        else if (p_userState.equals(LDAP_DEACTIVE_STATUS))
        {
            state = User.State.DEACTIVE;
        }
        else
        {
            state = User.State.DELETED;
        }
        return state;
    }

    static public Attribute generateUniqueMemberAttr(String key, String[] strSet)
    {
        Attribute uniqueMemberAttr = new BasicAttribute(key);
        // "uniqueMember" attribute is necessary in openLdap for
        // "groupofuniquenames"
        // but optional in iPlanet. So return empty String by default.
        if (strSet.length <= 0)
        {
            uniqueMemberAttr.add("");
        }
        for (int i = 0; i < strSet.length; i++)
        {
            uniqueMemberAttr.add(strSet[i]);
        }

        return uniqueMemberAttr;
    }

    static public boolean isStringValid(String value)
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

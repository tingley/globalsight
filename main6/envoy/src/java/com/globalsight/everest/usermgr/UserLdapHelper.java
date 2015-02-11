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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * UserLdapHelper, a LDAP Helper class that helps convert data for LDAP
 * operations on the 'User' object.
 */
public class UserLdapHelper extends LdapHelper
{
    public static final String LDAP_ATTR_USERID = "uid";

    public static final String LDAP_ATTR_USER_NAME = "cn";

    public static final String LDAP_ATTR_FIRST_NAME = "givenName";

    public static final String LDAP_ATTR_TITLE = "title";

    public static final String LDAP_ATTR_LAST_NAME = "sn";

    public static final String LDAP_ATTR_EMAIL = "email";

    public static final String LDAP_ATTR_CC_EMAIL = "emailCc";

    public static final String LDAP_ATTR_BCC_EMAIL = "emailBcc";

    public static final String LDAP_ATTR_PASSWORD = "userPassword";

    public static final String LDAP_ATTR_OFFICE_PHONE = "telephoneNumber";

    public static final String LDAP_ATTR_HOME_PHONE = "homePhone";

    public static final String LDAP_ATTR_FAX_NUMBER = "facsimileTelephoneNumber";

    public static final String LDAP_ATTR_CELL_NUMBER = "mobile";

    public static final String LDAP_ATTR_DEFAULT_UI_LOCALE = "defaultUILocale";

    public static final String LDAP_ATTR_ADDRESS = "postalAddress";

    public static final String LDAP_ATTR_COMPANY = "companyName";

    public static final String LDAP_ATTR_INALLPROJECTS = "isInAllProjects";

    public static final String LDAP_ATTR_TYPE = "employeeType";

    //
    // Package constants to hold the User LDAP attribute and key names
    //
    static final String USER_BASE_DN = "ou=People," + LDAP_BASE;

    static final String LDAP_ATTR_OBJECT_CLASS = "objectclass";

    static final String LDAP_USER_END_OBJECT_CLASS = "localizationPerson";

    static final String LDAP_ANONYMOUS_USER_TYPE = "ANONYMOUS";

    static final String USER_LDAP_RDN_ATTRIBUTE = LDAP_ATTR_USERID;

    static final String BLANK = " ";

    static final String LDAP_PWD_MD5 = "MD5";

    static final String LDAP_PREFIX_MD5 = "{MD5}";

    static final String LDAP_PWD_SHA = "SHA";

    static final String LDAP_PREFIX_SHA = "{sha}";

    //
    // Protected constants
    //

    // Object type for the User object in LDAP
    protected static final String[] LDAP_USER_OBJECT_CLASSES =
    { "top", "person", "organizationalPerson", "inetOrgPerson",
            "localizationPerson" };

    //
    // Private constants
    //
    private static final Logger CATEGORY = Logger
            .getLogger(UserLdapHelper.class.getName());

    //
    // Package level and Local Methods.
    //

    UserLdapHelper()
    {
        // empty constructor.
    }

    /**
     * Get LDAP DN of a User for the given uid
     */
    public static String getUserDN(String p_uid)
    {
        return USER_LDAP_RDN_ATTRIBUTE + "=" + p_uid + "," + USER_BASE_DN;
    }

    /**
     * Gets user LDAP DN based on given user name.
     */
    public static String getUserDNByName(String p_userName)
    {
        return LDAP_ATTR_USER_NAME + "=" + p_userName + "," + USER_BASE_DN;
    }

    /**
     * Get LDAP DNs for a number of users by having the uids
     * 
     * @return a String[] of user DNs
     */
    static String[] getUsersDN(String[] p_uids)
    {
        if (p_uids == null)
        {
            return null;
        }

        String[] dns = new String[p_uids.length];

        for (int i = 0; i < p_uids.length; i++)
        {
            dns[i] = getUserDN(p_uids[i]);
        }

        return dns;
    }

    /**
     * Get LDAP DN of a User for the given email
     */
    public static String getUserDNFromEmail(String p_email)
    {
        return LDAP_ATTR_EMAIL + "=" + p_email + "," + USER_BASE_DN;
    }

    /**
     * Convert a User object to an Attributes object
     * 
     * @param user
     *            The ldap user info.
     * @param needEncodePwd
     *            If user password need to be encoded, set this to true.
     * 
     * @return a Attributes
     */
    static Attributes convertUserToLDAPEntry(User user, boolean needEncodePwd)
    {

        BasicAttributes attrSet = new BasicAttributes();
        Attribute objClass = new BasicAttribute(LDAP_ATTR_OBJECT_CLASS);
        objClass.add(LDAP_USER_OBJECT_CLASSES[0]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[1]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[2]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[3]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[4]);
        // add each LDAP attribute
        attrSet.put(objClass);
        if (isStringValid(user.getUserId()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_USERID,
                    user.getUserId()));
        }
        if (isStringValid(user.getUserName()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_USER_NAME,
                    user.getUserName()));
        }
        if (isStringValid(user.getTitle()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_TITLE, user.getTitle()));
        }
        if (isStringValid(user.getLastName()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_LAST_NAME,
                    user.getLastName()));
        }
        if (isStringValid(user.getFirstName()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_FIRST_NAME,
                    user.getFirstName()));
        }

        String status = getStateAsString(user.getState());
        attrSet.put(generateLDAPAttribute(LDAP_ATTR_STATUS, status));

        if (isStringValid(user.getPassword()))
        {
            if (needEncodePwd)
            {
                attrSet.put(generateLDAPAttribute(LDAP_ATTR_PASSWORD,
                        encyptMD5Password(user.getPassword())));
            }
            else
            {
                attrSet.put(generateLDAPAttribute(LDAP_ATTR_PASSWORD,
                        user.getPassword()));
            }
        }

        if (isStringValid(user.getEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_EMAIL, user.getEmail()));
        }
        if (isStringValid(user.getCCEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_CC_EMAIL,
                    user.getCCEmail()));
        }
        if (isStringValid(user.getBCCEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_BCC_EMAIL,
                    user.getBCCEmail()));
        }
        if (isStringValid(user.getHomePhoneNumber()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_HOME_PHONE,
            		user.getHomePhoneNumber()));
        }
        if (isStringValid(user.getOfficePhoneNumber()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_OFFICE_PHONE,
            		user.getOfficePhoneNumber()));
        }
        if (isStringValid(user.getFaxPhoneNumber()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_FAX_NUMBER,
            		user.getFaxPhoneNumber()));
        }
        if (isStringValid(user.getCellPhoneNumber()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_CELL_NUMBER,
                    user.getCellPhoneNumber()));
        }
        if (isStringValid(user.getDefaultUILocale()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_DEFAULT_UI_LOCALE,
                    user.getDefaultUILocale()));
        }
        if (isStringValid(user.getAddress()))
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_ADDRESS,
                    user.getAddress()));
        if (isStringValid(user.getCompanyName()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_COMPANY,
                    user.getCompanyName()));
        }
        // vonverts 'boolean' to corresponding 'String' because openldap
        // 'Attribute' need string parameter.
        attrSet.put(generateLDAPAttribute(LDAP_ATTR_INALLPROJECTS,
                user.isInAllProjects()));
        // if anonymous then write it out - otherwise just leave NULL
        if (user.getType() == User.UserType.ANONYMOUS)
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_TYPE,
                    LDAP_ANONYMOUS_USER_TYPE));
        }

        return attrSet;
    }

    /**
     * Generate an ModificationItem object for delete user operation. This
     * actually just sets the user status to 'DELETED' instead of deleting the
     * LDAP entry.
     */
    static ModificationItem getLDAPModificationForDeleteUser()
    {
        BasicAttribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_DELETED_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generates an ModificationItem object for deactivating a user.
     */
    static ModificationItem getLDAPModificationForDeactiveUser()
    {

        BasicAttribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_DEACTIVE_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generate an ModificationItem object for activating a user. This actually
     * just sets the user status to 'ACTIVE' in the LDAP entry.
     */
    static ModificationItem getLDAPModificationForActivateUser()
    {

        BasicAttribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_ACTIVE_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Convert an User object to ModificationItem[] object for updating that
     * User info in LDAP.
     */
    static ModificationItem[] convertUserToModificationSet(User p_user)
    {

        ArrayList attrSet = new ArrayList();
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_USERID, p_user.getUserId())));

        if (isStringValid(p_user.getTitle()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_TITLE, p_user.getTitle())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_TITLE, "null")));
        }

        if (p_user.getPassword() != null && p_user.isPasswordSet())
        {
            /* If the user doesn't set the password, use the original one */
            String password = encyptMD5Password(p_user.getPassword());

            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_PASSWORD, password)));
        }

        attrSet.add(new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_USER_NAME, p_user.getUserName())));
        attrSet.add(new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_LAST_NAME, p_user.getLastName())));
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_FIRST_NAME,
                        p_user.getFirstName())));

        String status = getStateAsString(p_user.getState());
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_STATUS, status)));

        if (isStringValid(p_user.getEmail()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_EMAIL, p_user.getEmail())));
        }
        if (isStringValid(p_user.getCCEmail()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_CC_EMAIL,
                            p_user.getCCEmail())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_CC_EMAIL, "null")));
        }
        if (isStringValid(p_user.getBCCEmail()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_BCC_EMAIL,
                            p_user.getBCCEmail())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_BCC_EMAIL, "null")));
        }
        if (isStringValid(p_user.getHomePhoneNumber()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_HOME_PHONE,
                            p_user.getHomePhoneNumber())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_HOME_PHONE, "null")));
        }
        if (isStringValid(p_user.getOfficePhoneNumber()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_OFFICE_PHONE,
                            p_user.getOfficePhoneNumber())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_OFFICE_PHONE, "null")));
        }
        if (isStringValid(p_user.getCellPhoneNumber()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_CELL_NUMBER,
                            p_user.getCellPhoneNumber())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_CELL_NUMBER, "null")));
        }
        if (isStringValid(p_user.getFaxPhoneNumber()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_FAX_NUMBER,
                            p_user.getFaxPhoneNumber())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_FAX_NUMBER, "null")));
        }
        if (isStringValid(p_user.getDefaultUILocale()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_DEFAULT_UI_LOCALE,
                            p_user.getDefaultUILocale())));
        }
        if (isStringValid(p_user.getAddress()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_ADDRESS,
                            p_user.getAddress())));
        }
        else
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_ADDRESS, "null")));
        }
        if (isStringValid(p_user.getCompanyName()))
        {
            attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_COMPANY,
                            p_user.getCompanyName())));
        }
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_INALLPROJECTS,
                        p_user.isInAllProjects())));
        // a user can't be changed from anonymous to GlobalSight and back
        // so just leave the type field alone for now
        // LDAP_ATTR_TYPE, LDAP_ANONYMOUS_USER_TYPE
        return (ModificationItem[]) attrSet.toArray(new ModificationItem[]
        {});
    }

    /**
     * Generates a collection of User objects from a NamingEnumeration after a
     * search is carried out.
     */
    static Vector<User> getUsersFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {

        Vector<User> userList = new Vector<User>();
        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                userList.addElement(getUserFromLDAPEntry(entry));
            }
        }

        p_SearchResults.close();
        return userList;
    }

    /**
     * Generates a collection of UserInfo objects from a NamingEnumeration after
     * a search is carried out.
     */
    static Vector getUserInfosFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {

        Vector userInfoList = new Vector();
        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                userInfoList.addElement(getUserInfoFromLDAPEntry(entry));
            }
        }
        p_SearchResults.close();

        return userInfoList;
    }

    /**
     * Get the User ID array from a NamingEnumeration
     */
    static String[] getUIDsFromSearchResults(NamingEnumeration p_SearchResults)
            throws NamingException
    {
        return convertVectorToStringArray(getUIDsVectorFromSearchResults(p_SearchResults));
    }

    /**
     * Get the User Name array from a NamingEnumeration
     */
    static String[] getNamesFromSearchResults(NamingEnumeration p_SearchResults)
            throws NamingException
    {
        return convertVectorToStringArray(getNamesVectorFromSearchResults(p_SearchResults));
    }

    static Vector getNamesVectorFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {
        Vector userNames = new Vector();

        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                String userName = getSingleAttributeValue(entry
                        .get(LDAP_ATTR_USER_NAME));
                userNames.addElement(userName);
            }
        }

        p_SearchResults.close();

        return userNames;
    }

    /**
     * Get the Uset ID array from a NamingEnumeration
     */
    static Vector getUIDsVectorFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {

        Vector uids = new Vector();

        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                String uid = getSingleAttributeValue(entry
                        .get(LDAP_ATTR_USERID));
                uids.addElement(uid);
            }
        }

        p_SearchResults.close();

        return uids;
    }

    /**
     * Get the company names from a NamingEnumeration
     */
    static String[] getCompanyNamesFromSearchResults(
            NamingEnumeration p_searchResults) throws NamingException
    {

        // use a set so duplicates are not saved
        Set companyNames = new TreeSet();

        while (p_searchResults.hasMoreElements())
        {

            String cName = null;
            Object searchResultObj = p_searchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                cName = getSingleAttributeValue(entry.get(LDAP_ATTR_COMPANY));
            }

            if (cName != null && cName.trim().length() > 0)
            {
                // adds it to the set
                // if it already exists just returns (NOP)
                companyNames.add(cName);
            }
        }
        p_searchResults.close();

        String[] cns = new String[companyNames.size()];
        return (String[]) companyNames.toArray(cns);
    }

    /**
     * Get the LDAP search filter for searching all LDAP User entries that are
     * active or atleast created and are not anonymous users. This should be
     * used for most queries.
     */
    static String getSearchFilter()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String superCompanyName = CompanyWrapper
                .getCompanyNameById(CompanyWrapper.SUPER_COMPANY_ID);

        StringBuilder buf = new StringBuilder();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_TYPE).append("=")
                .append(LDAP_ANONYMOUS_USER_TYPE).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append("))");
        // If is super user, then no limit in search rang.
        if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            buf.append(")");
        }
        else
        {
            // for a specific company, two kinds of users will be selected,
            // one kind of users belong to this specific company,
            // another kind of users belong to the super company
            buf.append("(|(").append(LDAP_ATTR_COMPANY).append("=")
                    .append(companyName).append(")").append("(")
                    .append(LDAP_ATTR_COMPANY).append("=")
                    .append(superCompanyName).append("))").append(")");
        }

        return buf.toString();
    }

    static String getSearchFilterForAllCompanies()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_TYPE).append("=")
                .append(LDAP_ANONYMOUS_USER_TYPE).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append(")))");

        return buf.toString();
    }

    /**
     * Encypt the passwd using md5. The return value will be suffic with the
     * {MD5}
     * 
     * @param passwd
     * @return
     */
    static String encyptMD5Password(String passwd)
    {
        try
        {
            byte[] md5Msg = MessageDigest.getInstance(LDAP_PWD_MD5).digest(
                    passwd.getBytes());
            return LDAP_PREFIX_MD5 + new String(new Base64().encode(md5Msg));
        }
        catch (NoSuchAlgorithmException e)
        {
            CATEGORY.error("The system didn't support the Md5 ALGORITHM", e);
        }

        return passwd;
    }

    /**
     * Encypt the passwd using sha. The return value will be suffic with the
     * {SHA}
     * 
     * @param passwd
     * @return
     */
    static String encyptShaPassword(String passwd)
    {
        try
        {
            byte[] shaMsg = MessageDigest.getInstance(LDAP_PWD_SHA).digest(
                    passwd.getBytes());
            return LDAP_PREFIX_SHA + new String(new Base64().encode(shaMsg));
        }
        catch (NoSuchAlgorithmException e)
        {
            CATEGORY.error("The system didn't support the Sha ALGORITHM", e);
        }

        return passwd;
    }

    /**
     * Get the LDAP search filter for searching all LDAP User entries that are
     * active or atleast created and are not anonymous users. This should be
     * used for most queries.
     */
    static String getSearchFilterForCurrentCompany()
    {
        String companyName = CompanyWrapper.getCurrentCompanyName();

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_TYPE).append("=")
                .append(LDAP_ANONYMOUS_USER_TYPE).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append("))").append("(|(")
                .append(LDAP_ATTR_COMPANY).append("=").append(companyName)
                .append(")").append("))");

        return buf.toString();
    }

    /**
     * Gets the LDAP search filter for searching all LDAP user entries from
     * specified company.
     * 
     * @param companyId
     *            - specified company id
     * @param searchOutOfDateUsers
     *            - whether to search the users that are in deleted, inactive
     *            status and of anonymous type.
     */
    static String getSearchFilterForUsersFromCompany(String companyId,
            boolean searchOutOfDateUsers)
    {
        String companyName = CompanyWrapper.getCompanyNameById(companyId);

        StringBuffer buf = new StringBuffer();
        buf.append("(&(");
        buf.append(LDAP_ATTR_OBJECT_CLASS);
        buf.append("=");
        buf.append(LDAP_USER_END_OBJECT_CLASS);
        buf.append(")");
        if (!searchOutOfDateUsers)
        {
            buf.append("(!(");
            buf.append(LDAP_ATTR_STATUS);
            buf.append("=");
            buf.append(LDAP_DELETED_STATUS);
            buf.append("))");
            buf.append("(!(");
            buf.append(LDAP_ATTR_TYPE);
            buf.append("=");
            buf.append(LDAP_ANONYMOUS_USER_TYPE);
            buf.append("))");
            buf.append("(!(");
            buf.append(LDAP_ATTR_STATUS);
            buf.append("=");
            buf.append(LDAP_DEACTIVE_STATUS);
            buf.append("))");
        }
        buf.append("(|(");
        buf.append(LDAP_ATTR_COMPANY);
        buf.append("=");
        buf.append(companyName);
        buf.append(")");
        buf.append("))");

        return buf.toString();
    }

    /**
     * Get the LDAP search filter for searching for all LDAP User entries that
     * are ACTIVE.
     */
    static String getSearchFilterForActiveUsers()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_TYPE).append("=")
                .append(LDAP_ANONYMOUS_USER_TYPE).append("))").append("(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_ACTIVE_STATUS).append(")");
        // If is super user, then no limit in search rang.
        if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            buf.append(")");
        }
        else
        {
            buf.append("(").append(LDAP_ATTR_COMPANY).append("=")
                    .append(companyName).append("))");
        }

        return buf.toString();
    }

    /**
     * 
     */
    static String getSearchFilterForEmail(String p_email)
    {
        if (p_email == null)
        {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append("))").append("(")
                .append(LDAP_ATTR_EMAIL).append("=").append(p_email)
                .append(")").append(")");

        return buf.toString();
    }

    /**
     * Get the LDAP search filter for searching all LDAP User entries (whether
     * active or deactive).
     */
    static String getSearchFilterForAllUsers()
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_USER_END_OBJECT_CLASS + "))";
    }

    /**
     * Gets the LDAP search filter for all active users that are part of ALL
     * projects.
     */
    static String getSearchFilterForActiveUsersInAllProjects()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_TYPE).append("=")
                .append(LDAP_ANONYMOUS_USER_TYPE).append("))").append("(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_ACTIVE_STATUS).append(")").append("(")
                .append(LDAP_ATTR_INALLPROJECTS).append("=")
                .append(LDAP_ATTR_TRUE).append(")");
        // If is super user, then no limit in search rang.
        if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            buf.append(")");
        }
        else
        {
            buf.append("(").append(LDAP_ATTR_COMPANY).append("=")
                    .append(companyName).append("))");
        }

        return buf.toString();

        /*
         * return "(&(" + LDAP_ATTR_INALLPROJECTS + "=" + LDAP_ATTR_TRUE + ")" +
         * LDAP_ATTR_OBJECT_CLASS + "=" + LDAP_USER_END_OBJECT_CLASS + ")" +
         * "(!(" + LDAP_ATTR_TYPE +"=" + LDAP_ANONYMOUS_USER_TYPE + "))" + "(" +
         * LDAP_ATTR_STATUS+"=" + LDAP_ACTIVE_STATUS+"))";
         */
    }

    /**
     * Get the LDAP search filter for searching LDAP Users entries that match
     * the given Attributes.
     */
    static String getSearchFilter(Attribute[] p_userAttrs)
            throws NamingException
    {

        if (p_userAttrs == null)
        {
            return null;
        }

        String[] operators = new String[p_userAttrs.length];
        for (int i = 0; i < p_userAttrs.length; i++)
        {
            operators[i] = "=";
        }

        return getSearchFilter(p_userAttrs, operators);
    }

    /**
     * Get the LDAP search filter for searching LDAP Users entries using the
     * given LDAP operators and the given Attributes.
     * 
     * <p>
     * The number of Attributes in p_userAttrs must equal the number of
     * operators in p_operators.
     */
    static String getSearchFilter(Attribute[] p_userAttrs, String[] p_operators)
            throws NamingException
    {

        if (p_userAttrs == null)
        {
            return null;
        }

        String[] stringAttrs = new String[p_userAttrs.length];
        String[] stringAttrValues = new String[p_userAttrs.length];
        for (int i = 0; i < p_userAttrs.length; i++)
        {
            stringAttrs[i] = p_userAttrs[i].getID();
            stringAttrValues[i] = getSingleAttributeValue(p_userAttrs[i]);
        }

        return getSearchFilter(stringAttrs, stringAttrValues, p_operators);
    }

    /**
     * Binds the user to the context.
     * 
     * 
     * @param context
     * @param dn
     * @param password
     * @throws NamingException
     */
    static void bindUser(DirContext context, String dn, String password)
            throws NamingException
    {
        if (context != null)
        {
            context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
        }
    }

    public static void authenticate(String password, String truePassword)
            throws NamingException
    {
        if (encyptMD5Password(password).equals(truePassword)
                || password.equals(truePassword)
                || encyptShaPassword(password).equals(truePassword))
        {
            return;
        }
        else
        {
            throw new NamingException();
        }
    }

    /**
     * Get the LDAP search filter for searching LDAP Users entries using the
     * given LDAP operators and the given Attributes.
     * 
     * <p>
     * The number of Strings in p_userAttrs, p_attrValues, and p_operators must
     * all be equal.
     */
    static String getSearchFilter(String[] p_userAttrs, String[] p_attrValues,
            String[] p_operators)
    {
        if (p_userAttrs == null || p_operators == null || p_attrValues == null
                || p_operators.length != p_userAttrs.length
                || p_attrValues.length != p_userAttrs.length)
        {
            return null;
        }

        String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append("))");
        // If is super user, then no limit in search rang.
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            buf.append("(").append(LDAP_ATTR_COMPANY).append("=")
                    .append(companyName).append(")");
        }

        for (int i = 0; i < p_userAttrs.length; i++)
        {
            buf.append("(").append(p_userAttrs[i]).append(p_operators[i])
                    .append(p_attrValues[i]).append(")");
        }

        buf.append(")");

        return buf.toString();

        /*
         * String filterPart1 = "(&(" + LDAP_ATTR_OBJECT_CLASS + "=" +
         * LDAP_USER_END_OBJECT_CLASS + ")" + "(!(" + LDAP_ATTR_STATUS+"=" +
         * LDAP_DELETED_STATUS +"))" + "(!(" + LDAP_ATTR_STATUS+"=" +
         * LDAP_DEACTIVE_STATUS +"))";
         * 
         * String filterPart2 = "";
         * 
         * for (int i = 0; i < p_userAttrs.length; i++) { filterPart2 += "("+
         * p_userAttrs[i] + p_operators[i] + p_attrValues[i] + ")"; }
         * 
         * String filterPart3 = ")"; String filter = filterPart1 + filterPart2 +
         * filterPart3;
         * 
         * return filter;
         */
    }

    /**
     * Get the LDAP search filter for searching LDAP Users entries that match
     * the given UIDs.
     */
    static String getSearchFilterOnUIDs(String[] p_uids)
    {
        if (p_uids == null)
        {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_USER_END_OBJECT_CLASS).append(")").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DELETED_STATUS).append("))").append("(!(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_DEACTIVE_STATUS).append("))").append("(|");

        for (int i = 0; i < p_uids.length; i++)
        {
            buf.append("(").append(LDAP_ATTR_USERID).append("=")
                    .append(p_uids[i]).append(")");
        }

        buf.append("))");

        return buf.toString();
    }

    /**
     * Convert a Attributes to a User object.
     */
    static User getUserFromLDAPEntry(Attributes p_entry) throws NamingException
    {
        User user = new UserImpl();

        Attribute attr = p_entry.get(LDAP_ATTR_USERID);
        user.setUserId(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_TITLE);
        user.setTitle(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_FIRST_NAME);
        user.setFirstName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_PASSWORD);
        user.setPassword(getSinglePasswordAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_LAST_NAME);
        user.setLastName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_USER_NAME);
        user.setUserName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_STATUS);
        String status = getSingleAttributeValue(attr);
        user.setState(getStateAsInt(status));

        attr = p_entry.get(LDAP_ATTR_ADDRESS);
        user.setAddress(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_COMPANY);
        user.setCompanyName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_EMAIL);
        user.setEmail(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_CC_EMAIL);
        user.setCCEmail(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_BCC_EMAIL);
        user.setBCCEmail(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_HOME_PHONE);
        user.setHomePhoneNumber(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_OFFICE_PHONE);
        user.setOfficePhoneNumber(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_FAX_NUMBER);
        user.setFaxPhoneNumber(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_CELL_NUMBER);
        user.setCellPhoneNumber(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_DEFAULT_UI_LOCALE);
        user.setDefaultUILocale(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_INALLPROJECTS);
        if (attr != null
                && getSingleAttributeValue(attr).equalsIgnoreCase(
                        LDAP_ATTR_TRUE))
        {
            // set to "false" as default - so only need to set to true
            // if value is "true"
            user.isInAllProjects(true);
        }

        attr = p_entry.get(LDAP_ATTR_TYPE);
        if (attr != null
                && getSingleAttributeValue(attr).equalsIgnoreCase(
                        LDAP_ANONYMOUS_USER_TYPE))
        {
            user.setType(User.UserType.ANONYMOUS);
        }
        else
        {
            user.setType(User.UserType.GLOBALSIGHT);
        }

        return user;
    }

    /**
     * Convert a Attributes to a UserInfo object.
     */
    static UserInfo getUserInfoFromLDAPEntry(Attributes p_entry)
            throws NamingException
    {
        UserInfo ui = new UserInfo();

        Attribute attr = p_entry.get(LDAP_ATTR_USERID);
        ui.setUserId(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_USER_NAME);
        ui.setUserName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_TITLE);
        ui.setTitle(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_FIRST_NAME);
        ui.setFirstName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_LAST_NAME);
        ui.setLastName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_EMAIL);
        ui.setEmailAddress(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_CC_EMAIL);
        ui.setCCEmailAddress(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_BCC_EMAIL);
        ui.setBCCEmailAddress(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_INALLPROJECTS);
        if (attr != null
                && getSingleAttributeValue(attr).equalsIgnoreCase(
                        LDAP_ATTR_TRUE))
        {
            // set to "false" as default - so only need to set to true
            // if value is "true"
            ui.isInAllProjects(true);
        }

        return ui;
    }

    /**
     * Get email address arrays for the given user objects.
     */
    static String[] getEmails(Vector p_users)
    {
        String[] emails = null;

        if (p_users != null)
        {
            int size = p_users.size();
            emails = new String[size];
            String tmp;

            for (int i = 0; i < size; i++)
            {
                tmp = ((User) p_users.elementAt(i)).getEmail();
                // ignore empty email -- ???
                if (tmp != null && tmp.length() != 0)
                {
                    emails[i] = tmp;
                }
            }
        }

        return emails;
    }

    /**
     * Get names of the given users, assuming each user must have a name.
     */
    static String[] getNames(Vector p_users)
    {
        String[] names = null;

        if (p_users != null)
        {
            int size = p_users.size();
            names = new String[size];

            for (int i = 0; i < size; i++)
            {
                names[i] = ((User) p_users.elementAt(i)).getUserName();
            }
        }

        return names;
    }

    /**
     * Strip off the user'd distinguished name pieces and just pass back the
     * userId. If the String passed in doesn't contain the key information it is
     * just returned as is.
     * 
     * @param p_userDn
     *            The distinguished name of a user (ie.
     *            uid=gsAdmin,ou=People,o=globalsight.com)
     * @return The userid parsed out of the distinguished name (ie. gsAdmin)
     */
    static String parseUserIdFromDn(String p_userDn)
    {
        // remove the key from the name

        String userId = p_userDn;
        String uidTag = LDAP_ATTR_USERID + "=";
        int startIndex = p_userDn.indexOf(uidTag);
        if (startIndex >= 0)
        {
            int endIndex = p_userDn.indexOf(",");
            // take off the uid= and the ",USER_BASE_DN
            userId = p_userDn.substring(startIndex + uidTag.length(), endIndex);
            userId.trim(); // trims off spaces before and after
        }
        return userId;
    }

    /**
     * Parse through the list of User DNs and pull out just the user ids.
     */
    static String[] parseUserIdsFromDns(String[] p_userDns)
    {
        String[] userIds = null;
        if (p_userDns != null && p_userDns.length > 0)
        {
            userIds = new String[p_userDns.length];
            for (int i = 0; i < p_userDns.length; i++)
            {
                userIds[i] = parseUserIdFromDn(p_userDns[i]);
            }
        }
        return userIds;
    }

    static List getListOfEmailInfo(NamingEnumeration p_searchResults)
            throws NamingException
    {

        List result = new ArrayList();

        while (p_searchResults.hasMoreElements())
        {
            Object searchResultObj = p_searchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tmpSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tmpSearchResult.getAttributes();
                result.add(getUserEmailInfo(entry));
            }
        }

        p_searchResults.close();
        return result.size() == 0 ? null : result;
    }

    /**
     * Convert a Attributes to a EmailInformation object.
     */
    static EmailInformation getUserEmailInfo(String p_userId, Attributes p_entry)
            throws NamingException
    {

        StringBuffer sb = new StringBuffer();

        Attribute attr = p_entry.get(LDAP_ATTR_FIRST_NAME);
        sb.append(getSingleAttributeValue(attr));
        sb.append(BLANK);

        attr = p_entry.get(LDAP_ATTR_LAST_NAME);
        sb.append(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_EMAIL);
        String email = getSingleAttributeValue(attr);

        attr = p_entry.get(LDAP_ATTR_CC_EMAIL);
        String ccEmail = getSingleAttributeValue(attr);

        attr = p_entry.get(LDAP_ATTR_BCC_EMAIL);
        String bccEmail = getSingleAttributeValue(attr);

        attr = p_entry.get(LDAP_ATTR_DEFAULT_UI_LOCALE);
        String uiLocale = getSingleAttributeValue(attr);

        attr = p_entry.get(LDAP_ATTR_COMPANY);
        String companyName = getSingleAttributeValue(attr);

        // return new EmailInformation(p_userId, sb.toString(), email,
        // uiLocale, getUserTimeZone(p_userId));
        EmailInformation eInfor = new EmailInformation(p_userId, sb.toString(),
                email, uiLocale, getUserTimeZone(p_userId));
        eInfor.setCCEmailAddress(ccEmail);
        eInfor.setBCCEmailAddress(bccEmail);
        eInfor.setCompanyName(companyName);
        return eInfor;

    }

    //
    // Private methods
    //

    /**
     * Convert a Attributes to a EmailInformation object.
     */
    private static EmailInformation getUserEmailInfo(Attributes p_entry)
            throws NamingException
    {

        Attribute attr = p_entry.get(LDAP_ATTR_USERID);
        String userId = getSingleAttributeValue(attr);

        return getUserEmailInfo(userId, p_entry);
    }

    private static TimeZone getUserTimeZone(String p_userId)
    {
        TimeZone timeZone = null;
        try
        {
            timeZone = ServerProxy.getCalendarManager().findUserTimeZone(
                    p_userId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get user time zone. ", e);
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }
}

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
package com.plug.Version_8_3_0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import com.util.ldap.LdapUtil;

public class UserLdapUtil extends LdapUtil
{
    private static final String LDAP_ATTR_USER_NAME = "cn";

    private static final String LDAP_ATTR_FIRST_NAME = "givenName";

    private static final String LDAP_ATTR_TITLE = "title";

    private static final String LDAP_ATTR_LAST_NAME = "sn";

    private static final String LDAP_ATTR_EMAIL = "email";

    private static final String LDAP_ATTR_CC_EMAIL = "emailCc";

    private static final String LDAP_ATTR_BCC_EMAIL = "emailBcc";

    private static final String LDAP_ATTR_PASSWORD = "userPassword";

    private static final String LDAP_ATTR_OFFICE_PHONE = "telephoneNumber";

    private static final String LDAP_ATTR_HOME_PHONE = "homePhone";

    private static final String LDAP_ATTR_FAX_NUMBER = "facsimileTelephoneNumber";

    private static final String LDAP_ATTR_CELL_NUMBER = "mobile";

    private static final String LDAP_ATTR_DEFAULT_UI_LOCALE = "defaultUILocale";

    private static final String LDAP_ATTR_ADDRESS = "postalAddress";

    private static final String LDAP_ATTR_COMPANY = "companyName";

    private static final String LDAP_ATTR_INALLPROJECTS = "isInAllProjects";

    private static final String LDAP_ATTR_STATUS = "status";

    private static final String LDAP_ATTR_OBJECT_CLASS = "objectclass";

    private static final String LDAP_USER_END_OBJECT_CLASS = "localizationPerson";

    private static final String LDAP_ANONYMOUS_USER_TYPE = "ANONYMOUS";

    private static final String LDAP_ATTR_TYPE = "employeeType";

    // maps to User.STATE.CREATED
    private static final String LDAP_CREATED_STATUS = "CREATED";

    // maps to User.State.ACTIVE
    private static final String LDAP_ACTIVE_STATUS = "ACTIVE";

    // maps to User.State.DELETED
    private static final String LDAP_DELETED_STATUS = "DELETED";

    // maps to User.State.DEACTIVE
    private static final String LDAP_DEACTIVE_STATUS = "DEACTIVE";

    private static final String[] LDAP_USER_OBJECT_CLASSES =
    { "top", "person", "organizationalPerson", "inetOrgPerson",
            "localizationPerson" };

    private static final String[] LDAP_ROLE_OBJECT_CLASSES =
    { "top", "groupOfUniqueNames", "localizationRole" };

    private static final String LDAP_ATTR_ACTIVITY = "activityType";
    private static final String LDAP_ATTR_SOURCE_LOCALE = "sourceLocale";
    private static final String LDAP_ATTR_TARGET_LOCALE = "targetLocale";
    private static final String LDAP_ATTR_COST = "cost";
    private static final String LDAP_ATTR_ROLE_TYPE = "roleType";
    private static final String LDAP_ATTR_ROLE_NAME = LDAP_ATTR_USER_NAME;
    private static final String LDAP_ATTR_RATES = "rateId";
    private static final String LDAP_ATTR_MEMBERSHIP = "uniqueMember";

    private static final String LDAP_ROLE_END_OBJECT_CLASS = "localizationRole";

    private static final String LDAP_ROLE_TYPE_CONTAINER = "C";

    private static final String LDAP_ROLE_TYPE_USER = "U";

    public static ModificationItem[] convertUserToModificationSet(User user)
    {
        ArrayList attrSet = new ArrayList();
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_USER_NAME, user.getUserId())));
        return (ModificationItem[]) attrSet.toArray(new ModificationItem[]
        {});
    }

    public static ModificationItem[] convertUserToModificationSet(String userId)
    {
        ArrayList attrSet = new ArrayList();
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                generateLDAPAttribute(LDAP_ATTR_USER_NAME, userId)));
        return (ModificationItem[]) attrSet.toArray(new ModificationItem[]
        {});
    }

    public static ModificationItem[] convertRoleToModificationSet(Role role)
    {
        ArrayList attrSet = new ArrayList();

        Attribute uniqueMemberAttr = new BasicAttribute(LDAP_ATTR_MEMBERSHIP);
        Map<String, String> members = role.getUniqueMembers();
        Set<String> keys = role.getUniqueMembers().keySet();
        for (String key : keys)
        {
            String userDn = members.get(key);
            uniqueMemberAttr.add(userDn);
        }
        attrSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                uniqueMemberAttr));

        return (ModificationItem[]) attrSet.toArray(new ModificationItem[]
        {});
    }

    public static Attributes convertUserRoleToLDAPEntry(Role p_role)
    {
        Attributes attrSet = new BasicAttributes();
        Attribute objClass = new BasicAttribute(LDAP_ATTR_OBJECT_CLASS);
        objClass.add(LDAP_ROLE_OBJECT_CLASSES[0]);
        objClass.add(LDAP_ROLE_OBJECT_CLASSES[1]);
        objClass.add(LDAP_ROLE_OBJECT_CLASSES[2]);
        attrSet.put(objClass);

        if (isStringValid(p_role.getActivityName()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_ACTIVITY, p_role
                    .getActivityName()));
        }

        if (isStringValid(p_role.getSourceLocale()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_SOURCE_LOCALE, p_role
                    .getSourceLocale()));
        }

        if (isStringValid(p_role.getTargetLocale()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_TARGET_LOCALE, p_role
                    .getTargetLocale()));
        }

        if (isStringValid(p_role.getRate()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_RATES, p_role.getRate()));
        }

        if (isStringValid(p_role.getCost()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_COST, p_role.getCost()));
        }

        if (isStringValid(p_role.getUserId()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_MEMBERSHIP,
                    getUserDN(p_role.getUserId())));
        }

        if (isStringValid(p_role.getRoleName()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_ROLE_NAME, p_role
                    .getRoleName()));
        }

        String status = getStateAsString(p_role.getState());
        attrSet.put(new BasicAttribute(LDAP_ATTR_STATUS, status));

        attrSet.put(new BasicAttribute(LDAP_ATTR_ROLE_TYPE, LDAP_ROLE_TYPE_USER));

        return attrSet;
    }

    public static Attributes convertUserToLDAPEntry(User user)
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
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_PASSWORD,
                    user.getPassword()));
        }

        if (isStringValid(user.getEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_EMAIL, user.getEmail()));
        }
        if (isStringValid(user.getCcEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_CC_EMAIL,
                    user.getCcEmail()));
        }
        if (isStringValid(user.getBccEmail()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_BCC_EMAIL,
                    user.getBccEmail()));
        }
        if (isStringValid(user.getPhoneNumber(User.PhoneType.HOME)))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_HOME_PHONE,
                    user.getPhoneNumber(User.PhoneType.HOME)));
        }
        if (isStringValid(user.getPhoneNumber(User.PhoneType.OFFICE)))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_OFFICE_PHONE,
                    user.getPhoneNumber(User.PhoneType.OFFICE)));
        }
        if (isStringValid(user.getPhoneNumber(User.PhoneType.FAX)))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_FAX_NUMBER,
                    user.getPhoneNumber(User.PhoneType.FAX)));
        }
        if (isStringValid(user.getPhoneNumber(User.PhoneType.CELL)))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_CELL_NUMBER,
                    user.getPhoneNumber(User.PhoneType.CELL)));
        }
        if (isStringValid(user.getDefaultLocale()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_DEFAULT_UI_LOCALE,
                    user.getDefaultLocale()));
        }
        if (isStringValid(user.getAddress()))
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_ADDRESS,
                    user.getAddress()));
        if (isStringValid(user.getCompanyName()))
        {
            attrSet.put(generateLDAPAttribute(LDAP_ATTR_COMPANY,
                    user.getCompanyName()));
        }
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

    public static String getSearchFilter()
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

    public static String getSearchFilterForContainerRolesOnUserId(
            String p_userId)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_ROLE_END_OBJECT_CLASS).append(")").append("(")
                .append(LDAP_ATTR_MEMBERSHIP).append("=")
                .append(getUserDN(p_userId)).append(")").append("(")
                .append(LDAP_ATTR_ROLE_TYPE).append("=")
                .append(LDAP_ROLE_TYPE_CONTAINER).append(")").append("(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_ACTIVE_STATUS).append("))");

        return buf.toString();
    }

    public static String getSearchFilterForUserRolesOnUserId(String p_userId)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_ROLE_END_OBJECT_CLASS).append(")").append("(")
                .append(LDAP_ATTR_MEMBERSHIP).append("=")
                .append(getUserDN(p_userId)).append(")").append("(")
                .append(LDAP_ATTR_ROLE_TYPE).append("=")
                .append(LDAP_ROLE_TYPE_USER).append(")").append("(|(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_ACTIVE_STATUS).append(")").append("(")
                .append(LDAP_ATTR_STATUS).append("=")
                .append(LDAP_CREATED_STATUS).append(")))");

        return buf.toString();
    }
    
    public static String getSearchFilterForRole()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("(&(").append(LDAP_ATTR_OBJECT_CLASS).append("=")
                .append(LDAP_ROLE_END_OBJECT_CLASS).append("))");

        return buf.toString();
    }

    public static String[] getRoleSearchAttributeNames()
    {
        return new String[]
        { LDAP_ATTR_ROLE_NAME, LDAP_ATTR_MEMBERSHIP };
    }

    public static List<User> getUsersFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {
        List<User> users = new ArrayList<User>();

        while (p_SearchResults.hasMoreElements())
        {
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                users.add(getUserFromLDAPEntry(entry));
            }
        }

        p_SearchResults.close();

        return users;
    }

    public static List<Role> getRolesFromSearchResults(
            NamingEnumeration p_SearchResults) throws NamingException
    {
        List<Role> roles = new ArrayList<Role>();

        while (p_SearchResults.hasMoreElements())
        {
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tempSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tempSearchResult.getAttributes();
                roles.add(getRoleFromLDAPEntry(entry));
            }
        }

        p_SearchResults.close();

        return roles;
    }

    public static Role getRoleFromLDAPEntry(Attributes p_entry)
            throws NamingException
    {
        Role role = new Role();

        Attribute attr = p_entry.get(LDAP_ATTR_ROLE_NAME);
        role.setRoleName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_STATUS);
        String status = getSingleAttributeValue(attr);
        role.setState(getStateAsInt(status));

        attr = p_entry.get(LDAP_ATTR_ROLE_TYPE);
        String roleType = getSingleAttributeValue(attr);
        if (LDAP_ROLE_TYPE_USER.equals(roleType))
        {
            attr = p_entry.get(LDAP_ATTR_ACTIVITY);
            role.setActivityName(getSingleAttributeValue(attr));

            attr = p_entry.get(LDAP_ATTR_SOURCE_LOCALE);
            role.setSourceLocale(getSingleAttributeValue(attr));

            attr = p_entry.get(LDAP_ATTR_TARGET_LOCALE);
            role.setTargetLocale(getSingleAttributeValue(attr));

            attr = p_entry.get(LDAP_ATTR_COST);
            role.setCost(getSingleAttributeValue(attr));

            attr = p_entry.get(LDAP_ATTR_RATES);
            role.setRate(getSingleAttributeValue(attr));

            attr = p_entry.get(LDAP_ATTR_MEMBERSHIP);
            String userDn = getSingleAttributeValue(attr);
            String userId = parseUserIdFromDn(userDn);
            role.setUserId(userId);
        }
        else
        {
            attr = p_entry.get(LDAP_ATTR_MEMBERSHIP);
            if (getMultiAttributeValue(attr) != null)
            {
                List<String> userDns = getMultiAttributeValue(attr);
                for (int i = 0; i < userDns.size(); i++)
                {
                    String userDn = (String) userDns.get(i);
                    String userId = parseUserIdFromDn(userDn);
                    role.setUniqueMember(userId, userDn);
                }
            }
        }

        return role;
    }

    public static User getUserFromLDAPEntry(Attributes p_entry)
            throws NamingException
    {
        User user = new User();

        Attribute attr = p_entry.get(LDAP_ATTR_USERID);
        user.setUserId(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_USER_NAME);
        user.setUserName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_TITLE);
        user.setTitle(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_FIRST_NAME);
        user.setFirstName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_PASSWORD);
        user.setPassword(getSinglePasswordAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_LAST_NAME);
        user.setLastName(getSingleAttributeValue(attr));

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
        user.setCcEmail(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_BCC_EMAIL);
        user.setBccEmail(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_HOME_PHONE);
        user.setPhoneNumber(User.PhoneType.HOME, getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_OFFICE_PHONE);
        user.setPhoneNumber(User.PhoneType.OFFICE,
                getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_FAX_NUMBER);
        user.setPhoneNumber(User.PhoneType.FAX, getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_CELL_NUMBER);
        user.setPhoneNumber(User.PhoneType.CELL, getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_DEFAULT_UI_LOCALE);
        user.setDefaultLocale(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_INALLPROJECTS);
        if (attr != null
                && getSingleAttributeValue(attr).equalsIgnoreCase(
                        LDAP_ATTR_TRUE))
        {
            // set to "false" as default - so only need to set to true
            // if value is "true"
            user.setIsInAllProjects(true);
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

    public static String getStateAsString(int p_userState)
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

    public static int getStateAsInt(String p_userState)
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

    public static String parseUserIdFromDn(String p_userDn)
    {
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

    public static String getRoleDN(String p_roleId)
    {
        return LDAP_ATTR_ROLE_NAME + "=" + p_roleId + ", " + ROLE_BASE_DN;
    }
}

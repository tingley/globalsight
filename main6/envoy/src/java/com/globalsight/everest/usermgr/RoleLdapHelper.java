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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
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

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.ContainerRoleImpl;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.persistence.project.ProjectUnnamedQueries;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class RoleLdapHelper extends LdapHelper
{
    public static final String LDAP_ROLE_NAME_DELIMITER = " ";
    public static final String LDAP_ATTR_ACTIVITY = "activityType";
    public static final String LDAP_ATTR_SOURCE_LOCALE = "sourceLocale";
    public static final String LDAP_ATTR_TARGET_LOCALE = "targetLocale";
    public static final String LDAP_ATTR_COST = "cost";
    public static final String LDAP_ATTR_ROLE_TYPE = "roleType";
    public static final String LDAP_ATTR_ROLE_NAME = "cn";
    public static final String LDAP_ATTR_RATES = "rateId";
    public static final String LDAP_ATTR_MEMBERSHIP = "uniqueMember";

    //
    // Package specific constants
    //
    static final String ROLE_BASE_DN = "ou=Groups," + LDAP_BASE;
    static final String LDAP_ATTR_OBJECT_CLASS = "objectclass";
    static final String ROLE_LDAP_RDN_ATTRIBUTE = LDAP_ATTR_ROLE_NAME;

    //
    // Protected constants
    //

    protected static final String[] LDAP_USER_OBJECT_CLASSES =
    { "top", "groupOfUniqueNames", "localizationRole" };
    protected static final String LDAP_ROLE_END_OBJECT_CLASS = "localizationRole";

    //
    // Private constants
    //

    private static final Logger CATEGORY = Logger
            .getLogger(RoleLdapHelper.class.getName());

    private static final String LDAP_FILTER_ACTIVE_ROLES = "("
            + LDAP_ATTR_STATUS + "=" + LDAP_ACTIVE_STATUS + ")";

    private static final String LDAP_FILTER_DEACTIVE_ROLES = "("
            + LDAP_ATTR_STATUS + "=" + LDAP_DEACTIVE_STATUS + ")";

    private static final String LDAP_FILTER_OUT_DELETED_DEACTIVE_ROLES = "(|("
            + LDAP_ATTR_STATUS + "=" + LDAP_ACTIVE_STATUS + ")" + "("
            + LDAP_ATTR_STATUS + "=" + LDAP_CREATED_STATUS + "))";

    //
    // Package specific methods
    //

    RoleLdapHelper()
    {
        // empty constructor.
    }

    /**
     * Return the distinguished name of the role. This is the key to find the
     * role in LDAP.
     */
    static String getRoleDN(String p_roleId)
    {
        return ROLE_LDAP_RDN_ATTRIBUTE + "=" + p_roleId + ", " + ROLE_BASE_DN;
    }

    /**
     * Gets the LDAP search filter for searching all LDAP role group entries.
     * <p>
     * since there is no company property for the group entry, we have to search
     * all the group entries.
     * 
     * @param searchOutOfDateRoles
     *            - whether to search the groups that are in deleted and
     *            inactive status.
     */
    static String getSearchFilterForAllRoles(boolean searchOutOfDateRoles)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("(&(");
        buf.append(LDAP_ATTR_OBJECT_CLASS);
        buf.append("=");
        buf.append(LDAP_ROLE_END_OBJECT_CLASS);
        buf.append(")");
        if (!searchOutOfDateRoles)
        {
            buf.append("(!(");
            buf.append(LDAP_ATTR_STATUS);
            buf.append("=");
            buf.append(LDAP_DELETED_STATUS);
            buf.append("))");
            buf.append("(!(");
            buf.append(LDAP_ATTR_STATUS);
            buf.append("=");
            buf.append(LDAP_DEACTIVE_STATUS);
            buf.append("))");
        }
        buf.append(")");

        return buf.toString();
    }

    /**
     * Return the distinguished name of the roles. This is the key to find the
     * roles in LDAP.
     */
    static String getSearchFilterForRolesOnRoleNames(String[] p_roleIds)
    {
        int size = p_roleIds == null ? 0 : p_roleIds.length;

        if (size == 0)
        {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("(&(");
        sb.append(LDAP_ATTR_OBJECT_CLASS);
        sb.append("=");
        sb.append(LDAP_ROLE_END_OBJECT_CLASS);
        sb.append(")(|");

        for (int i = 0; i < size; i++)
        {
            sb.append("(");
            sb.append(LDAP_ATTR_ROLE_NAME);
            sb.append("=");
            sb.append(p_roleIds[i]);
            sb.append(") ");
        }

        sb.append(")");
        sb.append(LDAP_FILTER_ACTIVE_ROLES);
        sb.append(")");

        return sb.toString();
    }

    /**
     * This method will first call UserLdapHelper.parseUserIdFromDn to parse the
     * user id from the user DNs and then only selects the users that are part
     * of the given list of project users.
     */
    static ArrayList filterRoleMembers(String[] p_userDns, Set p_projectUserIds)
    {
        ArrayList userIds = new ArrayList();
        if (p_userDns != null && p_userDns.length > 0)
        {
            for (int i = 0; i < p_userDns.length; i++)
            {
                String userId = UserLdapHelper.parseUserIdFromDn(p_userDns[i]);
                // if the list of project users is null, then add all users.
                // Also if the the user is part of the project's user
                if (p_projectUserIds == null
                        || p_projectUserIds.contains(userId))
                {
                    userIds.add(userId);
                }
            }
        }
        return userIds;
    }

    /**
     * Convert the specified role into an entry for LDAP to handle.
     */
    static Attributes convertRoleToLdapEntry(Role p_role)
    {
        Attributes attrSet = new BasicAttributes();
        Attribute objClass = new BasicAttribute(LDAP_ATTR_OBJECT_CLASS);
        objClass.add(LDAP_USER_OBJECT_CLASSES[0]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[1]);
        objClass.add(LDAP_USER_OBJECT_CLASSES[2]);
        attrSet.put(objClass);

        Activity activity = p_role.getActivity();
        if (activity != null && isStringValid(activity.getName()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_ACTIVITY, activity
                    .getName()));
        }

        if (p_role != null && isStringValid(p_role.getSourceLocale()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_SOURCE_LOCALE, p_role
                    .getSourceLocale()));
        }

        if (p_role != null && isStringValid(p_role.getTargetLocale()))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_TARGET_LOCALE, p_role
                    .getTargetLocale()));
        }

        // ---- rates----
        Collection r = p_role.getRates();
        if (r.size() > 0)
        {
            // store all the rates in a String array
            Object[] rateElements = r.toArray();
            Attribute attr = new BasicAttribute(LDAP_ATTR_RATES);
            for (int i = 0; i < r.size(); i++)
            {
                attr.add(Long.toString(((Rate) rateElements[i]).getId()));
            }

            attrSet.put(attr);
        }

        // --- status field
        String status = getStateAsString(p_role.getState());
        attrSet.put(new BasicAttribute(LDAP_ATTR_STATUS, status));
        String roleId = p_role.getName();

        // If we're a ContainerRole, then the role type is "C", and the
        // membership consists of a String array of all the uids.
        if (p_role instanceof ContainerRole)
        {
            ContainerRole containerRole = (ContainerRole) p_role;

            String roleType = ContainerRole.ROLE_TYPE_VALUE;
            attrSet.put(new BasicAttribute(LDAP_ATTR_ROLE_TYPE, roleType));

            Collection c = containerRole.getUsers();
            Object[] colElements = c.toArray();

            if (c.size() > 0)
            {
                String[] users = new String[c.size()];
                for (int i = 0; i < c.size(); i++)
                {
                    users[i] = UserLdapHelper.getUserDN(((User) colElements[i])
                            .getUserId());
                }
                attrSet.put(generateUniqueMemberAttr(LDAP_ATTR_MEMBERSHIP,
                        users));
            }
            else
            {
                attrSet.put(new BasicAttribute(LDAP_ATTR_MEMBERSHIP, ""));
            }
        }

        // If we're a UserRole, then the role type is "U", we fill in cost
        // information, and the membership consists of the uid of the user
        // assigned to the role.
        if (p_role instanceof UserRole)
        {
            UserRole userRole = (UserRole) p_role;

            String roleType = UserRole.ROLE_TYPE_VALUE;
            attrSet.put(new BasicAttribute(LDAP_ATTR_ROLE_TYPE, roleType));

            String cost = userRole.getCost();
            if (cost != null)
            {
                attrSet.put(new BasicAttribute(LDAP_ATTR_COST, cost));
            }

            String uid = userRole.getUser();
            if (uid != null)
            {
                attrSet.put(new BasicAttribute(LDAP_ATTR_MEMBERSHIP,
                        UserLdapHelper.getUserDN(uid)));
            }
        }

        if (isStringValid(roleId))
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_ROLE_NAME, roleId));
        }

        return attrSet;
    }

    /**
     * @param p_searchResults
     *            - The search results for querying for role(s) These results
     *            are used to build Roles from.
     * @param p_withRates
     *            - Specifies 'true' if the rate collection associated with the
     *            role should be populated (Role.getRates()) or 'false' if it
     *            shouldn't be populated. Performance will be much better if the
     *            rates do not need to be retrieved.
     * @param p_projectId
     *            - The id of the project where a user for the given role
     *            belongs to. This is an optional parameter.
     */
    static Vector<Role> getRolesFromSearchResults(
            NamingEnumeration p_searchResults, boolean p_withRates,
            String companyId, long p_projectId) throws NamingException,
            UserManagerException
    {

        Vector<Role> roleList = new Vector<Role>();
        while (p_searchResults.hasMoreElements())
        {
            Object searchResultObj = p_searchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                // For "AppletResourceBundle_en.properties  file issue" issue
                SearchResult tmpSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tmpSearchResult.getAttributes();

                try
                {
                    Role role = getRoleFromLdapEntry(entry, p_withRates,
                            companyId, p_projectId);
                    if (role != null)
                    {
                        roleList.addElement(role);
                    }
                }
                catch (UserManagerException ume)
                {
                    // Couldn't find the activity in database. Already
                    // logged by previous message, so just continue.
                }
            }
        }
        p_searchResults.close();

        return (roleList.size() == 0 ? null : roleList);
    }

    static String[] getSearchAttributeNames()
    {
        return new String[]
        { LDAP_ATTR_ACTIVITY, LDAP_ATTR_SOURCE_LOCALE, LDAP_ATTR_TARGET_LOCALE,
                LDAP_ATTR_COST, LDAP_ATTR_RATES, LDAP_ATTR_ROLE_TYPE,
                LDAP_ATTR_ROLE_NAME, LDAP_ATTR_MEMBERSHIP, LDAP_ATTR_STATUS };
    }

    static String getSearchFilterForContainerRolesOnUserId(String p_userId)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")(" + LDAP_ATTR_MEMBERSHIP
                + "=" + UserLdapHelper.getUserDN(p_userId) + ")("
                + LDAP_ATTR_ROLE_TYPE + "=" + ContainerRole.ROLE_TYPE_VALUE
                + ")" + LDAP_FILTER_ACTIVE_ROLES + ")";
    }

    static String getSearchFilterForUserRolesOnUserId(String p_userId)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")(" + LDAP_ATTR_MEMBERSHIP
                + "=" + UserLdapHelper.getUserDN(p_userId) + ")("
                + LDAP_ATTR_ROLE_TYPE + "=" + UserRole.ROLE_TYPE_VALUE + ")"
                + LDAP_FILTER_OUT_DELETED_DEACTIVE_ROLES + ")";
    }

    static String getSearchFilterForRoleOnRoleName(String p_roleName)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")(" + LDAP_ATTR_ROLE_NAME + "="
                + p_roleName + ") " + LDAP_FILTER_ACTIVE_ROLES + ")";
    }

    /**
     * Get the LDAP search filter for searching LDAP Role entry that match the
     * given attributes.
     */
    static String getSearchFilter(Attribute[] p_attrs) throws NamingException
    {
        if (p_attrs == null)
        {
            return null;
        }

        String filterPart1 = "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")";
        String filterPart2 = "";

        for (int i = 0; i < p_attrs.length; i++)
        {
            filterPart2 += "(" + p_attrs[i].getID() + "="
                    + getSingleAttributeValue(p_attrs[i]) + ")";
        }

        String filterPart3 = LDAP_FILTER_ACTIVE_ROLES + ")";

        String filter = filterPart1 + filterPart2 + filterPart3;

        return filter;
    }

    /**
     * Get the Uset ID array from a NamingEnumeration. Returns an empty array if
     * no uids are found in the search result.
     */
    static String[] getUIDsFromSearchResults(NamingEnumeration p_SearchResults)
            throws NamingException
    {
        String[] output = new String[0];

        while (p_SearchResults.hasMoreElements())
        {
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tmpSearchResult = (SearchResult) searchResultObj;
                Attributes entry = tmpSearchResult.getAttributes();
                Attribute attr = entry.get(LDAP_ATTR_MEMBERSHIP);
                if (attr != null)
                {
                    NamingEnumeration userDns = attr.getAll();
                    String[] userIds = new String[attr.size()];
                    int i = 0;
                    while (userDns.hasMoreElements())
                    {
                        String userDn = userDns.nextElement().toString();
                        String userId = UserLdapHelper
                                .parseUserIdFromDn(userDn);
                        userIds[i++] = userId;
                    }
                    userDns.close();
                    output = combineArrays(output, userIds);
                }
            }
        }
        p_SearchResults.close();

        return output;
    }

    /**
     * Get the LDAP search filter for searching LDAP group entries that contains
     * a userID.
     */
    static String getSearchFilterOnUserId(String p_userId)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")(" + LDAP_ATTR_MEMBERSHIP
                + "=" + UserLdapHelper.getUserDN(p_userId) + ")"
                + LDAP_FILTER_ACTIVE_ROLES + ")";
    }

    /**
     * Get the LDAP search filter for searching LDAP group/role entires that are
     * inactive and contain the specified userId.
     */
    static String getSearchFilterForInactiveRolesOnUserId(String p_userId)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "="
                + LDAP_ROLE_END_OBJECT_CLASS + ")(" + LDAP_ATTR_MEMBERSHIP
                + "=" + UserLdapHelper.getUserDN(p_userId) + ")"
                + LDAP_FILTER_DEACTIVE_ROLES + ")";
    }

    /**
     * Generate a set of role entry DNs from a NamingEnumeration after a search
     * is carried out.
     */
    static Vector getRoleDNsFromSearchResults(NamingEnumeration p_SearchResults)
            throws NamingException
    {

        Vector dnList = new Vector();
        while (p_SearchResults.hasMoreElements())
        {
            Object searchResultObj = p_SearchResults.nextElement();
            if (searchResultObj instanceof SearchResult)
            {
                SearchResult tmpSearchResult = (SearchResult) searchResultObj;
                // get DN of Search Result
                String dn = ((Context) tmpSearchResult.getObject())
                        .getNameInNamespace();
                dnList.addElement(dn);
            }
        }
        p_SearchResults.close();

        return dnList.size() == 0 ? null : dnList;
    }

    // private static String getEntryDN(Attributes entry) throws NamingException
    // {
    // Attribute cnAttr = entry.get("cn");
    // String cnValue = getSingleAttributeValue(cnAttr);
    // String tmpDN = "cn=" + cnValue + "," + ROLE_BASE_DN;
    //
    // return tmpDN;
    // }

    /**
     * Generate a ModificationItem[] for deleting users from a role.
     */
    static ModificationItem[] deleteUsersModificationSet(String[] p_uids)
    {
        if (p_uids == null)
        {
            return null;
        }

        ModificationItem[] attrSet = new ModificationItem[p_uids.length];
        for (int i = 0; i < p_uids.length; i++)
        {
            String userId = p_uids[i];
            attrSet[i] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_MEMBERSHIP,
                            UserLdapHelper.getUserDN(userId)));
        }

        return attrSet;
    }

    /**
     * Generate a ModificationItem[] for deleting rates from a role.
     */
    static ModificationItem[] deleteRatesModificationSet(String[] p_rates)
    {
        if (p_rates == null || p_rates.length <= 0)
        {
            return null;
        }

        ModificationItem[] attrSet = new ModificationItem[p_rates.length];
        for (int i = 0; i < p_rates.length; i++)
        {
            attrSet[i] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_RATES, p_rates[i]));
        }

        return attrSet;
    }

    /**
     * Delete all the rates on a role.
     */
    static ModificationItem deleteAllRatesModification()
    {
        // specifying a NULL for the value will remove all rates on the role.
        String allRates = null;
        Attribute attr = new BasicAttribute(LDAP_ATTR_RATES, allRates);
        ModificationItem mod = new ModificationItem(
                DirContext.REMOVE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Delete all the members of a role.
     */
    static ModificationItem deleteAllRoleMembers()
    {
        // specifying a NULL value will remove all members on the role.
        String allMembers = null;
        Attribute attr = new BasicAttribute(LDAP_ATTR_MEMBERSHIP, allMembers);
        ModificationItem mod = new ModificationItem(
                DirContext.REMOVE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generates an ModificationItem object for delete role operation. It
     * actually just sets the status to 'DELETED' instead of deleting the LDAP
     * entry. This enables current job to continue processing using this role,
     * however no new jobs can be created using the role.
     */
    static ModificationItem getLDAPModificationForDeleteRole()
    {
        Attribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_DELETED_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generates an ModificationItem object for deactivating a role. This
     * enables current job to continue processing using this role, however no
     * new jobs can be created using the role.
     */
    static ModificationItem getLDAPModificationForDeactivateRole()
    {

        Attribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_DEACTIVE_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generate an ModificationItem object for re-activating a role. It sets the
     * user role to 'ACTIVE' in the LDAP entry.
     */
    static ModificationItem getLDAPModificationForReactivateRole()
    {

        Attribute attr = new BasicAttribute(LDAP_ATTR_STATUS,
                LDAP_ACTIVE_STATUS);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generate an ModificationItem object for updating the rateId
     */
    static ModificationItem getLDAPModificationForUpdatingRate(String p_rateId)
    {

        Attribute attr = new BasicAttribute(LDAP_ATTR_RATES, p_rateId);
        ModificationItem mod = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, attr);

        return mod;
    }

    /**
     * Generate a ModificationItem[] for adding users to a role.
     */
    static ModificationItem[] addUsersModificationSet(String[] p_uids)
    {

        if (p_uids == null)
        {
            return null;
        }

        ModificationItem[] attrSet = new ModificationItem[p_uids.length];
        for (int i = 0; i < p_uids.length; i++)
        {
            String userId = p_uids[i];
            attrSet[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_MEMBERSHIP,
                            UserLdapHelper.getUserDN(userId)));
        }

        return attrSet;
    }

    /**
     * Generate a ModificationItem[] for adding a rate to a role.
     */
    static ModificationItem[] addRatesModificationSet(String[] p_rates)
    {

        if (p_rates == null)
        {
            return null;
        }

        ModificationItem[] attrSet = new ModificationItem[p_rates.length];
        for (int i = 0; i < p_rates.length; i++)
        {
            attrSet[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                    generateLDAPAttribute(LDAP_ATTR_RATES, p_rates[i]));
        }

        return attrSet;
    }

    //
    // Private methods
    //

    /**
     * @param p_entry
     *            - One LDAP entry that represents a role
     * @param p_withRates
     *            - Specifies 'true' if the rate collection associated with the
     *            role should be populated, 'false' if it shouldn't be
     *            populated.
     * @param p_projectId
     *            - The id of the project where a user for the given role
     *            belongs to. This is an optional parameter.
     */
    private static Role getRoleFromLdapEntry(Attributes p_entry,
            boolean p_withRates, String p_companyId, long p_projectId)
            throws UserManagerException, NamingException
    {

        Role retVal = null;
        Attribute attr = p_entry.get(LDAP_ATTR_ROLE_TYPE);
        String roleType = getSingleAttributeValue(attr);

        JobHandler jh = getJobHandler();
        CostingEngine ce = getCostingEngine();
        String activityName = null;

        // For "AppletResourceBundle_en.properties  file issue" issue
        String companyId = p_companyId == null ? CompanyWrapper
                .getCurrentCompanyId() : p_companyId;
        boolean isSuperAdmin = CompanyWrapper.SUPER_COMPANY_ID
                .equalsIgnoreCase(companyId);
        try
        {
            attr = p_entry.get(LDAP_ATTR_ACTIVITY);
            activityName = getSingleAttributeValue(attr);
            int boundry = activityName.lastIndexOf("_");
            String comStr = activityName.substring(boundry + 1);
            if (comStr.equalsIgnoreCase(companyId) || isSuperAdmin)
            {
                if (roleType.equals(UserRole.ROLE_TYPE_VALUE))
                {
                    retVal = new UserRoleImpl();

                    Activity act = jh.getActivity(activityName);
                    retVal.setActivity(act);

                    attr = p_entry.get(LDAP_ATTR_SOURCE_LOCALE);
                    retVal.setSourceLocale(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_TARGET_LOCALE);
                    retVal.setTargetLocale(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_ROLE_NAME);
                    retVal.setName(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_COST);
                    ((UserRole) retVal).setCost(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_RATES);
                    ((UserRole) retVal).setRate(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_MEMBERSHIP);
                    String userDn = getSingleAttributeValue(attr);
                    String userId = UserLdapHelper.parseUserIdFromDn(userDn);
                    ((UserRole) retVal).setUser(userId);

                }
                else if (roleType.equals(ContainerRole.ROLE_TYPE_VALUE))
                {
                    retVal = new ContainerRoleImpl();

                    attr = p_entry.get(LDAP_ATTR_ACTIVITY);
                    activityName = getSingleAttributeValue(attr);

                    Activity act = jh.getActivity(activityName);
                    retVal.setActivity(act);

                    attr = p_entry.get(LDAP_ATTR_SOURCE_LOCALE);
                    retVal.setSourceLocale(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_TARGET_LOCALE);
                    retVal.setTargetLocale(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_ROLE_NAME);
                    retVal.setName(getSingleAttributeValue(attr));

                    attr = p_entry.get(LDAP_ATTR_MEMBERSHIP);
                    if (getMultiAttributeValue(attr) != null)
                    {
                        Vector userDns = getMultiAttributeValue(attr);
                        Vector userIds = new Vector(userDns.size());

                        for (int i = 0; i < userDns.size(); i++)
                        {
                            String userDn = (String) userDns.get(i);
                            String userId = UserLdapHelper
                                    .parseUserIdFromDn(userDn);
                            userIds.add(userId);
                        }

                        // only add user if he/she is in the specified project
                        if (usersInProject(userIds, p_projectId))
                        {
                            ((ContainerRole) retVal).addUsers(userIds);
                        }
                    }

                    // if the rate collection should be populated - then
                    // retrieve them
                    if (p_withRates)
                    {
                        attr = p_entry.get(LDAP_ATTR_RATES);
                        if (getMultiAttributeValue(attr) != null)
                        {
                            Rate r = null;
                            try
                            {
                                Vector rates = getMultiAttributeValue(attr);
                                for (int i = 0; i < rates.size(); i++)
                                {
                                    r = ce.getRate(Long
                                            .parseLong((String) rates.get(i)));
                                    ((ContainerRole) retVal).addRate(r);
                                }
                            }
                            catch (CostingException cex)
                            {
                                // just log error
                                CATEGORY.error(
                                        "Couldn't get the rate " + r.getId()
                                                + " from the CostingEngine",
                                        cex);
                            }
                        }
                    }
                }

                attr = p_entry.get(LDAP_ATTR_STATUS);
                String status = getSingleAttributeValue(attr);
                if (status != null)
                {
                    retVal.setState(getStateAsInt(status));
                }

            }
        }
        catch (Exception je)
        {
            CATEGORY.error("Couldn't find the activity " + activityName, je);

            String args[] =
            { activityName };
            throw new UserManagerException(
                    UserManagerException.MSG_GET_ACTIVITY_ERROR, args, je);
        }

        return retVal;
    }

    /**
     * Wraps the code for getting the job handler and handles any exceptions.
     */
    private static JobHandler getJobHandler() throws UserManagerException
    {
        JobHandler jh = null;

        try
        {
            jh = ServerProxy.getJobHandler();
        }
        catch (Exception e)
        {
            CATEGORY.error("Couldn't find the JobHandler", e);

            throw new UserManagerException(
                    UserManagerException.MSG_GET_JOB_HANDLER_ERROR, null, e);
        }

        return jh;
    }

    /**
     * Wraps the code for getting the costing engine and handles any exceptions.
     */
    private static CostingEngine getCostingEngine() throws UserManagerException
    {
        CostingEngine ce = null;

        try
        {
            ce = ServerProxy.getCostingEngine();
        }
        catch (Exception e)
        {
            CATEGORY.error("Couldn't find the CostingEngine", e);

            throw new UserManagerException(
                    UserManagerException.MSG_GET_COSTING_ENGINE_ERROR, null, e);
        }

        return ce;
    }

    /**
     * Returns true if at least one user is in the project with the given id.
     * Otherwise, returns false.
     */
    private static boolean usersInProject(Vector p_userIds, long p_projectId)
            throws Exception
    {
        boolean usersInProject = false;
        if (p_projectId > 0)
        {
            String sql = ProjectUnnamedQueries.countUsersInProject(p_userIds,
                    p_projectId);
            int count = HibernateUtil.countWithSql(sql, null);
            if (count > 0)
            {
                usersInProject = true;
            }
        }
        return usersInProject;
    }
}

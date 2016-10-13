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

//GlobalSight
import com.globalsight.everest.foundation.Group;
import com.globalsight.everest.foundation.GroupImpl;

import com.globalsight.everest.usermgr.LdapHelper;
import com.globalsight.everest.usermgr.UserLdapHelper;

// connects openldap by jndi
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


import java.util.*;

/**
 * GroupLdapHelper, a LDAP Helper class that helps convert data for
 * LDAP operations on the 'Group' object.
 * Used just within the usermgr package so methods are not PUBLIC.
 */
public class GroupLdapHelper
    extends LdapHelper
{
    public static final String LDAP_ATTR_GROUP_NAME = "cn";
    public static final String LDAP_ATTR_GROUP_DESCRIPTION = "description";
    public static final String LDAP_ATTR_GROUP_PERMISSION = "permission";
    public static final String LDAP_ATTR_GROUP_MEMBER = "uniqueMember";

    //
    // Package specific constants
    //

    static final String GROUP_BASE_DN =
        "ou=accessGroups," + LDAP_BASE;

    static final String AMBASSADOR_GROUP_BASE_DN =
        "ou=GlobalSight,ou=accessGroups," + LDAP_BASE;
    static final String VENDOR_GROUP_BASE_DN =
        "ou=VendorManagement,ou=accessGroups," + LDAP_BASE;

    static final String LDAP_ATTR_OBJECT_CLASS = "objectclass";

    /** Object type for the Group object in LDAP. */
    static final String LDAP_GROUP_END_OBJECT_CLASS = "AccessGroup";

    static final String GROUP_LDAP_RDN_ATTRIBUTE = LDAP_ATTR_GROUP_NAME;

    /** 
     * Object type for the Group object in LDAP (not used anywhere). 
     */
    protected static final String[] LDAP_GROUP_OBJECT_CLASSES =
        {"top", "groupOfUniqueNames", "AccessGroup"};


    //
    // Package methods
    //

    GroupLdapHelper()
    {
        // empty constructor.
    }

    /**
     * Get the LDAP entry DN for the given groupName
     */
    static String getGroupDN(String p_grpName)
    {
        if (p_grpName.toLowerCase().startsWith("vendor"))
        {
            return GROUP_LDAP_RDN_ATTRIBUTE + "=" + p_grpName +
                ", " + VENDOR_GROUP_BASE_DN;
        }
        else
        {
            return GROUP_LDAP_RDN_ATTRIBUTE + "=" + p_grpName +
                ", " + AMBASSADOR_GROUP_BASE_DN;
        }
    }

    /**
     * Generate a ModificationItem Set for adding users to group operation.
     */
    static ModificationItem[] addUsersModificationSet(
        String grpName, String[] p_uids)
    {
        if (p_uids == null)
        {
            return null;
        }

        ModificationItem[]  attrSet = new ModificationItem[p_uids.length];

        for (int i = 0; i < p_uids.length; i++) 
        {
            attrSet[i] = new ModificationItem( DirContext.ADD_ATTRIBUTE,
                             generateLDAPAttribute( LDAP_ATTR_GROUP_MEMBER,
                                       UserLdapHelper.getUserDN(p_uids[i])));               
        }

        return attrSet;
    }

    /**
     * Generate a ModificationItem Set for deleting users from group operation.
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
            attrSet[i] = new ModificationItem( DirContext.REMOVE_ATTRIBUTE,
                             generateLDAPAttribute( LDAP_ATTR_GROUP_MEMBER,
                                      UserLdapHelper.getUserDN(p_uids[i]) ) );
        }

        return attrSet;
    }

    /**
     * Get the target attribute names for searching LDAP group entries.
     */
    static String[] getSearchAttributeNames()
    {
        return new String[] { LDAP_ATTR_GROUP_NAME,
                              LDAP_ATTR_GROUP_PERMISSION,
                              LDAP_ATTR_GROUP_DESCRIPTION,
                              LDAP_ATTR_GROUP_MEMBER
        };
    }

    /**
     * Get the LDAP search filter for searching LDAP group entries.
     */
    static String getSearchFilter()
    {
        return "(" + LDAP_ATTR_OBJECT_CLASS + "=" +
               LDAP_GROUP_END_OBJECT_CLASS + ")";
    }

    /**
     * Get the LDAP search filter for searching LDAP group entries
     * that contains a userID.
     */
    static String getSearchFilterOnUserId(String p_userId)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "=" + 
               LDAP_GROUP_END_OBJECT_CLASS +")(" + LDAP_ATTR_GROUP_MEMBER + 
               "=" + UserLdapHelper.getUserDN(p_userId) + "))";
    }

    /**
     * Get the LDAP search filter for searching LDAP group entry that
     * matchs the given name.
     */
    static String getSearchFilterOnGroupname(String p_grpName)
    {
        return "(&(" + LDAP_ATTR_OBJECT_CLASS + "=" + 
               LDAP_GROUP_END_OBJECT_CLASS + ")(" + LDAP_ATTR_GROUP_NAME + 
               "=" + p_grpName + "))";
    }

    /**
     * Generate a collection of Group objects from a NamingEnumeration
     * after a search is carried out.
     */
    static Vector getGroupsFromSearchResults(NamingEnumeration p_SearchResults) 
    throws NamingException 
    {
        Vector grpList = new Vector();

        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            SearchResult sr = (SearchResult)p_SearchResults.next();
            Attributes entry = sr.getAttributes();
            grpList.addElement(getGroupFromLDAPEntry(entry));
        }
        
        p_SearchResults.close();

        return grpList.size() == 0 ? null : grpList;
    }

    /**
     * Generate a set of group entry DNs from a NamingEnumeration
     * after a search is carried out.
     */
    static Vector getGroupDNsFromSearchResults(NamingEnumeration p_SearchResults) 
    throws NamingException 
    {       
        Vector grpList = new Vector();

        while (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            SearchResult sr = (SearchResult)p_SearchResults.next();
            String dn = ((Context)sr.getObject()).getNameInNamespace();
            grpList.addElement(dn);
        }
        
        p_SearchResults.close();

        return grpList.size() == 0 ? null : grpList;
    }

    /**
     * Get the Uset ID array for the given group from a
     * NamingEnumeration.  Returns an empty array if no uids are found
     * in the search result.
     */
    static String[] getGroupUserIDsFromSearchResults(NamingEnumeration p_SearchResults)
    throws NamingException 
    {
        String[] userIds = new String[]{};

        if (p_SearchResults.hasMoreElements())
        {
            /* Next directory entry */
            SearchResult sr = (SearchResult)p_SearchResults.next();
            Attributes entry = sr.getAttributes();
            NamingEnumeration namingEnu = (entry.get(LDAP_ATTR_GROUP_MEMBER)).getAll();
            
            String[] userDns = new String[]{};
            for(int i=0; namingEnu.hasMoreElements();i++) 
            {
                userDns[i] = namingEnu.next().toString();
            }
            namingEnu.close();

            for (int i = 0; i < userDns.length; i++)
            {
                String userId = UserLdapHelper.parseUserIdFromDn(userDns[i]);
                userIds[i] = userId;
            }
        }
        
        p_SearchResults.close();

        return userIds;
    }


    /**
     * Get names of the given groups suppose each group must have a name
     */
    static String[] getNames(Vector p_grps)
    {
        String[] names = null;

        if (p_grps != null)
        {
            int size = p_grps.size();
            names = new String[size];

            for (int i = 0; i < size; i++)
            {
                names[i] = ((Group)p_grps.elementAt(i)).getGroupName();
            }
        }

        return names;
    }

    /**
     * Convert a Group object to an Attributes object
     *
     * @return a Attributes
     */
    static Attributes convertGroupToLDAPEntry(Group p_group) 
    {
        Attributes attrSet = new BasicAttributes();
        attrSet.put(new BasicAttribute( LDAP_ATTR_OBJECT_CLASS, 
                                        LDAP_GROUP_END_OBJECT_CLASS) );
        if (isStringValid(p_group.getGroupName())) 
        {
            attrSet.put(generateLDAPAttribute( LDAP_ATTR_GROUP_NAME, 
                                               p_group.getGroupName() ) );
        }
        if (isStringValid(p_group.getGroupDescription()))   
        {
            attrSet.put(generateLDAPAttribute( LDAP_ATTR_GROUP_DESCRIPTION, 
                                               p_group.getGroupDescription()));
        }

        // convert the user ids to the full user distinguished name
        String[] userDns = UserLdapHelper.getUsersDN(p_group.getUserIds());

        // either set to the list of user DNs or an empty list if no users are
        // associated with the group.
        attrSet.put(generateUniqueMemberAttr(LDAP_ATTR_GROUP_MEMBER, userDns));
        
        String[] strSet = p_group.getPermissionNames();
        for (int i = 0; i < strSet.length; i++) 
        {
            attrSet.put(new BasicAttribute(LDAP_ATTR_GROUP_PERMISSION, strSet[i]));
        }

        return attrSet;
    }

    //
    // Private methods
    //

    /**
     * Convert a Attributes to a Group object
     */
    private static Group getGroupFromLDAPEntry(Attributes p_entry) 
    throws NamingException 
    {       
        Group group = new GroupImpl();
        Attribute attr = p_entry.get(LDAP_ATTR_GROUP_NAME);
        group.setGroupName(getSingleAttributeValue(attr));

        attr = p_entry.get(LDAP_ATTR_GROUP_DESCRIPTION);
        group.setGroupDescription(getSingleAttributeValue(attr));
        attr = p_entry.get(LDAP_ATTR_GROUP_MEMBER);
        if (attr != null) 
        {
            String[] userDns = new String[attr.size()];
            NamingEnumeration namingEnu = attr.getAll();
            for(int i=0; namingEnu.hasMoreElements();i++) 
            {
                userDns[i] = namingEnu.next().toString();
            }
            
            namingEnu.close();
            
            String[] userIds = new String[userDns.length];
            for (int i=0 ; i< userDns.length ; i++)
            {
                String userId = UserLdapHelper.parseUserIdFromDn(userDns[i]);
                userIds[i] = userId;
            }
            group.setUserIds(userIds);
        }

        attr = p_entry.get(LDAP_ATTR_GROUP_PERMISSION);
        if (attr != null) 
        {
            String[] tempStr = new String[attr.size()];
            NamingEnumeration namingEnuTemp = attr.getAll();
            for(int i=0; namingEnuTemp.hasMoreElements();i++) 
            {
                tempStr[i] = namingEnuTemp.next().toString();
            }
            namingEnuTemp.close();
            group.setPermissionNames(tempStr);
        }

        return group;
    }
}

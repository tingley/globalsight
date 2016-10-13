package com.plug.Version_8_3_0;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
import com.util.ldap.LdapUtil;

public class UserUtil
{
    private static Logger logger = Logger.getLogger(UserUtil.class);

    private static final String SUPER_ADMIN = "system4_admin_username";

    public static List<User> getAllUsers()
    {
        List<User> users = new ArrayList<User>();
        User ldapConnection = getUserById(getLdapConnectionUser());
        if (ldapConnection == null)
        {
            return users;
        }
        DirContext dirContext = LdapUtil.checkOutConnection();
        String filter = UserLdapUtil.getSearchFilter();
        try
        {
            users = getUsers(filter, null, dirContext);
        }
        catch (NamingException e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            LdapUtil.checkInConnection(dirContext);
        }

        return users;
    }

    public static User getUserById(String uid)
    {
        DirContext dirContext = LdapUtil.checkOutConnection();
        User user = null;
        try
        {
            user = getUserById(dirContext, uid);
        }
        catch (NamingException e)
        {
            // ignore
        }
        finally
        {
            LdapUtil.checkInConnection(dirContext);
        }
        return user;
    }

    public static String getLdapConnectionUser()
    {
        return LdapUtil.getUserName();
    }

    public static String getSuperAdminUser()
    {
        return InstallValues.getIfNull(SUPER_ADMIN, null);
    }

    public static void modifyUserName(String userId)
    {
        DirContext dirContext = LdapUtil.checkOutConnection();
        try
        {
            ModificationItem[] modSet = UserLdapUtil
                    .convertUserToModificationSet(userId);
            dirContext.modifyAttributes(UserLdapUtil.getUserDN(userId), modSet);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            LdapUtil.checkInConnection(dirContext);
        }
    }

    private static User getUserById(DirContext dirContext, String uid)
            throws NamingException
    {
        User user = null;
        if (uid != null)
        {
            String userDN = UserLdapUtil.getUserDN(uid);
            Attributes userEntry = dirContext.getAttributes(userDN);
            if (userEntry != null)
            {
                user = UserLdapUtil.getUserFromLDAPEntry(userEntry);
            }
        }
        return user;
    }

    private static List<User> getUsers(String p_filter, String[] p_targetAttrs,
            DirContext dirContext) throws NamingException
    {
        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(p_targetAttrs);
        NamingEnumeration res = dirContext.search(UserLdapUtil.USER_BASE_DN,
                p_filter, constraints);

        return UserLdapUtil.getUsersFromSearchResults(res);
    }
}

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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributesException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SchemaViolationException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.ContainerRoleImpl;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserFieldSecurity;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.vendormanagement.UpdatedDataEvent;
import com.globalsight.everest.workflow.Activity;

/**
 * UserManagerLocal implements UserManager and is responsible for managing user
 * information in the system.
 */
public class UserManagerLocal implements UserManager
{
    public static String PROJECT_MANAGER = "Project Manager";

    private static final Logger CATEGORY = Logger
            .getLogger(UserManagerLocal.class.getName());

    private DirContextPool dirContextPool; // LDAP connection pool

    private UserManagerEventHandler m_eventHandler = null;

    // for calculate users that logged in.
    private Map m_userMap = null;

    //
    // Constructor
    //

    public UserManagerLocal() throws UserManagerException
    {
        m_userMap = new Hashtable(20);

        String errorMsgKey = UserManagerException.MSG_INIT_SERVER_ERROR;
        try
        {
            dirContextPool = LdapHelper.getConnectionPool();
        }
        catch (NamingException le)
        {
            String[] errorMsgArg = { "Couldn't connect to LDAP" };

            CATEGORY.error(
                    "UserManagerException is thrown from: "
                            + "UserManagerLocal::UserManagerLocal(): "
                            + errorMsgArg[0], le);

            throw new UserManagerException(errorMsgKey, errorMsgArg, le);
        }

        m_eventHandler = new UserManagerEventHandler();
    }

    //
    // UserManager Implementation
    //

    /**
     * A factory method returning a new User object
     * 
     * @return a User object
     */
    public User createUser()
    {
        return new UserImpl();
    }

    /**
     * A factory method that creates a UserRole.
     */
    public UserRole createUserRole()
    {
        return new UserRoleImpl();
    }

    /**
     * A factory method that creates a ContainerRole.
     */
    public ContainerRole createContainerRole()
    {
        ContainerRole cr = new ContainerRoleImpl();
        // a container role should start out as ACTIVE
        cr.setState(User.State.ACTIVE);

        return cr;
    }

    public void addRole(Role p_role) throws RemoteException,
            UserManagerException
    {
        String[] errorMsgArg;
        String errorMsgKey = UserManagerException.MSG_ADD_ROLE_ERROR;

        // Validate the parameter
        if (p_role == null || (!(p_role.isRoleValid())))
        {
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ROLE };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRole(): " + "(Role name= "
                    + (p_role == null ? "null" : p_role.toString()) + ")"
                    + errorMsgArg[0], null);
            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);
        // generate a LDAP entry for the Role
        Attributes entry = RoleLdapHelper.convertRoleToLdapEntry(p_role);
        String roleId = p_role.getName();
        String tmpDN = RoleLdapHelper.getRoleDN(roleId);
        try
        {
            // add to LDAP
            dirContext.createSubcontext(tmpDN, entry);
        }
        catch (NameAlreadyBoundException ex)
        {
            try
            {
                reactivateRole(dirContext, p_role);
                errorMsgArg = new String[] { UserManagerException.ARG_ROLE_ALREADY_EXIST };
            }
            catch (NamingException lde)
            {
                errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_REACTIVATE_ROLE };
            }
        }
        catch (InvalidAttributesException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ATTRIBUTE };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRole(): " + "(Role name= "
                    + p_role.toString() + ")" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRole(): " + "(Role name= "
                    + p_role.toString() + ")" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }
    }

    public void removeRole(String p_roleName) throws RemoteException,
            UserManagerException
    {
        if (p_roleName == null || "".equals(p_roleName.trim()))
        {
            return;
        }

        // modify the role status
        String roleDN = RoleLdapHelper.getRoleDN(p_roleName);
        removeRoleByDn(roleDN);
    }

    /**
     * @see UserManager.addUser(User, User, List, FieldSecurity, List, List)
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException
    {
        addUserBasicInfo(p_userRequestingAdd, p_user, p_projectIds,
                p_fieldSecurity);
        addUserRoles(p_user, p_roles);
    }

    /**
     * @see UserManager.removeUser(User, String)
     */
    public void removeUser(User p_userRequestingRemove, String p_uid)
            throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_DELETE_USER_ERROR;
        String[] errorMsgArg;

        // Validate parameter--UID
        if (p_uid == null || "".equals(p_uid.trim()))
        {
            return;
        }

        // Check if it is the LDAP Connection user.
        if (p_uid.equals(LdapHelper.LDAP_LOGIN))
        {
            // throw exception
            errorMsgArg = new String[] { UserManagerException.ARG_PROTECTED_USER };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        User u = getUser(p_uid);
        try
        {
            ServerProxy.getSecurityManager().removeFieldSecurity(
                    p_userRequestingRemove, u);
        }
        catch (Exception e)
        {
            // just log an error and continue - the user will still be able to
            // be removed
            CATEGORY.error("Failed to remove the field security for user "
                    + p_uid, e);
        }
        // remove the user from all the projects
        associateUserWithProjectsById(p_uid, new ArrayList());

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        // modify the user status
        ModificationItem[] modSet = new ModificationItem[1];
        modSet[0] = UserLdapHelper.getLDAPModificationForDeleteUser();

        String userDN = UserLdapHelper.getUserDN(p_uid);

        // delete the user from the roles that contains the user
        try
        {
            removeUserFromRoles(p_uid, dirContext);
            // remove user from ldap
            dirContext.destroySubcontext(userDN);
        }
        catch (NamingException e)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_DELETE_USER_FROM_ROLES };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], e);
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        removeUserCalendar(p_uid);
        try
        {
            // now that the user has been removed from the roles -
            // can remove user as a task's acceptor
            ServerProxy.getTaskManager().removeUserAsTaskAcceptor(p_uid);
        }
        catch (Exception e)
        { /* exception is logged in TaskManager */
        }

        try
        {
            ArrayList usersToUnmap = new ArrayList();
            usersToUnmap.add(p_uid);
            Permission.getPermissionManager().unMapUsersFromPermissionGroup(
                    usersToUnmap, null);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to unmap user " + p_uid
                    + " from all perm groups.", e);
        }
        m_eventHandler.dataUpdated(u, new UpdatedDataEvent(
                UpdatedDataEvent.DELETE_EVENT, p_userRequestingRemove));
    }

    /**
     * @see UserManager.activateUser(String, List)
     */
    public User activateUser(String p_uid, List p_projectIds)
            throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_REACTIVATE_USER_ERROR;

        // Validate parameter--UID
        if (p_uid == null || "".equals(p_uid.trim()))
        {
            return null;
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);
        try
        {
            // activate the user's roles
            activateUserRoles(p_uid, dirContext);
        }
        catch (NamingException ex)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_DEACTIVATE_USER_ROLES };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deactivateUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        if (p_projectIds != null && p_projectIds.size() > 0)
        {
            // add the user to the specified projects.
            associateUserWithProjectsById(p_uid, p_projectIds);
        }

        // check out a connection from the pool with a system defined user bond.
        dirContext = checkOutConnection(true);

        // modify the user status
        ModificationItem[] modSet = new ModificationItem[1];
        modSet[0] = UserLdapHelper.getLDAPModificationForActivateUser();
        String userDN = UserLdapHelper.getUserDN(p_uid);

        try
        {
            dirContext.modifyAttributes(userDN, modSet);
        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::reactivateUser(): " + "(User Id = "
                    + p_uid + "):\n", ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return getUser(p_uid);
    }

    /**
     * @see UserManager.deactivateUser(String)
     */
    public User deactivateUser(String p_uid) throws RemoteException,
            UserManagerException
    {
        // deactivate a user
        String errorMsgKey = UserManagerException.MSG_DEACTIVATE_USER_ERROR;
        String[] errorMsgArg;

        // Validate parameter--UID
        if (p_uid == null || "".equals(p_uid.trim()))
        {
            return null;
        }

        // Check if it is the LDAP Connection user.
        // can't deactivate them.
        if (p_uid.equals(LdapHelper.LDAP_LOGIN))
        {
            // throw exception
            errorMsgArg = new String[] { UserManagerException.ARG_PROTECTED_USER };

            CATEGORY.error("Can't deactiavte the ldap connection user " + p_uid
                    + ")\n" + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // remove the user from all the projects
        associateUserWithProjectsById(p_uid, new ArrayList());

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        try
        {
            // deactivate the user's roles
            deactivateUserRoles(p_uid, dirContext);
        }
        catch (NamingException e)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_DEACTIVATE_USER_ROLES };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deactivateUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], e);
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        try
        {
            // now that the user has been removed/deactivated from the
            // roles - can remove user as a task's acceptor
            ServerProxy.getTaskManager().removeUserAsTaskAcceptor(p_uid);
        }
        catch (Exception e)
        { /* exception is logged in TaskManager */
        }

        // modify the user status
        ModificationItem[] modSet = new ModificationItem[1];
        modSet[0] = UserLdapHelper.getLDAPModificationForDeactiveUser();

        String userDN = UserLdapHelper.getUserDN(p_uid);

        dirContext = checkOutConnection(true);
        // mark the user as deactive
        try
        {
            dirContext.modifyAttributes(userDN, modSet);
        }
        catch (NoSuchAttributeException ex)
        {
            checkInConnection(dirContext);
            CATEGORY.error("deleteUser " + p_uid + " " + ex.toString(), ex);
            errorMsgArg = new String[] { UserManagerException.ARG_USER_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deleteUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            // return the connection to the pool
            checkInConnection(dirContext);
            CATEGORY.error("deleteUser " + p_uid + " " + ex.toString(), ex);
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deleteUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }

        return getUser(p_uid);
    }

    /**
     * @see UserManager.modifyUser(User, User, List, FieldSecurity, List, List)
     */
    public void modifyUser(User p_userRequestingMod, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_MODIFY_USER_ERROR;
        String[] errorMsgArg;

        // Validate the parameter
        if (p_user == null || (!p_user.isUserValid()))
        {
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_USER };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::modifyUser(): " + "(User Id = "
                    + (p_user == null ? null : p_user.getUserId()) + "):\n"
                    + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // if the user is not yet active - verify that all the
        // required fields are present - if they are, set the state to
        // ACTIVE (otherwise leave in the current state)
        if ((p_user.getState() == User.State.CREATED)
                && userContainsRequiredFields(p_user))
        {
            p_user.setState(User.State.ACTIVE);
        }

        try
        {
            // update the field security
            if (p_fieldSecurity != null)
            {
                ServerProxy.getSecurityManager().setFieldSecurity(
                        p_userRequestingMod, p_user, p_fieldSecurity);
            }
        }
        catch (Exception e)
        {
            CATEGORY
                    .error("Failed to modify the user " + p_user.getUserId(), e);
            errorMsgArg = new String[] { p_user.getUserId() };
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        // generate a ModificationItem[] to hold the attributes to be modified
        ModificationItem[] modSet = UserLdapHelper
                .convertUserToModificationSet(p_user);
        try
        {
            // Modify the entry
            dirContext.modifyAttributes(UserLdapHelper.getUserDN(p_user
                    .getUserId()), modSet);
        }
        catch (NoSuchAttributeException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_USER_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::modifyUser(): " + "(User Id = "
                    + p_user.getUserId() + "):\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (InvalidAttributesException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ATTRIBUTE };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::modifyUser(): " + "(User Id = "
                    + p_user.getUserId() + "):\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::modifyUser(): " + "(User Id = "
                    + p_user.getUserId() + "):\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        // assign the user to the appropriate projects
        associateUserWithProjectsById(p_user.getUserId(), p_projectIds);

        modifyUserRoles(p_user, p_roles);
        m_eventHandler.dataUpdated(p_user, new UpdatedDataEvent(
                UpdatedDataEvent.UPDATE_EVENT, p_userRequestingMod));
    }

    /**
     * Get user matched the given uid
     * 
     * @param p_uid -
     *            The user id
     * 
     * @return a User object
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @throws UserManagerException
     *             if no user exists
     * @throws RemoteException
     *             for RMI problems
     */
    public User getUser(String p_uid) throws RemoteException,
            UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USER_ERROR;
        if (p_uid == null || p_uid.trim().equals(""))
            return null;
        
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);

        setConnectionOptions(dirContext);
        User outUser = null;
        Attributes userEntry = null;
        try
        {
            if(p_uid!=null)
            {
            	String userDN = UserLdapHelper.getUserDN(p_uid);
            	userEntry = dirContext.getAttributes(userDN);
            }
            // if a user was found
            if (userEntry != null)
            {
                outUser = UserLdapHelper.getUserFromLDAPEntry(userEntry);
            }
            else
            {
                CATEGORY.error("User " + p_uid + " does not exist in LDAP!");
                String[] args = { UserManagerException.ARG_USER_NOT_EXIST };
                throw new UserManagerException(errorMsgKey, args, null);
            }
        }
        catch (NameNotFoundException ex)
        {
            CATEGORY.error("User " + p_uid + " does not exist in LDAP!", ex);
            String[] args = { UserManagerException.ARG_USER_NOT_EXIST };
            throw new UserManagerException(errorMsgKey, args, ex);
        }
        catch (NamingException ex)
        {
            CATEGORY.error("LDAPException finding user " + p_uid, ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return outUser;
    }

    /**
     * @see UserManager.getUserInfo(String)
     */
    public UserInfo getUserInfo(String p_userId) throws RemoteException,
            UserManagerException
    {
        // tbd - create a new LDAP query to return only the
        // necessary information. This will make this faster.
        // for now and for the API use - just get the user and
        // create the UserInfo from it.
        User u = getUser(p_userId);
        return new UserInfo(u);
    }

    /**
     * @see UserManager.getUserInfos(String)
     */
    public List getUserInfos(String[] p_userIds) throws RemoteException,
            UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;
        Vector userInfos = null;
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        // creates the filter for search for users that are not deleted or
        // active and have one of the user ids specified
        String filter = UserLdapHelper.getSearchFilterOnUIDs(p_userIds);
        try
        {
            userInfos = getUserInfos(filter, null, dirContext);
        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUserInfos(String[]): ", ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return new ArrayList(userInfos);
    }

    /**
     * @see UserManager.getUserInfos(String, String, String)
     */
    public List getUserInfos(String p_activityName, String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {

        Attribute activityAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ACTIVITY, p_activityName);
        Attribute sourceAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_SOURCE_LOCALE, p_sourceLocale);
        Attribute targetAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_TARGET_LOCALE, p_targetLocale);
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);

        Attribute[] roleAttrs = { typeAttr, activityAttr, sourceAttr,
                targetAttr };

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        String[] userIds = null;
        try
        {
            userIds = getUserIdsFromRole(roleAttrs, dirContext);
        }
        catch (Exception e)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
            CATEGORY.error("failed to get usernames from role: "
                    + p_activityName + ", " + p_sourceLocale + ", "
                    + p_targetLocale, e);

            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, e);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        if (userIds == null || userIds.length == 0)
        {
            return null;
        }

        return getUserInfos(userIds);

    }

    /**
     * @see UserManager.getCompanyNames()
     */
    public String[] getCompanyNames() throws RemoteException,
            UserManagerException
    {
        try
        {
            return CompanyWrapper.getAllCompanyNames();

        }
        catch (Exception e)
        {
            String[] errorMsgArg = { UserManagerException.ARG_FAILED_TO_GET_COMPANY_ENTRIES };

            CATEGORY.error("getCompanyNames :: " + errorMsgArg[0], e);

            throw new UserManagerException(
                    UserManagerException.MSG_GET_COMPANY_NAMES_ERROR,
                    errorMsgArg, e);
        }

    }

    /**
     * @see UserManager.getEmailInformationForUser(String)
     */
    public EmailInformation getEmailInformationForUser(String p_userId)
            throws RemoteException, UserManagerException
    {
        EmailInformation emailInfo = null;
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_FIRST_NAME,
                UserLdapHelper.LDAP_ATTR_LAST_NAME,
                UserLdapHelper.LDAP_ATTR_EMAIL,
                UserLdapHelper.LDAP_ATTR_CC_EMAIL,
                UserLdapHelper.LDAP_ATTR_BCC_EMAIL,
                UserLdapHelper.LDAP_ATTR_DEFAULT_UI_LOCALE };

        // Search User entries based on the uids
        try
        {
            String[] uids = { p_userId };
            String filter = UserLdapHelper.getSearchFilterOnUIDs(uids);

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    UserLdapHelper.USER_BASE_DN, filter, constraints);

            if (res.hasMoreElements())
            {
                SearchResult tmpSearchResult = (SearchResult) res.nextElement();
                Attributes entry = tmpSearchResult.getAttributes();

                emailInfo = UserLdapHelper.getUserEmailInfo(p_userId, entry);

            }
            res.close();
        }
        catch (NamingException ex)
        {
            String[] errorMsgArg = { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
            CATEGORY.error("getEmailInformationForUser :: " + errorMsgArg[0],
                    ex);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_EMAILS_FORUSERS_ERROR,
                    errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return emailInfo;
    }

    /**
     * @see UserManager.getEmailInformationForUsers(String[])
     */
    public List getEmailInformationForUsers(String[] p_userIds)
            throws RemoteException, UserManagerException
    {

        String errorMsgKey = UserManagerException.MSG_GET_EMAILS_FORUSERS_ERROR;
        String[] errorMsgArg;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_FIRST_NAME,
                UserLdapHelper.LDAP_ATTR_LAST_NAME,
                UserLdapHelper.LDAP_ATTR_EMAIL,
                UserLdapHelper.LDAP_ATTR_CC_EMAIL,
                UserLdapHelper.LDAP_ATTR_BCC_EMAIL,
                UserLdapHelper.LDAP_ATTR_DEFAULT_UI_LOCALE,
                UserLdapHelper.LDAP_ATTR_USERID };

        List emails = null;

        // Search User entries based on the uids
        try
        {
            emails = getListOfEmailInfo(p_userIds, attrs, dirContext);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { p_userIds.toString() };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getEmailInformationForUsers(): "
                    + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return emails;
    }

    /**
     * Get the user names (i.e. user ids) as an array of strings. Returns the
     * user names of the ACTIVE users.
     * 
     * @return An array of user names.
     * 
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getUserNames() throws RemoteException, UserManagerException
    {
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        String[] userNames = null;
        setConnectionOptions(dirContext);
        // Search User names
        try
        {
            String filter = UserLdapHelper.getSearchFilter();
            String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_USERID };

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    UserLdapHelper.USER_BASE_DN, filter, constraints);

            userNames = UserLdapHelper.getUIDsFromSearchResults(res);
        }
        catch (NamingException ex)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };

            CATEGORY
                    .error("UserManagerException is thrown from: "
                            + "UserManagerLocal::getUserNames(): "
                            + errorMsgArg[0], ex);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return userNames;
    }

    /**
     * @see UserManager.getVendorlessUsers()
     */
    public Vector getVendorlessUsers() throws RemoteException,
            UserManagerException
    {

        DirContext dirContext = checkOutConnection(false);
        Vector users = null;

        try
        {
            // get all active and created user ids
            String filter = UserLdapHelper.getSearchFilter();
            String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_USERID };

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    UserLdapHelper.USER_BASE_DN, filter, constraints);

            Vector userNames = UserLdapHelper
                    .getUIDsVectorFromSearchResults(res);
            // now remove the user ids that are already vendors
            Collection usersWithVendors = ServerProxy.getVendorManagement()
                    .getUserIdsOfVendors();
            for (Iterator ui = usersWithVendors.iterator(); ui.hasNext();)
            {
                String userId = (String) ui.next();
                if (userNames.contains(userId))
                {
                    userNames.remove(userId);
                }
            }

            // get all the users specified by the user ids in the list
            String[] userNamesArray = new String[userNames.size()];
            userNamesArray = (String[]) userNames.toArray(userNamesArray);
            users = getUsers(userNamesArray, null, dirContext);
        }
        catch (Exception ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getVendorlessUsers().", ex);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_VENDORLESS_USERS_ERROR, null,
                    ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return users;
    }

    /**
     * Gets all the user names. This is necessary when checking for duplicate
     * name. A user name can NOT be re-used even if they have been deactivated.
     * 
     * @return An array of all user names.
     * 
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getAllUserNames() throws RemoteException,
            UserManagerException
    {
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        String[] userNames = null;
        setConnectionOptions(dirContext);

        // Search User names
        try
        {
            String filter = UserLdapHelper.getSearchFilterForAllUsers();
            String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_USERID };

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    UserLdapHelper.USER_BASE_DN, filter, constraints);

            userNames = UserLdapHelper.getUIDsFromSearchResults(res);
        }
        catch (NamingException ex)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
            CATEGORY
                    .error("UserManagerException is thrown from: "
                            + "UserManagerLocal::getUserNames(): "
                            + errorMsgArg[0], ex);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return userNames;
    }

    /**
     * @see UserManager.getUserNamesByFilter(String, Project)
     */
    public String[] getUserNamesByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        String[] userNames = null;

        if (p_roleName != null)
        {
            // check out a connection from the connection pool
            DirContext dirContext = checkOutConnection(false);
            setConnectionOptions(dirContext);
            try
            {
                userNames = getUserIdsFromRole(p_roleName, dirContext);
            }
            catch (Exception e)
            {
                String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
                CATEGORY.error("UserManagerException is thrown from: "
                        + "UserManagerLocal::getUserNamesByRole(): "
                        + errorMsgArg[0], e);
                throw new UserManagerException(
                        UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg,
                        e);
            }
            finally
            {
                // return the connection to the pool
                checkInConnection(dirContext);
            }
        }
        else
        {
            // role is null, so just get all users and then let the filter sort
            // it out
            Vector allUsers = getUsers();
            userNames = new String[allUsers.size()];
            Iterator userIter = allUsers.iterator();
            int i = 0;
            while (userIter.hasNext())
            {
                User u = (User) userIter.next();
                userNames[i++] = u.getUserId();
            }

        }

        // if project was specified then filter by it
        if (p_project != null)
        {
            return filterUsersByProject(userNames, p_project);
        }
        else
        // otherwise just return the ones found
        {
            return userNames;
        }
    }

    /**
     * @see UserManager.getUserNamesFromRoles(String[], Project)
     */
    public String[] getUserNamesFromRoles(String[] p_roleNames,
            Project p_project) throws RemoteException, UserManagerException
    {
        // sanity check
        if (p_roleNames == null || p_roleNames.length == 0)
        {
            return null;
        }

        String[] userIds = null;
        // get the users associated with the given project.
        Set projectUserIds = p_project == null ? null : p_project.getUserIds();

        // String errorMsgKey = UserManagerException.MSG_GET_ROLES_ERROR;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        try
        {
            userIds = getUserIdsFromRoles(p_roleNames, projectUserIds,
                    dirContext);
        }
        catch (Exception ex)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };

            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ex);
        }
        finally
        {
            checkInConnection(dirContext);
        }

        return userIds;
    }

    /**
     * @see UserManager.getUsersByFilter(String, Project)
     */
    public List getUsersByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        String[] userNames = getUserNamesByFilter(p_roleName, p_project);

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        List users = null;
        try
        {
            users = getUsers(userNames, null, dirContext);
        }
        catch (NamingException ldp)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
            CATEGORY.error(
                    "Failed to get all the users that match the filter - Role: "
                            + p_roleName + ", Project: " + p_project.getName()
                            + p_roleName + ", Project: " + p_project.getName(),
                    ldp);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ldp);
        }
        finally
        {
            checkInConnection(dirContext);
        }
        return users;
    }
    
    /**
     *  get users from ldap follow the filter
     */
    public List getUsersByFilter(String p_filter)
            throws RemoteException, UserManagerException
    {
        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        List users = null;
        try
        {
            users = getUsers(p_filter, null, dirContext);
        }
        catch (NamingException ldp)
        {
            String[] errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
            CATEGORY.error("Failed to get all the users that match the filter - filter: "+ p_filter,ldp);
            throw new UserManagerException(
                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ldp);
        }
        finally
        {
            checkInConnection(dirContext);
        }
        return users;
    }

    /**
     * @see UserManager.getUsersFromEmail(String)
     */
    @SuppressWarnings("unchecked")
	public List getUsersByEmail(String p_email) 
    	throws UserManagerException, RemoteException{
    	
    	List result = new ArrayList();
    	String filter = UserLdapHelper.getSearchFilterForEmail(p_email);
    	result = getUsersByFilter(filter);
		
		return result;
    }
    
    /**
     * Get users matched the specified criteria
     * 
     * @param p_userAttrs -
     *            Arrtibute array contains the User entry attributes
     * @param p_roleAttrs -
     *            Attribute array contains the Role entry attributes
     * @param p_project
     * @return a Vector of User objects
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException -
     *                Network related exception.
     */
    public Vector getUsers(Attribute[] p_userAttrs, Attribute[] p_roleAttrs,
            Project p_project) throws RemoteException, UserManagerException
    {
        return getUsers(p_userAttrs, p_roleAttrs, null, p_project);
    }

    /**
     * Get all active and created users.
     * 
     * @return a Vector of User objects
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException -
     *                Network related exception.
     */
    public Vector getUsers() throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        Vector users = null;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        String filter = UserLdapHelper.getSearchFilter();
        try
        {
            users = getUsers(filter, null, dirContext);
        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsers(): ", ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return users;
    }

    /**
     * Get all active and created users for current company only.
     * 
     * @return a Vector of User objects
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Vector getUsersForCurrentCompany() throws RemoteException,
            UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        Vector users = null;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        String filter = UserLdapHelper.getSearchFilterForCurrentCompany();

        try
        {
            users = getUsers(filter.toString(), null, dirContext);
        }
        catch (Exception e)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsersForCudrrentCompany(): ", e);
            throw new UserManagerException(errorMsgKey, null, e);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return users;
    }

    /**
     * @see UserManager.getUserInfos()
     */
    public Vector getUserInfos() throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        Vector userInfos = null;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);
        String filter = UserLdapHelper.getSearchFilter();

        try
        {
            userInfos = getUserInfos(filter, null, dirContext);
        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsers(): ", ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return userInfos;
    }

    /**
     * Get all the UserRoles for a particular User.
     * 
     * @param p_user
     *            The User for whom we want the UserRoles.
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     * @return A Collection containing the UserRoles for the given User.
     */
    public Collection getUserRoles(User p_user) throws RemoteException,
            UserManagerException
    {
        String userId = p_user.getUserId();
        String filter = RoleLdapHelper
                .getSearchFilterForUserRolesOnUserId(userId);
        String[] attrs = RoleLdapHelper.getSearchAttributeNames();

        return (Collection) getRoles(filter, attrs, true);
    }

    /**
     * Get all the ContainerRoles for a particular User.
     * 
     * @param p_user
     *            The User for whom we want the ContainerRoles.
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     * @return A Collection containing the ContainerRoles for the given User.
     */
    public Collection getContainerRoles(User p_user) throws RemoteException,
            UserManagerException
    {
        String userId = p_user.getUserId();
        String filter = RoleLdapHelper
                .getSearchFilterForContainerRolesOnUserId(userId);
        String[] attrs = RoleLdapHelper.getSearchAttributeNames();

        return (Collection) getRoles(filter, attrs, true);
    }

    /**
     * Returns the Collection of all UserRole objects with the given activity
     * name, source locale, and target locale.
     * 
     * @param p_activityName
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public Collection getUserRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {

        Attribute activityAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ACTIVITY, p_activityName);
        Attribute sourceAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_SOURCE_LOCALE, p_sourceLocale);
        Attribute targetAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_TARGET_LOCALE, p_targetLocale);
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE, "U");

        Attribute[] ldapAttrs = { typeAttr, activityAttr, sourceAttr,
                targetAttr };
        String filter = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
        }
        catch (NamingException ex)
        {
            // ignorance.
        }
        String[] attrs = RoleLdapHelper.getSearchAttributeNames();

        return (Collection) getRoles(filter, attrs, true);
    }

    /**
     * Returns the Collection of all ContainerRole objects with the given
     * activity name, source locale, and target locale.
     * 
     * @param p_activityName
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     * 
     * @deprecated It is supposed to have null or only one ContainerRole object
     *             returned. suggest to use method getContainerRole(String
     *             p_activityName, String p_sourceLocale, String p_targetLocale)
     */
    public Collection getContainerRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {

        Attribute activityAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ACTIVITY, p_activityName);
        Attribute sourceAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_SOURCE_LOCALE, p_sourceLocale);
        Attribute targetAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_TARGET_LOCALE, p_targetLocale);
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);

        Attribute[] ldapAttrs = { typeAttr, activityAttr, sourceAttr,
                targetAttr };
        String filter = null;
        String[] attrs = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
            attrs = RoleLdapHelper.getSearchAttributeNames();
        }
        catch (NamingException ex)
        {
            // ignorance.
        }
        return (Collection) getRoles(filter, attrs, true);
    }

    /**
     * @see UserManager.getContainerRoles(String, String)
     * 
     */
    public Collection getContainerRoles(String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {

        Attribute sourceAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_SOURCE_LOCALE, p_sourceLocale);
        Attribute targetAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_TARGET_LOCALE, p_targetLocale);
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);
        Attribute[] ldapAttrs = { typeAttr, sourceAttr, targetAttr };

        String filter = null;
        String[] attrs = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
            attrs = RoleLdapHelper.getSearchAttributeNames();
        }
        catch (NamingException ex)
        {
            // ignorance.
        }

        return (Collection) getRoles(filter, attrs, true);
    }

    /**
     * Returns the ContainerRole object with the given activity name, source
     * locale, and target locale.
     * 
     * @param p_activityName
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     * 
     * @return an ContainerRole object
     */
    public ContainerRole getContainerRole(Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        return getContainerRole(p_activity, p_sourceLocale, p_targetLocale, -1);
    }

    /**
     * @see UserManager.getContainerRole(String, String, String, long)
     */
    public ContainerRole getContainerRole(Activity p_activity,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
        StringBuffer buf = new StringBuffer();
        buf.append(p_activity.getId()).append(" ").append(p_activity.getName())
                .append(" ").append(p_sourceLocale).append(" ").append(
                        p_targetLocale);
        Attribute cnAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_NAME, buf.toString());
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);
        Attribute[] ldapAttrs = { typeAttr, cnAttr };
        String filter = null;
        String[] attrs = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
            attrs = RoleLdapHelper.getSearchAttributeNames();
        }
        catch (NamingException ex)
        {
            // ignorance.
        }

        Vector cRoles = getRoles(filter, attrs, true, p_projectId);

        if (cRoles == null || cRoles.size() == 0)
        {
            return null;
        }
        else
        {
            return (ContainerRole) cRoles.get(0);
        }
    }

    /**
     * @see UserManager.getContainerRole(String, String, String, long)
     */
    public ContainerRole getContainerRole(String p_activityName,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
        Attribute activityAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ACTIVITY, p_activityName);
        Attribute sourceAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_SOURCE_LOCALE, p_sourceLocale);
        Attribute targetAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_TARGET_LOCALE, p_targetLocale);
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);

        Attribute[] ldapAttrs = { typeAttr, activityAttr, sourceAttr,
                targetAttr };
        String filter = null;
        String[] attrs = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
            attrs = RoleLdapHelper.getSearchAttributeNames();
        }
        catch (NamingException ex)
        {
            // ignorance.
        }

        Vector cRoles = getRoles(filter, attrs, true, p_projectId);

        if (cRoles == null || cRoles.size() == 0)
        {
            return null;
        }
        else
        {
            return (ContainerRole) cRoles.get(0);
        }
    }

    /**
     * @see UserManager.getContainerRole(Rate)
     */
    public ContainerRole getContainerRole(Rate p_rate, boolean p_withRates)
            throws RemoteException, UserManagerException
    {

        Attribute rateIdAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_RATES, Long.toString(p_rate.getId()));
        Attribute typeAttr = new BasicAttribute(
                RoleLdapHelper.LDAP_ATTR_ROLE_TYPE,
                ContainerRole.ROLE_TYPE_VALUE);

        Attribute[] ldapAttrs = { typeAttr, rateIdAttr };
        String filter = null;
        String[] attrs = null;
        try
        {
            filter = RoleLdapHelper.getSearchFilter(ldapAttrs);
            attrs = RoleLdapHelper.getSearchAttributeNames();
        }
        catch (NamingException ex)
        {
            // ignorance.
        }

        Vector cRoles = getRoles(filter, attrs, p_withRates);
        if (cRoles == null || cRoles.size() == 0)
        {
            return null;
        }
        else
        {
            return (ContainerRole) cRoles.get(0);
        }
    }

    /**
     * Removes users from a role.
     * 
     * @param p_uids -
     *            The user id to be deleted.
     * @param p_roleName -
     *            The role to add the user to.
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void removeUsersFromRole(String[] p_uids, String p_roleName)
            throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_DELETE_USERS_FROM_ROLE_ERROR;
        String[] errorMsgArg;

        // Validate the parameters
        if (p_uids == null || p_uids.length == 0)
        {
            // throw exception
            return;
        }

        if (p_roleName == null || "".equals(p_roleName.trim()))
        {
            // throw exception
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ROLE_NAME };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUsersFromRole(): "
                    + "(role name = " + p_roleName + ", users = " + p_uids
                    + ")\n" + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        // generate a ModificationItem[] to hold the attributes to be modified
        ModificationItem[] modSet = RoleLdapHelper
                .deleteUsersModificationSet(p_uids);

        try
        {
            // Modify the entry
            dirContext.modifyAttributes(RoleLdapHelper.getRoleDN(p_roleName),
                    modSet);
        }
        catch (NameNotFoundException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_ROLE_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUsersFromRole(): "
                    + "(role name = " + p_roleName + ", users = " + p_uids
                    + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (InvalidAttributesException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_USERS_NOT_IN_ROLE };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUsersFromRole(): "
                    + "(role name = " + p_roleName + ", users = " + p_uids
                    + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUsersFromRole(): "
                    + "(role name = " + p_roleName + ", users = " + p_uids
                    + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

    }

    /**
     * Adds users to a role.
     * 
     * @param p_uids -
     *            The user ids to be added.
     * @param p_roleName -
     *            The role to add the user to.
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void addUsersToRole(String[] p_uids, String p_roleName)
            throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_ADD_USERS_TO_ROLE_ERROR;
        String[] errorMsgArg;

        // Validate the parameters
        if (p_uids == null || p_uids.length == 0)
        {
            return;
        }

        if (p_roleName == null || "".equals(p_roleName.trim()))
        {
            // throw exception
            errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ROLE_NAME };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addUsersToRole(): " + "(role name = "
                    + p_roleName + ", users = " + p_uids + ")\n"
                    + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        try
        {
            // validate the users
            if (!areUsersExist(p_uids, dirContext))
            {
                errorMsgArg = new String[] { UserManagerException.ARG_USERS_NOT_EXIST };

                CATEGORY.error("UserManagerException is thrown from: "
                        + "UserManagerLocal::addUsersToRole(): "
                        + "(role name = " + p_roleName + ", users = " + p_uids
                        + ")\n" + errorMsgArg[0], null);
                throw new UserManagerException(errorMsgKey, errorMsgArg, null);
            }
        }
        catch (NamingException e)
        { // failed to validate these users
            // return the connection to the pool
            checkInConnection(dirContext);
            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_VALIDATE_USERS };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addUsersToRole(): " + "(role name = "
                    + p_roleName + ", users = " + p_uids + ")\n"
                    + errorMsgArg[0], null);
            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // Generate a ModificationItem[] to hold the attributes to be
        // modified.
        ModificationItem[] modSet = RoleLdapHelper
                .addUsersModificationSet(p_uids);

        try
        {
            // Modify the entry
            dirContext.modifyAttributes(RoleLdapHelper.getRoleDN(p_roleName),
                    modSet);
        }
        catch (NameNotFoundException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_ROLE_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addUsersToRole(): " + "(role name = "
                    + p_roleName + ", users = " + p_uids + ")\n"
                    + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (AttributeInUseException ex)
        {
            // ignorance.
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addUsersToRole(): " + "(role name = "
                    + p_roleName + ", users = " + p_uids + ")\n"
                    + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }
    }

    /**
     * Get the Role with the role name.
     * 
     * @param p_roleName
     *            the role name.
     * @returns the Role with the role name.
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Role getRole(String p_roleName) throws RemoteException,
            UserManagerException
    {

        String filter = RoleLdapHelper
                .getSearchFilterForRoleOnRoleName(p_roleName);
        String[] attrs = RoleLdapHelper.getSearchAttributeNames();
        Collection roles = (Collection) getRoles(filter, attrs, true);

        if (roles != null && !roles.isEmpty())
        {
            return (Role) roles.iterator().next();
        }

        return null;
    }

    /**
     * @see UserManager.getUserInfosInAllProjects()
     */
    public List getUserInfosInAllProjects() throws RemoteException,
            UserManagerException
    {
        List userInfos = new ArrayList();
        String filter = UserLdapHelper
                .getSearchFilterForActiveUsersInAllProjects();
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        try
        {
            // retrieve all the user ids that are in all projects
            userInfos = getUserInfos(filter, null, dirContext);
        }
        catch (NamingException e)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUserInfosInAllProjects(): ", e);
            throw new UserManagerException(errorMsgKey, null, e);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return userInfos;
    }

    public boolean containsPermissionGroup(String p_uid, String p_permGroupName)
            throws RemoteException, UserManagerException
    {
        Collection permGroups = null;
        try
        {
            permGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupsForUser(p_uid);
        }
        catch (PermissionException e)
        {
            throw new UserManagerException(e);
        }

        PermissionGroup permGroup = null;
        for (Iterator iter = permGroups.iterator(); iter.hasNext();)
        {
            permGroup = (PermissionGroup) iter.next();
            if (permGroup.getName().equals(p_permGroupName))
            {
                return true;
            }
        }
        return false;
    }

    //
    // Package Methods
    //

    /**
     * Get users based on the search filter and target attributes
     * 
     * @return a Vector of User objects
     */
    Vector getUsers(String p_filter, String[] p_targetAttrs,
            DirContext dirContext) throws NamingException
    {

        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(p_targetAttrs);
        NamingEnumeration res = dirContext.search(UserLdapHelper.USER_BASE_DN,
                p_filter, constraints);

        return UserLdapHelper.getUsersFromSearchResults(res);
    }

    /**
     * Get UserInfos based on the search filter and target attributes
     * 
     * @return a Vector of UserInfo objects
     */
    Vector getUserInfos(String p_filter, String[] p_targetAttrs,
            DirContext dirContext) throws NamingException
    {

        Vector userInfos = null;

        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(p_targetAttrs);
        NamingEnumeration res = dirContext.search(UserLdapHelper.USER_BASE_DN,
                p_filter, constraints);

        userInfos = UserLdapHelper.getUserInfosFromSearchResults(res);

        return userInfos;
    }

    // --------------------- private methods -----------------------

    /**
     * Add the user's basic info which includes the field security and projects
     * too - but not roles.
     */
    private void addUserBasicInfo(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity)
            throws UserManagerException, RemoteException
    {
        String errorMsgKey = UserManagerException.MSG_ADD_USER_ERROR;
        String[] errorMsgArg;

        try
        {
            // Validate the parameter
            if (p_user == null || (!p_user.isUserValid()))
            {
                errorMsgArg = new String[] { UserManagerException.ARG_INVALID_USER };

                CATEGORY.error("UserManagerException is thrown from: "
                        + "UserManagerLocal::addUser(): " + errorMsgArg[0],
                        null);
                throw new UserManagerException(errorMsgKey, errorMsgArg, null);
            }

            // if the user has all the required information
            // make them active (otherwise leave as "CREATED")
            if (userContainsRequiredFields(p_user))
            {
                p_user.setState(User.State.ACTIVE);
            }

            // check out a connection from the pool with a system defined user
            // bond.
            DirContext dirContext = checkOutConnection(true);

            // generate a LDAP entry for the user
            Attributes entry = UserLdapHelper.convertUserToLDAPEntry(p_user);
            String tmpDN = UserLdapHelper.getUserDN(p_user.getUserId());
            try
            {
                // add to LDAP
                dirContext.createSubcontext(tmpDN, entry);
            }
            catch (SchemaViolationException ex)
            {
                errorMsgArg = new String[] { UserManagerException.ARG_INVALID_ATTRIBUTE };
                CATEGORY.error("UserManagerException is thrown from: "
                        + "UserManagerLocal::addUser(): " + "(User Id = "
                        + p_user.getUserId() + "):\n" + errorMsgArg[0], ex);
                throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
            }
            catch (NamingException ex)
            {
                errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
                CATEGORY.error("UserManagerException is thrown from: "
                        + "UserManagerLocal::addUser(): " + "(User Id = "
                        + p_user.getUserId() + "):\n" + errorMsgArg[0], ex);
                throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
            }
            finally
            {
                // return the connection to the pool
                checkInConnection(dirContext);
            }

            // add the user to the appropriate projects
            associateUserWithProjectsById(p_user.getUserId(), p_projectIds);
            // create a default field security if not set
            if (p_fieldSecurity == null)
            {
                p_fieldSecurity = new UserFieldSecurity();
            }
            ServerProxy.getSecurityManager().setFieldSecurity(
                    p_userRequestingAdd, p_user, p_fieldSecurity);
        }
        catch (Exception e)
        {
            // if either of these fail - remove the user which removes
            // it from permission groups, roles and projects
            removeUser(p_userRequestingAdd, p_user.getUserId());
            CATEGORY.error("Failed to add the user " + p_user.getUserId(), e);
            errorMsgArg = new String[] { p_user.getUserId() };
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }
    }

    /**
     * Add the roles specified to the user.
     */
    private void addUserRoles(User p_user, List p_roles)
            throws UserManagerException
    {
        int numOfFailedRoles = 0;
        String failedRoles[] = new String[p_roles.size()];
        try
        {
            if (p_roles != null && p_roles.size() > 0)
            {
                // Add the new UserRoles for this user, and update the
                // memberships
                // of ContainerRoles appropriately.
                for (int i = 0; i < p_roles.size(); i++)
                {
                    Role curRole = (Role) p_roles.get(i);
                    Role cContainerRole = getContainerRole(curRole
                            .getActivity(), curRole.getSourceLocale(), curRole
                            .getTargetLocale());

                    if (cContainerRole != null)
                    {
                        // only add the user to the container roles if
                        // the user is active
                        if (p_user.isActive())
                        {
                            String[] uids = { p_user.getUserId() };
                            addUsersToRole(uids, cContainerRole.getName());
                            curRole.setState(User.State.ACTIVE);
                        }
                        // add the user role - its state reflects CREATED or
                        // ACTIVE
                        // depending if the user role is ACTIVE
                        addRole(curRole);
                    }
                    else
                    {
                        // log an error - this container role should always
                        // exist
                        // otherwise some things are corrupted or removed
                        // manually
                        CATEGORY
                                .error("The container role doesn't exist for user role "
                                        + curRole.getName()
                                        + ".  However the activity and locale pairs do. "
                                        + " Something is out of sync between the database and ldap.");
                        failedRoles[numOfFailedRoles] = curRole.getName();
                        numOfFailedRoles++;
                    }
                }
            }

            // This method will force iFlow to refresh it's user/group cache
            // which is GlobalSight's roles - that's only populated during iFlow
            // startup.
            // ServerProxy.getWorkflowServer().refreshIflowCache();
        }
        catch (Exception ex)
        {
            throw createUserRoleException(p_user.getUserId(), ex,
                    numOfFailedRoles, failedRoles);
        }

        // if there was atleast one failed role - throw an exception
        // and show error to user
        if (numOfFailedRoles > 0)
        {
            // create an exception since some of the roles failed
            throw createUserRoleException(p_user.getUserId(), null,
                    numOfFailedRoles, failedRoles);
        }
    }

    /**
     * Modifies the user with the roles specified.
     * 
     * @param p_user
     * @param p_newRoleList -
     *            The list of roles the user should be associated with. If an
     *            empty list then remove all roles from the user. If NULL ignore
     *            the request and leave the user with their current roles.
     */
    private void modifyUserRoles(User p_user, List p_newRoleList)
            throws RemoteException, UserManagerException
    {
        int numOfFailedRoles = 0;

        if (p_newRoleList != null)
        {
            String failedRoles[] = new String[p_newRoleList.size()];
            try
            {
                // figure out the difference between the old role and new ones
                // and add/modify/remove appropriately
                Collection oldRoles = getUserRoles(p_user);
                List unchangedRoles = new ArrayList();
                String[] uids = { p_user.getUserId() };

                // find all the removed roles and REMOVE then
                // find the unmodified roles and add them to the list.
                if (oldRoles != null && oldRoles.size() > 0)
                {
                    for (Iterator i = oldRoles.iterator(); i.hasNext();)
                    {
                        Role oldRole1 = (Role) i.next();
                        boolean found = false;

                        if (p_newRoleList.contains(oldRole1))
                        {
                            unchangedRoles.add(oldRole1);
                            found = true;
                        }
                        // the old role not found in the new list - so remove
                        if (!found)
                        {
                            Role cRole1 = getContainerRole(oldRole1
                                    .getActivity(), oldRole1.getSourceLocale(),
                                    oldRole1.getTargetLocale());

                            if (cRole1 != null)
                            {
                                // remove user from Container role if the user
                                // role
                                // is active. otherwise if not the user won't
                                // exist in the container role.
                                if (oldRole1.isActive())
                                {
                                    removeUsersFromRole(uids, cRole1.getName());
                                }
                                // remove the user role
                                removeRole(oldRole1.getName());
                            }
                            else
                            {
                                // log an error - this container role should
                                // always exist
                                // otherwise some things are corrupted or
                                // removed manually
                                CATEGORY
                                        .error("The container role doesn't exist for user role "
                                                + oldRole1.getName()
                                                + ", so the user couldn't be removed from the role."
                                                + "  However the activity and locale pairs do. "
                                                + " Something is out of sync between the database and ldap.");
                                failedRoles[numOfFailedRoles] = oldRole1
                                        .getName();
                                numOfFailedRoles++;
                            }
                        }
                    } // for
                }

                // add any new ones
                for (int k = 0; k < p_newRoleList.size(); k++)
                {
                    Role newRole2 = (Role) p_newRoleList.get(k);
                    boolean found = false;
                    for (int m = 0; m < unchangedRoles.size() && !found; m++)
                    {
                        Role unchangedRole = (Role) unchangedRoles.get(m);
                        if (newRole2.equals(unchangedRole))
                        {
                            found = true;
                            // if the user is now active but the old role isn't
                            // update the status
                            if (p_user.isActive() && !unchangedRole.isActive())
                            {
                                // remove the user role
                                removeRole(unchangedRole.getName());
                                // set to false to add it back with correct
                                // state
                                found = false;
                            }
                        }
                    }
                    // then this must be new so add it
                    if (!found)
                    {
                        // add to container Role
                        Role cRole2 = getContainerRole(newRole2.getActivity(),
                                newRole2.getSourceLocale(), newRole2
                                        .getTargetLocale());
                        if (cRole2 != null)
                        {
                            if (p_user.isActive())
                            {
                                addUsersToRole(uids, cRole2.getName());
                                newRole2.setState(User.State.ACTIVE);
                            }
                            addRole(newRole2);
                            addUsersToRole(uids, newRole2.getName());
                        }
                        else
                        {
                            // log an error - this container role should always
                            // exist
                            // otherwise some things are corrupted or removed
                            // manually
                            CATEGORY
                                    .error("The container role doesn't exist for user role "
                                            + newRole2.getName()
                                            + ".  However the activity and locale pairs do. "
                                            + " Something is out of sync between the database and ldap.");
                            failedRoles[numOfFailedRoles] = newRole2.getName();
                            numOfFailedRoles++;
                        }
                    }
                } // for

                // This method will force iFlow to refresh it's user/group cache
                // that's only populated during iFlow startup.
                // ServerProxy.getWorkflowServer().refreshIflowCache();
                if (numOfFailedRoles > 0)
                {
                    throw createUserRoleException(p_user.getUserId(), null,
                            numOfFailedRoles, failedRoles);
                }
            }
            catch (UserManagerException ume)
            {
                throw ume;
            }
            catch (Exception ex)
            {
                throw createUserRoleException(p_user.getUserId(), ex,
                        numOfFailedRoles, failedRoles);
            }
        }
    }

    /**
     * Activates all the user roles associated with the specified user. And adds
     * the user to the appropriate container roles.
     */
    private void activateUserRoles(String p_uid, DirContext dirContext)
            throws NamingException, UserManagerException, RemoteException
    {
        // Validate the parameters
        if (p_uid == null || p_uid.length() == 0)
        {
            return;
        }

        Vector roleDNs = getInactiveRoleDNs(p_uid, dirContext);
        if (roleDNs != null)
        {
            for (int i = 0; i < roleDNs.size(); i++)
            {
                String roleDn = (String) roleDNs.elementAt(i);
                // if contains the userid in name then it is a user role
                int index = roleDn.indexOf(p_uid);
                if (index > 0)
                {
                    // activate the role
                    ModificationItem[] modSet = new ModificationItem[1];
                    modSet[0] = RoleLdapHelper
                            .getLDAPModificationForReactivateRole();
                    dirContext.modifyAttributes(roleDn, modSet);

                    // strip off just the name - from "cn=" to the "userid"
                    String containerRole = roleDn.substring(3, index - 1);
                    String uids[] = { p_uid };
                    addUsersToRole(uids, containerRole);
                }
            } // end for
        }

    }

    /**
     * Deactiavte the user roles the user is associated with and remove the user
     * from the container roles.
     * 
     * @param p_uids -
     *            The user id to be deactivated.
     * @param dirContext
     * @exception UserManagerException -
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */

    private void deactivateUserRoles(String p_uid, DirContext dirContext)
            throws NamingException, UserManagerException, RemoteException
    {
        // Validate the parameters
        if (p_uid == null || p_uid.length() == 0)
        {
            // throw exception
            return;
        }

        // get all the roles the user is part of
        // if a container role - remove the user from them
        // if a user role mark them as DEACTIVE
        Vector roleDNs = getRoleDNs(p_uid, dirContext);
        if (roleDNs != null)
        {
            for (int i = 0; i < roleDNs.size(); i++)
            {
                String roleDn = (String) roleDNs.elementAt(i);
                // this is a user role - it contains the user's id
                // within the name
                if (roleDn.indexOf(p_uid) > 0)
                {
                    // deactivate the role
                    ModificationItem[] modSet = new ModificationItem[1];
                    modSet[0] = RoleLdapHelper
                            .getLDAPModificationForDeactivateRole();
                    dirContext.modifyAttributes(roleDn, modSet);
                }
                else
                {
                    // removes the user from the particular container role
                    removeUserFromRole(p_uid, roleDn, dirContext);
                }
            }// end for
        }
    }

    /**
     * Calls the project manager to add/remove the user from its current
     * projects and associate them with the list specified here
     * 
     * @param p_userId
     *            The id of the user to associate with the projects.
     * @param p_projectIds
     *            The list of project ids to associate the user with. This can
     *            be NULL or an empty list to either remove the user from all
     *            projects, or if this hasn't been set yet.
     */
    private void associateUserWithProjectsById(String p_userId,
            List p_projectIds) throws UserManagerException
    {
        try
        {
            if (p_projectIds == null)
            {
                // no changes to be made with projects
                return;
            }

            ProjectHandler ph = ServerProxy.getProjectHandler();
            ph.associateUserWithProjectsById(p_userId, p_projectIds);
        }
        catch (Exception e)
        {
            CATEGORY
                    .error(
                            "UserManagerException is thrown from: "
                                    + "UserManagerLocal::associateUserWithProjects() for "
                                    + p_userId + " with projects "
                                    + p_projectIds.toString(), e);

            String[] args = { p_userId, p_projectIds.toString() };
            throw new UserManagerException(
                    UserManagerException.MSG_FAILED_TO_ASSOCIATE_USER_WITH_PROJECTS,
                    args, e);
        }
    }

    /**
     * Get email info of users based on the search filter and target attributes
     * 
     * @return a List of EmailInformation objects.
     */
    private List getListOfEmailInfo(String[] p_uids, String[] p_targetAttrs,
            DirContext dirContext) throws NamingException
    {

        // make sure we have a valid array of uids
        if ((p_uids == null) || (p_uids.length == 0))
        {
            return null;
        }

        String filter = UserLdapHelper.getSearchFilterOnUIDs(p_uids);
        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(p_targetAttrs);
        NamingEnumeration res = dirContext.search(UserLdapHelper.USER_BASE_DN,
                filter, constraints);

        return UserLdapHelper.getListOfEmailInfo(res);
    }

    private String getLDAPLoginDN()
    {
        return UserLdapHelper.getUserDN(LdapHelper.LDAP_LOGIN);
    }

    /**
     * This reactivates a role. However it doesn't keep around the old members
     * and rates that are part of it. It removes all the members and rates and
     * then looks at the new role passed in (that happens to have the same name)
     * and sets the members and rates to be the one from the new role.
     * <p>
     * Reactivating a role is used when adding a role that already exists in
     * LDAP but is marked as deleted. The information on the role is not removed
     * on deletion, but rather on reactivation. This is needed since some of the
     * deletion code is reused by some areas that need access to the information
     * after deletion.
     */
    private void reactivateRole(DirContext dirContext, Role p_role)
            throws NamingException, UserManagerException, RemoteException
    {
        // get the unique key (DN) of the role
        String roleDN = RoleLdapHelper.getRoleDN(p_role.getName());

        // --------- handle rates (delete old ones and add from new role)----
        ModificationItem[] deleteRates = new ModificationItem[1];
        deleteRates[0] = RoleLdapHelper.deleteAllRatesModification();
        try
        {
            dirContext.modifyAttributes(roleDN, deleteRates);
        }
        catch (NamingException lde)
        {
            // don't do anything, will throw an exception if there aren't any
            // rates to delete
        }
        String[] rateIds = null;
        if (p_role instanceof UserRole)
        {
            String rateId = ((UserRole) p_role).getRate();
            if (rateId != null && rateId.length() > 0)
            {
                rateIds = new String[1];
                rateIds[0] = rateId;
            }
        }
        else
        // container role
        {
            Collection rates = p_role.getRates();
            if (rates != null && rates.size() > 0)
            {
                rateIds = new String[rates.size()];
                int index = 0;
                for (Iterator i = rates.iterator(); i.hasNext();)
                {
                    Rate r = (Rate) i.next();
                    rateIds[index] = Long.toString(r.getId());
                    index++;
                }
            }
        }
        // now add the new ones in LDAP
        if (rateIds != null && rateIds.length > 0)
        {
            ModificationItem[] addRates = RoleLdapHelper
                    .addRatesModificationSet(rateIds);
            dirContext.modifyAttributes(roleDN, addRates);
        }

        // ------ handle members/user ids (delete old ones and add from new
        // role)--
        ModificationItem[] deleteMembers = new ModificationItem[1];
        deleteMembers[0] = RoleLdapHelper.deleteAllRoleMembers();
        try
        {
            dirContext.modifyAttributes(roleDN, deleteMembers);
        }
        catch (NamingException ldex)
        {
            // don't do anything, will throw an exception if there aren't
            // any members to delete
        }

        String[] userIds = null;
        if (p_role instanceof UserRole)
        {
            String userId = ((UserRole) p_role).getUser();
            if (userId != null && userId.length() > 0)
            {
                userIds = new String[1];
                userIds[0] = userId;
            }
        }
        else
        // container role
        {
            Vector users = ((ContainerRole) p_role).getUsers();
            if (users != null && users.size() > 0)
            {
                userIds = new String[users.size()];
                int index = 0;
                for (Iterator i = users.iterator(); i.hasNext();)
                {
                    User u = (User) i.next();
                    userIds[index] = u.getUserId();
                    index++;
                }
            }
        }
        // now add the new user ids in LDAP
        if (userIds != null && userIds.length > 0)
        {
            ModificationItem[] addUsers = RoleLdapHelper
                    .addUsersModificationSet(userIds);
            try
            {
                dirContext.modifyAttributes(roleDN, addUsers);
            }
            catch (AttributeInUseException aiuex)
            {
                // ignorance.
            }
        }

        // finally reactivate the role since all the other modifications
        // finished successfully
        ModificationItem[] reactivateMod = new ModificationItem[1];
        reactivateMod[0] = RoleLdapHelper
                .getLDAPModificationForReactivateRole();
        dirContext.modifyAttributes(roleDN, reactivateMod);
    }

    /**
     * Just deactivates the role - but leaves all other information in the role
     * the same.
     */
    private void removeRoleByDn(String p_roleDN) throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_DELETE_ROLE_ERROR;
        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        ModificationItem[] mod = new ModificationItem[1];
        mod[0] = RoleLdapHelper.getLDAPModificationForDeleteRole();

        String[] errorMsgArg;
        try
        {
            dirContext.modifyAttributes(p_roleDN, mod);
        }
        catch (NameNotFoundException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_ROLE_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deleteRole(): " + "(role name = "
                    + p_roleDN + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deleteRole(): " + "(role name = "
                    + p_roleDN + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }
    }

    /**
     * To judge if the user exists in LDAP directories.
     */
    private boolean isUserExist(String p_uid, DirContext dirContext)
            throws NamingException
    {

        String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_USERID };
        try
        {
            dirContext.getAttributes(UserLdapHelper.getUserDN(p_uid), attrs);
        }
        catch (NameNotFoundException ex)
        {
            return false;
        }

        return true;
    }

    /**
     * To judge if the users exist in LDAP directories
     */
    private boolean areUsersExist(String[] p_uids, DirContext p_connection)
            throws NamingException
    {
        boolean result = true;

        for (int i = 0; i < p_uids.length; i++)
        {
            result = isUserExist(p_uids[i], p_connection);

            if (!result)
            {
                break;
            }
        }

        return result;
    }

    /**
     * Remove a user from the roles that has him as a memeber. If this is a user
     * role then remove the entire role since the user is the only member. This
     * is NOT a deactivation but rather a true removal of a user role.
     * 
     * Called by removeUser(...)
     */
    private void removeUserFromRoles(String p_uid, DirContext dirContext)
            throws NamingException, UserManagerException, RemoteException
    {

        Vector roleDNs = getRoleDNs(p_uid, dirContext);

        if (roleDNs != null)
        {
            for (int i = 0; i < roleDNs.size(); i++)
            {
                String roleDn = (String) roleDNs.elementAt(i);
                // this is a user role - it contains the user's id
                // within the name
                // roleDn.split(" ").length > 4 to ensure removing this group
                // "1001 activity sourceLocale targetLocale userId"
                if (roleDn.indexOf(p_uid) > 0 && roleDn.split(" ").length > 4)
                {
                    // deletes the role and all of its contents
                    dirContext.destroySubcontext(roleDn);
                }
                else
                {
                    // removes the user from the particular container role
                    removeUserFromRole(p_uid, roleDn, dirContext);
                }
            }
        }
    }

    /**
     * Remove a user from a role.
     */
    private void removeUserFromRole(String p_uid, String p_roleDN,
            DirContext dirContext) throws NamingException
    {

        // generate a ModificationItem[] to hold the attributes to be modified
        ModificationItem[] modSet = RoleLdapHelper
                .deleteUsersModificationSet(new String[] { p_uid });
        // Modify the entry
        dirContext.modifyAttributes(p_roleDN, modSet);
    }

    public Vector getRoles(String p_filter, String[] p_attrs,
            boolean p_withRates) throws UserManagerException
    {
        return getRoles(p_filter, p_attrs, p_withRates, -1);
    }

    private Vector getRoles(String p_filter, String[] p_attrs,
            boolean p_withRates, long p_projectId) throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_ROLES_ERROR;
        Vector outputRoles = null;

        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        try
        {
            // Search LDAP
            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(p_attrs);
            NamingEnumeration res = dirContext.search(
                    RoleLdapHelper.ROLE_BASE_DN, p_filter, constraints);

            // generate role objects
            outputRoles = RoleLdapHelper.getRolesFromSearchResults(res,
                    p_withRates, p_projectId);

        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getRoles():" + "(Filter = " + p_filter
                    + ", attributes = " + p_attrs + ")\n", ex);

            throw new UserManagerException(errorMsgKey, null, ex);
        }
        finally
        {
            checkInConnection(dirContext);
        }

        return outputRoles;
    }

    /**
     * @see UserManager.addRateToRole(Rate, Activity, String, String)
     */
    public void addRateToRole(Rate p_rate, Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        // get the role
        ContainerRole cr = getContainerRole(p_activity, p_sourceLocale,
                p_targetLocale);
        if (cr != null)
        {
            addRateToRole(p_rate, cr);
        }
        else
        {
            String args[] = { p_activity.getName(), p_sourceLocale,
                    p_targetLocale };
            throw new UserManagerException(
                    UserManagerException.MSG_ROLE_DOES_NOT_EXIST, args, null);
        }
    }

    /**
     * @seeUserManager.removeRateFromRole(Rate, Activity, String, String)
     */
    public void removeRateFromRole(Rate p_rate, Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        ContainerRole cr = getContainerRole(p_activity, p_sourceLocale,
                p_targetLocale);

        if (cr != null)
        {
            removeRateFromRole(p_rate, cr);
        }
    }

    /**
     * @see UserManager.duplicateRateName(String p_name, Role p_role)
     */
    public boolean isDuplicateRateName(String p_name, Role p_role)
            throws RemoteException, UserManagerException
    {
        boolean isDup = false;

        // Verivy that the same named rate doesn't exist within this role.
        // The rate name must be unique within the role.
        Collection curRates = p_role.getRates();

        if (curRates != null)
        {
            for (Iterator cri = curRates.iterator(); cri.hasNext();)
            {
                Rate r = (Rate) cri.next();

                // if the names are the same throw an exception
                if (r.getName().equals(p_name))
                {
                    isDup = true;
                }
            }
        }

        return isDup;
    }

    // -----------------------private methods--------------------------

    /**
     * Get the UserIds of the User entry that match the given LDAP attributes.
     * Return empty array if search is performed but no match found.
     */
    private String[] getUserIds(Attribute[] p_userAttrs, DirContext dirContext)
            throws NamingException
    {

        String[] uids = null;

        if (p_userAttrs != null && p_userAttrs.length != 0)
        {
            String[] attrs = new String[] { UserLdapHelper.LDAP_ATTR_USERID };
            String filter = UserLdapHelper.getSearchFilter(p_userAttrs);

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    UserLdapHelper.USER_BASE_DN, filter, constraints);

            uids = UserLdapHelper.getUIDsFromSearchResults(res);
        }

        return uids;
    }

    /**
     * Get the UserIds of the Role entries that match the LDAP attributes.
     * Return empty array if search is performed but no match found.
     */
    private String[] getUserIdsFromRole(Attribute[] p_roleAttrs,
            DirContext dirContext) throws NamingException
    {

        String[] uids = null;

        if (p_roleAttrs != null && p_roleAttrs.length != 0)
        {
            String[] attrs = new String[] { RoleLdapHelper.LDAP_ATTR_MEMBERSHIP };
            String filter = RoleLdapHelper.getSearchFilter(p_roleAttrs);

            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    RoleLdapHelper.ROLE_BASE_DN, filter, constraints);

            uids = RoleLdapHelper.getUIDsFromSearchResults(res);
        }

        return uids;
    }

    /**
     * Get the UserIds of the Role entries that match the LDAP attributes.
     * Return empty array if search is performed but no match found.
     */
    private String[] getUserIdsFromRole(String p_roleName, DirContext dirContext)
            throws NamingException
    {

        String[] uids = null;
        String[] attrs = new String[] { RoleLdapHelper.LDAP_ATTR_MEMBERSHIP };

        if (p_roleName != null && p_roleName.trim().length() != 0)
        {
            try
            {
                Attributes roleEntry = dirContext.getAttributes(RoleLdapHelper
                        .getRoleDN(p_roleName), attrs);
                Attribute attr = roleEntry
                        .get(RoleLdapHelper.LDAP_ATTR_MEMBERSHIP);

                if (attr != null)
                {
                    String[] userDns = new String[attr.size()];
                    NamingEnumeration tmpValue = attr.getAll();
                    for (int i = 0; tmpValue.hasMoreElements(); i++)
                    {
                        userDns[i] = tmpValue.nextElement().toString();
                    }
                    tmpValue.close();

                    uids = UserLdapHelper.parseUserIdsFromDns(userDns);
                }
            }
            catch (NameNotFoundException ex)
            {
                uids = new String[0];
            }
            catch (NamingException ex)
            {
                // ignorance.
            }
        }

        return uids;
    }

    /**
     * Get the UserIds of the Role entries that match the LDAP attributes.
     * Return empty array if search is performed but no match found.
     */
    private String[] getUserIdsFromRoles(String[] p_roleNames,
            Set p_projectUserId, DirContext dirContext) throws NamingException
    {
        String filter = RoleLdapHelper
                .getSearchFilterForRolesOnRoleNames(p_roleNames);

        String[] attrs = new String[] { RoleLdapHelper.LDAP_ATTR_MEMBERSHIP };

        Set userIds = new TreeSet();

        try
        {
            // Search LDAP
            SearchControls constraints = new SearchControls();
            constraints.setReturningObjFlag(true);
            constraints.setCountLimit(0);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration res = dirContext.search(
                    RoleLdapHelper.ROLE_BASE_DN, filter, constraints);

            while (res.hasMoreElements())
            {
                SearchResult tmpSearchResult = (SearchResult) res.nextElement();
                Attributes entry = tmpSearchResult.getAttributes();
                Attribute attr = entry.get(RoleLdapHelper.LDAP_ATTR_MEMBERSHIP);

                if (attr != null)
                {
                    NamingEnumeration userDnsEnu = attr.getAll();
                    String[] userDns = new String[attr.size()];
                    int i = 0;
                    while (userDnsEnu.hasMoreElements())
                    {
                        String userDn = userDnsEnu.nextElement().toString();
                        userDns[i] = userDn;
                        i++;
                    }
                    userDnsEnu.close();
                    userIds.addAll(RoleLdapHelper.filterRoleMembers(userDns,
                            p_projectUserId));
                }
            }
            res.close();
        }
        catch (NameNotFoundException ex)
        {
            return new String[0];
        }
        catch (NamingException ex)
        {
            // ignorance.
        }

        String[] finalIdsArray = new String[userIds.size()];
        finalIdsArray = (String[]) userIds.toArray(finalIdsArray);
        return finalIdsArray;
    }

    /**
     * Get users based on the given uids
     * 
     * @return a Vector of User objects
     */
    private Vector getUsers(String[] p_uids, String[] p_targetAttrs,
            DirContext dirContext) throws NamingException
    {

        Vector users = new Vector();

        if ((p_uids != null) && (p_uids.length != 0))
        {
            String filter = UserLdapHelper.getSearchFilterOnUIDs(p_uids);
            users = getUsers(filter, p_targetAttrs, dirContext);
        }

        return users;
    }

    /**
     * Get users matched the specified criteria with the specified attributes
     */
    private Vector getUsers(Attribute[] p_userAttrs, Attribute[] p_roleAttrs,
            String[] p_targetUserAttrs, Project p_project)
            throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        String[] uidSet1 = null;
        String[] uidSet2 = null;
        String[] uidSet3 = null;
        String[] commonUids = null;

        Vector result = null;
        String[] errorMsgArg;

        // check out a connection from the connection pool
        DirContext dirContext = checkOutConnection(false);
        setConnectionOptions(dirContext);

        // Search uids from the User branch
        try
        {
            uidSet1 = getUserIds(p_userAttrs, dirContext);
        }
        catch (NamingException e)
        {
            // return the connection to the pool
            checkInConnection(dirContext);
            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_UIDS_FROM_USER };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsers(): " + errorMsgArg[0], e);
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }

        // Search the uids from the Role branch
        try
        {
            uidSet3 = getUserIdsFromRole(p_roleAttrs, dirContext);
        }
        catch (NamingException e)
        {
            // return the connection to the pool
            checkInConnection(dirContext);

            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_UIDS_FROM_ROLE };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsers(): " + errorMsgArg[0], e);

            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }

        // get the common set of user ids of the three search,
        // excluding the null set
        commonUids = LdapHelper.getCommonSet(uidSet1, LdapHelper.getCommonSet(
                uidSet2, uidSet3));

        // filter out the users that are in the specified project
        if (p_project != null)
        {
            commonUids = filterUsersByProject(commonUids, p_project);
        }

        // Search User entries based on the common set of uids
        try
        {
            result = getUsers(commonUids, p_targetUserAttrs, dirContext);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::getUsers(): " + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }

        return result;
    }

    /**
     * Add the rate to the specified role.
     */
    private void addRateToRole(Rate p_rate, Role p_role)
            throws UserManagerException, RemoteException
    {
        String errorMsgKey = UserManagerException.MSG_ADD_RATE_TO_ROLE_ERROR;
        String[] errorMsgArg = { Long.toString(p_rate.getId()),
                p_role.getName() };

        // if the rate being added has a duplicate name - this is an error
        if (isDuplicateRateName(p_rate.getName(), p_role))
        {
            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        // generate a ModificationItem[] to hold the attributes to be modified
        String rates[] = { Long.toString(p_rate.getId()) };
        ModificationItem[] modSet = RoleLdapHelper
                .addRatesModificationSet(rates);
        try
        {
            // Modify the entry
            dirContext.modifyAttributes(RoleLdapHelper.getRoleDN(p_role
                    .getName()), modSet);
        }
        catch (NameNotFoundException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_ROLE_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRateToRole(): " + "(role name = "
                    + p_role.getName() + ", rate = " + rates + ")\n"
                    + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRateToRole(): " + "(role name = "
                    + p_role.getName() + ", rate = " + rates + ")\n"
                    + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }
    }

    /**
     * Remove the rate from the specified role.
     */
    private void removeRateFromRole(Rate p_rate, Role p_role)
            throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_REMOVE_RATE_FROM_ROLE_ERROR;
        String[] errorMsgArg = { Long.toString(p_rate.getId()),
                p_role.getName() };

        // check out a connection from the pool with a system defined user bond.
        DirContext dirContext = checkOutConnection(true);

        // generate a ModificationItem[] to hold the attributes to be modified
        String rates[] = { Long.toString(p_rate.getId()) };
        ModificationItem[] modSet = RoleLdapHelper
                .deleteRatesModificationSet(rates);

        try
        {
            // Modify the entry
            dirContext.modifyAttributes(RoleLdapHelper.getRoleDN(p_role
                    .getName()), modSet);
        }
        catch (NameNotFoundException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_ROLE_NOT_EXIST };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeRateFromRole(): "
                    + "(role name = " + p_role.getName() + ", rate = " + rates
                    + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        catch (NoSuchAttributeException ex)
        {
            // ignorance.
        }
        catch (NamingException ex)
        {
            errorMsgArg = new String[] { UserManagerException.ARG_LDAP_UNKNOWN_ERROR };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeRateFromRole(): "
                    + "(role name = " + p_role.getName() + ", rate = " + rates
                    + ")\n" + errorMsgArg[0], ex);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ex);
        }
        finally
        {
            // return the connection to the pool
            checkInConnection(dirContext);
        }
    }

    /**
     * Get the role DNs of which containes the specified user
     */
    private Vector getRoleDNs(String p_uid, DirContext dirContext)
            throws NamingException
    {
        Vector roleDNs = null;
        String[] attrs = new String[] { RoleLdapHelper.ROLE_LDAP_RDN_ATTRIBUTE };
        String filter = RoleLdapHelper.getSearchFilterOnUserId(p_uid);
        // search
        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrs);
        NamingEnumeration res = dirContext.search(RoleLdapHelper.ROLE_BASE_DN,
                filter, constraints);

        // generate the DNs
        roleDNs = RoleLdapHelper.getRoleDNsFromSearchResults(res);

        return roleDNs;
    }

    /**
     * Get the role DNs that are deactive and contain the specified user.
     */
    private Vector getInactiveRoleDNs(String p_uid, DirContext dirContext)
            throws NamingException
    {
        String[] attrs = new String[] { RoleLdapHelper.ROLE_LDAP_RDN_ATTRIBUTE };
        String filter = RoleLdapHelper
                .getSearchFilterForInactiveRolesOnUserId(p_uid);

        // search
        SearchControls constraints = new SearchControls();
        constraints.setReturningObjFlag(true);
        constraints.setCountLimit(0);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrs);
        NamingEnumeration res = dirContext.search(RoleLdapHelper.ROLE_BASE_DN,
                filter, constraints);

        // generate the DNs
        Vector roleDNs = RoleLdapHelper.getRoleDNsFromSearchResults(res);
        return roleDNs;
    }

    /**
     * Filter the list of user ids passed in and just return the ones that are
     * in the project specified.
     */
    private String[] filterUsersByProject(String[] p_userIds, Project p_project)
    {
        Set projectUserIds = null;
        ArrayList finalUserIds = new ArrayList();
        if (p_userIds != null)
        {
            if (p_project != null)
            {
                projectUserIds = p_project.getUserIds();
            }

            if (projectUserIds != null)
            {
                for (int i = 0; i < p_userIds.length; i++)
                {
                    String userId = p_userIds[i];
                    // if the specified user is contained in the project
                    // then add to the final list.
                    if (projectUserIds.contains(userId))
                    {
                        finalUserIds.add(userId);
                    }
                }// end for
            }
        }

        String[] finalIdsArray = new String[finalUserIds.size()];
        finalIdsArray = (String[]) finalUserIds.toArray(finalIdsArray);

        return finalIdsArray;
    }

    /**
     * Returns 'true' if all the required fields of the user have been
     * specified. Returns 'false' if one or more of the fields haven't been
     * specified.
     */
    private boolean userContainsRequiredFields(User p_user)
    {
        boolean result = false;

        if (p_user.isUserValid() && p_user.getFirstName() != null
                && p_user.getFirstName().length() > 0
                && p_user.getLastName() != null
                && p_user.getLastName().length() > 0
                && p_user.getPassword() != null
                && p_user.getPassword().length() > 0
                && p_user.getEmail() != null && p_user.getEmail().length() > 0)
        {
            result = true;
        }

        return result;
    }

    private void setConnectionOptions(DirContext dirContext)
            throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_SET_CONNECTION_OPTIONS_ERROR;

        try
        {
            // unlimited size limit
            // wait for all results
            dirContext.addToEnvironment(DirContext.BATCHSIZE, "0");

        }
        catch (NamingException ex)
        {
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::setConnectionOptions(): ", ex);
            throw new UserManagerException(errorMsgKey, null, ex);
        }
    }

    /**
     * Checks out an DirContext object from the connection pool. Also binds a
     * System defined user to the connection by demand.
     */
    private DirContext checkOutConnection(boolean p_bindUser)
            throws UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_AUTHENTICATE_BINDING_USER_ERROR;

        // NOTE:
        // for right now bind at all times - we are having problems
        // with many users on searches - but binding at all times seems to
        // fix the problem. Leave until we have a better solution
        //
        DirContext dirContext = null;
        try
        {
            // check out a connection from the connection pool
            dirContext = dirContextPool.getDirContext();
            // bind a user to the connection
            UserLdapHelper.bindUser(dirContext, getLDAPLoginDN(),
                    LdapHelper.LDAP_PASSWORD);
        }
        catch (NamingException ne)
        {
            // return the connection to the pool
            checkInConnection(dirContext);

            String[] errorMsgArg = { "(binding user id =" + getLDAPLoginDN()
                    + ")\n" };
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::bindUserToConnection(): "
                    + errorMsgArg[0], ne);
            throw new UserManagerException(errorMsgKey, errorMsgArg, ne);
        }

        return dirContext;
    }

    /**
     * Create the proper exception for an error with user role
     * creation/modification. It specifies if any roles failed to be added.
     * 
     * @param p_userId
     *            The id of the user whose role creation/modification failed.
     * @param p_ex
     *            The exception that caused an error. This can be NULL if an
     *            error occurred was detected without an exception being thrown.
     * @param p_numOfFailedRoles
     *            The number of failed roles.
     * @param p_failedRoles
     *            The names of any roles that the user failed to be added to.
     * @return UserManagerException
     */
    private UserManagerException createUserRoleException(String p_userId,
            Exception p_ex, int p_numOfFailedRoles, String[] p_failedRoles)
    {
        String errMessageKey = UserManagerException.MSG_FAILED_TO_ADD_ROLES_TO_USER;
        String errArgs[] = new String[2];

        // an exception was thrown so all roles failed
        if (p_ex != null)
        {
            errArgs[0] = "ALL"; // tbd - change this to come out of some
            // property file??
            errArgs[1] = p_userId;
        }
        else
        {
            StringBuffer errRoles = new StringBuffer();
            if (p_numOfFailedRoles > 0)
            {
                errRoles.append(p_failedRoles[0]);
                for (int x = 1; x < p_numOfFailedRoles; x++)
                {
                    errRoles.append(", ");
                    errRoles.append(p_failedRoles[x]);
                }
            }

            errArgs[0] = errRoles.toString();
            errArgs[1] = p_userId;
        }

        CATEGORY.error(errMessageKey + errArgs.toString(), p_ex);
        return new UserManagerException(errMessageKey, errArgs, p_ex);
    }

    /**
     * Returns an DirContext object to the connection pool.
     */
    private void checkInConnection(DirContext dirContext)
    {

        try
        {
            dirContextPool.closeDirContext(dirContext);
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Remove the calendar associated with the user based on the given username.
     */
    private void removeUserCalendar(String p_uid)
    {
        try
        {
            ServerProxy.getCalendarManager().removeUserCalendar(p_uid);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to remove the calendar of user '" + p_uid
                    + "'", e);
        }
    }

    /**
     * Calculates the users that logged in the system.
     * 
     * @param p_userName -
     *            the user name.
     * @param p_sessionId -
     *            the session id of the user.
     */
    public void loggedInUsers(String p_userName, String p_sessionId)
    {
        if (p_sessionId != null)
        {
            m_userMap.put(p_sessionId, p_userName);
            CATEGORY.info("Logging in user: " + p_userName + ". Total users="
                    + m_userMap.size());
        }
    }

    /**
     * Calculates the users after logging out the system.
     * 
     * @param p_userName -
     *            the user name.
     * @param p_sessionId -
     *            the session id of the user.
     */
    public void loggedOutUsers(String p_userName, String p_sessionId)
    {
        if (p_sessionId != null)
        {
            m_userMap.remove(p_sessionId);
            CATEGORY.info("Logging out user: " + p_userName + ". Total users="
                    + m_userMap.size());
        }
    }
}

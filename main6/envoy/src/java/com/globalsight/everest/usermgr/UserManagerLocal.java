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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.globalsight.everest.company.CompanyThreadLocal;
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
import com.globalsight.everest.persistence.project.ProjectUnnamedQueries;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserFieldSecurity;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.vendormanagement.UpdatedDataEvent;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

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
    private Map<String, String> m_userMap = null;
    
    static final String LDAP_PWD_MD5 = "MD5";

    static final String LDAP_PREFIX_MD5 = "{MD5}";

    //
    // Constructor
    //

    public UserManagerLocal() throws UserManagerException
    {
        m_userMap = new Hashtable<String, String>(20);

        String errorMsgKey = UserManagerException.MSG_INIT_SERVER_ERROR;
        try
        {
            dirContextPool = LdapHelper.getConnectionPool();
        }
        catch (NamingException le)
        {
            String[] errorMsgArg =
            { "Couldn't connect to LDAP" };

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
            errorMsgArg = new String[]
            { UserManagerException.ARG_INVALID_ROLE };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addRole(): " + "(Role name= "
                    + (p_role == null ? "null" : p_role.toString()) + ")"
                    + errorMsgArg[0], null);
            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        HibernateUtil.saveOrUpdate(p_role);
    }

    public void removeRole(Role role) throws RemoteException,
            UserManagerException
    {
        if (role == null)
        {
            return;
        }

        try 
        {
			HibernateUtil.delete(role);
		} 
        catch (Exception e) 
        {
        	CATEGORY.error(e);
        	throw new UserManagerException(e);
		}
    }

    /**
     * Removes the specified role from LDAP database.
     */
    public void removeRoleFromLDAP(String roleName) throws RemoteException,
            UserManagerException
    {
    	try 
    	{
			HibernateUtil.delete(RoleDatabaseHelper.getContainerRoleByName(roleName));
			HibernateUtil.delete(RoleDatabaseHelper.getUserRoleByName(roleName));
		} 
    	catch (Exception e) 
    	{
    		 CATEGORY.error(e);
    		 throw new UserManagerException(e);
		}
    	
    }

    /**
     * @see UserManager.addUser(User, User, List, FieldSecurity, List, List)
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException
    {
        addUserBasicInfo(p_userRequestingAdd, p_user, p_projectIds,
                p_fieldSecurity, true);
        addUserRoles(p_user, p_roles);
    }

    /**
     * @see UserManager.addUser(User, User, List, FieldSecurity, List, List)
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles,
            boolean needEncodePwd) throws RemoteException, UserManagerException
    {
        addUserBasicInfo(p_userRequestingAdd, p_user, p_projectIds,
                p_fieldSecurity, needEncodePwd);
        addUserRoles(p_user, p_roles);
    }

    /**
     * Removes the specified user from LDAP database.
     */
    public void removeUserFromLDAP(String userId) throws RemoteException,
            UserManagerException
    {
    	try 
    	{
			HibernateUtil.delete(getUser(userId));
		} 
    	catch (Exception e) 
    	{
    		 CATEGORY.error(e);
		}
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
            errorMsgArg = new String[]
            { UserManagerException.ARG_PROTECTED_USER };

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
        
        List<ContainerRoleImpl> crs = RoleDatabaseHelper.getContainerRoleByUserId(p_uid);
        for (ContainerRoleImpl cr : crs)
        {
        	RoleDatabaseHelper.removeUserFromContainerRole(cr, p_uid);
        }

        removeUserCalendar(p_uid);

        UserRoleImpl ur = RoleDatabaseHelper.getUserRoleByUserId(p_uid);
        try 
        {
			HibernateUtil.delete(ur);
			HibernateUtil.delete(u);

		} 
        catch (Exception e1) 
        {
        	CATEGORY.error(e1);
        	throw new UserManagerException(e1);
		}

        try
        {
            // now that the user has been removed from the roles -
            // can remove user as a task's acceptor
            ServerProxy.getTaskManager().removeUserAsTaskAcceptor(p_uid);
        }
        catch (Exception e)
        { /* exception is logged in TaskManager */
        }
        // remove user owned activities from interim table (TASK_INTERIM)
        TaskInterimPersistenceAccessor.deleteInterimUser(p_uid);
        UserUtil.removeUserFromUserIdUserName(p_uid);
        OperationLog.log(p_userRequestingRemove.getUserId(),
                OperationLog.EVENT_DELETE, OperationLog.COMPONET_USERS,
                u.getUserName());
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

        String hql = "from UserRoleImpl u where u.user = :user and u.state = :state";
        Map map = new HashMap();
        map.put("user", p_uid);
        map.put("state", User.State.DEACTIVE);
        
        List<UserRoleImpl> urs = (List<UserRoleImpl>) HibernateUtil.search(hql, map);
        for (UserRoleImpl ur : urs)
        {
        	ur.setState(User.State.ACTIVE);
        	
        	//add to ContainerRole
        	String name = ur.getName();
        	int index = name.indexOf(p_uid);
        	if (index > 0)
        	{
				name = name.substring(0, index - 1);
				String uids[] = { p_uid };
				addUsersToRole(uids, name);
        	}
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
            errorMsgArg = new String[]
            { UserManagerException.ARG_PROTECTED_USER };

            CATEGORY.error("Can't deactiavte the ldap connection user " + p_uid
                    + ")\n" + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        // remove the user from all the projects
        associateUserWithProjectsById(p_uid, new ArrayList());

        // check out a connection from the pool with a system defined user bond.
//        DirContext dirContext = checkOutConnection(true);

        try
        {
            // deactivate the user's roles
            deactivateUserRoles(p_uid);
        }
        catch (NamingException e)
        {
            errorMsgArg = new String[]
            { UserManagerException.ARG_FAILED_TO_DEACTIVATE_USER_ROLES };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::deactivateUser(): " + "(User Id = "
                    + p_uid + ")\n" + errorMsgArg[0], e);
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }
        finally
        {
            // return the connection to the pool
//            checkInConnection(dirContext);
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

        User user = getUser(p_uid);
        user.setState(User.State.DEACTIVE);
        HibernateUtil.saveOrUpdate(user);

        return user;
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
            errorMsgArg = new String[]
            { UserManagerException.ARG_INVALID_USER };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::modifyUser(): " + "(User Name = "
                    + (p_user == null ? null : p_user.getUserName()) + "):\n"
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
            CATEGORY.error("Failed to modify the user " + p_user.getUserName(),
                    e);
            errorMsgArg = new String[]
            { p_user.getUserName() };
            throw new UserManagerException(errorMsgKey, errorMsgArg, e);
        }

        String password = p_user.getPassword();
        if (!password.startsWith("{MD5}"))
        {
        	p_user.setPassword(encyptMD5Password(password));
        }
        HibernateUtil.saveOrUpdate(p_user);
        OperationLog.log(p_userRequestingMod.getUserId(),
                OperationLog.EVENT_EDIT, OperationLog.COMPONET_USERS,
                p_user.getUserName());
        // assign the user to the appropriate projects
        associateUserWithProjectsById(p_user.getUserId(), p_projectIds);

        modifyUserRoles(p_user, p_roles);
        m_eventHandler.dataUpdated(p_user, new UpdatedDataEvent(
                UpdatedDataEvent.UPDATE_EVENT, p_userRequestingMod));
    }

    /**
     * Get user matched the given uid
     * 
     * @param p_uid
     *            - The user id
     * 
     * @return a User object
     * @exception UserManagerException
     *                - Component related exception.
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
        if (p_uid == null || p_uid.trim().equals(""))
            return null;
        
        String hql = "from UserImpl u where u.userId = ?";
    	return (User) HibernateUtil.getFirst(hql, p_uid);
    }

    /**
     * Gets user matched the given name.
     */
    public User getUserByName(String p_userName) throws RemoteException,
            UserManagerException
    {
        return getUser(UserUtil.getUserIdByName(p_userName));
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
    	List uIds = new ArrayList();
    	for (String uId : p_userIds)
    	{
    		uIds.add(uId);
    	}
    	
        Session session = HibernateUtil.getSession();
    	
    	boolean filterUser = false;
    	
    	Criteria c = session.createCriteria(UserImpl.class);
    	c.add(Restrictions.in("userId", uIds));
    	c.add(Restrictions.not(Restrictions.eq("state", User.State.DEACTIVE)));
    	c.add(Restrictions.not(Restrictions.eq("state", User.State.DELETED)));
    	
    	List<UserImpl> us = c.list();
    	List userInfos = new ArrayList();
    	for (UserImpl u : us)
    	{
    		userInfos.add(u.toUserInfo());
    	}
    	
    	return userInfos;
    }

    /**
     * @see UserManager.getUserInfos(String, String, String)
     */
    public List getUserInfos(String p_activityName, String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {

    	List<ContainerRoleImpl> crs = getContainerRoles(p_activityName, p_sourceLocale, p_targetLocale);
    	Set<String> ids = new HashSet<String>();
    	
    	for (ContainerRoleImpl c : crs)
    	{
    		ids.addAll(c.getUserIds());
    	}
    	
    	List result = new ArrayList();
    	for (String userId : ids)
    	{
    		UserImpl u = (UserImpl) getUser(userId);
    		result.add(u.toUserInfo());
    	}
    	
    	return result;
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
            String[] errorMsgArg =
            { UserManagerException.ARG_FAILED_TO_GET_COMPANY_ENTRIES };

            CATEGORY.error("getCompanyNames :: " + errorMsgArg[0], e);

            throw new UserManagerException(
                    UserManagerException.MSG_GET_COMPANY_NAMES_ERROR,
                    errorMsgArg, e);
        }

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
    
    /**
     * @see UserManager.getEmailInformationForUser(String)
     */
    public EmailInformation getEmailInformationForUser(String p_userId)
            throws RemoteException, UserManagerException
    {
        User user = getUser(p_userId);
        if (user == null)
        	return null;
        
        StringBuffer sb = new StringBuffer();

        sb.append(user.getFirstName());
        sb.append(" ");
        sb.append(user.getLastName());
        String email = user.getEmail();
        String ccEmail = user.getCCEmail();
        String bccEmail = user.getBCCEmail();
        String uiLocale = user.getDefaultUILocale();
        String companyName = user.getCompanyName();

        // return new EmailInformation(p_userId, sb.toString(), email,
        // uiLocale, getUserTimeZone(p_userId));
        EmailInformation eInfor = new EmailInformation(p_userId, sb.toString(),
                email, uiLocale, getUserTimeZone(p_userId));
        eInfor.setCCEmailAddress(ccEmail);
        eInfor.setBCCEmailAddress(bccEmail);
        eInfor.setCompanyName(companyName);
        
        return eInfor;
    }

    /**
     * @see UserManager.getEmailInformationForUsers(String[])
     */
    public List getEmailInformationForUsers(String[] p_userIds)
            throws RemoteException, UserManagerException
    {
    	List result = new ArrayList();
    	
    	for (String uId : p_userIds)
    	{
    		EmailInformation e = getEmailInformationForUser(uId);
    		
    		if (e != null)
    		{
    			result.add(e);
    		}
    	}
    	
    	return result;
    }

    /**
     * Get the user names (i.e. user ids) as an array of strings. Returns the
     * user names of the ACTIVE users.
     * 
     * @return An array of user names.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getUserNamesFromCurrentAndSuperCompany()
            throws RemoteException, UserManagerException
    {
    	String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String superCompanyName = CompanyWrapper
                .getCompanyNameById(CompanyWrapper.SUPER_COMPANY_ID);
        
        if (companyName.equals(superCompanyName))
        {
        	return getUserNamesFromAllCompanies();
        }
        
        Vector<UserImpl> us = getUsers();
    	
    	List names = new ArrayList();
    	for (UserImpl u : us)
    	{
    		if (u.getCompanyName().equals(companyName) || u.getCompanyName().equals(superCompanyName))
    		{
    			names.add(u.getUserName());
    		}
    	}
    	
    	return (String[]) names.toArray();
    }

    public String[] getUserNamesFromAllCompanies() throws RemoteException,
            UserManagerException
    {
    	Vector<UserImpl> us = getUsersFromAllCompany();
    	
    	List names = new ArrayList();
    	for (UserImpl u : us)
    	{
    		names.add(u.getUserName());
    	}
    	
    	String[] result = new String[us.size()];
    	return (String[]) names.toArray(result);
    }

    /**
     * @see UserManager.getVendorlessUsers()
     */
    public Vector getVendorlessUsers() throws RemoteException,
            UserManagerException
    {
    	String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String superCompanyName = CompanyWrapper
                .getCompanyNameById(CompanyWrapper.SUPER_COMPANY_ID);
        
        Vector<User> us = null;
        Vector<User> us2 = new Vector<User>();
        
        if (companyName.equals(superCompanyName))
        {
        	us = getUsers();
        }
        else
        {
        	us = getUsersFromCompany(companyId);
        }
        
        Collection usersWithVendors = ServerProxy.getVendorManagement()
                .getUserIdsOfVendors();
        for (User u : us)
        {
            if (!usersWithVendors.contains(u.getUserId()))
            {
            	us2.add(u);
            }
        }
        
        return us2;
//        
//    	
//        DirContext dirContext = checkOutConnection(false);
//        Vector users = null;
//
//        try
//        {
//            // get all active and created user ids
//            String filter = UserLdapHelper.getSearchFilter();
//            String[] attrs = new String[]
//            { UserLdapHelper.LDAP_ATTR_USERID };
//
//            SearchControls constraints = new SearchControls();
//            constraints.setReturningObjFlag(true);
//            constraints.setCountLimit(0);
//            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
//            constraints.setReturningAttributes(attrs);
//            NamingEnumeration res = dirContext.search(
//                    UserLdapHelper.USER_BASE_DN, filter, constraints);
//
//            Vector userNames = UserLdapHelper
//                    .getUIDsVectorFromSearchResults(res);
//            // now remove the user ids that are already vendors
//            Collection usersWithVendors = ServerProxy.getVendorManagement()
//                    .getUserIdsOfVendors();
//            for (Iterator ui = usersWithVendors.iterator(); ui.hasNext();)
//            {
//                String userId = (String) ui.next();
//                if (userNames.contains(userId))
//                {
//                    userNames.remove(userId);
//                }
//            }
//
//            // get all the users specified by the user ids in the list
//            String[] userNamesArray = new String[userNames.size()];
//            userNamesArray = (String[]) userNames.toArray(userNamesArray);
//            users = getUsers(userNamesArray, null, dirContext);
//        }
//        catch (Exception ex)
//        {
//            CATEGORY.error("UserManagerException is thrown from: "
//                    + "UserManagerLocal::getVendorlessUsers().", ex);
//            throw new UserManagerException(
//                    UserManagerException.MSG_GET_VENDORLESS_USERS_ERROR, null,
//                    ex);
//        }
//        finally
//        {
//            // return the connection to the pool
//            checkInConnection(dirContext);
//        }
//
//        return users;
    }

    /**
     * @see UserManager.getUserIdsByFilter(String, Project)
     */
    public String[] getUserIdsByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        String[] userIds = null;

        if (p_roleName != null)
        {
//            // check out a connection from the connection pool
//            DirContext dirContext = checkOutConnection(false);
//            setConnectionOptions(dirContext);
//            try
//            {
//                userIds = getUserIdsFromRole(p_roleName, dirContext);
//            }
//            catch (Exception e)
//            {
//                String[] errorMsgArg = new String[]
//                { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
//                CATEGORY.error("UserManagerException is thrown from: "
//                        + "UserManagerLocal::getUserNamesByRole(): "
//                        + errorMsgArg[0], e);
//                throw new UserManagerException(
//                        UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg,
//                        e);
//            }
//            finally
//            {
//                // return the connection to the pool
//                checkInConnection(dirContext);
//            }
        	List<String> allIds = new ArrayList<String>();
        	
        	ContainerRoleImpl c = RoleDatabaseHelper.getContainerRoleByName(p_roleName);
    		if (c != null)
    		{
    			allIds.addAll(c.getUserIds());
    		}
    		
    		UserRoleImpl ur = RoleDatabaseHelper.getUserRoleByName(p_roleName);
    		if (ur != null)
    		{
    			allIds.add(ur.getUser());
    		}
    		
    		userIds = new String[allIds.size()];
    		userIds = (String[]) allIds.toArray(userIds);
        }
        else
        {
            // role is null, so just get all users and then let the filter sort
            // it out
            Vector allUsers = getUsers();
            userIds = new String[allUsers.size()];
            Iterator userIter = allUsers.iterator();
            int i = 0;
            while (userIter.hasNext())
            {
                User u = (User) userIter.next();
                userIds[i++] = u.getUserId();
            }

        }

        // if project was specified then filter by it
        if (p_project != null)
        {
            return filterUsersByProject(userIds, p_project);
        }
        else
        // otherwise just return the ones found
        {
            return userIds;
        }
    }

    /**
     * @see UserManager.getUserIdsFromRoles(String[], Project)
     */
    public String[] getUserIdsFromRoles(String[] p_roleNames, Project p_project)
            throws RemoteException, UserManagerException
    {
        // sanity check
        if (p_roleNames == null || p_roleNames.length == 0)
        {
            return null;
        }
        
        List<String> ids = new ArrayList<String>();
        List<String> allIds = new ArrayList<String>();
        
        Set projectUserIds = p_project == null ? null : p_project.getUserIds();
        
        for (String name : p_roleNames)
    	{
    		ContainerRoleImpl c = RoleDatabaseHelper.getContainerRoleByName(name);
    		if (c != null)
    		{
    			allIds.addAll(c.getUserIds());
    		}
    		
    		UserRoleImpl ur = RoleDatabaseHelper.getUserRoleByName(name);
    		if (ur != null)
    		{
    			allIds.add(ur.getUser());
    		}
    	}
        
        for (String userId : allIds)
		{
			if (projectUserIds == null
                    || projectUserIds.contains(userId))
            {
				ids.add(userId);
            }
		}
        
        String[] finalIdsArray = new String[ids.size()];
        finalIdsArray = ids.toArray(finalIdsArray);
        return finalIdsArray;
//
//        String[] userIds = null;
//        // get the users associated with the given project.
//        
//
//        // String errorMsgKey = UserManagerException.MSG_GET_ROLES_ERROR;
//
//        // check out a connection from the connection pool
//        DirContext dirContext = checkOutConnection(false);
//        setConnectionOptions(dirContext);
//
//        try
//        {
//            userIds = getUserIdsFromRoles(p_roleNames, projectUserIds,
//                    dirContext);
//        }
//        catch (Exception ex)
//        {
//            String[] errorMsgArg = new String[]
//            { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
//
//            throw new UserManagerException(
//                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ex);
//        }
//        finally
//        {
//            checkInConnection(dirContext);
//        }
//
//        return userIds;
    }

    /**
     * @see UserManager.getUsersByFilter(String, Project)
     */
    public List getUsersByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        String[] userIds = getUserIdsByFilter(p_roleName, p_project);
        List users = new ArrayList();
        for (String id : userIds)
        {
        	users.add(id);
        }
        
        Criteria c = HibernateUtil.getSession().createCriteria(UserImpl.class);
        c.add(Restrictions.in("userId", users));
        return c.list();

        // check out a connection from the connection pool
//        DirContext dirContext = checkOutConnection(false);
//        setConnectionOptions(dirContext);
//        List users = null;
//        try
//        {
//            users = getUsers(userIds, null, dirContext);
//        }
//        catch (NamingException ldp)
//        {
//            String[] errorMsgArg = new String[]
//            { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
//            CATEGORY.error(
//                    "Failed to get all the users that match the filter - Role: "
//                            + p_roleName + ", Project: " + p_project.getName()
//                            + p_roleName + ", Project: " + p_project.getName(),
//                    ldp);
//            throw new UserManagerException(
//                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ldp);
//        }
//        finally
//        {
//            checkInConnection(dirContext);
//        }
//        return users;
    }

//    /**
//     * get users from ldap follow the filter
//     */
//    public List getUsersByFilter(String p_filter) throws RemoteException,
//            UserManagerException
//    {
//        // check out a connection from the connection pool
//        DirContext dirContext = checkOutConnection(false);
//        setConnectionOptions(dirContext);
//        List users = null;
//        try
//        {
//            users = getUsers(p_filter, null, dirContext);
//        }
//        catch (NamingException ldp)
//        {
//            String[] errorMsgArg = new String[]
//            { UserManagerException.ARG_FAILED_TO_GET_USERS_ENTRIES };
//            CATEGORY.error(
//                    "Failed to get all the users that match the filter - filter: "
//                            + p_filter, ldp);
//            throw new UserManagerException(
//                    UserManagerException.MSG_GET_USERS_ERROR, errorMsgArg, ldp);
//        }
//        finally
//        {
//            checkInConnection(dirContext);
//        }
//        return users;
//    }

    /**
     * @see UserManager.getUsersFromEmail(String)
     */
    @SuppressWarnings("unchecked")
    public List getUsersByEmail(String p_email) throws UserManagerException,
            RemoteException
    {
        List users = new ArrayList();
        
        String hql = "from UserImpl u where u.state != :state1 and u.state != :state2 and u.email = :email";
    	Map map = new HashMap();
    	map.put("state1", User.State.DELETED);
    	map.put("state2", User.State.DEACTIVE);
    	map.put("email", p_email);
    	
    	users.addAll(HibernateUtil.search(hql, map));
    	
    	return users;
    }

    /**
     * Get users matched the specified criteria
     * 
     * @param p_userAttrs
     *            - Arrtibute array contains the User entry attributes
     * @param p_roleAttrs
     *            - Attribute array contains the Role entry attributes
     * @param p_project
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                - Network related exception.
     */
    public Vector getUsers(UserSearchParams p_searchParams,
            Project p_project) throws RemoteException, UserManagerException
    {
    	Session session = HibernateUtil.getSession();
    	
    	boolean filterUser = false;
    	Vector<UserImpl> us = new Vector<UserImpl>();
    	
    	Criteria c = session.createCriteria(UserImpl.class);
    	String userId = p_searchParams.getIdName();
    	if (userId != null && userId.length() > 0)
    	{
    		filterUser = true;
    		c.add(Restrictions.or(Restrictions.ilike("userId", "%" + userId + "%"), Restrictions.ilike("userName", "%" + userId + "%")));
    	}
    	
    	String firstName = p_searchParams.getFirstName();
    	if (firstName != null && firstName.length() > 0)
    	{
    		filterUser = true;
    		c.add(Restrictions.eq("firstName", firstName));
    	}
    	
    	String lastName = p_searchParams.getLastName();
    	if (lastName != null && lastName.length() > 0)
    	{
    		filterUser = true;
    		c.add(Restrictions.eq("lastName", lastName));
    	}
    	
    	String email = p_searchParams.getEmail();
    	if (email != null && email.length() > 0)
    	{
    		filterUser = true;
    		c.add(Restrictions.eq("email", email));
    	}
    	
    	if (filterUser)
    	{
    		us.addAll(c.list());
    	}

    	boolean filterRole = false;
    	Criteria cc = session.createCriteria(ContainerRoleImpl.class);
    	Criteria uc = session.createCriteria(UserRoleImpl.class);
    	
    	String sourceLocale = p_searchParams.getSourceLocaleParam();
    	if (sourceLocale != null && sourceLocale.length() > 0)
    	{
    		filterRole = true;
    		cc.add(Restrictions.eq("sourceLocale", sourceLocale));
    		uc.add(Restrictions.eq("sourceLocale", sourceLocale));
    	}
    	
    	String targetLocale = p_searchParams.getTargetLocaleParam();
    	if (targetLocale != null && targetLocale.length() > 0)
    	{
    		filterRole = true;
    		cc.add(Restrictions.eq("targetLocale", targetLocale));
    		uc.add(Restrictions.eq("targetLocale", targetLocale));
    	}
    	
    	if (filterRole)
    	{
    		List uids = new ArrayList();
    		
    		cc.add(Restrictions.eq("state", User.State.ACTIVE));
    		uc.add(Restrictions.eq("state", User.State.ACTIVE));
    		
    		List<ContainerRoleImpl> crs = cc.list();
    		List<UserRoleImpl> urs = uc.list();
    		
    		for (ContainerRoleImpl cr : crs)
    		{
    			uids.addAll(cr.getUserIds());
    		}
    		
    		for (UserRoleImpl ur : urs)
    		{
    			uids.add(ur.getUser());
    		}
    		
    		if (uids.size() > 0)
    		{
    			c = session.createCriteria(UserImpl.class);
        		c.add(Restrictions.in("userId", uids));
        		
        		us.addAll(c.list());
    		}
    	}
    	
    	return us;
//        return getUsers(p_userAttrs, p_roleAttrs, null, p_project);
    }

    /**
     * Get all active and created users.
     * 
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                - Network related exception.
     */
    public Vector getUsersFromAllCompany() throws RemoteException, UserManagerException
    {
        Vector users = new Vector();
        
        String hql = "from UserImpl u where u.state != :state1 and u.state != :state2 and u.type != :type";
    	Map map = new HashMap();
    	map.put("state1", User.State.DELETED);
    	map.put("state2", User.State.DEACTIVE);
    	map.put("type", User.UserType.ANONYMOUS);
    	
    	users.addAll(HibernateUtil.search(hql, map));
    	return users;
    }
    
    /**
     * Get all active and created users.
     * 
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                - Network related exception.
     */
    public Vector getUsers() throws RemoteException, UserManagerException
    {
    	String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String superCompanyName = CompanyWrapper
                .getCompanyNameById(CompanyWrapper.SUPER_COMPANY_ID);
    	
        Vector users = new Vector();
        
        String hql = "from UserImpl u where u.state != :state1 and u.state != :state2 and u.type != :type";
    	Map map = new HashMap();
    	map.put("state1", User.State.DELETED);
    	map.put("state2", User.State.DEACTIVE);
    	map.put("type", User.UserType.ANONYMOUS);
    	
    	if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
    		hql += " and (u.companyName = :companyName or u.companyName = :superCompanyName)";
    		map.put("companyName", companyName);
    		map.put("superCompanyName", superCompanyName);
        }
    	
    	users.addAll(HibernateUtil.search(hql, map));
    	return users;
    }

    public Vector getUsers(String condition) throws RemoteException,
            UserManagerException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String superCompanyName = CompanyWrapper
                .getCompanyNameById(CompanyWrapper.SUPER_COMPANY_ID);

        Vector users = new Vector();

        String hql = "from UserImpl u where u.state != :state1 and u.state != :state2 and u.type != :type";
        Map map = new HashMap();
        map.put("state1", User.State.DELETED);
        map.put("state2", User.State.DEACTIVE);
        map.put("type", User.UserType.ANONYMOUS);

        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            hql += " and (u.companyName = :companyName or u.companyName = :superCompanyName)";
            map.put("companyName", companyName);
            map.put("superCompanyName", superCompanyName);
        }
        hql += condition;

        users.addAll(HibernateUtil.search(hql, map));
        return users;
    }

    /**
     * Get all active and created users for current company only.
     * 
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Vector getUsersForCurrentCompany() throws RemoteException,
            UserManagerException
    {
    	 String companyId = CompanyWrapper.getCurrentCompanyId();
    	 return getUsersFromCompany(companyId);
    }

    /**
     * Gets all users including inactive ones from specified company.
     */
    public Vector<User> getUsersFromCompany(String companyId)
            throws RemoteException, UserManagerException
    {
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        Vector users = new Vector();
        
        String hql = "from UserImpl u where u.state != :state1 and u.state != :state2 and u.type != :type and u.companyName = :companyName";
    	Map map = new HashMap();
    	map.put("state1", User.State.DELETED);
    	map.put("state2", User.State.DEACTIVE);
    	map.put("type", User.UserType.ANONYMOUS);
    	map.put("companyName", companyName);
    	
    	users.addAll(HibernateUtil.search(hql, map));
    	return users;
    }

    /**
     * Gets all roles including inactive ones from specified company.
     */
    public List<Role> getRolesFromCompany(String companyId)
            throws RemoteException, UserManagerException
    {
        List<Role> roles = new ArrayList<Role>();
        roles.addAll(RoleDatabaseHelper.getContainerRoleByCompanyId(companyId));
        roles.addAll(RoleDatabaseHelper.getUserRoleByCompanyId(companyId));
        return roles;
    }

    /**
     * @see UserManager.getUserInfos()
     */
    public Vector getUserInfos() throws RemoteException, UserManagerException
    {
        String errorMsgKey = UserManagerException.MSG_GET_USERS_ERROR;

        Vector userInfos = new Vector();
        Vector<UserImpl> us = getUsers();
        for (UserImpl u : us)
        {
        	userInfos.add(u.toUserInfo());
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
    	String hql = "from UserRoleImpl u where u.user = ? and (u.state = ? or u.state = ?)";
    	return HibernateUtil.search(hql, p_user.getUserId(), User.State.ACTIVE, User.State.CREATED);
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
    	String hql = "from ContainerRoleImpl c where c.userIds = ?";
    	return HibernateUtil.search(hql, p_user.getUserId());
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

    	String hql = "from UserRoleImpl u where u.activity.name= :activityName " +
    			"and u.sourceLocale = :sourceLocale and u.targetLocale = :targetLocale and u.state = " + User.State.ACTIVE;
    	Map map = new HashMap();
    	map.put("activityName", p_activityName);
    	map.put("sourceLocale", p_sourceLocale);
    	map.put("targetLocale", p_targetLocale);
    	
    	return HibernateUtil.search(hql, map);
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
    public List getContainerRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
    	String hql = "from ContainerRoleImpl u where u.activity.name= :activityName " +
    			"and u.sourceLocale = :sourceLocale and u.targetLocale = :targetLocale and u.state = " + User.State.ACTIVE;
    	Map map = new HashMap();
    	map.put("activityName", p_activityName);
    	map.put("sourceLocale", p_sourceLocale);
    	map.put("targetLocale", p_targetLocale);
    	
    	return HibernateUtil.search(hql, map);
    }

    /**
     * @see UserManager.getContainerRoles(String, String)
     * 
     */
    public Collection getContainerRoles(String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {
		String hql = "from ContainerRoleImpl u where u.sourceLocale = :sourceLocale "
				+ "and u.targetLocale = :targetLocale and u.state = "
				+ User.State.ACTIVE;
    	Map map = new HashMap();
    	map.put("sourceLocale", p_sourceLocale);
    	map.put("targetLocale", p_targetLocale);
    	
    	return HibernateUtil.search(hql, map);
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
     * Returns true if at least one user is in the project with the given id.
     * Otherwise, returns false.
     */
    private static boolean usersInProject(List p_userIds, long p_projectId)
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

    /**
     * @see UserManager.getContainerRole(String, String, String, long)
     */
    public ContainerRole getContainerRole(Activity p_activity,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
    	String hql = "from ContainerRoleImpl c where c.state = :state " +
    			"and  c.activity.id = :activityId and c.sourceLocale = :sourceLocale " +
    			"and c.targetLocale = :targetLocale";
    	Map map = new HashMap();
    	map.put("state", User.State.ACTIVE);
    	map.put("activityId", p_activity.getId());
    	map.put("sourceLocale", p_sourceLocale);
    	map.put("targetLocale", p_targetLocale);
    	
    	List<ContainerRoleImpl> crs = (List<ContainerRoleImpl>) HibernateUtil.search(hql, map);
    	for (ContainerRoleImpl cr : crs)
    	{
    		List uIds = cr.getUserIds();
    		if (p_projectId < 0 || usersInProject(uIds, p_projectId))
    		{
    			return cr;
    		}
    	}
    	
    	return null;
    }

    /**
     * @see UserManager.getContainerRole(String, String, String, long)
     */
    public ContainerRole getContainerRole(String p_activityName,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
    	String hql = "from ContainerRoleImpl c where c.state = :state " +
    			"and  c.activity.name = :activityName and c.sourceLocale = :sourceLocale " +
    			"and c.targetLocale = :targetLocale";
    	Map map = new HashMap();
    	map.put("state", User.State.ACTIVE);
    	map.put("activityName", p_activityName);
    	map.put("sourceLocale", p_sourceLocale);
    	map.put("targetLocale", p_targetLocale);
    	
    	List<ContainerRoleImpl> crs = (List<ContainerRoleImpl>) HibernateUtil.search(hql, map);
    	for (ContainerRoleImpl cr : crs)
    	{
    		List uIds = cr.getUserIds();
    		if (uIds.size() > 0 && usersInProject(uIds, p_projectId))
    		{
    			return cr;
    		}
    	}
    	
    	return null;
    }

    /**
     * @see UserManager.getContainerRole(Rate)
     */
    public ContainerRole getContainerRole(Rate p_rate, boolean p_withRates)
            throws RemoteException, UserManagerException
    {
    	 String hql = "select c from ContainerRoleImpl c join c.rateSet u where u.id = ? and c.state = " + User.State.ACTIVE;
    	return (ContainerRole) HibernateUtil.getFirst(hql, p_rate.getId());
    }

    /**
     * Removes users from a role.
     * 
     * @param p_uids
     *            - The user id to be deleted.
     * @param p_roleName
     *            - The role to add the user to.
     * @exception UserManagerException
     *                - Component related exception.
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
            errorMsgArg = new String[]
            { UserManagerException.ARG_INVALID_ROLE_NAME };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::removeUsersFromRole(): "
                    + "(role name = " + p_roleName + ", users = " + p_uids
                    + ")\n" + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }

        ContainerRoleImpl cr = RoleDatabaseHelper.getContainerRoleByName(p_roleName);
        
        if (cr == null)
        {
            errorMsgArg = new String[]
            { UserManagerException.ARG_INVALID_ROLE_NAME };

            CATEGORY.error("Can not find the container role with name: " + p_roleName);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }
        
        List<String> uIds = new ArrayList<String>();
        for (String s : p_uids)
        {
        	uIds.add(s);
        }
        RoleDatabaseHelper.removeUserFromContainerRole(cr, uIds);

    }

    /**
     * Adds users to a role.
     * 
     * @param p_uids
     *            - The user ids to be added.
     * @param p_roleName
     *            - The role to add the user to.
     * @exception UserManagerException
     *                - Component related exception.
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
            errorMsgArg = new String[]
            { UserManagerException.ARG_INVALID_ROLE_NAME };

            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::addUsersToRole(): " + "(role name = "
                    + p_roleName + ", users = " + p_uids + ")\n"
                    + errorMsgArg[0], null);

            throw new UserManagerException(errorMsgKey, errorMsgArg, null);
        }
        
        Role role = RoleDatabaseHelper.getRoleByName(p_roleName);
        if (role == null)
        {
        	CATEGORY.error("Can not find the role with name: " + p_roleName);
        	return;
        }
        
        if (role instanceof ContainerRoleImpl) 
        {
			ContainerRoleImpl cRole = (ContainerRoleImpl) role;
			List ids = cRole.getUserIds();
			
			for (String id : p_uids)
	        {
				if (!ids.contains(id))
				{
					ids.add(id);
				}
	        }
		}
        else if (role instanceof UserRoleImpl)
        {
        	UserRoleImpl uRole = (UserRoleImpl) role;
        	uRole.setUser(p_uids[0]);
        }

        HibernateUtil.saveOrUpdate(role);
    }

    /**
     * Get the Role with the role name.
     * 
     * @param p_roleName
     *            the role name.
     * @returns the Role with the role name.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Role getRole(String p_roleName) throws RemoteException,
            UserManagerException
    {
    	return RoleDatabaseHelper.getRoleByName(p_roleName);
    }
    
    private List user2userInfo(List<UserImpl> users)
    {
    	List userInfos = new ArrayList();
    	
    	for (UserImpl u : users)
    	{
    		userInfos.add(u.toUserInfo());
    	}
    	
    	return userInfos;
    }

    /**
     * @see UserManager.getUserInfosInAllProjects()
     */
    public List getUserInfosInAllProjects() throws RemoteException,
            UserManagerException
    {
    	String companyId = CompanyThreadLocal.getInstance().getValue();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        
    	String hql = "from UserImpl u where u.isInAllProjects = 'Y' and u.state = :state";
    	Map map = new HashMap();
    	map.put("state", User.State.ACTIVE);
    	
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
        	hql += " u.companyName = :companyName";
        	map.put("companyName", companyName);
        }
        
        List<UserImpl> users = (List<UserImpl>) HibernateUtil.search(hql, map);
    	return user2userInfo(users);
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

    // --------------------- private methods -----------------------

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
     * Add the user's basic info which includes the field security and projects
     * too - but not roles.
     */
    private void addUserBasicInfo(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity,
            boolean needEncodePwd) throws UserManagerException, RemoteException
    {
        String errorMsgKey = UserManagerException.MSG_ADD_USER_ERROR;
        String[] errorMsgArg;

        try
        {
            // Validate the parameter
            if (p_user == null || (!p_user.isUserValid()))
            {
                errorMsgArg = new String[]
                { UserManagerException.ARG_INVALID_USER };

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
            
            String pass = p_user.getPassword();
            if (pass != null && !pass.startsWith("{MD5}"))
            {
                pass = encyptMD5Password(pass);
            }
            p_user.setPassword(pass);
            HibernateUtil.saveOrUpdate(p_user);
            OperationLog.log(p_userRequestingAdd.getUserId(),
                    OperationLog.EVENT_ADD, OperationLog.COMPONET_USERS,
                    p_user.getUserId());

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
            UserUtil.removeUserFromUserIdUserName(p_user.getUserId());
            removeUser(p_userRequestingAdd, p_user.getUserId());
            CATEGORY.error("Failed to add the user " + p_user.getUserName(), e);
            errorMsgArg = new String[]
            { p_user.getUserName() };
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
                    Role cContainerRole = getContainerRole(
                            curRole.getActivity(), curRole.getSourceLocale(),
                            curRole.getTargetLocale());

                    if (cContainerRole != null)
                    {
                        // only add the user to the container roles if
                        // the user is active
                        if (p_user.isActive())
                        {
                            String[] uids =
                            { p_user.getUserId() };
                            addUsersToRole(uids, cContainerRole.getName());
                            curRole.setState(User.State.ACTIVE);
                            UserRole userRole = (UserRole) curRole;
                            if (userRole.getUser() == null)
                            {
                                userRole.setUser(UserUtil
                                        .getUserIdByName(userRole.getUserName()));
                            }
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
                        CATEGORY.error("The container role doesn't exist for user role "
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
            throw createUserRoleException(p_user.getUserName(), ex,
                    numOfFailedRoles, failedRoles);
        }

        // if there was atleast one failed role - throw an exception
        // and show error to user
        if (numOfFailedRoles > 0)
        {
            // create an exception since some of the roles failed
            throw createUserRoleException(p_user.getUserName(), null,
                    numOfFailedRoles, failedRoles);
        }
    }

    /**
     * Modifies the user with the roles specified.
     * 
     * @param p_user
     * @param p_newRoleList
     *            - The list of roles the user should be associated with. If an
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
                String[] uids =
                { p_user.getUserId() };

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
                            Role cRole1 = getContainerRole(
                                    oldRole1.getActivity(),
                                    oldRole1.getSourceLocale(),
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
                                removeRole(oldRole1);
                            }
                            else
                            {
                                // log an error - this container role should
                                // always exist
                                // otherwise some things are corrupted or
                                // removed manually
                                CATEGORY.error("The container role doesn't exist for user role "
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
//                                removeRole(unchangedRole);
                                // set to false to add it back with correct
                                // state
                                found = false;
                                HibernateUtil.getSession().evict(unchangedRole);
                            }
                        }
                    }
                    // then this must be new so add it
                    if (!found)
                    {
                        // add to container Role
                        Role cRole2 = getContainerRole(newRole2.getActivity(),
                                newRole2.getSourceLocale(),
                                newRole2.getTargetLocale());
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
                            CATEGORY.error("The container role doesn't exist for user role "
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
                    throw createUserRoleException(p_user.getUserName(), null,
                            numOfFailedRoles, failedRoles);
                }
            }
            catch (UserManagerException ume)
            {
                throw ume;
            }
            catch (Exception ex)
            {
                throw createUserRoleException(p_user.getUserName(), ex,
                        numOfFailedRoles, failedRoles);
            }
        }
    }


    /**
     * Deactiavte the user roles the user is associated with and remove the user
     * from the container roles.
     * 
     * @param p_uids
     *            - The user id to be deactivated.
     * @param dirContext
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */

    private void deactivateUserRoles(String p_uid)
            throws NamingException, UserManagerException, RemoteException
    {
        // Validate the parameters
        if (p_uid == null || p_uid.length() == 0)
        {
            // throw exception
            return;
        }

        UserRoleImpl ur = RoleDatabaseHelper.getUserRoleByUserId(p_uid);
        if (ur != null)
        {
        	ur.setState(User.State.DEACTIVE);
        }
        
        List<ContainerRoleImpl> crs = RoleDatabaseHelper.getContainerRoleByUserId(p_uid);
        for (ContainerRoleImpl cr : crs)
        {
        	RoleDatabaseHelper.removeUserFromContainerRole(cr, p_uid);
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
            CATEGORY.error("UserManagerException is thrown from: "
                    + "UserManagerLocal::associateUserWithProjects() for "
                    + p_userId + " with projects " + p_projectIds.toString(), e);

            String[] args =
            { p_userId, p_projectIds.toString() };
            throw new UserManagerException(
                    UserManagerException.MSG_FAILED_TO_ASSOCIATE_USER_WITH_PROJECTS,
                    args, e);
        }
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
//            addRateToRole(p_rate, cr);
        	cr.addRate(p_rate);
        	HibernateUtil.saveOrUpdate(cr);
        }
        else
        {
            String args[] =
            { p_activity.getName(), p_sourceLocale, p_targetLocale };
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
        	List<Rate> removedRate = new ArrayList<Rate>();
        	Collection rs = cr.getRates();
        	for (Object o : rs)
        	{
        		Rate r = (Rate) o;
        		if (r.getId() == p_rate.getId())
        		{
        			removedRate.add(r);
        		}
        	}
        	
        	rs.removeAll(removedRate);
        	HibernateUtil.saveOrUpdate(cr);
//            removeRateFromRole(p_rate, cr);
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
     * @param p_userId
     *            - the user ID.
     * @param p_sessionId
     *            - the session id of the user.
     */
    public void loggedInUsers(String p_userId, String p_sessionId)
    {
        if (p_sessionId != null)
        {
            m_userMap.put(p_sessionId, p_userId);
            CATEGORY.info("Logging in user: " + p_userId + ". Total users="
                    + m_userMap.size());
        }
    }

    /**
     * Calculates the users after logging out the system.
     * 
     * @param p_userId
     *            - the user ID.
     * @param p_sessionId
     *            - the session id of the user.
     */
    public void loggedOutUsers(String p_userId, String p_sessionId)
    {
        if (p_sessionId != null)
        {
            m_userMap.remove(p_sessionId);
            CATEGORY.info("Logging out user: " + p_userId + ". Total users="
                    + m_userMap.size());
        }
    }

    public Map<String, String> getLoggedInUsers()
    {
        return m_userMap;
    }
}

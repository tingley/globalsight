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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.directory.Attribute;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams;
import com.globalsight.everest.workflow.Activity;

/**
 * UserManagerWLRMIImpl is the remote implementation of UserManagerLocal.
 */
public class UserManagerWLRMIImpl extends RemoteServer implements
        UserManagerWLRemote
{
    private UserManager m_localInstance;

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote UserManager
     * 
     * @param p_localInstance
     *            The local instance of the UserManager.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public UserManagerWLRMIImpl() throws UserManagerException, RemoteException
    {
        super(UserManager.SERVICE_NAME);
        m_localInstance = new UserManagerLocal();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: UserManagerWLRemote Implementation
    // ////////////////////////////////////////////////////////////////////////////
    public Collection getUserRoles(User p_user) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserRoles(p_user);
    }

    public Collection getContainerRoles(User p_user) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getContainerRoles(p_user);
    }

    /**
     * Returns the Collection of all UserRole objects with the given activity
     * name, source locale, and target locale. All of the arguments must be
     * valid.
     * 
     * @param p_activtyName
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     */
    public Collection getUserRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getUserRoles(p_activityName, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * Returns the Collection of all ContainerRole objects with the given
     * activity name, source locale, and target locale. All of the arguments
     * must be valid.
     * 
     * @param p_activtyName
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     */
    public Collection getContainerRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {

        return m_localInstance.getContainerRoles(p_activityName,
                p_sourceLocale, p_targetLocale);
    }

    /**
     * @see UserManager.getContainerRoles(String, String)
     * 
     */
    public Collection getContainerRoles(String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {
        return m_localInstance
                .getContainerRoles(p_sourceLocale, p_targetLocale);
    }

    /**
     * Returns the ContainerRole object with the given activity name, source
     * locale, and target locale.
     * 
     * @param p_activtyName
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
        return m_localInstance.getContainerRole(p_activity, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * @see UserManager.getContainerRole(String, String, String, long)
     */
    public ContainerRole getContainerRole(String p_activityName,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getContainerRole(p_activityName, p_sourceLocale,
                p_targetLocale, p_projectId);
    }

    /**
     * Returns the ContainerRole object with the given activity name, source
     * locale, and target locale where at least one of its users is in the
     * project with the given id.
     * 
     * 
     * @param p_activityName
     *            The name of the activity.
     * @param p_sourceLocale
     *            The results of the Locale.toString() method, for the source
     *            Locale.
     * @param p_targetLocale
     *            The results of the Locale.toString() method, for the target
     *            Locale.
     * @param p_projectId
     *            The id of the project where at least one of the users of the
     *            container role should be part of.
     * 
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public ContainerRole getContainerRole(Activity p_activity,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getContainerRole(p_activity, p_sourceLocale,
                p_targetLocale, p_projectId);
    }

    /**
     * @see UserManager.getContainerRole(Rate)
     */
    public ContainerRole getContainerRole(Rate p_rate, boolean p_withRates)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getContainerRole(p_rate, p_withRates);
    }

    /**
     * @see UserManager.addUser(User, User, List, FieldSecurity, List, List)
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException
    {
        m_localInstance.addUser(p_userRequestingAdd, p_user, p_projectIds,
                p_fieldSecurity, p_roles);
    }

    /**
     * @see UserManager.remove(User, String)
     */
    public void removeUser(User p_userRequestingRemove, String p_uid)
            throws RemoteException, UserManagerException
    {
        m_localInstance.removeUser(p_userRequestingRemove, p_uid);
    }

    public void removeUserFromLDAP(String userId) throws RemoteException,
            UserManagerException
    {
        m_localInstance.removeUserFromLDAP(userId);
    }

    /**
     * @see UserManager.activateUser(String, List)
     */
    public User activateUser(String p_uid, List p_projectIds)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.activateUser(p_uid, p_projectIds);
    }

    /**
     * @see UserManager.deactivateUser(String)
     */
    public User deactivateUser(String p_uid) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.deactivateUser(p_uid);
    }

    /**
     * @see UserManager.modifyUser(User, User, List, FieldSecurity, List, List)
     */
    public void modifyUser(User p_userRequestingMod, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException
    {
        m_localInstance.modifyUser(p_userRequestingMod, p_user, p_projectIds,
                p_fieldSecurity, p_roles);
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
     */
    public User getUser(String p_uid) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUser(p_uid);
    }

    public User getUserByName(String p_userName) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserByName(p_userName);
    }

    /**
     * @see UserManager.getUserInfo(String)
     */
    public UserInfo getUserInfo(String p_userId) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserInfo(p_userId);
    }

    /**
     * @see UserManager.getUserInfos(String[])
     */
    public List getUserInfos(String[] p_userIds) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserInfos(p_userIds);
    }

    /**
     * @see UserManager.getUserInfos(String, String, String)
     */
    public List getUserInfos(String p_activityName, String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException
    {
        return m_localInstance.getUserInfos(p_activityName, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * Get all active users
     * 
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Vector getUsers() throws RemoteException, UserManagerException
    {
        return m_localInstance.getUsers();
    }

    public Vector getUsers(String condition) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUsers(condition);
    }

    /**
     * Get all active users for current company only
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
        return m_localInstance.getUsersForCurrentCompany();
    }

    public Vector<User> getUsersFromCompany(String companyId)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getUsersFromCompany(companyId);
    }

    /**
     * @see UserManager.getUserInfos()
     */
    public Vector getUserInfos() throws RemoteException, UserManagerException
    {
        return m_localInstance.getUserInfos();
    }

    /**
     * @see UserManager.getCompanyNames()
     */
    public String[] getCompanyNames() throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getCompanyNames();
    }

    /**
     * @see UserManager.getEmailInformationForUser(String)
     */
    public EmailInformation getEmailInformationForUser(String p_userId)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getEmailInformationForUser(p_userId);
    }

    /**
     * @see UserManager.getEmailInformationForUsers(String[])
     */
    public List getEmailInformationForUsers(String[] p_userIds)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getEmailInformationForUsers(p_userIds);
    }

    /**
     * Get the user names as an array of strings.
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
        return m_localInstance.getUserNamesFromCurrentAndSuperCompany();
    }

    public String[] getUserNamesFromAllCompanies() throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserNamesFromAllCompanies();
    }

    /**
     * @see UserManager.getVendorlessUsers()
     */
    public Vector getVendorlessUsers() throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getVendorlessUsers();
    }

    /**
     * @see UserManager.getUserNamesByFilter(String, Project)
     */
    public String[] getUserIdsByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getUserIdsByFilter(p_roleName, p_project);
    }

    /**
     * @see UserManager.getUsersByFilter(String, Project)
     */
    public List getUsersByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getUsersByFilter(p_roleName, p_project);
    }

    /**
     * @see UserManager.getUsersFromEmail(String)
     */
    public List getUsersByEmail(String p_email) throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUsersByEmail(p_email);
    }

    /**
     * @see UserManager.getUserIdsFromRoles(String[], Project)
     */
    public String[] getUserIdsFromRoles(String[] p_roleNames, Project p_project)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getUserIdsFromRoles(p_roleNames, p_project);
    }

    /**
     * @see UserManager.getUsers(LDAPAttribute[], LDAPAttribute[], Project)
     */
    public Vector getUsers(UserSearchParams params,
            Project p_project) throws RemoteException, UserManagerException
    {
        return m_localInstance.getUsers(params, p_project);
    }

    /**
     * A factory method returning a new User object
     * 
     * @return a User object
     */
    public User createUser()
    {
        return m_localInstance.createUser();
    }

    public ContainerRole createContainerRole()
    {
        return m_localInstance.createContainerRole();
    }

    public UserRole createUserRole()
    {
        return m_localInstance.createUserRole();
    }

    public void addRole(Role p_role) throws RemoteException,
            UserManagerException
    {
        m_localInstance.addRole(p_role);
    }

    public void removeRole(Role p_roleName) throws RemoteException,
            UserManagerException
    {
        m_localInstance.removeRole(p_roleName);
    }

    public void removeRoleFromLDAP(String p_roleName) throws RemoteException,
            UserManagerException
    {
        m_localInstance.removeRoleFromLDAP(p_roleName);
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
        m_localInstance.removeUsersFromRole(p_uids, p_roleName);
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
        m_localInstance.addUsersToRole(p_uids, p_roleName);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Get the reference to the local implementation of the server.
     * 
     * @return The reference to the local implementation of the server.
     */
    public Object getLocalReference()
    {
        return m_localInstance;
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
        return m_localInstance.getRole(p_roleName);
    }

    public List<Role> getRolesFromCompany(String companyId)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.getRolesFromCompany(companyId);
    }

    /**
     * @see UserManager.addRateToRole(Rate, Activity, String, String)
     */
    public void addRateToRole(Rate p_rate, Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        m_localInstance.addRateToRole(p_rate, p_activity, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * @seeUserManager.removeRateFromRole(Rate, Activity, String, String)
     */
    public void removeRateFromRole(Rate p_rate, Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException
    {
        m_localInstance.removeRateFromRole(p_rate, p_activity, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * @see UserManager.duplicateRateName(String p_name, Role p_role)
     */
    public boolean isDuplicateRateName(String p_name, Role p_role)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.isDuplicateRateName(p_name, p_role);
    }

    /**
     * @see UserManager.getUserInfosInAllProjects
     */
    public List getUserInfosInAllProjects() throws RemoteException,
            UserManagerException
    {
        return m_localInstance.getUserInfosInAllProjects();
    }

    public boolean containsPermissionGroup(String p_uid, String p_permGroupName)
            throws RemoteException, UserManagerException
    {
        return m_localInstance.containsPermissionGroup(p_uid, p_permGroupName);
    }

    /**
     * @see UserManagerLocal.loggedInUsers(String, String).
     */
    public void loggedInUsers(String p_userId, String p_sessionId)
            throws RemoteException, UserManagerException
    {
        m_localInstance.loggedInUsers(p_userId, p_sessionId);
    }

    /**
     * @see UserManagerLocal.loggedOutUsers(String, String).
     */
    public void loggedOutUsers(String p_userId, String p_sessionId)
            throws RemoteException, UserManagerException
    {
        m_localInstance.loggedOutUsers(p_userId, p_sessionId);
    }

    @Override
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles,
            boolean needEncodePwd) throws RemoteException, UserManagerException
    {
        m_localInstance.addUser(p_userRequestingAdd, p_user, p_projectIds,
                p_fieldSecurity, p_roles, needEncodePwd);
    }

    public Map<String, String> getLoggedInUsers()
    {
        return m_localInstance.getLoggedInUsers();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////////////

}

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
import com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams;
import com.globalsight.everest.workflow.Activity;

/**
 * UserManager is an interface used for handling and delegating job related
 * processes.
 */
public interface UserManager
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "UserManagerServer";

    /**
     * Add a new User.
     * 
     * @param p_userRequestingAdd
     *            The user requesting the add (performing the action).
     * @param p_user
     *            The user to be added.
     * @param p_projectIds
     *            The ids of the projects the user is associated with. This can
     *            also be NULL or an empty list if the user isn't associated
     *            with any projects.
     * @param p_fieldSecurity
     *            The field security to be set up on the user's attributes. If
     *            set to NULL then a default will be added with all attributes
     *            SHARED.
     * @param p_roles
     *            The list of the roles the user can perform. If NULL or empty -
     *            don't add any roles to the user.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException;

    /**
     * Add a new User.
     * 
     * @param p_userRequestingAdd
     *            The user requesting the add (performing the action).
     * @param p_user
     *            The user to be added.
     * @param p_projectIds
     *            The ids of the projects the user is associated with. This can
     *            also be NULL or an empty list if the user isn't associated
     *            with any projects.
     * @param p_fieldSecurity
     *            The field security to be set up on the user's attributes. If
     *            set to NULL then a default will be added with all attributes
     *            SHARED.
     * @param p_roles
     *            The list of the roles the user can perform. If NULL or empty -
     *            don't add any roles to the user.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void addUser(User p_userRequestingAdd, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles,
            boolean needEncodePwd) throws RemoteException, UserManagerException;

    /**
     * Modify an existing User.
     * 
     * @param p_userRequestinMod
     *            The user requesting the modification (performing the action).
     * @param p_user
     *            The user to be modified.
     * @param p_projectIds
     *            The ids of the projects the user is associated with. This can
     *            also be NULL if there aren't any changes. Or an empty list if
     *            the user isn't associated with any projects.
     * @param p_fieldSecurity
     *            The field security to be updated on the user's attributes. If
     *            set to NULL then it won't be updated.
     * @param p_roles
     *            The list of the roles the user can perform. If NULL - don't
     *            change the roles the user performs. empty - remove the user
     *            from all roles.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void modifyUser(User p_userRequestingMod, User p_user,
            List p_projectIds, FieldSecurity p_fieldSecurity, List p_roles)
            throws RemoteException, UserManagerException;

    /**
     * Remove an existing user.
     * 
     * @param p_userRequestingRemove
     *            The user requesting the other user to be removed.
     * @param p_uid
     *            The user id of the user to be removed.
     * @exception UserManagerException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception RemoteException
     */
    public void removeUser(User p_userRequestingRemove, String p_uid)
            throws RemoteException, UserManagerException;

    public void removeUserFromLDAP(String userId) throws RemoteException,
            UserManagerException;

    /**
     * Activate an existing user with deactive status.
     * 
     * @param p_uid
     *            - The user id to be activated.
     * @param p_projectIds
     *            - The list of project ids to associate the activated user to.
     *            If NULL or empty then the user isn't associated with any.
     * 
     * @return Return the activated/updated user
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public User activateUser(String p_uid, List p_projectIds)
            throws RemoteException, UserManagerException;

    /**
     * Deactivate an existing user.
     * 
     * @param p_uid
     *            - The user id to be deactivated.
     * 
     * @return Return the deactivated/updated user.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public User deactivateUser(String p_uid) throws RemoteException,
            UserManagerException;

    /**
     * Removes users from a roles.
     * 
     * @param p_uid
     *            - The user ids to be deleted.
     * @param p_role
     *            - The role to remove the users to.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void removeUsersFromRole(String[] p_uids, String p_roleName)
            throws RemoteException, UserManagerException;

    /**
     * Adds a user to a role.
     * 
     * @param p_uid
     *            - The user id to be added.
     * @param p_role
     *            - The role to add the user to.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public void addUsersToRole(String[] p_uids, String p_roleName)
            throws RemoteException, UserManagerException;

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
            UserManagerException;

    public User getUserByName(String p_userName) throws RemoteException,
            UserManagerException;

    /**
     * Return a more light-weight object of User information.
     * 
     * @param p_userId
     *            The id of the user to get information about.
     * 
     * @return a UserInfo object
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public UserInfo getUserInfo(String p_userId) throws RemoteException,
            UserManagerException;

    /**
     * Return a collection of a more light-weight object of User information
     * that correspond to the users ids passed in.
     * 
     * @param p_userIds
     *            The ids of the users to get the information about.
     * 
     * @return A list of UserInfo objects
     */
    public List getUserInfos(String[] p_userIds) throws RemoteException,
            UserManagerException;

    /**
     * Return a list of light-weight objects of user information for the users
     * in the container role specified by the given activity name, source
     * locale, and target locale.
     * 
     * @param p_activityName
     *            The activity name that's part of a container role.
     * @param p_sourceLocale
     *            The source locale as a result of Locale.toString()
     * @param p_targetLocale
     *            The target locale as a result of Locale.toString()
     * 
     * @return A list of UserInfo objects.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public List getUserInfos(String p_activityName, String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException;

    /**
     * Returns all the company names that users are associated with.
     * 
     * @return A list of Company names.
     */
    public String[] getCompanyNames() throws RemoteException,
            UserManagerException;

    /**
     * Get the users's email information.
     * 
     * @param p_userId
     *            The id of the user to get information about.
     * 
     * 
     * @return An EmailInformation object.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public EmailInformation getEmailInformationForUser(String p_userId)
            throws RemoteException, UserManagerException;

    /**
     * Get the users's email information for all the users specified.
     * 
     * @param p_userIds
     *            - The users to retrieve email addresses from.
     * 
     * @return A list of EmailInformation objects.
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public List getEmailInformationForUsers(String[] p_userIds)
            throws RemoteException, UserManagerException;

    /**
     * Get the user names of all ACTIVE users as an array of strings.
     * 
     * @return An array of user names.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getUserNamesFromCurrentAndSuperCompany()
            throws RemoteException, UserManagerException;

    public String[] getUserNamesFromAllCompanies() throws RemoteException,
            UserManagerException;

    /**
     * Get all users that are not a vendor.
     * 
     * @return A list of users.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Vector getVendorlessUsers() throws RemoteException,
            UserManagerException;

    /**
     * Get the user names based on a filter (given role, and the specified
     * project). Any of these can be NULL to not be included in the filter.
     * 
     * @return An array of user names filtered by the specified parameters.
     * 
     * @param p_roleName
     *            - The string representation of the role. If NULL then no
     *            filtering by roles.
     * @param p_project
     *            - The project the users need to be associated with to be
     *            returned (filter). This can be NULL to specify no filtering by
     *            project.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getUserIdsByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException;

    /**
     * Get the user names of the members or the given array of roles. If the
     * project is null, all members of each role will be included in the result.
     * 
     * @return An array of user names filtered by the specified parameters.
     * 
     * @param p_roleNames
     *            - An array of roles where each value is the string
     *            representation of the role.If NULL then the result will be
     *            null.
     * @param p_project
     *            - The project the users need to be associated with to be
     *            returned (filter). This can be NULL to specify no filtering by
     *            project.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public String[] getUserIdsFromRoles(String[] p_roleNames, Project p_project)
            throws RemoteException, UserManagerException;

    /**
     * Get the users based on a filter (given role, and the specified project).
     * Any of these can be NULL to not be included in the filter.
     * 
     * @return A list of users filtered by the specified parameters.
     * 
     * @param p_roleName
     *            - The string representation of the role. If NULL then no
     *            filtering by roles.
     * @param p_project
     *            - The project the users need to be associated with to be
     *            returned (filter). This can be NULL to specify no filtering by
     *            project.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public List getUsersByFilter(String p_roleName, Project p_project)
            throws RemoteException, UserManagerException;

    /**
     * Get users follow the email
     * 
     * @param p_email
     *            the email of users
     */
    public List getUsersByEmail(String p_email) throws UserManagerException,
            RemoteException;

//    /**
//     * Get users matched the specified criteria
//     * 
//     * @param p_userAttrs
//     *            - LDAPArrtibute array contains the User entry attributes
//     * @param p_roleAttrs
//     *            - LDAPArrtibute array contains the Role entry attributes
//     * @param p_project
//     *            - The project to filter the users by. This can be NULL if this
//     *            filtering shouldn't be done.
//     * 
//     * @author Bethany Wang
//     * @return a Vector of User objects
//     * @exception UserManagerException
//     *                - Component related exception.
//     * @exception java.rmi.RemoteException
//     *                Network related exception.
//     */
//    public Vector getUsers(Attribute[] p_userAttrs, Attribute[] p_roleAttrs,
//            Project p_project) throws RemoteException, UserManagerException;

    public Vector getUsers(UserSearchParams p_searchParams,
          Project p_project) throws RemoteException, UserManagerException;
    		
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
            UserManagerException;

    public Vector<User> getUsersFromCompany(String companyId)
            throws RemoteException, UserManagerException;

    /**
     * Get all active users
     * 
     * @return a Vector of User objects
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Vector getUsers() throws RemoteException, UserManagerException;

    public Vector getUsers(String condtion) throws RemoteException,
            UserManagerException;

    /**
     * Get all active user's information.
     * 
     * @return A collection of UserInfo objects, which is information about all
     *         active users.
     */
    public Vector getUserInfos() throws RemoteException, UserManagerException;

    /**
     * A factory method returning a new User object
     * 
     * @return a User object
     */
    public User createUser();

    public ContainerRole createContainerRole();

    public UserRole createUserRole();

    public void addRole(Role p_role) throws RemoteException,
            UserManagerException;

    public void removeRole(Role p_role) throws RemoteException,
            UserManagerException;

    public void removeRoleFromLDAP(String p_roleName) throws RemoteException,
            UserManagerException;

    public Collection getUserRoles(User p_user) throws RemoteException,
            UserManagerException;

    public Collection getContainerRoles(User p_user) throws RemoteException,
            UserManagerException;

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
     */
    public Collection getUserRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException;

    /**
     * Returns the Collection of all ContainerRole objects with the given
     * activity name, source locale, and target locale.
     * 
     * Old deprecation comment: It is supposed to have null or only one
     * ContainerRole object returned. suggest to use method
     * getContainerRole(String p_activityName, String p_sourceLocale, String
     * p_targetLocale)
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
    public Collection getContainerRoles(String p_activityName,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException;

    /**
     * Determin whether a user with specific user id is a super pm.
     */
    public boolean containsPermissionGroup(String p_uid, String p_permGroupName)
            throws RemoteException, UserManagerException;

    /**
     * Returns the container roles that are associated with the source/ target
     * locale pair specified (this does NOT filter by activity.
     * 
     * @param p_rate
     * @param p_sourceLocale
     *            The "toString" version of the source locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @param p_targetLocale
     *            The "toString" version of the target locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @exception UserManagerException
     *                Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    Collection getContainerRoles(String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException;

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
    public ContainerRole getContainerRole(String p_activityName,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws RemoteException, UserManagerException;

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
            throws RemoteException, UserManagerException;

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
            throws RemoteException, UserManagerException;

    /**
     * Returns the ContainerRole that is associated with the specified Rate.
     * 
     * @param p_rate
     *            The rate to find the role it is associated with.
     * @param p_withRates
     *            If set to 'true' then return the role and all rates that are
     *            associated with it (an internal collection in the role. If set
     *            to 'false' do NOT set the collection of rates, just need to
     *            know the role.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    ContainerRole getContainerRole(Rate p_rate, boolean p_withRates)
            throws RemoteException, UserManagerException;

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
            UserManagerException;

    public List<Role> getRolesFromCompany(String companyId)
            throws RemoteException, UserManagerException;

    /**
     * Associates the specified rate with the container role (identified by the
     * activity and locales.
     * 
     * @param p_rate
     *            The rate to associate with a role.
     * @param p_activity
     *            The activity the role is associated with.
     * @param p_sourceLocale
     *            The "toString" version of the source locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @param p_targetLocale
     *            The "toString" version of the target locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    void addRateToRole(Rate p_rate, Activity p_activity, String p_sourceLocale,
            String p_targetLocale) throws RemoteException, UserManagerException;

    /**
     * Removes the associated rate from the role.
     * 
     * @param p_rate
     *            The rate to remove from the role.
     * @param p_sourceLocale
     *            The "toString" version of the source locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @param p_targetLocale
     *            The "toString" version of the target locale the role is
     *            associated with (i.e. en_US, fr_FR)
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    void removeRateFromRole(Rate p_rate, Activity p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws RemoteException, UserManagerException;

    /**
     * Checks to see if the name specified has already been used for a rate
     * associated with the same Container Role (activity name, locale pair).
     * <p>
     * 
     * @param p_name
     *            The name of the rate to check for a duplicate.
     * @param p_role
     *            The role to check in.
     * @return 'true' if the name is a duplicate, 'false' if it isn't.
     */
    public boolean isDuplicateRateName(String p_name, Role p_role)
            throws RemoteException, UserManagerException;

    /**
     * Return the information about each user that is in all projects (current
     * and future).
     */
    public List getUserInfosInAllProjects() throws RemoteException,
            UserManagerException;

    /**
     * @see UserManagerLocal.loggedInUsers(String, String).
     */
    public void loggedInUsers(String p_userId, String p_sessionId)
            throws RemoteException, UserManagerException;

    /**
     * @see UserManagerLocal.loggedOutUsers(String, String).
     */
    public void loggedOutUsers(String p_userId, String p_sessionId)
            throws RemoteException, UserManagerException;

    public Map<String, String> getLoggedInUsers();
}

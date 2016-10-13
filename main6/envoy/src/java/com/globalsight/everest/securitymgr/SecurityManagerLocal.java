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
package com.globalsight.everest.securitymgr;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorInfo;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * SecurityManagerLocal implements SecurityManager and is responsible for
 * managing security information in the system.
 */
public class SecurityManagerLocal implements SecurityManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(SecurityManagerLocal.class.getName());

    private UserManager m_userManager = null;

    // uses a Vector because it is syncronized. As logins and logouts
    // occur of the same login name this is necessary.
    // Contains the user's login name (String) that is logged in.
    // Depending if concurrent duplicate login's are allowed - there may
    // be duplicates within the vector..
    private Vector m_loggedInUsers = new Vector(20);

    // ///////////////////////////////////////////////////////////
    // Begin: Constructor
    // ///////////////////////////////////////////////////////////
    public SecurityManagerLocal() throws SecurityManagerException
    {
        initServer();
    }

    // ///////////////////////////////////////////////////////////
    // End: Constructor
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////
    // Begin: UserManager Implementation
    // ///////////////////////////////////////////////////////////

    public User authenticateUserByName(String p_userName, String p_password)
            throws RemoteException, SecurityManagerException
    {
        return authenticateUser(UserUtil.getUserIdByName(p_userName),
                p_password);
    }

    /**
     * Authenticate the user by the provided credentials and return the user
     * information upon a successful login.
     * 
     * @exception SecurityManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public User authenticateUser(String p_userId, String p_password)
            throws RemoteException, SecurityManagerException
    {
        User loginUser = null;
        String userName = UserUtil.getUserNameById(p_userId);

        // Validate parameter
        if (userName == null || "".equals(userName.trim())
                || p_password == null || "".equals(p_password.trim()))
        {
            String[] messageArgument =
            { "Invalid login info received: " + userName + ", password="
                    + p_password };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                    messageArgument, null);
        }

        // if duplicate concurrent logins aren't allowed and the user
        // is already logged in - login fails
        boolean allowDupLogins = false;
        try
        {
            allowDupLogins = SystemConfiguration
                    .getInstance()
                    .getBooleanParameter(
                            SystemConfigParamNames.CONCURRENT_DUP_LOGIN_ALLOWED);
        }
        catch (GeneralException ge)
        {
            // don't do anything - just leave in strictest state of don't
            // allow dup logins
        }

        // if duplicate logins aren't allowed and the user is not
        // an anonymous user and the user is already logged in
        // do not allow login - throw exception
        if (!allowDupLogins && m_loggedInUsers.contains(userName)
                && !userName.equals(User.ANONYMOUS_VV_USER_ID))
        {
            String[] args =
            { userName };
            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_CONCURRENT_LOGIN, args,
                    null);
        }

        // pass user info back
        try
        {
            loginUser = m_userManager.getUser(p_userId);

            // Validate parameter
            if (loginUser == null)
            {
                String[] messageArgument =
                { "Invalid login info received: " + userName + ", password="
                        + p_password };

                throw new SecurityManagerException(
                        SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                        messageArgument, null);
            }

            UserLdapHelper.authenticate(p_password, loginUser.getPassword());
        }
        catch (NamingException ex)
        {
            String[] messageArgument =
            { "Error for authenticating user " + userName };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                    messageArgument, ex);
        }
        catch (GeneralException e)
        {
            String[] messageArgument =
            { "UserManager failed to get info for user " + userName };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                    messageArgument, e);
        }

        // check case of user Id.
        // It turns out that LDAP authentication is case insensitive.
        // But userid must be case-sensitive in this system.
        if (!userName.equalsIgnoreCase(loginUser.getUserName()))
        {
            String[] messageArgument =
            { "Case of user name " + userName + " is not correct" };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                    messageArgument, null);
        }

        // check user status
        if (!loginUser.isActive())
        {
            String[] messageArgument =
            { "User " + userName + " is not an active user" };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_AUTHENTICATE,
                    messageArgument, null);
        }
        // made it all the way to here so the user is authenticated
        // add to list of logged in users
        m_loggedInUsers.add(userName);
        return loginUser;
    }

    /**
     * @see SecurityManager.logUserOut(String)
     */
    public void logUserOut(String p_userName) throws RemoteException,
            SecurityManagerException
    {
        m_loggedInUsers.remove(p_userName);
    }

    /**
     * @see SecurityManager.getFieldSecurity(User, Object, boolean)
     */
    public FieldSecurity getFieldSecurity(User p_requestingUser,
            Object p_objectWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException
    {
        if (p_objectWithFields instanceof Vendor)
        {
            Vendor v = (Vendor) p_objectWithFields;
            return vendorFieldSecurities(p_requestingUser, v.getId(),
                    v.getProjects(), p_checkProjects, true);
        }
        else if (p_objectWithFields instanceof VendorInfo)
        {
            VendorInfo v = (VendorInfo) p_objectWithFields;
            List projects = null;
            try
            {

                projects = ServerProxy.getProjectHandler().getProjectsByVendor(
                        v.getId());
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "Failed to get the projects for vendor " + v.getId()
                                + " to get the field security.", e);
                String args[] =
                { Long.toString(v.getId()) };
                throw new SecurityManagerException(
                        SecurityManagerException.MSG_FAILED_TO_GET_VENDOR_FIELD_SECURITY,
                        args, e);

            }

            return vendorFieldSecurities(p_requestingUser, v.getId(), projects,
                    p_checkProjects, false);
        }
        else if (p_objectWithFields instanceof User)
        {
            User u = (User) p_objectWithFields;
            // if projects aren't supposed to be checked OR
            // if they aren't in atleast one common project
            // then get the field security
            if (!p_checkProjects
                    || (p_checkProjects && !inSameProject(p_requestingUser, u)))
            {
                try
                {
                    String hql = "from UserFieldSecurity u where u.username = ?";
                    UserFieldSecurity security = (UserFieldSecurity) HibernateUtil
                            .getFirst(hql, u.getUserId());

                    // none found will pass back a default one
                    return security == null ? new UserFieldSecurity()
                            : security;
                }
                catch (Exception e)
                {
                    CATEGORY.error("Failed to get the field security for user "
                            + u.getUserName(), e);
                    String[] errorArgs =
                    { u.getUserName() };
                    throw new SecurityManagerException(
                            SecurityManagerException.MSG_FAILED_TO_GET_USER_FIELD_SECURITY,
                            errorArgs, e);
                }
            }
            else
            // they are in one project the same so no security on the fields
            {
                // create a new one with all fields marked as SHARED
                // the default one has all fields marked as SHARED except for
                // the security fields - so change that to SHARED
                // since they are in the same project this user can access the
                // security fields
                UserFieldSecurity securities = new UserFieldSecurity();
                securities.put(UserFieldSecurity.SECURITY,
                        UserFieldSecurity.SHARED);
                return securities;
            }
        }
        else
        {
            return new FieldSecurity();
        }
    }

    /**
     * @see SecurityManager.getFieldSecurities(User, List, boolean)
     */
    public List getFieldSecurities(User p_requestingUser,
            List p_objectsWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException
    {
        int size = p_objectsWithFields == null ? 0 : p_objectsWithFields.size();

        List l = new ArrayList(size);
        for (int i = 0; i < size; i++)
        {
            FieldSecurity fs = getFieldSecurity(p_requestingUser,
                    p_objectsWithFields.get(i), p_checkProjects);
            l.add(fs);
        }
        return l;
    }

    /**
     * @see SecurityManager.setFieldSecurity(User, Object, FieldSecurity)
     */
    public void setFieldSecurity(User p_requestingUser,
            Object p_objectWithFields, FieldSecurity p_fs)
            throws RemoteException, SecurityManagerException
    {
        // save the field securities out
        if (p_objectWithFields instanceof Vendor)
        {
            Vendor v = (Vendor) p_objectWithFields;
            VendorFieldSecurity vfs = (VendorFieldSecurity) p_fs;

            VendorFieldSecurity oldFs = (VendorFieldSecurity) getFieldSecurity(
                    p_requestingUser, p_objectWithFields, false);
            try
            {
                // if an existing one
                if (oldFs.getId() > 0)
                {
                    // update the clone with the latest field security
                    oldFs.setFieldSecurity(vfs.toString());
                    HibernateUtil.saveOrUpdate(oldFs);
                }
                else
                {
                    vfs.setVendorId(v.getId());
                    HibernateUtil.save(vfs);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to save the field security for vendor "
                        + v.getId(), e);
                String args[] =
                { Long.toString(v.getId()) };
                throw new SecurityManagerException(
                        SecurityManagerException.MSG_FAILED_TO_SAVE_VENDOR_FIELD_SECURITY,
                        args, e);
            }
        }
        else if (p_objectWithFields instanceof User)
        {

            User u = (User) p_objectWithFields;
            UserFieldSecurity ufs = (UserFieldSecurity) p_fs;

            UserFieldSecurity oldFs = (UserFieldSecurity) getFieldSecurity(
                    p_requestingUser, p_objectWithFields, false);
            try
            {
                // if an existing one
                if (oldFs.getId() > 0)
                {
                    // update the clone with the latest field security
                    oldFs.setFieldSecurity(ufs.toString());
                    oldFs.setUsername(u.getUserId());
                    HibernateUtil.saveOrUpdate(oldFs);
                }
                else
                {
                    ufs.setUsername(u.getUserId());
                    HibernateUtil.save(ufs);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to save the field security for user "
                        + u.getUserName(), e);
                String args[] =
                { u.getUserName() };
                throw new SecurityManagerException(
                        SecurityManagerException.MSG_FAILED_TO_SAVE_USER_FIELD_SECURITY,
                        args, e);
            }
        }
        else
        {
            CATEGORY.error("Trying to set the field security on an object that isn't recognized.  Object: "
                    + p_objectWithFields.toString()
                    + " with "
                    + p_fs.toString());
        }
    }

    /**
     * @see SecurityManager.removeFieldSecurity(User, Object)
     */
    public void removeFieldSecurity(User p_requestingUser,
            Object p_objectWithFields) throws RemoteException,
            SecurityManagerException
    {
        FieldSecurity fs = getFieldSecurity(p_requestingUser,
                p_objectWithFields, false);
        try
        {
            if (fs.getId() > 0)
            {
                HibernateUtil.delete(fs);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to remove the field security for object "
                    + p_objectWithFields.toString());
            String args[] =
            { p_objectWithFields.toString() };
            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_REMOVE_FIELD_SECURITY,
                    args, e);
        }
    }

    // ///////////////////////////////////////////////////////////
    // End: SecurityManager Implementation
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ///////////////////////////////////////////////////////////

    private void initServer() throws SecurityManagerException
    {
        try
        {
            m_userManager = ServerProxy.getUserManager();
        }
        catch (GeneralException ge)
        {
            String[] messageArgument =
            { "Couldn't find UserManager!" };
            CATEGORY.error("SecurityManagerException is thrown from: "
                    + "SecurityManagerException::initServer(): "
                    + messageArgument[0], ge);
            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_TO_INIT_SERVER, null,
                    ge);
        }
    }

    /**
     * Return 'true' if the user manages or is in at least one of the same
     * projects that the vendor is in.
     */
    private boolean inSameProject(User p_requestingUser, long p_vendorId,
            List p_vendorProjects) throws SecurityManagerException
    {
        boolean found = false;
        try
        {
            // if the user is an adminstrator then just return true
            Collection permGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupNamesForUser(
                            p_requestingUser.getUserId());

            if (permGroups.contains(Permission.GROUP_ADMINISTRATOR)
                    || permGroups.contains(Permission.GROUP_VENDOR_ADMIN))
            {
                found = true;
            }
            else
            {
                List uProjects = ServerProxy.getProjectHandler()
                        .getProjectsManagedByUser(p_requestingUser,
                                Permission.GROUP_MODULE_VENDOR_MANAGER);
                // if there are projecs to check against
                if (uProjects != null && uProjects.size() > 0)
                {
                    for (Iterator vpi = p_vendorProjects.iterator(); vpi
                            .hasNext() && !found;)
                    {
                        Project p = (Project) vpi.next();
                        if (uProjects.contains(p))
                        {
                            found = true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to verify if the user "
                            + p_requestingUser.getUserName()
                            + " is in the same project as vendor " + p_vendorId,
                    e);
            String[] args =
            { p_requestingUser.getUserName(), Long.toString(p_vendorId) };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_VENDOR_PROJECTS_VERIFY,
                    args, e);
        }
        return found;
    }

    /**
     * Return 'true' if the user (p_requestingUserId) manages or is in at least
     * one of the same projects that the user (p_user) is in.
     */
    private boolean inSameProject(User p_requestingUser, User p_user)
            throws SecurityManagerException
    {
        boolean found = false;
        try
        {
            Collection permGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupNamesForUser(
                            p_requestingUser.getUserId());
            // if the user is an adminstrator then just return true
            if (permGroups.contains(Permission.GROUP_ADMINISTRATOR))
            {
                found = true;
            }
            // if you are the same person you are in the same project
            else if (p_requestingUser.getUserId().equals(p_user.getUserId()))
            {
                found = true;
            }
            else
            {
                List u1Projects = ServerProxy.getProjectHandler()
                        .getProjectsManagedByUser(p_requestingUser,
                                Permission.GROUP_MODULE_GLOBALSIGHT);
                List u2Projects = ServerProxy.getProjectHandler()
                        .getProjectsByUser(p_user.getUserId());
                for (Iterator u2pi = u2Projects.iterator(); u2pi.hasNext()
                        && !found;)
                {
                    Project p = (Project) u2pi.next();
                    if (u1Projects.contains(p))
                    {
                        found = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to verify if the user "
                            + p_requestingUser.getUserName()
                            + " is in the same project as user "
                            + p_user.getUserName(), e);
            String[] args =
            { p_requestingUser.getUserName(), p_user.getUserName() };

            throw new SecurityManagerException(
                    SecurityManagerException.MSG_FAILED_USER_PROJECTS_VERIFY,
                    args, e);
        }
        return found;
    }

    /*
     * Get the vendor field security based on the given info of a Vendor or
     * VendorInfo.
     */
    private VendorFieldSecurity vendorFieldSecurities(User p_requestingUser,
            long p_vendorId, List p_projects, boolean p_checkProjects,
            boolean p_clone) throws SecurityManagerException
    {
        // if don't check projects OF
        // if they aren't in atleast one common project
        // get the field security
        if (!p_checkProjects
                || (p_checkProjects && !inSameProject(p_requestingUser,
                        p_vendorId, p_projects)))
        {
            try
            {
                String hql = "from VendorFieldSecurity v where v.vendorId = ?";
                VendorFieldSecurity vfs = (VendorFieldSecurity) HibernateUtil
                        .getFirst(hql, Long.toString(p_vendorId));
                return vfs == null ? new VendorFieldSecurity() : vfs;
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "Failed to get the vendor field security for vendor "
                                + p_vendorId, e);
                String[] errorArgs =
                { Long.toString(p_vendorId) };
                throw new SecurityManagerException(
                        SecurityManagerException.MSG_FAILED_TO_GET_VENDOR_FIELD_SECURITY,
                        errorArgs, e);
            }
        }
        else
        // they are in one project the same so no security on the fields
        {
            // create a new one with all fields marked as SHARED
            // the default one has all fields marked as SHARED except for
            // the security fields - so change that to SHARED
            // since they are in the same project this user can access the
            // security fields
            VendorFieldSecurity securities = new VendorFieldSecurity();
            securities.put(VendorFieldSecurity.SECURITY,
                    VendorFieldSecurity.SHARED);
            return securities;
        }
    }

    // ///////////////////////////////////////////////////////////
    // End: Local Methods
    // ///////////////////////////////////////////////////////////
}

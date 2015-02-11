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
import java.util.List;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.system.RemoteServer;

/**
 * SecurityManagerWLRMIImpl is the remote implementation of
 * SecurityManagerLocal.
 */
public class SecurityManagerWLRMIImpl extends RemoteServer implements
        SecurityManagerWLRemote
{
    // the local instance of the job handler
    private SecurityManager m_localInstance;

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote Security Manager.
     * 
     * @param p_localInstance
     *            The local instance of the UserManager.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public SecurityManagerWLRMIImpl() throws RemoteException,
            SecurityManagerException
    {
        super(SecurityManager.SERVICE_NAME);
        m_localInstance = new SecurityManagerLocal();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: UserManagerWLRemote Implementation
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Authenticate the user by the provided credentials and return the user
     * information upon a successful login.
     * 
     * @exception UserManagerException
     *                - Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public User authenticateUser(String p_userId, String p_password)
            throws RemoteException, SecurityManagerException
    {
        return m_localInstance.authenticateUser(p_userId, p_password);
    }

    public User authenticateUserByName(String p_userName, String p_password)
            throws RemoteException, SecurityManagerException
    {
        return m_localInstance.authenticateUserByName(p_userName, p_password);
    }

    /**
     * 
     * @see SecurityManager.logUserOut(String)
     * 
     */
    public void logUserOut(String p_userName) throws RemoteException,
            SecurityManagerException
    {
        m_localInstance.logUserOut(p_userName);
    }

    /**
     * @see SecurityManager.getFieldSecurity(User, Object, boolean)
     */
    public FieldSecurity getFieldSecurity(User p_requestingUser,
            Object p_objectWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException
    {
        return m_localInstance.getFieldSecurity(p_requestingUser,
                p_objectWithFields, p_checkProjects);
    }

    /**
     * @see SecurityManager.getFieldSecurities(User, List, boolean)
     */
    public List getFieldSecurities(User p_requestingUser,
            List p_objectsWithFields, boolean p_checkProjects)
            throws RemoteException, SecurityManagerException
    {
        return m_localInstance.getFieldSecurities(p_requestingUser,
                p_objectsWithFields, p_checkProjects);
    }

    /**
     * @see SecurityManager.setFieldSecurity(User, Object, FieldSecurity)
     */
    public void setFieldSecurity(User p_requestingUser,
            Object p_objectWithFields, FieldSecurity p_fs)
            throws RemoteException, SecurityManagerException
    {
        m_localInstance.setFieldSecurity(p_requestingUser, p_objectWithFields,
                p_fs);
    }

    /**
     * @see SecurityManager.removeFieldSecurity(User, Object)
     */
    public void removeFieldSecurity(User p_requestingUser,
            Object p_objectWithFields) throws RemoteException,
            SecurityManagerException
    {
        m_localInstance.removeFieldSecurity(p_requestingUser,
                p_objectWithFields);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: UserManagerWLRemote Implementation
    // ////////////////////////////////////////////////////////////////////////////

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

    // ////////////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////////////
}

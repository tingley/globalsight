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
package com.globalsight.everest.permission;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import com.globalsight.everest.util.system.RemoteServer;

public class PermissionManagerWLRMIImpl extends RemoteServer implements
        PermissionManagerWLRemote
{
    PermissionManager m_localReference;

    public PermissionManagerWLRMIImpl() throws java.rmi.RemoteException,
            PermissionException
    {
        super(PermissionManager.SERVICE_NAME);
        m_localReference = new PermissionManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public PermissionGroup createPermissionGroup(PermissionGroup param1)
            throws PermissionException, RemoteException
    {
        return m_localReference.createPermissionGroup(param1);
    }

    public void deletePermissionGroup(PermissionGroup param1)
            throws PermissionException, RemoteException
    {
        m_localReference.deletePermissionGroup(param1);
    }

    public Collection getAllPermissionGroups() throws PermissionException,
            RemoteException
    {
        return m_localReference.getAllPermissionGroups();
    }

    public PermissionGroup readPermissionGroup(long param1)
            throws PermissionException, RemoteException
    {
        return m_localReference.readPermissionGroup(param1);
    }

    public PermissionGroup updatePermissionGroup(PermissionGroup param1)
            throws PermissionException, RemoteException
    {
        return m_localReference.updatePermissionGroup(param1);
    }

    public PermissionSet getPermissionSetForUser(String p_userId)
            throws PermissionException, RemoteException
    {
        return m_localReference.getPermissionSetForUser(p_userId);
    }

    public Collection getAllPermissionGroupsForUser(String p_u)
            throws PermissionException, RemoteException
    {
        return m_localReference.getAllPermissionGroupsForUser(p_u);
    }

    /**
     * @deprecated
     */
    public Collection getAllPermissionGroupNamesForUser(String p_u)
            throws PermissionException, RemoteException
    {
        return m_localReference.getAllPermissionGroupNamesForUser(p_u);
    }

    public Collection getAllUsersForPermissionGroup(long p_id)
            throws PermissionException, RemoteException
    {
		return m_localReference.getAllUsersForPermissionGroup(p_id);
	}

	public Collection getAllUsersForPermissionGroup(String p_name)
			throws PermissionException, RemoteException {
		return m_localReference.getAllUsersForPermissionGroup(p_name);
	}

	public Collection getAllPermissionGroupsByCompanyId(String p_companyId)
			throws PermissionException, RemoteException
    {
        return m_localReference.getAllPermissionGroupsByCompanyId(p_companyId);
    }

    public Collection getAllUsersWithPermission(String p_permission)
            throws PermissionException, RemoteException
    {
        return m_localReference.getAllUsersWithPermission(p_permission);
    }

    public void mapUsersToPermissionGroup(List p_users,
            PermissionGroup p_permGroup) throws PermissionException,
            RemoteException
    {
        m_localReference.mapUsersToPermissionGroup(p_users, p_permGroup);
    }

    public void unMapUsersFromPermissionGroup(List p_users,
            PermissionGroup p_permGroup) throws PermissionException,
            RemoteException
    {
        m_localReference.unMapUsersFromPermissionGroup(p_users, p_permGroup);
    }
    public Collection getPermissionGroupsBycondition(String condition)
            throws PermissionException, RemoteException{
        return m_localReference.getPermissionGroupsBycondition(condition);
        
    };
    
    public  Collection<Object[]> getAlltableNameForUser(String tableName)
            throws PermissionException, RemoteException{
        return m_localReference.getAlltableNameForUser(tableName);
        
    };
}

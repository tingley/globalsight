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
package com.globalsight.cxe.persistence.cms.teamsite.server;

import java.util.Collection;
import java.rmi.RemoteException;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl;

import com.globalsight.everest.util.system.RemoteServer;

public class TeamSiteServerPersistenceManagerWLRMIImpl 
    extends RemoteServer implements TeamSiteServerPersistenceManagerWLRemote
{
    TeamSiteServerPersistenceManager m_localReference;

    public TeamSiteServerPersistenceManagerWLRMIImpl() 
        throws RemoteException, TeamSiteServerEntityException
    {
        super(TeamSiteServerPersistenceManager.SERVICE_NAME);
        m_localReference = new TeamSiteServerPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: TeamSite Servers  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public  TeamSiteServer createTeamSiteServer(TeamSiteServer param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        return m_localReference.createTeamSiteServer(param1);
    }

    public  void deleteTeamSiteServer(TeamSiteServer param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        m_localReference.deleteTeamSiteServer(param1);
    }

    public Collection getAllTeamSiteServers() 
    throws TeamSiteServerEntityException, RemoteException
    {
        return m_localReference.getAllTeamSiteServers();
    }

    public  TeamSiteServer readTeamSiteServer(long param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        return m_localReference.readTeamSiteServer(param1);
    }

    public  void updateTeamSiteServer(TeamSiteServer param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        m_localReference.updateTeamSiteServer(param1);
    }

    /**
     * Return the TeamSite Server id for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the TeamSite Server.
     * @return the TeamSite Server id for the given name.
     */
    public long getTeamSiteServerIdByName(String p_name)
        throws TeamSiteServerEntityException, RemoteException
    {
        return m_localReference.getTeamSiteServerIdByName(p_name);
    }

    /**
     * Return the TeamSite Server for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the TeamSite Server.
     * @return the TeamSite Server for the given name.
     */
    public TeamSiteServer getTeamSiteServerByName(String p_name)
        throws TeamSiteServerEntityException, RemoteException
    {
        return m_localReference.getTeamSiteServerByName(p_name);
    }


    
    //////////////////////////////////////////////////////////////////////////////
    //  END: TeamSite Servers  //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: Backing Stores  //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public BackingStore createBackingStore(BackingStore param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        return m_localReference.createBackingStore(param1);
    }

    public  void deleteBackingStore(BackingStore param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        m_localReference.deleteBackingStore(param1);
    }

    public Collection getAllBackingStores() 
    throws TeamSiteServerEntityException, RemoteException
    {
        return m_localReference.getAllBackingStores();
    }

    public  BackingStore readBackingStore(long param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        return m_localReference.readBackingStore(param1);
    }

    public BackingStore updateBackingStore(BackingStore param1) 
    throws TeamSiteServerEntityException,RemoteException
    {
        return m_localReference.updateBackingStore(param1);
    }

    public java.util.Collection getBackingStoresByTeamSiteServer(TeamSiteServer param1) 
    throws TeamSiteServerEntityException, RemoteException
    {
        return m_localReference.getBackingStoresByTeamSiteServer(param1);
    }
    //////////////////////////////////////////////////////////////////////////////
    //  END: Backing Stores  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
}

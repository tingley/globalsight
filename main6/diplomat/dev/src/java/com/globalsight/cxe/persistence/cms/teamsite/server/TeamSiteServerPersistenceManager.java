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

public interface TeamSiteServerPersistenceManager
{
    public static final String SERVICE_NAME = "TeamSiteServerPersistenceManager";

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: TeamSite Servers  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
    ** Creates a new TeamSiteServer object in the data store
    ** @return the newly created object
    **/
    public TeamSiteServer createTeamSiteServer(TeamSiteServer p_teamSiteServer)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Reads the TeamSiteServer object from the datastore
    ** @return TeamSiteServer with the given id
    **/
    public TeamSiteServer readTeamSiteServer(long p_id)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Deletes a TeamSiteServer object from the datastore
    **/
    public void deleteTeamSiteServer(TeamSiteServer p_teamSiteServer)
    throws TeamSiteServerEntityException, RemoteException;


    /**
    ** Update the TeamSiteServer object in the datastore
    **/
    public void updateTeamSiteServer(TeamSiteServer p_teamSiteServer)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Get a list of all existing TeamSiteServer objects in the datastore
    ** @return a vector of the TeamSiteServer objects
    **/
    public Collection getAllTeamSiteServers()
    throws TeamSiteServerEntityException, RemoteException;

    /**
     * Return the TeamSite Server id for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the TeamSite Server.
     * @return the TeamSite Server id for the given name.
     */
    public long getTeamSiteServerIdByName(String p_name)
        throws TeamSiteServerEntityException, RemoteException;

    /**
     * Return the TeamSite Server for the given name from the database
     * The returned object is in a state that does not allow editing.
     *
     * @param p_name - The name of the TeamSite Server.
     * @return the TeamSite Server for the given name.
     */
    public TeamSiteServer getTeamSiteServerByName(String p_name)
        throws TeamSiteServerEntityException, RemoteException;


    //////////////////////////////////////////////////////////////////////////////
    //  END: TeamSite Servers  //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: Backing Stores  //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
    ** Creates a new BackingStore object in the data store
    ** @return the created object
    **/
    public BackingStore createBackingStore(BackingStore p_BackingStore)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Reads the BackingStore object from the datastore
    ** @return the BackingStore
    **/
    public BackingStore readBackingStore(long p_id)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Deletes a BackingStore from the datastore
    **/
    public void deleteBackingStore(BackingStore p_BackingStore)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Update the BackingStore object in the datastore
    ** @return the updated object
    **/
    public BackingStore updateBackingStore(BackingStore p_BackingStore)
    throws TeamSiteServerEntityException, RemoteException;

    /**
    ** Get a list of all existing BackingStore objects in the datastore
    ** @return a vector of the BackingStore objects
    **/
    public Collection getAllBackingStores()
    throws TeamSiteServerEntityException, RemoteException;

    /**
     ** Get a list of BackingStore objects in the datastore associated with
     ** the TeamSiteServer object.
     ** @param p_teamSiteServer The TeamSiteServer object.
     ** @return a vector of BackingStore objects.
     **/
    public Collection getBackingStoresByTeamSiteServer(TeamSiteServer p_teamSiteServer)
    throws TeamSiteServerEntityException, RemoteException;
    //////////////////////////////////////////////////////////////////////////////
    //  END: Backing Stores  ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

}


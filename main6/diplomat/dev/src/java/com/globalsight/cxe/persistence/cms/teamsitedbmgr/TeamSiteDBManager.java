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
package com.globalsight.cxe.persistence.cms.teamsitedbmgr;

import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;

import java.util.Collection;

import java.rmi.RemoteException;

import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;

/**
 * TeamSiteDBManager is an interface used for managing TeamSite database activity.
 */
public interface TeamSiteDBManager
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "TeamSiteDBManager";

    /**
     * Create a new branch.
     * <p>
     * @param p_branch - The branch to be created.
     * @exception java.rmi.RemoteException Network related exception.
     * @exception DBException Component related exception.
     */
    public void createBranch(TeamSiteBranch p_branch) throws RemoteException, DBException;

    /**
     * Get a list of all existing branches in the database.
     * <p>
     * @exception java.rmi.RemoteException Network related exception.
     * @exception DBException Component related exception.
     */
    public Collection getAllBranches() throws RemoteException, DBException;

    /**
     * Remove an existing branch.
     * <p>
     * @param p_branch - The branch to be removed.
     * @exception jara.rmi.RemoteException Network related exception.
     * @exception DBException Component related exception.
     */
    public void removeBranch(TeamSiteBranch p_branch) throws RemoteException, DBException;
}

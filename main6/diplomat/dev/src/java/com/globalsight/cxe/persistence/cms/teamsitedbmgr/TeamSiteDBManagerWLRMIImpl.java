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

import java.util.Collection;

import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;
import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;


public class TeamSiteDBManagerWLRMIImpl 
    extends RemoteServer 
    implements TeamSiteDBManagerWLRemote
{
    TeamSiteDBManager m_localReference;

    public TeamSiteDBManagerWLRMIImpl() throws RemoteException, DBException
    {
        super(TeamSiteDBManager.SERVICE_NAME);
        m_localReference = new TeamSiteDBManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public void createBranch(TeamSiteBranch param1) throws RemoteException, DBException
    {
        m_localReference.createBranch(param1);
    }

    public Collection getAllBranches() throws RemoteException, DBException
    {
        return m_localReference.getAllBranches();
    }

    public void removeBranch(TeamSiteBranch param1) throws RemoteException, DBException
    {
        m_localReference.removeBranch(param1);
    }

}

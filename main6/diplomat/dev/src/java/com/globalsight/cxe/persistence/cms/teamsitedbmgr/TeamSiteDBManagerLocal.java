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

import java.rmi.RemoteException;
import java.util.Collection;

import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * TeamSiteDBManagerLocal implements TeamSiteDBManager and is responsible for
 * managing TeamSite database activity.
 */
public class TeamSiteDBManagerLocal implements TeamSiteDBManager
{
    /**
     * Create a new branch.
     * <p>
     * 
     * @param p_branch -
     *            The branch to be created.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception DBException
     *                Component related exception.
     */
    public void createBranch(TeamSiteBranch p_branch) throws RemoteException,
            DBException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_branch);
        }
        catch (Exception e)
        {
            throw new DBException(DBException.EX_BRANCH_ALREADY_EXISTS,
                    DBException.MSG_FAILED_TO_CREATE_BRANCH, e);
        }
    }

    /**
     * Get a list of all existing branches in the database; make them editable.
     * <p>
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception DBException
     *                Component related exception.
     */
    public Collection getAllBranches() throws RemoteException, DBException
    {
        try
        {
            return HibernateUtil.search("from TeamSiteBranch");
        }
        catch (Exception e)
        {
            throw new DBException(DBException.EX_PERSISTENCE,
                    DBException.MSG_FAILED_TO_GET_ALL_BRANCHES, e);
        }
    }

    /**
     * Remove an existing branch.
     * <p>
     * 
     * @param p_branch -
     *            The branch to be removed.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception DBException
     *                Component related exception.
     */
    public void removeBranch(TeamSiteBranch p_branch) throws RemoteException,
            DBException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_branch);
        }
        catch (Exception e)
        {
            throw new DBException(DBException.EX_PERSISTENCE,
                    DBException.MSG_FAILED_TO_REMOVE_BRANCH, e);
        }
    }
}

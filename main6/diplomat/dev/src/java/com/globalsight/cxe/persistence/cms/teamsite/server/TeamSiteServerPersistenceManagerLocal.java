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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TeamSiteServerPersistenceManagerLocal implements
        TeamSiteServerPersistenceManager
{
    /**
     * Default constructor
     */
    public TeamSiteServerPersistenceManagerLocal()
            throws TeamSiteServerEntityException
    {
        super();
    }

    //
    // IMPLEMENTATION OF TEAMSITESERVERS FUNCTIONALITY
    //
    /**
     * Creates a new TeamSiteServer object in the data store
     * 
     * @return the newly created object
     */
    public TeamSiteServer createTeamSiteServer(TeamSiteServer p_teamSiteServer)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            // if this is not a duplicate name - add it
            if (!isTeamSiteServerNameDuplicate(p_teamSiteServer))
            {
                HibernateUtil.save(p_teamSiteServer);
                return readTeamSiteServer(p_teamSiteServer.getId());
            }

            // it is a duplicate
            String errorArgs[] =
            { p_teamSiteServer.getName() };
            throw new TeamSiteServerEntityException(
                    TeamSiteServerEntityException.MSG_TEAMSITE_SERVER_ALREADY_EXISTS,
                    errorArgs, null);

        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
    }

    /**
     * Return the TeamSite Server with the given id from the database The
     * returned object is in a state that allows editing.
     * 
     * @return the TeamSite Server with the given id
     */
    public TeamSiteServer readTeamSiteServer(long p_id)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            return (TeamSiteServerImpl) HibernateUtil.get(
                    TeamSiteServerImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
    }

    /**
     * Delete the given TeamSite Server from the database.
     */
    public void deleteTeamSiteServer(TeamSiteServer p_teamSiteServer)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            // get a read only copy of the TeamSite Server
            // the one being passed in is a clone.
            TeamSiteServerImpl fp = (TeamSiteServerImpl) HibernateUtil.get(
                    TeamSiteServerImpl.class,
                    new Long(p_teamSiteServer.getId()));
            // delete the object
            HibernateUtil.delete(fp);
        }
        catch (Exception pe)
        {
            throw new TeamSiteServerEntityException(pe);
        }
    }

    /**
     * Update the given TeamSite Server in the database. It is assumed that the
     * given object is a valid clone (i.e. it was obtained from the database in
     * an EDITABLE state).
     * <p>
     */
    public void updateTeamSiteServer(TeamSiteServer p_teamSiteServer)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update(p_teamSiteServer);
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
    }

    /**
     * Return all available TeamSite Servers from the database. These are
     * returned as EDITABLE objects.
     * 
     * @return a collection of TeamSite Servers
     */
    public Collection getAllTeamSiteServers()
            throws TeamSiteServerEntityException, RemoteException
    {
        String hql = "from TeamSiteServerImpl t ";
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Map map = null;
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
        {
            hql += "where t.companyId = :companyId ";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentCompanyId));
        }

        return HibernateUtil.search(hql, map);
    }

    /**
     * Return the TeamSite Server id for the given name from the database The
     * returned object is in a state that does not allow editing.
     * 
     * @param p_name
     *            - The name of the TeamSite Server.
     * @return the TeamSite Server id for the given name.
     */
    public long getTeamSiteServerIdByName(String p_name)
            throws TeamSiteServerEntityException, RemoteException
    {
        TeamSiteServer teamSite = getTeamSiteServerByName(p_name);
        return teamSite == null ? -1 : teamSite.getId();
    }

    /**
     * Return the TeamSite Server with the given name from the database The
     * returned object is in a state that does not allow editing.
     * 
     * @return the TeamSite Server with the given name
     */
    public TeamSiteServer getTeamSiteServerByName(String p_name)
            throws TeamSiteServerEntityException, RemoteException
    {
        String hql = "from TeamSiteServerImpl t where t.name = :name ";
        Map map = new HashMap();
        map.put("name", p_name);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
        {
            hql += "and t.companyId = :companyId ";
            map.put("companyId", Long.parseLong(currentCompanyId));
        }

        List result = HibernateUtil.search(hql, map);

        TeamSiteServerImpl teamSite = null;
        if (result != null && result.size() > 0)
        {
            teamSite = (TeamSiteServerImpl) result.get(0);
        }

        return teamSite;
    }

    //
    // IMPLEMENTATION OF BACKINGSTORES FUNCTIONALITY
    //
    /**
     * Creates a new BackingStore object in the data store
     * 
     * @return the newly created object
     */
    public BackingStore createBackingStore(BackingStore p_BackingStore)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            HibernateUtil.save(p_BackingStore);
            return readBackingStore(p_BackingStore.getId());
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
    }

    /**
     * Return the TeamSite Server with the given id from the database The
     * returned object is in a state that allows editing.
     * 
     * @return the TeamSite Server with the given id
     */
    public BackingStore readBackingStore(long p_id)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            return (BackingStoreImpl) HibernateUtil.get(BackingStoreImpl.class,
                    p_id);
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }

    }

    /**
     * Deletes a backing store from the datastore
     */
    public void deleteBackingStore(BackingStore p_backingStore)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            BackingStoreImpl bki = (BackingStoreImpl) readBackingStore(p_backingStore
                    .getId());
            HibernateUtil.delete(bki);
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
    }

    /**
     * Update the given backing store in the database. It is assumed that the
     * given object is a valid clone (i.e. it was obtained from the database in
     * an EDITABLE state).
     * <p>
     * This method must be changed to be a void method.
     */
    public BackingStore updateBackingStore(BackingStore p_BackingStore)
            throws TeamSiteServerEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update(p_BackingStore);
        }
        catch (Exception e)
        {
            throw new TeamSiteServerEntityException(e);
        }
        return p_BackingStore;
    }

    /**
     * Return all available backing stores from the database. These are returned
     * as EDITABLE objects.
     * 
     * @return a collection of backing stores
     */
    public Collection getAllBackingStores()
            throws TeamSiteServerEntityException, RemoteException
    {
        String hql = "from BackingStoreImpl b ";

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Map map = null;
        if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
        {
            hql += " where b.companyId = :companyId ";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentCompanyId));
        }

        hql += "order by b.name";
        return HibernateUtil.search(hql);
    }

    /**
     * Return all Backing Stores associated with the given TeamSite Server.
     * These are returned as IMMUTABLE (i.e. non-editable) objects.
     * 
     * @param p_teamSiteServer
     *            the TeamSite Server object containing the backing store ids to
     *            search for
     * 
     * @return a collection of backing store objects.
     */
    public Collection getBackingStoresByTeamSiteServer(
            TeamSiteServer p_teamSiteServer)
            throws TeamSiteServerEntityException, RemoteException
    {
        Collection results = new Vector();

        // if there are TeamSite Server
        if (p_teamSiteServer.getBackingStoreIds().size() > 0)
        {
            Vector args = new Vector();
            args.add(p_teamSiteServer.getBackingStoreIds());
            try
            {
                String sql = BackingStoreIds(p_teamSiteServer
                        .getBackingStoreIds());
                results = HibernateUtil.searchWithSql(BackingStoreImpl.class,
                        sql);
            }
            catch (Exception e)
            {
                throw new TeamSiteServerEntityException(e);
            }
        }

        return results;
    }

    private String BackingStoreIds(Vector p_BackingStoreIds)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM TEAMSITE_BACKING_STORE WHERE ");
        addStoreIds(sb, p_BackingStoreIds);
        return sb.toString();
    }

    private void addStoreIds(StringBuffer p_sb, Vector p_ids)
    {
        p_sb.append("ID in (");
        for (int i = 0; i < p_ids.size(); i++)
        {
            p_sb.append(p_ids.elementAt(i));
            if (i < p_ids.size() - 1)
            {
                p_sb.append(", ");
            }
        }
        p_sb.append(")");
    }

    /**
     * Checks if the TeamSite Server name already exists. If it does, make sure
     * that it isn't the same TeamSite Server being passed in (this can happeen
     * on a modify if the name isn't changed). This is used when creating a new
     * Server or modifying an existing one.
     * 
     * @return 'true' if it already exists. 'false' if it doesn't.
     */
    private boolean isTeamSiteServerNameDuplicate(
            TeamSiteServer p_teamSiteServer) throws Exception
    {
        boolean isDuplicate = false;

        // check if an active TeamSite Server already exists with this name
        long TeamSiteServerId = getTeamSiteServerIdByName(p_teamSiteServer
                .getName());

        // if one was found verify that it isn't the same file
        // server as passed in
        if (TeamSiteServerId > 0)
        {
            if (TeamSiteServerId != p_teamSiteServer.getId())
            {
                isDuplicate = true;
            }
        }

        return isDuplicate;
    }
}

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * Implements the service interface for performing CRUD operations for
 * PermissionGroup objects.
 */
public class PermissionManagerLocal implements PermissionManager
{
    private static final Logger s_logger = Logger
            .getLogger(PermissionManagerLocal.class);

    private static final String SQL_SELECT_USERS = "select USER_ID from "
            + " PERMISSIONGROUP_USER where PERMISSIONGROUP_ID=?";

    private static final String SQL_SELECT_USERS_WITH_PERM = "select distinct "
            + " USER_ID from PERMISSIONGROUP_USER pgu, PERMISSIONGROUP pg where"
            + " pgu.PERMISSIONGROUP_ID=pg.ID and pg.PERMISSION_SET like ?";

    private static final String SQL_SELECT_USERS_BY_PERM_NAME = "select distinct "
            + " USER_ID from PERMISSIONGROUP_USER pgu, PERMISSIONGROUP pg where"
            + " pgu.PERMISSIONGROUP_ID=pg.ID and pg.NAME = ?";

    private static final String SQL_INSERT_PERMISSIONGROUP_USER = "insert into "
            + " PERMISSIONGROUP_USER values(?,?)";

    private static final String SQL_DELETE_USER_FROM_PERMGROUP = "delete from "
            + " PERMISSIONGROUP_USER where PERMISSIONGROUP_ID=? and USER_ID=?";

    private static final String SQL_DELETE_USER = "delete from "
            + " PERMISSIONGROUP_USER where USER_ID=?";

    private static final String SQL_DELETE_ALL_USERS = "delete from "
            + " PERMISSIONGROUP_USER where PERMISSIONGROUP_ID=?";

    private Boolean m_lock = Boolean.TRUE;

    /**
     * Creates a new PermissionGroup object in the database.
     * 
     * @return the newly created object
     */
    public PermissionGroup createPermissionGroup(PermissionGroup p_permGroup)
            throws PermissionException, RemoteException
    {
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Creating perm group in db: " + p_permGroup);                
            }
            HibernateUtil.save(p_permGroup);
            return p_permGroup;
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * Reads the PermissionGroup object from the database.
     * 
     * @return PermissionGroup with the given id
     */
    public PermissionGroup readPermissionGroup(long p_id)
            throws PermissionException, RemoteException
    {
        try
        {
            s_logger.debug("Reading perm group " + p_id);
            return (PermissionGroupImpl) HibernateUtil.get(
                    PermissionGroupImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * Deletes a PermissionGroup from the DB.
     */
    public void deletePermissionGroup(PermissionGroup p_permGroup)
            throws PermissionException, RemoteException
    {
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Deleting perm group " + p_permGroup.toString());                
            }
            unMapAllUsersFromPermissionGroup(p_permGroup);
            HibernateUtil.delete(p_permGroup);
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * Updates the PermissionGroup object in the database.
     * 
     * @return the updated PermissionGroup
     */
    public PermissionGroup updatePermissionGroup(PermissionGroup p_permGroup)
            throws PermissionException, RemoteException
    {
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Updating perm group " + p_permGroup);                
            }
            HibernateUtil.saveOrUpdate(p_permGroup);
            return p_permGroup;
        }
        catch (Exception ex)
        {
            throw new PermissionException(ex);
        }
    }

    /**
     * Gets a list of all existing PermissionGroup objects in the database; make
     * them editable.
     * 
     * @return a vector of the PermissionGroup objects
     */
    @SuppressWarnings("unchecked")
    public Collection<PermissionGroup> getAllPermissionGroups()
            throws PermissionException, RemoteException
    {
        String hql = " from PermissionGroupImpl p ";
        Map<String, Long> map = null;

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
        {
            hql += " where p.companyId = :companyId ";
            map = new HashMap<String, Long>();
            map.put("companyId", Long.parseLong(currentCompanyId));
        }

        hql += " order by p.name ";

        try
        {
            s_logger.debug("Getting all permission groups from DB");
            return (Collection<PermissionGroup>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<PermissionGroup> getPermissionGroupsBycondition(
            String condition) throws PermissionException, RemoteException
    {
        String hql = "select p from PermissionGroupImpl p ,Company c where c.id=p.companyId";
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();

        Session session = HibernateUtil.getSession();

        if (!StringUtil.isEmpty(condition))
        {
            hql += condition;
        }
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
        {
            hql += " and p.companyId =" + Long.parseLong(currentCompanyId);
        }
        try
        {
            s_logger.debug("Getting all permission groups from DB");
            return (Collection<PermissionGroup>) session.createQuery(hql)
                    .list();
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * * Get a list of all existing PermissionGroup objects in the database with
     * specified company id. *
     * 
     * @return a Collection of the PermissionGroup objects
     */
    @SuppressWarnings("unchecked")
    public Collection<PermissionGroup> getAllPermissionGroupsByCompanyId(
            String p_companyId) throws PermissionException, RemoteException
    {
        String hql = " from PermissionGroupImpl p "
                + " where p.companyId = :COMPANY_ID order by p.name ";

        Session session = HibernateUtil.getSession();

        try
        {
            s_logger.debug("Getting all permission groups by companyId.");
            return session.createQuery(hql)
                    .setString("COMPANY_ID", p_companyId).list();
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * Queries out all the permissiongroups for the user, and ORs together all
     * the permissionsets to create one permission set.
     * 
     * @param p_userId
     *            userid
     * @return PermissionSet
     */
    public PermissionSet getPermissionSetForUser(String p_userId)
            throws PermissionException, RemoteException
    {
        PermissionSet perms = new PermissionSet();

        try
        {
            Collection<PermissionGroup> permGroups = getAllPermissionGroupsForUser(p_userId);

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(p_userId + " has " + permGroups.size()
                        + " permission groups");
            }

            Iterator<PermissionGroup> iter = permGroups.iterator();
            while (iter.hasNext())
            {
                PermissionGroup g = (PermissionGroup) iter.next();

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(p_userId + " is in group " + g.getName());
                }

                perms.or(g.getPermissionSet());
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get permissiongroups for user "
                    + p_userId, e);

            throw new PermissionException(e);
        }

        return perms;
    }

    /**
     * Queries all permissiongroups for this user.
     * 
     * @param p_userId
     *            user
     * @return Collection of PermissionGroups
     */
    @SuppressWarnings("unchecked")
    public Collection<PermissionGroup> getAllPermissionGroupsForUser(
            String p_userId) throws PermissionException, RemoteException
    {
        String sql = "select p.* from permissiongroup p, "
                + " permissiongroup_user pu where p.id = pu.permissiongroup_id "
                + " and pu.user_id = :USER_ID_ARG ";

        Session session = HibernateUtil.getSession();
        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(PermissionGroupImpl.class);
        query.setString("USER_ID_ARG", p_userId);

        try
        {
            return query.list();
        }
        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getAlltableNameForUser(String tableName)
            throws RemoteException
    {
        String sql = "";
        if (tableName == "permissiongroup")
        {
            sql = "SELECT  peru.USER_ID uid, per.NAME pname  FROM permissiongroup per, permissiongroup_user peru WHERE per.ID=peru.PERMISSIONGROUP_ID";

        }
        else if (tableName == "project")
        {
            sql = "SELECT  peru.USER_ID uid, per.PROJECT_NAME pname  FROM project per, project_user peru WHERE per.PROJECT_SEQ=peru.PROJECT_ID and per.is_active = \"Y\"";

        }

        Session session = HibernateUtil.getSession();
        SQLQuery query = session.createSQLQuery(sql);
        Collection<Object[]> l = query.list();

        try
        {
            return l;
        }

        catch (Exception e)
        {
            throw new PermissionException(e);
        }
    }

    /**
     * Queries all permissiongroup names for this user.
     * 
     * @deprecated This method only exists for backwards compatibility with the
     *             old code that checked for group names. Try not to use it.
     * 
     * @param p_userId
     *            user
     * @return Collection of PermissionGroup names as Strings
     */
    public Collection<String> getAllPermissionGroupNamesForUser(String p_userId)
            throws PermissionException, RemoteException
    {
        Collection<PermissionGroup> groups = getAllPermissionGroupsForUser(p_userId);
        Iterator<PermissionGroup> iter = groups.iterator();
        ArrayList<String> groupNames = new ArrayList<String>();

        while (iter.hasNext())
        {
            PermissionGroup pg = (PermissionGroup) iter.next();
            groupNames.add(pg.getName());
        }

        return groupNames;
    }

    /**
     * Queries all usernames for a given permission group.
     * 
     * @param p_id
     *            the ID of the permission group
     * @return Collection of String
     */
    public Collection<String> getAllUsersForPermissionGroup(long p_id)
            throws PermissionException, RemoteException
    {
        // just execute a SQL query to the mapping table since User
        // is not a toplink mapped object
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<String> users = new ArrayList<String>();

        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(SQL_SELECT_USERS);
            ps.setLong(1, p_id);
            rs = ps.executeQuery();

            while (rs.next())
            {
                users.add(rs.getString(1));
            }
        }
        catch (Exception ex)
        {
            throw new PermissionException(ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }

        int idx = users.indexOf(User.SYSTEM_USER_ID);
        if (idx > -1)
        {
            users.remove(idx);
        }

        return users;
    }

    /**
     * Queries all usernames for a given permission group name
     * 
     * @param permGroupName
     *            -- the name of the permission group
     * @return Collection of String
     */
    public Collection<String> getAllUsersForPermissionGroup(String permGroupName)
    {
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<String> users = new ArrayList<String>();

        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(SQL_SELECT_USERS_BY_PERM_NAME);
            ps.setString(1, permGroupName);
            rs = ps.executeQuery();

            while (rs.next())
            {
                users.add(rs.getString(1));
            }
        }
        catch (Exception ex)
        {
            throw new PermissionException(ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }

        int idx = users.indexOf(User.SYSTEM_USER_ID);
        if (idx > -1)
        {
            users.remove(idx);
        }

        return users;
    }

    /**
     * Queries for all users with a specific permission. This query removes the
     * system user User.SYSTEM_USER_ID from consideration.
     * 
     * @param p_permission
     *            permission name
     * @see Permission
     * @return Collection of String
     * @exception PermissionException
     * @exception RemoteException
     */
    public Collection<String> getAllUsersWithPermission(String p_permission)
            throws PermissionException, RemoteException
    {
        // Just execute a SQL query to the mapping table since User
        // is not a toplink mapped object.
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<String> users = new ArrayList<String>();

        try
        {
            String permValue = "%|"
                    + Permission.getBitValueForPermission(p_permission) + "|%";

            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(SQL_SELECT_USERS_WITH_PERM);
            ps.setString(1, permValue);
            rs = ps.executeQuery();

            while (rs.next())
            {
                users.add(rs.getString(1));
            }
        }
        catch (Exception ex)
        {
            throw new PermissionException(ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }

        int idx = users.indexOf(User.SYSTEM_USER_ID);
        if (idx > -1)
        {
            users.remove(idx);
        }

        return users;
    }

    /**
     * Takes in a list of usernames and a permissiongroup. And maps all the
     * users to the permissiongroup.
     * 
     * @param p_users
     *            list of usernames (list of String)
     * @param p_permGroup
     *            PermissionGroup
     * @exception PermissionException
     * @exception RemoteException
     */
    public void mapUsersToPermissionGroup(List<String> p_users,
            PermissionGroup p_permGroup) throws PermissionException,
            RemoteException
    {
        synchronized (m_lock)
        {
            Connection c = null;
            PreparedStatementBatch psb = null;

            try
            {
                c = ConnectionPool.getConnection();
                c.setAutoCommit(false);
                // remove existing mapped users from the list to avoid remapping
                p_users.removeAll(getAllUsersForPermissionGroup(p_permGroup
                        .getId()));
                psb = new PreparedStatementBatch(
                        PreparedStatementBatch.DEFAULT_BATCH_SIZE, c,
                        SQL_INSERT_PERMISSIONGROUP_USER, false);

                Iterator<String> usersIter = p_users.iterator();
                while (usersIter.hasNext())
                {
                    PreparedStatement ps = psb.getNextPreparedStatement();
                    String userId = (String) usersIter.next();
                    ps.setLong(1, p_permGroup.getId());
                    ps.setString(2, userId);
                    ps.addBatch();
                }

                psb.executeBatches();
                c.commit();
            }
            catch (Exception ex)
            {
                throw new PermissionException(ex);
            }
            finally
            {
                if (psb != null)
                {
                    psb.closeAll();
                }
                ConnectionPool.silentReturnConnection(c);
            }
        }
    }

    /**
     * Removes the given users from the PermissionGroup. If p_permGroup is null,
     * then the users are removed from all permgroups.
     * 
     * @param p_users
     *            list of usernames (String)
     * @param p_permGroup
     */
    public void unMapUsersFromPermissionGroup(List<String> p_users,
            PermissionGroup p_permGroup) throws PermissionException,
            RemoteException
    {
        synchronized (m_lock)
        {
            Connection c = null;
            PreparedStatementBatch psb = null;

            try
            {
                c = ConnectionPool.getConnection();
                c.setAutoCommit(false);

                if (p_permGroup == null)
                {
                    // delete all instances of this user
                    psb = new PreparedStatementBatch(
                            PreparedStatementBatch.DEFAULT_BATCH_SIZE, c,
                            SQL_DELETE_USER, false);
                }
                else
                {
                    psb = new PreparedStatementBatch(
                            PreparedStatementBatch.DEFAULT_BATCH_SIZE, c,
                            SQL_DELETE_USER_FROM_PERMGROUP, false);
                }

                Iterator<String> usersIter = p_users.iterator();
                while (usersIter.hasNext())
                {
                    PreparedStatement ps = psb.getNextPreparedStatement();
                    String userId = (String) usersIter.next();

                    if (p_permGroup == null)
                    {
                        ps.setString(1, userId);
                    }
                    else
                    {
                        ps.setLong(1, p_permGroup.getId());
                        ps.setString(2, userId);
                    }

                    ps.addBatch();
                }

                psb.executeBatches();
                c.commit();
            }
            catch (Exception ex)
            {
                throw new PermissionException(ex);
            }
            finally
            {
                if (psb != null)
                {
                    psb.closeAll();
                }
                ConnectionPool.silentReturnConnection(c);
            }
        }
    }

    /**
     * Removes all users from the PermissionGroup.
     * 
     * @param p_permGroup
     */
    private void unMapAllUsersFromPermissionGroup(PermissionGroup p_permGroup)
            throws PermissionException, RemoteException
    {
        synchronized (m_lock)
        {
            Connection c = null;
            PreparedStatement ps = null;

            try
            {
                c = ConnectionPool.getConnection();
                c.setAutoCommit(false);
                ps = c.prepareStatement(SQL_DELETE_ALL_USERS);
                ps.setLong(1, p_permGroup.getId());
                ps.execute();
                c.commit();
            }
            catch (Exception ex)
            {
                throw new PermissionException(ex);
            }
            finally
            {
                ConnectionPool.silentClose(ps);
                ConnectionPool.silentReturnConnection(c);
            }
        }
    }

}

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
package com.globalsight.everest.projecthandler;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/**
 * Update TM/TB's users.
 */
public class ProjectTMTBUsers
{
    private static final Logger c_category = Logger
            .getLogger(ProjectTMTBUsers.class);

    /**
     * update TM/TB's users
     * 
     * @param tId
     *            tm/tb's id
     * @param type
     *            "TM" or "TB"
     * @param selectedFiled
     *            selected users by admin
     * @throws UserManagerException
     * @throws RemoteException
     * @throws GeneralException
     * 
     * @author Leon Song
     * @since 8.0
     */
    public void updateUsers(String tId, String type, String selectedFiled)
            throws UserManagerException, RemoteException, GeneralException
    {
        ArrayList users = new ArrayList();
        if (selectedFiled != null && !selectedFiled.equals(""))
        {
            String[] user = selectedFiled.split(",");
            for (int i = 0; i < user.length; i++)
            {
                users.add(ServerProxy.getUserManager().getUser(user[i]));
            }
        }
        // get the users need to remove
        ArrayList existing = getAddedUsers(tId, type);
        String usersStr = "null";
        for (int i = 0; i < existing.size(); i++)
        {
            boolean found = false;
            User user = (User) existing.get(i);
            for (int j = 0; j < users.size(); j++)
            {
                User cuser = (User) users.get(j);
                if (user.getUserId().equals(cuser.getUserId()))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                usersStr = usersStr + ", '" + user.getUserId() + "'";
            }
        }
        if (!usersStr.equals("null"))
        {
            removeUsers(usersStr, tId, type);
        }
        // Get the users need to add
        ArrayList add = new ArrayList();
        for (int i = 0; i < users.size(); i++)
        {
            boolean found = false;
            User user = (User) users.get(i);
            for (int j = 0; j < existing.size(); j++)
            {
                User cuser = (User) existing.get(j);
                if (user.getUserId().equals(cuser.getUserId()))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                addUsers(user.getUserId(), tId, type);
            }
        }
    }

    /**
     * Get TM/TBs for this user
     * 
     * @param userId
     * @param type
     *            "TM" or "TB"
     * @return
     * 
     * @author Leon Song
     * @since 8.0
     */
    public List<?> getTList(String userId, String type)
    {
        String sql = "select TM_TB_ID from TM_TB_USERS where USER_ID=:userId and T_TYPE=:type";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("type", type);
        List<?> list = HibernateUtil.searchWithSql(sql, params);
        return list;
    }

    /**
     * Get all available users
     * 
     * @param locale
     * @return
     * @throws EnvoyServletException
     * 
     * @author Leon Song
     * @since 8.0
     */
    public Vector getAvailableUsers(Locale locale) throws EnvoyServletException
    {
        Vector users = new Vector();
        try
        {
            Vector usersInCompany = UserHandlerHelper
                    .getUsersForCurrentCompany();
            Iterator i = usersInCompany.iterator();
            while (i.hasNext())
            {
                User user = (User) i.next();
                boolean isAdmin = UserUtil.isInPermissionGroup(
                        user.getUserId(), "Administrator");
                if (!isAdmin)
                {
                    users.add(user);
                }
            }
            // add super users
            String sql = "select pgu.USER_ID from permissiongroup pg, permissiongroup_user pgu "
                    + "where pg.id = pgu.PERMISSIONGROUP_ID and pg.NAME !='SuperAdministrator' and pg.COMPANY_ID=:companyId";
            Map params = new HashMap();
            params.put("companyId", "1");
            List list = HibernateUtil.searchWithSql(sql, params);
            Iterator it = list.iterator();
            while (it.hasNext())
            {
                String userId = (String) it.next();
                User user = UserUtil.getUserById(userId);
                users.add(user);
            }

            UserComparator userComparator = new UserComparator(
                    UserComparator.DISPLAYNAME, locale);
            SortUtil.sort(users, userComparator);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        return users;
    }

    /**
     * Get all the users who can access this TM/TB
     * 
     * @author Leon Song
     * @since 8.0
     */
    public ArrayList getAddedUsers(String tId, String type)
    {
        ArrayList addedUsers = new ArrayList();
        String sql = "select USER_ID from TM_TB_USERS where TM_TB_ID=:tId and T_TYPE=:type";
        Map params = new HashMap();
        params.put("tId", tId);
        params.put("type", type);
        List list = HibernateUtil.searchWithSql(sql, params);
        Iterator it = list.iterator();
        while (it.hasNext())
        {
            String userId = (String) it.next();
            boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                    "Administrator");
            if (!isAdmin)
            {
                User user = UserUtil.getUserById(userId);
                addedUsers.add(user);
            }
        }
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, Locale.getDefault());
        SortUtil.sort(addedUsers, userComparator);
        return addedUsers;
    }

    /**
     * Remove users of TM/TB
     * 
     * @param usersStr
     * 
     * @author Leon Song
     * @since 8.0
     */
    private void removeUsers(String usersStr, String tId, String type)
    {
        String sql = "Delete from TM_TB_USERS where T_TYPE='" + type + "' "
                + "and TM_TB_ID=" + tId + " and USER_ID in (" + usersStr + ")";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_category
                    .error("Exception: There is error when remove users for TM");
        }
    }

    /**
     * add users for TM
     * 
     * @param userId
     *            User's id
     * @param tId
     *            Object id which needs to be added
     * @param type
     *            Object type of 'TB' or 'TM'
     * 
     * @author Leon Song
     * @since 8.0
     */
    public void addUsers(String userId, String tId, String type)
    {
        String sql = "Insert into TM_TB_USERS values (" + tId + ", '" + userId
                + "', " + "'" + type + "')";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_category.error("Exception: There is error when add users for TM");
        }
    }

    /**
     * delete all users when delete tm/tb
     * 
     * @param tId
     *            object id which needs to be deleted
     * @param type
     *            Type of 'TB' or 'TM'
     * 
     * @author Leon Song
     * @since 8.0
     */
    public void deleteAllUsers(String tId, String type)
    {
        String sql = "Delete from TM_TB_USERS where TM_TB_ID =" + tId
                + " and T_TYPE='" + type + "';";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_category
                    .error("Exception: There is error when remove users for TM");
        }
    }

    /**
     * delete all tm and tb when delete user
     * 
     * @param userId
     *            User's id
     * 
     * @author Leon Song
     * @since 8.0
     */
    public void deleteAllTMTB(String userId)
    {
        String sql = "Delete from TM_TB_USERS where user_id ='" + userId + "'";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_category
                    .error("Exception: There is error when remove users for TM");
        }
    }
}

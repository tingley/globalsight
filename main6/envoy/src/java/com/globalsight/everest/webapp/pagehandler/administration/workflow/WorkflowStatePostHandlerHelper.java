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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class WorkflowStatePostHandlerHelper
{

    /**
     * Return all the workflow state post profiles (for GUI) in the system.
     * 
     * @param filterParams
     * @return Return all the workflow state post profiles as a vector.
     */
    public static List<WorkflowStatePosts> getAllWfStatePostForGUI(
            String[] filterParams)
    {
        try
        {
            return ServerProxy.getProjectHandler()
                    .getAllWorkflowStatePostProfie(filterParams);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static void createWfStatePostProfile(WorkflowStatePosts wfStatePost)
    {
        try
        {
            ServerProxy.getProjectHandler().createWfStatePostProfile(
                    wfStatePost);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static WorkflowStatePosts getWfStatePostProfile(long wfStatePostId)
    {
        try
        {
            return ServerProxy.getProjectHandler().getWfStatePostProfile(
                    wfStatePostId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static void modifyWfStatePostProfile(WorkflowStatePosts wfstaPosts)
    {
        try
        {
            ServerProxy.getProjectHandler()
                    .modifyWfStatePostProfile(wfstaPosts);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }

    }

    public static void removeWorkflowStatePost(long wfStatePostId)
    {

        try
        {
            ServerProxy.getProjectHandler().removeWorkflowStatePost(
                    getWfStatePostProfile(wfStatePostId));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    public static Map<String, Object> checkWfStatePostProfile(
            HttpServletRequest p_request)
    {
        Connection connection = null;
        PreparedStatement query = null;
        try
        {
            connection = ConnectionPool.getConnection();
            String sql1 = "UPDATE l10n_profile SET WF_STATE_POST_ID = -1 WHERE IS_ACTIVE = \"N\"";
            connection.setAutoCommit(false);
            query = connection.prepareStatement(sql1);
            query.execute();
            connection.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (query != null)
            {
                try
                {
                    query.close();
                }
                catch (Exception e)
                {
                }
            }
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (Exception cpe)
            {
            }
        }
        String errorId = "";
        Map<String, String> param = null;
        String sql = "SELECT DISTINCT L.* FROM L10N_PROFILE L"
                + " WHERE L.IS_ACTIVE = \"Y\" "
                + " AND L.WF_STATE_POST_ID =:wfStatePostId";
        String wfStatePostId = p_request.getParameter("wfStatePostId");
        if (StringUtil.isNotEmpty(wfStatePostId))
        {
            param = new HashMap<String, String>();
            param.put("wfStatePostId", wfStatePostId);
            List list = HibernateUtil.searchWithSql(sql, param);
            if (list != null && list.size() > 0)
            {
                errorId = wfStatePostId;
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        if (errorId.trim().length() > 0)
        {
            map.put("wrong", errorId);
        }
        else
        {
            map.put("wrong", "needRemove");
        }
        return map;
    }

    public static List getAllWfStatePost()
    {
        try
        {
            return ServerProxy.getProjectHandler()
                    .getAllWorkflowStatePostInfos();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

}

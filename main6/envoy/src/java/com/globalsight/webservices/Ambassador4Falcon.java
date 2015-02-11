/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.webservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.log.ActivityLog;
import com.globalsight.util.StringUtil;

/**
 * WebService APIs of GlobalSight handles web services related to projects,
 * jobs, workflows, import, export,setup, etc. for GlobalSight
 * 
 * NOTE: The web service that Apache Axis generates will be named
 * Ambassador4Falcon
 */
public class Ambassador4Falcon extends JsonTypeWebService
{
    // Method names
    private static final Logger logger = Logger
            .getLogger(Ambassador4Falcon.class);

    public static final String GET_JOB_IDS_WITH_STATUES_CHANGED = "getJobIDsWithStatusChanged";

    /**
     * Constructs a GlobalSight WebService object.
     */
    public Ambassador4Falcon()
    {
        logger.info("Creating new GlobalSight Web Service for Falcon.");
    }

    public String getJobIDsWithStatusChanged(String p_accessToken,
            int p_intervalInMinute) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_IDS_WITH_STATUES_CHANGED);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String json = "";
        ActivityLog.Start activityStart = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("intervalInMinute", p_intervalInMinute);
            activityStart = ActivityLog.start(Ambassador4Falcon.class,
                            "getJobIDsWithStatusChanged(p_accessToken,p_intervalInMinute)",
                    activityArgs);
            if (StringUtil.isEmpty(p_accessToken) || p_intervalInMinute < 1)
            {
                return makeErrorJson("getJobIDsWithStatusChanged",
                        "Invaild time range parameter.");
            }
            // int hours = getHours(p_sinceTime);
            Calendar calendar = Calendar.getInstance();
            // calendar.add(Calendar.HOUR, 0 - hours);
            calendar.add(Calendar.MINUTE, 0 - p_intervalInMinute);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStamp = sdf.format(calendar.getTime());
            User user = getUser(getUsernameFromSession(p_accessToken));
            String companyName = user.getCompanyName();

            String sql = "SELECT DISTINCT workflow.JOB_ID FROM task_info, workflow, job "
                    + "WHERE workflow.COMPANY_ID = ? "
                    + "AND (task_info.ACCEPTED_DATE > ? "
                    + "    OR task_info.COMPLETED_DATE > ? "
                    + "    OR job.TIMESTAMP > ? "
                    + "    OR workflow.TIMESTAMP > ? "
                    + "    OR workflow.COMPLETED_DATE > ?"
                    + "    OR workflow.DISPATCH_DATE > ?) "
                    + "AND workflow.IFLOW_INSTANCE_ID = task_info.WORKFLOW_ID "
                    + "AND job.ID = workflow.JOB_ID";

            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, CompanyWrapper.getCompanyIdByName(companyName));
            query.setString(2, timeStamp);
            query.setString(3, timeStamp);
            query.setString(4, timeStamp);
            query.setString(5, timeStamp);
            query.setString(6, timeStamp);
            query.setString(7, timeStamp);
            results = query.executeQuery();
            json = resultSetToJson("JOB_ID", results);
        }
        catch (Exception e)
        {
            return makeErrorJson("getJobIDsWithStatusChanged",
                    "Cannot get jobs correctly. " + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            releaseDBResource(results, query, connection);
        }

        return json;
    }
}

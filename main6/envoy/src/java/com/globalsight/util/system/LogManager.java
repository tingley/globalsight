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
package com.globalsight.util.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.usermgr.LoggedUser;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class LogManager
{
    public static final String EVENT_TYPE_INSERT = "NEW";
    public static final String EVENT_TYPE_UPDATE = "UPDATE";
    public static final String EVENT_TYPE_REMOVE = "REMOVE";
    
    private static String LOG_SQL = "insert into system_log (object_type, event_type, object_id, operator, operate_time, message, company_id) values (?, ?, ?, ?, ?, ?, ?)";

    private static Logger LOGGER = Logger.getLogger(LogManager.class);
 
    @SuppressWarnings("unchecked")  
    public ArrayList<LogInfo> getAllOperationLogs(String objectType, String eventType, String operator, String message) throws Exception
    {
        try
        {
            StringBuffer hql = new StringBuffer("from LogInfo a where 1 = 1");
            
            if(objectType !=null && objectType.length()>0)
            {
                hql.append(" and a.objectType like '%" + objectType + "%'");
            }
            if(eventType !=null && eventType.length()>0)
            {
                hql.append(" and a.eventType like '%" + eventType + "%'");
            }
            if(operator !=null && operator.length()>0)
            {
                hql.append(" and a.operator like '%" + operator + "%'");
            }
            if(message !=null && message.length()>0)
            {
                hql.append(" and a.message like '%" + message + "%'");
            }
            
            HashMap<String, LogInfo> map = new HashMap<String, LogInfo>();
            
            ArrayList<LogInfo> listLog = (ArrayList<LogInfo>)  HibernateUtil.search(hql.toString(), map);
            
            return listLog;
        }
        catch (Exception e)
        {
            throw new Exception(e);
        }
    }
    
    public static void log(LogInfo logInfo) throws Exception
    {
        if (logInfo == null)
            return;
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try
        {
            conn = DbUtil.getConnection();
            pstmt = conn.prepareStatement(LOG_SQL);
            pstmt.setString(1, logInfo.getObjectType());
            pstmt.setString(2, logInfo.getEventType());
            pstmt.setString(3, logInfo.getObjectId());
            pstmt.setString(4, logInfo.getOperator());
            pstmt.setTimestamp(5, logInfo.getOperateTime());
            pstmt.setString(6, logInfo.getMessage());
            pstmt.setLong(7, logInfo.getCompanyId());
            
            pstmt.execute();
        }
        catch (Exception e)
        {
            LOGGER.error("Cannot save log info successfully.", e);
            throw new Exception(e);
        }
        finally
        {
            DbUtil.silentClose(pstmt);
            DbUtil.silentReturnConnection(conn);
        }
    }

    public static void log(LogType logType, String eventType, long objectId,
            String message, long companyId) throws Exception
    {
        LogInfo logInfo = new LogInfo();
        logInfo.setObjectType(logType.getType());
        logInfo.setEventType(eventType);
        logInfo.setObjectId(String.valueOf(objectId));

        UserInfo userInfo = LoggedUser.getInstance().getLoggedUserInfo();
        if (userInfo != null)
            logInfo.setOperator(userInfo.getUserId());
        else
            logInfo.setOperator("Unknown");

        logInfo.setOperateTime(new Timestamp(Calendar.getInstance()
                .getTimeInMillis()));
        logInfo.setMessage(message);
        logInfo.setCompanyId(companyId);

        log(logInfo);
    }
}

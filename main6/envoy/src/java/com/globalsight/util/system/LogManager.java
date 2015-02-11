package com.globalsight.util.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.usermgr.LoggedUser;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class LogManager
{
    public static final String EVENT_TYPE_INSERT = "INSERT";
    public static final String EVENT_TYPE_UPDATE = "UPDATE";
    public static final String EVENT_TYPE_REMOVE = "REMOVE";
    public static final String EVENT_TYPE_QUERY = "QUERY";
    private static String LOG_SQL = "insert into system_log (event_type, object_type, object_id, operator, operate_time, message, company_id) values (?, ?, ?, ?, ?, ?, ?)";
    
    private static Logger LOGGER = Logger.getLogger(LogManager.class);

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
            pstmt.setString(1, logInfo.getEventType());
            pstmt.setString(2, logInfo.getObjectType());
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

    public static void log(LogType logType, String objectType, long objectId,
            String message, long companyId) throws Exception
    {
        LogInfo logInfo = new LogInfo();
        logInfo.setEventType(logType.getType());
        logInfo.setObjectType(objectType);
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

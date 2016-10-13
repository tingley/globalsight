/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.SortUtil;

/**
 * The ReportsData JDBC Utility.
 */
public class ReportDBUtil
{
    static private final Logger logger = Logger.getLogger(ReportDBUtil.class);
    static final int MAX_LENGTH = 500;
    
    static private final String SQL_REPORTSDATA_SELECT = 
            "SELECT STATUS, PERCENT FROM REPORTS_DATA WHERE USER_ID = ? AND REPORT_JOBIDS = ? AND REPORT_TYPELIST = ?";
    static private final String SQL_REPORTSDATA_SELECT_ALL = 
            "SELECT USER_ID, REPORT_JOBIDS, REPORT_TYPELIST, STATUS, PERCENT FROM REPORTS_DATA";
    static private final String SQL_REPORTSDATA_SELECT_BYUSER = 
            "SELECT REPORT_JOBIDS, REPORT_TYPELIST, STATUS, PERCENT FROM REPORTS_DATA WHERE USER_ID = ? ORDER BY REPORT_TYPELIST";
    static private final String SQL_REPORTSDATA_DELETE = 
            "DELETE FROM REPORTS_DATA WHERE USER_ID = ? AND REPORT_JOBIDS = ? AND REPORT_TYPELIST = ?";
    static private final String SQL_REPORTSDATA_DELETE_ALL = "DELETE FROM REPORTS_DATA";
    static private final String SQL_REPORTSDATA_INSERT = 
            "INSERT INTO REPORTS_DATA(USER_ID, REPORT_JOBIDS, REPORT_TYPELIST, STATUS, PERCENT) VALUES(?, ?, ?, ?, ?)";
    static private final String SQL_REPORTSDATA_UPDATE = 
            "UPDATE REPORTS_DATA SET STATUS = ? , PERCENT = ? WHERE USER_ID = ? AND REPORT_JOBIDS = ? AND REPORT_TYPELIST = ?";
    static private final String SQL_REPORTSDATA_UPDATE_STATUS = 
            "UPDATE REPORTS_DATA SET STATUS = ? WHERE USER_ID = ? AND REPORT_JOBIDS = ? AND REPORT_TYPELIST = ?";
    
    /**
     * Get the ReportsData Object from Table(REPORTS_DATA)
     * 
     * @param p_userId
     *            User ID
     * @param p_reportJobIDS
     *            Report Job ID List
     * @param p_reportTypeList
     *            Report Type List
     */
    public static synchronized ReportsData getReportsData(String p_userId,
            List<Long> p_reportJobIDS, List<String> p_reportTypeList)
    {
        ReportsData result = null;
        Connection conn = null;
        PreparedStatement ps  = null;
        ResultSet rs = null;
        try
        {
            String jobIdStr = getString(p_reportJobIDS, MAX_LENGTH);
            String typeListStr = getString(p_reportTypeList, MAX_LENGTH);
            
            conn = SqlUtil.hireConnection();
            ps = conn.prepareStatement(SQL_REPORTSDATA_SELECT);
            ps.setString(1, p_userId);
            ps.setString(2, jobIdStr);
            ps.setString(3, typeListStr); 
            rs = ps.executeQuery();
            if (rs.next())
            {
                result = new ReportsData(p_userId, p_reportJobIDS,
                        p_reportTypeList, rs.getDouble("PERCENT"),
                        rs.getString("STATUS"));
            }
        }
        catch (SQLException e)
        {
            String message = "getReportsData error on user:" + p_userId;
            logger.error(message, e);
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return result;
    }
    
    public static synchronized List<ReportsData> getReportsData(String p_userId)
    {
        List<ReportsData> result = new ArrayList<ReportsData>();
        Connection conn = null;
        PreparedStatement ps  = null;
        ResultSet rs = null;
        try
        {            
            conn = SqlUtil.hireConnection();
            ps = conn.prepareStatement(SQL_REPORTSDATA_SELECT_BYUSER);
            ps.setString(1, p_userId);
            rs = ps.executeQuery();
            while (rs.next())
            {
                ReportsData data = new ReportsData(p_userId, null, null, 
                        rs.getDouble("PERCENT"), rs.getString("STATUS"));
                data.setReportJobIDString(rs.getString("REPORT_JOBIDS"));
                data.setReportTypeString(rs.getString("REPORT_TYPELIST"));
                result.add(data);
            }
        }
        catch (SQLException e)
        {
            String message = "getReportsData error on user:" + p_userId;
            logger.error(message, e);
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    public static synchronized String saveOrUpdateReportsData(String p_userId,
            List<Long> p_reportJobIDS, List<String> p_reportTypeList,
            String p_status, double p_percent)
    {
        if(p_status == null)
            return null;
        
        Connection conn = null;
        PreparedStatement ps  = null;
        ResultSet rs = null;
        try
        {
            String jobIdStr = getString(p_reportJobIDS, MAX_LENGTH);
            String typeListStr = getString(p_reportTypeList, MAX_LENGTH);
            
            conn = SqlUtil.hireConnection();
            
            ps = conn.prepareStatement(SQL_REPORTSDATA_SELECT);
            ps.setString(1, p_userId);
            ps.setString(2, jobIdStr);
            ps.setString(3, typeListStr); 
            rs = ps.executeQuery();
            
            // Save or Update
            if (!rs.next())
            {
                ps = conn.prepareStatement(SQL_REPORTSDATA_INSERT);
                ps.setString(1, p_userId);
                ps.setString(2, jobIdStr);
                ps.setString(3, typeListStr);
                ps.setString(4, p_status);
                ps.setDouble(5, p_percent);
                ps.executeUpdate();
                return "SAVE";
            }
            else
            {
                ps = conn.prepareStatement(SQL_REPORTSDATA_UPDATE);
                ps.setString(1, p_status);
                ps.setDouble(2, p_percent);
                ps.setString(3, p_userId);
                ps.setString(4, jobIdStr);
                ps.setString(5, typeListStr);                   
                ps.executeUpdate();
                return "UPDATE";
            }
        }
        catch (SQLException e)
        {
            String message = "saveOrUpdateReportsData error by user:" + p_userId;
            logger.error(message, e);
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return null;
    }
    
    public static synchronized String updateReportsData(ReportsData p_reportsData)
    {
        if (p_reportsData.getStatus() == null)
            return null;

        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = SqlUtil.hireConnection();
            String jobIdStr = getString(p_reportsData.getReportJobIDS(), MAX_LENGTH);
            String typeListStr = getString(p_reportsData.getReportTypeList(), MAX_LENGTH);

            ps = conn.prepareStatement(SQL_REPORTSDATA_UPDATE);
            ps.setString(1, p_reportsData.getStatus());
            ps.setDouble(2, p_reportsData.getPercent());
            ps.setString(3, p_reportsData.getUserId());
            ps.setString(4, jobIdStr);
            ps.setString(5, typeListStr);
            ps.executeUpdate();
            return "Success";
        }
        catch (SQLException e)
        {
            String message = "updateReportsData error by user:" + p_reportsData.getUserId();
            logger.error(message, e);
        }
        finally
        {
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return "fail";
    }
    
    /**
     * Update ReportsData Status
     * 
     * @param p_userId              Table USER_ID column
     * @param p_jobIdStr            Table REPORT_JOBIDS column 
     * @param p_reportTypeListStr   Table REPORT_TYPELIST column
     * @param p_reportTypeListStr   Table STATUS column
     * @return
     */
    public static synchronized String updateReportsDataStatus(String p_userId, String p_jobIdStr, String p_reportTypeListStr, String p_status)
    {
        if (p_status == null)
            return null;

        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = SqlUtil.hireConnection();

            ps = conn.prepareStatement(SQL_REPORTSDATA_UPDATE_STATUS);
            ps.setString(1, p_status);
            ps.setString(2, p_userId);
            ps.setString(3, p_jobIdStr);
            ps.setString(4, p_reportTypeListStr);
            ps.executeUpdate();
            return "Success";
        }
        catch (SQLException e)
        {
            String message = "updateReportsDataStatus error by user:" + p_userId;
            logger.error(message, e);
        }
        finally
        {
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return "fail";
    }

    public static synchronized boolean delReportsData(String p_userId,
            List<Long> p_reportJobIDS, List<String> p_reportTypeList)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            String jobIdStr = getString(p_reportJobIDS, MAX_LENGTH);
            String typeListStr = getString(p_reportTypeList, MAX_LENGTH);
            
            conn = SqlUtil.hireConnection();
            ps = conn.prepareStatement(SQL_REPORTSDATA_DELETE);
            ps.setString(1, p_userId);
            ps.setString(2, jobIdStr);
            ps.setString(3, typeListStr);  
            ps.execute();
        }
        catch (SQLException e)
        {
            String message = "delReportsData error by user:" + p_userId;
            logger.error(message, e);
            return false;
        }
        finally
        {
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return true;
    }
    
    // Empty REPORTS_DATA Table. 
    public static synchronized boolean delAllReportsData()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = SqlUtil.hireConnection();
            ps = conn.prepareStatement(SQL_REPORTSDATA_SELECT_ALL);
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next())
            {
                count++;
                StringBuffer message = new StringBuffer();
                message.append("[USER_ID:").append(rs.getString("USER_ID")).append(", ");
                message.append("REPORT_JOBIDS:").append(rs.getString("REPORT_JOBIDS")).append(", ");
                message.append("REPORT_TYPELIST:").append(rs.getString("REPORT_TYPELIST")).append(", ");
                message.append("STATUS:").append(rs.getString("STATUS")).append(", ");
                message.append("PERCENT:").append(rs.getDouble("PERCENT")).append("]");
                logger.info("REPORTS_DATA Data: " + message.toString());
            }
            if (count > 0)
            {
                ps = conn.prepareStatement(SQL_REPORTSDATA_DELETE_ALL);
                ps.execute();
                logger.info("'REPORTS_DATA' table is cleaned : " + count);
            }
        }
        catch (SQLException e)
        {
            logger.error("delAllReportsData error.", e);
            return false;
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return true;
    }
    
    private static String getString(List p_list, int p_maxLength)
    {
        if (p_list == null || p_list.size() == 0)
            return "[]";
        
        List dataList = new ArrayList(p_list);
        SortUtil.sort(dataList);
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < dataList.size(); i++)
        {
            result.append(dataList.get(i)).append(",");
            if (result.length() > p_maxLength)
                break;
        }
        result.deleteCharAt(result.lastIndexOf(","));
        result.append("]");
        if (result.length() > p_maxLength)
        {
            return result.substring(0, p_maxLength - 1);
        }

        return result.toString();
    }
}

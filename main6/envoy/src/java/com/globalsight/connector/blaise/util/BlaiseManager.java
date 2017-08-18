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
package com.globalsight.connector.blaise.util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class BlaiseManager
{
    private static final Logger logger = Logger.getLogger(BlaiseManager.class);

    /**
     * Return all Blaise connectors.
     * 
     * @return List<?>
     */
    public static List<?> getAllConnectors()
    {
        String hql = "from BlaiseConnector e where e.isActive = 'Y' ";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and e.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    /**
     * Gets all Blaise server connectors
     */
    public static List<?> getConnectors()
    {
        ArrayList<BlaiseConnector> connectors = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from connector_blaise where is_active='Y'");
            BlaiseConnector connector;
            while (rs.next())
            {
                connector = new BlaiseConnector();
                connector.setId(rs.getInt("ID"));
                connector.setName(rs.getString("Name"));
                connector.setUrl(rs.getString("URL"));
                connector.setUsername(rs.getString("User_Name"));
                connector.setPassword(rs.getString("Password"));
                connector.setClientCoreVersion(rs.getString("CLIENT_CORE_VERSION"));
                connector.setClientCoreRevision(rs.getInt("CLIENT_CORE_REVISION"));
                connector.setWorkflowId(rs.getString("WORKFLOW_ID"));
                connector.setIsActive("Y".equalsIgnoreCase(rs.getString("IS_ACTIVE")));
                connector.setCompanyId(rs.getLong("COMPANY_ID"));
                connector.setAutomatic("Y".equalsIgnoreCase(rs.getString("IS_AUTOMATIC")));
                connector.setUserCalendar(rs.getString("USER_CALENDAR"));
                connector.setPullDays(rs.getString("PULL_DAYS"));
                connector.setUserPullDays(rs.getString("USER_PULL_DAYS"));
                connector.setPullHour(rs.getInt("PULL_HOUR"));
                connector.setUserPullHour(rs.getInt("USER_PULL_HOUR"));
                connector.setDefaultFileProfileId(rs.getLong("DEFAULT_FILE_PROFILE_ID"));
                connector.setMinProcedureWords(rs.getInt("MIN_PROCEDURE_WORDS"));
                connector.setCombined("Y".equals(rs.getString("IS_COMBINED")));
                connector.setLastMaxEntryId(rs.getLong("LAST_MAX_ENTRY_ID"));
                connector.setLoginUser(rs.getString("LOGIN_USER"));
                connector.setCheckDuration(rs.getInt("CHECK_DURATION"));
                connector.setQaCount(rs.getInt("QA_COUNT"));

                connectors.add(connector);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when invoking getConnectors().", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
        return connectors;
    }

    public static BlaiseConnector getBlaiseConnectorById(long blaiseConnectorId)
    {
        return HibernateUtil.get(BlaiseConnector.class, blaiseConnectorId);
    }

    @SuppressWarnings("unchecked")
    public static List<BlaiseConnectorJob> getBlaiseConnectorJobByJobId(long jobId)
    {
        String hql = "from BlaiseConnectorJob bcj where bcj.jobId = " + jobId;
        return (List<BlaiseConnectorJob>) HibernateUtil.search(hql);
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public static BlaiseConnectorJob getBlaiseConnectorJobByJobIdEntryId(long jobId, long entryId)
    {
        String hql = "from BlaiseConnectorJob bcj where bcj.jobId = :jobId and bcj.blaiseEntryId = :blaiseEntryId";
        Map map = new HashMap();
        map.put("jobId", jobId);
        map.put("blaiseEntryId", entryId);
        return (BlaiseConnectorJob) HibernateUtil.getFirst(hql, map);
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public static BlaiseConnectorJob getBlaiseConnectorJobByJobIdSourcePageId(long jobId,
            long sourcePageId)
    {
        String hql = "from BlaiseConnectorJob bcj where bcj.jobId = :jobId and bcj.sourcePageId = :sourcePageId";
        Map map = new HashMap();
        map.put("jobId", jobId);
        map.put("sourcePageId", sourcePageId);
        return (BlaiseConnectorJob) HibernateUtil.getFirst(hql, map);
    }

    public static Map<Long, List<Long>> getEntryId2JobIdsMap(List<Long> entryIds,
            long blaiseConnectorId) throws Exception
    {
        Map<Long, List<Long>> entryId2JobIdsMap = new HashMap<Long, List<Long>>();
        if (entryIds == null || entryIds.size() == 0)
            return entryId2JobIdsMap;

        String idStr = listToString(entryIds);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            String sql = "SELECT BLAISE_ENTRY_ID, JOB_ID " + " FROM connector_blaise_job "
                    + " WHERE BLAISE_CONNECTOR_ID = ? " + " AND BLAISE_ENTRY_ID IN (" + idStr + ")"
                    + " ORDER BY BLAISE_ENTRY_ID, JOB_ID";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, blaiseConnectorId);
            rs = ps.executeQuery();
            while (rs.next())
            {
                long entryId = rs.getLong(1);
                long jobId = rs.getLong(2);
                List<Long> jobIds = entryId2JobIdsMap.get(entryId);
                if (jobIds == null)
                {
                    jobIds = new ArrayList<Long>();
                }
                jobIds.add(jobId);
                entryId2JobIdsMap.put(entryId, jobIds);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return entryId2JobIdsMap;
    }

    public static String listToString(Set<Long> ids)
    {
        List<Long> idList = new ArrayList<Long>();
        idList.addAll(ids);
        return listToString(idList);
    }

    public static String listToString(List<Long> ids)
    {
        if (ids == null || ids.size() == 0)
            return "";

        StringBuilder result = new StringBuilder();
        for (Long id : ids)
        {
            result.append(id).append(",");
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * Gets the blaise entry id from the source page file path.
     * 
     * @since GBS-4749
     * 
     */
    public static long fetchBlaiseEntryIdFromExternalPageId(String externalPageId)
    {
        long blaiseEntryId = -1;
        try
        {
            int startIndex = externalPageId.indexOf(File.separator);
            startIndex = externalPageId.indexOf(File.separator, startIndex + 1);
            int endIndex = externalPageId.indexOf(File.separator, startIndex + 1);
            String id = externalPageId.substring(startIndex + 1, endIndex);
            blaiseEntryId = Long.parseLong(id);
        }
        catch (Exception e)
        {
            blaiseEntryId = -1;
        }
        return blaiseEntryId;
    }
}

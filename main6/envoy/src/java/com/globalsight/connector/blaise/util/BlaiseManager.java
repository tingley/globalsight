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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public static BlaiseConnector getBlaiseConnectorById(long blaiseConnectorId)
    {
        return HibernateUtil.get(BlaiseConnector.class, blaiseConnectorId);
    }

    public static BlaiseConnectorJob getBlaiseConnectorJobByJobId(long jobId)
    {
    	String hql = "from BlaiseConnectorJob bcj where bcj.jobId = " + jobId;
    	return (BlaiseConnectorJob) HibernateUtil.getFirst(hql);
    }

    public static Map<Long, List<Long>> getEntryId2JobIdsMap(
			List<Long> entryIds, long blaiseConnectorId) throws Exception
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
			String sql = "SELECT BLAISE_ENTRY_ID, JOB_ID "
					+ " FROM connector_blaise_job "
					+ " WHERE BLAISE_CONNECTOR_ID = ? "
					+ " AND BLAISE_ENTRY_ID IN (" + idStr + ")"
					+ " ORDER BY BLAISE_ENTRY_ID, JOB_ID";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, blaiseConnectorId);
			rs = ps.executeQuery();
			while (rs.next())
			{
				long entryId = rs.getLong(1);
				long jobId = rs.getLong(2);
				List<Long> jobIds = entryId2JobIdsMap.get(entryId);
				if (jobIds == null) {
					jobIds = new ArrayList<Long>();
				}
				jobIds.add(jobId);
				entryId2JobIdsMap.put(entryId, jobIds);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		} finally {
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
}

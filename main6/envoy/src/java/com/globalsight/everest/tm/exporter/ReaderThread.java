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

package com.globalsight.everest.tm.exporter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportOptions.FilterOptions;
import com.globalsight.everest.tm.exporter.ExportOptions.JobAttributeOptions;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;

/**
 * Reads entries from a TM and produces TU objects by putting ReaderResult
 * objects into a ReaderResultQueue.
 */
public class ReaderThread extends Thread
{
    static private final Logger CATEGORY = Logger.getLogger(ReaderThread.class);

    /** When reading TUs from the TM, read BATCH_READ_SIZE TUs at a time. */
    static private final int BATCH_READ_SIZE = 200;

    private ReaderResultQueue m_results;

    private ExportOptions m_options;

    private Tm m_database;

    private SessionInfo m_session;

    //
    // Constructor
    //
    public ReaderThread(ReaderResultQueue p_queue, ExportOptions p_options,
            Tm p_database, SessionInfo p_session)
    {
        m_results = p_queue;
        m_options = p_options;
        m_database = p_database;
        m_session = p_session;

        this.setName("TM Reader Thread");
    }

    //
    // Thread methods
    //
    public void run()
    {
        ReaderResult result = null;
        SegmentResultSet tus = null;
        Connection conn = null;
        try
        {
            CATEGORY.debug("ReaderThread: start reading TM "
                    + m_database.getName());
            conn = DbUtil.getConnection();

            // Simple, not optimal: get all TU ids, keep them in memory.
            // Then read each TUs in batches and output.
            tus = getTuSegments(conn);
            while (tus.hasNext())
            {
                SegmentTmTu tu = tus.next();
                result = m_results.hireResult();
                result.setResultObject(tu);
                boolean done = m_results.put(result);
                result = null;

                if (done)
                {
                    // writer died, cleanup & return.
                    return;
                }
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("export error", ex);

            m_options.setError(ex.getMessage());
        }
        finally
        {
            if (result != null)
            {
                m_results.fireResult(result);
            }
            m_results.producerDone();
            m_results = null;
            DbUtil.silentReturnConnection(conn);
            HibernateUtil.closeSession();
            CATEGORY.debug("ReaderThread: done.");
        }
    }

    //
    // PRIVATE METHODS
    //

    /**
     * Retrieves a list of TU ids (as Long objects) that need to be exported
     * from the TM.
     */
    private SegmentResultSet getTuSegments(Connection conn) throws Exception
	{
		com.globalsight.everest.tm.exporter.ExportOptions options = m_options;
		String mode = options.getSelectMode();
		FilterOptions filterString = options.getFilterOptions();

		JobAttributeOptions jobAttributes = options.getJobAttributeOptions();
		Set<String> jobAttributeSet = jobAttributes.jobAttributeSet;
		TmCoreManager mgr = LingServerProxy.getTmCoreManager();

		Map<String, Object> paramterMap = getParamMap(filterString);
		paramterMap.put("jobAttributeSet", jobAttributeSet);

		if (mode.equals(ExportOptions.SELECT_ALL))
		{
			return mgr.getAllSegmentsByParamMap(m_database, paramterMap, conn);
		}
		else
		{
			String msg = "invalid select mode `" + mode + "'";
			CATEGORY.error(msg);
			m_options.setError(msg);

			return null;
		}
	}
    
	private Map<String, Object> getParamMap(FilterOptions filterString)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String createdAfter = filterString.m_createdAfter;
		String createdBefore = filterString.m_createdBefore;
		String modifyAfter = filterString.m_modifiedAfter;
		String modifyBefore = filterString.m_modifiedBefore;
		String modifyUser = filterString.m_modifiedBy;
		String createUser = filterString.m_createdBy;
		String tuIds = filterString.m_tuId;
		String stringId = filterString.m_sid;
		String isRegex = filterString.m_regex;
		String jobId = filterString.m_jobId;
		String lastUsageAfter = filterString.m_lastUsageAfter;
		String lastUsageBefore = filterString.m_lastUsageBefore;

		String lang = filterString.m_language;
		List localelist = null;
		if (StringUtil.isNotEmpty(lang))
		{
			List<String> langList = Arrays.asList(lang.split(","));
			localelist = getLocaleList(langList);
		}
		String propType = filterString.m_projectName;

		if (localelist != null && localelist.size() > 0)
		{
			paramMap.put("language", localelist);
		}

		if (StringUtil.isNotEmpty(propType))
		{
			paramMap.put("projectName", propType);
		}

		if (StringUtil.isNotEmpty(createUser))
		{
			paramMap.put("createUser", createUser);
		}
		if (StringUtil.isNotEmpty(modifyUser))
		{
			paramMap.put("modifyUser", modifyUser);
		}
		if (StringUtil.isNotEmpty(modifyAfter))
		{
			paramMap.put("modifyAfter", modifyAfter);
		}
		if (StringUtil.isNotEmpty(modifyBefore))
		{
			paramMap.put("modifyBefore", modifyBefore);
		}
		if (StringUtil.isNotEmpty(createdAfter))
		{
			paramMap.put("createdAfter", createdAfter);
		}
		if (StringUtil.isNotEmpty(createdBefore))
		{
			paramMap.put("createdBefore", createdBefore);
		}
		if (StringUtil.isNotEmpty(tuIds))
		{
			paramMap.put("tuIds", tuIds);
		}
		if (StringUtil.isNotEmpty(stringId))
		{
			paramMap.put("stringId", stringId);
			paramMap.put("isRegex", isRegex);
		}
		if (StringUtil.isNotEmpty(jobId))
		{
			paramMap.put("jobId", jobId);
		}
		if (StringUtil.isNotEmpty(lastUsageAfter))
		{
			paramMap.put("lastUsageAfter", lastUsageAfter);
		}
		if (StringUtil.isNotEmpty(lastUsageBefore))
		{
			paramMap.put("lastUsageBefore", lastUsageBefore);
		}

		return paramMap;
	}
	
	private List getLocaleList(List<String> localeCodeList)
	{
		List localeList = new ArrayList();
		GlobalSightLocale locale = null;
		for (int i = 0; i < localeCodeList.size(); i++)
		{
			locale = GSDataFactory.localeFromCode(localeCodeList.get(i));
			if (locale != null)
			{
				localeList.add(locale);
			}
		}
		return localeList;
	}
}

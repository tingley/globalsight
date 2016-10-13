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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportOptions.JobAttributeOptions;
import com.globalsight.everest.tm.exporter.ExportOptions.FilterOptions;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IReader;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;

/**
 * Implementation of the export reader. Reads entries from a TM.
 */
public class Reader implements IReader
{
    private static final Logger CATEGORY = Logger
            .getLogger(Reader.class);

    private Tm m_database;

    private ExportOptions m_options;

    private SessionInfo m_session;

    private ReaderThread m_thread = null;

    private ReaderResultQueue m_results;

    private ReaderResult m_result;

    public Reader(ExportOptions p_options, Tm p_database, SessionInfo p_session)
    {
        m_database = p_database;
        m_session = p_session;

        setExportOptions(p_options);
    }

    /**
     * Sets new export options for this reader.
     */
    public void setExportOptions(ExportOptions p_options)
    {
        m_options = p_options;
    }

    /**
     * Analyzes export options and TM and returns an updated ExportOptions
     * object with a status whether the options are syntactically correct, the
     * number of expected entries to be exported, and column descriptors in case
     * of CSV files.
     */
    public ExportOptions analyze()
    {
        m_options = doAnalyze();

        return m_options;
    }

    /**
     * Start reading termbase and producing entries.
     */
    public void start()
    {
        // Ensure the thread is running
        startThread();
    }

    /**
     * Lets the reader read in the next entry and returns true if an entry is
     * available, else false.
     */
    public boolean hasNext()
    {
        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        return false;
    }

    /**
     * Retrieves the next ReaderResult, which is an Entry together with a status
     * code and error message.
     * 
     * @see ReaderResult
     * @see Entry
     */
    public ReaderResult next()
    {
        return m_result;
    }

    /**
     * Stop reading and producing new entries.
     */
    public void stop()
    {
        stopThread();
    }

    //
    // PRIVATE METHODS
    //

    private void startThread()
    {
        com.globalsight.everest.tm.exporter.ExportOptions options = (com.globalsight.everest.tm.exporter.ExportOptions) m_options;

        if (m_thread == null)
        {
            m_results = new ReaderResultQueue(100);
            m_thread = new ReaderThread(m_results, options, m_database,
                    m_session);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();

            m_results = null;
            m_thread = null;
        }
    }

    /**
     * Retrieves the expected entry count from the database.
     */
    @SuppressWarnings("static-access")
    private ExportOptions doAnalyze()
	{
		com.globalsight.everest.tm.exporter.ExportOptions options = (com.globalsight.everest.tm.exporter.ExportOptions) m_options;
		String identifyKey = null;
		try
		{
			String mode = options.getSelectMode();
			int count = -1;
			FilterOptions filterString = options.getFilterOptions();
			identifyKey = options.getIdentifyKey();

			TmCoreManager mgr = LingServerProxy.getTmCoreManager();
			JobAttributeOptions jobAttributes = options
					.getJobAttributeOptions();
			Set<String> jobAttributeSet = jobAttributes.jobAttributeSet;

			Map<String, Object> paramMap = getParamMap(filterString);
			paramMap.put("jobAttributeSet", jobAttributeSet);

			if (mode.equals(com.globalsight.everest.tm.exporter.ExportOptions.SELECT_ALL))
			{
				count = mgr.getAllSegmentsCountByParamMap(m_database, paramMap);

				m_options.setStatus(ExportOptions.ANALYZED);
				m_options.setExpectedEntryCount(count);
			}
			else
			{
				String msg = "invalid select mode `" + mode + "'";

				CATEGORY.error(msg);

				m_options.setError(msg);
			}
		}
		catch (/* Exporter */Exception ex)
		{
			ExportUtil.handleTmExportFlagFile(identifyKey, "failed", true);
			CATEGORY.error("analysis error", ex);
			m_options.setError(ex.getMessage());
		}

		return m_options;
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
		List localelist = null;
		String lang = filterString.m_language;
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

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



package com.globalsight.everest.edit.offline.download.HTMLResourcePages;

import java.util.ResourceBundle;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;

/**
 * Generates the list of pages in a job (html).
 */
public class JobListWriter
    extends DownloadWriter
    implements DownloadWriterInterface
{
    static private final String JOB_LIST_START = "JobListStart";
    static private final String JOB_LIST_ENTRY = "JobListEntry";
    static private final String JOB_LIST_END = "JobListEnd";
    static private final int PARAM_COUNT = 3;
    static private final String RESOURCE_FILE = "JobListWriter";

    private String m_jobId = null;
    private StringBuffer m_entries = null;
    private ResourceBundle m_resource = null;
    private String m_uiLocale = "en_US";
    private OfflinePageData m_offlinePageData = null;

    /**
     * Constructor.
     */
    public JobListWriter()
        throws AmbassadorDwUpException
    {
        super();
        m_entries = new StringBuffer();
    }

    public void reset()
    {
        m_entries.setLength(0);
    }

    public void setUiLocale(String p_uiLocale)
    {
        m_uiLocale = p_uiLocale;
    }

    private boolean areParamsValid()
    {
        if (m_jobId.length() == 0)
        {
            return false;
        }

        return true;
    }

    private String[] makeParamList()
    {
        String[] params = new String[PARAM_COUNT];
        params[1] = JOBS_DIR + "/" + m_jobId + ".html";
        params[2] = m_offlinePageData.getJobName() + "&nbsp;(" + m_jobId + ")";
        return params;
    }

    protected StringBuffer buildPage()
        throws AmbassadorDwUpException
    {
        StringBuffer page = new StringBuffer();
        page.append(m_resource.getString(JOB_LIST_START));
        page.append(m_entries);
        page.append(m_resource.getString(JOB_LIST_END));

        return page;
    }

    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException
    {
        m_resource = loadProperties(getClass().getName(), getLocale(m_uiLocale));

        m_offlinePageData = p_page;
        m_jobId = p_page.getJobId() + "";

        if (!areParamsValid())
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_INVALID_PARAMETER,
                this.getClass().getName());
        }

        // add job if not exists
        if (m_entries.indexOf("&nbsp;(" + m_jobId + ")") == -1)
            m_entries.append(formatString(
                    m_resource.getString(JOB_LIST_ENTRY), makeParamList()));
    }
}

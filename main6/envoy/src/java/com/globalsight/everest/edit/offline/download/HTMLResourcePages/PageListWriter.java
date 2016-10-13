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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ResourceBundle;
import java.util.ListIterator;
import java.util.ResourceBundle;

import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;

/**
 * Generates the list of pages in a job (html).
 */
public class PageListWriter
    extends DownloadWriter
    implements DownloadWriterInterface
{
    static private final String PAGE_LIST_START = "PageListStart";
    static private final String PAGE_LIST_ENTRY = "PageListEntry";
    static private final String PAGE_LIST_END = "PageListEnd";
    static private final String PREVIEW_PAGE = "PreviewPage";
    static private final int PARAM_COUNT = 3;
    static private final String RESOURCE_FILE = "PageListWriter";

    private String m_SegmentIdListLink = null;
    private String m_pageId = null;
    private long m_jobId = -1;
    private boolean m_useJobs = false;
    private StringBuffer m_entries = null;
    private ResourceBundle m_resource = null;
    private String m_uiLocale = "en_US";
    private String m_pageName = null;

    /**
     * Constructor.
     */
    public PageListWriter()
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
        if (m_pageId.length() == 0)
        {
            return false;
        }

        return true;
    }

    private String[] makeParamList()
    {
        String[] params = new String[PARAM_COUNT];
        if (!m_useJobs)
        {
            params[1] = RESOURCE_DIR + "/" + m_pageId + "/" + SEG_ID_LIST_FILE;
        }
        else
        {
            params[1] = "../" + m_pageId + "/" + SEG_ID_LIST_FILE;
        }
        params[2] = m_pageName + "&nbsp;(" + m_pageId + ")";
        return params;
    }

    protected StringBuffer buildPage()
        throws AmbassadorDwUpException
    {
        StringBuffer page = new StringBuffer();
        page.append(m_resource.getString(PAGE_LIST_START));
        page.append(m_entries);
        page.append(m_resource.getString(PAGE_LIST_END));

        return page;
    }
    
    public void useJobs(boolean usejobs)
    {
        m_useJobs = usejobs;
    }
    
    public boolean isAddable(long jobid)
    {
        return (m_jobId == -1 || m_jobId == jobid);
    }
    
    public long getJobId()
    {
        return m_jobId;
    }

    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException
    {
        m_resource = loadProperties(getClass().getName(), getLocale(m_uiLocale));

        m_pageName = p_page.getPageName();
        m_pageId = p_page.getPageId();
        m_jobId = p_page.getJobId();

        if (!areParamsValid())
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_INVALID_PARAMETER,
                this.getClass().getName());
        }

        m_entries.append(formatString(
            m_resource.getString(PAGE_LIST_ENTRY), makeParamList()));

        // add preview page URL
        if (p_page.isCanUseUrl())
        {
            String previewUrl = p_page.getUrlPrefix() + p_page.getPageName();
            m_entries.append(formatString(
                m_resource.getString(PREVIEW_PAGE), previewUrl));
        }
    }
}

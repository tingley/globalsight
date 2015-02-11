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
import java.lang.StringBuffer;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;

import java.util.Locale;

public class IndexPageWriter
    extends DownloadWriter
    implements DownloadWriterInterface
{
    static private final String INDEX_PAGE = "IndexPage";
    static private final int PARAM_COUNT = 4;
    static private final String RESOURCE_FILE = "IndexPageWriter";

    private ResourceBundle m_resource = null;
    private String m_pageId = null;

    /**
     * Constructor.
     */
    public IndexPageWriter()
        throws AmbassadorDwUpException
    {
        super();
    }

    public void reset()
    {
    }

    private String[] makeParamList()
    {
        String[] params = new String[PARAM_COUNT];
        params[1] = PAGE_LIST_FILE;
        params[2] = RESOURCE_DIR + "/" + m_pageId + "/" + SEG_ID_LIST_FILE;
        params[3] = RESOURCE_DIR + "/" + m_pageId + "/" + m_pageId + ".html";
        return params;
    }

    private boolean areParamsValid()
    {
        if (m_pageId == null || m_pageId.length() == 0)
        {
            return false;
        }

        return true;
    }

    /**
     *
     *
     */
    protected StringBuffer buildPage()
        throws AmbassadorDwUpException
    {
        if (!areParamsValid())
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_INVALID_PARAMETER,
                this.getClass().getName());
        }

        StringBuffer page = new StringBuffer();
        page.append(formatString(m_resource.getString(INDEX_PAGE),
            makeParamList()));

        return page;
    }

    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException
    {
        // TODO: get ui locale through download params.
        // Qucik fix - force en_US
        m_resource = loadProperties(getClass().getName(),
            getLocale("en_US" /*p_page.getTargetLocaleName()*/));

        m_pageId = p_page.getPageId();
    }
}

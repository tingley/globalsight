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

import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.StringBuffer;
import java.util.ResourceBundle;
import java.util.ListIterator;
import java.util.ResourceBundle;

/**
 * Generates the list of segment IDs for a page (html).
 */
public class SegmentIdListWriter
    extends DownloadWriter
    implements DownloadWriterInterface
{
    static private final String SEGMENT_ID_LIST_START = "SegmentIdListStart";
    static private final String SEGMENT_ID_LIST_ENTRY = "SegmentIdListEntry";
    static private final String SEGMENT_ID_LIST_END = "SegmentIdListEnd";
    static private final int SEG_ID_PARAM_COUNT = 3;
    static private final String SEG_ID_LIST_TITLE = "SEG_ID_LIST_TITLE";

    private StringBuffer m_segIds = null;
    private ResourceBundle m_resource = null;
    private String m_uiLocale = "en_US";
    private OfflinePageData m_offlinePageData = null;

    /**
     * Constructor.
     */
    public SegmentIdListWriter()
        throws AmbassadorDwUpException
    {
        super();

        m_segIds = new StringBuffer();
    }

    public void reset()
    {
        m_segIds.setLength(0);
    }

    public void setUiLocale(String p_uiLocale)
    {
        m_uiLocale = p_uiLocale;
    }

    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException
    {
        m_resource = loadProperties(getClass().getName(), getLocale(m_uiLocale));

        m_offlinePageData = p_page;

        for (ListIterator it = p_page.getSegmentIterator(); it.hasNext(); )
        {
            OfflineSegmentData segment = (OfflineSegmentData)it.next();

            if (!areParamsValid(segment))
            {
                throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_INVALID_PARAMETER,
                    this.getClass().getName());
            }

            m_segIds.append(formatString(
                m_resource.getString(SEGMENT_ID_LIST_ENTRY),
                makeParamList(segment)));
        }
    }

    protected StringBuffer buildPage()
        throws AmbassadorDwUpException
    {
        StringBuffer page = new StringBuffer();

        page.append(formatString(
            m_resource.getString(SEGMENT_ID_LIST_START),
            m_resource.getString(SEG_ID_LIST_TITLE)));
        page.append(m_segIds);
        page.append(m_resource.getString(SEGMENT_ID_LIST_END));

        return page;
    }

    private boolean areParamsValid(OfflineSegmentData p_segment)
    {
        if (p_segment.getDisplaySegmentID() == null ||
            p_segment.getDisplaySegmentID().length() == 0)
        {
            return false;
        }

        return true;
    }

    private String[] makeParamList(OfflineSegmentData p_segment)
    {
        String id = p_segment.getDisplaySegmentID();
        String[] params = new String[SEG_ID_PARAM_COUNT];
        params[1] = m_offlinePageData.getPageId() + ".html#" + id;
        params[2] = p_segment.getDisplaySegmentID();
        return params;
    }
}

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
package com.globalsight.everest.tm.searchreplace;

import java.io.Serializable;

import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.ExactMatchFormatHandler;

public class TuvInfo implements Serializable
{
    private static final long serialVersionUID = 3938227156447003315L;

    private long m_id;
    private String m_segment;
    private long m_exactMatchKey = 0;
    private String m_exactMatchFormat = null;
    private String m_dataType;
    private long m_localeId;
    private long m_jobId;

    public TuvInfo()
    {
    }

    public void setId(long p_id)
    {
        m_id = p_id;
    }

    public void setSegment(String p_segment)
    {
        m_segment = p_segment;
    }

    public long getId()
    {
        return m_id;
    }

    public String getGxml()
    {
        return getSegment();
    }

    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    public String getDataType()
    {
        return m_dataType;
    }

    public void setLocaleId(long p_localeId)
    {
        m_localeId = p_localeId;
    }

    public long getLocaleId()
    {
        return m_localeId;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#getExactMatchKey()
     */
    public long getExactMatchKey()
    {
        return m_exactMatchKey;
    }

    /**
     * @see com.globalsight.ling.tm.TuvLing#setExactMatchKey(long)
     */
    public void setExactMatchKey(long p_exactMatchKey)
    {
        m_exactMatchKey = p_exactMatchKey;
    }

    public String getSegment()
    {
        return m_segment;
    }

    public String getDisplayHtml()
    {
        int viewMode = 0;

        return GxmlUtil.getDisplayHtml(m_segment,
            IFormatNames.FORMAT_HTML, viewMode);
    }

    /**
     * Get the native formatted string - used to generate CRCs.
     *
     * @return The native formatted string..
     */
    public String getExactMatchFormat()
        throws Exception
    {
        if (m_exactMatchFormat == null)
        {
            ExactMatchFormatHandler handler = new ExactMatchFormatHandler();
            DiplomatBasicParser diplomatParser =
                new DiplomatBasicParser(handler);

            try
            {
                diplomatParser.parse(getGxml());
            }
            catch (DiplomatBasicParserException e)
            {
                throw new Exception(e);
            }

            m_exactMatchFormat = handler.toString();
        }

        return m_exactMatchFormat;
    }

    public long getJobId()
    {
        return m_jobId;
    }

    public void setJobId(long p_jobId)
    {
        m_jobId = p_jobId;
    }
}

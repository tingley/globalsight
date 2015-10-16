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
package com.globalsight.ling.docproc.extractor.xliff20;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

public class XliffAlt extends PersistentObject
{
    private static final long serialVersionUID = 1L;

    private String altSource = "";
    private String altSegment = "";
    private TuvImpl tuv = null;
    private long tuvId = -1;
    private GxmlElement m_gxmlElement = null;
    private GxmlElement m_gxmlElementSource = null;
    private String language = null;
    private String quality = null;

    public void setTuv(TuvImpl tuvimpl)
    {
        tuv = tuvimpl;
        if (tuvimpl != null)
        {
            tuvId = tuvimpl.getId();
        }
    }

    public long getTuvId()
    {
        return tuvId;
    }

    public void setTuvId(long tuvId)
    {
        this.tuvId = tuvId;
    }

    public String getSourceSegment()
    {
        return altSource;
    }

    public void setSourceSegment(String altSource)
    {
        this.altSource = altSource;
    }

    public String getSegment()
    {
        return altSegment;
    }

    public void setSegment(String altSegment)
    {
        this.altSegment = altSegment;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String p_language)
    {
        this.language = p_language;
    }

    public String getQuality()
    {
        return quality;
    }

    public void setQuality(String p_quality)
    {
        this.quality = p_quality;
    }

    public GxmlElement getGxmlElement()
    {
        if (m_gxmlElement == null)
        {
            String segment = getSegment();
            GxmlFragmentReader reader = null;

            try
            {
                reader = GxmlFragmentReaderPool.instance()
                        .getGxmlFragmentReader();

                m_gxmlElement = reader.parseFragment(segment);
            }
            catch (Exception e)
            {
                // Can't have Tuv in inconsistent state, throw runtime
                // exception.
                throw new RuntimeException("Error in XliffAlt: ", e);
            }
            finally
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }

        return m_gxmlElement;
    }

    public GxmlElement getGxmlElementSource()
    {
        if (m_gxmlElementSource == null)
        {
            String segment = getSourceSegment();
            GxmlFragmentReader reader = null;

            try
            {
                reader = GxmlFragmentReaderPool.instance()
                        .getGxmlFragmentReader();

                m_gxmlElementSource = reader.parseFragment(segment);
            }
            catch (Exception e)
            {
                // Can't have Tuv in inconsistent state, throw runtime
                // exception.
                throw new RuntimeException("Error in XliffAlt: ", e);
            }
            finally
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }

        return m_gxmlElementSource;
    }
}

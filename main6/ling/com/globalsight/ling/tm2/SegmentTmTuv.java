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
package com.globalsight.ling.tm2;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.ling.tm2.leverage.TmxTagStatistics;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.SegmentTmExactMatchFormatHandler;
import com.globalsight.ling.common.TuvSegmentBaseHandler;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

/**
 * SegmentTmTuv is a representation of Tuv in Page Tm.
 */
public class SegmentTmTuv
    extends AbstractTmTuv
{
    static private Logger logger = Logger.getLogger(SegmentTmTuv.class);

    // cache
    private String m_noCodeFormat = null;
    private TmxTagStatistics m_tagStatistics = null;

    // Used for leverage match statistics. Not persisted.
    private int m_wordCount = 0;
    
    private String orgSegment = null;


    /**
     * Default constructor.
     */
    public SegmentTmTuv()
    {
        super();
    }


    /**
     * Constructor.
     * @param p_id id
     * @param p_segment segment string
     * @param p_locale GlobalSightLocale
     */
    public SegmentTmTuv(long p_id, String p_segment,
        GlobalSightLocale p_locale)
    {
        super(p_id, p_segment, p_locale);
    }

    public void setWordCount(int p_wordCount)
    {
        m_wordCount = p_wordCount;
    }

    public int getWordCount()
    {
        return m_wordCount;
    }


    public boolean isSourceTuv()
    {
        return getLocale().equals(((SegmentTmTu)getTu()).getSourceLocale());
    }

    /**
     * Returns a segment string with no TMX tag and no formatting code.
     *
     * @return A string that contains no code and no TMX tags.
     */
    public String getNoCodeFormat()
        throws LingManagerException
    {
        if (m_noCodeFormat == null)
        {
            try
            {
                NoCodeFormatHandler handler = new NoCodeFormatHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);

                diplomatParser.parse(getSegment());
                m_noCodeFormat = handler.toString();
            }
            catch (Exception ex)
            {
                throw new LingManagerException(ex);
            }
        }

        return m_noCodeFormat;
    }


    /**
     * Returns a TmxTagStatistics object. The object records the
     * number of occurence of the same tags. The same tags means that
     * they are the same element (bpt, ept, ph or it) and their type
     * attribute value are the same. Eraseable tags are not counted in
     * the statistics. The statistics can be used to determine if two
     * segment's tag structures are similar enough to call them the
     * same.
     */
    public TmxTagStatistics getTmxTagStatistics()
        throws LingManagerException
    {
        if (m_tagStatistics == null)
        {
            try
            {
                TagStatisticsHandler handler =
                    new TagStatisticsHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);

                diplomatParser.parse(getSegment());
                m_tagStatistics = handler.getTmxStatistics();
            }
            catch (Exception ex)
            {
                throw new LingManagerException(ex);
            }
        }

        return m_tagStatistics;
    }


    protected void cleanCache()
    {
        super.cleanCache();
        m_noCodeFormat = null;
        m_tagStatistics = null;
    }


    /**
     * See the description of ExactMatchFormatHandler inner class.
     *
     * @return The native formatted string.
     */
    public String getExactMatchFormat()
        throws LingManagerException
    {
        if (m_exactMatchFormat == null)
        {
            try
            {
                SegmentTmExactMatchFormatHandler handler =
                    new SegmentTmExactMatchFormatHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);

                diplomatParser.parse(getSegment());
                m_exactMatchFormat = handler.toString();
            }
            catch (Exception ex)
            {
                throw new LingManagerException(ex);
            }
        }

        return m_exactMatchFormat;
    }


    /**
     * This handler process a segment string to produce a string with
     * no TMX tag in it.
     */
    private static class NoCodeFormatHandler
        extends TuvSegmentBaseHandler
    {
        private StringBuffer m_content = new StringBuffer(200);

        // Write out all text node. TMX tags in SegmentTmTuv don't
        // have text nodes.
        public void handleText(String p_text)
        {
            // accumulate all the text
            m_content.append(m_xmlDecoder.decodeStringBasic(p_text));
        }

        // convert nbsp markup and MS Office space markup to real
        // space characters
        public void handleStartTag(String p_name, Properties p_attributes,
            String p_originalString)
        {
            if (p_name.equals(GxmlNames.PH))
            {
                String type = p_attributes.getProperty(GxmlNames.PH_TYPE);

                if (type != null)
                {
                    if (type.equals(TmUtil.X_NBSP))
                    {
                        m_content.append('\u00a0');
                    }
                    else if (type.equals(TmUtil.X_MSO_SPACERUN))
                    {
                        m_content.append(' ');
                    }
                    else if (type.equals(TmUtil.X_MSO_TAB))
                    {
                        m_content.append('\t');
                    }
                }
            }
        }

        public String toString()
        {
            return m_content.toString();
        }

    }

    /**
     * This handler process a segment string and produce a statistics
     * of TMX tags. The result is stored in a TmxTagStatistics
     * object. The object records the number of occurence of the same
     * tags. The same tags means that they are the same element (bpt,
     * ph or it) and their type attribute value are the
     * same. Eraseable tags are not counted in the statistics. ept
     * tags are not counted as well. The statistics can be used to
     * determine if two segment's tag structures are similar enough to
     * call them the same.  */
    private static class TagStatisticsHandler
        extends TuvSegmentBaseHandler
    {
        private TmxTagStatistics m_statistics = new TmxTagStatistics();

        public void handleStartTag(String p_name, Properties p_attributes,
            String p_originalString)
        {
            if (p_name.equals(GxmlNames.BPT) ||
                p_name.equals(GxmlNames.PH) ||
                p_name.equals(GxmlNames.IT))
            {
                String type = p_attributes.getProperty(GxmlNames.BPT_TYPE);

                if (type == null)
                {
                    type = "none";
                }

                // def 11875: don't look at "type" when <it pos="end">
                if (p_name.equals(GxmlNames.IT))
                {
                    String pos = p_attributes.getProperty(GxmlNames.IT_POS);
                    if (pos != null && pos.equals("end"))
                    {
                        type = pos;
                    }
                }

                // def 11876: normalize "type"
                type = TmxTypeMapper.normalizeType(type);

                String erasable = p_attributes.getProperty(
                    GxmlNames.BPT_ERASEABLE);

                if (erasable == null)
                {
                    erasable = "no";
                }

                if (erasable.equals("no"))
                {
                    m_statistics.add(p_name, type);
                }
            }
        }

        public TmxTagStatistics getTmxStatistics()
        {
            return m_statistics;
        }
    }

    public String getOrgSegment()
    {
        return orgSegment;
    }


    public void setOrgSegment(String orgSegment)
    {
        this.orgSegment = orgSegment;
    }

    public void merge(SegmentTmTuv another)
    {
        this.setCreationUser(another.getCreationUser());
        this.setModifyDate(new Timestamp(new Date().getTime()));
        this.setModifyUser(another.getModifyUser());
        this.setSegment(another.getSegment());
        this.setUpdatedProject(another.getUpdatedProject());
        this.setSid(another.getSid());
    }
}

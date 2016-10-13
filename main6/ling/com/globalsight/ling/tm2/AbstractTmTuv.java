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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.ExactMatchFormatHandler;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.TuvSegmentBaseHandler;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlNames;

/**
 * AbstractTmTuv is an abstract class that represents a super class of
 * various Translation Unit Variant representations. Translation Unit
 * Variant has a text in a given locale and its meta data. One source
 * and one or more Translation Unit Variants form a Translation Unit.
 * The term Translation Unit and Translation Unit Variant are taken
 * from TMX (http://www.lisa.org/tmx) specification.
 */
public abstract class AbstractTmTuv
    implements BaseTmTuv, Cloneable
{
	static private final Logger c_logger = Logger
			.getLogger(AbstractTmTuv.class);

    private long m_id;  // Tuv id
    private BaseTmTu m_tu; // reference to Tu

    // segment string in GXML. It must have a top level tag (<segment>
    private String m_segment;
    private long m_exactMatchKey;
    private GlobalSightLocale m_locale;

    private String m_creationUser = null;
    private Timestamp m_creationDate = null;
    private String m_modifyUser = null;
    private Timestamp m_modifyDate = null;

    // Cache field for formatted strings
    protected String m_exactMatchFormat = null;
    protected String m_fuzzyIndexFormat = null;
    
    private String m_updatedByProject = null;
    private String sid = null;

    private Timestamp lastUsageDate = null;
    private long jobId = -1;
    private String jobName = null;
    private long previousHash = -1;
    private long nextHash = -1;

    /**
     * Default constructor. It can be called only from a subclass.
     */
    protected AbstractTmTuv()
    {
        m_id = 0;
        m_tu = null;
        m_segment = null;
        m_exactMatchKey = 0;
        m_locale = null;
        m_creationDate = new Timestamp(System.currentTimeMillis());
        m_modifyDate = new Timestamp(System.currentTimeMillis());
    }


    /**
     * Constructor. It can be called only from a subclass.
     * @param p_id id
     * @param p_segment segment string
     * @param p_locale GlobalSightLocale
     */
    protected AbstractTmTuv(long p_id, String p_segment,
        GlobalSightLocale p_locale)
    {
        m_id = p_id;
        m_tu = null;
        m_segment = p_segment;
        m_exactMatchKey = 0;
        m_locale = p_locale;
        m_creationDate = new Timestamp(System.currentTimeMillis());
        m_modifyDate = new Timestamp(System.currentTimeMillis());
    }


    public long getId()
    {
        return m_id;
    }

    public void setId(long p_id)
    {
        m_id = p_id;
    }

    /**
     * Gets the tu that this tuv belongs to.
     * @return The Tu that this tuv belongs to.
     */
    public BaseTmTu getTu()
    {
        return m_tu;
    }


    /**
     * Sets the Tu that this tuv belongs to.
     * @param p_tu - The Tu to be set.
     */
    public void setTu(BaseTmTu p_tu)
    {
        m_tu = p_tu;
    }


    /**
     * Gets the segment string.
     * @return segment string
     */
    public String getSegment()
    {
        return m_segment;
    }


    /**
     * Gets the segment string without top tag.
     * @return segment string
     */
    public String getSegmentNoTopTag()
        throws LingManagerException
    {
        NoTopTagHandler handler = new NoTopTagHandler();

        try
        {
            DiplomatBasicParser diplomatParser =
                new DiplomatBasicParser(handler);

            diplomatParser.parse(m_segment);
        }
        catch (Exception ex)
        {
            throw new LingManagerException(ex);
        }

        return handler.toString();
    }



    /**
     * Sets the segment string.
     * @param p_segment segment string
     */
    public void setSegment(String p_segment)
    {
        m_segment = p_segment;
        cleanCache();
    }


    /**
     * Gets the exact match key.
     * @return exact match key
     */
    public long getExactMatchKey()
    {
        return m_exactMatchKey;
    }

    /**
     * Sets the exact match key.
     * @param p_exactMatchKey exact match key
     */
    public void setExactMatchKey(long p_exactMatchKey)
    {
        m_exactMatchKey = p_exactMatchKey;
    }

    /**
     * Sets the exact match key. This method calculates the exact
     * match key based on m_segment and sets the key.
     */
    public void setExactMatchKey()
        throws LingManagerException
    {
        try
        {
            setExactMatchKey(GlobalSightCrc.calculate(
                getExactMatchFormat()));
        }
        catch (Exception ex)
        {
            throw new LingManagerException(ex);
        }
    }

    /**
     * Gets the locale.
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getLocale()
    {
        return m_locale;
    }

    /**
     * Sets the locales
     * @param p_locale GlobalSightLocale
     */
    public void setLocale(GlobalSightLocale p_locale)
    {
        m_locale = p_locale;
    }


    public String getType()
    {
        return m_tu.getType();
    }


    public boolean isTranslatable()
    {
        // if m_tu is not set, NullPointerException is thrown.
        return m_tu.isTranslatable();
    }

    public String getCreationUser()
    {
        return m_creationUser;
    }

    public void setCreationUser(String p_creationUser)
    {
        m_creationUser = p_creationUser;
    }

    public Timestamp getCreationDate()
    {
        return m_creationDate;
    }

    public void setCreationDate(Timestamp p_creationDate)
    {
        m_creationDate = p_creationDate;
    }

    public String getModifyUser()
    {
        return m_modifyUser;
    }

    public void setModifyUser(String p_modifyUser)
    {
        m_modifyUser = p_modifyUser;
    }

    public Timestamp getModifyDate()
    {
        return m_modifyDate;
    }

    public void setModifyDate(Timestamp p_modifyDate)
    {
        m_modifyDate = p_modifyDate;
    }
    
    public void setUpdatedProject(String p_project)
    {
    	m_updatedByProject = p_project;
    }
    
    public String getUpdatedProject()
    {
    	return m_updatedByProject;
    }
    /**
     * Overrides Object#equals(). Two BaseTmTuv objects are deemed to
     * be equal if the locale, the text, the type and the localize type
     * (translatable or localizable) are the same.
     *
     * @param p_other some object
     */
    public boolean equals(Object p_other)
    {
        boolean ret = false;

        try
        {
            if (p_other instanceof BaseTmTuv)
            {
                BaseTmTuv otherTuv = (BaseTmTuv)p_other;
                if (this == otherTuv)
                {
                    ret = true;
                }
                else if (getLocale().equals(otherTuv.getLocale())
                        && getType().equals(otherTuv.getType())
                        && (!(isTranslatable() ^ otherTuv.isTranslatable()))
                        && m_exactMatchKey == otherTuv.getExactMatchKey()
                        && getExactMatchFormat().equals(
                                otherTuv.getExactMatchFormat())
                        && hashCode() == otherTuv.hashCode()
                        && getPreviousHash() == otherTuv.getPreviousHash()
                        && getNextHash() == otherTuv.getNextHash())
                {
                    ret = true;
                }
            }
        }
        // can't throw exception from this overriden method
        catch (LingManagerException ex)
        {
            throw new RuntimeException(ex.toString());
        }

        return ret;
    }

    /**
     * Override Object#hashCode().
     */
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (int) (m_exactMatchKey ^ (m_exactMatchKey >>> 32));
        result = prime * result + ((sid == null) ? 0 : sid.hashCode());
        return result;
    }


    /**
     * Get the native formatted string - used to generate CRCs.
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
                ExactMatchFormatHandler handler =
                    new ExactMatchFormatHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);

                diplomatParser.parse(m_segment);
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
     * Gets translatable and localizable content only from the Tuv -
     * used to generate fuzzy indexes. That is, the text plus subflows
     * in order.
     *
     * @return tuv without formatting.
     */
    public String getFuzzyIndexFormat()
        throws LingManagerException
    {
        if (m_fuzzyIndexFormat == null)
        {
            try
            {
                FuzzyIndexFormatHandler handler =
                    new FuzzyIndexFormatHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);

                diplomatParser.parse(m_segment);

                // add spaces at the beginning and the end of the string
                String fuzzyIndexFormat = " " + handler.toString() + " ";
                // normalize white space
                fuzzyIndexFormat =
                    Text.normalizeWhiteSpaceForTm(fuzzyIndexFormat);
                // down case the string
                m_fuzzyIndexFormat =
                    fuzzyIndexFormat.toLowerCase(m_locale.getLocale());
            }
            catch (Exception ex)
            {
                throw new LingManagerException(ex);
            }
        }

        return m_fuzzyIndexFormat;
    }


    /**
     * Prepares the segment string for saving to or leveraging from
     * Segment TM. This method does the followings.
     *
     * 1) removes native formatting codes. For example, '<bpt
     * type="link" x="1">some code</bpt>' becomes '<bpt type="link"
     * x="1"/>'.
     *
     * 2) separates subflows out from the main text and make its own
     * segment. Subflows segment will have <segment> or <localizable>
     * top level tag according to its localizable type.
     *
     * <sub> elements in segment text in this object must have id
     * attribute set with unique sub id values. This is supposed to be
     * already done if the text comes from job data
     * (translation_unit_variant).
     *
     * @return Processed strings are returned in a SegmentAttributes
     * object. The object has an attribute of sub id. For the main
     * text, its sub id is "0".
     */
    public Collection prepareForSegmentTm()
        throws LingManagerException
    {
        Collection results = null;

        try
        {
            SubflowSeparationHandler handler =
                new SubflowSeparationHandler();
            DiplomatBasicParser diplomatParser =
                new DiplomatBasicParser(handler);

            diplomatParser.parse(m_segment);
            results = handler.getSegments();
        }
        catch (Exception ex)
        {
            throw new LingManagerException(ex);
        }

        return results;
    }

    /**
     * @return Always return false. 
     */
    public boolean isClobSegment()
    {
    	return false;
    }


    public Object clone()
    {
        Object o = null;

        try
        {
            o = super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }

        return o;
    }


    public String toDebugString()
    {
        StringBuffer result = new StringBuffer();

        result.append("TUV ");
        result.append(" id=");
        result.append(getId());
        result.append(" loc=");
        result.append(getLocale());
        result.append(" exact key=");
        result.append(getExactMatchKey());
        result.append(" created by=");
        result.append(getCreationUser());
        result.append(" created on=");
        result.append(getCreationDate());
        result.append(" modified by=");
        result.append(getModifyUser());
        result.append(" modified on=");
        result.append(getModifyDate());
        result.append("\n");
        result.append("\t`");
        result.append(getSegment());
        result.append("'\n");

        return result.toString();
    }


    protected void cleanCache()
    {
        m_exactMatchFormat = null;
        m_fuzzyIndexFormat = null;
    }


    /////////// Handler inner classes //////////


    // Used in prepareForSegmentTm method
    private class SubflowSeparationHandler
        extends TuvSegmentBaseHandler
    {
        private boolean m_addsText = true;
        private boolean m_isPhSub = false;
        private Stack m_currentSegment = new Stack();
        private Collection m_segments = new ArrayList();

        // Overridden method
        public void handleStart()
        {
            m_addsText = true;

            // create SegmentAttributes for the main text
            SegmentAttributes segAtt = new SegmentAttributes(
                m_tu.getFormat(), m_tu.getType(),
                m_tu.isTranslatable(), SegmentTmTu.ROOT);
            m_segments.add(segAtt);
            m_currentSegment.push(segAtt);
        }

        // Overridden method
        public void handleText(String p_text)
        {
            if (m_addsText)
            {
                // add only text, no formatting code, to the current
                // SegmentAttributes. Do not decode string.
                ((SegmentAttributes)m_currentSegment.peek()).appendText(
                    p_text);
            }
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_attributes,
            String p_originalString)
            throws DiplomatBasicParserException
        {
        	p_name = p_name.toLowerCase();
            if (p_name.equals(GxmlNames.SEGMENT) ||
                p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_addsText = true;

                SegmentAttributes segAtt =
                    (SegmentAttributes)m_currentSegment.peek();

                // append the tag with its origianl form which
                // includes wordcount. These tags must be at the
                // beginning of the text.
                segAtt.appendText(p_originalString);

                // set wordcount
                String wordCount = p_attributes.getProperty(
                    GxmlNames.SEGMENT_WORDCOUNT);
                if (wordCount == null)
                {
                    wordCount = "0";
                }

                try
                {
                    segAtt.setWordCount(Integer.parseInt(wordCount));
                }
                catch (NumberFormatException ex)
                {
                    throw new DiplomatBasicParserException(
                        "Word count format error: " + ex.getMessage());
                }
            }
            else if (p_name.equals(GxmlNames.SUB))
            {
                m_addsText = true;

                SegmentAttributes segAtt = getSegmentAttributes(p_attributes);
                m_segments.add(segAtt);
                m_currentSegment.push(segAtt);
            }
            else if (p_name.equals(GxmlNames.BPT) ||
                p_name.equals(GxmlNames.EPT) ||
                p_name.equals(GxmlNames.PH) ||
                p_name.equals(GxmlNames.IT))
            {
                SegmentAttributes segatt = ((SegmentAttributes) m_currentSegment
                        .peek());
                String segsss = segatt.getText();
                if (p_name.equals(GxmlNames.PH)
                        && "sub".equals(p_attributes.get("type"))
                        && segsss.endsWith("/>"))
                {
                    m_addsText = false;
                    m_isPhSub = true;
                }
                else
                {
                    m_addsText = false;

                    // make the tag empty tag
                    String tag = p_originalString.substring(0,
                            p_originalString.length() - 1)
                            + "/>";

                    segatt.appendText(tag);
                }
            }
            else
            {
                // non conforming tags
                //                  String[] params = new String[1];
                //                  params[0] = p_name;
                //                  throw new LingManagerException(
                //                      "NonConformingGxmlTag", params, null);
                throw new DiplomatBasicParserException(
                    "Found non conforming gxml tag " + p_name);
            }
        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            if (m_isPhSub && p_name.equals(GxmlNames.PH))
            {
                m_addsText = false;
                m_isPhSub = false;
            }
            else if (p_name.equals(GxmlNames.SUB))
            {
                m_addsText = false;
                SegmentAttributes segAtt =
                    (SegmentAttributes)m_currentSegment.pop();

                segAtt.appendText(getTopLevelEndTag(
                    segAtt.isTranslatable()));
            }
            else if (p_name.equals(GxmlNames.SEGMENT) ||
                p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_addsText = true;

                // This tag should be the end of the text
                ((SegmentAttributes)m_currentSegment.peek()).appendText(
                    p_originalTag);
            }
            else // bpt, ept, it, ph
            {
                m_addsText = true;
            }
        }

        public Collection getSegments()
        {
            return m_segments;
        }


        private SegmentAttributes getSegmentAttributes(Properties p_attributes)
            throws DiplomatBasicParserException
        {
            String format = p_attributes.getProperty(GxmlNames.SUB_DATATYPE);
            if (format == null)
            {
                format = m_tu.getFormat();
            }

            String type = p_attributes.getProperty(GxmlNames.SUB_TYPE);
            if (type == null)
            {
                type = m_tu.getType();
            }

            boolean isTranslatable;
            String locType = p_attributes.getProperty(
                GxmlNames.SUB_LOCTYPE);
            if (locType == null)
            {
                isTranslatable = m_tu.isTranslatable();
            }
            else
            {
                isTranslatable = locType.equals("translatable");
            }

            String subId = p_attributes.getProperty(GxmlNames.SUB_ID);
            if (subId == null)
            {
                // throw new LingManagerException("NoSubId", null, null);
                throw new DiplomatBasicParserException(
                    "No sub id is found in a segment.");
            }

            SegmentAttributes segAtt = new SegmentAttributes(
                format, type, isTranslatable, subId);

            String wordCount = p_attributes.getProperty(
                GxmlNames.SUB_WORDCOUNT);
            if (wordCount == null)
            {
                wordCount = "0";
            }

            String topTag = null;
            if (isTranslatable)
            {
                topTag = "<segment wordcount=\"" + wordCount + "\">";
            }
            else
            {
                topTag = "<localizable wordcount=\"" + wordCount + "\">";
            }

            segAtt.appendText(topTag);

            try
            {
                segAtt.setWordCount(Integer.parseInt(wordCount));
            }
            catch (NumberFormatException ex)
            {
                throw new DiplomatBasicParserException(
                    "Word count format error: " + ex.getMessage());
            }

            return segAtt;
        }

        private String getTopLevelEndTag(boolean p_isTranslatable)
        {
            return p_isTranslatable ? "</segment>" : "</localizable>";
        }
    }


    // used in getSegmentNoTopTag
    static private class NoTopTagHandler
        extends TuvSegmentBaseHandler
    {
        private StringBuffer m_text = new StringBuffer();

        // Overridden method
        public void handleText(String p_text)
        {
            // Text is not decoded
            m_text.append(p_text);
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_attributes,
            String p_originalString)
        {
            if (!p_name.equals(GxmlNames.SEGMENT) &&
                !p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_text.append(p_originalString);
            }
        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            if (!p_name.equals(GxmlNames.SEGMENT) &&
                !p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_text.append(p_originalTag);
            }
        }

        public String toString()
        {
            return m_text.toString();
        }
    }


    public class SegmentAttributes
    {
        private String m_format; // in lowercase, html, xml, plaintext, etc
        private String m_type;   // type of the segment. (text, url-a, etc)
        private boolean m_translatable; // translatable or localizable
        private String m_subId;
        private int m_wordCount;

        private StringBuffer m_segment;

        public SegmentAttributes(String p_format,
            String p_type, boolean p_translatable, String p_subId)
        {
            m_format = p_format;
            m_type = p_type;
            m_translatable = p_translatable;
            m_subId = p_subId;
            m_wordCount = 0;
            m_segment = new StringBuffer();
        }

        public void appendText(String p_text)
        {
            m_segment.append(p_text);
        }

        public String getText()
        {
            return m_segment.toString();
        }

        public String getFormat()
        {
            return m_format;
        }

        public String getType()
        {
            return m_type;
        }

        public boolean isTranslatable()
        {
            return m_translatable;
        }

        public String getSubId()
        {
            return m_subId;
        }

        public void setWordCount(int p_wordCount)
        {
            m_wordCount = p_wordCount;
        }

        public int getWordCount()
        {
            return m_wordCount;
        }
    }
    
    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

	public Timestamp getLastUsageDate() {
		return lastUsageDate;
	}

	public void setLastUsageDate(Timestamp p_lastUsageDate) {
		this.lastUsageDate = p_lastUsageDate;
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long p_jobId) {
		this.jobId = p_jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public long getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(long previousHash) {
		this.previousHash = previousHash;
	}

	public long getNextHash() {
		return nextHash;
	}

	public void setNextHash(long nextHash) {
		this.nextHash = nextHash;
	}
}

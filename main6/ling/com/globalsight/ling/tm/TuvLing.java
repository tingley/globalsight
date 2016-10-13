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
package com.globalsight.ling.tm;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.Stack;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.ExactMatchFormatHandler;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.TuvSegmentBaseHandler;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlNames;

/**
 * Please add documentation.
 */
abstract public class TuvLing extends PersistentObject implements Serializable,
        TuvLingConstants
{
    private static final long serialVersionUID = 6133431234756026206L;

    // Cache field for formatted strings
    protected String m_exactMatchFormat = null;
    protected String m_fuzzyMatchFormat = null;
    protected String m_termMatchFormat = null;
    protected String m_gxmWithoutTagsFormat = null;

    /**
     * Get the native formatted string - used to generate CRCs.
     * 
     * @return The native formatted string..
     */
    public String getExactMatchFormat()
    {
        if (m_exactMatchFormat == null)
        {
            ExactMatchFormatHandler handler = new ExactMatchFormatHandler();
            DiplomatBasicParser diplomatParser = new DiplomatBasicParser(
                    handler);

            try
            {
                diplomatParser.parse(getGxml());
            }
            catch (DiplomatBasicParserException e)
            {
                throw new RuntimeException(e);
            }

            m_exactMatchFormat = handler.toString();
        }

        return m_exactMatchFormat;
    }

    /**
     * Get translatable and localizable content only from the Tuv - used to
     * generate fuzzy indexes. That is, the text plus subflows in order.
     * 
     * @return tuv without formatting.
     */
    public String getFuzzyMatchFormat()
    {
        if (m_fuzzyMatchFormat == null)
        {
            FuzzyMatchFormatHandler handler = new FuzzyMatchFormatHandler();
            DiplomatBasicParser diplomatParser = new DiplomatBasicParser(
                    handler);

            try
            {
                diplomatParser.parse(getGxml());
            }
            catch (DiplomatBasicParserException e)
            {
                throw new RuntimeException(e);
            }

            m_fuzzyMatchFormat = handler.toString();

            // Fri Apr 12 16:33:22 2002 CvdL: GSDEF 6275. For fuzzy
            // matching, normalize whitespace in the TUV regardless of
            // the data type the TU might have. The match result will
            // be a 100% fuzzy match and post-processing will
            // determine if it is an exact match (whitespace matches
            // exactly) or needs to be penalized to 99% fuzzy match
            // (mismatching whitespace penalty).
            m_fuzzyMatchFormat = Text.normalizeWhiteSpaces(m_fuzzyMatchFormat);
        }

        return m_fuzzyMatchFormat;
    }

    /**
     * Get translatable content only from the Tuv - used to generate term
     * indexes. That is, the text without translatable subflows.
     * 
     * @return tuv without formatting.
     */
    public String getTermMatchFormat()
    {
        if (m_termMatchFormat == null)
        {
            TermMatchFormatHandler handler = new TermMatchFormatHandler();
            DiplomatBasicParser diplomatParser = new DiplomatBasicParser(
                    handler);

            try
            {
                diplomatParser.parse(getGxml());
            }
            catch (DiplomatBasicParserException e)
            {
                throw new RuntimeException(e);
            }

            m_termMatchFormat = handler.toString();

            // Fri Apr 12 16:33:22 2002 CvdL: For term matching,
            // normalize whitespace in the TUV regardless of the data
            // type the TU might have. See the comment made earlier
            // in getFuzzyMatchFormat().
            m_termMatchFormat = Text.normalizeWhiteSpaces(m_termMatchFormat);
        }

        return m_termMatchFormat;
    }

    /**
     * Get a string without tags nor subflows.
     * 
     * @return Tuv without tags and subflows.
     */
    public String getGxmlWithoutTags()
    {
        if (m_gxmWithoutTagsFormat == null)
        {
            GxmlWithoutTagsHandler handler = new GxmlWithoutTagsHandler();
            DiplomatBasicParser diplomatParser = new DiplomatBasicParser(
                    handler);

            try
            {
                diplomatParser.parse(getGxml());
            }
            catch (DiplomatBasicParserException e)
            {
                throw new RuntimeException(e);
            }

            m_gxmWithoutTagsFormat = handler.toString();
        }

        return m_gxmWithoutTagsFormat;
    }

    // // setters and getters /////////////////

    /**
     * Get the GlobalSightLocale.
     * 
     * @return GlobalSightLocale of this Tuv
     */
    abstract public GlobalSightLocale getGlobalSightLocale();

    /**
     * Get the exact match key (CRC)
     * 
     * @return Exact match key of this Tuv
     */
    abstract public long getExactMatchKey();

    /**
     * Set the exact match key (CRC)
     * 
     * @param p_exactMatchKey
     *            new exact match key
     */
    abstract public void setExactMatchKey(long p_exactMatchKey);

    /**
     * Get the segment string of this Tuv
     * 
     * @return Segment string
     */
    abstract public String getGxml();

    /**
     * Set the segment string of this Tuv
     * 
     * @param p_segment
     *            Gxml String.
     */
    abstract public void setGxml(String p_segment);

    /**
     * Get the TuLing assosiated with this Tuv
     * 
     * @return TuLing
     */
    // abstract public TuLing getTuLing();

    /**
     * Test if this Tuv is completed.
     * 
     * @return true if completed, false if not.
     */
    abstract public boolean isCompleted();

    /**
     * Get the word count of this Tuv
     * 
     * @return Word count
     */
    abstract public int getWordCount();

    /**
     * Set timestamp when user last modified the segment.
     */
    abstract public void setLastModified(Date p_now);

    /**
     * Get timestamp when user last modified the segment.
     */
    abstract public Date getLastModified();

    /**
     * Get Tuv Locale identifier.
     * 
     * @return Tuv Locale identifier.
     */
    abstract public long getLocaleId();

    /**
     * Am I localizable or translatable?
     * 
     * @return true if localizable, false if translatable.
     */
    abstract public boolean isLocalizable(long jobId);

    /**
     * Returns a localize type string which is used for persistence.
     * 
     * @param p_locType
     *            localize type in int. The possible values are
     *            TuvLing.TRANSLATABLE and TuvLing.LOCALIZABLE.
     * @return Localize type string. "T" if translatable, "L" if localizable.
     */
    public static String getLocTypeString(long p_locType)
    {
        String locType = null;

        if (p_locType == TRANSLATABLE)
        {
            locType = "T";
        }
        else if (p_locType == LOCALIZABLE)
        {
            locType = "L";
        }

        return locType;
    }

    // ///////// Handler inner classes //////////

    // Used in getFuzzyMatchFormat method
    private static class FuzzyMatchFormatHandler extends TuvSegmentBaseHandler
    {
        protected StringBuffer m_content = new StringBuffer(200);
        protected Stack m_addsText = new Stack();

        // Overridden method
        public void handleStart()
        {
            m_addsText.push(Boolean.TRUE);
        }

        // Overridden method
        public void handleStop()
        {
            m_addsText.clear();
        }

        // Overridden method
        public void handleText(String p_text)
        {
            if ((Boolean) m_addsText.peek() == Boolean.TRUE)
            {
                // add only main text and subflow text (no formatting code)
                m_content.append(m_xmlDecoder.decodeStringBasic(p_text));
            }
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_atributes,
                String p_originalString)
        {
            if (p_name.equals(GxmlNames.SUB)
                    || p_name.equals(GxmlNames.SEGMENT)
                    || p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_addsText.push(Boolean.TRUE);
            }
            else
            {
                m_addsText.push(Boolean.FALSE);
            }
        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            m_addsText.pop();
        }

        public String toString()
        {
            return m_content.toString();
        }

    }

    private static class TermMatchFormatHandler extends FuzzyMatchFormatHandler
    {
        // Overridden method
        public void handleStartTag(String p_name, Properties p_atributes,
                String p_originalString)
        {
            /*
             * Fri Nov 19 22:00:29 2004 CvdL: I'm only interested the main
             * segment. boolean isTranslatableSub =
             * (p_name.equals(GxmlNames.SUB) &&
             * p_atributes.getProperty(GxmlNames.SUB_LOCTYPE).equals(
             * GxmlNames.TRANSLATABLE));
             */

            if (/* isTranslatableSub || */
            p_name.equals(GxmlNames.SEGMENT)
                    || p_name.equals(GxmlNames.LOCALIZABLE))
            {
                m_addsText.push(Boolean.TRUE);
            }
            else
            {
                m_addsText.push(Boolean.FALSE);
            }
        }
    }

    // Used in getGxmlWithoutTags method
    private static class GxmlWithoutTagsHandler extends TuvSegmentBaseHandler
    {
        private StringBuffer m_content = new StringBuffer(200);
        private int m_nestLevel = 0;

        // Overridden method
        public void handleStart()
        {
            m_nestLevel = 0;
        }

        // Overridden method
        public void handleText(String p_text)
        {
            // A segment is supposed to be wrapped with one tag,
            // <segment> or <localized>
            if (m_nestLevel == 1)
            {
                // add only main text (no formatting code nor subflow)
                m_content.append(m_xmlDecoder.decodeStringBasic(p_text));
            }
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_atributes,
                String p_originalString)
        {
            m_nestLevel++;
        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            m_nestLevel--;
        }

        public String toString()
        {
            return m_content.toString();
        }

    }

    protected static String stateId2StateName(int p_stateId)
    {
        String name = null;

        switch (p_stateId)
        {
            case NOT_LOCALIZED:
                name = NOT_LOCALIZED_NAME;
                break;
            case LOCALIZED:
                name = LOCALIZED_NAME;
                break;
            case OUT_OF_DATE:
                name = OUT_OF_DATE_NAME;
                break;
            case COMPLETE:
                name = COMPLETE_NAME;
                break;
            case EXACT_MATCH_LOCALIZED:
                name = EXACT_MATCH_LOCALIZED_NAME;
                break;
            case LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED:
                name = LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED_NAME;
                break;
            case ALIGNMENT_LOCALIZED:
                name = ALIGNMENT_LOCALIZED_NAME;
                break;
            default:
                name = UNKNOWN_NAME;
        }

        return name;
    }

    protected static int stateName2StateId(String p_stateName)
    {
        int id = UNKNOWN;

        if (p_stateName.equals(NOT_LOCALIZED_NAME))
        {
            id = NOT_LOCALIZED;
        }
        else if (p_stateName.equals(LOCALIZED_NAME))
        {
            id = LOCALIZED;
        }
        else if (p_stateName.equals(OUT_OF_DATE_NAME))
        {
            id = OUT_OF_DATE;
        }
        else if (p_stateName.equals(COMPLETE_NAME))
        {
            id = COMPLETE;
        }
        else if (p_stateName.equals(EXACT_MATCH_LOCALIZED_NAME))
        {
            id = EXACT_MATCH_LOCALIZED;
        }
        else if (p_stateName.equals(LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED_NAME))
        {
            id = LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED;
        }
        else if (p_stateName.equals(ALIGNMENT_LOCALIZED_NAME))
        {
            id = ALIGNMENT_LOCALIZED;
        }
        else
        {
            id = UNKNOWN;
        }
        return id;
    }
}

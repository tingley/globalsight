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
package com.globalsight.everest.segmentationhelper;

import java.util.*;

/**
 * The SrxHeader provides a datastructure to hold the header element's
 * information of one segmentation rule file writen in xml format.
 * 
 * @author holden.cai
 * 
 */

public class SrxHeader
{

    /**
     * segmentationsubflows attribute of header element, m_isSegmentsubflows
     * will be ture if the value of segmentsubflows is "yes", and false if "no"
     */
    private boolean m_isSegmentsubflows;

    /**
     * cascade attribute of header element, m_isCascade will be true if the
     * value of cascade is "yes", and false if "no"
     */
    private boolean m_isCascade;

    /**
     * To hold information of formathandle elements.
     */
    private HashMap<String, String> formatHandle = new HashMap<String, String>();

    /**
     * True to tell segmenter that a segment should include the whole text while
     * the text just has one segment
     */
    private boolean oneSegmentIncludesAll = false;

    /**
     * Trim the leading white-spaces of segments or not
     */
    private boolean trimLeadingWhitespaces = false;

    /**
     * Trim the trailing white-spaces of segments or not
     */
    private boolean trimTrailingWhitespaces = false;

    /**
     * sample text for current SRX rule
     */
    private String sample = null;

    /**
     * the language code applied by sample. Null or empty strings are changed to
     * the default language.
     */
    private String sampleLanguage = null;

    /**
     * indicates if all languages rules for the given language should be applied
     * on the sample text. If the value is no only the language rules currently
     * displayed should be used.
     */
    private boolean useMappedRulesForSample = false;

    /**
     * the specified regular expression patter should be searched on the text
     * unit being segmented and if found, a segment corresponding to the
     * matching content should be made a segment, overriding normal segmentation
     * rules.
     */
    private String rangeRule = null;

    /**
     * @param segmentsubflows
     * @param cascade
     * @param formatHandle
     */
    public SrxHeader()
    {
        m_isSegmentsubflows = false;
        m_isCascade = false;
        formatHandle = null;
    }

    public SrxHeader(boolean p_segmentsubflows, boolean p_cascade,
            HashMap<String, String> p_formatHandle)
    {
        m_isSegmentsubflows = p_segmentsubflows;
        m_isCascade = p_cascade;
        formatHandle = p_formatHandle;
    }

    public HashMap<String, String> getFormatHandle()
    {
        return formatHandle;
    }

    public void setFormatHandle(HashMap<String, String> formatHandle)
    {
        this.formatHandle = formatHandle;
    }

    public boolean isCascade()
    {
        return m_isCascade;
    }

    /**
     * @deprecated Use {@link #isCascade(boolean)} instead
     */
    public void setCascade(boolean cascade)
    {
        isCascade(cascade);
    }

    public void isCascade(boolean cascade)
    {
        m_isCascade = cascade;
    }

    public boolean isSegmentsubflows()
    {
        return m_isSegmentsubflows;
    }

    /**
     * @deprecated Use {@link #isSegmentsubflows(boolean)} instead
     */
    public void setSegmentsubflows(boolean segmentsubflows)
    {
        isSegmentsubflows(segmentsubflows);
    }

    public void isSegmentsubflows(boolean segmentsubflows)
    {
        m_isSegmentsubflows = segmentsubflows;
    }

    public boolean isOneSegmentIncludesAll()
    {
        return oneSegmentIncludesAll;
    }

    public void setOneSegmentIncludesAll(boolean oneSegmentIncludesAll)
    {
        this.oneSegmentIncludesAll = oneSegmentIncludesAll;
    }

    public boolean isTrimLeadingWhitespaces()
    {
        return trimLeadingWhitespaces;
    }

    public void setTrimLeadingWhitespaces(boolean trimLeadingWhitespaces)
    {
        this.trimLeadingWhitespaces = trimLeadingWhitespaces;
    }

    public boolean isTrimTrailingWhitespaces()
    {
        return trimTrailingWhitespaces;
    }

    public void setTrimTrailingWhitespaces(boolean trimTrailingWhitespaces)
    {
        this.trimTrailingWhitespaces = trimTrailingWhitespaces;
    }

    public String getSample()
    {
        return sample;
    }

    public void setSample(String sample)
    {
        this.sample = sample;
    }

    public String getSampleLanguage()
    {
        return sampleLanguage;
    }

    public void setSampleLanguage(String sampleLanguage)
    {
        this.sampleLanguage = sampleLanguage;
    }

    public boolean isUseMappedRulesForSample()
    {
        return useMappedRulesForSample;
    }

    public void setUseMappedRulesForSample(boolean useMappedRulesForSample)
    {
        this.useMappedRulesForSample = useMappedRulesForSample;
    }

    public String getRangeRule()
    {
        return rangeRule;
    }

    public void setRangeRule(String rangeRule)
    {
        this.rangeRule = rangeRule;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("m_isSegmentsubflows: ");
        sb.append(m_isSegmentsubflows + "\n");
        sb.append("m_isCascade: ");
        sb.append(m_isCascade + "\n");
        Set<String> format = formatHandle.keySet();
        Iterator<String> formatIter = format.iterator();
        while (formatIter.hasNext())
        {
            String type = formatIter.next();
            String include = formatHandle.get(type);
            sb.append(type + " = " + include + "\n");
        }
        return sb.toString();
    }

}

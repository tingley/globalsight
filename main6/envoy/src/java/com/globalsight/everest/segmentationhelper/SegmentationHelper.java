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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.Segmenter;

public class SegmentationHelper
{
    /**
     * This method is used to segment text accoding to segmentation rule text,
     * and return an String[] filled with segments after segmentation
     * 
     * @param p_segmentationRuleText
     * @param p_locale
     * @param p_textToSegment
     * @return
     */
    public static String[] segment(String p_segmentationRuleText, Locale p_locale, String p_text)
            throws Exception
    {
        SegmentationRule segmentationRule = XmlLoader.parseSegmentationRule(p_segmentationRuleText);

        return segment(segmentationRule, p_locale.getLanguage() + "_" + p_locale.getCountry(),
                p_text);
    }

    public static String[] segment(SegmentationRule p_segmentationRule, String p_locale,
            String p_text) throws Exception
    {
        Segmentation segmentation = new Segmentation();
        segmentation.setLocale(p_locale);
        segmentation.setTranslable(p_text);
        segmentation.setSegmentationRule(p_segmentationRule);
        String[] segments = segmentation.doSegmentation();
        String[] newSegments = Segmentation.handleSrxExtension(p_segmentationRule, segments);

        return newSegments;
    }

    public static String[] segmentWithDefault(Locale p_locale, String p_text)
            throws Exception
    {
        SegmentationHelper shelper = new SegmentationHelper();
        Segmenter m_defaultSegmenter = new Segmenter(p_locale);
        
        m_defaultSegmenter.setText(p_text);
        
        ArrayList breakPositions = new ArrayList();
        
        int iStart = m_defaultSegmenter.first();
        for (int iEnd = m_defaultSegmenter.next(); iEnd != Segmenter.DONE; iStart = iEnd, iEnd = m_defaultSegmenter
                .next())
        {
            // System.err.println("--> `" +
            // m_segmentWithoutTags.substring(iStart, iEnd) + "'");

            BreakPosition pos = shelper.new BreakPosition(iEnd);
            breakPositions.add(pos);
        }
        
        // split segments
        return splitOriSegment(breakPositions, p_text);
    }
    
    private static String[] splitOriSegment(ArrayList p_breaks, String p_text)
    {
        List<String> result = new ArrayList<String>();

        // offsets in m_segment
        int iStart = 0;
        int iEnd;

        // If the section was empty, restore the original input.
        if (p_breaks.size() == 0)
        {
            result.add(p_text);
        }

        else
        {
            // Else compute the segment breaks in the original input and
            // add the segments to the segment list.
            for (Iterator it = p_breaks.iterator(); it.hasNext();)
            {
                BreakPosition pos = (BreakPosition) it.next();

                iEnd = pos.m_split;

                if (iEnd > p_text.length())
                {
                    result.add(p_text.substring(iStart));
                }
                else
                {
                    result.add(p_text.substring(iStart, iEnd));
                }

                iStart = iEnd;
            }
        }

        String[] strs = new String[result.size()];
        return result.toArray(strs);
    }
    
    final private class BreakPosition
    {
        // Offset of segment boundary
        public int m_split = 0;

        public BreakPosition(int p_pos)
        {
            m_split = p_pos;
        }
    }
}

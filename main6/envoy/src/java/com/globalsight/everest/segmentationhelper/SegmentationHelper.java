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

import java.util.Locale;

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

}

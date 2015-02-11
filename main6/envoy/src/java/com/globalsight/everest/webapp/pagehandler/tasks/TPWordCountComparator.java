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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.util.Locale;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;

/**
 * This class can be used to compare TargetPage objects
 */
public class TPWordCountComparator extends StringComparator
{
    private static final long serialVersionUID = 1L;

    //types of Task comparison
    public static final int FILE_NAME           = 0;
    public static final int EXACT               = 1;
    public static final int BAND1               = 2;
    public static final int BAND2               = 3;
    public static final int BAND3               = 4;
    public static final int BAND4               = 5;
    public static final int NO_MATCH            = 6;
    public static final int REPETITIONS         = 7;
    public static final int CONTEXT             = 8;
    public static final int WC_TOTAL            = 9;
    public static final int TOTAL_FUZZY         = 10;
    public static final int IN_CONTEXT          = 11;
    public static final int NO_USE_IN_CONTEXT   = 12;
    public static final int TOTAL_EXACT        = 13;
    public static final int DEFAULT_CONTEXT_EXACT = 14;
    
    public TPWordCountComparator(Locale p_locale)
    {
        super(p_locale);
    }


    /**
     * Performs a comparison of two Task objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        TargetPage a = (TargetPage)p_A;
        TargetPage b = (TargetPage)p_B;
        PageWordCounts aCounts = a.getWordCount();
        PageWordCounts bCounts = b.getWordCount();

        String aValue;
        String bValue;
        int aInt;
        int bInt;
        int rv;

        switch (m_type)
        {
        default:
        case FILE_NAME:
            boolean isExtracted = a.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
            if (isExtracted)
            {
                aValue = a.getExternalPageId();
            }
            else
            {
                UnextractedFile unextractedFile = (UnextractedFile)a.getPrimaryFile();
                aValue = unextractedFile.getStoragePath();
            }

            isExtracted = b.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
            if (isExtracted)
            {
                bValue = b.getExternalPageId();
            }
            else
            {
                UnextractedFile unextractedFile = (UnextractedFile)b.getPrimaryFile();
                bValue = unextractedFile.getStoragePath();
            }

            String aMainName = this.getMainFileName(aValue);
            String aSubName = this.getSubFileName(aValue);
            String bMainName = this.getMainFileName(bValue);
            String bSubName = this.getSubFileName(bValue);

            rv = this.compareStrings(aMainName, bMainName);
            if (rv == 0)
            {
                rv = this.compareStrings(aSubName, bSubName);
            }
            break;

        case EXACT:
            aInt = aCounts.getSegmentTmWordCount();
            bInt = bCounts.getSegmentTmWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case BAND1:
            aInt = aCounts.getHiFuzzyWordCount();
            bInt = bCounts.getHiFuzzyWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case BAND2:
            aInt = aCounts.getMedHiFuzzyWordCount();
            bInt = bCounts.getMedHiFuzzyWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case BAND3:
            aInt = aCounts.getMedFuzzyWordCount();
            bInt = bCounts.getMedFuzzyWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case BAND4:
            aInt = aCounts.getLowFuzzyWordCount();
            bInt = bCounts.getLowFuzzyWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case NO_MATCH:  // JPF- fix when have back end
            aInt = aCounts.getNoMatchWordCount();
            bInt = bCounts.getNoMatchWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case REPETITIONS:
            aInt = aCounts.getRepetitionWordCount();
            bInt = bCounts.getRepetitionWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case CONTEXT:
            aInt = aCounts.getContextMatchWordCount();
            bInt = bCounts.getContextMatchWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;
        case DEFAULT_CONTEXT_EXACT:
            aInt = aCounts.getSegmentTmWordCount() - aCounts.getContextMatchWordCount();
            bInt = bCounts.getSegmentTmWordCount() - bCounts.getContextMatchWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case NO_USE_IN_CONTEXT:
            aInt = aCounts.getNoUseInContextMatchWordCount();
            bInt = bCounts.getNoUseInContextMatchWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case TOTAL_EXACT:
            aInt = aCounts.getTotalExactMatchWordCount();
            bInt = bCounts.getTotalExactMatchWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case IN_CONTEXT:
            aInt = aCounts.getInContextWordCount();
            bInt = bCounts.getInContextWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case WC_TOTAL:
            aInt = aCounts.getTotalWordCount();
            bInt = bCounts.getTotalWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;

        case TOTAL_FUZZY:
            aInt = (aCounts.getLowFuzzyWordCount() + 
                        aCounts.getMedFuzzyWordCount() + 
                        aCounts.getMedHiFuzzyWordCount() +
                        aCounts.getHiFuzzyWordCount());

            bInt = (bCounts.getLowFuzzyWordCount() + 
                    bCounts.getMedFuzzyWordCount() + 
                    bCounts.getMedHiFuzzyWordCount() +
                    bCounts.getHiFuzzyWordCount());
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
            break;
        }

        return rv;
    }
}


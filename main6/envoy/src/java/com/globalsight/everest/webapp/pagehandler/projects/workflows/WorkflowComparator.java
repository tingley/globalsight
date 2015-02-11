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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.workflowmanager.Workflow;

/**
* This class can be used to compare Workflow objects
*/
public class WorkflowComparator extends StringComparator
{
    private static final long serialVersionUID = -1449607179643674302L;
    
    //types of Workflow comparison
    public static final int TARG_LOCALE         = 0;
    public static final int COMPLETE            = 1;
    public static final int PLANNED_DATE        = 2;
    public static final int EXACT               = 3;// 100%
    public static final int BAND1               = 4;// 95%-99%
    public static final int BAND2               = 5;// 85%-94%
    public static final int BAND3               = 6;// 75%-84%
    public static final int BAND4               = 7;// 50%-74%
    public static final int NO_MATCH            = 8;// no match
    public static final int REPETITIONS         = 9;// all repetitions that covers not all 100% segments
    public static final int CONTEXT             = 10;
    public static final int WC_TOTAL            = 11;
    public static final int TOTAL_FUZZY         = 14;
    public static final int TARG_LOCALE_SIMPLE  = 24;

    // For sla report issue
    public static final int ESTIMATED_COMP_DATE = 15;
    public static final int ESTIMATED_TRANSLATE_COMP_DATE = 16;
    
    public static final int IN_CONTEXT          = 17;
    public static final int NO_USE_IN_CONTEXT   = 18;
    public static final int TOTAL_EXACT        = 19;
    public static final int DEFAULT_CONTEXT_EXACT = 20;
    
    public static final int HIFUZZYREPETITION   = 21;
    public static final int MEDHIFUZZYREPETITION   = 22;
    public static final int MEDFUZZYREPETITION   = 23;

    public WorkflowComparator(Locale p_locale)
    {
        super(p_locale);
    }
    
    public WorkflowComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
    * Performs a comparison of two Workflow objects.
    */
    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        Workflow a = (Workflow) p_A;
        Workflow b = (Workflow) p_B;

        String aValue;
        String bValue;
        int rv = 0;

        switch (m_type)
        {
            default:
            case TARG_LOCALE:
                aValue = a.getTargetLocale().getDisplayName(getLocale());
                bValue = b.getTargetLocale().getDisplayName(getLocale());
                rv = this.compareStrings(aValue,bValue);
                break;
            case COMPLETE:
                int aInt = a.getPercentageCompletion();
                int bInt = b.getPercentageCompletion();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case PLANNED_DATE:
                Date aDate = a.getPlannedCompletionDate();
                Date bDate = b.getPlannedCompletionDate();
                rv = aDate.compareTo(bDate);
                break;
            case ESTIMATED_COMP_DATE:
                Date aDate2 = a.getEstimatedCompletionDate();
                Date bDate2 = b.getEstimatedCompletionDate();
                rv = aDate2.compareTo(bDate2);
                break;
            case ESTIMATED_TRANSLATE_COMP_DATE:
                Date aDate3 = a.getEstimatedTranslateCompletionDate();
                Date bDate3 = b.getEstimatedTranslateCompletionDate();
                
                if ((aDate3 == null) && (bDate3 == null))
                {
                    rv = 0;
                }
                else if ((aDate3 != null) && (bDate3 == null))
                {
                    rv = 1;
                }
                else if ((aDate3 == null) && (bDate3 != null))
                {
                    rv = -1;
                }
                else
                {
                    rv = aDate3.compareTo(bDate3);
                }
                
                break;
            case EXACT:
                aInt = a.getSegmentTmWordCount();
                bInt = b.getSegmentTmWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case BAND1:  
                aInt = a.getHiFuzzyMatchWordCount();
                bInt = b.getHiFuzzyMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case BAND2:  
                aInt = a.getMedHiFuzzyMatchWordCount();
                bInt = b.getMedHiFuzzyMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case BAND3:  
                aInt = a.getMedFuzzyMatchWordCount();
                bInt = b.getMedFuzzyMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case BAND4:  
                aInt = a.getLowFuzzyMatchWordCount();
                bInt = b.getLowFuzzyMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case NO_MATCH:
                aInt = a.getNoMatchWordCount();
                bInt = b.getNoMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case REPETITIONS:
                aInt = a.getRepetitionWordCount();
                bInt = b.getRepetitionWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case CONTEXT:
                aInt = a.getContextMatchWordCount();
                bInt = b.getContextMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case DEFAULT_CONTEXT_EXACT:
                aInt = a.getSegmentTmWordCount() - a.getContextMatchWordCount();
                bInt = b.getSegmentTmWordCount() - b.getContextMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt) 
                    rv = 0;
                else
                    rv = -1;
                break;
            case IN_CONTEXT:  
                aInt = a.getInContextMatchWordCount();
                bInt = b.getInContextMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case NO_USE_IN_CONTEXT:  
                aInt = a.getNoUseInContextMatchWordCount();
                bInt = b.getNoUseInContextMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;  
            case TOTAL_EXACT:
                aInt = a.getTotalExactMatchWordCount();
                bInt = b.getTotalExactMatchWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case WC_TOTAL:
                aInt = a.getTotalWordCount();
                bInt = b.getTotalWordCount();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;

            case TOTAL_FUZZY:
                aInt = (a.getLowFuzzyMatchWordCount() + 
                        a.getMedFuzzyMatchWordCount() + 
                        a.getMedHiFuzzyMatchWordCount() +
                        a.getHiFuzzyMatchWordCount());

                bInt = (b.getLowFuzzyMatchWordCount() + 
                        b.getMedFuzzyMatchWordCount() + 
                        b.getMedHiFuzzyMatchWordCount() +
                        b.getHiFuzzyMatchWordCount());
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
            case TARG_LOCALE_SIMPLE:
                aValue = a.getTargetLocale().toString();
                bValue = b.getTargetLocale().toString();
                rv = this.compareStrings(aValue,bValue);
                break;
        }
        return rv;
    }
}

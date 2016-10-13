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
package com.globalsight.everest.page.pageimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GlobalSightLocale;

/**
 * Interface to persist extracted or un-extracted files.
 * 
 * @author YorkJin
 * @since 2011-09-16
 * @version 8.2.1
 */
public interface TargetPagePersistence
{
    public Collection<TargetPage> persistObjectsWithExtractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales,
            TermLeverageResult p_termMatches, boolean p_useLeveragedSegments,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException;
    
    public Collection<TargetPage> persistObjectsWithUnextractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException;

    public Collection<TargetPage> persistFailedObjectsWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocaleErrors)
            throws PageException;
    
    /**
     * For all source TUVs, generates target TUVs in the requested locale and
     * populates them with 100% exact matches from TM leveraging, or copies of
     * the original source TUV. If terminology matches are passed in, then
     * automatic term replacement is performed.
     * 
     * @return a List containing all target TUVs for the requested locale.
     */
    public ArrayList<Tuv> getTargetTuvs(SourcePage p_sourcePage,
            HashMap<Tu, Tuv> p_sourceTuvMap, GlobalSightLocale p_targetLocale,
            TermLeverageResult p_termMatches, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException;
}

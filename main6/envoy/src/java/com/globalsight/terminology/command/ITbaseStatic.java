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

package com.globalsight.terminology.command;

import java.util.ArrayList;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.ling.lucene.Index;

public interface ITbaseStatic
{
    public void setConceptIndex(Index m_conceptLevelFulltextIndex);
    
    public void setFullTextIndex(ArrayList m_fulltextIndexes);
    
    public void setFuzzyIndex(ArrayList m_fuzzyIndexes);
    /**
     * Retrieves a string containing termbase statistics.
     * 
     * @return an xml string of the form <statistics> <termbase>termbase name</termbase>
     *         <concepts>number of entries</concepts> <terms>number of overall
     *         terms</terms> <indexes> <index> <language>index name</language>
     *         <terms>number of terms</terms> </index> </indexes> </statistics>
     */
    public String getStatistics(long tb_id) throws TermbaseException;
    
    public String getStatisticsNoIndex(long tb_id) throws TermbaseException;
}

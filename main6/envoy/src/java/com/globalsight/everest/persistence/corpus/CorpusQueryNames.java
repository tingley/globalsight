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
package com.globalsight.everest.persistence.corpus;

/**
 * Specifies the names of all the named queries for this package
 */
public interface CorpusQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES 
    //
    
    /**
     * A named query to return a corpus doc based on the id.
     * <p>
     * Arguments: 1: corpus doc (corpus_unit_variant) Id.
     */
    public static String CORPUS_DOC_BY_ID = "corpusDocById";

    /**
     * A named query to return a corpus doc based on the name.
     * <p>
     * Arguments: 1: corpus doc group name.
     */
    public static String CORPUS_DOC_BY_NAME = "corpusDocByName";


    /**
     * A named query to return a corpus doc group based on the id.
     * <p>
     * Arguments: 1: corpus doc group (corpus_unit) Id.
     */
    public static String CORPUS_DOC_GROUP_BY_ID = "corpusDocGroupById";


    /**
     * A named query to return a corpus context based on the tuv id.
     * <p>
     * Arguments: 1: tuv id (project_tm_tuv_t id)
     */
    public static String CORPUS_CONTEXT_BY_TUV_ID = "corpusContextByTuvId";

    /**
     * A named query to return a corpus context based on the tu id and locale
     * <p>
     * Arguments: 1: tu id (project_tm_tu_t id)
     * Arguments: 2: locale_id 
     * Arguments: 3: cu_id
     */
    public static String CORPUS_CONTEXT_BY_TU_LOCALE_CU = "corpusContextByTuAndLocaleAndCu";


    /**
     * A named query to return a corpus context based on the cuv id.
     * <p>
     * Arguments: 1: cuv id (corpus_unit_variant id)
     */
    public static String CORPUS_CONTEXT_BY_CUV_ID = "corpusContextByCuvId";
}


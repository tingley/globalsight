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
package com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile;


public interface SgmlRuleConstants
{
    
    //////////////////////////////////////////////////////////////////////
    //  The following are for list of elements
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant that identifies the page number (i.e. first page means
     * the first 20 elements to be displayed).
     */
    public static final String PAGE_NUM = "elemPageNum";

    /**
     * String
     */
    public static final String NUM_PER_PAGE_STR = "elemNumPerPage";

    /**
     * Size of list
     */
    public static final String LIST_SIZE = "elemSize";

    /**
     * what type of sorting to use
     */
    public static final String SORTING = "elemSorting";

    /**
     * reverse sort
     */
    public static final String REVERSE_SORT = "elemReverseSort";

    /**
     * the last page number
     */
    public static final String LAST_PAGE_NUM = "elemLastPageNum";

    /**
     * number of pages 
     */
    public static final String NUM_PAGES = "elemNumPages";

    /**
     * list names
     */
    public static final String ELEM_LIST = "elems";
}

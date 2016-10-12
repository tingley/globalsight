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

package com.globalsight.cxe.persistence.segmentationrulefile;

/**
 * Specifies the names of all the named queries for SegmentationRuleFile.
 */
public interface SegmentationRuleFileQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all Segmentation Rule Files.
     * <p>
     * Arguments: none.
     */
    public static String ALL_SEGMENTATION_RULE_FILES = "getAllSegmentationRuleFiles";

    /**
     * A named query to return the Segmentation Rule File with the given id.
     * <p>
     * Arguments: Segmentation Rule File id.
     */
    public static String SEGMENTATION_RULE_FILE_BY_ID = "getSegmentationRuleFileById";


}

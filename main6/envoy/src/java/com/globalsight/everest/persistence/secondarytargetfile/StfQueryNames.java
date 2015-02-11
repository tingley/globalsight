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
package com.globalsight.everest.persistence.secondarytargetfile;

/**
 * Specifies the names of all the named queries for this package
 */
public interface StfQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES 
    //
    /**
     * A named query to return an STF based on the id.
     * <p>
     * Arguments: 1: Secondary Target File Id.
     */
    public static String STF_BY_ID = "StfById";

    /**
     * A named query to return a list of STFs based on the workflow id.
     * <p>
     * Arguments: 1: Workflow Id.
     */
    public static String STF_BY_WORKFLOW_ID = "StfByWorkflowId";

    /**
     * A named query to return a list of STFs based on the workflow id 
     * and relative path.
     * <p>
     * Arguments: 1: Workflow Id.
     *            2: Relative Path
     */
    public static String STF_BY_WF_ID_AND_RELATIVE_PATH = "StfByWfIdAndRelativePath";
    
}

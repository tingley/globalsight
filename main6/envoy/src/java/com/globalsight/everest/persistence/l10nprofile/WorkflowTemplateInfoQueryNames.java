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
package com.globalsight.everest.persistence.l10nprofile;

/**
 * Specifies the names of all the named queries for WorkflowTemplateInfo.
 */
public interface WorkflowTemplateInfoQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available workflow template names.
     * <p>
     * Arguments: None.
     */
    public static String ALL_WF_TEMPLATE_INFOS = "getAllWfTemplateInfos";

    
    /**
     * A named query to return a Workflow Template Info based on its id
     * <p>
     * Arguments: 1: WorkflowTemplateInfo Id.
     */
    public static String WFT_INFO_BY_ID = "getWfTemplateById";

    /** 
     * A named query to return all active Workflow Templates associated with a 
     * particular locale pair.
     * <p>
     * Arguments: 1 - source locale id
     *            2 - target locale id
     *            3 - company id (for fix multi company issue)
     */
    public static String TEMPLATE_INFOS_BY_LOCALE_PAIR = "getAllWfTemplateInfosByLocalePair";

    /**
     * A named query to return all active Workflow templates associated with a
     * locale pair, and project.
     * <p>
     * Arguments: 1 - source locale id
     *            2 - target locale id
     *            3 - project id
     */
    public static String TEMPLATE_INFOS_BY_FILTER = "getAllWfTemplateInfosByFilter";

    /**
     * A named query to return all active Workflow templates associated with a
     * particlule Workflow Mgr.
     * <p>
     * Arguments: 1 - Workflow Mgr id
     */
    public static String WFT_INFO_BY_WF_MGR_ID = "getAllWfTemplateInfosByWfMgrId";

    /**
     * A named query to return all active Workflow templates associated with a
     * particlular Project Manager (by the projects they are assigned to).
     * <p>
     * Arguments: 1 - Project manager id
     */
    public static String WFT_INFO_BY_PM_ID = "getAllWfTemplateInfosByProjectManagerId";

    /**
     * A named query to return all active Workflow templates associated with a
     * particlular project they are assigned to.
     * <p>
     * Arguments: 1 - Project id
     */
    public static final String WFT_INFO_BY_PROJECT_ID = "getWfTemplateInfosByProjectId";
    
    public static String WFT_INFO_BY_SOURCE_LOCALE_AND_PROJECT_ID = "getAllWfTemplateInfosBySourceLocaleAndProjectId";
}

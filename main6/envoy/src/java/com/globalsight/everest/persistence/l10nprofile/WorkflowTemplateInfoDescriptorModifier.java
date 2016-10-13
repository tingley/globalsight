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
 * WorkflowTemplateInfoDescriptorModifier extends DescriptorModifier by
 * providing amendment methods unique to the WorkflowInfo descriptor.
 */
public class WorkflowTemplateInfoDescriptorModifier
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String TABLE_NAME = "WORKFLOW_TEMPLATE";
    private static final String TABLE_NAME_L10N = "L10N_PROFILE_WFTEMPLATE_INFO";
    private static final String IS_ACTIVE_FIELD = "IS_ACTIVE";
    private static final String NO = "N";
    private static final String YES = "Y";
    private static final String ID_ARG = "idArg";

    // Add the comany id into the query parameter to fix multi company issue.
    public static final String COMPANY_ID = "companyId";
    public static final String SOURCE_LOCALE_ID = "sourceLocaleId";
    public static final String TARGET_LOCALE_ID = "targetLocaleId";
    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_MANAGER_ID = "projectManagerId";
    public static final String WORKFLOW_MANAGER_ID = "workflowManagerId";
    public static final String L10N_PROFILE_ID = "l10nProfileId";

    public static final String DEACTIVATE_TEMPLATE_SQL = "UPDATE " + TABLE_NAME
            + " SET " + IS_ACTIVE_FIELD + "='" + NO + "' where ID=:" + ID_ARG;

    public static final String ALL_ACTIVE_TEMPLATES_SQL = "SELECT * FROM "
            + TABLE_NAME + " wft WHERE wft.IS_ACTIVE = '" + YES + "'";

    public static final String TEMPLATE_BY_ID_SQL = "SELECT wft.* FROM "
            + TABLE_NAME + " wft WHERE wft.ID = :" + ID_ARG;

    public static final String TEMPLATE_BY_PROJECT_ID_SQL = "SELECT * FROM "
            + TABLE_NAME + " WHERE" + " PROJECT_ID = :" + PROJECT_ID
            + " AND IS_ACTIVE = '" + YES + "'";

    public static final String TEMPLATE_BY_PROJECT_MANAGER_ID_SQL = "SELECT wft.* FROM "
            + TABLE_NAME
            + " wft, project p WHERE wft.PROJECT_ID = p.project_seq and "
            + " p.manager_user_id = :"
            + PROJECT_MANAGER_ID
            + " AND wft.IS_ACTIVE = '" + YES + "'";

    // Get templates that can be added to an existed job
    public static final String TEMPLATE_BY_L10PROFILE_ID_SQL ="SELECT wt.* FROM "
    		+ TABLE_NAME_L10N
    		+" lpwi, "
    		+ TABLE_NAME
    		+" wt where lpwi.wf_template_id = wt.id "
    		+" AND wt.IS_ACTIVE = '"+YES+"'"
    		+" AND lpwi.L10N_PROFILE_ID = :"
    		+ L10N_PROFILE_ID;

    public static final String TEMPLATE_BY_WF_MGR_ID_SQL = "SELECT wft.* FROM "
            + TABLE_NAME + " wft, wf_template_wf_manager wftwfm "
            + "WHERE wft.id=wftwfm.workflow_template_id "
            + "and wftwfm.workflow_manager_id=:" + WORKFLOW_MANAGER_ID
            + " AND wft.IS_ACTIVE = '" + YES + "'";

    public static final String TEMPLATE_BY_PARAMETERS_SQL = "SELECT wft.* FROM "
            + TABLE_NAME
            + " wft WHERE wft.SOURCE_LOCALE_ID =:"
            + SOURCE_LOCALE_ID
            + " AND wft.TARGET_LOCALE_ID =:"
            + TARGET_LOCALE_ID
            + " AND wft.PROJECT_ID =:"
            + PROJECT_ID
            + " AND " + IS_ACTIVE_FIELD + " = '" + YES + "'";

    // Add the comany id into the query parameter to fix multi company issue.
    public static final String TEMPLATES_BY_LOCALE_PAIR_SQL = "SELECT * FROM "
            + TABLE_NAME + " WHERE SOURCE_LOCALE_ID =:" + SOURCE_LOCALE_ID
            + " AND TARGET_LOCALE_ID =:" + TARGET_LOCALE_ID
            + " AND COMPANYID =:" + COMPANY_ID + " AND " + IS_ACTIVE_FIELD
            + " = '" + YES + "'";
}

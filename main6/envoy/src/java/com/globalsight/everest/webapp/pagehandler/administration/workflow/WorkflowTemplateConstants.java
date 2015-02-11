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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;



/**
 * Graphical Workflow related constants.
 */

public interface WorkflowTemplateConstants
{
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constants used as attribute names
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a key for a list of encodings.
     */
    public static final String ENCODINGS = "encodings";

    /**
     * Constant used as a key for a list of dependencies.
     */
    public static final String DEPENDENCIES = "dependencies";

    /**
     * Constant used as a key for Leverage locale objects
     */
    public static final String LEVERAGE_OBJ = "leverageObj";

    /**
     * Constant used as a key for Leverage locale Display names
     */
    public static final String LEVERAGE_DISP = "leverageDisp";

    /**
     * Constant used as a key for a list of locale pairs.
     */
    public static final String LOCALE_PAIRS = "localePairs";

    /**
     * Constant used as a key for a list of target locales.
     */
    public static final String TARGET_LOCALES = "targetLocales";

    /**
     * Constant used as a key for a list of source locales.
     */
    public static final String SOURCE_LOCALES = "sourceLocales";

    /**
     * Constant used as a key for a list of projects.
     */
    public static final String PROJECTS = "projects";

    /**
     * Constant used as a key for a list of workflow managers.
     */
    public static final String WORKFLOW_MANAGERS = "workflowManagers";

    /**
     * Constant used in duplicate page
     */
    public static final String ALL_LOCALES = "locales";

    /**
     * Constant used as a key for a template id..
     */
    public static final String TEMPLATE_ID = "templateId";

    /**
     * Constant used as a key for a list of workflow templates.
     */
    public static final String TEMPLATES = "templates";

    /**
     * Constant used for key in TableNav tag
     */
    public static final String KEY = "template";

    /**
     * Constant used as a key for a list of labels.
     */
    public static final String LABELS = "labels";

    /**
     * Constant used as a key for a list of button labels.
     */
    public static final String BTN_LABELS = "btn_labels";

    /**
     * Constant used as a key for a list of messages.
     */
    public static final String MESSAGES = "message";

    /**
     * Constant used as a key for the job costing flag.
     */
    public static final String JOB_COSTING_ENABLED = "jobCostingEnabled";

    /**
     * Constant used as a key for the job revenue flag.
     */
    public static final String JOB_REVENUE_ENABLED = "jobRevenueEnabled";
    
    /**
     * Constant used as a key for a list of activities.
     */
    public static final String ACTIVITIES = "activities";
        
    /**
     * Constant used as a key for a particular locale pair.
     */
    public static final String LOCALE_PAIR = "localePair";

    /**
     * Constant used as a key for a particular source locale.
     */
    public static final String SOURCE_LOCALE = "sourceLocale";
    /**
     * Constant used as a key for a particular target locale.
     */
    public static final String TARGET_LOCALE = "targetLocale";
    
    /**
     * Constant used as a key for a particular target locale in select box.
     */
    public static final String TARGET_LOCALE_IN_BOX = "targetLocaleInBox";

    /**
     * Constant used as a key for the list of rates.
     */
    public static final String RATES = "rates";
    
    /**
     * Constant used as a key for the i18n content.
     */
    public static final String I18N_CONTENT = "i18n_content";

    /**
     * Constant used as a key for a role type.
     */
    public static final String ROLE = "role";

    /**
     * Constant used as a key for a list of system actions.
     */
    public static final String SYSTEM_ACTION = "systemAction";

    /**
     * Constant used as a key for user role.
     */
    public static final String USER_ROLE = "user";

    /**
     * Constant used as a key for a workflow template info object.
     */
    public static final String WF_TEMPLATE_INFO = "wfTemplateInfo";

    /**
     * Constant used as a key for a workflow template info id.
     */
    public static final String WF_TEMPLATE_INFO_ID = "wfTemplateInfoId";

    /**
     * Constant used as a key for a workflow instance.
     */
    public static final String WF_INSTANCE = "wfInstance";

    /**
     * Constant used as a key for the displayable name of a workflow instance.
     */
    public static final String WF_INSTANCE_NAME = "wfInstanceName";

    /**
     * Constant used as a key for a hashtable of WorkflowTaskInstance.
     */
    public static final String TASK_HASH = "taskHash";
    //////////////////////////////////////////////////////////////////////
    //  End: Constants used as attribute names
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: UI Fields
    //////////////////////////////////////////////////////////////////////
    // fields for the first page of template creation.
    public static final String NAME_FIELD = "nameField";
    public static final String WORKFLOW_TYPE_FIELD = "workflowTypeField";
    public static final String DESCRIPTION_FIELD = "descField";
    public static final String PROJECT_FIELD = "projectField"; 
    public static final String NOTIFICATION_FIELD = "notificationField";
    public static final String WFM_FIELD = "wfmField";
    public static final String LOCALE_PAIR_FIELD = "lpField";
    public static final String ENCODING_FIELD = "encodingField";
    public static final String COSTING_FIELD = "costingField";
    public static final String LEVERAGE_FIELD = "leverageField";
    public static final String CHOSEN_NAME = "chosenName";
    public static final String CHOSEN_WORKFLOW_TYPE = "chosenWorkflowType";
    public static final String CHOSEN_DESCRIPTION = "chosenDescription";
    public static final String CHOSEN_PROJECT = "chosenProject";
    public static final String CHOSEN_NOTIFICATION = "chosenNotification";
    public static final String CHOSEN_TARGET_ENCODING = "chosenEncoding";
    public static final String CHOSEN_LOCALE_PAIR = "chosenLocalePair";
    public static final String CHOSEN_COSTING = "chosenCosting";
    public static final String CHOSEN_WORKFLOW_MANAGERS = "chosenWorkflowManagers";
    public static final String SCORECARD_SHOW_TYPE = "scorecardShowType";
    //////////////////////////////////////////////////////////////////////
    //  End: UI Fields
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used as an action string for a request.
     */
    public static final String ACTION = "action";
    /**
     * Constant used for a new action.
     */
    public static final String NEW_ACTION = "new";
    /**
     * Constant used for a duplicate action.
     */
    public static final String DUPLICATE_ACTION = "duplicate";
    /**
     * Constant used for an edit action.
     */
    public static final String EDIT_ACTION = "edit";
    /**
     * Constant used for a cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for a leverage action.
     */
    public static final String LEVERAGE_ACTION = "leverage";
    /**
     * Constant used for a populating workflow action.
     */
    public static final String POPULATE_WORKFLOWS_ACTION = "populateWorkflows";
    /**
     * Constant used for a remove action.
     */
    public static final String REMOVE_ACTION = "remove";
    /**
     * Constant used for a export action.
     */
    public static final String IMPORT_ACTION = "import";
    /**
     * Constant used for a export action.
     */
    public static final String EXPORT_ACTION = "export";
    /**
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";
    /**
     * Constant used for a save all workflows action.
     */
    public static final String SAVE_ALL_WORKFLOWS_ACTION = "saveWorkflows";
    /**
     * Constant used for an advanced search
     */
    public static final String ADV_SEARCH_ACTION = "advancedSearch";
    /**
     * Constant used for an mini search
     */
    public static final String SEARCH_ACTION = "search";




    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for a save workflow action.
     */
    public static final String SAVE_WORKFLOW_TEMPLATE = "saveWorkflowTemplate";
    /**
     * Constant used for a save workflow action.
     */
    public static final String LOCALE_WORKFLOW_HASH = "localeWorkflowHash";
    /**
     * Constant used for a selected workflow
     */
    public static final String SELECTED_WORKFLOW = "selectedWorkflow";
}

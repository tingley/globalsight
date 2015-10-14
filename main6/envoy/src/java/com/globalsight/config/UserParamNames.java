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

package com.globalsight.config;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;

/**
 * <p>
 * This interface defines the names of user-definable parameters that users can
 * set using small option dialogs and that are stored in the USER_PARAMETER
 * table.
 * </p>
 * 
 * <p>
 * Options that affect the same component should be arranged in groups having
 * the same prefix. This will make it easier to design option dialogs.
 * </p>
 */
public interface UserParamNames
{
    //
    // TM and Leveraging Parameters
    //
    public static final String TM_MATCHING_THRESHOLD = "tm_matching_threshold";
    public static final String TM_MATCHING_THRESHOLD_DEFAULT = "70";

    //
    // Terminology Parameters
    //
    public static final String TB_MATCHING_THRESHOLD = "tb_matching_threshold";
    public static final String TB_MATCHING_THRESHOLD_DEFAULT = "70";

    //
    // General Parameters
    //
    public static final String PAGENAME_DISPLAY = "pagename_display";
    public static final String PAGENAME_DISPLAY_FULL = "full";
    public static final String PAGENAME_DISPLAY_SHORT = "short";

    public static final String PAGENAME_DISPLAY_DEFAULT = PAGENAME_DISPLAY_FULL;

    // Email notification parameters
    public static final String NOTIFICATION_ENABLED = "notificationEnabled";
    public static final String NOTIFICATION_ENABLED_DEFAULT = "1";
    public static final String NOTIFICATION_DISABLED_DEFAULT = "0";
    // ////////////////////////////////////////////////////////////////////
    // Do not change the values for the notification names as they are
    // used as a key to resource bundles.
    // ////////////////////////////////////////////////////////////////////
    // ADMIN
    public static final String NOTIFY_INITIAL_IMPORT_FAILURE = "notifyInitilaImportFailure";
    // PM/WFM
    public static final String NOTIFY_JOB_DISCARD_FAILURE = "notifyJobDiscardFailure";
    public static final String NOTIFY_READY_TO_DISPATCH = "notifyReadyToBeDispatched";
    public static final String NOTIFY_DISPATCH_FAILURE = "notifyDispatchFailure";
    public static final String NOTIFY_IMPORT_FAILURE = "notifyImportFailure";
    public static final String NOTIFY_IMPORT_CORRECTION = "notifyImportCorrection";
    public static final String NOTIFY_DELAYED_REIMPORT_FAILURE = "notifyDelayedReimportFailure";
    public static final String NOTIFY_TASK_ACCEPTANCE = "notifyTaskAcceptance";
    public static final String NOTIFY_TASK_COMPLETION = "notifyTaskCompletion";
    public static final String NOTIFY_WFL_COMPLETION = "notifyWFLCompletion";
    public static final String NOTIFY_JOB_COMPLETION = "notifyJobCompletion";
    public static final String NOTIFY_TASK_REJECTION = "notifyTaskRejection";
    public static final String NOTIFY_PM_CHANGE_IN_PROJECT = "notifyPmChangeInProject";
    public static final String NOTIFY_NO_AVAILABLE_RESOURCE = "notifyNoAvailableResource";
    public static final String NOTIFY_EXPORT_SOURCE_FAILURE = "notifyExportSourceFailure";
    public static final String NOTIFY_ESTIMATED_EXCEEDS_PLANNED_DATE = "notifyEstimatedExceedsPlannedDate";
    public static final String NOTIFY_ADD_WORKFLOW_TO_JOB_FAILURE = "notifyAddWfToJobFailure";
    public static final String NOTIFY_STF_CREATION_FAILURE = "notifyStfCreationFailure";
    public static final String NOTIFY_SCHEDULING_FAILURE = "notifyDeadlineScheduleFailure";
    public static final String NOTIFY_ACTIVITY_DEADLINE = "notifyActivityDeadline";

    // Overdue for pm and user
    public static final String NOTIFY_OVERDUE_PM = "notifyOverDuePM";
    public static final String NOTIFY_OVERDUE_PM_DEFAULT = "0";
    public static final String NOTIFY_OVERDUE_USER = "notifyOverDueUser";
    public static final String NOTIFY_OVERDUE_USER_DEFAULT = "0";

    // LP, LM,..
    public static final String NOTIFY_DELAYED_REIMPORT = "notifyDelayedReimport";
    public static final String NOTIFY_NEWLY_ASSIGNED_TASK = "notifyNewlyAssignedTask";
    public static final String NOTIFY_WORKFLOW_DISCARD = "notifyWorkflowDiscard";
    public static final String NOTIFY_EXPORT_FOR_UPDATE = "notifyExportForUpdate";
    public static final String NOTIFY_EXPORT_COMPLETION = "notifyExportCompletion";
    public static final String NOTIFY_SUCCESSFUL_UPLOAD = "notifySuccessfulUpload";
    public static final String NOTIFY_SAVING_SEGMENTS_FAILURE = "notifySavingSegmentsFailure";
    public static final String NOTIFY_BATCH_ALIGNMENT_SUCCESS = "notifyBatchAlignmentSuccess";
    public static final String NOTIFY_BATCH_ALIGNMENT_FAILURE = "notifyBatchAlignmentFailure";
    public static final String NOTIFY_ALIGNMENT_UPLOAD_SUCCESS = "notifyAlignmentUploadSuccess";
    public static final String NOTIFY_ALIGNMENT_UPLOAD_FAILURE = "notifyAlignmentUploadFailure";

    //
    // Editor Parameters
    //

    // Which editor do we use, the old popup editor (Main+Segment) or
    // the new inline editor?
    public static final String EDITOR_SELECTION = "editor_selection";
    public static final String EDITOR_POPUP = "editor_popup";
    public static final String EDITOR_INLINE = "editor_inline";
    public static final String EDITOR_SELECTION_DEFAULT = EDITOR_POPUP;

    public static final String EDITOR_AUTO_SAVE_SEGMENT = "editor_autosave";
    public static final String EDITOR_AUTO_SAVE_SEGMENT_DEFAULT = "1";

    public static final String EDITOR_AUTO_UNLOCK = "editor_autounlock";
    public static final String EDITOR_AUTO_UNLOCK_DEFAULT = "0";

    public static final String EDITOR_AUTO_SYNC = "editor_autosync";
    public static final String EDITOR_AUTO_SYNC_DEFAULT = "1";

    public static final String EDITOR_AUTO_ADJUST_WHITESPACE = "editor_autowhite";
    public static final String EDITOR_AUTO_ADJUST_WHITESPACE_DEFAULT = "1";

    // Report Options -- Abbreviate Report Name
    public static final String EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT = "abbreviate_report_name";
    public static final String EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT_DEFAULT = "yes";

    // Allowed values for Main Editor layout: "source_target_horizontal",
    // "source_target_vertical" "source", or "target".
    public static final String EDITOR_LAYOUT = "editor_layout";
    public static final String EDITOR_LAYOUT_DEFAULT = "source_target_vertical";

    // Allowed values for Main Editor viewmode: 1 (preview), 2 (text),
    // 3 (list/detail)
    public static final String EDITOR_VIEWMODE = "editor_viewmode";
    public static final String EDITOR_VIEWMODE_DEFAULT = "3";

    // Allowed values for Segment Editor ptags: "compact", "verbose"
    public static final String EDITOR_PTAGMODE = "editor_ptagmode";
    public static final String EDITOR_PTAGMODE_DEFAULT = "compact";

    // Allowed values for Segment Editor ptags: "0" (dim), "1" (bright)
    public static final String EDITOR_PTAGHILITE = "editor_ptaghilite";
    public static final String EDITOR_PTAGHILITE_DEFAULT = "1";

    // Show the MT button in editor?
    public static final String EDITOR_SHOW_MT = "editor_show_mt";
    public static final String EDITOR_SHOW_MT_DEFAULT = "0";

    // Iterate through subs when editor in preview mode?
    public static final String EDITOR_ITERATE_SUBS = "editor_iterate_subs";
    public static final String EDITOR_ITERATE_SUBS_DEFAULT = "0";

    public static final String EDITOR_SEGMENTS_MAX_NUM = "segments_max_num";
    public static final String EDITOR_SEGMENTS_MAX_NUM_DEFAULT = "0";

    public static final String HYPERLINK_COLOR_OVERRIDE = "editor_linkColorOverride";
    public static final String HYPERLINK_COLOR_OVERRIDE_DEFAULT = "1";

    public static final String HYPERLINK_COLOR = "editor_linkColor";
    public static final String HYPERLINK_COLOR_DEFAULT = "blue";

    public static final String ACTIVE_HYPERLINK_COLOR = "editor_activeLinkColor";
    public static final String ACTIVE_HYPERLINK_COLOR_DEFAULT = "blue";

    public static final String VISITED_HYPERLINK_COLOR = "editor_visitedLinkColor";
    public static final String VISITED_HYPERLINK_COLOR_DEFAULT = "blue";

    public static final String PREVIEW_100MATCH_COLOR = "PREVIEW_100_MATCH_COLOR";
    public static final String PREVIEW_100MATCH_COLOR_DEFAULT = "Black";

    public static final String PREVIEW_ICEMATCH_COLOR = "PREVIEW_ICE_MATCH_COLOR";
    public static final String PREVIEW_ICEMATCH_COLOR_DEFAULT = "Black";

    public static final String PREVIEW_NONMATCH_COLOR = "PREVIEW_NON_MATCH_COLOR";
    public static final String PREVIEW_NONMATCH_COLOR_DEFAULT = "Black";

    public static final String EDITOR_SHOW_CLOSEALLCOMMENT = "editor_show_closeAllComment";
    public static final String EDITOR_SHOW_CLOSEALLCOMMENT_DEFAULT = "0";

    // Notify quote person email
    public static final String NOTIFY_QUOTE_PERSON = "notifyQuotePerson";

    public static final String DOWNLOAD_OPTION_FORMAT = "format";
    public static final String DOWNLOAD_OPTION_FORMAT_DEFAULT = "OmegaT";

    public static final String DOWNLOAD_OPTION_EDITOR = "editor";
    public static final String DOWNLOAD_OPTION_EDITOR_DEFAULT = "WinWord97";

    public static final String DOWNLOAD_OPTION_ENCODING = "encoding";
    public static final String DOWNLOAD_OPTION_ENCODING_DEFAULT = "defaultEncoding";

    public static final String DOWNLOAD_OPTION_PLACEHOLDER = "placeholder";
    public static final String DOWNLOAD_OPTION_PLACEHOLDER_DEFAULT = "compact";

    public static final String DOWNLOAD_OPTION_RESINSSELECT = "resInsSelector";
    public static final String DOWNLOAD_OPTION_RESINSSELECT_DEFAULT = "resInsTmx14b";

    public static final String DOWNLOAD_OPTION_EDITEXACT = "editExact";
    public static final String DOWNLOAD_OPTION_EDITEXACT_DEFAULT = "no";

    public static final String DOWNLOAD_OPTION_DISPLAYEXACTMATCH = "displayExactMatch";
    public static final String DOWNLOAD_OPTION_DISPLAYEXACTMATCH_DEFAULT = "no";

    public static final String DOWNLOAD_OPTION_CONSOLIDATE_TMX = "consolidate";
    public static final String DOWNLOAD_OPTION_CONSOLIDATE_TMX_DEFAULT = "yes";

    public static final String DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT = "changeCreationIdForMT";
    public static final String DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT_DEFAULT = "yes";
    
    public static final String DOWNLOAD_OPTION_SEPARATE_TM_FILE = "separateTMfile";
    public static final String DOWNLOAD_OPTION_SEPARATE_TM_FILE_DEFAULT = "no";

    // GBS-3776
    public static final String DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PRE = "penalizedReferenceTmPre";
    public static final String DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PRE_DEFAULT = "yes";
    public static final String DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PER = "penalizedReferenceTmPer";
    public static final String DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PER_DEFAULT = "no";

    public static final String DOWNLOAD_OPTION_TERMINOLOGY = "termSelector";
    public static final String DOWNLOAD_OPTION_TERMINOLOGY_DEFAULT = "tbx";

    public static final String DOWNLOAD_OPTION_CONSOLIDATE_TERM = "consolidateTerm";
    public static final String DOWNLOAD_OPTION_CONSOLIDATE_TERM_DEFAULT = "yes";

    public static final String DOWNLOAD_OPTION_TM_EDIT_TYPE = "TMEditType";
    // Allow edit locked segments: default is "Deny Edit",value 4 (original "No"
    // meant 0).
    public static final int DOWNLOAD_OPTION_TM_EDIT_TYPE_DEFAULT = AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY;

    //
    // Other candidates that are currently global system parameters:
    //
    // REFRESH_UI_LISTS
    // REFRESH_RATE
}

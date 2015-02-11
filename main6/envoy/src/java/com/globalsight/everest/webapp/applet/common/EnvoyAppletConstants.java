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
package com.globalsight.everest.webapp.applet.common;

import java.awt.Font;
import java.awt.Color;

/**
 * This interface contains all of the applet package related constants.
 */
/*
 * MODIFIED MM/DD/YYYY TomyD 11/01/2000 Initial version. BWang 12/10/2000 Added
 * look&feel constants
 */
public interface EnvoyAppletConstants
{
    // A
    public static final String ACTIVITIES = "activities";
    public static final String ACTIVITY = "activity";
    public static final String ALL_TERMBASES = "allTermbases";
    public static final String ALL_TMS = "allTms";
    // B
    public static final String BTN_LABELS = "btnLabels";
    // D
    public static final String DBCOLUMN_MOD = "dbcolumnMod";
    public static final String DBCOLUMN_DBPROFILEPAIRS = "dbcolumnDBProfilePairs";
    public static final String DBCOLUMN_FORMATPAIRS = "dbcolumnFormatPairs";
    public static final String DBCOLUMN_RULEPAIRS = "dbcolumnRulePairs";
    public static final String DBCOLUMN_MODEPAIRS = "dbcolumnModePairs";
    public static final String DBCONNECTION_MOD = "dbconnectionMod";
    public static final String DBCONNECTION_RESULTS = "dbconnectionResults";
    public static final String DBCONNECTION_TEST = "dbconnectionTest";
    public static final String DBDISPATCH_CONNECTIONID = "dbdispatchConnectionId";
    public static final String DBDISPATCH_DUP = "dbdispatchDup";
    public static final String DBDISPATCH_MOD = "dbdispatchMod";
    public static final String DBPROFILE_COLUMNS = "dbprofileColumns";
    public static final String DBPROFILE_DETAILS = "dbprofileDetails";
    // E
    public static final String ERROR = "error";
    // F
    public static final String FILEPROFILE_MOD = "fileprofileMod";
    public static final String FILEPROFILE_Ll10NPAIRS = "fileprofileL10nPairs";
    public static final String FILEPROFILE_FORMATPAIRS = "fileprofileFormatPairs";
    public static final String FILEPROFILE_RULEPAIRS = "fileprofileRulePairs";
    public static final String FILEPROFILE_ENCODINGPAIRS = "fileprofileEncodingPairs";
    public static final String FILEPROFILE_EXTPAIRS = "fileprofileExtPairs";
    // G
    public static final String GRID_HEADER_LABELS = "gridHeaderLabels";
    public static final String GROUP_NAMES = "groupNames";
    // I
    public static final String INFO_BTN_LABELS = "infoBtnLabels";
    public static final String INFO_LABELS = "infoLabels";
    public static final String I18N_CONTENT = "i18n_content";
    // L
    public static final String LABELS = "labels";
    public static final String LOCPROFILE_ACTIVITIES = "locprofileActivities";
    public static final String LOCPROFILE_RATES = "locprofileRates";
    public static final String LOCPROFILE_MOD = "locprofileMod";
    public static final String LOCPROFILE_UNITS = "locprofileUnits";

    public static final String COSTING_ENABLED = "jobCostingEnabled";
    public static final String REVENUE_ENABLED = "jobRevenueEnabled";
    // M
    public static final String MESSAGE = "message";
    // N
    public static final String NAME_TYPE = "nameType";
    public static final String NAME_CRITERIA = "nameCriteria";
    // P
    public static final String PREVIEWURLXMLMOD = "previewUrlXmlMod";
    public static final String PROJECT = "project";
    public static final String PROJECT_MANAGERS = "projectManagers";

    // R
    public static final String RATES = "rates";
    // S
    public static final String SOURCE_LOCALE = "sourceLocales";
    public static final String SOURCE_WRAPPERS = "sourceWrappers";
    public static final String SYSTEM_ACTION = "systemAction";
    // T
    public static final String TARGET_LOCALE = "targetLocales";
    public static final String TARGET_WRAPPERS = "targetWrappers";
    public static final String THRESHOLDS = "thresholds";
    // U
    // V
    public static final String VALUES = "values";
    // X
    public static final String XMLRULEFILEMOD = "xmlrulefileMod";
    public static final String XMLRULEFILEDUP = "xmlrulefileDup";
    // //////////////////////////////////////////////////////////////////////////
    // Look&Feel Constants
    // //////////////////////////////////////////////////////////////////////////
    // Font
    public static final String ARIAL_FONT_NAME = "Arial";
    public static final String ARIAL_UNICODE_FONT_NAME = "Arial Unicode MS";

    public static final Font SMALL_FONT = new Font("Arial", Font.BOLD, 11);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 12);
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font MAIN_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font CELL_FONT = new Font("Arial", Font.PLAIN, 10);
    public static final Font DIALOG_FONT = new Font("Arial", Font.PLAIN, 10);

    // Color
    public static final Color ENVOY_LITE_BLUE = new Color(0x9999FF);
    public static final Color ENVOY_LITE_GREEN = new Color(0x99FF99);
    public static final Color ENVOY_LITE_VIOLET = new Color(0xFF99FF);
    public static final Color ENVOY_BLUE = new Color(0x708EB3);
    public static final Color ENVOY_BLUE_DARK = new Color(0x415A7A); // (65,90,122)
    public static final Color ENVOY_GREEN = new Color(0x009966);
    public static final Color ENVOY_VIOLET = new Color(0x990099);
    public static final Color ENVOY_BLACK = new Color(0x000000);
    public static final Color ENVOY_WHITE = new Color(0xFFFFFF);
    public static final Color ENVOY_GREY = new Color(0x999999);
    public static final Color ENVOY_RED = new Color(0xFF0000);
    public static final Color BUTTON_BACKGROUND_COLOR = Color.lightGray;
    // Button
    public static final int ARROW_WIDTH = 70;
    public static final int ARROW_HEIGHT = 30;
    public static final int BUTTON_WIDTH = 105;
    public static final int BUTTON_HEIGHT = 35;
    public static final int BUTTON_HORIZONTAL_PADDING = 5;
    public static final int APPLET_FRAME_TOP_LEFT_LABEL_WIDTH = BUTTON_WIDTH;
    public static final int APPLET_FRAME_TOP_LEFT_LABEL_HEIGHT = 25;
    public static final int APPLET_FRAME_BOTTOM_LEFT_LABEL_WIDTH = BUTTON_WIDTH;
    public static final int APPLET_FRAME_BOTTOM_LEFT_LABEL_HEIGHT = APPLET_FRAME_TOP_LEFT_LABEL_HEIGHT;

    // Spacing & Sizing.
    public static final int HORIZONTAL_GAP = 0;
    public static final int VERTICAL_GAP = 0;
    public static final int HORIZONTAL_BORDER_GAP = 0;
    public static final int VERTICAL_BORDER_GAP = 0;
    public static final int GRID_WIDTH = 538;
    public static final int GRID_HEIGHT = 327;
    public static final int APPLET_WIDTH = 538;
    public static final int APPLET_HEIGHT = 363;
    // Images
    public static final String HR_LINE_IMAGE = "images/hr.gif";
    public static final String PIPE = "images/pipe.gif";
    public static final String START_OR_EXIT_NODE_IMAGE = "images/startOrExitNode.gif";
    public static final String START_NODE_IMAGE = "/images/startNode.gif";
    public static final String EXIT_NODE_IMAGE = "/images/exitNode.gif";
    public static final String DEACTIVE_NODE_IMAGE = "images/deactiveNode.gif";
    public static final String ACTIVE_NODE_IMAGE = "images/activeNode.gif";
    public static final String COMPLETED_NODE_IMAGE = "images/completedNode.gif";
}

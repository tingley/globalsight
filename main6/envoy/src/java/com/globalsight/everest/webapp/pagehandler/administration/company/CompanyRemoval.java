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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.qachecks.DITAQAChecker;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.system.LogManager;
import com.globalsight.util.system.LogType;

/**
 * Used for removing a company and all its associated data stored in DB and
 * disk.
 */
public class CompanyRemoval
{
    private static final Logger CATEGORY = Logger
            .getLogger(CompanyRemoval.class.getName());

    private Company company = null;
    private boolean isJobRemoval = false;
    private long runtime = -1l;
    private String runningMessage = "";
    private String doneMessage = "";

    private static final int BATCH_CAPACITY = 1000;

    private static final String TABLE_BACKUP_COMPANY = "COMPANY_BACKUP";
    private static final String TABLE_BACKUP_JOB = "JOB_BACKUP";
    private static final String TABLE_BACKUP_REQUEST = "REQUEST_BACKUP";
    private static final String TABLE_BACKUP_WORKFLOW = "WORKFLOW_BACKUP";

    // sql for deletion
    private static final String SQL_DELETE_ACTIVITY = "delete from ACTIVITY where COMPANY_ID=?";
    private static final String SQL_DELETE_ADDING_SOURCE_PAGE = "delete from ADDING_SOURCE_PAGE where JOB_ID in ";
    private static final String SQL_DELETE_AMOUNT_OF_WORK = "delete from AMOUNT_OF_WORK where TASK_ID in ";
    private static final String SQL_DELETE_ATTRIBUTE = "delete from ATTRIBUTE where COMPANY_ID=?";
    private static final String SQL_DELETE_ATTRIBUTE_CLONE = "delete from ATTRIBUTE_CLONE where COMPANY_ID=?";
    private static final String SQL_DELETE_ATTRIBUTE_SET = "delete from ATTRIBUTE_SET where COMPANY_ID=?";
    private static final String SQL_DELETE_ATTRIBUTE_SET_ATTRIBUTE = "delete from ATTRIBUTE_SET_ATTRIBUTE where SET_ID in ";
    private static final String SQL_DELETE_BASE_FILTER = "delete from BASE_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_BASE_FILTER_MAPPING = "delete from BASE_FILTER_MAPPING where BASE_FILTER_ID in ";
    private static final String SQL_DELETE_CALENDAR = "delete from CALENDAR where COMPANY_ID=?";
    private static final String SQL_DELETE_CALENDAR_HOLIDAY = "delete from CALENDAR_HOLIDAY where CALENDAR_ID in ";
    private static final String SQL_DELETE_CALENDAR_WORKING_DAY = "delete from CALENDAR_WORKING_DAY where CALENDAR_ID in ";
    private static final String SQL_DELETE_CALENDAR_WORKING_HOUR = "delete from CALENDAR_WORKING_HOUR where CALENDAR_WORKING_DAY_ID in ";
    private static final String SQL_DELETE_CATEGORY = "delete from CATEGORY_SEGMENT_COMMENT where COMPANY_ID=?";
    private static final String SQL_DELETE_COMMENTS = "delete from COMMENTS where CREATOR_USER_ID in ";
    private static final String SQL_DELETE_COMPANY = "delete from COMPANY where ID=?";
    private static final String SQL_DELETE_CORPUS_MAP = "delete from CORPUS_MAP where TM_ID in ";
    private static final String SQL_DELETE_COST = "delete from COST where CURRENCY_CONVERSION_ID in ";
    private static final String SQL_DELETE_COST_BY_WORD_COUNT = "delete from COST_BY_WORD_COUNT where COST_ID in ";
    private static final String SQL_DELETE_CURRENCY_CONVERSION = "delete from CURRENCY_CONVERSION where COMPANY_ID=?";
    private static final String SQL_DELETE_CUSTOMER_COLUMN_DETAIL = "delete from CUSTOMER_COLUMN_DETAIL where XML_RULE_ID in ";
    private static final String SQL_DELETE_CUSTOMER_DB_ACCESS_PROFILE = "delete from CUSTOMER_DB_ACCESS_PROFILE where L10N_PROFILE_ID in ";
    private static final String SQL_DELETE_CVS_FILE_PROFILE = "delete from CVS_FILE_PROFILE where COMPANY_ID=?";
    private static final String SQL_DELETE_CVS_MODULE = "delete from CVS_MODULE where SERVER in ";
    private static final String SQL_DELETE_CVS_REPOSITORY = "delete from CVS_REPOSITORY where SERVER in ";
    private static final String SQL_DELETE_CVS_SERVER = "delete from CVS_SERVER where COMPANYID=?";
    private static final String SQL_DELETE_CVS_SERVER_USER = "delete from CVS_SERVER_USER where SERVER in ";
    private static final String SQL_DELETE_CVS_SOURCE_FILES = "delete from CVS_SOURCE_FILES where MODULE_ID in ";
    private static final String SQL_DELETE_EXPORT_BATCH_EVENT = "delete from EXPORT_BATCH_EVENT where JOB_ID in ";
    private static final String SQL_DELETE_EXPORTBATCH_WORKFLOW = "delete from EXPORTBATCH_WORKFLOW where WORKFLOW_ID in ";
    private static final String SQL_DELETE_EXPORT_LOCATION = "delete from EXPORT_LOCATION where COMPANY_ID=?";
    private static final String SQL_DELETE_EXTENSION = "delete from EXTENSION where COMPANY_ID=?";
    private static final String SQL_DELETE_EXPORTING_PAGE = "delete from EXPORTING_PAGE where EXPORT_BATCH_ID in ";
    private static final String SQL_DELETE_FILE_PROFILE = "delete from FILE_PROFILE where COMPANYID=?";
    private static final String SQL_DELETE_FILE_PROFILE_EXTENSION = "delete from FILE_PROFILE_EXTENSION where FILE_PROFILE_ID in ";
    private static final String SQL_DELETE_FILE_VALUE_ITEM = "delete from FILE_VALUE_ITEM where JOB_ATTRIBUTE_ID in ";
    private static final String SQL_DELETE_FILTER_CONFIGURATION = "delete from FILTER_CONFIGURATION where COMPANY_ID=?";
    private static final String SQL_DELETE_FRAME_MAKER_FILTER = "delete from FRAME_MAKER_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_GS_EDITION = "delete from GS_EDITION where COMPANYID=?";
    private static final String SQL_DELETE_HOLIDAY = "delete from HOLIDAY where COMPANY_ID=?";
    private static final String SQL_DELETE_HTML_FILTER = "delete from HTML_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_IMAGE_REPLACE_FILE_MAP = "delete from IMAGE_REPLACE_FILE_MAP where TARGET_PAGE_ID in ";
    private static final String SQL_DELETE_INDD_FILTER = "delete from INDD_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_ISSUE = "delete from ISSUE where CREATOR_USER_ID in ";
    private static final String SQL_DELETE_ISSUE_HISTORY = "delete from ISSUE_HISTORY where ISSUE_ID in ";
    private static final String SQL_DELETE_IP_TM_INDEX = "delete from IP_TM_INDEX where JOB_ID in ";
    private static final String SQL_DELETE_IP_TM_SRC_L = "delete from IP_TM_SRC_L where JOB_ID in ";
    private static final String SQL_DELETE_IP_TM_SRC_T = "delete from IP_TM_SRC_T where JOB_ID in ";
    private static final String SQL_DELETE_IP_TM_TRG_L = "delete from IP_TM_TRG_L where SRC_ID in ";
    private static final String SQL_DELETE_IP_TM_TRG_T = "delete from IP_TM_TRG_T where SRC_ID in ";
    private static final String SQL_DELETE_JAVA_PROPERTIES_FILTER = "delete from JAVA_PROPERTIES_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_JAVA_SCRIPT_FILTER = "delete from JAVA_SCRIPT_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_JBPM_ACTION = "delete from JBPM_ACTION where ACTIONDELEGATION_ in ";
    private static final String SQL_DELETE_JBPM_DELEGATION = "delete from JBPM_DELEGATION where PROCESSDEFINITION_ in ";
    private static final String SQL_DELETE_JBPM_DELEGATION_NODE = "delete from JBPM_NODE WHERE DECISIONDELEGATION in ";
    private static final String SQL_DELETE_JBPM_GS_VARIABLE = "delete from JBPM_GS_VARIABLE where TASKINSTANCE_ID in ";
    private static final String SQL_DELETE_JBPM_MODULEDEFINITION = "delete from JBPM_MODULEDEFINITION where PROCESSDEFINITION_ in ";
    private static final String SQL_DELETE_JBPM_MODULEINSTANCE = "delete from JBPM_MODULEINSTANCE where PROCESSINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_NODE = "delete from JBPM_NODE where PROCESSDEFINITION_ in ";
    private static final String SQL_DELETE_JBPM_POOLEDACTOR = "delete from JBPM_POOLEDACTOR where ID_ in ";
    private static final String SQL_DELETE_JBPM_PROCESSDEFINITION = "delete from JBPM_PROCESSDEFINITION where ID_ in ";
    private static final String SQL_DELETE_JBPM_PROCESSINSTANCE = "delete from JBPM_PROCESSINSTANCE where ID_ in ";
    private static final String SQL_DELETE_JBPM_TASK = "delete from JBPM_TASK where PROCESSDEFINITION_ in ";
    private static final String SQL_DELETE_JBPM_TASKACTORPOOL = "delete from JBPM_TASKACTORPOOL where TASKINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_TASKCONTROLLER = "delete from JBPM_TASKCONTROLLER where TASKCONTROLLERDELEGATION_ in ";
    private static final String SQL_DELETE_JBPM_TASKINSTANCE = "delete from JBPM_TASKINSTANCE where TASKMGMTINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_TOKEN = "delete from JBPM_TOKEN where PROCESSINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_TOKENVARIABLEMAP = "delete from JBPM_TOKENVARIABLEMAP where CONTEXTINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_TRANSITION = "delete from JBPM_TRANSITION where PROCESSDEFINITION_ in ";
    private static final String SQL_DELETE_JBPM_VARIABLEACCESS = "delete from JBPM_VARIABLEACCESS where PROCESSSTATE_ in ";
    private static final String SQL_DELETE_JBPM_VARIABLEINSTANCE_BY_TASKINSTANCE = "delete from JBPM_VARIABLEINSTANCE where TASKINSTANCE_ in ";
    private static final String SQL_DELETE_JBPM_VARIABLEINSTANCE_BY_TOKENVARIABLEMAP = "delete from JBPM_VARIABLEINSTANCE where TOKENVARIABLEMAP_ in ";
    private static final String SQL_DELETE_JOB = "delete from JOB where COMPANY_ID=?";
    private static final String SQL_DELETE_JOB_ATTRIBUTE = "delete from JOB_ATTRIBUTE where JOB_ID in ";
    private static final String SQL_DELETE_JOB_ATTRIBUTE_SELECT_OPTION = "delete from JOB_ATTRIBUTE_SELECT_OPTION where JOB_ATTRIBUTE_ID in ";
    private static final String SQL_DELETE_JSP_FILTER = "delete from JSP_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_L10N_PROFILE = "delete from L10N_PROFILE where COMPANYID=?";
    private static final String SQL_DELETE_MT_PROFILE = "delete from MT_PROFILE where COMPANY_ID=?";
    private static final String SQL_DELETE_L10N_PROFILE_TM_PROFILE = "delete from L10N_PROFILE_TM_PROFILE where L10N_PROFILE_ID in ";
    private static final String SQL_DELETE_L10N_PROFILE_VERSION = "delete from L10N_PROFILE_VERSION where MODIFIED_PROFILE_SEQ in ";
    private static final String SQL_DELETE_MT_PROFILE_EXTENTINFO = "delete from MT_PROFILE_EXTENT_INFO where MT_PROFILE_ID in ";
    private static final String SQL_DELETE_L10N_PROFILE_WFTEMPLATE_INFO = "delete from L10N_PROFILE_WFTEMPLATE_INFO where L10N_PROFILE_ID in ";
    private static final String SQL_DELETE_LEVERAGE_LOCALES = "delete from LEVERAGE_LOCALES where WORKFLOW_INFO_ID in ";
//    private static final String SQL_DELETE_LEVERAGE_MATCH = "delete from LEVERAGE_MATCH where SOURCE_PAGE_ID in ";
    private static final String SQL_DELETE_LOCALE_PAIR = "delete from LOCALE_PAIR where COMPANY_ID=?";
    private static final String SQL_DELETE_MODULE_MAPPING = "delete from MODULE_MAPPING where COMPANY_ID=?";
    private static final String SQL_DELETE_MODULE_RENAME = "delete from MODULE_RENAME where MODULE_MAPPING_ID in ";
    private static final String SQL_DELETE_MS_OFFICE_DOC_FILTER = "delete from MS_OFFICE_DOC_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_MS_OFFICE_EXCEL_FILTER = "delete from MS_OFFICE_EXCEL_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_MS_OFFICE_PPT_FILTER = "delete from MS_OFFICE_PPT_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_OFFICE2010_FILTER = "delete from OFFICE2010_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_OPENOFFICE_FILTER = "delete from OPENOFFICE_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_PERMISSIONGROUP = "delete from PERMISSIONGROUP where COMPANY_ID=?";
    private static final String SQL_DELETE_PERMISSIONGROUP_USER = "delete from PERMISSIONGROUP_USER where PERMISSIONGROUP_ID in ";
    private static final String SQL_DELETE_PO_FILTER = "delete from PO_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_PROJECT = "delete from PROJECT where COMPANYID=?";
    private static final String SQL_DELETE_JOB_GROUP = "delete from JOB_GROUP where COMPANY_ID=?";
    private static final String SQL_DELETE_PROJECT_USER = "delete from PROJECT_USER where PROJECT_ID in ";
    private static final String SQL_DELETE_PROJECT_VENDOR = "delete from PROJECT_VENDOR where PROJECT_ID in ";
    private static final String SQL_DELETE_PROJECT_TM = "delete from PROJECT_TM where COMPANY_ID=?";
    private static final String SQL_DELETE_PROJECT_TM_TU_L = "delete from PROJECT_TM_TU_L where TM_ID in ";
    private static final String SQL_DELETE_PROJECT_TM_TUV_L = "delete from PROJECT_TM_TUV_L where TU_ID in ";
    private static final String SQL_DELETE_PROJECT_TM_TU_T = "delete from PROJECT_TM_TU_T where TM_ID in ";
    private static final String SQL_DELETE_PROJECT_TM_TUV_T = "delete from PROJECT_TM_TUV_T where TU_ID in ";
    private static final String SQL_DELETE_PROJECT_TM_TU_T_PROP = "delete from PROJECT_TM_TU_T_PROP where TU_ID in ";
    private static final String SQL_DELETE_RATE = "delete from RATE where ACTIVITY_ID in ";
    private static final String SQL_DELETE_REMOTE_ACCESS_HISTORY = "delete from REMOTE_ACCESS_HISTORY where USER_ID in ";
    private static final String SQL_DELETE_REMOVED_PREFIX_TAG = "delete from REMOVED_PREFIX_TAG where TU_ID in ";
    private static final String SQL_DELETE_REMOVED_SUFFIX_TAG = "delete from REMOVED_SUFFIX_TAG where TU_ID in ";
    private static final String SQL_DELETE_REMOVED_TAG = "delete from REMOVED_TAG where TU_ID in ";
    private static final String SQL_DELETE_REQUEST = "delete from REQUEST where COMPANY_ID=?";
    private static final String SQL_DELETE_RESERVED_TIME_BY_CALENDAR_ID = "delete from RESERVED_TIME where USER_CALENDAR_ID in ";
    private static final String SQL_DELETE_RESERVED_TIME_BY_TASK_ID = "delete from RESERVED_TIME where TASK_ID in ";
    private static final String SQL_DELETE_RSS_FEED = "delete from RSS_FEED where COMPANY_ID=?";
    private static final String SQL_DELETE_RSS_ITEM = "delete from RSS_ITEM where FEED_ID in ";
    private static final String SQL_DELETE_SECONDARY_TARGET_FILE = "delete from SECONDARY_TARGET_FILE where WORKFLOW_ID in ";
    private static final String SQL_DELETE_SEGMENTATION_RULE = "delete from SEGMENTATION_RULE where COMPANY_ID=?";
    private static final String SQL_DELETE_SEGMENTATION_RULE_TM_PROFILE = "delete from SEGMENTATION_RULE_TM_PROFILE where SEGMENTATION_RULE_ID in ";
    private static final String SQL_DELETE_SOURCE_PAGE = "delete from SOURCE_PAGE where COMPANY_ID=?";
    private static final String SQL_DELETE_SOURCE_PAGE_LEVERAGE_GROUP = "delete from SOURCE_PAGE_LEVERAGE_GROUP where SP_ID in ";
    private static final String SQL_DELETE_SSO_USER_MAPPING = "delete from SSO_USER_MAPPING where COMPANY_ID=?";
    private static final String SQL_DELETE_SURCHARGE = "delete from SURCHARGE where COST_ID in ";
    private static final String SQL_DELETE_SYSTEM_PARAMETER = "delete from SYSTEM_PARAMETER where COMPANY_ID=?";
    private static final String SQL_DELETE_TARGET_PAGE = "delete from TARGET_PAGE where ID in ";
    private static final String SQL_DELETE_TARGET_PAGE_LEVERAGE_GROUP = "delete from TARGET_PAGE_LEVERAGE_GROUP where TP_ID in ";
    private static final String SQL_DELETE_TASK_INFO_BY_COMPANY_ID = "delete from TASK_INFO where COMPANY_ID=?";
    private static final String SQL_DELETE_SCORECARD_CATEGORY_BY_COMPANY_ID = "delete from CATEGORY_SCORECARD WHERE COMPANY_ID = ?";
    private static final String SQL_DELETE_GIT_CONNECTOR_BY_COMPANY_ID = "delete from CONNECTOR_GIT WHERE COMPANY_ID = ?";
    private static final String SQL_DELETE_GIT_CONNECTOR_FILE_MAPPING_BY_COMPANY_ID = "delete from CONNECTOR_GIT_FILE_MAPPING WHERE COMPANY_ID = ?";
    private static final String SQL_DELETE_POST_REVIEW_CATEGORY_BY_COMPANY_ID = "delete from CATEGORY_POST_REVIEW WHERE COMPANY_ID = ?";
    private static final String SQL_DELETE_TASK_INFO_BY_TASK_ID = "delete from TASK_INFO where TASK_ID in ";
    private static final String SQL_DELETE_TASK_INTERIM = "delete from TASK_INTERIM where USER_ID in ";
    private static final String SQL_DELETE_TASK_TUV = "delete from TASK_TUV where TASK_ID in ";
    private static final String SQL_DELETE_TB_TERMBASE = "delete from TB_TERMBASE where COMPANYID=?";
    private static final String SQL_DELETE_TB_CONCEPT = "delete from TB_CONCEPT where TBID in ";
    private static final String SQL_DELETE_TB_LANGUAGE = "delete from TB_LANGUAGE where TBID in ";
    private static final String SQL_DELETE_TB_LOCK = "delete from TB_LOCK where TBid in ";
    private static final String SQL_DELETE_TB_SCHEDULED_JOBS = "delete from TB_SCHEDULED_JOBS where TBid in ";
    private static final String SQL_DELETE_TB_TERM = "delete from TB_TERM where TBID in ";
    private static final String SQL_DELETE_TB_USER_DATA = "delete from TB_USER_DATA where TBID in ";
    private static final String SQL_DELETE_TDA_TM = "delete from TDA_TM where TM_FIPROFILE_ID in ";
    private static final String SQL_DELETE_TERM_LEVERAGE_MATCH = "delete from TERM_LEVERAGE_MATCH where TERMBASE_ID in ";
    private static final String SQL_DELETE_TEMPLATE = "delete from TEMPLATE where ID in ";
    private static final String SQL_DELETE_TEMPLATE_PART = "delete from TEMPLATE_PART where ID in ";
    private static final String SQL_DELETE_TEMPLATE_PART_ARCHIVED = "delete from TEMPLATE_PART_ARCHIVED where ID in ";
    private static final String SQL_DELETE_TM_ATTRIBUTE = "delete from PROJECT_TM_ATTRIBUTE where TM_ID in ";
    private static final String SQL_DELETE_TM_PROFILE_PROJECT_TM_INFO = "delete from TM_PROFILE_PROJECT_TM_INFO where PROJECT_TM_ID in ";
    private static final String SQL_DELETE_TM_PROFILE = "delete from TM_PROFILE where PROJECT_TM_ID_FOR_SAVE in ";
    private static final String SQL_DELETE_TM_PROFILE_ATTRIBUTE = "delete from TM_PROFILE_ATTRIBUTE where TMP_ID in ";
    private static final String SQL_DELETE_TM3_ATTR = "delete from TM3_ATTR where TMID in ";
    private static final String SQL_DELETE_TM3_EVENTS = "delete from TM3_EVENTS where TMID in ";
    private static final String SQL_DELETE_TM3_TM = "delete from TM3_TM where SHAREDSTORAGEID=?";
    private static final String SQL_DELETE_TM_TB_USERS = "delete from TM_TB_USERS where USER_ID in ";
    private static final String SQL_DELETE_TRANSLATION_TU_TUV_INDEX = "delete from TRANSLATION_TU_TUV_INDEX where COMPANY_ID=?";
    private static final String SQL_DELETE_UPDATED_SOURCE_PAGE = "delete from UPDATED_SOURCE_PAGE where JOB_ID in ";
    private static final String SQL_DELETE_USER_CALENDAR = "delete from USER_CALENDAR where CALENDAR_ID in ";
    private static final String SQL_DELETE_USER_CALENDAR_WORKING_DAY = "delete from USER_CALENDAR_WORKING_DAY where CALENDAR_ID in ";
    private static final String SQL_DELETE_USER_CALENDAR_WORKING_HOUR = "delete from USER_CALENDAR_WORKING_HOUR where USER_CALENDAR_WORKING_DAY_ID in ";
    private static final String SQL_DELETE_USER_DEFAULT_ACTIVITIES = "delete from USER_DEFAULT_ACTIVITIES where DEFAULT_ROLE_ID in ";
    private static final String SQL_DELETE_USER_DEFAULT_ROLES = "delete from USER_DEFAULT_ROLES where USER_ID in ";
    private static final String SQL_DELETE_USER_FIELD_SECURITY = "delete from USER_FIELD_SECURITY where USER_ID in ";
    private static final String SQL_DELETE_USER_ID_USER_NAME = "delete from USER_ID_USER_NAME where USER_NAME in ";
    private static final String SQL_DELETE_USER_PARAMETER = "delete from USER_PARAMETER where USER_ID in ";
    private static final String SQL_DELETE_VENDOR_RATING = "delete from VENDOR_RATING where TASK_ID in ";
    private static final String SQL_DELETE_VENDOR_ROLE = "delete from VENDOR_ROLE where ACTIVITY_ID in ";
    private static final String SQL_DELETE_WORKFLOW_TEMPLATE = "delete from WORKFLOW_TEMPLATE where COMPANYID=?";
    private static final String SQL_DELETE_WF_TEMPLATE_WF_MANAGER = "delete from WF_TEMPLATE_WF_MANAGER where WORKFLOW_TEMPLATE_ID in ";
    private static final String SQL_DELETE_WORKFLOW = "delete from WORKFLOW where COMPANY_ID=?";
    private static final String SQL_DELETE_WORKFLOW_OWNER = "delete from WORKFLOW_OWNER where WORKFLOW_ID in ";
    private static final String SQL_DELETE_WORKFLOW_REQUEST = "delete from WORKFLOW_REQUEST where ID in ";
    private static final String SQL_DELETE_SCORE = "delete from SCORECARD_SCORE where JOB_ID in ";
    private static final String SQL_DELETE_GIT_CONNECTOR_JOB = "delete from CONNECTOR_GIT_JOB where JOB_ID in ";
    private static final String SQL_DELETE_BLAISE_CONNECTOR_JOB = "delete from CONNECTOR_BLAISE_JOB WHERE JOB_ID in ";
    private static final String SQL_DELETE_WORKFLOW_REQUEST_WFTEMPLATE = "delete from WORKFLOW_REQUEST_WFTEMPLATE where WORKFLOW_TEMPLATE_ID in ";
    private static final String SQL_DELETE_XLIFF_ALT = "delete from XLIFF_ALT where TUV_ID in ";
    private static final String SQL_DELETE_XML_DTD = "delete from XML_DTD where COMPANY_ID=?";
    private static final String SQL_DELETE_XML_RULE = "delete from XML_RULE where COMPANY_ID=?";
    private static final String SQL_DELETE_XML_RULE_FILTER = "delete from XML_RULE_FILTER where COMPANY_ID=?";
    private static final String SQL_DELETE_WORKFLOW_STATE_POST_BY_COMPANY_ID = "delete from WORKFLOW_STATE_POSTS where COMPANY_ID=?";
    // sql for query
    private static final String SQL_QUERY_ID = "select ID from ";
//    private static final String SQL_QUERY_TABLE = "show tables like ?";
    private static final String SQL_QUERY_ACTIVITY = "select ID from ACTIVITY where COMPANY_ID=?";
    private static final String SQL_QUERY_ATTRIBUTE_SET = "select ID from ATTRIBUTE_SET where COMPANY_ID=?";
    private static final String SQL_QUERY_BASE_FILTER = "select ID from BASE_FILTER where COMPANY_ID=?";
    private static final String SQL_QUERY_CALENDAR = "select ID from CALENDAR where COMPANY_ID=?";
    private static final String SQL_QUERY_CALENDAR_WORKING_DAY = "select ID from CALENDAR_WORKING_DAY where CALENDAR_ID in ";
    private static final String SQL_QUERY_COST = "select ID from COST where CURRENCY_CONVERSION_ID in ";
    private static final String SQL_QUERY_CURRENCY_CONVERSION = "select ID from CURRENCY_CONVERSION where COMPANY_ID=?";
    private static final String SQL_QUERY_CVS_MODULE = "select ID from CVS_MODULE where SERVER in ";
    private static final String SQL_QUERY_CVS_SERVER = "select ID from CVS_SERVER where COMPANYID=?";
    private static final String SQL_QUERY_EXPORT_BATCH_EVENT = "select ID from EXPORT_BATCH_EVENT where JOB_ID in ";
    private static final String SQL_QUERY_FILE_PROFILE = "select ID from FILE_PROFILE where COMPANYID=?";
    private static final String SQL_QUERY_ISSUE = "select ID from ISSUE where CREATOR_USER_ID in ";
    private static final String SQL_QUERY_IP_TM_SRC_L = "select ID from IP_TM_SRC_L where JOB_ID in ";
    private static final String SQL_QUERY_IP_TM_SRC_T = "select ID from IP_TM_SRC_T where JOB_ID in ";
    private static final String SQL_QUERY_JBPM_DELEGATION = "select ID_ from JBPM_DELEGATION where PROCESSDEFINITION_ in ";
    private static final String SQL_QUERY_JBPM_MODULEINSTANCE = "select ID_ from JBPM_MODULEINSTANCE where PROCESSINSTANCE_ in ";
    private static final String SQL_QUERY_JBPM_NODE = "select ID_ from JBPM_NODE where PROCESSDEFINITION_ in ";
    private static final String SQL_QUERY_JBPM_DELEGATION_NODE = "select ID_ from JBPM_NODE where DECISIONDELEGATION in ";
    private static final String SQL_QUERY_JBPM_PROCESSINSTANCE = "select PROCESSDEFINITION_ from JBPM_PROCESSINSTANCE where ID_ in ";
    private static final String SQL_QUERY_JBPM_TASKACTORPOOL = "select POOLEDACTOR_ from JBPM_TASKACTORPOOL where TASKINSTANCE_ in ";
    private static final String SQL_QUERY_JBPM_TASKINSTANCE = "select ID_ from JBPM_TASKINSTANCE where TASKMGMTINSTANCE_ in ";
    private static final String SQL_QUERY_JBPM_TOKENVARIABLEMAP = "select ID_ from JBPM_TOKENVARIABLEMAP where CONTEXTINSTANCE_ in ";
    private static final String SQL_QUERY_JOB = "select ID from JOB where COMPANY_ID=?";
    private static final String SQL_QUERY_JOB_ATTRIBUTE = "select ID from JOB_ATTRIBUTE where JOB_ID in ";
    private static final String SQL_QUERY_L10N_PROFILE = "select ID from L10N_PROFILE where COMPANYID=?";
    private static final String SQL_QUERY_MT_PROFILE = "select ID from MT_PROFILE where COMPANY_ID=?";
    private static final String SQL_QUERY_MODULE_MAPPING = "select ID from MODULE_MAPPING where COMPANY_ID=?";
    private static final String SQL_QUERY_PERMISSIONGROUP = "select ID from PERMISSIONGROUP where COMPANY_ID=?";
    private static final String SQL_QUERY_PROJECT = "select PROJECT_SEQ from PROJECT where COMPANYID=?";
    private static final String SQL_QUERY_PROJECT_TM = "select ID from PROJECT_TM where COMPANY_ID=?";
    private static final String SQL_QUERY_PROJECT_TM_TU_L = "select ID from PROJECT_TM_TU_L where TM_ID in ";
    private static final String SQL_QUERY_PROJECT_TM_TU_T = "select ID from PROJECT_TM_TU_T where TM_ID in ";
    private static final String SQL_QUERY_RSS_FEED = "select ID from RSS_FEED where COMPANY_ID=?";
    private static final String SQL_QUERY_SEGMENTATION_RULE = "select ID from SEGMENTATION_RULE where COMPANY_ID=?";
    private static final String SQL_QUERY_SOURCE_PAGE = "select ID from SOURCE_PAGE where COMPANY_ID=?";
    private static final String SQL_QUERY_TARGET_PAGE = "select ID from TARGET_PAGE where SOURCE_PAGE_ID in ";
    private static final String SQL_QUERY_TASK_INFO_BY_COMPANY_ID = "select TASK_ID from TASK_INFO where COMPANY_ID=?";
    private static final String SQL_QUERY_TASK_INFO_BY_WORKFLOW_ID = "select TASK_ID from TASK_INFO where WORKFLOW_ID in ";
    private static final String SQL_QUERY_TB_TERMBASE = "select TBID from TB_TERMBASE where COMPANYID=?";
    private static final String SQL_QUERY_TEMPLATE = "select ID from TEMPLATE where SOURCE_PAGE_ID in ";
    private static final String SQL_QUERY_TM_PROFILE = "select ID from TM_PROFILE where PROJECT_TM_ID_FOR_SAVE in ";
    private static final String SQL_QUERY_TM3_TM = "select ID from TM3_TM where SHAREDSTORAGEID=?";
    private static final String SQL_QUERY_USER_CALENDAR = "select ID from USER_CALENDAR where CALENDAR_ID in ";
    private static final String SQL_QUERY_USER_CALENDAR_WORKING_DAY = "select ID from USER_CALENDAR_WORKING_DAY where CALENDAR_ID in ";
    private static final String SQL_QUERY_USER_DEFAULT_ROLES = "select ID from USER_DEFAULT_ROLES where USER_ID in ";
    private static final String SQL_QUERY_WORKFLOW_REQUEST = "select ID from WORKFLOW_REQUEST where JOB_ID in ";
    private static final String SQL_QUERY_WORKFLOW_TEMPLATE_ID = "select ID from WORKFLOW_TEMPLATE where COMPANYID=?";
    private static final String SQL_QUERY_WORKFLOW_TEMPLATE_PROCESSDEFINITION_ID = "select IFLOW_TEMPLATE_ID from WORKFLOW_TEMPLATE where COMPANYID=?";
    private static final String SQL_QUERY_WORKFLOW = "select IFLOW_INSTANCE_ID from WORKFLOW where COMPANY_ID=?";
    private static final String SQL_QUERY_XML_RULE = "select ID from XML_RULE where COMPANY_ID=?";
    // hql for query
//    private static final String HQL_QUERY_REQUEST = "from RequestImpl r where r.companyId=?";
    // sql for update
    private static final String SQL_UPDATE_COMPANY_STATE_NULL_ALL = "update COMPANY set STATE=null where STATE='"
            + Company.STATE_DELETING + "'";
    private static final String SQL_UPDATE_COMPANY_STATE_DELETING = "update COMPANY set STATE='"
            + Company.STATE_DELETING + "' where ID=?";
    private static final String SQL_UPDATE_COMPANY_STATE_NULL = "update COMPANY set STATE=null where ID=?";
    private static final String SQL_UPDATE_EXPORT_BATCH_EVENT = "update EXPORT_BATCH_EVENT set TASK_ID=null where TASK_ID in ";
    private static final String SQL_UPDATE_JBPM_TRANSITION_FROM_ = "update JBPM_TRANSITION set FROM_=null where FROM_ in ";
    private static final String SQL_UPDATE_JBPM_TRANSITION_TO_ = "update JBPM_TRANSITION set TO_=null where TO_ in ";
    private static final String SQL_UPDATE_JBPM_PROCESSINSTANCE_ROOTTOKEN_ = "update JBPM_PROCESSINSTANCE set ROOTTOKEN_=null where ID_ in ";
    private static final String SQL_UPDATE_JBPM_PROCESSINSTANCE_SUPERPROCESSTOKEN_ = "update JBPM_PROCESSINSTANCE set SUPERPROCESSTOKEN_=null where ID_ in ";
    private static final String SQL_UPDATE_JBPM_PROCESSDEFINITION = "update JBPM_PROCESSDEFINITION set STARTSTATE_=null where STARTSTATE_ in ";
    private static final String SQL_UPDATE_JBPM_TASK_TASKNODE_BY_PROCESSDEFINITION_ = "update JBPM_TASK set TASKNODE_=null where PROCESSDEFINITION_ in ";
    private static final String SQL_UPDATE_JBPM_TASK_TASKNODE_BY_TASKNODE_ = "update JBPM_TASK set TASKNODE_=null where TASKNODE_ in ";
//    private static final String SQL_UPDATE_REQUEST_JOB_ID = "update REQUEST set JOB_ID=null where JOB_ID in ";
    private static final String SQL_UPDATE_SOURCE_PAGE = "update SOURCE_PAGE set PREVIOUS_PAGE_ID=null where ID in ";
    private static final String SQL_UPDATE_SURCHARGE = "update SURCHARGE set CURRENCY_CONV_ID=null where CURRENCY_CONV_ID in ";
    private static final String SQL_UPDATE_TARGET_PAGE_WORKFLOW_IFLOW_INSTANCE_ID = "update TARGET_PAGE set WORKFLOW_IFLOW_INSTANCE_ID=null where WORKFLOW_IFLOW_INSTANCE_ID in ";
    private static final String SQL_UPDATE_WORKFLOW_REQUEST_WFTEMPLATE = "delete from WORKFLOW_REQUEST_WFTEMPLATE where WORKFLOW_REQUEST_ID in ";
    // sql for drop
    private static final String SQL_DROP = "drop table if exists ";
    
    // SQL sentences for removing job
    private static final String SQL_JOB_QUERY_SOURCE_PAGE = "select PAGE_ID from REQUEST where JOB_ID=?";
    private static final String SQL_JOB_DELETE_REQUEST = "delete from REQUEST where JOB_ID in ";
    private static final String SQL_JOB_DELETE_SOURCE_PAGE = "delete from SOURCE_PAGE where ID in ";
    private static final String SQL_JOB_UPDATE_SOURCE_PAGE = "update REQUEST set PAGE_ID=null where PAGE_ID in ";
    private static final String SQL_JOB_QUERY_TASK = "select ti.TASK_ID from TASK_INFO ti, WORKFLOW wf where ti.WORKFLOW_ID=wf.IFLOW_INSTANCE_ID and wf.JOB_ID=?";
    private static final String SQL_JOB_QUERY_WORKFLOW = "select IFLOW_INSTANCE_ID from WORKFLOW where JOB_ID=?";
    private static final String SQL_JOB_DELETE_WORKFLOW = "delete from WORKFLOW where IFLOW_INSTANCE_ID in ";
    private static final String SQL_JOB_DELETE_JOB = "delete from JOB where ID in ";
    private static final String SQL_JOB_QUERY_JOB = "select ID from JOB where ID=?";

    private static final String SQL_JOB_QUERY_CORPUS = "select CUV_ID from SOURCE_PAGE where ID in ";

    private static final String SQL_JOB_QUERY_CORPUS_UNIT = "select CU_ID from CORPUS_UNIT_VARIANT where ID in ";

    private static final String SQL_JOB_DELETE_CORPUS_UNIT = "delete from CORPUS_UNIT where ID in ";
    private static final String SQL_JOB_DELETE_CORPUS_UNIT_VARIANT = "delete from CORPUS_UNIT_VARIANT where CU_ID in ";
    private static final String SQL_JOB_DELETE_CORPUS_MAP = "delete from CORPUS_MAP where CUV_ID in ";

    private static final String SQL_JOB_QUERY_COST = "select ID from COST where COSTABLE_OBJECT_TYPE='J' and COSTABLE_OBJECT_ID=?";
    private static final String SQL_JOB_QUERY_COST_WORKFLOW = "select ID from COST where COSTABLE_OBJECT_TYPE='W' and COSTABLE_OBJECT_ID in ";
    private static final String SQL_JOB_QUERY_COST_TASK = "select ID from COST where COSTABLE_OBJECT_TYPE='T' and COSTABLE_OBJECT_ID in ";
    private static final String SQL_JOB_DELETE_COST = "delete from COST where ID in ";
//    private static final String SQL_JOB_DELETE_COST_WORKFLOW = "delete from COST where COSTABLE_OBJECT_TYPE='W' and COSTABLE_OBJECT_ID in";
//    private static final String SQL_JOB_DELETE_COST_TASK = "delete from COST where COSTABLE_OBJECT_TYPE='T' and COSTABLE_OBJECT_ID in ";

    private static final String SQL_JOB_DELETE_COMMENTS = "delete from COMMENTS where COMMENT_OBJECT_TYPE='J' and COMMENT_OBJECT_ID=?";
    private static final String SQL_JOB_DELETE_TASK_COMMENTS = "delete from COMMENTS where COMMENT_OBJECT_TYPE='T' and COMMENT_OBJECT_ID in ";
    private static final String SQL_JOB_DELETE_TASK_INTERIM = "delete from TASK_INTERIM where TASK_ID in ";
    
//    private static final String SQL_JOB_DELETE_ADDING_SOURCE_PAGE = "delete from Adding_Source_Page where JOB_ID=?";
//    private static final String SQL_JOB_DELETE_UPDATED_SOURCE_PAGE = "delete from Updated_Source_Page where JOB_ID=?";
//    private static final String SQL_JOB_DELETE_CVS_SOURCE_FILES = "delete from CVS_SOURCE_FILES where JOB_ID=?";

    private static final String SQL_JOB_QUERY_ISSUE = "select ID from ISSUE where ISSUE_OBJECT_TYPE='S' and ISSUE_OBJECT_ID in ";
    private static final String SQL_JOB_DELETE_ISSUE_HISTORY = "delete from ISSUE_HISTORY where ISSUE_ID in ";
    private static final String SQL_JOB_DELETE_ISSUE = "delete from ISSUE where ID in ";

    private static final String SQL_JOB_QUERY_LEVERAGE_GROUP = "select LG_ID from SOURCE_PAGE_LEVERAGE_GROUP where SP_ID in ";

    private static final String SQL_JOB_QUERY_TASK_COMMENTS = "select ID from COMMENTS where COMMENT_OBJECT_TYPE='T' and COMMENT_OBJECT_ID in ";

    private static final String SQL_JOB_QUERY_EXTERNAL_PAGE = "select EXTERNAL_PAGE_ID from SOURCE_PAGE where ID in ";

    private static final String SQL_JOB_QUERY_PAGE_TM = "select ID from PAGE_TM where PAGE_NAME in ";

    private static final String SQL_JOB_QUERY_PAGE_TM_TU_L = "select ID from PAGE_TM_TU_L where TM_ID in ";
    private static final String SQL_JOB_QUERY_PAGE_TM_TU_T = "select ID from PAGE_TM_TU_T where TM_ID in ";
    private static final String SQL_JOB_DELETE_PAGE_TM_TU_L = "delete from PAGE_TM_TU_L where TM_ID in ";
    private static final String SQL_JOB_DELETE_PAGE_TM_TU_T = "delete from PAGE_TM_TU_T where TM_ID in ";
    private static final String SQL_JOB_DELETE_PAGE_TM_TUV_L = "delete from PAGE_TM_TUV_L where TU_ID in ";
    private static final String SQL_JOB_DELETE_PAGE_TM_TUV_T = "delete from PAGE_TM_TUV_T where TU_ID in ";

    private static final String SQL_JOB_DELETE_PAGE_TM = "delete from PAGE_TM where ID in ";

    private static final String SQL_JOB_QUERY_ATTRIBUTES = "select Attribute_ID from JOB_ATTRIBUTE where JOB_ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_DATE = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='date' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_FILE = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='file' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_FLOAT = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='float' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_INT = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='int' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_TEXT = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='text' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_LIST = "select CONDITION_ID from ATTRIBUTE_CLONE where TYPE='list' and ID in ";
    private static final String SQL_JOB_QUERY_ATTRIBUTES_FILE_ID = "select ID from ATTRIBUTE_CLONE where TYPE='file' and ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_DATE = "delete from ATTRIBUTE_CONDITION_DATE where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_FILE = "delete from ATTRIBUTE_CONDITION_FILE where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_FLOAT = "delete from ATTRIBUTE_CONDITION_FLOAT where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_INT = "delete from ATTRIBUTE_CONDITION_INT where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_TEXT = "delete from ATTRIBUTE_CONDITION_TEXT where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_LIST = "delete from ATTRIBUTE_CONDITION_LIST where ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTES_LIST_SELECT = "delete from SELECT_OPTION where LIST_CONDITION_ID in ";
    private static final String SQL_JOB_DELETE_ATTRIBUTE = "delete from ATTRIBUTE_CLONE where ID in ";

    public CompanyRemoval(String companyId)
    {
        try
        {
            company = ServerProxy.getJobHandler().getCompanyById(
                    Long.parseLong(companyId));
            
            runningMessage = "Deleting company " + company.getCompanyName()
                    + " records in table ";
            doneMessage = "Done deleting company " + company.getCompanyName()
                    + " records in table ";
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get company with id: " + companyId, e);
        }
    }

    public CompanyRemoval(long companyId)
    {
        try
        {
            company = ServerProxy.getJobHandler().getCompanyById(companyId);

            runningMessage = "Deleting company " + company.getCompanyName()
                    + " records in table ";
            doneMessage = "Done deleting company " + company.getCompanyName()
                    + " records in table ";
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get company with id: " + companyId, e);
        }
    }

    /**
     * Removes a company and all its associated data from DB and disk.
     */
    public void removeCompany()
    {
        if (company == null)
        {
            return;
        }
        if (Company.STATE_DELETING.equals(company.getState()))
        {
            return;
        }
        if (company.getId() == 1)
        {
            CATEGORY.warn("Super company is NOT allowed to be removed from the system!");
            return;
        }
        Connection conn = null;
        try
        {
            conn = ConnectionPool.getConnection();
            updateCompanyStateToDeleting(conn);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to update company state to DELETING", e);
            return;
        }
        finally
        {
            HibernateUtil.closeSession();
        }
        final Connection cnn = conn;
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                // run the remove process in a new thread
                runRemoveCompany(cnn);
            }
        };
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Clears company state to NULL during system starting up.
     */
    public static void clearCompanyState()
    {
        Connection conn = null;
        try
        {
            conn = ConnectionPool.getConnection();
            conn.setAutoCommit(false);
            execOnce(conn, SQL_UPDATE_COMPANY_STATE_NULL_ALL);
            conn.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to clear company state", e);
        }
        finally
        {
            ConnectionPool.silentReturnConnection(conn);
        }
    }

    /**
     * Remove job data from database when user discard a job
     * 
     * @param job
     *            Job instance that needs to be removed
     */
    public void removeJob(Job job)
    {
        boolean isRecreateJob = false;
        removeJob(job, isRecreateJob);
    }

    /**
     * Remove job data from database when user discard a job
     * 
     * @param job
     *            Job -- instance that needs to be removed
     * @param isRecreateJob
     *            -- Default true; if false, do not delete data in "job" table.
     */
    public void removeJob(Job job, boolean isRecreateJob)
    {
        Connection conn = null;
        try
        {
            // set variables used in job removal
            isJobRemoval = true;
            runningMessage = "Deleting job data in table ";
            doneMessage = "Done deleting job data in table ";

            conn = DbUtil.getConnection();
            conn.setAutoCommit(true);
            
            long jobId = job.getId();
            String jobName = job.getJobName();
            long companyId = job.getCompanyId();
            
            List<List<Object>> jobIds = queryBatchList(conn, SQL_JOB_QUERY_JOB, jobId);
            removeAddingSourcePage(conn, jobIds);
            removeUpdatedSourcePage(conn, jobIds);
            
            removeScoreByJobId(conn, jobIds);
            
            removeWorkflowRequest(conn, jobIds);
            removeIpTmIndex(conn, jobIds);
            removeIpTmSrcL(conn, jobIds);
            removeIpTmSrcT(conn, jobIds);
            
            //remove job cost
            removeJobCost(conn, jobId);
            
            HashMap<String, String> jobFileInfo = getJobFileInfo(job);
            
            // get source page ids
            List<List<Object>> ids = queryBatchList(conn,
                    SQL_JOB_QUERY_SOURCE_PAGE, jobId);

            // remove source pages
            removeSourcePage(conn, ids);

            // remove workflows
            ids = queryBatchList(conn, SQL_JOB_QUERY_WORKFLOW, jobId);
            removeWorkflow(conn, ids);
                // this should be after "removeWorkflow(..)".
            removeExportBatchEvent(conn, jobIds);

            if (!isRecreateJob)
            {
                // remove job comments
                removeJobComments(conn, jobId);
                
                // remove job attribute
                removeJobAttribute(conn, jobIds);
                
                // for git files
                removeGitConnectorJobByJobId(conn, jobIds);

                removeBlaiseConnectorJobByJobId(conn, jobIds);
            }

            // remove requests
            ArrayList<Request> requests = new ArrayList<Request>(job.getRequestList());
            ArrayList<String> eventFlowXmls = new ArrayList<String>();
            for (Request ri : requests) {
                eventFlowXmls.add(ri.getEventFlowXml());
            }
            removeRequest(conn, jobIds);

            if (!isRecreateJob)
            {
                logStart("JOB");
                execOnce(conn, "delete from JOB where ID=?", jobId);
                logEnd("JOB");
            }

            removeJobFiles(jobFileInfo, isRecreateJob);
            if (eventFlowXmls.size() > 0) {
                for (String eventFlowXml : eventFlowXmls) {
                    CompanyFileRemoval fileRemoval = new CompanyFileRemoval(eventFlowXml);
                    fileRemoval.removeConverterFile();
                }
            }

            if (!isRecreateJob)
            {
                // Drop job level tables.
                dropTuTuvLmTables(conn, jobId);
            }

            LogManager.log(LogType.JOB, LogManager.EVENT_TYPE_REMOVE, jobId,
                    "Delete job [" + jobName + "]", companyId);
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (SQLException e1)
            {
            }
            CATEGORY.error("Error found in removing job. ", e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }
    
    private void removeIssues(Connection conn, List<List<Object>> tuvIds) throws SQLException
    {
        List<List<Object>> issueIds = queryBatchList(conn, SQL_JOB_QUERY_ISSUE, tuvIds);
        if (issueIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ISSUE_HISTORY, issueIds);
            exec(conn, SQL_JOB_DELETE_ISSUE, issueIds);
            
            removeIssueFiles(tuvIds);
        }
    }

    private void removeIssueFiles(List<List<Object>> tuvIds)
    {
        if (tuvIds == null || tuvIds.size() == 0)
            return;
        
        ArrayList<String> ids = new ArrayList<String>();
        Long tuvId = null;
        for (List<Object> list : tuvIds) {
            if (list.size() > 0) {
                for (Object object : list) {
                    tuvId = (Long) object;
                    ids.add("tuv_" + tuvId.toString());
                }
            }
        }
        
        String webRoot = FileUploadHelper.DOCROOT + "terminologyImg";
        File webRootDir = new File(webRoot);
        String tmp = "";
        if (webRootDir.exists() && webRootDir.isDirectory()) {
            File[] fileList = webRootDir.listFiles();
            for (File file : fileList) {
                if (file.isDirectory())
                    continue;
                tmp = file.getName();
                tmp = tmp.substring(0, tmp.lastIndexOf("."));
                if (ids.contains(tmp))
                    FileUtil.deleteFile(file);
            }
        }
    }

    private void removeJobComments(Connection conn, long jobId) throws SQLException
    {
        logStart("COMMENTS");
        execOnce(conn, SQL_JOB_DELETE_COMMENTS, jobId);
        logEnd("COMMENTS");
    }

    private void removeJobFiles(HashMap<String, String> jobFileInfo,
            boolean isRecreateJob)
    {
        String jobId = jobFileInfo.get("jobId");
        String jobName = jobFileInfo.get("jobName");
        String companyId = jobFileInfo.get("companyId");
        String sourceLocale = jobFileInfo.get("sourceLocale");
        String targetLocales = jobFileInfo.get("targetLocales");
        String comments = jobFileInfo.get("comments");
        String userId = jobFileInfo.get("userId");
        
        logStart("Remove files of job " + jobName);
        // remove files in doc dir
        File jobDocFileDir = AmbFileStoragePathUtils.getCxeDocDir(companyId);
        File previewDir = null;

        // If this job's name is another job's id, we do not delete files by name.
        try {
            long id = Long.parseLong(jobName);
            Job job = ServerProxy.getJobHandler().getJobById(id);
            if (job != null) {
                deleteJobFiles(jobId, null, jobDocFileDir, sourceLocale);                
            }
        } catch (Exception e) {
            deleteJobFiles(jobId, jobName, jobDocFileDir, sourceLocale);
        }

        previewDir = new File(AmbFileStoragePathUtils.getPdfPreviewDir(companyId), userId);
        deleteJobFiles(jobId, jobName, previewDir, sourceLocale);
        
        if (StringUtil.isNotEmpty(targetLocales)) {
            String[] targetLocalesArray = targetLocales.split(",");
            for (String targetLocale : targetLocalesArray) {
                deleteJobFiles(jobId, jobName, jobDocFileDir, targetLocale);
                deleteJobFiles(jobId, jobName, previewDir, targetLocale);
            }
        }
        
        // remove secondary target files
        jobDocFileDir = AmbFileStoragePathUtils.getStfParentDir(companyId);
        deleteJobFiles(jobId, null, jobDocFileDir, null);
        
        // remove comment files
        if (!isRecreateJob)
        {
            jobDocFileDir = AmbFileStoragePathUtils
                    .getCommentReferenceDir(companyId);
            if (StringUtil.isNotEmpty(comments)) {
                String[] commentsArray = comments.split(",");
                for (String comment : commentsArray) {
                    deleteJobFiles(comment, null, jobDocFileDir, null);
                }
            }
        }

        // remove corpus files
        jobDocFileDir = AmbFileStoragePathUtils.getCorpusDir(companyId);
        deleteJobFiles(jobId, jobName, jobDocFileDir, sourceLocale);
        deleteCorpusFiles(jobId, jobName, jobDocFileDir, sourceLocale);
        
        jobDocFileDir = new File(
                AmbFileStoragePathUtils.getCxeDocDir(companyId), "passolo");
        deleteJobFiles(jobId, jobName, jobDocFileDir, null);
        
        jobDocFileDir = AmbFileStoragePathUtils.getCustomerDownloadDir(companyId);
        deleteJobFiles(jobId, jobName, jobDocFileDir, null);

        deleteQAChecksReportFile(jobId, companyId);

        logEnd("Remove files of job " + jobName);
    }

    /**
     * Remove QA and DITA checks report files.
     * @param jobId
     * @param companyId
     */
    private void deleteQAChecksReportFile(String jobId, String companyId)
    {
        String basePath = AmbFileStoragePathUtils.getReportsDir(companyId)
                .getAbsolutePath();

        StringBuilder ditaChecksPath = new StringBuilder(basePath);
        ditaChecksPath.append(File.separator);
        ditaChecksPath.append(DITAQAChecker.DITA_QA_CHECKS_REPORT);
        ditaChecksPath.append(File.separator);
        ditaChecksPath.append(jobId);
        File file = new File(ditaChecksPath.toString());
        FileUtil.deleteFile(file);

        StringBuilder qaChecksPath = new StringBuilder(basePath);
        qaChecksPath.append(File.separator);
        qaChecksPath.append(ReportConstants.REPORT_QA_CHECKS_REPORT);
        qaChecksPath.append(File.separator);
        qaChecksPath.append(jobId);
        file = new File(qaChecksPath.toString());
        FileUtil.deleteFile(file);
    }

    /**
     * Remove files generated by xlsx or pptx
     * @param jobId
     * @param jobName
     * @param jobDocFileDir
     * @param sourceLocale
     */
    private void deleteCorpusFiles(String jobId, String jobName,
            File jobDocFileDir, String sourceLocale)
    {
        File[] subDirs = jobDocFileDir.listFiles();
        for (File dir : subDirs) {
            if (dir.isFile() || dir.getName().equals(sourceLocale))
                continue;
            deleteJobFiles(jobId, jobName, dir, null);
        }
    }

    private void deleteJobFiles(String jobId, String jobName, File baseDir,
            String locale)
    {
        File dir = null;
        if (StringUtil.isEmpty(locale))
            locale = "";
        
        if (StringUtil.isNotEmpty(jobId))
        {
            dir = new File(baseDir, locale + File.separator + jobId);
            FileUtil.deleteFile(dir);
            dir = new File(baseDir, locale + File.separator
                    + AmbFileStoragePathUtils.WEBSERVICE_DIR + File.separator
                    + jobId);
            FileUtil.deleteFile(dir);
        }
        if (StringUtil.isNotEmpty(jobName))
        {
            dir = new File(baseDir, locale + File.separator + jobName);
            FileUtil.deleteFile(dir);
            dir = new File(baseDir, locale + File.separator
                    + AmbFileStoragePathUtils.WEBSERVICE_DIR + File.separator
                    + jobName);
            FileUtil.deleteFile(dir);
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> getJobFileInfo(Job job)
    {
        HashMap<String, String> jobFileInfo = new HashMap<String, String>();
        jobFileInfo.put("jobId", String.valueOf(job.getId()));
        jobFileInfo.put("jobName", job.getJobName());
        jobFileInfo.put("userId", job.getCreateUserId());
        jobFileInfo.put("companyId", String.valueOf(job.getCompanyId()));
        jobFileInfo.put("sourceLocale", job.getSourceLocale().toString());
        
        // get target locale of job
        ArrayList<Workflow> workflows = new ArrayList<Workflow>(job.getWorkflows());
        String targetLocales = "";
        for (Workflow wf : workflows) {
            targetLocales += wf.getTargetLocale() + ",";
        }
        if (targetLocales.length() > 0)
            targetLocales = targetLocales.substring(0, targetLocales.length() - 1);
        jobFileInfo.put("targetLocales", targetLocales);
        
        ArrayList<Comment> comments = new ArrayList<Comment>(job.getJobComments());
        String commentIds = "";
        for (Comment comment : comments)
            commentIds += comment.getId() + ",";
        if (commentIds.length() > 0)
            commentIds = commentIds.substring(0, commentIds.length() - 1);
        jobFileInfo.put("comments", commentIds);
        
        return jobFileInfo;
    }

    /**
     * Remove job cost data from database
     * There are 3 kind of costs should be removed.
     * 1.Job
     * 2.Workflow
     * 3.Task
     * 
     * @param conn Connection instance
     * @param job Job
     * @throws SQLException
     */
    private void removeJobCost(Connection conn, long jobId) throws SQLException
    {
        List<List<Object>> costIds = queryBatchList(conn, SQL_JOB_QUERY_COST, jobId);
        List<List<Object>> workflowIds = queryBatchList(conn, SQL_JOB_QUERY_WORKFLOW, jobId);
        List<List<Object>> taskIds = queryBatchList(conn, SQL_JOB_QUERY_TASK, jobId);
        costIds.addAll(queryBatchList(conn, SQL_JOB_QUERY_COST_WORKFLOW, workflowIds));
        costIds.addAll(queryBatchList(conn, SQL_JOB_QUERY_COST_TASK, taskIds));
        
        if (costIds.size() > 0) {
            removeCostByWordCount(conn, costIds);
            removeSurcharge(conn, costIds);
        }
        
        exec(conn, SQL_JOB_DELETE_COST, costIds);
    }

    private void runRemoveCompany(Connection conn)
    {
        String companyName = company.getCompanyName();
        CATEGORY.info("Deleting company " + companyName);
        long start = System.currentTimeMillis();
        try
        {
            List<Long> jobIds = getJobIdsByCompanyId(company.getId());
            // do not use a transaction in order to remove at least something
            // before meeting with errors during the process
            // remove permission groups
            removePermissionGroup(conn);
            // remove users
            removeUser(conn);
            // remove roles from LDAP
            removeRole();
            // remove calendars
            removeCalendar(conn);
            // remove requests
            removeRequest(conn, null);
            // remove leverage match tables
            removeLeverageMatch(conn);
            // remove source pages
            removeSourcePage(conn, null);
            // remove tasks
            removeTaskInfo(conn);
            // remove scores
            removeScoreByCompanyId(conn);
            // remove scorecardCategory
            removeScorecardCategory(conn);
            //remove git connector Job
            removeGitConnectorJob(conn);
            //remove git connector file mapping
            removeGitConnectorFileMapping(conn);
            //remove git connector
            removeGitConnector(conn);
            // remove mindtouch target servers
            removeMindTouchTargetServers(conn);
            // remove mindtouch connectors
            removeMindTouchConnectors(conn);
            // remove blaise connector job
            removeBlaiseConnectorJob(conn);
            // remove blaise connectors
            removeBlaiseConnectors(conn);
            //remove post review category
            romovePostReviewCategory(conn);
            // remove workflows
            removeWorkflow(conn, null);
            //remove workflow state post profile
            removeWfStatePostProfile(conn);
            // remove jobs
            removeJob(conn, null);
            // remove activities
            removeActivity(conn);
            // remove attributes
            removeAttribute(conn);
            // remove categories
            removeCategory(conn);
            // remove currency conversions
            removeCurrencyConversion(conn);
            // remove cvs file profiles
            removeCvsFileProfile(conn);
            // remove cvs module mappings
            removeCvsModuleMapping(conn);
            // remove cvs servers
            removeCvsServer(conn);
            // remove export locations
            removeExportLocation(conn);
            // remove file profiles
            removeFileProfile(conn);
            // remove extensions
            removeExtension(conn);
            // remove filters
            removeFilter(conn);
            // remove filter configurations
            removeFilterConfiguration(conn);
            // remove gs editions
            removeGsEdition(conn);
            // remove holidays
            removeHoliday(conn);
            // remove MT profiles
            removeMtProfile(conn);
            // remove l10n profiles
            removeL10nProfile(conn);
            // remove locale pairs
            removeLocalePair(conn);
            // remove project tms
            removeProjectTm(conn);
            // remove rss feeds
            removeRssFeed(conn);
            // remove segmentation rules
            removeSegmentationRule(conn);
            // remove sso user mappings
            removeSsoUserMapping(conn);
            // remove system parameters
            removeSystemParameter(conn);
            // remove term bases
            removeTermbase(conn);
            // remove Tm3 tables
            removeTm3Tables(conn);
            // remove Tm3 Tms
            removeTm3Tm(conn);
            // remove workflow templates
            removeWorkflowTemplate(conn);
            //remove job group
            removeJobGroup(conn);
            // remove projects
            removeProject(conn);
            // remove xml dtds
            removeXmlDtd(conn);
            // remove xml rules
            removeXmlRule(conn);
            // remove tu/tuv attributes
            removeTuTuvAttr(conn);
            // remove tuvs
            removeTuv(conn, jobIds);
            // remove tus
            removeTu(conn, jobIds);
            // remove translation_tu_tuv_index
            removeTuTuvIndex(conn);
            // remove docs folder from disk
            removeDocs();
            // remove file storage folder from disk
            removeFileStorage();
            // remove properties folder from disk
            removeProperties();
            // remove converter files based on company folder
            // removeConverterFile();
            // remove company
            removeCompany(conn);
            // NOTE: Keep the sequence of the methods above, because changing
            // the order of them may bring constraint issues during the process
        }
        catch (Exception e)
        {
            CATEGORY.error("Errors occurred when deleting company "
                    + companyName, e);
            try
            {
                updateCompanyStateToNull(conn);
            }
            catch (SQLException e1)
            {
                CATEGORY.error("Failed to clear company state", e);
            }
            return;
        }
        finally
        {
            ConnectionPool.silentReturnConnection(conn);
        }
        long end = System.currentTimeMillis();
        CATEGORY.info("Done deleting company " + companyName + ", took "
                + (end - start) + " ms");
    }

    private void backupCompany(Connection conn) throws SQLException
    {
        CATEGORY.info("Backing up company " + company.getCompanyName()
                + " records from table COMPANY to " + TABLE_BACKUP_COMPANY);
        createTableCompanyBackup(conn);
        backupCompanyData(conn);
        CATEGORY.info("Done backing up company " + company.getCompanyName()
                + " records from table COMPANY to " + TABLE_BACKUP_COMPANY);
    }

    private void backupJob(Connection conn) throws SQLException
    {
        CATEGORY.info("Backing up company " + company.getCompanyName()
                + " records from table JOB to " + TABLE_BACKUP_JOB);
        createTableJobBackup(conn);
        backupJobData(conn);
        CATEGORY.info("Done backing up company " + company.getCompanyName()
                + " records from table JOB to " + TABLE_BACKUP_JOB);
    }

    private void backupRequest(Connection conn) throws SQLException
    {
        CATEGORY.info("Backing up company " + company.getCompanyName()
                + " records from table REQUEST to " + TABLE_BACKUP_REQUEST);
        createTableRequestBackup(conn);
        backupRequestData(conn);
        CATEGORY.info("Done backing up company " + company.getCompanyName()
                + " records from table REQUEST to " + TABLE_BACKUP_REQUEST);
    }

    private void backupWorkflow(Connection conn) throws SQLException
    {
        CATEGORY.info("Backing up company " + company.getCompanyName()
                + " records from table WORKFLOW to " + TABLE_BACKUP_WORKFLOW);
        createTableWorkflowBackup(conn);
        backupWorkflowData(conn);
        CATEGORY.info("Done backing up company " + company.getCompanyName()
                + " records from table WORKFLOW to " + TABLE_BACKUP_WORKFLOW);
    }

    private void backupCompanyData(Connection conn) throws SQLException
    {
        StringBuilder columns = new StringBuilder();
        columns.append("ID,");
        columns.append(" NAME,");
        columns.append(" DESCRIPTION,");
        columns.append(" IS_ACTIVE,");
        columns.append(" ENABLE_IP_FILTER,");
        columns.append(" ENABLE_TM_ACCESS_CONTROL,");
        columns.append(" ENABLE_TB_ACCESS_CONTROL,");
        columns.append(" ENABLE_SSO_LOGIN,");
        columns.append(" SSO_IDP_URL,");
        columns.append(" SSO_LOGIN_URL,");
        columns.append(" SSO_LOGOUT_URL,");
        columns.append(" SSO_WS_ENDPOINT,");
        columns.append(" TM_VERSION,");
        columns.append(" SESSION_TIME,");
        columns.append(" EMAIL,");
        columns.append(" STATE,");
        columns.append(" BIG_DATA_STORE_LEVEL,");
        columns.append(" MIGRATE_PROCESSING,");
        columns.append(" ENABLE_DITA_CHECKS,");
        columns.append(" ENABLE_WORKFLOW_STATE_POSTS,");
        columns.append(" ENABLE_QA_CHECKS");

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TABLE_BACKUP_COMPANY);
        sql.append(" (");
        sql.append(columns.toString());
        sql.append(")");
        sql.append(" SELECT ");
        sql.append(columns.toString());
        sql.append(" FROM COMPANY WHERE ID=?");

        execOnce(conn, sql.toString(), company.getId());
    }

    private void backupJobData(Connection conn) throws SQLException
    {
        StringBuilder columns = new StringBuilder();
        columns.append("ID,");
        columns.append(" NAME,");
        columns.append(" STATE,");
        columns.append(" ORIGINAL_STATE,");
        columns.append(" CREATE_DATE,");
        columns.append(" PRIORITY,");
        columns.append(" IS_WORDCOUNT_REACHED,");
        columns.append(" TIMESTAMP,");
        columns.append(" PAGE_COUNT,");
        columns.append(" LEVERAGE_MATCH_THRESHOLD,");
        columns.append(" OVERRIDEN_WORD_COUNT,");
        columns.append(" QUOTE_DATE,");
        columns.append(" QUOTE_PO_NUMBER,");
        columns.append(" QUOTE_APPROVED_DATE,");
        columns.append(" COMPANY_ID,");
        columns.append(" L10N_PROFILE_ID,");
        columns.append(" AUTHORISER_USER_ID,");
        columns.append(" CREATE_USER_ID,");
        columns.append(" UUID,");
        columns.append(" TU_TABLE,");
        columns.append(" TU_ARCHIVE_TABLE,");
        columns.append(" TUV_TABLE,");
        columns.append(" TUV_ARCHIVE_TABLE,");
        columns.append(" LM_TABLE,");
        columns.append(" LM_ARCHIVE_TABLE,");
        columns.append(" LEVERAGE_OPTION");

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TABLE_BACKUP_JOB);
        sql.append(" (");
        sql.append(columns.toString());
        sql.append(")");
        sql.append(" SELECT ");
        sql.append(columns.toString());
        sql.append(" FROM JOB WHERE COMPANY_ID=?");

        execOnce(conn, sql.toString(), company.getId());
    }

    private void backupRequestData(Connection conn) throws SQLException
    {
        StringBuilder columns = new StringBuilder();
        columns.append("ID,");
        columns.append(" L10N_PROFILE_ID,");
        columns.append(" TYPE,");
        columns.append(" EVENT_FLOW_XML,");
        columns.append(" EXCEPTION_XML,");
        columns.append(" JOB_ID,");
        columns.append(" PAGE_ID,");
        columns.append(" DATA_SOURCE_ID,");
        columns.append(" IS_PAGE_CXE_PREVIEWABLE,");
        columns.append(" BATCH_ID,");
        columns.append(" BATCH_PAGE_COUNT,");
        columns.append(" BATCH_PAGE_NUMBER,");
        columns.append(" BATCH_DOC_PAGE_COUNT,");
        columns.append(" BATCH_DOC_PAGE_NUMBER,");
        columns.append(" BATCH_JOB_NAME,");
        columns.append(" BASE_HREF,");
        columns.append(" TIMESTAMP,");
        columns.append(" COMPANY_ID");

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TABLE_BACKUP_REQUEST);
        sql.append(" (");
        sql.append(columns.toString());
        sql.append(")");
        sql.append(" SELECT ");
        sql.append(columns.toString());
        sql.append(" FROM REQUEST WHERE COMPANY_ID=?");

        execOnce(conn, sql.toString(), company.getId());
    }

    private void backupWorkflowData(Connection conn) throws SQLException
    {
        StringBuilder columns = new StringBuilder();
        columns.append("IFLOW_INSTANCE_ID,");
        columns.append(" STATE,");
        columns.append(" TARGET_LOCALE_ID,");
        columns.append(" JOB_ID,");
        columns.append(" TYPE,");
        columns.append(" DISPATCH_DATE,");
        columns.append(" ESTIMATED_COMPLETION_DATE,");
        columns.append(" COMPLETED_DATE,");
        columns.append(" EXPORT_DATE,");
        columns.append(" FRACTION,");
        columns.append(" DURATION,");
        columns.append(" TIMESTAMP,");
        columns.append(" PLANNED_COMPLETION_DATE,");
        columns.append(" EXACT_CONTEXT_WORD_COUNT,");
        columns.append(" EXACT_SEGMENT_TM_WORD_COUNT,");
        columns.append(" FUZZY_LOW_WORD_COUNT,");
        columns.append(" FUZZY_MED_WORD_COUNT,");
        columns.append(" FUZZY_MED_HI_WORD_COUNT,");
        columns.append(" FUZZY_HI_WORD_COUNT,");
        columns.append(" NO_MATCH_WORD_COUNT,");
        columns.append(" REPETITION_WORD_COUNT,");
        columns.append(" THRESHOLD_FUZZY_HI_WORD_COUNT,");
        columns.append(" THRESHOLD_FUZZY_MED_HI_WORD_COUNT,");
        columns.append(" THRESHOLD_FUZZY_MED_WORD_COUNT,");
        columns.append(" THRESHOLD_FUZZY_LOW_WORD_COUNT,");
        columns.append(" THRESHOLD_NO_MATCH_WORD_COUNT,");
        columns.append(" TOTAL_WORD_COUNT,");
        columns.append(" IS_ESTI_CMPLTN_DATE_OVERRIDED,");
        columns.append(" ESTI_TRANSLATE_CMPLTN_DATE,");
        columns.append(" IS_ESTI_TRANS_DATE_OVERRIDED,");
        columns.append(" TRANSLATION_COMPLETED_DATE,");
        columns.append(" COMPANY_ID,");
        columns.append(" IN_CONTEXT_MATCH_WORD_COUNT,");
        columns.append(" NO_USE_IC_MATCH_WORD_COUNT,");
        columns.append(" TOTAL_EXACT_MATCH_WORD_COUNT,");
        columns.append(" PRIORITY,");
        columns.append(" MT_EXACT_MATCH_WORD_COUNT");

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TABLE_BACKUP_WORKFLOW);
        sql.append(" (");
        sql.append(columns.toString());
        sql.append(")");
        sql.append(" SELECT ");
        sql.append(columns.toString());
        sql.append(" FROM WORKFLOW WHERE COMPANY_ID=?");

        execOnce(conn, sql.toString(), company.getId());
    }

    private void createTableCompanyBackup(Connection conn) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(TABLE_BACKUP_COMPANY);
        sql.append(" (");
        sql.append(" ID BIGINT,");
        sql.append(" NAME VARCHAR(40) NOT NULL,");
        sql.append(" DESCRIPTION VARCHAR(4000),");
        sql.append(" IS_ACTIVE CHAR(1) NOT NULL,");
        sql.append(" ENABLE_IP_FILTER CHAR(1) DEFAULT 'Y',");
        sql.append(" ENABLE_TM_ACCESS_CONTROL CHAR(1) DEFAULT 'N',");
        sql.append(" ENABLE_TB_ACCESS_CONTROL CHAR(1) DEFAULT 'N',");
        sql.append(" ENABLE_SSO_LOGIN CHAR(1) DEFAULT 'N' NOT NULL,");
        sql.append(" SSO_IDP_URL VARCHAR(256) NULL,");
        sql.append(" SSO_LOGIN_URL VARCHAR(256) NULL,");
        sql.append(" SSO_LOGOUT_URL VARCHAR(256) NULL,");
        sql.append(" SSO_WS_ENDPOINT VARCHAR(256) NULL,");
        sql.append(" TM_VERSION SMALLINT DEFAULT 2 NOT NULL,");
        sql.append(" SESSION_TIME VARCHAR(10) DEFAULT NULL,");
        sql.append(" EMAIL VARCHAR(100) DEFAULT NULL,");
        sql.append(" STATE VARCHAR(40) DEFAULT NULL,");
        sql.append(" BIG_DATA_STORE_LEVEL SMALLINT(1) DEFAULT 1,");
        sql.append(" MIGRATE_PROCESSING INT DEFAULT 0,");
        sql.append(" ENABLE_DITA_CHECKS CHAR(1) DEFAULT 'N',");
        sql.append(" ENABLE_WORKFLOW_STATE_POSTS CHAR(1) DEFAULT 'N',");
        sql.append(" ENABLE_QA_CHECKS char(1) DEFAULT 'N'");
        sql.append(");");

        execOnce(conn, sql.toString());
    }

    private void createTableJobBackup(Connection conn) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(TABLE_BACKUP_JOB);
        sql.append(" (");
        sql.append(" ID BIGINT,");
        sql.append(" NAME VARCHAR(320) NOT NULL,");
        sql.append(" STATE VARCHAR(40),");
        sql.append(" ORIGINAL_STATE VARCHAR(40),");
        sql.append(" CREATE_DATE DATETIME NOT NULL,");
        sql.append(" PRIORITY INT(3) NOT NULL,");
        sql.append(" IS_WORDCOUNT_REACHED CHAR(1) NOT NULL,");
        sql.append(" TIMESTAMP DATETIME NOT NULL,");
        sql.append(" PAGE_COUNT INT(10) NOT NULL,");
        sql.append(" LEVERAGE_MATCH_THRESHOLD INT DEFAULT 50,");
        sql.append(" OVERRIDEN_WORD_COUNT INT(10),");
        sql.append(" QUOTE_DATE VARCHAR(320),");
        sql.append(" QUOTE_PO_NUMBER VARCHAR(320),");
        sql.append(" QUOTE_APPROVED_DATE VARCHAR(320),");
        sql.append(" COMPANY_ID BIGINT NOT NULL,");
        sql.append(" L10N_PROFILE_ID BIGINT DEFAULT -1,");
        sql.append(" AUTHORISER_USER_ID VARCHAR(80),");
        sql.append(" CREATE_USER_ID VARCHAR(80),");
        sql.append(" UUID VARCHAR(320),");
        sql.append(" TU_TABLE VARCHAR(128),");
        sql.append(" TU_ARCHIVE_TABLE VARCHAR(128),");
        sql.append(" TUV_TABLE VARCHAR(128),");
        sql.append(" TUV_ARCHIVE_TABLE VARCHAR(128),");
        sql.append(" LM_TABLE VARCHAR(128),");
        sql.append(" LM_ARCHIVE_TABLE VARCHAR(128),");
        sql.append(" LEVERAGE_OPTION VARCHAR(45) NOT NULL DEFAULT 'in-context'");
        sql.append(");");

        execOnce(conn, sql.toString());
    }

    private void createTableRequestBackup(Connection conn) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(TABLE_BACKUP_REQUEST);
        sql.append(" (");
        sql.append(" ID BIGINT,");
        sql.append(" L10N_PROFILE_ID BIGINT NOT NULL,");
        sql.append(" TYPE VARCHAR(35) NOT NULL,");
        sql.append(" EVENT_FLOW_XML TEXT,");
        sql.append(" EXCEPTION_XML TEXT,");
        sql.append(" JOB_ID BIGINT,");
        sql.append(" PAGE_ID BIGINT,");
        sql.append(" DATA_SOURCE_ID INT NOT NULL,");
        sql.append(" IS_PAGE_CXE_PREVIEWABLE CHAR(1) NOT NULL,");
        sql.append(" BATCH_ID VARCHAR(400),");
        sql.append(" BATCH_PAGE_COUNT INT(5),");
        sql.append(" BATCH_PAGE_NUMBER INT(5),");
        sql.append(" BATCH_DOC_PAGE_COUNT INT(5),");
        sql.append(" BATCH_DOC_PAGE_NUMBER INT(5),");
        sql.append(" BATCH_JOB_NAME VARCHAR(320),");
        sql.append(" BASE_HREF VARCHAR(2000),");
        sql.append(" TIMESTAMP DATETIME NOT NULL,");
        sql.append(" COMPANY_ID BIGINT NOT NULL");
        sql.append(");");

        execOnce(conn, sql.toString());
    }

    private void createTableWorkflowBackup(Connection conn) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(TABLE_BACKUP_WORKFLOW);
        sql.append(" (");
        sql.append(" IFLOW_INSTANCE_ID BIGINT(20),");
        sql.append(" STATE VARCHAR(40),");
        sql.append(" TARGET_LOCALE_ID BIGINT NOT NULL,");
        sql.append(" JOB_ID BIGINT NOT NULL,");
        sql.append(" TYPE VARCHAR(20) NOT NULL,");
        sql.append(" DISPATCH_DATE DATETIME,");
        sql.append(" ESTIMATED_COMPLETION_DATE DATETIME,");
        sql.append(" COMPLETED_DATE DATETIME,");
        sql.append(" EXPORT_DATE DATETIME,");
        sql.append(" FRACTION VARCHAR(25) NOT NULL,");
        sql.append(" DURATION BIGINT(20),");
        sql.append(" TIMESTAMP DATETIME NOT NULL,");
        sql.append(" PLANNED_COMPLETION_DATE DATETIME,");
        sql.append(" EXACT_CONTEXT_WORD_COUNT INT(10),");
        sql.append(" EXACT_SEGMENT_TM_WORD_COUNT INT(10),");
        sql.append(" FUZZY_LOW_WORD_COUNT INT(10),");
        sql.append(" FUZZY_MED_WORD_COUNT INT(10),");
        sql.append(" FUZZY_MED_HI_WORD_COUNT INT(10),");
        sql.append(" FUZZY_HI_WORD_COUNT INT(10),");
        sql.append(" NO_MATCH_WORD_COUNT INT(10),");
        sql.append(" REPETITION_WORD_COUNT INT(10),");
        sql.append(" THRESHOLD_FUZZY_HI_WORD_COUNT INT(10),");
        sql.append(" THRESHOLD_FUZZY_MED_HI_WORD_COUNT INT(10),");
        sql.append(" THRESHOLD_FUZZY_MED_WORD_COUNT INT(10),");
        sql.append(" THRESHOLD_FUZZY_LOW_WORD_COUNT INT(10),");
        sql.append(" THRESHOLD_NO_MATCH_WORD_COUNT INT(10),");
        sql.append(" TOTAL_WORD_COUNT INT(10),");
        sql.append(" IS_ESTI_CMPLTN_DATE_OVERRIDED CHAR(1),");
        sql.append(" ESTI_TRANSLATE_CMPLTN_DATE DATETIME,");
        sql.append(" IS_ESTI_TRANS_DATE_OVERRIDED CHAR(1),");
        sql.append(" TRANSLATION_COMPLETED_DATE DATETIME,");
        sql.append(" COMPANY_ID BIGINT NOT NULL,");
        sql.append(" IN_CONTEXT_MATCH_WORD_COUNT INT(10),");
        sql.append(" NO_USE_IC_MATCH_WORD_COUNT INT(10),");
        sql.append(" TOTAL_EXACT_MATCH_WORD_COUNT INT(10),");
        sql.append(" PRIORITY INT(3) NOT NULL DEFAULT 3");
        sql.append(");");

        execOnce(conn, sql.toString());

        // For GBS-2877 (previous workflow backup data should be reserved,so
        // have to handle column change like this)
        try
        {
            String sql1 = "ALTER TABLE workflow_backup CHANGE NO_USE_EXACT_MATCH_WORD_COUNT TOTAL_EXACT_MATCH_WORD_COUNT INT(10);";
            execOnce(conn, sql1);
        }
        catch (SQLException mustIgnoreEx)
        {
        }

        try
        {
            String sql1 = "ALTER TABLE workflow_backup ADD COLUMN MT_EXACT_MATCH_WORD_COUNT INT(10) DEFAULT 0;";
            execOnce(conn, sql1);
        }
        catch (SQLException mustIgnoreEx)
        {
        }
    }

    private void updateCompanyStateToDeleting(Connection conn)
            throws SQLException
    {
        execOnce(conn, SQL_UPDATE_COMPANY_STATE_DELETING, company.getId());
    }

    private void updateCompanyStateToNull(Connection conn) throws SQLException
    {
        execOnce(conn, SQL_UPDATE_COMPANY_STATE_NULL, company.getId());
    }

    private void removeActivity(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> activityIds = queryBatchList(conn,
                SQL_QUERY_ACTIVITY, companyId);
        if (activityIds.size() > 0)
        {
            removeVendorRole(conn, activityIds);
            removeRate(conn, activityIds);
        }
        logStart("ACTIVITY");
        execOnce(conn, SQL_DELETE_ACTIVITY, companyId);
        logEnd("ACTIVITY");
    }

    private void removeAddingSourcePage(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
        logStart("ADDING_SOURCE_PAGE");
        exec(conn, SQL_DELETE_ADDING_SOURCE_PAGE, jobIds);
        logEnd("ADDING_SOURCE_PAGE");
    }

    private void removeAmountOfWork(Connection conn, List<List<Object>> taskIds)
            throws SQLException
    {
        logStart("AMOUNT_OF_WORK");
        exec(conn, SQL_DELETE_AMOUNT_OF_WORK, taskIds);
        logEnd("AMOUNT_OF_WORK");
    }

    private void removeAttribute(Connection conn) throws SQLException
    {
        removeAttributes(conn);
        removeAttributeClone(conn);
        removeAttributeSet(conn);
    }

    private void removeAttributes(Connection conn) throws SQLException
    {
        logStart("ATTRIBUTE");
        execOnce(conn, SQL_DELETE_ATTRIBUTE, company.getId());
        logEnd("ATTRIBUTE");
    }

    private void removeAttributeClone(Connection conn) throws SQLException
    {
        logStart("ATTRIBUTE_CLONE");
        execOnce(conn, SQL_DELETE_ATTRIBUTE_CLONE, company.getId());
        logEnd("ATTRIBUTE_CLONE");
    }

    private void removeAttributeSet(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> setIds = queryBatchList(conn,
                SQL_QUERY_ATTRIBUTE_SET, companyId);
        if (setIds.size() > 0)
        {
            removeAttributeSetAttribute(conn, setIds);
        }
        logStart("ATTRIBUTE_SET");
        execOnce(conn, SQL_DELETE_ATTRIBUTE_SET, companyId);
        logEnd("ATTRIBUTE_SET");
    }

    private void removeAttributeSetAttribute(Connection conn,
            List<List<Object>> setIds) throws SQLException
    {
        logStart("ATTRIBUTE_SET_ATTRIBUTE");
        exec(conn, SQL_DELETE_ATTRIBUTE_SET_ATTRIBUTE, setIds);
        logEnd("ATTRIBUTE_SET_ATTRIBUTE");
    }

    private void removeBaseFilter(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> baseFilterIds = queryBatchList(conn,
                SQL_QUERY_BASE_FILTER, companyId);
        if (baseFilterIds.size() > 0)
        {
            removeBaseFilterMapping(conn, baseFilterIds);
        }
        logStart("BASE_FILTER");
        execOnce(conn, SQL_DELETE_BASE_FILTER, companyId);
        logEnd("BASE_FILTER");
    }

    private void removeBaseFilterMapping(Connection conn,
            List<List<Object>> baseFilterIds) throws SQLException
    {
        logStart("BASE_FILTER_MAPPING");
        exec(conn, SQL_DELETE_BASE_FILTER_MAPPING, baseFilterIds);
        logEnd("BASE_FILTER_MAPPING");
    }

    private void removeCalendar(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> calendarIds = queryBatchList(conn,
                SQL_QUERY_CALENDAR, companyId);
        if (calendarIds.size() > 0)
        {
            removeCalendarHoliday(conn, calendarIds);
            removeCalendarWorkingDay(conn, calendarIds);
            removeUserCalendar(conn, calendarIds);
        }
        logStart("CALENDAR");
        execOnce(conn, SQL_DELETE_CALENDAR, companyId);
        logEnd("CALENDAR");
    }

    private void removeCalendarHoliday(Connection conn,
            List<List<Object>> calendarIds) throws SQLException
    {
        logStart("CALENDAR_HOLIDAY");
        exec(conn, SQL_DELETE_CALENDAR_HOLIDAY, calendarIds);
        logEnd("CALENDAR_HOLIDAY");
    }

    private void removeCalendarWorkingDay(Connection conn,
            List<List<Object>> calendarIds) throws SQLException
    {
        List<List<Object>> calendarWorkingDayIds = queryBatchList(conn,
                SQL_QUERY_CALENDAR_WORKING_DAY, calendarIds);
        if (calendarWorkingDayIds.size() > 0)
        {
            removeCalendarWorkingHour(conn, calendarWorkingDayIds);
        }
        logStart("CALENDAR_WORKING_DAY");
        exec(conn, SQL_DELETE_CALENDAR_WORKING_DAY, calendarIds);
        logEnd("CALENDAR_WORKING_DAY");
    }

    private void removeCalendarWorkingHour(Connection conn,
            List<List<Object>> calendarWorkingDayIds) throws SQLException
    {
        logStart("CALENDAR_WORKING_HOUR");
        exec(conn, SQL_DELETE_CALENDAR_WORKING_HOUR, calendarWorkingDayIds);
        logEnd("CALENDAR_WORKING_HOUR");
    }

    private void removeCategory(Connection conn) throws SQLException
    {
        logStart("CATEGORY_SEGMENT_COMMENT");
        execOnce(conn, SQL_DELETE_CATEGORY, company.getId());
        logEnd("CATEGORY_SEGMENT_COMMENT");
    }

    private void removeComment(Connection conn, List<List<Object>> userIds,
            List<List<Object>> userNames) throws SQLException
    {
        removeComments(conn, userNames);
        removeIssue(conn, userIds);
    }

    private void removeComments(Connection conn, List<List<Object>> userIds)
            throws SQLException
    {
        logStart("COMMENTS");
        exec(conn, SQL_DELETE_COMMENTS, userIds);
        logEnd("COMMENTS");
    }

    private void removeCompany(Connection conn) throws SQLException
    {
        backupCompany(conn);
        logStart("COMPANY");
        execOnce(conn, SQL_DELETE_COMPANY, company.getId());
        logEnd("COMPANY");
    }

    private void removeCorpusMap(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        logStart("CORPUS_MAP");
        exec(conn, SQL_DELETE_CORPUS_MAP, projectTmIds);
        logEnd("CORPUS_MAP");
    }

    private void removeCostByWordCount(Connection conn,
            List<List<Object>> costIds) throws SQLException
    {
        logStart("COST_BY_WORD_COUNT");
        exec(conn, SQL_DELETE_COST_BY_WORD_COUNT, costIds);
        logEnd("COST_BY_WORD_COUNT");
    }

    private void removeCost(Connection conn,
            List<List<Object>> currencyConversionIds) throws SQLException
    {
        List<List<Object>> costIds = queryBatchList(conn, SQL_QUERY_COST,
                currencyConversionIds);
        
        if (costIds.size() > 0) {
            removeCostByWordCount(conn, costIds);
            removeSurcharge(conn, costIds);
        }

        logStart("COST");
        exec(conn, SQL_DELETE_COST, currencyConversionIds);
        logEnd("COST");
    }

    private void removeCurrencyConversion(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> currencyConversionIds = queryBatchList(conn,
                SQL_QUERY_CURRENCY_CONVERSION, companyId);
        if (currencyConversionIds.size() > 0)
        {
            removeCost(conn, currencyConversionIds);
            // set CURRENCY_CONV_ID to in SURCHARGE table null first
            exec(conn, SQL_UPDATE_SURCHARGE, currencyConversionIds);
        }
        logStart("CURRENCY_CONVERSION");
        execOnce(conn, SQL_DELETE_CURRENCY_CONVERSION, companyId);
        logEnd("CURRENCY_CONVERSION");
    }

    private void removeCustomerColumnDetail(Connection conn,
            List<List<Object>> xmlRuleIds) throws SQLException
    {
        logStart("CUSTOMER_COLUMN_DETAIL");
        exec(conn, SQL_DELETE_CUSTOMER_COLUMN_DETAIL, xmlRuleIds);
        logEnd("CUSTOMER_COLUMN_DETAIL");
    }

    private void removeCustomerDbAccessProfile(Connection conn,
            List<List<Object>> l10nProfileIds) throws SQLException
    {
        logStart("CUSTOMER_DB_ACCESS_PROFILE");
        exec(conn, SQL_DELETE_CUSTOMER_DB_ACCESS_PROFILE, l10nProfileIds);
        logEnd("CUSTOMER_DB_ACCESS_PROFILE");
    }

    private void removeCvsFileProfile(Connection conn) throws SQLException
    {
        logStart("CVS_FILE_PROFILE");
        execOnce(conn, SQL_DELETE_CVS_FILE_PROFILE, company.getId());
        logEnd("CVS_FILE_PROFILE");
    }

    private void removeCvsModuleMapping(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> moduleMappingIds = queryBatchList(conn,
                SQL_QUERY_MODULE_MAPPING, companyId);
        if (moduleMappingIds.size() > 0)
        {
            removeCvsModuleRename(conn, moduleMappingIds);
        }
        logStart("MODULE_MAPPING");
        execOnce(conn, SQL_DELETE_MODULE_MAPPING, companyId);
        logEnd("MODULE_MAPPING");
    }

    private void removeCvsModuleRename(Connection conn,
            List<List<Object>> moduleMappingIds) throws SQLException
    {
        logStart("MODULE_RENAME");
        exec(conn, SQL_DELETE_MODULE_RENAME, moduleMappingIds);
        logEnd("MODULE_RENAME");
    }

    private void removeCvsModule(Connection conn,
            List<List<Object>> cvsServerIds) throws SQLException
    {
        List<List<Object>> cvsModuleIds = queryBatchList(conn,
                SQL_QUERY_CVS_MODULE, cvsServerIds);
        if (cvsModuleIds.size() > 0)
        {
            removeCvsSourceFiles(conn, cvsModuleIds);
        }
        logStart("CVS_MODULE");
        exec(conn, SQL_DELETE_CVS_MODULE, cvsServerIds);
        logEnd("CVS_MODULE");
    }

    private void removeCvsRepository(Connection conn,
            List<List<Object>> cvsServerIds) throws SQLException
    {
        logStart("CVS_REPOSITORY");
        exec(conn, SQL_DELETE_CVS_REPOSITORY, cvsServerIds);
        logEnd("CVS_REPOSITORY");
    }

    private void removeCvsServer(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> cvsServerIds = queryBatchList(conn,
                SQL_QUERY_CVS_SERVER, companyId);
        if (cvsServerIds.size() > 0)
        {
            removeCvsModule(conn, cvsServerIds);
            removeCvsRepository(conn, cvsServerIds);
            removeCvsServerUser(conn, cvsServerIds);
        }
        logStart("CVS_SERVER");
        execOnce(conn, SQL_DELETE_CVS_SERVER, companyId);
        logEnd("CVS_SERVER");
    }

    private void removeCvsServerUser(Connection conn,
            List<List<Object>> cvsServerIds) throws SQLException
    {
        logStart("CVS_SERVER_USER");
        exec(conn, SQL_DELETE_CVS_SERVER_USER, cvsServerIds);
        logEnd("CVS_SERVER_USER");
    }

    private void removeCvsSourceFiles(Connection conn,
            List<List<Object>> cvsModuleIds) throws SQLException
    {
        logStart("CVS_SOURCE_FILES");
        exec(conn, SQL_DELETE_CVS_SOURCE_FILES, cvsModuleIds);
        logEnd("CVS_SOURCE_FILES");
    }

    private void removeExportBatchEvent(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
        List<List<Object>> exportBatchIds = queryBatchList(conn,
                SQL_QUERY_EXPORT_BATCH_EVENT, jobIds);
        if (exportBatchIds.size() > 0)
        {
            removeExportingPage(conn, exportBatchIds);
        }
        logStart("EXPORT_BATCH_EVENT");
        exec(conn, SQL_DELETE_EXPORT_BATCH_EVENT, jobIds);
        logEnd("EXPORT_BATCH_EVENT");
    }

    private void removeExportBatchWorkflow(Connection conn,
            List<List<Object>> workflowIds) throws SQLException
    {
        logStart("EXPORTBATCH_WORKFLOW");
        exec(conn, SQL_DELETE_EXPORTBATCH_WORKFLOW, workflowIds);
        logEnd("EXPORTBATCH_WORKFLOW");
    }

    private void removeExportLocation(Connection conn) throws SQLException
    {
        logStart("EXPORT_LOCATION");
        execOnce(conn, SQL_DELETE_EXPORT_LOCATION, company.getId());
        logEnd("EXPORT_LOCATION");
    }

    private void removeExtension(Connection conn) throws SQLException
    {
        logStart("EXTENSION");
        execOnce(conn, SQL_DELETE_EXTENSION, company.getId());
        logEnd("EXTENSION");
    }

    private void removeExportingPage(Connection conn,
            List<List<Object>> exportBatchIds) throws SQLException
    {
        logStart("EXPORTING_PAGE");
        exec(conn, SQL_DELETE_EXPORTING_PAGE, exportBatchIds);
        logEnd("EXPORTING_PAGE");
    }

    private void removeFileProfile(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> fileProfileIds = queryBatchList(conn,
                SQL_QUERY_FILE_PROFILE, companyId);
        if (fileProfileIds.size() > 0)
        {
            removeFileProfileExtension(conn, fileProfileIds);
        }
        logStart("FILE_PROFILE");
        execOnce(conn, SQL_DELETE_FILE_PROFILE, companyId);
        logEnd("FILE_PROFILE");
    }

    private void removeFileProfileExtension(Connection conn,
            List<List<Object>> fileProfileIds) throws SQLException
    {
        logStart("FILE_PROFILE_EXTENSION");
        exec(conn, SQL_DELETE_FILE_PROFILE_EXTENSION, fileProfileIds);
        logEnd("FILE_PROFILE_EXTENSION");
    }

    private void removeFileValueItem(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("FILE_VALUE_ITEM");
        exec(conn, SQL_DELETE_FILE_VALUE_ITEM, jobAttributeIds);
        logEnd("FILE_VALUE_ITEM");
    }

    private void removeFilter(Connection conn) throws SQLException
    {
        removeBaseFilter(conn);
        removeFrameMakerFilter(conn);
        removeHtmlFilter(conn);
        removeInddFilter(conn);
        removeJavaPropertiesFilter(conn);
        removeJavaScriptFilter(conn);
        removeJspFilter(conn);
        removeMsOfficeDocFilter(conn);
        removeMsOfficeExcelFilter(conn);
        removeMsOfficePptFilter(conn);
        removeMsOffice2010Filter(conn);
        removeOpenOfficeFilter(conn);
        removePoFilter(conn);
        removeXmlFilter(conn);
    }

    private void removeFilterConfiguration(Connection conn) throws SQLException
    {
        logStart("FILTER_CONFIGURATION");
        execOnce(conn, SQL_DELETE_FILTER_CONFIGURATION, company.getId());
        logEnd("FILTER_CONFIGURATION");
    }

    private void removeFrameMakerFilter(Connection conn) throws SQLException
    {
        logStart("FRAME_MAKER_FILTER");
        execOnce(conn, SQL_DELETE_FRAME_MAKER_FILTER, company.getId());
        logEnd("FRAME_MAKER_FILTER");
    }

    private void removeGsEdition(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        logStart("GS_EDITION");
        execOnce(conn, SQL_DELETE_GS_EDITION, companyId);
        logEnd("GS_EDITION");
    }

    private void removeHoliday(Connection conn) throws SQLException
    {
        logStart("HOLIDAY");
        execOnce(conn, SQL_DELETE_HOLIDAY, company.getId());
        logEnd("HOLIDAY");
    }

    private void removeHtmlFilter(Connection conn) throws SQLException
    {
        logStart("HTML_FILTER");
        execOnce(conn, SQL_DELETE_HTML_FILTER, company.getId());
        logEnd("HTML_FILTER");
    }

    private void removeIssue(Connection conn, List<List<Object>> userIds)
            throws SQLException
    {
        List<List<Object>> issueIds = queryBatchList(conn, SQL_QUERY_ISSUE,
                userIds);
        if (issueIds.size() > 0)
        {
            removeIssueHistory(conn, issueIds);
        }
        logStart("ISSUE");
        exec(conn, SQL_DELETE_ISSUE, userIds);
        logEnd("ISSUE");
    }

    private void removeIssueHistory(Connection conn, List<List<Object>> issueIds)
            throws SQLException
    {
        logStart("ISSUE_HISTORY");
        exec(conn, SQL_DELETE_ISSUE_HISTORY, issueIds);
        logEnd("ISSUE_HISTORY");
    }

    private void removeImageReplaceFileMap(Connection conn,
            List<List<Object>> targetPageIds) throws SQLException
    {
        logStart("IMAGE_REPLACE_FILE_MAP");
        exec(conn, SQL_DELETE_IMAGE_REPLACE_FILE_MAP, targetPageIds);
        logEnd("IMAGE_REPLACE_FILE_MAP");
    }

    private void removeInddFilter(Connection conn) throws SQLException
    {
        logStart("INDD_FILTER");
        execOnce(conn, SQL_DELETE_INDD_FILTER, company.getId());
        logEnd("INDD_FILTER");
    }

    private void removeIpTmIndex(Connection conn, List<List<Object>> jobIds)
            throws SQLException
    {
        logStart("IP_TM_INDEX");
        exec(conn, SQL_DELETE_IP_TM_INDEX, jobIds);
        logEnd("IP_TM_INDEX");
    }

    private void removeIpTmSrcL(Connection conn, List<List<Object>> jobIds)
            throws SQLException
    {
        List<List<Object>> srcIds = queryBatchList(conn, SQL_QUERY_IP_TM_SRC_L,
                jobIds);
        if (srcIds.size() > 0)
        {
            removeIpTmTrgL(conn, srcIds);
        }
        logStart("IP_TM_SRC_L");
        exec(conn, SQL_DELETE_IP_TM_SRC_L, jobIds);
        logEnd("IP_TM_SRC_L");
    }

    private void removeIpTmTrgL(Connection conn, List<List<Object>> srcIds)
            throws SQLException
    {
        logStart("IP_TM_TRG_L");
        exec(conn, SQL_DELETE_IP_TM_TRG_L, srcIds);
        logEnd("IP_TM_TRG_L");
    }

    private void removeIpTmSrcT(Connection conn, List<List<Object>> jobIds)
            throws SQLException
    {
        List<List<Object>> srcIds = queryBatchList(conn, SQL_QUERY_IP_TM_SRC_T,
                jobIds);
        if (srcIds.size() > 0)
        {
            removeIpTmTrgT(conn, srcIds);
        }
        logStart("IP_TM_SRC_T");
        exec(conn, SQL_DELETE_IP_TM_SRC_T, jobIds);
        logEnd("IP_TM_SRC_T");
    }

    private void removeIpTmTrgT(Connection conn, List<List<Object>> srcIds)
            throws SQLException
    {
        logStart("IP_TM_TRG_T");
        exec(conn, SQL_DELETE_IP_TM_TRG_T, srcIds);
        logEnd("IP_TM_TRG_T");
    }

    private void removeJavaPropertiesFilter(Connection conn)
            throws SQLException
    {
        logStart("JAVA_PROPERTIES_FILTER");
        execOnce(conn, SQL_DELETE_JAVA_PROPERTIES_FILTER, company.getId());
        logEnd("JAVA_PROPERTIES_FILTER");
    }

    private void removeJavaScriptFilter(Connection conn) throws SQLException
    {
        logStart("JAVA_SCRIPT_FILTER");
        execOnce(conn, SQL_DELETE_JAVA_SCRIPT_FILTER, company.getId());
        logEnd("JAVA_SCRIPT_FILTER");
    }

    private void removeJbpmAction(Connection conn,
            List<List<Object>> delegationIds) throws SQLException
    {
        logStart("JBPM_ACTION");
        exec(conn, SQL_DELETE_JBPM_ACTION, delegationIds);
        logEnd("JBPM_ACTION");
    }

    private void removeJbpmDelegation(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        List<List<Object>> delegationIds = queryBatchList(conn,
                SQL_QUERY_JBPM_DELEGATION, processDefinitionIds);
        if (delegationIds.size() > 0)
        {
            removeJbpmAction(conn, delegationIds);
            removeJbpmTaskController(conn, delegationIds);
            removeJbpmDelegationNode(conn, delegationIds, processDefinitionIds);
        }
        logStart("JBPM_DELEGATION");
        exec(conn, SQL_DELETE_JBPM_DELEGATION, processDefinitionIds);
        logEnd("JBPM_DELEGATION");
    }

    private void removeJbpmDelegationNode(Connection conn,
            List<List<Object>> delegationIds,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        List<List<Object>> nodeIds = queryBatchList(conn,
                SQL_QUERY_JBPM_DELEGATION_NODE, delegationIds);
        if (nodeIds.size() > 0)
        {
            removeJbpmVariableAccess(conn, nodeIds);
            // set JBPM_NODE records to null in JBPM_PROCESSDEFINITION table
            // first
            exec(conn, SQL_UPDATE_JBPM_PROCESSDEFINITION, nodeIds);
            // set JBPM_NODE records to null in JBPM_TRANSITION table first
            exec(conn, SQL_UPDATE_JBPM_TRANSITION_FROM_, nodeIds);
            exec(conn, SQL_UPDATE_JBPM_TRANSITION_TO_, nodeIds);
        }
        logStart("JBPM_NODE");
        exec(conn, SQL_DELETE_JBPM_DELEGATION_NODE, delegationIds);
        logEnd("JBPM_NODE");
    }

    private void removeJbpmGsVariable(Connection conn,
            List<List<Object>> taskInstanceIds) throws SQLException
    {
        logStart("JBPM_GS_VARIABLE");
        exec(conn, SQL_DELETE_JBPM_GS_VARIABLE, taskInstanceIds);
        logEnd("JBPM_GS_VARIABLE");
    }

    private void removeJbpmModuleDefinition(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        logStart("JBPM_MODULEDEFINITION");
        exec(conn, SQL_DELETE_JBPM_MODULEDEFINITION, processDefinitionIds);
        logEnd("JBPM_MODULEDEFINITION");
    }

    private void removeJbpmModuleInstance(Connection conn,
            List<List<Object>> processInstanceIds) throws SQLException
    {
        List<List<Object>> moduleInstanceIds = queryBatchList(conn,
                SQL_QUERY_JBPM_MODULEINSTANCE, processInstanceIds);
        if (moduleInstanceIds.size() > 0)
        {
            List<List<Object>> tokenVariableMapIds = queryBatchList(conn,
                    SQL_QUERY_JBPM_TOKENVARIABLEMAP, moduleInstanceIds);
            removeJbpmTaskInstance(conn, moduleInstanceIds, tokenVariableMapIds);
            removeJbpmTokenVariableMap(conn, moduleInstanceIds);
        }
        logStart("JBPM_MODULEINSTANCE");
        exec(conn, SQL_DELETE_JBPM_MODULEINSTANCE, processInstanceIds);
        logEnd("JBPM_MODULEINSTANCE");
    }

    private void removeJbpmNode(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        List<List<Object>> nodeIds = queryBatchList(conn, SQL_QUERY_JBPM_NODE,
                processDefinitionIds);
        if (nodeIds.size() > 0)
        {
            removeJbpmVariableAccess(conn, nodeIds);
            // set JBPM_NODE records to null in JBPM_PROCESSDEFINITION table
            // first
            exec(conn, SQL_UPDATE_JBPM_PROCESSDEFINITION, nodeIds);
            // set JBPM_NODE records to null in JBPM_TRANSITION table first
            exec(conn, SQL_UPDATE_JBPM_TRANSITION_FROM_, nodeIds);
            exec(conn, SQL_UPDATE_JBPM_TRANSITION_TO_, nodeIds);
            // set JBPM_NODE records to null in JBPM_TASK table first
            exec(conn, SQL_UPDATE_JBPM_TASK_TASKNODE_BY_TASKNODE_, nodeIds);
        }
        logStart("JBPM_NODE");
        exec(conn, SQL_DELETE_JBPM_NODE, processDefinitionIds);
        logEnd("JBPM_NODE");
    }

    private void removeJbpmPooledActor(Connection conn,
            List<List<Object>> pooledActorIds) throws SQLException
    {
        logStart("JBPM_POOLEDACTOR");
        exec(conn, SQL_DELETE_JBPM_POOLEDACTOR, pooledActorIds);
        logEnd("JBPM_POOLEDACTOR");
    }

    private void removeJbpmProcessDefinition(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        logStart("JBPM_PROCESSDEFINITION");
        exec(conn, SQL_DELETE_JBPM_PROCESSDEFINITION, processDefinitionIds);
        logEnd("JBPM_PROCESSDEFINITION");
    }

    private void removeJbpmProcessDefinitionData(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        removeJbpmTransition(conn, processDefinitionIds);
        removeJbpmTask(conn, processDefinitionIds);
        removeJbpmModuleDefinition(conn, processDefinitionIds);
        removeJbpmNode(conn, processDefinitionIds);
        removeJbpmDelegation(conn, processDefinitionIds);
        removeJbpmProcessDefinition(conn, processDefinitionIds);
    }

    private void removeJbpmProcessInstance(Connection conn,
            List<List<Object>> processInstanceIds) throws SQLException
    {
        List<List<Object>> processDefinitionIds = queryBatchList(conn,
                SQL_QUERY_JBPM_PROCESSINSTANCE, processInstanceIds);
        logStart("JBPM_PROCESSINSTANCE");
        exec(conn, SQL_DELETE_JBPM_PROCESSINSTANCE, processInstanceIds);
        logEnd("JBPM_PROCESSINSTANCE");
        if (processDefinitionIds.size() > 0)
        {
            removeJbpmProcessDefinitionData(conn, processDefinitionIds);
        }
    }

    private void removeJbpmTask(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        // set TASKNODE_ records referenced to JBPM_NODE table to null first
        exec(conn, SQL_UPDATE_JBPM_TASK_TASKNODE_BY_PROCESSDEFINITION_,
                processDefinitionIds);
        logStart("JBPM_TASK");
        exec(conn, SQL_DELETE_JBPM_TASK, processDefinitionIds);
        logEnd("JBPM_TASK");
    }

    private void removeJbpmTaskActorPool(Connection conn,
            List<List<Object>> taskInstanceIds) throws SQLException
    {
        List<List<Object>> pooledActorIds = queryBatchList(conn,
                SQL_QUERY_JBPM_TASKACTORPOOL, taskInstanceIds);
        logStart("JBPM_TASKACTORPOOL");
        exec(conn, SQL_DELETE_JBPM_TASKACTORPOOL, taskInstanceIds);
        logEnd("JBPM_TASKACTORPOOL");
        if (pooledActorIds.size() > 0)
        {
            removeJbpmPooledActor(conn, pooledActorIds);
        }
    }

    private void removeJbpmTaskController(Connection conn,
            List<List<Object>> delegationIds) throws SQLException
    {
        logStart("JBPM_TASKCONTROLLER");
        exec(conn, SQL_DELETE_JBPM_TASKCONTROLLER, delegationIds);
        logEnd("JBPM_TASKCONTROLLER");
    }

    private void removeJbpmTaskInstance(Connection conn,
            List<List<Object>> moduleInstanceIds,
            List<List<Object>> tokenVariableMapIds) throws SQLException
    {
        List<List<Object>> taskInstanceIds = queryBatchList(conn,
                SQL_QUERY_JBPM_TASKINSTANCE, moduleInstanceIds);
        removeJbpmVariableInstance(conn, taskInstanceIds, tokenVariableMapIds);
        if (taskInstanceIds.size() > 0)
        {
            removeJbpmTaskActorPool(conn, taskInstanceIds);
            removeJbpmGsVariable(conn, taskInstanceIds);
        }
        logStart("JBPM_TASKINSTANCE");
        exec(conn, SQL_DELETE_JBPM_TASKINSTANCE, moduleInstanceIds);
        logEnd("JBPM_TASKINSTANCE");
    }

    private void removeJbpmToken(Connection conn,
            List<List<Object>> processInstanceIds) throws SQLException
    {
        // set JBPM_TOKEN records to null in JBPM_PROCESSINSTANCE table first
        exec(conn, SQL_UPDATE_JBPM_PROCESSINSTANCE_ROOTTOKEN_,
                processInstanceIds);
        exec(conn, SQL_UPDATE_JBPM_PROCESSINSTANCE_SUPERPROCESSTOKEN_,
                processInstanceIds);
        logStart("JBPM_TOKEN");
        exec(conn, SQL_DELETE_JBPM_TOKEN, processInstanceIds);
        logEnd("JBPM_TOKEN");
    }

    private void removeJbpmTokenVariableMap(Connection conn,
            List<List<Object>> moduleInstanceIds) throws SQLException
    {
        logStart("JBPM_TOKENVARIABLEMAP");
        exec(conn, SQL_DELETE_JBPM_TOKENVARIABLEMAP, moduleInstanceIds);
        logEnd("JBPM_TOKENVARIABLEMAP");
    }

    private void removeJbpmTransition(Connection conn,
            List<List<Object>> processDefinitionIds) throws SQLException
    {
        logStart("JBPM_TRANSITION");
        exec(conn, SQL_DELETE_JBPM_TRANSITION, processDefinitionIds);
        logEnd("JBPM_TRANSITION");
    }

    private void removeJbpmVariableAccess(Connection conn,
            List<List<Object>> nodeIds) throws SQLException
    {
        logStart("JBPM_VARIABLEACCESS");
        exec(conn, SQL_DELETE_JBPM_VARIABLEACCESS, nodeIds);
        logEnd("JBPM_VARIABLEACCESS");
    }

    private void removeJbpmVariableInstance(Connection conn,
            List<List<Object>> taskInstanceIds,
            List<List<Object>> tokenVariableMapIds) throws SQLException
    {
        logStart("JBPM_VARIABLEINSTANCE");
        if (taskInstanceIds.size() > 0)
        {
            exec(conn, SQL_DELETE_JBPM_VARIABLEINSTANCE_BY_TASKINSTANCE,
                    taskInstanceIds);
        }

        if (tokenVariableMapIds.size() > 0)
        {
            exec(conn, SQL_DELETE_JBPM_VARIABLEINSTANCE_BY_TOKENVARIABLEMAP,
                    tokenVariableMapIds);
        }
        logEnd("JBPM_VARIABLEINSTANCE");
    }

    private void removeJob(Connection conn, List<List<Object>> jobIds) throws SQLException
    {
        logStart("JOB");
        long companyId = company.getId();
        if (!isJobRemoval)
            jobIds = queryBatchList(conn, SQL_QUERY_JOB, companyId);

        if (jobIds.size() > 0)
        {
            removeWorkflowRequest(conn, jobIds);
            removeIpTmIndex(conn, jobIds);
            removeIpTmSrcL(conn, jobIds);
            removeIpTmSrcT(conn, jobIds);
            removeJobAttribute(conn, jobIds);
            removeExportBatchEvent(conn, jobIds);
            removeAddingSourcePage(conn, jobIds);
            removeUpdatedSourcePage(conn, jobIds);
            // set JOB records to null in REQUEST table first
            // exec(conn, SQL_UPDATE_REQUEST_JOB_ID, jobIds);
        }

        if (!isJobRemoval)
        {
            // backup JOB data
            backupJob(conn);
            execOnce(conn, SQL_DELETE_JOB, companyId);
        }
        else
            exec(conn, SQL_JOB_DELETE_JOB, jobIds);
        logEnd("JOB");
    }
    
    private void removeScoreByCompanyId(Connection conn) 
    		throws SQLException
    {
    	logStart("SCORE");
    	long companyId = company.getId();
        List<List<Object>> jobIds = queryBatchList(conn, SQL_QUERY_JOB,
                companyId);
        if (jobIds.size() > 0)
        {
        	removeScoreByJobId(conn, jobIds);
        }
    	logEnd("SCORE");
    }

    private void removeJobAttribute(Connection conn, List<List<Object>> jobIds)
            throws SQLException
    {
        List<List<Object>> jobAttributeIds = queryBatchList(conn,
                SQL_QUERY_JOB_ATTRIBUTE, jobIds);
        if (jobAttributeIds.size() > 0)
        {
            removeJobAttributeSelectOption(conn, jobAttributeIds);
            removeFileValueItem(conn, jobAttributeIds);
            
            removeJobAttributeAndCondition(conn, jobIds);
        }
        logStart("JOB_ATTRIBUTE");
        exec(conn, SQL_DELETE_JOB_ATTRIBUTE, jobIds);
        logEnd("JOB_ATTRIBUTE");
    }

    private void removeJobAttributeAndCondition(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
        List<List<Object>> jobAttributeIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES, jobIds);
        if (jobAttributeIds.size() > 0)
        {
            removeDateConditions(conn, jobAttributeIds);
            removeFloatConditions(conn, jobAttributeIds);
            removeIntConditions(conn, jobAttributeIds);
            removeTextConditions(conn, jobAttributeIds);
            removeListConditions(conn, jobAttributeIds);
            removeFileConditions(conn, jobAttributeIds);

            removeJobAttributes(conn, jobAttributeIds);
        }
    }

    private void removeDateConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_DATE");
        List<List<Object>> dateConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_DATE, jobAttributeIds);
        if (dateConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_DATE, dateConditionIds);
        }
        logEnd("ATTRIBUTE_CONDITION_DATE");
    }

    private void removeFloatConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_FLOAT");
        List<List<Object>> floatConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_FLOAT, jobAttributeIds);
        if (floatConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_FLOAT, floatConditionIds);
        }
        logEnd("ATTRIBUTE_CONDITION_FLOAT");
    }

    private void removeIntConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_INT");
        List<List<Object>> intConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_INT, jobAttributeIds);
        if (intConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_INT, intConditionIds);
        }
        logEnd("ATTRIBUTE_CONDITION_INT");
    }

    private void removeTextConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_TEXT");
        List<List<Object>> textConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_TEXT, jobAttributeIds);
        if (textConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_TEXT, textConditionIds);
        }
        logEnd("ATTRIBUTE_CONDITION_TEXT");
    }

    private void removeListConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_LIST");
        List<List<Object>> listConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_LIST, jobAttributeIds);
        if (listConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_LIST_SELECT, listConditionIds);
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_LIST, listConditionIds);
        }
        logEnd("ATTRIBUTE_CONDITION_LIST");
    }

    @SuppressWarnings("unchecked")
    private void removeFileConditions(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CONDITION_FILE");
        List<List<Object>> fileConditionIds = queryBatchList(conn,
                SQL_JOB_QUERY_ATTRIBUTES_FILE, jobAttributeIds);
        if (fileConditionIds.size() > 0)
        {
            exec(conn, SQL_JOB_DELETE_ATTRIBUTES_FILE, fileConditionIds);

            // Delete files in $FileStorage$/<company>/GlobalSight/JobAttribute
            fileConditionIds = queryBatchList(conn,
                    SQL_JOB_QUERY_ATTRIBUTES_FILE_ID, jobAttributeIds);
            if (fileConditionIds.size() > 0)
            {
                File jobAttributeDir = AmbFileStoragePathUtils
                        .getJobAttributeDir();
                File file = null;
                for (Object obj : fileConditionIds)
                {
                    List<Object> tmp = (List<Object>) obj;
                    if (tmp != null && tmp.size() > 0)
                    {
                        for (Object o : tmp)
                        {
                            Long fileId = (Long) o;
                            file = new File(jobAttributeDir, fileId.toString());
                            if (file.exists() && file.isDirectory())
                                FileUtil.deleteFile(file);
                        }
                    }
                }
            }
        }
        logEnd("ATTRIBUTE_CONDITION_FILE");
    }

    private void removeJobAttributes(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("ATTRIBUTE_CLONE");
        exec(conn, SQL_JOB_DELETE_ATTRIBUTE, jobAttributeIds);
        logEnd("ATTRIBUTE_CLONE");
    }

    private void removeJobAttributeSelectOption(Connection conn,
            List<List<Object>> jobAttributeIds) throws SQLException
    {
        logStart("JOB_ATTRIBUTE_SELECT_OPTION");
        exec(conn, SQL_DELETE_JOB_ATTRIBUTE_SELECT_OPTION, jobAttributeIds);
        logEnd("JOB_ATTRIBUTE_SELECT_OPTION");
    }

    private void removeJspFilter(Connection conn) throws SQLException
    {
        logStart("JSP_FILTER");
        execOnce(conn, SQL_DELETE_JSP_FILTER, company.getId());
        logEnd("JSP_FILTER");
    }

    private void removeL10nProfile(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> l10nProfileIds = queryBatchList(conn,
                SQL_QUERY_L10N_PROFILE, companyId);
        if (l10nProfileIds.size() > 0)
        {
            removeL10nProfileVersion(conn, l10nProfileIds);
            removeL10nProfileTmProfile(conn, l10nProfileIds);
            removeL10nProfileWfTemplateInfo(conn, l10nProfileIds);
            removeCustomerDbAccessProfile(conn, l10nProfileIds);
        }
        logStart("L10N_PROFILE");
        execOnce(conn, SQL_DELETE_L10N_PROFILE, companyId);
        logEnd("L10N_PROFILE");
    }

    private void removeMtProfile(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> mtProfileIds = queryBatchList(conn,
                SQL_QUERY_MT_PROFILE, companyId);
        if (mtProfileIds.size() > 0)
        {
            removeMTProfileExtentInfo(conn, mtProfileIds);
        }
        logStart("MT_PROFILE");
        execOnce(conn, SQL_DELETE_MT_PROFILE, companyId);
        logEnd("MT_PROFILE");
    }

    private void removeL10nProfileTmProfile(Connection conn,
            List<List<Object>> l10nProfileIds) throws SQLException
    {
        logStart("L10N_PROFILE_TM_PROFILE");
        exec(conn, SQL_DELETE_L10N_PROFILE_TM_PROFILE, l10nProfileIds);
        logEnd("L10N_PROFILE_TM_PROFILE");
    }

    private void removeL10nProfileVersion(Connection conn,
            List<List<Object>> l10nProfileIds) throws SQLException
    {
        logStart("L10N_PROFILE_VERSION");
        exec(conn, SQL_DELETE_L10N_PROFILE_VERSION, l10nProfileIds);
        logEnd("L10N_PROFILE_VERSION");
    }

    private void removeMTProfileExtentInfo(Connection conn,
            List<List<Object>> mtProfileIds) throws SQLException
    {
        logStart("MT_PROFILE_EXTENTINFO");
        exec(conn, SQL_DELETE_MT_PROFILE_EXTENTINFO, mtProfileIds);
        logEnd("MT_PROFILE_EXTENTINFO");
    }

    private void removeL10nProfileWfTemplateInfo(Connection conn,
            List<List<Object>> l10nProfileIds) throws SQLException
    {
        logStart("L10N_PROFILE_WFTEMPLATE_INFO");
        exec(conn, SQL_DELETE_L10N_PROFILE_WFTEMPLATE_INFO, l10nProfileIds);
        logEnd("L10N_PROFILE_WFTEMPLATE_INFO");
    }

    private void removeLeverageLocales(Connection conn,
            List<List<Object>> workflowTemplateIds) throws SQLException
    {
        logStart("LEVERAGE_LOCALES");
        exec(conn, SQL_DELETE_LEVERAGE_LOCALES, workflowTemplateIds);
        logEnd("LEVERAGE_LOCALES");
    }

    // For remove company ONLY.
    private void removeLeverageMatch(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        // Delete from "leverage_match" and "leverage_match_archived" tables.
        if (DbUtil.isTableExisted(conn, "LEVERAGE_MATCH"))
        {
            String sqlDelFromLM = "delete lm from source_page sp, leverage_match lm where lm.source_page_id = sp.id and sp.company_id = ?";
            logStart("LEVERAGE_MATCH");
            execOnce(conn, sqlDelFromLM, companyId);
            logEnd("LEVERAGE_MATCH");
        }

        if (DbUtil.isTableExisted(conn, "LEVERAGE_MATCH_ARCHIVED"))
        {
            String sqlDelFromLM = "delete lm from source_page sp, leverage_match_archived lm where lm.source_page_id = sp.id and sp.company_id = ?";
            logStart("LEVERAGE_MATCH_ARCHIVED");
            execOnce(conn, sqlDelFromLM, companyId);
            logEnd("LEVERAGE_MATCH_ARCHIVED");
        }

        // Drop all leverage match extension tables for current company.
        List<Long> jobIds = getJobIdsByCompanyId(companyId);
        List<String> lmExtTables = new ArrayList<String>();
        lmExtTables.add("LEVERAGE_MATCH_EXT_" + companyId);
        lmExtTables.add("LEVERAGE_MATCH_EXT_" + companyId + "_ARCHIVED");
        for (long jobId : jobIds)
        {
        	lmExtTables.add("LEVERAGE_MATCH_EXT_" + companyId + "_" + jobId);
        }
        for (String tableName : lmExtTables)
        {
            if (DbUtil.isTableExisted(conn, tableName))
            {
                logStart(tableName);
                execOnce(conn, SQL_DROP + tableName);
                logEnd(tableName);
            }
        }

        // Drop all leverage match tables for current company.
        List<String> lmTables = new ArrayList<String>();
        lmTables.add("LEVERAGE_MATCH_" + companyId);
        lmTables.add("LEVERAGE_MATCH_" + companyId + "_ARCHIVED");
        for (long jobId : jobIds)
        {
            lmTables.add("LEVERAGE_MATCH_" + companyId + "_" + jobId);
        }
        for (String tableName : lmTables)
        {
            if (DbUtil.isTableExisted(conn, tableName))
            {
                logStart(tableName);
                execOnce(conn, SQL_DROP + tableName);
                logEnd(tableName);
            }
        }
    }

    private void removeLeverageMatch(Connection conn,
            List<List<Object>> sourcePageIds, long companyId)
            throws SQLException
    {
        List<String> tables = new ArrayList<String>();
        tables.add("LEVERAGE_MATCH");
        tables.add("LEVERAGE_MATCH_ARCHIVED");
        tables.add("LEVERAGE_MATCH_" + companyId);
        tables.add("LEVERAGE_MATCH_" + companyId + "_ARCHIVED");
        tables.add("LEVERAGE_MATCH_EXT_" + companyId);
        tables.add("LEVERAGE_MATCH_EXT_" + companyId + "_ARCHIVED");

        for (String tableName : tables)
        {
            if (DbUtil.isTableExisted(conn, tableName))
            {
                logStart(tableName);
                exec(conn, "delete from " + tableName
                        + " where SOURCE_PAGE_ID in ", sourcePageIds);
                logEnd(tableName);
            }
        }
    }

    private void removeLocalePair(Connection conn) throws SQLException
    {
        logStart("LOCALE_PAIR");
        execOnce(conn, SQL_DELETE_LOCALE_PAIR, company.getId());
        logEnd("LOCALE_PAIR");
    }

    private void removeMsOfficeDocFilter(Connection conn) throws SQLException
    {
        logStart("MS_OFFICE_DOC_FILTER");
        execOnce(conn, SQL_DELETE_MS_OFFICE_DOC_FILTER, company.getId());
        logEnd("MS_OFFICE_DOC_FILTER");
    }

    private void removeMsOfficeExcelFilter(Connection conn) throws SQLException
    {
        logStart("MS_OFFICE_EXCEL_FILTER");
        execOnce(conn, SQL_DELETE_MS_OFFICE_EXCEL_FILTER, company.getId());
        logEnd("MS_OFFICE_EXCEL_FILTER");
    }

    private void removeMsOfficePptFilter(Connection conn) throws SQLException
    {
        logStart("MS_OFFICE_PPT_FILTER");
        execOnce(conn, SQL_DELETE_MS_OFFICE_PPT_FILTER, company.getId());
        logEnd("MS_OFFICE_PPT_FILTER");
    }

    private void removeMsOffice2010Filter(Connection conn) throws SQLException
    {
        logStart("OFFICE2010_FILTER");
        execOnce(conn, SQL_DELETE_OFFICE2010_FILTER, company.getId());
        logEnd("OFFICE2010_FILTER");
    }

    private void removeOpenOfficeFilter(Connection conn) throws SQLException
    {
        logStart("OPENOFFICE_FILTER");
        execOnce(conn, SQL_DELETE_OPENOFFICE_FILTER, company.getId());
        logEnd("OPENOFFICE_FILTER");
    }

    private void removePermissionGroup(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> permissionGroupIds = queryBatchList(conn,
                SQL_QUERY_PERMISSIONGROUP, companyId);
        if (permissionGroupIds.size() > 0)
        {
            removePermissionGroupUser(conn, permissionGroupIds);
        }
        logStart("PERMISSIONGROUP");
        execOnce(conn, SQL_DELETE_PERMISSIONGROUP, companyId);
        logEnd("PERMISSIONGROUP");
    }

    private void removePermissionGroupUser(Connection conn,
            List<List<Object>> permissionGroupIds) throws SQLException
    {
        logStart("PERMISSIONGROUP_USER");
        exec(conn, SQL_DELETE_PERMISSIONGROUP_USER, permissionGroupIds);
        logEnd("PERMISSIONGROUP_USER");
    }

    private void removePoFilter(Connection conn) throws SQLException
    {
        logStart("PO_FILTER");
        execOnce(conn, SQL_DELETE_PO_FILTER, company.getId());
        logEnd("PO_FILTER");
    }

	private void removeJobGroup(Connection conn) throws SQLException
	{
		long companyId = company.getId();
	    logStart("JOB_GROUP");
        execOnce(conn, SQL_DELETE_JOB_GROUP, companyId);
        logEnd("JOB_GROUP");
	}
    
    private void removeProject(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> projectIds = queryBatchList(conn, SQL_QUERY_PROJECT,
                companyId);
        if (projectIds.size() > 0)
        {
            removeProjectUser(conn, projectIds);
            removeProjectVendor(conn, projectIds);
        }
        logStart("PROJECT");
        execOnce(conn, SQL_DELETE_PROJECT, companyId);
        logEnd("PROJECT");
    }

    private void removeProjectUser(Connection conn,
            List<List<Object>> projectIds) throws SQLException
    {
        logStart("PROJECT_USER");
        exec(conn, SQL_DELETE_PROJECT_USER, projectIds);
        logEnd("PROJECT_USER");
    }

    private void removeProjectVendor(Connection conn,
            List<List<Object>> projectIds) throws SQLException
    {
        logStart("PROJECT_VENDOR");
        exec(conn, SQL_DELETE_PROJECT_VENDOR, projectIds);
        logEnd("PROJECT_VENDOR");
    }

    private void removeProjectTm(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> projectTmIds = queryBatchList(conn,
                SQL_QUERY_PROJECT_TM, companyId);
        if (projectTmIds.size() > 0)
        {
            removeProjectTmTuL(conn, projectTmIds);
            removeProjectTmTuT(conn, projectTmIds);
            removeTmAttribute(conn, projectTmIds);
            removeTmProfileProjectTmInfo(conn, projectTmIds);
            removeTmProfile(conn, projectTmIds);
            removeCorpusMap(conn, projectTmIds);
        }
        logStart("PROJECT_TM");
        execOnce(conn, SQL_DELETE_PROJECT_TM, companyId);
        logEnd("PROJECT_TM");
    }

    private void removeProjectTmTuL(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        List<List<Object>> projectTmTuIds = queryBatchList(conn,
                SQL_QUERY_PROJECT_TM_TU_L, projectTmIds);
        if (projectTmTuIds.size() > 0)
        {
            removeProjectTmTuvL(conn, projectTmTuIds);
        }
        logStart("PROJECT_TM_TU_L");
        exec(conn, SQL_DELETE_PROJECT_TM_TU_L, projectTmIds);
        logEnd("PROJECT_TM_TU_L");
    }

    private void removeProjectTmTuvL(Connection conn,
            List<List<Object>> projectTmTuIds) throws SQLException
    {
        logStart("PROJECT_TM_TUV_L");
        exec(conn, SQL_DELETE_PROJECT_TM_TUV_L, projectTmTuIds);
        logEnd("PROJECT_TM_TUV_L");
    }

    private void removeProjectTmTuT(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        List<List<Object>> projectTmTuIds = queryBatchList(conn,
                SQL_QUERY_PROJECT_TM_TU_T, projectTmIds);
        if (projectTmTuIds.size() > 0)
        {
            removeProjectTmTuvT(conn, projectTmTuIds);
            removeProjectTmTuTProp(conn, projectTmTuIds);
        }
        logStart("PROJECT_TM_TU_T");
        exec(conn, SQL_DELETE_PROJECT_TM_TU_T, projectTmIds);
        logEnd("PROJECT_TM_TU_T");
    }

    private void removeProjectTmTuvT(Connection conn,
            List<List<Object>> projectTmTuIds) throws SQLException
    {
        logStart("PROJECT_TM_TUV_T");
        exec(conn, SQL_DELETE_PROJECT_TM_TUV_T, projectTmTuIds);
        logEnd("PROJECT_TM_TUV_T");
    }

    private void removeProjectTmTuTProp(Connection conn,
            List<List<Object>> projectTmTuIds) throws SQLException
    {
        logStart("PROJECT_TM_TU_T_PROP");
        exec(conn, SQL_DELETE_PROJECT_TM_TU_T_PROP, projectTmTuIds);
        logEnd("PROJECT_TM_TU_T_PROP");
    }

    private void removeRate(Connection conn, List<List<Object>> activityIds)
            throws SQLException
    {
        logStart("RATE");
        exec(conn, SQL_DELETE_RATE, activityIds);
        logEnd("RATE");
    }

    private void removeRemoteAccessHistory(Connection conn,
            List<List<Object>> userIds) throws SQLException
    {
        logStart("REMOTE_ACCESS_HISTORY");
        exec(conn, SQL_DELETE_REMOTE_ACCESS_HISTORY, userIds);
        logEnd("REMOTE_ACCESS_HISTORY");
    }

    private void removeRemovedPrefixTag(Connection conn,
            List<List<Object>> tuIds) throws SQLException
    {
        logStart("REMOVED_PREFIX_TAG");
        exec(conn, SQL_DELETE_REMOVED_PREFIX_TAG, tuIds);
        logEnd("REMOVED_PREFIX_TAG");
    }

    private void removeRemovedSuffixTag(Connection conn,
            List<List<Object>> tuIds) throws SQLException
    {
        logStart("REMOVED_SUFFIX_TAG");
        exec(conn, SQL_DELETE_REMOVED_SUFFIX_TAG, tuIds);
        logEnd("REMOVED_SUFFIX_TAG");
    }

    private void removeRemovedTag(Connection conn, List<List<Object>> tuIds)
            throws SQLException
    {
        logStart("REMOVED_TAG");
        exec(conn, SQL_DELETE_REMOVED_TAG, tuIds);
        logEnd("REMOVED_TAG");
    }

    private void removeRequest(Connection conn, List<List<Object>> jobIds) throws Exception
    {
        String table = "REQUEST";
        logStart(table);
        long companyId = company.getId();
        if (isJobRemoval) {
            exec(conn, SQL_JOB_DELETE_REQUEST, jobIds);
        } else {
            backupRequest(conn);
            execOnce(conn, SQL_DELETE_REQUEST, companyId);
        }
        logEnd(table);
    }

    private void removeReservedTime(Connection conn,
            List<List<Object>> userCalendarIds, List<List<Object>> taskIds)
            throws SQLException
    {
        logStart("RESERVED_TIME");
        if (userCalendarIds != null)
        {
            exec(conn, SQL_DELETE_RESERVED_TIME_BY_CALENDAR_ID, userCalendarIds);
        }
        if (taskIds != null)
        {
            exec(conn, SQL_DELETE_RESERVED_TIME_BY_TASK_ID, taskIds);
        }
        logEnd("RESERVED_TIME");
    }

    private void removeRole() throws Exception
    {
        String companyName = company.getCompanyName();
        UserManager um = ServerProxy.getUserManager();
        List<Role> roles = um.getRolesFromCompany(String.valueOf(company
                .getId()));
        CATEGORY.info("Deleting company "
                + companyName
                + " records in table CONTAINER_ROLE, CONTAINER_ROLE_USER_IDS, CONTAINER_ROLE_RATE and USER_ROLE");
        for (Role r : roles)
        {
            um.removeRoleFromLDAP(r.getName());
        }
        CATEGORY.info("Done deleting company "
                + companyName
                + " records in table CONTAINER_ROLE, CONTAINER_ROLE_USER_IDS, CONTAINER_ROLE_RATE and USER_ROLE");
    }

    private void removeRssFeed(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> rssFeedIds = queryBatchList(conn,
                SQL_QUERY_RSS_FEED, companyId);
        if (rssFeedIds.size() > 0)
        {
            removeRssItem(conn, rssFeedIds);
        }
        logStart("RSS_FEED");
        execOnce(conn, SQL_DELETE_RSS_FEED, companyId);
        logEnd("RSS_FEED");
    }

    private void removeRssItem(Connection conn, List<List<Object>> rssFeedIds)
            throws SQLException
    {
        logStart("RSS_ITEM");
        exec(conn, SQL_DELETE_RSS_ITEM, rssFeedIds);
        logEnd("RSS_ITEM");
    }

    private void removeSecondaryTargetFile(Connection conn,
            List<List<Object>> workflowIds) throws SQLException
    {
        logStart("SECONDARY_TARGET_FILE");
        exec(conn, SQL_DELETE_SECONDARY_TARGET_FILE, workflowIds);
        logEnd("SECONDARY_TARGET_FILE");
    }

    private void removeSegmentationRule(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> segmentationRuleIds = queryBatchList(conn,
                SQL_QUERY_SEGMENTATION_RULE, companyId);
        if (segmentationRuleIds.size() > 0)
        {
            removeSegmentationRuleTmProfile(conn, segmentationRuleIds);
        }
        logStart("SEGMENTATION_RULE");
        execOnce(conn, SQL_DELETE_SEGMENTATION_RULE, companyId);
        logEnd("SEGMENTATION_RULE");
    }

    private void removeSegmentationRuleTmProfile(Connection conn,
            List<List<Object>> segmentationRuleIds) throws SQLException
    {
        logStart("SEGMENTATION_RULE_TM_PROFILE");
        exec(conn, SQL_DELETE_SEGMENTATION_RULE_TM_PROFILE, segmentationRuleIds);
        logEnd("SEGMENTATION_RULE_TM_PROFILE");
    }

    private void removeSourcePage(Connection conn,
            List<List<Object>> sourcePageIds) throws SQLException
    {
        logStart("SOURCE_PAGE");
        long companyId = company.getId();
        if (!isJobRemoval)
            sourcePageIds = queryBatchList(conn, SQL_QUERY_SOURCE_PAGE, companyId);
        
        if (sourcePageIds.size() > 0)
        {
            List<List<Object>> leverageGroupIds = queryBatchList(conn,
                    SQL_JOB_QUERY_LEVERAGE_GROUP, sourcePageIds);
            if (leverageGroupIds.size() > 0)
            {
                if (isJobRemoval) {
                    removeTuTuvForJobRemoval(conn, companyId, leverageGroupIds);
                } else {
                    removeTuTuvForCompanyRemoval(conn, companyId, leverageGroupIds);
                }
            }

            removeSourcePageLeverageGroup(conn, sourcePageIds);
            removeLeverageMatch(conn, sourcePageIds, companyId);
            removeTemplate(conn, sourcePageIds);
            removeTargetPage(conn, sourcePageIds);
            removePageTm(conn, sourcePageIds);
            // set PREVIOUS_PAGE_ID to null first
            exec(conn, SQL_UPDATE_SOURCE_PAGE, sourcePageIds);
        }
        
        if (!isJobRemoval) {
            execOnce(conn, SQL_DELETE_SOURCE_PAGE, companyId);
        } else {
            List<List<Object>> cuvIds = queryBatchList(conn,
                    SQL_JOB_QUERY_CORPUS, sourcePageIds);
            exec(conn, SQL_JOB_UPDATE_SOURCE_PAGE, sourcePageIds);
            exec(conn, SQL_JOB_DELETE_SOURCE_PAGE, sourcePageIds);
            removeCorpus(conn, cuvIds);
            removeUntractedFiles(sourcePageIds);
        }
        logEnd("SOURCE_PAGE");
    }

    // Only delete from SYSTEM and COMPANY level tables as they will not be
    // dropped when discard job.
    // For JOB level tables, dropping them later is enough.
    private void removeTuTuvForJobRemoval(Connection conn, long companyId,
            List<List<Object>> leverageGroupIds) throws SQLException
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("TRANSLATION_UNIT", "TRANSLATION_UNIT_VARIANT");
        map.put("TRANSLATION_UNIT_ARCHIVED", "TRANSLATION_UNIT_VARIANT_ARCHIVED");
        map.put("TRANSLATION_UNIT_" + companyId, "TRANSLATION_UNIT_VARIANT_" + companyId);
        map.put("TRANSLATION_UNIT_" + companyId + "_ARCHIVED", "TRANSLATION_UNIT_VARIANT_" + companyId + "_ARCHIVED");

        String tuTableName = null;
        String tuvTableName = null;
        for (Entry<String, String> entry : map.entrySet())
        {
            tuTableName = entry.getKey();
            tuvTableName = entry.getValue();

            // remove TU data of job
            if (!DbUtil.isTableExisted(conn, tuTableName))
            {
                continue;
            }
            List<List<Object>> tuIds = queryBatchList(conn, "select ID from "
                    + tuTableName + " where LEVERAGE_GROUP_ID in ",
                    leverageGroupIds);
            if (tuIds.size() > 0)
            {
                removeRemovedPrefixTag(conn, tuIds);
                removeRemovedSuffixTag(conn, tuIds);
                removeRemovedTag(conn, tuIds);

				if (DbUtil.isTableExisted("TRANSLATION_TU_TUV_ATTR_" + companyId))
                {
                	exec(conn, "delete from TRANSLATION_TU_TUV_ATTR_" + companyId + " where object_type = 'TU' and object_id in ", tuIds);
                }
            }

            // remove TUV data of job
            List<List<Object>> tuvIds = queryBatchList(conn, "select ID from "
                    + tuvTableName + " where TU_ID in ", tuIds);
            if (tuvIds.size() > 0)
            {
                removeXliffAlt(conn, tuvIds);
                removeIssues(conn, tuvIds);
                // to be safe, delete again via tuvIds
				if (DbUtil.isTableExisted("TRANSLATION_TU_TUV_ATTR_" + companyId))
                {
                	exec(conn, "delete from TRANSLATION_TU_TUV_ATTR_" + companyId + " where object_type = 'TUV' and object_id in ", tuvIds);
                }
                exec(conn, "delete from " + tuvTableName + " where ID in ", tuvIds);
            }

            exec(conn, "delete from " + tuTableName + " where ID in ", tuIds);
        }
    }

    /**
     * When remove "old" company, need delete tu/tuv data from both tu/tuv and
     * their archived tables. for "new" company, "drop" is enough in
     * "removeTu()" and "removeTuv()".
     */
    private void removeTuTuvForCompanyRemoval(Connection conn, long companyId,
            List<List<Object>> leverageGroupIds) throws SQLException
    {
        String tuTableName = "TRANSLATION_UNIT";
        String tuvTableName = "TRANSLATION_UNIT_VARIANT";
        if (DbUtil.isTableExisted(conn, tuTableName))
        {
            List<List<Object>> tuIds = queryBatchList(conn, "select ID from "
                    + tuTableName + " where LEVERAGE_GROUP_ID in ",
                    leverageGroupIds);
            if (tuIds.size() > 0)
            {
                // remove TU data of job
                removeRemovedPrefixTag(conn, tuIds);
                removeRemovedSuffixTag(conn, tuIds);
                removeRemovedTag(conn, tuIds);

                // remove TUV data of job
                List<List<Object>> tuvIds = queryBatchList(conn,
                        "select ID from " + tuvTableName + " where TU_ID in ",
                        tuIds);
                if (tuvIds.size() > 0)
                {
                    removeXliffAlt(conn, tuvIds);
                    removeIssues(conn, tuvIds);
                    exec(conn, "delete from " + tuvTableName + " where ID in ",
                            tuvIds);
                }

                exec(conn, "delete from " + tuTableName + " where ID in ",
                        tuIds);
            }
        }

        String tuArchivedTableName = "TRANSLATION_UNIT_ARCHIVED";
        String tuvArchivedTableName = "TRANSLATION_UNIT_VARIANT_ARCHIVED";
        if (DbUtil.isTableExisted(conn, tuArchivedTableName))
        {
            List<List<Object>> tuIds = queryBatchList(conn, "select ID from "
                    + tuArchivedTableName + " where LEVERAGE_GROUP_ID in ",
                    leverageGroupIds);
            if (tuIds.size() > 0)
            {
                // remove TU data of job
                removeRemovedPrefixTag(conn, tuIds);
                removeRemovedSuffixTag(conn, tuIds);
                removeRemovedTag(conn, tuIds);

                // remove TUV data of job
                List<List<Object>> tuvIds = queryBatchList(conn,
                        "select ID from " + tuvArchivedTableName
                                + " where TU_ID in ", tuIds);
                if (tuvIds.size() > 0)
                {
                    removeXliffAlt(conn, tuvIds);
                    removeIssues(conn, tuvIds);
                    exec(conn, "delete from " + tuvArchivedTableName
                            + " where ID in ", tuvIds);
                }

                exec(conn, "delete from " + tuArchivedTableName
                        + " where ID in ", tuIds);
            }
        }
    }

    private void removePageTm(Connection conn, List<List<Object>> sourcePageIds) throws SQLException
    {
        if (sourcePageIds.size() == 0)
            return;
        List<List<Object>> externalPages = queryBatchList(conn, SQL_JOB_QUERY_EXTERNAL_PAGE, sourcePageIds);
        if (externalPages.size() > 0) {
            List<List<Object>> pageTmIds = queryBatchList(conn, SQL_JOB_QUERY_PAGE_TM, externalPages);
            if (pageTmIds.size() > 0) {
                removePageTmTuL(conn, pageTmIds);
                removePageTmTuT(conn, pageTmIds);
            }
            exec(conn, SQL_JOB_DELETE_PAGE_TM, pageTmIds);
        }
    }

    private void removePageTmTuT(Connection conn, List<List<Object>> pageTmIds) throws SQLException
    {
        if (pageTmIds.size() == 0)
            return;
        List<List<Object>> tuIds = queryBatchList(conn, SQL_JOB_QUERY_PAGE_TM_TU_T, pageTmIds);
        if (tuIds.size() > 0) {
            logStart("PAGE_TM_TUV_T");
            exec(conn, SQL_JOB_DELETE_PAGE_TM_TUV_T, tuIds);
            logStart("PAGE_TM_TUV_T");

            logStart("PAGE_TM_TU_T");
            exec(conn, SQL_JOB_DELETE_PAGE_TM_TU_T, pageTmIds);
            logEnd("PAGE_TM_TU_T");
        }
    }

    private void removePageTmTuL(Connection conn, List<List<Object>> pageTmIds) throws SQLException
    {
        if (pageTmIds.size() == 0)
            return;
        List<List<Object>> tuIds = queryBatchList(conn, SQL_JOB_QUERY_PAGE_TM_TU_L, pageTmIds);
        if (tuIds.size() > 0) {
            logStart("PAGE_TM_TUV_L");
            exec(conn, SQL_JOB_DELETE_PAGE_TM_TUV_L, tuIds);
            logStart("PAGE_TM_TUV_L");

            logStart("PAGE_TM_TU_L");
            exec(conn, SQL_JOB_DELETE_PAGE_TM_TU_L, pageTmIds);
            logEnd("PAGE_TM_TU_L");
        }
    }

    @SuppressWarnings("rawtypes")
    private void removeUntractedFiles(List<List<Object>> sourcePageIds)
    {
        if (sourcePageIds.size() == 0)
            return;

        File baseUnextractedDir = AmbFileStoragePathUtils
                .getUnextractedParentDir();
        File dir = null;
        for (Object object : sourcePageIds)
        {
            if (object instanceof List)
            {
                for (Object o : (List) object)
                {
                    String sourcePageId = String.valueOf((Long) o);
                    dir = new File(baseUnextractedDir, sourcePageId);
                    FileUtil.deleteFile(dir);
                }
            }
        }
    }

    private void removeCorpus(Connection conn, List<List<Object>> cuvIds) throws SQLException
    {
        removeCorpusMapForJob(conn, cuvIds);
        List<List<Object>> cuIds = queryBatchList(conn, SQL_JOB_QUERY_CORPUS_UNIT, cuvIds);
        removeCorpusUnitVariant(conn, cuIds);
        exec(conn, SQL_JOB_DELETE_CORPUS_UNIT, cuIds);
    }

    private void removeCorpusUnitVariant(Connection conn,
            List<List<Object>> cuIds) throws SQLException
    {
        logStart("CORPUS_UNIT_VARIANT");
        exec(conn, SQL_JOB_DELETE_CORPUS_UNIT_VARIANT, cuIds);
        logEnd("CORPUS_UNIT_VARIANT");
    }

    private void removeCorpusMapForJob(Connection conn,
            List<List<Object>> cuvIds) throws SQLException
    {
        logStart("CORPUS_MAP");
        exec(conn, SQL_JOB_DELETE_CORPUS_MAP, cuvIds);
        logEnd("CORPUS_MAP");
    }
    

    private void removeSourcePageLeverageGroup(Connection conn,
            List<List<Object>> sourcePageIds) throws SQLException
    {
        logStart("SOURCE_PAGE_LEVERAGE_GROUP");
        exec(conn, SQL_DELETE_SOURCE_PAGE_LEVERAGE_GROUP, sourcePageIds);
        logEnd("SOURCE_PAGE_LEVERAGE_GROUP");
    }

    private void removeSsoUserMapping(Connection conn) throws SQLException
    {
        logStart("SSO_USER_MAPPING");
        execOnce(conn, SQL_DELETE_SSO_USER_MAPPING, company.getId());
        logEnd("SSO_USER_MAPPING");
    }

    private void removeSurcharge(Connection conn, List<List<Object>> costIds)
            throws SQLException
    {
        logStart("SURCHARGE");
        exec(conn, SQL_DELETE_SURCHARGE, costIds);
        logEnd("SURCHARGE");
    }

    private void removeSystemParameter(Connection conn) throws SQLException
    {
        logStart("SYSTEM_PARAMETER");
        execOnce(conn, SQL_DELETE_SYSTEM_PARAMETER, company.getId());
        logEnd("SYSTEM_PARAMETER");
    }

    private void removeTargetPage(Connection conn,
            List<List<Object>> sourcePageIds) throws SQLException
    {
        List<List<Object>> targetPageIds = queryBatchList(conn,
                SQL_QUERY_TARGET_PAGE, sourcePageIds);
        if (targetPageIds.size() > 0)
        {
            removeTargetPageLeverageGroup(conn, targetPageIds);
            removeImageReplaceFileMap(conn, targetPageIds);
        }
        logStart("TARGET_PAGE");
        exec(conn, SQL_DELETE_TARGET_PAGE, targetPageIds);
        logEnd("TARGET_PAGE");
    }

    private void removeTargetPageLeverageGroup(Connection conn,
            List<List<Object>> targetPageIds) throws SQLException
    {
        logStart("TARGET_PAGE_LEVERAGE_GROUP");
        exec(conn, SQL_DELETE_TARGET_PAGE_LEVERAGE_GROUP, targetPageIds);
        logEnd("TARGET_PAGE_LEVERAGE_GROUP");
    }

    private void removeScorecardCategory(Connection conn) throws SQLException
    {
        logStart("CATEGORY_SCORECARD");
        long companyId = company.getId();
        execOnce(conn, SQL_DELETE_SCORECARD_CATEGORY_BY_COMPANY_ID, companyId);

        logEnd("CATEGORY_SCORECARD");
    }

    private void removeMindTouchTargetServers(Connection conn) throws SQLException
    {
        logStart("CONNECTOR_MINDTOUCH_TARGET_SERVER");
		String sql = "delete from CONNECTOR_MINDTOUCH_TARGET_SERVER where company_id = ?";
        execOnce(conn, sql, company.getId());
        logEnd("CONNECTOR_MINDTOUCH_TARGET_SERVER");
    }

    private void removeMindTouchConnectors(Connection conn) throws SQLException
    {
        logStart("CONNECTOR_MINDTOUCH");
		String sql = "delete from CONNECTOR_MINDTOUCH where company_id = ?";
        execOnce(conn, sql, company.getId());
        logEnd("CONNECTOR_MINDTOUCH");
    }

    private void removeBlaiseConnectorJob(Connection conn) throws SQLException
    {
    	long companyId = company.getId();
        List<List<Object>> jobIds = queryBatchList(conn, SQL_QUERY_JOB,
                companyId);
        if (jobIds.size() > 0)
        {
        	removeBlaiseConnectorJobByJobId(conn, jobIds);
        }
    }

    private void removeBlaiseConnectors(Connection conn) throws SQLException
    {
        logStart("CONNECTOR_BLAISE");
		String sql = "delete from CONNECTOR_BLAISE where company_id = ?";
        execOnce(conn, sql, company.getId());
        logEnd("CONNECTOR_BLAISE");
    }

    private void removeGitConnector(Connection conn) throws SQLException
    {
        logStart("CONNECTOR_GIT");
        long companyId = company.getId();
        execOnce(conn, SQL_DELETE_GIT_CONNECTOR_BY_COMPANY_ID, companyId);

        logEnd("CONNECTOR_GIT");
    }

    private void removeGitConnectorJob(Connection conn) 
    		throws SQLException
    {
    	long companyId = company.getId();
        List<List<Object>> jobIds = queryBatchList(conn, SQL_QUERY_JOB,
                companyId);
        if (jobIds.size() > 0)
        {
        	removeGitConnectorJobByJobId(conn, jobIds);
        }
    }

    private void removeGitConnectorFileMapping(Connection conn) throws SQLException
    {
        logStart("CONNECTOR_GIT_FILE_MAPPING");
        long companyId = company.getId();
        execOnce(conn, SQL_DELETE_GIT_CONNECTOR_FILE_MAPPING_BY_COMPANY_ID, companyId);

        logEnd("CONNECTOR_GIT_FILE_MAPPING");
    }
    
    private void romovePostReviewCategory(Connection conn) throws SQLException
    {
        logStart("CATEGORY_POST_REVIEW");
        long companyId = company.getId();
        execOnce(conn, SQL_DELETE_POST_REVIEW_CATEGORY_BY_COMPANY_ID, companyId);

        logEnd("CATEGORY_POST_REVIEW");
    }
    
    private void removeTaskInfo(Connection conn) throws SQLException
    {
        logStart("TASK_INFO");
        long companyId = company.getId();

        List<List<Object>> taskIds = queryBatchList(conn,
                SQL_QUERY_TASK_INFO_BY_COMPANY_ID, companyId);
        if (taskIds.size() > 0)
        {
            removeTaskInfoAssociatedData(conn, taskIds);
        }
        execOnce(conn, SQL_DELETE_TASK_INFO_BY_COMPANY_ID, companyId);

        logEnd("TASK_INFO");
    }

    private void removeTaskInfoAssociatedData(Connection conn,
            List<List<Object>> taskIds) throws SQLException
    {
        removeAmountOfWork(conn, taskIds);
        removeTaskTuv(conn, taskIds);
        removeVendorRating(conn, taskIds);
        removeReservedTime(conn, null, taskIds);
        removeTaskComments(conn, taskIds);
        remkoveTaskInterim(conn, taskIds);
        // set TASK_ID to null in EXPORT_BATCH_EVENT table first
        exec(conn, SQL_UPDATE_EXPORT_BATCH_EVENT, taskIds);
    }

    private void remkoveTaskInterim(Connection conn, List<List<Object>> taskIds) throws SQLException
    {
        exec(conn, SQL_JOB_DELETE_TASK_INTERIM, taskIds);
    }

    @SuppressWarnings("rawtypes")
    private void removeTaskComments(Connection conn, List<List<Object>> taskIds) throws SQLException
    {
        if (isJobRemoval) {
            List<List<Object>> commentIds = queryBatchList(conn, SQL_JOB_QUERY_TASK_COMMENTS, taskIds);
            if (commentIds.size() > 0) {
                for (Object object : commentIds) {
                    if (object instanceof List) {
                        for (Object o : (List) object) {
                            String commentId = String.valueOf((Long) o);
                            removeCommentFile(commentId);
                        }
                    }
                }
            }
        }
        exec(conn, SQL_JOB_DELETE_TASK_COMMENTS, taskIds);
    }

    private void removeCommentFile(String commentId)
    {
        try
        {
            if (StringUtil.isEmpty(commentId))
                return;

            String baseFSDir = AmbFileStoragePathUtils
                    .getCommentReferenceDirPath();
            File file = new File(baseFSDir, commentId);
            if (file.exists() && file.isDirectory())
                FileUtil.deleteFile(file);
        }
        catch (Exception e)
        {
        }
    }

    private void removeTaskInterim(Connection conn, List<List<Object>> userIds)
            throws SQLException
    {
        logStart("TASK_INTERIM");
        exec(conn, SQL_DELETE_TASK_INTERIM, userIds);
        logEnd("TASK_INTERIM");
    }

    private void removeTaskTuv(Connection conn, List<List<Object>> taskIds)
            throws SQLException
    {
        logStart("TASK_TUV");
        exec(conn, SQL_DELETE_TASK_TUV, taskIds);
        logEnd("TASK_TUV");
    }

    private void removeTemplate(Connection conn,
            List<List<Object>> sourcePageIds) throws SQLException
    {
        List<List<Object>> templateIds = queryBatchList(conn,
                SQL_QUERY_TEMPLATE, sourcePageIds);
        if (templateIds.size() > 0)
        {
            removeTemplatePart(conn, templateIds);
        }

        logStart("TEMPLATE");
        exec(conn, SQL_DELETE_TEMPLATE, templateIds);
        logEnd("TEMPLATE");
    }

    private void removeTemplatePart(Connection conn,
            List<List<Object>> templateIds) throws SQLException
    {
		List<List<Object>> templatePartIds = queryBatchList(conn,
				"SELECT ID FROM TEMPLATE_PART WHERE TEMPLATE_ID IN ",
				templateIds);
		logStart("TEMPLATE_PART");
        exec(conn, SQL_DELETE_TEMPLATE_PART, templatePartIds);
        logEnd("TEMPLATE_PART");

        // "TEMPLATE_PART_ARCHIVED"
        if (DbUtil.isTableExisted(conn, "TEMPLATE_PART_ARCHIVED"))
        {
			templatePartIds = queryBatchList(
					conn,
					"SELECT ID FROM TEMPLATE_PART_ARCHIVED WHERE TEMPLATE_ID IN ",
					templateIds);
            logStart("TEMPLATE_PART_ARCHIVED");
			exec(conn, SQL_DELETE_TEMPLATE_PART_ARCHIVED, templatePartIds);
            logEnd("TEMPLATE_PART_ARCHIVED");
        }
    }

    private void removeTermbase(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> tbIds = queryBatchList(conn, SQL_QUERY_TB_TERMBASE,
                companyId);
        if (tbIds.size() > 0)
        {
            removeTbConcept(conn, tbIds);
            removeTbLanguage(conn, tbIds);
            removeTbLock(conn, tbIds);
            removeTbScheduledJobs(conn, tbIds);
            removeTbTerm(conn, tbIds);
            removeTbUserData(conn, tbIds);
            removeTermLeverageMatch(conn, tbIds);
        }
        logStart("TB_TERMBASE");
        execOnce(conn, SQL_DELETE_TB_TERMBASE, companyId);
        logEnd("TB_TERMBASE");
    }

    private void removeTbConcept(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_CONCEPT");
        exec(conn, SQL_DELETE_TB_CONCEPT, tbIds);
        logEnd("TB_CONCEPT");
    }

    private void removeTbLanguage(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_LANGUAGE");
        exec(conn, SQL_DELETE_TB_LANGUAGE, tbIds);
        logEnd("TB_LANGUAGE");
    }

    private void removeTbLock(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_LOCK");
        exec(conn, SQL_DELETE_TB_LOCK, tbIds);
        logEnd("TB_LOCK");
    }

    private void removeTbScheduledJobs(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_SCHEDULED_JOBS");
        exec(conn, SQL_DELETE_TB_SCHEDULED_JOBS, tbIds);
        logEnd("TB_SCHEDULED_JOBS");
    }

    private void removeTbTerm(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_TERM");
        exec(conn, SQL_DELETE_TB_TERM, tbIds);
        logEnd("TB_TERM");
    }

    private void removeTbUserData(Connection conn, List<List<Object>> tbIds)
            throws SQLException
    {
        logStart("TB_USER_DATA");
        exec(conn, SQL_DELETE_TB_USER_DATA, tbIds);
        logEnd("TB_USER_DATA");
    }

    private void removeTdaTm(Connection conn, List<List<Object>> tmProfileIds)
            throws SQLException
    {
        logStart("TDA_TM");
        exec(conn, SQL_DELETE_TDA_TM, tmProfileIds);
        logEnd("TDA_TM");
    }

    private void removeTermLeverageMatch(Connection conn,
            List<List<Object>> tbIds) throws SQLException
    {
        logStart("TERM_LEVERAGE_MATCH");
        exec(conn, SQL_DELETE_TERM_LEVERAGE_MATCH, tbIds);
        logEnd("TERM_LEVERAGE_MATCH");
    }

    private void removeTmAttribute(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        logStart("PROJECT_TM_ATTRIBUTE");
        exec(conn, SQL_DELETE_TM_ATTRIBUTE, projectTmIds);
        logEnd("PROJECT_TM_ATTRIBUTE");
    }

    private void removeTmProfileProjectTmInfo(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        logStart("TM_PROFILE_PROJECT_TM_INFO");
        exec(conn, SQL_DELETE_TM_PROFILE_PROJECT_TM_INFO, projectTmIds);
        logEnd("TM_PROFILE_PROJECT_TM_INFO");
    }

    private void removeTmProfile(Connection conn,
            List<List<Object>> projectTmIds) throws SQLException
    {
        List<List<Object>> tmProfileIds = queryBatchList(conn,
                SQL_QUERY_TM_PROFILE, projectTmIds);
        if (tmProfileIds.size() > 0)
        {
            removeTmProfileAttribute(conn, tmProfileIds);
            removeTdaTm(conn, tmProfileIds);
        }
        logStart("TM_PROFILE");
        exec(conn, SQL_DELETE_TM_PROFILE, projectTmIds);
        logEnd("TM_PROFILE");
    }

    private void removeTmProfileAttribute(Connection conn,
            List<List<Object>> tmProfileIds) throws SQLException
    {
        logStart("TM_PROFILE_ATTRIBUTE");
        exec(conn, SQL_DELETE_TM_PROFILE_ATTRIBUTE, tmProfileIds);
        logEnd("TM_PROFILE_ATTRIBUTE");
    }

    private void removeTm3Attr(Connection conn, List<List<Object>> tm3Ids)
            throws SQLException
    {
        logStart("TM3_ATTR");
        exec(conn, SQL_DELETE_TM3_ATTR, tm3Ids);
        logEnd("TM3_ATTR");
    }

    private void removeTm3Events(Connection conn, List<List<Object>> tm3Ids)
            throws SQLException
    {
        logStart("TM3_EVENTS");
        exec(conn, SQL_DELETE_TM3_EVENTS, tm3Ids);
        logEnd("TM3_EVENTS");
    }

    private void removeTm3Tables(Connection conn)
    {
        String companyName = company.getCompanyName();
        CATEGORY.info("Deleting company " + companyName
                + " records in TM3 tables");
        DefaultManager.create().removeStoragePool(conn, company.getId());
        CATEGORY.info("Done deleting company " + companyName
                + " records in TM3 tables");
    }

    private void removeTm3Tm(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> tm3Ids = queryBatchList(conn, SQL_QUERY_TM3_TM,
                companyId);
        if (tm3Ids.size() > 0)
        {
            removeTm3Attr(conn, tm3Ids);
            removeTm3Events(conn, tm3Ids);
        }
        logStart("TM3_TM");
        execOnce(conn, SQL_DELETE_TM3_TM, companyId);
        logEnd("TM3_TM");
    }

    private void removeTmTbUsers(Connection conn, List<List<Object>> userIds)
            throws SQLException
    {
        logStart("TM_TB_USERS");
        exec(conn, SQL_DELETE_TM_TB_USERS, userIds);
        logEnd("TM_TB_USERS");
    }

    private void removeTuTuvIndex(Connection conn) throws SQLException
    {
        String tableName = "TRANSLATION_TU_TUV_INDEX";
        if (!DbUtil.isTableExisted(conn, tableName))
        {
            return;
        }
        logStart(tableName);
        execOnce(conn, SQL_DELETE_TRANSLATION_TU_TUV_INDEX, company.getId());
        logEnd(tableName);
    }

    // Drop all TU tables (for remove company ONLY)
    private void removeTu(Connection conn, List<Long> p_jobIds)
            throws SQLException
    {
        long companyId = company.getId();
        List<String> tuTables = new ArrayList<String>();
        tuTables.add("TRANSLATION_UNIT_" + companyId);
        tuTables.add("TRANSLATION_UNIT_" + companyId + "_ARCHIVED");
        if (p_jobIds != null && p_jobIds.size() > 0)
        {
            for (long jobId : p_jobIds)
            {
                tuTables.add("TRANSLATION_UNIT_" + companyId + "_" + jobId);
            }            
        }

        for (String tableName : tuTables)
        {
            if (!DbUtil.isTableExisted(conn, tableName))
            {
                continue;
            }

            List<List<Object>> tuIds = queryBatchList(conn, SQL_QUERY_ID
                    + tableName);
            if (tuIds.size() > 0)
            {
                removeRemovedPrefixTag(conn, tuIds);
                removeRemovedSuffixTag(conn, tuIds);
                removeRemovedTag(conn, tuIds);
            }

            logStart(tableName);
            execOnce(conn, SQL_DROP + tableName);
            logEnd(tableName);
        }
    }

    // drop TRANSLATION_TU_TUV_ATTR_x table (for company removal ONLY)
    private void removeTuTuvAttr(Connection conn) throws SQLException
    {
    	String tableName = "TRANSLATION_TU_TUV_ATTR_" + company.getId();
        logStart(tableName);
        execOnce(conn, SQL_DROP + tableName);
        logEnd(tableName);
    }

    // Drop all TUV tables (for remove company ONLY)
    private void removeTuv(Connection conn, List<Long> p_jobIds)
            throws SQLException
    {
        long companyId = company.getId();
        List<String> tuvTables = new ArrayList<String>();
        tuvTables.add("TRANSLATION_UNIT_VARIANT_" + companyId);
        tuvTables.add("TRANSLATION_UNIT_VARIANT_" + companyId + "_ARCHIVED");
        if (p_jobIds != null && p_jobIds.size() > 0)
        {
            for (long jobId : p_jobIds)
            {
                tuvTables.add("TRANSLATION_UNIT_VARIANT_" + companyId + "_"
                        + jobId);
            }            
        }

        for (String tableName : tuvTables)
        {
            if (!DbUtil.isTableExisted(conn, tableName))
            {
                continue;
            }

            List<List<Object>> tuvIds = queryBatchList(conn, SQL_QUERY_ID
                    + tableName);
            if (tuvIds.size() > 0)
            {
                removeXliffAlt(conn, tuvIds);
            }

            logStart(tableName);
            execOnce(conn, SQL_DROP + tableName);
            logEnd(tableName);
        }
    }

    private void removeUpdatedSourcePage(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
        logStart("UPDATED_SOURCE_PAGE");
        exec(conn, SQL_DELETE_UPDATED_SOURCE_PAGE, jobIds);
        logEnd("UPDATED_SOURCE_PAGE");
    }

    private void removeUser(Connection conn) throws Exception
    {
        String companyName = company.getCompanyName();
        UserManager um = ServerProxy.getUserManager();
        Vector<User> users = um.getUsersFromCompany(String.valueOf(company
                .getId()));
        List<List<Object>> userIds = new ArrayList<List<Object>>();
        List<List<Object>> userNames = new ArrayList<List<Object>>();
        List<Object> userIdSubList = new ArrayList<Object>();
        List<Object> userNameSubList = new ArrayList<Object>();
        CATEGORY.info("Deleting company " + companyName
                + " records in table USER");
        for (User user : users)
        {
            String userId = user.getUserId();
            String userName = user.getUserName();
            // remove user from LDAP database
            um.removeUserFromLDAP(userId);
            userIdSubList.add(userId);
            userNameSubList.add(userName);
        }
        if (userIdSubList.size() > 0)
        {
            userIds.add(userIdSubList);
            userNames.add(userNameSubList);
        }

        if (userIds.size() > 0)
        {
            // remove user associated data from MySQL database
            removeTaskInterim(conn, userIds);
            removeTmTbUsers(conn, userIds);
            removeUserDefaultRoles(conn, userIds);
            removeUserFieldSecurity(conn, userIds);
            removeUserParameter(conn, userIds);
            removeRemoteAccessHistory(conn, userIds);
            removeComment(conn, userIds, userNames);
            removeUserIdUserName(conn, userNames);
        }

        CATEGORY.info("Done deleting company " + companyName
                + " records in table USER");
    }

    private void removeUserCalendar(Connection conn,
            List<List<Object>> calendarIds) throws SQLException
    {
        List<List<Object>> userCalendarIds = queryBatchList(conn,
                SQL_QUERY_USER_CALENDAR, calendarIds);
        if (userCalendarIds.size() > 0)
        {
            removeReservedTime(conn, userCalendarIds, null);
            removeUserCalendarWorkingDay(conn, userCalendarIds);
        }
        logStart("USER_CALENDAR");
        exec(conn, SQL_DELETE_USER_CALENDAR, calendarIds);
        logEnd("USER_CALENDAR");
    }

    private void removeUserCalendarWorkingDay(Connection conn,
            List<List<Object>> userCalendarIds) throws SQLException
    {
        List<List<Object>> userCalendarWorkingDayIds = queryBatchList(conn,
                SQL_QUERY_USER_CALENDAR_WORKING_DAY, userCalendarIds);
        if (userCalendarWorkingDayIds.size() > 0)
        {
            removeUserCalendarWorkingHour(conn, userCalendarWorkingDayIds);
        }
        logStart("USER_CALENDAR_WORKING_DAY");
        exec(conn, SQL_DELETE_USER_CALENDAR_WORKING_DAY, userCalendarIds);
        logEnd("USER_CALENDAR_WORKING_DAY");
    }

    private void removeUserCalendarWorkingHour(Connection conn,
            List<List<Object>> userCalendarWorkingDayIds) throws SQLException
    {
        logStart("USER_CALENDAR_WORKING_HOUR");
        exec(conn, SQL_DELETE_USER_CALENDAR_WORKING_HOUR,
                userCalendarWorkingDayIds);
        logEnd("USER_CALENDAR_WORKING_HOUR");
    }

    private void removeUserDefaultActivities(Connection conn,
            List<List<Object>> userDefaultRoleIds) throws SQLException
    {
        logStart("USER_DEFAULT_ACTIVITIES");
        exec(conn, SQL_DELETE_USER_DEFAULT_ACTIVITIES, userDefaultRoleIds);
        logEnd("USER_DEFAULT_ACTIVITIES");
    }

    private void removeUserDefaultRoles(Connection conn,
            List<List<Object>> userIds) throws SQLException
    {
        List<List<Object>> userDefaultRoleIds = queryBatchList(conn,
                SQL_QUERY_USER_DEFAULT_ROLES, userIds);
        if (userDefaultRoleIds.size() > 0)
        {
            removeUserDefaultActivities(conn, userDefaultRoleIds);
        }
        logStart("USER_DEFAULT_ROLES");
        exec(conn, SQL_DELETE_USER_DEFAULT_ROLES, userIds);
        logEnd("USER_DEFAULT_ROLES");
    }

    private void removeUserFieldSecurity(Connection conn,
            List<List<Object>> userIds) throws SQLException
    {
        logStart("USER_FIELD_SECURITY");
        exec(conn, SQL_DELETE_USER_FIELD_SECURITY, userIds);
        logEnd("USER_FIELD_SECURITY");
    }

    private void removeUserIdUserName(Connection conn,
            List<List<Object>> userNames) throws SQLException
    {
        String tableName = "USER_ID_USER_NAME";
        if (!DbUtil.isTableExisted(conn, tableName))
        {
            return;
        }
        logStart(tableName);
        exec(conn, SQL_DELETE_USER_ID_USER_NAME, userNames);
        logEnd(tableName);
    }

    private void removeUserParameter(Connection conn, List<List<Object>> userIds)
            throws SQLException
    {
        logStart("USER_PARAMETER");
        exec(conn, SQL_DELETE_USER_PARAMETER, userIds);
        logEnd("USER_PARAMETER");
    }

    private void removeVendorRating(Connection conn, List<List<Object>> taskIds)
            throws SQLException
    {
        logStart("VENDOR_RATING");
        exec(conn, SQL_DELETE_VENDOR_RATING, taskIds);
        logEnd("VENDOR_RATING");
    }

    private void removeVendorRole(Connection conn,
            List<List<Object>> activityIds) throws SQLException
    {
        logStart("VENDOR_ROLE");
        exec(conn, SQL_DELETE_VENDOR_ROLE, activityIds);
        logEnd("VENDOR_ROLE");
    }

    private void removeWorkflow(Connection conn, List<List<Object>> workflowIds) throws SQLException
    {
        logStart("WORKFLOW");
        long companyId = company.getId();
        if (!isJobRemoval)
            workflowIds = queryBatchList(conn, SQL_QUERY_WORKFLOW, companyId);
        
        if (workflowIds.size() > 0)
        {
            removeWorkflowOwner(conn, workflowIds);
            removeExportBatchWorkflow(conn, workflowIds);
            removeSecondaryTargetFile(conn, workflowIds);
            // removing jbpm data
            removeJbpmModuleInstance(conn, workflowIds);
            removeJbpmToken(conn, workflowIds);
            removeJbpmProcessInstance(conn, workflowIds);
            // set WORKFLOW records to null in TARGET_PAGE table first
            exec(conn, SQL_UPDATE_TARGET_PAGE_WORKFLOW_IFLOW_INSTANCE_ID,
                    workflowIds);
            // delete TASK related records in task associated tables first
            List<List<Object>> taskIds = queryBatchList(conn,
                    SQL_QUERY_TASK_INFO_BY_WORKFLOW_ID, workflowIds);
            if (taskIds.size() > 0)
            {
                removeTaskInfoAssociatedData(conn, taskIds);
                exec(conn, SQL_DELETE_TASK_INFO_BY_TASK_ID, taskIds);
            }
        }
        
        if (!isJobRemoval)
        {
            // backup WORKFLOW data
            backupWorkflow(conn);
            execOnce(conn, SQL_DELETE_WORKFLOW, companyId);
        }
        else
            exec(conn, SQL_JOB_DELETE_WORKFLOW, workflowIds);
        logEnd("WORKFLOW");
    }

    private void removeWfStatePostProfile(Connection conn) throws SQLException
    {
        logStart("WORKFLOW_STATE_POSTS");
        long companyId = company.getId();
        execOnce(conn, SQL_DELETE_WORKFLOW_STATE_POST_BY_COMPANY_ID, companyId);
        logEnd("WORKFLOW_STATE_POSTS");
    }
    
    private void removeWorkflowTemplate(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> workflowTemplateIds = queryBatchList(conn,
                SQL_QUERY_WORKFLOW_TEMPLATE_ID, companyId);
        if (workflowTemplateIds.size() > 0)
        {
            removeLeverageLocales(conn, workflowTemplateIds);
            removeWorkflowManager(conn, workflowTemplateIds);
            removeWorkflowRequestWfTemplate(conn, workflowTemplateIds);
        }
        List<List<Object>> processDefinitionIds = queryBatchList(conn,
                SQL_QUERY_WORKFLOW_TEMPLATE_PROCESSDEFINITION_ID, companyId);
        if (processDefinitionIds.size() > 0)
        {
            removeJbpmProcessDefinitionData(conn, processDefinitionIds);
        }
        logStart("WORKFLOW_TEMPLATE");
        execOnce(conn, SQL_DELETE_WORKFLOW_TEMPLATE, companyId);
        logEnd("WORKFLOW_TEMPLATE");
    }

    private void removeWorkflowManager(Connection conn,
            List<List<Object>> workflowTemplateIds) throws SQLException
    {
        logStart("WF_TEMPLATE_WF_MANAGER");
        exec(conn, SQL_DELETE_WF_TEMPLATE_WF_MANAGER, workflowTemplateIds);
        logEnd("WF_TEMPLATE_WF_MANAGER");
    }

    private void removeWorkflowOwner(Connection conn,
            List<List<Object>> workflowIds) throws SQLException
    {
        logStart("WORKFLOW_OWNER");
        exec(conn, SQL_DELETE_WORKFLOW_OWNER, workflowIds);
        logEnd("WORKFLOW_OWNER");
    }

    private void removeWorkflowRequestWfTemplate(Connection conn,
            List<List<Object>> workflowTemplateIds) throws SQLException
    {
        logStart("WORKFLOW_REQUEST_WFTEMPLATE");
        exec(conn, SQL_DELETE_WORKFLOW_REQUEST_WFTEMPLATE, workflowTemplateIds);
        logEnd("WORKFLOW_REQUEST_WFTEMPLATE");
    }
    
    private void removeScoreByJobId(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
    	logStart("SCORE");
        exec(conn, SQL_DELETE_SCORE, jobIds);
        logEnd("SCORE");
    }
    
    private void removeGitConnectorJobByJobId(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
    	logStart("CONNECTOR_GIT_JOB");
        exec(conn, SQL_DELETE_GIT_CONNECTOR_JOB, jobIds);
        logEnd("CONNECTOR_GIT_JOB");
    }

    private void removeBlaiseConnectorJobByJobId(Connection conn,
    		List<List<Object>> jobIds) throws SQLException
    {
    	logStart("CONNECTOR_BLAISE_JOB");
        exec(conn, SQL_DELETE_BLAISE_CONNECTOR_JOB, jobIds);
        logEnd("CONNECTOR_BLAISE_JOB");
    }

    private void removeWorkflowRequest(Connection conn,
            List<List<Object>> jobIds) throws SQLException
    {
        List<List<Object>> workflowRequestIds = queryBatchList(conn,
                SQL_QUERY_WORKFLOW_REQUEST, jobIds);
        if (workflowRequestIds.size() > 0)
        {
            // set WORKFLOW_REQUEST records to null in
            // WORKFLOW_REQUEST_WFTEMPLATE table first
            exec(conn, SQL_UPDATE_WORKFLOW_REQUEST_WFTEMPLATE,
                    workflowRequestIds);
            logStart("WORKFLOW_REQUEST");
            exec(conn, SQL_DELETE_WORKFLOW_REQUEST, workflowRequestIds);
            logEnd("WORKFLOW_REQUEST");
        }
    }

    private void removeXliffAlt(Connection conn, List<List<Object>> tuvIds)
            throws SQLException
    {
        logStart("XLIFF_ALT");
        exec(conn, SQL_DELETE_XLIFF_ALT, tuvIds);
        logEnd("XLIFF_ALT");
    }

    private void removeXmlDtd(Connection conn) throws SQLException
    {
        logStart("XML_DTD");
        execOnce(conn, SQL_DELETE_XML_DTD, company.getId());
        logEnd("XML_DTD");
    }

    private void removeXmlFilter(Connection conn) throws SQLException
    {
        logStart("XML_RULE_FILTER");
        execOnce(conn, SQL_DELETE_XML_RULE_FILTER, company.getId());
        logEnd("XML_RULE_FILTER");
    }

    private void removeXmlRule(Connection conn) throws SQLException
    {
        long companyId = company.getId();
        List<List<Object>> xmlRuleIds = queryBatchList(conn,
                SQL_QUERY_XML_RULE, companyId);
        if (xmlRuleIds.size() > 0)
        {
            removeCustomerColumnDetail(conn, xmlRuleIds);
        }
        logStart("XML_RULE");
        execOnce(conn, SQL_DELETE_XML_RULE, companyId);
        logEnd("XML_RULE");
    }

//    private void removeConverterFile()
//    {
//        CATEGORY.info("Deleting company " + company.getCompanyName()
//                + " converter files in conversion directory");
//        CompanyFileRemoval.removeConverterFiles(company);
//        CATEGORY.info("Done deleting company " + company.getCompanyName()
//                + " converter files in conversion directory");
//    }

    private void removeDocs()
    {
        String companyName = company.getCompanyName();
        File f = AmbFileStoragePathUtils.getCxeDocDir(String.valueOf(company
                .getId()));
        CATEGORY.info("Deleting company " + companyName
                + " records in docs directory");
        if (f != null)
        {
            FileUtil.deleteFile(f);
            // delete again for large files
            FileUtil.deleteFile(f);
        }
        CATEGORY.info("Done deleting company " + companyName
                + " records in docs directory");
    }

    private void removeFileStorage()
    {
        String companyName = company.getCompanyName();
        File f = AmbFileStoragePathUtils.getFileStorageDir(String
                .valueOf(company.getId()));
        CATEGORY.info("Deleting company " + companyName
                + " records in file storage directory");
        if (f != null)
        {
            FileUtil.deleteFile(f);
            // delete again for large files
            FileUtil.deleteFile(f);
        }
        CATEGORY.info("Done deleting company " + companyName
                + " records in file storage directory");
    }

    private void removeProperties()
    {
        String companyName = company.getCompanyName();
        File f = AmbFileStoragePathUtils.getPropertiesDir(String
                .valueOf(company.getId()));
        CATEGORY.info("Deleting company " + companyName
                + " records in properties directory");
        if (f != null)
        {
            FileUtil.deleteFile(f);
        }
        CATEGORY.info("Done deleting company " + companyName
                + " records in properties directory");
    }

    private void dropTuTuvLmTables(Connection conn, long jobId)
            throws SQLException
    {
        long companyId = company.getId();
        List<String> tableNames = new ArrayList<String>();
        tableNames.add("TRANSLATION_UNIT_" + companyId + "_" + jobId);
        tableNames.add("TRANSLATION_UNIT_VARIANT_" + companyId + "_" + jobId);
        tableNames.add("LEVERAGE_MATCH_" + companyId + "_" + jobId);
        tableNames.add("LEVERAGE_MATCH_EXT_" + companyId + "_" + jobId);

        for (String tableName : tableNames)
        {
            if (DbUtil.isTableExisted(tableName))
            {
                logStart(tableName);
                execOnce(conn, SQL_DROP + tableName);
                logEnd(tableName);
            }
        }
    }

    private List<Long> getJobIdsByCompanyId(long companyId)
    {
        List<Long> result = new ArrayList<Long>();
        HashMap<String, Long> params = new HashMap<String, Long>();
        params.put("COMPANYID", companyId);
        String sql = "select ID from JOB where COMPANY_ID= :COMPANYID";
        List<?> jobIds = HibernateUtil.searchWithSql(sql, params);
        if (jobIds != null && jobIds.size() > 0)
        {
            for (int i = 0; i < jobIds.size(); i++)
            {
                BigInteger bi = (BigInteger) jobIds.get(i);
                result.add(bi.longValue());
            }
        }
        return result;
    }

    private void exec(Connection conn, String sql, List<List<Object>> batchList)
            throws SQLException
    {
        int batchCount = batchList.size();
        if (batchCount > 1)
        {
            CATEGORY.info(batchCount
                    + " batches of records found to be deleted, default batch capacity:"
                    + BATCH_CAPACITY);
        }
        int deletedBatchCount = 0;
        for (List<Object> list : batchList)
        {
            execOnce(conn, sql + toInClause(list));
            if (batchCount > 1)
            {
                deletedBatchCount++;
                int leftBatchCount = batchCount - deletedBatchCount;
                String message = "";
                if (deletedBatchCount == 1)
                {
                    if (leftBatchCount == 1)
                    {
                        message = "1 batch deleted, left 1";
                    }
                    else
                    {
                        message = "1 batch deleted, left " + leftBatchCount;
                    }
                }
                else
                {
                    if (leftBatchCount == 1)
                    {
                        message = deletedBatchCount
                                + " batches deleted, left 1";
                    }
                    else if (leftBatchCount > 1)
                    {
                        message = deletedBatchCount + " batches deleted, left "
                                + leftBatchCount;
                    }
                }
                if (leftBatchCount > 0)
                {
                    CATEGORY.info(message);
                }
            }
        }
    }

    private void execOnce(PreparedStatement ps) throws SQLException
    {
        try
        {
            ps.execute();
        }
        catch (Exception e)
        {
            CATEGORY.error("Current SQL :: " + ps.toString());
            throw new SQLException(e.getMessage());
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }
    }

    private static void execOnce(Connection conn, String sql)
            throws SQLException
    {
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            stmt.execute(sql);
        }
        catch (Exception e)
        {
            CATEGORY.info("Current SQL :: " + sql);
            throw new SQLException(e.getMessage());
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
        }
    }

    private void execOnce(Connection conn, String sql, Object param)
            throws SQLException
    {
        try
        {
            execOnce(toPreparedStatement(conn, sql, param));
        }
        catch (Exception e)
        {
            CATEGORY.info("Current SQL :: " + sql);
            throw new SQLException(e.getMessage());
        }
    }

    private List<List<Object>> queryBatchList(Connection conn, String sql)
            throws SQLException
    {
        Statement stmt = null;
        try
        {
            stmt = toStatement(conn);

            ResultSet rs = stmt.executeQuery(sql);

            return toBatchList(rs);
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
        }
    }

    private List<List<Object>> queryBatchList(Connection conn, String sql,
            Object param) throws SQLException
    {
        Statement stmt = null;
        PreparedStatement ps = null;
        try
        {
            ResultSet rs = null;
            if (param instanceof List)
            {
                StringBuilder sb = new StringBuilder(sql);
                sb.append(toInClause((List<?>) param));

                stmt = toStatement(conn);

                rs = stmt.executeQuery(sb.toString());
            }
            else
            {
                ps = toPreparedStatement(conn, sql, param);
                rs = ps.executeQuery();
            }
            return toBatchList(rs);
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentClose(ps);
        }
    }

    private List<List<Object>> toBatchList(ResultSet rs) throws SQLException
    {
        List<List<Object>> batchList = new ArrayList<List<Object>>();
        if (rs == null)
        {
            return batchList;
        }
        List<Object> subList = new ArrayList<Object>();
        int count = 0;
        try
        {
            while (rs.next())
            {
                subList.add(rs.getObject(1));
                count++;
                if (count == BATCH_CAPACITY)
                {
                    batchList.add(subList);
                    subList = new ArrayList<Object>();
                    count = 0;
                }
            }
            if (subList.size() > 0)
            {
                batchList.add(subList);
            }
        }
        finally
        {
            ConnectionPool.silentClose(rs);
        }
        return batchList;
    }

    @SuppressWarnings("rawtypes")
    private String toInClause(List<?> list)
    {
        StringBuilder in = new StringBuilder();
        if (list.size() == 0)
            return "(0)";
        
        in.append("(");
        for (Object o : list)
        {
            if (o instanceof List)
            {
                if (((List) o).size() == 0)
                    continue;
                
                for (Object id : (List<?>) o)
                {
                    if (id instanceof String)
                    {
                        in.append("'");
                        in.append(((String) id).replace("\'", "\\\'"));
                        in.append("'");
                    }
                    else
                    {
                        in.append(id);
                    }
                    in.append(",");
                }
            }
            else if (o instanceof String)
            {
                in.append("'");
                in.append(((String) o).replace("\'", "\\\'"));
                in.append("'");
                in.append(",");
            }
            else
            {
                in.append(o);
                in.append(",");
            }
        }
        in.deleteCharAt(in.length() - 1);
        in.append(")");

        return in.toString();
    }

    private PreparedStatement toPreparedStatement(Connection conn, String sql,
            Object param) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        // will have oom error when querying great number of records without
        // this setting
        ps.setFetchSize(Integer.MIN_VALUE);
        ps.setObject(1, param);
        return ps;
    }

    private Statement toStatement(Connection conn) throws SQLException
    {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        // will have oom error when querying great number of records without
        // this setting
        stmt.setFetchSize(Integer.MIN_VALUE);
        return stmt;
    }

    private void logStart(String table)
    {
        runtime = System.currentTimeMillis();
        
        CATEGORY.info(runningMessage + table);
    }

    private void logEnd(String table)
    {
        long runningTime = System.currentTimeMillis() - runtime;
        
        CATEGORY.info(doneMessage + table + " [" + runningTime + " msc]");
    }
}

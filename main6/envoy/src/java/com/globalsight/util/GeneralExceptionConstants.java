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

package com.globalsight.util;

/**
 * Constants used with the GeneralException class.
 * <p>
 */
public interface GeneralExceptionConstants
{

    // default message used by new exception code
    public static final String DEFAULT_MSG_STRING = "NO MESSAGE";

    // --- old exception code constants----

    // /////////////////////////////////////////////////////////////////////////
    // ////////////////////////// DEFAULT STUFF //////////////////////////////
    // /////////////////////////////////////////////////////////////////////////
    /**
     * Default message id to be used when no message id is specified for this
     * exception.
     */
    public static final int DEFAULT_MSG_ID = 0;

    /**
     * Default message to be used when no specific message is defined for the
     * exception component and id in this exception.
     */
    public static final String DEFAULT_MESSAGE = "Default GeneralException Message!";

    // /////////////////////////////////////////////////////////////////////////
    // ////////////////////////// COMPONENT ID's /////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Id for the "Foundation" component.
     */
    public static final int COMP_FOUNDATION = 1;
    /**
     * String for the "Foundation" component.
     */
    public static final String FOUNDATION = "Foundation";

    /**
     * Id for the "Job Handler" component.
     */
    public static final int COMP_JOBS = 2;
    /**
     * String for the "Job Handler" component.
     */
    public static final String JOBS = "Job Handler";

    /**
     * Id for any "Linguistics" components that have no specific component
     * identifications.
     */
    public static final int COMP_LING = 3;
    /**
     * String for the "Linguistics" component.
     */
    public static final String LING = "Linguistics";

    /**
     * Id for the "Locale Manager" component.
     */
    public static final int COMP_LOCALEMANAGER = 4;
    /**
     * String for the "Locale Manager" component.
     */
    public static final String LOCALEMANAGER = "Locale Manager";

    /**
     * Id for the "Persistence" component.
     */
    public static final int COMP_PERSISTENCE = 5;
    /**
     * String for the "Persistence" component.
     */
    public static final String PERSISTENCE = "Persistence";

    /**
     * Id for the "Request Handler" component.
     */
    public static final int COMP_REQUEST = 6;
    /**
     * String for the "Request Handler" component.
     */
    public static final String REQUEST = "Request";

    /**
     * Id for the "Servlet" component.
     */
    public static final int COMP_SERVLET = 7;
    /**
     * String for the "Servlet" component.
     */
    public static final String SERVLET = "Servlet";

    /**
     * Id for the "User Manager" component.
     */
    public static final int COMP_USERMANAGER = 8;
    /**
     * String for the "User Manager" component.
     */
    public static final String USERMANAGER = "User Manager";

    /**
     * Id for system utilities for Envoy.
     */
    public static final int COMP_ENVOYSYSTEM = 9;
    /**
     * String for the "Envoy System" component.
     */
    public static final String ENVOYSYSTEM = "Envoy System";

    /**
     * Id for the "Admin Interface" component.
     */
    public static final int COMP_WEBAPP = 10;
    /**
     * String for the "Webapp" component.
     */
    public static final String WEBAPP = "Webapp Interface";

    /**
     * Id for the "Workflow" component.
     */
    public static final int COMP_WORKFLOW = 11;
    /**
     * String for "Workflow" component.
     */
    public static final String WORKFLOW = "Workflow";

    /**
     * Id for the virtual component.
     */
    public static final int COMP_GENERAL = 12;
    /**
     * String for the virtual component.
     */
    public static final String GENERAL = "General";

    /**
     * Id for system utilities for all GlobalSight Java software.
     */
    public static final int COMP_SYSUTIL = 13;
    /**
     * String for the system utilities.
     */
    public static final String SYSUTIL = "System Utilities";

    /**
     * Id for the "ProjectHandler" component.
     */
    public static final int COMP_PROJECT = 14;
    /**
     * String for the projectHandler" component.
     */
    public static final String PROJECT = "Project Handler";

    /**
     * Id for the "User Manager" component.
     */
    public static final int COMP_SECURITYMANAGER = 15;
    /**
     * String for the "User Manager" component.
     */
    public static final String SECURITYMANAGER = "Security manager";

    /**
     * Id for the "GXML" component.
     */
    public static final int COMP_GXML = 16;
    /**
     * String for the "GXML" component.
     */
    public static final String GXML = "Gxml";

    /**
     * Id for the "Workflow Manager" component.
     */
    public static final int COMP_WORKFLOWMANAGER = 17;
    /**
     * String for the "workflow Manager" component.
     */
    public static final String WORKFLOWMANAGER = "Workflow Manager";

    /**
     * Id for the "PageImporter" component.
     */
    public static final int COMP_PAGEIMPORTER = 18;
    /**
     * String for the "PageImporter" component.
     */
    public static final String PAGEIMPORTER = "Page Importer";

    /**
     * Id for the "OnlineEditor" component.
     */
    public static final int COMP_ONLINEEDITOR = 19;
    /**
     * String for the "onlineEditor" component.
     */
    public static final String ONLINEEDITOR = "Online Editor";

    /**
     * Id for the "OfflineEditorManager" component.
     */
    public static final int COMP_OFFLINEEDITMANAGER = 20;
    /**
     * String for the "OfflineEditorManager" component.
     */
    public static final String OFFLINEEDITMANAGER = "Offline Edit Manager";

    // Merge from System 3 Ling added constants
    public static final int COMP_SEGMENTER = 21;
    public static final String SEGMENTER = "Segmenter";

    public static final int COMP_MERGER = 22;
    public static final String MERGER = "Merger";

    public static final int COMP_EXTRACTOR = 23;
    public static final String EXTRACTOR = "Extractor";

    public static final int COMP_WORDCOUNTER = 24;
    public static final String WORDCOUNTER = "Wordcounter";

    public static final int COMP_COSTING = 25;
    public static final String COSTING = "CostingEngine";

    // /////////////////////////////////////////////////////////////////////////
    // ///////////////////////// EXCEPTION ID's //////////////////////////////
    // /////////////////////////////////////////////////////////////////////////
    /*
     * General exception id is from 2900 to 2999 while component specific
     * exception id is from 1000 to 2899;
     */
    // /////////////////////////////////////////////////////
    // ///////////// General Exception ID's //////////////
    // /////////////////////////////////////////////////////
    /**
     * Exception id for "general exception", which means any exception that does
     * not have a specific id.
     */
    public static final int EX_GENERAL = 2900; // General exception

    /**
     * Exception id for JMS
     */
    public static final int EX_JMS = 2901; // JMS exception.

    /**
     * Exception id for missing resource exception
     */
    public static final int EX_MISSING_RESOURCE_EXCEPTION = 2902; // Missing
                                                                  // resource
                                                                  // exception

    /**
     * Exception id for javax.naming.NamingException
     */
    public static final int EX_NAMING = 2903; // Naming exception

    /**
     * Exception id for general exception while accessing a properties object.
     */
    public static final int EX_PROPERTIES = 2904; // Properties access exception

    /**
     * Exception id for system or network level exception.
     */
    public static final int EX_REMOTE = 2905; // Remote or network exception

    /**
     * Exception id for java.sql.SQLException An exception that provides
     * information on a database access error.
     */
    public static final int EX_SQL = 2906; // SQL Exception.

    /**
     * Exception id for glossary-related exceptions.
     */
    public static final int EX_GLOSSARY = 2907;

    /**
     * Exception id for comment reference related exceptions.
     */
    public static final int EX_COMMENT_REFERENCE = 2908;

    /**
     * Exception id for native file related exceptions.
     */
    public static final int EX_NATIVE_FILE = 2909;

    // /////////////////////////////////////////////////////////////////////////
    // ////////////////////////// All Message ID's ///////////////////////////
    // /////////////////////////////////////////////////////////////////////////
    /*
     * 1. General ERROR message id is from 4900 to 4999 while component specific
     * ERROR message id is from 3000 to 4899; 2. General INFO message id is from
     * 6900 to 6999 while component specific INFO message id is from 5000 to
     * 6899; 3. General DEBUG message id is from 8900 to 8999 while component
     * specific DEBUG message id is from 7000 to 8899;
     */
    // /////////////////////////////////////////////////////////////////////////
    // //////////////////// General ERROR Message ID's ///////////////////////
    // /////////////////////////////////////////////////////////////////////////
    /**
     * Error message id for MissingResourceException
     */
    public static final int MSG_ERR_NO_MESSAGE = 4900;

    /**
     * Error message id for failing to read system configuration file.
     */
    public static final int MSG_FAILED_TO_READ_SYSTEM_CONFIGURATION_FILE = 4901;

    /**
     * Error message id for failing to lookup usermgr.
     */
    public static final int MSG_FAILED_TO_LOOKUP_USERMGR = 4902;

    /**
     * Error message id for previewing to PDF file.
     */
    public static final int MSG_FAILED_TO_PREVIEW_PDF = 4903;

    /**
     * Error message id for previewing to XML file.
     */
    public static final int MSG_FAILED_TO_PREVIEW_XML = 4904;

    /**
     * Error message id for previewing to XML file.
     */
    public static final int MSG_FAILED_TO_IMPORT_MIF = -1102;
}

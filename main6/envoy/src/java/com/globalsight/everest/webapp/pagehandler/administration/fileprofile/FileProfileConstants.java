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
package com.globalsight.everest.webapp.pagehandler.administration.fileprofile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface FileProfileConstants
{
    // Constant for saving file profile in session
    public static final String FILEPROFILE = "fileprofile";

    // For checking dup names
    public static final String NAMES = "names";

    // For tags
    public static final String FILEPROFILE_LIST = "fileprofiles";
    public static final String FILEPROFILE_KEY = "fileprofile";
    
    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String DEPENDENCIES = "dependencies";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";

    /**
     * Constant used as a key for a list of source formats.
     */
    public static final String SOURCE_FILE_FORMATS = "sourcefileformats";

    /**
     * Constant used as a key for a list of source encodings.
     */
    public static final String SOURCE_FILE_ENCODINGS = "sourcefileencodings";

    /**
     * Constant used as a key for a list of localization profiles.
     */
    public static final String FILE_PROFILE_LOCALIZATION_PROFILES ="fileprofilelocalizationprofiles";

    /**
     * Constant used as a key for a template id..
     */
    public static final String FILE_PROFILE_ID = "fileprofileId";

    /**
     * Constant used for key in TableNav tag
     */
    public static final String KEY = "fileprofile";

    /**
     * Constant used as a key for a particular source format.
     */
    public static final String SOURCE_FILE_FORMAT = "sourceFormat";
    
    /**
     * Constant used as a key for a particular source encoding.
     */
    public static final String SOURCE_FILE_ENCODING = "sourceencoding";
    
    /**
     * Constant used as a key for a list of localization profile.
     */
    public static final String FILE_PROFILE_LOCALIZATION_PROFILE ="fileprofilelocalizationprofile";
    
    /**
     * Constant used as a key for a list of system actions.
     */
    public static final String SYSTEM_ACTION = "systemAction";

    /**
     * Constant used as a key for user role.
     */
    public static final String USER_ROLE = "user";

    /**
     * Constant used as a key for a fileprofile template info object.
     */
    public static final String FP_TEMPLATE_INFO = "fpTemplateInfo";

    /**
     * Constant used as a key for a fileprofile template info id.
     */
    public static final String FP_TEMPLATE_INFO_ID = "fpTemplateInfoId";

    /**
     * Constant used as a key for a fileprofile instance.
     */
    public static final String FP_INSTANCE = "fpInstance";

    /**
     * Constant used as a key for the displayable name of a fileprofile instance.
     */
    public static final String FP_INSTANCE_NAME = "fpInstanceName";
    
    /**
     * Constant used for an advanced search
     */
    public static final String ADV_SEARCH_ACTION = "advancedSearch";
    /**
     * Constant used for an mini search
     */
    public static final String SEARCH_ACTION = "search";
    /**
     * Constant used as a key for a hashtable of fileprofileTaskInstance.
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
    public static final String DESCRIPTION_FIELD = "descField";
    public static final String LOCALE_PAIR_FIELD = "lpField";
    public static final String ENCODING_FIELD = "encodingField";
    public static final String LEVERAGE_FIELD = "leverageField";
    public static final String CHOSEN_NAME = "chosenName";
    public static final String CHOSEN_TARGET_ENCODING = "chosenEncoding";
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
     * Constant used for a verify action.
     */
    public static final String VERIFY_ACTION = "verify";
    //////////////////////////////////////////////////////////////////////
    //  Begin: Action Parameters
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Constants used for verifying script of fileprofile
     */
    public final static int IS_DIRECTORY = 0;
    public final static int FILE_NOT_EXIST = 1;
    public final static int FILE_OK = 2;
    
    /**
     * Default file extensions
     */
    public static final String[] extensions = {
        "doc", "docx", "htm", "html", "indd", "fm", 
        "inx", "java", "js", "jsp", "odp", 
        "ods", "odt", "po", "ppt", "pptx", "properties", 
        "rtf", "txt", "xlf", "xliff", "xls", 
        "xlsx", "xml", "rc", "resx", "idml", "xlz", "mif", "lpu", "exe", "dll"
        };
    
    /**
     * Useless Source File Format & Extensions
     */
    public final String[] useLessFormat = {"ASP", "C++/C", "Catalyst", "ColdFusion", "CSS", "JHTML", "Quark (WIN)", "SGML", "XPTag"};
    public static final List<String> useLessFormatList = new ArrayList<String>(Arrays.asList(useLessFormat));
}

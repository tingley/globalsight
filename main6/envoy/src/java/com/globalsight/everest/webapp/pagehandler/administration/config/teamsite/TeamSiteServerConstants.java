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
package com.globalsight.everest.webapp.pagehandler.administration.config.teamsite;



/**
 * Teamsite Server related constants.
 */

public interface TeamSiteServerConstants
{
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constants used as attribute names
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a key for a template id..
     */
    public static final String SERVER_ID = "serverId";

    /**
     * Constant used as a key for a list of TeamSite templates.
     */
    public static final String SERVERS = "servers";

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
     * what type of sorting to use
     */
    public static final String SORTING = "sorting";
    public static final String REVERSE_SORT= "reverseSort";
    public static final String PAGE_NUM = "pageNum";
    public static final String NUM_OF_PAGES = "numOfPage";
    public static final String TOTAL_SERVERS = "totalServers";
    public static final String NUM_PER_PAGE = "num_per_page";
    public static final String LAST_PAGE_NUM = "lastPageNum";

    /**
     * add parameters for cgi scripts and properties files
     */
    public static final String TEAMSITE_HOME="teamsite_home";
    public static final String TEAMSITE_MASTER="teamsite_master";
    public static final String TEAMSITE_SERVER_NAME="teamsite_server";
    public static final String SERVER_HOST="server_host";
    public static final String SERVER_PORT="server_port";
    public static final String TEAMSITE_STORE_DIR="teamsite_store_dir";
    public static final String TEAMSITE_MOUNT_DIR="teamsite_mount_dir";
    public static final String TEAMSITE_MASTER_USER="teamsite_master";
    public static final String TRANSLATE_URL="ambassador_translate_servlet_url";
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: UI Fields
    //////////////////////////////////////////////////////////////////////
    // fields for the first page of template creation.
    public static final String COMPANY_FIELD = "companyField";
    public static final String NAME_FIELD = "nameField";
    public static final String DESCRIPTION_FIELD = "descField";
    public static final String OS_FIELD = "osField"; 
    public static final String EXPORT_FIELD = "exportField";
    public static final String IMPORT_FIELD = "importField";
    public static final String PROXY_FIELD = "proxyField";
    public static final String HOME_FIELD = "homeField";
    public static final String USER_FIELD = "userField";
    public static final String USER_PASS_FIELD = "userPassField";
    public static final String USER_PASS_REPEAT_FIELD = "userPassRepeatField";
    public static final String TYPE_FIELD = "typeField";
    public static final String MOUNT_FIELD = "mountField";
    public static final String REIMPORT_FIELD = "reimportField";
    public static final String CHOSEN_NAME = "chosenName";
    public static final String CHOSEN_DESCRIPTION = "chosenDesc";
    public static final String CHOSEN_OS = "chosenOs"; 
    public static final String CHOSEN_EXPORT = "chosenExport";
    public static final String CHOSEN_IMPORT = "chosenImport";
    public static final String CHOSEN_PROXY = "chosenProxy";
    public static final String CHOSEN_HOME = "chosenHome";
    public static final String CHOSEN_USER = "chosenUser";
    public static final String CHOSEN_USER_PASS = "chosenUserPass";
    public static final String CHOSEN_TYPE = "chosenType";
    public static final String CHOSEN_MOUNT = "chosenMount";
    public static final String CHOSEN_REIMPORT = "chosenReimport";

    public static final String TEAMSITE_SERVER = "teamsiteServer";
    public static final String MODIFY_ACTION = "modifyAction";
    public static final String MODIFY = "modify";
    public static final String OPERATING_SYSTEMS = "operatingSystems";
    public static final String USER_TYPES = "userTypes";
    public static final String UNIX = "unix";
    public static final String WINDOWS = "nt";
    public static final String MASTER = "master";
    public static final String ADMINISTRATOR = "administrator";

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
     * Constant used for a adding a new store.
     */
    public static final String ADD_ACTION = "add";
    /**
     * Constant used for a new action.
     */
    public static final String NEW_ACTION = "new";
    /**
     * Constant used for a create action.
     */
    public static final String CREATE_ACTION = "create";
    /**
     * Constant used for an edit action.
     */
    public static final String EDIT_ACTION = "edit";
    /**
     * Constant used for a cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for a remove action.
     */
    public static final String REMOVE_ACTION = "remove";
    /**
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";
    /**
     * Constant used for a next action.
     */
    public static final String NEXT_ACTION = "next";

    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for a save TeamSite action.
     */
    public static final String SAVE_TEAMSITE_SERVER_TEMPLATE = "saveTeamSiteTemplate";
    /**
     * Constant used for a selected TeamSite
     */
    public static final String SELECTED_TEAMSITE_SERVER = "selectedTeamSite";
    /**
     * Constant used for content stores
     */
    public static final String CONTENT_STORES = "contentStores";
    public static final String OLD_STORES = "oldStores";
    public static final String BRANCH_STORES = "branchStores";
    public static final String REMOVED_STORES = "removedStores";
    /**
     * Constant used for content stores checkboxes
     */
    public static final String STORE_CHECKBOXES = "storeCheckBoxes";
    /**
     * Constant used for store name
     */
    public static final String STORE_NAME = "storeName";
    /**
     * Constant used for stores
     */
    public static final String STORES = "stores";
}

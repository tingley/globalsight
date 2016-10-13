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
package com.globalsight.everest.foundation;

import java.util.Vector;


public interface Group
{
    // the module this group is part of
    public static final String GROUP_MODULE_GLOBALSIGHT =
        "GlobalSight";
    public static final String GROUP_MODULE_VENDOR_MANAGER =
        "VendorManagement";


    // GlobalSight groups
    public static final String GROUP_ADMINISTRATOR = "Administrator";
    public static final String GROUP_PROJECT_MANAGER = "ProjectManager";
    public static final String GROUP_LOCALE_MANAGER="LocaleManager";
    public static final String GROUP_WORKFLOW_MANAGER = "WorkflowManager";
    public static final String GROUP_LOCALIZATION_PARTICIPANT
                                        = "LocalizationParticipant";
    public static final String GROUP_CUSTOMER = "Customer";

    // VM groups
    public static final String GROUP_VENDOR_ADMIN = "VendorAdministrator";
    public static final String GROUP_VENDOR_MANAGER = "VendorManager";
    public static final String GROUP_VENDOR_VIEWER = "VendorViewer";


    // retrieves the module that the group is part of
    // see GROUP_MODULE_xxxx types
    public String getGroupModuleType();

    public String getGroupName();
    public void setGroupName(String p_groupName);

    public String getGroupDescription();
    public void setGroupDescription(String p_groupDescription);

    public String[] getPermissionNames();
    public void setPermissionNames(String[] p_permissionNames);

    public String[] getUserIds();
    public void setUserIds(String[] p_userIds);
}
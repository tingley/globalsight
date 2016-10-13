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

package com.globalsight.everest.util.applet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppletConstant
{
    public static Map<String, List<String>> ALL_RESOURCE = new HashMap<String, List<String>>();
    public static final String ADD_SOURCE_FILES = "addSourceFiles";
    public static List<String> ADD_SOURCE_FILES_KEYS = new ArrayList<String>();
    static
    {
        ADD_SOURCE_FILES_KEYS.add("lb_file");
        ADD_SOURCE_FILES_KEYS.add("lb_file_profile");
        ADD_SOURCE_FILES_KEYS.add("lb_add_files");
        ADD_SOURCE_FILES_KEYS.add("lb_cancel");
        ADD_SOURCE_FILES_KEYS.add("lb_remove_files");
        ADD_SOURCE_FILES_KEYS.add("lb_clean_map");
        ADD_SOURCE_FILES_KEYS.add("lb_number_directory");
        ADD_SOURCE_FILES_KEYS.add("lb_tip_number_directory");
        ADD_SOURCE_FILES_KEYS.add("msg_file_exist");
        ADD_SOURCE_FILES_KEYS.add("msg_file_added");
        ADD_SOURCE_FILES_KEYS.add("msg_no_map");
        ADD_SOURCE_FILES_KEYS.add("lb_upldate_applet_msg");
        ADD_SOURCE_FILES_KEYS.add("lb_finish");
        ADD_SOURCE_FILES_KEYS.add("lb_processing_upload_file");
        ADD_SOURCE_FILES_KEYS.add("lb_applet_title");
        ADD_SOURCE_FILES_KEYS.add("lb_upload_successful");
        ADD_SOURCE_FILES_KEYS.add("msg_cannot_add_delete_file");
        ADD_SOURCE_FILES_KEYS.add("msg_cannot_add_delete_file2");
        
        ALL_RESOURCE.put(ADD_SOURCE_FILES, ADD_SOURCE_FILES_KEYS);
    }
}

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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.util.HashMap;
import java.util.Map;

/**
 * This interface contains all of the configuration export and import related constants.
 */
public interface ConfigConstants
{
    Map<String, String> config_error_map = new HashMap<String, String>();

    public static final String LOCALEPAIR_FILE_NAME = "LocalePairs_";
    public static final String USER_FILE_NAME = "User_information_";
    public static final String MT_FILE_NAME = "MachineTranslationProfiles_";
    public static final String FILTER_FILE_NAME = "FilterConfiguration_";
}

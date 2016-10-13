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
package com.plug.Version_8_5_2.gs.util;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.config.properties.InstallValues;

/**
 * Used for obtaining file stored paths in GlobalSight.
 * 
 */
public class AmbFileStoragePathUtils
{
    private static final String FILE_STORAGE_DIR = "file_storage_dir";
    
    public static File getFileStorageDir()
    {
        return new File(InstallValues.getIfNull(FILE_STORAGE_DIR, null));
    }
}

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

import java.io.IOException;
import java.util.Properties;

import com.globalsight.log.GlobalSightCategory;

public class ServerUtil
{
    static private final GlobalSightCategory logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(ServerUtil.class);

    private static String version = null;

    public static String getVersion()
    {
        if (version == null)
        {
            Properties p = new Properties();

            try
            {
                p.load(ServerUtil.class
                        .getResourceAsStream("/properties/server.properties"));
                version = (String) p.get("version");
            }
            catch (IOException e)
            {
                logger.error(e);
            }
        }
        
        return version;
    }
}

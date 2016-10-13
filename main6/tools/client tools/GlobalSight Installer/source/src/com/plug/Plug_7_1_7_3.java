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
package com.plug;

import java.io.File;

import org.apache.log4j.Logger;

import com.util.ServerUtil;

public class Plug_7_1_7_3 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_7_3.class);

    private static final String LIB_PATH = "/jboss/jboss_server/server/default/"
            + "deploy/globalsight.ear/lib";

    @Override
    public void run()
    {
        removeEtoolsJar();
    }

    private void removeEtoolsJar()
    {
        File file = new File(ServerUtil.getPath() + LIB_PATH + "/etools.jar");
        if (file.exists())
        {
            if (!file.delete())
            {
                log.error("Failed to remove etools.jar");
            }
        }
    }
}

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

import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_8_7_4 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_8_7_4.class);

    private static final String JAR_XML_APIS = "/jboss/server/standalone/deployments/globalsight.ear/lib/xml-apis-1.3.04.jar";
    private static final String JAR_XML_APIS_EXT = "/jboss/server/standalone/deployments/globalsight.ear/lib/xml-apis-ext-1.3.04.jar";

    @Override
    public void run()
    {
        // GBS-4660: delete xml-apis*.jar from build
        deleteFiles(ServerUtil.getPath() + JAR_XML_APIS);
        deleteFiles(ServerUtil.getPath() + JAR_XML_APIS_EXT);
    }

    private void deleteFiles(String path)
    {
        try
        {
            File f = new File(path);
            if (f.exists())
                FileUtil.deleteFile(f);
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }
}

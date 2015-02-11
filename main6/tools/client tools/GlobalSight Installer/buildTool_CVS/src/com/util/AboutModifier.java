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
package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AboutModifier
{
    private static Logger log = Logger.getLogger(AboutModifier.class);
    private static String srcPath = BuildUtil.ROOT
            + "/main6/envoy/src/java/properties/server.properties";
    private static String trgPath = "/GlobalSight/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/server.properties";
    
    public static void modifyAboutJsp(String root)
    {
        String version = PropertyUtil.get(new File("release.properties"),
                "newVersion");
        File f = new File(srcPath);
        Properties property = new Properties();
        
        try
        {
            if (f.exists())
            {
                property.load(new FileInputStream(f));
            }
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
        
        property.setProperty("version", version);
        try
        {
            File trg = new File(root + trgPath);
            if (!trg.getParentFile().exists())
            {
                trg.getParentFile().mkdirs();
            }
            property.store(new FileWriter(trg), "Update version to " + version);
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
    }
}

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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.Main;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PatchUtil;
import com.util.ServerUtil;
import com.util.UpgradeUtil;

public class PlugUtil 
{
    private static Logger log = Logger.getLogger(PlugUtil.class);
    
    private static final String DEPLOY_PATH = "/jboss/server/standalone/deployments";
    private static final String PROPERTIES_PATH = DEPLOY_PATH
        + "/globalsight.ear/lib/classes/properties/";

    private static String getPath()
    {
    	if (Main.isPatch())
    	{
    		PatchUtil util = new PatchUtil();
    		return util.getPath();
    	}
    	else
    	{
    		return UpgradeUtil.newInstance().getPath();
    	}
    }
    
    public static void copyPropertiesToCompany(List<String> names)
	{
    	copyPropertiesToCompany(names, false);
	}
    
	public static void copyPropertiesToCompany(List<String> names, boolean ignoreError)
	{
		String root = getPath() + PROPERTIES_PATH;
        String copyRoot = ServerUtil.getPath() + PROPERTIES_PATH;
        
        File pRoot = new File (copyRoot);
        File[] files = pRoot.listFiles();
        List<String> companys = new ArrayList<String>();
        
        for (File f : files)
        {
            if (f.isDirectory())
            {
                String name = f.getName();
                if (!"tm".equals(name) && !"aligner".equals(name))
                {
                    companys.add(name);
                }
            }
        }
        
        for (String name : names)
        {
            File file = new File (root + name);
            try
            {
                FileUtil.copyFile(file, new File (copyRoot + name));
            }
            catch (Exception e)
            {
            	if (!ignoreError)
            	{
                    log.error(e.getMessage(), e);
                    UI ui = UIFactory.getUI();
                    ui.confirmContinue("Failed to copy " + name);
            	}
            }
            
            for (String company : companys)
            {
                try
                {
                    FileUtil.copyFile(file, new File (copyRoot + company + "/" + name));
                }
                catch (Exception e)
                {
                	if (!ignoreError)
                	{
                        log.error(e.getMessage(), e);
                        UI ui = UIFactory.getUI();
                        ui.confirmContinue("Failed to copy " + name + " for company " + company);
                	}
                }
            }
        }
	}
	
	public static void copyProperties(List<String> names)
	{
		copyProperties(names, false);
	}
    
	public static void copyProperties(List<String> names, boolean ignoreError)
	{
		String root = getPath() + PROPERTIES_PATH;
        String copyRoot = ServerUtil.getPath() + PROPERTIES_PATH;
        
        for (String name : names)
        {
            File file = new File (root + name);
            try
            {
                FileUtil.copyFile(file, new File (copyRoot + name));
            }
            catch (Exception e)
            {
            	if (!ignoreError)
            	{
                    log.error(e.getMessage(), e);
                    UI ui = UIFactory.getUI();
                    ui.confirmContinue("Failed to copy " + name);
            	}
            }
        }
	}
}

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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.Main;
import com.config.properties.EnvoyConfig;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

public class Plug_8_1_1 implements Plug
{
	private static Logger log = Logger.getLogger(Plug_8_1_1.class);
    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
        + "default/deploy";
    private static final String WEB_TEMPLATE_PATH = DEPLOY_PATH
    + "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml.template";
	
    @Override
    public void run()
    {
    	updateProperties();
    	removeCoffeeTableAWTJar();
    }

    public void removeCoffeeTableAWTJar()
    {
    	log.info("Start to delete /applet/lib/CoffeeTableAWT.jar");
		File file = new File(ServerUtil.getPath()
				+ "/jboss/jboss_server/server/default/deploy/globalsight.ear"
				+ "/globalsight-web.war/applet/lib/CoffeeTableAWT.jar");
		if (file.exists())
		{
			boolean isRemoved = file.delete();
			if (!isRemoved)
			{
				log.info("Failed to remove /applet/lib/CoffeeTableAWT.jar");
			}
			else
			{
				log.info("/applet/lib/CoffeeTableAWT.jar has been removed");
			}
		}
		else
		{
			log.info("CoffeeTableAWT.jar is not exist");
		}
		
		log.info("Start to delete /globalsight.ear/lib/CoffeeTableAWT.jar");
		file = new File(ServerUtil.getPath()
				+ "/jboss/jboss_server/server/default/deploy/globalsight.ear"
				+ "/lib/CoffeeTableAWT.jar");
		if (file.exists())
		{
			boolean isRemoved = file.delete();
			if (!isRemoved)
			{
				log.info("Failed to remove /globalsight.ear/lib/CoffeeTableAWT.jar");
			}
			else
			{
				log.info("/globalsight.ear/lib/CoffeeTableAWT.jar has been removed");
			}
		}
		else
		{
			log.info("/globalsight.ear/lib/CoffeeTableAWT.jar is not exist");
		}
    }
    
    private void updateProperties()
    {
        List<String> files = new ArrayList<String>();
        files.add("MSDocxXmlRule.properties");
        PlugUtil.copyPropertiesToCompany(files);
        
        files = new ArrayList<String>();
        files.add("AdobeAdapter.properties");
        PlugUtil.copyProperties(files);
        
        updateEnvoyProperties();
        
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + WEB_TEMPLATE_PATH));
        templates.add(new File(ServerUtil.getPath() + "/jboss/startJboss.cmd.template"));
        Main.getInstallUtil().parseTemplates(templates);
    }
    
    private void updateEnvoyProperties()
    {
    	File f = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT + "/envoy.properties");
        if (!f.exists())
        { 
        	String s = f.getPath() + " is not exist";
        	 log.error(s);
             UI ui = UIFactory.getUI();
             ui.confirmContinue(s);
        }
        
        String content = FileUtil.readFile(f);
        content = content.replace("AdobeAdapter.properties,", "");
		try 
		{
			FileUtil.writeFile(f, content);
		} 
		catch (IOException e) 
		{
			 log.error(e.getMessage(), e);
             UI ui = UIFactory.getUI();
             ui.confirmContinue("Failed to update envoy.properties");
		}
    }
}

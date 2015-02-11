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

import com.Main;
import com.util.ServerUtil;

public class Plug_7_1_8_2 implements Plug
{
	private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
			+ "default/deploy";
	private static final String WEB_TEMPLATE_PATH = DEPLOY_PATH
			+ "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml.template";
	
	private static final String ENVOY_PATH = DEPLOY_PATH
			+ "/globalsight.ear/lib/classes/properties/envoy.properties";

    @Override
    public void run()
    {
    	addProperties();
    	parseTemplates();
    }
   
    private void addProperties()
    {
    	List<String> files = new ArrayList<String>();
    	files.add("MSXlsxXmlRule.properties");
    	files.add("MSDocxXmlRule.properties");
    	files.add("MSPptxXmlRule.properties");
    	files.add("ResxRule.properties");
    	PlugUtil.copyPropertiesToCompany(files);
    	
    	files.clear();
    	files.add("MSCommentXmlRule.properties");
    	files.add("MSHeaderXmlRule.properties");
    	PlugUtil.copyPropertiesToCompany(files, true);
    	
    }
    
    private void parseTemplates()
    {
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + WEB_TEMPLATE_PATH));
        Main.getInstallUtil().parseTemplates(templates);
    }
}

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.Main;
import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_8_2 implements Plug
{
	private static Logger log = Logger.getLogger(Plug_8_2.class);
	
    @Override
    public void run()
    {
    	updateProperties();
    	deleteSaaj();
    	deleteLog4jJar();
    	updateWrapperConf();
    	deleteLog4jTemplate();
    	renameLog4jProperties();
    	renameLog4jXml();
    }

    private void updateProperties()
    { 
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + "/jboss/jboss_server/server/default/deploy/globalsight.ear/lib/classes/properties/SRX2.0.xsd.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/mysql/create_cap_mysql.sql.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/mysql/insert_system_parameters_mysql.sql.template"));
        templates.add(new File(ServerUtil.getPath() + "/jboss/jboss-service.xml.template"));
        templates.add(new File(ServerUtil.getPath() + "/jboss/startJboss.cmd.template"));
        Main.getInstallUtil().parseTemplates(templates);
    }
    
	private boolean isExist(String content, String regex)
	{
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		return m.find();
	}
    
    private void updateWrapperConf()
    {
    	File f = new File(ServerUtil.getPath() + "/install/JavaServiceWrapper/conf/wrapper.conf");
    	if (f.exists())
    	{
    		File bacF = new File(ServerUtil.getPath() + "/install/JavaServiceWrapper/conf/wrapper.conf.bak");
    		if (!bacF.exists())
    		{
    			try 
    			{
					FileUtil.copyFile(f, bacF);
					log.info("Backing up wrapper.conf to wrapper.conf.bak");
				} 
    			catch (Exception e) 
				{
					log.error(e.getMessage(), e);
				}
    		}
    		
			String content = FileUtil.readFile(f);
			if (content.indexOf("# GBS-2166:") < 1) 
			{
				StringBuffer s = new StringBuffer();
				int i = 1;
				if (!isExist(content, "wrapper\\.java\\.additional\\..*=\\s*-Xss512k"))
				{
					while (isExist(content, "wrapper\\.java\\.additional\\." + i + "\\s*="))
					{
						i++;
					}
					
					s.append("wrapper.java.additional." + i + "=-Xss512k"
							+ FileUtil.lineSeparator);
					
					i++;
				}
				
				if (!isExist(content, "wrapper\\.java\\.additional\\..*=\\s*-Dorg\\.jboss\\.logging\\.Log4jService\\.catchSystemOut="))
				{
					while (isExist(content, "wrapper\\.java\\.additional\\." + i + "\\s*="))
					{
						i++;
					}
					
					s.append("wrapper.java.additional." + i + "=-Dorg.jboss.logging.Log4jService.catchSystemOut=false"
							+ FileUtil.lineSeparator);
					
					i++;
				}
				
				if (!isExist(content, "wrapper\\.java\\.additional\\..*=\\s*-Dorg\\.jboss\\.logging\\.Log4jService\\.catchSystemErr="))
				{
					while (isExist(content, "wrapper\\.java\\.additional\\." + i + "\\s*="))
					{
						i++;
					}
					
					s.append("wrapper.java.additional." + i + "=-Dorg.jboss.logging.Log4jService.catchSystemErr=false"
							+ FileUtil.lineSeparator);
					
					i++;
				}
				
				if (!isExist(content, "wrapper\\.java\\.additional\\..*=\\s*-Dcom\\.sun\\.management\\.jmxremote"))
				{
					while (isExist(content, "wrapper\\.java\\.additional\\." + i + "\\s*="))
					{
						i++;
					}
					
					s.append("# GBS-2166: start JMX agent early; if loaded later it will fail"
							+ FileUtil.lineSeparator);
					s.append("wrapper.java.additional." + i + "=-Dcom.sun.management.jmxremote"
							+ FileUtil.lineSeparator);
					
					i++;
				}

				s.append(FileUtil.lineSeparator);
				s.append("# Initial Java Heap Size (in MB)");

				content = content.replace(FileUtil.lineSeparator + "# Initial Java Heap Size (in MB)",
						s.toString());

				try 
				{
					FileUtil.writeFile(f, content);
				} 
				catch (IOException e) 
				{
					log.error(e.getMessage(), e);
				}
			}
		}
    }
    
	private void deleteLog4jTemplate() 
	{
		List<File> fs = FileUtil.getAllFiles(new File(ServerUtil.getPath()
				+ "/jboss/jboss_server/server/default/deploy/globalsight.ear"),
				new FileFilter() 
		{
					@Override
					public boolean accept(File pathname) 
					{
						return "log4j.properties.template"
								.equalsIgnoreCase(pathname.getName());
					}
				});

		for (File f : fs) 
		{
			if (!f.delete()) 
			{
				log.warn("Can not delete the file: " + f.getAbsolutePath());
			}
		}
	}
    
    private void deleteLog4jJar()
    {
		List<File> fs = FileUtil.getAllFiles(new File(ServerUtil.getPath()
				+ "/jboss/jboss_server/server/default/deploy/globalsight.ear"),
				new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return "log4j.jar".equalsIgnoreCase(pathname.getName());
					}
				});
    	
    	for (File f : fs)
    	{
    		if (!f.delete())
    		{
    			log.warn("Can not delete the file: " + f.getAbsolutePath());
    		}
    	}
    }
    
    private void renameLog4jProperties()
    {
    	File f = new File(ServerUtil.getPath() + "/jboss/log4j.properties");
    	if (f.exists())
    	{
    		f.delete();
    	}
    	
    	f = new File(ServerUtil.getPath() + "/jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/WEB-INF/classes/log4j.properties");
    	if (f.exists())
    	{
    		File dest = new File(f.getAbsolutePath() + ".unused");
    		f.renameTo(dest);
    	}
    }
    
    private void renameLog4jXml()
    {
    	File f = new File(ServerUtil.getPath() + "/jboss/jboss_server/server/default/conf/jboss-log4j.xml");
    	if (f.exists())
    	{
    		File dest = new File(f.getAbsolutePath() + ".unused");
    		f.renameTo(dest);
    	}
    }
    
    
    private void deleteSaaj()
    {
    	File f = new File(ServerUtil.getPath() + "/install/data/axis/saaj.jar");
    	if (f.exists())
    	{
    		f.delete();
    	}
    }
}

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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JbossUpdater 
{
//	private static final String RUN_CONF_PATH = "/jboss/jboss_server/bin/run.conf";
	private static final String WRAPPER = "/install/JavaServiceWrapper/conf/wrapper.conf";
	private static final String BIN_PATH = "/jboss/server/bin";
	private static final String STANDALONE_CONF_PATH = BIN_PATH + "/standalone.conf";
	private static final String STANDALONE_CONF_BAT_PATH = BIN_PATH + "/standalone.conf.bat";
	private static Properties option = null;
	
	public static void updateJavaOptions() throws IOException
	{
		if (option != null)
		{
			String runConf = ServerUtil.getPath() + STANDALONE_CONF_BAT_PATH;
			File f = new File(runConf);
			
			String content = FileUtil.readFile(f);
			content = content
					.replace(
							"set \"JAVA_OPTS=-Xms1024m -Xmx1024m -XX:MaxPermSize=256m -Xss512k\"",
							MessageFormat
									.format("set \"JAVA_OPTS=-Xms{0} -Xmx{1} -XX:MaxPermSize={2} -Xss{3}\"",
											option.get("Xms"),
											option.get("Xmx"),
											option.get("XX"),
											option.get("Xss")));
			FileUtil.writeFile(f, content);
			
			String path = ServerUtil.getPath() + STANDALONE_CONF_PATH;
			f = new File(path);
			content = FileUtil.readFile(f);
			content = content
					.replace(
							"JAVA_OPTS=\"-Xms64m -Xmx512m -XX:MaxPermSize=256m ",
							MessageFormat
									.format("JAVA_OPTS=\"-Xms{0} -Xmx{1} -XX:MaxPermSize={2} ",
											option.get("Xms"),
											option.get("Xmx"),
											option.get("XX")));
			FileUtil.writeFile(f, content);
		}
	}
	
//	public static void readOptionsFromRunConf()
//	{
//		String runConf = ServerUtil.getPath() + RUN_CONF_PATH;
//		File f = new File(runConf);
//		
//		String content = FileUtil.readFile(f);
//		Pattern p = Pattern.compile("-Xms([^-]*) -Xmx([^-]*) -XX:MaxPermSize=([^-]*) -Xss([^-]*) ");
//		Matcher m = p.matcher(content);
//		
//		if (m.find())
//		{
//			option = new Properties();
//			
//			option.put("Xms", m.group(1));
//			option.put("Xmx", m.group(2));
//			option.put("XX", m.group(3));
//			option.put("Xss", m.group(4));
//		}
//	}
	
	public static void readOptionsFromWrapper()
	{
		String runConf = ServerUtil.getPath() + WRAPPER;
		File f = new File(runConf);
		
		String content = FileUtil.readFile(f);
		
		option = new Properties();
		
		Pattern p = Pattern.compile("-Xms([\\d]*[\\D])");
		Matcher m = p.matcher(content);
		
		if (m.find())
		{
			option.put("Xms", m.group(1));
		}
		
		p = Pattern.compile("-Xmx([\\d]*[\\D])");
		m = p.matcher(content);
		
		if (m.find())
		{
			option.put("Xmx", m.group(1));
		}
		
		p = Pattern.compile("-XX:MaxPermSize=([\\d]*[\\D])");
		m = p.matcher(content);
		
		if (m.find())
		{
			option.put("XX", m.group(1));
		}
		
		p = Pattern.compile("-Xss([\\d]*[\\D])");
		m = p.matcher(content);
		
		if (m.find())
		{
			option.put("Xss", m.group(1));
		}
		
		if (option.size() != 4)
		{
			option = null;
		}
	}
}

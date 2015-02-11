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

package com.globalsight.cxe.adapter.adobe;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfiguration;

public class InddRuleHelper
{
	private static final Logger logger = Logger.getLogger(InddRuleHelper.class);

	private static String defRule;

	static
	{
		defRule = "";
	}

	public static boolean isIndd(String str)
	{
		if (str != null)
		{
		    return str.startsWith("indd") || str.startsWith("inx");
		}
		
		return false;
	}

	public static String loadRule()
	{
		String fileName = SystemConfiguration
				.getCompanyResourcePath("/properties/inddrule.properties");
		String rule = null;
		try
		{
			rule = FileUtils.read(InddRuleHelper.class
					.getResourceAsStream(fileName));
			if (logger.isDebugEnabled())
			{
				logger.debug("indd rule file loaded:\n" + rule + "\n");
			}
		}
		catch (Exception e)
		{
			logger.error("file not found:\n" + fileName, e);
		}
		if (rule == null)
		{
			return defRule;
		}
		return rule;
	}

	public static String loadAdobeXmpRule()
	{
		String fileName = SystemConfiguration
				.getCompanyResourcePath("/properties/AdobeXmpRule.properties");
		String rule = null;
		try
		{
			// load company xmp rule
			rule = FileUtils.read(InddRuleHelper.class
					.getResourceAsStream(fileName));
			if (logger.isDebugEnabled())
			{
				logger.debug("indd rule file loaded:\n" + rule + "\n");
			}
		}
		catch (Exception e)
		{
			StringBuffer sb = new StringBuffer("Error when loading indd rules :\n");
			sb.append(fileName);
			logger.error(sb.toString(), e);
		}
		if (rule == null)
		{
			return defRule;
		}
		return rule;
	}
}
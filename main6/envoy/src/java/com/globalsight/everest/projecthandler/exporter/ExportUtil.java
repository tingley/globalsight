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

package com.globalsight.everest.projecthandler.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.IExportManager;

import com.globalsight.everest.util.system.SystemConfiguration;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportUtil
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ExportUtil.class);

    public static String EXPORT_BASE_DIRECTORY = "/";
    public static Properties WHITE_SPACE_EXPORT = new Properties();
    static {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            EXPORT_BASE_DIRECTORY = root + IExportManager.EXPORT_DIRECTORY;

            if (!(EXPORT_BASE_DIRECTORY.endsWith("/") ||
                  EXPORT_BASE_DIRECTORY.endsWith("\\")))
            {
                EXPORT_BASE_DIRECTORY = EXPORT_BASE_DIRECTORY + "/";
            }

            File temp = new File(EXPORT_BASE_DIRECTORY);
            temp.mkdirs();
            
            WHITE_SPACE_EXPORT.load(ExportUtil.class
        			.getResourceAsStream("/properties/WhitespaceForExport.properties"));
        }
        catch (Throwable e)
        {
            CATEGORY.error(
                "cannot create directory " + EXPORT_BASE_DIRECTORY, e);
        }
    }

    /** Static class, private constructor. */
    private ExportUtil ()
    {
    }

    //
    // Public Methods
    //

    static public String getExportDirectory()
    {
        return EXPORT_BASE_DIRECTORY;
    }
    
    public static String replaceWhitespace(String s, String locale)
    {
    	try 
    	{
			s = replaceLeadingWhitespace(s, locale);
			s = replaceTrailingWhitespace(s, locale);
		} catch (Exception e) 
		{
			CATEGORY.warn(e);
		}
    	
    	return s;
    }
    
    private static String replaceLeadingWhitespace(String s, String locale) throws Exception
	{
		Pattern p = Pattern.compile("<skeleton>(\\s*)</skeleton>([\\r\\n]*<translatable [^>]*>)");
		Matcher m = p.matcher(s);
		String[] rules = null;
		while (m.find())
		{
			if (rules == null)
			{
				rules = getRules(locale);
			}
			
			String all = m.group();

			String s1 = rules[0].substring(1, rules[0].length() - 1);
			String s2 = rules[1].substring(1, rules[1].length() - 1);
			
			String sk = m.group(1);
			sk = sk.replace(s1, s2);
			
			String newString = "<skeleton>" + sk + "</skeleton>" + m.group(2);
			s = s.replace(all, newString);
		}
		
		return s;
	}
	
	private static String replaceTrailingWhitespace(String s, String locale) throws Exception
	{
		Pattern p = Pattern.compile("(</translatable>[\\r\\n]*)<skeleton>(\\s*)</skeleton>");
		Matcher m = p.matcher(s);
		String[] rules = null;
		while (m.find())
		{
			if (rules == null)
			{
				rules = getRules(locale);
			}
			
			String all = m.group();

			String s1 = rules[0].substring(1, rules[0].length() - 1);
			String s2 = rules[1].substring(1, rules[1].length() - 1);
			
			String sk = m.group(2);
			sk = sk.replace(s1, s2);
			
			String newString = m.group(1) + "<skeleton>" + sk + "</skeleton>";
			s = s.replace(all, newString);
		}
		
		return s;
	}
	
	private static String[] getRules(String locale) throws Exception
	{
		String rule = null;
		for (Object ob : WHITE_SPACE_EXPORT.keySet())
		{
			String key = (String) ob;
			String key2 = key.replace("*", ".*");
			if (locale.matches(key2))
			{
				rule = WHITE_SPACE_EXPORT.getProperty(key);
				break;
			}
		}
		
		if (rule != null)
		{
			return rule.split(":");
		}
		
		return null;
	}
}

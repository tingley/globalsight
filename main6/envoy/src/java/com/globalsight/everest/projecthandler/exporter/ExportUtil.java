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

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.exporter.IExportManager;
import com.globalsight.util.Replacer;
import com.globalsight.util.StringUtil;

public class ExportUtil
{
    private static final Logger CATEGORY = Logger.getLogger(ExportUtil.class);

    public static String EXPORT_BASE_DIRECTORY = "/";
    public static Properties WHITE_SPACE_EXPORT = new Properties();
    
    private static Pattern SKELETON_PATTERN = Pattern
            .compile("<skeleton>(\\s*)</skeleton>([\\r\\n]*<translatable [^>]*>)");
    private static Pattern SKELETON2_PATTERN = Pattern
            .compile("(</translatable>[\\r\\n]*)<skeleton>(\\s*)</skeleton>");
    
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc
                    .getStringParameter(SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            EXPORT_BASE_DIRECTORY = root + IExportManager.EXPORT_DIRECTORY;

            if (!(EXPORT_BASE_DIRECTORY.endsWith("/") || EXPORT_BASE_DIRECTORY
                    .endsWith("\\")))
            {
                EXPORT_BASE_DIRECTORY = EXPORT_BASE_DIRECTORY + "/";
            }

            File temp = new File(EXPORT_BASE_DIRECTORY);
            temp.mkdirs();

            WHITE_SPACE_EXPORT
                    .load(ExportUtil.class
                            .getResourceAsStream("/properties/WhitespaceForExport.properties"));
        }
        catch (Throwable e)
        {
            CATEGORY.error("cannot create directory " + EXPORT_BASE_DIRECTORY,
                    e);
        }
    }

    /** Static class, private constructor. */
    private ExportUtil()
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
            String[] rules = getRules(locale);
            
            if (rules != null)
            {
                s = replaceLeadingWhitespace(s, rules);
                s = replaceTrailingWhitespace(s, rules);
            }
        }
        catch (Exception e)
        {
            CATEGORY.warn(e);
        }

        return s;
    }

    private static String replaceLeadingWhitespace(String s, String[] rules)
            throws Exception
    {
        s = StringUtil.replaceWithRE(s, SKELETON_PATTERN, new Replacer(rules[0], rules[1]) 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
	            String s1 = r1.substring(1, r1.length() - 1);
	            String s2 = r2.substring(1, r2.length() - 1);

	            String sk = m.group(1);
	            sk = StringUtil.replace(sk, s1, s2);

	            String newString = "<skeleton>" + sk + "</skeleton>" + m.group(2);
				return newString;
			}
		});
        
        return s;
    }

    private static String replaceTrailingWhitespace(String s, String[] rules)
            throws Exception
    {
        s = StringUtil.replaceWithRE(s, SKELETON2_PATTERN, new Replacer(rules[0], rules[1]) 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
	            String s1 = r1.substring(1, r1.length() - 1);
	            String s2 = r2.substring(1, r2.length() - 1);

	            String sk = m.group(2);
	            sk = StringUtil.replace(sk, s1, s2);

	            String newString = m.group(1) + "<skeleton>" + sk + "</skeleton>";
				return newString;
			}
		});
        
        return s;
    }

    private static String[] getRules(String locale) throws Exception
    {
        String rule = null;
        for (Object ob : WHITE_SPACE_EXPORT.keySet())
        {
            String key = (String) ob;
            String key2 = StringUtil.replace(key, "*", ".*");
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

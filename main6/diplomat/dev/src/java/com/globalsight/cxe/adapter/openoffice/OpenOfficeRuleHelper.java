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

package com.globalsight.cxe.adapter.openoffice;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.ling.docproc.IFormatNames;

public class OpenOfficeRuleHelper
{
    private static final Logger logger = Logger
            .getLogger(OpenOfficeRuleHelper.class);

    private static String defaultRule;

    static
    {
        defaultRule = "";
    }

    public static boolean isOpenOffice(String str)
    {
        if (str != null)
        {
            return str.startsWith(IFormatNames.FORMAT_OPENOFFICE_XML)
                    || str.startsWith("openoffice");
        }

        return false;
    }

    public static String loadRule(String p_displayName)
    {
        String dname = p_displayName.toLowerCase();
        String fileName = "";

        if (dname.endsWith(".odp"))
        {
            fileName = "/properties/OdpXmlRule.properties";
        }
        else if (dname.endsWith(".ods"))
        {
            fileName = "/properties/OdsXmlRule.properties";
        }
        else
        // defalult is odt
        {
            fileName = "/properties/OdtXmlRule.properties";
        }

        String rule = null;
        try
        {
            rule = FileUtils.read(OpenOfficeRuleHelper.class
                    .getResourceAsStream(fileName));
            if (logger.isDebugEnabled())
            {
                logger.debug("openoffice rule file loaded:\n" + rule + "\n");
            }
        }
        catch (Exception e)
        {
            StringBuffer sb = new StringBuffer(
                    "Error when loading openoffice rules :\n");
            sb.append(fileName);
            logger.error(sb.toString(), e);
        }

        if (rule == null)
        {
            return defaultRule;
        }
        return rule;
    }

    public static String loadStylesRule()
    {
        String fileName = "/properties/OdStylesXmlRule.properties";
        String rule = null;
        try
        {
            // load company xmp rule
            rule = FileUtils.read(OpenOfficeRuleHelper.class
                    .getResourceAsStream(fileName));
            if (logger.isDebugEnabled())
            {
                logger.debug("openoffice styles rule file loaded:\n" + rule
                        + "\n");
            }
        }
        catch (Exception e)
        {
            StringBuffer sb = new StringBuffer(
                    "Error when loading openoffice rules :\n");
            sb.append(fileName);
            logger.error(sb.toString(), e);
        }

        if (rule == null)
        {
            return defaultRule;
        }
        return rule;
    }
}
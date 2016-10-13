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

package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.ling.docproc.IFormatNames;

public class OfficeXmlRuleHelper
{
    private static final Logger logger = Logger
            .getLogger(OfficeXmlRuleHelper.class);

    private static String defaultRule;

    static
    {
        defaultRule = "";
    }

    public static boolean isOfficeXml(String str)
    {
        if (str != null)
        {
            return str.startsWith(IFormatNames.FORMAT_OFFICE_XML);
        }

        return false;
    }

    public static String loadRule(String p_displayName, int docPageCount)
    {
        String fileName = null;

        if (p_displayName.startsWith(OfficeXmlHelper.DNAME_PRE_DOCX_COMMENT))
        {
            fileName = "/properties/MSDocxXmlRule.properties";
        }
        else if (p_displayName.startsWith("(sheet")
                || p_displayName
                        .startsWith(OfficeXmlHelper.DNAME_PRE_XLSX_SHARED)
                || p_displayName
                        .startsWith(OfficeXmlHelper.DNAME_PRE_XLSX_SHEET_NAME))
        {
            fileName = "/properties/MSXlsxXmlRule.properties";
        }
        else if (p_displayName.startsWith("(presentation")
                || p_displayName.startsWith("(slide")
                || p_displayName
                        .startsWith(OfficeXmlHelper.DNAME_PRE_PPTX_DIAGRAM))
        {
            fileName = "/properties/MSPptxXmlRule.properties";
        }
        else
        {
            String dname = p_displayName.toLowerCase();

            if (dname.endsWith(".xlsx"))
            {
                fileName = "/properties/MSXlsxXmlRule.properties";
            }
            else if (dname.endsWith(".pptx"))
            {
                fileName = "/properties/MSPptxXmlRule.properties";
            }
            else if (dname.endsWith(".docx"))
            {
                fileName = "/properties/MSDocxXmlRule.properties";
            }
        }

        String file = null;
        String rule = null;
        if (fileName != null)
        {
            try
            {
                URL url = OfficeXmlRuleHelper.class.getResource(fileName);
                File theFile = null;

                if (url != null)
                {
                    try
                    {
                        theFile = new File(url.toURI());
                    }
                    catch (Exception exx)
                    {
                        theFile = new File(url.getPath());
                    }
                }

                if (theFile != null && theFile.exists())
                {
                    rule = FileUtils.read(theFile);
                }
                else
                {
                    rule = FileUtils.read(OfficeXmlRuleHelper.class
                            .getResourceAsStream(fileName));
                }
            }
            catch (Exception e)
            {
                StringBuffer sb = new StringBuffer(
                        "Error when loading office (xml) rules :\n");
                sb.append(fileName);
                logger.error(sb.toString(), e);
            }
        }
        else
        {
            rule = loadDefaultRule(p_displayName);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("office (xml) rule file loaded:\n" + rule + "\n");
        }

        return rule;
    }

    private static String loadDefaultRule(String displayName)
    {
        return defaultRule;
    }
}
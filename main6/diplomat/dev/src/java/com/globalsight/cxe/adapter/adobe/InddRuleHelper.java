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

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;

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
        String rule = loadRuleFile("/properties/inddrule.properties");

        if (logger.isDebugEnabled())
        {
            logger.debug("indd rule file loaded:\n" + rule + "\n");
        }

        return rule;
    }

    private static String loadRuleFile(String file)
    {
        String rule = null;
        try
        {

            URL url = InddRuleHelper.class.getResource(file);
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
                rule = FileUtils.read(InddRuleHelper.class
                        .getResourceAsStream(file));
            }
        }
        catch (Exception e)
        {
            logger.error("Error when load file :\n" + file, e);
        }

        if (rule == null)
        {
            return defRule;
        }

        return rule;
    }

    public static String loadAdobeXmpRule()
    {
        String rule = loadRuleFile("/properties/AdobeXmpRule.properties");

        if (logger.isDebugEnabled())
        {
            logger.debug("indd rule file loaded:\n" + rule + "\n");
        }

        return rule;
    }
}
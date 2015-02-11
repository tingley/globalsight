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

package com.globalsight.cxe.adapter.idml;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;

public class IdmlRuleHelper
{
    private static final Logger logger = Logger.getLogger(IdmlRuleHelper.class);

    private static String defRule;

    static
    {
        defRule = "";
    }

    public static boolean isIdml(String event)
    {
        return "IDML_IMPORTED_EVENT".equalsIgnoreCase(event);
    }

    public static String loadRule()
    {
        String fileName = "/properties/idmlrule.properties";

        String rule = null;
        try
        {
            rule = FileUtils.read(IdmlRuleHelper.class
                    .getResourceAsStream(fileName));
            if (logger.isDebugEnabled())
            {
                logger.debug("idml rule file loaded:\n" + rule + "\n");
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
}
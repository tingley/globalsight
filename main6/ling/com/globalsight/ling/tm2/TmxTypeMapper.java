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
package com.globalsight.ling.tm2;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.ling.tm2.leverage.TmxTagStatistics;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicHandler;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Tmx inline tag type mapping class
 */

public class TmxTypeMapper
{
    private static Logger c_logger =
        Logger.getLogger(
            TmxTypeMapper.class.getName());

    // tag type mapping
    private static final Map c_tmxTagMapping = getTmxTagMapping();

    private static Map getTmxTagMapping()
    {
        Map tagMapping = new HashMap();
        
        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle("properties/tm/TmxTypeMapper");
            
            Enumeration e = res.getKeys();
            while(e.hasMoreElements())
            {
                String key = (String)e.nextElement();
                String value = res.getString(key);
                tagMapping.put(key, value);
            }
        }
        catch (MissingResourceException e)
        {
            c_logger.warn("Can't find TmxTypeMapper.properties.");
        }
        
        return tagMapping;
    }
        

    public static String normalizeType(String p_type)
    {
        String normalizedType = (String)c_tmxTagMapping.get(p_type);
        if(normalizedType == null)
        {
            normalizedType = p_type;
        }
        
        return normalizedType;
    }

}

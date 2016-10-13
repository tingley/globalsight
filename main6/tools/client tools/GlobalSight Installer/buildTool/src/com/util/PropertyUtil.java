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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A util class, provide some methods to read values from properties file.<br>
 * For example, If there is propertis file (example.properties), and the content
 * is "key1=value1 key2=value2", you can use
 * PropertyUtil.get("example.properties", "key1") to get the String "value1".
 */
public class PropertyUtil
{
    private static Logger log = Logger.getLogger(PropertyUtil.class);
    private static Map<String, Properties> PROPERTIES = new HashMap<String, Properties>();

    /**
     * Gets the value of the key from the specified properties file.
     * 
     * @param file
     *            The specified properties file, can not be null, and must
     *            exist.
     * @param key
     *            The key of value you want to get.
     * @return The value, matching the <code>key</code>.
     */
    public static String get(File file, String key)
    {
        FileInputStream in = null;
        try
        {
            Properties properties = PROPERTIES.get(file.getCanonicalPath());
            if (properties == null)
            {
                properties = new Properties();
                in = new FileInputStream(file);
                properties.load(in);
                PROPERTIES.put(file.getCanonicalPath(), properties);
            }

            return properties.getProperty(key);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return null;
    }

    /**
     * Gets the value of the key from the specified properties file.
     * 
     * @param filePath
     *            The path of specified properties file, can not be null, and
     *            must exist.
     * @param key
     *            The key of value you want to get.
     * @return The value, matching the <code>key</code>.
     */
    public static String get(String filePath, String key)
    {
        return get(new File(filePath), key);
    }
}

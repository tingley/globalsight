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
package com;

import java.io.File;

import org.apache.log4j.Logger;

import com.util.BuildUtil;
import com.util.PropertyUtil;

public class Main
{
    private static Logger log = Logger.getLogger(Main.class);

    public static boolean IS_PATCH = "Y".equalsIgnoreCase(PropertyUtil.get(
            new File("release.properties"), "isPatch"));

    private static Boolean IN_LINUX = null;

    /**
     * @param args
     * 
     */
    public static void main(String[] args)
    {
        try
        {
        	
        	if (BuildUtil.VERSION.startsWith("8.3"))
        	{
        		IS_PATCH = false;
        	}
        	
            BuildUtil.build();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Judge the system is linux or not.
     * 
     * @return The system is linux or not.
     */
    public static boolean isInLinux()
    {
        if (IN_LINUX == null)
        {
            String os = System.getProperty("os.name");
            log.info("System: " + os);
            if (os.startsWith("Win"))
            {
                IN_LINUX = Boolean.FALSE;
            }
            else if (os.startsWith("Linux"))
            {
                IN_LINUX = Boolean.TRUE;
            }
            else
            {
                IN_LINUX = Boolean.FALSE;
                throw new IllegalStateException("Unsupported OS: " + os);
            }
        }

        return IN_LINUX.booleanValue();
    }
}

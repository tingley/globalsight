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

import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
import com.config.xml.model.SystemInfo;
import com.ui.PatchTreeModel;

/**
 * A util class, provids some methos to get informations (name, path, version,
 * and so on) about Globalsight server.
 */
public class ServerUtil
{
    private static String VERSION;
    private static String PATH;
    private static Boolean IN_LINUX = null;
    
    private static final String TEST_PATH = "install/data";   
    public static final String SERVER_NAME_PRUFIX = "GlobalSight";
    private static SystemInfo SYSTEM_INFO;
    
    private static Logger log = Logger.getLogger(ServerUtil.class);

    /**
     * <p>
     * Gets server path. The following is work flow.<br>
     * <br>
     * 1. Try to split server path from current path.<br>
     * 2. If 1 failed, try to get server path from installValues.properties.<br>
     * 3. If 2 failed, force user input the path.
     * 
     * @return The path of server, it will not be null.
     */
    public static String getPath()
    {
        return PATH;
    }

    public static void initPath()
    {

        PATH = InstallValues.getServerPath();

        if (PATH != null)
        {
        	log.info("Server: " + PATH);
        }
    }

    public static void setPath(String path) {
    	
    	if (path != null && isServerPath(path) && !path.equals(PATH))
    	{
    		PATH = path;
    		PatchTreeModel.loadHotfix();
    	}
	}

    /**
     * Gets the name of server, it is splited from server path.
     * 
     * @return The name of server.
     */
    public static String getServerName()
    {
        String path = getPath();
        Assert.assertNotNull(path, "Server path");
        int index = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
        return path.substring(index + 1);
    }

    /**
     * Validates the <code>path</code> is really the server path. The judge
     * standard is that the fold (<code>path</code>/install) is exist.
     * 
     * @param path
     *            The path to validate.
     * @return is the server path or not.
     */
    public static boolean isServerPath(String path)
    {
        path = path + File.separator + TEST_PATH;
        File testFile = new File(path);

        return testFile.exists();
    }

    /**
     * Gets the version of the server. It is readed from
     * <code>version.properties</code>, you can get more informations from
     * <class>CommonConfig.getVersionFilePath()</class> about it.
     * 
     * @return
     */
    public static String getVersion()
    {
        if (VERSION == null)
        {
            VERSION = getSystemInfo().getVersion();
            log.info("System version: " + VERSION);
        }

        return VERSION;
    }

    /**
     * Load <code>systemInfo</code> from the system.xml in server.
     * 
     * @see com.config.xml.model.SystemInfo.
     * @see com.util.XmlUtil#load(Class, String)
     * @return The loaded <code>systemInfo</code>.
     */
    public static SystemInfo getSystemInfo()
    {
        if (SYSTEM_INFO == null)
        {
            String path = getPath() + File.separator + SystemInfo.XML_PATH;
            if (new File(path).exists())
            {
                SYSTEM_INFO = XmlUtil.load(SystemInfo.class, path);
            }
            else
            {
            	log.info("Can't find " + path);
                SYSTEM_INFO = new SystemInfo();
            }
        }

        return SYSTEM_INFO;
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

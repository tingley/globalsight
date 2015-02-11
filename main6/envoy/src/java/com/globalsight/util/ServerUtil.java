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

package com.globalsight.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.config.SystemParameter;
import com.globalsight.config.SystemParameterPersistenceManager;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

public class ServerUtil
{
    static private final Logger logger = Logger.getLogger(ServerUtil.class);

    private static String version = null;

    public static List<Hotfix> getInstalledPatches()
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String path = sc
                .getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY);
        path = path.replace("\\", "/");
        int index = path.indexOf("jboss/server");
        if (index > 0)
        {
            path = path.substring(0, index);
        }

        File f = new File(path + "/hotfix");
        List<Hotfix> hs = new ArrayList<Hotfix>();
        if (f.exists())
        {
            File[] fs = f.listFiles();
            for (File f1 : fs)
            {
                File f2 = new File(f1.getAbsolutePath() + "/config.xml");
                if (f2.exists())
                {
                    Hotfix h = XmlUtil.load(Hotfix.class, f2.getAbsolutePath());
                    String version = getVersion();
                    version = version.trim();
                    version = version.split(" ")[0];
                    if (h.getVersion().equals(version) && h.getInstalled())
                    {
                        hs.add(h);
                    }
                }
            }

            SortUtil.sort(hs, Hotfix.getComparator());
        }

        return hs;
    }

    public static String getVersion()
    {
        if (version == null)
        {

            Properties p = new Properties();

            try
            {
                p.load(ServerUtil.class
                        .getResourceAsStream("/properties/server.properties"));
                version = (String) p.get("version");
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }

        return version;
    }

    // Gets the Server Login URL from Database.
    public static String getServerURL()
    {
        String result;
        SystemConfiguration sc = SystemConfiguration.getInstance();
        if (sc.getBooleanParameter("cap.public.url.enable"))
        {
            result = sc.getStringParameter("cap.public.url");
        }
        else if (sc.getBooleanParameter("server.ssl.enable"))
        {
            result = sc.getStringParameter("cap.login.url.ssl");
        }
        else
        {
            result = sc
                    .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);
        }

        if (!result.endsWith("/"))
        {
            result += "/";
        }

        return result;
    }

    /**
     * Gets the system parameter "server.instance.id". The correct default value
     * is getServerURL().
     */
    public static String getServerInstanceID()
    {
        try
        {
            SystemParameterPersistenceManager manager = ServerProxy
                    .getSystemParameterPersistenceManager();
            SystemParameter sysParam = manager
                    .getAdminSystemParameter(SystemConfigParamNames.SERVER_INSTANCE_ID);
            if (sysParam != null
                    && sysParam.getValue().equals("GlobalSightInstanceID"))
            {
                sysParam.setValue(getServerURL());
                manager.updateAdminSystemParameter(sysParam);
            }
            return sysParam.getValue();
        }
        catch (Exception e)
        {
        }

        return getServerURL();
    }
}

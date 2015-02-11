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
package com.globalsight.util.j2ee.jboss;

import java.io.File;

import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

public class JBossWrapper extends AppServerWrapper
{

    private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(AppServerWrapper.class.getName());

    private static final String NAMING_SERVICE_PORT = "10999";

    public static final String USER_TRANSACTION = "UserTransaction";

    public JBossWrapper()
    {
        super();
        s_logger.info("Using JBoss as the J2EE Application Server");
    }

    public String getJ2EEServerName()
    {
        return AppServerWrapperFactory.JBOSS;
    }

    public String getUserTransactionString()
    {
        return USER_TRANSACTION;
    }

    public void shutdown()
    {
        try
        {
            System.out.println("Initiating shutdown of jboss server.");

            // first find out if the server is running from the command line
            // or as an NT service
            // String startupType = System.getProperty("globalsight.startup");
            // String command;
            // if ("NTservice".equals(startupType))
            // {
            // //user net stop GlobalSight to shut it down
            // command = "net stop \"GlobalSight\"";
            // }
            // else
            // {
            // running from command line so use shutdown to shut it down
            String os = System.getProperty("os.name");
            boolean isWindows = os.startsWith("Win");
            SystemConfiguration config = SystemConfiguration.getInstance();
            String ambHome = config
                    .getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY);
            StringBuffer c = new StringBuffer();
            if (!isWindows)
                c.append("/bin/sh ");
            c.append(ambHome.replace('/', File.separatorChar));
            c.append(File.separator);
            c.append("jboss");
            c.append(File.separator);
            c.append("jboss_server");
            c.append(File.separator);
            c.append("bin");
            c.append(File.separator);

            if (os.startsWith("Linux"))
                c.append("shutdown.sh");
            else
                c.append("shutdown.bat");

            c.append(" -S --server=localhost:");
            c.append(NAMING_SERVICE_PORT);
            String command = c.toString();
            // }
            s_logger.info("Executing command:" + command);
            ProcessRunner pr = new ProcessRunner(command, System.out,
                    System.err);
            Thread t = new Thread(pr);
            t.start();
            s_logger.info("Thread to stop jboss started.");
        }
        catch (Exception e)
        {
            s_logger.error("Failed to execute script to shutdown jboss server",
                    e);
        }

        // s_logger.info("Start to shut down JBoss server.");
        // String[] parameters = { "-S",
        // "--server=localhost:" + NAMING_SERVICE_PORT };
        // try
        // {
        // Shutdown.main(parameters);
        // }
        // catch (Exception e)
        // {
        // s_logger.error("Shut down JBoss failed.", e);
        // }
    }

    public ServerRegistry getServerRegistry() throws GeneralException
    {

        return JBossServerRegistry.getInstance();
    }
}

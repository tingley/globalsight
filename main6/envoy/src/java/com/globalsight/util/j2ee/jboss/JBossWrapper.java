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

import org.apache.log4j.Logger;

import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

public class JBossWrapper extends AppServerWrapper
{
    private static final Logger s_logger = Logger
            .getLogger(AppServerWrapper.class.getName());

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
            s_logger.info("Shutting down jboss server.");

            String os = System.getProperty("os.name");
            boolean isWindows = os.startsWith("Win");
            StringBuilder c = new StringBuilder();
            if (isWindows)
            {
                c.append("jboss-cli.bat");
            }
            else
            {
                c.append("jboss-cli.sh");
            }
            c.append(" --connect command=:shutdown");

            SystemConfiguration config = SystemConfiguration.getInstance();
            String gsHome = config
                    .getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY);
            StringBuilder bin = new StringBuilder();
            bin.append(gsHome.replace('/', File.separatorChar));
            bin.append(File.separator);
            bin.append("jboss");
            bin.append(File.separator);
            bin.append("server");
            bin.append(File.separator);
            bin.append("bin");
            bin.append(File.separator);

            String command = bin.toString() + c.toString();
            s_logger.info("Executing command:" + command);
            ProcessRunner pr = new ProcessRunner(command, System.out,
                    System.err);
            Thread t = new Thread(pr);
            t.start();
        }
        catch (Exception e)
        {
            s_logger.error("Failed to execute script to shutdown jboss server",
                    e);
        }
    }

    public ServerRegistry getServerRegistry() throws GeneralException
    {
        return JBossServerRegistry.getInstance();
    }
}

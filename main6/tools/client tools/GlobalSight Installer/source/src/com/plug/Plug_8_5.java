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
package com.plug;

import org.apache.log4j.Logger;

import com.util.CmdUtil;
import com.util.ServerUtil;

public class Plug_8_5 implements Plug
{
	private static Logger log = Logger.getLogger(Plug_8_5.class);
	
    @Override
    public void run()
    {

    	try 
    	{
			installGlobalSightService();
		} 
    	catch (Exception e) 
		{
			log.error(e);
		}
          
        updateStartMenuPath();
    }
    
    private boolean is64Bit()
    {
        String os = System.getProperty("os.arch");
        return os.endsWith("64");
    }
    
    public void updateStartMenuPath()
    {
    	String updateBin = ServerUtil.getPath() + "/jboss/util/bin/CreateStartMenu.cmd";
    	String os = System.getProperty("os.name");
    	
    	if (os.startsWith("Win"))
        {
            String[] cmd = {updateBin};
            
            log.info("\nUpdating start menu path...");
            try 
        	{
            	 CmdUtil.run(cmd, false, true);
    		} 
        	catch (Exception e) 
    		{
    			log.error(e);
    		}
           
            log.info("done.");
        }
    }
    
    public void installGlobalSightService() throws Exception
    {
    	String os = System.getProperty("os.name");
    	String jbossUtilBin = ServerUtil.getPath() + "/jboss/util/bin";
    	
    	if (os.startsWith("Win"))
        {
            boolean is64Bit = is64Bit();
            log.info("Removing service if it is installed...");
			String uninstallCommand = jbossUtilBin
					+ "/service-win32-uninstall.bat";
            if (is64Bit)
            {
				uninstallCommand = jbossUtilBin + "/service-win64-uninstall.bat";
            }
            
            String[] cmd = {uninstallCommand};
            
            try 
        	{
            	 CmdUtil.run(cmd, false, true);
    		} 
        	catch (Exception e) 
    		{
    			log.error(e);
    		}
           
            log.info("done.");

            log.info("\nAdding NT service...");
			String installCommand = jbossUtilBin + "/service-win32-install.bat";
            if (is64Bit)
            {
                installCommand = jbossUtilBin + "/service-win64-install.bat";
            }
            cmd[0] = installCommand;
            
            try 
        	{
            	CmdUtil.run(cmd, false, true);
    		} 
        	catch (Exception e) 
    		{
    			log.error(e);
    		}
            
            log.info("done.");
        }
    	else if (os.startsWith("Linux"))
        {
            String serviceSh = jbossUtilBin + "/service.sh";
            String installServiceSh = jbossUtilBin + "/InstallApp-NT.sh";
            String[] installService =
                { "sh", installServiceSh, serviceSh, "globalsight" };
            CmdUtil.run(installService, false, true);
        }
    }
}

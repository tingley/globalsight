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

/** NtServicesController allows the starting and stopping of NT Services*/
public class NtServicesController
{

    /** Stops the given NT Service. Output goes to stdout,stderr*/
    public static void stopService (String p_serviceName)
    {
	controlNetService(STOP_SERVICE,p_serviceName);
    }

    /** Starts the given NT Service. Output goes to stdout,stderr*/
    public static void startService (String p_serviceName)
    {
	controlNetService(START_SERVICE, p_serviceName);
    }

    //actually performs the action and sends output to System.out, System.err
    private static void controlNetService (int p_command, String p_serviceName)
    {
	String netCommand = "";
	switch (p_command)
	{
	case STOP_SERVICE:
	    netCommand = "stop";
	    break;
	case START_SERVICE:
	    netCommand = "start";
	    break;
	default:
	    netCommand = "stop";
	    break;
	}

	//create the command line like: net start "myService"
	String commandLine = NET + netCommand + " \"" + p_serviceName + "\"";
        ProcessRunner pr = new ProcessRunner(commandLine,System.out,System.err);
        Thread t = new Thread(pr);
        t.start();
        try
        {
            t.join();
        }
        catch (InterruptedException ie)
        {
        }
    }


    //actions on NT Services
    private static final int STOP_SERVICE = 0;
    private static final int START_SERVICE = 1;
    private static final String NET = "net ";

}


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
package com.globalsight.util.j2ee.pramati; 

import com.globalsight.everest.util.system.AmbassadorServer;
import com.pramati.services.j2ee.spi.ApplicationStateListener;

/**
 * This is the class for Pramati to startup and shutdown the
 * GlobalSight system.
 */
public class AmbassadorStartupShutdown implements ApplicationStateListener
{
    public void applicationPrepared()
    {
        System.out.println("-----------(Pramati) GlobalSight starting up------------");
        start();
        System.out.println("-----------(Pramati) GlobalSight started ------------");
    }

    public void applicationStarted(){}

    public void applicationStopped()
    {
        System.out.println("-----------(Pramati) GlobalSight shutting down------------");
        stop();
        System.out.println("-----------(Pramati) GlobalSight shut down------------");
    }

    public void applicationRemoved(){}

    /**
     * Called when Pramati starts up
     */
    private void start(){
        try
        {
            String s= AmbassadorServer.getAmbassadorServer().startup("pramati",null);
            System.out.println(s);
        }
        catch (Throwable t)
        {
            System.out.println("Error starting up:" + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Called when Pramati shuts down
     */
    private void stop(){
        try
        {
            String s = AmbassadorServer.getAmbassadorServer().shutdown("pramati",null);
            System.out.println(s);
        }
        catch (Exception e)
        {
            System.out.println("Error shutting down:" + e.getMessage());
            e.printStackTrace();
        }
    }
}


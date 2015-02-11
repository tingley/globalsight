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

package com.globalsight.everest.util.system;

/**
 * This interface defines the methods that a server control object in
 * Envoy must implement.  A control object has a startup method for
 * system startup, and a shutdown method for system shutdown.
 * 
 * @version     1.0, (8/14/00)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         08/14/2000   Initial version.
 */

public interface SystemControl
{
    /**
     * This method is called to start the system that is controlled
     * by this object.
     */
    public void startup() throws SystemStartupException;

    /**
     * This method is called to shutdown the system that is
     * controlled by this object.
     */
    public void shutdown() throws SystemShutdownException;
}
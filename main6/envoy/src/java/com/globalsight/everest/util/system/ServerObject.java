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

import com.globalsight.util.GeneralException;
import com.globalsight.util.system.ConfigException;

/**
 * This interface defines the methods that a server object in Envoy
 * must implement.  A server object in Envoy is one that needs to be
 * started at system startup, and destroyed at system shutdown.
 * 
 * @version     1.0, (8/14/00)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         08/14/2000   Initial version.
 */

public interface ServerObject
{
    /**
     * This is the initialization method for the server object.
     * The system calls this method immediate after instantiating
     * this object. 
     */
    public void init() throws SystemStartupException;

    /**
     * This is the method for the server object to clean up before
     * it is destroyed.  The system calls this method immediately
     * before system shutdown.
     */
    public void destroy() throws SystemShutdownException;
}
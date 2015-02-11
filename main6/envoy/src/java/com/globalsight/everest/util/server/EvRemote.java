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
package com.globalsight.everest.util.server;

/**
 * This interface defines a type that the Envoy Server Registry
 * Facility accepts as Envoy servers.  Any server objects that need to
 * be registered with the Envoy Server Registry Facility must
 * implement this interface.
 *
 * @version     1.0, (3/17/00 9:53:23 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

public interface EvRemote
{
    /**
     * Get the reference to the local implementation of the server.
     *
     * @return The reference to the local implementation of the
     * server.
     */
    Object getLocalReference();
}

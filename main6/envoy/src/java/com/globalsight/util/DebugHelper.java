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


import com.globalsight.everest.workflow.WorkflowHelper;



/**
 * Helper class to aid in debugging.
 */
public class DebugHelper 
{

    /**
     * Return a string representation of the object appropriate
     * for logging at the DEBUG priority.
     * @param p_Object - any object.  A null object will
     * return the String "null".
     * @return a string representation of the object appropriate
     * for logging at the DEBUG priority.
     */
    public static String getDebugString(Object p_Object)
    {
        // Temporarily use WorkflowHelper until
        // we have time to move the logic here.
        return WorkflowHelper.toDebugString(p_Object);
    }
}

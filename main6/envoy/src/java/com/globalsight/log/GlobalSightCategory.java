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

package com.globalsight.log;

/**
 * Extends Logger for some separation and control of log4j logging.
 */
public class GlobalSightCategory
{
    static private final String LINE_CONTINUATION = "\t~\n\t";

    /**
     * Return line continuation string to be used in logging.
     * 
     * <p>
     * This is not a static method so some category specific behavior could be
     * used.
     */
    public static String getLineContinuation()
    {
        return LINE_CONTINUATION;
    }
}

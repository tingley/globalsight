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

package com.globalsight.util.progress;

import java.util.List;

/**
 * ProcessMonitor is an interface to monitor a lengthy process.
 * ProcessMonitor is typically used to refresh a progress bar UI.
 */
public interface ProcessMonitor
{
    /** Method for getting counter value. */
    public int getCounter();

    /** Method for getting percentage complete information. */
    public int getPercentage();

    /**
     * Method for getting a status if the process has finished (either
     * successfully, with error or canceled by user request)
     */
    public boolean hasFinished();
    
    /**
     * Method for getting a message that replaces an existing message
     * in UI. This is typically used to get a message that shows the
     * current status
     */
    public String getReplacingMessage();

    /**
     * Method for getting messages that are appended in UI. This is
     * typically used to get messages that cumulatively displayed
     * e.g. items so far done.
     */
    public List getAppendingMessages();

    /**
     * Returns true if an error has occured and the replacing message
     * contains error message.
     */
    public boolean isError();
        
}

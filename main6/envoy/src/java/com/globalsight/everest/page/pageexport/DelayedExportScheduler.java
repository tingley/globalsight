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

package com.globalsight.everest.page.pageexport;

import java.util.List;
import java.util.Date;
import org.apache.log4j.Logger;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.page.pageexport.DelayedExportRequest;

/**
 *  Provides the timer for scheduling how long a delayed export should be stalled.
 *  Also provides the call-back (trigger) for when the timer expires.
 */
class DelayedExportScheduler
{
    // for logging purposes
    private transient static Logger s_logger =
        Logger.getLogger(
            DelayedExportScheduler.class.getName());

    private DelayedExportRequest m_delayedRequest;

    /**
     * Constructor
     */
    DelayedExportScheduler(DelayedExportRequest p_request)
    {
        m_delayedRequest = p_request;
    }

    /**
     * method which executes the timer task
     * all operations which are periodic have to be done here
     */
    public void execute()
    {
	try {
            s_logger.info("Executing delayed export task.");
	    DelayedExporter.getInstance().startExport(m_delayedRequest);
	}
	catch (Exception e)
	{
	    s_logger.error("Failed to delay export of source pages for update:",e);
	}
    }

    /**
     * The point in time at which the timer must happen.
     * @return Date 
     */
    public Date getExpiration()
    {
        Date  d = m_delayedRequest.getTime().getDate();
        Date now = new Date();
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Date now is: " + now.toString() +
                           " launching timer for " + d.toString());
        }
        return d; 
    }
}


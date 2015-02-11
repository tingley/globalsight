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

package com.globalsight.terminology.scheduler;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.scheduling.EventHandler;
import com.globalsight.scheduling.EventHandlerException;
import com.globalsight.scheduling.EventInfo;
import com.globalsight.scheduling.KeyFlowContext;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.audit.TermAuditEvent;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.terminology.indexer.IIndexManager;



/**
 * TermbaseDispatcher acts on "termbase reindex" events sent by schedule engine (Quartz),
 * and executes them.
 */
public class TermbaseDispatcher
    extends EventHandler
{
    private static final Logger s_logger =
        Logger.getLogger(
            TermbaseDispatcher.class);

    static private ITermbaseManager s_manager;

	//
    // Constructor
    //

    public TermbaseDispatcher()
    {
        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
            }
            catch (Exception ex)
            {
                s_logger.error("Termbase Manager is not available (yet?)", ex);

                throw new RuntimeException("Cannot access TermbaseManager", ex);
            }
        }
    }

    //
    // Implementation of the Abstract Method in EventHandler.
    //

    /**
     * This method is called when a scheduled event is fired.  Subclasses must
     * implement this method in order to obtain the desired behavior at fire
     * time.
     *
     * @param 
     * @throws EventHandlerException if any error occurs.
     */
    public void eventFired(KeyFlowContext p_flowContext)
        throws EventHandlerException
    {
        try
        {
            EventInfo info = (EventInfo)p_flowContext.getKey();

            performReindexing(info.getMap());
        }
        catch (Throwable ex)
        {
            s_logger.error("Failed to re-index a termbase.", ex);
        }
    }

    //
    // Private Methods
    //

    private void performReindexing(HashMap p_args)
        throws Exception
    {
        Long termbaseId = (Long)p_args.get(TermbaseScheduler.TERMBASE_ID);
		IIndexManager indexer = null;

		try
		{
			String name = s_manager.getTermbaseName(termbaseId.longValue());

			if (name == null)
			{
				s_logger.info("Scheduled re-indexing of termbase " + termbaseId + 
					" cannot be started because the termbase has been deleted.");

				return;
			}

			ITermbase tb = s_manager.connect(name, ITermbase.SYSTEM_USER, "");

			try
			{
				indexer = tb.getIndexer();
			}
			catch (Throwable ex)
			{
				TermAuditEvent auditEvent = new TermAuditEvent(
					new Date(), ITermbase.SYSTEM_USER, name, name, "ALL",
					"re-index", "scheduled re-indexing cannot be performed" + 
					" (termbase is being re-indexed already)");
				TermAuditLog.log(auditEvent);

				return;
			}

			TermAuditEvent auditEvent = new TermAuditEvent(
				new Date(), ITermbase.SYSTEM_USER, name, name, "ALL",
				"re-index", "scheduled re-indexing started");
			TermAuditLog.log(auditEvent);

			indexer.doIndex();
		}
		catch (Throwable ex)
		{
			s_logger.error("scheduled re-indexing error", ex);
		}
	}
}

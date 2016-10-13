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

import com.globalsight.terminology.scheduler.TermbaseReindexExpression;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>The RMI interface for the Terminology Scheduler, which is
 * responsible for setting up cron jobs for termbase indexing.</p>
 */
public interface ITermbaseScheduler
	extends Remote
{
    /**
     * Schedules the event for re-indexing a termbase.
     */
    public void scheduleEvent(TermbaseReindexExpression p_expr)
        throws Exception, RemoteException;

    /**
     * Unschedules the event for reindexing the given termbase and
     * removes it from the db.
     */
	public void unscheduleEvent(Long p_termbaseId)
        throws Exception, RemoteException;

	/**
	 * Retrieves the TermbaseReindexExpression for the given termbase. 
	 */
	public TermbaseReindexExpression getEvent(Long p_termbaseId)
        throws Exception, RemoteException;
}

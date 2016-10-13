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

package com.globalsight.scheduling;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * EventHandler is an abstract class that implements the required behavior
 * for Quartz and provides local abstract methods that must be implemented
 * by concrete subclasses.
 * <p>
 * Subclasses can be devised for implementing whatever behavior is required
 * for certain kinds of events.  For example, a subclass could handle the
 * sending of emails to specific recipients.
 * <p>
 * To qualify as a valid subclass, the new class <i>must</i> implement the
 * method <code>eventFired(KeyFlowContext)</code>.
 */
public abstract class EventHandler implements StatefulJob
{

    /**
     * Constant used as the intial firing of a recurring event.
     */
    public static final int INITIAL = 1;

    
    // PROTECTED CONSTRUCTORS
    protected EventHandler()
    {
        super();
    }

    // ABSTRACT METHODS
    /**
     * This method is called when a scheduled event is fired.  Subclasses must
     * implement this method in order to obtain the desired behavior at fire
     * time.
     *
     * @param p_fireDate the date/time when the event fired.
     * @param p_event the event itself.
     *
     * @throws EventHandlerException if any error occurs.
     */
    public abstract void eventFired(KeyFlowContext p_flowContext)
        throws EventHandlerException;

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		eventFired(new KeyFlowContext(context));
	}
}

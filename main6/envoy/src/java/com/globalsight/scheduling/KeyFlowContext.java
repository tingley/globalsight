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

import org.quartz.JobExecutionContext;

/**
 * KeyFlowContent is a wrapper for JobExecutionContext which contains the execution
 * context for this job.
 */
public class KeyFlowContext {
	
	private JobExecutionContext context = null;
	
	public KeyFlowContext(JobExecutionContext context) {
		this.context = context;
	}
	
	/**
	 * Get special parameter from execution context.
	 * The parameter key is SchedulerConstants.KEY_PARAM
	 * 
	 * @return
	 */
	public Object getKey() {
		if (context == null) {
			return null;
		} else {
			return context.getJobDetail().getJobDataMap().get(
					SchedulerConstants.KEY_PARAM);
		}
	}
	
	/**
	 * Get special parameter value by the name from context.
	 * 
	 * @param name
	 * @return
	 */
	public Object get(String name) {
		if (context == null) {
			return null;
		} else {
			return context.getJobDetail().getJobDataMap().get(name);
		}
	}
	
	/**
	 * Put parameter into the execution context.
	 * 
	 * @param name
	 * @param value
	 */
	public void put(String name, Object value) {
		if (context == null) {
			return;
		} else {
			//context.put(name, value);
		    context.getJobDetail().getJobDataMap().put(name, value); 

		}
	}

}

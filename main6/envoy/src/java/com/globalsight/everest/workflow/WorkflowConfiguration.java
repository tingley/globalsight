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
package com.globalsight.everest.workflow;

/*
 * Copyright (c) 2000, GlobalSight Corporation. All rights reserved. THIS
 * DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF GLOBALSIGHT
 * CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT IN CONFIDENCE.
 * INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED OR DISCLOSED IN WHOLE OR
 * IN PART EXCEPT AS PERMITTED BY WRITTEN AGREEMENT SIGNED BY AN OFFICER OF
 * GLOBALSIGHT CORPORATION.
 * 
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER SECTIONS 104
 * AND 408 OF TITLE 17 OF THE UNITED STATES CODE. UNAUTHORIZED USE, COPYING OR
 * OTHER REPRODUCTION IS PROHIBITED BY LAW.
 */
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;

/**
 * WorkflowConfiguration is a singleton object used for providing a reference to
 * the jbpm's workflow configuration. This object is initially instantiated
 * within WorkflowServerLocal
 * <p>
 */

public class WorkflowConfiguration
{
	private static WorkflowConfiguration m_instance = null;

	private JbpmConfiguration m_config = null;

	private WorkflowConfiguration()
	{
		m_config = JbpmConfiguration.getInstance();
	}

	public static WorkflowConfiguration getInstance()
	{
		if (m_instance == null)
		{
			m_instance = new WorkflowConfiguration();
		}
		return m_instance;
	}

	/**
     * Gets the jbpm's workflow context.
     * <p>
     * 
     * @return The jbpm workflow context.
     */
	public JbpmContext getJbpmContext()
	{
		return m_config.createJbpmContext();
	}

	/**
     * Gets current context.
     * 
     * @return The jbpm workflow context.
     */
	public JbpmContext getCurrentContext()
	{
		return m_config.getCurrentJbpmContext();
	}

	/**
     * Closes the jbpm configuration.
     * <p>
     */
	public void logout()
	{
		if (m_config != null)
		{
			m_config.close();
			m_instance = null;
		}
	}
}

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

// JDK
import java.util.List;

/**
 * A wrapper object that holds a process instance and its active nodes.
 */

public class ProcessInstanceInfo
{

	private List m_activeNodes = null;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
     * Constructs a package level ProcessInstanceInfo.
     */
	ProcessInstanceInfo(List p_activeNodes)
	{

		m_activeNodes = p_activeNodes;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	/**
     * Get a list of active nodes for this process instance.
     * 
     * @return A list of active nodes.
     */
	List getActiveNodes()
	{
		return m_activeNodes;
	}

}

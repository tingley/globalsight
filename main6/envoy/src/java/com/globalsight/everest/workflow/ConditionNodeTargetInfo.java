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



/**
* ConditionNodeTargetInfo contains the info about the target nodes of a conditon node.
* The info consists of the outgoing arrow name and the target node name.
* Note that only non-condition node info will be stored here.  If another condition node
* is attached to a condition node, it's info will not be stored here.  Therefore, the 
* target default arrow will be activated for that second target node.
*/

public class ConditionNodeTargetInfo implements java.io.Serializable
{
    
	private static final long serialVersionUID = -8295274826434490587L;
	
	private String m_arrowName = null;
    private String m_targetNodeName = null;


    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public ConditionNodeTargetInfo(String p_arrowName,
                                   String p_targetNodeName)
    {
        m_arrowName = p_arrowName;
        m_targetNodeName = p_targetNodeName;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the arrow name pointing from the condition node to the target node.
     * @return The arrow name pointing to the target node.
     */
    public String getArrowName()
    {
        return m_arrowName;
    }

    /**
     * Get the target node name.
     * @return The name of the conditon node's target node.
     */
    public String getTargetNodeName()
    {
        return m_targetNodeName;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
}

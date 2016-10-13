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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NextNodes contains a list of NodeInstance objects following a particular
 * node instance.  It will also give information whether one of the following
 * nodes is the Exit node.  Note that for a Condition node, only if the target
 * selection was an Exit node, we'll set the flag to true.  Also for an AND
 * node, the flag is set to true if all of the incoming source nodes are 
 * completed.
 *
 */

public class NextNodes
{
    private boolean m_hasExitNode = false;
    private List<Object> m_nodes = null;
    private Map<Object,Integer> m_activeStates = new HashMap<Object,Integer>();
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public NextNodes(int p_initCapacity)
    {        
        m_nodes = new ArrayList<Object>(p_initCapacity);
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Adds the node instance to the list and also keep the state 
     * prior to any action performed on the task or its workflow.
     */
    boolean addNode(Object p_nodeInstance, Integer p_state)
    {
        m_activeStates.put(p_nodeInstance, p_state);
        return m_nodes.add(p_nodeInstance);
    }

    /*
     * Set the value to determine whether there's an Exit node as the next node.     
     */
    void exitNode()
    {
        m_hasExitNode = true;
    }

    /**
     * Get the node at the specified index in the list.
     */
    Object getNode(int p_index)
    {
        return m_nodes.get(p_index);
    }

    /**
     * Returns true if the state of the specified node was active
     * prior to any possible action that might have been performed
     * on the task or the workflow.
     */
    boolean wasActive(Object p_nodeInstance)
    {
        Integer activeState = (Integer)m_activeStates.get(p_nodeInstance);

        return activeState != null && 
            activeState.intValue() == WorkflowConstants.STATE_RUNNING;
    }

    /*
     * Return true if at least one of the next nodes is an Exit node.  
     * Otherwise, return false.  This is used to determine the completion
     * of a workflow
     */
    boolean hasExitNode()
    {
        return m_hasExitNode;
    }

    /**
     * Returns the number of nodes in this list.
     */
    int size()
    {
        return m_nodes.size();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////
}

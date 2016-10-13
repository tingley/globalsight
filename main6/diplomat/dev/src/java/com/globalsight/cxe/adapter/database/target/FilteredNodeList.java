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
package com.globalsight.cxe.adapter.database.target;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a simple vector-based implementation of the NodeList interface.
 */
public class FilteredNodeList
implements NodeList
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Vector m_nodes;
    
    /**
     * Creates a new instance of the implementing class, with an initially
     * empty collection.  Nodes are added to the collection via the addNode()
     * method.
     */
    public FilteredNodeList()
    {
        super();
        m_nodes = new Vector();
    }

    //
    // INTERFACE METHOD IMPLEMENTATION
    //
    /**
     * Return the Node at the given index, or null if the index is out of range.
     *
     * @param p_index the index of the desired Node, must be between 0 and the
     * length of the internal list.
     *
     * @return the desired Node or null.
     */
    public Node item(int p_index)
    {
        return ((p_index < 0 || p_index >= m_nodes.size()) ?
                null : 
                (Node)m_nodes.elementAt(p_index));
    }

    /**
     * Return the length of the list.
     *
     * @return the current length of the list.
     */
    public int getLength()
    {
        return m_nodes.size();
    }

    //
    // PUBLIC SUPPORT METHODS
    //
    /**
     * Add the given Node to the internal collection.
     *
     * @param p_node the node to add.
     */
    public void addNode(Node p_node)
    {
        m_nodes.addElement(p_node);
    }
}


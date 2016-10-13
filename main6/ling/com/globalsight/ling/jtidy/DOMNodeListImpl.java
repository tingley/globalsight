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
package com.globalsight.ling.jtidy;

/**
 * DOMNodeListImpl. The items in the <code>NodeList</code> are accessible via an integral index, starting from 0.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMNodeListImpl implements org.w3c.dom.NodeList
{

    /**
     * Parent Node.
     */
    private Node parent;

    /**
     * Instantiates a new DOM node list.
     * @param parent parent Node
     */
    protected DOMNodeListImpl(Node parent)
    {
        this.parent = parent;
    }

    /**
     * @see org.w3c.dom.NodeList#item(int)
     */
    public org.w3c.dom.Node item(int index)
    {
        if (parent == null)
        {
            return null;
        }

        int i = 0;
        Node node = this.parent.content;
        while (node != null)
        {
            if (i >= index)
            {
                break;
            }
            i++;
            node = node.next;
        }
        if (node != null)
        {
            return node.getAdapter();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.NodeList#getLength
     */
    public int getLength()
    {
        if (parent == null)
        {
            return 0;
        }

        int len = 0;
        Node node = this.parent.content;
        while (node != null)
        {
            len++;
            node = node.next;
        }
        return len;
    }

}

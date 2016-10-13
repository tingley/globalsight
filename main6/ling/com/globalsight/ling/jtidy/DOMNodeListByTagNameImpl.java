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
 * DOMNodeListByTagNameImpl. The items in the <code>NodeList</code> are accessible via an integral index, starting
 * from 0.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMNodeListByTagNameImpl implements org.w3c.dom.NodeList
{

    /**
     * First node.
     */
    private Node first;

    /**
     * Tag name.
     */
    private String tagName;

    /**
     * Current index.
     */
    private int currIndex;

    /**
     * Max index (number of nodes).
     */
    private int maxIndex;

    /**
     * Current node.
     */
    private Node currNode;

    /**
     * Instantiates a new DOMNodeListByTagName.
     * @param first first node.
     * @param tagName tag name
     */
    protected DOMNodeListByTagNameImpl(Node first, String tagName)
    {
        this.first = first;
        this.tagName = tagName;
    }

    /**
     * @see org.w3c.dom.NodeList#item
     */
    public org.w3c.dom.Node item(int index)
    {
        currIndex = 0;
        maxIndex = index;
        preTraverse(first);

        if (currIndex > maxIndex && currNode != null)
        {
            return currNode.getAdapter();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.NodeList#getLength
     */
    public int getLength()
    {
        currIndex = 0;
        maxIndex = Integer.MAX_VALUE;
        preTraverse(first);
        return currIndex;
    }

    /**
     * Traverse the node list.
     * @param node Node
     */
    protected void preTraverse(Node node)
    {
        if (node == null)
        {
            return;
        }

        if (node.type == Node.START_TAG || node.type == Node.START_END_TAG)
        {
            if (currIndex <= maxIndex && (tagName.equals("*") || tagName.equals(node.element)))
            {
                currIndex += 1;
                currNode = node;
            }
        }
        if (currIndex > maxIndex)
        {
            return;
        }

        node = node.content;
        while (node != null)
        {
            preTraverse(node);
            node = node.next;
        }
    }

}

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

package com.globalsight.everest.webapp.webnavigation;

import java.util.Hashtable;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class describes a web link.
 */
public class WebLinkDescriptor
{
    // Constants.
    public static final String LINK_ELEMENT_NAME = "PageLink";
    public static final String LINK_NAME = "linkName";
    public static final String DESTINATION_PAGE_NAME = "destinationPageName";

    // Persistent member variables.
    private String m_linkName = null;
    private String m_pageName = null;

    /**
     * Default constructor.
     *
     * @param linkName The name of the link descriptor.
     * @param pageName The name of the web page to which the link points.
     */
    public WebLinkDescriptor(String linkName, String pageName)
    {
        m_linkName = linkName;
        m_pageName = pageName;
    }

    /**
     * Factory method that builds web links based on a DOM node
     *
     * @param aLinkNode a Node object that contains an XML/DOM
     * description for a web link
     */
    public static WebLinkDescriptor createWebLink(Node aLinkNode)
    {
        WebLinkDescriptor retVal = null;

        // Sanity check.
        if (aLinkNode.getNodeName().equals(LINK_ELEMENT_NAME))
        {
            // Extract the attributes of the page.
            NamedNodeMap nnm = aLinkNode.getAttributes();

            // Find the name of the link.
            Node attNode = nnm.getNamedItem(LINK_NAME);
            String linkName = attNode.getNodeValue();

            // Extract destination page name attribute.
            attNode = nnm.getNamedItem(DESTINATION_PAGE_NAME);
            String destinationPageName = attNode.getNodeValue();

            // Enforce required attributes.
            if (linkName != null && destinationPageName != null)
            {
                retVal = new WebLinkDescriptor(linkName, destinationPageName);
            }
        }

        return retVal;
    }

    /**
     * Accessor for the link name.
     *
     * @return the name of the link
     */
    public String getLinkName()
    {
        return m_linkName;
    }

    /**
     * accessor for the page name
     *
     * @return the name of the page
     */
    public String getPageName()
    {
        return m_pageName;
    }

    /**
     * Utility method to stream out contents of WebLinkDescriptor
     *
     * @param sb The StringBuffer to which to append the stream.
     * @param indent A String to prepend to the stream, in the
     * StringBuffer.
     */
    public void streamOut(StringBuffer sb, String indent)
    {
        sb.append(indent + "Web Link Name: " + m_linkName
          + " targetPageName: " + m_pageName + "\n");
    }


}



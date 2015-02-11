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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class describes a web page and its resources.
 */
public class WebPageDescriptor
{
    // Constants.
    public static final String PAGE_ELEMENT_NAME = "WebPage";
    public static final String PAGE_NAME = "pageName";
    public static final String IS_DEFAULT_PAGE = "isDefault";
    public static final String JSP_NAME = "jspName";
    public static final String PAGE_HANDLER_CLASS_NAME = "pageHandlerClassName";
    public static final String PERMISSION_NAME = "permission";

    // Persistent member variables.
    private String m_pageName = null;
    private Boolean m_isDefault = Boolean.FALSE;
    private String m_jspName = null;
    private String m_pageHandlerClassName = null;
    private String m_permissionName = null;
    private Hashtable m_webLinks = new Hashtable();
    private Hashtable m_predecessorWebLinks = new Hashtable();

    public WebPageDescriptor (Node p_pageNode)
    {
        createWebPage(p_pageNode);
    }

    /**
     * Adds a new predecessor web link to the description of this page.
     *
     * @param p_descriptor the descriptor for a web link
     */
    public void addPredecessorWebLink(WebLinkDescriptor p_descriptor)
    {
        m_predecessorWebLinks.put(p_descriptor.getLinkName(), p_descriptor);
    }

    /**
     * Adds a new successor web link to the description of this page.
     *
     * @param p_descriptor the descriptor for a web link
     */
    public void addWebLink(WebLinkDescriptor p_descriptor)
    {
        m_webLinks.put(p_descriptor.getLinkName(), p_descriptor);
    }


    /**
     * Factory method that builds pages based on a DOM node.
     *
     * @param aPageNode a Node object that contains an XML/DOM
     * description for a web page
     */
    public void createWebPage(Node p_pageNode)
    {
        // Sanity check.
        if (p_pageNode.getNodeName().equals(PAGE_ELEMENT_NAME))
        {
            // Extract the attributes of the page.
            NamedNodeMap attrs = p_pageNode.getAttributes();

            // Find the name of the page.
            Node attr = attrs.getNamedItem(PAGE_NAME);
            String pageName = attr.getNodeValue();

            // Extract is_default attribute
            attr = attrs.getNamedItem(IS_DEFAULT_PAGE);
            Boolean isDefaultPage = Boolean.valueOf(attr.getNodeValue());

            // Extract the JSP name.
            attr = attrs.getNamedItem(JSP_NAME);
            String jspName = attr.getNodeValue();

            // Extract the page handler fully qualified class name.
            attr = attrs.getNamedItem(PAGE_HANDLER_CLASS_NAME);
            String pageHandlerClassName = attr.getNodeValue();

            // Extract the permission name.
            attr = attrs.getNamedItem(PERMISSION_NAME);
            String permissionName = null;
            if (attr != null)
            {
                permissionName = attr.getNodeValue();
            }

            m_pageName = pageName;
            m_isDefault = isDefaultPage;
            m_jspName = jspName;
            m_pageHandlerClassName = pageHandlerClassName;

            if (permissionName != null)
            {
                m_permissionName = permissionName;
            }

            // Retrieve any web link nodes hanging from this page.
            NodeList childNodes = p_pageNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++)
            {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    WebLinkDescriptor descriptor =
                        WebLinkDescriptor.createWebLink(node);

                    if (descriptor != null)
                    {
                        addWebLink(descriptor);
                    }
                }
            }
        }
    }

    /**
     * Accessor for the JSP URL.
     *
     * @return the URL of the JSP tht will display this page
     */
    public String getJspURL()
    {
        return m_jspName;
    }

    /**
     * Returns the name of a destination page given a link name.
     *
     * @param linkName a link name to traverse from this page
     */
    public String getDestinationPageName(String linkName)
    {
        WebLinkDescriptor descriptor =
            (WebLinkDescriptor)m_webLinks.get(linkName);

        return (String) descriptor.getPageName();
    }

    /**
     * Returns an enumeration of the link names that exit this page.
     *
     * @return Enumeration containing the link names that exit this
     * page.
     */
    public Enumeration getLinkNames()
    {
        return m_webLinks.keys();
    }

    /**
     * Accessor for the page handler class name.
     *
     * @return the name the page handler class that will process this
     * page
     */
    public String getPageHandlerClassName()
    {
        return m_pageHandlerClassName;
    }

    /**
     * Accessor for the page name.
     *
     * @return the name of the page
     */
    public String getPageName()
    {
        return m_pageName;
    }

    /**
     * Accessor for the permission name.
     *
     * @return The permission name.
     */
    public String getPermissionName()
    {
        return m_permissionName;
    }

    /**
     * Accessor for isDefault flag.
     *
     * @return indocates whether this page is the default page within
     * the containing activity
     */
    public boolean isDefault()
    {
        return m_isDefault.booleanValue();
    }

    /**
     * Utility method to stream out contents of a WebPageDescriptor.
     *
     * @param sb The StringBuffer to which to append the stream.
     * @param indent String to prepend to the stream.
     */
    public void streamOut(StringBuffer sb, String indent)
    {
        sb.append(indent);
        sb.append("Web Page Name `");
        sb.append(m_pageName);
        sb.append("' JSP Name: `");
        sb.append(m_jspName);
        sb.append("' page handler: `");
        sb.append(m_pageHandlerClassName);
        sb.append("'\n");

        Enumeration linkList = m_webLinks.elements();
        while (linkList.hasMoreElements())
        {
            WebLinkDescriptor descriptor =
                (WebLinkDescriptor) linkList.nextElement();

            descriptor.streamOut(sb, indent + "  ");
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        streamOut(sb, "");

        return sb.toString();
    }
}


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
 * This class describes a web link.
 */
public class WebActivityDescriptor
{
    // Constants.
    public static final String ACTIVITY_ELEMENT_NAME = "WebActivity";
    public static final String ACTIVITY_NAME = "webActivityName";
    public static final String CLEAR_SESSION = "clearSession";

    // Persistent member variables.
    private String m_activityName = null;
    private Hashtable m_webPages = new Hashtable();
    private boolean m_shouldClearSession = true; //default for most activities


    public WebActivityDescriptor(Node anActivityNode, WebSiteDescription aWebSiteDescription)
    {
        createWebActivity(anActivityNode, aWebSiteDescription);
    }

    /**
     * Adds a new web page to the description of this activity.
     *
     * @param aWebPageDescriptor The descriptor for a web link.
     */
    public void addWebPage(WebPageDescriptor aWebPageDescriptor)
    {
        m_webPages.put(aWebPageDescriptor.getPageName(), aWebPageDescriptor);
    }

    /**
     * Factory method that builds web activity based on a DOM
     * node. Rewritten by Brett 11/7/2000 to remove funky control-flow
     * from multiple return statements.
     *
     * @param anActivityNode A Node object that contains an XML/DOM
     * description for a web activity.
     * @param aWebSiteDescription An object that contains a
     * description of the entire web site.
     *
     * @return A WebActivityDescriptor object.
     */
    public void createWebActivity(Node anActivityNode,
        WebSiteDescription aWebSiteDescription)
    {

        // Sanity check.
        if (anActivityNode.getNodeName().equals(ACTIVITY_ELEMENT_NAME))
        {
            // Extract the attributes of the page.
            NamedNodeMap nnm = anActivityNode.getAttributes();

            // Find the name of the page.
            Node attNode = nnm.getNamedItem(ACTIVITY_NAME);
            String activityName = attNode.getNodeValue();

            // Enforce required attributes.
            if (activityName != null)
            {
                m_activityName = activityName;
            }

            //find whether we should clear the session
            Node csNode = nnm.getNamedItem(CLEAR_SESSION);
            if (csNode != null)
            {
                m_shouldClearSession = Boolean.valueOf(csNode.getNodeValue()).booleanValue();
            }


            NodeList childNodeList = anActivityNode.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++)
            {
                Node aPageNode = childNodeList.item(i);
                if (aPageNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    WebPageDescriptor aWebPageDescriptor = new WebPageDescriptor(aPageNode);

                    if (aWebPageDescriptor != null)
                    {
                        // Add the page to this activity.
                        addWebPage(aWebPageDescriptor);
                        // Add the page to the WebSiteDescription.
                        aWebSiteDescription.addPageDescriptor(aWebPageDescriptor);
                    }
                }
            }

            // Add to the cached list in the web site descriptor.
            aWebSiteDescription.addActivityDescriptor(this);
        }
    }

    /**
     * Accessor for the activity name.
     *
     * @return The String name of the link.
     */
    public String getActivityName()
    {
        return m_activityName;
    }

    /**
     * Returns whether the session (SessionManager) should be cleared
     * before calling this activity
     * 
     * @return true or false
     */
    public boolean shouldClearSession()
    {
        return m_shouldClearSession;
    }

    /**
     * Accessor for the default page within the activity. Only one
     * page can be the default page
     *
     * @return The WebPageDescriptor for the default activity page.
     */
    public WebPageDescriptor getDefaultPageDescriptor()
    {
        WebPageDescriptor retVal = null;

        Enumeration en = m_webPages.elements();
        while (en.hasMoreElements())
        {
            WebPageDescriptor aWebPageDescriptor =
              (WebPageDescriptor) en.nextElement();

            if (aWebPageDescriptor.isDefault())
            {
                retVal = aWebPageDescriptor;
                break;
            }
        }

        return retVal;
    }

    /**
     * Utility method to stream out contents of this
     * WebActivityeDescriptor.
     *
     * @param sb The StringBuffer to which to stream the contents of
     * the descriptor.
     * @param indent The String to prepend to the stream.
     */
    public void streamOut(StringBuffer sb, String indent)
    {
        sb.append(indent + "Web Activity Name: " + m_activityName + "\n");

        Enumeration pageList = m_webPages.elements();
        while (pageList.hasMoreElements())
        {
            WebPageDescriptor aWebPageDescriptor =
              (WebPageDescriptor)pageList.nextElement();

            aWebPageDescriptor.streamOut(sb, indent + "  ");
        }
    }
}

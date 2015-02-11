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
import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class describes a web module.
 */
public class WebModuleDescriptor
{
    // Constants.
    public static final String MODULE_ELEMENT_NAME = "WebModule";
    public static final String MODULE_NAME = "webModuleName";

    // Persistent member variables.
    private String m_moduleName = null;
    private Hashtable webActivities = new Hashtable();
    private List m_accessGroupNames = null;

    /**
     * Default constructor.
     *
     * @param moduleName The name of the web module.
     */
    private WebModuleDescriptor(String moduleName)
    {
        m_moduleName = moduleName;
    }


    /**
     * Adds a new web activity to the description of this module.
     *
     * @param aWebActivityDescriptor the descriptor for a web activity
     */
    public void addWebActivity(WebActivityDescriptor aWebActivityDescriptor)
    {
        webActivities.put(aWebActivityDescriptor.getActivityName(),
          aWebActivityDescriptor);
    }

    /**
     * Factory method that builds web activity based on a DOM node
     *
     * @param aModuleNode a Node object that contains an XML/DOM
     * description for a web module
     * @param aWebSiteDescription an object that contains a
     * description of the entire web site
     */
    public WebModuleDescriptor(Node aModuleNode,
        WebSiteDescription aWebSiteDescription)
    {
        createWebModule(aModuleNode, aWebSiteDescription);
    }

    private void createWebModule(Node aModuleNode,
        WebSiteDescription aWebSiteDescription)
    {

        // Sanity check.
        if (aModuleNode.getNodeName().equals(MODULE_ELEMENT_NAME))
        {
            // Extract the attributes of the page.
            NamedNodeMap nnm = aModuleNode.getAttributes();

            // Find the name of the page.
            Node attNode = nnm.getNamedItem(MODULE_NAME);
            String moduleName = attNode.getNodeValue();

            // Enforce required attributes.
            if (moduleName != null)
            {
                m_moduleName = moduleName;
            }

            // Retrieve the web activity descriptions.
            NodeList childNodeList = aModuleNode.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++)
            {
                Node anActivityNode = childNodeList.item(i);

                if (anActivityNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    if (anActivityNode.getNodeName().equals(
                      WebActivityDescriptor.ACTIVITY_ELEMENT_NAME))
                    {
                        WebActivityDescriptor aWebActivityDescriptor =
                            new WebActivityDescriptor(anActivityNode, aWebSiteDescription);

                        if (aWebActivityDescriptor != null)
                        {
                            addWebActivity(aWebActivityDescriptor);
                        }
                    }
                }
            }
        }
    }

    /**
     * accessor for the module name
     *
     * @return the name of the module
     */
    public String getModuleName()
    {
        return m_moduleName;
    }


    /**
     * Utility method to stream out contents of WebModuleDescriptor
     *
     * @param sb The StringBuffer to which to stream out the descriptor.
     * @param indent String to prepend to the stream.
     */
    public void streamOut(StringBuffer sb, String indent)
    {
        sb.append(indent + "Web Module Name: " + m_moduleName + "\n");

        Enumeration activityList = webActivities.elements();
        while (activityList.hasMoreElements())
        {
            WebActivityDescriptor aWebActivityDescriptor =
              (WebActivityDescriptor)activityList.nextElement();

            aWebActivityDescriptor.streamOut(sb, indent + "  ");
        }
    }
}


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

import java.io.InputStream;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.globalsight.everest.webapp.WebAppConstants;

/**
 * This class contains the description of an entire web site.
 */
public class WebSiteDescription implements WebAppConstants
{
    // Singleton instance.
    private static WebSiteDescription instance = null;

    // Persistent member variables.
    // a Hashtable that indexes all the web modules
    private Hashtable m_moduleTable = new Hashtable();
    // a Hashtable that indexes all the web activities
    private Hashtable m_activityTable = new Hashtable();
    // a hash table for quick access to the web pages
    private Hashtable m_pageTable = new Hashtable();

    /**
     * Default constructor.
     */
    private WebSiteDescription()
    {
    }

    /**
     * Adds a activity description to the web site description for
     * quick access
     *
     * @param anActivityDescriptor param the description of a Web
     * Activity
     */
    public void addActivityDescriptor(WebActivityDescriptor anActivityDescriptor)
    {
        // the acivities are indexed by their name (it must be unique)
        if (anActivityDescriptor != null)
        {
            m_activityTable.put(anActivityDescriptor.getActivityName(),
              anActivityDescriptor);
        }
    }

    /**
     * Adds a module description to the web site description for quick
     * access
     *
     * @param aModuleDescriptor param the description of a Web Module
     */
    public void addModuleDescriptor(WebModuleDescriptor aModuleDescriptor)
    {
        // The modules are indexed by their name (it must be unique).
        if (aModuleDescriptor != null)
        {
            m_moduleTable.put(aModuleDescriptor.getModuleName(),
              aModuleDescriptor);
        }
    }

    /**
     * Adds a page description to the web site description for quick
     * access
     *
     * @param aPageDescriptor param the description of a Web Page
     */
    public void addPageDescriptor(WebPageDescriptor aPageDescriptor)
    {
        // The pages are indexed by their name (it must be unique).
        if (aPageDescriptor != null)
        {
            m_pageTable.put(aPageDescriptor.getPageName(), aPageDescriptor);
        }
    }

    /**
     * Attempts to create the site description from a default XML file.
     *
     * @return <code>true</code> if successful, <code>false</code> otherwise.
     */
    public static boolean createSiteDescription()
    {
        return createSiteDescription(ENVOY_CONFIG_FILE);
    }

    /**
     * Reads in the Web Site description from an XML file, parses it
     * and populates this data structure
     *
     * @param siteDescriptionXMLFile the name of the XML file that
     * holds the site description
     * @return <code>true</code> if successful, <code>false</code>
     * otherwise.
     */
    public static boolean createSiteDescription(String siteDescriptionXMLFile)
    {
        boolean retVal = false;
        InputStream is = WebSiteDescription.class.getResourceAsStream(
          siteDescriptionXMLFile);

        if (is != null)
        {
	    DOMParser theParser = new DOMParser();
            try
            {
                theParser.parse(new InputSource(is));
            }
            catch (IOException exc1)
            {
                exc1.printStackTrace();
            }
            catch (SAXException exc2)
            {
                exc2.printStackTrace();
            }

            Document theDocument = theParser.getDocument();
            if (instance == null)
            {
                instance = new WebSiteDescription();
                instance.populateWebDescription(theDocument);
                retVal = true;
            }
        }
	return retVal;
    }

    /**
     * Returns a web activity descriptor given its name
     *
     * @param activityName The name of a activity descriptor.
     * @return The appropriate WebActivityDescriptor.
     */
    public WebActivityDescriptor getActivityDescriptor(String activityName)
    {
        return (WebActivityDescriptor) m_activityTable.get(activityName);
    }

    /**
     * Returns a web module descriptor given its name
     *
     * @param moduleName the name of a module descriptor
     * @return The appropriate WebModuleDescriptor.
     */
    public WebModuleDescriptor getModuleDescriptor(String moduleName)
    {
        return (WebModuleDescriptor) m_moduleTable.get(moduleName);
    }


    /**
     * Returns a web page descriptor given its name
     *
     * @param pageName The name of a page descriptor.
     * @return The appropriate WebPageDescriptor.
     */
    public WebPageDescriptor getPageDescriptor(String pageName)
    {
        return (WebPageDescriptor) m_pageTable.get(pageName);
    }

    /**
     * Returns a the singleton instance of this WebSiteDescription
     *
     * @return The singleton WebSiteDescription.
     */
    public static WebSiteDescription instance()
    {
        if (instance == null)
        {
            instance = new WebSiteDescription();
        }

        return instance;
    }

    /**
     * Indicates whether the site description is initialized or not
     *
     * @return <code>true</code> if is initialized, <code>false</code>
     * if not
     */
    public static boolean isInitialized()
    {
        return (instance != null);
    }

    /**
     * Creates a description of the entire web site.
     *
     * @param theDocument The document base of the web site.
     */
    private void populateWebDescription(Document theDocument)
    {
        Element rootElement = theDocument.getDocumentElement();
        String rootName = rootElement.getNodeName();

	// Extract all modules.
        NodeList moduleList = rootElement.getChildNodes();
        for (int j = 0; j < moduleList.getLength(); j++)
        {
            Node aModuleNode = moduleList.item(j);
            if (aModuleNode.getNodeType() == Node.ELEMENT_NODE)
            {
                WebModuleDescriptor aModuleDescriptor =
                    new WebModuleDescriptor(aModuleNode, this);

                if (aModuleDescriptor != null)
                {
                    this.addModuleDescriptor(aModuleDescriptor);
                }
            }
        }

        // Traverse the memory structure to link back all the pages
        // (obtain their predecessors).
        Enumeration en = m_pageTable.elements();
        while (en.hasMoreElements())
        {
            WebPageDescriptor currentPageDescriptor =
              (WebPageDescriptor)en.nextElement();

            Enumeration successorLinkNames = currentPageDescriptor.getLinkNames();
            while (successorLinkNames.hasMoreElements())
            {
                String linkName = (String) successorLinkNames.nextElement();
                String destPageName =
                  currentPageDescriptor.getDestinationPageName(linkName);

                WebPageDescriptor destPageDescriptor =
                  getPageDescriptor(destPageName);

                if (destPageDescriptor != null)
                {
                    // Add the current page to the destination page as
                    // a predecessor.
                    destPageDescriptor.addPredecessorWebLink(
                      new WebLinkDescriptor(linkName,
                        currentPageDescriptor.getPageName()));
                }
	    }
        }
    }

    /**
     * Creates a description of the contents of this Web Site
     * Description
     *
     * @return a String representation of the contents of this Web
     * Site Description message
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Enumeration moduleList = m_moduleTable.elements();
        while (moduleList.hasMoreElements())
        {
            WebModuleDescriptor aModuleDescriptor =
              (WebModuleDescriptor) moduleList.nextElement();

            aModuleDescriptor.streamOut(sb, "  ");
        }

        return sb.toString();
    }
}

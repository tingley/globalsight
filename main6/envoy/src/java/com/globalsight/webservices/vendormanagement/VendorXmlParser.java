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

package com.globalsight.webservices.vendormanagement;


// globalsight

// java
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import org.apache.log4j.Logger;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the vendor XML that is passed in to add or modify a vendor
 * via webservices.  It provides access into the XML and returns
 * certain elements and attributes that are needed.
 */
public class VendorXmlParser
{
    // public elements used by the web services
    public static final String ACTIVITY = "activityType";
    public static final String SOURCE_LOCALE = "sourceLocale";
    public static final String TARGET_LOCALE = "targetLocale";
    public static final String CUSTOM_NAME = "name";
    public static final String CUSTOM_FIELD = "field";
    public static final String CUSTOM_FIELD_VALUE = "value";

    // private elements referenced just by the parser
    private static final String ADDRESS = "address";
    private static final String COMM = "communication";
    private static final String COMM_LOCALE = "communicationLocale";
    private static final String COMPANY = "companyName";
    private static final String COUNTRY = "country";
    private static final String CUSTOM_ID = "customVendorId";
    private static final String CUSTOM_FIELDS = "customFields";
    private static final String CUSTOM_SECTION = "section";
    private static final String DOB = "dob";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String NATIONALITY = "nationality";
    private static final String NOTES = "notes";
    private static final String PASSWORD = "password";
    private static final String PROJECTS = "projects";
    private static final String PROJECT_ID = "projectId";
    private static final String PSEUDONYM = "pseudonym";
    private static final String RESUME = "resume";
    private static final String RESUME_FILENAME = "fileName";
    private static final String ROLE = "role";
    private static final String STATUS = "status";
    private static final String TITLE = "title";
    private static final String USER = "user";
    private static final String USERID = "username";
    private static final String VENDOR_TYPE = "isInternalVendor";
    
    private static final Logger c_logger = 
        Logger.getLogger(VendorXmlParser.class.getName());
    
    // attributes
    private static final String COMM_TYPE = "communicationType";
    private static final String COMM_VALUE = "communicationValue";
    private static final String ALL = "all";
    private static final String AMBASSADOR_USER = "isAmbassadorUser";
    private static final String EXISTING_USER = "isExistingAmbassadorUser";

    // private data members                                             
    private String m_vendorXml;     // the XML being parsed
    private Document m_document;    // 
    private Element m_rootElement;  // the root element of the document


    /**
     * Constructor
     */
    public VendorXmlParser(String p_vendorXml)
    {
        m_vendorXml = p_vendorXml;
    }               

    /** 
     * Parse the xml.
     */
    public void parse()
        throws Exception
    {
        StringReader sr = new StringReader(m_vendorXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        // tbd - "true"?
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        m_document = parser.getDocument();
        m_rootElement = m_document.getDocumentElement();    
    }

   /**
    * Finds the vendor custom id.
    */
    public String getCustomVendorId()
    {
        return getValue(CUSTOM_ID);
    }                

    public String getFirstName()
    {
        return getValue(FIRST_NAME);
    }

    public String getLastName()
    {
        return getValue(LAST_NAME);
    }

    public String getPseudonym()
    {
        return getValue(PSEUDONYM);
    }

    public String getCompanyName()
    {
        return getValue(COMPANY);
    }

    public String getTitle()
    {
        return getValue(TITLE);
    }

    public String getAddress()
    {
        return getValue(ADDRESS);
    }

    public String getCountry()
    {
        return getValue(COUNTRY);
    }

    public String getNationality()
    {
        return getValue(NATIONALITY);
    }

    public String getDateOfBirth()
    {
        return getValue(DOB);
    }

    public String getStatus()
    {
        return getValue(STATUS);
    }

    public String isInternalVendor()
    {
        return getValue(VENDOR_TYPE);
    }

    public String getNotes()
    {
        return getValue(NOTES);
    }

    public String getResume()
    {
        return getValue(RESUME);
    }

    public String getResumeFilename()
    {
        return findAttributeInXml(RESUME, RESUME_FILENAME);
    }

    public String getCommunicationLocale()
    {
        return getValue(COMM_LOCALE);
    }

    public String getAmbassadorUser()
    {
        return findAttributeInXml(USER, AMBASSADOR_USER);
    }

    public String getExistingAmbassadorUser()
    {
        return findAttributeInXml(USER, EXISTING_USER);
    }

    public String getUserId()
    {
        Element e = getSingleElement(USER);
        if (e != null) 
        {
            return getValue(USERID);
        }
        return null;
    }

    public String getPassword()
    {
        Element e = getSingleElement(USER);
        if (e != null)
        {
            return getValue(PASSWORD);
        }
        return null;
    }

    /** 
     * Return a hashtable of communication information supplied in the XML.
     * The key is the communication type and the object is the value.
     */
    public Hashtable getCommunicationInfo()
    {
        Hashtable cis = new Hashtable();

        NodeList elements = 
            m_rootElement.getElementsByTagName(COMM);

        // go through each of them and add to list
        for ( int i = 0 ; i < elements.getLength() ; i++)
        {
            Element elem = (Element)elements.item(i);
            // get the type and value
            String ct = getValue(elem, COMM_TYPE);
            String cv = getValue(elem, COMM_VALUE);

            // if the type isn't missing then store the value.
            // the value could be NULL to clear out the particular
            // communication type
            if (ct != null)
            {
                cis.put(ct, cv);
            }
        }
        return cis;
    }                  

    /** 
     * Return a list of all the custom field elements.
     */
    public NodeList getCustomFields()
    {
        NodeList nodes = null;
        Element celem = getSingleElement(CUSTOM_FIELDS);
        // if the custom tag was found.
        if (celem != null)
        {
            nodes = celem.getElementsByTagName(CUSTOM_SECTION);
        }
        return nodes;
    }

    public String getAllProjectsFlag()
    {
        return findAttributeInXml(PROJECTS, ALL);
    }

    /**
     * Returns a list of all the project ids.
     */
    public List getProjects()
    {
        List projects = new ArrayList();

        Element pelem = getSingleElement(PROJECTS);
        // if the project tag was found
        if (pelem != null)
        {
            NodeList nodes = pelem.getElementsByTagName(PROJECT_ID);
            // go through each of them and add to list
            for ( int i = 0 ; i < nodes.getLength() ; i++)
            {
                Element elem = (Element)nodes.item(i);
                // get the project id
                String pi = elem.getFirstChild().getNodeValue();
                projects.add(new Long(pi));
            }
        }
        return projects;
    }

    /**
     * Returns the node list to access the list of roles.
     */
    public NodeList getRoles()
    {
        NodeList elements = 
            m_rootElement.getElementsByTagName(ROLE);
        return elements;
    }
    // =================== package methods =========================

    /**
     * Return the value of the specified tag relative to 
     * the root element passed in.
     */
    static String getValue(Element p_rootElement, String p_tagName)
    {
        String value = null;
        Element ele = getSingleElement(p_rootElement, p_tagName);
        if (ele != null)
        {
            Node n = ele.getFirstChild();
            if (n != null)
            {
                value = n.getNodeValue();
            }
        }
        return value;
    }


    // =================== private methods =================

    /**
     * Return the value of the specified tag relative to the
     * main root element.
     */
    private String getValue(String p_tagName)
    {
        return getValue(m_rootElement, p_tagName);
    }      

    /**
    * Gets the named element from the DOM structure
    * relative to the main root of the document.
    * <br>
    * @param p_elementName -- the name of the element to get
    * @return the Element
    */
    private Element getSingleElement(String p_elementName)
    {
        return getSingleElement(m_rootElement,p_elementName);
    }

    /**
    * Gets the named element from the specified DOM structure
    * relative to the root element specified.
    * <br>
    * @param p_elementName -- the name of the element to get
    * @return the Element
    */
    private static Element getSingleElement(Element p_rootElement, 
                                            String p_elementName)
    {
        return (Element)p_rootElement.
            getElementsByTagName(p_elementName).item(0);
    }

    /**
     * Find the specified attribute in the DOM and return the value.
     *
     * @param p_element The element tag the attribute is associated
     * with.
     * @param p_attributeLookingFor The attribute tag that is being
     * looked for.
     *
     * @return String The attribute value or null if it couldn't be
     * found.
     */
    private String findAttributeInXml(String p_element,
                                      String p_attributeLookingFor)
    {
        String attrValue = null;
        Element elem = m_document.getDocumentElement();

        //if didn't find it at the root
        if (elem.getNodeName() != p_element)
        {
            NodeList  elements = m_document.getDocumentElement().
                getElementsByTagName(p_element);

            //should only be one
            if (elements.getLength() > 0)
            {
                elem = (Element)elements.item(0);
                NamedNodeMap attrs = elem.getAttributes();
            }
        }
        if (elem != null)
        {
            attrValue = elem.getAttribute(p_attributeLookingFor);
        }

        // returns the value of the attribute or null if it couldn't
        // be found
        return attrValue;
    }
}

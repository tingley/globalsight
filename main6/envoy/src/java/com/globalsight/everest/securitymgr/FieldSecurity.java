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
 
 
package com.globalsight.everest.securitymgr;

import org.apache.log4j.Logger;


// globalsight
import com.globalsight.everest.persistence.PersistentObject;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.io.StringReader;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class holds the security access to a group of fields.
 * The field names are keys and the access levels are String
 * that are defined as shared, locked or hidden.
 */
public class FieldSecurity
    extends PersistentObject
{
    private static final long serialVersionUID = 3264291154435765079L;
    
    // valid access levels
    public static final String SHARED = "shared";
    public static final String LOCKED = "locked";
    public static final String HIDDEN = "hidden";

    // the field names and their security access levels are
    // stored in XML.
    protected String m_securityXml = new String();
    // the parsed XML is put into a hashtable
    // the hashtable is not persisted, but rather the XML representation of it
    protected Hashtable m_fs = new Hashtable();
    
    protected static Logger c_logger =
      Logger.getLogger(
          FieldSecurity.class.getName());

    private boolean m_htChanged = true;  // is set to true when the hashtable
                                          // changes - is used to know when
                                          // the xml needs to be regenerated.

    private String FIELD_SECURITY_TAG = "fieldSecurity";
    private String FIELD_TAG = "field";
    private String ACCESS_LEVEL_ATTRIBUTE = "accessLevel";

    
    /**
     * Default constructor
     */
    public FieldSecurity()
    {}
   
    /**
     * Sets all the field securities via a hashtable where
     * the field names are the keys and the value is the
     * access string (SHARED, LOCKED, HIDDEN)
     */
    public void setFieldSecurity(Hashtable p_security)
    {
        if (p_security == null)
        {
            m_fs.clear();
        }
        else
        {
            m_fs = p_security;
        }
        StringBuffer sXml = new StringBuffer("<?xml version=\"1.0\"?>");
        sXml.append("<");
        sXml.append(FIELD_SECURITY_TAG);
        sXml.append(">");
        // set up XML
        Set keys = m_fs.keySet();
        for (Iterator fsi = keys.iterator() ; fsi.hasNext() ; )
        {
            // tbd - check for valid field name??
            String fieldName = (String)fsi.next();
            String accessLevel = (String)m_fs.get(fieldName);
            sXml.append("<");
            sXml.append(FIELD_TAG);
            sXml.append(" ");
            sXml.append(ACCESS_LEVEL_ATTRIBUTE);
            sXml.append("=\"");
            if (!isValidAccessLevel(accessLevel))
            {
                accessLevel = HIDDEN;
            }
            sXml.append(accessLevel);
            sXml.append("\">");
            sXml.append(fieldName);
            sXml.append("</");
            sXml.append(FIELD_TAG);
            sXml.append(">");
        }
        sXml.append("</");
        sXml.append(FIELD_SECURITY_TAG);
        sXml.append(">");
        m_securityXml = sXml.toString();
        m_htChanged = false;    // the hashtable was updated and the XML too
                                // so no change to be noted
    }

    /**
     * Sets the field security with the XML - parsing 
     * through the XML and filling in the hashtable.
     *
     */
    public void setFieldSecurity(String p_securityXml)
    {
        //parse through and set up the hash table
        if (p_securityXml != null &&
            p_securityXml.length() > 0) 
        {
            try
            {
                Element doc = getDocumentRoot(p_securityXml);
                NodeList nl = doc.getElementsByTagName(FIELD_TAG);
                
                m_fs.clear();
                for (int i=0; i < nl.getLength(); i++)
                {
                    Element e = (Element)nl.item(i);
                    Node field = (Node)e.getFirstChild();
                    String fieldName = field.getNodeValue();
                    
                    String accessLevel = e.getAttribute(ACCESS_LEVEL_ATTRIBUTE);
                    // if null or not a valid access level set to hidden
                    if (!isValidAccessLevel(accessLevel))
                    {   
                        accessLevel = HIDDEN;
                    }
                    m_fs.put(fieldName, accessLevel);   
                }
                m_securityXml = p_securityXml;

            } catch (Exception e)
            {
                c_logger.error("Failed to parse through the security XML " +
                               p_securityXml, e);
                // leave the securities as they were before
            }   
        }
        else
        {
            m_securityXml = new String();
            m_fs.clear();
        }
        m_htChanged = false;    // the hashtable was updated and the XML too
                                // so no change to be noted 
    }
        
    /** 
     * Gets the field access associated with the key.
     * Works like a "get" on a Hashtable - pass in the key
     * and get the value.
     */
    public String get(String p_fieldName)
    {
        return (String)m_fs.get(p_fieldName);
    }

    /**
     * Puts in the access level for a specific field.
     * It'll override the value if the field already exists
     * in the security list.  This works like "put" on a Hashtable
     * with the field name the key and the securtiy assess as the value.
     */
    public void put(String p_fieldName, String p_accessLevel)
    {                                
        if (!isValidAccessLevel(p_accessLevel))
        {
            p_accessLevel = HIDDEN;
        }
        m_fs.put(p_fieldName, p_accessLevel);
        m_htChanged = true;
    }

    public String toString()
    {
        if (m_htChanged == true)
        {
            // regenerates the XML
            setFieldSecurity(m_fs);
        }
        return m_securityXml;
    }

    /**
     * For debug purposes.
     */
    public String getHashtableString()
    {
        StringBuffer htString = new StringBuffer();
       htString.append("HASHTABLE CONTENTS:\n");
       Set keys = m_fs.keySet();
       for (Iterator ki = keys.iterator() ; ki.hasNext() ; )
       {
           String name = (String)ki.next();
           String access = (String)m_fs.get(name);
           htString.append("  ");
           htString.append(name);
           htString.append("=");
           htString.append(access);
           htString.append("\n");
       }

       return htString.toString();
    }

    // =============================private methods===============================

    private Element getDocumentRoot(String p_xmlString)
            throws Exception
    {
        StringReader sr = new StringReader(p_xmlString);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        return parser.getDocument().getDocumentElement();    
    }

    private boolean isValidAccessLevel(String p_access)
    {
        if (p_access != null &&
            (p_access.equals(SHARED) ||
             p_access.equals(LOCKED) ||
             p_access.equals(HIDDEN)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

	public String getSecurityXml()
	{
		return m_securityXml;
	}

	public void setSecurityXml(String xml)
	{
	    setFieldSecurity(xml);
	}  
}


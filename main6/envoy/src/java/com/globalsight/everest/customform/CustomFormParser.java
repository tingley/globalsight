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

package com.globalsight.everest.customform;

// globalsight

// java
import java.io.StringReader;
import java.util.Hashtable;

import org.apache.log4j.Logger;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the custom form XML to verify section and custom fields.
 * It provides access to the field names and their types.
 */
public class CustomFormParser
{
    /**
     * Constants for custom pages
     */
    public static final String CUSTOM_PAGE = "customPage";
    public static final String CUSTOM_PAGE_TITLE = "title";
    public static final String SECTION = "section";
    public static final String SECTION_NAME = "name";
    public static final String FIELD = "field";
    public static final String FIELD_LABEL = "label";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TYPE_TEXT = "Text";
    public static final String FIELD_TYPE_CHECKBOX = "Checkbox";
    public static final String FIELD_TYPE_RADIO = "Radio";

    private static final Logger c_logger = 
        Logger.getLogger(CustomFormParser.class.getName());

    /**
     * Get the XML, parse it and return handle to the DOM
     */
    public static Document getDocument(CustomForm p_cf)
    {
        if (p_cf == null) 
        {
            return null;
        }
        try
        {
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(new InputSource(new StringReader(p_cf.getXmlDesign())));
            return parser.getDocument();
        }
        catch (org.xml.sax.SAXNotRecognizedException e)
        {
            c_logger.error(e.getMessage(), e);
            return null;
        }
        catch (org.xml.sax.SAXException e1)
        {
            c_logger.error(e1.getMessage(), e1);
            return null;
        }
        catch (java.io.IOException e2)
        {
            c_logger.error(e2.getMessage(), e2);
            return null;
        }
    }

    /**
     * Return a list of custom field names and their types.  
     * The key in the hashtable is the name which is represented
     * as <sectionName>.<fieldName> for uniqueness.  This allows different
     * sections to have the same field names.  The value is the type which
     * is a String (type, checkbox).
     *
     * xml format
     * <section>
     *   <name>sectionName</name>
     *     <field>
     *       <label>labelName</label>
     *       <type>type</type>
     *     </field>
     * </section>
     */
    public static Hashtable getCustomFields(CustomForm p_cf)
    {
        Document doc = getDocument(p_cf);
        String sectionName = null;
        String fieldName = null;
        Hashtable fields = new Hashtable();

        NodeList sections = doc.getElementsByTagName(SECTION);
        for (int i=0; i < sections.getLength(); i++)
        {
            Node section = sections.item(i);
            NodeList children = section.getChildNodes();
            for (int j=0; j < children.getLength(); j++)
            {
                Node item = children.item(j);
                if (item.getNodeName().equals(SECTION_NAME))
                {
                    sectionName = item.getFirstChild().getNodeValue();
                }
                else if (item.getNodeName().equals(FIELD))
                {
                    NodeList fieldChildren = item.getChildNodes();
                    String fieldType = FIELD_TYPE_TEXT;
                    for (int k=0; k < fieldChildren.getLength(); k++)
                    {
                        Node field = fieldChildren.item(k);
                        if (field.getNodeName().equals(FIELD_LABEL))
                        {
                            fieldName = sectionName + "." +
                                 field.getFirstChild().getNodeValue();
                        }
                        else if (field.getNodeName().equals(FIELD_TYPE))
                        {
                            fieldType = field.getFirstChild().getNodeValue();
                        }
                    }
                    if (fieldName != null)
                    {
                        if (fieldType.equals(FIELD_TYPE_RADIO))
                            fields.put(sectionName, fieldType);    
                        else
                            fields.put(fieldName, fieldType);    
                    }
                }
            }
        }
        return fields;
    }
}

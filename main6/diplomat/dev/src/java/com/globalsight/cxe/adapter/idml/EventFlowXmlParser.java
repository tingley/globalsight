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
package com.globalsight.cxe.adapter.idml;

//JDK
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.diplomat.util.XmlUtil;

/**
 * The EventFlowXmlParser is a class that the desktop helpers can use to parse
 * the EventFlowXml. This is not intended for a general use EventFlowXml Parser,
 * although it could be modified to be such.
 */
public class EventFlowXmlParser
{
    // CONSTANTS

    /**
     * The name of the category in the event flow xml for any data specific to
     * the DesktopApplicationAdapters.
     */
    public static final String EFXML_DA_CATEGORY_NAME = "PdfAdapter";

    /**
     * Constructor for use by sub classes <br>
     * 
     * @param p_workingDir
     *            -- the main working directory where the conversion server
     *            looks for files
     * @param p_eventFlowXml
     *            -- the EventFlowXml
     * @param p_content
     *            -- the content (whether GXML or Native)
     */
    public EventFlowXmlParser(String p_eventFlowXml)
    {
        m_eventFlowXml = p_eventFlowXml;
    }

    /**
     * Gets the event flow XML. <br>
     * 
     * @return eventFlowXml
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * Gets the named element from the DOM structure <br>
     * 
     * @param p_elementName
     *            -- the name of the element to get
     * @return the Element
     */
    protected Element getSingleElement(String p_elementName)
    {
        return getSingleElement(m_rootElement, p_elementName);
    }

    /**
     * Gets the named element from the specified DOM structure <br>
     * 
     * @param p_elementName
     *            -- the name of the element to get
     * @return the Element
     */
    protected static Element getSingleElement(Element p_rootElement,
            String p_elementName)
    {
        return (Element) p_rootElement.getElementsByTagName(p_elementName)
                .item(0);
    }

    /**
     * Sets the value of a single element. For example,
     * setSingleElementValue(rootElement,"postMergeEvent",postMergeEvent) <br>
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to set
     * @param p_value
     *            -- the single value for this element
     * @return the old value of the element
     */
    public static String setSingleElementValue(Element p_rootElement,
            String p_elementName, String p_value)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        String originalValue = e.getFirstChild().getNodeValue();
        e.getFirstChild().setNodeValue(p_value);
        return originalValue;
    }

    /**
     * Sets the value of a single element using. For example,
     * setSingleElementValue("postMergeEvent",postMergeEvent) <br>
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to set
     * @param p_value
     *            -- the single value for this element
     * @return the old value of the element
     */
    public String setSingleElementValue(String p_elementName, String p_value)
    {
        return setSingleElementValue(m_rootElement, p_elementName, p_value);
    }

    /**
     * Gets the value of a single element. For example,
     * getSingleElementValue(rootElement,"postMergeEvent") <br>
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to get
     * @return the value of the element
     */
    public static String getSingleElementValue(Element p_rootElement,
            String p_elementName)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        return e.getFirstChild().getNodeValue();
    }

    /**
     * Gets the value of a single element. For example,
     * getSingleElementValue("postMergeEvent") <br>
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to get
     * @return the value of the element
     */
    public String getSingleElementValue(String p_elementName)
    {
        return getSingleElementValue(m_rootElement, p_elementName);
    }

    /**
     * Adds a category with the name to the Event Flow XML <br>
     * 
     * @param p_name
     *            -- the name of the category to add
     * @return Element -- the newly added category element
     */
    public Element addCategory(String p_name)
    {
        Element categoryElement = m_document.createElement("category");
        categoryElement.setAttribute("name", p_name);
        m_rootElement.appendChild(categoryElement);
        return categoryElement;
    }

    /**
     * Finds the given category element with the attribute name equal to p_name <br>
     * 
     * @param p_name
     *            --the name of the category to find
     * @return Element
     */
    public Element getCategory(String p_name)
    {
        NodeList nl = m_rootElement.getElementsByTagName("category");
        for (int i = 0; i < nl.getLength(); i++)
        {
            Element e = (Element) nl.item(i);
            if (e.getAttribute("name").equals(p_name))
                return e;
        }

        throw new NoSuchElementException("Category " + p_name
                + " does not exist.");
    }

    /**
     * Gets the value of the given data attribute of the given category. <br>
     * 
     * @param p_categoryElement
     *            -- the category
     * @param p_daName
     *            -- the value of the name attribute for the desired da element
     * @return String -- the value(s) associated with the data attribute element
     */
    public String[] getCategoryDaValue(Element p_categoryElement,
            String p_daName)
    {
        NodeList nl = p_categoryElement.getElementsByTagName("da");
        String[] values = null;

        for (int i = 0; i < nl.getLength(); i++)
        {
            Element daElement = (Element) nl.item(i);
            if (daElement.getAttribute("name").equals(p_daName))
            {
                NodeList dvs = daElement.getElementsByTagName("dv");
                values = new String[dvs.getLength()];
                for (int j = 0; j < values.length; j++)
                {
                    Element dv = (Element) dvs.item(j);
                    values[j] = dv.getFirstChild().getNodeValue();
                }
                break;
            }
        }

        return values;
    }

    /**
     * Returns the actual data attribute of the given category <br>
     * 
     * @param p_categoryElement
     *            The category element
     * @param p_daName
     *            The value of the name attribute for the desired da element.
     * @return The actual data attribute element.
     */
    public Element getCategoryDa(Element p_categoryElement, String p_daName)
    {
        NodeList nl = p_categoryElement.getElementsByTagName("da");
        Element da = null;

        for (int i = 0; da == null && i < nl.getLength(); i++)
        {
            Element daElement = (Element) nl.item(i);
            if (daElement.getAttribute("name").equals(p_daName))
            {
                da = daElement;

            }
        }

        return da;
    }

    /**
     * Utility function to create an EventFlowXml da element containing the
     * given values. <br>
     * 
     * @param p_name
     *            -- the attribute name of the da element
     * @param p_values
     *            -- the dv values of the da element
     * @return the newly made Element
     */
    public Element makeEventFlowXmlDaElement(String p_name, String[] p_values)
    {
        Element da = m_document.createElement("da");
        da.setAttribute("name", p_name);
        Element dv = null;
        for (int i = 0; i < p_values.length; i++)
        {
            dv = m_document.createElement("dv");
            dv.appendChild(m_document.createTextNode(p_values[i]));
            da.appendChild(dv);
        }

        return da;
    }

    /**
     * Re-sets the eventFlowXml String after serializing the DOM structure out
     * as a string. <br>
     * 
     * @throws IOException
     */
    public void reconstructEventFlowXmlStringFromDOM() throws IOException
    {
        OutputFormat oformat = new OutputFormat(m_document, "UTF-8", true);
        oformat.setOmitDocumentType(true);
        oformat.setOmitComments(false);
        oformat.setOmitXMLDeclaration(true);
        oformat.setPreserveSpace(true);
        oformat.setIndenting(false);
        XMLSerializer xmlSerializer = new XMLSerializer(oformat);
        int bufferSize = m_eventFlowXml.length() * 2; // just double the buffer
                                                      // size to be safe
        StringWriter stringWriter = new StringWriter(bufferSize);
        stringWriter.write(XmlUtil.formattedEventFlowXmlDtd()); // restore the
                                                                // DTD
        xmlSerializer.setOutputCharStream(stringWriter);
        xmlSerializer.serialize(m_document);
        m_eventFlowXml = stringWriter.toString();
    }

    /**
     * Parses the EventFlowXml to set or re-set internal values <br>
     * 
     * @throws SAXException
     *             , IOException
     */
    public void parse() throws SAXException, IOException
    {
        findRootElement();
        m_displayName = getSingleElementValue(m_rootElement, "displayName");
        m_srcLocale = findLocale("source");
        m_targetLocale = findLocale("target");
    }

    /**
     * Finds and sets the root Element for EventFlowXml parsing <br>
     * 
     * @throws SAXException
     */
    private void findRootElement() throws SAXException, IOException
    {
        StringReader sr = new StringReader(m_eventFlowXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        m_document = parser.getDocument();
        m_rootElement = m_document.getDocumentElement();
    }

    /**
     * Finds the source or target locale <br>
     * 
     * @param p_type
     *            is "source" or "target"
     * @return locale
     */
    private String findLocale(String p_type)
    {
        NodeList nl = m_rootElement.getElementsByTagName(p_type);
        Element e = (Element) nl.item(0);
        nl = e.getElementsByTagName("locale");
        Element localeElement = (Element) nl.item(0);
        return localeElement.getFirstChild().getNodeValue();
    }

    /******************************************/
    /*** Mutators/Accessors for member data ***/
    /******************************************/

    /**
     * Returns the root Element of the Event Flow XML DOM tree for parsing.
     */
    public Element getRootElement()
    {
        return m_rootElement;
    }

    /**
     * Returns the DOM Document object for the EventFlowXml
     */
    public Document getDocument()
    {
        return m_document;
    }

    /**
     * Returns the display name of the document.
     */
    public String getDisplayName()
    {
        return m_displayName;
    }

    /**
     * Returns the source locale
     */
    public String getSourceLocale()
    {
        return m_srcLocale;
    }

    /**
     * Returns the target locale
     */
    public String getTargetLocale()
    {
        return m_targetLocale;
    }

    /**
     * Sets the post merge event in the DOM structure <br>
     * 
     * @param p_postMergeEvent
     *            -- the new post merge event
     * @return the old post merge event
     */
    public String setPostMergeEvent(String p_postMergeEvent)
    {
        return setSingleElementValue("postMergeEvent", p_postMergeEvent);
    }

    /**
     * Gets the post merge event from the DOM structure <br>
     * 
     * @return the post merge event
     */
    public String getPostMergeEvent()
    {
        return getSingleElementValue("postMergeEvent");
    }

    /**
     * Gets the ID of the data source profile (most likely file profile)
     */
    public String getSourceDataSourceId()
    {
        return getSingleElement("source").getAttribute("dataSourceId");
    }

    /**
     * Gets the format type from the <source> element <br>
     * 
     * @return "word","xml",etc.
     */
    public String getSourceFormatType()
    {
        return getSingleElement("source").getAttribute("formatType");
    }

    /**
     * Sets the format type in the <source> element <br>
     * 
     * @param p_newFormatType
     *            -- the new format ("xml","word",etc.)
     * @return the previous value of formatType
     */
    public String setSourceFormatType(String p_newFormatType)
    {
        Element s = getSingleElement("source");
        String oldFormatType = s.getAttribute("formatType");
        s.setAttribute("formatType", p_newFormatType);
        return oldFormatType;
    }

    /**
     * Sets the source charset in the DOM structure i.e.
     * <source><charset>UTF-8</charset></source> <br>
     * 
     * @param p_encoding
     *            -- the new encoding (charset) to use
     * @return the original value
     */
    public String setSourceEncoding(String p_encoding)
    {
        return setSingleElementValue(getSingleElement("source"), "charset",
                p_encoding);
    }

    /**
     * Gets the source charset from the DOM structure i.e.
     * <source><charset>UTF-8</charset></source> <br>
     * 
     * @return the encoding (charset)
     */
    public String getSourceEncoding()
    {
        return getSingleElementValue(getSingleElement("source"), "charset");
    }

    /**
     * Set the target file name.
     * 
     * @param p_filename
     *            The file name to give the target file upon export.
     */
    public void setTargetFileName(String p_filename)
    {
        Element t = getSingleElement("target");
        Element oldFileElement = getCategoryDa(t, "Filename");
        String[] filename =
        { p_filename };
        Element newFileElement = makeEventFlowXmlDaElement("Filename", filename);
        t.replaceChild(newFileElement, oldFileElement);
    }

    /**
     * Get the filename for the target file.
     */
    public String getTargetFileName()
    {
        Element trg = getSingleElement("target");
        String fileName = getCategoryDaValue(trg, "Filename")[0];
        return fileName;
    }

    /*****************************/
    /*** Private Member Data ***/
    /*****************************/
    private String m_eventFlowXml = null;
    private Document m_document = null;
    private Element m_rootElement = null;
    private String m_displayName = null;
    private String m_srcLocale = null;
    private String m_targetLocale = null;
    private String m_workingDir = null;
    private String m_prefixFileName = null;
}

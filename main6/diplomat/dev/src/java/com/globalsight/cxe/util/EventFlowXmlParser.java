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
package com.globalsight.cxe.util;

//JDK
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
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
 * The EventFlowXmlParser can be used by all adapters to wander through the
 * EventFlowXml structure and modify or read values.
 */
public class EventFlowXmlParser
{
    //
    // Private Member Data
    //

    private String m_eventFlowXml = null;
    private Document m_document = null;
    private Element m_rootElement = null;
    private String m_displayName = null;
    private String m_srcLocale = null;
    private String m_targetLocale = null;

    /** Creates an EventFlowXmlParser */
    public EventFlowXmlParser()
    {
        m_eventFlowXml = null;
    }

    /**
     * Gets the event flow XML.
     * 
     * @return eventFlowXml
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * Gets the named element from the DOM structure
     * 
     * @param p_elementName
     *            -- the name of the element to get
     * @return the Element
     */
    public Element getSingleElement(String p_elementName)
    {
        return getSingleElement(m_rootElement, p_elementName);
    }

    /**
     * Gets the named element from the specified DOM structure
     * 
     * @param p_elementName
     *            -- the name of the element to get
     * @return the Element
     */
    static protected Element getSingleElement(Element p_rootElement,
            String p_elementName)
    {
        return (Element) p_rootElement.getElementsByTagName(p_elementName)
                .item(0);
    }

    /**
     * Sets the value of a single element. For example,
     * setSingleElementValue(rootElement, "postMergeEvent", postMergeEvent)
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to set
     * @param p_value
     *            -- the single value for this element
     * @return the old value of the element
     */
    static public String setSingleElementValue(Element p_rootElement,
            String p_elementName, String p_value)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        String originalValue = e.getFirstChild().getNodeValue();
        e.getFirstChild().setNodeValue(p_value);
        return originalValue;
    }

    /**
     * Sets the value of a single element using. For example,
     * setSingleElementValue("postMergeEvent",postMergeEvent)
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
     * getSingleElementValue(rootElement,"postMergeEvent")
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to get
     * @return the value of the element
     */
    static public String getSingleElementValue(Element p_rootElement,
            String p_elementName)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        if (e != null && e.getFirstChild() != null)
        {
            return e.getFirstChild().getNodeValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the value of a single element. For example,
     * getSingleElementValue("postMergeEvent")
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
     * Adds a category with the name to the Event Flow XML
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
     * Adds the alternate target page to the event flow xml
     * 
     * @param p_locale
     *            locale
     * @param p_pageName
     *            file path of the alternate file
     */
    public void addAlternateTargetPage(String p_locale, String p_pageName)
    {
        // <alternateTargetPage locale="fr_FR"> E:\foo.gif
        // </alternateTargetPage>
        Element altElement = m_document.createElement("alternateTargetPage");
        altElement.setAttribute("locale", p_locale);
        altElement.appendChild(m_document.createTextNode(p_pageName));
        m_rootElement.appendChild(altElement);
    }

    /**
     * Returns a hashmap containing a mapping of ISO locale codes to an
     * alternate target page.
     * 
     * @return HashMap
     */
    public HashMap getAlternateTargetPages()
    {
        NodeList nl = m_rootElement.getElementsByTagName("alternateTargetPage");
        HashMap map = new HashMap();
        for (int i = 0; i < nl.getLength(); i++)
        {
            Element a = (Element) nl.item(i);
            String locale = a.getAttribute("locale");
            String pageName = a.getFirstChild().getNodeValue();
            map.put(locale, pageName);
        }

        return map;
    }

    /**
     * Finds the given category element with the attribute name equal to p_name
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
            {
                return e;
            }
        }

        throw new NoSuchElementException("Category " + p_name
                + " does not exist.");
    }

    /**
     * Gets the value of the given data attribute of the given category.
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
     * Utility function to create an EventFlowXml da element containing the
     * given values.
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
        // just double the buffer size to be safe
        int bufferSize = m_eventFlowXml.length() * 2;
        StringWriter stringWriter = new StringWriter(bufferSize);
        // restore the DTD
        stringWriter.write(XmlUtil.formattedEventFlowXmlDtd());
        xmlSerializer.setOutputCharStream(stringWriter);
        xmlSerializer.serialize(m_document);

        m_eventFlowXml = stringWriter.toString();
    }

    /**
     * Parses the EventFlowXml to set or re-set internal values
     * 
     * @throws SAXException
     * @throws IOException
     */
    public void parse(String p_eventFlowXml) throws SAXException, IOException
    {
        m_eventFlowXml = p_eventFlowXml;
        findRootElement();
        m_displayName = getSingleElementValue(m_rootElement, "displayName");
        m_srcLocale = findLocale("source");
        m_targetLocale = findLocale("target");
    }

    /**
     * Finds and sets the root Element for EventFlowXml parsing
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
     * Finds the source or target locale
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
     * Sets the display name (external page Id)
     * 
     * @param p_displayName
     *            -- the new display name to use
     * @return the original value
     */
    public String setDisplayName(String p_displayName)
    {
        return setSingleElementValue(getSingleElement("batchInfo"),
                "displayName", p_displayName);
    }

    /**
     * Sets the import page count
     * 
     * @param p_pageCount
     * @return old value
     * @deprecated Use setImportPageCount() instead
     */
    // public String setPageCount(String p_pageCount)
    // {
    // return setImportPageCount(p_pageCount);
    // }

    /**
     * Sets the import page number
     * 
     * @param p_pageNum
     * @return old value
     * @deprecated Use setImportPageNumber() instead
     */
    // public String setPageNumber(String p_pageNum)
    // {
    // return setImportPageNumber(p_pageNum);
    // }

    /**
     * Sets the import page count for batches
     * 
     * @param p_pageCount
     * @return old value
     */
    public String setImportPageCount(String p_pageCount)
    {
        return setSingleElementValue(getSingleElement("batchInfo"),
                "pageCount", p_pageCount);
    }

    public Integer getImportPageCount()
    {
        String pageCount = getSingleElementValue(getSingleElement("batchInfo"),
                "pageCount");
        return new Integer(pageCount);
    }

    /**
     * Sets the import page number for batches
     * 
     * @param p_pageNum
     * @return old value
     */
    public String setImportPageNumber(String p_pageNum)
    {
        return setSingleElementValue(getSingleElement("batchInfo"),
                "pageNumber", p_pageNum);
    }

    public Integer getImportPageNumber()
    {
        String pageNum = getSingleElementValue(getSingleElement("batchInfo"),
                "pageNumber");
        return new Integer(pageNum);
    }

    /**
     * Sets the import document page count for batches (Office doc exploded to
     * multiple files)
     * 
     * @param p_pageCount
     * @return old value
     */
    public String setImportDocPageCount(String p_docPageCount)
    {
        return setSingleElementValue(getSingleElement("batchInfo"),
                "docPageCount", p_docPageCount);
    }

    public Integer getImportDocPageCount()
    {
        String pageCount = getSingleElementValue(getSingleElement("batchInfo"),
                "docPageCount");
        return new Integer(pageCount);
    }

    /**
     * Sets the import document page number for batches (Office doc exploded to
     * multiple files)
     * 
     * @param p_pageNum
     * @return old value
     */
    public String setImportDocPageNumber(String p_docPageNum)
    {
        return setSingleElementValue(getSingleElement("batchInfo"),
                "docPageNumber", p_docPageNum);
    }

    public Integer getImportDocPageNumber()
    {
        String pageNum = getSingleElementValue(getSingleElement("batchInfo"),
                "docPageNumber");
        return new Integer(pageNum);
    }

    public String setBaseHref(String p_baseHref)
    {
        return setSingleElementValue(getSingleElement("batchInfo"), "baseHref",
                p_baseHref);
    }

    public String setBatchId(String p_batchId)
    {
        return setSingleElementValue(getSingleElement("batchInfo"), "batchId",
                p_batchId);
    }

    public String setJobName(String p_jobName)
    {
        return setSingleElementValue(getSingleElement("batchInfo"), "jobName",
                p_jobName);
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
     * Sets the post merge event in the DOM structure
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
     * Gets the post merge event from the DOM structure
     * 
     * @return the post merge event
     */
    public String getPostMergeEvent()
    {
        return getSingleElementValue("postMergeEvent");
    }

    /**
     * Gets the pre-merge event from the DOM structure
     * 
     * @return the pre-merge event
     */
    public String getPreMergeEvent()
    {
        return getSingleElementValue("preMergeEvent");
    }

    /**
     * Gets the ID of the data source profile (most likely file profile)
     */
    public String getSourceDataSourceId()
    {
        return getSingleElement("source").getAttribute("dataSourceId");
    }

    /**
     * Gets the data source type
     */
    public String getSourceDataSourceType()
    {
        return getSingleElement("source").getAttribute("dataSourceType");
    }

    /**
     * Gets the L10nProfileId
     */
    public String getL10nProfileId()
    {
        return getSingleElement("batchInfo").getAttribute("l10nProfileId");
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

    public String getSourceFormatName()
    {
        return getSingleElement("source").getAttribute("formatName");
    }

    /**
     * Gets the import initiator id from the <source> element <br>
     * 
     * @return import initiator id.
     */
    public String getSourceImportInitiatorId()
    {
        return getSingleElement("source").getAttribute("importInitiatorId");
    }

    /**
     * Sets the format type in the <source> element
     * 
     * @param p_newFormatType
     *            -- the new format ("xml", "word", etc.)
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
     * <source><charset>UTF-8</charset></source>
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
     * <source><charset>UTF-8</charset></source>
     * 
     * @return the encoding (charset)
     */
    public String getSourceEncoding()
    {
        return getSingleElementValue(getSingleElement("source"), "charset");
    }

    /**
     * Gets the database mode of the export. Content is written or commited back
     * to the data source for "final" mode
     */
    public String getDatabaseMode()
    {
        Element e = getSingleElement(m_rootElement, "target");
        String databaseMode = e.getAttribute("databaseMode");
        return databaseMode;
    }

    public String getBatchId()
    {
        return getSingleElementValue(getSingleElement("batchInfo"), "batchId");
    }

    public String getJobName()
    {
        return getSingleElementValue(getSingleElement("batchInfo"), "jobName");
    }

    public String getJobId()
    {
        return getSingleElementValue(getSingleElement("batchInfo"), "jobId");
    }

    public String getJobUuid()
    {
        try
        {
            return getSingleElementValue(getSingleElement("batchInfo"), "uuid");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Integer getExportPageNum()
    {
        String pageNum = getSingleElementValue(
                getSingleElement("exportBatchInfo"), "exportBatchPageNum");
        return new Integer(pageNum);
    }

    public Integer getExportPageCount()
    {
        String pageCount = getSingleElementValue(
                getSingleElement("exportBatchInfo"), "exportBatchPageCount");
        return new Integer(pageCount);
    }

    public String getExportBatchId()
    {
        return getSingleElementValue(getSingleElement("exportBatchInfo"),
                "exportBatchId");
    }

    public String getDataValue(String p_type, String p_daName)
    {
        Element element = getSingleElement(p_type);
        NodeList da = element.getElementsByTagName("da");
        String value = null;
        for (int i = 0; value == null && i < da.getLength(); i++)
        {
            Element e = (Element) da.item(i);
            if (e.getAttribute("name").equals(p_daName))
            {
                Element dv = (Element) e.getElementsByTagName("dv").item(0);
                value = dv.getFirstChild().getNodeValue();
            }
        }

        return value;
    }

    /**
     * Get the Server Name
     */
    public String getServerName()
    {
        return getSingleElementValue("ServerName");
    }

    /**
     * Get the Store name
     */
    public String getStoreName()
    {
        return getSingleElementValue("StoreName");
    }

    /**
     * Get the converted file name.
     */
    public String getConvertedFileName()
    {
        return getSingleElementValue(getSingleElement("source"),
                "ConvertedFileName");
    }

    /**
     * Get the size of the source file.
     */
    public String getSourceFileSize()
    {
        return getSingleElementValue(getSingleElement("source"), "FileSize");
    }

    public String getSafeBaseFileName()
    {
        Element category = getSingleElement("category");
        if (category == null)
        {
            return null;
        }
        return getCategoryDaValue(category, "safeBaseFileName")[0];
    }

    public String getFormatType()
    {
        Element category = getSingleElement("category");
        if (category == null)
        {
            return null;
        }
        return getCategoryDaValue(category, "formatType")[0];
    }
}

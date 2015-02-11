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
package com.globalsight.cxe.adapter.database.target;

import com.globalsight.cxe.adapter.database.InvalidTaskXmlException;
import com.globalsight.cxe.adapter.database.TaskXml;

import com.globalsight.diplomat.util.XmlUtil;


import java.io.StringReader;

import java.util.Vector;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * DbRecordProxyBuilder, constructs instances of class DbRecordProxy from the
 * contents of a PaginatedResultSetXml string.
 */
public class DbRecordProxyBuilder
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String PRS_XML = "paginatedResultSetXml";
    private static final String EF_XML = "eventFlowXml";
    private static final String RECORD = "record";
    private static final String ACQ_PARM = "acqSqlParm";
    private static final String COLUMN = "column";
    private static final String ROW = "row";
    private static final String NAME = "name";
    private static final String TABLE_NAME = "tableName";
    private static final String RECORD_PROFILE_ID = "recordProfileId";
    private static final String CONTENT = "content";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String LOCALE = "locale";
    private static final String DB_MODE = "databaseMode";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private TaskXml m_taskXml;
    private Document m_prsXmlDoc;
    private Document m_efXmlDoc;
    private String m_sqlType;
    private String m_srcLang;
    private String m_tgtLang;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Creates a new instance of the Parser.
     */
    public DbRecordProxyBuilder()
    {
        super();
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Set the value of the task Xml that is to be converted into record
     * proxy(ies).
     *
     * @param p_taskXml the new value to use.
     */
    public void setTaskXml(TaskXml p_taskXml)
    throws InvalidTaskXmlException
    {
        m_taskXml = p_taskXml;
        m_sqlType = null;
        m_srcLang = null;
        m_tgtLang = null;
        domify();
    }

    /**
     * Return a vector of proxies for all record elements in the task Xml.
     *
     * @return the proxies corresponding to the current task Xml.
     *
     * @throws InvalidTaskXmlException if any problems occur.
     */
    public Vector buildProxies()
    throws InvalidTaskXmlException
    {
        Vector v = new Vector();
        NodeList recNodes = m_prsXmlDoc.getDocumentElement().getElementsByTagName(RECORD);
        conditionallyThrowException(recNodes, noPaginatedResultSetXmlRecords());

        for (int i = 0 ; i < recNodes.getLength() ; i++)
        {
            Element record = (Element)recNodes.item(i);
            DbRecordProxy recProxy = new DbRecordProxy();
            recProxy.setSqlType(sqlType());
            recProxy.setSourceLanguage(sourceLanguage());
            recProxy.setTargetLanguage(targetLanguage());
            recProxy.setRecordProfileId(record.getAttribute(RECORD_PROFILE_ID));
            addAcqSqlParm(recProxy, record);
            addColumns(recProxy, record);
            v.addElement(recProxy);
        }
        return v;
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Return the paginated result set xml string. */
    private String paginatedResultSetXml()
    {
        return m_taskXml.getPaginatedResultSetXml();
    }

    /* Return the domified paginated result set xml document. */
    private Document paginatedResultSetXmlDocument()
    {
        return m_prsXmlDoc;
    }

    /* Return the event flow xml string. */
    private String eventFlowXml()
    {
        return m_taskXml.getEventFlowXml();
    }

    /* Return the domified event flow xml document. */
    private Document eventFlowXmlDocument()
    {
        return m_efXmlDoc;
    }

    /* Convert the xml string into a pair of DOM objects */
    private void domify()
    throws InvalidTaskXmlException
    {
        StringReader reader;
        InputSource source;
        try
        {
            reader = new StringReader(paginatedResultSetXml());
            source = new InputSource(reader);
	    DOMParser parser = new DOMParser();
	    parser.setFeature("http://xml.org/sax/features/validation", true); //validate
	    parser.parse(source);
	    m_prsXmlDoc = parser.getDocument();
            
	    reader = new StringReader(eventFlowXml());
            source = new InputSource(reader);
	    DOMParser parser2 = new DOMParser();
	    parser2.setFeature("http://xml.org/sax/features/validation", true); //validate
	    parser2.parse(source);
            m_efXmlDoc = parser2.getDocument();
        }
        catch (Exception e)
        {
            throw new InvalidTaskXmlException(e);
        }
    }

    /* Extract the source or target language value */
    private String language(String p_tagName)
    throws InvalidTaskXmlException
    {
        NodeList nodes =
            m_efXmlDoc.getDocumentElement().getElementsByTagName(p_tagName);
        conditionallyThrowException(nodes, noEventFlowXmlRecords());
        NodeList children = 
            ((Element)nodes.item(0)).getElementsByTagName(LOCALE);
        conditionallyThrowException(children, noEventFlowXmlRecords());
        return children.item(0).getFirstChild().getNodeValue();
    }

    /* If the given nodelist is null or empty throw an exception with */
    /* the given string as its message. */
    private void conditionallyThrowException(NodeList p_nl, String p_str)
    throws InvalidTaskXmlException
    {
        if (p_nl == null || p_nl.getLength() == 0)
        {
            InvalidTaskXmlException ex = new InvalidTaskXmlException(p_str);
            ex.fillInStackTrace();
            throw ex;
        }
    }

    /* Return a constructed error string embodying the given string. */
    private String noRecords(String p_subStr)
    {
        return ("Supplied " + p_subStr + " contains no records.");
    }

    /* Construct an error string for missing event flow xml records. */
    private String noEventFlowXmlRecords()
    {
        return noRecords(EF_XML);
    }

    /* Construct an error string for missing paginated result set xml records. */
    private String noPaginatedResultSetXmlRecords()
    {
        return noRecords(PRS_XML);
    }

    /* Get the sql type from the event flow xml */
    private String sqlType()
    throws InvalidTaskXmlException
    {
        if (m_sqlType == null)
        {
            NodeList targets = 
                m_efXmlDoc.getDocumentElement().getElementsByTagName(TARGET);
            conditionallyThrowException(targets, noEventFlowXmlRecords());
            Element el = (Element)targets.item(0);
            m_sqlType = el.getAttribute(DB_MODE);
        }
        return m_sqlType;
    }

    /* Get the target language from the event flow xml */
    private String targetLanguage()
    throws InvalidTaskXmlException
    {
        if (m_tgtLang == null)
        {
            m_tgtLang = language(TARGET);
        }
        return m_tgtLang;
    }

    /* Get the source language from the event flow xml */
    private String sourceLanguage()
    throws InvalidTaskXmlException
    {
        if (m_srcLang == null)
        {
            m_srcLang = language(SOURCE);
        }
        return m_srcLang;
    }

    /* Add the acquisition Sql parameter string, if it exists */
    private void addAcqSqlParm(DbRecordProxy p_proxy, Element p_record)
    {
        NodeList nl = p_record.getElementsByTagName(ACQ_PARM);
        Element acqSqlParmElement = (Element) nl.item(0);
        p_proxy.setAcquisitionSqlParameter(acqSqlParmElement.getFirstChild().getNodeValue());
    }

    /* Add all the columns for the current record to the given proxy */
    private void addColumns(DbRecordProxy p_proxy, Element p_record)
    {
        NodeList colNodes = columnNodes(p_record);
        for (int i = 0 ; i < colNodes.getLength() ; i++)
        {
            DbColumnProxy colProxy = new DbColumnProxy();
            Element column = (Element)colNodes.item(i);
            colProxy.setName(column.getAttribute(NAME));
            colProxy.setTableName(column.getAttribute(TABLE_NAME));
            NodeList children = column.getElementsByTagName(CONTENT);
            Node content = children.item(0).getFirstChild();
            colProxy.setContent((content == null) ? "" : unescapeString(content.getNodeValue()));
            p_proxy.addColumn(colProxy);
        }
    }

    /* Return the list of non-context columns associated with the given record */
    private NodeList columnNodes(Element p_record)
    {
        NodeList colNodes = p_record.getElementsByTagName(COLUMN);
        FilteredNodeList fnl = new FilteredNodeList();
        for (int i = 0 ; i < colNodes.getLength(); i++)
        {
            Node n = colNodes.item(i);
            if (!n.getParentNode().getNodeName().equals(ROW))
            {
                fnl.addNode(n);
            }
        }
        return fnl;
    }

    /* Unescape the contents of the given string and return the result. */
    private String unescapeString(String p_str)
    {
        return XmlUtil.unescapeString(p_str);
    }
}


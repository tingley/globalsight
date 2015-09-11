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
package com.globalsight.cxe.adapter.cap;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;

/**
 * Handles the interaction with CAP for initiating Import
 */
public class CapImporter
{
    // ////////////////////////////////////
    // Constants //
    // ////////////////////////////////////

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////

    private org.apache.log4j.Logger m_logger;

    private String m_eventFlowXml = null;
    private String m_displayName = null;
    private String m_messageId = null;
    private String m_exportBatchId = null;

    // information needed for the L10nRequestXml
    private String m_dataSourceType = null;
    private String m_dataSourceId = null;
    private String m_externalPageId = null;
    private String m_baseHref = null;
    private String m_pageIsCxePreviewable = "false";
    private String m_originalCharacterEncoding = "UTF-8";
    private String m_l10nProfileId = null;
    private String m_batchId = null;
    private String m_pageCount = null;
    private String m_pageNumber = null;
    private String m_docPageCount = null;
    private String m_docPageNumber = null;
    private String m_jobPrefixName = null;
    private String m_importInitiatorId = null;
    // the original CXE request type that CAP passes in for export or preview
    private String m_origCxeRequestType;
    private Long m_exportedTime = null;
    private CxeMessage m_cxeMessage = null;

    private String m_priority = null;

    /** CAP L10nRequest Type */
    private int m_requestType = Request.EXTRACTED_LOCALIZATION_REQUEST;

    /** CXE Import Request Type -- l10n or aligner */
    private String m_cxeImportRequestType = null;

    // ////////////////////////////////////
    // Constructors //
    // ////////////////////////////////////

    public CapImporter(CxeMessage p_cxeMessage,
            org.apache.log4j.Logger p_logger, int p_requestType)
            throws Exception
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_logger = p_logger;
        m_requestType = p_requestType;
    }

    /**
     * Static method that will return the number of imports waiting in the JMS
     * queue. If the session hasn't been set up or an exception occurs then "0"
     * is returned.
     */
    static public long getNumberOfWaitingImports()
    {
        // no standard way to do this yet..
        return 0;
    }

    // ////////////////////////////////////
    // Package Private Methods //
    // ////////////////////////////////////

    /**
     * Creates an l10nxml and then sends the eventFlowXml, content filename, and
     * then sends a JMS message to the L10nRequester queue
     * 
     * @param p_eventFlowXml
     *            event flow xml
     * @param p_content
     *            MessageData
     * @param p_exception
     *            exception message (XML)
     * @exception CapAdapterException
     */
    void sendContent() throws CapAdapterException
    {
        try
        {
            parseEventFlowXml();

            GeneralException exception = (GeneralException) m_cxeMessage
                    .getParameters().get("Exception");

            if (exception != null)
            {
                m_logger.info("Uploading import failure for: " + m_displayName);
            }
            else
            {
                m_logger.info("Uploading import request to CAP for: "
                        + m_displayName);
            }

            String l10nRequestXml = makeL10nRequestXml();
            String contentFileName = readContentFileName(m_cxeMessage
                    .getMessageData());

            HashMap hm = new HashMap();
            CompanyWrapper.saveCurrentCompanyIdInMap(hm, m_logger);
            hm.put(CxeToCapRequest.REQUEST_TYPE, new Integer(m_requestType));
            hm.put(CxeToCapRequest.CONTENT, contentFileName);
            hm.put(CxeToCapRequest.EVENT_FLOW_XML, m_eventFlowXml);
            hm.put(CxeToCapRequest.L10N_REQUEST_XML, l10nRequestXml);
            hm.put(CxeToCapRequest.EXCEPTION, exception);

            // See whether the CXE import type was l10n or aligner.
            if (m_cxeImportRequestType.equals(CxeProxy.IMPORT_TYPE_ALIGNER))
            {
                m_logger.info("Sending message to JMS_ALIGNER_QUEUE for aligner.");

                hm.put("PageCount", m_pageCount);
                hm.put("PageNumber", m_pageNumber);
                hm.put("DocPageCount", m_docPageCount);
                hm.put("DocPageNumber", m_docPageNumber);
                hm.put("AlignerExtractor",
                        m_cxeMessage.getParameters().get("AlignerExtractor"));

                JmsHelper.sendMessageToQueue((Serializable) hm,
                        JmsHelper.JMS_ALIGNER_QUEUE);
            }
            else
            {
                // m_logger.info("Sending message to JMS_IMPORTING_QUEUE for import.");

                JmsHelper.sendMessageToQueue((Serializable) hm,
                        JmsHelper.JMS_IMPORTING_QUEUE);
            }
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = "CapTargetAdapter";
            errorArgs[1] = m_displayName;
            m_logger.error("Import upload failed.", e);
            throw new CapAdapterException("HTTPIm", errorArgs, e);
        }
    }
    
    public HashMap getContent()
    {
    	try
        {
            parseEventFlowXml();

            GeneralException exception = (GeneralException) m_cxeMessage
                    .getParameters().get("Exception");

            if (exception != null)
            {
                m_logger.info("Uploading import failure for: " + m_displayName);
            }
            else
            {
                m_logger.info("Uploading import request to CAP for: "
                        + m_displayName);
            }

            String l10nRequestXml = makeL10nRequestXml();
            String contentFileName = readContentFileName(m_cxeMessage
                    .getMessageData());

            HashMap hm = new HashMap();
            CompanyWrapper.saveCurrentCompanyIdInMap(hm, m_logger);
            hm.put(CxeToCapRequest.REQUEST_TYPE, new Integer(m_requestType));
            hm.put(CxeToCapRequest.CONTENT, contentFileName);
            hm.put(CxeToCapRequest.EVENT_FLOW_XML, m_eventFlowXml);
            hm.put(CxeToCapRequest.L10N_REQUEST_XML, l10nRequestXml);
            hm.put(CxeToCapRequest.EXCEPTION, exception);

            // See whether the CXE import type was l10n or aligner.
            if (m_cxeImportRequestType.equals(CxeProxy.IMPORT_TYPE_ALIGNER))
            {
                m_logger.info("Sending message to JMS_ALIGNER_QUEUE for aligner.");

                hm.put("PageCount", m_pageCount);
                hm.put("PageNumber", m_pageNumber);
                hm.put("DocPageCount", m_docPageCount);
                hm.put("DocPageNumber", m_docPageNumber);
                hm.put("AlignerExtractor",
                        m_cxeMessage.getParameters().get("AlignerExtractor"));
            }

            return hm;
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = "CapTargetAdapter";
            errorArgs[1] = m_displayName;
            m_logger.error("Import upload failed.", e);
            throw new CapAdapterException("HTTPIm", errorArgs, e);
        }
    }

    // ////////////////////////////
    // Private Methods //
    // ////////////////////////////

    /**
     * Fills member data from parsing the EventFlowXml
     */
    private void parseEventFlowXml() throws Exception
    {
        // first find information from the EventFlowXml
        StringReader sr = null;
        try
        {
            sr = new StringReader(m_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            // don't validate
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            Element rootElement = parser.getDocument().getDocumentElement();
            Element e = null;

            NodeList nl = null;
            nl = rootElement.getElementsByTagName("source");
            Element sourceElement = (Element) nl.item(0);
            m_dataSourceType = sourceElement.getAttribute("dataSourceType");
            m_dataSourceId = sourceElement.getAttribute("dataSourceId");
            m_pageIsCxePreviewable = sourceElement
                    .getAttribute("pageIsCxePreviewable");
            m_cxeImportRequestType = sourceElement
                    .getAttribute("importRequestType");

            m_importInitiatorId = sourceElement
                    .getAttribute("importInitiatorId");

            nl = rootElement.getElementsByTagName("displayName");
            e = (Element) nl.item(0);
            m_externalPageId = e.getFirstChild().getNodeValue();
            m_displayName = m_externalPageId;

            nl = rootElement.getElementsByTagName("baseHref");
            if (nl == null || nl.getLength() == 0)
            {
                m_baseHref = null;
            }
            else
            {
                e = (Element) nl.item(0);
                m_baseHref = e.getFirstChild().getNodeValue();
            }

            nl = sourceElement.getElementsByTagName("charset");
            e = (Element) nl.item(0);
            m_originalCharacterEncoding = e.getFirstChild().getNodeValue();

            nl = rootElement.getElementsByTagName("batchInfo");
            e = (Element) nl.item(0);
            m_l10nProfileId = e.getAttribute("l10nProfileId");

            // the jobName is optional in the EventFlowXml
            nl = rootElement.getElementsByTagName("jobName");
            if (nl.getLength() > 0)
            {
                e = (Element) nl.item(0);
                m_jobPrefixName = e.getFirstChild().getNodeValue();
            }
            else
            {
                m_jobPrefixName = null;
            }

            nl = rootElement.getElementsByTagName("priority");
            if (nl.getLength() > 0)
            {
                e = (Element) nl.item(0);
                m_priority = e.getFirstChild().getNodeValue();
            }
            else
            {
                m_priority = "3";
            }

            nl = rootElement.getElementsByTagName("batchId");
            e = (Element) nl.item(0);
            m_batchId = e.getFirstChild().getNodeValue();

            nl = rootElement.getElementsByTagName("pageCount");
            e = (Element) nl.item(0);
            m_pageCount = e.getFirstChild().getNodeValue();

            nl = rootElement.getElementsByTagName("pageNumber");
            e = (Element) nl.item(0);
            m_pageNumber = e.getFirstChild().getNodeValue();

            nl = rootElement.getElementsByTagName("docPageCount");
            e = (Element) nl.item(0);
            m_docPageCount = e.getFirstChild().getNodeValue();

            nl = rootElement.getElementsByTagName("docPageNumber");
            e = (Element) nl.item(0);
            m_docPageNumber = e.getFirstChild().getNodeValue();

            // the messageId is optional in the EventFlowXml
            nl = rootElement.getElementsByTagName("capMessageId");
            if (nl.getLength() > 0)
            {
                e = (Element) nl.item(0);
                m_messageId = e.getFirstChild().getNodeValue();
            }
            else
            {
                m_messageId = null;
            }

            // the original cxe request type is optional in the EventFlowXml
            nl = rootElement.getElementsByTagName("cxeRequestType");
            if (nl.getLength() > 0)
            {
                e = (Element) nl.item(0);
                m_origCxeRequestType = e.getFirstChild().getNodeValue();
            }
            else
            {
                m_origCxeRequestType = null;
            }

            // get out the export batch ID
            nl = rootElement.getElementsByTagName("exportBatchInfo");
            if (nl.getLength() > 0)
            {
                e = (Element) nl.item(0);
                NodeList nl2 = e.getElementsByTagName("exportBatchId");
                e = (Element) nl2.item(0);
                m_exportBatchId = e.getFirstChild().getNodeValue();
            }
            else
            {
                m_exportBatchId = "N/A";
            }
        }
        finally
        {
            if (sr != null)
            {
                sr.close();
            }
        }
    }

    /**
     * Makes the L10nRequestXML from information in the EventFlowXml
     */
    private String makeL10nRequestXml()
    {
        // create the l10nxml
        String l10xml = null;
        StringBuffer b = new StringBuffer(XmlUtil.formattedL10nRequestXmlDtd());
        b.append("<l10nRequestXml dataSourceType=\"");
        b.append(m_dataSourceType);
        b.append("\" dataSourceId=\"");
        b.append(m_dataSourceId);
        b.append("\">\n");
        b.append("<externalPageId pageIsCxePreviewable=\"");
        b.append(m_pageIsCxePreviewable);
        b.append("\">");
        b.append(EditUtil.encodeXmlEntities(m_externalPageId));
        b.append("</externalPageId>\n");

        if (m_importInitiatorId != null)
        {
            b.append("<importInitiatorId>");
            b.append(EditUtil.encodeXmlEntities(m_importInitiatorId));
            b.append("</importInitiatorId>\n");
        }

        b.append("<originalSourceFileContent>");
        String originalSourceFileContent = (String) m_cxeMessage
                .getParameters().get(BaseAdapter.PARAM_ORIGINAL_FILE_CONTENT);
        b.append(EditUtil.encodeXmlEntities(originalSourceFileContent));
        b.append("</originalSourceFileContent>");

        b.append("<originalCharacterEncoding>");
        b.append(m_originalCharacterEncoding);
        b.append("</originalCharacterEncoding>\n");
        b.append("<l10nProfileId>");
        b.append(m_l10nProfileId);
        b.append("</l10nProfileId>\n");

        if (m_baseHref != null)
        {
            b.append("<baseHref>");
            b.append(EditUtil.encodeXmlEntities(m_baseHref));
            b.append("</baseHref>\n");
        }

        b.append("<priority>");
        b.append(m_priority);
        b.append("</priority>\n");

        b.append("<batchInfo>\n");
        b.append("<batchId>");
        b.append(EditUtil.encodeXmlEntities(m_batchId));
        b.append("</batchId>\n");
        b.append("<pageCount>");
        b.append(m_pageCount);
        b.append("</pageCount>\n");
        b.append("<pageNumber>");
        b.append(m_pageNumber);
        b.append("</pageNumber>\n");
        b.append("<docPageCount>");
        b.append(m_docPageCount);
        b.append("</docPageCount>\n");
        b.append("<docPageNumber>");
        b.append(m_docPageNumber);
        b.append("</docPageNumber>\n");

        if (m_jobPrefixName != null)
        {
            b.append("<jobPrefixName>");
            b.append(EditUtil.encodeXmlEntities(m_jobPrefixName));
            b.append("</jobPrefixName>\n");
        }

        b.append("</batchInfo>\n");
        b.append("</l10nRequestXml>\n");
        l10xml = b.toString();

        Logger.writeDebugFile("l10n.xml", l10xml);

        return l10xml;
    }

    /**
     * Reads the message data and returns a filename containing the message data
     * 
     * @param p_fmd
     *            MessageData object
     * @return String of GXML
     */
    private String readContentFileName(MessageData p_fmd) throws Exception
    {
        // This is dangerous since it may not be a FileMessageData
        // object, in that case the code could read the data and write
        // it to a new FileMessageData object using the createFrom()
        // method, but since we know we're using FileMessageData we'll
        // risk it here.
        if (p_fmd == null)
        {
            // for some reason, the MessageData was not set and transferred from
            // previous MDB
            return null;
        }
        FileMessageData fmd = (FileMessageData) p_fmd;
        return fmd.getName();
    }
}

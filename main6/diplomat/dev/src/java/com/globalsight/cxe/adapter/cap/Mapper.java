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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.page.pageexport.ExportBatchInfo;
import com.globalsight.everest.page.pageexport.ExportConstants;

/**
 * Helper class for the CapAdapter, does work related to preparing a file for
 * export.
 */
class Mapper
{
    static final int PRE_MERGE_EVENT = 0;
    static final int POST_MERGE_EVENT = 1;

    //
    // Private Members
    //
    private String m_eventFlowXml;
    private String m_targetLocale;
    private String m_targetCharset;
    private String m_cxeRequestType;
    private Element m_rootElement;
    private String m_preMergeEvent;
    private String m_postMergeEvent;
    private String m_exportLocation;
    private String m_localeSubDir;
    // CAP's unique message ID
    private String m_messageId;
    private Document m_document;
    // initial buffer size for writing out EventFlowXml
    private int m_bufferSize;
    // the name of the target file
    private String m_targetFileName;
    private ExportBatchInfo m_exportBatchInfo;
    private org.apache.log4j.Logger m_logger;
    private CxeMessage m_cxeMessage;

    //
    // Constructor
    //

    /**
     * Creates a Mapper object.
     * 
     * @param p_cxeMessage
     *            incoming CxeMessage
     * @param p_logger
     */
    Mapper(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
    {
        m_cxeMessage = p_cxeMessage;
        m_logger = p_logger;

        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_bufferSize = m_eventFlowXml.length();

        HashMap params = p_cxeMessage.getParameters();
        m_targetLocale = (String) params.get("TargetLocale");
        m_targetCharset = (String) params.get("TargetCharset");
        m_cxeRequestType = (String) params.get("CxeRequestType");
        m_messageId = (String) params.get("MessageId");
        m_exportLocation = (String) params.get("ExportLocation");
        m_localeSubDir = (String) params.get("LocaleSubDir");

        String exportBatchId = (String) params.get("ExportBatchId");
        Integer pageCount = (Integer) params.get("PageCount");
        Integer pageNum = (Integer) params.get("PageNum");
        Integer docPageCount = (Integer) params.get("DocPageCount");
        Integer docPageNum = (Integer) params.get("DocPageNum");
        m_targetFileName = (String) params.get("TargetFileName");

        m_exportBatchInfo = new ExportBatchInfo(exportBatchId, pageCount,
                pageNum, docPageCount, docPageNum);
    }

    //
    // Package Private Methods
    //

    /**
     * Modifies the event flow xml for the return trip through CXE. Returns a
     * CxeMessage where the event type is either the pre-merge or post-merge
     * event.
     * 
     * @param eventType
     *            PRE_MERGE_EVENT or POST_MERGE_EVENT
     * @return CxeMessage
     * @exception CapAdapterException
     */
    CxeMessage map(int eventType) throws CapAdapterException
    {
        Logger.writeDebugFile("csa_ef.xml", m_eventFlowXml);

        parseEventFlowXml();
        modifyEventFlowXml();

        Logger.writeDebugFile("csa_efm.xml", m_eventFlowXml);

        CxeMessageType type;
        if (eventType == PRE_MERGE_EVENT)
        {
            type = CxeMessageType.getCxeMessageType(m_preMergeEvent);
        }
        else
        {
            CxeMessageType catalystLoc = CxeMessageType
                    .getCxeMessageType(CxeMessageType.CATALYST_LOCALIZED_EVENT);

            if (m_preMergeEvent.equals(catalystLoc.getName()))
            {
                type = catalystLoc;
            }
            else
            {
                type = CxeMessageType.getCxeMessageType(m_postMergeEvent);
            }
        }

        CxeMessage newMsg = new CxeMessage(type);
        newMsg.setEventFlowXml(m_eventFlowXml);
        newMsg.setParameters(m_cxeMessage.getParameters());

        try
        {
            newMsg.setMessageData(m_cxeMessage.getMessageData());
        }
        catch (IOException ex)
        {
            m_logger.error("Problem re-using message data:", ex);
            String[] errorArgs = new String[1];
            errorArgs[0] = m_logger.getName();
            throw new CapAdapterException("CxeInternalEx", errorArgs, ex);
        }

        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("Publishing event " + type.getName());            
        }

        return newMsg;
    }

    //
    // Private Methods
    //

    /** Uses DOM to parse the EventFlowXml. */
    private void parseEventFlowXml() throws CapAdapterException
    {
        try
        {
            StringReader sr = new StringReader(m_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            m_document = parser.getDocument();
            m_rootElement = m_document.getDocumentElement();
            NodeList nl = m_rootElement.getElementsByTagName("preMergeEvent");
            Element pmElement = (Element) nl.item(0);
            m_preMergeEvent = pmElement.getFirstChild().getNodeValue();

            nl = m_rootElement.getElementsByTagName("postMergeEvent");
            pmElement = (Element) nl.item(0);
            m_postMergeEvent = pmElement.getFirstChild().getNodeValue();
        }
        catch (Exception ex)
        {
            m_logger.error("Problem parsing EventFlowXml: ", ex);
            String[] errorArgs = new String[1];
            errorArgs[0] = "CapSourceAdapter";
            throw new CapAdapterException("CxeInternalEx", errorArgs, ex);
        }
    }

    /**
     * Inserts the targetLocale, targetCharset, exportLocation, targetfilename
     * and capMessageId into the EventFlowXml.
     */
    private void modifyEventFlowXml() throws CapAdapterException
    {
        try
        {
            NodeList nl = m_rootElement.getElementsByTagName("target");
            Element targetElem = (Element) nl.item(0);

            // set target attributes for databaseMode and previewUrl
            if (m_cxeRequestType.equals(ExportConstants.PREVIEW))
            {
                targetElem.setAttribute("databaseMode", "preview");
                targetElem.setAttribute("previewUrl", "true");
            }
            else
            {
                targetElem.setAttribute("databaseMode", "final");
                targetElem.setAttribute("previewUrl", "false");
            }

            // now fill in the targetLocale and targetCharset
            nl = targetElem.getElementsByTagName("locale");
            Element localeElem = (Element) nl.item(0);
            localeElem.getFirstChild().setNodeValue(m_targetLocale);

            m_logger.info("Processing: " + m_targetFileName + ", locale: "
                    + m_targetLocale);

            nl = targetElem.getElementsByTagName("charset");
            Element charsetElem = (Element) nl.item(0);
            charsetElem.getFirstChild().setNodeValue(m_targetCharset);

            Element targetFileNameElem = null;

            // now fill in the export directory if this is for the filesystem
            if (targetElem.getAttribute("name").equals(
                    "FileSystemTargetAdapter"))
            {
                nl = targetElem.getElementsByTagName("da");
                Element daElement;
                Element dvElement;
                NodeList nl2;
                boolean foundExportLoc = false;
                boolean foundLocaleSubDir = false;

                for (int i = 0; i < nl.getLength(); i++)
                {
                    daElement = (Element) nl.item(i);
                    if (daElement.getAttribute("name").equals("ExportLocation"))
                    {
                        nl2 = daElement.getElementsByTagName("dv");
                        dvElement = (Element) nl2.item(0);
                        dvElement.getFirstChild()
                                .setNodeValue(m_exportLocation);
                        foundExportLoc = true;
                    }

                    if (daElement.getAttribute("name").equals("LocaleSubDir"))
                    {
                        nl2 = daElement.getElementsByTagName("dv");
                        dvElement = (Element) nl2.item(0);
                        dvElement.getFirstChild().setNodeValue(m_localeSubDir);
                        foundLocaleSubDir = true;
                    }

                    if (daElement.getAttribute("name").equals("Filename"))
                    {
                        nl2 = daElement.getElementsByTagName("dv");
                        targetFileNameElem = (Element) nl2.item(0);
                    }

                    // if all have been found then break out of the rest
                    if (foundLocaleSubDir && foundLocaleSubDir
                            && targetFileNameElem != null)
                    {
                        break;
                    }
                }

                if (!foundExportLoc)
                {
                    // an older format of eventflowxml. needs to
                    // update the attribute name and add localeSubDir
                    // as well
                    m_logger.info("Converting a pre-4.3 eventflowxml to 4.3 schema.");
                    migrateEventFlowXml(targetElem);
                }

                // Now add in the target file name.
                // The target file name may be different from the source -
                // so add it to the xml to be modified by the Adapters
                // if necessary.

                // If it wasn't found already in the XML then add a new
                // attribute.
                if (targetFileNameElem == null)
                {
                    Element src = (Element) m_rootElement.getElementsByTagName(
                            "source").item(0);
                    String[] values = null;
                    NodeList nlS = src.getElementsByTagName("da");
                    Element srcFileNameElement = null;
                    for (int i = 0; srcFileNameElement == null
                            && i < nlS.getLength(); i++)
                    {
                        Element daElementS = (Element) nlS.item(i);
                        if (daElementS.getAttribute("name").equals("Filename"))
                        {
                            NodeList dvs = daElementS
                                    .getElementsByTagName("dv");
                            values = new String[dvs.getLength()];
                            for (int j = 0; j < values.length; j++)
                            {
                                Element dv = (Element) dvs.item(j);
                                values[j] = dv.getFirstChild().getNodeValue();
                            }
                            srcFileNameElement = daElementS;
                        }
                    }

                    // make a copy of the source node
                    Element newda = (Element) srcFileNameElement
                            .cloneNode(true);
                    NodeList nlT = newda.getElementsByTagName("dv");
                    Element dvElementT = (Element) nlT.item(0);
                    dvElementT.getFirstChild().setNodeValue(m_targetFileName);
                    targetElem.appendChild(newda);
                }
                else
                {
                    // just modify the one that is already part of the XML
                    targetFileNameElem.getFirstChild().setNodeValue(
                            m_targetFileName);
                }
            }

            // now add in the capMessageId
            NodeList nodes = m_rootElement.getElementsByTagName("capMessageId");
            for (int i = 0; i < nodes.getLength(); i++)
            {
                m_rootElement.removeChild(nodes.item(i));
            }
            Element capMessageIdElem = m_document.createElement("capMessageId");
            capMessageIdElem
                    .appendChild(m_document.createTextNode(m_messageId));
            m_rootElement.appendChild(capMessageIdElem);

            // add in the cxe request type
            nodes = m_rootElement.getElementsByTagName("cxeRequestType");
            for (int i = 0; i < nodes.getLength(); i++)
            {
                m_rootElement.removeChild(nodes.item(i));
            }
            Element cxeRequestTypeElem = m_document
                    .createElement("cxeRequestType");
            cxeRequestTypeElem.appendChild(m_document
                    .createTextNode(m_cxeRequestType));
            m_rootElement.appendChild(cxeRequestTypeElem);

            // add in the Export Batch Info, but remove any existing ones first
            nodes = m_rootElement.getElementsByTagName("exportBatchInfo");
            for (int i = 0; i < nodes.getLength(); i++)
            {
                m_rootElement.removeChild(nodes.item(i));
            }

            Element ebi = m_document.createElement("exportBatchInfo");
            Element elem = m_document.createElement("exportBatchId");
            elem.appendChild(m_document
                    .createTextNode(m_exportBatchInfo.exportBatchId));
            ebi.appendChild(elem);
            elem = m_document.createElement("exportBatchPageCount");
            elem.appendChild(m_document
                    .createTextNode(m_exportBatchInfo.pageCount.toString()));
            ebi.appendChild(elem);
            elem = m_document.createElement("exportBatchPageNum");
            elem.appendChild(m_document
                    .createTextNode(m_exportBatchInfo.pageNum.toString()));
            ebi.appendChild(elem);
            elem = m_document.createElement("exportBatchDocPageCount");
            elem.appendChild(m_document
                    .createTextNode(m_exportBatchInfo.docPageCount.toString()));
            ebi.appendChild(elem);
            elem = m_document.createElement("exportBatchDocPageNum");
            elem.appendChild(m_document
                    .createTextNode(m_exportBatchInfo.docPageNum.toString()));
            ebi.appendChild(elem);
            m_rootElement.appendChild(ebi);

            m_rootElement.appendChild(cxeRequestTypeElem);

            // now recreate the EventFlowXml String
            OutputFormat oformat = new OutputFormat(m_document, "UTF-8", true);
            oformat.setOmitDocumentType(true);
            oformat.setOmitComments(false);
            oformat.setOmitXMLDeclaration(true);
            oformat.setPreserveSpace(true);
            oformat.setIndenting(false);

            XMLSerializer xmlSerializer = new XMLSerializer(oformat);
            StringWriter stringWriter = new StringWriter(m_bufferSize);
            // restore the DTD
            stringWriter.write(XmlUtil.formattedEventFlowXmlDtd());
            xmlSerializer.setOutputCharStream(stringWriter);
            xmlSerializer.serialize(m_document);

            m_eventFlowXml = stringWriter.toString();
        }
        catch (Exception ex)
        {
            String[] errorArgs = new String[1];
            errorArgs[0] = "CapSourceAdapter";
            throw new CapAdapterException("CxeInternalEx", errorArgs, ex);
        }
    }

    /**
     * Pre 4.3 EventFlowXml did not have the ExportLocation and LocaleSubDir
     * attributes. ExportDirectory was used as the locale specific subdir. This
     * migrates to the new format by replacing "ExportDirectory" with
     * "ExportLocation" and adding the LocaleSubDir attribute
     * 
     * @param p_targetElem
     */
    private void migrateEventFlowXml(Element p_targetElem)
    {
        // first replace the ExportDirectory with ExportLocation
        NodeList nl = p_targetElem.getElementsByTagName("da");
        NodeList nl2 = null;
        Element daElement = null;
        Element dvElement = null;

        for (int i = 0; i < nl.getLength(); i++)
        {
            daElement = (Element) nl.item(i);
            if (daElement.getAttribute("name").equals("ExportDirectory"))
            {
                daElement.setAttribute("name", "ExportLocation");
                nl2 = daElement.getElementsByTagName("dv");
                dvElement = (Element) nl2.item(0);
                dvElement.getFirstChild().setNodeValue(m_exportLocation);
            }
        }

        // add a new attribute
        Element newda = (Element) daElement.cloneNode(true);
        newda.setAttribute("name", "LocaleSubDir");
        nl2 = newda.getElementsByTagName("dv");
        dvElement = (Element) nl2.item(0);
        dvElement.getFirstChild().setNodeValue(m_localeSubDir);
        p_targetElem.appendChild(newda);
    }
}

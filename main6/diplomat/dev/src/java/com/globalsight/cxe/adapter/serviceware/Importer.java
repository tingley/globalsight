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
package com.globalsight.cxe.adapter.serviceware;

import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import com.globalsight.diplomat.adapter.DiplomatOperation;
import com.globalsight.diplomat.adapter.DiplomatProcessor;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.servlet.util.ServerProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.List;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
//DOM4J
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.util.XmlParser;


/**
 * Helper class used by the ServiceWareAdapter for importing
 */
public class Importer
{
    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private byte[] m_content = null;
    private String[] m_errorArgs = null;
    private String m_preExtractEvent = null;
    private String m_preMergeEvent = null;
    private org.apache.log4j.Logger m_logger = null;
    private CxeMessage m_cxeMessage = null;
    private String m_koid = null; //knowledge object id
    private String m_koName = null;

    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    /**
     * Creates an Importer object
     *
     * @param p_logger
     */
    Importer(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
    {
        m_logger = p_logger;
        m_cxeMessage = p_cxeMessage;
        m_errorArgs = new String[3];
        m_errorArgs[0] = p_logger.getName();
    }

    public CxeMessageType getPreExtractEvent()
    {
        return CxeMessageType.getCxeMessageType(m_preExtractEvent);
    }

    /** Makes event flow xml for import */
    /**
     * Creates the EventFlowXml. Assume going back to the ServiceWareTargetAdapter
     *
     * @return the string of Event Flow Xml
     * @exception ServiceWareAdapterException
     */
    public String makeEventFlowXml() throws ServiceWareAdapterException
    {
        try {
            HashMap params = m_cxeMessage.getParameters();
            m_errorArgs[1] = "serviceware";
            long fpId = Long.parseLong((String)params.get("FileProfileId"));

            FileProfile fp = ServerProxy.getFileProfilePersistenceManager().
                getFileProfileById(fpId,false);
            long lpId = fp.getL10nProfileId();
            L10nProfile l10np = ServerProxy.getProjectHandler().getL10nProfile(lpId);
            long kfId = fp.getKnownFormatTypeId();
            KnownFormatType kf = ServerProxy.getFileProfilePersistenceManager().
                getKnownFormatTypeById(kfId,false);

            Boolean overrideAsUnextracted = (Boolean) params.get("OverrideFileProfileAsUnextracted");
            m_preExtractEvent = overrideAsUnextracted.booleanValue() ?
                CxeMessageType.getCxeMessageType(
                    CxeMessageType.UNEXTRACTED_IMPORTED_EVENT).getName() :
                kf.getPreExtractEvent();

            m_preMergeEvent = overrideAsUnextracted.booleanValue() ?
                CxeMessageType.getCxeMessageType(
                    CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT).getName() :
                kf.getPreMergeEvent();

            m_preMergeEvent = kf.getPreMergeEvent();
            m_preExtractEvent = kf.getPreExtractEvent();
            String batchId = (String) params.get("BatchId");
            Integer pageNum = (Integer) params.get("PageNum");
            Integer pageCount = (Integer) params.get("PageCount");
            Integer docPageNum = (Integer) params.get("DocPageNum");
            Integer docPageCount = (Integer) params.get("DocPageCount");
            String displayName = m_koName + " [" + m_koid + "]";
            String baseHref = "";
            String jobName = (String) params.get("JobName");

            String eventFlowXml = null;
            StringBuffer b = new StringBuffer(XmlUtil.formattedEventFlowXmlDtd());
            b.append("<eventFlowXml>\r\n");
            b.append("<preMergeEvent>");
            b.append(m_preMergeEvent);
            b.append("</preMergeEvent>\r\n");
            b.append("<postMergeEvent>");
            b.append(CxeMessageType.getCxeMessageType(
                CxeMessageType.SERVICEWARE_EXPORT_EVENT).getName());
            b.append("</postMergeEvent>\r\n");
            b.append("<batchInfo l10nProfileId=\"");
            b.append(lpId);
            b.append("\" processingMode=\"automatic\">\n<batchId>");
            b.append(EditUtil.encodeXmlEntities(batchId));
            b.append("</batchId>\r\n");
            b.append("<pageCount>");
            b.append(pageCount);
            b.append("</pageCount>\r\n");
            b.append("<pageNumber>");
            b.append(pageNum);
            b.append("</pageNumber>\r\n");
            b.append("<docPageCount>");
            b.append(docPageCount);
            b.append("</docPageCount>\r\n");
            b.append("<docPageNumber>");
            b.append(docPageNum);
            b.append("</docPageNumber>\r\n");
            b.append("<displayName>");
            b.append(EditUtil.encodeXmlEntities(displayName));
            b.append("</displayName>\r\n");
            b.append("<baseHref>");
            b.append(baseHref);
            b.append("/");
            b.append("</baseHref>\r\n");
            b.append("<jobName>");
            b.append(EditUtil.encodeXmlEntities(jobName));
            b.append("</jobName></batchInfo>\r\n");
            b.append("<source name=\"ServiceWareSourceAdapter\" ");
            b.append("dataSourceType=\"serviceware\" dataSourceId=\"");
            b.append(fpId);
            b.append("\" formatType=\"");
            b.append(kf.getFormatType());
            b.append("\" pageIsCxePreviewable=\"true\">\n<locale>");
            b.append(l10np.getSourceLocale().getLocale());
            b.append("</locale>\r\n");
            b.append("<charset>");
            b.append(fp.getCodeSet());
            b.append("</charset>\r\n");
            b.append("</source>\r\n");
            b.append("<target name=\"ServiceWareTargetAdapter\">\r\n");
            b.append("<locale>unknown</locale>\r\n"); //the locale and charset get filled in by CXE later
            b.append("<charset>unknown</charset>\r\n");
            b.append("</target>\r\n");

            //add on some data that the ServiceWare target adapter will need later
            b.append("<category name=\"ServiceWare\">\r\n");
            b.append("<da name=\"KOID\"><dv>");
            b.append(m_koid);
            b.append("</dv></da>\r\n");
            b.append("</category>\r\n");
            b.append("</eventFlowXml>\r\n");
            eventFlowXml = b.toString();
            Logger.writeDebugFile("sfsa_ef.xml", eventFlowXml);
            return eventFlowXml;
        }
        catch (Exception e)
        {
            m_logger.error("Could not create event flow xml.",e);
            throw new ServiceWareAdapterException("UnexpectedIm", m_errorArgs, e);
        }
    }

    /**
     * reads the serviceware content. XML is used.
     * @return MessageData
     * @exception ServiceWareAdapterException
     */
    public MessageData readContent() throws ServiceWareAdapterException
    {
        try {
            HashMap params = m_cxeMessage.getParameters();
            m_koid = (String) params.get("KOID");
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            String sessionId = ServiceWareAPI.connect();
            m_logger.info("Querying XML for knowledge object " + m_koid);
            String knowledgeObjXml = ServiceWareAPI.getKnowledgeObjectXml(sessionId,m_koid);
            String swXml = makeServiceWareXml(sessionId,knowledgeObjXml);
            m_logger.debug("XML to Localize is:\r\n" + swXml);
            OutputStreamWriter osw = new OutputStreamWriter(fmd.getOutputStream());
            osw.write(swXml);
            osw.close();
            ServiceWareAPI.disconnect(sessionId);
            return fmd;
        }
        catch (Exception e)
        {
            m_logger.error("Could not read content for import.",e);
            throw new ServiceWareAdapterException("UnexpectedEx", m_errorArgs, e);
        }
    }

    /**
     * Makes a special ServiceWareXML that contains the fields
     * GlobalSight needs to localize
     *
     * @param p_koXml knowledge object XML
     * @return Service Ware XML to localize
     * @exception Exception
     */
    private String makeServiceWareXml(String p_sessionId, String p_koXml) throws Exception
    {
        //first get some data from the knowledge objxml
        XmlParser xmlp = XmlParser.hire();
        Document d = xmlp.parseXml(p_koXml);
        Element root = d.getRootElement();
        String koName= root.selectSingleNode("/GetKOsResponse/return/SessionKOs/KO/Name").getText();
        m_koName=koName;
        String koShortDesc= root.selectSingleNode("/GetKOsResponse/return/SessionKOs/KO/ShortDescription").getText();
        StringBuffer swXml = new StringBuffer(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        swXml.append("<serviceWareXml>\r\n");
        swXml.append("<knowledgeObj id=\"");
        swXml.append(m_koid);
        swXml.append("\">\r\n");
        swXml.append("<knowledgeObjName>");
        swXml.append(koName);
        swXml.append("</knowledgeObjName>\r\n");
        swXml.append("<knowledgeObjShortDesc>");
        swXml.append(koShortDesc);
        swXml.append("</knowledgeObjShortDesc>\r\n");
        List conceptIdListNodes = root.selectNodes("/GetKOsResponse/return/SessionKOs/KO/ConceptIDs");
        String conceptIdListOrig = ((Node)conceptIdListNodes.get(0)).getText();
        m_logger.debug("orig concept Id list: " + conceptIdListOrig);
        String conceptIdList = conceptIdListOrig.replaceAll(", ",",");
        m_logger.debug("fixed concept Ids: " + conceptIdList);

        //get the XML for all concept Objs
        String conceptObjXml = ServiceWareAPI.getConceptObjectXml(
            p_sessionId, conceptIdList);
        XmlParser conceptXmlp = XmlParser.hire();
        Document conceptDoc = conceptXmlp.parseXml(conceptObjXml);
        Element conceptRoot = conceptDoc.getRootElement();
        List conceptNodes = conceptRoot.selectNodes("/GetConceptsResponse/return/SessionConcepts/Concept");
        m_logger.debug("There are " + conceptNodes.size() + " concept nodes.");
        for (int i=0; i < conceptNodes.size(); i++)
        {
            Node conceptNode = (Node) conceptNodes.get(i);
            String conceptId = conceptNode.selectSingleNode("ConceptID").getText();
            String conceptName = conceptNode.selectSingleNode("Name").getText();
            String conceptShortDesc = conceptNode.selectSingleNode("ShortDescription").getText();
            swXml.append("<concept id=\"");
            swXml.append(conceptId);
            swXml.append("\">");
            swXml.append("<conceptName>");
            swXml.append(conceptName);
            swXml.append("</conceptName>\r\n");
            swXml.append("<conceptShortDesc>");
            swXml.append(conceptShortDesc);
            swXml.append("</conceptShortDesc>\r\n");
            swXml.append("</concept>\r\n");
        }
        swXml.append("</knowledgeObj>\r\n");
        swXml.append("</serviceWareXml>\r\n");
        XmlParser.fire(conceptXmlp);
        XmlParser.fire(xmlp);
        return swXml.toString();
    }
}


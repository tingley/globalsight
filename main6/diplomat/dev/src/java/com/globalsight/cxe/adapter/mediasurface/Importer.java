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
package com.globalsight.cxe.adapter.mediasurface;

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
import com.mediasurface.client.*;
import com.mediasurface.datatypes.*;
import com.mediasurface.general.*;
import java.util.HashMap;
import java.util.Hashtable;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;

/**
 * Helper class used by the MediasurfaceAdapter for importing
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
    private Mediasurface m_mediaSurface = null;
    private SecurityContextHandle m_securityContextHandle = null;
    private CxeMessage m_cxeMessage = null;
    private IItem m_item = null; //the mediasurface item
    private ContentServer m_contentServer =null;
    private String m_username = null;
    private String m_password = null;

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

    /** Connect to Mediasurface */
    void connect()
        throws MediasurfaceAdapterException
    {
        try
        {
            HashMap params = m_cxeMessage.getParameters();
            String url = (String) params.get("MediasurfaceContentServerUrl");
            String name = (String) params.get("MediasurfaceContentServerName");
            String port = (String) params.get("MediasurfaceContentServerPort");
            m_contentServer = new ContentServer(url,name,
                Integer.parseInt(port));
            String contentServerId = m_contentServer.toString();
            m_mediaSurface = MediasurfaceConnection.getConnection(
                m_contentServer);
            m_username = (String) params.get("MediasurfaceUser");
            m_password= (String) params.get("MediasurfacePassword");
            m_logger.debug("Logging in to Mediasurface as " + m_username);
            m_securityContextHandle = m_mediaSurface.login(m_username,m_password);
            m_mediaSurface.setAdminMode(m_securityContextHandle,true);
            m_logger.debug("Logged in ok.");
        }
        catch (Exception e)
        {
            m_logger.error("Could not connect to Mediasurface.",e);
            throw new MediasurfaceAdapterException("ConnectionIm", m_errorArgs, e);
        }
    }

    /** Disconnect from Mediasurface */
    void disconnect() throws MediasurfaceAdapterException
    {
        try
        {
            m_logger.debug("Logging out.");
            m_mediaSurface.logout(m_securityContextHandle);
        }
        catch (Exception e)
        {
            m_logger.error("Could not disconnect to Mediasurface.", e);
            throw new MediasurfaceAdapterException("ConnectionIm", m_errorArgs, e);
        }
    }

    public CxeMessageType getPreExtractEvent()
    {
        return CxeMessageType.getCxeMessageType(m_preExtractEvent);
    }

    /** Makes event flow xml for import */
    /**
     * Creates the EventFlowXml. Assume going back to the MediasurfaceTargetAdapter
     *
     * @return the string of Event Flow Xml
     * @exception MediasurfaceAdapterException
     */
    public String makeEventFlowXml()
        throws MediasurfaceAdapterException
    {
        try
        {
            HashMap params = m_cxeMessage.getParameters();
            Integer itemKey = (Integer) params.get("MediasurfaceItemKey");
            m_item = m_mediaSurface.getItem(m_securityContextHandle,
                new ItemKey(itemKey.intValue()));
            m_errorArgs[1] = m_item.getUrl();
            long fpId = Long.parseLong((String)params.get("FileProfileId"));

            FileProfile fp = ServerProxy.getFileProfilePersistenceManager().
                getFileProfileById(fpId,false);
            long lpId = fp.getL10nProfileId();
            L10nProfile l10np = ServerProxy.getProjectHandler().getL10nProfile(lpId);
            long kfId = fp.getKnownFormatTypeId();
            KnownFormatType kf = ServerProxy.getFileProfilePersistenceManager().
                getKnownFormatTypeById(kfId,false);

            Boolean overrideAsUnextracted = (Boolean) params.get(
                "OverrideFileProfileAsUnextracted");
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
            //        int idx = m_item.getUrl().indexOf("://");
            //        int idx2 = m_item.getUrl().indexOf("/",idx + 3);
            String displayName = m_item.getPath();
            String baseHref = "";
            String jobName = (String) params.get("JobName");

            String importRequestType = (String) params.get(CxeProxy.IMPORT_TYPE);
            String eventFlowXml = null;
            StringBuffer b = new StringBuffer(XmlUtil.formattedEventFlowXmlDtd());
            b.append("<eventFlowXml>\r\n");
            b.append("<preMergeEvent>");
            b.append(m_preMergeEvent);
            b.append("</preMergeEvent>\r\n");
            b.append("<postMergeEvent>");
            b.append(CxeMessageType.getCxeMessageType(
                CxeMessageType.MEDIASURFACE_EXPORT_EVENT).getName());
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
            b.append("<source name=\"MediasurfaceSourceAdapter\" ");
            b.append("dataSourceType=\"mediasurface\" dataSourceId=\"");
            b.append(fpId);
            b.append("\" formatType=\"");
            b.append(kf.getFormatType());
            b.append("\" pageIsCxePreviewable=\"true\" importRequestType=\"");
            b.append(importRequestType).append("\">\n<locale>");
            b.append(l10np.getSourceLocale().getLocale());
            b.append("</locale>\r\n");
            b.append("<charset>");
            b.append(fp.getCodeSet());
            b.append("</charset>\r\n");
            b.append("</source>\r\n");
            b.append("<target name=\"MediasurfaceTargetAdapter\">\r\n");
            //the locale and charset get filled in by CXE later
            b.append("<locale>unknown</locale>\r\n");
            b.append("<charset>unknown</charset>\r\n");
            b.append("</target>\r\n");

            //add on some data that the Mediasurface target adapter will need later
            b.append("<category name=\"Mediasurface\">\r\n");
            b.append("<da name=\"OriginalItemKey\"><dv>");
            b.append(itemKey.toString());
            b.append("</dv></da>\r\n");
            b.append("<da name=\"ContentServerUrl\"><dv>");
            b.append(m_contentServer.url);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"ContentServerName\"><dv>");
            b.append(m_contentServer.name);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"ContentServerPort\"><dv>");
            b.append(m_contentServer.port);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"MediasurfaceUser\"><dv>");
            b.append(m_username);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"MediasurfacePassword\"><dv>");
            b.append(m_password);
            b.append("</dv></da>\r\n");
            b.append("</category>\r\n");
            b.append("</eventFlowXml>\r\n");
            eventFlowXml = b.toString();

            Logger.writeDebugFile("eventFlow.xml", eventFlowXml);

            return eventFlowXml;
        }
        catch (Exception e)
        {
            m_logger.error("Could not create event flow xml.", e);
            throw new MediasurfaceAdapterException("UnexpectedIm", m_errorArgs, e);
        }
    }

    /**
     * Reads the mediasurface content.
     * If the item is a content item (application/x-mediasurface)
     * then XML is used, otherwise the item is read as a binary
     * file.
     *
     * @return MessageData
     * @exception MediasurfaceAdapterException
     */
    public MessageData readContent()
        throws MediasurfaceAdapterException
    {
        try
        {
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            if (m_item.getMediaType().equals("application/x-mediasurface"))
            {
                m_logger.debug("Reading Mediasurface XML for item.");
                String xml = makeMediasurfaceXml();
                OutputStreamWriter osw = new OutputStreamWriter(
                    fmd.getOutputStream(),"UTF8");
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(xml);
                bw.close();
            }
            else
            {
                m_logger.debug("Reading binary data for item.");
                BinaryContent bc = m_item.getBinaryContent();
                BufferedInputStream bis = new BufferedInputStream(
                    bc.getContentStream());
                BufferedOutputStream bos = new BufferedOutputStream(
                    fmd.getOutputStream());
                byte[] buf = new byte[10240];
                int count = 0;
                while ((count = bis.read(buf)) != -1)
                    bos.write(buf,0,count);
                bis.close();
                bos.close();
            }
            return fmd;
        }
        catch (Exception e)
        {
            m_logger.error("Could not read content for import.", e);
            throw new MediasurfaceAdapterException("UnexpectedEx", m_errorArgs, e);
        }
    }


    /**
     * Returns XML containing the information which
     * should be localized from the content item
     *
     * @return String
     */
    private String makeMediasurfaceXml() throws Exception
    {
        StringBuffer xml = new StringBuffer(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<MediasurfaceXml>\r\n");
        //<Content itemKey="531">
        xml.append("<Content itemKey=\"");
        xml.append(m_item.getKey().getKey());
        xml.append("\">\r\n");
        String fieldNames[] = m_item.getFieldNames();
        for (int i = 0; i < fieldNames.length; i++)
        {
            String name = fieldNames[i];
            String value = m_item.getFieldValue(name);
            xml.append("<Field name=\"");
            xml.append(name);
            xml.append("\">\r\n<![CDATA[");
            xml.append(value);
            xml.append("]]></Field>\r\n");
        }
        xml.append("</Content>\r\n");
        xml.append("</MediasurfaceXml>\r\n");
        return xml.toString();
    }
}


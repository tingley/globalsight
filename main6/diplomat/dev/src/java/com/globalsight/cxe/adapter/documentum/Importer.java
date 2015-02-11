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
package com.globalsight.cxe.adapter.documentum;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.adapter.DiplomatOperation;
import com.globalsight.diplomat.adapter.DiplomatProcessor;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

//DOM4J
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.util.XmlParser;

//documentum
import com.documentum.com.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

/**
 * Helper class used by the DocumentumAdapter for importing
 */
public class Importer
{
    //
    // Private Members
    //

    private byte[] m_content = null;
    private String[] m_errorArgs = null;
    private String m_preExtractEvent = null;
    private String m_preMergeEvent = null;
    private org.apache.log4j.Logger m_logger = null;
    private CxeMessage m_cxeMessage = null;
    //source file DCTM oid
    private String m_oid = null; 
    //target file DCTM oid for this sourc file
    private Boolean m_isAttrFile = null;
    //DCTM account
    private String userId = null;

    //
    // Constructor
    //

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
     * Creates the EventFlowXml. Assume going back to the DocumentumTargetAdapter
     *
     * @return the string of Event Flow Xml
     * @exception DocumentumAdapterException
     */
    public String makeEventFlowXml()
        throws DocumentumAdapterException
    {
        try
        {
            HashMap params = m_cxeMessage.getParameters();
            m_errorArgs[1] = "Documentum";
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
            String displayName = (String) params.get("Filename");
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
                CxeMessageType.DOCUMENTUM_EXPORT_EVENT).getName());
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
            b.append("<source name=\"DocumentumSourceAdapter\" ");
            b.append("dataSourceType=\"documentum\" dataSourceId=\"");
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
            b.append("<target name=\"DocumentumTargetAdapter\">\r\n");
            b.append("<locale>unknown</locale>\r\n"); //the locale and charset get filled in by CXE later
            b.append("<charset>unknown</charset>\r\n");
            b.append("</target>\r\n");

            //add on some data that the Documentum target adapter will need later
            b.append("<category name=\"Documentum\">\r\n");
            b.append("<da name=\"" + DocumentumOperator.DCTM_OBJECTID + "\"><dv>");
            b.append(m_oid);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"" + DocumentumOperator.DCTM_ISATTRFILE + "\"><dv>");
            b.append(m_isAttrFile);
            b.append("</dv></da>\r\n");
            b.append("<da name=\"" + DocumentumOperator.DCTM_USERID + "\"><dv>");
            b.append(userId);
            b.append("</dv></da>\r\n");
            
            b.append("</category>\r\n");
            b.append("</eventFlowXml>\r\n");
            eventFlowXml = b.toString();

            Logger.writeDebugFile("dctmsa_ef.xml", eventFlowXml);

            return eventFlowXml;
        }
        catch (Exception e)
        {
            m_logger.error("Could not create event flow xml.", e);
            throw new DocumentumAdapterException("UnexpectedIm", m_errorArgs, e);
        }
    }

    /**
     * Reads the documentum content.
     * 
     * @return MessageData
     * @exception DocumentumAdapterException
     */
    public MessageData readContent() throws DocumentumAdapterException {

        try {
            //Get parameters for importing a Documentum file.
            HashMap params = m_cxeMessage.getParameters();
            m_oid = (String)params.get(DocumentumOperator.DCTM_OBJECTID);
            m_isAttrFile = (Boolean)params.get(DocumentumOperator.DCTM_ISATTRFILE);
            String dctmFileAttrXml = (String)params.get(DocumentumOperator.DCTM_FILEATTRXML);
            params.remove(DocumentumOperator.DCTM_FILEATTRXML);
            userId = (String)params.get(DocumentumOperator.DCTM_USERID);

            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            String tmpFile = fmd.getFile().getAbsolutePath();
            m_logger.debug("Starting to read content from Documentum side......");
            if (!m_isAttrFile.booleanValue()) {
                DocumentumOperator.getInstance().readContentFromDCTM(userId, m_oid, tmpFile);
            } else if (dctmFileAttrXml != null){
                File xmlFile = null;
                xmlFile = new File(tmpFile);
                OutputStream os = new FileOutputStream(xmlFile);
                OutputStreamWriter  fos = new OutputStreamWriter(os, "UTF-8");
                fos.write(dctmFileAttrXml);
                fos.close();
                m_logger.debug("Finish to read attributes of Documentum object.");
            }

            return fmd;
        }
        catch (Exception e)
        {
            m_logger.error("Could not read content for import.", e);
            throw new DocumentumAdapterException("UnexpectedEx", m_errorArgs, e);
        }
    }
}


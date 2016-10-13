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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.filesystem.Exporter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgrWLRemote;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;

/**
 * Helper class for sending info to CAP whether it is new content for import, or
 * import/export error or status messages.
 */
public class Uploader
{
    // CAP Reqest Types (for import)
    static final String L10N_REQUEST = "1";
    static final String ERROR_REQUEST = "2";

    // CAP Response Types (for export)
    static final String EXPORT_SUCCESS = "1";
    static final String EXPORT_FAILURE = "2";
    static final String EXPORT_STATUS = "33";
    static final String PREVIEW_STATUS = "34";

    static final String AMPERSAND = "&";
    static final String EQUALS = "=";

    private URL m_url = null;
    private String m_eventFlowXml = null;
    private String m_gxml = null;
    private String m_exception = null;
    private String m_displayName = null;
    private String m_messageId = null;
    private String m_exportBatchId = null;
    private Logger m_logger;

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
    // the original CXE request type that CAP passes in for export or preview
    private String m_origCxeRequestType;
    private Long m_exportedTime = null;
    private String m_exportAbsolutePath = null;

    /**
     * A temporary export path where a copy of the final version of the exported
     * document was placed
     */
    private String m_tempExportPath = null;
    private String m_isComponentPage = null;
    private CxeMessage m_cxeMessage = null;

    /**
     * Creates an Uploader with the given URL for a normal l10n request type
     * upload.
     */
    public Uploader(CxeMessage p_cxeMessage, Logger p_logger)
            throws Exception
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_logger = p_logger;

        SystemConfiguration config = SystemConfiguration.getInstance();
        String port = config
                .getStringParameter(SystemConfigParamNames.CXE_NON_SSL_PORT);
        StringBuffer url = new StringBuffer("http://localhost:" + port);
        String companyId = (String) m_cxeMessage.getParameters().get(
                CompanyWrapper.CURRENT_COMPANY_ID);
        Long xmlDtdId = (Long) m_cxeMessage.getParameters().get(
                Exporter.XML_DTD_ID);
        if (xmlDtdId == null)
        {
            xmlDtdId = -1L;
        }

        url.append("/globalsight/CapExportServlet?").append(
                CompanyWrapper.CURRENT_COMPANY_ID).append("=")
                .append(companyId).append("&").append(Exporter.XML_DTD_ID)
                .append("=").append(xmlDtdId);
        m_url = new URL(url.toString());
        parseEventFlowXml();
    }

    /**
     * uploads notification of an export status
     */
    void uploadExportStatus() throws CapAdapterException
    {
        m_exportedTime = (Long) m_cxeMessage.getParameters()
                .get("ExportedTime");
        m_exportAbsolutePath = (String) m_cxeMessage.getParameters().get(
                "AbsoluteExportPath");
        m_isComponentPage = (String) m_cxeMessage.getParameters().get(
                "IsComponentPage");
        m_tempExportPath = (String) m_cxeMessage.getParameters().get(
                BaseAdapter.PARAM_ORIGINAL_FILE_CONTENT);
        uploadStatus(false);
        performStfCreationFailureNotificationIfNeeded();
    }

    /**
     * Uploads status for a preview and returns a CxeMessage for dynamic preview
     * 
     * @return CxeMessage
     * @exception CapAdapterException
     */
    void uploadPreviewStatus() throws CapAdapterException
    {
        m_exportedTime = (Long) m_cxeMessage.getParameters()
                .get("ExportedTime");
        m_exportAbsolutePath = (String) m_cxeMessage.getParameters().get(
                "AbsoluteExportPath");
        m_isComponentPage = (String) m_cxeMessage.getParameters().get(
                "IsComponentPage");
        m_tempExportPath = (String) m_cxeMessage.getParameters().get(
                BaseAdapter.PARAM_ORIGINAL_FILE_CONTENT);
        uploadStatus(true);
    }

    /**
     * Calls the appropriate methods in CAP for Secondary Target File Creation.
     * 
     * @exception GeneralException
     */
    void performStfCreationNotification() throws Exception
    {
        parseEventFlowXml();
        SecondaryTargetFileMgrWLRemote mgr = ServerProxy
                .getSecondaryTargetFileManager();
        FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
        String fileName = fmd.getFile().getAbsolutePath();
        long exportBatchId = Long.parseLong(m_exportBatchId);
        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("Calling stfmgr.createSTF() with : " + fileName + ", "
                    + m_displayName + ", <efxml>, " + exportBatchId);            
        }
        int sourcePageBomType = ExportConstants.NO_UTF_BOM;
        
        try
        {
            if (FileUtil.isUTFFormat(m_originalCharacterEncoding)) {
                if (FileUtil.isNeedBOMProcessing(m_externalPageId)) {
                    File originalFile = new File(AmbFileStoragePathUtils.getCxeDocDir(), m_externalPageId);
                    if (originalFile.exists() && originalFile.isFile()) {
                        String encoding = FileUtil.guessEncoding(originalFile);
                        if (FileUtil.UTF8.equals(encoding)) {
                            sourcePageBomType = ExportConstants.UTF8_WITH_BOM;
                        } else if (FileUtil.UTF16LE.equals(encoding)) {
                            sourcePageBomType = ExportConstants.UTF16_LE;
                        } else if (FileUtil.UTF16BE.equals(encoding)) {
                            sourcePageBomType = ExportConstants.UTF16_BE;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            performStfCreationFailureNotificationIfNeeded();
        }
        mgr.createSecondaryTargetFile(fileName, m_displayName, sourcePageBomType, m_eventFlowXml,
                exportBatchId);
    }

    /**
     * Checks to see if this export was related to secondary target file
     * creation, and if so, notifies the stf manager about the failure as well;
     * otherwise does nothing.
     */
    private void performStfCreationFailureNotificationIfNeeded()
    {
        GeneralException genEx = (GeneralException) m_cxeMessage
                .getParameters().get("Exception");

        // if it is a STF creation and it failed
        if (genEx != null
                && m_cxeMessage.getMessageType().getValue() == CxeMessageType.STF_CREATION_EVENT)
        {
            try
            {
                SecondaryTargetFileMgrWLRemote mgr = ServerProxy
                        .getSecondaryTargetFileManager();
                long exportBatchId = Long.parseLong(m_exportBatchId);
                if (m_logger.isDebugEnabled())
                {
                    m_logger.debug("Calling stfmgr.failed...() with : "
                            + exportBatchId);                    
                }
                mgr.failedToCreateSecondaryTargetFile(exportBatchId);
            }
            catch (Exception e)
            {
                // log it, but don't throw an exception
                m_logger.error("Failed to notify STF manager about error.", e);
            }
        }
    }

    private void uploadStatus(boolean p_isPreview) throws CapAdapterException
    {
        GeneralException genEx = (GeneralException) m_cxeMessage
                .getParameters().get("Exception");
        try
        {
            // m_eventFlowXml = new String (m_eventFlowXml.getBytes("UTF8"),
            // "ISO8859_1");

            if (genEx == null)
                m_exception = null;
            else
            {
                // byte[] xml = genEx.serialize().getBytes("UTF8");
                // m_exception = new String(xml, "ISO8859_1");
                m_exception = genEx.serialize();
            }

            parseEventFlowXml();

            if (genEx != null)
            {
                m_logger.info("Uploading export failure for: " + m_displayName);
            }
            else
            {
                m_logger.info("Uploading export success for: " + m_displayName);
            }
            upload(prepareExportRelatedHTTPString(p_isPreview));
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = "CapTargetAdapter";
            errorArgs[1] = m_displayName;
            m_logger.error("Upload of export status failed.", e);
            throw new CapAdapterException("HTTPEx", errorArgs, e);
        }
    }

    /**
     * Performs the actual upload with the urlencoded line of text
     */
    private void upload(String p_line) throws Exception
    {
        URLConnection conn = m_url.openConnection();
        conn.setDoOutput(true);

        // The string has been URLencoded and consists of ASCII
        // characters only. Still we have to write UTF8.
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(),
                "UTF8");

        wr.write(p_line);
        wr.flush();
        wr.close();

        // Invoke "getInputStream()" method to send out the request to
        // "CapExportServlet".
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                .getInputStream()));
        while ((rd.readLine()) != null)
        {
            /* just read the output */
        }
        rd.close();
    }

    /**
     * Prepares the export status related string for writing to the HTTP stream
     * according to the CAP/CXE Communication Document
     */
    private String prepareExportRelatedHTTPString(boolean p_isPreview)
            throws Exception
    {
        StringBuffer line = new StringBuffer();

        line.append(URLEncoder.encode(ExportConstants.CXE_REQUEST_TYPE));
        line.append(EQUALS);
        line.append(p_isPreview ? PREVIEW_STATUS : EXPORT_STATUS);
        line.append(AMPERSAND);

        line.append(URLEncoder.encode(ExportConstants.RESPONSE_TYPE));
        line.append(EQUALS);
        if (m_exception == null)
        {
            line.append(URLEncoder.encode(EXPORT_SUCCESS));
        }
        else
        {
            line.append(URLEncoder.encode(EXPORT_FAILURE));
        }

        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.MESSAGE_ID));
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_messageId));

        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.EXPORT_BATCH_ID));
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_exportBatchId));

        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.EXPORTED_TIME));
        line.append(EQUALS);
        if (m_exportedTime != null)
            line.append(URLEncoder.encode(m_exportedTime.toString()));
        else
            line.append("0");

        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.ABSOLUTE_EXPORT_PATH));
        line.append(EQUALS);
        if (m_exportAbsolutePath != null)
        {
            line.append(URLEncoder.encode(m_exportAbsolutePath));
        }
        else
        {
            line.append(URLEncoder
                    .encode(ExportConstants.ABSOLUTE_EXPORT_PATH_UNKNOWN));
        }

        // used to aid the gui with presentation of ms-office exports results
        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.IS_COMPONENT_PAGE));
        line.append(EQUALS);
        if (m_isComponentPage != null && m_isComponentPage.equals("true"))
        {
            line.append(URLEncoder.encode("true"));
        }
        else
        {
            line.append(URLEncoder.encode("false"));
        }

        // add in the temp path for the final exported file
        if (m_tempExportPath != null)
        {
            line.append(AMPERSAND);
            line.append(URLEncoder.encode(ExportConstants.TEMP_EXPORT_PATH));
            line.append(EQUALS);
            line.append(URLEncoder.encode(m_tempExportPath));
        }

        line.append(AMPERSAND);
        line.append(URLEncoder.encode(ExportConstants.RESPONSE_DETAILS));
        line.append(EQUALS);
        if (m_exception == null)
            line.append(URLEncoder.encode(""));
        else
            line.append(URLEncoder.encode(m_exception));

        // pass back the original cxe request type so CAP knows what this is
        // related to
        if (m_origCxeRequestType != null)
        {
            line.append(AMPERSAND);
            line.append(URLEncoder
                    .encode(ExportConstants.ORIG_CXE_REQUEST_TYPE));
            line.append(EQUALS);
            line.append(URLEncoder.encode(m_origCxeRequestType));
        }

        // add the page's display name to the request
        if (m_displayName != null)
        {
            line.append(AMPERSAND);
            line.append(URLEncoder.encode(CxeToCapRequest.DISPLAY_NAME));
            line.append(EQUALS);
            line.append(URLEncoder.encode(m_displayName));
        }
        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("prepared ExportRelatedHTTPString=" + line.toString());            
        }
        return line.toString();
    }

    /**
     * Fills member data from parsing the EventFlowXml
     */
    private void parseEventFlowXml() throws Exception
    {
        // first find information from the EventFlowXml
        StringReader sr = new StringReader(m_eventFlowXml);
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
        String formatType = sourceElement.getAttribute("formatType");
        boolean isPassolo = "passolo".equals(formatType);

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

        nl = rootElement.getElementsByTagName("batchId");
        e = (Element) nl.item(0);
        m_batchId = e.getFirstChild().getNodeValue();

        nl = rootElement.getElementsByTagName("pageCount");
        e = (Element) nl.item(0);
        m_pageCount = e.getFirstChild().getNodeValue();

        nl = rootElement.getElementsByTagName("pageNumber");
        e = (Element) nl.item(0);
        m_pageNumber = e.getFirstChild().getNodeValue();

        try
        {
            nl = rootElement.getElementsByTagName("docPageCount");
            e = (Element) nl.item(0);
            m_docPageCount = e.getFirstChild().getNodeValue();
            nl = rootElement.getElementsByTagName("docPageNumber");
            e = (Element) nl.item(0);
            m_docPageNumber = e.getFirstChild().getNodeValue();
        }
        catch (Exception e2)
        {
            // problem may happen due to 6.7 mini batches which weren't there in
            // 6.5
            // ok to use the batch page count and number
            m_logger
                    .warn("Failed to find docPageNumber and docPageCount in event flow xml (old 6.5 job)");
            m_docPageNumber = m_pageNumber;
            m_docPageCount = m_pageCount;
        }

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

        // get the target file name
        if (isPassolo)
        {
            nl = rootElement.getElementsByTagName("source");
        }
        else
        {
            nl = rootElement.getElementsByTagName("target");
        }
        
        if (nl.getLength() > 0)
        {
            Element trg = (Element) nl.item(0);

            String[] values = null;
            NodeList nlT = trg.getElementsByTagName("da");
            for (int i = 0; values == null && i < nlT.getLength(); i++)
            {
                Element daElementT = (Element) nlT.item(i);
                if (daElementT.getAttribute("name").equals("Filename"))
                {
                    NodeList dvt = daElementT.getElementsByTagName("dv");
                    values = new String[dvt.getLength()];
                    for (int j = 0; j < values.length; j++)
                    {
                        Element dv = (Element) dvt.item(j);
                        values[j] = dv.getFirstChild().getNodeValue();
                    }
                }
            }
            if (values != null && values.length > 0)
            {
                // set the display name to the target file name - since
                // the target file name may be a bit different than the source
                // (i.e. .pdf extension to .doc extension)
                // it should be used as the file name
                m_displayName = values[0];
            }
        }
    }
}

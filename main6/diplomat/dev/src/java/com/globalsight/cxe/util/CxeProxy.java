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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.database.DatabaseAdapter;
import com.globalsight.cxe.adapter.documentum.DocumentumOperator;
import com.globalsight.cxe.adapter.msoffice.MsOfficeAdapter;
import com.globalsight.cxe.adapter.pdf.PdfAdapter;
import com.globalsight.cxe.adapter.quarkframe.QuarkFrameAdapter;
import com.globalsight.cxe.adaptermdb.BaseAdapterMDB;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.fileImport.FileImportUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * The CxeProxy class allows clients to make import/export requests to CXE
 * without having to directly use JMS.
 */
public class CxeProxy
{
    static private final Logger s_logger = Logger.getLogger(CxeProxy.class);

    /** The name of the import type parameter. */
    static public final String IMPORT_TYPE = "ImportType";

    /** The normal localization import request. */
    static public final String IMPORT_TYPE_L10N = "l10n";

    static private final Integer ONE = new Integer(1);

    static private HashMap s_targetLocales = new HashMap();

    /**
     * store target locales which were choosed when importing file
     * 
     * @param p_key
     *            key (batchId + fileName + pageNumber) to store target locales
     * @param p_locales
     *            like en_US,zh_CN,zh_TW
     * 
     * @see com.globalsight.webservices.GlobalSight#publishEventToCxe()
     * @see this#
     *      {@link #importFromFileSystem(String, String, String, String, Integer, Integer, Integer, Integer, Boolean, Boolean, String, String, Integer)}
     */
    public static void setTargetLocales(String p_key, String p_locales)
    {
        s_targetLocales.put(p_key, p_locales);
    }

    /**
     * Initiates a file system import.
     * 
     * @param p_fileName
     *            file name relative to the docs directory
     * @param p_jobName
     *            job name
     * @param p_batchId
     *            batch Id
     * @param p_fileProfileId
     *            file profile ID
     * @param p_pageCount
     *            number of pages in the batch
     * @param p_pageNum
     *            number of the current page in the batch (starting from 1)
     * @param p_docPageCount
     *            number of pages in the document
     * @param p_docPageNum
     *            number of the current page in the document (starting from 1)
     * @param p_isAutoImport
     *            FALSE if this is a manual import
     * @param p_importRequestType
     *            IMPORT_TYPE_XXX
     * @param p_importInitiatorId
     *            The user id of the user who initiated the import for this
     *            file.
     * @param p_exitValueByScript
     *            The return value by the script on import.
     */
    static public void importFromFileSystem(String p_fileName, String p_jobName, String jobUuid,
            String p_batchId, String p_fileProfileId, Integer p_pageCount, Integer p_pageNum,
            Integer p_docPageCount, Integer p_docPageNum, Boolean p_isAutoImport,
            String p_importRequestType, String p_importInitiatorId, Integer p_exitValueByScript)
            throws Exception
    {
        importFromFileSystem(p_fileName, p_jobName, jobUuid, p_batchId, p_fileProfileId,
                p_pageCount, p_pageNum, p_docPageCount, p_docPageNum, p_isAutoImport, Boolean.FALSE,
                p_importRequestType, p_importInitiatorId, p_exitValueByScript);
    }

    /**
     * Initiates a file system import.
     * <p>
     * From GBS-2137.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    static public void importFromFileSystem(String p_fileName, String p_jobId, String p_batchId,
            String p_fileProfileId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, Boolean p_isAutoImport,
            Boolean p_overrideFileProfileAsUnextracted, String p_importRequestType,
            Integer p_exitValueByScript, String p_priority) throws Exception
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT);
        CxeMessage cxeMessage = new CxeMessage(type);
        HashMap params = cxeMessage.getParameters();

        Job job = JobCreationMonitor.loadJobFromDB(Long.parseLong(p_jobId));

        params.put(CompanyWrapper.CURRENT_COMPANY_ID, String.valueOf(job.getCompanyId()));
        params.put("Filename", p_fileName);
        params.put("JobId", p_jobId);
        params.put("BatchId", p_batchId);
        params.put("FileProfileId", p_fileProfileId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);
        params.put("IsAutomaticImport", p_isAutoImport);
        params.put("OverrideFileProfileAsUnextracted", p_overrideFileProfileAsUnextracted);
        params.put(IMPORT_TYPE, p_importRequestType);
        params.put("ScriptOnImport", p_exitValueByScript);
        params.put("priority", p_priority);

        if (p_isAutoImport.booleanValue() == false)
        {
            simulateAutoImportIfNeeded(params);
        }

        // some target locales can be unchecked before import
        String key = p_batchId + p_fileName + p_pageNum.intValue();
        if (s_targetLocales.containsKey(key))
        {
            String targetLocales = (String) s_targetLocales.get(key);
            if (!targetLocales.equals(""))
                params.put("TargetLocales", targetLocales);
            s_targetLocales.remove(key);
        }
        params.put("key", key);

        // the uiKey is used in system activity page.
        params.put("uiKey", KeyUtil.generateKey());

        // for GBS-2137, update the new job to "IN_QUEUE" state
        if (Job.UPLOADING.equals(job.getState()))
        {
            JobCreationMonitor.updateJobState(job, Job.IN_QUEUE);
        }

        FileImportUtil.importFileWithThread(cxeMessage);
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public static CxeMessage formCxeMessageType(String p_fileName, String p_jobId, String p_batchId,
            String p_fileProfileId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, Boolean p_isAutoImport,
            Boolean p_overrideFileProfileAsUnextracted, String p_importRequestType,
            Integer p_exitValueByScript, String p_priority, String p_companyId)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT);
        CxeMessage cxeMessage = new CxeMessage(type);
        HashMap params = cxeMessage.getParameters();

        params.put(CompanyWrapper.CURRENT_COMPANY_ID, p_companyId);
        params.put("Filename", p_fileName);
        params.put("JobId", p_jobId);
        params.put("BatchId", p_batchId);
        params.put("FileProfileId", p_fileProfileId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);
        params.put("IsAutomaticImport", p_isAutoImport);
        params.put("OverrideFileProfileAsUnextracted", p_overrideFileProfileAsUnextracted);
        params.put(IMPORT_TYPE, p_importRequestType);
        params.put("ScriptOnImport", p_exitValueByScript);
        params.put("priority", p_priority);

        if (p_isAutoImport.booleanValue() == false)
        {
            simulateAutoImportIfNeeded(params);
        }

        // some target locales can be unchecked before import
        String key = p_batchId + p_fileName + p_pageNum.intValue();
        if (s_targetLocales.containsKey(key))
        {
            String targetLocales = (String) s_targetLocales.get(key);
            if (!targetLocales.equals(""))
                params.put("TargetLocales", targetLocales);
            s_targetLocales.remove(key);
        }
        params.put("key", key);

        // the uiKey is used in system activity page.
        params.put("uiKey", KeyUtil.generateKey());

        return cxeMessage;
    }

    /**
     * Initiates a file system import.
     * 
     * @param p_fileName
     *            file name relative to the docs directory
     * @param p_jobName
     *            job name
     * @param p_batchId
     *            batch Id
     * @param p_fileProfileId
     *            file profile ID
     * @param p_pageCount
     *            number of pages in the batch
     * @param p_pageNum
     *            number of the current page in the batch (starting from 1)
     * @param p_isAutoImport
     *            FALSE if this is a manual import
     * @param p_overrideFileProfileAsUnextracted
     *            - Determines whether the import process is invoked upon a
     *            failure for an extracted file (will override the file
     *            profile's format type).
     * @param p_importRequestType
     *            IMPORT_TYPE_XXX
     * @param p_importInitiatorId
     *            The user id of the user who initiated the import for this
     *            file.
     * @param p_exitValueByScript
     *            The return value by the script on import.
     */
    static public void importFromFileSystem(String p_fileName, String p_jobName, String uuid,
            String p_batchId, String p_fileProfileId, Integer p_pageCount, Integer p_pageNum,
            Integer p_docPageCount, Integer p_docPageNum, Boolean p_isAutoImport,
            Boolean p_overrideFileProfileAsUnextracted, String p_importRequestType,
            String p_importInitiatorId, Integer p_exitValueByScript) throws Exception
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT);
        CxeMessage cxeMessage = new CxeMessage(type);
        HashMap params = cxeMessage.getParameters();
        params.put("Filename", p_fileName);
        String jobName = p_jobName;
        if (p_jobName == null || p_jobName.length() == 0)
        {
            long timeInSec = System.currentTimeMillis() / 1000L;
            jobName = "job_" + timeInSec;
        }

        // To make JMS message support CompanyThreadLocal, we have to do so.
        String currentCompanyId = getCompanyIdByFileProfileId(p_fileProfileId);
        params.put(CompanyWrapper.CURRENT_COMPANY_ID, currentCompanyId);

        params.put("JobName", jobName);
        params.put("uuid", uuid);
        params.put("BatchId", p_batchId);
        params.put("FileProfileId", p_fileProfileId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);
        params.put("IsAutomaticImport", p_isAutoImport);
        params.put("OverrideFileProfileAsUnextracted", p_overrideFileProfileAsUnextracted);
        params.put(IMPORT_TYPE, p_importRequestType);
        params.put("ImportInitiator", p_importInitiatorId);
        params.put("ScriptOnImport", p_exitValueByScript);

        if (p_isAutoImport.booleanValue() == false)
        {
            simulateAutoImportIfNeeded(params);
        }

        // make target locales selectable when import file
        String key = p_batchId + p_fileName + p_pageNum.intValue();
        if (s_targetLocales.containsKey(key))
        {
            String targetLocales = (String) s_targetLocales.get(key);
            if (!targetLocales.equals(""))
                params.put("TargetLocales", targetLocales);
            s_targetLocales.remove(key);
        }

        // GBS-4400
        FileImportUtil.importFileWithThread(cxeMessage);
    }

    static public void importFromFileSystem(String p_fileName, long p_jobId, String p_jobName,
            String uuid, String p_batchId, String p_fileProfileId, Integer p_pageCount,
            Integer p_pageNum, Integer p_docPageCount, Integer p_docPageNum, Boolean p_isAutoImport,
            Boolean p_overrideFileProfileAsUnextracted, String p_importRequestType,
            String p_importInitiatorId, Integer p_exitValueByScript, String p_priority)
            throws Exception
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT);
        CxeMessage cxeMessage = new CxeMessage(type);
        HashMap params = cxeMessage.getParameters();
        params.put("Filename", p_fileName);
        String jobName = p_jobName;
        if (p_jobName == null || p_jobName.length() == 0)
        {
            long timeInSec = System.currentTimeMillis() / 1000L;
            jobName = "job_" + timeInSec;
        }

        // To make JMS message support CompanyThreadLocal, we have to do so.
        String currentCompanyId = getCompanyIdByFileProfileId(p_fileProfileId);
        params.put(CompanyWrapper.CURRENT_COMPANY_ID, currentCompanyId);
        params.put("JobId", String.valueOf(p_jobId));
        params.put("JobName", jobName);
        params.put("uuid", uuid);
        params.put("BatchId", p_batchId);
        params.put("FileProfileId", p_fileProfileId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);
        params.put("IsAutomaticImport", p_isAutoImport);
        params.put("OverrideFileProfileAsUnextracted", p_overrideFileProfileAsUnextracted);
        params.put(IMPORT_TYPE, p_importRequestType);
        params.put("ImportInitiator", p_importInitiatorId);
        params.put("ScriptOnImport", p_exitValueByScript);
        params.put("priority", p_priority);

        if (p_isAutoImport.booleanValue() == false)
        {
            simulateAutoImportIfNeeded(params);
        }

        // make target locales selectable when import file
        String key = p_batchId + p_fileName + p_pageNum.intValue();
        if (s_targetLocales.containsKey(key))
        {
            String targetLocales = (String) s_targetLocales.get(key);
            if (!targetLocales.equals(""))
                params.put("TargetLocales", targetLocales);
            s_targetLocales.remove(key);
        }
        // GBS-4400
        FileImportUtil.importFileWithThread(cxeMessage);
    }

    /**
     * Modifies the parameters in the HashMap to simulate certain aspects of
     * auto import with a batch size of 1, including 1 page per job and the
     * jobname being the page name. If the value of the property
     * import.manualImportSingleBatch in envoy.properties is false, then nothing
     * happens here.
     */
    static private void simulateAutoImportIfNeeded(HashMap p_params)
    {
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();

            if (config.getBooleanParameter("import.manualImportSingleBatch") == true)
            {
                s_logger.info("using manual import single batch");

                String filename = (String) p_params.get("Filename");
                int slashIdx = filename.lastIndexOf("/");
                int bslashIdx = filename.lastIndexOf("\\");
                int idx = (slashIdx > bslashIdx) ? slashIdx : bslashIdx;

                idx++;

                String baseName = filename.substring(idx);
                p_params.put("JobName", baseName);
                p_params.put("PageCount", ONE);
                p_params.put("PageNum", ONE);
                p_params.put("DocPageCount", ONE);
                p_params.put("DocPageNum", ONE);
                String newBatchId = baseName.hashCode() + Long.toString(System.currentTimeMillis());
                p_params.put("BatchId", newBatchId);
            }
        }
        catch (Exception ex)
        {
            s_logger.error("Could not handle import.manualImportSingleBatch processing.", ex);
        }
    }

    /**
     * @see exportFile(String, MessageData, String, String, String, int, String,
     *      String, String String, Integer, Integer, Integer, Integer, long,
     *      boolean, String, boolean).
     */
    static public void exportFile(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, boolean p_isUnextracted, String p_fileName,
            int p_sourcePageBomType, boolean p_isFinalExport, String p_companyId) throws Exception
    {
        exportFile(p_eventFlowXml, p_gxml, p_cxeRequestType, p_targetLocale, p_targetCharset,
                p_bomType, p_messageId, p_exportLocation, p_localeSubDir, p_exportBatchId,
                p_pageCount, p_pageNum, p_docPageCount, p_docPageNum, null, 0, false,
                p_isUnextracted, p_fileName, p_sourcePageBomType, p_isFinalExport, p_companyId);
    }

    /**
     * Initiates an export within CXE (Override method for documentum workflow
     * Id).
     * 
     * @param p_eventFlowXml
     *            string of event flow xml
     * @param p_gxmlFileName
     *            filename of a file that contains GXML
     * @param p_cxeRequestType
     *            CXE Request Type
     * @param p_targetLocale
     *            target locale
     * @param p_targetCharset
     *            target encoding
     * @param p_bomType
     *            BOM type
     * @param p_messageId
     *            export message ID
     * @param p_exportLocation
     *            export location
     * @param p_localeSubDir
     * @param p_exportBatchId
     * @param p_pageCount
     * @param p_pageNum
     * @param p_docPageCount
     * @param p_docPageNum
     * @param wfId
     * @param p_isUnextracted
     * @param p_fileName
     *            The name of the file to be exported (relative path)
     */
    static public void exportFile(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, String newObjId, long wfId, boolean isJobDone,
            boolean p_isUnextracted, String p_fileName, int p_sourcePageBomType,
            boolean p_isFinalExport, String p_companyId) throws Exception
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("CXE Export Request: (requestType=" + p_cxeRequestType + ", " + "pageId="
                    + p_messageId + ", " + "page " + p_docPageNum + " of " + p_docPageCount + ", "
                    + "targetLocale=" + p_targetLocale + ", " + "unextracted=" + p_isUnextracted
                    + ", " + "dir=" + p_localeSubDir + ")");
        }

        exportFile(p_eventFlowXml, p_gxml, p_cxeRequestType, p_targetLocale, p_targetCharset,
                p_bomType, p_messageId, p_exportLocation, p_localeSubDir, p_exportBatchId,
                p_pageCount, p_pageNum, p_docPageCount, p_docPageNum, newObjId, wfId, isJobDone,
                p_isUnextracted, null, p_fileName, p_sourcePageBomType, p_isFinalExport,
                p_companyId);
    }

    /**
     * Export for dynamic preview
     * 
     * @param p_eventFlowXml
     * @param p_gxml
     * @param p_cxeRequestType
     * @param p_targetLocale
     * @param p_targetCharset
     * @param p_bomType
     *            BOM Type
     * @param p_messageId
     * @param p_exportLocation
     * @param p_localeSubDir
     * @param p_exportBatchId
     * @param p_pageCount
     * @param p_pageNum
     * @param p_sessionId
     */
    static public void exportForDynamicPreview(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, String p_sessionId)
            throws Exception, IOException
    {
        exportFile(p_eventFlowXml, p_gxml, p_cxeRequestType, p_targetLocale, p_targetCharset,
                p_bomType, p_messageId, p_exportLocation, p_localeSubDir, p_exportBatchId,
                p_pageCount, p_pageNum, /* TODO */ONE, ONE, null, 0, false, false, p_sessionId,
                null, 0, false, CompanyThreadLocal.getInstance().getValue());
    }

    /**
     * Returns true if the MS Office Adapter is installed
     * 
     * @return true | false
     */
    static public boolean isMsOfficeAdapterInstalled()
    {
        return MsOfficeAdapter.isInstalled();
    }

    /**
     * Returns true if the Database Adapter is installed
     * 
     * @return true | false
     */
    static public boolean isDatabaseAdapterInstalled()
    {
        return DatabaseAdapter.isInstalled();
    }

    /**
     * Returns true if the Pdf Adapter is installed
     * 
     * @return true | false
     */
    static public boolean isPdfAdapterInstalled()
    {
        return PdfAdapter.isInstalled();
    }

    /**
     * Returns true if the Desktop Publishing (Quark/Frame) Adapter is installed
     * 
     * @return true | false
     */
    static public boolean isQuarkFrameAdapterInstalled()
    {
        return QuarkFrameAdapter.isInstalled();
    }

    // /////////////////////
    // Private methods //
    // ////////////////////

    /**
     * Initiates an export within CXE.
     * 
     * @param p_eventFlowXml
     *            string of event flow xml
     * @param p_gxmlFileName
     *            filename of a file that contains GXML
     * @param p_cxeRequestType
     *            CXE Request Type
     * @param p_targetLocale
     *            target locale
     * @param p_targetCharset
     *            target encoding
     * @param p_messageId
     *            export message ID
     * @param p_exportLocation
     *            export location
     * @param p_localeSubDir
     * @param p_exportBatchId
     * @param p_pageCount
     * @param p_pageNum
     * @param p_isUnextracted
     * @param p_sessionId
     * @param p_fileName
     *            The name of the file that is being exported (relative path)
     */
    static private void exportFile(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, String newObjId, long wfId, boolean isJobDone,
            boolean p_isUnextracted, String p_sessionId, String p_fileName, int p_sourcePageBomType,
            boolean p_isFinalExport, String p_companyId) throws Exception
    {
        CxeMessageType type;
        if (p_isUnextracted)
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT);
        }
        else
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.GXML_LOCALIZED_EVENT);
        }

        // Handle STF created files as unextracted as well if it's not
        // the STF creation, but a regular STF export.
        if (ExportConstants.EXPORT_STF.equals(p_cxeRequestType))
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT);
        }

        CxeMessage exportMsg = new CxeMessage(type);
        HashMap params = new HashMap();

        if (p_targetCharset != null && p_targetCharset.endsWith("_di"))
        {
            p_targetCharset = p_targetCharset.replace("_di", "");
        }

        else if ("Unicode Escape".equalsIgnoreCase(p_targetCharset))
        {
            p_targetCharset = "UTF-8";
            params.put("UnicodeEscape", "true");
        }
        else
        {
            params.put("UnicodeEscape", "false");
        }
        if ("Entity Escape".equalsIgnoreCase(p_targetCharset))
        {
            p_targetCharset = "ISO-8859-1";
            params.put("EntityEscape", "true");
        }
        else
        {
            params.put("EntityEscape", "false");
        }
        params.put(CompanyWrapper.CURRENT_COMPANY_ID, p_companyId);
        params.put("TargetLocale", p_targetLocale);
        params.put("TargetCharset", p_targetCharset);
        params.put("BOMType", Integer.valueOf(p_bomType));
        params.put("CxeRequestType", p_cxeRequestType);
        params.put("MessageId", p_messageId);
        params.put("ExportLocation", p_exportLocation);
        params.put("LocaleSubDir", p_localeSubDir);
        params.put("ExportBatchId", p_exportBatchId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);

        params.put(DocumentumOperator.DCTM_NEWOBJECTID, newObjId);
        params.put(DocumentumOperator.DCTM_WORKFLOWID, String.valueOf(wfId));
        params.put(DocumentumOperator.DCTM_ISJOBDONE, new Boolean(isJobDone));

        params.put("SessionId", p_sessionId);
        params.put("TargetFileName", p_fileName);
        params.put("SourcePageBomType", Integer.valueOf(p_sourcePageBomType));
        params.put("IsFinalExport", new Boolean(p_isFinalExport));

        exportMsg.setParameters(params);
        exportMsg.setEventFlowXml(p_eventFlowXml);
        exportMsg.setMessageData(p_gxml);

        try
        {
            handleCxeMessage(exportMsg);
        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    /**
     * Handles the CxeMessage.
     * 
     * @param cxeMessage
     * @throws Exception
     */
    static private void handleCxeMessage(CxeMessage cxeMessage) throws Exception
    {
        if (cxeMessage == null)
        {
            return;
        }

        CxeMessageType eventName = cxeMessage.getMessageType();

        BaseAdapterMDB adapter = EventTopicMap.getBaseAdapterMDB(eventName);

        List<CxeMessage> cms = adapter.handlerAdapterResults(cxeMessage);

        for (CxeMessage c : cms)
        {
            handleCxeMessage(c);
        }

        return;
    }

    /**
     * Initiates an export within CXE (Override method for documentum workflow
     * Id).
     * 
     * @param p_eventFlowXml
     *            string of event flow xml
     * @param p_gxmlFileName
     *            filename of a file that contains GXML
     * @param p_cxeRequestType
     *            CXE Request Type
     * @param p_targetLocale
     *            target locale
     * @param p_targetCharset
     *            target encoding
     * @param p_bomType
     *            BOM type
     * @param p_messageId
     *            export message ID
     * @param p_exportLocation
     *            export location
     * @param p_localeSubDir
     * @param p_exportBatchId
     * @param p_pageCount
     * @param p_pageNum
     * @param p_docPageCount
     * @param p_docPageNum
     * @param wfId
     * @param p_isUnextracted
     * @param p_fileName
     *            The name of the file to be exported (relative path)
     */
    static public CxeMessage getExportCxeMessage(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, String newObjId, long wfId, boolean isJobDone,
            boolean p_isUnextracted, String p_fileName, int p_sourcePageBomType,
            boolean p_isFinalExport, String p_companyId) throws Exception
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("CXE Export Request: (requestType=" + p_cxeRequestType + ", " + "pageId="
                    + p_messageId + ", " + "page " + p_docPageNum + " of " + p_docPageCount + ", "
                    + "targetLocale=" + p_targetLocale + ", " + "unextracted=" + p_isUnextracted
                    + ", " + "dir=" + p_localeSubDir + ")");
        }

        return getCxeMessage(p_eventFlowXml, p_gxml, p_cxeRequestType, p_targetLocale,
                p_targetCharset, p_bomType, p_messageId, p_exportLocation, p_localeSubDir,
                p_exportBatchId, p_pageCount, p_pageNum, p_docPageCount, p_docPageNum, newObjId,
                wfId, isJobDone, p_isUnextracted, null, p_fileName, p_sourcePageBomType,
                p_isFinalExport, p_companyId);
    }

    static private CxeMessage getCxeMessage(String p_eventFlowXml, MessageData p_gxml,
            String p_cxeRequestType, String p_targetLocale, String p_targetCharset, int p_bomType,
            String p_messageId, String p_exportLocation, String p_localeSubDir,
            String p_exportBatchId, Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, String newObjId, long wfId, boolean isJobDone,
            boolean p_isUnextracted, String p_sessionId, String p_fileName, int p_sourcePageBomType,
            boolean p_isFinalExport, String p_companyId) throws Exception
    {
        CxeMessageType type;
        if (p_isUnextracted)
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT);
        }
        else
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.GXML_LOCALIZED_EVENT);
        }

        // Handle STF created files as unextracted as well if it's not
        // the STF creation, but a regular STF export.
        if (ExportConstants.EXPORT_STF.equals(p_cxeRequestType))
        {
            type = CxeMessageType.getCxeMessageType(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT);
        }

        CxeMessage exportMsg = new CxeMessage(type);
        HashMap params = new HashMap();

        if (p_targetCharset != null && p_targetCharset.endsWith("_di"))
        {
            p_targetCharset = p_targetCharset.replace("_di", "");
        }

        else if ("Unicode Escape".equalsIgnoreCase(p_targetCharset))
        {
            p_targetCharset = "UTF-8";
            params.put("UnicodeEscape", "true");
        }
        else
        {
            params.put("UnicodeEscape", "false");
        }
        if ("Entity Escape".equalsIgnoreCase(p_targetCharset))
        {
            p_targetCharset = "ISO-8859-1";
            params.put("EntityEscape", "true");
        }
        else
        {
            params.put("EntityEscape", "false");
        }
        params.put(CompanyWrapper.CURRENT_COMPANY_ID, p_companyId);
        params.put("TargetLocale", p_targetLocale);
        params.put("TargetCharset", p_targetCharset);
        params.put("BOMType", Integer.valueOf(p_bomType));
        params.put("CxeRequestType", p_cxeRequestType);
        params.put("MessageId", p_messageId);
        params.put("ExportLocation", p_exportLocation);
        params.put("LocaleSubDir", p_localeSubDir);
        params.put("ExportBatchId", p_exportBatchId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);

        params.put(DocumentumOperator.DCTM_NEWOBJECTID, newObjId);
        params.put(DocumentumOperator.DCTM_WORKFLOWID, String.valueOf(wfId));
        params.put(DocumentumOperator.DCTM_ISJOBDONE, new Boolean(isJobDone));

        params.put("SessionId", p_sessionId);
        params.put("TargetFileName", p_fileName);
        params.put("SourcePageBomType", Integer.valueOf(p_sourcePageBomType));
        params.put("IsFinalExport", new Boolean(p_isFinalExport));

        exportMsg.setParameters(params);
        exportMsg.setEventFlowXml(p_eventFlowXml);
        exportMsg.setMessageData(p_gxml);

        return exportMsg;
    }

    /**
     * Initiates a Documentum import.
     * 
     * @param p_objID
     *            Documentum object ID
     * @param p_filename
     *            path based filename
     * @param p_jobName
     *            job name
     * @param p_batchId
     *            batch Id
     * @param p_fileProfileId
     *            file profile ID
     * @param p_pageCount
     *            number of pages in the batch
     * @param p_pageNum
     *            number of the current page in the batch (starting from 1)
     * @param dctmFileAttrXml
     *            - the xml string for DCTM file attributes.
     */
    static public void importFromDocumentum(String p_objID, String p_fileName, String p_jobName,
            String p_batchId, String p_fileProfileId, Integer p_pageCount, Integer p_pageNum,
            Integer p_docPageCount, Integer p_docPageNum, boolean isAttrFile,
            String dctmFileAttrXml, String userId) throws Exception
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT);
        CxeMessage cxeMessage = new CxeMessage(type);
        HashMap params = cxeMessage.getParameters();
        params.put(DocumentumOperator.DCTM_OBJECTID, p_objID);
        // params.put("DocbaseName", p_docbaseName);
        String companyId = getCompanyIdByFileProfileId(p_fileProfileId);
        params.put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
        params.put("Filename", p_fileName);
        String jobName = p_jobName;
        if (p_jobName == null || p_jobName.length() == 0)
        {
            long timeInSec = System.currentTimeMillis() / 1000L;
            jobName = "dctmjob_" + timeInSec;
        }

        params.put("JobName", jobName);
        params.put("BatchId", p_batchId);
        params.put("FileProfileId", p_fileProfileId);
        params.put("PageCount", p_pageCount);
        params.put("PageNum", p_pageNum);
        params.put("DocPageCount", p_docPageCount);
        params.put("DocPageNum", p_docPageNum);

        params.put(DocumentumOperator.DCTM_ISATTRFILE, new Boolean(isAttrFile));
        params.put(DocumentumOperator.DCTM_FILEATTRXML, dctmFileAttrXml);
        params.put(DocumentumOperator.DCTM_USERID, userId);

        params.put("IsAutomaticImport", Boolean.FALSE);
        params.put("OverrideFileProfileAsUnextracted", Boolean.FALSE);

        // GBS-4400
        FileImportUtil.importFileWithThread(cxeMessage);
    }

    private static String getCompanyIdByFileProfileId(String p_fileProfileId)
    {
        String companyId = null;
        try
        {
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(Long.parseLong(p_fileProfileId), false);
            companyId = String.valueOf(fp.getCompanyId());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return companyId;
    }
}
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
package com.globalsight.cxe.util.fileImport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.filesystem.FileSystemAdapterException;
import com.globalsight.cxe.adapter.filesystem.Importer;
import com.globalsight.cxe.adaptermdb.BaseAdapterMDB;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.cxe.util.fileImport.eventFlow.Source;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestFactory;
import com.globalsight.everest.request.RequestHandlerLocal;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.request.RequestPersistenceAccessor;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ObjectUtil;
import com.globalsight.util.PropertiesFactory;
import com.globalsight.util.edit.EditUtil;

/**
 * The FileImportUtil class allows clients to make import requests without
 * having to use JMS.   
 */
public class FileImportUtil
{
    static private final Logger logger = Logger.getLogger(FileImportUtil.class);

    public static Object LOCKER = new Object();

    // max size (in bytes) of the job name
    public static final int MAX_JOBNAME_SIZE = 320;

    public static boolean USE_JMS = false;
    public static HashMap<String, Integer> RUN_MAX_MESSAGE = new HashMap<String, Integer>();
    public static HashMap<String, Integer> ON_RUN_MESSAGE = new HashMap<String, Integer>();
    public static HashMap<String, List<CxeMessage>> ON_HOLD_MESSAGE = new HashMap<String, List<CxeMessage>>();

    public static HashMap<String, CxeMessage> WAITING_REQUEST = new HashMap<String, CxeMessage>();
    public static HashMap<String, CxeMessage> RUNNING_REQUEST = new HashMap<String, CxeMessage>();
    public static HashMap<String, List<BatchInfo>> CANCELED_REQUEST = new HashMap<String, List<BatchInfo>>();

    // initialize the USE_JMS and RUN_MAX_MESSAGE from
    // "properties/createJob.properties"
    static
    {
        try
        {
            Properties p = (new PropertiesFactory())
                    .getProperties("/properties/createJob.properties");
            USE_JMS = "true".equalsIgnoreCase(p.getProperty("useJms"));

            Set<Object> keys = p.keySet();
            for (Object key : keys)
            {
                if ("useJms".equals(key))
                    continue;

                String type = (String) key;
                type = type.trim().toLowerCase();
                RUN_MAX_MESSAGE.put(type,
                        Integer.parseInt(p.getProperty((String) key)));
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    /**
     * Cancel the unimport file.
     * 
     * @param key
     * @return found the file or not.
     */
    static public boolean cancelUnimportFile(String key)
    {
        CxeMessage m = null;
        synchronized (LOCKER)
        {
            m = WAITING_REQUEST.get(key);
            if (m == null)
                return false;

            String name = getName(m);
            ON_HOLD_MESSAGE.get(name).remove(m);
            WAITING_REQUEST.remove(key);
        }

        handleCancelEvent(m);

        return true;
    }
    
    static public HashMap<String, CxeMessage> getCloneRunningRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(RUNNING_REQUEST);
        }
    }
    
    static public HashMap<String, List<CxeMessage>> getCloneHoldingRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(ON_HOLD_MESSAGE);
        }
    }
    

    /**
     * This method uses the thread to create job, not JMS.
     * 
     * @param cxeMessage
     */
    static public void importFileWithThread(CxeMessage cxeMessage)
    {
        FileImportRunnable runnable = new FileImportRunnable(cxeMessage);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    @SuppressWarnings("rawtypes")
    static public String getName(CxeMessage cxeMessage)
    {
        HashMap map = cxeMessage.getParameters();
        String name = null;
        String fileProfileId = (String) map.get("FileProfileId");
        FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class,
                Long.parseLong(fileProfileId));
        // Check if it is from XLZ file
        if (fp == null)
        {
            String hql = "from FileProfileImpl fp where fp.referenceFP = ?";
            fp = (FileProfileImpl) HibernateUtil.getFirst(hql, Long.parseLong(fileProfileId));
            
            if (fp != null && 48 == fp.getKnownFormatTypeId())
            {
                name = "xlz";
            }
        }

        if (name == null)
        {
            String file = (String) map.get("Filename");
            name = getSuffix(file);
        }
        
        return name;
    }
    
    /**
     * Creates job without JMS.
     * 
     * @param cxeMessage
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public void importFile(CxeMessage cxeMessage)
    {
        HashMap map = cxeMessage.getParameters();
        String key = (String) map.get("uiKey");
        
        // this is first time.
        if (map.get("requestTime") == null)
        {
            map.put("requestTime", new Date());
            
            // the order is according to sort priority, sort time and sort axis.
            map.put("sortTime", new Date().getTime());
            
            String n1 = (String) map.get("priority");
            if (n1 == null)
                n1 = "3";
            map.put("sortPriority", Integer.parseInt(n1));
            map.put("sortAxis", 1);
        }

        String name = null;
        // hold the message if the importing file is upper limit
        synchronized (LOCKER)
        {
            name = getName(cxeMessage);
            Integer max = RUN_MAX_MESSAGE.get(name);
            if (max == null)
                max = 10;

            Integer run = ON_RUN_MESSAGE.get(name);
            if (run == null)
                run = 0;

            if (run >= max)
            {
                List<CxeMessage> ms = ON_HOLD_MESSAGE.get(name);
                if (ms == null)
                {
                    ms = new ArrayList<CxeMessage>();
                    ON_HOLD_MESSAGE.put(name, ms);
                }

                ms.add(cxeMessage);
                sortMessages(ms);
                
                WAITING_REQUEST.put(key, cxeMessage);
                return;
            }
            else
            {
                ON_RUN_MESSAGE.put(name, run + 1);
            }
        }

        try
        {
            synchronized (LOCKER)
            {
                WAITING_REQUEST.remove(key);
                map.put("startTime", new Date());
                RUNNING_REQUEST.put(key, cxeMessage);
            }

            CxeMessage c = handleSelectedFileEvent(cxeMessage);
            handleCxeMessage(c);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            HibernateUtil.closeSession();
            
            RUNNING_REQUEST.remove(key);

            // Import a file that has been hold.
            CxeMessage holdMsg = null;
            synchronized (LOCKER)
            {
                Integer run = ON_RUN_MESSAGE.get(name);
                if (run != null)
                {
                    ON_RUN_MESSAGE.put(name, run - 1);

                    List<CxeMessage> ms = ON_HOLD_MESSAGE.get(name);
                    if (ms != null && ms.size() > 0)
                    {
                        holdMsg = ms.remove(0);
                    }
                }
            }

            if (holdMsg != null)
            {
                importFile(holdMsg);
            }
        }
    }
    
    public static void sortWaitingMessage()
    {
        synchronized (LOCKER)
        {
            for (List<CxeMessage> ms : ON_HOLD_MESSAGE.values())
            {
                sortMessages(ms);
            }
        }
    }
    
    private static void sortMessages(List<CxeMessage> ms) 
    {
        Collections.sort(ms, new Comparator<CxeMessage>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public int compare(CxeMessage o1, CxeMessage o2)
            {
                HashMap p1 = o1.getParameters();
                HashMap p2 = o2.getParameters();
                
                int n1 = (Integer) p1.get("sortPriority");
                
                int n2 = (Integer) p2.get("sortPriority");
                
                int k = n1 - n2;
                if (k != 0)
                    return k;
                
                long d1 = (Long) p1.get("sortTime");
                long d2 = (Long) p2.get("sortTime");
                
                k = (int) (d1 - d2);
                
                if (k != 0)
                    return k;
                
                int axis1 = (Integer) p1.get("sortAxis");
                int axis2 = (Integer) p2.get("sortAxis");
                return (int) (axis1 - axis2);
            }
        });
    }

    /**
     * Gets the suffix file name. Take "c:/a.docx" for example, the return value
     * should be "docx".
     * 
     * @param fileName
     * @return
     */
    static private String getSuffix(String fileName)
    {
        fileName = fileName.replace("\\", "/");
        int n = fileName.lastIndexOf("/");
        fileName = fileName.substring(n);
        n = fileName.lastIndexOf(".");
        if (n > 0)
        {
            fileName = fileName.substring(n + 1);
        }

        return fileName.toLowerCase();
    }

    /**
     * Deletes the job.
     * 
     * @param id
     */
    private static void cancelJob(long id)
    {
        JobImpl job = HibernateUtil.get(JobImpl.class, id);
        if (job != null)
        {
            try
            {
                HibernateUtil.delete(job);
            }
            catch (Exception e)
            {
                logger.info(e);
            }
        }

    }

    /**
     * handles the cancel request.
     * 
     * @param p_cxeMessage
     */
    @SuppressWarnings("rawtypes")
    private static void handleCancelEvent(CxeMessage p_cxeMessage)
    {
        HashMap params = p_cxeMessage.getParameters();
        String batchId = (String) params.get("BatchId");
        int pageCount = ((Integer) params.get("PageCount")).intValue();
        if (pageCount == 1)
        {
            String jobId = (String) params.get("JobId");
            cancelJob(Long.parseLong(jobId));
            return;
        }

        int pageNumber = ((Integer) params.get("PageNum")).intValue();
        int docPageCount = ((Integer) params.get("DocPageCount")).intValue();
        int docPageNumber = ((Integer) params.get("DocPageNum")).intValue();

        BatchInfo bi = new BatchInfo(batchId, pageCount, pageNumber,
                docPageCount, docPageNumber);
        List<BatchInfo> batchInfos = CANCELED_REQUEST.get(batchId);
        if (batchInfos == null)
        {
            batchInfos = new ArrayList<BatchInfo>();
            CANCELED_REQUEST.put(batchId, batchInfos);
        }
        batchInfos.add(bi);
    }

    /**
     * Handles the import case where a user has selected a file and a
     * "selected file event" comes in to the file system adapter
     * 
     * @param p_cxeMessage
     *            CxeMessage of type FILE_SYSTEM_FILE_SELECTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    @SuppressWarnings("rawtypes")
    static private CxeMessage handleSelectedFileEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        CxeMessage results = null;
        String filename = null;
        try
        {
            HashMap params = p_cxeMessage.getParameters();

            filename = (String) params.get("Filename");
            boolean isAutomaticImport = ((Boolean) params
                    .get("IsAutomaticImport")).booleanValue();
            Importer importer = new Importer(p_cxeMessage, logger);

            EventFlowXml eventFlowObject = importer
                    .makeEventFlowXmlObject(isAutomaticImport);
            p_cxeMessage.setEventFlowObject(eventFlowObject);
            HashMap newParams = params;
            MessageData newMessageData = importer.readFile();
            CxeMessageType newMessageType = importer.getPreExtractEvent();
            BaseAdapter.preserveOriginalFileContent(newMessageData, newParams);

            p_cxeMessage.setMessageData(newMessageData);
            // If the script on import return value 1, GlobalSight will report
            // an error on import.
            Object scriptOnImport = params.get("ScriptOnImport");
            if (scriptOnImport != null
                    && ((Integer) scriptOnImport).intValue() == 1)
            {
                String[] errorArgs = new String[2];
                errorArgs[0] = logger.getName();
                errorArgs[1] = filename;
                FileSystemAdapterException fsae = new FileSystemAdapterException(
                        "ScriptOnImport", errorArgs, new Exception());
                results = makeImportError(p_cxeMessage, fsae);

                return results;
            }

            CxeMessage outputMessage = new CxeMessage(newMessageType);
            outputMessage.setMessageData(newMessageData);
            outputMessage.setParameters(newParams);
            outputMessage.setEventFlowObject(eventFlowObject);

            return outputMessage;
        }
        catch (FileSystemAdapterException fsae)
        {
            results = makeImportError(p_cxeMessage, fsae);
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = logger.getName();
            errorArgs[1] = filename;
            FileSystemAdapterException fsae = new FileSystemAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeImportError(p_cxeMessage, fsae);
        }

        return results;
    }

    /**
     * Handles the CxeMessage.
     * 
     * @param cxeMessage
     * @throws Exception
     */
    static private void handleCxeMessage(CxeMessage cxeMessage)
            throws Exception
    {
        if (cxeMessage == null)
        {
            return;
        }

        CxeMessageType eventName = cxeMessage.getMessageType();
        String name = eventName.getName();
        BaseAdapterMDB adapter = EventHandlerMap.getHandler(name);
        if (adapter != null)
        {
            List<CxeMessage> cms = adapter.handlerAdapterResults(cxeMessage);

            for (CxeMessage c : cms)
            {
                handleCxeMessage(c);
            }
        }
        else if ("GXML_CREATED_EVENT".equalsIgnoreCase(name)
                || "CXE_IMPORT_ERROR_EVENT".equalsIgnoreCase(name))
        {
            int requestType = Request.EXTRACTED_LOCALIZATION_REQUEST;

            if (cxeMessage.getParameters().get("Exception") != null)
                requestType = Request.REQUEST_WITH_CXE_ERROR;

            GeneralException exception = (GeneralException) cxeMessage
                    .getParameters().get("Exception");
            addRequest(cxeMessage, requestType, cxeMessage.getMessageData()
                    .getName(), cxeMessage.getEventFlowObject(), exception);
        }
        else
        {
            throw new Exception("Can not handle event: " + name);
        }

        return;
    }

    /**
     * Adds the request to GS.
     * 
     * @param p_cxeMessage
     * @param p_requestType
     * @param p_contentFileName
     * @param p_eventFlowXml
     * @param p_exception
     * @throws Exception
     */
    static private void addRequest(CxeMessage p_cxeMessage, int p_requestType,
            String p_contentFileName, EventFlowXml p_eventFlowXml,
            GeneralException p_exception) throws Exception
    {
        // 1.Create the request object
        RequestImpl req = prepareL10nRequest(p_cxeMessage, p_requestType,
                p_contentFileName, p_eventFlowXml, p_exception);

        // 2.Submit request
        RequestHandlerLocal h = new RequestHandlerLocal();
        h.submitRequest(req);
    }

    /**
     * Update the request with the event flow xml.
     * 
     * @param r
     * @param eventFlowXml
     */
    static private void updateRequest(RequestImpl r, EventFlowXml eventFlowXml)
    {
        Source source = eventFlowXml.getSource();

        // external page id
        String externalPageId = eventFlowXml.getBatchInfo().getDisplayName();

        // set the values that were sent in and not part of the
        // constructor
        if (externalPageId != null)
        {
            r.setExternalPageId(externalPageId);
        }

        // source encoding
        String originalSourceEncoding = source.getCharset();
        if (originalSourceEncoding != null)
        {
            r.setSourceEncoding(originalSourceEncoding);
        }

        // data source type
        String dataSourceType = source.getDataSourceType();
        if (dataSourceType != null)
        {
            r.setDataSourceType(dataSourceType);
        }

        r.setPriority(eventFlowXml.getBatchInfo().getPriority());

        // data source id
        String dataSourceIdString = source.getDataSourceId();
        long dataSourceId = -1;
        if (dataSourceIdString != null && dataSourceIdString.length() > 0)
        {
            dataSourceId = Long.parseLong(dataSourceIdString);
        }

        r.setDataSourceId(dataSourceId);

        // is page preview able - convert to boolean
        r.setPageCxePreviewable(CxeToCapRequest.TRUE.equals(source
                .getPageIsCxePreviewable()));
        r.setBaseHref(eventFlowXml.getBatchInfo().getBaseHref());

        // set target locale
        setUnimportedLocales(eventFlowXml, r);
    }

    /**
     * Gets the L10nProfile specified in the EventFlowXml.
     * 
     * @param eventFlowXml
     * @return
     * @throws Exception
     */
    static L10nProfile getL10nProfile(EventFlowXml eventFlowXml)
            throws Exception
    {
        // l10nProfileId
        String l10nProfileIdAsString = eventFlowXml.getBatchInfo()
                .getL10NProfileId();
        long l10nProfileId = -1;
        if (l10nProfileIdAsString != null)
        {
            l10nProfileId = Long.parseLong(l10nProfileIdAsString);
        }

        // get the localization profile
        ProjectHandler ph = new ProjectHandlerLocal();
        return ph.getL10nProfile(l10nProfileId);
    }

    /**
     * Creates a request from all the information passed in. Must parse through
     * the L10nRequest Xml. This also stores some of the information of the
     * request that won't be part of the page.
     */
    static private RequestImpl prepareL10nRequest(CxeMessage p_cxeMessage,
            int p_requestType, String p_gxml, EventFlowXml eventFlowXml,
            GeneralException p_exception) throws Exception
    {
        Source source = eventFlowXml.getSource();
        L10nProfile profile = getL10nProfile(eventFlowXml);

        // Create a request
        RequestImpl r = RequestFactory.createRequest(p_requestType, profile,
                p_gxml, XmlUtil.object2String(eventFlowXml), p_exception);
        updateRequest(r, eventFlowXml);

        String originalSourceFileContent = (String) p_cxeMessage
                .getParameters().get(BaseAdapter.PARAM_ORIGINAL_FILE_CONTENT);
        r.setOriginalSourceFileContent(originalSourceFileContent);

        // if the profile is set to batch requests add the batch
        // information
        String importInitiatorId = null;
        if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.BATCH_CONDITION)
        {

            importInitiatorId = source.getImportInitiatorId();
            com.globalsight.cxe.util.fileImport.eventFlow.BatchInfo info = eventFlowXml
                    .getBatchInfo();

            if (info.getBatchId() != null)
            {
                String jobPrefixName = info.getJobName();
                // truncate the jobPrefixName to the max byte size in the DB
                jobPrefixName = EditUtil.truncateUTF8Len(jobPrefixName,
                        MAX_JOBNAME_SIZE);

                BatchInfo bi = new BatchInfo(info.getBatchId(),
                        info.getPageCount(), info.getPageNumber(),
                        info.getDocPageCount(), info.getDocPageNumber(),
                        jobPrefixName);
                r.setBatchInfo(bi);
            }

            RequestPersistenceAccessor.insertRequest(r);
        }
        else if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.WORD_COUNT_OR_TIMER_CONDITION)
        {
            RequestPersistenceAccessor.insertWordCountRequest(r);
        }
        else if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.WORD_COUNT_CONDITION)
        {
            RequestPersistenceAccessor.insertWordCountRequest(r);
        }

        if (importInitiatorId != null)
        {
            logger.info("Received an import request from "
                    + UserUtil.getUserNameById(importInitiatorId));
        }

        return r;
    }

    /**
     * Updates the target locale with the specified EventFlowXml.
     * 
     * @param p_eventFlowXml
     * @param r
     */
    static private void setUnimportedLocales(EventFlowXml p_eventFlowXml,
            RequestImpl r)
    {
        try
        {
            String targetLocales = p_eventFlowXml.getTarget().getLocale();
            if (!"unknown".equalsIgnoreCase(targetLocales))
            {
                GlobalSightLocale[] gs = r.getL10nProfile().getTargetLocales();
                String[] locales = targetLocales.split(",");
                if (gs.length != locales.length)
                {
                    for (int j = 0; j < gs.length; j++)
                    {
                        GlobalSightLocale gLocale = gs[j];
                        for (int i = 0; i < locales.length; i++)
                        {
                            String localeName = locales[i];
                            if (gLocale.toString().equalsIgnoreCase(localeName))
                            {
                                break;
                            }
                            if (i == locales.length - 1)
                            {
                                r.addUnimportTargetLocale(gLocale);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when set target locales in RequestImpl.", e);
        }
    }

    /**
     * Convenience method to quickly make an AdapterResult array containing a
     * single import error event
     * 
     * @param p_cxeMessage
     *            a cxe message
     * @return AdapterResult[] of one item
     */
    @SuppressWarnings("unchecked")
    static protected CxeMessage makeImportError(CxeMessage p_cxeMessage,
            GeneralException p_exception)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_IMPORT_ERROR_EVENT);
        CxeMessage errorMsg = new CxeMessage(type);
        errorMsg.setEventFlowXml(p_cxeMessage.getEventFlowXml());
        try
        {
            errorMsg.setMessageData(p_cxeMessage.getMessageData());
        }
        catch (IOException ioe)
        {
            logger.error(
                    "Could not create message data in import error event.", ioe);
        }
        errorMsg.setParameters(p_cxeMessage.getParameters());
        p_exception.setLogger(null);
        errorMsg.getParameters().put("Exception", p_exception);
        return errorMsg;
    }

    /**
     * Convenience method to create an AdapterResult array based on a newly
     * created CxeMessage object. The created array only has one AdapterResult
     * in it.
     * 
     * @param p_msgType
     *            the type of the CxeMessage
     * @param p_messageData
     *            MessageData
     * @param p_params
     *            HashMap of parameters
     * @param p_eventFlowXml
     *            String of EventFlowXml
     * @return AdapterResult[]
     */
    @SuppressWarnings("rawtypes")
    protected static AdapterResult[] makeSingleAdapterResult(
            CxeMessageType p_msgType, MessageData p_messageData,
            HashMap p_params, String p_eventFlowXml) throws IOException
    {
        CxeMessage outputMessage = new CxeMessage(p_msgType);
        outputMessage.setMessageData(p_messageData);
        outputMessage.setParameters(p_params);
        outputMessage.setEventFlowXml(p_eventFlowXml);
        return makeSingleAdapterResult(outputMessage);
    }

    /**
     * Convenience method to create an AdapterResult array based on a newly
     * created CxeMessage object. The created array only has one AdapterResult
     * in it.
     * 
     * @param p_cxeMessage
     *            the output cxe message
     * @return AdapterResult[]
     */
    protected static AdapterResult[] makeSingleAdapterResult(
            CxeMessage p_cxeMessage)
    {
        AdapterResult[] results = new AdapterResult[1];

        results[0] = new AdapterResult(p_cxeMessage);
        return results;
    }
}

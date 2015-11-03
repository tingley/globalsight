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
package com.globalsight.cxe.util.fileExport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.KeyUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ObjectUtil;
import com.globalsight.util.PropertiesFactory;

/**
 * The FileImportUtil class allows clients to make import requests without
 * having to use JMS.
 */
public class FileExportUtil
{
    static private final Logger logger = Logger.getLogger(FileExportUtil.class);

    public static Object LOCKER = new Object();

    // max size (in bytes) of the job name
    public static final int MAX_JOBNAME_SIZE = 320;

    public static boolean USE_JMS = false;
    public static HashMap<String, Integer> RUN_MAX_MESSAGE = new HashMap<String, Integer>();
    public static HashMap<String, Integer> ON_RUN_MESSAGE = new HashMap<String, Integer>();
    @SuppressWarnings("rawtypes")
    public static HashMap<String, List<Hashtable>> ON_HOLD_MESSAGE = new HashMap<String, List<Hashtable>>();

    @SuppressWarnings("rawtypes")
    public static HashMap<String, Hashtable> WAITING_REQUEST = new HashMap<String, Hashtable>();
    @SuppressWarnings("rawtypes")
    public static HashMap<String, Hashtable> RUNNING_REQUEST = new HashMap<String, Hashtable>();
    public static HashMap<String, List<BatchInfo>> CANCELED_REQUEST = new HashMap<String, List<BatchInfo>>();
    
    // initialize the USE_JMS and RUN_MAX_MESSAGE from
    // "properties/exportJob.properties"
    static
    {
        try
        {
            Properties p = (new PropertiesFactory())
                    .getProperties("/properties/exportJob.properties");
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
        @SuppressWarnings("rawtypes")
        Hashtable m = null;
        synchronized (LOCKER)
        {
            m = WAITING_REQUEST.get(key);
            if (m == null)
                return false;

            String name = getName(m);
            ON_HOLD_MESSAGE.get(name).remove(m);
            WAITING_REQUEST.remove(key);
        }

        if (m != null)
        {
            handleCancelEvent(m);
        }

        return true;
    }

    /**
     * This method uses the thread to create job, not JMS.
     * 
     * @param Hashtable
     */
    @SuppressWarnings("rawtypes")
    static public void exportFileWithThread(Hashtable ht)
    {
        FileExportRunnable runnable = new FileExportRunnable(ht);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    @SuppressWarnings("rawtypes")
    static public String getName(Hashtable ht)
    {
        String file = (String) ht.get("filePath");
        String name = getSuffix(file);
        
        return name;
    }
    
    static public HashMap<String, Hashtable> getCloneRunningRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(RUNNING_REQUEST);
        }
    }
    
    static public HashMap<String, List<Hashtable>> getCloneHoldingRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(ON_HOLD_MESSAGE);
        }
    }
    
    /**
     * Creates job without JMS.
     * 
     * @param Hashtable
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public void exportFile(Hashtable ht)
    {
        String key = (String) ht.get("uiKey");
        if (key == null)
        {
            key = KeyUtil.generateKey();
            ht.put("uiKey", key);
        }
        
        if (ht.get("requestTime") == null)
        {
            ht.put("requestTime", new Date());
            
         // the order is according to sort priority, sort time and sort axis.
            ht.put("sortTime", new Date().getTime());
            
            Integer n1 = (Integer) ht.get("priority");
            if (n1 == null)
                n1 = 3;
            ht.put("sortPriority", n1);
            ht.put("sortAxis", 1);
        }

        String name = getName(ht);

        // hold the message if the importing file is upper limit
        synchronized (LOCKER)
        {
            Integer max = RUN_MAX_MESSAGE.get(name);
            if (max == null)
                max = 10;

            Integer run = ON_RUN_MESSAGE.get(name);
            if (run == null)
                run = 0;

            if (run >= max)
            {
                List<Hashtable> ms = ON_HOLD_MESSAGE.get(name);
                if (ms == null)
                {
                    ms = new ArrayList<Hashtable>();
                    ON_HOLD_MESSAGE.put(name, ms);
                }

                ms.add(ht);
                sortMessages(ms);
                WAITING_REQUEST.put(key, ht);
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
                ht.put("startTime", new Date());
                RUNNING_REQUEST.put(key, ht);
            }

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) ht.get(CompanyWrapper.CURRENT_COMPANY_ID));

            ExportHelper helper = new ExportHelper();
            helper.export(ht);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            HibernateUtil.closeSession();
            
            RUNNING_REQUEST.remove(key);

            // Export a file that has been hold.
            Hashtable ht2 = null;
            synchronized (LOCKER)
            {
                Integer run = ON_RUN_MESSAGE.get(name);
                if (run != null)
                {
                    ON_RUN_MESSAGE.put(name, run - 1);

                    List<Hashtable> ms = ON_HOLD_MESSAGE.get(name);
                    if (ms != null && ms.size() > 0)
                    {
                        ht2 = ms.get(0);
                        ms.remove(0);
                    }
                }
            }

            if (ht2 != null)
            {
                exportFile(ht2);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static void sortWaitingMessage()
    {
        synchronized (LOCKER)
        {
            for (List<Hashtable> ms : ON_HOLD_MESSAGE.values())
            {
                sortMessages(ms);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static void sortMessages(List<Hashtable> ms) {
        Collections.sort(ms, new Comparator<Hashtable>()
        {
            @Override
            public int compare(Hashtable o1, Hashtable o2)
            {
                Integer n1 = (Integer) o1.get("sortPriority");
                if (n1 == null)
                    n1 = 3;

                Integer n2 = (Integer) o2.get("sortPriority");
                if (n2 == null)
                    n2 = 3;

                int k = n1 - n2;
                if (k != 0)
                    return k;

                long d1 = (Long) o1.get("sortTime");
                long d2 = (Long) o2.get("sortTime");

                k = (int) (d1 - d2);

                if (k != 0)
                    return k;

                int axis1 = (Integer) o1.get("sortAxis");
                int axis2 = (Integer) o2.get("sortAxis");
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
     * handles the cancel request.
     * 
     * @param p_cxeMessage
     */
    @SuppressWarnings("rawtypes")
    private static void handleCancelEvent(Hashtable ht)
    {
        ExportParameters exportParameters = (ExportParameters) ht.get(new Integer(1));
        long workFlowId = exportParameters.getWorkflowId();
        WorkflowExportingHelper.setAsNotExporting(workFlowId);
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

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
package com.globalsight.cxe.util.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.everest.request.RequestHandlerWLRemote;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * Class {@code RequestHandlerActivatorUtil} is used for continuing the job
 * creation starting with request creation without using JMS.
 * 
 * @since GBS-4400
 */
public class RequestHandlerActivatorUtil
{
    static private final Logger logger = Logger.getLogger(RequestHandlerActivatorUtil.class);

    /**
     * Handles job request during job creation asynchronously with thread
     * instead of JMS.
     */
    static public void handleRequestWithThread(Object data)
    {
        RequestHandlerActivatorRunnable runnable = new RequestHandlerActivatorRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Handles job request during job creation synchronously.
     */
    @SuppressWarnings("unchecked")
    static public void handleRequest(Object p_data)
    {
        if (p_data instanceof List)
        {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) p_data;
            for (Map<String, Object> data : dataList)
            {
                handleRequest(data);
            }
        }
        else
        {
            handleRequest((Map<String, Object>) p_data);
        }
    }

    private static void handleRequest(Map<String, Object> p_data)
    {
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID,
                    CompanyThreadLocal.getInstance().getValue());
            activityArgs.put(CxeToCapRequest.REQUEST_TYPE,
                    p_data.get(CxeToCapRequest.REQUEST_TYPE));
            activityArgs.put(CxeToCapRequest.CONTENT, p_data.get(CxeToCapRequest.CONTENT));
            activityStart = ActivityLog.start(RequestHandlerActivatorUtil.class, "handleRequest",
                    activityArgs);
            String contentFileName = (String) p_data.get(CxeToCapRequest.CONTENT);
            int requestType = ((Integer) p_data.get(CxeToCapRequest.REQUEST_TYPE)).intValue();
            String eventFlowXml = (String) p_data.get(CxeToCapRequest.EVENT_FLOW_XML);
            GeneralException exception = (GeneralException) p_data.get(CxeToCapRequest.EXCEPTION);
            String l10nRequestXml = (String) p_data.get(CxeToCapRequest.L10N_REQUEST_XML);

            RequestHandlerWLRemote requestHandler = ServerProxy.getRequestHandler();
            if (requestHandler != null)
            {
                requestHandler.prepareAndSubmitRequest(contentFileName, requestType, eventFlowXml,
                        exception, l10nRequestXml);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to create and persist the job request.", e);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            HibernateUtil.closeSession();
        }
    }
}

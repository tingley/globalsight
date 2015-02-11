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
package com.globalsight.everest.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * The RequestHandlerActivatorMDB is a JMS Message Driven Bean that activates
 * RequestHandler
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_IMPORTING_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class RequestHandlerActivatorMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = -246609669279528856L;

    // for logging purposes
    private static Logger s_logger = Logger
            .getLogger(RequestHandlerActivatorMDB.class);

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    public RequestHandlerActivatorMDB()
    {
        super(s_logger);

    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    private void onMessage(HashMap hm)
    {
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID,
                    hm.get(CompanyWrapper.CURRENT_COMPANY_ID));
            // This is a Request.* constant
            activityArgs.put(CxeToCapRequest.REQUEST_TYPE,
                    hm.get(CxeToCapRequest.REQUEST_TYPE));
            activityArgs.put(CxeToCapRequest.CONTENT,
                    hm.get(CxeToCapRequest.CONTENT));
            activityStart = ActivityLog.start(RequestHandlerActivatorMDB.class,
                    "onMessage", activityArgs);

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) hm.get(CompanyWrapper.CURRENT_COMPANY_ID));

            String contentFileName = (String) hm.get(CxeToCapRequest.CONTENT);
            int requestType = ((Integer) hm.get(CxeToCapRequest.REQUEST_TYPE))
                    .intValue();
            String eventFlowXml = (String) hm
                    .get(CxeToCapRequest.EVENT_FLOW_XML);
            GeneralException exception = (GeneralException) hm
                    .get(CxeToCapRequest.EXCEPTION);
            String l10nRequestXml = (String) hm
                    .get(CxeToCapRequest.L10N_REQUEST_XML);
            // If jobs have not finished importing when GS server is shut down,
            // and when restart GS server, GS will try to re-import the job.
            // At that time "requestHandler" possibly has not been loaded by
            // Jboss server,
            // this will return null.
            RequestHandlerWLRemote requestHandler = ServerProxy
                    .getRequestHandler();
            if (requestHandler != null)
            {
                requestHandler.prepareAndSubmitRequest(hm, contentFileName,
                        requestType, eventFlowXml, exception, l10nRequestXml);
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to create and persist the request - left in JMS message queue.",
                    e);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }

    /**
     * This is the JMS service activator for the RequestHandler.
     * 
     * @param p_cxeRequest
     *            The JMS message containing the request for localization.
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_cxeRequest)
    {
        HibernateUtil.closeSession();
        ObjectMessage msg = (ObjectMessage) p_cxeRequest;
        Serializable ob = null;
        try
        {
            ob = msg.getObject();

            if (ob instanceof ArrayList)
            {
                ArrayList<HashMap> msgs = (ArrayList<HashMap>) ob;
                for (HashMap m : msgs)
                {
                    onMessage(m);
                    HibernateUtil.closeSession();
                }
            }
            else
            {
                onMessage((HashMap) ob);
            }
        }
        catch (JMSException e1)
        {
            s_logger.error(e1);
            return;
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}

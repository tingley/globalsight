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
package com.globalsight.everest.page.pageexport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * The ExportMDB is a JMS Message Driven Bean that uses the ExportHelper (which
 * used to be ExportMessageListener) to asynchronously do exports.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_EXPORTING_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class ExportMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = -3427740442035115271L;

    // for logging purposes
    private static Logger s_logger = Logger.getLogger(ExportMDB.class);

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    public ExportMDB()
    {
        super(s_logger);
    }

    // ////////////////////////////////////
    // public Methods //
    // ////////////////////////////////////

    /**
     * This is the JMS onMessage wrapper to call ExportHelper. This performs an
     * export asynchronously.
     * 
     * @param p_cxeRequest
     *            The JMS message containing the info to export
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_cxeRequest)
    {
        ActivityLog.Start activityStart = null;
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("received message: " + p_cxeRequest);                
            }
            ObjectMessage msg = (ObjectMessage) p_cxeRequest;
            Serializable ob = msg.getObject();
            if (ob instanceof Hashtable)
            {
                Hashtable ht = (Hashtable) msg.getObject();

                Map<Object, Object> activityArgs = new HashMap<Object, Object>();
                activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID,
                        ht.get(CompanyWrapper.CURRENT_COMPANY_ID));
                activityArgs.put("pageId", ht.get(PageManager.PAGE_ID));
                activityStart = ActivityLog.start(ExportMDB.class, "onMessage",
                        activityArgs);

                CompanyThreadLocal.getInstance().setIdValue(
                        (String) ht.get(CompanyWrapper.CURRENT_COMPANY_ID));

                ExportHelper helper = new ExportHelper();
                helper.export(ht);
            }
            else if (ob instanceof ArrayList)
            {

                ArrayList<Hashtable> hts = (ArrayList<Hashtable>) ob;
                Hashtable ht = hts.get(0);
                CompanyThreadLocal.getInstance().setIdValue(
                        (String) ht.get(CompanyWrapper.CURRENT_COMPANY_ID));
                ExportHelper helper = new ExportHelper();
                helper.export(hts);
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
            HibernateUtil.closeSession();
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }
}

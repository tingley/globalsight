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
package com.globalsight.everest.aligner;

import java.util.HashMap;

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
import com.globalsight.everest.page.pageimport.ExtractedFileImporter;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * The AlignerMDB is a JMS Message Driven Bean that is used to send asynchronous
 * messages to back-end Aligner services. This class is also used to receive the
 * final GXML for aligner import requests.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_ALIGNER_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class AlignerMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;

    // for logging purposes
    static private Logger s_logger = Logger.getLogger(AlignerMDB.class);

    //
    // Constructor
    //

    public AlignerMDB()
    {
        super(s_logger);
    }

    //
    // Public Methods
    //

    /**
     * This is the JMS service activator for whatever the Aligner needs to do.
     * 
     * @param p_cxeRequest
     *            The JMS message containing the request for alignment or
     *            alignment services
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_cxeRequest)
    {
        try
        {
            ObjectMessage msg = (ObjectMessage) p_cxeRequest;
            HashMap<String, ?> hm = (HashMap<String, ?>) msg.getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) hm.get(CompanyWrapper.CURRENT_COMPANY_ID));

            String contentFileName = (String) hm.get(CxeToCapRequest.CONTENT);
            String eventFlowXml = (String) hm
                    .get(CxeToCapRequest.EVENT_FLOW_XML);
            GeneralException exception = (GeneralException) hm
                    .get(CxeToCapRequest.EXCEPTION);

            String alignerExtractorName = (String) hm.get("AlignerExtractor");
            String gxml = null;

            if (exception == null)
            {
                s_logger.info("Aligner received GXML in file: "
                        + contentFileName);
            }
            else
            {
                s_logger.info("Aligner received an aligner-import failure."
                        + exception.getLocalizedMessage());
            }

            if (contentFileName != null)
            {
                gxml = ExtractedFileImporter.readXmlFromFile(contentFileName);
            }

            AlignerExtractorResult aeResult = new AlignerExtractorResult(gxml,
                    eventFlowXml, exception);
            AlignerExtractor ae = AlignerExtractor
                    .getAlignerExtractor(alignerExtractorName);

            ae.addToResults(aeResult, (String) hm.get("PageCount"),
                    (String) hm.get("PageNumber"),
                    (String) hm.get("DocPageCount"),
                    (String) hm.get("DocPageNumber"));
        }
        catch (Throwable ex)
        {
            s_logger.error("Failed to handle alignment request.", ex);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}

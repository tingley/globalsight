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

import java.util.Hashtable;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * The ExportMDB is a JMS Message Driven Bean
 * that uses the ExportHelper (which used to be ExportMessageListener)
 * to asynchronously do exports.
 */
public class ExportMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = -3427740442035115271L;

    // for logging purposes
    private static GlobalSightCategory s_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger("EXPORT");

  //////////////////////////////////////
  // Constructor                      //
  //////////////////////////////////////
  public ExportMDB()
  {
      super(s_logger);
  }

  //////////////////////////////////////
  // public Methods                   //
  //////////////////////////////////////

    /**
     * This is the JMS onMessage wrapper to call ExportHelper. This performs
     * an export asynchronously.
     *
     * @param p_cxeRequest The JMS message containing the info to export
     */
    public void onMessage(Message p_cxeRequest)
    {
        try
        {
            s_logger.debug("received message: " + p_cxeRequest);
            ObjectMessage msg = (ObjectMessage)p_cxeRequest;
            Hashtable ht = (Hashtable) msg.getObject();

            CompanyThreadLocal.getInstance().setIdValue((String) ht.get(CompanyWrapper.CURRENT_COMPANY_ID));
            
            ExportHelper helper = new ExportHelper();
            helper.export(ht);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to create and persist the request - left in JMS message queue.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}


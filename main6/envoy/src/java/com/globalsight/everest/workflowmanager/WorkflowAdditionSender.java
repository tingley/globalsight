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

package com.globalsight.everest.workflowmanager;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.naming.NamingException;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.JmsHelper;
public class WorkflowAdditionSender
    implements Serializable
{
    
	private static final long serialVersionUID = -548644800233033712L;
	//////////////////////////////////////
    // Constants                        //
    //////////////////////////////////////
    private List m_wfTIds = null; 
    private long m_jobId = 0;


    //////////////////////////////////////
    // Constructors                     //
    //////////////////////////////////////

    public WorkflowAdditionSender(List p_wfTIds,
                                  long p_jobId)throws JMSException
    {
        m_wfTIds = p_wfTIds;
        m_jobId = p_jobId;
    }

    public void sendToAddWorkflows()
    throws JMSException,NamingException
    {
        Hashtable map = new Hashtable(3); 
        map.put(CompanyWrapper.CURRENT_COMPANY_ID, 
        		CompanyThreadLocal.getInstance().getValue());
        map.put("JOB", new Long(m_jobId));
        Iterator it = m_wfTIds.iterator();
        int i = 1;
        while(it.hasNext())
        {
            String wfId = "WFTID" + i;
            Long wfTId = (Long)it.next();
            map.put(wfId,wfTId);
            i++;
        }

        JmsHelper.sendMessageToQueue(map,JmsHelper.JMS_WORKFLOW_ADDITION_QUEUE);
    }
}

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
package com.globalsight.cxe.adaptermdb;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;

/**
 * BaseAdapterMDB is a base class for all adapter message driven beans to
 * extend.
 */
public abstract class BaseAdapterMDB
{
    private Logger m_logger;
    private BaseAdapter m_adapter;

    /**
     * Handles the AdapterResults. It is used if no JMS.
     * 
     * @param cxeMessage
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<CxeMessage> handlerAdapterResults(CxeMessage cxeMessage) throws Exception
    {
        setAdapter(loadAdapter());
        List<CxeMessage> cms = new ArrayList<CxeMessage>();
        String companyId = CompanyThreadLocal.getInstance().getValue();
        m_adapter.loadConfiguration();
        m_adapter.loadProcessors();

        CxeMessage preProcessedMsg = m_adapter.runPreProcessor(cxeMessage);
        preProcessedMsg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

        AdapterResult results[] = m_adapter.handleMessage(preProcessedMsg);

        for (int i = 0; results != null && i < results.length; i++)
        {
            CxeMessage newCxeMessage = results[i].cxeMessage;
            if (newCxeMessage != null)
            {
                newCxeMessage.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                CxeMessage postProcessedMsg = m_adapter.runPostProcessor(newCxeMessage);
                postProcessedMsg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                cms.add(postProcessedMsg);
            }

            List<CxeMessage> msgs = results[i].getMsgs();
            for (CxeMessage msg : msgs)
            {
                msg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                CxeMessage postProcessedMsg = m_adapter.runPostProcessor(msg);
                postProcessedMsg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                cms.add(postProcessedMsg);
            }
        }

        return cms;
    }

    /**
     * Gets the logger this AdapterMDB should use
     * 
     * @return Logger
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Sets the logger this AdapterMDB should use
     * 
     * @param p_categoryName
     *            a category name
     */
    protected void setLogger(String p_categoryName)
    {
        m_logger = Logger.getLogger(p_categoryName);
    }

    /**
     * Sets the adapter to use to actually handle messages
     * 
     * @param p_adapter
     *            desired adapter
     */
    protected void setAdapter(BaseAdapter p_adapter)
    {
        m_adapter = p_adapter;
    }

    /**
     * Gets the adapter this proxy uses
     * 
     * @return adapter
     */
    protected BaseAdapter getAdapter()
    {
        return m_adapter;
    }

    /**
     * Returns a String containing the adapter name. Like
     * "FileSystemSourceAdapter";
     * 
     * @return String
     */
    abstract protected String getAdapterName();

    /**
     * Creates and loads the appropriate BaseAdapter class
     * 
     * @return BaseAdapter
     */
    abstract protected BaseAdapter loadAdapter() throws Exception;
}

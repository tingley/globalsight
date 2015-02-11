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
package com.globalsight.cxe.adaptermdb.windowspe;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.MessageListener;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.windowspe.WindowsPEAdapter;
import com.globalsight.cxe.adaptermdb.BaseAdapterMDB;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.util.jms.JmsHelper;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + EventTopicMap.JMS_PREFIX
                + EventTopicMap.FOR_WINPE_TARGET_ADAPTER),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class WindowsPETargetAdapterMDB extends BaseAdapterMDB
{
    private static final long serialVersionUID = 848062561358350232L;
    private static String ADAPTER_NAME = WindowsPETargetAdapterMDB.class
            .getName();

    protected String getAdapterName()
    {
        return ADAPTER_NAME;
    }

    protected BaseAdapter loadAdapter() throws Exception
    {
        return new WindowsPEAdapter(ADAPTER_NAME);
    }
}

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

package com.globalsight.webservices;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;

import com.globalsight.webservices.coti.util.COTIUtil;

/**
 * Login service for COTI web services
 * @author Wayzou
 *
 */
public class COTILogin
{
    private static String session_id = "session_id";

    public void init(ServiceContext serviceContext)
    {
        String sid = (String) serviceContext.getProperty(session_id);
        if (sid != null)
        {

        }
    }

    public void destroy(ServiceContext serviceContext)
    {

    }

    /**
     * SessionService Login
     * 
     * @return
     */
    public String Login()
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();

        String cotiSessionId = COTIUtil.loginSession(msgCtx);
        return cotiSessionId;
    }
}

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

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIpManager;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;

/**
 * IP Address Filter Handler. This blocks the web service from being invoked
 * from unknown IP Addresses.
 */
public class IpAddressFilterHandler extends BasicHandler
{
    private static final long serialVersionUID = 2909599370937608480L;
    private static final Logger s_logger = Logger.getLogger(IpAddressFilterHandler.class);

    // Allow certain APIs to ignore IP check.
    private static Set<String> notCareIpFilterMethods = new HashSet<String>();
    static
    {
        // Create job uses below methods("Ambassador2.java"), IP filter should
        // ignore them.
        notCareIpFilterMethods.add("dummyLogin");
        notCareIpFilterMethods.add("uploadFiles");
        // GBS-3389: recreate job uses "createJobOnInitial" API, IP filter
        // should ignore it.
        notCareIpFilterMethods.add("createJobOnInitial");
    }

    /**
     * Checks the IP address of the incoming request from the MessageContext,
     * and if that IP address is not in the list of allowed IP addresses, then
     * it is disallowed.
     */
    public void invoke(MessageContext p_msgContext) throws AxisFault
    {
        try
        {
            HttpServletRequest request = (HttpServletRequest) p_msgContext
                    .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

            boolean enableIPFiler = false;
            String operationName = p_msgContext.getOperation().getName();
            if (!notCareIpFilterMethods.contains(operationName))
            {
                try
                {
                    String loginUser = p_msgContext.getUsername();
                    String userID = UserUtil.getUserIdByName(loginUser);
                    User user = ServerProxy.getUserManager().getUser(userID);
                    String companyName = user.getCompanyName();
                    Company company = ServerProxy.getJobHandler().getCompany(
                            companyName);
                    enableIPFiler = company.getEnableIPFilter();
                }
                catch (Exception e)
                {

                }
            }

            if (enableIPFiler)
            {
                InetAddress remoteIP = InetAddress.getByName(request
                        .getRemoteAddr());
                if (!RemoteIpManager.allowed(remoteIP.getHostAddress()))
                {
                    String msg = "Illegal web service access attempt from IP address "
                            + remoteIP.getHostAddress();
                    s_logger.info(msg);
                    throw new AxisFault(msg);
                }
            }
        }
        catch (AxisFault af)
        {
            throw af;
        }
        catch (Throwable t)
        {
            throw new AxisFault("Blocking web service request: " + t.getMessage());
        }
    }
}

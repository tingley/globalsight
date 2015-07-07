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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.loginBlockConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.systemActivity.LoginAttemptConfig;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.login.LoginAttemptController;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class LoginBlockConfigHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(LoginBlockConfigHandler.class);
    
    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.systemActivity.LoginAttemptConfig")
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving LoginAttemptConfig...");
        String[] exemptIps = request.getParameterValues("exemptIps");
        List<String> ips = new ArrayList<String>();
        if (exemptIps != null)
        {
            for (String ip : exemptIps)
            {
                ips.add(ip);
            }
        }
        
        LoginAttemptConfig config = (LoginAttemptConfig) form;
        LoginAttemptConfig old = LoginAttemptController.getConfigFromDb();
        old.setBlockTime(config.getBlockTime());
        old.setEnable(config.isEnable());
        old.setMaxTime(config.getMaxTime());
        old.setExemptIpsAsList(ips);

        HibernateUtil.saveOrUpdate(old);

        logger.debug("Saving LoginAttemptConfig finished.");
    }

    
    
    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        request.setAttribute("config", LoginAttemptController.getConfigFromDb());
        request.setAttribute("blockedIps", LoginAttemptController.getBlockedIpList());
        request.setAttribute("exemptIps", LoginAttemptController.getExemptIpList());
    }

}

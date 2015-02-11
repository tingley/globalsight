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
package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.TDATM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tda.TdaHelper;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class ModifyTDAOptionsHandler extends PageHandler implements
        TMProfileConstants
{
    private static Logger s_logger = Logger
            .getLogger(ModifyTDAOptionsHandler.class);

    public ModifyTDAOptionsHandler()
    {
        super();
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
		if (p_request.getMethod().equalsIgnoreCase(
				WebAppConstants.REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
            return;
        }

        HttpSession sess = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);

        TranslationMemoryProfile tmProfile = null;
        String errorInfo = "";
        String tmProfileId = p_request.getParameter(TMProfileConstants.TM_PROFILE_ID);
        if (tmProfileId != null)
        {
            tmProfile = (TranslationMemoryProfile) TMProfileHandlerHelper
                    .getTMProfileById(Long.parseLong(tmProfileId));
        }

        if (p_request.getParameter("action") != null
                && p_request.getParameter("action").equals("modify"))
        {

            String isCreateOrModify = "create";

            TDATM tdatm = new TDATM();

            if (tmProfile.getTdatm() != null)
            {
                tdatm = tmProfile.getTdatm();
                isCreateOrModify = "modify";
            }

            if (p_request.getParameter("enableTda") != null)
            {
                tdatm.setEnable(1);

                if (p_request.getParameter("hostName") != null)
                {
                    tdatm
                            .setHostName(p_request.getParameter("hostName")
                                    .trim());
                }

                if (p_request.getParameter("userName") != null)
                {
                    tdatm.setUserName(p_request.getParameter("userName"));
                }

                if (p_request.getParameter("password") != null)
                {
                    tdatm.setPassword(p_request.getParameter("password"));
                }

                if (p_request.getParameter("description") != null)
                {
                    tdatm.setDescription(p_request.getParameter("description"));
                }

                try
                {
                    TdaHelper tdaHelper = new TdaHelper();
                    errorInfo = tdaHelper.loginCheck(p_request
                            .getParameter("hostName"), p_request
                            .getParameter("userName"), p_request
                            .getParameter("password"));
                }
                catch (Exception e)
                {
                    s_logger.error("TDA login error:" + e.getMessage());
                    errorInfo = "Can not connect TDA server";
                }
            }
            else
            {
                tdatm.setEnable(0);
                errorInfo = "ture";
            }

            tmProfile.setTdatm(tdatm);
            tdatm.setTranslationMemoryProfile(tmProfile);

            try
            {
                if (errorInfo != null && errorInfo.equals("ture"))
                {
                    if (isCreateOrModify.equals("create"))
                    {
                        HibernateUtil.save(tdatm);
                    }
                    else
                    {
                        HibernateUtil.saveOrUpdate(tdatm);
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error("TDA save failed:" + e.getMessage());
                errorInfo = "TDA save failed.";
            }
        }

        sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE, tmProfile);
        p_request.setAttribute("checkInfo", errorInfo);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}

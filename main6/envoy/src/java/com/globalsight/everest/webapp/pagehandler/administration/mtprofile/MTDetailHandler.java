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
package com.globalsight.everest.webapp.pagehandler.administration.mtprofile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.projecthandler.MachineTranslateAdapter;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * mtProfileHandler is the page handler responsible for displaying a list of tm
 * profiles and perform actions supported by the UI (JSP).
 */

public class MTDetailHandler extends PageHandler
{

    // non user related state
    private static final Logger CATEGORY = Logger
            .getLogger(MTDetailHandler.class);

    public MTDetailHandler()
    {
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
        MachineTranslateAdapter mta = new MachineTranslateAdapter();
        HttpSession sess = p_request.getSession(false);
        ResourceBundle bundle = PageHandler.getBundle(sess);
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);
        // when edit submit
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) sessionMgr
                .getAttribute(MTProfileConstants.MT_PROFILE);
        p_response.setContentType("text/json; charset=utf-8");
        p_response.setCharacterEncoding("utf-8");
        PrintWriter writer = p_response.getWriter();
        String mtId = p_request.getParameter(MTProfileConstants.MT_PROFILE_ID);

        if (StringUtils.isNotEmpty(mtId))
        {
            // edit
            mtProfile = MTProfileHandlerHelper.getMTProfileById(mtId);
            p_request.setAttribute("title",
                    bundle.getString("lb_mt_options_edit"));
        }
        else if (mtProfile == null)
        {
            // new
            mtProfile = new MachineTranslationProfile();
            mtProfile.setActive(true);
            p_request.setAttribute("title",
                    bundle.getString("lb_mt_options_new"));

        }

        String action = (String) p_request
                .getParameter(MTProfileConstants.ACTION);
        if (StringUtils.isNotBlank(action))
        {
            String engine = p_request
                    .getParameter(MTProfileConstants.MT_ENGINE);

            if (engine != null)
            {
                mta.setMTCommonOptions(p_request, mtProfile, engine);
            }
            if (MTProfileConstants.CANCEL_ACTION.equals(action))
            {
                clearSessionExceptTableInfo(sess, MTProfileConstants.MTP_KEY);
            }
            else if (MTProfileConstants.CANCEL_MT_OPTIONS_ACTION.equals(action))
            {
                clearSessionExceptTableInfo(sess, MTProfileConstants.MTP_KEY);
            }
            else if (MTProfileConstants.SAVE_MT_OPTIONS_ACTION.equals(action))
            {
                // for the ui has a force save flag
                MTProfileHandlerHelper.savemtProfile(mtProfile);
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=mtProfiles");
                return;
            }
            else if (MTProfileConstants.TEST_MT_OPTIONS_ACTION.equals(action))
            {
                try
                {
                    if (MTProfileHandlerHelper.isMtProfileExisted(mtProfile)
                            || StringUtils
                                    .isEmpty(mtProfile.getMtProfileName()))
                    {
                        JSONObject jso = new JSONObject();
                        jso.put("ExceptionInfo",
                                "Well:The Name has been in used!");
                        writer.write(jso.toString());
                    }
                    // if promt and ao will test return false in case not save
                    // just for session
                    else if (mta.testMTCommonOptions(mtProfile, writer))
                    {
                        MTProfileHandlerHelper.savemtProfile(mtProfile);
                        JSONObject jso = new JSONObject();
                        jso.put("Info", "saved");
                        writer.write(jso.toString());
                        writer.close();
                    }
                    sessionMgr.setAttribute(MTProfileConstants.MT_PROFILE,
                            mtProfile);
                    writer.close();

                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }
                return;
            }
        }
        sessionMgr.setAttribute(MTProfileConstants.MT_PROFILE, mtProfile);
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

}

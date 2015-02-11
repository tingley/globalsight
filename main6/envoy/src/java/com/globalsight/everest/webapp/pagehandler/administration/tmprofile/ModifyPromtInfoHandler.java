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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.promt.ProMtInvoker;
import com.globalsight.machineTranslation.promt.ProMtPts9Invoker;

public class ModifyPromtInfoHandler extends PageHandler implements
        TMProfileConstants
{
    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(ModifyMTOptionsHandler.class);

    public ModifyPromtInfoHandler()
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

        HttpSession sess = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);

        TranslationMemoryProfile tmProfile = null;
        String id = (String) p_request.getParameter(RADIO_BUTTON);
        if (id == null
                || p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
            return;
        }
        if (id != null)
        {
            long tmProfileId = -1;
            try
            {
                tmProfileId = Long.parseLong(id);
            }
            catch (NumberFormatException nfe)
            {
            }

            tmProfile = TMProfileHandlerHelper.getTMProfileById(tmProfileId);
        }

        // TM engine
        String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
        tmProfile.setMtEngine(engine);
        // TM override non-exact matches under threshold
        String overrideNonExactMatches = p_request
                .getParameter(TMProfileConstants.MT_OVERRIDE_MATCHES);
        if (overrideNonExactMatches == null
                || !"on".equals(overrideNonExactMatches))
        {
            tmProfile.setOverrideNonExactMatches(false);
        }
        else
        {
            tmProfile.setOverrideNonExactMatches(true);
        }
        // auto commit to TM
        String autoCommitToTM = p_request
                .getParameter(TMProfileConstants.MT_AUTOCOMMIT_TO_TM);
        if (autoCommitToTM == null || !"on".equals(autoCommitToTM))
        {
            tmProfile.setAutoCommitToTM(false);
        }
        else
        {
            tmProfile.setAutoCommitToTM(true);
        }

        // isMtSensitiveLeveraging
        String isMtSensitiveLeveraging = p_request.getParameter("mtLeveraging");
        if (isMtSensitiveLeveraging == null
                || !"on".equals(isMtSensitiveLeveraging))
        {
            tmProfile.setIsMTSensitiveLeveraging(false);
        }
        else
        {
            tmProfile.setIsMTSensitiveLeveraging(true);
        }
        // MtSensitivePenalty
        String mtSensitivePenalty = p_request
                .getParameter("mtSensitivePenalty");
        long long_mtSensitivePenalty = 1;
        try
        {
            long_mtSensitivePenalty = Long.parseLong(mtSensitivePenalty);
        }
        catch (Exception ex)
        {
        }
        tmProfile.setMtSensitivePenalty(long_mtSensitivePenalty);

        // show in segment editor
        String showInEditor = p_request
                .getParameter(TMProfileConstants.MT_SHOW_IN_EDITOR);
        if (showInEditor == null || !"on".equals(showInEditor))
        {
            tmProfile.setShowInEditor(false);
        }
        else
        {
            tmProfile.setShowInEditor(true);
        }
        // PROMT URL
        String ptsUrl = p_request.getParameter(TMProfileConstants.MT_PTSURL);
        if (ptsUrl != null && !"".equals(ptsUrl.trim()))
        {
            tmProfile.setPtsurl(ptsUrl.trim());
        }
        // PROMT UserName
        String ptsUsername = p_request.getParameter(
                TMProfileConstants.MT_PTS_USERNAME).trim();
        tmProfile.setPtsUsername(ptsUsername);
        // PROMT password
        String ptsPassword = p_request
                .getParameter(TMProfileConstants.MT_PTS_PASSWORD);
        tmProfile.setPtsPassword(ptsPassword);

        sessionMgr.setAttribute("changedTmProfile", tmProfile);

        setPromtInfo(ptsUrl, ptsUsername, ptsPassword, p_request);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void setPromtInfo(String ptsUrl, String ptsUsername,
            String ptsPassword, HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // "0":pts8; "1":pts9
        String ptsVersionFlag = "";
        
        HashMap<String, String> dirsMap = null;
        List directionsList = new ArrayList();
        HashMap dirToTopicTemplateHM = new HashMap();
        String exMsg = null;
        
        try
        {
            // Try PTS8 APIs first.
            ProMtInvoker invoker = null;
            if (ptsUsername != null && !"".equals(ptsUsername))
            {
                invoker = new ProMtInvoker(ptsUrl, ptsUsername, ptsPassword);
            }
            else
            {
                invoker = new ProMtInvoker(ptsUrl);
            }
            
            dirsMap = invoker.getDirectionsHashMap();
            if (dirsMap != null && dirsMap.size() > 0)
            {
                Iterator iter = dirsMap.entrySet().iterator();
                while (iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String lpName = (String) entry.getKey();
                    String lpId = (String) entry.getValue();
                    List directionsTplList = invoker
                            .getTopicTemplateByDirId(lpId);
                    if (directionsTplList != null
                            && directionsTplList.size() > 0)
                    {
                        Collections.sort(directionsTplList);
                        dirToTopicTemplateHM.put(lpName, directionsTplList);
                        directionsList.add(lpName);
                    }
                    else
                    {
                        // if there is no template info for this direction,
                        // then remove it from directions map
                        dirsMap.remove(lpName);
                    }
                }
            }

            Collections.sort(directionsList);
            p_request.setAttribute(TMProfileConstants.MT_LocalPairs_List,
                    directionsList);
            p_request.setAttribute(TMProfileConstants.MT_LocalPairs_Map,
                    dirsMap);
            p_request.setAttribute(
                    TMProfileConstants.MT_DIRECTION_TOPICTEMPLATE_MAP,
                    dirToTopicTemplateHM);

            sessionMgr.setAttribute(TMProfileConstants.MT_LocalPairs_Map,
                    dirsMap);
            sessionMgr.setAttribute(
                    TMProfileConstants.MT_DIRECTION_TOPICTEMPLATE_MAP,
                    dirToTopicTemplateHM);
            
            ptsVersionFlag = TMProfileConstants.MT_PTS_URL_FLAG_V8;
            TranslationMemoryProfile tmp = (TranslationMemoryProfile) sessionMgr
                    .getAttribute("changedTmProfile");
            if (tmp != null)
            {
                tmp.setPtsUrlFlag(ptsVersionFlag);
            }
        }
        catch (Exception ex)
        {
            exMsg = ex.getMessage();
            if (exMsg.toLowerCase().indexOf("unknownhostexception") != -1)
            {
                int index = exMsg.toLowerCase().indexOf("unknownhostexception");
                exMsg = exMsg.substring(index + "unknownhostexception".length()
                        + 1, exMsg.length());
                exMsg = "Unkown Host:" + exMsg;
            }
            
            try
            {
                // Try PTS9 APIs.
                ProMtPts9Invoker invoker2 = null;
                if (ptsUsername != null && !"".equals(ptsUsername))
                {
                    invoker2 = new ProMtPts9Invoker(ptsUrl, ptsUsername, ptsPassword);
                }
                else
                {
                    invoker2 = new ProMtPts9Invoker(ptsUrl);
                }
                
                dirsMap = invoker2.getDirectionsHashMap();
                if (dirsMap != null && dirsMap.size() > 0)
                {
                    Iterator iter = dirsMap.entrySet().iterator();
                    while (iter.hasNext())
                    {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String lpName = (String) entry.getKey();
                        String lpId = (String) entry.getValue();
                        List directionsTplList = invoker2
                                .getTopicTemplateByDirId(lpId);
                        if (directionsTplList != null
                                && directionsTplList.size() > 0)
                        {
                            Collections.sort(directionsTplList);
                            dirToTopicTemplateHM.put(lpName, directionsTplList);
                            directionsList.add(lpName);
                        }
                        else
                        {
                            // if there is no template info for this direction,
                            // then remove it from directions map
                            dirsMap.remove(lpName);
                        }
                    }
                }

                Collections.sort(directionsList);
                p_request.setAttribute(TMProfileConstants.MT_LocalPairs_List,
                        directionsList);
                p_request.setAttribute(TMProfileConstants.MT_LocalPairs_Map,
                        dirsMap);
                p_request.setAttribute(
                        TMProfileConstants.MT_DIRECTION_TOPICTEMPLATE_MAP,
                        dirToTopicTemplateHM);

                sessionMgr.setAttribute(TMProfileConstants.MT_LocalPairs_Map,
                        dirsMap);
                sessionMgr.setAttribute(
                        TMProfileConstants.MT_DIRECTION_TOPICTEMPLATE_MAP,
                        dirToTopicTemplateHM);
                
                ptsVersionFlag = TMProfileConstants.MT_PTS_URL_FLAG_V9;
                TranslationMemoryProfile tmp = (TranslationMemoryProfile) sessionMgr
                        .getAttribute("changedTmProfile");
                if (tmp != null)
                {
                    tmp.setPtsUrlFlag(ptsVersionFlag);
                }
            }
            catch (Exception e)
            {
                exMsg = e.getMessage();
                p_request.setAttribute("ExceptionInfo", exMsg);
            }
        }
    }

}

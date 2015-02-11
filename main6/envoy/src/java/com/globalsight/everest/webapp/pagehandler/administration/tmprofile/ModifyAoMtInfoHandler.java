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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.globalsight.machineTranslation.asiaOnline.AsiaOnlineMtInvoker;

public class ModifyAoMtInfoHandler extends PageHandler implements
        TMProfileConstants
{
    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(ModifyAoMtInfoHandler.class);
    
    public ModifyAoMtInfoHandler()
    {
        super();
    }
    
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession sess = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) sess.getAttribute(SESSION_MANAGER);

        TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr
                    .getAttribute(TMProfileConstants.TM_PROFILE);
        setChangedTmProfile(p_request, tmProfile);

        String aoMtUrl = tmProfile.getAoMtUrl();
        long aoMtPort = tmProfile.getAoMtPort();
        String aoMtUserName = tmProfile.getAoMtUsername();
        String aoMtPassword = tmProfile.getAoMtPassword();
        long aoMtAcctNumber = tmProfile.getAoMtAccountNumber();
        
        try
        {
            AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(aoMtUrl,
                    (int) aoMtPort, aoMtUserName, aoMtPassword,
                    (int) aoMtAcctNumber);

            HashMap aoSupportedLocalePairs = aoInvoker
                    .getAllSupportedLanguagePairs();
            p_request.setAttribute("aoSupportedLocalePairs",
                    aoSupportedLocalePairs);
            sessionMgr.setAttribute("aoSupportedLocalePairs",
                    aoSupportedLocalePairs);

            HashMap aoDomainCombinationsMap = new HashMap();
            if (aoSupportedLocalePairs != null
                    && aoSupportedLocalePairs.size() > 0)
            {
                Iterator lpCodesIt = aoSupportedLocalePairs.keySet().iterator();
                while (lpCodesIt.hasNext())
                {
                    String strLPCode = (String) lpCodesIt.next();
                    List dcListForSpecifiedLPCode = null;

                    dcListForSpecifiedLPCode = aoInvoker
                            .getDomainCombinationByLPCode(Long
                                    .parseLong(strLPCode));
                    if (dcListForSpecifiedLPCode != null
                            && dcListForSpecifiedLPCode.size() > 0)
                    {
                        aoDomainCombinationsMap.put(strLPCode,
                                dcListForSpecifiedLPCode);
                    }
                }
            }

            p_request.setAttribute("domainCombinationMap",
                    aoDomainCombinationsMap);
            sessionMgr.setAttribute("domainCombinationMap",
                    aoDomainCombinationsMap);
        }
        catch (Exception ex)
        {
            String exceptionInfo = ex.getMessage();
            p_request.setAttribute("ExceptionInfo", exceptionInfo);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
    
    private void setChangedTmProfile(HttpServletRequest p_request,
            TranslationMemoryProfile tmProfile)
    {
        HttpSession sess = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);

        // mt engine
        String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
        tmProfile.setMtEngine(engine);
        // mt override non-exact matches under threshold
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
        // auto commit to tm
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
        // MS Translator options
        if (engine != null && engine.equalsIgnoreCase("ms_translator"))
        {
            String msMTUrl = p_request
                    .getParameter(TMProfileConstants.MT_MS_URL);
            if (msMTUrl != null && !"".equals(msMTUrl.trim()))
            {
                tmProfile.setMsMTUrl(msMTUrl.trim());
            }

            String appid = p_request
                    .getParameter(TMProfileConstants.MT_MS_APPID);
            tmProfile.setMsMTAppID(appid);

            tmProfile.setMsMTUrlFlag(p_request
                    .getParameter(TMProfileConstants.MT_MS_URL_FLAG));
        }
        // Asia Online options
        if (engine != null && engine.equalsIgnoreCase("asia_online"))
        {
            String aoMtUrl = p_request
                    .getParameter(TMProfileConstants.MT_AO_URL);
            if (aoMtUrl != null && !"".equals(aoMtUrl.trim()))
            {
                tmProfile.setAoMtUrl(aoMtUrl);
            }

            String aoMtPort = p_request
                    .getParameter(TMProfileConstants.MT_AO_PORT);
            if (aoMtPort != null && !"".equals(aoMtPort.trim()))
            {
                tmProfile.setAoMtPort(Long.parseLong(aoMtPort));
            }

            String aoMtUsername = p_request
                    .getParameter(TMProfileConstants.MT_AO_USERNAME);
            if (aoMtUsername != null && !"".equals(aoMtUsername.trim()))
            {
                tmProfile.setAoMtUsername(aoMtUsername);
            }

            String aoMtPassword = p_request
                    .getParameter(TMProfileConstants.MT_AO_PASSWORD);
            if (aoMtPassword != null && !"".equals(aoMtPassword.trim()))
            {
                tmProfile.setAoMtPassword(aoMtPassword);
            }

            String aoMtAccountNumber = p_request
                    .getParameter(TMProfileConstants.MT_AO_ACCOUNT_NUMBER);
            if (aoMtAccountNumber != null
                    && !"".equals(aoMtAccountNumber.trim()))
            {
                tmProfile.setAoMtAccountNumber(Long
                        .parseLong(aoMtAccountNumber));
            }
        }

        sessionMgr.setAttribute("changedTmProfile", tmProfile);
    }
}

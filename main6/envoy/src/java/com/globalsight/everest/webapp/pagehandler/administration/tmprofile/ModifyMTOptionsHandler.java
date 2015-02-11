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
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import org.tempuri.LanguageService;
import org.tempuri.SoapService;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.asiaOnline.AsiaOnlineMtInvoker;
import com.globalsight.machineTranslation.mstranslator.MSTranslatorInvoker;
import com.microsoft.schemas.MSNSearch._2005._09.fex.LanguagePair;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationRequest;

public class ModifyMTOptionsHandler extends PageHandler 
    implements TMProfileConstants
{
    private static Logger s_logger =
        Logger.getLogger(ModifyMTOptionsHandler.class);

    public ModifyMTOptionsHandler()
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
        if (p_request.getMethod().equalsIgnoreCase(
                WebAppConstants.REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
            return;
        }
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = 
            (SessionManager) session.getAttribute(SESSION_MANAGER);
        
        // Store TM profile in session.
        TranslationMemoryProfile tmProfile = null;
        String id = (String) p_request.getParameter(WebAppConstants.RADIO_BUTTON);
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
            sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE, tmProfile);
            sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE_ID, tmProfileId);
        }
        
        // Operation
    	String operation = p_request.getParameter("operation");
    	p_request.setAttribute("operation", operation);

    	// Return from "modifyPromtInfo.jsp" (there is error)
    	if (operation != null && "previous".equals(operation))
    	{
            String exceptionInfo = p_request.getParameter("ExceptionInfo");
            p_request.setAttribute("ExceptionInfo", exceptionInfo);
    	}
    	// Test MS translator host
    	else if (operation != null && "testMSHost".equals(operation))
    	{
        	setChangedTmProfile(p_request, tmProfile);
        	String appid = p_request.getParameter(TMProfileConstants.MT_MS_APPID);
        	String category = p_request.getParameter(TMProfileConstants.MT_MS_CATEGORY);
        	if (category == null || category.length() == 0)
            {
                category = "general";
            }
        	
        	try 
        	{
        	    // If it works, it should be "internal" URL
            	String msMTEndpoint = p_request.getParameter(TMProfileConstants.MT_MS_URL);
        		MSTranslatorInvoker ms_mt = new MSTranslatorInvoker(msMTEndpoint);
        		
        		TranslationRequest transRequest = new TranslationRequest();
        		LanguagePair lp = new LanguagePair("en", "fr");
        		transRequest.setLangPair(lp);
        		String[] texts = {"hello world","I love this game"};
        		transRequest.setTexts(texts);
        		ms_mt.translate(transRequest);
        		p_request.setAttribute("URL_flag", TMProfileConstants.MT_MS_URL_FLAG_INTERNAL);
        	}
        	catch (Exception ex) 
        	{
                try
                {
                    // If it works, it should be "public" URL
                    URL baseUrl = SoapService.class.getResource(".");
                    URL url = new URL(baseUrl, p_request.getParameter(TMProfileConstants.MT_MS_URL));
                    SoapService soap = new SoapService(url);
                    LanguageService service = soap.getBasicHttpBindingLanguageService();
                    service.translate(appid, "hello world", "en", "fr",
                            MachineTranslator.MSMT_CONTENT_TYPE, category);
                    p_request.setAttribute("URL_flag", TMProfileConstants.MT_MS_URL_FLAG_PUBLIC);
                }
                catch (Exception exx)
                {
                    String exceptionInfo2 = exx.getMessage();

                    if (exceptionInfo2.indexOf("InaccessibleWSDLException") != -1
                            || exceptionInfo2.indexOf("XML reader error") != -1)
                    {
                        exceptionInfo2 = "Could not access MS Translator URL.";
                    }
                    else if (exceptionInfo2.indexOf("ArgumentOutOfRangeException") != -1
                            || exceptionInfo2.indexOf("ArgumentException") != -1)
                    {
                        exceptionInfo2 = "ArgumentException: invalid appId Parameter.";
                    }
                    p_request.setAttribute("ExceptionInfo", exceptionInfo2);
                }
        	}
    	}
    	// Test Asia Online host
    	else if (operation != null && "testAOHost".equals(operation))
    	{
    	    setChangedTmProfile(p_request, tmProfile);
    	    
            String aoMtUrl = p_request
                    .getParameter(TMProfileConstants.MT_AO_URL);
            String aoMtPort = p_request
                    .getParameter(TMProfileConstants.MT_AO_PORT);
            String aoMtUsername = p_request
                    .getParameter(TMProfileConstants.MT_AO_USERNAME);
            String aoMtPassword = p_request
                    .getParameter(TMProfileConstants.MT_AO_PASSWORD);
            String aoMtAccountNumber = p_request
                    .getParameter(TMProfileConstants.MT_AO_ACCOUNT_NUMBER);
            
            try
            {
                AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(
                        aoMtUrl, Integer.parseInt(aoMtPort), aoMtUsername,
                        aoMtPassword, Integer.parseInt(aoMtAccountNumber));
                HashMap lps = aoInvoker.getAllSupportedLanguagePairs();
                if (lps != null && lps.size() > 0) 
                {
                    sessionMgr.setAttribute("aoSupportedLocalePairs", lps);
                    s_logger.info("Asia Online totally supports " + lps.size()
                            + " language pairs.");
                }
            }
            catch (Exception ex)
            {
                String exceptionInfo = ex.getMessage();
                p_request.setAttribute("ExceptionInfo", exceptionInfo);
            }

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
            tmProfile.setMsMTCategory(p_request
                    .getParameter(TMProfileConstants.MT_MS_CATEGORY));
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


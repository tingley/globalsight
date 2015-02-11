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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.tempuri.SoapService;
import org.tempuri.SoapServiceLocator;

import com.globalsight.everest.projecthandler.TMProfileMTInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.asiaOnline.AsiaOnlineMtInvoker;
import com.globalsight.machineTranslation.mstranslator.MSMTUtil;
import com.globalsight.machineTranslation.mstranslator.MSTranslatorInvoker;
import com.globalsight.machineTranslation.safaba.SafabaTranslateUtil;
import com.microsoft.schemas.MSNSearch._2005._09.fex.LanguagePair;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationRequest;
import com.microsofttranslator.api.V2.LanguageService;
import com.microsofttranslator.api.V2.adm.AdmAccessToken;

public class ModifyMTOptionsHandler extends PageHandler 
    implements TMProfileConstants
{
    private static Logger logger =
        Logger.getLogger(ModifyMTOptionsHandler.class);

    public ModifyMTOptionsHandler()
    {
        super();
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
		if (request.getMethod().equalsIgnoreCase(
				WebAppConstants.REQUEST_METHOD_GET))
		{
			p_response.sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
			return;
		}

		HttpSession session = request.getSession(false);
        SessionManager sessionMgr = 
            (SessionManager) session.getAttribute(SESSION_MANAGER);
        
        // Store TM profile in session.
        TranslationMemoryProfile tmProfile = null;
        String id = (String) request.getParameter(TMProfileConstants.TM_PROFILE_ID);
        if (id != null)
        {
            long tmProfileId = -1;
            try {
                tmProfileId = Long.parseLong(id);
            } catch (NumberFormatException nfe) {

            }

            tmProfile = TMProfileHandlerHelper.getTMProfileById(tmProfileId);
            if (tmProfile != null) {
            	storeSafabaMtInfo(tmProfileId, request);
            }
            sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE, tmProfile);
            sessionMgr.setAttribute(TMProfileConstants.TM_PROFILE_ID, tmProfileId);
        }
        else
        {
			tmProfile = (TranslationMemoryProfile) sessionMgr
					.getAttribute(TMProfileConstants.TM_PROFILE);
            if (tmProfile != null) {
            	storeSafabaMtInfo(tmProfile.getId(), request);
            }
        }
        
        // action
    	String action = request.getParameter("action");
    	request.setAttribute("action", action);

    	// Return from "modifyPromtInfo.jsp" (there is error)
    	if ("previous".equals(action))
    	{
            String exceptionInfo = request.getParameter("ExceptionInfo");
            request.setAttribute("ExceptionInfo", exceptionInfo);
    	}
    	else if ("testMSHost".equals(action))
    	{
        	setChangedTmProfile(request, tmProfile);
        	
        	testMSHost(request, tmProfile);
    	}
    	else if ("testSAHost".equals(action)) 
        {
    	    setChangedTmProfile(request, tmProfile);

    	    testSAHost(request, tmProfile);
        }
    	else if ("testAOHost".equals(action))
    	{
    	    setChangedTmProfile(request, tmProfile);

    	    testAOHost(request, tmProfile);
    	}

    	super.invokePageHandler(p_pageDescriptor, request, p_response, p_context);
    }

    public String getMSMTAccessToken(String clientId, String clientSecret)
    {
        final String uriAPI = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"; 
        try
        {
            HttpPost post = new HttpPost(uriAPI);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", TMProfileConstants.MT_MS_GRANT_TYPE));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("scope", TMProfileConstants.MT_MS_SCOPE));
            
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(post);
            
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                String strResult = URLDecoder.decode(
                        EntityUtils.toString(httpResponse.getEntity()),
                        HTTP.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                AdmAccessToken adm = mapper.readValue(strResult, AdmAccessToken.class);
                String accessToken = adm.getAccess_token();
                return accessToken;
            }
            else 
            {
                logger.warn("Getting microsoft translation status error.");
                return null;
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getting microsoft translation access token.", e);
            return null;
        }
    }
    
    /**
     * Store the changed MT settings into a TEMP TM profile.
     */
    private void setChangedTmProfile(HttpServletRequest request,
            TranslationMemoryProfile tmProfile)
    {
        HttpSession sess = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);

        // MT engine
        String engine = request.getParameter(TMProfileConstants.MT_ENGINE);
        tmProfile.setMtEngine(engine);
        // Use MT
        String useMT = request.getParameter(TMProfileConstants.MT_USE_MT);
        if (useMT == null || !"on".equals(useMT))
        {
            tmProfile.setUseMT(false);
        }
        else
        {
            tmProfile.setUseMT(true);
        }

        // MtConfidenceScore
        String mtConfidenceScore = request.getParameter("mtConfidenceScore");
        long long_mtConfidenceScore = 0;
        try {
        	long_mtConfidenceScore = Long.parseLong(mtConfidenceScore);
        	if (long_mtConfidenceScore < 0 || long_mtConfidenceScore > 100) {
        		long_mtConfidenceScore = 0;
        	}
        } catch (Exception ex) {

        }
        tmProfile.setMtConfidenceScore(long_mtConfidenceScore);

        // show in segment editor
        String showInEditor = request
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
            String msMTUrl = request
                    .getParameter(TMProfileConstants.MT_MS_URL);
            if (msMTUrl != null && !"".equals(msMTUrl.trim()))
            {
                tmProfile.setMsMTUrl(msMTUrl.trim());
            }

            // "appid" has been abandoned.
            String appid = request
                    .getParameter(TMProfileConstants.MT_MS_APPID);
            tmProfile.setMsMTAppID(appid);
			tmProfile.setMsMTClientID(request
					.getParameter(TMProfileConstants.MT_MS_CLIENT_ID));
			tmProfile.setMsMTClientSecret(request
					.getParameter(TMProfileConstants.MT_MS_CLIENT_SECRET));

            tmProfile.setMsMTUrlFlag(request
                    .getParameter(TMProfileConstants.MT_MS_URL_FLAG));
            tmProfile.setMsMTCategory(request
                    .getParameter(TMProfileConstants.MT_MS_CATEGORY));
        }
        // Asia Online options
        else if (engine != null && engine.equalsIgnoreCase("asia_online"))
        {
            String aoMtUrl = request
                    .getParameter(TMProfileConstants.MT_AO_URL);
            if (aoMtUrl != null && !"".equals(aoMtUrl.trim()))
            {
                tmProfile.setAoMtUrl(aoMtUrl);
            }

            String aoMtPort = request
                    .getParameter(TMProfileConstants.MT_AO_PORT);
            if (aoMtPort != null && !"".equals(aoMtPort.trim()))
            {
                tmProfile.setAoMtPort(Long.parseLong(aoMtPort));
            }

            String aoMtUsername = request
                    .getParameter(TMProfileConstants.MT_AO_USERNAME);
            if (aoMtUsername != null && !"".equals(aoMtUsername.trim()))
            {
                tmProfile.setAoMtUsername(aoMtUsername);
            }

            String aoMtPassword = request
                    .getParameter(TMProfileConstants.MT_AO_PASSWORD);
            if (aoMtPassword != null && !"".equals(aoMtPassword.trim()))
            {
                tmProfile.setAoMtPassword(aoMtPassword);
            }

            String aoMtAccountNumber = request
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

    private void storeSafabaMtInfo(long tmProfileId, HttpServletRequest request)
    {
		List<?> mtInfoList = TMProfileHandlerHelper
				.getMtinfoByTMProfileIdAndEngine(tmProfileId,
						MachineTranslator.ENGINE_SAFABA);
        for (int i = 0; i < mtInfoList.size(); i++)
        {
            TMProfileMTInfo mtInfo = (TMProfileMTInfo) mtInfoList.get(i);
            request.setAttribute(mtInfo.getMtKey(), mtInfo.getMtValue());
        }
    }

    /**
	 * Test the MS MT engine is reachable for specified parameters.
	 * 
	 * @param request
	 * @param tmProfile
	 */
	private void testMSHost(HttpServletRequest request,
			TranslationMemoryProfile tmProfile)
    {
    	String clientId = request.getParameter(TMProfileConstants.MT_MS_CLIENT_ID);
    	String clientSecret = request.getParameter(TMProfileConstants.MT_MS_CLIENT_SECRET);
    	if(!TMProfileHandlerHelper.checkPassword(clientSecret))
    	{
    	    clientSecret = tmProfile.getMsMTClientSecret();
    	}
    	String category = request.getParameter(TMProfileConstants.MT_MS_CATEGORY);
    	if (category == null || category.length() == 0)
        {
            category = "general";
        }
    	
    	try 
    	{
    	    // If it works, it should be "internal" URL
        	String msMTEndpoint = request.getParameter(TMProfileConstants.MT_MS_URL);
    		MSTranslatorInvoker ms_mt = new MSTranslatorInvoker(msMTEndpoint);
    		
    		TranslationRequest transRequest = new TranslationRequest();
    		LanguagePair lp = new LanguagePair("en", "fr");
    		transRequest.setLangPair(lp);
    		String[] texts = {"hello world","I love this game"};
    		transRequest.setTexts(texts);
    		ms_mt.translate(transRequest);
    		request.setAttribute("URL_flag", TMProfileConstants.MT_MS_URL_FLAG_INTERNAL);
    	}
    	catch (Exception ex) 
    	{
            try
            {
            	// Test if it is "public" URL
            	String msMtUrl = request.getParameter(TMProfileConstants.MT_MS_URL);
                SoapService soap = new SoapServiceLocator(msMtUrl);
                String accessToken = MSMTUtil.getAccessToken(clientId, clientSecret);
                LanguageService service = soap.getBasicHttpBinding_LanguageService();
                service.translate(accessToken, "hello world", "en", "fr",
                        MachineTranslator.MSMT_CONTENT_TYPE, category);
                request.setAttribute("URL_flag", TMProfileConstants.MT_MS_URL_FLAG_PUBLIC);
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
                request.setAttribute("ExceptionInfo", exceptionInfo2);
            }
    	}
    }

    /**
	 * Test the Safaba engine is reachable for specified parameters.
	 * 
	 * @param request
	 * @param tmProfile
	 */
	private void testSAHost(HttpServletRequest request,
			TranslationMemoryProfile tmProfile)
	{
	    String safaHost = request.getParameter(TMProfileConstants.MT_SAFA_HOST);
        String safaPort = request.getParameter(TMProfileConstants.MT_SAFA_PORT);
        String safaCompanyName = request.getParameter(TMProfileConstants.MT_SAFA_COMPANY_NAME);
        String safaPassword = request.getParameter(TMProfileConstants.MT_SAFA_PASSWORD);
        String safaClient = request.getParameter(TMProfileConstants.MT_SAFA_CLIENT);
        if (!TMProfileHandlerHelper.checkPassword(safaPassword))
        {
			@SuppressWarnings("unchecked")
			List<TMProfileMTInfo> formerData =
					(List<TMProfileMTInfo>) TMProfileHandlerHelper
						.getMtinfoByTMProfileIdAndEngine(tmProfile.getId(),
								tmProfile.getMtEngine());

			TMProfileMTInfo safaPasswordMT = TMProfileHandlerHelper.getMTInfo(
					formerData, tmProfile.getId(), tmProfile.getMtEngine(),
					TMProfileConstants.MT_SAFA_PASSWORD);
            safaPassword = safaPasswordMT.getMtValue();
        }
        
        try
        {
			SafabaTranslateUtil.translate(safaHost, Integer.parseInt(safaPort),
					safaCompanyName, safaPassword, safaClient, "ENUS-DEDE",
					"This is a test.", 30);
        }
        catch (Exception e)
        {
            String errString = e.getMessage();
            if (StringUtils.isNotEmpty(errString))
            {
                request.setAttribute("ExceptionInfo", e.getMessage());
            }
            else 
            {
                request.setAttribute("ExceptionInfo", "Safaba server is not reachable.");
            }
        }
        
        request.setAttribute(TMProfileConstants.MT_SAFA_HOST, safaHost);
        request.setAttribute(TMProfileConstants.MT_SAFA_PORT, safaPort);
        request.setAttribute(TMProfileConstants.MT_SAFA_COMPANY_NAME, safaCompanyName);
        request.setAttribute(TMProfileConstants.MT_SAFA_PASSWORD, safaPassword);
        request.setAttribute(TMProfileConstants.MT_SAFA_CLIENT, safaClient);

		getSessionManager(request).setAttribute(
				TMProfileConstants.MT_SAFA_PASSWORD, safaPassword);
	}
	
    /**
	 * Test the Asian Online engine is reachable for specified parameters.
	 * 
	 * @param request
	 * @param tmProfile
	 */
	private void testAOHost(HttpServletRequest request,
			TranslationMemoryProfile tmProfile)
	{
        String aoMtUrl = request
                .getParameter(TMProfileConstants.MT_AO_URL);
        String aoMtPort = request
                .getParameter(TMProfileConstants.MT_AO_PORT);
        String aoMtUsername = request
                .getParameter(TMProfileConstants.MT_AO_USERNAME);
        String aoMtPassword = request
                .getParameter(TMProfileConstants.MT_AO_PASSWORD);
        String aoMtAccountNumber = request
                .getParameter(TMProfileConstants.MT_AO_ACCOUNT_NUMBER);
        
        try
        {
            AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(
                    aoMtUrl, Integer.parseInt(aoMtPort), aoMtUsername,
                    aoMtPassword, Integer.parseInt(aoMtAccountNumber));
            HashMap lps = aoInvoker.getAllSupportedLanguagePairs();
            if (lps != null && lps.size() > 0) 
            {
				getSessionManager(request).setAttribute(
						"aoSupportedLocalePairs", lps);
				logger.info("Asia Online totally supports " + lps.size()
						+ " language pairs.");
            }
        }
        catch (Exception ex)
        {
            String exceptionInfo = ex.getMessage();
            request.setAttribute("ExceptionInfo", exceptionInfo);
        }
	}
	
	private SessionManager getSessionManager(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(SESSION_MANAGER);

		return sessionMgr;
	}
}


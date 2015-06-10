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
package com.globalsight.everest.projecthandler;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.google.translate.api.v2.core.Translator;
import org.google.translate.api.v2.core.model.Translation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tempuri.SoapService;
import org.tempuri.SoapServiceLocator;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.machineTranslation.asiaOnline.AsiaOnlineMtInvoker;
import com.globalsight.machineTranslation.asiaOnline.DomainCombination;
import com.globalsight.machineTranslation.domt.DoMTUtil;
import com.globalsight.machineTranslation.iptranslator.IPTranslatorUtil;
import com.globalsight.machineTranslation.mstranslator.MSMTUtil;
import com.globalsight.machineTranslation.promt.ProMtInvoker;
import com.globalsight.machineTranslation.promt.ProMtPts9Invoker;
import com.globalsight.machineTranslation.safaba.SafabaTranslateUtil;
import com.globalsight.util.StringUtil;
import com.microsofttranslator.api.V2.LanguageService;

public class MachineTranslateAdapter
{

    private static Logger logger = Logger
            .getLogger(MachineTranslateAdapter.class);

    public static final String MSMT_CONTENT_TYPE = "text/plain";

    public static boolean checkPassword(String p_pass)
    {
        if (p_pass == null)
        {
            return false;
        }

        Pattern pattern = Pattern.compile("\\*+");
        Matcher matcher = pattern.matcher(p_pass);
        return !matcher.matches();
    }

    public void setMTCommonOptions(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile, String engine)
    {
        makeBaseMT(p_request, mtProfile, engine);
        Date date = new Date();
        mtProfile.setTimestamp(new Timestamp(date.getTime()));
        EngineEnum ee = EngineEnum.getEngine(engine);
        switch (ee)
        {
            case ProMT:
                setPromtParams(p_request, mtProfile);
                setExtendInfo(p_request, mtProfile);
                break;
            case Asia_Online:
                setAOParams(p_request, mtProfile);
                setExtendInfo(p_request, mtProfile);
                break;
            case Safaba:
                setSafabaParams(p_request, mtProfile);
                break;
            case MS_Translator:
                setMsMtParams(p_request, mtProfile);
                break;
            case IPTranslator:
                setIPMtParams(p_request, mtProfile);
                break;
            case DoMT:
                setDoMtParams(p_request, mtProfile);
                break;
            case Google_Translate:
                setGoogleParams(p_request, mtProfile);
                break;
        }
    }

    private void setGoogleParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String apiKey = p_request
                .getParameter(MTProfileConstants.MT_GOOGLE_API_KEY);
        if (apiKey != null)
            apiKey = apiKey.trim();

        if (StringUtils.isNotBlank(apiKey))
        {
            mtProfile.setAccountinfo(apiKey);
        }
    }

    private void makeBaseMT(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile, String engine)
    {
        String mtProfileName = p_request.getParameter("MtProfileName");
        mtProfile.setMtProfileName(mtProfileName);
        mtProfile.setMtEngine(engine);
        // MT engine
        String description = p_request.getParameter("description");
        if (StringUtils.isEmpty(description))
        {
            mtProfile.setDescription("");
        }
        else
        {
            mtProfile.setDescription(description);
        }

        // MtConfidenceScore
        String mtConfidenceScore = p_request.getParameter("mtConfidenceScore");

        long long_mtConfidenceScore = 0;
        try
        {
            long_mtConfidenceScore = Long.parseLong(mtConfidenceScore);
            if (long_mtConfidenceScore < 0 || long_mtConfidenceScore > 100)
            {
                long_mtConfidenceScore = 0;
            }
        }
        catch (Exception ex)
        {

        }
        mtProfile.setMtConfidenceScore(long_mtConfidenceScore);

        // show in segment editor
        String showInEditor = p_request
                .getParameter(MTProfileConstants.MT_SHOW_IN_EDITOR);
        if (showInEditor == null || !"on".equals(showInEditor))
        {
            mtProfile.setShowInEditor(false);
        }
        else
        {
            mtProfile.setShowInEditor(true);
        }
        String includeMTIdentifiers = p_request
                .getParameter(MTProfileConstants.MT_INCLUDE_MT_IDENTIFIERS);
        mtProfile.setIncludeMTIdentifiers("on".equals(includeMTIdentifiers));

        if (mtProfile.isIncludeMTIdentifiers())
        {
            String mtIdentifierLeading = p_request
                    .getParameter(MTProfileConstants.MT_MT_IDENTIFIER_LEADING);
            if (StringUtil.isEmpty(mtIdentifierLeading))
            {
                mtProfile.setMtIdentifierLeading("");
            }
            else
            {
                mtProfile.setMtIdentifierLeading(mtIdentifierLeading);
            }
            String mtIdentifierTrailing = p_request
                    .getParameter(MTProfileConstants.MT_MT_IDENTIFIER_TRAILING);
            if (StringUtil.isEmpty(mtIdentifierTrailing))
            {
                mtProfile.setMtIdentifierTrailing("");
            }
            else
            {
                mtProfile.setMtIdentifierTrailing(mtIdentifierTrailing);
            }
        }

        String companyName = p_request.getParameter("companyName");
        if (StringUtils.isEmpty(companyName))
        {
            companyName = UserUtil.getCurrentCompanyName(p_request);
        }
        if (companyName != null)
        {
            try
            {
                long companyId = ServerProxy.getJobHandler()
                        .getCompany(companyName).getIdAsLong();
                mtProfile.setCompanyid(companyId);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
    }

    private void setDoMtParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String url = p_request.getParameter(MTProfileConstants.MT_DOMT_URL)
                .trim();
        if (StringUtils.isNotBlank(url))
        {
            mtProfile.setUrl(url);
        }

        String engineName = p_request.getParameter(
                MTProfileConstants.MT_DOMT_ENGINE_NAME).trim();
        if (StringUtils.isNotBlank(engineName))
        {
            mtProfile.setCategory(engineName);
        }
    }

    private void setIPMtParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String url = p_request.getParameter(MTProfileConstants.MT_IP_URL)
                .trim();

        if (StringUtils.isNotBlank(url))
        {
            mtProfile.setUrl(url);
        }
        String key = p_request.getParameter(MTProfileConstants.MT_IP_KEY)
                .trim();
        if (StringUtils.isNotBlank(key))
        {
            mtProfile.setPassword(key);
        }
    }

    /**
     * Set Promt specified parameters into TM profile object for save.
     */
    private void setPromtParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        // set pts url
        String ptsUrl = p_request.getParameter(MTProfileConstants.MT_PTSURL);
        if (ptsUrl != null && !"".equals(ptsUrl.trim())
                && !"null".equals(ptsUrl.trim()))
        {
            mtProfile.setUrl(ptsUrl.trim());
        }
        // set pts username
        String ptsUsername = p_request
                .getParameter(MTProfileConstants.MT_PTS_USERNAME);
        mtProfile.setUsername(ptsUsername.trim());
        // set pts password
        String ptsPassword = p_request
                .getParameter(MTProfileConstants.MT_PTS_PASSWORD);
        if (checkPassword(ptsPassword))
        {
            mtProfile.setPassword(ptsPassword);
        }

    }

    private void setExtendInfo(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String[] dirNames = p_request.getParameterValues("dirName");
        if (dirNames == null || dirNames.length == 0)
            return;
        Set<MachineTranslationExtentInfo> exInfo = mtProfile.getExInfo();
        if (exInfo == null || exInfo.size() < dirNames.length)
        {

            exInfo = exInfo == null ? new HashSet<MachineTranslationExtentInfo>()
                    : exInfo;
            for (int i = exInfo.size(); i < dirNames.length; i++)
            {
                exInfo.add(new MachineTranslationExtentInfo());
            }
            mtProfile.setExInfo(exInfo);
        }

        Iterator<MachineTranslationExtentInfo> it = exInfo.iterator();
        int i = 0;
        while (it.hasNext())
        {
            MachineTranslationExtentInfo mtExtentInfo = it.next();
            String[] key = dirNames[i].split("@");
            mtExtentInfo.setSelfInfo(key);
            mtExtentInfo.setMtProfile(mtProfile);
            i++;
        }

    }

    /**
     * Set MS MT specified parameters into TM profile object for save.
     */
    private void setMsMtParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String url = p_request.getParameter(MTProfileConstants.MT_MS_URL);
        String category = p_request
                .getParameter(MTProfileConstants.MT_MS_CATEGORY);
        String clientId = p_request
                .getParameter(MTProfileConstants.MT_MS_CLIENT_ID);
        String clientSecret = p_request
                .getParameter(MTProfileConstants.MT_MS_CLIENT_SECRET);

        if (url != null && !"".equals(url.trim()))
        {
            mtProfile.setUrl(url.trim());
        }

        if (StringUtils.isNotEmpty(clientId))
        {
            mtProfile.setUsername(clientId.trim());
        }

        if (StringUtils.isNotEmpty(clientSecret) && checkPassword(clientSecret))
        {
            mtProfile.setPassword(clientSecret.trim());
        }

        if (category != null && !category.equals(""))
        {
            mtProfile.setCategory(category.trim());
        }
        else
        {
            mtProfile.setCategory("general");
        }

        String srRS = p_request.getParameter("sr_RS");
        if (StringUtils.isEmpty(srRS)) {
        	srRS = "sr-Latn";
        }
        String srYU = p_request.getParameter("sr_YU");
        if (StringUtils.isEmpty(srYU)) {
        	srYU = "sr-Latn";
        }
        JSONArray arr = new JSONArray();
        JSONObject srRSObj = new JSONObject();
        JSONObject srYUObj = new JSONObject();
        try {
			srRSObj.put("sr_RS", srRS);
			srYUObj.put("sr_YU", srYU);
			arr.put(srRSObj);
			arr.put(srYUObj);
			mtProfile.setJsonInfo(arr.toString());
		} catch (JSONException e) {
			// default.
			mtProfile.setJsonInfo("[{\"sr_RS\":\"sr-Latn\"},{\"sr_YU\":\"sr-Latn\"}]");
			logger.warn("Fail to save MT setting for MS Translator Serbian: "
					+ e.getMessage());
		}
    }

    /**
     * Set Safaba specified parameters into TM profile object for save.
     */
    private void setSafabaParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String safaHost = p_request
                .getParameter(MTProfileConstants.MT_SAFA_HOST);
        String safaPort = p_request.getParameter(
                MTProfileConstants.MT_SAFA_PORT).trim();
        String safaCompanyName = p_request
                .getParameter(MTProfileConstants.MT_SAFA_COMPANY_NAME);
        String safaClient = p_request
                .getParameter(MTProfileConstants.MT_SAFA_CLIENT);
        String safaPassword = p_request
                .getParameter(MTProfileConstants.MT_SAFA_PASSWORD);
        mtProfile.setUrl(safaHost);

        mtProfile.setPort(Integer.parseInt(safaPort.trim()));
        mtProfile.setAccountinfo(safaClient);
        mtProfile.setUsername(safaCompanyName);
        mtProfile.setPassword(safaPassword);
    }

    /**
     * Set Asian Online specified parameters into TM profile object for save.
     */
    private void setAOParams(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        // MS Translator options

        String aoMtUrl = p_request.getParameter(MTProfileConstants.MT_AO_URL);
        mtProfile.setUrl(aoMtUrl.trim());
        String aoMtPort = p_request.getParameter(MTProfileConstants.MT_AO_PORT)
                .trim();
        mtProfile.setPort(Integer.parseInt(aoMtPort.trim()));
        String aoMtUsername = p_request
                .getParameter(MTProfileConstants.MT_AO_USERNAME);
        mtProfile.setUsername(aoMtUsername);
        String aoMtPassword = p_request
                .getParameter(MTProfileConstants.MT_AO_PASSWORD);
        mtProfile.setPassword(aoMtPassword);
        String aoMtAccountNumber = p_request.getParameter(
                MTProfileConstants.MT_AO_ACCOUNT_NUMBER).trim();
        mtProfile.setAccountinfo(aoMtAccountNumber);
    }

    public boolean testMTCommonOptions(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        EngineEnum ee = EngineEnum.getEngine(mtProfile.getMtEngine());
        switch (ee)
        {
            case ProMT:
                return testPromtInfo(mtProfile, writer);
            case Asia_Online:
                return testAOHost(mtProfile, writer);
            case Safaba:
                return testSafabaHost(mtProfile, writer);
            case MS_Translator:
                return testMSHost(mtProfile, writer);
            case IPTranslator:
                return testIPHost(mtProfile, writer);
            case DoMT:
                return testDoMT(mtProfile, writer);
            case Google_Translate:
                return testGoogle(mtProfile, writer);
        }

        return false;
    }

    private boolean testGoogle(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String encodedText = "";

        try
        {
            encodedText = URLEncoder.encode("This is test", "UTF-8");
        }
        catch (UnsupportedEncodingException e1)
        {
            logger.error(e1);
        }

        String apiKey = mtProfile.getAccountinfo();
        Translator translator = new Translator(apiKey);
        Translation translation = null;

        try
        {
            translation = translator.translate(encodedText, "en", "fr");
        }
        catch (Exception e)
        {
            JSONObject jso = new JSONObject();
            jso.put("ExceptionInfo", new String(
                    "Connection to https://www.googleapis.com refused."));
            writer.write(jso.toString());
            logger.error(e);
            return false;
        }

        if (translation != null)
        {
            return true;
        }

        return false;
    }

    /**
     * Test the MS MT engine is reachable for specified parameters.
     * 
     * @param mtProfile
     * @param writer
     * @param tmProfile
     * @return
     * @throws JSONException
     */
    private boolean testMSHost(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String clientId = mtProfile.getUsername();
        String clientSecret = mtProfile.getPassword();
        String category = mtProfile.getCategory();
        if (StringUtils.isBlank(category))
        {
            category = "general";
        }

        try
        {
            // Test if it is "public" URL
            String msMtUrl = mtProfile.getUrl();
            SoapService soap = new SoapServiceLocator(msMtUrl);
            String accessToken = MSMTUtil
                    .getAccessToken(clientId, clientSecret);
            LanguageService service = soap
                    .getBasicHttpBinding_LanguageService();
            service.translate(accessToken, "hello world", "en", "fr",
                    MSMT_CONTENT_TYPE, category);
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
                exceptionInfo2 = "Invalid Parameter.";
            }
            JSONObject jso = new JSONObject();
            jso.put("ExceptionInfo", exceptionInfo2);
            writer.write(jso.toString());
            return false;
        }

        return true;
    }

    /**
     * Test the Safaba engine is reachable for specified parameters.
     * 
     * @param mtProfile
     * @param writer
     * @param tmProfile
     * @return
     * @throws JSONException
     */
    private boolean testSafabaHost(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String safaHost = mtProfile.getUrl();
        Integer safaPort = mtProfile.getPort();
        String safaCompanyName = mtProfile.getUsername();
        String safaPassword = mtProfile.getPassword();
        String safaClient = mtProfile.getAccountinfo();

        try
        {
            SafabaTranslateUtil.translate(safaHost, (int) safaPort,
                    safaCompanyName, safaPassword, safaClient, "ENUS-DEDE",
                    "This is a test.", 30);
        }
        catch (Exception e)
        {
            String errString = e.getMessage();
            if (StringUtils.isNotEmpty(errString))
            {
                JSONObject jso = new JSONObject();
                jso.put("ExceptionInfo", e.getMessage());
                writer.write(jso.toString());
                return false;
            }
            else
            {
                JSONObject jso = new JSONObject();
                jso.put("ExceptionInfo", "Safaba server is not reachable.");
                writer.write(jso.toString());
                return false;
            }
        }
        return true;

    }

    /**
     * Test the Asian Online engine is reachable for specified parameters.
     * 
     * @param mtProfile
     * @param writer
     * @param tmProfile
     * @return
     * @throws JSONException
     */
    private boolean testAOHost(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String aoMtUrl = mtProfile.getUrl();
        Integer aoMtPort = mtProfile.getPort();
        String aoMtUserName = mtProfile.getUsername();
        String aoMtPassword = mtProfile.getPassword();
        String aoMtAccountNumber = mtProfile.getAccountinfo();

        try
        {
            AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(aoMtUrl,
                    (int) aoMtPort, aoMtUserName, aoMtPassword,
                    Integer.parseInt(aoMtAccountNumber.trim()));

            Map aoSupportedLocalePairs = aoInvoker
                    .getAllSupportedLanguagePairs();

            Map dirToTopicTemplateHM = new TreeMap();
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
                        List directionsTplList = new ArrayList();
                        DomainCombination firstDC = (DomainCombination) dcListForSpecifiedLPCode
                                .get(0);
                        String lpName4show = firstDC.getSourceLanguage() + "-"
                                + firstDC.getTargetLanguage();
                        String lpName = firstDC.getSourceAbbreviation() + "-"
                                + firstDC.getTargetAbbreviation();
                        for (int i = 0; i < dcListForSpecifiedLPCode.size(); i++)
                        {
                            DomainCombination dc = (DomainCombination) dcListForSpecifiedLPCode
                                    .get(i);
                            String dcName = dc.getDomainCombinationName();
                            String dcCode = dc.getCode();
                            directionsTplList.add(dcCode + "@" + dcName);
                        }
                        dirToTopicTemplateHM.put(lpName + "@" + strLPCode
                                + "@#" + lpName4show, directionsTplList);

                    }
                }
            }
            JSONObject jso = new JSONObject(dirToTopicTemplateHM);
            String jsonInfo = jso.toString();
            mtProfile.setJsonInfo(jsonInfo);
            // jsonMap.put(mtProfile.getId(), jsonInfo);
            writer.write(jsonInfo);
            return false;

        }
        catch (Exception ex)
        {
            String exceptionInfo = ex.getMessage();
            exceptionInfo = "Asia_Online server is not reachable.Please check "
                    + exceptionInfo;
            JSONObject jso = new JSONObject();
            jso.put("ExceptionInfo", exceptionInfo);
            writer.write(jso.toString());
            return false;
        }
    }

    private boolean testPromtInfo(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        Map<String, String> dirsMap = null;
        List directionsList = new ArrayList();
        Map dirToTopicTemplateHM = new TreeMap();
        String exMsg = null;

        String ptsUrl = mtProfile.getUrl();
        // set pts username
        String ptsUsername = mtProfile.getUsername();
        // set pts password
        String ptsPassword = mtProfile.getPassword();
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
                        dirToTopicTemplateHM.put(lpName + "@" + lpId + "@",
                                directionsTplList);
                    }
                    else
                    {
                        // if there is no template info for this direction,
                        // then remove it from directions map
                        dirsMap.remove(lpName);
                    }
                }
            }
            JSONObject jso = new JSONObject(dirToTopicTemplateHM);
            String jsonInfo = jso.toString();
            mtProfile.setJsonInfo(jsonInfo);
            // jsonMap.put(mtProfile.getId(), jsonInfo);
            writer.write(jsonInfo);

            return false;

        }
        catch (Exception ex)
        {
            exMsg = ex.getMessage();
            if (exMsg.toLowerCase().indexOf("unknownhostexception") != -1)
            {
                int index = exMsg.toLowerCase().indexOf("unknownhostexception");
                exMsg = exMsg.substring(index + "unknownhostexception".length()
                        + 1, exMsg.length());
                // exMsg = "Unkown Host:" + exMsg;
            }

            try
            {
                // Try PTS9 APIs.
                ProMtPts9Invoker invoker2 = null;
                if (ptsUsername != null && !"".equals(ptsUsername))
                {
                    invoker2 = new ProMtPts9Invoker(ptsUrl, ptsUsername,
                            ptsPassword);
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
                            dirToTopicTemplateHM.put(lpName + "@" + lpId,
                                    directionsTplList);
                        }
                        else
                        {
                            // if there is no template info for this direction,
                            // then remove it from directions map
                            dirsMap.remove(lpName);
                        }
                    }
                }

                JSONObject jso = new JSONObject(dirToTopicTemplateHM);
                String jsonInfo = jso.toString();
                mtProfile.setJsonInfo(jsonInfo);
                // jsonMap.put(mtProfile.getId(), jsonInfo);
                writer.write(jsonInfo);

                return false;

            }
            catch (Exception e)
            {
                exMsg = e.getMessage();
                JSONObject jso = new JSONObject();

                jso.put("ExceptionInfo", exMsg);
                writer.write(jso.toString());
            }
        }
        return false;
    }

    private boolean testIPHost(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String ipUrl = mtProfile.getUrl();
        String ipKey = mtProfile.getPassword();

        try
        {
            IPTranslatorUtil.testIPHost(ipUrl, ipKey, "En", "Fr");
        }
        catch (Exception e)
        {
            String errString = "IP Translator server is not reachable.";
            if (StringUtils.isNotEmpty(errString))
            {
                errString = "IP Translator URL or Key is invalid.";
                logger.warn(e.getMessage());
            }
            JSONObject jso = new JSONObject();
            jso.put("ExceptionInfo", errString);
            writer.write(jso.toString());
            return false;
        }
        return true;
    }

    private boolean testDoMT(MachineTranslationProfile mtProfile,
            PrintWriter writer) throws JSONException
    {
        String url = mtProfile.getUrl();
        String engineName = mtProfile.getCategory();

        try
        {
            DoMTUtil.testDoMtHost(url, engineName);
        }
        catch (Exception e)
        {
            String errString = e.getMessage();
            JSONObject jso = new JSONObject();
            jso.put("ExceptionInfo", errString);
            writer.write(jso.toString());

            return false;
        }
        return true;
    }

    private String checkLang(Locale p_locale)
    {
        if (p_locale == null)
        {
            return "";
        }

        String lang = p_locale.getLanguage();
        if ("in".equalsIgnoreCase(lang))
        {
            lang = "id";
        }

        return lang;
    }

    public boolean isSupportsLocalePair(MachineTranslationProfile mt,
            Locale sourcelocale, Locale targetlocale)
    {
        boolean isSupportLocalePair = false;
        MachineTranslationExtentInfo result = null;

        EngineEnum ee = EngineEnum.getEngine(mt.getMtEngine());
        String lp = "";
        switch (ee)
        {
            case ProMT:
                lp = getLanguagePairNameForProMt(sourcelocale, targetlocale);
                break;
            case IPTranslator:
                return IPTranslatorUtil.supportsLocalePair(sourcelocale,
                        targetlocale);
            case Asia_Online:
                lp = getLanguagePairNameForAo(sourcelocale, targetlocale);
                // Currently AO supports zh-CN, not support zh-HK and zh-TW.
                if (sourcelocale.getLanguage().equalsIgnoreCase("ZH")
                        && !sourcelocale.getCountry().equalsIgnoreCase("CN"))
                {
                    return false;
                }
                if (targetlocale.getLanguage().equalsIgnoreCase("ZH")
                        && !targetlocale.getCountry().equalsIgnoreCase("CN"))
                {
                    return false;
                }
                break;
        }
        try
        {
            Set lp2DomainCombinations = mt.getExInfo();
            if (lp2DomainCombinations != null
                    && lp2DomainCombinations.size() > 0)
            {
                Iterator lp2DCIt = lp2DomainCombinations.iterator();
                while (lp2DCIt.hasNext())
                {
                    MachineTranslationExtentInfo aoLP2DC = (MachineTranslationExtentInfo) lp2DCIt
                            .next();
                    String lpName = aoLP2DC.getLanguagePairName();
                    if (lpName != null && lpName.equalsIgnoreCase(lp))
                    {
                        result = aoLP2DC;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
        isSupportLocalePair = result == null ? false : true;

        return isSupportLocalePair;
    }

    private String getLanguagePairNameForAo(Locale sourcelocale,
            Locale targetlocale)
    {
        String srcLang = checkLang(sourcelocale);
        String trgLang = checkLang(targetlocale);
        String lp = srcLang + "-" + trgLang;
        return lp;
    }

    private String getLanguagePairNameForProMt(Locale p_sourceLocale,
            Locale p_targetLocale)
    {
        if (p_sourceLocale == null || p_targetLocale == null)
        {
            return null;
        }

        String srcLang = p_sourceLocale.getDisplayLanguage(Locale.ENGLISH);
        String srcCountry = p_sourceLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(srcLang) && "China".equals(srcCountry))
        {
            srcLang = "Chinese (Simplified)";
        }

        String trgLang = p_targetLocale.getDisplayLanguage(Locale.ENGLISH);
        String trgCountry = p_targetLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(trgLang) && "China".equals(trgCountry))
        {
            trgLang = "Chinese (Simplified)";
        }

        return (srcLang + "-" + trgLang);
    }
}

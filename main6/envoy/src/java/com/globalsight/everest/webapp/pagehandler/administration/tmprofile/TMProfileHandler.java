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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.projecthandler.AsiaOnlineLP2DomainInfo;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProMTInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TMProfileComparator;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.asiaOnline.DomainCombination;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * TMProfileHandler is the page handler responsible for displaying a list of tm
 * profiles and perform actions supported by the UI (JSP).
 */

public class TMProfileHandler extends PageHandler implements TMProfileConstants
{

    // non user related state
    private int num_per_page; // number of tm profiles per page
    private static final Logger CATEGORY = Logger
            .getLogger(TMProfileHandler.class);
    private static final String COMMA = ",";

    public TMProfileHandler()
    {
        try
        {
            num_per_page = SystemConfiguration.getInstance().getIntParameter(
                    SystemConfigParamNames.NUM_TMPROFILES_PER_PAGE);
        }
        catch (Exception e)
        {
            num_per_page = 10;
        }
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
        TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr
                .getAttribute(TMProfileConstants.TM_PROFILE);

        String action = (String) p_request
                .getParameter(TMProfileConstants.ACTION);
        if (action != null && action.equals(TMProfileConstants.SAVE_ACTION))
        {
            if (tmProfile == null)
            {
                tmProfile = parseHTTPRequest(p_request, "NEW");

                // Judge if the tm profile with the same name has existed,
                // otherwise do not save it.
                boolean ifSave = true;
                try
                {
                    Collection allTMProfiles = ServerProxy.getProjectHandler()
                            .getAllTMProfiles();
                    if (allTMProfiles != null && allTMProfiles.size() > 0)
                    {
                        Iterator itTmProfiles = allTMProfiles.iterator();
                        while (itTmProfiles.hasNext())
                        {
                            TranslationMemoryProfile innerTmProfile = (TranslationMemoryProfile) itTmProfiles
                                    .next();
                            String tmProfileName = innerTmProfile.getName();
                            if (tmProfileName != null
                                    && tmProfileName
                                            .equals(tmProfile.getName()))
                            {
                                ifSave = false;
                            }
                        }
                    }
                } 
                catch (Exception e)
                {
                    ifSave = false;
                }

                if (ifSave)
                {
                    TMProfileHandlerHelper.saveTMProfile(tmProfile);
                    clearSessionExceptTableInfo(sess, TMP_KEY);
                    saveRelationShipWithSR(p_request, tmProfile);
                }
            }
            else
            {
                tmProfile = parseHTTPRequest(p_request, "MODIFY");
                boolean orderchanged = tmProfile.tmOrderChanged();
                TMProfileHandlerHelper.saveTMProfile(tmProfile);
                clearSessionExceptTableInfo(sess, TMP_KEY);
                if (orderchanged)
                {
                    ArrayList msg = new ArrayList();
                    msg.add(tmProfile.getProjectTMsToLeverageFrom());
                    msg.add(Long.toString(tmProfile.getId()));
                    try
                    {
                        JmsHelper.sendMessageToQueue(msg,
                                JmsHelper.JMS_UPDATE_lEVERAGE_MATCH_QUEUE);
                    }
                    catch (JMSException e)
                    {
                        CATEGORY.error(e);
                        throw new EnvoyServletException(e);
                    }
                    catch (NamingException e)
                    {
                        CATEGORY.error(e);
                        throw new EnvoyServletException(e);
                    }
                }

                saveRelationShipWithSR(p_request, tmProfile);
            }
        }
        else if (action != null
                && action.equals(TMProfileConstants.CANCEL_ACTION))
        {
            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (action != null
                && action.equals(TMProfileConstants.CANCEL_MT_OPTIONS_ACTION))
        {
            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (action != null
                && action.equals(TMProfileConstants.SAVE_MT_OPTIONS_ACTION))
        {
            String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
            if (engine != null && !"asia_online".equalsIgnoreCase(engine))
            {
                tmProfile = setMTCommonOptions(p_request, tmProfile);                
            }

            // For "ProMT" only
            if("promt".equals(engine.toLowerCase().trim())) 
            {
            	//set pts url
                String ptsUrl = p_request.getParameter(TMProfileConstants.MT_PTSURL);
                if (ptsUrl != null && !"".equals(ptsUrl.trim())
                		&& !"null".equals(ptsUrl.trim())) 
                {
                	tmProfile.setPtsurl(ptsUrl.trim());
                }
                //set pts username
                String ptsUsername = p_request.getParameter(TMProfileConstants.MT_PTS_USERNAME);
                tmProfile.setPtsUsername(ptsUsername.trim());
                //set pts password
                String ptsPassword = p_request.getParameter(TMProfileConstants.MT_PTS_PASSWORD);
                tmProfile.setPtsPassword(ptsPassword);
                // set pts url flag
                String ptsUrlFlag = p_request.getParameter(TMProfileConstants.MT_PTS_URL_FLAG);
                tmProfile.setPtsUrlFlag(ptsUrlFlag);
                
                HashMap dirMap = (HashMap) sessionMgr.getAttribute("directionsMap");
                Vector promtInfosVector = new Vector();
                if (dirMap != null && !dirMap.isEmpty()) 
                {
                    Iterator dirIt = dirMap.entrySet().iterator();
                    while (dirIt.hasNext()) 
                    {
                    	ProMTInfo promtInfo = new ProMTInfo();
                    	Entry dirEntry = (Entry) dirIt.next();
                    	String dirId = (String) dirEntry.getValue();
                    	promtInfo.setDirId(Long.parseLong(dirId));
                    	promtInfo.setDirName((String) dirEntry.getKey());
                    	String tpl = p_request.getParameter((String)dirEntry.getKey());
                    	promtInfo.setTopicTemplateId(tpl);
                    	promtInfo.setTMProfile(tmProfile);
                    	promtInfosVector.addElement(promtInfo);
                    }
                }
                tmProfile.setAllPromtInfo(promtInfosVector);
            }
            
            // For MS Translator only
            if ("ms_translator".equals(engine.toLowerCase().trim()))
            {
            	String url = p_request.getParameter(TMProfileConstants.MT_MS_URL);
            	String appid = p_request.getParameter(TMProfileConstants.MT_MS_APPID);
            	String url_flag = p_request.getParameter(TMProfileConstants.MT_MS_URL_FLAG);
            	String category = p_request.getParameter(TMProfileConstants.MT_MS_CATEGORY);
            	if (url != null && !"".equals(url.trim()))
            	{
            		tmProfile.setMsMTUrl(url.trim());
            	}
            	if (appid != null && !appid.trim().equals("")) 
            	{
            		tmProfile.setMsMTAppID(appid.trim());
            	}
            	if (url_flag != null && !url_flag.equals("")) 
            	{
            		tmProfile.setMsMTUrlFlag(url_flag);
            	}
            	if (category != null && !category.equals(""))
            	{
            	    tmProfile.setMsMTCategory(category);
            	}
            }
            
            // For Asia Online only
            if ("asia_online".equals(engine.toLowerCase().trim()))
            {
                Vector tmProfileAoInfoVec = new Vector();
                HashMap domainCombinations = (HashMap) sessionMgr
                        .getAttribute("domainCombinationMap");
                if (domainCombinations != null && domainCombinations.size() > 0)
                {
                    Iterator lpCodeEntryIter = domainCombinations.entrySet().iterator();
                    while (lpCodeEntryIter.hasNext())
                    {
                        AsiaOnlineLP2DomainInfo aoLP2DC = new AsiaOnlineLP2DomainInfo();
                        
                        Map.Entry entry = (Map.Entry) lpCodeEntryIter.next();
                        String lpCode = (String) entry.getKey();
                        aoLP2DC.setLanguagePairCode(Long.parseLong(lpCode));

                        List dcListForSpecifiedLPCode = (List) entry.getValue();
                        DomainCombination firstDC = 
                            (DomainCombination) dcListForSpecifiedLPCode.get(0);
                        String lpName = firstDC.getSourceAbbreviation() + "-"
                                + firstDC.getTargetAbbreviation();
                        aoLP2DC.setLanguagePairName(lpName);

                        String dcCode = p_request.getParameter(lpCode);
                        aoLP2DC.setDomainCombinationCode(Long.parseLong(dcCode));
                        aoLP2DC.setTmProfile(tmProfile);
                        tmProfileAoInfoVec.addElement(aoLP2DC);
                    }
                }
                
                tmProfile.setTmProfileAoInfoVector(tmProfileAoInfoVec);
            }

            TMProfileHandlerHelper.saveTMProfile(tmProfile);
            clearInvalidPromtAndAOSettings();
            
            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (action != null
				&& action.equals(TMProfileConstants.REMOVE_ACTION)) 
        {
			String id = (String) p_request
					.getParameter(WebAppConstants.RADIO_BUTTON);
			if (id == null
					|| p_request.getMethod().equalsIgnoreCase(
							WebAppConstants.REQUEST_METHOD_GET)) 
			{
				p_response
						.sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
				return;
			}
			if (id != null) 
			{
				long tmProfileId = -1;
				try {
					tmProfileId = Long.parseLong(id);
				} 
				catch (NumberFormatException nfe) 
				{
				}
				TranslationMemoryProfile tmProfileToBeDeleted = TMProfileHandlerHelper
						.getTMProfileById(tmProfileId);

				try 
				{
					ServerProxy.getProjectHandler().removeTmProfile(
							tmProfileToBeDeleted);
					clearSessionExceptTableInfo(sess, TMP_KEY);

					SegmentationRuleFile segRuleFile = ServerProxy
							.getSegmentationRuleFilePersistenceManager()
							.getSegmentationRuleFileByTmpid(id);
					ServerProxy.getSegmentationRuleFilePersistenceManager()
							.deleteSegmentationRuleFile(segRuleFile);
				} 
				catch (Exception e) 
				{
					CATEGORY.error(e.getMessage());
				}
			}

		}

        selectTMProfilesForDisplay(p_request, sess);
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * for segmentation rule relate with tm_profile, Delete old relationship and
     * create new relationship
     */
    private void saveRelationShipWithSR(HttpServletRequest p_request,
            TranslationMemoryProfile p_tmProfile)
    {
        try
        {
            String ruleId = p_request
                    .getParameter(TMProfileConstants.SELECTED_SR);
            String tmpId = p_tmProfile.getIdAsLong().toString();

            ServerProxy.getSegmentationRuleFilePersistenceManager()
                    .createRelationshipWithTmp(ruleId, tmpId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    // Select all tm profiles that should be displayed
    private void selectTMProfilesForDisplay(HttpServletRequest p_request,
            HttpSession p_session) throws ServletException, IOException,
            EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        List tmProfiles = null;
        try
        {
            tmProfiles = TMProfileHandlerHelper.getAllTMProfiles();
            setTableNavigation(p_request, p_session, tmProfiles,
                    new TMProfileComparator(uiLocale), num_per_page, TMPS_LIST,
                    TMP_KEY);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    private TranslationMemoryProfile parseHTTPRequest(
            HttpServletRequest p_request, String p_requestType)
            throws ServletException, IOException, EnvoyServletException
    {
        // There should be twenty fields to parse from the JSP
        TranslationMemoryProfile tmProfile = null;
        if (p_requestType.equals("NEW"))
        {
            tmProfile = new TranslationMemoryProfile();
        }
        else if (p_requestType.equals("MODIFY"))
        {
            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            tmProfile = (TranslationMemoryProfile) sessionMgr
                    .getAttribute(TMProfileConstants.TM_PROFILE);
            try
            {
                tmProfile = ServerProxy.getProjectHandler().getTMProfileById(
                        tmProfile.getId(), true);
            }
            catch (Exception e)
            {
                //
            }
        }
        // 1
        String name = p_request.getParameter(TMProfileConstants.NAME_FIELD);
        tmProfile.setName(name);
        // 2
        String description = p_request
                .getParameter(TMProfileConstants.DESCRIPTION_FIELD);
        tmProfile.setDescription(description);
        // 3
        long projectTmIdToSave = Long.parseLong(p_request
                .getParameter(TMProfileConstants.PROJECT_TM_ID_TO_SAVE));
        if (projectTmIdToSave > 0)
        {
            tmProfile.setProjectTmIdForSave(projectTmIdToSave);
        }
        // 4
        String saveToProjectTm = p_request.getParameter("isSaveToProjectTm");
        if (saveToProjectTm == null)
        {
            tmProfile.setSaveUnLocSegToProjectTM(false);
        }
        else if (saveToProjectTm.equals("true"))
        {
            tmProfile.setSaveUnLocSegToProjectTM(true);
        }
        // 5
        String saveToPageTm = p_request.getParameter("isSaveToPageTm");
        if (saveToPageTm == null)
        {
            tmProfile.setSaveUnLocSegToPageTM(false);
        }
        else if (saveToPageTm.equals("true"))
        {
            tmProfile.setSaveUnLocSegToPageTM(true);
        }
        // 6
        String excludeItemType = p_request.getParameter("excludeItemType");
        if (excludeItemType != null && excludeItemType.length() > 0)
        {
            tmProfile.setExcludeTuTypes(excludeItemType);
        }
        else if (excludeItemType == null || excludeItemType.length() == 0)
        {
            tmProfile.setExcludeTuTypes(excludeItemType);
        }
        // 7
        String levLocalizeable = p_request.getParameter("levLocalizable");
        if (levLocalizeable == null)
        {
            tmProfile.setLeverageLocalizable(false);
        }
        else if (levLocalizeable.equals("true"))
        {
            tmProfile.setLeverageLocalizable(true);
        }
        // 8
        String levExactMatches = p_request.getParameter("levExactMatches");
        if (levExactMatches == null)
        {
            tmProfile.setIsExactMatchLeveraging(false);
        }
        else if (levExactMatches.equals("true"))
        {
            tmProfile.setIsExactMatchLeveraging(true);
        }

        String levContextMatches = p_request
                .getParameter("Leverage in-context matches");
        if (levContextMatches == null)
        {
            tmProfile.setIsContextMatchLeveraging(false);
        }
        else if (levContextMatches.equals("true"))
        {
            tmProfile.setIsContextMatchLeveraging(true);
        }

        // 9
        String leveragePTM = p_request.getParameter("leveragedProjects");
        String leverageProjectTMIndexs = p_request.getParameter("indexes");
        Vector leverageProjectTMs = new Vector();
        if (leveragePTM != null && leveragePTM.length() > 0)
        {
            StringTokenizer tokenizer = new StringTokenizer(leveragePTM, COMMA);
            StringTokenizer indexTokenizer = new StringTokenizer(
                    leverageProjectTMIndexs, COMMA);
            while (tokenizer.hasMoreTokens() && indexTokenizer.hasMoreTokens())
            {
                String value = tokenizer.nextToken();
                String indexValue = indexTokenizer.nextToken();
                LeverageProjectTM leverageProjectTM = new LeverageProjectTM();
                long projectTmId = Long.parseLong(value);
                int projectTmIndex = Integer.parseInt(indexValue);
                leverageProjectTM.setProjectTmId(projectTmId);
                leverageProjectTM.setTMProfile(tmProfile);
                leverageProjectTM.setProjectTmIndex(projectTmIndex);
                leverageProjectTMs.add(leverageProjectTM);
            }
        }
        tmProfile.setNewProjectTMs(leverageProjectTMs);

        // 10 &11
        String typeSensitiveLeveraging = p_request
                .getParameter(TMProfileConstants.TYPE_SENSITIVE_LEVERAGING);
        String typeDiffP = p_request
                .getParameter(TMProfileConstants.TYPE_DIFFERENCE_PENALTY);
        long typeDiffPenalty = -1;
        if (typeDiffP != null && typeDiffP.length() > 0)
        {
            typeDiffPenalty = Long.parseLong(typeDiffP);
        }
        if (typeSensitiveLeveraging == null)
        {
            tmProfile.setIsTypeSensitiveLeveraging(false);
        }
        else if (typeSensitiveLeveraging.equals("true"))
        {
            tmProfile.setIsTypeSensitiveLeveraging(true);
        }
        tmProfile.setTypeDifferencePenalty(typeDiffPenalty);

        //

        String isRefTm = p_request.getParameter("isRefTm");
        String refTmP = p_request.getParameter("refTmPenalty");
        long refTmPenalty = -1;
        if (refTmP != null && refTmP.trim().length() != 0)
        {
            refTmPenalty = Long.parseLong(refTmP);
        }
        if (isRefTm == null)
        {
            tmProfile.setSelectRefTm(false);
        }
        else if (isRefTm.equals("true"))
        {
            tmProfile.setSelectRefTm(true);
        }
        tmProfile.setRefTmPenalty(refTmPenalty);

        String leverageRefPTMs = p_request.getParameter("leveragedRefProjects");
        tmProfile.setRefTMsToLeverageFrom(leverageRefPTMs);

        // 12 and 13
        String caseSensitiveLeveraging = p_request
                .getParameter("caseSensitiveLeveraging");
        String caseDiffP = p_request.getParameter("caseDiffPenalty");
        long caseDiffPenalty = -1;
        if (caseDiffP != null && caseDiffP.length() > 0)
        {
            caseDiffPenalty = Long.parseLong(caseDiffP);
        }
        if (caseSensitiveLeveraging == null)
        {
            tmProfile.setIsCaseSensitiveLeveraging(false);
        }
        else if (caseSensitiveLeveraging.equals("true"))
        {
            tmProfile.setIsCaseSensitiveLeveraging(true);
        }
        tmProfile.setCaseDifferencePenalty(caseDiffPenalty);
        // 14 & 15
        String whitespaceSensitiveLeveraging = p_request
                .getParameter("whitespaceSensitiveLeveraging");
        String whiteDiffP = p_request.getParameter("whiteDiffPenalty");
        long whiteDiffPenalty = -1;
        if (whiteDiffP != null && whiteDiffP.length() > 0)
        {
            whiteDiffPenalty = Long.parseLong(whiteDiffP);
        }
        if (whitespaceSensitiveLeveraging == null)
        {
            tmProfile.setIsWhiteSpaceSensitiveLeveraging(false);
        }
        else if (whitespaceSensitiveLeveraging.equals("true"))
        {
            tmProfile.setIsWhiteSpaceSensitiveLeveraging(true);
        }
        tmProfile.setWhiteSpaceDifferencePenalty(whiteDiffPenalty);
        // 16 & 17
        String codeSensitiveLeveraging = p_request
                .getParameter("codeSensitiveLeveraging");
        String codeDiffP = p_request.getParameter("codeDiffPenalty");
        long codeDiffPenalty = -1;
        if (codeDiffP != null && codeDiffP.length() > 0)
        {
            codeDiffPenalty = Long.parseLong(codeDiffP);
        }
        if (codeSensitiveLeveraging == null)
        {
            tmProfile.setIsCodeSensitiveLeveraging(false);
        }
        else if (codeSensitiveLeveraging.equals("true"))
        {
            tmProfile.setIsCodeSensitiveLeveraging(true);
        }
        tmProfile.setCodeDifferencePenalty(codeDiffPenalty);
        // 18
        String multiLingualLeveraging = p_request
                .getParameter("multiLingualLeveraging");
        if (multiLingualLeveraging == null)
        {
            tmProfile.setIsMultiLingualLeveraging(false);
        }
        else if (multiLingualLeveraging.equals("true"))
        {
            tmProfile.setIsMultiLingualLeveraging(true);
        }
        // 19 & 20
        String multExactMatches = p_request.getParameter("multEM");
        if (multExactMatches
                .equals(TranslationMemoryProfile.LATEST_EXACT_MATCH))
        {
            tmProfile
                    .setMultipleExactMatches(TranslationMemoryProfile.LATEST_EXACT_MATCH);
        }
        else if (multExactMatches
                .equals(TranslationMemoryProfile.DEMOTED_EXACT_MATCH))
        {
            tmProfile
                    .setMultipleExactMatches(TranslationMemoryProfile.DEMOTED_EXACT_MATCH);
        }
        else if (multExactMatches
                .equals(TranslationMemoryProfile.OLDEST_EXACT_MATCH))
        {
            tmProfile
                    .setMultipleExactMatches(TranslationMemoryProfile.OLDEST_EXACT_MATCH);
        }
        String multDiffPenalty = p_request.getParameter("multDiffPenalty");
        long mDiffPenalty = 1;
        if (multDiffPenalty != null && multDiffPenalty.length() > 0)
        {
            mDiffPenalty = Long.parseLong(multDiffPenalty);
            tmProfile.setMultipleExactMatchPenalty(mDiffPenalty);
        }
        // 21
        String fuzzyMatchThreshold = p_request
                .getParameter("fuzzyMatchThreshold");
        if (fuzzyMatchThreshold != null && fuzzyMatchThreshold.length() > 0)
        {
            long fmThreshold = Long.parseLong(fuzzyMatchThreshold);
            tmProfile.setFuzzyMatchThreshold(fmThreshold);
        }
        // 22
        String numberOfMatches = p_request.getParameter("numberOfMatches");
        if (numberOfMatches != null && numberOfMatches.length() > 0)
        {
            long numMatches = Long.parseLong(numberOfMatches);
            tmProfile.setNumberOfMatchesReturned(numMatches);
        }
        // 23
        String latestMatch = p_request.getParameter("latestMatchForReimport");
        if (latestMatch == null)
        {
            tmProfile.setIsLatestMatchForReimport(false);
        }
        else if (latestMatch.equals("true"))
        {
            tmProfile.setIsLatestMatchForReimport(true);
        }
        // 24 & 25
        String typeInLevReimp = p_request
                .getParameter("typeSensitiveLeveragingReimport");
        String typeDiffReimpP = p_request
                .getParameter("typeDiffPenaltyReimport");
        long typeDiffPenaltyReimp = -1;
        if (typeDiffReimpP != null && typeDiffReimpP.length() > 0)
        {
            typeDiffPenaltyReimp = Long.parseLong(typeDiffReimpP);
        }
        if (typeInLevReimp == null)
        {
            tmProfile.setIsTypeSensitiveLeveragingForReimp(false);
        }
        else if (typeInLevReimp.equals("true"))
        {
            tmProfile.setIsTypeSensitiveLeveragingForReimp(true);
        }
        tmProfile.setTypeDifferencePenaltyForReimp(typeDiffPenaltyReimp);
        // 26 & 27
        String multLGEM = p_request.getParameter("multLGEM");
        String multLGEMP = p_request.getParameter("multMatchesPenaltyReimport");
        long multMatchesPenalty = -1;
        if (multLGEMP != null && multLGEMP.length() > 0)
        {
            multMatchesPenalty = Long.parseLong(multLGEMP);
        }
        if (multLGEM == null)
        {
            tmProfile.setIsMultipleMatchesForReimp(false);
        }
        else if (multLGEM.equals("true"))
        {
            tmProfile.setIsMultipleMatchesForReimp(true);
        }
        tmProfile.setMultipleMatchesPenalty(multMatchesPenalty);

        // dynamic leverage from gold TM
        String dynLevValue = p_request
                .getParameter(TMProfileConstants.DYN_LEV_GOLD);
        if (dynLevValue != null && dynLevValue.equals("true"))
        {
            tmProfile.setDynLevFromGoldTm(true);
        }
        else
        {
            tmProfile.setDynLevFromGoldTm(false);
        }

        // dynamic leverage from in-progress TM
        dynLevValue = p_request
                .getParameter(TMProfileConstants.DYN_LEV_IN_PROGRESS);
        if (dynLevValue != null && dynLevValue.equals("true"))
        {
            tmProfile.setDynLevFromInProgressTm(true);
        }
        else
        {
            tmProfile.setDynLevFromInProgressTm(false);
        }

        // dynamic leverage from population TM
        dynLevValue = p_request
                .getParameter(TMProfileConstants.DYN_LEV_POPULATION);
        if (dynLevValue != null && dynLevValue.equals("true"))
        {
            tmProfile.setDynLevFromPopulationTm(true);
        }
        else
        {
            tmProfile.setDynLevFromPopulationTm(false);
        }

        // dynamic leverage from reference TM
        dynLevValue = p_request
                .getParameter(TMProfileConstants.DYN_LEV_REFERENCE);
        if (dynLevValue != null && dynLevValue.equals("true"))
        {
            tmProfile.setDynLevFromReferenceTm(true);
        }
        else
        {
            tmProfile.setDynLevFromReferenceTm(false);
        }
        String isMatchPercentage = p_request
                .getParameter(TMProfileConstants.MATCH_PERCENTAGE);
        if (isMatchPercentage == null)
        {
            tmProfile.setMatchPercentage(false);
        }
        else if ("true".equals(isMatchPercentage))
        {
            tmProfile.setMatchPercentage(true);
        }

        String isTmProcendence = p_request
                .getParameter(TMProfileConstants.TM_PROCENDENCE);
        if (isTmProcendence == null)
        {
            tmProfile.setTmProcendence(false);
        }
        else if ("true".equals(isTmProcendence))
        {
            tmProfile.setTmProcendence(true);
        }

        // set default MT options
        if (p_requestType.equals("NEW"))
        {
            tmProfile.setMtEngine(TMProfileConstants.MT_ENGINE_GOOGLE);
            tmProfile.setOverrideNonExactMatches(false);
            tmProfile.setAutoCommitToTM(false);
            tmProfile.setShowInEditor(false);
            tmProfile.setPtsurl("");
        }

        // set auto repair placeholder
        String autoRepair = p_request
                .getParameter(TMProfileConstants.AUTO_REPAIR);
        tmProfile.setAutoRepair("true".equalsIgnoreCase(autoRepair));

        return tmProfile;
    }
    
    /**
     * Set common options for machine translation engines.
     * 
     * @param p_request
     * @param p_tmProfile
     * 
     * @return
     */
    private TranslationMemoryProfile setMTCommonOptions(
            HttpServletRequest p_request, TranslationMemoryProfile p_tmProfile)
    {
        // MT engine
        String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
        p_tmProfile.setMtEngine(engine);
        // MT override non-exact matches under threshold
        String overrideNonExactMatches = p_request
                .getParameter(TMProfileConstants.MT_OVERRIDE_MATCHES);
        if (overrideNonExactMatches == null
                || !"on".equals(overrideNonExactMatches))
        {
            p_tmProfile.setOverrideNonExactMatches(false);
        }
        else
        {
            p_tmProfile.setOverrideNonExactMatches(true);
        }
        // auto commit to TM
        String autoCommitToTM = p_request
                .getParameter(TMProfileConstants.MT_AUTOCOMMIT_TO_TM);
        if (autoCommitToTM == null || !"on".equals(autoCommitToTM))
        {
            p_tmProfile.setAutoCommitToTM(false);
        }
        else
        {
            p_tmProfile.setAutoCommitToTM(true);
        }
        // isMtSensitiveLeveraging
        String isMtSensitiveLeveraging = p_request.getParameter("mtLeveraging");
        if (isMtSensitiveLeveraging == null
                || !"on".equals(isMtSensitiveLeveraging))
        {
            p_tmProfile.setIsMTSensitiveLeveraging(false);
        }
        else
        {
            p_tmProfile.setIsMTSensitiveLeveraging(true);
        }
        // MtSensitivePenalty
        String mtSensitivePenalty = p_request
                .getParameter("mtSensitivePenalty");
        long long_mtSensitivePenalty = 1;
        try
        {
            long_mtSensitivePenalty = Long.parseLong(mtSensitivePenalty);
            // if "MT-sensitive Leveraging" is not checked,mt penalty is set to 1.
            if (!p_tmProfile.getIsMTSensitiveLeveraging()
                    && (long_mtSensitivePenalty > 100 || long_mtSensitivePenalty < 1))
            {
                long_mtSensitivePenalty = 1;
            }
        }
        catch (Exception ex)
        {
        }
        p_tmProfile.setMtSensitivePenalty(long_mtSensitivePenalty);

        // show in segment editor
        String showInEditor = p_request
                .getParameter(TMProfileConstants.MT_SHOW_IN_EDITOR);
        if (showInEditor == null || !"on".equals(showInEditor))
        {
            p_tmProfile.setShowInEditor(false);
        }
        else
        {
            p_tmProfile.setShowInEditor(true);
        }
        
        return p_tmProfile;
    }
    
    /**
     * Clear all invalid data for PROMT and Asia Online.
     */
    private void clearInvalidPromtAndAOSettings()
    {
        try 
        {
            String sql = "delete from tm_profile_ao_info where tm_profile_id is null";
            HibernateUtil.executeSql(sql);
            
            sql = "delete from tm_profile_promt_info where tm_profile_id is null";
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            
        }
    }

}

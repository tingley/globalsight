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
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

//import javax.jms.JMSException;
//import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.TMPAttributeManager;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.projecthandler.AsiaOnlineLP2DomainInfo;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProMTInfo;
import com.globalsight.everest.projecthandler.TMProfileMTInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TMProfileComparator;
//import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.asiaOnline.DomainCombination;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

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
        if (TMProfileConstants.SAVE_ACTION.equals(action))
        {
            if (tmProfile == null)
            {
                tmProfile = parseHTTPRequest(p_request, "NEW");

                // Check if the TM profile with the same name has existed,
                // otherwise do not save it.
                boolean ifSave = checkTmProfileNameExisted(tmProfile.getName());
                if (ifSave)
                {
                    String tmpAttributes = p_request.getParameter("tmpAttributes");
                    TMPAttributeManager.setTMPAttributes(tmProfile, tmpAttributes);
                    TMProfileHandlerHelper.saveTMProfile(tmProfile);
                    clearSessionExceptTableInfo(sess, TMP_KEY);
                    saveRelationShipWithSR(p_request, tmProfile);
                }
            }
            else
            {
                tmProfile = parseHTTPRequest(p_request, "MODIFY");
//                boolean orderchanged = tmProfile.tmOrderChanged();
                String tmpAttributes = p_request.getParameter("tmpAttributes");
                TMPAttributeManager.setTMPAttributes(tmProfile, tmpAttributes);
                TMProfileHandlerHelper.saveTMProfile(tmProfile);
                clearSessionExceptTableInfo(sess, TMP_KEY);
//                if (orderchanged)
//                {
//                    ArrayList msg = new ArrayList();
//                    msg.add(tmProfile.getProjectTMsToLeverageFrom());
//                    msg.add(Long.toString(tmProfile.getId()));
//                    String currentCompanyId = CompanyThreadLocal.getInstance()
//                            .getValue();
//                    msg.add(currentCompanyId);
//                    try
//                    {
//                        JmsHelper.sendMessageToQueue(msg,
//                                JmsHelper.JMS_UPDATE_lEVERAGE_MATCH_QUEUE);
//                    }
//                    catch (JMSException e)
//                    {
//                        CATEGORY.error(e.getMessage(), e);
//                        throw new EnvoyServletException(e);
//                    }
//                    catch (NamingException e)
//                    {
//                        CATEGORY.error(e.getMessage(), e);
//                        throw new EnvoyServletException(e);
//                    }
//                }

                saveRelationShipWithSR(p_request, tmProfile);
            }
        }
        else if (TMProfileConstants.CANCEL_ACTION.equals(action))
        {
            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (TMProfileConstants.CANCEL_MT_OPTIONS_ACTION.equals(action))
        {
            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (TMProfileConstants.SAVE_MT_OPTIONS_ACTION.equals(action))
        {
            String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
            if (engine != null && !"asia_online".equalsIgnoreCase(engine))
            {
                setMTCommonOptions(p_request, tmProfile);
            }

            // For "ProMT"
            if ("promt".equals(engine.toLowerCase().trim()))
            {
            	setPromtParams(p_request, tmProfile);
            	
            	TMProfileHandlerHelper.saveTMProfile(tmProfile);
            }
            // For MS Translator
            else if ("ms_translator".equals(engine.toLowerCase().trim()))
            {
            	setMsMtParams(p_request, tmProfile);

            	TMProfileHandlerHelper.saveTMProfile(tmProfile);
            }
            // for Safaba
            else if ("safaba".equals(engine.toLowerCase().trim())) 
            {
            	setSafabaParams(p_request, tmProfile);
            }
            // For Asia Online
            else if ("asia_online".equals(engine.toLowerCase().trim()))
            {
            	setAOParams(p_request, tmProfile);

                TMProfileHandlerHelper.saveTMProfile(tmProfile);
            }

            clearInvalidPromtAndAOSettings();

            clearSessionExceptTableInfo(sess, TMP_KEY);
        }
        else if (TMProfileConstants.REMOVE_ACTION.equals(action)) 
        {
        	removeTmProfile(p_request, p_response);
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

    /**
     * Select all tm profiles that should be displayed
     */
	@SuppressWarnings("unchecked")
	private void selectTMProfilesForDisplay(HttpServletRequest p_request,
            HttpSession p_session) throws ServletException, IOException,
            EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        List<TranslationMemoryProfile> tmProfiles = null;
        try
        {
            tmProfiles = TMProfileHandlerHelper.getAllTMProfiles();

            // Filter data by name, storage TM, company ...
            filterTmProfilesByName(p_request, tmProfiles);
            filterTMProfilesByStorageTm(p_request, tmProfiles);
            filterTmProfilesByCompany(p_request, tmProfiles);

            // num_per_page
            determineNumPerPage(p_request);

            setTableNavigation(p_request, p_session, tmProfiles,
                    new TMProfileComparator(uiLocale), num_per_page, TMPS_LIST,
                    TMP_KEY);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    /**
     * Filter TM Profiles by name.
     */
	private void filterTmProfilesByName(HttpServletRequest p_request,
			List<TranslationMemoryProfile> tmProfiles)
    {
		// Decide the "name" first.
		SessionManager sessionMgr = getSessionManager(p_request);
        String name = p_request.getParameter(TMProfileConstants.FILTER_NAME);
        if (name == null)
        {
        	name = (String) sessionMgr.getAttribute(TMProfileConstants.FILTER_NAME);
        }
        if (name == null)
        {
            name = "";
        }
        sessionMgr.setAttribute(TMProfileConstants.FILTER_NAME, name.trim());

        // Filter by name
        if (!StringUtil.isEmpty(name))
        {
            for (Iterator tmpIt = tmProfiles.iterator(); tmpIt.hasNext();)
            {
                TranslationMemoryProfile tmp = (TranslationMemoryProfile) tmpIt.next();
                String tmpName = tmp.getName().toLowerCase();
                if (tmpName.indexOf(name.trim().toLowerCase()) == -1)
                {
                    tmpIt.remove();
                }
            }
        }
    }

    /**
     * Filter TM Profiles by storage TM.
     */
	private void filterTMProfilesByStorageTm(HttpServletRequest p_request,
			List<TranslationMemoryProfile> tmProfiles)
	{
		// Decide the "storage TM name" first.
		SessionManager sessionMgr = getSessionManager(p_request);
		String storageTmName = p_request
				.getParameter(TMProfileConstants.FILTER_STORAGE_TM);
        if (storageTmName == null)
        {
			storageTmName = (String) sessionMgr
					.getAttribute(TMProfileConstants.FILTER_STORAGE_TM);
        }
        if (storageTmName == null)
        {
        	storageTmName = "";
        }
        sessionMgr.setAttribute(TMProfileConstants.FILTER_STORAGE_TM, storageTmName.trim());

        // Filter by storage TM name
        if (!StringUtil.isEmpty(storageTmName))
        {
            for (Iterator tmpIt = tmProfiles.iterator(); tmpIt.hasNext();)
            {
                TranslationMemoryProfile tmp =
                		(TranslationMemoryProfile) tmpIt.next();
                long storageTmId = tmp.getProjectTmIdForSave();
                String loopStorageTmName = "";
                try {
					loopStorageTmName = ServerProxy.getProjectHandler()
							.getProjectTMById(storageTmId, false).getName();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (loopStorageTmName.toLowerCase().indexOf(
						storageTmName.trim().toLowerCase()) == -1)
                {
                    tmpIt.remove();
                }
            }
        }
	}

    /**
     * Filter TM Profiles by company.
     */
	private void filterTmProfilesByCompany(HttpServletRequest p_request,
			List<TranslationMemoryProfile> tmProfiles)
	{
		// Decide the "name" first.
		SessionManager sessionMgr = getSessionManager(p_request);
		String companyName = p_request
				.getParameter(TMProfileConstants.FILTER_COMPANY_NAME);
        if (companyName == null)
        {
			companyName = (String) sessionMgr
					.getAttribute(TMProfileConstants.FILTER_COMPANY_NAME);
        }
        if (companyName == null)
        {
        	companyName = "";
        }
		sessionMgr.setAttribute(TMProfileConstants.FILTER_COMPANY_NAME,
				companyName.trim());

        // Filter by name
        if (!StringUtil.isEmpty(companyName))
        {
            String sql = "SELECT tmp.*  FROM tm_profile tmp, project_tm tm, company com "
            		+ " WHERE tmp.PROJECT_TM_ID_FOR_SAVE = tm.ID "
            	    + " AND tm.COMPANY_ID = com.ID "
            		+ " AND com.NAME LIKE '%" + companyName + "%'";
			List<TranslationMemoryProfile> tmpList2 = HibernateUtil
					.searchWithSql(TranslationMemoryProfile.class, sql);

            for (Iterator tmpIt = tmProfiles.iterator(); tmpIt.hasNext();)
            {
                TranslationMemoryProfile tmp = (TranslationMemoryProfile) tmpIt.next();
                if (!tmpList2.contains(tmp))
                {
                    tmpIt.remove();
                }
            }
        }
	}

	private void determineNumPerPage(HttpServletRequest request)
	{
		SessionManager sessionMgr = getSessionManager(request);
        String tmProfileNumPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(tmProfileNumPerPage))
        {
        	tmProfileNumPerPage = (String) sessionMgr.getAttribute("tmProfileNumPerPage");
        }
 
        if (tmProfileNumPerPage != null){
        	sessionMgr.setAttribute("tmProfileNumPerPage", tmProfileNumPerPage.trim());
            if ("all".equalsIgnoreCase(tmProfileNumPerPage))
            {
            	num_per_page = Integer.MAX_VALUE;
            }
            else
            {
                try {
                	num_per_page = Integer.parseInt(tmProfileNumPerPage);
                } catch (NumberFormatException ignore){
                	num_per_page = 10;
                }
            }
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
            tmProfile.setMtEngine(TMProfileConstants.MT_ENGINE_MSTRANSLATOR);
            tmProfile.setUseMT(false);
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
     * Set common options for machine translation engines into TM profile.
     */
	private void setMTCommonOptions(HttpServletRequest p_request,
			TranslationMemoryProfile p_tmProfile)
    {
        // MT engine
        String engine = p_request.getParameter(TMProfileConstants.MT_ENGINE);
        p_tmProfile.setMtEngine(engine);
        // User MT
        String useMT = p_request.getParameter(TMProfileConstants.MT_USE_MT);
        if (useMT == null || !"on".equals(useMT))
        {
            p_tmProfile.setUseMT(false);
        }
        else
        {
            p_tmProfile.setUseMT(true);
        }

        // MtConfidenceScore
        String mtConfidenceScore = p_request.getParameter("mtConfidenceScore");
        long long_mtConfidenceScore = 0;
        try {
        	long_mtConfidenceScore = Long.parseLong(mtConfidenceScore);
        	if (long_mtConfidenceScore < 0 || long_mtConfidenceScore > 100) {
        		long_mtConfidenceScore = 0;
        	}
        } catch (Exception ex) {

        }
        p_tmProfile.setMtConfidenceScore(long_mtConfidenceScore);

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
    
    /**
     * Check if the TM profile name has been existed in system.
     */
    private boolean checkTmProfileNameExisted(String p_tmProfileName)
    {
    	boolean result = true;
        try
        {
			Collection allTMProfiles =
					ServerProxy.getProjectHandler().getAllTMProfiles();
            if (allTMProfiles != null && allTMProfiles.size() > 0)
            {
            	for (Iterator it = allTMProfiles.iterator(); it.hasNext();)
                {
                    TranslationMemoryProfile innerTmProfile =
                    		(TranslationMemoryProfile) it.next();
                    String tmProfileName = innerTmProfile.getName();
					if (tmProfileName != null
							&& tmProfileName.equals(p_tmProfileName))
                    {
                        result = false;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            result = false;
        }

        return result;
    }

    /**
     * Set Promt specified parameters into TM profile object for save.
     */
	private void setPromtParams(HttpServletRequest p_request,
			TranslationMemoryProfile tmProfile)
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
        if (TMProfileHandlerHelper.checkPassword(ptsPassword))
        {
            tmProfile.setPtsPassword(ptsPassword);
        }
        // set pts url flag
        String ptsUrlFlag = p_request.getParameter(TMProfileConstants.MT_PTS_URL_FLAG);
        tmProfile.setPtsUrlFlag(ptsUrlFlag);

		HashMap dirMap = (HashMap) getSessionManager(p_request).getAttribute(
				"directionsMap");
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

    /**
     * Set MS MT specified parameters into TM profile object for save.
     */
	private void setMsMtParams(HttpServletRequest p_request,
			TranslationMemoryProfile tmProfile)
	{
		String url = p_request.getParameter(TMProfileConstants.MT_MS_URL);
		String url_flag = p_request
				.getParameter(TMProfileConstants.MT_MS_URL_FLAG);
		String category = p_request
				.getParameter(TMProfileConstants.MT_MS_CATEGORY);
		String clientId = p_request
				.getParameter(TMProfileConstants.MT_MS_CLIENT_ID);
		String clientSecret = p_request
				.getParameter(TMProfileConstants.MT_MS_CLIENT_SECRET);

    	if (url != null && !"".equals(url.trim()))
    	{
    		tmProfile.setMsMTUrl(url.trim());
    	}

    	if (StringUtils.isNotEmpty(clientId))
        {
            tmProfile.setMsMTClientID(clientId);
        }

    	if (StringUtils.isNotEmpty(clientSecret)
                && TMProfileHandlerHelper.checkPassword(clientSecret))
        {
            tmProfile.setMsMTClientSecret(clientSecret);
        }

        if (url_flag != null && !"".equals(url_flag)
				&& !"null".equalsIgnoreCase(url_flag)) 
    	{
    		tmProfile.setMsMTUrlFlag(url_flag);
    	}

		if (category != null && !category.equals(""))
    	{
    	    tmProfile.setMsMTCategory(category);
    	}
	}

    /**
     * Set Safaba specified parameters into TM profile object for save.
     */
	private void setSafabaParams(HttpServletRequest p_request,
			TranslationMemoryProfile tmProfile)
	{
		String safaHost = p_request
				.getParameter(TMProfileConstants.MT_SAFA_HOST);
		String safaPort = p_request
				.getParameter(TMProfileConstants.MT_SAFA_PORT);
		String safaCompanyName = p_request
				.getParameter(TMProfileConstants.MT_SAFA_COMPANY_NAME);
		String safaClient = p_request
				.getParameter(TMProfileConstants.MT_SAFA_CLIENT);
		String safaPassword = p_request
				.getParameter(TMProfileConstants.MT_SAFA_PASSWORD);

		if (StringUtils.isEmpty(safaPassword)
				|| !TMProfileHandlerHelper.checkPassword(safaPassword))
        {
			SessionManager sessionMgr = getSessionManager(p_request);
			safaPassword = (String) sessionMgr
					.getAttribute(TMProfileConstants.MT_SAFA_PASSWORD);
            sessionMgr.removeElement(TMProfileConstants.MT_SAFA_PASSWORD);
        }
        
        List<TMProfileMTInfo> mtInfoList = new ArrayList<TMProfileMTInfo>();
        if (StringUtils.isNotEmpty(safaHost))
        {
            TMProfileMTInfo mtInfo = new TMProfileMTInfo("safaba",
                    TMProfileConstants.MT_SAFA_HOST, safaHost);
            mtInfo.setTmProfile(tmProfile);
            mtInfoList.add(mtInfo);
        }
        if (StringUtils.isNotEmpty(safaPort))
        {
            TMProfileMTInfo mtInfo = new TMProfileMTInfo("safaba",
                    TMProfileConstants.MT_SAFA_PORT, safaPort);
            mtInfo.setTmProfile(tmProfile);
            mtInfoList.add(mtInfo);
        }
        if (StringUtils.isNotEmpty(safaCompanyName))
        {
            TMProfileMTInfo mtInfo = new TMProfileMTInfo("safaba",
                    TMProfileConstants.MT_SAFA_COMPANY_NAME,
                    safaCompanyName);
            mtInfo.setTmProfile(tmProfile);
            mtInfoList.add(mtInfo);
        }
        if (StringUtils.isNotEmpty(safaPassword)
                && TMProfileHandlerHelper.checkPassword(safaPassword))
        {
            TMProfileMTInfo mtInfo = new TMProfileMTInfo("safaba",
                    TMProfileConstants.MT_SAFA_PASSWORD, safaPassword);
            mtInfo.setTmProfile(tmProfile);
            mtInfoList.add(mtInfo);
        }                
        if (StringUtils.isNotEmpty(safaClient))
        {
            TMProfileMTInfo mtInfo = new TMProfileMTInfo("safaba",
                    TMProfileConstants.MT_SAFA_CLIENT, safaClient);
            mtInfo.setTmProfile(tmProfile);
            mtInfoList.add(mtInfo);
        }

        try
        {
            TMProfileHandlerHelper.saveTMProfile(tmProfile);

            List<TMProfileMTInfo> formerData = (List<TMProfileMTInfo>) 
                    TMProfileHandlerHelper.getMtinfoByTMProfileIdAndEngine(
                            tmProfile.getId(), tmProfile.getMtEngine());
            
            // Keep safaba password, if there is no value. 
            TMProfileMTInfo newSafaPasswordMT = TMProfileHandlerHelper
                    .getMTInfo(mtInfoList, tmProfile.getId(),
                            tmProfile.getMtEngine(),
                            TMProfileConstants.MT_SAFA_PASSWORD);
            if (newSafaPasswordMT == null)
            {
                TMProfileMTInfo oldSafaPasswordMT = TMProfileHandlerHelper
                        .getMTInfo(formerData, tmProfile.getId(),
                                tmProfile.getMtEngine(),
                                TMProfileConstants.MT_SAFA_PASSWORD);
                formerData.remove(oldSafaPasswordMT);
            }
                
            HibernateUtil.delete(formerData);
            HibernateUtil.save(mtInfoList);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to save Safaba data.", e);
        }	
	}

    /**
     * Set Asian Online specified parameters into TM profile object for save.
     */
	private void setAOParams(HttpServletRequest p_request,
			TranslationMemoryProfile tmProfile)
	{
        Vector tmProfileAoInfoVec = new Vector();
        HashMap domainCombinations = (HashMap) getSessionManager(p_request)
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

	/**
	 * Remove TM profile.
	 */
	private void removeTmProfile(HttpServletRequest p_request,
			HttpServletResponse p_response) throws IOException
	{
		String id = (String) p_request
				.getParameter(TMProfileConstants.TM_PROFILE_ID);
		if (id == null
				|| p_request.getMethod().equalsIgnoreCase(
						WebAppConstants.REQUEST_METHOD_GET)) 
		{
			p_response.sendRedirect("/globalsight/ControlServlet?activityName=tmProfiles");
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
				clearSessionExceptTableInfo(p_request.getSession(false), TMP_KEY);

				SegmentationRuleFile segRuleFile = ServerProxy
						.getSegmentationRuleFilePersistenceManager()
						.getSegmentationRuleFileByTmpid(id);
				ServerProxy.getSegmentationRuleFilePersistenceManager()
						.deleteSegmentationRuleFile(segRuleFile);
			} 
			catch (Exception e) 
			{
				CATEGORY.error(e.getMessage(), e);
			}
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

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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.searchreplace.GxmlElementSubstringReplace;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.ProjectTMComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PtagStringFormatter;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;



public class TMSearchBroswerHandlerHelper
{
	private static final String DATE_EQUALS = "eq";
	private static final String DATE_NOT_EQUALS = "neq";
	private static final String DATE_GREATER_THAN = "gt";
	private static final String DATE_LESS_THAN = "lt";
	private static final String DATE_GREATER_THAN_OR_EQUALS = "gteq";
	private static final String DATE_LESS_THAN_OR_EQUALS = "lteq";
	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"MM/dd/yyyy");
    /**
     * Set permission for user
     * 
     * @param request
     * @param bundle
     */
    public static void setPermission(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean hasAddEntriesPerm = userPerms
                .getPermissionFor(Permission.TM_ADD_ENTRY);
        boolean hasDeleteEntriesPerm = userPerms
                .getPermissionFor(Permission.TM_DELETE_ENTRY);
        boolean hasEditEntriesPerm = userPerms
                .getPermissionFor(Permission.TM_EDIT_ENTRY);
        boolean hasAdvancedSearchPerm = userPerms
                .getPermissionFor(Permission.TM_SEARCH_ADVANCED);
        boolean hasTermSearchPermission = userPerms
                .getPermissionFor(Permission.TERMINOLOGY_SEARCH)
                || userPerms.getPermissionFor(Permission.ACTIVITIES_TB_SEARCH);
        request.setAttribute("hasAddEntriesPerm", hasAddEntriesPerm);
        request.setAttribute("hasDeleteEntriesPerm", hasDeleteEntriesPerm);
        request.setAttribute("hasEditEntriesPerm", hasEditEntriesPerm);
        request.setAttribute("hasAdvancedSearchPerm", hasAdvancedSearchPerm);
        request.setAttribute("hasTermSearchPermission", hasTermSearchPermission);

    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    public static void setLable(HttpServletRequest request,
            ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_tm_search");
        setLableToJsp(request, bundle, "lb_tm_search2");
        setLableToJsp(request, bundle, "lb_corpus_searchFT");
        setLableToJsp(request, bundle, "lb_corpus_searchFZ");
        setLableToJsp(request, bundle, "lb_tm_search_text");
        setLableToJsp(request, bundle, "lb_source_locale");
        setLableToJsp(request, bundle, "lb_target_locale");

        setLableToJsp(request, bundle, "lb_tm");
        setLableToJsp(request, bundle, "lb_tms");
        setLableToJsp(request, bundle, "lb_corpus_tmprofile");

        setLableToJsp(request, bundle, "lb_first");
        setLableToJsp(request, bundle, "lb_previous");
        setLableToJsp(request, bundle, "lb_next");
        setLableToJsp(request, bundle, "lb_last");

        setLableToJsp(request, bundle, "lb_source");
        setLableToJsp(request, bundle, "lb_target");
        setLableToJsp(request, bundle, "lb_tm_name");
        setLableToJsp(request, bundle, "lb_percentage");
        setLableToJsp(request, bundle, "lb_tm_search_type");
        setLableToJsp(request, bundle, "lb_tm2");
        setLableToJsp(request, bundle, "lb_tm_search_result");
        setLableToJsp(request, bundle, "lb_tm");
        setLableToJsp(request, bundle, "lb_ok");
        setLableToJsp(request, bundle, "lb_cancel");
        setLableToJsp(request, bundle, "lb_sid");
        setLableToJsp(request, bundle, "lb_company");

        setLableToJsp(request, bundle, "msg_search_results_nothing_found");
        setLableToJsp(request, bundle, "msg_tm_search_search_text");
        setLableToJsp(request, bundle, "msg_tm_search_source");
        setLableToJsp(request, bundle, "msg_tm_search_target");
        setLableToJsp(request, bundle, "msg_tm_search_tm_profile");
        setLableToJsp(request, bundle, "msg_tm_search_tms");

        setLableToJsp(request, bundle, "lb_tm_search_hint");
        setLableToJsp(request, bundle, "lb_tm_search_hint2");
        setLableToJsp(request, bundle, "lb_tm_search_display");
        setLableToJsp(request, bundle, "lb_advanced");
        setLableToJsp(request, bundle, "lb_simple");
        setLableToJsp(request, bundle, "lb_replace_with");
        setLableToJsp(request, bundle, "lb_search_in");
        setLableToJsp(request, bundle, "lb_terminology_search_entries");
        setLableToJsp(request, bundle, "msg_tm_search_text_invalid");
        setLableToJsp(request, bundle, "msg_tm_search_text_invalid2");
        setLableToJsp(request, bundle, "msg_tm_search_search_text_and_sid");
        setLableToJsp(request, bundle, "msg_tm_search_with_replace_text");
        setLableToJsp(request, bundle, "msg_tm_search_no_entry_selected");
        setLableToJsp(request, bundle, "msg_tm_search_confirm_deleted");
        setLableToJsp(request, bundle, "msg_tm_search_confirm_replaced");
        
        setLableToJsp(request, bundle, "lb_search_by");
        setLableToJsp(request, bundle, "lb_report_startDate");
        setLableToJsp(request, bundle, "lb_report_endDate");
        setLableToJsp(request, bundle, "lb_modified_on");
        setLableToJsp(request, bundle, "lb_created_on");
        setLableToJsp(request, bundle, "lb_last_usage_date");
        setLableToJsp(request, bundle, "lb_equal_to");
        setLableToJsp(request, bundle, "lb_not_equal_to");
        setLableToJsp(request, bundle, "lb_greater_than");
        setLableToJsp(request, bundle, "lb_less_than");
        setLableToJsp(request, bundle, "lb_greater_than_or_equal_to");
        setLableToJsp(request, bundle, "lb_less_than_or_equal_to");
        setLableToJsp(request, bundle, "lb_calendar_title");
        setLableToJsp(request, bundle, "lb_tm_check_date_greater_less");
        setLableToJsp(request, bundle, "lb_tm_check_date_greater_equal_less");
        setLableToJsp(request, bundle, "lb_tm_check_date_greater_less_equal");
        setLableToJsp(request, bundle, "lb_export_create_user");
        setLableToJsp(request, bundle, "lb_export_modify_user");
        setLableToJsp(request, bundle, "lb_job_id");
        setLableToJsp(request, bundle, "lb_export_tu_id");
        setLableToJsp(request, bundle, "lb_export_sid");
        setLableToJsp(request, bundle, "lb_export_regex");
    }

    /**
     * Set locales
     * 
     * @param request
     * @throws LocaleManagerException
     * @throws RemoteException
     */
    public static void setLocales(HttpServletRequest request)
            throws LocaleManagerException, RemoteException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAvailableLocales();
        SortUtil.sort(sources,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        request.setAttribute(LocalePairConstants.LOCALES,
                JsonUtil.toJson(sources));
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private static void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

    /**
     * Set TM list, according to TM access control permission and super user
     */
    public static void setTMs(HttpServletRequest request, String userId)
    {
        List<String> companies = null;
        ArrayList<ProjectTM> tmList = new ArrayList<ProjectTM>();
        String currentCompanyId = CompanyWrapper.getCurrentCompanyId();
        Company curremtCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTMAccessControl = curremtCompany
                .getEnableTMAccessControl();
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperLP = UserUtil.isSuperLP(userId);

        ProjectHandler projectHandler;
        Collection allTMs = null;
        try
        {
            projectHandler = ServerProxy.getProjectHandler();
            allTMs = projectHandler.getAllProjectTMs();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if ("1".equals(currentCompanyId))
        {
            // Current company is super company, user is super translator or
            // super administrator
            companies = new ArrayList<String>();
            if (isSuperLP)
            {
                // Get all the companies the super translator worked for
                List projectList = null;
                try
                {
                    projectList = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (projectList.size() < 1)
                {
                    // This translator works for no company
                    allTMs = new ArrayList<String>();
                }
                for (Iterator it = projectList.iterator(); it.hasNext();)
                {
                    Project pj = (Project) it.next();
                    String companyName = CompanyWrapper.getCompanyNameById(pj
                            .getCompanyId());
                    if (!companies.contains(companyName))
                    {
                        companies.add(companyName);
                    }
                }
            }
            else
            {
                // Super administrator
                String[] companyNames = CompanyWrapper.getAllCompanyNames();
                for (String name : companyNames)
                {
                    companies.add(name);
                }

            }
            SortUtil.sort(companies, new StringComparator(Locale.getDefault()));
            tmList.addAll(allTMs);
        }
        else
        {
            // sub company
            if (enableTMAccessControl && !isAdmin)
            {
                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                Iterator it = tmIdList.iterator();
                while (it.hasNext())
                {
                    ProjectTM tm = null;
                    try
                    {
                        tm = ServerProxy.getProjectHandler().getProjectTMById(
                                ((BigInteger) it.next()).longValue(), false);
                    }
                    catch (Exception e)
                    {
                        throw new EnvoyServletException(e);
                    }
                    if (isSuperPM)
                    {
                        if (String.valueOf(tm.getCompanyId()).equals(
                                currentCompanyId))
                        {
                            tmList.add(tm);
                        }
                    }
                    else
                    {
                        tmList.add(tm);
                    }
                }
            }
            else
            {
                tmList.addAll(allTMs);
            }
        }

        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        SortUtil.sort(tmList, new ProjectTMComparator(Locale.getDefault()));
        for (Iterator it = tmList.iterator(); it.hasNext();)
        {
            ProjectTM tm = (ProjectTM) it.next();
            if (tm.getIsRemoteTm())
            {
                continue;
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", tm.getId());
            map.put("name", tm.getName());
            map.put("company",
                    CompanyWrapper.getCompanyNameById(tm.getCompanyId()));
            list.add(map);
        }
        request.setAttribute("companiesForTM", JsonUtil.toJson(companies));
        request.setAttribute("tms", JsonUtil.toJson(list));
    }

    /**
     * Set TM Profiles
     * 
     * @param request
     * @return
     * @throws IOException
     * @throws NamingException
     * @throws GeneralException
     * @throws ProjectHandlerException
     */
    public static void setTMProfiles(HttpServletRequest request, String userId)
            throws Exception
    {
        String currentCompanyId = CompanyWrapper.getCurrentCompanyId();

        boolean companyFilter = false;
        List<String> companies = new ArrayList<String>();
        if ("1".equals(currentCompanyId))
        {
            companyFilter = true;
            boolean isSuperLP = UserUtil.isSuperLP(userId);
            if (isSuperLP)
            {
                // Get all the companies the super translator worked for
                List projectList = null;
                try
                {
                    projectList = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (projectList.size() > 0)
                {
                    for (Iterator it = projectList.iterator(); it.hasNext();)
                    {
                        Project pj = (Project) it.next();
                        String companyName = CompanyWrapper
                                .getCompanyNameById(pj.getCompanyId());
                        if (!companies.contains(companyName))
                        {
                            companies.add(companyName);
                        }
                    }
                }
            }
            request.setAttribute("companiesForTMP", JsonUtil.toJson(companies));
        }
        List<TranslationMemoryProfile> tmProfiles = null;
        tmProfiles = new ArrayList(ServerProxy.getProjectHandler()
                .getAllTMProfiles());
        List<TMProfileVO> list = new ArrayList<TMProfileVO>();

        if (companyFilter)
        {
            // For super user
            for (Iterator<TranslationMemoryProfile> it = tmProfiles.iterator(); it
                    .hasNext();)
            {
                TranslationMemoryProfile tmp = it.next();
                String companyId = null;
                try
                {
                    companyId = String.valueOf(ServerProxy
                            .getProjectHandler()
                            .getProjectTMById(tmp.getProjectTmIdForSave(),
                                    false).getCompanyId());
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String companyName = CompanyWrapper
                        .getCompanyNameById(companyId);
                if (companies.contains(companyName))
                {
                    TMProfileVO tmpVO = new TMProfileVO();
                    tmpVO.setId(tmp.getId());
                    tmpVO.setName(companyName + "-" + tmp.getName());
                    list.add(tmpVO);
                }
            }
        }
        else
        {
            for (Iterator<TranslationMemoryProfile> it = tmProfiles.iterator(); it
                    .hasNext();)
            {
                TranslationMemoryProfile tmp = it.next();
                TMProfileVO tmpVO = new TMProfileVO();
                tmpVO.setId(tmp.getId());
                tmpVO.setName(tmp.getName());
                list.add(tmpVO);
            }
        }
        SortUtil.sort(list, new TMProfileVOComparator(Locale.getDefault()));
        request.setAttribute("tmProfiles", JsonUtil.toJson(list));
    }

    /**
     * Get locale of leverage from
     * 
     * @param p_request
     * @param localePairs
     * @throws GeneralException
     * @throws RemoteException
     * @throws NumberFormatException
     * @throws LocaleManagerException
     */
    private static List<String> getLeverageLocales(Locale uiLocale,
            String localeId) throws LocaleManagerException,
            NumberFormatException, RemoteException, GeneralException
    {
        GlobalSightLocale trgLocale = ServerProxy.getLocaleManager()
                .getLocaleById(Long.parseLong(localeId));
        String newTargetLangCode = trgLocale.getLanguageCode();
        Vector supportedLocales = LocProfileHandlerHelper.getSupportedLocales();
        List<String> list = new ArrayList<String>();

        // add matching cross-locales based on lang code
        for (int j = 0; j < supportedLocales.size(); j++)
        {
            String lang = ((GlobalSightLocale) supportedLocales.elementAt(j))
                    .getLanguageCode();
            if (newTargetLangCode.equals(lang))
            {
                // check special exclusionary cases
                if (!validLeverageLocale(trgLocale,
                        (GlobalSightLocale) supportedLocales.elementAt(j)))
                {
                    continue;
                }
                list.add(supportedLocales.elementAt(j).toString());
            }
        }
        return list;
    }

    /**
     * Helps to handle certain leverage exclusions
     * 
     * @param defaultTargetLocale
     * @param leverageLocale
     * @return
     */
    private static boolean validLeverageLocale(
            GlobalSightLocale defaultTargetLocale,
            GlobalSightLocale leverageLocale)
    {
        boolean result = true;

        // exclude Chinese (tiwan) if default target is Chinese (china)
        if (defaultTargetLocale.toString().equals("zh_CN")
                && leverageLocale.toString().equals("zh_TW"))
        {
            result = false;
        }

        // exclude Chinese (china) if default target is Chinese (Tiwan)
        if (defaultTargetLocale.toString().equals("zh_TW")
                && leverageLocale.toString().equals("zh_CN"))
        {
            result = false;
        }

        return result;
    }

    /**
     * Exact/Fuzzy match search
     * 
     * @param p_request
     * @param p_response
     * @param session
     * @throws Exception
     */
    public static String searchExact(HttpServletRequest request,
            String queryText, String sourceLocaleId, String targetLocaleId,
            String tmpId, Locale uiLocale, String maxEntriesPerPageStr,
            String searchIn, String replaceText) throws Exception
    {
        boolean searchInSource = "source".equals(searchIn);
        boolean advancedSearch = "true".equals((String) request
                .getParameter("advancedSearch"));

        HashMap filterMap = getFilterMap(request,advancedSearch);
        String attributeName = null;
		String attributeValue = null;
		if (advancedSearch)
		{
			attributeName = (String) request.getParameter("attributeName");
			attributeValue = (String) request.getParameter("attributeValue");
		}
        LocaleManager lm = ServerProxy.getLocaleManager();
        GlobalSightLocale sourceGSL = lm.getLocaleById(Long
                .parseLong(sourceLocaleId));
        GlobalSightLocale targetGSL = lm.getLocaleById(Long
                .parseLong(targetLocaleId));

        // Prepare target locale according to leverage from
        List<GlobalSightLocale> searchTrgLocales = new ArrayList<GlobalSightLocale>();
        searchTrgLocales.add(searchInSource ? targetGSL : sourceGSL);
        LeveragingLocales searchLevLocales = new LeveragingLocales();

        Set<GlobalSightLocale> set = new HashSet<GlobalSightLocale>();
        set.add(searchInSource ? targetGSL : sourceGSL);
        
        searchLevLocales.setLeveragingLocale(searchInSource ? targetGSL
                : sourceGSL, set);

        Long tmProfileId = Long.parseLong(tmpId);
        TranslationMemoryProfile tmp = ServerProxy.getProjectHandler()
                .getTMProfileById(tmProfileId.longValue(), false);
        LeverageOptions levOptions = new LeverageOptions(tmp, searchLevLocales);
        // fix for GBS-2448, user could search target locale in TM Search Page
        levOptions.setFromTMSearchPage(true);

        String segment = "<segment>" + EditUtil.encodeHtmlEntities(queryText)
                + "</segment>";
        PageTmTu tu = new PageTmTu(-1, -1, "plaintext", "text", true);
		GlobalSightLocale searchFromLocale = searchInSource ? sourceGSL : targetGSL;
        PageTmTuv tuv = new PageTmTuv(-1, segment, searchFromLocale);
        tuv.setTu(tu);
        tuv.setExactMatchKey();
        tu.addTuv(tuv);
        // Do leverage
        LeverageDataCenter leverageDataCenter = LingServerProxy
                .getTmCoreManager().leverageSegments(
                        Collections.singletonList(tuv), searchFromLocale,
                        searchTrgLocales, levOptions);
        Iterator<LeverageMatches> itLeverageMatches = leverageDataCenter
                .leverageResultIterator();
        List<Map<String, Object>> leverageResult = new ArrayList<Map<String, Object>>();
        Set<Long> attributeFilterTuIds = new HashSet<Long>();
        boolean attributeFilter = false;
        if(advancedSearch && StringUtil.isNotEmpty(attributeName) && StringUtil.isNotEmpty(attributeValue))
        {
        	attributeFilter = true;
        	attributeFilterTuIds = getAttributeFilterTuIds(getTuIds(itLeverageMatches), attributeName, attributeValue);
        }
        long jobId = -1; // -1 is fine here
        Set<String> jobIdSet = (Set<String>) filterMap.get("jobIds");
        while (itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = itLeverageMatches.next();

            // walk through all target locale in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator(jobId);
            while (itLocales.hasNext())
            {
                GlobalSightLocale tLocale = (GlobalSightLocale) itLocales
                        .next();

                // walk through all matches in the locale
                Iterator itMatch = levMatches.matchIterator(tLocale, jobId);
                while (itMatch.hasNext())
                {
                    LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
                    if(attributeFilter)
                    {
                    	if(attributeFilterTuIds.size() == 0 || 
                    			!attributeFilterTuIds.contains(matchedTuv.getTu().getId()))
                    	{
                    		continue;
                    	}
                    }
                    
                  //0001181: TM Search: Job ID search is incorrect
					if (jobIdSet != null && jobIdSet.size() > 0)
					{
						if (!jobIdSet.contains(String.valueOf(matchedTuv
								.getJobId())))
						{
							continue;
						}
					}
                    
            		if (advancedSearch && !searchInSource)
    				{
    					if (!searchFilter(filterMap, matchedTuv))
    						continue;
    				}
                    long score = new Float(matchedTuv.getScore()).longValue();
                    if (score < tmp.getFuzzyMatchThreshold())
                    {
                        // if match score less than thres hold, do not display
                        continue;
                    }

                    BaseTmTuv sourceTuv = matchedTuv.getSourceTuv();
               		if (advancedSearch && searchInSource)
    				{
    					if (!searchFilter(filterMap, sourceTuv))
    						continue;
    				}
                    String scoreStr = StringUtil.formatPCT(score);
                    long tuId = matchedTuv.getTu().getId();
                    long tmId = matchedTuv.getTu().getTmId();
                    long sourceTuvId;
                    long targetTuvId;
                    String sourceLocale;
                    String targetLocale;
                    String sid;
                    String tmName = ServerProxy.getProjectHandler()
                            .getProjectTMById(tmId, false).getName();

                    Map<String, String> formattedSource;
                    Map<String, String> formattedTarget;
                    if (searchInSource)
                    {
                        // search in source
                        sourceTuvId = sourceTuv.getId();
                        sourceLocale = sourceTuv.getLocale().toString();
                        targetTuvId = matchedTuv.getId();
                        targetLocale = matchedTuv.getLocale().toString();
                        sid = matchedTuv.getSid();
                        if (null == sid)
                        {
                            sid = "N/A";
                        }
                        formattedSource = getFormattedSegment(
                                EditUtil.encodeHtmlEntities(queryText),
                                EditUtil.encodeHtmlEntities(replaceText),
                                sourceTuv);
                        formattedTarget = getFormattedSegment(null, null,
                                matchedTuv);
                    }
                    else
                    {
                        // search in target
                        sourceTuvId = matchedTuv.getId();
                        sourceLocale = matchedTuv.getLocale().toString();
                        targetTuvId = sourceTuv.getId();
                        targetLocale = sourceTuv.getLocale().toString();
                        sid = sourceTuv.getSid();
                        if (null == sid)
                        {
                            sid = "N/A";
                        }
                        formattedSource = getFormattedSegment(null, null,
                                matchedTuv);
                        formattedTarget = getFormattedSegment(
                                EditUtil.encodeHtmlEntities(queryText),
                                EditUtil.encodeHtmlEntities(replaceText),
                                sourceTuv);
                    }

                    Map<String, Object> matchMap = new HashMap<String, Object>();
                    matchMap.put("sid", sid);
                    matchMap.put("source", formattedSource);
                    matchMap.put("target", formattedTarget);
                    matchMap.put("score", scoreStr);
                    matchMap.put("tm", tmName);
                    matchMap.put("tmId", tmId);
                    matchMap.put("tuId", tuId);
                    matchMap.put("sourceTuvId", sourceTuvId);
                    matchMap.put("targetTuvId", targetTuvId);
                    matchMap.put("sourceLocale", sourceLocale);
                    matchMap.put("targetLocale", targetLocale);
                    leverageResult.add(matchMap);
                }
            }
        }

        // Set the search result to session
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("searchResult", leverageResult);
        // Get the displayed result according records number and the max number
        // per pages
        Map<String, Object> temp = getDisplayResult(leverageResult, 1,
                maxEntriesPerPageStr);
        String jsonStr = getJsonStr(JsonUtil.toJson(temp));
        return jsonStr;
    }
	
    /**
     * Full text search
     * 
     * @return
     * @throws Exception
     */
    public static String searchFullText(HttpServletRequest request,
            String searchText, String sourceLocaleId, String targetLocaleId,
            String tms, Locale uiLocale, String maxEntriesPerPageStr,
            String searchIn, String replaceText) throws Exception
    {
        boolean searchInSource = "source".equals(searchIn);
        boolean advancedSearch = "true".equals((String) request
                .getParameter("advancedSearch"));
        HashMap filterMap = getFilterMap(request,advancedSearch);
        String attributeName = null;
		String attributeValue = null;
		if (advancedSearch)
		{
			attributeName = (String) request.getParameter("attributeName");
			attributeValue = (String) request.getParameter("attributeValue");
		}
		
        LocaleManager lm = ServerProxy.getLocaleManager();
        GlobalSightLocale sourceGSL = lm.getLocaleById(Long
                .parseLong(sourceLocaleId));
        GlobalSightLocale targetGSL = lm.getLocaleById(Long
                .parseLong(targetLocaleId));

        // get all selected TMS
        ArrayList<Tm> tmList = new ArrayList<Tm>();
        String[] tmsArray = tms.split(",");
        for (String tm : tmsArray)
        {
            long tmId = Long.parseLong(tm);
            tmList.add(ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false));
        }

        // do search
        TmCoreManager mgr = LingServerProxy.getTmCoreManager();
        List<TMidTUid> queryResult = mgr.tmConcordanceQuery(tmList, searchText,
                searchInSource ? sourceGSL : targetGSL,
                searchInSource ? targetGSL : sourceGSL, null);
        // Get all TUS by queryResult, then get all needed properties
        List<SegmentTmTu> tus = LingServerProxy.getTmCoreManager()
                .getSegmentsById(queryResult);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String findText = EditUtil.encodeHtmlEntities(searchText);
        replaceText = EditUtil.encodeHtmlEntities(replaceText);
        Set<Long> attributeFilterTuIds = new HashSet<Long>();
        boolean attributeFilter = false;
        if(advancedSearch && StringUtil.isNotEmpty(attributeName) && StringUtil.isNotEmpty(attributeValue))
        {
        	attributeFilter = true;
        	attributeFilterTuIds = getAttributeFilterTuIds(getTuIds(tus), attributeName, attributeValue);
        }
        Set<String> jobIdSet = (Set<String>) filterMap.get("jobIds");
        for (int i = 0, max = tus.size(); i < max; i++)
        {
            try
            {
                SegmentTmTu tu = tus.get(i);
                if (tu == null)
                {
                    continue;
                }
                long tuId = tu.getId();
                if(attributeFilter)
                {
                	if( attributeFilterTuIds.size() ==0 || !attributeFilterTuIds.contains(tuId))
                	{
                		continue;
                	}
                }
                BaseTmTuv srcTuv = tu.getFirstTuv(sourceGSL);
				if (advancedSearch && searchInSource)
				{
					if (!searchFilter(filterMap, srcTuv))
						continue;
				}
                String gxml = GxmlUtil.stripRootTag(srcTuv.getSegment());

                BaseTmTuv trgTuv;
                Collection targetTuvs = tu.getTuvList(targetGSL);
                
                for (Iterator it = targetTuvs.iterator(); it.hasNext();)
                {
                    Map<String, Object> map = new HashMap<String, Object>();
                    trgTuv = (BaseTmTuv) it.next();
					if (advancedSearch && !searchInSource)
					{
						if (!searchFilter(filterMap, trgTuv))
							continue;
					}
					//0001181: TM Search: Job ID search is incorrect
					if (jobIdSet != null && jobIdSet.size() > 0)
					{
						if (!jobIdSet
								.contains(String.valueOf(trgTuv.getJobId())))
						{
							continue;
						}
					}
					
                    String sid = trgTuv.getSid();
                    long tuvId = trgTuv.getId();
                    if (null == sid)
                    {
                        sid = "N/A";
                    }

                    // get formatted source and target with highlight
                    Map<String, String> formattedSource = getFormattedSegment(
                            searchInSource ? findText : null, replaceText, srcTuv);
                    Map<String, String> formattedTarget = getFormattedSegment(
                            searchInSource ? null : findText, replaceText, trgTuv);
                    String targetLocale = trgTuv.getLocale().toString();
                    long tmId = trgTuv.getTu().getTmId();
                    String tmName = ServerProxy.getProjectHandler()
                            .getProjectTMById(tmId, false).getName();
                    map.put("sid", sid);
                    map.put("originSource", gxml);
                    map.put("source", formattedSource);
                    map.put("target", formattedTarget);
                    map.put("tm", tmName);
                    map.put("tuId", tuId);
                    map.put("sourceTuvId", srcTuv.getId());
                    map.put("targetTuvId", tuvId);
                    map.put("tmId", tmId);
                    map.put("sourceLocale", srcTuv.getLocale().toString());
                    map.put("targetLocale", targetLocale);
                    result.add(map);
                }                
            }
            catch (Exception e)
            {
                // ignore this.
            }
        }

        // Find the exact match and put the exact match record to beginning
        List<Map<String, Object>> resultExact = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> resultTemp = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> resultNew = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : result)
        {
            if (searchText.equals(map.get("originSource")))
            {
                resultExact.add(map);
            }
            else
            {
                resultTemp.add(map);
            }
        }
        resultNew.addAll(resultExact);
        resultNew.addAll(resultTemp);

        // Set the search result to session
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("searchResult", resultNew);

        // Get the displayed result according records number and the max number
        // per pages
        Map<String, Object> temp = getDisplayResult(resultNew, 1,
                maxEntriesPerPageStr);
        String jsonStr = getJsonStr(JsonUtil.toJson(temp));
        return jsonStr;
    }
    
    public static String searchFullTextBySid(HttpServletRequest request,
            String searchText, String sourceLocaleId, String targetLocaleId,
            String tms, Locale uiLocale, String maxEntriesPerPageStr,
            String searchIn, String replaceText) throws Exception
    {
		boolean searchInSource = "source".equals(searchIn);
		boolean advancedSearch = "true".equals((String) request
				.getParameter("advancedSearch"));
		HashMap filterMap = getFilterMap(request, advancedSearch);
		LocaleManager lm = ServerProxy.getLocaleManager();
		GlobalSightLocale sourceGSL = lm.getLocaleById(Long
				.parseLong(sourceLocaleId));
		GlobalSightLocale targetGSL = lm.getLocaleById(Long
				.parseLong(targetLocaleId));
		if (searchInSource)
		{
			filterMap.put("localeIds", sourceGSL.getId());
		}
		else
		{
			filterMap.put("localeIds", targetGSL.getId());
		}

		String attributeName = null;
		String attributeValue = null;
		if (advancedSearch)
		{
			attributeName = (String) request.getParameter("attributeName");
			attributeValue = (String) request.getParameter("attributeValue");
		}
		
		// get all selected TMS
		Connection conn = DbUtil.getConnection();
         String[] tmsArray = tms.split(",");
         List<TMidTUid> queryResult = new ArrayList<TMidTUid>();
		for (String tm : tmsArray)
		{
			StatementBuilder sb = new StatementBuilder();
			long tmId = Long.parseLong(tm);
			ProjectTM projectTM = ServerProxy.getProjectHandler()
					.getProjectTMById(tmId, false);
			// tm3
			if (projectTM.getTm3Id() != null)
			{
				BaseTm baseTM = TM3Util.getBaseTm(projectTM.getTm3Id());
				getTm3SqlByParamMap(sb, filterMap, baseTM, projectTM.getTm3Id());
				List<TMidTUid> tuId = getIdList(conn, sb, tmId);
				if (tuId != null && tuId.size() > 0)
				{
					queryResult.addAll(tuId);
				}
			}
			// tm2
			else
			{
				getTm2SqlByParamMap(sb, filterMap, tmId);
				List<TMidTUid> tuId = getIdList(conn, sb, tmId);
				if (tuId != null && tuId.size() > 0)
				{
					queryResult.addAll(tuId);
				}
			}
		}
         
		// Get all TUS by queryResult, then get all needed properties
        List<SegmentTmTu> tus = LingServerProxy.getTmCoreManager()
                .getSegmentsById(queryResult);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String findText = EditUtil.encodeHtmlEntities(searchText);
        replaceText = EditUtil.encodeHtmlEntities(replaceText);
        Set<Long> attributeFilterTuIds = new HashSet<Long>();
        boolean attributeFilter = false;
        if(advancedSearch && StringUtil.isNotEmpty(attributeName) && StringUtil.isNotEmpty(attributeValue))
        {
        	attributeFilter = true;
        	attributeFilterTuIds = getAttributeFilterTuIds(getTuIds(tus), attributeName, attributeValue);
        }
        
        Set<String> jobIdSet = (Set<String>) filterMap.get("jobIds");
        for (int i = 0, max = tus.size(); i < max; i++)
        {
            try
            {
                SegmentTmTu tu = tus.get(i);
                if (tu == null)
                {
                    continue;
                }
                long tuId = tu.getId();
                if(attributeFilter)
                {
                	if( attributeFilterTuIds.size() ==0 || !attributeFilterTuIds.contains(tuId))
                	{
                		continue;
                	}
                }
                BaseTmTuv srcTuv = tu.getFirstTuv(sourceGSL);
				if (advancedSearch && searchInSource)
				{
					if (!searchFilterBySid(filterMap, srcTuv))
						continue;
				}
                String gxml = GxmlUtil.stripRootTag(srcTuv.getSegment());

                BaseTmTuv trgTuv;
                Collection targetTuvs = tu.getTuvList(targetGSL);
                
                for (Iterator it = targetTuvs.iterator(); it.hasNext();)
                {
                    Map<String, Object> map = new HashMap<String, Object>();
                    trgTuv = (BaseTmTuv) it.next();
					if (advancedSearch && !searchInSource)
					{
						if (!searchFilterBySid(filterMap, trgTuv))
							continue;
					}
					//0001181: TM Search: Job ID search is incorrect
					if (jobIdSet != null && jobIdSet.size() > 0)
					{
						if (!jobIdSet
								.contains(String.valueOf(trgTuv.getJobId())))
						{
							continue;
						}
					}
					
                    String sid = trgTuv.getSid();
                    long tuvId = trgTuv.getId();
                    if (null == sid)
                    {
                        sid = "N/A";
                    }

                    // get formatted source and target with highlight
                    Map<String, String> formattedSource = getFormattedSegment(
                            searchInSource ? findText : null, replaceText, srcTuv);
                    Map<String, String> formattedTarget = getFormattedSegment(
                            searchInSource ? null : findText, replaceText, trgTuv);
                    String targetLocale = trgTuv.getLocale().toString();
                    long tmId = trgTuv.getTu().getTmId();
                    String tmName = ServerProxy.getProjectHandler()
                            .getProjectTMById(tmId, false).getName();
                    map.put("sid", sid);
                    map.put("originSource", gxml);
                    map.put("source", formattedSource);
                    map.put("target", formattedTarget);
                    map.put("tm", tmName);
                    map.put("tuId", tuId);
                    map.put("sourceTuvId", srcTuv.getId());
                    map.put("targetTuvId", tuvId);
                    map.put("tmId", tmId);
                    map.put("sourceLocale", srcTuv.getLocale().toString());
                    map.put("targetLocale", targetLocale);
                    result.add(map);
                }                
            }
            catch (Exception e)
            {
                // ignore this.
            }
        }

        // Find the exact match and put the exact match record to beginning
        List<Map<String, Object>> resultExact = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> resultTemp = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> resultNew = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : result)
        {
            if (searchText.equals(map.get("originSource")))
            {
                resultExact.add(map);
            }
            else
            {
                resultTemp.add(map);
            }
        }
        resultNew.addAll(resultExact);
        resultNew.addAll(resultTemp);

        // Set the search result to session
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("searchResult", resultNew);

        // Get the displayed result according records number and the max number
        // per pages
        Map<String, Object> temp = getDisplayResult(resultNew, 1,
                maxEntriesPerPageStr);
        String jsonStr = getJsonStr(JsonUtil.toJson(temp));
        return jsonStr;
    }

	private static String getJsonStr(String jsonStr)
	{
		char[] crArr = jsonStr.toCharArray();
		StringBuffer bufer = new StringBuffer();
		char cr;
		for (int i = 0; i < crArr.length; i++)
		{
			cr = crArr[i];
			bufer.append(cr);
			if (cr == '\\')
			{
				if (i < crArr.length - 1 && i > 0)
				{
					if (crArr[i + 1] == 'n' && crArr[i - 1] != '\\')
					{
						bufer.append(crArr[i + 1]).append("<br>");
						i++;
					}
				}
			}
		}
		return bufer.toString();
	}
    
	private static void getTm2SqlByParamMap(StatementBuilder sb,
			HashMap paramMap,long tmId)
	{
		sb.append("SELECT DISTINCT tuv.TU_ID AS tuId FROM ")
				.append(" PROJECT_TM_TU_T AS tu, ")
				.append(" PROJECT_TM_TUV_T AS tuv").append(" WHERE 1=1 ")
				.append(" AND tu.TM_ID = ?").addValue(tmId)
				.append(" AND tu.id = tuv.TU_ID ");
		sb.append(" AND tuv.sid is not null ");
		getParameterSql(sb, paramMap, "TM2");
	}
	
	private static void getTm3SqlByParamMap(StatementBuilder sb,
			HashMap paramMap, BaseTm baseTM, long tm3Id)
	{
		sb.append("SELECT DISTINCT tuv.tuId AS tuId FROM ")
				.append(baseTM.getTuvTableName()).append(" as tuv, ")
				.append(baseTM.getTuvExtTableName()).append(" AS ext,")
				.append(baseTM.getTuTableName()).append(" as tu ")
				.append(" WHERE 1 = 1").append(" AND tuv.id = ext.tuvId ")
				.append(" AND tu.id = ext.tuId ").append(" AND tuv.tmId = ?")
				.addValue(tm3Id);
		getParameterSql(sb, paramMap, "TM3");
		sb.append(" AND ext.sid IS NOT NULL ");
	}
    
    private static void getParameterSql(StatementBuilder sb,
    		HashMap paramMap,String tmType)
	{
		if (paramMap != null)
		{
			String createStartDateOption = (String) paramMap.get("createStartDateOption");
			String createEndDateOption = (String) paramMap.get("createEndDateOption");
			Date createStartDate = (Date) paramMap.get("createStartDate");
			Date createEndDate = (Date) paramMap.get("createEndDate");

			String modifyStartDateOption = (String) paramMap.get("modifyStartDateOption");
			String modifyEndDateOption = (String) paramMap.get("modifyEndDateOption");
			Date modifyStartDate = (Date) paramMap.get("modifyStartDate");
			Date modifyEndDate = (Date) paramMap.get("modifyEndDate");
			
			String lastUsageStartDateOption = (String) paramMap.get("lastUsageStartDateOption");
			String lastUsageEndDateOption = (String) paramMap.get("lastUsageEndDateOption");
			Date lastUsageStartDate = (Date) paramMap.get("lastUsageStartDate");
			Date lastUsageEndDate = (Date) paramMap.get("lastUsageEndDate");
			
			String createUser = (String) paramMap.get("createUser");
			String modifyUser = (String) paramMap.get("modifyUser");
			long localeId = (Long) paramMap.get("localeIds");
//			String jobIds = (String) paramMap.get("jobIds");
			//create date
			if (createStartDate != null)
			{
				if (createStartDateOption.equals(DATE_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate >= ? ").addValue(parseStartDate(createStartDate));
						sb.append(" AND tuv.creationDate <= ? ").addValue(parseEndDate(createStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE >= ? ").addValue(parseStartDate(createStartDate));
						sb.append(" AND tuv.CREATION_DATE <= ? ").addValue(parseEndDate(createStartDate));
					}
				}
				else if (createStartDateOption.equals(DATE_NOT_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate < ? ").addValue(parseStartDate(createStartDate));
						sb.append(" AND tuv.creationDate > ? ").addValue(parseEndDate(createStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE < ? ").addValue(parseStartDate(createStartDate));
						sb.append(" AND tuv.CREATION_DATE > ? ").addValue(parseEndDate(createStartDate));
					}
				}
				else if (createStartDateOption.equals(DATE_GREATER_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate > ? ").addValue(parseStartDate(createStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE > ? ").addValue(parseStartDate(createStartDate));
					}
				}

				else if (createStartDateOption.equals(DATE_GREATER_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate >= ? ").addValue(parseStartDate(createStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE >= ?").addValue(parseStartDate(createStartDate));
					}
				}
			}
			
			if (createEndDate != null)
			{
				if (createEndDateOption.equals(DATE_LESS_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate < ? ").addValue(parseEndDate(createEndDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE < ?").addValue(parseEndDate(createEndDate));
					}
				}
				else if (createEndDateOption.equals(DATE_LESS_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.creationDate <= ? ").addValue(parseEndDate(createEndDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.CREATION_DATE <= ?").addValue(parseEndDate(createEndDate));
					}
				}
			}
			
			// modify date
			if (modifyStartDate != null)
			{
				if (modifyStartDateOption.equals(DATE_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate >= ? ").addValue(parseStartDate(modifyStartDate));
						sb.append(" AND tuv.modifyDate <= ? ").addValue(parseEndDate(modifyStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE >= ? ").addValue(parseStartDate(modifyStartDate));
						sb.append(" AND tuv.MODIFY_DATE <= ? ").addValue(parseEndDate(modifyStartDate));
					}
				}
				else if (modifyStartDateOption.equals(DATE_NOT_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate < ? ").addValue(parseStartDate(modifyStartDate));
						sb.append(" AND tuv.modifyDate > ? ").addValue(parseEndDate(modifyStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE < ? ").addValue(parseStartDate(modifyStartDate));
						sb.append(" AND tuv.MODIFY_DATE > ? ").addValue(parseEndDate(modifyStartDate));
					}
				}
				else if (modifyStartDateOption.equals(DATE_GREATER_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate > ? ").addValue(parseStartDate(modifyStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE > ?").addValue(parseStartDate(modifyStartDate));
					}
				}
				else if (modifyStartDateOption.equals(DATE_GREATER_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate >= ? ").addValue(parseStartDate(modifyStartDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE >= ?").addValue(parseStartDate(modifyStartDate));
					}
				}
			}
			
			if (modifyEndDate != null)
			{
				if (modifyEndDateOption.equals(DATE_LESS_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate < ? ").addValue(parseEndDate(modifyEndDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE < ?").addValue(parseEndDate(modifyEndDate));
					}
				}
				else if (modifyEndDateOption.equals(DATE_LESS_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND tuv.modifyDate <= ? ").addValue(parseEndDate(modifyEndDate));
					}
					else if (tmType.equalsIgnoreCase("TM2"))
					{
						sb.append(" AND tuv.MODIFY_DATE <= ?").addValue(parseEndDate(modifyEndDate));
					}
				}
			}
			
			// last usage date
			if (lastUsageStartDate != null)
			{
				if (lastUsageStartDateOption.equals(DATE_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate >= ? ").addValue(
								parseStartDate(lastUsageStartDate));
						sb.append(" AND ext.lastUsageDate <= ? ").addValue(
								parseEndDate(lastUsageStartDate));
					}
				}
				else if (lastUsageStartDateOption.equals(DATE_NOT_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate < ? ").addValue(
								parseStartDate(lastUsageStartDate));
						sb.append(" AND ext.lastUsageDate > ? ").addValue(
								parseEndDate(lastUsageStartDate));
					}
				}
				else if (lastUsageStartDateOption.equals(DATE_GREATER_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate > ? ").addValue(
								parseStartDate(lastUsageStartDate));
					}
				}
				else if (lastUsageStartDateOption
						.equals(DATE_GREATER_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate >= ? ").addValue(
								parseStartDate(lastUsageStartDate));
					}
				}
			}

			if (lastUsageEndDate != null)
			{
				if (lastUsageEndDateOption.equals(DATE_LESS_THAN))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate < ? ").addValue(
								parseEndDate(lastUsageEndDate));
					}
				}
				else if (lastUsageEndDateOption
						.equals(DATE_LESS_THAN_OR_EQUALS))
				{
					if (tmType.equalsIgnoreCase("TM3"))
					{
						sb.append(" AND ext.lastUsageDate <= ? ").addValue(
								parseEndDate(lastUsageEndDate));
					}
				}
			}
						
			//create user
			if (StringUtil.isNotEmpty(createUser))
			{
				if (tmType.equalsIgnoreCase("TM3"))
				{
					sb.append(" AND tuv.creationUser = ? ").addValues(createUser);
				}
				else if (tmType.equalsIgnoreCase("TM2"))
				{
					sb.append(" AND tuv.CREATION_USER = ? ").addValues(createUser);
				}
			}
			//modify user
			if (StringUtil.isNotEmpty(modifyUser))
			{
				if (tmType.equalsIgnoreCase("TM3"))
				{
					sb.append(" AND tuv.modifyUser = ? ").addValues(modifyUser);
				}
				else if (tmType.equalsIgnoreCase("TM2"))
				{
					sb.append(" AND tuv.MODIFY_USER = ? ")
							.addValues(modifyUser);
				}
			}
			
			if (StringUtil.isNotEmpty(String.valueOf(localeId)))
			{
				if (tmType.equalsIgnoreCase("TM3"))
				{
					sb.append(" AND tuv.localeId = ? ").addValue(localeId);
				}
				else if (tmType.equalsIgnoreCase("TM2"))
				{
					sb.append(" AND tuv.LOCALE_ID = ? ").addValue(localeId);
				}
			}
			
//			if (StringUtil.isNotEmpty(jobIds))
//			{
//				if (tmType.equalsIgnoreCase("TM3"))
//				{
//					sb.append(" AND ext.jobId in (").append(jobIds).append(")");
//				}
//			}
		}
	}

	private static String parseStartDate(Date start)
	{
		String startDate = format.format(start);
		if (StringUtil.isNotEmpty(startDate))
			return startDate + " 00:00:00";
		else return null;
	}

	private static String parseEndDate(Date end)
	{
		String endDate = format.format(end);
		if (StringUtil.isNotEmpty(endDate))
			return endDate + " 23:59:59";
		else return null;
	}
    
	private static List<TMidTUid> getIdList(Connection conn, StatementBuilder sb,
			long tmId) throws SQLException
	{
		List<TMidTUid> idList = new ArrayList<TMidTUid>();
		PreparedStatement ps = sb.toPreparedStatement(conn);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			TMidTUid tt = new TMidTUid(tmId, rs.getLong(1));
			if (!idList.contains(tt))
			{
				idList.add(tt);
			}
		}
		ps.close();
		return idList;
	}
	
    private static HashMap getFilterMap(HttpServletRequest request,
			boolean advancedSearch)
	{
		HashMap filterMap = new HashMap();
		String createStartDateOption = null;
		Date createStartDate = null;
		String createEndDateOption = null;
		Date createEndDate = null;
		String modifyStartDateOption = null;
		Date modifyStartDate = null;
		String modifyEndDateOption = null;
		Date modifyEndDate = null;
		String lastUsageStartDateOption = null;
		Date lastUsageStartDate = null;
		String lastUsageEndDateOption = null;
		Date lastUsageEndDate = null;
		String tuIds = null;
		String sids = null;
		String isRegex = null;
		String createUser = null;
		String modifyUser = null;
		Set<String> jobIdSet = null;
		
		if (advancedSearch)
		{
			createStartDateOption = (String) request.getParameter("createStartDateOption");
			createStartDate = parseDate((String) request.getParameter("createStartDate"));
			createEndDateOption = (String) request.getParameter("createEndDateOption");
			createEndDate = parseDate((String) request.getParameter("createEndDate"));

			modifyStartDateOption = (String) request.getParameter("modifyStartDateOption");
			modifyStartDate = parseDate((String) request.getParameter("modifyStartDate"));
			modifyEndDateOption = (String) request.getParameter("modifyEndDateOption");
			modifyEndDate = parseDate((String) request.getParameter("modifyEndDate"));

			lastUsageStartDateOption = (String) request.getParameter("lastUsageStartDateOption");
			lastUsageStartDate = parseDate((String) request.getParameter("lastUsageStartDate"));
			lastUsageEndDateOption = (String) request.getParameter("lastUsageEndDateOption");
			lastUsageEndDate = parseDate((String) request.getParameter("lastUsageEndDate"));
			
			tuIds = (String) request.getParameter("tuIds");
			sids = (String) request.getParameter("sids");
			isRegex = (String) request.getParameter("isRegex");
			createUser = (String) request.getParameter("createUser");
			modifyUser = (String) request.getParameter("modifyUser");
			String jobIds = (String)request.getParameter("jobIds");
			if (StringUtil.isNotEmpty(jobIds))
			{
				jobIdSet = new HashSet<String>();
				for (String jobId : jobIds.split(","))
				{
					jobIdSet.add(jobId);
				}
			}
			
			filterMap.put("createStartDateOption", createStartDateOption);
			filterMap.put("createStartDate", createStartDate);
			filterMap.put("createEndDateOption", createEndDateOption);
			filterMap.put("createEndDate", createEndDate);
			filterMap.put("modifyStartDateOption", modifyStartDateOption);
			filterMap.put("modifyStartDate", modifyStartDate);
			filterMap.put("modifyEndDateOption", modifyEndDateOption);
			filterMap.put("modifyEndDate", modifyEndDate);
			filterMap.put("lastUsageStartDateOption", lastUsageStartDateOption);
			filterMap.put("lastUsageStartDate", lastUsageStartDate);
			filterMap.put("lastUsageEndDateOption", lastUsageEndDateOption);
			filterMap.put("lastUsageEndDate", lastUsageEndDate);
			filterMap.put("tuIds", tuIds);
			filterMap.put("sids", sids);
			filterMap.put("isRegex", isRegex);
			filterMap.put("createUser", createUser);
			filterMap.put("modifyUser", modifyUser);
			filterMap.put("jobIds", jobIdSet);
		}
		return filterMap;
	}
    
    private static String getTuIds(Iterator<LeverageMatches> itLeverageMatches)
    {
    	String ids = "";
		while (itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = itLeverageMatches.next();
            Iterator itLocales = levMatches.targetLocaleIterator(-1);
            while (itLocales.hasNext())
            {
                GlobalSightLocale tLocale = (GlobalSightLocale) itLocales
                        .next();
                Iterator itMatch = levMatches.matchIterator(tLocale, -1);
                while (itMatch.hasNext())
                {
                    LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
                    ids += matchedTuv.getTu().getId() + ",";
                }
            }
        }
		if(ids.length() > 0)
		{
			ids = ids.substring(0, ids.length() -1);
		}
    	return ids;
    }
    
    private static String getTuIds(List<SegmentTmTu> tus)
    {
    	String ids = "";
    	for(SegmentTmTu tu: tus)
		{
			if(tu != null)
			{
				ids+= tu.getId() + ",";
			}
		}
    	if(ids.length() > 0)
		{
			ids = ids.substring(0, ids.length() -1);
		}
    	return ids;
    }
    
    private static Set<Long> getAttributeFilterTuIds(String ids,
    		String attributeName, String attributeValue)
    {
    	Connection conn = null;
    	Set<Long> attributeFilterTuIds = new HashSet<Long>();
    	try
    	{
    		conn = DbUtil.getConnection();
    		StatementBuilder sbTm3 = new StatementBuilder();
    		StatementBuilder sbTm2 = new StatementBuilder();
    		if(ids.length() > 0)
    		{
    			sbTm3.append(" SELECT DISTINCT tuId from tm3_attr_val_shared_").append(CompanyThreadLocal.getInstance().getValue())
    					.append(" as value, tm3_attr as attr where attr.name = '").append(attributeName).append("' and value.value = '")
    					.append(attributeValue).append("' and value.attrId = attr.id and tuId in (").append(ids).append(")");
    			attributeFilterTuIds.addAll(SQLUtil.execIdsQuery(conn, sbTm3));
    			
    			sbTm2.append("select DISTINCT tu_id  from  project_tm_tu_t_prop as prop where prop.prop_value = '").append(attributeValue)
    						.append("' and prop.prop_type = 'Att::").append(attributeName).append("'");
    			attributeFilterTuIds.addAll(SQLUtil.execIdsQuery(conn, sbTm2));
    		}
    	}
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    	
    	return attributeFilterTuIds;
    }
 
    private static boolean searchFilter(HashMap filterMap, BaseTmTuv tuv) 
    {
    	String createStartDateOption = (String) filterMap.get("createStartDateOption");
		Date createStartDate = (Date) filterMap.get("createStartDate");
		String createEndDateOption = (String) filterMap.get("createEndDateOption");
		Date createEndDate = (Date) filterMap.get("createEndDate");
		String modifyStartDateOption = (String) filterMap.get("modifyStartDateOption");
		Date modifyStartDate = (Date) filterMap.get("modifyStartDate");
		String modifyEndDateOption =(String)  filterMap.get("modifyEndDateOption");
		Date modifyEndDate = (Date) filterMap.get("modifyEndDate");
		String lastUsageStartDateOption = (String) filterMap.get("lastUsageStartDateOption");
		Date lastUsageStartDate = (Date) filterMap.get("lastUsageStartDate");
		String lastUsageEndDateOption =(String)  filterMap.get("lastUsageEndDateOption");
		Date lastUsageEndDate = (Date) filterMap.get("lastUsageEndDate");
		String tuIds = (String) filterMap.get("tuIds");
		String sids = (String) filterMap.get("sids");
		String isRegex = (String) filterMap.get("isRegex");
		String createUser = (String) filterMap.get("createUser");
		String modifyUser = (String) filterMap.get("modifyUser");
//    	String jobIds = (String) filterMap.get("jobIds");

        boolean checkCreateDate = searchByDate("create", createStartDateOption,
                createEndDateOption, createStartDate, createEndDate, tuv);
    	if(!checkCreateDate)
    	{
    		return false;
    	}

    	boolean checkModifyDate = searchByDate("modify", modifyStartDateOption, 
    			modifyEndDateOption, modifyStartDate, modifyEndDate, tuv);
    	if(!checkModifyDate)
    	{
    		return false;
    	}
    	
      	boolean checkLastUsageDate = searchByDate("lastUsage", lastUsageStartDateOption, 
      			lastUsageEndDateOption, lastUsageStartDate, lastUsageEndDate, tuv);
    	if(!checkLastUsageDate)
    	{
    		return false;
    	}

    	if(StringUtil.isNotEmpty(tuIds))
    	{
    		boolean tuidMatch = false;
    		if(tuIds.indexOf(",") > 0)
    		{
    			String[] temp = tuIds.split(",");
    			for(String tuid: temp)
    			{
    				if(tuid.indexOf("-") > 0)
    				{
    					String[] temp2 = tuid.split("-");
    					Long minTuid = Long.parseLong(temp2[0]);
    					Long maxTuid = Long.parseLong(temp2[1]);
    					Long tuId = tuv.getTu().getId();
    					if(tuId >= minTuid && tuId <= maxTuid)
    					{
    						tuidMatch = true;
    						break;
    					}
    				}
    				else if(tuv.getTu().getId() == Long.parseLong(tuid))
    				{
    					tuidMatch = true;
    					break;
    				}
    			}
    		}
    		else if(tuIds.indexOf("-") > 0)
    		{
    			String[] temp = tuIds.split("-");
    			Long minTuid = Long.parseLong(temp[0]);
    			Long maxTuid = Long.parseLong(temp[1]);
    			Long tuId = tuv.getTu().getId();
    			if(tuId >= minTuid && tuId <= maxTuid)
    			{
    				tuidMatch = true;
    			}
    		}
    		else if(tuv.getTu().getId() == Long.parseLong(tuIds))
    		{
    			tuidMatch = true;
    		}
    		
    		if(!tuidMatch)
    		{
    			return false;
    		}
    	}
    	
    	if(StringUtil.isNotEmpty(sids))
    	{
    		if(isRegex.equals("true"))
    		{
    			 Pattern p = Pattern.compile(sids);
    			 Matcher m = p.matcher(tuv.getSid());
    			 if(!m.matches())
    			 {
    				 return false;
    			 }
    		}
    		else
    		{
    			if(!sids.equals(tuv.getSid()))
    			{
    				return false;
    			}
    		}
    	}
    	
    	if(StringUtil.isNotEmpty(createUser))
    	{
    		if(!createUser.equals(tuv.getCreationUser()))
    		{
    			return false;
    		}
    	}
    	
    	if(StringUtil.isNotEmpty(modifyUser))
    	{
    		if(!modifyUser.equals(tuv.getModifyUser()))
    	    {
    	    		return false;
    	    }
    	}
    	
//		if (StringUtil.isNotEmpty(jobIds))
//		{
//			if (!jobIds.contains(String.valueOf(tuv.getJobId())))
//			{
//				return false;
//			}
//		}
    	
    	return true;
    }

    private static boolean searchFilterBySid(HashMap filterMap, BaseTmTuv tuv)
	{
		String tuIds = (String) filterMap.get("tuIds");
		String sids = (String) filterMap.get("sids");
		String isRegex = (String) filterMap.get("isRegex");

		if (StringUtil.isNotEmpty(tuIds))
		{
			boolean tuidMatch = false;
			if (tuIds.indexOf(",") > 0)
			{
				String[] temp = tuIds.split(",");
				for (String tuid : temp)
				{
					if (tuid.indexOf("-") > 0)
					{
						String[] temp2 = tuid.split("-");
						Long minTuid = Long.parseLong(temp2[0]);
						Long maxTuid = Long.parseLong(temp2[1]);
						Long tuId = tuv.getTu().getId();
						if (tuId >= minTuid && tuId <= maxTuid)
						{
							tuidMatch = true;
							break;
						}
					}
					else if (tuv.getTu().getId() == Long.parseLong(tuid))
					{
						tuidMatch = true;
						break;
					}
				}
			}
			else if (tuIds.indexOf("-") > 0)
			{
				String[] temp = tuIds.split("-");
				Long minTuid = Long.parseLong(temp[0]);
				Long maxTuid = Long.parseLong(temp[1]);
				Long tuId = tuv.getTu().getId();
				if (tuId >= minTuid && tuId <= maxTuid)
				{
					tuidMatch = true;
				}
			}
			else if (tuv.getTu().getId() == Long.parseLong(tuIds))
			{
				tuidMatch = true;
			}

			if (!tuidMatch)
			{
				return false;
			}
		}

		if (StringUtil.isNotEmpty(sids))
		{
			if (isRegex.equals("true"))
			{
				Pattern p = Pattern.compile(sids);
				Matcher m = p.matcher(tuv.getSid());
				if (!m.matches())
				{
					return false;
				}
			}
			else
			{
				if (!sids.equals(tuv.getSid()))
				{
					return false;
				}
			}
		}
		return true;
	}
    
	private static boolean searchByDate(String searchByDataType,
			String startDateOption, String endDateOption, Date startDate,
			Date endDate, BaseTmTuv tuv) 
	{
		if (startDate == null && endDate == null)
			return true;

		Date date = null;
        try
        {
            if (StringUtil.isNotEmpty(searchByDataType))
            {
                if (searchByDataType.equalsIgnoreCase("create"))
                {
                    if (tuv.getCreationDate() != null)
                    {
                        date = format.parse(format.format(tuv.getCreationDate()));
                    }
                }
                else if (searchByDataType.equalsIgnoreCase("modify"))
                {
                    if (tuv.getModifyDate() != null)
                    {
                        date = format.parse(format.format(tuv.getModifyDate()));
                    }
                }
                else if (searchByDataType.equalsIgnoreCase("lastUsage"))
                {
                    if (tuv.getLastUsageDate() != null)
                    {
                        date = format.parse(format.format(tuv.getLastUsageDate()));
                    }
                }
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

		if (date != null)
        {
            if (startDate != null && endDate == null)
            {
                if (startDateOption.equals(DATE_EQUALS))
                {
                    if (date.equals(startDate))
                    {
                        return true;
                    }
                }
                else if (startDateOption.equals(DATE_NOT_EQUALS))
                {
                    if (!date.equals(startDate))
                    {
                        return true;
                    }
                }
                else if (startDateOption.equals(DATE_GREATER_THAN))
                {
                    if (date.after(startDate))
                    {
                        return true;
                    }
                }

                else if (startDateOption.equals(DATE_GREATER_THAN_OR_EQUALS))
                {
                    if (date.after(startDate) || date.equals(startDate))
                    {
                        return true;
                    }
                }
            }
            else if (startDate == null && endDate != null)
            {

                if (endDateOption.equals(DATE_LESS_THAN))
                {
                    if (date.before(endDate))
                    {
                        return true;
                    }
                }
                else if (endDateOption.equals(DATE_LESS_THAN_OR_EQUALS))
                {
                    if (date.before(endDate) || date.equals(endDate))
                    {
                        return true;
                    }
                }
            }
            else if (startDate != null && endDate != null)
            {
                if (startDateOption.equals(DATE_GREATER_THAN))
                {
                    if (endDateOption.equals(DATE_LESS_THAN))
                    {
                        if (date.after(startDate) && date.before(endDate))
                        {
                            return true;
                        }
                    }
                    else if (endDateOption.equals(DATE_LESS_THAN_OR_EQUALS))
                    {
                        if (date.after(startDate) && (date.before(endDate) || date.equals(endDate)))
                        {
                            return true;
                        }
                    }
                }
                else if (startDateOption.equals(DATE_GREATER_THAN_OR_EQUALS))
                {
                    if (endDateOption.equals(DATE_LESS_THAN))
                    {
                        if ((date.after(startDate) || date.equals(startDate))
                                && date.before(endDate))
                        {
                            return true;
                        }
                    }
                    else if (endDateOption.equals(DATE_LESS_THAN_OR_EQUALS))
                    {
                        if ((date.after(startDate) || date.equals(startDate))
                                && (date.before(endDate) || date.equals(endDate)))
                        {
                            return true;
                        }
                    }
                }
            }
        }
		return false;
	}
	
	private static Date parseDate(String dateStr)
	{
		if (StringUtil.isNotEmpty(dateStr))
		{
			try
			{
				return sdf.parse(dateStr);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

    /**
     * Refresh page
     * 
     * @return
     * @throws Exception
     */
    public static String refreshPage(HttpServletRequest request)
    {
        // get the search result from session
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List result = (List) sessionMgr.getAttribute("searchResult");
        if (result == null)
        {
            return "";
        }
        // get the display records according current page and search result
        String page = (String) request.getParameter("page");
        int maxEntriesPerPage = Integer.parseInt((String) request
                .getParameter("maxEntriesPerPage"));
        List<Object> list = new ArrayList<Object>();
        int i = (Integer.parseInt(page) - 1) * maxEntriesPerPage;
        int max = i + maxEntriesPerPage - 1 < result.size() ? i
                + maxEntriesPerPage : result.size();
        for (; i < max; i++)
        {
            list.add(result.get(i));
        }

        return JsonUtil.toJson(list);
    }

    /**
     * Delete Entries
     * 
     */
    public static String deleteEntries(HttpServletRequest request)
            throws Exception
    {
        // get the search result from session
        HttpSession httpSession = request.getSession();
        SessionManager sessionMgr = (SessionManager) httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List<Map<String, Object>> result = (List<Map<String, Object>>) sessionMgr
                .getAttribute("searchResult");
        String maxEntriesPerPageStr = (String) request
                .getParameter("maxEntriesPerPage");
        int maxEntriesPerPage;
        if ("All".equals(maxEntriesPerPageStr))
        {
            maxEntriesPerPage = result.size();
        }
        else
        {
            maxEntriesPerPage = Integer.parseInt(maxEntriesPerPageStr);
        }
        // Get the deleted index of search results
        List<Integer> deletedEntries = getSelectedEntries(request,
                result.size(), maxEntriesPerPage);

        // clear up all result, tuMap(tmId, tus), tuTuvMap(tmId+tuId, tuvs)
        Map<Long, List<Long>> tuMap = new HashMap<Long, List<Long>>();
        Map<String, List<Long>> tuTuvMap = new HashMap<String, List<Long>>();
        String targetLocaleStr = null;
        for (int entryIndex : deletedEntries)
        {
            Map<String, Object> map = result.get(entryIndex);
            long tuId = (Long) map.get("tuId");
            long tmId = (Long) map.get("tmId");
            long tuvId = (Long) map.get("targetTuvId");
            targetLocaleStr = (String) map.get("targetLocale");

            if (tuMap.get(tmId) != null)
            {
                tuMap.get(tmId).add(tuId);
            }
            else
            {
                List<Long> tuIds = new ArrayList<Long>();
                tuIds.add(tuId);
                tuMap.put(tmId, tuIds);
            }

            String tmtu = String.valueOf(tmId) + String.valueOf(tuId);
            if (tuTuvMap.get(tmtu) != null)
            {
                tuTuvMap.get(tmtu).add(tuvId);
            }
            else
            {
                List<Long> tuvIds = new ArrayList<Long>();
                tuvIds.add(tuvId);
                tuTuvMap.put(tmtu, tuvIds);
            }
        }

        GlobalSightLocale targetLocale = ServerProxy.getLocaleManager()
                .getLocaleByString(targetLocaleStr);
        ProjectHandler ph = ServerProxy.getProjectHandler();
        TmCoreManager tmManager = LingServerProxy.getTmCoreManager();
        try
        {
            Iterator<Entry<Long, List<Long>>> it = tuMap.entrySet().iterator();
            while (it.hasNext())
            {
                List<SegmentTmTuv> tuvList = new ArrayList<SegmentTmTuv>();
                Map.Entry<Long, List<Long>> entry = it.next();
                long tmId = entry.getKey();
                List<Long> tuIds = entry.getValue();
                Tm tm = ph.getProjectTMById(tmId, false);
                // get all TUS in current TM
                List<SegmentTmTu> tus = tm.getSegmentTmInfo().getSegmentsById(
                        tm, tuIds);
                for (SegmentTmTu tu : tus)
                {
                    long tuId = tu.getId();
                    String tmtu = String.valueOf(tmId) + String.valueOf(tuId);
                    List<Long> tuvIdList = tuTuvMap.get(tmtu);
                    Collection targetTuvs = tu.getTuvList(targetLocale);
                    for (Iterator itTuvs = targetTuvs.iterator(); itTuvs
                            .hasNext();)
                    {
                        SegmentTmTuv trgTuv = (SegmentTmTuv) itTuvs.next();
                        if (tuvIdList.contains(trgTuv.getId()))
                        {
                            tuvList.add(trgTuv);
                        }
                    }
                }
                // Delete TUVS of current tm
                tmManager.deleteSegmentTmTuvs(tm, tuvList);
            }
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }

        // refresh the search results
        List<Map<String, Object>> newResult = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < result.size(); i++)
        {
            if (!deletedEntries.contains(i))
            {
                newResult.add(result.get(i));
            }
        }

        if (newResult.size() < 1)
        {
            sessionMgr.setAttribute("searchResult", null);
            return "";
        }
        sessionMgr.setAttribute("searchResult", newResult);

        Map<String, Object> temp = getDisplayResult(newResult, 1,
                maxEntriesPerPageStr);
        return JsonUtil.toJson(temp);
    }

    /**
     * Get display result
     * 
     * @param result
     * @param maxEntriesPerPage
     * @return
     */
    private static Map<String, Object> getDisplayResult(
            List<Map<String, Object>> result, int page,
            String maxEntriesPerPageStr)
    {
        int maxEntriesPerPage;
        if ("All".equals(maxEntriesPerPageStr))
        {
            maxEntriesPerPage = result.size();
        }
        else
        {
            maxEntriesPerPage = Integer.parseInt(maxEntriesPerPageStr);
        }
        List<Object> list = new ArrayList<Object>();
        int i = (page - 1) * maxEntriesPerPage;
        int max = i + maxEntriesPerPage - 1 < result.size() ? i
                + maxEntriesPerPage : result.size();

        for (; i < max; i++)
        {
            list.add(result.get(i));
        }
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("result", list);
        temp.put("totalNum", result.size());
        return temp;
    }

    /**
     * Apply Replaced
     * 
     */
    public static String applyReplaced(HttpServletRequest request, String userId)
            throws Exception
    {
        HttpSession httpSession = request.getSession();
        SessionManager sessionMgr = (SessionManager) httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List<Map<String, Object>> result = (List<Map<String, Object>>) sessionMgr
                .getAttribute("searchResult");

        String maxEntriesPerPageStr = (String) request
                .getParameter("maxEntriesPerPage");
        int maxEntriesPerPage;
        if ("All".equals(maxEntriesPerPageStr))
        {
            maxEntriesPerPage = result.size();
        }
        else
        {
            maxEntriesPerPage = Integer.parseInt(maxEntriesPerPageStr);
        }

        String searchIn = (String) request.getParameter("searchIn");
        boolean searchInSource = "source".equals(searchIn);
        String searchText = (String) request.getParameter("searchText");
        String replaceText = (String) request.getParameter("replaceText");

        // Get selected index of entries
        List<Integer> deletedEntries = getSelectedEntries(request,
                result.size(), maxEntriesPerPage);

        // clear up all result, tuMap(tmId, tus), tuTuvMap(tmId+tuId, tuvs)
        Map<Long, List<Long>> tuMap = new HashMap<Long, List<Long>>();
        Map<String, List<Long>> tuTuvMap = new HashMap<String, List<Long>>();
        String localeStr = null;
        for (int entryIndex : deletedEntries)
        {
            Map<String, Object> map = (Map<String, Object>) result
                    .get(entryIndex);
            long tuId = (Long) map.get("tuId");
            long tmId = (Long) map.get("tmId");
            long tuvId;
            if (searchInSource)
            {
                tuvId = (Long) map.get("sourceTuvId");
                localeStr = (String) map.get("sourceLocale");
            }
            else
            {
                tuvId = (Long) map.get("targetTuvId");
                localeStr = (String) map.get("targetLocale");
            }

            if (tuMap.get(tmId) != null)
            {
                tuMap.get(tmId).add(tuId);
            }
            else
            {
                List<Long> tuIds = new ArrayList<Long>();
                tuIds.add(tuId);
                tuMap.put(tmId, tuIds);
            }

            String tmtu = String.valueOf(tmId) + String.valueOf(tuId);
            if (tuTuvMap.get(tmtu) != null)
            {
                tuTuvMap.get(tmtu).add(tuvId);
            }
            else
            {
                List<Long> tuvIds = new ArrayList<Long>();
                tuvIds.add(tuvId);
                tuTuvMap.put(tmtu, tuvIds);
            }
        }

        // Get all TUVS from DB, and alter segment, modify user, modify date,
        // then save
        Map<Tm, List<SegmentTmTuv>> tmTuvs = new HashMap<Tm, List<SegmentTmTuv>>();
        GlobalSightLocale locale = ServerProxy.getLocaleManager()
                .getLocaleByString(localeStr);
        ProjectHandler ph = ServerProxy.getProjectHandler();
        int replacedNum = 0;
        try
        {
            GxmlElementSubstringReplace substringReplacer = new GxmlElementSubstringReplace(
                    searchText, replaceText, false, locale.getLocale());
            Iterator<Entry<Long, List<Long>>> it = tuMap.entrySet().iterator();
            while (it.hasNext())
            {
                List<SegmentTmTuv> tuvList = new ArrayList<SegmentTmTuv>();
                Map.Entry<Long, List<Long>> entry = it.next();
                long tmId = entry.getKey();
                List<Long> tuIds = entry.getValue();
                Tm tm = ph.getProjectTMById(tmId, false);
                // get all TUS in current TM
                List<SegmentTmTu> tus = tm.getSegmentTmInfo().getSegmentsById(
                        tm, tuIds);
                for (SegmentTmTu tu : tus)
                {
                    long tuId = tu.getId();
                    String tmtu = String.valueOf(tmId) + String.valueOf(tuId);
                    List<Long> tuvIdList = tuTuvMap.get(tmtu);
                    Collection tuvs = tu.getTuvList(locale);
                    for (Iterator itTuvs = tuvs.iterator(); itTuvs.hasNext();)
                    {
                        SegmentTmTuv tuv = (SegmentTmTuv) itTuvs.next();
                        if (tuvIdList.contains(tuv.getId()))
                        {
                            String segment = tuv.getSegment();
                            GxmlElement gxmlElement = getGxmlElement(segment);
                            boolean replaced = substringReplacer
                                    .replace(gxmlElement);
                            if (replaced)
                            {
                                tuv.setSegment(gxmlElement.toGxml());
                                String exactMatchFormat = tuv
                                        .getExactMatchFormat();
                                tuv.setExactMatchKey(GlobalSightCrc
                                        .calculate(exactMatchFormat));
                                tuv.setModifyUser(userId);
                                tuvList.add(tuv);
                            }
                        }
                    }
                }

                // Update all tuvs in current TM
                tmTuvs.put(tm, tuvList);
                replacedNum = replacedNum + tuvList.size();
            }
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        TmCoreManager tmManager = LingServerProxy.getTmCoreManager();
        for (Map.Entry<Tm, List<SegmentTmTuv>> entry : tmTuvs.entrySet())
        {
            Tm tm = entry.getKey();
            List<SegmentTmTuv> tuvs = entry.getValue();
            tmManager.updateSegmentTmTuvs(tm, tuvs);
        }

        return replacedNum + " entries have been replaced.";
    }

    /**
     * Get index of selected entries
     * 
     * @param request
     * @param resultLength
     * @param maxEntriesPerPage
     * @return
     */
    private static List<Integer> getSelectedEntries(HttpServletRequest request,
            int resultLength, int maxEntriesPerPage)
    {
        int currentPage = Integer.parseInt((String) request
                .getParameter("currentPage"));
        String entriesStr = (String) request.getParameter("entries");
        String selectAllEntriesAllPages = (String) request
                .getParameter("allEntriesAllPages");
        boolean deleteAllEntriesAllPages = "true"
                .equals(selectAllEntriesAllPages);

        List<Integer> selectedEntries = new ArrayList<Integer>();
        if (deleteAllEntriesAllPages)
        {
            for (int i = 0; i < resultLength; i++)
            {
                selectedEntries.add(i);
            }
        }
        else
        {
            String[] entries = entriesStr.split(",");
            int num = (currentPage - 1) * maxEntriesPerPage;
            for (String entry : entries)
            {
                int entryIndex = num + Integer.parseInt(entry);
                selectedEntries.add(entryIndex);
            }
        }
        return selectedEntries;
    }

    /**
     * Get source or target segment display in page
     * 
     * @param p_findText
     *            text to find
     * @param p_caseSensitive
     *            case sensitive?
     * @param p_tuv
     *            a TUV
     * @return String of HTML
     */
    private static Map<String, String> getFormattedSegment(String findText,
            String replaceText, BaseTmTuv tuv) throws Exception
    {
        Map<String, String> segment = new HashMap<String, String>();
        String gxml = GxmlUtil.stripRootTag(tuv.getSegment());
        String rawPtagStr = makePtagString(gxml,
                PseudoConstants.PSEUDO_COMPACT, tuv.getTu().getFormat());

        if (EditUtil.isRTLLocale(tuv.getLocale()))
        {
            segment.put("dir", Text.containsBidiChar(rawPtagStr) ? "RTL"
                    : "LTR");
        }
        else
        {
            segment.put("dir", "LTR");
        }
        PtagStringFormatter format = new PtagStringFormatter();
        String temp = format.htmlLtrPtags(
                EditUtil.encodeHtmlEntities(rawPtagStr), findText, false,
                tuv.getLocale());

        // Highlight
        if (findText != null)
        {
            String findTextLower = findText.toLowerCase();
            String tempLower = temp.toLowerCase();
            String matchFontLower = "<font color=\"blue\"><b>" + findTextLower
                    + "</b></font>";
            int findTextStart = matchFontLower.indexOf(findTextLower);
            int findTextEnd = findTextStart + findTextLower.length();
            int i = tempLower.indexOf(matchFontLower);
            if (i >= 0)
            {
                String matchFont = temp.substring(i,
                        i + matchFontLower.length());
                String matchStr = temp.substring(i + findTextStart, i
                        + findTextEnd);
                if (replaceText != null)
                {
                    String newStr = "<span style=\"color:blue;background-color:#C0C0C0;\"><b>"
                            + matchStr
                            + "</b></span>"
                            + "<span style=\"color:blue;background-color:#C2F70E;\">"
                            + replaceText + "</span>";
                    temp = temp.replace(matchFont, newStr);
                }
                else
                {
                    String newStr = "<span style=\"color:blue;background-color:#C0C0C0;\"><b>"
                            + matchStr + "</b></span>";
                    temp = temp.replace(matchFont, newStr);
                }
            }
        }
        segment.put("content", temp);

        return segment;
    }

    /**
     * Convert a gxml string to a ptag string.
     * 
     * @param gxml
     * @param pTagDisplayMode
     * @param dataType
     * @return
     * @throws Exception
     */
    private static String makePtagString(String gxml, int pTagDisplayMode,
            String dataType) throws Exception
    {
        PseudoData pTagData = null;

        // create ptag resources
        pTagData = new PseudoData();
        // convert gxml
        pTagData.setMode(pTagDisplayMode);
        pTagData.setAddables(dataType);
        TmxPseudo.tmx2Pseudo(gxml, pTagData);

        return pTagData.getPTagSourceString();
    }

    private static GxmlElement getGxmlElement(String p_segment)
            throws GxmlException
    {
        GxmlElement result = null;

        GxmlFragmentReader reader = GxmlFragmentReaderPool.instance()
                .getGxmlFragmentReader();

        try
        {
            result = reader.parseFragment(p_segment);
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }

        return result;
    }

    static class TMProfileVO
    {
        long id;
        String name;

        public void setId(long id)
        {
            this.id = id;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
    }

    static class TMProfileVOComparator extends StringComparator
    {
        private static final long serialVersionUID = 6761726275505402553L;

        public TMProfileVOComparator(Locale locale)
        {
            super(locale);
            // TODO Auto-generated constructor stub
        }

        /**
         * Performs a comparison of two TMProfileVO objects.
         */
        public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            TMProfileVO a = (TMProfileVO) p_A;
            TMProfileVO b = (TMProfileVO) p_B;

            String aValue;
            String bValue;
            int rv;
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            return rv;
        }
    }
}

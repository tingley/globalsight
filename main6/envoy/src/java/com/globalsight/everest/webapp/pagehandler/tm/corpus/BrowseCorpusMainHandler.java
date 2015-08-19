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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.BasicWorkflowTemplateHandler;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.tm.maintenance.TableMaker;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>
 * BrowseCorpusMainHandler is responsible for helping browse the corpus.
 * </p>
 */
public class BrowseCorpusMainHandler extends PageHandler
{
    private static final Logger c_logger = Logger
            .getLogger(BrowseCorpusMainHandler.class);
    private static final String COLON = ":";

    //
    // Constructor
    //
    public BrowseCorpusMainHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler, which performs the search/browse request and
     * puts information on the request for the JSP to use
     * 
     * @param p_pageDescriptor
     *            the page descriptor
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
        try
        {
            // find out which search we're doing
            setCommonAttributesInRequest(p_request, p_response);

            String searchType = p_request.getParameter("searchType");

            if ("docNameSearch".equals(searchType))
            {
                handleDocNameSearch(p_request, p_response);
            }
            else if ("fuzzySearch".equals(searchType))
            {
                handleFuzzySearch(p_request, p_response);
            }
            else
            {
                setOrderedTm(p_request);
                handleFullTextSearch(p_request, p_response);
            }

            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            Company curremtCompany = CompanyWrapper
                    .getCompanyById(currentCompanyId);
            boolean enableTMAccessControl = curremtCompany
                    .getEnableTMAccessControl();
            p_request.setAttribute("enableTMAccessControl",
                    enableTMAccessControl);
            // used for TM Access Control
            if (enableTMAccessControl)
            {
                getTmListOfUser(p_request);
            }
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
        catch (EnvoyServletException ese)
        {
            throw ese;
        }
        catch (ServletException se)
        {
            throw se;
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void setOrderedTm(HttpServletRequest p_request)
            throws ProjectHandlerException, RemoteException, GeneralException,
            NamingException
    {
        Collection alltms = ServerProxy.getProjectHandler().getAllProjectTMs();

        String orderedTM = p_request.getParameter("orderedTM");
        if (orderedTM == null)
        {
            return;
        }
        String[] tms = orderedTM.split(COLON);
        ArrayList<String> orderedTmNames = new ArrayList<String>();
        for (int i = 0; i < tms.length; i++)
        {
            if (!"".equals(tms[i]))
            {
                orderedTmNames.add(tms[i]);
            }
        }

        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < orderedTmNames.size(); i++)
        {
            map.put(orderedTmNames.get(i), i);
        }
        Map<Long, Integer> mapOfIdIndex = new HashMap<Long, Integer>();
        for (Iterator iter = alltms.iterator(); iter.hasNext();)
        {
            ProjectTM ptm = (ProjectTM) iter.next();
            if (orderedTmNames.contains(ptm.getName()))
            {
                mapOfIdIndex.put(ptm.getId(), map.get(ptm.getName()));
            }
        }

        p_request.setAttribute("orderedTmNames", orderedTmNames);
        p_request.setAttribute("orderedTmMap", map);
        p_request.setAttribute("mapOfTmIdIndex", mapOfIdIndex);
    }

    private void setAllTmNamesInRequest(HttpServletRequest p_request)
            throws Exception
    {
        Collection tms = ServerProxy.getProjectHandler().getAllProjectTMs();
        ArrayList tmNames = new ArrayList();

        for (Iterator iter = tms.iterator(); iter.hasNext();)
        {
            ProjectTM ptm = (ProjectTM) iter.next();
            if (ptm.getIsRemoteTm() == false)
            {
                tmNames.add(ptm.getName());
            }
        }

        Locale uiLocale = (Locale) p_request.getSession()
                .getAttribute(UILOCALE);
        SortUtil.sort(tmNames, new StringComparator(uiLocale)); // jpf
        p_request.setAttribute("tmNames", tmNames);
    }

    private void setCommonAttributesInRequest(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        SessionManager sessionMgr = (SessionManager) p_request.getSession()
                .getAttribute(SESSION_MANAGER);
        Locale uiLocale = (Locale) p_request.getSession()
                .getAttribute(UILOCALE);

        EditorState state = (EditorState) sessionMgr.getAttribute(EDITORSTATE);

        // first find out whether the browser was launched from the
        // editor or not, if so use EditorState
        String fromEditorParam = p_request.getParameter("fromEditor");
        boolean fromEditor = false;
        if (fromEditorParam != null && "true".equals(fromEditorParam))
        {
            fromEditor = true;
        }

        p_request.setAttribute("fromEditor", new Boolean(fromEditor));

        // Get state
        String searchstate = (String) p_request
                .getParameter(TM_SEARCH_STATE_PARAM);

        boolean isFresh = true;
        p_request.setAttribute("isFresh", new Boolean(isFresh));

        String queryTextTmp = p_request.getParameter("queryText");
        if ((queryTextTmp == null || queryTextTmp.length() <= 0)
                && searchstate != null && searchstate.equals(TM_ACTION_REFRESH))
        {
            queryTextTmp = (String) sessionMgr.getAttribute("queryText");
        }

        String queryText = null;
        if (queryTextTmp != null && queryTextTmp.length() > 0)
        {
            sessionMgr.setAttribute("queryText", queryTextTmp);
            isFresh = false;
            queryText = EditUtil.utf8ToUnicode(queryTextTmp);
            p_request.setAttribute("isFresh", new Boolean(isFresh));
            p_request.setAttribute("queryText", queryText);
        }

        SystemConfiguration config = SystemConfiguration.getInstance();
        boolean showAllTms = config
                .getBooleanParameter(SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS);
        p_request.setAttribute("showAllTms", new Boolean(showAllTms));

        GlobalSightLocale sourceGSL;
        GlobalSightLocale targetGSL;

        // set the TMs
        if (fromEditor)
        {
            sourceGSL = state.getSourceLocale();
            targetGSL = state.getTargetLocale();
            p_request.setAttribute("sourceLocale", sourceGSL);
            p_request.setAttribute("targetLocale", targetGSL);

            // check whether we should use the TMs in the TM Profile
            if (showAllTms)
            {
                c_logger.debug("Showing all TMs to editor user.");

                setAllTmNamesInRequest(p_request);
            }
            else
            {
                c_logger.debug("Showing only TM Profile TMs to editor user.");

                ArrayList tmNames = new ArrayList();

                String[] a = state.getTmNames();
                for (int i = 0; i < a.length; i++)
                {
                    tmNames.add(a[i]);
                }

                p_request.setAttribute("tmNames", tmNames);
            }
        }
        else
        {
            c_logger.debug("Showing all TMs to non editor user.");
            setAllTmNamesInRequest(p_request);
        }

        // set the locales
        if (!fromEditor && !isFresh)
        {
            // we can't get source locale and target locale from the state
            // so get it from the request
            String localePairParam = (String) p_request
                    .getParameter("localePair");
            if ((localePairParam == null || localePairParam.length() <= 0)
                    && searchstate != null
                    && searchstate.equals(TM_ACTION_REFRESH))
            {
                LocalePair lp = (LocalePair) sessionMgr
                        .getAttribute("localePair");
                localePairParam = lp.getIdAsLong().toString();
            }

            if (localePairParam != null)
            {
                long lpid = Long.valueOf(localePairParam).longValue();
                LocalePair lp = ServerProxy.getLocaleManager()
                        .getLocalePairById(lpid);
                sourceGSL = lp.getSource();
                targetGSL = lp.getTarget();
                p_request.setAttribute("localePair", lp);
                sessionMgr.setAttribute("localePair", lp); // for refresh
                p_request.setAttribute("sourceLocale", sourceGSL);
                p_request.setAttribute("targetLocale", targetGSL);
            }
        }

        // set the fuzzy override
        String fuzzyOverrideParam = p_request.getParameter("fuzzyOverride");
        if ((fuzzyOverrideParam == null || fuzzyOverrideParam.length() <= 0)
                && searchstate != null && searchstate.equals(TM_ACTION_REFRESH))
        {
            Integer tmp = (Integer) sessionMgr.getAttribute("fuzzyOverride");
            p_request.setAttribute("fuzzyOverride", tmp);

        }
        else if (fuzzyOverrideParam != null)
        {
            Integer tmp = new Integer(fuzzyOverrideParam);
            p_request.setAttribute("fuzzyOverride", tmp);
            sessionMgr.setAttribute("fuzzyOverride", tmp); // for refresh
        }

        // set the tm profiles
        ArrayList tmProfiles = new ArrayList(ServerProxy.getProjectHandler()
                .getAllTMProfiles());
        p_request.setAttribute("tmProfiles", tmProfiles);

        String tmProfileId = p_request.getParameter("tmProfileId");
        if ((tmProfileId == null || tmProfileId.length() <= 0)
                && searchstate != null && searchstate.equals(TM_ACTION_REFRESH))
        {
            Long t = (Long) sessionMgr.getAttribute("tmProfileId");
            p_request.setAttribute("tmProfileId", t);
        }

        if (tmProfileId != null)
        {
            Long id = new Long(tmProfileId);
            p_request.setAttribute("tmProfileId", id);
            sessionMgr.setAttribute("tmProfileId", id); // for refresh
        }

        // re-use some code from BasicWorkflowTemplateHandler that puts
        // some values related to leverage locales in the sessionmgr
        BasicWorkflowTemplateHandler wfHandler = new BasicWorkflowTemplateHandler();
        wfHandler.setLeverageLocales(p_request,
                WorkflowTemplateHandlerHelper.getAllLocalePairs(uiLocale));
    }

    private void handleDocNameSearch(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        boolean isFresh = ((Boolean) p_request.getAttribute("isFresh"))
                .booleanValue();

        if (isFresh)
        {
            return;
        }

        String queryText = (String) p_request.getAttribute("queryText");
        Collection docs = ServerProxy.getCorpusManager()
                .getCorpusDoc(queryText);
        p_request.setAttribute("corpusDocs", docs);
    }

    private void handleFullTextSearch(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ArrayList tmNames = (ArrayList) p_request.getAttribute("tmNames");
        ArrayList tmNamesSession = (ArrayList) session.getAttribute("tmNames");

        GlobalSightLocale sourceGSL = (GlobalSightLocale) p_request
                .getAttribute("sourceLocale");
        GlobalSightLocale targetGSL = (GlobalSightLocale) p_request
                .getAttribute("targetLocale");
        boolean isFresh = ((Boolean) p_request.getAttribute("isFresh"))
                .booleanValue();
        String queryText = (String) p_request.getAttribute("queryText");

        // Get state, must be non-null.
        String searchstate = (String) p_request
                .getParameter(TM_SEARCH_STATE_PARAM);
        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);
        SearchReplaceManager manager;
        ArrayList searchtmNamesOverride = (ArrayList) p_request
                .getAttribute("orderedTmNames");
        buildTmNames(tmNames, searchtmNamesOverride, p_request);
        Map<Long, Integer> mapOfTmIdIndex = (HashMap) p_request
                .getAttribute("mapOfTmIdIndex");
        String[] tmIndexParams = buildTmIndexParams(searchtmNamesOverride,
                p_request);
        p_request.setAttribute("tmIndex", tmIndexParams);
        if (tmIndexParams != null)
        {
            sessionMgr.setAttribute("tmIndex", tmIndexParams);
        }

        if (status != null && searchstate != null
                && searchstate.equals(TM_SEARCH_STATE_NORMAL))
        {
            manager = (SearchReplaceManager) sessionMgr.getAttribute("manager");
            manager.detachListener((IProcessStatusListener) status);
            status = null;
        }

        if (!isFresh)
        {
            TableMaker tableMaker = new TableMaker(true);
            if (searchstate == null
                    || searchstate.equals(TM_SEARCH_STATE_NORMAL))
            {
                // initial search
                tmIndexParams = p_request.getParameterValues("tmIndex");
                ArrayList tmNamesOverride = new ArrayList();
                if (tmIndexParams != null)
                {
                    for (int j = 0; j < tmIndexParams.length; j++)
                    {
                        int index = Integer.parseInt(tmIndexParams[j]);
                        String tmName = (String) tmNames.get(index);
                        tmNamesOverride.add(tmName);
                    }
                }

                if (searchtmNamesOverride != null)
                {

                    tmNamesOverride = searchtmNamesOverride;
                }
                tmIndexParams = buildTmIndexParams(searchtmNamesOverride,
                        p_request);
                sessionMgr.setAttribute("tmIndex", tmIndexParams);
                p_request.setAttribute("tmIndex", tmIndexParams);

                TmManager mgr = ServerProxy.getTmManager();
                manager = mgr.getSearchReplacer(tmNamesOverride);

                status = new ProcessStatus();

                manager.attachListener((IProcessStatusListener) status);
                manager.search(EditUtil.encodeXmlEntities(queryText),
                        sourceGSL, targetGSL, false, mapOfTmIdIndex);

                sessionMgr.setAttribute(TM_TM_STATUS, status);
                sessionMgr.setAttribute("manager", manager);
            }
            else if (searchstate.equals(TM_ACTION_REFRESH))
            {
                TmConcordanceResult results = (TmConcordanceResult) status
                        .getResults();
                queryText = stripWildCards(queryText);
                String tableRows = "";
                if (results != null)
                {
                    tableRows = tableMaker.getTableRows(
                            EditUtil.encodeHtmlEntities(queryText), false,
                            null, false, results,
                            new TmConcordanceResultComparator(mapOfTmIdIndex),
                            null);
                }
                p_request.setAttribute("tableRows", tableRows);
                sessionMgr.setAttribute("results", results);

                tmIndexParams = (String[]) sessionMgr.getAttribute("tmIndex");
                tmIndexParams = buildTmIndexParams(searchtmNamesOverride,
                        p_request);
                p_request.setAttribute("tmIndex", tmIndexParams);
                if (tmIndexParams != null)
                {
                    sessionMgr.setAttribute("tmIndex", tmIndexParams); // for
                                                                       // refresh
                }

                if (tmNamesSession != null)
                {
                    session.setAttribute("tmNames", tmNamesSession);
                }
            }
            else
            {
                // paging

                TmConcordanceResult results = (TmConcordanceResult) sessionMgr
                        .getAttribute("results");
                if (searchstate.equals(WebAppConstants.TM_SEARCH_STATE_PREV))
                {
                    results.readPreviousPage();
                }
                else
                {
                    results.readNextPage();
                }
                queryText = stripWildCards(queryText);
                String tableRows = tableMaker.getTableRows(EditUtil
                        .encodeHtmlEntities(queryText), false, null, false,
                        results, new TmConcordanceResultComparator(
                                mapOfTmIdIndex), null);

                p_request.setAttribute("tableRows", tableRows);
            }
        }
    }

    private String[] buildTmIndexParams(ArrayList searchtmNamesOverride,
            HttpServletRequest request)
    {
        if (searchtmNamesOverride == null)
        {
            return request.getParameterValues("tmIndex");
        }
        String[] tmIndexParams = new String[searchtmNamesOverride.size()];
        for (int i = 0; i < searchtmNamesOverride.size(); i++)
        {
            tmIndexParams[i] = i + "";
        }
        return tmIndexParams;
    }

    private void buildTmNames(ArrayList tmNames,
            ArrayList searchtmNamesOverride, HttpServletRequest p_request)
    {
        if (searchtmNamesOverride == null)
        {
            p_request.getSession().setAttribute("tmNames", null);
            return;
        }
        ArrayList noSearchTmNames = new ArrayList();
        for (int i = 0; i < tmNames.size(); i++)
        {
            String tmName = (String) tmNames.get(i);
            for (int j = 0; j < searchtmNamesOverride.size(); j++)
            {
                if (tmName.equals((String) (searchtmNamesOverride.get(j))))
                {
                    tmNames.set(i, COLON);
                }
            }
        }
        for (int i = 0; i < tmNames.size(); i++)
        {
            if (!COLON.equals(tmNames.get(i)))
            {
                noSearchTmNames.add(tmNames.get(i));
            }
        }
        tmNames.clear();
        tmNames.addAll(searchtmNamesOverride);
        tmNames.addAll(noSearchTmNames);
        p_request.getSession().setAttribute("tmNames", tmNames);
    }

    /**
     * Strips wildcard chars from the string so it gets displayed properly
     * 
     * @param s
     *            query string
     * @return String
     */
    private String stripWildCards(String p_s)
    {
        String s = p_s;
        if (s.endsWith("*"))
        {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void handleFuzzySearch(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        boolean isFresh = ((Boolean) p_request.getAttribute("isFresh"))
                .booleanValue();
        if (isFresh)
        {
            return;
        }

        // add valid TM Profiles to the request
        GlobalSightLocale sourceGSL = (GlobalSightLocale) p_request
                .getAttribute("sourceLocale");
        GlobalSightLocale targetGSL = (GlobalSightLocale) p_request
                .getAttribute("targetLocale");

        List<GlobalSightLocale> trgLocales = new ArrayList<GlobalSightLocale>();
        trgLocales.add(targetGSL);
        LeveragingLocales levLocales = new LeveragingLocales();

        String[] chosenLeveragesParam = p_request
                .getParameterValues("chosenLeverages");
        if (chosenLeveragesParam != null)
        {
            Set<GlobalSightLocale> set = new HashSet<GlobalSightLocale>();

            for (int j = 0; j < chosenLeveragesParam.length; j++)
            {
                GlobalSightLocale gsl = ServerProxy.getLocaleManager()
                        .getLocaleByString(chosenLeveragesParam[j]);

                set.add(gsl);
            }

            levLocales.setLeveragingLocale(targetGSL, set);
        }
        else
        {
            levLocales.setLeveragingLocale(targetGSL, null);
        }

        Long tmProfileId = (Long) p_request.getAttribute("tmProfileId");
        TranslationMemoryProfile tmp = ServerProxy.getProjectHandler()
                .getTMProfileById(tmProfileId.longValue(), false);

        Vector<LeverageProjectTM> leverageProjectTms = tmp
                .getProjectTMsToLeverageFrom();
        Map<Long, String> map = new HashMap<Long, String>();
        for (int j = 0; j < leverageProjectTms.size(); j++)
        {
            LeverageProjectTM tm = leverageProjectTms.get(j);
            ProjectTM projectTM = ServerProxy.getProjectHandler()
                    .getProjectTMById(tm.getProjectTmId(), false);
            map.put(tm.getProjectTmId(), projectTM.getName());
        }
        p_request.setAttribute("mapTmIdName", map);
        LeverageOptions levOptions = new LeverageOptions(tmp, levLocales);

        Integer fuzzyOverride = (Integer) p_request.getAttribute("fuzzyOverride");

        String queryText = (String) p_request.getAttribute("queryText");
        String segment = "<segment>" + queryText + "</segment>";
        PageTmTu tu = new PageTmTu(-1, -1, "plaintext", "text", true);
        PageTmTuv tuv = new PageTmTuv(-1, segment, sourceGSL);
        tuv.setTu(tu);
        tuv.setExactMatchKey();
        tu.addTuv(tuv);

        c_logger.debug("Leveraging with fuzzy threshold: " + fuzzyOverride);

		LeverageDataCenter leverageDataCenter = LingServerProxy
				.getTmCoreManager().leverageSegments(
						Collections.singletonList(tuv), sourceGSL, trgLocales,
						levOptions);
        Iterator<LeverageMatches> itLeverageMatches = leverageDataCenter
                .leverageResultIterator();
        boolean hasMatches = false;
        int numMatches = 0;
        long jobId = -1;// -1 is fine here.
        while (itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = itLeverageMatches.next();

            // walk through all target locales in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator(jobId);
            while (itLocales.hasNext())
            {
                GlobalSightLocale tLocale = (GlobalSightLocale) itLocales
                        .next();

                // walk through all matches in the locale
                Iterator itMatch = levMatches.matchIterator(tLocale, jobId);
                while (itMatch.hasNext())
                {
                    hasMatches = true;
                    LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
                    numMatches++;
                }
            }
        }

        c_logger.debug("Number of lev matches: " + numMatches);

        if (hasMatches)
        {
            p_request.setAttribute("leverageResults", leverageDataCenter);
        }
    }

    class TmConcordanceResultComparator implements Comparator<SegmentTmTu>
    {
        Map<Long, Integer> mapOfTmIdIndex;

        public TmConcordanceResultComparator(Map<Long, Integer> mapOfTmIdIndex)
        {
            this.mapOfTmIdIndex = mapOfTmIdIndex;
        }

        public int compare(SegmentTmTu tu1, SegmentTmTu tu2)
        {
            if (mapOfTmIdIndex == null)
            {
                return 0;
            }
            int index1 = mapOfTmIdIndex.get(tu1.getTmId());
            int index2 = mapOfTmIdIndex.get(tu2.getTmId());
            return index1 - index2;
        }

    }

    /**
     * Get tmList for current user.
     * If current user is a super PM, then only fetch TM list of his in current company.
     * 
     * @param p_request
     * 
     * @author Leon Song
     * @since 8.0
     */
    private void getTmListOfUser(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) p_request.getSession()
                .getAttribute(SESSION_MANAGER);
        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        ArrayList<String> tmListOfUser = new ArrayList<String>();
        if (!isAdmin && !isSuperAdmin)
        {
            if (isSuperPM)
            {
                String companyId = CompanyThreadLocal.getInstance().getValue();
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
                    if (String.valueOf(tm.getCompanyId()).equals(companyId))
                    {
                        tmListOfUser.add(tm.getName());
                    }
                }
            }
            else
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
                    tmListOfUser.add(tm.getName());
                }
            }
            sessionMgr.setAttribute("tmListOfUser", tmListOfUser);
        }
    }
}

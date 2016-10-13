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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.ProcessStatus;

/**
 *
 */
public class TmSegmentDeleteHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmSegmentDeleteHandler.class);

    //
    // Constructor
    //
    public TmSegmentDeleteHandler()
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
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=tm");
            return;
        }
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        String userId = getUser(session).getUserId();

        String action = (String) p_request.getParameter(TM_ACTION);

        // From session Manger:
        // - get target search locale
        GlobalSightLocale targetLocale = (GlobalSightLocale) sessionMgr
                .getAttribute(TM_TARGET_SEARCH_LOCALE);

        // - get the previous search manager
        SearchReplaceManager manager = (SearchReplaceManager) sessionMgr
                .getAttribute(TM_CONCORDANCE_MANAGER);

        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);

        // - get the initial search results
        TmConcordanceResult searchResults = (TmConcordanceResult) status
                .getResults();

        // - get user selected tu ids where the replacement will occur
        String selectedTuIds[] = p_request
                .getParameterValues(WebAppConstants.TM_REPLACE_SEGMENT_CHKBOX);

        ArrayList deletedTus = null;
        ArrayList notDeletedTus = null;
        
        if(action.equals(TM_ACTION_DELETE_TUV))
        {
            // get Tuvs to delete
            ArrayList trgTuvs = getSelectedTargetTuvs(
                searchResults, selectedTuIds, targetLocale);
            
            // get the list of source and non-source Tuvs
            ArrayList srcTuvs = getSourceTuvs(trgTuvs);
            ArrayList nonSrcTuvs = (srcTuvs.size() == 0
                ? trgTuvs : getNonSourceTuvs(trgTuvs));

            // delete non-source Tuvs
            doDeleteTuvs(getTmForTuvs(nonSrcTuvs), nonSrcTuvs);

            // get Tu list from Tuv list
            deletedTus = getTuList(nonSrcTuvs);
            notDeletedTus = getTuList(srcTuvs);
        }
        else if(action.equals(TM_ACTION_DELETE_TU))
        {
            // get Tus to delete
            ArrayList tus = getSelectedTargetTus(searchResults, selectedTuIds);

            Tm tm = getTmForTus(tus);
            
            // delete Tus
            doDeleteTus(tm, tus);
            
            deletedTus = tus;
            notDeletedTus = new ArrayList();
        }
        
        // save the result for the result screen
        p_request.setAttribute(TM_DELETED_SEGMENTS, deletedTus);
        p_request.setAttribute(TM_NOT_DELETED_SEGMENTS, notDeletedTus);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Private Methods
    //

    private Tm getTmForTus(ArrayList p_tus) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectTMById(
                    ((BaseTmTu) p_tus.get(0)).getTmId(), true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    private Tm getTmForTuvs(ArrayList p_tuvs) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectTMById(
                    ((BaseTmTuv) p_tuvs.get(0)).getTu().getTmId(), true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    private void doDeleteTuvs(Tm p_tm, ArrayList p_tuvs)
            throws EnvoyServletException
    {
        try
        {
            TmCoreManager tmManager = LingServerProxy.getTmCoreManager();
            tmManager.deleteSegmentTmTuvs(p_tm, p_tuvs);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    private void doDeleteTus(Tm p_tm, ArrayList p_tus)
            throws EnvoyServletException
    {
        try
        {
            TmCoreManager tmManager = LingServerProxy.getTmCoreManager();
            tmManager.deleteSegmentTmTus(p_tm, p_tus);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }


    private ArrayList getSelectedTargetTuvs(TmConcordanceResult p_searchResults,
        String[] p_selectedTuIds, GlobalSightLocale p_targetLocale)
    {
        return getTuvs(makeMap(p_searchResults), p_selectedTuIds, p_targetLocale);
    }

	private ArrayList getSelectedTargetTus(TmConcordanceResult p_searchResults,
			String[] p_selectedTuIds)
    {
        return getTus(makeMap(p_searchResults), p_selectedTuIds);
    }

    private ArrayList getTuvs(HashMap p_searchResultsMap, String[] p_tuIds,
            GlobalSightLocale p_targetLocale)
    {
        ArrayList result = new ArrayList();

        SegmentTmTu tu = null;
        SegmentTmTuv tuv = null;

        for (int i = 0, max = p_tuIds.length; i < max; i++)
        {
            if ((tu = (SegmentTmTu) p_searchResultsMap
                    .get(new Long(p_tuIds[i]))) != null)
            {
                result.addAll(tu.getTuvList(p_targetLocale));
            }
        }

        return result;
    }

    private ArrayList getTus(HashMap p_searchResultsMap, String[] p_tuIds)
    {
        ArrayList result = new ArrayList();

        SegmentTmTu tu = null;

        for (int i = 0, max = p_tuIds.length; i < max; i++)
        {
            if ((tu = (SegmentTmTu) p_searchResultsMap
                    .get(new Long(p_tuIds[i]))) != null)
            {
                result.add(tu);
            }
        }

        return result;
    }

    /**
     * Returns a map from [tu id (Long)] to [SegmentTmTu].
     */
    private HashMap<Long, SegmentTmTu> makeMap(TmConcordanceResult p_searchResults)
    {
        HashMap<Long, SegmentTmTu> result = new HashMap<Long, SegmentTmTu>();
        List<SegmentTmTu> tus = p_searchResults.getTus();

        for (int i = 0, max = tus.size(); i < max; i++)
        {
            SegmentTmTu tu = tus.get(i);

            result.put(new Long(tu.getId()), tu);
        }

        return result;
    }

    private ArrayList getSourceTuvs(ArrayList p_tuvs)
    {
        return separeteSrcAndNonSrcTuvs(p_tuvs, true);
    }

    private ArrayList getNonSourceTuvs(ArrayList p_tuvs)
    {
        return separeteSrcAndNonSrcTuvs(p_tuvs, false);
    }

    private ArrayList separeteSrcAndNonSrcTuvs(ArrayList p_tuvs,
            boolean p_getSource)
    {
        ArrayList result = new ArrayList();

        for (Iterator it = p_tuvs.iterator(); it.hasNext();)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) it.next();
            if (tuv.isSourceTuv() == p_getSource)
            {
                result.add(tuv);
            }
        }

        return result;
    }

    private ArrayList getTuList(ArrayList p_tuvs)
    {
        TreeSet tus = new TreeSet(new TuComparator());

        for (Iterator it = p_tuvs.iterator(); it.hasNext();)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) it.next();
            tus.add(tuv.getTu());
        }

        return new ArrayList(tus);
    }

    private class TuComparator implements Comparator
    {
        public int compare(Object p_o1, Object p_o2)
        {
            SegmentTmTu tu1 = (SegmentTmTu) p_o1;
            SegmentTmTu tu2 = (SegmentTmTu) p_o2;

            return (int) (tu1.getId() - tu2.getId());
        }
    }

}

package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.StringUtil;

public class TMSearchBrowserHandler extends PageActionHandler
{
    @ActionHandler(action = TM_ACTION_TM_SEARCH, formClass = "")
    public void tmSearch(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession(false);
        ResourceBundle bundle = PageHandler.getBundle(session);
        String userId = getUser(session).getUserId();

        TMSearchBroswerHandlerHelper.setLable(request, bundle);
        // set locales
        TMSearchBroswerHandlerHelper.setLocales(request);
        // set tms
        TMSearchBroswerHandlerHelper.setTMs(request, userId);
        // set all TMProfile
        TMSearchBroswerHandlerHelper.setTMProfiles(request, userId);
        // Set permission for User
        TMSearchBroswerHandlerHelper.setPermission(request);

        // From term search, for search condition share
        String fromTermSearchPage = (String) request
                .getParameter("fromTermSearchPage");
        if (fromTermSearchPage != null)
        {
            request.setAttribute("fromTermSearchPage", fromTermSearchPage);
            String sourceLocale = (String) request.getParameter("sourceLocale");
            request.setAttribute("sourceLocale", sourceLocale);
            String targetLocale = (String) request.getParameter("targetLocale");
            request.setAttribute("targetLocale", targetLocale);
            String searchText = (String) request.getParameter("searchText");
            searchText = iso88591ToUtf8(searchText);
            request.setAttribute("searchText", searchText);
        }
    }

    @ActionHandler(action = TM_ACTION_SEARCH, formClass = "")
    public void search(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String searchType = (String) request.getParameter("searchType");
        String searchText = (String) request.getParameter("searchText");
        String sourceLocale = (String) request.getParameter("sourceLocale");
        String targetLocale = (String) request.getParameter("targetLocale");
        String tms = (String) request.getParameter("tms");
        String tmps = (String) request.getParameter("tmps");
        Locale uiLocale = (Locale) request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        String maxEntriesPerPageStr = (String) request
                .getParameter("maxEntriesPerPage");
        String searchIn = (String) request.getParameter("searchIn");
        boolean advancedSearch = "true".equals((String) request
                .getParameter("advancedSearch"));
        String replaceText = null;
        if (advancedSearch)
        {
            // Advanced search for replace
            replaceText = (String) request.getParameter("replaceText");
            if ("".equals(replaceText))
            {
                replaceText = null;
            }
        }
        String searchResult = "";
        if ("matchSearch".equals(searchType))
        {
            // Exact fuzzy search
            searchResult = TMSearchBroswerHandlerHelper.searchExact(request,
                    searchText, sourceLocale, targetLocale, tmps, uiLocale,
                    maxEntriesPerPageStr, searchIn, replaceText);
        }
        else
        {
            // Full text search
            tms = tms.substring(tms.indexOf('"') + 1, tms.lastIndexOf('"'));
            String sids = (String) request.getParameter("sids");
			if (StringUtil.isNotEmpty(sids) && StringUtil.isEmpty(searchText))
			{
				searchResult = TMSearchBroswerHandlerHelper.searchFullTextBySid(request,
						searchText, sourceLocale, targetLocale, tms, uiLocale,
						maxEntriesPerPageStr, searchIn, replaceText);
			}
			else
			{
				searchResult = TMSearchBroswerHandlerHelper.searchFullText(request,
						searchText, sourceLocale, targetLocale, tms, uiLocale,
						maxEntriesPerPageStr, searchIn, replaceText);
			}
        }

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(searchResult);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_REFRESH_PAGE, formClass = "")
    public void refreshPage(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // For paging
        String result = TMSearchBroswerHandlerHelper.refreshPage(request);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();

    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_APPLY_REPLACE, formClass = "")
    public void applyReplaced(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // Apply replaced text
        HttpSession session = request.getSession(false);
        String userId = getUser(session).getUserId();
        String result = TMSearchBroswerHandlerHelper.applyReplaced(request,
                userId);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();

    }

    @ActionHandler(action = WebAppConstants.TM_ACTION_DELETE_ENTRIES, formClass = "")
    public void deleteEntries(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // Delete entries
        String result = TMSearchBroswerHandlerHelper.deleteEntries(request);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(result);
        if (out != null)
        {
            out.close();
        }
        pageReturn();

    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }
}

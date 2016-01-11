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
package com.globalsight.everest.webapp.pagehandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParameter;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.SortUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class PageHandler implements WebAppConstants
{
    private static final Logger s_category = Logger
            .getLogger(PageHandler.class);

    protected boolean isCache = false;

    /**
     * Invokes this PageHandler object.
     * 
     * @param pageDescriptor
     *            the description of the page to be produced
     * @param request
     *            the original request sent from the browser
     * @param response
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        // Populate the links on the search results page.
        Enumeration en = p_pageDescriptor.getLinkNames();

        while (en.hasMoreElements())
        {
            String linkName = (String) en.nextElement();
            String pageName = p_pageDescriptor.getPageName();

            // create a navigation bean for each link
            NavigationBean bean = new NavigationBean(linkName, pageName);

            // each navigation bean will be labelled with the name of
            // the link
            p_request.setAttribute(linkName, bean);
        }

        // turn off cache. do both. "pragma" for the older browsers.
        if (!isCache)
        {
            p_response.setHeader("Pragma", "no-cache"); // HTTP 1.0
            p_response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
            p_response.addHeader("Cache-Control", "no-store"); // tell proxy not
                                                               // to cache
            p_response.addHeader("Cache-Control", "max-age=0"); // stale right
                                                                // away
        }

        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_pageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    /**
     * Invokes this EntryPageHandler object. This is used for applets.
     * 
     * @param p_isDoGet
     *            - Determines whether the request is a get or post.
     * @param p_pageDescriptor
     *            the description of the page to be produced
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            the Servlet context
     * @param p_session
     *            the HTTP session
     * @return A vector of serializable objects to be passed to applet.
     */
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
            WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context,
            HttpSession p_session) throws ServletException, IOException,
            EnvoyServletException
    {
        return null;
    }

    /**
     * Returns an optional object that helps in refining flow of control. This
     * object helps specifying the correct link to follow after the user has
     * left the page associated with this page handler.
     * 
     * @return By default returns null.
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return null;
    }

    /**
     * Remove all session variables except those used for paging/sorting of
     * tables.
     */
    public void clearSessionExceptTableInfo(HttpSession p_session, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        Integer sortType = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.SORTING);
        Boolean reverseSort = (Boolean) sessionMgr.getAttribute(p_key
                + TableConstants.REVERSE_SORT);
        Integer lastPage = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.LAST_PAGE_NUM);
        sessionMgr.clear();
        sessionMgr.setAttribute(p_key + TableConstants.SORTING, sortType);
        sessionMgr.setAttribute(p_key + TableConstants.REVERSE_SORT,
                reverseSort);
        sessionMgr.setAttribute(p_key + TableConstants.LAST_PAGE_NUM, lastPage);
    }

    /**
     * Remove all session variables having to do with table.
     */
    public void clearSessionOfTableInfo(HttpSession p_session, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        sessionMgr.removeElement(p_key + TableConstants.SORTING);
        sessionMgr.removeElement(p_key + TableConstants.REVERSE_SORT);
        sessionMgr.removeElement(p_key + TableConstants.LAST_PAGE_NUM);

    }

    /**
     * Given a list of data, this method sorts it and creates a sublist to be
     * displayed in a jsp.
     * 
     * @param data
     *            Data for the table
     * @param comp
     *            Comparator for sorting table data
     * @param numItemsDisplayed
     *            Number of displayed items per page
     * @param listname
     *            A name for the list to be used in the jsp (via useBean)
     * @param key
     *            A unique id which is used to pass hidden data between here and
     *            jsp so the jsp knows which column is sorted, etc.
     */
    public void setTableNavigation(HttpServletRequest request,
            HttpSession session, List data, StringComparator comp,
            int numItemsDisplayed, String listname, String key)
            throws EnvoyServletException
    {
        setTableNavigation(request, session, data, comp, numItemsDisplayed, key
                + TableConstants.NUM_PER_PAGE_STR, key
                + TableConstants.NUM_PAGES, listname, key
                + TableConstants.SORTING, key + TableConstants.REVERSE_SORT,
                key + TableConstants.PAGE_NUM, key
                        + TableConstants.LAST_PAGE_NUM, key
                        + TableConstants.LIST_SIZE, key
                        + TableConstants.DO_SORT);
    }

    /*
     * Set request and session information needed in the UI for displaying the
     * navigation of tables. ie: Displaying 1-10 of 15
     */
    public void setTableNavigation(HttpServletRequest request,
            HttpSession session, List data, StringComparator comp,
            int numItemsDisplayed, String numPerPageStr, String numOfPagesStr,
            String listStr, String thisSortChoiceStr, String reverseSortStr,
            String thisPageNumStr, String lastPageNumStr, String sizeStr)
            throws EnvoyServletException
    {
        setTableNavigation(request, session, data, comp, numItemsDisplayed,
                numPerPageStr, numOfPagesStr, listStr, thisSortChoiceStr,
                reverseSortStr, thisPageNumStr, lastPageNumStr, sizeStr,
                "doSort");
    }

    /*
     * Set request and session information needed in the UI for displaying the
     * navigation of tables. ie: Displaying 1-10 of 15
     */
    public void setTableNavigation(HttpServletRequest request,
            HttpSession session, List data, StringComparator comp,
            int numItemsDisplayed, String numPerPageStr, String numOfPagesStr,
            String listStr, String thisSortChoiceStr, String reverseSortStr,
            String thisPageNumStr, String lastPageNumStr, String sizeStr,
            String doSortStr) throws EnvoyServletException
    {
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        int numOfPages = getNumOfPages(data.size(), numItemsDisplayed);

        Integer lastPageNumber = (Integer) sessionManager
                .getAttribute(lastPageNumStr);
        String pageStr = (String) request.getParameter(thisPageNumStr);

        int pageNum = 1;
        if (pageStr != null)
        {
            pageNum = Integer.parseInt(pageStr);
        }
        else if (lastPageNumber != null)
        {
            pageNum = lastPageNumber.intValue();
        }

        // GBS-1322 problem (4).
        // Page number will be set to previous or no result page if removing the
        // record which is the only one in current page.
        int size = 0;
        if (data != null && !data.isEmpty())
        {
            size = data.size();
            if ((size % numItemsDisplayed == 0)
                    && (pageNum * numItemsDisplayed > size))
            {
                pageNum--;
                if (pageNum == 0)
                {
                    pageNum = 1;
                }
            }
        }
        if (pageNum > numOfPages)
        {
            // pageNum = numOfPages;
            pageNum = 1;
        }

        String sortType = (String) request.getParameter(thisSortChoiceStr);
        int sortChoice = 0;
        if (sortType == null)
        {
            Integer sortTypeInt = (Integer) sessionManager
                    .getAttribute(thisSortChoiceStr);
            if (sortTypeInt != null)
                sortType = sortTypeInt.toString();
        }
        if (sortType != null)
        {
            sortChoice = Integer.parseInt(sortType);
        }
        Boolean reverseSort = Boolean.FALSE;

        // Compare to the last sort choice. If the sort choice has changed,
        // then kick them back to page one

        Integer lastSortChoice = (Integer) sessionManager
                .getAttribute(thisSortChoiceStr);

        Integer currentSortChoice = new Integer(sortChoice);
        reverseSort = (Boolean) sessionManager.getAttribute(reverseSortStr);

        if (reverseSort == null)
        {
            reverseSort = Boolean.FALSE;
            sessionManager.setAttribute(reverseSortStr, reverseSort);
        }
        Integer currentPageNumber = new Integer(pageNum);
        if (lastPageNumber == null)
        {
            lastPageNumber = currentPageNumber;
            sessionManager.setAttribute(lastPageNumStr, lastPageNumber);
        }

        // "doSort" should be passed in the url on column headers. This
        // is so that when another button on that page returns to the same
        // page, it keeps the same sort, rather than reversing it.
        String doSort = (String) request.getParameter(doSortStr);
        if (lastSortChoice == null)
        {
            sessionManager.setAttribute(thisSortChoiceStr, currentSortChoice);
        }
        else if (doSort != null)
        {
            // see if the user stayed on the same page and
            // clicked a sort column header
            if (lastSortChoice.equals(currentSortChoice))
            {
                // flip the sort direction (no auto refresh on this page)
                // if they clicked the same link again on the same page
                if (lastPageNumber.equals(currentPageNumber))
                {
                    reverseSort = new Boolean(!reverseSort.booleanValue());
                    sessionManager.setAttribute(reverseSortStr, reverseSort);
                }
            }
            else
            {
                reverseSort = Boolean.FALSE;
                sessionManager.setAttribute(reverseSortStr, reverseSort);
            }
            sessionManager.setAttribute(thisSortChoiceStr, currentSortChoice);
        }

        List subList = null;
        if (data != null && !data.isEmpty())
        {
            try
            {
                if (comp != null)
                {
                    comp.setType(sortChoice);
                    SortUtil.sort(data, comp);
                    if (reverseSort.booleanValue())
                    {
                        Collections.reverse(data);
                    }
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            size = data.size();
            if (size > numItemsDisplayed)
            {
                int start = getStartIndex(pageNum, size, numItemsDisplayed);
                int end = getEndingIndex(pageNum, size, numItemsDisplayed);
                subList = data.subList(start, end);
            }
            else
            {
                subList = data;
            }
        }

        if (subList == null)
            request.setAttribute(listStr, new ArrayList());
        else
            request.setAttribute(listStr, new ArrayList(subList));
        request.setAttribute(thisPageNumStr, new Integer(pageNum));
        request.setAttribute(numOfPagesStr, new Integer(numOfPages));
        request.setAttribute(numPerPageStr, new Integer(numItemsDisplayed));
        request.setAttribute(sizeStr, new Integer(size));

        // remember the sortChoice and pageNumber
        int current = currentPageNumber.intValue();
        if (current > numOfPages && numOfPages != 0)
        {
            sessionManager.setAttribute(lastPageNumStr,
                    new Integer(current - 1));
        }
        else
            sessionManager.setAttribute(lastPageNumStr, currentPageNumber);

    }

    // get total number of pages that can be displayed.
    private int getNumOfPages(int numOfItems, int perPage)
    {
        if (perPage == 0)
        {
            return perPage;
        }
        // List of templates
        int remainder = numOfItems % perPage;

        return remainder == 0 ? (numOfItems / perPage)
                : ((numOfItems - remainder) / perPage) + 1;
    }

    // get the start index of the collection (this is inclusive)
    private int getStartIndex(int pageNum, int collectionSize, int perPage)
    {
        int startIndex = (pageNum - 1) * perPage;
        startIndex = (startIndex < collectionSize) ? startIndex
                : collectionSize;

        startIndex = (startIndex > 0) ? startIndex : 0;

        return startIndex;
    }

    // get the ending index for this page (this is exclusive).
    private int getEndingIndex(int pageNum, int collectionSize, int perPage)
    {
        int endIndex = pageNum * perPage;
        endIndex = (endIndex < collectionSize) ? endIndex : collectionSize;

        endIndex = (endIndex > 0) ? endIndex : 0;
        return endIndex;
    }

    /**
     * Returns the JSP Page to use as the Error Page if an error happens when
     * using this PageHandler.
     */
    public String getErrorPage()
    {
        return WebAppConstants.ERROR_PAGE;
    }

    /**
     * Returns the resource bundle for the default locale of the user.
     * 
     * @param p_session
     *            The HTTP session.
     * @return ResourceBundle.
     */
    public static ResourceBundle getBundle(HttpSession p_session)
    {
        ResourceBundle rb;
        SystemResourceBundle srb = SystemResourceBundle.getInstance();

        // if session has a valid locale, use it
        if (p_session != null
                && p_session.getAttribute(WebAppConstants.UILOCALE) != null)
        {
            rb = srb.getResourceBundle(
                    ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                    (Locale) p_session.getAttribute(WebAppConstants.UILOCALE));
        }
        // otherwise, use default locale
        else
        {
            rb = srb.getResourceBundle(
                    ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                    Locale.getDefault());
        }
        return rb;
    }

    /**
     * Returns the resource bundle for the point locale.
     * 
     * @param p_session
     *            The HTTP session.
     * @return ResourceBundle.
     */
    public static ResourceBundle getBundleByLocale(String locale)
    {
        ResourceBundle rb;
        SystemResourceBundle srb = SystemResourceBundle.getInstance();

        try
        {
            Locale l = com.globalsight.util.GlobalSightLocale
                    .makeLocaleFromString(locale);
            rb = srb.getResourceBundle(
                    ResourceBundleConstants.LOCALE_RESOURCE_NAME, l);
        }
        catch (Exception e)
        {
            rb = srb.getResourceBundle(
                    ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                    Locale.getDefault());
        }

        return rb;
    }

    /**
     * Returns the user of the system.
     * 
     * @param p_session
     *            The HTTP session.
     */
    public static User getUser(HttpSession p_session)
    {
        SessionManager sessionManager = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        if (sessionManager == null)
        {
            return null;
        }

        return (User) sessionManager.getAttribute(WebAppConstants.USER);
    }

    /**
     * Retrieves the UI Locale as a String.
     */
    public static String getUILocaleAsString(HttpSession p_session)
    {
        Locale uilocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        return uilocale.toString();
    }

    /**
     * Constructs a Locale object from a locale string (en_US_VARIANT).
     * 
     * @see com.globalsight.util.resourcebundle.LocaleWrapper.
     */
    public static Locale getUILocale(String s)
    {
        String language = "";
        String country = "";
        String variant = "";

        StringTokenizer st = new StringTokenizer(s, "_");

        // language
        if (st.hasMoreTokens())
        {
            language = st.nextToken();
        }

        // country
        if (st.hasMoreTokens())
        {
            country = st.nextToken();
        }

        // variant
        if (st.hasMoreTokens())
        {
            variant = st.nextToken();
        }

        return new Locale(language, country, variant);
    }

    /**
     * Retrieves a user parameter object from the session.
     * 
     * Parameters are set by pagehandler/login/EntryPageControlFlowHelper.
     * 
     * @return UserParameter object or null if it doesn't exist.
     */
    public static UserParameter getUserParameter(HttpSession p_session,
            String p_name)
    {
        HashMap params = (HashMap) p_session
                .getAttribute(WebAppConstants.USER_PARAMS);

        return (UserParameter) params.get(p_name);
    }

    /**
     * Sets a user parameter object in the session.
     * 
     * Initially, parameters are set by
     * pagehandler/login/EntryPageControlFlowHelper.
     * 
     * @return UserParameter object or null if it doesn't exist.
     */
    public static void setUserParameter(HttpSession p_session,
            UserParameter p_param)
    {
        HashMap params = (HashMap) p_session
                .getAttribute(WebAppConstants.USER_PARAMS);

        params.put(p_param.getName(), p_param);
    }

    /**
     * Retrieves the Meta Tag in order to refresh the page based on the
     * refreshUrl passed in and the values of the cap.refresh properties in
     * envoy.properties. The refreshUrl is the URL to go to.
     */
    public static String getRefreshMetaTag(String p_refreshUrl)
    {
        StringBuffer refreshMetaTag = new StringBuffer();

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean doRefresh = sc
                    .getBooleanParameter(SystemConfigParamNames.REFRESH_UI_LISTS);
            int refreshRate = sc
                    .getIntParameter(SystemConfigParamNames.REFRESH_RATE);

            if (doRefresh)
            {
                refreshMetaTag
                        .append("<meta http-equiv=\"refresh\" content=\"");
                refreshMetaTag.append(refreshRate);
                refreshMetaTag.append("; URL=");
                refreshMetaTag.append(p_refreshUrl);
                refreshMetaTag.append("\">");
            }
        }
        catch (Exception e)
        {
            s_category.error(e.getMessage(), e);
        }

        return refreshMetaTag.toString();
    }

    /**
     * Retrieves the Meta Tag in order to refresh the progress bar based on the
     * refreshUrl passed in and the values of the cap.refreshProgress and
     * cap.refrechProgressRate properties in envoy.properties. The refreshUrl is
     * the URL to go to.
     */
    public static String getRefreshMetaTagForProgressBar(String p_refreshUrl)
    {
        StringBuffer refreshMetaTag = new StringBuffer();

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean doRefresh = sc
                    .getBooleanParameter(SystemConfigParamNames.REFRESH_PROGRESS);
            int refreshRate = sc
                    .getIntParameter(SystemConfigParamNames.PROGRESS_REFRESH_RATE);

            if (doRefresh)
            {
                refreshMetaTag
                        .append("<meta http-equiv=\"refresh\" content=\"");
                refreshMetaTag.append(refreshRate);
                refreshMetaTag.append("; URL=");
                refreshMetaTag.append(p_refreshUrl);
                refreshMetaTag.append("\">");
            }
        }
        catch (Exception e)
        {
            s_category.error(e.getMessage(), e);
        }

        return refreshMetaTag.toString();
    }

    //
    // PROTECTED SUPPORT METHODS
    //

    /**
     * Converts the given Collection into a Vector for use by applets
     * 
     * @param p_collection
     *            the original instance of a java.util.Collection
     * 
     * @return an instance of java.util.Vector containing the elements of the
     *         original collection
     */
    protected Vector vectorizedCollection(Collection p_collection)
    {
        return p_collection == null ? new Vector() : new Vector(p_collection);
    }

    /**
     * Converts the given Collection into a sorted Vector for use by applets The
     * sorting is the natural order of the elements
     * 
     * @param p_collection
     *            the original instance of a java.util.Collection
     * 
     * @return an instance of java.util.Vector containing the elements of the
     *         original collection
     */
    protected Vector sortedVectorizedCollection(Collection p_collection)
    {
        if (p_collection == null)
            return new Vector();

        ArrayList al;
        if (p_collection instanceof ArrayList)
            al = (ArrayList) p_collection;
        else
            al = new ArrayList(p_collection);
        SortUtil.sort(al);
        return new Vector(al);
    }

    /**
     * Converts the given Collection into a sorted Vector for use by applets
     * sorted by the given comparator
     * 
     * @param p_collection
     *            the original instance of a java.util.Collection
     * 
     * @return an instance of java.util.Vector containing the elements of the
     *         original collection
     */
    protected Vector sortedVectorizedCollection(Collection p_collection,
            Comparator p_comparator)
    {
        if (p_collection == null)
            return new Vector();

        ArrayList al;
        if (p_collection instanceof ArrayList)
            al = (ArrayList) p_collection;
        else
            al = new ArrayList(p_collection);
        SortUtil.sort(al, p_comparator);
        return new Vector(al);
    }

    /**
     * Dump request parameters
     */
    protected void dumpParameters(HttpServletRequest p_request)
    {
        s_category.error("\n\nRequest Parameters\n\n");

        Enumeration enumeration = p_request.getParameterNames();
        while (enumeration.hasMoreElements())
        {
            String name = (String) enumeration.nextElement();
            String values[] = p_request.getParameterValues(name);

            if (values != null)
            {
                for (int i = 0; i < values.length; i++)
                {
                    System.err.println(name + "(" + i + "): " + values[i]);
                }
            }
        }
    }

    /**
     * Save named parameter from Http request object into SessionManager.
     */
    protected void saveParameterToSession(HttpServletRequest p_request,
            SessionManager p_sessionMgr, String p_paramName)
    {
        if (p_request.getParameter(p_paramName) != null)
        {
            p_sessionMgr.setAttribute(p_paramName,
                    p_request.getParameter(p_paramName));
        }
    }

    /**
     * Save named parameter which contains locale specific values from Http
     * request object into SessionManager.
     */
    protected void saveUTFParameterToSession(HttpServletRequest p_request,
            SessionManager p_sessionMgr, String p_paramName)
    {
        if (p_request.getParameter(p_paramName) != null)
        {
            p_sessionMgr.setAttribute(p_paramName,
                    p_request.getParameter(p_paramName));
        }
    }

    /**
     * Remove named parameter from SessionManager, effectively clearing it.
     */
    protected void removeParameterFromSession(SessionManager p_sessionMgr,
            String p_paramName)
    {
        if (p_sessionMgr.getAttribute(p_paramName) != null)
            p_sessionMgr.removeElement(p_paramName);
    }

    /**
     * If the person is not a Workflow owner, then this method returns true.
     * 
     * @param p_userId
     * @param p_perms
     * @param p_workflow
     * @return
     */
    public static boolean invalidForWorkflowOwner(String p_userId,
            PermissionSet p_perms, Workflow p_workflow)
    {
        boolean isInvalid = true;
        if (p_perms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS)
                || p_perms.getPermissionFor(Permission.PROJECTS_MANAGE))
        {
            List<String> wfOwners = p_workflow.getWorkflowOwnerIds();
            boolean isWorkflowOwner = wfOwners.contains(p_userId);
            if (isWorkflowOwner)
                isInvalid = false;
            else
                isInvalid = true;
        }
        return isInvalid;
    }

    /*
     * Checks whether this is a refresh operation.
     */
    public boolean isRefresh(SessionManager sessionMgr, String p_value,
            String p_param)
    {
        String value = (String) sessionMgr.getAttribute(p_param);
        if (value != null && value.equals(p_value))
        {
            return true;
        }

        return false;
    }

    public static boolean isInContextMatch(Request request)
    {
        if (request == null)
        {
            return false;
        }

        FileProfile fileProfile = ServerProxy.getRequestHandler()
                .getFileProfile(request);
        TranslationMemoryProfile tmProfile = request.getL10nProfile()
                .getTranslationMemoryProfile();

        return isInContextMatch(fileProfile, tmProfile);
    }

    public static boolean isInContextMatch(FileProfile fileProfile,
            TranslationMemoryProfile tmProfile)
    {
        if (fileProfile == null || tmProfile == null)
        {
            return false;
        }

        long knowFormatId = fileProfile.getKnownFormatTypeId();
        KnownFormatType kf = null;
        try
        {
            kf = ServerProxy.getFileProfilePersistenceManager()
                    .getKnownFormatTypeById(knowFormatId, false);
        }
        catch (Exception e)
        {
            s_category.error("Can not get the value of known format type", e);
        }

        if ("xml".equalsIgnoreCase(kf.getFormatType()))
        {
            if (fileProfile.getXmlRuleId() > 0L)
            {
                return true;
            }
            else
            {
                return tmProfile.getIsContextMatchLeveraging();
            }
        }
        else
        {
            if (kf.getFormatType().startsWith("javaprop"))
            {
                if (fileProfile.supportsSid())
                {
                    return true;
                }
                else
                {
                    return tmProfile.getIsContextMatchLeveraging();
                }
            }
            else
            {
                return tmProfile.getIsContextMatchLeveraging();
            }
        }
    }

	public static boolean isInContextMatch(Job job)
	{
		if (job == null)
		{
			return false;
		}

		try
		{
			job = ServerProxy.getJobHandler().getJobById(job.getId());
			if (Job.IN_CONTEXT.equals(job.getLeverageOption()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			s_category.error("Can not get job:", e);
		}

		return true;
	}

    /**
     * Set cache control header for https download
     * 
     * @param p_response
     */
    public static void setHeaderForHTTPSDownload(HttpServletResponse p_response)
    {
        p_response.setHeader("Expires", "0");
        p_response.setHeader("Pragma", "public");
        p_response.setHeader("Cache-Control",
                "must-revalidate, max-age=0, post-check=0, pre-check=0");
        p_response.setHeader("Cache-Control", "public");
    }

    protected boolean isPost(HttpServletRequest p_request)
    {
        return "POST".equals(p_request.getMethod());
    }

    protected String iso88591ToUtf8(String oldValue)
            throws UnsupportedEncodingException
    {
        return new String(oldValue.getBytes("ISO-8859-1"), "UTF-8");
    }
}

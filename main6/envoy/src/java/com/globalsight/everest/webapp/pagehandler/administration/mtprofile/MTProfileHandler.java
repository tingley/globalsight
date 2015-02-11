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
package com.globalsight.everest.webapp.pagehandler.administration.mtprofile;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.MTProfileComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.util.StringUtil;

/**
 * mtProfileHandler is the page handler responsible for displaying a list of tm
 * profiles and perform actions supported by the UI (JSP).
 */

public class MTProfileHandler extends PageHandler
{

    // non user related state
    private int num_per_page; // number of tm profiles per page
    private static final Logger CATEGORY = Logger
            .getLogger(MTProfileHandler.class);
    private static final String COMMA = ",";
    public static final String MTPS_LIST = "mtProfiles";
    public static final String MTP_KEY = "mtProfile";

    public MTProfileHandler()
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
        SessionManager sessionManager = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);
        sessionManager.removeElement(MTProfileConstants.MT_PROFILE);
        String action = (String) p_request
                .getParameter(MTProfileConstants.ACTION);
        if (MTProfileConstants.REMOVE_ACTION.equals(action))
        {
            MachineTranslationProfile mtProfile = getMTProfile(p_request,
                    p_response);
            if (null == mtProfile)
                return;
            removeMT(p_request, mtProfile);
        }
        if (MTProfileConstants.MT_ACTIVE_ACTION.equals(action))
        {
            MachineTranslationProfile mtProfile = getMTProfile(p_request,
                    p_response);
            if (null == mtProfile)
                return;
            active(p_request, mtProfile);
        }
        StringBuffer condition = new StringBuffer();
        String[][] array = new String[][]
        {
        { MTProfileConstants.FILTER_NAME, "mtp.MT_PROFILE_NAME" } };

        for (int i = 0; i < array.length; i++)
        {
            makeCondition(p_request, sessionManager, condition, array[i][0],
                    array[i][1]);
        }

        selectTMProfilesForDisplay(p_request, sess, condition.toString());

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void active(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        MTProfileHandlerHelper.activeMTProfile(mtProfile);

    }

    private MachineTranslationProfile getMTProfile(
            HttpServletRequest p_request, HttpServletResponse p_response)
            throws IOException
    {
        String id = (String) p_request
                .getParameter(MTProfileConstants.MT_PROFILE_ID);
        if (id == null
                || p_request.getMethod().equalsIgnoreCase(
                        WebAppConstants.REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=mtProfiles");
            return null;
        }

        if (id != null)
        {
            long mtProfileId = -1;
            try
            {
                mtProfileId = Long.parseLong(id);

            }
            catch (NumberFormatException nfe)
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=mtProfiles");
                return null;
            }
            MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                    .getMTProfileById(id);
            return mtProfile;

        }
        return null;

    }

    private void removeMT(HttpServletRequest p_request,
            MachineTranslationProfile mtProfile)
    {
        String promt = MTProfileHandlerHelper.isAble2Delete(mtProfile.getId());
        if (promt != null)
        {
            p_request
                    .setAttribute(
                            "exception",
                            "Mt profile is refered to by the following localization profiles. Please deselect this Mt profile from the localization profiles before removing it."
                                    + promt);
        }
        else
        {
            MTProfileHandlerHelper.removeMTProfile(mtProfile);
            clearSessionExceptTableInfo(p_request.getSession(false), MTP_KEY);
        }

    }

    private void makeCondition(HttpServletRequest p_request,
            SessionManager sessionMgr, StringBuffer condition, String par,
            String sqlparam)
    {
        String uNameFilter = (String) (p_request.getParameter(par) == null ? sessionMgr
                .getAttribute(par) : p_request.getParameter(par));
        if (StringUtils.isNotBlank(uNameFilter))
        {
            sessionMgr.setAttribute(par, uNameFilter);
            condition.append(" and  " + sqlparam + " LIKE '%"
                    + StringUtil.transactSQLInjection(uNameFilter.trim())
                    + "%'");
        }
        else
        {
            sessionMgr.setAttribute(par, "");
        }
    }

    private void selectTMProfilesForDisplay(HttpServletRequest p_request,
            HttpSession p_session, String condition) throws ServletException,
            IOException, EnvoyServletException
    {
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        List<MachineTranslationProfile> mtProfiles = null;
        try
        {
            mtProfiles = (List<MachineTranslationProfile>) MTProfileHandlerHelper
                    .getAllMTProfiles(condition);
            String par = MTProfileConstants.FILTER_COMPANY_NAME;
            String filterCompanyValue = (String) (p_request.getParameter(par) == null ? getSessionManager(
                    p_request).getAttribute(par) : p_request.getParameter(par));
            if (StringUtil.isEmpty(filterCompanyValue))
            {
                filterCompanyValue = "";
            }
            else
            {
                for (Iterator iter = mtProfiles.iterator(); iter.hasNext();)
                {
                    MachineTranslationProfile mt = (MachineTranslationProfile) iter
                            .next();
                    if (!CompanyWrapper.getCompanyNameById(mt.getCompanyid())
                            .contains(filterCompanyValue))
                    {
                        iter.remove();
                    }
                }

            }
            // num_per_page
            determineNumPerPage(p_request);
            getSessionManager(p_request).setAttribute(par, filterCompanyValue);
            setTableNavigation(p_request, p_session, mtProfiles,
                    new MTProfileComparator(uiLocale), num_per_page, MTPS_LIST,
                    MTP_KEY);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    private void determineNumPerPage(HttpServletRequest request)
    {
        SessionManager sessionMgr = getSessionManager(request);
        String mtProfileNumPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(mtProfileNumPerPage))
        {
            mtProfileNumPerPage = (String) sessionMgr
                    .getAttribute("mtProfileNumPerPage");
        }

        if (mtProfileNumPerPage != null)
        {
            sessionMgr.setAttribute("mtProfileNumPerPage",
                    mtProfileNumPerPage.trim());
            if ("all".equalsIgnoreCase(mtProfileNumPerPage))
            {
                num_per_page = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    num_per_page = Integer.parseInt(mtProfileNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    num_per_page = 10;
                }
            }
        }
    }

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

    private SessionManager getSessionManager(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        return sessionMgr;
    }
}

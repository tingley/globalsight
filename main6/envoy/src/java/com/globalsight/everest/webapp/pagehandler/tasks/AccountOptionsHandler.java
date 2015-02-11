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
package com.globalsight.everest.webapp.pagehandler.tasks;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.mediasurface.CmsUserInfo;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class AccountOptionsHandler
    extends PageHandler
    implements UserParamNames, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AccountOptionsHandler.class);

    protected static boolean s_isParagraphEditorEnabled = false;

    static
    {
        try
        {
            s_isParagraphEditorEnabled =
                EditHelper.isParagraphEditorInstalled();
        }
        catch (Throwable ignore) {}
    }

    /**
     * Invokes this EntryPageHandler object.
     *
     * @param pageDescriptor the description of the page to be produced
     * @param request the original request sent from the browser
     * @param response original response object
     * @param context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        GeneralException exception = null;

        try
        {
            if (p_request.getParameter("__save") != null)
            {
                // this will just save to hash table
                saveOptions(session, p_request);
            }
            else
            {
                TaskHelper.saveBasicInformation(session, p_request);
            }

            getParameters(session, p_request);

            // null error means everything ok
            p_request.setAttribute(USER_PARAMS_ERROR, "");

            p_request.setAttribute(WebAppConstants.PARAGRAPH_EDITOR,
                s_isParagraphEditorEnabled ? "true" : "false");
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot read user parameters", ex);

            exception = new GeneralException(
                "cannot read user parameters", ex);
        }
        finally
        {
            if (exception != null)
            {
                // string means show error message
                p_request.setAttribute(USER_PARAMS_ERROR,
                    exception.getTopLevelMessage() + "@@@@@" +
                    GeneralException.getStackTraceString(exception));
            }
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }


    private void getParameters(HttpSession p_session,
        HttpServletRequest p_request)
        throws EnvoyServletException
    {
        p_request.setAttribute(PAGENAME_DISPLAY,
            PageHandler.getUserParameter(
                p_session, PAGENAME_DISPLAY).getValue());

        p_request.setAttribute(EDITOR_SELECTION,
            PageHandler.getUserParameter(
                p_session, EDITOR_SELECTION).getValue());
        
        p_request.setAttribute(EDITOR_SEGMENTS_MAX_NUM,
                PageHandler.getUserParameter(
                    p_session, EDITOR_SEGMENTS_MAX_NUM).getValue());

        p_request.setAttribute(EDITOR_AUTO_SAVE_SEGMENT,
            PageHandler.getUserParameter(
                p_session, EDITOR_AUTO_SAVE_SEGMENT).getValue());

        p_request.setAttribute(EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT,
                PageHandler.getUserParameter(
                    p_session, EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT).getValue());
        
        p_request.setAttribute(EDITOR_AUTO_UNLOCK,
            PageHandler.getUserParameter(
                p_session, EDITOR_AUTO_UNLOCK).getValue());

        p_request.setAttribute(EDITOR_AUTO_SYNC,
            PageHandler.getUserParameter(
                p_session, EDITOR_AUTO_SYNC).getValue());

        p_request.setAttribute(EDITOR_AUTO_ADJUST_WHITESPACE,
            PageHandler.getUserParameter(
                p_session, EDITOR_AUTO_ADJUST_WHITESPACE).getValue());

        p_request.setAttribute(EDITOR_LAYOUT,
            PageHandler.getUserParameter(
                p_session, EDITOR_LAYOUT).getValue());

        p_request.setAttribute(EDITOR_VIEWMODE,
            PageHandler.getUserParameter(
                p_session, EDITOR_VIEWMODE).getValue());

        p_request.setAttribute(EDITOR_PTAGMODE,
            PageHandler.getUserParameter(
                p_session, EDITOR_PTAGMODE).getValue());

        p_request.setAttribute(TM_MATCHING_THRESHOLD,
            PageHandler.getUserParameter(
                p_session, TM_MATCHING_THRESHOLD).getValue());

        p_request.setAttribute(TB_MATCHING_THRESHOLD,
            PageHandler.getUserParameter(
                p_session, TB_MATCHING_THRESHOLD).getValue());

        p_request.setAttribute(HYPERLINK_COLOR_OVERRIDE,
            PageHandler.getUserParameter(
                p_session, HYPERLINK_COLOR_OVERRIDE).getValue());

        p_request.setAttribute(HYPERLINK_COLOR,
            PageHandler.getUserParameter(
                p_session, HYPERLINK_COLOR).getValue());

        p_request.setAttribute(ACTIVE_HYPERLINK_COLOR,
            PageHandler.getUserParameter(
                p_session, ACTIVE_HYPERLINK_COLOR).getValue());

        p_request.setAttribute(VISITED_HYPERLINK_COLOR,
            PageHandler.getUserParameter(
                p_session, VISITED_HYPERLINK_COLOR).getValue());
        
        p_request.setAttribute(PREVIEW_100MATCH_COLOR,
                PageHandler.getUserParameter(p_session, PREVIEW_100MATCH_COLOR).getValue());

        p_request.setAttribute(PREVIEW_ICEMATCH_COLOR,
                PageHandler.getUserParameter(p_session, PREVIEW_ICEMATCH_COLOR).getValue());

        p_request.setAttribute(PREVIEW_NONMATCH_COLOR,
                PageHandler.getUserParameter(p_session, PREVIEW_NONMATCH_COLOR).getValue());
        
        p_request.setAttribute(EDITOR_SHOW_CLOSEALLCOMMENT,
                PageHandler.getUserParameter(
                    p_session, EDITOR_SHOW_CLOSEALLCOMMENT).getValue());

        loadCmsUserInfo(p_session, p_request);
    }

    /*
     * Get the CMS user info (if any).  This is optional info and will
     * only be used when our CMS is installed.
     */
    private void loadCmsUserInfo(HttpSession p_session, 
                                 HttpServletRequest p_request)
    {
        CmsUserInfo cmsUserInfo = (CmsUserInfo)p_session.
            getAttribute(CMS_USER_INFO);        
        if (cmsUserInfo != null)
        {
            p_request.setAttribute(CMS_USER_NAME, cmsUserInfo.getCmsUserId());
            p_request.setAttribute(CMS_PASSWORD, cmsUserInfo.getCmsPassword());
        }
    }

    private void saveOptions(HttpSession p_session,
        HttpServletRequest p_request)
        throws EnvoyServletException
    {
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        HashMap optionsHash = (HashMap)sessionMgr.getAttribute("optionsHash");
        if (optionsHash == null)
        {
            optionsHash = new HashMap();
            sessionMgr.setAttribute("optionsHash", optionsHash);
        }

        saveCmsOptions(optionsHash, p_request);
        
        optionsHash.put(PAGENAME_DISPLAY,
            p_request.getParameter(PAGENAME_DISPLAY));
        optionsHash.put(EDITOR_SELECTION,
            p_request.getParameter(EDITOR_SELECTION));
        optionsHash.put(EDITOR_SEGMENTS_MAX_NUM,
                p_request.getParameter(EDITOR_SEGMENTS_MAX_NUM));        
        optionsHash.put(EDITOR_AUTO_SAVE_SEGMENT,
            p_request.getParameter(EDITOR_AUTO_SAVE_SEGMENT));
        optionsHash.put(EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT, 
        	p_request.getParameter(EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT));
        optionsHash.put(EDITOR_AUTO_UNLOCK,
            p_request.getParameter(EDITOR_AUTO_UNLOCK));
        optionsHash.put(EDITOR_AUTO_SYNC,
            p_request.getParameter(EDITOR_AUTO_SYNC)); 
        optionsHash.put(EDITOR_AUTO_ADJUST_WHITESPACE,
            p_request.getParameter(EDITOR_AUTO_ADJUST_WHITESPACE));
        optionsHash.put(EDITOR_LAYOUT,
            p_request.getParameter(EDITOR_LAYOUT));
        optionsHash.put(EDITOR_VIEWMODE,
            p_request.getParameter(EDITOR_VIEWMODE));
        optionsHash.put(EDITOR_PTAGMODE,
            p_request.getParameter(EDITOR_PTAGMODE));
        optionsHash.put(TM_MATCHING_THRESHOLD,
            p_request.getParameter(TM_MATCHING_THRESHOLD));
        optionsHash.put(TB_MATCHING_THRESHOLD,
            p_request.getParameter(TB_MATCHING_THRESHOLD));
        optionsHash.put(HYPERLINK_COLOR_OVERRIDE,
            p_request.getParameter(HYPERLINK_COLOR_OVERRIDE));
        optionsHash.put(HYPERLINK_COLOR,
            p_request.getParameter(HYPERLINK_COLOR));
        optionsHash.put(ACTIVE_HYPERLINK_COLOR,
            p_request.getParameter(ACTIVE_HYPERLINK_COLOR));
        optionsHash.put(VISITED_HYPERLINK_COLOR,
            p_request.getParameter(VISITED_HYPERLINK_COLOR));
        optionsHash.put(PREVIEW_100MATCH_COLOR, p_request.getParameter(PREVIEW_100MATCH_COLOR));
        optionsHash.put(PREVIEW_ICEMATCH_COLOR, p_request.getParameter(PREVIEW_ICEMATCH_COLOR));
        optionsHash.put(PREVIEW_NONMATCH_COLOR, p_request.getParameter(PREVIEW_NONMATCH_COLOR));
        optionsHash.put(EDITOR_SHOW_CLOSEALLCOMMENT,
                p_request.getParameter(EDITOR_SHOW_CLOSEALLCOMMENT));
        for (int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
        	.size(); i++)
		{
		    String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
		            .get(i);
		    if(optionsHash.get(downloadOption) == null)
		    {		    	
		    	optionsHash.put(downloadOption,
		    			PageHandler
		    			.getUserParameter(p_session, downloadOption).getValue());
		    }
		}
    }

    /*
     * Store the optional CMS info in the HashMap.
     */
    private void saveCmsOptions(HashMap p_optionsHash, 
                                HttpServletRequest p_request)
    {
        String cmsUsername = (String)p_request.getParameter(CMS_USER_NAME);
        String cmsPassword = (String)p_request.getParameter(CMS_PASSWORD);
        if (cmsUsername != null && cmsUsername.length() > 0)
        {
            p_optionsHash.put(CMS_USER_NAME,
                              cmsUsername);
        }

        if (cmsPassword != null && cmsPassword.length() > 0)
        {
            p_optionsHash.put(CMS_PASSWORD,
                              cmsPassword);
        }        
    }
}

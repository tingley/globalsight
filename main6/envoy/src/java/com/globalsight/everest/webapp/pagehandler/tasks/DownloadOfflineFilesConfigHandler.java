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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;

public class DownloadOfflineFilesConfigHandler extends PageHandler implements
        UserParamNames, WebAppConstants
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(AccountOptionsHandler.class);

    protected static boolean s_isParagraphEditorEnabled = false;
    public static final List<String> DOWNLOAD_OPTIONS = new ArrayList<String>();
    public static final List<String> DOWNLOAD_OPTIONS_DEFAULT = new ArrayList<String>();
    static
    {
        try
        {
            s_isParagraphEditorEnabled = EditHelper
                    .isParagraphEditorInstalled();
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_FORMAT);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITOR);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_ENCODING);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITEXACT);
            DOWNLOAD_OPTIONS
                    .add(UserParamNames.DOWNLOAD_OPTION_DISPLAYEXACTMATCH);
            DOWNLOAD_OPTIONS
                    .add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT);
            DOWNLOAD_OPTIONS
                    .add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM);
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_TERMINOLOGY);
            DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_100);
            DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_FUZZY);
            DOWNLOAD_OPTIONS.add(OfflineConstants.NEED_CONSOLIDATE);

            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_FORMAT_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_EDITOR_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_ENCODING_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_EDITEXACT_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_DISPLAYEXACTMATCH_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
            		.add(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT
                    .add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM_DEFAULT);
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_TERMINOLOGY_DEFAULT);
            
            //populate 100
            DOWNLOAD_OPTIONS_DEFAULT.add("yes");
            
            //populate fuzzy
            DOWNLOAD_OPTIONS_DEFAULT.add("yes");
            
            DOWNLOAD_OPTIONS_DEFAULT.add("no");
        }
        catch (Throwable ignore)
        {
        }
    }

    /**
     * Invokes this EntryPageHandler object.
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
        HttpSession session = p_request.getSession();
        GeneralException exception = null;
        //

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

            exception = new GeneralException("cannot read user parameters", ex);
        }
        finally
        {
            if (exception != null)
            {
                // string means show error message
                p_request.setAttribute(USER_PARAMS_ERROR, exception
                        .getTopLevelMessage()
                        + "@@@@@"
                        + GeneralException.getStackTraceString(exception));
            }
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void getParameters(HttpSession p_session,
            HttpServletRequest p_request) throws EnvoyServletException
    {
        for (int i = 0; i < DOWNLOAD_OPTIONS.size(); i++)
        {
            String downloadOption = DOWNLOAD_OPTIONS.get(i);
            p_request.setAttribute(downloadOption, PageHandler
                    .getUserParameter(p_session, downloadOption).getValue());
        }
    }

    private void saveOptions(HttpSession session, HttpServletRequest request)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        HashMap optionsHash = (HashMap) sessionMgr.getAttribute("optionsHash");
        if (optionsHash == null)
        {
            optionsHash = new HashMap();
            sessionMgr.setAttribute("optionsHash", optionsHash);
        }
        for (int i = 0; i < DOWNLOAD_OPTIONS.size(); i++)
        {
            String downloadOption = DOWNLOAD_OPTIONS.get(i);
            optionsHash.put(downloadOption, request
                    .getParameter(downloadOption));
        }

        String key = UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.POPULATE_100;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.POPULATE_FUZZY;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.NEED_CONSOLIDATE;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
    }

}

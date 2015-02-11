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

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class DownloadOfflineFilesConfigHandler extends PageHandler implements
        UserParamNames, WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(AccountOptionsHandler.class);

    protected static boolean s_isParagraphEditorEnabled = false;
    public static final List<String> DOWNLOAD_OPTIONS = new ArrayList<String>();
    public static final List<String> DOWNLOAD_OPTIONS_DEFAULT = new ArrayList<String>();
    public static HashMap optionsHash;
    static
    {
        try
        {
            s_isParagraphEditorEnabled = EditHelper.isParagraphEditorInstalled();
            
            // NOTES:These constants must be added in sequence!!!!
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_FORMAT);//0
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITOR);//1
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_ENCODING);//2
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER);//3
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT);//4
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITEXACT);//5
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_DISPLAYEXACTMATCH);//6
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX);//7
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM);//8
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_TERMINOLOGY);//9
            DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_100);//10
            DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_FUZZY);//11
            DOWNLOAD_OPTIONS.add(OfflineConstants.NEED_CONSOLIDATE);//12
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT);//13
            DOWNLOAD_OPTIONS.add(OfflineConstants.INCLUDE_REPETITIONS);//14
            DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_TM_EDIT_TYPE);//15

            // NOTES:These constants must be added in sequence!!!!
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_FORMAT_DEFAULT);//0
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_EDITOR_DEFAULT);//1
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_ENCODING_DEFAULT);//2
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER_DEFAULT);//3
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT_DEFAULT);//4
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_EDITEXACT_DEFAULT);//5
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_DISPLAYEXACTMATCH_DEFAULT);//6
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX_DEFAULT);//7
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM_DEFAULT);//8
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_TERMINOLOGY_DEFAULT);//9
            //populate 100
            DOWNLOAD_OPTIONS_DEFAULT.add("yes");//10
            //populate fuzzy target segments (only for bilingual RTF)
            DOWNLOAD_OPTIONS_DEFAULT.add("yes");//11
            //need consolidate output file (for XLF format)
            DOWNLOAD_OPTIONS_DEFAULT.add("no");//12
            DOWNLOAD_OPTIONS_DEFAULT.add(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT_DEFAULT);//13
            DOWNLOAD_OPTIONS_DEFAULT.add("yes");// 14
            DOWNLOAD_OPTIONS_DEFAULT.add(String.valueOf(UserParamNames.DOWNLOAD_OPTION_TM_EDIT_TYPE_DEFAULT));//15
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
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        optionsHash = (HashMap) sessionMgr.getAttribute("optionsHash");
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
            if (optionsHash == null)

            {
                optionsHash = new HashMap();
                getParameters(session, p_request);
                sessionMgr.setAttribute("optionsHash", optionsHash);
            }

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
            optionsHash.put(downloadOption,
                    PageHandler
                    .getUserParameter(p_session, downloadOption).getValue());
        }
    }

    private void saveOptions(HttpSession session, HttpServletRequest request)
            throws EnvoyServletException
    {

        for (int i = 0; i < DOWNLOAD_OPTIONS.size(); i++)
        {
            String downloadOption = DOWNLOAD_OPTIONS.get(i);
            String temp=request.getParameter(downloadOption);
            if (null == temp)
            {
                optionsHash
                        .put(downloadOption, DOWNLOAD_OPTIONS_DEFAULT.get(i));
            }
            else
            {
                optionsHash.put(downloadOption, temp);
            }
        }

        // Below parameters need "yes" or "no" as value,but it is "on" or null
        // from UI,so need convert to overwrite in "optionsHash".
        // String key = UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX;
        // optionsHash.put(key, request.getParameter(key) == null ? "no" :
        // "yes");
        
        String key = UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.POPULATE_100;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.POPULATE_FUZZY;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.NEED_CONSOLIDATE;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
        
        key = OfflineConstants.INCLUDE_REPETITIONS;
        optionsHash.put(key, request.getParameter(key) == null ? "no" : "yes");
    }

}

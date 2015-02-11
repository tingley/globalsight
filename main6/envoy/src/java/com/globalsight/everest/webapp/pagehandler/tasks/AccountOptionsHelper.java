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

import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParamNames;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.mediasurface.CmsUserInfo;
import com.globalsight.util.GeneralException;
import com.globalsight.util.modules.Modules;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class AccountOptionsHelper
    implements UserParamNames, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AccountOptionsHandler.class);

    /**
     * Persists the user's options.
     * (This code was moved from AccountOptionsHandler)
     */
    public static void modifyOptions(HttpSession session,
            HttpServletRequest request, HashMap optionsHash)
    throws  EnvoyServletException
    {
        GeneralException exception = null;

        try
        {
            updateCmsOptions(session, optionsHash);
            setParameters(session, optionsHash);
            setNotificationOptions(session, optionsHash);
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
                request.setAttribute(USER_PARAMS_ERROR,
                    exception.getTopLevelMessage() + "@@@@@" +
                    GeneralException.getStackTraceString(exception));
            }
        }
    }

    /*
     * Update the notification options.
     */
    private static void setNotificationOptions(HttpSession p_session,
        HashMap optionsHash)
        throws EnvoyServletException
    {
        String userName = (String)p_session.getAttribute(
            WebAppConstants.USER_NAME);

        // first set the main notification flag
        setParameter(p_session, optionsHash, userName, NOTIFICATION_ENABLED);

        // now set the options
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        List availableOptions = (List)sessionMgr.getAttribute(
            WebAppConstants.AVAILABLE_NOTIFICATION_OPTIONS);

        if (availableOptions != null)
        {
            int size = availableOptions.size();
            for (int i = 0; i < size; i++)
            {
                String option = (String)availableOptions.get(i);
                setParameter(p_session, optionsHash, userName, option);
            }
        }
    }

    private static void setParameters(HttpSession p_session,
        HashMap optionsHash)
        throws EnvoyServletException
    {
        String userName = (String)p_session.getAttribute(
            WebAppConstants.USER_NAME);

        setParameter(p_session, optionsHash, userName, PAGENAME_DISPLAY);
        setParameter(p_session, optionsHash, userName, EDITOR_SELECTION);
        setParameter(p_session, optionsHash, userName, EDITOR_SEGMENTS_MAX_NUM);
        setParameter(p_session, optionsHash, userName, EDITOR_AUTO_SAVE_SEGMENT);
        setParameter(p_session, optionsHash, userName, EDITOR_AUTO_UNLOCK);
        setParameter(p_session, optionsHash, userName, EDITOR_AUTO_SYNC); 
        setParameter(p_session, optionsHash, userName, EDITOR_AUTO_ADJUST_WHITESPACE);
        setParameter(p_session, optionsHash, userName, EDITOR_LAYOUT);
        setParameter(p_session, optionsHash, userName, EDITOR_VIEWMODE);
        setParameter(p_session, optionsHash, userName, EDITOR_PTAGMODE);
        setParameter(p_session, optionsHash, userName, TM_MATCHING_THRESHOLD);
        setParameter(p_session, optionsHash, userName, TB_MATCHING_THRESHOLD);
        setParameter(p_session, optionsHash, userName, HYPERLINK_COLOR_OVERRIDE);
        setParameter(p_session, optionsHash, userName, HYPERLINK_COLOR);
        setParameter(p_session, optionsHash, userName, ACTIVE_HYPERLINK_COLOR);
        setParameter(p_session, optionsHash, userName, VISITED_HYPERLINK_COLOR);
        setParameter(p_session, optionsHash, userName, EDITOR_SHOW_CLOSEALLCOMMENT);
        for(int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS.size(); i++)
        {
        	String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS.get(i);
        	setParameter(p_session, optionsHash, userName, downloadOption);
        }
    }

    private static void setParameter(HttpSession p_session,
        HashMap optionsHash, String p_userName, String p_name)
        throws EnvoyServletException
    {
        String newValue = (String)optionsHash.get(p_name);

        if (newValue != null)
        {
            UserParameter param = PageHandler.getUserParameter(p_session, p_name);

            param.setValue(newValue);
            param = updateParameter(param);

            PageHandler.setUserParameter(p_session, param);
        }
    }

    private static UserParameter updateParameter(UserParameter p_parameter)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserParameterManager().
                updateUserParameter(p_parameter);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    /*
     * Update the CMS options if both the username and password were
     * entered by the user.  This method will create the CmsUserInfo
     * if the user did not have one.  Otherwise, it'll be updated.
     */
    private static void updateCmsOptions(HttpSession p_session,
                                         HashMap p_optionsHash)
        throws Exception
    {
        if (!Modules.isCmsAdapterInstalled())
        {
            return;
        }

        String cmsUserId = (String)p_optionsHash.get(CMS_USER_NAME);
        String cmsPassword = (String)p_optionsHash.get(CMS_PASSWORD);
        if (cmsUserId == null || cmsUserId.length() == 0 ||
            cmsPassword == null || cmsPassword.length() == 0)
        {
            return;
        }

        CmsUserInfo cmsUserInfo = (CmsUserInfo)p_session.getAttribute(
            WebAppConstants.CMS_USER_INFO);

        if (cmsUserInfo == null)
        {
            cmsUserInfo = ServerProxy.getCmsUserManager().
                createCmsUserInfo(new CmsUserInfo(
                    (String)p_session.getAttribute(
                        WebAppConstants.USER_NAME),
                    cmsUserId, cmsPassword));
        }
        else
        {
            cmsUserInfo.setCmsUserId(cmsUserId);
            cmsUserInfo.setCmsPassword(cmsPassword);
            cmsUserInfo = ServerProxy.getCmsUserManager().
                modifyCmsUserInfo(cmsUserInfo);
        }

        // now put it in the session
        p_session.setAttribute(WebAppConstants.CMS_USER_INFO,
                               cmsUserInfo);
    }
}

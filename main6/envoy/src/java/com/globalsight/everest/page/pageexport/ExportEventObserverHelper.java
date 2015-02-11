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

package com.globalsight.everest.page.pageexport;

//globalsight
import com.globalsight.everest.page.pageexport.PageExportException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.page.pageexport.ExportEventObserver;
import com.globalsight.everest.page.pageexport.ExportEventObserverException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.workflow.EventNotificationHelper;


// java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Helper class
 */
public class ExportEventObserverHelper
{
    // determines whether the system-wide notification is enabled
    static private boolean m_systemNotificationEnabled =
        EventNotificationHelper.systemNotificationEnabled();


    public static long notifyBeginExportSourceBatch(
        Job p_job, User p_user, List p_pageIds, Long p_taskId)
        throws Exception
    {
        ExportEventObserver eeo = ServerProxy.getExportEventObserver();
        return eeo.notifyBeginExportSourceBatch(
            p_job, p_user, p_pageIds, p_taskId);
    }

    public static long notifyBeginExportSourcePage(Job p_job, User p_user,
        long p_pageId, Long p_taskId)
        throws Exception
    {
        ExportEventObserver eeo = ServerProxy.getExportEventObserver();
        return eeo.notifyBeginExportSourcePage(
            p_job, p_user, p_pageId, p_taskId);
    }

    public static long notifyBeginExportTargetBatch(Job p_job, User p_user,
        List p_pageIds, List p_wfIds, Long p_taskId, String p_exportType)
        throws Exception
    {
        ExportEventObserver eeo = ServerProxy.getExportEventObserver();
        return eeo.notifyBeginExportTargetBatch(
            p_job, p_user, p_pageIds, p_wfIds, p_taskId, p_exportType);
    }

    public static long notifyBeginExportTargetPage(Job p_job, User p_user,
        long p_pageId, Long p_wfId, Long p_taskId, String p_exportType)
        throws Exception
    {
        ExportEventObserver eeo = ServerProxy.getExportEventObserver();
        return eeo.notifyBeginExportTargetPage(
            p_job, p_user, p_pageId, p_wfId, p_taskId, p_exportType);
    }

    /**
     * See ExportEventObserver.notifyPageExportComplete()
     */
    public static void notifyPageExportComplete(long p_exportId, String p_pageId,
        HttpServletRequest p_request)
        throws Exception
    {
        ExportEventObserver eeo = ServerProxy.getExportEventObserver();
        eeo.notifyPageExportComplete(p_exportId, p_pageId, p_request);
    }

    public static User getUser(HttpServletRequest m_request)
    {
        HttpSession session = m_request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        return (User)sessionMgr.getAttribute(WebAppConstants.USER);
    }

    public static User getUser(String p_userId)
        throws EnvoyServletException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUser(p_userId);
            return user;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns the target Page.
     * @param p_targetPageId  target page identifier
     */
    static TargetPage getTargetPage(long p_targetPageId)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getPageManager().getTargetPage(p_targetPageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns the source Page.
     * @param p_sourcePageId  target page identifier
     */
    static SourcePage getSourcePage(long p_sourcePageId)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getPageManager().getSourcePage(p_sourcePageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    static void sendEmail(User p_user, String[] p_args, String p_subjectKey,
        String p_messageKey)
        throws EnvoyServletException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            ServerProxy.getMailer().sendMailFromAdmin(
                p_user, p_args, p_subjectKey, p_messageKey);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}

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

package com.globalsight.everest.webapp.pagehandler.offline.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.glossaries.GlossaryManager;
import com.globalsight.everest.localemgr.CodeSet;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.DownloadOfflineFilesConfigHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

/**
 * DownloadPageHandler is responsible for displaying download page and send the
 * download file to the user.
 */
public class DownloadPageHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(DownloadPageHandler.class);
    public static HashMap optionsHash;
    // Constructor
    public DownloadPageHandler()
    {
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_thePageDescriptor
     *            the page desciptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
    	HttpSession httpSession = p_request.getSession();
        String taskId = p_request.getParameter("taskId");
        if(taskId != null && !taskId.equals(""))
        {
        	TaskDetailHelper taskDetailHelper = new TaskDetailHelper();
        	taskDetailHelper.prepareTaskData(p_request, p_response, httpSession, taskId);
        }
        setParameters(p_request);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Gets file name with full path.
     */
    public static String getFileName(String p_page)
    {
        String pageName = getMainFileName(p_page);
        String subName = getSubFileName(p_page);

        if (subName != null)
        {
            pageName = pageName + " " + subName;
        }

        return pageName;
    }

    /**
     * Gets short file name with full path in tooltip.
     */
    public static String getFileNameShort(String p_page)
    {
        String pageName = getMainFileName(p_page);
        String subName = getSubFileName(p_page);
        String shortName = pageName;

        int bslash = shortName.lastIndexOf("\\");
        int fslash = shortName.lastIndexOf("/");
        if (bslash > 0 && bslash > fslash)
        {
            shortName = shortName.substring(bslash + 1);
        }
        else if (fslash > 0 && fslash > bslash)
        {
            shortName = shortName.substring(fslash + 1);
        }

        if (subName != null)
        {
            pageName = pageName + " " + subName;
            shortName = shortName + " " + subName;
        }

        return shortName;
    }

    private static String getMainFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            index++;
            while (Character.isSpace(p_filename.charAt(index)))
            {
                index++;
            }

            return p_filename.substring(index, p_filename.length());
        }

        return p_filename;
    }

    private static String getSubFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            return p_filename.substring(0, p_filename.indexOf(")") + 1);
        }

        return null;
    }

    //
    // Private Methods
    //

    private void setParameters(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Task task = TaskHelper.retrieveMergeObject(session, WORK_OBJECT);
        if (task == null)
        {
            EnvoyServletException e = new EnvoyServletException("TaskNotFound",
                    null, null);
            CATEGORY.error(e.getMessage(), e);
            throw e;
        }

        // HACK: Make sure the Secondary Target Files are loaded
        // before the session is closed.
        task.getWorkflow().getSecondaryTargetFiles().size();

        // do not process download if workflow has been cancelled.
        checkTaskValidation(task);

        // all the encoding names for the target locale
        ArrayList encodingNames = getEncodingNames(task);
        session.setAttribute(OfflineConstants.DOWNLOAD_ENCODING_OPTIONS,
                encodingNames);

        // exact match editing
//        int exactMatchEditing = task.getWorkflow().getJob()
//                .getL10nProfile().getTMEditType();
//        String tmChoice = String.valueOf(task.getWorkflow().getJob()
//                .getL10nProfile().getTmChoice());
//        session.setAttribute(OfflineConstants.DOWNLOAD_EDIT_EXACT,
//                tmChoice);
        
        session.setAttribute(
                OfflineConstants.DOWNLOAD_TM_EDIT_TYPE,
                String.valueOf(task.getWorkflow().getJob().getL10nProfile()
                        .getTMEditType()));

        // The glossary state information for this locale pair
        GlossaryState glossaryState = getGlossaryState(task);
        session.setAttribute(OfflineConstants.DOWNLOAD_GLOSSARY_STATE,
                glossaryState);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        optionsHash = (HashMap) sessionMgr.getAttribute("optionsHash");
        if (optionsHash == null)

        {
            optionsHash = new HashMap();
            setDownloadOptions(session, p_request);
            sessionMgr.setAttribute("optionsHash", optionsHash);
        }
    }

    private void setDownloadOptions(HttpSession p_session,
            HttpServletRequest p_request)
    {
        for (int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                .size(); i++)
        {
            String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                    .get(i);
            optionsHash.put(downloadOption,
                    PageHandler
                    .getUserParameter(p_session, downloadOption).getValue());
        }
    }

    private ArrayList getEncodingNames(Task task) throws EnvoyServletException
    {
        GlobalSightLocale locale = task.getTargetLocale();
        List codesetList = null;
        try
        {
            LocaleManager localeMgr = ServerProxy.getLocaleManager();
            codesetList = localeMgr.getAllCodeSets(locale.getId());
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }

        ArrayList result = new ArrayList();

        for (Iterator it = codesetList.iterator(); it.hasNext();)
        {
            result.add(((CodeSet) it.next()).getCodeSet());
        }

        return result;
    }

    // check to make sure that the workflow has not been cancelled.
    private void checkTaskValidation(Task p_task) throws EnvoyServletException
    {
        String state = null;
        String args[] =
        { p_task.getTaskName() };
        try
        {
            long wfId = p_task.getWorkflow().getId();
            Workflow wf = ServerProxy.getWorkflowManager()
                    .getWorkflowByIdRefresh(wfId);
            state = wf == null ? null : wf.getState();
        }
        catch (Exception e)
        {
            CATEGORY.error("DownloadPageHandler :: checkTaskValidation(). ", e);
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_GET_WORKFLOW, args,
                    null, EnvoyServletException.PROPERTY_FILE_NAME);
        }

        if (state == null || state.equals(Workflow.CANCELLED))
        {
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_DOWNLOAD, args, null,
                    EnvoyServletException.PROPERTY_FILE_NAME);
        }
    }

    /**
     * Builds a gLossaryState object which contains all the parameters needed
     * for glossary selction.
     * 
     * @param p_sourceLocale
     *            get glossaries for this source and target locale
     * @param p_targetLocale
     *            get glossaries for this target and target locale
     * @param p_category
     *            glossray category, used to filter glossaries by subject.
     * @return GlossaryState object
     */
    public GlossaryState getGlossaryState(Task p_task)
            throws EnvoyServletException
    {
        GlossaryState result = new GlossaryState();

        result.setGlossaries(getGlossaries(p_task.getSourceLocale(),
                p_task.getTargetLocale(), null,
                String.valueOf(p_task.getCompanyId())));

        return result;
    }

    /**
     * Wraps the code for getting the GlossaryFiles from the DB and handling any
     * exceptions.
     * 
     * @param p_sourceLocale
     *            get glossaries for this source and target locale
     * @param p_targetLocale
     *            get glossaries for this target and target locale
     * @param p_category
     *            glossray category, used to filter glossaries by subject.
     * @return collection of GlossaryFile objects
     */
    private ArrayList getGlossaries(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, String p_category,
            String companyId) throws EnvoyServletException
    {
        GlossaryManager mgr = null;

        try
        {
            mgr = ServerProxy.getGlossaryManager();
            return mgr.getGlossaries(p_sourceLocale, p_targetLocale,
                    p_category, companyId);
        }
        catch (Exception e)
        {
            CATEGORY.error("DownloadPageHandler :: getGlossaries(). ", e);
            String args[] =
            { p_sourceLocale.toString(), p_targetLocale.toString(), p_category };
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_GET_GLOSSARIES, args,
                    null, EnvoyServletException.PROPERTY_FILE_NAME);
        }
    }
}

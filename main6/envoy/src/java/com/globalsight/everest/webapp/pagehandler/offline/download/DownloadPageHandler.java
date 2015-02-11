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

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.glossaries.GlossaryManager;
import com.globalsight.everest.localemgr.CodeSet;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GlobalSightLocale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;


/**
 * DownloadPageHandler is responsible for displaying download page and
 * send the download file to the user.
 */
public class DownloadPageHandler
    extends PageHandler
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            DownloadPageHandler.class);

    public static final List<String> DOWNLOAD_OPTIONS = new ArrayList<String>();
    
    // Constructor
    public DownloadPageHandler()
    {
    }


    /**
     * Invokes this PageHandler
     *
     * @param p_thePageDescriptor the page desciptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        setParameters(p_request);
        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    /**
     * Outputs a page name as file name with full path in a tooltip.
     * See also envoy/tasks/taskDetail.jsp.
     */
    public static String getFileName(String p_page)
    {
        String pageName = p_page;

        int bslash = p_page.lastIndexOf("\\");
        int fslash = p_page.lastIndexOf("/");
        int index;

        if (bslash > 0 && bslash > fslash)
        {
            pageName = p_page.substring(bslash + 1);
        }
        else if (fslash > 0 && fslash > bslash)
        {
            pageName = p_page.substring(fslash + 1);
        }

        return pageName;
    }

    //
    // Private Methods
    //

    private void setParameters(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Task task = (Task)TaskHelper.retrieveObject(session, WORK_OBJECT);
        if (task == null)
        {
            EnvoyServletException e =
                new EnvoyServletException("TaskNotFound", null, null);
            CATEGORY.error(e);
            throw e;
        }

        // do not process download if workflow has been cancelled.
        checkTaskValidation(task, session.getId());

        // all the encoding names for the target locale
        ArrayList encodingNames = getEncodingNames(task);
        session.setAttribute(OfflineConstants.DOWNLOAD_ENCODING_OPTIONS,
            encodingNames);

        // exact match editing
        Boolean exactMatchEditing = new Boolean(task.getWorkflow().getJob().
            getL10nProfile().isExactMatchEditing());
        session.setAttribute(OfflineConstants.DOWNLOAD_EDIT_EXACT,
            exactMatchEditing);

        // The glossary state infomation for this locale pair
        GlossaryState glossaryState = getGlossaryState(task);
        session.setAttribute(OfflineConstants.DOWNLOAD_GLOSSARY_STATE,
            glossaryState);

        setDownloadOptions(session, p_request);
    }

    private void setDownloadOptions(HttpSession p_session,
            HttpServletRequest p_request)
    {
    	DOWNLOAD_OPTIONS.clear();
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_FORMAT);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITOR);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_ENCODING);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_EDITEXACT);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_DISPLAYEXACTMATCH);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_TERMINOLOGY);
        DOWNLOAD_OPTIONS.add(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TERM);
        DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_100);
        DOWNLOAD_OPTIONS.add(OfflineConstants.POPULATE_FUZZY);
        DOWNLOAD_OPTIONS.add(OfflineConstants.NEED_CONSOLIDATE);
        
        for (int i = 0; i < DOWNLOAD_OPTIONS.size(); i++)
        {
        	String downloadOption = DOWNLOAD_OPTIONS.get(i);
        	p_request.setAttribute(downloadOption, PageHandler
        			.getUserParameter(p_session, downloadOption).getValue());
        }
    }


    private ArrayList getEncodingNames(Task task)
        throws EnvoyServletException
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
            CATEGORY.error(e);
            throw new EnvoyServletException(e);
        }

        ArrayList result = new ArrayList();

        for (Iterator it = codesetList.iterator(); it.hasNext(); )
        {
            result.add(((CodeSet)it.next()).getCodeSet());
        }

        return result;
    }

    // check to make sure that the workflow has not been cancelled.
    private void checkTaskValidation(Task p_task, String p_sessionId)
        throws EnvoyServletException
    {
        String state = null;
        String args[] = {p_task.getTaskName()};
        try
        {
            long wfId = p_task.getWorkflow().getId();
            Workflow wf = ServerProxy.getWorkflowManager().
                getWorkflowById(p_sessionId, wfId);
            state = wf == null ? null : wf.getState();
        }
        catch (Exception e)
        {
            CATEGORY.error("DownloadPageHandler :: checkTaskValidation(). ", e);
            throw new EnvoyServletException(
                EnvoyServletException.MSG_FAILED_TO_GET_WORKFLOW,
                args, null, EnvoyServletException.PROPERTY_FILE_NAME);
        }

        if (state == null || state.equals(Workflow.CANCELLED))
        {
            throw new EnvoyServletException(
                EnvoyServletException.MSG_FAILED_TO_DOWNLOAD,
                args, null, EnvoyServletException.PROPERTY_FILE_NAME);
        }
    }

    /**
     * Builds a gLossaryState object which contains all the parameters needed
     * for glossary selction.
     * @param p_sourceLocale get glossaries for this source and target locale
     * @param p_targetLocale get glossaries for this target and target locale
     * @param p_category glossray category, used to filter glossaries by subject.
     * @return GlossaryState object
     */
    public GlossaryState getGlossaryState(Task p_task)
        throws EnvoyServletException
    {
        GlossaryState result = new GlossaryState();

        result.setGlossaries(getGlossaries(
            p_task.getSourceLocale(), p_task.getTargetLocale(), null, p_task.getCompanyId() ));

        return result;
    }

    /**
     * Wraps the code for getting the GlossaryFiles from the DB and
     * handling any exceptions.
     * @param p_sourceLocale get glossaries for this source and target locale
     * @param p_targetLocale get glossaries for this target and target locale
     * @param p_category glossray category, used to filter glossaries by subject.
     * @return collection of GlossaryFile objects
     */
    private ArrayList getGlossaries(GlobalSightLocale p_sourceLocale,
        GlobalSightLocale p_targetLocale, String p_category, String companyId)
        throws EnvoyServletException
    {
        GlossaryManager mgr = null;

        try
        {
            mgr = ServerProxy.getGlossaryManager();
            return mgr.getGlossaries(p_sourceLocale, p_targetLocale,
                p_category, companyId);
        }
        catch (Exception  e)
        {
            CATEGORY.error("DownloadPageHandler :: getGlossaries(). ", e);
            String args[] = { p_sourceLocale.toString(),
                              p_targetLocale.toString(), p_category};
            throw new EnvoyServletException(
                EnvoyServletException.MSG_FAILED_TO_GET_GLOSSARIES,
                args, null, EnvoyServletException.PROPERTY_FILE_NAME);
        }
    }
}

/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.edit.ImageHelper;
import com.globalsight.everest.edit.SegmentProtectionManager;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.SynchronizationStatus;
import com.globalsight.everest.edit.online.CommentThreadView;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.edit.online.OnlineEditorManager;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.edit.online.imagereplace.ImageReplace;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.glossaries.GlossaryManager;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;

/**
 * <p>
 * Helper functions for the online editor to call server-side objects and
 * implement exception handling.
 * </p>
 */
public class EditorHelper implements EditorConstants
{
    private static final Logger CATEGORY = Logger.getLogger(EditorHelper.class);

    /**
     * This class can not be instantiated.
     */
    private EditorHelper()
    {
    }

    /**
     * Called by every pagehandler.
     */
    static public void initEditorManager(EditorState p_state)
            throws EnvoyServletException
    {
        try
        {
            p_state.setEditorManager(ServerProxy
                    .getOnlineEditorManagerWLRemote());
        }
        catch (GeneralException e)
        {
            CATEGORY.error("can't get OnlineEditorManager??", e);

            throw new EnvoyServletException(e);
        }
    }

    /**
     * Called by every pagehandler.
     */
    static public void initEditorOptions(EditorState p_state,
            HttpSession p_session) throws EnvoyServletException
    {
        EditorState.Options options = p_state.getOptions();

        if (options == null)
        {
            options = new EditorState.Options();
        }

        EditorState.Layout layout = p_state.getLayout();

        if (layout == null)
        {
            layout = new EditorState.Layout();
        }

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            p_state.setCanShowMt(sc
                    .getBooleanParameter(SystemConfigParamNames.MT_SHOW_IN_EDITOR));
        }
        catch (Exception ignore)
        {
            p_state.setCanShowMt(false);
        }

        UserParameter param;

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_AUTO_SAVE_SEGMENT);

        if (param != null)
        {
            options.setAutoSave(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_AUTO_UNLOCK);

        if (param != null)
        {
            options.setAutoUnlock(param.getBooleanValue());

            if (p_state.canEditAll() && options.getAutoUnlock())
            {
                p_state.setEditAllState(EDIT_ALL);
            }
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_AUTO_SYNC);

        if (param != null)
        {
            options.setAutoSync(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);

        if (param != null)
        {
            options.setAutoAdjustWhitespace(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_LAYOUT);

        if (param != null)
        {
            layout.setLayout(param.getValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_VIEWMODE);

        if (param != null)
        {
            layout.setViewmode(param.getIntValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_PTAGMODE);

        if (param != null)
        {
            options.setPTagMode(param.getValue());
            p_state.setPTagFormat(param.getValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_PTAGHILITE);

        if (param != null)
        {
            options.setHilitePtags(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_SHOW_MT);

        if (param != null)
        {
            options.setShowMt(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_ITERATE_SUBS);

        if (param != null)
        {
            options.setIterateSubs(param.getBooleanValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.TM_MATCHING_THRESHOLD);

        if (param != null)
        {
            options.setTmMatchingThreshold(param.getIntValue());
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.TB_MATCHING_THRESHOLD);

        if (param != null)
        {
            options.setTbMatchingThreshold(param.getIntValue());
        }

        p_state.setOptions(options);
        p_state.setLayout(layout);

        // Now set the hyperlink color info

        String color, active, visited;
        color = active = visited = "blue";

        EditorState.LinkStyles styles = p_state.getLinkStyles();
        if (styles == null)
        {
            styles = new EditorState.LinkStyles(color, active, visited);
        }

        param = PageHandler.getUserParameter(p_session,
                UserParamNames.HYPERLINK_COLOR_OVERRIDE);

        if (param != null && param.getBooleanValue() == true)
        {
            param = PageHandler.getUserParameter(p_session,
                    UserParamNames.HYPERLINK_COLOR);

            if (param != null)
            {
                styles.m_A_color = param.getValue();
            }

            param = PageHandler.getUserParameter(p_session,
                    UserParamNames.ACTIVE_HYPERLINK_COLOR);

            if (param != null)
            {
                styles.m_A_active = param.getValue();
            }

            param = PageHandler.getUserParameter(p_session,
                    UserParamNames.VISITED_HYPERLINK_COLOR);

            if (param != null)
            {
                styles.m_A_visited = param.getValue();
            }
        }

        p_state.setLinkStyles(styles);
    }

    //
    // Initialize from Job
    //
    static public void initializeFromJob(EditorState p_state, String p_jobId,
            String p_srcPageId, Locale p_uiLocale, String p_userId,
            PermissionSet p_perms) throws EnvoyServletException
    {
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.parseLong(p_jobId));
            String companyId = String.valueOf(job.getCompanyId());

            setPagesInJob(p_state, job);
            // Why this invoking is ignored?
            // setJobTargetLocales(p_state, job, p_perms, p_userId);
            setExcludedItemsFromJob(p_state, job);
            setTermbaseNames(p_state, p_uiLocale, p_userId, companyId);
            setTermbaseFromJob(p_state, job, p_userId, companyId);
            setTmNamesFromJob(p_state, job);
        }
        catch (EnvoyServletException ex)
        {
            throw ex;
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    static private void setPagesInJob(EditorState p_state, Job p_job)
            throws EnvoyServletException
    {
        ArrayList pages = new ArrayList();
        Hashtable pagesHash = new Hashtable();

        Iterator it1, it2;
        SourcePage srcPage;
        TargetPage trgPage;
        GlobalSightLocale srcLocale, trgLocale;
        EditorState.PagePair pair;

        // New: For target page lookup the source page. Add the source
        // page to a hash, and add target pages to the PagePair object
        // referenced by the source page key.
        try
        {

            l_workflows: for (it1 = p_job.getWorkflows().iterator(); it1
                    .hasNext();)
            {
                Workflow workflow = (Workflow) it1.next();
                trgLocale = workflow.getTargetLocale();

                // Skip workflows that have failed or been cancelled.
                // Keep this in sync with setJobTargetLocales() or else.
                if (workflow.getState().equals(Workflow.CANCELLED)
                        || workflow.getState().equals(Workflow.IMPORT_FAILED))
                {
                    continue l_workflows;
                }

                l_targetpages:
                // retrieve just the pages with an extracted file associated
                // with it
                for (it2 = workflow.getTargetPages(
                        ExtractedSourceFile.EXTRACTED_FILE).iterator(); it2
                        .hasNext();)
                {
                    trgPage = (TargetPage) it2.next();
                    srcPage = trgPage.getSourcePage();
                    String state = srcPage.getPageState();
                    if (state.equals(PageState.IMPORT_FAIL))
                    {
                        // If the source page for this target page
                        // failed to import (but the workflow itself
                        // has not been set to IMPORTFAILED, can this
                        // happen?), ignore this target page.
                        continue l_targetpages;
                    }

                    if (pagesHash.containsKey(srcPage.getIdAsLong()))
                    {
                        ((EditorState.PagePair) (pagesHash.get(srcPage
                                .getIdAsLong()))).putTargetPage(trgLocale,
                                trgPage.getIdAsLong());
                    }
                    else
                    {
                        pair = new EditorState.PagePair(
                                srcPage.getIdAsLong(),
                                srcPage.getGlobalSightLocale(),
                                srcPage.getExternalPageId(),
                                getExtractedSourceFile(srcPage).containGsTags(),
                                workflow.getState());

                        pair.putTargetPage(trgLocale, trgPage.getIdAsLong());

                        pagesHash.put(srcPage.getIdAsLong(), pair);
                    }
                }
            }

            it1 = p_job.getSourcePages(ExtractedSourceFile.EXTRACTED_FILE)
                    .iterator();
            while (it1.hasNext())
            {
                srcPage = (SourcePage) it1.next();

                EditorState.PagePair pagepair = (EditorState.PagePair) pagesHash
                        .get(srcPage.getIdAsLong());

                if (pagepair != null)
                {
                    pages.add(pagepair);
                }
            }

            // pages better be not empty now
            p_state.setPages(pages);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    static private void setExcludedItemsFromJob(EditorState p_state, Job p_job)
    {
        Vector items = getExcludedItemsFromL10nProfile(p_job.getL10nProfile());

        p_state.setExcludedItems(items);
    }

    static private void setTermbaseFromJob(EditorState p_state, Job p_job,
            String p_userId, String p_companyId) throws EnvoyServletException
    {
        String name = p_job.getL10nProfile().getProject().getTermbaseName();

        if (name != null && name.length() > 0)
        {
            long tbid = getTermbaseIdByName(name, p_companyId);
            p_state.setDefaultTermbaseName(name);
            p_state.setDefaultTermbaseId(tbid);
            p_state.setCanAccessTB(tbFilter(p_userId, name, tbid));
        }
        else
        {
            p_state.setDefaultTermbaseName(null);
            p_state.setDefaultTermbaseId(-1);
        }
    }

    static private void setTmNamesFromJob(EditorState p_state, Job p_job)
            throws EnvoyServletException
    {
        try
        {
            TranslationMemoryProfile tmp = p_job.getL10nProfile()
                    .getTranslationMemoryProfile();
            Vector tms = tmp.getProjectTMsToLeverageFrom();
            String[] tmNames = new String[tms.size()];
            for (int i = 0; i < tms.size(); i++)
            {
                LeverageProjectTM lptm = (LeverageProjectTM) tms.get(i);
                ProjectTM tm = ServerProxy.getProjectHandler()
                        .getProjectTMById(lptm.getProjectTmId(), false);
                tmNames[i] = tm.getName();
            }

            p_state.setTmNames(tmNames);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    //
    // Initialize from Activity
    //

    static public void initializeFromActivity(EditorState p_state,
            HttpSession p_session, String p_userId, String p_taskId,
            HttpServletRequest p_request, Locale p_uiLocale)
            throws EnvoyServletException
    {
        try
        {
            Task task = ServerProxy.getTaskManager()
                    .getTask(p_userId, Long.parseLong(p_taskId),
                            WorkflowConstants.TASK_ALL_STATES);
            String companyId = String.valueOf(task.getCompanyId());

            p_state.setIsReviewActivity(task.isType(Activity.TYPE_REVIEW));

            setPagesInActivity(p_state, task);
            setExcludedItemsFromActivity(p_state, task);
            setTermbaseNames(p_state, p_uiLocale, p_userId, companyId);
            setTermbaseFromActivity(p_state, task, p_userId, companyId);
            setTmNamesFromJob(p_state, task.getWorkflow().getJob());
            setAllowEditAll(p_state, task);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static private void setPagesInActivity(EditorState p_state, Task p_task)
    {
        ArrayList<EditorState.PagePair> pages = new ArrayList<EditorState.PagePair>();

        SourcePage srcPage;
        TargetPage trgPage;
        GlobalSightLocale srcLocale, trgLocale;
        Iterator it;

        // For each target page in the task (activity), find the
        // source and target page and create a page pair.
        srcLocale = p_task.getSourceLocale();
        trgLocale = p_task.getTargetLocale();

        for (it = p_task.getTargetPages(ExtractedSourceFile.EXTRACTED_FILE)
                .iterator(); it.hasNext();)
        {
            trgPage = (TargetPage) it.next();
            srcPage = trgPage.getSourcePage();

            EditorState.PagePair pair = new EditorState.PagePair(
                    srcPage.getId(), srcLocale, srcPage.getExternalPageId(),
                    getExtractedSourceFile(srcPage).containGsTags(),
                    Workflow.DISPATCHED);
            pair.putTargetPage(trgLocale, trgPage.getIdAsLong());

            pages.add(pair);
        }

        p_state.setPages(pages);
    }

    static public void setExcludedItemsFromActivity(EditorState p_state,
            Task p_task)
    {
        Vector items = getExcludedItemsFromL10nProfile(p_task.getWorkflow()
                .getJob().getL10nProfile());

        p_state.setExcludedItems(items);
    }

    static public void setTermbaseFromActivity(EditorState p_state,
            Task p_task, String p_userId, String p_companyId)
            throws EnvoyServletException
    {
        String name = p_task.getWorkflow().getJob().getL10nProfile()
                .getProject().getTermbaseName();

        if (name != null && name.length() > 0)
        {
            long tbid = getTermbaseIdByName(name, p_companyId);
            p_state.setDefaultTermbaseName(name);
            p_state.setDefaultTermbaseId(tbid);
            p_state.setCanAccessTB(tbFilter(p_userId, name, tbid));
        }
        else
        {
            p_state.setDefaultTermbaseName(null);
            p_state.setDefaultTermbaseId(-1);
        }
    }

    static public Vector<String> getExcludedItemsFromL10nProfile(
            L10nProfile p_profile)
    {
        TranslationMemoryProfile profile = (TranslationMemoryProfile) p_profile
                .getTranslationMemoryProfile();

        return profile.getJobExcludeTuTypes();
    }

    /**
     * When called from an activity (task), find out if locked segments can be
     * edited.
     */
    static private void setAllowEditAll(EditorState p_state, Task p_task)
    {
        int TMEditType = p_task.getWorkflow().getJob().getL10nProfile().getTMEditType();
        
        p_state.setAllowEditAll(TMEditType == 2);
    }

    //
    // General initialization methods
    //

    static private void setTermbaseNames(EditorState p_state,
            Locale p_uiLocale, String p_userId, String p_companyId)
            throws GeneralException, RemoteException
    {
        ITermbaseManager manager = ServerProxy.getTermbaseManager();
        p_state.setTermbaseNames(manager.getTermbases(p_uiLocale, p_userId,
                p_companyId));
    }

    static private long getTermbaseIdByName(String p_name, String p_companyId)
            throws EnvoyServletException
    {
        try
        {
            ITermbaseManager manager = ServerProxy.getTermbaseManager();
            return manager.getTermbaseId(p_name, p_companyId);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    //
    // Other public methods
    //

    /**
     * Determines if project managers can edit the current page being displayed
     * in the editor. Pages whose workflows are ready to be dispatched,
     * dispatched, localized, exported (or export_failed), can be edited.
     */
    static public boolean pmCanEditCurrentPage(EditorState p_state)
    {
        String state = p_state.getCurrentPage().getWorkflowState();

        // Workflow states are PENDING, IMPORT_FAILED, DISPATCHED,
        // LOCALIZED, EXPORTED, EXPORT_FAILED, CANCELLED, ARCHIVED,
        // BATCHRESERVED, and READY_TO_BE_DISPATCHED.

        if (state.equals(Workflow.READY_TO_BE_DISPATCHED)
                || state.equals(Workflow.DISPATCHED)
                || state.equals(Workflow.LOCALIZED)
                || state.equals(Workflow.EXPORTED)
                || state.equals(Workflow.EXPORT_FAILED))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns a list of TU ids in the page that have not been deleted by GSA
     * delete tags.
     */
    static public HashSet getInterpretedTuIds(EditorState p_state,
            Long p_srcPageId, GlobalSightLocale p_locale)
            throws EnvoyServletException
    {
        try
        {
            return p_state.getEditorManager().getInterpretedTuIds(
                    p_srcPageId.longValue(), p_locale);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    /**
     * When called from an activity (task) that has not been accepted yet, the
     * target page must be displayed read-only.
     */
    static public boolean getTaskIsReadOnly(String p_userId, String p_taskId,
            int p_taskState) throws EnvoyServletException
    {
        try
        {
            Task task = ServerProxy.getTaskManager().getTask(p_userId,
                    Long.parseLong(p_taskId), p_taskState);

            if (task.getState() == Task.STATE_ACCEPTED
                    || task.getState() == Task.STATE_TRANSLATION_COMPLETED)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    /**
     * Returns a list of TU ids (Long) for a source page.
     */
    static public ArrayList<Long> getTuIdsInPage(EditorState p_state,
            Long p_srcPageId) throws EnvoyServletException
    {
        try
        {
            return p_state.getEditorManager().getTuIdsInPage(p_srcPageId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    /**
     * For source page editing: returns the GXML of the source page.
     */
    static public String getSourcePageGxml(EditorState p_state)
            throws Exception
    {
        return p_state.getEditorManager().getSourcePageGxml(
                p_state.getSourcePageId().longValue());
    }

    /**
     * For source page editing: returns a list of validation error messages or
     * null.
     */
    static public ArrayList validateSourcePageGxml(EditorState p_state,
            String p_gxml) throws Exception
    {
        return p_state.getEditorManager().validateSourcePageGxml(p_gxml);
    }

    /**
     * For source page editing: returns a preview of the page in the selected
     * target locale (snippets are interpreted).
     */
    static public String getGxmlPreview(EditorState p_state, String p_gxml,
            String p_locale) throws Exception
    {
        return p_state.getEditorManager().getGxmlPreview(p_gxml, p_locale);
    }

    /**
     * For source page editing: returns a list of validation error messages or
     * null, in which case the page is getting updated in the background.
     */
    static public ArrayList updateSourcePageGxml(EditorState p_state,
            String p_gxml) throws Exception
    {
        return p_state.getEditorManager().updateSourcePageGxml(
                p_state.getSourcePageId().longValue(), p_gxml);
    }

    static public String getSourcePageView(EditorState p_state,
            boolean p_dirtyTemplate) throws EnvoyServletException
    {
        return getSourcePageView(p_state, p_dirtyTemplate, null);
    }

    /**
     * Retrieves the source page view with add/delete instructions executed in
     * the given (target-page) locale.
     */
    static public String getSourcePageView(EditorState p_state,
            boolean p_dirtyTemplate, HashMap p_searchMap)
            throws EnvoyServletException
    {
        try
        {
            RenderingOptions options = p_state.getRenderingOptions();
            options.setNeedShowPTags(p_state.getNeedShowPTags());

            return p_state.getEditorManager().getSourcePageView(
                    p_state.getSourcePageId().longValue(),
                    options,
                    // Sat Jun 08 17:33:53 2002 added for add/delete:
                    // always execute page in current target locale
                    p_state.getTargetLocale(), p_dirtyTemplate,
                    p_state.getPaginateInfo(), p_searchMap);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public String getTargetPageView(EditorState p_state,
            boolean p_dirtyTemplate) throws EnvoyServletException
    {
        return getTargetPageView(p_state, p_dirtyTemplate, null);
    }

    static public String getTargetPageView(EditorState p_state,
            boolean p_dirtyTemplate, HashMap p_searchMap)
            throws EnvoyServletException
    {
        RenderingOptions options = p_state.getRenderingOptions();

        if (p_state.isReadOnly())
        {
            options.setEditMode(UIConstants.EDITMODE_READ_ONLY);
        }
        else if (p_state.isEditAll())
        {
            options.setEditMode(UIConstants.EDITMODE_EDIT_ALL);
        }
        else
        {
            options.setEditMode(UIConstants.EDITMODE_DEFAULT);
        }

        // update with the updated modes
        p_state.setRenderingOptions(options);
        options.setUserName(p_state.getUserName());

        try
        {
            return p_state.getEditorManager().getTargetPageView(
                    p_state.getTargetPageId().longValue(), p_state,
                    p_state.getExcludedItems(), p_dirtyTemplate, p_searchMap);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public SegmentView getSegmentView(EditorState p_state, long p_tuId,
            long p_tuvId, long p_subId, long p_targetPageId,
            long p_sourceLocaleId, long p_targetLocaleId)
            throws EnvoyServletException
    {
        SegmentView result;

        try
        {
            result = p_state.getEditorManager().getSegmentView(p_tuId, p_tuvId,
                    String.valueOf(p_subId), p_targetPageId, p_sourceLocaleId,
                    p_targetLocaleId, p_state.getTmNames(),
                    p_state.getDefaultTermbaseName());
        }
        catch (GeneralException e)
        {
            CATEGORY.error("getSegmentView(" + p_tuId + "," + p_tuvId + ","
                    + p_subId + ") failed: ", e);

            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.error("getSegmentView(" + p_tuId + "," + p_tuvId + ","
                    + p_subId + ") failed: ", e);

            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }

        return result;
    }

    /**
     * Determines which type of editor to show: Image, Bidi, Text
     * (whitespace-preserving), or standard Editor.
     */
    static public void setEditorType(EditorState p_state, SegmentView p_view)
    {
        String dataType = p_view.getDataType();
        String itemType = p_view.getItemType();

        // solution a NullpointerException
        if (itemType == null || dataType == null)
        {
            p_state.setEditorType(SE_SEGMENTEDITOR);
        }
        else if (ImageHelper.isImageItemType(itemType))
        {
            p_state.setEditorType(SE_IMAGEEDITOR);
        }
        // Show bidi editor if 1) target language is bidi 2) data type
        // is HTML 3) segment type is text.
        // (Bidi editor is not whitespace-save as of Jul 19 2002.)
        else if ("text".equals(itemType)
                && EditUtil.isRTLLocale(p_state.getTargetLocale())
                && EditUtil.isHtmlDerivedFormat(dataType))
        {
            p_state.setEditorType(SE_BIDIEDITOR);
        }
        else if (EditUtil.isWhitePreservingFormat(dataType, itemType))
        {
            p_state.setEditorType(SE_TEXTEDITOR);
        }
        else
        {
            p_state.setEditorType(SE_SEGMENTEDITOR);
        }
    }

    /**
     * Uploads an image and updates the segment value. Ensures that the new
     * segment value points to a relative path and contains the filename of the
     * uploaded image (.gif vs. jpg!).
     */
    static public void uploadImage(HttpServletRequest p_request,
            EditorState p_state, SegmentView p_view)
            throws EnvoyServletException, RemoteException
    {
        Long targetPageId = p_state.getTargetPageId();
        long jobId = p_state.getJobId();
        long tuvId = p_state.getTuvId();
        long subId = p_state.getSubId();

        String strTargetPageId = String.valueOf(targetPageId);
        String strTuvId = String.valueOf(tuvId);
        String strSubId = String.valueOf(subId);

        try
        {
            ImageReplace o_upload = new ImageReplace();

            o_upload.setTargetPageId(strTargetPageId);
            o_upload.setTuvId(strTuvId);
            o_upload.setSubId(strSubId);

            o_upload.doUpload(p_request);

            String diskName = o_upload.getSavedFilepath()
                    + o_upload.getFilename();

            // Fri Dec 20 16:54:38 2002 CvdL Must ignore the segment.
            // Uploading an image and modifying the segment are
            // mutually exclusive operations.

            // String imageUrl = o_upload.getFieldValue("targetImageURL");
            String oldSegment = p_view.getTargetSegment().getTotalTextValue();
            String newSegment = ImageHelper.mergeSegmentAndFilename(oldSegment,
                    o_upload.getFilename());

            p_state.getEditorManager().createImageMap(targetPageId, tuvId,
                    subId, diskName, newSegment);

            p_state.getEditorManager().updateTUV(tuvId, strSubId, newSegment,
                    jobId);
        }
        catch (GeneralException e)
        {
            CATEGORY.error("Image upload failed: ", e);

            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_UPLOAD_IMAGE, null, e);
        }
    }

    /**
     * <P>
     * Wrapper around {@see getPageInfo(long)}.
     * </P>
     */
    static public PageInfo getPageInfo(EditorState p_state, Long p_srcPageId)
            throws EnvoyServletException
    {
        return getPageInfo(p_state, p_srcPageId.longValue());
    }

    /**
     * <P>
     * Returns a data object for the editor's page info dialog: page name, page
     * format, word count and segment count.
     * </P>
     */
    static public PageInfo getPageInfo(EditorState p_state, long p_srcPageId)
            throws EnvoyServletException
    {
        try
        {
            return p_state.getEditorManager().getPageInfo(p_srcPageId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void updateSegment(EditorState p_state, SegmentView p_view,
            String p_tuId, String p_tuvId, String p_subId, String p_gxml)
            throws EnvoyServletException
    {
        try
        {
            long jobId = p_state.getJobId();
            EditorState.Options options = p_state.getOptions();
            boolean b_adjustWS = options.getAutoAdjustWhitespace();

            if (b_adjustWS)
            {
                p_gxml = EditUtil.adjustWhitespace(p_gxml, p_view
                        .getSourceSegment().getTextValue());
            }

            // update the image map if necessary, using null for tempName
            if (p_view != null && p_view.getImageMapExists())
            {
                // if an absolute "http:" path
                if (ImageHelper.hasProtocol(p_gxml))
                {
                    // specify NO image map - remove if created
                    p_state.getEditorManager().createImageMap(
                            p_state.getTargetPageId(), Long.parseLong(p_tuvId),
                            Long.parseLong(p_subId), null, null);
                }
                else
                {
                    // New image url must have same extension as the
                    // uploaded file and must be a relative path.
                    p_gxml = ImageHelper.mergeImages(p_view.getTargetSegment()
                            .getTextValue(), p_gxml);

                    p_state.getEditorManager().createImageMap(
                            p_state.getTargetPageId(), Long.parseLong(p_tuvId),
                            Long.parseLong(p_subId), null, p_gxml);
                }
            }

            // update target segment
            p_state.getEditorManager().updateTUV(Long.parseLong(p_tuvId),
                    p_subId, p_gxml, jobId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void updateSegment(EditorState p_state, SegmentView p_view,
            String p_tuId, String p_tuvId, String p_subId, String p_gxml,
            String p_userId) throws EnvoyServletException
    {
        try
        {
            EditorState.Options options = p_state.getOptions();
            boolean b_adjustWS = options.getAutoAdjustWhitespace();
            long jobId = p_state.getJobId();

            if (b_adjustWS)
            {
                p_gxml = EditUtil.adjustWhitespace(p_gxml, p_view
                        .getSourceSegment().getTotalTextValue());
            }

            // update the image map if necessary, using null for tempName
            if (p_view != null && p_view.getImageMapExists())
            {
                // if an absolute "http:" path
                if (ImageHelper.hasProtocol(p_gxml))
                {
                    // specify NO image map - remove if created
                    p_state.getEditorManager().createImageMap(
                            p_state.getTargetPageId(), Long.parseLong(p_tuvId),
                            Long.parseLong(p_subId), null, null);
                }
                else
                {
                    // New image url must have same extension as the
                    // uploaded file and must be a relative path.
                    p_gxml = ImageHelper.mergeImages(p_view.getTargetSegment()
                            .getTextValue(), p_gxml);

                    p_state.getEditorManager().createImageMap(
                            p_state.getTargetPageId(), Long.parseLong(p_tuvId),
                            Long.parseLong(p_subId), null, p_gxml);
                }
            }

            // update target segment
            p_state.getEditorManager().updateTUV(Long.parseLong(p_tuvId),
                    p_subId, p_gxml, p_userId, jobId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void updateSegment(EditorState p_state, SegmentView p_view,
            long p_tuId, long p_tuvId, long p_subId, String p_gxml)
            throws EnvoyServletException
    {
        updateSegment(p_state, p_view, String.valueOf(p_tuId),
                String.valueOf(p_tuvId), String.valueOf(p_subId), p_gxml);
    }

    static public void updateSegment(EditorState p_state, SegmentView p_view,
            long p_tuId, long p_tuvId, long p_subId, String p_gxml,
            String p_userId) throws EnvoyServletException
    {
        updateSegment(p_state, p_view, String.valueOf(p_tuId),
                String.valueOf(p_tuvId), String.valueOf(p_subId), p_gxml,
                p_userId);
    }

    static public void splitSegments(EditorState p_state, String p_tuv1,
            String p_tuv2, String p_location, long p_jobId)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().splitSegments(Long.parseLong(p_tuv1),
                    Long.parseLong(p_tuv2), p_location, p_jobId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void mergeSegments(EditorState p_state, String p_tuv1,
            String p_tuv2, long p_jobId) throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().mergeSegments(Long.parseLong(p_tuv1),
                    Long.parseLong(p_tuv2), p_jobId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public CommentThreadView getCommentThreads(EditorState p_state)
    {
        try
        {
            return p_state.getEditorManager().getCommentThreads(
                    p_state.getTargetPageId().longValue());
        }
        catch (GeneralException e)
        {
            CATEGORY.error("getComments("
                    + p_state.getTargetPageId().longValue() + ") failed: ", e);

            // throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.error("getComments("
                    + p_state.getTargetPageId().longValue() + ") failed: ", e);

            // throw new EnvoyServletException(EnvoyServletException.EX_REMOTE,
            // e);
        }

        return null;
    }

    static public CommentView getCommentView(EditorState p_state,
            long p_commentId, long p_tuId, long p_tuvId, long p_subId)
    {
        try
        {
            return p_state.getEditorManager().getCommentView(p_commentId,
                    p_state.getTargetPageId().longValue(), p_tuId, p_tuvId,
                    p_subId);
        }
        catch (GeneralException e)
        {
            CATEGORY.error("getCommentView(" + p_tuId + "," + p_tuvId + ","
                    + p_subId + ") failed: ", e);

            // throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.error("getCommentView(" + p_tuId + "," + p_tuvId + ","
                    + p_subId + ") failed: ", e);

            // throw new EnvoyServletException(EnvoyServletException.EX_REMOTE,
            // e);
        }

        return null;
    }

    static public void createComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user, boolean share,
            boolean overwrite) throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().createComment(p_view.getTuId(),
                    p_view.getTuvId(), p_view.getSubId(), p_title, p_comment,
                    p_priority, p_status, p_category, p_user, share, overwrite);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void createComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().createComment(p_view.getTuId(),
                    p_view.getTuvId(), p_view.getSubId(), p_title, p_comment,
                    p_priority, p_status, p_category, p_user);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void editComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user, boolean share,
            boolean overwrite) throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().editComment(p_view, p_title, p_comment,
                    p_priority, p_status, p_category, p_user, share, overwrite);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void editComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().editComment(p_view, p_title, p_comment,
                    p_priority, p_status, p_category, p_user);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void addComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().addComment(p_view, p_title, p_comment,
                    p_priority, p_status, p_category, p_user);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void addComment(EditorState p_state, CommentView p_view,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user, boolean share,
            boolean overwrite) throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().addComment(p_view, p_title, p_comment,
                    p_priority, p_status, p_category, p_user, share, overwrite);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public void closeAllComment(EditorState p_state,
            ArrayList p_issueList, String p_user) throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().closeAllComment(p_issueList, p_user);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }
    }

    static public Tuv getTuv(String p_tuvId, long p_jobId)
            throws EnvoyServletException
    {
        return getTuv(Long.parseLong(p_tuvId), p_jobId);
    }

    static public Tuv getTuv(long p_tuvId, long p_jobId)
            throws EnvoyServletException
    {
        Tuv result;

        try
        {
            TuvManager tuvMgr = ServerProxy.getTuvManager();

            result = tuvMgr.getTuvForSegmentEditor(p_tuvId, p_jobId);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }

        return result;
    }

    static public Tuv getTuv(long p_tuId, GlobalSightLocale p_targetLocale,
            long p_jobId) throws EnvoyServletException
    {
        Tuv result;

        try
        {
            TuvManager tuvMgr = ServerProxy.getTuvManager();

            result = tuvMgr.getTuvForSegmentEditor(p_tuId,
                    p_targetLocale.getId(), p_jobId);
        }
        catch (GeneralException e)
        {
            CATEGORY.error("getTuv: ", e);

            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.error("getTuv: ", e);

            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, e);
        }

        return result;
    }

    private static int getServerViewMode(int p_viewMode)
    {
        switch (p_viewMode)
        {
            case EditorConstants.VIEWMODE_TEXT:
                return UIConstants.VIEWMODE_TEXT;
            case EditorConstants.VIEWMODE_DETAIL:
                return UIConstants.VIEWMODE_LIST;
            case EditorConstants.VIEWMODE_PREVIEW:
            default:
                return UIConstants.VIEWMODE_PREVIEW;
        }
    }

    /**
     * When working with snippets and changing the target locale, the cached
     * templates need to be invalidated for snippets to be recalculated
     * correctly.
     */
    static public void invalidateCachedTemplates(EditorState p_state)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().invalidateCachedTemplates();
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    //
    // Online-Offline Synchronization Methods
    //

    static public void clearSynchronizationStatus(EditorState p_state)
            throws EnvoyServletException
    {
        try
        {
            p_state.getEditorManager().invalidateCache();

            p_state.clearSynchronizationStatus();

            p_state.setOldSynchronizationStatus(new SynchronizationStatus(
                    p_state.getTargetPageId(), System.currentTimeMillis(),
                    SynchronizationStatus.UNKNOWN));
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    static public void checkSynchronizationStatus(EditorState p_state)
            throws EnvoyServletException
    {
        try
        {
            SynchronizationStatus oldStatus = p_state
                    .getOldSynchronizationStatus();
            SynchronizationStatus newStatus = getSynchronizationManager()
                    .getStatus(p_state.getTargetPageId());

            if (newStatus != null)
            {
                String status = newStatus.getStatus();

                // Always post GXML update messages. When uploading
                // offline data, let editor manage showing messages
                // only once.
                if (status.equals(SynchronizationStatus.GXMLUPDATE_STARTED)
                        || newStatus.getTimestamp() > oldStatus.getTimestamp())
                {
                    // Offline upload has started, already finished or
                    // restarted. Let editors show a message.
                    p_state.setNewSynchronizationStatus(newStatus);
                }
            }
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    //
    // Glosary-related methods
    //

    /**
     * Retrieves a list of glossaries available for this source/target language
     * pair and returns it in a GlossaryState object for use by the JSP page.
     */
    static public GlossaryState getGlossaryState(GlobalSightLocale p_srcLocale,
            GlobalSightLocale p_trgLocale, String companyId)
            throws EnvoyServletException
    {
        GlossaryState glossaryState = new GlossaryState();

        ArrayList glossaryFiles = (ArrayList) getGlossaries(p_srcLocale,
                p_trgLocale, null, companyId);

        glossaryState.setGlossaries(glossaryFiles);
        glossaryState.setSourceLocale(p_srcLocale);
        glossaryState.setTargetLocale(p_trgLocale);

        return glossaryState;
    }

    /**
     * Retrieves a list of glossaries available for this source/target language
     * pair and returns it in a GlossaryState object for use by the JSP page.
     */
    static public GlossaryState getGlossaryState(GlobalSightLocale p_srcLocale,
            GlobalSightLocale p_trgLocale) throws EnvoyServletException
    {
        GlossaryState glossaryState = new GlossaryState();

        ArrayList glossaryFiles = (ArrayList) getGlossaries(p_srcLocale,
                p_trgLocale, null, null);

        glossaryState.setGlossaries(glossaryFiles);
        glossaryState.setSourceLocale(p_srcLocale);
        glossaryState.setTargetLocale(p_trgLocale);

        return glossaryState;
    }

    /**
     * Retrieves a list of glossaries available for this source/target language
     * pair and updates the GlossaryState object's list with it.
     */
    static public GlossaryState updateGlossaryState(GlossaryState p_state,
            GlobalSightLocale p_srcLocale, GlobalSightLocale p_trgLocale,
            String companyId) throws EnvoyServletException
    {
        ArrayList glossaryFiles = (ArrayList) getGlossaries(p_srcLocale,
                p_trgLocale, null, companyId);

        p_state.setGlossaries(glossaryFiles);
        p_state.setSourceLocale(p_srcLocale);

        return p_state;
    }

    /**
     * Retrieves a list of glossaries available for this source/target language
     * pair and updates the GlossaryState object's list with it.
     */
    static public GlossaryState updateGlossaryState(GlossaryState p_state,
            GlobalSightLocale p_srcLocale, GlobalSightLocale p_trgLocale)
            throws EnvoyServletException
    {
        ArrayList glossaryFiles = (ArrayList) getGlossaries(p_srcLocale,
                p_trgLocale, null, null);

        p_state.setGlossaries(glossaryFiles);
        p_state.setSourceLocale(p_srcLocale);

        return p_state;
    }

    /**
     * Wraps the code for getting the GlossaryFiles from the DB and maps any
     * exceptions to an EnvoyServletException.
     * 
     * @param p_sourceLocale
     *            get glossaries for this source and target locale
     * @param p_targetLocale
     *            get glossaries for this target and target locale
     * @param p_category
     *            glossary category for filtering glossaries by subject
     * @return collection of GlossaryFile objects
     */
    static private ArrayList getGlossaries(GlobalSightLocale p_sourceLocale,
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
        catch (Exception ex)
        {
            String args[] =
            { p_sourceLocale.toString(), p_targetLocale.toString(), p_category };
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_GET_GLOSSARIES, args,
                    null);
        }
    }

    // assumes the page has an extracted file because
    // it is in the editor code
    static private ExtractedSourceFile getExtractedSourceFile(SourcePage p_page)
    {
        return (ExtractedSourceFile) p_page.getPrimaryFile();
    }

    /**
     * Helper function to get locale GlobalSightLocale with ID.
     */
    static public GlobalSightLocale getLocale(String p_locale)
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleByString(p_locale);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    static private SynchronizationManager getSynchronizationManager()
    {
        try
        {
            return ServerProxy.getSynchronizationManager();
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Internal error: cannot receive offline/online synchronization messages",
                    ex);
        }

        return null;
    }

    /**
     * <P>
     * Sets the next segment to be edited. In a TUV with subs, the next segment
     * is computed as follows:
     * </P>
     * 
     * <UL>
     * <LI>if looking at a non-last sub, the next sub</LI>
     * <LI>if looking at the last sub, the next segment</LI>
     * <LI>if looking at a top-level segment, the next segment</LI>
     * </UL>
     * 
     * <P>
     * Of course, all this should work with recursive subs as well.
     * </P>
     * 
     * Wed Jun 18 23:14:14 2003 when in preview mode, don't show subs.
     */
    public static void nextSegment(EditorState p_state)
            throws EnvoyServletException
    {
        long currentTuId = p_state.getTuId();
        long currentTuvId = p_state.getTuvId();
        long currentSubId = p_state.getSubId();
        Vector excludedTypes = p_state.getExcludedItems();

        TargetPage targetPage = (TargetPage) HibernateUtil.get(
                TargetPage.class, p_state.getTargetPageId());
        SourcePage sourcePage = targetPage.getSourcePage();
        long jobId = p_state.getJobId();

        HashSet includedTuIds = null;
        if (p_state.hasGsaTags())
        {
            includedTuIds = EditorHelper.getInterpretedTuIds(p_state,
                    p_state.getSourcePageId(), p_state.getTargetLocale());
        }

        // Fri Mar 11 23:43:29 2005 Flag whether to include subs.
        // Subs are included in list and text view, but not in
        // preview view unless the user has set the option.
        boolean b_includeSubs = p_state.getLayout().getTargetViewMode() != VIEWMODE_PREVIEW
                || p_state.getOptions().getIterateSubs();

        // Find the "next" segment depending on what segment types the
        // user is looking at. I.e., looking at all translatable text
        // strings may mean showing a <sub locType="translatable">
        // next.
        Tuv currentTuv = EditorHelper.getTuv(currentTuvId, jobId);
        long nextId = -1;
        if (b_includeSubs)
        {
            nextId = findNextSub(currentTuv, currentSubId, excludedTypes, jobId);
        }
        if (nextId >= 0)
        {
            p_state.setSubId(nextId);
            return;
        }

        // No sub in current segment, advance to next segment
        GlobalSightLocale targetLocale = p_state.getTargetLocale();

        ArrayList<Long> tus = p_state.getTuIds();
        Long currentTuIdLong = new Long(currentTuId);
        int i_index = tus.indexOf(currentTuIdLong);

        ArrayList<TuvImpl> sourceTuvs = null;
        List targetTuvs = null;
        MatchTypeStatistics tuvMatchTypes = null;
        try
        {
            TuvManager tuvManager = ServerProxy.getTuvManager();
            sourceTuvs = new ArrayList(
                    tuvManager.getSourceTuvsForStatistics(sourcePage));
            targetTuvs = new ArrayList(
                    tuvManager.getTargetTuvsForStatistics(targetPage));

            long sourcePageId = sourcePage.getId();
            long targetLocaleId = targetPage.getGlobalSightLocale().getId();
            if (sourceTuvs.size() > 0)
            {
                tuvMatchTypes = getMatchTypesForStatistics(p_state,
                        sourcePageId, targetLocaleId);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }

        // This loop terminates because when you can edit *this*
        // segment and none of the other segments are editable, we
        // wrap around the list at the end and will stop at *this*
        // segment again.
        while (true)
        {
            while (true)
            {
                // Segment editor wraps at the end of the list
                if (i_index < (tus.size() - 1))
                {
                    ++i_index;
                }
                else
                {
                    i_index = 0;
                }

                currentTuIdLong = (Long) tus.get(i_index);
                currentTuId = currentTuIdLong.longValue();
                currentTuv = EditorHelper.getTuv(currentTuId, targetLocale,
                        jobId);
                Tuv srcTuv = EditorHelper.getTuv(currentTuId,
                        p_state.getSourceLocale(), jobId);
                currentTuvId = currentTuv.getId();
                currentSubId = 0;

                // Look for the next tuv if this one is not contained
                // in the GS-tagged page.
                if (includedTuIds != null
                        && !includedTuIds.contains(currentTuIdLong))
                {
                    continue;
                }

                // Mon Sep 13 19:52:22 2004 Skip merged TUVs.
                String mergeState = currentTuv.getMergeState();
                if (mergeState.equals(Tuv.MERGE_MIDDLE)
                        || mergeState.equals(Tuv.MERGE_END))
                {
                    continue;
                }

                // Wed Jun 18 23:20:42 2003 Skip localizable and
                // non-text TUVs when in preview mode.
                if (b_includeSubs == false
                        && (currentTuv.isLocalizable(jobId) || !currentTuv
                                .getTu(jobId).getTuType().equals("text")))
                {
                    continue;
                }

                // updated
                if (currentTuv.isLocalized() || srcTuv.isLocalized())
                {
                    break;
                }

                if (p_state.isEditAll())
                {
                    break;
                }

                if (LeverageUtil.isIncontextMatch(srcTuv, sourceTuvs,
                        targetTuvs, tuvMatchTypes, p_state.getExcludedItems(),
                        jobId))
                {
                    continue;
                }

                // Use this tuv if it is not a locked exact match or
                // the user has clicked Edit All.
                if (!EditHelper.isTuvInProtectedState(currentTuv, jobId))
                {
                    break;
                }
            }

            p_state.setTuId(currentTuId);
            p_state.setTuvId(currentTuvId);
            p_state.setSubId(currentSubId);

            nextId = findNextSub(currentTuv, -1, excludedTypes, jobId);

            if (nextId >= 0)
            {
                p_state.setSubId(nextId);
                return;
            }
        }
    }

    /**
     * <P>
     * Sets the previous segment to be edited. In a TUV with subs, the previous
     * segment is computed as follows:
     * </P>
     * 
     * <UL>
     * <LI>if looking at a non-first sub, the previous sub</LI>
     * <LI>if looking at the first sub, the parent segment</LI>
     * <LI>if looking at a parent segment, the previous segment</LI>
     * </UL>
     * 
     * <P>
     * Of course, all this should work with recursive subs as well.
     * </P>
     * 
     * Wed Jun 18 23:14:14 2003 when in preview mode, don't show subs.
     */
    public static void previousSegment(EditorState p_state)
            throws EnvoyServletException
    {
        long currentTuId = p_state.getTuId();
        long currentTuvId = p_state.getTuvId();
        long currentSubId = p_state.getSubId();
        Vector excludedTypes = p_state.getExcludedItems();
        HashSet includedTuIds = null;

        if (p_state.hasGsaTags())
        {
            includedTuIds = EditorHelper.getInterpretedTuIds(p_state,
                    p_state.getSourcePageId(), p_state.getTargetLocale());
        }

        // Fri Mar 11 23:43:29 2005 Flag whether to include subs.
        // Subs are included in list and text view, but not in
        // preview view unless the user has set the option.
        boolean b_includeSubs = p_state.getLayout().getTargetViewMode() != VIEWMODE_PREVIEW
                || p_state.getOptions().getIterateSubs();

        TargetPage targetPage = (TargetPage) HibernateUtil.get(
                TargetPage.class, p_state.getTargetPageId());
        SourcePage sourcePage = targetPage.getSourcePage();
        long jobId = p_state.getJobId();

        // Find the "previous" segment depending on what segment types
        // the user is looking at. I.e., looking at all translatable
        // text strings may mean showing a <sub locType="translatable">
        // next.
        Tuv currentTuv = EditorHelper.getTuv(currentTuvId, jobId);
        long prevId = -1;

        if (b_includeSubs)
        {
            prevId = findPreviousSub(currentTuv, currentSubId, excludedTypes,
                    jobId);
        }

        if (prevId >= 0)
        {
            p_state.setSubId(prevId);
            return;
        }

        // No sub in current segment, advance to previous segment
        GlobalSightLocale targetLocale = p_state.getTargetLocale();

        ArrayList tus = p_state.getTuIds();
        Long currentTuIdLong = new Long(currentTuId);
        int i_index = tus.indexOf(currentTuIdLong);

        ArrayList<TuvImpl> sourceTuvs = null;
        List targetTuvs = null;
        MatchTypeStatistics tuvMatchTypes = null;
        try
        {
            TuvManager tuvManager = ServerProxy.getTuvManager();
            sourceTuvs = new ArrayList(
                    tuvManager.getSourceTuvsForStatistics(sourcePage));
            targetTuvs = new ArrayList(
                    tuvManager.getTargetTuvsForStatistics(targetPage));

            long sourcePageId = sourcePage.getId();
            long targetLocaleId = targetPage.getGlobalSightLocale().getId();
            if (sourceTuvs.size() > 0)
            {
                tuvMatchTypes = getMatchTypesForStatistics(p_state,
                        sourcePageId, targetLocaleId);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }

        // This loop terminates because when you can edit *this*
        // segment and none of the other segments are editable, we
        // wrap around the list at the beginning and will stop at
        // *this* segment again.
        while (true)
        {
            while (true)
            {
                // Segment editor wraps at the beginning of the list
                if (i_index > 0)
                {
                    --i_index;
                }
                else
                {
                    i_index = tus.size() - 1;
                }

                currentTuIdLong = (Long) tus.get(i_index);
                currentTuId = currentTuIdLong.longValue();
                currentTuv = EditorHelper.getTuv(currentTuId, targetLocale,
                        jobId);
                Tuv srcTuv = EditorHelper.getTuv(currentTuId,
                        p_state.getSourceLocale(), jobId);
                currentTuvId = currentTuv.getId();
                currentSubId = 0;

                // Look for the next tuv if this one is not contained
                // in the GS-tagged page.
                if (includedTuIds != null
                        && !includedTuIds.contains(currentTuIdLong))
                {
                    continue;
                }

                // Mon Sep 13 19:52:22 2004 Skip merged TUVs.
                String mergeState = currentTuv.getMergeState();
                if (mergeState.equals(Tuv.MERGE_MIDDLE)
                        || mergeState.equals(Tuv.MERGE_END))
                {
                    continue;
                }

                // Wed Jun 18 23:20:42 2003 Skip localizable and
                // non-text TUVs when in preview mode.
                if (b_includeSubs == false
                        && (currentTuv.isLocalizable(jobId) || !currentTuv
                                .getTu(jobId).getTuType().equals("text")))
                {
                    continue;
                }

                if (currentTuv.isLocalized() || srcTuv.isLocalized())
                {
                    break;
                }

                if (p_state.isEditAll())
                {
                    break;
                }
                if (LeverageUtil.isIncontextMatch(srcTuv, sourceTuvs,
                        targetTuvs, tuvMatchTypes, p_state.getExcludedItems(),
                        jobId))
                {
                    continue;
                }

                // Use this tuv if it is not a locked exact match or
                // the user has clicked Edit All.
                if (!EditHelper.isTuvInProtectedState(currentTuv, jobId))
                {
                    break;
                }
                
                if (b_includeSubs
                        && EditHelper.isTuvInProtectedState(currentTuv, jobId)
                        && !isRealExactMatchLocalied(srcTuv, currentTuv,
                                tuvMatchTypes, "" + currentSubId, jobId))
                {
                    break;
                }
            }

            p_state.setTuId(currentTuId);
            p_state.setTuvId(currentTuvId);
            p_state.setSubId(currentSubId);

            // When in preview mode, do not try to locate a sub but
            // return the top-level segment.
            if (!b_includeSubs)
            {
                return;
            }

            prevId = findPreviousSub(currentTuv, -1, excludedTypes, jobId);

            if (prevId >= 0)
            {
                p_state.setSubId(prevId);
                return;
            }
        }
    }
    
    /**
     * For segment with sub, when sub is exact match
     */
    public static boolean isRealExactMatchLocalied(Tuv p_srcTuv,
            Tuv p_targetTuv, MatchTypeStatistics p_matchTypes, String subid,
            long p_jobId)
    {
        boolean result = p_targetTuv.isExactMatchLocalized(p_jobId);

        if (result)
        {
            int tuvState = p_matchTypes.getLingManagerMatchType(
                    p_srcTuv.getId(), subid);

            if (tuvState == LeverageMatchLingManager.NO_MATCH
                    || tuvState == LeverageMatchLingManager.FUZZY)
            {
                result = false;
            }
        }
        else
        {
            int tuvState = p_matchTypes.getLingManagerMatchType(
                    p_srcTuv.getId(), subid);
            
            if (tuvState == LeverageMatchLingManager.EXACT)
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * As "MatchTypeStatistics" data has been cached in "PageCache" of
     * "OnlineEditorManagerLocal", try to get it from the pageCache first.
     * 
     * "EditorState" >> "OnlineEditorManagerLocal" >> "PageCache" >>
     * "getMatchTypes".
     * 
     * @param p_state
     * @param p_ourcePageId
     * @param p_targetLocaleId
     * @return
     */
    private static MatchTypeStatistics getMatchTypesForStatistics(
            EditorState p_state, long p_sourcePageId, long p_targetLocaleId)
    {
        if (p_state == null)
        {
            return null;
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.info("------ Before getMatchTypesForStatistics ------");
        }
        MatchTypeStatistics tuvMatchTypes = null;
        try
        {
            OnlineEditorManager oem = p_state.getEditorManager();
            if (oem != null)
            {
                tuvMatchTypes = oem.getMatchTypes(p_sourcePageId,
                        p_targetLocaleId);
            }

            // This is not needed commonly. Anyway, put it here.
            if (tuvMatchTypes == null)
            {
                LeverageMatchLingManager lingManager = LingServerProxy
                        .getLeverageMatchLingManager();
                // If the source file is WorldServer XLF file,MT translations
                // should NOT impact the color in pop-up editor.
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(p_sourcePageId);
                if (isWSXlfSourceFile)
                {
                    lingManager.setIncludeMtMatches(false);
                }
                tuvMatchTypes = lingManager.getMatchTypesForStatistics(
                        p_sourcePageId, p_targetLocaleId, 0);
            }
        }
        catch (Exception e)
        {

        }
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.info("------ After getMatchTypesForStatistics ------");
        }

        return tuvMatchTypes;
    }

    /**
     * <P>
     * Finds the next segment or sub-segment relative to a starting segment
     * indicated by the sub id <code>p_sub</code>. The sub-segment with the id
     * <code>p_sub</code> is taken as the starting point to find the next
     * sub-segment. The search ignores subs that are not eligible for editing,
     * as indicated by <code>p_editState</code>.
     * 
     * @return -1 when no sub could be found, 0 for the top-level segment, and
     *         &gt; 0 for a sub-segment (i.e. the ID of the sub-segment).
     */
    private static long findNextSub(Tuv p_tuv, long p_sub,
            Vector p_excludedTypes, long p_jobId)
    {
        if (p_tuv == null)
        {
            return -1;
        }

        GxmlElement root = p_tuv.getGxmlElement();
        String tuType = p_tuv.getTu(p_jobId).getTuType();
        String subId = String.valueOf(p_sub);

        // next node may be the root node
        if (p_sub == -1 && !isExcludedNode(root, tuType, p_excludedTypes))
        {
            return 0;
        }

        // else find and check all sub elements
        List subNodes = getSubNodes(root);

        if (subNodes == null || subNodes.size() == 0)
        {
            return -1;
        }

        ListIterator it = subNodes.listIterator();
        GxmlElement currentNode = null;

        // Move to the current sub if starting at a sub, else start at
        // the first sub
        if (p_sub > 0)
        {
            while (it.hasNext())
            {
                GxmlElement subNode = (GxmlElement) it.next();

                if (subNode.getAttribute("id").equals(subId))
                {
                    currentNode = subNode;
                    break;
                }
            }

            // no subs left and current node not found: error (should
            // raise exception)
            if (currentNode == null)
            {
                return -1;
            }
        }

        // and search forward for the next matching one
        while (it.hasNext())
        {
            currentNode = (GxmlElement) it.next();

            tuType = currentNode.getAttribute("type");

            if (!isExcludedNode(currentNode, tuType, p_excludedTypes))
            {
                return Long.parseLong(currentNode.getAttribute("id"));
            }
        }

        return -1;
    }

    /**
     * <P>
     * Finds the previous segment or sub-segment relative to a starting segment
     * indicated by the sub id <code>p_sub</code>. When <code>p_sub</code> is 0,
     * indicating the top-level segment, there is no previous segment and -1 is
     * returned. When <code>p_sub</code> is greater than 0, the sub-segment with
     * that id is taken as the starting point to find the previous sub-segment.
     * When <code>p_sub</code> is -1, the search starts at (and includes) the
     * last sub in the tuv. The search ignores subs that are not eligible for
     * editing, as indicated by <code>p_editState</code>.
     * 
     * @return -1 when no sub could be found, 0 for the top-level segment, and
     *         &gt; 0 for a sub-segment (i.e. the ID of the sub-segment).
     */
    private static long findPreviousSub(Tuv p_tuv, long p_sub,
            Vector p_excludedTypes, long p_jobId)
    {
        // No previous sub in this tuv when looking at top-level segment
        if (p_tuv == null || p_sub == 0)
        {
            return -1;
        }

        GxmlElement root = p_tuv.getGxmlElement();
        String tuType = p_tuv.getTu(p_jobId).getTuType();
        String subId = String.valueOf(p_sub);

        // find all sub elements
        List subNodes = getSubNodes(root);

        // no subs, check if the top-level segment would do...
        if (subNodes == null || subNodes.size() == 0)
        {
            if (p_sub == -1 && !isExcludedNode(root, tuType, p_excludedTypes))
            {
                return 0;
            }

            return -1;
        }

        // navigate to the current sub (if id==-1, move to last sub)
        ListIterator it = subNodes.listIterator();
        GxmlElement currentNode = null;

        while (it.hasNext())
        {
            GxmlElement subNode = (GxmlElement) it.next();

            if (p_sub == -1)
            {
                currentNode = subNode;
            }
            else if (subNode.getAttribute("id").equals(subId))
            {
                currentNode = subNode;

                // change direction - consume this node. See JDK
                // docs: ListIterator.previous(): "Note that
                // alternating calls to next and previous will return
                // the same element repeatedly."
                if (it.hasPrevious())
                {
                    it.previous();
                }

                break;
            }
        }

        // current node not found, error (should raise exception)
        if (currentNode == null)
        {
            return -1;
        }

        tuType = currentNode.getAttribute("type");

        // if the last sub is a matching sub, return it
        if (p_sub == -1
                && !isExcludedNode(currentNode, tuType, p_excludedTypes))
        {
            return Long.parseLong(currentNode.getAttribute("id"));
        }

        // search backward for the next matching one
        while (it.hasPrevious())
        {
            currentNode = (GxmlElement) it.previous();

            tuType = currentNode.getAttribute("type");

            if (!isExcludedNode(currentNode, tuType, p_excludedTypes))
            {
                return Long.parseLong(currentNode.getAttribute("id"));
            }
        }

        // Return the root if it is a matching node
        tuType = p_tuv.getTu(p_jobId).getTuType();
        if (!isExcludedNode(root, tuType, p_excludedTypes))
        {
            return 0;
        }

        return -1;
    }

    private static boolean isExcludedNode(GxmlElement p_node, String p_tuType,
            Vector p_excludedTypes)
    {
        return SegmentProtectionManager.isTuvExcluded(p_node, p_tuType,
                p_excludedTypes);
    }

    /**
     * <P>
     * Retrieves a list of descendant sub nodes of <code>p_node</code>.
     * </P>
     */
    private static List getSubNodes(GxmlElement p_node)
    {
        if (p_node == null)
        {
            return null;
        }

        return p_node.getDescendantElements(GxmlElement.SUB_TYPE);
    }

    /**
     * Get the TB name this user could access
     * 
     * If enable TB access control, we should consider the TB this user could
     * access.
     * 
     * @param p_userId
     * @param tbName
     * @param tbId
     * @return
     */
    private static boolean tbFilter(String p_userId, String tbName, long tbId)
    {
        boolean canAccessTB = true;
        boolean isAdmin = UserUtil.isInPermissionGroup(p_userId,
                "Administrator");
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();
        if (!isAdmin && enableTBAccessControl)
        {
            ProjectTMTBUsers ptb = new ProjectTMTBUsers();
            List tbList = ptb.getTList(p_userId, "TB");
            boolean flag = false;
            Iterator it = tbList.iterator();
            while (it.hasNext())
            {
                long id = ((BigInteger) it.next()).longValue();
                if (tbId == id)
                {
                    flag = true;
                    break;
                }
            }
            if (!flag)
            {
                canAccessTB = false;
            }
        }
        return canAccessTB;
    }
}

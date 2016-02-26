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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.AddingSourcePage;
import com.globalsight.everest.page.AddingSourcePageManager;
import com.globalsight.everest.page.JobSourcePageDisplay;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.UpdateSourcePageManager;
import com.globalsight.everest.page.UpdatedSourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.zip.ZipIt;
import com.sun.jndi.toolkit.url.UrlUtil;

public class JobSourceFilesHandler extends PageHandler implements
        UserParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobSourceFilesHandler.class);

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        String action = p_request.getParameter("action");
        JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
        Job job = jobSummaryHelper.getJobByRequest(p_request);
        if ("downloadSourcePages".equals(action))
        {
            downloadSourcePages(p_request, p_response, job);
            return;
        }
        
        boolean isOk = jobSummaryHelper.packJobSummaryInfoView(p_request,
                p_response, p_context, job);
        if (!isOk)
        {
            return;
        }
        packJobSourceFilesInfoView(job, p_request);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void packJobSourceFilesInfoView(Job job,
            HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.removeElement("sourcePageIdList");
        p_request.setAttribute("addCheckBox", getAddCheckBox(p_request));
        p_request.setAttribute("cancelledWorkflow",
                getCancelledWorkflowExist(job));
        boolean atLeastOneError = false;

        List<SourcePage> sourcePages = (List<SourcePage>) job.getSourcePages();
        List<UpdatedSourcePage> uSourcdPages = UpdateSourcePageManager
                .getAllUpdatedSourcePage(job);
        List<AddingSourcePage> aSourcdPages = AddingSourcePageManager
                .getAllAddingSourcePage(job);

        for (SourcePage sourcePage : sourcePages)
        {
            if (sourcePage.hasRemoved())
            {
                sourcePages.remove(sourcePage);
            }
        }

        // sorts the pages in the correct order and store the column and sort
        // order
        // also filters them according to the search params
        List<Long> sourcePageIdList = new ArrayList<Long>();
        sourcePages = filterPagesByName(p_request, session, sourcePages);
		for (Iterator fi = sourcePages.iterator(); fi.hasNext();)
		{
			SourcePage page = (SourcePage) fi.next();
			sourcePageIdList.add(page.getId());
		}
		sessionMgr.setAttribute("sourcePageIdList", sourcePageIdList);
        sortPages(p_request, session, sourcePages);

        aSourcdPages = filterPagesByName(p_request, session, aSourcdPages);
        AddingSourcePageManager.sort(aSourcdPages);

        uSourcdPages = filterPagesByName(p_request, session, uSourcdPages);
        UpdateSourcePageManager.sort(uSourcdPages);

        // package jobSourcePageDisplayList
        List<JobSourcePageDisplay> jobSourcePageDisplayList = new ArrayList<JobSourcePageDisplay>();
        for (SourcePage sourcePage : sourcePages)
        {
            JobSourcePageDisplay jobSourcePageDisplay = new JobSourcePageDisplay(
                    sourcePage);
            jobSourcePageDisplay.setPageUrl(getPageUrl(job, sourcePage));
            jobSourcePageDisplay
                    .setDataSourceName(getDataSourceName(sourcePage));
            jobSourcePageDisplay.setWordCountOverriden(sourcePage
                    .isWordCountOverriden());
            jobSourcePageDisplay.setSourceLink(getSourceLink(sourcePage));
            jobSourcePageDisplayList.add(jobSourcePageDisplay);

            if (sourcePage.getPageState().equals(PageState.IMPORT_FAIL))
            {
                atLeastOneError = true;
            }
        }
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        Map<Long,String> targetLocaleMap = new HashMap<Long,String>();
        List<Workflow> workflows = new ArrayList<Workflow>(job.getWorkflows());
		for (Workflow workflow : workflows)
		{
			targetLocaleMap.put(workflow.getTargetLocale().getId(), workflow
					.getTargetLocale().getDisplayName(uiLocale));
		}
		p_request.setAttribute("targetLocaleMap", targetLocaleMap);
        p_request.setAttribute("shortOrFullPageNameDisplay",
                getShortOrFullPageNameDisplay(session));
        p_request.setAttribute("JobSourcePageDisplayList",
                jobSourcePageDisplayList);

        // package jobAddingSourcePageList
        p_request.setAttribute("JobAddingSourcePageList", aSourcdPages);
        // package jobUpdatedSourcePageList
        p_request.setAttribute("JobUpdatedSourcePageList", uSourcdPages);

        p_request.setAttribute("atLeastOneError", atLeastOneError);
        p_request.setAttribute("sourcePagesSize", sourcePages.size());
        p_request.setAttribute("canModifyWordCount", canModifyWordCount(job));
        p_request.setAttribute("wordCountOverridenAtAll",
                job.isWordCountOverriden());

        p_request.setAttribute("allowEditSourcePage",
                JobWorkflowsHandler.s_isGxmlEditorEnabled);
        p_request.setAttribute("canEditSourcePage",
                getCanEditSourcePage(job, session));
    }

    /**
     * Filter the pages by their name. Compare against the filter string.
     */
    protected List filterPagesByName(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
	{
		p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_PARAM,
				p_request.getParameter(JobManagementHandler.PAGE_SEARCH_PARAM));
		p_request
				.setAttribute(
						JobManagementHandler.PAGE_SEARCH_LOCALE,
						p_request
								.getParameter(JobManagementHandler.PAGE_SEARCH_LOCALE));
		p_request.setAttribute(JobManagementHandler.PAGE_TARGET_LOCAL,
				p_request.getParameter(JobManagementHandler.PAGE_TARGET_LOCAL));

		String thisFileSearch = (String) p_request
				.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
		String thisSearchText = (String) p_request
				.getParameter(JobManagementHandler.PAGE_SEARCH_TEXT);
		try
		{
			if (thisSearchText != null && !"".equals(thisSearchText))
			{
				thisSearchText = URLDecoder.decode(thisSearchText, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new EnvoyServletException(e1);
		}
		p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_TEXT,
				thisSearchText);
		String thisSearchLocale = (String) p_request
				.getAttribute(JobManagementHandler.PAGE_SEARCH_LOCALE);
		String targetLocaleId = (String) p_request
				.getAttribute(JobManagementHandler.PAGE_TARGET_LOCAL);

		if (thisSearchText != null && !"".equals(thisSearchText) && thisSearchLocale != null)
		{
			ArrayList newPages = new ArrayList();
			SessionManager sessionMgr = (SessionManager) p_session
					.getAttribute(WebAppConstants.SESSION_MANAGER);
			Job job = (Job) sessionMgr
					.getAttribute(WebAppConstants.WORK_OBJECT);
			String tuTableName = null;
			String tuvTableName = null;
			try
			{
				tuTableName = BigTableUtil.getTuTableJobDataInByJobId(job
						.getId());
				tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(job
						.getId());
			}
			catch (Exception e)
			{
				throw new EnvoyServletException(e);
			}

			long pageId = -1;
			long localeId = -1;
			for (Iterator fi = p_pages.iterator(); fi.hasNext();)
			{
				SourcePage page = (SourcePage) fi.next();
				if (thisSearchLocale.equals("sourceLocale"))
				{
					pageId = page.getId();
					localeId = page.getLocaleId();
				}
				else if (thisSearchLocale.equals("targetLocale"))
				{
					if (targetLocaleId != null)
					{
						StringBuffer localeIdStrBuffer = new StringBuffer();
						localeId = Long.parseLong(targetLocaleId);
						Set<TargetPage> targetSet = page.getTargetPages();
						for (Iterator tg = targetSet.iterator(); tg.hasNext();)
						{
							TargetPage target = (TargetPage) tg.next();
							if (target.getLocaleId() == localeId)
							{
								localeIdStrBuffer.append(target.getId())
										.append(",");
							}
						}
						if (localeIdStrBuffer.toString().endsWith(","))
						{
							String str = localeIdStrBuffer.toString()
									.substring(
											0,
											localeIdStrBuffer.toString()
													.lastIndexOf(","));
							pageId = Long.parseLong(str);
						}
					}
				}
				boolean check = TaskHelper.checkPageContainText(tuTableName,
						tuvTableName, thisSearchLocale, thisSearchText, pageId,
						localeId);

				if (check)
					newPages.add(page);
			}
			return newPages;
		}

		if (thisFileSearch != null)
		{
			ArrayList filteredFiles = new ArrayList();
			for (Iterator fi = p_pages.iterator(); fi.hasNext();)
			{
				Page p = (Page) fi.next();
				if (p.getExternalPageId().indexOf(thisFileSearch) >= 0)
				{
					filteredFiles.add(p);
				}
			}
			return filteredFiles;
		}
		// just return all - no filter
		return p_pages;
	}

    protected void sortPages(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
    {
        // first get job comparator from session
        PageComparator comparator = getPageComparator(p_session);

        String criteria = p_request
                .getParameter(JobManagementHandler.PAGE_SORT_PARAM);
        if (criteria != null)
        {
            int sortCriteria = Integer.parseInt(criteria);
            if (comparator.getSortColumn() == sortCriteria)
            {
                // just reverse the sort order
                comparator.reverseSortingOrder();
            }
            else
            {
                // set the sort column
                comparator.setSortColumn(sortCriteria);
            }
        }

        SortUtil.sort(p_pages, comparator);
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
    }

    private PageComparator getPageComparator(HttpSession p_session)
    {
        PageComparator comparator = (PageComparator) p_session
                .getAttribute(JobManagementHandler.PAGE_COMPARATOR);
        if (comparator == null)
        {
            comparator = new PageComparator(PageComparator.EXTERNAL_PAGE_ID);
            p_session.setAttribute(JobManagementHandler.PAGE_COMPARATOR,
                    comparator);
        }

        return comparator;
    }

    private boolean getAddCheckBox(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        return userPerms.getPermissionFor(Permission.EDIT_SOURCE_FILES)
                || userPerms.getPermissionFor(Permission.DELETE_SOURCE_FILES);
    }

    private boolean getCancelledWorkflowExist(Job job)
    {
        Object[] wfs = job.getWorkflows().toArray();
        boolean cancelled = true;
        // If all workflows of a job are discarded, don't show the
        // pages as links.
        for (int i = 0; cancelled && i < wfs.length; i++)
        {
            Workflow workflow = (Workflow) wfs[i];
            cancelled = workflow.getState().equals(Workflow.CANCELLED);
        }
        return cancelled;
    }

    private String getPageUrl(Job job, SourcePage sourcePage)
    {
        String pageUrl;
        if (sourcePage.getPrimaryFileType() == PrimaryFile.UNEXTRACTED_FILE)
        {
            UnextractedFile uf = (UnextractedFile) sourcePage.getPrimaryFile();
            pageUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING +  uf.getStoragePath().replace("\\", "/");
        }
        else
        {
            pageUrl = "&" + WebAppConstants.SOURCE_PAGE_ID + "="
                    + sourcePage.getId() + "&" + WebAppConstants.JOB_ID + "="
                    + job.getJobId();
        }
        return pageUrl;
    }

    private String getShortOrFullPageNameDisplay(HttpSession session)
    {
        UserParameter param = PageHandler.getUserParameter(session,
                UserParamNames.PAGENAME_DISPLAY);
        return param.getValue();
    }

    private String getDataSourceName(SourcePage p_sp)
    {
        String dataSourceType = p_sp.getDataSourceType();
        long dataSourceId = p_sp.getRequest().getDataSourceId();

        String currentRetString;
        try
        {
            if (dataSourceType.equals("db"))
            {
                currentRetString = getDBProfilePersistenceManager()
                        .getDatabaseProfile(dataSourceId).getName();
            }
            else
            {
                currentRetString = getFileProfilePersistenceManager()
                        .readFileProfile(dataSourceId).getName();
                // If source file is XLZ,here show the XLZ file profile name
                // instead of the XLF file profile name.
                boolean isXlzReferFP = getFileProfilePersistenceManager()
                        .isXlzReferenceXlfFileProfile(currentRetString);
                if (isXlzReferFP)
                {
                    currentRetString = currentRetString.substring(0,
                            currentRetString.length() - 4);
                }
            }
        }
        catch (Exception e)
        {
            currentRetString = "Unknown";
        }
        return currentRetString;
    }

    private DatabaseProfilePersistenceManager getDBProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getDatabaseProfilePersistenceManager();
    }

    private FileProfilePersistenceManager getFileProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getFileProfilePersistenceManager();
    }

    private String getSourceLink(SourcePage sourcePage)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("/globalsight" + WebAppConstants.VIRTUALDIR_CXEDOCS2);
        sb.append(CompanyWrapper.getCompanyNameById(sourcePage.getCompanyId()));
        sb.append("/");
        sb.append(SourcePage.filtSpecialFile(sourcePage.getExternalPageId()));
        String str = sb.toString();
        str = str.replace('\\', '/');
        try
        {
            str = UrlUtil.encode(str, "utf-8");
        }
        catch (Exception e)
        {
            str = URLEncoder.encode(str, "utf-8");
        }
        str = str.replace("%2F", "/");
        return str;
    }

    private boolean canModifyWordCount(Job job)
    {
        boolean canModify = job.getState().equals(Job.DISPATCHED)
                || job.getState().equals(Job.PENDING)
                || job.getState().equals(Job.READY_TO_BE_DISPATCHED)
                || job.getState().equals(Job.BATCHRESERVED)
                || job.getState().equals(Job.LOCALIZED);
        return canModify;
    }

    private boolean getCanEditSourcePage(Job job, HttpSession session)
    {
        boolean canEditSourcePage = false;
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        canEditSourcePage = perms.getPermissionFor(Permission.SOURCE_PAGE_EDIT);

        if (JobWorkflowsHandler.s_isGxmlEditorEnabled)
        {
            canEditSourcePage = perms
                    .getPermissionFor(Permission.SOURCE_PAGE_EDIT);

            // Can only edit pending or dispatched jobs...
            if (!Job.READY_TO_BE_DISPATCHED.equals(job.getState())
                    && !Job.DISPATCHED.equals(job.getState()))
            {
                canEditSourcePage = false;
            }
            // ... where none of the workflows are localizaed already
            else
            {
                ArrayList workflows = new ArrayList(job.getWorkflows());

                for (int i = 0, max = workflows.size(); i < max; i++)
                {
                    Workflow wf = (Workflow) workflows.get(i);

                    if (wf.getState().equals(Workflow.LOCALIZED))
                    {
                        canEditSourcePage = false;
                        break;
                    }
                }
            }
        }
        return canEditSourcePage;
    }

    private void downloadSourcePages(HttpServletRequest p_request,
            HttpServletResponse p_response, Job p_job) throws IOException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ResourceBundle bundle = getBundle(session);

        List sourcePages = (List) p_job.getSourcePages();

        Iterator it = sourcePages.iterator();
        String m_cxeDocsDir = SystemConfiguration.getInstance()
                .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR,
                		String.valueOf(p_job.getCompanyId()));
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> filePaths = new ArrayList<String>();
        Map<String, String> mapOfNamePath = new HashMap<String, String>();
        while (it.hasNext())
        {
            SourcePage sourcePage = (SourcePage) it.next();

            if (sourcePage.hasRemoved())
            {
                continue;
            }

            String pageName = sourcePage.getDisplayPageName();

            StringBuffer sourceSb = new StringBuffer().append(m_cxeDocsDir)
                    .append("/");
            String externalPageId = sourcePage.getExternalPageId();
            externalPageId = externalPageId.replace("\\", "/");

            if (PassoloUtil.isPassoloFile(sourcePage))
            {
                externalPageId = externalPageId.substring(0,
                        externalPageId.lastIndexOf(".lpu/") + 4);
            }

            externalPageId = SourcePage.filtSpecialFile(externalPageId);

            if (filePaths.contains(externalPageId))
                continue;

            sourceSb = sourceSb.append(externalPageId);
            filePaths.add(externalPageId);
            fileNames.add(sourceSb.toString());
            mapOfNamePath.put(sourceSb.toString(), externalPageId);
        }
        Map<String, String> entryNamesMap = new HashMap<String, String>();
        String jobName = p_job.getJobName();
        String zipFileName = URLEncoder.encode(jobName + ".zip");
        File tmpFile = File.createTempFile("GSDownloadSource", ".zip");
        try
        {
            JobPackageZipper m_zipper = new JobPackageZipper();
            m_zipper.createZipFile(tmpFile);
            entryNamesMap = ZipIt.getEntryNamesMap(filePaths);
            for (int i = 0; i < fileNames.size(); i++)
            {
                filePaths.set(i,
                        entryNamesMap.get(mapOfNamePath.get(fileNames.get(i))));
            }
            addSourcePages(m_zipper, fileNames, filePaths, zipFileName);
            m_zipper.closeZipFile();
            CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
            commentFilesDownload.sendFileToClient(p_request, p_response,
                    jobName + ".zip", tmpFile);
        }
        finally
        {
            FileUtil.deleteTempFile(tmpFile);
        }
    }

    private void addSourcePages(JobPackageZipper m_zipper,
            List<String> fileNames, List<String> filePaths, String zipFileName)
    {
        for (int i = 0; i < fileNames.size(); i++)
        {
            File file = new File(fileNames.get(i));

            if (!file.exists())
            {
                XmlEntities entity = new XmlEntities();
                File dir = file.getParentFile();
                if (dir.isDirectory())
                {
                    File[] files = dir.listFiles();
                    for (File f : files)
                    {
                        if (file.getName().equals(
                                entity.decodeStringBasic(f.getName())))
                        {
                            file = f;
                            break;
                        }
                    }
                }
            }

            if (file.exists())
            {
                FileInputStream input = null;
                try
                {
                    input = new FileInputStream(file);

                    m_zipper.writePath(filePaths.get(i));
                    m_zipper.writeFile(input);
                    input.close();
                }
                catch (IOException ex)
                {
                    CATEGORY.warn("cannot write comment file: " + file
                            + " to zip stream.", ex);
                }
                finally
                {
                    try
                    {
                        if (input != null)
                        {
                            input.close();
                        }
                    }
                    catch (IOException e)
                    {
                        CATEGORY.warn("cannot close comment file: " + file
                                + " to zip stream.", e);
                    }
                }
            }
        }
    }
}

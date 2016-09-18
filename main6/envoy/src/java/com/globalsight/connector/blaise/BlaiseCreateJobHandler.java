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
package com.globalsight.connector.blaise;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.cognitran.translation.client.TranslationPageCommand;
import com.globalsight.connector.blaise.form.BlaiseInboxEntryFilter;
import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.CreateJobsMainHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

public class BlaiseCreateJobHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(BlaiseCreateJobHandler.class);

    private static final int MAX_THREAD = 5;
    private static final int DEFAULT_PAGE_SIZE = 25;

    private static final String KEY = "blaiseEntryKey";

    private static final String NUM_PER_PAGE = KEY + TableConstants.NUM_PER_PAGE_STR;
    private static final String NUM_PAGES = KEY + TableConstants.NUM_PAGES;
    private static final String SORTING = KEY + TableConstants.SORTING;
    private static final String REVERSE_SORT = KEY + TableConstants.REVERSE_SORT;
    private static final String PAGE_NUM = KEY + TableConstants.PAGE_NUM;
    private static final String LAST_PAGE_NUM = KEY + TableConstants.LAST_PAGE_NUM;
    private static final String TOTAL_SIZE = KEY + TableConstants.LIST_SIZE;
    private static final String DO_SORT = KEY + TableConstants.DO_SORT;

    private int totalSize = 0;
    private List<TranslationInboxEntryVo> currPageEntries = null;
    // entry ID : TranslationInboxEntryVo
    private Map<Long, TranslationInboxEntryVo> currPageEntryMap = new HashMap<Long, TranslationInboxEntryVo>();

    private BlaiseInboxEntryFilter filter = null;

    // Cache helpers for performance: <blaise connector id>:<BlaiseHelper>
    private static ConcurrentHashMap<Long, BlaiseHelper> helpers = new ConcurrentHashMap<Long, BlaiseHelper>();
    
    @ActionHandler(action = "connect", formClass = "")
    public void prepareConnect(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        helpers.clear();
    }

    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.blaise.form.BlaiseInboxEntryFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	filter = (BlaiseInboxEntryFilter) form;

		SessionManager sessionMgr = getSessionManager(request);
		sessionMgr.setAttribute("relatedObjectIdFilter", filter.getRelatedObjectIdFilter());
		sessionMgr.setAttribute("sourceLocaleFilter", filter.getSourceLocaleFilter());
		sessionMgr.setAttribute("targetLocaleFilter", filter.getTargetLocaleFilter());
		sessionMgr.setAttribute("descriptionFilter", filter.getDescriptionFilter());
		sessionMgr.setAttribute("jobIdFilter", filter.getJobIdFilter());
    }

    private void fetchEntries(HttpServletRequest request)
    {
        SessionManager sessionMgr = getSessionManager(request);
        BlaiseConnector blc = getBlaiseConnector(request);
        BlaiseHelper helper = getBlaiseHelper(blc);

        int pageIndex = getPageNum(request);
        int pageSize = getNumPerPage(request);
        int sortBy = getSortBy(request);
        boolean sortDesc = false;
        String sourceLocaleFilter = (String) sessionMgr.getAttribute("sourceLocaleFilter");
        String targetLocaleFilter = (String) sessionMgr.getAttribute("targetLocaleFilter");
        String descriptionFilter = (String) sessionMgr.getAttribute("descriptionFilter");
        String relatedObjectIdFilter = (String) sessionMgr.getAttribute("relatedObjectIdFilter");
        String jobIdFilter = (String) sessionMgr.getAttribute("jobIdFilter");
        Set<Long> entryIds = getEntryIdsFromJobIdFilter(jobIdFilter);

        // For getInboxEntryCount, it does not care page index, page size, sort by, sort asc/desc.
        TranslationPageCommand command = BlaiseHelper.initTranslationPageCommand(pageIndex,
                pageSize, relatedObjectIdFilter, sourceLocaleFilter, targetLocaleFilter,
                descriptionFilter, sortBy, sortDesc);
        if (entryIds.size() > 0)
        {
            pageIndex = 1;
            command.setPageIndex(pageIndex);
            command.setSortDesc(isSortDesc(request, pageIndex, sortBy));
            currPageEntries = helper.listInboxByIds(entryIds, command);
            totalSize = currPageEntries.size();
        }
        else
        {
            if (StringUtil.isNotEmpty(jobIdFilter) && entryIds.size() == 0)
            {
                totalSize = 0;
                pageIndex = possibllyFixPageIndex(totalSize, pageSize, pageIndex);
                command.setPageIndex(pageIndex);
                command.setSortDesc(isSortDesc(request, pageIndex, sortBy));
                currPageEntries = new ArrayList<TranslationInboxEntryVo>();
            }
            else
            {
                totalSize = helper.getInboxEntryCount(command);
                pageIndex = possibllyFixPageIndex(totalSize, pageSize, pageIndex);
                command.setPageIndex(pageIndex);
                command.setSortDesc(isSortDesc(request, pageIndex, sortBy));
                currPageEntries = helper.listInbox(command);
            }
        }

        currPageEntryMap.clear();
        for (TranslationInboxEntryVo entry : currPageEntries)
        {
            currPageEntryMap.put(entry.getId(), entry);
        }
    }

    private Set<Long> getEntryIdsFromJobIdFilter(String jobIdFilter)
    {
        Set<Long> entryIds = new HashSet<Long>();
        if (StringUtil.isEmpty(jobIdFilter))
            return entryIds;

        for (String jobId : jobIdFilter.split(","))
        {
            try
            {
                List<BlaiseConnectorJob> blaiseJobs = BlaiseManager
                        .getBlaiseConnectorJobByJobId(Long.parseLong(jobId.trim()));
                if (blaiseJobs != null && blaiseJobs.size() > 0)
                {
                    for (BlaiseConnectorJob bcj : blaiseJobs)
                    {
                        entryIds.add(bcj.getBlaiseEntryId());
                    }
                }
            }
            catch (NumberFormatException e)
            {

            }
        }

        return entryIds;
    }

    private int possibllyFixPageIndex(int totalSize, int pageSize, int pageIndex)
    {
        int numOfPages = getNumOfPages(totalSize, pageSize);
        if ((totalSize % pageSize == 0) && (pageIndex * pageSize > totalSize))
        {
            pageIndex--;
            if (pageIndex == 0)
            {
                pageIndex = 1;
            }
        }
        if (pageIndex > numOfPages)
        {
            pageIndex = 1;
        }
        return pageIndex;
    }

    private boolean isSortDesc(HttpServletRequest request, int pageIndex, int sortBy)
    {
        boolean isSortDesc = false;

        SessionManager sessionMgr = getSessionManager(request);
        String isSortDescStr = (String) sessionMgr.getAttribute("isSortDesc");
        if (StringUtil.isNotEmpty(isSortDescStr))
        {
            isSortDesc = "true".equals(isSortDescStr) ? true : false;
        }

        Integer lastSortChoice = (Integer) sessionMgr.getAttribute(SORTING);
        Integer currentSortChoice = new Integer(sortBy);
        String doSort = (String) request.getParameter(DO_SORT);
        // click column header to reverse sorting
        if (doSort != null)
        {
            if (lastSortChoice != null && lastSortChoice.equals(currentSortChoice))
            {
                isSortDesc = !isSortDesc;
            }
            else
            {
                isSortDesc = false;
            }
        }
        // page navigation/filtering/page size change
        else
        {
            if (lastSortChoice == null || !lastSortChoice.equals(currentSortChoice))
            {
                isSortDesc = false;
            }
        }

        sessionMgr.setAttribute("isSortDesc", String.valueOf(isSortDesc));

        return isSortDesc;
    }

    @ActionHandler(action = "claim", formClass = "")
    public void claim(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
    	BlaiseConnector blc = getBlaiseConnector(p_request);
    	BlaiseHelper helper = getBlaiseHelper(blc);

    	String selectedEntryIds = p_request.getParameter("entryIds");
    	String[] ids = selectedEntryIds.split(",");
    	for (String id : ids)
    	{
    		long longId = Long.parseLong(id.trim());
    		TranslationInboxEntryVo entry = currPageEntryMap.get(longId);
    		if (entry != null && entry.getEntry().isGroup())
    		{
    			helper.claim(longId);
    		}
    	}
    }

    /**
     * Upload the job attachment file to server, and store it in session for
     * later use.
     */
    @ActionHandler(action = "checkTargetLocalesUrl", formClass = "")
    public void checkTargetLocalesUrl(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	List<TranslationInboxEntryVo> selectedEntries = new ArrayList<TranslationInboxEntryVo>();
    	String entryIds = request.getParameter("entryIds");
    	if (entryIds != null) {
    		for (String entryId : entryIds.split(",")) {
    			try {
        			long id = Long.parseLong(entryId);
        			TranslationInboxEntryVo vo = currPageEntryMap.get(id);
        			if (vo != null) {
            			selectedEntries.add(vo);
        			}
    			} catch(Exception e) {
    				logger.error(e);
    			}
    		}
    	}

    	HashSet<Locale> l10nLocales = new HashSet<Locale>();
    	String l10Nid = request.getParameter("l10Nid");
        if (StringUtils.isNotEmpty(l10Nid))
        {
			L10nProfile l10nP = ServerProxy.getProjectHandler().getL10nProfile(
					Long.parseLong(l10Nid));
        	for (Object obj : l10nP.getWorkflowTemplateInfos())
        	{
        		WorkflowTemplateInfo wti = (WorkflowTemplateInfo) obj;
        		l10nLocales.add(wti.getTargetLocale().getLocale());
        	}
        }

        Iterator<TranslationInboxEntryVo> it = selectedEntries.iterator();
        while (it.hasNext())
        {
        	if (l10nLocales.contains(it.next().getTargetLocale())) {
        		it.remove();
        	}
        }

        String result = "";
        StringBuilder ids = new StringBuilder();
        if (selectedEntries.size() > 0) {
        	for (TranslationInboxEntryVo vo : selectedEntries) {
        		ids.append(vo.getId()).append(",");
        	}
        	result = ids.toString();
        	result = result.substring(0, result.length() - 1);
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(result.getBytes("UTF-8"));

        pageReturn();
    }

    /**
     * Get job attributes
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    @ActionHandler(action = "checkAttributeRequired", formClass = "")
    public void checkAttributeRequired(HttpServletRequest request, HttpServletResponse response,
            Object form) throws IOException
    {
        PrintWriter writer = response.getWriter();
        try
        {
            String l10Nid = request.getParameter("l10Nid");
            String hasAttribute = "false";
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(Long.valueOf(l10Nid));
            Project p = lp.getProject();
            AttributeSet attributeSet = p.getAttributeSet();

            if (attributeSet != null)
            {
                List<Attribute> attributeList = attributeSet.getAttributeAsList();
                for (Attribute attribute : attributeList)
                {
                    if (attribute.isRequired())
                    {
                        hasAttribute = "required";
                        break;
                    }
                    else
                    {
                        hasAttribute = "true";
                    }
                }
            }
            response.setContentType("text/html;charset=UTF-8");
            writer.write(hasAttribute);
        }
        catch (Exception e)
        {
            logger.error("Failed to query job attributes of project.", e);
        }
        finally
        {
            writer.close();
        }
        
        pageReturn();
    }

    @ActionHandler(action = "createBlaiseJob", formClass = "com.globalsight.connector.blaise.form.CreateBlaiseJobForm")
    public void createBlaiseJob(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	CreateBlaiseJobForm blaiseForm = (CreateBlaiseJobForm) form;
        SessionManager sessionMgr = this.getSessionManager(request);

        // Have to reset?
        resetParameters(request, blaiseForm);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }

        long blcId = Long.parseLong(blaiseForm.getBlaiseConnectorId());
        BlaiseConnector blc = BlaiseManager.getBlaiseConnectorById(blcId);

        File attachFile = null;
        String jobCommentFilePathName = (String) sessionMgr.getAttribute("uploadAttachment");
        if (jobCommentFilePathName != null)
        {
            attachFile = new File(jobCommentFilePathName);
        }
        sessionMgr.removeElement("uploadAttachment");
        String attachFileName = request.getParameter("attachment");

        List<Long> entryIds = new ArrayList<Long>();
        List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
        String[] ffs = blaiseForm.getFileMapFileProfile().split(",");
        for (String ff : ffs)
        {
            String[] f = ff.split("-");
            entryIds.add(Long.parseLong(f[0]));
            fileProfileList.add(HibernateUtil.get(FileProfileImpl.class, Long.parseLong(f[1])));
        }

        // Claim all entries one by one.
        BlaiseHelper helper = getBlaiseHelper(blc);
        for (int i = 0; i < entryIds.size(); i++)
        {
            TranslationInboxEntryVo curEntry = currPageEntryMap.get(entryIds.get(i));
        	if (curEntry.getEntry().isGroup())
        	{
                helper.claim(entryIds.get(i));
        	}
        }

        long l10Id = fileProfileList.get(0).getL10nProfileId();
        BasicL10nProfile l10Profile = HibernateUtil.get(BasicL10nProfile.class, l10Id);

        String publicUuid = (String) sessionMgr.getAttribute("uuid");
        sessionMgr.removeElement("uuid");
        File srcFolder = null;
        if (publicUuid != null)
        {
            srcFolder = new File(AmbFileStoragePathUtils.getJobAttributeDir(), publicUuid);
        }

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        // Entries with same source and target locale will be in one job.
        String combineByLangs = request.getParameter("combineByLangs");
        if ("true".equalsIgnoreCase(combineByLangs))
        {
            // Group TranslationInboxEntryVo objects by source and target.
            Map<String, List<Integer>> localeGroups = groupEntriesByLangs(entryIds);

            Iterator<Entry<String, List<Integer>>> ite = localeGroups.entrySet().iterator();
            while (ite.hasNext())
            {
                List<TranslationInboxEntryVo> entries = new ArrayList<TranslationInboxEntryVo>();
                List<FileProfile> fileProfiles = new ArrayList<FileProfile>();

                Map.Entry<String, List<Integer>> entry = ite.next();
                for (Integer i : entry.getValue())
                {
                    entries.add(currPageEntryMap.get(entryIds.get(i)));
                    fileProfiles.add(fileProfileList.get(i));
                }

                String uuid = JobImpl.createUuid();
                if (srcFolder != null && srcFolder.exists())
                {
                    // Locate file attribute by UUID
                    File trgFolder = new File(AmbFileStoragePathUtils.getJobAttributeDir(), uuid);
                    FileUtil.copyFolder(srcFolder, trgFolder);
                }

                List<JobAttribute> jobAttribtues = getJobAttributes(
                        blaiseForm.getAttributeString(), l10Profile);
                CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user, currentCompanyId,
                        blc, blaiseForm, entries, fileProfiles, attachFile, attachFileName, uuid,
                        jobAttribtues);
                Thread t = new MultiCompanySupportedThread(runnable);
                pool.execute(t);
            }
        }
        else
        {
            // Every entry creates one job with one workflow
            for (int i = 0; i < entryIds.size(); i++)
            {
                TranslationInboxEntryVo curEntry = currPageEntryMap.get(entryIds.get(i));
                List<TranslationInboxEntryVo> entries = new ArrayList<TranslationInboxEntryVo>();
                entries.add(curEntry);

                FileProfile curFileProfile = fileProfileList.get(i);
                List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
                fileProfiles.add(curFileProfile);

                String uuid = JobImpl.createUuid();
                if (srcFolder != null && srcFolder.exists())
                {
                    // Locate file attribute by UUID
                    File trgFolder = new File(AmbFileStoragePathUtils.getJobAttributeDir(), uuid);
                    FileUtil.copyFolder(srcFolder, trgFolder);
                }

                List<JobAttribute> jobAttribtues = getJobAttributes(
                        blaiseForm.getAttributeString(), l10Profile);
                CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user, currentCompanyId,
                        blc, blaiseForm, entries, fileProfiles, attachFile, attachFileName, uuid,
                        jobAttribtues);
                Thread t = new MultiCompanySupportedThread(runnable);
                pool.execute(t);
            }
        }
		pool.shutdown();

		if (srcFolder != null && srcFolder.exists())
        {
        	FileUtil.deleteFile(srcFolder);
        }

        pageReturn();
    }

    @ActionHandler(action = "uploadAttachment", formClass = "")
    public void uploadAttachment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FileUploader uploader = new FileUploader();
        File file = uploader.upload(request);

		getSessionManager(request).setAttribute("uploadAttachment",
				file.getAbsolutePath());

        PrintWriter writer = response.getWriter();
        writer.write("<script type='text/javascript'>window.parent.addAttachment(' ')</script>;");

        pageReturn();
    }

    @Override
	public void beforeAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
    {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html");
    }

	@Override
	public void afterAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
	{
	    fetchEntries(request);

        setLables(request, PageHandler.getBundle(request.getSession(false)));

        // All XLF 1.2 file profiles.
        request.setAttribute("fps", getXlfFileProfileOptions(request));

        setEntryInfo(request);

        // Ensure the blaise connector is always in session manager.
        BlaiseConnector blc = getBlaiseConnector(request);
		getSessionManager(request).setAttribute("blaiseConnector", blc);

		setJobIdsForEntries(blc.getId(), currPageEntries);

		setCreatingJobsNum(request);

		setBlaiseLocalesInfo(request);

		dataForTable(request);
	}

	private void resetParameters(HttpServletRequest request, CreateBlaiseJobForm blaiseForm)
	{
        String blaiseConnectorId = request.getParameter("blaiseConnectorId");
        if (StringUtil.isNotEmpty(blaiseConnectorId))
        {
            blaiseForm.setBlaiseConnectorId(blaiseConnectorId);
        }
        String attributeString = request.getParameter("attributeString");
        if (StringUtil.isNotEmpty(attributeString))
        {
            blaiseForm.setAttributeString(attributeString);
        }
        String fileMapFileProfile = request.getParameter("fileMapFileProfile");
        if (StringUtil.isNotEmpty(fileMapFileProfile))
        {
            blaiseForm.setFileMapFileProfile(fileMapFileProfile);
        }
        String priority = request.getParameter("priority");
        if (StringUtil.isNotEmpty(priority))
        {
            blaiseForm.setPriority(priority);
        }
        String comment = request.getParameter("comment");
        blaiseForm.setComment(comment);
	}

	private void setEntryInfo(HttpServletRequest request)
			throws LocaleManagerException, RemoteException, GeneralException
	{
		HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        HashMap<Long, String> id2FileNameMap = new HashMap<Long, String>();
        HashMap<Long, String> id2LocaleMap =  new HashMap<Long, String>();
        for (TranslationInboxEntryVo entry : currPageEntries)
        {
            id2FileNameMap.put(entry.getId(), BlaiseHelper.getEntryFileName(entry));

			Locale javaLocale = entry.getTargetLocale();
			String localeCode = BlaiseHelper.fixLocale(javaLocale.getLanguage()
					+ "_" + javaLocale.getCountry());
			StringBuilder sb = new StringBuilder(localeCode).append(" (")
					.append(javaLocale.getDisplayLanguage(uiLocale))
					.append("_").append(javaLocale.getDisplayCountry(uiLocale))
					.append(")");
				id2LocaleMap.put(entry.getId(), sb.toString());
        }

        getSessionManager(request).setAttribute("id2FileNameMap", id2FileNameMap);
        getSessionManager(request).setAttribute("id2LocaleMap", id2LocaleMap);
	}

    private void setBlaiseLocalesInfo(HttpServletRequest request)
    {
        getSessionManager(request).setAttribute("allBlaiseLocales",
                BlaiseHelper.blaiseSupportedLocales);
    }

	private String getXlfFileProfileOptions(HttpServletRequest request)
    {
        StringBuffer s = new StringBuffer();
        try
        {
            ArrayList<FileProfileImpl> fps = getAllXlf12FileProfiles(request);
            if (fps.size() != 1)
            {
                s.append("<option class=\"-1\" value=\"-1\"></option>");
            }

            for (FileProfileImpl fp : fps)
            {
                s.append("<option class=\"").append(fp.getL10nProfileId())
                        .append("\" value=\"").append(fp.getId()).append("\"");
                
                if (fps.size() == 1)
                    s.append(" selected=\"selected\" ");
                
                s.append(">");
                s.append(fp.getName()).append("</option>");
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return s.toString();
    }

    /**
     * Get all xliff 1.2 file profiles associated with current user/company.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList<FileProfileImpl> getAllXlf12FileProfiles(
            HttpServletRequest request) throws Exception
    {
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        SessionManager sessionMgr = this.getSessionManager(request);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }

        ArrayList<FileProfileImpl> fileProfileListOfUser = new ArrayList<FileProfileImpl>();
        List<String> extensionList = new ArrayList<String>();
        extensionList.add("xlf");
        extensionList.add("xliff");
        List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                .getFileProfilePersistenceManager().getFileProfilesByExtension(
                        extensionList, Long.valueOf(currentCompanyId));
        SortUtil.sort(fileProfileListOfCompany, new Comparator<Object>()
        {
            public int compare(Object arg0, Object arg1)
            {
                FileProfileImpl a0 = (FileProfileImpl) arg0;
                FileProfileImpl a1 = (FileProfileImpl) arg1;
                return a0.getName().compareToIgnoreCase(a1.getName());
            }
        });

        List projectsOfCurrentUser = ServerProxy.getProjectHandler()
                .getProjectsByUser(user.getUserId());

        for (FileProfileImpl fp : fileProfileListOfCompany)
        {
            Project fpProj = getProject(fp);
            // get the project and check if it is in the group of user's
            // projects.
			if (projectsOfCurrentUser.contains(fpProj)
					&& fp.getKnownFormatTypeId() == 39)// xliff 1.2
            {
                fileProfileListOfUser.add(fp);
            }
        }

        return fileProfileListOfUser;
    }

    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get the project that file profile "
                            + p_fp.toString() + " is associated with.", e);
        }
        return p;
    }

	private void setJobIdsForEntries(long blaiseConnectorId,
			List<TranslationInboxEntryVo> inboxEntries)
    {
		List<Long> entryIds = new ArrayList<Long>();
		for (TranslationInboxEntryVo vo : inboxEntries)
		{
			entryIds.add(vo.getId());
		}

		try
		{
			Map<Long, List<Long>> map = BlaiseManager.getEntryId2JobIdsMap(
					entryIds, blaiseConnectorId);
			for (TranslationInboxEntryVo vo : inboxEntries)
			{
				List<Long> jobIds = map.get(vo.getId());
				if (jobIds != null)
				{
					vo.setJobIds(jobIds);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e);
		}
    }

	private void setCreatingJobsNum(HttpServletRequest request)
	{
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Integer creatingJobsNum = CreateJobsMainHandler.getCreatingJobsNum(Long
                .parseLong(currentCompanyId));
        request.setAttribute("creatingJobsNum", creatingJobsNum);
	}

    @SuppressWarnings("rawtypes")
    private void dataForTable(HttpServletRequest request) throws GeneralException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        int numPerPage = getNumPerPage(request);
        int numOfPages = getNumOfPages(totalSize, numPerPage);
        int pageNum = this.getCurrentPageNum(request, totalSize, numPerPage);

        Integer lastPageNumber = (Integer) sessionManager.getAttribute(LAST_PAGE_NUM);

        int sortChoice = getSortBy(request);

        Boolean reverseSort = Boolean.FALSE;
        // Compare to the last sort choice. If the sort choice has changed,
        // then kick them back to page one
        Integer lastSortChoice = (Integer) sessionManager.getAttribute(SORTING);

        Integer currentSortChoice = new Integer(sortChoice);
        reverseSort = (Boolean) sessionManager.getAttribute(REVERSE_SORT);
        if (reverseSort == null)
        {
            reverseSort = Boolean.FALSE;
            sessionManager.setAttribute(REVERSE_SORT, reverseSort);
        }
        Integer currentPageNumber = new Integer(pageNum);
        if (lastPageNumber == null)
        {
            lastPageNumber = currentPageNumber;
            sessionManager.setAttribute(LAST_PAGE_NUM, lastPageNumber);
        }

        // "doSort" should be passed in the url on column headers. This
        // is so that when another button on that page returns to the same
        // page, it keeps the same sort, rather than reversing it.
        String doSort = (String) request.getParameter(DO_SORT);
        if (lastSortChoice == null)
        {
            sessionManager.setAttribute(SORTING, currentSortChoice);
        }
        else if (doSort != null)
        {
            // see if the user stayed on the same page and
            // clicked a sort column header
            if (lastSortChoice.equals(currentSortChoice))
            {
                // flip the sort direction (no auto refresh on this page)
                // if they clicked the same link again on the same page
                if (lastPageNumber.equals(currentPageNumber))
                {
                    reverseSort = new Boolean(!reverseSort.booleanValue());
                    sessionManager.setAttribute(REVERSE_SORT, reverseSort);
                }
            }
            else
            {
                reverseSort = Boolean.FALSE;
                sessionManager.setAttribute(REVERSE_SORT, reverseSort);
            }
            sessionManager.setAttribute(SORTING, currentSortChoice);
        }

        if (currPageEntries == null)
            request.setAttribute("blaiseEntryList", new ArrayList());
        else
            request.setAttribute("blaiseEntryList", currPageEntries);

        request.setAttribute(PAGE_NUM, new Integer(pageNum));
        request.setAttribute(NUM_PAGES, new Integer(numOfPages));
        request.setAttribute(NUM_PER_PAGE, new Integer(numPerPage));
        request.setAttribute(TOTAL_SIZE, new Integer(totalSize));

        // remember the sortChoice and pageNumber
        int current = currentPageNumber.intValue();
        if (current > numOfPages && numOfPages != 0)
        {
            sessionManager.setAttribute(LAST_PAGE_NUM, new Integer(current - 1));
        }
        else
        {
            sessionManager.setAttribute(LAST_PAGE_NUM, currentPageNumber);
        }
    }

    private int getPageNum(HttpServletRequest request)
    {
        String currentPageNumber = (String) request.getParameter(PAGE_NUM);
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Integer lastPageNumber = (Integer) sessionManager.getAttribute(LAST_PAGE_NUM);

        int pageNum = 1;
        if (currentPageNumber != null)
        {
            pageNum = Integer.parseInt(currentPageNumber);
        }
        else if (lastPageNumber != null)
        {
            pageNum = lastPageNumber.intValue();
        }

        return pageNum;
    }

    private int getNumPerPage(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Integer orgSize = (Integer) session.getAttribute("blaiseConnectorPageSize");
        int pageSize = orgSize == null ? DEFAULT_PAGE_SIZE : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                pageSize = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                pageSize = Integer.MAX_VALUE;
            }

            session.setAttribute("blaiseConnectorPageSize", pageSize);
        }

        return pageSize;
    }

    private int getCurrentPageNum(HttpServletRequest request, int totalSize, int pageSize)
    {
        int numOfPages = getNumOfPages(totalSize, pageSize);

        SessionManager sessionManager = getSessionManager(request);
        Integer lastPageNumber = (Integer) sessionManager.getAttribute(LAST_PAGE_NUM);
        String currentPageNumber = (String) request.getParameter(PAGE_NUM);

        int pageNum = 1;
        if (currentPageNumber != null)
        {
            pageNum = Integer.parseInt(currentPageNumber);
        }
        else if (lastPageNumber != null)
        {
            pageNum = lastPageNumber.intValue();
        }

        // GBS-1322 problem (4).
        // Page number will be set to previous or no result page if removing the
        // record which is the only one in current page.
        if ((totalSize % pageSize == 0) && (pageNum * pageSize > totalSize))
        {
            pageNum--;
            if (pageNum == 0)
            {
                pageNum = 1;
            }
        }
        if (pageNum > numOfPages)
        {
            pageNum = 1;
        }
        return pageNum;
    }

    private int getSortBy(HttpServletRequest request)
    {
        int sortChoice = 0;

        String sortType = (String) request.getParameter(SORTING);
        if (sortType == null)
        {
            SessionManager sessionManager = getSessionManager(request);
            Integer sortTypeInt = (Integer) sessionManager.getAttribute(SORTING);
            if (sortTypeInt != null)
                sortType = sortTypeInt.toString();
        }
        if (sortType != null)
        {
            sortChoice = Integer.parseInt(sortType);
        }
        return sortChoice;
    }

    // get total number of pages that can be displayed.
    private int getNumOfPages(int numOfItems, int perPage)
    {
        if (perPage == 0)
        {
            return perPage;
        }
        // List of templates
        int remainder = numOfItems % perPage;

        return remainder == 0 ? (numOfItems / perPage) : ((numOfItems - remainder) / perPage) + 1;
    }

    private void setLables(HttpServletRequest request, ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_name");// name
        setLableToJsp(request, bundle, "lb_status");// status
        setLableToJsp(request, bundle, "lb_size");// size
        setLableToJsp(request, bundle, "lb_file_profile");// file profile
        setLableToJsp(request, bundle, "lb_target_locales");// target locales
        setLableToJsp(request, bundle, "lb_create_job");// create job
        setLableToJsp(request, bundle, "lb_create_job_without_java");
        setLableToJsp(request, bundle, "lb_add_files");
        setLableToJsp(request, bundle, "lb_browse");// Browse
        setLableToJsp(request, bundle, "lb_cancel");// Cancel
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_invalid_job_name_1");
        setLableToJsp(request, bundle, "jsmsg_choose_file_profiles_for_all_files");
        setLableToJsp(request, bundle, "lb_import_select_target_locale");
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_customer_comment");
        setLableToJsp(request, bundle, "jsmsg_comment_must_be_less");
        setLableToJsp(request, bundle, "lb_total");// Total
        setLableToJsp(request, bundle, "lb_uploaded");
        setLableToJsp(request, bundle, "msg_failed");
        setLableToJsp(request, bundle, "msg_job_add_files");
        setLableToJsp(request, bundle, "msg_job_folder_confirm");
        setLableToJsp(request, bundle, "help_create_job");
        setLableToJsp(request, bundle, "msg_job_create_empty_file");
        setLableToJsp(request, bundle, "msg_job_create_exist");
        setLableToJsp(request, bundle, "msg_job_create_large_file");
        setLableToJsp(request, bundle, "highest");
        setLableToJsp(request, bundle, "major");
        setLableToJsp(request, bundle, "normal");
        setLableToJsp(request, bundle, "lower");
        setLableToJsp(request, bundle, "lowest");
        setLableToJsp(request, bundle, "lb_attachment");
        setLableToJsp(request, bundle, "lb_reference_file");
        setLableToJsp(request, bundle, "lb_uploaded_files");
        setLableToJsp(request, bundle, "lb_clear_profile");
        setLableToJsp(request, bundle, "msg_job_attachment_uploading");
        setLableToJsp(request, bundle, "lb_create_job_uploaded_files_tip");
        setLableToJsp(request, bundle, "lb_create_job_clean_map_tip");
        setLableToJsp(request, bundle, "lb_create_job_add_file_tip");
        setLableToJsp(request, bundle, "lb_create_job_browse_tip");
        setLableToJsp(request, bundle, "lb_job_creating");
        setLableToJsp(request, bundle, "lb_jobs_creating");
        setLableToJsp(request, bundle, "lb_job_attributes");
        setLableToJsp(request, bundle, "msg_set_job_attributes");
        setLableToJsp(request, bundle, "msg_blaise_no_workflow");
        setLableToJsp(request, bundle, "lb_blaise_create_job_add_file_tip");
        setLableToJsp(request, bundle, "lb_blaise_combine_by_languages");
    }

    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

	private BlaiseConnector getBlaiseConnector(HttpServletRequest request)
	{
		SessionManager sessionMgr = getSessionManager(request);
		BlaiseConnector connector = (BlaiseConnector) sessionMgr
				.getAttribute("blaiseConnector");
		if (connector == null)
		{
			// From "blaiseEntriesForm"
			String blcId = request.getParameter("blcId");
			if (blcId == null) {
				// From "createJobForm"
				blcId = request.getParameter("blaiseConnectorId");
			}
			if (blcId != null) {
				connector = BlaiseManager.getBlaiseConnectorById(Long
						.parseLong(blcId));
			}
		}

		// To be safe
		if (connector != null) {
			sessionMgr.setAttribute("blaiseConnector", connector);
		}

		return connector;
	}

    private List<JobAttribute> getJobAttributes(String attributeString,
            BasicL10nProfile l10Profile)
    {
        List<JobAttribute> jobAttributeList = new ArrayList<JobAttribute>();

        if (l10Profile.getProject().getAttributeSet() == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(attributeString))
        {
            String[] attributes = attributeString.split(";.;");
            for (String ele : attributes)
            {
                try
                {
                    String attributeId = ele.substring(ele.indexOf(",.,") + 3,
                            ele.lastIndexOf(",.,"));
                    String attributeValue = ele.substring(ele
                            .lastIndexOf(",.,") + 3);

                    Attribute attribute = HibernateUtil.get(Attribute.class,
                            Long.parseLong(attributeId));
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(attribute.getCloneAttribute());
                    if (attribute != null
                            && StringUtils.isNotEmpty(attributeValue))
                    {
                        Condition condition = attribute.getCondition();
                        if (condition instanceof TextCondition)
                        {
                            jobAttribute.setStringValue(attributeValue);
                        }
                        else if (condition instanceof IntCondition)
                        {
                            jobAttribute.setIntegerValue(Integer
                                    .parseInt(attributeValue));
                        }
                        else if (condition instanceof FloatCondition)
                        {
                            jobAttribute.setFloatValue(Float
                                    .parseFloat(attributeValue));
                        }
                        else if (condition instanceof DateCondition)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    DateCondition.FORMAT);
                            jobAttribute
                                    .setDateValue(sdf.parse(attributeValue));
                        }
                        else if (condition instanceof ListCondition)
                        {
                            String[] options = attributeValue.split("#@#");
                            List<String> optionValues = Arrays.asList(options);
                            jobAttribute.setValue(optionValues, false);
                        }
                    }
                    jobAttributeList.add(jobAttribute);
                }
                catch (Exception e)
                {
                    logger.error("Failed to get job attributes", e);
                }
            }
        }
        else
        {
            List<Attribute> attsList = l10Profile.getProject()
                    .getAttributeSet().getAttributeAsList();
            for (Attribute att : attsList)
            {
                JobAttribute jobAttribute = new JobAttribute();
                jobAttribute.setAttribute(att.getCloneAttribute());
                jobAttributeList.add(jobAttribute);
            }
        }

        return jobAttributeList;
    }

    /**
     * Group TranslationInboxEntryVo objects by source and target locales.
     * Entries with same source and target locale will be in same job.
     */
    private Map<String, List<Integer>> groupEntriesByLangs(List<Long> entryIds)
    {
        Map<String, List<Integer>> localeGroups = new HashMap<String, List<Integer>>();
        for (int i = 0; i < entryIds.size(); i++)
        {
            String key = getInboxEntryKey(currPageEntryMap.get(entryIds.get(i)));
            List<Integer> entryIndexes = localeGroups.get(key);
            if (entryIndexes == null)
            {
                entryIndexes = new ArrayList<Integer>();
                localeGroups.put(key, entryIndexes);
            }
            entryIndexes.add(new Integer(i));
        }

        return localeGroups;
    }

    /**
     * Create a key by source and target locale information.
     * A sample is "en_US_zh_CN".
     */
    private String getInboxEntryKey(TranslationInboxEntryVo entry)
    {
        StringBuilder key = new StringBuilder();
        key.append(entry.getSourceLocale().getLanguage());
        key.append("_");
        key.append(entry.getSourceLocale().getCountry());
        key.append("_");
        key.append(entry.getTargetLocale().getLanguage());
        key.append("_");
        key.append(entry.getTargetLocale().getCountry());

        return key.toString();
    }

    private BlaiseHelper getBlaiseHelper(BlaiseConnector blc)
    {
        BlaiseHelper helper = helpers.get(blc.getIdAsLong());
        if (helper == null)
        {
            helper = new BlaiseHelper(blc);
            helpers.put(blc.getIdAsLong(), helper);
        }

        return helper;
    }

    private SessionManager getSessionManager(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		return (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
	}
}
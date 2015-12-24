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
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.connector.blaise.form.BlaiseInboxEntryFilter;
import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.customAttribute.Attribute;
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
import com.globalsight.everest.util.comparator.BlaiseInboxEntryComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.CreateJobsMainHandler;
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

    private List<TranslationInboxEntryVo> allInboxEntries = null;
    private List<TranslationInboxEntryVo> inboxEntries = null;
    private Map<Long, TranslationInboxEntryVo> allInboxEntryMap = new HashMap<Long, TranslationInboxEntryVo>();

    private BlaiseInboxEntryFilter filter = null;

    @ActionHandler(action = "connect", formClass = "")
    public void prepareConnect(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
    	refreshAllInboxEntries(p_request);
    }

    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.blaise.form.BlaiseInboxEntryFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		if (allInboxEntries == null) {
			refreshAllInboxEntries(request);
		}

    	filter = (BlaiseInboxEntryFilter) form;
		inboxEntries = filter.filter(allInboxEntries);

		SessionManager sessionMgr = getSessionManager(request);
		sessionMgr.setAttribute("idFilter", filter.getIdFilter());
		sessionMgr.setAttribute("sourceLocaleFilter", filter.getSourceLocaleFilter());
		sessionMgr.setAttribute("targetLocaleFilter", filter.getTargetLocaleFilter());
		sessionMgr.setAttribute("descriptionFilter", filter.getDescriptionFilter());
		sessionMgr.setAttribute("jobIdFilter", filter.getJobIdFilter());
    }

    @ActionHandler(action = "claim", formClass = "")
    public void claim(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
    	// Refresh to get latest entries
    	refreshAllInboxEntries(p_request);

    	BlaiseConnector blc = getBlaiseConnector(p_request);
    	BlaiseHelper helper = new BlaiseHelper(blc);

    	boolean flag = false;
    	String selectedEntryIds = p_request.getParameter("entryIds");
    	String[] ids = selectedEntryIds.split(",");
    	for (String id : ids)
    	{
    		long longId = Long.parseLong(id.trim());
    		TranslationInboxEntryVo entry = allInboxEntryMap.get(longId);
    		if (entry != null && entry.getEntry().isGroup())
    		{
    			helper.claim(longId);
    			flag = true;
    		}
    	}

		// Refresh to get latest entries again
		if (flag)
		{
			refreshAllInboxEntries(p_request);
		}

		if (filter != null)
		{
			inboxEntries = filter.filter(allInboxEntries);
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
        			TranslationInboxEntryVo vo = allInboxEntryMap.get(id);
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

    @ActionHandler(action = "createBlaiseJob", formClass = "com.globalsight.connector.blaise.form.CreateBlaiseJobForm")
    public void createBlaiseJob(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	CreateBlaiseJobForm blaiseForm = (CreateBlaiseJobForm) form;
        SessionManager sessionMgr = this.getSessionManager(request);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
		if (user == null) {
			String userName = request.getParameter("userName");
			if (userName != null && !"".equals(userName)) {
				user = ServerProxy.getUserManager().getUserByName(userName);
				sessionMgr.setAttribute(WebAppConstants.USER, user);
			}
		}

        long blcId = Long.parseLong(blaiseForm.getBlaiseConnectorId());
        BlaiseConnector blc = BlaiseManager.getBlaiseConnectorById(blcId);

        File attachFile = null;
        String jobCommentFilePathName = (String) sessionMgr
                .getAttribute("uploadAttachment");
		if (jobCommentFilePathName != null) {
			attachFile = new File(jobCommentFilePathName);
		}
        sessionMgr.removeElement("uploadAttachment");
        String attachFileName = request.getParameter("attachment");

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        List<Long> entryIds = new ArrayList<Long>();
        List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
        String fileMapFileProfile = blaiseForm.getFileMapFileProfile();
        String[] ffs = fileMapFileProfile.split(",");
        for (String ff : ffs)
        {
            String[] f = ff.split("-");
            entryIds.add(Long.parseLong(f[0]));
            fileProfileList.add(HibernateUtil.get(FileProfileImpl.class,
                    Long.parseLong(f[1])));
        }

        // Claim all entries one by one.
        BlaiseHelper helper = new BlaiseHelper(blc);
        for (int i = 0; i < entryIds.size(); i++)
        {
        	TranslationInboxEntryVo curEntry =
        			allInboxEntryMap.get(entryIds.get(i));
        	if (curEntry.getEntry().isGroup())
        	{
                helper.claim(entryIds.get(i));
        	}
        }

        long l10Id = fileProfileList.get(0).getL10nProfileId();
        BasicL10nProfile l10Profile = HibernateUtil.get(
                BasicL10nProfile.class, l10Id);

        String publicUuid = (String) sessionMgr.getAttribute("uuid");
        sessionMgr.removeElement("uuid");
        File srcFolder = null;
        if (publicUuid != null)
        {
			srcFolder = new File(AmbFileStoragePathUtils.getJobAttributeDir(),
					publicUuid);
        }
		// Every entry creates one job with one workflow
		for (int i = 0; i < entryIds.size(); i++)
        {
        	TranslationInboxEntryVo curEntry =
        			allInboxEntryMap.get(entryIds.get(i));
        	FileProfile curFileProfile = fileProfileList.get(i);
            String uuid = JobImpl.createUuid();
            if (srcFolder!= null && srcFolder.exists())
            {
                // Locate file attribute by uuid
				File trgFolder = new File(
						AmbFileStoragePathUtils.getJobAttributeDir(), uuid);
                FileUtil.copyFolder(srcFolder, trgFolder);
            }

			List<JobAttribute> jobAttribtues = getJobAttributes(
					blaiseForm.getAttributeString(), l10Profile);
			CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user,
					currentCompanyId, blc, blaiseForm, curEntry,
					curFileProfile, attachFile, attachFileName, uuid,
					jobAttribtues);
            Thread t = new MultiCompanySupportedThread(runnable);
            pool.execute(t);
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
        setLables(request, PageHandler.getBundle(request.getSession(false)));

        // All XLF 1.2 file profiles.
        request.setAttribute("fps", getXlfFileProfileOptions(request));

        setEntryInfo(request);

        // Ensure the blaise connector is always in session manager.
        BlaiseConnector blc = getBlaiseConnector(request);
		getSessionManager(request).setAttribute("blaiseConnector", blc);

		setJobIdsForEntries(blc.getId(), inboxEntries);

		setCreatingJobsNum(request);

        dataForTable(request);
	}

	private void setEntryInfo(HttpServletRequest request)
			throws LocaleManagerException, RemoteException, GeneralException
	{
		HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        HashMap<Long, String> id2FileNameMap = new HashMap<Long, String>();
        HashMap<Long, String> id2LocaleMap =  new HashMap<Long, String>();
        for (TranslationInboxEntryVo entry : allInboxEntries)
        {
			id2FileNameMap.put(entry.getId(),
					BlaiseHelper.getEntryFileName(entry));

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

	private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        Integer orgSize = (Integer) session.getAttribute("blaiseConnectorPageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
		if (StringUtil.isNotEmpty(numOfPerPage)) {
			try {
				size = Integer.parseInt(numOfPerPage);
			} catch (Exception e) {
				size = Integer.MAX_VALUE;
			}

			session.setAttribute("blaiseConnectorPageSize", size);
		}

		setTableNavigation(request, session, inboxEntries,
				new BlaiseInboxEntryComparator(uiLocale), size,
				"blaiseInboxEntryList", "blaiseInboxEntryKey");
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
    }

    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

    /**
	 * Get all latest Blaise inbox entries.
	 */
	private void refreshAllInboxEntries(HttpServletRequest request)
	{
		BlaiseConnector blc = getBlaiseConnector(request);
		BlaiseHelper helper = new BlaiseHelper(blc);
		allInboxEntries = helper.listInbox();
		inboxEntries = allInboxEntries;

		allInboxEntryMap.clear();
		for (TranslationInboxEntryVo entry : allInboxEntries)
		{
			allInboxEntryMap.put(entry.getId(), entry);
		}
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

    private SessionManager getSessionManager(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		return (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
	}
}
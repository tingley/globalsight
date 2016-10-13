package com.globalsight.connector.git;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.connector.git.form.CreateGitConnectorJobForm;
import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.CreateJobsMainHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class GitConnectorCreateJobHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorCreateJobHandler.class);

    private final static Map<String, String> l10NToTargetLocalesMap = new HashMap<String, String>();

    @ActionHandler(action = "connect", formClass = "")
    public void prepareConnect(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String gcId = p_request.getParameter("gcId");
        GitConnector gc = GitConnectorManagerLocal
                .getGitConnectorById(Long.parseLong(gcId));
        p_request.setAttribute("gitConnector", gc);
    }

    @ActionHandler(action = "initTree", formClass = "")
    public void initTree(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
        	String gcId = p_request.getParameter("gcId");
            GitConnector gc = GitConnectorManagerLocal
                    .getGitConnectorById(Long.parseLong(gcId));
            GitConnectorHelper helper = new GitConnectorHelper(gc);
            String checkout = p_request.getParameter("checkout");
            if(checkout != null && checkout.equals("yes"))
            {
            	helper.gitConnectorPull();
            }
            out.write(helper.getGitConnectorFilesJson().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error("Fail to init the tree.", e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "checkFileMapping", formClass = "")
    public void checkFileMapping(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	String gcIdStr = request.getParameter("gcId");
    	GitConnector gc = GitConnectorManagerLocal.getGitConnectorById(Long.parseLong(gcIdStr));
    	GitConnectorHelper helper = new GitConnectorHelper(gc);
    	response.setContentType("text/html;charset=UTF-8");
    	String filePath = request.getParameter("filePath");
    	filePath = filePath.substring(helper.getGitFolder().getPath().length() + 1);
    	String parentFolderPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
    	
    	String fileProfileIdStr = request.getParameter("fileProfileId").split(",")[1];
    	long l10Id = HibernateUtil.get(FileProfileImpl.class, Long.parseLong(fileProfileIdStr)).getL10nProfileId();
    	BasicL10nProfile l10Profile = HibernateUtil.get(BasicL10nProfile.class, l10Id);
    	String sourceLocaleName = l10Profile.getSourceLocale().getLocaleCode();
    	
    	Set<String> targetLocales = new HashSet();
    	String[] targetLocaleIds = request.getParameter("targetLocaleIds").split(" ");
    	for(String targetLocaleIdStr: targetLocaleIds)
    	{
    		targetLocales.add(HibernateUtil.get(GlobalSightLocale.class, Long.parseLong(targetLocaleIdStr)).toString());
    	}
    	
    	List<GitConnectorFileMapping> fms = (List<GitConnectorFileMapping>)
    			GitConnectorManagerLocal.getAllFileMapping(Long.parseLong(gcIdStr), sourceLocaleName, targetLocales);
    	for(GitConnectorFileMapping fm: fms)
    	{
    		if(targetLocales.size() == 0)
    		{
    			break;
    		}
    		
			String sourceMappingPath = fm.getSourceMappingPath();
			if(filePath.equals(sourceMappingPath) ||
					parentFolderPath.equals(sourceMappingPath))
			{
				targetLocales.remove(fm.getTargetLocale());
			}
    	}
    	
    	String msg = "";
    	if(targetLocales.size() != 0)
    	{
    		for(String targetLocale: targetLocales)
    		{
    			msg += targetLocale + ",";
    		}
    		msg = msg.substring(0, msg.length() - 1);
    	}
    	
        PrintWriter writer = response.getWriter();
        writer.write(msg);
        writer.close();
    	pageReturn();
    }

    @ActionHandler(action = "updateTargetLocales", formClass = "")
    public void updateTargetLocales(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
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
        queryTargetLocales(request, response, user);

        pageReturn();
    }

    /**
     * Upload the job attachment file to server, and store it in session for
     * later use.
     */
    @ActionHandler(action = "uploadAttachment", formClass = "")
    public void uploadAttachment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FileUploader uploader = new FileUploader();
        File file = uploader.upload(request);

        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute("uploadAttachment", file.getAbsolutePath());

        PrintWriter writer = response.getWriter();
        writer.write("<script type='text/javascript'>window.parent.addAttachment(' ')</script>;");

        pageReturn();
    }

    @ActionHandler(action = "createGitConnectorJob", formClass = "com.globalsight.connector.git.form.CreateGitConnectorJobForm")
    public void createGitConnectorJob(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	CreateGitConnectorJobForm gcForm = (CreateGitConnectorJobForm) form;
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
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

        long gcId = Long.parseLong(gcForm.getGitConnectorId());
        GitConnector gc = GitConnectorManagerLocal.getGitConnectorById(gcId);

        File attachment = null;
        String jobCommentFilePathName = (String) sessionMgr
                .getAttribute("uploadAttachment");
        if (jobCommentFilePathName != null)
        {
            attachment = new File(jobCommentFilePathName);
        }
        sessionMgr.removeElement("uploadAttachment");

        String[] targetLocales = request.getParameterValues("targetLocale");
        String attachmentName = request.getParameter("attachment");
        String uuid = sessionMgr.getAttribute("uuid") == null ? JobImpl
                .createUuid() : (String) sessionMgr.getAttribute("uuid");
        sessionMgr.removeElement("uuid");
        
        CreateGitConnectorJobThread runnable = new CreateGitConnectorJobThread(user,
                currentCompanyId, gc, gcForm, targetLocales, attachment,
                attachmentName, uuid, request.getParameterValues("jobFilePath"));
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();

        pageReturn();
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        setLables(request, PageHandler.getBundle(request.getSession()));

        // Creating jobs NUM.
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Integer creatingJobsNum = CreateJobsMainHandler.getCreatingJobsNum(Long
                .parseLong(currentCompanyId));
        request.setAttribute("creatingJobsNum", creatingJobsNum);

    }
    
    /**
     * Called by ajax, search target locales for files, and init the target
     * locales checkbox on the jsp
     * 
     * @param request
     * @param response
     * @param user
     * @throws IOException
     */
    private void queryTargetLocales(HttpServletRequest request,
            HttpServletResponse response, User user) throws IOException
    {
        String l10Nid = request.getParameter("l10Nid");
        if (StringUtils.isNotEmpty(l10Nid))
        {
            String targetLocalesString = l10NToTargetLocalesMap.get(l10Nid);
            if (targetLocalesString == null)
            {
                String hsql = "select wti.targetLocale from "
                        + "L10nProfileWFTemplateInfo as ltp, WorkflowTemplateInfo wti "
                        + "where wti.id = ltp.key.wfTemplateId and ltp.key.l10nProfileId = "
                        + l10Nid
                        + " and ltp.isActive = 'Y' and wti.isActive = 'Y' "
                        + "order by wti.targetLocale.language";
                List<?> localeList = HibernateUtil.search(hsql);

                if (localeList != null)
                {
                    targetLocalesString = this.initTargetLocaleSelect(
                            localeList, user);
                    l10NToTargetLocalesMap.put(l10Nid, targetLocalesString);
                }
                else
                {
                    targetLocalesString = "";
                }
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(targetLocalesString);
            writer.close();
        }
    }

    /**
     * Init the checkbox of target locales. The pattern is fr_FR(French_France).
     * 
     * @param localeList
     * @param user
     * @param checked
     */
    private String initTargetLocaleSelect(List<?> localeList, User user)
    {
        StringBuffer sb = new StringBuffer();
        Locale locale = this.getUserLocale(user);
        for (int i = 0; i < localeList.size(); i++)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) localeList.get(i);
            sb.append("<div class='locale'>");
            sb.append("<input type='checkbox' name='targetLocale' value='"
                    + gsl.getId() + "' checked='true'>&nbsp;");
            sb.append(gsl.getLanguage() + "_" + gsl.getCountry() + " ("
                    + gsl.getDisplayLanguage(locale) + "_"
                    + gsl.getDisplayCountry(locale) + ")");
            sb.append("</div>");
        }
        return sb.toString();
    }

    /**
     * Get default UI locale information for specified user
     * 
     * @param user
     *            User information
     * @return Locale Default UI locale for the specified user
     */
    private Locale getUserLocale(User user)
    {
        String dl = null;
        if (user != null)
        {
            dl = user.getDefaultUILocale();
        }
        if (dl == null)
            return new Locale("en", "US");
        else
        {
            try
            {
                String language = dl.substring(0, dl.indexOf("_"));
                String country = dl.substring(dl.indexOf("_") + 1);
                country = (country == null) ? "" : country;

                return new Locale(language, country);
            }
            catch (Exception e)
            {
                return new Locale("en", "US");
            }
        }
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
        setLableToJsp(request, bundle,
                "jsmsg_choose_file_profiles_for_all_files");
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
        setLableToJsp(request, bundle, "lb_git_connector_create_job_add_file_tip");
        setLableToJsp(request, bundle, "msg_set_job_attributes");
    }

    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }
}

package com.globalsight.everest.util.applet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.AddingSourcePage;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.JsonUtil;

public class AppletService extends HttpServlet
{
    private static final Logger CATEGORY = Logger
            .getLogger(AppletService.class);
    private static final long serialVersionUID = 1L;
    private HttpServletRequest request;
    HttpServletResponse response;
    // private PrintWriter writer;
    // private ServletOutputStream out;
    private long companyId;

    public void service(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        this.response = response;
        response.setCharacterEncoding("utf-8");
        setCompanyId();

        String method = request.getParameter("action");
        try
        {
            // writer = response.getWriter();
            AppletService.class.getMethod(method).invoke(AppletService.this);
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not invoke the method:" + method);
            CATEGORY.error(e);
        }
    }
    
    public void canAddSourceFile()
    {
        PrintWriter writer = null;
        try
        {
            String jobId = request.getParameter("jobId");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(jobId));
            String errorMsgKey = job.canAddSourceFiles();
            if (errorMsgKey != null)
            {
                writer = response.getWriter();
                writer.write(errorMsgKey);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    public void addSouceFile()
    {
        try
        {
            String xml = request.getParameter("xml");
            AddFileVo vo = XmlUtil.string2Object(AddFileVo.class, xml);
            
            List<String> filePaths = vo.getFilePaths();
            List<String> realPaths = new ArrayList<String>();
            List<Long> fpIds = vo.getFileProfileIds();
            String locale = vo.getLocale();
            
            JobImpl job = HibernateUtil.get(JobImpl.class, vo.getJobId());
            String errorMsgKey = job.canAddSourceFiles();
            if (errorMsgKey != null)
            {
                ResourceBundle b = PageHandler.getBundle(request.getSession());
                CATEGORY.error(b.getString(errorMsgKey));
                return;
            }
            
            if (locale == null)
            {
                locale = job.getSourceLocale().toString();
                vo.setLocale(locale);
            }
            
            File root = AmbFileStoragePathUtils.getCxeDocDir();
            for (String path : filePaths)
            {
                File targetFile = new File(root, path);
                
                if (!targetFile.exists())
                {
                    StringBuffer newPath = new StringBuffer(locale).append(File.separator);
                    
                    if (job.isFromWebService())
                    {
                        newPath.append("webservice").append(File.separator);
                    }
                    
                    String realJobPath = getRealJobNamePath(job);
                    newPath.append(realJobPath).append(
                            File.separator).append(path).toString();
                    path = newPath.toString();
                    
                    targetFile = new File(root, path);
                }

                realPaths.add(path);
            }
            vo.setFilePaths(realPaths);
            
            for (int i = 0; i < realPaths.size(); i++)
            {
                FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class, fpIds.get(i), false);
                
                AddingSourcePage page = new AddingSourcePage();
                page.setJobId(vo.getJobId());
                page.setExternalPageId(realPaths.get(i));
                page.setL10nProfileId(fpIds.get(i));
                page.setDataSource(fp.getName());
                
                HibernateUtil.save(page);
            }
            
            JmsHelper.sendMessageToQueue(vo,
                    JmsHelper.JMS_ADD_SOURCE_FILE_QUEUE);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }

    // For now, this is only being used to "Add Files" on job details UI.
    public void uploadFile()
    {
        PrintWriter writer = null;
        try
        {
            FileUploader uploader = new FileUploader();
            // Ignore the returned file object here as it can't upload real file
            // content to server with JRE7, APPLET and HTTPS combination.
            uploader.upload(request);

            String strJobId = uploader.getFieldValue("jobId");
            if (strJobId == null || "".equals(strJobId.trim()))
            {
                strJobId = request.getParameter("jobId");
            }
            long jobId = Long.parseLong(strJobId);
            String strCompanyId = uploader.getFieldValue("currentCompanyId");
            if (strCompanyId == null || "".equals(strCompanyId.trim()))
            {
                strCompanyId = request.getParameter("currentCompanyId");
            }
            companyId = Long.parseLong(strCompanyId);
            if (companyId > 0)
            {
                CompanyThreadLocal.getInstance().setIdValue("" + companyId);
            }

            String addFileTmpSavingPathName = uploader
                    .getFieldValue("addFileTmpSavingPathName");
            File file = new File(AmbFileStoragePathUtils.getCxeDocDir(String
                    .valueOf(companyId)), addFileTmpSavingPathName);

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);

            GlobalSightLocale locale = job.getSourceLocale();
            String realJobNamePath = getRealJobNamePath(job);
            StringBuffer newPath = new StringBuffer(locale.toString());
            newPath.append(File.separator);
            if (job.isFromWebService())
            {
                newPath.append("webservice").append(File.separator);
            }

            newPath.append(realJobNamePath).append(File.separator).append(
                    uploader.getFieldValue("path"));
            File targetFile = new File(
                    AmbFileStoragePathUtils.getCxeDocDir(String
                            .valueOf(companyId)), newPath.toString());
            
            if (!file.renameTo(targetFile))
            {
                FileUtils.copyFile(file, targetFile);
            }

            writer = response.getWriter();
            writer.write(file.getAbsolutePath());

            file.delete();
            file.getParentFile().delete();
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    private void setCompanyId()
    {
        String companyName = UserUtil.getCurrentCompanyName(request);
        if (companyName == null)
        {
            return;
        }

        try
        {
            companyId = ServerProxy.getJobHandler().getCompany(companyName)
                    .getIdAsLong();
            CompanyThreadLocal.getInstance().setIdValue("" + companyId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not get the Company!");
        }
    }

    private String subPath(String path, String jobName)
    {
        path = path.replace("\\", "/");
        String s = "/" + jobName + "/";
        int index = path.indexOf(s);
        if (index > 0)
        {
            path = path.substring(index + s.length());
        }

        return path;
    }

    public void getResource() throws IOException
    {
        String name = request.getParameter("name");
        String pageLocale = request.getParameter("pageLocale");
        
        Map<String, String> resource = new HashMap<String, String>();
        List<String> keys = AppletConstant.ALL_RESOURCE.get(name);
        
        ResourceBundle b;
        if (pageLocale != null)
        {
            b = PageHandler.getBundleByLocale(pageLocale);
        }
        else
        {
            b = PageHandler.getBundle(request.getSession());
        }
        
        for (String key : keys)
        {
            try
            {
                resource.put(key, b.getString(key));
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }

        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(response.getOutputStream());
            out.writeObject(resource);
        }
        catch (IOException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            if (out != null)
            {
                out.flush();
                out.close();
            }
        }

    }

    public void getFiles()
    {
        long jobId = Long.parseLong(request.getParameter("jobId"));
        JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
        String realJobNamePath = getRealJobNamePath(job);
        List<SourcePage> pages = (List) job.getSourcePages();
        ExistFiles existFiles = new ExistFiles();
        for (SourcePage page : pages)
        {
            existFiles.addFiles(subPath(page.getDisplayPageName(), realJobNamePath));
        }

        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(XmlUtil.object2String(existFiles));
            writer.close();
        }
        catch (IOException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    public void getFileProfiles()
    {
        Iterator<FileProfile> fileProfileIter = null;
        try
        {
            Collection<FileProfile> results = ServerProxy
                    .getFileProfilePersistenceManager().getAllFileProfiles();
            fileProfileIter = results.iterator();
        }
        catch (Exception e)
        {
            String message = "Unable to get file profiles from db.";
            CATEGORY.error(message, e);
            return;
        }

        ProfileInfo info = new ProfileInfo();
        List<FileProfileVo> fileProfiles = new ArrayList<FileProfileVo>();
        FileProfile fileProfile = null;
        while (fileProfileIter.hasNext())
        {
            fileProfile = fileProfileIter.next();
            FileProfileVo vo = new FileProfileVo();
            vo.init(fileProfile);
            fileProfiles.add(vo);
        }

        info.setFileProfiles(fileProfiles);

        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(XmlUtil.object2String(info));
        }
        catch (IOException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }

    }
    
    private String getRealJobNamePath(JobImpl p_job) {
        if (p_job == null || p_job.getSourcePages() == null)
            return "";
        
        String realJobPath = "";
        ArrayList sps = new ArrayList(p_job.getSourcePages());
        if (sps != null && sps.size() > 0) {
            SourcePage sp = (SourcePage)sps.get(0);
            String tmp = sp.getExternalPageId();
            tmp = tmp.substring(tmp.indexOf("\\") + 1);
            if (p_job.isFromWebService()) {
                tmp = tmp.substring(tmp.indexOf("\\") + 1);
            }
            realJobPath = tmp.substring(0, tmp.indexOf("\\"));
        }
        return realJobPath;
    }
    
    public void getRoles()
    {
        HttpSession session = request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
        
        GlobalSightLocale targetLocale = wfti.getTargetLocale();
        GlobalSightLocale sourceLocale = wfti.getSourceLocale();
        
        String activity = request.getParameter("activity");
        
        ContainerRole containerRole = WorkflowTemplateHandlerHelper
                .getContainerRole(activity, sourceLocale.toString(),
                        targetLocale.toString(), wfti.getProject().getId());

        if (containerRole != null)
        {
            writeString(containerRole.getName());
        } 
        else
        {
            writeString("");
        }
    }
    
    public void getParticipantUser()
    {
        String activity = request.getParameter("activity");
        
        HttpSession session = request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
        
        GlobalSightLocale targetLocale = wfti.getTargetLocale();
        GlobalSightLocale sourceLocale = wfti.getSourceLocale();
        
     // obtain the roles to be turned into grid data.
        Collection usersCollection = WorkflowTemplateHandlerHelper
                .getUserRoles(activity, sourceLocale.toString(),
                        targetLocale.toString());

        ArrayList<Object[]> userRoles = new ArrayList<Object[]>();
        if (usersCollection != null)
        {
            Set projectUserIds = wfti.getProject().getUserIds();
            Vector<UserRoleImpl> usersInProject = new Vector<UserRoleImpl>();

            // filter out the users that aren't in the project
            for (Iterator i = usersCollection.iterator(); i.hasNext();)
            {
                UserRoleImpl userRole = (UserRoleImpl) i.next();
                if (projectUserIds.contains(userRole.getUser()))
                {
                    usersInProject.add(userRole);
                }
            }
            
            

            for (int i = 0; i < usersInProject.size(); i++)
            {
                UserRoleImpl userRole = (UserRoleImpl) usersInProject
                        .get(i);
                User user = WorkflowTemplateHandlerHelper.getUser(userRole
                        .getUser());
                if (user != null)
                {
                    String[] role = new String[6];
                    role[0] = user.getFirstName();
                    role[1] = user.getLastName();
                    role[2] = user.getUserName();
                    // 3 - place holder for calendaring
                    // since the wf instance needs this and uses
                    // same WorkflowTaskDialog code
                    role[3] = null;
                    role[4] = userRole.getName();
                    role[5] = userRole.getRate();
                    userRoles.add(role);                        
                }
            }
        }
        
        writeString(JsonUtil.toJson(userRoles));
    }
    
    public void getWorkflowDetailData()
    {
        Map m = new HashMap();
        JsonUtil.toJson(m);
        
        HttpSession session = request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
        
        GlobalSightLocale targetLocale = wfti.getTargetLocale();
        GlobalSightLocale sourceLocale = wfti.getSourceLocale();
        
        Hashtable table = WorkflowTemplateHandlerHelper.getWorkflowDetailData(bundle, uiLocale,
                sourceLocale, targetLocale);
        
        table.put("companyId", wfti.getCompanyId());
        table.put("workflowName", wfti.getName());
        table.put("workflowDesc", wfti.getDescription());
        table.put("workflowPM", wfti.getProjectManagerId());
        table.put("workflowManager", wfti.getWorkflowManagerIds());
        
        writeString(JsonUtil.toJson(table));
    }
    
    private void writeString(String js)
    {
        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(js);
        }
        catch (IOException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }
    
    /**
     * For edit workflow.
     * @throws IOException 
     */
    public void getWorkflowData() throws IOException 
    {
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
        
        if (wfti.getId() < 0)
        	return;
        
        // the name can be edit
        wfti = HibernateUtil.get(WorkflowTemplateInfo.class, wfti.getId());
        
        String templateName = wfti.getName();
        String templateFileName = AmbFileStoragePathUtils
                .getWorkflowTemplateXmlDir().getAbsolutePath()
                + File.separator
                + templateName + WorkflowConstants.SUFFIX_XML;
        File file = new File(templateFileName);
        
        if (file.exists())
        {
            String content = FileUtil.readFile(file, "utf-8");
            JSONObject json = (JSONObject) new XMLSerializer().read(content);
            String js = json.toString();
            writeString(js);
        }        
    }
    
    public void saveWorkflow()
    {
        String xml = request.getParameter("xml");
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // always check for the object (whether it's new or existing)
        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
        
        WorkflowTemplate temp = new WorkflowTemplate();
        temp.setName(wfti.getName());
        temp.setDescription(wfti.getDescription());
        
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessDefinition pd = ProcessDefinition.parseXmlString(xml);
            ctx.deployProcessDefinition(pd);
            temp.setId(pd.getId());
            wfti.setWorkflowTemplate(temp);
            if (wfti.getId() > 0)
            {
                ServerProxy.getProjectHandler().modifyWorkflowTemplate(wfti);
            }
            else
            {
                ServerProxy.getProjectHandler().createWorkflowTemplateInfo(
                        wfti);
            }
            String path = AmbFileStoragePathUtils
                    .getWorkflowTemplateXmlDir().getAbsolutePath()
                    + File.separator
                    + wfti.getName()
                    + WorkflowConstants.SUFFIX_XML;
            FileUtil.writeFile(new File(path), xml, "utf-8");
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            ctx.close();
        }
    }
}

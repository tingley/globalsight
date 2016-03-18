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

package com.globalsight.everest.util.applet;

import java.awt.Point;
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
import com.globalsight.everest.util.applet.workflow.WorkflowXmlParser;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.JsonUtil;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

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
        
        GlobalSightLocale sourceLocale;
        GlobalSightLocale targetLocale;
        long projectId;
        
        if (wfti == null)
        {
            Workflow wf = (Workflow) sessionMgr.getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
            WorkflowInstance wfi = wf.getIflowInstance();
            
            targetLocale = wf.getTargetLocale();
            sourceLocale = wf.getJob().getSourceLocale();
            JobImpl job = HibernateUtil.get(JobImpl.class, wf.getJob().getId());
            projectId = job.getProjectId();
        }
        else
        {
            targetLocale = wfti.getTargetLocale();
            sourceLocale = wfti.getSourceLocale();
            projectId = wfti.getProject().getId();
        }
        
        String activity = request.getParameter("activity");
        
        ContainerRole containerRole = WorkflowTemplateHandlerHelper
                .getContainerRole(activity, sourceLocale.toString(),
                        targetLocale.toString(), projectId);

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
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);

        if (wfti != null)
        {
            getParticipantUserForTemplate();
        }
        else
        {
            getParticipantUserForInstance();
        }
    }
    
    public void getParticipantUserForTemplate()
    {
        String activity = request.getParameter("activity");
        
        HttpSession session = request.getSession();
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
    
    public void getParticipantUserForInstance()
    {
        String activity = request.getParameter("activity");
        
        HttpSession session = request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Workflow wf = (Workflow) sessionMgr.getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
        WorkflowInstance wfi = wf.getIflowInstance();
        
        GlobalSightLocale targetLocale = wf.getTargetLocale();
        GlobalSightLocale sourceLocale = wf.getJob().getSourceLocale();
        
     // obtain the roles to be turned into grid data.
        Collection usersCollection = WorkflowTemplateHandlerHelper
                .getUserRoles(activity, sourceLocale.toString(),
                        targetLocale.toString());

        ArrayList<Object[]> userRoles = new ArrayList<Object[]>();
        if (usersCollection != null)
        {
            JobImpl job = HibernateUtil.get(JobImpl.class, wf.getJob().getId());
            Set projectUserIds = job.getProject().getUserIds();
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
    
    /**
     * Used for GBS-4022. Remove applet from job detail/workflows/details page.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getWorkflowInstance()
    {
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        WorkflowInstance wfi = (WorkflowInstance) sessionMgr.getAttribute("WorkflowInstance");
        
        Vector<WorkflowTaskInstance> wts = wfi.getWorkflowInstanceTasks();
        
        Map result = new HashMap();
        Vector lines = new Vector();
        Vector nodes = new Vector();
        result.put("lines", lines);
        result.put("nodes", nodes);
        
        for (WorkflowTaskInstance wt : wts)
        {
            Map map = new HashMap();
            map.put("id", wt.getTaskId());
            
            int state = wt.getTaskState();
            map.put("state", state);
            Point p = wt.getPosition();
            map.put("x", p.getX());
            map.put("y", p.getY());
            
            int type = wt.getType();
            map.put("type", type);
            
            if (type == WorkflowConstants.ACTIVITY)
            {
                map.put("roleName", wt.getDisplayRoleName());
                map.put("activityName", wt.getActivityDisplayName());
            }
            
            Vector arrows = wt.getOutgoingArrows();
            
            for (int j = 0; j < arrows.size(); j++)
            {
                WorkflowArrow modelArrow = (WorkflowArrow) arrows.elementAt(j);
                WorkflowTask twt = modelArrow.getTargetNode();
                
                Map tmap = new HashMap();
                tmap.put("fid", wt.getTaskId());
                tmap.put("tid", twt.getTaskId());
                tmap.put("name", modelArrow.getName());
                tmap.put("isDefault", modelArrow.isDefault());
                lines.add(tmap);
            }
            
            nodes.add(map);
        }
        
        String js = JsonUtil.toJson(result);
        writeString(js);
    }
    
    /**
     * Used for GBS-4022. Remove applet from job detail/workflows/details page.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getWorkflowInstanceForEdit()
    {
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        Workflow wf = (Workflow) sessionMgr.getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
        WorkflowInstance wfi = wf.getIflowInstance();
        Vector<WorkflowTaskInstance> wts = wfi.getWorkflowInstanceTasks();
        
        Map result = new HashMap();
        Vector lines = new Vector();
        Vector nodes = new Vector();
        result.put("lines", lines);
        result.put("nodes", nodes);
        
        for (WorkflowTaskInstance wt : wts)
        {
            Map map = getTaskJson(wt, lines);
            nodes.add(map);
        }
        
        String js = JsonUtil.toJson(result);
        writeString(js);
    }
    
    private Map getTaskJson(WorkflowTaskInstance wt, Vector lines)
    {
        Map map = new HashMap();
        
        map.put("id", wt.getTaskId());
        
        int state = wt.getTaskState();
        map.put("state", state);
        Point p = wt.getPosition();
        map.put("x", p.getX());
        map.put("y", p.getY());
        
        int type = wt.getType();
        map.put("type", type);
        
        if (type == WorkflowConstants.ACTIVITY)
        {
            map.put("roleName", wt.getDisplayRoleName());
            map.put("activityName", wt.getActivityDisplayName());
            
            //properties
            Map json = new HashMap();
            map.put("json", json);     
            Map task = new HashMap();
            json.put("task", task);           
            Map assignment = new HashMap();
            task.put("assignment", assignment);
            
            assignment.put("activity", wt.getActivityName());
            assignment.put("report_upload_check", wt.getReportUploadCheck());
            assignment.put("roles", wt.getRolesAsString());
            assignment.put("accepted_time", wt.getAcceptTime());
            assignment.put("completed_time", wt.getCompletedTime());
            assignment.put("overdueToPM_time", wt.getOverdueToPM());
            assignment.put("overdueToUser_time", wt.getOverdueToUser());
            assignment.put("role_type", Boolean.toString(wt.getRoleType()));
            assignment.put("sequence", wt.getSequence());
            
            assignment.put("structural_state", wt.getStructuralState());
            assignment.put("rate_selection_criteria", wt.getRateSelectionCriteria());
            assignment.put("expense_rate_id", wt.getExpenseRateId());
            assignment.put("revenue_rate_id", wt.getRevenueRateId());
            assignment.put("role_name", wt.getDisplayRoleName());
            assignment.put("action_type", wt.getActionType());
            assignment.put("role_preference", wt.getRolePreference());
        }
        
        Vector arrows = wt.getOutgoingArrows();
        
        for (int j = 0; j < arrows.size(); j++)
        {
            WorkflowArrow modelArrow = (WorkflowArrow) arrows.elementAt(j);
            WorkflowTask twt = modelArrow.getTargetNode();
            
            Map tmap = new HashMap();
            tmap.put("fid", wt.getTaskId());
            tmap.put("tid", twt.getTaskId());
            tmap.put("name", modelArrow.getName());
            tmap.put("isDefault", modelArrow.isDefault());
            lines.add(tmap);
        }
        
        return map;
    }
    
    public void getWorkflowDetailDataForEdit()
    {
        HttpSession session = request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        Workflow wf = (Workflow) sessionMgr.getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
        WorkflowInstance wfi = wf.getIflowInstance();
        
        GlobalSightLocale targetLocale = wf.getTargetLocale();
        GlobalSightLocale sourceLocale = wf.getJob().getSourceLocale();
        
        Hashtable table = WorkflowTemplateHandlerHelper.getWorkflowDetailData(bundle, uiLocale,
                sourceLocale, targetLocale);
        sessionMgr.setAttribute("workflowDetailData", table);
        table.put("companyId", wf.getCompanyId());
//        table.put("workflowName", wfi.getName());
        sessionMgr.setAttribute("workflowDetailData", table);
        writeString(JsonUtil.toJson(table));
    }
    
    public void saveWorkflowInstance()
    {
        HttpSession session = request.getSession();
        
        String xml = request.getParameter("xml");
        String ids = request.getParameter("ids");
        
        WorkflowXmlParser parser = new WorkflowXmlParser(xml, ids);
        parser.parse(request.getSession());
        writeString("ok");
    }
 }

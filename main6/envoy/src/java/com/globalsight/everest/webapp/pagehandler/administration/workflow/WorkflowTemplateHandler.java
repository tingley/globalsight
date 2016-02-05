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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WfTemplateSearchParameters;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.log.OperationLog;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.sun.jndi.toolkit.url.UrlUtil;

/**
 * WorkflowTemplateHandler is the page handler responsible for displaying a list
 * of workflow templates and perform actions supported by the UI (JSP).
 */

public class WorkflowTemplateHandler extends PageHandler implements
        WorkflowTemplateConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(WorkflowTemplateHandler.class.getName());

    // non user related state
    private int m_numOfWfsPerPage; // number of workflow templates per page
    String m_userId;
    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // //////////////////////////////////////////////////////////////////
    public WorkflowTemplateHandler()
    {
        try
        {
            m_numOfWfsPerPage = SystemConfiguration.getInstance()
                    .getIntParameter(SystemConfigParamNames.NUM_WFT_PER_PAGE);
        }
        catch (Exception e)
        {
            m_numOfWfsPerPage = 20;
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        WfTemplateSearchParameters params;
//        preDataForDropBox(p_request, session);
        
        params = getSearchCriteria(p_request);
        if (isPost(p_request))
        {
            String action = p_request.getParameter(ACTION);
            if (CANCEL_ACTION.equals(action))
            {
                sessionMgr.setAttribute(WF_TEMPLATE_INFO, null);//distinguish cancel and previous
                sessionMgr.setAttribute(TEMPLATE_ID, null);
            }
            if (SAVE_ACTION.equals(action))
            {
                saveDuplicates(p_request, session);
            }
            else if (IMPORT_ACTION.equals(action))
            {
                importWorkFlow(p_request, session);
            }
            else if (EXPORT_ACTION.equals(action))
            {
                exportWorkFlow(p_request, p_response, session);
                return;
            }
        }

        selectTemplatesForDisplay(p_request, session, params);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

//    private void preDataForDropBox(HttpServletRequest p_request,
//            HttpSession session)
//    {
//        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
//        Locale uiLocale =  (Locale)session.getAttribute(WebAppConstants.UILOCALE);
//
//        // Get data needed for search page
//        p_request.setAttribute(WorkflowTemplateConstants.SOURCE_LOCALES,
//                 WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale));
//        p_request.setAttribute(WorkflowTemplateConstants.TARGET_LOCALES,
//                 WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale));
//        // If not admin, get only the projects for that user (PM).
//        List projectInfos;
//
//        if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
//        {
//            projectInfos = 
//                WorkflowTemplateHandlerHelper.getAllProjectInfos(uiLocale);
//        }
//        else
//        {
//            String userName = (String)session.getAttribute(
//                                            WebAppConstants.USER_NAME);
//            User user = UserHandlerHelper.getUser(userName);
//            projectInfos = 
//                WorkflowTemplateHandlerHelper.getAllProjectInfosForUser(user,
//                                                                    uiLocale);
//        }
//        p_request.setAttribute(WorkflowTemplateConstants.PROJECTS, projectInfos);
//    }

    private void importWorkFlow(HttpServletRequest p_request,
            HttpSession session)
    {
        FileUploadHelper o_upload = new FileUploadHelper();
        try
        {
            o_upload.doUpload(p_request);
            String list = o_upload.getFieldValue("localePairs");
            String name = o_upload.getFieldValue("nameTF");
            String projectId = o_upload.getFieldValue("project");
            String fileName = o_upload.getSavedFilepath();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new File(fileName));
            ArrayList<LocalePair> alist = new ArrayList<LocalePair>();
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens())
            {
                String id = (String) st.nextToken();
                alist.add(ServerProxy.getLocaleManager().getLocalePairById(
                        Long.parseLong(id)));
            }

            WorkflowTemplateHandlerHelper.importWorkflowTemplateInfo(doc,
                    alist, name, projectId, getBundle(session));
            for (LocalePair localePair : alist)
            {
                String importWorkflowTemplateName = generateName(name,localePair);
                OperationLog
                        .log(m_userId, OperationLog.EVENT_ADD,
                                OperationLog.COMPONET_WORKFLOW,
                                importWorkflowTemplateName);
            }
            
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    private void exportWorkFlow(HttpServletRequest p_request,
            HttpServletResponse p_response, HttpSession session)
    {
        String wfTemplateId = p_request.getParameter(WF_TEMPLATE_INFO_ID);
        WorkflowTemplateInfo template = WorkflowTemplateHandlerHelper
                .getWorkflowTemplateInfoById(Long.parseLong(wfTemplateId));
        String templateName = template.getName();
        String templateFileName = AmbFileStoragePathUtils
                .getWorkflowTemplateXmlDir().getAbsolutePath()
                + File.separator
                + templateName + WorkflowConstants.SUFFIX_XML;
        File file = new File(templateFileName);
        sendFileToClient(p_request, p_response, templateName
                + WorkflowConstants.SUFFIX_XML, file);
    }
    
    // The old method is using CommentFilesDownLoad.sendFileToClient(). 
    // This will cause the XML file to be deleted¡£ So rewrite it again.
    public void sendFileToClient(HttpServletRequest request,
            HttpServletResponse response, String zipFileName, File workflowXml)
    {
        if (request.isSecure())
        {
            PageHandler.setHeaderForHTTPSDownload(response);
        }
        FileInputStream fis = null;
        try
        {
            response.setContentType("application/zip");
            String attachment = "attachment; filename=\""
                    + UrlUtil.encode(zipFileName, "utf-8") + "\";";
            response.setHeader("Content-Disposition", attachment);
            response.setContentLength((int) workflowXml.length());
            byte[] inBuff = new byte[4096];
            fis = new FileInputStream(workflowXml);
            int bytesRead = 0;
            while ((bytesRead = fis.read(inBuff)) != -1)
            {
                response.getOutputStream().write(inBuff, 0, bytesRead);
            }

            if (bytesRead > 0)
            {
                response.getOutputStream().write(inBuff, 0, bytesRead);
            }

            fis.close();
        }
        catch (IOException e)
        {
            CATEGORY.error(e);
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    CATEGORY.error(e);
                }
            }
        }

    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {

        return new WorkflowTemplateControlFlowHelper(p_request, p_response);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Search for workflows with certain criteria.
     */
    private WfTemplateSearchParameters getSearchCriteria(
            HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String[] filterParam = dealWithFilterParam(p_request);
        String name = filterParam[0];
        String srcLocale = filterParam[1];
        String targLocale = filterParam[2];
        String project = filterParam[3];
        String companyName = filterParam[4];
        WfTemplateSearchParameters params = new WfTemplateSearchParameters();
        params.setWorkflowName(name);
        params.setSourceLocale(srcLocale);
        params.setTargetLocale(targLocale);
        params.setProject(project);
        params.setCompanyName(companyName);

        return params;
    }
    
    private String[] dealWithFilterParam(HttpServletRequest p_request){
        String action = p_request.getParameter("action");
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String name = p_request.getParameter("nameField");
        String srcLocale = p_request.getParameter("srcLocale");
        String targLocale = p_request.getParameter("targLocale");
        String project = p_request.getParameter("project");
        String companyName = p_request.getParameter("workflowPageCompanyName");

        if (!FILTER_SEARCH.equals(action) || p_request.getMethod().equalsIgnoreCase(WebAppConstants.REQUEST_METHOD_GET)) {
            name = (String) sessionMgr.getAttribute("nameField");
            srcLocale = (String) sessionMgr.getAttribute("srcLocale");
            targLocale = (String) sessionMgr.getAttribute("targLocale");
            project = (String) sessionMgr.getAttribute("project");
            companyName = (String) sessionMgr.getAttribute("companyName");
        }
        if (FILTER_SEARCH.equals(action)) {
            //Go to page #1 if current action is filter searching.
            sessionMgr.setAttribute(KEY + TableConstants.LAST_PAGE_NUM, Integer.valueOf(1));
        }
        name = name == null ? "" : name;
        srcLocale = srcLocale == null ? "" : srcLocale;
        targLocale = targLocale == null ? "" : targLocale;
        project = project == null ? "" : project;
        companyName = companyName == null ? "" : companyName;
        sessionMgr.setAttribute("nameField", name);
        sessionMgr.setAttribute("srcLocale", srcLocale);
        sessionMgr.setAttribute("targLocale", targLocale);
        sessionMgr.setAttribute("project", project);
        sessionMgr.setAttribute("companyName", companyName);
        
        String[] filterParam = {name,srcLocale,targLocale,project,companyName};
        return filterParam;
    }

    private void selectTemplatesForDisplay(HttpServletRequest p_request,
            HttpSession p_session, WfTemplateSearchParameters p_params)
            throws ServletException, IOException, EnvoyServletException
    {
        List<WorkflowTemplateInfo> templates = null;
        try
        {
            templates = (List<WorkflowTemplateInfo>) ServerProxy.getProjectHandler()
                    .findWorkflowTemplates(p_params);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        templates = getTemplatesByLocaleFilter(templates,p_params,uiLocale);
        setNumberPerPage(p_request);
        WorkflowTemplateInfoComparator comp = new WorkflowTemplateInfoComparator(
                uiLocale);
        setTableNavigation(p_request, p_session, templates, comp,
                m_numOfWfsPerPage, TEMPLATES, KEY);
    }
    
    private List<WorkflowTemplateInfo> getTemplatesByLocaleFilter(List<WorkflowTemplateInfo> templates,WfTemplateSearchParameters p_params,Locale uiLocale){
        List<WorkflowTemplateInfo> filteredTemplates = new ArrayList<WorkflowTemplateInfo>();
        Map criteria = p_params.getParameters();
        for(WorkflowTemplateInfo template : templates){
            boolean src = StringUtils.containsIgnoreCase(template.getSourceLocale().getDisplayName(uiLocale), (String)criteria.get(WfTemplateSearchParameters.SOURCE_LOCALE));
            boolean targ = StringUtils.containsIgnoreCase(template.getTargetLocale().getDisplayName(uiLocale), (String)criteria.get(WfTemplateSearchParameters.TARGET_LOCALE));
            if(src && targ){
                filteredTemplates.add(template);
            }
        }
        return filteredTemplates;
    }

    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the default value instead (always 1) */
    private int parseInt(String p_string)
    {
        int intVal = 1;
        if (p_string != null)
        {
            try
            {
                intVal = Integer.parseInt(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return intVal;
    }

    private void saveDuplicates(HttpServletRequest p_request,
            HttpSession session) throws EnvoyServletException
    {
        try
        {
            String wftiId = (String) session.getAttribute(WF_TEMPLATE_INFO_ID);
            String list = (String) p_request.getParameter("localePairs");
            String name = (String) p_request.getParameter("nameTF").trim();
            String projectId = p_request.getParameter(PROJECT_FIELD);
            Project project = ServerProxy.getProjectHandler().getProjectById(
                    Long.parseLong(projectId));
            ArrayList<LocalePair> alist = new ArrayList<LocalePair>();
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens())
            {
                String id = (String) st.nextToken();
                alist.add(ServerProxy.getLocaleManager().getLocalePairById(
                        Long.parseLong(id)));
            }

            WorkflowTemplateHandlerHelper.duplicateWorkflowTemplateInfo(
                    Long.parseLong(wftiId), alist, name, project,
                    getBundle(session));
            for (LocalePair localePair : alist)
            {
                String dupWorkflowTemplateName = generateName(name,localePair);
                OperationLog
                        .log(m_userId, OperationLog.EVENT_ADD,
                                OperationLog.COMPONET_WORKFLOW,
                                dupWorkflowTemplateName);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private String generateName(String name, LocalePair localePair)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("_");
        sb.append(localePair.getSource().toString());
        sb.append("_");
        sb.append(localePair.getTarget().toString());
        return sb.toString();
    }

    private void setNumberPerPage(HttpServletRequest req) {
        String pageSize = (String) req.getParameter("numOfPageSize");
        if (!StringUtil.isEmpty(pageSize)) {
            try
            {
                m_numOfWfsPerPage = Integer.parseInt(pageSize);
            }
            catch (Exception e)
            {
                m_numOfWfsPerPage = Integer.MAX_VALUE;
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////

}

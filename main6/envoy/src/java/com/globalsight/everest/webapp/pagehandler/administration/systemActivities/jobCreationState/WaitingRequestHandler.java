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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobCreationState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.util.fileImport.FileImportUtil;
import com.globalsight.cxe.util.fileImport.sort.ImportRequestSortUtil;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.RequestFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ObjectUtil;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class WaitingRequestHandler extends PageActionHandler
{
    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        for (String key : keys)
        {
            FileImportUtil.cancelUnimportFile(key);
        }
    }
    
    @ActionHandler(action = "upPriority", formClass = "")
    public void upPriority(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        ImportRequestSortUtil.upRequest(keys);
    }
    
    @ActionHandler(action = "downPriority", formClass = "")
    public void downPriority(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        ImportRequestSortUtil.downRequest(keys);
    }
    
    @ActionHandler(action = "topPriority", formClass = "")
    public void topPriority(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        ImportRequestSortUtil.topRequest(keys);
    }
    
    @ActionHandler(action = "bottomPriority", formClass = "")
    public void bottomPriority(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] keys = request.getParameterValues("key");
        ImportRequestSortUtil.bottomRequest(keys);
    }
    
    
    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        
        int n = 10;
        String size = request.getParameter("numOfPageSize");
        if (size != null)
        {
            n = Integer.parseInt(size);
            session.setAttribute("systemActivityPageSize", size);
        }
        else
        {
            size = (String) session.getAttribute("systemActivityPageSize");
            if (size != null)
            {
                n = Integer.parseInt(size);
            }
        }
        
        List<RequestFile> requestVos = getAllRequestVos();
        
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        setTableNavigation(request, session, requestVos,
                new RequestFileComparator(uiLocale), n,
                "waitingRequestDefine", "waitingRequestDefineKey");

        String tableNav = "";
        String tableNav2 = "";
        if (requestVos.size() == 0)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            tableNav = bundle.getString("lb_displaying_zero");
            tableNav2 = getNav2(n);
        }
        
        request.setAttribute("tableNav", tableNav);
        request.setAttribute("tableNav2", tableNav2);
        request.setAttribute("pageSize", n);
        request.setAttribute("requestVos", requestVos);
    }

    private String getNav2(int pageSize)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Display #: ");
        sb.append("<select id='numOfPageSize' onchange='changePageSize(this.value);'>");
        List<Integer> pageScopes = new ArrayList<Integer>();
        pageScopes.add(10);
        pageScopes.add(20);
        pageScopes.add(50);
        for (Integer s : pageScopes)
        {
            if (pageSize == s)
                sb.append("<option value='" + s + "' selected>" + s + "</option>");
            else
                sb.append("<option value='" + s + "'>" + s + "</option>");
        }
        sb.append("</select>&nbsp;&nbsp;");
        return sb.toString();
    }
    
    @SuppressWarnings("rawtypes")
    private List<RequestFile> getAllRequestVos()
    {
        HashMap<String, String> fileProfileId2priority = new HashMap<String, String>();
        HashMap<String, FileProfileImpl> fileProfiles = new HashMap<String, FileProfileImpl>();
        
        List<RequestFile> requestVos = new ArrayList<RequestFile>();
        HashMap<String, List<CxeMessage>> ms = FileImportUtil.getCloneHoldingRequests();
        for (List<CxeMessage> ms2 : ms.values())
        {
            int i = 1;
            for (CxeMessage t : ms2)
            {
                RequestFile requestVo = new RequestFile();
                HashMap args = t.getParameters();
                String cId = (String) args.get("currentCompanyId");
                String companyName = CompanyWrapper.getCompanyNameById(cId);
                requestVo.setCompany(companyName);

                String fileName = (String) args.get("Filename");
                String fullname = AmbFileStoragePathUtils.getCxeDocDirPath()
                        + File.separator + companyName + File.separator + fileName;
                requestVo.setFile(fileName);
                requestVo.setSize(new File(fullname).length());
                requestVo.setKey((String) args.get("uiKey"));
                String fileProfileId = (String) args.get("FileProfileId");
                FileProfileImpl fp = fileProfiles.get(fileProfileId);
                if (fp == null)
                {
                    fp = HibernateUtil.get(FileProfileImpl.class,
                            Long.parseLong(fileProfileId));
                    if (fp == null)
                    {
                        String hql = "from FileProfileImpl fp where fp.referenceFP = ?";
                        fp = (FileProfileImpl) HibernateUtil.getFirst(hql, Long.parseLong(fileProfileId));
                    }
                    
                    fileProfiles.put(fileProfileId, fp);
                }
                
                requestVo.setFileProfile(fp.getName());
                String jobId = (String) args.get("JobId");
                JobImpl job = HibernateUtil.get(JobImpl.class,
                        Long.parseLong(jobId));
                requestVo.setJobId(job.getJobId());
                requestVo.setJobName(job.getJobName());
                String priority = (String) args.get("priority");
                if (priority == null || priority.length() == 0)
                {
                    priority = fileProfileId2priority.get(fileProfileId);
                    if (priority == null)
                    {
                        long l10Id = fp.getL10nProfileId();
                        BasicL10nProfile l10Profile = HibernateUtil.get(
                                BasicL10nProfile.class, l10Id);
                        priority = Integer.toString(l10Profile.getPriority());
                        
                        fileProfileId2priority.put(fileProfileId, priority);
                    }
                }
                
                int sortPriority = (Integer) args.get("sortPriority");
                
                requestVo.setPriority(priority);
                requestVo.setRequestTime((Date) args.get("requestTime"));
                requestVo.setProject(job.getProject().getName());
                requestVo.setSortTime((Long)(args.get("sortTime")));
                requestVo.setSortPriority(sortPriority);
                requestVo.setSortIndex(i++);
                requestVos.add(requestVo);
            }
        }
        
        return requestVos;
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
        
        String[] keys = request.getParameterValues("key");
        if (keys != null)
        {
            ArrayList<String> keyList = new ArrayList<String>();
            for (String key : keys)
            {
                keyList.add(key);
            }
            request.setAttribute("selectedKeys", keyList);
        }
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws EnvoyServletException,
            ServletException, IOException
    {

    }
}

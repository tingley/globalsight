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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobExportState;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.fileExport.FileExportUtil;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.util.comparator.ExportRequestComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.pagehandler.administration.systemActivities.RequestAbstractHandler;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.ObjectUtil;

public class RunningRequestHandler extends RequestAbstractHandler
{

    @Override
    protected void cancelRequest(String key)
    {
//        FileExportUtil.cancelUnimportFile(key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getAllVos()
    {
        return getAllRequestVos();
    }
    
    @SuppressWarnings({ "unused", "rawtypes" })
    private List<RequestFile> getAllRequestVos()
    {
        HashMap<String, String> fileProfileId2priority = new HashMap<String, String>();
        HashMap<String, FileProfileImpl> fileProfiles = new HashMap<String, FileProfileImpl>();
        
        List<RequestFile> requestVos = new ArrayList<RequestFile>();
        HashMap<String, Hashtable> ms = FileExportUtil.getCloneRunningRequests();
        for (Hashtable args : ms.values())
        {   

            RequestFile requestVo = new RequestFile();
            String cId = (String) args.get("currentCompanyId");
            String companyName = CompanyWrapper.getCompanyNameById(cId);
            requestVo.setCompany(companyName);

            String fileName = (String) args.get("filePath");
            String fullname = AmbFileStoragePathUtils.getCxeDocDirPath()
                    + File.separator + companyName + File.separator + fileName;
            requestVo.setFile(fileName);
            requestVo.setSize(new File(fullname).length());
            requestVo.setKey((String) args.get("key"));

            ExportParameters exportParameters = (ExportParameters) args.get(new Integer(1));
            long workFlowId = exportParameters.getWorkflowId();
            if (workFlowId >0)
            {
                WorkflowImpl wk = HibernateUtil.get(WorkflowImpl.class, workFlowId);
                if (wk != null)
                {
                    requestVo.setWorkflowId("" +workFlowId);
                    requestVo.setWorkflowLocale(wk.getTargetLocale().toString());
                    
                    JobImpl job = (JobImpl) wk.getJob();
                    requestVo.setJobId(job.getJobId());
                    requestVo.setJobName(job.getJobName());
                    requestVo.setProject(job.getProject().getName());
                }
            }
            
            requestVo.setRequestTime((Date) args.get("requestTime"));
            requestVos.add(requestVo);

        }

        return requestVos;
    }

    @Override
    protected StringComparator getComparator(Locale uiLocale)
    {
        return new ExportRequestComparator(uiLocale);
    }

}

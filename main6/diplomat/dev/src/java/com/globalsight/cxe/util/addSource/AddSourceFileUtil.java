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
package com.globalsight.cxe.util.addSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.util.applet.AddFileVo;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.SortUtil;

/**
 * Class {@code AddSourceFileUtil} is used for adding source files without using
 * JMS.
 * 
 * @since GBS-4400
 */
public class AddSourceFileUtil
{
    static private final Logger logger = Logger.getLogger(AddSourceFileUtil.class);

    /**
     * Adds a source file asynchronously with thread instead of JMS.
     */
    static public void addSourceFileWithThread(AddFileVo file)
    {
        AddSourceFileRunnable runnable = new AddSourceFileRunnable(file);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Adds a source file synchronously.
     */
    static public void addSourceFile(AddFileVo p_file)
    {
        if (p_file == null)
        {
            return;
        }
        try
        {
            Vector<FileProfileImpl> fileProfiles = new Vector<FileProfileImpl>();
            Vector<File> files = new Vector<File>();
            Vector<String> locales = new Vector<String>();

            long jobId = p_file.getJobId();
            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            HibernateUtil.getSession().refresh(job);

            CompanyThreadLocal.getInstance().setIdValue(String.valueOf(job.getCompanyId()));

            StringBuffer allLocales = new StringBuffer();
            for (Workflow w : job.getWorkflows())
            {
                if (allLocales.length() > 0)
                {
                    allLocales.append(",");
                }
                allLocales.append(w.getTargetLocale().toString());
            }

            List<Long> fpIds = p_file.getFileProfileIds();
            for (Long id : fpIds)
            {
                fileProfiles.add(HibernateUtil.get(FileProfileImpl.class, id, false));
                locales.add(allLocales.toString());
            }

            List<String> filePaths = p_file.getFilePaths();
            String locale = p_file.getLocale();
            if (locale == null)
            {
                locale = job.getSourceLocale().toString();
            }

            Boolean fromDi = null;
            File root = AmbFileStoragePathUtils.getCxeDocDir();
            for (String path : filePaths)
            {
                File targetFile = new File(root, path);
                if (!targetFile.exists())
                {
                    String newPath = new StringBuffer(locale).append(File.separator)
                            .append(job.getName()).append(File.separator).append(path).toString();
                    targetFile = new File(root, newPath);
                }
                else if (fromDi == null)
                {
                    path = path.replace("\\", "/");
                    String[] nodes = path.split("/");
                    fromDi = nodes.length > 1 && "webservice".equals(nodes[1]);
                }

                files.add(targetFile);
            }

            String username = job.getCreateUser().getUserName();
            String jobName = job.getName();

            Vector result = FileSystemUtil.execScript(files, fileProfiles, locales, jobId, jobName);
            Vector sFiles = (Vector) result.get(0);
            Vector exitValues = (Vector) result.get(3);

            int addedCount = job.getRequestSet().size();
            int pageCount = sFiles.size();

            List<Request> requests = new ArrayList<Request>();
            requests.addAll(job.getRequestSet());
            SortUtil.sort(requests, new Comparator<Request>()
            {
                @Override
                public int compare(Request o1, Request o2)
                {
                    BatchInfo info1 = o1.getBatchInfo();
                    BatchInfo info2 = o2.getBatchInfo();

                    if (info1 != null && info2 != null)
                    {
                        return info1.getPageNumber() > info2.getPageNumber() ? 1 : -1;
                    }

                    return 0;
                }
            });

            for (int i = 0; i < requests.size(); i++)
            {
                Request request = requests.get(i);
                BatchInfo info = request.getBatchInfo();

                info.setPageCount(addedCount + pageCount);
                info.setPageNumber(i + 1);
                info.setDocPageCount(1);
                info.setDocPageNumber(1);
            }

            String orgState = job.getState();
            job.setState(Job.ADD_FILE);
            job.setOrgState(orgState);
            HibernateUtil.update(job);

            for (int i = 0; i < pageCount; i++)
            {
                File realFile = (File) sFiles.get(i);
                String path = realFile.getPath();
                String relativeName = path
                        .substring(AmbFileStoragePathUtils.getCxeDocDir().getPath().length() + 1);

                try
                {
                    String key = jobName + relativeName + (addedCount + i + 1);
                    CxeProxy.setTargetLocales(key, locales.get(i));
                    logger.info("Publishing import request to CXE for file " + relativeName);
                    CxeProxy.importFromFileSystem(relativeName, job.getId(), jobName, job.getUuid(),
                            jobName, "" + fileProfiles.get(i).getId(),
                            new Integer(addedCount + pageCount), new Integer(addedCount + i + 1),
                            new Integer(1), new Integer(1), fromDi, Boolean.FALSE,
                            CxeProxy.IMPORT_TYPE_L10N, username, (Integer) exitValues.get(i),
                            "" + job.getPriority());
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}

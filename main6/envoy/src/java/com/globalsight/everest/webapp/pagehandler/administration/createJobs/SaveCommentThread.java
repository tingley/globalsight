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
package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;

public class SaveCommentThread extends Thread
{
    private static final Logger logger = Logger
            .getLogger(SaveCommentThread.class);

    private String jobName;
    private String comment;
    private String userId;
    private String attachmentName;
    private String baseStorageFolder;

    public SaveCommentThread(String jobName, String comment,
            String attachmentName, String userId, String baseStorageFolder)
    {
        this.jobName = jobName;
        this.comment = comment;
        this.userId = userId;
        this.attachmentName = attachmentName;
        this.baseStorageFolder = baseStorageFolder;
    }

    public void run()
    {
        try
        {
            Job job = ServerProxy.getJobHandler().getJobByJobName(jobName);

            Comment comm = ServerProxy.getCommentManager().saveComment(
                    (WorkObject) job, job.getId(), userId, comment);

            // save attachment
            if (StringUtils.isNotEmpty(attachmentName))
            {
                long commentId = comm.getId();
                String folder = (new File(baseStorageFolder)).getParentFile()
                        .getParentFile().getPath()
                        + File.separator + commentId;
                File src = new File(baseStorageFolder + File.separator
                        + attachmentName);
                File des = new File(folder + File.separator + "General"
                        + File.separator + attachmentName);
                FileUtil.copyFile(src, des);
                FileUtil.deleteFile(new File(baseStorageFolder));
            }
        }
        catch (Exception e)
        {
            logger.error("Error when saving comment to job " + jobName, e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}

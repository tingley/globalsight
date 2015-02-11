package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.FileUtil;

public class SaveCommentThread extends Thread
{

    private String jobName;
    private String comment;
    private String userName;
    private String attachmentName;
    private String baseStorageFolder;
    
    public SaveCommentThread(String jobName, String comment,
            String attachmentName, String userName, String baseStorageFolder)
    {
        this.jobName = jobName;
        this.comment = comment;
        this.userName = userName;
        this.attachmentName = attachmentName;
        this.baseStorageFolder = baseStorageFolder;
    }
    
    public void run()
    {
        try
        {
            Job job = null;
            do
            {
                // search the database for job information every 10 seconds,
                // until the job is found
                sleep(10000);
                job = ServerProxy.getJobHandler().getJobByJobName(jobName);
            }
            while (job == null);
            Comment comm = ServerProxy.getCommentManager().saveComment((WorkObject) job,
                    job.getId(), userName, comment);
            
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
            e.printStackTrace();
        }
    }
}

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
package com.globalsight.everest.comment;

import java.util.Calendar;
import java.util.Date;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobPersistenceAccessor;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskPersistenceAccessor;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * For GBS-3780 jobs have comments from different companies. Splits the comments
 * table into 3 tables (job comments, workflow comments, activity comments).
 *
 */
public class CommentUtil
{
    private static Integer UUID_PREFIX = 1;
    public static Object LOCKER = new Object();
    
    /**
     * Updates job comment with id and comment text.
     * 
     * @param p_commentId
     * @param p_userId
     * @param p_comment
     * @return the updated job comment.
     */
    public static Comment updateJobComment(long p_commentId, String p_userId,
            String p_comment)
    {
        JobComment c = HibernateUtil.get(JobComment.class, p_commentId);
        if (c != null){
            Job job = (Job) c.getWorkObject();
            int jcIndex = job.getJobComments().indexOf(c);
            if (jcIndex > -1)
            {
                Comment jobComment = (Comment) job.getJobComments()
                        .get(jcIndex);
                updateComment((CommentImpl) jobComment,
                        p_comment, p_userId);
                JobPersistenceAccessor.updateJobState(job);
            }
        }
        
        return c;
    }
    
    /**
     * Updates task comment with id and comment text.
     * 
     * @param p_commentId
     * @param p_userId
     * @param p_comment
     * @return
     */
    public static Comment updateTaskComment(long p_commentId, String p_userId,
            String p_comment)
    {
        TaskComment c = HibernateUtil.get(TaskComment.class, p_commentId);
        if (c != null){
            Task t = (Task) c.getWorkObject();
            int tcIndex = t.getTaskComments().indexOf(c);
            if (tcIndex > -1)
            {
                Comment taskComment = (Comment) t.getTaskComments()
                        .get(tcIndex);
                updateComment((CommentImpl) taskComment,
                        p_comment, p_userId);
                TaskPersistenceAccessor.updateTask(t);
            }
        }
        
        return c;
    }
    
    /**
     * Updates the comment text, modifier user id and the modified date.
     * 
     * @param p_originalComment
     * @param p_updatedComment
     * @param p_modifierUserId
     */
    private static void updateComment(CommentImpl p_originalComment,
            String p_updatedComment, String p_modifierUserId)
    {
        if (p_originalComment != null)
        {
            p_originalComment.setComment(p_updatedComment);
            p_originalComment.setModifierId(p_modifierUserId);
            p_originalComment.setModifiedDate(Calendar.getInstance().getTime());
        }
    }

    /**
     * <p>
     * For GBS-3780 jobs have comments from different companies. Splits the
     * comments table into 3 tables (job comments, workflow comments, activity
     * comments).
     * <p>
     * There are a lot of code use ID to get the corresponding files,
     * so it is needed that keep the id is unique in job comments and activity
     * comment.
     * @return
     */
    public static long createUuid()
    {
        synchronized (LOCKER)
        {
            UUID_PREFIX++;
            Date date = new Date();
            return Long.parseLong(Integer.toString(UUID_PREFIX) + date.getTime());
        }
    }
}

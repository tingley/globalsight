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
package com.globalsight.everest.jobhandler;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.User;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobPersistenceAccessor
{
    public static void updateJobState(Job p_job) throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            JobImpl job = (JobImpl) session.get(JobImpl.class, new Long(p_job
                    .getId()));

            if (job != null)
            {
                job.setState(p_job.getState());
                session.update(job);
            }

            transaction.commit();
        }
        catch (Exception pe)
        {
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, pe,
                    JobException.PROPERTY_FILE_NAME);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Updates the page count in the job.
     * 
     * @param p_job
     *            The job to update.
     * @param p_pageCount
     *            The new page count in the job.
     * 
     * @return Job The updated job.
     */
    public static Job updatePageCount(Job p_job, int p_pageCount)
            throws JobException
    {
        Job job = null;
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        try
        {
            job = (JobImpl) session.get(JobImpl.class, new Long(p_job.getId()));
            if (job != null)
            {
                job.setPageCount(p_pageCount);
                session.update(job);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);

        }
        finally
        {
            // session.close();
        }
        return job;
    }

    /**
     * Updates the quotation email date in the job.
     * 
     * @param p_job
     *            The job to update.
     * @param p_quoteDate
     *            The new quotation email date in the job.
     * 
     */
    public static void updateQuoteDate(Job p_job, String p_quoteDate)
            throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            JobImpl job = (JobImpl) session.get(JobImpl.class, new Long(p_job
                    .getId()));
            if (job != null)
            {
                job.setQuoteDate(p_quoteDate);
                session.update(job);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Update Approve quote date for job.
     * 
     * @param p_job
     *            The job to update.
     * @param p_quoteApprovedDate
     *            The new date of Approve Quote.
     * @throws JobException
     */
    public static void updateQuoteApprovedDate(Job p_job,
            String p_quoteApprovedDate) throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            JobImpl job = (JobImpl) session.get(JobImpl.class, new Long(p_job
                    .getId()));
            if (job != null)
            {
                job.setQuoteApprovedDate(p_quoteApprovedDate);
                session.update(job);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Update the quote PO Number
     * 
     * @param p_job
     *            The job to update.
     * @param p_quotePoNumber
     *            The new PO number.
     * @throws JobException
     */
    public static void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
            throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            JobImpl job = (JobImpl) session.get(JobImpl.class, new Long(p_job
                    .getId()));
            if (job != null)
            {
                job.setQuotePoNumber(p_quotePoNumber);
                session.update(job);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);
        }
        finally
        {
            // session.close();
        }
    }

    public static void updateAuthoriserUser(Job p_job, User user)
            throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            JobImpl job = (JobImpl) session.get(JobImpl.class, new Long(p_job
                    .getId()));
            if (job != null)
            {
                job.setUserId(user.getUserId());
                session.update(job);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Updates the page count and state in a job.
     * 
     */
    public static Job updatePageCountAndState(Job p_job, String p_newState,
            int p_newPageCount) throws JobException
    {
        try
        {
            if (p_job != null)
            {
                p_job.setState(p_newState);
                p_job.setPageCount(p_newPageCount);
                HibernateUtil.saveOrUpdate(p_job);
            }
        }
        catch (Exception e)
        {
            throw new JobException(
                    JobException.MSG_PERSISTENCE_SERVICE_FAILURE, null, e);

        }
        finally
        {
            // session.close();
        }
        return p_job;
    }

    /**
     * Updates the overriden word count for a job.
     */
    public static Job overrideWordCount(Job p_job, int p_wordCount)
            throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        JobImpl job = null;
        try
        {
            job = (JobImpl) session.get(JobImpl.class, new Long(p_job.getId()));
            if (job != null)
            {
                job.overrideWordCount(p_wordCount);
                session.update(job);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            String[] args = { Long.toString(p_job.getId()),
                    Long.toString(p_wordCount) };
            throw new JobException(
                    JobException.MSG_FAILED_TO_OVERRIDE_WORD_COUNT, args, e);
        }
        finally
        {
            // session.close();
        }
        return job;
    }

    /**
     * Removes the overriden word count for a job. The job's total word count
     * will now be calculated from the sum of all its source pages' word counts.
     * 
     * @param p_job
     *            The job to clear the word count from.
     * 
     * @return The job after its word count has been cleared.
     */
    public static Job clearOverridenWordCount(Job p_job) throws JobException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        JobImpl job = null;
        try
        {
            job = (JobImpl) session.get(JobImpl.class, new Long(p_job.getId()));
            if (job != null)
            {
                job.clearOverridenWordCount();
                session.update(job);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            String[] args = { Long.toString(p_job.getId()) };
            throw new JobException(
                    JobException.MSG_FAILED_TO_CLEAR_WC_OVERRIDE, args, e);
        }
        finally
        {
            // session.close();
        }
        return job;
    }

    /**
     * To retrieve a job object from TopLink by a given id.
     * 
     * @param p_jobId
     *            the id of the job
     * @param p_editable
     *            Specifies whether the object returned should be editable
     *            (cloned) or not.
     * @return a Job object.
     */
    public static Job getJob(long p_jobId, boolean p_editable)
            throws JobException
    {
        Job job = null;
        try
        {
            job = (JobImpl) HibernateUtil.get(JobImpl.class, p_jobId);
        }
        catch (Exception pe)
        {
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_ID,
                    null, pe);
        }
        return job;
    }

}

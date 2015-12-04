/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.jobhandler;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.calendar.CalendarWorkingDay;
import com.globalsight.calendar.CalendarWorkingHour;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.Holiday;
import com.globalsight.calendar.WorkingHour;
import com.globalsight.config.SystemParameter;
import com.globalsight.config.SystemParameterEntityException;
import com.globalsight.config.SystemParameterImpl;
import com.globalsight.config.SystemParameterPersistenceManager;
import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.cxe.entity.exportlocation.ExportLocationImpl;
import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationEntityException;
import com.globalsight.everest.company.Category;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.PostReviewCategory;
import com.globalsight.everest.company.ScorecardCategory;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.IsoCurrency;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.jobmanagement.JobDispatchEngine;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileDescriptorModifier;
import com.globalsight.everest.persistence.page.SourcePageDescriptorModifier;
import com.globalsight.everest.persistence.request.RequestDescriptorModifier;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.comparator.PageComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.config.ConfigMainHandler;
import com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.jobvo.JobVoSearchCriteria;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.jobcreation.JobCreationQuery;
import com.globalsight.persistence.jobcreation.RemoveRequestFromJobCommand;
import com.globalsight.terminology.ITermbase;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * JobHandlerLocal implements JobHandler and is responsible for handling and
 * delegating job related processes.
 */
public class JobHandlerLocal implements JobHandler
{
    //
    // PRIVATE STATIC VARIABLES
    //
    private static Logger c_category = Logger.getLogger(JobHandlerLocal.class
            .getName());

    private static final String MANAGER_ID_ARG = "managerId";

    private static final String JOB_CREATE_DATE = "jobCreateDate";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private PersistenceService m_tlp;

    private LocaleManager m_locMgr;

    private WorkflowManager m_wfMgr;

    private UserManager m_userMgr;

    private CostingEngine m_costing;

    private Hashtable<String, JobResultSetCursor> m_jobCursorList;

    private int m_size;

    private int m_myJobsDaysRetrieved = 0;

    //
    // PUBLIC CONSTRUCTORS
    //
    public JobHandlerLocal()
    {
        super();
    }

    /**
     * Cancels the entire job and all workflows - doesn't matter what state they
     * are in. Not allowed from the GUI, but done from the Server side in
     * certain circumstances.
     */
    public void cancelJob(Job p_job) throws RemoteException, JobException
    {
        JobDispatchEngine jobDispatchEngine = getJobDispatchEngine();
        jobDispatchEngine.cancelJob(p_job, false);
    }

    /**
     * Cancels the entire job and all workflows - doesn't matter what state they
     * are in. Not allowed from the GUI, but done from the Server side in
     * certain circumstances.
     */
    public void cancelJob(Job p_job, boolean p_reimport)
            throws RemoteException, JobException
    {
        JobDispatchEngine jobDispatchEngine = getJobDispatchEngine();
        jobDispatchEngine.cancelJob(p_job, p_reimport);
    }

    /**
     * @see JobHandler.cancelJob(long)
     */
    public void cancelJob(long p_jobId) throws RemoteException, JobException
    {
        Job j = getJobById(p_jobId);
        cancelJob(j);
    }

    /**
     * @see JobHandler.cancelJob(String, Job, String)
     */
    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, JobException
    {
        JobDispatchEngine jobDispatchEngine = getJobDispatchEngine();
        jobDispatchEngine.cancelJob(p_idOfUserRequestingCancel, p_job, p_state,
                false);
    }

    /**
     * Dispatch the job specified by its job id;
     * <p>
     * 
     * @param p_job
     *            the specified job
     * @throws RemoteException
     *             , JobException
     */
    public void dispatchJob(Job p_job) throws RemoteException, JobException
    {
        JobDispatchEngine jobDispatchEngine = getJobDispatchEngine();
        jobDispatchEngine.dispatchJob(p_job);
    }

    /*
     * @see JobHandler.cancelImportErrorPage(String, Job)
     */
    public void cancelImportErrorPages(String p_idOfUserRequestingCancel,
            Job p_job) throws RemoteException, JobException
    {
        String jobState = p_job.getState();
        // if job is in an error state
        if (Job.IMPORTFAILED.equals(jobState))
        {
            // if the job contains all error pages treat it as a
            // cancellation
            if (containsAllErrors(p_job))
            {
                // cancel the job and all workflows whether in
                // the PENDING or IMPORT_FAIL state
                cancelJob(p_idOfUserRequestingCancel, p_job, null);
            }
            else
            {
                try
                {
                    ServerProxy.getJobEventObserver();
                    // remove request in database
                    int numOfRemovedRequests = removeErrorRequests(p_job
                            .getId());

                    // Refresh the job through toplink to reload the
                    // in-memory requestlist, which has not been updated
                    // by removeErrorRequest().
                    Job job = getJobById(p_job.getId());

                    // Set the state of the job to pending since it doesn't
                    // have any error requests as part of it anymore.
                    // and update the number of pages since some were removed.
                    // Update in the cache and DB.
                    int numOfPages = job.getPageCount() - numOfRemovedRequests;
                    job = JobPersistenceAccessor.updatePageCountAndState(job,
                            Job.PENDING, numOfPages);
                    Collection<Request> requests = job.getRequestList();
                    Request req = null;

                    if (requests.size() > 0)
                    {
                        req = (Request) requests.iterator().next();
                    }

                    // if this is a batch job - contains a batch request
                    if (req != null && req.getBatchInfo() != null)
                    {
                        // notify the job dispatcher - it is ready to
                        // be dispatched
                        getJobDispatchEngine().dispatchBatchJob(job);
                    }
                    // not batch - just a regular job
                    else
                    {
                        // notify the job dispatcher - the job may be
                        // ready for dispatching
                        getJobDispatchEngine().wordCountIncreased(job);
                    }
                }
                catch (Exception e)
                {
                    c_category.error("Failure when modifying failed job "
                            + p_job.getId()
                            + " and moving to pending/dispatch.", e);
                    String args[] = new String[1];
                    args[0] = Long.toString(p_job.getId());
                    throw new JobException(
                            JobException.MSG_FAILED_TO_MOVE_FAILED_JOB_INTO_PENDING_STATE_AND_DISPATCH,
                            args, e);
                }
            }
        }
    }

    public void archiveJob(Job p_job) throws RemoteException, JobException
    {
        try
        {
            getWorkflowManager().archive(p_job);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_job.getId());
            throw new JobException(JobException.MSG_FAILED_TO_ARCHIVE_JOB,
                    args, e);
        }
    }

    /**
     * @see JobHandler.getActivity(String, String)
     */
    public Activity getActivityByCompanyId(String p_activityName,
            String p_companyId) throws RemoteException, JobException
    {
        Activity result = null;
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.name = :name and a.companyId = :cId";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", p_activityName);
            map.put("cId", Long.parseLong(p_companyId));
            Iterator<?> it = HibernateUtil.search(hql, map).iterator();
            if (it.hasNext())
            {
                result = (Activity) it.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getActivity", e);
            String[] args = new String[1];
            args[0] = p_activityName;
            throw new JobException(JobException.MSG_FAILED_TO_GET_ACTIVITY,
                    args, e);
        }
        return result;
    }

    /**
     * @see JobHandler.getActivity(String)
     */
    public Activity getActivity(String p_activityName) throws RemoteException,
            JobException
    {
        Activity result = null;
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.name = :name";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", p_activityName);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }

            Iterator<?> it = HibernateUtil.search(hql, map).iterator();

            if (it.hasNext())
            {
                result = (Activity) it.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getActivity", e);
            String[] args = new String[1];
            args[0] = p_activityName;
            throw new JobException(JobException.MSG_FAILED_TO_GET_ACTIVITY,
                    args, e);
        }
        return result;
    }

    /**
     * @see JobHandler.getActivity(String)
     */
    public Activity getActivityByDisplayName(String p_activityName)
            throws RemoteException, JobException
    {
        Activity result = null;
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.displayName = :name";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", p_activityName);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }

            Iterator<?> it = HibernateUtil.search(hql, map).iterator();

            if (it.hasNext())
            {
                result = (Activity) it.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getActivity", e);
            String[] args = new String[1];
            args[0] = p_activityName;
            throw new JobException(JobException.MSG_FAILED_TO_GET_ACTIVITY,
                    args, e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Collection<Activity> getAllDtpActivities() throws RemoteException,
            JobException
    {
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.useType = 'DTP'";

            Map<String, Long> map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap<String, Long>();
                map.put("companyId", Long.parseLong(currentId));
            }

            return (Collection<Activity>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getAllDtpActivities", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_ALL_DTP_ACTIVITIES, null, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Activity> getAllTransActivities() throws RemoteException,
            JobException
    {
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.useType = 'TRANS'";

            Map<String, Long> map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap<String, Long>();
                map.put("companyId", Long.parseLong(currentId));
            }

            return (Collection<Activity>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getAllTransActivities", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_ALL_TRANS_ACTIVITIES, null,
                    e);
        }
    }

    /**
     * Get a list of all existing activities in the system.
     * 
     * @throws java.rmi.RemoteException
     *             Network related exception.
     * @throws JobException
     *             Component related exception.
     */
    @SuppressWarnings("unchecked")
    public Collection<Activity> getAllActivities() throws RemoteException,
            JobException
    {
        try
        {
            String hql = "from Activity a where a.isActive = 'Y'";

            Map<String, Long> map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap<String, Long>();
                map.put("companyId", Long.parseLong(currentId));
            }

            return (Collection<Activity>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getAllActivities", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_ALL_ACTIVITIES, null, e);
        }
    }

    /**
     * Get a list of all existing activities in the system.
     * 
     * @throws java.rmi.RemoteException
     *             Network related exception.
     * @throws JobException
     *             Component related exception.
     */
    @SuppressWarnings("unchecked")
    public Collection<Activity> getAllActivitiesByCompanyId(String p_companyId)
            throws RemoteException, JobException
    {
        try
        {
            String hql = "from Activity a where a.isActive = 'Y' and a.companyId = :companyId";
            Map<String, Long> map = new HashMap<String, Long>();
            map.put("companyId", Long.parseLong(p_companyId));

            return (Collection<Activity>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getAllActivities", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_ALL_ACTIVITIES, null, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Company> getAllCompanies() throws RemoteException,
            JobException
    {
        try
        {
            String hql = "from Company c where c.isActive = 'Y'";
            return (Collection<Company>) HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getAllCompanies", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_ALL_COMPANIES, null, e);
        }
    }

    public Company getCompany(String p_companyName) throws RemoteException,
            JobException
    {
        Company result = null;
        try
        {
            String hql = "from Company c where c.isActive = 'Y' and lower(c.name) = :name";
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", p_companyName.toLowerCase());

            Iterator<?> it = HibernateUtil.search(hql, map).iterator();
            if (it.hasNext())
            {
                result = (Company) it.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getCompany", e);
            String[] args = new String[1];
            args[0] = p_companyName;
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_COMPANY_BY_NAME, args, e);
        }
        return result;
    }

    public Company getCompanyById(long p_companyId) throws RemoteException,
            JobException
    {
        Company result = null;
        try
        {
            String hql = "from Company c where c.isActive = 'Y' and c.id = ?";
            Iterator<?> it = HibernateUtil.search(hql, new Long(p_companyId))
                    .iterator();
            if (it.hasNext())
            {
                result = (Company) it.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getCompany", e);
            String[] args = new String[1];
            args[0] = Long.toString(p_companyId);
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_COMPANY_BY_ID, args, e);
        }
        return result;
    }

    public void modifyCompany(Company p_company) throws RemoteException,
            JobException
    {
        try
        {
            Company c = (Company) HibernateUtil.get(Company.class,
                    p_company.getIdAsLong());
            if (c != null)
            {
                c.setDescription(p_company.getDescription());
                c.setEmail(p_company.getEmail());
                c.setEnableIPFilter(p_company.getEnableIPFilter());
                c.setSessionTime(p_company.getSessionTime());
                c.setEnableTMAccessControl(p_company.getEnableTMAccessControl());
                c.setEnableTBAccessControl(p_company.getEnableTBAccessControl());
                c.setEnableQAChecks(p_company.getEnableQAChecks());
                c.setEnableSSOLogin(p_company.getEnableSSOLogin());
                c.setSsoIdpUrl(p_company.getSsoIdpUrl());
                c.setTmVersion(p_company.getTmVersion());
                c.setBigDataStoreLevel(p_company.getBigDataStoreLevel());
                c.setEnableDitaChecks(p_company.getEnableDitaChecks());
                c.setEnableWorkflowStatePosts(p_company
                        .getEnableWorkflowStatePosts());
                HibernateUtil.update(c);

                if (p_company.getTmVersion().getValue() == 3)
                {
                    Connection conn = DbUtil.getConnection();
                    conn.setAutoCommit(false);
                    try
                    {
                        createTmStorage(c.getId(), conn);
                        conn.commit();
                    }
                    finally
                    {
                        DbUtil.returnConnection(conn);
                    }
                }
            }
        }
        catch (Exception pe)
        {
            c_category.error("JobHandlerLocal::modifyCompany", pe);
            String args[] = new String[1];
            args[0] = p_company.getCompanyName();
            throw new JobException(JobException.MSG_FAILED_TO_MODIFY_COMPANY,
                    args, pe);
        }
    }

    public void removeCompany(Company p_company) throws RemoteException,
            JobException
    {
        try
        {
            long companyId = p_company.getId();
            HibernateUtil.delete(p_company);
            // Delete TM storage. Use a dedicated connection for this
            // since session.connection() will leak. Note that this
            // is non-transactional.
            Connection conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            try
            {
                removeTmStorage(companyId, conn);
                conn.commit();
            }
            finally
            {
                DbUtil.returnConnection(conn);
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::removeCompany", e);
            String args[] = new String[1];
            args[0] = p_company.getCompanyName();
            throw new JobException(JobException.MSG_FAILED_TO_REMOVE_COMPANY,
                    args, e);
        }
    }

    //
    // public Company createCompany(Company p_company) throws RemoteException,
    // JobException
    // {
    // try
    // {
    // //verify that its name is not a duplicate
    // Vector args = new Vector();
    // args.add(p_company.getName().toUpperCase());
    // Collection companies = getPersistence().executeNamedQuery(
    // CompanyQueryNames.COMPANY_BY_NAME, args, false);
    //
    // // if none return than not a duplicate
    // if (companies == null ||
    // companies.size() <= 0)
    // {
    // UnitOfWork uow = getPersistence().acquireUnitOfWork();
    // uow.registerNewObject(p_company);
    //
    // uow.commit();
    // } else //it is a duplicate
    // {
    // String errorArgs[] = {p_company.getCompanyName()};
    // throw new JobException(JobException.MSG_COMPANY_ALREADY_EXISTS,
    // errorArgs, null);
    // }
    // } catch (JobException je)
    // {
    // // just throw it as is
    // throw je;
    // } catch (Exception e)
    // {
    // String args[] = new String[1];
    // args[0] = p_company.getCompanyName();
    // c_category.error("JobHandlerLocal::createCompany", e);
    // throw new JobException(JobException.MSG_FAILED_TO_CREATE_COMPANY,
    // args, e);
    // }
    //
    // return p_company;
    // }

    public void createCategory(Category category) throws JobException
    {
        try
        {
            HibernateUtil.save(category);
        }
        catch (Exception e)
        {
            String[] arg = new String[1];
            arg[0] = category.getCategory();
            throw new JobException(
                    JobException.MSG_FAILED_TO_CREATE_COMPANY_CATEGORY, arg, e);
        }
    }

    public void createScorecardCategory(ScorecardCategory scorecardCategory)
            throws JobException
    {
        try
        {
            HibernateUtil.save(scorecardCategory);
        }
        catch (Exception e)
        {
            String[] arg = new String[1];
            arg[0] = scorecardCategory.getScorecardCategory();
            throw new JobException(
                    JobException.MSG_FAILED_TO_CREATE_COMPANY_CATEGORY, arg, e);
        }
    }
    
    public void createPostReviewCategory(
            PostReviewCategory postReviewCategory) throws JobException
    {
        try
        {
            HibernateUtil.save(postReviewCategory);
        }
        catch (Exception e)
        {
            String[] arg = new String[1];
            arg[0] = postReviewCategory.getCategoryName();
            throw new JobException(
                    JobException.MSG_FAILED_TO_CREATE_COMPANY_CATEGORY, arg, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Company createCompany(Company p_company, String p_userId)
            throws RemoteException, JobException
    {
        ITermbase tb = null;
        Company company = null;
        String exportLocation = "not exit";

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // uow = getPersistence().acquireUnitOfWork();

            // verify that its name is not a duplicate
            String hql = "select c.name from Company c where c.isActive = 'Y' and lower(c.name) = :name";
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", p_company.getName().toLowerCase());
            List<String> result = session.createQuery(hql)
                    .setString("name", p_company.getName().toLowerCase())
                    .list();

            if (result.size() == 0)
            {
                session.save(p_company);
                transaction.commit();
                transaction = session.beginTransaction();
                company = p_company;
                String companyId = Long.toString(company.getId());

                // Insert default termbase
                String definitionXml = "<definition><name>Sample</name><description>Sample Termbase</description><languages><language><name>English</name><locale>en</locale><hasterms>true</hasterms></language><language><name>French</name><locale>fr</locale><hasterms>true</hasterms></language><language><name>Spanish</name><locale>es</locale><hasterms>true</hasterms></language><language><name>German</name><locale>de</locale><hasterms>true</hasterms></language></languages><fields></fields></definition>";
                tb = (ITermbase) ServerProxy.getTermbaseManager().create(
                        p_userId, "", definitionXml, companyId);

                // Insert system parameters
                createDefSystemParamters(companyId, session);

                exportLocation = createExportLocation(company, session);
                new File(exportLocation).mkdirs();

                // Insert pivot currency
                createPivotCurrency(companyId, session);

                // Insert default calendar
                createDefaultCalendar(companyId, p_userId, session);

                // Insert default file extension
                createDefaultFileExtension(companyId, session);

                // Insert default permission groups
                createDefaultPermGroups(companyId, session);

                transaction.commit();

                // Copy properties files
                SystemConfiguration.copyPropertiesToCompany(company
                        .getCompanyName());

                // Must set dirty after the transaction committed
                ServerProxy.getSystemParameterPersistenceManager().setDirty();

                // Create TM storage. Use a dedicated connection for this
                // since session.connection() will leak. Note that this is
                // non-transactional.
                Connection conn = DbUtil.getConnection();
                conn.setAutoCommit(false);
                try
                {
                    createTmStorage(company.getId(), conn);
                    conn.commit();
                }
                finally
                {
                    DbUtil.returnConnection(conn);
                }
            }
            else
            // it is a duplicate
            {
                String errorArgs[] =
                { p_company.getCompanyName() };
                throw new JobException(JobException.MSG_COMPANY_ALREADY_EXISTS,
                        errorArgs, null);
            }
        }
        catch (JobException je)
        {
            throw je;
        }
        catch (Exception e)
        {
            if (transaction != null && company == null)
            {
                transaction.rollback();

                // delete the new created exportLocation file as possible
                File file = new File(exportLocation);
                if (file.exists() && (file.listFiles().length == 0))
                {
                    file.delete();
                }
            }
            if (tb != null)
            {
                try
                {
                    ServerProxy.getTermbaseManager().delete(tb.getName(),
                            p_userId, "");
                }
                catch (GeneralException e1)
                {
                    e1.printStackTrace();
                }
            }
            if (company != null)
            {
                removeCompany(company);
            }

            String args[] = new String[1];
            args[0] = (p_company == null ? "" : p_company.getCompanyName());
            c_category.error("JobHandlerLocal::createCompany", e);
            throw new JobException(JobException.MSG_FAILED_TO_CREATE_COMPANY,
                    args, e);
        }

        return p_company;
    }

    /**
     * @param companyId
     * @throws NamingException
     * @throws GeneralException
     * @throws RemoteException
     * @throws ExportLocationEntityException
     */
    private String createExportLocation(Company p_company, Session session)
            throws ExportLocationEntityException, RemoteException,
            GeneralException, NamingException
    {

        // create an export location
        Long id = new Long(CompanyWrapper.SUPER_COMPANY_ID);
        ExportLocation adminEl = (ExportLocation) session.get(
                ExportLocationImpl.class, id);

        ExportLocation el = new ExportLocationImpl();
        el.setName(adminEl.getName());
        el.setDescription(adminEl.getDescription());
        String location = adminEl.getLocation() + "/"
                + p_company.getCompanyName();
        el.setLocation(location);
        el.setCompanyId(p_company.getId());
        session.save(el);

        // create the default location of SystemParameter
        SystemParameter sp = ServerProxy.getSystemParameterPersistenceManager()
                .getAdminSystemParameter(
                        SystemConfigParamNames.DEFAULT_EXPORT_LOCATION);
        SystemParameter newSp = new SystemParameterImpl(sp.getName(),
                Long.toString(el.getId()), p_company.getId());
        session.save(newSp);
        return location;
    }

    /**
     * @param companyId
     * @throws RemoteException
     * @throws SystemParameterEntityException
     */
    private void createDefSystemParamters(String p_companyId, Session session)
            throws SystemParameterEntityException, RemoteException
    {
        SystemParameterPersistenceManager spManager = ServerProxy
                .getSystemParameterPersistenceManager();
        String companyName = CompanyWrapper.getCompanyNameById(p_companyId);
        String[] sysParamsNames = ConfigMainHandler.getParams();
        String[] sysParamsNamesSuff = ConfigMainHandler
                .getCompanySuffixedParams();
        List<String> ignoredSysParams = ConfigMainHandler.getIgnoredSysParams();

        // Copy super system params
        for (int i = 0; i < sysParamsNames.length; i++)
        {
            String spName = sysParamsNames[i];
            // Ignore some system parameters, which is only used in super
            // company.
            if (ignoredSysParams.contains(spName))
            {
                continue;
            }
            String spValue = spManager.getAdminSystemParameter(spName)
                    .getValue();
            SystemParameter sp = new SystemParameterImpl(spName, spValue,
                    Long.parseLong(p_companyId));
            session.save(sp);
        }

        // Add system params like %superParam%\companyName
        for (int i = 0; i < sysParamsNamesSuff.length; i++)
        {
            String value = spManager.getAdminSystemParameter(
                    sysParamsNamesSuff[i]).getValue();
            SystemParameter sp = new SystemParameterImpl(sysParamsNamesSuff[i],
                    value + "/" + companyName, Long.parseLong(p_companyId));
            session.save(sp);
        }
    }

    @SuppressWarnings("unchecked")
    private void createPivotCurrency(String p_companyId, Session session)
            throws GeneralException, RemoteException
    {
        // get the pivot currency
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String pivotCurCode = sc
                .getStringParameter(SystemConfigParamNames.PIVOT_CURRENCY);

        String hql = "from IsoCurrency i where i.code = :code";
        List<IsoCurrency> result = session.createQuery(hql)
                .setString("code", pivotCurCode).list();

        Currency pivot = new Currency((IsoCurrency) result.get(0), 1,
                Long.parseLong(p_companyId));
        session.save(pivot);

        Currency.addPivotCurdrency(pivot);
    }

    private void createDefaultCalendar(String p_companyId, String userId,
            Session session) throws PersistenceException, Exception
    {
        FluxCalendar defCal = null;

        // Init base info
        FluxCalendar baseCal = (FluxCalendar) session.get(FluxCalendar.class,
                new Long(1));
        defCal = new FluxCalendar("Base Calendar", baseCal.getTimeZoneId(),
                true, 8);
        // defCal.setLastUpdatedBy(userId);
        // defCal.setLastUpdatedTime(new Date());
        defCal.setCompanyId(Long.parseLong(p_companyId));

        // Set holidaies
        Holiday christmas = new Holiday("Christmas Day", null, 25, 0, 0, true,
                11, null, p_companyId);
        christmas.setTimeExpression("0 0 0 0 25 11");
        session.save(christmas);
        defCal.addHoliday(christmas);

        Holiday newYear = new Holiday("New Year's Day", null, 1, 0, 0, true, 0,
                null, p_companyId);
        newYear.setTimeExpression("0 0 0 0 1 0");
        session.save(newYear);
        defCal.addHoliday(newYear);

        // Set working day and working hour.
        CalendarWorkingDay workingDay = null;
        WorkingHour workingHour = null;
        for (int i = 2; i < 7; i++)
        {
            workingDay = new CalendarWorkingDay(i);
            workingHour = new CalendarWorkingHour(1, 8, 0, 12, 0);
            workingDay.addWorkingHour(workingHour, defCal.getTimeZone());
            workingHour = new CalendarWorkingHour(2, 13, 0, 17, 0);
            workingDay.addWorkingHour(workingHour, defCal.getTimeZone());
            defCal.addWorkingDay(workingDay);
        }

        session.save(defCal);
    }

    private void createDefaultFileExtension(String p_companyId, Session session)
            throws PersistenceException, Exception
    {
        String[] extensions = FileProfileConstants.extensions;

        for (int i = 0; i < extensions.length; i++)
        {
            session.save(new FileExtensionImpl(extensions[i], p_companyId));
        }
    }

    private void createDefaultPermGroups(String p_companyId, Session session)
            throws PersistenceException, Exception
    {
        long companyId = Long.parseLong(p_companyId);
        PermissionGroup permGroup = null;

        permGroup = new PermissionGroupImpl();
        permGroup.setName("Administrator");
        permGroup.setDescription("Default Administrator Group");
        permGroup
                .setPermissionSet("|3|4|5|6|7|8|9|13|14|17|18|19|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|"
                        + "51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|70|71|72|73|74|75|76|77|78|79|80|82|83|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|"
                        + "101|102|103|104|117|118|119|120|121|123|124|125|126|127|128|130|131|132|133|134|135|136|137|138|139|140|142|143|144|145|146|147|148|149|150|"
                        + "151|152|154|155|156|157|158|159|160|162|163|164|165|166|167|168|170|171|172|173|174|188|190|191|192|193|194|195|196|197|200|"
                        + "201|202|203|204|205|206|208|209|210|212|213|214|215|216|217|218|219|220|221|223|224|227|228|229|230|235|244|247|"
                        + "255|256|257|258|259|260|262|263|264|265|266|267|268|269|270|291|292|293|294|295|296|297|298|299|"
                        + "321|322|330|362|365|367|368|369|371|375|378|379|380|381|382|383|385|388|389|390|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("ProjectManager");
        permGroup.setDescription("Default Project Manager Group");
        permGroup
                .setPermissionSet("|13|14|17|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|"
                        + "51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|70|71|72|73|75|76|77|78|79|80|82|83|89|90|91|92|"
                        + "104|127|128|129|130|131|132|133|134|135|136|137|138|139|140|142|143|144|145|146|147|148|149|150|"
                        + "151|152|154|155|156|157|158|159|160|163|164|165|166|167|168|169|170|171|172|173|174|188|190|191|192|194|195|196|198|199|200|"
                        + "201|202|203|204|205|206|208|214|218|219|220|221|223|224|225|226|227|228|229|230|236|237|238|239|240|242|243|245|246|247|248|"
                        + "252|253|254|255|256|259|261|270|362|367|368|385|388|389|390|393|394|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("WorkflowManager");
        permGroup.setDescription("Default Workflow Manager Group");
        permGroup
                .setPermissionSet("|14|20|21|22|23|24|25|26|27|28|29|30|31|"
                        + "32|86|87|88|128|130|132|133|134|135|136|137|138|140|141|142|"
                        + "143|144|145|146|147|148|149|150|151|152|153|154|155|156|157|"
                        + "158|159|160|161|162|163|164|165|166|167|169|170|171|172|173|"
                        + "187|192|198|199|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("LocaleManager");
        permGroup.setDescription("Default Locale Manager Group");
        permGroup
                .setPermissionSet("|163|164|166|167|168|169|170|171|172|173|187|199|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("LocalizationParticipant");
        permGroup.setDescription("Default Localization Participant Group");
        permGroup
                .setPermissionSet("|163|164|165|166|167|168|169|170|171|172|173|174|195|199|200|"
                        + "201|225|226|245|246|253|254|261|273|283|285|291|"
                        + "324|325|326|327|361|362|363|364|370|373|374|386|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("Customer");
        permGroup.setDescription("Default Customer Group");
        permGroup.setPermissionSet("|14|37|38|40|128|130|131|132|133|134|135|"
                + "136|137|138|139|140|142|143|144|145|146|147|148|"
                + "149|150|151|154|155|156|157|158|159|160|162|"
                + "174|188|192|193|199|205|368|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("VendorAdmin");
        permGroup.setDescription("Default VendorAdmin Group");
        permGroup
                .setPermissionSet("|37|38|39|40|41|177|178|179|180|181|182|183|184|185|186|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("VendorManager");
        permGroup.setDescription("Default VendorManager Group");
        permGroup
                .setPermissionSet("|37|38|39|40|41|177|178|179|180|181|183|184|185|186|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("VendorViewer");
        permGroup.setDescription("Default VendorViewer Group");
        permGroup.setPermissionSet("|178|181|184|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("EngineerDtp");
        permGroup.setDescription("Default Engineer DTP Group");
        permGroup
                .setPermissionSet("|163|164|167|168|169|170|171|172|173|174|195|199|225|226|253|254|261|273|283|285|291|324|325|326|327|361|362|364|370|373|374|386|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("Reviewer");
        permGroup.setDescription("Default Reviewer Group");
        permGroup
                .setPermissionSet("|163|164|167|168|169|170|171|172|173|174|195|199|225|226|253|254|258|261|273|283|285|291|324|325|326|327|361|362|364|370|373|374|386|");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("JobCreator");
        permGroup.setDescription("Default Job Creator Group");
        permGroup.setPermissionSet("||");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("TechnicalSupport");
        permGroup.setDescription("Default Technical Support Group");
        permGroup.setPermissionSet("||");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);

        permGroup = new PermissionGroupImpl();
        permGroup.setName("ApiConnector");
        permGroup.setDescription("Default API Connector Group");
        permGroup.setPermissionSet("||");
        permGroup.setCompanyId(companyId);
        session.save(permGroup);
    }

    /**
     * Get the job object specified by its job id.
     * 
     * @param p_jobId
     *            The specified job id.
     * @return The job object.
     */
    public Job getJobById(long p_jobId) throws RemoteException, JobException
    {
        Job result = null;

        try
        {
            result = (JobImpl) HibernateUtil.get(JobImpl.class, p_jobId);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobById", e);
            String[] args = new String[1];
            args[0] = new Long(p_jobId).toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_ID,
                    args, e);
        }

        return result;
    }

    public Job refreshJob(Job job) throws JobException
    {
        try
        {
            return getJobById(job.getId());
        }
        catch (Exception e)
        {
            throw new JobException(e);
        }
    }

    /**
     * Gets a collection of Jobs based on the search params
     * 
     * @param p_searchParameters
     * @return Collection of jobs
     * @exception RemoteException
     * @exception JobException
     */
    public Collection getJobs(JobSearchParameters p_searchParameters)
            throws RemoteException, JobException
    {
        try
        {
            return new JobSearchCriteria().search(p_searchParameters);
        }
        catch (Exception e)
        {
            c_category.error("Failed to get jobs by criteria.", e);
            throw new JobException(e);
        }
    }

    /**
     * Get a list of job objects based on a particular state(such as: 'ready',
     * 'in progresss' and 'completed').
     * 
     * @param p_state
     *            - The state of the job.
     * @return A vector of all jobs in that state.
     * @throws JobException
     *             Component related exception.
     * @throws java.rmi.RemoteException
     *             Network related exception.
     */
    public Collection<JobImpl> getJobsByState(String p_state)
            throws RemoteException, JobException
    {
        return getJobsByStateList(argList(p_state));
    }

    /**
     * Get a list of job objects based on rate
     * 
     * @return A vector of all jobs
     * @throws JobException
     *             Component related exception.
     * @throws java.rmi.RemoteException
     *             Network related exception.
     */
    public Collection<JobImpl> getJobsByRate(String p_rateId)
            throws RemoteException, JobException
    {
        Collection<JobImpl> results = null;
        m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();

        try
        {
            String sql = jobsByRateQuery(p_rateId);
            results = HibernateUtil.searchWithSql(sql, null, JobImpl.class);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByStateList", e);
            String[] args = new String[1];
            args[0] = p_rateId;
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }

        return results;
    }

    /**
     * Get a list of job objects based on a list of states.
     * 
     * @param p_listOfStates
     *            a vector of strings, each of which is a possible state
     * @return A vector of all jobs in that state.
     * @throws JobException
     *             Component related exception.
     * @throws java.rmi.RemoteException
     *             Network related exception.
     */
    public Collection<JobImpl> getJobsByStateList(Vector<String> p_listOfStates)
            throws RemoteException, JobException
    {
        Collection<JobImpl> results = null;

        m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();
        try
        {
            String sql = jobsByStateListQuery(p_listOfStates);
            results = HibernateUtil.searchWithSql(JobImpl.class, sql);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByStateList", e);
            String[] args = new String[1];
            args[0] = p_listOfStates.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }

        return results;
    }

    /**
     * @see JobHandler.getJobsByStateList(Vector, boolean)
     */
    public Collection<JobImpl> getJobsByStateList(
            Vector<String> p_listOfStates, boolean p_queryLimitByDate)
            throws RemoteException, JobException
    {
        m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();
        if (!p_queryLimitByDate || m_myJobsDaysRetrieved <= 0)
        {
            return getJobsByStateList(p_listOfStates);
        }

        try
        {
            String sql = jobsByStateListQuery(p_listOfStates,
                    p_queryLimitByDate);

            return HibernateUtil.searchWithSql(JobImpl.class, sql,
                    getQueryDate().getDate());
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByStateList", e);
            String[] args = new String[1];
            args[0] = p_listOfStates.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }
    }

    public Collection<JobImpl> getJobsByStateList(String p_httpSessionId,
            Vector<String> p_listOfStates) throws RemoteException, JobException
    {
        Collection<JobImpl> results = null;
        m_size = SystemConfiguration.getInstance().getIntParameter(
                SystemConfigParamNames.RECORDS_PER_PAGE_JOBS);
        m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();

        try
        {
            JobResultSetCursor jrsc = (JobResultSetCursor) m_jobCursorList
                    .get(p_httpSessionId);
            if (jrsc == null)
            {
                String sql = jobsByStateListQuery(p_listOfStates);
                List jobs = HibernateUtil.searchWithSql(JobImpl.class, sql);
                jrsc = new JobResultSetCursor(m_size);
                jrsc.setScrollableCursor(jobs);
                m_jobCursorList.put(p_httpSessionId, jrsc);
                results = jrsc.getJobCollection();
            }
            else
            {
                if (jrsc.isLast())
                {
                    results = jrsc.getJobCollection();
                    m_jobCursorList.remove(p_httpSessionId);
                }
                else
                {
                    results = jrsc.getJobCollection();
                }
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByStateList", e);
            String[] args = new String[1];
            args[0] = p_listOfStates.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public Collection getJobsByManagerId(String p_managerId)
            throws RemoteException, JobException
    {
        try
        {
            String hql = "select r.job from RequestImpl r where r.l10nProfile.project.managerUserId = :uId";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uId", p_managerId);

            return HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobsByManagerId", e);
            String[] args = new String[1];
            args[0] = p_managerId;
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_STATE_AND_PROJECT_MANAGER,
                    args, e);
        }
    }

    public Collection<JobImpl> getJobsByManagerIdAndState(String p_managerId,
            String p_state) throws RemoteException, JobException
    {
        return getJobsByManagerIdAndStateList(p_managerId, argList(p_state));
    }

    public Collection<JobImpl> getJobsByManagerIdAndStateList(
            String p_managerId, Vector<String> p_listOfStates)
            throws RemoteException, JobException
    {
        Collection<JobImpl> results = null;

        try
        {
            m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();

            String sql = jobsByManagerAndStateListQuery(p_listOfStates);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(MANAGER_ID_ARG, p_managerId);
            if (m_myJobsDaysRetrieved > 0)
            {
                map.put(JOB_CREATE_DATE, getQueryDate().getDate());
            }
            results = HibernateUtil.searchWithSql(sql, map, JobImpl.class);

            c_category.debug("The value of manager id is " + p_managerId);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobsByManagerIdAndStateList", e);
            String[] args = new String[2];
            args[0] = p_managerId;
            args[1] = p_listOfStates.toString();
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_STATE_AND_PROJECT_MANAGER,
                    args, e);
        }

        return results;
    }

    /**
     * To make search result from My Jobs menu be same as search page, return 0
     * directly
     * 
     * @return 0
     */
    private int getMyJobsDaysRetrieved()
    {
        return 0;
        /*
         * return SystemConfiguration.getInstance().getIntParameter(
         * SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED);
         */
    }

    /**
     * Get a collection of Jobs for a Workflow Manager (who assists PM for
     * workflow modifications.
     * 
     * @param p_wfManagerId
     *            - The user name of the workflow manager.
     * @param p_listOfStates
     *            - A list of job states for query purposes.
     * @return A collection of jobs where the workflow manager can perform
     *         actions on behalf of the PM (only on Workflows that they are
     *         assigned to).
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Collection getJobsByWfManagerIdAndStateList(String p_wfManagerId,
            Vector p_listOfStates) throws RemoteException, JobException
    {
        Collection results = null;

        try
        {
            m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();

            String sql = jobsByWfManagerAndStateListQuery(p_listOfStates);
            Map map = new HashMap();
            map.put(MANAGER_ID_ARG, p_wfManagerId);
            if (m_myJobsDaysRetrieved > 0)
            {
                map.put(JOB_CREATE_DATE, getQueryDate().getDate());
            }
            results = HibernateUtil.searchWithSql(sql, map, JobImpl.class);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobsByWfManagerIdAndStateList",
                    e);
            String[] args = new String[2];
            args[0] = p_wfManagerId;
            args[1] = p_listOfStates.toString();
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_STATE_AND_PROJECT_MANAGER,
                    args, e);
        }

        return results;
    }

    /**
     * @deprecated use getJobsByManagerIdAndState(String, String) instead.
     */
    public Collection getJobsByProjectManager(String p_userId, String p_state)
            throws RemoteException, JobException
    {
        return getJobsByManagerIdAndState(p_userId, p_state);
    }

    public L10nProfile getL10nProfileByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        L10nProfile l10nProfile = null;

        try
        {
            Vector args = new Vector();
            args.add(Long.toString(p_jobId));

            c_category.debug("The job id is " + p_jobId);

            String sql = L10nProfileDescriptorModifier.L10N_PROFILE_FOR_JOB_ID;
            Map map = new HashMap();
            map.put("jobId", new Long(p_jobId));

            Iterator it = HibernateUtil.searchWithSql(sql, map,
                    BasicL10nProfile.class).iterator();
            if (it.hasNext())
                l10nProfile = (L10nProfile) it.next();

            c_category.debug("The l10n profile is " + l10nProfile);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::L10nProfileByJobId", e);
            throw new JobException(e);
        }

        return l10nProfile;
    }

    public Collection getSourcePageByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        ArrayList sourcePages = null;
        try
        {
            String sql = SourcePageDescriptorModifier.SOURCE_PAGE_BY_JOB_ID;
            Map map = new HashMap();
            map.put("jobId", new Long(p_jobId));
            sourcePages = new ArrayList(HibernateUtil.searchWithSql(sql, map,
                    SourcePage.class));

            // this does sort the pages according to page name by the US
            // locale sorting order.
            // If we ever need to support user locale specific sorting for this
            // later,
            // that can be added in the UI if the user's lang is not english
            Comparator comparator = new PageComparator(
                    PageComparator.EXTERNAL_PAGE_ID, Locale.US);
            SortUtil.sort(sourcePages, comparator);

        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getSourcePageWordCount" + e);
            throw new JobException(e);
        }

        return sourcePages;
    }

    /**
     * @see JobHandler.getSourcePageByTypeAndJobId(int, long)
     */
    public Collection getSourcePagesByTypeAndJobId(int p_primaryFileType,
            long p_jobId) throws RemoteException, JobException
    {
        // must call a query to get these rather than just get from the object.
        // some of the relationships between the objects have been broken
        // i.e. Request to SourcePage - so can't retrieve them this way
        ArrayList sourcePages = null;
        try
        {
            String sql = null;
            Map map = new HashMap();
            map.put("jobId", new Long(p_jobId));

            switch (p_primaryFileType)
            {
                case PrimaryFile.UNEXTRACTED_FILE:
                    sql = SourcePageDescriptorModifier.UNEXTRACTED_SOURCE_PAGES_BY_JOB_ID_SQL;
                    break;
                case PrimaryFile.EXTRACTED_FILE:
                default:
                    sql = SourcePageDescriptorModifier.EXTRACTED_SOURCE_PAGES_BY_JOB_ID_SQL;
                    break;
            }

            sourcePages = new ArrayList(HibernateUtil.searchWithSql(sql, map,
                    SourcePage.class));
        }
        catch (Exception e)
        {
            c_category.error("Failed to get the source pages of type "
                    + p_primaryFileType + " that are associated with job "
                    + p_jobId);
            throw new JobException(e);
        }

        // this does sort the pages according to page name by the US
        // locale sorting order.
        // If we ever need to support user locale specific sorting for this
        // later,
        // that can be added in the UI if the user's lang is not english
        Comparator comparator = new PageComparator(
                PageComparator.EXTERNAL_PAGE_ID, Locale.US);
        SortUtil.sort(sourcePages, comparator);

        return sourcePages;
    }

    /**
     * Return all the requests associated with the specified job.
     */
    public Collection getRequestListByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        Collection requestList = null;
        try
        {
            String sql = RequestDescriptorModifier.REQUEST_LIST_BY_JOB_ID;
            Map map = new HashMap();
            map.put("jobId", new Long(p_jobId));
            requestList = new ArrayList(HibernateUtil.searchWithSql(sql, map,
                    RequestImpl.class));
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getRequestListByJobId" + e);
            throw new JobException(e);
        }

        return requestList;
    }

    /**
     * Find the job that contains the specific source page.
     * 
     * @param p_sourcePageId
     *            - the unique identifier of the source page.
     * 
     * @return Either the job that contains that source page or NULL if a job
     *         doesn't contain that page.
     */
    public Job findJobOfPage(long p_sourcePageId) throws RemoteException,
            JobException
    {
        Job result = null;

        try
        {
            String sql = "select r.job from RequestImpl r where r.sourcePageId = :sId";
            Map map = new HashMap();
            map.put("sId", new Long(p_sourcePageId));
            Iterator iJobs = HibernateUtil.searchWithSql(sql, map,
                    RequestImpl.class).iterator();

            // if there is one return it
            if (iJobs.hasNext())
            {
                result = (Job) iJobs.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::findJobOfPage ", e);
            String[] args =
            { Long.toString(p_sourcePageId) };
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_JOB_BY_SOURCE_PAGE_ID, args,
                    e);
        }

        return result;
    }

    /**
     * @see JobHandler.updatePageCount(Job, int)
     */
    public Job updatePageCount(Job p_job, int p_pageCount)
            throws RemoteException, JobException
    {
        return JobPersistenceAccessor.updatePageCount(p_job, p_pageCount);
    }

    /**
     * @see JobHandler.updateQuoteDate(Job, String)
     */
    public void updateQuoteDate(Job p_job, String p_quoteDate)
            throws RemoteException, JobException
    {
        JobPersistenceAccessor.updateQuoteDate(p_job, p_quoteDate);
    }

    /**
     * Update the date of Approved quote in a job.
     * 
     * @param p_job
     *            The job to update the Approved Quotedate of.
     * @param p_quoteApprovedDate
     *            The new date of Approved Quote.
     */
    public void updateQuoteApprovedDate(Job p_job, String p_quoteApprovedDate)
            throws RemoteException, JobException
    {
        JobPersistenceAccessor.updateQuoteApprovedDate(p_job,
                p_quoteApprovedDate);

    }

    /**
     * Update the PO Number of quote in a job.
     * 
     * @param p_job
     *            The job to update the PO Number of Quote.
     * @param p_quotePoNumber
     *            The PO Number of Quote.
     */
    public void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
            throws RemoteException, JobException
    {
        JobPersistenceAccessor.updateQuotePoNumber(p_job, p_quotePoNumber);

    }

    /**
     * @see JobHandler.updateAuthoriserUser(Job, User)
     */
    public void updateAuthoriserUser(Job p_job, User user)
            throws RemoteException, JobException
    {
        JobPersistenceAccessor.updateAuthoriserUser(p_job, user);
    }

    /**
     * @see JobHandler.overrideWordCount(Job, int)
     */
    public Job overrideWordCount(Job p_job, int p_wordCount)
            throws RemoteException, JobException
    {
        return JobPersistenceAccessor.overrideWordCount(p_job, p_wordCount);
    }

    /**
     * @see JobHandler.clearOverridenWordCount(Job)
     */
    public Job clearOverridenWordCount(Job p_job) throws RemoteException,
            JobException
    {
        return JobPersistenceAccessor.clearOverridenWordCount(p_job);
    }

    /**
     * Create a new activity.
     * 
     * @param p_actvity
     *            - The activity to be created.
     * 
     * @return The created activity - with id populated.
     * @throws java.rmi.RemoteException
     *             Network related exception.
     * @throws JobException
     *             Component related exception.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public Activity createActivity(Activity p_activity) throws RemoteException,
            JobException
    {
        try
        {
            // verify that its name is not a duplicate
            String hql = "from Activity a where a.isActive = 'Y' and lower(a.name) = :name";
            Map map = new HashMap();
            map.put("name", p_activity.getName().toLowerCase());

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            Collection activities = HibernateUtil.search(hql, map);

            // if none return than not a duplicate
            if (activities == null || activities.size() <= 0)
            {
                // the roles need the activity seqence number so need to
                // assign one before commiting - since all part of one
                // transaction
                HibernateUtil.save(p_activity);
                createRolesForActivity(p_activity);
            }
            else
            // it is a duplicate
            {
                String errorArgs[] =
                { p_activity.getActivityName() };
                throw new JobException(
                        JobException.MSG_ACTIVITY_ALREADY_EXISTS, errorArgs,
                        null);
            }
        }
        catch (JobException je)
        {
            // just throw it as is
            throw je;
        }
        catch (Exception e)
        {
            removeRolesForActivity(p_activity);

            String args[] = new String[1];
            args[0] = p_activity.getActivityName();
            c_category.error("JobHandlerLocal::createActivity", e);
            throw new JobException(JobException.MSG_FAILED_TO_CREATE_ACTIVITY,
                    args, e);
        }

        return p_activity;
    }

    /**
     * Modify the description and/or type on the given activity.
     * 
     * @param p_activity
     *            - The activity to be updated.
     * 
     * @throws java.rmi.RemoteException
     *             Network related exception.
     * @throws JobException
     *             Component related exception.
     */
    public void modifyActivity(Activity p_activity) throws RemoteException,
            JobException
    {
        try
        {
            Activity clone = (Activity) HibernateUtil.get(Activity.class,
                    p_activity.getId());
            clone.setDescription(p_activity.getDescription());
            clone.setType(p_activity.getType());
            clone.setIsEditable(p_activity.getIsEditable());
            clone.setQaChecks(p_activity.getQaChecks());
            clone.setRunDitaQAChecks(p_activity.getRunDitaQAChecks());
            clone.setAutoCompleteActivity(p_activity.getAutoCompleteActivity());
            clone.setAfterJobCreation(p_activity.getAfterJobCreation());
            clone.setAfterJobDispatch(p_activity.getAfterJobDispatch());
            clone.setAfterActivityStart(p_activity.getAfterActivityStart());
            HibernateUtil.update(clone);
        }
        catch (Exception pe)
        {
            c_category.error("JobHandlerLocal::modifyActivity", pe);
            String args[] = new String[1];
            args[0] = p_activity.getActivityName();
            throw new JobException(JobException.MSG_FAILED_TO_MODIFY_ACTIVITY,
                    args, pe);
        }
    }

    /**
     * Remove the given activity.
     * 
     * @param p_activity
     *            - The activity to be removed.
     * 
     * @throws java.rmi.RemoteException
     *             Network related exception.
     * @throws JobException
     *             Component related exception.
     */
    public void removeActivity(Activity p_activity) throws RemoteException,
            JobException
    {
        try
        {
            HibernateUtil.delete(p_activity);
            removeRolesForActivity(p_activity);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::removeActivity", e);
            String args[] = new String[1];
            args[0] = p_activity.getActivityName();
            throw new JobException(JobException.MSG_FAILED_TO_REMOVE_ACTIVITY,
                    args, e);
        }
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Return the Job Dispatch Engine */
    private JobDispatchEngine getJobDispatchEngine() throws JobException
    {
        try
        {
            return ServerProxy.getJobDispatchEngine();
        }
        catch (Exception e)
        {
            c_category.error("Unable to retrieve Job Dispatching Engine", e);
            throw new JobException(
                    JobException.MSG_FAILED_TO_FIND_DISPATCH_ENGINE, null, e);
        }
    }

    /* Remove all non-Localization requests from the given job */
    private int removeErrorRequests(long p_jobId) throws JobException
    {
        Connection conn = null;
        int numDeletedRequests = 0;

        try
        {
            JobCreationQuery cmd1 = new JobCreationQuery();

            List requests = cmd1.getRequestListByJobId(p_jobId);

            ArrayList errorRequests = new ArrayList();

            // Build a list of error requests that need to be removed.
            for (Iterator it = requests.iterator(); it.hasNext();)
            {
                Request r = (Request) it.next();

                // if it is an error (negative number)
                if (r.getType() < 0)
                {
                    errorRequests.add(r);
                }
            }

            numDeletedRequests = errorRequests.size();
            if (numDeletedRequests == 0)
            {
                return 0;
            }
            // Delete the error requests and their source pages.
            RemoveRequestFromJobCommand cmd2 = new RemoveRequestFromJobCommand(
                    p_jobId, errorRequests);

            conn = getPersistence().getConnection();
            conn.setAutoCommit(false);

            cmd2.persistObjects(conn);

            conn.commit();
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ignore)
            {
            }

            c_category.error(
                    "Couldn't remove the requests and source pages from job "
                            + p_jobId, e);
            String[] args =
            { Long.toString(p_jobId) };
            throw new JobException(
                    JobException.MSG_FAILED_TO_REMOVE_ERROR_REQUEST, args, e);
        }
        finally
        {
            try
            {
                getPersistence().returnConnection(conn);
            }
            catch (Exception ignore)
            {
            }
        }
        return numDeletedRequests;
    }

    /* Create all the roles associated with the given activity. */
    private void createRolesForActivity(Activity p_activity)
            throws JobException
    {
        try
        {
            Iterator it = getLocaleManager().getSourceTargetLocalePairs()
                    .iterator();
            while (it.hasNext())
            {
                LocalePair lp = (LocalePair) it.next();

                createRole(p_activity, lp.getSource(), lp.getTarget());
            }
        }
        catch (Exception e)
        {
            throwRoleException(
                    JobException.MSG_FAILED_TO_CREATE_ROLE_FOR_ACTIVITY,
                    p_activity.getActivityName(), e);
        }
    }

    /* Remove all the roles associated with the given activity. */
    private void removeRolesForActivity(Activity p_activity)
            throws JobException
    {
        try
        {
            Iterator it = getLocaleManager().getSourceTargetLocalePairs()
                    .iterator();
            while (it.hasNext())
            {
                LocalePair lp = (LocalePair) it.next();

                removeRole(p_activity, lp.getSource(), lp.getTarget());
            }
        }
        catch (Exception e)
        {
            throwRoleException(
                    JobException.MSG_FAILED_TO_REMOVE_ROLE_FOR_ACTIVITY,
                    p_activity.getActivityName(), e);
        }
    }

    private void createTmStorage(long companyId, Connection conn)
    {
        DefaultManager.create().createStoragePool(conn, companyId,
                SegmentTmAttribute.inlineAttributes());
    }

    private void removeTmStorage(long companyId, Connection conn)
    {
        DefaultManager.create().removeStoragePool(conn, companyId);
    }

    /* Throw a JobException based on the given arguments. */
    private void throwRoleException(String p_msg, String p_activityName,
            Exception p_ex) throws JobException
    {
        c_category.error(p_msg, p_ex);
        String args[] = new String[1];
        args[0] = p_activityName;

        throw new JobException(p_msg, args, p_ex);
    }

    /* Create the role whose name is based on the given arguments */
    private void createRole(Activity p_activity, GlobalSightLocale p_source,
            GlobalSightLocale p_target) throws Exception
    {
        ContainerRole role = getUserManager().createContainerRole();

        role.setActivity(p_activity);
        role.setSourceLocale(p_source.toString());
        role.setTargetLocale(p_target.toString());

        getUserManager().addRole(role);
    }

    /* Remove the role whose name is based on the given arguments */
    private void removeRole(Activity p_activity, GlobalSightLocale p_source,
            GlobalSightLocale p_target) throws Exception
    {
        Role curRole = null;
        // remove all the container roles
        Collection cRoles = getUserManager().getContainerRoles(
                p_activity.getName(), p_source.toString(), p_target.toString());

        if (cRoles != null && cRoles.size() > 0)
        {
            Object roles[] = (Object[]) cRoles.toArray();

            for (int k = 0; k < roles.length; k++)
            {
                curRole = (Role) roles[k];
                // deactivate the rates that are part of the role before
                // removing the role
                // the rates are in database and the Roles are in LDAP - so the
                // dependancy
                // needs to be kept in the code.
                getCostingEngine().deleteRatesOnRole(curRole);
                getUserManager().removeRole(curRole);
            }
        }

        // remove all the user roles
        Collection uRoles = getUserManager().getUserRoles(p_activity.getName(),
                p_source.toString(), p_target.toString());

        if (uRoles != null && uRoles.size() > 0)
        {
            Object roles[] = (Object[]) uRoles.toArray();

            for (int j = 0; j < roles.length; j++)
            {
                curRole = (Role) roles[j];
                getUserManager().removeRole(curRole);
            }
        }
    }

    /* Return the workflow manager */
    private WorkflowManager getWorkflowManager() throws JobException
    {
        if (m_wfMgr == null)
        {
            try
            {
                m_wfMgr = ServerProxy.getWorkflowManager();
            }
            catch (Exception e)
            {
                c_category.error("Unable to retrieve Workflow Manager", e);
                throw new JobException(
                        JobException.MSG_WORKFLOWMANAGER_FAILURE, null, e);
            }
        }

        return m_wfMgr;
    }

    public static boolean containsAllErrors(Job p_job)
    {
        boolean isAllErrors = true;

        Iterator it = p_job.getRequestList().iterator();
        while (isAllErrors && it.hasNext())
        {
            Request r = (Request) it.next();

            // if the request is NOT an error - set the flag
            if (r.getType() > 0)
            {
                isAllErrors = false;
            }
        }

        return isAllErrors;
    }

    /* Return the locale manager */
    private LocaleManager getLocaleManager() throws JobException
    {
        if (m_locMgr == null)
        {
            try
            {
                m_locMgr = ServerProxy.getLocaleManager();
            }
            catch (Exception e)
            {
                c_category.error("Unable to retrieve Locale Manager", e);
                throw new JobException(
                        JobException.MSG_WORKFLOWMANAGER_FAILURE, null, e);
            }
        }

        return m_locMgr;
    }

    /* Return the user manager */
    private UserManager getUserManager() throws JobException
    {
        if (m_userMgr == null)
        {
            try
            {
                m_userMgr = ServerProxy.getUserManager();
            }
            catch (Exception e)
            {
                c_category.error("Unable to retrieve User Manager", e);
                throw new JobException(
                        JobException.MSG_WORKFLOWMANAGER_FAILURE, null, e);
            }
        }

        return m_userMgr;
    }

    /* Return the costing engine */
    private CostingEngine getCostingEngine() throws JobException
    {
        if (m_costing == null)
        {
            try
            {
                m_costing = ServerProxy.getCostingEngine();
            }
            catch (Exception e)
            {
                c_category.error("Unable to retrieve Costing Engine", e);
                throw new JobException(JobException.MSG_COSTING_ENGINE_FAILURE,
                        null, e);
            }
        }
        return m_costing;
    }

    /* Return the persistence service */
    private PersistenceService getPersistence() throws Exception
    {
        if (m_tlp == null)
        {
            m_tlp = PersistenceService.getInstance();
        }
        return m_tlp;
    }

    /**
     * Create and return a vector argument list containing the given string
     */
    private Vector<String> argList(String p_string)
    {
        return newVector(p_string);
    }

    /* Create and return a vector containing the given object */
    private <T> Vector<T> newVector(T p_object)
    {
        Vector<T> v = new Vector<T>();
        v.addElement(p_object);
        return v;
    }

    private String jobsByStateListQuery(Vector p_listOfStates)
    {
        return jobsByStateListQuery(p_listOfStates, false);
    }

    private String jobsByStateListQuery(Vector p_listOfStates,
            boolean p_queryLimitByDate)
    {

        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append("FROM JOB j");
        sb.append(" WHERE ");
        addStateClause(sb, p_listOfStates, "j");

        if (p_queryLimitByDate && m_myJobsDaysRetrieved > 0)
        {
            sb.append(" AND j.TIMESTAMP > ?");
        }

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            sb.append(" AND j.COMPANY_ID = ");
            sb.append(Long.parseLong(currentId));
        }

        c_category.debug("The query is " + sb.toString());

        return sb.toString();
    }

    private Vector getCompanyIdBoundArgs()
    {
        Vector arg = null;
        try
        {
            arg = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        return arg;
    }

    private String jobsByRateQuery(String p_rateId)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT * FROM JOB ");
        sb.append(" WHERE ID IN (");
        sb.append("               SELECT JOB_ID ");
        sb.append("               FROM WORKFLOW ");
        sb.append("               WHERE IFLOW_INSTANCE_ID IN (SELECT DISTINCT WORKFLOW_ID  ");
        sb.append("                                           FROM TASK_INFO ");
        sb.append("                                           WHERE ( REVENUE_RATE_ID = ");
        sb.append(p_rateId);
        sb.append("                                           OR    EXPENSE_RATE_ID = ");
        sb.append(p_rateId);
        sb.append("                                                         ))");
        sb.append("               AND WORKFLOW.STATE IN ('PENDING', 'DISPATCHED'))");

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            sb.append(" AND JOB.company_id = " + Long.parseLong(currentId));
        }

        c_category.debug("The query is " + sb.toString());
        return sb.toString();
    }

    private void addStateClause(StringBuffer p_sb, Vector p_listOfStates,
            String p_tableName)
    {
        p_sb.append(p_tableName);
        p_sb.append(".STATE in (");

        for (int i = 0; i < p_listOfStates.size(); i++)
        {
            p_sb.append("'");
            p_sb.append(p_listOfStates.elementAt(i));
            p_sb.append("'");

            if (i < p_listOfStates.size() - 1)
            {
                p_sb.append(", ");
            }
        }

        p_sb.append(")");
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: The following two queries should not go in the descriptor
    // modifier.
    // ////////////////////////////////////////////////////////////////////
    private String jobsByManagerAndStateListQuery(Vector p_listOfStates)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append(" FROM WORKFLOW wf, JOB j, REQUEST r, L10N_PROFILE l10n, PROJECT p");
        sb.append(" WHERE p.MANAGER_USER_ID = :");
        sb.append(MANAGER_ID_ARG);
        sb.append(" AND (");
        addStateClause(sb, p_listOfStates, "j");
        sb.append(" OR ");
        addStateClause(sb, p_listOfStates, "wf");
        if (p_listOfStates.contains(Job.DTPINPROGRESS))
        {
            sb.append(" OR (wf.STATE = '").append(Workflow.DISPATCHED)
                    .append("'").append(" and wf.TYPE = '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("'))");
        }
        else
        {
            sb.append(" AND wf.TYPE <> '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("')");
        }
        sb.append(" AND wf.JOB_ID = j.ID");
        sb.append(" AND r.JOB_ID = j.ID");
        sb.append(" AND l10n.ID = r.L10N_PROFILE_ID");
        sb.append(" AND p.PROJECT_SEQ = l10n.PROJECT_ID");
        if (m_myJobsDaysRetrieved > 0)
        {
            sb.append(" AND j.TIMESTAMP > :");
            sb.append(JOB_CREATE_DATE);
        }
        Vector arg = getCompanyIdBoundArgs();
        if (arg != null)
        {
            sb.append(" AND (( j.company_id >= ");
            sb.append(arg.get(0));
            sb.append(") and (j.company_id <= ");
            sb.append(arg.get(1));
            sb.append("))");
        }
        c_category.debug("The query is " + sb.toString());

        return sb.toString();
    }

    private String jobsByWfManagerAndStateListQuery(Vector p_listOfStates)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append(" FROM JOB j, WORKFLOW wf, WORKFLOW_OWNER wfo");
        sb.append(" WHERE wfo.OWNER_ID = :");
        sb.append(MANAGER_ID_ARG);
        sb.append(" AND wfo.OWNER_TYPE = '");
        sb.append(Permission.GROUP_WORKFLOW_MANAGER);
        sb.append("' AND (");
        addStateClause(sb, p_listOfStates, "j");
        sb.append(" OR ");
        addStateClause(sb, p_listOfStates, "wf");
        if (p_listOfStates.contains(Job.DTPINPROGRESS))
        {
            sb.append(" OR (wf.STATE = '").append(Workflow.DISPATCHED)
                    .append("'").append(" and wf.TYPE = '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("'))");
        }
        else
        {
            sb.append(" AND wf.TYPE <> '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("')");
        }
        // Don't query the cancelled workflows
        if (!p_listOfStates.contains(Workflow.CANCELLED))
        {
            sb.append(" AND wf.STATE <> '");
            sb.append(Workflow.CANCELLED);
            sb.append("'");
        }

        sb.append(" AND wf.JOB_ID = j.ID");
        sb.append(" AND wfo.WORKFLOW_ID = wf.IFLOW_INSTANCE_ID");
        if (m_myJobsDaysRetrieved > 0)
        {
            sb.append(" AND j.TIMESTAMP > :");
            sb.append(JOB_CREATE_DATE);
        }
        Vector arg = getCompanyIdBoundArgs();
        if (arg != null)
        {
            sb.append(" AND (( j.company_id >= ");
            sb.append(arg.get(0));
            sb.append(") and (j.company_id <= ");
            sb.append(arg.get(1));
            sb.append("))");
        }
        c_category.debug("The query is " + sb.toString());

        return sb.toString();
    }

    /*
     * Get the date for the my jobs UI query. This is used to get the most
     * recent jobs based on the number of days specified in envoy.properties
     * file.
     */
    private Timestamp getQueryDate()
    {
        Timestamp myDate = new Timestamp();
        myDate.add(Timestamp.DAY, (-1 * m_myJobsDaysRetrieved));

        return myDate;
    }

    /**
     * Get the job object specified by its job name.
     * 
     * @param p_jobName
     *            : The specified job name.
     * @return The job object
     * @throws RemoteException
     * @throws JobException
     */
    public Job getJobByJobName(String p_jobName) throws RemoteException,
            JobException
    {
        Job result = null;

        try
        {
            String hql = "from JobImpl j where j.jobName = :jobName";
            Map map = new HashMap();
            map.put("jobName", p_jobName);
            Iterator jobsIt = HibernateUtil.search(hql, map).iterator();
            while (jobsIt.hasNext())
            {
                result = (Job) jobsIt.next();
            }
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::getJobByJobName ", e);
            throw new JobException("FailedToFindJobByJobName", new String[]
            { p_jobName }, e);
        }

        return result;
    }

    /**
     * Get Jobs by userId and stateList
     * 
     * @param p_userId
     * @param p_listOfStates
     * @return
     * @throws RemoteException
     * @throws JobException
     */
    public Collection<JobImpl> getJobsByUserIdAndState(String p_userId,
            Vector<String> p_listOfStates) throws RemoteException, JobException
    {
        Collection<JobImpl> results = null;

        try
        {
            m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();

            String sql = getJobsByUserAndStateListQuery(p_userId,
                    p_listOfStates);
            Map<String, Object> map = new HashMap<String, Object>();
            if (m_myJobsDaysRetrieved > 0)
            {
                map.put(JOB_CREATE_DATE, getQueryDate().getDate());
            }
            results = HibernateUtil.searchWithSql(sql, map, JobImpl.class);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByUserStateList", e);
            String[] args = new String[1];
            args[0] = p_listOfStates.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }

        return results;
    }

    private String getJobsByUserAndStateListQuery(String userId,
            Vector p_listOfStates)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append(" FROM WORKFLOW wf, JOB j, REQUEST r, L10N_PROFILE l10n, PROJECT p");
        sb.append(" WHERE (");
        addStateClause(sb, p_listOfStates, "j");
        if (p_listOfStates.contains(Job.DTPINPROGRESS))
        {
            sb.append(" OR (wf.STATE = '").append(Workflow.DISPATCHED)
                    .append("'").append(" and wf.TYPE = '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("'))");
        }
        else
        {
            sb.append(" AND wf.TYPE <> '")
                    .append(WorkflowTypeConstants.TYPE_DTP).append("')");
        }
        sb.append(" AND wf.JOB_ID = j.ID");
        sb.append(" AND r.JOB_ID = j.ID");
        sb.append(" AND l10n.ID = r.L10N_PROFILE_ID");
        sb.append(" AND p.PROJECT_SEQ = l10n.PROJECT_ID");
        sb.append(" and p.PROJECT_SEQ in (" + getProjectStr(userId) + ") ");
        if (m_myJobsDaysRetrieved > 0)
        {
            sb.append(" AND j.TIMESTAMP > :");
            sb.append(JOB_CREATE_DATE);
        }
        Vector arg = getCompanyIdBoundArgs();
        if (arg != null)
        {
            sb.append(" AND (( j.company_id >= ");
            sb.append(arg.get(0));
            sb.append(") and (j.company_id <= ");
            sb.append(arg.get(1));
            sb.append("))");
        }

        c_category.debug("The query is " + sb.toString());

        return sb.toString();
    }

    /**
     * Get the projectsIdsStr by userId
     * 
     * @param userId
     * @return
     */
    private String getProjectStr(String userId)
    {
        List<Long> condition = new ArrayList<Long>();
        List allProjects = (List) ProjectHandlerHelper.getProjectByUser(userId);
        Iterator itAllProjects = allProjects.iterator();
        while (itAllProjects.hasNext())
        {
            Project project = (Project) itAllProjects.next();
            Set userIds = (Set) project.getUserIds();
            if (userIds.contains(userId))
            {
                condition.add(project.getId());
            }
        }
        String projectsIdsStr = convertList(condition);
        return projectsIdsStr;

    }

    private String convertList(List<?> list)
    {
        String result = "null";
        if (list == null || list.size() == 0)
        {
            return result;
        }
        for (int i = 0; i < list.size(); i++)
        {
            Object obj = list.get(i);
            result += "," + list.get(i) + "";
        }
        return result;
    }

	public String[] getJobIdsByCompany(String p_companyId, int p_offset,
			int p_count, boolean p_isDescOrder, String currentUserId)
			throws RemoteException, JobException
	{
        if (StringUtil.isEmpty(p_companyId))
            return null;
        if (p_offset < 1)
            p_offset = 0;
        p_count = p_count <= 0 ? 1 : p_count;

        Collection<JobImpl> results = null;
        String[] ids = null;

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT j.* FROM Job j ");
            //GBS-3987:fetchJobsPerCompany does not work correctly
            sb.append("  left outer join REQUEST r on j.ID = r.JOB_ID ");
            sb.append("  left outer join L10N_PROFILE l on j.L10N_PROFILE_ID = l.ID ");
            sb.append("  left outer join PROJECT p on l.PROJECT_ID = p.PROJECT_SEQ ");
            sb.append("  left outer join WORKFLOW w on j.ID=w.JOB_ID ");
            sb.append("  left outer join WORKFLOW_OWNER wo on w.IFLOW_INSTANCE_ID=wo.WORKFLOW_ID ");
            sb.append(" WHERE 1=1 ");
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(p_companyId))
            {
                sb.append(" AND j.COMPANY_ID = ");
                sb.append(Long.parseLong(p_companyId));
            }
			// GBS-3987:fetchJobsPerCompany does not work correctly
			if (currentUserId != null)
			{
				userExpression(currentUserId, sb);
			}
            sb.append(" ORDER BY j.ID");
            if (p_isDescOrder)
                sb.append(" DESC");
            else
                sb.append(" ASC");

            sb.append(" LIMIT ").append(p_offset).append(",").append(p_count);
            c_category.debug("The query is " + sb.toString());

            results = HibernateUtil.searchWithSql(JobImpl.class, sb.toString());

            if (results != null && results.size() > 0)
            {
                ids = new String[results.size()];
                int index = 0;
                for (JobImpl job : results)
                {
                    ids[index++] = String.valueOf(job.getId());
                }
            }

            return ids;
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
            throw new JobException(e);
        }
    }

	public String[] getJobIdsByState(String p_companyId, String p_state,
			int p_offset, int p_count, boolean p_isDescOrder,
			String currentUserId) throws RemoteException, JobException
	{
        if (StringUtil.isEmpty(p_companyId))
            return null;
        if (p_offset < 1)
            p_offset = 0;
        p_count = p_count <= 0 ? 1 : p_count;

        Collection<JobImpl> results = null;
        String[] ids = null;

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT j.* FROM Job j ");
            sb.append("  left outer join REQUEST r on j.ID = r.JOB_ID ");
            sb.append("  left outer join L10N_PROFILE l on j.L10N_PROFILE_ID = l.ID ");
            sb.append("  left outer join PROJECT p on l.PROJECT_ID = p.PROJECT_SEQ ");
            sb.append("  left outer join WORKFLOW w on j.ID=w.JOB_ID ");
            sb.append("  left outer join WORKFLOW_OWNER wo on w.IFLOW_INSTANCE_ID=wo.WORKFLOW_ID ");
            sb.append(" WHERE 1=1 ");
            if (!StringUtil.isEmpty(p_state))
            {
                sb.append(" AND j.STATE IN ('").append(p_state.toUpperCase())
                        .append("')");
            }
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(p_companyId))
            {
                sb.append(" AND j.COMPANY_ID = ");
                sb.append(Long.parseLong(p_companyId));
            }
        	// GBS-3987:fetchJobsPerCompany does not work correctly
			if (currentUserId != null)
			{
				userExpression(currentUserId, sb);
			}
			
            sb.append(" ORDER BY j.ID");
            if (p_isDescOrder)
                sb.append(" DESC");
            else
                sb.append(" ASC");

            sb.append(" LIMIT ").append(p_offset).append(",").append(p_count);
            c_category.debug("The query is " + sb.toString());

            results = HibernateUtil.searchWithSql(JobImpl.class, sb.toString());

            if (results != null && results.size() > 0)
            {
                ids = new String[results.size()];
                int index = 0;
                for (JobImpl job : results)
                {
                    ids[index++] = String.valueOf(job.getId());
                }
            }

            return ids;
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
            throw new JobException(e);
        }
    }

	public String[] getJobIdsByCreator(long p_companyId,
			String p_creatorUserId, int p_offset, int p_count,
			boolean p_isDescOrder, String currentUserId)
			throws RemoteException, JobException
	{
        if (p_offset < 1)
            p_offset = 0;
        p_count = p_count <= 0 ? 1 : p_count;

        Collection<JobImpl> results = null;
        String[] ids = null;

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT j.* FROM Job j ");
            //GBS-3987:fetchJobsPerCompany does not work correctly
            sb.append("  left outer join REQUEST r on j.ID = r.JOB_ID ");
            sb.append("  left outer join L10N_PROFILE l on j.L10N_PROFILE_ID = l.ID ");
            sb.append("  left outer join PROJECT p on l.PROJECT_ID = p.PROJECT_SEQ ");
            sb.append("  left outer join WORKFLOW w on j.ID=w.JOB_ID ");
            sb.append("  left outer join WORKFLOW_OWNER wo on w.IFLOW_INSTANCE_ID=wo.WORKFLOW_ID ");
            sb.append("WHERE 1=1");
            if (!StringUtil.isEmpty(p_creatorUserId))
            {
                sb.append(" AND j.CREATE_USER_ID = ('").append(p_creatorUserId)
                        .append("')");
            }
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(String
                    .valueOf(p_companyId)))
            {
                sb.append(" AND j.COMPANY_ID = ");
                sb.append(p_companyId);
            }
            //GBS-3987:fetchJobsPerCompany does not work correctly
			if (currentUserId != null)
			{
				userExpression(currentUserId, sb);
			}
            
            sb.append(" ORDER BY j.ID");
            if (p_isDescOrder)
                sb.append(" DESC");
            else
                sb.append(" ASC");

            sb.append(" LIMIT ").append(p_offset).append(",").append(p_count);
            c_category.debug("The query is " + sb.toString());

            results = HibernateUtil.searchWithSql(JobImpl.class, sb.toString());

            if (results != null && results.size() > 0)
            {
                ids = new String[results.size()];
                int index = 0;
                for (JobImpl job : results)
                {
                    ids[index++] = String.valueOf(job.getId());
                }
            }

            return ids;
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
            throw new JobException(e);
        }
    }

    private void userExpression(String currentUserId,StringBuilder sb)
    {
        PermissionSet perms = new PermissionSet();
        JobVoSearchCriteria vo = new JobVoSearchCriteria();
        try
        {
            perms = Permission.getPermissionManager().getPermissionSetForUser(
            		currentUserId);
        }
        catch (Exception e)
        {
        	c_category.error("Failed to get permissions for user " + currentUserId, e);
        }

        if (perms.getPermissionFor(Permission.JOB_SCOPE_ALL))
        {
            return;
        }
        else
        {
            if (perms.getPermissionFor(Permission.PROJECTS_MANAGE))
            {
            	sb.append(" and ( p.MANAGER_USER_ID ='").append(currentUserId).append("'");
                if (perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    sb.append(" or wo.OWNER_ID = '").append(currentUserId).append("'");
                }
                if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                {
                    List allProjectsIds = vo.getMyProjects(currentUserId);
                    String projectsIdsStr = convertList(allProjectsIds);
                    sb.append(" or l.PROJECT_ID in ("
                            + projectsIdsStr + ") ");
                }
                sb.append(")");
            }
            else
            {
                if (perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                	sb.append(" and ( wo.OWNER_ID = '").append(currentUserId).append("'");
                    if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                    {
                        List allProjectsIds = vo.getMyProjects(currentUserId);
                        String projectsIdsStr = convertList(allProjectsIds);
                        sb.append(" or l.PROJECT_ID in ("
                                + projectsIdsStr + ") ");
                    }
                    sb.append(")");
                }
                else
                {
                    if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                    {
                        List allProjectsIds = vo.getMyProjects(currentUserId);
                        String projectsIdsStr = convertList(allProjectsIds);
                        sb.append(" and ( l.PROJECT_ID in ("
                                + projectsIdsStr + ") ");
                        sb.append(")");
                    }
                    else
                    {
                        sb.append("and 1=2");
                    }
                }
            }
        }
    }
    
    public HashMap<String, Integer> getCountsByJobState(String p_companyId)
            throws RemoteException, JobException
    {
        if (StringUtil.isEmpty(p_companyId))
            return null;

        Collection results = null;
        HashMap<String, Integer> stateCounts = new HashMap<String, Integer>();
        stateCounts.put(Job.BATCHRESERVED, Integer.valueOf(0));
        stateCounts.put(Job.CANCELLED, Integer.valueOf(0));
        stateCounts.put(Job.DISPATCHED, Integer.valueOf(0));
        stateCounts.put(Job.DTPINPROGRESS, Integer.valueOf(0));
        stateCounts.put(Job.EXPORTED, Integer.valueOf(0));
        stateCounts.put(Job.EXPORT_FAIL, Integer.valueOf(0));
        stateCounts.put(Job.PENDING, Integer.valueOf(0));
        stateCounts.put(Job.IMPORTFAILED, Integer.valueOf(0));
        stateCounts.put(Job.LOCALIZED, Integer.valueOf(0));
        stateCounts.put(Job.READY_TO_BE_DISPATCHED, Integer.valueOf(0));
        stateCounts.put(Job.ADD_FILE, Integer.valueOf(0));
        stateCounts.put(Job.ARCHIVED, Integer.valueOf(0));

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String state = "";
        int count = 0;

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT j.state, COUNT(*) FROM Job j WHERE 1=1");
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(p_companyId))
            {
                sb.append(" AND j.COMPANY_ID = ");
                sb.append(Long.parseLong(p_companyId));
            }
            sb.append(" GROUP BY j.state");

            c_category.debug("The query is " + sb.toString());

            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sb.toString());
            while (rs.next())
            {
                state = rs.getString(1);
                count = rs.getInt(2);
                if (stateCounts.containsKey(state))
                    stateCounts.put(state, Integer.valueOf(count));
            }

            return stateCounts;
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
            throw new JobException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
    }

    public Collection<JobImpl> getJobsByState(String p_state, String userId)
            throws RemoteException, JobException
    {
        return getJobsByStateList(argList(p_state), userId);
    }

    private Collection<JobImpl> getJobsByStateList(
            Vector<String> p_listOfStates, String userId)
    {
        Collection<JobImpl> results = null;

        m_myJobsDaysRetrieved = getMyJobsDaysRetrieved();
        try
        {
            String sql = jobsByStateListQuery(p_listOfStates, userId);
            results = HibernateUtil.searchWithSql(JobImpl.class, sql);
        }
        catch (Exception e)
        {
            c_category.error("JobHandlerLocal::jobByStateList", e);
            String[] args = new String[1];
            args[0] = p_listOfStates.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }

        return results;
    }

    private String jobsByStateListQuery(Vector<String> p_listOfStates,
            String userId)
    {
        return jobsByStateListQuery(p_listOfStates, userId, false);
    }

    private String jobsByStateListQuery(Vector<String> p_listOfStates,
            String userId, boolean p_queryLimitByDate)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append("FROM JOB j, L10N_PROFILE lp, PROJECT_USER pu");
        sb.append(" WHERE ");
        addStateClause(sb, p_listOfStates, "j");
        sb.append(" AND j.l10n_profile_id = lp.id");
        sb.append(" AND lp.project_id = pu.project_id");

        if (p_queryLimitByDate && m_myJobsDaysRetrieved > 0)
        {
            sb.append(" AND j.TIMESTAMP > ?");
        }

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            sb.append(" AND j.COMPANY_ID = ");
            sb.append(Long.parseLong(currentId));
        }
        sb.append(" AND pu.user_id = ");
        sb.append(" '" + userId + "'");

        c_category.debug("The query is " + sb.toString());

        return sb.toString();
    }
}

package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.date.DateHelper;

public class JobSummaryHelper
{
    private static final Logger CATEGORY = Logger.getLogger(JobSummaryHelper.class);
    protected static boolean s_isCostingEnabled = false;
    protected static boolean s_isRevenueEnabled = false;
    
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_isCostingEnabled = sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            s_isRevenueEnabled = sc.getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch (Throwable e)
        {
            CATEGORY.error("JobSummaryHelper::invokeJobControlPage(): " + "Problem getting costing parameter from database.", e);
        }
    }
    
    public void packJobSummaryInfoView(HttpServletRequest p_request,Job job)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        p_request.setAttribute("Job",job);
        p_request.setAttribute("jobId",job.getJobId());
        p_request.setAttribute("dateCreated",getDateCreated(session, uiLocale, job));
        p_request.setAttribute("jobInitiator",getInitiatorUserName(job));
        p_request.setAttribute("srcLocale",job.getSourceLocale().getDisplayName(uiLocale));
        p_request.setAttribute("openSegmentCommentsCount", getOpenSegmentCommentsCount(job, session));
        p_request.setAttribute("closedSegmentCommentsCount", getClosedSegmentCommentsCount(job, session));
        p_request.setAttribute("isFinshedJob", isFinishedJob(job));
        p_request.setAttribute("isCostingEnabled", s_isCostingEnabled);
        p_request.setAttribute("jobCostsTabPermission", getJobCostsTabPermission(session));
        
        //Edit Source page word counts and edit costs page needed
        sessionMgr.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET,job.getJobName());
    }
    
    public Job getJobByRequest(HttpServletRequest p_request){
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
        Job job = WorkflowHandlerHelper.getJobById(getJobId(p_request,sessionMgr));
        
        //Comment Page will retrieve it,but I don't know it's reason since now.
        TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT, job);
        return job;
    }
    
    private String getDateCreated(HttpSession session, Locale uiLocale, Job job)
    {
        TimeZone timeZone = (TimeZone) session.getAttribute(WebAppConstants.USER_TIME_ZONE);
        String dateCreated = DateHelper.getFormattedDateAndTime(job.getCreateDate(),uiLocale, timeZone);
        return dateCreated;
    }

    private String getInitiatorUserName(Job job)
    {
        User initiator = job.getCreateUser();
        if (initiator == null)
        {
            initiator = job.getProject().getProjectManager();
        }
        return initiator.getUserName();
    }
    
    /**
     * Returns the id of the job that an action is being performed on.
     */
    protected long getJobId(HttpServletRequest p_request,
            SessionManager p_sessionMgr)
    {
        long jobId = 0;
        // Get the jobId from the sessionMgr if it's not in the request.
        // This means you are coming to the Job Details screen from
        // somewhere other than the My Jobs screens.
        if (p_request.getParameterValues(WebAppConstants.JOB_ID) == null)
        {
            try
            {
                Object oJobId = p_sessionMgr.getAttribute(WebAppConstants.JOB_ID);
                // The type of oJobId may be Long or String.
                if (oJobId instanceof Long)
                {
                    jobId = ((Long) oJobId).longValue();
                }
                else
                {
                    jobId = Long.parseLong((String) oJobId);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to get job id from session manager ", e);
            }
        }
        else
        {
            String[] jobIdInfo = p_request.getParameterValues(JobManagementHandler.JOB_ID);
            String jobIdString = jobIdInfo[0];
            jobId = Long.parseLong(jobIdString);
            p_sessionMgr.setAttribute(JobManagementHandler.JOB_ID, new Long(jobId));
        }
        return jobId;
    }
    
    private int getOpenSegmentCommentsCount(Job job,HttpSession session){
        int openSegmentCount = 0;
        List<String> oStates = new ArrayList<String>();
        oStates.add(Issue.STATUS_OPEN);
        oStates.add(Issue.STATUS_QUERY);
        oStates.add(Issue.STATUS_REJECTED);
        openSegmentCount = getSegmentCommentsCount(job, session, oStates);
        return openSegmentCount;
    }
    
    private int getClosedSegmentCommentsCount(Job job,HttpSession session){
        int closedSegmentCount = 0;
        List<String> cStates = new ArrayList<String>();
        cStates.add(Issue.STATUS_CLOSED);
        closedSegmentCount = getSegmentCommentsCount(job, session, cStates);
        return closedSegmentCount;
    }
    
    private int getSegmentCommentsCount(Job job, HttpSession session,
            List<String> states) throws EnvoyServletException
    {
        Set<Long> targetPageIds = getTargetPageIdsByJob(job.getId());
        if (targetPageIds.size() == 0)
        {
            return 0;
        }

        try
        {
            CommentManager manager = ServerProxy.getCommentManager();
            return manager.getIssueCount(Issue.TYPE_SEGMENT,
                    new ArrayList<Long>(targetPageIds), states);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    /**
     * Get all target page IDs for all workflows in current job.
     */
    private Set<Long> getTargetPageIdsByJob(long jobId)
    {
        Set<Long> targetPageIds = new HashSet<Long>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = "SELECT DISTINCT tp.ID FROM target_page tp, workflow wf "
                        + " WHERE wf.IFLOW_INSTANCE_ID = tp.WORKFLOW_IFLOW_INSTANCE_ID "
                        + " AND wf.STATE != 'CANCELLED' "
                        + " AND tp.STATE != 'IMPORT_FAIL' "
                        + " AND wf.JOB_ID = ?";
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, jobId);
            rs = ps.executeQuery();
            while (rs != null && rs.next())
            {
                long tpId = rs.getLong(1);
                targetPageIds.add(tpId);
            }
        }
        catch (Exception e)
        {
            
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return targetPageIds;
    }

    private boolean isFinishedJob(Job job)
    {
        if (Job.EXPORTED.equals(job.getState()) || 
           Job.EXPORT_FAIL.equals(job.getState()) ||
           Job.LOCALIZED.equals(job.getState()) || 
           Job.ARCHIVED.equals(job.getState()))
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
    
    private boolean getJobCostsTabPermission(HttpSession session){
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
        boolean jobCostingView = perms.getPermissionFor(Permission.JOB_COSTING_VIEW);
        boolean jobQuoteSend = perms.getPermissionFor(Permission.JOB_QUOTE_SEND);
        boolean jobQuotePonumberView = perms.getPermissionFor(Permission.JOB_QUOTE_PONUMBER_VIEW);
        boolean jobQuoteApprove = perms.getPermissionFor(Permission.JOB_QUOTE_APPROVE);
        boolean jobQuoteStatusView = perms.getPermissionFor(Permission.JOB_QUOTE_STATUS_VIEW);
        
        return jobCostingView || jobQuoteSend || jobQuotePonumberView || jobQuoteApprove || jobQuoteStatusView;
    }
}

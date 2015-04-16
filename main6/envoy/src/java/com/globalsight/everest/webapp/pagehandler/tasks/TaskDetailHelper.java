package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.comment.CommentMainHandler;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class TaskDetailHelper 
{
	
	private static final Logger CATEGORY = Logger
    	.getLogger(CommentMainHandler.class.getName());
	
	public static final String TASK_HOURS_STATE = "taskHours";
    public static final String TASK_PAGES_STATE = "taskPages";
    public static final String TASK_STATE = WebAppConstants.TASK_STATE;
    public static final String WORK_OBJECT = WebAppConstants.WORK_OBJECT;
    public static final String UILOCALE = WebAppConstants.UILOCALE;

	public void prepareTaskData(HttpServletRequest p_request, HttpServletResponse p_response,
			HttpSession httpSession, String taskId)
	{
    	User user = TaskHelper.getUser(httpSession);
		String taskStateParam = p_request.getParameter(TASK_STATE);
		int taskState = TaskHelper.getInt(taskStateParam, -10);
    	Task task = TaskHelper.getTask(user.getUserId(),Long.parseLong(taskId), taskState);
    	TaskHelper.storeObject(httpSession, WORK_OBJECT, task);
    	TaskHelper.updateMRUtask(p_request, httpSession, task, p_response,
                task.getState());
    	
    	Locale uiLocale = (Locale) httpSession.getAttribute(UILOCALE);
    	List targetPages = null;
        if (task.getTaskType().equals(Task.TYPE_TRANSLATION))
        {
            targetPages = task.getTargetPages();
        }
        else
        {
            try
            {
                Job job = ServerProxy.getJobHandler().getJobById(
                        task.getJobId());
                List workflows = new ArrayList();
                workflows.addAll(job.getWorkflows());
                for (int i = 0; i < workflows.size(); i++)
                {
                    Workflow workflow = (Workflow) workflows.get(i);
                    if (workflow.getTargetLocale().equals(
                            task.getTargetLocale()))
                    {
                        if (workflow.getWorkflowType().equals(
                                WorkflowTypeConstants.TYPE_TRANSLATION))
                        {
                            targetPages = new ArrayList();
                            targetPages.addAll(workflow.getTargetPages());
                            break;
                        }
                    }
                }
                if (targetPages == null)
                {
                    targetPages = new ArrayList();
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
    	targetPages = filterPagesByName(p_request, httpSession, targetPages);
        sortPages(p_request, httpSession, uiLocale, targetPages);
        TaskHelper.storeObject(httpSession, WebAppConstants.TARGET_PAGES,
                targetPages);
        
        boolean isHourlyRate = ((task.getState() == Task.STATE_ACCEPTED) && isHourlyRate(
                task, null)) ? true : false;
        TaskHelper.storeObject(httpSession, TASK_HOURS_STATE, new Boolean(
                isHourlyRate));
        
        boolean isPageBasedRate = ((task.getState() == Task.STATE_ACCEPTED) && isPageBasedRate(
                task, null)) ? true : false;
        TaskHelper.storeObject(httpSession, TASK_PAGES_STATE, new Boolean(
                isPageBasedRate));
        
        int openSegmentCount = 0;
        int closedSegmentCount = 0;
        // Get the number of open and closed issues.
        // get just the number of issues in OPEN state
        // query is also considered a subset of the OPEN state
        List<String> oStates = new ArrayList<String>();
        oStates.add(Issue.STATUS_OPEN);
        oStates.add(Issue.STATUS_QUERY);
        oStates.add(Issue.STATUS_REJECTED);
        openSegmentCount = getIssueCount(task, httpSession, oStates);

        // get just the number of issues in CLOSED state
        List<String> cStates = new ArrayList<String>();
        cStates.add(Issue.STATUS_CLOSED);
        closedSegmentCount = getIssueCount(task, httpSession, cStates);
        httpSession.setAttribute(
                JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS,
                new Integer(openSegmentCount).toString());
        httpSession.setAttribute(
                JobManagementHandler.CLOSED_SEGMENT_COMMENTS, new Integer(
                        closedSegmentCount).toString());
        
        String action = p_request.getParameter(WebAppConstants.TASK_ACTION);
        if (WebAppConstants.TASK_ACTION_RETRIEVE.equals(action))
        {
        	// Set detail page id in session
            TaskHelper.storeObject(httpSession, WebAppConstants.TASK_DETAILPAGE_ID,
                    TaskHelper.DETAIL_PAGE_1);
        }
	}
	
	
	/**
     * Filter the pages by the specified search filter. Return only the pages
     * that match the filter.
     */
    protected List filterPagesByName(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
    {
        String thisFileSearch = (String) p_request
                .getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);

        if (thisFileSearch == null)
            thisFileSearch = (String) p_session
                    .getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);

        if (thisFileSearch != null)
        {
            ArrayList filteredFiles = new ArrayList();
            for (Iterator fi = p_pages.iterator(); fi.hasNext();)
            {
                Page p = (Page) fi.next();
                if (p.getExternalPageId().indexOf(thisFileSearch) >= 0)
                {
                    filteredFiles.add(p);
                }
            }
            return filteredFiles;
        }
        else
        {
            // just return all - no filter
            return p_pages;
        }
    }
    
    /**
     * Sorts the target pages for the task specified by the sort column and
     * direction.
     */
    @SuppressWarnings("unchecked")
    protected void sortPages(HttpServletRequest p_request,
            HttpSession p_session, Locale p_uiLocale, List p_pages)
    {
        // first get comparator from session
        PageComparator comparator = (PageComparator) p_session
                .getAttribute(JobManagementHandler.PAGE_COMPARATOR);
        if (comparator == null)
        {
            // Default: Sort by external page id (page name) ascending, so it'll
            // be alphabetized
            comparator = new PageComparator(PageComparator.EXTERNAL_PAGE_ID,
                    true, p_uiLocale);
            p_session.setAttribute(JobManagementHandler.PAGE_COMPARATOR,
                    comparator);
        }

        String criteria = p_request
                .getParameter(JobManagementHandler.PAGE_SORT_PARAM);
        if (criteria != null)
        {
            int sortCriteria = Integer.parseInt(criteria);
            if (comparator.getSortColumn() == sortCriteria)
            {
                // just reverse the sort order
                comparator.reverseSortingOrder();
            }
            else
            {
                // set the sort column
                comparator.setSortColumn(sortCriteria);
            }
        }

        SortUtil.sort(p_pages, comparator);
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
    }
    
    private int getIssueCount(Task task, HttpSession session,
            List<String> states) throws EnvoyServletException
    {
        int count = 0;

        Workflow wf = task.getWorkflow();
        if (!(wf.getState().equals(Workflow.CANCELLED)))
        {
            List pages = wf.getTargetPages();
            List<Long> targetPageIds = new ArrayList<Long>();
            for (int j = 0; j < pages.size(); j++)
            {
                TargetPage tPage = (TargetPage) pages.get(j);
                String state = tPage.getPageState();
                if (!PageState.IMPORT_FAIL.equals(state))
                {
                    targetPageIds.add(tPage.getId());
                }
            }

            try
            {
                CommentManager manager = ServerProxy.getCommentManager();
                count = manager.getIssueCount(Issue.TYPE_SEGMENT,
                        targetPageIds, states);
            }
            catch (Exception ex)
            {
                throw new EnvoyServletException(ex);
            }
        }
        return count;
    }
    
    private boolean isPageBasedRate(Task p_task, String p_user)
    {
        // If costing has been disabled,
        // we do not want to show the Page based rate field in the ui.
        boolean isPage = false;
        SystemConfiguration sc = SystemConfiguration.getInstance();
        boolean s_isCostingEnabled = sc
                .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        if (s_isCostingEnabled)
        {
            Rate actualRate = getActualRateToBeUsed(p_task, p_user);
            if ((actualRate != null && actualRate.getRateType().equals(
                    Rate.UnitOfWork.PAGE_COUNT))
                    || ((p_task.getRevenueRate() != null) && p_task
                            .getRevenueRate().getRateType()
                            .equals(Rate.UnitOfWork.PAGE_COUNT)))
            {
                isPage = true;
            }
        }
        return isPage;
    }
    
    private boolean isHourlyRate(Task p_task, String p_user)
    {
        // If costing has been disabled,
        // we do not want to show the hour based rate field in the ui.
        boolean isHourly = false;
        SystemConfiguration sc = SystemConfiguration.getInstance();
        boolean s_isCostingEnabled = sc
                .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        if (s_isCostingEnabled)
        {
            Rate actualRate = getActualRateToBeUsed(p_task, p_user);
            if ((actualRate != null && actualRate.getRateType().equals(
                    Rate.UnitOfWork.HOURLY))
                    || ((p_task.getRevenueRate() != null) && p_task
                            .getRevenueRate().getRateType()
                            .equals(Rate.UnitOfWork.HOURLY)))
            {
                isHourly = true;
            }
        }
        return isHourly;
    }
    
    /**
     * This is copied from CostingEngineLocal The logic is not exactly same but
     * quite similar.
     */
    private Rate getActualRateToBeUsed(Task t, String p_acceptor)
    {
        Rate useRate = t.getExpenseRate();
        int selectionCriteria = t.getRateSelectionCriteria();
        User user = null;

        if (selectionCriteria == WorkflowConstants.USE_SELECTED_RATE_UNTIL_ACCEPTANCE)
        {
            // find out who accepted the task
            try
            {
                String acceptor = t.getAcceptor();
                if (acceptor != null)
                {
                    user = ServerProxy.getUserManager().getUser(acceptor);
                }
                else
                {
                    if (p_acceptor != null)
                    {
                        user = ServerProxy.getUserManager().getUser(p_acceptor);
                    }
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "TaskDetailHandler::Problem getting user information ",
                        e);
            }
            try
            {
                // Now find out what is the default rate for this user.
                if (user != null)
                {
                    // find out user role
                    Vector uRoles = new Vector(ServerProxy.getUserManager()
                            .getUserRoles(user));
                    String activity = t.getTaskName();
                    GlobalSightLocale source = t.getSourceLocale();
                    GlobalSightLocale target = t.getTargetLocale();

                    for (int i = 0; i < uRoles.size(); i++)
                    {
                        Role curRole = (Role) uRoles.get(i);
                        // Get the source and target locale for each role.
                        String sourceLocale = curRole.getSourceLocale();
                        String targetLocale = curRole.getTargetLocale();
                        Activity act = curRole.getActivity();
                        UserRole cRole = (UserRole) uRoles.get(i);

                        if (act.getActivityName().equals(activity)
                                && sourceLocale.equals(source.toString())
                                && targetLocale.equals(target.toString()))
                        {
                            // Found the userRole we are talking about
                            if (cRole != null && cRole.getRate() != null)
                            {
                                Long rate = new Long(cRole.getRate());
                                useRate = getRate(rate.longValue());
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "TaskDetailHandler::Problem getting user information ",
                        e);
            }
        }
        return useRate;
    }
    
    /*
     * Copied from CostingEngine
     */
    private Rate getRate(long p_id) throws RemoteException
    {
        CostingEngineLocal local = new CostingEngineLocal();
        return local.getRate(p_id);
    }
    
    
    /**
     * methods from taskdetail jsp
     */
    public static String getMainFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            index++;
            while (Character.isSpace(p_filename.charAt(index)))
            {
                index++;
            }

            return p_filename.substring(index, p_filename.length());
        }

        return p_filename;
    }

    public static String getSubFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            return p_filename.substring(0, p_filename.indexOf(")") + 1);
        }

        return null;
    }

    public static String getFileName(String p_page)
    {
        String fileName = getMainFileName(p_page);
        String subName = getSubFileName(p_page);

        if (subName != null)
        {
            fileName = fileName + " " + subName;
        }

        return fileName;
    }

    // Prints file name with full path
    public static String printPageLink(JspWriter out, String p_page,
            String p_url, boolean hasEditPerm, int pageNum) throws IOException
    {
        // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
        // show them last so the main file names are grouped together
        // text=en_US\182\endpoint\ch01.htm|sourcePageId=752|targetPageId=3864|taskId=4601?

        String pageName = getMainFileName(p_page);
        String subName = getSubFileName(p_page);

        if (subName != null)
        {
            pageName = pageName + " " + subName;
        }
        StringBuffer treeParam = new StringBuffer();
        if (hasEditPerm)
        {
            out.print("<a href='#'");
            out.print(" onclick=\"openEditorWindow('");
            out.print(p_url);
            out.print("', event); return false;\"");
            out.print(" oncontextmenu=\"contextForPage('");
            out.print(p_url);
            out.print("', event, '");
            out.print( pageNum);
            out.print("')\"");
            out.print(" onfocus='this.blur();'");
            out.print(" page='");
            out.print(p_url);
            out.print("'");
            out.print(" CLASS='standardHREF'>");
            out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:500px\'>\");}</SCRIPT>");
            out.print(pageName);
            out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>");
            out.print("</a>");
            treeParam.append("text=" + pageName)
                    .append(p_url.replaceAll("&", "|"))
                    .append("|title=" + pageName);
        }
        else
        {
            out.print(pageName);
        }

        return treeParam.toString();
    }

    // Prints short file name with full path in tooltip
    public static String printPageLinkShort(JspWriter out, String p_page,
            String p_url, boolean hasEditPerm, int pageNum) throws IOException
    {
        // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
        // show them last so the main file names are grouped together
        String pageName = getMainFileName(p_page);
        String subName = getSubFileName(p_page);
        String shortName = pageName;

        int bslash = shortName.lastIndexOf("\\");
        int fslash = shortName.lastIndexOf("/");
        int index;
        if (bslash > 0 && bslash > fslash)
        {
            shortName = shortName.substring(bslash + 1);
        }
        else if (fslash > 0 && fslash > bslash)
        {
            shortName = shortName.substring(fslash + 1);
        }

        if (subName != null)
        {
            pageName = pageName + " " + subName;
            shortName = shortName + " " + subName;
        }
        StringBuffer treeParam = new StringBuffer();
        if (hasEditPerm)
        {
            out.print("<a href='#'");
            out.print(" onclick=\"openEditorWindow('");
            out.print(p_url);
            out.print("', event); return false;\"");
            out.print(" oncontextmenu=\"contextForPage('");
            out.print(p_url);
            out.print("', event, '");
            out.print(pageNum);
            out.print("')\"");
            out.print(" onfocus='this.blur();'");
            out.print(" page='");
            out.print(p_url);
            out.print("'");
            out.print(" CLASS='standardHREF' TITLE='");
            out.print(pageName.replace("\'", "&apos;"));
            out.print("'>");
            out.print(shortName);
            out.print("</a>");
            treeParam.append("text=" + shortName.replace("\'", "&apos;"))
                    .append(p_url.replaceAll("&", "|"))
                    .append("|title=" + pageName);
        }
        else
        {
            out.print(pageName);
        }

        return treeParam.toString();
    }
    
    public static StringBuffer getExportLink(Task theTask, String url, long workflowId)
    {
        StringBuffer exportLink = new StringBuffer(url);
        exportLink.append("&");
        exportLink.append(JobManagementHandler.WF_ID);
        exportLink.append("=");
        exportLink.append(workflowId);
        exportLink.append("&");
        exportLink.append(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
        exportLink.append("=true");
        //GBS-2913 Added to the url parameter taskId
        exportLink.append("&");
        exportLink.append(WebAppConstants.TASK_ID);
        exportLink.append("=");
        exportLink.append(theTask.getId());
        exportLink.append("&");
        exportLink.append(WebAppConstants.TASK_STATE);
        exportLink.append("=");
        exportLink.append(theTask.getState());
        
        return exportLink;
    }

    public static StringBuffer getDownloadLink(Task theTask, String url,
            long workflowId, String jobId)
    {
        StringBuffer downloadLink = new StringBuffer(
                "/globalsight/ControlServlet"
                        + "?linkName=jobDownload&pageName=TK2"
                        + "&firstEntry=true&fromTaskDetail=true");
        downloadLink.append("&");
        downloadLink.append(DownloadFileHandler.PARAM_JOB_ID);
        downloadLink.append("=");
        downloadLink.append(jobId);
        downloadLink.append("&");
        downloadLink.append(DownloadFileHandler.PARAM_WORKFLOW_ID);
        downloadLink.append("=");
        downloadLink.append(workflowId);
        // GBS-2913 Added to the url parameter taskId
        downloadLink.append("&");
        downloadLink.append(WebAppConstants.TASK_ID);
        downloadLink.append("=");
        downloadLink.append(theTask.getId());
        downloadLink.append("&");
        downloadLink.append(WebAppConstants.TASK_STATE);
        downloadLink.append("=");
        downloadLink.append(theTask.getState());

        return downloadLink;
    }
}

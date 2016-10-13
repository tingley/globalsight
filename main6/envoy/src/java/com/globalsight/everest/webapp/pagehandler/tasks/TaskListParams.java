package com.globalsight.everest.webapp.pagehandler.tasks;

import com.globalsight.everest.taskmanager.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Parameter object used in task list page
 * Created by VincentYan on 14-1-14.
 */
public class TaskListParams
{
    private String action = "";
    private int taskState = -1;
    private String taskStateString = "";
    private String errorMessage = "";

    private boolean isSearchEnable = false;

    private String helpTitle = "";
    private String helpFileUrl = "";

    private String sortColumn = "";
    private boolean isAscSort = false;

    private List<Task> tasks = null;
    private HashMap<String, String> filters = new HashMap<String, String>();

    private int pageNumber = 0;
    private int pageCount = 0;
    private int totalRecords = 0;
    private int startRecord = 0;
    private int endRecord = 0;
    private int perPageCount = 20;

    private boolean isSuperUser = false;
    private boolean isProjectManager = false;
    private boolean canManageProjects = false;
    private boolean canManageWorkflows = false;
    private boolean canExportAll = false;
    private boolean canExportInProgress = false;
    private boolean isCombinedFormat = false;
    
    private Locale UILocale = Locale.US;
    private TimeZone timeZone = TimeZone.getDefault();

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public int getEndRecord() {
        return endRecord;
    }

    public void setEndRecord(int endRecord) {
        this.endRecord = endRecord;
    }

    public boolean isSearchEnable() {
        return isSearchEnable;
    }

    public void setSearchEnable(boolean isSearchEnable) {
        this.isSearchEnable = isSearchEnable;
    }

    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }

    public String getTaskStateString() {
        return taskStateString;
    }

    public void setTaskStateString(String taskStateString) {
        this.taskStateString = taskStateString;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAscSort() {
        return isAscSort;
    }

    public void setAscSort(boolean isAscSort) {
        this.isAscSort = isAscSort;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public boolean isSuperUser() {
        return isSuperUser;
    }

    public void setSuperUser(boolean isSuperUser) {
        this.isSuperUser = isSuperUser;
    }

    public boolean isCanManageProjects() {
        return canManageProjects;
    }

    public void setCanManageProjects(boolean canManageProjects) {
        this.canManageProjects = canManageProjects;
    }

    public boolean isCanManageWorkflows() {
        return canManageWorkflows;
    }

    public void setCanManageWorkflows(boolean canManageWorkflows) {
        this.canManageWorkflows = canManageWorkflows;
    }

    public boolean isCanExportAll() {
        return canExportAll;
    }

    public void setCanExportAll(boolean canExportAll) {
        this.canExportAll = canExportAll;
    }

    public boolean isCanExportInProgress() {
        return canExportInProgress;
    }

    public void setCanExportInProgress(boolean canExportInProgress) {
        this.canExportInProgress = canExportInProgress;
    }

    public boolean isCombinedFormat() {
        return isCombinedFormat;
    }

    public void setCombinedFormat(boolean isCombinedFormat) {
        this.isCombinedFormat = isCombinedFormat;
    }

    public String getHelpTitle() {
        return helpTitle;
    }

    public void setHelpTitle(String helpTitle) {
        this.helpTitle = helpTitle;
    }

    public String getHelpFileUrl() {
        return helpFileUrl;
    }

    public void setHelpFileUrl(String helpFileUrl) {
        this.helpFileUrl = helpFileUrl;
    }
    
    public int getPerPageCount()
    {
        return perPageCount;
    }

    public void setPerPageCount(int perPageCount)
    {
        this.perPageCount = perPageCount;
    }

    public boolean isProjectManager()
    {
        return isProjectManager;
    }

    public void setProjectManager(boolean isProjectManager)
    {
        this.isProjectManager = isProjectManager;
    }

    public Locale getUILocale()
    {
        return UILocale;
    }

    public void setUILocale(Locale uILocale)
    {
        UILocale = uILocale;
    }

    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

}

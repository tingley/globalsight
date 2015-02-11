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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.Param;
import com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.ParamObjectPair;
import com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.ParamObjectPairFactory;
import com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.ProjectWorkflowData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGeneratorHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.IntHolder;

public class CustomizeReportsGenerator 
{
    private Map paramMap;
    private Param jobInfoParam;
    private ResourceBundle bundle = null;
    private ReportWriter reportWriter = null;
    
//    private final String SHEET_NAME = "Sheet1";
//    private final String REPORT_TITLE = "Customize Reports";
    
    private int beginColumn;
    private int titleRow;
    private int headerBeginRow;
    private int headerEndRow;
    private int contentBeginRow;
    
    private List total;
    private String userId = null;
    private List<Long> m_jobIDS = null;
    
    public CustomizeReportsGenerator(Map p_paramMap, ReportWriter p_reportWriter) 
    {
        if (p_paramMap == null)
        {
            throw new NullPointerException();
        }
        
        this.paramMap = p_paramMap;
        this.reportWriter = p_reportWriter;
        userId = (String) paramMap.get(WebAppConstants.USER_NAME);
        
        this.init();
    }
    
    private void init()
    {
        this.bundle = this.getLabelBundle();
        this.jobInfoParam = this.getJobInfoParam();
        this.setRanges(this.jobInfoParam);
        this.reportWriter.setSheet(this.reportWriter.createSheet(bundle.getString("lb_sheet") + "1"));
    }
    
    private void setRanges(Param p_jobInfoParam)
    {
        this.beginColumn = 0;
        
        this.titleRow = 0;
        
        this.headerBeginRow = 3;
        this.headerEndRow = 
            this.headerBeginRow + this.countRowAmount(p_jobInfoParam) - 1;
        
        this.contentBeginRow = this.headerEndRow + 1;
    }
    
    public void pupulate() 
    throws JobException, IOException
    {
        // Add title
        this.reportWriter.addTitleCell(this.beginColumn, 
                                       this.titleRow, 
                                       bundle.getString("lb_customize_reports"));
        
        // Add header
        this.addHeader(jobInfoParam, beginColumn, headerBeginRow);   
        
        //Add workflow
        Job job = null;
        IntHolder row = new IntHolder(this.contentBeginRow);
        List jobList = this.getJobList();
        List targetLocaleList = this.getTargetLocaleList();
        List statusList = this.getStatusList();
        ProjectWorkflowData workflowData = new ProjectWorkflowData();
        m_jobIDS = ReportHelper.getJobIDS(jobList);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                m_jobIDS, getReportType()))
        {
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);
        
        for (Iterator jobIter = jobList.iterator(); jobIter.hasNext();)
        {
            if (isCancelled())
            {
                return;
            }
            
            job = (Job) jobIter.next();
            boolean needsBlankRow = false;
            
            Iterator workflowIter = job.getWorkflows().iterator();
            while (workflowIter.hasNext())
            {
                Workflow workflow = (Workflow) workflowIter.next();
                
                if (this.validWorkflow(workflow, statusList, targetLocaleList))
                {
                    this.addWorkflow(job, workflow, workflowData, row);
                    needsBlankRow = true;
                }
            }
            
            //Add blank row between different jobs.
            if (needsBlankRow) this.addBlankRow(row);
        }
        
        //Add footer
        this.addFooter(row.getValue());
        
        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                        100, ReportsData.STATUS_FINISHED);
    }
    
    /**
     * Query all jobs according to the parameter in paramMap and return them.
     * @throws JobException 
     */
    private List getJobList() throws JobException
    {
        List jobRangeParams = (List) paramMap.get(WebAppConstants.JOB_RANGE_PARAM);
        List jobList = new ArrayList();
        
        if (jobRangeParams.size() == 0) return jobList;
        
        for(int i = 0; i < jobRangeParams.size(); i++)
        {
            JobSearchParameters sp = (JobSearchParameters)jobRangeParams.get(i);
            
            try 
            {
                jobList.addAll(ServerProxy.getJobHandler().getJobs(sp));
            } 
            catch (Exception e) 
            {
                throw new JobException(e);
            }
        }
        
        return jobList;
    }
    
    /**
     * Get the target locale list specified. If no target locale specified, then
     * all target locales will be added into report. Otherwise, only the selected 
     * target locales will be added into report.
     */
    private List getTargetLocaleList()
    {
        return (List) paramMap.get(WebAppConstants.TARGET_LOCALE_PARAM);
    }
    
    private ResourceBundle getLabelBundle()
    {
        return (ResourceBundle) paramMap.get(WebAppConstants.LABEL_BUNDLE_PARAM);
    }
    
    private List getStatusList() 
    {
        return (List) paramMap.get(WebAppConstants.WORKFLOW_STATUS_PARAM);
    }
    
    private Param getJobInfoParam()
    {
        return (Param) paramMap.get(WebAppConstants.JOB_INFO_PARAM);
    }
    
    private DateFormat getDateFormat()
    {
        return (DateFormat) paramMap.get(WebAppConstants.DATE_FORMAT_PARAM);
    }
    
    /**
     * Add header to the specified sheet.  The header columns will be determined
     * by the specified job info parameters.  
     */
    private void addHeader(Param p_param, int p_column, int p_row)
    throws IOException
    {
        Param[] params = p_param.getChildParams();
        
        for (int i = 0; i < params.length; i++)
        {
            Param param = params[i];
            if (!param.getValue()) continue;
            
            String label = this.bundle.getString(param.getCompletedName());
            this.reportWriter.addHeaderCell(p_column, p_row, label);
            
            if (param.hasSelectedChildren())
            {
                this.reportWriter.mergeCells(
                        p_column, 
                        p_row,
                        p_column + this.countColumnAmount(param) - 1,
                        p_row);
                
                addHeader(param, p_column, p_row + 1);
                
                p_column += this.countColumnAmount(param);
            }
            else
            {
                this.reportWriter.mergeCells(
                        p_column, 
                        p_row,
                        p_column, 
                        this.headerEndRow);
                
                p_column++;
            }
        }
    }
    
    private int countColumnAmount(Param param)
    {
        int result = 0;

        if (param.hasSelectedChildren()) {
            Param[] children = param.getChildParams();
            for (int i = 0; i < children.length; i++) {
                result += countColumnAmount(children[i]);
            }
        } else {
            if (param.getValue()) {
                result++;
            }
        }

        return result;
    }
    
    private int countRowAmount(Param param)
    {
        int result = 1;
        
        if (param.hasSelectedChildren())
        {
            Param[] children = param.getChildParams();
            for (int i = 0; i < children.length; i++) 
            {
                if (!children[i].getValue()) continue;
                int childrenRowAmount = countRowAmount(children[i]) + 1;
                result = result > childrenRowAmount ? result : childrenRowAmount;
            }
        }
        
        return result;
    }
    
    /**
     * Validate if the specified workflow is in the specified status or has the 
     * specified target locale.  If no specifed status or specified target
     * locale, then just treat this workflow as valid.
     */
    private boolean validWorkflow(Workflow p_workflow, List status, List targetLocales)
    {
        boolean result = true;
        
        //If has specified status, then check if the state of this workflow in  
        //it; Otherwise, just treat the status as valid
        String state = p_workflow.getState();
        boolean isStatusValid = true;
        if ((status != null) && (status.size() > 0))
        {
            isStatusValid = false;
            for (int i = 0; i < status.size(); i++)
            {
                isStatusValid |= state.equals((String)status.get(i));
            }
        }
        result &= isStatusValid;
        
        //If has specified target locale, then check if the target locale of 
        //this workflow is in it; Otherwise, just treat the target locale as valid
        boolean isValidTargetLocale = true;
        if ((targetLocales != null) && (targetLocales.size() > 0))
        {
            isValidTargetLocale = false;
            for (int i = 0; i < targetLocales.size(); i++)
            {
                isValidTargetLocale = isValidTargetLocale ||
                    ((String)targetLocales.get(i))
                        .equals(Long.toString(p_workflow.getTargetLocale().getId()));
                
            }
        }
        result &= isValidTargetLocale;
        
        return result;
    }
    
    private void addWorkflow(Job p_job, 
                             Workflow p_workflow, 
                             ProjectWorkflowData workflowData, 
                             IntHolder p_row) 
    throws IOException
    {
        DateFormat dateFormat = getDateFormat();
        int columnCounter = 0;
        int rowCounter = p_row.getValue();
        
        this.total = new ArrayList();
        
        //If no field selected, then the report should be empty.
        if (!this.jobInfoParam.hasSelectedChildren())
        {
            return;
        }
        
        //Add all info that should be in report into a list.
        Param [] children = this.jobInfoParam.getSelectedChildren();
        
        
        List result = new ArrayList();
        for (int i = 0; i < children.length; i++)
        {
            ParamObjectPair pair = ParamObjectPairFactory.getInstance(children[i]);
            result.addAll(pair.getResult(p_job, p_workflow, dateFormat, workflowData));
            this.total.addAll(pair.getTotal(workflowData));
        }
        
        //Add all info into report.
        for (Iterator iter = result.iterator(); iter.hasNext();)
        {
            this.reportWriter.addContentCell(columnCounter++, 
                                             rowCounter, 
                                             iter.next());    
        }
        
        //Increase the row number.
        p_row.inc();
    }
    
    private void addBlankRow(IntHolder p_row)
    {
        p_row.inc();
    }
    
    private void addFooter(int p_row) throws IOException
    {
        if ((this.total != null) && (this.total.size() > 0))
        {
            // Add total into report for each numerical column.
            this.reportWriter.addTotal(p_row, this.total);
        }
    }
    
    public String getReportType()
    {
        return ReportConstants.CUSTOMIZEREPORTS_REPORT;
    }
    
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(userId,
                m_jobIDS, getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }
}

/**
 *  Copyright 2012 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.reports.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * This Business Object is store the search option from page, which is used for creating
 * report.
 * 
 * @Date Sep 7, 2012
 */
public class ReportSearchOptions
{
    private boolean allJobIds = false;
    private boolean allProjects = false;
    private boolean allJobStatus = false;
    private boolean allTargetLangs = false;
    private List<Long> jobIdList = new ArrayList<Long>();
    private List<Long> projectIdList = new ArrayList<Long>();
    private List<String> jobStatusList = new ArrayList<String>();
    private List<GlobalSightLocale> targetLocaleList = new ArrayList<GlobalSightLocale>();
    private List<String> months = new ArrayList<String>();
    private String currency = null;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private Company curCompany;
    private String userId;
    
    private ResourceBundle bundle;
    
    private SimpleDateFormat sdf = null;

    public boolean isAllJobIds()
    {
        return allJobIds;
    }

    public void setAllJobIds(boolean allJobIds)
    {
        this.allJobIds = allJobIds;
    }
    
    public boolean isAllProjects()
    {
        return allProjects;
    }

    public void setAllProjects(boolean allProjects)
    {
        this.allProjects = allProjects;
    }
    
    public boolean isAllJobStatus()
    {
        return allJobStatus;
    }

    public void setAllJobStatus(boolean allJobStatus)
    {
        this.allJobStatus = allJobStatus;
    }

    public boolean isAllTargetLangs()
    {
        return allTargetLangs;
    }

    public void setAllTargetLangs(boolean allTargetLangs)
    {
        this.allTargetLangs = allTargetLangs;
    }

    public List<Long> getJobIdList()
    {
        return jobIdList;
    }

    public void setJobIdList(List<Long> jobIdList)
    {
        this.jobIdList = jobIdList;
    }
    
    public List<Long> getProjectIdList()
    {
        return projectIdList;
    }

    public void setProjectIdList(List<Long> projectIdList)
    {
        this.projectIdList = projectIdList;
    }
    
    public void addProjectId(String id)
    {
        if (id == null || id.trim().length() == 0)
            return;
        
        if (projectIdList == null)
            projectIdList = new ArrayList<Long>();
        
        projectIdList.add(new Long(id));
    }

    public List<String> getJobStatusList()
    {
        return jobStatusList;
    }

    public void setJobStatusList(List<String> jobStatusList)
    {
        this.jobStatusList = jobStatusList;
    }
    
    public void addJobStatusList(String status)
    {
        if (status == null || status.trim().length() == 0)
            return;
        
        if (jobStatusList == null)
            jobStatusList = new ArrayList<String>();
        
        jobStatusList.add(status);
    }
    
    public List<GlobalSightLocale> getTargetLocaleList()
    {
        if (targetLocaleList.size() == 0 && allTargetLangs)
        {
            return ReportHelper.getAllTargetLocales(Locale.US,
                    GlobalSightLocaleComparator.ISO_CODE);
        }
        else
        {
            return targetLocaleList;
        }
    }

    public void setTargetLocaleList(List<GlobalSightLocale> targetLocaleList)
    {
        this.targetLocaleList = targetLocaleList;
    }
    
    public void addTargetLocaleList(String targetLocaleId)
    {
        if (targetLocaleId == null || targetLocaleId.trim().length() == 0)
            return;
        
        try
        {
            GlobalSightLocale locale = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.valueOf(targetLocaleId));
            targetLocaleList.add(locale);
        }
        catch (Exception e)
        {
        }
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getSymbol()
    {
        return ReportUtil.getCurrencySymbol(currency);
    }

    public List<String> getMonths()
    {
        if(months.size() == 0)
        {
            Calendar date = (Calendar) startDate.clone();
            Calendar endDate1 = (Calendar) endDate.clone();
            endDate1.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            while (date.before(endDate1))
            {
                months.add(date.get(Calendar.YEAR) + "" + (date.get(Calendar.MONTH)+1));
                date.add(Calendar.MONTH, 1);
            }
        }
        
        return months;
    }
    
    public Date getStartDate()
    {
        return startDate.getTime();
    }
    
    public String getStartDateStr()
    {
        return formatDate(getStartDate());
    }

    public void setStartDate(Date p_startDate)
    {
        startDate.setTime(p_startDate);
        months.clear();
    }
    
    public void setStartDate(String p_startDate)
    {
        formatDate(startDate, p_startDate);
        months.clear();
    }

    public Date getEndDate()
    {
        return endDate.getTime();
    }
    
    public String getEndDateStr()
    {
        return formatDate(getEndDate());
    }

    public void setEndDate(Date p_endDate)
    {
        endDate.setTime(p_endDate);
        months.clear();
    }
    
    public void setEndDate(String p_endDate)
    {
        formatDate(endDate, p_endDate);
        endDate.add(Calendar.HOUR, 24);
        endDate.add(Calendar.MILLISECOND, -1);
        months.clear();
    }
    
    private void formatDate(Calendar p_date, String p_dateStr)
    {
        if(sdf == null)
        {
            setDateFormat(null);
        }
        
        try
        {
            p_date.setTime(sdf.parse(p_dateStr));
        }
        catch (ParseException e)
        {
        }
    }
    
    private String formatDate(Date p_date)
    {
        if(sdf == null)
        {
            setDateFormat(null);
        }
        
        return sdf.format(p_date);
    }
    
    public void setDateFormat(String p_pattern)
    {
        if(p_pattern == null || p_pattern.trim().length() == 0)
        {
            sdf = new SimpleDateFormat("MM/dd/yyyy");
        }
        else
        {
            sdf = new SimpleDateFormat(p_pattern);
        }
    }

    public ResourceBundle getBundle()
    {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle)
    {
        this.bundle = bundle;
    }
    
    public String getCurrentCompanyName()
    {
        return curCompany != null ? curCompany.getCompanyName() : "";
    }
    
    public Company getCurrentCompany()
    {
        return curCompany;
    }
    
    public void setCurrentCompany(String p_companyId)
    {
        if(p_companyId == null || p_companyId.trim().length() == 0)
            return;
        
        curCompany = CompanyWrapper.getCompanyById(p_companyId);
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
}

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
package com.globalsight.everest.webapp.pagehandler.administration.reports.bo;

import java.util.List;

public class ReportsData
{
    String userId;                      // Reports operator
    List<Long> reportJobIDS;            // Reports job id list
    List<String> reportTypeList;        // Reports type list
    double percent;                     // Reports percent
    String status;                      // Reports status
    String reportJobIDString;           // Reports job id list String
    String reportTypeString;            // Reports type list String

    // Reports status constants
    public static final String STATUS_INPROGRESS = "In Progress";
    public static final String STATUS_CANCEL = "Cancelled";
    public static final String STATUS_FINISHED = "Finished";

    
    public ReportsData(String userID, List<Long> reportJobIDS, List<String> reportTypeList, 
            double percent, String status)
    {
        this.reportJobIDS = reportJobIDS;
        this.reportTypeList = reportTypeList;
        this.userId = userID;
        this.percent = percent;
        this.status = status;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    
    public List<Long> getReportJobIDS()
    {
        return reportJobIDS;
    }

    public void setReportJobIDS(List<Long> reportJobIDS)
    {
        this.reportJobIDS = reportJobIDS;
    }
    
    public List<String> getReportTypeList()
    {
        return reportTypeList;
    }

    public void setReportTypeList(List<String> reportTypeList)
    {
        this.reportTypeList = reportTypeList;
    }
    
    public double getPercent()
    {
        return percent;
    }

    // Set the value, when bigger.
    public void setPercent(double p_percent)
    {
        if (p_percent < 0)
            return;
        else if(p_percent>100)
            percent = 100;
        else if (p_percent > percent)
            percent = p_percent;
    }
    
    public void addPercent(double p_percent)
    {
        setPercent(percent + p_percent);
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public boolean isCancle()
    {
        return STATUS_CANCEL.equals(status);
    }
    
    public boolean isInProgress()
    {
        return STATUS_INPROGRESS.equals(status);
    }
    
    public String getReportJobIDString()
    {
        return reportJobIDString;
    }

    public void setReportJobIDString(String reportJobIDString)
    {
        this.reportJobIDString = reportJobIDString;
    }

    public String getReportTypeString()
    {
        return reportTypeString;
    }

    public void setReportTypeString(String reportTypeString)
    {
        this.reportTypeString = reportTypeString;
    }

    /**
     * Return JOSN string for JS.
     * Remove user object for not using.
     */
    public String toJSON()
    {
        StringBuilder json = new StringBuilder("{");
        json.append("\"userId\":\"").append(getUserId()).append("\",")
            .append("\"percent\":").append(getPercent()).append(",")
            .append("\"status\":\"").append(getStatus()).append("\",");
        
        // Report Job ID List JSON
        String reportJobIdString = getReportJobIDString();
        if(reportJobIdString == null && reportJobIDS != null)
        {
            reportJobIdString = getReportJobIDS().toString();
        }
        json.append("\"reportJobIDS\":\"").append(reportJobIdString).append("\",");
        
        // Report Type List JSON
        String reportTypeListString = getReportTypeString();
        if(reportTypeListString == null && reportTypeList != null)
        {
            reportTypeListString = getReportTypeList().toString();
        }
        json.append("\"reportTypeList\":\"").append(reportTypeListString).append("\"");
        
        return json.append("}").toString();
    }
    
    @Override
    public String toString()
    {
        return "{reportJobIDS=" + reportJobIDS + ", reportTypeList="
                + reportTypeList + ", userId=" + userId + ", percent=" + percent
                + ", status=" + status + "}@ReportsData";
    }
}

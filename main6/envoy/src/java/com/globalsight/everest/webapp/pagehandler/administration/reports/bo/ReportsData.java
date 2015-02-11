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

import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.foundation.User;

public class ReportsData
{
    List<Long> reportJobIDS;            // Reports job id list
    List<String> reportTypeList;        // Reports type list
    User user;                          // Reports operator
    double percent;                     // Reports percent
    String status;                      // Reports status

    // Reports status constants
    public static final String STATUS_INPROGRESS = "inProgress";
    public static final String STATUS_CANCEL = "cancel";
    public static final String STATUS_FINISHED = "finished";

    public List<Long> getReportJobIDS()
    {
        return reportJobIDS;
    }

    public void setReportJobIDS(List<Long> reportJobIDS)
    {
        this.reportJobIDS = reportJobIDS;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
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

    public List<String> getReportTypeList()
    {
        return reportTypeList;
    }

    public void setReportTypeList(List<String> reportTypeList)
    {
        this.reportTypeList = reportTypeList;
    }
    
    /**
     * Return JOSN string for JS.
     * Remove user object for not using.
     */
    public String toJSON() throws JSONException
    {
        JSONObject jsonObj = new JSONObject(this);
        jsonObj.remove("user");
        String userId = null;
        if (user != null)
        {
            userId = user.getUserId();
        }
        jsonObj.append("userId", userId);
        
        return jsonObj.toString();
    }
    
    @Override
    public String toString()
    {
        return "ReportsData [reportJobIDS=" + reportJobIDS
                + ", reportTypeList=" + reportTypeList + ", user=" + user
                + ", percent=" + percent + ", status=" + status + "]";
    }
}

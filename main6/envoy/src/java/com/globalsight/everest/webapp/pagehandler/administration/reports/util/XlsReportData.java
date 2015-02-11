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
package com.globalsight.everest.webapp.pagehandler.administration.reports.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.poi.ss.usermodel.Sheet;

public class XlsReportData
{
    public boolean wantsAllJobs = false;
    public ArrayList jobIdList = new ArrayList();
    
    public boolean wantsAllProjects = false;
    public ArrayList projectIdList = new ArrayList();
    
    public ArrayList statusList = new ArrayList();
    
    public boolean wantsAllTargetLangs = false;
    public ArrayList targetLangList = new ArrayList();
    
    public ArrayList wrongJobs = new ArrayList();
    public ArrayList wrongJobNames = new ArrayList();
    public ArrayList ignoreJobs = new ArrayList();
    public Hashtable wrongJobMap = new Hashtable(); 
    
    public SimpleDateFormat dateFormat = null;
    
    public Sheet generalSheet = null;
    public Sheet dellSheet = null;
    public Sheet tradosSheet = null;
    
    public HashMap projectMap = new HashMap();
    
    public String[] headers = null;
    
    public boolean allActivities = false;
    public ArrayList activityNameList = new ArrayList();
    public boolean allJobStatus = false;
    public ArrayList jobStatusList = new ArrayList();
}

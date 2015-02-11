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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

public class JobSearchConstants
{
    // Fields on the page
    public static String NAME_FIELD = "nf";
    public static String NAME_OPTIONS = "no";
    public static String ACT_NAME_FIELD = "af";
    public static String ID_FIELD = "idf";
    public static String ID_GROUP = "idg";
    public static String ID_OPTIONS = "io";
    public static String STATUS_OPTIONS = "sto";
    public static String PROJECT_OPTIONS = "po";
    public static String SRC_LOCALE = "sl";
    public static String TARG_LOCALE = "tl";
    public static String PRIORITY_OPTIONS = "pro";
    public static String CREATION_START = "csf";
    public static String CREATION_START_OPTIONS = "cso";
    public static String CREATION_END = "cef";
    public static String CREATION_END_OPTIONS = "ceo";
    public static String EST_COMPLETION_START = "esf";
    public static String EST_COMPLETION_START_OPTIONS = "eso";
    public static String EST_COMPLETION_END = "eef";
    public static String EST_COMPLETION_END_OPTIONS = "eeo";
    public static String ACCEPTANCE_START = "asf";
    public static String ACCEPTANCE_START_OPTIONS = "aso";
    public static String ACCEPTANCE_END = "aef";
    public static String ACCEPTANCE_END_OPTIONS = "aeo";
    public static String COMPANY_OPTIONS = "comanyo";
    
    public static final String EXPORT_DATE_START = "edss";
    public static final String EXPORT_DATE_START_OPTIONS = "edso";
    public static final String EXPORT_DATE_END = "edee";
    public static final String EXPORT_DATE_END_OPTIONS = "edes";

    // Cookie and session attribute names
    public static final String MRU_JOBS = "mostRecentlyUsedJobs";
    public static final String MRU_TASKS = "mostRecentlyUsedTasks";
    public static final String MRU_JOBS_COOKIE = "mruJobs-";
    public static final String MRU_TASKS_COOKIE = "mruTasks-";
    public static final String JOB_SEARCH_COOKIE = "jobSearch-";
    public static final String MINI_JOB_SEARCH_COOKIE = "miniJobSearch-";
    public static final String TASK_SEARCH_COOKIE = "taskSearch-";
    public static final String MINI_TASK_SEARCH_COOKIE = "miniTaskSearch-";
    public static final String LAST_JOB_SEARCH_TYPE = "lastJobSearchType";
    public static final String LAST_TASK_SEARCH_TYPE = "lastTaskSearchType";
}

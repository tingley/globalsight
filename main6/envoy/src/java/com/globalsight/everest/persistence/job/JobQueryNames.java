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
package com.globalsight.everest.persistence.job;

/**
 * Specifies the names of all the named queries for Job.
 */
public interface JobQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all jobs.
     * <p>
     * Arguments: none.
     */
    public static String ALL_JOBS = "getAllJobs";
    
    /**
     * A named query to return all jobs of a specific state.
     * <p>
     * Arguments: 1: state as a string.
     */
    public static String ALL_JOBS_OF_SPECIFIC_STATE = "getJobsOfSpecificState";

    /**
     * A named query to return all jobs of one, two or three states.
     * <p>
     * Arguments: 1: one, two or three different states to return jobs for
     */
    public static String ALL_JOBS_OF_THESE_STATES = "getJobsOfTheseStates";

    /**
     * A named query to return all pending jobs.
     * <p>
     * Arguments: none.
     */
    public static String ALL_PENDING_JOBS = "getAllPendingJobs";

    /**
     * A named query to return all active jobs that have active workflows
     * with the source and target locale (locale pair) and company id (fix multi company issue)specified.
     * <p>
     * Arguments: 1: Source locale id
     *            2: Target locale id
     *            3: company id
     */
    public static String ALL_ACTIVE_JOBS_BY_LOCALE_PAIR = "getAllActiveJobsByLocalePair";
     

    /**
     * A named query to return jobs with any of the given states.
     * <p>
     * Arguments: 1: A List of states.
     */
    public static String JOBS_BY_STATE_LIST = "getJobsByStateList";

    /**
     * A named query to return jobs associated with the given manager.
     * <p>
     * Arguments: 1: Project Manager ID
     */
    public static String JOBS_BY_MANAGER_ID = "getJobsByManagerId";

    /**
     * A named query to return jobs associated with the given manager tht
     * have any of the given states.
     * <p>
     * Arguments: 1: Project Manager ID
     *            2: A List of states.
     */
    public static String JOBS_BY_MANAGER_ID_AND_STATE_LIST =
        "getJobsByManagerIdAndStateList";

    /**
     * A named query to return the first pending job that has the given
     * localization profile id and data source type.
     * <p>
     * Arguments: 1: L10Profile id.
     * Arguments: 2: Data Source Type 
     */
    public static String PENDING_JOB_BY_PROFILE_ID_AND_DATA_SOURCE_TYPE = 
    	"getPendingJobByProfileIdAndDataSourceType";
   	
    /**
     * A named query to return a job with a particular id.
     * <p>
     * Arguments: 1: Job id.
     */
    public static String JOB_BY_ID = "getJobById";

    /**
    *  A named query to return all the jobs for a particular project manager
    *  Arguments: 1: String user name
    */
    public static String JOBS_BY_PROJECT_MANAGER = "getJobsByProjectManager";

    /**
     * A named query to return the batch job with the specified batch id.
     * The job must be in the BATCH_RESERVED state.
     * Will either return the job or none if the job doesn't exist yet.
     * <p>
     * Arguments: 1: Batch Id
     */
    public static String BATCH_JOB_BY_BATCH_ID = "getBatchJobByBatchId";
    
    /**
     * A named query to return the job associated with the
     * specified source page.
     * <p>
     * Arguments: 1: Source Page Id
     */
    public static String JOB_BY_SOURCE_PAGE_ID = "getJobBySourcePageId";
}

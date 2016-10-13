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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;

/**
 * 
 * Process the reports generation.
 * 
 * @author Silver.Chen
 * 
 */
public interface ReportsProcessor
{
	public static final String REPORTS = "Reports";

	// For Job Status Report
	public static final String JOB_STATUS = "Job Status";

	public static final String PARAM_JOB_ID = "jobId";

	public static final String PARAM_TARGET_LOCALES_LIST = "targetLocalesList";

	public static final String PARAM_SELECTED_ALL = "*";

	public static final String PARAM_SELECTED_NONE = "-1";

	public static final String PARAM_PROJECT_ID = "projectId";

	public static final String PARAM_STATUS = "status";

	public static final String PARAM_CREATION_START = JobSearchConstants.CREATION_START;

	public static final String PARAM_CREATION_START_OPTIONS = JobSearchConstants.CREATION_START_OPTIONS;

	public static final String PARAM_CREATION_END = JobSearchConstants.CREATION_END;

	public static final String PARAM_CREATION_END_OPTIONS = JobSearchConstants.CREATION_END_OPTIONS;

	public static final String PARAM_DATE_FORMAT = "dateFormat";

	/**
     * Generates the Excel report.
     * 
     * @param p_request
     *            the http request.
     * @param p_response
     *            the http response.
     * 
     * @throws Exception
     */
	public void generateReport(HttpServletRequest p_request,
			HttpServletResponse p_response) throws Exception;
}

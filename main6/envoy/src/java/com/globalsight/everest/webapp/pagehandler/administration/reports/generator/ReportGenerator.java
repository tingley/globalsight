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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.util.List;

import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.util.GlobalSightLocale;

/**
 * Interface for all Report Generator
 * 
 * @author Joey Jiang
 */
public interface ReportGenerator extends ReportConstants
{
    public static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    /**
     * Generates report files.
     * 
     * @param p_jobIDS
     *            The Job ID List, used as report data.
     * @param p_targetLocales
     *            Target Locales List, used as report data.
     * @throws Exception
     */
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception;

    /**
     * Gets the report type of report generator.
     */
    public String getReportType();

    /**
     * Sets the percent of generating reports, used for showing progress
     * percent.
     * 
     * @param p_finishedJobNum
     *            Finished job number in the Report Generator.
     */
    public void setPercent(int p_finishedJobNum);

    /**
     * Report Generator justify whether cancel generating reports.
     */
    public boolean isCancelled();
}

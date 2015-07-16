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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;

/**
 * Report Generator Factory, 
 * which is used for creating ReportGenerator by Report Type.
 * 
 * @author Joey Jiang
 */
public class ReportGeneratorFactory
{
    private static Logger log = Logger.getLogger(ReportGeneratorFactory.class.getName());
    
    // The Map of report type and report generator class name.
    protected static final Map<String, String> reportGeneratorMap = new HashMap<String, String>();
    
    // Initial the map for ReportGenerator and Report Type
    static
    {
        reportGeneratorMap.put(ReportConstants.ACTIVITY_DURATION_REPORT,
                ActivityDurationReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.ONLINE_JOBS_REPORT, OnlineJobsReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR,
                OnlineJobsReportForIPTranslatorGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.DETAILED_WORDCOUNTS_REPORT,
                DetailedWordCountsByJobReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.REVIEWERS_COMMENTS_REPORT,
                ReviewersCommentsReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.REVIEWERS_COMMENTS_SIMPLE_REPORT,
                ReviewersCommentsSimpleReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.COMMENTS_ANALYSIS_REPORT,
                CommentsAnalysisReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.SCORECARD_REPORT,
                ScorecardReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.IMPLEMENTED_COMMENTS_CHECK_REPORT,
        		ImplementedCommentsCheckReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.CHARACTER_COUNT_REPORT, CharacterCountReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.TRANSLATIONS_EDIT_REPORT,
                TranslationsEditReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.SUMMARY_REPORT, SummaryReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.POST_REVIEW_QA_REPORT, PostReviewQAReportGenerator.class.getName());
        reportGeneratorMap.put(ReportConstants.TRANSLATION_VERIFICATION_REPORT,
                TranslationVerificationReportGenerator.class.getName());
    }

    public static ReportGenerator getReportGenerator(String p_reportType,
            HttpServletRequest p_request, HttpServletResponse p_response)
    {
        String reportGeneratorName = getReportGenaratorName(p_reportType);
        try
        {
            return (ReportGenerator) Class
                    .forName(reportGeneratorName)
                    .getDeclaredConstructor(HttpServletRequest.class, HttpServletResponse.class)
                    .newInstance(p_request, p_response);
        }
        catch (Exception e)
        {
            log.info("getReportGenerator error." + e);
        }

        return null;

    }

    public static String getReportGenaratorName(String p_reportType)
    {
        return reportGeneratorMap.get(p_reportType);
    }
}

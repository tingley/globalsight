package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import static org.junit.Assert.*;

import org.junit.Test;

import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;

public class ReportGeneratorFactoryTest
{
    @Test
    public void testGetReportGenaratorName()
    {
        String errorMsg = "Gets report name error on ";
        String reportType;
        Class<?> reportClass;
        
        reportType = ReportConstants.ONLINE_JOBS_REPORT;
        reportClass = OnlineJobsReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                        ReportGeneratorFactory.getReportGenaratorName(reportType)));
        
        reportType = ReportConstants.DETAILED_WORDCOUNTS_REPORT;
        reportClass = DetailedWordCountsByJobReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                        ReportGeneratorFactory.getReportGenaratorName(reportType)));
        
        reportType = ReportConstants.REVIEWERS_COMMENTS_REPORT;
        reportClass = ReviewersCommentsReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                        ReportGeneratorFactory.getReportGenaratorName(reportType)));
        
        reportType = ReportConstants.COMMENTS_ANALYSIS_REPORT;
        reportClass = CommentsAnalysisReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                        ReportGeneratorFactory.getReportGenaratorName(reportType)));
        
        reportType = ReportConstants.CHARACTER_COUNT_REPORT;
        reportClass = CharacterCountReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                ReportGeneratorFactory.getReportGenaratorName(reportType)));
        
        reportType = ReportConstants.TRANSLATIONS_EDIT_REPORT;
        reportClass = TranslationsEditReportGenerator.class;
        assertTrue(errorMsg + reportType, reportClass.getName().equalsIgnoreCase(
                ReportGeneratorFactory.getReportGenaratorName(reportType)));
    }
}

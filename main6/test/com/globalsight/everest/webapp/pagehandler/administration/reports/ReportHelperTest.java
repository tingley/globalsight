package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.everest.jobhandler.JobImpl;

public class ReportHelperTest
{
    private long m_jobId = 1;
    private String m_jobName = "testJobName";
    private String m_companyId = "22";
    private String m_str = "2,3,6,8,9";

    @Test
    public void testGetListOfLong()
    {
        List<?> list = ReportHelper.getListOfLong(m_str);
        Assert.assertTrue("The size is error.", list.size() == 5);
    }

    @Test
    public void testGetListOfStr()
    {
        List<?> list = ReportHelper.getListOfStr(m_str, ",");
        Assert.assertTrue("The size is error.", list.size() == 5);
    }

    @Test
    public void testGetReportFile()
    {
        JobImpl job = new JobImpl();
        job.setId(m_jobId);
        job.setJobName(m_jobName);
        job.setCompanyId(m_companyId);
        File file = ReportHelper.getReportFile(
                ReportConstants.REVIEWERS_COMMENTS_REPORT, job, ".xls");
        String expectedFileName = "ReviewersCommentsReport-[testJobName][1].xls";
        Assert.assertTrue("File name error." + file.getName(),
                expectedFileName.equalsIgnoreCase(file.getName()));
    }
}

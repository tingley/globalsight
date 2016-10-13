package com.globalsight.connector.blaise.vo;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class TranslationInboxEntryVoTest
{
    @Test
    public final void testGetJobIdLinks()
    {
        String expected = "";
        TranslationInboxEntryVo entry = new TranslationInboxEntryVo();
        String actual = entry.getJobIdLinks();
        Assert.assertEquals(expected, actual);

        expected = "<a class='standardHREF' target='_blank' href='/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=2132'>2132</a>";
        List<Long> jobIds = new ArrayList<Long>();
        jobIds.add(new Long(2132));
        entry.setJobIds(jobIds);
        actual = entry.getJobIdLinks();
        Assert.assertEquals(expected, actual);

        expected = "<a class='standardHREF' target='_blank' href='/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=2132'>2132</a>, <a class='standardHREF' target='_blank' href='/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=2134'>2134</a>, <a class='standardHREF' target='_blank' href='/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=2135'>2135</a>";
        jobIds.clear();
        jobIds.add(new Long(2132));
        jobIds.add(new Long(2134));
        jobIds.add(new Long(2135));
        actual = entry.getJobIdLinks();
        Assert.assertEquals(expected, actual);
    }

}

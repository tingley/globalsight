package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.util.ClassUtil;

public class JobSummaryHelperTest
{
    static JobSummaryHelper helper = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        helper = new JobSummaryHelper();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public final void testDecideBlaiseUploadState()
    {
        List<BlaiseConnectorJob> blaiseJobEntries = new ArrayList<BlaiseConnectorJob>();

        BlaiseConnectorJob failBcj = new BlaiseConnectorJob();
        failBcj.setUploadXliffState(BlaiseConnectorJob.FAIL);
        failBcj.setCompleteState(BlaiseConnectorJob.FAIL);

        BlaiseConnectorJob nullBcj = new BlaiseConnectorJob();
        nullBcj.setUploadXliffState(null);
        nullBcj.setCompleteState(null);

        BlaiseConnectorJob succeedBcj1 = new BlaiseConnectorJob();
        succeedBcj1.setUploadXliffState(BlaiseConnectorJob.SUCCEED);
        succeedBcj1.setCompleteState(BlaiseConnectorJob.SUCCEED);
        
        BlaiseConnectorJob succeedBcj2 = new BlaiseConnectorJob();
        succeedBcj2.setUploadXliffState(BlaiseConnectorJob.SUCCEED);
        succeedBcj2.setCompleteState(BlaiseConnectorJob.SUCCEED);

        // One fail, all fail
        blaiseJobEntries.add(succeedBcj1);
        blaiseJobEntries.add(nullBcj);
        blaiseJobEntries.add(failBcj);//
        blaiseJobEntries.add(succeedBcj2);
        String actual = (String) ClassUtil.testMethod(helper, "decideBlaiseUploadState",
                blaiseJobEntries);
        Assert.assertEquals(BlaiseConnectorJob.FAIL, actual);

        // One null, all null
        blaiseJobEntries.remove(failBcj);
        actual = (String) ClassUtil.testMethod(helper, "decideBlaiseUploadState", blaiseJobEntries);
        Assert.assertNull("There is entry that has never been uploaded", actual);

        // All succeed, then succeed
        blaiseJobEntries.remove(nullBcj);
        actual = (String) ClassUtil.testMethod(helper, "decideBlaiseUploadState", blaiseJobEntries);
        Assert.assertEquals(BlaiseConnectorJob.SUCCEED, actual);
    }

    @Test
    public final void testDecideBlaiseCompleteState()
    {
        List<BlaiseConnectorJob> blaiseJobEntries = new ArrayList<BlaiseConnectorJob>();

        BlaiseConnectorJob failBcj = new BlaiseConnectorJob();
        failBcj.setUploadXliffState(BlaiseConnectorJob.FAIL);
        failBcj.setCompleteState(BlaiseConnectorJob.FAIL);

        BlaiseConnectorJob nullBcj = new BlaiseConnectorJob();
        nullBcj.setUploadXliffState(null);
        nullBcj.setCompleteState(null);

        BlaiseConnectorJob succeedBcj1 = new BlaiseConnectorJob();
        succeedBcj1.setUploadXliffState(BlaiseConnectorJob.SUCCEED);
        succeedBcj1.setCompleteState(BlaiseConnectorJob.SUCCEED);
        
        BlaiseConnectorJob succeedBcj2 = new BlaiseConnectorJob();
        succeedBcj2.setUploadXliffState(BlaiseConnectorJob.SUCCEED);
        succeedBcj2.setCompleteState(BlaiseConnectorJob.SUCCEED);

        // One fail, all fail
        blaiseJobEntries.add(succeedBcj1);
        blaiseJobEntries.add(nullBcj);
        blaiseJobEntries.add(failBcj);//
        blaiseJobEntries.add(succeedBcj2);
        String actual = (String) ClassUtil.testMethod(helper, "decideBlaiseCompleteState",
                blaiseJobEntries);
        Assert.assertEquals(BlaiseConnectorJob.FAIL, actual);

        // One null, all null
        blaiseJobEntries.remove(failBcj);
        actual = (String) ClassUtil.testMethod(helper, "decideBlaiseCompleteState",
                blaiseJobEntries);
        Assert.assertNull("There is entry that has never been completed.", actual);

        // All succeed, then succeed
        blaiseJobEntries.remove(nullBcj);
        actual = (String) ClassUtil.testMethod(helper, "decideBlaiseCompleteState", blaiseJobEntries);
        Assert.assertEquals(BlaiseConnectorJob.SUCCEED, actual);
    }

}

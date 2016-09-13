package com.globalsight.connector.blaise.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ClassUtil;

public class BlaiseManagerTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public final void testGetBlaiseConnectorJobByJobIdEntryId() throws Exception
    {
        long jobId = 899999;
        long entryId = 999999;
        BlaiseConnectorJob bcj = new BlaiseConnectorJob();
        bcj.setBlaiseConnectorId(1);
        bcj.setBlaiseEntryId(entryId);
        bcj.setJobId(jobId);
        HibernateUtil.save(bcj);

        BlaiseManager manager = new BlaiseManager();
        BlaiseConnectorJob result = (BlaiseConnectorJob) ClassUtil.testMethod(manager,
                "getBlaiseConnectorJobByJobIdEntryId", jobId, entryId);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getBlaiseEntryId(), entryId);
        Assert.assertEquals(result.getJobId(), jobId);

        HibernateUtil.delete(result);
    }
}

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.everest.request;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.jobhandler.JobImpl;

import junit.framework.TestCase;

public class RequestImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10398);

    private List getIds()
    {
        ArrayList idList = new ArrayList();
        if (ids.size() == 0)
        {
            idList.add(defaultId);
        }
        else
        {
            idList = new ArrayList(ids);
        }
        return idList;
    }

    public void testSave() throws Exception
    {
        RequestImpl impl = new RequestImpl();
        
        BasicL10nProfile l = new BasicL10nProfile();
        l.setId(1);
        
        impl.setType(-2);
        impl.setDataSourceId(-1);
        impl.setIsPageCxePreviewable(true);
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setCompanyId("2");

        impl.setL10nProfile(l);
        
        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        SourcePage source = new SourcePage();
        source.setId(1);

        RequestImpl impl = new RequestImpl();

        BasicL10nProfile l = new BasicL10nProfile();
        l.setId(1);
        JobImpl job = new JobImpl();
        job.setId(1);
        BatchInfo batchInfo = new BatchInfo("s", 1,1,1,1,"s");
        
        impl.setType(-2);
        impl.setDataSourceId(-1);
        impl.setIsPageCxePreviewable(true);
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setCompanyId("2");
        impl.setEventFlowXml("x");
        impl.setExceptionAsString("x");
        impl.setJob(job);
        impl.setSourcePageId(new Long(1));
        impl.setDataSourceId(1);
        impl.setIsPageCxePreviewable(true);
        impl.setBatchInfo(batchInfo);
        impl.setBaseHref("f");             

        impl.setL10nProfile(l);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testGet() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            System.out.println("id: " + id);
            RequestImpl object = (RequestImpl) HibernateUtil.get(
                    RequestImpl.class, id);
            System.out.println(object.getEventFlowXml());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            RequestImpl impl = new RequestImpl();
            Long id = (Long) ids.get(i);
            impl.setId(id.longValue());

            BasicL10nProfile l = new BasicL10nProfile();
            l.setId(1);
            
            impl.setType(-2);
            impl.setDataSourceId(-1);
            impl.setIsPageCxePreviewable(true);
            impl.setTimestamp(new Timestamp(new Date().getTime()));
            impl.setCompanyId("2");

            impl.setL10nProfile(l);

            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);
        }
    }
}

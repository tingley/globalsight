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

package com.globalsight.cxe.entity.segmentationrulefile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class SegmentationRuleFileImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10165);

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
        SegmentationRuleFileImpl impl = new SegmentationRuleFileImpl();
        impl.setName("s");
        impl.setCompanyId("1");
        impl.setType(1);
        impl.setRuleText("sdf");
        impl.setIsActive(true);

        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        SegmentationRuleFileImpl impl = new SegmentationRuleFileImpl();
        impl.setName("s");
        impl.setCompanyId("1");
        impl.setType(1);
        impl.setRuleText("sdf");
        impl.setIsActive(true);
        impl.setDescription("d");
        impl.setRuleText("text");

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
            SegmentationRuleFileImpl impl = (SegmentationRuleFileImpl) HibernateUtil
                    .get(SegmentationRuleFileImpl.class, id);
            System.out.println(impl.getName());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            SegmentationRuleFileImpl impl = new SegmentationRuleFileImpl();
            impl.setId(id.longValue());

            impl.setName("s");
            impl.setName("s");
            impl.setCompanyId("1");
            impl.setType(1);
            impl.setRuleText("sdf");
            impl.setIsActive(false);
            HibernateUtil.delete(impl);
        }
    }
}

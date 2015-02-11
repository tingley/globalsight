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

package com.globalsight.cxe.entity.databaseprofile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class DatabaseProfileImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10001);

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
        DatabaseProfileImpl impl = new DatabaseProfileImpl();
        impl.setName("s");
        impl.setL10nProfileId(12);
        Date date = new Date();
        impl.setTimestamp(new Timestamp(date.getTime()));

        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        DatabaseProfileImpl impl = new DatabaseProfileImpl();
        impl.setName("s2");
        impl.setL10nProfileId(12);
        Date date = new Date();
        impl.setTimestamp(new Timestamp(date.getTime()));
        impl.setDescription("d");
        impl.setCheckOutSql("s");
        impl.setCheckOutConnectionProfileId(1);
        impl.setPreviewInsertSql("p");
        impl.setPreviewUpdateSql("q");
        impl.setPreviewConnectProfileId(new Long(1));
        impl.setCheckInConnectionProfileId(1);
        impl.setCheckInInsertSql("s");
        impl.setCheckInUpdateSql("u");
        impl.setPreviewUrlId(1);
        impl.setCodeSet("c");

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
            DatabaseProfileImpl impl = (DatabaseProfileImpl) HibernateUtil.get(
                    DatabaseProfileImpl.class, id);
            System.out.println(impl.getName());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            DatabaseProfileImpl impl = new DatabaseProfileImpl();
            impl.setName("s");
            impl.setL10nProfileId(12);
            impl.setId(id.longValue());
            Date date = new Date();
            impl.setTimestamp(new Timestamp(date.getTime()));
            HibernateUtil.delete(impl);
        }
    }
}

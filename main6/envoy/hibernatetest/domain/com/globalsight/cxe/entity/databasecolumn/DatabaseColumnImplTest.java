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

package com.globalsight.cxe.entity.databasecolumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class DatabaseColumnImplTest extends TestCase
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
        DatabaseColumnImpl impl = new DatabaseColumnImpl();
        impl.setColumnName("a");
        impl.setColumnNumber(12);
        impl.setDatabaseProfileId(12);
        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        DatabaseColumnImpl impl = new DatabaseColumnImpl();
        impl.setColumnName("b");
        impl.setColumnNumber(12);
        impl.setDatabaseProfileId(12);
        impl.setFormatType(1);
        impl.setTableName("t");
        impl.setXmlRuleId(1);
        impl.setContentMode(1);
        impl.setLabel("l");

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
            DatabaseColumnImpl impl = (DatabaseColumnImpl) HibernateUtil.get(
                    DatabaseColumnImpl.class, id);
            System.out.println(impl.getColumnName());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            DatabaseColumnImpl impl = new DatabaseColumnImpl();
            impl.setColumnName("a");
            impl.setColumnNumber(12);
            impl.setDatabaseProfileId(12);
            impl.setId(id.longValue());
            HibernateUtil.delete(impl);
        }
    }
}

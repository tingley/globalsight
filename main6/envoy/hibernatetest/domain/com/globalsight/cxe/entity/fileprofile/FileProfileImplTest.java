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

package com.globalsight.cxe.entity.fileprofile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Timestamp;
import java.util.*;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class FileProfileImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10553);

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
        FileProfileImpl impl = new FileProfileImpl();
        impl.setName("s");
        impl.setCompanyId("12");
        impl.setKnownFormatTypeId(1);
        impl.setL10nProfileId(1);
        impl.setByDefaultExportStf(false);
        Date date = new Date();
        impl.setTimestamp(new Timestamp(date.getTime()));
        impl.setIsActive(true);

        Set ens = new HashSet();
        ens.add(new Long(12));
        ens.add(new Long(13));
        impl.setExtensionIds(ens);

        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        FileProfileImpl impl = new FileProfileImpl();
        impl.setName("s");
        impl.setCompanyId("12");
        impl.setKnownFormatTypeId(1);
        impl.setL10nProfileId(1);
        impl.setByDefaultExportStf(false);
        Date date = new Date();
        impl.setTimestamp(new Timestamp(date.getTime()));
        impl.setIsActive(true);
        impl.setKnownFormatTypeId(1);
        impl.setCodeSet("s");
        impl.setXmlRuleFileId(1);
        impl.setScriptOnExport("s");
        impl.setScriptOnImport("e");

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
            FileProfileImpl impl = (FileProfileImpl) HibernateUtil.get(
                    FileProfileImpl.class, id);
            System.out.println(impl.getName());

            Set es = impl.getExtensionIds();
            Iterator iterator = es.iterator();
            while (iterator.hasNext())
            {
                System.out.println(iterator.next());
            }

        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            FileProfileImpl impl = new FileProfileImpl();
            impl.setName("s");
            impl.setCompanyId("12");
            impl.setKnownFormatTypeId(1);
            impl.setL10nProfileId(1);
            impl.setByDefaultExportStf(false);
            Date date = new Date();
            impl.setTimestamp(new Timestamp(date.getTime()));
            impl.setIsActive(true);
            impl.setId(id.longValue());
            HibernateUtil.delete(impl);
        }
    }
}

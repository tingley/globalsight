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

package com.globalsight.everest.page;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.UnextractedFile;
import junit.framework.TestCase;

public class SourcePageTest extends TestCase
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
        SourcePage source = new SourcePage();
        source.setId(1);

        SourcePage impl = new SourcePage();

        impl.setExternalPageId("aa");
        impl.setPageState("LOCALIZED");
        impl.setCompanyId("111");
        impl.setTimestamp(new Timestamp(new Date().getTime()));

        HibernateUtil.saveOrUpdate(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        ExtractedSourceFile e = new ExtractedSourceFile();
        e.setOriginalCodeSet("s");
        e.setDataType("s");
        e.setInternalBaseHref("b");
        e.setExternalBaseHref("s");
        e.setContainGsTags(Boolean.TRUE);
        e.setGxmlVersion("ss");
        // e.set

        UnextractedFile u = new UnextractedFile();
        u.setStoragePath("path");
        u.setLastModifiedBy("s");
        u.setLastModifiedDate(new Date());
        u.setFileLength(23L);

        SourcePage source = new SourcePage();
        source.setId(1);

        SourcePage impl = new SourcePage();

        impl.setExternalPageId("aa");
        impl.setPageState("LOCALIZED");
        impl.setCompanyId("111");
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setWordCount(1);
        impl.setDataSourceType("s");
        impl.setPreviousPageId(1);
        impl.setCuvId(new Long(12));
        impl.setOverrideWordCount(new Integer(1));
        impl.setPrevStateBeforeUpdate("LOCALIZED");
        impl.setCompanyId("1");
        
        impl.setPrimaryFile(u);
        impl.setExtractedFile(e);


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
            SourcePage object = (SourcePage) HibernateUtil.get(
                    SourcePage.class, id);
            System.out.println(object.getPageState());
            System.out.println(object.getPrimaryFileType());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            SourcePage impl = new SourcePage();
            Long id = (Long) ids.get(i);
            impl.setId(id.longValue());

            impl.setExternalPageId("aa");
            impl.setPageState("LOCALIZED");
            impl.setCompanyId("111");
            impl.setTimestamp(new Timestamp(new Date().getTime()));

            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);
        }
    }
}

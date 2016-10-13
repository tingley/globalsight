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

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.page.PageWordCounts;

public class TargetPageTest extends TestCase
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

        TargetPage impl = new TargetPage();

        impl.setExternalPageId("aa");
        impl.setPageState("LOCALIZED");
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setExportSubDir("e");
        impl.setSourcePage(source);
        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        PageWordCounts c = new PageWordCounts();
        
        WorkflowImpl w = new WorkflowImpl();
        w.setId(1);
        
        ExtractedTargetFile e = new ExtractedTargetFile();
 
        e.setInternalBaseHref("b");
        e.setExternalBaseHref("s");
        e.setGxmlVersion("ss");
        
        // e.set

        UnextractedFile u = new UnextractedFile();
        u.setStoragePath("path");
        u.setLastModifiedBy("s");
        u.setLastModifiedDate(new Date());
        u.setFileLength(23L);

        SourcePage source = new SourcePage();
        source.setId(1);

        TargetPage impl = new TargetPage();
        impl.setExternalPageId("aa");
        impl.setPageState("LOCALIZED");
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        
        impl.setErrorAsString("error");
        impl.setExportSubDir("e");
        
        impl.setPrimaryFile(u);
        impl.setExtractedFile(e);
        impl.setWorkflowInstance(w);
        impl.setWordCount(c);
        impl.setSourcePage(source);
        


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
            TargetPage object = (TargetPage) HibernateUtil.get(
                    TargetPage.class, id);
            System.out.println(object.getExportSubDir());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            TargetPage impl = new TargetPage();
            Long id = (Long) ids.get(i);
            impl.setId(id.longValue());

            impl.setExternalPageId("aa");
            impl.setPageState("LOCALIZED");
            impl.setTimestamp(new Timestamp(new Date().getTime()));
            impl.setExportSubDir("e");
            SourcePage source = new SourcePage();
            source.setId(1);
            impl.setSourcePage(source);

            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);
        }
    }
}

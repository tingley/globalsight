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

package com.globalsight.everest.corpus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.corpus.CorpusDoc;

import junit.framework.TestCase;

public class CorpusDocGroupTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10469);

    public void testSave() throws Exception
    {
        CorpusDoc doc = new CorpusDoc();
        GlobalSightLocale locale = (GlobalSightLocale) HibernateUtil.get(
                GlobalSightLocale.class, 1);
        doc.setStoreDate(new java.util.Date());
        doc.setGlobalSightLocale(locale);
        doc.setIsMapped(true);

        Set docs = new HashSet();
        docs.add(doc);

        CorpusDocGroup impl = new CorpusDocGroup();
        impl.setCorpusName("name");
        impl.setDocs(docs);

        doc.setCorpusDocGroup(impl);
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
            CorpusDocGroup impl = (CorpusDocGroup) HibernateUtil.get(
                    CorpusDocGroup.class, id);
            System.out.println(impl.getCorpusName());
            // Set s = impl.getDocs();
            // System.out.println(s.size());
            // List l = new ArrayList(s);
            // CorpusDoc doc = (CorpusDoc) l.get(0);
            // System.out.println(doc.getIsMapped());
        }
    }

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

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            CorpusDocGroup impl = (CorpusDocGroup) HibernateUtil.get(
                    CorpusDocGroup.class, id);

            HibernateUtil.delete(impl);
        }
    }
}

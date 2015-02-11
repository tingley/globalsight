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

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.corpus.CorpusDoc;

import junit.framework.TestCase;
import java.util.*;

public class CorpusContextTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10001);

    public void testSave() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setLanguage("ar");
        locale.setId(1);

        CorpusDocGroup corpusDocGroup = new CorpusDocGroup();
        corpusDocGroup.setCorpusName("name");
       
        CorpusDoc doc = new CorpusDoc();
        doc.setStoreDate(new java.util.Date());
        doc.setGlobalSightLocale(locale);
        doc.setIsMapped(false);
        doc.setCorpusDocGroup(corpusDocGroup);

        CorpusContext impl = new CorpusContext();
        impl.setCorpusDoc(doc);

        impl.setTuId(new Long(1));
        impl.setTuvId(new Long(2));
        impl.setPartialContext("c");
        impl.setLinkDate(new Date());

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
            CorpusContext impl = (CorpusContext) HibernateUtil.get(
                    CorpusContext.class, id);
            System.out.println(impl.getPartialContext());
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
            CorpusContext impl = new CorpusContext();
            impl.setId(id.longValue());

            impl.setCuvId(new Long(100));
            impl.setTuId(new Long(1));
            impl.setTuvId(new Long(2));
            impl.setPartialContext("c");
            impl.setLinkDate(new Date());

            HibernateUtil.delete(impl);
        }
    }
}

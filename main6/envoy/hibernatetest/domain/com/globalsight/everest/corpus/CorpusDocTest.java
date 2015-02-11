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

import junit.framework.TestCase;
import com.globalsight.util.GlobalSightLocale;

public class CorpusDocTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10001);
    
    public void testSave() throws Exception
    {
        CorpusDocGroup corpusDocGroup = new CorpusDocGroup();
//        corpusDocGroup.setId(1000);
        corpusDocGroup.setCorpusName("name");
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setLanguage("en");
        locale.setId(1001);
        
        CorpusDoc impl = new CorpusDoc();
        impl.setCorpusDocGroup(corpusDocGroup);
        impl.setStoreDate(new java.util.Date());
        impl.setGlobalSightLocale(locale);
        impl.setIsMapped(false);

        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSavenotNull() throws Exception
    {
        CorpusDocGroup corpusDocGroup = new CorpusDocGroup();
        corpusDocGroup.setId(1000);
        corpusDocGroup.setCorpusName("name");
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setLanguage("en");
        locale.setId(1001);
        
        CorpusDoc impl = new CorpusDoc();
        impl.setCorpusDocGroup(corpusDocGroup);
        impl.setStoreDate(new java.util.Date());
        impl.setGlobalSightLocale(locale);
        impl.setIsMapped(false);
        impl.setNativeFormatPath("path");

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
            CorpusDoc impl = (CorpusDoc) HibernateUtil.get(
                    CorpusDoc.class, id);

            System.out.println(impl.getStoreDate());
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
            CorpusDoc impl = new CorpusDoc();
            impl.setId(id.longValue());

            CorpusDocGroup corpusDocGroup = new CorpusDocGroup();
            corpusDocGroup.setId(1000);
            corpusDocGroup.setCorpusName("name");
            GlobalSightLocale locale = new GlobalSightLocale();
            locale.setLanguage("en");
            locale.setId(1001);

            impl.setCorpusDocGroup(corpusDocGroup);
            impl.setStoreDate(new java.util.Date());
            impl.setGlobalSightLocale(locale);
            impl.setIsMapped(false);

            HibernateUtil.delete(impl);
        }
        
    }
}

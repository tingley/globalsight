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

import java.lang.reflect.*;
import java.rmi.RemoteException;
import java.util.Collection;
import java.io.*;
import java.util.*;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.nativefilestore.NativeFileManagerWLRMIImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.corpusinterface.TuvMapping;

import junit.framework.TestCase;

public class CorpusManagerLocalTest extends TestCase
{
    CorpusManagerLocal local = new CorpusManagerLocal();

    public void setUp() throws Exception
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");

        Field f = CorpusManagerLocal.class.getDeclaredField("m_isInstalled");
        f.setAccessible(true);
        f.setBoolean(local, true);

        NativeFileManagerWLRMIImpl fileManager = new NativeFileManagerWLRMIImpl();
        f = ServerProxy.class.getDeclaredField("m_nativeFileManager");
        f.setAccessible(true);
        ServerProxy proxy = new ServerProxy();
        f.set(proxy, fileManager);

    }

    public void testGetCorpusDocById() throws CorpusException, RemoteException
    {
        CorpusDoc doc = local.getCorpusDoc(new Long(10511));
        System.out.println(doc.getGlobalSightLocale().getId());
    }

    public void testGetCorpusDocByName() throws CorpusException,
            RemoteException
    {
        Collection result = local.getCorpusDoc("name");
        System.out.println(result.size());
    }

    public void testAddNewSourceLanguageCorpusDoc() throws Exception
    {
        GlobalSightLocale locale = (GlobalSightLocale) HibernateUtil.get(
                GlobalSightLocale.class, 33);
        File f = new File("C:/ambassador/filestore/GlobalSight/Corpus/"
                + "sdfsdf/10575/fr_CA/Arabic_on.doc.gxml");
        FileInputStream in = new FileInputStream(f);
        byte[] bs = new byte[in.available()];
        in.read(bs);
        File doc = new File("C:/ambassador/filestore/GlobalSight/Corpus/"
                + "sdfsdf/10575/fr_CA/Arabic_on.doc");

        local.addNewSourceLanguageCorpusDoc(locale, "Arabic_on.doc",
                new String(bs), doc, true);
    }

    public void testAddNewTargetLanguageCorpusDoc() throws Exception
    {
        CorpusDoc doc = local.getCorpusDoc(new Long(10619));
        File f = new File("C:/ambassador/filestore/GlobalSight/Corpus/"
                + "sdfsdf/10575/fr_CA/Arabic_on.doc.gxml");
        FileInputStream in = new FileInputStream(f);
        byte[] bs = new byte[in.available()];
        File docs = new File("C:/ambassador/filestore/GlobalSight/Corpus/"
                + "sdfsdf/10575/fr_CA/Arabic_on.doc");

        GlobalSightLocale locale = (GlobalSightLocale) HibernateUtil.get(
                GlobalSightLocale.class, 34);
        local.addNewTargetLanguageCorpusDoc(doc, locale, new String(bs),
                docs, true);
    }

    public void testGetCorpusContextsForSegment() throws CorpusException,
            RemoteException
    {
        ArrayList l = local.getCorpusContextsForSegment(2, 1);
        System.out.println(l.size());
    }

    // need tu tuv.
    public void testMapSegmentsToCorpusDoc() throws Exception
    {
        List s = new ArrayList();
        
        TuvMapping tuv = new TuvMapping(1,1,1,1,1);
        s.add(tuv);
        CorpusDoc doc = local.getCorpusDoc(new Long(10619));
        local.mapSegmentsToCorpusDoc(s, doc);
    }

    public void testRemoveSourceCorpusDoc() throws Exception
    {
        SourcePage object = (SourcePage) HibernateUtil.get(SourcePage.class,
                10555);
        local.removeSourceCorpusDoc(object);
        object = (SourcePage) HibernateUtil.get(SourcePage.class,
                10555);
        assertNull(object.getCuvId());
    }

    // need job
    public void testCleanUpMsOfficeJobSourcePages() throws CorpusException,
            RemoteException
    {

    }

    public void testRemoveCorpusDoc() throws CorpusException, RemoteException
    {
        local.removeCorpusDoc(new Long(10632));
    }
}

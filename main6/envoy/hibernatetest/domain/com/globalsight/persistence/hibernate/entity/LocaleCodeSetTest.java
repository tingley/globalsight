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

package com.globalsight.persistence.hibernate.entity;

import java.util.List;
import java.util.HashMap;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.localemgr.CodeSetImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class LocaleCodeSetTest extends TestCase
{

    public void testSave() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", false);
        locale.setId(-11);
        CodeSetImpl code = new CodeSetImpl();
        code.setId(-11);
        code.setCode_set("s");

        LocaleCodeSet impl = new LocaleCodeSet();
        impl.setLocal(locale);
        impl.setCodeSet(code);

        HibernateUtil.save(impl);
    }

    public void testGet() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", false);
        locale.setId(-11);
        CodeSetImpl code = new CodeSetImpl();
        code.setId(-11);
        code.setCode_set("s");


        String hql = "from LocaleCodeSet c where c.local = :locale and c.codeSet = :code";
        HashMap map = new HashMap();
        map.put("locale", locale);
        map.put("code", code);

        List codeSets = HibernateUtil.search(hql, map);
        LocaleCodeSet impl = (LocaleCodeSet) codeSets.get(0);
        System.out.println(impl.getLocal().getId());

    }

    public void testDelete() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", false);
        locale.setId(-11);
        CodeSetImpl code = new CodeSetImpl();
        code.setId(-11);
        code.setCode_set("s");

        LocaleCodeSet impl = new LocaleCodeSet();
        impl.setLocal(locale);
        impl.setCodeSet(code);

        HibernateUtil.delete(impl);
    }
}

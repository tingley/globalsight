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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class PagePersistenceAccessorTest extends TestCase
{
    public void setUp()
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");
    }
    
    public void testGetTemplateParts()
    {
        Collection c = PagePersistenceAccessor.getTemplateParts(new Long(1), "EXP");
        System.out.println(c.size());
    }
    
    public void testGetTargetPage()
    {
        TargetPage t = PagePersistenceAccessor.getTargetPage(10660, 23);
        System.out.println(t.getId());
    }
    
    public void testResetPagesToPreviousState() throws Exception
    {
        SourcePage page = (SourcePage)HibernateUtil.get(SourcePage.class, 10555);

        ArrayList pages = new ArrayList();
        pages.add(page);
        
        PagePersistenceAccessor.resetPagesToPreviousState(pages);
    }
    
    public void testGetTargetPages()
    {
        System.out.println(PagePersistenceAccessor.getTargetPages(10660).size());       
    }
    
    public void testGetTargetPageById()
    {
        TargetPage page = PagePersistenceAccessor.getTargetPageById(10748);
        System.out.println(page.getId());
    }
}


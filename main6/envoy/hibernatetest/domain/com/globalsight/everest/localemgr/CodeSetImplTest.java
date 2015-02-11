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

package com.globalsight.everest.localemgr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class CodeSetImplTest extends TestCase
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
        CodeSetImpl impl = new CodeSetImpl();

        impl.setCode_set("set");

        
        HibernateUtil.save(impl);

        ids.add(impl.getCodeSet());
        System.out.println("Save id: " + impl.getCodeSet());
    }
    
    public void testGet() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            String id = (String)ids.get(i);
            System.out.println("id: " + id);
            CodeSetImpl object = (CodeSetImpl) HibernateUtil.get(
                    CodeSetImpl.class, id);
            System.out.println(object.getCode_set());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            CodeSetImpl impl = new CodeSetImpl();

            
            String id = (String)ids.get(i);
            impl.setCode_set(id);
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

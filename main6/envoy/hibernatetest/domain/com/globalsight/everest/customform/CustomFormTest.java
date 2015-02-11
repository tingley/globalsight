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

package com.globalsight.everest.customform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

import junit.framework.TestCase;

public class CustomFormTest extends TestCase
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
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(1);
        
        CustomForm impl = new CustomForm();
        impl.setName("name");
        impl.setModifierUserId("1");
        impl.setModifiedDate(new java.util.Date());
        impl.setLocale(locale);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSaveNoNull() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(1);
        
        CustomForm impl = new CustomForm();
        impl.setName("name");
        impl.setModifierUserId("1");
        impl.setModifiedDate(new java.util.Date());
        impl.setLocale(locale);
        impl.setXmlDesign("xml");


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
            CustomForm object = (CustomForm) HibernateUtil.get(
                    CustomForm.class, id);
            System.out.println(object.getModifierUserId());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            CustomForm impl = new CustomForm();

            impl.setName("name");
            impl.setModifierUserId("1");
            impl.setModifiedDate(new java.util.Date());
            
            GlobalSightLocale locale = new GlobalSightLocale();
            locale.setId(1);
            impl.setLocale(locale);
            
            Long id = (Long)ids.get(i);
            impl.setId(id.longValue());
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

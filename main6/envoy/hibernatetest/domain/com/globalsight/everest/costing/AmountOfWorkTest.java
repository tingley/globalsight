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

package com.globalsight.everest.costing;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.taskmanager.TaskImpl;

import junit.framework.TestCase;

public class AmountOfWorkTest extends TestCase
{
    private static Long id;
    private static Long defaultId = new Long(10001);
    
    public void testSave() throws Exception
    {
        AmountOfWork impl = new AmountOfWork();
        impl.setStringUnitOfWork("F");
        impl.setEstimatedAmount(12);
        impl.setActualAmount(13);
        
        TaskImpl task = new TaskImpl();
        task.setId(12);
        impl.setTask(task);

        HibernateUtil.save(impl);
        id = new Long(impl.getId());
    }

    private Long getId()
    {
        return id == null? defaultId: id;
    }
    
    public void testGet() throws Exception
    {        
        AmountOfWork i = (AmountOfWork) HibernateUtil.get(AmountOfWork.class, getId());

        System.out.println(i.getStringUnitOfWork());
    }

    public void testDelete() throws Exception
    {
        AmountOfWork impl = new AmountOfWork();
        impl.setId(getId().longValue());

        impl.setStringUnitOfWork("F");
        impl.setEstimatedAmount(12);
        impl.setActualAmount(13);
        
        TaskImpl task = new TaskImpl();
        task.setId(12);
        impl.setTask(task);

        HibernateUtil.delete(impl);
    }
}

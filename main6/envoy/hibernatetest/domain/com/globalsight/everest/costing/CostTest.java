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

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import java.util.*;

import junit.framework.TestCase;

public class CostTest extends TestCase
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
        WorkflowImpl w = new WorkflowImpl();
        w.setId(2);
        TaskImpl t = new TaskImpl();
        t.setId(2);
        
        Cost impl = new Cost();
        impl.setObject(w);
        impl.setEstimatedCost(1);
        
        IsoCurrency isoCurrency = new IsoCurrency();
        isoCurrency.setCode("c");
        Currency currency = new Currency(isoCurrency,1,"11");
        currency.setId(1);
        impl.setCurrency(currency);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSaveNoNull() throws Exception
    {
        TaskImpl t = new TaskImpl();
        t.setId(2);
        
        Cost impl = new Cost();
        impl.setObject(t);
        impl.setEstimatedCost(1);
        impl.setActualCostAsFloat(1);
        
        IsoCurrency isoCurrency = new IsoCurrency();
        isoCurrency.setCode("c");
        Currency currency = new Currency(isoCurrency,1,"11");
        currency.setId(1);
        impl.setCurrency(currency);

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
            Cost object = (Cost) HibernateUtil.get(
                    Cost.class, id);
            System.out.println("objectId: " + object.getObject().getId());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Cost impl = new Cost();

            TaskImpl t = new TaskImpl();
            t.setId(2);
            impl.setObject(t);
            impl.setEstimatedCost(1);
            
            IsoCurrency isoCurrency = new IsoCurrency();
            isoCurrency.setCode("c");
            Currency currency = new Currency(isoCurrency,1,"11");
            currency.setId(1);
            impl.setCurrency(currency);
            
            Long id = (Long)ids.get(i);
            impl.setId(id.longValue());
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

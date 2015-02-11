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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.foundation.LocalePair;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class RateTest extends TestCase
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
        IsoCurrency isoCurrency = new IsoCurrency();
        isoCurrency.setCode("c");
        Currency currency = new Currency(isoCurrency,1,"11");
        currency.setId(1);
        Activity activity = new Activity();
        activity.setId(1);
        LocalePair pair = new LocalePair();
        pair.setId(1);
        
        Rate impl = new Rate();
        impl.setName("name");
        impl.setActivity(activity);
        impl.setLocalePair(pair);
        impl.setContextMatchRate(1);
        impl.setSegmentTmRate(1);
        impl.setLowFuzzyMatchRate(1);
        impl.setMedFuzzyMatchRate(1);
        impl.setHiFuzzyMatchRate(1);
        impl.setMedHiFuzzyMatchRate(1);
        impl.setNoMatchRate(1);
        impl.setNoMatchRepetitionRate(1);
        impl.setUnitRate(1);
        impl.setIsActive(true);

        impl.setCurrency(currency);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSaveNoNull() throws Exception
    {
        IsoCurrency isoCurrency = new IsoCurrency();
        isoCurrency.setCode("c");
        Currency currency = new Currency(isoCurrency,1,"11");
        currency.setId(1);
        Activity activity = new Activity();
        activity.setId(1);
        LocalePair pair = new LocalePair();
        pair.setId(1);
        
        Rate impl = new Rate();
        impl.setName("name");
        impl.setCurrency(currency);
        impl.setType("F");
        impl.setActivity(activity);
        impl.setLocalePair(pair);
        impl.setContextMatchRate(1);
        impl.setSegmentTmRate(1);
        impl.setLowFuzzyMatchRate(1);
        impl.setMedFuzzyMatchRate(1);
        impl.setHiFuzzyMatchRate(1);
        impl.setMedHiFuzzyMatchRate(1);
        impl.setNoMatchRate(1);
        impl.setNoMatchRepetitionRate(1);
        impl.setUnitRate(1);
        impl.setIsActive(true);

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
            Rate object = (Rate) HibernateUtil.get(
                    Rate.class, id);
            System.out.println("name: " + object.getName());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();
        Activity activity = new Activity();
        activity.setId(1);
        LocalePair pair = new LocalePair();
        pair.setId(1);

        for (int i = 0; i < ids.size(); i++)
        {
            Rate impl = new Rate();

            impl.setName("name");
            impl.setActivity(activity);
            impl.setLocalePair(pair);
            impl.setContextMatchRate(1);
            impl.setSegmentTmRate(1);
            impl.setLowFuzzyMatchRate(1);
            impl.setMedFuzzyMatchRate(1);
            impl.setHiFuzzyMatchRate(1);
            impl.setMedHiFuzzyMatchRate(1);
            impl.setNoMatchRate(1);
            impl.setNoMatchRepetitionRate(1);
            impl.setUnitRate(1);
            impl.setIsActive(true);
            
            Long id = (Long)ids.get(i);
            impl.setId(id.longValue());
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

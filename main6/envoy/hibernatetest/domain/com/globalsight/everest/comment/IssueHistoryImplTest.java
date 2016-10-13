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

package com.globalsight.everest.comment;

import java.util.*;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class IssueHistoryImplTest extends TestCase
{
    private static Map map = new HashMap();
    
    public void testSave() throws Exception
    {
        IssueImpl issue = new IssueImpl();
        issue.setId(1000);
        
        IssueHistoryImpl impl = new IssueHistoryImpl();
        impl.setDateReported(new Date());
        impl.setReportedBy("s");
        impl.setIssue(issue);

        HibernateUtil.save(impl);
        
        map.put(new Long(impl.getIssue().getId()), impl.getDateReported());

        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        IssueImpl issue = new IssueImpl();
        issue.setId(1002);
        
        IssueHistoryImpl impl = new IssueHistoryImpl();
        impl.setDateReported(new Date());
        impl.setComment("c");
        impl.setReportedBy("s");
        impl.setIssue(issue);

        HibernateUtil.save(impl);
        map.put(new Long(impl.getIssue().getId()), impl.getDateReported());
        System.out.println("Save id: " + impl.getId());
    }

    public void testGet()
    {
        IssueHistoryImpl impl;
        try
        {
            Set keys = map.keySet();
            Iterator ids = keys.iterator();
            while (ids.hasNext())
            {
                Long id = (Long) ids.next();
                Date date = (Date)map.get(id);
                String hql = "from IssueHistoryImpl ih where ih.issue.id=:id and ih.dateReported = :date";
                HashMap map = new HashMap();
                map.put("id", id);
                map.put("date", new java.sql.Date(date.getTime()));
                List impls = HibernateUtil.search(hql, map);
                for (int j = 0; j < impls.size(); j++ )
                {
                    impl = (IssueHistoryImpl) impls.get(j);
                    System.out.println(impl.getComment());
                }
            }

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertFalse(true);
        }
    }  
    
    public void testDelete() throws Exception
    {
        Set keys = map.keySet();
        Iterator ids = keys.iterator();
        while (ids.hasNext())
        {
            Long id = (Long) ids.next();
            Date date = (Date)map.get(id);
            
            IssueImpl issue = new IssueImpl();
            issue.setId(id.longValue());
            
            IssueHistoryImpl impl = new IssueHistoryImpl();
            impl.setDateReported(date);
            impl.setReportedBy("s");
            impl.setIssue(issue);
            
            HibernateUtil.delete(impl);
        }
    }
}

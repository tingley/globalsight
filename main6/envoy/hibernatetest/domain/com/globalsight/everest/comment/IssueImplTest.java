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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.globalsight.everest.comment.IssueHistoryImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class IssueImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10001);
    
    public void testSave() throws Exception
    {
        IssueHistoryImpl history = new IssueHistoryImpl();
        history.setDateReported(new Date());
        history.setReportedBy("s");
        
        IssueImpl impl = new IssueImpl();
        impl.setLevelObjectId(12);
        impl.setLevelObjectTypeAsString("S");
        impl.setCreateDate(new Date());
        impl.setCreatorId("121");
        impl.setTitle("s");
        impl.setPriority("M");
        impl.setStatus("open");
        // impl.setLogicalKey("k");
        impl.setCategory("type03");

        history.setIssue(impl);
        Set ls = new HashSet();
        ls.add(history);
        impl.setIssueHistory(ls);
        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        IssueImpl impl = new IssueImpl();
        impl.setLevelObjectId(12);
        impl.setLevelObjectTypeAsString("S");
        impl.setCreateDate(new Date());
        impl.setCreatorId("121");
        impl.setTitle("s");
        impl.setPriority("M");
        impl.setStatus("open");
        impl.setLogicalKey("k");
        impl.setCategory("type03");

        HibernateUtil.save(impl);
        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
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
    
    public void testGet() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            System.out.println("id: " + id);
            
            IssueImpl impl = (IssueImpl) HibernateUtil.get(IssueImpl.class, id);

            System.out.println(impl.getLevelObjectId());
            System.out.println(impl.getLevelObjectTypeAsString());
            System.out.println(impl.getCreateDate());
            System.out.println(impl.getTitle());
            System.out.println(impl.getPriority());
            System.out.println(impl.getStatus());
            System.out.println(impl.getLogicalKey());
            System.out.println(impl.getCategory());
        }       
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {

            Long id = (Long) ids.get(i);
            
            IssueImpl impl = new IssueImpl();
            impl.setId(id.longValue());

            impl.setLevelObjectId(12);
            impl.setLevelObjectTypeAsString("S");
            impl.setCreateDate(new Date());
            impl.setCreatorId("121");
            impl.setTitle("s");
            impl.setPriority("M");
            impl.setStatus("open");
            impl.setLogicalKey("k");
            impl.setCategory("type03");

            HibernateUtil.delete(impl);
        }
        
    }
}

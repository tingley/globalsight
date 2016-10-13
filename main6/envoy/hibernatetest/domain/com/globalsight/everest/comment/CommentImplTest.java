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

import com.globalsight.persistence.hibernate.HibernateUtil;
import java.util.*;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.taskmanager.TaskImpl;

import junit.framework.TestCase;

public class CommentImplTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10420);
    
    public void testSave() throws Exception
    {
        WorkflowImpl w = new WorkflowImpl();
        w.setId(2);
        TaskImpl t = new TaskImpl();
        t.setId(2);
        
        CommentImpl impl = new CommentImpl();
        impl.setCreateDate(new Date());
        impl.setCreatorId("1111");
        impl.setObject(w);
        System.out.println(impl.getObject().getId());

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNotNull() throws Exception
    {
        JobImpl job = new JobImpl();
        job.setId(3);
        CommentImpl impl = new CommentImpl();
        impl.setCreateDate(new Date());
        impl.seCommentString("comment");
        impl.setCreatorId("1111");
        impl.setObject(job);

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
            CommentImpl object = (CommentImpl) HibernateUtil.get(
                    CommentImpl.class, id);

            System.out.println(object.toString());
//            System.out.println(object.getComment());
//            System.out.println(object.getCreatorId());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            CommentImpl impl = new CommentImpl();

            JobImpl job = new JobImpl();
            job.setId(3);
            
            impl.setCreateDate(new Date());
            impl.seCommentString("comment");
            impl.setCreatorId("1111");
            impl.setObject(job);

            Long id = (Long) ids.get(i);
            impl.setId(id.longValue());

            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);
        }
    }
}

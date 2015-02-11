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

package com.globalsight.everest.workflowmanager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.jobhandler.JobImpl;

import junit.framework.TestCase;

public class WorkflowImplTest extends TestCase
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

        GlobalSightLocale locale = new GlobalSightLocale("a","a", false);
        locale.setId(-11);
        JobImpl job = new JobImpl();
        job.setId(1);
        
        WorkflowImpl impl = new WorkflowImpl();

        impl.setId(111);
        impl.setTargetLocale(locale);
        impl.setJob(job);
        impl.setWorkflowType("DTP");
        impl.setFraction("f");
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setCompanyId("-1");
        
        
        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSaveNoNull() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale("a","a", false);
        locale.setId(-11);
        JobImpl job = new JobImpl();
        job.setId(1);
        
        
        WorkflowImpl impl = new WorkflowImpl();

        impl.setId(112);
        impl.setTargetLocale(locale);
        impl.setJob(job);
        impl.setWorkflowType("DTP");
        impl.setFraction("f");
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setCompanyId("-1");
        impl.setState("LOCALIZED");
        impl.setDispatchedDate(new Date());
        impl.setEstimatedCompletionDate(new Date());
        impl.setDuration(3);
        impl.setPlannedCompletionDate(new Date());
        impl.setSubLevMatchWordCount(-1);
        impl.setSubLevRepetitionWordCount(-1);
        impl.setContextMatchWordCount(-1);
        impl.setSegmentTmWordCount(-1);
        impl.setLowFuzzyMatchWordCount(-1);
        impl.setMedFuzzyMatchWordCount(-1);
        impl.setMedHiFuzzyMatchWordCount(-1);
        impl.setHiFuzzyMatchWordCount(-1);
        impl.setNoMatchWordCount(-1);
        impl.setRepetitionWordCount(-1);
        impl.setTotalWordCount(-1);
        impl.setIsEstimatedCompletionDateOverrided(Boolean.FALSE);
        impl.setEstimatedCompletionDate(new Date());
        impl.setIsEstimatedTranslateCompletionDateOverrided(Boolean.FALSE);
        impl.setTranslationCompletedDate(new Date());
        
        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testGet() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long)ids.get(i);
            System.out.println("id: " + id);
            WorkflowImpl object = (WorkflowImpl) HibernateUtil.get(
                    WorkflowImpl.class, id);
            System.out.println(object.getCompanyId());
        }
    }

    public void testDelete() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale("a","a", false);
        locale.setId(-11);
        JobImpl job = new JobImpl();
        job.setId(1);
               
        List ids = getIds();        

        for (int i = 0; i < ids.size(); i++)
        {
            WorkflowImpl impl = new WorkflowImpl();
            Long id = (Long)ids.get(i);
            impl.setId(id.longValue());

            impl.setId(111);
            impl.setTargetLocale(locale);
            impl.setJob(job);
            impl.setWorkflowType("DTP");
            impl.setFraction("f");
            impl.setTimestamp(new Timestamp(new Date().getTime()));
            impl.setCompanyId("-1");
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

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

package com.globalsight.everest.jobhandler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.globalsight.everest.comment.CommentImpl;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.request.WorkflowRequestImpl;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

import junit.framework.TestCase;

public class JobImplTest extends TestCase
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
        JobImpl impl = new JobImpl();

        impl.setJobName("jobName");
        impl.setCreateDate(new java.util.Date());
        impl.setPriority(3);
        impl.setIsWordCountReached(false);
        impl.setTimestamp(new Timestamp(new java.util.Date().getTime()));
        impl.setPageCount(3);
        impl.setCompanyId("4");
        
        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }
    
    public void testSaveNoNull() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(1);
        
        JobImpl impl = new JobImpl();
        impl.setJobName("jobName");
        impl.setCreateDate(new java.util.Date());
        impl.setPriority(3);
        impl.setIsWordCountReached(false);
        impl.setTimestamp(new Timestamp(new java.util.Date().getTime()));
        impl.setPageCount(3);
        impl.setCompanyId("4");
        impl.setState("LOCALIZED");
        impl.setLeverageMatchThreshold(3);
        impl.setOverridenWordCount(new Integer(3));
        impl.setQuoteDate("1");
        impl.setQuotePoNumber("3");
        impl.setQuoteApprovedDate("date");
        impl.setJauId("345");
        
        CommentImpl comment = new CommentImpl();
        comment.setCreateDate(new Date());
        comment.setCreatorId("1111");
        comment.setObject(impl);
        
        RequestImpl request = new RequestImpl();
        
        BasicL10nProfile l = new BasicL10nProfile();
        l.setId(1);
        
        request.setType(-2);
        request.setDataSourceId(-1);
        request.setIsPageCxePreviewable(true);
        request.setTimestamp(new Timestamp(new Date().getTime()));
        request.setCompanyId("1001");
        request.setL10nProfile(l);
        
        WorkflowImpl workflow = new WorkflowImpl();
        workflow.setTargetLocale(locale);
        workflow.setJob(impl);
        workflow.setWorkflowType("DTP");
        workflow.setFraction("f");
        workflow.setTimestamp(new Timestamp(new Date().getTime()));
        workflow.setCompanyId("1001");
        
        WorkflowRequestImpl workflowRequestImpl = new WorkflowRequestImpl();
        workflowRequestImpl.setJob(impl);
        workflowRequestImpl
                .setTypeStr("ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB");
        workflowRequestImpl.setExceptionXml("exceptionXml");
        ArrayList templateList = new ArrayList();
        WorkflowTemplateInfo workflowTemplateInfo = new WorkflowTemplateInfo();
        workflowTemplateInfo.setId(System.currentTimeMillis());
        templateList.add(workflowTemplateInfo);
        workflowRequestImpl.setWorkflowTemplateList(templateList);
     
        List comments = new ArrayList();
        comments.add(comment);
        impl.setJobComments(comments);
        List requests = new ArrayList();
        requests.add(request);
        impl.setRequestList(requests);
        List ws = new ArrayList();
        ws.add(workflow);
        impl.setWorkflowInstances(ws);
        List wq = new ArrayList();
        wq.add(workflowRequestImpl);
        impl.setWorkflowRequestList(wq);
        
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
            JobImpl object = (JobImpl) HibernateUtil.get(
                    JobImpl.class, id);
            System.out.println(object.getCompanyId());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            JobImpl impl = new JobImpl();

            impl.setJobName("jobName");
            impl.setCreateDate(new java.util.Date());
            impl.setPriority(3);
            impl.setIsWordCountReached(false);
            impl.setTimestamp(new Timestamp(new java.util.Date().getTime()));
            impl.setPageCount(3);
            impl.setCompanyId("4");
            
            Long id = (Long)ids.get(i);
            impl.setId(id.longValue());
            
            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);         
        }
    }
}

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

package com.globalsight.everest.jobreportingmgr;

import java.rmi.RemoteException;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.JobException;

import junit.framework.TestCase;
import java.util.*;

public class JobReportingManagerLocalTest extends TestCase
{
    JobReportingManagerLocal local = new JobReportingManagerLocal();
    
    public void setUp()throws Exception
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");       
    }
    
    public void testGetJobsByState() throws JobException, RemoteException
    {
        Collection c = local.getJobsByState("LOCALIZED");
        System.out.println(c.size());
        
        c = local.getJobsByState("LOCALIZED", "DISPATCHED");
        System.out.println(c.size());
        
        c = local.getJobsByState("LOCALIZED", "PENDING", "DISPATCHED");
        System.out.println(c.size());
    }
    
    // need task
    public void testGetWorkflowsByJobId() throws JobException, RemoteException
    {
        Collection ws = local.getWorkflowsByJobId(1);
        System.out.println(ws.size());
    }
}

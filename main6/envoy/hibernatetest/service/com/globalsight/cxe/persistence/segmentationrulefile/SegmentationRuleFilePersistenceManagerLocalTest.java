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

package com.globalsight.cxe.persistence.segmentationrulefile;

import com.globalsight.everest.company.CompanyThreadLocal;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.util.*;

public class SegmentationRuleFilePersistenceManagerLocalTest extends TestCase
{
    SegmentationRuleFilePersistenceManagerLocal local = null;

    public void setUp()
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");
        
        local = new SegmentationRuleFilePersistenceManagerLocal();
    }
    
    public void testGetSegmentationRuleFileIdByTmpid() throws SegmentationRuleFileEntityException, RemoteException
    {
        String f = local.getSegmentationRuleFileIdByTmpid("2");
        System.out.println(f);
    }
    
    public void testGetAllSegmentationRuleFiles() throws SegmentationRuleFileEntityException, RemoteException
    {
        Collection f = local.getAllSegmentationRuleFiles();
        System.out.println(f.size());
    }
    
    public void testCreateRelationshipWithTmp() throws SegmentationRuleFileEntityException, RemoteException
    {
        local.createRelationshipWithTmp("2", "3");
    }
}

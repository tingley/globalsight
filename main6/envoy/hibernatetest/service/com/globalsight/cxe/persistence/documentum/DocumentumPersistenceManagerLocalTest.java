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

package com.globalsight.cxe.persistence.documentum;

import com.globalsight.everest.company.CompanyThreadLocal;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.util.*;

public class DocumentumPersistenceManagerLocalTest extends TestCase
{

    DocumentumPersistenceManagerLocal local = new DocumentumPersistenceManagerLocal();

    public void setUp() throws Exception
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");
    }

    public void testCreateDocumentumUserInfo()
            throws DocumentumPersistenceManagerException, RemoteException
    {
        DocumentumUserInfo impl = new DocumentumUserInfo();
        impl.setDocumentumUserId("121");
        impl.setDocumentumPassword("sdf");
        local.createDocumentumUserInfo(impl);
    }

    public void testFindDocumentumUserInfo()
            throws DocumentumPersistenceManagerException, RemoteException
    {
        DocumentumUserInfo info = local.findDocumentumUserInfo("10683");
        System.out.println(info.getDocumentumPassword());
    }

    public void testGetAllDocumentumUserInfos()
            throws DocumentumPersistenceManagerException, RemoteException
    {
        Collection c = local.getAllDocumentumUserInfos();
        System.out.println(c.size());
    }

    public void testmodifyDocumentumUserInfo()
            throws DocumentumPersistenceManagerException, RemoteException
    {
        DocumentumUserInfo info = local.findDocumentumUserInfo("10683");
        info.setDocumentumUserId("55");
        info.setDocumentumDocbase("sql");
        local.modifyDocumentumUserInfo(info);
        info = local.findDocumentumUserInfo("10683");

        assertEquals("55", info.getDocumentumUserId());
        assertEquals("sql", info.getDocumentumDocbase());
    }
}

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

package com.globalsight.cxe.persistence.fileprofile;

import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;

public class FileProfilePersistenceManagerLocalTest extends TestCase
{
    FileProfilePersistenceManagerLocal local = new FileProfilePersistenceManagerLocal();

    public void setUp()
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");
    }

    public void testCreateFileProfile() throws FileProfileEntityException, RemoteException
    {
        FileProfileImpl impl = new FileProfileImpl();
        impl.setName("s");
        impl.setCompanyId("1001");
        impl.setKnownFormatTypeId(1);
        impl.setL10nProfileId(1);
        impl.setByDefaultExportStf(false);
        Date date = new Date();
        impl.setTimestamp(new Timestamp(date.getTime()));
        impl.setIsActive(true);

        Set ens = new HashSet();
        ens.add(new Long(12));
        ens.add(new Long(13));
        impl.setExtensionIds(ens);

        local.createFileProfile(impl);
        assertNotNull(impl.getIdAsLong());
    }
    
    public void testGetFileProfileById() throws FileProfileEntityException, RemoteException
    {
        FileProfileImpl f = (FileProfileImpl)local.getFileProfileById(10687, false);
        System.out.println(f.getName());
    }

    public void testGetFileProfilesByExtension()
            throws FileProfileEntityException, RemoteException
    {
        List es = new ArrayList();
        es.add("s");
        es.add("jsp");
        Collection fs = local.getFileProfilesByExtension(es);
        Iterator iterator = fs.iterator();
        while (iterator.hasNext())
        {
            FileProfileImpl f = (FileProfileImpl) iterator.next();
            System.out.println(f.getName());
        }
        System.out.println(fs.size());
    }

    public void testGetFileProfileIdByName() throws FileProfileEntityException,
            RemoteException
    {
        System.out.println(local.getFileProfileIdByName("s"));
    }

    public void testGetAllFileExtensions() throws FileProfileEntityException,
            RemoteException
    {
        System.out.println(local.getAllFileExtensions().size());
    }

    public void testGetAllKnownFormatTypes() throws FileProfileEntityException,
            RemoteException
    {
        Collection a = local.getAllKnownFormatTypes();
        Iterator iterator = a.iterator();
        while (iterator.hasNext())
        {
            System.out.println(iterator.next());
        }

    }
    
    public void testGetAllFileProfiles() throws FileProfileEntityException, RemoteException
    {
        Collection c = local.getAllFileProfiles();
        System.out.println(c.size());
    }

    
    public void testGetFileExtensionsByFileProfile()
            throws FileProfileEntityException, RemoteException
    {
        List es = new ArrayList();
        es.add("s");
        es.add("jsp");
        Collection fs = local.getFileProfilesByExtension(es);
        FileProfileImpl f = (FileProfileImpl) fs.iterator().next();

        Collection a = local.getFileExtensionsByFileProfile(f);
        Iterator iterator = a.iterator();
        while (iterator.hasNext())
        {
            System.out.println(iterator.next());
        }
    }
}

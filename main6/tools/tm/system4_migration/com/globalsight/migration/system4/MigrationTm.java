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

package com.globalsight.migration.system4;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmImpl;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.tm.CreateTmImpl;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.lowagie.text.List;

import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;

/**
 * This class is responsible for creating a Migration Tm.
 */
public class MigrationTm
{
    /**
     * get a migration tm. If it doesn't exist yet, creates a new one.
     * @return a migration tm
     */
    public static Tm get(String p_tmName)
        throws Exception
    {
        Tm migrationTm = getTmByName(p_tmName);
        if(migrationTm == null)
        {
            TmManager tmManager = CreateTmImpl.getTmManager();
            migrationTm = tmManager.createTm(p_tmName, null, null, null);
        }
        return migrationTm;
    }


    private static Tm getTmByName(String p_name)
        throws Exception
    {
        String hql = "from TmImpl tm where tm.name = :name";
        java.util.List tms = HibernateUtil.getSession().createQuery(hql).setShort("name", p_name).list();
        Tm tm = null;
        if (tms != null && tms.size() > 0)
        {
            tm = tms.get(0);
        }
        return tm;
    }
}

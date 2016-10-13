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

package com.globalsight.tools.tmximport;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmImpl;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.tm.CreateTmImpl;
import com.globalsight.everest.persistence.PersistenceService;

import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;

import TOPLink.Public.PublicInterface.UnitOfWork;
import TOPLink.Public.Expressions.Expression;
import TOPLink.Public.Expressions.ExpressionBuilder;
import TOPLink.Public.QueryFramework.ReadObjectQuery;
import TOPLink.Public.PublicInterface.Session;

/**
 * This class is responsible for creating a Migration Tm.
 */
public class TmTmx
{
    /**
     * get a migration tm. If it doesn't exist yet, creates a new one.
     * @return a migration tm
     */
    public static Tm get(String p_tmName)
        throws Exception
    {
        Tm tm = getTmByName(p_tmName);
        if(tm == null)
        {
            TmManager tmManager = CreateTmImpl.getTmManager();
            tm = tmManager.createTm(p_tmName, null, null, null);
        }
        return tm;
    }


    private static Tm getTmByName(String p_name)
        throws Exception
    {
        Session session
            = PersistenceService.getInstance().acquireClientSession();

        ReadObjectQuery query = new ReadObjectQuery(TmImpl.class);
        ExpressionBuilder builder = query.getExpressionBuilder();
        Expression expr =
            builder.get(TmImpl.M_NAME).equal(p_name);
        query.setSelectionCriteria(expr);
        return (Tm)session.executeQuery(query);
    }

}

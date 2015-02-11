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

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuType;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.fuzzy.Token;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistentObject;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


import TOPLink.Public.QueryFramework.CursoredStream;
import TOPLink.Public.Expressions.Expression;
import TOPLink.Public.Expressions.ExpressionBuilder;
import TOPLink.Public.QueryFramework.ReadAllQuery;
import TOPLink.Public.PublicInterface.Session;

public class DeleteData
{
    
    /**
     * Delete all Tus associated with a given Tm, Tuvs associated with
     * the Tus and FUZZY_INDEX associated with the Tuvs. In addition,
     * it deletes LeverageGroup assosiated with Tus, too.
     * @param p_tm Tm
     */
    public static void deleteAllTuTuvs(Tm p_tm)
        throws Exception
    {
        CursoredStream stream = queryTus(p_tm.getIdAsLong());
        TuImpl tu = null;
        while(!stream.atEnd())
        {
            tu = (TuImpl)stream.read();

            // delete fuzzy_index
            deleteAllFuzzyIndex(tu);

            // delete Tu and associated Tuvs
            PersistenceService.getInstance().deleteObject(tu);
            
            stream.releasePrevious();
        }
        stream.close();
        
        if(tu != null)
        {
            LeverageGroup leverageGroup = tu.getLeverageGroup();
            PersistenceService.getInstance()
                .deleteObject((PersistentObject)leverageGroup);
        }
        
    }


    private static void deleteAllFuzzyIndex(Tu p_tu)
        throws Exception
    {
        Collection tuvs = p_tu.getTuvs();
        if(tuvs != null && tuvs.size() != 0)
        {
            Vector tuvIds = new Vector();
            Iterator itTuv = tuvs.iterator();
            while(itTuv.hasNext())
            {
                tuvIds.add(((Tuv)itTuv.next()).getIdAsLong());
            }
            PersistenceService
                .getInstance().deleteObjects(queryFuzzies(tuvIds));
        }
    }
    

    private static CursoredStream queryTus(Long p_tmId)
        throws Exception
    {
        Session session
            = PersistenceService.getInstance().acquireClientSession();
        ReadAllQuery query = new ReadAllQuery(TuImpl.class);

        ExpressionBuilder tu = query.getExpressionBuilder();
        Expression exp
            = tu.get(TuImpl.M_TM_ID).equal(p_tmId);
        query.setSelectionCriteria(exp);
        query.useCursoredStream();
        query.dontMaintainCache();
        
        return (CursoredStream)session.executeQuery(query);
    }



    private static Collection queryFuzzies(Vector p_tuvIds)
        throws Exception
    {
        Session session
            = PersistenceService.getInstance().acquireClientSession();

        ExpressionBuilder token = new ExpressionBuilder();
        Expression exp
            = token.get(Token.TUV_ID).in(p_tuvIds);
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Token.class);
        query.setSelectionCriteria(exp);
        query.bindAllParameters();
        query.dontMaintainCache();
        
        return (Collection)session.executeQuery(query);
    }
    
}

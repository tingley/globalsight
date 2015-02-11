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

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexer;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.Indexer;
import com.globalsight.ling.tm.IndexerLocal;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;

import TOPLink.Public.QueryFramework.CursoredStream;
import TOPLink.Public.Expressions.Expression;
import TOPLink.Public.Expressions.ExpressionBuilder;
import TOPLink.Public.QueryFramework.ReadAllQuery;
import TOPLink.Public.PublicInterface.Session;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

/**
 * This class is responsible for indexing the migrated data
 */
public class MigrationIndexer
{
    private static FuzzyIndexer m_fuzzyIndexer = new FuzzyIndexer();
    private static Indexer m_indexer = new IndexerLocal();

    /**
     * index the data
     * @param p_locale locale to index
     * @param p_tmId tm id 
     */
    public static void index(GlobalSightLocale p_locale, long p_tmId)
        throws Exception
    {
        PersistenceService persistence = PersistenceService.getInstance();
        CursoredStream cursoredStream
            = queryTuvs(p_locale.getId(), p_tmId);

        Indexer indexer = new IndexerLocal();
        // LIST_SIZE can be any number unless it exhausts memory
        int LIST_SIZE = 1000;
        List tuvList = new ArrayList(LIST_SIZE);
        for(int size = 0; !cursoredStream.atEnd(); size++)
        {
            TuvLing tuvLing = (TuvLing)cursoredStream.read();

            if(size < LIST_SIZE)
            {
                tuvList.add(tuvLing);
            }
            else
            {
                // index 1000 Tuvs at a time
                indexer.index(tuvList);
                size = 0;
                tuvList.clear();
            }
        }

        // index the rest of Tuvs
        if(tuvList.size() > 0)
        {
            indexer.index(tuvList);
        }
    }        


    public static void index(Tuv p_tuv)
        throws Exception
    {
        List tuvList = new ArrayList();
        tuvList.add(p_tuv);
        m_indexer.index(tuvList);
    }
    

    private static CursoredStream queryTuvs(long p_localeId,
                                            long p_tmId)
        throws Exception
    {
        Session session
            = PersistenceService.getInstance().acquireClientSession();
        ReadAllQuery query = new ReadAllQuery(TuvImpl.class);

        ExpressionBuilder builder = query.getExpressionBuilder();
        Expression tuExpr = builder.get(TuvImpl.M_TU);
        Expression expr1 = 
            builder.get(TuvImpl.M_GLOBAL_SIGHT_LOCALE)
            .get(GlobalSightLocale.ID).equal(p_localeId);
        Expression expr2 =
            tuExpr.get(TuImpl.M_TM_ID).equal(p_tmId);

        query.setSelectionCriteria(expr1.and(expr2));

        query.useCursoredStream();
        query.dontMaintainCache();
        
        return (CursoredStream)session.executeQuery(query);
    }


}

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

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuType;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.LocalizableType;
import com.globalsight.everest.tuv.CustomTuType;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvManagerLocal;
import com.globalsight.everest.tuv.CreateTuvManager;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.fuzzy.Token;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistentObject;

import com.globalsight.migration.system3.System3Segment;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


import TOPLink.Public.QueryFramework.CursoredStream;
import TOPLink.Public.Expressions.Expression;
import TOPLink.Public.Expressions.ExpressionBuilder;
import TOPLink.Public.QueryFramework.ReadAllQuery;
import TOPLink.Public.PublicInterface.Session;
import TOPLink.Public.PublicInterface.UnitOfWork;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * This class is responsible for creating and deleting Tu
 */
public class MigrationTu
{
    private static final String SEQUENCE_QUERY
        = "SELECT count FROM sequence WHERE name = 'TU_SEQ'";
    private static final String TU_INSERT
        = "INSERT INTO translation_unit VALUES (?, 0, ?, ?, ?, ?, ?)";
    
    private PreparedStatement m_sequenceStatement = null;
    private PreparedStatement m_tuStatement = null;
    
    public MigrationTu(Connection p_connection)
        throws Exception
    {
        m_sequenceStatement = p_connection.prepareStatement(SEQUENCE_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_tuStatement = p_connection.prepareStatement(TU_INSERT);
    }
    
    /**
     * Create a Tu
     * @param p_segment system 3 segment
     * @param p_tm Tm to use
     * @param p_leverageGroup LeverageGroup to use
     * @return Tu object
     */
    public Tu create(System3Segment p_segment,
        Tm p_tm, LeverageGroup p_leverageGroup)
        throws Exception
    {
        TuType itemType;
        try
        {
            itemType = TuType.valueOf(p_segment.getItemType());
        }
        catch(TuvException e)
        {
            itemType = new CustomTuType(p_segment.getItemType());
        }

        TuvManager tuvManager = CreateTuvManager.create();
        Tu tu = tuvManager.createTu(p_tm.getId(), p_segment.getDataType(),
                                    itemType,
                                    p_segment.isTranslatable()
                                    ? LocalizableType.TRANSLATABLE.getType()
                                    : LocalizableType.LOCALIZABLE.getType());
        tu.setLeverageGroup(p_leverageGroup);
        storeTuToDatabase((TuImpl)tu);
        
        // Toplink code
//          Session client = null;
//          UnitOfWork uow = null;
//          client = PersistenceService.getInstance().acquireClientSession();
//          uow = client.acquireUnitOfWork();
//          Tu clone = (Tu)uow.registerNewObject(tu);
//          LeverageGroup lgClone
//              = (LeverageGroup)uow.registerObject(tu.getLeverageGroup());
//          clone.setLeverageGroup(lgClone);
//          uow.commit();

//          client.release();

//          PersistenceService persistence = PersistenceService.getInstance();
//          persistence.insertObject((PersistentObject)tu);
        return tu;
    }


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
            PersistenceService.getInstance().deleteObject((PersistentObject)leverageGroup);
        }
        
    }




    private void storeTuToDatabase(TuImpl p_tu)
        throws Exception
    {
        // increase TU_SEQ by one
        ResultSet rs = m_sequenceStatement.executeQuery();
        rs.next();
        long id = rs.getLong("count");
        rs.updateLong("count", ++id);
        rs.updateRow();
        rs.close();
        
        m_tuStatement.setLong(1, id);
        m_tuStatement.setLong(2, p_tu.getTmId());
        m_tuStatement.setString(3, p_tu.getDataType());
        m_tuStatement.setString(4, p_tu.getTuType());
        m_tuStatement.setString(5, new Character(p_tu.getLocalizableType()).toString());
        m_tuStatement.setLong(6, p_tu.getLeverageGroupId());
        
        m_tuStatement.executeUpdate();
        p_tu.setId(id);
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

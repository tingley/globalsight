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
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.CreateTuvManager;
import com.globalsight.everest.tm.Tm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * This class is responsible for creating Tu
 */
public class TuTmx
{
    private static final String SEQUENCE_QUERY
        = "SELECT count FROM sequence WHERE name = 'TU_SEQ'";
    private static final String TU_INSERT
        = "INSERT INTO translation_unit VALUES (?, 0, ?, ?, 'text', 'T', ?)";
    
    private PreparedStatement m_sequenceStatement = null;
    private PreparedStatement m_tuStatement = null;
    
    public TuTmx(Connection p_connection)
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
    public Tu create(String p_datatype,
        Tm p_tm, LeverageGroup p_leverageGroup)
        throws Exception
    {
        TuvManager tuvManager = CreateTuvManager.create();
        TuImpl tu = (TuImpl)tuvManager.createTu(p_tm.getId(), p_datatype,
            TuType.TEXT, 'T');
        tu.setLeverageGroup(p_leverageGroup);
        setTuId(tu);
        return tu;
    }


    public void save(Tu p_tu)
        throws Exception
    {
        m_tuStatement.setLong(1, p_tu.getId());
        m_tuStatement.setLong(2, p_tu.getTmId());
        m_tuStatement.setString(3, p_tu.getDataType());
        m_tuStatement.setLong(4, p_tu.getLeverageGroupId());
        
        m_tuStatement.executeUpdate();
    }



    private void setTuId(TuImpl p_tu)
        throws Exception
    {
        // increase TU_SEQ by one
        ResultSet rs = m_sequenceStatement.executeQuery();
        rs.next();
        long id = rs.getLong("count");
        rs.updateLong("count", ++id);
        rs.updateRow();
        rs.close();
        p_tu.setId(id);
    }

}

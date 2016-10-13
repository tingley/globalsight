/*
Copyright (c) 2000-2001 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/
package com.globalsight.tools.reindexer;

import com.globalsight.util.GlobalSightLocale;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import oracle.sql.CLOB;

/**
 * 
 */
public class TuvQuery
{
    private GlobalSightLocale m_srcLocale = null;

    private Connection m_connection;
    private ResultSet m_resultSet = null;
    private PreparedStatement m_tuvClobQuery = null;
    private PreparedStatement m_tuvQuery = null;
    private PreparedStatement m_tuvUpdate = null;

    private static final String TUV_QUERY
        = "select tuv.id, tuv.segment_string, "
        + "tu.tm_id from translation_unit_variant tuv, "
        + "translation_unit tu where tu.id = tuv.tu_id and "
        + "tu.localize_type = 'T' and tuv.is_indexed = 'Y' "
        + "and tuv.locale_id = ?";
    private static final String TUV_CLOB_QUERY
        = "select segment_clob from translation_unit_variant "
        + "where id = ? for update";
    private static final String TUV_IS_INDEXED_UPDATE
        = "update translation_unit_variant set is_indexed = 'N' "
        + "where id = ?";

    // public constructor
    public TuvQuery(Connection p_connection, GlobalSightLocale p_srcLocale)
        throws Exception
    {
        m_connection = p_connection;
        m_srcLocale = p_srcLocale;

        p_connection.setAutoCommit(false);

        m_tuvQuery = p_connection.prepareStatement(TUV_QUERY);
        m_tuvClobQuery = p_connection.prepareStatement(TUV_CLOB_QUERY);
        m_tuvUpdate = p_connection.prepareStatement(TUV_IS_INDEXED_UPDATE);
    }


    /**
     * Query all source TUVs whose localiza_type is translatable and
     * is_index is 'Y'
     */
    public void query()
        throws Exception
    {
        m_tuvQuery.setLong(1, m_srcLocale.getId());
        m_resultSet = m_tuvQuery.executeQuery();
    }

    /**
     * Return queried Tuv. If query() is not called before this method
     * is called, an exception is thrown.
     *
     * @return TuvRep object. If no more Tuv is available, null is returned.
     */
    public TuvRep next()
        throws Exception
    {
        TuvRep rep = null;

        if(m_resultSet == null)
        {
            throw new Exception("query() has not been called yet!");
        }
        
        if(m_resultSet.next())
        {
            long tuvId = m_resultSet.getLong(1);
            String segmentString = m_resultSet.getString(2);
            if(segmentString == null)
            {
                // get the segment from segment_clob
                m_tuvClobQuery.setLong(1, tuvId);
                ResultSet rsClob = m_tuvClobQuery.executeQuery();
                rsClob.next();
                CLOB clob = (CLOB)rsClob.getObject(1);
                segmentString =  clob.getSubString(1, (int)clob.length());
            }
            long tmId = m_resultSet.getLong(3);
            rep = new TuvRep(tuvId, segmentString, tmId);

            // change is_indexed column to 'N'
            m_tuvUpdate.setLong(1, tuvId);
            m_tuvUpdate.executeUpdate();
            m_connection.commit();
        }
        
        return rep;
    }

}

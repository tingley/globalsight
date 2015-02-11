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
package com.globalsight.tools.normalizetuv;

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
    private TuvRep m_tuv = null;

    private ResultSet m_resultSet = null;
    private PreparedStatement m_tuvQuery = null;

    private static final String TUV_QUERY
        = "select tuv.id, tuv.segment_string, "
        + "tu.localize_type, tu.tm_id from translation_unit_variant tuv, "
        + "translation_unit tu where tu.id = tuv.tu_id and "
        + "tu.data_type = 'html' and tuv.locale_id = ?";
    

    public TuvQuery(Connection p_connection, GlobalSightLocale p_srcLocale)
        throws Exception
    {
        m_tuvQuery = p_connection.prepareStatement(TUV_QUERY);
        m_srcLocale = p_srcLocale;
        m_tuv = new TuvRep(p_connection);
    }


    /**
     * Query all the source TUVs whose data type is HTML
     * 
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
        if(m_resultSet == null)
        {
            throw new Exception("query() has not been called yet!");
        }
        
        if(m_resultSet.next())
        {
            long tuvId = m_resultSet.getLong(1);
            m_tuv.setId(tuvId);
            
            String segmentString = m_resultSet.getString(2);
            if(segmentString == null)
            {
                // get the segment from segment_clob
                m_tuv.setSegmentClob(tuvId);
            }
            else
            {
                m_tuv.setSegmentString(segmentString);
            }
            m_tuv.setLocalizeType(m_resultSet.getString(3));
            m_tuv.setTmId(m_resultSet.getLong(4));

            return m_tuv;
        }
        else
        {
            return null;
        }
    }
        
}

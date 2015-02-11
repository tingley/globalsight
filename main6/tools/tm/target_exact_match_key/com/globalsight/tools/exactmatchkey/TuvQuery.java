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
package com.globalsight.tools.exactmatchkey;

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
    private TuvRep m_tuv = null;

    private ResultSet m_resultSet = null;
    private PreparedStatement m_tuvQuery = null;

    private static final String TUV_QUERY
        = "select id, segment_string from translation_unit_variant "
        + "where state in ('COMPLETE', 'LOCALIZED', 'ALIGNMENT_LOCALIZED') "
        + "and exact_match_key = 0";
    

    public TuvQuery(Connection p_connection)
        throws Exception
    {
        m_tuvQuery = p_connection.prepareStatement(TUV_QUERY);
        m_tuv = new TuvRep(p_connection);
    }


    /**
     * Query all TUVs of which exact_match_key is 0 and state is COMPLETE.
     * 
     */
    public void query()
        throws Exception
    {
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

            return m_tuv;
        }
        else
        {
            return null;
        }
    }
        
}

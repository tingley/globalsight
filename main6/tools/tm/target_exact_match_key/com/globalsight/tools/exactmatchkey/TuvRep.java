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

import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.TuLing;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GlobalSightLocale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.Writer;
import java.io.StringReader;
import java.io.Reader;

import oracle.sql.CLOB;

/**
 * 
 */
public class TuvRep
    extends TuvLing
{
    private long m_tuvId;
    private String m_segmentString;
    private String m_segmentClob;
    
    private PreparedStatement m_tuvCrcUpdate = null;
    private PreparedStatement m_tuvClobQuery = null;

    private static final String TUV_CRC_UPDATE
        = "update translation_unit_variant set exact_match_key = ? "
        + "where id = ?";
    private static final String TUV_CLOB_QUERY
        = "select segment_clob from translation_unit_variant "
        + "where id = ? for update";
    
    // default constructor is private
    private TuvRep()
    {
    }
    
    // public constructor
    public TuvRep(Connection p_connection)
        throws Exception
    {
        m_tuvCrcUpdate = p_connection.prepareStatement(TUV_CRC_UPDATE);
        m_tuvClobQuery = p_connection.prepareStatement(TUV_CLOB_QUERY);
    }


    // set the string from segment_string column in
    // translation_unit_variant table
    public void setSegmentString(String p_segmentString)
    {
        m_segmentString = p_segmentString;
        m_segmentClob = null;
        m_exactMatchFormat = null;
    }
    
    // set the Oracle CLOB object from segment_clob column in
    // translation_unit_variant table
    public void setSegmentClob(long p_tuvId)
        throws Exception
    {
        ResultSet rsClob = null;
        try
        {
            m_tuvClobQuery.setLong(1, p_tuvId);
            rsClob = m_tuvClobQuery.executeQuery();
            rsClob.next();
            CLOB clob = (CLOB)rsClob.getObject(1);

            Reader r = clob.getCharacterStream();
            StringBuffer sb = new StringBuffer();
            int charsRead = 0;
            char[] buffer = new char[clob.getChunkSize()];
            while ((charsRead = r.read(buffer)) != -1)
            {
                sb.append(buffer, 0, charsRead);
            }
            r.close();
            m_segmentClob = sb.toString();

            m_segmentString = null;
            m_exactMatchFormat = null;
        }
        finally
        {
            if(rsClob != null)
            {
                rsClob.close();
            }
        }
    }
    

    public void setId(long p_id)
    {
        m_tuvId = p_id;
    }
    
    public long getId()
    {
        return m_tuvId;
    }
    

    public void updateExactMatchKey()
        throws Exception
    {
        long crc = GlobalSightCrc.calculate(getExactMatchFormat());

        m_tuvCrcUpdate.setLong(1, crc);
        m_tuvCrcUpdate.setLong(2, getId());
        m_tuvCrcUpdate.executeUpdate();
    }

    
    //
    // Inherited mothods from TuvLing class
    //

    /**
     * Get the segment string of this Tuv
     * @return Segment string
     */
    public String getGxml()
    {
        return (m_segmentString == null
            ? m_segmentClob : m_segmentString);
    }


    //
    // unused inherited mothods.
    //

    public boolean isLocalizable()
    {
        // not used.
        return false;
    }

    public GlobalSightLocale getGlobalSightLocale()
    {
        // not used.
        return null;
    }
    
    public long getLocType()
    {
        // not used.
        return 0;
    }

    public void setGxml(String p_segment)
    {
        throw new RuntimeException("Don't use this method");
    }

    public TuLing getTuLing()
    {
        // not used.
        return null;
    }

    public boolean isCompleted()
    {
        // not used.
        return false;
    }

    public int getWordCount()
    {
        // not used.
        return 0;
    }

    public long getLocaleId()
    {
        // not used.
        return 0;
    }

    public long getExactMatchKey()
    {
        // not used
        return 0;
    }

    public void setExactMatchKey(long p_exactMatchKey)
    {
        // not used
    }


}

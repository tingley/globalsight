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

import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.TuLing;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GlobalSightLocale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.Writer;
import java.io.StringReader;

import oracle.sql.CLOB;

/**
 * 
 */
public class TuvRep
    extends TuvLing
{
    private long m_tuvId;
    private String m_locType;
    private String m_segmentString;
    private String m_segmentClob;
    private long m_tmId;
    
    private PreparedStatement m_tuvCrcUpdate = null;
    private PreparedStatement m_tuvCrcStringUpdate = null;
    private PreparedStatement m_tuvClobQuery = null;
    private PreparedStatement m_tuvClobNullout = null;

    private static final String TUV_CRC_UPDATE
        = "update translation_unit_variant set exact_match_key = ?, "
        + "is_indexed = 'N' where id = ?";
    private static final String TUV_CRC_STRING_UPDATE
        = "update translation_unit_variant set exact_match_key = ?, "
        + "segment_string = ?, is_indexed = 'N' where id = ?";
    private static final String TUV_CLOB_QUERY
        = "select segment_clob from translation_unit_variant "
        + "where id = ? for update";
//    private static final String TUV_CLOB_NULLOUT
//        = "UPDATE translation_unit_variant SET segment_clob = EMPTY_CLOB()"
//        + " WHERE id = ?";
    private static final String TUV_CLOB_NULLOUT
        = "UPDATE translation_unit_variant SET segment_clob = ?"
        + " WHERE id = ?";
    private static final int EOF = -1;
    
    // default constructor is private
    private TuvRep()
    {
    }
    
    // public constructor
    public TuvRep(Connection p_connection)
        throws Exception
    {
        m_tuvCrcUpdate = p_connection.prepareStatement(TUV_CRC_UPDATE);
        m_tuvCrcStringUpdate
            = p_connection.prepareStatement(TUV_CRC_STRING_UPDATE);
        m_tuvClobQuery = p_connection.prepareStatement(TUV_CLOB_QUERY);
        m_tuvClobNullout = p_connection.prepareStatement(TUV_CLOB_NULLOUT);
    }


    // set the string from segment_string column in
    // translation_unit_variant table
    public void setSegmentString(String p_segmentString)
    {
        m_segmentString = p_segmentString;
        m_segmentClob = null;
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

            m_segmentClob = clob.getSubString(1, (int)clob.length());
            m_segmentString = null;
        }
        finally
        {
            if(rsClob != null)
            {
                rsClob.close();
            }
        }
    }
    

    // update TUV with a new String
    public void updateSegment(String p_newSegment)
        throws Exception
    {
        if(m_segmentString != null)
        {
            m_segmentString = p_newSegment;
        }
        else
        {
            m_segmentClob = p_newSegment;
        }
        
        
        // generate exact match key
        long crc = GlobalSightCrc.calculate(getExactMatchFormat());

        if(m_segmentString != null)
        {
            m_tuvCrcStringUpdate.setLong(1, crc);
            m_tuvCrcStringUpdate.setString(2, p_newSegment);
            m_tuvCrcStringUpdate.setLong(3, getId());
            m_tuvCrcStringUpdate.executeUpdate();
        }
        else  // CLOB segment
        {
            m_tuvCrcUpdate.setLong(1, crc);
            m_tuvCrcUpdate.setLong(2, getId());
            m_tuvCrcUpdate.executeUpdate();

            writeClob(p_newSegment);
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
    
    public void setLocalizeType(String p_locType)
    {
        m_locType = p_locType;
    }

    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }
    
    public long getTmId()
    {
        return m_tmId;
    }
    
        
    //
    // Inherited mothods from TuvLing class
    //

    /**
     * Am I localizable?
     * @return true if localizable, false otherwise
     */
    public boolean isLocalizable()
    {
        return m_locType.equals("L");
    }

    /**
     * Get the segment string of this Tuv
     * @return Segment string
     */
    public String getGxml()
    {
        String gxml = null;
        
        try
        {
            gxml = (m_segmentString == null
                ? m_segmentClob : m_segmentString);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
        
        return gxml;
    }


    private void writeClob(String p_segmentString)
        throws Exception
    {
        Writer writer = null;
        ResultSet rs = null;
        
        try
        {
            // null out the clob field, otherwise we end up
            // overwriting a part of the field
        	m_tuvClobNullout.setString(1, p_segmentString);
            m_tuvClobNullout.setLong(2, getId());
            m_tuvClobNullout.executeUpdate();

//            m_tuvClobQuery.setLong(1, getId());
//            rs = m_tuvClobQuery.executeQuery();
//            rs.next();
//            CLOB clob = (CLOB)rs.getObject(1);
//
//            writer = clob.getCharacterOutputStream();
//            StringReader sr = new StringReader(p_segmentString);
//            char[] buffer = new char[clob.getChunkSize()];
//            int charsRead = 0;
//            while ((charsRead = sr.read(buffer)) != EOF)
//            {
//                writer.write(buffer, 0, charsRead);
//            }
//
//            writer.flush();
        }
        finally
        {
            writer.close();
            rs.close();
        }
    }

    //
    // unused inherited mothods.
    //

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

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

import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.TuLing;
import com.globalsight.util.GlobalSightLocale;


/**
 * 
 */
public class TuvRep
    extends TuvLing
{
    private String m_segmentString;
    private long m_tmId;

    // constructor
    public TuvRep(long p_id, String p_segmentString, long p_tmId)
    {
        setId(p_id);
        m_segmentString = p_segmentString;
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
     * Get the segment string of this Tuv
     * @return Segment string
     */
    public String getGxml()
    {
        return m_segmentString;
    }


    public void setGxml(String p_segmentString)
    {
        m_segmentString = p_segmentString;
    }
    
    //
    // unused inherited mothods.
    //

    public boolean isLocalizable()
    {
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

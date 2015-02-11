package com.globalsight.ling.tm3.integration.segmenttm;

import java.sql.Timestamp;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSTuvData;

/**
 * Helper routines.
 */
public class TM3Util {

    public static TM3Attribute getAttr(TM3Tm<GSTuvData> tm, 
                        SegmentTmAttribute attr) {
        return tm.getAttributeByName(attr.getKey());
    }
    
    public static Timestamp toTimestamp(TM3Event e) {
        return new Timestamp(e.getTimestamp().getTime());
    }
}

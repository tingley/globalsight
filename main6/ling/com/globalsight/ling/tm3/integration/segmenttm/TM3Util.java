package com.globalsight.ling.tm3.integration.segmenttm;

import java.sql.Timestamp;

import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3AttributeValueType;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.BaseTm;
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
    
    public static TM3Attribute toTM3Attribute(ProjectTmTuTProp prop)
    {
        TM3Attribute tm3a = null;

        if (prop != null)
        {
            String name = getNameForTM3(prop);

            tm3a = new TM3Attribute(name, new TM3AttributeValueType.CustomType(), null, false);
        }

        return tm3a;
    }
    
    public static ProjectTmTuTProp toProjectTmTuTProp(TM3Attribute tm3a, Object value)
    {
        ProjectTmTuTProp result = null;

        if (tm3a != null && tm3a.isCustom() && value != null)
        {
            result = new ProjectTmTuTProp();
            result.setPropType(ProjectTmTuTProp.TYPE_ATT_PREFIX + tm3a.getName());
            result.setPropValue(value.toString());
        }

        return result;
    }

    public static String getNameForTM3(ProjectTmTuTProp prop)
    {
        String name = prop.getAttributeName();
        if (name == null)
            name = prop.getPropType();
        
        return name;
    }
    
    public static TM3Attribute saveTM3Attribute(TM3Attribute tm3a, BaseTm tm)
    {
        TM3Attribute oriAtt = tm.getAttributeByName(tm3a.getName());
        if (oriAtt == null)
        {
            tm3a.setTm(tm);
            tm.addAttribute(tm3a);

            return tm3a;
        }
        else
        {
            return oriAtt;
        }
    }
}

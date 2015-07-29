package com.globalsight.ling.tm3.integration.segmenttm;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3AttributeValueType;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.util.GlobalSightLocale;

/**
 * Helper routines.
 */
public class TM3Util
{
    public static TM3Attribute getAttr(TM3Tm<GSTuvData> tm,
            SegmentTmAttribute attr)
    {
        return tm.getAttributeByName(attr.getKey());
    }

    public static Timestamp toTimestamp(TM3Event e)
    {
        return new Timestamp(e.getTimestamp().getTime());
    }

    public static TM3Attribute toTM3Attribute(ProjectTmTuTProp prop)
    {
        TM3Attribute tm3a = null;

        if (prop != null)
        {
            String name = getNameForTM3(prop);

            tm3a = new TM3Attribute(name,
                    new TM3AttributeValueType.CustomType(), null, false);
        }

        return tm3a;
    }

    public static ProjectTmTuTProp toProjectTmTuTProp(TM3Attribute tm3a,
            Object value)
    {
        ProjectTmTuTProp result = null;

        if (tm3a != null && tm3a.isCustom() && value != null)
        {
            result = new ProjectTmTuTProp();
            result.setPropType(ProjectTmTuTProp.TYPE_ATT_PREFIX
                    + tm3a.getName());
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

    @SuppressWarnings("rawtypes")
    public static BaseTm getBaseTm(long tm3TmId)
    {
        return (BaseTm) DefaultManager.create().getTm(new GSDataFactory(),
                tm3TmId);
    }

    /**
     * Convert "TM3Tu" to "SegmentTmTu" object.
     * 
     * @param tm3tu
     * @param ptmId
     * @param formatAttr
     * @param typeAttr
     * @param sidAttr
     * @param fromWsAttr
     * @param translatableAttr
     * @param projectAttr
     * @return SegmentTmTu
     */
    public static SegmentTmTu toSegmentTmTu(TM3Tu<GSTuvData> tm3tu, long ptmId,
            TM3Attribute formatAttr, TM3Attribute typeAttr,
            TM3Attribute sidAttr, TM3Attribute fromWsAttr,
            TM3Attribute translatableAttr, TM3Attribute projectAttr)
    {
        SegmentTmTu tu = new SegmentTmTu(tm3tu.getId(), ptmId,
                (String) tm3tu.getAttribute(formatAttr),
                (String) tm3tu.getAttribute(typeAttr), true,
                (GlobalSightLocale) tm3tu.getSourceTuv().getLocale());
        String sid = (String) tm3tu.getAttribute(sidAttr);
        tu.setSID(sid);
        tu.setFromWorldServer((Boolean) tm3tu.getAttribute(fromWsAttr));
        if ((Boolean) tm3tu.getAttribute(translatableAttr))
        {
            tu.setTranslatable();
        }
        else
        {
            tu.setLocalizable();
        }

        // convert TM3 attributes to TU properties
        Map<TM3Attribute, Object> tmAts = tm3tu.getAttributes();
        if (tmAts != null && !tmAts.isEmpty())
        {
            for (Map.Entry<TM3Attribute, Object> tmAt : tmAts.entrySet())
            {
                ProjectTmTuTProp prop = TM3Util.toProjectTmTuTProp(
                        tmAt.getKey(), tmAt.getValue());

                if (prop != null)
                {
                    tu.addProp(prop);
                }
            }
        }

        for (TM3Tuv<GSTuvData> tuv : tm3tu.getAllTuv())
        {
            SegmentTmTuv stuv = new LeveragedSegmentTuv(tuv.getId(), tuv
                    .getContent().getData(),
                    (GlobalSightLocale) tuv.getLocale());
            stuv.setTu(tu);
            stuv.setSid(sid);

            stuv.setCreationDate(getCreationDate(tuv));
            stuv.setCreationUser(tuv.getCreationUser());
            stuv.setModifyDate(getModifyDate(tuv));
            stuv.setModifyUser(tuv.getModifyUser());
            stuv.setUpdatedProject((String) tm3tu.getAttribute(projectAttr));
            stuv.setLastUsageDate(getLastUsageDate(tuv));
            stuv.setJobId(tuv.getJobId());
            stuv.setJobName(tuv.getJobName());
            stuv.setSid(tuv.getSid());
            // to be safe...
            if (tuv.getSid() != null && tu.getSID() == null) {
            	tu.setSID(tuv.getSid());
            }
            stuv.setPreviousHash(tuv.getPreviousHash());
            stuv.setNextHash(tuv.getNextHash());

            tu.addTuv(stuv);
        }
        return tu;
    }

    private static Timestamp getCreationDate(TM3Tuv<GSTuvData> tuv)
    {
        Date creationDate = tuv.getCreationDate();
        if (creationDate != null)
        {
            return new Timestamp(creationDate.getTime());
        }

        return null;
    }

    private static Timestamp getModifyDate(TM3Tuv<GSTuvData> tuv)
    {
        Date modifyDate = tuv.getModifyDate();
        if (modifyDate != null)
        {
            return new Timestamp(modifyDate.getTime());
        }

        return null;
    }

    private static Timestamp getLastUsageDate(TM3Tuv<GSTuvData> tuv)
    {
    	Date lastUsageDate = tuv.getLastUsageDate();
    	if (lastUsageDate != null)
    	{
    		return new Timestamp(lastUsageDate.getTime());
    	}

    	return null;
    }
}

/*
 Copyright (c) 2000-2005 GlobalSight Corporation. All rights reserved.

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

package com.globalsight.everest.edit.offline.xliff;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;

/**
 * A base class responsible for generating standard XLIFF.
 */
public class XLIFFStandardUtil
{
    private static final Logger s_logger = Logger
            .getLogger(XLIFFStandardUtil.class);

    public static final String GS_SEPARATOR = "_GS_";

    private static final String att_posClose = " pos=\"close\"";

    private static final String att_posEnd = " pos=\"end\"";

    private static final String att_posBegin = " pos=\"begin\"";

    private static final String att_posOpen = " pos=\"open\"";

    private static final String att_ctype = " ctype=\"";

    private static final String att_type = " type=\"";

    private static final String att_id = " id=\"";

    private static final String bpt_start = "<bpt ";

    private static final String bpt_end = ">";

    private static final String ept_start = "<ept ";

    private static final String ept_end = ">";

    private static final String it_start = "<it ";

    private static final String it_end = ">";

    private static final String ph_start = "<ph ";

    private static final String ph_end = ">";

    private static final String att_i = " i=\"";

    private static final String att_rid = " rid=\"";

    private static final String att_x = " x=\"";

    private static final String att_xid = " xid=\"";

    private static final String att_end = "\"";

    public static String convertToStandard(OfflineSegmentData p_osd,
            String segment)
    {
        if (segment == null)
        {
            return "";
        }

        if (!segment.contains("<") || !segment.contains(">"))
        {
            return segment;
        }

        // bpt
        segment = replaceAtts(segment, bpt_start, bpt_end, true, att_i, att_id,
                att_type, att_ctype, att_x, att_xid);

        // ept
        segment = replaceAtts(segment, ept_start, ept_end, true, att_i, att_id);

        // it : add "id" by "x" first, then replace "x" to "xid" etc.
        segment = addAtts(segment, it_start, it_end, att_x, att_id,
                p_osd.getDisplaySegmentID());
        segment = replaceAtts(segment, it_start, it_end, true, att_posBegin,
                att_posOpen, att_posEnd, att_posClose, att_type, att_ctype,
                att_x, att_xid);

        // ph : add "id" by "x" first, then replace "x" to "xid" etc.
        segment = addAtts(segment, ph_start, ph_end, att_x, att_id,
                p_osd.getDisplaySegmentID());
        segment = replaceAtts(segment, ph_start, ph_end, true, att_type,
                att_ctype, att_x, att_xid);

        return segment;
    }

    public static String convertToTmx(String segment)
    {
        if (segment == null)
        {
            return "";
        }

        if (!segment.contains("<") || !segment.contains(">"))
        {
            return segment;
        }

        // bpt
        segment = replaceAtts(segment, bpt_start, bpt_end, false, att_x,
                att_xid, att_i, att_rid, att_i, att_id, att_type, att_ctype);

        // ept
        segment = replaceAtts(segment, ept_start, ept_end, false, att_i,
                att_rid, att_i, att_id);

        // it
        segment = removeAtts(segment, it_start, it_end, att_id);
        segment = replaceAtts(segment, it_start, it_end, false, att_posBegin,
                att_posOpen, att_posEnd, att_posClose, att_type, att_ctype,
                att_x, att_xid);

        // ph
        segment = removeAtts(segment, ph_start, ph_end, att_id);
        segment = replaceAtts(segment, ph_start, ph_end, false, att_type,
                att_ctype, att_x, att_xid);

        return segment;
    }

    private static String addAtts(String segment, String tagStart,
            String tagEnd, String oriAttSt, String newAttSt, String prefix)
    {
        StringBuffer src = new StringBuffer(segment);
        int s = 0;
        StringIndex si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        while (si != null)
        {
            String oriSeg = si.allValue;
            String newSeg = oriSeg;
            if (oriAttSt != null && newAttSt != null
                    && !oriSeg.contains(newAttSt))
            {
                StringBuffer oriB = new StringBuffer(oriSeg);
                StringIndex siB = StringIndex.getValueBetween(oriB, 0,
                        oriAttSt, att_end);

                if (siB != null)
                {
                    String newAtt = newAttSt + prefix + GS_SEPARATOR
                            + siB.value + att_end;
                    newSeg = tagStart + newAtt + " "
                            + newSeg.substring(tagStart.length());

                    segment = segment.replace(oriSeg, newSeg);
                }
            }

            src = new StringBuffer(segment);
            s = si.end;
            si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        }
        return segment;
    }

    private static String removeAtts(String segment, String tagStart,
            String tagEnd, String attSt)
    {
        StringBuffer src = new StringBuffer(segment);
        int s = 0;
        StringIndex si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        while (si != null)
        {
            String oriSeg = si.allValue;
            String newSeg = oriSeg;
            if (attSt != null)
            {
                StringBuffer oriB = new StringBuffer(oriSeg);
                StringIndex siB = StringIndex.getValueBetween(oriB, 0, attSt,
                        att_end);
                if (siB != null && siB.allValue != null
                        && siB.allValue.contains(GS_SEPARATOR))
                {
                    String ss = newSeg.substring(0, siB.allStart);
                    String ee = newSeg.substring(siB.allEnd);
                    if (ss.endsWith(" ") && ee.startsWith(" "))
                    {
                        ee = ee.substring(1);
                    }
                    newSeg = ss + ee;

                    segment = segment.replace(oriSeg, newSeg);
                }
            }

            src = new StringBuffer(segment);
            s = si.end;
            si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        }
        return segment;
    }

    private static String replaceAtts(String segment, String tagStart,
            String tagEnd, boolean toXliff, String... strings)
    {
        StringBuffer src = new StringBuffer(segment);
        int s = 0;
        StringIndex si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        while (si != null)
        {
            String oriSeg = si.allValue;
            String newSeg = oriSeg;
            if (strings != null && strings.length > 0
                    && strings.length % 2 == 0)
            {
                for (int i = 0; i < strings.length; i = i + 2)
                {
                    String tmxAtt = strings[i];
                    String xliffAtt = strings[i + 1];

                    if (toXliff)
                    {
                        newSeg = newSeg.replace(tmxAtt, xliffAtt);
                    }
                    else
                    {
                        newSeg = newSeg.replace(xliffAtt, tmxAtt);
                    }

					// We do not adjust any attribute values to avoid issues
					// like GBS-3950, even though XLF specification asks
					// "A user-defined value must start with an "x-" prefix".
                    /** 
					if (att_ctype.equals(xliffAtt))
					{
						String attStart = tmxAtt;
						if (toXliff)
						{
							attStart = xliffAtt;
						}

						StringIndex siAtt = StringIndex.getValueBetween(newSeg,
								0, attStart, att_end);
						if (siAtt != null)
						{
							String newValue = processCType(siAtt.value, toXliff);
							newSeg = newSeg.replace(siAtt.allValue, attStart
									+ newValue + att_end);
						}
					}
					*/
                }
            }
            segment = segment.replace(oriSeg, newSeg);

            src = new StringBuffer(segment);
            s = si.end;
            si = StringIndex.getValueBetween(src, s, tagStart, tagEnd);
        }
        
        return segment;
    }

    private static String processCType(String avalue, boolean toXliff)
    {
        if (toXliff)
        {
            if ("ulined".equals(avalue))
            {
                return "x-" + avalue;
            }

            if ("strong".equals(avalue))
            {
                return "x-" + avalue;
            }
        }
        else
        {
            if ("x-ulined".equals(avalue) || "x-strong".equals(avalue))
            {
                return avalue.substring(2);
            }
        }

        return avalue;
    }
}

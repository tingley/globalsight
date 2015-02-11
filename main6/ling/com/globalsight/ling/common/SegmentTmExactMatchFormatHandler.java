package com.globalsight.ling.common;

import java.util.Properties;

import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.TmxTypeMapper;
import com.globalsight.util.gxml.GxmlNames;

/**
 * This inner class was originally private to SegmentTmTuv.  It 
 * overrides the default implementation. Segment TM segments don't  
 * have original formatting code. They hold only attributes 
 * of the codes and their locations. This handler generates those 
 * information in the formatted string.
 *
 * For example, a tag <bpt type="link"/> is converted to a string
 * "<bpt-link/>" and placed in the formatted string where the tag
 * is. This string is a combination of a tag name and type. This
 * string will be used to generate the segment's exact match key
 * so that the two segments with exactly the same tags at exactly
 * the same location in the text can be considered as the same
 * segments.
 *
 * This string shouldn't be used for the purpose other than
 * generating an exact match key or determining the similarlity of
 * the two segments by comparing this string.
 */
public class SegmentTmExactMatchFormatHandler extends TuvSegmentBaseHandler {
    private StringBuffer m_content = new StringBuffer(200);

    // Overridden method
    public void handleText(String p_text)
    {
        // accumulate all the text
        m_content.append(m_xmlDecoder.decodeStringBasic(p_text));
    }

    public void handleStartTag(String p_name, Properties p_attributes,
        String p_originalString)
    {
        if (p_name.equals(GxmlNames.BPT) ||
            p_name.equals(GxmlNames.EPT) ||
            p_name.equals(GxmlNames.PH) ||
            p_name.equals(GxmlNames.IT))
        {
            String type = p_attributes.getProperty(GxmlNames.BPT_TYPE);

            if (type == null)
            {
                type = "none";
            }

            // def 11875: don't look at "type" when <it pos="end">
            if (p_name.equals(GxmlNames.IT))
            {
                String pos = p_attributes.getProperty(GxmlNames.IT_POS);
                if (pos != null && pos.equals("end"))
                {
                    type = pos;
                }
            }

            // def 11876: normalize "type"
            type = TmxTypeMapper.normalizeType(type);

            // convert x-nbspace to a real character U+00A0
            if (type.equals(TmUtil.X_NBSP))
            {
                m_content.append('\u00a0');
            }
            else if (type.equals(TmUtil.X_MSO_SPACERUN))
            {
                m_content.append(' ');
            }
            else if (type.equals(TmUtil.X_MSO_TAB))
            {
                m_content.append('\t');
            }
            else
            {
                // make the tag identification string
                String tagId = "<" + p_name + "-" + type + "/>";
                m_content.append(tagId);
            }
        }
    }

    public String toString()
    {
        return m_content.toString();
    }

}

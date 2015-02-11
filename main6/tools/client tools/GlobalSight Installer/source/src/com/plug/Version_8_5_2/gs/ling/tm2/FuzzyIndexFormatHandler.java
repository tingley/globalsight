package com.plug.Version_8_5_2.gs.ling.tm2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import com.plug.Version_8_5_2.gs.ling.common.DiplomatBasicParserException;
import com.plug.Version_8_5_2.gs.ling.common.TuvSegmentBaseHandler;
import com.plug.Version_8_5_2.gs.util.gxml.GxmlNames;

/**
 * This class used to live inside AbstractTmTuv.
 */
public class FuzzyIndexFormatHandler 
        extends TuvSegmentBaseHandler
{
    public static final String X_NBSP = "x-nbspace";
    public static final String X_MSO_SPACERUN = "x-mso-spacerun";
    public static final String X_MSO_TAB = "x-mso-tab";
    
    private boolean m_addsText = true;
    private Stack m_current = new Stack();
    private Collection m_subflows = new ArrayList();
    private StringBuffer m_mainText = new StringBuffer();

    // Overridden method
    public void handleStart()
    {
        m_addsText = true;

        // set the main text buffer to the current
        m_current.push(m_mainText);
    }

    // Overridden method
    public void handleText(String p_text)
    {
        if (m_addsText)
        {
            // add only text, no formatting code, to the current
            // StringBuffer. Decode string.
            ((StringBuffer)m_current.peek()).append(
                m_xmlDecoder.decodeStringBasic(p_text));
        }
    }

    // Overridden method
    public void handleStartTag(String p_name, Properties p_attributes,
        String p_originalString)
        throws DiplomatBasicParserException
    {
        if (p_name.equals(GxmlNames.SEGMENT) ||
            p_name.equals(GxmlNames.LOCALIZABLE))
        {
            m_addsText = true;
        }
        else if (p_name.equals(GxmlNames.SUB))
        {
            m_addsText = true;

            StringBuffer subflow = new StringBuffer();
            m_subflows.add(subflow);
            m_current.push(subflow);
        }
        else if (p_name.equals(GxmlNames.BPT) ||
            p_name.equals(GxmlNames.EPT) ||
            p_name.equals(GxmlNames.PH) ||
            p_name.equals(GxmlNames.IT))
        {
            m_addsText = false;

            // <ph> element representing nbsp is replaced with a
            // nbsp character U+00A0
            String type = p_attributes.getProperty(GxmlNames.PH_TYPE);
            if (type != null)
            {
                if (type.equals(X_NBSP))
                {
                    ((StringBuffer)m_current.peek()).append('\u00a0');
                }
                else if (type.equals(X_MSO_SPACERUN))
                {
                    ((StringBuffer)m_current.peek()).append(' ');
                }
                else if (type.equals(X_MSO_TAB))
                {
                    ((StringBuffer)m_current.peek()).append('\t');
                }
            }
        }
        else
        {
            // non conforming tags
            //                  String[] params = new String[1];
            //                  params[0] = p_name;
            //                  throw new LingManagerException(
            //                      "NonConformingGxmlTag", params, null);
            throw new DiplomatBasicParserException(
                "Found non conforming gxml tag " + p_name);
        }
    }

    public void handleEndTag(String p_name, String p_originalTag)
    {
        if (p_name.equals(GxmlNames.SUB))
        {
            m_addsText = false;
            m_current.pop();
        }
        else // bpt, ept, it, ph, segment, localizable
        {
            m_addsText = true;
        }
    }

    public String toString()
    {
        for (Iterator it = m_subflows.iterator(); it.hasNext(); )
        {
            StringBuffer subflow = (StringBuffer)it.next();
            m_mainText.append(" ").append(subflow);
        }

        return m_mainText.toString();
    }
}

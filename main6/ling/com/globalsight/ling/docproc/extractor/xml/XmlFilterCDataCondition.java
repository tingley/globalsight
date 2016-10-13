package com.globalsight.ling.docproc.extractor.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlFilterCDataCondition
{
    private String m_aType = null;
    private String m_operator = null;
    private String m_aValue = null;
    
    public XmlFilterCDataCondition(String aType, String operator, String aValue)
    {
        this.m_aType = aType;
        this.m_operator = operator;
        this.m_aValue = aValue;
    }
    
    public boolean match(Node p_node)
    {
        if (p_node == null)
        {
            return false;
        }
        
        if (m_aType != null && m_operator != null && m_aValue != null
                && p_node.getNodeType() == Node.CDATA_SECTION_NODE)
        {
            if ("cdatacontent".equals(m_aType))
            {
                return isValueMatched(p_node.getNodeValue());
            }
        }
        
        return false;
    }

    private boolean isValueMatched(String nodeValue)
    {
        if ("equal".equals(m_operator))
        {
            return m_aValue.equals(nodeValue);
        }
        else if ("not equal".equals(m_operator))
        {
            return !m_aValue.equals(nodeValue);
        }
        else if ("match".equals(m_operator))
        {
            Pattern p = Pattern.compile(m_aValue, Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = p.matcher(nodeValue);
            boolean b = m.matches();
            return b;
        }
        else
        {
            // should not here
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        return m_aType + " " + m_operator + " " + m_aValue;
    }
}

package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;

public class XmlFilterCDataTag
{
    private String m_Name = null;
    private List<XmlFilterCDataCondition> m_conditions = null;
    private boolean m_translatable = true;
    private String m_postFilterId = null;
    private String m_postFilterTableName = null;

    public XmlFilterCDataTag(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        m_Name = tagNameElement.getFirstChild().getNodeValue();

        NodeList conditionNodes = tagElement.getElementsByTagName("cdataConditions");
        m_conditions = buildConditions(conditionNodes);
        
        NodeList translatableNodes = tagElement.getElementsByTagName("translatable");
        if (translatableNodes != null && translatableNodes.getLength() > 0)
        {
            m_translatable = "true".equals(translatableNodes.item(0).getFirstChild().getNodeValue());
        }
        
        NodeList idNodes = tagElement.getElementsByTagName("postFilterId");
        if (idNodes != null && idNodes.getLength() > 0)
        {
            Node anode = idNodes.item(0).getFirstChild();
            m_postFilterId = (anode == null) ? "-2" : anode.getNodeValue();
        }
        
        NodeList tnameNodes = tagElement.getElementsByTagName("postFilterTableName");
        if (tnameNodes != null && tnameNodes.getLength() > 0)
        {
            Node anode = tnameNodes.item(0).getFirstChild();
            m_postFilterTableName = (anode == null) ? "" : anode.getNodeValue();
        }
    }
    
    public boolean isMatched(Node p_node)
    {
        if (m_conditions == null || m_conditions.size() == 0)
        {
            return true;
        }
        
        for (XmlFilterCDataCondition cond : m_conditions)
        {
            if (!cond.match(p_node))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isTranslatable()
    {
        return m_translatable;
    }
    
    public long getPostFilterId()
    {
        try
        {
            return Long.parseLong(m_postFilterId);
        }
        catch (Exception e)
        {
            return -2;
        }
    }
    
    public String getPostFilterTableName()
    {
        return m_postFilterTableName == null ? "" : m_postFilterTableName;
    }
    
    public Filter getPostFilter() throws Exception
    {
        long filterId = getPostFilterId();
        String filterTableName = getPostFilterTableName();

        if (filterId >= 0 && filterTableName != null)
        {
            return FilterHelper.getFilter(filterTableName, filterId);
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public String toString()
    {
        return m_Name;
    }
    
    private List<XmlFilterCDataCondition> buildConditions(NodeList conditionNodes)
    {
        List<XmlFilterCDataCondition> ret = new ArrayList<XmlFilterCDataCondition>();
        if (conditionNodes != null && conditionNodes.getLength() > 0)
        {
            int length = conditionNodes.getLength();
            for (int i = 0; i < length; i++)
            {
                Node node = conditionNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element el = (Element) node;
                    String aType = el.getElementsByTagName("aType").item(0).getFirstChild()
                            .getNodeValue();
                    String operator = el.getElementsByTagName("aOp").item(0).getFirstChild()
                            .getNodeValue();
                    String aValue = el.getElementsByTagName("aValue").item(0).getFirstChild()
                            .getNodeValue();
                    XmlFilterCDataCondition xa = new XmlFilterCDataCondition(aType, operator, aValue);
                    ret.add(xa);
                }
            }
        }

        return ret;
    }
}

package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFilterProcessIns
{
    private String name = null;
    private int handleType = -1;

    private List<String> m_transAttributes = null;

    public XmlFilterProcessIns()
    {
        m_transAttributes = new ArrayList<String>();
    }

    public XmlFilterProcessIns(Element tagElement)
    {
        String nodeName = "aName";
        name = XmlFilterTag.getChildByName(tagElement, nodeName);

        Node modeElement = tagElement.getElementsByTagName("handleType")
                .item(0);
        String tt = modeElement.getFirstChild().getNodeValue();

        try
        {
            handleType = Integer.parseInt(tt);
        }
        catch (Exception e)
        {
            handleType = -1;
        }

        NodeList transAttributeNodes = tagElement
                .getElementsByTagName("piTransAttributes");
        buildTransAttributes(transAttributeNodes);
    }
    
    public List<String> getTransAttributes()
    {
        return m_transAttributes;
    }

    public String getName()
    {
        return name;
    }

    public int getHandleType()
    {
        return handleType;
    }

    public boolean isNameMatched(String aName)
    {
        if (aName == null)
        {
            return false;
        }
        else
        {
            return aName.equals(name);
        }
    }

    private void buildTransAttributes(NodeList transAttributeNodes)
    {
        m_transAttributes = new ArrayList<String>();

        if (transAttributeNodes != null && transAttributeNodes.getLength() > 0)
        {
            int length = transAttributeNodes.getLength();
            for (int i = 0; i < length; i++)
            {
                Node node = transAttributeNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element el = (Element) node;
                    String attrName = el.getElementsByTagName("aName").item(0)
                            .getFirstChild().getNodeValue();
                    // String transAttrSegRule =
                    // el.getElementsByTagName("transAttrSegRule").item(0).getFirstChild()
                    // .getNodeValue();

                    m_transAttributes.add(attrName);
                }
            }
        }
    }
}

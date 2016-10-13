/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.docproc.extractor.xliff20;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeInfo implements INodeInfo
{
    protected String format = new String();

    public NodeInfo()
    {
        format = "";
    }

    public NodeInfo(String p_format)
    {
        this.format = p_format;
    }

    @Override
    public Map<String, String> getNodeTierInfo(Node node)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(XliffHelper.MARK_XLIFF_PART, "");
        Node parentNode = node;
        while (parentNode != null)
        {
            String name = parentNode.getNodeName().toLowerCase();
            if (XliffHelper.SOURCE.equals(name)
                    || XliffHelper.TARGET.equals(name))
            {
                Node pNode = parentNode.getParentNode();
                if (pNode != null)
                {
                    String pNodeName = pNode.getNodeName().toLowerCase();
                    if (XliffHelper.SEGMENT.equals(pNodeName))
                    {
                        if (XliffHelper.SOURCE.equals(name))
                        {
                            map.put(XliffHelper.MARK_XLIFF_PART,
                                    XliffHelper.SOURCE);

                            NodeList ns = parentNode.getChildNodes();
                            if (ns != null && ns.getLength() == 1)
                            {
                                Node n = ns.item(0);
                                if (n.getNodeType() == Node.ELEMENT_NODE
                                        && "mrk".equalsIgnoreCase(n
                                                .getNodeName()))
                                {
                                    NamedNodeMap atts = n.getAttributes();
                                    Node idNode = atts.getNamedItem("id");
                                    if (idNode != null)
                                    {
                                        map.put(XliffHelper.MRK_ID,
                                                idNode.getNodeValue());
                                    }
                                }
                            }
                        }
                        else if (XliffHelper.TARGET.equals(name))
                        {
                            map.put(XliffHelper.MARK_XLIFF_PART,
                                    XliffHelper.TARGET);
                        }
                        // <segment id="1001" state="initial">
                        NamedNodeMap segmentAttrs = pNode.getAttributes();
                        for (int i = 0; i < segmentAttrs.getLength(); i++)
                        {
                            Node attr = segmentAttrs.item(i);
                            String attrName = attr.getNodeName().toLowerCase();
                            String value = attr.getNodeValue();
                            if (XliffHelper.ATTR_ID.equals(attrName))
                            {
                                map.put(XliffHelper.MARK_TUV_ID, value);
                            }
                        }
                        // <unit id="u1001" translate="yes">
                        Node ppNode = pNode.getParentNode();
                        if (ppNode != null)
                        {
                            String ppNodeName = ppNode.getNodeName()
                                    .toLowerCase();
                            if (XliffHelper.UNIT.equals(ppNodeName))
                            {
                                NamedNodeMap unitAttrs = ppNode.getAttributes();
                                for (int i = 0; i < unitAttrs.getLength(); i++)
                                {
                                    Node attr = unitAttrs.item(i);
                                    String attrName = attr.getNodeName()
                                            .toLowerCase();
                                    String value = attr.getNodeValue();
                                    if (XliffHelper.ATTR_TRANSLATE
                                            .equals(attrName))
                                    {
                                        map.put(XliffHelper.ATTR_TRANSLATE,
                                                value);
                                    }
                                    else if (XliffHelper.ATTR_ID
                                            .equals(attrName))
                                    {
                                        map.put(XliffHelper.MARK_TU_ID, value);
                                    }
                                }
                            }
                        }
                    }
                    else if (XliffHelper.MATCH.equals(pNodeName)) 
                    {
                        if (XliffHelper.SOURCE.equals(name))
                        {
                            map.put(XliffHelper.MARK_XLIFF_PART, XliffHelper.ALT_SOURCE);
                        }
                        else if (XliffHelper.TARGET.equals(name))
                        {
                            map.put(XliffHelper.MARK_XLIFF_PART, XliffHelper.ALT_TARGET);
                            map.put(XliffHelper.XLIFF_VERSION, XliffHelper.XLIFF_VERSION_20);
                        }

                        String attname = null;
                        NamedNodeMap grandAttrs = parentNode.getParentNode()
                                .getAttributes();

                        if (grandAttrs != null)
                        {
                            for (int i = 0; i < grandAttrs.getLength(); ++i)
                            {
                                Node att = grandAttrs.item(i);
                                attname = att.getNodeName();
                                String value = att.getNodeValue();

                                if (attname.equalsIgnoreCase("matchQuality"))
                                {
                                    map.put("altQuality", value);
                                }
                                else if (attname.equals("ref"))
                                {
                                    // the value is always start with #
                                    map.put("altMid", value.substring(1));
                                }
                            }
                        }
                    }
                }
            }
            parentNode = parentNode.getParentNode();
        }
        return map;
    }
}

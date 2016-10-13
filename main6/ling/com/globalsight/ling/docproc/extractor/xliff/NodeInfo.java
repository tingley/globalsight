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


package com.globalsight.ling.docproc.extractor.xliff;

import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.ExtractorRegistry;

public class NodeInfo implements INodeInfo
{
    protected String format = new String();
    
    public NodeInfo() {
        format = "";
    }
    
    public NodeInfo(String p_format) 
    {
        this.format = p_format;
    }
    
    @Override
    public HashMap<String, String> getNodeTierInfo(Node node)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("xliffPart", "");
        Node parentNode = node;

        while (parentNode != null)
        {
            String name = parentNode.getNodeName();
            
            if ("mrk".equals(name))
            {
                if (parentNode.getParentNode() != null)
                {
                    if (parentNode.getParentNode().getNodeName()
                            .equalsIgnoreCase("seg-source")
                            || parentNode.getParentNode().getNodeName()
                                    .equalsIgnoreCase("target"))
                    {
                        NamedNodeMap atts = parentNode.getAttributes();
                        if (atts != null)
                        {
                            Node mid = atts.getNamedItem("mid");
                            if (mid != null)
                            {
                                map.put("xliffSegSourceMrkId",
                                        mid.getNodeValue());
                            }
                        }
                        
                        int index = 0;
                        Node pppNode = parentNode.getParentNode();
                        NodeList jnodes = pppNode.getChildNodes();
                        if (jnodes != null)
                        {
                            for (int j = 0; j < jnodes.getLength(); j++)
                            {
                                Node jnode = jnodes.item(j);
                                String jnodeName = jnode.getNodeName();
                                if ("mrk".equals(jnodeName))
                                {
                                    if (jnode.equals(parentNode))
                                    {
                                        map.put("xliffSegSourceMrkIndex", ""
                                                + index);
                                        break;
                                    }
                                    else
                                    {
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else if ("source".equals(name) || "target".equals(name) || "seg-source".equals(name))
            {
                if (parentNode.getParentNode() != null)
                {
                    if (parentNode.getParentNode().getNodeName()
                            .equalsIgnoreCase("alt-trans"))
                    {
                        if ("source".equals(name))
                        {
                            map.put("xliffPart", "altSource");
                        }
                        else if ("target".equals(name))
                        {
                            map.put("xliffPart", "altTarget");
                        }
                        else if ("seg-source".equals(name))
                        {
                            map.put("xliffPart", "altSource");
                        }

                        NamedNodeMap attrs = parentNode.getAttributes();
                        String attname = null;

                        if (attrs != null)
                        {
                            for (int i = 0; i < attrs.getLength(); ++i)
                            {
                                Node att = attrs.item(i);
                                attname = att.getNodeName();
                                String value = att.getNodeValue();

                                if (attname.equals("xml:lang"))
                                {
                                    map.put("altLanguage", value);
                                    break;
                                }
                            }
                        }

                        NamedNodeMap grandAttrs = parentNode.getParentNode()
                                .getAttributes();

                        if (grandAttrs != null)
                        {
                            for (int i = 0; i < grandAttrs.getLength(); ++i)
                            {
                                Node att = grandAttrs.item(i);
                                attname = att.getNodeName();
                                String value = att.getNodeValue();

                                if (attname.equals("match-quality"))
                                {
                                    map.put("altQuality", value);
                                }
                                else if (attname.equals("mid"))
                                {
                                    map.put("altMid", value);
                                }
                            }
                        }
                    }
                    else if (parentNode.getParentNode().getNodeName()
                            .equalsIgnoreCase("trans-unit"))
                    {
                        if ("source".equals(name))
                        {
                            map.put("xliffPart", "source");
                        }
                        else if ("target".equals(name))
                        {
                            map.put("xliffPart", "target");
                            
                            if (ExtractorRegistry.FORMAT_PASSOLO.equals(format))
                            {
                                NamedNodeMap grandAttrs = parentNode.getAttributes();
                                for (int i = 0; i < grandAttrs.getLength(); ++i)
                                {
                                    Node att = grandAttrs.item(i);
                                    String attname = att.getNodeName();
                                    String value = att.getNodeValue();

                                    if (attname.equals("state"))
                                    {
                                        map.put("passoloState", value);
                                        break;
                                    }
                                }
                            }
                        }
                        else if ("seg-source".equals(name))
                        {
                            map.put("xliffPart", "seg-source");
                        }
                        
                        NodeList nodes = parentNode.getParentNode().getChildNodes();
                        for (int i = 0; i < nodes.getLength(); i++)
                        {
                            Node curNode = nodes.item(i);
                            String curNodeName = curNode.getNodeName();

                            if ("seg-source".equals(curNodeName))
                            {
                                map.put("xliffSegSource", "true");
                                NodeList jnodes = curNode.getChildNodes();
                                if (jnodes != null)
                                {
                                    int mrkCount = 0;
                                    String mrkIds = "";
                                    for (int j = 0; j < jnodes.getLength(); j++)
                                    {
                                        Node jnode = jnodes.item(j);
                                        String jnodeName = jnode.getNodeName();
                                        if ("mrk".equals(jnodeName))
                                        {
                                            map.put("xliffSegSourceMrk", "true");
                                            String jnodeValue = jnode.getTextContent();
                                            
                                            if (jnodeValue == null
                                                    || Text.isBlank(jnodeValue))
                                            {
                                                continue;
                                            }
                                            
                                            mrkCount++;
                                            NamedNodeMap atts = jnode
                                                    .getAttributes();
                                            if (atts != null)
                                            {
                                                Node mid = atts
                                                        .getNamedItem("mid");
                                                if (mid != null)
                                                {
                                                    mrkIds = (mrkIds.equals("") ? ""
                                                            : mrkIds + ",")
                                                            + mid.getNodeValue();
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (mrkCount > 0)
                                    {
                                        map.put("xliffSegSourceMrkCount", "" + mrkCount);
                                        map.put("xliffSegSourceMrkIds", mrkIds);
                                    }
                                }
                            }
                            else if ("target".equals(name))
                            {
                                NodeList jnodes = curNode.getChildNodes();
                                if (jnodes != null)
                                {
                                    for (int j = 0; j < jnodes.getLength(); j++)
                                    {
                                        Node jnode = jnodes.item(j);
                                        String jnodeName = jnode.getNodeName();
                                        if ("mrk".equals(jnodeName))
                                        {
                                            map.put("xliffTargetMrk", "true");
                                            break;
                                        }
                                    }
                                }
                            }

							// Store "state" and "state-qualifier" attributes
							// from "target" for both "source" and "target".
							if ("target".equals(curNodeName)
									&& ("source".equals(name) || "target".equals(name)))
                            {
                                NamedNodeMap targetAttrs = curNode.getAttributes();
                                if (targetAttrs != null)
                                {
                                    for (int k = 0; k < targetAttrs.getLength(); ++k)
                                    {
                                        Node att = targetAttrs.item(k);
                                        String attname = att.getNodeName();
                                        String value = att.getNodeValue();
                                        if ("state".equals(attname))
                                        {
                                            map.put("state", value);
                                        }
                                        else if ("state-qualifier".equals(attname))
                                        {
                                            map.put("state-qualifier", value);
                                        }
                                    }
                                }
                            }                            
                        }

                        NamedNodeMap grandAttrs = parentNode.getParentNode()
                                .getAttributes();
                        String attname = null;

                        for (int i = 0; i < grandAttrs.getLength(); ++i)
                        {
                            Node att = grandAttrs.item(i);
                            attname = att.getNodeName();
                            String value = att.getNodeValue();

                            if (attname.equals("id")) {
                                map.put("tuID", value);
                            } else if (attname.equals("translate")) {
                                map.put("translate", value);
                            } else if (attname.equals("resname")){
                                    //ExtractorRegistry.FORMAT_PASSOLO.equals(format)) {
                                    XmlEntities encoder = new XmlEntities();
                                    map.put("resname", encoder.encodeStringBasic(encoder.encodeStringBasic(value)));
                            }
                        }
                    }
                }

                break;
            }

            parentNode = parentNode.getParentNode();
        }

        return map;
    }
}

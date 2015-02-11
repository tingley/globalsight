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

public class WSNodeInfo extends NodeInfo implements INodeInfo
{
    public WSNodeInfo() {
        super();
    }
    
    public WSNodeInfo(String format) 
    {
        super(format);
    }

    @Override
    public HashMap<String, String> getNodeTierInfo(Node node)
    {
        HashMap<String, String> map = super.getNodeTierInfo(node);

        Node parentNode = node;

        while (parentNode != null)
        {
            String name = parentNode.getNodeName();

            if ("source".equals(name) || "target".equals(name))
            {
                Node grandNode = parentNode.getParentNode();
                NodeList childs = grandNode.getChildNodes();

                for (int i = 0; i < childs.getLength(); i++)
                {
                    Node childNode = childs.item(i);

                    if (childNode.getNodeName().equalsIgnoreCase(
                            Extractor.IWS_SEGMENT_DATA))
                    {
                        map.putAll(segmentDataChildrenProcess(childNode));
                        map.putAll(segmentDataProcess(childNode));
                    }
                }

                break;
            }

            parentNode = parentNode.getParentNode();
        }

        return map;
    }
    
    
    private HashMap<String, String> segmentDataProcess(Node node) {
        HashMap<String, String> map = new HashMap<String, String>();
        
        NamedNodeMap attrs = node.getAttributes();

        for (int x = 0; x < attrs.getLength(); x++)
        {
            Node attr = attrs.item(x);
            if (attr.getNodeName().equals(Extractor.IWS_TM_SCORE))
            {
                map.put(Extractor.IWS_TM_SCORE, attr.getNodeValue());
            }
            else if (attr.getNodeName().equals(Extractor.IWS_SID))
            {
                map.put(Extractor.IWS_SID, attr.getNodeValue());
            }
            else if (Extractor.IWS_WORDCOUNT.equals(attr.getNodeName()))
            {
                map.put(Extractor.IWS_WORDCOUNT, attr.getNodeValue());
            }
        }
        
        return map;
    }
    
    private HashMap<String, String> segmentDataChildrenProcess(Node node) {
        HashMap<String, String> map = new HashMap<String, String>();
        
        NodeList segNodes = node.getChildNodes();

        for (int j = 0; j < segNodes.getLength(); j++)
        {
            Node segNode = segNodes.item(j);
            if (Extractor.IWS_STATUS.equalsIgnoreCase(segNode
                    .getNodeName()))
            {
                map.putAll(StatusProcess(segNode));
            }
        }
        
        return map;
    }
    
    private HashMap<String, String> StatusProcess(Node segNode) {
        HashMap<String, String> map = new HashMap<String, String>();
        
        NamedNodeMap attrs = segNode.getAttributes();

        for (int x = 0; x < attrs.getLength(); x++)
        {
            Node attr = attrs.item(x);
            // translation_type
            setStatusValue(attr, map, Extractor.IWS_TRANSLATION_TYPE);
            // source_content
            setStatusValue(attr, map, Extractor.IWS_SOURCE_CONTENT);
            
            setStatusValue(attr, map, Extractor.IWS_LOCK_STATUS);
        }
        
        return map;
    }
    
    private void setStatusValue(Node attr, HashMap<String, String> map,
            String mapKey)
    {
        if (mapKey.equals(attr.getNodeName()))
        {
            String keyValue = attr.getNodeValue();
            if (keyValue != null && keyValue.trim().length() > 0)
            {
                map.put(mapKey, keyValue);
            }
        }
    }
}

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

package com.globalsight.ling.docproc.extractor.xml.xmlrule;

import java.lang.reflect.Field;
import java.util.Map;

import com.sun.org.apache.xerces.internal.dom.NodeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.Rule;


public class SidRuleItem extends XmlRuleItem
{
    public static final String NAME = "sid";

    private NotTranslateRuleItem item = new NotTranslateRuleItem();

    private void updateSid(Map ruleMap, Node node, String sid)
    {
        Rule rule = (Rule) ruleMap.get(node);
        if (rule == null)
        {
            rule = new Rule();
            ruleMap.put(node, rule);
        }
        rule.setSid(sid);

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null)
        {
            for (int i = 0; i < attributes.getLength(); i++)
            {
                Node attr = attributes.item(i);
                updateSid(ruleMap, attr, sid);
            }
        }

        NodeList nodes = node.getChildNodes();
        if (nodes != null)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node childNode = nodes.item(i);
                updateSid(ruleMap, childNode, sid);
            }
        }
    }

    public void applySidRule(Node ruleNode, Document toBeExtracted,
            Map ruleMap, Object[] namespaces) throws Exception
    {
        Rule rule = new Rule();
        String name = "sid";

        NamedNodeMap attributes = ruleNode.getAttributes();
        String xpath = attributes.getNamedItem("path").getNodeValue();
        xpath = fixXPath(xpath);
        Node nameNode = attributes.getNamedItem("name");

        if (nameNode != null)
        {
            name = nameNode.getNodeValue();
        }

        NodeList affectedNodes = selectNodeList(toBeExtracted, xpath);
        if (affectedNodes != null)
        {
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Node node = affectedNodes.item(l);
                Node sidNode = null;
                Node rootNode = node;

                if (Node.ATTRIBUTE_NODE == node.getNodeType())
                {
                    sidNode = node;
                    if (node instanceof NodeImpl)
                    {
                        Field f = NodeImpl.class.getDeclaredField("ownerNode");
                        f.setAccessible(true);
                        rootNode = (Node) f.get(node);
                    }
                    else if (node instanceof org.apache.xerces.dom.NodeImpl)
                    {
                        Field f = org.apache.xerces.dom.NodeImpl.class
                                .getDeclaredField("ownerNode");
                        f.setAccessible(true);
                        rootNode = (Node) f.get(node);
                    }
                }
                else
                {
                    NamedNodeMap atts = node.getAttributes();
                    if (atts != null)
                    {
                        sidNode = atts.getNamedItem(name);
                    }
                }

                if (sidNode != null)
                {
                    updateSid(ruleMap, rootNode, sidNode.getNodeValue());
                }

                Rule previousRule = (Rule) ruleMap.get(node);
                Rule newRule = rule;

                if (previousRule != null)
                {
                    newRule = previousRule.merge(rule);
                }

                ruleMap.put(node, newRule);
            }
        }
    }

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws Exception
    {
        applySidRule(ruleNode, toBeExtracted, ruleMap, namespaces);
        item.applyRule(ruleNode, toBeExtracted, ruleMap, namespaces);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

}

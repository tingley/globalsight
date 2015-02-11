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

import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.Rule;

public class CommentRuleItem extends XmlRuleItem
{
    public static final String NAME = "comment";
    private NotTranslateRuleItem item = new NotTranslateRuleItem();

    public static void updateCommend(Map ruleMap, Node node, String comment)
    {
        Rule rule = (Rule) ruleMap.get(node);
        if (rule == null)
        {
            rule = new Rule();
            ruleMap.put(node, rule);
        }

        if (!rule.isSrcCommentNode())
        {
            String cmt = (comment == null || "".equals(comment.trim())) ? null : comment.trim();
            rule.setSrcComment(cmt);
        }
    }

    public static boolean isSrcCommentNode(Map ruleMap, Node node)
    {
        if (ruleMap == null)
        {
            return false;
        }

        Rule rule = (Rule) ruleMap.get(node);
        if (rule != null)
        {
            return rule.isSrcCommentNode();
        }
        else
        {
            return false;
        }
    }

    public static void updateCommendForChildTextNode(Map ruleMap, Node node, String comment)
    {
        if (node == null || ruleMap == null || comment == null)
        {
            return;
        }

        NodeList list = node.getChildNodes();

        for (int l = 0; l < list.getLength(); ++l)
        {
            Node child = list.item(l);

            if (isSrcCommentNode(ruleMap, child))
            {
                break;
            }

            if (child.getNodeType() == Node.TEXT_NODE)
            {
                updateCommend(ruleMap, child, comment);
            }
            else if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                updateCommendForChildTextNode(ruleMap, child, comment);
            }
        }
    }

    public static void updateCommendForNextTextNode(Map ruleMap, Node node, String comment)
    {
        if (node == null || ruleMap == null || comment == null)
        {
            return;
        }

        Node nextNode = node.getNextSibling();
        boolean elementProcessed = false;

        while (nextNode != null)
        {
            if (isSrcCommentNode(ruleMap, nextNode))
            {
                break;
            }

            if (nextNode.getNodeType() == Node.TEXT_NODE)
            {
                updateCommend(ruleMap, nextNode, comment);
            }
            else if (nextNode.getNodeType() == Node.ELEMENT_NODE)
            {
                updateCommendForChildTextNode(ruleMap, nextNode, comment);
                elementProcessed = true;
            }
            
            if (elementProcessed)
            {
                break;
            }

            nextNode = nextNode.getNextSibling();
        }
    }

    public void applyCommentRule(Node ruleNode, Document toBeExtracted, Map ruleMap,
            Object[] namespaces) throws Exception
    {
        NamedNodeMap attributes = ruleNode.getAttributes();
        String xpath = attributes.getNamedItem("path").getNodeValue();
        xpath = fixXPath(xpath);

        NodeList affectedNodes = selectNodeList(toBeExtracted, xpath);
        if (affectedNodes != null)
        {
            // set src comment nodes' properties
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Node node = affectedNodes.item(l);
                if (Node.ATTRIBUTE_NODE == node.getNodeType()
                        || Node.ELEMENT_NODE == node.getNodeType())
                {
                    CommentRuleItem.setSrcCommentNodeProperties(node, ruleMap);
                }
            }

            // set src comments for other nodes
            for (int l = 0; l < affectedNodes.getLength(); ++l)
            {
                Node node = affectedNodes.item(l);

                String srcComment = null;

                if (Node.ATTRIBUTE_NODE == node.getNodeType())
                {
                    srcComment = node.getNodeValue();
                    Attr at = (Attr) node;
                    Node parentNode = at.getOwnerElement();
                    if (srcComment != null)
                    {
                        updateCommendForChildTextNode(ruleMap, parentNode, srcComment);
                    }

                    setSrcCommentNodeProperties(node, ruleMap);
                }
                else if (Node.ELEMENT_NODE == node.getNodeType())
                {
                    try
                    {
                        srcComment = node.getChildNodes().item(0).getNodeValue();
                    }
                    catch (Exception e)
                    {
                        // ignore e
                        srcComment = null;
                    }

                    if (srcComment != null)
                    {
                        updateCommendForNextTextNode(ruleMap, node, srcComment);
                    }

                    setSrcCommentNodeProperties(node, ruleMap);
                }
            }
        }
    }

    public static void setSrcCommentNodeProperties(Node node, Map ruleMap)
    {
        if (node == null)
        {
            return;
        }

        Rule rule = new Rule();
        rule.setTranslate(false);
        rule.setIsSrcCommentNode(true);

        if (ruleMap.containsKey(node))
        {
            Rule pRule = (Rule) ruleMap.get(node);
            rule = rule.merge(pRule);
        }

        ruleMap.put(node, rule);
    }

    @Override
    public void applyRule(Node ruleNode, Document toBeExtracted, Map ruleMap, Object[] namespaces)
            throws Exception
    {
        applyCommentRule(ruleNode, toBeExtracted, ruleMap, namespaces);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

}

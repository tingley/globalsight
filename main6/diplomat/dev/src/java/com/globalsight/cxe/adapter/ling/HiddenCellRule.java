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
package com.globalsight.cxe.adapter.ling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;
import com.globalsight.ling.docproc.extractor.xml.Rule;

public class HiddenCellRule implements ExtractRule
{

    static private final Logger s_logger = Logger
            .getLogger(HiddenCellRule.class);

    private List<String> m_hiddenCell = new ArrayList<String>();

    /**
     * @see com.globalsight.cxe.adapter.ling.ExtractRule#buildRule(org.w3c.dom.Document,
     *      java.util.Map)
     */
    @Override
    public void buildRule(Document toBeExtracted, Map ruleMap)
    {
        if (m_hiddenCell == null || m_hiddenCell.size() == 0)
            return;

        buildRuleForSheet(toBeExtracted, ruleMap);
        buildRuleForComment(toBeExtracted, ruleMap);
    }

    private void buildRuleForComment(Document toBeExtracted, Map ruleMap)
    {
        String xpath = "//*[local-name()=\"comment\"]";
        NodeList affectedNodes = null;
        try
        {
            affectedNodes = XPathAPI.selectNodeList(
                    toBeExtracted.getDocumentElement(), xpath);
        }
        catch (Exception e)
        {
            s_logger.error(e);
            return;
        }

        XmlUtil util = new XmlUtil();
        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            for (int i = 0; i < affectedNodes.getLength(); i++)
            {
                Element comment = (Element) affectedNodes.item(i);
                String ref = comment.getAttribute("ref");
                if (m_hiddenCell.contains(ref))
                {
                    notTranslate(comment, ruleMap);
                    List<Node> ns = new ArrayList<Node>();
                    util.getAllNodes(comment, "t", ns);
                    for (Node n : ns)
                    {
                        notTranslate(n, ruleMap);
                    }
                }
            }
        }
    }

    private void buildRuleForSheet(Document toBeExtracted, Map ruleMap)
    {
        String xpath = "//*[local-name()=\"c\"]";
        NodeList affectedNodes = null;
        try
        {
            affectedNodes = XPathAPI.selectNodeList(
                    toBeExtracted.getDocumentElement(), xpath);
        }
        catch (Exception e)
        {
            s_logger.error(e);
            return;
        }

        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            for (int i = 0; i < affectedNodes.getLength(); i++)
            {
                Element c = (Element) affectedNodes.item(i);
                String r = c.getAttribute("r");
                if (m_hiddenCell.contains(r))
                {
                    notTranslate(c, ruleMap);
                }

                NodeList ns = c.getChildNodes();
                if (ns != null && ns.getLength() > 0)
                {
                    for (int j = 0; j < ns.getLength(); j++)
                    {
                        notTranslate(ns.item(j), ruleMap);
                    }
                }
            }
        }
    }

    private void notTranslate(Node n, Map ruleMap)
    {
        Rule rule = new Rule();
        rule.setTranslate(false);

        if (ruleMap.containsKey(n))
        {
            Rule pRule = (Rule) ruleMap.get(n);
            rule = rule.merge(pRule);
        }

        ruleMap.put(n, rule);
    }

    /**
     * @return the hiddenCell
     */
    public List<String> getHiddenCell()
    {
        return m_hiddenCell;
    }

    /**
     * @param hiddenCell
     *            the hiddenCell to set
     */
    public void setHiddenCell(List<String> hiddenCell)
    {
        m_hiddenCell = hiddenCell;
    }

}

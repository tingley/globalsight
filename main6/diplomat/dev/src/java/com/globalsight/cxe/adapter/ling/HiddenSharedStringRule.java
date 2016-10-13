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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.ling.docproc.extractor.xml.Rule;
import com.globalsight.ling.docproc.extractor.xml.XPathAPIJdk;

public class HiddenSharedStringRule implements ExtractRule
{
    static private final Logger s_logger = Logger
            .getLogger(HiddenSharedStringRule.class);

    private List<String> hiddenSharedSI = new ArrayList<String>();

    /**
     * @return the hiddenSharedSI
     */
    public List<String> getHiddenSharedSI()
    {
        return hiddenSharedSI;
    }

    /**
     * @param hiddenSharedSI
     *            the hiddenSharedSI to set
     */
    public void setHiddenSharedSI(List<String> hiddenSharedSI)
    {
        this.hiddenSharedSI = hiddenSharedSI;
    }

    /**
     * @see com.globalsight.cxe.adapter.ling.ExtractRule#buildRule(org.w3c.dom.Document,
     *      java.util.Map)
     */
    @Override
    public void buildRule(Document toBeExtracted, Map ruleMap)
    {
        if (hiddenSharedSI == null || hiddenSharedSI.size() == 0)
            return;

        String xpath = "//*[local-name()=\"si\"]";
        NodeList affectedNodes = null;
        try
        {
            affectedNodes = XPathAPIJdk.selectNodeList(
                    toBeExtracted.getDocumentElement(), xpath);
        }
        catch (Exception e)
        {
            s_logger.error(e);
            return;
        }

        for (String si : hiddenSharedSI)
        {
            int i = Integer.parseInt(si);
            Node n = affectedNodes.item(i);

            NodeList ns = n.getChildNodes();
            if (ns != null && ns.getLength() > 0)
            {
                for (int j = 0; j < ns.getLength(); j++)
                {
                    notTranslate(ns.item(j), ruleMap);
                }
            }

            notTranslate(n, ruleMap);
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
}

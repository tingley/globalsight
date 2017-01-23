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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports property file segmentation rule info.
 */
public class SegmentationRuleImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(SegmentationRuleImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;

    public SegmentationRuleImporter(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = currentCompanyId;
        this.importToCompId = importToCompId;
    }

    /**
     * Analysis and imports upload file.
     */
    public void analysisAndImport(File uploadedFile)
    {
        try
        {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(uploadedFile);

            Element rootElm = document.getRootElement();
            List<?> ruleNodes = rootElm.elements("SegmentationRule");
            int size = ruleNodes.size();
            for (int i = 0; i < size; i++)
            {
                Element ruleNode = (Element) ruleNodes.get(i);
                addNewRule(ruleNode);
            }
            addMessage("<b>Done importing Segmentation Rules.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Segmentation Rule.", e);
            addToError(e.getMessage());
        }
    }

    private String getSRNewName(String oldName, long companyId)
    {
        String hql = "select sr.name from SegmentationRuleFileImpl "
                + "  sr where sr.companyId=:companyid";
        Map map = new HashMap();
        map.put("companyid", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(oldName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (oldName.contains("_import_"))
                {
                    returnStr = oldName.substring(0, oldName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = oldName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return oldName;
        }
    }

    /**
     * Adds new segmentation rule.
     */
    private void addNewRule(Element segRuleNode)
    {
        try
        {
            String text = "";
            SegmentationRuleFileImpl segRule = new SegmentationRuleFileImpl();
            String segRuleName = segRuleNode.element("NAME").getText();
            String segRuleNewName = getSRNewName(segRuleName, Long.parseLong(currentCompanyId));
            segRule.setName(segRuleNewName);

            text = segRuleNode.element("RULE_TEXT").getText();
            segRule.setRuleText(text);
            if (importToCompId != null && !importToCompId.equals("-1"))
            {
                segRule.setCompanyId(Long.parseLong(importToCompId));
            }
            else
            {
                segRule.setCompanyId(Long.parseLong(currentCompanyId));
            }
            text = segRuleNode.element("SR_TYPE").getText();
            segRule.setType(Integer.parseInt(text));
            text = segRuleNode.element("DESCRIPTION").getText();
            segRule.setDescription(text);
            text = segRuleNode.element("IS_ACTIVE").getText();
            segRule.setIsActive(Boolean.parseBoolean(text));
            text = segRuleNode.element("IS_DEFAULT").getText();
            segRule.setIsDefault(Boolean.parseBoolean(text));
            ServerProxy.getSegmentationRuleFilePersistenceManager().createSegmentationRuleFile(
                    segRule);
            if (segRuleName.equals(segRuleNewName))
            {
                addMessage("<b>" + segRuleNewName + "</b> imported successfully.");
            }
            else
            {
                addMessage(" Segmentation Rule name <b>" + segRuleName + "</b> already exists. <b>"
                        + segRuleNewName + "</b> imported successfully.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

public class AttributeSetImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(AttributeSetImporter.class);
    private String sessionId;
    private Long companyId;

    /**
     * Imports attribute group info to system.
     */
    public AttributeSetImporter(String sessionId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        if (importToCompId != null && !importToCompId.equals("-1"))
        {
            this.companyId = Long.parseLong(importToCompId);
        }
        else
        {
            this.companyId = Long.parseLong(companyId);
        }
    }

    /**
     * Analysis and imports upload file.
     */
    public void analysisAndImport(File uploadedFile)
    {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

        try
        {
            String[] keyArr = null;
            String key = null;
            String strKey = null;
            String strValue = null;
            InputStream is;
            is = new FileInputStream(uploadedFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            Properties prop = new Properties();
            prop.load(bf);
            Enumeration enum1 = prop.propertyNames();
            while (enum1.hasMoreElements())
            {
                // The key profile
                strKey = (String) enum1.nextElement();
                key = strKey.substring(0, strKey.lastIndexOf('.'));
                keyArr = strKey.split("\\.");
                // Value in the properties file
                strValue = prop.getProperty(strKey);
                Set<String> keySet = map.keySet();
                if (keySet.contains(key))
                {
                    Map<String, String> valueMap = map.get(key);
                    Set<String> valueKey = valueMap.keySet();
                    if (!valueKey.contains(keyArr[2]))
                    {
                        valueMap.put(keyArr[2], strValue);
                    }
                }
                else
                {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put(keyArr[2], strValue);
                    map.put(key, valueMap);
                }
            }
            // Data analysis
            analysisData(map);
        }
        catch (Exception e)
        {
            logger.error("Failed to parse the file", e);
        }
    }

    private void analysisData(Map<String, Map<String, String>> map) throws ParseException
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<AttributeSet> attributeSetList = new ArrayList<AttributeSet>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("AttributeGroup"))
                {
                    AttributeSet attributeSet = putDataIntoAttributeSet(valueMap);
                    attributeSetList.add(attributeSet);
                }
            }
        }

        if (attributeSetList.size() > 0)
            dataMap.put("AttributeGroup", attributeSetList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("AttributeGroup"))
            {
                storeAtttributeSetData(dataMap);
            }
            addMessage("<b> Done importing Attribute Groups.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Attribute Groups.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeAtttributeSetData(Map<String, List> dataMap)
    {
        List<AttributeSet> attributeSetList = dataMap.get("AttributeGroup");
        try
        {
            for (int i = 0; i < attributeSetList.size(); i++)
            {
                AttributeSet attributeSet = attributeSetList.get(i);
                // saves attributes
                List<Attribute> attrList = attributeSet.getAttributeAsList();
                if (attrList.size() == 0)
                {

                }
                else
                {
                    String oldName = attributeSet.getName();
                    String newName = getAttributeSetNewName(oldName, attributeSet.getCompanyId());
                    attributeSet.setName(newName);
                    HibernateUtil.save(attributeSet);
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Attribute Group name <b>" + oldName
                                + "</b> already exists. <b>" + newName
                                + "</b> imported successfully.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Attribute Group data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private AttributeSet putDataIntoAttributeSet(Map<String, String> valueMap)
            throws ParseException
    {
        AttributeSet attributeSet = new AttributeSet();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if ("NAME".equalsIgnoreCase(keyField))
            {
                attributeSet.setName(valueField);
            }
            else if ("DESCRIPTION".equalsIgnoreCase(keyField))
            {
                if (StringUtil.isNotEmptyAndNull(valueField))
                {
                    attributeSet.setDescription(valueField);
                }
                else
                {
                    attributeSet.setDescription(null);
                }
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                attributeSet.setCompanyId(companyId);
            }
            else if ("ATTRIBUTE_NAMES".equalsIgnoreCase(keyField))
            {
                Set<Attribute> attributes = new HashSet<Attribute>();
                String[] attributeNames = valueField.split(",");
                for (String attributeName : attributeNames)
                {
                    List<Attribute> attributeList = (List<Attribute>) AttributeManager
                            .getAllAttributes(companyId);
                    for (Attribute attribute : attributeList)
                    {
                        String attrName = attribute.getName();
                        if (attrName.equals(attributeName)
                                || attrName.startsWith(attributeName + "_import_"))
                        {
                            attributes.add(attribute);
                            break;
                        }
                    }
                }
                attributeSet.setAttributes(attributes);
            }
        }
        return attributeSet;
    }

    private String getAttributeSetNewName(String attributeSetName, Long companyId)
    {
        String hql = "select name from AttributeSet where companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(attributeSetName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (attributeSetName.contains("_import_"))
                {
                    returnStr = attributeSetName.substring(0, attributeSetName.lastIndexOf('_'))
                            + "_" + num;
                }
                else
                {
                    returnStr = attributeSetName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return attributeSetName;
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

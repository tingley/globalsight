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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FileCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

/**
 * Imports attribute info to system.
 */
public class AttributeImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(AttributeImporter.class);
    private String sessionId;
    private long companyId;

    public AttributeImporter(String sessionId, String companyId, String importToCompId)
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
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("Attribute"))
                {
                    Attribute attribute = putDataIntoAttribute(valueMap);
                    attributeList.add(attribute);
                }
            }
        }

        if (attributeList.size() > 0)
            dataMap.put("Attribute", attributeList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("Attribute"))
            {
                storeAttributeData(dataMap);
            }
            addMessage("<b> Done importing Attributes.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Attributes.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeAttributeData(Map<String, List> dataMap)
    {
        List<Attribute> attributeList = dataMap.get("Attribute");
        try
        {
            for (int i = 0; i < attributeList.size(); i++)
            {
                Attribute attribute = attributeList.get(i);
                String oldName = attribute.getName();
                String displayName = attribute.getDisplayName();
                String newName = getAttributeNewName(oldName, companyId);
                attribute.setName(newName);
                if (newName.contains("_import_"))
                {
                    String suffix = newName.substring(newName.indexOf("_import_"));
                    if (displayName.contains("_import_"))
                    {
                        displayName = displayName.substring(0, displayName.lastIndexOf('_'))
                                + suffix;
                    }
                    else
                    {
                        displayName = displayName + suffix;
                    }
                }
                attribute.setDisplayName(displayName);
                HibernateUtil.save(attribute);
                if (oldName.equals(newName))
                {
                    addMessage("Attribute name <b>" + newName + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Attribute name <b>" + oldName + "</b> already exists. <b>"
                            + newName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Attribute data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    // private Attribute createNewAttribute(String newName, Attribute attribute)
    // {
    // try
    // {
    // attribute.setName(newName);
    // attribute.setDisplayName(newName);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // return attribute;
    // }

    private Attribute putDataIntoAttribute(Map<String, String> valueMap) throws ParseException
    {
        String conditionType = null;
        String textLength = null;
        String intMax = null;
        String intMin = null;
        String floatMax = null;
        String floatMin = null;
        String floatDefinition = null;
        String listOptions = null;
        Boolean listMultiple = false;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        dateFormat.setLenient(false);
        Attribute attribute = new Attribute();
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
                attribute.setName(valueField);
            }
            else if ("DISPLAY_NAME".equalsIgnoreCase(keyField))
            {
                attribute.setDisplayName(valueField);;
            }
            else if ("DESCRIPTION".equalsIgnoreCase(keyField))
            {
                if (StringUtil.isNotEmptyAndNull(valueField))
                {
                    attribute.setDescription(valueField);
                }
                else
                {
                    attribute.setDescription(null);
                }
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                attribute.setCompanyId(companyId);
            }
            else if ("VISIBLE".equalsIgnoreCase(keyField))
            {
                attribute.setVisible(Boolean.parseBoolean(valueField));
            }
            else if ("EDITABLE".equalsIgnoreCase(keyField))
            {
                attribute.setEditable(Boolean.parseBoolean(valueField));
            }
            else if ("REQUIRED".equalsIgnoreCase(keyField))
            {
                attribute.setRequired(Boolean.parseBoolean(valueField));
            }
            else if ("TYPE".equalsIgnoreCase(keyField))
            {
                conditionType = valueField;
            }
            else if ("TEXTLENGTH".equalsIgnoreCase(keyField))
            {
                textLength = valueField;
            }
            else if ("INTMAX".equalsIgnoreCase(keyField))
            {
                intMax = valueField;
            }
            else if ("INTMIN".equalsIgnoreCase(keyField))
            {
                intMin = valueField;
            }
            else if ("FLOATMAX".equalsIgnoreCase(keyField))
            {
                floatMax = valueField;
            }
            else if ("FLOATMIN".equalsIgnoreCase(keyField))
            {
                floatMin = valueField;
            }
            else if ("FLOATDEFINITION".equalsIgnoreCase(keyField))
            {
                floatDefinition = valueField;
            }
            else if ("LISTOPTIONS".equalsIgnoreCase(keyField))
            {
                listOptions = valueField;
            }
            else if ("LISTMULTIPLE".equalsIgnoreCase(keyField))
            {
                listMultiple = Boolean.parseBoolean(valueField);
            }
        }
        if (Attribute.TYPE_TEXT.equals(conditionType))
        {
            TextCondition textCondition = new TextCondition();
            attribute.setCondition(textCondition);
            if (StringUtil.isNotEmptyAndNull(textLength))
            {
                textCondition.setLength(Integer.parseInt(textLength));
            }
            else
            {
                textCondition.setLength(null);
            }
        }
        else if (Attribute.TYPE_INTEGER.equals(conditionType))
        {
            IntCondition intCondition = new IntCondition();
            attribute.setCondition(intCondition);

            if (StringUtil.isNotEmptyAndNull(intMax))
            {
                intCondition.setMax(Integer.valueOf(intMax));
            }
            else
            {
                intCondition.setMax(null);
            }

            if (StringUtil.isNotEmptyAndNull(intMin))
            {
                intCondition.setMin(Integer.valueOf(intMin));
            }
            else
            {
                intCondition.setMin(null);
            }
        }
        else if (Attribute.TYPE_FLOAT.equals(conditionType))
        {
            FloatCondition floatCondition = new FloatCondition();
            attribute.setCondition(floatCondition);

            if (StringUtil.isNotEmptyAndNull(floatMax))
            {
                floatCondition.setMax(Float.valueOf(floatMax));
            }
            else
            {
                floatCondition.setMax(null);
            }

            if (StringUtil.isNotEmptyAndNull(floatMin))
            {
                floatCondition.setMin(Float.valueOf(floatMin));
            }
            else
            {
                floatCondition.setMin(null);
            }

            if (StringUtil.isNotEmptyAndNull(floatDefinition))
            {
                floatCondition.setDefinition(Integer.valueOf(floatDefinition));
            }
            else
            {
                floatCondition.setDefinition(null);
            }
        }
        else if (Attribute.TYPE_FILE.equals(conditionType))
        {
            FileCondition fileCondition = new FileCondition();
            attribute.setCondition(fileCondition);
        }
        else if (Attribute.TYPE_DATE.equals(conditionType))
        {
            DateCondition dateCondition = new DateCondition();
            attribute.setCondition(dateCondition);
        }
        else if (Attribute.TYPE_CHOICE_LIST.equals(conditionType))
        {
            ListCondition listCondition = new ListCondition();
            attribute.setCondition(listCondition);

            String[] optionList = listOptions.split(",");

            for (int i = 0; i < optionList.length; i++)
            {
                listCondition.addOption(optionList[i]);
            }

            listCondition.setMultiple(listMultiple);

        }
        return attribute;
    }

    private String getAttributeNewName(String attributeName, Long companyId)
    {
        String hql = "select att.name from Attribute att where att.companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(attributeName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (attributeName.contains("_import_"))
                {
                    returnStr = attributeName.substring(0, attributeName.lastIndexOf('_')) + "_"
                            + num;
                }
                else
                {
                    returnStr = attributeName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return attributeName;
        }
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? ""
                : config_error_map.get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? ""
                : config_error_map.get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }
}

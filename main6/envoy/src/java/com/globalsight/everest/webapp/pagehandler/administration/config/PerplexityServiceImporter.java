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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

/**
 * Imports perplexity service info to system.
 */
public class PerplexityServiceImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(PerplexityServiceImporter.class);
    private String sessionId;
    private long companyId;

    public PerplexityServiceImporter(String sessionId, String currentCompanyId,
            String importToCompId)
    {
        this.sessionId = sessionId;
        if (importToCompId != null && !importToCompId.equals("-1"))
        {
            companyId = Long.parseLong(importToCompId);
        }
        else
        {
            companyId = Long.parseLong(currentCompanyId);
        }
    }

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
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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
        List<PerplexityService> psList = new ArrayList<PerplexityService>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("PerplexityService"))
                {
                    PerplexityService ps = putDataIntoPS(valueMap);
                    psList.add(ps);
                }
            }
        }

        if (psList.size() > 0)
            dataMap.put("PerplexityService", psList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("PerplexityService"))
            {
                storePSData(dataMap);
            }

            addMessage("<b> Done importing Perplexity Service.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Perplexity Service.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storePSData(Map<String, List> dataMap)
    {
        List<PerplexityService> psList = dataMap.get("PerplexityService");
        PerplexityService ps = null;
        try
        {
            for (int i = 0; i < psList.size(); i++)
            {
                ps = psList.get(i);
                String oldName = ps.getName();
                String newName = getPSNewName(oldName, ps.getCompanyId());
                ps.setName(newName);
                HibernateUtil.save(ps);
                if (oldName.equals(newName))
                {
                    addMessage("<b>" + newName + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Perplexity Service name <b>" + oldName + "</b> already exists. <b>"
                            + newName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Perplexity Service data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private PerplexityService putDataIntoPS(Map<String, String> valueMap) throws ParseException
    {
        PerplexityService ps = new PerplexityService();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if (keyField.equalsIgnoreCase("NAME"))
            {
                ps.setName(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                ps.setCompanyId(companyId);
            }
            else if (keyField.equalsIgnoreCase("USER_NAME"))
            {
                ps.setUserName(valueField);
            }
            else if (keyField.equalsIgnoreCase("PASSWORD"))
            {
                ps.setPassword(valueField);
            }
            else if (keyField.equalsIgnoreCase("DESCRIPTION"))
            {
                if (StringUtil.isNotEmptyAndNull(valueField))
                {
                    ps.setDescription(valueField);
                }
                else
                {
                    ps.setDescription(null);
                }
            }
            else if (keyField.equalsIgnoreCase("URL"))
            {
                ps.setUrl(valueField);
            }
        }
        return ps;
    }

    private String getPSNewName(String filterName, Long companyId)
    {
        String hql = "select ps.name from PerplexityService ps where ps.companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(filterName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (filterName.contains("_import_"))
                {
                    returnStr = filterName.substring(0, filterName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = filterName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return filterName;
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

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

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.java.Termbase;

/**
 * Imports Terminology info to system.
 */
public class TermbaseImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(TermbaseImporter.class);
    private String sessionId;
    private Long companyId;

    public TermbaseImporter(String sessionId, String companyId, String importToCompId)
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
        List<Termbase> termbaseList = new ArrayList<Termbase>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("Termbase"))
                {
                    Termbase termbase = putDataIntoTermbase(valueMap);
                    termbaseList.add(termbase);
                }
            }
        }

        if (termbaseList.size() > 0)
            dataMap.put("Termbase", termbaseList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("Termbase"))
            {
                storeTermbaseData(dataMap);
            }
            addMessage("<b> Done importing Terminology.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Terminology.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeTermbaseData(Map<String, List> dataMap)
    {
        List<Termbase> termbaseList = dataMap.get("Termbase");
        try
        {
            for (int i = 0; i < termbaseList.size(); i++)
            {
                Termbase termbase = termbaseList.get(i);
                String oldName = termbase.getName();
                String newName = getTermbaseNewName(oldName, termbase.getCompany().getId());
                termbase.setName(newName);
                HibernateUtil.save(termbase);
                if (oldName.equals(newName))
                {
                    addMessage("<b>" + newName + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Terminology name <b>" + oldName + "</b> already exists. <b>"
                            + newName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Terminology data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private Termbase putDataIntoTermbase(Map<String, String> valueMap) throws ParseException
    {
        Termbase termbase = new Termbase();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if ("TB_NAME".equalsIgnoreCase(keyField))
            {
                termbase.setName(valueField);
            }
            else if ("TB_DESCRIPTION".equalsIgnoreCase(keyField))
            {
                termbase.setDescription(valueField);
            }
            else if ("TB_DEFINITION".equalsIgnoreCase(keyField))
            {
                termbase.setDefination(valueField);
            }
            else if ("COMPANYID".equalsIgnoreCase(keyField))
            {
                termbase.setCompany(HibernateUtil.get(Company.class, companyId));
            }
        }
        return termbase;
    }

    private String getTermbaseNewName(String termbaseName, Long companyId)
    {
        String hql = "select tb.name from Termbase tb where tb.company=:company";
        Map map = new HashMap();
        map.put("company", CompanyWrapper.getCompanyById(companyId));
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(termbaseName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (termbaseName.contains("_import_"))
                {
                    returnStr = termbaseName.substring(0, termbaseName.lastIndexOf('_')) + "_"
                            + num;
                }
                else
                {
                    returnStr = termbaseName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return termbaseName;
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

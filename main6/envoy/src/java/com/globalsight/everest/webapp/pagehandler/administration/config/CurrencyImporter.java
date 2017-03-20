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

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.IsoCurrency;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports currency info to system.
 */
public class CurrencyImporter implements ConfigConstants
{

    private static final Logger logger = Logger.getLogger(CurrencyImporter.class);
    private String sessionId;
    private Long companyId;

    public CurrencyImporter(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.sessionId = sessionId;
        if (importToCompId != null && !importToCompId.equals("-1"))
        {
            this.companyId = Long.parseLong(importToCompId);
        }
        else
        {
            this.companyId = Long.parseLong(currentCompanyId);
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
        List<Currency> currencyList = new ArrayList<Currency>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("Currency"))
                {
                    Currency currency = putDataIntoCurrency(valueMap);
                    currencyList.add(currency);
                }
            }
        }

        if (currencyList.size() > 0)
            dataMap.put("Currency", currencyList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("Currency"))
            {
                storeCurrencyData(dataMap);
            }
            addMessage("<b> Done importing Currency.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Currency.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeCurrencyData(Map<String, List> dataMap)
    {
        List<Currency> currencyList = dataMap.get("Currency");
        try
        {
            for (int i = 0; i < currencyList.size(); i++)
            {
                Currency currency = currencyList.get(i);
                Currency curr = ServerProxy.getCostingEngine().getCurrency(currency.getIsoCode(),
                        companyId);
                if (curr == null)
                {
                    HibernateUtil.save(currency);
                    addMessage("<b>" + currency.getIsoCurrency().getDisplayName()
                            + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Currency name <b>" + currency.getIsoCurrency().getDisplayName()
                            + "</b> already exists.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Currency data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private Currency putDataIntoCurrency(Map<String, String> valueMap) throws ParseException
    {
        Currency currency = new Currency();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if ("CURRENCY_ID".equalsIgnoreCase(keyField))
            {
                IsoCurrency isoCurrency = HibernateUtil.get(IsoCurrency.class,
                        Long.parseLong(valueField));
                currency.setIsoCurrency(isoCurrency);
            }
            else if ("CONVERSION_FACTOR".equalsIgnoreCase(keyField))
            {
                currency.setConversionFactor(Float.parseFloat(valueField));
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                currency.setCompanyId(companyId);
            }
            else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
            {
                currency.setIsActive(Boolean.parseBoolean(valueField));
            }
        }
        return currency;
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

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
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports rate info to system.
 */
public class RatesImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(RatesImporter.class);
    private String sessionId;
    private long companyId;

    public RatesImporter(String sessionId, String companyId, String importToCompId)
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
        List<Rate> rateList = new ArrayList<Rate>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("Rate"))
                {
                    Rate rate = putDataIntoRate(valueMap);
                    rateList.add(rate);
                }
            }
        }

        if (rateList.size() > 0)
            dataMap.put("Rate", rateList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("Rate"))
            {
                storeRateData(dataMap);
            }
            addMessage("<b> Done importing Rates.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Rates.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeRateData(Map<String, List> dataMap)
    {
        List<Rate> rateList = dataMap.get("Rate");
        try
        {
            for (int i = 0; i < rateList.size(); i++)
            {
                Rate rate = rateList.get(i);
                // checks activity type
                Activity activity = rate.getActivity();
                // checks locale pair
                LocalePair localePair = rate.getLocalePair();
                // checks currency
                Currency currency = rate.getCurrency();

                if (activity != null && localePair != null && currency != null)
                {
                    String oldName = rate.getName();
                    String newName = getRateNewName(oldName, companyId);
                    rate.setName(newName);
                    HibernateUtil.save(rate);
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Rate name <b>" + oldName + "</b> already exists. <b>" + newName
                                + "</b> imported successfully.");
                    }
                }
                else
                {
                    String msg = "Upload Rate data failed !";
                    logger.warn(msg);
                    addToError(msg);
                }

            }
        }
        catch (Exception e)
        {
            String msg = "Upload Rate data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private Rate putDataIntoRate(Map<String, String> valueMap) throws ParseException
    {
        Rate rate = new Rate();
        try
        {
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
                    rate.setName(valueField);
                }
                else if ("CURRENCY_CONV_ISO_CODE".equalsIgnoreCase(keyField))
                {
                    Currency currency = ServerProxy.getCostingEngine().getCurrency(valueField,
                            companyId);
                    rate.setCurrency(currency);
                }
                else if ("TYPE".equalsIgnoreCase(keyField))
                {
                    rate.setType(valueField);
                }
                else if ("ACTIVITY_NAME".equalsIgnoreCase(keyField))
                {
                    Activity activity = ServerProxy.getJobHandler()
                            .getActivityByDisplayNameCompanyId(valueField,
                                    String.valueOf(companyId));
                    rate.setActivity(activity);
                }
                else if ("LOCALE_PAIR_NAME".equalsIgnoreCase(keyField))
                {
                    String[] locales = valueField.split("->");
                    LocalePair localePair = ServerProxy.getLocaleManager()
                            .getLocalePairBySourceTargetAndCompanyStrings(locales[0], locales[1],
                                    companyId);
                    rate.setLocalePair(localePair);
                }
                else if ("EXACT_CONTEXT_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setContextMatchRate(Float.parseFloat(valueField));
                }
                else if ("EXACT_SEGMENT_TM_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setSegmentTmRate(Float.parseFloat(valueField));
                }
                else if ("FUZZY_LOW_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setLowFuzzyMatchRate(Float.parseFloat(valueField));
                }
                else if ("FUZZY_MED_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setMedFuzzyMatchRate(Float.parseFloat(valueField));
                }
                else if ("FUZZY_MED_HI_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setMedHiFuzzyMatchRate(Float.parseFloat(valueField));
                }
                else if ("FUZZY_HI_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setHiFuzzyMatchRate(Float.parseFloat(valueField));
                }
                else if ("EXACT_CONTEXT_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setContextMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("EXACT_SEGMENT_TM_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setSegmentTmRatePer(Float.parseFloat(valueField));
                }
                else if ("FUZZY_LOW_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setLowFuzzyMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("FUZZY_MED_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setMedFuzzyMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("FUZZY_MED_HI_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setMedHiFuzzyMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("FUZZY_HI_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setHiFuzzyMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("NO_MATCH_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setNoMatchRate(Float.parseFloat(valueField));
                }
                else if ("REPETITION_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setRepetitionRate(Float.parseFloat(valueField));
                }
                else if ("IN_CONTEXT_MATCH_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setInContextMatchRate(Float.parseFloat(valueField));
                }
                else if ("IN_CONTEXT_MATCH_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setInContextMatchRatePer(Float.parseFloat(valueField));
                }
                else if ("REPETITION_RATE_PER".equalsIgnoreCase(keyField))
                {
                    rate.setRepetitionRatePer(Float.parseFloat(valueField));
                }
                else if ("UNIT_RATE".equalsIgnoreCase(keyField))
                {
                    rate.setUnitRate(Float.parseFloat(valueField));
                }
                else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
                {
                    rate.setIsActive(Boolean.parseBoolean(valueField));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return rate;
    }

    private String getRateNewName(String rateName, long companyId)
    {
        String hql = "select r.name from Rate r where r.activity.companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(rateName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (rateName.contains("_import_"))
                {
                    returnStr = rateName.substring(0, rateName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = rateName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return rateName;
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

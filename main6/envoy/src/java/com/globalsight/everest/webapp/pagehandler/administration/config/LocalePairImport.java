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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;

/**
 * 
 * imports property file locale pair info
 */
public class LocalePairImport extends MultiCompanySupportedThread implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(LocalePairImport.class);
    private Map<String, String> localePairMap = new HashMap<String, String>();
    private Map<String, GlobalSightLocale> localeMap = new HashMap<String, GlobalSightLocale>();
    private File uploadedFile;
    private String companyId;
    private String sessionId;

    public LocalePairImport(String sessionId, File uploadedFile, String companyId)
    {
        this.sessionId = sessionId;
        this.uploadedFile = uploadedFile;
        this.companyId = companyId;
    }

    public void run()
    {
        CompanyThreadLocal.getInstance().setIdValue(this.companyId);
        this.analysisAndImport(uploadedFile);
    }

    /**
     *  analysis and imports upload file
     * @param uploadedFile
     */
    private void analysisAndImport(File uploadedFile)
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
            Properties prop = new Properties();
            prop.load(is);
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
            e.printStackTrace();
        }
    }

    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<LocalePair> localePairList = new ArrayList<LocalePair>();
        List<GlobalSightLocale> globalSightLocaleList = new ArrayList<GlobalSightLocale>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("LocalPairs"))
                {
                    LocalePair localePair = putDataIntoLocalePair(valueMap);
                    localePairList.add(localePair);
                }
                else if (keyArr[0].equalsIgnoreCase("Locale"))
                {
                    GlobalSightLocale globalSightLocale = putDataIntoLocale(valueMap);
                    globalSightLocaleList.add(globalSightLocale);
                }
            }
        }

        if (localePairList.size() > 0)
            dataMap.put("LocalPairs", localePairList);

        if (globalSightLocaleList.size() > 0)
            dataMap.put("Locale", globalSightLocaleList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        int i = 0;
        int size = dataMap.keySet().size();

        try
        {
            if (dataMap.containsKey("Locale"))
            {
                i++;
                // stores locale info
                storeGlobalSightLocaleData(dataMap);
                Thread.sleep(100);
            }

            if (dataMap.containsKey("LocalPairs"))
            {
                i++;
                // stores locale pair info
                storeLocalePairData(dataMap);
                Thread.sleep(100);
            }

            addMessage("<b>Locale Pairs Imported successfully !</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Locale Pairs.", e);
            addToError(e.getMessage());
        }
    }

    private void storeGlobalSightLocaleData(Map<String, List> dataMap)
    {
        List<GlobalSightLocale> globalSightLocaleList = dataMap.get("Locale");
        LocaleManagerWLRemote localeMangerLocal = ServerProxy.getLocaleManager();

        GlobalSightLocale locale = null;
        try
        {
            for (int i = 0; i < globalSightLocaleList.size(); i++)
            {
                locale = globalSightLocaleList.get(i);
                long oldId = locale.getId();
                GlobalSightLocale gslInDb = localeMangerLocal.addLocale(locale);
                localeMap.put(String.valueOf(oldId), gslInDb);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when upload GlobalSight Locale data", e);
            addToError("Upload GlobalSight Locale data failed!");
        }
    }

    private void storeLocalePairData(Map<String, List> dataMap)
    {
        LocalePair localePair = null;
        List<LocalePair> localePairList = dataMap.get("LocalPairs");
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        try
        {
            for (int i = 0; i < localePairList.size(); i++)
            {
                localePair = localePairList.get(i);
                if (localePairMap.containsKey(String.valueOf(localePair.getId())))
                {
                    String value = localePairMap.get(String.valueOf(localePair.getId()));
                    String[] valueArr = value.split("\\|");
                    if (valueArr[0].endsWith(".source"))
                    {
                        String sourceId = valueArr[0].substring(0, valueArr[0].indexOf("."));
                        String targetId = valueArr[1].substring(0, valueArr[1].indexOf("."));
                        GlobalSightLocale source = localeMap.get(sourceId);
                        GlobalSightLocale target = localeMap.get(targetId);

                        if (source != null)
                            localePair.setSource(source);
                        if (target != null)
                            localePair.setTarget(target);
                    }
                    else if (valueArr[0].endsWith(".target"))
                    {
                        String targetId = valueArr[0].substring(0, valueArr[0].indexOf("."));
                        String sourceId = valueArr[1].substring(0, valueArr[1].indexOf("."));
                        GlobalSightLocale source = localeMap.get(sourceId);
                        GlobalSightLocale target = localeMap.get(targetId);

                        if (source != null)
                            localePair.setSource(source);
                        if (target != null)
                            localePair.setTarget(target);
                    }
                }

                localeMgr.addSourceTargetLocalePair(localePair.getSource(), localePair.getTarget(),
                        localePair.getCompanyId());
            }
        }
        catch (Exception e)
        {
            String msg = "Upload LocalePair data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private LocalePair putDataIntoLocalePair(Map<String, String> valueMap)
    {
        LocalePair localePair = new LocalePair();
        String key = null;
        StringBuffer value = new StringBuffer();;
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if (keyField.equalsIgnoreCase("ID"))
            {
                key = valueField;
                localePair.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("SOURCE_LOCALE_ID"))
            {
                value.append(valueField).append(".source").append("|");
            }
            else if (keyField.equalsIgnoreCase("TARGET_LOCALE_ID"))
            {
                value.append(valueField).append(".target").append("|");
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                localePair.setCompanyId(Long.parseLong(companyId));
            }
            else if (keyField.equalsIgnoreCase("IS_ACTIVE"))
            {
                localePair.setIsActive(Boolean.parseBoolean(valueField));
            }
        }
        if (value.toString().endsWith("|"))
        {
            String valueStr = value.toString().substring(0, value.toString().lastIndexOf("|"));
            localePairMap.put(key, valueStr);
        }

        return localePair;
    }

    private GlobalSightLocale putDataIntoLocale(Map<String, String> valueMap)
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        String keyLocale = null;
        String valueLocale = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyLocale = (String) itor.next();
            valueLocale = valueMap.get(keyLocale);
            if (keyLocale.equalsIgnoreCase("ID"))
            {
                locale.setId(Long.parseLong(valueLocale));
            }
            else if (keyLocale.equalsIgnoreCase("ISO_LANG_CODE"))
            {
                locale.setLanguage(valueLocale);
            }
            else if (keyLocale.equalsIgnoreCase("ISO_COUNTRY_CODE"))
            {
                locale.setCountry(valueLocale);
            }
            else if (keyLocale.equalsIgnoreCase("IS_UI_LOCALE"))
            {
                locale.setIsUiLocale(Boolean.parseBoolean(valueLocale));
            }
        }

        return locale;
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:red'>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:blue'>" + msg);
    }
}

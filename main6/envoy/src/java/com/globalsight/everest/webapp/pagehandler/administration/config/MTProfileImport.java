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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.EngineEnum;
import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports mt profile info to system.
 */
public class MTProfileImport implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(MTProfileImport.class);
    private Map<Long, MachineTranslationProfile> mtpMap = new HashMap<Long, MachineTranslationProfile>();
    private Map<Long, Long> mtpExtentInfoMap = new HashMap<Long, Long>();
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;
    private EngineEnum[] engines = null;

    public MTProfileImport(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = currentCompanyId;
        this.importToCompId = importToCompId;
        engines = EngineEnum.values();
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
        List<MachineTranslationProfile> mtpList = new ArrayList<MachineTranslationProfile>();
        List<MachineTranslationExtentInfo> extenInfoList = new ArrayList<MachineTranslationExtentInfo>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("MachineTranslationProfile"))
                {
                    MachineTranslationProfile mtp = putDataIntoMTP(valueMap);
                    if (mtp.getMtEngine() == null || "".equals(mtp.getMtEngine()))
                    {
                        addToError("<b >Profile name is "
                                + mtp.getMtProfileName()
                                + " imported failed because mt engine was modified type not supported!</b>");
                    }
                    if (mtp.getMtEngine() != null && !"".equals(mtp.getMtEngine()))
                    {
                        mtpList.add(mtp);
                    }
                }
                if (keyArr[0].equalsIgnoreCase("MachineTranslationExtentInfo"))
                {
                    MachineTranslationExtentInfo globalSightLocale = putDataIntoMTPExtenInfo(valueMap);
                    extenInfoList.add(globalSightLocale);
                }
            }
        }

        if (mtpList.size() > 0)
            dataMap.put("MachineTranslationProfile", mtpList);

        if (extenInfoList.size() > 0)
            dataMap.put("MachineTranslationExtentInfo", extenInfoList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("MachineTranslationProfile"))
            {
                storeMTPData(dataMap);
            }

            if (dataMap.containsKey("MachineTranslationExtentInfo"))
            {
                storeMTPExtentInfoData(dataMap);
            }

            addMessage("<b> Done importing Machine Translation Profiles.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Machine Translation Profiles.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeMTPData(Map<String, List> dataMap)
    {
        List<MachineTranslationProfile> mtpList = dataMap.get("MachineTranslationProfile");
        MachineTranslationProfile mtp = null;
        try
        {
            for (int i = 0; i < mtpList.size(); i++)
            {
                mtp = mtpList.get(i);
                long oldId = mtp.getId();
                String oldName = mtp.getMtProfileName();
                String newName = getMTPNewName(oldName, mtp.getCompanyid());
                mtp.setMtProfileName(newName);
                HibernateUtil.save(mtp);
                long newId = selectNewId(newName, mtp.getCompanyid());
                mtp = MTProfileHandlerHelper.getMTProfileById(String
                        .valueOf(newId));
                mtpMap.put(oldId, mtp);
                if (oldName.equals(newName))
                {
                    addMessage("<b>" + mtp.getMtProfileName() + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" MT Profile name <b>" + oldName + "</b> already exists. <b>"
                            + mtp.getMtProfileName() + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MachineTranslationProfile data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeMTPExtentInfoData(Map<String, List> dataMap)
    {
        MachineTranslationExtentInfo extenInfo = null;
        List<MachineTranslationExtentInfo> extenInfoList = dataMap
                .get("MachineTranslationExtentInfo");
        try
        {
            for (int i = 0; i < extenInfoList.size(); i++)
            {
                extenInfo = extenInfoList.get(i);
                long id = extenInfo.getId();
                if (mtpExtentInfoMap.containsKey(id))
                {
                    long value = mtpExtentInfoMap.get(id);
                    if (mtpMap.containsKey(value))
                    {
                        MachineTranslationProfile mtp = mtpMap.get(value);
                        extenInfo.setMtProfile(mtp);
                    }
                }
                HibernateUtil.save(extenInfo);
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MachineTranslationExtentInfo data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private MachineTranslationProfile putDataIntoMTP(Map<String, String> valueMap)
            throws ParseException
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        dateFormat.setLenient(false);
        MachineTranslationProfile mtp = new MachineTranslationProfile();
        mtp.setMtEngine("");
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
                mtp.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("MT_PROFILE_NAME"))
            {
                mtp.setMtProfileName(valueField);
            }
            else if (keyField.equalsIgnoreCase("MT_ENGINE"))
            {
                for (int i = 0; i < engines.length; i++)
                {
                    String _engine = engines[i].name();
                    if (_engine.equalsIgnoreCase(valueField))
                    {
                        mtp.setMtEngine(valueField);
                    }
                }
            }
            else if (keyField.equalsIgnoreCase("DESCRIPTION"))
            {
                mtp.setDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("MT_THRESHOLD"))
            {
                mtp.setMtThreshold(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("URL"))
            {
                mtp.setUrl(valueField);
            }
            else if (keyField.equalsIgnoreCase("PORT"))
            {
                mtp.setPort(Integer.parseInt(valueField));
            }
            else if (keyField.equalsIgnoreCase("USERNAME"))
            {
                mtp.setUsername(valueField);
            }
            else if (keyField.equalsIgnoreCase("PASSWORD"))
            {
                mtp.setPassword(valueField);
            }
            else if (keyField.equalsIgnoreCase("CATEGORY"))
            {
                mtp.setCategory(valueField);
            }
            else if (keyField.equalsIgnoreCase("ACCOUNTINFO"))
            {
                mtp.setAccountinfo(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    mtp.setCompanyid(Long.parseLong(importToCompId));
                }
                else
                {
                    mtp.setCompanyid(Long.parseLong(currentCompanyId));
                }
            }
            else if (keyField.equalsIgnoreCase("TIMESTAMP"))
            {
                Date timeDate = dateFormat.parse(valueField);
                Timestamp dateTime = new Timestamp(timeDate.getTime());
                mtp.setTimestamp(dateTime);
            }
            else if (keyField.equalsIgnoreCase("INCLUDE_MT_IDENTIFIERS"))
            {
                mtp.setIncludeMTIdentifiers(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IGNORE_TM_MATCHES"))
            {
                mtp.setIgnoreTMMatch(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("LOG_DEBUG_INFO"))
            {
                mtp.setLogDebugInfo(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("MS_TRANS_TYPE"))
            {
                mtp.setMsTransType(valueField);
            }
            else if (keyField.equalsIgnoreCase("MS_MAX_LENGTH"))
            {
                mtp.setMsMaxLength(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("MT_IDENTIFIER_LEADING"))
            {
                mtp.setMtIdentifierLeading(valueField);
            }
            else if (keyField.equalsIgnoreCase("MT_IDENTIFIER_TRAILING"))
            {
                mtp.setMtIdentifierTrailing(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_ACTIVE"))
            {
                mtp.setActive(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("EXTENT_JSON_INFO"))
            {
                if (valueField == null || "".equals(valueField))
                {
                    mtp.setJsonInfo(null);
                }
                else
                {
                    mtp.setJsonInfo(valueField);
                }
            }
        }
        return mtp;
    }

    private MachineTranslationExtentInfo putDataIntoMTPExtenInfo(Map<String, String> valueMap)
    {
        MachineTranslationExtentInfo extenInfo = new MachineTranslationExtentInfo();
        Long mtProfileId = null;
        String key = null;
        String value = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            key = (String) itor.next();
            value = valueMap.get(key);
            if (key.equalsIgnoreCase("ID"))
            {
                extenInfo.setId(Long.parseLong(value));
            }
            else if (key.equalsIgnoreCase("MT_PROFILE_ID"))
            {
                mtProfileId = Long.parseLong(value);
            }
            else if (key.equalsIgnoreCase("LANGUAGE_PAIR_CODE"))
            {
                extenInfo.setLanguagePairCode(Long.parseLong(value));
            }
            else if (key.equalsIgnoreCase("LANGUAGE_PAIR_NAME"))
            {
                extenInfo.setLanguagePairName(value);
            }
            else if (key.equalsIgnoreCase("DOMAIN_CODE"))
            {
                extenInfo.setDomainCode(value);
            }
        }
        mtpExtentInfoMap.put(extenInfo.getId(), mtProfileId);
        return extenInfo;
    }

    private String getMTPNewName(String filterName, Long companyId)
    {
        String hql = "select mtp.mtProfileName from MachineTranslationProfile "
                + "  mtp where mtp.companyid=:companyid";
        Map map = new HashMap();
        map.put("companyid", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(filterName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (filterName.contains("_import_"))
                {
                    returnStr = filterName.substring(0,
                            filterName.lastIndexOf('_'))
                            + "_" + num;
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

    private Long selectNewId(String mtProfileName, Long companyId)
    {
        Map map = new HashMap();

        String hql = "select mtp.id from MachineTranslationProfile "
                + "  mtp where mtp.companyid=:companyid and  mtp.mtProfileName=:mtProfileName ";

        map.put("companyid", companyId);
        map.put("mtProfileName", mtProfileName);

        Long id = (Long) HibernateUtil.getFirst(hql, map);

        return id;
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

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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

/**
 * Imports property file translation memories (configuration only - not
 * content).
 */
public class TMImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(TMImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;
    private String userId;

    public TMImporter(String sessionId, String userId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = companyId;
        this.importToCompId = importToCompId;
        this.userId = userId;
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
        List<ProjectTM> projectTMList = new ArrayList<ProjectTM>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("TranslationMemory"))
                {
                    ProjectTM projectTM = putDataIntoProjectTM(valueMap);
                    projectTMList.add(projectTM);
                }
            }
        }

        if (projectTMList.size() > 0)
            dataMap.put("TranslationMemory", projectTMList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("TranslationMemory"))
            {
                storeProjectTMData(dataMap);
            }
            addMessage("<b> Done importing Translation Memorys.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Translation Memorys.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeProjectTMData(Map<String, List> dataMap)
    {
        List<ProjectTM> projectTMList = dataMap.get("TranslationMemory");
        try
        {
            for (int i = 0; i < projectTMList.size(); i++)
            {
                ProjectTM projectTM = projectTMList.get(i);
                String oldName = projectTM.getName();
                String newName = getTMNewName(oldName, projectTM.getCompanyId());
                projectTM = createNewProjectTM(newName, projectTM);
                HibernateUtil.save(projectTM);
                if (oldName.equals(newName))
                {
                    addMessage("<b>" + newName + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Translation Memory name <b>" + oldName
                            + "</b> already exists. <b>" + newName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload TranslationMemory data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private ProjectTM createNewProjectTM(String newName, ProjectTM projectTM)
    {
        try
        {
            projectTM.setName(newName);
            projectTM.setCreationUser(userId);
            projectTM.setCreationDate(new Date());

            // saves tmAttribute
            Set<TMAttribute> attributes = new HashSet<TMAttribute>();
            List<TMAttribute> tmAttributeList = projectTM.getAllTMAttributes();

            for (TMAttribute tmAttribute : tmAttributeList)
            {
                List<Attribute> attributeList = (List<Attribute>) AttributeManager
                        .getAllAttributes(projectTM.getCompanyId());
                for (Attribute attribute : attributeList)
                {
                    String attributeName = attribute.getName();
                    if (attributeName.equals(tmAttribute.getAttributename())
                            || attributeName
                                    .startsWith(tmAttribute.getAttributename() + "_import_"))
                    {
                        TMAttribute tma = new TMAttribute();
                        tma.setAttributename(attributeName);
                        tma.setSettype(tmAttribute.getSettype());
                        tma.setTm(projectTM);
                        attributes.add(tma);
                        break;
                    }
                }
            }
            projectTM.setAttributes(attributes);

            // saves tm3 info
            Company company = ServerProxy.getJobHandler().getCompanyById(projectTM.getCompanyId());
            if (company.getTmVersion().equals(TmVersion.TM3))
            {
                // We need to create the tm3 storage. Use the shared TM pool for
                // this company.
                TM3Manager mgr = DefaultManager.create();
                TM3Tm<GSTuvData> tm3tm = mgr.createMultilingualSharedTm(new GSDataFactory(),
                        SegmentTmAttribute.inlineAttributes(), company.getId());
                projectTM.setTm3Id(tm3tm.getId());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return projectTM;
    }

    private ProjectTM putDataIntoProjectTM(Map<String, String> valueMap) throws ParseException
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        dateFormat.setLenient(false);
        ProjectTM projectTM = new ProjectTM();
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
                projectTM.setName(valueField);
            }
            else if ("DOMAIN".equalsIgnoreCase(keyField))
            {
                projectTM.setDomain(valueField);;
            }
            else if ("ORGANIZATION".equalsIgnoreCase(keyField))
            {
                projectTM.setOrganization(valueField);
            }
            else if ("DESCRIPTION".equalsIgnoreCase(keyField))
            {
                projectTM.setDescription(valueField);
            }
            else if ("INDEX_TARGET".equalsIgnoreCase(keyField))
            {
                projectTM.setIndexTarget(Boolean.parseBoolean(valueField));
            }
            else if ("TM3_ID".equalsIgnoreCase(keyField))
            {
                projectTM.setTm3Id(Long.parseLong(valueField));
            }
            else if ("CREATION_DATE".equalsIgnoreCase(keyField))
            {
                Date timeDate = dateFormat.parse(valueField);
                projectTM.setCreationDate(timeDate);
            }
            else if ("CREATION_USER".equalsIgnoreCase(keyField))
            {
                projectTM.setCreationUser(valueField);
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    projectTM.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    projectTM.setCompanyId(Long.parseLong(currentCompanyId));
                }
            }
            else if ("IS_REMOTE_TM".equalsIgnoreCase(keyField))
            {
                projectTM.setIsRemoteTm(Boolean.parseBoolean(valueField));
            }
            else if ("REMOTE_TM_PROFILE_ID".equalsIgnoreCase(keyField))
            {
                projectTM.setRemoteTmProfileId(Long.parseLong(valueField));
            }
            else if ("REMOTE_TM_PROFILE_NAME".equalsIgnoreCase(keyField))
            {
                projectTM.setRemoteTmProfileName(valueField);;
            }
            else if ("CONVERT_RATE".equalsIgnoreCase(keyField))
            {
                projectTM.setConvertRate(Integer.parseInt(valueField));
            }
            else if ("LAST_TU_ID".equalsIgnoreCase(keyField))
            {
                projectTM.setLastTUId(Long.parseLong(valueField));;
            }
            else if ("STATUS".equalsIgnoreCase(keyField))
            {
                projectTM.setStatus(valueField);
            }
            else if ("CONVERTED_TM3_ID".equalsIgnoreCase(keyField))
            {
                projectTM.setConvertedTM3Id(Long.parseLong(valueField));
            }
            else if ("PROJECT_TM_ATTRIBUTE_IDS".equalsIgnoreCase(keyField))
            {
                Set<TMAttribute> attributes = new HashSet<TMAttribute>();
                if (StringUtil.isNotEmptyAndNull(valueField))
                {
                    String[] attributeIds = valueField.split(",");
                    for (String attributeId : attributeIds)
                    {
                        TMAttribute tmAttr = HibernateUtil.get(TMAttribute.class,
                                Long.parseLong(attributeId));
                        attributes.add(tmAttr);
                    }
                }
                projectTM.setAttributes(attributes);
            }
        }
        return projectTM;
    }

    private String getTMNewName(String filterName, Long companyId)
    {
        String hql = "select pt.name from ProjectTM " + "  pt where pt.companyId=:companyid";
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

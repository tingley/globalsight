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

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfoKey;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Imports property file localization profiles.
 */
public class LocProfileImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(LocProfileImporter.class);
    private String sessionId;
    private long companyId;

    public LocProfileImporter(String sessionId, String companyId, String importToCompId)
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

    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<BasicL10nProfile> l10nProfileList = new ArrayList<BasicL10nProfile>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("LocalizationProfile"))
                {
                    BasicL10nProfile wftInfo = putDataIntoWFT(valueMap);
                    l10nProfileList.add(wftInfo);
                }
            }
        }

        if (l10nProfileList.size() > 0)
            dataMap.put("LocalizationProfile", l10nProfileList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private BasicL10nProfile putDataIntoWFT(Map<String, String> valueMap)
    {
        BasicL10nProfile l10nProfile = new BasicL10nProfile();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        dateFormat.setLenient(false);
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
                    l10nProfile.setName(valueField);
                }
                else if ("DESCRIPTION".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setDescription(valueField);
                }
                else if ("PRIORITY".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setPriority(Integer.parseInt(valueField));
                }
                else if ("SOURCE_LOCALE_ID".equalsIgnoreCase(keyField))
                {
                    GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager().getLocaleById(
                            Long.parseLong(valueField));
                    l10nProfile.setSourceLocale(sourceLocale);
                }
                else if ("PROJECT_NAME".equalsIgnoreCase(keyField))
                {
                    List<Project> projects = ServerProxy.getProjectHandler()
                            .getProjectsByCompanyId(companyId);
                    Project project = null;
                    for (Project pro : projects)
                    {
                        String projectName = pro.getName();
                        if (projectName.equalsIgnoreCase(valueField)
                                || projectName.startsWith(valueField + "_import_"))
                        {
                            project = pro;
                            break;
                        }
                    }
                    l10nProfile.setProject(project);
                }
                else if ("IS_AUTO_DISPATCH".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setAutoDispatch(Boolean.parseBoolean(valueField));
                }
                else if ("USE_MT_ON_JOB_CREATION".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setUseMtOnJobCreation(Boolean.parseBoolean(valueField));
                }
                else if ("TIMESTAMP".equalsIgnoreCase(keyField))
                {
                    Date timeDate = dateFormat.parse(valueField);
                    Timestamp dateTime = new Timestamp(timeDate.getTime());
                    l10nProfile.setTimestamp(dateTime);
                }
                else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setIsActive(Boolean.parseBoolean(valueField));
                }
                else if ("COMPANYID".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setCompanyId(companyId);
                }
                else if ("IS_SCRIPT_RUN_AT_JOB_CREATION".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setRunScriptAtJobCreation(Boolean.parseBoolean(valueField));
                }
                else if ("JOB_CREATION_SCRIPT_NAME".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setJobCreationScriptName(valueField);
                }
                else if ("TM_CHOICE".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setTmChoice(Integer.parseInt(valueField));
                }
                else if ("IS_EXACT_MATCH_EDIT".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setIsExactMatchEditing(Boolean.parseBoolean(valueField));
                }
                else if ("TM_EDIT_TYPE".equalsIgnoreCase(keyField))
                {
                    l10nProfile.setTMEditType(Integer.parseInt(valueField));
                }
                else if ("WF_STATE_POST_NAME".equalsIgnoreCase(keyField))
                {
                    long wfStatePostId = -1;
                    List<WorkflowStatePosts> wfStatePosts = ServerProxy.getProjectHandler()
                            .getWfStatePostProfileByCompanyId(companyId);
                    for (WorkflowStatePosts workflowStatePost : wfStatePosts)
                    {
                        if (workflowStatePost.getName().equalsIgnoreCase(valueField)
                                || workflowStatePost.getName().startsWith(valueField + "_import_"))
                        {
                            wfStatePostId = workflowStatePost.getId();
                            break;
                        }
                    }
                    l10nProfile.setWfStatePostId(wfStatePostId);
                }
                else if ("WORKFLOW_TEMPLATE_NAMES".equals(keyField))
                {
                    Set<WorkflowTemplateInfo> wftiSet = new HashSet<WorkflowTemplateInfo>();
                    String[] workflowTemplateNames = valueField.split(",");
                    for (String workflowTemplateName : workflowTemplateNames)
                    {
                        WorkflowTemplateInfo wftInfo = ServerProxy.getProjectHandler()
                                .getWorkflowTemplateInfoByNameAndCompanyId(workflowTemplateName,
                                        companyId);
                        if (wftInfo != null)
                            wftiSet.add(wftInfo);

                    }
                    l10nProfile.setWorkflowTemplates(wftiSet);
                }
                else if ("TM_PROFILE_NAME".equalsIgnoreCase(keyField))
                {
                    List<TranslationMemoryProfile> tmProfiles = ServerProxy.getProjectHandler()
                            .getAllTMProfilesByCompanyId(companyId);
                    Set<TranslationMemoryProfile> tmpSet = new HashSet<TranslationMemoryProfile>();
                    for (TranslationMemoryProfile tmp : tmProfiles)
                    {
                        String tmpName = tmp.getName();
                        if (tmpName.equalsIgnoreCase(valueField)
                                || tmpName.startsWith(valueField + "_import_"))
                        {
                            tmpSet.add(tmp);
                            break;
                        }
                    }
                    l10nProfile.setTmProfiles(tmpSet);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return l10nProfile;
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("LocalizationProfile"))
            {
                storeL10NData(dataMap);
            }

            addMessage("<b> Done importing Localization Profiles.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Localization Profiles.", e);
            addToError(e.getMessage());
        }

    }

    private void storeL10NData(Map<String, List> dataMap)
    {
        List<BasicL10nProfile> l10nProfileList = dataMap.get("LocalizationProfile");
        BasicL10nProfile l10nProfile = null;
        try
        {
            for (int i = 0; i < l10nProfileList.size(); i++)
            {
                l10nProfile = l10nProfileList.get(i);

                // checks project exist
                Project project = l10nProfile.getProject();

                // checks tm profile exist
                Set<TranslationMemoryProfile> tmpSet = l10nProfile.getTmProfiles();

                // checks workflow template exist
                Set<WorkflowTemplateInfo> wftiSet = l10nProfile.getWorkflowTemplates();
         
                if (project == null || tmpSet.size() == 0 || wftiSet.size() == 0)
                {
                    String msg = "Upload Localization Profile data failed ! Some require infos don't exist.";
                    logger.warn(msg);
                    addToError(msg);
                }
                else
                {
                    String oldName = l10nProfile.getName();
                    String newName = getL10NNewName(oldName, l10nProfile.getCompanyId());
                    l10nProfile.setName(newName);
                    l10nProfile.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    HibernateUtil.save(l10nProfile);

                    // saves l10nProfileWFTemplateInfo
                    saveL10nProfileWfTemplateInfo(l10nProfile);
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Localization Profile name <b>" + oldName
                                + "</b> already exists. <b>" + newName
                                + "</b> imported successfully.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Localization Profile data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private void saveL10nProfileWfTemplateInfo(BasicL10nProfile l10nProfile)
    {
        try
        {
            Set<WorkflowTemplateInfo> workflowTemplateInfoSet = l10nProfile.getWorkflowTemplates();
            for (Iterator it = workflowTemplateInfoSet.iterator(); it.hasNext();)
            {
                WorkflowTemplateInfo wfInfo = (WorkflowTemplateInfo) it.next();
                long mtProfileId = wfInfo.getMtProfileId();
                if (mtProfileId != 0)
                {
                    MachineTranslationProfile mtProfile = HibernateUtil.get(
                            MachineTranslationProfile.class, mtProfileId);
                    List<MachineTranslationProfile> mtProfileList = MTProfileHandlerHelper
                            .getAllMTProfiles(l10nProfile.getCompanyId());
                    for (MachineTranslationProfile mt : mtProfileList)
                    {
                        if (mt.getMtProfileName().equals(mtProfile.getMtProfileName())
                                || mt.getMtProfileName().startsWith(
                                        mtProfile.getMtProfileName() + "_import_"))
                        {
                            wfInfo.setMtProfileId(mt.getId());
                        }
                    }
                }
                L10nProfileWFTemplateInfo lnWfInfo = new L10nProfileWFTemplateInfo();
                L10nProfileWFTemplateInfoKey key = new L10nProfileWFTemplateInfoKey();
                key.setL10nProfileId(l10nProfile.getId());
                key.setWfTemplateId(wfInfo.getId());
                key.setMtProfileId(wfInfo.getMtProfileId() == 0 ? -1 : wfInfo.getMtProfileId());
                lnWfInfo.setKey(key);
                lnWfInfo.setIsActive(true);
                HibernateUtil.save(lnWfInfo);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getL10NNewName(String oldName, long companyId)
    {
        String hql = "select lp.name from BasicL10nProfile lp where lp.companyId=:companyId";
        Map map = new HashMap();
        map.put("companyId", companyId);
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

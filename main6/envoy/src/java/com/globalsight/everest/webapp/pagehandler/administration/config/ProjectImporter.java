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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.SetDefaultRoleUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.StringUtil;

/**
 * Imports property file projects.
 */
public class ProjectImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(ProjectImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;

    public ProjectImporter(String sessionId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = companyId;
        this.importToCompId = importToCompId;
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

    /**
     * Analysis and imports upload file.
     */
    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<ProjectImpl> projectList = new ArrayList<ProjectImpl>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("Project"))
                {
                    ProjectImpl project = putDataIntoPro(valueMap);
                    projectList.add(project);
                }
            }
        }

        if (projectList.size() > 0)
            dataMap.put("Project", projectList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private ProjectImpl putDataIntoPro(Map<String, String> valueMap)
    {
        ProjectImpl project = new ProjectImpl();
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
                if ("PROJECT_NAME".equalsIgnoreCase(keyField))
                {
                    project.setName(valueField);
                }
                else if ("DESCRIPTION".equalsIgnoreCase(keyField))
                {
                    project.setDescription(valueField);
                }
                else if ("MANAGER_USER_ID".equalsIgnoreCase(keyField))
                {
                    project.setManagerUserId(valueField);
                }
                else if ("TERMBASE_NAME".equalsIgnoreCase(keyField))
                {
                    project.setTermbaseName(valueField);
                }
                else if ("QUOTE_USER_ID".equalsIgnoreCase(keyField))
                {
                    project.setQuoteUserId(valueField);
                }
                else if ("COMPANYID".equalsIgnoreCase(keyField))
                {
                    if (importToCompId != null && !importToCompId.equals("-1"))
                    {
                        project.setCompanyId(Long.parseLong(importToCompId));
                    }
                    else
                    {
                        project.setCompanyId(Long.parseLong(currentCompanyId));
                    }
                }
                else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
                {
                    project.setIsActive(Boolean.parseBoolean(valueField));
                }
                else if ("PMCOST".equalsIgnoreCase(keyField))
                {
                    project.setPMCost(Float.parseFloat(valueField));
                }
                else if ("ATTRIBUTE_SET_ID".equalsIgnoreCase(keyField))
                {
                    AttributeSet attSet = null;
                    if (valueField != null)
                    {
                        long attSetId = Long.valueOf(valueField);
                        if (attSetId > 0)
                        {
                            attSet = HibernateUtil.get(AttributeSet.class, attSetId);
                        }
                        project.setAttributeSet(attSet);
                    }
                }
                else if ("POREQUIRED".equalsIgnoreCase(keyField))
                {
                    project.setPoRequired(Integer.parseInt(valueField));
                }
                else if ("AUTO_ACCEPT_TRANS".equalsIgnoreCase(keyField))
                {
                    project.setAutoAcceptTrans(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_SEND_TRANS".equalsIgnoreCase(keyField))
                {
                    project.setAutoSendTrans(Boolean.parseBoolean(valueField));
                }
                else if ("REVIEWONLYAUTOACCEPT".equalsIgnoreCase(keyField))
                {
                    project.setReviewOnlyAutoAccept(Boolean.parseBoolean(valueField));
                }
                else if ("REVIEWONLYAUTOSEND".equalsIgnoreCase(keyField))
                {
                    project.setReviewOnlyAutoSend(Boolean.parseBoolean(valueField));
                }
                else if ("REVIEW_REPORT_INCLUDE_COMPACT_TAGS".equalsIgnoreCase(keyField))
                {
                    project.setReviewReportIncludeCompactTags(Boolean.parseBoolean(valueField));
                }
                else if ("AUTOACCEPTPMTASK".equalsIgnoreCase(keyField))
                {
                    project.setAutoAcceptPMTask(Boolean.parseBoolean(valueField));
                }
                else if ("CHECK_UNTRANSLATED_SEGMENTS".equalsIgnoreCase(keyField))
                {
                    project.setCheckUnTranslatedSegments(Boolean.parseBoolean(valueField));
                }
                else if ("SAVE_TRANSLATIONS_EDIT_REPORT".equalsIgnoreCase(keyField))
                {
                    project.setSaveTranslationsEditReport(Boolean.parseBoolean(valueField));
                }
                else if ("SAVE_REVIEWERS_COMMENTS_REPORT".equalsIgnoreCase(keyField))
                {
                    project.setSaveReviewersCommentsReport(Boolean.parseBoolean(valueField));
                }
                else if ("SAVE_OFFLINE_FILES".equalsIgnoreCase(keyField))
                {
                    project.setSaveOfflineFiles(Boolean.parseBoolean(valueField));
                }
                else if ("ALLOW_MANUAL_QA_CHECKS".equalsIgnoreCase(keyField))
                {
                    project.setAllowManualQAChecks(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_ACCEPT_QA_TASK".equalsIgnoreCase(keyField))
                {
                    project.setAutoAcceptQATask(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_SEND_QA_REPORT".equalsIgnoreCase(keyField))
                {
                    project.setAutoSendQAReport(Boolean.parseBoolean(valueField));
                }
                else if ("MANUAL_RUN_DITA_CHECKS".equalsIgnoreCase(keyField))
                {
                    project.setManualRunDitaChecks(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_ACCEPT_DITA_QA_TASK".equalsIgnoreCase(keyField))
                {
                    project.setAutoAcceptDitaQaTask(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_SEND_DITA_QA_REPORT".equalsIgnoreCase(keyField))
                {
                    project.setAutoSendDitaQaReport(Boolean.parseBoolean(valueField));
                }
                else if ("PROJECT_USER".equalsIgnoreCase(keyField))
                {
                    Set<String> projectUserIds = new HashSet<>();
                    String[] userIds = valueField.split(",");
                    for (String userId : userIds)
                    {
                        projectUserIds.add(userId);
                    }
                    project.setUserIds(projectUserIds);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return project;
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("Project"))
            {
                storeProjectData(dataMap);
            }

            addMessage("<b> Done importing Projects.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Projects.", e);
            addToError(e.getMessage());
        }
    }

    private void storeProjectData(Map<String, List> dataMap)
    {
        List<ProjectImpl> projectList = dataMap.get("Project");
        ProjectImpl origProject = null;
        try
        {
            for (int i = 0; i < projectList.size(); i++)
            {
                origProject = projectList.get(i);
                long companyId = origProject.getCompanyId();
                // checks project manager exsits
                Vector<User> users = ServerProxy.getUserManager().getUsersFromCompany(
                        String.valueOf(companyId));
                String managerId = origProject.getProjectManagerId();
                User projectManager = null;
                for (User user : users)
                {
                    String userId = user.getUserId();
                    if (userId.equals(managerId) || userId.startsWith(managerId + "_import_"))
                    {
                        projectManager = ProjectHandlerHelper.getUser(userId);
                        origProject.setProjectManager(projectManager);
                        break;
                    }
                }

                if (projectManager != null)
                {
                    String oldName = origProject.getName();
                    String newName = getProjectNewName(oldName, companyId);
                    ProjectImpl newProject = createNewProject(newName, origProject);
                    HibernateUtil.save(newProject);
                    SetDefaultRoleUtil.setUserDefaultRoleToProject(newProject);
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Project name <b>" + oldName + "</b> already exists. <b>"
                                + newName + "</b> imported successfully.");
                    }
                }
                else
                {
                    String msg = "Upload Project data failed ! Project Manager is not exist.";
                    logger.warn(msg);
                    addToError(msg);
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Project data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Creates a new project.
     */
    private ProjectImpl createNewProject(String newName, ProjectImpl project)
    {
        try
        {
            long companyId = project.getCompanyId();
            Vector<User> users = ServerProxy.getUserManager().getUsersFromCompany(
                    String.valueOf(companyId));
            project.setName(newName);

            // save termbase
            String origTermName = project.getTermbaseName();
            if (StringUtil.isNotEmptyAndNull(origTermName))
            {
                ArrayList<String> termNames = TermbaseList.getNames(companyId);
                if (termNames.size() > 0)
                {
                    for (String termName : termNames)
                    {
                        if (termName.equals(origTermName)
                                || termName.startsWith(origTermName + "_import_"))
                        {
                            project.setTermbaseName(termName);
                            break;
                        }
                    }
                }
                else
                {
                    project.setTermbase("");
                }
            }

            // save attributeSet
            AttributeSet attSet = project.getAttributeSet();
            AttributeSet currentAttSet = null;
            if (attSet != null)
            {
                List<AttributeSet> attrSets = (List<AttributeSet>) AttributeManager
                        .getAllAttributeSets(companyId);
                if (attrSets.size() > 0)
                {
                    for (AttributeSet attrSet : attrSets)
                    {
                        if (attrSet.getName().equals(attSet.getName())
                                || attrSet.getName().startsWith(attSet.getName() + "_import_"))
                        {
                            currentAttSet = AttributeManager.getAttributeSetByNameAndCompanyId(
                                    attrSet.getName(), companyId);
                            project.setAttributeSet(currentAttSet);
                            break;
                        }
                    }
                }
                else
                {
                    project.setAttributeSet(null);
                }
            }

            // save quotePerson
            String quotePersonId = project.getQuotePersonId();
            if (StringUtil.isNotEmptyAndNull(quotePersonId))
            {
                if ("-1".equals(quotePersonId))
                {
                    project.setQuotePerson(null);
                }
                else if ("0".equals(quotePersonId))
                {
                    project.setQuotePerson("0");
                }
                else
                {

                    for (User user : users)
                    {
                        String userId = user.getUserId();
                        if (userId.equals(quotePersonId)
                                || userId.startsWith(quotePersonId + "_import_"))
                        {
                            project.setQuotePerson(ProjectHandlerHelper.getUser(quotePersonId));
                            break;
                        }
                    }
                }
            }

            // save project userIds
            Set<String> origUserIds = project.getUserIds();
            Set<String> currentUserIds = new HashSet<String>();
            for (String origUserId : origUserIds)
            {
                for (User user : users)
                {
                    String userId = user.getUserId();
                    if (userId.equals(origUserId) || userId.startsWith(origUserId + "_import_"))
                    {
                        currentUserIds.add(userId);
                        break;
                    }
                }
            }
            project.setUserIds(currentUserIds);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return project;
    }

    private String getProjectNewName(String oldName, long companyId)
    {
        String hql = "select p.name from ProjectImpl p where p.companyId=:companyId";
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

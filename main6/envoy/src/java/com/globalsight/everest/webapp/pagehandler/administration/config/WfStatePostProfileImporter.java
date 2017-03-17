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

import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports property file workflow state post profiles.
 */
public class WfStatePostProfileImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(WfStatePostProfileImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;

    public WfStatePostProfileImporter(String sessionId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = companyId;
        this.importToCompId = importToCompId;
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
        List<WorkflowStatePosts> wfStatePostList = new ArrayList<WorkflowStatePosts>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("WorkflowStatePostProfile"))
                {
                    WorkflowStatePosts wfStatePost = putDataIntoWfStatePost(valueMap);
                    wfStatePostList.add(wfStatePost);
                }
            }
        }

        if (wfStatePostList.size() > 0)
            dataMap.put("WorkflowStatePostProfile", wfStatePostList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            if (dataMap.containsKey("WorkflowStatePostProfile"))
            {
                storeWfStatePostData(dataMap);
            }
            addMessage("<b> Done importing Workflow State Post Profiles.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Workflow State Post Profiles.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeWfStatePostData(Map<String, List> dataMap)
    {
        List<WorkflowStatePosts> wfStatePostList = dataMap.get("WorkflowStatePostProfile");
        try
        {
            for (int i = 0; i < wfStatePostList.size(); i++)
            {
                WorkflowStatePosts wfStatePost = wfStatePostList.get(i);
                String oldName = wfStatePost.getName();
                String newName = getWfStatePostNewName(oldName, wfStatePost.getCompanyId());
                wfStatePost.setName(newName);
                HibernateUtil.save(wfStatePost);
                if (oldName.equals(newName))
                {
                    addMessage("<b>" + newName + "</b> imported successfully.");
                }
                else
                {
                    addMessage(" Workflow State Post Profile name <b>" + oldName
                            + "</b> already exists. <b>" + newName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Workflow State Post Profile data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private WorkflowStatePosts putDataIntoWfStatePost(Map<String, String> valueMap)
            throws ParseException
    {
        WorkflowStatePosts wfStatePost = new WorkflowStatePosts();
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
                wfStatePost.setName(valueField);
            }
            else if ("DESCRIPTION".equalsIgnoreCase(keyField))
            {
                wfStatePost.setDescription(valueField);
            }
            else if ("LISTENER_URL".equalsIgnoreCase(keyField))
            {
                wfStatePost.setListenerURL(valueField);
            }
            else if ("SECRET_KEY".equalsIgnoreCase(keyField))
            {
                wfStatePost.setSecretKey(valueField);
            }
            else if ("TIMEOUT_PERIOD".equalsIgnoreCase(keyField))
            {
                wfStatePost.setTimeoutPeriod(Integer.parseInt(valueField));
            }
            else if ("RETRY_NUMBER".equalsIgnoreCase(keyField))
            {
                wfStatePost.setRetryNumber(Integer.parseInt(valueField));
            }
            else if ("NOTIFY_EMAIL".equalsIgnoreCase(keyField))
            {
                wfStatePost.setNotifyEmail(valueField);
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    wfStatePost.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    wfStatePost.setCompanyId(Long.parseLong(currentCompanyId));
                }
            }
            else if ("POST_JOB_CHANGE".equalsIgnoreCase(keyField))
            {
                wfStatePost.setPostJobChange(Boolean.parseBoolean(valueField));
            }
        }
        return wfStatePost;
    }

    private String getWfStatePostNewName(String wfStatePostName, Long companyId)
    {
        String hql = "select wfsp.name from WorkflowStatePosts wfsp where wfsp.companyId=:companyid";
        Map map = new HashMap();
        map.put("companyid", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(wfStatePostName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (wfStatePostName.contains("_import_"))
                {
                    returnStr = wfStatePostName.substring(0, wfStatePostName.lastIndexOf('_')) + "_"
                            + num;
                }
                else
                {
                    returnStr = wfStatePostName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return wfStatePostName;
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

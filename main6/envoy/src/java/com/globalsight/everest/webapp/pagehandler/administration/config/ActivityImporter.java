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

import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Imports property file activity type info.
 */
public class ActivityImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(ActivityImporter.class);
    private String currentCompanyId;
    private String importToCompId;
    private String sessionId;

    public ActivityImporter(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.currentCompanyId = currentCompanyId;
        this.importToCompId = importToCompId;
        this.sessionId = sessionId;
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
        List<Activity> actList = new ArrayList<Activity>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("ActivityType"))
                {
                    Activity act = putDataIntoAct(valueMap);
                    actList.add(act);
                }
            }
        }

        if (actList.size() > 0)
            dataMap.put("ActivityType", actList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("ActivityType"))
            {
                // stores activity type info
                storeActTypeData(dataMap);
            }

            addMessage("<b> Done importing Activity Types.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Activity Types.", e);
            addToError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void storeActTypeData(Map<String, List> dataMap)
    {
        List<Activity> actList = dataMap.get("ActivityType");
        Activity act = null;
        try
        {
            for (int i = 0; i < actList.size(); i++)
            {
                act = actList.get(i);
                String oldDisName = act.getDisplayName();
                if (!isAlreadyExisted(oldDisName))
                {
                    HibernateUtil.save(act);
                    addMessage("<b>" + oldDisName + "</b> imported successfully.");
                    createRolesForActivity(act);
                }
                else
                {
                    addMessage("Activity Type name <b>" + oldDisName + "</b> already exists.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Activity Type data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private boolean isAlreadyExisted(String oldDisName)
    {
        String hql = "from Activity act where act.companyId=:companyid and act.isActive = 'Y'";
        Map map = new HashMap();
        map.put("companyid", Long.parseLong(currentCompanyId));
        List itList = HibernateUtil.search(hql, map);
        ArrayList names = new ArrayList();
        if (itList != null)
        {
            for (int i = 0; i < itList.size(); i++)
            {
                Activity act = (Activity) itList.get(i);
                names.add(act.getDisplayName());
            }
        }
        if (names.contains(oldDisName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Creates all the roles associated with the given activity.
     */
    private void createRolesForActivity(Activity p_activity) throws JobException
    {
        try
        {
            Iterator it = ServerProxy.getLocaleManager().getSourceTargetLocalePairs().iterator();
            while (it.hasNext())
            {
                LocalePair lp = (LocalePair) it.next();

                createRole(p_activity, lp.getSource(), lp.getTarget());
            }
        }
        catch (Exception e)
        {
            throwRoleException(JobException.MSG_FAILED_TO_CREATE_ROLE_FOR_ACTIVITY,
                    p_activity.getActivityName(), e);
        }
    }

    /**
     * Creates the role whose name is based on the given arguments.
     * */
    private void createRole(Activity p_activity, GlobalSightLocale p_source,
            GlobalSightLocale p_target) throws Exception
    {
        ContainerRole role = ServerProxy.getUserManager().createContainerRole();

        role.setActivity(p_activity);
        role.setSourceLocale(p_source.toString());
        role.setTargetLocale(p_target.toString());

        ServerProxy.getUserManager().addRole(role);
    }

    /**
     * Throws a JobException based on the given arguments.
     */
    private void throwRoleException(String p_msg, String p_activityName, Exception p_ex)
            throws JobException
    {
        logger.error(p_msg, p_ex);
        String args[] = new String[1];
        args[0] = p_activityName;

        throw new JobException(p_msg, args, p_ex);
    }

    private Activity putDataIntoAct(Map<String, String> valueMap) throws ParseException
    {
        Activity act = new Activity();
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
                act.setName(valueField);
            }
            else if (keyField.equalsIgnoreCase("DISPLAY_NAME"))
            {
                act.setDisplayName(valueField);
            }
            else if (keyField.equalsIgnoreCase("DESCRIPTION"))
            {
                act.setDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("USER_TYPE"))
            {
                act.setUseType(valueField);;
            }
            else if (keyField.equalsIgnoreCase("TYPE"))
            {
                act.setType(Activity.typeAsInt(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_EDITABLE"))
            {
                act.setIsEditable(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("QA_CHECKS"))
            {
                act.setQaChecks(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("RUN_DITA_QA_CHECKS"))
            {
                act.setRunDitaQAChecks(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("AUTO_COMPLETE_ACTIVITY"))
            {
                act.setAutoCompleteActivity(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPLETE_TYPE"))
            {
                act.setCompleteType(Integer.parseInt(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !"-1".equals(importToCompId))
                {
                    act.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    act.setCompanyId(Long.parseLong(currentCompanyId));
                }
            }
            else if (keyField.equalsIgnoreCase("AFTER_JOB_CREATION"))
            {
                act.setAfterJobCreation(valueField);
            }
            else if (keyField.equalsIgnoreCase("AFTER_JOB_DISPATCH"))
            {
                act.setAfterJobDispatch(valueField);
            }
            else if (keyField.equalsIgnoreCase("AFTER_ACTIVITY_START"))
            {
                act.setAfterActivityStart(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPLETE_SCHEDULE"))
            {
                act.setCompleteSchedule(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_ACTIVE"))
            {
                act.setIsActive(Boolean.parseBoolean(valueField));
            }
        }
        return act;
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

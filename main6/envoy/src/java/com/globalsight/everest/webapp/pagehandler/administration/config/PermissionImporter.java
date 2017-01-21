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
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Imports property file permission group info.
 */
public class PermissionImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(PermissionImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;

    public PermissionImporter(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = currentCompanyId;
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

    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<PermissionGroupImpl> permList = new ArrayList<PermissionGroupImpl>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("PermissionGroup"))
                {
                    PermissionGroupImpl perm = putDataIntoPermGroup(valueMap);
                    permList.add(perm);
                }
            }
        }

        if (permList.size() > 0)
            dataMap.put("PermissionGroup", permList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("PermissionGroup"))
            {
                // stores permission group
                storePermGData(dataMap);
            }

            addMessage("<b> Done importing Permission Groups.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Permission Groups.", e);
            addToError(e.getMessage());
        }
    }

    private void storePermGData(Map<String, List> dataMap)
    {
        List<PermissionGroupImpl> permList = dataMap.get("PermissionGroup");
        PermissionGroupImpl perm = null;
        try
        {
            for (int i = 0; i < permList.size(); i++)
            {
                perm = permList.get(i);
                Collection<UserImpl> userList = PermissionHelper.getAllUsersForPermissionGroup(perm
                        .getId());
                PermissionGroupImpl clone = new PermissionGroupImpl(perm.getName(),
                        perm.getDescription(), perm.getPermissionSetAsString(), String.valueOf(perm
                                .getCompanyId()));
                String oldName = clone.getName();
                if (!isAlreadyExisted(oldName))
                {
                    HibernateUtil.save(clone);
                    addMessage("<b>" + clone.getName() + "</b> imported successfully.");
                    // stores user info
                    storePermUser(clone, userList);
                }
                else
                {
                    addMessage(" Permission Group name <b>" + oldName + "</b> already exists.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Permission Group data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private void storePermUser(PermissionGroupImpl permGroup, Collection<UserImpl> userList)
    {
        try
        {
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    Long.parseLong(currentCompanyId));
            ArrayList add = new ArrayList();
            for (UserImpl userImpl : userList)
            {
                String hsql = "from UserImpl as u where u.userId =:userId and a.companyName =:cName";
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("userId", userImpl.getUserId());
                map.put("cName", company.getName());
                List res = HibernateUtil.search(hsql, map);
                if (res != null && res.size() > 0)
                {
                    UserImpl user = (UserImpl) res.get(0);
                    add.add(user.getUserId());
                }
                else
                {
                    String msg = "Cannot find proper User. Name: <b>" + userImpl.getUserName()
                            + "</b> in Company <b>" + company.getName() + "</b>.";
                    addToError(msg);
                    continue;
                }
            }
            if (add.size() > 0)
            {
                Permission.getPermissionManager().mapUsersToPermissionGroup(add, permGroup);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean isAlreadyExisted(String oldName)
    {
        ArrayList list = (ArrayList) PermissionHelper.getAllPermissionGroups();
        ArrayList names = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                PermissionGroup group = (PermissionGroup) list.get(i);
                names.add(group.getName());
            }
        }
        if (names.contains(oldName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private PermissionGroupImpl putDataIntoPermGroup(Map<String, String> valueMap)
    {
        PermissionGroupImpl perm = new PermissionGroupImpl();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if ("ID".equalsIgnoreCase(keyField))
            {
                perm.setId(Long.parseLong(valueField));
            }
            else if ("NAME".equalsIgnoreCase(keyField))
            {
                perm.setName(valueField);
            }
            else if ("COMPANY_ID".equalsIgnoreCase(keyField))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    perm.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    perm.setCompanyId(Long.parseLong(currentCompanyId));
                }
            }
            else if ("DESCRIPTION".equalsIgnoreCase(keyField))
            {
                perm.setDescription(valueField);
            }
            else if ("PERMISSION_SET".equalsIgnoreCase(keyField))
            {
                perm.setPermissionSet(valueField);
            }
        }
        return perm;
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

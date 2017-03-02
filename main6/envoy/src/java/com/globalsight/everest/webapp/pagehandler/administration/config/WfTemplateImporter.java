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
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * Imports property file workflows.
 */
public class WfTemplateImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(WfTemplateImporter.class);
    private String sessionId;
    private long companyId;
    private String path;

    public WfTemplateImporter(String sessionId, String currentCompanyId, String importToCompId)
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
        path = uploadedFile.getParentFile().getParentFile().getPath();

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
        List<WorkflowTemplateInfo> wftInfoList = new ArrayList<WorkflowTemplateInfo>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("WorkflowTemplateInfo"))
                {
                    WorkflowTemplateInfo wftInfo = putDataIntoWFT(valueMap);
                    wftInfoList.add(wftInfo);
                }
            }
        }

        if (wftInfoList.size() > 0)
            dataMap.put("WorkflowTemplateInfo", wftInfoList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private WorkflowTemplateInfo putDataIntoWFT(Map<String, String> valueMap)
    {
        WorkflowTemplateInfo wftInfo = new WorkflowTemplateInfo();
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
                    wftInfo.setName(valueField);
                }
                else if ("DESCRIPTION".equalsIgnoreCase(keyField))
                {
                    wftInfo.setDescription(valueField);
                }
                else if ("PROJECT_NAME".equalsIgnoreCase(keyField))
                {
                    List<Project> projectList = ServerProxy.getProjectHandler()
                            .getProjectsByCompanyId(companyId);
                    Project project = null;
                    for (Project pro : projectList)
                    {
                        if (pro.getName().equals(valueField)
                                || pro.getName().startsWith(valueField + "_import_"))
                        {
                            project = pro;
                            break;
                        }
                    }
                    wftInfo.setProject(project);
                }
                else if ("SOURCE_LOCALE_ID".equalsIgnoreCase(keyField))
                {
                    GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager().getLocaleById(
                            Long.parseLong(valueField));
                    wftInfo.setSourceLocale(sourceLocale);
                }
                else if ("TARGET_LOCALE_ID".equalsIgnoreCase(keyField))
                {
                    GlobalSightLocale targetLocale = ServerProxy.getLocaleManager().getLocaleById(
                            Long.parseLong(valueField));
                    wftInfo.setTargetLocale(targetLocale);
                }
                else if ("CHAR_SET".equalsIgnoreCase(keyField))
                {
                    wftInfo.setCodeSet(valueField);
                }
                else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
                {
                    wftInfo.setIsActive(Boolean.parseBoolean(valueField));
                }
                else if ("NOTIFY_PM".equalsIgnoreCase(keyField))
                {
                    wftInfo.notifyProjectManager(Boolean.parseBoolean(valueField));
                }
                else if ("TYPE".equalsIgnoreCase(keyField))
                {
                    wftInfo.setWorkflowType(valueField);
                }
                else if ("COMPANYID".equalsIgnoreCase(keyField))
                {
                    wftInfo.setCompanyId(companyId);
                }
                else if ("SCORECARD_SHOWTYPE".equalsIgnoreCase(keyField))
                {
                    wftInfo.setScorecardShowType(Integer.parseInt(valueField));
                }
                else if ("PERPLEXITY_ID".equalsIgnoreCase(keyField))
                {
                    if (StringUtil.isEmptyAndNull(valueField))
                    {
                        wftInfo.setPerplexityService(null);
                    }
                    else
                    {
                        PerplexityService ps = HibernateUtil.get(PerplexityService.class,
                                Long.parseLong(valueField));
                        wftInfo.setPerplexityService(ps);
                    }

                }
                else if ("PERPLEXITY_KEY".equalsIgnoreCase(keyField))
                {
                    if (StringUtil.isEmptyAndNull(valueField))
                    {
                        wftInfo.setPerplexityKey(null);
                    }
                    else
                    {
                        wftInfo.setPerplexityKey(valueField);
                    }
                }
                else if ("PERPLEXITY_SOURCE_THRESHOLD".equalsIgnoreCase(keyField))
                {
                    wftInfo.setPerplexitySourceThreshold(Double.parseDouble(valueField));
                }
                else if ("PERPLEXITY_TARGET_THRESHOLD".equalsIgnoreCase(keyField))
                {
                    wftInfo.setPerplexityTargetThreshold(Double.parseDouble(valueField));
                }
                else if ("WORKFLOW_MANAGER_ID".equalsIgnoreCase(keyField))
                {
                    List wfMgrIds = new ArrayList();
                    String[] managerIds = valueField.split(",");
                    for (String wfMgrId : managerIds)
                    {
                        wfMgrIds.add(wfMgrId);
                    }

                    wftInfo.setWorkflowManagerIds(wfMgrIds);
                }
                else if ("LEVERAGE_LOCALES".equalsIgnoreCase(keyField))
                {
                    String[] localeIds;
                    localeIds = valueField.split(",");
                    Set<LeverageLocales> leveragedLocales = new HashSet<LeverageLocales>();

                    for (String localeId : localeIds)
                    {
                        GlobalSightLocale gsl = ServerProxy.getLocaleManager().getLocaleById(
                                Long.parseLong(localeId));
                        LeverageLocales leverageLocales = new LeverageLocales(gsl);
                        leveragedLocales.add(leverageLocales);
                    }
                    wftInfo.setLeveragingLocalesSet(leveragedLocales);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return wftInfo;
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("WorkflowTemplateInfo"))
            {
                storeWFTData(dataMap);
            }

            addMessage("<b> Done importing Workflows.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Workflows.", e);
            addToError(e.getMessage());
        }
    }

    private void storeWFTData(Map<String, List> dataMap)
    {
        List<WorkflowTemplateInfo> wftInfoList = dataMap.get("WorkflowTemplateInfo");
        WorkflowTemplateInfo origWorkflowTemplateInfo = null;
        try
        {
            for (int i = 0; i < wftInfoList.size(); i++)
            {
                origWorkflowTemplateInfo = wftInfoList.get(i);
                LocalePair lp = ServerProxy.getLocaleManager()
                        .getLocalePairBySourceTargetAndCompanyStrings(
                                origWorkflowTemplateInfo.getSourceLocale().toString(),
                                origWorkflowTemplateInfo.getTargetLocale().toString(),
                                origWorkflowTemplateInfo.getCompanyId());
                // checks project exist
                Project project = origWorkflowTemplateInfo.getProject();
                if (project == null || lp == null)
                {
                    String msg = "Failed uploading Workflow data! Missing some required information.";
                    logger.warn(msg);
                    addToError(msg);
                }
                else
                {
                    String oldName = origWorkflowTemplateInfo.getName();
                    if (!isAlreadyExisted(oldName))
                    {
                        WorkflowTemplate workflowTemplate = new WorkflowTemplate();
                        String fileName = path + File.separator + "WorkflowTemplateXml"
                                + File.separator + oldName + WorkflowConstants.SUFFIX_XML;
                        SAXReader reader = new SAXReader();
                        Document doc = reader.read(new File(fileName));
                        workflowTemplate.setName(oldName);
                        workflowTemplate.setDescription(origWorkflowTemplateInfo.getDescription());
                        WorkflowTemplateInfo workflowTemplateInfo = createNewWorkflowTemplateInfo(
                                lp, project, origWorkflowTemplateInfo);
                        WorkflowTemplate jbpmTemp = createIFlowNew(workflowTemplate, doc);
                        workflowTemplateInfo.setWorkflowTemplate(jbpmTemp);
                        HibernateUtil.save(workflowTemplateInfo);
                        addMessage("<b>" + workflowTemplateInfo.getName()
                                + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Workflow name <b>" + oldName + "</b> already exists.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Workflow data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private WorkflowTemplate createIFlowNew(WorkflowTemplate p_workflowTemplate, Document doc)
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessDefinition pd = ProcessDefinition.parseXmlString(doc.asXML());
            ctx.deployProcessDefinition(pd);
            p_workflowTemplate.setId(pd.getId());
            saveXmlToFileStore(doc, p_workflowTemplate.getName());
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            ctx.close();
        }
        return p_workflowTemplate;
    }

    private void saveXmlToFileStore(Document p_document, String p_templateName)
    {
        OutputFormat format = OutputFormat.createCompactFormat();
        XMLWriter writer = null;
        try
        {
            writer = new XMLWriter(new FileOutputStream(AmbFileStoragePathUtils
                    .getWorkflowTemplateXmlDir().getAbsolutePath()
                    + File.separator
                    + p_templateName + WorkflowConstants.SUFFIX_XML), format);
            writer.write(p_document);
        }
        catch (Exception e)
        {
            logger.info("Exception occurs when saving the template xml to file storage");
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    private WorkflowTemplateInfo createNewWorkflowTemplateInfo(LocalePair p_localePair,
            Project project, WorkflowTemplateInfo workflowTemplateInfo)
    {
        try
        {
            long companyId = workflowTemplateInfo.getCompanyId();

            // saves leverage locales
            Set<LeverageLocales> lls = new HashSet<LeverageLocales>();
            Set<LeverageLocales> origLls = workflowTemplateInfo.getLeveragingLocalesSet();
            for (Iterator it = origLls.iterator(); it.hasNext();)
            {
                LeverageLocales l = ((LeverageLocales) it.next()).cloneForInsert();
                l.setBackPointer(workflowTemplateInfo);
                lls.add(l);
            }
            workflowTemplateInfo.setLeveragingLocalesSet(lls);

            // saves workflow_managers
            List<String> wfmIds = workflowTemplateInfo.getWorkflowManagerIds();
            ArrayList<String> wfmanagerIds = new ArrayList<String>();
            ArrayList<String> userIds = new ArrayList<String>();
            Vector<User> users = ServerProxy.getUserManager().getUsersFromCompany(
                    String.valueOf(companyId));
            for (User user : users)
            {
                userIds.add(user.getUserId());
            }
            for (String wfmId : wfmIds)
            {
                for (String userId : userIds)
                {
                    if (StringUtil.isNotEmptyAndNull(wfmId)
                            && (userId.equalsIgnoreCase(wfmId) || userId.startsWith(wfmId)))
                    {
                        wfmanagerIds.add(userId);
                    }
                }
            }
            workflowTemplateInfo.setWorkflowManagerIds(wfmanagerIds);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return workflowTemplateInfo;
    }

    private boolean isAlreadyExisted(String oldDisName)
    {
        String hql = "from WorkflowTemplateInfo wf where wf.companyId=:companyid and wf.isActive = 'Y'";
        Map map = new HashMap();
        map.put("companyid", companyId);
        List itList = HibernateUtil.search(hql, map);
        ArrayList names = new ArrayList();
        if (itList != null)
        {
            for (int i = 0; i < itList.size(); i++)
            {
                WorkflowTemplateInfo act = (WorkflowTemplateInfo) itList.get(i);
                names.add(act.getName());
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

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
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.User;

/**
 * Starts a thread to import configuration files.
 */
public class ConfigImporter extends MultiCompanySupportedThread implements ConfigConstants
{
    private String companyId;
    private String sessionId;
    private String importToCompId;
    private User user;
    private Map<String, File> uploadFiles;
    private int wfXmlSize;

    private static final Logger logger = Logger.getLogger(ConfigImporter.class);

    public ConfigImporter(String sessionId, Map<String, File> fileInfo, User user, String companyId,
            String importToCompId, int wfxmlFileSize)
    {
        this.sessionId = sessionId;
        this.uploadFiles = fileInfo;
        this.companyId = companyId;
        this.user = user;
        this.importToCompId = importToCompId;
        this.wfXmlSize = wfxmlFileSize;
    }

    public void run()
    {
        this.decideFileTypeAndImport(uploadFiles);
    }

    private void decideFileTypeAndImport(Map<String, File> fileInfo)
    {
        try
        {
            int size = fileInfo.keySet().size() - wfXmlSize;
            int i = 0;

            if (fileInfo.containsKey(ATTRIBUTE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(ATTRIBUTE_FILE_NAME);
                AttributeImporter attrImporter = new AttributeImporter(sessionId, companyId,
                        importToCompId);
                attrImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(ATTRIBUTE_SET_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(ATTRIBUTE_SET_FILE_NAME);
                AttributeSetImporter attrSetImporter = new AttributeSetImporter(sessionId,
                        companyId, importToCompId);
                attrSetImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(LOCALEPAIR_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(LOCALEPAIR_FILE_NAME);
                LocalePairImport lpImporter = new LocalePairImport(sessionId, companyId,
                        importToCompId);
                lpImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(ACTIVITY_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(ACTIVITY_FILE_NAME);
                ActivityImporter actImporter = new ActivityImporter(sessionId, companyId,
                        importToCompId);
                actImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(CURRENCY_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(CURRENCY_FILE_NAME);
                CurrencyImporter currImporter = new CurrencyImporter(sessionId, companyId,
                        importToCompId);
                currImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(RATE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(RATE_FILE_NAME);
                RatesImporter rateImporter = new RatesImporter(sessionId, companyId,
                        importToCompId);
                rateImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(PERMISSION_GROUP_NAME))
            {
                i++;
                File file = fileInfo.get(PERMISSION_GROUP_NAME);
                PermissionImporter permImporter = new PermissionImporter(sessionId, companyId,
                        importToCompId);
                permImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(USER_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(USER_FILE_NAME);
                UserImport userImporter = new UserImport(sessionId, user, companyId,
                        importToCompId);
                userImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(TM_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(TM_FILE_NAME);
                TMImporter tmImporter = new TMImporter(sessionId, user.getUserId(), companyId,
                        importToCompId);
                tmImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(SEGMENT_RULE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(SEGMENT_RULE_FILE_NAME);
                SegmentationRuleImporter srImporter = new SegmentationRuleImporter(sessionId,
                        companyId, importToCompId);
                srImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(TM_PROFILE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(TM_PROFILE_FILE_NAME);
                TMProfileImporter srImporter = new TMProfileImporter(sessionId, companyId,
                        importToCompId);
                srImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(MT_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(MT_FILE_NAME);
                MTProfileImport mtImporter = new MTProfileImport(sessionId, companyId,
                        importToCompId);
                mtImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(PERPLEXITY_SERVICE))
            {
                i++;
                File file = fileInfo.get(PERPLEXITY_SERVICE);
                PerplexityServiceImporter psImporter = new PerplexityServiceImporter(sessionId,
                        companyId, importToCompId);
                psImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(TERMINOLOGY_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(TERMINOLOGY_FILE_NAME);
                TermbaseImporter tbImporter = new TermbaseImporter(sessionId, companyId,
                        importToCompId);
                tbImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(PROJECT_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(PROJECT_FILE_NAME);
                ProjectImporter proImporter = new ProjectImporter(sessionId, companyId,
                        importToCompId);
                proImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(WORKFLOW_TEMPLATE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(WORKFLOW_TEMPLATE_FILE_NAME);
                WfTemplateImporter wfImporter = new WfTemplateImporter(sessionId, companyId,
                        importToCompId);
                wfImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(WORKFLOW_STATE_POST_PROFILE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(WORKFLOW_STATE_POST_PROFILE_FILE_NAME);
                WfStatePostProfileImporter wfStatePostImporter = new WfStatePostProfileImporter(
                        sessionId, companyId, importToCompId);
                wfStatePostImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(LOC_PROFILE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(LOC_PROFILE_FILE_NAME);
                LocProfileImporter wfImporter = new LocProfileImporter(sessionId, companyId,
                        importToCompId);
                wfImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(FILE_PROFILE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(FILE_PROFILE_FILE_NAME);
                FileProfileImporter fpImporter = new FileProfileImporter(sessionId, companyId,
                        importToCompId);
                fpImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(XML_RULE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(XML_RULE_FILE_NAME);
                XmlRuleImporter srImporter = new XmlRuleImporter(sessionId, companyId,
                        importToCompId);
                srImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
            if (fileInfo.containsKey(FILTER_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(FILTER_FILE_NAME);
                FilterConfigImport filterImporter = new FilterConfigImport(sessionId,
                        user.getUserId(), companyId, importToCompId);
                filterImporter.analysisAndImport(file);
                this.cachePercentage(i, size);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to import Configuration Files.", e);
            addToError(e.getMessage());
        }
    }

    private void cachePercentage(int i, int size)
    {
        int percentage = (int) (i * 100 / size);
        config_percentage_map.put(sessionId, percentage);
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? ""
                : config_error_map.get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:red'>" + msg);
    }

}

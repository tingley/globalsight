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

    private static final Logger logger = Logger.getLogger(ConfigImporter.class);
    public ConfigImporter(String sessionId, Map<String, File> fileInfo, User user,
            String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.uploadFiles = fileInfo;
        this.companyId = companyId;
        this.user = user;
        this.importToCompId = importToCompId;
    }

    public void run()
    {
        this.decideFileTypeAndImport(uploadFiles);
    }

    private void decideFileTypeAndImport(Map<String, File> fileInfo)
    {
        try
        {
            int size = fileInfo.keySet().size();
            int i = 0;
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
            if (fileInfo.containsKey(USER_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(USER_FILE_NAME);
                UserImport userImporter = new UserImport(sessionId, user, companyId, importToCompId);
                userImporter.analysisAndImport(file);
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
            if (fileInfo.containsKey(FILTER_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(FILTER_FILE_NAME);
                FilterConfigImport filterImporter = new FilterConfigImport(sessionId,
                        user.getUserId(), companyId, importToCompId);
                filterImporter.analysisAndImport(file);
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
            if (fileInfo.containsKey(SEGMENT_RULE_FILE_NAME))
            {
                i++;
                File file = fileInfo.get(SEGMENT_RULE_FILE_NAME);
                SegmentationRuleImporter srImporter = new SegmentationRuleImporter(sessionId,
                        companyId, importToCompId);
                srImporter.analysisAndImport(file);
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
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:red'>" + msg);
    }
    
    
}

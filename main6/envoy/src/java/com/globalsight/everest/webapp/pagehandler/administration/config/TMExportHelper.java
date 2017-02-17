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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports translation memories (configuration only - not content).
 */
public class TMExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Translation Memories");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = TM_FILE_NAME + userName + "_" + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets translation memories info.
     */
    public static File propertiesInputTM(File tmPropertyFile, String p_projectTMId)
    {
        try
        {
            ProjectTM tm = ServerProxy.getProjectHandler().getProjectTMById(
                    Long.valueOf(p_projectTMId), false);
            StringBuffer buffer = new StringBuffer();
            buffer.append("##TranslationMemory.").append(tm.getCompanyId()).append(".")
                    .append(tm.getId()).append(".begin").append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".ID=")
                    .append(tm.getId()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".NAME=")
                    .append(tm.getName()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".DOMAIN=")
                    .append(tm.getDomain()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".ORGANIZATION=")
                    .append(tm.getOrganization()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".DESCRIPTION=")
                    .append(tm.getDescription()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".INDEX_TARGET=")
                    .append(tm.isIndexTarget()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".TM3_ID=")
                    .append(tm.getTm3Id()).append(NEW_LINE);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            buffer.append("TranslationMemory.").append(tm.getId()).append(".CREATION_DATE=")
                    .append(df.format(tm.getCreationDate())).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".CREATION_USER=")
                    .append(tm.getCreationUser()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".COMPANY_ID=")
                    .append(tm.getCompanyId()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".IS_REMOTE_TM=")
                    .append(tm.getIsRemoteTm()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".REMOTE_TM_PROFILE_ID=")
                    .append(tm.getRemoteTmProfileId()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId())
                    .append(".REMOTE_TM_PROFILE_NAME=").append(tm.getRemoteTmProfileName())
                    .append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".CONVERT_RATE=")
                    .append(tm.getConvertRate()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".LAST_TU_ID=")
                    .append(tm.getLastTUId()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".STATUS=")
                    .append(tm.getStatus()).append(NEW_LINE);
            buffer.append("TranslationMemory.").append(tm.getId()).append(".CONVERTED_TM3_ID=")
                    .append(tm.getConvertedTM3Id()).append(NEW_LINE);
            // exports TMAttribute
            List<TMAttribute> tmAttrList = tm.getAllTMAttributes();
            StringBuffer tmAttrIds = new StringBuffer();
            if (tmAttrList != null && tmAttrList.size() > 0)
            {
                for (TMAttribute tmAttr : tmAttrList)
                {
                    tmAttrIds.append(tmAttr.getId()).append(",");
                }
                if (tmAttrIds.length() > 1)
                    tmAttrIds.deleteCharAt(tmAttrIds.length() - 1);
            }
            buffer.append("TranslationMemory.").append(tm.getId())
                    .append(".PROJECT_TM_ATTRIBUTE_IDS=").append(tmAttrIds).append(NEW_LINE);
            buffer.append("##TranslationMemory.").append(tm.getCompanyId()).append(".")
                    .append(tm.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);
            writeToFile(tmPropertyFile, buffer.toString().getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tmPropertyFile;
    }

    private static void writeToFile(File tmPropertyFile, byte[] bytes)
    {
        tmPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(tmPropertyFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }
}

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
import java.util.Vector;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.QAFilter;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports file profiles.
 */
public class FileProfileExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append(" File Profiles");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = FILE_PROFILE_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets file profiles info.
     */
    public static File propertiesInputFP(File fpPropertyFile, String profileId)
    {
        try
        {
            FileProfileImpl fileProfile = (FileProfileImpl) ServerProxy
                    .getFileProfilePersistenceManager().getFileProfileById(
                            Long.parseLong(profileId), true);
            StringBuffer buffer = new StringBuffer();
            if (fileProfile != null)
            {
                buffer.append("##FileProfile.").append(fileProfile.getCompanyId()).append(".")
                        .append(fileProfile.getId()).append(".begin").append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".ID = ")
                        .append(fileProfile.getId()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".NAME = ")
                        .append(fileProfile.getName()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".DESCRIPTION = ")
                        .append(fileProfile.getDescription()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".KNOWN_FORMAT_TYPE_ID = ")
                        .append(fileProfile.getKnownFormatTypeId()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".CODE_SET = ")
                        .append(fileProfile.getCodeSet()).append(NEW_LINE);
                XmlDtdImpl xmlDtd = fileProfile.getXmlDtd();
                if (xmlDtd == null)
                {
                    buffer.append("FileProfile.").append(fileProfile.getId())
                            .append(".XML_DTD_NAME = ").append("")
                            .append(NEW_LINE);
                }
                else
                {
                    buffer.append("FileProfile.").append(fileProfile.getId())
                            .append(".XML_DTD_NAME = ").append(xmlDtd.getName()).append(NEW_LINE);
                }
                String locProfileName = HibernateUtil.get(BasicL10nProfile.class,
                        fileProfile.getL10nProfileId()).getName();
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".L10N_PROFILE_NAME = ").append(locProfileName).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".DEFAULT_EXPORT_STF = ").append(fileProfile.byDefaultExportStf())
                        .append(NEW_LINE);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".TIMESTAMP = ")
                        .append(df.format(fileProfile.getTimestamp())).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".IS_ACTIVE = ")
                        .append(fileProfile.getIsActive()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".FILTER_NAME = ")
                        .append(fileProfile.getFilterName()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".FILTER_TABLE_NAME = ").append(fileProfile.getFilterTableName())
                        .append(NEW_LINE);
                QAFilter qaFilter = fileProfile.getQaFilter();
                if (qaFilter == null)
                {
                    buffer.append("FileProfile.").append(fileProfile.getId())
                            .append(".QA_FILTER_NAME = ").append("").append(NEW_LINE);
                }
                else
                {
                    buffer.append("FileProfile.").append(fileProfile.getId())
                            .append(".QA_FILTER_NAME = ").append(qaFilter.getFilterName())
                            .append(NEW_LINE);
                }
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".COMPANYID = ")
                        .append(fileProfile.getCompanyId()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".SCRIPT_ON_IMPORT = ").append(fileProfile.getScriptOnImport())
                        .append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".SCRIPT_ON_EXPORT = ").append(fileProfile.getScriptOnExport())
                        .append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".NEW_ID = ")
                        .append(fileProfile.getNewId()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".TERMINOLOGY_APPROVAL = ")
                        .append(fileProfile.getTerminologyApproval()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".XLF_SOURCE_AS_UNTRANSLATED_TARGET = ")
                        .append(fileProfile.getXlfSourceAsUnTranslatedTarget()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".REFERENCE_FP = ").append(fileProfile.getReferenceFP())
                        .append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId()).append(".BOM_TYPE = ")
                        .append(fileProfile.getBOMType()).append(NEW_LINE);
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".EOL_ENCODING = ").append(fileProfile.getEolEncoding())
                        .append(NEW_LINE);
                Vector<Long> extensionIds = fileProfile.getFileExtensionIds();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < extensionIds.size(); i++)
                {
                    FileExtensionImpl extension = HibernateUtil.get(FileExtensionImpl.class, extensionIds.elementAt(i));
                    sb.append(extension.getName());
                    if (i < extensionIds.size() - 1)
                    {
                        sb.append(",");
                    }
                }
                buffer.append("FileProfile.").append(fileProfile.getId())
                        .append(".EXTENSION_NAMES = ").append(sb).append(NEW_LINE);
                buffer.append("##FileProfile.").append(fileProfile.getCompanyId()).append(".")
                        .append(fileProfile.getId()).append(".end").append(NEW_LINE)
                        .append(NEW_LINE);

                writeToFile(fpPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return fpPropertyFile;
    }

    private static void writeToFile(File fpPropertyFile, byte[] bytes)
    {
        fpPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(fpPropertyFile, true);
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

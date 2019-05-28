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
import java.util.Set;

import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports system mt profile info to property.
 */
public class MTExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId)).append(File.separator)
                .append("GlobalSight").append(File.separator).append("config")
                .append(File.separator).append("export").append(File.separator)
                .append("MachineTranslationProfiles");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = MT_FILE_NAME + userName + "_" + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     *  Puts mt profile info to property.
     */
    public static File propertiesInputMTP(File mtPropertyFile, MachineTranslationProfile mtp)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("##MachineTranslationProfile.").append(mtp.getCompanyid()).append(".")
                .append(mtp.getId()).append(".begin").append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".ID=")
                .append(mtp.getId()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MT_PROFILE_NAME=")
                .append(mtp.getMtProfileName()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MT_ENGINE=")
                .append(mtp.getMtEngine()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".DESCRIPTION=")
                .append(mtp.getDescription()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MT_THRESHOLD=")
                .append(mtp.getMtThreshold()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".URL=")
                .append(mtp.getUrl()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MS_TRANS_VERSION=")
                .append(mtp.getMsVersion()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MS_TOKEN_URL=")
                .append(mtp.getMsTokenUrl()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".PORT=")
                .append(mtp.getPort()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".USERNAME=")
                .append(mtp.getUsername()).append(NEW_LINE);
        if (mtp.getPassword() == null)
        {
            buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".PASSWORD=")
                    .append("").append(NEW_LINE);
        }
        else
        {
            buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".PASSWORD=")
                    .append(mtp.getPassword()).append(NEW_LINE);
        }
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".CATEGORY=")
                .append(mtp.getCategory()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".ACCOUNTINFO=")
                .append(mtp.getAccountinfo()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".COMPANY_ID=")
                .append(mtp.getCompanyid()).append(NEW_LINE);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".TIMESTAMP=")
                .append(df.format(mtp.getTimestamp())).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".INCLUDE_MT_IDENTIFIERS=").append(mtp.isIncludeMTIdentifiers())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".IGNORE_TM_MATCHES=").append(mtp.isIgnoreTMMatch()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".LOG_DEBUG_INFO=")
                .append(mtp.isLogDebugInfo()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MS_TRANS_TYPE=")
                .append(mtp.getMsTransType()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".MS_MAX_LENGTH=")
                .append(mtp.getMsMaxLength()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_IDENTIFIER_LEADING=").append(mtp.getMtIdentifierLeading())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".MT_IDENTIFIER_TRAILING=").append(mtp.getMtIdentifierTrailing())
                .append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId()).append(".IS_ACTIVE=")
                .append(mtp.isActive()).append(NEW_LINE);
        buffer.append("MachineTranslationProfile.").append(mtp.getId())
                .append(".EXTENT_JSON_INFO=").append(mtp.getJsonInfo()).append(NEW_LINE);
        buffer.append("##MachineTranslationProfile.").append(mtp.getCompanyid()).append(".")
                .append(mtp.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);
        writeToFile(mtPropertyFile, buffer.toString().getBytes());

        String companyId = String.valueOf(mtp.getCompanyid());
        Set<MachineTranslationExtentInfo> mteInfoSet = mtp.getExInfo();
        if (mteInfoSet != null && mteInfoSet.size() > 0)
        {
            for (MachineTranslationExtentInfo mteInfo : mteInfoSet)
            {
                propertiesInputMTEInfo(mtPropertyFile, mteInfo, companyId);
            }
        }
        return mtPropertyFile;
    }

    /**
     * Puts mt profile extent info to property.
     */
    private static void propertiesInputMTEInfo(File mtPropertyFile,
            MachineTranslationExtentInfo mteInfo, String companyId)
    {
        if (mteInfo == null)
            return;

        StringBuffer buffer = new StringBuffer();
        buffer.append("##MachineTranslationExtentInfo.").append(companyId).append(".")
                .append(mteInfo.getId()).append(".begin").append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId()).append(".ID=")
                .append(mteInfo.getId()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".MT_PROFILE_ID=").append(mteInfo.getMtProfile().getId()).append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".LANGUAGE_PAIR_CODE=").append(mteInfo.getLanguagePairCode())
                .append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".LANGUAGE_PAIR_NAME=").append(mteInfo.getLanguagePairName())
                .append(NEW_LINE);
        buffer.append("MachineTranslationExtentInfo.").append(mteInfo.getId())
                .append(".DOMAIN_CODE=").append(mteInfo.getDomainCode()).append(NEW_LINE);
        buffer.append("##MachineTranslationExtentInfo.").append(companyId).append(".")
                .append(mteInfo.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);
        writeToFile(mtPropertyFile, buffer.toString().getBytes());
    }

    private static void writeToFile(File mtPropertyFile, byte[] bytes)
    {
        mtPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(mtPropertyFile, true);
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

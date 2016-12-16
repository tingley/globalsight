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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

/**
 * locale pair export
 */
public class LocalePairExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";
    private static List<Long> localeIdList;

    public static File createPropertyFile(String userName)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath()).append(File.separator)
                .append("GlobalSight").append(File.separator).append("config")
                .append(File.separator).append("export").append(File.separator)
                .append("LocalePairs");

        File file = new File(filePath.toString());
        file.mkdirs();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = LOCALEPAIR_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     *  gets locale pair info
     * @param propertyFile
     * @param idArr
     */
    public static void propertiesInputLocalePair(File propertyFile, String idArr)
    {
        localeIdList = new ArrayList<Long>();

        try
        {
            long localePairId = Long.parseLong(idArr);
            LocalePair localePair = ServerProxy.getLocaleManager().getLocalePairById(localePairId);
            StringBuffer buffer = new StringBuffer();
            if (localePair != null)
            {
                buffer.append("##LocalPairs.").append(localePair.getCompanyId()).append(".")
                        .append(localePair.getId()).append(".begin").append(NEW_LINE);
                buffer.append("LocalPairs.").append(localePair.getId()).append(".ID = ")
                        .append(localePair.getId()).append(NEW_LINE);
                buffer.append("LocalPairs.").append(localePair.getId())
                        .append(".SOURCE_LOCALE_ID = ").append(localePair.getSource().getId())
                        .append(NEW_LINE);
                buffer.append("LocalPairs.").append(localePair.getId())
                        .append(".TARGET_LOCALE_ID = ").append(localePair.getTarget().getId())
                        .append(NEW_LINE);
                buffer.append("LocalPairs.").append(localePair.getId()).append(".COMPANY_ID = ")
                        .append(localePair.getCompanyId()).append(NEW_LINE);
                buffer.append("LocalPairs.").append(localePair.getId()).append(".IS_ACTIVE = ")
                        .append(localePair.getIsActive()).append(NEW_LINE);
                buffer.append("##LocalPairs.").append(localePair.getCompanyId()).append(".")
                        .append(localePair.getId()).append(".end").append(NEW_LINE)
                        .append(NEW_LINE);

                writeToFile(propertyFile, buffer.toString().getBytes());

                if (localePair.getSource() != null
                        && !localeIdList.contains(localePair.getSource().getId()))
                {
                    propertiesInputLocale(propertyFile, localePair.getSource(),
                            localePair.getCompanyId());
                    localeIdList.add(localePair.getSource().getId());
                }
                if (localePair.getTarget() != null
                        && !localeIdList.contains(localePair.getTarget().getId()))
                {
                    propertiesInputLocale(propertyFile, localePair.getTarget(),
                            localePair.getCompanyId());
                    localeIdList.add(localePair.getTarget().getId());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  gets locale info
     * @param propertyFile
     * @param locale
     * @param companyId
     */
    private static void propertiesInputLocale(File propertyFile, GlobalSightLocale locale,
            Long companyId)
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("##Locale.").append(companyId).append(".").append(locale.getId())
                .append(".begin").append(NEW_LINE);
        buffer.append("Locale.").append(locale.getId()).append(".ID = ").append(locale.getId())
                .append(NEW_LINE);
        buffer.append("Locale.").append(locale.getId()).append(".ISO_LANG_CODE = ")
                .append(locale.getLanguage()).append(NEW_LINE);
        buffer.append("Locale.").append(locale.getId()).append(".ISO_COUNTRY_CODE = ")
                .append(locale.getCountry()).append(NEW_LINE);
        buffer.append("Locale.").append(locale.getId()).append(".IS_UI_LOCALE = ")
                .append(locale.isIsUiLocale()).append(NEW_LINE);
        buffer.append("##Locale.").append(companyId).append(".").append(locale.getId())
                .append(".end").append(NEW_LINE).append(NEW_LINE);

        writeToFile(propertyFile, buffer.toString().getBytes());
    }

    /**
     *  Writes locale pair the properties file
     * @param writeInFile
     * @param bytes
     */
    private static void writeToFile(File writeInFile, byte[] bytes)
    {
        writeInFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(writeInFile, true);
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

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

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.java.Termbase;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports Terminology.
 */
public class TermbaseExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Terminology");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = TERMINOLOGY_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets Terminology info.
     */
    public static File exportTermbase(File termbasePropertyFile, String id)
    {
        try
        {
            Termbase termbase = HibernateUtil.get(Termbase.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (termbase != null)
            {
                buffer.append("##Termbase.").append(termbase.getCompany().getId()).append(".")
                        .append(termbase.getId()).append(".begin").append(NEW_LINE);
                buffer.append("Termbase.").append(termbase.getId()).append(".TB_ID = ")
                        .append(termbase.getId()).append(NEW_LINE);
                buffer.append("Termbase.").append(termbase.getId()).append(".TB_NAME = ")
                        .append(termbase.getName()).append(NEW_LINE);
                buffer.append("Termbase.").append(termbase.getId()).append(".TB_DESCRIPTION = ")
                        .append(termbase.getDescription()).append(NEW_LINE);
                buffer.append("Termbase.").append(termbase.getId()).append(".TB_DEFINITION = ")
                        .append(termbase.getDefination()).append(NEW_LINE);
                buffer.append("Termbase.").append(termbase.getId()).append(".COMPANYID = ")
                        .append(termbase.getCompany().getId()).append(NEW_LINE);
                buffer.append("##Termbase.").append(termbase.getCompany().getId()).append(".")
                        .append(termbase.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(termbasePropertyFile, buffer.toString().getBytes());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return termbasePropertyFile;
    }

    private static void writeToFile(File termbasePropertyFile, byte[] bytes)
    {
        termbasePropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(termbasePropertyFile, true);
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

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

import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports system perplexity service info to property.
 */
public class PerplexityServiceExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("PerplexityService");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = PERPLEXITY_SERVICE + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Puts perplexity service info to property.
     */
    public static File propertiesInputPS(File psPropertyFile, String id)
    {
        try
        {
            PerplexityService ps = HibernateUtil.get(PerplexityService.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            buffer.append("##PerplexityService.").append(ps.getCompanyId()).append(".")
                    .append(ps.getId()).append(".begin").append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".ID=").append(ps.getId())
                    .append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".NAME=")
                    .append(ps.getName()).append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".COMPANY_ID=")
                    .append(ps.getCompanyId()).append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".USER_NAME=")
                    .append(ps.getUserName()).append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".PASSWORD=")
                    .append(ps.getPassword()).append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".DESCRIPTION=")
                    .append(ps.getDescription()).append(NEW_LINE);
            buffer.append("PerplexityService.").append(ps.getId()).append(".URL=")
                    .append(ps.getUrl()).append(NEW_LINE);
            buffer.append("##PerplexityService.").append(ps.getCompanyId()).append(".")
                    .append(ps.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);
            writeToFile(psPropertyFile, buffer.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return psPropertyFile;
    }

    private static void writeToFile(File psPropertyFile, byte[] bytes)
    {
        psPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(psPropertyFile, true);
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

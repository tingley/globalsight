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

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports attribute groups.
 */
public class AttributeSetExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("AttributeGroups");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = ATTRIBUTE_SET_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets attribute groups info.
     */
    public static File exportAttributeSet(File attrSetPropertyFile, String id)
    {
        try
        {
            AttributeSet attSet = HibernateUtil.get(AttributeSet.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (attSet != null)
            {
                buffer.append("##AttributeGroup.").append(attSet.getCompanyId()).append(".")
                        .append(attSet.getId()).append(".begin").append(NEW_LINE);
                buffer.append("AttributeGroup.").append(attSet.getId()).append(".ID = ")
                        .append(attSet.getId()).append(NEW_LINE);
                buffer.append("AttributeGroup.").append(attSet.getId()).append(".NAME = ")
                        .append(attSet.getName()).append(NEW_LINE);
                buffer.append("AttributeGroup.").append(attSet.getId()).append(".DESCRIPTION = ")
                        .append(attSet.getDescription()).append(NEW_LINE);
                buffer.append("AttributeGroup.").append(attSet.getId()).append(".COMPANY_ID = ")
                        .append(attSet.getCompanyId()).append(NEW_LINE);
                List<Attribute> attributeList = attSet.getAttributeAsList();
                StringBuffer sb = new StringBuffer();
                for (Attribute attribute : attributeList)
                {
                    sb.append(attribute.getName()).append(",");
                }
                if (sb.length() > 1)
                    sb.deleteCharAt(sb.length() - 1);
                buffer.append("AttributeGroup.").append(attSet.getId())
                        .append(".ATTRIBUTE_NAMES = ").append(sb).append(NEW_LINE);
                buffer.append("##AttributeGroup.").append(attSet.getCompanyId()).append(".")
                        .append(attSet.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(attrSetPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return attrSetPropertyFile;
    }

    private static void writeToFile(File attrSetPropertyFile, byte[] bytes)
    {
        attrSetPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(attrSetPropertyFile, true);
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

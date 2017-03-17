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
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports attributes.
 */
public class AttributeExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Attributes");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = ATTRIBUTE_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets attributes info.
     */
    public static File exportAttribute(File attrPropertyFile, String id)
    {
        try
        {
            Attribute att = HibernateUtil.get(Attribute.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (att != null)
            {
                buffer.append("##Attribute.").append(att.getCompanyId()).append(".")
                        .append(att.getId()).append(".begin").append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".ID = ").append(att.getId())
                        .append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".NAME = ")
                        .append(att.getName()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".DISPLAY_NAME = ")
                        .append(att.getDisplayName()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".DESCRIPTION = ")
                        .append(att.getDescription()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".COMPANY_ID = ")
                        .append(att.getCompanyId()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".VISIBLE = ")
                        .append(att.isVisible()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".EDITABLE = ")
                        .append(att.getEditable()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".REQUIRED = ")
                        .append(att.isRequired()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".CONDITION_ID = ")
                        .append(att.getCondition().getId()).append(NEW_LINE);
                buffer.append("Attribute.").append(att.getId()).append(".TYPE = ")
                        .append(att.getType()).append(NEW_LINE);

                Condition condition = att.getCondition();
                if (condition instanceof ListCondition)
                {
                    ListCondition listCondition = (ListCondition) condition;
                    List<String> options = listCondition.getOptions();
                    StringBuffer sb = new StringBuffer();
                    for (String option : options)
                    {
                        sb.append(option).append(",");
                    }
                    if (sb.length() > 1)
                        sb.deleteCharAt(sb.length() - 1);
                    buffer.append("Attribute.").append(att.getId()).append(".LISTOPTIONS = ")
                            .append(sb).append(NEW_LINE);
                    buffer.append("Attribute.").append(att.getId()).append(".LISTMULTIPLE = ")
                            .append(listCondition.isMultiple()).append(NEW_LINE);
                }
                else if (condition instanceof IntCondition)
                {
                    IntCondition intCondition = (IntCondition) condition;
                    Integer maxValue = intCondition.getMax();
                    Integer minValue = intCondition.getMin();
                    buffer.append("Attribute.").append(att.getId()).append(".INTMAX = ")
                            .append(maxValue).append(NEW_LINE);
                    buffer.append("Attribute.").append(att.getId()).append(".INTMIN = ")
                            .append(minValue).append(NEW_LINE);
                }
                else if (condition instanceof FloatCondition)
                {
                    FloatCondition floatCondition = (FloatCondition) condition;
                    Float maxValue = floatCondition.getMax();
                    Float minValue = floatCondition.getMin();
                    buffer.append("Attribute.").append(att.getId()).append(".FLOATMAX = ")
                            .append(maxValue).append(NEW_LINE);
                    buffer.append("Attribute.").append(att.getId()).append(".FLOATMIN = ")
                            .append(minValue).append(NEW_LINE);
                    buffer.append("Attribute.").append(att.getId()).append(".FLOATDEFINITION = ")
                            .append(floatCondition.getDefinition()).append(NEW_LINE);
                }
                else if (condition instanceof TextCondition)
                {
                    TextCondition textCondition = (TextCondition) condition;

                    Integer maxLength = textCondition.getLength();
                    buffer.append("Attribute.").append(att.getId()).append(".TEXTLENGTH = ")
                            .append(maxLength).append(NEW_LINE);
                }
                buffer.append("##Attribute.").append(att.getCompanyId()).append(".")
                        .append(att.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);
                writeToFile(attrPropertyFile, buffer.toString().getBytes());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return attrPropertyFile;
    }

    private static void writeToFile(File attrPropertyFile, byte[] bytes)
    {
        attrPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(attrPropertyFile, true);
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

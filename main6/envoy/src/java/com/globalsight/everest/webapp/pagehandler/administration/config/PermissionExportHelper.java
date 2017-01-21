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

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports permission groups.
 *
 */
public class PermissionExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("PermissionGroups");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = PERMISSION_GROUP_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets permission group info.
     */
    public static File exportPermissions(File permPropertyFile, String p_permId)
    {
        try
        {
            PermissionGroup permGroup = Permission.getPermissionManager().readPermissionGroup(
                    Long.parseLong(p_permId));
            StringBuffer buffer = new StringBuffer();
            if (permGroup != null)
            {
                buffer.append("##PermissionGroup.").append(permGroup.getCompanyId()).append(".")
                        .append(permGroup.getId()).append(".begin").append(NEW_LINE);
                buffer.append("PermissionGroup.").append(permGroup.getId()).append(".ID = ")
                        .append(permGroup.getId()).append(NEW_LINE);
                buffer.append("PermissionGroup.").append(permGroup.getId()).append(".NAME = ")
                        .append(permGroup.getName()).append(NEW_LINE);
                buffer.append("PermissionGroup.").append(permGroup.getId())
                        .append(".COMPANY_ID = ").append(permGroup.getCompanyId()).append(NEW_LINE);
                buffer.append("PermissionGroup.").append(permGroup.getId())
                        .append(".DESCRIPTION = ").append(permGroup.getDescription())
                        .append(NEW_LINE);
                buffer.append("PermissionGroup.").append(permGroup.getId())
                        .append(".PERMISSION_SET = ").append(permGroup.getPermissionSet())
                        .append(NEW_LINE);
                buffer.append("##PermissionGroup.").append(permGroup.getCompanyId()).append(".")
                        .append(permGroup.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(permPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return permPropertyFile;
    }

    private static void writeToFile(File permPropertyFile, byte[] bytes)
    {
        permPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(permPropertyFile, true);
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

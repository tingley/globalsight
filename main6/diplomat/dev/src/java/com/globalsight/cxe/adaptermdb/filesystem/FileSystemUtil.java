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

package com.globalsight.cxe.adaptermdb.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Vector;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.webservices.WebServiceException;

public class FileSystemUtil
{
    private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
    .getLogger("FileSystemUtil");

    
    /**
     * <p>
     * Calls script.
     * <p>
     * The size of p_files, p_fps, p_locales must be same, others will throw
     * exception.
     * 
     * @param p_files
     *            Vector<File>
     * @param p_fps
     *            Vector<FileProfile>
     * @param p_locales
     *            Vector<String>
     * @return result Vector, include Vector<File> files, Vector<FileProfile>
     *         fileProfiles, Vector<String> locales, Vector<Integer>
     *         exitValueser.
     * @throws WebServiceException
     */
    public static Vector execScript(Vector p_files, Vector p_fps, Vector p_locales)
    {
        if (p_files.size() != p_fps.size())
        {
            throw new IllegalArgumentException("The size of files is "
                    + p_files.size() + "but the file profile size is "
                    + p_fps.size());
        }

        s_logger.info("Begin call script");

        Vector files = new Vector();
        Vector fileProfiles = new Vector();
        Vector locales = new Vector();
        Vector exitValues = new Vector();

        for (int i = 0; i < p_files.size(); i++)
        {
            File file = (File) p_files.get(i);
            FileProfile fp = (FileProfile) p_fps.get(i);
            String scriptOnImport = fp.getScriptOnImport();
            int exitValue = 0;

            if (scriptOnImport != null && scriptOnImport.length() > 0)
            {
                String path = file.getPath();
                int index = AmbFileStoragePathUtils.getCxeDocDirPath().length() + 1;
                String fileName = path.substring(index);
                String scriptedDir = fileName.substring(0, fileName
                        .lastIndexOf("."));
                String scriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath()
                        + File.separator + scriptedDir;
                File scriptedFolder = new File(scriptedFolderPath);

                if (!scriptedFolder.exists())
                {
                    String filePath = file.getParent();
                    // Call the script on import to convert the file
                    try
                    {
                        String cmd = "cmd.exe /c " + scriptOnImport + " \""
                                + filePath + "\"";
                        // If the script is Lexmark tool, another parameter
                        // -encoding is passed.
                        if ("lexmarktool.bat".equalsIgnoreCase(new File(
                                scriptOnImport).getName()))
                        {
                            cmd += " \"-encoding " + fp.getCodeSet() + "\"";
                        }
                        Process process = Runtime.getRuntime().exec(cmd);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String line = "";
                        while ((line = reader.readLine()) != null)
                        {
                            // just read the output.
                        }

                        BufferedReader error_reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));
                        String error_line = "";
                        while ((error_line = error_reader.readLine()) != null)
                        {
                            // just read the output.
                        }
                        s_logger.info("Script on Import " + scriptOnImport
                                + " was called: \n");
                        exitValue = process.exitValue();
                    }
                    catch (Exception e)
                    {
                        // Set exitValue to 1 if the file was not scripted
                        // correctly.
                        exitValue = 1;
                        s_logger
                                .error("The script on import was not executed successfully.");
                    }
                }

                if (scriptedFolder.exists())
                {
                    File[] scriptedFiles = scriptedFolder.listFiles();
                    if (scriptedFiles != null && scriptedFiles.length > 0)
                    {
                        for (int j = 0; j < scriptedFiles.length; j++)
                        {
                            File file1 = scriptedFiles[j];
                            files.add(file1);
                            fileProfiles.add(fp);
                            locales.add(p_locales.get(i));
                            exitValues.add(new Integer(exitValue));
                        }
                    }
                    else
                    // there are no scripted files in the folder
                    {
                        files.add(file);
                        fileProfiles.add(fp);
                        locales.add(p_locales.get(i));
                        exitValues.add(new Integer(exitValue));
                    }
                }
                else
                // the corresponding folder was not created by the
                // script.
                {
                    files.add(file);
                    fileProfiles.add(fp);
                    locales.add(p_locales.get(i));
                    exitValues.add(new Integer(exitValue));
                }
            }
            else
            {
                files.add(file);
                fileProfiles.add(fp);
                locales.add(p_locales.get(i));
                exitValues.add(new Integer(exitValue));
            }
        }

        Vector result = new Vector();
        result.add(files);
        result.add(fileProfiles);
        result.add(locales);
        result.add(exitValues);

        return result;
    }

}

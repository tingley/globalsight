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

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.ProcessRunner;
import com.globalsight.webservices.WebServiceException;

public class FileSystemUtil
{
    private static final Logger s_logger = Logger
            .getLogger(FileSystemUtil.class);

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
    public static Vector execScript(Vector p_files, Vector p_fps,
            Vector p_locales, long p_jobId, String p_jobName)
    {
        if (p_files.size() != p_fps.size())
        {
            throw new IllegalArgumentException("The size of files is "
                    + p_files.size() + "but the file profile size is "
                    + p_fps.size());
        }

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
                int index = AmbFileStoragePathUtils.getCxeDocDirPath(
                        fp.getCompanyId()).length() + 1;
                String fileName = path.substring(index);
                
                String oldScriptedDir = fileName.substring(0,
                        fileName.lastIndexOf("."));
                String oldScriptedFolderPath = AmbFileStoragePathUtils
		                .getCxeDocDirPath(fp.getCompanyId())
		                + File.separator
		                + oldScriptedDir;
                File oldScriptedFolder = new File(oldScriptedFolderPath);
                
                String scriptedFolderNamePrefix= getScriptedFolderNamePrefixByJob(p_jobId);
                String name = fileName.substring(fileName
                		.lastIndexOf(File.separator) + 1,fileName.lastIndexOf("."));
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                
                String scriptedDir = fileName.substring(0,fileName.lastIndexOf(File.separator))
		        		+ File.separator + scriptedFolderNamePrefix + "_" 
		        		+ name + "_" + extension;
		        String scriptedFolderPath = AmbFileStoragePathUtils
		                .getCxeDocDirPath(fp.getCompanyId())
		                + File.separator
		                + scriptedDir;
                File scriptedFolder = new File(scriptedFolderPath); 
                if (!scriptedFolder.exists())
                {
	                String filePath = file.getParent();
	                // Call the script on import to convert the file
	                try
	                {
	                    String cmd = "cmd.exe /c " + scriptOnImport + " \""
	                    + filePath + "\" \"" + scriptedFolderNamePrefix + "\"";
	                    // If the script is Lexmark tool, another parameter -encoding is passed.
	            		if ("lexmarktool.bat".equalsIgnoreCase(new File(
								scriptOnImport).getName()))
						{
							cmd += " \"-encoding " + fp.getCodeSet() + "\"";
						}
	                    ProcessRunner pr = new ProcessRunner(cmd);
	                    Thread t = new Thread(pr);
	                    t.start();
	                    try
	                    {
	                        t.join();
	                    }
	                    catch (InterruptedException ie)
	                    {
	                    }
	                    s_logger.info("Script on Import " + scriptOnImport
	                            + " is called to handle " + filePath);
	                }
	                catch (Exception e)
	                {
	                    // Set exitValue to 1 if the file was not scripted
	                    // correctly.
	                    exitValue = 1;
	                    s_logger.error("The script on import was not executed successfully.");
	                }
                }
                

                if (scriptedFolder.exists() || oldScriptedFolder.exists())
                {
                    File[] scriptedFiles;
                    if(scriptedFolder.exists())
                    {
                    	scriptedFiles = scriptedFolder.listFiles();
                    }
                    else
                    {
                    	scriptedFiles = oldScriptedFolder.listFiles();
                    }
                    if (scriptedFiles != null && scriptedFiles.length > 0)
                    {
                        for (int j = 0; j < scriptedFiles.length; j++)
                        {
                            File file1 = scriptedFiles[j];
                            if(!file.getName().equals(file1.getName()))
                            {
                            	continue;
                            }
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

    /**
     * Determine the folder name(suffix) to store "scripted" files.
     * <p>
     * The folder name is like "PreProcessed_[jobID]_[fileName]_[extension]",
     * i.e. "a_txt_PreProcessed_10_625229266";
     * </p>
     * 
     * @param jobId
     * @return
     */
    public static String getScriptedFolderNamePrefixByJob(long jobId)
    {
    	return "PreProcessed_" + jobId;
    }
}

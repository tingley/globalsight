package com.globalsight.cxe.entity.fileprofile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.ProcessRunner;

public class FileProfileUtil
{
    static private final Logger logger = Logger
            .getLogger(FileProfileUtil.class);

    public static Map<String, long[]> excuteScriptOfFileProfile(
            List<String> descList, List<FileProfile> fileProfileList, Job p_job)
    {
        Map<String, long[]> filesToFpId = new HashMap<String, long[]>();

        for (int i = 0; i < descList.size(); i++)
        {
            String fileName = descList.get(i);
            FileProfile fp = fileProfileList.get(i);

            String scriptOnImport = fp.getScriptOnImport();
            long exitValue = 0;
            if (StringUtils.isNotEmpty(scriptOnImport))
            {
                String oldScriptedDir = fileName.substring(0,
                        fileName.lastIndexOf("."));
                String oldScriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath(fp.getCompanyId()) + File.separator
                        + oldScriptedDir;
                File oldScriptedFolder = new File(oldScriptedFolderPath);

                String scriptedFolderNamePrefix = FileSystemUtil
                        .getScriptedFolderNamePrefixByJob(p_job.getId());
                String name = fileName.substring(
                        fileName.lastIndexOf(File.separator) + 1,
                        fileName.lastIndexOf("."));
                String extension = fileName
                        .substring(fileName.lastIndexOf(".") + 1);

                String scriptedDir = fileName.substring(0,
                        fileName.lastIndexOf(File.separator)) + File.separator
                        + scriptedFolderNamePrefix + "_" + name + "_"
                        + extension;
                String scriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath(fp.getCompanyId()) + File.separator
                        + scriptedDir;
                File scriptedFolder = new File(scriptedFolderPath);
                if (!scriptedFolder.exists())
                {
                    File file = new File(fileName);
                    String filePath = AmbFileStoragePathUtils
                            .getCxeDocDirPath(fp.getCompanyId())
                            + File.separator + file.getParent();
                    // Call the script on import to convert the file
                    try
                    {
                        String cmd = "cmd.exe /c " + scriptOnImport + " \""
                                + filePath + "\" \"" + scriptedFolderNamePrefix
                                + "\"";
                        // If the script is Lexmark tool, another parameter
                        // -encoding is passed.
                        if ("lexmarktool.bat".equalsIgnoreCase(
                                new File(scriptOnImport).getName()))
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
                        logger.info("Script on Import " + scriptOnImport
                                + " is called to handle " + filePath);
                    }
                    catch (Exception e)
                    {
                        exitValue = 1;
                        logger.error(
                                "The script on import was not executed successfully.");
                    }
                }

                // Iterator the files converted by the script and import
                // each one of them.
                if (scriptedFolder.exists() || oldScriptedFolder.exists())
                {
                    String scriptedFiles[];
                    if (scriptedFolder.exists())
                    {
                        scriptedFiles = scriptedFolder.list();
                    }
                    else
                    {
                        scriptedFiles = oldScriptedFolder.list();
                    }
                    if (scriptedFiles != null && scriptedFiles.length > 0)
                    {
                        for (int j = 0; j < scriptedFiles.length; j++)
                        {
                            String scriptedFileName = scriptedFiles[j];
                            String oldName = fileName.substring(
                                    fileName.lastIndexOf(File.separator) + 1);
                            if (!oldName.equals(scriptedFileName))
                            {
                                continue;
                            }
                            long fileProfileId = fp.getId();
                            String key_fileName;
                            if (scriptedFolder.exists())
                            {
                                key_fileName = scriptedDir + File.separator
                                        + scriptedFileName;
                            }
                            else
                            {
                                key_fileName = oldScriptedDir + File.separator
                                        + scriptedFileName;
                            }
                            filesToFpId.put(key_fileName,
                                    new long[] { fileProfileId, exitValue });
                        }
                    }
                    else
                    // there are no scripted files in the folder
                    {
                        filesToFpId.put(fileName,
                                new long[] { fp.getId(), exitValue });
                    }
                }
                else
                // the corresponding folder was not created by the script.
                {
                    filesToFpId.put(fileName,
                            new long[] { fp.getId(), exitValue });
                }
            }
            else
            {
                filesToFpId.put(fileName, new long[] { fp.getId(), exitValue });
            }
        }

        return filesToFpId;
    }
    
    public static boolean isXmlPreviewPDF(FileProfile fp) throws Exception
    {
        File xlsFile = getXsl(fp);
        if (xlsFile == null || !xlsFile.exists())
        {
            return false;
        }
        
        String content = FileUtil.readFile(xlsFile, "UTF-8");
        if (content.contains("<fo:root"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static File getXsl(FileProfile fp)
    {
        if (fp == null)
        {
            return null;
        }
        
        File xslFile = null;

        StringBuffer xslPath = new StringBuffer(
                AmbFileStoragePathUtils.getXslDir(fp.getId()).getPath())
                        .append("/").append(fp.getId()).append("/");
        File xslParent = new File(xslPath.toString());
        if (xslParent.exists())
        {
            File[] files = xslParent.listFiles();
            if (files.length > 0)
            {
                String fileName = files[0].getName();
                if (fileName.toLowerCase().endsWith("xsl")
                        || fileName.toLowerCase().endsWith("xml")
                        || fileName.toLowerCase().endsWith("xslt"))
                {
                    xslFile = files[0];
                }
            }

        }

        return xslFile;
    }
}

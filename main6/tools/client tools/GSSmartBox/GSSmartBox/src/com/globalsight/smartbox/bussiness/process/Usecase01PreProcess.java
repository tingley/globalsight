/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bussiness.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FileProfile;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;
import com.globalsight.smartbox.util.ZipUtil;

/**
 * Use case 01 pre process
 * 
 * @author leon
 * 
 */
public class Usecase01PreProcess implements PreProcess
{
    private JobInfo jobInfo = new JobInfo();

    @Override
    public JobInfo process(String originFilePath, CompanyConfiguration cpConfig)
    {
        jobInfo.setOriginFile(originFilePath);
        File originFile = new File(originFilePath);
        String fileName = originFile.getName();

        // Validate file type(zip file)
        if (!fileName.endsWith(".zip"))
        {
            LogUtil.FAILEDLOG.error("File format error, must be zip file: "
                    + fileName);
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }

        // temp directory used for saving converted file
        String tempDirPath = originFilePath.substring(0,
                originFilePath.indexOf(".zip"));
        File tempDir = new File(tempDirPath);
        tempDir.mkdir();
        jobInfo.setTempFile(tempDirPath);

        // handing original file
        Vector<String> sourceFiles = fileHanding(originFile);
        if (sourceFiles == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }

        String originFileName = originFile.getName();
        String jobName = determineJobName(originFileName);
        if (jobName == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }
        String targetLocale = determineTargetLocale(originFileName);
        String sourceLocale = determineSourceLocale(originFileName);
        Vector<String> fileProfileIds = determineFileProfileIds(sourceFiles,
                sourceLocale, cpConfig.getExtension2fp());
        if (fileProfileIds == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }
        Vector<String> tls = new Vector<String>();
        for (int i = 0; i < sourceFiles.size(); i++)
        {
            tls.add(targetLocale);
        }

        jobInfo.setJobName(jobName);
        jobInfo.setSourceFiles(sourceFiles);
        jobInfo.setTargetLocales(tls);
        jobInfo.setFileProfileIds(fileProfileIds);
        jobInfo.setOtherInfo("infomation");

        return jobInfo;
    }

    /**
     * Use fileName as jobName
     * 
     * @param originFileName
     * @return
     */
    private String determineJobName(String originFileName)
    {
        String jobName = originFileName.substring(0,
                originFileName.lastIndexOf("."));
        String uniqueJobName = null;
        try
        {
            uniqueJobName = WebClientHelper.getUniqueJobName(jobName);
        }
        catch (Exception e)
        {
            String message = "Get unique job name failed. Web Service Exception.";
            LogUtil.fail(message, e);
        }
        return uniqueJobName;
    }

    /**
     * Get target locale
     * 
     * @param originFileName
     * @return
     */
    private String determineTargetLocale(String originFileName)
    {
        String[] str = originFileName.split("_");
        String originTargetLocale = str[1];
        String targetLocale = getLocale(originTargetLocale);
        return targetLocale;
    }

    /**
     * Determine file profile ids
     * 
     * @param sourceFiles
     * @param sourceLocale
     * @return
     */
    private Vector<String> determineFileProfileIds(Vector<String> sourceFiles,
            String sourceLocale, Map<String, String> extension2fp)
    {
        Vector<String> fpIds = new Vector<String>();
        // Get file profile info from GS
        List<FileProfile> fileProfileInfo;
        try
        {
            fileProfileInfo = WebClientHelper.getFileProfileInfoFromGS();
        }
        catch (Exception e)
        {
            String message = "Get file profile info failed, Web Service Exception.";
            LogUtil.fail(message, e);
            return null;
        }

        for (String sf : sourceFiles)
        {
            String extension = sf.substring(sf.lastIndexOf(".") + 1);
            extension = extension.toLowerCase();
            String mapFPName = extension2fp.get(extension);
            if (mapFPName == null)
            {
                String message = "No file profile config for this extension(."
                        + extension + ") in GSSmartBox.conf : " + sf;
                LogUtil.FAILEDLOG.error(message);
                return null;
            }
            boolean findNoFileProfile = true;
            for (FileProfile fp : fileProfileInfo)
            {
                if (fp.getName().equals(mapFPName))
                {
                    String sl = fp.getSourceLocale();
                    Set<String> extensions = fp.getFileExtensions();
                    if (!sourceLocale.equals(sl))
                    {
                        String message = "The file profile(" + mapFPName
                                + ") does not have the source locale("
                                + sourceLocale + ") in GlobalSight Server: "
                                + sf;
                        LogUtil.FAILEDLOG.error(message);
                        return null;
                    }
                    if (!extensions.contains(extension))
                    {
                        String message = "The file profile(" + mapFPName
                                + ") does not have the file extension(."
                                + extension + ") in GlobalSight Server: " + sf;
                        LogUtil.FAILEDLOG.error(message);
                        return null;
                    }
                    fpIds.add(fp.getId());
                    findNoFileProfile = false;
                    break;
                }
            }
            if (findNoFileProfile)
            {
                String message = "No file profile found in GlobalSight Server for ."
                        + extension + " format: " + sf;
                LogUtil.FAILEDLOG.error(message);
                return null;
            }
        }
        return fpIds;
    }

    /**
     * Get source locale
     * 
     * @param originFileName
     * @return
     */
    private String determineSourceLocale(String originFileName)
    {
        String[] str = originFileName.split("_");
        String originSourceLocale = str[0];
        String sourceLocale = getLocale(originSourceLocale);
        return sourceLocale;
    }

    /**
     * Get locale of GS, en-us -> en_US
     * 
     * @param originLocale
     * @return
     */
    private String getLocale(String originLocale)
    {
        String[] str = originLocale.split("-");
        String localeCode = str[0];
        String country = str[1];
        String gsLocale = localeCode + "_" + country.toUpperCase();
        return gsLocale;
    }

    /**
     * File Handing and Validate
     * 
     * @param originFilePath
     */
    private Vector<String> fileHanding(File originFile)
    {
        String fileName = originFile.getName();
        String tempDir = jobInfo.getTempFile();

        // Unpack zip file
        LogUtil.info("Unpack zip file: " + fileName);
        List<String> fileList = new ArrayList<String>();
        try
        {
            fileList = ZipUtil.unpackZipPackage(originFile.getPath(), tempDir);
        }
        catch (Exception e)
        {
            String message = "File unpack error: " + fileName;
            LogUtil.fail(message, e);
            return null;
        }
        // Analysis XML file to check file missing
        LogUtil.info("Validate file missing: " + fileName);
        String packageDetailFileName = fileName.substring(0,
                fileName.indexOf(".zip"))
                + ".xml";
        String packageDetailFilePath = tempDir + File.separator
                + packageDetailFileName;
        if (fileMissingCheck(packageDetailFilePath, fileList))
        {
            return null;
        }
        Vector<String> sourceFiles = new Vector<String>();
        for (String str : fileList)
        {
            if (!str.equals(packageDetailFileName))
            {
                sourceFiles.add(tempDir + File.separator + str);
            }
        }
        return sourceFiles;
    }

    /**
     * Read XML file of package detail, check file missing
     * 
     * @param packageDetailFilePath
     * @return
     */
    private boolean fileMissingCheck(String packageDetailFilePath,
            List<String> fileList)
    {
        File packageDetailFile = new File(packageDetailFilePath);
        SAXReader saxReader = new SAXReader();
        Document content = null;
        try
        {
            content = saxReader.read(packageDetailFile);
        }
        catch (Exception e)
        {
            String message = "XMl file of package detail read error: "
                    + packageDetailFile.getName();
            LogUtil.fail(message, e);
            return true;
        }
        List<Element> profileList = content
                .selectNodes("/package/filelist/file");
        for (Element node : profileList)
        {
            String fileName = node.selectSingleNode("filename").getText();
            if (!fileList.contains(fileName))
            {
                String message = "Validate error, file missing: " + fileName;
                LogUtil.FAILEDLOG.error(message);
                return true;
            }
        }
        return false;
    }
}

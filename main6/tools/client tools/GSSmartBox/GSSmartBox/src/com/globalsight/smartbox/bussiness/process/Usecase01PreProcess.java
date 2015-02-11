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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Special prefix/uploading process for "Use case 01"
 * The job info(jobName, fpName, targetLocales) comes from ZIP file.
 * 
 * @author Leon
 * @author Joey         2013-01-22
 * 
 */
public class Usecase01PreProcess implements PreProcess
{
    public static final String CONFIG_NAME = "Usecase01.xml";
    
    private JobInfo jobInfo = new JobInfo();
    private String basicJobName;
    private FileProfile fp;
    private FileProfile unExtractedFP;
    private Map<String, FileProfile> gsFPMap;
    private Map<String, FileProfile> configFPMap;
    private String trgLocale;

    @Override
    public JobInfo process(String originFilePath, CompanyConfiguration cpConfig)
    {
        jobInfo.setOriginFile(originFilePath);
        File originFile = new File(originFilePath);
        String fileName = originFile.getName();

        // Validate file type(zip file)
        if (!fileName.endsWith(".zip"))
        {
            LogUtil.FAILEDLOG.error("File format error, must be zip file: " + fileName);
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }

        // Temp directory used for saving converted file
        String tempDirPath = originFilePath.substring(0, originFilePath.lastIndexOf(".zip"));
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();
        jobInfo.setTempFile(tempDirPath);

        // Handing original file
        Vector<String> sourceFiles = fileHanding(originFile);
        if (sourceFiles == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }

        // Gets GS Info
        getGSInfo();
        // Gets some data from source file.
        parseFile(sourceFiles);
        
        String jobName = determineJobName(basicJobName);
        if (jobName == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }
        Vector<String> fileProfileIds = determineFileProfileIds(sourceFiles, fileName);
        if (fileProfileIds == null)
        {
            jobInfo.setFailedFlag(true);
            return jobInfo;
        }
        Vector<String> tls = new Vector<String>();
        for (int i = 0; i < sourceFiles.size(); i++)
        {
            tls.add(trgLocale);
        }

        jobInfo.setJobName(jobName);
        jobInfo.setSourceFiles(sourceFiles);
        jobInfo.setTargetLocales(tls);
        jobInfo.setFileProfileIds(fileProfileIds);
        jobInfo.setOtherInfo("infomation");

        return jobInfo;
    }

    /*
     * Get GlobalSight Info for Creating Job.
     */
    private void getGSInfo()
    {
        //1. Get GlobalSight File Profile Map<FPName, FP>.
        gsFPMap = new HashMap<String, FileProfile>();
        try
        {
            List<FileProfile> fps = WebClientHelper.getFileProfileInfoFromGS();
            for(FileProfile fp : fps)
            {
                gsFPMap.put(fp.getName(), fp);
            }
        }
        catch (Exception e)
        {
            String message = "Get file profile info failed, Web Service Exception.";
            LogUtil.fail(message, e);
            return;
        }
        
        //2. Get Alias File Profile Name Map<AliasFPName, GSFPName>
        try
        {
            SAXReader saxReader = new SAXReader();
            String path = System.getProperty("user.dir") + File.separator + CONFIG_NAME;
            Document doc = saxReader.read(path);
            List<Element> nodes =  doc.selectNodes("//fileProfileNameMappings/fileProfileNameMapping");
            configFPMap = new HashMap<String, FileProfile>();
            for(Element el : nodes)
            {
                String gsFPName = el.attributeValue("gs_xml");
                String aliasFPName = el.attributeValue("aliasFPName");
                String gsUnExtractedFPName = el.attributeValue("gs_unextracted");
                FileProfile fp = new FileProfile(gsFPName, aliasFPName, gsUnExtractedFPName);
                configFPMap.put(aliasFPName, fp);
            }
        }
        catch (Exception e)
        {
            String message = "Parse " + CONFIG_NAME + " Error.";
            LogUtil.fail(message, e);
            return;
        }
    }

    // Parse the source files for getting some useful data.
    private void parseFile(Vector<String> p_sourceFiles)
    {
        // Parse Bookmark XML File to get fileProfileName and job name.
        Map<String, String> srcMap = new HashMap<String, String>();
        String fileName = null;
        for (String path : p_sourceFiles)
        {
            String temp = path.substring(path.lastIndexOf(File.separator) + 1);
            srcMap.put(temp, path);
            if (temp.endsWith(".pdf"))
            {
                fileName = temp.replace(".pdf", ".xml");
            }
        }
        String path = srcMap.get(fileName);
        try
        {
            SAXReader saxReader = new SAXReader();
            saxReader.setValidation(false);
            saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document doc = saxReader.read(path);
            Element node = (Element) doc.selectSingleNode("//bookmeta/data[@datatype='GSDATA']");
            String gsDataValue = node.attributeValue("value");
            String jobName = gsDataValue.substring(0, gsDataValue.indexOf("~"));
            String customerFPName = gsDataValue.substring(gsDataValue.indexOf("~") + 1);
            FileProfile configFP = getConfigFP(customerFPName);
            if (configFP == null)
            {
                String message = "Can't find the fileProfileNameMapping for." + customerFPName;
                LogUtil.info(message);
                return ;
            }
            fp = gsFPMap.get(configFP.getName());
            if (fp == null)
            {
                String message = "Can't find the file profile.";
                LogUtil.info(message);
                return ;
            }
            
            // Get Target Locale.
            node = (Element) doc.selectSingleNode("/bookmap");
            String lang = node.attributeValue("lang");
            for (String locale : fp.getTargetLocale())
            {
                if (locale.startsWith(lang))
                {
                    trgLocale = locale;
                    break;
                }
            }
            if (trgLocale == null || trgLocale.trim().length() == 0)
            {
                String message = "Can't find the correct Target Locale in File Profile.";
                LogUtil.info(message);
                return ;
            }
            
            unExtractedFP = gsFPMap.get(configFP.getGsUnExtractedFPName());
            if (unExtractedFP == null)
            {
                String message = "Can't find the UnExtracted file profile: "
                        + fp.getGsUnExtractedFPName();
                LogUtil.info(message);
                return ;
            }
            
            basicJobName = jobName + "_" + customerFPName + "_" + lang;
        }
        catch (Exception e)
        {
            String message = "Read XML error: " + path;
            LogUtil.fail(message, e);
            return ;
        }
    }

    /**
     * Get the File Profile Name in GlobalSight.
     * 
     * @param p_aliasFPName
     *            File Profile Alias Name
     */
    private FileProfile getConfigFP(String p_aliasFPName)
    {
        for(String regex : configFPMap.keySet())
        {
            if(p_aliasFPName.matches(regex))
                return configFPMap.get(regex);
        }
        
        return null; 
    }
    
    /**
     * Use fileName as jobName
     * 
     * @param originFileName
     * @return
     */
    private String determineJobName(String p_jobName)
    {
        if(p_jobName == null || p_jobName.trim().length() == 0)
            return null;
        
        String uniqueJobName = null;
        try
        {
            uniqueJobName = WebClientHelper.getUniqueJobName(p_jobName);
        }
        catch (Exception e)
        {
            String message = "Get unique job name failed. Web Service Exception.";
            LogUtil.fail(message, e);
        }
        return uniqueJobName;
    }

    /**
     * Determine file profile Ids
     * 
     */
    private Vector<String> determineFileProfileIds(Vector<String> sourceFiles,
            String p_fileName)
    {
        Vector<String> fpIds = new Vector<String>();     
        
        for (String sf : sourceFiles)
        {
            String fileName = sf.toLowerCase();
            if (fileName.endsWith(".pdf") || fileName.endsWith(".log"))
            {
                fpIds.add(unExtractedFP.getId());
            }
            else
            {
                fpIds.add(fp.getId());
            }
        }

        return fpIds;
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
            sourceFiles.add(tempDir + File.separator + str);
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
        List<Element> profileList = content.selectNodes("/package/filelist/file");
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

/**
 *  Copyright 2013 Welocalize, Inc. 
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
 * Special prefix/uploading process for "Use case 03".
 * The job info(jobName, fpName, targetLocales) comes from file name.
 * 
 * @author Joey         2013-08-12
 * 
 */
public class Usecase03PreProcess implements PreProcess
{
    public static final String CONFIG_NAME = "Usecase03.xml";
    public static final String NAME_SEPERATOR = "$$";
    
    private JobInfo jobInfo = new JobInfo();
    private FileProfile fp;                         // Main File Profile
    private FileProfile unExtractedFP;              // Un-extracted File Profile
    private Map<String, FileProfile> serverFPMap;   // Server File Profiles Map
    private Map<String, FileProfile> localeFPMap;   // Locale File Profiles Map

    @Override
    public JobInfo process(String originalFilePath, CompanyConfiguration cpConfig)
    {
        String jobName;                         // Job Name
        String fpName;                          // File Profile Name     
        String trgLocales;                      // Target Locales String
        Vector<String> sourceFiles = new Vector<String>();
        
        jobInfo.setFailedFlag(true);
        jobInfo.setOriginFile(originalFilePath);
        File originalFile = new File(originalFilePath);
        String originalFileName = originalFile.getName();
        if (originalFileName.contains(NAME_SEPERATOR) 
                && originalFileName.contains("(")
                && originalFileName.contains(")"))
        {
            jobName = originalFileName.substring(0, originalFileName.indexOf(NAME_SEPERATOR));
            fpName = originalFileName.substring(originalFileName.indexOf(NAME_SEPERATOR) + NAME_SEPERATOR.length(), originalFileName.lastIndexOf("("));
            trgLocales = originalFileName.substring(originalFileName.lastIndexOf("(") + 1, originalFileName.lastIndexOf(")"));
            trgLocales = trgLocales.replace(" ", "");
            sourceFiles.add(originalFilePath);
        }
        else
        {
            return jobInfo;
        }
        
        // Gets File Profile Info.
        loadFileProfileInfo();
        fp = getFileProfile(fpName);
        if (fp == null)
        {
            String message = "Can't find the file profile.";
            LogUtil.info(message);
            return jobInfo;
        }

        // Special Operation for ZIP File
        if (originalFileName.endsWith(".zip"))
        {
            // Temp directory used for saving converted file
            String tempDirPath = originalFilePath.substring(0, originalFilePath.lastIndexOf(".zip"));
            File tempDir = new File(tempDirPath);
            tempDir.mkdirs();
            jobInfo.setTempFile(tempDirPath);

            // Handling original file
            sourceFiles = fileHanding(originalFile);
            if (sourceFiles == null)
            {
                return jobInfo;
            }
            
            // Get Un-extracted File Profile
            FileProfile localeFP = getLocaleFP(fpName); 
            if (localeFP != null)
            {
                unExtractedFP = serverFPMap.get(localeFP.getGsUnExtractedFPName());
            }
        }        

        jobName = determineJobName(jobName);
        if (jobName == null)
        {
            return jobInfo;
        }
        Vector<String> fileProfileIds = determineFileProfileIds(sourceFiles, originalFileName);
        if (fileProfileIds == null)
        {
            return jobInfo;
        }
        Vector<String> tls = new Vector<String>();
        for (int i = 0; i < sourceFiles.size(); i++)
        {
            tls.add(trgLocales);
        }

        jobInfo.setJobName(jobName);
        jobInfo.setSourceFiles(sourceFiles);
        jobInfo.setTargetLocales(tls);
        jobInfo.setFileProfileIds(fileProfileIds);
        jobInfo.setOtherInfo("infomation");
        jobInfo.setFailedFlag(false);

        return jobInfo;
    }
    
    private FileProfile getFileProfile(String p_fpName){
        FileProfile localeFP = getLocaleFP(p_fpName);
        if(localeFP != null)
        {
            return serverFPMap.get(localeFP.getName());
        }
        
        return serverFPMap.get(p_fpName);
    }

    /*
     * Prepare File Profile Info for Creating Job.
     */
    private void loadFileProfileInfo()
    {
        //1. Get GlobalSight File Profiles from Server.
        serverFPMap = new HashMap<String, FileProfile>();
        try
        {
            List<FileProfile> fps = WebClientHelper.getFileProfileInfoFromGS();
            for(FileProfile fp : fps)
            {
                serverFPMap.put(fp.getName(), fp);
            }
        }
        catch (Exception e)
        {
            String message = "Get file profile info failed, Web Service Exception.";
            LogUtil.fail(message, e);
            return;
        }
        
        //2. Get Locale File Profiles from Local Configure File.
        try
        {
            SAXReader saxReader = new SAXReader();
            String path = System.getProperty("user.dir") + File.separator + CONFIG_NAME;
            Document doc = saxReader.read(path);
            List<Element> nodes =  doc.selectNodes("//fileProfileNameMappings/fileProfileNameMapping");
            localeFPMap = new HashMap<String, FileProfile>();
            for(Element el : nodes)
            {
                String gsFPName = el.attributeValue("gs_xml");
                String aliasFPName = el.attributeValue("aliasFPName");
                String gsUnExtractedFPName = el.attributeValue("gs_unextracted");
                FileProfile fp = new FileProfile(gsFPName, aliasFPName, gsUnExtractedFPName);
                localeFPMap.put(aliasFPName, fp);
            }
        }
        catch (Exception e)
        {
            String message = "Parse " + CONFIG_NAME + " Error.";
            LogUtil.fail(message, e);
            return;
        }
    }

    /**
     * Get the File Profile Name in GlobalSight.
     * 
     * @param p_aliasFPName
     *            File Profile Alias Name
     */
    private FileProfile getLocaleFP(String p_aliasFPName)
    {
        for(String regex : localeFPMap.keySet())
        {
            if(p_aliasFPName.matches(regex))
                return localeFPMap.get(regex);
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
        Set<String> fpExtensions = fp.getFileExtensions();
        
        for (String sf : sourceFiles)
        {
            String fileExtension = sf.substring(sf.lastIndexOf(".") + 1);            
            if (fpExtensions != null && fpExtensions.contains(fileExtension))
            {
                fpIds.add(fp.getId());
            }
            else
            {
                fpIds.add(unExtractedFP.getId());
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
        
        Vector<String> sourceFiles = new Vector<String>();
        for (String str : fileList)
        {
            sourceFiles.add(tempDir + File.separator + str);
        }
        
        return sourceFiles;
    }
}

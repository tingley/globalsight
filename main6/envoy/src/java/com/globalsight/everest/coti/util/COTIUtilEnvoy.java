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

package com.globalsight.everest.coti.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Util class for COTI jobs in Envoy
 * 
 * @author Wayzou
 * 
 */
public class COTIUtilEnvoy
{
    /**
     * Write bytes into file
     * 
     * @param dst
     * @param data
     * @throws IOException
     */
    public static void writeFile(File dst, byte[] data) throws IOException
    {
        File parent = dst.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        FileOutputStream fos = null;
        try
        {
            int len = data.length;
            fos = new FileOutputStream(dst);
            fos.write(data, 0, len);
            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * Get the path to save this project files
     * 
     * @param companyId
     * @param cpackage
     * @param cproject
     * @return
     */
    public static String getProjectDir(long companyId, COTIPackage cpackage,
            COTIProject cproject)
    {
        String path = AmbFileStoragePathUtils.getFileStorageDirPath(companyId);
        path += "/" + COTIConstants.Dir_Root_Name;
        path += "/" + cpackage.getId() + ".unzip";
        path += "/" + cproject.getId();

        return path;
    }

    /**
     * Get the path to save this document
     * 
     * @param companyId
     * @param cpackage
     * @param cproject
     * @param document
     * @return
     */
    public static String getCotiDocumentPath(long companyId,
            COTIPackage cpackage, COTIProject cproject, COTIDocument document)
    {
        // copy coti xml to path
        String fileType = document.getIsTranslation() ? COTIConstants.fileType_translation
                : COTIConstants.fileType_reference;
        String fileRef = document.getFileRef();

        return getCotiDocumentPath(companyId, cpackage, cproject, fileType,
                fileRef);
    }

    /**
     * Get the path to save this document
     * 
     * @param companyId
     * @param cpackage
     * @param cproject
     * @param fileType
     * @param fileRef
     * @return
     */
    public static String getCotiDocumentPath(long companyId,
            COTIPackage cpackage, COTIProject cproject, String fileType,
            String fileRef)
    {
        boolean isTranslation = COTIConstants.fileType_translation
                .equals(fileType);
        String folderName = (isTranslation ? COTIConstants.Dir_TranslationFiles_Name
                : COTIConstants.Dir_ReferenceFiles_Name);

        String projectDir = getProjectDir(companyId, cpackage, cproject);
        String filePath = projectDir + "/" + folderName + "/" + fileRef;
        return filePath;
    }

    /**
     * Get COTI.XML path by project
     * 
     * @param companyId
     * @param cpackage
     * @param cproject
     * @return
     */
    public static String getCotiXmlPath(long companyId, COTIPackage cpackage,
            COTIProject cproject)
    {
        // copy coti xml to path
        String projectDir = getProjectDir(companyId, cpackage, cproject);
        String cotiXmlPath = projectDir + "/COTI.XML";
        return cotiXmlPath;
    }

    /**
     * Zip the file and get its bytes content
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] zipAndReadData(File file)
            throws FileNotFoundException, IOException
    {
        File tempFile = File.createTempFile("GS_COTI_file", ".zip");

        ZipIt.addEntriesToZipFile(tempFile, new File[] { file }, true,
                "Download From GlobalSight");

        return FileUtil.readFile(tempFile, (int) tempFile.length());
    }

    /**
     * Unzip the bytes data and get the first file
     * 
     * @param cotiFile
     * @return
     * @throws Exception
     */
    public static File unzipAndGetFirstFile(byte[] cotiFile) throws Exception
    {
        // read coti xml file
        File tempFile = File.createTempFile("GS_COTI_xml", ".zip");
        File tempDir = tempFile.getParentFile();
        File tempUnzip = new File(tempDir, tempFile.getName() + ".unzip");

        writeFile(tempFile, cotiFile);
        List<String> files = ZipIt.unpackZipPackage(tempFile.getPath(),
                tempUnzip.getPath());

        File firstFile = null;
        if (files != null && files.size() > 0)
        {
            firstFile = new File(tempUnzip, files.get(0));
        }

        return firstFile;
    }

    /**
     * Unzip the zipped COTI.XML and read out its content
     * 
     * @param cotiFile
     * @return
     * @throws Exception
     */
    public static String readOutCotiXml(byte[] cotiFile) throws Exception
    {
        File cotiXmlFile = unzipAndGetFirstFile(cotiFile);
        String cotiXml = FileUtil.readFile(cotiXmlFile, "UTF-8");
        return cotiXml;
    }

    /**
     * Save COTI.XML to its path in GlobalSight
     * 
     * @param companyId
     * @param cpackage
     * @param cproject
     * @param cotiXml
     * @throws IOException
     */
    public static void saveCotiXml(long companyId, COTIPackage cpackage,
            COTIProject cproject, String cotiXml) throws IOException
    {
        // copy coti xml to path
        String cotiXmlPath = getCotiXmlPath(companyId, cpackage, cproject);
        FileUtil.writeFile(new File(cotiXmlPath), cotiXml, "UTF-8");
    }

    /**
     * Cancel COTI job
     * 
     * @param job
     */
    public static void cancelCOTIJob(Job job)
    {
        if (job == null)
        {
            return;
        }

        cancelCOTIJob(job.getJobId());
    }

    /**
     * Cancel COTI job by job id
     * 
     * @param globalsightJobId
     */
    public static void cancelCOTIJob(long globalsightJobId)
    {
        try
        {
            COTIProject cp = COTIDbUtil
                    .getCOTIProjectByGlobalSightJobId(globalsightJobId);
            if (cp != null)
            {
                cp.setStatus(COTIConstants.project_status_cancelled);
                COTIDbUtil.update(cp);
            }
        }
        catch (Exception ex)
        {
            // ignore this exception
        }
    }

    /**
     * Finish COTI job
     * 
     * @param job
     */
    public static void finishCOTIJob(Job job)
    {
        if (job == null)
        {
            return;
        }

        finishCOTIJob(job.getJobId());
    }

    /**
     * Finish COTI job by job id
     * 
     * @param globalsightJobId
     */
    public static void finishCOTIJob(long globalsightJobId)
    {
        try
        {
            COTIProject cp = COTIDbUtil
                    .getCOTIProjectByGlobalSightJobId(globalsightJobId);
            if (cp != null)
            {
                cp.setStatus(COTIConstants.project_status_finished);
                COTIDbUtil.update(cp);
            }
        }
        catch (Exception ex)
        {
            // ignore this exception
        }
    }

    /**
     * Get GlobalSight target page by COTI Document
     * @param cproject
     * @param cdoc
     * @return
     */
    public static TargetPage getTargetPageByCOTIDocument(COTIProject cproject,
            COTIDocument cdoc)
    {
        if (cproject == null || cdoc == null)
        {
            return null;
        }
        
        Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);
        String fileRef = cdoc.getFileRef();
        boolean isTranslation = cdoc.getIsTranslation();

        // if GlobalSight job is exported, then return the exported file
        if (isTranslation && gsjob != null)
        {
            Iterator<Workflow> ite = gsjob.getWorkflows().iterator();
            if (ite != null && ite.hasNext())
            {
                Workflow wf = ite.next();
                Vector<TargetPage> targetPages = wf.getTargetPages();

                for (TargetPage targetPage : targetPages)
                {
                    String pagename = targetPage.getSourcePage()
                            .getExternalPageId();
                    if (pagename.endsWith(fileRef))
                    {
                        return targetPage;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Create Locale from string like ZH-CN or zh_CN
     * 
     * @param p_localeName
     * @return
     * @throws NoSuchElementException
     */
    public static Locale makeLocaleFromString(String p_localeName)
            throws NoSuchElementException
    {
        String sep = "_";
        if (!p_localeName.contains(sep) && p_localeName.contains("-"))
        {
            sep = "-";
        }

        StringTokenizer st = new StringTokenizer(p_localeName, sep);
        String language = st.nextToken();
        String country = st.nextToken();
        String variant = null;
        Locale locale = null;

        if (st.hasMoreTokens())
            variant = st.nextToken();

        if (variant == null)
            locale = new Locale(language, country);
        else
            locale = new Locale(language, country, variant);

        return locale;
    }

    /**
     * Get GlobalSight Locale Manager
     * 
     * @return
     * @throws Exception
     */
    public static LocaleManager getLocaleManager() throws Exception
    {
        LocaleManager m_locMgr = null;

        try
        {
            m_locMgr = ServerProxy.getLocaleManager();
        }
        catch (Exception e)
        {
            throw e;
        }

        return m_locMgr;
    }

    /**
     * Get GlobalSight job handler
     * 
     * @return
     * @throws Exception
     */
    public static JobHandler getJobHandler() throws Exception
    {
        JobHandler jh = null;

        try
        {
            jh = ServerProxy.getJobHandler();
        }
        catch (Exception e)
        {
            throw e;
        }

        return jh;
    }
}

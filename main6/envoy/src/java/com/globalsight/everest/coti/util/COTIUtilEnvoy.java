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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
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

    public static String getCOTITempDir(long companyId)
    {
        return getCOTIRootDir(companyId) + File.separator + "Temp";
    }

    public static String getCOTIRootDir(long companyId)
    {
        String path = AmbFileStoragePathUtils.getFileStorageDirPath(companyId);
        path += File.separator + COTIConstants.Dir_Root_Name;

        return path;
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
        String path = getCOTIRootDir(companyId);
        path += File.separator + cpackage.getId() + ".unzip";
        path += File.separator + cproject.getId();

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
        boolean addFolderName = true;
        if (fileRef.startsWith(COTIConstants.Dir_TranslationFiles_Name)
                || fileRef.startsWith(COTIConstants.Dir_ReferenceFiles_Name))
        {
            addFolderName = false;
        }

        String projectDir = getProjectDir(companyId, cpackage, cproject);
        String filePath = projectDir + File.separator
                + (addFolderName ? folderName + File.separator : "") + fileRef;
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
        String cotiXmlPath = projectDir + File.separator + "COTI.XML";
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

    public static File zipCOTIProject(List<COTIProject> cotiProjects)
            throws FileNotFoundException, IOException
    {
        List<File> packageFiles = new ArrayList<File>();
        List<COTIPackage> packages = new ArrayList<COTIPackage>();

        for (int i = 0; i < cotiProjects.size(); i++)
        {
            COTIProject cotiProject = cotiProjects.get(i);
            long packageId = cotiProject.getPackageId();
            COTIPackage thePackage = COTIDbUtil.getCOTIPackage(packageId);

            // added continue
            if (packages.contains(thePackage))
            {
                continue;
            }

            // add project into package
            List<COTIProject> theProjects = getCOTIProjectsByPackageId(
                    cotiProjects, packageId);

            long companyId = thePackage.getCompanyId();
            File tempDir = new File(getCOTITempDir(companyId));
            if (!tempDir.exists())
            {
                tempDir.mkdirs();
            }
            String packageFileName = thePackage.getId() + "_"
                    + thePackage.getFileName();

            File tempCoti = File.createTempFile("ID_" + thePackage.getId()
                    + "_", ".coti", tempDir);
            tempCoti.delete();
            File parant = new File(tempCoti.getPath() + ".unzip");
            File tempFile = new File(parant, packageFileName);
            if (!tempFile.exists())
            {
                tempFile.getParentFile().mkdirs();
                tempFile.createNewFile();
            }

            for (int j = 0; j < theProjects.size(); j++)
            {
                COTIProject theProject = theProjects.get(j);
                String projectDir = new File(getProjectDir(companyId,
                        thePackage, theProject)).getPath();
                String cotiXmlPath = getCotiXmlPath(companyId, thePackage,
                        theProject);
                File cotiXmlFile = new File(cotiXmlPath);
                Set<File> docs = new HashSet<File>();
                if (cotiXmlFile.exists())
                {
                    docs.add(cotiXmlFile);
                }

                List<COTIDocument> theDocs = COTIDbUtil
                        .getCOTIDocumentsByProjectId(theProject.getId());
                for (COTIDocument cotiDocument : theDocs)
                {
                    File f = getExportedDocument(thePackage, theProject,
                            cotiDocument);

                    if (f.exists())
                    {
                        String path = COTIUtilEnvoy.getCotiDocumentPath(
                                thePackage.getCompanyId(), thePackage, theProject, cotiDocument);
                        File oriFile = new File(path);
                        
                        if (f.getPath().equals(oriFile.getPath()))
                        {
                            docs.add(f);
                        }
                        else
                        {
                            FileUtil.copyFile(f, oriFile);
                            docs.add(oriFile);
                        }
                    }
                }

                ZipIt.addEntriesToZipFile(tempFile, docs, theProject.getId()
                        + "_" + theProject.getDirName(), projectDir, "");
            }

            packages.add(thePackage);
            packageFiles.add(tempFile);
        }

        if (packageFiles.size() > 0)
        {
            File result = File.createTempFile("GS_Downlaod_", ".coti");
            File[] fa = new File[packageFiles.size()];
            fa = packageFiles.toArray(fa);
            ZipIt.addEntriesToZipFile(result, fa, true,
                    "Generated by GlobalSight");

            return result;
        }

        return null;
    }

    public static List<COTIProject> getCOTIProjectsByPackageId(
            List<COTIProject> cotiProjects, long packageId)
    {
        List<COTIProject> result = new ArrayList<COTIProject>();

        for (COTIProject cotiProject : cotiProjects)
        {
            if (cotiProject.getPackageId() == packageId)
            {
                result.add(cotiProject);
            }
        }

        return result;
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

    public static List<File> unzipAndGetProjectDir(File cotiFile)
            throws Exception
    {
        // read coti xml file
        File tempFile = File.createTempFile("GS_COTI_xml", ".zip");
        File tempDir = tempFile.getParentFile();
        File tempUnzip = new File(tempDir, tempFile.getName() + ".unzip");

        List<String> files = ZipIt.unpackZipPackage(cotiFile.getPath(),
                tempUnzip.getPath());

        List<File> resultfiles = new ArrayList<File>();
        if (files != null && files.size() > 0)
        {
            for (int i = 0; i < files.size(); i++)
            {
                File ff = new File(tempUnzip, files.get(i));
                String fname = ff.getName();
                if ("COTI.xml".equalsIgnoreCase(fname))
                {
                    resultfiles.add(ff.getParentFile());
                }
            }
        }

        return resultfiles;
    }

    public static COTIProject createCOTIProject(Company c, String cotiXml)
            throws Exception
    {
        COTIPackage cpackage = null;
        COTIProject cproject = null;

        long cid = c.getId();
        COTIXmlBase cx = COTIXmlBase.getInstance(cotiXml);
        cx.setCompanyId(cid);

        // create coti package & project
        cpackage = cx.createPackage();
        HibernateUtil.save(cpackage);
        COTIDbUtil.cacheObject(cpackage.getId(), cpackage);

        cproject = cx.createProject();
        cproject.setPackageId(cpackage.getId());
        cproject.setStatus(COTIConstants.project_status_created);
        HibernateUtil.save(cproject);
        COTIDbUtil.cacheObject(cproject.getId(), cproject);

        List<COTIDocument> docs = cx.createDocuments();
        for (COTIDocument cotiDocument : docs)
        {
            cotiDocument.setProjectId(cproject.getId());
        }
        HibernateUtil.save(docs);
        for (COTIDocument cotiDocument : docs)
        {
            COTIDbUtil.cacheObject(cotiDocument.getId(), cotiDocument);
        }

        COTIUtilEnvoy.saveCotiXml(cid, cpackage, cproject, cotiXml);

        try
        {
            HibernateUtil.getSession().close();
        }
        catch (Exception ex)
        {
            // ignore this
        }

        return cproject;
    }

    public static boolean startCOTIProject(COTIProject cproject) throws Exception
    {
        boolean result = false;
        
        // check project status
        String oriStatus = cproject.getStatus();
        if (COTIConstants.project_status_created.equals(oriStatus)
                || COTIConstants.project_status_finished.equals(oriStatus))
        {
            cproject.setStatus(COTIConstants.project_status_started);
            try
            {
                COTIDbUtil.update(cproject);
                result = true;
            }
            catch (Exception e)
            {
                String msg = "Update status failed information by projectId "
                        + cproject.getId();
                throw new Exception(msg, e);
            }
        }
        
        return result;
    }
    
    public static String saveDocumentFile(COTIPackage cpackage, COTIProject cproject,
            File documentFile, String fileRef, String fileType)
            throws IOException
    {
        String path = COTIUtilEnvoy.getCotiDocumentPath(
                cpackage.getCompanyId(), cpackage, cproject, fileType, fileRef);
        File dst = new File(path);
        if (dst.exists())
        {
            dst.delete();
        }
        FileUtil.copyFile(documentFile, dst);

        String docid = COTIDbUtil.getDocumentId("" + cproject.getId(),
                fileType, fileRef);

        return docid;
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

    public static File getExportedDocument(COTIPackage cpackage,
            COTIProject cproject, COTIDocument cdoc)
    {
        File result = null;
        boolean returnOriFile = true;

        if (cdoc.getIsTranslation())
        {
            TargetPage tp = getTargetPageByCOTIDocument(cproject, cdoc);
            String companyFolderPath = AmbFileStoragePathUtils
                    .getCxeDocDirPath(cpackage.getCompanyId());

            if (tp != null)
            {
                Job gsjob = tp.getWorkflowInstance().getJob();
                String targetLocale = tp.getGlobalSightLocale().toString();

                File companyFolder = new File(companyFolderPath);
                File targetLocaleFolder = new File(companyFolder, targetLocale);
                File jobIdFile = new File(targetLocaleFolder, ""
                        + gsjob.getJobId());
                File exportedFile = new File(jobIdFile, cdoc.getFileRef());

                if (exportedFile.exists() && exportedFile.isFile())
                {
                    result = exportedFile;
                    returnOriFile = false;
                }
            }
        }

        // else return the source file
        if (returnOriFile)
        {
            String path = COTIUtilEnvoy.getCotiDocumentPath(
                    cpackage.getCompanyId(), cpackage, cproject, cdoc);

            result = new File(path);
        }

        return result;
    }

    /**
     * Get GlobalSight target page by COTI Document
     * 
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

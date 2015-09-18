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
package com.globalsight.util;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Used for obtaining file stored paths in GlobalSight.
 * 
 */
public class AmbFileStoragePathUtils
{
    private static SystemConfiguration sc = SystemConfiguration.getInstance();

    private static Map<String, String> cxeDocDirPaths = new HashMap<String, String>();

    private static Map<String, File> cxeDocDirs = new HashMap<String, File>();

    private static Map<String, File> tempFileDirs = new HashMap<String, File>();

    private static Map<String, String> fileStorageDirPaths = new HashMap<String, String>();

    private static Map<String, File> fileStorageDirs = new HashMap<String, File>();

    private static Map<String, File> stfParentDirs = new HashMap<String, File>();

    private static Map<String, File> unextractedParentDirs = new HashMap<String, File>();

    private static Map<String, File> catalogDirs = new HashMap<String, File>();

    private static Map<String, File> commentReferenceDirs = new HashMap<String, File>();

    private static Map<String, File> dtdDirs = new HashMap<String, File>();

    private static Map<String, File> jobAttributeDirs = new HashMap<String, File>();

    private static Map<String, File> xslDirs = new HashMap<String, File>();

    private static Map<String, File> commentReferenceTempDirs = new HashMap<String, File>();

    private static Map<String, File> customerDownloadDirs = new HashMap<String, File>();

    private static Map<String, File> indexDirs = new HashMap<String, File>();

    private static Map<String, File> tmIndexFileDirs = new HashMap<String, File>();

    private static Map<String, File> goldTmIndexDirs = new HashMap<String, File>();

    private static Map<String, File> alignerPackageDirs = new HashMap<String, File>();

    private static Map<String, File> alignerTmpDirs = new HashMap<String, File>();

    private static Map<String, File> supportFileDirs = new HashMap<String, File>();

    private static Map<String, File> desktopIconDir = new HashMap<String, File>();

    private static Map<String, File> desktopIconExportedDir = new HashMap<String, File>();

    private static Map<String, File> pdfPreviewDir = new HashMap<String, File>();

    private static Map<String, File> wfTemplateXmlDir = new HashMap<String, File>();

    private static Map<String, File> corpusDirs = new HashMap<String, File>();

    private static Map<String, File> m_uploadDir = new HashMap<String, File>();

    private static Map<String, File> m_reportsDir = new HashMap<String, File>();

    public final static String INDEX_SUB_DIR = "GlobalSight/Indexes";

    public final static String TEMPFILE_SUB_DIRECTORY = "GlobalSight/CXE";

    public final static String STF_SUB_DIRECTORY = "GlobalSight/SecondaryTargetFiles";

    public final static String UNEXTRACTED_SUB_DIRECTORY = "GlobalSight/UnextractedFiles";

    public final static String CATALOG_SUB_DIRECTORY = "catalog";

    public final static String COMMENT_REFERENCE_SUB_DIR = "GlobalSight/CommentReference";

    public final static String COMMENT_REFERENCE_TEMP_SUB_DIR = "tmp";

    public final static String CUSTOMER_DOWNLOAD_SUB_DIR = "GlobalSight/CustomerDownload";

    public final static String TM_INDEX_FILE_SUB_DIR = "GlobalSight/TmIndexFiles";

    public final static String TM_IMPORT_FILE_SUB_DIR = "GlobalSight/TmImport";

    public final static String TM_EXPORT_FILE_SUB_DIR = "GlobalSight/TmExport";

    public final static String GOLD_TM_INDEX_SUB_DIR = "GlobalSight/GoldTmIndex";

    public final static String ALIGNER_PACKAGE_SUB_DIRECTORY = "GlobalSight/AlignerPackages";

    public final static String ALIGNER_TMP_SUB_DIRECTORY = "_Aligner_";

    public final static String DTD_FILE_DIR = "GlobalSight/dtd";

    public final static String JOB_ATTRIBUTE_FILE_DIR = "GlobalSight/JobAttribute";

    public final static String XSL_FILE_DIR = "GlobalSight/xsl";

    public final static String SUPPORT_FILES_SUB_DIRECTORY = "GlobalSight/SupportFiles";

    public final static String DESKTOPICON_DIRECTORY = "GlobalSight/DesktopIcon";

    public final static String EXPORTED = "exported";

    public final static String PDF_PREVIEW = "GlobalSight/Preview";

    public final static String WF_TEMPLATE_XML = "GlobalSight/WorkflowTemplateXml";

    public final static String PATH_PROPERTIES = "/properties/";

    public final static String CONV_DIR_OFFICE2010 = "OfficeXml-Conv";

    public final static String CONV_DIR_IDML = "Idml-Conv";

    public final static String CONV_DIR_OPENOFFICE = "OpenOffice-Conv";

    public final static String FRAMEMAKER9_CONV_DIR = "FrameMaker9";

    public final static String OFFLINE_FILE_DOWNLOAD_DIR = "workOfflineDownload";

    public final static String WEBSERVICE_DIR = "webservice";

    public final static String CORPUS_DIR = "GlobalSight/Corpus";

    public final static String DIR_UPLOAD = "GlobalSight/Upload";

    public final static String DIR_REPORTS = "GlobalSight/Reports";

    public static File getTempFileDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (tempFileDirs.get(companyId) == null)
        {
            File tempFileDir = new File(getFileStorageDirPath(companyId),
                    TEMPFILE_SUB_DIRECTORY);
            tempFileDir.mkdirs();
            tempFileDirs.put(companyId, tempFileDir);
        }

        return (File) tempFileDirs.get(companyId);
    }

    public static File getCxeDocDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getCxeDocDir(companyId);
    }

    public static File getCxeDocDir(long companyId)
    {
        return getCxeDocDir(String.valueOf(companyId));
    }

    public static File getCxeDocDir(String p_companyId)
    {
        if (cxeDocDirs.get(p_companyId) == null)
        {
            String path = getCxeDocDirPath(p_companyId);
            if (path == null)
            {
                return null;
            }
            File cxeDocDir = new File(path);
            cxeDocDir.mkdirs();

            cxeDocDirs.put(p_companyId, cxeDocDir);
        }

        return (File) cxeDocDirs.get(p_companyId);
    }

    public static String getCxeDocDirPath()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getCxeDocDirPath(companyId);
    }

    public static String getCxeDocDirPath(String p_companyId)
    {
        if (cxeDocDirPaths.get(p_companyId) == null)
        {
            String cxeDocDirPath = sc.getStringParameter(
                    SystemConfigParamNames.CXE_DOCS_DIR, p_companyId);
            cxeDocDirPaths.put(p_companyId, cxeDocDirPath);
        }

        return (String) cxeDocDirPaths.get(p_companyId);
    }

    public static String getCxeDocDirPath(long companyId)
    {
        return getCxeDocDirPath(String.valueOf(companyId));
    }

    /**
     * Get the storage dir for company base on the company_id in current thread.
     * 
     * @deprecated
     * @return
     */
    public static String getFileStorageDirPath()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getFileStorageDirPath(companyId);
    }

    /**
     * Get the storage dir for company base on the parameter p_companyId. It
     * will get it from Systemparameter table if the stroage has no in memory.
     * 
     * @param p_companyId
     *            specify the company id.
     * @return
     */
    public static String getFileStorageDirPath(String p_companyId)
    {
        if (fileStorageDirPaths.get(p_companyId) == null)
        {
            String fileStorageDirPath = sc.getStringParameter(
                    SystemConfigParamNames.FILE_STORAGE_DIR, p_companyId);
            fileStorageDirPaths.put(p_companyId, fileStorageDirPath);
        }

        return (String) fileStorageDirPaths.get(p_companyId);
    }

    public static String getFileStorageDirPath(long companyId)
    {
        return getFileStorageDirPath(String.valueOf(companyId));
    }

    public static File getFileStorageDir(long companyId)
    {
        return getFileStorageDir(String.valueOf(companyId));
    }

    public static File getFileStorageDir(String p_companyId)
    {
        if (fileStorageDirs.get(p_companyId) == null)
        {
            String path = getFileStorageDirPath(p_companyId);
            if (path == null)
            {
                return null;
            }
            File fileStorageDir = new File(path);
            fileStorageDir.mkdirs();

            fileStorageDirs.put(p_companyId, fileStorageDir);
        }

        return (File) fileStorageDirs.get(p_companyId);
    }

    /**
     * @deprecated
     */
    public static File getFileStorageDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getFileStorageDir(companyId);
    }

    public static File getStfParentDir(long companyId)
    {
        return getStfParentDir(String.valueOf(companyId));
    }

    public static File getStfParentDir(String companyId)
    {
        if (companyId == null)
        {
            companyId = CompanyThreadLocal.getInstance().getValue();
        }
        if (stfParentDirs.get(companyId) == null)
        {
            File stfParentDir = new File(getFileStorageDirPath(companyId),
                    STF_SUB_DIRECTORY);
            stfParentDir.mkdirs();
            stfParentDirs.put(companyId, stfParentDir);
        }

        return (File) stfParentDirs.get(companyId);
    }

    public static File getStfParentDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (stfParentDirs.get(companyId) == null)
        {
            File stfParentDir = new File(getFileStorageDirPath(),
                    STF_SUB_DIRECTORY);
            stfParentDir.mkdirs();
            stfParentDirs.put(companyId, stfParentDir);
        }

        return (File) stfParentDirs.get(companyId);
    }

    public static File getUnextractedParentDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (unextractedParentDirs.get(companyId) == null)
        {
            File unextractedParentDir = new File(
                    getFileStorageDirPath(companyId),
                    UNEXTRACTED_SUB_DIRECTORY);
            unextractedParentDir.mkdirs();
            unextractedParentDirs.put(companyId, unextractedParentDir);
        }

        return (File) unextractedParentDirs.get(companyId);
    }

    public static File getUnextractedParentDir(long companyId)
    {
        return getUnextractedParentDir(String.valueOf(companyId));
    }

    public static File getUnextractedParentDir(String companyId)
    {
        if (unextractedParentDirs.get(companyId) == null)
        {
            File unextractedParentDir = new File(
                    getFileStorageDirPath(companyId),
                    UNEXTRACTED_SUB_DIRECTORY);
            unextractedParentDir.mkdirs();
            unextractedParentDirs.put(companyId, unextractedParentDir);
        }

        return (File) unextractedParentDirs.get(companyId);
    }

    public static File getCatalogDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (catalogDirs.get(companyId) == null)
        {
            File catalogDir = new File(getFileStorageDirPath(),
                    CATALOG_SUB_DIRECTORY);
            catalogDir.mkdirs();
            catalogDirs.put(companyId, catalogDir);
        }

        return (File) catalogDirs.get(companyId);
    }

    public static File getCommentReferenceDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getCommentReferenceDir(companyId);
    }

    public static File getCommentReferenceDir(String companyId)
    {
        if (commentReferenceDirs.get(companyId) == null)
        {
            File commentReferenceDir = new File(
                    getFileStorageDirPath(companyId),
                    COMMENT_REFERENCE_SUB_DIR);
            commentReferenceDir.mkdirs();
            commentReferenceDirs.put(companyId, commentReferenceDir);
        }

        return (File) commentReferenceDirs.get(companyId);
    }

    public static File getCommentReferenceDir(long companyId)
    {
        return getCommentReferenceDir(String.valueOf(companyId));
    }

    public static File getDtdDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (dtdDirs.get(companyId) == null)
        {
            File dtdDir = new File(getFileStorageDirPath(), DTD_FILE_DIR);
            dtdDir.mkdirs();
            dtdDirs.put(companyId, dtdDir);
        }

        return (File) dtdDirs.get(companyId);
    }

    public static File getJobAttributeDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (jobAttributeDirs.get(companyId) == null)
        {
            File jobAttributeDir = new File(getFileStorageDirPath(),
                    JOB_ATTRIBUTE_FILE_DIR);
            jobAttributeDir.mkdirs();
            jobAttributeDirs.put(companyId, jobAttributeDir);
        }

        return (File) jobAttributeDirs.get(companyId);
    }

    public static File getJobAttributeDir2(long companyId)
    {
        return getJobAttributeDir2(String.valueOf(companyId));
    }

    public static File getJobAttributeDir2(String companyId)
    {
        if (jobAttributeDirs.get(companyId) == null)
        {
            File jobAttributeDir = new File(getFileStorageDirPath(companyId),
                    JOB_ATTRIBUTE_FILE_DIR);
            jobAttributeDir.mkdirs();
            jobAttributeDirs.put(companyId, jobAttributeDir);
        }

        return (File) jobAttributeDirs.get(companyId);
    }

    public static File getXslDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (xslDirs.get(companyId) == null)
        {
            File xslDir = new File(getFileStorageDirPath(), XSL_FILE_DIR);
            xslDir.mkdirs();
            xslDirs.put(companyId, xslDir);
        }

        return (File) xslDirs.get(companyId);
    }

    public static File getXslDir(long fileProfileId)
    {
        FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class,
                fileProfileId, false);

        String companyId = null;
        if (fp != null)
        {
            companyId = String.valueOf(fp.getCompanyId());
        }
        else
        {
            companyId = CompanyThreadLocal.getInstance().getValue();
        }

        if (xslDirs.get(companyId) == null)
        {
            File xslDir = new File(getFileStorageDirPath(companyId),
                    XSL_FILE_DIR);
            xslDir.mkdirs();
            xslDirs.put(companyId, xslDir);
        }

        return (File) xslDirs.get(companyId);
    }

    public static String getCommentReferenceDirPath()
    {
        return getCommentReferenceDir().toString();
    }

    public static File getCommentReferenceTempDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (commentReferenceTempDirs.get(companyId) == null)
        {
            File commentReferenceTempDir = new File(getCommentReferenceDir(),
                    COMMENT_REFERENCE_TEMP_SUB_DIR);
            commentReferenceTempDir.mkdirs();
            commentReferenceTempDirs.put(companyId, commentReferenceTempDir);
        }

        return (File) commentReferenceTempDirs.get(companyId);
    }

    /**
     * @deprecated
     */
    public static File getCustomerDownloadDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getCustomerDownloadDir(companyId);
    }

    public static File getCustomerDownloadDir(Long p_companyId)
    {
        return getCustomerDownloadDir(String.valueOf(p_companyId));
    }

    public static File getCustomerDownloadDir(String p_companyId)
    {
        if (customerDownloadDirs.get(p_companyId) == null)
        {
            File customerDownloadDir = new File(
                    getFileStorageDirPath(p_companyId),
                    CUSTOMER_DOWNLOAD_SUB_DIR);
            customerDownloadDir.mkdirs();
            customerDownloadDirs.put(p_companyId, customerDownloadDir);
        }

        return (File) customerDownloadDirs.get(p_companyId);
    }

    public static File getIndexDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (indexDirs.get(companyId) == null)
        {
            File indexDir = new File(getFileStorageDirPath(), INDEX_SUB_DIR);
            indexDir.mkdirs();
            indexDirs.put(companyId, indexDir);
        }

        return (File) indexDirs.get(companyId);
    }

    public static File getTmIndexFileDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (tmIndexFileDirs.get(companyId) == null)
        {
            File tmIndexFileDir = new File(getFileStorageDirPath(),
                    TM_INDEX_FILE_SUB_DIR);
            tmIndexFileDir.mkdirs();
            tmIndexFileDirs.put(companyId, tmIndexFileDir);
        }

        return (File) tmIndexFileDirs.get(companyId);
    }

    public static File getGoldTmIndexDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (goldTmIndexDirs.get(companyId) == null)
        {
            File goldTmIndexDir = new File(getFileStorageDirPath(),
                    GOLD_TM_INDEX_SUB_DIR);
            goldTmIndexDir.mkdirs();
            goldTmIndexDirs.put(companyId, goldTmIndexDir);
        }

        return (File) goldTmIndexDirs.get(companyId);
    }

    public static File getGoldTmIndexDir(long tmId)
    {
        ProjectTM tm = HibernateUtil.get(ProjectTM.class, tmId);
        if (tm == null)
        {
            return getGoldTmIndexDir();
        }

        String companyId = String.valueOf(tm.getCompanyId());
        if (goldTmIndexDirs.get(companyId) == null)
        {
            File goldTmIndexDir = new File(getFileStorageDirPath(companyId),
                    GOLD_TM_INDEX_SUB_DIR);
            goldTmIndexDir.mkdirs();
            goldTmIndexDirs.put(companyId, goldTmIndexDir);
        }

        return (File) goldTmIndexDirs.get(companyId);
    }

    public static File getAlignerPackageDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (alignerPackageDirs.get(companyId) == null)
        {
            File alignerPackageDir = new File(getFileStorageDirPath(),
                    ALIGNER_PACKAGE_SUB_DIRECTORY);
            alignerPackageDir.mkdirs();
            alignerPackageDirs.put(companyId, alignerPackageDir);
        }

        return (File) alignerPackageDirs.get(companyId);
    }

    public static File getAlignerTmpDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (alignerTmpDirs.get(companyId) == null)
        {
            File alignerTmpDir = new File(getFileStorageDirPath(),
                    ALIGNER_TMP_SUB_DIRECTORY);
            alignerTmpDir.mkdirs();
            alignerTmpDirs.put(companyId, alignerTmpDir);
        }

        return (File) alignerTmpDirs.get(companyId);
    }

    public static File getSupportFileDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (supportFileDirs.get(companyId) == null)
        {
            File supportFileDir = new File(getFileStorageDirPath(),
                    SUPPORT_FILES_SUB_DIRECTORY);
            supportFileDir.mkdirs();
            supportFileDirs.put(companyId, supportFileDir);
        }

        return (File) supportFileDirs.get(companyId);
    }

    public static File getDesktopIconDir(String companyId)
    {
        if (desktopIconDir.get(companyId) == null)
        {
            File desktopiconDir = new File(getFileStorageDirPath(companyId),
                    DESKTOPICON_DIRECTORY);
            desktopiconDir.mkdirs();
            desktopIconDir.put(companyId, desktopiconDir);
        }

        return (File) desktopIconDir.get(companyId);
    }

    public static File getDesktopIconExportedDir(long companyId)
    {
        return getDesktopIconExportedDir(String.valueOf(companyId));
    }

    public static File getDesktopIconExportedDir(String companyId)
    {
        if (desktopIconExportedDir.get(companyId) == null)
        {
            File desktopiconExportedDir = new File(getDesktopIconDir(companyId),
                    EXPORTED);
            desktopiconExportedDir.mkdirs();
            desktopIconExportedDir.put(companyId, desktopiconExportedDir);
        }

        return (File) desktopIconExportedDir.get(companyId);
    }

    /**
     * For abobe preview
     * 
     * @return
     */
    public static File getPdfPreviewDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return getPdfPreviewDir(companyId);
    }

    public static File getPdfPreviewDir(String p_company_id)
    {
        if (pdfPreviewDir.get(p_company_id) == null)
        {
            File pdfPriviewDir = new File(getFileStorageDirPath(p_company_id),
                    PDF_PREVIEW);
            pdfPriviewDir.mkdirs();
            pdfPreviewDir.put(p_company_id, pdfPriviewDir);
        }

        return (File) pdfPreviewDir.get(p_company_id);
    }

    public static File getCorpusDir(long companyId)
    {
        return getCorpusDir(String.valueOf(companyId));
    }

    public static File getCorpusDir(String p_company_id)
    {
        if (corpusDirs.get(p_company_id) == null)
        {
            File corpusDir = new File(getFileStorageDirPath(p_company_id),
                    CORPUS_DIR);
            corpusDir.mkdirs();
            corpusDirs.put(p_company_id, corpusDir);
        }

        return (File) corpusDirs.get(p_company_id);
    }

    public static File getPdfPreviewDir(long companyId)
    {
        return getPdfPreviewDir(String.valueOf(companyId));
    }

    /**
     * Gets the directory of the workflow template xml.
     * 
     * @return the directory of the workflow template xml.
     */
    public static File getWorkflowTemplateXmlDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return getWorkflowTemplateXmlDir(companyId);
    }

    /**
     * Gets the directory of the workflow template xml.
     * 
     * @param p_company_id
     *            - the current company
     * @return the directory of the workflow template xml.
     */
    public static File getWorkflowTemplateXmlDir(String p_company_id)
    {
        if (wfTemplateXmlDir.get(p_company_id) == null)
        {
            File f = new File(getFileStorageDirPath(p_company_id),
                    WF_TEMPLATE_XML);
            f.mkdirs();
            wfTemplateXmlDir.put(p_company_id, f);
        }

        return (File) wfTemplateXmlDir.get(p_company_id);
    }

    public static File getWorkflowTemplateXmlDir(long companyId)
    {
        return getWorkflowTemplateXmlDir(String.valueOf(companyId));
    }

    public static File getPropertiesDir(long companyId)
    {
        return getPropertiesDir(String.valueOf(companyId));
    }

    public static File getPropertiesDir(String companyId)
    {
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        URL url = AmbFileStoragePathUtils.class
                .getResource(PATH_PROPERTIES + companyName);
        File f = null;
        if (url != null)
        {
            try
            {
                f = new File(url.toURI());
            }
            catch (Exception e)
            {
                f = new File(url.getPath());
            }
        }
        return f;
    }

    public static String getFrameMaker9ConversionPath()
    {
        StringBuilder path = new StringBuilder();

        path.append(sc.getStringParameter(SystemConfigParamNames.CXE_NTCS_DIR));
        path.append(File.separator);
        path.append(FRAMEMAKER9_CONV_DIR);

        return path.toString();
    }

    public static String getIdmlConversionPath()
    {
        StringBuilder path = new StringBuilder();

        path.append(getFileStorageDirPath(CompanyWrapper.SUPER_COMPANY_ID));
        path.append(File.separator);
        path.append(CONV_DIR_IDML);

        return path.toString();
    }

    public static String getOffice2010ConversionPath()
    {
        StringBuilder path = new StringBuilder();

        path.append(getFileStorageDirPath(CompanyWrapper.SUPER_COMPANY_ID));
        path.append(File.separator);
        path.append(CONV_DIR_OFFICE2010);

        return path.toString();
    }

    public static String getOpenOfficeConversionPath()
    {
        StringBuilder path = new StringBuilder();

        path.append(getFileStorageDirPath(CompanyWrapper.SUPER_COMPANY_ID));
        path.append(File.separator);
        path.append(CONV_DIR_OPENOFFICE);

        return path.toString();
    }

    public static String getOffice2003ConversionPath()
    {
        return sc.getStringParameter(
                SystemConfigParamNames.MSOFFICE2003_CONV_DIR);
    }

    public static String getOffice2007ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.MSOFFICE_CONV_DIR);
    }

    public static String getInddCs2ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR);
    }

    public static String getInddCs3ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS3);
    }

    public static String getInddCs4ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS4);
    }

    public static String getInddCs5ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5);
    }

    public static String getInddCs55ConversionPath()
    {
        return sc.getStringParameter(
                SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5);
    }

    public static String getInddCs6ConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS6);
    }

    public static String getInContextReviewInDesignPath()
    {
        return sc.getStringParameter(
                SystemConfigParamNames.INCTXRV_CONV_DIR_INDD,
                CompanyWrapper.SUPER_COMPANY_ID);
    }

    public static String getInContextReviewOfficePath()
    {
        return sc.getStringParameter(
                SystemConfigParamNames.INCTXRV_CONV_DIR_OFFICE,
                CompanyWrapper.SUPER_COMPANY_ID);
    }

    public static String getWindowsPeConversionPath()
    {
        return sc.getStringParameter(SystemConfigParamNames.WINDOWS_PE_DIR);
    }

    /**
     * Gets the upload directory.
     * <p>
     * 
     * @since GBS-3115
     */
    public static File getUploadDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return getUploadDir(companyId);
    }

    public static File getUploadDir(String companyId)
    {
        if (m_uploadDir.get(companyId) == null)
        {
            File uploadDir = new File(getFileStorageDirPath(companyId),
                    DIR_UPLOAD);
            uploadDir.mkdirs();
            m_uploadDir.put(companyId, uploadDir);
        }

        return (File) m_uploadDir.get(companyId);
    }

    public static File getUploadDir(long companyId)
    {
        return getUploadDir(String.valueOf(companyId));
    }

    /**
     * Gets the reports saved directory.
     * <p>
     * 
     * @since GBS-3697
     */
    public static File getReportsDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return getReportsDir(companyId);
    }

    public static File getReportsDir(String companyId)
    {
        if (m_reportsDir.get(companyId) == null)
        {
            File reportsDir = new File(getFileStorageDirPath(companyId),
                    DIR_REPORTS);
            reportsDir.mkdirs();
            m_reportsDir.put(companyId, reportsDir);
        }

        return (File) m_reportsDir.get(companyId);
    }

    public static File getReportsDir(long companyId)
    {
        return getReportsDir(String.valueOf(companyId));
    }
}

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
import java.util.HashMap;
import java.util.Map;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class AmbFileStoragePathUtils
{

    private static SystemConfiguration sc = SystemConfiguration.getInstance();

    private static Map cxeDocDirPaths = new HashMap();

    private static Map cxeDocDirs = new HashMap();

    private static Map tempFileDirs = new HashMap();

    private static Map fileStorageDirPaths = new HashMap();

    private static Map fileStorageDirs = new HashMap();

    private static Map stfParentDirs = new HashMap();

    private static Map unextractedParentDirs = new HashMap();

    private static Map catalogDirs = new HashMap();

    private static Map commentReferenceDirs = new HashMap();
    
    private static Map dtdDirs = new HashMap();
    
    private static Map jobAttributeDirs = new HashMap();
    
    private static Map xslDirs = new HashMap();

    private static Map commentReferenceTempDirs = new HashMap();

    private static Map customerDownloadDirs = new HashMap();

    private static Map indexDirs = new HashMap();

    private static Map tmIndexFileDirs = new HashMap();

    private static Map goldTmIndexDirs = new HashMap();

    private static Map alignerPackageDirs = new HashMap();

    private static Map alignerTmpDirs = new HashMap();

    private static Map supportFileDirs = new HashMap();

    private static Map desktopIconDir = new HashMap();

    private static Map desktopIconExportedDir = new HashMap();

    private static Map pdfPreviewDir = new HashMap();
    
    private static Map wfTemplateXmlDir = new HashMap();

    public final static String INDEX_SUB_DIR = "GlobalSight/Indexes";

    public final static String TEMPFILE_SUB_DIRECTORY = "GlobalSight/CXE";

    public final static String STF_SUB_DIRECTORY = "GlobalSight/SecondaryTargetFiles";

    public final static String UNEXTRACTED_SUB_DIRECTORY = "GlobalSight/UnextractedFiles";

    public final static String CATALOG_SUB_DIRECTORY = "catalog";

    public final static String COMMENT_REFERENCE_SUB_DIR = "GlobalSight/CommentReference";

    public final static String COMMENT_REFERENCE_TEMP_SUB_DIR = "tmp";

    public final static String CUSTOMER_DOWNLOAD_SUB_DIR = "GlobalSight/CustomerDownload";

    public final static String TM_INDEX_FILE_SUB_DIR = "GlobalSight/TmIndexFiles";

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

    public static File getTempFileDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (tempFileDirs.get(companyId) == null)
        {
            File tempFileDir = new File(getFileStorageDirPath(),
                    TEMPFILE_SUB_DIRECTORY);
            tempFileDir.mkdirs();
            tempFileDirs.put(companyId, tempFileDir);
        }

        return (File) tempFileDirs.get(companyId);
    }

    public static File getCxeDocDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (cxeDocDirs.get(companyId) == null)
        {
            File cxeDocDir = new File(getCxeDocDirPath());
            cxeDocDir.mkdirs();

            cxeDocDirs.put(companyId, cxeDocDir);
        }

        return (File) cxeDocDirs.get(companyId);
    }

    public static String getCxeDocDirPath()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (cxeDocDirPaths.get(companyId) == null)
        {
            String cxeDocDirPath = sc
                    .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR);
            cxeDocDirPaths.put(companyId, cxeDocDirPath);
        }

        return (String) cxeDocDirPaths.get(companyId);
    }
    
    public static String getCxeDocDirPath(String p_companyId)
    {
        if (p_companyId == null) 
        {
            return getCxeDocDirPath();
        }
        
        if (cxeDocDirPaths.get(p_companyId) == null)
        {
            String cxeDocDirPath = sc
                    .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR, p_companyId);
            cxeDocDirPaths.put(p_companyId, cxeDocDirPath);
        }

        return (String) cxeDocDirPaths.get(p_companyId);
    }

    /**
     * Get the storage dir for company base on the company_id in current thread.
     * 
     * @return
     */
    public static String getFileStorageDirPath()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return getFileStorageDirPath(companyId);
    }

    /**
     * Get the storage dir for company base on the parameter p_company_id. It
     * will get it from Systemparameter table if the stroage has no in memory.
     * 
     * @param p_company_id
     *            specify the company id.
     * @return
     */
    public static String getFileStorageDirPath(String p_company_id)
    {
        if (fileStorageDirPaths.get(p_company_id) == null)
        {
            String fileStorageDirPath = sc
                    .getStringParameter(SystemConfigParamNames.FILE_STORAGE_DIR);
            fileStorageDirPaths.put(p_company_id, fileStorageDirPath);
        }

        return (String) fileStorageDirPaths.get(p_company_id);
    }

    public static File getFileStorageDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (fileStorageDirs.get(companyId) == null)
        {
            File fileStorageDir = new File(getFileStorageDirPath());
            fileStorageDir.mkdirs();

            fileStorageDirs.put(companyId, fileStorageDir);
        }

        return (File) fileStorageDirs.get(companyId);
    }

    public static File getStfParentDir(String companyId)
    {
    	if (companyId == null) {
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
            File unextractedParentDir = new File(getFileStorageDirPath(),
                    UNEXTRACTED_SUB_DIRECTORY);
            unextractedParentDir.mkdirs();
            unextractedParentDirs.put(companyId, unextractedParentDir);
        }

        return (File) unextractedParentDirs.get(companyId);
    }
    
    public static File getUnextractedParentDir(String companyId)
    {
        if (unextractedParentDirs.get(companyId) == null)
        {
            File unextractedParentDir = new File(getFileStorageDirPath(companyId),
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
        if (commentReferenceDirs.get(companyId) == null)
        {
            File commentReferenceDir = new File(getFileStorageDirPath(),
                    COMMENT_REFERENCE_SUB_DIR);
            commentReferenceDir.mkdirs();
            commentReferenceDirs.put(companyId, commentReferenceDir);
        }

        return (File) commentReferenceDirs.get(companyId);
    }
    
    public static File getCommentReferenceDir(String companyId)
    {
    	if (companyId == null) {
    		return getCommentReferenceDir();
    	} else {
    		if (commentReferenceDirs.get(companyId) == null)
            {
                File commentReferenceDir = new File(getFileStorageDirPath(companyId),
                        COMMENT_REFERENCE_SUB_DIR);
                commentReferenceDir.mkdirs();
                commentReferenceDirs.put(companyId, commentReferenceDir);
            }
    	}
    	
        return (File) commentReferenceDirs.get(companyId);
    }
    
    public static File getDtdDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (dtdDirs.get(companyId) == null)
        {
            File dtdDir = new File(getFileStorageDirPath(),
                    DTD_FILE_DIR);
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
            File xslDir = new File(getFileStorageDirPath(),
                    XSL_FILE_DIR);
            xslDir.mkdirs();
            xslDirs.put(companyId, xslDir);
        }

        return (File) xslDirs.get(companyId);
    }
    
    public static File getXslDir(long fileProfileId)
    {
        FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class, fileProfileId, false);
        
        String companyId = null;
        if (fp != null)
        {
            companyId = fp.getCompanyId();
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

    public static File getCustomerDownloadDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (customerDownloadDirs.get(companyId) == null)
        {
            File customerDownloadDir = new File(getFileStorageDirPath(),
                    CUSTOMER_DOWNLOAD_SUB_DIR);
            customerDownloadDir.mkdirs();
            customerDownloadDirs.put(companyId, customerDownloadDir);
        }

        return (File) customerDownloadDirs.get(companyId);
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
        
        String companyId = tm.getCompanyId();
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

    public static File getDesktopIconDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (desktopIconDir.get(companyId) == null)
        {
            File desktopiconDir = new File(getFileStorageDirPath(),
                    DESKTOPICON_DIRECTORY);
            desktopiconDir.mkdirs();
            desktopIconDir.put(companyId, desktopiconDir);
        }

        return (File) desktopIconDir.get(companyId);
    }

    public static File getDesktopIconExportedDir()
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (desktopIconExportedDir.get(companyId) == null)
        {
            File desktopiconExportedDir = new File(getDesktopIconDir(),
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
     * @param p_company_id -
     *            the current company
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
}

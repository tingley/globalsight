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
package com.globalsight.everest.edit.offline.download;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.glossaries.GlossaryManager;
import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.servlet.util.ServerProxy;

import java.util.ResourceBundle;
import java.io.File;

/**
 * A helper class which supports the Download process.
 */
public class DownloadHelper implements AmbassadorDwUpConstants
{
    static public final Logger CATEGORY = Logger
            .getLogger(DownloadHelper.class);

    //
    // Public Static Constants
    //

    /**
     * A resource bundle that contains our strings. For now we do not translate
     * the directory names. If we translate dir names it will be much harder to
     * later write word macro's to operate on the files in those dirs.
     */
    static public ResourceBundle m_resource;
    static
    {
        m_resource = ResourceBundle
                .getBundle("com.globalsight.everest.edit.offline.download.DownLoadApi");
    }

    //
    // Constructors
    //

    /** Default constructor. */
    public DownloadHelper() throws AmbassadorDwUpException
    {
    }

    //
    // Public Methods
    //

    static private String getTargetLocaleCode(DownloadParams p_downloadParams)
    {
        String targetLocale = p_downloadParams.getTargetLocale().getLanguage()
                + "_" + p_downloadParams.getTargetLocale().getCountryCode();

        return targetLocale;
    }
    
    static public String makeParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/";
    }

    // GBS-633 Downloaded folder naming convention:
    // <jobname>_<targetlocale> instead of <jobname>_<task id> before

    static public String makeTmxParentPath(DownloadParams p_downloadParams)
    {
        boolean isOmegaT = (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT);
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams)
                + (isOmegaT ? "/tm/" : "/tmx/");
    }

    static public String makeTmxPlainParentPath(DownloadParams p_downloadParams)
    {
        boolean isOmegaT = (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT);
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams)
                + (isOmegaT ? "/tm" : "/tmx") + "/plain text/";
    }

    static public String makeTmx14bParentPath(DownloadParams p_downloadParams)
    {
        boolean isOmegaT = (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT);
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams)
                + (isOmegaT ? "/tm" : "/tmx") + (isOmegaT ? "/" : "/1.4b/");
    }

    static public String makeTmxAutoParentPath(DownloadParams p_downloadParams)
    {
        boolean isOmegaT = (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT);
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams)
                + (isOmegaT ? "/tm" : "/tmx") + "/auto/";
    }

    static public String makeMt14bParentPath(DownloadParams p_downloadParams)
    {
        boolean isOmegaT = (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT);
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/mt/";
    }

    static public String makeTermParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/terminology/";
    }

    /**
     * Creates a path - the location under which to write the help files.
     */
    static public String makeHelpParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/help";
    }

    static public String makeInboxParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/"
                + m_resource.getString(INBOX_NAME) + "/";
    }

    static public String makeOutboxParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/"
                + m_resource.getString(OUTBOX_NAME) + "/";
    }

    static public String makeOutboxPath(DownloadParams p_downloadParams)
    {
        return makeOutboxParentPath(p_downloadParams)
                + m_resource.getString(OUTBOX_PLACEHOLDER);
    }

    /**
     * Creates a path - the location under which to write the HTML resource
     * pages.
     */
    static public String makeResParentPath(DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/"
                + m_resource.getString(RESOURCES_DIR) + "/";
    }

    /**
     * Creates a relative path from the offline input/output dir to the HTML
     * resource pages. The string is used by the RTF writer to create links from
     * each segment to the cooresponding resource pages entry.
     * 
     * NOTE: The string must be escaped correctly to appear in an MS-word
     * hyperlink.
     */
    static public String makeMSWordResParentPath(int deep)
    {
    	String parentPrefix = "";
    	for(int i = 0; i < deep - 2; i++)
    	{
    		parentPrefix = parentPrefix + "..\\\\\\\\";
    	}
        return parentPrefix + m_resource.getString(RESOURCES_DIR) + "\\\\\\\\";
    }

    /**
     * Creates a path - the location under which to write the support files.
     */
    static public String makeSupportFileParentPath(
            DownloadParams p_downloadParams)
    {
        return p_downloadParams.getTruncatedJobName() + FILE_NAME_BREAK
                + getTargetLocaleCode(p_downloadParams) + "/"
                + m_resource.getString(SUPPORTFILE_DIR) + "/";
    }

    /**
     * Creates a path - the location under which to write the primary targets.
     * 
     * @return java.lang.String
     */
    static public String makePTFParentPath(DownloadParams p_downloadParams)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(makeInboxParentPath(p_downloadParams));
        // sb.append("/"); inbox already has the slash
        // sb.append( m_resource.getString(PTF_DIR));
        // sb.append("/");
        return sb.toString();
    }

    /**
     * Creates a path - the location under which to write the secondary targets.
     * 
     * @return java.lang.String
     */
    static public String makeSTFParentPath(DownloadParams p_downloadParams)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(makeInboxParentPath(p_downloadParams));
        // sb.append("/"); inbox already has the slash
        sb.append(m_resource.getString(STF_DIR));
        sb.append("/");
        return sb.toString();
    }

    /**
     * Creates a path - the location under which to write the source files.
     * 
     * @return java.lang.String
     */
    static public String makePSFParentPath(DownloadParams p_downloadParams)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(makeInboxParentPath(p_downloadParams));
        // sb.append("/"); inbox already has the slash
        sb.append(m_resource.getString(SOURCE_DIR));
        sb.append("/");
        return sb.toString();
    }

    /**
     * Wraps the code for getting the support file path and handling any
     * exceptions. This is the path from which to read the support files from
     * disk.
     * 
     * @param p_supportFile
     *            a GlossaryFile object.
     * @return full path as a string
     */
    static public String getSupportFileAbsolutePath(GlossaryFile p_supportFile, String companyId)
            throws AmbassadorDwUpException
    {
        GlossaryManager mgr = null;
        try
        {
            mgr = ServerProxy.getGlossaryManager();
            return mgr.getFilename(p_supportFile, companyId);
        }
        catch (Exception e)
        {
            CATEGORY.error("DownloadApi::getSupportFileAbsolutePath(). ", e);
            throw new AmbassadorDwUpException(e);
        }
    }

    /**
     * Creates a unique secondary download file name. SecondaryTargetFiles(STF)
     * are unextracted files. Since they are unextracted, we have to append the
     * STF-id to the filename to be able to identify the file during upload and
     * save it to the proper location.
     * 
     * @param p_pageId -
     *            the page id.
     * @param p_path -
     *            the path (or filename) from which to build the new filename
     * @param p_suffix -
     *            the pageId suffix: PRIMARY_SUFFIX or SECONDARY_SUFFIX
     * @return a new filename (minus the path - if present)
     */
    static public String makeUnextractedFileName(Long p_pageId, String path,
            String p_suffix, DownloadParams p_downloadParams, String p_taskId)
    {
        StringBuffer uniqueName = new StringBuffer();
        File f = new File(path);
        int idx = f.getName().lastIndexOf('.');
        String name = f.getName();

        uniqueName.append(idx > 0 ? name.substring(0, idx) : name);
        uniqueName.append(FILE_NAME_BREAK);
        uniqueName.append(p_pageId.toString());
        uniqueName.append(p_suffix);
        uniqueName.append(FILE_NAME_BREAK);
        uniqueName.append(p_taskId);

        if (idx > 0)
        {
            uniqueName.append(name.substring(idx, name.length()));
        }

        return uniqueName.toString();
    }

    static public String makeUnextractedFilePath(PrimaryFile p_pf,
            Long p_fileId, DownloadParams p_downloadParams, String p_taskId)
    {
        UnextractedFile uf = (UnextractedFile) p_pf;
        return makePTFParentPath(p_downloadParams)
                + DownloadHelper.makeUnextractedFileName(p_fileId, uf
                        .getStoragePath(), PRIMARY_SUFFIX, p_downloadParams, p_taskId);
    }

    static public String makeSrcDocName(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_SRCDOC);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(".");
        sb.append(FILE_EXT_RTF_NO_DOT);

        return sb.toString();
    }

    static public String makeTmDocName(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_TMDOC);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(".");
        sb.append(FILE_EXT_RTF_NO_DOT);

        return sb.toString();
    }

    static public String makeTagInfoDocName(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_TAGDOC);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(".");
        sb.append(FILE_EXT_RTF_NO_DOT);

        return sb.toString();
    }

    static public String makeTermDocName(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_TERMDOC);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(".");
        sb.append(FILE_EXT_RTF_NO_DOT);

        return sb.toString();
    }

    static public String makeBinResFname(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_BINRES);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(FILE_EXT_BIN);

        return sb.toString();
    }

    static public String makeResIdxFname(OfflinePageData p_OPD)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(WC_PREFIX_IDXRES);
        sb.append(p_OPD.getPageId());
        sb.append("_");
        sb.append(p_OPD.getTaskId());
        sb.append(".");
        sb.append(FILE_EXT_TXT);

        return sb.toString();
    }
}

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

package com.globalsight.everest.page.pageexport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameterPersistenceManager;
import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.ling.StandardMerger;
import com.globalsight.cxe.adapter.msoffice.EventFlowXmlParser;
import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.corpus.CorpusManagerWLRemote;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.RenderingOptions;
import com.globalsight.everest.page.SnippetPageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.pageexport.style.StyleFactory;
import com.globalsight.everest.page.pageexport.style.StyleUtil;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.RemovedPrefixTag;
import com.globalsight.everest.tuv.RemovedSuffixTag;
import com.globalsight.everest.tuv.RemovedTag;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvMerger;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.FontFaceModifier;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.merger.html.HtmlPreviewerHelper;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.corpusinterface.TuvMapping;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageexport.DeleteLeverageMatchPersistenceCommand;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.Base64;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.SegmentUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.TextNode;
import com.globalsight.util.modules.Modules;

/**
 * ExportHelper performs the export process. It is normally triggered by the
 * ExportMDB, but also has methods to support dynamic preview.
 * 
 * The process is basically generating the export GXML using the export
 * template. The generated GXML will have the updated segments. Note that "Link
 * Management" is not performed during export. NOTE: This used to be the old
 * ExportMessageListener, but other classes started using its public methods.
 * This was refactored to be a separate helper class which can be invoked either
 * by the ExportMDB or other classes that need to use it directly.
 */
@SuppressWarnings("deprecation")
public class ExportHelper
{
    static private final org.apache.log4j.Logger s_logger = org.apache.log4j.Logger
            .getLogger(ExportHelper.class);

    private static final String AND = "&";
    private static final String EQUALS = "=";

    // The page information to be exported.
    private Page m_page = null;
    private SourcePage m_sourcePage = null;
    private FileProfile m_fp = null;
    private TranslationMemoryProfile m_tmp = null;
    private String m_format = null;

    /**
     * Key: String [sourcePageId-localeId] Value: TUV ArrayList
     */
    @SuppressWarnings("rawtypes")
    private Map<String, ArrayList> cachedTuvs = new HashMap<String, ArrayList>();
    private SecondaryTargetFile m_secondaryTargetFile = null;
    private int m_genericPageType = -1;
    private String srcHtmlText;

    private String m_userid;
    private EditorState m_editorState;
    private HashMap<String, String> m_gsColorMap;
    private List<Tuv> m_sourceTuvs;
    private List<Tuv> m_targetTuvs;

    @SuppressWarnings("rawtypes")
    private Vector m_excludedItemTypes;
    private MatchTypeStatistics m_matchTypes;

    private String m_color100;

    private String m_colorIce;

    private String m_colorNon;

    private static String REGEX_BPT = "<bpt[^>]*i=\"([^\"]*)\"[^>]*>";
    private static String REGEX_BPT_ALL = "<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>[\\d\\D]*<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    private static String REGEX_BPT_ALL_SUB = "<bpt[^>]*i=\"{0}\"[^>]*>.*</bpt>.*<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    private static String REGEX_IT_END = "<it[^>]*pos=\"end\"[^>]*>([^<]*)</it>";
    private static String REGEX_IT_START = "<it[^>]*pos=\"begin\"[^>]*>[^<]*</it>";
    private static String REGEX_IT = "<it [^>]*>([^<]*)</it>";
    private static String XML_SPACE_PRESERVE = "xml:space=&quot;preserve&quot;";

    public static String GS_COLOR_S = "((GS_COLOR_START)";
    public static String GS_COLOR_E = "((GS_COLOR_END)";
    public static String GS_COLOR_S_100MATCH = GS_COLOR_S + "(100))";
    public static String GS_COLOR_E_100MATCH = GS_COLOR_E + "(100))";
    public static String GS_COLOR_S_ICEMATCH = GS_COLOR_S + "(ICE))";
    public static String GS_COLOR_E_ICEMATCH = GS_COLOR_E + "(ICE))";
    public static String GS_COLOR_S_NONMATCH = GS_COLOR_S + "(NON))";
    public static String GS_COLOR_E_NONMATCH = GS_COLOR_E + "(NON))";

    public static String GS_COLOR_S_P = "((GS_COLOR_START)" + "(_color_))";
    public static String GS_COLOR_E_P = "((GS_COLOR_END)" + "(_color_))";

    public static String RE_GS_COLOR_S = "\\(\\(GS_COLOR_START\\)\\((.+?)\\)\\)";
    public static String RE_GS_COLOR_E = "\\(\\(GS_COLOR_END\\)\\((.+?)\\)\\)";
    public static String RE_GS_COLOR_DEFINE = "\\(\\(GS_COLOR_DEFINE\\)\\((.+?)\\)\\)";

    /**
     * Constructor called ONLY for preview dynamic content.
     */
    public ExportHelper()
    {
    }

    /**
     * Perform the export for preview based on the specified list of tuvs. The
     * export template is populated with segments for given tuv ids and the rest
     * of placeholders in the template will be populated by a constant string.
     * 
     * @return The string representation of request information for a preview.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for preview.
     * @param p_pageId
     *            - The id of the page where the tuvs belong to.
     * @param p_tuvIds
     *            - A collection of tuv ids used for preview.
     * @param p_uiLocale
     *            - The UI locale of CAP which will be used by CXE for
     *            displaying the preview screen.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    @SuppressWarnings("rawtypes")
    public String exportForPreview(long p_pageId, List p_tuvIds,
            String p_uiLocale) throws PageException
    {
        String info = null;

        try
        {
            m_page = ServerProxy.getPageManager().getTargetPage(p_pageId);
            m_sourcePage = ((TargetPage) m_page).getSourcePage();
            ExportParameters ep = new ExportParameters((TargetPage) m_page);
            info = exportForPreview(ep, p_tuvIds, p_uiLocale);
        }
        catch (PageException pe)
        {
            s_logger.error("getPreviewInfo ", pe);
            throw pe;
        }
        catch (Exception e)
        {
            s_logger.error("ExportMessageListener :: Failed to export"
                    + " for preview: " + p_pageId + "\n", e);

            String[] args =
            { Long.toString(p_pageId) };
            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_PREVIEW_INFO, args, e);
        }

        return info == null ? "" : info;
    }

    /**
     * Start the page export process.
     * 
     * @param p_hashtable
     *            -- contains information needed for export
     */
    @SuppressWarnings("rawtypes")
    public void export(Hashtable p_hashtable)
    {
        try
        {
            startExport(p_hashtable);
        }
        catch (Exception e)
        {
            printError("Failed to export ", e);

            try
            {
                if (m_secondaryTargetFile != null)
                {
                    ServerProxy.getSecondaryTargetFileManager().updateState(
                            m_secondaryTargetFile.getIdAsLong(),
                            SecondaryTargetFileState.EXPORT_FAIL);
                }
                else if (m_page instanceof TargetPage)
                {
                    PageExportException pee = new PageExportException(
                            PageExportException.MSG_FAILED_TO_EXPORT_PAGE,
                            null, e);

                    getPageEventObserver().notifyExportFailEvent(
                            (TargetPage) m_page, pee.serialize());
                }
            }
            catch (Exception ex)
            {
                printError("Failed to update the state to EXPORT_FAILED for ",
                        ex);
            }
        }
    }

    /**
     * Start the page export process.
     * 
     * @param p_hashtable
     *            -- contains information needed for export
     */
    @SuppressWarnings("rawtypes")
    public void export(List<Hashtable> p_hashtable)
    {
        try
        {
            startExport(p_hashtable);
        }
        catch (Exception e)
        {
            printError("Failed to export ", e);

            try
            {
                if (m_secondaryTargetFile != null)
                {
                    ServerProxy.getSecondaryTargetFileManager().updateState(
                            m_secondaryTargetFile.getIdAsLong(),
                            SecondaryTargetFileState.EXPORT_FAIL);
                }
                else if (m_page instanceof TargetPage)
                {
                    PageExportException pee = new PageExportException(
                            PageExportException.MSG_FAILED_TO_EXPORT_PAGE,
                            null, e);

                    getPageEventObserver().notifyExportFailEvent(
                            (TargetPage) m_page, pee.serialize());
                }
            }
            catch (Exception ex)
            {
                printError("Failed to update the state to EXPORT_FAILED for ",
                        ex);
            }
        }
    }

    public String exportForPdfPreview(long p_pageId, String p_targetEncoding,
            boolean p_keepGsTags, boolean p_isIncontextReview, boolean isTarget)
            throws RemoteException, TranscoderException,
            UnsupportedEncodingException
    {
        if (isTarget)
        {
            Page pageObj = ServerProxy.getPageManager().getTargetPage(p_pageId);
            m_page = pageObj;
            m_sourcePage = ((TargetPage) m_page).getSourcePage();
        }
        else
        {
            Page pageObj = ServerProxy.getPageManager().getSourcePage(p_pageId);
            m_page = pageObj;
            m_sourcePage = ((SourcePage) m_page);
        }

        // Touch to load all TUs of this source page for performance.
        try
        {
            SegmentTuUtil.getTusBySourcePageId(m_sourcePage.getId());
        }
        catch (Exception ex)
        {
        }
        String page = populatePage(getExportTemplate(),
                getSegments(m_page.getGlobalSightLocale()),
                m_page.getGlobalSightLocale(), true, p_isIncontextReview, null);
        DiplomatAPI diplomat = new DiplomatAPI();
        diplomat.setPreview(true);
        byte[] mergeResult = diplomat.merge(page, p_targetEncoding,
                p_keepGsTags);
        String s = new String(mergeResult, p_targetEncoding);
        StringBuffer sb = new StringBuffer(s);
        int pos1 = 0;
        while (pos1 >= 0)
        {
            pos1 = sb.indexOf("&nbsp;", pos1);
            s_logger.debug("The position of the &nbsp; "
                    + "in lam_merge.txt is: " + pos1);
            if (pos1 > 0)
            {
                sb.replace(pos1, pos1 + 6, " ");
            }
        }
        int pos2 = 0;
        while (pos2 >= 0)
        {
            pos2 = sb.indexOf("&nbsp", pos2);
            s_logger.debug("The position of the &nbsp "
                    + "in lam_merge.txt is: " + pos2);
            if (pos2 > 0)
            {
                sb.replace(pos2, pos2 + 5, " ");
            }
        }

        return sb.toString();

    }

    /**
     * For adobe PDF file Preview
     * 
     * @param p_pageId
     * @param p_targetEncoding
     * @param p_keepGsTags
     * @return
     * @throws RemoteException
     * @throws TranscoderException
     * @throws UnsupportedEncodingException
     */
    public String exportForPdfPreview(long p_pageId, String p_targetEncoding,
            boolean p_keepGsTags) throws RemoteException, TranscoderException,
            UnsupportedEncodingException
    {
        return exportForPdfPreview(p_pageId, p_targetEncoding, p_keepGsTags,
                false, true);
    }

    // ///////////////////
    // Private Methods //
    // ///////////////////

    /**
     * Return the absolute path of the file to be exported.
     */
    private String absolutePathOfStf(String p_stfRelativePath, String companyId)
            throws Exception
    {
        // String fileStorageRoot = SystemConfiguration.getInstance().
        // getStringParameter(SystemConfigParamNames.FILE_STORAGE_DIR);
        String fileStorageRoot = AmbFileStoragePathUtils
                .getFileStorageDirPath(companyId);

        StringBuffer sb = new StringBuffer();
        sb.append(fileStorageRoot);

        if (!fileStorageRoot.endsWith(File.separator))
        {
            sb.append(File.separator);
        }

        sb.append(AmbFileStoragePathUtils.STF_SUB_DIRECTORY);
        sb.append(File.separator);
        sb.append(p_stfRelativePath);

        return sb.toString();
    }

    /**
     * Return the absolute path for this un-extracted file. Should use the
     * NativeFileManager for this, however, that would be another RMI call for
     * something simple.
     */
    private String getAbsolutePathOfUnextractedFile(String p_relativePath)
            throws Exception
    {
		long jobCompanyId = ((TargetPage) m_page).getSourcePage()
				.getCompanyId();
		String fileStorageRoot = AmbFileStoragePathUtils
				.getFileStorageDirPath(jobCompanyId);

		StringBuffer sb = new StringBuffer();
        sb.append(fileStorageRoot);
        if (!fileStorageRoot.endsWith(File.separator))
        {
            sb.append(File.separator);
        }
        sb.append(AmbFileStoragePathUtils.UNEXTRACTED_SUB_DIRECTORY);
        sb.append(File.separator);
        sb.append(p_relativePath);

        return sb.toString();
    }

    // append export info to the string buffer
    private void appendExportInfo(StringBuffer p_sb, String p_parameterName,
            String p_parameterValue)
    {
        p_sb.append(AND);
        writeParameterValuePair(p_sb, p_parameterName, p_parameterValue);
    }

    private void deleteLeverageMatches(TargetPage p_tp)
            throws PersistenceException
    {
        Connection connection = null;

        try
        {
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            DeleteLeverageMatchPersistenceCommand cmd = new DeleteLeverageMatchPersistenceCommand(
                    p_tp.getSourcePage().getId(), p_tp.getLocaleId());

            cmd.persistObjects(connection);

            connection.commit();
        }
        catch (Exception e)
        {
            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
                throw new PersistenceException(e);
            }
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    PersistenceService.getInstance().returnConnection(
                            connection);
                }
            }
            catch (Exception e)
            {
                s_logger.error("Unable to return connection to pool");
            }
        }
    }

    /**
     * Deletes the task tuvs and leverage matches for a localized target page.
     */
    private void deleteLevMatches()
    {
        try
        {
            // if this is the final export - delete the other
            // versions of the segments
            if (m_page.getPageState().equals(PageState.LOCALIZED))
            {
                deleteLeverageMatches((TargetPage) m_page);
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Unable to delete leverage matches (ignoring error)", e);
        }
    }

    /*
     * delete the task tuvs and leverage matches for a localized target page.
     */
    private void deleteLevMatches(Workflow p_wf)
    {
        try
        {
            // if this is the final export - delete the other versions
            // of the segments
            if (p_wf.getState().equals(Workflow.LOCALIZED))
            {
                // just get the target pages associated with an extracted file
                @SuppressWarnings("rawtypes")
                List tps = p_wf.getTargetPages(ExtractedFile.EXTRACTED_FILE);
                for (int i = 0, max = tps.size(); i < max; i++)
                {
                    deleteLeverageMatches((TargetPage) tps.get(i));
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error("Unable to delete leverage matches "
                    + "for all pages in workflow (ignoring error)", e);
        }
    }

    /**
     * Starts the export process by: getting the template and segments,
     * populating the page, and sending it to CXE.
     */
    private void export(ExportParameters p_exportParameters,
            Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, String p_exportBatchId) throws PageException
    {
        try
        {
            GlobalSightLocale targetLocale = null;
            MessageData messageData = MessageDataFactory
                    .createFileMessageData();
            boolean isUnextracted = false;
            // name of the file being exported
            String exportingFileName = null;
            int sourcePageBomType = ExportConstants.NO_UTF_BOM;
            boolean isTabstrip = false;
            HashMap<String, String> mapOfSheetTabs = new HashMap<String, String>();
            if (m_secondaryTargetFile == null)
            {
                targetLocale = m_page.getGlobalSightLocale();

                // determine if the page is associated with an extracted or
                // unextracted primary file
                if (m_page.getPrimaryFileType() == UnextractedFile.UNEXTRACTED_FILE)
                {
                    isUnextracted = true;
                    UnextractedFile uf = (UnextractedFile) m_page
                            .getPrimaryFile();
                    String filePath = getAbsolutePathOfUnextractedFile(uf
                            .getStoragePath());
                    int index = filePath.lastIndexOf(targetLocale.toString()
                            + File.separator
                            + ((TargetPage) m_page).getSourcePage()
                                    .getGlobalSightLocale().toString());

                    exportingFileName = filePath.substring(index
                            + targetLocale.toString().length() + 1);
                    sourcePageBomType = ExportConstants.NO_UTF_BOM;
                    index = exportingFileName.indexOf(File.separator);
                    exportingFileName = exportingFileName.substring(index + 1);
                    messageData.copyFrom(new File(filePath));
                }
                else
                // extracted
                {
                    exportingFileName = m_page.getExternalPageId();
                    isTabstrip = exportingFileName.startsWith("(tabstrip)");
                    int index = exportingFileName.indexOf(File.separator);
                    exportingFileName = exportingFileName.substring(index + 1);
                    sourcePageBomType = m_sourcePage.getBOMType();
                    PageTemplate pageTemplate = getExportTemplate();
                    pageTemplate.setTabsTrip(isTabstrip);
					boolean srcAsTrg = ServerProxy.getJobHandler()
							.getJobById(m_sourcePage.getJobId()).isBlaiseJob();
					if (srcAsTrg) {
						pageTemplate.setXlfSrcAsTrg(1);
					} else {
						pageTemplate.setXlfSrcAsTrg(p_exportParameters
								.getXlfSrcAsTrg());
					}
                    String page = populatePage(pageTemplate,
                            getSegments(m_page.getGlobalSightLocale()),
                            targetLocale, false, false, null);
                    if (isTabstrip)
                    {
                        String mainFileName = buildMainFileName(
                                exportingFileName, targetLocale.getLanguage()
                                        + "_" + targetLocale.getCountry());
                        mapOfSheetTabs = pageTemplate.getMapOfSheetTabs();
                        modifyMainFile(mainFileName, mapOfSheetTabs);
                    }
                    BufferedOutputStream bos = new BufferedOutputStream(
                            messageData.getOutputStream());

                    OutputStreamWriter osw = new OutputStreamWriter(bos,
                            ExportConstants.UTF8);

                    osw.write(page, 0, page.length());
                    osw.close();
                }

                // takes off the first part of the directory name which is the
                // locale
            }
            else
            {
                targetLocale = m_secondaryTargetFile.getWorkflow()
                        .getTargetLocale();
                String companyId = String.valueOf(m_secondaryTargetFile
                        .getWorkflow().getJob().getCompanyId());
                String fileName = absolutePathOfStf(
                        m_secondaryTargetFile.getStoragePath(), companyId);
                File file = new File(fileName);
                if (FileUtil.isNeedBOMProcessing(fileName))
                {
                    String encoding = FileUtil.guessEncoding(file);
                    if (FileUtil.UTF8.equals(encoding))
                    {
                        sourcePageBomType = ExportConstants.UTF8_WITH_BOM;
                    }
                    else if (FileUtil.UTF16LE.equals(encoding))
                    {
                        sourcePageBomType = ExportConstants.UTF16_LE;
                    }
                    else if (FileUtil.UTF16BE.equals(encoding))
                    {
                        sourcePageBomType = ExportConstants.UTF16_BE;
                    }
                }
                messageData.copyFrom(new File(fileName));
                isUnextracted = true;

                // takes off the first two parts of the directory name which is
                // part of the storage path
                exportingFileName = m_secondaryTargetFile.getStoragePath();
                int index = exportingFileName.indexOf(File.separator);
                exportingFileName = exportingFileName.substring(index + 1);
                index = exportingFileName.indexOf(File.separator);
                exportingFileName = exportingFileName.substring(index + 1);
            }

            // For "Exported files in one folder" issue

            exportingFileName = ExportHelper.transformExportedFilename(
                    exportingFileName, targetLocale.toString());

            sendToCXE(messageData, p_exportParameters, targetLocale,
                    p_pageCount, p_pageNum, p_docPageCount, p_docPageNum,
                    p_exportBatchId, isUnextracted, exportingFileName,
                    sourcePageBomType);
        }
        catch (PageException pe)
        {
            throw pe;
        }
        catch (Exception e)
        {
            throw new PageException(PageException.MSG_FAILED_TO_CONNECT_TO_CXE,
                    getExceptionArgument(), e);
        }
        finally
        {
            switch (m_genericPageType)
            {
                case PageManager.SECONDARY_TARGET_FILE:
                    deleteLevMatches(m_secondaryTargetFile.getWorkflow());
                    break;

                case PageManager.TARGET_PAGE:
                    deleteLevMatches();
                    break;

                default:
                    break;
            }
        }
    }

    private String getSourceXml(String fullHtmlName)
    {
        BufferedReader br = null;
        String xml = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fullHtmlName), "utf-8"));
            String s = br.readLine();
            StringBuilder srcHtml = new StringBuilder();
            StringBuilder srcXML = new StringBuilder();
            boolean isXml = false;
            ArrayList<String> xmls = new ArrayList<String>();
            while (s != null)
            {
                srcHtml.append(s);
                if ((s.indexOf("<xml") != -1) && (s.indexOf("</xml>") != -1))
                {
                    // Fix for GBS-1744
                    if (s.indexOf("<xml") > s.indexOf("</xml>"))
                    {
                        srcXML.append(s);
                        srcXML.append("\n");
                    }
                }
                else
                {
                    if (s.indexOf("<xml") != -1)
                    {
                        srcXML.append(s);
                        srcXML.append("\n");
                        isXml = true;
                    }
                    if (s.indexOf("</xml>") != -1)
                    {
                        srcXML.append(s);
                        srcXML.append("\n");
                        xmls.add(srcXML.toString());
                        srcXML = new StringBuilder();
                        isXml = false;
                    }
                    if (isXml && s.indexOf("<xml") == -1)
                    {
                        srcXML.append(s);
                        srcXML.append("\n");
                    }
                }
                srcHtml.append("\n");
                s = br.readLine();

            }

            for (int i = 0; i < xmls.size(); i++)
            {
                xml = xmls.get(i);
                if (xml.indexOf("<x:ExcelWorkbook>") != -1
                        && xml.indexOf("<x:ExcelWorksheets>") != -1)
                {
                    break;
                }
                if (i == xmls.size() - 1)
                {
                    xml = null;
                }
            }
            srcHtmlText = srcHtml.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return xml;
    }

    private String transform(String xml, HashMap<String, String> map)
    {
        Set<Entry<String, String>> set = map.entrySet();
        Iterator<Entry<String, String>> it = set.iterator();
        while (it.hasNext())
        {
            Entry<String, String> entry = it.next();
            int hrefIndex = xml.indexOf(entry.getKey());
            String startToHref = xml.substring(0, hrefIndex);
            int startNameIndex = startToHref.lastIndexOf("<x:Name>");
            int endNameIndex = startToHref.lastIndexOf("</x:Name>");
            xml = xml.replace(
                    startToHref.substring(startNameIndex, endNameIndex) + "<",
                    "<x:Name>" + entry.getValue() + "<");
        }
        return xml;

    }

    private void writeFile(String s, String fileName)
    {
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                    fileName), "UTF-8"));
            pw.write(s);
            pw.flush();
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }
        }
    }

    private void modifyMainFile(String fullHtmlName,
            HashMap<String, String> mapOfTabstrip)
    {
        String srcXML = getSourceXml(fullHtmlName);
        String trgXML = transform(srcXML, mapOfTabstrip);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(trgXML);
        }
        int startIndex = srcHtmlText.indexOf(srcXML);
        String trgHtml = srcHtmlText.substring(0, startIndex) + trgXML
                + srcHtmlText.substring(startIndex + srcXML.length());
        writeFile(trgHtml, fullHtmlName);
    }

    private String buildMainFileName(String exportingFileName,
            String targetLocale)
    {
        StringBuffer convDir = new StringBuffer(SystemConfiguration
                .getInstance().getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR));
        EventFlowXmlParser parser = new EventFlowXmlParser(getEventFlowXML());
        try
        {
            parser.parse();
        }
        catch (Exception e)
        {
            s_logger.error("cannot parse event flow XML", e);
        }
        String formatName = parser.getSourceFormatName().toLowerCase();
        Element categoryElement = parser
                .getCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        String relSafeName = parser.getCategoryDaValue(categoryElement,
                "relSafeName")[0];
        relSafeName = relSafeName.substring(0,
                relSafeName.lastIndexOf(File.separator) - "_files".length())
                + ".html";
        if (formatName != null && ("excel2003".equals(formatName)))
        {
            convDir = new StringBuffer(SystemConfiguration.getInstance()
                    .getStringParameter(
                            SystemConfigParamNames.MSOFFICE2003_CONV_DIR));
        }

        convDir.append(File.separator);
        convDir.append("excel");
        convDir.append(File.separator);
        convDir.append(targetLocale);
        convDir.append(File.separator);
        // convDir.append(batchId.substring(jobName.length()));
        // convDir.append(exportingFileName.substring(exportingFileName.lastIndexOf(File.separator)+1));
        convDir.append(relSafeName);
        return convDir.toString();
    }

    // For "Exported files in one folder" issue
    public static String transformExportedFilename(String filename,
            String locale)
    {
        String s1 = null;
        String s3 = null;
        int indexOfDot = filename.lastIndexOf(".");

        if (indexOfDot > 0)
        {
            s1 = filename.substring(0, indexOfDot);
            s3 = filename.substring(indexOfDot);
        }
        else
        {
            s1 = filename;
            s3 = "";
        }

        SystemConfiguration config = SystemConfiguration.getInstance();
        if (!"export"
                .equalsIgnoreCase(config
                        .getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE)))
        {

            return filename;
        }
        return s1 + "_" + locale + s3;
    }

    /**
     * Populate the template with segments of the given tuvIds and replace the
     * rest of placeholders with a default GS string. Then prepare the request
     * info as a single line to be sent back to the servlet.
     * 
     * @param p_exportParameters
     * @param p_tuvIds
     *            The list of tuv ids that are in the page.
     * @param p_uiLocale
     *            The locale of the UI.
     */
    @SuppressWarnings("rawtypes")
    private String exportForPreview(ExportParameters p_exportParameters,
            List p_tuvIds, String p_uiLocale) throws PageException
    {
        GlobalSightLocale targetLocale = m_page.getGlobalSightLocale();
        // Touch to load all TUs of this source page for performance.
        try
        {
            if (m_page instanceof TargetPage)
            {
                SegmentTuUtil.getTusBySourcePageId(((TargetPage) m_page)
                        .getSourcePage().getId());
            }
        }
        catch (Exception ex)
        {
        }
        String page = populatePage(getExportTemplate(),
                getSegments(m_page.getGlobalSightLocale()), targetLocale, true,
                false, p_tuvIds);

        // since only one file is exported at a time for preview, there is
        // no relevant batch info.
        ExportBatchInfo ebi = new ExportBatchInfo();
        String requestInfo = getExportRequestInfo(page, p_exportParameters,
                targetLocale, ebi);

        StringBuffer sb = new StringBuffer(requestInfo);

        try
        {
            sb.append(AND);
            sb.append(URLEncoder.encode(ExportConstants.UI_LOCALE,
                    ExportConstants.UTF8));
            sb.append(EQUALS);
            sb.append(URLEncoder.encode(p_uiLocale, ExportConstants.UTF8));
        }
        catch (Exception ex)
        {
            printError("ExportForPreview for ", ex);
            throw new PageException("Problem while encoding url ",
                    getExceptionArgument(), ex);
        }

        return sb.toString();
    }

    /**
     * Gets exported xml target file according to page id.
     * 
     * @param pageId
     * @return
     * @throws IOException
     */
    public File getTargetXmlPage(long pageId, int p_eventValue)
            throws IOException
    {
        return getTargetXmlPage(pageId, p_eventValue, false);
    }

    public String getTargetXmlContent(long pageId, int p_eventValue,
            boolean isPreview) throws IOException
    {
        TargetPage targetPage = HibernateUtil.get(TargetPage.class, pageId);
        m_page = targetPage;
        Assert.assertNotNull(targetPage, "target page");
        m_sourcePage = targetPage.getSourcePage();
        // Touch to load all TUs of this source page for performance.
        try
        {
            SegmentTuUtil.getTusBySourcePageId(m_sourcePage.getId());
        }
        catch (Exception ex)
        {
        }

        String eventFlowXml = m_sourcePage.getRequest().getEventFlowXml();

        PageTemplate pageTemplate = getExportTemplate();
        pageTemplate.setTabsTrip(false);
        String page = populatePage(pageTemplate,
                getSegments(m_page.getGlobalSightLocale()),
                targetPage.getGlobalSightLocale(), isPreview, false, null);
        File temp = File.createTempFile("GSTargetPage", ".xml");
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(temp), "UTF-8");
        // FileWriter out = new FileWriter(temp);
        writer.write(page);
        writer.flush();
        writer.close();

        eventFlowXml = fixEventFlowXml(eventFlowXml);
        CxeMessage cxeMessage = new CxeMessage(
                CxeMessageType.getCxeMessageType(p_eventValue));
        cxeMessage.setEventFlowXml(eventFlowXml);
        cxeMessage.setMessageData(new FileMessageData(temp.getPath()));
        StandardMerger merger = new StandardMerger(cxeMessage, null);

        String content = null;
        if (isPreview)
        {
            content = merger.getPreviewContent();
        }
        else
        {
            content = merger.getContent();
        }
        temp.delete();

        return content;
    }

    public File getTargetXmlPage(long pageId, int p_eventValue,
            boolean isPreview) throws IOException
    {
        String content = getTargetXmlContent(pageId, p_eventValue, isPreview);

        File xmlFile = File.createTempFile("GSTarget", ".xml");
        OutputStreamWriter outwriter = new OutputStreamWriter(
                new FileOutputStream(xmlFile), "UTF-8");
        // FileWriter output = new FileWriter(xmlFile);
        outwriter.write(content);
        outwriter.flush();
        outwriter.close();
        return xmlFile;

    }

    /**
     * Set the right target locale in eventFlowXml.
     * 
     * @param eventFlowXml
     * @return
     */
    private String fixEventFlowXml(String eventFlowXml)
    {
        try
        {
            com.globalsight.cxe.util.EventFlowXmlParser parser = new com.globalsight.cxe.util.EventFlowXmlParser();
            parser.parse(eventFlowXml);

            String targetLocale = m_page.getGlobalSightLocale().toString();
            String targetLocaleFromXml = parser.getTargetLocale();

            if (!targetLocale.equalsIgnoreCase(targetLocaleFromXml))
            {
                com.globalsight.cxe.util.EventFlowXmlParser
                        .setSingleElementValue(
                                parser.getSingleElement("target"), "locale",
                                targetLocale);

                parser.reconstructEventFlowXmlStringFromDOM();
                eventFlowXml = parser.getEventFlowXml();
            }

            return eventFlowXml;
        }
        catch (Exception e)
        {
            // just log here
            s_logger.error("Error when fix event flow xml in ExportHelper", e);
            return eventFlowXml;
        }
    }

    /*
     * Get the EventFlowXML file that was sent during import.
     */
    private String getEventFlowXML()
    {
        return m_secondaryTargetFile == null ? m_sourcePage.getRequest()
                .getEventFlowXml() : m_secondaryTargetFile.getEventFlowXml();
    }

    // get the page's external id as a common exception argument.
    private String[] getExceptionArgument()
    {
        String[] exceptionMsgArgument =
        { m_secondaryTargetFile == null ? m_page.getExternalPageId()
                : m_secondaryTargetFile.getStoragePath() };

        return exceptionMsgArgument;
    }

    // create the encoded info to be sent to CXE
    private String getExportRequestInfo(String p_fileName,
            ExportParameters p_exportParameters,
            GlobalSightLocale p_targetLocale, ExportBatchInfo p_exportBatchInfo)
            throws PageException
    {
        StringBuffer sb = new StringBuffer();

        try
        {
            String codeSet = p_exportParameters.getExportCodeset();
            String cxeRequestType = p_exportParameters.getExportType();
            String eventFlowXml = getEventFlowXML();
            String targetLocale = p_targetLocale.toString();
            String messageId = String
                    .valueOf(m_secondaryTargetFile == null ? m_page.getId()
                            : m_secondaryTargetFile.getId());
            TargetPage targetPage = null;

            if (m_page != null)
            {
                targetPage = (TargetPage) m_page;

                // If the page is of Microsoft Office Type enforce the
                // codeset to be UTF-8.
                if (SourcePage.isMicrosoftOffice(targetPage.getSourcePage()))
                {
                    codeSet = "UTF-8";
                }
                // If the page is a FrameMaker file, enforce MacRoman
                // as the only codeset that is understood by the
                // Noonetime converter (and FrameMaker).
                else if (SourcePage.isFrameMaker(targetPage.getSourcePage()))
                {
                    codeSet = "MacRoman";
                }
            }

            String exportLocation = p_exportParameters.getExportLocation();
            String localeSubDir = p_exportParameters.getLocaleSubDir();

            // Export type (automatic, manual, or preview)
            startExportInfo(sb, ExportConstants.CXE_REQUEST_TYPE,
                    cxeRequestType);

            // event flow xml
            appendExportInfo(sb, ExportConstants.EVENT_FLOW_XML, eventFlowXml);

            // TargetLocale
            appendExportInfo(sb, ExportConstants.TARGET_LOCALE, targetLocale);

            // Target Charset
            appendExportInfo(sb, ExportConstants.TARGET_CODESET, codeSet);

            // MessageId
            appendExportInfo(sb, ExportConstants.MESSAGE_ID, messageId);

            // Gxml
            appendExportInfo(sb, ExportConstants.GXML, p_fileName);

            // export location
            appendExportInfo(sb, ExportConstants.EXPORT_LOCATION,
                    exportLocation);

            // locale sub dir
            appendExportInfo(sb, ExportConstants.LOCALE_SUBDIR, localeSubDir);

            // Add on the export batch info, page count, and page number
            appendExportInfo(sb, ExportConstants.EXPORT_BATCH_ID,
                    p_exportBatchInfo.exportBatchId);
            appendExportInfo(sb, ExportConstants.PAGE_COUNT,
                    p_exportBatchInfo.pageCount.toString());
            appendExportInfo(sb, ExportConstants.PAGE_NUM,
                    p_exportBatchInfo.pageNum.toString());

            return sb.toString();
        }
        catch (Exception ex)
        {
            printError("getExportRequestInfo for ", ex);
            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_EXPORT_REQUEST_INFO,
                    getExceptionArgument(), ex);
        }
    }

    /*
     * Get the export template in GXML format for a given page id. This could
     * return NULL if the page is NOT an extracted file.
     */
    private PageTemplate getExportTemplate()
    {
        PageTemplate result = null;

        // verify it is an extracted file.
        if (m_page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
        {
            s_logger.debug("Starting a transparent collection Export Template");
            ExtractedFile ef = (ExtractedFile) m_page.getPrimaryFile();
            result = ef.getPageTemplate(PageTemplate.TYPE_EXPORT);
        }

        return result;
    }

    // return the page data for a given source page id and page template.
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private String getPageData(PageTemplate p_pageTemplate)
            throws PageException
    {
        String pageData;

        try
        {
            Collection tp = ServerProxy.getPageManager()
                    .getTemplatePartsForSourcePage(m_sourcePage.getIdAsLong(),
                            p_pageTemplate.getTypeAsString());

            // ALWAYS set the template parts before getting the page data
            p_pageTemplate.setTemplateParts(new ArrayList(tp));

            boolean addDeleteEnabled = SystemConfiguration.getInstance()
                    .getBooleanParameter(
                            SystemConfigParamNames.ADD_DELETE_ENABLED);

            // assume this page has an extracted file since it
            // has reached this method.
            ExtractedSourceFile esf = (ExtractedSourceFile) m_sourcePage
                    .getPrimaryFile();

            if (m_genericPageType == PageManager.TARGET_PAGE
                    && addDeleteEnabled && esf.containGsTags())
            {
                p_pageTemplate = new SnippetPageTemplate(p_pageTemplate, m_page
                        .getGlobalSightLocale().toString());
            }

            p_pageTemplate.setTargetLocale(m_page.getGlobalSightLocale()
                    .getId());
            pageData = p_pageTemplate.getPageData(new RenderingOptions(
                    UIConstants.UIMODE_EXPORT, 0, 0));
        }
        catch (Exception e)
        {
            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_TEMPLATE_PARTS, null, e);
        }

        return pageData;
    }

    // get page event observer remote object.
    private PageEventObserver getPageEventObserver() throws PageException
    {
        PageEventObserver pageEventObserver = null;

        try
        {
            pageEventObserver = ServerProxy.getPageEventObserver();
        }
        catch (Exception ex)
        {
            throw new PageException(
                    PageException.MSG_FAILED_TO_LOCATE_PAGE_EVENT_OBSERVER,
                    null, ex);
        }

        return pageEventObserver;
    }

    /**
     * Get the segments of a page
     * 
     * @param p_locale
     *            the locale of the segments to get
     * @return List of Tuvs
     * @exception PageException
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private ArrayList getSegments(GlobalSightLocale p_locale)
            throws PageException
    {
        ArrayList segments = null;
        try
        {
            String key = m_sourcePage.getId() + "-" + p_locale.getId();
            segments = cachedTuvs.get(key);
            if (segments == null || segments.size() == 0)
            {
                SegmentTuUtil.getTusBySourcePageId(m_sourcePage.getId());
                segments = new ArrayList(ServerProxy.getTuvManager()
                        .getExportTuvs(m_sourcePage, p_locale));
                cachedTuvs.put(key, segments);
            }
        }
        catch (Exception ex)// GeneralException, TuvException, RemoteException
        {
            s_logger.error("cannot load target page segments", ex);

            throw new PageException(PageException.MSG_FAILED_TO_GET_TUVS,
                    getExceptionArgument(), ex);
        }

        return segments == null ? new ArrayList() : segments;
    }

    /**
     * If the page is not in the states defined here, it's valid. This is used
     * for determining indexing.
     */
    private boolean isValidState()
    {
        String state = m_page.getPageState();

        return (!PageState.ACTIVE_JOB.equals(state)
                && !PageState.EXPORTED.equals(state) && !PageState.IMPORT_SUCCESS
                    .equals(state));
    }

    /**
     * Populates the page by replacing place holders in the template with valid
     * segments. This method also populates the TM and Corpus if appropriate (at
     * first real export time)
     * 
     * @param p_template
     *            The page template to use for populating the page.
     * @param p_segments
     *            The localizable segments of the page.
     * @param p_targetLocale
     *            The locale to create a page in.
     * @param p_isPreview
     *            Specifies if this is for preview or not.
     * @param p_tuvIds
     *            This parameter only needs to be set if "p_isPreview=true",
     *            otherwise it can be NULL or an empty list.
     * 
     */
    @SuppressWarnings("rawtypes")
    private String populatePage(PageTemplate p_template, ArrayList p_segments,
            GlobalSightLocale p_targetLocale, boolean p_isPreview,
            boolean p_isIncontextReview, List p_tuvIds) throws PageException
    {
        TuvMappingHolder corpusMappings = null;
        Map<Long, TuvMapping> targetCorpusSegments = null;
        Map<Long, TuvMapping> sourceCorpusSegments = null;
        boolean populateTm = false;

        // def 10057
        // strip extra leading and trailing whitespaces
        // this must be called before populating TM
        long jobId = p_template.getSourcePage().getJobId();
        stripExtraSpaces(p_segments, jobId);

        p_segments = removeNotTranslateTag(p_segments);

        // Populate TM
        // do not populate TM for export source
        if ((!p_isPreview && isValidState()) && m_page instanceof TargetPage)
        {
            populateTm = true;
            s_logger.debug("Populating TM and getting corpus mappings.");

            corpusMappings = populateTm(p_targetLocale);
            targetCorpusSegments = corpusMappings
                    .getMappingsByLocale(p_targetLocale);
            sourceCorpusSegments = corpusMappings
                    .getMappingsByLocale(m_sourcePage.getGlobalSightLocale());
        }

        s_logger.debug("Populating template with target segments.");
        // Using cloned "template" to avoid adding extra spaces into TM.
        PageTemplate innerTemplate = new PageTemplate(p_template);
        populateTemplateWithUpdatedSegments(innerTemplate, p_segments,
                p_targetLocale, p_isPreview, p_isIncontextReview, p_tuvIds,
                targetCorpusSegments, true);
        // gxml is used to export (template maybe have been changed for java
        // properties)
        String gxml = getPageData(innerTemplate);

        // replace the locale in the gxml
        StringBuffer sb = new StringBuffer(gxml);
        int idx = sb.indexOf("locale=");
        String newLocale = "locale=\""
                + m_page.getGlobalSightLocale().toString() + "\"";
        sb.replace(idx, idx + newLocale.length(), newLocale);
        Logger.writeDebugFile("targetPage.xml", sb.toString());

        // recombine source page GXML if we're populating the corpus TM
        if (Modules.isCorpusInstalled()
                && (populateTm && (m_page instanceof TargetPage)))
        {
            try
            {
                CorpusManagerWLRemote corpusManager = ServerProxy
                        .getCorpusManager();
                Long srcPageCuvId = m_sourcePage.getCuvId();

                if (srcPageCuvId != null && srcPageCuvId.longValue() > 0)
                {
                    CorpusDoc sourceCorpusDoc = corpusManager
                            .getCorpusDoc(srcPageCuvId);

                    if (sourceCorpusDoc != null
                            && sourceCorpusDoc.isMapped() == false)
                    {
                        s_logger.debug("Populating template with source segments.");
                        ArrayList srcSegments = getSegments(m_sourcePage
                                .getGlobalSightLocale());
                        populateTemplateWithUpdatedSegments(p_template,
                                srcSegments,
                                m_sourcePage.getGlobalSightLocale(), false,
                                p_isIncontextReview, null,
                                sourceCorpusSegments, false);

                        s_logger.debug("Getting source page GXML.");
                        String srcGxml = getPageData(p_template);
                        Logger.writeDebugFile("sourcePage.xml", srcGxml);

                        // overwrite the original src GXML
                        s_logger.debug("Overwriting original source gxml.");
                        ServerProxy.getNativeFileManager().save(srcGxml,
                                ExportConstants.UTF8,
                                sourceCorpusDoc.getGxmlPath());

                        // add the source page mapping to the corpus
                        if (sourceCorpusSegments != null
                                && sourceCorpusSegments.size() > 0)
                        {
                            s_logger.debug("Mapping src segments to corpus doc.");
                            List<TuvMapping> sourceCorpusSegmentList = new ArrayList<TuvMapping>(
                                    sourceCorpusSegments.values());

                            corpusManager.mapSegmentsToCorpusDoc(
                                    sourceCorpusSegmentList, sourceCorpusDoc);
                        }
                        else
                        {
                            // nothing was saved to the TM
                            s_logger.debug("No src segments to map to corpus doc.");
                        }
                    }

                    // add the target page to the corpus, but not store target
                    // files
                    s_logger.debug("Adding target corpus doc.");
                    CorpusDoc targetCorpusDoc = corpusManager
                            .addNewTargetLanguageCorpusDoc(sourceCorpusDoc,
                                    p_targetLocale, gxml, null, false);

                    if (targetCorpusSegments != null
                            && targetCorpusSegments.size() > 0
                            && targetCorpusDoc != null)
                    {
                        s_logger.debug("Mapping target segments to corpus doc.");
                        List<TuvMapping> targetCorpusSegmentList = new ArrayList<TuvMapping>(
                                targetCorpusSegments.values());
                        corpusManager.mapSegmentsToCorpusDoc(
                                targetCorpusSegmentList, targetCorpusDoc);
                    }
                    else
                    {
                        s_logger.debug("No target segments to map.");
                    }

                    Long cuvId = m_page.getCuvId();
                    m_page = (TargetPage) HibernateUtil.get(TargetPage.class,
                            m_page.getId());
                    m_page.setCuvId(cuvId);
                    HibernateUtil.update(m_page);
                }
                else
                {
                    s_logger.info("can NOT get source page cuvId of ["
                            + m_sourcePage.getExternalPageId() + "]");
                }
            }
            catch (Exception e)
            {
                s_logger.error("Failed to populate corpus TM.", e);
            }

        }

        return gxml;
    }

    /**
     * Populates the given template with updated segments. The segments are
     * adjusted for font, and project_tm_tu id.
     * 
     * @param p_template
     * @param p_segments
     * @param p_locale
     * @param p_isPreview
     * @param p_tuvIds
     * @param p_corpusSegments
     * @param p_restoreSpacesForJavaProerties
     */
    @SuppressWarnings("rawtypes")
    private void populateTemplateWithUpdatedSegments(PageTemplate p_template,
            List p_segments, GlobalSightLocale p_locale, boolean p_isPreview,
            boolean p_isIncontextReview, List p_tuvIds, Map p_corpusSegments,
            boolean p_restoreSpacesForJavaProerties) throws PageException
    {
        boolean changeFont = determineIfNeedFontChange(p_locale);
        long jobId = p_template.getSourcePage().getJobId();

        try
        {
            String eventFlowXml = m_sourcePage.getRequest().getEventFlowXml();
            EventFlowXmlParser parser = new EventFlowXmlParser(eventFlowXml);
            parser.parse();

            m_format = parser.getSourceFormatType();

            long fileProfileId = parser.getFileProfileId();
            m_fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(fileProfileId, false);

            long l10nprofileId = m_fp.getL10nProfileId();
            L10nProfile l10nprofile = ServerProxy.getProjectHandler()
                    .getL10nProfile(l10nprofileId);
            m_tmp = l10nprofile.getTranslationMemoryProfile();
        }
        catch (Exception ex)
        {
        }

        boolean needToAddGsColor = needToAddColorTag(p_isPreview,
                p_isIncontextReview);
        boolean isInddOrIdml = isInddOrIdml();
        if (needToAddGsColor)
        {
            needToAddGsColor = initGsColor();
        }

        for (Iterator it = p_segments.iterator(); it.hasNext();)
        {
            Tuv segment = (Tuv) it.next();
            int element = segment.getGxmlElement().getType();

            if (element == GxmlElement.SEGMENT
                    || element == GxmlElement.LOCALIZABLE)
            {
                if (changeFont)
                {
                    FontFaceModifier.addWasToFontFace(segment, jobId);
                }

                // When export,revert white spaces for java property files.
                // Is properties file?
                boolean isJavaProperties = m_format.toLowerCase().endsWith(
                        "javaprop");
                if (p_restoreSpacesForJavaProerties && isJavaProperties)
                {
                    revertWhiteSpacesForJavaProperty(segment, m_fp.getId(),
                            m_format, jobId);
                }

                boolean isLocalizable = (element == GxmlElement.LOCALIZABLE);
                updateSegValue(p_template, segment, p_isPreview,
                        needToAddGsColor, p_isIncontextReview, p_tuvIds,
                        p_corpusSegments, isLocalizable, isInddOrIdml);
            }
        }

        // add Color define for mif
        if (needToAddGsColor && IFormatNames.FORMAT_MIF.equals(m_format)
                && m_targetTuvs != null && m_targetTuvs.size() > 0)
        {
            SourcePage sp = p_template.getSourcePage();
            long companyId = sp != null ? sp.getCompanyId() : Long
                    .parseLong(CompanyWrapper.getCurrentCompanyId());
            for (int i = 0, max = m_targetTuvs.size(); i < max; i++)
            {
                Tuv tuv = m_targetTuvs.get(i);
                Long tuid = tuv.getTu(jobId).getIdAsLong();
                String oldContent = p_template.getTuvContent(tuid);
                if (oldContent != null)
                {
                    String newContent = addGSColorDefine(oldContent);
                    p_template.insertTuvContent(tuid, newContent);
                    break;
                }
            }
        }
    }

    // GBS-2305: for the Office2010 filter, add xml:space="preserve" to <w:t>
    // and <t> tags. (Can handle <w:tab> case)
    private String addXmlSpacePreserve(String tuvContent)
    {
        if (!tuvContent.contains("&lt;w:t") && !tuvContent.contains("&lt;t"))
        {
            return tuvContent;
        }

        String startFlag = tuvContent.contains("&lt;w:t") ? "&lt;w:t" : "&lt;t";
        String endFlag = "&gt;";

        int startFlagLen = startFlag.length();
        int index = tuvContent.indexOf(startFlag);
        int len = tuvContent.length();
        String nextContent = tuvContent;
        String tagContent = null;
        StringBuffer sb = new StringBuffer();

        while (index != -1 && index + startFlagLen < len)
        {
            char nextChar = nextContent.charAt(index + startFlagLen);
            sb.append(nextContent.substring(0, index));
            sb.append(startFlag);
            StringBuffer temp = new StringBuffer(nextContent);
            StringIndex si = StringIndex.getValueBetween(temp, index,
                    startFlag, endFlag);
            tagContent = (si == null) ? null : si.value;
            nextContent = nextContent.substring(index + startFlagLen);

            if (tagContent == null)
            {
                break;
            }

            if (!tagContent.contains(XML_SPACE_PRESERVE)
                    && (Character.isWhitespace(nextChar) || nextChar == '&'))
            {
                sb.append(" xml:space=&quot;preserve&quot;");
            }

            index = nextContent.indexOf(startFlag);
            len = nextContent.length();
        }

        if (nextContent != null)
        {
            sb.append(nextContent);
        }

        tuvContent = sb.toString();
        return tuvContent;
    }

    /**
     * Determines based on the target locale whether the font face should change
     * for CJK languages.
     */
    private boolean determineIfNeedFontChange(GlobalSightLocale p_targetLocale)
    {
        boolean result = false;

        try
        {
            boolean overrideFontFace = SystemConfiguration
                    .getInstance()
                    .getBooleanParameter(
                            SystemConfigParamNames.OVERRIDE_FONT_FACE_ON_EXPORT);

            if (overrideFontFace)
            {
                // add WAS to font face if the target language is CJK
                String lang = p_targetLocale.getLanguageCode();
                if (lang.equals("ja") || lang.equals("zh") || lang.equals("ko"))
                {
                    result = true;
                }
            }
        }
        catch (GeneralException ge)
        {
            s_logger.error("Failed to retrieve the override font face system parameter.  "
                    + "The page will be exported with the font faces that are contained in the page.");
            // doesn't change the font then
        }

        return result;
    }

    // print the error to the log file.
    private void printError(String p_message, Exception p_exception)
    {
        String type = "page";
        String name = null;
        long id = -1;

        if (m_page != null)
        {
            name = m_page.getExternalPageId();
            id = m_page.getId();
        }
        else if (m_secondaryTargetFile != null)
        {
            type = "secondary target file";
            name = m_secondaryTargetFile.getStoragePath();
            id = m_secondaryTargetFile.getId();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(p_message);
        sb.append(type);
        sb.append(": ");
        sb.append(name);
        sb.append(" with Id=");
        sb.append(id);

        s_logger.error(sb.toString(), p_exception);
    }

    /**
     * Prepares the replaced image export info and sends it to CXE export
     * servlet - from where it will get redirected to image replace servlet.
     */
    private void sendReplacedImagesToCXE(String p_gxmlFileName,
            ExportParameters p_exportParameters,
            GlobalSightLocale p_targetLocale) throws PageException
    {
        String imageName = "";

        try
        {
            StringBuffer docRoot = new StringBuffer(SystemConfiguration
                    .getInstance().getStringParameter(
                            SystemConfigParamNames.FILE_STORAGE_DIR));

            docRoot.append(File.separator)
                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                    .append(WebAppConstants.VIRTUALDIR_IMAGE_REPLACE);

            // Get collection of images that need to be replaced
            // and iterate through it while posting the replaced
            // images to CXE.
            //
            @SuppressWarnings("rawtypes")
            Collection replaceImages = ServerProxy
                    .getImageReplaceFileMapPersistenceManager()
                    .getImageReplaceFileMapsForTargetPage(m_page.getIdAsLong());

            Integer numImages = new Integer(replaceImages.size());
            int imageNum = 0;

            for (@SuppressWarnings("rawtypes")
            Iterator itr = replaceImages.iterator(); itr.hasNext();)
            {
                ExportBatchInfo ebi = new ExportBatchInfo("N/A", numImages,
                        new Integer(imageNum));

                imageNum++;

                String line = getExportRequestInfo(p_gxmlFileName,
                        p_exportParameters, p_targetLocale, ebi);

                ImageReplaceFileMap irmf = (ImageReplaceFileMap) itr.next();
                imageName = irmf.getRealSourceName();

                String imageFilePath = docRoot.toString() + File.separator
                        + irmf.getTempSourceName();

                s_logger.debug("imageFilePath=" + imageFilePath);

                File imageFile = new File(imageFilePath);
                int fileLength = (int) imageFile.length();
                FileInputStream fis = new FileInputStream(imageFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                byte[] imageBuffer = new byte[fileLength];

                bis.read(imageBuffer);
                bis.close();
                fis.close();

                // Now append the replaced image information
                //
                StringBuffer sb = new StringBuffer(line);
                sb.append(AND);
                sb.append(URLEncoder.encode(ExportConstants.IMAGE_FILENAME,
                        ExportConstants.UTF8));
                sb.append(EQUALS);
                sb.append(URLEncoder.encode(irmf.getRealSourceName(),
                        ExportConstants.UTF8));
                sb.append(AND);
                sb.append(URLEncoder.encode(ExportConstants.IMAGE_DATA,
                        ExportConstants.UTF8));
                sb.append(EQUALS);

                // Encode the binary file into an Base64 encoded string to
                // send it across the wire
                //
                sb.append(URLEncoder.encode(Base64.encodeToString(imageBuffer),
                        ExportConstants.UTF8));

                URL url = new URL(p_exportParameters.getTargetURL());
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(
                        conn.getOutputStream(), ExportConstants.UTF8);

                wr.write(sb.toString());
                wr.flush();
                wr.close();

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(url.toString()
                            + GlobalSightCategory.getLineContinuation()
                            + "Sent image " + irmf.getTempSourceName()
                            + " (temp) as " + irmf.getRealSourceName()
                            + " (real)");
                }

                // just read the output so the other side doesn't hang,
                // and so we can get an exception if it's a 500 error
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                StringBuffer sbexp = new StringBuffer();
                while ((line = rd.readLine()) != null && line.length() > 0)
                {
                    sbexp.append(line);
                }
                rd.close();
            }
        }
        catch (RemoteException re)
        {
            s_logger.error(
                    "sendReplacedImagesToCXE: RemoteException image:"
                            + imageName + " page:" + m_page.getExternalPageId()
                            + " " + p_exportParameters.toString() + " "
                            + p_targetLocale.toString(), re);
            throw new PageException(
                    PageException.MSG_FAILED_TO_LOCATE_PAGE_EVENT_OBSERVER,
                    null, re);
        }
        catch (PageException pe)
        {
            throw pe;
        }
        catch (Exception e)
        {
            s_logger.error(
                    "sendReplacedImagesToCXE: image:" + imageName + " page:"
                            + m_page.getExternalPageId() + " "
                            + p_exportParameters.toString() + " "
                            + p_targetLocale.toString(), e);
            throw new PageException(PageException.MSG_FAILED_TO_CONNECT_TO_CXE,
                    getExceptionArgument(), e);
        }
    }

    // prepare the export info and send it to CXE
    private void sendToCXE(MessageData p_messageData,
            ExportParameters p_exportParameters,
            GlobalSightLocale p_targetLocale, Integer p_pageCount,
            Integer p_pageNum, Integer p_docPageCount, Integer p_docPageNum,
            String p_exportBatchId, boolean p_isUnextracted,
            String p_exportingFileName, int p_sourcePageBomType)
            throws PageException
    {
        try
        {
            // an interim export can happen during dispatch without
            // updating any states.
            switch (m_genericPageType)
            {
                case PageManager.SECONDARY_TARGET_FILE:

                    if (!m_secondaryTargetFile.getWorkflow().getState()
                            .equals(Workflow.DISPATCHED))
                    {
                        ServerProxy
                                .getSecondaryTargetFileManager()
                                .updateState(
                                        m_secondaryTargetFile.getIdAsLong(),
                                        SecondaryTargetFileState.EXPORT_IN_PROGRESS);
                    }
                    break;

                case PageManager.TARGET_PAGE:
                    String workflowState = ((TargetPage) m_page)
                            .getWorkflowInstance().getState();

                    if (!inProgressWorkflow(workflowState))
                    {
                        getPageEventObserver().notifyExportInProgressEvent(
                                (TargetPage) m_page);
                    }

                    sendReplacedImagesToCXE(p_messageData.getName(),
                            p_exportParameters, p_targetLocale);
                    break;

                default:
                    break;
            }

            sendExportMessagetoCXE(p_exportBatchId, p_pageCount, p_pageNum,
                    p_docPageCount, p_docPageNum, p_messageData,
                    p_exportParameters, p_targetLocale, p_isUnextracted,
                    p_exportingFileName, p_sourcePageBomType);
        }
        catch (PageException pe)
        {
            throw pe;
        }
        catch (RemoteException re)
        {
            s_logger.error(
                    "sendToCXE: " + re.toString() + "\n"
                            + p_exportParameters.toString() + "\n"
                            + p_targetLocale.toString(), re);

            throw new PageException(
                    PageException.MSG_FAILED_TO_LOCATE_PAGE_EVENT_OBSERVER,
                    null, re);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "sendToCXE: " + e.toString() + "\n"
                            + p_exportParameters.toString() + "\n"
                            + p_targetLocale.toString(), e);

            throw new PageException(PageException.MSG_FAILED_TO_CONNECT_TO_CXE,
                    getExceptionArgument(), e);
        }
    }

    private void startExport(@SuppressWarnings("rawtypes") Hashtable p_map)
            throws Exception
    {
        // check for a pageId, and export parameter object
        ExportParameters exportParam = (ExportParameters) p_map
                .get(new Integer(PageManager.EXPORT_PARAMETERS));

        long pageId = ((Long) p_map.get(new Integer(PageManager.PAGE_ID)))
                .longValue();
        Integer genericPageType = (Integer) p_map.get(new Integer(
                PageManager.TARGET_PAGE));
        Integer pageCount = (Integer) p_map.get(new Integer(
                PageManager.PAGE_COUNT));
        Integer pageNum = (Integer) p_map
                .get(new Integer(PageManager.PAGE_NUM));
        Integer docPageCount = (Integer) p_map.get(new Integer(
                PageManager.DOC_PAGE_COUNT));
        Integer docPageNum = (Integer) p_map.get(new Integer(
                PageManager.DOC_PAGE_NUM));
        String exportBatchId = ((Long) p_map
                .get(ExportConstants.EXPORT_BATCH_ID)).toString();

        m_genericPageType = genericPageType.intValue();
        switch (m_genericPageType)
        {
            case PageManager.SECONDARY_TARGET_FILE:
                m_secondaryTargetFile = ServerProxy
                        .getSecondaryTargetFileManager()
                        .getSecondaryTargetFile(pageId);
                break;

            case PageManager.SOURCE_PAGE:
                m_page = ServerProxy.getPageManager().getSourcePage(pageId);
                m_sourcePage = (SourcePage) m_page;
                // Touch to load all TUs of this source page for performance.
                SegmentTuUtil.getTusBySourcePageId(m_sourcePage.getId());
                break;

            case PageManager.TARGET_PAGE:
                m_page = ServerProxy.getPageManager().getTargetPage(pageId);
                m_sourcePage = ((TargetPage) m_page).getSourcePage();
                // Touch to load all TUs of this source page for performance.
                SegmentTuUtil.getTusBySourcePageId(m_sourcePage.getId());
                break;

            default:
                break;
        }

        export(exportParam, pageCount, pageNum, docPageCount, docPageNum,
                exportBatchId);
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private void startExport(List<Hashtable> p_maps) throws Exception
    {
        AdapterResult adapter = new AdapterResult();

        for (Hashtable map : p_maps)
        {
            ExportParameters exportParam = (ExportParameters) map
                    .get(new Integer(PageManager.EXPORT_PARAMETERS));

            long pageId = ((Long) map.get(new Integer(PageManager.PAGE_ID)))
                    .longValue();
            Integer genericPageType = (Integer) map.get(new Integer(
                    PageManager.TARGET_PAGE));
            Integer pageCount = (Integer) map.get(new Integer(
                    PageManager.PAGE_COUNT));
            Integer pageNum = (Integer) map.get(new Integer(
                    PageManager.PAGE_NUM));
            Integer docPageCount = (Integer) map.get(new Integer(
                    PageManager.DOC_PAGE_COUNT));
            Integer docPageNum = (Integer) map.get(new Integer(
                    PageManager.DOC_PAGE_NUM));
            String exportBatchId = ((Long) map
                    .get(ExportConstants.EXPORT_BATCH_ID)).toString();

            int type = genericPageType.intValue();
            SourcePage sourcePage = null;
            TargetPage targetPage = null;
            Page page = null;

            switch (type)
            {
                case PageManager.SOURCE_PAGE:
                    sourcePage = ServerProxy.getPageManager().getSourcePage(
                            pageId);
                    // Touch to load all TUs of this source page for
                    // performance.
                    SegmentTuUtil.getTusBySourcePageId(pageId);
                    page = sourcePage;
                    m_page = page;
                    m_sourcePage = sourcePage;
                    break;

                case PageManager.TARGET_PAGE:
                    targetPage = ServerProxy.getPageManager().getTargetPage(
                            pageId);
                    sourcePage = targetPage.getSourcePage();
                    // Touch to load all TUs of this source page for
                    // performance.
                    SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                    page = targetPage;
                    m_page = page;
                    m_sourcePage = sourcePage;
                    break;

                default:
                    break;
            }

            try
            {
                GlobalSightLocale targetLocale = null;
                MessageData messageData = MessageDataFactory
                        .createFileMessageData();
                boolean isUnextracted = false;
                // name of the file being exported
                String exportingFileName = null;
                int sourcePageBomType = ExportConstants.NO_UTF_BOM;
                boolean isTabstrip = false;
                HashMap<String, String> mapOfSheetTabs = new HashMap<String, String>();

                targetLocale = page.getGlobalSightLocale();
                exportingFileName = page.getExternalPageId();
                isTabstrip = exportingFileName.startsWith("(tabstrip)");
                int index = exportingFileName.indexOf(File.separator);
                exportingFileName = exportingFileName.substring(index + 1);
                sourcePageBomType = sourcePage.getBOMType();
                PageTemplate pageTemplate = null;

                // verify it is an extracted file.
                if (page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
                {
                    s_logger.debug("Starting a transparent collection Export Template");
                    ExtractedFile ef = (ExtractedFile) page.getPrimaryFile();
                    pageTemplate = ef.getPageTemplate(PageTemplate.TYPE_EXPORT);
                }

                ArrayList segments = null;
                try
                {
                    String key = sourcePage.getId() + "-"
                            + page.getGlobalSightLocale().getId();
                    segments = cachedTuvs.get(key);
                    if (segments == null || segments.size() == 0)
                    {
                        SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                        segments = new ArrayList(ServerProxy.getTuvManager()
                                .getExportTuvs(sourcePage,
                                        page.getGlobalSightLocale()));
                        cachedTuvs.put(key, segments);
                    }
                }
                catch (Exception ex)// GeneralException, TuvException,
                                    // RemoteException
                {
                    s_logger.error("cannot load target page segments", ex);

                    throw new PageException(
                            PageException.MSG_FAILED_TO_GET_TUVS,
                            getExceptionArgument(), ex);
                }

                segments = segments == null ? new ArrayList() : segments;

                pageTemplate.setTabsTrip(isTabstrip);
                pageTemplate.setXlfSrcAsTrg(exportParam.getXlfSrcAsTrg());
                String populatePage = populatePage(pageTemplate, segments,
                        targetLocale, false, false, null);

                if (isTabstrip)
                {
                    String mainFileName = buildMainFileName(
                            exportingFileName,
                            targetLocale.getLanguage() + "_"
                                    + targetLocale.getCountry());
                    mapOfSheetTabs = pageTemplate.getMapOfSheetTabs();
                    modifyMainFile(mainFileName, mapOfSheetTabs);
                }
                BufferedOutputStream bos = new BufferedOutputStream(
                        messageData.getOutputStream());

                OutputStreamWriter osw = new OutputStreamWriter(bos,
                        ExportConstants.UTF8);

                osw.write(populatePage, 0, populatePage.length());
                osw.close();

                exportingFileName = ExportHelper.transformExportedFilename(
                        exportingFileName, targetLocale.toString());

                try
                {
                    // an interim export can happen during dispatch without
                    // updating any states.
                    switch (type)
                    {
                        case PageManager.TARGET_PAGE:
                            String workflowState = ((TargetPage) m_page)
                                    .getWorkflowInstance().getState();

                            if (!inProgressWorkflow(workflowState))
                            {
                                getPageEventObserver()
                                        .notifyExportInProgressEvent(
                                                (TargetPage) m_page);
                            }

                            sendReplacedImagesToCXE(messageData.getName(),
                                    exportParam, targetLocale);
                            break;

                        default:
                            break;
                    }

                    String codeSet = exportParam.getExportCodeset();
                    String cxeRequestType = exportParam.getExportType();
                    int bomType = exportParam.getBOMType();

                    String messageId = String
                            .valueOf(m_secondaryTargetFile == null ? m_page
                                    .getId() : m_secondaryTargetFile.getId());

                    if (page != null && (page instanceof TargetPage))
                    {
                        // If the page is of Microsoft Office Type enforce the
                        // codeset to be UTF-8
                        if (SourcePage.isMicrosoftOffice(targetPage
                                .getSourcePage()))
                        {
                            codeSet = "UTF-8";
                        }
                        // If the page is a FrameMaker file, enforce MacRoman
                        // as the only codeset that is understood by the
                        // Noonetime converter (and FrameMaker).
                        else if (SourcePage.isFrameMaker(targetPage
                                .getSourcePage()))
                        {
                            codeSet = "MacRoman";
                        }
                    }

                    CxeMessage msg = CxeProxy.getExportCxeMessage(sourcePage
                            .getRequest().getEventFlowXml(), messageData,
                            cxeRequestType, targetLocale.toString(), codeSet,
                            bomType, messageId,
                            exportParam.getExportLocation(), exportParam
                                    .getLocaleSubDir(), exportBatchId,
                            pageCount, pageNum, docPageCount, docPageNum,
                            exportParam.getNewObjectId(), exportParam
                                    .getWorkflowId(), exportParam.isJobDone(),
                            isUnextracted, exportingFileName,
                            sourcePageBomType, exportParam.getIsFinalExport(),
                            CompanyThreadLocal.getInstance().getValue());

                    adapter.addMsg(msg);
                }
                catch (PageException pe)
                {
                    throw pe;
                }
                catch (RemoteException re)
                {
                    s_logger.error(
                            "sendToCXE: " + re.toString() + "\n"
                                    + exportParam.toString() + "\n"
                                    + targetLocale.toString(), re);

                    throw new PageException(
                            PageException.MSG_FAILED_TO_LOCATE_PAGE_EVENT_OBSERVER,
                            null, re);
                }
                catch (Exception e)
                {
                    s_logger.error(
                            "sendToCXE: " + e.toString() + "\n"
                                    + exportParam.toString() + "\n"
                                    + targetLocale.toString(), e);

                    throw new PageException(
                            PageException.MSG_FAILED_TO_CONNECT_TO_CXE,
                            getExceptionArgument(), e);
                }
            }
            catch (PageException pe)
            {
                throw pe;
            }
            catch (Exception e)
            {
                throw new PageException(
                        PageException.MSG_FAILED_TO_CONNECT_TO_CXE,
                        getExceptionArgument(), e);
            }
            finally
            {
                switch (type)
                {
                    case PageManager.SECONDARY_TARGET_FILE:
                        deleteLevMatches(m_secondaryTargetFile.getWorkflow());
                        break;

                    case PageManager.TARGET_PAGE:
                        deleteLevMatches();
                        break;

                    default:
                        break;
                }
            }
        }

        String jmsTopic = EventTopicMap.JMS_PREFIX
                + EventTopicMap.FOR_CAP_SOURCE_ADAPTER;
        JmsHelper.sendMessageToQueue(adapter, jmsTopic);
    }

    private boolean inProgressWorkflow(String workflowState)
    {
        return Workflow.READY_TO_BE_DISPATCHED.equals(workflowState)
                || Workflow.PENDING.equals(workflowState)
                || Workflow.DISPATCHED.equals(workflowState)
                || Workflow.SKIPPING.equals(workflowState);
    }

    // starts off the request info
    private void startExportInfo(StringBuffer p_sb, String p_parameterName,
            String p_parameterValue)
    {
        writeParameterValuePair(p_sb, p_parameterName, p_parameterValue);
    }

    /**
     * Update the segment (translatable/localizable item). Note that for a
     * dynamic preview, we need to replace the place holders in the template
     * with a default GS segment. This also modifies the GXML segment tag to
     * include the project_tm_tu id that corresponds to this segment if
     * corpusMappings is not null.
     * 
     * @param p_template
     *            The page template to insert the content into.
     * @param segment
     *            The segment to update.
     * @param p_isPreview
     *            Specifies if this for preview or not.
     * @param p_tuvIds
     *            This only needs to be set if this is for preview otherwise it
     *            can be null or an empty ArrayList.
     * @param p_targetCorpusMappings
     *            The mappings showing which tuv maps to which project_tm_tuv
     * @param p_isLocalizable
     *            -- true if a localizable, false if segment
     * @param p_restoreSpacesForJavaProerties
     *            Flag for preserve trailing spaces, default false
     */
    @SuppressWarnings("rawtypes")
    private void updateSegValue(PageTemplate p_template, Tuv p_segment,
            boolean p_isPreview, boolean p_needToAddGsColor,
            boolean p_isIncontextReview, List p_tuvIds,
            Map p_targetCorpusMappings, boolean p_isLocalizable,
            boolean isInddOrIdml)
    {
        long jobId = p_template.getSourcePage().getJobId();
        String tuvContent = null;
        Long tuId = p_segment.getTu(jobId).getIdAsLong();

        if (p_isPreview && p_tuvIds != null)
        {
            tuvContent = p_tuvIds.contains(p_segment.getIdAsLong()) ? p_segment
                    .getGxml() : ExportConstants.DEFAULT_SEGMENT;
        }
        else if (p_isLocalizable)
        {
            tuvContent = p_segment.getGxmlExcludeTopTags();
        }
        else
        {
            tuvContent = p_segment.getGxml();

            if (p_segment instanceof TuvImpl)
            {
                TuvImpl tuv = (TuvImpl) p_segment;
                TuImpl tu = (TuImpl) tuv.getTu(jobId);

                if (hasRemovedTags(tu))
                {
                    if (tuvContent.endsWith("/>"))
                    {
                        // this is an empty segment - should be from an empty
                        // translation
                        tuvContent = tuvContent.replace("/>", "></segment>");
                    }

                    String regex = "(<segment[^>]*>)([\\d\\D]*)(</segment>)";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(tuvContent);

                    if (m.find())
                    {
                        String content = m.group(2);

                        // GBS-3722, clean the MT tags first
                        content = MTHelper.cleanMTTagsForExport(content);

                        content = addRemovedPrefixTag(content,
                                tu.getPrefixTag());
                        content = addRemovedSuffixTag(content,
                                tu.getSuffixTag());

                        if (tu.hasRemovedTags())
                        {
                            StyleUtil util = getStyleUtil();

                            if (util != null)
                            {
                                content = util.preHandle(content);
                            }

                            String oriContent = content;
                            content = addRemovedTags(content,
                                    tu.getRemovedTag());

                            if (oriContent.equals(content))
                            {
                                content = addRemovedTags2(content,
                                        tu.getRemovedTag());
                            }

                            if (util != null)
                            {
                                content = util.sufHandle(content);
                            }
                        }

                        tuvContent = m.group(1) + content + m.group(3);
                    }
                }

                if (IFormatNames.FORMAT_OFFICE_XML.equals(tu.getDataType()))
                {
                    tuvContent = addXmlSpacePreserve(tuvContent);
                }
            }
        }

        if (p_targetCorpusMappings != null && !p_isLocalizable)
        {
            tuvContent = addProjectTmTuIdToSegment(p_targetCorpusMappings,
                    p_segment, tuvContent);
        }

        // do not add this id for style issue. 
        if (p_isIncontextReview)
        {
            ///TODO: add this back later
            /*
            String gsidMark = "_gsid_" + tuId + "_";
            if (tuvContent != null && !"".equals(tuvContent.trim()))
            {
                String segStart = "";
                String segEnd = "";
                String innerContent = tuvContent;
                if (tuvContent.startsWith("<segment")
                        && tuvContent.endsWith("</segment>"))
                {
                    int index = tuvContent.indexOf(">");
                    int index_2 = tuvContent.lastIndexOf("<");
                    segStart = tuvContent.substring(0, index + 1);
                    innerContent = tuvContent.substring(index + 1, index_2);
                    segEnd = tuvContent.substring(index_2);
                }

                boolean added = false;
                if (isIdml())
                {
                    if (innerContent.startsWith("<"))
                    {
                        if (innerContent.endsWith(">"))
                        {
                            String keyWords = "<ept[^>]*?>&lt;/Content&gt;";
                            Pattern p = Pattern.compile(keyWords);
                            Matcher m = p.matcher(innerContent);

                            if (m.find())
                            {
                                int index = m.start();

                                while (m.find())
                                {
                                    index = m.start();
                                }

                                innerContent = innerContent.substring(0, index)
                                        + gsidMark
                                        + innerContent.substring(index);
                                added = true;
                            }
                        }
                        else
                        {
                            int index = innerContent.lastIndexOf(">");

                            if (index != -1)
                            {
                                innerContent = innerContent.substring(0,
                                        index + 1)
                                        + gsidMark
                                        + innerContent.substring(index + 1);
                                added = true;
                            }
                        }
                    }
                    else if (innerContent.endsWith(">"))
                    {
                        int index = innerContent.indexOf("<");
                        innerContent = innerContent.substring(0, index)
                                + gsidMark + innerContent.substring(index);
                        added = true;
                    }
                }

                if (!added)
                {
                    innerContent = innerContent + gsidMark;
                }

                tuvContent = segStart + innerContent + segEnd;
            }
            */
        }
        else if (p_needToAddGsColor)
        {
            tuvContent = addGlobalSightColorTag(tuvContent, p_segment,
                    isInddOrIdml, jobId);
        }

        p_template.insertTuvContent(tuId, tuvContent);
    }

    private StyleUtil getStyleUtil()
    {
        StyleUtil util = null;
        if (isDocx())
        {
            util = StyleFactory.getStyleUtil(StyleFactory.DOCX);
        }
        else if (isPptx())
        {
            util = StyleFactory.getStyleUtil(StyleFactory.PPTX);
        }
        else if (isXlsx())
        {
            util = StyleFactory.getStyleUtil(StyleFactory.XLSX);
        }

        return util;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private boolean initGsColor()
    {
        m_gsColorMap = new HashMap<String, String>();
        m_color100 = UserParamNames.PREVIEW_100MATCH_COLOR_DEFAULT;
        m_colorIce = UserParamNames.PREVIEW_ICEMATCH_COLOR_DEFAULT;
        m_colorNon = UserParamNames.PREVIEW_NONMATCH_COLOR_DEFAULT;

        if (m_userid != null)
        {
            try
            {
                UserParameterPersistenceManager upm = ServerProxy
                        .getUserParameterManager();
                m_color100 = upm.getUserParameter(m_userid,
                        UserParamNames.PREVIEW_100MATCH_COLOR).getValue();
                m_colorIce = upm.getUserParameter(m_userid,
                        UserParamNames.PREVIEW_ICEMATCH_COLOR).getValue();
                m_colorNon = upm.getUserParameter(m_userid,
                        UserParamNames.PREVIEW_NONMATCH_COLOR).getValue();
            }
            catch (Exception e)
            {
                s_logger.warn("Cannot get user parameter for previewing color, use default colors.");
            }
        }

        // if 3 colors are black, so just keep it as before
        if ("Black".equalsIgnoreCase(m_color100)
                && "Black".equalsIgnoreCase(m_colorIce)
                && "Black".equalsIgnoreCase(m_colorNon))
        {
            return false;
        }

        String color0 = GS_COLOR_S_100MATCH.replace("100", m_color100);
        String color1 = GS_COLOR_E_100MATCH.replace("100", m_color100);
        m_gsColorMap.put(GS_COLOR_S_100MATCH, color0);
        m_gsColorMap.put(GS_COLOR_E_100MATCH, color1);

        String color2 = GS_COLOR_S_ICEMATCH.replace("ICE", m_colorIce);
        String color3 = GS_COLOR_E_ICEMATCH.replace("ICE", m_colorIce);
        m_gsColorMap.put(GS_COLOR_S_ICEMATCH, color2);
        m_gsColorMap.put(GS_COLOR_E_ICEMATCH, color3);

        String color4 = GS_COLOR_S_NONMATCH.replace("NON", m_colorNon);
        String color5 = GS_COLOR_E_NONMATCH.replace("NON", m_colorNon);
        m_gsColorMap.put(GS_COLOR_S_NONMATCH, color4);
        m_gsColorMap.put(GS_COLOR_E_NONMATCH, color5);

        try
        {
            m_sourceTuvs = new ArrayList<Tuv>(ServerProxy.getTuvManager()
                    .getSourceTuvsForStatistics(m_sourcePage));
            m_targetTuvs = new ArrayList<Tuv>(ServerProxy.getTuvManager()
                    .getTargetTuvsForStatistics((TargetPage) m_page));
        }
        catch (Exception e)
        {
            s_logger.error("Cannot get tuvs for previewing color.", e);
            m_sourceTuvs = new ArrayList<Tuv>();
            m_targetTuvs = new ArrayList<Tuv>();
        }

        try
        {
            m_matchTypes = LingServerProxy.getLeverageMatchLingManager()
                    .getMatchTypesForStatistics(m_sourcePage.getId(),
                            m_page.getLocaleId(), 0);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Cannot MatchTypesForStatistics for previewing color.", e);
        }

        m_excludedItemTypes = m_editorState == null ? new Vector()
                : m_editorState.getExcludedItems();

        return true;
    }

    private int getTuvIndex(Tuv p_targetTuv)
    {
        for (int i = 0, max = m_targetTuvs.size(); i < max; i++)
        {
            Tuv c = m_targetTuvs.get(i);

            if (c.getId() == p_targetTuv.getId())
            {
                return i;
            }
        }

        return -1;
    }

    private String addGSColorDefine(String tuvContent)
    {
        if (tuvContent == null || "".equals(tuvContent.trim()))
        {
            return tuvContent;
        }
        else
        {
            String define = "((GS_COLOR_DEFINE)(" + m_colorNon + ","
                    + m_color100 + "," + m_colorIce + "))";

            if (tuvContent.startsWith("<segment")
                    && tuvContent.endsWith("</segment>"))
            {
                int index_e = tuvContent.length() - 10;
                StringBuffer sb = new StringBuffer(tuvContent);
                sb.insert(index_e, define);

                return sb.toString();
            }
            else
            {
                return tuvContent + define;
            }
        }
    }

    private String addGlobalSightColorTag(String tuvContent, Tuv p_segment,
            boolean isInddOrIdml, long p_jobId)
    {
        if (tuvContent == null || "".equals(tuvContent.trim()))
        {
            return tuvContent;
        }
        else
        {
            TuvState state = p_segment.getState();

            String start = null;
            String end = null;
            // black for 100%/ICE which would be ignored and red for all others.
            if (TuvState.EXACT_MATCH_LOCALIZED.equals(state))
            {
                int index = getTuvIndex(p_segment);
                boolean isICE = isICEMatch(index, p_jobId);

                if (isICE)
                {
                    start = GS_COLOR_S_ICEMATCH;
                    end = GS_COLOR_E_ICEMATCH;
                }
                else
                {
                    start = GS_COLOR_S_100MATCH;
                    end = GS_COLOR_E_100MATCH;
                }
            }
            else if (TuvState.NOT_LOCALIZED.equals(state))
            {
                start = GS_COLOR_S_NONMATCH;
                end = GS_COLOR_E_NONMATCH;

                int index = getTuvIndex(p_segment);
                boolean is100Match = LeverageUtil.isExactMatch(index,
                        m_sourceTuvs, m_matchTypes);
                boolean isICE = isICEMatch(index, p_jobId);
                if (isICE)
                {
                    start = GS_COLOR_S_ICEMATCH;
                    end = GS_COLOR_E_ICEMATCH;
                }
                else if (is100Match)
                {
                    start = GS_COLOR_S_100MATCH;
                    end = GS_COLOR_E_100MATCH;
                }
            }
            else if (TuvState.LOCALIZED.equals(state))
            {
                String lastuser = p_segment.getLastModifiedUser();
                if (isMTUser(lastuser))
                {
                    start = GS_COLOR_S_100MATCH;
                    end = GS_COLOR_E_100MATCH;
                }
                else
                {
                    start = GS_COLOR_S_NONMATCH;
                    end = GS_COLOR_E_NONMATCH;
                }
            }
            else
            {
                start = GS_COLOR_S_NONMATCH;
                end = GS_COLOR_E_NONMATCH;
            }

            start = m_gsColorMap.get(start);
            end = m_gsColorMap.get(end);

            if (tuvContent.startsWith("<segment")
                    && tuvContent.endsWith("</segment>"))
            {
                return HtmlPreviewerHelper.addGSColorForSegment(m_format,
                        tuvContent, start, end, isInddOrIdml);
            }
            else
            {
                return start + tuvContent + end;
            }
        }
    }

    private boolean isMTUser(String username)
    {
        if (username != null)
        {
            boolean ismt = username.endsWith("_MT");

            return ismt;
        }

        return false;
    }

    private boolean isICEMatch(int index, long p_jobId)
    {
        @SuppressWarnings("unchecked")
        boolean isICE = LeverageUtil.isIncontextMatch(index, m_sourceTuvs,
                m_targetTuvs, m_matchTypes, m_excludedItemTypes, p_jobId);

        if (isICE && m_tmp != null && !m_tmp.getIsContextMatchLeveraging())
        {
            isICE = false;
        }

        return isICE;
    }

    private boolean needToAddColorTag(boolean p_isPreview,
            boolean p_isIncontextReview)
    {
        if (!p_isPreview || p_isIncontextReview)
        {
            return false;
        }

        if (IFormatNames.FORMAT_MIF.equals(m_format))
        {
            return true;
        }

        if (IFormatNames.FORMAT_OFFICE_XML.equals(m_format))
        {
            return true;
        }

        if (isInddOrIdml())
        {
            return true;
        }

        return false;
    }

    private boolean isInddOrIdml()
    {
        if (IFormatNames.FORMAT_XML.equals(m_format))
        {
            String srcPageId = m_sourcePage.getExternalPageId().toLowerCase();
            if (srcPageId.endsWith(".idml") || srcPageId.endsWith(".indd"))
            {
                return true;
            }
        }

        return false;
    }

    private void revertWhiteSpacesForJavaProperty(Tuv p_segment, long fpId,
            String format, long p_jobId)
    {
        try
        {
            // Preserve trailing spaces?
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(fpId, false);
            boolean isPreserveTrailingSpace = fp.getPreserveSpaces();
            // Is properties file?
            boolean isJavaProperties = format.toLowerCase()
                    .endsWith("javaprop");
            // Source segment has trailing spaces?
            long sourcePageLocaleId = m_sourcePage.getGlobalSightLocale()
                    .getIdAsLong();
            Tuv sourceTuv = p_segment.getTu(p_jobId).getTuv(sourcePageLocaleId,
                    p_jobId);
            String sourceTuvContentNoTags = sourceTuv.getGxmlExcludeTopTags();
            int sourceTrailingSpaceNum = countTrailingSpaceNum(sourceTuvContentNoTags);
            // Target segment has trailing spaces?
            String targetGxml = p_segment.getGxmlExcludeTopTags();
            int targetTrailingSpaceNum = countTrailingSpaceNum(targetGxml);

            if (isPreserveTrailingSpace && isJavaProperties
                    && sourceTrailingSpaceNum > 0
                    && targetTrailingSpaceNum == 0)
            {
                long currentTuvPageLocaleId = p_segment.getGlobalSightLocale()
                        .getIdAsLong();

                String spaces = "";
                if (currentTuvPageLocaleId != sourcePageLocaleId)
                {
                    while (Character.isWhitespace(sourceTuvContentNoTags
                            .charAt(sourceTuvContentNoTags.length() - 1)))
                    {
                        sourceTuvContentNoTags = sourceTuvContentNoTags
                                .substring(0,
                                        sourceTuvContentNoTags.length() - 1);
                        spaces += " ";
                    }
                }
                targetGxml = targetGxml + spaces;
                p_segment.setGxmlExcludeTopTags(targetGxml, p_jobId);
            }
        }
        catch (Exception e)
        {
        }
    }

    private boolean hasRemovedTags(TuImpl tu)
    {
        if (tu.hasRemovedTags())
            return true;

        if (tu.getPrefixTag() != null)
            return true;

        if (tu.getSuffixTag() != null)
            return true;

        return false;
    }

    private String addRemovedPrefixTag(String s, RemovedPrefixTag tag)
    {
        if (tag == null || s == null)
        {
            return s;
        }

        return tag.getString() + s;
    }

    private String addRemovedSuffixTag(String s, RemovedSuffixTag tag)
    {
        if (tag == null || s == null)
        {
            return s;
        }

        return s + tag.getString();
    }

    /**
     * Checks the page type is idml or not.
     * 
     * @return
     */
    private boolean isIdml()
    {
        boolean isIdml = false;
        if (m_page != null)
        {
            String page = m_page.getExternalPageId();
            if (page != null && page.toLowerCase().endsWith(".idml"))
            {
                isIdml = true;
            }
        }

        return isIdml;
    }

    /**
     * Checks the page type is docx or not.
     * 
     * @return
     */
    private boolean isDocx()
    {
        if (m_page != null)
        {
            String page = m_page.getExternalPageId();
            if (page != null)
            {
                String path = page.toLowerCase();
                if (path.endsWith(".docx"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks the page type is docx or not.
     * 
     * @return
     */
    private boolean isXlsx()
    {
        if (m_page != null)
        {
            String page = m_page.getExternalPageId();
            if (page != null)
            {
                String path = page.toLowerCase();
                if (path.endsWith(".xlsx"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks the page type is docx or not.
     * 
     * @return
     */
    private boolean isPptx()
    {
        if (m_page != null)
        {
            String page = m_page.getExternalPageId();
            if (page != null)
            {
                String path = page.toLowerCase();
                if (path.endsWith(".pptx"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds tags which removed during import.
     * 
     * @param s
     *            the original gxml
     * @param removedTag
     *            the removed tag
     * @return the new gxml
     */
    private String addRemovedTags(String s, RemovedTag removedTag)
    {
        if (isIdml())
        {
            if (s.length() == 0)
            {
                return s;
            }
        }
        else
        {
            if (s.trim().length() == 0)
            {
                return s;
            }
        }

        if (isIdml())
        {
            // for [it] start
            Pattern pItStart = Pattern.compile(REGEX_IT_START);
            Matcher mItStart = pItStart.matcher(s);

            if (mItStart.find())
            {
                String all = mItStart.group();
                int index = s.indexOf(all);
                String s1 = s.substring(0, index);
                String s2 = s.substring(index);
                return addRemovedTags(s1, removedTag) + s2;
            }

            // for other [it]
            Pattern pIt = Pattern.compile(REGEX_IT);
            Matcher mIt = pIt.matcher(s);

            if (mIt.find())
            {
                String all = mIt.group();
                String content = mIt.group(1);

                int sPre = content.indexOf("&lt;Content&gt;");
                int ePre = content.lastIndexOf("&lt;Content&gt;");
                int sSuf = content.indexOf("&lt;/Content&gt;");
                int eSuf = content.lastIndexOf("&lt;/Content&gt;");

                boolean ignoreBefore = sSuf > -1 && (sSuf < sPre || sPre == -1);
                boolean ignoreAfter = ePre > eSuf;

                int index = s.indexOf(all);
                String s1 = s.substring(0, index);
                String s2 = s.substring(index + all.length());

                if (!ignoreBefore)
                    s1 = addRemovedTags(s1, removedTag);

                if (!ignoreAfter)
                    s2 = addRemovedTags(s2, removedTag);

                return s1 + all + s2;
            }
        }
        else
        {
            // for [it] start
            Pattern pItStart = Pattern.compile(REGEX_IT_START);
            Matcher mItStart = pItStart.matcher(s);

            if (mItStart.find())
            {
                String all = mItStart.group();
                int index = s.indexOf(all);
                String s1 = s.substring(0, index);
                String s2 = s.substring(index);
                return addRemovedTags(s1, removedTag) + s2;
            }

            // for [it] end
            Pattern pIt = Pattern.compile(REGEX_IT_END);
            Matcher mIt = pIt.matcher(s);

            if (mIt.find())
            {
                String all = mIt.group();
                int index = s.indexOf(all);

                String s1 = s.substring(0, index + all.length());
                String s2 = s.substring(index + all.length());
                return s1 + addRemovedTags(s2, removedTag);
            }
        }

        // for bpt
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        if (m.find())
        {
            String idd = m.group(1);
            String re = s.contains("</sub>") ? REGEX_BPT_ALL_SUB
                    : REGEX_BPT_ALL;
            Pattern p2 = Pattern.compile(MessageFormat.format(re, idd));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                int index = s.indexOf(all);
                String s1 = s.substring(0, index);
                String s2 = s.substring(index + all.length());
                return addRemovedTags(s1, removedTag) + all
                        + addRemovedTags(s2, removedTag);
            }
        }

        Pattern p2 = Pattern.compile("<[^>]*>[^<]*</[^>]*>");
        Matcher m2 = p2.matcher(s);
        if (m2.find())
        {
            String all = m2.group();
            int index = s.indexOf(all);
            String s1 = s.substring(0, index);
            String s2 = s.substring(index + all.length());
            return addRemovedTags(s1, removedTag) + all
                    + addRemovedTags(s2, removedTag);
        }

        return "<bpt>" + removedTag.getPrefixString() + "</bpt>" + s + "<ept>"
                + removedTag.getSuffixString() + "</ept>";
    }

    /**
     * Adds tags which removed during import.
     * 
     * @param s
     *            the original gxml
     * @param removedTag
     *            the removed tag
     * @return the new gxml
     */
    private String addRemovedTags2(String s, RemovedTag removedTag)
    {
        if (s.trim().length() == 0)
        {
            return s;
        }

        if (isDocx())
        {
            String ree = "(.+?)(</bpt>.+?)(<bpt .+)";
            String tagPre = removedTag.getPrefixString();
            String tagSuf = removedTag.getSuffixString();

            // add missed w:hyperlink tag which save in bpt ept
            if (tagPre.startsWith("&lt;w:hyperlink ")
                    && tagSuf
                            .endsWith("&lt;/w:t&gt;&lt;/w:r&gt;&lt;/w:hyperlink&gt;"))
            {
                return s + "<bpt>" + tagPre + "</bpt><ept>" + tagSuf + "</ept>";
            }
            else if ((tagPre.contains("&lt;w:t&gt;") || tagPre
                    .contains("&lt;w:t ")) && tagSuf.contains("&lt;/w:t&gt;"))
            {
                // for bpt
                Pattern p = Pattern.compile(REGEX_BPT);
                Matcher m = p.matcher(s);
                if (m.find())
                {
                    String idd = m.group(1);
                    String re = s.contains("</sub>") ? REGEX_BPT_ALL_SUB
                            : REGEX_BPT_ALL;
                    Pattern p2 = Pattern.compile(MessageFormat.format(re, idd));
                    Matcher m2 = p2.matcher(s);

                    if (m2.find())
                    {
                        String all = m2.group();
                        int index = s.indexOf(all);
                        String s1 = s.substring(0, index);
                        String s2 = s.substring(index + all.length());
                        if (all.contains("&lt;/w:r&gt;")
                                && !all.contains("&lt;/w:t&gt;")
                                && !all.contains("&lt;w:t "))
                        {
                            int i_ept = all.indexOf("<ept ");
                            int i_pp = all.lastIndexOf("</", i_ept);
                            int i_ee = all.indexOf(">", i_ept + 2) + 1;
                            String result = all.substring(0, i_pp) + tagPre
                                    + all.substring(i_pp, i_ee) + tagSuf
                                    + all.substring(i_ee);
                            result = s1 + result + s2;
                            return result;
                        }
                        else if (all.contains("&lt;w:hyperlink ")
                                && all.contains(" w:tooltip=&quot;<sub ")
                                && all.matches(ree))
                        {
                            Pattern pat = Pattern.compile(ree);
                            Matcher mer = pat.matcher(all);
                            if (mer.matches())
                            {
                                String g1 = mer.group(1);
                                String g2 = mer.group(2);
                                String g3 = mer.group(3);

                                int i_ee = g3.indexOf(">") + 1;
                                String result = g1 + tagPre + g2
                                        + g3.substring(0, i_ee) + tagSuf
                                        + g3.substring(i_ee);
                                result = s1 + result + s2;
                                return result;
                            }
                        }
                        else
                        {
                            return addRemovedTags2(s1, removedTag) + all
                                    + addRemovedTags2(s2, removedTag);
                        }
                    }
                }
            }
        }

        return s;
    }

    /**
     * Adds the project_tm_tu_id to the segment gxml
     * 
     * @param p_corpusMappings
     *            the HashMap containing TuvMapping objects
     * @param p_segment
     *            the Tuv segment
     * @param p_tuvContent
     *            the segment text as gxml
     * @return new segment gxml
     */
    @SuppressWarnings("rawtypes")
    private String addProjectTmTuIdToSegment(Map p_corpusMappings,
            Tuv p_segment, String p_tuvContent)
    {
        TuvMapping mapping = (TuvMapping) p_corpusMappings.get(p_segment
                .getIdAsLong());

        if (mapping == null)
        {
            return p_tuvContent;
        }

        long project_tm_tu_id = mapping.getProjectTmTuId();
        String s = " tuid=\"" + project_tm_tu_id + "\" ";
        StringBuffer sb = new StringBuffer(p_tuvContent);
        sb.insert("<segment".length(), s);
        return sb.toString();
    }

    // writes a parameter/value pair to the string buffer
    private void writeParameterValuePair(StringBuffer p_sb,
            String p_parameterName, String p_parameterValue)
    {
        try
        {
            p_sb.append(URLEncoder
                    .encode(p_parameterName, ExportConstants.UTF8));
            p_sb.append(EQUALS);
            p_sb.append(URLEncoder.encode(p_parameterValue,
                    ExportConstants.UTF8));
        }
        catch (Exception ex)
        {
            printError("WriteParameterValuePair for ", ex);
        }
    }

    /**
     * Uses the CxeProxy class to initiate an export within CXE.
     * 
     * @param p_exportBatchId
     *            export batch ID
     * @param p_pageCount
     *            total number of pages in the batch
     * @param p_pageNum
     *            the page number of this page (>=1)
     * @param p_messageData
     *            a message data object containing the content (GXML/binary)
     * @param p_exportParameters
     *            export parameters
     * @param p_targetLocale
     *            target locale
     * @param mapOfSheetTabs
     * @param isTabstrip
     * @exception PageException
     */
    private void sendExportMessagetoCXE(String p_exportBatchId,
            Integer p_pageCount, Integer p_pageNum, Integer p_docPageCount,
            Integer p_docPageNum, MessageData p_messageData,
            ExportParameters p_exportParameters,
            GlobalSightLocale p_targetLocale, boolean p_isUnextracted,
            String p_exportingFileName, int p_sourcePageBomType)
            throws PageException
    {
        try
        {
            String codeSet = p_exportParameters.getExportCodeset();
            String cxeRequestType = p_exportParameters.getExportType();
            String targetLocale = p_targetLocale.toString();
            int bomType = p_exportParameters.getBOMType();

            String messageId = String
                    .valueOf(m_secondaryTargetFile == null ? m_page.getId()
                            : m_secondaryTargetFile.getId());

            if (m_page != null && (m_page instanceof TargetPage))
            {
                TargetPage targetPage = (TargetPage) m_page;

                // If the page is of Microsoft Office Type enforce the
                // codeset to be UTF-8
                if (SourcePage.isMicrosoftOffice(targetPage.getSourcePage()))
                {
                    codeSet = "UTF-8";
                }
                // If the page is a FrameMaker file, enforce MacRoman
                // as the only codeset that is understood by the
                // Noonetime converter (and FrameMaker).
                else if (SourcePage.isFrameMaker(targetPage.getSourcePage()))
                {
                    codeSet = "MacRoman";
                }
            }

            CxeProxy.exportFile(getEventFlowXML(), p_messageData,
                    cxeRequestType, targetLocale, codeSet, bomType, messageId,
                    p_exportParameters.getExportLocation(), p_exportParameters
                            .getLocaleSubDir(), p_exportBatchId, p_pageCount,
                    p_pageNum, p_docPageCount, p_docPageNum, p_exportParameters
                            .getNewObjectId(), p_exportParameters
                            .getWorkflowId(), p_exportParameters.isJobDone(),
                    p_isUnextracted, p_exportingFileName, p_sourcePageBomType,
                    p_exportParameters.getIsFinalExport(), CompanyThreadLocal
                            .getInstance().getValue());
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    private TuvMappingHolder populateTm(GlobalSightLocale p_targetLocale)
            throws PageException
    {
        s_logger.info("Populating Tm for the page "
                + m_sourcePage.getExternalPageId());

        try
        {
            L10nProfile l10nProfile = m_sourcePage.getRequest()
                    .getL10nProfile();
            LeveragingLocales leveragingLocales = l10nProfile
                    .getLeveragingLocales();

            TranslationMemoryProfile tmProfile = l10nProfile
                    .getTranslationMemoryProfile();

            LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                    leveragingLocales);

            TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();

            TuvMappingHolder mappingHolder;
            long jobId = m_sourcePage.getJobId();
            if (m_genericPageType == PageManager.SOURCE_PAGE)
            {
                mappingHolder = tmCoreManager.populatePageForAllLocales(
                        m_sourcePage, leverageOptions, jobId);
            }
            else
            {
                mappingHolder = tmCoreManager.populatePageByLocale(
                        m_sourcePage, leverageOptions, p_targetLocale, jobId);
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("CorpusMappings:\n"
                        + mappingHolder.toDebugString());
            }

            s_logger.info("Populating Tm finished.");

            return mappingHolder;
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void stripExtraSpaces(List p_targetSegments, long p_jobId)
            throws PageException
    {
        GlobalSightLocale sourceLocale = m_sourcePage.getGlobalSightLocale();
        GlobalSightLocale targetLocale = null;
        if (p_targetSegments.size() > 0)
        {
            targetLocale = ((Tuv) p_targetSegments.get(0))
                    .getGlobalSightLocale();
        }

        // Don't change source segments
        if (sourceLocale.equals(targetLocale))
        {
            return;
        }

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            // get source segments
            HashMap sourceSegmentMap = getSourceSegments();
            int mergeStart = 0;

            for (int i = 0; i < p_targetSegments.size(); i++)
            {
                Tuv targetSegment = (Tuv) p_targetSegments.get(i);
                String mergeState = targetSegment.getMergeState();

                if (mergeState.equals(Tuv.NOT_MERGED))
                {
                    stripExtraSpaces(
                            conn,
                            targetSegment,
                            (Tuv) sourceSegmentMap.get(targetSegment.getTuId()),
                            p_jobId);
                }
                else if (mergeState.equals(Tuv.MERGE_START))
                {
                    mergeStart = i;
                }
                else if (mergeState.equals(Tuv.MERGE_END))
                {
                    List targetSegmentList = p_targetSegments.subList(
                            mergeStart, i + 1);
                    Tuv sourceSegment = getMergedSourceSegment(
                            targetSegmentList, sourceSegmentMap);

                    stripExtraSpaces(conn,
                            (Tuv) p_targetSegments.get(mergeStart),
                            sourceSegment, p_jobId);
                }
            }
        }
        catch (Exception ex)
        {
            s_logger.error("stripExtraSpaces: " + ex.getMessage(), ex);
            throw new PageException(ex);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private ArrayList removeNotTranslateTag(ArrayList p_targetSegments)
    {
        for (int i = 0; i < p_targetSegments.size(); i++)
        {
            Tuv targetSegment = (Tuv) p_targetSegments.get(i);
            String content = targetSegment.getGxml();
            content = content.replaceAll("\\&lt;"
                    + SegmentUtil.XML_NOTCOUNT_TAG + "\\&gt;", "");
            content = content.replaceAll("\\&lt;/"
                    + SegmentUtil.XML_NOTCOUNT_TAG + "\\&gt;", "");
            targetSegment.setGxml(content);
            p_targetSegments.set(i, targetSegment);
        }

        return p_targetSegments;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private void stripExtraSpaces(Connection p_connection, Tuv p_targetSegment,
            Tuv p_sourceSegment, long p_jobId) throws Exception
    {
        Map modifiedSubs = new HashMap();
        String changedText = null;

        if (p_targetSegment.isLocalizable(p_jobId))
        {
            String sourceText = p_sourceSegment.getGxmlExcludeTopTags();
            String targetText = p_targetSegment.getGxmlExcludeTopTags();

            changedText = doStripSpaces(sourceText, targetText);
            if (changedText != null)
            {
                p_targetSegment.setGxmlExcludeTopTags(changedText, p_jobId);
            }
        }
        else
        {
            // get all the subflows of the TUV
            List subs = p_targetSegment.getSubflowsAsGxmlElements();

            for (Iterator it = subs.iterator(); it.hasNext();)
            {
                GxmlElement targetSub = (GxmlElement) it.next();

                String locType = targetSub.getAttribute(GxmlNames.SUB_LOCTYPE);
                // check if the subflow is localizable
                if (locType.equals(GxmlNames.LOCALIZABLE))
                {
                    // get source TUV subflow with the same sub id
                    String subId = targetSub.getAttribute(GxmlNames.SUB_ID);
                    GxmlElement sourceSub = p_sourceSegment
                            .getSubflowAsGxmlElement(subId);

                    String sourceSubText = getSubText(sourceSub);
                    String targetSubText = getSubText(targetSub);
                    String changedSubText = null;

                    if (sourceSubText != null && targetSubText != null)
                    {
                        changedSubText = doStripSpaces(sourceSubText,
                                targetSubText);
                    }

                    if (changedSubText != null)
                    {
                        // encode XML string
                        XmlEntities xmlEncoder = new XmlEntities();
                        changedSubText = xmlEncoder
                                .encodeStringBasic(changedSubText);

                        // save it in a Map for storing it in TUV later
                        modifiedSubs.put(subId, changedSubText);
                    }
                }
            }

            if (modifiedSubs.size() > 0)
            {
                p_targetSegment.setSubflowsGxml(modifiedSubs);
            }
        }

        // save the change to the database
        if (changedText != null || modifiedSubs.size() > 0)
        {
            // We don't use TuvManager.updateTuv() because we don't
            // want to change the state of the tuv.
            TuvImpl tuv = (TuvImpl) p_targetSegment;

            tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                    .getExactMatchFormat()));
            tuv.setLastModified(new Date());

            SegmentTuvUtil.updateTuv(p_connection, tuv, p_jobId);
            // HibernateUtil.update(tuv);
        }
    }

    private String getSubText(GxmlElement p_subElement)
    {
        String result = null;

        if (p_subElement != null)
        {
            @SuppressWarnings("rawtypes")
            List children = p_subElement
                    .getChildElements(GxmlElement.TEXT_NODE);

            // We assume there is only one text node under sub element
            // (which is incorrect).
            if (children != null && children.size() > 0)
            {
                TextNode textNode = (TextNode) children.get(0);
                result = textNode.getTextValue();
            }
        }

        return result;
    }

    private String doStripSpaces(String p_sourceText, String p_targetText)
    {
        int sourceLen = p_sourceText.length();
        int targetLen = p_targetText.length();

        boolean hasChanged = false;

        // leading whitespaces
        if ((sourceLen == 0 || !Character.isWhitespace(p_sourceText.charAt(0)))
                && targetLen > 0
                && Character.isWhitespace(p_targetText.charAt(0)))
        {
            p_targetText = Text.removeLeadingSpaces(p_targetText);
            targetLen = p_targetText.length();
            hasChanged = true;
        }

        // trailing whitespaces
        if ((sourceLen == 0 || !Character.isWhitespace(p_sourceText
                .charAt(sourceLen - 1)))
                && targetLen > 0
                && Character.isWhitespace(p_targetText.charAt(targetLen - 1)))
        {
            p_targetText = Text.removeTrailingSpaces(p_targetText);
            hasChanged = true;
        }

        return hasChanged ? p_targetText : null;
    }

    // get source segements of the page.
    // @return key: TU id (Long)
    // Value: source segment (Tuv)
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private HashMap getSourceSegments() throws PageException
    {
        HashMap result = new HashMap();

        ArrayList segments = getSegments(m_sourcePage.getGlobalSightLocale());
        for (int i = 0, max = segments.size(); i < max; i++)
        {
            Tuv tuv = (Tuv) segments.get(i);

            result.put(tuv.getTuId(), tuv);
        }

        return result;
    }

    // create a merged source segment that corresponds to a merged
    // target segment (p_targetSegments).
    // returns a clone of the first source segment with a merged text in it.
    // p_targetSegments must be sorted in order.
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private Tuv getMergedSourceSegment(List p_targetSegments,
            Map p_sourceSegmentMap) throws Exception
    {
        ArrayList sourceSegments = new ArrayList();

        for (int i = 0, max = p_targetSegments.size(); i < max; i++)
        {
            Tuv targetSegment = (Tuv) p_targetSegments.get(i);
            Tuv sourceSegment = (Tuv) p_sourceSegmentMap.get(targetSegment
                    .getTuId());
            sourceSegments.add(sourceSegment);
        }

        // create source tuv copy
        TuvImpl orgTuv = (TuvImpl) sourceSegments.get(0);
        TuvImpl newTuv = new TuvImpl(orgTuv);
        newTuv.setId(orgTuv.getId());

        // set combined text
        String tuvText = TuvMerger.getMergedText(sourceSegments);
        newTuv.setGxml/* WithSubIds */(tuvText);

        return newTuv;
    }

    /**
     * Count trailing spaces num of the specified string
     * 
     * @param p_string
     * @return
     */
    private int countTrailingSpaceNum(String p_string)
    {
        int trailingSpaceNum = 0;
        String tmpStr = p_string;
        boolean isContinue = true;
        while (isContinue)
        {
            if (Character.isWhitespace(tmpStr.charAt(tmpStr.length() - 1)))
            {
                trailingSpaceNum++;
                tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
            }
            else
            {
                isContinue = false;
            }
        }

        return trailingSpaceNum;
    }

    public void setUserId(String uid)
    {
        m_userid = uid;
    }

    public void setEditorState(EditorState state)
    {
        m_editorState = state;
    }
}

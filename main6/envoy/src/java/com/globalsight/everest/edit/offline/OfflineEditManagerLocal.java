/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.edit.offline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.download.DownLoadApi;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.rtf.ParaViewWorkDocWriter;
import com.globalsight.everest.edit.offline.ttx.TTXParser;
import com.globalsight.everest.edit.offline.upload.FormatTwoEncodingSniffer;
import com.globalsight.everest.edit.offline.upload.UploadApi;
import com.globalsight.everest.edit.offline.upload.UploadPageSaverException;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.rtf.RtfInfo;
import com.globalsight.ling.rtf.RtfObject;
import com.globalsight.ling.rtf.RtfParagraph;
import com.globalsight.ling.rtf.RtfText;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.util.zip.ZipIt;

/**
 * Interface Implementation
 * 
 * @see OfflineEditManager
 */
public class OfflineEditManagerLocal implements OfflineEditManager
{
    static private final Logger s_category = Logger
            .getLogger(OfflineEditManagerLocal.class);

    /**
     * Identifies our standard extracted offline text files (example: list view
     * - text format upload)
     */
    static private final REProgram RE_OFFLINE_TEXT_FILE_SIGNATURE = createProgram("(.*)"
            + "(" + AmbassadorDwUpConstants.SIGNATURE + ")" + "(.*)");

    /** Regex that identifies any RTF file. */
    static private final REProgram RE_RTF1_FILE_SIGNATURE = createProgram("^\\{\\\\rtf1");

    static private REProgram createProgram(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }

        return pattern;
    }

    // Upload types - file format detection
    static private final int UPLOAD_TYPE_DETECTION_ERROR = 0;

    static private final int UPLOAD_TYPE_UNKNOWN = 1;

    static private final int UPLOAD_TYPE_UNKNOWN_UNICODE_TEXT = 2;

    static private final int UPLOAD_TYPE_GS_UNICODE_TEXT = 3;

    static private final int UPLOAD_TYPE_GS_PARAVIEW_1 = 4;

    static private final int UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT = 5;

    static private final int UPLOAD_TYPE_XLF = 6;

    static private final int UPLOAD_TYPE_TTX = 7;

    static private int counter = 0;

    private boolean isXlfOrTtxException = false;

    // static private int m_totalFiles = 0;

    private OEMProcessStatus m_status = null;

    private ResourceBundle m_resource = null;

    /** Holds the results of the file format discovery process. */
    private class DetectionResult
    {
        /** The type of file detected */
        public int m_type = UPLOAD_TYPE_UNKNOWN;

        /**
         * The Reader that backs all types. If the type is
         * UPLOAD_TYPE_GS_PARAVIEW_1 or UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT, it
         * has been parsed into a DOM and you should use m_rtfDoc to read the
         * file.
         */
        public Reader m_reader = null;

        /**
         * An RtfDocument (a DOM). This DOM is ONLY valid/available if the type
         * is one of our own RTF formats: UPLOAD_TYPE_GS_PARAVIEW_1 or
         * UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT. Otherwise the value is null and
         * subsequent methods read the uploaded content directly.
         */
        public RtfDocument m_rtfDoc = null;
    }

    public OfflineEditManager newInstance()
            throws OfflineEditorManagerException, RemoteException
    {
        OfflineEditManager manager = new OfflineEditManagerLocal();
        return new OfflineEditManagerWLImpl(manager);
    }

    /**
     * Creates new OfflineEditManagerLocal
     */
    public OfflineEditManagerLocal() throws OfflineEditorManagerException
    {
        super();
    }

        /****** START: PROCESS DOWNLOAD REQUEST ******/
    public void processDownloadRequest(final DownloadParams p_params)
            throws OfflineEditorManagerException, RemoteException
    {
        // Note: Currently ther is only one thread encompassing the
        // entire download process - used to enable process status
        // feedback.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runProcessDownloadRequest(p_params);
                }
                catch (Throwable e)
                {
                    s_category.error("Can't process download request", e);

                    try
                    {
                        m_status.speakRed(m_status.getTotalFiles(), e
                                .getMessage(), m_resource
                                .getString("msg_dnld_abort"));
                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
                finally
                {
                    HibernateUtil.closeSession();
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("DOWNLOADER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * Download driver. Interface Implementation.
     * 
     * @see OfflineEditManager interface
     */
    public void runProcessDownloadRequest(DownloadParams p_params)
            throws OfflineEditorManagerException, RemoteException
    {
        try
        {
            DownLoadApi downloadApi = new DownLoadApi();
            setUILocaleResources(GlobalSightLocale
                    .makeLocaleFromString(p_params.getUiLocale()));

            // Fix for GBS-2036
            p_params.generateUniqueFileName();
            if (p_params.isCreateZip())
            {
                // MAKE PACKAGES
                if (p_params.isSupportFilesOnlyDownload())
                {
                    m_status
                            .speak(
                                    0,
                                    m_resource
                                            .getString("msg_dnld_create_support_files_package"));

                    downloadApi.makeSupportFilesOnlyPackage(p_params, m_status);
                }
                else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TXT)
                {
                    m_status
                            .speak(
                                    0,
                                    m_resource
                                            .getString("msg_dnld_create_text_list_view_package"));

                    downloadApi.makeTxtPackage(p_params, m_status);
                }
                else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)
                {
                    m_status
                            .speak(
                                    0,
                                    m_resource
                                            .getString("msg_dnld_create_rtf_para_view_package"));

                    downloadApi.makeEmbeddedWordClientPackage(p_params,
                            m_status);
                }
                else
                {
                    // This method will create either a normal RTF or
                    // a Trados style RTF based on the file fomat id
                    // set in p_params.
                    if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF)
                    {
                        m_status
                                .speak(
                                        0,
                                        m_resource
                                                .getString("msg_dnld_create_rtf_list_view_package"));
                    }
                    else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF)
                    {
                        m_status
                                .speak(
                                        0,
                                        m_resource
                                                .getString("msg_dnld_create_rtf_trados_list_view_package"));
                    }

                    downloadApi.makeRtfPackage(p_params, m_status);
                }

                m_status.speak(m_status.getTotalFiles(), m_resource
                        .getString("msg_dnld_package_done"));
            }
            else
            {
                // MAKE INDIVIDUAL FILES
                s_category
                        .warn("NOTE: Single-file downloads were removed from the "
                                + "UI some time ago. Therefore the corresponding methods "
                                + "have not been maintained.");
                // downloadApi.makeTxtPages(p_params);
                // speak(m_totalFiles, "done adding files");
            }

            if (!m_status.isAborted())
            {
                m_status.setResults(p_params.getOutputFile());
            }
        }
        catch (Exception ex)
        {
            throw new OfflineEditorManagerException(
                    OfflineEditorManagerException.MSG_INTERNAL_ERROR, null, ex);
        }
    }
    /****** END: PROCESS DOWNLOAD REQUEST ******/
    
    
    /****** START: PROCESS UPLOAD PAGE ******/
    public void processUploadPage(final File p_tmpFile,
            final User p_user, final Task p_task,
            final String p_fileName) throws AmbassadorDwUpException,
            RemoteException
    {
        // Note: Currently ther is only one thread encompassing the
        // entire upload process - used to enable process status
        // feedback.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runProcessUploadPage(p_tmpFile, p_user,
                            p_task, p_fileName);
                }
                catch (Throwable e)
                {
                    s_category.error("Can't process upload request", e);

                    try
                    {
                        if (e instanceof AmbassadorDwUpException)
                        {
                            AmbassadorDwUpException ae = (AmbassadorDwUpException) e;
                            String aeMessage = ae.getMessage();
                            int start, end = 0;
                            start = aeMessage.indexOf("of document file");
                            if (start != -1)
                            {
                                end = aeMessage.indexOf(".tmp")
                                        + ".tmp".length();

                                aeMessage = aeMessage.substring(0, start)
                                        + aeMessage.substring(end);

                                end = aeMessage.indexOf("Nested exception:");
                                aeMessage = aeMessage.substring(0, end);
                                aeMessage = EditUtil
                                        .encodeHtmlEntities(aeMessage);

                                m_status.setResults(aeMessage + "<BR>");
                                m_status.speakRed(m_status.getTotalFiles(),
                                        aeMessage, m_resource
                                                .getString("msg_upld_abort"));
                            }
                            else
                            {
                                String errorMsg = aeMessage;
                                Throwable oe = ae.getOriginalException();
                                if (oe == null)
                                {
                                    oe = ae.getCause();
                                }

                                if (oe != null)
                                {
                                    errorMsg = oe.getMessage();
                                }

                                errorMsg = EditUtil.encodeTohtml(errorMsg);
                                errorMsg = errorMsg.replace("\r\n", "<BR />");
                                errorMsg = errorMsg.replace("\n", "<BR />");
                                errorMsg = errorMsg.replace("\r", "<BR />");
                                m_status.setResults(errorMsg + "<BR />");
                                m_status.speakRed(m_status.getTotalFiles(),
                                        errorMsg, m_resource
                                                .getString("msg_upld_abort"));
                            }
                        }
                        else
                        {
                            m_status.setResults(e.toString() + "<BR>");
                            m_status.speakRed(m_status.getTotalFiles(), e
                                    .getMessage(), m_resource
                                    .getString("msg_upld_abort"));
                        }

                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
            }
        };

        // To support Multi-Company, Must use MultiCompanySupportedThread
        // Thread t = new Thread(runnable);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("UPLOADER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * Upload driver. Interface Implementation.
     * 
     * @see OfflineEditManager interface
     */
    public void runProcessUploadPage(File p_tmpFile,
            User p_user, Task p_task, String p_fileName)
            throws AmbassadorDwUpException
    {
        if (p_fileName != null && p_fileName.endsWith(".zip"))
        {
            // It needs to extract the zip file first when the file type is zip,
            // then call method "processUploadSingleFile" to upload every file.

            String zipDir = p_tmpFile.getPath() + "-1";
            try
            {
                ArrayList files = ZipIt.unpackZipPackage(p_tmpFile.getPath(),
                        zipDir);
                m_status.setTotalFiles(files.size());

                List<TargetPage> pages = new ArrayList<TargetPage>();
                for (Iterator it = files.iterator(); it.hasNext();)
                {
                    File file = new File(zipDir, (String) it.next());
                    TargetPage page = processUploadSingleFile(file,
                            p_user, p_task, file.getName());
                    if (page != null)
                    {
                        pages.add(page);
                    }
                }
                XmlDtdManager.validateTargetPages(pages,
                        XmlDtdManager.OFF_LINE_IMPORT);
            }
            catch (Exception e)
            {
                throw new AmbassadorDwUpException(
                        AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                        e);
            }
        }
        else
        {
            // Direct to upload single file content when the file type is not
            // zip.
            TargetPage page = processUploadSingleFile(p_tmpFile,
                    p_user, p_task, p_fileName);

            XmlDtdManager.validateTargetPage(page,
                    XmlDtdManager.OFF_LINE_IMPORT);
        }
    }
    
    /**
     * Upload single file.
     * 
     * @param p_tmpFile
     *            Temp file in server.
     * @param p_user
     * @param p_task
     * @param p_fileName
     *            need to upload file, the file type can not be zip.
     */
    private TargetPage processUploadSingleFile(File p_tmpFile,
            User p_user, Task p_task, String p_fileName)
    {
        setUILocaleResources(GlobalSightLocale.makeLocaleFromString(p_user
                .getDefaultUILocale()));

        String fileName = m_resource.getString("lb_upload_file") + p_fileName;

        try
        {
            String errorString = null;

            L10nProfile l10nProfile = p_task.getWorkflow().getJob()
                    .getL10nProfile();
            List excludedTus = l10nProfile.getTranslationMemoryProfile()
                    .getJobExcludeTuTypes();

            DetectionResult detect = determineUploadFormat(p_tmpFile, p_user);

            int processedCounter = m_status.getCounter() + 1;
            UploadApi api = new UploadApi();

            switch (detect.m_type)
            {
                case UPLOAD_TYPE_GS_UNICODE_TEXT:
                {
                    errorString = api.processPage(detect.m_reader,
                            p_user, p_task.getId(), p_fileName, excludedTus,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_errchk_in_progress"));
                    m_status.setResults(errorString);
                    break;
                }

                case UPLOAD_TYPE_XLF:
                {
                    errorString = api.processPage(detect.m_reader,
                            p_user, p_task.getId(), p_fileName, excludedTus,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_errchk_in_progress"));
                    m_status.setResults(errorString);
                    break;
                }

                case UPLOAD_TYPE_TTX:
                {
                    errorString = api.processPage(detect.m_reader,
                            p_user, p_task.getId(), p_fileName, excludedTus,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled

                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_errchk_in_progress"));
                    m_status.setResults(errorString);
                    break;
                }

                case UPLOAD_TYPE_GS_PARAVIEW_1:
                {
                    errorString = api.process_GS_PARAVIEW_1(detect.m_rtfDoc,
                            p_user, p_task.getId(), p_fileName,
                            excludedTus, JmsHelper.JMS_UPLOAD_QUEUE);

                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_rtf_paraview"));
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_errchk_in_progress"));
                    m_status.setResults(errorString);
                    break;
                }

                case UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT:
                {
                    errorString = api.process_GS_WRAPPED_UNICODE_TEXT(
                            detect.m_rtfDoc, p_user, p_task
                                    .getId(), p_fileName, excludedTus,
                            JmsHelper.JMS_UPLOAD_QUEUE);

                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_rtf_listview"));
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_errchk_in_progress"));
                    m_status.setResults(errorString);
                    break;
                }

                case UPLOAD_TYPE_DETECTION_ERROR:
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_unknown"));
                    // intentional fall through
                    // the discovery process failed to execute normally
                case UPLOAD_TYPE_UNKNOWN_UNICODE_TEXT:
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_unknown_unicode_txt"));
                    // intentional fall through
                case UPLOAD_TYPE_UNKNOWN:
                    // at this point,
                    // we have to assume an unextracted file is
                    // being uploaded, if the embedded filename ids are not
                    // present or incorrect, an applicable error page will
                    // be returned to the user.
                    try
                    {
                        m_status.speak(processedCounter, fileName);
                        m_status.speak(processedCounter, m_resource
                                .getString("msg_upld_format_unextracted"));
                        m_status
                                .speak(
                                        processedCounter,
                                        m_resource
                                                .getString("msg_upld_reading_unextracted_file"));

                        errorString = api
                                .doUnextractedFileUpload(p_tmpFile,
                                        p_user, p_task.getId(),
                                        p_fileName);
                        m_status.setResults(errorString);
                    }
                    catch (Exception ex)
                    {
                        s_category.error("Can't upload un-extracted file", ex);

                        throw new AmbassadorDwUpException(
                                AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                                ex);
                    }
                    break;

                default:
                    break;
            }

            if (detect.m_reader != null)
            {
                detect.m_reader.close();
            }

            OfflineEditHelper.deleteFile(p_tmpFile);

            OfflinePageData data = api.getUploadPageData();
            if (data != null)
            {
                if (data.isConsolated())
                {
                    // multiple file to one xliff
                    String pageId = data.getPageId();
                    String[] pageIds = pageId.split(",");
                    return getTargetPage(Long.parseLong(pageIds[0]), data
                            .getTargetLocaleName());
                }
                else
                    return getTargetPage(Long.parseLong(data.getPageId()), data
                            .getTargetLocaleName());
            }

            return null;
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR, ex);
        }
    }
    /****** END: PROCESS UPLOAD PAGE ******/
    
    
    /****** START: PROCESS UPLOAD REPORT PAGE ******/
    public void processUploadReportPage(final File p_tmpFile,
            final User p_user, final Task p_task,
            final String p_fileName, final String p_reportName)
            throws AmbassadorDwUpException, RemoteException
    {
        // Note: Currently ther is only one thread encompassing the
        // entire upload process - used to enable process status
        // feedback.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runProcessUploadReportPage(p_tmpFile, p_user,
                            p_task, p_fileName, p_reportName);
                }
                catch (Throwable e)
                {
                    s_category.error("Can't process upload request", e);

                    try
                    {
                        m_status.setResults(e.toString() + "<BR>");
                        m_status.speakRed(m_status.getTotalFiles(), e
                                .getMessage(), m_resource
                                .getString("msg_upld_abort"));
                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
                finally
                {
                    HibernateUtil.closeSession();
                }
            }
        };

        // To support Multi-Company, Must use MultiCompanySupportedThread
        // Thread t = new Thread(runnable);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("UPLOADER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * Upload driver. Interface Implementation.
     * 
     * @see OfflineEditManager interface
     */
    public void runProcessUploadReportPage(File p_tmpFile,
            User p_user, Task p_task, String p_fileName, String p_reportName)
            throws AmbassadorDwUpException
    {
        setUILocaleResources(GlobalSightLocale.makeLocaleFromString(p_user
                .getDefaultUILocale()));

        String fileName = m_resource.getString("lb_upload_file") + p_fileName;

        try
        {
            String errorString = null;
            m_status.speak(0, fileName);
            m_status.speak(0, m_resource.getString("msg_upld_format_xls"));
            m_status.speak(0, m_resource
                    .getString("msg_upld_errchk_in_progress"));

            UploadApi api = new UploadApi();

            errorString = api.processReport(p_tmpFile, p_user,
                    p_task.getId(), p_fileName, JmsHelper.JMS_UPLOAD_QUEUE,
                    p_reportName);

            OfflineEditHelper.deleteFile(p_tmpFile);

            m_status.setResults(errorString);
            m_status.setCounter(1);
            m_status.setPercentage(100);
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR, ex);
        }
    }
    /****** END: PROCESS UPLOAD REPORT PAGE ******/

    private TargetPage getTargetPage(long p_sourcePageId,
            String targetLocaleName) throws UploadPageSaverException
    {

        PageManager mgr = null;

        try
        {
            GlobalSightLocale trgLoc = ServerProxy.getLocaleManager()
                    .getLocaleByString(targetLocaleName);
            mgr = ServerProxy.getPageManager();
            return mgr.getTargetPage(p_sourcePageId, trgLoc.getId());
        }
        catch (Exception ex)
        {
            s_category.error(ex);
            throw new UploadPageSaverException(ex);
        }
    }

    private DetectionResult determineUploadFormat(File p_tmpFile, User p_user)
            throws Exception
    {
        DetectionResult rslt = new DetectionResult();

        try
        {
            // First we assume the basics - that this is a text file upload
            FormatTwoEncodingSniffer conv = new FormatTwoEncodingSniffer();
            FileInputStream is = new FileInputStream(p_tmpFile);
            InputStreamReader isr = conv.convertFileToUnicode(is);
            BufferedReader br = new BufferedReader(isr);

            String content = br.readLine();
            RE rtfRe = new RE(RE_RTF1_FILE_SIGNATURE);
            RE txtRe = new RE(RE_OFFLINE_TEXT_FILE_SIGNATURE);

            if (rtfRe.match(content))
            {
                RtfAPI api = new RtfAPI();
                RtfDocument doc = api.parse(br);
                doc = api.optimize(doc);

                if (isParaViewOneWorkFile(doc))
                {
                    s_category.debug("Detected UPLOAD_TYPE_GS_PARAVIEW_1");

                    rslt.m_type = UPLOAD_TYPE_GS_PARAVIEW_1;
                    rslt.m_rtfDoc = doc;
                    rslt.m_reader = br;
                }
                else if (isRtfWrappedTextWorkFile(doc))
                {
                    s_category
                            .debug("Detected UPLOAD_TYPE_GS_RTF_WRAPPED_UNICODE_TEXT");

                    rslt.m_type = UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT;
                    rslt.m_rtfDoc = doc;
                    rslt.m_reader = br;
                }
                else
                {
                    s_category.debug("Detected UPLOAD_TYPE_UNKNOWN");

                    rslt.m_type = UPLOAD_TYPE_UNKNOWN;
                    rslt.m_rtfDoc = null;
                    rslt.m_reader = null;
                }
            }
            else if (txtRe.match(content))
            {
                s_category.debug("Detected UPLOAD_TYPE_GS_UNICODE_TEXT.");

                rslt.m_type = UPLOAD_TYPE_GS_UNICODE_TEXT;

                // RESET NOTE: We reset the reader by creating a new
                // one using the detected encoding.

                // The number of characters read by readLine() is
                // dependent on whether or not there are any carriage
                // returns in the file, so it is too risky. According
                // to the mark method, reset may fail.
                rslt.m_reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(p_tmpFile), isr.getEncoding()));
            }
            else if (content.indexOf("<?xml") > -1)
            {
                content = br.readLine();
                // read the second non-null line
                while (content == null || "".equals(content.trim()))
                {
                    content = br.readLine();
                }
                // Xliff file
                if (content.indexOf("<xliff") > -1)
                {
                    rslt.m_type = UPLOAD_TYPE_XLF;

                    String xlfContent = "";
                    try
                    {
                        Document doc = convertXlif2Pseudo(p_tmpFile, p_user);
                        XlfParser parser = new XlfParser();
                        xlfContent = parser.parseToTxt(doc);
                    }
                    catch (Exception de)
                    {
                        s_category.error("xlf parse error");
                        s_category.error(de.getMessage());
                        isXlfOrTtxException = true;
                        throw de;
                    }
                    StringReader sr = new StringReader(xlfContent);
                    rslt.m_reader = new BufferedReader(sr);
                }
                // TTX file
                else if (content.indexOf("<TRADOStag") > -1)
                {
                    rslt.m_type = UPLOAD_TYPE_TTX;

                    String ttxContent = "";
                    try
                    {
                        SAXReader reader = new SAXReader();
                        Document doc = reader.read(p_tmpFile);
                        TTXParser parser = new TTXParser();
                        boolean isParsingTTXForGS = true;
                        ttxContent = parser.parseToTxt(doc, isParsingTTXForGS);
                    }
                    catch (Exception de)
                    {
                        s_category.error("ttx parse error");
                        s_category.error(de.getMessage());
                        isXlfOrTtxException = true;
                        throw de;
                    }

                    StringReader sr = new StringReader(ttxContent);
                    rslt.m_reader = new BufferedReader(sr);
                }
            }
            else
            {
                // Now, we may have correctly detected unicode but it
                // lacks our signature. At this point we can only
                // assume this is a native **unextracted** unicode
                // text file.
                s_category.debug("Detected UPLOAD_TYPE_UNKNOWN_UNICODE_TEXT.");

                rslt.m_type = UPLOAD_TYPE_UNKNOWN_UNICODE_TEXT;

                // see the "RESET NOTE:" above
                rslt.m_reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(p_tmpFile), isr.getEncoding()));
            }
        }
        catch (Exception ex)
        {
            boolean isExHandled = false;
            try
            {
                // If we received an exception while trying to convert
                // the file to unicode text above, we know the file is
                // not a standard offlineTextFile. So now lets try RTF.
                // Note: rtf is always ASCII.

                FileInputStream is = new FileInputStream(p_tmpFile);
                InputStreamReader isr = new InputStreamReader(is, "ASCII");
                BufferedReader br = new BufferedReader(isr);

                // First - confirm we have some form of RTF
                String tmp = br.readLine();
                RE matcher = new RE(RE_RTF1_FILE_SIGNATURE);
                if (matcher.match(tmp))
                {
                    isExHandled = true;
                    // If we have RTF, parse it into an RTF object model.

                    // see the "RESET NOTE:" above
                    br = new BufferedReader(new InputStreamReader(
                            new FileInputStream(p_tmpFile), "ASCII"));

                    RtfAPI api = new RtfAPI();
                    RtfDocument doc = api.parse(br);
                    doc = api.optimize(doc);

                    if (isParaViewOneWorkFile(doc))
                    {
                        s_category.debug("Detected UPLOAD_TYPE_GS_PARAVIEW_1");

                        rslt.m_type = UPLOAD_TYPE_GS_PARAVIEW_1;
                        rslt.m_rtfDoc = doc;
                        rslt.m_reader = br;
                    }
                    else if (isRtfWrappedTextWorkFile(doc))
                    {
                        s_category
                                .debug("Detected UPLOAD_TYPE_GS_RTF_WRAPPED_UNICODE_TEXT");

                        rslt.m_type = UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT;
                        rslt.m_rtfDoc = doc;
                        rslt.m_reader = br;
                    }
                    else
                    {
                        s_category.debug("Detected UPLOAD_TYPE_UNKNOWN");

                        rslt.m_type = UPLOAD_TYPE_UNKNOWN;
                        rslt.m_rtfDoc = null;
                        rslt.m_reader = null;
                    }
                }
            }
            catch (Exception ex2)
            {
                s_category
                        .error("Failed to run the RTF detection process. "
                                + "Retuning UPLOAD_TYPE_DETECTION_ERROR. The exception follows: \n"
                                + ex2.toString());

                rslt.m_type = UPLOAD_TYPE_DETECTION_ERROR;
            }

            if (isXlfOrTtxException)
            {
                throw ex;
            }
            
            if (!isExHandled)
            {
                // throw ex;
                s_category.debug("Detected UPLOAD_TYPE_UNKNOWN");

                rslt.m_type = UPLOAD_TYPE_UNKNOWN;
                rslt.m_rtfDoc = null;
                rslt.m_reader = null;
            }
        }

        return rslt;
    }

    /**
     * Convert xliff elements to Pseudo
     * 
     * @param file
     *            : the file to offline upload.
     * @return
     */
    private Document convertXlif2Pseudo(File file, User p_user)
    {
        SAXReader reader = new SAXReader();
        org.dom4j.Document doc = null;
        try
        {
            doc = reader.read(file);
        }
        catch (Exception e)
        {
            s_category.error(e);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT, e);
        }
        
        // Only when the source file is XLF, need re-wrap the off-line
        // down-loaded XLIFF file.
        String sourceFileType = getSourceFileType(doc);
        if (sourceFileType != null
                && ("xlf".equalsIgnoreCase(sourceFileType) 
                        || "xliff".equalsIgnoreCase(sourceFileType) ) )
        {
            reWrapXliff(doc);
        }

        convertNode2Pseudo(doc, XliffConstants.SOURCE);
        convertNode2Pseudo(doc, XliffConstants.TARGET);

        Transaction tx = HibernateUtil.getTransaction();
        try
        {
            addComment(doc, p_user);
            HibernateUtil.commit(tx);
        }
        catch (HibernateException e)
        {
            HibernateUtil.rollback(tx);
            throw e;
        }
        catch (IllegalArgumentException e)
        {
            HibernateUtil.rollback(tx);
            throw e;
        }

        return doc;
    }

    private void reWrapXliff(Document doc)
    {
        Element root = doc.getRootElement();
        Element bodyElement = root.element(XliffConstants.FILE).element(
                XliffConstants.BODY);
        StringBuffer xliffString = new StringBuffer();
        xliffString.append("<?xml version=\"1.0\"?>");
        xliffString.append("<xliff version=\"1.2\">");
        xliffString.append("<file>");
        xliffString.append("<body>");

        for (Iterator i = bodyElement
                .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
        {
            Element foo = (org.dom4j.Element) i.next();
            Element sourceElement = foo.element(XliffConstants.SOURCE);
            String sourceContent = sourceElement.asXML();
            sourceContent = sourceContent.replaceFirst("<"
                    + XliffConstants.SOURCE + "[^>]*>", "");
            sourceContent = sourceContent.replace("</" + XliffConstants.SOURCE
                    + ">", "");
            Element targetElement = foo.element(XliffConstants.TARGET);
            String targetContent = targetElement.asXML();
            targetContent = targetContent.replaceFirst("<"
                    + XliffConstants.TARGET + "[^>]*>", "");
            targetContent = targetContent.replace("</" + XliffConstants.TARGET
                    + ">", "");

            xliffString.append("<trans-unit><source>" + sourceContent
                    + "</source>");
            xliffString.append("<target>" + targetContent
                    + "</target></trans-unit>");

        }
        xliffString.append("</body>");
        xliffString.append("</file>");
        xliffString.append("</xliff>");

        DiplomatAPI api = new DiplomatAPI();
        api.setEncoding("UTF-8");
        api.setLocale(m_resource.getLocale());
        api.setInputFormat("xlf");
        api.setSentenceSegmentation(false);
        api.setSegmenterPreserveWhitespace(true);
        api.setSourceString(xliffString.toString());

        ArrayList<String> sourceArray = new ArrayList<String>();
        ArrayList<String> targetArray = new ArrayList<String>();

        try
        {
            api.extract();
            Output output = api.getOutput();

            for (Iterator it = output.documentElementIterator(); it.hasNext();)
            {
                DocumentElement element = (DocumentElement) it.next();

                if (element instanceof TranslatableElement)
                {
                    TranslatableElement trans = (TranslatableElement) element;

                    SegmentNode src = (SegmentNode) (trans.getSegments().get(0));
                    if (trans.getXliffPartByName().equals("source"))
                    {
                        sourceArray
                                .add("<source>"
                                        + replaceEntity(src.getSegment())
                                        + "</source>");
                    }
                    else if (trans.getXliffPartByName().equals("target"))
                    {
                        targetArray
                                .add("<target>"
                                        + replaceEntity(src.getSegment())
                                        + "</target>");
                    }
                }
            }

        }
        catch (Exception e)
        {
            if (s_category.isDebugEnabled())
            {
                s_category.error(e.getMessage());
            }
        }

        int index = 0;

        if (sourceArray != null && targetArray != null)
        {
            for (Iterator i = bodyElement
                    .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
            {
                if (index >= sourceArray.size())
                {
                    break;
                }

                Element foo = (org.dom4j.Element) i.next();
                Element sourceElement = foo.element(XliffConstants.SOURCE);
                Element targetElement = foo.element(XliffConstants.TARGET);
                Element newSourceElement = getDom(sourceArray.get(index))
                        .getRootElement();
                foo.remove(sourceElement);
                foo.add(newSourceElement);

                Element newTargetElement = getDom(targetArray.get(index))
                        .getRootElement();
                foo.remove(targetElement);
                foo.add(newTargetElement);
                index++;
            }
        }
    }

    /*
     * Because the getDom will deEntity the code when parse, so before parse,
     * first entity the code.
     */
    private String replaceEntity(String str)
    {
        str = str.replaceAll("&amp;", "&amp;amp;");
        str = str.replaceAll("&lt;", "&amp;lt;");
        str = str.replaceAll("&gt;", "&amp;gt;");
        str = str.replaceAll("&quot;", "&amp;quot;");
        str = str.replaceAll("&apos;", "&amp;apos;;");
        str = str.replaceAll("&#xa;", "&amp;#xa;");
        str = str.replaceAll("&#xd;", "&amp;#xd;");
        str = str.replaceAll("&#x9;", "&amp;#x9");

        return str;
    }

    private static Document getDom(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("invalid GXML `" + p_xml + "': "
                    + ex.getMessage());
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    private void convertNode2Pseudo(Document doc, String tagName)
    {
        Element root = doc.getRootElement();
        Element foo;

        Element bodyElement = root.element(XliffConstants.FILE).element(
                XliffConstants.BODY);
        for (Iterator i = bodyElement
                .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
        {
            foo = (org.dom4j.Element) i.next();
            Element sourceElement = foo.element(tagName);
            String textContent = sourceElement.asXML();
            textContent = textContent
                    .replaceFirst("<" + tagName + "[^>]*>", "");
            textContent = textContent.replace("</" + tagName + ">", "");
            textContent = convertSegment2Pseudo(textContent);
            sourceElement.setText(textContent);
        }
    }

    private String convertSegment2Pseudo(String textContent)
    {
        PseudoData PTagData = null;
        TmxPseudo convertor = null;

        // Create PTag resources
        PTagData = new PseudoData();
        PTagData.setMode(2);
        convertor = new TmxPseudo();

        // configure addable ptags for this format
        PTagData.setAddables("html");

        // convert the current source text and
        // set the native map to represent source tags
        textContent = XliffInternalTag.revertXliffInternalText(textContent);
        try
        {
            convertor.tmx2Pseudo(textContent, PTagData);
        }
        catch (DiplomatBasicParserException e)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_GXML, e
                            + "\n\nString for above exception: " + textContent);
        }
        return PTagData.getPTagSourceString();
    }

    /**
     * Adds all comments to database. The path is trans-unit/note.
     * 
     * @param doc
     *            The document of the xliff file.
     * @param p_user
     *            The user who uploaded the xliff file.
     */
    private void addComment(Document doc, User p_user)
    {
        TargetPage tPage = null;

        XmlEntities entity = new XmlEntities();

        Element root = doc.getRootElement();
        Element file = root.element(XliffConstants.FILE);
        String target = file.attributeValue("target-language");
        target = target.replace("-", "_");

        org.dom4j.Element bodyElement = file.element(XliffConstants.BODY);
        for (Iterator i = bodyElement
                .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
        {
            Element foo = (Element) i.next();
            for (Iterator notes = foo.elementIterator(XliffConstants.NOTE); notes
                    .hasNext();)
            {
                Element note = (Element) notes.next();
                List elements = note.elements();
                String msg = m_resource.getString("msg_note_format_error");

                if (elements == null || note.content().size() == 0)
                {
                    continue;
                }

                for (Object obj : note.content())
                {
                    if (!(obj instanceof DefaultText))
                    {
                        s_category.error(msg);
                        s_category.error("Error note: " + note.asXML());
                        throw new IllegalArgumentException(msg);
                    }
                }

                String textContent = note.getText();
                textContent = entity.decodeString(textContent, null);

                String tuId = foo.attributeValue("id");
                TuImpl tu = HibernateUtil.get(TuImpl.class, Long
                        .parseLong(tuId));
                for (Object ob : tu.getTuvs())
                {
                    TuvImpl tuv = (TuvImpl) ob;
                    if (tPage == null)
                    {
                        tPage = tuv.getTargetPage();
                    }

                    if (tuv.getGlobalSightLocale().toString().equalsIgnoreCase(
                            target))
                    {
                        String title = String.valueOf(tu.getId());
                        String priority = "Medium";
                        String status = "open";
                        String category = "Type01";

                        IssueImpl issue = tuv.getComment();
                        if (issue == null)
                        {
                            String key = CommentHelper.makeLogicalKey(tPage
                                    .getId(), tu.getId(), tuv.getId(), 0);
                            issue = new IssueImpl(Issue.TYPE_SEGMENT, tuv
                                    .getId(), title, priority, status,
                                    category, p_user.getUserId(), textContent,
                                    key);
                            issue.setShare(false);
                            issue.setOverwrite(false);
                        }
                        else
                        {
                            issue.setTitle(title);
                            issue.setPriority(priority);
                            issue.setStatus(status);
                            issue.setCategory(category);
                            issue.addHistory(p_user.getUserId(), textContent);
                            issue.setShare(false);
                            issue.setOverwrite(false);
                        }

                        HibernateUtil.saveOrUpdate(issue);
                        break;
                    }
                }

            }
        }
    }

    private boolean isParaViewOneWorkFile(RtfDocument p_doc)
    {
        boolean rslt = false;
        RtfInfo info = null;

        if ((info = p_doc.getInfo()) != null)
        {
            rslt = ParaViewWorkDocWriter.WORK_DOC_TITLE.equalsIgnoreCase(info
                    .getProperty(RtfInfo.TITLE));
        }

        return rslt;
    }

    private boolean isRtfWrappedTextWorkFile(RtfDocument p_doc)
    {
        RtfObject docObj = null;
        RtfObject obj = null;

        for (int i = 0, max1 = p_doc.size(); i < max1; i++)
        {
            docObj = p_doc.getObject(i);

            if (docObj instanceof RtfParagraph)
            {
                RtfParagraph para = (RtfParagraph) docObj;

                for (int j = 0, max2 = para.size(); j < max2; j++)
                {
                    obj = para.getObject(j);
                    if (obj instanceof RtfText)
                    {
                        // check only the first instance of text
                        RE matcher = new RE(RE_OFFLINE_TEXT_FILE_SIGNATURE);
                        return matcher.match(obj.getData());
                    }
                }
            }
        }

        return false;
    }

    public void attachListener(IProcessStatusListener p_listener)
    {
        m_status = (OEMProcessStatus) p_listener;
    }

    public void detachListener()
    {
        m_status = null;
    }

    private void setUILocaleResources(Locale p_locale)
    {
        SystemResourceBundle srb = SystemResourceBundle.getInstance();

        m_resource = srb.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);
    }
    
    private String getSourceFileType(Document p_doc)
    {
        String result = null;
        try
        {
            Element root = p_doc.getRootElement();
            Element noteElement = root.element(XliffConstants.FILE).element(
                    XliffConstants.HEADER).element(XliffConstants.NOTE);
            String notes = noteElement.getText();
            int index = notes.indexOf("Document Format");
            notes = notes.substring(index);
            notes = notes.substring(0, notes.indexOf("#")).trim();
            notes = notes.substring(notes.indexOf(":")+1).trim();
            result = notes;
        }
        catch (Exception ex)
        {
            
        }
        
        return result;
    }
    
}

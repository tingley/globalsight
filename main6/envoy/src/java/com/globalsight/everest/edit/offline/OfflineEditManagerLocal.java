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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultText;
import org.hibernate.Transaction;
import org.jboss.util.Strings;

import com.globalsight.config.UserParameter;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.offline.download.DownLoadApi;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.rtf.ParaViewWorkDocWriter;
import com.globalsight.everest.edit.offline.ttx.TTXParser;
import com.globalsight.everest.edit.offline.upload.CheckResult;
import com.globalsight.everest.edit.offline.upload.FormatTwoEncodingSniffer;
import com.globalsight.everest.edit.offline.upload.PtagErrorPageWriter;
import com.globalsight.everest.edit.offline.upload.UploadApi;
import com.globalsight.everest.edit.offline.upload.UploadPageSaverException;
import com.globalsight.everest.edit.offline.xliff.XLIFFStandardUtil;
import com.globalsight.everest.edit.offline.xliff.xliff20.ListViewWorkXLIFF20Writer;
import com.globalsight.everest.edit.offline.xliff.xliff20.Tmx2Xliff20;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.DownloadOfflineFilesConfigHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflowmanager.Workflow;
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
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ObjectUtil;
import com.globalsight.util.PropertiesFactory;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.util.zip.ZipIt;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Interface Implementation
 * 
 * @see OfflineEditManager
 */
public class OfflineEditManagerLocal implements OfflineEditManager, Cancelable
{
    static private final Logger s_category = Logger
            .getLogger(OfflineEditManagerLocal.class);

    private static Object LOCKER = new Object();
    private static int MAX_THREAD = 5;
    public static List<OfflineUploadForm> WAITING_FORMS = new ArrayList<OfflineUploadForm>();
    public static List<OfflineUploadForm> RUNNING_FORMS = new ArrayList<OfflineUploadForm>();

    // initialize the RUN_MAX_THREAD from
    // "properties/offlineUpload.properties"
    static
    {
        try
        {
            Properties p = (new PropertiesFactory())
                    .getProperties("/properties/offlineUpload.properties");
            MAX_THREAD = Integer.parseInt(p.getProperty("MAX_THREAD"));

            if (MAX_THREAD <= 0)
                MAX_THREAD = 1;
        }
        catch (Exception e)
        {
            s_category.error(e);
        }
    }

    /**
     * Identifies our standard extracted offline text files (example: list view
     * - text format upload)
     */
    static private final REProgram RE_OFFLINE_TEXT_FILE_SIGNATURE = createProgram("(.*)"
            + "(" + AmbassadorDwUpConstants.SIGNATURE + ")" + "(.*)");

    static private final REProgram RE_OFFLINE_TEXT_FILE_SIGNATURE2 = createProgram("(.*)"
            + "(GlobalSight Download File)" + "(.*)");

    /** Regex that identifies any RTF file. */
    static private final REProgram RE_RTF1_FILE_SIGNATURE = createProgram("^\\{\\\\rtf1");

    private UploadApi api = null;
    private SynchronizationManager m_syncManager = null;
    private boolean cancel = false;
    private List<String> m_canceledFiles = null;

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

    static private final int UPLOAD_TYPE_XLF20 = 8;

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
                        m_status.speakRed(m_status.getTotalFiles(),
                                e.getMessage(),
                                m_resource.getString("msg_dnld_abort"));
                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("DOWNLOADER" + String.valueOf(counter++));
        t.start();
    }
    
    static public List<OfflineUploadForm> getCloneRunningRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(RUNNING_FORMS);
        }
    }
    
    static public List<OfflineUploadForm> getCloneHoldingRequests()
    {
        synchronized (LOCKER)
        {
            return ObjectUtil.deepClone(WAITING_FORMS);
        }
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
                    m_status.speak(0, m_resource
                            .getString("msg_dnld_create_support_files_package"));

                    downloadApi.makeSupportFilesOnlyPackage(p_params, m_status);
                }
                else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TXT)
                {
                    m_status.speak(
                            0,
                            m_resource
                                    .getString("msg_dnld_create_text_list_view_package"));

                    downloadApi.makeTxtPackage(p_params, m_status);
                }
                else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)
                {
                    m_status.speak(0, m_resource
                            .getString("msg_dnld_create_rtf_para_view_package"));

                    downloadApi.makeEmbeddedWordClientPackage(p_params,
                            m_status);
                }
                else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT)
                {
                    m_status.speak(0, m_resource
                            .getString("msg_dnld_create_omegat_package"));

                    downloadApi.makeOmegaTPackage(p_params, m_status);
                }
                else
                {
                    // This method will create either a normal RTF or
                    // a Trados style RTF based on the file fomat id
                    // set in p_params.
                    if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF)
                    {
                        downloadApi.setConvertLF(true);
                        m_status.speak(
                                0,
                                m_resource
                                        .getString("msg_dnld_create_rtf_list_view_package"));
                    }
                    else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF)
                    {
                        downloadApi.setConvertLF(true);
                        m_status.speak(
                                0,
                                m_resource
                                        .getString("msg_dnld_create_rtf_trados_list_view_package"));
                    }
                    else if (p_params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED)
                    {
                        downloadApi.setConvertLF(true);
                        m_status.speak(
                                0,
                                m_resource
                                        .getString("msg_dnld_create_rtf_trados_list_view_optimized_package"));
                    }

                    downloadApi.makeRtfPackage(p_params, m_status);
                }

                m_status.speak(m_status.getTotalFiles(),
                        m_resource.getString("msg_dnld_package_done"));
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
            s_category.error("Download internal error. ", ex);
            throw new OfflineEditorManagerException(
                    OfflineEditorManagerException.MSG_INTERNAL_ERROR, null, ex);
        }
    }

    public void processUploadPage(final File p_tmpFile, final User p_user,
            final Task p_task, final String p_fileName)
            throws AmbassadorDwUpException, RemoteException
    {
        final OfflineUploadForm form = new OfflineUploadForm(p_tmpFile, p_user,
                p_task, p_fileName);
        processUploadPage(form);
    }

    public void processUploadPage(final OfflineUploadForm form)
    {
        if (form.getStatus() == null)
        {
            form.setStatus(m_status);
        }

        synchronized (LOCKER)
        {
            if (RUNNING_FORMS.size() >= MAX_THREAD)
            {
                WAITING_FORMS.add(form);
                s_category.info("Putting a Thread in Queue. Max Thread: "
                        + MAX_THREAD + ", Running Thread: "
                        + RUNNING_FORMS.size() + ", Waiting Thread: "
                        + WAITING_FORMS.size());
                return;
            }
        }

        // Note: Currently ther is only one thread encompassing the
        // entire upload process - used to enable process status
        // feedback.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    RUNNING_FORMS.add(form);
                    m_status = form.getStatus();
                    s_category.info("Processing a Running Thread. Max Thread: "
                            + MAX_THREAD + ", Running Thread: "
                            + RUNNING_FORMS.size() + ", Waiting Thread: "
                            + WAITING_FORMS.size());
                    s_category.info("The file name is: " + form.getFileName()
                            + ", user is: " + form.getUser().getUserName());
                    runProcessUploadPage(form.getTmpFile(), form.getUser(),
                            form.getTask(), form.getFileName());
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
                                        aeMessage,
                                        m_resource.getString("msg_upld_abort"));
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

                                if (errorMsg == null)
                                {
                                    errorMsg = aeMessage;
                                }

                                errorMsg = EditUtil.encodeTohtml(errorMsg);
                                errorMsg = errorMsg.replace("\r\n", "<BR />");
                                errorMsg = errorMsg.replace("\n", "<BR />");
                                errorMsg = errorMsg.replace("\r", "<BR />");
                                m_status.setResults(errorMsg + "<BR />");
                                m_status.speakRed(m_status.getTotalFiles(),
                                        errorMsg,
                                        m_resource.getString("msg_upld_abort"));
                            }
                        }
                        else
                        {
                            m_status.setResults(e.toString() + "<BR>");
                            m_status.speakRed(m_status.getTotalFiles(),
                                    e.getMessage(),
                                    m_resource.getString("msg_upld_abort"));
                        }

                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
                finally
                {
                    HibernateUtil.closeSession();

                    OfflineUploadForm waitForm = null;

                    synchronized (LOCKER)
                    {
                        RUNNING_FORMS.remove(form);
                        s_category
                                .info("Cleaned up a Running Thread. Max Thread: "
                                        + MAX_THREAD
                                        + ", Running Thread: "
                                        + RUNNING_FORMS.size()
                                        + ", Waiting Thread: "
                                        + WAITING_FORMS.size());
                        if (WAITING_FORMS.size() > 0)
                        {
                            waitForm = WAITING_FORMS.remove(0);
                        }
                    }

                    if (waitForm != null)
                    {
                        try
                        {
                            processUploadPage(waitForm);
                        }
                        catch (AmbassadorDwUpException e1)
                        {
                            s_category.error(e1);
                        }
                    }
                }
            }
        };

        // To support Multi-Company, Must use MultiCompanySupportedThread
        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("UPLOADER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * For internal tag miss confirm.
     * 
     * <p>
     * GBS-4106, when detecting a thread is waiting for user respond, do not
     * count the waiting thread as an available thread.
     */
    public void startConfirm()
    {
        OfflineUploadForm waitForm = null;
        synchronized (LOCKER)
        {
            MAX_THREAD++;

            if (WAITING_FORMS.size() > 0)
            {
                waitForm = WAITING_FORMS.remove(0);

                s_category.info("Processing a Running Thread. Max Thread: "
                        + MAX_THREAD + ", Running Thread: "
                        + RUNNING_FORMS.size() + ", Waiting Thread: "
                        + WAITING_FORMS.size());
            }
        }

        if (waitForm != null)
        {
            try
            {
                processUploadPage(waitForm);
            }
            catch (AmbassadorDwUpException e1)
            {
                s_category.error(e1);
            }
        }
    }

    /**
     * For internal tag miss confirm.
     * 
     * @since GBS-4106
     */
    public void endConfirm()
    {
        synchronized (LOCKER)
        {
            MAX_THREAD--;
        }
    }

    /**
     * Upload driver. Interface Implementation.
     * 
     * @see OfflineEditManager interface
     */
    public String runProcessUploadPage(File p_tmpFile, User p_user,
            Task p_task, String p_fileName) throws AmbassadorDwUpException
    {
        String errMsg = null;
        m_canceledFiles = new ArrayList<String>();
        String tempFileName = p_tmpFile.getName();
        if (m_syncManager == null)
        {
            m_syncManager = ServerProxy.getSynchronizationManager();
        }
        m_syncManager.setTempFileName(tempFileName);
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
                    Object[] processResult = processUploadSingleFile(file,
                            p_user, p_task, file.getName(), tempFileName);
                    Object trgPage = processResult[0];
                    if (trgPage != null)
                    {
                        pages.add((TargetPage) trgPage);
                    }
                    Object msg = processResult[1];
                    if (msg != null)
                    {
                        errMsg = (String) msg;
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
            Object[] processResult = processUploadSingleFile(p_tmpFile, p_user,
                    p_task, p_fileName, tempFileName);
            Object trgPage = processResult[0];
            if (trgPage != null)
            {
                XmlDtdManager.validateTargetPage((TargetPage) trgPage,
                        XmlDtdManager.OFF_LINE_IMPORT);
            }
            Object msg = processResult[1];
            if (msg != null)
            {
                errMsg = (String) msg;
            }
        }

        return errMsg;
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
    @SuppressWarnings("static-access")
    private Object[] processUploadSingleFile(File p_tmpFile, User p_user,
            Task p_task, String p_fileName, String p_tempFileName)
    {
        List<Long> taskIdList = null;
        Object[] result = new Object[2];
        OfflineFileUploadStatus status = OfflineFileUploadStatus.getInstance();

        setUILocaleResources(GlobalSightLocale.makeLocaleFromString(p_user
                .getDefaultUILocale()));

        String fileName = m_resource.getString("lb_upload_file") + p_fileName;
        // Fix for GBS-2191
        PermissionSet ps = null;
        try
        {
            ps = Permission.getPermissionManager().getPermissionSetForUser(
                    p_user.getUserId());
        }
        catch (Exception e)
        {
            s_category.error(e.getMessage(), e);
        }
        long taskId = p_task == null ? -1 : p_task.getId();
        long oriTaskId = taskId;

        p_task = TaskHelper.getTask(taskId);

        // if user have the permission, he could upload any files within
        // activity
        if (ps.getPermissionFor(Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY))
        {
            taskId = -1;
        }

        String errorString = null;
        try
        {
            List<String> excludedItemTypes = getExcludedItemTypes(p_task);

            int processedCounter = m_status.getCounter() + 1;
            UploadApi api = new UploadApi();
            api.setStatus(m_status);
            api.setTempFileName(p_tempFileName);

            DetectionResult detect = determineUploadFormat(p_tmpFile, p_user);
            switch (detect.m_type)
            {
                case UPLOAD_TYPE_GS_UNICODE_TEXT:
                {
                    errorString = api.processPage(detect.m_reader, p_user,
                            taskId, p_fileName, excludedItemTypes,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_XLF:
                {
                    errorString = convertXlif2Pseudo(detect, p_tmpFile, p_user,
                            p_fileName);
                    // If any error here, must stop, have to skip handling
                    // "good" translations.
                    if (StringUtil.isEmpty(errorString))
                    {
                        errorString = api.processPage(detect.m_reader, p_user,
                                taskId, p_fileName, excludedItemTypes,
                                JmsHelper.JMS_UPLOAD_QUEUE);
                    }
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_XLF20:
                {
                    String txt = Tmx2Xliff20.conveterToTxt(FileUtil
                            .readFile(p_tmpFile,
                                    ListViewWorkXLIFF20Writer.XLIFF_ENCODING));
                    errorString = api.processXliff20(new StringReader(txt),
                            p_fileName, p_user, taskId, excludedItemTypes,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_rtf_listview"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_TTX:
                {
                    errorString = api.processPage(detect.m_reader, p_user,
                            taskId, p_fileName, excludedItemTypes,
                            JmsHelper.JMS_UPLOAD_QUEUE);
                    // Note: final error message is set in the
                    // UploadProgress.jsp
                    // where color formatting can be controlled
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_format_unicode_txt"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_GS_PARAVIEW_1:
                {
                    errorString = api.process_GS_PARAVIEW_1(detect.m_rtfDoc,
                            p_user, taskId, p_fileName, excludedItemTypes,
                            JmsHelper.JMS_UPLOAD_QUEUE);

                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_rtf_paraview"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_GS_WRAPPED_UNICODE_TEXT:
                {
                    errorString = api.process_GS_WRAPPED_UNICODE_TEXT(
                            detect.m_rtfDoc, p_user, taskId, p_fileName,
                            excludedItemTypes, JmsHelper.JMS_UPLOAD_QUEUE);

                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter, m_resource
                            .getString("msg_upld_format_rtf_listview"));
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_errchk_in_progress"));
                    processUploadResult(errorString, processedCounter);
                    break;
                }

                case UPLOAD_TYPE_DETECTION_ERROR:
                    m_status.speak(processedCounter, fileName);
                    m_status.speak(processedCounter,
                            m_resource.getString("msg_upld_format_unknown"));
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
                        m_status.speak(processedCounter, m_resource
                                .getString("msg_upld_reading_unextracted_file"));

                        errorString = api.doUnextractedFileUpload(p_tmpFile,
                                p_user, taskId, p_fileName);
                        m_status.setResults(errorString);
                        String unextractedFileTaskId = api
                                .getUnextractedFileTaskId();
                        if (unextractedFileTaskId != null)
                        {
                            taskId = Long.parseLong(unextractedFileTaskId);
                        }
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

            OfflinePageData data = api.getUploadPageData();
            if (data != null)
            {
                taskIdList = data.getTaskIds();
                if (taskIdList != null)
                {
                    m_status.setTaskIdList(taskIdList);
                }
                taskId = Long.valueOf(data.getTaskId());
                m_status.addTaskId(taskId);
                String pageId = data.getPageId();
                if (!data.isConsolated() && pageId != null
                        && pageId.contains(","))
                {
                    data.setConsolated(true);
                }

                if (data.isConsolated()
                        && (pageId == null || pageId.length() == 0))
                {
                    data.setConsolated(false);
                }

                status.addFileState(taskId, p_fileName, "Handled");

                if (TaskHelper.getTask(taskId) != null)
                {
                    logUploadResult(data, p_fileName, p_user);
                }

                if (data.isConsolated())
                {
                    // multiple file to one xliff
                    String[] pageIds = pageId.split(",");
                    result[0] = getTargetPage(Long.parseLong(pageIds[0]),
                            data.getTargetLocaleName());
                }
                else
                {
                    if (pageId != null && pageId.length() > 0)
                    {
                        result[0] = getTargetPage(Long.parseLong(pageId),
                                data.getTargetLocaleName());
                    }
                }
            }
            else
            {
                status.addFileState(taskId, p_fileName,
                        "Failed. Error:Cannot generate offline page data successfully.");
            }

            result[1] = errorString;
            return result;
        }
        catch (Exception ex)
        {
            status.addFileState(oriTaskId, p_fileName, "Failed. Error:"
                    + (errorString != null ? errorString : ex.getMessage()));
            s_category.error("processUploadSingleFile error", ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR, ex);
        }
        finally
        {
            try
            {
                if (taskIdList != null)
                {
                    for (Long taskID : taskIdList)
                    {
                        Task task = TaskHelper.getTask(taskID);
                        if (task != null)
                        {
                            saveUploadedFile(p_tmpFile, task, p_fileName, null);
                        }
                    }
                    OfflineEditHelper.deleteFile(p_tmpFile);

                }
                else if (taskId != -1)
                {
                    Task task = TaskHelper.getTask(taskId);
                    if (task != null)
                    {
                        saveUploadedFile(p_tmpFile, task, p_fileName, null);
                    }
                    OfflineEditHelper.deleteFile(p_tmpFile);
                }
            }
            catch (Exception e)
            {
                s_category.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Logs information for each offline report uploaded.
     * 
     * @since GBS-3198
     */
    private void logUploadResultForReport(User user, Task task, String fileName)
    {
        try
        {
            Task tsk = ServerProxy.getTaskManager().getTask(task.getId());
            long jobId = tsk.getJobId();
            String locale = tsk.getTargetLocale().toString();

            s_category.info(user.getUserName() + " uploaded " + fileName
                    + ", job id: " + jobId + ", locale: " + locale);
        }
        catch (Exception e)
        {
            s_category
                    .warn("Ignored: error happens in logUploadResultForReport method : "
                            + e.getMessage());
        }
    }

    /**
     * Logs information for each offline file uploaded.
     * 
     * @since GBS-3198
     */
    private void logUploadResult(OfflinePageData data, String fileName,
            User user) throws Exception
    {
        // refresh the task related object in the hibernate session because they
        // may be updated in another thread through jms while saving page tuvs.
        HibernateUtil.closeSession();
        String taskId = data.getTaskId();
        Task task = TaskHelper.getTask(Long.parseLong(taskId));
        GlobalSightLocale targetLocale = task.getTargetLocale();
        List<Long> taskIdList = new ArrayList<Long>();
        if (data.getTaskIds() != null)
        {
            taskIdList.addAll(data.getTaskIds());
            for (Long taskID : taskIdList)
            {
                Long jobID = TaskHelper.getTask(taskID).getJobId();
                s_category.info(user.getUserName() + " uploaded " + fileName
                        + ", job id: " + jobID + ", locale: "
                        + targetLocale.toString());
            }
        }
        else
        {
            s_category.info(user.getUserName() + " uploaded " + fileName
                    + ", job id: " + task.getJobId() + ", locale: "
                    + targetLocale.toString());
        }
    }

    private void processUploadResult(String errorString, int processedCounter)
            throws IOException
    {
        if (m_status.getIsContinue() != null
                && m_status.getIsContinue().equals(Boolean.FALSE)
                && "IsContinue=fasle".equals(errorString))
        {
            m_canceledFiles.add(errorString);

            CheckResult checkResult = m_status.getCheckResultCopy();
            String rrr = checkResult.getMessage(false);
            m_status.setIsContinue(null);
            m_status.setCheckResultCopy(null);
            if (processedCounter == 1 && m_status.getTotalFiles() == 1)
            {
                rrr = "<div class='headingError'>"
                        + "Page is cancelled for missing following internal tags"
                        + rrr + "</div>";
                m_status.setResults(rrr);
            }
            else if (processedCounter == m_status.getTotalFiles()
                    && m_canceledFiles.size() == m_status.getTotalFiles())
            {
                rrr = "<div class='headingError'><bold>"
                        + "Page is cancelled for missing following internal tags</bold>"
                        + rrr + "</div>";
                m_status.speak(processedCounter, rrr);
                m_status.setResults("<bold>All pages are cancelled for missing internal tags</bold>");
            }
            else
            {
                rrr = "<div class='headingError'><bold>"
                        + "Page is cancelled for missing following internal tags</bold>"
                        + rrr + "</div>";
                m_status.speak(processedCounter, rrr);
            }
        }
        else
            m_status.setResults(errorString);
    }

    /****** END: PROCESS UPLOAD PAGE ******/

    /****** START: PROCESS UPLOAD REPORT PAGE ******/
    public void processUploadReportPage(final File p_tmpFile,
            final User p_user, final Task p_task, final String p_fileName,
            final String p_reportName) throws AmbassadorDwUpException,
            RemoteException
    {
        // Note: Currently there is only one thread encompassing the
        // entire upload process - used to enable process status
        // feedback.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runProcessUploadReportPage(p_tmpFile, p_user, p_task,
                            p_fileName, p_reportName);
                }
                catch (Throwable e)
                {
                    s_category.error("Can't process upload request", e);

                    try
                    {
                        m_status.setResults(e.toString() + "<BR>");
                        m_status.speakRed(m_status.getTotalFiles(),
                                e.getMessage(),
                                m_resource.getString("msg_upld_abort"));
                    }
                    catch (Throwable ex)
                    {
                        s_category.error("UI notification error", ex);
                    }
                }
            }
        };

        // To support Multi-Company, Must use MultiCompanySupportedThread
        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("UPLOADER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * Upload driver. Interface Implementation.
     * 
     * @see OfflineEditManager interface
     */
    public String runProcessUploadReportPage(File p_tmpFile, User p_user,
            Task p_task, String p_fileName, String p_reportName)
            throws AmbassadorDwUpException
    {
        setUILocaleResources(GlobalSightLocale.makeLocaleFromString(p_user
                .getDefaultUILocale()));

        String fileName = m_resource.getString("lb_upload_file") + p_fileName;
        String errorString = null;
        try
        {
            m_status.speak(0, fileName);
            String extension = fileName
                    .substring(fileName.lastIndexOf(".") + 1);
            m_status.speak(0, "Format: " + extension);
            m_status.speak(0,
                    m_resource.getString("msg_upld_errchk_in_progress"));
            if (p_task != null)
            {
                m_status.addTaskId(p_task.getId());
            }
            if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT)
                    || WebAppConstants.LANGUAGE_SIGN_OFF.equals(p_reportName)
                    || WebAppConstants.POST_REVIEW_QA.equals(p_reportName)
                    || WebAppConstants.TRANSLATION_VERIFICATION
                            .equals(p_reportName))
            {
                m_status.setUseProcess(true);
            }

            api = new UploadApi();
            api.setStatus(m_status);

            if (p_task == null)
            {
                errorString = api.reportErrorInfos();
            }
            else
            {
                errorString = api.processReport(p_tmpFile, p_user, p_task,
                        p_fileName, JmsHelper.JMS_UPLOAD_QUEUE, p_reportName);
            }
            m_status.setResults(errorString);
            m_status.setCounter(1);
            m_status.setPercentage(100);

            if (WebAppConstants.TRANSLATION_EDIT.equals(p_reportName)
                    || WebAppConstants.TRANSLATION_VERIFICATION
                            .equals(p_reportName))
            {
                logUploadResultForReport(p_user, p_task, p_fileName);
            }
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR, ex);
        }
        finally
        {
            try
            {
                saveUploadedFile(p_tmpFile, p_task, p_fileName, p_reportName);
                OfflineEditHelper.deleteFile(p_tmpFile);
            }
            catch (Exception e)
            {
                s_category.error(e.getMessage(), e);
            }
        }

        return errorString;
    }

    /**
     * Saves uploaded file into file storage.
     * <p>
     * For GBS-3115.
     */
    private void saveUploadedFile(File p_tmpFile, Task p_task,
            String p_fileName, String p_reportName) throws Exception
    {
        // refresh the task to avoid the
        // "could not initialize proxy - no Session" error
        Task task = TaskHelper.getTask(p_task.getId());
        Project project = ServerProxy.getProjectHandler()
                .getProjectByNameAndCompanyId(task.getProjectName(),
                        task.getCompanyId());
        File uploadDir = AmbFileStoragePathUtils.getUploadDir(project
                .getCompanyId());
        StringBuilder uploadedFilePath = new StringBuilder(uploadDir.getPath());
        uploadedFilePath.append(File.separator);
        uploadedFilePath.append(task.getJobId());
        uploadedFilePath.append(File.separator);
        uploadedFilePath.append(task.getTargetLocale().toString());
        uploadedFilePath.append(File.separator);
        if (p_reportName != null)
        {
            if (WebAppConstants.TRANSLATION_EDIT.equals(p_reportName))
            {
                if (!project.getSaveTranslationsEditReport())
                {
                    return;
                }
                uploadedFilePath.append("Translations Edit Report");
            }
            else if (WebAppConstants.LANGUAGE_SIGN_OFF.equals(p_reportName))
            {
                if (!project.getSaveReviewersCommentsReport())
                {
                    return;
                }
                uploadedFilePath.append("Reviewers Comments Report");
            }
        }
        else
        {
            if (!project.getSaveOfflineFiles())
            {
                return;
            }
            uploadedFilePath.append("Offline Files");
            uploadedFilePath.append(File.separator);
            Workflow wf = task.getWorkflow();
            Collection tasks = ServerProxy.getTaskManager().getCurrentTasks(
                    wf.getId());
            Task oriTask = (Task) tasks.iterator().next();
            uploadedFilePath.append(oriTask.getId()
                    + "_"
                    + oriTask.getTaskName().substring(0,
                            oriTask.getTaskName().lastIndexOf("_")));

        }
        uploadedFilePath.append(File.separator);
        uploadedFilePath.append(p_fileName);

        FileUtil.copyFile(p_tmpFile, new File(uploadedFilePath.toString()));
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
            s_category.error(ex.getMessage(), ex);
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
                    if (content.contains("2.0"))
                    {
                        rslt.m_type = UPLOAD_TYPE_XLF20;
                    }
                    else
                    {
                        rslt.m_type = UPLOAD_TYPE_XLF;
                    }
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
                        s_category.error(de.getMessage(), de);
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
     * @throws Exception
     */
    private String convertXlif2Pseudo(DetectionResult detect, File file,
            User p_user, String p_fileName) throws Exception
    {
        String errMsg = null;
        org.dom4j.Document doc = null;
        try
        {
            SAXReader reader = new SAXReader();
            doc = reader.read(file);
        }
        catch (Exception e)
        {
            s_category.error(e.getMessage(), e);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT, e);
        }

        // Get all jobIds from uploading file. If combined, there will be
        // multiple tasks/pages/jobs in one file.
        HashSet<Long> jobIds = getJobIdsFromDoc(doc);

        PtagErrorPageWriter errWriter = new PtagErrorPageWriter();
        errWriter.setFileName(p_fileName);
        errWriter.setPageId(XliffFileUtil.getPageId(doc));
        errWriter.setTaskId(XliffFileUtil.getTaskId(doc));
        errWriter.setWorkflowId(XliffFileUtil.getWorkflowId(doc));

        reWrapXliff(doc, jobIds);

        errMsg = convertNode2Pseudo(doc, XliffConstants.SOURCE, p_fileName,
                jobIds, errWriter);
        if (errMsg != null)
            return errMsg;

        errMsg = convertNode2Pseudo(doc, XliffConstants.TARGET, p_fileName,
                jobIds, errWriter);
        if (errMsg != null)
            return errMsg;

        Transaction tx = HibernateUtil.getTransaction();
        try
        {
            addComment(doc, p_user, jobIds);
            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(tx);
            throw e;
        }

        try
        {
            XlfParser parser = new XlfParser();
            String xlfContent = parser.parseToTxt(doc);
            StringReader sr = new StringReader(xlfContent);
            detect.m_reader = new BufferedReader(sr);
        }
        catch (Exception e)
        {
            s_category.error(e.getMessage(), e);
            throw e;
        }

        return errMsg;
    }

    /**
     * Only when the source file is XLF, need re-wrap the off-line down-loaded
     * XLIFF file.
     */
    private void reWrapXliff(Document doc, HashSet<Long> jobIds)
    {
        Element root = doc.getRootElement();
        Element bodyElement = root.element(XliffConstants.FILE).element(
                XliffConstants.BODY);

        // Find TU elements that are from XLF source file.
        List<Element> xlfTuElements = new ArrayList<Element>();
        for (Iterator i = bodyElement
                .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
        {
            Element foo = (org.dom4j.Element) i.next();
            if (isSrcFileXlf(foo, jobIds))
            {
                xlfTuElements.add(foo);
            }
        }

        if (xlfTuElements.size() == 0)
            return;

        //
        StringBuffer xliffString = new StringBuffer();
        xliffString.append("<?xml version=\"1.0\"?>");
        xliffString.append("<xliff version=\"1.2\">");
        xliffString.append("<file>");
        xliffString.append("<body>");
        Attribute stateAttr = null;
        ArrayList<String> trgStates = new ArrayList<String>();
        for (Element foo : xlfTuElements)
        {
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

            xliffString.append("<trans-unit>");
            xliffString.append("<source>").append(sourceContent)
                    .append("</source>");
            xliffString.append("<target>").append(targetContent)
                    .append("</target>");
            xliffString.append("</trans-unit>");

            // Store the state attributes in sequence
            stateAttr = targetElement.attribute(XliffConstants.STATE);
            if (stateAttr == null)
            {
                trgStates.add("");
            }
            else
            {
                trgStates.add(stateAttr.getValue());
            }
        }
        xliffString.append("</body>");
        xliffString.append("</file>");
        xliffString.append("</xliff>");

        // Extract it to get re-wrapped segments.
        DiplomatAPI api = new DiplomatAPI();
        api.setEncoding("UTF-8");
        api.setLocale(new Locale("en_US"));
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
                s_category.error(e.getMessage(), e);
            }
        }

        // Replace source/target elements
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
                if (!isSrcFileXlf(foo, jobIds))
                {
                    continue;
                }

                TuImpl tu = getTu(foo, jobIds);
                GxmlElement srcGxmlElement = tu.getSourceTuv().getGxmlElement();
                String newSrc = "<segment>"
                        + GxmlUtil.stripRootTag(sourceArray.get(index))
                        + "</segment>";
                GxmlElement trgGxmlElement = SegmentUtil2
                        .getGxmlElement(newSrc);
                newSrc = SegmentUtil2.adjustSegmentAttributeValues(
                        srcGxmlElement, trgGxmlElement, "xlf");
                newSrc = "<source>" + GxmlUtil.stripRootTag(newSrc)
                        + "</source>";
                Element sourceElement = foo.element(XliffConstants.SOURCE);
                Element newSourceElement = getDom(newSrc).getRootElement();
                foo.remove(sourceElement);
                foo.add(newSourceElement);

                String newTrg = "<segment>"
                        + GxmlUtil.stripRootTag(targetArray.get(index))
                        + "</segment>";
                trgGxmlElement = SegmentUtil2.getGxmlElement(newTrg);
                newTrg = SegmentUtil2.adjustSegmentAttributeValues(
                        srcGxmlElement, trgGxmlElement, "xlf");
                newTrg = "<target>" + GxmlUtil.stripRootTag(newTrg)
                        + "</target>";
                Element newTargetElement = getDom(newTrg).getRootElement();
                Element targetElement = foo.element(XliffConstants.TARGET);
                foo.remove(targetElement);
                // If target has "state" attribute, it should be preserved.
                try
                {
                    if (!"".equals(trgStates.get(index)))
                    {
                        newTargetElement.add(new DefaultAttribute(
                                XliffConstants.STATE, trgStates.get(index)));
                    }
                }
                catch (Exception ignore)
                {

                }

                foo.add(newTargetElement);
                index++;
            }
        }
    }

    /**
     * If the TU element is from XLF source file, return true;
     * 
     * @param tuElement
     * @param jobIds
     * @return
     */
    private boolean isSrcFileXlf(Element tuElement, HashSet<Long> jobIds)
    {
        TuImpl tu = getTu(tuElement, jobIds);
        if (tu != null)
        {
            if ("xlf".equals(tu.getDataType()))
            {
                return true;
            }
        }

        return false;
    }

    private TuImpl getTu(Element tuElement, HashSet<Long> jobIds)
    {
        TuImpl tu = null;
        try
        {
            String tuId = getTuId(tuElement);
            if (tuId != null)
            {
                if (tuId.indexOf(":") > 0)
                {
                    tuId = tuId.substring(0, tuId.indexOf(":"));
                }

                for (long id : jobIds)
                {
                    tu = SegmentTuUtil.getTuById(Long.parseLong(tuId), id);
                    if (tu != null)
                    {
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (s_category.isDebugEnabled())
            {
                s_category.error("isSrcFileXlf(..) error:", e);
            }
        }

        return tu;
    }

    /**
     * Get "id" attribute from a "<trans-unit id="2328958" ...>".
     * 
     * @param tuElement
     * @return
     */
    private String getTuId(Element tuElement)
    {
        Attribute tuIdAtt = tuElement.attribute("id");
        if (tuIdAtt != null)
        {
            return tuIdAtt.getValue();
        }
        return null;
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

    private String convertNode2Pseudo(Document doc, String tagName,
            String p_fileName, HashSet<Long> jobIds,
            PtagErrorPageWriter m_errWriter)
    {
        Element root = doc.getRootElement();
        Element foo;
        String tuId = null;
        boolean hasError = false;

        Element bodyElement = root.element(XliffConstants.FILE).element(
                XliffConstants.BODY);
        for (Iterator i = bodyElement
                .elementIterator(XliffConstants.TRANS_UNIT); i.hasNext();)
        {
            try
            {
                foo = (org.dom4j.Element) i.next();
                Element sourceElement = foo.element(tagName);
                String textContent = sourceElement.asXML();
                textContent = textContent.replaceFirst(
                        "<" + tagName + "[^>]*>", "");
                textContent = textContent.replace("</" + tagName + ">", "");
                textContent = convertSegment2Pseudo(textContent,
                        isSrcFileXlf(foo, jobIds), getTu(foo, jobIds));
                if (tagName.equalsIgnoreCase("target")
                        && textContent.startsWith("#"))
                {
                    textContent = textContent.replaceFirst("#", OfflineConstants.PONUD_SIGN);
                }
                sourceElement.setText(textContent);
            }
            catch (Exception e)
            {
                hasError = true;
                m_errWriter.addSegmentErrorMsg(tuId, e.getMessage());
            }
        }

        if (hasError)
            return m_errWriter.buildPage().toString();
        else
            return null;
    }

    @SuppressWarnings("static-access")
    private String convertSegment2Pseudo(String textContent,
            boolean isXliffXlf, TuImpl currentTu)
    {
        textContent = XLIFFStandardUtil.convertToTmx(textContent);

        PseudoData PTagData = null;
        TmxPseudo convertor = null;

        // Create PTag resources
        PTagData = new PseudoData();
        PTagData.setMode(2);
        convertor = new TmxPseudo();

        // configure addable ptags for this format
        if (currentTu != null)
        {
            String tuDataType = currentTu.getDataType();
            String tuType = currentTu.getTuType();
            PTagData.setAddables(tuDataType);
            // special treatment for html
            if ("html".equalsIgnoreCase(tuDataType) && !"text".equals(tuType))
            {
                PTagData.setAddables(tuType);
            }
        }
        PTagData.setIsXliffXlfFile(isXliffXlf);

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
    private void addComment(Document doc, User p_user, HashSet<Long> jobIds)
    {
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
            // For GBS-3643: if resname="SID", do not add note value as comment.
            String resName = foo.attributeValue("resname");
            if ("SID".equalsIgnoreCase(resName))
            {
                continue;
            }

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
                if (textContent.startsWith("Match Type:"))
                {
                    continue;
                }
                textContent = entity.decodeString(textContent, null);

                String tuId = foo.attributeValue("id");
                try
                {
                    // As we can not get to know the job ID only by the tuId, we
                    // have to loop jobIds until we can get the TU object.
                    long jobId = -1;
                    TuImpl tu = null;
                    for (long id : jobIds)
                    {
                        tu = SegmentTuUtil.getTuById(Long.parseLong(tuId), id);
                        jobId = id;
                        if (tu != null)
                        {
                            break;
                        }
                    }
                    for (Object ob : tu.getTuvs(true, jobId))
                    {
                        TuvImpl tuv = (TuvImpl) ob;
                        TargetPage tPage = tuv.getTargetPage(jobId);
                        if (tuv.getGlobalSightLocale().toString()
                                .equalsIgnoreCase(target))
                        {
                            String title = String.valueOf(tu.getId());
                            String priority = "Medium";
                            String status = "open";
                            String category = "Type01";

                            IssueImpl issue = tuv.getComment();
                            if (issue == null)
                            {
                                String key = CommentHelper.makeLogicalKey(
                                        tPage.getId(), tu.getId(), tuv.getId(),
                                        0);
                                issue = new IssueImpl(Issue.TYPE_SEGMENT,
                                        tuv.getId(), title, priority, status,
                                        category, p_user.getUserId(),
                                        textContent, key);
                                issue.setShare(false);
                                issue.setOverwrite(false);
                            }
                            else
                            {
                                issue.setTitle(title);
                                issue.setPriority(priority);
                                issue.setStatus(status);
                                issue.setCategory(category);
                                issue.addHistory(p_user.getUserId(),
                                        textContent);
                                issue.setShare(false);
                                issue.setOverwrite(false);
                            }

                            HibernateUtil.saveOrUpdate(issue);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    s_category.error("Failed to add comments", e);
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
                        RE matcher = new RE(RE_OFFLINE_TEXT_FILE_SIGNATURE2);
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

    @Override
    public void cancel()
    {
        cancel = true;
        if (api != null)
        {
            api.cancel();
        }
    }

    /**
     * Get user's default download options ("My Account" >> "Download Options").
     * 
     * @param p_userId
     * @param p_task
     * @return DownloadParams
     * @throws OfflineEditorManagerException
     */
    @Override
    @SuppressWarnings("rawtypes")
    public DownloadParams getDownloadParamsByUser(String p_userId, Task p_task)
            throws OfflineEditorManagerException
    {
        DownloadParams downloadParams = null;
        try
        {
            Vector<String> downloadOfflineFilesOptions = getDownloadOptions(p_userId);
            SendDownloadFileHelper helper = new SendDownloadFileHelper();
            User user = UserUtil.getUserById(p_userId);
            String uiLocale = user.getDefaultUILocale();

            int fileFormat = helper.getFileFormat(downloadOfflineFilesOptions
                    .get(0));
            int editorId = helper.getEditorId(downloadOfflineFilesOptions
                    .get(1));
            String encoding = downloadOfflineFilesOptions.get(2);
            int ptagFormat = helper.getPtagFormat(downloadOfflineFilesOptions
                    .get(3));
            // In current implementation, editor is always "WinWord97".
            int platformId = helper.getPlatformId(null, editorId);
            int resInsMode = helper
                    .getResourceInsertionMode(downloadOfflineFilesOptions
                            .get(4));
            String displayExactMatch = downloadOfflineFilesOptions.get(6);
            String consolidateTM = downloadOfflineFilesOptions.get(7);
            String consolidateTerm = downloadOfflineFilesOptions.get(8);
            String terminology = downloadOfflineFilesOptions.get(9);
            // populate 100
            String populate100 = downloadOfflineFilesOptions.get(10);
            // populate fuzzy target segments (only for Bilingual RTF)
            String populateFuzzy = downloadOfflineFilesOptions.get(11);
            // need consolidate output file (for XLF format)
            String consolidateXLF = downloadOfflineFilesOptions.get(12);
            String changeCreationIdForMt = downloadOfflineFilesOptions.get(13);
            String includeRepetitions = downloadOfflineFilesOptions.get(14);
            String excludeFullyLeveragedFiles = downloadOfflineFilesOptions
                    .get(16);
            String preserveSourceFolder = downloadOfflineFilesOptions.get(17);
            String includeXmlNodeContextInformation = downloadOfflineFilesOptions
                    .get(18);
            String consolidateFileType = downloadOfflineFilesOptions.get(19);
            String wordCountForDownload = downloadOfflineFilesOptions.get(20);
            String penalizedReferenceTmPre = downloadOfflineFilesOptions
                    .get(21);
            String penalizedReferenceTmPer = downloadOfflineFilesOptions
                    .get(22);

            List<Long> pageIdList = new ArrayList<Long>();
            List<String> pageNameList = new ArrayList<String>();
            List<Boolean> canUseUrlList = new ArrayList<Boolean>();

            helper.getAllPageIdList(p_task, pageIdList, pageNameList);
            if (pageIdList != null && pageIdList.size() <= 0)
            {
                pageIdList = null;
                pageNameList = null;
            }

            if (pageIdList != null)
            {
                for (int i = 0; i < pageIdList.size(); i++)
                {
                    canUseUrlList.add(Boolean.FALSE);
                }
            }

            long workflowId = p_task.getWorkflow().getId();
            L10nProfile l10nProfile = p_task.getWorkflow().getJob()
                    .getL10nProfile();
            int downloadEditAll = 4;
            if (l10nProfile.getTmChoice() == LocProfileStateConstants.ALLOW_EDIT_TM_USAGE)
            {
                downloadEditAll = helper.getEditAllState(
                        downloadOfflineFilesOptions.get(15), l10nProfile);
            }
            Vector excludeTypes = l10nProfile.getTranslationMemoryProfile()
                    .getJobExcludeTuTypes();
            List primarySourceFiles = helper.getAllPSFList(p_task);
            List stfList = helper.getAllSTFList(p_task);
            List supportFileList = helper.getAllSupportFileList(p_task);

            downloadParams = new DownloadParams(p_task.getJobName(), null, "",
                    Long.toString(workflowId), Long.toString(p_task.getId()),
                    pageIdList, pageNameList, canUseUrlList,
                    primarySourceFiles, stfList, editorId, platformId,
                    encoding, ptagFormat, uiLocale, p_task.getSourceLocale(),
                    p_task.getTargetLocale(), true, fileFormat, excludeTypes,
                    downloadEditAll, supportFileList, resInsMode, user);
            downloadParams.setConsolidateTmxFiles("yes"
                    .equalsIgnoreCase(consolidateTM));
            downloadParams.setConsolidateTermFiles("yes"
                    .equalsIgnoreCase(consolidateTerm));
            downloadParams.setTermFormat(terminology);
            downloadParams.setJob(ServerProxy.getJobHandler().getJobById(
                    p_task.getJobId()));
            downloadParams.setDisplayExactMatch(displayExactMatch);
            downloadParams.setPopulate100("yes".equalsIgnoreCase(populate100));
            downloadParams.setPopulateFuzzy("yes"
                    .equalsIgnoreCase(populateFuzzy));
            downloadParams.setPreserveSourceFolder("yes"
                    .equalsIgnoreCase(preserveSourceFolder));
            downloadParams.setConsolidateFileType(consolidateFileType);
            downloadParams.setWordCountForDownload(Integer
                    .parseInt(wordCountForDownload));
            downloadParams.setIncludeXmlNodeContextInformation("yes"
                    .equalsIgnoreCase(includeXmlNodeContextInformation));
            downloadParams.setIncludeRepetitions("yes"
                    .equalsIgnoreCase(includeRepetitions));
            downloadParams.setChangeCreationIdForMTSegments("yes"
                    .equalsIgnoreCase(changeCreationIdForMt));
            downloadParams.setExcludeFullyLeveragedFiles("yes"
                    .equalsIgnoreCase(excludeFullyLeveragedFiles));
            downloadParams.setPenalizedReferenceTmPre("yes"
                    .equalsIgnoreCase(penalizedReferenceTmPre));
            downloadParams.setPenalizedReferenceTmPer("yes"
                    .equalsIgnoreCase(penalizedReferenceTmPer));
        }
        catch (Exception e)
        {
            s_category.error("Fail to get user default download options : "
                    + p_userId, e);
            throw new OfflineEditorManagerException(e);
        }

        return downloadParams;
    }

    @SuppressWarnings("unchecked")
    private Vector<String> getDownloadOptions(String p_userId)
            throws OfflineEditorManagerException
    {
        Vector<String> downloadOptions = new Vector<String>();
        try
        {
            HashMap<String, UserParameter> map = ServerProxy
                    .getUserParameterManager().getUserParameterMap(p_userId);
            for (int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                    .size(); i++)
            {
                String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                        .get(i);
                downloadOptions.add(map.get(downloadOption).getValue());
            }
        }
        catch (Exception e)
        {
            s_category
                    .error("Fail to get current user parameters, probably this user has never logged in system. Logging once, user paramters will be initialized default : "
                            + p_userId);
            throw new OfflineEditorManagerException(e);
        }

        return downloadOptions;
    }

    /**
     * Get offline translation kit file in ZIP according to download params.
     * 
     * @param p_userId
     * @param p_taskId
     * @param p_downloadParams
     * @return File
     * @throws OfflineEditorManagerException
     */
    public File getDownloadOfflineFiles(String p_userId, Long p_taskId,
            DownloadParams p_downloadParams)
            throws OfflineEditorManagerException
    {
        File tmpFile = null;
        try
        {
            Task task = ServerProxy.getTaskManager().getTask(p_taskId);

            File tmpDir = AmbFileStoragePathUtils.getCustomerDownloadDir(String
                    .valueOf(task.getCompanyId()));
            String fileName = p_downloadParams.getTruncatedJobName() + "_"
                    + task.getTargetLocale() + ".zip";
            tmpFile = new File(tmpDir, fileName);

            JobPackageZipper zipper = new JobPackageZipper();
            zipper.createZipFile(tmpFile);

            p_downloadParams.setZipper(zipper);
            p_downloadParams.verify();

            OEMProcessStatus status = new OEMProcessStatus(p_downloadParams);
            attachListener(status);
            runProcessDownloadRequest(p_downloadParams);

            zipper.closeZipFile();
        }
        catch (Exception e)
        {
            s_category.error(e);
            throw new OfflineEditorManagerException(e);
        }

        return tmpFile;
    }

    private List<String> getExcludedItemTypes(Task task)
    {
        if (task != null)
        {
            L10nProfile l10nProfile = task.getWorkflow().getJob()
                    .getL10nProfile();
            List<String> p_excludedItemTypes = l10nProfile
                    .getTranslationMemoryProfile().getJobExcludeTuTypes();
            return p_excludedItemTypes;
        }
        return null;
    }

    private HashSet<Long> getJobIdsFromDoc(org.dom4j.Document doc)
    {
        HashSet<Long> jobIds = new HashSet<Long>();
        String taskIdsStr = XliffFileUtil.getTaskId(doc);
        if (taskIdsStr != null)
        {
            List<Long> taskIds = new ArrayList<Long>();
            if (taskIdsStr.indexOf(",") == -1)
            {
                taskIds.add(Long.parseLong(taskIdsStr.trim()));
            }
            else
            {
                String[] ids = Strings.split(taskIdsStr, ",");
                for (String id : ids)
                {
                    taskIds.add(Long.parseLong(id.trim()));
                }
            }
            for (long tskId : taskIds)
            {
                jobIds.add(TaskHelper.getTask(tskId).getJobId());
            }
        }
        return jobIds;
    }

    public OEMProcessStatus getStatus()
    {
        return m_status;
    }
}

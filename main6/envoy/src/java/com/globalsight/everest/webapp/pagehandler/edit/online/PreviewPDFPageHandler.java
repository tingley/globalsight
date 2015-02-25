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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.globalsight.cxe.adapter.adobe.AdobeConfiguration;
import com.globalsight.cxe.adapter.adobe.AdobeHelper;
import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.adapter.quarkframe.FrameHelper;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.docproc.merger.fm.FontMappingHelper;
import com.globalsight.ling.docproc.merger.fm.IDMLFontMappingHelper;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.file.FileWaiter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PreviewPDFPageHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(PreviewPDFPageHandler.class);

    private static int BUFFERSIZE = 4096;

    private static final String ADOBE_INDD = "indd";
    private static final String ADOBE_INX = "inx";
    private static final String ADOBE_FM = "FM";
    private static final String ADOBE_IDML = "idml";

    private static final String PDF_SUFFIX = ".pdf";
    private static final String XML_SUFFIX = ".xml";
    private static final String XMP_SUFFIX = ".xmp";
    private static final String INDD_SUFFIX = ".indd";
    private static final String INX_SUFFIX = ".inx";
    private static final String FM_SUFFIX = ".fm";
    private static final String MIF_SUFFIX = ".mif";
    private static final String IDML_SUFFIX = ".idml";
    private static final String STATUS_SUFFIX = ".status";

    private static final String COMMAND_STATUS_SUFFIX = ".pv_status";
    private static final String PV_COMMAND_SUFFIX = ".pv_command";
    private static final String FM_COMMAND_SUFFIX = ".fm_command";

    private static final String LOCALE_PRE_CONVERTED = "iw_IL";
    private static final String LOCALE_POST_CONVERTED = "he_IL";

    private static final int ADOBE_CS2 = 0;
    private static final int ADOBE_CS3 = 1;
    private static final int ADOBE_CS4 = 2;
    private static final int ADOBE_CS5 = 3;
    private static final int ADOBE_FM9 = 4;
    private static final int ADOBE_CS5_5 = 5;
    private static final int ADOBE_TYPE_IDML = 6;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private int m_versionType = ADOBE_CS2;
    private String m_conversionType = ADOBE_INDD;
    private String m_inDesignFileSuffix = INDD_SUFFIX;
    private boolean m_masterTranslated = true;
    private boolean m_translateHiddenLayer = false;

    static private final String[] PROPERTY_FILES =
    { "/properties/Logger.properties", "/properties/AdobeAdapter.properties" };

    static private final String[] PROPERTY_FILES_FM =
    { "/properties/Logger.properties", "/properties/frameAdapter.properties" };

    private String sourceLocale;
    private String targetLocale;
    private SessionManager sessionMgr = null;
    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private String m_company_id = "";

    private String m_relSafeName = null;;
    private String m_safeBaseFileName = null;

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(true);
        sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);

        determineConversionParameters();
        long jobId;
        Object jobIdO = sessionMgr.getAttribute(WebAppConstants.JOB_ID);
        if (jobIdO instanceof Long)
        {
            jobId = (Long) jobIdO;
        }
        else
        {
            jobId = Long.valueOf((String) jobIdO);
        }

        Object userobj = sessionMgr.getAttribute(WebAppConstants.USER);
        UserImpl user = null;
        if (userobj != null)
        {
            user = (UserImpl) userobj;
        }
        String userid = user == null ? null : user.getUserId();

        m_job = getJobById(jobId);
        if (m_job != null)
        {
            m_company_id = String.valueOf(m_job.getCompanyId());
        }
        else
        {
            m_company_id = CompanyThreadLocal.getInstance().getValue();
            CATEGORY.error("If can not view the pdf file besause the company id is incorrect.");
        }
        String action = p_request.getParameter("action") == null ? ""
                : (String) p_request.getParameter("action");
        File pdfFile = getPreviewPdf(p_request, userid, action);
        File oldPdfFile = getOldPreviewPdf(p_request);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
        long srcPageId = state.getSourcePageId().longValue();
        long targetPageId = state.getTargetPageId().longValue();

        if (action == null)
        {
            CATEGORY.error("action is null.");
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
            return;
        }

        String converterDir = null;
        if (m_versionType == ADOBE_FM9)
        {
            if (!pdfFile.exists())
            {
                File mifFile = null;
                File fmFile = null;
                String currentLocale = null;
                String pageName = p_request.getParameter("file");
                if (action.equals("previewSrc"))
                {
                    currentLocale = m_job.getSourceLocale().toString();
                    fmFile = getSourceFile(srcPageId);
                }
                else if (action.equals("previewTar"))
                {
                    mifFile = getTargetFile(targetPageId, userid, state);
                    int index = Math.max(pageName.indexOf("/"),
                            pageName.indexOf("\\"));
                    targetLocale = pageName.substring(0, index);
                    if (LOCALE_PRE_CONVERTED.equals(targetLocale))
                    {
                        pageName = pageName.replaceFirst(targetLocale,
                                LOCALE_POST_CONVERTED);
                        targetLocale = LOCALE_POST_CONVERTED;
                    }

                    currentLocale = targetLocale;
                }

                try
                {
                    converterDir = getConvertDir() + currentLocale;
                    new File(converterDir).mkdirs();

                    int lastSeparatorIndex = Math.max(
                            pageName.lastIndexOf("/"),
                            pageName.lastIndexOf("\\"));
                    String filename = pageName
                            .substring(lastSeparatorIndex + 1);
                    String filenamePre = FileUtils.getPrefix(filename);
                    String currentTime = String.valueOf(System
                            .currentTimeMillis());
                    String fileToConvert = converterDir + File.separator
                            + currentTime + filenamePre + MIF_SUFFIX;
                    String fileExpected = converterDir + File.separator
                            + currentTime + filenamePre + PDF_SUFFIX;
                    String fileStatus = converterDir + File.separator
                            + currentTime + filenamePre + STATUS_SUFFIX;
                    String fileCommand = converterDir + File.separator
                            + currentTime + filenamePre + FM_COMMAND_SUFFIX;

                    // source preview, convert fm to mif first
                    // target preview, copy exported mif file directly
                    if (action.equals("previewSrc"))
                    {
                        String fmFileToConvert = converterDir + File.separator
                                + currentTime + filenamePre + FM_SUFFIX;
                        FileUtils.copyFile(fmFile, new File(fmFileToConvert));
                        // write command
                        StringBuffer text = new StringBuffer();
                        text.append("ConvertFrom=fm").append("\r\n");
                        text.append("ConvertTo=mif").append("\r\n");
                        FileUtil.writeFileAtomically(new File(fileCommand),
                                text.toString(), "US-ASCII");
                        // wait for status
                        FileWaiter fileWaiter = new FileWaiter(
                                AdobeConfiguration.SLEEP_TIME,
                                getMaxWaitTime(), fileStatus);
                        fileWaiter.waitForFile();
                        // parse status file
                        String[] status = statusInfo(new File(fileStatus));
                        FileUtils.deleteSilently(fileStatus);
                        if (!"0".equals(status[0]))
                        {
                            CATEGORY.error("FrameMaker convertion failed: "
                                    + "Cannot convert to PDF file correctly. "
                                    + ((status.length == 2) ? status[1]
                                            : status[0]));

                            throw new EnvoyServletException(
                                    EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                                    "Cannot convert to PDF file correctly.");
                        }
                    }
                    else
                    {
                        FileUtils.copyFile(mifFile, new File(fileToConvert));
                    }

                    // convert mif to PDF
                    // write command
                    StringBuffer text = new StringBuffer();
                    text.append("ConvertFrom=mif").append("\r\n");
                    text.append("ConvertTo=pdf").append("\r\n");
                    FileUtil.writeFileAtomically(new File(fileCommand),
                            text.toString(), "US-ASCII");
                    // wait for status
                    FileWaiter fileWaiter = new FileWaiter(
                            AdobeConfiguration.SLEEP_TIME, getMaxWaitTime(),
                            fileStatus);
                    fileWaiter.waitForFile();
                    // parse status file
                    String[] status = statusInfo(new File(fileStatus));
                    FileUtils.deleteSilently(fileStatus);
                    if (!"0".equals(status[0]))
                    {
                        CATEGORY.error("FrameMaker convertion failed: "
                                + "Cannot convert to PDF file correctly. "
                                + ((status.length == 2) ? status[1] : status[0]));

                        throw new EnvoyServletException(
                                EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                                "Cannot convert to PDF file correctly.");
                    }
                    else
                    {
                        FileUtils.copyFile(new File(fileExpected), pdfFile);
                    }
                }
                catch (Exception e)
                {
                    CATEGORY.error(e.getMessage(), e);
                    super.invokePageHandler(p_pageDescriptor, p_request,
                            p_response, p_context);
                }
            }
        }
        else if (m_versionType == ADOBE_TYPE_IDML)
        {
            try
            {
                IdmlConverter converter = new IdmlConverter();

                if (!pdfFile.exists())
                {
                    File idmlFile = null;
                    String currentLocale = null;
                    String pageName = p_request.getParameter("file");
                    if (action.equals("previewSrc"))
                    {
                        currentLocale = m_job.getSourceLocale().toString();
                        idmlFile = getSourceFile(srcPageId);
                        converter
                                .convertToPdf(idmlFile, pdfFile, currentLocale);
                    }
                    else if (action.equals("previewTar"))
                    {
                        String targetPageName = p_request.getParameter("file");
                        int index = Math.max(targetPageName.indexOf("/"),
                                targetPageName.indexOf("\\"));
                        targetLocale = targetPageName.substring(0, index);
                        if (LOCALE_PRE_CONVERTED.equals(targetLocale))
                        {
                            targetPageName = targetPageName.replaceFirst(
                                    targetLocale, LOCALE_POST_CONVERTED);
                            targetLocale = LOCALE_POST_CONVERTED;
                        }
                        converterDir = getConvertDir() + "/" + targetLocale;
                        new File(converterDir).mkdirs();

                        String xmlFilePath = converterDir + File.separator
                                + m_relSafeName;
                        File zipDir = getZipDir(new File(xmlFilePath),
                                m_safeBaseFileName);
                        // write xml file
                        writeXMLFileToConvertDir(p_request, xmlFilePath,
                                targetPageName);
                        IdmlHelper.split(zipDir.getAbsolutePath());
                        IDMLFontMappingHelper idmlFontMappinghelper = new IDMLFontMappingHelper();
                        idmlFontMappinghelper.processIDMLFont(
                                zipDir.getAbsolutePath(), targetLocale);
                        String idmlPath = converter.convertXmlToIdml(
                                m_safeBaseFileName, zipDir.getAbsolutePath());
                        IDMLFontMappingHelper.restoreIDMLFont(zipDir
                                .getAbsolutePath());
                        converter.convertToPdf(new File(idmlPath), pdfFile,
                                targetLocale);
                    }
                }

            }
            catch (Exception e)
            {
                ResourceBundle rb = PageHandler.getBundle(p_request
                        .getSession());

                CATEGORY.error(e.getMessage(), e);
                String msg = e.getMessage();
                String error = "Read PDF Data Error!";
                if ("idml converter is not started".equals(msg))
                {
                    error = rb.getString("lb_filter_msg_convert_start_idml");
                }

                StringBuffer sb = new StringBuffer();
                sb.append("<%@ page contentType=\"text/html; charset=UTF-8\" errorPage=\"error.jsp\" session=\"true\"%>");
                sb.append("<HTML>");
                sb.append("<HEAD>");
                sb.append("<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"/globalsight/includes/setStyleSheet.js\"></SCRIPT>");
                sb.append("</HEAD>");
                sb.append("<BODY>");
                sb.append("<DIVSTYLE=\" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;\">");
                sb.append("<DIV STYLE=\" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;\">");
                sb.append("<SPAN CLASS=\"headingError\">" + error + "</SPAN>");
                sb.append("</DIV>");
                sb.append("</BODY>");
                sb.append("</HTML>");
                p_response.getWriter().write(sb.toString());
                return;
                // super.invokePageHandler(p_pageDescriptor, p_request,
                // p_response, p_context);
            }

        }
        else
        {
            Map newFileNameMap = new HashMap();
            try
            {
                if (action.equals("previewSrc"))
                {
                    if (!pdfFile.exists())
                    {
                        CATEGORY.info("The source PDF file is missing.");
                        throw new EnvoyServletException(
                                EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                                "PDF file does not exist.");
                    }
                }
                else if (action.equals("previewTar"))
                {
                    if (!pdfFile.exists())
                    {
                        String targetPageName = p_request.getParameter("file");
                        int index = Math.max(targetPageName.indexOf("/"),
                                targetPageName.indexOf("\\"));
                        targetLocale = targetPageName.substring(0, index);
                        if (LOCALE_PRE_CONVERTED.equals(targetLocale))
                        {
                            targetPageName = targetPageName.replaceFirst(
                                    targetLocale, LOCALE_POST_CONVERTED);
                            targetLocale = LOCALE_POST_CONVERTED;
                        }
                        converterDir = getConvertDir() + targetLocale;
                        new File(converterDir).mkdirs();

                        String targetPageFolder = pdfFile.getParent();
                        String statusPageFolder = oldPdfFile.getParent();
                        File f = new File(targetPageFolder);
                        if (!f.exists())
                        {
                            f.mkdirs();
                            copyFilesToNewTargetLocale(p_request,
                                    targetPageName, targetPageFolder,
                                    statusPageFolder);
                        }
                        String oriXmlFileName = getConvertedFileName(
                                targetLocale, targetPageName);

                        backupInddFile(newFileNameMap, getConvertDir(),
                                targetLocale, oriXmlFileName);
                        // back up the indd and xmp file
                        String xmlFilePath = converterDir + File.separator
                                + (String) newFileNameMap.get(XML_SUFFIX);

                        // write xml file
                        writeXMLFileToConvertDir(p_request, xmlFilePath,
                                targetPageName);
                        // write command file
                        writeCommandFile(xmlFilePath);
                        // 5 wait for Adobe Converter to convert
                        pdfFile = readTargetPdfFile(xmlFilePath,
                                targetPageName, userid);

                    }
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
                super.invokePageHandler(p_pageDescriptor, p_request,
                        p_response, p_context);
            }
            finally
            {
                // 6 delete back up indd and xmp file
                try
                {
                    if (!newFileNameMap.isEmpty())
                    {
                        FileUtils.deleteSilently(converterDir
                                + File.separator
                                + (String) newFileNameMap
                                        .get(m_inDesignFileSuffix));
                        FileUtils.deleteSilently(converterDir + File.separator
                                + (String) newFileNameMap.get(XMP_SUFFIX));
                    }
                }
                catch (Exception eex)
                {
                }
            }
        }

        if (pdfFile.exists())
        {
            try
            {
                File viewFile = setCopyOnlyPermission(pdfFile);

                p_response.setContentType("application/pdf");
                if (p_request.isSecure())
                {
                    setHeaderForHTTPSDownload(p_response);
                }
                else
                {
                    p_response.setHeader("Cache-Control", "no-cache");
                }

                // filename, maybe we need to handle some specail character,
                // like &
                String filename = pdfFile.getName();
                p_response.setHeader("Content-Disposition",
                        "inline; filename=\"" + filename + "\"");
                writeOutFile(viewFile, p_response, action);
                FileUtils.deleteSilently(viewFile.getAbsolutePath());
            }
            catch (DocumentException e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else
        {
            CATEGORY.error("Can not generate PDF file for review: "
                    + pdfFile.getPath());
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    private File getZipDir(File file, String safeBaseFileName)
    {
        // avoid dead circle
        if (file == null || file.getPath().length() < 20)
        {
            return file;
        }

        if (file.getName().startsWith(safeBaseFileName))
        {
            return file;
        }
        else
        {
            File parent = file.getParentFile();
            // check if can not get parent file
            if (parent.getPath().equals(file.getPath()))
            {
                return file;
            }
            else
            {
                return getZipDir(parent, safeBaseFileName);
            }
        }
    }

    public static File setCopyOnlyPermission(File p_file)
            throws DocumentException, IOException
    {
        PdfReader reader = new PdfReader(p_file.getAbsolutePath());
        String outPutFile = FileUtils.getPrefix(p_file.getAbsolutePath())
                + "_hidebars.pdf";
        int n = reader.getNumberOfPages();
        Rectangle psize = reader.getPageSize(1);
        float width = psize.height();
        float height = psize.width();
        Document document = new Document(new Rectangle(width, height));
        PdfCopy writer = new PdfCopy(document, new FileOutputStream(outPutFile));
        writer.setEncryption(PdfWriter.STRENGTH40BITS, null, null,
                PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING);
        try
        {
            document.open();
            int i = 0;
            while (i < n)
            {
                document.newPage();
                i++;
                PdfImportedPage page1 = writer.getImportedPage(reader, i);
                writer.addPage(page1);
            }
            document.close();
        }
        catch (IllegalArgumentException e)
        {
            if (!e.getMessage().equals(
                    "PdfReader not opened with owner password"))
            {
                throw new EnvoyServletException(e);
            }
            else
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }

        return new File(outPutFile);
    }

    private File getSourceFile(long srcPageId)
    {
        File srcFile = null;
        try
        {
            SourcePage srcPage = ServerProxy.getPageManager().getSourcePage(
                    srcPageId);
            srcFile = srcPage.getFile();

            // for super translater
            if (srcFile == null)
            {
                srcFile = srcPage.getFileByPageCompanyId();
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("get source file error: " + e.getMessage());
        }

        return srcFile;

    }

    private File getTargetIdmlFile(long targetPageId)
    {
        File targetFile = null;

        try
        {
            ExportHelper helper = new ExportHelper();
            targetFile = helper.getTargetXmlPage(targetPageId,
                    CxeMessageType.IDML_LOCALIZED_EVENT, true);
        }
        catch (Exception e)
        {
            CATEGORY.error("get target file error: " + e.getMessage());
        }

        return targetFile;
    }

    private File getTargetFile(long targetPageId, String uid, EditorState state)
    {
        File targetFile = null;

        try
        {
            ExportHelper helper = new ExportHelper();
            helper.setUserId(uid);
            helper.setEditorState(state);
            targetFile = helper.getTargetXmlPage(targetPageId,
                    CxeMessageType.MIF_LOCALIZED_EVENT, true);
        }
        catch (Exception e)
        {
            CATEGORY.error("get target file error: " + e.getMessage());
        }

        return targetFile;
    }

    /**
     * Copies files to the folder of newly added target locale.
     * 
     * @param p_request
     * @param p_targetPageFolder
     * @param p_targetPageName
     * @throws Exception
     */
    private void copyFilesToNewTargetLocale(HttpServletRequest p_request,
            String p_targetPageName, String p_targetPageFolder,
            String p_statusFolder) throws Exception
    {
        String existedLocale = null;
        File previewDir = AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id);
        L10nProfile lp = m_job.getL10nProfile();
        GlobalSightLocale[] targetLocales = lp.getTargetLocales();
        String prefix = FileUtils.getPrefix(p_targetPageName);
        String tarName = null;
        for (GlobalSightLocale globalSightLocale : targetLocales)
        {
            existedLocale = globalSightLocale.toString();
            if (LOCALE_PRE_CONVERTED.equals(existedLocale))
                existedLocale = LOCALE_POST_CONVERTED;

            String existedFolder = prefix.replaceFirst(targetLocale,
                    existedLocale);
            tarName = previewDir + File.separator + existedFolder
                    + STATUS_SUFFIX;

            if (new File(tarName).exists())
            {
                break;
            }

            tarName = tarName.replace("\\", "/");
            if (new File(tarName).exists())
            {
                break;
            }

            tarName = null;
        }

        // do not copy if the target locale is same as existed locale
        if (existedLocale.equalsIgnoreCase(targetLocale))
        {
            return;
        }

        String existedTpFolder = p_statusFolder.replaceFirst(targetLocale,
                existedLocale);
        File file = new File(existedTpFolder);
        File[] files = file.listFiles();
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                File f = files[i];
                if (STATUS_SUFFIX.equalsIgnoreCase(f.getAbsolutePath()
                        .substring(f.getAbsolutePath().lastIndexOf("."))))
                    FileCopier.copy(f, p_targetPageFolder);
            }
        }

        GlobalSightLocale srcGSLocale = lp.getSourceLocale();
        String srcLocale = srcGSLocale.toString();
        if (LOCALE_PRE_CONVERTED.equals(srcLocale))
            srcLocale = LOCALE_POST_CONVERTED;
        copyAdobeFiles(srcLocale,
                getConvertedFileName(targetLocale, p_targetPageName));
    }

    private void copyAdobeFiles(String p_existedLocale,
            String p_convertedFileName) throws Exception
    {
        String conDir = getConvertDir();
        String fileName = FileUtils.getPrefix(p_convertedFileName);

        StringBuffer inddFile = new StringBuffer(conDir);
        inddFile.append(p_existedLocale).append(File.separator)
                .append(fileName).append(m_inDesignFileSuffix);
        StringBuffer xmpFile = new StringBuffer(conDir);
        xmpFile.append(p_existedLocale).append(File.separator).append(fileName)
                .append(XMP_SUFFIX);
        StringBuffer pdfFile = new StringBuffer(conDir);
        pdfFile.append(p_existedLocale).append(File.separator).append(fileName)
                .append(PDF_SUFFIX);

        FileCopier.copy(new File(inddFile.toString()), conDir + targetLocale);
        FileCopier.copy(new File(xmpFile.toString()), conDir + targetLocale);
        FileCopier.copy(new File(pdfFile.toString()), conDir + targetLocale);
    }

    /**
     * Returns the converted file name
     * 
     * @param p_targetPageName
     * @return
     */
    private String getConvertedFileName(String targetLocale,
            String p_targetPageName)
    {
        File previewDir = AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id);
        String prefix = FileUtils.getPrefix(p_targetPageName);
        String tarName = null;

        L10nProfile lp = m_job.getL10nProfile();
        GlobalSightLocale[] targetLocales = lp.getTargetLocales();
        for (GlobalSightLocale globalSightLocale : targetLocales)
        {
            String existedLocale = globalSightLocale.toString();
            if (LOCALE_PRE_CONVERTED.equals(existedLocale))
                existedLocale = LOCALE_POST_CONVERTED;

            String existedFolder = prefix.replaceFirst(targetLocale,
                    existedLocale);
            tarName = previewDir + File.separator + existedFolder
                    + STATUS_SUFFIX;

            if (new File(tarName).exists())
            {
                break;
            }

            tarName = tarName.replace("\\", "/");
            if (new File(tarName).exists())
            {
                break;
            }

            tarName = null;
        }

        if (tarName == null)
        {
            tarName = previewDir + File.separator + prefix + STATUS_SUFFIX;

            CATEGORY.error(tarName + " does not exist");
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF, tarName
                            + " does not exist");
        }

        BufferedReader br;
        String convertedFileName = "";
        try
        {
            br = new BufferedReader(new FileReader(tarName));
            convertedFileName = br.readLine();
        }
        catch (FileNotFoundException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (IOException ex)
        {
            throw new EnvoyServletException(ex);
        }

        return FileUtils.getPrefix(convertedFileName
                .substring(convertedFileName.indexOf("=") + 1)) + XML_SUFFIX;
    }

    /**
     * Backs up adobe indesign and xmp file
     * 
     * @param p_conDir
     * @param p_xmlFileName
     */
    private void backupInddFile(Map fileMap, String p_conDir,
            String targetLocale, String p_xmlFileName)
    {
        String fileNameWithoutSuffix = FileUtils.getPrefix(p_xmlFileName);
        String currentTime = String.valueOf(System.currentTimeMillis());
        String targetInddName = currentTime + "_" + fileNameWithoutSuffix
                + m_inDesignFileSuffix;
        String targetXmpName = currentTime + "_" + fileNameWithoutSuffix
                + XMP_SUFFIX;
        String targetXmlName = currentTime + "_" + p_xmlFileName;

        L10nProfile lp = m_job.getL10nProfile();
        File oriInddFile = null;
        File oriXmpFile = null;

        // get from target locale first
        String oriInddFileName = p_conDir + targetLocale + File.separator
                + fileNameWithoutSuffix + m_inDesignFileSuffix;
        String oriXmpFileName = p_conDir + targetLocale + File.separator
                + fileNameWithoutSuffix + XMP_SUFFIX;
        oriInddFile = new File(oriInddFileName);
        oriXmpFile = new File(oriXmpFileName);
        if (!oriInddFile.exists() || !oriXmpFile.exists())
        {
            oriInddFile = null;
            oriXmpFile = null;
        }

        // get from source locale then
        if (oriInddFile == null || oriXmpFile == null)
        {
            String srcLocale = lp.getSourceLocale().toString();
            oriInddFileName = p_conDir + srcLocale + File.separator
                    + fileNameWithoutSuffix + m_inDesignFileSuffix;
            oriXmpFileName = p_conDir + srcLocale + File.separator
                    + fileNameWithoutSuffix + XMP_SUFFIX;
            oriInddFile = new File(oriInddFileName);
            oriXmpFile = new File(oriXmpFileName);

            if (!oriInddFile.exists() || !oriXmpFile.exists())
            {
                oriInddFile = null;
                oriXmpFile = null;
            }
        }

        // get from other target locales
        if (oriInddFile == null || oriXmpFile == null)
        {
            GlobalSightLocale[] targetLocales = lp.getTargetLocales();
            for (GlobalSightLocale globalSightLocale : targetLocales)
            {
                String existedLocale = globalSightLocale.toString();
                if (LOCALE_PRE_CONVERTED.equals(existedLocale))
                    existedLocale = LOCALE_POST_CONVERTED;

                oriInddFileName = p_conDir + existedLocale + File.separator
                        + fileNameWithoutSuffix + m_inDesignFileSuffix;
                oriXmpFileName = p_conDir + existedLocale + File.separator
                        + fileNameWithoutSuffix + XMP_SUFFIX;

                oriInddFile = new File(oriInddFileName);
                oriXmpFile = new File(oriXmpFileName);

                if (oriInddFile.exists() && oriXmpFile.exists())
                {
                    break;
                }
                else
                {
                    oriInddFile = null;
                    oriXmpFile = null;
                }
            }
        }

        if (oriInddFile == null || oriXmpFile == null)
        {
            oriInddFileName = p_conDir + targetLocale + File.separator
                    + fileNameWithoutSuffix + m_inDesignFileSuffix;
            CATEGORY.error("Cannot find original indd and xmp file: "
                    + oriInddFileName);
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                    "Cannot find original indd and xmp file: "
                            + oriInddFileName);
        }

        // back up indd file
        FileCopier.copyFile(oriInddFile, new File(p_conDir + targetLocale),
                targetInddName);
        // back up xml file
        FileCopier.copyFile(oriXmpFile, new File(p_conDir + targetLocale),
                targetXmpName);

        fileMap.put(m_inDesignFileSuffix, targetInddName);
        fileMap.put(XMP_SUFFIX, targetXmpName);
        fileMap.put(XML_SUFFIX, targetXmlName);
    }

    /**
     * Reads the pdf file converted from adobe indd to GlobalSight file storage,
     * and be ready to output
     * 
     * @param p_xmlFileName
     * @param p_targetPageName
     * @return
     */
    private File readTargetPdfFile(String p_xmlFileName,
            String p_targetPageName, String userid)
    {
        String statusFileName = FileUtils.getPrefix(p_xmlFileName)
                + COMMAND_STATUS_SUFFIX;
        File statusFile = new File(statusFileName);
        String[] status = null;
        FileWaiter fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME,
                getMaxWaitTime(), statusFileName);
        StringBuffer tarDir = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getAbsolutePath());
        String pdfFileName = FileUtils.getPrefix(p_xmlFileName) + PDF_SUFFIX;

        File post_pdfFile;
        try
        {
            fileWaiter.waitForFile();
            status = statusInfo(statusFile);
            if (!"0".equals(status[0]))
            {
                FileUtils.deleteSilently(statusFileName);
                FileUtils.deleteSilently(p_xmlFileName);
                CATEGORY.error("Adobe convertion failed: "
                        + "Cannot convert to PDF file correctly.");
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            else
            {
                tarDir.append(File.separator).append(userid);
                tarDir.append(File.separator).append(targetLocale);
                String splitChar = p_targetPageName.lastIndexOf("/") > 0 ? "/"
                        : "\\\\";
                String[] tarDisplayName = p_targetPageName.split(splitChar);

                for (int j = 1; j < tarDisplayName.length - 1; j++)
                {
                    tarDir.append(File.separator);
                    tarDir.append(tarDisplayName[j]);
                }
                File tarDirFile = new File(tarDir.toString());
                tarDirFile.mkdirs();
                FileCopier.copy(pdfFileName, tarDir.toString());
                // rename the copied pdf file to the one without the timestamp
                // prefixed.
                StringBuffer temp1 = new StringBuffer(tarDir.toString());
                File pre_pdfFile = new File(temp1.append(
                        pdfFileName.substring(pdfFileName
                                .lastIndexOf(File.separator))).toString());
                StringBuffer temp2 = new StringBuffer(tarDir.toString());
                post_pdfFile = new File(temp2
                        .append(p_targetPageName.substring(
                                p_targetPageName.lastIndexOf(splitChar),
                                p_targetPageName.lastIndexOf(".")))
                        .append(PDF_SUFFIX).toString());
                pre_pdfFile.renameTo(post_pdfFile);
            }
        }
        catch (IOException e)
        {
            throw new EnvoyServletException(e);
        }
        finally
        {
            FileUtils.deleteSilently(statusFileName);
            FileUtils.deleteSilently(p_xmlFileName);
        }

        return post_pdfFile;
    }

    /**
     * Reads the status file after adobe indd converted to PDF
     * 
     * @param p_file
     * @return
     */
    private static String[] statusInfo(File p_file)
    {
        BufferedReader br = null;
        String errorLine = null;
        try
        {
            br = new BufferedReader(new FileReader(p_file));
            errorLine = br.readLine();
            errorLine = errorLine.substring(6); // error=
            String msg = "msg";
            try
            {
                msg = br.readLine();
            }
            catch (Exception eex)
            {
                msg = "";
            }

            return new String[]
            { errorLine, msg };
        }
        catch (Exception e)
        {
            if (CATEGORY.isEnabledFor(Priority.WARN))
                CATEGORY.warn("Cannot read status info", e);
            return new String[]
            { e.getMessage() };
        }
        finally
        {
            FileUtils.closeSilently(br);
        }
    }

    private void determineConversionParameters()
    {
        String sourcePageId = (String) sessionMgr
                .getAttribute(WebAppConstants.SOURCE_PAGE_ID);
        long pageId = Long.parseLong(sourcePageId);
        SourcePage sourcePage = null;
        try
        {
            sourcePage = ServerProxy.getPageManager().getSourcePage(pageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        
        EventFlowXml eventFlow = XmlUtil.string2Object(EventFlowXml.class, sourcePage.getRequest()
                .getEventFlowXml());

        String formatType = eventFlow.getValue("formatType").trim();
        String displayNameLower = eventFlow.getDisplayName().toLowerCase();
        m_relSafeName = eventFlow.getValue("relSafeName");
        m_safeBaseFileName = eventFlow.getValue("safeBaseFileName");

        if ("mif".equals(formatType))
        {
            m_versionType = ADOBE_FM9;
        }
        else if ("indd_cs4".equals(formatType))
        {
            m_versionType = ADOBE_CS4;
        }
        else if ("indd_cs5".equals(formatType))
        {
            m_versionType = ADOBE_CS5;
        }
        else if ("indd_cs5.5".equals(formatType))
        {
            m_versionType = ADOBE_CS5_5;
        }
        else
        {
            m_versionType = (formatType.equals("indd") || formatType
                    .equals("inx")) ? ADOBE_CS2 : ADOBE_CS3;
        }

        if (m_versionType == ADOBE_FM9)
        {
            m_conversionType = ADOBE_FM;
            m_inDesignFileSuffix = FM_SUFFIX;
            return;
        }
        else if (displayNameLower.endsWith("idml"))
        {
            m_conversionType = ADOBE_IDML;
            m_inDesignFileSuffix = IDML_SUFFIX;
            m_versionType = ADOBE_TYPE_IDML;
            return;
        }
        else
        {
            m_conversionType = displayNameLower.endsWith("indd") ? ADOBE_INDD
                    : ADOBE_INX;
            m_inDesignFileSuffix = displayNameLower.endsWith("indd") ? INDD_SUFFIX
                    : INX_SUFFIX;

            String inddHiddenTranslated = eventFlow.getBatchInfo().getInddHiddenTranslated();
            if (inddHiddenTranslated != null
                    && !"".equals(inddHiddenTranslated))
            {
                m_translateHiddenLayer = "true".equals(inddHiddenTranslated);
            }
            else
            {
                m_translateHiddenLayer = true;
            }

            String masterTranslated = eventFlow.getBatchInfo().getMasterTranslated();
            if (masterTranslated != null && !"".equals(masterTranslated))
            {
                m_masterTranslated = "true".equals(masterTranslated);
            }
        }
    }

    /**
     * Returns the convert directory
     * 
     * @return
     * @throws Exception
     */
    private String getConvertDir() throws Exception
    {
        if (m_versionType == ADOBE_FM9)
        {
            return FrameHelper.getConversionDir() + File.separator;
        }
        else
        {
            StringBuffer convDir = null;
            if (m_versionType == ADOBE_CS2)
            {
                // These are adobe InDesign cs2 files, we will use cs2 converter
                // to process them.
                convDir = new StringBuffer(
                        m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR));
            }
            else if (m_versionType == ADOBE_CS4)
            {
                // These are adobe InDesign cs4 files, we will use cs4 converter
                // to process them.
                convDir = new StringBuffer(
                        m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS4));
            }
            else if (m_versionType == ADOBE_CS5)
            {
                convDir = new StringBuffer(
                        m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5));
            }
            else if (m_versionType == ADOBE_CS5_5)
            {
                convDir = new StringBuffer(
                        m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5));
            }
            else if (m_versionType == ADOBE_TYPE_IDML)
            {
                return IdmlHelper.getConversionDir();
            }
            else
            {
                // These (formatType are "indd_cs3" and "inx_cs3") are adobe
                // InDesign cs3 files, we will use cs3 converter to process
                // them.
                convDir = new StringBuffer(
                        m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS3));
            }
            convDir.append(File.separator);
            convDir.append(m_conversionType);
            convDir.append(File.separator);
            return convDir.toString();
        }
    }

    /**
     * Writes xml file to convert directory
     * 
     * @param p_request
     */
    private void writeXMLFileToConvertDir(HttpServletRequest p_request,
            String p_xmlFilePath, String p_tarFileName) throws Exception
    {
        String sourceLocale = m_job.getSourceLocale().toString();
        String sourcePageName = null;
        if (sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID) != null)
        {
            String sourcePageId = (String) sessionMgr
                    .getAttribute(WebAppConstants.SOURCE_PAGE_ID);
            long pageId = Long.parseLong(sourcePageId);
            SourcePage sourcePage = ServerProxy.getPageManager().getSourcePage(
                    pageId);
            sourcePageName = sourcePage.getExternalPageId();
        }
        else
        {
            int index = Math.max(p_tarFileName.indexOf("/"),
                    p_tarFileName.indexOf("\\"));
            sourcePageName = sourceLocale + p_tarFileName.substring(index);
        }

        long tpgId = 0;

        for (Workflow wf : m_job.getWorkflows())
        {
            if (targetLocale.equals(wf.getTargetLocale().toString()))
            {
                boolean isBreak = false;
                Vector targetPgs = wf.getTargetPages();
                Iterator tgsIterator = targetPgs.iterator();
                while (tgsIterator.hasNext())
                {
                    TargetPage tpg = (TargetPage) tgsIterator.next();
                    if (tpg.getSourcePage().getExternalPageId()
                            .equals(sourcePageName))
                    {
                        tpgId = tpg.getId();
                        isBreak = true;
                        break;
                    }
                }
                if (isBreak)
                    break;
            }
        }

        ExportHelper ex = new ExportHelper();
        try
        {
            if (tpgId == 0)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "No target page found");
            }
            String xml = ex.exportForPdfPreview(tpgId, "UTF-8", false);
            // GBS-2955
            if (ADOBE_INDD.equals(m_conversionType)
                    || ADOBE_INX.equals(m_conversionType))
            {
                xml = AdobeHelper.recoverLineBreak(xml);
            }
            FontMappingHelper fontMappinghelper = new FontMappingHelper();
            String processed = (m_versionType == ADOBE_TYPE_IDML) ? xml
                    : fontMappinghelper.processInddXml(targetLocale, xml);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p_xmlFilePath.toString()),
                    ExportConstants.UTF8), processed.length());
            writer.write(processed);
            writer.close();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Writes command file for adobe converter
     * 
     * @param p_xmlFileName
     */
    private void writeCommandFile(String p_xmlFileName) throws IOException
    {
        String commandFileName = getCommandFileName(p_xmlFileName);
        StringBuffer text = new StringBuffer();
        text.append("ConvertFrom=xml").append("\r\n");
        text.append("ConvertTo=").append(m_conversionType).append("\r\n");
        text.append("AcceptChanges=true").append("\r\n");
        text.append("MasterTranslated=").append(m_masterTranslated)
                .append("\r\n");
        text.append("TranslateHiddenLayer=").append(m_translateHiddenLayer)
                .append("\r\n");

        FileUtil.writeFileAtomically(new File(commandFileName),
                text.toString(), "US-ASCII");
    }

    /**
     * Returns the command file name
     * 
     * @param p_xmlFileName
     * @return
     */
    private String getCommandFileName(String p_xmlFileName)
    {
        return FileUtils.getPrefix(p_xmlFileName) + PV_COMMAND_SUFFIX;
    }

    /**
     * Writes output file for outputing pdf file
     * 
     * @param p_pdfFile
     * @param p_response
     */
    public static void writeOutFile(File p_pdfFile,
            HttpServletResponse p_response, String p_action)
    {
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(p_pdfFile));
            OutputStream out = p_response.getOutputStream();
            byte[] buffer = new byte[BUFFERSIZE];
            int readLen = 0;
            while ((readLen = bis.read(buffer, 0, BUFFERSIZE)) != -1)
            {
                out.write(buffer, 0, readLen);
            }
            bis.close();
        }
        catch (IOException e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private File getOldPreviewPdf(HttpServletRequest p_request)
    {
        String filePath = p_request.getParameter("file");
        int index = filePath.lastIndexOf(".");
        String pdfPath = filePath.substring(0, index) + PDF_SUFFIX;
        StringBuffer pdfFullPath = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getAbsolutePath());
        pdfFullPath.append(File.separator);
        pdfFullPath.append(pdfPath);

        File pdfFile = new File(pdfFullPath.toString());
        return pdfFile;
    }

    /**
     * Returns the previewed pdf file
     * 
     * @param p_request
     * @return
     */
    private File getPreviewPdf(HttpServletRequest p_request, String userid,
            String action)
    {
        String filePath = p_request.getParameter("file");
        int index = filePath.lastIndexOf(".");
        String pdfPath = filePath.substring(0, index) + PDF_SUFFIX;
        StringBuffer pdfFullPath = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getAbsolutePath());

        if ("previewTar".equals(action))
        {
            pdfFullPath.append(File.separator);
            pdfFullPath.append(userid);
        }

        pdfFullPath.append(File.separator);
        pdfFullPath.append(pdfPath);

        File pdfFile = new File(pdfFullPath.toString());
        return pdfFile;
    }

    /**
     * Returns Job instance
     * 
     * @param p_jobId
     * @return
     */
    private Job getJobById(long p_jobId)
    {
        Job job = null;
        try
        {
            job = ServerProxy.getJobHandler().getJobById(p_jobId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return job;
    }

    /**
     * Returns the max wait time for Adobe converter
     * 
     * @return
     */
    private long getMaxWaitTime()
    {
        String maxWaitTime = "";
        try
        {
            Properties m_adobeProperties = ((DynamicPropertiesSystemConfiguration) SystemConfiguration
                    .getInstance(PROPERTY_FILES[1])).getProperties();
            m_adobeProperties.load(AdobeConfiguration.class
                    .getResourceAsStream(PROPERTY_FILES[0]));
            maxWaitTime = m_adobeProperties
                    .getProperty(AdobeConfiguration.MAX_TIME_TO_WAIT);

        }
        catch (IOException e)
        {
            throw new EnvoyServletException(e);
        }
        return Long.parseLong(maxWaitTime) * AdobeConfiguration.MINUTE;
    }

    public static void deleteOldPdfByUser(String userid)
    {
        DeleteOldPdfThread t = new DeleteOldPdfThread(userid, CATEGORY);
        t.run();
    }

    public static void deleteOldPdf(long p_targetPageId, long p_targetLocaleId)
    {
        try
        {
            TargetPage tPage = ServerProxy.getPageManager().getTargetPage(
                    p_targetPageId);
            String company_id = String.valueOf(tPage.getSourcePage()
                    .getCompanyId());
            String filename = tPage.getSourcePage().getExternalPageId();
            String fileSuffix = filename.substring(filename.lastIndexOf("."));
            if (INDD_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || INX_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || FM_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || IDML_SUFFIX.equalsIgnoreCase(fileSuffix))
            {
                String targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                String targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                String targetPdfFileName = FileUtils.getPrefix(targetFileName)
                        + PDF_SUFFIX;
                File previewDir = AmbFileStoragePathUtils
                        .getPdfPreviewDir(company_id);
                String fullTargetPdfFileName = previewDir + File.separator
                        + targetPdfFileName;
                FileUtils.deleteSilently(fullTargetPdfFileName);

                File[] files = previewDir.listFiles(new FileFilter()
                {
                    @Override
                    public boolean accept(File arg0)
                    {
                        if (arg0.isDirectory())
                        {
                            return true;
                        }

                        return false;
                    }
                });

                if (files != null)
                {
                    for (File file : files)
                    {
                        String userPreviewFile = file + File.separator
                                + targetPdfFileName;
                        FileUtils.deleteSilently(userPreviewFile);
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not get the target page.");
            throw new EnvoyServletException(e);
        }
    }
}

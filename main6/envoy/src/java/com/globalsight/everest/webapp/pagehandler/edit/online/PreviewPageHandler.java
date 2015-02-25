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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.msoffice.ExcelFileManager;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlRepairer;
import com.globalsight.cxe.adapter.msoffice.PptxFileManager;
import com.globalsight.cxe.adapter.msoffice2010.MsOffice2010Converter;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeConverter;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
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
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.merger.html.HtmlPreviewerHelper;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.lowagie.text.DocumentException;

public class PreviewPageHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(PreviewPageHandler.class);

    private static final String ODT_EXT = ".odt";
    private static final String ODP_EXT = ".odp";
    private static final String ODS_EXT = ".ods";

    private static final String DOCX_EXT = ".docx";
    private static final String PPTX_EXT = ".pptx";
    private static final String XLSX_EXT = ".xlsx";

    private static final String HTML_SUFFIX = ".html";

    private static final String LOCALE_PRE_CONVERTED = "iw_IL";

    private static final String LOCALE_POST_CONVERTED = "he_IL";

    static private final String[] PROPERTY_FILES =
    { "/properties/Logger.properties",
            "/properties/OpenOfficeAdapter.properties" };

    private String sourceLocale;
    private String targetLocale;

    private SessionManager sessionMgr = null;

    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private String m_company_id = "";
    private EventFlowXml m_eventFlow = null;
    private String m_relSafeName = null;;
    private String m_safeBaseFileName = null;
    private String m_convDir = null;
    private String formatType = null;

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
        m_job = getJobById(Long.valueOf(jobId).longValue());
        if (m_job != null)
        {
            m_company_id = String.valueOf(m_job.getCompanyId());
        }
        else
        {
            m_company_id = CompanyThreadLocal.getInstance().getValue();
            CATEGORY.warn("Get company id from ThreadLocal for openoffice preview.");
        }

        Object userobj = sessionMgr.getAttribute(WebAppConstants.USER);
        UserImpl user = null;
        if (userobj != null)
        {
            user = (UserImpl) userobj;
        }
        String userid = user == null ? null : user.getUserId();
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);

        sourceLocale = m_job.getSourceLocale().toString();

        String action = p_request.getParameter("action") == null ? ""
                : (String) p_request.getParameter("action");
        File preFile = getPreviewFile(p_request, userid);
        File pdfFile = getPreviewPdfFile(p_request, userid);
        File preFileInWAR = getPreviewFileInWar(p_request, p_context, userid);
        String htmlurl = getPreviewUrl(p_request, userid);

        if (action != null)
        {
            try
            {
                if (action.equals("previewSrc"))
                {
                    if (!preFile.exists() || !preFileInWAR.exists())
                    {
                        File srcFile = getSourceFile(p_request);

                        if (IFormatNames.FORMAT_OPENOFFICE_XML
                                .equals(formatType))
                        {
                            OpenOfficeConverter ooConv = new OpenOfficeConverter();

                            ooConv.convertOdToHtml(srcFile, preFile);
                        }
                        else if (IFormatNames.FORMAT_OFFICE_XML
                                .equals(formatType))
                        {
                            MsOffice2010Converter converter = new MsOffice2010Converter();

                            if (srcFile.getPath().toLowerCase()
                                    .endsWith(".docx"))
                            {
                                converter.convertToHtml(srcFile, preFile,
                                        pdfFile, sourceLocale, false, true);
                            }
                            else
                            {
                                converter.convertToHtml(srcFile, preFile,
                                        pdfFile, sourceLocale, false, false);
                            }
                        }

                        preFileInWAR.getParentFile().mkdirs();
                        FileCopier.copyDir(preFile.getParentFile(),
                                preFileInWAR.getParent());
                    }
                }
                else if (action.equals("previewTar"))
                {
                    boolean newConved = false;
                    if (!preFile.exists())
                    {
                        String targetPageName = getFilePathFromRequest(p_request);
                        int index = Math.max(targetPageName.indexOf("/"),
                                targetPageName.indexOf("\\"));
                        targetLocale = targetPageName.substring(0, index);
                        if (LOCALE_PRE_CONVERTED.equals(targetLocale))
                        {
                            targetPageName = targetPageName.replaceFirst(
                                    targetLocale, LOCALE_POST_CONVERTED);
                            targetLocale = LOCALE_POST_CONVERTED;
                        }
                        String converterDir = m_convDir + File.separator
                                + targetLocale;
                        new File(converterDir).mkdirs();

                        File odFile = new File(converterDir, m_safeBaseFileName);
                        if (!odFile.exists())
                        {
                            copyFilesToNewTargetLocale();
                        }

                        String xmlFilePath = converterDir + File.separator
                                + m_relSafeName;
                        File zipDir = getZipDir(new File(xmlFilePath),
                                m_safeBaseFileName);

                        if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
                        {
                            if (odFile.getPath().toLowerCase()
                                    .endsWith(".pptx"))
                            {
                                PptxFileManager m = new PptxFileManager();
                                m.splitFile(zipDir.getPath());
                            }
                            else if (odFile.getPath().toLowerCase()
                                    .endsWith(".xlsx"))
                            {
                                ExcelFileManager m = new ExcelFileManager();
                                m.mergeSortSegments(zipDir.getPath());
                            }
                            
                            writeXMLFileForOffice(p_request, userid, state);
                            OfficeXmlRepairer.repair(zipDir.getPath());
                            MsOffice2010Converter converter = new MsOffice2010Converter();
                            converter.convertXmlToOffice(odFile.getName(),
                                    zipDir.getPath());
                            if (odFile.getPath().toLowerCase()
                                    .endsWith(".docx"))
                            {
                                converter.convertToHtml(odFile, preFile,
                                        pdfFile, targetLocale, true, true);
                            }
                            else
                            {
                                converter.convertToHtml(odFile, preFile,
                                        pdfFile, targetLocale, false, false);
                            }
                        }
                        else
                        {
                            // write xml file
                            writeXMLFileToConvertDir(p_request, xmlFilePath,
                                    targetPageName);
                            OpenOfficeConverter ooConv = new OpenOfficeConverter();
                            // convert xml to OD
                            ooConv.convertXmlToOd(odFile.getName(),
                                    zipDir.getPath());
                            // convert od to html
                            ooConv.convertOdToHtml(odFile, preFile);
                        }

                        newConved = true;
                    }

                    if (newConved || !preFileInWAR.exists())
                    {
                        File predir = preFile.getParentFile();

                        // copy all files into war
                        preFileInWAR.getParentFile().mkdirs();
                        FileCopier.copyDir(predir, preFileInWAR.getParent());
                    }
                }

                if (pdfFile.exists())
                {
                    try
                    {
                        File viewFile = PreviewPDFHelper
                                .setCopyOnlyPermission(pdfFile);

                        p_response.setContentType("application/pdf");
                        if (p_request.isSecure())
                        {
                            setHeaderForHTTPSDownload(p_response);
                        }
                        else
                        {
                            p_response.setHeader("Cache-Control", "no-cache");
                        }

                        // filename, maybe we need to handle some specail
                        // character,
                        // like &
                        String filename = pdfFile.getName();
                        p_response.setHeader("Content-Disposition",
                                "inline; filename=\"" + filename + "\"");
                        PreviewPDFHelper.writeOutFile(viewFile,
                                p_response, action);
                        FileUtils.deleteSilently(viewFile.getAbsolutePath());
                    }
                    catch (DocumentException e)
                    {
                        throw new EnvoyServletException(e);
                    }
                }
                else
                {
                    writeOutUrl(p_response, htmlurl);
                }
            }
            catch (Exception e)
            {
                handleException(session, p_response, e, action);
            }

            return;
        }
        else
        {
            CATEGORY.error("action is null.");
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

    /**
     * Copies files to the folder of newly added target locale.
     */
    private void copyFilesToNewTargetLocale()
    {
        L10nProfile lp = m_job.getL10nProfile();
        GlobalSightLocale[] existedLocales = lp.getTargetLocales();
        copyOpenOfficeFiles(existedLocales);
    }

    private void copyOpenOfficeFiles(GlobalSightLocale[] p_existedLocales)
    {
        if (p_existedLocales != null)
        {
            boolean copied = false;
            String conDir = m_convDir;
            String tgtDir = conDir + File.separator + targetLocale;
            StringBuffer existsLocales = new StringBuffer();
            for (GlobalSightLocale gsLocale : p_existedLocales)
            {
                String existedLocale = gsLocale.toString();
                if (LOCALE_PRE_CONVERTED.equals(existedLocale))
                    existedLocale = LOCALE_POST_CONVERTED;

                existsLocales.append(existedLocale).append(" ");
                StringBuffer odFile = new StringBuffer(conDir);
                odFile.append(File.separator).append(existedLocale)
                        .append(File.separator).append(m_safeBaseFileName);

                String existsPDir = conDir + File.separator + existedLocale;
                File relSafeFile = new File(existsPDir, m_relSafeName);
                File parent = relSafeFile.getParentFile();
                File srcFile = new File(odFile.toString());

                if (srcFile.exists())
                {
                    if (!parent.getName().contains("."))
                    {
                        parent = parent.getParentFile();
                    }
                    
                    String newPDir = tgtDir + File.separator + parent.getName();
                    File newPDirFile = new File(newPDir);
                    if (!newPDirFile.exists())
                    {
                        newPDirFile.mkdirs();
                    }
                    FileCopier.copy(srcFile, tgtDir);
                    FileCopier.copyDir(parent, newPDir);
                    copied = true;
                }
                
                if (copied)
                {
                    break;
                }
            }
            
            if (!copied)
            {
                String msg = "Cannot copy files to " + tgtDir
                        + ". p_existedLocales: " + existsLocales.toString();
                CATEGORY.error(msg);
            }
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

            m_eventFlow = XmlUtil.string2Object(EventFlowXml.class, sourcePage.getRequest()
                    .getEventFlowXml());
            formatType = m_eventFlow.getSource().getFormatType();
            if (IFormatNames.FORMAT_OPENOFFICE_XML.equals(formatType))
            {
                m_convDir = OpenOfficeHelper.getConversionDir();
            }
            else if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
            {
                m_convDir = OfficeXmlHelper.getConversionDir();
            }
            m_relSafeName = m_eventFlow.getValue("relSafeName");
            m_safeBaseFileName = m_eventFlow.getValue("safeBaseFileName");;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void writeFile(SourcePage sourcePage, String userid,
            EditorState state)
    {
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
                            .equals(sourcePage.getExternalPageId()))
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

        String converterDir = m_convDir + File.separator + targetLocale;
        EventFlowXml eventFlow = XmlUtil.string2Object(EventFlowXml.class, sourcePage.getRequest()
                .getEventFlowXml());

        String relSafeName = eventFlow.getValue("relSafeName");
        String path = converterDir + File.separator + relSafeName;
        ExportHelper ex = new ExportHelper();
        try
        {
            if (tpgId == 0)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
                        "No target page found");
            }
            String xml = null;

            if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
            {
                ex.setUserId(userid);
                ex.setEditorState(state);
                xml = ex.getTargetXmlContent(tpgId,
                        CxeMessageType.XML_LOCALIZED_EVENT, true);
            }
            else
            {
                xml = ex.exportForPdfPreview(tpgId, "UTF-8", false);
            }
            xml = fixOfficePreviewXml(xml);
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(path.toString()),
                            ExportConstants.UTF8), xml.length());
            writer.write(xml);
            writer.close();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Writes xml file to convert directory
     * 
     * @param p_request
     */
    private void writeXMLFileForOffice(HttpServletRequest p_request,
            String userid, EditorState state) throws Exception
    {
        String sourceLocale = m_job.getSourceLocale().toString();
        String sourcePageId = (String) sessionMgr
                .getAttribute(WebAppConstants.SOURCE_PAGE_ID);
        long pageId = Long.parseLong(sourcePageId);

        SourcePage sourcePage = ServerProxy.getPageManager().getSourcePage(
                pageId);
        List<SourcePage> pages = new ArrayList<SourcePage>();
        File file = sourcePage.getFile();

        for (Object ob : m_job.getSourcePages())
        {
            SourcePage page = (SourcePage) ob;
            File f = page.getFile();

            if (file.getPath().equals(f.getPath()))
            {
                writeFile(page, userid, state);
            }
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
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
                        "No target page found");
            }
            String xml = ex.exportForPdfPreview(tpgId, "UTF-8", false);
            xml = fixOpenOfficeXml(xml);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p_xmlFilePath.toString()),
                    ExportConstants.UTF8), xml.length());
            writer.write(xml);
            writer.close();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Fix issues in open office files
     */
    private String fixOpenOfficeXml(String p_content) throws Exception
    {
        if (m_relSafeName != null && m_relSafeName.length() > 0)
        {
            if (m_relSafeName.endsWith(OpenOfficeHelper.XML_CONTENT)
                    && m_relSafeName.toLowerCase().contains(".ods.1"))
            {
                String oriXmlPath = OpenOfficeHelper.getConversionDir()
                        + File.separator + sourceLocale + File.separator
                        + m_relSafeName;
                File oriXmlFile = new File(oriXmlPath);
                if (oriXmlFile.exists())
                {
                    String oriXml = FileUtils.read(oriXmlFile, "UTF-8");
                    return OpenOfficeHelper.fixContentXmlForOds(p_content,
                            oriXml, sourceLocale, targetLocale, m_relSafeName);
                }
            }
        }

        return p_content;
    }

    private String fixOfficePreviewXml(String p_content) throws Exception
    {
        if (m_relSafeName != null && m_relSafeName.length() > 0)
        {
            if (m_relSafeName.toLowerCase().contains(".docx.0")
                    && p_content.contains(ExportHelper.GS_COLOR_S))
            {
                return HtmlPreviewerHelper.fixOfficePreviewXml(p_content);
            }
        }

        return p_content;
    }

    private void writeOutUrl(HttpServletResponse p_response, String url)
    {
        try
        {
            StringBuffer sb = new StringBuffer("<SCRIPT>");
            sb.append("document.location=\"" + url + "\";");
            sb.append("</SCRIPT>");
            p_response.getWriter().write(sb.toString());
        }
        catch (IOException e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void handleException(HttpSession p_session,
            HttpServletResponse p_response, Exception ex, String action)
            throws IOException
    {
        CATEGORY.error(ex);

        int modeId = EditorConstants.VIEWMODE_DETAIL; // list
        String contentLocation = null;
        String menuLocation = null;
        String menuStr = null;
        if (action != null && action.equals("previewSrc"))
        {
            contentLocation = "/globalsight/ControlServlet?linkName=content&pageName=ED4&srcViewMode="
                    + modeId;
            menuLocation = "/globalsight/ControlServlet?linkName=sourceMenu&pageName=ED4&srcViewMode="
                    + modeId;
            menuStr = "parent.sourceMenu.document.location=\"" + menuLocation
                    + "\";";
        }
        else if (action != null && action.equals("previewTar"))
        {
            contentLocation = "/globalsight/ControlServlet?linkName=content&pageName=ED7&trgViewMode="
                    + modeId;
            menuLocation = "/globalsight/ControlServlet?linkName=targetMenu&pageName=ED7&trgViewMode="
                    + modeId;
            menuStr = "parent.targetMenu.document.location=\"" + menuLocation
                    + "\";";
        }

        ResourceBundle rb = PageHandler.getBundle(p_session);
        String errorMsg = ex.getMessage();
        String msg1 = rb.getString("lb_filter_msg_oopreview_error");
        String msg2 = rb.getString("lb_msg_use_list_view");

        if (errorMsg != null)
        {
            if (errorMsg.contains("officeHome"))
            {
                msg1 = rb.getString("lb_filter_msg_oopreview_error_oohome");
            }
            else if (errorMsg.endsWith(" converter is not started"))
            {
                String s = errorMsg.replace(" converter is not started", "");
                msg1 = rb.getString("lb_filter_msg_convert_start_" + s);
            }
        }

        StringBuffer sb = new StringBuffer("<SCRIPT>");
        sb.append(menuStr);
        sb.append("alert(\"" + msg1 + "\\r\\n\\r\\n" + msg2 + "\");");
        sb.append("document.location=\"" + contentLocation + "\";");
        sb.append("</SCRIPT>");
        p_response.getWriter().write(sb.toString());
    }

    /**
     * Returns the previewed file
     * 
     * @param p_request
     * @return
     */
    private File getPreviewFile(HttpServletRequest p_request, String userId)
    {
        String filePath = getFilePathFromRequest(p_request);
        String htmlRoot = AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getPath();

        StringBuffer fullPath = new StringBuffer(htmlRoot);
        fullPath.append(File.separator);
        fullPath.append(userId);
        fullPath.append(File.separator);
        fullPath.append(filePath);
        fullPath.append(HTML_SUFFIX);
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        File file = new File(fullPath.toString());
        return file;
    }

    private File getPreviewPdfFile(HttpServletRequest p_request, String userId)
    {
        String filePath = getFilePathFromRequest(p_request);
        String htmlRoot = AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getPath();

        StringBuffer fullPath = new StringBuffer(htmlRoot);
        fullPath.append(File.separator);
        fullPath.append(userId);
        fullPath.append(File.separator);
        fullPath.append(filePath);
        fullPath.append(HTML_SUFFIX);
        fullPath.append(File.separator);
        fullPath.append("exported.pdf");

        File file = new File(fullPath.toString());
        return file;
    }

    private File getPreviewFileInWar(HttpServletRequest p_request,
            ServletContext p_context, String userId)
    {
        // String filePath = p_request.getParameter("file");
        String htmlRoot = p_context.getRealPath("/resources/preview/");

        StringBuffer fullPath = new StringBuffer(htmlRoot);
        fullPath.append(File.separator);
        fullPath.append(userId);
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("type"));
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("pageId"));
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        File file = new File(fullPath.toString());
        return file;
    }

    private File getSourceFile(HttpServletRequest p_request)
    {
        String filePath = getFilePathFromRequest(p_request);
        
        String companyName = null;
        if(CompanyThreadLocal.getInstance().fromSuperCompany())
        {
            companyName = CompanyWrapper.getCompanyNameById(m_company_id);
        }

        StringBuffer fullPath = new StringBuffer(
                AmbFileStoragePathUtils.getCxeDocDirPath());
        if (companyName != null)
        {
            fullPath.append(File.separator);
            fullPath.append(companyName);
        }
        fullPath.append(File.separator);
        fullPath.append(filePath);

        File file = new File(fullPath.toString());
        return file;
    }

    private String getPreviewUrl(HttpServletRequest p_request, String userId)
    {
        // String filePath = getFilePathFromRequest(p_request);

        StringBuffer fullPath = new StringBuffer("resources/preview/");
        fullPath.append(File.separator);
        fullPath.append(userId);
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("type"));
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("pageId"));
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        return fullPath.toString().replace("\\", "/");
    }

    private String getPreviewPdfUrl(HttpServletRequest p_request, String userId)
    {
        // String filePath = getFilePathFromRequest(p_request);

        StringBuffer fullPath = new StringBuffer("resources/preview/");
        fullPath.append(File.separator);
        fullPath.append(userId);
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("type"));
        fullPath.append(File.separator);
        fullPath.append(p_request.getParameter("pageId"));
        fullPath.append(File.separator);
        fullPath.append("exported.pdf");

        return fullPath.toString().replace("\\", "/");
    }

    private String getFilePathFromRequest(HttpServletRequest p_request)
    {
        String fPath = p_request.getParameter("file");

        if (fPath == null)
            return fPath;

        if (fPath.startsWith(OpenOfficeHelper.OO_HEADER_DISPLAY_NAME_PREFIX))
        {
            fPath = fPath
                    .substring(OpenOfficeHelper.OO_HEADER_DISPLAY_NAME_PREFIX
                            .length());
            fPath = fPath.trim();
        }
        else
        {
            fPath = OfficeXmlHelper.getOriginalFilename(fPath);
        }

        return fPath;
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

    public static void deleteOldPreviewFile(long p_targetPageId,
            long p_targetLocaleId)
    {
        try
        {
            TargetPage tPage = ServerProxy.getPageManager().getTargetPage(
                    p_targetPageId);
            SourcePage sPage = tPage.getSourcePage();
            String company_id = String.valueOf(sPage.getCompanyId());
            String filename = sPage.getExternalPageId();
            String fileSuffix = filename.substring(filename.lastIndexOf("."));
            if (ODT_EXT.equalsIgnoreCase(fileSuffix)
                    || ODP_EXT.equalsIgnoreCase(fileSuffix)
                    || ODS_EXT.equalsIgnoreCase(fileSuffix)
                    || DOCX_EXT.equalsIgnoreCase(fileSuffix)
                    || PPTX_EXT.equalsIgnoreCase(fileSuffix)
                    || XLSX_EXT.equalsIgnoreCase(fileSuffix))
            {
                String targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                String targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                String targetHtmlFileName = targetFileName + HTML_SUFFIX
                        + File.separator + "exported.html";
                File previewDir = AmbFileStoragePathUtils
                        .getPdfPreviewDir(company_id);
                String fullTargetHtmlFileName = previewDir + File.separator
                        + targetHtmlFileName;
                FileUtils.deleteSilently(fullTargetHtmlFileName);

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
                                + targetHtmlFileName;
                        FileUtils.deleteSilently(userPreviewFile);
                    }
                }
            }

            if ("xlsx".equalsIgnoreCase(fileSuffix)
                    || "docx".equalsIgnoreCase(fileSuffix)
                    || "pptx".equalsIgnoreCase(fileSuffix))
            {
                String targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                String targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                String root = targetFileName + HTML_SUFFIX;
                File f = new File(root);
                if (f.exists())
                    FileUtil.getAllFiles(f);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not get the target page.");
            throw new EnvoyServletException(e);
        }
    }
}

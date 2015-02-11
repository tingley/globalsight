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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Priority;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeConfiguration;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeConverter;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.SourcePage;
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
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.OpenOfficeFilter;

public class PreviewPageHandler extends PageHandler
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
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

    static private final String[] PROPERTY_FILES = { "/properties/Logger.properties",
            "/properties/OpenOfficeAdapter.properties" };

    private String sourceLocale;
    private String targetLocale;

    private SessionManager sessionMgr = null;

    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private String m_company_id = "";
    private EventFlow m_eventFlow = null;
    private String m_relSafeName = null;;
    private String m_safeBaseFileName = null;
    private String m_convDir = null;

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(true);
        sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);

        determineConversionParameters();

        String jobId = (String) sessionMgr.getAttribute(WebAppConstants.JOB_ID);
        m_job = getJobById(Long.valueOf(jobId).longValue());
        if (m_job != null)
        {
            m_company_id = m_job.getCompanyId();
        }
        else
        {
            m_company_id = CompanyThreadLocal.getInstance().getValue();
            CATEGORY.warn("Get company id from ThreadLocal for openoffice preview.");
        }

        sourceLocale = m_job.getSourceLocale().toString();

        String action = p_request.getParameter("action") == null ? "" : (String) p_request
                .getParameter("action");
        File preFile = getPreviewFile(p_request);
        File preFileInWAR = getPreviewFileInWar(p_request, p_context);
        String htmlurl = getPreviewUrl(p_request);

        if (action != null)
        {
            try
            {
                if (action.equals("previewSrc"))
                {
                    if (!preFile.exists() || !preFileInWAR.exists())
                    {
                        OpenOfficeConverter ooConv = new OpenOfficeConverter();
                        File srcFile = getSourceFile(p_request);
                        ooConv.convertOdToHtml(srcFile, preFile);
                        preFileInWAR.getParentFile().mkdirs();
                        FileCopier.copyDir(preFile.getParentFile(), preFileInWAR.getParent());
                    }
                }
                else if (action.equals("previewTar"))
                {
                    boolean newConved = false;
                    if (!preFile.exists())
                    {
                        String targetPageName = getFilePathFromRequest(p_request);
                        int index = Math.max(targetPageName.indexOf("/"), targetPageName
                                .indexOf("\\"));
                        targetLocale = targetPageName.substring(0, index);
                        if (LOCALE_PRE_CONVERTED.equals(targetLocale))
                        {
                            targetPageName = targetPageName.replaceFirst(targetLocale,
                                    LOCALE_POST_CONVERTED);
                            targetLocale = LOCALE_POST_CONVERTED;
                        }
                        String converterDir = m_convDir + File.separator + targetLocale;
                        new File(converterDir).mkdirs();

                        File odFile = new File(converterDir, m_safeBaseFileName);
                        if (!odFile.exists())
                        {
                            copyFilesToNewTargetLocale();
                        }

                        String xmlFilePath = converterDir + File.separator + m_relSafeName;
                        File zipDir = getZipDir(new File(xmlFilePath), m_safeBaseFileName);
                        OpenOfficeConverter ooConv = new OpenOfficeConverter();

                        // write xml file
                        writeXMLFileToConvertDir(p_request, xmlFilePath, targetPageName);
                        // convert xml to OD
                        ooConv.convertXmlToOd(odFile.getName(), zipDir.getPath());
                        // convert od to html
                        ooConv.convertOdToHtml(odFile, preFile);
                        newConved = true;
                    }

                    if (newConved || !preFileInWAR.exists())
                    {
                        // copy all files into war
                        preFileInWAR.getParentFile().mkdirs();
                        FileCopier.copyDir(preFile.getParentFile(), preFileInWAR.getParent());
                    }
                }

                writeOutUrl(p_response, htmlurl);
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
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
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
        String existedLocale = existedLocales[0].toString();
        if (LOCALE_PRE_CONVERTED.equals(existedLocale))
            existedLocale = LOCALE_POST_CONVERTED;
        copyOpenOfficeFiles(existedLocale);
    }

    private void copyOpenOfficeFiles(String p_existedLocale)
    {
        String conDir = m_convDir;

        StringBuffer odFile = new StringBuffer(conDir);
        odFile.append(p_existedLocale).append(File.separator).append(m_safeBaseFileName);

        File relSafeFile = new File(conDir, m_relSafeName);
        File parent = relSafeFile.getParentFile();
        String tgtDir = conDir + File.separator + targetLocale;

        FileCopier.copy(new File(odFile.toString()), tgtDir);
        FileCopier.copyDir(parent, tgtDir);
    }

    private void determineConversionParameters()
    {
        String sourcePageId = (String) sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID);
        long pageId = Long.parseLong(sourcePageId);
        SourcePage sourcePage = null;
        try
        {
            sourcePage = ServerProxy.getPageManager().getSourcePage(pageId);

            m_eventFlow = new EventFlow(sourcePage.getRequest().getEventFlowXml());
            String formatType = m_eventFlow.getSourceFormatType();
            if (IFormatNames.FORMAT_OPENOFFICE_XML.equals(formatType))
            {
                m_convDir = OpenOfficeHelper.getConversionDir();
            }
            else if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
            {
                m_convDir = OfficeXmlHelper.getConversionDir();
            }
            m_relSafeName = m_eventFlow.getDiplomatAttribute("relSafeName").getValue();
            m_safeBaseFileName = m_eventFlow.getDiplomatAttribute("safeBaseFileName").getValue();
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
    private void writeXMLFileToConvertDir(HttpServletRequest p_request, String p_xmlFilePath,
            String p_tarFileName) throws Exception
    {
        String sourceLocale = m_job.getSourceLocale().toString();
        String sourcePageName = null;
        if (sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID) != null)
        {
            String sourcePageId = (String) sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID);
            long pageId = Long.parseLong(sourcePageId);
            SourcePage sourcePage = ServerProxy.getPageManager().getSourcePage(pageId);
            sourcePageName = sourcePage.getExternalPageId();
        }
        else
        {
            int index = Math.max(p_tarFileName.indexOf("/"), p_tarFileName.indexOf("\\"));
            sourcePageName = sourceLocale + p_tarFileName.substring(index);
        }

        List workflows = new ArrayList(m_job.getWorkflows());
        long tpgId = 0;

        for (int i = 0; i < workflows.size(); i++)
        {
            Workflow wf = (Workflow) workflows.get(i);
            if (targetLocale.equals(wf.getTargetLocale().toString()))
            {
                boolean isBreak = false;
                Vector targetPgs = wf.getTargetPages();
                Iterator tgsIterator = targetPgs.iterator();
                while (tgsIterator.hasNext())
                {
                    TargetPage tpg = (TargetPage) tgsIterator.next();
                    if (tpg.getSourcePage().getExternalPageId().equals(sourcePageName))
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
                throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
                        "No target page found");
            }
            String xml = ex.exportForPdfPreview(tpgId, "UTF-8", false);
            xml = fixOpenOfficeXml(xml);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    p_xmlFilePath.toString()), ExportConstants.UTF8), xml.length());
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
                String oriXmlPath = OpenOfficeHelper.getConversionDir() + File.separator
                        + sourceLocale + File.separator + m_relSafeName;
                File oriXmlFile = new File(oriXmlPath);
                if (oriXmlFile.exists())
                {
                    String oriXml = FileUtils.read(oriXmlFile, "UTF-8");
                    return OpenOfficeHelper.fixContentXmlForOds(p_content, oriXml, sourceLocale,
                            targetLocale, m_relSafeName);
                }
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

    private void handleException(HttpSession p_session, HttpServletResponse p_response,
            Exception ex, String action) throws IOException
    {
        CATEGORY.error("Error when loading preview : " + ex.toString());

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
            menuStr = "parent.sourceMenu.document.location=\"" + menuLocation + "\";";
        }
        else if (action != null && action.equals("previewTar"))
        {
            contentLocation = "/globalsight/ControlServlet?linkName=content&pageName=ED7&trgViewMode="
                    + modeId;
            menuLocation = "/globalsight/ControlServlet?linkName=targetMenu&pageName=ED7&trgViewMode="
                    + modeId;
            menuStr = "parent.targetMenu.document.location=\"" + menuLocation + "\";";
        }

        ResourceBundle rb = PageHandler.getBundle(p_session);
        String errorMsg = ex.getMessage();
        String msg1 = rb.getString("lb_filter_msg_oopreview_error");
        String msg2 = rb.getString("lb_msg_use_list_view");

        if (errorMsg != null && errorMsg.contains("officeHome"))
        {
            msg1 = rb.getString("lb_filter_msg_oopreview_error_oohome");
        }

        StringBuffer sb = new StringBuffer("<SCRIPT>");
        sb.append(menuStr);
        sb.append("alert(\"" + msg1 + " " + msg2 + "\");");
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
    private File getPreviewFile(HttpServletRequest p_request)
    {
        String filePath = getFilePathFromRequest(p_request);
        String htmlRoot = AmbFileStoragePathUtils.getPdfPreviewDir(m_company_id).getPath();

        StringBuffer fullPath = new StringBuffer(htmlRoot);
        fullPath.append(File.separator);
        fullPath.append(filePath);
        fullPath.append(HTML_SUFFIX);
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        File file = new File(fullPath.toString());
        return file;
    }

    private File getPreviewFileInWar(HttpServletRequest p_request, ServletContext p_context)
    {
        String filePath = getFilePathFromRequest(p_request);
        String htmlRoot = p_context.getRealPath("/resources/openofficepreview/");

        StringBuffer fullPath = new StringBuffer(htmlRoot);
        fullPath.append(File.separator);
        fullPath.append(filePath);
        fullPath.append(HTML_SUFFIX);
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        File file = new File(fullPath.toString());
        return file;
    }

    private File getSourceFile(HttpServletRequest p_request)
    {
        String filePath = getFilePathFromRequest(p_request);

        StringBuffer fullPath = new StringBuffer(AmbFileStoragePathUtils.getCxeDocDirPath());
        fullPath.append(File.separator);
        fullPath.append(filePath);

        File file = new File(fullPath.toString());
        return file;
    }

    private String getPreviewUrl(HttpServletRequest p_request)
    {
        String filePath = getFilePathFromRequest(p_request);

        StringBuffer fullPath = new StringBuffer("resources/openofficepreview/");
        fullPath.append(File.separator);
        fullPath.append(filePath);
        fullPath.append(HTML_SUFFIX);
        fullPath.append(File.separator);
        fullPath.append("exported.html");

        return fullPath.toString().replace("\\", "/");
    }

    private String getFilePathFromRequest(HttpServletRequest p_request)
    {
        String fPath = p_request.getParameter("file");

        if (fPath == null)
            return fPath;

        if (fPath.startsWith(OpenOfficeHelper.OO_HEADER_DISPLAY_NAME_PREFIX))
        {
            fPath = fPath.substring(OpenOfficeHelper.OO_HEADER_DISPLAY_NAME_PREFIX.length());
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

    public static void deleteOldPreviewFile(long p_targetPageId, long p_targetLocaleId)
    {
        try
        {
            TargetPage tPage = ServerProxy.getPageManager().getTargetPage(p_targetPageId);
            SourcePage sPage = tPage.getSourcePage();
            String company_id = sPage.getCompanyId();
            String filename = sPage.getExternalPageId();
            String fileSuffix = filename.substring(filename.lastIndexOf("."));
            if (ODT_EXT.equalsIgnoreCase(fileSuffix) || ODP_EXT.equalsIgnoreCase(fileSuffix)
                    || ODS_EXT.equalsIgnoreCase(fileSuffix)
                    || DOCX_EXT.equalsIgnoreCase(fileSuffix)
                    || PPTX_EXT.equalsIgnoreCase(fileSuffix)
                    || XLSX_EXT.equalsIgnoreCase(fileSuffix))
            {
                String targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                String targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                String targetHtmlFileName = targetFileName + HTML_SUFFIX + File.separator
                        + "exported.html";
                String fullTargetHtmlFileName = AmbFileStoragePathUtils
                        .getPdfPreviewDir(company_id)
                        + File.separator + targetHtmlFileName;
                FileUtils.deleteSilently(fullTargetHtmlFileName);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not get the target page.");
            throw new EnvoyServletException(e);
        }
    }
}

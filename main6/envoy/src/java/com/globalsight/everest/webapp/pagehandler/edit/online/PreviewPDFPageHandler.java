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
import com.globalsight.cxe.adapter.adobe.AdobeConfiguration;
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
import com.globalsight.cxe.entity.filterconfiguration.InddFilter;

public class PreviewPDFPageHandler extends PageHandler
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(PreviewPDFPageHandler.class);

    private static int BUFFERSIZE = 4096;

    private static final String ADOBE_INDD = "indd";

    private static final String ADOBE_INX = "inx";

    private static final String PDF_SUFFIX = ".pdf";

    private static final String XML_SUFFIX = ".xml";

    private static final String XMP_SUFFIX = ".xmp";

    private static final String INDD_SUFFIX = ".indd";

    private static final String INX_SUFFIX = ".inx";

    private static final String STATUS_SUFFIX = ".status";

    private static final String COMMAND_STATUS_SUFFIX = ".pv_status";

    private static final String PV_COMMAND_SUFFIX = ".pv_command";

    private static final String LOCALE_PRE_CONVERTED = "iw_IL";

    private static final String LOCALE_POST_CONVERTED = "he_IL";

    private static final int ADOBE_CS2 = 0;

    private static final int ADOBE_CS3 = 1;
    
    private static final int ADOBE_CS4 = 2;
    
    private static final int ADOBE_CS5 = 3;
    
    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private int m_versionType = ADOBE_CS2;

    private String m_conversionType = ADOBE_INDD;

    private String m_inDesignFileSuffix = INDD_SUFFIX;
    
    private boolean m_masterTranslated = true;
    
    private boolean m_translateHiddenLayer = false;

    static private final String[] PROPERTY_FILES = {
            "/properties/Logger.properties",
            "/properties/AdobeAdapter.properties" };

    private String targetLocale;

    private SessionManager sessionMgr = null;

    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private String m_company_id = "";

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
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
            CATEGORY
                    .error("If can not view the pdf file besause the company id is incorrect.");
        }
        String action = p_request.getParameter("action") == null ? ""
                : (String) p_request.getParameter("action");
        File pdfFile = getPreviewPdf(p_request);

        if (action != null)
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
                    String converterDir = getConvertDir() + targetLocale;
                    new File(converterDir).mkdirs();

                    String targetPageFolder = pdfFile.getParent();
                    File f = new File(targetPageFolder);
                    if (!f.exists())
                    {
                        f.mkdirs();
                        copyFilesToNewTargetLocale(p_request, targetPageName,
                                targetPageFolder);
                    }
                    String oriXmlFileName = getConvertedFileName(targetPageName);

                    Map newFileNameMap = backupInddFile(converterDir,
                            oriXmlFileName);
                    // back up the indd and xmp file
                    String xmlFilePath = converterDir + File.separator
                            + (String) newFileNameMap.get(XML_SUFFIX);
                    try
                    {
                        // write xml file
                        writeXMLFileToConvertDir(p_request, xmlFilePath,
                                targetPageName);
                        // write command file
                        writeCommandFile(xmlFilePath);
                        // 5 wait for Adobe Converter to convert
                        pdfFile = readTargetPdfFile(xmlFilePath, targetPageName);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(e);
                        super.invokePageHandler(p_pageDescriptor, p_request,
                                p_response, p_context);
                    }
                    finally
                    {
                        // 6 delete back up indd and xmp file
                        FileUtils.deleteSilently(converterDir
                                + File.separator
                                + (String) newFileNameMap
                                        .get(m_inDesignFileSuffix));
                        FileUtils.deleteSilently(converterDir + File.separator
                                + (String) newFileNameMap.get(XMP_SUFFIX));
                    }
                }
            }
            File viewFile = null;

            try
            {
                viewFile = setCopyOnlyPermission(pdfFile);
            }
            catch (DocumentException e)
            {
                throw new EnvoyServletException(e);
            }

            p_response.setContentType("application/pdf");
            if (p_request.isSecure())
            {
                setHeaderForHTTPSDownload(p_response);
            }
            else
            {
                p_response.setHeader("Cache-Control", "no-cache");
            }

            writeOutFile(viewFile, p_response, action);
            FileUtils.deleteSilently(viewFile.getAbsolutePath());
            return;
        }
        else
        {
            CATEGORY.error("action is null.");
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    public File setCopyOnlyPermission(File p_file) throws DocumentException,
            IOException
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
                PdfWriter.ALLOW_COPY);
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
                CATEGORY.error(e.toString());
            }
        }

        return new File(outPutFile);
    }

    /**
     * Copies files to the folder of newly added target locale.
     * 
     * @param p_request
     * @param p_targetPageFolder
     * @param p_targetPageName
     */
    private void copyFilesToNewTargetLocale(HttpServletRequest p_request,
            String p_targetPageName, String p_targetPageFolder)
    {
        L10nProfile lp = m_job.getL10nProfile();
        GlobalSightLocale[] existedLocales = lp.getTargetLocales();
        String existedLocale = existedLocales[0].toString();
        if (LOCALE_PRE_CONVERTED.equals(existedLocale))
            existedLocale = LOCALE_POST_CONVERTED;
        String existedTpFolder = p_targetPageFolder.replaceFirst(targetLocale,
                existedLocale);
        File file = new File(existedTpFolder);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (STATUS_SUFFIX.equalsIgnoreCase(f.getAbsolutePath().substring(
                    f.getAbsolutePath().lastIndexOf("."))))
                FileCopier.copy(f, p_targetPageFolder);
        }
        copyAdobeFiles(existedLocale, getConvertedFileName(p_targetPageName));
    }

    private void copyAdobeFiles(String p_existedLocale,
            String p_convertedFileName)
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
    private String getConvertedFileName(String p_targetPageName)
    {
        String tarName = AmbFileStoragePathUtils.getPdfPreviewDir(m_company_id)
                + File.separator + FileUtils.getPrefix(p_targetPageName)
                + STATUS_SUFFIX;
        if (!new File(tarName).exists())
        {
            tarName = tarName.replace("\\", "/");
            if (!new File(tarName).exists())
            {
                CATEGORY.error(tarName + " does not exist");
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        tarName + " does not exist");
            }
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
                .substring(convertedFileName.indexOf("=") + 1))
                + XML_SUFFIX;
    }

    /**
     * Backs up adobe indesign and xmp file
     * 
     * @param p_conDir
     * @param p_xmlFileName
     * @return
     */
    private Map backupInddFile(String p_conDir, String p_xmlFileName)
    {
        Map fileMap = new HashMap();
        String fileNameWithoutSuffix = FileUtils.getPrefix(p_xmlFileName);
        String oriInddFileName = p_conDir + File.separator
                + fileNameWithoutSuffix + m_inDesignFileSuffix;
        String oriXmpFileName = p_conDir + File.separator
                + fileNameWithoutSuffix + XMP_SUFFIX;
        String currentTime = String.valueOf(System.currentTimeMillis());
        String targetInddName = currentTime + "_" + fileNameWithoutSuffix
                + m_inDesignFileSuffix;
        String targetXmpName = currentTime + "_" + fileNameWithoutSuffix
                + XMP_SUFFIX;
        String targetXmlName = currentTime + "_" + p_xmlFileName;

        // back up indd file
        FileCopier.copyFile(new File(oriInddFileName), new File(p_conDir),
                targetInddName);
        // back up xml file
        FileCopier.copyFile(new File(oriXmpFileName), new File(p_conDir),
                targetXmpName);

        fileMap.put(m_inDesignFileSuffix, targetInddName);
        fileMap.put(XMP_SUFFIX, targetXmpName);
        fileMap.put(XML_SUFFIX, targetXmlName);

        return fileMap;
    }

    /**
     * Reads the pdf file converted from adobe indd to GlobalSight file storage,
     * and be ready to output
     * 
     * @param p_xmlFileName
     * @param p_targetPageName
     * @return
     */
    private File readTargetPdfFile(String p_xmlFileName, String p_targetPageName)
    {
        String statusFileName = FileUtils.getPrefix(p_xmlFileName)
                + COMMAND_STATUS_SUFFIX;
        File statusFile = new File(statusFileName);
        String status = null;
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
            if (status != null)
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
                post_pdfFile = new File(temp2.append(
                        p_targetPageName.substring(p_targetPageName
                                .lastIndexOf(splitChar), p_targetPageName
                                .lastIndexOf("."))).append(PDF_SUFFIX)
                        .toString());
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
    private static String statusInfo(File p_file)
    {
        BufferedReader br = null;
        String errorLine = null;
        try
        {
            br = new BufferedReader(new FileReader(p_file));
            errorLine = br.readLine();
            errorLine = errorLine.substring(6); // error=
            int error = Integer.parseInt(errorLine);

            return error == 0 ? null : errorLine;
        }
        catch (NumberFormatException nfe)
        {
            return errorLine;
        }
        catch (Exception e)
        {
            if (CATEGORY.isEnabledFor(Priority.WARN))
                CATEGORY.warn("Cannot read status info", e);
            return e.getMessage();
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

        EventFlow eventFlow = new EventFlow(sourcePage.getRequest()
                .getEventFlowXml());
        String formatType = eventFlow.getDiplomatAttribute("formatType")
                .getValue().trim();
        String displayNameLower = eventFlow.getDisplayName().toLowerCase();
        
        m_conversionType = displayNameLower.endsWith("indd") ? ADOBE_INDD
                : ADOBE_INX;
        m_inDesignFileSuffix = displayNameLower.endsWith("indd") ? INDD_SUFFIX
                : INX_SUFFIX;
        
        if ("indd_cs4".equals(formatType))
        {
            m_versionType = ADOBE_CS4;
        }
        else if ("indd_cs5".equals(formatType))
        {
            m_versionType = ADOBE_CS5;
        }
        else
        {
            m_versionType = (formatType.equals("indd") || formatType.equals("inx")) ? ADOBE_CS2
                    : ADOBE_CS3;
        }
        
        String inddHiddenTranslated = eventFlow.getInddHiddenTranslated();
        if (inddHiddenTranslated != null && !"".equals(inddHiddenTranslated))
        {
            m_translateHiddenLayer = "true".equals(inddHiddenTranslated);
        }
        else
        {
            m_translateHiddenLayer = true;
        }
        
        String masterTranslated = eventFlow.getMasterTranslated();
        if (masterTranslated != null && !"".equals(masterTranslated))
        {
            m_masterTranslated = "true".equals(masterTranslated);
        }
    }

    /**
     * Returns the convert directory
     * 
     * @return
     */
    private String getConvertDir()
    {
        StringBuffer convDir = null;
        if (m_versionType == ADOBE_CS2)
        {
            // These are adobe InDesign cs2 files, we will use cs2 converter
            // to process them.
            convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR));
        }
        else if (m_versionType == ADOBE_CS4)
        {
            // These are adobe InDesign cs4 files, we will use cs4 converter
            // to process them.
            convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS4));
        }
        else if (m_versionType == ADOBE_CS5)
        {
            convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5));
        }
        else
        {
            // These (formatType are "indd_cs3" and "inx_cs3") are adobe
            // InDesign cs3 files, we will use cs3 converter to process them.
            convDir = new StringBuffer(m_sc.getStringParameter(
                            SystemConfigParamNames.ADOBE_CONV_DIR_CS3));
        }
        convDir.append(File.separator);
        convDir.append(m_conversionType);
        convDir.append(File.separator);
        return convDir.toString();
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
            int index = Math.max(p_tarFileName.indexOf("/"), p_tarFileName
                    .indexOf("\\"));
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
                    if (tpg.getSourcePage().getExternalPageId().equals(
                            sourcePageName))
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
     * Writes command file for adobe converter
     * 
     * @param p_xmlFileName
     */
    private void writeCommandFile(String p_xmlFileName)
    {
        String commandFileName = getCommandFileName(p_xmlFileName);
        StringBuffer text = new StringBuffer();
        text.append("ConvertFrom=xml").append("\r\n");
        text.append("ConvertTo=").append(m_conversionType).append("\r\n");
        text.append("AcceptChanges=true").append("\r\n");
        text.append("MasterTranslated=").append(m_masterTranslated).append("\r\n");
        text.append("TranslateHiddenLayer=").append(m_translateHiddenLayer).append("\r\n");
        FileWriter commandFile;

        try
        {
            commandFile = new FileWriter(commandFileName);
            commandFile.write(text.toString());
            commandFile.close();
        }
        catch (IOException e)
        {
            throw new EnvoyServletException(e);
        }
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
    private void writeOutFile(File p_pdfFile, HttpServletResponse p_response,
            String p_action)
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

    /**
     * Returns the previewed pdf file
     * 
     * @param p_request
     * @return
     */
    private File getPreviewPdf(HttpServletRequest p_request)
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

    public static void deleteOldPdf(long p_targetPageId, long p_targetLocaleId)
    {
        try
        {
            TargetPage tPage = ServerProxy.getPageManager().getTargetPage(
                    p_targetPageId);
            String company_id = tPage.getSourcePage().getCompanyId();
            String filename = tPage.getSourcePage().getExternalPageId();
            String fileSuffix = filename.substring(filename.lastIndexOf("."));
            if (INDD_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || INX_SUFFIX.equalsIgnoreCase(fileSuffix))
            {
                String targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                String targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                String targetPdfFileName = FileUtils.getPrefix(targetFileName)
                        + PDF_SUFFIX;
                String fullTargetPdfFileName = AmbFileStoragePathUtils
                        .getPdfPreviewDir(company_id)
                        + File.separator + targetPdfFileName;
                FileUtils.deleteSilently(fullTargetPdfFileName);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not get the target page.");
            throw new EnvoyServletException(e);
        }
    }
}

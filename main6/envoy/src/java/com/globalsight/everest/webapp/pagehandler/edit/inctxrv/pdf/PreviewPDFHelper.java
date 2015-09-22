/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.adobe.AdobeConfiguration;
import com.globalsight.cxe.adapter.adobe.AdobeHelper;
import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlConverter;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.msoffice.PptxFileManager;
import com.globalsight.cxe.adapter.quarkframe.FrameHelper;
import com.globalsight.cxe.engine.eventflow.DiplomatAttribute;
import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.Da;
import com.globalsight.cxe.util.fileImport.eventFlow.Dv;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDetailsPDFsBO;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
import com.globalsight.ling.docproc.merger.fm.FontMappingHelper;
import com.globalsight.ling.docproc.merger.fm.IDMLFontMappingHelper;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlTransformer;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.util.system.ConfigException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PreviewPDFHelper implements PreviewPDFConstants
{
    private static final Logger LOGGER = Logger
            .getLogger(PreviewPDFHelper.class);
    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();
    // The Map for storing PDF future, the key is TargetPageID, the value is PDF
    // Future.
    private static final Map<String, Future<File>> createPDFMap = new ConcurrentHashMap<String, Future<File>>();
    private static final ExecutorService serviceForINDD = Executors
            .newSingleThreadExecutor();
    private static final ExecutorService serviceForIDML = Executors
            .newSingleThreadExecutor();
    private static final ExecutorService serviceForDOCX = Executors
            .newSingleThreadExecutor();
    private static final ExecutorService serviceForPPTX = Executors
            .newSingleThreadExecutor();
    private static final ExecutorService serviceForXLSX = Executors
            .newSingleThreadExecutor();
    private static final ExecutorService serviceForXML = Executors
            .newSingleThreadExecutor();

    private static final int BUFFERSIZE = 4096;

    private static final String[] PROPERTY_FILES = {
            "/properties/Logger.properties",
            "/properties/AdobeAdapter.properties" };

    // Cancel Creating PDF Threads.
    public void cancelPDF(Set<Long> p_workflowIdSet, String p_userId)
            throws WorkflowManagerException, RemoteException
    {
        for (long workflowId : p_workflowIdSet)
        {
            Workflow workflow = ServerProxy.getWorkflowManager()
                    .getWorkflowById(workflowId);
            for (TargetPage tp : workflow.getTargetPages())
            {
                String key = getKey(true, tp, p_userId);
                Future<File> future = createPDFMap.get(key);
                if (future != null)
                {
                    future.cancel(true);
                    createPDFMap.remove(key);
                }
            }
        }
    }

    // Create PDF file for Target Page
    public File createPDF(long p_pageId, String p_userId, boolean isTarget)
    {
        try
        {
            Page page = null;
            if (isTarget)
            {
                TargetPage targetPage = ServerProxy.getPageManager()
                        .getTargetPage(p_pageId);

                page = targetPage;
            }
            else
            {
                SourcePage sourcePage = ServerProxy.getPageManager()
                        .getSourcePage(p_pageId);

                page = sourcePage;
            }

            String key = getKey(isTarget, page, p_userId);
            Future<File> future = createPDFMap.get(key);
            if (future == null)
            {
                createPDF(page, p_userId, isTarget);
                future = createPDFMap.get(key);
            }

            // Create PDF again, if no result.
            File pdfFile = future.get();
            if (pdfFile == null || !pdfFile.exists())
            {
                future.cancel(true);
                createPDFMap.remove(key);
                createPDF(page, p_userId, isTarget);
                future = createPDFMap.get(key);
            }

            return future.get();
        }
        catch (Exception e)
        {
            String msg = "Getting PDF File Error, with target page id:"
                    + p_pageId;
            msg += ", and by userId:" + p_userId;
            LOGGER.error(msg, e);
        }

        return null;
    }

    // Create PDF file for Target Page
    private void createPDF(Page p_page, String p_userId, boolean isTarget)
            throws WorkflowManagerException, RemoteException
    {
        // Cancel Duplicate Request
        String key = getKey(isTarget, p_page, p_userId);
        if (createPDFMap.get(key) != null)
            return;

        Callable<File> task;
        Future<File> future = null;
        String externalPageId = p_page.getExternalPageId().toLowerCase();
        if (externalPageId.endsWith(INDD_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, ADOBE_CS5_5, isTarget);
            future = serviceForINDD.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(IDML_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, ADOBE_TYPE_IDML,
                    isTarget);
            future = serviceForIDML.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(DOCX_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, TYPE_OFFICE_DOCX,
                    isTarget);
            future = serviceForDOCX.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(PPTX_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, TYPE_OFFICE_PPTX,
                    isTarget);
            future = serviceForPPTX.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(XLSX_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, TYPE_OFFICE_XLSX,
                    isTarget);
            future = serviceForXLSX.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(XML_SUFFIX))
        {
            task = new CreatePDFTask(p_page, p_userId, TYPE_XML, isTarget);
            future = serviceForXML.submit(task);
            createPDFMap.put(key, future);
        }
    }

    /**
     * Set data for PDF Business Object
     * 
     * @param p_wf
     *            Workflow Data
     * @param p_pdfBO
     *            PDF Business Object
     * @param p_userId
     *            User ID
     */
    public void setJobDetailsPDFsBO(Workflow p_wf, JobDetailsPDFsBO p_pdfBO,
            String p_userId)
    {
        long totalPDFFileNumber = 0, existPDFFileNumber = 0;
        long companyId = p_wf.getCompanyId();
        for (TargetPage tp : p_wf.getTargetPages())
        {
            // Set Status
            String key = getKey(true, tp, p_userId);
            Future<File> future = createPDFMap.get(key);
            if (future != null)
            {
                if (future.isDone())
                    p_pdfBO.setStatus(JobDetailsPDFsBO.STATUS_DONE);
                else
                    p_pdfBO.setStatus(JobDetailsPDFsBO.STATUS_IN_PROGRESS);
            }

            // Get existPDFFileNumber & totalPDFFileNumber
            String tpPath = getPagePath(tp, true);
            String extension = tpPath.substring(tpPath.lastIndexOf("."));

            totalPDFFileNumber++;
            File pdfFile = getPreviewPdf(tpPath, companyId, p_userId);
            if (pdfFile.exists())
                existPDFFileNumber++;
        }

        p_pdfBO.setTotalPDFFileNumber(totalPDFFileNumber);
        p_pdfBO.setExistPDFFileNumber(existPDFFileNumber);
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
        PdfCopy writer = new PdfCopy(document,
                new FileOutputStream(outPutFile));
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
            if (!e.getMessage()
                    .equals("PdfReader not opened with owner password"))
            {
                throw new EnvoyServletException(e);
            }
            else
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return new File(outPutFile);
    }

    public static boolean isInContextReviewEnabled()
    {
        boolean enabled = false;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            enabled = "true".equals(
                    sc.getStringParameter(SystemConfigParamNames.INCTXRV_ENABLE,
                            CompanyWrapper.SUPER_COMPANY_ID));
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isInDesignEnabled()
    {
        boolean enabled = false;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            enabled = "true".equals(
                    sc.getStringParameter(SystemConfigParamNames.INCTXRV_ENABLE,
                            CompanyWrapper.SUPER_COMPANY_ID));

            if (enabled)
            {
                String dir = AmbFileStoragePathUtils
                        .getInContextReviewInDesignPath();

                enabled = !StringUtil.isEmpty(dir);
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isOfficeEnabled()
    {
        boolean enabled = false;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            enabled = "true".equals(
                    sc.getStringParameter(SystemConfigParamNames.INCTXRV_ENABLE,
                            CompanyWrapper.SUPER_COMPANY_ID));

            if (enabled)
            {
                String dir = AmbFileStoragePathUtils
                        .getInContextReviewOfficePath();

                enabled = !StringUtil.isEmpty(dir);
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isXMLEnabled()
    {
        boolean enabled = false;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            enabled = "true".equals(
                    sc.getStringParameter(SystemConfigParamNames.INCTXRV_ENABLE,
                            CompanyWrapper.SUPER_COMPANY_ID));
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isInDesignEnabled(String companyId)
    {
        boolean enabled = false;

        try
        {
            if (isInDesignEnabled())
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();

                enabled = "true".equals(sc.getStringParameter(
                        SystemConfigParamNames.INCTXRV_ENABLE_INDD, companyId));
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isOfficeEnabled(String companyId)
    {
        boolean enabled = false;

        try
        {
            if (isOfficeEnabled())
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();

                enabled = "true".equals(sc.getStringParameter(
                        SystemConfigParamNames.INCTXRV_ENABLE_OFFICE,
                        companyId));
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isXMLEnabled(String companyId)
    {
        boolean enabled = false;

        try
        {
            if (isXMLEnabled())
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();

                enabled = "true".equals(sc.getStringParameter(
                        SystemConfigParamNames.INCTXRV_ENABLE_XML, companyId));
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        return enabled;
    }

    public static boolean isINDDAndInx(PreviewPDFBO p_params)
    {
        int fileVersionType = p_params.getVersionType();
        if (ADOBE_CS2 == fileVersionType || ADOBE_CS3 == fileVersionType
                || ADOBE_CS4 == fileVersionType || ADOBE_CS5 == fileVersionType
                || ADOBE_CS5_5 == fileVersionType
                || ADOBE_CS6 == fileVersionType)
            return true;

        return false;
    }

    File createPDF4INDDAndInx(Page p_page, String p_userId, boolean isTarget)
            throws JobException, RemoteException, GeneralException,
            NamingException
    {
        TargetPage tp = isTarget ? (TargetPage) p_page : null;
        SourcePage sp = isTarget ? null : (SourcePage) p_page;
        long spId = isTarget ? tp.getSourcePage().getId() : sp.getId();

        String pagePath = getPagePath(p_page, isTarget);
        long pageId = p_page.getId();
        long companyId = isTarget ? tp.getCompanyId() : sp.getCompanyId();
        Job job = isTarget ? tp.getWorkflowInstance().getJob()
                : ServerProxy.getJobHandler().getJobById(sp.getJobId());
        File pdfFile = getPreviewPdf(pagePath, companyId, null);
        File oldPdfFile = getOldPreviewPdf(pagePath, companyId);
        PreviewPDFBO params = determineConversionParameters(spId);

        return createPDF4INDDAndInx(pagePath, pdfFile, oldPdfFile, job,
                p_userId, companyId, p_page, params, isTarget);
    }

    /**
     * Create PDF file for Adobe Indd & Inx File.
     * 
     * @param p_trgPageName
     *            Target page path, such as
     *            "fr_FR\webservice\70\Data\temp\INDD\JoeyTest_CS55.indd".
     * @param p_pdfFile
     *            preview pdf file
     * @param p_oldPdfFile
     *            old pdf file
     * @param p_job
     * @param p_userId
     * @param p_companyId
     * @param p_trgPageId
     * @param p_params
     *            parameters for creating preview pdf.
     * @return
     */
    private File createPDF4INDDAndInx(String p_pageName, File p_pdfFile,
            File p_oldPdfFile, Job p_job, String p_userId, long p_companyId,
            Page p_page, PreviewPDFBO p_params, boolean isTarget)
    {
        String converterDir = null;
        Map<String, String> newFileNameMap = new HashMap<String, String>();
        String p_fileSuffix = p_params.getFileSuffix();
        try
        {
            TargetPage tp = isTarget ? (TargetPage) p_page : null;
            SourcePage sp = isTarget ? null : (SourcePage) p_page;
            long spId = isTarget ? tp.getSourcePage().getId() : sp.getId();
            long pageId = p_page.getId();

            String trgLocale = isTarget ? tp.getGlobalSightLocale().toString()
                    : sp.getGlobalSightLocale().toString();
            converterDir = getConvertDir(p_params, true, p_companyId)
                    + trgLocale;
            new File(converterDir).mkdirs();

            // test converter is started or not
            String testFile = "indd" + (int) (Math.random() * 1000000)
                    + ".test";
            File tFile = new File(converterDir + "/" + testFile);
            FileUtil.writeFile(tFile, "test converter is start or not");

            String targetPageFolder = p_pdfFile.getParent();
            String statusPageFolder = p_oldPdfFile.getParent();
            File f = new File(targetPageFolder);
            if (!f.exists())
            {
                f.mkdirs();
                copyFilesToNewTargetLocale(p_job, trgLocale, p_pageName,
                        targetPageFolder, statusPageFolder, p_params,
                        p_companyId);
            }
            File previewDir = new File(AmbFileStoragePathUtils
                    .getPdfPreviewDir(p_companyId).getAbsolutePath() + "");
            String oriXmlFileName = getConvertedFileName(previewDir, p_pageName,
                    trgLocale, p_job.getL10nProfile().getTargetLocales());

            // Upload indd and xmp file to converter.
            backupInddFile(newFileNameMap,
                    getConvertDir(p_params, false, p_companyId),
                    getConvertDir(p_params, true, p_companyId), trgLocale,
                    oriXmlFileName, p_fileSuffix, p_job);
            // Upload XML file to converter
            String xmlFilePath = converterDir + File.separator
                    + (String) newFileNameMap.get(XML_SUFFIX);
            writeXMLFileToConvertDir(xmlFilePath, trgLocale, pageId, p_params,
                    p_userId, isTarget);
            // Upload command file to converter
            writeCommandFile(xmlFilePath, p_params);

            // Wait for Adobe Converter to convert
            return readTargetPdfFile(xmlFilePath, p_pageName, trgLocale,
                    p_userId, p_companyId, isTarget, tFile);
        }
        catch (InterruptedException e)
        {
        }
        catch (ConfigException ce)
        {
            LOGGER.error(
                    "Please set the correct InDesign conversion dir for In Context Review Tool.");
        }
        catch (Exception e)
        {
            LOGGER.error("CreatePDFForINDD Error: ", e);
        }
        finally
        {
            // 6 delete back up indd and xmp file
            try
            {
                if (!newFileNameMap.isEmpty())
                {
                    FileUtils.deleteSilently(converterDir + File.separator
                            + (String) newFileNameMap.get(p_fileSuffix));
                    FileUtils.deleteSilently(converterDir + File.separator
                            + (String) newFileNameMap.get(XMP_SUFFIX));
                }
            }
            catch (Exception fex)
            {
            }
        }

        return null;
    }

    File createPDF4IDML(Page p_page, String p_userId, boolean isTarget)
    {
        TargetPage tp = isTarget ? (TargetPage) p_page : null;
        SourcePage sp = isTarget ? null : (SourcePage) p_page;
        long spId = isTarget ? tp.getSourcePage().getId() : sp.getId();

        IdmlConverter converter = new IdmlConverter();
        converter.setIsInContextReivew(true);
        String trgPagePath = getPagePath(p_page, isTarget);
        long pageid = p_page.getId();
        long companyId = isTarget ? tp.getCompanyId() : sp.getCompanyId();
        converter.setCompanyId(companyId);
        String locale = p_page.getGlobalSightLocale().toString();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, null);
        PreviewPDFBO params = determineConversionParameters(spId);

        try
        {
            String converterDir = getConvertDir(params, true, companyId)
                    + File.separator + locale;
            new File(converterDir).mkdirs();

            String xmlFilePath = converterDir + File.separator
                    + params.getRelSafeName();
            File zipDir = getZipDir(new File(xmlFilePath),
                    params.getSafeBaseFileName());
            // write xml file
            writeXMLFileToConvertDir(xmlFilePath, locale, pageid, params,
                    p_userId, isTarget);
            IdmlHelper.split(zipDir.getAbsolutePath());
            IDMLFontMappingHelper idmlFontMappinghelper = new IDMLFontMappingHelper();
            idmlFontMappinghelper.processIDMLFont(zipDir.getAbsolutePath(),
                    locale);
            String idmlPath = converter.convertXmlToIdml(
                    params.getSafeBaseFileName(), zipDir.getAbsolutePath());
            IDMLFontMappingHelper.restoreIDMLFont(zipDir.getAbsolutePath());
            converter.convertToPdf(new File(idmlPath), pdfFile, locale);
        }
        catch (InterruptedException e)
        {
        }
        catch (ConfigException ce)
        {
            LOGGER.error(
                    "Please set the correct InDesign conversion dir for In Context Review Tool.");
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(trgPagePath).append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }

        return pdfFile;
    }

    File createPDF4XML(Page p_page, String p_userId, boolean isTarget)
    {
        TargetPage tp = isTarget ? (TargetPage) p_page : null;
        SourcePage sp = isTarget ? null : (SourcePage) p_page;
        long spId = isTarget ? tp.getSourcePage().getId() : sp.getId();

        String trgPagePath = getPagePath(p_page, isTarget);
        long pageid = p_page.getId();
        long companyId = isTarget ? tp.getCompanyId() : sp.getCompanyId();
        String locale = p_page.getGlobalSightLocale().toString();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, null);
        PreviewPDFBO params = determineConversionParameters(spId);
        OutputStream out = null;

        try
        {
            File tempDir = AmbFileStoragePathUtils.getTempFileDir();
            Job job = isTarget ? tp.getWorkflowInstance().getJob()
                    : ServerProxy.getJobHandler().getJobById(sp.getJobId());
            SourcePage sourcepage = isTarget ? tp.getSourcePage() : sp;
            long dataSourceId = sourcepage.getRequest().getDataSourceId();
            FileProfile fileProfile = ServerProxy
                    .getFileProfilePersistenceManager()
                    .readFileProfile(dataSourceId);

            File xmlFile = File.createTempFile("GS-xml", ".xml", tempDir);
            String xmlFilePath = xmlFile.getAbsolutePath();
            // write xml file
            writeXMLFileToConvertDir(xmlFilePath, locale, pageid, params,
                    p_userId, isTarget);

            if (FileProfileUtil.isXmlPreviewPDF(fileProfile))
            {
                File jarFile = new File(this.getClass().getProtectionDomain()
                        .getCodeSource().getLocation().getFile());
                String fopRoot = jarFile.getParent()
                        + "/globalsight-web.war/WEB-INF";
                final FopFactory fopFactory = FopFactory
                        .newInstance(new File(fopRoot).toURI());

                FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
                if (!pdfFile.exists())
                {
                    pdfFile.getParentFile().mkdirs();
                    pdfFile.createNewFile();
                }
                FileOutputStream fileOut = new FileOutputStream(pdfFile);
                out = new BufferedOutputStream(fileOut);

                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent,
                        out);

                File xslFile = FileProfileUtil.getXsl(fileProfile);

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory
                        .newTransformer(new StreamSource(xslFile));
                transformer.setParameter("versionParam", "2.0");

                Source src = new StreamSource(xmlFile);
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.transform(src, res);
            }
            else
            {
                throw new Exception(
                        "Use XML-FO xsl file for In Context Review!");
            }
        }
        catch (InterruptedException e)
        {
        }
        catch (ConfigException ce)
        {
            LOGGER.error(
                    "Please set the correct InDesign conversion dir for In Context Review Tool.");
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(trgPagePath).append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (Exception ex)
            {
                // ignore;
            }
        }

        return pdfFile;
    }

    File createPDF4Office(Page p_page, String p_userId, boolean isTarget)
    {
        TargetPage tp = isTarget ? (TargetPage) p_page : null;
        SourcePage sp = isTarget ? null : (SourcePage) p_page;
        long spId = isTarget ? tp.getSourcePage().getId() : sp.getId();

        OfficeXmlConverter oxc = new OfficeXmlConverter();
        String trgPagePath = getPagePath(p_page, isTarget);
        if (trgPagePath.startsWith("("))
        {
            int index = trgPagePath.indexOf(") ");

            if (index != -1)
            {
                trgPagePath = trgPagePath.substring(index + 2);
            }
        }

        long pageid = p_page.getId();
        long companyId = isTarget ? tp.getCompanyId() : sp.getCompanyId();
        oxc.setCompanyId(companyId);
        String locale = p_page.getGlobalSightLocale().toString();
        String srcLocale = isTarget
                ? tp.getSourcePage().getGlobalSightLocale().toString()
                : sp.getGlobalSightLocale().toString();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, null);
        PreviewPDFBO params = determineConversionParameters(spId);
        File zipDir = null;
        File officeFile = null;

        try
        {
            String converterDir = getConvertDir(params, true, companyId)
                    + File.separator + locale;
            new File(converterDir).mkdirs();
            String xmlFilePath = converterDir + File.separator
                    + params.getRelSafeName();
            zipDir = getZipDir(new File(xmlFilePath),
                    params.getSafeBaseFileName());
            if (!zipDir.exists())
            {
                zipDir.mkdirs();
            }

            String o2010path = AmbFileStoragePathUtils
                    .getOffice2010ConversionPath();
            String o2010DirSrc = o2010path + File.separator + srcLocale
                    + File.separator + zipDir.getName();
            String o2010Dir = o2010path + File.separator + locale
                    + File.separator + zipDir.getName();
            File f_o2010DirSrc = new File(o2010DirSrc);
            File f_o2010Dir = new File(o2010Dir);
            File f_src = f_o2010Dir;

            if (!f_o2010Dir.exists())
            {
                f_src = f_o2010DirSrc;
            }

            // copy all files to converter dir
            FileCopier.copyDir(f_src, zipDir.getPath());

            // write xml file
            writeXMLFileToConvertDir(xmlFilePath, locale, pageid, params,
                    p_userId, isTarget);

            if ("pptx".equals(params.getFileType()))
            {
                PptxFileManager pptxFileManager = new PptxFileManager();
                MSOffice2010Filter msf = new MSOffice2010Filter();

                pptxFileManager.setFilter(msf);
                pptxFileManager.splitFile(zipDir.getPath());
            }

            String officePath = oxc.convertXmlToOffice(
                    params.getSafeBaseFileName(), zipDir.getAbsolutePath());
            officeFile = new File(officePath);
            oxc.convertToPdf(params.getFileType(), officeFile, pdfFile, locale);
        }
        catch (ConfigException ce)
        {
            LOGGER.error(
                    "Please set the correct Office conversion dir for In Context Review Tool.");
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(trgPagePath).append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }
        finally
        {
            if (zipDir != null && zipDir.exists())
            {
                try
                {
                    FileUtil.deleteFile(zipDir);
                }
                catch (Exception e2)
                {
                    // ignore
                }
            }

            if (officeFile != null && officeFile.exists())
            {
                try
                {
                    FileUtil.deleteFile(officeFile);
                }
                catch (Exception e2)
                {
                    // ignore
                }
            }
        }

        return pdfFile;
    }

    /**
     * Returns the convert directory
     * 
     * @return
     * @throws Exception
     */
    public String getConvertDir(PreviewPDFBO p_params,
            boolean isIncontextReview, long companyId) throws Exception
    {
        int fileVersionType = p_params.getVersionType();
        if (fileVersionType == ADOBE_TYPE_IDML)
        {
            return IdmlHelper.getConversionDir();
        }
        else if (fileVersionType == TYPE_OFFICE_DOCX
                || fileVersionType == TYPE_OFFICE_PPTX
                || fileVersionType == TYPE_OFFICE_XLSX)
        {
            StringBuffer convDir = null;
            convDir = new StringBuffer(
                    AmbFileStoragePathUtils.getInContextReviewOfficePath());

            convDir.append(File.separator);

            switch (fileVersionType)
            {
                case TYPE_OFFICE_DOCX:
                    convDir.append("word");
                    break;

                case TYPE_OFFICE_PPTX:
                    convDir.append("powerpoint");
                    break;

                case TYPE_OFFICE_XLSX:
                    convDir.append("excel");
                    break;
            }
            convDir.append(File.separator);

            return convDir.toString();
        }
        else if (isIncontextReview)
        {
            StringBuffer convDir = null;
            convDir = new StringBuffer(
                    AmbFileStoragePathUtils.getInContextReviewInDesignPath());

            convDir.append(File.separator);
            convDir.append(p_params.getFileType());
            convDir.append(File.separator);

            return convDir.toString();
        }
        else
        {
            StringBuffer convDir = null;
            if (fileVersionType == ADOBE_CS2)
            {
                // These are adobe InDesign cs2 files, we will use cs2 converter
                // to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR));
            }
            else if (fileVersionType == ADOBE_CS4)
            {
                // These are adobe InDesign cs4 files, we will use cs4 converter
                // to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS4));
            }
            else if (fileVersionType == ADOBE_CS5)
            {
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5));
            }
            else if (fileVersionType == ADOBE_CS5_5)
            {
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5));
            }
            else if (fileVersionType == ADOBE_CS6)
            {
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS6));
            }
            else
            {
                // These (formatType are "indd_cs3" and "inx_cs3") are adobe
                // InDesign cs3 files,
                // we will use cs3 converter to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS3));
            }

            convDir.append(File.separator);
            convDir.append(p_params.getFileType());
            convDir.append(File.separator);

            return convDir.toString();
        }
    }

    /**
     * Copies files to the folder of newly added target locale.
     * 
     * @param p_request
     * @param p_trgPageFolder
     * @param p_trgPageName
     * @throws Exception
     */
    private void copyFilesToNewTargetLocale(Job p_job, String p_trgLocale,
            String p_trgPageName, String p_trgPageFolder, String p_statusFolder,
            PreviewPDFBO p_params, long p_companyId) throws Exception
    {
        String existedLocale = null;
        String tarName = null;
        String prefix = FileUtils.getPrefix(p_trgPageName);
        File previewDir = new File(AmbFileStoragePathUtils
                .getPdfPreviewDir(p_companyId).getAbsolutePath());
        File previewDir_inctx = new File(AmbFileStoragePathUtils
                .getPdfPreviewDir(p_companyId).getAbsolutePath() + "_inctx");
        String srcLocale = p_job.getSourceLocale().toString();
        GlobalSightLocale[] allTrgLocales = p_job.getL10nProfile()
                .getTargetLocales();
        for (GlobalSightLocale globalSightLocale : allTrgLocales)
        {
            existedLocale = globalSightLocale.toString();

            String existedFolder = prefix.replaceFirst(p_trgLocale,
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
        if (existedLocale.equalsIgnoreCase(p_trgLocale))
        {
            return;
        }

        String existedTpFolder = p_statusFolder.replaceFirst(p_trgLocale,
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
                    FileCopier.copy(f, p_trgPageFolder);
            }
        }

        copyAdobeFiles(
                srcLocale, p_trgLocale, getConvertedFileName(previewDir,
                        p_trgPageName, p_trgLocale, allTrgLocales),
                p_params, p_companyId);
    }

    /**
     * Gets the converted file name, by parsing the STATUS file.
     * 
     * @param p_targetPageName
     * @return
     * @throws IOException
     */
    private String getConvertedFileName(File p_previewDir,
            String p_targetPageName, String targetLocale,
            GlobalSightLocale[] p_allTrgLocales) throws IOException
    {
        String tarName = null;
        String prefix = FileUtils.getPrefix(p_targetPageName);
        for (GlobalSightLocale globalSightLocale : p_allTrgLocales)
        {
            String existedLocale = globalSightLocale.toString();
            String existedFolder = prefix.replaceFirst(targetLocale,
                    existedLocale);
            tarName = p_previewDir + File.separator + existedFolder
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
            tarName = p_previewDir + File.separator + prefix + STATUS_SUFFIX;

            // CATEGORY.error(tarName + " does not exist");
            throw new EnvoyServletException(
                    EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                    tarName + " does not exist");
        }

        BufferedReader br = null;
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
        finally
        {
            if (br != null)
                br.close();
        }

        return FileUtils
                .getPrefix(convertedFileName
                        .substring(convertedFileName.indexOf("=") + 1))
                + XML_SUFFIX;
    }

    private void copyAdobeFiles(String p_existedLocale, String p_trgLocale,
            String p_convertedFileName, PreviewPDFBO p_params, long companyId)
                    throws Exception
    {
        String conDir = getConvertDir(p_params, false, companyId);
        String conDir_inctxrv = getConvertDir(p_params, true, companyId);
        
        // ignore if same dir
        if (conDir.equals(conDir_inctxrv))
        {
            return;
        }
        
        String fileName = FileUtils.getPrefix(p_convertedFileName);

        StringBuffer inddFile = new StringBuffer(conDir);
        inddFile.append(p_existedLocale).append(File.separator).append(fileName)
                .append(p_params.getFileSuffix());
        StringBuffer xmpFile = new StringBuffer(conDir);
        xmpFile.append(p_existedLocale).append(File.separator).append(fileName)
                .append(XMP_SUFFIX);
        StringBuffer pdfFile = new StringBuffer(conDir);
        pdfFile.append(p_existedLocale).append(File.separator).append(fileName)
                .append(PDF_SUFFIX);

        FileCopier.copy(new File(inddFile.toString()),
                conDir_inctxrv + p_trgLocale);
        FileCopier.copy(new File(xmpFile.toString()),
                conDir_inctxrv + p_trgLocale);
        FileCopier.copy(new File(pdfFile.toString()),
                conDir_inctxrv + p_trgLocale);
    }

    /**
     * Backs up adobe indesign and xmp file
     * 
     * @param p_conDir
     * @param p_xmlFileName
     */
    private void backupInddFile(Map<String, String> fileMap, String p_oriConDir,
            String p_conDir, String targetLocale, String p_xmlFileName,
            String p_fileSuffix, Job p_job)
    {
        String fileNameWithoutSuffix = FileUtils.getPrefix(p_xmlFileName);
        String currentTime = String.valueOf(System.currentTimeMillis());
        String targetInddName = currentTime + "_" + fileNameWithoutSuffix
                + p_fileSuffix;
        String targetXmpName = currentTime + "_" + fileNameWithoutSuffix
                + XMP_SUFFIX;
        String targetXmlName = currentTime + "_" + p_xmlFileName;

        L10nProfile lp = p_job.getL10nProfile();
        File oriInddFile = null;
        File oriXmpFile = null;

        // get from target locale first
        String oriInddFileName = p_oriConDir + targetLocale + File.separator
                + fileNameWithoutSuffix + p_fileSuffix;
        String oriXmpFileName = p_oriConDir + targetLocale + File.separator
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
            oriInddFileName = p_oriConDir + srcLocale + File.separator
                    + fileNameWithoutSuffix + p_fileSuffix;
            oriXmpFileName = p_oriConDir + srcLocale + File.separator
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

                oriInddFileName = p_oriConDir + existedLocale + File.separator
                        + fileNameWithoutSuffix + p_fileSuffix;
                oriXmpFileName = p_oriConDir + existedLocale + File.separator
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
            oriInddFileName = p_oriConDir + targetLocale + File.separator
                    + fileNameWithoutSuffix + p_fileSuffix;
            LOGGER.error("Cannot find original indd and xmp file: "
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

        fileMap.put(p_fileSuffix, targetInddName);
        fileMap.put(XMP_SUFFIX, targetXmpName);
        fileMap.put(XML_SUFFIX, targetXmlName);
    }

    /**
     * Writes xml file to convert directory
     * 
     * @param p_request
     */
    private void writeXMLFileToConvertDir(String p_xmlFilePath,
            String p_trgLocale, long p_trgPageId, PreviewPDFBO p_params,
            String uid, boolean isTarget) throws Exception
    {
        ExportHelper ex = new ExportHelper();
        ex.setUserId(uid);
        try
        {
            if (p_trgPageId == 0)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "No target page found");
            }
            String xml = ex.exportForPdfPreview(p_trgPageId, "UTF-8", false,
                    true, isTarget);
            // GBS-2955
            if (isINDDAndInx(p_params))
            {
                xml = AdobeHelper.recoverLineBreak(xml);
            }

            String processed = xml;
            // idml
            if (p_params.getVersionType() == ADOBE_TYPE_IDML)
            {
                processed = xml;
            }
            else if (p_params.getVersionType() == TYPE_OFFICE_DOCX
                    || p_params.getVersionType() == TYPE_OFFICE_PPTX
                    || p_params.getVersionType() == TYPE_OFFICE_XLSX)
            {
                processed = xml;
            }
            // indd inx
            else
            {
                FontMappingHelper fontMappinghelper = new FontMappingHelper();
                processed = fontMappinghelper.processInddXml(p_trgLocale,
                        processed);
            }

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
    private void writeCommandFile(String p_xmlFileName, PreviewPDFBO p_params)
            throws IOException
    {
        String commandFileName = FileUtils.getPrefix(p_xmlFileName)
                + IR_COMMAND_SUFFIX;
        StringBuffer text = new StringBuffer();
        text.append("ConvertFrom=xml").append("\r\n");
        text.append("ConvertTo=indd").append("\r\n");
        text.append("AcceptChanges=true").append("\r\n");
        text.append("MasterTranslated=").append(p_params.isTranslateMaster())
                .append("\r\n");
        text.append("TranslateHiddenLayer=")
                .append(p_params.isTranslateHiddenLayer()).append("\r\n");

        FileUtil.writeFileAtomically(new File(commandFileName), text.toString(),
                "US-ASCII");
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

            return new String[] { errorLine, msg };
        }
        catch (Exception e)
        {
            if (LOGGER.isEnabledFor(Priority.WARN))
                LOGGER.warn("Cannot read status info", e);
            return new String[] { e.getMessage() };
        }
        finally
        {
            FileUtils.closeSilently(br);
        }
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
            String p_targetPageName, String p_trgLocale, String p_userId,
            long p_companyId, boolean isTarget, File testFile) throws Exception
    {
        String statusFileName = FileUtils.getPrefix(p_xmlFileName)
                + COMMAND_STATUS_SUFFIX;
        File statusFile = new File(statusFileName);
        String[] status = null;

        int i = 0;
        File f = new File(statusFileName);
        boolean found = false;
        while (i++ < 10)
        {
            Thread.sleep(2000);
            if (f.exists())
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            if (testFile.exists())
            {
                testFile.delete();

                throw new Exception(
                        "In Context Review converter is not started");
            }
        }

        FileWaiter fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME,
                getMaxWaitTime(), statusFileName);
        StringBuffer tarDir = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(p_companyId).getAbsolutePath() + "_inctx");
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
                LOGGER.error("Adobe convertion failed: "
                        + "Cannot convert to PDF file correctly.");
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            else
            {
                tarDir.append(File.separator).append(p_trgLocale);
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
                StringBuffer folderPath = new StringBuffer(tarDir.toString());
                File pre_pdfFile = new File(
                        folderPath
                                .append(pdfFileName.substring(pdfFileName
                                        .lastIndexOf(File.separator)))
                        .toString());
                folderPath = new StringBuffer(tarDir.toString());
                String basicFileName = tarDisplayName[tarDisplayName.length
                        - 1];
                String ext = basicFileName
                        .substring(basicFileName.lastIndexOf("."))
                        .toLowerCase();
                basicFileName = basicFileName.substring(0,
                        basicFileName.lastIndexOf("."));
                post_pdfFile = new File(
                        folderPath.append(File.separator).append(basicFileName)
                                .append(ext).append(PDF_SUFFIX).toString());
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
     * Returns the previewed pdf file
     * 
     * @param p_request
     * @return
     */
    public static File getPreviewPdf(String p_filePath, long p_companyId,
            String p_userid)
    {
        int index = p_filePath.lastIndexOf(".");
        String ext = p_filePath.substring(index).toLowerCase();
        String pdfPath = p_filePath.substring(0, index) + ext + PDF_SUFFIX;
        StringBuffer pdfFullPath = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(p_companyId).getAbsolutePath() + "_inctx");

        if (StringUtil.isNotEmpty(p_userid))
        {
            pdfFullPath.append(File.separator);
            pdfFullPath.append(p_userid);
        }

        pdfFullPath.append(File.separator);
        pdfFullPath.append(pdfPath);

        File pdfFile = new File(pdfFullPath.toString());
        return pdfFile;
    }

    public static Set<File> getPreviewPdf(Set<Long> workflowIdSet,
            String p_userid)
    {
        Set<File> pdfs = new HashSet<File>();
        try
        {
            for (long wfId : workflowIdSet)
            {
                Workflow wf = ServerProxy.getWorkflowManager()
                        .getWorkflowById(wfId);
                long companyId = wf.getCompanyId();
                for (TargetPage tp : wf.getTargetPages())
                {
                    String trgPagePath = getPagePath(tp, true);
                    File pdf = getPreviewPdf(trgPagePath, companyId, p_userid);
                    if (pdf.exists())
                        pdf = setCopyOnlyPermission(pdf);
                    pdfs.add(pdf);
                }
            }
        }
        catch (Exception e)
        {
            String message = "Get Preview pdf error, with workflowId set:"
                    + workflowIdSet;
            message += (", and userId:" + p_userid);
            LOGGER.error(message, e);
        }

        return pdfs;
    }

    public File getOldPreviewPdf(String p_filePath, long p_companyId)
    {
        return getPreviewPdf(p_filePath, p_companyId, null);
    }

    public PreviewPDFBO determineConversionParameters(SourcePage sourcePage)
    {
        int fileVersionType = ADOBE_CS2;
        String fileType = ADOBE_INDD;
        String fileSuffix = INDD_SUFFIX;
        boolean isTranslateMaster = true;
        boolean isTranslateHiddenLayer = false;
        String relSafeName = null;
        String safeBaseFileName = null;

        EventFlowXml eventFlow = XmlUtil.string2Object(EventFlowXml.class,
                sourcePage.getRequest().getEventFlowXml());

        String formatType = eventFlow.getValue("formatType");
        String displayNameLower = eventFlow.getDisplayName().toLowerCase();
        relSafeName = eventFlow.getValue("relSafeName");
        safeBaseFileName = eventFlow.getValue("safeBaseFileName");

        if (formatType == null)
        {
            List<Category> categoryList = eventFlow.getCategory();

            if (categoryList != null && categoryList.size() > 0)
            {
                for (Category category : categoryList)
                {
                    List<Da> dalist = category.getDa();

                    for (Da d : dalist)
                    {
                        if ("formatType".equalsIgnoreCase(d.getName()))
                        {
                            List<Dv> dvs = d.getDv();

                            if (dvs.size() > 0)
                                formatType = dvs.get(0).getvalue();
                        }
                        if ("relSafeName".equalsIgnoreCase(d.getName()))
                        {
                            List<Dv> dvs = d.getDv();

                            if (dvs.size() > 0)
                                relSafeName = dvs.get(0).getvalue();
                        }
                        if ("safeBaseFileName".equalsIgnoreCase(d.getName()))
                        {
                            List<Dv> dvs = d.getDv();

                            if (dvs.size() > 0)
                                safeBaseFileName = dvs.get(0).getvalue();
                        }
                    }
                }
            }
        }

        if (formatType == null)
        {
            if (displayNameLower.endsWith(".indd"))
            {
                formatType = "indd_cs5.5";
            }
            else if (displayNameLower.endsWith(".idml"))
            {
                formatType = "idml";
            }
            else if (displayNameLower.endsWith(".docx"))
            {
                formatType = "docx";
            }
            else if (displayNameLower.endsWith(".pptx"))
            {
                formatType = "pptx";
            }
            else if (displayNameLower.endsWith(".xlsx"))
            {
                formatType = "xlsx";
            }
            else if (displayNameLower.endsWith(".xml"))
            {
                formatType = "xml";
            }
        }

        formatType = formatType.trim();

        if ("office-xml".equals(formatType))
        {
            if (displayNameLower.endsWith(".docx"))
            {
                fileVersionType = TYPE_OFFICE_DOCX;
            }
            else if (displayNameLower.endsWith(".pptx"))
            {
                fileVersionType = TYPE_OFFICE_PPTX;
            }
            else if (displayNameLower.endsWith(".xlsx"))
            {
                fileVersionType = TYPE_OFFICE_XLSX;
            }
        }
        else if ("indd_cs4".equals(formatType))
        {
            fileVersionType = ADOBE_CS4;
        }
        else if ("indd_cs5".equals(formatType))
        {
            fileVersionType = ADOBE_CS5;
        }
        else if ("indd_cs5.5".equals(formatType))
        {
            fileVersionType = ADOBE_CS5_5;
        }
        else if ("indd_cs6".equals(formatType))
        {
            fileVersionType = ADOBE_CS6;
        }
        else if ("xml".equals(formatType))
        {
            fileVersionType = TYPE_XML;
        }
        else
        {
            fileVersionType = (formatType.equals("indd")
                    || formatType.equals("inx")) ? ADOBE_CS2 : ADOBE_CS3;
        }

        if (displayNameLower.endsWith(".docx"))
        {
            fileType = OFFICE_DOCX;
            fileSuffix = DOCX_SUFFIX;
            fileVersionType = TYPE_OFFICE_DOCX;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                    isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                    safeBaseFileName);
        }
        else if (displayNameLower.endsWith(".pptx"))
        {
            fileType = OFFICE_PPTX;
            fileSuffix = PPTX_SUFFIX;
            fileVersionType = TYPE_OFFICE_PPTX;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                    isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                    safeBaseFileName);
        }
        else if (displayNameLower.endsWith(".xlsx"))
        {
            fileType = OFFICE_XLSX;
            fileSuffix = XLSX_SUFFIX;
            fileVersionType = TYPE_OFFICE_XLSX;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                    isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                    safeBaseFileName);
        }
        else if (displayNameLower.endsWith(".idml"))
        {
            fileType = ADOBE_IDML;
            fileSuffix = IDML_SUFFIX;
            fileVersionType = ADOBE_TYPE_IDML;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                    isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                    safeBaseFileName);
        }
        else if (displayNameLower.endsWith(".xml"))
        {
            fileType = FILETYPE_XML;
            fileSuffix = XML_SUFFIX;
            fileVersionType = TYPE_XML;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                    isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                    safeBaseFileName);
        }
        else
        {
            fileType = ADOBE_INDD;
            fileSuffix = INDD_SUFFIX;

            String inddHiddenTranslated = eventFlow.getBatchInfo()
                    .getInddHiddenTranslated();
            if (inddHiddenTranslated != null
                    && !"".equals(inddHiddenTranslated))
            {
                isTranslateHiddenLayer = "true".equals(inddHiddenTranslated);
            }
            else
            {
                isTranslateHiddenLayer = true;
            }

            String masterTranslated = eventFlow.getBatchInfo()
                    .getMasterTranslated();
            if (masterTranslated != null && !"".equals(masterTranslated))
            {
                isTranslateMaster = "true".equals(masterTranslated);
            }
        }

        return new PreviewPDFBO(fileVersionType, fileType, fileSuffix,
                isTranslateMaster, isTranslateHiddenLayer, relSafeName,
                safeBaseFileName);
    }

    public PreviewPDFBO determineConversionParameters(long p_srcPageId)
    {
        SourcePage sourcePage = null;
        try
        {
            sourcePage = ServerProxy.getPageManager()
                    .getSourcePage(p_srcPageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return determineConversionParameters(sourcePage);
    }

    private static String getPagePath(Page p_tp, boolean isTarget)
    {
        String path = p_tp.getExternalPageId();
        String trgLocale = p_tp.getGlobalSightLocale().toString();
        if (!path.startsWith(trgLocale))
        {
            String srcLocale = null;

            if (isTarget)
            {
                srcLocale = ((TargetPage) p_tp).getSourcePage()
                        .getGlobalSightLocale().toString();
            }
            else
            {
                srcLocale = p_tp.getGlobalSightLocale().toString();
            }

            path = path.replace(srcLocale, trgLocale);
        }

        return path;
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

    private File getSourceFile(long srcPageId)
    {
        File srcFile = null;
        try
        {
            SourcePage srcPage = ServerProxy.getPageManager()
                    .getSourcePage(srcPageId);
            srcFile = srcPage.getFile();

            // for super translater
            if (srcFile == null)
            {
                srcFile = srcPage.getFileByPageCompanyId();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("get source file error: " + e.getMessage());
        }

        return srcFile;

    }

    private String getKey(boolean isTarget, Page p_page, String p_userId)
    {
        return (isTarget ? "target_" : "source_") + p_page.getId() + "_" + p_userId;
    }

    /*
     * public void removeMapValue(TargetPage p_tp, String p_userId) { String key
     * = getKey(p_tp, p_userId); createPDFMap.remove(key); }
     */
}

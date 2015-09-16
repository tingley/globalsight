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
package com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF;

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
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.globalsight.cxe.adapter.adobe.AdobeConfiguration;
import com.globalsight.cxe.adapter.adobe.AdobeHelper;
import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.adapter.quarkframe.FrameHelper;
import com.globalsight.cxe.engine.eventflow.DiplomatAttribute;
import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
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
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.file.FileWaiter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PreviewPDFHelper implements PreviewPDFConstants
{
    private static final Logger LOGGER = Logger.getLogger(PreviewPDFHelper.class);
    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();
    public static final Set<String> extensionSet = new HashSet<String>();
    // The Map for storing PDF future, the key is TargetPageID, the value is PDF Future.
    private static final Map<String, Future<File>> createPDFMap = new ConcurrentHashMap<String, Future<File>>();
    private static final ExecutorService serviceForINDD = Executors.newSingleThreadExecutor();
    private static final ExecutorService serviceForFM = Executors.newSingleThreadExecutor();
    private static final ExecutorService serviceForIDML = Executors.newSingleThreadExecutor();
    
    private static final int BUFFERSIZE = 4096;    

    private static final String[] PROPERTY_FILES =
    { "/properties/Logger.properties", "/properties/AdobeAdapter.properties" };

    static
    {
        // Initial File Extension Set, which could be previewed by PDF.
        extensionSet.add(INDD_SUFFIX);
        extensionSet.add(INX_SUFFIX);
        extensionSet.add(FM_SUFFIX);
        extensionSet.add(IDML_SUFFIX);
    }

    // Justify whether the job contained the file, which could been previewed by PDF.
    public static boolean isEnablePreviewPDF(Job p_job)
    {
        for (SourcePage sp : (Collection<SourcePage>) p_job.getSourcePages())
        {
            String spPath = sp.getExternalPageId();
            int index = spPath.lastIndexOf(".");
            if (index > -1)
            {
                String extension = spPath.substring(index);
                if (extensionSet.contains(extension.toLowerCase()))
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    public static boolean isEnablePreviewPDF(Task p_task)
    {
        Job job = p_task.getWorkflow().getJob();
        return isEnablePreviewPDF(job);
    }

    // Cancel Creating PDF Threads.
    public void cancelPDF(Set<Long> p_workflowIdSet, String p_userId) throws WorkflowManagerException, RemoteException
    {
        for (long workflowId : p_workflowIdSet)
        {
            Workflow workflow = ServerProxy.getWorkflowManager().getWorkflowById(workflowId);
            for (TargetPage tp : workflow.getTargetPages())
            {
                String key = getKey(tp, p_userId);
                Future<File> future = createPDFMap.get(key);
                if (future != null)
                {
                    future.cancel(true);
                    createPDFMap.remove(key);
                }
            }
        }
    }
    
    // Create All PDF files for Workflow
    public void createPDF(Set<Long> p_workflowIdSet, String p_userId) throws WorkflowManagerException, RemoteException
    {
        for (long workflowId : p_workflowIdSet)
        {
            Workflow workflow = ServerProxy.getWorkflowManager().getWorkflowById(workflowId);
            long companyId = workflow.getCompanyId();
            for (TargetPage tp : workflow.getTargetPages())
			{
				String trgPagePath = getTargetPagePath(tp);
				int index = trgPagePath.lastIndexOf(".");
				String extension = trgPagePath.substring(index);
				if (extensionSet.contains(extension.toLowerCase()))
				{
					File pdf = getPreviewPdf(trgPagePath, companyId, p_userId);
					if (!pdf.exists())
						createPDF(tp, p_userId);
				}
			}
        }
    }

    // Create PDF file for Target Page
    public File createPDF(long p_targetPageId, String p_userId)
    {
        try
        {
            TargetPage targetPage = ServerProxy.getPageManager().getTargetPage(p_targetPageId);
            String key = getKey(targetPage, p_userId);
            Future<File> future = createPDFMap.get(key);
            if(future == null)
            {
                createPDF(targetPage, p_userId);
                future = createPDFMap.get(key);
            }

            // Create PDF again, if no result.
            File pdfFile = future.get();
            if(pdfFile == null || !pdfFile.exists())
            {
                future.cancel(true);
                createPDFMap.remove(key);
                createPDF(targetPage, p_userId);
                future = createPDFMap.get(key);
            }

            return future.get();
        }
        catch (Exception e)
        {
            String msg = "Getting PDF File Error, with target page id:" + p_targetPageId;
            msg += ", and by userId:" + p_userId;
            LOGGER.error(msg, e);
        }

        return null;
    }
    
    // Create PDF file for Target Page
    private void createPDF(TargetPage p_targetPage, String p_userId)
            throws WorkflowManagerException, RemoteException
    {
        // Cancel Duplicate Request
        String key = getKey(p_targetPage, p_userId);
        if(createPDFMap.get(key) != null)
            return;
        
        Callable<File> task;
        Future<File> future = null;
        String externalPageId = p_targetPage.getExternalPageId().toLowerCase();
        if (externalPageId.endsWith(INDD_SUFFIX) || externalPageId.endsWith(INX_SUFFIX))
        {
            task = new CreatePDFTask(p_targetPage, p_userId, ADOBE_CS5_5);
            future = serviceForINDD.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(FM_SUFFIX))
        {
            task = new CreatePDFTask(p_targetPage, p_userId, ADOBE_FM9);
            EditorState editState = new EditorState();
            Vector items = EditorHelper.getExcludedItemsFromL10nProfile(p_targetPage.getWorkflowInstance()
                    .getJob().getL10nProfile());            
            editState.setExcludedItems(items);
            ((CreatePDFTask)task).setEditState(editState);
            future = serviceForFM.submit(task);
            createPDFMap.put(key, future);
        }
        else if (externalPageId.endsWith(IDML_SUFFIX))
        {
            task = new CreatePDFTask(p_targetPage, p_userId, ADOBE_TYPE_IDML);
            future = serviceForIDML.submit(task);
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
    public void setJobDetailsPDFsBO(Workflow p_wf, JobDetailsPDFsBO p_pdfBO, String p_userId)
    {
        long totalPDFFileNumber = 0, existPDFFileNumber = 0;
        long companyId = p_wf.getCompanyId();
        for(TargetPage tp : p_wf.getTargetPages())
        {
            // Set Status
            String key = this.getKey(tp, p_userId);
            Future<File> future = createPDFMap.get(key);
            if (future != null)
            {
                if(future.isDone())
                    p_pdfBO.setStatus(JobDetailsPDFsBO.STATUS_DONE);
                else
                    p_pdfBO.setStatus(JobDetailsPDFsBO.STATUS_IN_PROGRESS);
            }
            
            // Get existPDFFileNumber & totalPDFFileNumber
            String tpPath = getTargetPagePath(tp);
            String extension = tpPath.substring(tpPath.lastIndexOf("."));
            if (extensionSet.contains(extension.toLowerCase()))
            {
                totalPDFFileNumber++;
                File pdfFile = getPreviewPdf(tpPath, companyId, p_userId);
                if(pdfFile.exists())
                    existPDFFileNumber++;
            }
        }

        p_pdfBO.setTotalPDFFileNumber(totalPDFFileNumber);
        p_pdfBO.setExistPDFFileNumber(existPDFFileNumber);
    }
    
    public static void deleteOldPdfByUser(String userid)
    {
        DeleteOldPdfThread t = new DeleteOldPdfThread(userid, LOGGER);
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
            String targetLocale = null;
            String targetFileName = null;
            String targetPdfFileName = null;
            File previewDir = null;
            
            // delete preview file
            if (INDD_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || INX_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || FM_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || IDML_SUFFIX.equalsIgnoreCase(fileSuffix))
            {
                targetLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(p_targetLocaleId).getLocale().toString();
                targetFileName = targetLocale
                        + filename.substring(filename.indexOf(File.separator));
                targetPdfFileName = FileUtils.getPrefix(targetFileName)
                        + PDF_SUFFIX;
                previewDir = AmbFileStoragePathUtils
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
            
            // delete in context review file
            if (INDD_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || IDML_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || DOCX_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || PPTX_SUFFIX.equalsIgnoreCase(fileSuffix)
                    || XLSX_SUFFIX.equalsIgnoreCase(fileSuffix)
                    | ".xml".equalsIgnoreCase(fileSuffix))
            {

                if (targetLocale == null)
                {
                    targetLocale = ServerProxy.getLocaleManager().getLocaleById(p_targetLocaleId)
                            .getLocale().toString();
                    targetFileName = targetLocale
                            + filename.substring(filename.indexOf(File.separator));
                }
                String ext = "." + FileUtils.getSuffix(targetFileName);
                String pdfFileName = FileUtils.getPrefix(targetFileName) + ext + PDF_SUFFIX;
                String previewInctxrv = AmbFileStoragePathUtils.getPdfPreviewDir(company_id)
                        + "_inctx";

                String fullTargetPdfFileName = previewInctxrv + File.separator + pdfFileName;
                FileUtils.deleteSilently(fullTargetPdfFileName);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Could not get the target page.");
            throw new EnvoyServletException(e);
        }
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
                LOGGER.error(e.getMessage(), e);
            }
        }

        return new File(outPutFile);
    }
    
    public static long getExistPDFFileNumber(Workflow p_wf)
    {
        int result = 0;
        Iterator<TargetPage> it = p_wf.getTargetPages().iterator();
        while (it.hasNext())
        {
            String spPath = it.next().getExternalPageId();
            String extension = spPath.substring(spPath.lastIndexOf("."));
            if (extensionSet.contains(extension.toLowerCase()))
            {
                result++;
            }
        }

        return result;
    }
    
    public static boolean isINDDAndInx(PreviewPDFBO p_params)
    {
        int fileVersionType = p_params.getVersionType();
        if (ADOBE_CS2 == fileVersionType || ADOBE_CS3 == fileVersionType 
                || ADOBE_CS4 == fileVersionType || ADOBE_CS5 == fileVersionType 
                || ADOBE_CS5_5 == fileVersionType)
            return true;

        return false;
    }

    File createPDF4INDDAndInx(TargetPage p_tp, String p_userId)
    {
        String trgPagePath = getTargetPagePath(p_tp);
        long trgPageId = p_tp.getId();
        long companyId = p_tp.getCompanyId();
        Job job = p_tp.getWorkflowInstance().getJob();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, p_userId);
        File oldPdfFile = getOldPreviewPdf(trgPagePath, companyId);
        PreviewPDFBO params = determineConversionParameters(p_tp.getSourcePage().getId());        

        return createPDF4INDDAndInx(trgPagePath, pdfFile, oldPdfFile, job, p_userId, companyId, trgPageId,
                params);
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
    private File createPDF4INDDAndInx(String p_trgPageName, File p_pdfFile, 
            File p_oldPdfFile, Job p_job, String p_userId, long p_companyId, 
            long p_trgPageId, PreviewPDFBO p_params)
    {
        String converterDir = null;
        Map<String, String> newFileNameMap = new HashMap<String, String>();
        String p_fileSuffix = p_params.getFileSuffix();
        try
        {
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(p_trgPageId);
            String trgLocale = tp.getGlobalSightLocale().toString();
            converterDir = getConvertDir(p_params) + trgLocale;
            new File(converterDir).mkdirs();

            String targetPageFolder = p_pdfFile.getParent();
            String statusPageFolder = p_oldPdfFile.getParent();
            File f = new File(targetPageFolder);
            if (!f.exists())
            {
                f.mkdirs();
                copyFilesToNewTargetLocale(p_job, trgLocale, p_trgPageName, 
                        targetPageFolder, statusPageFolder, p_params, p_companyId);
            }
            File previewDir = AmbFileStoragePathUtils.getPdfPreviewDir(p_companyId);
            String oriXmlFileName = getConvertedFileName(previewDir, p_trgPageName, trgLocale, p_job.getL10nProfile()
                    .getTargetLocales());

            // Upload indd and xmp file to converter.
            backupInddFile(newFileNameMap, getConvertDir(p_params), trgLocale, oriXmlFileName, p_fileSuffix, p_job);
            // Upload XML file to converter
            String xmlFilePath = converterDir + File.separator + (String) newFileNameMap.get(XML_SUFFIX);
            writeXMLFileToConvertDir(xmlFilePath, trgLocale, p_trgPageId, p_params, p_userId);
            // Upload command file to converter
            writeCommandFile(xmlFilePath, p_params);
            
            // Wait for Adobe Converter to convert
            return readTargetPdfFile(xmlFilePath, p_trgPageName, trgLocale, p_userId, p_companyId);
        }
        catch (InterruptedException e)
        {
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
                    FileUtils.deleteSilently(converterDir + File.separator + (String) newFileNameMap.get(p_fileSuffix));
                    FileUtils.deleteSilently(converterDir + File.separator + (String) newFileNameMap.get(XMP_SUFFIX));
                }
            }
            catch (Exception fex)
            {
            }
        }

        return null;
    }
    
    File createPDF4IDML(TargetPage p_tp, String p_userId)
    {
        IdmlConverter converter = new IdmlConverter();
        String trgPagePath = getTargetPagePath(p_tp);
        long trgPageId = p_tp.getId();  
        long companyId = p_tp.getCompanyId();
        String trgLocale = p_tp.getGlobalSightLocale().toString();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, p_userId);
        PreviewPDFBO params = determineConversionParameters(p_tp.getSourcePage().getId()); 
        
        try
        {
            String converterDir = getConvertDir(params) + File.separator + trgLocale;
            new File(converterDir).mkdirs();

            String xmlFilePath = converterDir + File.separator + params.getRelSafeName();
            File zipDir = getZipDir(new File(xmlFilePath), params.getSafeBaseFileName());
            // write xml file
            writeXMLFileToConvertDir(xmlFilePath, trgLocale, trgPageId, params, p_userId);
            IdmlHelper.split(zipDir.getAbsolutePath());
            IDMLFontMappingHelper idmlFontMappinghelper = new IDMLFontMappingHelper();
            idmlFontMappinghelper.processIDMLFont(zipDir.getAbsolutePath(), trgLocale);
            String idmlPath = converter.convertXmlToIdml(params.getSafeBaseFileName(), zipDir.getAbsolutePath());
            IDMLFontMappingHelper.restoreIDMLFont(zipDir.getAbsolutePath());
            converter.convertToPdf(new File(idmlPath), pdfFile, trgLocale);
        }
        catch (InterruptedException e)
        {
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(trgPagePath)
               .append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }
        
        return pdfFile;
    }
    
    /**
     * Create PDF file for FM Target page.
     * 
     * @param p_tp
     * @param p_userId
     * @param state
     * @return
     */
    File createPDF4FM9(TargetPage p_tp, String p_userId, EditorState state)
    {
        String trgPagePath = getTargetPagePath(p_tp);
        long trgPageId = p_tp.getId();
        long companyId = p_tp.getCompanyId();
        String trgLocale = p_tp.getGlobalSightLocale().toString();
        File pdfFile = getPreviewPdf(trgPagePath, companyId, p_userId);
        PreviewPDFBO params = determineConversionParameters(p_tp.getSourcePage().getId());        

        File mifFile = getMifTargetFile(trgPageId, p_userId, state);
        
        try
        {
            String converterDir = getConvertDir(params) + trgLocale;
            new File(converterDir).mkdirs();

            int lastSeparatorIndex = Math.max(trgPagePath.lastIndexOf("/"), trgPagePath.lastIndexOf("\\"));
            String filename = trgPagePath.substring(lastSeparatorIndex + 1);
            String filenamePre = FileUtils.getPrefix(filename);
            String currentTime = String.valueOf(System.currentTimeMillis());
            String fileToConvert = converterDir + File.separator + currentTime + filenamePre + MIF_SUFFIX;
            String fileExpected = converterDir + File.separator + currentTime + filenamePre + PDF_SUFFIX;
            String fileStatus = converterDir + File.separator + currentTime + filenamePre + STATUS_SUFFIX;
            String fileCommand = converterDir + File.separator + currentTime + filenamePre + FM_COMMAND_SUFFIX;
            
            // Prepare mif file for creating pdf.
            FileUtils.copyFile(mifFile, new File(fileToConvert));
            
            // write command to convert mif to PDF
            StringBuffer text = new StringBuffer();
            text.append("ConvertFrom=mif").append("\r\n");
            text.append("ConvertTo=pdf").append("\r\n");
            FileUtil.writeFileAtomically(new File(fileCommand), text.toString(), "US-ASCII");
            // wait for status
            FileWaiter fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME, getMaxWaitTime(), fileStatus);
            fileWaiter.waitForFile();
            // parse status file
            String[] status = statusInfo(new File(fileStatus));
            FileUtils.deleteSilently(fileStatus);
            if (!"0".equals(status[0]))
            {
                LOGGER.error("FrameMaker convertion failed: " + "Cannot convert to PDF file correctly. "
                        + ((status.length == 2) ? status[1] : status[0]));

                throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            else
            {
                FileUtils.copyFile(new File(fileExpected), pdfFile);
            }
        }
        catch (InterruptedException e)
        {
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(trgPagePath).append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }
        
        return pdfFile;
    }
    
    File createPDF4FM9SourcePage(File p_pdfFile, long p_sourcePageId, String p_userId, String p_pageName)
    {
        SourcePage sp = null;
        try
        {
            sp = ServerProxy.getPageManager().getSourcePage(p_sourcePageId);
            String srcLocale = sp.getGlobalSightLocale().toString();
            PreviewPDFBO params = determineConversionParameters(p_sourcePageId);        

            File fmFile = getSourceFile(p_sourcePageId);
            String converterDir = getConvertDir(params) + srcLocale;
            new File(converterDir).mkdirs();

            int lastSeparatorIndex = Math.max(p_pageName.lastIndexOf("/"), p_pageName.lastIndexOf("\\"));
            String filename = p_pageName.substring(lastSeparatorIndex + 1);
            String filenamePre = FileUtils.getPrefix(filename);
            String currentTime = String.valueOf(System.currentTimeMillis());
            String fileExpected = converterDir + File.separator + currentTime + filenamePre + PDF_SUFFIX;
            String fileStatus = converterDir + File.separator + currentTime + filenamePre + STATUS_SUFFIX;
            String fileCommand = converterDir + File.separator + currentTime + filenamePre + FM_COMMAND_SUFFIX;
            
            // source preview, convert fm to mif first
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
                LOGGER.error("FrameMaker convertion failed: "
                        + "Cannot convert to PDF file correctly. "
                        + ((status.length == 2) ? status[1]
                                : status[0]));

                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            
            // write command to convert mif to PDF
            text = new StringBuffer();
            text.append("ConvertFrom=mif").append("\r\n");
            text.append("ConvertTo=pdf").append("\r\n");
            FileUtil.writeFileAtomically(new File(fileCommand), text.toString(), "US-ASCII");
            // wait for status
            fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME, getMaxWaitTime(), fileStatus);
            fileWaiter.waitForFile();
            // parse status file
            status = statusInfo(new File(fileStatus));
            FileUtils.deleteSilently(fileStatus);
            if (!"0".equals(status[0]))
            {
                LOGGER.error("FrameMaker convertion failed: " + "Cannot convert to PDF file correctly. "
                        + ((status.length == 2) ? status[1] : status[0]));

                throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            else
            {
                FileUtils.copyFile(new File(fileExpected), p_pdfFile);
            }
        }
        catch (InterruptedException e)
        {
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer("Create PDF error for page:");
            msg.append(sp.getExternalPageId()).append(", by user:").append(p_userId);
            LOGGER.error(msg.toString(), e);
        }
        
        return p_pdfFile;
    }

    /**
     * Returns the convert directory
     * 
     * @return
     * @throws Exception
     */
    public String getConvertDir(PreviewPDFBO p_params) throws Exception
    {
        int fileVersionType = p_params.getVersionType();
        if (fileVersionType == ADOBE_FM9)
        {
            return FrameHelper.getConversionDir() + File.separator;
        }
        else if (fileVersionType == ADOBE_TYPE_IDML)
        {
            return IdmlHelper.getConversionDir();
        }
        else
        {
            StringBuffer convDir = null;
            if (fileVersionType == ADOBE_CS2)
            {
                // These are adobe InDesign cs2 files, we will use cs2 converter to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR));
            }
            else if (fileVersionType == ADOBE_CS4)
            {
                // These are adobe InDesign cs4 files, we will use cs4 converter to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS4));
            }
            else if (fileVersionType == ADOBE_CS5)
            {
                convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5));
            }
            else if (fileVersionType == ADOBE_CS5_5)
            {
                convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5));
            }
            else
            {
                // These (formatType are "indd_cs3" and "inx_cs3") are adobe InDesign cs3 files, 
                // we will use cs3 converter to process them.
                convDir = new StringBuffer(m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS3));
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
    private void copyFilesToNewTargetLocale(Job p_job, String p_trgLocale, String p_trgPageName,
            String p_trgPageFolder, String p_statusFolder, PreviewPDFBO p_params, long p_companyId)
            throws Exception
    {
        String existedLocale = null;
        String tarName = null;
        String prefix = FileUtils.getPrefix(p_trgPageName);
        File previewDir = AmbFileStoragePathUtils.getPdfPreviewDir(p_companyId);
        String srcLocale = p_job.getSourceLocale().toString();
        GlobalSightLocale[] allTrgLocales = p_job.getL10nProfile().getTargetLocales();
        for (GlobalSightLocale globalSightLocale : allTrgLocales)
        {
            existedLocale = globalSightLocale.toString();

            String existedFolder = prefix.replaceFirst(p_trgLocale, existedLocale);
            tarName = previewDir + File.separator + existedFolder + STATUS_SUFFIX;

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

        String existedTpFolder = p_statusFolder.replaceFirst(p_trgLocale, existedLocale);
        File file = new File(existedTpFolder);
        File[] files = file.listFiles();
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                File f = files[i];
                if (STATUS_SUFFIX.equalsIgnoreCase(f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("."))))
                    FileCopier.copy(f, p_trgPageFolder);
            }
        }

        copyAdobeFiles(srcLocale, p_trgLocale, getConvertedFileName(previewDir, p_trgPageName, p_trgLocale, allTrgLocales), p_params);
    }

    /**
     * Gets the converted file name, by parsing the STATUS file.
     * 
     * @param p_targetPageName
     * @return
     * @throws IOException 
     */
    private String getConvertedFileName(File p_previewDir, String p_targetPageName, String targetLocale,
            GlobalSightLocale[] p_allTrgLocales) throws IOException
    {
        String tarName = null;
        String prefix = FileUtils.getPrefix(p_targetPageName);
        for (GlobalSightLocale globalSightLocale : p_allTrgLocales)
        {
            String existedLocale = globalSightLocale.toString();
            String existedFolder = prefix.replaceFirst(targetLocale, existedLocale);
            tarName = p_previewDir + File.separator + existedFolder + STATUS_SUFFIX;

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
            throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF, tarName
                    + " does not exist");
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
        finally{
            if(br != null)
                br.close();
        }

        return FileUtils.getPrefix(convertedFileName.substring(convertedFileName.indexOf("=") + 1)) + XML_SUFFIX;
    }

    private void copyAdobeFiles(String p_existedLocale, String p_trgLocale,
            String p_convertedFileName, PreviewPDFBO p_params) throws Exception
    {
        String conDir = getConvertDir(p_params);
        String fileName = FileUtils.getPrefix(p_convertedFileName);

        StringBuffer inddFile = new StringBuffer(conDir);
        inddFile.append(p_existedLocale).append(File.separator).append(fileName).append(p_params.getFileSuffix());
        StringBuffer xmpFile = new StringBuffer(conDir);
        xmpFile.append(p_existedLocale).append(File.separator).append(fileName).append(XMP_SUFFIX);
        StringBuffer pdfFile = new StringBuffer(conDir);
        pdfFile.append(p_existedLocale).append(File.separator).append(fileName).append(PDF_SUFFIX);

        FileCopier.copy(new File(inddFile.toString()), conDir + p_trgLocale);
        FileCopier.copy(new File(xmpFile.toString()), conDir + p_trgLocale);
        FileCopier.copy(new File(pdfFile.toString()), conDir + p_trgLocale);
    }

    /**
     * Backs up adobe indesign and xmp file
     * 
     * @param p_conDir
     * @param p_xmlFileName
     */
    private void backupInddFile(Map<String, String> fileMap, String p_conDir, String targetLocale, String p_xmlFileName,
            String p_fileSuffix, Job p_job)
    {
        String fileNameWithoutSuffix = FileUtils.getPrefix(p_xmlFileName);
        String currentTime = String.valueOf(System.currentTimeMillis());
        String targetInddName = currentTime + "_" + fileNameWithoutSuffix + p_fileSuffix;
        String targetXmpName = currentTime + "_" + fileNameWithoutSuffix + XMP_SUFFIX;
        String targetXmlName = currentTime + "_" + p_xmlFileName;

        L10nProfile lp = p_job.getL10nProfile();
        File oriInddFile = null;
        File oriXmpFile = null;

        // get from target locale first
        String oriInddFileName = p_conDir + targetLocale + File.separator + fileNameWithoutSuffix + p_fileSuffix;
        String oriXmpFileName = p_conDir + targetLocale + File.separator + fileNameWithoutSuffix + XMP_SUFFIX;
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
            oriInddFileName = p_conDir + srcLocale + File.separator + fileNameWithoutSuffix + p_fileSuffix;
            oriXmpFileName = p_conDir + srcLocale + File.separator + fileNameWithoutSuffix + XMP_SUFFIX;
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

                oriInddFileName = p_conDir + existedLocale + File.separator + fileNameWithoutSuffix + p_fileSuffix;
                oriXmpFileName = p_conDir + existedLocale + File.separator + fileNameWithoutSuffix + XMP_SUFFIX;

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
            oriInddFileName = p_conDir + targetLocale + File.separator + fileNameWithoutSuffix + p_fileSuffix;
            LOGGER.error("Cannot find original indd and xmp file: " + oriInddFileName);
            throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                    "Cannot find original indd and xmp file: " + oriInddFileName);
        }

        // back up indd file
        FileCopier.copyFile(oriInddFile, new File(p_conDir + targetLocale), targetInddName);
        // back up xml file
        FileCopier.copyFile(oriXmpFile, new File(p_conDir + targetLocale), targetXmpName);

        fileMap.put(p_fileSuffix, targetInddName);
        fileMap.put(XMP_SUFFIX, targetXmpName);
        fileMap.put(XML_SUFFIX, targetXmlName);
    }

    /**
     * Writes xml file to convert directory
     * 
     * @param p_request
     */
    private void writeXMLFileToConvertDir(String p_xmlFilePath, String p_trgLocale, 
            long p_trgPageId, PreviewPDFBO p_params, String uid) throws Exception
    {
        ExportHelper ex = new ExportHelper();
        ex.setUserId(uid);
        try
        {
            if (p_trgPageId == 0)
            {
                throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF, "No target page found");
            }
            String xml = ex.exportForPdfPreview(p_trgPageId, "UTF-8", false);
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
            // indd inx
            else 
            {
                FontMappingHelper fontMappinghelper = new FontMappingHelper();
                processed = fontMappinghelper.processInddXml(p_trgLocale, processed);
            }
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    p_xmlFilePath.toString()), ExportConstants.UTF8), processed.length());
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
    private void writeCommandFile(String p_xmlFileName, PreviewPDFBO p_params) throws IOException
    {
        String commandFileName = FileUtils.getPrefix(p_xmlFileName) + PV_COMMAND_SUFFIX;
        StringBuffer text = new StringBuffer();
        text.append("ConvertFrom=xml").append("\r\n");
        text.append("ConvertTo=").append(p_params.getFileType()).append("\r\n");
        text.append("AcceptChanges=true").append("\r\n");
        text.append("MasterTranslated=").append(p_params.isTranslateMaster()).append("\r\n");
        text.append("TranslateHiddenLayer=").append(p_params.isTranslateHiddenLayer()).append("\r\n");

        FileUtil.writeFileAtomically(new File(commandFileName), text.toString(), "US-ASCII");
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
            m_adobeProperties.load(AdobeConfiguration.class.getResourceAsStream(PROPERTY_FILES[0]));
            maxWaitTime = m_adobeProperties.getProperty(AdobeConfiguration.MAX_TIME_TO_WAIT);

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

            return new String[]
            { errorLine, msg };
        }
        catch (Exception e)
        {
            if (LOGGER.isEnabledFor(Priority.WARN))
                LOGGER.warn("Cannot read status info", e);
            return new String[]
            { e.getMessage() };
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
    private File readTargetPdfFile(String p_xmlFileName, String p_targetPageName, String p_trgLocale, String p_userId,
            long p_companyId)
    {
        String statusFileName = FileUtils.getPrefix(p_xmlFileName) + COMMAND_STATUS_SUFFIX;
        File statusFile = new File(statusFileName);
        String[] status = null;
        FileWaiter fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME, getMaxWaitTime(), statusFileName);
        StringBuffer tarDir = new StringBuffer(AmbFileStoragePathUtils.getPdfPreviewDir(p_companyId).getAbsolutePath());
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
                LOGGER.error("Adobe convertion failed: " + "Cannot convert to PDF file correctly.");
                throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                        "Cannot convert to PDF file correctly.");
            }
            else
            {
                tarDir.append(File.separator).append(p_userId);
                tarDir.append(File.separator).append(p_trgLocale);
                String splitChar = p_targetPageName.lastIndexOf("/") > 0 ? "/" : "\\\\";
                String[] tarDisplayName = p_targetPageName.split(splitChar);

                for (int j = 1; j < tarDisplayName.length - 1; j++)
                {
                    tarDir.append(File.separator);
                    tarDir.append(tarDisplayName[j]);
                }
                File tarDirFile = new File(tarDir.toString());
                tarDirFile.mkdirs();
                FileCopier.copy(pdfFileName, tarDir.toString());
                // rename the copied pdf file to the one without the timestamp prefixed.
                StringBuffer folderPath = new StringBuffer(tarDir.toString());
                File pre_pdfFile = new File(folderPath.append(pdfFileName.substring(pdfFileName.lastIndexOf(File.separator))).toString());
                folderPath = new StringBuffer(tarDir.toString());                
                String basicFileName = tarDisplayName[tarDisplayName.length - 1];
                basicFileName = basicFileName.substring(0, basicFileName.lastIndexOf("."));
                post_pdfFile = new File(folderPath.append(File.separator).append(basicFileName).append(PDF_SUFFIX).toString());
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
    public static File getPreviewPdf(String p_filePath, long p_companyId, String p_userid)
    {
        int index = p_filePath.lastIndexOf(".");
        String pdfPath = p_filePath.substring(0, index) + PDF_SUFFIX;
        StringBuffer pdfFullPath = new StringBuffer(AmbFileStoragePathUtils.getPdfPreviewDir(p_companyId)
                .getAbsolutePath());

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
    
    public static Set<File> getPreviewPdf(Set<Long> workflowIdSet, String p_userid)
    {
        Set<File> pdfs = new HashSet<File>();
        try
        {
            for (long wfId : workflowIdSet)
            {
                Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(wfId);
                long companyId = wf.getCompanyId();
                for (TargetPage tp : wf.getTargetPages())
                {
                    String trgPagePath = getTargetPagePath(tp);
                    int index = trgPagePath.lastIndexOf(".");
    				String extension = trgPagePath.substring(index);
    				if (extensionSet.contains(extension.toLowerCase()))
    				{
    					File pdf = getPreviewPdf(trgPagePath, companyId, p_userid);
    					if (pdf.exists())
    						pdf = setCopyOnlyPermission(pdf);
    					pdfs.add(pdf);
    				}
                }
            }
        }
        catch (Exception e)
        {
            String message = "Get Preview pdf error, with workflowId set:" + workflowIdSet;
            message += (", and userId:" + p_userid);
            LOGGER.error(message, e);
        }

        return pdfs;
    }

    File getOldPreviewPdf(String p_filePath, long p_companyId)
    {
        return getPreviewPdf(p_filePath, p_companyId, null);
    }

    PreviewPDFBO determineConversionParameters(long p_srcPageId)
    {
        int fileVersionType = ADOBE_CS2;
        String fileType = ADOBE_INDD;
        String fileSuffix = INDD_SUFFIX;
        boolean isTranslateMaster = true;
        boolean isTranslateHiddenLayer = false;
        String relSafeName = null;
        String safeBaseFileName = null;

        SourcePage sourcePage = null;
        try
        {
            sourcePage = ServerProxy.getPageManager().getSourcePage(p_srcPageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        EventFlowXml eventFlow = XmlUtil.string2Object(EventFlowXml.class, sourcePage.getRequest()
                .getEventFlowXml());

        String formatType = eventFlow.getValue("formatType").trim();
        String displayNameLower = eventFlow.getDisplayName().toLowerCase();
        relSafeName = eventFlow.getValue("relSafeName");
        safeBaseFileName = eventFlow.getValue("safeBaseFileName");

        if ("mif".equals(formatType))
        {
            fileVersionType = ADOBE_FM9;
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
        else
        {
            fileVersionType = (formatType.equals("indd") || formatType.equals("inx")) ? ADOBE_CS2 : ADOBE_CS3;
        }

        if (fileVersionType == ADOBE_FM9)
        {
            fileType = ADOBE_FM;
            fileSuffix = FM_SUFFIX;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix, isTranslateMaster, isTranslateHiddenLayer,
                    relSafeName, safeBaseFileName);
        }
        else if (displayNameLower.endsWith("idml"))
        {
            fileType = ADOBE_IDML;
            fileSuffix = IDML_SUFFIX;
            fileVersionType = ADOBE_TYPE_IDML;
            return new PreviewPDFBO(fileVersionType, fileType, fileSuffix, isTranslateMaster, isTranslateHiddenLayer,
                    relSafeName, safeBaseFileName);
        }
        else
        {
            fileType = displayNameLower.endsWith("indd") ? ADOBE_INDD : ADOBE_INX;
            fileSuffix = displayNameLower.endsWith("indd") ? INDD_SUFFIX : INX_SUFFIX;

            String inddHiddenTranslated = eventFlow.getBatchInfo().getInddHiddenTranslated();
            if (inddHiddenTranslated != null && !"".equals(inddHiddenTranslated))
            {
                isTranslateHiddenLayer = "true".equals(inddHiddenTranslated);
            }
            else
            {
                isTranslateHiddenLayer = true;
            }

            String masterTranslated = eventFlow.getBatchInfo().getMasterTranslated();
            if (masterTranslated != null && !"".equals(masterTranslated))
            {
                isTranslateMaster = "true".equals(masterTranslated);
            }
        }

        return new PreviewPDFBO(fileVersionType, fileType, fileSuffix, isTranslateMaster, isTranslateHiddenLayer,
                relSafeName, safeBaseFileName);
    }
    
    private static String getTargetPagePath(TargetPage p_tp)
    {
        String path = p_tp.getExternalPageId();
        String trgLocale = p_tp.getGlobalSightLocale().toString();
        if(!path.startsWith(trgLocale))
        {
            String srcLocale = p_tp.getSourcePage().getGlobalSightLocale().toString();
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
    
    private File getMifTargetFile(long targetPageId, String uid, EditorState state)
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
            LOGGER.error("get target file error: " + e.getMessage());
        }

        return targetFile;
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
            LOGGER.error("get source file error: " + e.getMessage());
        }

        return srcFile;

    }
    
    private String getKey(TargetPage p_tp, String p_userId)
    {
        return p_tp.getId() + "_" + p_userId;
    }
    
    /*
    public void removeMapValue(TargetPage p_tp, String p_userId)
    {
        String key = getKey(p_tp, p_userId);
        createPDFMap.remove(key);
    }       */
}

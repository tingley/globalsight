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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState.PagePair;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.lowagie.text.DocumentException;

public class PreviewPDFPageHandler extends PageHandler implements
        PreviewPDFConstants
{
    private static final Logger LOGGER = Logger
            .getLogger(PreviewPDFPageHandler.class);

    private SessionManager sessionMgr = null;
    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private long m_company_id;
    
    private static Object locker = new Object();
    private static long deleteFileBefore = 1000 * 60 * 60 * 24 * 7;

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(true);
        sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);

        Object jobIdO = sessionMgr.getAttribute(WebAppConstants.JOB_ID);
        m_job = getJobById(getLongValue(jobIdO));
        if (m_job != null)
        {
            m_company_id = m_job.getCompanyId();
        }
        else
        {
            m_company_id = Long
                    .valueOf(CompanyThreadLocal.getInstance().getValue());
            LOGGER.error(
                    "If can not view the pdf file besause the company id is incorrect.");
        }

        Object userobj = sessionMgr.getAttribute(WebAppConstants.USER);
        UserImpl user = null;
        if (userobj != null)
        {
            user = (UserImpl) userobj;
        }
        String userid = user == null ? null : user.getUserId();

        String action = p_request.getParameter("action");
        
        if (action == null)
        {
            LOGGER.error("action is null.");
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
            return;
        }
        
        File pdfFile = getPreviewPdf(p_request, userid, action);
        
        if (!pdfFile.exists())
        {
            EditorState state = (EditorState) sessionMgr
                    .getAttribute(WebAppConstants.EDITORSTATE);
            long srcPageId = state.getSourcePageId();
            long trgPageId = state.getTargetPageId();
    
            SourcePage srcPage = ServerProxy.getPageManager().getSourcePage(
                    srcPageId);
            String filePath = srcPage.getExternalPageId();
            if (filePath.toLowerCase().startsWith("(adobe file information)"))
            {
                String inddFilePath = filePath
                        .substring(filePath.indexOf(") ") + 2);
                inddFilePath = inddFilePath.replace('/', '\\');
                Collection<SourcePage> sourcePages = (Collection<SourcePage>) m_job
                        .getSourcePages();
                for (SourcePage sourcePage : sourcePages)
                {
                    String eid = sourcePage.getExternalPageId();
                    eid = eid.replace('/', '\\');
                    if (eid.equalsIgnoreCase(inddFilePath))
                    {
                        srcPageId = sourcePage.getId();
                        trgPageId = sourcePage
                                .getTargetPageByLocaleId(
                                        state.getTargetLocale().getId())
                                .getId();
                        break;
                    }
                }
            }
            
            PreviewPDFHelper previewHelper = new PreviewPDFHelper();
            PreviewPDFBO previewParams = previewHelper
                    .determineConversionParameters(srcPageId);
            if (previewParams.getVersionType() == ADOBE_TYPE_IDML)
            {
                try
                {
                    if (action.equals("previewSrc"))
                    {
                        pdfFile = previewHelper.createPDF(srcPageId, userid,
                                false);
                    }
                    else if (action.equals("previewTar"))
                    {
                        pdfFile = previewHelper.createPDF(trgPageId, userid,
                                true);
                    }
                }
                catch (Exception e)
                {
                    ResourceBundle rb = PageHandler
                            .getBundle(p_request.getSession());

                    LOGGER.error(e.getMessage(), e);
                    String msg = e.getMessage();
                    String error = "Read PDF Data Error!";
                    if ("idml converter is not started".equals(msg))
                    {
                        error = rb
                                .getString("lb_filter_msg_convert_start_idml");
                    }

                    StringBuffer sb = new StringBuffer();
                    sb.append(
                            "<%@ page contentType=\"text/html; charset=UTF-8\" errorPage=\"error.jsp\" session=\"true\"%>");
                    sb.append("<HTML>");
                    sb.append("<HEAD>");
                    sb.append(
                            "<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"/globalsight/includes/setStyleSheet.js\"></SCRIPT>");
                    sb.append("</HEAD>");
                    sb.append("<BODY>");
                    sb.append(
                            "<DIVSTYLE=\" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;\">");
                    sb.append(
                            "<DIV STYLE=\" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;\">");
                    sb.append("<SPAN CLASS=\"headingError\">" + error
                            + "</SPAN>");
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
                if (action.equals("previewSrc"))
                {
                    pdfFile = previewHelper.createPDF(srcPageId, userid, false);
                }
                else if (action.equals("previewTar"))
                {
                    pdfFile = previewHelper.createPDF(trgPageId, userid, true);
                }
            }
        }

        if (pdfFile != null && pdfFile.exists())
        {
            try
            {
                Date nowDate = new Date();
                String pdfroot = p_context
                        .getRealPath("/envoy/edit/inctxrv/pdf/");
                // generate the new file for each preview to avoid PDF.js cache.
                String fileName = nowDate.getTime() + "_" + pdfFile.getName();

                File pdfInWeb = new File(pdfroot + File.separator + "pdf_files",
                        fileName);
                if (pdfInWeb.exists())
                {
                    pdfInWeb.delete();
                }
                pdfInWeb.getParentFile().mkdirs();
                FileCopier.copyFile(pdfFile, pdfInWeb.getParentFile(),
                        fileName);

                // start to delete old files on Monday
                if (nowDate.getDay() == 1)
                {
                    synchronized (locker)
                    {
                        File[] files = pdfInWeb.getParentFile().listFiles();

                        long newDate = nowDate.getTime();

                        for (File file : files)
                        {
                            long times = newDate - file.lastModified();

                            if (times > deleteFileBefore)
                            {
                                try
                                {
                                    file.delete();
                                }
                                catch (Exception eee)
                                {
                                    // ignore, delete on next time.
                                }
                            }
                        }
                    }
                }

                String pdfUrl = "pdf_files/" + fileName;
                p_request.setAttribute("pdfUrl", pdfUrl);

                super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                        p_context);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else
        {
            String msg = "Can not generate PDF file for review: " + pdfFile;
            LOGGER.error(msg);
            p_request.setAttribute("errorMsg",
                    "Can't generate PDF for review, please contact Admin for advice.");
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
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
        if (filePath.startsWith("("))
        {
            filePath = filePath.substring(filePath.indexOf(") ") + 2);
        }
        
        int index = filePath.lastIndexOf(".");
        String ext = filePath.substring(index).toLowerCase();
        
        String pdfPath = filePath.substring(0, index) + ext + PDF_SUFFIX;
        StringBuffer pdfFullPath = new StringBuffer(AmbFileStoragePathUtils
                .getPdfPreviewDir(m_company_id).getAbsolutePath());
        pdfFullPath.append("_inctx");

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

    private long getLongValue(Object p_obj)
    {
        if (p_obj instanceof Long)
            return (Long) p_obj;

        return Long.valueOf((String) p_obj);
    }
    
    public static boolean okForInContextReview(PagePair pagep)
    {
        String pageName = pagep.getPageName().toLowerCase();

        try
        {
            if (pageName.endsWith(".indd") || pageName.endsWith(".idml")
                    || pageName.endsWith(".docx") || pageName.endsWith(".pptx")
                    || pageName.endsWith(".xlsx") || pageName.endsWith(".xml"))
            {
                SourcePage sourcePage = ServerProxy.getPageManager()
                        .getSourcePage(pagep.getSourcePageId());
                Job job = sourcePage.getRequest().getJob();
                long fpId = sourcePage.getRequest().getDataSourceId();
                FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                        .readFileProfile(fpId);
                String companyId = "" + job.getCompanyId();

                if (pageName.endsWith(".xml")
                        && PreviewPDFHelper.isXMLEnabled(companyId)
                        && FileProfileUtil.isXmlPreviewPDF(fp))
                {
                    return true;
                }

                if ((pageName.endsWith(".indd") || pageName.endsWith(".idml"))
                        && PreviewPDFHelper.isInDesignEnabled(companyId))
                {
                    return true;
                }

                if ((pageName.endsWith(".docx") || pageName.endsWith(".pptx")
                        || pageName.endsWith(".xlsx"))
                        && PreviewPDFHelper.isOfficeEnabled(companyId))
                {
                    return true;
                }

            }
        }
        catch (Exception ex)
        {
            LOGGER.error("pageName = " + pageName, ex);
        }

        return false;
    }
}

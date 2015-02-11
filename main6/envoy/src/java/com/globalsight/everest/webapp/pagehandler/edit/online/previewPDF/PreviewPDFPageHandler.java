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

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.engine.util.FileUtils;
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
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.lowagie.text.DocumentException;

public class PreviewPDFPageHandler extends PageHandler implements PreviewPDFConstants
{
    private static final Logger LOGGER = Logger
            .getLogger(PreviewPDFPageHandler.class);

    private SessionManager sessionMgr = null;
    private Job m_job = null;
    // this company_id is of the job, not for the user.
    private long m_company_id;

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
            m_company_id = Long.valueOf(CompanyThreadLocal.getInstance().getValue());
            LOGGER.error("If can not view the pdf file besause the company id is incorrect.");
        }
        
        Object userobj = sessionMgr.getAttribute(WebAppConstants.USER);
        UserImpl user = null;
        if (userobj != null)
        {
            user = (UserImpl) userobj;
        }
        String userid = user == null ? null : user.getUserId();

        String action = p_request.getParameter("action");
        File pdfFile = getPreviewPdf(p_request, userid, action);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
        long srcPageId = state.getSourcePageId();
        long trgPageId = state.getTargetPageId();

        if (action == null)
        {
            LOGGER.error("action is null.");
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
            return;
        }

        PreviewPDFHelper previewHelper = new PreviewPDFHelper();
        PreviewPDFBO previewParams = previewHelper.determineConversionParameters(srcPageId);
        if (previewParams.getVersionType() == ADOBE_FM9)
        {
            if (!pdfFile.exists())
            {
                if (action.equals("previewSrc"))
                {
                    String pageName = p_request.getParameter("file");
                    pdfFile = previewHelper.createPDF4FM9SourcePage(pdfFile, srcPageId, userid, pageName);
                }
                else if (action.equals("previewTar"))
                {
                    pdfFile = previewHelper.createPDF(trgPageId, userid);
                }
            }
        }
        else if (previewParams.getVersionType() == ADOBE_TYPE_IDML)
        {
            try
            {
                if (!pdfFile.exists())
                {
                    if (action.equals("previewSrc"))
                    {
                        String currentLocale = m_job.getSourceLocale().toString();
                        File idmlFile = getSourceFile(srcPageId);
                        new IdmlConverter().convertToPdf(idmlFile, pdfFile, currentLocale);
                    }
                    else if (action.equals("previewTar"))
                    {
                        pdfFile = previewHelper.createPDF(trgPageId, userid);
                    }
                }
            }
            catch (Exception e)
            {
                ResourceBundle rb = PageHandler.getBundle(p_request
                        .getSession());

                LOGGER.error(e.getMessage(), e);
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
            if (action.equals("previewSrc"))
            {
                if (!pdfFile.exists())
                {
                    LOGGER.info("The source PDF file is missing.");
                    throw new EnvoyServletException(EnvoyServletException.MSG_FAILED_TO_PREVIEW_PDF,
                            "PDF file does not exist.");
                }
            }
            else if (action.equals("previewTar"))
            {
                if (!pdfFile.exists())
                {
                    pdfFile = previewHelper.createPDF(trgPageId, userid);
                }
            }
        }

        if (pdfFile.exists())
        {
            try
            {
                File viewFile = PreviewPDFHelper.setCopyOnlyPermission(pdfFile);

                p_response.setContentType("application/pdf");
                if (p_request.isSecure())
                {
                    setHeaderForHTTPSDownload(p_response);
                }
                else
                {
                    p_response.setHeader("Cache-Control", "no-cache");
                }

                String filename = pdfFile.getName();
                p_response.setHeader("Content-Disposition",
                        "inline; filename=\"" + filename + "\"");
                PreviewPDFHelper.writeOutFile(viewFile, p_response, action);
                FileUtils.deleteSilently(viewFile.getAbsolutePath());
            }
            catch (DocumentException e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else
        {
            LOGGER.error("Can not generate PDF file for review: "
                    + pdfFile.getPath());
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

    private long getLongValue(Object p_obj)
    {
        if(p_obj instanceof Long)
            return (Long)p_obj;
        
        return Long.valueOf((String)p_obj);
    }
}

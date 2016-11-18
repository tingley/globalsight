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
package com.globalsight.everest.webapp.pagehandler.offline.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class SimpleUploadPageHandler extends PageHandler
{
    /**
     * Invokes this EntryPageHandler object.
     * 
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param theRequest
     *            the original request sent from the browser
     * @param theResponse
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDesc,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession httpSession = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) httpSession
                .getAttribute(SESSION_MANAGER);

        // Fix for GBS-2291, from Simple Upload Page
        sessionMgr.setAttribute(UPLOAD_ORIGIN, UPLOAD_FROMSIMPLEUPLOAD);

        // Get state, must be non-null.
        String action = (String) p_request.getParameter(UPLOAD_ACTION);
        OEMProcessStatus status = (OEMProcessStatus) sessionMgr
                .getAttribute(UPLOAD_STATUS);

        // process the uploaded content
        if (action != null)
        {
            UploadPageHandlerHelper uploadPageHandlerHelper = new UploadPageHandlerHelper();
            if (action.equals(CHECK_UPLOAD_FILE_TYPE))
            {
                try
                {
                    String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
                    MultipartFormDataReader reader = new MultipartFormDataReader();
                    File tempFile = reader.uploadToTempFile(p_request);
                    File savedDir = new File(AmbFileStoragePathUtils.getUploadDir()
                            + "/checkUploadFile/");
                    savedDir.mkdirs();
                    File uploadFile = new File(savedDir, reader.getFilename());
                    if (!tempFile.renameTo(uploadFile))
                    {
                        FileUtils.copyFile(tempFile, uploadFile);
                    }
                    tempFile.delete();

                    ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
                    p_response.setContentType("text/html;charset=UTF-8");
                    ServletOutputStream out = p_response.getOutputStream();
                    List<File> uploadFileList = new ArrayList<File>();
                    uploadFileList.add(uploadFile);
                    String disableUploadFileTypes = CompanyWrapper.getCompanyById(
            				currentCompanyId).getDisableUploadFileTypes();
                    List<File> canNotUploadFiles = null;
                    if (StringUtil.isNotEmptyAndNull(disableUploadFileTypes))
            		{
                    	Set<String> disableUploadFileTypeSet = StringUtil
                    			.split(disableUploadFileTypes);
                    	canNotUploadFiles = FileUtil.isDisableUploadFileType(
                    			disableUploadFileTypeSet, uploadFileList);
            		}
                    
                    if (canNotUploadFiles != null && canNotUploadFiles.size() > 0)
                    {
                        out.write(((bundle.getString("lb_message_check_upload_file_type") + CompanyWrapper
                                .getCompanyById(currentCompanyId).getDisableUploadFileTypes()))
                                .getBytes("UTF-8"));
                        for (File file : canNotUploadFiles)
                        {
                            file.delete();
                        }
                    }
                    else
                    {
                        out.write(("notContain").getBytes("UTF-8"));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
            else if (action.equals(UPLOAD_ACTION_START_UPLOAD))
            {
                uploadPageHandlerHelper.processRequest(p_request, status);
            }
            else if (action.equals(UPLOAD_ACTION_PROGRESS))
            {
                uploadPageHandlerHelper.uploadProcess(p_request, p_response,
                        sessionMgr);
                return;
            }
            else if (action.equals(UPLOAD_ACTION_REFRESH))
            {
                uploadPageHandlerHelper.refreshProcessStatus(p_response,
                        sessionMgr, status);
                return;
            }
            else if(action.equals(TASK_ACTION_TRANSLATED_TEXT_RETRIEVE))
			{
				uploadPageHandlerHelper.translatedText(p_request,p_response);
				return;
			}
            else if (action.equals(UPLOAD_ACTION_CANCE_PROGRESS))
            {
                uploadPageHandlerHelper.cancelProcess(p_request, p_response, sessionMgr);
                return;
            }
            else if (action.equals(UPLOAD_ACTION_CONFIRM_CONTINUE))
            {
                String s = p_request.getParameter("isContinue");
                status.setIsContinue("y".equals(s));
                return;
            }
        }
        super.invokePageHandler(p_pageDesc, p_request, p_response, p_context);
    }
}

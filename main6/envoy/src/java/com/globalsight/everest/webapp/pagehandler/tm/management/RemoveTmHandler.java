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

package com.globalsight.everest.webapp.pagehandler.tm.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.TmRemover;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.StringUtil;

/**
 * <p>
 * This PageHandler is responsible for removing TMs.
 * </p>
 */

public class RemoveTmHandler extends PageHandler implements WebAppConstants
{
    private static final Logger logger = Logger
            .getLogger(RemoveTmHandler.class);

    private static ProjectHandler projectHandler = null;
    String m_userId;

    public RemoveTmHandler()
    {
        super();

        if (projectHandler == null)
        {
            try
            {
                projectHandler = ServerProxy.getProjectHandler();
            }
            catch (Exception ex)
            {
                logger.error("Initialization failed.", ex);
            }
        }
    }

    /**
     * Invoke this PageHandler.
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String action = (String) p_request.getParameter(TM_ACTION);
        ResourceBundle bundle = PageHandler.getBundle(session);
        String errorMsg = null;
        StringBuilder errors = new StringBuilder();
        
        try
        {
            if (TM_ACTION_DELETE.equals(action)
                    || TM_ACTION_DELETE_LANGUAGE.equals(action)
                    || TM_ACTION_DELETE_TULISTING.equals(action))
            {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1024000);
                ServletFileUpload upload = new ServletFileUpload(factory);

                List<FileItem> fileItems = upload.parseRequest(p_request);

                String tmIdArray = (String) p_request.getParameter(TM_TM_ID);
                String language = null;
                File tmxFile = null;
                for (FileItem item : fileItems)
                {
                    if (TM_TM_ID.equals(item.getFieldName()))
                    {
                        tmIdArray = item.getString();
                    }
                    else if ("tmxFile".equals(item.getFieldName()))
                    {
                        tmxFile = File.createTempFile("GSTUListing", null);
                        String fileName = item.getName();
                        item.write(tmxFile);
                    }
                    else if ("LanguageList".equals(item.getFieldName()))
                    {
                        language = item.getString();
                    }
                }

                String[] tmIds = tmIdArray.split(",");
                if (!TM_ACTION_DELETE_LANGUAGE.equals(action))
                {
                    language = null;
                }

                long tmId = -1l;
                errorMsg = removeTM(sessionMgr, tmIds, bundle, language,
                        tmxFile);
            }
            else if (TM_ACTION_CANCEL.equals(action))
            {
                TmRemover tmRemover = (TmRemover) sessionMgr
                        .getAttribute(TM_REMOVER);
                tmRemover.cancelProcess();
            }
        }
        catch (Throwable ex)
        {
            logger.error("Tm removal error", ex);
            sessionMgr.setAttribute(TM_ERROR, ex.getMessage());
        }

        sessionMgr.setAttribute(TM_ERROR, errorMsg);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
    
    private String removeTM(SessionManager sessionManager, String[] tmIds,
            ResourceBundle bundle, String languageList, File tmxFile)
    {
        logger.info("Removing TM (tm id = " + Arrays.toString(tmIds) + ")");

        ArrayList<String> tmIdsArray = new ArrayList<String>();
        if (tmIds != null) {
            for (String tmp : tmIds)
                tmIdsArray.add(tmp);
        }
        
        String errorMsg = null;
        try
        {
            // Start remove TM in a separate thread.
            TmRemover tmRemover = new TmRemover(tmIdsArray, m_userId);
            
            if (tmxFile != null && tmxFile.exists())
            {
                tmRemover.setTmxFile(tmxFile);
                tmRemover.SetDeleteTUListingFlag(true);
                tmRemover.SetDeleteLanguageFlag(false);
            }
            else if (!StringUtil.isEmpty(languageList))
            {
                tmRemover.setLocaleId(Long.parseLong(languageList));
                tmRemover.SetDeleteTUListingFlag(false);
                tmRemover.SetDeleteLanguageFlag(true);
            }
            else
            {
                tmRemover.SetDeleteTUListingFlag(false);
                tmRemover.SetDeleteLanguageFlag(false);
            }
            tmRemover.setResourceBundle(bundle);
            tmRemover.initReplacingMessage();
            sessionManager.setAttribute(TM_REMOVER, tmRemover);

            tmRemover.start();
        }
        catch (Exception e)
        {
            logger.error("Tm removal error", e);
            errorMsg = e.getMessage();
        }

        return errorMsg;
    }
}

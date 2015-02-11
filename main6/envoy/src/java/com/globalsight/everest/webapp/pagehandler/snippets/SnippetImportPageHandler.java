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

package com.globalsight.everest.webapp.pagehandler.snippets;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.snippet.SnippetLibrary;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This PageHandler is responsible for importing snippet files into
 * the snippet library.</p>
 */

public class SnippetImportPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SnippetImportPageHandler.class);

    //
    // Static Members
    //
    static private SnippetLibrary s_library = null;

    //
    // Constructor
    //
    public SnippetImportPageHandler()
    {
        super();

        if (s_library == null)
        {
            try
            {
                s_library = ServerProxy.getSnippetLibrary();
            }
            catch (GeneralException ex)
            {
                CATEGORY.error("Initialization failed.", ex);
            }
        }
    }


    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        String userId = getUser(session).getUserId();

        String action  = (String)p_request.getParameter(TERMBASE_ACTION);
        String options = (String)p_request.getParameter(TERMBASE_IMPORT_OPTIONS);

        IImportManager importer =
            (IImportManager)sessionMgr.getAttribute(TERMBASE_IMPORTER);
        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TERMBASE_STATUS);

        try
        {
            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
            }

            if (action == null || action.equals(TERMBASE_ACTION_IMPORT))
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing import");
                }

                importer = s_library.getImporter(userId);
                options = importer.getImportOptions();

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);
                sessionMgr.setAttribute(TERMBASE_IMPORTER, importer);
            }
            else if (action.equals(TERMBASE_ACTION_UPLOAD_FILE))
            {
                // Read file and options from upload request,
                // then pass to importer.
                FileUploadHelper o_upload = new FileUploadHelper();
                o_upload.doUpload(p_request);

                importer.setImportOptions(o_upload.getImportOptions());
                importer.setImportFile(o_upload.getSavedFilepath(), false);

                options = importer.analyzeFile();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("upload options = " + options);
                }

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

                // Snippet import has no screen after upload but
                // starts the test import right away.
                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    importer.attachListener(status);
                    importer.doTestImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("test import error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_ANALYZE_FILE))
            {
                // pass down new options from client (won't reanalyze files)
                importer.setImportOptions(options);

                // then retrieve new options
                options = importer.analyzeFile();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("analysis options = " + options);
                }

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TERMBASE_ACTION_SET_IMPORT_OPTIONS))
            {
                // pass down new options from client (won't reanalyze files)

                // testrun may come here without setting options
                if (options != null)
                {
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("set options = " + options);
                    }

                    importer.setImportOptions(options);
                }
                else
                {
                    options = importer.getImportOptions();
                }

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL_IMPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TERMBASE_ACTION_TEST_IMPORT))
            {
                // Actually this is never called during snippet import.
                // See TERMBASE_ACTION_UPLOAD_FILE above which does
                // testImport.
                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    importer.attachListener(status);
                    importer.doTestImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("test import error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_START_IMPORT))
            {
                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    importer.attachListener(status);
                    importer.doImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("import error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL)  ||
                action.equals(TERMBASE_ACTION_DONE))
            {
                // we don't come here, but this is the cleanup code.
                sessionMgr.removeElement(TERMBASE_IMPORTER);
                sessionMgr.removeElement(TERMBASE_IMPORT_OPTIONS);
                sessionMgr.removeElement(TERMBASE_STATUS);
            }
        }
        catch (ImporterException ex)
        {
            CATEGORY.error("import error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }
        catch (Throwable ex)
        {
            CATEGORY.error("import error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

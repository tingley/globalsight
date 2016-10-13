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

package com.globalsight.everest.webapp.pagehandler.terminology.management;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.l18n.L18nable;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.importer.ImportManager;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.progress.ProcessStatus2;

/**
 * <p>This PageHandler is responsible for importing data into
 * termbases.</p>
 */

public class TermbaseImportPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermbaseImportPageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseImportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
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
        ResourceBundle bundle = PageHandler.getBundle(session);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        String userId = getUser(session).getUserId();

        String action  = (String)p_request.getParameter(TERMBASE_ACTION);
        String options = (String)p_request.getParameter(TERMBASE_IMPORT_OPTIONS);
        String tbid    = (String)p_request.getParameter(RADIO_BUTTON);
        if(action.equals(TERMBASE_ACTION_REFRESH)){
        	String warning = (String)p_request.getParameter("warning");
        	p_request.setAttribute("warning", warning);
        }
        String name    = null;

        ImportManager importer = (ImportManager) sessionMgr
                .getAttribute(TERMBASE_IMPORTER);
        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TERMBASE_STATUS);
	    //fix for GBS-2080
        ProcessStatus2 reindext_status = (ProcessStatus2) sessionMgr
                .getAttribute(TERMBASE_REINDEX_STATUS);

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
            }

            if (action.equals(TERMBASE_ACTION_IMPORT))
            {
            	if (tbid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing import");
                }

                ITermbase tb = s_manager.connect(name, userId, "");
                String definition = tb.getDefinition();

                importer = (ImportManager) tb.getImporter();
                options = importer.getImportOptions();

                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);
                sessionMgr.setAttribute(TERMBASE_IMPORTER, importer);
            }
            else if (action.equals(TERMBASE_ACTION_UPLOAD_FILE))
            {
				if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // Read file and options from upload request,
                // then pass to importer.
                FileUploadHelper o_upload = new FileUploadHelper();
                o_upload.doUpload(p_request);

                importer.setImportOptions(o_upload.getImportOptions());
                importer.setImportFile(o_upload.getSavedFilepath(), false);

                if (importer instanceof L18nable)
                {
                    L18nable l18nable = (L18nable) importer;
                    l18nable.setBundle(bundle);
                }

                // Should be a separate process with progress bar, see
                // TERMBASE_ACTION_ANALYZE_FILE.
                options = importer.analyzeFile();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("upload options = " + options);
                }

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

                // remove ProcessStatus if it still exists in the session
                // from the previous import
                sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
            }
            else if (action.equals(TERMBASE_ACTION_UPLOAD_IMPORT_EXCEL_FILE))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
            	// Read file and options from upload request,
                // then pass to importer.
                FileUploadHelper o_upload = new FileUploadHelper();
                o_upload.doUpload(p_request);

                importer.setImportOptions(o_upload.getImportOptions());
                importer.setImportFile(o_upload.getSavedFilepath(), false);

                // Should be a separate process with progress bar, see
                // TERMBASE_ACTION_ANALYZE_FILE.
                options = importer.analyzeFile();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("upload and import excel options = " + options);
                }

                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

                // remove ProcessStatus if it still exists in the session
                // from the previous import
                sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
                // pass down new options from analyzing excel file.
                importer.setImportOptions(options);

                // start the import in a separate thread
                try
                {
                    status = new ProcessStatus();
                    status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    //fix for GBS-2080
                    reindext_status = new ProcessStatus2();
                    reindext_status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_REINDEX_STATUS,
                            reindext_status);

                    importer.setReindexStatus(reindext_status);
                    importer.attachListener(status);
                    importer.doImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("import error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_ANALYZE_FILE))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
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
            else if (action.equals(TERMBASE_ACTION_CANCEL_IMPORT_TEST) ||
                action.equals(TERMBASE_ACTION_CANCEL_IMPORT))
            {
                status.interrupt();
				//fix for GBS-2080
                reindext_status.interrupt();
                String tbName = (String) sessionMgr
                        .getAttribute(TERMBASE_TB_NAME);
                ITermbase tb = s_manager.connect(tbName, userId, "");
                String definition = tb.getDefinition();

                importer = (ImportManager) tb.getImporter();
                options = importer.getImportOptions();
                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

            }
            else if (action.equals(TERMBASE_ACTION_SET_IMPORT_OPTIONS))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
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
            else if (action.equals(TERMBASE_ACTION_TEST_IMPORT))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("**TESTING** import with options = " +
                        options);
                }

                // pass down new options from client
                importer.setImportOptions(options);
                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

                // start the test import in a separate thread
                try
                {
                    status = new ProcessStatus();
                    status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    //fix for GBS-2080
                    reindext_status = new ProcessStatus2();
                    reindext_status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_REINDEX_STATUS,
                            reindext_status);

                    importer.setReindexStatus(reindext_status);
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
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running import with options = " + options);
                }

                // pass down new options from client
                importer.setImportOptions(options);
                sessionMgr.setAttribute(TERMBASE_IMPORT_OPTIONS, options);

                // start the import in a separate thread
                try
                {
                    status = new ProcessStatus();
                    status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    //fix for GBS-2080
                    reindext_status = new ProcessStatus2();
                    reindext_status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_REINDEX_STATUS,
                            reindext_status);

                    importer.setReindexStatus(reindext_status);
                    importer.attachListener(status);
                    importer.doImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("import error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL) ||
                action.equals(TERMBASE_ACTION_DONE))
            {
                // we don't come here, but this is the cleanup code.
                sessionMgr.removeElement(TERMBASE_DEFINITION);
                sessionMgr.removeElement(TERMBASE_IMPORTER);
                sessionMgr.removeElement(TERMBASE_IMPORT_OPTIONS);
                sessionMgr.removeElement(TERMBASE_STATUS);
            }
        }
        catch (TermbaseException ex)
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

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

package com.globalsight.everest.webapp.pagehandler.administration.projects;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.projects.FileUploadHelper;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;

import com.globalsight.importer.IImportManager;

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
 * <p>This PageHandler is responsible for importing project data.</p>
 */

public class ProjectImportPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ProjectImportPageHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler s_manager = null;

    //
    // Constructor
    //
    public ProjectImportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler();
            }
            catch (/*General*/Exception ex)
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

        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);
        String userId = getUser(session).getUserId();

        String action  = (String)p_request.getParameter(TM_ACTION);
        String options = (String)p_request.getParameter(TM_IMPORT_OPTIONS);
        String tmid     = (String)p_request.getParameter(RADIO_BUTTON);
        String name = null;
        Project project = null;

        IImportManager importer =
            (IImportManager)sessionMgr.getAttribute(TM_IMPORTER);
        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TM_TM_STATUS);

        try
        {
            if (tmid != null)
            {
                project = s_manager.getProjectById(Long.parseLong(tmid));
                name = project.getName();
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
            }

            if (action.equals(TM_ACTION_IMPORT))
            {
            	if (tmid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=projects");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing import");
                }

                importer = s_manager.getProjectDataImportManager(
                    getUser(session), Long.parseLong(tmid));

                options = importer.getImportOptions();

                sessionMgr.setAttribute(TM_TM_NAME, name);
                sessionMgr.setAttribute(TM_TM_ID, tmid);
                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
                sessionMgr.setAttribute(TM_IMPORTER, importer);

                sessionMgr.removeElement(TM_TM_STATUS);
            }
            else if (action.equals(TM_ACTION_UPLOAD_FILE))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
        		{
        			p_response
        					.sendRedirect("/globalsight/ControlServlet?activityName=projects");
        			return;
        		}
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

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);

                // remove ProcessStatus if it still exists in the
                // session from the previous import
                sessionMgr.removeElement(TM_TM_STATUS);
            }
            else if (action.equals(TM_ACTION_ANALYZE_FILE))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
        		{
        			p_response
        					.sendRedirect("/globalsight/ControlServlet?activityName=projects");
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

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_CANCEL_IMPORT_TEST) ||
                action.equals(TM_ACTION_CANCEL_IMPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TM_ACTION_SET_IMPORT_OPTIONS))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
        		{
        			p_response
        					.sendRedirect("/globalsight/ControlServlet?activityName=projects");
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

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running import with options = " + options);
                }

                // pass down new options from client
                importer.setImportOptions(options);

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running import with options = " + options);
                }

                // pass down new options from client
                importer.setImportOptions(options);
                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);

                // start the import in a separate thread
                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TM_TM_STATUS, status);

                    importer.attachListener((IProcessStatusListener)status);
                    importer.doImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("import error occured ", ex);
                }
            }
            else if (action.equals(TM_ACTION_TEST_IMPORT))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
        		{
        			p_response
        					.sendRedirect("/globalsight/ControlServlet?activityName=projects");
        			return;
        		}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("**TESTING** import with options = " +
                        options);
                }

                // pass down new options from client
                importer.setImportOptions(options);

                // start the test import in a separate thread
                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TM_TM_STATUS, status);

                    importer.attachListener((IProcessStatusListener)status);
                    importer.doTestImport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("test import error occured ", ex);
                }

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_CANCEL)  ||
                action.equals(TM_ACTION_DONE))
            {
                // we don't come here, do we??
                sessionMgr.removeElement(TM_IMPORTER);
                sessionMgr.removeElement(TM_IMPORT_OPTIONS);
                sessionMgr.removeElement(TM_TM_STATUS);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("import error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

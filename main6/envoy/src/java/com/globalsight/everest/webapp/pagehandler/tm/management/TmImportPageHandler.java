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

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.importer.IImportManager;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.progress.ProgressReporter;
import com.globalsight.util.progress.TmProcessStatus;
import com.sun.jndi.toolkit.url.UrlUtil;

/**
 * <p>
 * This PageHandler is responsible for importing data into termbases.
 * </p>
 */

public class TmImportPageHandler extends PageHandler implements
        WebAppConstants, ProgressReporter
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmImportPageHandler.class);

    static private ProjectHandler /* TmManager */s_manager = null;

    static public final String LOG_URL = "logUrl";
    static public final String ERROR_URL = "errorUrl";

    private String progressMessage = "";
    private int progressPercent = 0;
    private ResourceBundle bundle = null;
    private Session hsession = null;
    private Transaction transaction = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    public TmImportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler() /* getTmManager() */;
            }
            catch (/* General */Exception ex)
            {
                CATEGORY.error("Initialization failed.", ex);
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
        this.request = p_request;
        this.response = p_response;

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        bundle = getBundle(session);

        String userId = getUser(session).getUserId();

        String action = (String) p_request.getParameter(TM_ACTION);
        String options = (String) p_request.getParameter(TM_IMPORT_OPTIONS);
        String tmid = (String) p_request.getParameter(RADIO_TM_ID);
        String name = null;
        Tm tm = null;

        IImportManager importer = (IImportManager) sessionMgr
                .getAttribute(TM_IMPORTER);
        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);
        boolean isConverting = false;
        try
        {
            if (tmid != null)
            {
                tm = s_manager.getProjectTMById(Long.parseLong(tmid), false);
                name = tm.getName();
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
                int index = options.indexOf("</importOptions>");
                if (index > 0)
                {
                    options = options.substring(0,
                            index + "</importOptions>".length());
                }
            }

            if (action.equals(TM_ACTION_IMPORT))
            {
                if (tmid == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing import");
                }

                importer = TmManagerLocal.getProjectTmImporter(name);
                options = importer.getImportOptions();

                sessionMgr.setAttribute(TM_TM_NAME, name);
                sessionMgr.setAttribute(TM_TM_ID, tmid);
                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
                sessionMgr.setAttribute(TM_IMPORTER, importer);
            }
            else if (action.equals(TM_ACTION_CONVERT))
            {
                // Convert selected TM from TM2 to TM3
                if (tmid == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServer?activityName=tm");
                    return;
                }
                sessionMgr.setAttribute(TM_TM_NAME, name);
                sessionMgr.setAttribute(TM_TM_ID, tmid);
                progressPercent = 0;

                long tm2Id = 0;
                try
                {
                    tm2Id = Long.parseLong(tmid);
                }
                catch (Exception e)
                {
                    tm2Id = 0;
                }
                try
                {
                    ProjectTM oldTm = (ProjectTM) HibernateUtil.get(
                            ProjectTM.class, tm2Id);
                    if (oldTm == null)
                        return;
                    long companyId = 0;
                    try
                    {
                        companyId = oldTm.getCompanyId();
                    }
                    catch (NumberFormatException e)
                    {
                        companyId = 0;
                    }

                    Tm3ConvertHelper tm3Convert = new Tm3ConvertHelper(
                            companyId, oldTm, this);
                    sessionMgr.setAttribute("tm3Convert", tm3Convert);
                    tm3Convert.convert();
                }
                catch (Exception e)
                {
                    progressMessage = "Error: " + e.getMessage();
                    progressPercent = 100;
                }
            }
            else if (action.equals(TM_ACTION_CONVERT_CANCEL))
            {
                progressMessage = "Cancel the conversion...";
                try
                {
                    Tm3ConvertHelper tm3ConvertHelper = (Tm3ConvertHelper) sessionMgr
                            .getAttribute("tm3Convert");
                    if (tm3ConvertHelper != null)
                        tm3ConvertHelper.cancel();
                }
                catch (Exception e)
                {
                    CATEGORY.error("Error in canceling conversion"
                            + e.getMessage());
                }
            }
            else if (action.equals(TM_ACTION_UPLOAD_FILE))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                TmProcessStatus tmStatus = new TmProcessStatus();
                sessionMgr.setAttribute(TM_UPLOAD_STATUS, tmStatus);

                FileUploadHelper uploader = new FileUploadHelper();
                uploader.attachListener(tmStatus);
                uploader.uploadWithValidation(p_request);
            }
            else if (TM_ACTION_VALIDATION_REFRESH.equals(action))
            {
                TmProcessStatus tmStatus = (TmProcessStatus) sessionMgr
                        .getAttribute(TM_UPLOAD_STATUS);

                p_response.setHeader("Charset", "UTF-8");
                ServletOutputStream os = p_response.getOutputStream();
                String resultMsg;
                if (tmStatus != null && !tmStatus.isCanceled())
                {
                    StringBuilder result = new StringBuilder(
                            tmStatus.getDisplaySize());
                    result.append("|")
                            .append(Integer.toString(tmStatus.getPercentage()))
                            .append("|").append(tmStatus.getMessage())
                            .append("|")
                            .append(Boolean.toString(tmStatus.isFinished()));

                    resultMsg = result.toString();
                }
                else
                {
                    resultMsg = "end";
                }

                os.write(resultMsg.getBytes("UTF-8"));
                os.close();

                return;
            }
            else if (TM_ACTION_CONVERT_REFRESH.equals(action))
            {
                p_response.setHeader("Charset", "UTF-8");
                ServletOutputStream os = p_response.getOutputStream();
                String resultMsg;
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("In TmImportPageHandler == "
                            + progressPercent);
                }
                if (progressPercent > 0)
                {
                    StringBuilder result = new StringBuilder();
                    result.append(progressPercent).append("|")
                            .append(progressMessage).append("|");
                    if (progressPercent == 100)
                        result.append("true");
                    else
                        result.append("false");
                    resultMsg = result.toString();
                }
                else
                {
                    resultMsg = "0|Prepared|false";
                }
                os.write(resultMsg.getBytes("UTF-8"));
                os.close();

                return;
            }
            else if (TM_ACTION_IMPORT_FILE.equals(action))
            {
                TmProcessStatus tmStatus = (TmProcessStatus) sessionMgr
                        .getAttribute(TM_UPLOAD_STATUS);

                importer.setImportOptions(tmStatus.getImportOptions());
                importer.setImportFile(tmStatus.getSavedFilepath(), true);

                options = importer.analyzeFile();
                StringBuffer sb = new StringBuffer();
                sb.append("/globalsight/tmImport/");
                sb.append(getUser(session).getCompanyName());
                sb.append("/" + AmbFileStoragePathUtils.TM_IMPORT_FILE_SUB_DIR);
                String filePathName = tmStatus.getLogUrl().replace('\\', '/');
                String fileName = filePathName.substring(
                        filePathName.lastIndexOf("/"), filePathName.length());
                sb.append(fileName);
                String logUrl = sb.toString();
                logUrl = logUrl.replace('\\', '/');
                try
                {
                    logUrl = UrlUtil.encode(logUrl, "utf-8");
                }
                catch (Exception e)
                {
                    logUrl = URLEncoder.encode(logUrl, "utf-8");
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("upload options = " + options);
                }
                sessionMgr.setAttribute(ImportUtil.TOTAL_COUNT,
                        tmStatus.getTotalTus());
                sessionMgr.setAttribute(ImportUtil.ERROR_COUNT,
                        tmStatus.getErrorTus());
                sessionMgr.setAttribute(LOG_URL, logUrl);
                sessionMgr.setAttribute(ERROR_URL, tmStatus.getErrorUrl());

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
                sessionMgr.removeElement(WebAppConstants.TM_TM_STATUS);
            }
            else if (action.equals(TM_ACTION_ANALYZE_FILE))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
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
            else if (action.equals(TM_ACTION_CANCEL_IMPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TM_ACTION_SET_IMPORT_OPTIONS))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
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

                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_TEST_IMPORT))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("**TESTING** import with options = "
                            + options);
                }

                // pass down new options from client
                importer.setImportOptions(options);

                // Let the jsp page run the import.
                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_START_IMPORT))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=tm");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running import with options = " + options);
                }

                // pass down new options from client
                importer.setImportOptions(options);

                // attach the listener and start import in a
                // separate thread.
                sessionMgr.setAttribute(TM_IMPORT_OPTIONS, options);

                try
                {
                    status = new ProcessStatus();
                    status.setResourceBundle(bundle);
                    sessionMgr.setAttribute(TM_TM_STATUS, status);

                    importer.attachListener(status);
                    importer.doImport();
                    OperationLog.log(userId, OperationLog.EVENT_TM_IMPORT, OperationLog.COMPONET_TM, (String)sessionMgr.getAttribute(TM_TM_NAME));
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("import error occured ", ex);
                }
            }
            else if (action.equals(TM_ACTION_CANCEL)
                    || action.equals(TM_ACTION_DONE))
            {
                // we don't come here, do we??
                sessionMgr.removeElement(TM_DEFINITION);
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

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    @Override
    public void setMessageKey(String messageKey, String defaultMessage)
    {
        if (bundle == null || StringUtil.isEmpty(messageKey))
            this.progressMessage = defaultMessage;
        else
        {
            if (bundle != null && bundle.getString(messageKey) != null)
                this.progressMessage = bundle.getString(messageKey);
            else
                this.progressMessage = defaultMessage;
        }
    }

    @Override
    public void setPercentage(int percentage)
    {
        this.progressPercent = percentage;
    }
}

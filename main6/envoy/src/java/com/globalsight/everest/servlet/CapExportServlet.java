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
package com.globalsight.everest.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.filesystem.Exporter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportEventObserverHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.log.ActivityLog;
import com.globalsight.util.GeneralException;

/**
 * <P>
 * This servlet is responsible for the following export processes:
 * 1. Performing an action upon an export result received from CXE.
 * This is basically CXE's asynchronous response to export process and
 * CAP will only complete the export process based on the response type
 * sent from CXE (success or failure).
 *
 * 2. Performing the process of dynamic preview.
 * The export process for preview is completed by sending a populated
 * Gxml to CXE, and then sending out the CXE's response for display
 * purposes.
 * </P>
 */
public class CapExportServlet extends HttpServlet
{
    private static final long serialVersionUID = 3601378874412599201L;

    private static final Logger c_logger =
        Logger.getLogger(
            CapExportServlet.class.getName());

    public static final String EXPORT_STATUS = "33";
    public static final String PREVIEW_STATUS = "34";

    private static String s_cxeServletUrl = null;


    /**
     * A convenience method which can be overridden.
     * @exception ServletException - Defines a general exception a
     * servlet can throw when it encounters difficulty.
     */
    public void init()
        throws ServletException
    {
        super.init();

        // set the cxe's servlet URL only once.
        s_cxeServletUrl = getCxeServletUrl();
    }

    /**
     * Called by the servlet container to indicate to a servlet that
     * the servlet is being taken out of service.
     */
    public void destroy()
    {
        super.destroy();
    }


    /**
     * <p>Process HTTP requests. Only processes the "POST" Method.</P>
     *
     * @param p_request The HttpServletRequest.
     * @param p_response The HttpServletResponse.
     * @exception IOException - Signals that an I/O related error has
     * occured.
     * @exception ServletException - Defines a general exception a
     * servlet can throw when it encounters difficulty.
     */
    public void doGet(HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse).
     *
     * <P>This POST action is responsible for two actions:</P>
     *
     * 1. Page Export Result: Basically it's the asynchronous
     * notification of CAP about the result of a page export.  Request
     * contains a message id, response type (success/failure), and a
     * response details upon a failure.  Note that the response
     * details is a serialized GeneralException and is only send if
     * the export process has failed.
     *
     * 2. Dynamic Preview: This is a request from CXE.  The request
     * contains request type (i.e. preview), message id, Tuv ids, and
     * UI locale.  The result of this process is a generated template
     * which is populated with valid segments for the given tuv ids
     * and a placeholder for the rest of tuvs.
     *
     * </P>
     * @param p_request The HttpServletRequest.
     * @param p_response The HttpServletResponse.
     * @exception IOException - Signals that an I/O related error has occurred.
     * @exception ServletException - Defines a general exception a servlet can
     * throw when it encounters difficulty.
     */
    public void doPost(HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        boolean isLogging = c_logger.isDebugEnabled();
        
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (companyName != null)
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }
        
        String requestType =
            p_request.getParameter(ExportConstants.CXE_REQUEST_TYPE);

        String dataSourceType =
            p_request.getParameter(ExportConstants.DATA_SOURCE_TYPE);

        // Note that message id is the Page Id (i.e. source page id,
        // target page id, or STF id).
        String messageId =
            p_request.getParameter(ExportConstants.MESSAGE_ID);

        if (isLogging)
        {
            c_logger.debug("CapExportServlet.doPost(), requestType=" +
                requestType + ", messageId=" + messageId);
        }

        Map<Object,Object> activityArgs = new HashMap<Object,Object>();
        activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID, companyName);
        activityArgs.put(ExportConstants.CXE_REQUEST_TYPE, requestType);
        activityArgs.put(ExportConstants.DATA_SOURCE_TYPE, dataSourceType);
        activityArgs.put(ExportConstants.MESSAGE_ID, messageId);
        ActivityLog.Start activityStart = ActivityLog.start(
            CapExportServlet.class, "doPost", activityArgs);
        try
        {
            // if request is export for preview
            if (ExportConstants.PREVIEW.equals(requestType))
            {
                if (ExportConstants.MEDIASURFACE.equals(dataSourceType))
                {
                    c_logger.debug("CapExportServlet.doPost(), PREVIEW_REQUEST, calling exportForDynamicPreview()");
                    exportForDynamicPreview(p_request, p_response,
                                            Long.valueOf(messageId));
                }
                else
                {
                    c_logger.debug("CapExportServlet.doPost(), PREVIEW_REQUEST, calling exportForPreview()");
                    //this may be used only for db preview??
                    exportForPreview(p_request, p_response, messageId);
                }
            }
            else if (EXPORT_STATUS.equals(requestType))
            {
                c_logger.debug("CapExportServlet.doPost(), EXPORT_STATUS, calling handlePageRequest()");
                handlePageRequest(p_request, p_response, messageId);
            }
            else if (PREVIEW_STATUS.equals(requestType))
            {
                c_logger.debug("CapExportServlet.doPost(), PREVIEW_STATUS, doing nothing");
            }
        }
        finally
        {
            activityStart.end();
        }
    }



    //
    // Local Methods
    //

    /**
     * request is for a "dynamic preview".  Get a template based on
     * the given tuvIds.
     */
    private void exportForPreview(HttpServletRequest p_request,
        HttpServletResponse p_response, String p_pageId)
        throws ServletException
    {
        try
        {
            long id = 0;
            String uiLocale = p_request.getParameter(
                ExportConstants.UI_LOCALE);
            String[] tuvIdValues =
                p_request.getParameterValues(ExportConstants.TUV_ID);
            // Page Manager expects a List of tuvIds as Long objects
            int size = tuvIdValues.length;
            List<Long> tuvIds = new ArrayList<Long>();

            for (int i = 0; i < size; i++)
            {
                Long tuvId = new Long(tuvIdValues[i]);
                tuvIds.add(tuvId);
            }

            try
            {
                id = Long.parseLong(p_pageId);
            }
            catch (NumberFormatException ne)
            {
                // value was not numeric.  Keep it 0
            }

            String line = ServerProxy.getPageManager().exportForPreview(
                id, tuvIds, uiLocale);

            p_response.setContentType("text/html");
            PrintWriter out = p_response.getWriter();

            URL url = new URL(s_cxeServletUrl);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(
                conn.getOutputStream(), ExportConstants.UTF8);

            wr.write(line);
            wr.flush();
            wr.close();

            // CXE's response...
            BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

            // CXE will only send a valid xml text for display...
            // (will go to proxy servlet.)
            while ((line = rd.readLine()) != null)
            {
                out.println(line);
            }

            rd.close();
            out.close();
        }
        catch(Exception e)
        {
            c_logger.error("CapExportServlet - exportForPreview problem.", e);
            throw new ServletException(e);
        }
    }

    private void exportForDynamicPreview(HttpServletRequest p_request,
        HttpServletResponse p_response, Long p_pageId)
        throws ServletException
    {
        try
        {
            TargetPage targetPage = ServerProxy.getPageManager().getTargetPage(
                p_pageId.longValue());

            Workflow wf = targetPage.getWorkflowInstance();
            // TODO: empty list?
            List<Long> ids = new ArrayList<Long>();
            boolean isTargetPage = true;

            // Register export event
            String exportType = 
                wf.getState().equals(Workflow.LOCALIZED) ? 
                    ExportBatchEvent.FINAL_PRIMARY : 
                            ExportBatchEvent.INTERIM_PRIMARY;
            long exportBatchId = 
                ExportEventObserverHelper.notifyBeginExportTargetPage(
                    wf.getJob(), ExportEventObserverHelper.getUser(p_request),
                    p_pageId.longValue(), wf.getIdAsLong(), null, exportType);
             
            ServerProxy.getPageManager().exportPage(
                    new ExportParameters(targetPage), ids, isTargetPage,
                    exportBatchId);

            Collection<Tuv> tuvCollect = ServerProxy.getTuvManager().
                getTargetTuvsForStatistics(targetPage);
            List<Long> tuvIds = new ArrayList<Long>();
            for (Tuv tuv : tuvCollect)
            {
                tuvIds.add(tuv.getIdAsLong());
            }

            String uiLocale = p_request.getParameter(ExportConstants.UI_LOCALE);
            String line = ServerProxy.getPageManager().exportForPreview(
                    p_pageId.longValue(), tuvIds, uiLocale);

            p_response.setContentType("text/html");
            PrintWriter out = p_response.getWriter();

            URL url = new URL(s_cxeServletUrl);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(
                conn.getOutputStream());

            wr.write(line);
            wr.flush();
            wr.close();

            // CXE's response...
            BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

            // CXE will only send a valid xml text for display...
            // (will go to proxy servlet.)
            while ((line = rd.readLine()) != null)
            {
                out.println(line);
            }

            rd.close();
            out.close();
        }
        catch (Exception e)
        {
            c_logger.error("Dynamic Preview Failed",e);
            throw new ServletException(e);
        }
    }


    // return the URL to CXE's export servlet...
    private String getCxeServletUrl()
        throws ServletException
    {
        StringBuffer sb = new StringBuffer();

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            sb.append("http://");
            sb.append(sc.getStringParameter(
                SystemConfiguration.SERVER_HOST));
            sb.append(":");
            sb.append(sc.getStringParameter(
                SystemConfiguration.SERVER_PORT));
            sb.append(sc.getStringParameter(
                SystemConfiguration.CXE_SERVLET_URL));
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }

        return sb.toString();
    }

    /**
     * Request is coming form CxeExportServlet (asynchronous response
     * to an export).
     */
    private void handlePageRequest(HttpServletRequest p_request,
        HttpServletResponse p_response, String p_pageId)
        throws IOException, ServletException
    {
        try
        {
            String originalCxeRequestType = p_request.getParameter(
                ExportConstants.ORIG_CXE_REQUEST_TYPE);
    
            long pageId = Long.parseLong(p_pageId);
//            PageManagerLocal.EXPORTING_TARGET_PAGE.remove(pageId);
            
            String responseType = p_request.getParameter(
                ExportConstants.RESPONSE_TYPE);
    
            if (originalCxeRequestType != null &&
                originalCxeRequestType.equals(
                    ExportConstants.EXPORT_FOR_UPDATE))
            {
                handleSourcePageRequest(pageId, p_request, responseType);
            }
            else if (originalCxeRequestType != null && 
                     originalCxeRequestType.equals(
                         ExportConstants.EXPORT_STF))
            {
                handleStfRequest(pageId, p_request, responseType);
            }
            else
            {
                handleTargetPageRequest(pageId, p_request, responseType);
            }

            // Notify export event observer
            notifyPageExportComplete(p_request, p_pageId);
        }
        catch (Exception ne)
        {
            c_logger.error("CapExportServlet -  error in handlePageRequest()",ne);
            throw new ServletException(ne);
        }

        // output to cxe's servlet just to have something to spit back
	PrintWriter outputToPageExport = p_response.getWriter();
        outputToPageExport.println("Received Export Status.");
        outputToPageExport.close();
    }


    // process the source page export (export for update)
    private void handleSourcePageRequest(long pageId, 
                                         HttpServletRequest p_request,
                                         String responseType)
        throws Exception
    {
        SourcePage sp = ServerProxy.getPageManager().
            getSourcePage(pageId);
        
        if (responseType.equals(ExportConstants.FAILURE))
        {
            c_logger.error("NOTIFYING ABOUT FAILED EXPORT FOR UPDATE");

            ServerProxy.getPageEventObserver().
                notifyExportForUpdateFailEvent(sp);            
        }
        else if (responseType.equals(ExportConstants.SUCCESS))
        {
            c_logger.info("Notifying about successful export for update.");
            ServerProxy.getPageEventObserver().
                notifyExportForUpdateSuccessEvent(sp);
        }
    }


    // process the result of the exported secondary target file.
    private void handleStfRequest(long p_pageId, 
                            HttpServletRequest p_request,
                            String p_responseType)
        throws Exception
    {

        if (p_responseType.equals(ExportConstants.FAILURE))
        {
            ServerProxy.getSecondaryTargetFileManager().
                notifyExportFailEvent(new Long(p_pageId));
            WorkflowExportingHelper.setStfAsNotExporting(p_pageId);         
        }
        else if (p_responseType.equals(ExportConstants.SUCCESS))
        {
            
            SecondaryTargetFile stf = 
                ServerProxy.getSecondaryTargetFileManager().
                getSecondaryTargetFile(p_pageId);
            // an interim export can happen during dispatch without
            // updating any states.  So only update the STF if it's
            // current state is EXPORT_IN_PROGRESS
            if (stf.getState().equals(
                SecondaryTargetFileState.EXPORT_IN_PROGRESS))
            {          
                ServerProxy.getSecondaryTargetFileManager().
                    notifyExportSuccessEvent(new Long(p_pageId));
            }
        }
    }
       

    // process the result of the exported target page.
    private void handleTargetPageRequest(long p_pageId, 
                                         HttpServletRequest p_request,
                                         String p_responseType)
        throws Exception
    {
        TargetPage tp = ServerProxy.getPageManager().
            getTargetPage(p_pageId);
        String state = null;
        if (p_responseType.equals(ExportConstants.FAILURE))
        {
            state = Workflow.EXPORT_FAILED;
            // details will always be a GeneralException
            String details =
                p_request.getParameter(ExportConstants.RESPONSE_DETAILS);

            GeneralException ge = GeneralException.deserialize(details);
            c_logger.error("CapExportServlet - Export for page id " +
                p_pageId + " failed.", ge);
            
            String xmlDtdId = p_request.getParameter(Exporter.XML_DTD_ID);
            Long id = Long.parseLong(xmlDtdId);
            
            // Xml dtd validation failed
            if (id > 0)
            {
                XmlDtdManager.sendEmail(tp, XmlDtdManager.EXPORT);
            }

            // Id > 0 means it is because xml dtd validation failed.
            ServerProxy.getPageEventObserver().notifyExportFailEvent(tp,
                    details, id < 1);
        }
        else if (p_responseType.equals(ExportConstants.SUCCESS))
        {
            
            state = Workflow.EXPORTED;
            // an interim export can happen during dispatch without
            // updating any states.
            // so only update the target page if it is marked in the
            // process of exporting
            if (tp.getPageState().equals(PageState.EXPORT_IN_PROGRESS))
            {
                ServerProxy.getPageEventObserver().
                    notifyExportSuccessEvent(tp);
            }
        }
        snedEmail(tp, state);
    }


    private void snedEmail(TargetPage targetPage, String state)
            throws ProjectHandlerException, RemoteException, GeneralException,
            NamingException
    {
        Job job = targetPage.getWorkflowInstance().getJob();
        ServerProxy.getWorkflowServer().advanceWorkFlowNotification(
                targetPage.getWorkflowInstance().getId() + job.getJobName(),
                state);
    }

    // Let the export event observer set the state of the exported page to either
    // successful or failed.
    private void notifyPageExportComplete(HttpServletRequest p_request, String p_pageId)
        throws Exception
    {
        String exportBatchId = 
            p_request.getParameter(ExportConstants.EXPORT_BATCH_ID);

        ExportEventObserverHelper.notifyPageExportComplete(
            Long.parseLong(exportBatchId), p_pageId, p_request);
    }
}

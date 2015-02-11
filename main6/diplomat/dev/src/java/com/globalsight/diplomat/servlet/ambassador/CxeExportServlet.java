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
package com.globalsight.diplomat.servlet.ambassador;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Locale;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.hornetq.api.jms.HornetQJMSConstants;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.cap.CapAdapterException;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.previewUrlXml.PreviewUrlXmlParser;
import com.globalsight.diplomat.util.previewUrlXml.UrlElement;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.ErrorBean;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.NumberUtil;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * The CxeExportServlet receives POSTs of GXML files It handles export for image
 * replace and dynamic preview.
 */
public class CxeExportServlet extends HttpServlet
{
    private static Logger s_logger = Logger.getLogger(CxeExportServlet.class);

    /**
     * Holds multiple return values from waitForPreviewMessage(). Holds the
     * information needed to do a preview.
     */
    private class PreviewInfoHolder
    {
        String previewUrlXml; // the preview url xml
        String modEventFlowXml; // the modified event flow xml (after going
                                // through cxe)
    }

    public CxeExportServlet() throws ServletException
    {
    }

    /**
     * Receives a POST request for handling image replace and dynamic preview
     */
    public void doPost(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException,
            ServletException
    {
        // The POST requests for image replacement export are processed by
        // image replacement export servlet. If it is one of those request
        // forward it accordingly and don't do anything here.
        //
        if (isImageReplaceRequest(p_request, p_response))
        {
            return;
        }

        try
        {
            CompanyThreadLocal.getInstance().setValue(
                    UserUtil.getCurrentCompanyName(p_request));

            exportForDynamicPreview(p_request);
            PreviewInfoHolder info = waitForPreviewMessage(p_request);
            String previewUrlXml = info.previewUrlXml;
            String modEventFlowXml = info.modEventFlowXml;
            setSessionValues(p_request);

            if (previewUrlXml != null && previewUrlXml.length() > 1)
            {
                s_logger.info("Showing preview.");
                showPreview(p_request, p_response, modEventFlowXml,
                        previewUrlXml);
            }
            else
            {
                s_logger.info("Showing error page.");
                // database write status is not ok either because of a
                // db problem or a preview url xml problem
                GeneralException ge = new GeneralException(
                        "Could not get dynamic preview.");
                showErrorPage(p_request, p_response, ge);
            }
        }
        catch (Exception ex)
        {
            s_logger.error("Problem performing dynamic preview.", ex);
            CapAdapterException cae = new CapAdapterException(
                    "ExportServletEx", new String[0], ex);
            p_response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = p_response.getWriter();
            try
            {
                out.println(cae.serialize());
            }
            catch (GeneralException ge)
            {
                s_logger.error("Problem creating GeneralException", ge);
            }
            out.close();
        }
    }

    private boolean isImageReplaceRequest(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        if (p_request.getParameter(ExportConstants.IMAGE_FILENAME) != null
                && p_request.getParameter(ExportConstants.IMAGE_DATA) != null)
        {
            s_logger.info("Forwarding the export request to the ImageReplace servlet.");
            getServletContext().getNamedDispatcher("ImageReplace").forward(
                    p_request, p_response);

            return true;
        }

        return false;
    }

    /**
     * Sets some values in the session like lang and locale
     * 
     * @param p_request
     */
    private void setSessionValues(HttpServletRequest p_request)
    {
        HttpSession theSession = p_request.getSession();
        String uilang = p_request.getParameter(ExportConstants.UI_LOCALE);
        if (uilang == null)
            uilang = "en_US"; // use English as a default ui lang
        theSession.setAttribute("uilang", uilang);
        Locale uilocale = GlobalSightLocale.makeLocaleFromString(uilang);
        theSession.setAttribute(WebAppConstants.UILOCALE, uilocale);
    }

    private void showPreview(HttpServletRequest p_request,
            HttpServletResponse p_response, String p_eventFlowXml,
            String p_previewUrlXml) throws ServletException
    {
        HttpSession theSession = p_request.getSession();
        try
        {
            // first get source and target locales
            String srcLocale;
            String tgtLocale;
            StringReader sr = new StringReader(p_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            Element elem = parser.getDocument().getDocumentElement();
            Element srcElement = (Element) elem.getElementsByTagName("source")
                    .item(0);
            Element tgtElement = (Element) elem.getElementsByTagName("target")
                    .item(0);
            srcLocale = ((Element) srcElement.getElementsByTagName("locale")
                    .item(0)).getFirstChild().getNodeValue();
            tgtLocale = ((Element) tgtElement.getElementsByTagName("locale")
                    .item(0)).getFirstChild().getNodeValue();
            s_logger.debug("SrcLocale is " + srcLocale);
            s_logger.debug("TgtLocale is " + tgtLocale);

            // spit out HTML for a page with two frames. One for source URLs and
            // one for Target URLs
            PreviewUrlXmlParser p = new PreviewUrlXmlParser(p_previewUrlXml,
                    srcLocale, tgtLocale);
            p.parse();
            UrlElement[] srcUrls = p.getSourceUrls();
            UrlElement[] tgtUrls = p.getTargetUrls();
            s_logger.debug("CES: There are " + srcUrls.length
                    + " source URLs and " + tgtUrls.length + " target Urls.");

            // create a unique ID based on the session Id
            // and put the arrays into the UrlListStash
            String srcUrlListId = new String("S" + theSession.getId());
            String tgtUrlListId = new String("T" + theSession.getId());

            // now put the vectors into the ServletStash singleton
            // this used to put two objects into the stash because each frame
            // would
            // pull out a separate copy. we now cannot use the session to
            // pass information between the frames used for preview...so the
            // singleton
            // has to have a copy of the URLs and they can never be removed....
            // this is a cleanup problem.
            ServletStash theServletStash = ServletStash.getServletStash();
            theServletStash.put(srcUrlListId, srcUrls, 1);
            theServletStash.put(tgtUrlListId, tgtUrls, 1);

            // put this information into the servlet session
            theSession.setAttribute("srcLocale", srcLocale);
            theSession.setAttribute("tgtLocale", tgtLocale);

            theSession
                    .setAttribute("srcEncoding", p.getSourceUrlListEncoding());
            theSession
                    .setAttribute("tgtEncoding", p.getTargetUrlListEncoding());

            theSession.setAttribute("srcUrlListId", srcUrlListId);
            theSession.setAttribute("tgtUrlListId", tgtUrlListId);
            theSession.setAttribute("numSrcUrls", new Integer(srcUrls.length));
            theSession.setAttribute("numTgtUrls", new Integer(tgtUrls.length));

            SystemConfiguration config = SystemConfiguration.getInstance();
            String useSSL = config.getStringParameter("useSSL");
            String nonSSLPort = config.getStringParameter("nonSSLPort");
            String SSLPort = config.getStringParameter("SSLPort");
            String cxeServer = null;

            if (useSSL.equals("true"))
                cxeServer = "https://" + p_request.getServerName() + ":"
                        + SSLPort;
            else
                cxeServer = "http://" + p_request.getServerName() + ":"
                        + nonSSLPort;

            theSession.setAttribute("cxeServer", cxeServer);

            // now forward to a JSP which will render the HTML output
            String displayPage = "/cxe/jsp/PreviewUrlFrame.jsp";
            RequestDispatcher requestDispatcher = getServletContext()
                    .getRequestDispatcher(displayPage);
            if (requestDispatcher != null)
                requestDispatcher.forward(p_request, p_response);
            else
            {
                s_logger.warn("Cannot present preview URL page.");
            }

        }
        catch (Exception e)
        {
            s_logger.error("Cannot handle the Preview URL XML.", e);
            try
            {
                showErrorPage(p_request, p_response, new CapAdapterException(
                        "PreviewProblems", null, e));
            }
            catch (Exception ex)
            {
                s_logger.error("Could not show error page.", ex);
                throw new ServletException(e.getMessage());
            }
        }
    }

    // shows the System4 error page
    private void showErrorPage(HttpServletRequest p_request,
            HttpServletResponse p_response, GeneralException p_generalException)
            throws Exception
    {
        EnvoyServletException ese = new EnvoyServletException(
                EnvoyServletException.EX_GENERAL, p_generalException);
        ErrorBean errorBean = new ErrorBean(0, p_generalException.getMessage(),
                ese);

        p_request.setAttribute(WebAppConstants.ERROR_BEAN_NAME, errorBean);
        getServletContext().getRequestDispatcher(WebAppConstants.ERROR_PAGE)
                .forward(p_request, p_response);
    }

    /**
     * Performs the actual export.
     * 
     * @param p_request
     * @exception Exception
     */
    private void exportForDynamicPreview(HttpServletRequest p_request)
            throws Exception
    {
        String eventFlowXml = p_request
                .getParameter(ExportConstants.EVENT_FLOW_XML);
        String gxml = p_request.getParameter(ExportConstants.GXML);
        String cxeRequestType = p_request
                .getParameter(ExportConstants.CXE_REQUEST_TYPE);
        String targetLocale = p_request
                .getParameter(ExportConstants.TARGET_LOCALE);
        String targetCharset = p_request
                .getParameter(ExportConstants.TARGET_CODESET);
        int bomType = NumberUtil.convertToInt(p_request
                .getParameter(ExportConstants.BOM_TYPE));
        String messageId = p_request.getParameter(ExportConstants.MESSAGE_ID);
        String exportLocation = p_request
                .getParameter(ExportConstants.EXPORT_LOCATION);
        String localeSubDir = p_request
                .getParameter(ExportConstants.LOCALE_SUBDIR);
        String exportBatchId = p_request
                .getParameter(ExportConstants.EXPORT_BATCH_ID);
        Integer pageCount = Integer.valueOf(p_request
                .getParameter(ExportConstants.PAGE_COUNT));
        Integer pageNum = Integer.valueOf(p_request
                .getParameter(ExportConstants.PAGE_NUM));
        String sessionId = p_request.getSession().getId();

        if (exportLocation == null)
        {
            // SystemConfiguration config = SystemConfiguration.getInstance();
            // exportLocation =
            // config.getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR);
            exportLocation = AmbFileStoragePathUtils.getCxeDocDirPath();
            s_logger.warn("ExportLocation is null, using cxe docs dir "
                    + exportLocation);
        }

        if (localeSubDir == null)
        {
            localeSubDir = targetLocale;
            s_logger.warn("LocaleSubDir is null, using target locale "
                    + localeSubDir);
        }

        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        BufferedOutputStream bos = new BufferedOutputStream(
                fmd.getOutputStream());
        OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF8");
        osw.write(gxml, 0, gxml.length());
        osw.close();

        s_logger.info("Publishing preview export request to CXE with messageId "
                + messageId);
        CxeProxy.exportForDynamicPreview(eventFlowXml, fmd, cxeRequestType,
                targetLocale, targetCharset, bomType, messageId,
                exportLocation, localeSubDir, exportBatchId, pageCount,
                pageNum, sessionId);
    }

    /**
     * Waits for the JMS return message to come. That message should have the
     * sessionId as a property and should contain the PreviewUrlXml
     * 
     * @param p_request
     * @return PreviewInfoHolder
     * @throws Exception
     */
    private PreviewInfoHolder waitForPreviewMessage(HttpServletRequest p_request)
            throws Exception
    {
        String sessionId = p_request.getSession().getId();
        s_logger.info("Waiting up to 5 minutes for dynamic preview reply for sessionId "
                + sessionId);
        // now wait for the reply if we're doing a preview
        InitialContext context = new InitialContext();
        TopicConnectionFactory cf = (TopicConnectionFactory) context
                .lookup(JmsHelper.JMS_TOPIC_FACTORY_NAME);
        TopicConnection topicConnection = cf.createTopicConnection();
        topicConnection.start();
        TopicSession session = topicConnection.createTopicSession(false,
                HornetQJMSConstants.PRE_ACKNOWLEDGE);
        String jmsTopicName = EventTopicMap.JMS_PREFIX
                + EventTopicMap.FOR_DYNAMIC_PREVIEW;

        AppServerWrapper s_appServerWrapper = AppServerWrapperFactory
                .getAppServerWrapper();
        if (s_appServerWrapper.getJ2EEServerName().equals(
                AppServerWrapperFactory.JBOSS))
        {
            jmsTopicName = EventTopicMap.TOPIC_PREFIX_JBOSS + jmsTopicName;
        }
        Topic topic = session.createTopic(jmsTopicName);
        String messageSelector = "SESSIONID='" + sessionId + "'";
        TopicSubscriber subscriber = session.createSubscriber(topic,
                messageSelector, false);
        long timeout = 5L * 60L * 1000L; // five minute timeout
        Message jmsMessage = subscriber.receive(timeout);
        if (jmsMessage == null)
            throw new Exception(
                    "Timeout waiting for JMS message with preview information. Waited 5 minutes.");

        ObjectMessage omsg = (ObjectMessage) jmsMessage;
        CxeMessage cxeMessage = (CxeMessage) omsg.getObject();
        PreviewInfoHolder info = new PreviewInfoHolder();
        info.previewUrlXml = (String) cxeMessage.getParameters().get(
                "PreviewUrlXml");
        info.modEventFlowXml = cxeMessage.getEventFlowXml();
        return info;
    }
}

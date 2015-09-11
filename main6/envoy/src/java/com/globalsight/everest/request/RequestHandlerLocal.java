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

package com.globalsight.everest.request;

// globalsight
import java.io.BufferedReader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.entity.databaseprofile.DatabaseProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.jobhandler.jobcreation.JobCreator;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.reimport.ActivePageReimporter;
import com.globalsight.everest.request.reimport.ReimporterException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.mail.MailerConstants;

/**
 * This is the concrete implementation of the RequestHandler.
 */
public class RequestHandlerLocal implements RequestHandler
{
    // for logging purposes
    private static Logger c_logger = Logger
            .getLogger(RequestHandlerLocal.class);

    // used for email messages - keys into property file
    // subject and message keys for all import failures
    private static final String IMPORT_FAILED_MESSAGE = "importFailedMessage";

    // max size (in bytes) of the job name
    public static final int MAX_JOBNAME_SIZE = 320;

    // max size (in bytes) of the external page id
    public static final int MAX_EXTERNAL_PAGE_ID_SIZE = 255;

    // handle to the reimporter for pages that are active and being re-imported
    private ActivePageReimporter m_reimporter = null;

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * constructor
     */
    public RequestHandlerLocal()
    {
        super();
        m_reimporter = ActivePageReimporter.getInstance();
    }

    /**
     * @see RequestHandler.findRequest(long)
     */
    public Request findRequest(long p_id) throws RequestHandlerException,
            RemoteException
    {
        return RequestPersistenceAccessor.findRequest(p_id);
    }

    public WorkflowRequest findWorkflowRequest(long p_id)
            throws RequestHandlerException, RemoteException
    {
        return RequestPersistenceAccessor.findWorkflowRequestById(p_id);
    }

    /**
     * Used by the service activator MDB. Prepares and submits a request for
     * localization. (This used to be the onMessage() functionality)
     */
    public void prepareAndSubmitRequest(HashMap p_hashmap,
            String p_contentFileName, int p_requestType, String p_eventFlowXml,
            GeneralException p_exception, String p_l10nRequestXml)
            throws RemoteException, RequestHandlerException
    {
        RequestImpl req = null;
        try
        {
            // 1.Create the request object
            req = prepareL10nRequest(p_requestType, p_contentFileName,
                    p_l10nRequestXml, p_eventFlowXml, p_exception);

            // 2.Submit request
            submitRequest(req);
        }
        catch (RequestHandlerException rhe)
        {
            c_logger.error(
                    "Failed to submit the request. "
                            + p_l10nRequestXml.toString(), rhe);

            // send an email to admin: at this point nothing is persisted yet.
            String[] attachments = null;

            if (p_exception != null)
            {
                attachments = new String[3];
                attachments[0] = p_contentFileName;
                attachments[1] = p_l10nRequestXml;
                try
                {
                    attachments[2] = p_exception.serialize();
                }
                catch (Exception e2)
                {
                    c_logger.error("Could not serialize exception.", e2);
                }
            }
            else
            {
                attachments = new String[2];
                attachments[0] = p_contentFileName;
                attachments[1] = p_l10nRequestXml;
            }

            String[] messageArgs = null;
            try
            {
                messageArgs = getMessageArgs(req, rhe);
            }
            catch (Exception e)
            {
                // failed to get message args
                c_logger.error(
                        "Could not get message arguments for import failure.",
                        e);
            }

            sendEmailToAdmin(IMPORT_FAILED_MESSAGE, messageArgs, attachments,
                    String.valueOf(req.getCompanyId()));
            throw rhe;
        }
        catch (Exception e)
        {
            c_logger.error("Can not get projecthandler to attain workflow infos");
        }
    }

    /**
     * @see RequestHandler.setExceptionInRequest(Request p_request,
     *      GeneralException p_exception)
     */
    public void setExceptionInRequest(Request p_request,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        RequestPersistenceAccessor
                .setExceptionInRequest(p_request, p_exception);
    }

    /**
     * @see RequestHandler.setExceptionInRequest(long p_requestId,
     *      GeneralException p_exception)
     */
    public void setExceptionInRequest(long p_requestId,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        RequestPersistenceAccessor.setExceptionInRequest(p_requestId,
                p_exception);
    }

    public void setExceptionInWorkflowRequest(WorkflowRequest p_request,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        RequestPersistenceAccessor.setExceptionInWorkflowRequest(p_request,
                p_exception);
    }

    public long createWorkflowRequest(WorkflowRequest p_request, Job p_job,
            Collection p_workflowTemplates) throws GeneralException,
            RemoteException
    {
        return RequestPersistenceAccessor.insertWorkflowRequest(p_request,
                p_job, p_workflowTemplates);
    }

    /**
     * @see RequestHandler.importPage(Request) method
     */
    public void importPage(Request p_request) throws RequestHandlerException,
            RemoteException
    {
        // just sends the request onto the job creator this imports all the page
        // content and adds it to a job.
        CompanyThreadLocal.getInstance().setIdValue(
                String.valueOf(p_request.getCompanyId()));
        JobCreator jc = getJobCreator();
        try
        {
            // During shutting down server,may return a null for "jc",so add
            // judgment to avoid throw exception.
            if (jc != null)
            {
                jc.addRequestToJob(p_request);
            }
        }
        catch (Exception ex)
        {
            c_logger.error(
                    "JobCreator.addRequestToJob. " + p_request.toString(), ex);

            try
            {
                sendMailFromAdmin(p_request);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see RequestHandler.getDataSourceNameOfRequest(Request p_request)
     */
    public String getDataSourceNameOfRequest(Request p_request)
            throws RequestHandlerException, RemoteException
    {
        String dsName = null;

        // if from a database - then call the database profile
        if (p_request.getDataSourceType() != null)
        {
            if (p_request.getDataSourceType().equals("db"))
            {
                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Looking for the data source name of a "
                            + "database profile for request "
                            + p_request.getId());
                }

                // get the database profile persistence manager
                DatabaseProfilePersistenceManager dbProfMgr = null;
                try
                {
                    dbProfMgr = ServerProxy
                            .getDatabaseProfilePersistenceManager();
                }
                catch (GeneralException ge)
                {
                    throw new RequestHandlerException(
                            RequestHandlerException.MSG_FAILED_TO_FIND_DB_PROFILE_MANAGER,
                            null, ge);
                }
                try
                {
                    DatabaseProfile dbProfile = dbProfMgr
                            .getDatabaseProfile(p_request.getDataSourceId());
                    dsName = dbProfile.getName();
                }
                catch (DatabaseProfileEntityException dpee)
                {
                    String args[] = new String[2];
                    args[0] = Long.toString(p_request.getDataSourceId());
                    args[1] = Long.toString(p_request.getId());
                    throw new RequestHandlerException(
                            RequestHandlerException.MSG_FAILED_TO_GET_DATA_SOURCE_NAME,
                            args, dpee);
                }
            }
            else
            {
                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Looking for the data source name of a "
                            + "file profile for request " + p_request.getId());
                }

                // get the file system profile persistence manager
                FileProfilePersistenceManager fileProfMgr = null;
                try
                {
                    fileProfMgr = ServerProxy
                            .getFileProfilePersistenceManager();
                }
                catch (Exception e)
                {
                    throw new RequestHandlerException(
                            RequestHandlerException.MSG_FAILED_TO_FIND_FILE_PROFILE_MANAGER,
                            null, e);
                }
                try
                {
                    FileProfile fileProfile = fileProfMgr
                            .readFileProfile(p_request.getDataSourceId());
                    dsName = fileProfile.getName();
                }
                catch (FileProfileEntityException fpee)
                {
                    String args[] = new String[2];
                    args[0] = Long.toString(p_request.getDataSourceId());
                    args[1] = Long.toString(p_request.getId());
                    throw new RequestHandlerException(
                            RequestHandlerException.MSG_FAILED_TO_GET_DATA_SOURCE_NAME,
                            args, fpee);
                }
            }
        }
        else
        {
            String args[] = new String[2];
            args[0] = Long.toString(-1);
            args[1] = Long.toString(p_request.getId());
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_GET_DATA_SOURCE_NAME,
                    args, null);
        }

        return dsName;
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.startDelayedImports
     */
    public void startDelayedImports() throws RemoteException,
            RequestHandlerException
    {
        try
        {
            m_reimporter.startDelayedImports();
        }
        catch (ReimporterException re)
        {
            // messages are logged in call to reimporter
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_START_DELAYED_IMPORTS,
                    null, re);
        }
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.cleanupIncompleteRequests()
     */
    public void cleanupIncompleteRequests() throws RemoteException,
            RequestHandlerException
    {
        c_logger.info("Starting cleanup of incomplete imports....");
        // handleIncompletePages();
        // handleIncompleteRequests();
        JobCreationMonitor.cleanupIncompleteJobs();
        c_logger.info("....done cleaning up incomplete imports.");
    }

    public FileProfile getFileProfile(Request p_request)
    {
        FileProfile fileProfile = null;
        // get the file system profile persistence manager
        FileProfilePersistenceManager fileProfMgr = null;
        try
        {
            fileProfMgr = ServerProxy.getFileProfilePersistenceManager();
            fileProfile = fileProfMgr.readFileProfile(p_request
                    .getDataSourceId());
        }
        catch (Exception e)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_FILE_PROFILE_MANAGER,
                    null, e);
        }
        return fileProfile;
    }

    /**
     * Creates a request from all the information passed in. Must parse through
     * the L10nRequest Xml. This also stores some of the information of the
     * request that won't be part of the page.
     */
    private RequestImpl prepareL10nRequest(int p_requestType, String p_gxml,
            String p_l10nRequestXml, String p_eventFlowXml,
            GeneralException p_exception) throws RequestHandlerException
    {
        // Parse through the L10nRequestXml
        BufferedReader br = new BufferedReader(new StringReader(
                p_l10nRequestXml));

        L10nRequestXmlParser parser = new L10nRequestXmlParser(br);

        // get the attributes - they could be NULL if they weren't found.

        // external page id
        String externalPageId = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.UNIQUE_PAGE_NAME);

        // is page previewable - convert to boolean
        String isPreviewable = parser.findAttributeInXml(
                CxeToCapRequest.L10nRequestXml.UNIQUE_PAGE_NAME,
                CxeToCapRequest.L10nRequestXml.IS_PAGE_PREVIEWABLE);
        boolean isPageCxePreviewable = false;
        if (isPreviewable != null && isPreviewable.equals(CxeToCapRequest.TRUE))
        {
            isPageCxePreviewable = true;
        }
        // l10nProfileId
        String l10nProfileIdAsString = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.L10N_PROFILE_ID);
        long l10nProfileId = -1;
        if (l10nProfileIdAsString != null)
        {
            l10nProfileId = Long.parseLong(l10nProfileIdAsString);
        }
        // source encoding
        String originalSourceEncoding = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.SOURCE_CODE_SET);
        // data source type
        String dataSourceType = parser.findAttributeInXml(
                CxeToCapRequest.L10nRequestXml.L10N_REQUEST_XML,
                CxeToCapRequest.L10nRequestXml.DATA_SOURCE_TYPE);
        // data source id
        String dataSourceIdString = parser.findAttributeInXml(
                CxeToCapRequest.L10nRequestXml.L10N_REQUEST_XML,
                CxeToCapRequest.L10nRequestXml.DATA_SOURCE_ID);
        long dataSourceId = -1;
        if (dataSourceIdString != null && dataSourceIdString.length() > 0)
        {
            dataSourceId = Long.parseLong(dataSourceIdString);
        }

        String priority = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.JOB_PRIORITY);

        String baseHref = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.BASE_HREF);

        if (c_logger.isDebugEnabled())
        {
            String debugMessage = "Parameters found: " + externalPageId + " "
                    + l10nProfileId + " " + originalSourceEncoding;
            debugMessage += " " + dataSourceType + " " + dataSourceId + " ";

            c_logger.debug(debugMessage);
        }

        String importInitiatorId = null;

        L10nProfile profile = null;
        // get the localization profile
        try
        {
            ProjectHandler ph = getProjectHandler();
            profile = ph.getL10nProfile(l10nProfileId);
        }
        catch (ProjectHandlerException phe)
        {
            String args[] = new String[1];
            args[0] = Long.toString(l10nProfileId);
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_GET_L10N_PROFILE,
                    args, phe);
        }
        catch (RemoteException re)
        {
            String args[] = new String[1];
            args[0] = Long.toString(l10nProfileId);
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_GET_L10N_PROFILE,
                    args, re);
        }

        // Create a request
        RequestImpl r = RequestFactory.createRequest(p_requestType, profile,
                p_gxml, p_eventFlowXml, p_exception);

        // set the values that were sent in and not part of the
        // constructor
        if (externalPageId != null)
        {
            r.setExternalPageId(externalPageId);
        }

        if (originalSourceEncoding != null)
        {
            r.setSourceEncoding(originalSourceEncoding);
        }

        if (dataSourceType != null)
        {
            r.setDataSourceType(dataSourceType);
        }

        r.setPriority(priority);
        r.setDataSourceId(dataSourceId);
        r.setPageCxePreviewable(isPageCxePreviewable);
        r.setBaseHref(baseHref);

        String originalSourceFileContent = parser
                .findElementInXml(CxeToCapRequest.L10nRequestXml.ORIGINAL_SOURCE_FILE_CONTENT);
        r.setOriginalSourceFileContent(originalSourceFileContent);

        // set target locales
        setUnimportedLocales(p_eventFlowXml, r);

        // if the profile is set to batch requests add the batch
        // information
        if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.BATCH_CONDITION)
        {
            String batchId = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.BATCH_ID);
            String pageCount = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.PAGE_COUNT);
            String pageNumber = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.PAGE_NUMBER);
            String docPageCount = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.DOC_PAGE_COUNT);
            String docPageNumber = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.DOC_PAGE_NUMBER);
            String jobPrefixName = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.JOB_PREFIX_NAME);
            importInitiatorId = parser
                    .findElementInXml(CxeToCapRequest.L10nRequestXml.IMPORT_INITIATOR);

            // truncate the jobPrefixName to the max byte size in the DB
            jobPrefixName = EditUtil.truncateUTF8Len(jobPrefixName,
                    MAX_JOBNAME_SIZE);

            if (batchId != null)
            {
                BatchInfo bi = new BatchInfo(batchId,
                        Long.parseLong(pageCount), Long.parseLong(pageNumber),
                        Long.parseLong(docPageCount),
                        Long.parseLong(docPageNumber), jobPrefixName);
                r.setBatchInfo(bi);
            }

            RequestPersistenceAccessor.insertRequest(r);
        }
        else if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.WORD_COUNT_OR_TIMER_CONDITION)
        {
            RequestPersistenceAccessor.insertWordCountRequest(r);
        }
        else if (profile.getDispatchCriteria().getCondition() == DispatchCriteria.WORD_COUNT_CONDITION)
        {
            RequestPersistenceAccessor.insertWordCountRequest(r);
        }

        if (importInitiatorId != null)
        {
            c_logger.info("Received an import request from "
                    + UserUtil.getUserNameById(importInitiatorId));
        }
        return r;
    }

    private void setUnimportedLocales(String p_eventFlowXml, RequestImpl r)
    {
        try
        {
            StringReader sr = new StringReader(p_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser domParser = new DOMParser();
            domParser.setFeature("http://xml.org/sax/features/validation",
                    false);
            domParser.parse(is);
            Document document = domParser.getDocument();
            Element root = document.getDocumentElement();
            NodeList nl = root.getElementsByTagName("target");
            Element e = (Element) nl.item(0);
            nl = e.getElementsByTagName("locale");
            Element localeElement = (Element) nl.item(0);
            String targetLocales = localeElement.getFirstChild().getNodeValue();
            if (!"unknown".equalsIgnoreCase(targetLocales))
            {
                GlobalSightLocale[] gs = r.getL10nProfile().getTargetLocales();
                String[] locales = targetLocales.split(",");
                if (gs.length != locales.length)
                {
                    for (int j = 0; j < gs.length; j++)
                    {
                        GlobalSightLocale gLocale = gs[j];
                        for (int i = 0; i < locales.length; i++)
                        {
                            String localeName = locales[i];
                            if (gLocale.toString().equalsIgnoreCase(localeName))
                            {
                                break;
                            }
                            if (i == locales.length - 1)
                            {
                                r.addUnimportTargetLocale(gLocale);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Error when set target locales in RequestImpl.", e);
        }
    }

    /**
     * Initiate the importing of the created, already persisted request.
     */
    public long submitRequest(RequestImpl p_request)
            throws RequestHandlerException, RemoteException
    {
        // if this is an extracted request then check if the page
        // already exists in an active job (an active re-import)
        if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
        {
            int reimportOption = ActivePageReimporter.getReimportOption();
            switch (reimportOption)
            {
                case ActivePageReimporter.REIMPORT_NEW_TARGETS:
                    importWithReimportNewTargets(p_request);
                    break;
                case ActivePageReimporter.DELAY_REIMPORT:
                    importWithDelayReimport(p_request);
                    break;
                case ActivePageReimporter.NO_REIMPORT:
                default:
                    importWithNoReimport(p_request);
                    break;
            }
        }
        else
        {
            // import all un-extracted files
            importPage(p_request);
        }

        return p_request.getId();
    }

    /**
     * Perform the import with the "reimport new targets" option turned on.
     */
    private void importWithReimportNewTargets(Request p_request)
            throws RequestHandlerException, RemoteException
    {
        // if local content is turned on first check to see if
        // the source page exists already with snippets and is ACTIVE
        // this will fail importing
        boolean addDeleteEnabled = true;
        try
        {
            addDeleteEnabled = SystemConfiguration.getInstance()
                    .getBooleanParameter(
                            SystemConfigParamNames.ADD_DELETE_ENABLED);
        }
        catch (Exception e)
        {
            // just assume it is set to true then for the most
            // validation
            c_logger.error(
                    "Failed to get the value of is add/delete enabled from the system parameters",
                    e);
        }

        if (addDeleteEnabled)
        {
            // if the previous page is active and it contains snippets
            // fail the import
            SourcePage p = findPreviousSourcePage(p_request);

            if (m_reimporter.isActivePage(p)
                    && ((ExtractedSourceFile) p.getPrimaryFile())
                            .containGsTags())
            {
                c_logger.error("Can't import the page "
                        + p.getExternalPageId()
                        + " to any target locales because it is part of an active job and contains snippets.");
                // takes in one argument - external page id
                String[] args =
                { p.getExternalPageId() };
                RequestHandlerException rhe = new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_WITH_SNIPPETS_IN_OF_ACTIVE_JOB,
                        args, null);
                RequestPersistenceAccessor
                        .setExceptionInRequest(p_request, rhe);
            }
        }

        // look to see if there is a previous page
        // must do for each target locale
        ArrayList tps = findPreviousTargetPages(p_request);

        if (tps != null && tps.size() > 0)
        {
            for (int i = 0; i < tps.size(); i++)
            {
                TargetPage tp = (TargetPage) tps.get(i);
                if (m_reimporter.isActivePage(tp))
                {
                    p_request.addActiveTarget(tp.getGlobalSightLocale(), tp
                            .getWorkflowInstance().getJob());
                    c_logger.info("Importing - Found the page "
                            + p_request.getExternalPageId()
                            + " to still be active in the locale "
                            + tp.getGlobalSightLocale().toString());
                }
            }
            importPage(p_request);
        }
        else
        {
            importPage(p_request);
        }
    }

    /**
     * Perform the import with the "delay re-import" option turned on.
     */
    private void importWithDelayReimport(Request p_request)
            throws RequestHandlerException, RemoteException
    {
        // if the previous page is part of an active job then it is
        // a re-import of an active page.
        SourcePage p = findPreviousSourcePage(p_request);

        if (m_reimporter.isActivePage(p))
        {
            try
            {
                m_reimporter.delayImport((RequestImpl) p_request, p);
            }
            catch (ReimporterException re)
            {
                c_logger.error(
                        "An exception was thrown when attempting to delay import.",
                        re);
                throw new RequestHandlerException(re);
            }
        }
        else
        {
            importPage(p_request);
        }
    }

    /**
     * Perform the import with "no reimport" turned on. Which mean any source
     * page that is already in an active job can NOT be re-imported until the
     * previous version of it has been completly translated or cancelled.
     */
    private void importWithNoReimport(Request p_request)
            throws RequestHandlerException, RemoteException
    {
        // if the previous page is part of an active job then it is
        // a re-import of an active page.
        SourcePage p = findPreviousSourcePage(p_request);
        if (m_reimporter.isActivePage(p))
        {
            c_logger.error("Can't import the page " + p.getExternalPageId()
                    + " because it is part of an active job.");
            // takes in one argument - external page id
            String[] args =
            { p.getExternalPageId() };
            RequestHandlerException rhe = new RequestHandlerException(
                    RequestHandlerException.MSG_PAGE_ALREADY_PART_OF_ACTIVE_JOB,
                    args, null);
            RequestPersistenceAccessor.setExceptionInRequest(p_request, rhe);
        }

        // import all files - if an error will recognize the error and add the
        // request to a job as an error
        importPage(p_request);
    }

    /**
     * Find the previous valid version of the page.
     * 
     * @param p_pageName
     *            The unique name for the page.
     * @param p_l10nProfile
     *            The profile associated with this page.
     * 
     * @return The previous version or NULL if one doesn't exist.
     */
    private SourcePage findPreviousSourcePage(Request p_request)
            throws RequestHandlerException
    {
        // get the source locale from the profile
        GlobalSightLocale sourceLocale = p_request.getL10nProfile()
                .getSourceLocale();
        // find the page manager and verify if the page can be imported
        PageManager pm = getPageManager();
        SourcePage p = null;

        try
        {
            p = pm.getCurrentPageByNameAndLocale(p_request, sourceLocale);
        }
        catch (Exception e)
        {
            c_logger.error("Exception when calling "
                    + "PageManager.getCurrentPageByNameAndLocale", e);
            // just passes null back doesn't throw an exception
        }
        return p;
    }

    /**
     * Find the previous valid version of the target page. This is for each
     * target locale specified in the request.
     * 
     * @param p_pageName
     *            The unique name for the page.
     * @param p_l10nProfile
     *            The profile associated with this page.
     * 
     * @return The previous version or NULL if one doesn't exist.
     */
    private ArrayList findPreviousTargetPages(Request p_request)
            throws RequestHandlerException
    {
        // find the page manager and verify if the page can be imported
        PageManager pm = getPageManager();
        ArrayList tps = new ArrayList(0);

        try
        {
            tps = pm.getActiveTargetPagesByNameAndLocale(p_request);
        }
        catch (Exception e)
        {
            c_logger.error("Exception when calling "
                    + "PageManager.getCurrentTargetPagesByNameAndLocales", e);
            // just passes null back doesn't throw an exception
        }
        return tps;
    }

    /**
     * Takes all source pages stuck in the IMPORTING state and marks the request
     * with the appropriate error. It then uses the common importing code to
     * update the page to be an IMPORT_FAIL and add it to the appropriate job.
     */
    private void handleIncompletePages() throws RequestHandlerException
    {
        try
        {
            // find all the incomplete requests and source pages
            Collection pages = getPageManager().getSourcePagesStillImporting();
            if (pages != null && pages.size() > 0)
            {
                c_logger.info("Cleaning up "
                        + pages.size()
                        + " page(s) that were importing when the system was shutdown.");
                for (Iterator i = pages.iterator(); i.hasNext();)
                {
                    SourcePage sp = (SourcePage) i.next();

                    // get the request straight from the DB - no caching
                    // involved
                    //
                    // should not get the request from the source page
                    // "sp.getRequest()"
                    // because this will cause the request to be cached and any
                    // updates
                    // done to it using JDBC will go directly to the DB and the
                    // cache and
                    // DB will be out-of-date.
                    Request r = RequestPersistenceAccessor
                            .findRequestByPageId(sp.getId());

                    // update the request type
                    String[] args =
                    { sp.getExternalPageId() };

                    // set up the relationship that is only persisted from
                    // source page to request
                    // and not the other way - will need this for importing
                    r.setSourcePage(sp);
                    // update the request with an error exception of failing to
                    // import
                    // because of the system being shutdown
                    setExceptionInRequest(
                            r,
                            new RequestHandlerException(
                                    RequestHandlerException.MSG_FAILED_TO_COMPLETE_IMPORT_DURING_SHUTDOWN,
                                    args, null));
                    // import the page - this method handles error requests too.
                    importPage(r);
                }
            }
            else
            {
                c_logger.info("There aren't any pages stuck in importing.");
            }
        }
        catch (Exception e)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_CLEANUP_INCOMPLETE_IMPORTS,
                    null, e);
        }
    }

    /**
     * Takes all requests that don't have a source page associated with it
     * because they were importing when the system was shut-down. Marks the
     * request as an error and then uses the common importing code to update the
     * page to be an IMPORT_FAIL and add it to the appropriate job.
     */
    private void handleIncompleteRequests() throws RequestHandlerException
    {
        try
        {
            // find all the incomplete requests
            Collection requests = RequestPersistenceAccessor
                    .findRequestsStillImporting();
            if (requests != null && requests.size() > 0)
            {
                c_logger.info("Cleaning up " + requests.size()
                        + " request(s) were importing "
                        + "when they system was shutdown.");
                for (Iterator i = requests.iterator(); i.hasNext();)
                {
                    Request r = (Request) i.next();

                    // update the page name and data source type
                    /** Uses DOM to parse the event flow XML */
                    StringReader sr = new StringReader(r.getEventFlowXml());
                    InputSource is = new InputSource(sr);
                    DOMParser parser = new DOMParser();
                    parser.setFeature("http://xml.org/sax/features/validation",
                            false);
                    parser.parse(is);
                    Document document = parser.getDocument();
                    Element root = document.getDocumentElement();
                    NodeList nl = root.getElementsByTagName("displayName");
                    Element displayNameElt = (Element) nl.item(0);

                    String pageName = displayNameElt.getFirstChild()
                            .getNodeValue();
                    r.setExternalPageId(pageName);
                    Element source = (Element) root.getElementsByTagName(
                            "source").item(0);
                    String dataSourceType = source
                            .getAttribute("dataSourceType");
                    r.setDataSourceType(dataSourceType);

                    String[] args =
                    { r.getExternalPageId() };
                    // update the request with an error exception of failing to
                    // import
                    // because of the system being shutdown
                    setExceptionInRequest(
                            r,
                            new RequestHandlerException(
                                    RequestHandlerException.MSG_FAILED_TO_COMPLETE_IMPORT_DURING_SHUTDOWN,
                                    args, null));

                    importPage(r);
                }
            }
            else
            {
                c_logger.info("There aren't any requests stuck in importing.");
            }
        }
        catch (Exception e)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_CLEANUP_INCOMPLETE_IMPORTS,
                    null, e);
        }
    }

    // Sends mail from the Admin to the PM about an Import Failure
    private void sendMailFromAdmin(Request p_r) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        String[] messageArgs = getMessageArgs(p_r, null);

        L10nProfile l10nProfile = p_r.getL10nProfile();
        String companyIdStr = String.valueOf(l10nProfile.getCompanyId());
        GlobalSightLocale[] targetLocales = p_r.getTargetLocalesToImport();
        boolean shouldNotifyPm = false;
        for (int i = 0; i < targetLocales.length; i++)
        {
            WorkflowTemplateInfo wfti = l10nProfile
                    .getWorkflowTemplateInfo(targetLocales[i]);
            if (!shouldNotifyPm && wfti.notifyProjectManager())
            {
                shouldNotifyPm = true;
            }
            List userIds = wfti.getWorkflowManagerIds();
            if (userIds != null)
            {
                for (Iterator uii = userIds.iterator(); uii.hasNext();)
                {
                    String userId = (String) uii.next();
                    sendEmail(userId, messageArgs, companyIdStr);
                }
            }

        }
        // if at least one of the wfInfos had the pm notify flag on, notify PM.
        if (shouldNotifyPm)
        {
            sendEmail(l10nProfile.getProject().getProjectManagerId(),
                    messageArgs, companyIdStr);
        }
    }

    // send mail to Project manager / Workflow Manager
    private void sendEmail(String p_userId, String[] p_messageArgs,
            String p_companyIdStr) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        User user = getUserManager().getUser(p_userId);

        ServerProxy.getMailer().sendMailFromAdmin(user, p_messageArgs,
                MailerConstants.INITIAL_IMPORT_FAILED_SUBJECT,
                IMPORT_FAILED_MESSAGE, p_companyIdStr);
    }

    /*
     * Send email to the administrator. If fails - log the error.
     */
    private void sendEmailToAdmin(String p_messageKey,
            String[] p_messageArguments, String[] p_attachments,
            String p_companyIdStr)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            ServerProxy.getMailer().sendMailToAdmin(p_messageArguments,
                    MailerConstants.INITIAL_IMPORT_FAILED_SUBJECT,
                    p_messageKey, p_attachments, p_companyIdStr);
        }
        catch (Exception ge)
        {
            c_logger.error("Failed to send an email to the administrator for "
                    + MailerConstants.INITIAL_IMPORT_FAILED_SUBJECT + " "
                    + p_messageKey + " " + p_messageArguments, ge);
        }
    }

    private ProjectHandler getProjectHandler() throws RequestHandlerException
    {
        ProjectHandler ph = null;

        try
        {
            ph = ServerProxy.getProjectHandler();
        }
        catch (Exception e)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_PROJECT_HANDLER,
                    null, e);
        }

        return ph;
    }

    private PageManager getPageManager() throws RequestHandlerException
    {
        PageManager pm = null;

        try
        {
            pm = ServerProxy.getPageManager();
        }
        catch (Exception ge)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_PAGE_MANAGER,
                    null, ge);
        }

        return pm;
    }

    private JobCreator getJobCreator() throws RequestHandlerException
    {

        JobCreator jc = null;

        try
        {
            jc = ServerProxy.getJobCreator();
        }
        catch (GeneralException ge)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_JOBCREATOR,
                    null, ge);
        }

        return jc;
    }

    private UserManager getUserManager() throws RequestHandlerException
    {
        UserManager um = null;

        try
        {
            um = ServerProxy.getUserManager();
        }
        catch (Exception e)
        {
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_USER_MANAGER,
                    null, e);
        }

        return um;
    }

    /**
     * Get the error message arguments.
     */
    private String[] getMessageArgs(Request p_r, GeneralException p_exception)
            throws Exception
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        String capLoginUrl = config
                .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

        String messageArgs[] = new String[5];
        messageArgs[0] = p_r.getDataSourceType();
        messageArgs[1] = p_r.getDataSourceName();
        messageArgs[2] = p_r.getExternalPageId();

        String exceptionMsg = "";
        GeneralException exp = p_r.getException();
        if (exp != null)
        {
            exceptionMsg = exp.getLocalizedMessage();
        }
        else if (p_exception != null)
        {
            exceptionMsg = p_exception.getLocalizedMessage();
        }

        messageArgs[3] = exceptionMsg;
        messageArgs[4] = capLoginUrl;

        return messageArgs;
    }

}

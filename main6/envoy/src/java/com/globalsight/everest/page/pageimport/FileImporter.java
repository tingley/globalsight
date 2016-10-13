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

package com.globalsight.everest.page.pageimport;

//globalsight - general
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This abstract class defines the methods necessary for importing a page into
 * System4. These are the common method needed for importing any page whether
 * extracted or not.
 */
public abstract class FileImporter
{
    static protected final Logger c_logger = Logger
            .getLogger(FileImporter.class);

    // Holds on to a copy of each of the importers.
    static private final ExtractedFileImporter s_extractedImporter = new ExtractedFileImporter();
    static private final UnextractedFileImporter s_unextractedImporter = new UnextractedFileImporter();

    /**
     * This method finds the appropriate importer and performs the import.
     */
    public static HashMap importPrimaryFile(Request p_request)
            throws FileImportException
    {
        HashMap pages = null;

        switch (p_request.getType())
        {
            case Request.EXTRACTED_LOCALIZATION_REQUEST:
                pages = s_extractedImporter.importFile(p_request);
                break;
            case Request.UNEXTRACTED_LOCALIZATION_REQUEST:
                pages = s_unextractedImporter.importFile(p_request);
                break;
            default:
                // throw a page importing exception - invalid type
        }

        return pages;
    }

    /**
     * Imports the request and create Source and Target Pages from the request
     * information.
     * 
     * @return A hash map of all the pages (Source and Target) that were
     *         created. The key for the hash map is the id of the page's
     *         GlobalSightLocale.
     */
    abstract HashMap importFile(Request p_request) throws FileImportException;

    protected GlobalSightLocale getSourceLocale(Request p_request)
    {
        return p_request.getL10nProfile().getSourceLocale();
    }

    /**
     * Wraps the code for setting an exception in a request and catching the
     * appropriate exception.
     */
    protected void setExceptionInRequest(Request p_request,
            GeneralException p_exception) throws FileImportException
    {
        try
        {
            getRequestHandler().setExceptionInRequest(p_request, p_exception);
        }
        catch (Exception ex)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST,
                    args, ex);
        }
    }

    /**
     * Wraps the code for getting the page event observer and handling any
     * exceptions.
     */
    protected PageEventObserver getPageEventObserver()
            throws FileImportException
    {
        PageEventObserver result = null;

        try
        {
            result = ServerProxy.getPageEventObserver();
        }
        catch (GeneralException ex)
        {
            c_logger.error("Couldn't find the PageEventObserver", ex);
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_FIND_PAGE_EVENT_OBSERVER,
                    null, ex);
        }

        return result;
    }

    /**
     * Wraps the code for getting the page manager and handling any exceptions.
     */
    protected PageManager getPageManager() throws FileImportException
    {
        PageManager result = null;

        try
        {
            result = ServerProxy.getPageManager();
        }
        catch (GeneralException ex)
        {
            c_logger.error("Couldn't find the PageManager", ex);
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_FIND_PAGE_MANAGER, null,
                    ex);
        }

        return result;
    }

    /**
     * Wraps the code for getting the request handler and handling any
     * exceptions.
     */
    protected RequestHandler getRequestHandler() throws FileImportException
    {
        RequestHandler result = null;

        try
        {
            result = ServerProxy.getRequestHandler();
        }
        catch (GeneralException ex)
        {
            c_logger.debug("Couldn't find the RequestHandler.", ex);
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_FIND_REQUEST_HANDLER,
                    null, ex);
        }

        return result;
    }

    protected long getJobIdFromEventFlowXml(Request p_request)
    {
        long jobId = -1;
        try
        {
            EventFlowXml e = XmlUtil.string2Object(EventFlowXml.class, p_request.getEventFlowXml());
            jobId = Long.parseLong(e.getBatchInfo().getJobId());
        }
        catch (Exception e)
        {
            c_logger.error(e);
            throw new FileImportException(e);
        }
        return jobId;
    }
}

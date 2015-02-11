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
package com.globalsight.everest.jobhandler.jobcreation;

import java.util.HashMap;
import org.apache.log4j.Logger;

import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * Processes the request before adding it to a job.
 * Imports the GXML and creates the source and target pages.
 */
class RequestProcessor
{
    private static Logger c_logger =
        Logger.getLogger(
            RequestProcessor.class);

    /**
     * Process the request by importing the SourcePage and if successful,
     * creating target pages.
     *
     * @param p_request The request to process.
     *
     * @return A hash map with all the created page.  It returns a
     *         source page and possibly 1 or more target pages.
     *         The key is the locale of the page as a GlobalSightLocale object.
     */
    HashMap processRequest(Request p_request)
        throws JobCreationException
    {
        HashMap pages = null;

        switch (p_request.getType())
        {
            // Since GBS-2218
            case Request.EXTRACTED_LOCALIZATION_REQUEST:
            case Request.UNEXTRACTED_LOCALIZATION_REQUEST:
                pages = importPage(p_request);
                break;

            default:
                // otherwise an error
                SourcePage page = processErrorRequest(p_request);
                if (page != null) {
                    pages = new HashMap(1);
                    pages.put(page.getGlobalSightLocale().getIdAsLong(), page);
                } else {
                    pages = new HashMap(0);
                }
                
                break;
        }

        return pages;
    }

    /**
     * Process this request, it has already been identified as an error.
     */
    private SourcePage processErrorRequest(Request p_request)
        throws JobCreationException
    {
        SourcePage page = p_request.getSourcePage();

        c_logger.info("Processing an error request " +
            p_request.getId() + ": " +
            p_request.getExternalPageId() + ". Importing failed.");

        PageManager pm = null;

        try
        {
            pm = ServerProxy.getPageManager();
            //if a profile is specified
            if (page == null && p_request.getL10nProfile() != null)
            {
                //persist a page to hold the error
                page = pm.getPageWithUnextractedFileForImport(p_request,
                    p_request.getL10nProfile().getSourceLocale(), 0);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't create an error page for import ", e);

            String args[] = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new JobCreationException(
                JobCreationException.MSG_FAILED_TO_CREATE_ERROR_PAGE,
                args, e);
        }

        try
        {
            PageEventObserver peo = ServerProxy.getPageEventObserver();
            peo.notifyImportFailEvent(page, null);
        }
        catch (Exception e)
        {
            c_logger.error("Exception when notifying about an import failure " +
                " for page " + page.getId(), e);

            String[] args = new String[1];
            args[0] = Long.toString(page.getId());
            throw new JobCreationException(
                JobCreationException.MSG_FAILED_TO_NOTIFY_IMPORT_FAILURE,
                args, e);
        }

        return page;
    }

    /**
     * Process the request by importing the page associated with it.
     */
    private HashMap importPage(Request p_request)
        throws JobCreationException
    {
        HashMap pages = new HashMap();
        PageManager pm = null;

        try
        {
            pm = ServerProxy.getPageManager();
            pages = pm.importPage(p_request);
        }
        catch ( Exception e )
        {
            c_logger.error("Exception when calling import page " +
                "for request " + p_request.getId(), e);

            String args[] = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new JobCreationException(
                JobCreationException.MSG_IMPORT_PAGE_FAILED, args, e);
        }

        return pages;
    }

}

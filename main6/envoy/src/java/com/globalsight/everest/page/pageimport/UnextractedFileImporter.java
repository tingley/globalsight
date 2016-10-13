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

//globalsight
import com.globalsight.util.GeneralException;
import com.globalsight.everest.servlet.util.ServerProxy;

import com.globalsight.everest.nativefilestore.NativeFileManager;
import com.globalsight.everest.nativefilestore.NativeFileManagerException;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.pageimport.FileImporter;
import com.globalsight.everest.page.pageimport.FileImportException;
import com.globalsight.everest.page.pageimport.UnextractedFileImportPersistenceHandler;
import com.globalsight.everest.request.Request;
import com.globalsight.cxe.util.EventFlowXmlParser;

//java core
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class imports a file that is un-extracted. It creates the source pages
 * and target pages and copies the file to the appropriate place for user
 * access.
 */
public class UnextractedFileImporter extends FileImporter
{
    /**
     * Imports the request and create Source and Target Pages from the request
     * information.
     * 
     * @return A hash map of all the pages (Source and Target) that were
     *         created. The key for the hash map is the id of the page's
     *         GlobalSightLocale.
     */
    HashMap importFile(Request p_request) throws FileImportException
    {
        SourcePage sourcePage = null;
        Collection targetPages = null;
        HashMap pages = new HashMap();
        // the method name is still called "getGxml" - but it returns the
        // file name for un-extracted file requests
        String contentFileName = p_request.getGxml();

        try
        {
            // create and persist the source page
            sourcePage = createSourcePage(p_request);
            pages.put(sourcePage.getGlobalSightLocale().getIdAsLong(),
                    sourcePage);

            // set the modified user id to be the project manager
            // associated with this request's profile
            String userId = p_request.getL10nProfile().getProject()
                    .getProjectManagerId();

            // copy the file to the internal storage
            UnextractedFile sourceUf = (UnextractedFile) sourcePage
                    .getPrimaryFile();
            storeFile(contentFileName, sourceUf, userId, false);

            // persist the target pages
            targetPages = createTargetPages(p_request, sourcePage);

            HashMap alternateTargetPages = findAlternateTargetPages(p_request);

            // copy the file to internal storage for target pages
            // this will be an exact copy of the source page at this time.
            for (Iterator i = targetPages.iterator(); i.hasNext();)
            {
                TargetPage targetPage = (TargetPage) i.next();
                UnextractedFile uf = (UnextractedFile) targetPage
                        .getPrimaryFile();
                String localeName = targetPage.getGlobalSightLocale()
                        .toString();
                String alternateTargetPage = (String) alternateTargetPages
                        .get(localeName);

                if (alternateTargetPage != null)
                {
                    System.out.println("For locale " + localeName
                            + " using alternate target page:"
                            + alternateTargetPage);
                    storeFile(alternateTargetPage, uf, userId, true/* delete it */);
                }
                else
                {
                    if (i.hasNext())
                    {
                        storeFile(contentFileName, uf, userId, false);
                    }
                    else
                    {
                        // this is the last one - specify to delete it
                        storeFile(contentFileName, uf, userId, true);
                    }
                }

                pages.put(targetPage.getGlobalSightLocale().getIdAsLong(),
                        targetPage);
            }

            // save all the updates to the UnextractedFiles - like file
            // modification and length.
            UnextractedFileImportPersistenceHandler uf = new UnextractedFileImportPersistenceHandler();
            uf.persistObjects(sourcePage);
        }
        catch (GeneralException e)
        {
            c_logger.warn("Import failed for the un-extracted page.");
            setExceptionInRequest(p_request, e);
        }

        try
        {
            if (p_request.getType() == Request.UNEXTRACTED_LOCALIZATION_REQUEST)
            {
                getPageEventObserver().notifyImportSuccessEvent(sourcePage,
                        targetPages);
            }
            else
            {
                c_logger.info("Import failed - updating the state.");
                getPageEventObserver().notifyImportFailEvent(sourcePage,
                        targetPages);
            }
        }
        catch (Exception e)
        {
            c_logger.info("Failed to update the state of the page.");
        }

        return pages;
    }

    /**
     * Gets a mapping of locale codes to alternate target pages that should be
     * used instead of a copy of the source page.
     * 
     * @param p_request
     *            request
     * @return hashmap
     * @exception Exception
     */
    private HashMap findAlternateTargetPages(Request p_request)
            throws GeneralException
    {
        try
        {
            // first find the EventFlowXml
            String efxml = p_request.getEventFlowXml();
            EventFlowXmlParser parser = new EventFlowXmlParser();
            parser.parse(efxml);
            return parser.getAlternateTargetPages();
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Problem parsing EventFlowXml for alternate target pages.",
                    e);
            throw new GeneralException(e);
        }
    }

    /**
     * Create the source page.
     */
    protected SourcePage createSourcePage(Request p_request)
            throws FileImportException
    {
        SourcePage srcPage = null;
        try
        {
            srcPage = getPageManager().getPageWithUnextractedFileForImport(
                    p_request, getSourceLocale(p_request), 0);
        }
        catch (PageException pe)
        {
            if (srcPage == null)
            {
                c_logger.error("Couldn't get/create the page for importing.",
                        pe);
                String[] args = new String[2];
                args[0] = Long.toString(p_request.getId());
                args[1] = p_request.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_IMPORT_PAGE, args, pe);
            }
            else
            {
                c_logger.error("Exception when adding the page to "
                        + "the request.", pe);
                String[] args = new String[2];
                args[0] = Long.toString(p_request.getId());
                args[1] = srcPage.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_ADD_PAGE_TO_REQUEST,
                        args, pe);
            }
        }
        long jobId = getJobIdFromEventFlowXml(p_request);
        srcPage.setJobId(jobId);

        return srcPage;
    }

    /**
     * Creates the target pages associated with the source page and the target
     * locales specified in the L10nProfile as part of the request.
     */
    protected Collection createTargetPages(Request p_request,
            SourcePage p_sourcePage) throws FileImportException
    {
        Collection targetPages = null;
        try
        {
            List targetLocales = Arrays.asList(p_request
                    .getTargetLocalesToImport());

            targetPages = getPageManager()
                    .createTargetPagesWithUnextractedFile(p_sourcePage,
                            targetLocales);
        }
        catch (PageException pe)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", pe);
            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAIL_TO_CREATE_TARGET_PAGES, args,
                    pe);
        }
        catch (Throwable e)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", e);
            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAIL_TO_CREATE_TARGET_PAGES, args,
                    new Exception(e.toString()));
        }
        return targetPages;
    }

    /**
     * Store the file out to the application's storage directory.
     */
    private void storeFile(String p_fileName, UnextractedFile p_uf,
            String p_modifierId, boolean p_removeOriginal)
            throws FileImportException
    {
        try
        {
            UnextractedFile uf = getNativeFileManager().copyFileToStorage(
                    p_fileName, p_uf, p_removeOriginal);
            uf.setLastModifiedBy(p_modifierId);
            c_logger.debug("File " + p_fileName + " copied to storage.");

        }
        catch (NativeFileManagerException e)
        {
            c_logger.error("Failed to copy file " + p_fileName
                    + " to the storage location");
            String args[] =
            { p_fileName };
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_COPY_FILE_TO_STORAGE,
                    args, e);
        }
    }

    /**
     * Return the native file manager.
     */
    private NativeFileManager getNativeFileManager() throws FileImportException
    {
        NativeFileManager nfm = null;

        try
        {
            nfm = ServerProxy.getNativeFileManager();
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find the NativeFileManager", e);
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_FIND_NATIVE_FILE_MANAGER,
                    null, e);
        }
        return nfm;
    }
}

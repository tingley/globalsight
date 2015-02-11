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
package com.globalsight.everest.page;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class represents the remote implementation of a page manager that
 * manages a Page(s).
 * 
 * Note that all of the methods of this class throw the following exceptions: 1.
 * PageException - For page related errors. 2. RemoteException - For network
 * related exception.
 */
public class PageManagerWLRMIImpl extends RemoteServer implements
        PageManagerWLRemote
{
    PageManager m_localInstance = null;

    //
    // Begin: Constructor
    //

    /**
     * Construct a remote Page Manager.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public PageManagerWLRMIImpl() throws RemoteException, PageException
    {
        super(PageManager.SERVICE_NAME);
        m_localInstance = new PageManagerLocal();
    }

    /**
     * Initialize the server
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        // bind the server
        super.init();

        // clean up any pages that were in the middle of being updated when
        // the system was shutdown
        try
        {
            cleanupUpdatingPages();
        }
        catch (Exception e)
        {
            throw new SystemStartupException(
                    SystemStartupException.MSG_FAILED_TO_START_PAGEMANAGER,
                    null, e);
        }
    }

    //
    // Begin: PageManager Implementation
    //

    /**
     * Returns the source Page.
     * 
     * @see PageManager#getSourcePage(long)
     * @param p_sourcePageId
     *            source page identifier
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public SourcePage getSourcePage(long p_sourcePageId) throws PageException,
            RemoteException
    {
        return m_localInstance.getSourcePage(p_sourcePageId);
    }

    /**
     * Returns the target Page.
     * 
     * @see PageManager#getTargetPage(long)
     * @param p_targetPageId
     *            target page identifier
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public TargetPage getTargetPage(long p_targetPageId) throws PageException,
            RemoteException
    {
        return m_localInstance.getTargetPage(p_targetPageId);
    }

    /**
     * @see PageManager#getTargetPage(long, long)
     * @param p_sourcePageId
     *            - The id of the original source page that the target page was
     *            derived from.
     * @param p_localeId
     *            - The locale id of the target page.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public TargetPage getTargetPage(long p_sourcePageId, long p_localeId)
            throws PageException, RemoteException
    {
        return m_localInstance.getTargetPage(p_sourcePageId, p_localeId);
    }

    /**
     * Retrieve a source page from the database based on the externalPageId, and
     * source locale
     * 
     * @return The lastest version of the page or null if it doesn't exist.
     */
    public SourcePage getCurrentPageByNameAndLocale(Request p_request,
            GlobalSightLocale p_sourceLocale) throws PageException
    {
        return m_localInstance.getCurrentPageByNameAndLocale(p_request,
                p_sourceLocale);
    }

    /**
     * @see PageManager.getActiveTargetPageByNameAndLocale(Request)
     * 
     */
    public ArrayList getActiveTargetPagesByNameAndLocale(Request p_request)
            throws PageException
    {
        return m_localInstance.getActiveTargetPagesByNameAndLocale(p_request);
    }

    /**
     * @see PageManager#getPageWithExtractedFileForImport(Request,
     *      GlobalSightLocale, String, int, boolean)
     */
    public SourcePage getPageWithExtractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, String p_dataType,
            int p_wordCount, boolean p_containGsTags, String p_gxmlVersion)
            throws PageException
    {
        return m_localInstance.getPageWithExtractedFileForImport(p_request,
                p_sourceLocale, p_dataType, p_wordCount, p_containGsTags,
                p_gxmlVersion);
    }

    /**
     * 
     * @see PageManager#getPageWithUnextractedFileForImport(Request,
     *      GlobalSightLocale, int)
     */
    public SourcePage getPageWithUnextractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, int p_wordCount)
            throws PageException
    {
        return m_localInstance.getPageWithUnextractedFileForImport(p_request,
                p_sourceLocale, p_wordCount);
    }

    /**
     * @see PageManager.getSourcePagesStillImporting()
     */
    public Collection getSourcePagesStillImporting() throws PageException,
            RemoteException
    {
        return m_localInstance.getSourcePagesStillImporting();
    }

    /**
     * Factory method to create a new LeverageGroup.
     * 
     * @return new LeverageGroup.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public LeverageGroup createLeverageGroup() throws PageException,
            RemoteException
    {
        return m_localInstance.createLeverageGroup();
    }

    /**
     * Creates a copy of the source page for each target locale. The copy
     * includes all the Tuvs, LeverageGroups, etc. The sequence numbers for all
     * should be set to -1, and the locale replaced by the one passed in. The
     * states can all remain the same. The target locale rows need new database
     * sequence numbers assigned. Target pages are associated with the same
     * LeverageGroups and Tus as the source page.
     * <p>
     * Used by the import process to create target locale pages.
     * 
     * @param p_sourcePages
     *            source locale page.
     * @param p_targetLocales
     *            collection of target locales.
     * @param p_useLeveragedSegments
     *            Determines whether leveraged segments should be used.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public Collection createTargetPagesWithExtractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales,
            TermLeverageResult p_termMatches, boolean p_useLeveragedSegments,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        return m_localInstance.createTargetPagesWithExtractedFile(p_sourcePage,
                p_targetLocales, p_termMatches, p_useLeveragedSegments,
                p_useLeveragedTerms, p_exactMatchedSegments);
    }

    /**
     * @see PageManager.createTargetPagesWithUnextractedFile(SourcePage,
     *      Collection)
     */
    public Collection createTargetPagesWithUnextractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException
    {
        return m_localInstance.createTargetPagesWithUnextractedFile(
                p_sourcePage, p_targetLocales);
    }

    /**
     * @see PageManager.createTFailedargetPagesWithUnextractedFile(SourcePage,
     *      Collection)
     */
    public Collection createFailedTargetPagesWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocales)
            throws PageException
    {
        return m_localInstance.createFailedTargetPagesWithExtractedFile(
                p_sourcePage, p_targetLocales);
    }

    /**
     * Get a collection of template parts for a given source page id.
     * 
     * @param p_sourcePageId
     *            - The id of the source page.
     * @param p_pageTemplateType
     *            - The string representation of the page template type.
     * 
     * @return A collection of template parts based on the given source page id.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public Collection getTemplatePartsForSourcePage(Long p_sourcePageId,
            String p_pageTemplateType) throws PageException, RemoteException
    {
        return m_localInstance.getTemplatePartsForSourcePage(p_sourcePageId,
                p_pageTemplateType);
    }

    /**
     * Returns a collection of leverage group ids of the previous page for
     * reimport.
     * 
     * @param p_sourcePage
     *            - The source page used for getting it's previous leverage
     *            group ids.
     * @return leverage group ids of the previous page for reimport.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public Collection getLeverageGroupIdsForReimport(SourcePage p_sourcePage)
            throws PageException, RemoteException
    {
        return m_localInstance.getLeverageGroupIdsForReimport(p_sourcePage);
    }

    /**
     * Returns true if all the pages have been exported.
     * 
     * @param p_pageIds
     *            page identifiers as Longs.
     * @return true if all pages have been exported.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public boolean isExported(Collection p_pageIds) throws PageException,
            RemoteException
    {
        return m_localInstance.isExported(p_pageIds);
    }

    /**
     * Import a page by parsing the GXML, creating segments of the page.
     * 
     * @param p_request
     *            - The request that the page-to-be-imported belongs to.
     * @return A hash map containing all the pages (Source and Target) that were
     *         created as part of import. The key is the locale of the page.
     * 
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public HashMap importPage(Request p_request) throws PageException,
            RemoteException
    {
        return m_localInstance.importPage(p_request);
    }

    /**
     * Perform the export process for the specified list of pages.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for export.
     * @param p_pageIds
     *            - A collection of pages to be exported (from a workflow).
     * @param p_isTargetPage
     *            -- whether the ids refer to target or source pages
     * @param p_exportBatchId
     *            - the export ID returned from
     *            ExportEventObserver.notifyBeginExport()
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public void exportPage(ExportParameters p_exportParameters, List p_pageIds,
            boolean p_isTargetPage, long p_exportBatchId) throws PageException,
            RemoteException
    {
        m_localInstance.exportPage(p_exportParameters, p_pageIds,
                p_isTargetPage, p_exportBatchId);
    }

    /**
     * @see PageManager.exportSecondaryTargetFiles(ExportParameters, List, long)
     */
    public void exportSecondaryTargetFiles(ExportParameters p_exportParameters,
            List p_stfIds, long p_exportBatchId) throws PageException,
            RemoteException
    {
        m_localInstance.exportSecondaryTargetFiles(p_exportParameters,
                p_stfIds, p_exportBatchId);
    }

    /**
     * Perform the export for preview based on the specified list of tuvs.
     * 
     * @return The string representation of request information for a preview.
     * 
     * @param p_pageId
     *            - The id of the page where the tuvs belong to.
     * @param p_tuvIds
     *            - A collection of tuv ids used for preview.
     * @param p_uiLocale
     *            - The UI locale of CAP which will be used by CXE for
     *            displaying the preview screen.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public String exportForPreview(long p_pageId, List p_tuvIds,
            String p_uiLocale) throws PageException, RemoteException
    {
        return m_localInstance.exportForPreview(p_pageId, p_tuvIds, p_uiLocale);
    }

    /**
     * @see PageManager.updateBaseHrefs(Page, String, String)
     */
    public void updateBaseHrefs(Page p_page, String p_internalBaseHref,
            String p_externalBaseHref) throws PageException, RemoteException
    {
        m_localInstance.updateBaseHrefs(p_page, p_internalBaseHref,
                p_externalBaseHref);
    }

    /**
     * @see PageManager.updateWordCount(HashMap)
     */
    public void updateWordCount(HashMap p_pageWordCounts) throws PageException,
            RemoteException
    {
        m_localInstance.updateWordCount(p_pageWordCounts);
    }

    /**
     * @see PageManager.updateUnextractedFileInfo(Page)
     */
    public void updateUnextractedFileInfo(Page p_page) throws PageException,
            RemoteException
    {
        m_localInstance.updateUnextractedFileInfo(p_page);
    }

    /**
     * Returns a TargetLocaleLgIdsMapper object, which maps leverage group ids
     * of the reimportable page and target locales
     * 
     * @param p_sourcePage
     *            - The source page used for getting it's previous leverage
     *            group ids.
     * @param p_targetLocales
     *            - all target locales for the job
     * @return TargetLocaleLgIdsMapper object
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public TargetLocaleLgIdsMapper getLocaleLgIdMapForReimportPage(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException, RemoteException
    {
        return m_localInstance.getLocaleLgIdMapForReimportPage(p_sourcePage,
                p_targetLocales);
    }

    /**
     * @see PageManager.cleanupUpdatingPages
     */
    public void cleanupUpdatingPages() throws RemoteException, PageException
    {
        m_localInstance.cleanupUpdatingPages();
    }

    /**
     * Get source page object by leverage group ID.
     * 
     * @param p_leverageGroupId
     * @return
     */
    public SourcePage getSourcePageByLeverageGroupId(long p_leverageGroupId)
    {
        return m_localInstance
                .getSourcePageByLeverageGroupId(p_leverageGroupId);
    }
    
    /**
     * (non-Javadoc)
     * @see com.globalsight.everest.page.PageManager#getTargetPages(com.globalsight.everest.taskmanager.Task, java.lang.String)
     */
    public List<TargetPage> filterTargetPages(Task p_task, String p_segState)
    {
        return m_localInstance.filterTargetPages(p_task, p_segState);
    }
}

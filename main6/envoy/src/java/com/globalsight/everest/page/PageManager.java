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
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GlobalSightLocale;

/**
 * The PageManager interface is intended to provide management of Page objects.
 * Management include create, read, update, and delete. It also includes
 * management of state and versions of Page.
 * 
 * <p>
 * Methods specific to the high level process that call them are provided to
 * help target the behavior to the process. For example, a method that obtains
 * Pages for export may need less information in Page than other processes. This
 * allows for optimization.
 * 
 * <p>
 * Note that all of the methods of PageManager will throw RemoteException and
 * PageException.
 */
public interface PageManager
{
    /**
     * Integer constant representing an ExportParameters object.
     */
    public static final int EXPORT_PARAMETERS = 1;

    /**
     * Integer constant representing a page id for a page to be exported.
     */
    public static final int PAGE_ID = 2;

    /**
     * Integer constant representing the page to be exported as a target page
     */
    public static final int TARGET_PAGE = 3;

    /**
     * The number of pages in this workflow export batch
     */
    public static final int PAGE_COUNT = 4;

    /**
     * The page number of a particular page in a workflow export batch
     */
    public static final int PAGE_NUM = 5;

    /**
     * Integer constant representing the page to be exported as a source page
     */
    public static final int SOURCE_PAGE = 6;

    /**
     * Integer constant representing the page to be exported as a secondary
     * target file.
     */
    public static final int SECONDARY_TARGET_FILE = 7;

    /**
     * The number of pages per original Office document in this workflow export
     * batch.
     * 
     * If a job consists of one Powerpoint document that was split into 10
     * slides, and 3 slides are exported, this count is 3 (total number of pages
     * being exported from the current PPT doc).
     */
    public static final int DOC_PAGE_COUNT = 8;

    /**
     * The page number of the current page per original Office document in this
     * workflow export batch.
     * 
     * If a job consists of one Powerpoint document that was split into 10
     * slides, and 3 slides are exported, this count is 1, 2, or 3 for each of
     * the slides being exported (current number of page being exported from the
     * current PPT document).
     */
    public static final int DOC_PAGE_NUM = 9;

    /**
     * The name bound to the remote object.
     */
    public static final String SERVICE_NAME = "PageManagerServer";

    /**
     * Returns the source Page.
     * 
     * @param p_sourcePageId
     *            source page identifier
     * @throws PageException
     *             when an error occurs.
     */
    SourcePage getSourcePage(long p_sourcePageId) throws PageException,
            RemoteException;

    /**
     * Returns the target Page.
     * 
     * @param p_targetPageId
     *            target page identifier
     * @throws PageException
     *             when an error occurs.
     */
    TargetPage getTargetPage(long p_targetPageId) throws PageException,
            RemoteException;

    /**
     * Get the target page of a particular source page.
     * 
     * @param p_sourcePageId
     *            - The id of the original source page that the target page was
     *            derived from.
     * @param p_localeId
     *            - The locale id of the target page.
     * @return A target page based on the given source page id and localeId.
     * @throws PageException
     *             when an error occurs.
     */
    TargetPage getTargetPage(long p_sourcePageId, long p_localeId)
            throws PageException, RemoteException;

    /**
     * Retrieve a source page from the database based on the externalPageId, and
     * source locale.
     * 
     * @return The lastest version of the page or null if it doesn't exist.
     */
    public SourcePage getCurrentPageByNameAndLocale(Request p_request,
            GlobalSightLocale p_sourceLocale) throws PageException;

    /**
     * Retrieve the active target pages from the database based on the
     * externalPageId and target locales.
     * 
     * @return A collection of target pages that are active of the target
     *         locales of this request. Can be an empty list if none exist.
     */
    public ArrayList getActiveTargetPagesByNameAndLocale(Request p_request)
            throws PageException;

    /**
     * Return copy of existing page with new page_seq, or if there is a page in
     * IMPORT_FAIL state, return that. If p_externalPageId does not exist yet,
     * create a new Page. Persists Page in IMPORTING state. This page is
     * associated with an extracted file.
     * 
     * @param p_request
     * @param p_sourceLocale
     *            source Locale
     * @param p_dataType
     *            - The data type of the page (i.e. HTML, CSS, etc.).
     * @param p_wordCount
     *            - The word count of the page.
     * @param p_containGsTags
     *            - specifies whether it contains GS tags or no.
     * @param p_gxmlVersion
     *            - GXML version: 1.0 or 1.1 after 6.something.
     * 
     * @return Page a copy of existing page, or a new page if page does not yet
     *         exist.
     * @throws PageException
     *             when an error occurs.
     */
    SourcePage getPageWithExtractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, String p_dataType,
            int p_wordCount, boolean p_containGsTags, String p_gxmlVersion)
            throws PageException;

    /**
     * Create a page with new page_seq and passed information and persist with
     * the state in IMPORTING. This page is associated with an unextracted file.
     * 
     * @param p_externalPageId
     *            external unique page identifier
     * @param p_sourceLocale
     *            source Locale
     * @param p_wordCount
     *            - The word count of the page.
     * 
     * @return SourcePage A newly persisted page.
     * @throws PageException
     *             when an error occurs.
     */
    SourcePage getPageWithUnextractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, int p_wordCount)
            throws PageException;

    /**
     * Return all source pages stuck in 'IMPORTING'.
     * 
     * Called on start-up to find pages that were importing when the system
     * shutdown.
     */
    Collection getSourcePagesStillImporting() throws PageException,
            RemoteException;

    /**
     * Factory method to create a new LeverageGroup.
     * 
     * @return new LeverageGroup.
     * @throws PageException
     *             when an error occurs.
     */
    LeverageGroup createLeverageGroup() throws PageException, RemoteException;

    /**
     * Creates a target page which is a copy of the source page for each target
     * locale. This is for a source page that contains an extracted file (so
     * there are TUVs to copy).
     * <p>
     * Used by the import process to create target locale pages.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_targetLocales
     *            target locales.
     * @param p_termMatches
     *            term matches.
     * @param p_useLeveragedSegments
     *            Determines whether leveraged segments should be used.
     * @param p_useLeveragedSegments
     *            Determines whether leveraged terms should be replaced in the
     *            target segment.
     * @return A collection of created target pages.
     * @throws PageException
     *             when an error occurs.
     */
    Collection createTargetPagesWithExtractedFile(SourcePage p_sourcePage,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            boolean p_useLeveragedSegments, boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException;

    /**
     * Creates a target page which is a copy of the source page for each target
     * locale. This is for a source page that contains an unextracted file.
     * <p>
     * Used by the import process to create target locale pages.
     * 
     * @param p_sourcePages
     *            source locale page.
     * @param p_targetLocales
     *            target locales.
     * 
     * @return A collection of created target pages.
     * @throws PageException
     *             when an error occurs.
     */
    Collection createTargetPagesWithUnextractedFile(SourcePage p_sourcePage,
            Collection p_targetLocales) throws PageException;

    /**
     * Creates a failed target page which is a copy of the source page for each
     * target locale, but with the state IMPORT_FAIL and the import error stored
     * in the exception Xml. No segments - TUs and TUVs will be made - just the
     * Target page itself without segments. This assumes this is for a source
     * page that is extracted.
     * <p>
     * Used by the import process to create failed target locale pages if the
     * imported target page happens to fail or is already active in another job.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_targetLocaleInfo
     *            A hashtable of the locales that failed and the exceptions on
     *            why they failed. The key is the target locale (as a
     *            GlobalSightLocale) and the value is the GeneralException on
     *            why the page failed.
     * 
     * @return A collection of created target pages with state IMPORT_FAIL and
     *         the import error set.
     * @throws PageException
     *             when an error occurs.
     */
    Collection createFailedTargetPagesWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocaleInfo)
            throws PageException;

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
    Collection getTemplatePartsForSourcePage(Long p_sourcePageId,
            String p_pageTemplateType) throws PageException, RemoteException;

    /**
     * Returns a collection of leverage group ids of the previous reimportable
     * page for reimport.
     * 
     * @param p_sourcePage
     *            - The source page used for getting it's previous leverage
     *            group ids.
     * @return leverage group ids of the previous page for reimport.
     * @throws PageException
     *             when an error occurs.
     */
    Collection getLeverageGroupIdsForReimport(SourcePage p_sourcePage)
            throws PageException, RemoteException;

    /**
     * Returns true if all all the pages have been exported.
     * 
     * @param p_pageIds
     *            page identifiers as Longs.
     * @return true if all pages have been exported.
     * @throws PageException
     *             when an error occurs.
     */
    boolean isExported(Collection p_pageIds) throws PageException,
            RemoteException;

    /**
     * Import a page by parsing the GXML, creating segments of the page.
     * 
     * @param p_request
     *            - The request that the page-to-be-imported belongs to.
     * @return Returns the created pages (Source and Targets) in a HashMap. The
     *         key is the locale of the page.
     * @throws PageException
     *             when an error occurs.
     */
    HashMap importPage(Request p_request) throws PageException, RemoteException;

    /**
     * Perform the manual/automatic export for the specified list of pages.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for export.
     * @param p_pageIds
     *            - A collection of pages to be exported.
     * @param p_isTargetPage
     *            -- whether the ids refer to target or source pages
     * @param p_exportBatchId
     *            - the export ID returned from
     *            ExportEventObserver.notifyBeginExport()
     */
    void exportPage(ExportParameters p_exportParameters, List p_pageIds,
            boolean p_isTargetPage, long p_exportBatchId) throws PageException,
            RemoteException;

    /**
     * Perform the manual/automatic export for the specified list of secondary
     * target files.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for export.
     * @param p_stfIds
     *            - A collection of secondary target file ids to be exported.
     * @param p_exportBatchId
     *            - the export ID returned from
     *            ExportEventObserver.notifyBeginExport()
     */
    void exportSecondaryTargetFiles(ExportParameters p_exportParameters,
            List p_stfIds, long p_exportBatchId) throws PageException,
            RemoteException;

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
     */
    String exportForPreview(long p_pageId, List p_tuvIds, String p_uiLocale)
            throws PageException, RemoteException;

    /**
     * Update the page with the base href's specified. If either is null - it
     * won't update the page, leaves the existing base href as is. If the base
     * href is of zero length it will update it. Persists in the database.
     * 
     * @param p_page
     *            The page to update.
     * @param p_internalBaseHref
     *            The base href specified internally to the page.
     * @param p_externalBaseHref
     *            The base href specified by the external system (CXE).
     */
    void updateBaseHrefs(Page p_page, String p_internalBaseHref,
            String p_externalBaseHref) throws PageException, RemoteException;

    /**
     * Updates the word count on source pages - and also passes on the update to
     * the target pages. This is just no match word count update that the user
     * is allowed to update pages associated with un-extracted files.
     * 
     * @param p_pageWordCounts
     *            This is a map that contains a list of word counts and their
     *            source pages to up-date. The key is a Long which specifies the
     *            the source page id and the value is an Integer object that
     *            specifies the word count of the page.
     */
    void updateWordCount(HashMap p_pageWordCounts) throws PageException,
            RemoteException;

    /**
     * Update the information on an un-extracted file.
     * 
     * It doesn't update any other information on the page (if it has been
     * modified) only data that is particular to the un-extracted file.
     * 
     * @param p_page
     *            The page that contains a modified un-extracted file
     */
    void updateUnextractedFileInfo(Page p_page) throws PageException,
            RemoteException;

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
            throws PageException, RemoteException;

    /**
     * Cleans up dangling, pages that were in the process of being UPDATED when
     * the system was shutdown. This is used on start-up.
     * 
     * @exception PageException
     *                An exception within the component that kept it from
     *                cleaning up updating pages.
     */
    void cleanupUpdatingPages() throws RemoteException, PageException;

    /**
     * Get source page object by leverage group ID.
     * 
     * @param p_leverageGroupId
     * @return
     */
    public SourcePage getSourcePageByLeverageGroupId(long p_leverageGroupId);
    
    /**
     * Get target page object by Task and Segment State
     * 
     * @param p_task
     * @param p_segState
     *            Segment State, e.g. un-translated Segments
     * @return
     */
    public List<TargetPage> filterTargetPages(Task p_task, String p_segState);
}

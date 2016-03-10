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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.fileExport.FileExportRunnable;
import com.globalsight.cxe.util.fileExport.FileExportUtil;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.online.OnlineEditorConstants;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.page.pageimport.FileImportException;
import com.globalsight.everest.page.pageimport.FileImporter;
import com.globalsight.everest.page.pageimport.TargetPageImportPersistence;
import com.globalsight.everest.page.pageimport.TargetPagePersistence;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageimport.PageImportQuery;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;

/**
 * The PageManagerLocal class provides an implementation of the PageManager.
 * <p>
 * 
 * Methods specific to the high level process that call them are provided to
 * help target the behavior to the process. For example, a method that obtains
 * Pages for export may need less information in Page than other processes. This
 * allows for optimization.
 */
public final class PageManagerLocal implements PageManager
{
    static private final Logger s_category = Logger
            .getLogger(PageManagerLocal.class);

    private static final String GET_SOURCE_PAGE_BY_LG_ID_SQL = "SELECT sp.* from source_page sp, source_page_leverage_group splg "
            + "WHERE sp.id = splg.sp_id " + "AND splg.lg_id = ?";

    private static final String GET_SOURCE_PAGE_BY_TU_ID_SQL = "SELECT sp.* from source_page sp, source_page_leverage_group splg, "
            + TuvQueryConstants.TU_TABLE_PLACEHOLDER
            + " tu "
            + "WHERE sp.id = splg.sp_id "
            + "AND splg.lg_id = tu.leverage_group_id " + "AND tu.id = ?";

    private static final int MAX_THREAD = 100;

    /**
     * PageToDocumentMap is a class mapping a list of page IDs to the name of
     * the original documents they came from, and from there to the list of all
     * other pages in the ID list that map to the same document (the length of
     * that list is what is needed).
     * 
     * This class is used during export when the UI sends a list of page IDs,
     * and PageManager creates export requests for each individual page. In case
     * of MS Office documents, multiple individual pages may belong to the same
     * Office document. This class allows to assign proper export IDs so the CXE
     * export code - which receives requests for individual pages - can
     * determine when the last page for a specific Office doc has been received,
     * and start exporting it.
     */
    private class PageToDocumentMap
    {
        private final Integer INT_ONE = new Integer(1);

        private HashMap m_idToDoc = new HashMap();
        private HashMap m_docToIds = new HashMap();

        PageToDocumentMap(List p_ids, Integer p_genericPageType)
        {
            for (int i = 0, max = p_ids.size(); i < max; i++)
            {
                Long pageId = (Long) p_ids.get(i);

                SourcePage page = null;
                String docName = null;

                try
                {
                    // Find the original document name this page came from.
                    // See Exporthelper.startExport() for how to get the pages.
                    switch (p_genericPageType.intValue())
                    {
                        case PageManager.SECONDARY_TARGET_FILE:
                            // TODO: gracefully handled below for now.
                            break;

                        case PageManager.SOURCE_PAGE:
                            page = getSourcePage(pageId.longValue());
                            docName = getDocNameFromPage(page);
                            break;

                        case PageManager.TARGET_PAGE:
                            page = getTargetPage(pageId.longValue())
                                    .getSourcePage();
                            docName = getDocNameFromPage(page);
                            break;
                    }
                }
                catch (Exception ex)
                {
                    s_category.error("Cannot initialize PageToDocumentMap", ex);
                }

                // If a docname was found, build the data structure.
                if (docName != null)
                {
                    m_idToDoc.put(pageId, docName);

                    ArrayList ids = (ArrayList) m_docToIds.get(docName);

                    if (ids == null)
                    {
                        ids = new ArrayList();
                        m_docToIds.put(docName, ids);
                    }

                    ids.add(pageId);
                }
            }
        }

        /**
         * Gets the original document name this page came from, not the display
         * name we use in the UI.
         */
        private String getDocNameFromPage(SourcePage p_page) throws Exception
        {
            String result = null;

            XmlParser parser = XmlParser.hire();

            try
            {
                String efxml = p_page.getRequest().getEventFlowXml();

                Document doc = parser.parseXml(efxml);
                Element root = doc.getRootElement();

                Node tmp = root
                        .selectSingleNode("/eventFlowXml/source/da[@name='Filename']/dv");

                if (tmp != null)
                {
                    result = tmp.getText();
                }
            }
            finally
            {
                XmlParser.fire(parser);
            }

            return result;
        }

        /**
         * For a page that is part of a multi-page Office document, returns the
         * current index of this page in the overall pages that are exported for
         * this document.
         */
        public Integer getPageNumberForDoc(Long p_pageId)
        {
            String docName = (String) m_idToDoc.get(p_pageId);

            if (docName != null)
            {
                ArrayList ids = (ArrayList) m_docToIds.get(docName);

                return new Integer(ids.indexOf(p_pageId) + 1);
            }

            // If a valid page ID is coming in but we don't have data
            // for it, assume it's a single page of a single document
            // (like a typical HTML page is) and return 1.
            return INT_ONE;
        }

        /**
         * For a page that is part of a multi-page Office document, returns the
         * total number of pages from that document that are to be exported as
         * part of the batch.
         */
        public Integer getPageCountForDoc(Long p_pageId)
        {
            String docName = (String) m_idToDoc.get(p_pageId);

            if (docName != null)
            {
                ArrayList ids = (ArrayList) m_docToIds.get(docName);

                return new Integer(ids.size());
            }

            // If a valid page ID is coming in but we don't have data
            // for it, assume it's a single page of a single document
            // (like a typical HTML page is) and return 1.
            return INT_ONE;
        }
    }

    /**
     * Constructor - construct an instance of PageManagerLocal.
     */
    public PageManagerLocal() throws PageException
    {
        super();
        // initMachineTranslation();
    }

    //
    // PageManager Implementation
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
        return PagePersistenceAccessor.getSourcePageById(p_sourcePageId);
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
        return getTargetPageById(p_targetPageId);
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
        return PagePersistenceAccessor
                .getTargetPage(p_sourcePageId, p_localeId);
    }

    /**
     * Filter target page list by Target Page Filter type
     * 
     * @param p_task
     * @param p_filertType
     *            Target Page Filter Type, e.g. un-translated Segments
     * @return
     */
    public List<TargetPage> filterTargetPages(Task p_task, String p_filertType)
    {
        List<TargetPage> tps = p_task.getTargetPages();
        if (OnlineEditorConstants.SEGMENT_FILTER_NO_TRANSLATED
                .equalsIgnoreCase(p_filertType))
        {
            return SegmentTuvUtil.filterUnTranslatedTargetPages(tps);
        }

        return tps;
    }

    /**
     * @see PageManager.getPageWithExtractedFileForImport(Request,
     *      GlobalSightLocale, String, int, boolean, String)
     */
    public SourcePage getPageWithExtractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, String p_dataType,
            int p_wordCount, boolean p_containGsTags, String p_gxmlVersion)
            throws PageException
    {
        SourcePage page = null;

        try
        {
            page = getCurrentPageByNameAndLocale(p_request, p_sourceLocale);

            if (page == null)
            {
                // create a new page with a state of Importing
                page = createNewPageWithExtractedFileForImport(p_request,
                        p_sourceLocale, p_dataType, p_wordCount,
                        p_containGsTags, p_gxmlVersion);
            }
            else
            {
                page = getClonedPage(page, p_request, p_sourceLocale,
                        p_dataType, p_wordCount, p_containGsTags, p_gxmlVersion);
            }
            page = persistSourcePage(p_request, page);
            page.setRequest(p_request);
        }
        catch (Exception e)
        {
            s_category.error("Unable to persist a page", e);
            String[] args =
            { Long.toString(p_request.getId()) };
            throw new PageException(PageException.MSG_FAILED_TO_CREATE_PAGE,
                    args, e);
        }

        return page;
    }

    /**
     * @see SourcePage getPageWithUnextractedFileForImport(Request,
     *      GlobalSightLocale, int)
     */
    public SourcePage getPageWithUnextractedFileForImport(Request p_request,
            GlobalSightLocale p_sourceLocale, int p_wordCount)
            throws PageException
    {
        String externalPageId = p_request.getExternalPageId();
        String dataSourceType = p_request.getDataSourceType();

        SourcePage sp = new SourcePage(externalPageId, p_sourceLocale,
                dataSourceType, p_wordCount, PrimaryFile.UNEXTRACTED_FILE);
        UnextractedFile uf = (UnextractedFile) sp.getPrimaryFile();

        // specify where to put the UnextractedFile
        StringBuffer fileName = new StringBuffer(Long.toString(p_request
                .getId()));
        fileName.append(File.separator);
        fileName.append("PSF");
        fileName.append(File.separator);
        fileName.append(sp.getExternalPageId());
        uf.setStoragePath(fileName.toString());

        try
        {
            sp = persistSourcePage(p_request, sp);
            sp.setRequest(p_request);
        }
        catch (PersistenceException pe)
        {
            s_category.error("Unable to persist a new source page", pe);

            String args[] =
            { Long.toString(p_request.getId()) };
            throw new PageException(PageException.MSG_FAILED_TO_CREATE_PAGE,
                    args, pe);
        }

        return sp;
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
        return new LeverageGroupImpl();
    }

    /**
     * @see PageManager.createTargetPagesWithExtractedFile( SourcePage,
     *      Collection, TermLeveragerResult, boolean, boolean,
     *      ExactMatchedSegments, ExactMatchedSegments)
     */
    public Collection createTargetPagesWithExtractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales,
            TermLeverageResult p_termMatches, boolean p_useLeveragedSegments,
            boolean p_useLeveragedTerms,
            ExactMatchedSegments p_exactMatchedSegments) throws PageException
    {
        Collection pages = null;

        try
        {

            TargetPagePersistence tpPersistence = new TargetPageImportPersistence();

            pages = tpPersistence.persistObjectsWithExtractedFile(p_sourcePage,
                    p_targetLocales, p_termMatches, p_useLeveragedSegments,
                    p_useLeveragedTerms, p_exactMatchedSegments);
        }
        catch (PageException e)
        {
            s_category.error("Unable to create target pages", e);
            String args[] =
            { Long.toString(p_sourcePage.getId()) };
            throw new PageException(PageException.MSG_FAILED_TO_CREATE_PAGE,
                    args, e);
        }

        return pages;
    }

    /**
     * @see PageManager.createTargetPagesWithUnextractedFile( SourcePage,
     *      Collection)
     */
    public Collection createTargetPagesWithUnextractedFile(
            SourcePage p_sourcePage, Collection p_targetLocales)
            throws PageException
    {
        Collection pages = null;

        try
        {

            TargetPagePersistence tpPersistence = new TargetPageImportPersistence();

            pages = tpPersistence.persistObjectsWithUnextractedFile(
                    p_sourcePage, p_targetLocales);
        }
        catch (PageException e)
        {
            s_category.error("Unable to create target pages", e);
            String args[] =
            { Long.toString(p_sourcePage.getId()) };
            throw new PageException(PageException.MSG_FAILED_TO_CREATE_PAGE,
                    args, e);
        }

        return pages;
    }

    /**
     * @see PageManager.createFailedTargetPagesWithExtractedFile( SourcePage,
     *      Hashtable)
     */
    public Collection createFailedTargetPagesWithExtractedFile(
            SourcePage p_sourcePage, Hashtable p_targetLocaleInfo)
            throws PageException
    {
        Collection pages = null;

        TargetPagePersistence tpPersistence = new TargetPageImportPersistence();

        pages = tpPersistence.persistFailedObjectsWithExtractedFile(
                p_sourcePage, p_targetLocaleInfo);

        return pages;
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
        return PagePersistenceAccessor.getTemplateParts(p_sourcePageId,
                p_pageTemplateType);
    }

    /**
     * Returns a collection of leverage group ids of the previous reimportable
     * page for reimport.
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
        Collection lgIds = new ArrayList();

        try
        {
            SourcePage page = findAcceptablePage(p_sourcePage, true);

            if (page != null)
            {
                PageImportQuery piq = new PageImportQuery();
                lgIds = piq.getLeverageGroupIds(page.getId());
            }
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return lgIds;
    }

    /**
     * Returns true if all the pages have been exported.
     * 
     * @param p_pageIds
     *            page identifiers as Long objects.
     * @return true if all pages have been exported.
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public boolean isExported(Collection p_pageIds) throws PageException,
            RemoteException
    {
        Object[] pageIds = p_pageIds.toArray();
        int size = pageIds.length;

        for (int i = 0; i < size; i++)
        {
            long id = ((Long) pageIds[i]).longValue();
            TargetPage page = getTargetPageById(id);

            if (!PageState.EXPORTED.equals(page.getPageState()))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * @see PageManager.importPage method
     * @exception PageException
     *                when a page related error occurs.
     * @exception RemoteException
     *                Network related exception.
     */
    public HashMap importPage(Request p_request) throws PageException,
            RemoteException
    {
        HashMap pages = null;

        try
        {
            pages = FileImporter.importPrimaryFile(p_request);
        }
        catch (FileImportException pie)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new PageException(PageException.MSG_FAILED_TO_IMPORT_PAGE,
                    args, pie);
        }

        return pages;
    }

    /**
     * Perform the export process for the specified list of pages.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for export.
     * @param p_pageIds
     *            - A collection of pages to be exported (from a workflow).
     * @param p_isTargetPage
     *            - whether the ids refer to target or source pages
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
        performExport(p_exportParameters, new Integer(
                p_isTargetPage ? TARGET_PAGE : SOURCE_PAGE), p_pageIds,
                p_exportBatchId);
    }

    /**
     * @see PageManager.exportSecondaryTargetFiles(ExportParameters, List, long)
     */
    public void exportSecondaryTargetFiles(ExportParameters p_exportParameters,
            List p_stfIds, long p_exportBatchId) throws PageException,
            RemoteException
    {
        performExport(p_exportParameters, new Integer(SECONDARY_TARGET_FILE),
                p_stfIds, p_exportBatchId);
    }

    /**
     * Perform the export for preview based on the specified list of tuvs.
     * 
     * @return The string representation of request information for a preview.
     * 
     * @param p_pageId
     *            - The id of the page the tuvs belong to.
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
        ExportHelper helper = new ExportHelper();
        return helper.exportForPreview(p_pageId, p_tuvIds, p_uiLocale);
    }

    /**
     * @see PageManager.updateBaseHrefs(Page, String, String)
     */
    public void updateBaseHrefs(Page p_page, String p_internalBaseHref,
            String p_externalBaseHref) throws PageException, RemoteException
    {
        PagePersistenceAccessor.updateBaseHrefs(p_page, p_internalBaseHref,
                p_externalBaseHref);
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
        SourcePage sourcePage = null;

        try
        {
            PageImportQuery query = new PageImportQuery();
            sourcePage = query
                    .getLatestVersionOfSourcePage((RequestImpl) p_request);
        }
        catch (Exception e)
        {
            throw new PageException(
            /* PageException.XXXXX, null, */e);
        }

        return sourcePage;
    }

    /**
     * @see PageManager.getActiveTargetPageByNameAndLocale(Request)
     * 
     */
    public ArrayList getActiveTargetPagesByNameAndLocale(Request p_request)
            throws PageException
    {
        ArrayList targetPages = new ArrayList(0);

        String pageName = p_request.getExternalPageId();
        GlobalSightLocale sourceLocale = p_request.getL10nProfile()
                .getSourceLocale();
        // go through all the target locales
        GlobalSightLocale[] targetLocales = p_request
                .getTargetLocalesToImport();
        for (int i = 0; i < targetLocales.length; i++)
        {
            TargetPage tp = PagePersistenceAccessor
                    .getActiveTargetPageByNameAndLocales(pageName,
                            sourceLocale, targetLocales[i]);
            // if there is a current page add to the array list
            if (tp != null)
            {
                targetPages.add(tp);
            }
        }

        return targetPages;
    }

    /**
     * @see PageManager.getSourcePagesStillImporting()
     */
    public Collection getSourcePagesStillImporting() throws PageException,
            RemoteException
    {
        return PagePersistenceAccessor.getSourcePagesStillImporting();
    }

    /**
     * @see PageManager.updateWordCount(HashMap)
     */
    public void updateWordCount(HashMap p_pageWordCounts) throws PageException,
            RemoteException
    {
        PagePersistenceAccessor.updateWordCount(p_pageWordCounts);
    }

    /**
     * @see PageManager.updateUnextractedFileInfo(Page)
     */
    public void updateUnextractedFileInfo(Page p_page) throws PageException,
            RemoteException
    {
        PagePersistenceAccessor.updateUnextractedFile(p_page);
    }

    /**
     * @see PageManager.cleanupUpdatingPages
     */
    public void cleanupUpdatingPages() throws RemoteException, PageException
    {
        try
        {
            // query for all pages with the state of UPDATING
            // both Target and Source pages

            HashMap map = new HashMap();
            map.put("state", PageState.UPDATING);

            String hql = "from SourcePage s where s.pageState = :state";
            Collection pages = HibernateUtil.search(hql, map);

            hql = "from TargetPage p where p.pageState = :state";
            pages.addAll(HibernateUtil.search(hql, map));

            PagePersistenceAccessor.resetPagesToPreviousState(pages);
        }
        catch (Exception e)
        {
            s_category.error(
                    "Failed to clean-up the pages left in the UPDATING state.",
                    e);
            throw new PageException(
                    PageException.MSG_PAGE_IN_UPDATING_STATE_ERROR, null, e);
        }
    }

    //
    // Local Methods
    //

    // create a new source page with an extracted file for importing
    private SourcePage createNewPageWithExtractedFileForImport(
            Request p_request, GlobalSightLocale p_sourceLocale,
            String p_dataType, int p_wordCount, boolean p_containGsTags,
            String p_gxmlVersion)
    {
        String externalPageId = p_request.getExternalPageId();
        String originalEncoding = p_request.getSourceEncoding();
        // GBS-2035: Bom not exported, Vincent Yan, 2011/08/04
        int BOMType = 0;
        if (FileUtil.UTF8.equals(originalEncoding))
        {
            String baseDocDir = AmbFileStoragePathUtils.getCxeDocDirPath();
            if (FileUtil.isNeedBOMProcessing(externalPageId))
            {
                File file = new File(baseDocDir, externalPageId);
                try
                {
                    String encoding = FileUtil.guessEncoding(file);
                    if (FileUtil.UTF8.equals(encoding))
                    {
                        // UTF-8 with BOM
                        BOMType = FileProfileImpl.UTF_8_WITH_BOM;
                    }
                    else if (FileUtil.UTF16LE.equals(encoding))
                    {
                        BOMType = FileProfileImpl.UTF_16_LE;
                    }
                    else if (FileUtil.UTF16BE.equals(encoding))
                    {
                        BOMType = FileProfileImpl.UTF_16_BE;
                    }
                }
                catch (IOException e)
                {
                }
            }
        }

        String dataSourceType = p_request.getDataSourceType();

        SourcePage sp = new SourcePage(externalPageId, p_sourceLocale,
                dataSourceType, p_wordCount, BOMType,
                PrimaryFile.EXTRACTED_FILE);
        ExtractedSourceFile esf = (ExtractedSourceFile) sp.getPrimaryFile();

        esf.setOriginalCodeSet(originalEncoding);
        esf.containGsTags(p_containGsTags);
        esf.setDataType(p_dataType);
        esf.setGxmlVersion(p_gxmlVersion);

        return sp;
    }

    /**
     * If the page exists (state is exported, export cancelled, or out of date)
     * return the updated cloned page. Otherwise, return a new source page.
     */
    private SourcePage getClonedPage(SourcePage p_page, Request p_request,
            GlobalSightLocale p_sourceLocale, String p_dataType,
            int p_wordCount, boolean p_containsGsTag, String p_gxmlVersion)
            throws RemoteException, PageException
    {
        SourcePage page = findAcceptablePage(p_page, false);

        if (page == null)
        {
            return createNewPageWithExtractedFileForImport(p_request,
                    p_sourceLocale, p_dataType, p_wordCount, p_containsGsTag,
                    p_gxmlVersion);
        }
        else
        // used for un-extracted pages only
        {
            page = createNewVersionOfPage(page);
            page.setWordCount(p_wordCount);

            if (page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
            {
                ExtractedSourceFile esf = (ExtractedSourceFile) page
                        .getPrimaryFile();
                esf.containGsTags(p_containsGsTag);
                esf.setGxmlVersion(p_gxmlVersion);
            }

            return page;
        }
    }

    /**
     * Create a new version of the page by cloning it from the previous page and
     * setting the previousPageId. This is done just for a SourcePage with an
     * extracted file.
     */
    private SourcePage createNewVersionOfPage(SourcePage p_page)
            throws PageException
    {
        // clone the page and set the previousPageId to be the current
        // page's id
        SourcePage newPage = (SourcePage) p_page.cloneSourcePage();
        newPage.setPreviousPageId(p_page.getId());

        return newPage;
    }

    /**
     * Make sure to return the page that is not in Not_Localized state.
     */
    private SourcePage findAcceptablePage(SourcePage p_page,
            boolean p_forLeverage) throws RemoteException, PageException
    {
        SourcePage acceptedPage = p_page;

        // if it isn't null but is a failure - look for the next one
        if (acceptedPage != null
                && !isPageReimportable(acceptedPage, p_forLeverage))
        {
            acceptedPage = findAcceptablePage(
                    getSourcePageById(acceptedPage.getPreviousPageId()),
                    p_forLeverage);
        }

        return acceptedPage;
    }

    private SourcePage findAcceptablePrevPageByLocale(SourcePage p_page,
            GlobalSightLocale p_locale) throws RemoteException, PageException
    {
        SourcePage acceptedPage = p_page;
        SourcePage prevPage = null;
        prevPage = getSourcePageById(acceptedPage.getPreviousPageId());
        return prevPage;
    }

    /**
     * Detemines whether the source page can be used as a previous page during
     * re-importing.
     */
    private boolean isPageReimportable(SourcePage p_page, boolean p_forLeverage)
    {
        String state = p_page.getPageState();
        boolean result;

        if (p_forLeverage)
        {
            // EXPORTED is the only state that guarantees the state of
            // all Tuvs associated with the page are COMPLETE.
            result = PageState.EXPORTED.equals(state);
        }
        else
        {
            // if the page failed import, is currently being
            // imported or was successfully imported - can't
            // use as the previous page (nothing to leverage)
            result = (!PageState.IMPORTING.equals(state)
                    && !PageState.IMPORT_FAIL.equals(state) && !PageState.IMPORT_SUCCESS
                    .equals(state));
        }

        return result;
    }

    /**
     * Gets a source page based on the specified id.
     */
    private SourcePage getSourcePageById(long p_pageId) throws PageException
    {
        SourcePage sourcePage = null;

        try
        {
            sourcePage = new PageImportQuery().getSourcePageById(p_pageId);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_pageId) };

            throw new PageException(PageException.MSG_FAILED_TO_GET_PAGE_BY_ID,
                    args, e);
        }

        return sourcePage;
    }

    /**
     * Gets a target page based on the specified id.
     */
    private TargetPage getTargetPageById(long p_pageId) throws PageException
    {
        return PagePersistenceAccessor.getTargetPageById(p_pageId);
    }

    // persist the page (along with LeverageGroups, Tus, and Tuvs)...
    private SourcePage persistSourcePage(Request p_request, SourcePage p_page)
            throws PersistenceException
    {
        try
        {
            p_page.setTimestamp(new Timestamp(System.currentTimeMillis()));
            p_page.setCompanyId(p_request.getCompanyId());
            p_page.setRequest(p_request);

            HibernateUtil.save(p_page);
            p_request.setSourcePage(p_page);
            HibernateUtil.update(p_request);
        }
        catch (Exception pe)
        {
            s_category.error("The source page could not be persisted");

            throw new PersistenceException(pe);
        }

        return p_page;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void performExport(ExportParameters exportParameters,
            Integer p_genericPageType, List p_ids, long p_exportBatchId)
            throws PageException
    {
        try
        {
            PageToDocumentMap pageDocMap = new PageToDocumentMap(p_ids,
                    p_genericPageType);

            int pageCount = p_ids.size();
            ArrayList<Hashtable> slidesPages = new ArrayList<Hashtable>();
            ArrayList<Hashtable> notesPages = new ArrayList<Hashtable>();
            ArrayList<Hashtable> otherPages = new ArrayList<Hashtable>();

            // loop through all pages....
            ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
            String exportCode = exportParameters.getExportCodeset();
            for (int i = 0; i < pageCount; i++)
            {
                ExportParameters eParameters = exportParameters.clone();
                // set the values in a hashtable
                Hashtable map = new Hashtable();
                map.put(new Integer(EXPORT_PARAMETERS), eParameters);
                map.put(new Integer(TARGET_PAGE), p_genericPageType);
                map.put(new Integer(PAGE_COUNT), new Integer(pageCount));
                map.put(ExportConstants.EXPORT_BATCH_ID, new Long(
                        p_exportBatchId));
                CompanyWrapper.saveCurrentCompanyIdInMap(map, s_category);

                Long pageId = (Long) p_ids.get(i);
                map.put(new Integer(PAGE_ID), pageId);
                map.put(new Integer(PAGE_NUM), new Integer(i));

                // For Office documents in multi-format jobs, specify
                // which sub-page of the doc this is, and how many in
                // total there are to export.
                map.put(new Integer(DOC_PAGE_NUM),
                        pageDocMap.getPageNumberForDoc(pageId));
                map.put(new Integer(DOC_PAGE_COUNT),
                        pageDocMap.getPageCountForDoc(pageId));

                SourcePage page = null;
                SecondaryTargetFile secondaryTargetFile = null;
                if (p_genericPageType == SOURCE_PAGE)
                {
                    page = HibernateUtil.get(SourcePage.class, pageId);
                }
                else if (TARGET_PAGE == p_genericPageType)
                {
                    TargetPage tpage = HibernateUtil.get(TargetPage.class,
                            pageId);
                    String exportSubDir = tpage.getExportSubDir();
                    // update target page only when export dir is changed.
                    if (exportSubDir == null
                            || !exportSubDir.equals(eParameters
                                    .getLocaleSubDir()))
                    {
                        tpage.setExportSubDir(eParameters
                                .getLocaleSubDir());
                        PagePersistenceAccessor.updateTargetPage(tpage);
                    }
                    page = tpage.getSourcePage();
                }
                else if (SECONDARY_TARGET_FILE == p_genericPageType)
                {
                    secondaryTargetFile = ServerProxy
                            .getSecondaryTargetFileManager()
                            .getSecondaryTargetFile(pageId);
                }

                String path = null;
                if (page != null)
                {
                    // GBS-3731
                    if (exportCode
                            .startsWith(JobManagementHandler.SAME_AS_SOURCE))
                    {
                        long fileProfileId = page.getRequest()
                                .getFileProfileId();
                        FileProfile fileProfile = (FileProfile) HibernateUtil
                                .get(FileProfileImpl.class, fileProfileId,
                                        false);
                        eParameters.setExportCodeset(exportCode.replace(
                                JobManagementHandler.SAME_AS_SOURCE,
                                fileProfile.getCodeSet()));
                    }

                    ExtractedSourceFile sfile = (ExtractedSourceFile) page
                            .getExtractedFile();
                    path = page.getExternalPageId().toLowerCase();
                    if (FileExportUtil.USE_JMS && path.endsWith(".pptx")
                            && sfile != null
                            && "office-xml".equals(sfile.getDataType()))
                    {
                        if (path.startsWith("(slide"))
                        {
                            slidesPages.add(map);
                        }
                        else if (path.startsWith("(notes"))
                        {
                            notesPages.add(map);
                        }
                        else
                        {
                            otherPages.add(map);
                        }

                        continue;
                    }
                }
                else if (secondaryTargetFile != null)
                {
                    path = secondaryTargetFile.getStoragePath();
                }

                if (FileExportUtil.USE_JMS)
                {
                    JmsHelper.sendMessageToQueue(map,
                            JmsHelper.JMS_EXPORTING_QUEUE);
                }
                else
                {
                    if (path != null)
                    {
                        map.put("filePath", path);
                        String key = p_exportBatchId + path
                                + map.get(new Integer(PAGE_NUM));
                        map.put("key", key);
                    }

                    int priority = 3;
                    if(page != null)
                    {
                        long jobId = page.getJobId();
                        JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
                        if (job != null)
                        {
                            priority = job.getPriority();
                        }
                    }
                    map.put("priority", priority);

                    FileExportRunnable runnable = new FileExportRunnable(map);
                    pool.execute(runnable);
                }
            }
            pool.shutdown();

            if (slidesPages.size() > 0)
            {
                JmsHelper.sendMessageToQueue(slidesPages,
                        JmsHelper.JMS_EXPORTING_QUEUE);
            }

            if (notesPages.size() > 0)
            {
                JmsHelper.sendMessageToQueue(notesPages,
                        JmsHelper.JMS_EXPORTING_QUEUE);
            }

            if (otherPages.size() > 0)
            {
                JmsHelper.sendMessageToQueue(otherPages,
                        JmsHelper.JMS_EXPORTING_QUEUE);
            }
        }
        catch (Exception ex)
        {
            s_category.error("PageManagerLocal: " + ex.getMessage(), ex);
            throw new PageException(ex);
        }
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
        TargetLocaleLgIdsMapper localeLgIdMap = new TargetLocaleLgIdsMapper();

        try
        {
            Iterator it = p_targetLocales.iterator();
            while (it.hasNext())
            {
                GlobalSightLocale locale = (GlobalSightLocale) it.next();
                SourcePage page = findAcceptablePrevPageByLocale(p_sourcePage,
                        locale);

                if (page != null)
                {
                    localeLgIdMap.addTargetLocale(page.getIdAsLong(), locale);
                }
            }

            PageImportQuery query = new PageImportQuery();
            it = localeLgIdMap.getAllSourcePageIds().iterator();
            while (it.hasNext())
            {
                Long pageId = (Long) it.next();
                Collection lgIds = query
                        .getLeverageGroupIds(pageId.longValue());
                localeLgIdMap.addLgIds(pageId, lgIds);
            }
        }
        catch (Exception ex)
        {
            throw new PageException(ex);
        }

        return localeLgIdMap;
    }

    /**
     * Get source page object by leverage group ID.
     * 
     * @param p_leverageGroupId
     * @return
     */
    public SourcePage getSourcePageByLeverageGroupId(long p_leverageGroupId)
    {
        List<SourcePage> sourcePages = HibernateUtil.searchWithSql(
                SourcePage.class, GET_SOURCE_PAGE_BY_LG_ID_SQL,
                p_leverageGroupId);

        if (sourcePages != null && sourcePages.size() > 0)
        {
            return sourcePages.get(0);
        }

        return null;
    }
}

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.persistence.page.SourcePageDescriptorModifier;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * PagePersistenceAccessor is responsible for accessing TopLink persistance
 * service for all of the PageManager activities. These activities include all
 * types of database query, insert, and update.
 */
public final class PagePersistenceAccessor
{
    static private final Logger CATEGORY = Logger
            .getLogger(PagePersistenceAccessor.class);

    // ////////////////////////////////////////////////////////////////////
    // Begin: Static package level methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get a source page based on the specified id.
     * 
     * @param p_sourcePageId
     *            - The id of the requested source page.
     * @return A source page object.
     * @exception PageException
     *                thrown upon a query error.
     */
    public static SourcePage getSourcePageById(long p_sourcePageId)
            throws PageException
    {
        SourcePage page = null;
        try
        {
            page = (SourcePage) HibernateUtil.get(SourcePage.class,
                    p_sourcePageId);
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return page;
    }

    /**
     * Get a target page based on the specified id.
     * 
     * @param p_targetPageId
     *            - The id of the requested target page.
     * @return A target page object.
     * @exception PageException
     *                thrown upon a query error.
     */
    public static TargetPage getTargetPageById(long p_targetPageId)
            throws PageException
    {
        TargetPage page = null;
        try
        {
            page = (TargetPage) HibernateUtil.get(TargetPage.class,
                    p_targetPageId);
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

        return page;
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
     * @throws PageException
     *             when an error occurs.
     */
    static public Collection<?> getTemplateParts(Long p_sourcePageId,
            String p_pageTemplateType) throws PageException
    {
        Collection<?> result = null;

        try
        {
            boolean flag = BigTableUtil.isJobDataMigrated(p_sourcePageId);
            String hql = null;
            if (flag)
            {
                hql = "from TemplatePartArchived t "
                        + "where t.pageTemplate.typeValue = :type "
                        + "and t.pageTemplate.sourcePage.id = :pId order by t.order";
            }
            else
            {
                hql = "from TemplatePart t "
                        + "where t.pageTemplate.typeValue = :type "
                        + "and t.pageTemplate.sourcePage.id = :pId order by t.order";
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("type", p_pageTemplateType);
            map.put("pId", p_sourcePageId);

            result = HibernateUtil.search(hql, map);
        }
        catch (Exception ex)
        {
            String[] args =
            { p_sourcePageId.toString(), p_pageTemplateType };

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_TEMPLATE_PARTS, args, ex);
        }

        return result;
    }

    /**
     * Updates the state of each page back to its previous state. The page must
     * currently be in the UPDATING state to be set back to its previous state.
     * 
     * @param p_pages
     *            - The pages which states are being updated/reset.
     * @exception PageException
     *                - component exception thrown if the update fails.
     */
    static public void resetPagesToPreviousState(Collection<?> p_pages)
            throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            for (Iterator<?> i = p_pages.iterator(); i.hasNext();)
            {
                Page page = (Page) i.next();

                String prevState = page.getPageStateBeforeUpdating();
                if (CATEGORY.isDebugEnabled())
                {
                    // this could be a target page or source page
                    CATEGORY.debug("Resetting page " + page.getId()
                            + " back to its previous state " + prevState);
                }

                // register the object and use the clone for updating
                Page pageClone = (Page) session.get(page.getClass(),
                        page.getIdAsLong());
                if (pageClone != null)
                {
                    pageClone.setPageState(prevState);
                    session.update(pageClone);
                }
            }

            transaction.commit();
        }
        catch (Exception ex) // PersistenceException and TOPLinkException
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            CATEGORY.error(
                    "PagePersistenceAccessor :: resetPagesToPreviousState(List)",
                    ex);
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_PAGE_STATE, null, ex);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Get a collection of target pages for a given source page.
     * 
     * @param p_sourcePageId
     *            - The id of the original source page that the target page was
     *            derived from.
     * @return A collection of target pages based on the given source page id.
     * @throws PageException
     *             when an error occurs.
     */
    @SuppressWarnings("unchecked")
    public static Vector<?> getTargetPages(long p_sourcePageId)
            throws PageException
    {
        Vector<?> result = null;

        try
        {
            String hql = "from TargetPage tp where tp.sourcePage.id = :sId";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sId", new Long(p_sourcePageId));
            result = new Vector<Object>(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            String[] args =
            { Long.toString(p_sourcePageId) };

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_TARGET_PAGES_OF_SOURCE,
                    args, pe);
        }

        return result;
    }

    /**
     * Update the state of the specified pages.
     * 
     * @param p_pages
     *            - The pages where their state should be updated.
     * @param p_state
     *            - The state to be set.
     * @exception PageException
     *                - Component exception thrown upon an update.
     */
    static public void updateStateOfPages(Collection<?> p_pages, String p_state)
            throws PageException
    {
        updateStateOfPages(p_pages.toArray(), p_state);
    }

    //
    // package methods
    //

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
    static TargetPage getTargetPage(long p_sourcePageId, long p_localeId)
            throws PageException
    {
        try
        {
            String hql = "from TargetPage tp where tp.sourcePage.id = :sId "
                    + "and tp.workflowInstance.targetLocale.id = :lId";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sId", new Long(p_sourcePageId));
            map.put("lId", new Long(p_localeId));
            List<?> queryResult = HibernateUtil.search(hql, map);

            return queryResult.size() == 0 ? null : (TargetPage) queryResult
                    .get(0);
        }
        catch (Exception ex)
        {
            String[] args =
            { Long.toString(p_sourcePageId), Long.toString(p_localeId) };

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_TARGET_PAGE_BY_SOURCE_AND_LOCALE,
                    args, ex);
        }
    }

    /**
     * Get a source page based on the externalPageId, and source locale.
     * 
     * @param p_externalPageId
     *            - The external id of the page.
     * @param p_sourceLocale
     *            - The source locale of the page.
     * @return The current version of a source page based on the specified
     *         parameters.
     * @exception PageException
     *                - Component exception thrown upon a query error.
     */
    @SuppressWarnings("unchecked")
    static SourcePage getCurrentPageByNameAndLocale(String p_externalPageId,
            GlobalSightLocale p_sourceLocale) throws PageException
    {
        Vector<Object> queryArgs = new Vector<Object>(2);
        queryArgs.add(p_externalPageId);
        queryArgs.add(p_sourceLocale.getIdAsLong());

        try
        {
            String hql = "from SourcePage sp where sp.externalPageId = :eId "
                    + "and sp.request.l10nProfile.id = :lId "
                    + "and sp.pageState != :fState order by sp.id desc";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("eId", p_externalPageId);
            map.put("lId", p_sourceLocale.getIdAsLong());
            map.put("fState", PageState.IMPORT_FAIL);

            List<?> result = HibernateUtil.search(hql, map);

            return result.size() == 0 ? null : (SourcePage) result.get(0);
        }
        catch (Exception pe)
        {
            String[] args =
            { p_externalPageId, p_sourceLocale.getDisplayName() };

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_PAGE_BY_NAME_AND_LOCALE,
                    args, pe);
        }
    }

    /**
     * Get an active target page based on the externalPageId, source locale, and
     * target locale id and state.
     * 
     * @param p_externalPageId
     *            - The external id of the page.
     * @param p_sourceLocale
     *            - The source locale of the page.
     * @param p_targetLocale
     *            - The target locale of the page.
     * @return The active version of a target page page based on the specified
     *         parameters or NULL if none exists
     * @exception PageException
     *                - Component exception thrown upon a query error.
     */
    @SuppressWarnings("unchecked")
    static TargetPage getActiveTargetPageByNameAndLocales(
            String p_externalPageId, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws PageException
    {
        try
        {
            String hql = "select t.* from TargetPage t, RequestImpl r "
                    + "where t.sourcePage.id = r.sourcePageId and "
                    + "t.sourcePage.externalPageId = :externalPageId "
                    + "and t.sourcePage.dataType != null "
                    + "and r.l10nProfile.sourceLocale.id = :sourceLocaleId "
                    + "and t.workflowInstance.targetLocale.id =:targetLocaleId "
                    + "and t.pageState in ('" + PageState.IMPORTING + "', '"
                    + PageState.IMPORT_SUCCESS + "', '" + PageState.ACTIVE_JOB
                    + "', '" + PageState.LOCALIZED + "'))";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("externalPageId", p_externalPageId);
            map.put("sourceLocaleId", p_sourceLocale.getIdAsLong());
            map.put("targetLocaleId", p_targetLocale.getIdAsLong());
            List<?> queryResult = HibernateUtil.search(hql, map);

            return queryResult.size() == 0 ? null : (TargetPage) queryResult
                    .get(0);
        }
        catch (Exception pe)
        {
            String[] args =
            { p_externalPageId, p_sourceLocale.getDisplayName() };

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_PAGE_BY_NAME_AND_LOCALE,
                    args, pe);
        }
    }

    /**
     * Get all source pages that are stuck importing. Used on start-up.
     * 
     * @return Collection of source pages that are in 'IMPORTING' state.
     * @exception pageException
     *                - Component exception thrown upon an query error.
     */
    static Collection<?> getSourcePagesStillImporting() throws PageException
    {
        Collection<?> result = null;

        try
        {
            result = HibernateUtil
                    .searchWithSql(
                            SourcePage.class,
                            SourcePageDescriptorModifier.SOURCE_PAGES_STILL_IMPORTING_SQL);
        }
        catch (Exception pe)
        {
            CATEGORY.error(
                    "Failed to get the source pages that are stuck importing.",
                    pe);

            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_SOURCE_PAGES_STILL_IMPORTING,
                    null, pe);
        }

        return result;
    }

    /**
     * Persist the specified source page (along with LeverageGroups, Tus, and
     * Tuvs). This method will either insert or update a page. The decision is
     * made based on the id of the previous version of the page. A new page will
     * not have a "previous page id" and will be inserted.
     * 
     * @param p_sourcePage
     *            - The source page to be persisted.
     * @exception PageException
     *                - Component exception thrown upon an insert/update.
     */
    static void persistSourcePage(SourcePage p_sourcePage) throws PageException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_sourcePage);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to persist source page.", e);
            throw new PageException(
                    PageException.MSG_FAILED_TO_GET_SOURCE_PAGES_STILL_IMPORTING,
                    null, e);
        }
    }

    static void updateTargetPage(TargetPage p_targetPage) throws PageException
    {
        long pageId = p_targetPage.getId();
        if (pageId > 0)
        {
            try
            {
                HibernateUtil.update(p_targetPage);
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to update target page.", e);
                throw new PageException(e);
            }
        }
    }

    /**
     * Update the state of the specified sets of pages.
     * 
     * @param p_pages1
     *            - The first set of pages whose state should be updated.
     * @param p_state2
     *            - The state to be set.
     * @param p_pages2
     *            - The second set of pages whose state should be updated.
     * @param p_state2
     *            - The state to be set.
     * @exception PageException
     *                - Component exception thrown upon an update.
     */
    static void updateStateOfPages(Collection<?> p_pages1, String p_state1,
            Collection<?> p_pages2, String p_state2) throws PageException
    {
        updateStateOfPages(p_pages1.toArray(), p_state1, p_pages2.toArray(),
                p_state2);
    }

    /**
     * Update the state of the specified page.
     * 
     * @param p_page
     *            - The page that its state should be updated.
     * @param p_state
     *            - The state to be set.
     * @param p_exportError
     *            -- the exception xml related to an export error
     * @exception PageException
     *                - Component exception thrown upon an update.
     */
    static void updateStateOfPage(Page p_page, String p_state,
            String p_exportError) throws PageException
    {
        if (p_page instanceof TargetPage)
        {
            updateStateOfTargetPage((TargetPage) p_page, p_state, p_exportError);
        }
        else
        {
            Object[] pages =
            { p_page };
            updateStateOfPages(pages, p_state);
        }
    }

    /**
     * Update the state of the specified page.
     * 
     * @param p_page
     *            - The page that its state should be updated.
     * @param p_state
     *            - The state to be set.
     * @exception PageException
     *                - Component exception thrown upon an update.
     */
    static void updateStateOfPage(Page p_page, String p_state)
            throws PageException
    {
        Object[] pages =
        { p_page };
        updateStateOfPages(pages, p_state);
    }

    /**
     * Delete all the templates and the leverage groups for a source page.
     * Deletes the target pages too. This is called when a page fails import.
     * 
     * @param p_page
     *            The source page to delete from
     * @exception PageException
     *                - Component exception thrown upon delete error.
     */
    static void deleteOnImportFailure(SourcePage p_page,
            Collection<?> p_targetPages) throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            SourcePage pageClone = (SourcePage) session.get(SourcePage.class,
                    p_page.getIdAsLong());

            if (pageClone.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
            {
                ExtractedFile ef = (ExtractedFile) pageClone.getPrimaryFile();
                // delete the templates from the DB
                Collection<?> templates = ef.getTemplateMap().values();

                // remove the templates from page
                Set<?> templateTypes = ef.getTemplateMap().keySet();
                for (Iterator<?> ti = templateTypes.iterator(); ti.hasNext();)
                {
                    int templateType = ((Long) ti.next()).intValue();
                    ef.removePageTemplate(templateType);
                }

                session.delete(templates);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            throw new PageException(PageException.MSG_FAILED_TO_DELETE_PAGE,
                    null, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Updates the base href's in the page and persists to the database. If
     * either base href is NULL it won't update the page (leaves it with the
     * existing one).
     */
    static void updateBaseHrefs(Page p_page, String p_internalBaseHref,
            String p_externalBaseHref) throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            // if this is an extracted file then go ahead and update
            if (p_page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                Page pageClone = (Page) session.get(p_page.getClass(),
                        p_page.getIdAsLong());
                if (pageClone != null)
                {
                    ExtractedFile ef = (ExtractedFile) pageClone
                            .getPrimaryFile();

                    if (p_internalBaseHref != null)
                    {
                        ef.setInternalBaseHref(p_internalBaseHref);
                    }
                    if (p_externalBaseHref != null)
                    {
                        ef.setExternalBaseHref(p_externalBaseHref);
                    }
                    session.update(ef);
                }

                transaction.commit();
            }
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args = new String[3];
            args[0] = Long.toString(p_page.getId());
            if (p_internalBaseHref == null)
            {
                args[1] = "";
            }
            else
            {
                args[1] = p_internalBaseHref;
            }

            if (p_externalBaseHref == null)
            {
                args[2] = "";
            }
            else
            {
                args[2] = p_externalBaseHref;
            }

            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_BASE_HREFS, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Updates the word count on source pages - and also passes on the update to
     * the target pages. This is just no match word count update that the user
     * is allowed to update pages associated with un-extracted files.
     * 
     * @param p_pageWordCounts
     *            The hashmap contains a collection of source pages and their
     *            new word count to update it with. The key is a Long that is
     *            the page id and the value is a Long that is the new word
     *            count.
     */
    static void updateWordCount(HashMap<?, ?> p_pageWordCounts)
            throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Set<?> sourcePages = p_pageWordCounts.keySet();
            // iterate through all the source pages
            for (Iterator<?> it = sourcePages.iterator(); it.hasNext();)
            {
                Long spId = (Long) it.next();
                int wordCount = ((Integer) p_pageWordCounts.get(spId))
                        .intValue();

                // clone the source page - but first get it from the server
                // cache - just in case it changed by someone else while
                // this update is being done.
                SourcePage spClone = (SourcePage) session.get(SourcePage.class,
                        spId);

                if (spClone != null)
                {
                    boolean changeMade = false;
                    // if the word count is set to remove the override
                    // and the word count is currently overriden - then clear
                    // the
                    // override
                    if (wordCount == -1)
                    {
                        if (spClone.isWordCountOverriden())
                        {
                            spClone.clearOverridenWordCount();
                            changeMade = true;
                        }
                    }
                    // if there was a change on the word count - then override
                    else if (spClone.getWordCount() != wordCount)
                    {
                        // set the word count on the source page
                        spClone.overrideWordCount(wordCount);
                        changeMade = true;
                    }

                    // if the page is an unextracted file then set the target
                    // pages
                    // too
                    if (changeMade
                            && spClone.getPrimaryFileType() == PrimaryFile.UNEXTRACTED_FILE)
                    {
                        Vector<?> targetPages = getTargetPages(spId.longValue());
                        if (targetPages != null && targetPages.size() > 0)
                        {
                            // Vector tpClones =
                            // uow.registerAllObjects(targetPages);
                            // loop through the target pages too
                            for (int j = 0; j < targetPages.size(); j++)
                            {
                                TargetPage tp = (TargetPage) targetPages.get(j);
                                PageWordCounts pwc = tp.getWordCount();
                                // set the unmatched word count
                                pwc.setNoMatchWordCount(spClone.getWordCount());
                                // update the total word count
                                pwc.setTotalWordCount(pwc.getNoMatchWordCount()
                                        + pwc.getLowFuzzyWordCount()
                                        + pwc.getMedFuzzyWordCount()
                                        + pwc.getMedHiFuzzyWordCount()
                                        + pwc.getHiFuzzyWordCount()
                                        + pwc.getContextMatchWordCount()
                                        + pwc.getSegmentTmWordCount());
                                tp.setWordCount(pwc);
                                session.update(tp);
                            }
                        }
                    }
                }
            }

            // commit the changes
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_WORD_COUNT, null, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Persists any changes to the unextracted file information in a page
     * (either source or target). The method assumes the page being passed in
     * contains an un-extracted primary file.
     * 
     * @param p_modifiedPage
     *            The page that has had its un-extracted primary file updated.
     */
    static void updateUnextractedFile(Page p_modifiedPage) throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        boolean targetPage = true;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // get the original page
            Page pageClone = null;
            if (p_modifiedPage instanceof TargetPage)
            {
                // a target page
                pageClone = (TargetPage) session.get(TargetPage.class,
                        p_modifiedPage.getIdAsLong());
            }
            else
            // a source page
            {
                pageClone = (TargetPage) session.get(SourcePage.class,
                        p_modifiedPage.getIdAsLong());
                targetPage = false;
            }

            if (pageClone != null)
            {
                UnextractedFile uf = (UnextractedFile) pageClone
                        .getPrimaryFile();
                UnextractedFile modifiedUf = (UnextractedFile) p_modifiedPage
                        .getPrimaryFile();
                // now copy all the changes
                uf.setStoragePath(modifiedUf.getStoragePath());
                uf.setLastModifiedDate(modifiedUf.getLastModifiedDate());
                uf.setLastModifiedBy(modifiedUf.getLastModifiedBy());
                uf.setLength(modifiedUf.getLength());
                session.update(pageClone);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            CATEGORY.error(
                    "Failed to persist the un-extracted file updates in page "
                            + p_modifiedPage.getId(), e);
            String args[] =
            { targetPage ? "target" : "source",
                    Long.toString(p_modifiedPage.getId()) };
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_UNEXTRACTED_FILE, args,
                    e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Static package level methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    // update the state of two sets of page(s)
    // but does NOT update any page with the state of IMPORT_FAIL
    private static void updateStateOfPages(Object[] p_pages1, String p_state1,
            Object[] p_pages2, String p_state2) throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            for (int i = 0; i < p_pages1.length; i++)
            {
                Page page = (Page) p_pages1[i];

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Setting page " + page.getId()
                            + " to state " + p_state1);
                }

                // change the state if it isn't in the IMPORT_FAIL state
                if (!page.getPageState().equals(PageState.IMPORT_FAIL))
                {
                    // register the object and use the clone for updating
                    Page pageClone = (Page) session.get(page.getClass(),
                            page.getIdAsLong());
                    if (pageClone != null)
                    {
                        pageClone.setPageState(p_state1);
                        session.update(pageClone);
                    }
                }
            }

            for (int i = 0; i < p_pages2.length; i++)
            {
                Page page = (Page) p_pages2[i];

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Setting page " + page.getId()
                            + " to state " + p_state2);
                }

                // change the state if it isn't in the IMPORT_FAIL state
                if (!page.getPageState().equals(PageState.IMPORT_FAIL))
                {
                    // register the object and use the clone for updating
                    Page pageClone = (Page) session.get(page.getClass(),
                            page.getIdAsLong());
                    if (pageClone != null)
                    {
                        pageClone.setPageState(p_state2);
                        session.update(pageClone);
                    }
                }
            }

            transaction.commit();
        }
        catch (Exception ex) // PersistenceException and TOPLinkException
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            CATEGORY.error("PagePersistenceAccessor :: updateStateOfPage -- ",
                    ex);
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_PAGE_STATE, null, ex);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Updates the state of the pages and sets the export error field
     * 
     * @param p_pages
     *            is an array of pages
     * @param p_state
     *            is the state
     */
    private static void updateStateOfPages(Object[] p_pages, String p_state)
            throws PageException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            for (int i = 0, max = p_pages.length; i < max; i++)
            {
                Page page = (Page) p_pages[i];

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Setting page " + page.getId()
                            + " to state " + p_state);
                }

                // only change the state if not of IMPORT_FAIL state
                if (!page.getPageState().equals(PageState.IMPORT_FAIL))
                {
                    Class<?> ob = page.getClass().getName()
                            .startsWith(SourcePage.class.getName()) ? SourcePage.class
                            : TargetPage.class;
                    // register the object and use the clone for updating
                    Page pageClone = (Page) session.get(ob, page.getIdAsLong());
                    if (pageClone != null)
                    {
                        pageClone.setPageState(p_state);
                        session.update(pageClone);
                    }
                }
            }

            transaction.commit();
        }
        catch (Exception ex) // PersistenceException and TOPLinkException
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            CATEGORY.error("PagePersistenceAccessor :: updateStateOfPage -- ",
                    ex);
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_PAGE_STATE, null, ex);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Updates the state of the pages and sets the export error field
     * 
     * @param p_targetPage
     *            The target page to be updated
     * @param p_state
     *            The state to be set for the specified target page.
     * @param p_exportError
     *            The error to be set (if any).
     */
    private static void updateStateOfTargetPage(TargetPage p_targetPage,
            String p_state, String p_exportError) throws PageException
    {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Setting target page with id "
                        + p_targetPage.getId() + " to state " + p_state);
            }

            // only change the state if not of IMPORT_FAIL state
            if (!p_targetPage.getPageState().equals(PageState.IMPORT_FAIL))
            {
                p_targetPage = (TargetPage) session.get(TargetPage.class,
                        p_targetPage.getIdAsLong());
                p_targetPage.setPageState(p_state);

                if (p_exportError == null)
                {
                    p_targetPage.setErrorAsString((String) null);
                }
                else
                {
                    p_targetPage.setErrorAsString(p_exportError);
                }

                session.update(p_targetPage);
                transaction.commit();
            }
        }
        catch (Exception ex) // PersistenceException and TOPLinkException
        {
            transaction.rollback();
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_PAGE_STATE, null, ex);
        }
        finally
        {
            // session.close();
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////
}

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
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SnippetPageTemplate.Position;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.snippet.Snippet;
import com.globalsight.everest.snippet.SnippetImpl;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * @see TemplateManager interface
 */
public class TemplateManagerLocal implements TemplateManager
{
    private static final Logger c_logger = Logger
            .getLogger(TemplateManagerLocal.class);

    // used in searches
    private final static String DELETED_TAG = DiplomatNames.Attribute.DELETED
            + "=\"";

    private final static String GS_TAG = "<" + DiplomatNames.Element.GSA + " ";

    /**
     * Helper class that holds a template, template part and new skeleton value
     * that need to be persisted and updated in the same transaction.
     */
    private class UpdateRecord
    {
        // key of Template in page's template hashtable
        public int m_type;
        public SnippetPageTemplate m_template;
        public TemplatePart m_part;
        public String m_skeleton;

        public UpdateRecord(int p_type, SnippetPageTemplate p_template)
        {
            m_type = p_type;
            m_template = p_template;
        }
    }

    //
    // Public Interface Methods
    //

    /**
     * @see TemplateManager.addSnippet(String, SourcePage, GlobalSightLocale,
     *      int, Snippet)
     */
    public Page addSnippet(String p_user, long p_srcPageId, String p_locale,
            int p_position, String p_snippetName, String p_snippetLocale,
            long p_snippetId) throws TemplateException, RemoteException
    {
        if (p_srcPageId <= 0 || p_locale == null || p_locale.length() <= 0
                || p_position < 1 || p_snippetId <= 0)
        {
            c_logger.error("Invalid parameter(s) when calling addSnippet");

            String args[] =
            { "addSnippet" };
            throw new TemplateException(TemplateException.INVALID_PARAM, args,
                    null);
        }

        SourcePage sp = getPage(p_srcPageId);
        ExtractedFile ef = getExtractedFile(sp);
        Snippet s = getSnippet(p_snippetName, p_snippetLocale, p_snippetId);

        synchronized (sp)
        {
            ArrayList updates = getUpdateRecordsForPage(sp, ef, p_locale);

            doAddSnippet(p_user, sp, updates, p_position, s);
            persistUpdates(updates);
            setTemplateRecordsInPage(ef, updates);
        }

        c_logger.info("User " + p_user + " added snippet " + p_snippetName
                + " (id " + p_snippetId + ") to page " + sp.getExternalPageId()
                + " (id " + sp.getId() + ") in language " + p_locale);

        return sp;
    }

    /**
     * @see TemplateManager.deleteSnippet(String, SourcePage, GlobalSightLocale,
     *      int)
     */
    public Page deleteSnippet(String p_user, long p_srcPageId, String p_locale,
            int p_position) throws TemplateException, RemoteException
    {
        if (p_srcPageId <= 0 || p_locale == null || p_locale.length() <= 0
                || p_position < 1)
        {
            c_logger.error("Invalid parameter(s) when calling deleteSnippet");

            String args[] =
            { "deleteSnippet" };
            throw new TemplateException(TemplateException.INVALID_PARAM, args,
                    null);
        }

        SourcePage sp = getPage(p_srcPageId);
        ExtractedFile ef = getExtractedFile(sp);

        synchronized (sp)
        {
            ArrayList updates = getUpdateRecordsForPage(sp, ef, p_locale);

            doDeleteSnippet(p_user, sp, updates, p_position);
            persistUpdates(updates);
            setTemplateRecordsInPage(ef, updates);
        }

        c_logger.info("User " + p_user + " deleted the snippet at position "
                + p_position + " from page " + sp.getExternalPageId() + " (id "
                + sp.getId() + ") in language " + p_locale);

        return sp;
    }

    /**
     * @see TemplateManager.deleteContent(String, SourcePage, GlobalSightLocale,
     *      int)
     */
    public Page deleteContent(String p_user, long p_srcPageId, String p_locale,
            int p_position) throws TemplateException, RemoteException
    {
        if (p_srcPageId <= 0 || p_locale == null || p_locale.length() <= 0
                || p_position < 1)
        {
            c_logger.error("Invalid parameter(s) when calling deleteContent");

            String args[] =
            { "deleteContent" };
            throw new TemplateException(TemplateException.INVALID_PARAM, args,
                    null);
        }

        SourcePage sp = getPage(p_srcPageId);
        ExtractedFile ef = getExtractedFile(sp);

        synchronized (sp)
        {
            ArrayList updates = getUpdateRecordsForPage(sp, ef, p_locale);

            doDeleteContent(p_user, sp, updates, p_position, p_locale);
            persistUpdates(updates);
            setTemplateRecordsInPage(ef, updates);
        }

        c_logger.info("User " + p_user + " deleted content at position "
                + p_position + " from page " + sp.getExternalPageId() + " (id "
                + sp.getId() + ") in language " + p_locale);

        return sp;
    }

    /**
     * @see TemplateManager.undeleteContent(String, SourcePage,
     *      GlobalSightLocale, int)
     */
    public Page undeleteContent(String p_user, long p_srcPageId,
            String p_locale, int p_position) throws TemplateException,
            RemoteException
    {
        if (p_srcPageId <= 0 || p_locale == null || p_locale.length() <= 0
                || p_position < 1)
        {
            c_logger.error("Invalid parameter(s) when calling undeleteContent");

            String args[] =
            { "undeleteContent" };
            throw new TemplateException(TemplateException.INVALID_PARAM, args,
                    null);
        }

        SourcePage sp = getPage(p_srcPageId);
        ExtractedFile ef = getExtractedFile(sp);

        synchronized (sp)
        {
            ArrayList updates = getUpdateRecordsForPage(sp, ef, p_locale);

            doUndeleteContent(p_user, sp, updates, p_position, p_locale);
            persistUpdates(updates);
            setTemplateRecordsInPage(ef, updates);
        }

        c_logger.info("User " + p_user + " un-deleted content at position "
                + p_position + " in page " + sp.getExternalPageId() + " (id "
                + sp.getId() + ") in language " + p_locale);

        return sp;
    }

    //
    // Private Worker Methods
    //

    /**
     * Performs the addition of a snippet to all templates. If the snippet needs
     * to be added to a template part, the new parts are precomputed and
     * returned in p_updates. The caller needs to persist this list. If the
     * snippet must be added to another snippet, this method persists the
     * snippet and clears p_updates.
     */
    private void doAddSnippet(String p_user, SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, Snippet p_snippet)
            throws TemplateException
    {
        SnippetPageTemplate spt = ((UpdateRecord) p_updates.get(0)).m_template;
        Position pos = findPosition(spt, p_position);

        if (pos != null && pos.m_type == Position.ADD)
        {
            if (pos.isInPart())
            {
                // Precompute all parts in one go and let caller persist
                doAddSnippetInParts(p_sourcePage, p_updates, p_position,
                        p_snippet);
            }
            else
            {
                // Update and persist the snippet
                Snippet sn = spt.getSnippet(pos);
                String skeleton = sn.getContent();
                String newSkeleton = executeAddSnippet(skeleton, pos,
                        p_snippet, spt);

                updateSnippet(p_user, sn, newSkeleton);

                // all done, caller doesn't need to update templates
                p_updates.clear();
            }
        }
        else
        {
            c_logger.error("AddSnippet:" + " couldn't find the ADD position "
                    + p_position + " in source page " + p_sourcePage.getId()
                    + " that was relevant to the locale " + spt.getLocale());

            String args[] =
            { Integer.toString(p_position),
                    Long.toString(p_sourcePage.getId()), spt.getLocale() };

            throw new TemplateException(
                    TemplateException.FAILED_TO_FIND_POSITION, args, null);
        }
    }

    private void doAddSnippetInParts(SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, Snippet p_snippet)
            throws TemplateException
    {
        for (int i = 0, max = p_updates.size(); i < max; i++)
        {
            UpdateRecord r = (UpdateRecord) p_updates.get(i);
            SnippetPageTemplate spt = r.m_template;
            Position pos = findPosition(spt, p_position);

            if (pos != null && pos.m_type == Position.ADD)
            {
                TemplatePart part = pos.m_part;
                String skeleton = part.getSkeleton();
                String newSkeleton = executeAddSnippet(skeleton, pos,
                        p_snippet, spt);

                r.m_part = part;
                r.m_skeleton = newSkeleton;
            }
            else
            {
                c_logger.error("AddSnippetInParts:"
                        + " couldn't find the ADD position " + p_position
                        + " in source page " + p_sourcePage.getId()
                        + " that was relevant to the locale " + spt.getLocale());

                String args[] =
                { Integer.toString(p_position),
                        Long.toString(p_sourcePage.getId()), spt.getLocale() };

                throw new TemplateException(
                        TemplateException.FAILED_TO_FIND_POSITION, args, null);
            }
        }
    }

    private void doDeleteSnippet(String p_user, SourcePage p_sourcePage,
            ArrayList p_updates, int p_position) throws TemplateException
    {
        SnippetPageTemplate spt = ((UpdateRecord) p_updates.get(0)).m_template;
        Position pos = findPosition(spt, p_position);

        if (pos != null && pos.m_type == Position.ADDED)
        {
            if (pos.isInPart())
            {
                // Precompute all parts in one go and let caller persist
                doDeleteSnippetInParts(p_sourcePage, p_updates, p_position);
            }
            else
            {
                // Update and persist the snippet
                Snippet sn = spt.getSnippet(pos);
                String skeleton = sn.getContent();
                String newSkeleton = executeDeleteSnippet(skeleton, pos);

                updateSnippet(p_user, sn, newSkeleton);

                // all done, caller doesn't need to update templates
                p_updates.clear();
            }
        }
        else
        {
            c_logger.error("DeleteSnippet:"
                    + " couldn't find the ADDED position " + p_position
                    + " in source page " + p_sourcePage.getId()
                    + " that was relevant to the locale " + spt.getLocale());

            String args[] =
            { Integer.toString(p_position),
                    Long.toString(p_sourcePage.getId()), spt.getLocale() };

            throw new TemplateException(
                    TemplateException.FAILED_TO_FIND_POSITION, args, null);
        }
    }

    private void doDeleteSnippetInParts(SourcePage p_sourcePage,
            ArrayList p_updates, int p_position) throws TemplateException
    {
        for (int i = 0, max = p_updates.size(); i < max; i++)
        {
            UpdateRecord r = (UpdateRecord) p_updates.get(i);
            SnippetPageTemplate spt = r.m_template;
            Position pos = findPosition(spt, p_position);

            if (pos != null && pos.m_type == Position.ADDED)
            {
                TemplatePart part = pos.m_part;
                String skeleton = part.getSkeleton();
                String newSkeleton = executeDeleteSnippet(skeleton, pos);

                r.m_part = part;
                r.m_skeleton = newSkeleton;
            }
            else
            {
                c_logger.error("DeleteSnippetInParts:"
                        + " couldn't find the ADDED position " + p_position
                        + " in source page " + p_sourcePage.getId()
                        + " that was relevant to the locale " + spt.getLocale());

                String args[] =
                { Integer.toString(p_position), spt.getLocale() };
                throw new TemplateException(
                        TemplateException.FAILED_TO_FIND_POSITION, args, null);
            }
        }
    }

    private void doDeleteContent(String p_user, SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, String p_locale)
            throws TemplateException
    {
        SnippetPageTemplate spt = ((UpdateRecord) p_updates.get(0)).m_template;
        Position pos = findPosition(spt, p_position);

        if (pos != null && pos.m_type == Position.DELETE)
        {
            if (pos.isInPart())
            {
                // Precompute all parts in one go and let caller persist
                doDeleteContentInParts(p_sourcePage, p_updates, p_position,
                        p_locale);
            }
            else
            {
                // Update and persist the snippet
                Snippet sn = spt.getSnippet(pos);
                String skeleton = sn.getContent();
                String newSkeleton = executeDeleteContent(skeleton, pos,
                        p_locale);

                updateSnippet(p_user, sn, newSkeleton);

                // all done, caller doesn't need to update templates
                p_updates.clear();
            }
        }
        else
        {
            c_logger.error("DeleteContent:"
                    + " couldn't find the DELETE position " + p_position
                    + " in source page " + p_sourcePage.getId()
                    + " that was relevant to the locale " + spt.getLocale());

            String args[] =
            { Integer.toString(p_position),
                    Long.toString(p_sourcePage.getId()), spt.getLocale() };

            throw new TemplateException(
                    TemplateException.FAILED_TO_FIND_POSITION, args, null);
        }
    }

    private void doDeleteContentInParts(SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, String p_locale)
            throws TemplateException
    {
        for (int i = 0, max = p_updates.size(); i < max; i++)
        {
            UpdateRecord r = (UpdateRecord) p_updates.get(i);
            SnippetPageTemplate spt = r.m_template;
            Position pos = findPosition(spt, p_position);

            if (pos != null && pos.m_type == Position.DELETE)
            {
                TemplatePart part = pos.m_part;
                String skeleton = part.getSkeleton();
                String newSkeleton = executeDeleteContent(skeleton, pos,
                        p_locale);

                r.m_part = part;
                r.m_skeleton = newSkeleton;
            }
            else
            {
                c_logger.error("DeleteContentInParts:"
                        + " couldn't find the DELETE position " + p_position
                        + " in source page " + p_sourcePage.getId()
                        + " that was relevant to the locale " + spt.getLocale());

                String args[] =
                { Integer.toString(p_position),
                        Long.toString(p_sourcePage.getId()), spt.getLocale() };

                throw new TemplateException(
                        TemplateException.FAILED_TO_FIND_POSITION, args, null);
            }
        }
    }

    private void doUndeleteContent(String p_user, SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, String p_locale)
            throws TemplateException
    {
        SnippetPageTemplate spt = ((UpdateRecord) p_updates.get(0)).m_template;
        Position pos = findPosition(spt, p_position);

        if (pos != null && pos.m_type == Position.DELETED)
        {
            if (pos.isInPart())
            {
                // Precompute all parts in one go and let caller persist
                doUndeleteContentInParts(p_sourcePage, p_updates, p_position,
                        p_locale);
            }
            else
            {
                // Update and persist the snippet
                Snippet sn = spt.getSnippet(pos);
                String skeleton = sn.getContent();
                String newSkeleton = executeUndeleteContent(skeleton, pos,
                        p_locale);

                updateSnippet(p_user, sn, newSkeleton);

                // all done, caller doesn't need to update templates
                p_updates.clear();
            }
        }
        else
        {
            c_logger.error("UndeleteContent"
                    + " couldn't find the DELETED position " + p_position
                    + " in source page " + p_sourcePage.getId()
                    + " that was relevant to the locale " + spt.getLocale());

            String args[] =
            { Integer.toString(p_position),
                    Long.toString(p_sourcePage.getId()), spt.getLocale() };

            throw new TemplateException(
                    TemplateException.FAILED_TO_FIND_POSITION, args, null);
        }
    }

    private void doUndeleteContentInParts(SourcePage p_sourcePage,
            ArrayList p_updates, int p_position, String p_locale)
            throws TemplateException
    {
        for (int i = 0, max = p_updates.size(); i < max; i++)
        {
            UpdateRecord r = (UpdateRecord) p_updates.get(i);
            SnippetPageTemplate spt = r.m_template;
            Position pos = findPosition(spt, p_position);

            if (pos != null && pos.m_type == Position.DELETED)
            {
                TemplatePart part = pos.m_part;
                String skeleton = part.getSkeleton();
                String newSkeleton = executeUndeleteContent(skeleton, pos,
                        p_locale);

                r.m_part = part;
                r.m_skeleton = newSkeleton;
            }
            else
            {
                c_logger.error("UndeleteContentInParts:"
                        + " couldn't find the DELETED position " + p_position
                        + " in source page " + p_sourcePage.getId()
                        + " that was relevant to the locale " + spt.getLocale());

                String args[] =
                { Integer.toString(p_position),
                        Long.toString(p_sourcePage.getId()), spt.getLocale() };

                throw new TemplateException(
                        TemplateException.FAILED_TO_FIND_POSITION, args, null);
            }
        }
    }

    //
    // Helper methods
    //

    /**
     * Performs in-memory updates of page templates (after data has been
     * persisted).
     */
    private void setTemplateRecordsInPage(ExtractedFile p_ef,
            ArrayList p_updates)
    {
        for (int i = 0, max = p_updates.size(); i < max; i++)
        {
            UpdateRecord r = (UpdateRecord) p_updates.get(i);

            int type = r.m_type;
            SnippetPageTemplate template = r.m_template;
            TemplatePart part = r.m_part;

            template.updateTemplatePart(part);

            // Wed Jun 19 18:19:58 2002 this is a no-op as long as
            // the Toplink relationship between a page and its
            // PageTemplate objects is not broken.
            p_ef.addPageTemplate(template, type);
        }
    }

    private ArrayList getUpdateRecordsForPage(SourcePage p_sourcepage,
            ExtractedFile p_ef, String p_locale) throws TemplateException
    {
        ArrayList result = new ArrayList();

        // get the page templates from the database
        Set templateTypes = p_ef.getTemplateMap().keySet();
        for (Iterator it = templateTypes.iterator(); it.hasNext();)
        {
            int type = ((Long) it.next()).intValue();

            // get the page template from the page with the specified type
            PageTemplate pt = p_ef.getPageTemplate(type);

            refreshPageTemplate(p_sourcepage, pt);

            SnippetPageTemplate spt = new SnippetPageTemplate(pt, p_locale);

            result.add(new UpdateRecord(type, spt));
        }

        return result;
    }

    /**
     * Find the position specified by the position within the snippet page
     * template which is for a particular locale.
     * 
     * @throws TemplateException
     *             if the position is invalid (&lt;= 0, &gt;
     *             max_num_of_positions) or couldn't be found.
     * 
     */
    private Position findPosition(SnippetPageTemplate p_spt, int p_position)
            throws TemplateException
    {
        SnippetPageTemplateInterpreter engine = new SnippetPageTemplateInterpreter(
                p_spt);

        Position result = engine.findPosition(p_position);

        if (result == null)
        {
            c_logger.error("Template position " + p_position + " not found in "
                    + p_spt.getTotalPositionCount() + " positions");

            String[] args =
            { Integer.toString(p_position), p_spt.getLocale() };
            throw new TemplateException(TemplateException.INVALID_POSITION,
                    args, null);
        }

        return result;
    }

    private SourcePage getPage(long p_pageId) throws TemplateException
    {
        try
        {
            return ServerProxy.getPageManager().getSourcePage(p_pageId);
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find the source page with id " + p_pageId,
                    e);
            String args[] =
            { Long.toString(p_pageId) };
            throw new TemplateException(TemplateException.FAILED_TO_GET_PAGE,
                    args, e);
        }
    }

    /**
     * Assumes the page has an extracted file attatched, otherwise it wouldn't
     * have reached this code in the template manager.
     */
    private ExtractedFile getExtractedFile(Page p_page)
    {
        return (ExtractedFile) p_page.getPrimaryFile();
    }

    private Snippet getSnippet(String p_snippetName, String p_locale,
            long p_snippetId) throws TemplateException
    {
        Snippet result = null;

        try
        {
            result = ServerProxy.getSnippetLibrary().getSnippet(p_snippetName,
                    p_locale, p_snippetId);
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find snippet " + p_snippetName, e);

            String args[] =
            { p_snippetName, p_locale, Long.toString(p_snippetId) };
            throw new TemplateException(
                    TemplateException.FAILED_TO_GET_SNIPPET, args, e);
        }

        return result;
    }

    /**
     * Gets the page template from the source page, specified by type. Refreshes
     * the correct template parts from the database and loads them into the
     * PageTemplate.
     */
    private PageTemplate refreshPageTemplate(SourcePage p_sourcePage,
            PageTemplate p_template) throws TemplateException
    {
        try
        {
            // refresh the template parts from the database
            Collection parts = ServerProxy.getPageManager()
                    .getTemplatePartsForSourcePage(p_sourcePage.getIdAsLong(),
                            p_template.getTypeAsString());

            // set the template parts retrieved from the database
            p_template.setTemplateParts(new ArrayList(parts));
        }
        catch (Exception e)
        {
            c_logger.error("Exception when getting the template parts "
                    + "to create the SnippetPageTemplate for page "
                    + p_sourcePage.getId(), e);

            String args[] =
            { Long.toString(p_sourcePage.getId()), p_template.getTypeAsString() };

            throw new TemplateException(
                    TemplateException.FAILED_TO_GET_TEMPLATE_PARTS, args, e);
        }

        return p_template;
    }

    /**
     * Persists all changes to all template parts in one transaction.
     */
    private void persistUpdates(ArrayList p_updates) throws TemplateException
    {
        try
        {
            for (int i = 0, max = p_updates.size(); i < max; i++)
            {
                UpdateRecord r = (UpdateRecord) p_updates.get(i);
                TemplatePart part = r.m_part;
                part.setSkeleton(r.m_skeleton);
                HibernateUtil.saveOrUpdate(part);
            }
        }
        catch (Exception e)
        {
            c_logger.error("PersistenceException when updating "
                    + "the skeleton in the template parts.", e);

            throw new TemplateException(TemplateException.FAILED_TO_PERSIST,
                    null, e);
        }
    }

    /**
     * Updates a snippet with a new value for its content. The caller should
     * probably have synchronized on the snippet (its key) since multiple,
     * unrelated pages can include the same snippet and several Locale Managers
     * can update it at the same time.
     */
    private void updateSnippet(String p_user, Snippet p_snippet,
            String p_newSkeleton) throws TemplateException
    {
        // Need to create a copy or else we modify the TOPlink object.
        Snippet newSnippet = new SnippetImpl(p_snippet.getId(),
                p_snippet.getName(), p_snippet.getDescription(),
                p_snippet.getLocale(), p_newSkeleton);

        try
        {
            // the new snippet's content has already been validated -
            ServerProxy.getSnippetLibrary().modifySnippet(p_user, newSnippet,
                    false);
        }
        catch (Exception ex)
        {
            throw new TemplateException(TemplateException.FAILED_TO_PERSIST,
                    null, ex);
        }
    }

    //
    // Helpers to modify GS tags in TemplatePart or Snippet strings.
    //

    private String executeAddSnippet(String p_skeleton, Position p_pos,
            Snippet p_snippet, SnippetPageTemplate p_spt)
    {
        int tagStart = p_pos.m_start;

        // put ADDED tag before the ADD tag
        StringBuffer newSkeleton = new StringBuffer(p_skeleton.substring(0,
                tagStart));

        newSkeleton.append(GS_TAG);
        newSkeleton.append(DiplomatNames.Attribute.ADDED);
        newSkeleton.append("=\"");
        newSkeleton.append(p_spt.getLocale().toString());
        newSkeleton.append("\" ");
        newSkeleton.append(DiplomatNames.Attribute.NAME);
        newSkeleton.append("=\"");
        newSkeleton.append(EditUtil.encodeHtmlEntities(p_snippet.getName()));
        newSkeleton.append("\" ");

        // No ID or ID=0 indicates a generic snippet
        if (!p_snippet.isGeneric())
        {
            newSkeleton.append(DiplomatNames.Attribute.ID);
            newSkeleton.append("=\"");
            newSkeleton.append(p_snippet.getId());
            newSkeleton.append("\" ");
        }

        newSkeleton.append("/>");

        newSkeleton.append(p_skeleton.substring(tagStart));

        return newSkeleton.toString();
    }

    /**
     * Remove the snippet tag from the template by joining together what is
     * before the snippet and what is after it.
     */
    private String executeDeleteSnippet(String p_skeleton, Position p_pos)
    {
        StringBuffer result = new StringBuffer(p_skeleton.substring(0,
                p_pos.m_start));

        result.append(p_skeleton.substring(p_pos.m_end));

        return result.toString();
    }

    private String executeDeleteContent(String p_skeleton, Position p_pos,
            String p_locale)
    {
        StringBuffer result = new StringBuffer(p_skeleton);

        // find the DELETE tag
        String tag = p_skeleton.substring(p_pos.m_start, p_pos.m_end);
        StringBuffer tagBuff = new StringBuffer(tag);

        // if already has a DELETED in it
        int deletedOffset = tag.indexOf(DELETED_TAG);
        if (deletedOffset > -1)
        {
            // add the length of the DELETED=" part of the tag
            int start = deletedOffset + DELETED_TAG.length();
            // insert locale
            tagBuff.insert(start, p_locale + ",");
        }
        else
        // only a delete
        {
            // create the string DELETED="xx_XX"
            StringBuffer deleted = new StringBuffer(" ");
            deleted.append(DELETED_TAG);
            deleted.append(p_locale);
            deleted.append("\" ");

            int offset = tag.indexOf(GS_TAG);
            // offset after the <GS
            offset += GS_TAG.length();
            // insert the new deleted string
            tagBuff.insert(offset, deleted.toString());
        }

        result.replace(p_pos.m_start, p_pos.m_end, tagBuff.toString());

        return result.toString();
    }

    private String executeUndeleteContent(String p_skeleton, Position p_pos,
            String p_locale) throws TemplateException
    {
        StringBuffer result = new StringBuffer(p_skeleton);

        String tag = p_skeleton.substring(p_pos.m_start, p_pos.m_end);
        StringBuffer tagBuff = new StringBuffer(tag);

        // get the indexes into tag.
        int startOffset = tag.indexOf(p_locale);
        int endOffset = 5;

        // if there is a comma after the locale value
        if (tag.indexOf(p_locale + ",") > 0)
        {
            endOffset = 6;
        }

        // remove the locale and update
        tagBuff = tagBuff.delete(startOffset, startOffset + endOffset);
        tag = tagBuff.toString();

        result.replace(p_pos.m_start, p_pos.m_end, tag);

        return result.toString();
    }
}

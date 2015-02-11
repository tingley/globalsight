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

package com.globalsight.everest.corpus;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.corpusinterface.TuvMapping;
import com.globalsight.ling.tm2.segmenttm.SegmentTmPopulator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;

/**
 * Provides methods for accessing the corpus TM.
 */
public class CorpusManagerLocal implements CorpusManager
{
    static private final Logger c_logger = Logger
            .getLogger(CorpusManagerLocal.class);

    /**
     * Constant identifying UTF8 codeset.
     */
    static public final String UTF8 = "UTF8";

    private boolean m_isInstalled = false;

    /**
     * The partial context db column size. The size is actually a VARCHAR(4000)
     * but in the code an additional xml tag is appended to the context after
     * the comparison. So we'll take the length of the tag into consideration.
     */
    private static int PARTIAL_CONTEXT_COLUMN_SIZE = 3985;

    /**
     * A table to keep track of ms office docs that need deleting it stores name
     * --> count
     */
    private Hashtable m_table = new Hashtable();

    //
    // Inner Class
    //

    /**
     * RequestMap takes a list of source pages and sorts them by original
     * document. Multiple pages may have been created for Office files like
     * PowerPoint where one document results in multiple slides.
     * 
     * This class is used after import when CXE has finished sending messages
     * for pages that may have been created from a single document. It sorts
     * source pages by the original document they were created from.
     * 
     * CorpusManager will use this information to fix corpus documents of
     * generated pages to point to a single master. See
     * cleanUpMsOfficeJobSourcePages().
     */
    private class DocToPageMap
    {
        private HashMap m_docsToPages = new HashMap();

        DocToPageMap(ArrayList p_pages)
        {
            for (int i = 0, max = p_pages.size(); i < max; i++)
            {
                SourcePage page = (SourcePage) p_pages.get(i);
                BatchInfo info = page.getRequest().getBatchInfo();

                Long curPage = new Long(info.getPageNumber());

                ArrayList list = (ArrayList) m_docsToPages.get(curPage);
                if (list == null)
                {
                    list = new ArrayList();
                    m_docsToPages.put(curPage, list);
                }

                list.add(page);
            }
        }

        Iterator getDocKeyIterator()
        {
            return m_docsToPages.keySet().iterator();
        }

        ArrayList getDocPages(Long p_pageId)
        {
            return (ArrayList) m_docsToPages.get(p_pageId);
        }
    }

    public CorpusManagerLocal()
    {
        m_isInstalled = CorpusTm.isInstalled();
    }

    private void checkIfInstalled() throws CorpusException
    {
        if (!m_isInstalled)
        {
            throw new CorpusException("Corpus TM is not installed.");
        }
    }

    /**
     * Looks up the corpus doc (corpus_unit_variant) with the given ID.
     * 
     * @param p_id
     *            cuv_id
     * @return CorpusDoc
     */
    public CorpusDoc getCorpusDoc(Long p_id) throws RemoteException,
            CorpusException
    {
        checkIfInstalled();

        try
        {
            c_logger.debug("Querying for corpus doc " + p_id);
            CorpusDoc doc = (CorpusDoc) HibernateUtil
                    .get(CorpusDoc.class, p_id);
            c_logger.debug("Got corpus doc " + doc.getId());

            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException("Could not query corpus doc " + p_id);
        }
    }

    /**
     * Looks up the corpus doc (corpus_unit_variant) with the given name.
     * Returns a collection of corpus docs with names that match.
     * 
     * @param p_id
     *            cuv_id
     * @return CorpusDoc
     */
    public Collection getCorpusDoc(String p_name) throws RemoteException,
            CorpusException
    {
        checkIfInstalled();

        try
        {
            String searchString = "%" + p_name + "%";

            c_logger.debug("Querying for corpus doc with name like '"
                    + searchString + "'");

            String hql = "from CorpusDoc c where c.corpusDocGroup.corpusName like '"
                    + searchString + "' order by c.globalSightLocale.id";
            Collection result = HibernateUtil.search(hql, null);

            c_logger.debug("Got " + result.size() + "corpus docs.");

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException(
                    "Could not query corpus docs with name like '" + p_name
                            + "'");
        }
    }

    /**
     * Adds a row to the the corpus_unit and corpus_unit_variant tables. Sets
     * the id and cu_id for the returned CorpusDoc. Normal import calls this
     * method.
     * 
     * Note: This is invoked while creating job.
     * 
     * @param p_locale
     * @param p_name
     * @param p_gxmlContent
     * @param p_binaryContent
     * @param p_deleteBinary
     *            -- true if the binary file should be deleted after save
     * @return A src corpus doc that has been added to the corpus
     * @exception RemoteException
     * @exception CorpusException
     */
    public CorpusDoc addNewSourceLanguageCorpusDoc(SourcePage p_sourcePage,
            String p_gxmlContent, File p_binaryContent, boolean p_deleteBinary)
            throws RemoteException, CorpusException
    {
        checkIfInstalled();

        try
        {
            GlobalSightLocale locale = p_sourcePage.getGlobalSightLocale();
            String pagename = p_sourcePage.getExternalPageId();
            // for ppt,pptx,xls,xlsx, only store one copy of original source
            // files
            boolean canStoreNativeFormatDosc = true;
            if (pagename.endsWith(".ppt") || pagename.endsWith(".pptx"))
            {
                if (!pagename.startsWith("(slide0001)"))
                {
                    canStoreNativeFormatDosc = false;
                }
            }
            if (pagename.endsWith(".xls") || pagename.endsWith(".xlsx"))
            {
                if (!pagename.startsWith("(tabstrip)"))
                {
                    canStoreNativeFormatDosc = false;
                }
            }

            long docPageCount = p_sourcePage.getRequest().getBatchInfo()
                    .getDocPageCount();

            return addNewSourceLanguageCorpusDoc(locale, pagename,
                    docPageCount, p_gxmlContent, p_binaryContent,
                    p_deleteBinary, canStoreNativeFormatDosc);
        }
        catch (CorpusException ce)
        {
            throw ce;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException("Failed to add src lang doc to corpus");
        }
    }

    /**
     * Adds a row to the the corpus_unit and corpus_unit_variant tables. Sets
     * the id and cu_id for the returned CorpusDoc. Aligner Import calls this
     * method
     * 
     * Note: This is invoked while creating aligner package.
     * 
     * @param p_locale
     * @param p_name
     * @param p_gxmlContent
     * @param p_binaryContent
     * @param p_deleteBinary
     *            -- true if the binary file should be deleted after save
     * @param boolean p_canStoreNativeFormatDosc for xls,xlsx,ppt,pptx
     *        files,only one copy of source files should be stored.
     * @return A src corpus doc that has been added to the corpus
     * @exception RemoteException
     * @exception CorpusException
     */
    public CorpusDoc addNewSourceLanguageCorpusDoc(GlobalSightLocale p_locale,
            String p_pageName, String p_gxmlContent, File p_binaryContent,
            boolean p_canStoreNativeFormatDosc) throws RemoteException,
            CorpusException
    {
        long docPageCount = -1; // doesn't matter
        boolean deleteBinary = false; // don't delete binaries when used by
                                      // aligner

        return addNewSourceLanguageCorpusDoc(p_locale, p_pageName,
                docPageCount, p_gxmlContent, p_binaryContent, deleteBinary,
                p_canStoreNativeFormatDosc);
    }

    /**
     * Private implementation
     */
    private CorpusDoc addNewSourceLanguageCorpusDoc(GlobalSightLocale p_locale,
            String p_pageName, long p_docPageCount, String p_gxmlContent,
            File p_binaryContent, boolean p_deleteBinary,
            boolean p_canStoreNativeFormatDosc) throws RemoteException,
            CorpusException
    {
        String name = p_pageName;
        GlobalSightLocale locale = p_locale;
        try
        {
            CorpusDocGroup cdg = new CorpusDocGroup();
            cdg.setCorpusName(name);
            CorpusDoc doc = new CorpusDoc();
            doc.setLocale(locale);
            doc.setStoreDate(new Date());
            doc.setCorpusDocGroup(cdg);
            cdg.addCorpusDoc(doc);

            HibernateUtil.save(cdg);
            HibernateUtil.save(doc);

            c_logger.debug("After commit cdg id is " + cdg.getId());
            c_logger.debug("After commit doc id is " + doc.getId());

            // make a relative path for writing out the GXML and native format
            // docs
            c_logger.debug("Saving source GXML to " + doc.getGxmlPath());

            ServerProxy.getNativeFileManager().save(p_gxmlContent, UTF8,
                    doc.getGxmlPath());

            if (CorpusTm.isStoringNativeFormatDocs()
                    && p_canStoreNativeFormatDosc)
            {
                c_logger.debug("Saving native format to "
                        + doc.getNativeFormatPath());

                // Don't delete
                ServerProxy.getNativeFileManager().copyFileToStorage(
                        p_binaryContent.getAbsolutePath(),
                        doc.getNativeFormatPath(), false);
            }
            else
            {
                c_logger.debug("NOT saving native format document.");
            }

            if (p_deleteBinary)
            {
                if (canDeleteFile(p_binaryContent, (int) p_docPageCount))
                {
                    p_binaryContent.delete();
                }
            }

            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException("Failed to add src lang doc to corpus");
        }
    }

    /**
     * Adds a row to the corpus_unit_variant table so that the new row for the
     * target-language doc is related to the same corpus_unit entry as the src
     * document. Sets the ids and name in the target doc and returns it.
     * 
     * Note: This is invoked while creating aligner package or exporting.
     * 
     * @param p_src
     *            src corpus doc
     * @param p_tgt
     *            target corpus doc
     * @return modified target corpus doc
     */
    public CorpusDoc addNewTargetLanguageCorpusDoc(CorpusDoc p_src,
            GlobalSightLocale p_targetLocale, String p_gxml, File p_binaryData,
            boolean p_canStoreNativeFormatDosc) throws RemoteException,
            CorpusException
    {
        checkIfInstalled();

        try
        {
            // create the corpus doc
            CorpusDoc doc = new CorpusDoc();
            doc.setLocale(p_targetLocale);
            doc.setStoreDate(new Date());
            CorpusDocGroup cdg = p_src.getCorpusDocGroup();
            doc.setCorpusDocGroup(cdg);
            HibernateUtil.save(doc);

            c_logger.debug("After commit cdg id is " + cdg.getId());
            c_logger.debug("After commit doc id is " + doc.getId());

            // make a relative path for writing out the GXML
            c_logger.debug("Saving target GXML to " + doc.getGxmlPath());
            ServerProxy.getNativeFileManager().save(p_gxml, UTF8,
                    doc.getGxmlPath());

            // save original document
            if (CorpusTm.isStoringNativeFormatDocs() && p_binaryData != null
                    && p_canStoreNativeFormatDosc)
            {
                c_logger.debug("Saving native format to "
                        + doc.getNativeFormatPath());

                // Don't delete
                ServerProxy.getNativeFileManager().copyFileToStorage(
                        p_binaryData.getAbsolutePath(),
                        doc.getNativeFormatPath(), false);
            }
            else
            {
                c_logger.debug("NOT saving  native format document.");
            }

            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException("Failed to add target lang doc to corpus");
        }
    }

    /**
     * Maps the segments (project_tm_tuv_t) to the specified corpus doc by
     * making new entries in the corpus_map table
     * 
     * @param p_segments
     *            list of project_tm_tuv_t ids
     * @param p_doc
     *            corpus doc to map
     * @exception RemoteException
     * @exception CorpusException
     */
    public void mapSegmentsToCorpusDoc(List<TuvMapping> p_segments,
            CorpusDoc p_doc) throws RemoteException, CorpusException
    {
        checkIfInstalled();

        // UnitOfWork uow = null;
        // Session session = null;
        synchronized (SegmentTmPopulator.LOCK)
        {
            Session session = null;
            Transaction transaction = null;
            try
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                XmlParser xmlParser = XmlParser.hire();
                String gxml = ServerProxy.getNativeFileManager().getString(
                        p_doc.getGxmlPath(), UTF8);
                GxmlParser gxmlParser = new GxmlParser(xmlParser, gxml);
                for (TuvMapping tuvmapping : p_segments)
                {
                    CorpusContext context = new CorpusContext();
                    context.setCorpusDoc(p_doc);
                    context.setTuvId(new Long(tuvmapping.getProjectTmTuvId()));
                    context.setTuId(new Long(tuvmapping.getProjectTmTuId()));
                    context.setTmId(tuvmapping.getTmId());

                    c_logger.debug("Adding mapping: " + context.getTuvId()
                            + " --> " + context.getCuvId());

                    String partialContext = gxmlParser.getLinesOfContext(1,
                            tuvmapping.getProjectTmTuId());

                    c_logger.debug("Partial context: '" + partialContext + "'");

                    context.setPartialContext(partialContext);
                    session.save(context);

                    p_doc.isMapped(true);
                    session.update(p_doc);
                }

                XmlParser.fire(xmlParser);

                c_logger.debug("Commiting mappings to database.");

                transaction.commit();
            }
            catch (Exception e)
            {
                if (transaction != null)
                {
                    transaction.rollback();
                }

                c_logger.error("Failed to add corpus mappings: ", e);

                throw new CorpusException("Failed to add corpus mappings");

            }
            finally
            {
                if (session != null)
                {
                    // session.close();
                }
            }
        }
    }

    /**
     * Returns an array list of CorpusContextHolder objects that correspond to
     * map entries mapping the tuv in the targetLocale to target language
     * documents and source language documents in the corpus.
     * 
     * @param p_projectTuvId
     *            the project_tm_tuv_t id
     * @param p_sourceLocale
     *            source locale db id
     * @return ArrayList of CorpusContextHolder
     * @exception RemoteException
     * @exception CorpusException
     */
    public ArrayList getCorpusContextsForSegment(long p_projectTuvId,
            long p_sourceLocale) throws RemoteException, CorpusException
    {
        checkIfInstalled();
        ArrayList context = new ArrayList();

        try
        {
            // first get the corpus mapping objects corresponding to this tuv
            c_logger.debug("Querying for corpus contexts for " + p_projectTuvId);

            String hql = "from CorpusContext c where c.tuvId = :tuvId";
            HashMap map = new HashMap();
            map.put("tuvId", new Long(p_projectTuvId));
            Collection result = HibernateUtil.search(hql, map);

            c_logger.debug("There are " + result.size() + " corpus contexts.");

            Iterator iter = result.iterator();
            int idx = -1;
            while (iter.hasNext())
            {
                idx++;

                // for each corpus context, look up the corresponding source
                // context
                CorpusContext targetContext = (CorpusContext) iter.next();
                CorpusDoc targetCorpusDoc = getCorpusDoc(targetContext
                        .getCuvId());
                CorpusDocGroup docGroup = targetCorpusDoc.getCorpusDocGroup();

                c_logger.debug("Executing named query with args: "
                        + targetContext.getTuId() + "," + p_sourceLocale + ","
                        + docGroup.getIdAsLong());

                hql = "from CorpusContext c where c.tuId = :tuId "
                        + "and c.corpusDoc.globalSightLocale.id = :localId "
                        + "and c.corpusDoc.corpusDocGroup.id = :cuId";
                map = new HashMap();
                map.put("tuId", targetContext.getTuId());
                map.put("localId", new Long(p_sourceLocale));
                map.put("cuId", docGroup.getIdAsLong());
                Collection result2 = HibernateUtil.search(hql, map);

                if (result == null || result2.size() == 0)
                {
                    c_logger.warn("Could not find source context for ("
                            + targetContext.getTuvId() + "," + p_sourceLocale
                            + "," + docGroup.getId() + ")");
                }
                else if (result2.size() > 1)
                {
                    // this is a strange situation that shouldn't
                    // happen, so just output some debugging here
                    c_logger.debug("CORPUS_CONTEXT_BY_TU_LOCALE_CU returned more than one match for tuv id: "
                            + p_projectTuvId + " and locale " + p_sourceLocale);

                    // walk through the results and print out this information
                    Object[] contexts = result2.toArray();
                    for (int i = 0; i < result2.size(); i++)
                    {
                        CorpusContext ctx = (CorpusContext) contexts[i];

                        c_logger.debug("Got context id=" + ctx.getIdAsLong()
                                + ",cuvId=" + ctx.getCuvId() + ",tuvId="
                                + ctx.getTuvId() + ",tuId=" + ctx.getTuId()
                                + ",name=" + ctx.getName());
                    }

                    // just add the first one or the idx one
                    int choice = 0;
                    if (contexts.length >= idx)
                    {
                        choice = idx;
                    }

                    CorpusContext sourceContext = (CorpusContext) contexts[choice];

                    c_logger.debug("Using src context[" + choice + "] "
                            + sourceContext.getIdAsLong());

                    context.add(new CorpusContextHolder(sourceContext,
                            targetContext));
                }
                else
                {
                    CorpusContext sourceContext = (CorpusContext) result2
                            .toArray()[0];

                    c_logger.debug("Got src context "
                            + sourceContext.getIdAsLong());

                    context.add(new CorpusContextHolder(sourceContext,
                            targetContext));
                }
            }

            c_logger.debug("There are " + context.size() + " context holders.");

            return context;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CorpusException(
                    "Failed to get mapped segments from database.");
        }
    }

    /**
     * Cleans the corpus of the source corpus doc. It also removes the
     * association between the source page and the source corpus doc. This
     * method is to be used when discarding a job (before Localized state)
     * 
     * @param p_sourcePage
     *            source page
     */
    public void removeSourceCorpusDoc(SourcePage p_sourcePage)
            throws RemoteException, CorpusException
    {
        checkIfInstalled();

        Long cuvId = p_sourcePage.getCuvId();

        if (cuvId == null)
        {
            // nothing to do
            return;
        }

        Transaction transaction = null;
        try
        {
            c_logger.info("Removing corpus doc " + cuvId + " for "
                    + p_sourcePage.getExternalPageId());

            CorpusDoc srcCorpusDoc = getCorpusDoc(cuvId);

            // get the native format doc and the gxml and remove them
            File gxmlFile = ServerProxy.getNativeFileManager().getFile(
                    srcCorpusDoc.getGxmlPath());

            if (gxmlFile.exists())
            {
                c_logger.info("Deleting gxml file "
                        + srcCorpusDoc.getGxmlPath());

                doDelete(gxmlFile);
            }

            File nativeFormatFile = ServerProxy.getNativeFileManager().getFile(
                    srcCorpusDoc.getNativeFormatPath());

            if (nativeFormatFile.exists())
            {
                c_logger.info("Deleting native format file "
                        + srcCorpusDoc.getNativeFormatPath());

                doDelete(nativeFormatFile);
            }

            transaction = HibernateUtil.getTransaction();

            // remove the doc group
            CorpusDocGroup docGroup = srcCorpusDoc.getCorpusDocGroup();
            HibernateUtil.delete(srcCorpusDoc);
            HibernateUtil.delete(docGroup);

            // remove the link from the source page to the corpus doc
            p_sourcePage.setCuvId(null);
            HibernateUtil.update(p_sourcePage);

            HibernateUtil.commit(transaction);
        }
        catch (Exception e)
        {
            c_logger.error("Could not remove corpus doc " + cuvId, e);
            HibernateUtil.rollback(transaction);
        }
    }

    /**
     * Removes the named corpus doc. If the number of docs in the group is 2,
     * then both docs and the group are removed as well. This also unlinks the
     * pages and removes all corpus context mappings.
     */
    public void removeCorpusDoc(Long p_corpusDocId) throws RemoteException,
            CorpusException
    {
        checkIfInstalled();

        Long cuvId = p_corpusDocId;
        Session session = null;
        Transaction transaction = null;
        CorpusDoc corpusDoc = null;
        try
        {
            try
            {
                corpusDoc = getCorpusDoc(cuvId);
            }
            catch (CorpusException ce)
            {
                c_logger.info(ce.getMessage());
            }

            if (corpusDoc != null)
            {
                CorpusDocGroup cdg = corpusDoc.getCorpusDocGroup();

                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                // first see if we need to delete the whole unit. this is
                // true if the corpus group contains only one source and
                // one target doc, or only one source doc
                int numDocs = cdg.getCorpusDocs().size();

                c_logger.debug("Corpus docs size is " + numDocs);

                if (numDocs < 3)
                {
                    c_logger.debug("Deleting whole group");

                    for (int i = 0; i < numDocs; i++)
                    {
                        // the list is shrinking as we delete, so it's ok
                        // to always grab the first one (index 0)
                        CorpusDoc d = (CorpusDoc) cdg.getCorpusDocs().get(i);
                        deleteCorpusDoc(cdg, d, session, true);
                    }

                    // delete the dog group
                    session.delete(cdg);
                }
                else
                {
                    // just delete this one corpus doc
                    c_logger.debug("Deleting single cuv");

                    deleteCorpusDoc(cdg, corpusDoc, session, true);
                }

                corpusDoc = null;

                c_logger.debug("Calling commit.");

                transaction.commit();
            }

        }
        catch (Exception e)
        {
            c_logger.error("Could not remove corpus doc " + cuvId, e);

            if (transaction != null)
            {
                transaction.rollback();
            }
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
     * Deletes the corpus doc and all its associated mappings.
     * 
     * @param p_groupClone
     *            corpus doc group
     * @param p_corpusDoc
     *            corpus doc
     * 
     * @param p_uow
     *            unit of work
     * @param p_unlinkPages
     *            if true then the associated src/target pages are unlinked
     * @exception Exception
     */
    private void deleteCorpusDoc(CorpusDocGroup p_groupClone,
            CorpusDoc p_corpusDoc, Session session, boolean p_unlinkPages)
            throws Exception
    {
        Long cuvId = p_corpusDoc.getIdAsLong();

        c_logger.info("Removing corpus doc " + p_corpusDoc.getLocale()
                + " for " + p_groupClone.getCorpusName());

        // find the source or target page that used this cuv and set it to null
        if (p_unlinkPages)
        {
            String hql = "from SourcePage s where s.cuvId = :cuvId";
            java.util.Map map = new HashMap();
            map.put("cuvId", cuvId);
            List sourcePages = HibernateUtil.search(hql, map);
            for (int i = 0; i < sourcePages.size(); i++)
            {
                SourcePage sourcePage = (SourcePage) sourcePages.get(i);
                sourcePage.setCuvId(null);
            }
            HibernateUtil.update(sourcePages);

            hql = "from TargetPage t where t.cuvId = :cuvId";
            List targetPages = HibernateUtil.search(hql, map);
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                targetPage.setCuvId(null);
            }
            HibernateUtil.update(targetPages);
        }

        // delete the mappings
        deleteCorpusMappings(cuvId, session);

        // get the native format doc and the gxml and remove them
        String gxmlPath = p_corpusDoc.getGxmlPath();
        String nativeFormatPath = p_corpusDoc.getNativeFormatPath();
        File gxmlFile = ServerProxy.getNativeFileManager().getFile(gxmlPath);
        File nativeFormatFile = ServerProxy.getNativeFileManager().getFile(
                nativeFormatPath);
        // now delete the corpus doc
        session.delete(p_corpusDoc);

        if (gxmlFile.exists())
        {
            c_logger.debug("Deleting gxml file " + gxmlPath);
            doDelete(gxmlFile);
        }
        if (nativeFormatFile.exists())
        {
            c_logger.debug("Deleting native format file " + nativeFormatPath);
            doDelete(nativeFormatFile);
        }
    }

    /**
     * Deletes the corpus mappings as a separate unit of work. This is necessary
     * because the mappings are mapped to corpus_unit_variant as a long id, not
     * by object mapping.
     * 
     * @param p_cuvId
     *            cuv id
     * @exception Exception
     */
    private void deleteCorpusMappings(Long p_cuvId, Session session)
            throws Exception
    {
        // Now delete any mappings...unfortunately we have to first
        // query them out first
        java.util.Map map = new HashMap();
        map.put("cuvId", p_cuvId);
        String hql = "from CorpusContext c where c.corpusDoc.id = :cuvId";
        List corpusContexts = HibernateUtil.search(hql, map);
        HibernateUtil.delete(corpusContexts);
    }

    /**
     * Makes all source pages in the job use the same native format document,
     * and deletes whatever they're currently using. The import process could
     * not make them share because of threading issues; therefore this cleanup
     * is necessary.
     * 
     * If the job is not an ms office job, then nothing happens
     * 
     * @param p_jobId
     *            job id
     */
    public void cleanUpMsOfficeJobSourcePages(long p_jobId)
            throws RemoteException, CorpusException
    {
        checkIfInstalled();

        if (!CorpusTm.isStoringNativeFormatDocs())
        {
            return;
        }

        // get job from toplink cache
        Session session = null;
        Transaction transaction = null;
        try
        {
            c_logger.debug("Cleaning up ms office job in the corpus.");

            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            ArrayList sourcePages = new ArrayList(job.getSourcePages());

            // Sort the source pages by document they came from.
            DocToPageMap map = new DocToPageMap(sourcePages);

            // Then clean up each document individually.
            for (Iterator it = map.getDocKeyIterator(); it.hasNext();)
            {
                Long key = (Long) it.next();

                // The list of all source pages belonging to the same document.
                ArrayList docPages = map.getDocPages(key);

                SourcePage firstSourcePage = (SourcePage) docPages.get(0);

                if (!SourcePage.isMicrosoftOffice(firstSourcePage))
                {
                    continue;
                }

                String originalSourceFileContentName = firstSourcePage
                        .getRequest().getOriginalSourceFileContent();

                if (originalSourceFileContentName != null)
                {
                    File f = new File(originalSourceFileContentName);
                    if (f.exists())
                    {
                        c_logger.debug("Deleting original shared ms office file: "
                                + f.getAbsolutePath());

                        doDelete(f);
                    }
                }
                else
                {
                    c_logger.warn("Request did not store the name of the original shared ms office file for sourcepage "
                            + firstSourcePage.getId());
                }

                CorpusDoc firstCorpusDoc = getCorpusDoc(firstSourcePage
                        .getCuvId());
                // the main document to use for all other generated documents
                String officialNFP = firstCorpusDoc.getNativeFormatPath();

                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                for (int i = 1; i < docPages.size(); i++)
                {
                    SourcePage sp = (SourcePage) docPages.get(i);
                    CorpusDoc cd = getCorpusDoc(sp.getCuvId());
                    String oldPath = cd.getNativeFormatPath();
                    cd.setNativeFormatPath(officialNFP);
                    session.update(cd);

                    File oldFile = ServerProxy.getNativeFileManager().getFile(
                            oldPath);
                    if (oldFile.exists())
                    {
                        doDelete(oldFile);

                        c_logger.debug("Deleted old file " + oldPath);
                    }

                    c_logger.debug("changed from " + oldPath + " to "
                            + officialNFP);
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
            e.printStackTrace();
            throw new CorpusException(
                    "Failed to set all MS office source pages in job to use same native format file.");
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
     * Makes all target pages in the job's workflows use the same native format
     * document.
     * 
     * If the job is not an ms office job, then nothing happens
     * 
     * @param p_jobId
     *            job id
     */
    public void cleanUpMsOfficeJobTargetPages(long p_targetPageId)
            throws RemoteException, CorpusException
    {
        checkIfInstalled();

        if (!CorpusTm.isStoringNativeFormatDocs())
        {
            return;
        }

        Session session = null;
        Transaction transaction = null;
        try
        {
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    p_targetPageId);

            if (!SourcePage.isMicrosoftOffice(tp.getSourcePage()))
            {
                return;
            }

            Job job = tp.getWorkflowInstance().getJob();

            c_logger.debug("cleaning up target pages for job "
                    + job.getJobName());

            Iterator workflowsIter = job.getWorkflows().iterator();
            while (workflowsIter.hasNext())
            {
                Workflow w = (Workflow) workflowsIter.next();

                c_logger.debug("workflow " + w.getTargetLocale()
                        + " is in state " + w.getState());

                if (!w.getState().equals(Workflow.EXPORTED))
                {
                    continue;
                }

                // make all pages in this workflow use the same
                Vector targetPages = tp.getWorkflowInstance().getTargetPages();
                // first run through and find the one page that
                // actually has a native format file associated with it
                String officialNFP = null;
                int officialIndex = -1;
                for (int i = 0; i < targetPages.size(); i++)
                {
                    tp = (TargetPage) targetPages.get(i);
                    CorpusDoc cd = getCorpusDoc(tp.getCuvId());
                    File nf = ServerProxy.getNativeFileManager().getFile(
                            cd.getNativeFormatPath());

                    if (nf.exists())
                    {
                        officialNFP = cd.getNativeFormatPath();
                        officialIndex = i;
                        break;
                    }
                }

                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                for (int i = 0; i < targetPages.size(); i++)
                {
                    if (i == officialIndex)
                    {
                        continue;
                    }

                    tp = (TargetPage) targetPages.get(i);
                    CorpusDoc cd = getCorpusDoc(tp.getCuvId());
                    String oldPath = cd.getNativeFormatPath();
                    cd.setNativeFormatPath(officialNFP);
                    session.update(cd);

                    c_logger.debug("changed from " + oldPath + " to "
                            + officialNFP);
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

            e.printStackTrace();
            throw new CorpusException(
                    "Failed to set all MS office target pages"
                            + " in job to use same native format file.");
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
     * If the file does not exist in the hashtable, then the file and pagecount
     * - 1 are put into the table. On each access, the current count is checked.
     * When it hits 0, the file is deleted. This allows many ms office files to
     * share the same file.
     * 
     * @param p_file
     *            shared file
     * @param p_pageCount
     *            page count in batch
     */
    private synchronized boolean canDeleteFile(File p_file, int p_docPageCount)
    {
        if (p_docPageCount == 1)
        {
            // no need to put it in the map
            return true;
        }

        // see if the file is in the map already
        Integer count = (Integer) m_table.get(p_file.getAbsolutePath());
        if (count == null)
        {
            // not in table, so add it
            m_table.put(p_file.getAbsolutePath(), new Integer(
                    p_docPageCount - 1));
        }
        else
        {
            // it's in the table, so update the count
            int newCount = count.intValue() - 1;
            if (newCount == 0)
            {
                m_table.remove(p_file.getAbsolutePath());
                return true;
            }
            else
            {
                // update count
                m_table.put(p_file.getAbsolutePath(), new Integer(newCount));
            }
        }

        return false;
    }

    /** Deletes the files and removes any empty parent directories */
    private void doDelete(File p_file)
    {
        try
        {
            // first delete the file
            p_file.delete();

            // now travel up the hierarchy and see if there are parents to
            // delete
            File parent = null;
            File child = p_file;
            while ((parent = child.getParentFile()) != null)
            {
                child = parent;
                String[] children = child.list();
                if (children.length == 0)
                {
                    child.delete();
                }
                else
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Problems deleting file.", e);
        }
    }

    /**
     * Used to parse the GXML to find lines of context.
     */
    private static class GxmlParser
    {
        XmlParser m_xmlParser;
        Document m_document;
        Element m_root;
        Object[] m_nodes;

        /**
         * Creates a GxmlParser using the given XmlParser and string of GXML
         * 
         * @param p_xmlParser
         *            (already hired)
         * @param p_gxml
         */
        GxmlParser(XmlParser p_xmlParser, String p_gxml) throws Exception
        {
            m_xmlParser = p_xmlParser;
            m_document = m_xmlParser.parseXml(p_gxml);
            m_root = m_document.getRootElement();
            m_nodes = m_root.selectNodes("//translatable/segment").toArray();
        }

        /**
         * Gets the <p_numLines> of content before and after the specific
         * segment identified by the tu id. They are returned as gxml snippets
         * in a <context></context> block.
         * 
         * @param p_numLines
         *            1,2,3..
         * @param p_tuId
         * @return String
         */
        String getLinesOfContext(int p_numLines, long p_tuId) throws Exception
        {
            int i = 0;
            boolean foundLine = false;
            StringBuffer sb = new StringBuffer();
            String goalTuId = Long.toString(p_tuId);
            while (foundLine == false && i < m_nodes.length)
            {
                Element t = (Element) m_nodes[i];
                Attribute a = t.attribute("tuid");
                if (a != null && goalTuId.equals(a.getValue()))
                {
                    foundLine = true;
                    // make it well formed XML for the XSLT processing
                    sb.append("<context>\r\n");
                }
                else
                {
                    i++;
                }
            }

            int initialSize = sb.length();
            int sbSize = initialSize;
            for (int j = i - p_numLines; foundLine && j < i + p_numLines + 1; j++)
            {
                if (j > -1 && j < m_nodes.length)
                {
                    Element t = (Element) m_nodes[j];
                    // make sure it's not more than the limited size in db
                    if (sbSize + t.asXML().getBytes(UTF8).length > PARTIAL_CONTEXT_COLUMN_SIZE)
                    {
                        if (sbSize == initialSize)
                        {
                            sb.append(getPlaceholderSegment());
                        }
                        break;
                    }

                    sb.append(t.asXML());
                    sb.append("\r\n");
                    sbSize = sb.toString().getBytes(UTF8).length;
                }
            }

            if (foundLine)
            {
                sb.append("</context>");
            }

            return sb.toString();
        }

        /**
         * Get a placeholder segment when a context happens to be greater than
         * the limited size in db (VARCHAR 4000).
         * 
         * @return A dummy segment instead of having a null value.
         */
        String getPlaceholderSegment()
        {
            return "<segment>--</segment>";
        }
    }

    public void setM_isInstalled(boolean installed)
    {
        m_isInstalled = installed;
    }
}

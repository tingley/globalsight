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

// java
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.corpus.CorpusManagerLocal;
import com.globalsight.everest.corpus.CorpusManager;
import com.globalsight.ling.tm2.corpusinterface.TuvMapping;
import com.globalsight.util.GlobalSightLocale;
import java.io.File;


/**
 * Provides methods for accessing the corpus TM.
 */
public class CorpusManagerWLRMIImpl extends RemoteServer implements CorpusManagerWLRemote
{
    private CorpusManager m_localReference = null;

    public CorpusManagerWLRMIImpl() throws RemoteException
    {
        super(CorpusManager.SERVICE_NAME);
        m_localReference = new CorpusManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    /**
     * Looks up the corpus doc (corpus_unit_variant) with
     * the given ID.
     * 
     * @param p_id     cuv_id
     * @return CorpusDoc
     */
    public CorpusDoc getCorpusDoc(Long p_id) throws RemoteException, CorpusException
    {
        return m_localReference.getCorpusDoc(p_id);
    }

    /**
     * Looks up the corpus doc (corpus_unit_variant) with
     * the given name. Returns a collection of corpus docs with
     * names that match.
     * 
     * @param p_id     cuv_id
     * @return CorpusDoc
     */
    public Collection getCorpusDoc(String p_name) throws RemoteException, CorpusException
    {
        return m_localReference.getCorpusDoc(p_name);
    }


    /**
     * Adds a row to the the corpus_unit and corpus_unit_variant
     * tables. Sets the id and cu_id for the returned CorpusDoc.
     * 
     * @param p_locale
     * @param p_name
     * @param p_gxmlContent
     * @param p_binaryContent
     * @param p_deleteBinary
     * @return A src corpus doc that has been added to the corpus
     * @exception RemoteException
     * @exception CorpusException
     */
    public CorpusDoc addNewSourceLanguageCorpusDoc(SourcePage p_sourcePage,
                                                   String p_gxmlContent, File p_binaryContent,
                                                   boolean p_deleteBinary)
    throws RemoteException, CorpusException
    {
        return m_localReference.addNewSourceLanguageCorpusDoc(p_sourcePage, p_gxmlContent, p_binaryContent,p_deleteBinary);
    }

    /**
     * Adds a row to the the corpus_unit and corpus_unit_variant
     * tables. Sets the id and cu_id for the returned CorpusDoc.
     * Aligner Import calls this method
     * 
     * @param p_locale
     * @param p_name
     * @param p_gxmlContent
     * @param p_binaryContent
     * @param p_deleteBinary -- true if the binary file should be deleted after save
     * @return A src corpus doc that has been added to the corpus
     * @exception RemoteException
     * @exception CorpusException
     */
    public CorpusDoc addNewSourceLanguageCorpusDoc(
        GlobalSightLocale p_locale,
        String p_pageName,
        String p_gxmlContent,
        File p_binaryContent,
        boolean p_canStoreNativeFormatDosc)
    throws RemoteException, CorpusException
    {
        return m_localReference.addNewSourceLanguageCorpusDoc(
            p_locale, p_pageName, p_gxmlContent, p_binaryContent,
            p_canStoreNativeFormatDosc);
    }

    /**
     * Adds a row to the corpus_unit_variant table so
     * that the new row for the target-language doc
     * is related to the same corpus_unit entry as
     * the src document.
     * Sets the ids and name in the target doc and returns it.
     * 
     * @param p_src    src corpus doc
     * @param p_tgt    target corpus doc
     * @return modified target corpus doc
     */
    public CorpusDoc addNewTargetLanguageCorpusDoc(
        CorpusDoc p_src, GlobalSightLocale p_targetLocale, 
        String p_gxml, File p_binaryData,
        boolean p_canStoreNativeFormatDosc)
    throws RemoteException, CorpusException

    {
        return m_localReference.addNewTargetLanguageCorpusDoc(
            p_src, p_targetLocale, p_gxml, p_binaryData, p_canStoreNativeFormatDosc);
    }

    /**
     * Maps the segments (project_tm_tuv_t) to the specified
     * corpus doc by making new entries in the corpus_map table
     * 
     * @param p_segments list of project_tm_tuv_t ids
     * @param p_doc      corpus doc to map
     * @exception RemoteException
     * @exception CorpusException
     */
    public void mapSegmentsToCorpusDoc(List<TuvMapping> p_segments, CorpusDoc p_doc) throws RemoteException, CorpusException
    {
        m_localReference.mapSegmentsToCorpusDoc(p_segments, p_doc);
    }

    /**
     * Returns an array list of CorpusContextHolder objects that
     * correspond to map entries mapping the tuv in the targetLocale
     * to target language documents and source language documents in
     * the corpus.
     * 
     * @param p_projectTuvId
     *               the project_tm_tuv_t id
     * @param p_sourceLocale
     *               source locale db id
     * @return ArrayList of CorpusContextHolder
     * @exception RemoteException
     * @exception CorpusException
     */
    public ArrayList getCorpusContextsForSegment(long p_projectTuvId, long p_sourceLocale)
    throws RemoteException, CorpusException
    {
        return m_localReference.getCorpusContextsForSegment(p_projectTuvId, p_sourceLocale);
    }

    /**
     * Cleans the corpus of the source corpus doc and any associated mappings.
     * It also removes the association between the source page and the source corpus doc
     * 
     * @param p_sourcePage
     *               source page
     */
    public void removeSourceCorpusDoc(SourcePage p_sourcePage)
    throws RemoteException, CorpusException
    {
        m_localReference.removeSourceCorpusDoc(p_sourcePage);
    }

    /**
    * Removes the named corpus doc. If the number of docs in the group is 2, then both docs
    * and the group are removed as well. This also unlinks the pages and removes all corpus
    * context mappings.
    */
    public void removeCorpusDoc(Long p_corpusDocId) throws RemoteException,CorpusException
    {
        m_localReference.removeCorpusDoc(p_corpusDocId);
    }


    /**
     * Makes all source pages in the job use the same native
     * format document, and deletes whatever they're currently
     * using. The import process could not make them share because
     * of threading issues; therefore this cleanup is necessary.
     * 
     * If the job is not an ms office job, then nothing happens
     * 
     * @param p_jobId  job id
     */
    public void cleanUpMsOfficeJobSourcePages(long p_jobId) throws RemoteException, CorpusException
    {
        m_localReference.cleanUpMsOfficeJobSourcePages(p_jobId);
    }

    /**
     * Makes all target pages in the job use the same native
     * format document.
     * If the job is not an ms office job, then nothing happens
     * 
     * @param p_targetPageId Target Page Id
     */
    public void cleanUpMsOfficeJobTargetPages(long p_targetPageId) throws RemoteException, CorpusException
    {
        m_localReference.cleanUpMsOfficeJobTargetPages(p_targetPageId);
    }
}


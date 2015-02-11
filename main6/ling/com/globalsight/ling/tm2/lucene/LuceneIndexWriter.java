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
package com.globalsight.ling.tm2.lucene;

import org.apache.log4j.Logger;

import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.population.SegmentsForSave.AddTuv;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSTuvData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.analysis.Analyzer;


/**
 * LuceneIndexWriter is responsible for indexing TM segments.
 */

public class LuceneIndexWriter
{
    private static final Logger c_logger =
        Logger.getLogger(
            LuceneIndexWriter.class);

    static protected String LOCK_NAME = "gs.lock";
    
    static
    {
        // set lock timeout to 3 minutes
        //IndexWriter.WRITE_LOCK_TIMEOUT = 180000L;
        //IndexWriter.COMMIT_LOCK_TIMEOUT = 180000L;
    }
    

    private long m_tmId;
    private Analyzer m_analyzer;

    private Lock m_lock = null;
    private Directory m_directory;
    private File m_indexDir;
    private boolean m_isFirst = false;
    
    
    public LuceneIndexWriter(long p_tmId, GlobalSightLocale p_locale)
            throws Exception
    {
        this(p_tmId, p_locale, false);
    }
    
    /**
     * The constructor gets a lock on the index directory.  If the
     * directory doesn't exist yet, it is created. When an operation
     * is done, close() method must be called to remove the lock.
     * 
     * @param p_tmId TM id
     * @param p_locale locale of the index
     */
    public LuceneIndexWriter(long p_tmId, GlobalSightLocale p_locale,
            boolean p_isFirst) throws Exception
    {
        m_tmId = p_tmId;
        m_analyzer = new GsPerFieldAnalyzer(p_locale);
        m_isFirst = p_isFirst;
        
        m_indexDir
            = LuceneUtil.getGoldTmIndexDirectory(p_tmId, p_locale, true);

        // get the directory. Note that the directory cannot be
        // created before getting a lock. Note2:
        // FSDirectory.getDirectory(dir, true) doesn't really create
        // index files. It just clear out the old index files and lock
        // file.
        m_directory = FSDirectory.open(m_indexDir);

        // get a lock on the directory
        m_lock = m_directory.makeLock(LOCK_NAME);
        if (!m_lock.obtain(180000L))
        {
            m_lock = null;
            throw new IOException("Index locked for write: " + m_lock);
        }

        // only after gettting a lock, create the initial index files
        // if it doesn't exist.
        if(!DirectoryReader.indexExists(m_directory))
        {
            IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                    m_analyzer);
            conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
            boolean initSuccess = false;
            IndexWriter writer = null;
            try
            {
                writer = new IndexWriter(m_directory, conf);
                initSuccess = true;
            }
            catch (IndexFormatTooOldException ie)
            {
                // delete too old index
                File[] files = m_indexDir.listFiles();
                if (files != null && files.length > 0)
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        File oneFile = files[i];
                        if (!LuceneIndexWriter.LOCK_NAME.equals(oneFile
                                .getName()))
                        {
                            oneFile.delete();
                        }
                    }
                }

                writer = new IndexWriter(m_directory, conf);
                initSuccess = true;
            }
            finally
            {
                if (!initSuccess)
                {
                    m_lock.release();
                }
                IOUtils.closeWhileHandlingException(writer);
            }
        }
    }
    

    /**
     * Removes all index for a Tm. This method removes all index files
     * and directories that belong to the specified Tm. This method
     * tries to remove Tm in a destructive fashion, meaning that, even
     * if the locks on the indexes have been obtained by other
     * process/thread, this method goes ahead and delete indexes. The
     * other process/thread with locks will get exceptions.
     *
     * @param p_tmId Tm id
     */
    static public void removeTm(long p_tmId)
        throws Exception
    {
        File tmIndexRoot
            = LuceneUtil.getGoldTmIndexParentDir(p_tmId);
        
        String[] localeNames = tmIndexRoot.list();

        // localeNames can be null if no index has been created.
        if(localeNames != null)
        {
            for(int i = 0; i < localeNames.length; i++)
            {
                File directory = new File(tmIndexRoot, localeNames[i]);

                try
                {
                    if (!directory.isDirectory())
                    {
                        FileUtil.deleteFile(directory);
                        continue;
                    }
                    
                    // delete all index files and Lucene lock files
                    FSDirectory fsDirectory
                        = FSDirectory.open(directory);

                    // delete our lock file
                    Lock lock = fsDirectory.makeLock(LOCK_NAME);
                    lock.release();

                    fsDirectory.close();
                    
                    // delete index directory 
                    FileUtil.deleteFile(directory);
                    
                    // clean cache if have
                    LuceneCache.cleanLuceneCache(directory);
                }
                catch(Exception e)
                {
                    c_logger.error("Exception is thrown while removing a Tm index. Ignoring...", e);
                }
            }
        }

        // delete index root directory
        tmIndexRoot.delete();
    }
    
    /**
     * Indexes segments. To maintain index integrity, indexes are at
     * first created in memory and merged into a file system index.
     *
     * @param p_tuvs List of BaseTmTuv, SegmentsForSave.AddTuv, or TM3Tuv
     * @param p_sourceLocale true if p_tuvs are source locale segments
     * @param p_indexTargetLocales true for TM3, false for TM2
     */
    public void index(List p_tuvs, boolean p_sourceLocale,
            boolean p_indexTargetLocales)
        throws Exception
    {
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                m_analyzer);
        conf.setOpenMode(m_isFirst ? OpenMode.CREATE
                : OpenMode.CREATE_OR_APPEND);
        IndexWriter fsIndexWriter = new IndexWriter(m_directory, conf);

        try
        {
            for (Iterator it = p_tuvs.iterator(); it.hasNext();)
            {
                Object tuv = it.next();

                Document doc = tuv instanceof BaseTmTuv ? createDocumentFromBaseTmTuv(
                        (BaseTmTuv) tuv, p_sourceLocale, p_indexTargetLocales)
                        : tuv instanceof AddTuv ? createDocumentFromAddTuv(
                                (AddTuv) tuv, p_sourceLocale,
                                p_indexTargetLocales)
                                : tuv instanceof TM3Tuv ? createDocumentFromTM3Tuv(
                                        (TM3Tuv<GSTuvData>) tuv,
                                        p_sourceLocale, p_indexTargetLocales)
                                        : null;

                fsIndexWriter.addDocument(doc);
            }
        }
        finally
        {
            fsIndexWriter.close();
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_indexDir);
    }

    /*
     * Removes indexes for the specified segments
     *
     * @param p_tuvs List of BaseTmTuv or TM3Tuv
     */
    public void remove(Collection p_tuvs)
        throws Exception
    {
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                m_analyzer);
        conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(m_directory, conf);
        
        try
        {
            for(Iterator it = p_tuvs.iterator(); it.hasNext();)
            {
                Object tuv = it.next();
                Long id =
                    tuv instanceof BaseTmTuv ? ((BaseTmTuv) tuv).getId() :
                    tuv instanceof TM3Tuv    ? ((TM3Tuv) tuv).getId()
                                             : null;
                
                Term term = new Term(TuvDocument.TUV_ID_FIELD, id.toString());
                writer.deleteDocuments(term);
            }
        }
        catch(Throwable e)
        {
            c_logger.error(e.getMessage(), e);
            //indexReader.undeleteAll();
            throw (e instanceof Exception ? (Exception)e : new Exception(e));
        }
        finally
        {
            writer.commit();
            writer.close();
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_indexDir);
    }
    
    public void close()
        throws Exception
    {
        m_lock.release();
        m_lock = null;
        
        m_directory.close();
    }


    /** Release the write lock, if needed. */
    protected void finalize()
        throws IOException
    {
        if (m_lock != null)
        {
            m_lock.release();
        }
        m_lock = null;
    }


    private Document createDocumentFromBaseTmTuv(
        BaseTmTuv p_tuv, boolean p_sourceLocale, boolean p_indexTargetLocales)
        throws Exception
    {
        Set<String> targetLocales = new HashSet<String>();
        if (p_indexTargetLocales) {
            for (BaseTmTuv tuv: p_tuv.getTu().getTuvs()) {
                if (tuv.equals(p_tuv))
                {
                    continue;
                }
                targetLocales.add(tuv.getLocale().toString());
            }
        }
        TuvDocument tuvDoc = new TuvDocument(
            p_tuv.getFuzzyIndexFormat(), p_tuv.getId(), p_tuv.getTu().getId(),
            m_tmId, p_sourceLocale, targetLocales, m_analyzer);
        return tuvDoc.getDocument();
    }
    
    private Document createDocumentFromAddTuv(
        AddTuv p_tuv, boolean p_sourceLocale, boolean p_indexTargetLocales)
        throws Exception
    {
        if (p_indexTargetLocales) {
            throw new IllegalArgumentException(
                "should never index target locales for TM3");
        }
        TuvDocument tuvDoc = new TuvDocument(
            p_tuv.getTuv().getFuzzyIndexFormat(), p_tuv.getNewTuvId(),
            p_tuv.getTuIdToAdd(), m_tmId, p_sourceLocale, null, m_analyzer);
        return tuvDoc.getDocument();
    }
    
    private Document createDocumentFromTM3Tuv(
        TM3Tuv<GSTuvData> p_tuv, boolean p_sourceLocale, boolean p_indexTargetLocales)
        throws Exception
    {
        if (! p_indexTargetLocales) {
            throw new IllegalArgumentException(
                "should always index target locales for TM3");
        }
        Set<String> targetLocales = new HashSet<String>();
        for (TM3Tuv<GSTuvData> tuv: p_tuv.getTu().getAllTuv())
        {
            if (tuv.equals(p_tuv))
            {
                continue;
            }
            targetLocales.add(tuv.getLocale().toString());
        }
        TuvDocument tuvDoc = new TuvDocument(
            p_tuv.getContent().normalizeTuvData(), p_tuv.getId(), p_tuv.getTu().getId(),
            m_tmId, p_sourceLocale, targetLocales, m_analyzer);
        return tuvDoc.getDocument();
    }
    
}

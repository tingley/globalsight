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

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.everest.util.system.SystemConfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.analysis.Analyzer;


/**
 * LuceneReindexer recreates the index of specified TM
 */

public class LuceneReindexer
{
    private static final Logger c_logger =
        Logger.getLogger(
            LuceneReindexer.class);

    static
    {
        // set lock timeout to 3 minutes
        //IndexWriter.WRITE_LOCK_TIMEOUT = 180000L;
        //IndexWriter.COMMIT_LOCK_TIMEOUT = 180000L;
    }
    

    private static int c_minMergeDocs;
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            c_minMergeDocs
                = sc.getIntParameter("tm.indexer.minMergeDocs");
        }
        catch(Exception e)
        {
            c_logger.warn(e.getMessage(), e);
            
            c_minMergeDocs = 10000; // default 10000 docs
        }
    }

    private long m_tmId;
    private Analyzer m_analyzer;

    private Lock m_lock = null;
    private IndexWriter m_indexWriter;
    private File m_indexDir;
    private GlobalSightLocale m_locale;
    
    
    /**
     * The constructor gets a lock on the index directory and discard
     * existing index, if any. When an operation is done, close()
     * method must be called to remove the lock.
     * 
     * @param p_tmId TM id
     * @param p_locale locale of the index
     */
    public LuceneReindexer(long p_tmId, GlobalSightLocale p_locale)
        throws Exception
    {
        m_locale = p_locale;
        m_tmId = p_tmId;
        m_analyzer = new GsAnalyzer(p_locale);
        
        m_indexDir
            = LuceneUtil.getGoldTmIndexDirectory(p_tmId, p_locale, true);

        // get the directory
        FSDirectory directory = FSDirectory.open(m_indexDir);
        
        // get a lock on the directory
        m_lock = directory.makeLock(LuceneIndexWriter.LOCK_NAME);
        if (!m_lock.obtain(180000L))
        {
            m_lock = null;
            throw new IOException("Index locked for write: " + m_lock);
        }

        // get an IndexWriter on the directory, recreating a new index
        // repository
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                m_analyzer);
        conf.setOpenMode(OpenMode.CREATE);
        boolean initSuccess = false;
        try
        {
            m_indexWriter = new IndexWriter(directory, conf);
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
                    if (!LuceneIndexWriter.LOCK_NAME.equals(oneFile.getName()))
                    {
                        oneFile.delete();
                    }
                }
            }

            m_indexWriter = new IndexWriter(directory, conf);
            initSuccess = true;
        }
        finally
        {
            if (!initSuccess)
            {
                m_lock.release();
            }
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_indexDir);
    }
    

    /**
     * Indexes a segment. 
     *
     * @param p_tuv BaseTmTuv to be indexed
     * @param p_sourceLocale true if p_tuv is source locale segment
     */
    public void index(SegmentTmTuv p_tuv)
        throws Exception
    {
        Document doc = createDocumentFromBaseTmTuv(p_tuv);
        m_indexWriter.addDocument(doc);
    }
    

    /**
     * optimize the index and release the lock
     */
    public void close()
        throws Exception
    {
        try
        {
            //m_indexWriter.optimize();
            m_indexWriter.close();
        }
        finally
        {
            m_lock.release();
            m_lock = null;
        }
    }


    public GlobalSightLocale getLocale()
    {
        return m_locale;
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


    private Document createDocumentFromBaseTmTuv(SegmentTmTuv p_tuv)
        throws Exception
    {
        TuvDocument tuvDoc = new TuvDocument(
            p_tuv.getFuzzyIndexFormat(), p_tuv.getId(), p_tuv.getTu().getId(),
            m_tmId, p_tuv.isSourceTuv(), null, m_analyzer);
        return tuvDoc.getDocument();
    }
}

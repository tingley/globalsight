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

package com.globalsight.ling.lucene;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.ling.lucene.analysis.AnalyzerFactory;
import com.globalsight.ling.lucene.highlight.Highlighter;
import com.globalsight.ling.lucene.highlight.QueryScorer;
import com.globalsight.ling.lucene.highlight.SimpleFormatter;
import com.globalsight.ling.lucene.locks.WriterPreferenceReadWriteLock;
import com.globalsight.ling.lucene.search.DictionarySimilarity;
import com.globalsight.ling.tm2.lucene.LuceneCache;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;
import com.globalsight.ling.tm2.lucene.LuceneUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * <p>Wrapper for a Lucene index which indexes terms, segment and text
 * with a binary, bit-compressed and highly efficient posting-vector.</p>
 *
 * <p>Lucene indexes support language-dependent stemming and
 * tokenization, and also n-gram formation (at the cost of larger
 * indexes of course).</p>
 *
 * <p>This class can store the originally indexed texts for returning
 * them with the results. The drawback of larger storage space
 * requirements is offset by the convenience of the UI layers
 * receiving the originally indexed texts for immediate display.
 * In any case, the real data objects are stored in the Termbases'
 * tb_concept, tb_language, tb_term tables, or the TM's
 * translation_unit and translation_unit_variant tables.</p>
 *
 * @see http://jakarta.apache.org/lucene/docs/index.html
 */
abstract public class Index
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            Index.class);

    /** Creates an index for term-like data (short data). */
    static public final int TYPE_TERM = 1;
    /** Creates an index for text-like data (long data). */
    static public final int TYPE_TEXT = 2;

    /** Token creation extracts lower-case words. */
    static public final int TOKENIZE_NONE = AnalyzerFactory.TOKENIZE_NONE;
    /** Token creation extracts stemmed words. */
    static public final int TOKENIZE_STEM = AnalyzerFactory.TOKENIZE_STEM;
    /** Token creation extracts lower-cased trigrams. */
    static public final int TOKENIZE_3GRAM = AnalyzerFactory.TOKENIZE_3GRAM;

    static public final String CATEGORY_TB = "TB";
    static public final String CATEGORY_TM = "TM";

    //
    // Private Static Members
    //

    static private final Integer STATE_CLOSED = new Integer(1);
    static private final Integer STATE_OPENING = new Integer(2);
    static private final Integer STATE_OPENED = new Integer(3);
    static private final Integer STATE_CREATING = new Integer(4);
    static private final Integer STATE_CLOSING = new Integer(5);

    /**
     * The similarity measure for dictionary-type data. It ignores
     * document frequencies.
     */
    static private final Similarity s_dictionarySimilarity =
        new DefaultSimilarity();

//    static private final String INDEX_FILE_DIR = "GlobalSight/Indexes";
//    static private final String s_baseDirectory = getBaseDirectory();

//    static private String getBaseDirectory()
//    {
//        File result;
//
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//            String fileStorageDir = sc.getStringParameter(
//                SystemConfiguration.FILE_STORAGE_DIR);
//
//            result = new File(fileStorageDir, INDEX_FILE_DIR);
//            result.mkdirs();
//        }
//        catch (Exception ex)
//        {
//            throw new RuntimeException(ex);
//        }
//
//        return result.getAbsolutePath();
//    }

    //
    // Members
    //
    private String m_category;
    private String m_dbname;
    private String m_name;
    private String m_locale;
    private int m_type;
    protected int m_tokenize;

    private String m_directory;
    private SimpleFSDirectory m_fsDir;

    private Similarity m_similarity;
    protected Analyzer m_analyzer;

    private IndexWriter m_ramIndexWriter;
    private RAMDirectory m_ramdir;

    private Integer m_state = STATE_CLOSED;
    private WriterPreferenceReadWriteLock m_lock =
        new WriterPreferenceReadWriteLock();
    
    private boolean noResult = true;

    //
    // Constructor
    //

    /**
     * An index is identified on disk by a directory name, which must
     * be unique per category (TM, TB), database name, and language
     * name.
     * @throws IOException 
     */
    protected Index(String p_category, String p_dbname, String p_name,
        String p_locale, int p_type, int p_tokenize) throws IOException
    {
        m_category = p_category;
        m_dbname = p_dbname;
        m_name = p_name;
        m_locale = p_locale;
        m_type = p_type;
        m_tokenize = p_tokenize;

        m_directory = getDirectoryName(m_dbname, m_name);

        if (m_type == TYPE_TERM)
        {
            m_similarity = s_dictionarySimilarity;
        }
        else
        {
            m_similarity = new DefaultSimilarity();
        }

        m_analyzer = AnalyzerFactory.getInstance(m_locale, m_tokenize);
        
        try
        {
            File path = new File(m_directory);
            if (!path.exists())
            {
                path.mkdirs();
            }
            m_fsDir = new SimpleFSDirectory(path);
        }
        catch (IOException ex)
        {
            CATEGORY.error("unexpected error when persisting index "
                    + m_directory, ex);

            throw ex;
        }
    }

    //
    // Public Methods
    //

    public String getName()
    {
        return m_name;
    }

    public String getLocale()
    {
        return m_locale;
    }

    public Integer getState()
    {
        return m_state;
    }

    public String getDirectory()
    {
        return m_directory;
    }

    public boolean exists() throws IOException
    {
        return DirectoryReader.indexExists(m_fsDir);
    }

    /** Opens or creates this index. */
    public void open()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_CLOSED)
            {
                throw new IOException("index is open");
            }

            m_state = STATE_OPENING;
        }

        try
        {
            if (!DirectoryReader.indexExists(m_fsDir))
            {
                // create empty index, close it.
                IndexWriter tempWriter = getIndexWriter(true);
                tempWriter.close();
                tempWriter = null;
            }
        }
        finally
        {
            m_state = STATE_OPENED;
        }
    }

    /**
     * Re-creates the index by batch-loading entries into it.
     * The index must be closed before calling this method.
     * Caller must use
     * <PRE>
     *   close();
     *   try
     *   {
     *     batchOpen();
     *     ...
     *     batchAddDocument();
     *     ...
     *   }
     *   finally
     *   {
     *     batchDone();
     *   }
     */
    public void batchOpen()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_CLOSED)
            {
                throw new IOException("index is open");
            }

            m_state = STATE_CREATING;
        }

        // setup RAMDirectory and writer
        m_ramdir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(LuceneUtil.VERSION, m_analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        //config.setSimilarity(m_similarity);
        
        m_ramIndexWriter = new IndexWriter(m_ramdir, config);
        //m_ramIndexWriter.mergeFactor = 10000;
    }

    /**
     * Ends the batch re-creation of an index by clearing out the old
     * index files, writing the new in-memory index to disk, and
     * setting the index state to STATE_OPENED.
     *
     * @see #batchOpen()
     */
    public void batchDone()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_CREATING)
            {
                throw new IOException("index is not being re-created");
            }
        }
        // try to unlock this dir : for unexpected shutdown
        try
        {
            if (IndexWriter.isLocked(m_fsDir))
            {
                IndexWriter.unlock(m_fsDir);
            }
        }
        catch (Exception ee)
        {
            // ignore
        }

        // Tho reports it can happen that the index cannot be created
        // on disk (GSDEF00012703). Trap this and release the memory
        // of the ram directory.
        IndexWriter diskwriter = null;
        try
        {
            // MUST optimize RAMDirectory before writing it to disk.
            // m_ramIndexWriter.optimize();

            // Write all data out to disk, optimize and clean up.

            diskwriter = getIndexWriter(true);
            diskwriter.commit();
            Directory[] ds = new Directory[] { m_ramdir } ;
            //Directory[] ds = new Directory[] { m_fsDir } ;
            diskwriter.addIndexes(ds);
            //diskwriter.optimize();
            //diskwriter.close();
            
            // clean cache if have
            LuceneCache.cleanLuceneCache(m_directory);
        }
        catch (IOException ex)
        {
            CATEGORY.error("unexpected error when persisting index " +
                m_directory, ex);

            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("unexpected error when persisting index " +
                m_directory, ex);

            throw new IOException(ex.getMessage());
        }
        finally
        {
            IOUtils.closeWhileHandlingException(diskwriter, m_ramIndexWriter, m_ramdir);

            m_ramIndexWriter = null;
            m_ramdir = null;
            m_state = STATE_OPENED;
        }
    }

    /** Closes this index. */
    public void close()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }

            m_state = STATE_CLOSING;
        }

        try
        {
            // Other readers will finish and release this object.
            // We wait for this to happen by acquiring the lock.
            m_lock.writeLock().acquire();
            m_lock.writeLock().release();

            m_state = STATE_CLOSED;
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    /** Deletes this index. */
    public void drop()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                if (m_state == STATE_CREATING)
                {
                    throw new IOException("index is being created");
                }
                else
                {
                    throw new IOException("index is not available");
                }
            }

            // disable new readers & writers
            m_state = STATE_CLOSING;
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_directory);

        try
        {
            // Other readers will finish and release this object.
            // We wait for this to happen by acquiring the lock.
            m_lock.writeLock().acquire();

            try
            {
                // This deletes all files in the directory.
                m_fsDir.close();
                new File(m_directory).delete();
            }
            finally
            {
                m_lock.writeLock().release();
                m_state = STATE_CLOSED;
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    /** Renames this index by renaming the storage directory. */
    public void rename(String p_newName)
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }

            // disable new readers & writers
            m_state = STATE_CREATING;
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_directory);

        try
        {
            // Other readers will finish and release this object.
            // We wait for this to happen by acquiring the lock.
            m_lock.writeLock().acquire();

            try
            {
                String oldDirectory = getDirectory();
                String newDirectory = getDirectoryName(m_dbname, p_newName);

                new File(oldDirectory).renameTo(new File(newDirectory));

                // Set members after call to rename.
                m_name = p_newName;
                m_directory = newDirectory;
            }
            finally
            {
                m_lock.writeLock().release();
                m_state = STATE_OPENED;
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    /** Optimizes this index, merging all recent changes into a single file. */
    public void optimize()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }

            // disable new readers & writers
            m_state = STATE_CREATING;
        }

        try
        {
            // Other readers will finish and release this object.
            // We wait for this to happen by acquiring the lock.
            m_lock.writeLock().acquire();

            try
            {
                // allocate writer and optimize index
                IndexWriter tempWriter = getIndexWriter(false);
                // m_ramIndexWriter.optimize();
                tempWriter.close();
                tempWriter = null;
            }
            finally
            {
                m_lock.writeLock().release();
                m_state = STATE_OPENED;
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    public void batchAddDocument(long p_mainId, long p_subId, String p_text)
        throws IOException
    {
        if (m_state != STATE_CREATING)
        {
            throw new IOException("index is not being re-created");
        }

        // we're the only writer (this thread called batchCreate())
        Document doc = getDocument(p_mainId, p_subId, p_text);
        m_ramIndexWriter.addDocument(doc);
        m_ramIndexWriter.commit();
    }

    public void addDocument(long p_mainId, long p_subId, String p_text)
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_directory);

        try
        {
            m_lock.writeLock().acquire();

            IndexWriter tempWriter = null;
            try
            {
                tempWriter = getIndexWriter(false);
                Document doc = getDocument(p_mainId, p_subId, p_text);
                tempWriter.addDocument(doc);
            }
            finally
            {
                m_lock.writeLock().release();
                IOUtils.closeWhileHandlingException(tempWriter);
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    public void deleteDocument(long p_mainId, long p_subId)
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }
        }
        
        // clean cache if have
        LuceneCache.cleanLuceneCache(m_directory);

        try
        {
            m_lock.writeLock().acquire();

            try
            {
                IndexWriter w = getIndexWriter(true);
//                IndexReader reader = DirectoryReader.open(m_fsDir);
//                reader.delete(new Term(
//                    IndexDocument.SUBID, String.valueOf(p_subId)));
                w.deleteDocuments(new Term(IndexDocument.SUBID, String
                        .valueOf(p_subId)));

                w.close();
            }
            finally
            {
                m_lock.writeLock().release();
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Executes a search in the index returning no more than p_maxHits
     * (suggested: 5-10), and having no score smaller than p_minScore.
     *
     * This implementation is based on Lucene and Lucene score values
     * float widely, making it hard to specify a useful cut-off like
     * 0.7 or 0.5. Good scores can be < 0.2. All that is guaranteed is
     * that scores are numerically ordered. Use p_maxHits instead.
     */
    public Hits search(String p_text, int end, int begin,float p_minScore)
        throws IOException, InterruptedException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }
        }

        try
        {
            m_lock.readLock().acquire();

            try
            {
                // Search the current index.
                //IndexReader reader = DirectoryReader.open(m_fsDir);
                //IndexSearcher searcher = new IndexSearcher(reader);
                IndexSearcher searcher = LuceneCache
                        .getLuceneCache(m_directory).getIndexSearcher();
                
                Query query = getQuery(p_text);
                
                int maxHits = end - begin;
                TopDocs topDocs = searcher.search(query, maxHits);
                
                if(topDocs.totalHits > 0) {
                    noResult = false;
                }

                // Store results in our own object.
                Hits result = new Hits(searcher, topDocs.scoreDocs, end, begin, p_minScore, p_text);

                // Highlight query terms in long results.
                if (m_type == TYPE_TEXT)
                {
                    // Note: rewrite MultiTermQuery, RangeQuery or PrefixQuery.

                    // TODO: optimize object creation if it all works.
                    Highlighter highlighter = new Highlighter(
                        new SimpleFormatter(), new QueryScorer(query));
                    
                    int max = Math.min(end, topDocs.totalHits);
                    for (int i = begin; i < max ; i++)
                    {
                        Document doc =  searcher.doc(topDocs.scoreDocs[i].doc);
                        String text = doc.get(IndexDocument.TEXT);

                        TokenStream tokenStream = m_analyzer.tokenStream(
                            IndexDocument.TEXT, new StringReader(text));
                        tokenStream.reset();

                        // Get 3 best fragments and separate with "..."
                        String hilite = highlighter.getBestFragments(
                            tokenStream, text, 3, "...");

                        result.getHit(i).setText(hilite);
                    }
                }

                //searcher.close();
                // reader.close();

                return result;
            }
            finally
            {
                m_lock.readLock().release();
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }
    
    public boolean isNullOfSearch() {
        return noResult;
    }
    
    public Hits search(String p_text, int p_maxHits, 
            float p_minScore) throws IOException, InterruptedException
    {
        return search(p_text, p_maxHits, 0, p_minScore);
    }

    /**
     * Returns the number of documents stored in this index.
     */
    public int getDocumentCount()
        throws IOException
    {
        synchronized (m_state)
        {
            if (m_state != STATE_OPENED)
            {
                throw new IOException("index is not available");
            }
        }

        try
        {
            m_lock.readLock().acquire();

            try
            {
                IndexReader reader = LuceneCache.getLuceneCache(m_directory)
                        .getIndexReader();
                int result = reader.numDocs();
                return result;
            }
            finally
            {
                m_lock.readLock().release();
            }
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    //
    // Private And Protected Methods
    //

    /**
     * Allocates a Lucene IndexWriter object. The IndexWriter must be
     * closed by the caller. If p_create is true, the existing index
     * is cleared on disk.
     */
    private IndexWriter getIndexWriter(boolean p_create)
        throws IOException
    {
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                m_analyzer);
        OpenMode om = p_create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND;
        conf.setOpenMode(om);

        IndexWriter result = null;
        boolean deleteFiles = false;
        try
        {
            result = new IndexWriter(m_fsDir, conf);
        }
        catch (EOFException eofe)
        {
            deleteFiles = true;
        }
        catch (IndexFormatTooOldException ie)
        {
            deleteFiles = true;
        }

        if (deleteFiles)
        {
            File indexDir = new File(m_directory);
            if (!indexDir.exists())
            {
                indexDir.mkdirs();
            }
            // delete too old index
            File[] files = indexDir.listFiles();
            if (files != null && files.length > 0)
            {
                for (int i = 0; i < files.length; i++)
                {
                    File oneFile = files[i];
                    try
                    {
                        oneFile.delete();
                    }
                    catch (Exception eee)
                    {
                        // ignore
                    }
                }
            }

            result = new IndexWriter(m_fsDir, conf);
        }

        return result;
    }

    private String getDirectoryName(String p_dbname, String p_name)
    {
        StringBuffer result = new StringBuffer();

        String fileStorageDir = null;
        if (AmbFileStoragePathUtils.getFileStorageDir() != null){
            fileStorageDir = AmbFileStoragePathUtils.getFileStorageDir().toString();
        } else {
            // When startup server, above invoking can't get file storage,have
            // to get that manually (GBS-2641).
            fileStorageDir = getFileStorageDirForLoggingUser();
        }

        result.append(fileStorageDir)
              .append(File.separatorChar)
              .append(m_category).append('-').append(p_dbname)
              .append(File.separatorChar)
              .append(p_name).append('-').append(m_locale);

        if (m_type == TYPE_TERM)
        {
            result.append("-TERM");
        }
        else
        {
            result.append("-TEXT");
        }

        result.append(File.separatorChar);

        return result.toString();
    }
    
    private String getFileStorageDirForLoggingUser()
    {
        String fileStorageDir = null;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DbUtil.getConnection();
            String companyId = CompanyThreadLocal.getInstance().getValue();
            String sql = "select value from system_parameter "
                    + "where name = ? "
                    + "and company_id = ? ";
            ps = connection.prepareStatement(sql);
            ps.setString(1, SystemConfigParamNames.FILE_STORAGE_DIR);
            ps.setLong(2, Long.parseLong(companyId));
            rs = ps.executeQuery();
            if (rs.next()){
                fileStorageDir = rs.getString(1);
            }
            if (fileStorageDir != null){
                File file = new File(fileStorageDir);
                file.mkdirs();
            }
        } catch (Exception ignore) {
            
        } finally {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return fileStorageDir;
    }

    /** Adds a new document to the Lucene index. */
    abstract protected Document getDocument(long p_mainId, long p_subId,
        String p_text)
        throws IOException;

    /** Constructs a query for the Lucene index. */
    abstract protected Query getQuery(String p_text)
        throws IOException;

    //
    // Test Code
    //

    static public void main(String[] args)
        throws Exception
    {
        Index index = new TbFuzzyIndex("test", "indextest", "en_US");

        index.open();
        index.close();

        index.batchOpen();
        index.batchAddDocument(1L, 1L, "abc def");
        index.batchAddDocument(2L, 2L, "def ghi");
        index.batchDone();

        System.out.println("Doc count after batch " + index.getDocumentCount());
        index.close();

        index.open();
        index.optimize();
        System.out.println("Doc count after optimize " + index.getDocumentCount());

        System.out.println("def");
        showHits(index.search("def", 5, 0.0f));
        System.out.println("ghi");
        showHits(index.search("ghi", 5, 0.0f));

        index.addDocument(3L, 3L, "abc ghi");
        System.out.println("Doc count after add " + index.getDocumentCount());

        System.out.println("ghi");
        showHits(index.search("ghi", 5, 0.0f));

        index.deleteDocument(2L, 2L);

        System.out.println("ghi");
        showHits(index.search("ghi", 5, 0.0f));

        index.addDocument(4L, 4L, "house");
        index.addDocument(5L, 5L, "bedroom");
        index.addDocument(6L, 6L, "bathroom");
        index.addDocument(7L, 7L, "fireplace");

        System.out.println("house");
        showHits(index.search("house", 10, 0.0f));
        System.out.println("The house has three bedrooms, one bathroom, a dining room, and fireplace.");
        showHits(index.search("The house has three bedrooms, one bathroom, a dining room, and fireplace.", 10, 0.0f));

        index.close();
    }

    static public void showHits(Hits p_hits)
    {
        for (int i = 0, max = p_hits.size(); i < max; i++)
        {
            long mainid = p_hits.getMainId(i);
            long subid = p_hits.getSubId(i);
            String text = p_hits.getText(i);
            float score = p_hits.getScore(i);

            System.out.println((i + 1) + ". mainid=" + mainid +
                " subid=" + subid + ": " + text + "(" + score + ")");
        }
    }
}

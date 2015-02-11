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

package com.globalsight.terminology.indexer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.ling.lucene.Index;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.util.ObjectPool;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.ClientInterruptException;
import com.globalsight.util.progress.IProcessStatusListener2;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>The RMI interface implementation for the Terminology Indexer.</p>
 *
 * <p>Re-indexing a termbase is implemented as a reader that reads
 * termbase data, and a index writer that updates the indexes.</p>
 *
 * <p>For concept-level fulltext index: the reader reads the TB_CONCEPT
 * table and sends CID and partial XML to the writer.</p>
 *
 * <p>For term-level fuzzy indexes: the reader reads the TB_TERM table
 * and sends CID, TID and TERM the writer.</p>
 *
 * <p>For term-level fulltext indexes: the reader reads the TB_TERM table
 * and sends CID, TID and partial XML to the writer.</p>
 */
public class IndexManager
    implements IIndexManager
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            IndexManager.class);

    static final private int BATCHSIZE = 100;

    //
    // Private Members
    //
    private Termbase m_termbase = null;
    private SessionInfo m_session;
    private Reader m_reader = null;
    private Writer m_writer = null;

    private Index m_conceptFulltextIndex;
    private ArrayList m_fulltextIndexes;
    private ArrayList m_fuzzyIndexes;

    private ObjectPool m_pool = new ObjectPool(IndexObject.class);

    // The indexer uses 2 progress bars.
    private IProcessStatusListener2 m_listener = null;

    // Thread instance counter.
    static private int s_counter = 0;

    //
    // Constructor
    //
    public IndexManager(Termbase p_database, SessionInfo p_session)
    {
        m_session = p_session;
        m_termbase = p_database;

        m_reader = createReader();
        m_writer = createWriter();
    }

    /**
     * Attaches an export event listener.
     */
    public void attachListener(IProcessStatusListener2 p_listener)
    {
        m_listener = p_listener;
    }

    /**
     * Detaches an export event listener.
     */
    public void detachListener(IProcessStatusListener2 p_listener)
    {
        m_listener = null;
    }

    public void doIndex()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                runIndexer();
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("INDEXER" + String.valueOf(s_counter++));
        t.start();
    }

    /**
     * Runs over all indexes and rebuilds them one by one. Updates the
     * 1st progress bar, handles cancel requests from the user.
     * Deregisters itself from the termbase when done.
     */
    private void runIndexer()
    {
        CATEGORY.info("Starting termbase indexing for `" +
            m_termbase.getName() + "'.");

        String desc = "";

        // Top level loop over all indexes
        int indexCount = prepareIndexInfo();

        // Count 1st step and last step separately.
        //int maxCount = indexCount;
        int counter = 1;

        try
        {
            /*
            if (m_conceptFulltextIndex != null)
            {
                desc = ProcessStatus.getStringFromResBundle(m_listener, "lb_term_desc_concept_full", "Concept-level full text");
                showStatus2(desc, counter++, maxCount, null);

                buildConceptFullTextIndex(m_conceptFulltextIndex);
            }
            */

            for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
            {
                Index index = (Index)m_fuzzyIndexes.get(i);

                desc = ProcessStatus.getStringFormattedFromResBundle(m_listener, "lb_term_desc_fuzzy_index_pattern", "Fuzzy index \u00AB{0}\u00BB", index.getName());
                showStatus2(desc, counter++, m_fuzzyIndexes.size(), null);

                buildFuzzyIndex(index);
            }

            /*
            for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
            {
                Index index = (Index)m_fulltextIndexes.get(i);

                desc = ProcessStatus.getStringFormattedFromResBundle(m_listener, "lb_term_desc_fulltext_index_pattern", "Full text index \u00AB{0}\u00BB", index.getName());
                showStatus2(desc, counter++, maxCount, null);

                buildFullTextIndex(index);
            }
            */

            //desc = ProcessStatus.getStringFromResBundle(m_listener, "lb_done", "Done");;
            //showStatus2(desc, counter, m_fuzzyIndexes.size(), null);
        }
        catch (ClientInterruptException e)
        {
            CATEGORY.info("client error: user cancelled the tb indexing!");
        }
        catch (IOException ex)
        {
            CATEGORY.error("indexing error", ex);

            try
            {
                showStatus2(desc, m_fuzzyIndexes.size(), m_fuzzyIndexes.size(), ProcessStatus.getStringFromResBundle(
                        m_listener, "lb_aborted", "Aborted")
                        + ": " + ex.getMessage());
            }
            catch (IOException ignore) {}
        }
        finally
        {
            // Set process status to really done regardless of percentages.
            try
            {
                setDone();
            }
            catch (Throwable ignore) {}

            // This indexer must deregister itself from the termbase.
            try
            {
                m_termbase.clearIndexer(this);
            }
            catch (Throwable ex)
            {
                CATEGORY.error("boo boo: the indexer was cleared twice", ex);
            }

            HibernateUtil.closeSession();
        }

        CATEGORY.info("Termbase indexing for `" + m_termbase.getName() +
            "' done.");
    }

    /**
     * Rebuilds the concept-level fulltext index.
     */
    private void buildConceptFullTextIndex(Index p_index)
        throws IOException
    {
        CATEGORY.info("Building concept-level index for termbase `" +
            m_termbase.getName() + "'.");

        ArrayList entries = new ArrayList(BATCHSIZE);
        ReaderResult result;

        int counter = 0, errCounter = 0;
        int expectedEntries = 0;

        try
        {
            speak(0, 0, null);

            expectedEntries = m_reader.getConceptCount();
            if (expectedEntries <= 0)
            {
                speak(0, 100, null);
                return;
            }

            showStatus(counter, expectedEntries, null);

            m_reader.startConceptXml();
            m_writer.startBatchIndexing(p_index);

            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == result.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;
                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    m_writer.indexXml(entries);

                    freeEntries(entries);

                    showStatus(counter, expectedEntries, null);
                }
            }

            // flush remaining entries
            if (entries.size() > 0)
            {
                m_writer.indexXml(entries);

                freeEntries(entries);
            }

            showStatus(counter, expectedEntries, null);
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Unexpected indexing error for " +
                p_index.getName(), ex);
            throw new IOException("unexpected error: " + ex.getMessage());
        }
        finally
        {
            m_reader.stop();
            m_writer.endIndex();

            speak(expectedEntries, 100, null);

            CATEGORY.info("Concept-level index for termbase `" +
                m_termbase.getName() + "' done.");
        }
    }

    /**
     * Rebuilds a term-level fuzzy index.
     */
    private void buildFuzzyIndex(Index p_index)
        throws IOException
    {
        CATEGORY.info("Building fuzzy index on `" + p_index.getName() +
            "' for termbase `" + m_termbase.getName() + "'.");

        ArrayList entries = new ArrayList(BATCHSIZE);
        ReaderResult result;

        int counter = 0, errCounter = 0;
        int expectedEntries = 0;

        try
        {
            speak(0, 0, null);

            expectedEntries = m_reader.getTermCount(p_index.getName());
            if (expectedEntries <= 0)
            {
                speak(0, 100, null);
                return;
            }

            showStatus(counter, expectedEntries, null);

            m_reader.startTerm(p_index.getName());
            m_writer.startBatchIndexing(p_index);

            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == result.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;
                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    m_writer.indexTerms(entries);

                    freeEntries(entries);

                    showStatus(counter, expectedEntries, null);
                }
            }

            // flush remaining entries
            if (entries.size() > 0)
            {
                m_writer.indexTerms(entries);

                freeEntries(entries);
            }

            showStatus(counter, expectedEntries, null);
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Unexpected indexing error for " +
                p_index.getName(), ex);
            throw new IOException("unexpected error: " + ex.getMessage());
        }
        finally
        {
            m_reader.stop();
            m_writer.endIndex();

            speak(expectedEntries, 100, null);

            CATEGORY.info("Fuzzy index on `" + p_index.getName() +
                "' for termbase `" + m_termbase.getName() + "' done.");
        }
    }

    /**
     * Rebuilds a full text index for a language by adding both
     * language-level text fields and term-level text fields.
     */
    private void buildFullTextIndex(Index p_index)
        throws IOException
    {
        CATEGORY.info("Building full text index on `" + p_index.getName() +
            "' for termbase `" + m_termbase.getName() + "'.");

        ArrayList entries = new ArrayList(BATCHSIZE);
        ReaderResult result;

        int counter = 0, errCounter = 0;
        int expectedEntries = 0;

        try
        {
            speak(0, 0, null);

            String langName = p_index.getName();

            expectedEntries = m_reader.getLanguageXmlCount(langName) +
                m_reader.getTermCount(langName);

            if (expectedEntries <= 0)
            {
                speak(0, 100, null);
                return;
            }

            showStatus(counter, expectedEntries, null);

            m_writer.startBatchIndexing(p_index);

            // Start reading language-level XML
            m_reader.startLanguageXml(langName);
            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == result.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;
                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    m_writer.indexXml(entries);

                    freeEntries(entries);

                    showStatus(counter, expectedEntries, null);
                }
            }

            // Continue with reading term-level XML
            m_reader.stop();
            m_reader.startTermXml(langName);

            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == result.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;
                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    m_writer.indexXml(entries);

                    freeEntries(entries);

                    showStatus(counter, expectedEntries, null);
                }
            }

            // flush remaining entries
            if (entries.size() > 0)
            {
                m_writer.indexXml(entries);

                freeEntries(entries);
            }

            showStatus(counter, expectedEntries, null);
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Unexpected indexing error for " +
                p_index.getName(), ex);
            throw new IOException("unexpected error: " + ex.getMessage());
        }
        finally
        {
            m_reader.stop();
            m_writer.endIndex();

            speak(expectedEntries, 100, null);

            CATEGORY.info("Full text index on `" + p_index.getName() +
                "' for termbase `" + m_termbase.getName() + "' done.");
        }
    }

    //
    // Helper Methods
    //

    private Reader createReader()
    {
        return new Reader(m_termbase, m_pool);
    }

    private Writer createWriter()
    {
        return new Writer(m_termbase);
    }

    private void freeEntries(ArrayList p_entries)
    {
        for (int i = 0, max = p_entries.size(); i < max; i++)
        {
            m_pool.freeInstance(p_entries.get(i));
        }

        p_entries.clear();
    }

    /**
     * Copies the termbase's indexes into local members and returns a
     * count of how many there are to be processed.
     */
    private int prepareIndexInfo()
    {
        m_conceptFulltextIndex = m_termbase.getConceptLevelFulltextIndex();

        m_fulltextIndexes = m_termbase.getFulltextIndexes();
        m_fuzzyIndexes = m_termbase.getFuzzyIndexes();

        int result = m_conceptFulltextIndex == null ? 0 : 1;
        result += m_fulltextIndexes.size();
        result += m_fuzzyIndexes.size();

        return result;
    }

    /**
     * Notifies the event listener of the current indexing status.
     * (2nd progress bar for individual indexes.)
     */
    private void speak(int p_entryCount, int p_percentage, String p_message)
        throws RemoteException, IOException
    {
        if (m_listener != null)
        {
            m_listener.listen(p_entryCount, p_percentage, p_message);
        }
    }

    /**
     * Notifies the event listener of the current indexing status.
     * (1st progress bar looping over indexes.)
     */
    private void speak2(String p_desc, int p_percentage, String p_message)
        throws RemoteException, IOException
    {
        if (m_listener != null)
        {
            m_listener.listen2(p_desc, p_percentage, p_message);
        }
    }

    private void setDone()
        throws RemoteException
    {
        if (m_listener != null)
        {
            m_listener.setDone();
        }
    }

    /**
     * Helper method for speak() that computes percentage.
     * (2nd progress bar for individual indexes.)
     */
    private void showStatus(int p_current, int p_expected, String p_message)
        throws IOException
    {
        int percentComplete =
            (int)((p_current * 1.0 / p_expected * 1.0) * 100.0);

        if (percentComplete > 100)
        {
            percentComplete = 100;
        }

        speak(p_current, percentComplete, p_message);
    }

    /**
     * Helper method for speak2() that computes percentage.
     * (1st progress bar looping over indexes.)
     */
    private void showStatus2(String p_desc, int p_current, int p_expected,
        String p_message)
        throws IOException
    {
        int percentComplete =
            (int)((p_current * 1.0 / p_expected * 1.0) * 100.0);

        if (percentComplete > 100)
        {
            percentComplete = 100;
        }

        speak2(p_desc, percentComplete, p_message);
    }
}

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

package com.globalsight.terminology;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.lucene.Index;
import com.globalsight.ling.lucene.TbFuzzyIndex;
import com.globalsight.ling.lucene.TbTextIndex;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.audit.TermAuditEvent;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.terminology.command.EntryOperation;
import com.globalsight.terminology.command.EntryOperationImpl;
import com.globalsight.terminology.command.ITbaseStatic;
import com.globalsight.terminology.command.TbStaticImpl;
import com.globalsight.terminology.entrycreation.EntryCreation;
import com.globalsight.terminology.entrycreation.EntryCreationDiscardByCid;
import com.globalsight.terminology.entrycreation.EntryCreationDiscardByLanguage;
import com.globalsight.terminology.entrycreation.EntryCreationMergeByCid;
import com.globalsight.terminology.entrycreation.EntryCreationMergeByLanguage;
import com.globalsight.terminology.entrycreation.EntryCreationOverWriteByCid;
import com.globalsight.terminology.entrycreation.EntryCreationOverWriteByLanguage;
import com.globalsight.terminology.entrycreation.IEntryCreation;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * The actual Terminology Database object including database definition,
 * operation modes and access to persistence layer. This is a multi-user,
 * multi-threaded object.
 * </p>
 * 
 * <p>
 * Read/write locking of termbases relates to schema changes: if an operation
 * changes the termbase definition, it needs to acquire a write lock; all other
 * operations need to acquire read locks.
 * </p>
 */
public class Termbase implements TermbaseExceptionMessages, WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(Termbase.class);

    //
    // Constants
    //
    public static final int STATE_UNDEFINED = 0;

    public static final int STATE_RUNNING = 1;

    public static final int STATE_SHUTDOWN = 2;

    public static final int STATE_DELETED = 2;

    /** Term column width in database */
    public static final int MAX_TERM_LEN = 2000;

    /** Sortkey column width in database */
    public static final int MAX_SORTKEY_LEN = 2000;

    //
    // Member Variables
    //

    /** Concurrency lock for readers */
    private long m_readers = 0;

    /** Concurrency lock for writers */
    private long m_writers = 0;

    private long m_state = STATE_UNDEFINED;

    /** Internal database id */
    private long m_id;

    /** Termbase name */
    private String m_name;

    /** Termbase description */
    private String m_description = "";

    /** Termbase definition */
    protected Definition m_definition;

    /** Termbase companyId */
    protected String m_companyId;

    /** Locks on entries when they are edited by a user. */
    private HashMap m_entryLocks = new HashMap();

    /** Fuzzy and full-text indexes. */
    private Index m_conceptLevelFulltextIndex;

    private ArrayList m_fuzzyIndexes = new ArrayList();

    private ArrayList m_fulltextIndexes = new ArrayList();

    private IIndexManager m_indexer;

    private StringBuffer m_details = null;

    private StringBuffer m_langs = null;

    //
    // Public Internal Classes
    //

    /**
     * Synchronization options for batch import.
     */
    static public class SyncOptions
    {
        // Synchronization constants, see importer.ImportOptions
        public static final int SYNC_BY_NONE = 1;

        public static final int SYNC_BY_CONCEPTID = 2;

        public static final int SYNC_BY_LANGUAGE = 3;

        public static final int SYNC_OVERWRITE = 10;

        public static final int SYNC_MERGE = 11;

        public static final int SYNC_DISCARD = 12;

        public static final int NOSYNC_ADD = 20;

        public static final int NOSYNC_DISCARD = 21;

        public int m_mode = SYNC_BY_NONE;

        public int m_syncAction = 0;

        public int m_nosyncAction = 0;

        public String m_language = "";

        public SyncOptions()
        {
        }

        public int getSyncMode()
        {
            return m_mode;
        }

        public void setSyncMode(int p)
        {
            if (p < SYNC_BY_NONE || p > SYNC_BY_LANGUAGE)
            {
                throw new IllegalArgumentException();
            }

            m_mode = p;
        }

        public int getSyncAction()
        {
            return m_syncAction;
        }

        public void setSyncAction(int p)
        {
            if (p < SYNC_OVERWRITE || p > SYNC_DISCARD)
            {
                throw new IllegalArgumentException();
            }

            m_syncAction = p;
        }

        public int getNosyncAction()
        {
            return m_nosyncAction;
        }

        public void setNosyncAction(int p)
        {
            if (p < NOSYNC_ADD || p > NOSYNC_DISCARD)
            {
                throw new IllegalArgumentException();
            }

            m_nosyncAction = p;
        }

        public String getSyncLanguage()
        {
            return m_language;
        }

        public void setSyncLanguage(String p)
        {
            if (p == null)
            {
                throw new IllegalArgumentException();
            }

            m_language = p;
        }
    }

    /**
     * Holds lists of SQLStatements to insert or update entries in the database.
     * Currently, these are strings. Later the statements will be objects that
     * can work together with prepared statements and batch updates.
     */
    static public class Statements
    {
        public ArrayList m_concepts = new ArrayList();

        public ArrayList m_languages = new ArrayList();

        public ArrayList m_terms = new ArrayList();

        // public ArrayList m_tokens = new ArrayList();
        public ArrayList m_others = new ArrayList();

        // public ArrayList m_clobs = new ArrayList();

        public void addConceptStatement(String s)
        {
            m_concepts.add(s);
        }

        public void addLanguageStatement(String s)
        {
            m_languages.add(s);
        }

        public void addTermStatement(String s)
        {
            m_terms.add(s);
        }

        public String toString()
        {
            StringBuffer result = new StringBuffer();

            result.append("---- Concepts:\n");
            for (int i = 0; i < m_concepts.size(); i++)
            {
                result.append(m_concepts.get(i));
                result.append("\n");
            }

            result.append("---- Language:\n");
            for (int i = 0; i < m_languages.size(); i++)
            {
                result.append(m_languages.get(i));
                result.append("\n");
            }

            result.append("---- Terms:\n");
            for (int i = 0; i < m_terms.size(); i++)
            {
                result.append(m_terms.get(i));
                result.append("\n");
            }

            /*
             * result.append("---- Tokens:\n"); for (int i = 0; i <
             * m_tokens.size(); i++) { result.append(m_tokens.get(i));
             * result.append("\n"); }
             */

            result.append("---- Other:\n");
            for (int i = 0; i < m_others.size(); i++)
            {
                result.append(m_others.get(i));
                result.append("\n");
            }

            return result.toString();
        }
    }

    //
    // Constructor
    //
    
    public Termbase(){}

    /**
     * Constructor for definitions loaded from the database (string).
     */
    protected Termbase(long p_id, String p_name, String p_description,
            String p_definition, String p_companyId) throws TermbaseException
    {
        m_id = p_id;
        m_name = p_name;
        m_companyId = p_companyId;

        if (p_description != null)
        {
            m_description = p_description;
        }

        m_definition = new Definition(p_definition);

        open();
    }

    /**
     * Constructor for in-memory definitions built by createTermbase().
     */
    protected Termbase(long p_id, String p_name, String p_description,
            Definition p_definition, String p_companyId)
            throws TermbaseException
    {
        m_id = p_id;
        m_name = p_name;
        m_companyId = p_companyId;

        if (p_description != null)
        {
            m_description = p_description;
        }

        m_definition = p_definition;

        open();
    }

    //
    // Synchronization Methods
    //

    public synchronized void addReader() throws TermbaseException
    {
        if (m_writers == 0 && m_state == STATE_RUNNING)
        {
            ++m_readers;
        }
        else
        {
            String[] args = { m_name };

            throw new TermbaseException(MSG_TB_IS_LOCKED, args, null);
        }
    }

    public synchronized void releaseReader() throws TermbaseException
    {
        if (m_readers > 0)
        {
            --m_readers;
        }
        else
        {
            throw new TermbaseException(MSG_INTERNAL_ERROR, null, null);
        }
    }

    public synchronized void addWriter() throws TermbaseException
    {
        if (m_readers == 0 && m_writers == 0 && m_state == STATE_RUNNING)
        {
            ++m_writers;
        }
        else
        {
            String[] args = { m_name };

            throw new TermbaseException(MSG_TB_IS_USED, args, null);
        }
    }

    public synchronized void releaseWriter() throws TermbaseException
    {
        if (m_writers > 0)
        {
            --m_writers;
        }
        else
        {
            throw new TermbaseException(MSG_INTERNAL_ERROR, null, null);
        }
    }

    //
    // Package-Private Methods
    //

    protected void open()
    {
        initIndexes(m_definition);

        m_state = STATE_RUNNING;
    }

    /**
     * Makes this termbase unavailable to further readers by setting the state
     * to STATE_SHUTDOWN and closing indexes.
     */
    protected void shutdown()
    {
        m_state = STATE_SHUTDOWN;

        closeIndexes();
    }

    /**
     * Deletes the indexes allocated by this database.
     */
    protected void delete()
    {
        deleteIndexes();
    }

    /**
     * Makes this termbase unavailable to further readers by setting the state
     * to STATE_DELETED. This acts like a "delete request" or "delete
     * notification" while the actual database data is modified. If deletion
     * fails, the termbase object can be made active again by calling
     * setRunning(). See {@see TermbaseManager()}.
     */
    protected void setDeleted()
    {
        m_state = STATE_DELETED;
    }

    /**
     * Makes this termbase available to readers by setting the state to
     * STATE_RUNNING.
     */
    protected void setRunning()
    {
        m_state = STATE_RUNNING;
    }

    /** Retrieves the termbase id. */
    public long getId()
    {
        return m_id;
    }

    /** Retrieves the termbase name. */
    public String getName()
    {
        return m_name;
    }

    /** Retrieves the termbase description. */
    public String getDescription()
    {
        return m_description;
    }

    /** Retrieves the termbase companyId. */
    public String getCompanyId()
    {
        return m_companyId;
    }

    /**
     * <p>
     * Retrieves the Entry Structure Schema used in the termbase (this is
     * <em>not</em> the Termbase Definition).
     * </p>
     * 
     * @return an XML string specifying the schema.
     * @see schema.dtd
     */
    public String getSchema()
    {
        // pull the dtd from a resource bundle
        return "";
    }

    /**
     * Returns true if the fuzzy and fulltext indexes are being rebuilt.
     */
    public synchronized boolean isIndexing()
    {
        return m_indexer != null;
    }

    public synchronized void setIndexer(IIndexManager p_manager)
            throws TermbaseException
    {
        if (m_indexer != null)
        {
            String[] args = { m_name };
            throw new TermbaseException(MSG_INDEXING_IN_PROGRESS, args, null);
        }

        // As long as there is an indexer there is a reader.
        addReader();

        m_indexer = p_manager;
    }

    public synchronized void clearIndexer(IIndexManager p_manager)
            throws TermbaseException
    {
        // Only clear indexer once. Prepare for multiple calls.
        if (m_indexer != null && m_indexer == p_manager)
        {
            m_indexer = null;

            releaseReader();
        }
        else
        {
            throw new TermbaseException(MSG_INTERNAL_ERROR, null, null);
        }
    }

    // For IndexManager. There's only one index manager and while it
    // is operating the indexes are not modifiable.
    public Index getConceptLevelFulltextIndex()
    {
        return m_conceptLevelFulltextIndex;
    }

    // For IndexManager. There's only one index manager and while it
    // is operating the indexes are not modifiable.
    public ArrayList getFuzzyIndexes()
    {
        return m_fuzzyIndexes;
    }

    // For IndexManager. There's only one index manager and while it
    // is operating the indexes are not modifiable.
    public ArrayList getFulltextIndexes()
    {
        return m_fulltextIndexes;
    }

    public Index getFuzzyIndex(String p_language)
    {
        ArrayList indexes = m_fuzzyIndexes;

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Index index = (Index) indexes.get(i);

            if (index.getName().equals(p_language))
            {
                return index;
            }
        }

        return null;
    }

    public Index getFulltextIndex(String p_language)
    {
        ArrayList indexes = m_fulltextIndexes;

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Index index = (Index) indexes.get(i);

            if (index.getName().equals(p_language))
            {
                return index;
            }
        }

        return null;
    }

    /**
     * <p>
     * Retrieves the termbase definition containing the languages, text fields,
     * and attribute fields.
     * </p>
     * 
     * @return an XML string specifying the termbase definition.
     */
    public String getDefinition() throws TermbaseException
    {
        addReader();

        try
        {
            return m_definition.getXml();
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * Given a language name, returns that language's locale as string.
     */
    public String getLocaleByLanguage(String p_language)
            throws TermbaseException
    {
        addReader();

        try
        {
            return m_definition.getLocaleByLanguage(p_language);
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * Given a locale, returns the language names that contain terms in that
     * locale. Normally this is one language only, but some termbases may
     * contain a general language like Spanish and a specific language like
     * Spanish (Costa Rica). In that case, when searching for es_CR, both
     * languages are returned (in no specific order).
     * 
     * @return an ArrayList of Strings.
     */
    public ArrayList getLanguagesByLocale(String p_locale)
            throws TermbaseException
    {
        addReader();

        try
        {
            return m_definition.getLanguagesByLocale(p_locale);
        }
        finally
        {
            releaseReader();
        }
    }

    public ArrayList getLanguages() throws TermbaseException
    {
        addReader();

        try
        {
            return m_definition.getLanguages();
        }
        finally
        {
            releaseReader();
        }
    }

    public boolean isIndexedField(String p_type) throws TermbaseException
    {
        addReader();

        try
        {
            return m_definition.isIndexedField(p_type);
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Renames a termbase by changing it's own name and the convenience copy of
     * the name in the definition object.
     * </p>
     * 
     * <p>
     * The caller must have obtained an exclusive lock (addWriter) on the
     * termbase.
     * </p>
     */
    synchronized void rename(String p_newName) throws TermbaseException
    {
        if (m_writers == 0)
        {
            String[] args = { m_name };

            throw new TermbaseException(MSG_TB_IS_NOT_LOCKED, args, null);
        }

        m_name = p_newName;
        m_definition.setName(p_newName);

        // Finally rename the indexes.
        renameIndexes(p_newName);
    }

    /**
     * <p>
     * Sets a new termbase definition by removing all languages (including terms
     * and fuzzy tokens) in the database that are no longer defined in the new
     * termbase.
     * </p>
     * 
     * <p>
     * This method can not be used to rename the termbase.
     * </p>
     * 
     * <p>
     * The caller must have obtained an exclusive lock (addWriter) on the
     * termbase.
     * </p>
     */
    protected void updateDefinition(Definition p_definition,
            SessionInfo p_session) throws TermbaseException
    {
        if (m_writers == 0)
        {
            String[] args = { m_name };

            throw new TermbaseException(MSG_TB_IS_NOT_LOCKED, args, null);
        }

        try
        {
            Definition _old = m_definition;
            Definition _new = p_definition;

            ArrayList oldLangs = _old.getLanguages();
            ArrayList newLangs = _new.getLanguages();

            ArrayList<String> deletedLangs = new ArrayList<String>();
            
            com.globalsight.terminology.java.Termbase tbase = 
                HibernateUtil.get(com.globalsight.terminology.java.Termbase.class, m_id);

            // Find out which languages have been removed in the new
            // definition.

            for (int i = 0, max = oldLangs.size(); i < max; i++)
            {
                Definition.Language lang = (Definition.Language) oldLangs
                        .get(i);

                if (!newLangs.contains(lang))
                {
                    deletedLangs.add(lang.getName());
                }
            }
            
            StringBuffer temStr = new StringBuffer();
            
            for (int i = 0, max = deletedLangs.size(); i < max; i++)
            {
                if (i == (deletedLangs.size() - 1))
                    temStr.append("'").append(deletedLangs.get(i)).append("'");
                else
                {
                    temStr.append("'").append(deletedLangs.get(i)).append("',");
                }
            }
            
            // Fix for GBS-2515
            // if (deletedLangs.size() > 0)
            // {
            // StringBuffer sb = new StringBuffer();
            // sb.append("from TbLanguage tl where tl.name in(");
            // sb.append(temStr.toString());
            // sb.append(") and tl.concept.termbase.id=").append(m_id);
            //
            // List list = HibernateUtil.search(sb.toString());
            // HibernateUtil.delete(list);
            // }
            
            String description = _new.getDescription();
            description = EditUtil.truncateUTF8Len(description, 4000);
            tbase.setDescription(description);
            tbase.setDefination(_new.getXml());
            HibernateUtil.update(tbase);

            StringBuffer temp = new StringBuffer();
            temp.append("Termbase ");
            temp.append(m_name);
            temp.append(" updated with new definition. ");

            if (deletedLangs.size() > 0)
            {
                temp.append(deletedLangs.size());
                temp.append(" languages deleted: ");
                temp.append(printArray(deletedLangs));
            }

            CATEGORY.info(temp);

            // Set the in-memory definition.
            m_definition = _new;

            // Update member variables from definition.
            m_description = m_definition.getDescription();

            // Finally update the indexes.
            updateIndexes(m_definition);
        }
        catch (Exception ex)
        {
            throw new TermbaseException(MSG_INTERNAL_ERROR, null, ex);
        }
    }

    /**
     * Retrieves a string containing termbase statistics.
     * 
     * @return an xml string of the form <statistics> <termbase>termbase name</termbase>
     *         <concepts>number of entries</concepts> <terms>number of overall
     *         terms</terms> <indexes> <index> <language>index name</language>
     *         <terms>number of terms</terms> </index> </indexes> </statistics>
     */
    public String getStatistics() throws TermbaseException
    {
        try
        {
            if (isIndexing())
            {
                StatisticsInfo result = new StatisticsInfo();
                result.setTermbase(m_name);
                result.setIndexStatus(StatisticsInfo.INDEX_NOTAVAILABLE);
                return result.asXML();
            }
            else
            {
                ITbaseStatic ts = new TbStaticImpl();
                ts.setConceptIndex(m_conceptLevelFulltextIndex);
                ts.setFullTextIndex(m_fulltextIndexes);
                ts.setFuzzyIndex(m_fuzzyIndexes);
                return ts.getStatistics(m_id);
            }
        }
        catch (TermbaseException e)
        {
            throw e;
        }
    }
    
    public String getStatisticsWithoutIndexInfo() throws TermbaseException
    {
        ITbaseStatic ts = new TbStaticImpl();
        return ts.getStatisticsNoIndex(m_id);
    }

    /**
     * <p>
     * Retrieves an entry as xml string conforming to the Entry Structure
     * Schema.
     * </p>
     */
    public String getEntry(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        return getEntry(p_entryId, 0, "", "", p_session);
    }
    
    /**
     * <p>
     * Retrieves an entry as xml string conforming to the Entry Structure
     * Schema.
     *  @return the empty string if the entry does not exist, else an XML string
     *         with root "conceptGrp".
     * </p>
     */
    public String getTbxEntry(String p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
    	addReader();
    	
    	try
        {
    	    EntryOperation eo = new EntryOperationImpl();
    	    String str = eo.getEntry(Long.parseLong(p_entryId),
                    WebAppConstants.TERMBASE_TBX, p_session);
    	    return str;
        }
    	finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Retrieves an entry as xml string conforming to the Entry Structure
     * Schema, with source/target language group and source term specially
     * marked for easy formatting in the viewer.
     * </p>
     * 
     * @return the empty string if the entry does not exist, else an XML string
     *         with root "conceptGrp".
     */
    public String getEntry(long p_entryId, long p_termId, String p_srcLang,
            String p_trgLang, SessionInfo p_session) throws TermbaseException
    {
        EntryOperation eo = new EntryOperationImpl();
        String str = eo.getEntry(p_entryId, p_termId, p_srcLang, p_trgLang,
                p_session);
        return str;
    }
    
    public String getEntryForBrowser(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        addReader();

        try
        {
            EntryOperation eo = new EntryOperationImpl();
            String str = eo.getEntryForBrowser(p_entryId, p_session);
            return str;
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Adds a new entry to the termbase.
     * </p>
     * 
     * @return the newly assigned entry ID as long.
     */
    public long addEntry(String p_entry, SessionInfo p_session)
            throws TermbaseException
    {
        addReader();

        try
        {
            EntryOperation eo = new EntryOperationImpl();
            return eo.addEntry(m_id, p_entry, m_definition, p_session);
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Adds a list of new entries to the termbase. This is more efficient than
     * adding each entry individually.
     * 
     * Return the failed entry array.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    public ArrayList batchAddEntries(ArrayList p_entries, SyncOptions p_options,
            SessionInfo p_session, String fileType) throws TermbaseException
    {
        checkEntry(p_entries, fileType);
        IEntryCreation ic = null;

        switch (p_options.m_mode)
        {
            case SyncOptions.SYNC_BY_NONE:
                ic = new EntryCreation(fileType);
                break;
            case SyncOptions.SYNC_BY_CONCEPTID:
                if (p_options.getSyncAction() == SyncOptions.SYNC_OVERWRITE)
                {
                    ic = new EntryCreationOverWriteByCid(fileType);
                }
                else if (p_options.getSyncAction() == SyncOptions.SYNC_MERGE)
                {
                    ic = new EntryCreationMergeByCid(fileType);
                }
                else if (p_options.getSyncAction() == SyncOptions.SYNC_DISCARD)
                {
                    ic = new EntryCreationDiscardByCid(fileType);
                }

                break;
            case SyncOptions.SYNC_BY_LANGUAGE:
                if (p_options.getSyncAction() == SyncOptions.SYNC_OVERWRITE)
                {
                    ic = new EntryCreationOverWriteByLanguage(fileType);
                }
                else if (p_options.getSyncAction() == SyncOptions.SYNC_MERGE)
                {
                    ic = new EntryCreationMergeByLanguage(fileType);
                }
                else if (p_options.getSyncAction() == SyncOptions.SYNC_DISCARD)
                {
                    ic = new EntryCreationDiscardByLanguage(fileType);
                }

                break;
        }

        ic.setSynchronizeOption(p_options);
        ic.batchAddEntriesAsNew(m_id, p_entries, p_session);
        return ic.getFailedEntries();
    }

    /**
     * Locks an entry for later update.
     * 
     * @param steal:
     *            locks can be stolen if they are held by the same user or have
     *            expired (default life time: 1 hour).
     * 
     * @return the empty string if the lock could not be obtained, or a
     *         non-empty string whose content is meaningless to the client.
     *         Well, in fact it is a hereby documented xml string:
     * 
     * <lock> <conceptid>1000</conceptid> <who>user name</who> <when>date</when>
     * <email>user's email address</email>
     * <cookie>E96CFDC1-9909-4264-A150-986BBE8E9564</cookie> </lock>
     */
    public String lockEntry(long p_entryId, boolean p_steal,
            SessionInfo p_session) throws TermbaseException
    {
        EntryOperation eo = new EntryOperationImpl();
        String result = 
            eo.lockEntry(m_id, p_entryId, p_steal, m_entryLocks, p_session);

        return result;
    }

    /**
     * <p>
     * Unlocks a previously locked entry. The cookie must have been obtained by
     * calling lockEntry() or the call will fail.
     * </p>
     */
    public void unlockEntry(long p_entryId, String p_cookie,
            SessionInfo p_session) throws TermbaseException
    {
        EntryOperation eo = new EntryOperationImpl();
        eo.unlockEntry(p_entryId, p_cookie, m_entryLocks, p_session);
    }

    /**
     * Returns the lock information for an entry.
     * 
     * @return the empty string if no lock has been set, or an xml string:
     * 
     * <lock> <conceptid>1000</conceptid> <who>user name</who> <when>date</when>
     * <email>user's email address</email> </lock>
     */
    public String getLockInfo(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        String result = "";

        LockInfo lock = getLockInfoAsLock(p_entryId, p_session);

        if (lock != null)
        {
            // return publicly accessible information only
            result = lock.asPublicXML();
        }

        return result;
    }

    /**
     * Helper method to return lock info as LockInfo object.
     */
    private LockInfo getLockInfoAsLock(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        LockInfo result;
        Long key = new Long(p_entryId);

        synchronized (m_entryLocks)
        {
            result = (LockInfo) m_entryLocks.get(key);
        }

        return result;
    }

    /**
     * For internal use only: checks if an entry is locked by anybody.
     */
    public boolean isLocked(Long p_entryId)
    {
        Long key = p_entryId;

        synchronized (m_entryLocks)
        {
            return m_entryLocks.get(key) != null;
        }
    }

    /**
     * For internal use only: unconditionally unlocks an entry, e.g. if an entry
     * gets deleted or updated by search/replace.
     */
    private void unlockEntryInternal(long p_entryId)
    {
        unlockEntryInternal(new Long(p_entryId));
    }

    public void unlockEntryInternal(Long p_entryId)
    {
        Long key = p_entryId;

        synchronized (m_entryLocks)
        {
            m_entryLocks.remove(key);
        }
    }

    /**
     * <p>
     * Updates an existing entry. The entry must have been locked for editing by
     * calling strCookie = lockEntry(entryID, false). If the update is
     * successful, the lock will be automatically released. If an error occurs,
     * the lock will remain set and must be manually released by the caller by
     * calling unlockEntry().
     * </p>
     * 
     * @throws an
     *             exception if the lock is not owned by the caller anymore,
     *             i.e., when it has been stolen by another user.
     */
    public void updateEntry(long p_entryId, String p_entry, String p_cookie,
            SessionInfo p_session) throws TermbaseException
    {
        // First check if entry still exists.
        String xml = getEntry(p_entryId, p_session);
        if (xml == null || xml.length() == 0)
        {
            throw new TermbaseException(MSG_ENTRY_DOES_NOT_EXIST, null, null);
        }

        // Then check if it has been appropriately locked by the caller.
        LockInfo myLock = new LockInfo(p_cookie);
        LockInfo lock = getLockInfoAsLock(p_entryId, p_session);

        if (lock == null)
        {
            throw new TermbaseException(MSG_ENTRY_NOT_LOCKED, null, null);
        }

        if (!lock.equals(myLock))
        {
            throw new TermbaseException(MSG_YOU_DONT_OWN_LOCK, null, null);
        }

        addReader();

        try
        {
            Entry newEntry = new Entry(p_entry);

            // Minimize entry and check for presence of required fields.
            EntryUtils.normalizeEntry(newEntry, m_definition);

            // Save entry to database
            m_langs = new StringBuffer();
            m_details = new StringBuffer();
            
            EntryOperation eo = new EntryOperationImpl();
            eo.updateEntry(p_entryId, newEntry, p_session);

            // write out audit event
            TermAuditEvent auditEvent = new TermAuditEvent(new Date(),
                    p_session.getUserName(), m_name, "Entry " + p_entryId,
                    m_langs.toString(), "update", m_details.toString());
            TermAuditLog.log(auditEvent);
        }
        finally
        {
            releaseReader();
        }
    }

    /** Deletes an entry from the termbase. */
    public void deleteEntry(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        // TODO: need to check if somebody is editing the entry.
        // Currently we allow unconditional deletion of entries.
        unlockEntryInternal(p_entryId);
        EntryOperation eo = new EntryOperationImpl();
        eo.deleteEntry(p_entryId, p_session);
    }

    /**
     * Validates an entry by comparing all terms against the termbase and
     * checking for duplicates or near duplicates.
     * 
     * @see ValidationInfo
     */
    public String validateEntry(String p_entry, SessionInfo p_session)
            throws TermbaseException
    {

        EntryOperation eo = new EntryOperationImpl();
        return eo.validateEntry(m_definition, m_id, p_entry, p_session);

    }

    /**
     * <p>
     * Searches a given index for the given expression. A hitlist is returned
     * listing the terms found in the termbase, their score, and their entry
     * number. For normal (alphabetical) searches, the hitlist starts with the
     * first term that is equal or greater to the search term.
     * </p>
     * 
     * @param index:
     *            a valid index name in the termbase
     * 
     * @param query:
     *            the search content. if it's empty, then search all
     * 
     * @param begin: the begin position of searching
     * 
     * @param maxhits:
     *            specifies how many hits should be retrieved. 
     * 
     * @return a hitlist as xml string of the form:
     * 
     * <hitlist> <index>english</index> <expression>parl*m?nt</expression>
     * <hits> <hit><score>100</score><term>parlamant</term><entry>10</entry></hit>
     * <hit><score>100</score><term>parlament</term><entry>11</entry></hit>
     * <homographs> <hit><score>100</score><term>parliament</term><entry>12</entry></hit>
     * <hit><score>100</score><term>parliament</term><entry>13</entry></hit>
     * </homographs> <hit><score>100</score><term>parlamont</term><entry>14</entry></hit>
     * </hits> </hitlist>
     */
    public String search(String p_language, String target_language, 
            String p_query, String queryType, int p_maxHits, int begin)
            throws TermbaseException
    {
        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return "<hitlist><hits></hits></hitlist>";
        }
        
        try
        {
            TermSearch termSearch = getTermSearch(p_language, p_query,
                    queryType);

            String str = termSearch.getXmlResults(p_language, target_language,
                    p_query, p_maxHits, begin);

            return str;
        }
        catch(Exception e){
            throw new TermbaseException(e);
        }
    }
    
    /**
     * Search "exact" or "fuzzy" hit list.
     */
    public Hitlist searchHitlist(String p_language, String target_language, 
            String p_query, String queryType, int p_maxHits, int begin)
            throws TermbaseException
    {
        Hitlist result = null;
        
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search for `" + p_query + "' in " + p_language);
        }

        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return null;
        }


        try
        {
            TermSearch termSearch = getTermSearch(p_language, p_query, queryType);

            result = termSearch.getHitListResults(p_language, target_language,
                    p_query, p_maxHits, begin);
        }
        catch(Exception e){
            throw new TermbaseException(e);
        }
        
        return result;
    }
    
    private TermSearch getTermSearch(String p_language, String p_query,
            String queryType)
    {
        TermSearch termSearch = new FuzzySearch();
        termSearch.setIndexs(m_fuzzyIndexes);

        // if p_query is "", need get all terms to show
        if (p_query == null || p_query.trim().equals("")
                || queryType.equals("exact"))
        {
            termSearch = new ExactSearch();
            termSearch.setTermbaseId(m_id);
        }

        return termSearch;
    }

    /**
     * <p>
     * Searches a given fuzzy index for the given expression.
     */
    public String fuzzySearch(String p_language, String p_query, int p_maxHits)
            throws TermbaseException
    {
        return search(p_language, null, p_query, "fuzzy", p_maxHits, 0);
    }
    
    /**
     * <p>
     * Implements part of term leveraging: for the query string, all matching
     * terms are retrieved from the termbase.
     * </p>
     * 
     * @see termleverager.TermLeverager
     * 
     * @param language:
     *            a valid language name in the termbase
     * @param query:
     *            a string for which to search terms
     * @param maxHits:
     *            a limit on the number of hits returned
     * 
     * @return Hitlist a Hitlist object listing all the matching terms.
     */
    public Hitlist recognizeTerms(String p_language, String p_query,
            int p_maxHits) throws TermbaseException
    {
        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return null;
        }
        
        addReader();

        try
        {
            Hitlist result;

            Definition.Language language = m_definition.getLanguage(p_language);

            if (language == null)
            {
                String[] args = { "language is unknown" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            TermSearch ts = new FuzzySearch();
            ts.setIndexs(m_fuzzyIndexes);
            result = ts.getHitListResults(language.getName(), null,
                    p_query, p_maxHits, 0);

            return result;
        }
        finally
        {
            releaseReader();
        }
    }

    //
    // Other public methods
    //

    /**
     * Retrieves the number of entries in the termbase.
     * 
     * @see getStatistics()
     */
    public int getEntryCount() throws SQLException, Exception
    {
        return getEntryCount(null);
    }

    /**
     * Retrieves the number of entries in the termbase, optionally filtered by
     * an EntryFilter.
     * 
     * @see getStatistics()
     */
    public int getEntryCount(EntryFilter p_filter) throws 
            Exception
    {
        addReader();
        
        try
        {
            String hql = "select count(*) from TbConcept t where t.termbase.id="
                    + m_id;
            int result = HibernateUtil.count(hql);
            return result;
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * Returns the number of terms in the given language.
     * 
     * @see getStatistics()
     */
    public int getTermCount(String p_language) throws TermbaseException
    {
        return getTermCount(p_language, null, false);
    }

    /**
     * Returns the number of terms in the given language, optionally filtered by
     * an EntryFilter.
     * 
     * @see getStatistics()
     */
    public int getTermCount(String p_language, EntryFilter p_filter, boolean isExport)
            throws TermbaseException
    {
        addReader();

        try
        {
        	if(isExport)
        	{
        		List entryIdList =  getEntryIds(p_language,p_filter);
        		return entryIdList.size();
        	}
        	else
        	{
        		Definition.Language language = m_definition.getLanguage(p_language);
        		
        		if (language == null)
        		{
        			String[] args =
        			{ "unknown language" };
        			throw new TermbaseException(MSG_INVALID_ARG, args, null);
        		}
        		
        		com.globalsight.terminology.java.Termbase tbase = HibernateUtil
        		.get(com.globalsight.terminology.java.Termbase.class, m_id);
        		String hql = "from TbTerm tm where tm.tbLanguage.concept.termbase=:tbase "
        			+ "and tm.tbLanguage.name=:planguage ";
        		HashMap map = new HashMap();
        		map.put("tbase", tbase);
        		map.put("planguage", SqlUtil.quote(SqlUtil.quote(p_language)));
        		
        		if (p_filter != null && p_filter.isDbFiltering())
        		{
        			hql += p_filter.getSqlExpression("tm.tbLanguage.concept", true);
        			HashMap map1 = p_filter.getQueryMap();
        			map.putAll(map1);
        			
        			if (CATEGORY.isDebugEnabled())
        			{
        				CATEGORY.debug("getEntryCount(filter): " + hql);
        			}
        		}
        		
        		Collection terms = HibernateUtil.search(hql, map);
        		return terms.size();
        	}
        }
        catch (Exception e)
        {
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * Returns the number of languageGrps that contain fields in the given
     * language, i.e. whose XML is not null.
     * 
     * @see getStatistics()
     */
    public int getLanguageXmlCount(String p_language) throws TermbaseException
    {
        addReader();
        
        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("select count(*) from TbLanguage t ");
            hql.append("where t.concept.termbase.id=");
            hql.append(m_id);
            hql.append(" and t.name='");
            hql.append(p_language).append("'");
            int result = HibernateUtil.count(hql.toString());
            
            return result;
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * Retrieves the entry ids in the termbase for export.
     */
    public ArrayList getEntryIds() throws TermbaseException
    {
        return getEntryIds((EntryFilter) null);
    }

    /**
     * Retrieves the entry ids in the termbase for export, applying an optional
     * filter in the DB.
     */
    public ArrayList getEntryIds(EntryFilter p_filter) throws TermbaseException
    {
        return getEntryIds(null, p_filter);
    }

    /**
     * Returns the entry ids that have terms in the given language for export.
     */
    public ArrayList getEntryIds(String p_language) throws TermbaseException
    {
        return getEntryIds(p_language, null);
    }

    /**
     * Returns the entry ids that have terms in the given language for export,
     * optionally applying a database filter.
     */

    public ArrayList getEntryIds(String p_language, EntryFilter p_filter)
            throws TermbaseException
    {
        addReader();

        if (p_language != null && !p_language.trim().equals(""))
        {
        	Set resultSet = new HashSet();
        	String[] languageNames = p_language.split(",");
        	List<Set> idSets = new ArrayList<Set>();
        	try
        	{
        		for(String languageName :languageNames)
        		{
        			StringBuffer hql = new StringBuffer();
        			hql.append("select t.concept.id from TbLanguage t ");
        			hql.append("where t.concept.termbase.id=");
        			hql.append(m_id);
        			
        			if (p_language != null && !p_language.trim().equals(""))
        			{
        				hql.append(" and t.name='").append(languageName).append("' ");
        			}
        			
        			if (p_filter != null && p_filter.isDbFiltering())
        			{
        				hql.append(p_filter.getSqlExpression("t.concept", true));
        			}
        			
        			HashMap map1 = p_filter.getQueryMap();
        			Set set = new HashSet(HibernateUtil.search(hql.toString(), map1));
        			if(set != null && set.size() > 0)
        				idSets.add(set);
        		}
        		for(int i = 0; i < idSets.size(); i++)
        		{
        			if(i == idSets.size() -1)
        				break;
        			
        			Set set1 = idSets.get(i);
        			for(int j = i + 1; j < idSets.size(); j++)
        			{
        				Set set2 = idSets.get(j);
        				resultSet.addAll(getIntersectionSet(set1, set2));
        			}
        		}
        		return new ArrayList(resultSet);
        	}
        	finally
        	{
        		releaseReader();
        	}
        }
        else
        {
        	StringBuffer hql = new StringBuffer();
        	hql.append("select t.concept.id from TbLanguage t ");
        	hql.append("where t.concept.termbase.id=");
        	hql.append(m_id);
        	
        	if (p_filter != null && p_filter.isDbFiltering())
        	{
        		hql.append(p_filter.getSqlExpression("t.concept", true));
        	}
        	
        	try
        	{
        		HashMap map1 = p_filter.getQueryMap();
        		Set set = new HashSet(HibernateUtil.search(hql.toString(), map1));
        		return new ArrayList(set);
        	}
        	finally
        	{
        		releaseReader();
        	}
        }
    }
    
    private Set getIntersectionSet(Set set1, Set set2)
    {
    	Set insertsectionSet = new HashSet();
    	
    	if(set1.size() < set2.size())
    	{
    		for(Object obj: set1)
    		{
    			if(set2.contains(obj))
    				insertsectionSet.add(obj);
    		}
    	}
    	else
    	{
    		for(Object obj: set2)
    		{
    			if(set1.contains(obj))
    				insertsectionSet.add(obj);
    		}
    	}
    	
    	return insertsectionSet;
    }
    
    public void checkEntry(ArrayList p_entries, String fileType)
    {
        ArrayList array = new ArrayList(p_entries);
        
        for (int i = 0, max = array.size(); i < max; ++i)
        {
            Entry entry = (Entry) array.get(i);
            // Check for presence of required fields.
            try
            {
                if (fileType != null && fileType.equalsIgnoreCase(TERMBASE_TBX)) {
                    EntryUtils.normalizeTbxEntry(entry, m_definition);
                }
                else {
                    EntryUtils.normalizeEntry(entry, m_definition);
                }
            }
            catch (Exception e)
            {
                p_entries.remove(entry);
                // Ignore errors in this one entry.
                CATEGORY.warn("batchAddEntries: ignoring invalid entry: "
                        + e.getMessage() + "\n" + entry.getXml());
                continue;
            }
        }
    }

    /**
     * Sets up all necessary index objects for this termbase.
     * 
     * This method is called from the constructor so it is unprotected.
     */
    public void initIndexes(Definition p_definition)
    {
        ArrayList indexes = p_definition.getIndexes();
        Index idx = null;

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Definition.Index index = (Definition.Index) indexes.get(i);

            if (index.getLanguageName().length() == 0)
            {
                // Concept-level full-text index in the specified locale.
                try
                {
                    idx = new TbTextIndex(m_name, "", index.getLocale());
                    idx.open();
                    m_conceptLevelFulltextIndex = idx;
                }
                catch (IOException ex)
                {
                    String dir = idx == null ? m_name : idx.getDirectory();
                    CATEGORY.error("index " + dir + " cannot be opened", ex);
                }
            }
            else if (index.getType().equals(index.TYPE_FULLTEXT))
            {
                try
                {
                    idx = new TbTextIndex(m_name, index.getLanguageName(), index
                            .getLocale());
                    idx.open();
                    m_fulltextIndexes.add(idx);
                }
                catch (IOException ex)
                {
                    String dir = idx == null ? m_name : idx.getDirectory();
                    CATEGORY.error("index " + dir + " cannot be opened", ex);
                }
            }
            else if (index.getType().equals(index.TYPE_FUZZY))
            {
                try
                {
                    idx = new TbFuzzyIndex(m_name, index.getLanguageName(), index
                            .getLocale());
                    idx.open();
                    m_fuzzyIndexes.add(idx);
                }
                catch (IOException ex)
                {
                    String dir = idx == null ? m_name : idx.getDirectory();
                    CATEGORY.error("index " + dir + " cannot be opened", ex);
                }
            }
            else
            {
                // This is really a corrupt termbase definition.
                CATEGORY.warn("Ignoring invalid index definition in termbase `"
                        + m_name + "': " + index.asXML()
                        + "\nPlease run the FixTbDefinitions script.");
            }
        }
    }

    private void closeIndex(Index p_index)
    {
        try
        {
            p_index.close();
        }
        catch (Exception ex)
        {
            CATEGORY.error("index " + p_index.getDirectory()
                    + " cannot be closed", ex);
        }
    }

    private void closeIndexes()
    {
        if (m_conceptLevelFulltextIndex != null)
        {
            closeIndex(m_conceptLevelFulltextIndex);
            m_conceptLevelFulltextIndex = null;
        }

        for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
        {
            closeIndex((Index) m_fulltextIndexes.get(i));
        }
        m_fulltextIndexes.clear();

        for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
        {
            closeIndex((Index) m_fuzzyIndexes.get(i));
        }
        m_fuzzyIndexes.clear();
    }

    private void deleteIndex(Index p_index)
    {
        try
        {
            p_index.drop();
        }
        catch (Exception ex)
        {
            CATEGORY.error("index " + p_index.getDirectory()
                    + " cannot be deleted", ex);
        }
    }

    private void deleteIndexes()
    {
        if (m_conceptLevelFulltextIndex != null)
        {
            deleteIndex(m_conceptLevelFulltextIndex);
            m_conceptLevelFulltextIndex = null;
        }

        for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
        {
            deleteIndex((Index) m_fulltextIndexes.get(i));
        }
        m_fulltextIndexes.clear();

        for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
        {
            deleteIndex((Index) m_fuzzyIndexes.get(i));
        }
        m_fuzzyIndexes.clear();
    }

    private void renameIndex(Index p_index, String p_newName)
    {
        try
        {
            p_index.rename(p_newName);
        }
        catch (Exception ex)
        {
            CATEGORY.error("index " + p_index.getDirectory()
                    + " cannot be renamed", ex);
        }
    }

    /**
     * Renames all indexes in this termbase.
     */
    private void renameIndexes(String p_newName)
    {
        if (m_conceptLevelFulltextIndex != null)
        {
            renameIndex(m_conceptLevelFulltextIndex, p_newName);
        }

        for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
        {
            Index idx = (Index) m_fulltextIndexes.get(i);

            renameIndex(idx, p_newName);
        }

        for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
        {
            Index idx = (Index) m_fuzzyIndexes.get(i);

            renameIndex(idx, p_newName);
        }
    }

    private boolean isIndexDefined(Index p_index, Definition p_definition)
    {
        ArrayList indexes = p_definition.getIndexes();

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Definition.Index index = (Definition.Index) indexes.get(i);

            String lang = index.getLanguageName();
            String locale = index.getLocale();

            // Check both locale and language since the new definition
            // may have changed name or locale.

            if (lang.equals(p_index.getName())
                    && locale.equals(p_index.getLocale()))
            {
                String type = index.getType();

                if (type.equals(index.TYPE_FULLTEXT)
                        && p_index instanceof TbTextIndex)
                {
                    return true;
                }

                if (type.equals(index.TYPE_FUZZY)
                        && p_index instanceof TbFuzzyIndex)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Keeps the indexes defined in the TB definition and the indexes allocated
     * by this object in sync.
     */
    private void updateIndexes(Definition p_definition)
    {
        // First drop indexes for languages that have been deleted.

        if (m_conceptLevelFulltextIndex != null
                && !isIndexDefined(m_conceptLevelFulltextIndex, p_definition))
        {
            deleteIndex(m_conceptLevelFulltextIndex);
            m_conceptLevelFulltextIndex = null;
        }

        for (Iterator it = m_fulltextIndexes.iterator(); it.hasNext();)
        {
            Index idx = (Index) it.next();

            if (!isIndexDefined(idx, p_definition))
            {
                deleteIndex(idx);
                it.remove();
            }
        }

        for (Iterator it = m_fuzzyIndexes.iterator(); it.hasNext();)
        {
            Index idx = (Index) it.next();

            if (!isIndexDefined(idx, p_definition))
            {
                deleteIndex(idx);
                it.remove();
            }
        }

        // Then close and reopen the remaining and new indexes.

        closeIndexes();
        initIndexes(p_definition);
    }

    /**
     * Static toString method for ArrayList.
     */
    private static String printArray(ArrayList p_array)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_array.size(); i < max; i++)
        {
            result.append(p_array.get(i));
            if (i < max - 1) result.append(",");
        }

        return result.toString();
    }
}

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.regexp.RE;
import org.dom4j.Element;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.lucene.Index;
import com.globalsight.ling.lucene.TbFuzzyIndex;
import com.globalsight.ling.lucene.TbTextIndex;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.terminology.audit.TermAuditEvent;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.terminology.indexer.IndexObject;
import com.globalsight.terminology.indexer.Writer;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverager;
import com.globalsight.terminology.termleverager.TermLeveragerException;
import com.globalsight.terminology.util.Sortkey;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.UTC;
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
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
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

    //
    // Private Internal Classes
    //
    // static public class ClobPair
    // {
    // public String m_stmt;
    // public String m_value;
    //
    // public ClobPair(String p_stmt, String p_value)
    // {
    // m_stmt = p_stmt;
    // m_value = p_value;
    // }
    //
    // public String toString()
    // {
    // return "CLOB UPDATE: " + m_stmt + " --> `" + m_value + "'";
    // }
    // }

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

        /*
         * public void addTokenStatement(String s) { m_tokens.add(s); }
         */

        public void addOtherStatement(String s)
        {
            m_others.add(s);
        }

        // public void addClobStatement(String stmt, String value)
        // {
        // m_clobs.add(new ClobPair(stmt, value));
        // }

        public void addAll(Statements other)
        {
            m_concepts.addAll(other.m_concepts);
            m_languages.addAll(other.m_languages);
            m_terms.addAll(other.m_terms);
            // m_tokens.addAll(other.m_tokens);
            m_others.addAll(other.m_others);
            // m_clobs.addAll(other.m_clobs);
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

            ArrayList deletedLangs = new ArrayList();

            // Find out which languages have been removed in the new
            // definition.
            Statements stmts = new Statements();

            for (int i = 0, max = oldLangs.size(); i < max; i++)
            {
                Definition.Language lang = (Definition.Language) oldLangs
                        .get(i);

                // This test is performed using Language.equals():
                // same name + same locale.
                if (!newLangs.contains(lang))
                {
                    deletedLangs.add(lang.getName());

                    stmts.addAll(getDeleteLanguageStatements(lang.getName()));
                }
            }

            stmts.addAll(getUpdateDefinitionStatements(_new));

            // Execute and commit all changes in one go.
            executeStatements(stmts);

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
        StatisticsInfo info = getStatisticsInfo();

        return info.asXML();
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
     * </p>
     */
    public String getTbxEntry(String p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        return getTbxEntry(p_entryId, 0, "", "", p_session);
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
    public String getTbxEntry(String p_entryId, long p_termId, String p_srcLang,
            String p_trgLang, SessionInfo p_session) throws TermbaseException
    {
    	addReader();
    	try
    	{
    		StringBuffer result = new StringBuffer();

            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;
            
            try
            {
            	conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                // Retrieve concept level data
                stmt = conn.createStatement();
                result.append("<termEntry id=\"").append(p_entryId).append("\">");
                
             // Retrieve languages and terms
                rset = stmt
                        .executeQuery("select l.LID, l.NAME, l.LOCALE, l.XML L_XML, "
                                + "       t.TID, t.TERM, t.XML T_XML "
                                + "from TB_LANGUAGE l, TB_TERM t "
                                + "where "
                                + "     l.TBId ="
                                + m_id
                                + " and l.Cid  ="
                                + p_entryId
                                + " and t.TBId ="
                                + m_id
                                + " and t.Cid ="
                                + p_entryId
                                + " and t.Lid = l.Lid " + "order by l.lid");
                
                long previousLid = 0;
                while (rset.next())
                {
                    long lid = rset.getLong("LID");
                    String langLocale = rset.getString("LOCALE");
                    String term = rset.getString("TERM");

                    // start a new languageGrp for a new language
                    if (lid != previousLid)
                    {
                        if (previousLid != 0)
                        {
                            result.append("</langSet>");
                        }

                        result.append("<langSet").append(" xml:lang=\"").append(langLocale).append("\">");
                    }

                    result.append("<ntig>").append("<termGrp>").append("<term>");

                    result.append(EditUtil.encodeXmlEntities(term));
                    result.append("</term>");
                    result.append("</termGrp>").append("</ntig>");

                    previousLid = lid;
                }
             // there could be entries with no languages at all??
                if (previousLid != 0)
                {
                    result.append("</langSet>");
                }

                result.append("</termEntry>");

                conn.commit();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Get entry " + p_entryId + ": "
                            + result.toString());
                }

                return result.toString();
            }
            catch (Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
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
        addReader();

        try
        {
            StringBuffer result = new StringBuffer();

            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                // Retrieve concept level data
                stmt = conn.createStatement();
                rset = stmt.executeQuery("select XML from TB_CONCEPT "
                        + "where TBid=" + m_id + " and Cid=" + p_entryId);

                if (!rset.next())
                {
                    conn.commit();

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("Get entry " + p_entryId
                                + ": entry does not exist.");
                    }

                    return "";
                }

                result.append("<conceptGrp>");
                result.append(SqlUtil.readClob(rset, "XML"));

                // Retrieve languages and terms
                rset = stmt
                        .executeQuery("select l.LID, l.NAME, l.LOCALE, l.XML L_XML, "
                                + "       t.TID, t.TERM, t.XML T_XML "
                                + "from TB_LANGUAGE l, TB_TERM t "
                                + "where "
                                + "     l.TBId ="
                                + m_id
                                + " and l.Cid  ="
                                + p_entryId
                                + " and t.TBId ="
                                + m_id
                                + " and t.Cid ="
                                + p_entryId
                                + " and t.Lid = l.Lid " + "order by l.lid");

                long previousLid = 0;
                while (rset.next())
                {
                    long lid = rset.getLong("LID");
                    long tid = rset.getLong("TID");
                    String langName = rset.getString("NAME");
                    String langLocale = rset.getString("LOCALE");
                    String term = rset.getString("TERM");
                    String txml = SqlUtil.readClob(rset, "T_XML");

                    // start a new languageGrp for a new language
                    if (lid != previousLid)
                    {
                        if (previousLid != 0)
                        {
                            result.append("</languageGrp>");
                        }

                        result.append("<languageGrp>");
                        result.append("<language name=\"");
                        result.append(EditUtil.encodeXmlEntities(langName));
                        result.append("\" locale=\"");
                        result.append(EditUtil.encodeXmlEntities(langLocale));
                        result.append("\"");

                        if (langName.equals(p_srcLang))
                        {
                            result.append(" source-lang=\"true\"");
                        }
                        else if (langName.equals(p_trgLang))
                        {
                            result.append(" target-lang=\"true\"");
                        }

                        result.append("/>");

                        String lxml = SqlUtil.readClob(rset, "L_XML");

                        result.append(lxml);
                    }

                    result.append("<termGrp>");
                    result.append("<term");

                    if (tid == p_termId)
                    {
                        result.append(" search-term=\"true\"");
                    }

                    result.append(">");
                    result.append(EditUtil.encodeXmlEntities(term));
                    result.append("</term>");
                    result.append(txml);
                    result.append("</termGrp>");

                    previousLid = lid;
                }

                // there could be entries with no languages at all??
                if (previousLid != 0)
                {
                    result.append("</languageGrp>");
                }

                result.append("</conceptGrp>");

                conn.commit();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Get entry " + p_entryId + ": "
                            + result.toString());
                }

                return result.toString();
            }
            catch (Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
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
            Entry entry = new Entry(p_entry);

            // Minimize entry and check for presence of required fields.
            EntryUtils.normalizeEntry(entry, m_definition);

            // Save entry to database
            long cid = addEntryToDatabase(entry, m_definition, p_session);

            // Cache entry?

            return cid;
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
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    public void batchAddEntries(ArrayList p_entries, SyncOptions p_options,
            SessionInfo p_session, String fileType) throws TermbaseException
    {
        addReader();

        try
        {
            Statements stmts = null;

            switch (p_options.m_mode)
            {
                case SyncOptions.SYNC_BY_NONE:
                	if (fileType != null && fileType.equalsIgnoreCase(TERMBASE_TBX)) {
                		stmts = batchAddTbxEntriesAsNew(p_entries, p_options,
                                p_session);
                	} else {
                		stmts = batchAddEntriesAsNew(p_entries, p_options,
                                p_session);
                	}
                    break;
                case SyncOptions.SYNC_BY_CONCEPTID:
                	if (fileType != null && fileType.equalsIgnoreCase(TERMBASE_TBX)) {
                		stmts = batchAddTbxEntriesByConceptId(p_entries, p_options,
                                p_session);
                	} else {
                		stmts = batchAddEntriesByConceptId(p_entries, p_options,
                                p_session);
                	}
                    break;
                case SyncOptions.SYNC_BY_LANGUAGE:
                	if (fileType != null && fileType.equalsIgnoreCase(TERMBASE_TBX)) {
                		stmts = batchAddTbxEntriesByLanguage(p_entries, p_options,
                                p_session);
                	} else {
                		stmts = batchAddEntriesByLanguage(p_entries, p_options,
                                p_session);
                	}
                    break;
            }

            if (stmts != null)
            {
                try
                {
                    executeStatements(stmts);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("batchAddEntries: processed "
                                + p_entries.size() + " entries");
                    }
                }
                catch (Throwable ignore)
                {
                    // TODO: This needs to be reimplemented. For now,
                    // the batch gets rolled back if a single entry
                    // fails.
                    CATEGORY
                            .error("Can't batch add entries, ignoring.", ignore);
                }
            }
        }
        finally
        {
            releaseReader();
        }
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
        String result = "";
        LockInfo myLock = makeLock(p_entryId, p_session);
        Long key = new Long(p_entryId);
        boolean b_acquired = false;

        synchronized (m_entryLocks)
        {
            LockInfo lock = (LockInfo) m_entryLocks.get(key);

            if (lock != null)
            {
                // locked, try to steal lock
                if (lock.isExpired() || p_steal
                        && canStealLock(lock, p_session))
                {
                    m_entryLocks.put(key, myLock);
                    b_acquired = true;
                }
            }
            else
            {
                // not locked, acquire lock
                m_entryLocks.put(key, myLock);
                b_acquired = true;
            }
        }

        if (b_acquired)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Locked entry " + p_entryId + " for user "
                        + p_session.getUserName());
            }

            result = myLock.asXML();
        }

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
        LockInfo lock;
        Long key = new Long(p_entryId);

        synchronized (m_entryLocks)
        {
            lock = (LockInfo) m_entryLocks.get(key);
        }

        if (lock != null)
        {
            LockInfo myLock = new LockInfo(p_cookie);

            if (myLock.equals(lock))
            {
                synchronized (m_entryLocks)
                {
                    m_entryLocks.remove(key);
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Unlocked entry " + p_entryId);
                }
            }
            else
            {
                throw new TermbaseException(MSG_YOU_DONT_OWN_LOCK, null, null);
            }
        }
        else
        {
            throw new TermbaseException(MSG_ENTRY_NOT_LOCKED, null, null);
        }
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
     * Example: How to update an entry:
     * 
     * <pre>
     * nEntry = 12345;
     * strCookie = TB.lockEntry(nEntry, false);
     * if (strCookie.length == 0)
     * {
     *     // entry locked, try stealing the lock...
     *     strCookie = TB.lockEntry(nEntry, true);
     * }
     * if (strCookie.length == 0)
     * {
     *     throw &quot;entry is locked by somebody else&quot;
     * }
     * 
     * try
     * {
     *     TB.updateEntry(nEntry, strNewEntry, strCookie)
     * }
     * finally
     * {
     *   TB.unlockEntry(nEntry, strCookie)
     * }
     * </pre>
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
            Entry oldEntry = new Entry(xml);
            Entry newEntry = new Entry(p_entry);

            // Minimize entry and check for presence of required fields.
            EntryUtils.normalizeEntry(newEntry, m_definition);

            // Save entry to database
            m_langs = new StringBuffer();
            m_details = new StringBuffer();
            updateEntryInDatabase(p_entryId, oldEntry, newEntry, m_definition,
                    p_session);

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

        addReader();

        try
        {
            Statements statements = getDeleteEntryStatements(p_entryId,
                    p_session);

            executeStatements(statements);

            CATEGORY.info("Entry " + p_entryId + " deleted.");
        }
        catch (TermbaseException ex)
        {
            CATEGORY.warn("Entry " + p_entryId + " could not be deleted.", ex);
            throw ex;
        }
        finally
        {
            releaseReader();
        }
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
        ValidationInfo result;
        Entry entry = new Entry(p_entry);

        // Minimize entry.
        EntryUtils.pruneEntry(entry);

        addReader();

        try
        {
            result = Validator.validate(entry, this);

            return result.asXML();
        }
        finally
        {
            releaseReader();
        }
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
     *            a search expression containing '?' for any single character
     *            '*' for any number of characters, '[a-z]' limited regular
     *            expressions as implemented by the SQL database - or - '#' to
     *            start a fuzzy search - or - '$' to start a full-text search -
     *            or - '!' to start an exact match search
     * 
     * @param maxhits:
     *            specifies how many hits should be retrieved. This can be only
     *            a hint and if homonyms ("homographs", really) appear, the
     *            system will return them all.
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
            String p_query, int p_maxHits)
            throws TermbaseException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search for `" + p_query + "' in " + p_language);
        }

        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return "<hitlist><hits></hits></hitlist>";
        }

        // safety check here
        if (p_maxHits > 100)
        {
            p_maxHits = 100;
        }

        addReader();

        try
        {
            Hitlist result;

            Definition.Language language = m_definition.getLanguage(p_language);
            
            Definition.Language targetLan = null;

            if (language == null)
            {
                String[] args = { "unknown language" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }
            
            if(target_language != null) {
                targetLan = m_definition.getLanguage(target_language);
            }

            String realQuery;

            // Fuzzy query starts with "#"
            if (p_query.indexOf('#') == 0)
            {
                realQuery = p_query.substring(1);

                result = searchFuzzy(language.getName(), language.getLocale(),
                        realQuery, p_maxHits);
            }
            // Full-text query starts with "$"
            else if (p_query.indexOf('$') == 0)
            {
                realQuery = p_query.substring(1);

                result = searchFulltext(language.getName(), language
                        .getLocale(), realQuery, p_maxHits);
            }
            // Exact match query starts with "!"
            else if (p_query.indexOf('!') == 0)
            {
                realQuery = p_query.substring(1);

                result = searchExact(language.getName(), language.getLocale(),
                        realQuery, p_maxHits);
            }
            // Regular expressions can use * and ?.
            else if (p_query.indexOf('*') != -1 || p_query.indexOf('?') != -1)
            {
                int i;

                try
                {
                    RE pattern = new RE("\\*");
                    realQuery = pattern.subst(p_query, "%", RE.REPLACE_ALL);
                    pattern = new RE("\\?");
                    realQuery = pattern.subst(realQuery, "_", RE.REPLACE_ALL);
                }
                catch (Throwable ignore)
                {
                    CATEGORY.error("pilot error in regexp", ignore);
                    realQuery = p_query;
                }

                result = searchRegexp(language.getName(), language.getLocale(),
                        realQuery, p_maxHits);
            }
            // For internal QA: access to the term leverager for testing
            else if (p_query.startsWith("@@@@"))
            {
                result = doTermLeveraging(language.getName(), language
                        .getLocale(), p_query.substring("@@@@".length()),
                        p_maxHits);
            }
            else
            {
                if(targetLan == null) {
                    result = searchIndex(language.getName(), language.getLocale(),
                        null, null,p_query, p_maxHits);
                }
                else {
                    result = searchIndex(language.getName(), language.getLocale(),
                            targetLan.getName(), targetLan.getLocale(),
                            p_query, p_maxHits);
                }
            }

            return result.getXml();
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Searches a given fuzzy index for the given expression.
     */
    public String fuzzySearch(String p_language, String p_query, int p_maxHits)
            throws TermbaseException
    {
        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return "<hitlist><hits></hits></hitlist>";
        }

        // safety check here
        if (p_maxHits > 100)
        {
            p_maxHits = 100;
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

            result = searchFuzzy(language.getName(), language.getLocale(),
                    p_query, p_maxHits);

            return result.getXml();
        }
        finally
        {
            releaseReader();
        }
    }

    /**
     * <p>
     * Alphabetically browses an index by returning the next <i>n</i> terms
     * greater (or smaller) than the start term.
     * </p>
     * 
     * @param language:
     *            a valid index name in the termbase
     * @param starting:
     *            position to start browsing. If the string is empty, browsing
     *            starts at the beginning or end of the index.
     * @param direction:
     *            browse up (0) or down (1)
     * @param maxHits:
     *            specifies how many hits should be retrieved. This can only be
     *            a hint. If homographs appear, the system will return them all.
     * 
     * @return a hitlist as xml string. See search(String,String,long).
     */
    public String browse(String p_language, String target_lan, String p_start, int p_direction,
            int p_maxHits) throws TermbaseException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Browse " + (p_direction == 0 ? "before" : "after")
                    + " `" + p_start + "' in " + p_language);
        }

        // shortcut if no results requested
        if (p_maxHits <= 0)
        {
            return "<hitlist><hits></hits></hitlist>";
        }

        // safety check here
        if (p_maxHits > 100)
        {
            p_maxHits = 100;
        }

        addReader();

        try
        {
            Hitlist result;

            Definition.Language language = m_definition.getLanguage(p_language);

            if (language == null)
            {
                String[] args = { "unknown language" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }
            
            Definition.Language target = null;
            
            if(target_lan != null) {
                target = m_definition.getLanguage(target_lan);
                result = browseIndex(language.getName(),  
                                     language.getLocale(),target.getName(),
                                     p_start, p_direction, p_maxHits);
            }
            else {
                result = browseIndex(language.getName(),  
                        language.getLocale(), null,
                        p_start, p_direction, p_maxHits);
            }

            return result.getXml();
        }
        finally
        {
            releaseReader();
        }
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

            result = searchFuzzy(language.getName(), language.getLocale(),
                    p_query, p_maxHits);

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
    public int getEntryCount(EntryFilter p_filter) throws SQLException,
            Exception
    {
        addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            int result = -1;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                String sql = "select count(*) from TB_CONCEPT c "
                        + "where c.TBid = " + m_id;

                if (p_filter != null && p_filter.isDbFiltering())
                {
                    sql += p_filter.getSqlExpression("c");

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("getEntryCount(filter): " + sql);
                    }
                }

                stmt = conn.createStatement();
                rset = stmt.executeQuery(sql);

                rset.next();
                result = rset.getInt(1);

                conn.commit();

                return result;
            }
            catch (/* SQL */Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Throwable ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
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
        return getTermCount(p_language, null);
    }

    /**
     * Returns the number of terms in the given language, optionally filtered by
     * an EntryFilter.
     * 
     * @see getStatistics()
     */
    public int getTermCount(String p_language, EntryFilter p_filter)
            throws TermbaseException
    {
        addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            int result = -1;

            Definition.Language language = m_definition.getLanguage(p_language);

            if (language == null)
            {
                String[] args = { "unknown language" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            try
            {
            	String sql = "select count(distinct(c.cid)) from TB_TERM a, TB_CONCEPT c "
                    + "where a.Cid = c.Cid and a.TBid = " + m_id + "  and a.Lang_Name = '"
                    + SqlUtil.quote(p_language) + "'";
                if (p_filter != null && p_filter.isDbFiltering())
                {
                    sql += p_filter.getSqlExpression("c");

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("getEntryCount(filter): " + sql);
                    }
                }
            	
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                stmt = conn.createStatement();
                rset = stmt.executeQuery(sql);

                rset.next();
                result = rset.getInt(1);

                conn.commit();

                return result;
            }
            catch (/* SQL */Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
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
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            int result = -1;

            Definition.Language language = m_definition.getLanguage(p_language);

            if (language == null)
            {
                String[] args = { "unknown language" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                stmt = conn.createStatement();
                rset = stmt.executeQuery("select count(LID) from TB_LANGUAGE "
                        + "where TBid = " + m_id + "  and Name = '"
                        + SqlUtil.quote(p_language) + "'"
                        + "  and not XML is null");

                rset.next();
                result = rset.getInt(1);

                conn.commit();

                return result;
            }
            catch (/* SQL */Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
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
        addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            ArrayList result = new ArrayList();

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                String sql = "select Cid from TB_CONCEPT c " + "where TBid = "
                        + m_id;

                if (p_filter != null && p_filter.isDbFiltering())
                {
                    sql += p_filter.getSqlExpression("c");

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("getEntryIds(filter): " + sql);
                    }
                }

                stmt = conn.createStatement();
                rset = stmt.executeQuery(sql);

                while (rset.next())
                {
                    result.add(new Long(rset.getLong(1)));
                }

                conn.commit();

                return result;
            }
            catch (/* SQL */Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            releaseReader();
        }
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

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            ArrayList result = new ArrayList();

            Definition.Language language = m_definition.getLanguage(p_language);

            if (language == null)
            {
                String[] args = { "unknown language" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                String sql;

                if (p_filter != null && p_filter.isDbFiltering())
                {
                    // TODO: optimize this join (well, it's not too bad on 30k).
                    sql = "select distinct t.Cid "
                            + "from TB_TERM t, TB_CONCEPT c " + "where t.TBid="
                            + m_id + "  and t.Lang_Name='"
                            + SqlUtil.quote(p_language) + "'"
                            + "  and t.cid = c.cid";
                    sql += p_filter.getSqlExpression("c");

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("getEntryIds(lang, filter): " + sql);
                    }
                }
                else
                {
                    // Much better without join.
                    sql = "select distinct Cid from TB_TERM " + "where TBid="
                            + m_id + "  and Lang_Name='"
                            + SqlUtil.quote(p_language) + "'";
                }

                stmt = conn.createStatement();
                rset = stmt.executeQuery(sql);

                while (rset.next())
                {
                    result.add(new Long(rset.getLong(1)));
                }

                conn.commit();

                return result;
            }
            catch (/* SQL */Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            releaseReader();
        }
    }

    //
    // Private Methods
    //

    /**
     * <p>
     * Adds a new entry to the database.
     * </p>
     */
    private long addEntryToDatabase(Entry p_entry, Definition p_definition,
            SessionInfo p_session) throws TermbaseException
    { 
        Statements statements = getAddEntryStatements(p_entry, p_definition,
                p_session);

        executeStatements(statements);

        return p_entry.getId();
    }

    private Statements getAddTbxEntryStatements(Entry p_entry,
            Definition p_definition, SessionInfo p_session)
            throws TermbaseException {
    	Statements result = new Statements();
        String statement;
//        String value;
        
        int numCids = 1;
        int numLids, numTids, i, max;

        long[] cids = new long[numCids];
        long[] lids;
        long[] tids;
        
        Element root = p_entry.getDom().getRootElement();
        
        
     // Count languages and terms and reserve ids
        List langSets = root.selectNodes("langSet");
        numLids = langSets.size();
        lids = new long[numLids];
        
        List ntigs = root.selectNodes("langSet/ntig/termGrp");
        List tigs = root.selectNodes("langSet/tig");
        numTids = ntigs.size() + tigs.size();
        tids = new long[numTids];
        
     // Allocate new ids for the table rows
        allocateIds(cids, lids, tids);

        // Update <concept> in entry
        p_entry.setId(cids[0]);
        EntryUtils.setConceptId(p_entry, cids[0]);

        // Update creation timestamp in entry (this is ADD)
        p_session.setTimestamp();
        EntryUtils.setCreationTimeStamp(p_entry, p_session);

        // Have to refresh root variable after all those changes
        root = p_entry.getDom().getRootElement();
        
        {
	        String domain = "*unknown*";
	        String project = "*unknown*";
	        String status = "proposed";
	        StringBuffer xml = new StringBuffer();
	
	        // Prepare concept nodes for storage
	        for (Iterator it = root.elementIterator(); it.hasNext();)
	        {
	            Element elmt = (Element) it.next();
	
	            if (!elmt.getName().equals("langSet"))
	            {
	            	String tmp = elmt.asXML();
	            	tmp = tmp.replace("\\", "\\\\");
	                xml.append(tmp);
	            }
	        }
	
	        String cxml = xml.toString();
	        boolean needClob = false;
	
	        statement = "insert into TB_CONCEPT "
	                + " (TBId, Cid, Domain, Status, Project, XML, "
	                + " Created_On, Created_By)" + " values (" + m_id + ","
	                + cids[0] + "," + "'" + SqlUtil.quote(domain) + "'," + "'"
	                + SqlUtil.quote(status) + "'," + "'"
	                + SqlUtil.quote(project) + "',"
	                + SqlUtil.getClobInitializer(cxml, needClob) + "," + "'"
	                + UTC.valueOf(p_session.getTimestamp()) + "'," + "'"
	                + SqlUtil.quote(p_session.getUserName()) + "')";
	
	        result.addConceptStatement(statement);
        }
        
     // produce language-level statements
        int cntLids = 0, cntTids = 0;
        for (i = 0, max = langSets.size(); i < max; ++i, ++cntLids)
        {
            Element langSet = (Element) langSets.get(i);

            String langLocale = langSet.attribute("lang").getText();
            String langName = EntryUtils.getLanguageName(langLocale);
            StringBuffer xml = new StringBuffer();

            // Prepare language nodes for storage
            for (Iterator it = langSet.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("ntig") && !elmt.getName().equals("tig"))
                {
                	String tmp = elmt.asXML();
	            	tmp = tmp.replace("\\", "\\\\");
                    xml.append(tmp);
                }
            }

            String lxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(lxml) > 4000;
            boolean needClob = false;

            statement = "insert into TB_LANGUAGE "
                    + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                    + m_id + "," + lids[cntLids] + "," + cids[0] + "," + "'"
                    + langName + "'," + "'" + langLocale + "',"
                    + SqlUtil.getClobInitializer(lxml, needClob) + ")";

            result.addLanguageStatement(statement);

            // produce term-level statements for all terms in this language
            List termGrps = langSet.selectNodes("ntig/termGrp");
            for (Iterator it1 = termGrps.iterator(); it1.hasNext(); ++cntTids)
            {
                Element termGrp = (Element) it1.next();

                String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();

                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);

                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);

                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                    	String tmp = elmt.asXML();
                    	tmp = tmp.replace("\\", "\\\\");
                        xml1.append(tmp);
                    }
                }

                String txml = xml1.toString();
                boolean needClob1 = false;

                statement = "insert into TB_TERM "
                        + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                        + " Type, Status, Sort_Key, XML)" + " values ("
                        + m_id + "," + cids[0] + "," + lids[cntLids]
                        + "," + tids[cntTids] + ",'" + SqlUtil.quote(langName)
                        + "','" + SqlUtil.quote(term) + "','" + SqlUtil.quote(termType)
                        + "','" + SqlUtil.quote(termStatus) + "','"
                        + sortKey + "'," + SqlUtil.getClobInitializer(txml, needClob1)
                        + ")";

                result.addTermStatement(statement);
            }
            
            termGrps = langSet.selectNodes("tig");
            for (Iterator it1 = termGrps.iterator(); it1.hasNext(); ++cntTids)
            {
                Element termGrp = (Element) it1.next();

                String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();

                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);

                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);

                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                    	String tmp = elmt.asXML();
                    	tmp = tmp.replace("\\", "\\\\");
                        xml1.append(tmp);
                    }
                }

                String txml = xml1.toString();
                boolean needClob1 = false;

                statement = "insert into TB_TERM "
                        + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                        + " Type, Status, Sort_Key, XML)" + " values ("
                        + m_id + "," + cids[0] + "," + lids[cntLids]
                        + "," + tids[cntTids] + ",'" + SqlUtil.quote(langName)
                        + "','" + SqlUtil.quote(term) + "','" + SqlUtil.quote(termType)
                        + "','" + SqlUtil.quote(termStatus) + "','"
                        + sortKey + "'," + SqlUtil.getClobInitializer(txml, needClob1)
                        + ")";

                result.addTermStatement(statement);
            }
        }
        
        
        return result;
    }
    /**
     * <p>
     * Produces SQL statements to store a single entry in the database.
     * </p>
     */
    private Statements getAddEntryStatements(Entry p_entry,
            Definition p_definition, SessionInfo p_session)
            throws TermbaseException
    {
        Statements result = new Statements();
        String statement;
        String value;

        int numCids = 1;
        int numLids, numTids, i, max;

        long[] cids = new long[numCids];
        long[] lids;
        long[] tids;

        Element root = p_entry.getDom().getRootElement();

        // Count languages and terms and reserve ids
        List langGrps = root.selectNodes("/conceptGrp/languageGrp");
        numLids = langGrps.size();
        lids = new long[numLids];

        List termGrps = root.selectNodes("/conceptGrp/languageGrp/termGrp");
        numTids = termGrps.size();
        tids = new long[numTids];

        // Allocate new ids for the table rows
        allocateIds(cids, lids, tids);

        // Update <concept> in entry
        p_entry.setId(cids[0]);
        EntryUtils.setConceptId(p_entry, cids[0]);

        // Update creation timestamp in entry (this is ADD)
        p_session.setTimestamp();
        EntryUtils.setCreationTimeStamp(p_entry, p_session);

        // Have to refresh root variable after all those changes
        root = p_entry.getDom().getRootElement();

        // CATEGORY.debug("ENTRY=" + root.asXML());

        // produce concept-level statement
        {
            String domain = "*unknown*";
            String project = "*unknown*";
            String status = "proposed";
            StringBuffer xml = new StringBuffer();

            // Extract values of indexed concept attributes.
            // TODO: this has to use the TbDefinition.
            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }

            // Prepare concept nodes for storage
            for (Iterator it = root.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("languageGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String cxml = xml.toString();
            boolean needClob = false;

            statement = "insert into TB_CONCEPT "
                    + " (TBId, Cid, Domain, Status, Project, XML, "
                    + " Created_On, Created_By)" + " values (" + m_id + ","
                    + cids[0] + "," + "'" + SqlUtil.quote(domain) + "'," + "'"
                    + SqlUtil.quote(status) + "'," + "'"
                    + SqlUtil.quote(project) + "',"
                    + SqlUtil.getClobInitializer(cxml, needClob) + "," + "'"
                    + UTC.valueOf(p_session.getTimestamp()) + "'," + "'"
                    + SqlUtil.quote(p_session.getUserName()) + "')";

            result.addConceptStatement(statement);
        }
        
        // produce language-level statements
        int cntLids = 0, cntTids = 0;
        for (i = 0, max = langGrps.size(); i < max; ++i, ++cntLids)
        {
            Element langGrp = (Element) langGrps.get(i);

            String langName = langGrp.valueOf("language/@name");
            String langLocale = langGrp.valueOf("language/@locale");
            StringBuffer xml = new StringBuffer();

            // Prepare language nodes for storage
            for (Iterator it = langGrp.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("language")
                        && !elmt.getName().equals("termGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String lxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(lxml) > 4000;
            boolean needClob = false;

            statement = "insert into TB_LANGUAGE "
                    + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                    + m_id + "," + lids[cntLids] + "," + cids[0] + "," + "'"
                    + langName + "'," + "'" + langLocale + "',"
                    + SqlUtil.getClobInitializer(lxml, needClob) + ")";

            result.addLanguageStatement(statement);

            // produce term-level statements for all terms in this language
            termGrps = langGrp.selectNodes("termGrp");
            for (Iterator it1 = termGrps.iterator(); it1.hasNext(); ++cntTids)
            {
                Element termGrp = (Element) it1.next();

                String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();

                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);

                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);

                // Extract term and values of indexed term attributes
                if ((value = termGrp.valueOf(".//descrip[@type='type']")) != null
                        && value.length() > 0)
                {
                    termType = value;
                }

                if ((value = termGrp.valueOf(".//descrip[@type='status']")) != null
                        && value.length() > 0)
                {
                    termStatus = value;
                }

                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                        xml1.append(elmt.asXML());
                    }
                }

                String txml = xml1.toString();
                boolean needClob1 = false;

                statement = "insert into TB_TERM "
                        + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                        + " Type, Status, Sort_Key, XML)" + " values ("
                        + m_id
                        + ","
                        + cids[0]
                        + ","
                        + lids[cntLids]
                        + ","
                        + tids[cntTids]
                        + ","
                        + "'"
                        + SqlUtil.quote(langName)
                        + "',"
                        + "'"
                        + SqlUtil.quote(term)
                        + "',"
                        + "'"
                        + SqlUtil.quote(termType)
                        + "',"
                        + "'"
                        + SqlUtil.quote(termStatus)
                        + "',"
                        + "'"
                        + sortKey
                        + "',"
                        + SqlUtil.getClobInitializer(txml, needClob1)
                        + ")";

                result.addTermStatement(statement);
            }
        }

        return result;
    }

    /**
     * Allocates new ids for concepts, languages and terms.
     */
    public void allocateIds(long[] p_cids, long[] p_lids, long[] p_tids)
            throws TermbaseException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        int numCids = p_cids.length;
        int numLids = p_lids.length;
        int numTids = p_tids.length;

        long cid = 0, lid = 0, tid = 0;
        int i;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select NAME, VALUE from TB_SEQUENCE "
                    + "where NAME='cid' or NAME='lid' or NAME='tid' "
                    + "FOR UPDATE");

            while (rset.next())
            {
                String name = rset.getString(1);
                if (name.equalsIgnoreCase("cid"))
                {
                    cid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("lid"))
                {
                    lid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("tid"))
                {
                    tid = rset.getLong(2);
                }
            }

            for (i = 0; i < numCids; ++i)
            {
                p_cids[i] = cid + i;
            }
            for (i = 0; i < numLids; ++i)
            {
                p_lids[i] = lid + i;
            }
            for (i = 0; i < numTids; ++i)
            {
                p_tids[i] = tid + i;
            }

            stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + (cid + numCids)
                    + " WHERE NAME='cid'");
            stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + (lid + numLids)
                    + " WHERE NAME='lid'");
            stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + (tid + numTids)
                    + " WHERE NAME='tid'");
            stmt.executeBatch();

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }
    
    /**
     * Allocates new ids for languages and terms.
     */
    public void allocateIds(long[] p_lids, long[] p_tids) throws TermbaseException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        
        int numLids = p_lids.length;
        int numTids = p_tids.length;
        
        long cid = 0, lid = 0, tid = 0;
        int i;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
        
            stmt = conn.createStatement();
            rset = stmt.executeQuery("select NAME, VALUE from TB_SEQUENCE "
                    + "where NAME='cid' or NAME='lid' or NAME='tid' "
                    + "FOR UPDATE");
        
            while (rset.next())
            {
                String name = rset.getString(1);
                if (name.equalsIgnoreCase("cid"))
                {
                    cid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("lid"))
                {
                    lid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("tid"))
                {
                    tid = rset.getLong(2);
                }
            }

            for (i = 0; i < numLids; ++i)
            {
                p_lids[i] = lid + i;
            }
            for (i = 0; i < numTids; ++i)
            {
                p_tids[i] = tid + i;
            }

            stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + (lid + numLids)
                    + " WHERE NAME='lid'");
            stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + (tid + numTids)
                    + " WHERE NAME='tid'");
            stmt.executeBatch();
        
            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }
        
            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Allocates new ids for concepts, languages and terms.
     */
    public void allocateIds(ArrayList p_lids, int p_neededLids,
            ArrayList p_tids, int p_neededTids) throws TermbaseException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        long lid = 0, tid = 0;
        int i;

        if (p_neededLids <= 0 && p_neededTids <= 0)
        {
            return;
        }

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select NAME, VALUE from TB_SEQUENCE "
                    + "where NAME='lid' or NAME='tid' " + "FOR UPDATE");

            while (rset.next())
            {
                String name = rset.getString(1);
                if (name.equalsIgnoreCase("lid"))
                {
                    lid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("tid"))
                {
                    tid = rset.getLong(2);
                }
            }

            for (i = 0; i < p_neededLids; ++i)
            {
                p_lids.add(new Long(lid + i));
            }
            for (i = 0; i < p_neededTids; ++i)
            {
                p_tids.add(new Long(tid + i));
            }

            if (p_neededLids > 0)
            {
                stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE="
                        + (lid + p_neededLids) + " WHERE NAME='lid'");
            }
            if (p_neededTids > 0)
            {
                stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE="
                        + (tid + p_neededTids) + " WHERE NAME='tid'");
            }

            stmt.executeBatch();

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("allocateIds", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Get max cid,lid,tid in database.
     */
    public Map<String, Long> getMaxIDS()
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        Map<String, Long> result = new HashMap<String, Long>();

        long cid = 0, lid = 0, tid = 0;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);        
            stmt = conn.createStatement();
            
            rset = stmt.executeQuery("select NAME, VALUE from TB_SEQUENCE "
                    + "where NAME='cid' or NAME='lid' or NAME='tid' "
                    + "FOR UPDATE");

            while (rset.next())
            {
                String name = rset.getString(1);
                if (name.equalsIgnoreCase("cid"))
                {
                    cid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("lid"))
                {
                    lid = rset.getLong(2);
                }
                else if (name.equalsIgnoreCase("tid"))
                {
                    tid = rset.getLong(2);
                }
            }
            
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("getMaxIDS", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            result.put("cid", Long.valueOf(cid));
            result.put("lid", Long.valueOf(lid));
            result.put("tid", Long.valueOf(tid));
            
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
            
            return result;
        }

    }

    private void setMaxIDS(long p_cid, long p_lid, long p_tid)
    {
        Connection conn = null;
        Statement stmt = null;

        long cid = 0, lid = 0, tid = 0;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);        
            stmt = conn.createStatement();
            
            if (p_cid > 0)
            {
                stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + p_cid
                        + " WHERE NAME='cid'");
            }
            if (p_lid > 0)
            {
                stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + p_lid
                        + " WHERE NAME='lid'");
            }
            if (p_tid > 0)
            {
                stmt.addBatch("UPDATE TB_SEQUENCE SET VALUE=" + p_tid
                        + " WHERE NAME='tid'");
            }

            stmt.executeBatch();

            conn.commit();

        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("setMaxIDS", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

    }
    
    /**
     * Fetch the existing IDs (lid + tid) used for an entry.
     */
    private void getExistingIds(long p_entryId, ArrayList p_lids,
            ArrayList p_tids) throws TermbaseException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        long lid = 0, tid = 0;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select LID from TB_LANGUAGE "
                    + "where TBID=" + m_id + " and CID=" + p_entryId);

            while (rset.next())
            {
                p_lids.add(new Long(rset.getLong(1)));
            }

            rset.close();

            rset = stmt.executeQuery("select TID from TB_TERM " + "where TBID="
                    + m_id + " and CID=" + p_entryId);

            while (rset.next())
            {
                p_tids.add(new Long(rset.getLong(1)));
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("getExistingIds", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Fetch the existing IDs (lid + tid)and language/term list 
     * used for a list of entry IDs.
     */
    /* private */void getMultipleExistingIds(ArrayList p_entryIds,
            ArrayList p_tb_language_lids, ArrayList p_tb_langs,
            ArrayList p_tb_term_tids, ArrayList p_tb_terms) 
            throws TermbaseException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        long lid = 0, tid = 0;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            String inlist = SqlUtil.getInList(p_entryIds);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select LID, CID, NAME, LOCALE from TB_LANGUAGE "
                    + "where TBID=" + m_id + " and CID in " + inlist);

            while (rset.next())
            {
                p_tb_language_lids.add(new Long(rset.getLong(1)));

                Language tbLang = new Language(rset.getInt(1), rset.getInt(2),
                                          rset.getString(3), rset.getString(4));
                tbLang.setTbid((int)m_id);
                p_tb_langs.add(tbLang);
            }

            rset.close();

            rset = stmt.executeQuery("select TID, LID, CID from TB_TERM " 
                    + "where TBID=" + m_id + " and CID in " + inlist);

            while (rset.next())
            {
                p_tb_term_tids.add(new Long(rset.getLong(1)));
                
                Term term = new Term(rset.getInt(1), rset.getInt(2), rset.getInt(3));
                term.setTbid((int)m_id);
                p_tb_terms.add(term);
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("getMultipleExistingIds", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }
    
    /**
     * Check which of the CIds passed in actually exist in the database.
     */
    ArrayList getExistingTbxCids(ArrayList p_cids) throws TermbaseException {
    	ArrayList result = new ArrayList();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        long cid = 0;
        int i;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select CID from TB_CONCEPT "
                    + "where TBID=" + m_id + " and CID in "
                    + SqlUtil.getInList(p_cids));

            while (rset.next())
            {
                result.add(rset.getString(1));
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("getExistingCids", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * Check which of the CIds passed in actually exist in the database.
     */
    /* private */ArrayList getExistingCids(ArrayList p_cids)
            throws TermbaseException
    {
        ArrayList result = new ArrayList();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        long cid = 0;
        int i;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("select CID from TB_CONCEPT "
                    + "where TBID=" + m_id + " and CID in "
                    + SqlUtil.getInList(p_cids));

            while (rset.next())
            {
                result.add(new Long(rset.getLong(1)));
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            CATEGORY.error("getExistingCids", e);

            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * <p>
     * Updates an entry in the database.
     * </p>
     */
    private void updateEntryInDatabase(long p_entryId, Entry p_oldEntry,
            Entry p_newEntry, Definition p_definition, SessionInfo p_session)
            throws TermbaseException
    {
        Statements statements = getUpdateEntryStatements(p_entryId, p_oldEntry,
                p_newEntry, p_definition, p_session);

        executeStatements(statements);
    }

    /**
     * <p>
     * Produces SQL statements to update a single entry in the database.
     * </p>
     */
    private Statements getUpdateEntryStatements(long p_entryId,
            Entry p_oldEntry, Entry p_newEntry, Definition p_definition,
            SessionInfo p_session) throws TermbaseException
    {
        //
        // TODO: Diff old and new entry
        // find what needs to be removed from old entry (langs, terms)
        // find what needs to be added to new entry (langs, terms)
        // establish what needs to be updated in new entry (XML)
        //

        Statements result = new Statements();
        String statement;
        String value;

        long cid = p_entryId;
        Long lid, tid;
        int neededLids, existingLids, neededTids, existingTids, i;
        ArrayList lids = new ArrayList();
        ArrayList tids = new ArrayList();
        Writer writer = new Writer(this);

        // fetch ids already used in entry
        getExistingIds(cid, lids, tids);

        existingLids = lids.size();
        existingTids = tids.size();

        Element root = p_newEntry.getDom().getRootElement();

        // Count languages and terms and reserve missing ids
        List langGrps = root.selectNodes("/conceptGrp/languageGrp");
        neededLids = langGrps.size();
        neededLids -= lids.size();

        List termGrps = root.selectNodes("/conceptGrp/languageGrp/termGrp");
        neededTids = termGrps.size();
        neededTids -= tids.size();

        // Allocate any needed new ids for the table rows
        allocateIds(lids, neededLids, tids, neededTids);

        // Update <concept> in entry (just for safety)
        p_newEntry.setId(cid);
        EntryUtils.setConceptId(p_newEntry, cid);

        // Update modification timestamp in entry (this is UPDATE)
        p_session.setTimestamp();
        EntryUtils.setModificationTimeStamp(p_newEntry, p_session);

        // Have to refresh root variable after all those changes
        root = p_newEntry.getDom().getRootElement();

        // CATEGORY.debug("ENTRY=" + root.asXML());

        /*
         * // delete all index rows, will re-index all new terms later {
         * statement = "delete from TB_FUZZY_INDEX " + "where tbid=" + m_id + "
         * and cid=" + cid;
         * 
         * result.addTokenStatement(statement); }
         */

        // produce concept-level statement
        {
            String domain = "*unknown*";
            String project = "*unknown*";
            String status = "proposed";
            StringBuffer xml = new StringBuffer();

            // Extract values of indexed concept attributes.
            // TODO: this has to use the TbDefinition.
            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }

            // Prepare concept nodes for storage
            for (Iterator it = root.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("languageGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String cxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(cxml) > 4000;
            boolean needClob = false;

            statement = "update TB_CONCEPT " + "set Domain='"
                    + SqlUtil.quote(domain) + "'," + "    Status='"
                    + SqlUtil.quote(status) + "'," + "    Project='"
                    + SqlUtil.quote(project) + "'," + "    XML="
                    + SqlUtil.getClobInitializer(cxml, needClob) + ","
                    + "    Modified_On='"
                    + UTC.valueOf(p_session.getTimestamp()) + "',"
                    + "    Modified_By='"
                    + SqlUtil.quote(p_session.getUserName()) + "' "
                    + " where tbid=" + m_id + "   and cid=" + cid;

            result.addConceptStatement(statement);
        }

        // produce language-level statements
        for (i = 0; i < langGrps.size(); ++i)
        {
            Element langGrp = (Element) langGrps.get(i);

            String langName = langGrp.valueOf("language/@name");
            String langLocale = langGrp.valueOf("language/@locale");
            m_langs.append(langName);
            m_langs.append(",");
            StringBuffer xml = new StringBuffer();

            // Prepare language nodes for storage
            for (Iterator it = langGrp.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("language")
                        && !elmt.getName().equals("termGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String lxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(lxml) > 4000;
            boolean needClob = false;

            if (existingLids > 0)
            {
                --existingLids;

                lid = (Long) lids.remove(0);

                // in-place update of the language info
                statement = "update TB_LANGUAGE " + "set Name='" + langName
                        + "'," + "    Locale='" + langLocale + "',"
                        + "    Xml="
                        + SqlUtil.getClobInitializer(lxml, needClob)
                        + " where tbid=" + m_id + " and cid=" + cid
                        + " and lid=" + lid;
                /*
                try{
                    writer.startBatchIndexing(getFulltextIndex(langName));
                    IndexObject p_object = new IndexObject();
                    p_object.m_cid = cid;
                    p_object.m_tid = lid;
                    p_object.m_text = lxml;
                    writer.modifyIndexXml(p_object);
                    writer.endIndex();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                */
            }
            else
            {
                lid = (Long) lids.remove(0);

                statement = "insert into TB_LANGUAGE "
                        + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                        + m_id + "," + lid + "," + cid + "," + "'" + langName
                        + "'," + "'" + langLocale + "',"
                        + SqlUtil.getClobInitializer(lxml, needClob) + ")";
            }

            result.addLanguageStatement(statement);

            // if (needClob)
            // {
            // statement = "select XML from TB_LANGUAGE " +
            // "where tbid=" + m_id +
            // " and lid=" + lid + " FOR UPDATE";
            //
            // result.addClobStatement(statement, lxml);
            // }

            /*
             * // Prepare a tokenizer for all terms in this language ITokenizer
             * tokenizer = TokenizerFactory.makeTokenizer(
             * LocaleCreater.makeLocale(langLocale));
             */

            // produce term-level statements for all terms in this language
            termGrps = langGrp.selectNodes("termGrp");
            for (Iterator it1 = termGrps.iterator(); it1.hasNext();)
            {
                Element termGrp = (Element) it1.next();

                String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();

                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);

                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);

                // Extract term and values of indexed term attributes
                if ((value = termGrp.valueOf(".//descrip[@type='type']")) != null
                        && value.length() > 0)
                {
                    termType = value;
                }

                if ((value = termGrp.valueOf(".//descrip[@type='status']")) != null
                        && value.length() > 0)
                {
                    termStatus = value;
                }

                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                        xml1.append(elmt.asXML());
                    }
                }

                String txml = xml1.toString();
                // boolean needClob1 = EditUtil.getUTF8Len(txml) > 4000;
                boolean needClob1 = false;

                if (existingTids > 0)
                {
                    --existingTids;
                    m_details.append("Updated '").append(term).append("' for ")
                            .append(langName).append(". ");
                    tid = (Long) tids.remove(0);

                    statement = "update TB_TERM " + " set lid=" + lid + ","
                            + " Lang_Name='" + SqlUtil.quote(langName) + "',"
                            + " Term='" + SqlUtil.quote(term) + "',"
                            + " Type='" + SqlUtil.quote(termType) + "',"
                            + " Status='" + SqlUtil.quote(termStatus) + "',"
                            + " Sort_Key='" + sortKey + "'," + " XML="
                            + SqlUtil.getClobInitializer(txml, needClob1)
                            + " where tbid=" + m_id + " and cid=" + cid
                            + " and tid=" + tid;
                    try{
                        try{
                            Index fullTextIndex = getFulltextIndex(langName);
                            fullTextIndex.deleteDocument(cid, tid);
                            writer.startBatchIndexing(fullTextIndex);
                            IndexObject p_object = new IndexObject();
                            p_object.m_cid = cid;
                            p_object.m_tid = tid;
                            p_object.m_text = txml;
                            ArrayList array = new ArrayList();
                            writer.indexXml(p_object, array);
                            writer.endIndex();
                            
                            Index fuzzIndex = getFuzzyIndex(langName);
                            fuzzIndex.deleteDocument(cid, tid);
                            writer.startBatchIndexing(fuzzIndex);
                            IndexObject p_object2 = new IndexObject();
                            p_object2.m_cid = cid;
                            p_object2.m_tid = tid;
                            p_object2.m_text = term;
                            writer.indexTerm(p_object2);
                            writer.endIndex();
                            
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                        finally{
                            writer.endIndex();
                        }
                    }
                    catch(Exception e) {}

                }
                else
                {
                    m_details.append("Added '").append(term).append("' for ")
                            .append(langName).append(". ");
                    tid = (Long) tids.remove(0);

                    statement = "insert into TB_TERM "
                            + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                            + " Type, Status, Sort_Key, XML)" + " values ("
                            + m_id
                            + ","
                            + cid
                            + ","
                            + lid
                            + ","
                            + tid
                            + ","
                            + "'"
                            + SqlUtil.quote(langName)
                            + "',"
                            + "'"
                            + SqlUtil.quote(term)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termType)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termStatus)
                            + "',"
                            + "'"
                            + sortKey
                            + "',"
                            + SqlUtil.getClobInitializer(txml, needClob1) + ")";
                }

                result.addTermStatement(statement);

                // if (needClob1)
                // {
                // statement = "select XML from TB_TERM " +
                // "where tbid=" + m_id +
                // " and tid=" + tid + " FOR UPDATE";
                //
                // result.addClobStatement(statement, txml);
                // }

                /*
                 * // prepare fuzzy tokens Collection tokens =
                 * tokenizer.tokenize(term);
                 * 
                 * for (Iterator it = tokens.iterator(); it.hasNext(); ) { Token
                 * token = (Token)it.next();
                 * 
                 * statement = "insert into TB_FUZZY_INDEX " + "(TBid, Cid, Tid,
                 * Lang_Name, Lang_Feature, " + " Weight, Feature_Count) " +
                 * "VALUES (" + m_id + "," + cid + "," + tid + "," + "'" +
                 * SqlUtil.quote(langName) + "'," + "'" +
                 * SqlUtil.quote(token.getToken()) + "'," + "" +
                 * token.getWeight() + "," + "" + tokens.size() + ")";
                 * 
                 * result.addTokenStatement(statement); } // Have to return
                 * tokens to their pool TokenPool.freeInstances(tokens);
                 */
            }
        }

        // remove any unused language and term rows
        while (lids.size() > 0)
        {
            lid = (Long) lids.remove(0);
            statement = "delete from tb_language " + "where tbid=" + m_id
                    + " and lid=" + lid;

            result.addLanguageStatement(statement);
        }

        while (tids.size() > 0)
        {
            tid = (Long) tids.remove(0);

            statement = "delete from tb_term " + "where tbid=" + m_id
                    + " and tid=" + tid;

            result.addTermStatement(statement);
        }

        return result;
    }

    
    Statements getOverwriteTbxEntriesStatements(TreeMap p_entries,
            SessionInfo p_session) throws TermbaseException
    {
    	Statements result = new Statements();
        Statements stmts;
        String statement;
        
        // Allocate all IDs we need.
        ArrayList cids = new ArrayList(p_entries.keySet());
        Long lid, tid;
        int neededLids = 0, neededTids = 0;
        IntHolder existingLids, existingTids;
        ArrayList tb_language_lids = new ArrayList();
        ArrayList tb_langs = new ArrayList();
        ArrayList tb_term_tids = new ArrayList();
        ArrayList tb_terms = new ArrayList();
        
        // Fetch language/term already used in entries.
        getMultipleExistingIds(cids, tb_language_lids, tb_langs,
                               tb_term_tids, tb_terms);
        
        // Now we're ready to collect the statements for all entries.
        for (int i = 0, max = cids.size(); i < max; i++)
        {
        	Object cid = null;
        	if (cids.get(i) instanceof Long) {
        		cid = (Long) cids.get(i);
        	} else if (cids.get(i) instanceof String) {
        		cid = (String) cids.get(i);
        	}
            
            Entry entry = (Entry) p_entries.get(cid);

            stmts = getOverwriteTbxEntryStatements(entry, String.valueOf(cid), 
                                        tb_language_lids, tb_langs, 
                                        tb_term_tids, tb_terms, 
                                        p_session);

            result.addAll(stmts);
        }

        return result;
    }
    /**
     * <p>
     * Produces SQL statements to overwrite a number of entries in the database.
     * The incoming Map is indexed by cid-&gt;entry.
     * </p>
     */
    /* private */Statements getOverwriteEntriesStatements(TreeMap p_entries,
            SessionInfo p_session) throws TermbaseException
    {
        Statements result = new Statements();
        Statements stmts;
        String statement;

        // Allocate all IDs we need.
        ArrayList cids = new ArrayList(p_entries.keySet());
        Long lid, tid;
        int neededLids = 0, neededTids = 0;
        IntHolder existingLids, existingTids;
        ArrayList tb_language_lids = new ArrayList();
        ArrayList tb_langs = new ArrayList();
        ArrayList tb_term_tids = new ArrayList();
        ArrayList tb_terms = new ArrayList();

        // Fetch language/term already used in entries.
        getMultipleExistingIds(cids, tb_language_lids, tb_langs,
                               tb_term_tids, tb_terms);

        // Now we're ready to collect the statements for all entries.
        for (int i = 0, max = cids.size(); i < max; i++)
        {
            Long cid = (Long) cids.get(i);
            Entry entry = (Entry) p_entries.get(cid);

            stmts = getOverwriteEntryStatements(entry, cid.longValue(), 
                       tb_language_lids, tb_langs,
                       tb_term_tids, tb_terms,
                       p_session);

            result.addAll(stmts);
        }

        return result;
    }

    /**
     * The implementation is different from updating a single entry because lids
     * and tids are shuffled across different cids.
     */
    private Statements getOverwriteTbxEntryStatements(Entry p_entry, String p_cid,
            ArrayList p_tb_language_lids, ArrayList p_tb_langs,
            ArrayList p_tb_term_tids, ArrayList p_tb_terms, SessionInfo p_session)
            throws TermbaseException
    {
    	Statements result = new Statements();
    	
    	long maxLID, maxTID;
        Map<String, Long> mapIDS = getMaxIDS();
        maxLID = mapIDS.get("lid");
        maxTID = mapIDS.get("tid");
        
    	Long lid, tid;
        String statement;
        
        // Update <concept> in entry (just for safety)
        p_entry.setId(Long.valueOf(p_cid));
        EntryUtils.setConceptId(p_entry, Long.valueOf(p_cid));

        // Update modification timestamp in entry (this is UPDATE)
        p_session.setTimestamp();
        EntryUtils.setModificationTimeStamp(p_entry, p_session);
        
        Element root = p_entry.getDom().getRootElement();
        {
        	String domain = "*unknown*";
            String project = "*unknown*";
            String status = "proposed";
            StringBuffer xml = new StringBuffer();
            
	        // Prepare concept nodes for storage
	        for (Iterator it = root.elementIterator(); it.hasNext();)
	        {
	            Element elmt = (Element) it.next();
	
	            if (!elmt.getName().equals("langSet"))
	            {
	                xml.append(elmt.asXML());
	            }
	        }
	        String cxml = xml.toString();
	        
	        boolean needClob = false;

            statement = "update TB_CONCEPT " + "set Domain='"
                    + SqlUtil.quote(domain) + "'," + "    Status='"
                    + SqlUtil.quote(status) + "'," + "    Project='"
                    + SqlUtil.quote(project) + "'," + "    XML="
                    + SqlUtil.getClobInitializer(cxml, needClob) + ","
                    + "    Modified_On='"
                    + UTC.valueOf(p_session.getTimestamp()) + "',"
                    + "    Modified_By='"
                    + SqlUtil.quote(p_session.getUserName()) + "' "
                    + " where tbid=" + m_id + "   and cid=" + p_cid;

            result.addConceptStatement(statement);
        }
        
        // produce language-level statements
        List langSets = root.selectNodes("langSet");
        for (int i = 0, max = langSets.size(); i < max; ++i)
        {
        	Element langSet = (Element) langSets.get(i);
        	String langLocale = langSet.attribute("lang").getText();
        	String langName = EntryUtils.getLanguageName(langLocale);
        	StringBuffer xml = new StringBuffer();
        	
        	for (Iterator it = langSet.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("ntig") && !elmt.getName().equals("tig"))
                {
                    xml.append(elmt.asXML());
                }
            }
        	String lxml = xml.toString();
        	boolean needClob = false;
            int lIndex = getIndexOfTBLangs(Integer.valueOf(p_cid), langName, 
                                           langLocale, p_tb_langs);
        	
            if (lIndex > -1)
            {
                p_tb_langs.remove(lIndex);

                lid = (Long) p_tb_language_lids.remove(lIndex);
                
                // in-place update of the language info
                statement = "update TB_LANGUAGE " + "set Cid=" + p_cid + ","
                        + "    Name='" + langName + "'," + "    Locale='"
                        + langLocale + "'," + "    Xml="
                        + SqlUtil.getClobInitializer(lxml, needClob)
                        + " where tbid=" + m_id + " and lid=" + lid;
            }
            else
            {
                lid = maxLID++;

                statement = "insert into TB_LANGUAGE "
                        + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                        + m_id + "," + lid + "," + p_cid + "," + "'" + langName
                        + "'," + "'" + langLocale + "',"
                        + SqlUtil.getClobInitializer(lxml, needClob) + ")";
            }
        	result.addLanguageStatement(statement);
        	
        	List termGrps = new ArrayList();
        	termGrps.addAll(langSet.selectNodes("ntig/termGrp"));
        	termGrps.addAll(langSet.selectNodes("tig"));
        	for (Iterator it1 = termGrps.iterator(); it1.hasNext();)
        	{
        		Element termGrp = (Element) it1.next();
        		
        		String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();
                
                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);
                
                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);
                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                        xml1.append(elmt.asXML());
                    }
                }
                
                String txml = xml1.toString();                
                boolean needClob1 = false;
                
                int tIndex = getIndexOfTBTerms(Integer.valueOf(p_cid), lid, p_tb_terms);
                
                if (tIndex > -1)
                {
                    p_tb_terms.remove(tIndex);
                    tid = (Long) p_tb_term_tids.remove(tIndex);

                    statement = "update TB_TERM " + " set Cid=" + p_cid + ","
                            + " Lid=" + lid + "," + " Lang_Name='"
                            + SqlUtil.quote(langName) + "'," + " Term='"
                            + SqlUtil.quote(term) + "'," + " Type='"
                            + SqlUtil.quote(termType) + "'," + " Status='"
                            + SqlUtil.quote(termStatus) + "'," + " Sort_Key='"
                            + sortKey + "'," + " XML="
                            + SqlUtil.getClobInitializer(txml, needClob1)
                            + " where tbid=" + m_id + " and tid=" + tid;
                    result.addTermStatement(statement);

                    while (getIndexOfTBTerms(Integer.valueOf(p_cid), lid, p_tb_terms) > -1)
                    {
                        tIndex = getIndexOfTBTerms(Integer.valueOf(p_cid), lid, p_tb_terms);
                        p_tb_terms.remove(tIndex);
                        tid = (Long) p_tb_term_tids.remove(tIndex);

                        // delete other term with the same language
                        statement = "DELETE FROM TB_TERM " +
                                    "WHERE tbid=" + m_id + " AND tid=" + tid;
                        result.addTermStatement(statement);
                    }
                }
                else
                {
                    tid = maxTID++;

                    statement = "insert into TB_TERM "
                            + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                            + " Type, Status, Sort_Key, XML)" + " values ("
                            + m_id + "," + p_cid + "," + lid + "," + tid
                            + ",'" + SqlUtil.quote(langName) + "','"
                            + SqlUtil.quote(term) + "','" + SqlUtil.quote(termType)
                            + "','" + SqlUtil.quote(termStatus) + "','"
                            + sortKey + "'," + SqlUtil.getClobInitializer(txml, needClob1) + ")";
                }
                result.addTermStatement(statement);
        	}
        }
        setMaxIDS(-1, maxLID, maxTID);
        return result;
    }
    
    /**
     * The implementation is different from updating a single entry because lids
     * and tids are shuffled across different cids.
     */
    private Statements getOverwriteEntryStatements(Entry p_entry, long p_cid,
            ArrayList p_tb_language_lids, ArrayList p_tb_langs,
            ArrayList p_tb_term_tids, ArrayList p_tb_terms,
            SessionInfo p_session)
            throws TermbaseException
    {
        Statements result = new Statements();
        
        long maxLID, maxTID;
        Map<String, Long> mapIDS = getMaxIDS();
        maxLID = mapIDS.get("lid");
        maxTID = mapIDS.get("tid");

        Long lid, tid;
        String statement;
        String value;

        // Update <concept> in entry (just for safety)
        p_entry.setId(p_cid);
        EntryUtils.setConceptId(p_entry, p_cid);

        // Update modification timestamp in entry (this is UPDATE)
        p_session.setTimestamp();
        EntryUtils.setModificationTimeStamp(p_entry, p_session);

        Element root = p_entry.getDom().getRootElement();

        // CATEGORY.debug("ENTRY=" + root.asXML());

        /*
         * // delete all index rows, will re-index all new terms later {
         * statement = "delete from TB_FUZZY_INDEX " + "where tbid=" + m_id + "
         * and cid=" + p_cid;
         * 
         * result.addTokenStatement(statement); }
         */

        // produce concept-level statement
        {
            String domain = "*unknown*";
            String project = "*unknown*";
            String status = "proposed";
            StringBuffer xml = new StringBuffer();

            // Extract values of indexed concept attributes.
            // TODO: this has to use the TbDefinition.
            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='domain']")) != null
                    && value.length() > 0)
            {
                domain = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='project']")) != null
                    && value.length() > 0)
            {
                project = value;
            }

            if ((value = root
                    .valueOf("/conceptGrp/descripGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }
            else if ((value = root
                    .valueOf("/conceptGrp/descrip[@type='status']")) != null
                    && value.length() > 0)
            {
                status = value;
            }

            // Prepare concept nodes for storage
            for (Iterator it = root.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("languageGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String cxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(cxml) > 4000;
            boolean needClob = false;

            statement = "update TB_CONCEPT " + "set Domain='"
                    + SqlUtil.quote(domain) + "'," + "    Status='"
                    + SqlUtil.quote(status) + "'," + "    Project='"
                    + SqlUtil.quote(project) + "'," + "    XML="
                    + SqlUtil.getClobInitializer(cxml, needClob) + ","
                    + "    Modified_On='"
                    + UTC.valueOf(p_session.getTimestamp()) + "',"
                    + "    Modified_By='"
                    + SqlUtil.quote(p_session.getUserName()) + "' "
                    + " where tbid=" + m_id + "   and cid=" + p_cid;

            result.addConceptStatement(statement);
        }

        // produce language-level statements
        List langGrps = root.selectNodes("/conceptGrp/languageGrp");
        for (int i = 0, max = langGrps.size(); i < max; ++i)
        {
            Element langGrp = (Element) langGrps.get(i);

            String langName = langGrp.valueOf("language/@name");
            String langLocale = langGrp.valueOf("language/@locale");
            StringBuffer xml = new StringBuffer();

            // Prepare language nodes for storage
            for (Iterator it = langGrp.elementIterator(); it.hasNext();)
            {
                Element elmt = (Element) it.next();

                if (!elmt.getName().equals("language")
                        && !elmt.getName().equals("termGrp"))
                {
                    xml.append(elmt.asXML());
                }
            }

            String lxml = xml.toString();
            // boolean needClob = EditUtil.getUTF8Len(lxml) > 4000;
            boolean needClob = false;
            int lIndex = getIndexOfTBLangs((int) p_cid, langName, 
                                           langLocale, p_tb_langs);
            if (lIndex > -1)
            {
                p_tb_langs.remove(lIndex);

                lid = (Long) p_tb_language_lids.remove(lIndex);

                // in-place update of the language info
                statement = "update TB_LANGUAGE " + "set Cid=" + p_cid + ","
                        + "    Name='" + langName + "'," + "    Locale='"
                        + langLocale + "'," + "    Xml="
                        + SqlUtil.getClobInitializer(lxml, needClob)
                        + " where tbid=" + m_id + " and lid=" + lid;
            }
            else
            {
                lid = maxLID++;
                
                statement = "insert into TB_LANGUAGE "
                        + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                        + m_id + "," + lid + "," + p_cid + "," + "'" + langName
                        + "'," + "'" + langLocale + "',"
                        + SqlUtil.getClobInitializer(lxml, needClob) + ")";
            }

            result.addLanguageStatement(statement);

            /*
             * // Prepare a tokenizer for all terms in this language ITokenizer
             * tokenizer = TokenizerFactory.makeTokenizer(
             * LocaleCreater.makeLocale(langLocale));
             */

            // produce term-level statements for all terms in this language
            List termGrps = langGrp.selectNodes("termGrp");
            for (Iterator it1 = termGrps.iterator(); it1.hasNext();)
            {
                Element termGrp = (Element) it1.next();

                String term;
                String termType = "*unknown*";
                String termStatus = "*unknown*";
                String sortKey;
                StringBuffer xml1 = new StringBuffer();

                // Extract term and compute binary sortkey
                term = termGrp.valueOf("term");
                sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, langLocale),
                        MAX_SORTKEY_LEN);

                // Limit size of data
                term = EditUtil.truncateUTF8Len(term, MAX_TERM_LEN);

                // Extract term and values of indexed term attributes
                if ((value = termGrp.valueOf(".//descrip[@type='type']")) != null
                        && value.length() > 0)
                {
                    termType = value;
                }

                if ((value = termGrp.valueOf(".//descrip[@type='status']")) != null
                        && value.length() > 0)
                {
                    termStatus = value;
                }

                for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
                {
                    Element elmt = (Element) it2.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                        xml1.append(elmt.asXML());
                    }
                }

                String txml = xml1.toString();
                // boolean needClob1 = EditUtil.getUTF8Len(txml) > 4000;
                boolean needClob1 = false;
                int tIndex = getIndexOfTBTerms((int)p_cid, lid, p_tb_terms);
                
                if (tIndex > -1)
                {
                    p_tb_terms.remove(tIndex);
                    
                    tid =  (Long) p_tb_term_tids.remove(tIndex);

                    statement = "update TB_TERM " + " set Cid=" + p_cid + ","
                            + " Lid=" + lid + "," + " Lang_Name='"
                            + SqlUtil.quote(langName) + "'," + " Term='"
                            + SqlUtil.quote(term) + "'," + " Type='"
                            + SqlUtil.quote(termType) + "'," + " Status='"
                            + SqlUtil.quote(termStatus) + "'," + " Sort_Key='"
                            + sortKey + "'," + " XML="
                            + SqlUtil.getClobInitializer(txml, needClob1)
                            + " where tbid=" + m_id + " and tid=" + tid;
                    result.addTermStatement(statement);

                    while (getIndexOfTBTerms((int) p_cid, lid, p_tb_terms) > -1)
                    {
                        tIndex = getIndexOfTBTerms((int) p_cid, lid, p_tb_terms);
                        p_tb_terms.remove(tIndex);
                        tid = (Long) p_tb_term_tids.remove(tIndex);

                        // delete other term with the same language
                        statement = "DELETE FROM TB_TERM " +
                                    "WHERE tbid=" + m_id + " AND tid=" + tid;
                        result.addTermStatement(statement);
                    }
                }
                else
                {
                    tid = maxTID++;
                    
                    statement = "insert into TB_TERM "
                            + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                            + " Type, Status, Sort_Key, XML)" + " values ("
                            + m_id
                            + ","
                            + p_cid
                            + ","
                            + lid
                            + ","
                            + tid
                            + ","
                            + "'"
                            + SqlUtil.quote(langName)
                            + "',"
                            + "'"
                            + SqlUtil.quote(term)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termType)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termStatus)
                            + "',"
                            + "'"
                            + sortKey
                            + "',"
                            + SqlUtil.getClobInitializer(txml, needClob1) + ")";
                }

                result.addTermStatement(statement);
            }
        }

        setMaxIDS(-1, maxLID, maxTID);
        return result;
    }

    /**
     * Get the index of tb_language list, by input data.
     * If can't find, return -1.
     * 
     * @param p_cid
     *            concept id
     * @param p_langName
     *            language name
     * @param p_langLocale
     *            language locale
     * @param p_tb_langs
     *            tb_language list
     */
    private int getIndexOfTBLangs(int p_cid, String p_langName,
            String p_langLocale, ArrayList p_tb_langs)
    {
        for (int i = 0; i < p_tb_langs.size(); i++)
        {
            Language lang = (Language) p_tb_langs.get(i);
            if (lang.isEqual(p_cid, p_langName, p_langLocale))
            {
                return i;
            }
        }

        return -1;
    }
    
    /**
     * Get the index of tb_term list, by input data. 
     * If can't find, return -1.
     * 
     * @param p_cid
     *            concept id
     * @param p_lid
     *            language id
     * @param p_tb_terms
     *            term list
     */
    private int getIndexOfTBTerms(int p_cid, long p_lid, ArrayList p_tb_terms)
    {
        for (int i = 0; i < p_tb_terms.size(); i++)
        {
            Term term = (Term) p_tb_terms.get(i);
            if (term.isEqual(p_cid, (int) p_lid))
            {
                return i;
            }
        }

        return -1;
    }
    
    /**
     * <p>
     * Produces SQL statements to delete a single entry from the database.
     * </p>
     */
    private Statements getDeleteEntryStatements(long p_entryId,
            SessionInfo p_session) throws TermbaseException
    {
        Statements result = new Statements();
        String statement;

        /*
         * statement = "delete from TB_FUZZY_INDEX where TBId=" + m_id + " and
         * CID=" + p_entryId; result.addTokenStatement(statement);
         */

        statement = "delete from TB_TERM where TBId=" + m_id + " and CID="
                + p_entryId;
        result.addTermStatement(statement);

        statement = "delete from TB_LANGUAGE where TBId=" + m_id + " and CID="
                + p_entryId;
        result.addLanguageStatement(statement);

        statement = "delete from TB_CONCEPT where TBId=" + m_id + " and CID="
                + p_entryId;
        result.addConceptStatement(statement);

        return result;
    }

    /**
     * <p>
     * Produces SQL statements to delete a language and all its terms from the
     * database.
     * </p>
     */
    private Statements getDeleteLanguageStatements(String p_languageName)
            throws TermbaseException
    {
        Statements result = new Statements();
        String statement;

        /*
         * statement = "delete from TB_FUZZY_INDEX where TBId=" + m_id + " and
         * Lang_Name='" + p_languageName + "'";
         * result.addTokenStatement(statement);
         */

        statement = "delete from TB_TERM where TBId=" + m_id
                + " and Lang_Name='" + p_languageName + "'";
        result.addTermStatement(statement);

        statement = "delete from TB_LANGUAGE where TBId=" + m_id
                + " and Name='" + p_languageName + "'";
        result.addLanguageStatement(statement);

        return result;
    }

    /**
     * <p>
     * Returns all statements to add a batch of entries as new entries to the
     * termbase.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    /* private */Statements batchAddEntriesAsNew(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session)
            throws TermbaseException
    {
        Statements result = new Statements();
        Statements stmts;

        for (int i = 0, max = p_entries.size(); i < max; ++i)
        {
            Entry entry = (Entry) p_entries.get(i);

            try
            {
                // Check for presence of required fields.
                EntryUtils.normalizeEntry(entry, m_definition);

                stmts = getAddEntryStatements(entry, m_definition, p_session);

                result.addAll(stmts);
            }
            catch (TermbaseException ex)
            {
                // Ignore errors in this one entry.
                CATEGORY.warn("batchAddEntries: ignoring invalid entry: "
                        + ex.getMessage() + "\n" + entry.getXml());
                continue;
            }
        }

        return result;
    }
    
    Statements batchAddTbxEntriesAsNew(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session) throws TermbaseException {
    	Statements result = new Statements();
        Statements stmts;
        
        for (int i = 0, max = p_entries.size(); i < max; ++i) {
        	Entry entry = (Entry) p_entries.get(i);
        	
        	try {
        		EntryUtils.normalizeTbxEntry(entry, m_definition);
        		stmts = getAddTbxEntryStatements(entry, m_definition, p_session);
        		result.addAll(stmts);
        	} catch (Exception e) {
        		CATEGORY.warn("batchAddTbxEntriesAsNew: ignoring invalid entry: "
                        + e.getMessage() + "\n" + entry.getXml());
                continue;
        	}
        }
        
        return result;
    }
    
    /**
     * <p>
     * Adds a batch of entries synchronizing on the concept id, for tbx files.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    private Statements batchAddTbxEntriesByConceptId(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session)
    		throws TermbaseException {
    	Statements result = null;
    	
    	for (Iterator it = p_entries.iterator(); it.hasNext();)
        {
            Entry entry = (Entry) it.next();

            try
            {
                // Check for presence of required fields.
                EntryUtils.normalizeTbxEntry(entry, m_definition);
            }
            catch (TermbaseException ex)
            {
                // Ignore errors in this one entry.
                CATEGORY.warn("ignoring invalid entry: " + ex.getMessage()
                        + "\n" + entry.getXml());
                it.remove();
            }
        }

        // Delegate the finicky work of merging to a data center.
        BatchDataCenterByCid bdc = new BatchDataCenterByCid(this, p_options,
                p_session);

        result = bdc.getTbxBatchImportStatements(p_entries);

        return result;
    }

    /**
     * <p>
     * Adds a batch of entries synchronizing on the concept id.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    private Statements batchAddEntriesByConceptId(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session)
            throws TermbaseException
    {
        Statements result = null;

        for (Iterator it = p_entries.iterator(); it.hasNext();)
        {
            Entry entry = (Entry) it.next();

            try
            {
                // Check for presence of required fields.
                EntryUtils.normalizeEntry(entry, m_definition);
            }
            catch (TermbaseException ex)
            {
                // Ignore errors in this one entry.
                CATEGORY.warn("ignoring invalid entry: " + ex.getMessage()
                        + "\n" + entry.getXml());

                it.remove();
            }
        }

        // Delegate the finicky work of merging to a data center.
        BatchDataCenterByCid bdc = new BatchDataCenterByCid(this, p_options,
                p_session);

        result = bdc.getBatchImportStatements(p_entries);

        return result;
    }
    
    /**
     * <p>
     * Adds a batch of entries by sunchronizing on a language.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    private Statements batchAddTbxEntriesByLanguage(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session)
            throws TermbaseException {
    	Statements result = null;
    	for (Iterator it = p_entries.iterator(); it.hasNext();) {
    		Entry entry = (Entry) it.next();
    		
    		try {
    			EntryUtils.normalizeTbxEntry(entry, m_definition);
    		} catch (TermbaseException ex) {
    			// Ignore errors in this one entry.
                CATEGORY.warn("ignoring invalid entry: " + ex.getMessage()
                        + "\n" + entry.getXml());

                it.remove();
    		}
    	}
    	// Delegate the finicky work of merging to a data center.
        BatchDataCenterByTerm bdc = new BatchDataCenterByTerm(this, p_options,
                p_session);

        result = bdc.getTbxBatchImportStatements(p_entries);

        return result;
    }

    /**
     * <p>
     * Adds a batch of entries by sunchronizing on a language.
     * </p>
     * 
     * @param entries
     *            a list of Entry objects.
     */
    private Statements batchAddEntriesByLanguage(ArrayList p_entries,
            SyncOptions p_options, SessionInfo p_session)
            throws TermbaseException
    {
        Statements result = null;

        for (Iterator it = p_entries.iterator(); it.hasNext();)
        {
            Entry entry = (Entry) it.next();

            try
            {
                // Check for presence of required fields.
                EntryUtils.normalizeEntry(entry, m_definition);
            }
            catch (TermbaseException ex)
            {
                // Ignore errors in this one entry.
                CATEGORY.warn("ignoring invalid entry: " + ex.getMessage()
                        + "\n" + entry.getXml());

                it.remove();
            }
        }

        // Delegate the finicky work of merging to a data center.
        BatchDataCenterByTerm bdc = new BatchDataCenterByTerm(this, p_options,
                p_session);

        result = bdc.getBatchImportStatements(p_entries);

        return result;
    }

    /**
     * <p>
     * Produces SQL statements to update the termbase definition. This method
     * does not update the name.
     * </p>
     */
    private Statements getUpdateDefinitionStatements(Definition p_definition)
    {
        Statements result = new Statements();

        String description = p_definition.getDescription();
        description = EditUtil.truncateUTF8Len(description, 4000);

        String definition = p_definition.getXml();
        // boolean needClob = EditUtil.getUTF8Len(definition) > 4000;
        boolean needClob = false;

        String statement = "UPDATE TB_TERMBASE " + "SET TB_Description='"
                + SqlUtil.quote(description) + "', " + "    TB_Definition="
                + SqlUtil.getClobInitializer(definition, needClob)
                + " WHERE TBid=" + m_id;

        result.addOtherStatement(statement);

        return result;
    }

    /**
     * Sets up all necessary index objects for this termbase.
     * 
     * This method is called from the constructor so it is unprotected.
     */
    public void initIndexes(Definition p_definition)
    {
        ArrayList indexes = p_definition.getIndexes();
        Index idx;

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Definition.Index index = (Definition.Index) indexes.get(i);

            if (index.getLanguageName().length() == 0)
            {
                // Concept-level full-text index in the specified locale.

                idx = new TbTextIndex(m_name, "", index.getLocale());

                try
                {
                    idx.open();
                    m_conceptLevelFulltextIndex = idx;
                }
                catch (IOException ex)
                {
                    CATEGORY.error("index " + idx.getDirectory()
                            + " cannot be opened", ex);
                }
            }
            else if (index.getType().equals(index.TYPE_FULLTEXT))
            {
                idx = new TbTextIndex(m_name, index.getLanguageName(), index
                        .getLocale());

                try
                {
                    idx.open();
                    m_fulltextIndexes.add(idx);
                }
                catch (IOException ex)
                {
                    CATEGORY.error("index " + idx.getDirectory()
                            + " cannot be opened", ex);
                }
            }
            else if (index.getType().equals(index.TYPE_FUZZY))
            {
                idx = new TbFuzzyIndex(m_name, index.getLanguageName(), index
                        .getLocale());

                try
                {
                    idx.open();
                    m_fuzzyIndexes.add(idx);
                }
                catch (IOException ex)
                {
                    CATEGORY.error("index " + idx.getDirectory()
                            + " cannot be opened", ex);
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
        Index idx;

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
     * Batches and executes statements.
     */
    public void executeStatements(Statements p_statements)
            throws TermbaseException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            for (int i = 0, max = p_statements.m_concepts.size(); i < max; ++i)
            {
                // CATEGORY.debug((String)p_statements.m_concepts.get(i));
                stmt.addBatch((String) p_statements.m_concepts.get(i));
            }

            for (int i = 0, max = p_statements.m_languages.size(); i < max; ++i)
            {
                // CATEGORY.debug((String)p_statements.m_languages.get(i));
                stmt.addBatch((String) p_statements.m_languages.get(i));
            }

            for (int i = 0, max = p_statements.m_terms.size(); i < max; ++i)
            {
                // CATEGORY.debug((String)p_statements.m_terms.get(i));

                stmt.addBatch((String) p_statements.m_terms.get(i));
            }

            /*
             * for (int i = 0, max = p_statements.m_tokens.size(); i < max; ++i) { //
             * CATEGORY.debug((String)p_statements.m_tokens.get(i));
             * stmt.addBatch((String)p_statements.m_tokens.get(i)); }
             */

            for (int i = 0, max = p_statements.m_others.size(); i < max; ++i)
            {
                // CATEGORY.debug((String)p_statements.m_others.get(i));
                stmt.addBatch((String) p_statements.m_others.get(i));
            }

            stmt.executeBatch();
            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Performs a fuzzy search in the given language and returns results in a
     * hitlist.
     */
    private Hitlist searchFuzzy(String p_language, String p_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        // Lucene indexes have different scores, use lower threshold.
        // I've seen 2% matches that seemed to make some sense, but the
        // 1% matches were garbage.
        // Screw it, the threshold doesn't do it.
        // float threshold = 0.0f;
        float threshold = 0.001f;

        if (p_query.length() == 0)
        {
            return result;
        }

        Index index = getFuzzyIndex(p_language);
        if (index == null)
        {
            return result;
        }

        // Perform the search in a Lucene index and copy the results.

        com.globalsight.ling.lucene.Hits hits = null;

        try
        {
            hits = index.search(p_query, p_maxHits, threshold);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Search fuzzy index " + index.getName()
                        + " query=" + p_query + " maxhits=" + p_maxHits
                        + " threshold=" + threshold + " returned "
                        + hits.size() + " results");
            }

            for (int i = 0, max = hits.size(); i < max; i++)
            {
                result.add(hits.getText(i), hits.getMainId(i),
                        hits.getSubId(i), (int) (hits.getScore(i) * 100.0), "");
            }
        }
        catch (Exception ex)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("cannot search fuzzy index", ex);
            }

            return result;
        }

        // For fuzzy searches, the index stores a copy of the original
        // term so we have all information at this point.
        // (Not sure what to do for fulltext searches.)

        return result;
    }

    /**
     * Performs a full text search in the given language and returns results in
     * a hitlist. If the concept-level index exists and is in the same locale,
     * search it too.
     */
    private Hitlist searchFulltext(String p_language, String p_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        // Lucene indexes have different scores, use lower threshold.
        // I've seen 2% matches that seemed to make some sense, but the
        // 1% matches were garbage.
        float threshold = 0.01f;

        if (p_query.length() == 0)
        {
            return result;
        }

        try
        {
            com.globalsight.ling.lucene.Hits hits = null;

            // Search the language's full text index.
            Index index = getFulltextIndex(p_language);
            if (index != null)
            {
                hits = index.search(p_query, p_maxHits, threshold);

                for (int i = 0, max = hits.size(); i < max; i++)
                {
                    result.add(hits.getText(i), hits.getMainId(i), hits
                            .getSubId(i), (int) (hits.getScore(i) * 100.0), "");
                }
            }

            // Search the concept-level index.
            index = getConceptLevelFulltextIndex();
            if (index != null && index.getLocale().equals(p_locale))
            {
                hits = index.search(p_query, p_maxHits, threshold);

                for (int i = 0, max = hits.size(); i < max; i++)
                {
                    result.add(hits.getText(i), hits.getMainId(i), hits
                            .getSubId(i), (int) (hits.getScore(i) * 100.0), "");
                }
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot search full text index", ex);
        }

        // Sort results by score.
        result.sortByScore();

        // Full text results are truncated in the c.g.ling.lucene.Index class.

        return result;
    }

    /**
     * Performs an exact-match search in the given language and returns results
     * in a hitlist. Multiple matches can be returned if the language contains
     * homographs.
     */
    protected Hitlist searchExact(String p_language, String p_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sortKey = SqlUtil.toHex(Sortkey
                    .getSortkey(p_query, p_locale), MAX_SORTKEY_LEN);

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                    + "where " + "    TBid =" + m_id + " "
                    + "and Lang_Name = '" + SqlUtil.quote(p_language) + "' "
                    + "and Sort_Key = '" + sortKey + "'");

            while (rset.next())
            {
                String term = rset.getString("TERM");
                long tid = rset.getLong("TID");
                long cid = rset.getLong("CID");
                String xml = rset.getString("XML");
                if (xml == null)
                	xml = "";

                // score of this hit is not relevant
                result.add(term, cid, tid, 100, xml);
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * Performs a regular-expression search using the SQL LIKE operator in the
     * given language and returns results in a hitlist.
     */
    private Hitlist searchRegexp(String p_language, String p_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("regexp search in " + p_language + " for `"
                        + p_query + "'");
            }

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                    + "where " + "    TBid =" + m_id + " "
                    + "and Lang_Name = '" + SqlUtil.quote(p_language) + "' "
                    + "and Term like '" + SqlUtil.quote(p_query) + "' "
                    + "order by sort_key asc" + "limit " + p_maxHits);

            while (rset.next())
            {
                String term = rset.getString("TERM");
                long tid = rset.getLong("TID");
                long cid = rset.getLong("CID");
                String xml = rset.getString("XML");
                if (xml == null)
                	xml = "";

                // score of this hit is not relevant
                result.add(term, cid, tid, 100, xml);
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * Performs an alphabetic prefix-search in the given language and returns
     * results in a hitlist. The result is an alphabetic list of terms that are
     * equal or greater (in the indexes sort order) to the search term.
     */
    private Hitlist searchIndex(String p_language, String p_locale, 
            String target_language, String target_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            String sortKey = SqlUtil.toHex(Sortkey
                    .getSortkey(p_query, p_locale), MAX_SORTKEY_LEN);

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            /*
            rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                    + "where " + "    TBid =" + m_id + " "
                    + "and Lang_Name = '" + SqlUtil.quote(p_language) + "' "
                    + "and Sort_Key >= '" + sortKey + "' "
                    + "order by sort_key asc " + "limit " + p_maxHits);
            */
            rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                    + "where " + "    TBid =" + m_id + " "
                    + "and Lang_Name = '" + SqlUtil.quote(p_language.toLowerCase()) + "' "
                    + "and Sort_Key > '" + sortKey + "' "
                    + "order by sort_key asc ");
            
            int number = 0;

            while (rset.next())
            {
                if(number == p_maxHits || number >= p_maxHits) {
                    break;
                }
                
                String term = rset.getString("TERM");
                long tid = rset.getLong("TID");
                long cid = rset.getLong("CID");
                String xml = rset.getString("XML");
                if (xml == null)
                	xml = "";

                if(target_language != null) {
                    String sql =  "select Lang_Name from tb_term where cid=" + cid;
                    Statement stmt1 = conn.createStatement();
                    ResultSet rs = stmt1.executeQuery(sql);
                    //if one source has tow or more target, the list will show duplicate
                    //same source. so use the arraylist remember the term, if the term content
                    //is same and in one concept, don add to result.
                    ArrayList array = new ArrayList();
                    
                    while(rs.next()) {
                        String langName = rs.getString("Lang_Name");
                        
                        if(langName.equalsIgnoreCase(target_language) && !array.contains(term)) {
                            array.add(term);
                            result.add(term, cid, tid, 100, xml);
                            number++;
                        }
                    }
                    
                    rs.close(); 
                    stmt1.close();
                }
                else {
                    // score of this hit is not relevant
                    result.add(term, cid, tid, 100, xml);
                }
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * Returns a fuzzy query.
     */
    /*
     * private String makeFuzzyQuery(Collection tokens, String p_language,
     * double threshold, int p_maxHits) { // 4th column is to get an avarage of
     * fuzzy score and // concordance score String sql = "select f.TID, f.CID,
     * t.TERM, " + " ((2.0*count(f.TID))/(f.Feature_Count + " + tokens.size() + ") +
     * (count(f.TID)/" + tokens.size() + "))/2 " + "from TB_FUZZY_INDEX f,
     * TB_TERM t " + "where f.LANG_FEATURE in (";
     * 
     * for (Iterator it = tokens.iterator(); it.hasNext(); ) { Token token =
     * (Token)it.next();
     * 
     * sql += "'" + SqlUtil.quote(token.getToken()) + "'";
     * 
     * if (it.hasNext()) { sql += ","; } }
     * 
     * sql += ") " + " and f.TBID = " + m_id + " and f.LANG_NAME = '" +
     * SqlUtil.quote(p_language) + "' " + " and f.TBID = t.TBID and f.TID =
     * t.TID " + "group by f.TID, f.CID, t.TERM, f.Feature_Count " + "having
     * ((2.0*count(f.TID))/(f.Feature_Count + " + tokens.size() + ") +
     * (count(f.TID)/" + tokens.size() + "))/2 >= " + threshold + " " + "order
     * by 4 DESC";
     * 
     * return sql; }
     */

    /**
     * Browses the given language alphabetically and returns results in a
     * hitlist. The result is an alphabetic list of terms that are greater (or
     * smaller) (in the indexes sort order) to the search term.
     */
    private Hitlist browseIndex(String p_language, String p_locale, String target_lan,
            String p_start, int p_direction, int p_maxHits)
            throws TermbaseException
    {
        Hitlist result = new Hitlist();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            if (p_start.length() == 0 && p_direction == 0)
            {
                // start at end of index -- use a max character
                p_start = "\uffee";
            }

            String sortKey = SqlUtil.toHex(Sortkey
                    .getSortkey(p_start, p_locale), MAX_SORTKEY_LEN);

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            if (p_direction == 0) // GO UP
            {
                rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                        + "where " + "    TBid =" + m_id + " "
                        + "and Lang_Name = '" + SqlUtil.quote(p_language)
                        + "' " + "and Sort_Key < '" + sortKey + "' "
                        + "order by sort_key desc ");
            }
            else
            // GO DOWN
            {
                rset = stmt.executeQuery("SELECT TERM, TID, CID, XML from TB_TERM "
                        + "where " + "    TBid =" + m_id + " "
                        + "and Lang_Name = '" + SqlUtil.quote(p_language)
                        + "' " + "and Sort_Key > '" + sortKey + "' "
                        + "order by sort_key asc ");
            }

            int number = 0;
            
            while (rset.next())
            {
                if(number == p_maxHits || number >= p_maxHits) {
                    break;
                }
                
                String term = rset.getString("TERM");
                long tid = rset.getLong("TID");
                long cid = rset.getLong("CID");
                String xml = rset.getString("XML");
                if (xml == null)
                	xml = "";

                if(target_lan != null) {
                    String sql =  "select Lang_Name from tb_term where cid=" + cid;
                    Statement stmt1 = conn.createStatement();
                    ResultSet rs = stmt1.executeQuery(sql);
                    
                    while(rs.next()) {
                        String langName = rs.getString("Lang_Name");
                        
                        if(langName.equals(target_lan)) {
                            result.add(term, cid, tid, 100, xml);
                            number++;
                        }
                    }
                    
                    rs.close(); 
                    stmt1.close();
                }
                else {
                    // score of this hit is not relevant
                    result.add(term, cid, tid, 100, xml);
                }
            }

            if (p_direction == 0)
            {
                result.reverse();
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    TreeMap getCidsByTerms(ArrayList p_terms, String p_language)
            throws TermbaseException
    {
        TreeMap result = new TreeMap();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        // TODO: this needs to use a stored procedure and array
        // argument passing.

        addReader();

        try
        {
            String locale = getLocaleByLanguage(p_language);

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            for (int i = 0, max = p_terms.size(); i < max; i++)
            {
                String term = (String) p_terms.get(i);

                rset = stmt.executeQuery("SELECT CID from TB_TERM " + "where "
                        + "    TBid =" + m_id + " " + "and Lang_Name = '"
                        + SqlUtil.quote(p_language) + "' " + "and Term = '"
                        + SqlUtil.quote(term) + "'");

                if (rset.next())
                {
                    long cid = rset.getLong("CID");

                    result.put(term, new Long(cid));
                }

                rset.close();
                rset = null;
            }

            conn.commit();
        }
        catch (/* SQL */Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            releaseReader();

            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    /**
     * For debugging the term leverager: calls the leverager on the given query,
     * which should be a segment, and returns the source terms found.
     */
    private Hitlist doTermLeveraging(String p_language, String p_locale,
            String p_query, int p_maxHits) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        if (p_query.length() == 0)
        {
            return result;
        }

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("term leverage in " + p_language + " ("
                        + p_locale + ") for '" + p_query + "'");
            }

            ArrayList matches = leverageTerms(p_query, p_language, p_locale);

            for (int i = 0, max = matches.size(); i < max; i++)
            {
                TermLeverageResult.MatchRecord match = (TermLeverageResult.MatchRecord) matches
                        .get(i);

                result.add(match.getMatchedSourceTerm(), match.getConceptId(),
                        match.getMatchedSourceTermId(), match.getScore(), match.getSourceDescXML());
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot leverage terms", ex);
        }

        return result;
    }

    private ArrayList leverageTerms(String p_segment, String p_language,
            String p_locale) throws TermbaseException, TermLeveragerException
    {
        Locale locale = LocaleCreater.makeLocale(p_locale);
        TermLeverageOptions options = new TermLeverageOptions();

        // fuzzy threshold set by object constructor - use defaults.
        // options.setFuzzyThreshold(50);

        options.addTermBase(m_name);
        options.setLoadTargetTerms(true);
        options.setSaveToDatabase(false);
        options.setSourcePageLocale(locale);
        options.addSourcePageLocale2LangName(p_language);

        // need to add a target language != source language, pick any
        // and don't care which it is - this is only a "feature" to
        // debug the leverager.
        String targetLang = "";
        String targetLocl = "";

        ArrayList langs = m_definition.getLanguages();
        for (int i = 0, max = langs.size(); i < max; i++)
        {
            Definition.Language lang = (Definition.Language) langs.get(i);

            if (lang.getName().equals(p_language))
            {
                continue;
            }

            targetLang = lang.getName();
            targetLocl = lang.getLocale();

            break;
        }

        Locale targetLocale = LocaleCreater.makeLocale(targetLocl);

        options.addTargetPageLocale2LangName(targetLocale, targetLang);
        options.addLangName2Locale(targetLang, targetLocale);

        TermLeverager tl = new TermLeverager();

        p_segment = "<segment>" + EditUtil.encodeXmlEntities(p_segment)
                + "</segment>";

        try
        {
            TermLeverageResult result = tl.leverageTerms(p_segment, options);
            return result.getAllMatchRecords();
        }
        catch (java.rmi.RemoteException ignore)
        {
            // sigh
        }

        return null;
    }

    /**
     * Retrieves a string containing termbase statistics.
     * 
     * @see getStatistics()
     */
    private StatisticsInfo getStatisticsInfo() throws TermbaseException
    {
        addReader();

        StatisticsInfo result = new StatisticsInfo();
        result.setTermbase(m_name);

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            String language;
            int count;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);

                // Number of concepts
                stmt = conn.createStatement();
                rset = stmt.executeQuery("select count(*) from TB_CONCEPT "
                        + "where TBid = " + m_id);

                rset.next();
                count = rset.getInt(1);

                result.setConcepts(count);

                // Total number of terms
                stmt = conn.createStatement();
                rset = stmt.executeQuery("select count(*) from TB_TERM "
                        + "where TBid = " + m_id);

                rset.next();
                count = rset.getInt(1);

                result.setTerms(count);

                // Number of terms per language
                stmt = conn.createStatement();
                rset = stmt.executeQuery("select lang_name, count(tid) "
                        + "from tb_term where tbid=" + m_id + " "
                        + "group by lang_name");

                while (rset.next())
                {
                    language = rset.getString(1);
                    count = rset.getInt(2);

                    result.addLanguageInfo(language, count);
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                { /* ignore */
                }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }

            // After the raw database numbers, add indexing status and
            // fuzzy and fulltext index counts.

            if (isIndexing())
            {
                result.setIndexStatus(StatisticsInfo.INDEX_NOTAVAILABLE);
            }
            else
            {
                result.setIndexStatus(StatisticsInfo.INDEX_OK);

                if (m_conceptLevelFulltextIndex != null)
                {
                    try
                    {
                        count = m_conceptLevelFulltextIndex.getDocumentCount();
                    }
                    catch (Exception ex)
                    {
                        count = -1;
                    }

                    result.setFulltextCount(count);
                }

                for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
                {
                    Index idx = (Index) m_fulltextIndexes.get(i);

                    try
                    {
                        count = idx.getDocumentCount();
                    }
                    catch (Exception ex)
                    {
                        count = 0;
                    }

                    result.setFulltextIndexedCount(idx.getName(), count);
                }

                for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
                {
                    Index idx = (Index) m_fuzzyIndexes.get(i);

                    try
                    {
                        count = idx.getDocumentCount();
                    }
                    catch (Exception ex)
                    {
                        count = 0;
                    }

                    result.setFuzzyIndexedCount(idx.getName(), count);
                }
            }
        }
        finally
        {
            releaseReader();
        }

        return result;
    }

    /**
     * Creates and initializes a lock object.
     */
    private LockInfo makeLock(long p_entryId, SessionInfo p_session)
    {
        LockInfo result = new LockInfo(m_id, p_entryId);

        result.setUser(p_session.getUserName());
        result.setEmail("");

        // Timestamp and cookie set in LockInfo.init();

        return result;
    }

    /**
     * Hook to override who can steal locks. Currently, every user can overide
     * everybody else's lock.
     * 
     * Later on we may decide that only administrator's can overide user's
     * locks.
     */
    private boolean canStealLock(LockInfo p_lock, SessionInfo p_session)
    {
        return true;
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

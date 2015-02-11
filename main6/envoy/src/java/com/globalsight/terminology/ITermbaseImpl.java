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

import java.util.ArrayList;

import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.terminology.exporter.ExportManager;
import com.globalsight.terminology.importer.ImportManager;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.terminology.indexer.IndexManager;
import com.globalsight.terminology.searchreplace.ITermbaseMaintance;
import com.globalsight.terminology.searchreplace.SearchReplaceConstants;
import com.globalsight.terminology.searchreplace.SearchReplaceParams;
import com.globalsight.terminology.searchreplace.TbConceptMaintance;
import com.globalsight.terminology.searchreplace.TbLanguageMaintance;
import com.globalsight.terminology.searchreplace.TermMaintance;
import com.globalsight.terminology.userdata.UserdataManager;
import com.globalsight.util.SessionInfo;

/**
 * <p>Implementation of the ITermbase interface.  This RMI layer is
 * responsible for argument and user credential checking, calling the
 * actual method on the Termbase object, result preparation, and error
 * handling.</p>
 */
public class ITermbaseImpl implements ITermbase,TermbaseExceptionMessages
{
    //
    // Private Members
    //

    /** Pointer to actual termbase object.  */
    private Termbase m_termbase;
    /** Pointer to user-related session info.  */
    private SessionInfo m_session;

    //
    // Constructor
    //
    public ITermbaseImpl(Termbase p_termbase, SessionInfo p_session)
    {
        m_termbase = p_termbase;
        m_session = p_session;
    }


    public SessionInfo getSession()
    {
        return m_session;
    }

    public void setSession(SessionInfo session)
    {
        m_session = session;
    }

    /** Retrieves the termbase name */
    public String getName()
        throws TermbaseException
    {
        return m_termbase.getName();
    }

    /**
     * <p>Retrieves the Entry Structure Schema used in the
     * termbase.</p>
     *
     * @return an XML string specifying the schema.
     * @see schema.dtd
     */
    public String getSchema()
        throws TermbaseException
    {
        return m_termbase.getSchema();
    }

    /**
     * <p>Retrieves the termbase definition containing the languages,
     * text fields, and attribute fields.</p>
     *
     * @return an XML string specifying the termbase definition.
     */
    public String getDefinition()
        throws TermbaseException
    {
        return m_termbase.getDefinition();
    }

    /**
     * Given a language name, returns that language's locale as string.
     */
    public String getLocaleByLanguage(String p_language)
        throws TermbaseException
    {
        return m_termbase.getLocaleByLanguage(p_language);
    }

    /**
     * Given a locale, returns the language names that contain terms
     * in that locale.  Normally this is one language only, but some
     * termbases may contain a general language like Spanish and a
     * specific language like Spanish (Costa Rica).  In that case,
     * when searching for es_CR, both languages are returned (in no
     * specific order).
     *
     * @return an ArrayList of Strings.
     */
    public ArrayList getLanguagesByLocale(String p_locale)
        throws TermbaseException
    {
        return m_termbase.getLanguagesByLocale(p_locale);
    }

    /**
     * Retrieves a string containing termbase statistics.
     *
     * @return an xml string of the form
     *   <statistics>
     *     <termbase>termbase name</termbase>
     *     <concepts>number of entries</concepts>
     *     <terms>number of overall terms</terms>
     *     <indexes>
     *       <index>
     *         <language>index name</language>
     *         <terms>number of terms</terms>
     *       </index>
     *     </indexes>
     *   </statistics>
     */
    public String getStatistics()
        throws TermbaseException
    {
        try
        {
            return m_termbase.getStatistics();
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }
    
    public String getStatisticsWithoutIndexInfo() throws TermbaseException
    {
        return m_termbase.getStatisticsWithoutIndexInfo();
    }


    /**
     * <p>Retrieves an entry as xml string conforming to the Entry
     * Structure Schema.</p>
     *
     * @return the empty string if the entry does not exist, else an
     * XML string with root "conceptGrp".
     */
    public String getEntry(long p_entryId)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.getEntry(p_entryId, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }
    
    public String getEntryForBrowser (long p_entryId)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.getEntryForBrowser(p_entryId, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * <p>Retrieves an entry as xml string conforming to the Entry
     * Structure Schema, with source/target language group and source
     * term specially marked for easy formatting in the viewer.</p>
     *
     * @return the empty string if the entry does not exist, else an
     * XML string with root "conceptGrp".
     */
    public String getEntry(long p_entryId, long p_termId,
        String p_sourceLanguage, String p_targetLanguage)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.getEntry(p_entryId, p_termId,
                p_sourceLanguage, p_targetLanguage, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * Adds a new entry to the termbase.
     *
     * @return the newly assigned entry ID as long.
     */
    public long addEntry(String p_entry)
        throws TermbaseException
    {
        try
        {
            if (p_entry == null || p_entry.length() == 0)
            {
                String[] args = { "entry is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.addEntry(p_entry, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * Locks an entry for later update.
     *
     * @param steal: locks can be stolen if they are held by the same
     * user or have expired (default life time: 1 hour).
     *
     * @return the empty string if the lock could not be obtained, or
     * a non-empty string whose content is meaningless to the
     * client. Well, in fact it is a hereby documented xml string:
     *
     * <lock>
     *   <conceptid>1000</conceptid>
     *   <who>user name</who>
     *   <when>date</when>
     *   <email>user's email address</email>
     *   <cookie>E96CFDC1-9909-4264-A150-986BBE8E9564</cookie>
     * </lock>
     */
    public String lockEntry(long p_entryId, boolean p_steal)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.lockEntry(p_entryId, p_steal, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * <p>Unlocks a previously locked entry.  The cookie must have
     * been obtained by calling lockEntry() or the call will fail.</p>
     */
    public void unlockEntry(long p_entryId, String p_cookie)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_cookie == null || p_cookie.length() == 0)
            {
                String[] args = { "cookie is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            m_termbase.unlockEntry(p_entryId, p_cookie, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * Returns the lock information for an entry, without the cookie
     * to prevent spoofing somebody else's identity.
     *
     * @return the empty string if no lock has been set, or an xml
     * string:
     *
     * <lock>
     *   <conceptid>1000</conceptid>
     *   <who>user name</who>
     *   <when>date</when>
     *   <email>user's email address</email>
     * </lock>
     */
    public String getLockInfo(long p_entryId)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.getLockInfo(p_entryId, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * <p>Updates an existing entry.  The entry must have been locked
     * for editing by calling strCookie = lockEntry(entryID, false).
     * If the update is successful, the lock will be automatically
     * released.  If an error occurs, the lock will remain set and
     * must be manually released by the caller by calling
     * unlockEntry(). </p>
     *
     * Example: How to update an entry:
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
     *     throw "entry is locked by somebody else"
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
     * @throws TermbaseException if the lock is not owned by the
     * caller anymore, i.e., when it has been stolen by another user.
     */
    public void updateEntry(long p_entryId, String p_entry, String p_cookie)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_entry == null || p_entry.length() == 0)
            {
                String[] args = { "entry is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_cookie == null || p_cookie.length() == 0)
            {
                String[] args = { "lock is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            m_termbase.updateEntry(p_entryId, p_entry, p_cookie, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /** Deletes an entry from the termbase. */
    public void deleteEntry(long p_entryId)
        throws TermbaseException
    {
        try
        {
            if (p_entryId <= 0)
            {
                String[] args = { "entry id is 0" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            m_termbase.deleteEntry(p_entryId, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * Validates an entry by comparing all terms against the termbase
     * and checking for duplicates or near duplicates.
     */
    public String validateEntry(String p_entry)
        throws TermbaseException
    {
        try
        {
            if (p_entry == null || p_entry.length() == 0)
            {
                String[] args = { "entry is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.validateEntry(p_entry, m_session);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * <p>Searches a given language for the given expression.  A
     * hitlist is returned listing the terms found in the termbase,
     * their score, and their entry number.  For normal (alphabetical)
     * searches, the hitlist starts with the first term that is equal
     * or greater to the search term.</p>
     *
     * @param language: a valid index name in the termbase
     *
     * @param query: a search expression containing
       @param type: fuzzy, exact or wildcard
     * @param maxhits: specifies how many hits should be retrieved.
     * This can be only a hint and if homonyms ("homographs", really)
     * appear, the system will return them all.
     *
     * @return a hitlist as xml string of the form:
     *
     * <hitlist>
     *    <index>english</index>
     *    <expression>parl*m?nt</expression>
     *    <hits>
     *       <hit><score>100</score><term>parlamant</term><entry>10</entry></hit>
     *       <hit><score>100</score><term>parlament</term><entry>11</entry></hit>
     *       <homographs>
     *         <hit><score>100</score><term>parliament</term><entry>12</entry></hit>
     *         <hit><score>100</score><term>parliament</term><entry>13</entry></hit>
     *       </homographs>
     *       <hit><score>100</score><term>parlamont</term><entry>14</entry></hit>
     *    </hits>
     *  </hitlist>
     */
    public String search(String p_language, String target_lan, String p_query,
            String queryType, int p_maxHits, int begin) throws TermbaseException
    {
        try
        {
            if (p_language == null || p_language.length() == 0)
            {
                String[] args = { "language is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_query == null)
            {
                String[] args = { "query is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.search(p_language, target_lan, p_query,
                    queryType, p_maxHits, begin);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * <p>Searches a given fuzzy index for the given expression.</p>
     */
    public String fuzzySearch(String p_language, String p_query,
        int p_maxHits)
        throws TermbaseException
    {
        try
        {
            if (p_language == null || p_language.length() == 0)
            {
                String[] args = { "language is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_query == null)
            {
                String[] args = { "query is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.fuzzySearch(p_language, p_query, p_maxHits);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * <p>Implements part of term leveraging: for the query string,
     * all matching terms are retrieved from the termbase.</p>
     *
     * @see termleverager.TermLeverager
     *
     * @param language: a valid language name in the termbase
     * @param query: a string for which to search terms
     * @param maxHits: a limit on the number of hits returned
     *
     * @return Hitlist a Hitlist object listing all the matching terms.
     */
    public Hitlist recognizeTerms(String p_language, String p_query,
        int p_maxHits)
        throws TermbaseException
    {
        try
        {
            if (p_language == null || p_language.length() == 0)
            {
                String[] args = { "language is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            if (p_query == null)
            {
                String[] args = { "query is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }

            return m_termbase.recognizeTerms(p_language, p_query, p_maxHits);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * <p>Imports entries from a file.</p>
     *
     * <p>Import options are an XML structure specifying
     * - the file name
     * - the file format (GlobalSight, Multiterm, CSV, etc)
     * - the languages to include/ignore
     * - field mappings, inclusion and exclusion settings
     * - synchronization on language or entry number
     * - duplicate handling (overwrite, merge, ignore)
     * - filters (e.g., on attribute values)
     * </p>
     */
    public IImportManager getImporter()
        throws TermbaseException
    {
        try
        {
            IImportManager result = new ImportManager(m_termbase, m_session);

            return result;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }


    /**
     * <p>Allocates an Export Manager for exporting entries to a
     * file.</p>
     *
     * <p>Export options are an XML structure specifying
     * - (the file name)
     * - the file format (GlobalSight, Multiterm, CSV, RTF, etc)
     * - the languages to export (source/target, all)
     * - field mappings, inclusion and exclusion settings
     * - filters (e.g., on attribute values)
     * </p>
     */
    public IExportManager getExporter()
        throws TermbaseException
    {
        try
        {
            IExportManager result = new ExportManager(m_termbase, m_session);

            return result;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * <p>Allocates a User Data Manager for storing and retrieving
     * user-related data (Input Models, Layouts, Filters,
     * Import/Export Descriptions and so on).</p>
     */
    public IUserdataManager getUserdataManager()
        throws TermbaseException
    {
        try
        {
            IUserdataManager result = new UserdataManager(m_termbase, m_session);

            return result;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INTERNAL_ERROR, null, e);
        }
    }

    /**
     * <p>Allocates an Index Manager for (re-)indexing the termbase.</p>
     */
    public IIndexManager getIndexer()
        throws TermbaseException
    {
        try
        {
            IIndexManager result = new IndexManager(m_termbase, m_session);

            m_termbase.setIndexer(result);

            return result;
        }
        catch (Exception e)
        {
            throw new TermbaseException (MSG_INDEXING_IN_PROGRESS, null, e);
        }
    }
    
    public ITermbaseMaintance getTbMaintance(SearchReplaceParams params) {
        int level = params.getLevelCode();
        ITermbaseMaintance tm = null;
        
        switch (level)
        {
            case SearchReplaceConstants.LEVEL_CONCEPT:
                tm = new TbConceptMaintance(params, m_termbase);
                break;
            case SearchReplaceConstants.LEVEL_LANGUAGE:
                tm = new TbLanguageMaintance(params, m_termbase);
                break;
            case SearchReplaceConstants.LEVEL_TERM:
                tm = new TermMaintance(params, m_termbase);
                break;
        }
        
        return tm;
    }
}

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

import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
// Should be moved to c.g.indexer.IIndexManager so TM can use it.
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.searchreplace.ITermbaseMaintance;
import com.globalsight.terminology.searchreplace.SearchReplaceParams;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

/**
 * <p>The RMI interface for a Terminology Database.</p>
 */
public interface ITermbase
    extends Remote
{
    static final int DIRECTION_UP = 0;
    static final int DIRECTION_DOWN = 1;

    // static final int FUZZY_LENGTH_WEIGHTED = 0;
    // static final int FUZZY_RELEVANCE_WEIGHTED = 1;

    /**
     * User name to use when connecting to a termbase from within
     * system 4, i.e. during import.
     */
    static final public String SYSTEM_USER = "system4";
    static final public String ANONYMOUS_USER = "guest";

    /** Retrieves the termbase name */
    String getName()
        throws TermbaseException, RemoteException;

    /**
     * <p>Retrieves the Entry Structure Schema used in the
     * termbase.</p>
     *
     * @return an XML string specifying the schema.
     * @see schema.dtd
     */
    String getSchema()
        throws TermbaseException, RemoteException;

    /**
     * <p>Retrieves the termbase definition containing the languages,
     * text fields, and attribute fields.</p>
     *
     * @return an XML string specifying the termbase definition.
     */
    String getDefinition()
        throws TermbaseException, RemoteException;

    /**
     * Given a language name, returns that language's locale as string.
     */
    String getLocaleByLanguage(String p_language)
        throws TermbaseException, RemoteException;

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
    ArrayList getLanguagesByLocale(String p_locale)
        throws TermbaseException, RemoteException;

    /**
     * Retrieves a string containing termbase statistics.
     *
     * @return an xml string of the form
     *   <statistics>
     *     <termbase>termbase name</termbase>
     *     <entries>number of entries</entries>
     *     <indexes>
     *       <index>
     *         <language>index name</language>
     *         <entries>number of terms</entries>
     *       </index>
     *     </indexes>
     *   </statistics>
     */
    String getStatistics()
        throws TermbaseException, RemoteException;
    
    public String getStatisticsWithoutIndexInfo() throws TermbaseException;

    /**
     * <p>Retrieves an entry as xml string conforming to the Entry
     * Structure Schema.</p>
     *
     * @return the empty string if the entry does not exist, else an
     * XML string with root "conceptGrp".
     */
    String getEntry(long entryID)
        throws TermbaseException, RemoteException;

    /**
     * <p>Retrieves an entry as xml string conforming to the Entry
     * Structure Schema, with source/target language group and source
     * term specially marked for easy formatting in the viewer.</p>
     *
     * @return the empty string if the entry does not exist, else an
     * XML string with root "conceptGrp".
     */
    String getEntry(long entryID, long termId,
        String sourceLanguage, String targetLanguage)
        throws TermbaseException, RemoteException;
    
    String getEntryForBrowser(long entryID)
            throws TermbaseException, RemoteException;

    /**
     * Adds a new entry to the termbase.
     *
     * @return the newly assigned entry ID as long.
     */
    long addEntry(String entry)
        throws TermbaseException, RemoteException;

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
    String lockEntry(long entryId, boolean steal)
        throws TermbaseException, RemoteException;

    /**
     * <p>Unlocks a previously locked entry.  The cookie must have
     * been obtained by calling lockEntry() or the call will fail.</p>
     */
    void unlockEntry(long entryId, String cookie)
        throws TermbaseException, RemoteException;

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
    String getLockInfo(long entryID)
        throws TermbaseException, RemoteException;

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
    void updateEntry(long entryID, String entry, String cookie)
        throws TermbaseException, RemoteException;

    /** Deletes an entry from the termbase. */
    void deleteEntry(long entryID)
        throws TermbaseException, RemoteException;

    /**
     * Validates an entry by comparing all terms against the termbase
     * and checking for duplicates or near duplicates.
     */
    String validateEntry(String entry)
        throws TermbaseException, RemoteException;

    /**
     * <p>Searches a given language for the given expression.  A
     * hitlist is returned listing the terms found in the termbase,
     * their score, and their entry number. For normal (alphabetical)
     * searches, the hitlist starts with the first term that is equal
     * or greater to the search term.</p>
     *
     * @param index: a valid index name in the termbase
     *
     * @param query: a search expression containing
     *     '?' for any single character
     *     '*' for any number of characters,
     *     '[a-z]' limited regular expressions as
     *         implemented by the SQL database
     *     - or -
     *     '#' to start a fuzzy search
     *     - or -
     *     '!' to start an exact match search
     *
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
    String search(String language, String target_lan, String query,
            String queryType, int maxHits, int begin) throws TermbaseException,
            RemoteException;

    /**
     * <p>Searches a given fuzzy index for the given expression.</p>
     */
    String fuzzySearch(String language, String query, int maxHits)
        throws TermbaseException, RemoteException;

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
    Hitlist recognizeTerms(String language, String query, int maxHits)
        throws TermbaseException, RemoteException;

    /**
     * <p>Allocates an Import Manager for importing entries from a
     * file.</p>
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
    IImportManager getImporter()
        throws TermbaseException, RemoteException;

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
    IExportManager getExporter()
        throws TermbaseException, RemoteException;

    /**
     * <p>Allocates a User Data Manager for storing and retrieving
     * user-related data (Input Models, Layouts, Filters,
     * Import/Export Descriptions and so on).</p>
     */
    IUserdataManager getUserdataManager()
        throws TermbaseException, RemoteException;

    /**
     * <p>Hires an Index Manager for (re-)indexing the termbase.</p>
     */
    IIndexManager getIndexer()
        throws TermbaseException, RemoteException;
    
    public ITermbaseMaintance getTbMaintance(SearchReplaceParams params);
}

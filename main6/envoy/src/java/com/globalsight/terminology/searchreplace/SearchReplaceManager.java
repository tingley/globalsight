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
package com.globalsight.terminology.searchreplace;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.Termbase.Statements;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.terminology.util.XmlParser;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.CaseUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

// TODO: handle system-defined fields - exclude them from search/replace
// or allow them to be updated.
//
// concept: domain, project, status
// term: type, status
//
// TODO: handle modification timestamp and user.
//
// TODO: in replaceAll() divide the intervals properly so a progress
// message can be sent after READING and after WRITING with constant
// feedback to the user.
//
// TODO: when reading window data from cached TIDs or LIDs, keep in
// mind entries can be overwritten and IDs reassigned by the time the
// data is retrieved. IDs can also be deleted. The window size should
// be updated.

public class SearchReplaceManager
    implements ISearchReplaceManager,
               SearchReplaceConstants,
               TermbaseExceptionMessages
{
    static private final GlobalSightCategory CATEGORY =
       (GlobalSightCategory)GlobalSightCategory.getLogger(
           SearchReplaceManager.class);

    private Termbase m_termbase;
    private SessionInfo m_session;
    private ProcessStatus m_listener;
    private SearchReplaceParams m_params;
    private SearchResults m_results;
    private static int searchCounter = 0;
    private static int replaceCounter = 0;
    private static int replaceAllCounter = 0;

    //
    // Constructor
    //

    public SearchReplaceManager(Termbase p_termbase, SessionInfo p_session)
    {
        m_termbase = p_termbase;
        m_session = p_session;
    }

    public void attachListener(IProcessStatusListener p_listener)
    {
        m_listener = (ProcessStatus)p_listener;
    }

    public void detachListener(IProcessStatusListener p_listener)
    {
        m_listener = null;
    }

    /**
     * Searches using the provided parameters, which specify the
     * field(s) to search on, and the search text.
     *
     * A closure encapsulating the entries fulfilling the
     * search request is stored in the status object and 
     * a window of N entries to be shown to the
     * user.
     */
    public void search(SearchReplaceParams p_params)
        throws TermbaseException
    {
        m_params = p_params;
        doSearch();
    }

    /**
     * After a search, retrieves the next N results.
     */
    public SearchResults getNextResults()
        throws TermbaseException
    {
        setNextWindow();
        setWindowData();

        return m_results;
    }

    /**
     * After a search, retrieves the previous N results.
     */
    public SearchResults getPreviousResults()
        throws TermbaseException
    {
        setPreviousWindow();
        setWindowData();

        return m_results;
    }

    /**
     * Replaces strings in a search when the entries are shown in a UI
     * and only a few of them are selected; ReplaceParams contains the
     * IDs of entries to replace in.
     */
    public void replace(SearchReplaceParams p_params)
        throws TermbaseException
    {
        // Should ensure that only the replace string potentially changes.
        m_params = p_params;

        if (m_params.getReplaceIndexes().size() == 0)
        {
            m_listener.setResults(m_results);
        }

        try
        {
            doReplace();
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Error during replace", ex);
            throw new TermbaseException(MSG_INTERNAL_ERROR, null,
                // gaah, constructor doesn't take in Throwable
                new Exception(ex));
        }
    }

    /**
     * Replaces strings in all search results and outputs progress
     * status through the listener.
     */
    public void replaceAll(SearchReplaceParams p_params)
        throws TermbaseException
    {
        m_params = p_params;

        try
        {
            doReplaceAll();
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Error during replace all", ex);
            throw new TermbaseException(MSG_INTERNAL_ERROR, null,
                // gaah, constructor doesn't take in Throwable
                new Exception(ex));
        }
    }

    //
    // Private Methods -- Search
    //

    /** Does search in a separate thread */
    private void doSearch()
        throws TermbaseException
    {
        Runnable runnable = new Runnable() 
        {
            public void run()
            {
                try
                {
                    runSearch();
                }
                catch(Exception e)
                {
                    CATEGORY.error("SeachReplaceManager::runSearch", e);
                }
            }
        };

        Thread t = new Thread(runnable);
        t.setName("SEARCHER" + String.valueOf(searchCounter++));
        t.start();
    }

    /** This method searches according to
     *  the parameters provided 
     */
    private void runSearch()
        throws TermbaseException
    {
        int level = m_params.getLevelCode();

        switch (level)
        {
        case LEVEL_ENTRY:
            m_results = doSearchEntry();
            break;
        case LEVEL_CONCEPT:
            m_results = doSearchConcept();
            break;
        case LEVEL_LANGUAGE:
            m_results = doSearchLanguage();
            break;
        case LEVEL_TERM:
            m_results = doSearchTerm();
            break;
        default:
            // return an empty result but bump up progress bar
            m_results = doNoSearch();
            break;
        }

        // Need to initialize window settings.
        getNextResults();

        m_listener.setResults(m_results);
    }

    /** Does replace in a separate thread */
    private void doReplace()
        throws TermbaseException
    {
        Runnable runnable = new Runnable() 
        {
            public void run()
            {
                try
                {
                    runReplace();
                }
                catch(Exception e)
                {
                    CATEGORY.error("SearchReplaceManager::runReplace", e);
                }
            }
        };

        Thread t = new Thread(runnable);
        t.setName("REPLACER" + String.valueOf(replaceCounter++));
        t.start();
    }

    /* Replaces all the entries in a separate thread */
    private void doReplaceAll()
        throws TermbaseException
    {
        Runnable runnable = new Runnable() 
        {
            public void run()
            {
                try
                {
                    runReplaceAll();
                }
                catch(Exception e)
                {
                    CATEGORY.error("SearchReaplceManager::runReplaceAll", e);
                }
            }
        };

        Thread t = new Thread(runnable);
        t.setName("ALLREPLACER" + String.valueOf(replaceAllCounter++));
        t.start();
    }

    /**
     * Empty method that does not search but updates the progress bar.
     */
    private SearchResults doNoSearch()
    {
        SearchResults result = new SearchResults(m_params);

        // We're done, bump progress bar to 100%.
        String message = m_listener.getStringFromBundle("lb_term_search_non", "No search was performed.");

        try
        {
            speak(0, 100, message);
        }
        catch (Throwable ignore) {}

        return result;
    }

    /**
     * Entry-level search, all languages, all terms.
     */
    private SearchResults doSearchEntry()
        throws TermbaseException
    {
        return doNoSearch();
    }

    /**
     * Concept-level search.
     */
    private SearchResults doSearchConcept()
        throws TermbaseException
    {
        CATEGORY.info("Begin concept-level search in termbase " +
            m_termbase.getName() + " for " + m_params.getSearchText());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        int counter = 0;
        int expectedEntries = 0;

        SearchResults result = new SearchResults(m_params);

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                // Read number of entries to search
                rset = stmt.executeQuery(
                    "select count(Cid) from TB_CONCEPT " +
                    "where TBid=" + m_termbase.getId());

                rset.next();
                expectedEntries = rset.getInt(1);
                rset.close();

                // Read all concept-level fields
                rset = stmt.executeQuery(
                    "select Cid, XML from TB_CONCEPT " +
                    "where TBid=" + m_termbase.getId());

                try
                {
                    RsetHolder rsetHolder = new RsetHolder(rset);
                    String message;

                    while (rset.next())
                    {
                        ++counter;
                        message = "";

                        if (matchConceptRow(rsetHolder))
                        {
                            if (entryIsLocked(rsetHolder))
                            {
                                message = "entry " + rsetHolder.m_entryId +
                                    " is locked, ignoring...";
                            }
                            else
                            {
                                addConceptRow(result, rsetHolder);
                            }
                        }

                        showStatus(counter, expectedEntries, "");
                    }
                }
                catch (IOException ignore)
                {
                    CATEGORY.info("client cancelled search");
                }
                catch (Throwable ignore)
                {
                    CATEGORY.error("unexpected error, aborting search\n" +
                        GeneralException.getStackTraceString(ignore));
                }
                finally
                {
                    // We're done, bump progress bar to 100%.
//                    String message = "Search finished, " + counter +
//                        " candidates processed, " +
//                        result.getNumResults() + " hit" +
//                        (result.getNumResults() == 1 ? "" : "s");
                    String pattern = "Search finished, {0} candidates processed, {1} hit(s)";
                    String message = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                            "lb_term_search_result_pattern", pattern, counter, result.getNumResults());
                    try
                    {
                        speak(counter, 100, message);
                    }
                    catch (Throwable ignore) {}
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        CATEGORY.info("End concept-level search in termbase " +
            m_termbase.getName());

        return result;
    }

    /**
     * Language-level search.
     */
    private SearchResults doSearchLanguage()
        throws TermbaseException
    {
        CATEGORY.info("Begin language-level search in termbase " +
            m_termbase.getName() + " for " + m_params.getSearchText());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        int counter = 0;
        int expectedEntries = 0;

        SearchResults result = new SearchResults(m_params);

        String language = m_params.getLanguage();

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                // Read number of entries to search
                rset = stmt.executeQuery(
                    "select count(Lid) from TB_LANGUAGE " +
                    "where TBid=" + m_termbase.getId() +
                    " and Name='" + SqlUtil.quote(language) + "'");

                rset.next();
                expectedEntries = rset.getInt(1);
                rset.close();

                // Read all language fields in the search language
                rset = stmt.executeQuery(
                    "select Cid, Lid, XML from TB_LANGUAGE " +
                    "where TBid=" + m_termbase.getId() +
                    " and Name='" + SqlUtil.quote(language) + "'");

                try
                {
                    RsetHolder rsetHolder = new RsetHolder(rset);
                    String message;

                    while (rset.next())
                    {
                        ++counter;
                        message = "";

                        if (matchLanguageRow(rsetHolder))
                        {
                            if (entryIsLocked(rsetHolder))
                            {
                                message = "entry " + rsetHolder.m_entryId +
                                    " is locked, ignoring...";
                            }
                            else
                            {
                                addLanguageRow(result, rsetHolder);
                            }
                        }

                        showStatus(counter, expectedEntries, "");
                    }
                }
                catch (IOException ignore)
                {
                    CATEGORY.info("client cancelled search");
                }
                catch (Throwable ignore)
                {
                    CATEGORY.error("unexpected error, aborting search\n" +
                        GeneralException.getStackTraceString(ignore));
                }
                finally
                {
                    // We're done, bump progress bar to 100%.
//                    String message = "Search finished, " + counter +
//                        " candidates processed, " +
//                        result.getNumResults() + " hit" +
//                        (result.getNumResults() == 1 ? "" : "s");
                    String pattern = "Search finished, {0} candidates processed, {1} hit(s)";
                    String message = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                            "lb_term_search_result_pattern", pattern, counter, result.getNumResults());
                    try
                    {
                        speak(counter, 100, message);
                    }
                    catch (Throwable ignore) {}
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        CATEGORY.info("End language-level search in termbase " +
            m_termbase.getName());

        return result;
    }

    /**
     * Term-level search.
     */
    private SearchResults doSearchTerm()
        throws TermbaseException
    {
        CATEGORY.info("Begin term-level search in termbase " +
            m_termbase.getName() + " for " + m_params.getSearchText());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        int counter = 0;
        int expectedEntries = 0;

        SearchResults result = new SearchResults(m_params);

        String language = m_params.getLanguage();

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                // Read number of entries to search
                rset = stmt.executeQuery(
                    "select count(Tid) from TB_TERM " +
                    "where TBid=" + m_termbase.getId() +
                    " and Lang_Name='" + SqlUtil.quote(language) + "'");

                rset.next();
                expectedEntries = rset.getInt(1);
                rset.close();

                // Read all term fields in the search language
                rset = stmt.executeQuery(
                    "select Cid, Tid, XML, Term from TB_TERM " +
                    "where TBid=" + m_termbase.getId() +
                    " and Lang_Name='" + SqlUtil.quote(language) + "'");

                try
                {
                    RsetHolder rsetHolder = new RsetHolder(rset);
                    String message;

                    while (rset.next())
                    {
                        ++counter;
                        message = "";

                        if (matchTermRow(rsetHolder))
                        {
                            if (entryIsLocked(rsetHolder))
                            {
                                message = "entry " + rsetHolder.m_entryId +
                                    " is locked, ignoring...";
                            }
                            else
                            {
                                addTermRow(result, rsetHolder);
                            }
                        }

                        showStatus(counter, expectedEntries, "");
                    }
                }
                catch (IOException ignore)
                {
                    CATEGORY.info("client cancelled search");
                }
                catch (Throwable ignore)
                {
                    CATEGORY.error("unexpected error, aborting search\n" +
                        GeneralException.getStackTraceString(ignore));
                }
                finally
                {
                    // We're done, bump progress bar to 100%.
//                    String message = "Search finished, " + counter +
//                        " candidates processed, " +
//                        result.getNumResults() + " hit" +
//                        (result.getNumResults() == 1 ? "" : "s");
                    String pattern = "Search finished, {0} candidates processed, {1} hit(s)";
                    String message = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                            "lb_term_search_result_pattern", pattern, counter, result.getNumResults());
                    try
                    {
                        speak(counter, 100, message);
                    }
                    catch (Throwable ignore) {}
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        CATEGORY.info("End term-level search in termbase " +
            m_termbase.getName());

        return result;
    }

    private boolean matchConceptRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        Long entryId = new Long(p_rset.m_rset.getLong("CID"));
        p_rset.setEntryId(entryId);

        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);

        if (!quickMatch(xml))
        {
            return false;
        }

        Document dom = parseXml(getConceptGrpXml(p_rset));
        p_rset.setDom(dom);

        return fieldMatches(dom.getRootElement());
    }

    private boolean matchLanguageRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        Long entryId = new Long(p_rset.m_rset.getLong("CID"));
        p_rset.setEntryId(entryId);

        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);

        if (!quickMatch(xml))
        {
            return false;
        }

        Document dom = parseXml(getLanguageGrpXml(p_rset));
        p_rset.setDom(dom);

        return fieldMatches(dom.getRootElement());
    }

    private boolean matchTermRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        Long entryId = new Long(p_rset.m_rset.getLong("CID"));
        p_rset.setEntryId(entryId);

        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);

        if (!quickMatch(xml))
        {
            return false;
        }

        Document dom = parseXml(getTermGrpXml(p_rset));
        p_rset.setDom(dom);

        return fieldMatches(dom.getRootElement());
    }

    private boolean fieldMatches(Element p_elem)
    {
        boolean result = false;
        List children = p_elem.elements();

        for (int i = 0, max = children.size(); i < max && !result; i++)
        {
            Element child = (Element)children.get(i);
            String childName = child.getName();

            if (childName.startsWith("descripGrp"))
            {
                result |= descripMatches(child);
            }
            else if (childName.startsWith("sourceGrp"))
            {
                result |= sourceMatches(child);
            }
            else if (childName.startsWith("noteGrp"))
            {
                result |= noteMatches(child);
            }
        }

        return result;
    }

    private boolean descripMatches(Element p_elem)
    {
        boolean result = false;
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLATTR:
            // TODO: distinguish between text and attribute fields
        case FIELD_ALL:
            Element descrip = (Element)p_elem.selectSingleNode("descrip");
            String name = descrip.attributeValue("type");

            if (fieldName != null && !fieldName.equalsIgnoreCase(name))
            {
                break;
            }

            result = valueMatches(descrip);
            break;

        case FIELD_ALLSOURCES:
        case FIELD_ALLNOTES:
            break;
        }

        // Search sources & notes
        if (!result)
        {
            List children = p_elem.elements();

            for (int i = 0, max = children.size(); i < max && !result; i++)
            {
                Element child = (Element)children.get(i);
                String childName = child.getName();

                if (childName.startsWith("sourceGrp"))
                {
                    result |= sourceMatches(child);
                }
                else if (childName.startsWith("noteGrp"))
                {
                    result |= noteMatches(child);
                }
            }
        }

        return result;
    }

    private boolean sourceMatches(Element p_elem)
    {
        boolean result = false;
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLSOURCES:
        case FIELD_ALL:
            Element source = (Element)p_elem.selectSingleNode("source");

            if (fieldName != null && !fieldName.equalsIgnoreCase("source"))
            {
                break;
            }

            result = valueMatches(source);
            break;

        case FIELD_ALLATTR:
        case FIELD_ALLNOTES:
            break;
        }

        // search notes
        if (!result)
        {
            List children = p_elem.elements();

            for (int i = 0, max = children.size(); i < max && !result; i++)
            {
                Element child = (Element)children.get(i);
                String childName = child.getName();

                if (childName.startsWith("noteGrp"))
                {
                    result |= noteMatches(child);
                }
            }
        }

        return result;
    }

    private boolean noteMatches(Element p_elem)
    {
        boolean result = false;
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLSOURCES:
        case FIELD_ALL:
            Element note = (Element)p_elem.selectSingleNode("note");

            if (fieldName != null && !fieldName.equalsIgnoreCase("note"))
            {
                break;
            }

            result = valueMatches(note);
            break;

        case FIELD_ALLATTR:
        case FIELD_ALLNOTES:
            break;
        }

        return result;
    }

    private boolean valueMatches(Element p_elem)
    {
        String value = EntryUtils.getInnerText(p_elem);
        String search = m_params.getSearchText();
        boolean caseInsensitive = m_params.isCaseInsensitive();

        if (caseInsensitive)
        {
            if (value.toLowerCase().indexOf(search.toLowerCase()) >= 0)
            {
                return true;
            }
        }
        else
        {
            if (value.indexOf(search) >= 0)
            {
                return true;
            }
        }

        return false;
    }

    private void addConceptRow(SearchResults p_result, RsetHolder p_rset)
        throws SQLException
    {
        p_result.addEntryId(p_rset.m_entryId);
        p_result.addLevelId(p_rset.m_entryId);

        // Cache a few results so the user gets them quickly
        if (p_result.canAddLevelDom())
        {
            p_result.addLevelDom(p_rset.m_dom);
        }
    }

    private void addLanguageRow(SearchResults p_result, RsetHolder p_rset)
        throws SQLException
    {
        p_result.addEntryId(p_rset.m_entryId);
        p_result.addLevelId(new Long(p_rset.m_rset.getLong("LID")));

        // Cache a few results so the user gets them quickly
        if (p_result.canAddLevelDom())
        {
            p_result.addLevelDom(p_rset.m_dom);
        }
    }

    private void addTermRow(SearchResults p_result, RsetHolder p_rset)
        throws SQLException
    {
        p_result.addEntryId(p_rset.m_entryId);
        p_result.addLevelId(new Long(p_rset.m_rset.getLong("TID")));

        // Cache a few results so the user gets them quickly
        if (p_result.canAddLevelDom())
        {
            p_result.addLevelDom(p_rset.m_dom);
        }
    }

    //
    // Private Methods -- Replace
    //

    private void runReplace()
        throws TermbaseException
    {
        String search = m_params.getSearchText();
        String replace = m_params.getReplaceText();
        boolean caseInsensitive = m_params.isCaseInsensitive();
        boolean smartReplace = m_params.isSmartReplace();

        CaseUtil replacer = new CaseUtil(search, replace,
            caseInsensitive, smartReplace);

        int level = m_params.getLevelCode();

        switch (level)
        {
        case LEVEL_ENTRY:
            m_results = doReplaceEntry(replacer);
            break;
        case LEVEL_CONCEPT:
            m_results = doReplaceConcept(replacer);
            break;
        case LEVEL_LANGUAGE:
            m_results = doReplaceLanguage(replacer);
            break;
        case LEVEL_TERM:
            m_results = doReplaceTerm(replacer);
            break;
        default:
            break;
        }

        // Need to reset the window settings.
        resetWindow();
        getNextResults();

        m_listener.setResults(m_results);
    }

    private SearchResults doReplaceEntry(CaseUtil p_replacer)
    {
        CATEGORY.info("Begin entry-level replace in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        CATEGORY.info("End entry-level replace in termbase " +
            m_termbase.getName());

        return m_results;
    }

    private SearchResults doReplaceConcept(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin concept-level replace in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        // Indexes are 0-based and index the dom list.
        ArrayList indexes = m_params.getReplaceIndexes();
        int start = m_results.getWindowStart();

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();
        ArrayList doms = m_results.getWindowDom();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            for (int i = 0, max = indexes.size(); i < max; i++)
            {
                Long index = (Long)indexes.get(i);

                Long entryId = (Long)entryIds.get(start + index.intValue());
                Long levelId = (Long)levelIds.get(start + index.intValue());
                Document dom = (Document)doms.get(index.intValue());
                Element root = dom.getRootElement();

                // Unlock any locks held by users (bummer, admin wins).
                m_termbase.unlockEntryInternal(entryId);

                // Replace search text with replace text.
                replaceField(root, p_replacer);

                StringBuffer temp = new StringBuffer();
                for (Iterator it = root.elementIterator(); it.hasNext(); )
                {
                    Element elmt = (Element)it.next();
                    // String name = elmt.getName();
                    // On concept level, all elements get added.

                    temp.append(elmt.asXML());
                }

                String xml = temp.toString();
                boolean needClob = false;

                String statement = "UPDATE TB_CONCEPT " +
                    "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                    " where tbid=" + m_termbase.getId() +
                    "   and  cid=" + levelId;

                statements.addConceptStatement(statement);
            }

            // Update the database. This may throw a TermbaseException
            // in which case we don't touch the original results.
            m_termbase.executeStatements(statements);
        }
        finally
        {
            m_termbase.releaseReader();
        }

        updateResults();

        CATEGORY.info("End concept-level replace in termbase " +
            m_termbase.getName());

        return m_results;
    }

    private SearchResults doReplaceLanguage(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin language-level replace in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        // Indexes are 0-based and index the dom list.
        ArrayList indexes = m_params.getReplaceIndexes();
        int start = m_results.getWindowStart();

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();
        ArrayList doms = m_results.getWindowDom();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            for (int i = 0, max = indexes.size(); i < max; i++)
            {
                Long index = (Long)indexes.get(i);

                Long entryId = (Long)entryIds.get(start + index.intValue());
                Long levelId = (Long)levelIds.get(start + index.intValue());
                Document dom = (Document)doms.get(index.intValue());
                Element root = dom.getRootElement();

                // Unlock any locks held by users (bummer, admin wins).
                m_termbase.unlockEntryInternal(entryId);

                // Replace search text with replace text.
                replaceField(root, p_replacer);

                StringBuffer temp = new StringBuffer();
                for (Iterator it = root.elementIterator(); it.hasNext(); )
                {
                    Element elmt = (Element)it.next();
                    String name = elmt.getName();

                    if (!name.equals("language"))
                    {
                        temp.append(elmt.asXML());
                    }
                }

                String xml = temp.toString();
                boolean needClob = false;

                String statement = "UPDATE TB_LANGUAGE " +
                    "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                    " where tbid=" + m_termbase.getId() +
                    "   and  lid=" + levelId;

                statements.addLanguageStatement(statement);
            }

            // Update the database. This may throw a TermbaseException
            // in which case we don't touch the original results.
            m_termbase.executeStatements(statements);
        }
        finally
        {
            m_termbase.releaseReader();
        }

        updateResults();

        CATEGORY.info("End language-level replace in termbase " +
            m_termbase.getName());

        return m_results;
    }

    private SearchResults doReplaceTerm(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin term-level replace in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        // Indexes are 0-based and index the dom list.
        ArrayList indexes = m_params.getReplaceIndexes();
        int start = m_results.getWindowStart();

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();
        ArrayList doms = m_results.getWindowDom();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            for (int i = 0, max = indexes.size(); i < max; i++)
            {
                Long index = (Long)indexes.get(i);

                Long entryId = (Long)entryIds.get(start + index.intValue());
                Long levelId = (Long)levelIds.get(start + index.intValue());
                Document dom = (Document)doms.get(index.intValue());
                Element root = dom.getRootElement();

                // Unlock any locks held by users (bummer, admin wins).
                m_termbase.unlockEntryInternal(entryId);

                // Replace search text with replace text.
                replaceField(root, p_replacer);

                StringBuffer temp = new StringBuffer();
                for (Iterator it = root.elementIterator(); it.hasNext(); )
                {
                    Element elmt = (Element)it.next();
                    String name = elmt.getName();

                    if (!name.equals("term"))
                    {
                        temp.append(elmt.asXML());
                    }
                }

                String xml = temp.toString();
                boolean needClob = false;

                String statement = "UPDATE TB_TERM " +
                    "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                    " where tbid=" + m_termbase.getId() +
                    "   and  tid=" + levelId;

                statements.addTermStatement(statement);
            }

            // Update the database. This may throw a TermbaseException
            // in which case we don't touch the original results.
            m_termbase.executeStatements(statements);
        }
        finally
        {
            m_termbase.releaseReader();
        }

        updateResults();

        CATEGORY.info("End term-level replace in termbase " +
            m_termbase.getName());

        return m_results;
    }

    private void replaceField(Element p_elem, CaseUtil p_replacer)
    {
        List children = p_elem.elements();

        for (int i = 0, max = children.size(); i < max; i++)
        {
            Element child = (Element)children.get(i);
            String childName = child.getName();

            if (childName.startsWith("descripGrp"))
            {
                replaceDescrip(child, p_replacer);
            }
            else if (childName.startsWith("sourceGrp"))
            {
                replaceSource(child, p_replacer);
            }
            else if (childName.startsWith("noteGrp"))
            {
                replaceNote(child, p_replacer);
            }
        }
    }

    private void replaceDescrip(Element p_elem, CaseUtil p_replacer)
    {
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLATTR:
            // TODO: distinguish between text and attribute fields
        case FIELD_ALL:
            Element descrip = (Element)p_elem.selectSingleNode("descrip");
            String name = descrip.attributeValue("type");

            if (fieldName != null && !fieldName.equalsIgnoreCase(name))
            {
                break;
            }

            replaceValue(descrip, p_replacer);
            break;

        case FIELD_ALLSOURCES:
        case FIELD_ALLNOTES:
            break;
        }

        // Search in sources & notes
        List children = p_elem.elements();

        for (int i = 0, max = children.size(); i < max; i++)
        {
            Element child = (Element)children.get(i);
            String childName = child.getName();

            if (childName.startsWith("sourceGrp"))
            {
                replaceSource(child, p_replacer);
            }
            else if (childName.startsWith("noteGrp"))
            {
                replaceNote(child, p_replacer);
            }
        }
    }

    private void replaceSource(Element p_elem, CaseUtil p_replacer)
    {
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLSOURCES:
        case FIELD_ALL:
            Element source = (Element)p_elem.selectSingleNode("source");

            if (fieldName != null && !fieldName.equalsIgnoreCase("source"))
            {
                break;
            }

            replaceValue(source, p_replacer);
            break;

        case FIELD_ALLATTR:
        case FIELD_ALLNOTES:
            break;
        }

        // search notes
        List children = p_elem.elements();

        for (int i = 0, max = children.size(); i < max; i++)
        {
            Element child = (Element)children.get(i);
            String childName = child.getName();

            if (childName.startsWith("noteGrp"))
            {
                replaceNote(child, p_replacer);
            }
        }
    }

    private void replaceNote(Element p_elem, CaseUtil p_replacer)
    {
        String fieldName = null;

        // look at the selected fields and find the search string in it
        int code = m_params.getFieldCode();

        switch (code)
        {
        case FIELD_SPECIFICFIELD:
            fieldName = m_params.getFieldName();
            // fall through
        case FIELD_ALLTEXT:
        case FIELD_ALLSOURCES:
        case FIELD_ALL:
            Element note = (Element)p_elem.selectSingleNode("note");

            if (fieldName != null && !fieldName.equalsIgnoreCase("note"))
            {
                break;
            }

            replaceValue(note, p_replacer);
            break;

        case FIELD_ALLATTR:
        case FIELD_ALLNOTES:
            break;
        }
    }

    private void replaceValue(Element p_elem, CaseUtil p_replacer)
    {
        List content = p_elem.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            if (node.getNodeType() == Node.TEXT_NODE)
            {
                node.setText(p_replacer.smartReplace(node.getText()));
            }
        }
    }

    //
    // Private Methods -- runReplaceAll
    //

    private void runReplaceAll()
        throws TermbaseException
    {
        String search = m_params.getSearchText();
        String replace = m_params.getReplaceText();
        boolean caseInsensitive = m_params.isCaseInsensitive();
        boolean smartReplace = m_params.isSmartReplace();

        CaseUtil replacer = new CaseUtil(search, replace,
            caseInsensitive, smartReplace);

        int level = m_params.getLevelCode();

        switch (level)
        {
        case LEVEL_ENTRY:
            doReplaceAllEntry(replacer);
            break;
        case LEVEL_CONCEPT:
            doReplaceAllConcept(replacer);
            break;
        case LEVEL_LANGUAGE:
            doReplaceAllLanguage(replacer);
            break;
        case LEVEL_TERM:
            doReplaceAllTerm(replacer);
            break;
        default:
            // return an empty result but bump up progress bar
            doNoReplaceAll();
            break;
        }
    }

    private void doNoReplaceAll()
    {
        // We're done, bump progress bar to 100%.
        String message = m_listener.getStringFromBundle("lb_term_search_replace_non", "No replace was performed.");

        try
        {
            speak(0, 100, message);
        }
        catch (Throwable ignore) {}
    }

    /**
     * Entry-level replace all.
     */
    private void doReplaceAllEntry(CaseUtil p_replacer)
    {
        doNoReplaceAll();
    }

    /**
     * Concept-level replace all.
     */
    private void doReplaceAllConcept(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin concept-level replace-all in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();

        int counter = 0;
        int expectedEntries = entryIds.size();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            // Load and process 100 entries at a time.
            final int chunkSize = 100;

            for (int chunk = 0; chunk < expectedEntries; chunk += chunkSize)
            {
                List tempEntryIds = entryIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));
                List tempLevelIds = levelIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));

                ArrayList doms = readConceptData(tempLevelIds);

                // count "half" the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");

                for (int i = 0, max = doms.size(); i < max; i++)
                {
                    Long entryId = (Long)entryIds.get(i);
                    Long levelId = (Long)levelIds.get(i);
                    Document dom = (Document)doms.get(i);
                    Element root = dom.getRootElement();

                    // Unlock any locks held by users (bummer, admin wins).
                    m_termbase.unlockEntryInternal(entryId);

                    // Replace search text with replace text.
                    replaceField(root, p_replacer);

                    StringBuffer temp = new StringBuffer();
                    for (Iterator it = root.elementIterator(); it.hasNext(); )
                    {
                        Element elmt = (Element)it.next();
                        // String name = elmt.getName();
                        // On concept level, all elements get added.

                        temp.append(elmt.asXML());
                    }

                    String xml = temp.toString();

                    boolean needClob = false;

                    String statement = "UPDATE TB_CONCEPT " +
                        "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                        " where tbid=" + m_termbase.getId() +
                        "   and  cid=" + levelId;

                    statements.addConceptStatement(statement);
                }

                // Update the database. This may throw a TermbaseException.
                m_termbase.executeStatements(statements);

                // count the "other half" of the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");
            }
        }
        catch (IOException ignore)
        {
            CATEGORY.info("client cancelled search");
        }
        catch (Throwable ignore)
        {
            CATEGORY.error("unexpected error, aborting replaceall\n" +
                GeneralException.getStackTraceString(ignore));
        }
        finally
        {
            m_termbase.releaseReader();

            try
            {
                String message = "";
                showProgress(expectedEntries, expectedEntries, message);
            }
            catch (Throwable ignore) {}
        }

        CATEGORY.info("End concept-level replace-all in termbase " +
            m_termbase.getName());
    }

    /**
     * Language-level replace all.
     */
    private void doReplaceAllLanguage(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin language-level replace-all in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();

        int counter = 0;
        int expectedEntries = entryIds.size();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            // Load and process 100 entries at a time.
            final int chunkSize = 100;

            for (int chunk = 0; chunk < expectedEntries; chunk += chunkSize)
            {
                List tempEntryIds = entryIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));
                List tempLevelIds = levelIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));

                ArrayList doms = readLanguageData(tempLevelIds);

                // count "half" the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");

                for (int i = 0, max = doms.size(); i < max; i++)
                {
                    Long entryId = (Long)entryIds.get(i);
                    Long levelId = (Long)levelIds.get(i);
                    Document dom = (Document)doms.get(i);
                    Element root = dom.getRootElement();

                    // Unlock any locks held by users (bummer, admin wins).
                    m_termbase.unlockEntryInternal(entryId);

                    // Replace search text with replace text.
                    replaceField(root, p_replacer);

                    StringBuffer temp = new StringBuffer();
                    for (Iterator it = root.elementIterator(); it.hasNext(); )
                    {
                        Element elmt = (Element)it.next();
                        String name = elmt.getName();

                        if (!name.equals("language"))
                        {
                            temp.append(elmt.asXML());
                        }
                    }

                    String xml = temp.toString();
                    boolean needClob = false;

                    String statement = "UPDATE TB_LANGUAGE " +
                        "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                        " where tbid=" + m_termbase.getId() +
                        "   and  lid=" + levelId;

                    statements.addLanguageStatement(statement);
                }

                // Update the database. This may throw a TermbaseException.
                m_termbase.executeStatements(statements);

                // count the "other half" of the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");
            }
        }
        catch (IOException ignore)
        {
            CATEGORY.info("client cancelled search");
        }
        catch (Throwable ignore)
        {
            CATEGORY.error("unexpected error, aborting replaceall\n" +
                GeneralException.getStackTraceString(ignore));
        }
        finally
        {
            m_termbase.releaseReader();

            try
            {
                String message = "";
                showProgress(expectedEntries, expectedEntries, message);
            }
            catch (Throwable ignore) {}
        }

        CATEGORY.info("End language-level replace-all in termbase " +
            m_termbase.getName());
    }

    /**
     * Term-level replace all.
     */
    private void doReplaceAllTerm(CaseUtil p_replacer)
        throws TermbaseException
    {
        CATEGORY.info("Begin term-level replace-all in termbase " +
            m_termbase.getName() + ": `" + m_params.getSearchText() +
            "' --> `" + m_params.getReplaceText() + "'");

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + m_params.toString());
        }

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();

        int counter = 0;
        int expectedEntries = entryIds.size();

        Statements statements = new Statements();

        m_termbase.addReader();

        try
        {
            // Load and process 100 entries at a time.
            final int chunkSize = 100;

            for (int chunk = 0; chunk < expectedEntries; chunk += chunkSize)
            {
                List tempEntryIds = entryIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));
                List tempLevelIds = levelIds.subList(
                    chunk, Math.min(chunk + chunkSize, expectedEntries));

                ArrayList doms = readTermData(tempLevelIds);

                // count "half" the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");

                for (int i = 0, max = doms.size(); i < max; i++)
                {
                    Long entryId = (Long)entryIds.get(i);
                    Long levelId = (Long)levelIds.get(i);
                    Document dom = (Document)doms.get(i);
                    Element root = dom.getRootElement();

                    // Unlock any locks held by users (bummer, admin wins).
                    m_termbase.unlockEntryInternal(entryId);

                    // Replace search text with replace text.
                    replaceField(root, p_replacer);

                    StringBuffer temp = new StringBuffer();
                    for (Iterator it = root.elementIterator(); it.hasNext(); )
                    {
                        Element elmt = (Element)it.next();
                        String name = elmt.getName();

                        if (!name.equals("term"))
                        {
                            temp.append(elmt.asXML());
                        }
                    }

                    String xml = temp.toString();
                    boolean needClob = false;

                    String statement = "UPDATE TB_TERM " +
                        "set XML=" + SqlUtil.getClobInitializer(xml, needClob) +
                        " where tbid=" + m_termbase.getId() +
                        "   and  tid=" + levelId;

                    statements.addTermStatement(statement);
                }

                // Update the database. This may throw a TermbaseException.
                m_termbase.executeStatements(statements);

                // count the "other half" of the entries
                counter += Math.min(chunkSize/2, (expectedEntries - chunk)/2);
                counter = Math.min(counter, expectedEntries);
                showProgress(counter, expectedEntries, "");
            }
        }
        catch (IOException ignore)
        {
            CATEGORY.info("client cancelled search");
        }
        catch (Throwable ignore)
        {
            CATEGORY.error("unexpected error, aborting replaceall\n" +
                GeneralException.getStackTraceString(ignore));
        }
        finally
        {
            m_termbase.releaseReader();

            try
            {
                String message = "";
                showProgress(expectedEntries, expectedEntries, message);
            }
            catch (Throwable ignore) {}
        }

        CATEGORY.info("End term-level replace-all in termbase " +
            m_termbase.getName());
    }

    //
    // Private Methods
    //

    private String getConceptGrpXml(RsetHolder p_rset)
    {
        StringBuffer result = new StringBuffer();

        result.append("<conceptGrp>\n");
        result.append(p_rset.m_xml);
        result.append("</conceptGrp>\n");

        return result.toString();
    }

    private String getLanguageGrpXml(RsetHolder p_rset)
    {
        StringBuffer result = new StringBuffer();

        result.append("<languageGrp>\n");
        result.append("<language name=\"");
        result.append(EditUtil.encodeXmlEntities(m_params.getLanguage()));
        result.append("\" locale=\"xx_XX\" />\n");
        result.append(p_rset.m_xml);
        result.append("</languageGrp>\n");

        return result.toString();
    }

    private String getTermGrpXml(RsetHolder p_rset)
        throws SQLException
    {
        StringBuffer result = new StringBuffer();

        result.append("<termGrp>\n");
        result.append("<term>");
        result.append(p_rset.m_rset.getString("TERM"));
        result.append("</term>\n");
        result.append(p_rset.m_xml);
        result.append("</termGrp>\n");

        return result.toString();
    }

    /**
     * Performs a quick check on the raw XML string if it may contain
     * the search string.
     */
    private boolean quickMatch(String p_xml)
    {
        if (p_xml == null || p_xml.length() == 0)
        {
            return false;
        }

        // Do a quick check on the full XML string (may be incorrect
        // due to embedded HTML tags but helps performance).
        String search = m_params.getSearchText();
        search = EditUtil.encodeXmlEntities(search);

        if (m_params.isCaseInsensitive())
        {
            if (p_xml.toLowerCase().indexOf(search.toLowerCase()) == -1)
            {
                return false;
            }
        }
        else
        {
            if (p_xml.indexOf(search) == -1)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * This method updates the SearchResults object after some or all
     * entries have been replace and need not be shown any longer.
     */
    private void updateResults()
    {
        ArrayList indexes = m_params.getReplaceIndexes();
        int start = m_results.getWindowStart();

        ArrayList entryIds = m_results.getEntryIds();
        ArrayList levelIds = m_results.getLevelIds();
        ArrayList doms = m_results.getWindowDom();
        ArrayList levelDoms = m_results.getLevelDom();

        ArrayList entryIdsToRemove = new ArrayList();

        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Long index = (Long)indexes.get(i);

            Long entryId = (Long)entryIds.get(start + index.intValue());
            entryIdsToRemove.add(entryId);

            Document dom = (Document)doms.get(index.intValue());
            levelDoms.remove(levelDoms.indexOf(dom));
        }

        // Remove entry and level ids by identity because indexes shift.
        for (int i = 0, max = entryIdsToRemove.size(); i < max; i++)
        {
            Long entryId = (Long)entryIdsToRemove.get(i);
            int index = entryIds.indexOf(entryId);

            entryIds.remove(index);
            levelIds.remove(index);
        }
    }

    private void resetWindow()
    {
        m_results.setWindowEnd(0);
    }

    private void setNextWindow()
    {
        int start = m_results.getWindowStart();
        int end = m_results.getWindowEnd();
        int max = m_results.getNumResults();
        int size = m_results.getWindowSize();

        if (end == 0)
        {
            // window not set yet, start at beginning
            start = 0;
            end = Math.min(size, max);
        }
        else
        {
            start += size;
            end += size;

            // end of data?
            if (end > max)
            {
                start = Math.max(0, max - size);
                end = max;
            }
        }

        m_results.setWindowStart(start);
        m_results.setWindowEnd(end);
    }

    private void setPreviousWindow()
    {
        int start = m_results.getWindowStart();
        int end = m_results.getWindowEnd();
        int max = m_results.getNumResults();
        int size = m_results.getWindowSize();

        if (end == 0)
        {
            // window not set yet, start at beginning
            start = 0;
            end = Math.min(size, max);
        }
        else
        {
            start -= size;
            end -= size;

            // start of data?
            if (start < 0)
            {
                start = 0;
                end = Math.min(size, max);
            }
        }

        m_results.setWindowStart(start);
        m_results.setWindowEnd(end);
    }

    private void setWindowData()
        throws TermbaseException
    {
        int start = m_results.getWindowStart();
        int end = m_results.getWindowEnd();
        int cacheSize = m_results.getLevelDom().size();

        if (end <= cacheSize)
        {
            List levelDom = m_results.getLevelDom().subList(start, end);

            m_results.setWindowDom(levelDom);
        }
        else
        {
            // some data still in cache
            if (start < cacheSize)
            {
                List levelDom = m_results.getLevelDom().subList(
                    start, cacheSize);

                m_results.setWindowDom(levelDom);
                m_results.addWindowDom(readWindowData(cacheSize, end));
            }
            else
            {
                m_results.setWindowDom(readWindowData(start, end));
            }
        }
    }

    private ArrayList readWindowData(int p_start, int p_end)
        throws TermbaseException
    {
        int level = m_params.getLevelCode();

        List levelIds = m_results.getLevelIds().subList(p_start, p_end);

        switch (level)
        {
        case LEVEL_ENTRY:
            // should use entryIds here
            return readEntryData(levelIds);
        case LEVEL_CONCEPT:
            return readConceptData(levelIds);
        case LEVEL_LANGUAGE:
            return readLanguageData(levelIds);
        case LEVEL_TERM:
            return readTermData(levelIds);
        }

        return null;
    }

    private ArrayList readEntryData(List p_levelIds)
        throws TermbaseException
    {
        ArrayList result = new ArrayList();

        return result;
    }

    private ArrayList readConceptData(List p_levelIds)
        throws TermbaseException
    {
        ArrayList result = new ArrayList();

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                rset = stmt.executeQuery(
                    "select XML from TB_CONCEPT " +
                    "where TBid=" + m_termbase.getId() +
                    "  and Cid in " + SqlUtil.getInList(p_levelIds));

                RsetHolder rsetHolder = new RsetHolder(rset);

                while (rset.next())
                {
                    result.add(readConceptRow(rsetHolder));
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        return result;
    }

    private ArrayList readLanguageData(List p_levelIds)
        throws TermbaseException
    {
        ArrayList result = new ArrayList();

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                rset = stmt.executeQuery(
                    "select XML from TB_LANGUAGE " +
                    "where TBid=" + m_termbase.getId() +
                    "  and Lid in " + SqlUtil.getInList(p_levelIds));

                RsetHolder rsetHolder = new RsetHolder(rset);

                while (rset.next())
                {
                    result.add(readLanguageRow(rsetHolder));
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        return result;
    }

    private ArrayList readTermData(List p_levelIds)
        throws TermbaseException
    {
        ArrayList result = new ArrayList();

        m_termbase.addReader();

        try
        {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;

            try
            {
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                rset = stmt.executeQuery(
                    "select XML, TERM from TB_TERM " +
                    "where TBid=" + m_termbase.getId() +
                    "  and Tid in " + SqlUtil.getInList(p_levelIds));

                RsetHolder rsetHolder = new RsetHolder(rset);

                while (rset.next())
                {
                    result.add(readTermRow(rsetHolder));
                }

                conn.commit();
            }
            catch (Exception e)
            {
                try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
                throw new TermbaseException(MSG_SQL_ERROR, null, e);
            }
            finally
            {
                try
                {
                    if (rset != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t) { /* ignore */ }

                SqlUtil.fireConnection(conn);
            }
        }
        finally
        {
            m_termbase.releaseReader();
        }

        return result;
    }

    private Document readConceptRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);
        return parseXml(getConceptGrpXml(p_rset));
    }

    private Document readLanguageRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);
        return parseXml(getLanguageGrpXml(p_rset));
    }

    private Document readTermRow(RsetHolder p_rset)
        throws TermbaseException, SQLException
    {
        String xml = SqlUtil.readClob(p_rset.m_rset, "XML");
        p_rset.setXml(xml);
        return parseXml(getTermGrpXml(p_rset));
    }

    private boolean entryIsLocked(RsetHolder p_rset)
    {
        return m_termbase.isLocked(p_rset.m_entryId);
    }

    /** Notifies the event listener of the current import status. */
    private void speak(int p_entryCount, int p_percentage, String p_message)
        throws RemoteException, IOException
    {
        IProcessStatusListener listener = m_listener;

        if (listener != null)
        {
            listener.listen(p_entryCount, p_percentage, p_message);
        }
    }

    /**
     * Helper method to speak unconditionally so the web-client
     * receives continues updates and has a chance to "cancel" by
     * throwing an IOException.
     */
    private void showProgress(int p_current, int p_expected, String p_message)
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
     * Helper method to speak only when appropriate so the web-client
     * is not flooded with traffic but still believes search has not
     * died.
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

        // Decide when to update the user's display.
        //   With error message: always
        //
        //   For   1-  10 expected entries, always update
        //   For  11- 100 expected entries, update after every 5th entry
        //   For 101-1000 expected entries, update after every 20th
        //   For more than 1000 entries, update after every 50th
        //
        if ((p_message.length() > 0) ||
            (p_expected <    10) ||
            (p_expected >=   10 && p_expected <   100 &&
                (p_current %  5 == 0)) ||
            (p_expected >=  100 && p_expected <  1000 &&
                (p_current % 20 == 0)) ||
            (p_expected >= 1000 &&
                (p_current % 50 == 0)))
        {
            speak(p_current, percentComplete, p_message);
        }
    }

    private Document parseXmlFragment(String p_fragment)
        throws TermbaseException
    {
        return parseXml("<AllYourBaseAreBelongToUs>" + p_fragment +
            "</AllYourBaseAreBelongToUs>");
    }

    public Document parseXml(String p_xml)
        throws TermbaseException
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    private static class RsetHolder
    {
        public ResultSet m_rset;
        public Long m_entryId;
        public String m_xml;
        public Document m_dom;

        public RsetHolder (ResultSet p_rset)
        {
            m_rset = p_rset;
            m_xml = null;
        }

        public void setResultSet(ResultSet p_rset)
        {
            m_rset = p_rset;
            m_xml = null;
            m_entryId = null;
        }

        public void setEntryId(Long p_id)
        {
            m_entryId = p_id;
        }

        public void setXml(String p_xml)
        {
            m_xml = p_xml;
        }

        public void setDom(Document p_dom)
        {
            m_dom = p_dom;
        }
    }
}

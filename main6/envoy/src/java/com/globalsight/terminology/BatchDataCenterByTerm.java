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
//
// Copyright (c) 2004-2005 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package com.globalsight.terminology;

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.Termbase.Statements;
import com.globalsight.terminology.Termbase.SyncOptions;

import com.globalsight.util.SessionInfo;

import com.globalsight.log.GlobalSightCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Helper class for batch import to synchronize new entries on terms.
 */
public class BatchDataCenterByTerm
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            BatchDataCenterByTerm.class);

    //
    // Private Members
    //
    private Termbase m_termbase;
    private SyncOptions m_options;
    private SessionInfo m_session;

    private String m_language;

    private ArrayList m_newEntries;

    // List of TermEntryPair objects
    private ArrayList m_entriesWithTerm;
    // List of Entry objects
    private ArrayList m_entriesWithNoTerm;



    // Sorted map key=Term, value=Cid
    private TreeMap m_oldTerms;
    // Sorted map key=Cid, value=Old Entry
    private TreeMap m_oldEntries;

    private class TermEntryPair
    {
        public String m_term;
        public Entry  m_entry;

        public TermEntryPair(String p_term, Entry p_entry)
        {
            m_term = p_term;
            m_entry = p_entry;
        }
    }

    //
    // Constructor
    //

    public BatchDataCenterByTerm(Termbase p_termbase, SyncOptions p_options,
        SessionInfo p_session)
    {
        m_termbase = p_termbase;
        m_options = p_options;
        m_session = p_session;

        m_language = p_options.getSyncLanguage();

        m_newEntries = new ArrayList();
        m_entriesWithTerm = new ArrayList();
        m_entriesWithNoTerm = new ArrayList();
    }

    //
    // Public Methods
    //

    public Statements getBatchImportStatements(ArrayList p_entries)
        throws TermbaseException
    {
        m_newEntries.addAll(p_entries);

        return getStatements();
    }
    
    public Statements getTbxBatchImportStatements(ArrayList p_entries)
			throws TermbaseException {
		m_newEntries.addAll(p_entries);

		return getTbxStatements();
	}

    //
    // Private Methods
    //
    
    private Statements getTbxStatements() throws TermbaseException {
    	Statements result = null;
    	getTbxEntriesByTerm();
    	if (m_entriesWithTerm.size() > 0) 
    	{
    		if (m_oldTerms.size() > 0) 
    		{
    			switch (m_options.getSyncAction()) 
    			{
    			case SyncOptions.SYNC_OVERWRITE:
    				computeOverwriteEntries();
    				result = getTbxOverwriteStatements(m_oldEntries);
    				break;
    				
    			case SyncOptions.SYNC_MERGE:
    				computeMergeTbxEntries();
    				result = getTbxOverwriteStatements(m_oldEntries);
                    break;

                case SyncOptions.SYNC_DISCARD:
                default:
                    break;
    			}
    		} 
    		else 
    		{
    			switch (m_options.getNosyncAction()) 
    			{
    			case SyncOptions.NOSYNC_ADD:
    				Statements stmts = getAddTbxStatements(m_newEntries);

                    if (result == null)
                    {
                        result = stmts;
                    }
                    else
                    {
                        result.addAll(stmts);
                    }

                    break;

                case SyncOptions.NOSYNC_DISCARD:
                default:
                    break;
    			}
    		}
    	}
    	if (m_entriesWithNoTerm.size() > 0)
        {
            switch (m_options.getNosyncAction())
            {
            case SyncOptions.NOSYNC_ADD:
                Statements stmts = getAddTbxStatements(m_entriesWithNoTerm);

                if (result == null)
                {
                    result = stmts;
                }
                else
                {
                    result.addAll(stmts);
                }

                break;

            case SyncOptions.NOSYNC_DISCARD:
            default:
                break;
            }
        }

        return result;
    }

    private Statements getStatements()
        throws TermbaseException
    {
        Statements result = null;

        getEntriesByTerm();

        if (m_entriesWithTerm.size() > 0)
        {
        	if (m_oldTerms.size() > 0)
        	{
        		  switch (m_options.getSyncAction())
                  {
                  case SyncOptions.SYNC_OVERWRITE: 
                      computeOverwriteEntries();
                      result = getOverwriteStatements(m_oldEntries);
                      break;

                  case SyncOptions.SYNC_MERGE:
                      computeMergeEntries();
                      result = getOverwriteStatements(m_oldEntries);
                      break;

                  case SyncOptions.SYNC_DISCARD:
                  default:
                      break;
                  }
        	}
        	else
        	{
        	    switch (m_options.getNosyncAction())
                {
                case SyncOptions.NOSYNC_ADD:
                    Statements stmts = getAddStatements(m_newEntries);

                    if (result == null)
                    {
                        result = stmts;
                    }
                    else
                    {
                        result.addAll(stmts);
                    }

                    break;

                case SyncOptions.NOSYNC_DISCARD:
                default:
                    break;
                }
        	}
          
        }
        
        if (m_entriesWithNoTerm.size() > 0)
        {
            switch (m_options.getNosyncAction())
            {
            case SyncOptions.NOSYNC_ADD:
                Statements stmts = getAddStatements(m_entriesWithNoTerm);

                if (result == null)
                {
                    result = stmts;
                }
                else
                {
                    result.addAll(stmts);
                }

                break;

            case SyncOptions.NOSYNC_DISCARD:
            default:
                break;
            }
        }

        return result;
    }

    private void getTbxEntriesByTerm() throws TermbaseException
    {
    	separateTbxEntriesByTerm(m_newEntries, m_entriesWithTerm,
        	m_entriesWithNoTerm);
    	if (m_entriesWithTerm.size() == 0)
        {
            // No new entry has a term.
            return;
        }
    	ArrayList terms = getUniqueTermsFromEntries(m_entriesWithTerm);
        m_oldTerms = getTermsFromDatabase(terms);

        if (m_oldTerms.size() == 0)
        {
            // All new terms are unknown in the termbase, cannot
            // synchronize on term.
            return;
        }

        // Prune the lists even further.
        separateEntriesByExistingTerm(m_oldTerms,
            m_entriesWithTerm, m_entriesWithNoTerm);
    }
    
    private void getEntriesByTerm()
        throws TermbaseException
    {
        // Synchronization on term in language: first we find out
        // which new entries have a term at all and which of the
        // remaining have a correspending term in the termbase.

        // New entries may contain more than one term in the language.
        // We pick the preferred term if it exists, or else the first
        // term.

        // Multiple new terms may refer to the same entry in the
        // termbase. Terms may also point to multiple entries in the
        // termbase (homonyms, the term describes multiple concepts).

        // We need to decide which concept to merge with in case there
        // are multiple. For now, we choose the first concept.

        separateEntriesByTerm(m_newEntries, m_entriesWithTerm,
            m_entriesWithNoTerm);

        if (m_entriesWithTerm.size() == 0)
        {
            // No new entry has a term.
            return;
        }

        ArrayList terms = getUniqueTermsFromEntries(m_entriesWithTerm);
        m_oldTerms = getTermsFromDatabase(terms);

        if (m_oldTerms.size() == 0)
        {
            // All new terms are unknown in the termbase, cannot
            // synchronize on term.
            return;
        }

        // Prune the lists even further.
        separateEntriesByExistingTerm(m_oldTerms,
            m_entriesWithTerm, m_entriesWithNoTerm);
    }

    /**
     * Figures out which new entries contain a term and can be sync'd.
     * Sorts entries into the two other lists, only tbx files.
     */
    private void separateTbxEntriesByTerm(ArrayList p_entries,
        ArrayList p_entriesWithTerm, ArrayList p_entriesWithNoTerm)
    {
    	for (int i = 0, max = p_entries.size(); i < max; i++)
    	{
    		Entry entry = (Entry)p_entries.get(i);
    		
    		String term = null;
    		
    		try 
    		{
    			term = EntryUtils.getPreferredTbxTerm(entry, m_language);
    		}
    		catch(Throwable ignore) {}
    		if (term == null)
    		{
    			p_entriesWithNoTerm.add(entry);
    		}
    		else
    		{
    			p_entriesWithTerm.add(new TermEntryPair(term, entry));
    		}
    	}
    }
    
    /**
     * Figures out which new entries contain a term and can be sync'd.
     * Sorts entries into the two other lists.
     */
    private void separateEntriesByTerm(ArrayList p_entries,
        ArrayList p_entriesWithTerm, ArrayList p_entriesWithNoTerm)
    {
        for (int i = 0, max = p_entries.size(); i < max; i++)
        {
            Entry entry = (Entry)p_entries.get(i);

            String term = null;

            try
            {
                term = EntryUtils.getPreferredTerm(entry, m_language);
            }
            catch (Throwable ignore)
            {
                // XML parse exception, SNH.
            }

            if (term == null)
            {
                p_entriesWithNoTerm.add(entry);
            }
            else
            {
                p_entriesWithTerm.add(new TermEntryPair(term, entry));
            }
        }
    }

    /**
     * Given a list of terms actually found in the termbase, separate
     * the lists of new entries further (may be no-op in most normal
     * circumstances).
     */
    private void separateEntriesByExistingTerm(TreeMap p_oldTerms,
        ArrayList p_entriesWithTerm, ArrayList p_entriesWithNoTerm)
    {
        for (Iterator it = p_entriesWithTerm.iterator(); it.hasNext(); )
        {
            TermEntryPair pair = (TermEntryPair)it.next();

            if (!p_oldTerms.containsKey(pair.m_term))
            {
                p_entriesWithNoTerm.add(pair.m_entry);

                it.remove();
            }
        }
    }

    private ArrayList getUniqueTermsFromEntries(ArrayList p_entriesWithTerm)
    {
        TreeSet terms = new TreeSet();

        for (int i = 0, max = p_entriesWithTerm.size(); i < max; i++)
        {
            TermEntryPair pair = (TermEntryPair)p_entriesWithTerm.get(i);

            terms.add(pair.m_term);
        }

        ArrayList result = new ArrayList(terms);

        return result;
    }

    private TreeMap getTermsFromDatabase(ArrayList p_terms)
        throws TermbaseException
    {
        TreeMap result;

        // result is map ["term" -> cid]
        result = m_termbase.getCidsByTerms(p_terms, m_language);

        return result;
    }

    private void computeOverwriteEntries()
    {
        m_oldEntries = new TreeMap();

        // overwrite entries, one by one
        for (int i = 0, max = m_entriesWithTerm.size(); i < max; i++)
        {
            TermEntryPair pair = (TermEntryPair)m_entriesWithTerm.get(i);

            String term = pair.m_term;
            Entry newEntry = pair.m_entry;

            Long cid = (Long)m_oldTerms.get(term);
            if (cid != null)
            {
            	// Put the entry in m_oldEntries so other terms that need
                // to overwrite the same entry will find it.
                m_oldEntries.put(cid, newEntry);
            }         
        }
    }
    
    private void computeMergeTbxEntries() throws TermbaseException 
    {
    	m_oldEntries = loadExistingTbxEntries(
                new ArrayList(m_oldTerms.values()));
    	if (m_oldEntries.size() == 0)
    	{
    		return;
    	}
    	for (int i = 0, max = m_entriesWithTerm.size(); i < max; i++)
    	{
    		TermEntryPair pair = (TermEntryPair)m_entriesWithTerm.get(i);
    		
    		String term = pair.m_term;
            Entry newEntry = pair.m_entry;
            
            String cid = String.valueOf(m_oldTerms.get(term));
            Entry oldEntry = (Entry)m_oldEntries.get(cid);
            
            if (oldEntry != null)
            {
            	 oldEntry = EntryUtils.mergeTbxEntries(oldEntry, newEntry);

                 // TODO: For now, all entries are dirty and need to be
                 // updated in the database. This may be improved.

                 // Put the merged entry back in m_oldEntries so other
                 // terms that need to modify the same entry will find it.
                 m_oldEntries.put(cid, oldEntry);
            }
    	}
    }
    
    /**
     * Computes a list of merged entries from a list of entries to be
     * imported and their concept IDs.
     */
    private void computeMergeEntries()
        throws TermbaseException
    {
        // Load the entries to be synchronized.
        m_oldEntries = loadExistingEntries(
            new ArrayList(m_oldTerms.values()));
        
        if (m_oldEntries.size() == 0)
        {
        	return;
        }

        // merge entries, one by one
        for (int i = 0, max = m_entriesWithTerm.size(); i < max; i++)
        {
            TermEntryPair pair = (TermEntryPair)m_entriesWithTerm.get(i);

            String term = pair.m_term;
            Entry newEntry = pair.m_entry;

            Long cid = (Long)m_oldTerms.get(term);
            Entry oldEntry = (Entry)m_oldEntries.get(cid);
            if (oldEntry != null)
            {
            	 oldEntry = EntryUtils.mergeEntries(oldEntry, newEntry);

                 // TODO: For now, all entries are dirty and need to be
                 // updated in the database. This may be improved.

                 // Put the merged entry back in m_oldEntries so other
                 // terms that need to modify the same entry will find it.
                 m_oldEntries.put(cid, oldEntry);
            }          
        }
    }

    private TreeMap loadExistingEntries(ArrayList p_cids)
        throws TermbaseException
    {
        TreeMap result = new TreeMap();

        // TODO: this needs to be a batch load.
        for (int i = 0, max = p_cids.size(); i < max; i++)
        {
            Long cid = (Long)p_cids.get(i);

            String xml = m_termbase.getEntry(cid.longValue(), m_session);

            result.put(cid, new Entry(xml));
        }

        return result;
    }
    
    private TreeMap loadExistingTbxEntries(ArrayList p_cids)
    	throws TermbaseException
	{
    	TreeMap result = new TreeMap();
    	for (int i = 0; i < p_cids.size(); i++) 
    	{
    		Object cid = null;
    		if (p_cids.get(i) instanceof Long) {
    			cid = (Long)p_cids.get(i);
    		} else if (p_cids.get(i) instanceof String) {
    			cid = (String)p_cids.get(i);
    		}

            String xml = m_termbase.getTbxEntry(String.valueOf(cid), m_session);

            result.put(String.valueOf(cid), new Entry(xml));
    	}
    	return result;
	}

    /**
     * Returns a list of statements to be executed to overwrite a set
     * of existing entries with a set of new entries.
     */
    private Statements getOverwriteStatements(TreeMap p_entries)
        throws TermbaseException
    {
        Statements result;

        result = m_termbase.getOverwriteEntriesStatements(p_entries,
            m_session);

        return result;
    }
    
    private Statements getTbxOverwriteStatements(TreeMap p_entries)
    {
    	Statements result;

        result = m_termbase.getOverwriteTbxEntriesStatements(p_entries,
            m_session);

        return result;
    }

    private Statements getAddStatements(ArrayList p_entries)
        throws TermbaseException
    {
        Statements result;

        result = m_termbase.batchAddEntriesAsNew(p_entries, null, m_session);

        return result;
    }
    
    private Statements getAddTbxStatements(ArrayList p_entries)
			throws TermbaseException 
	{
		Statements result;

		result = m_termbase.batchAddTbxEntriesAsNew(p_entries, null, m_session);

		return result;
	}
}

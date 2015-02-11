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

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.Termbase.Statements;
import com.globalsight.terminology.Termbase.SyncOptions;

import com.globalsight.util.SessionInfo;

import com.globalsight.log.GlobalSightCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Helper class for batch import to synchronize new entries on concept ID.
 */
public class BatchDataCenterByCid
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            BatchDataCenterByCid.class);

    //
    // Private Members
    //
    private Termbase m_termbase;
    private SyncOptions m_options;
    private SessionInfo m_session;

    private ArrayList m_oldCids;
    private ArrayList m_newCids;

    private ArrayList m_newEntries;
    private ArrayList m_oldEntries;

    // Sorted map key=Long(CID), value=Entry
    private TreeMap m_entriesWithId;
    private ArrayList m_entriesWithNoId;

    //
    // Constructor
    //

    public BatchDataCenterByCid(Termbase p_termbase, SyncOptions p_options,
        SessionInfo p_session)
    {
        m_termbase = p_termbase;
        m_options = p_options;
        m_session = p_session;

        m_newEntries = new ArrayList();
        m_entriesWithId = new TreeMap();
        m_entriesWithNoId = new ArrayList();
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
			throws TermbaseException
    {
    	m_newEntries.addAll(p_entries);
    	return getTbxStatements();
    }

    

	//
    // Private Methods
    //
    
    private Statements getTbxStatements() 
    {
    	Statements result = null;
    	m_entriesWithId = getTbxEntriesById();
    	if (m_entriesWithId != null && m_entriesWithId.size() > 0)
        {
            switch (m_options.getSyncAction())
            {
	            case SyncOptions.SYNC_OVERWRITE:
	                computeOverwriteEntries();
	                result = getOverwriteTbxStatements(m_entriesWithId);
	                break;
	
	            case SyncOptions.SYNC_MERGE:
	                computeMergeTbxEntries();
	                result = getOverwriteTbxStatements(m_entriesWithId);
	                break;
	
	            case SyncOptions.SYNC_DISCARD:
	            default:
	                break;
            }
        }
    	
    	if (m_entriesWithNoId.size() > 0)
        {
        	   switch (m_options.getNosyncAction())
               {
	               case SyncOptions.NOSYNC_ADD:
	                   Statements stmts = getAddTbxStatements(m_entriesWithNoId);
	
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

        m_entriesWithId = getEntriesByConceptId();

        if (m_entriesWithId != null && m_entriesWithId.size() > 0)
        {
            switch (m_options.getSyncAction())
            {
	            case SyncOptions.SYNC_OVERWRITE:
	                computeOverwriteEntries();
	                result = getOverwriteStatements(m_entriesWithId);
	                break;
	
	            case SyncOptions.SYNC_MERGE:
	                computeMergeEntries();
	                result = getOverwriteStatements(m_entriesWithId);
	                break;
	
	            case SyncOptions.SYNC_DISCARD:
	            default:
	                break;
            }
        }
              
        if (m_entriesWithNoId.size() > 0)
        {
        	   switch (m_options.getNosyncAction())
               {
	               case SyncOptions.NOSYNC_ADD:
	                   Statements stmts = getAddStatements(m_entriesWithNoId);
	
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

    private TreeMap getTbxEntriesById()
    	throws TermbaseException
    {
    	separateTbxEntriesById(m_newEntries, m_entriesWithId, m_entriesWithNoId);
    	if (m_entriesWithId.size() == 0)
        {
            // No new entry has a concept ID, discard.
            return null;
        }

        m_newCids = getIdsFromEntries(m_entriesWithId);
        m_oldCids = getTbxIdsFromDatabase(m_newCids);

        if (m_oldCids.size() == 0)
        {
            // All new entries are unknown in the termbase, cannot
            // synchronize on concept ID. Discard.
            return null;
        }

        // Prune the lists even further.
        separateTbxEntriesByExistingId(m_oldCids,
            m_entriesWithId, m_entriesWithNoId);

        return m_entriesWithId;
    }
    
    private TreeMap getEntriesByConceptId()
        throws TermbaseException
    {
        // Synchronization on Concept ID: first we find out which new
        // entries have a concept ID at all and which of the remaining
        // have a correspending concept ID in the termbase.

        // By using a TreeMap for m_entriesWithId we ensure that
        // duplicates within the list of entries to be added get
        // collapsed into only one (the latest).

        separateEntriesById(m_newEntries, m_entriesWithId, m_entriesWithNoId);

        if (m_entriesWithId.size() == 0)
        {
            // No new entry has a concept ID, discard.
            return null;
        }

        m_newCids = getIdsFromEntries(m_entriesWithId);
        m_oldCids = getIdsFromDatabase(m_newCids);

        if (m_oldCids.size() == 0)
        {
            // All new entries are unknown in the termbase, cannot
            // synchronize on concept ID. Discard.
            return null;
        }

        // Prune the lists even further.
        separateEntriesByExistingId(m_oldCids,
            m_entriesWithId, m_entriesWithNoId);

        return m_entriesWithId;
    }

    /**
     * Figures out which new entries have a CID and can be sync'd.
     * Returns a list of CIDs, and sorts entries into the two other
     * lists.
     */
    private void separateEntriesById(ArrayList p_entries,
        TreeMap p_entriesWithId, ArrayList p_entriesWithNoId)
    {
        for (int i = 0, max = p_entries.size(); i < max; i++)
        {
            Entry entry = (Entry)p_entries.get(i);

            long cid = 0;

            try
            {
                cid = EntryUtils.getConceptId(entry);
            }
            catch (Throwable ignore)
            {
                // XML parse exception, SNH.
            }

            if (cid == 0)
            {
                p_entriesWithNoId.add(entry);
            }
            else
            {
                p_entriesWithId.put(new Long(cid), entry);
            }
        }
    }
    
    private void separateTbxEntriesById(ArrayList p_entries,
            TreeMap p_entriesWithId, ArrayList p_entriesWithNoId)
    {
    	for (int i = 0, max = p_entries.size(); i < max; i++)
    	{
    		Entry entry = (Entry)p_entries.get(i);
    		String id = "";
    		try {
    			id = EntryUtils.getTbxTermEntryId(entry);
    		} catch (Throwable ignore){}
    		if (id == null) {
    			p_entriesWithNoId.add(entry);
    		} else {
    			p_entriesWithId.put(id, entry);
    		}
    	}
    }
    
    /**
     * Given a list of CIDs actually found in the termbase, separate
     * the lists of new entries further (may be no-op in most normal
     * circumstances).
     */
    private void separateTbxEntriesByExistingId(ArrayList p_oldCids,
        TreeMap p_entriesWithId, ArrayList p_entriesWithNoId)
    {
    	Set keys = p_entriesWithId.keySet();
    	for (Iterator it = keys.iterator(); it.hasNext(); )
    	{
    		String cid = (String)it.next();
    		if (!p_oldCids.contains(cid)) 
    		{
    			Object entry = p_entriesWithId.get(cid);

                p_entriesWithNoId.add(entry);

                it.remove();
    		}
    	}
    }

    /**
     * Given a list of CIDs actually found in the termbase, separate
     * the lists of new entries further (may be no-op in most normal
     * circumstances).
     */
    private void separateEntriesByExistingId(ArrayList p_oldCids,
        TreeMap p_entriesWithId, ArrayList p_entriesWithNoId)
    {
        Set keys = p_entriesWithId.keySet();

        for (Iterator it = keys.iterator(); it.hasNext(); )
        {
            Long cid = (Long)it.next();

            if (!p_oldCids.contains(cid))
            {
                Object entry = p_entriesWithId.get(cid);

                p_entriesWithNoId.add(entry);

                it.remove();
            }
        }
    }

    private ArrayList getIdsFromEntries(TreeMap p_entriesWithId)
    {
        ArrayList result = new ArrayList();

        result.addAll(p_entriesWithId.keySet());

        return result;
    }

    private ArrayList getIdsFromDatabase(ArrayList p_cids)
        throws TermbaseException
    {
        ArrayList result;

        result = m_termbase.getExistingCids(p_cids);

        return result;
    }
    
    private ArrayList getTbxIdsFromDatabase(ArrayList p_cids)
			throws TermbaseException {
		ArrayList result;

		result = m_termbase.getExistingTbxCids(p_cids);

		return result;
	} 

    private void computeOverwriteEntries()
    {
        // NOOP - all new entries overwrite existing entries as is.
    }

    /**
     * Computes a list of merged entries from a list of entries to be
     * imported and their concept IDs.
     */
    private void computeMergeEntries()
        throws TermbaseException
    {
        // Load the entries to be synchronized.
        TreeMap oldEntries = loadExistingEntries(m_oldCids);

        // merge entries, one by one
        Set keys = oldEntries.keySet();
        for (Iterator it = keys.iterator(); it.hasNext(); )
        {
            Long key = (Long)it.next();

            Entry oldEntry = (Entry)oldEntries.get(key);
            Entry newEntry = (Entry)m_entriesWithId.get(key);

            newEntry = EntryUtils.mergeEntries(oldEntry, newEntry);

            // TODO: For now, all entries are dirty and need to be
            // updated in the database. This may be improved.

            m_entriesWithId.put(key, newEntry);
        }
    }
    
    /**
     * Computes a list of merged entries from a list of entries to be
     * imported and their concept IDs.
     */
    private void computeMergeTbxEntries() throws TermbaseException
    {
        // Load the entries to be synchronized.
        TreeMap oldEntries = loadExistingTbxEntries(m_oldCids);

        // merge entries, one by one
        Set keys = oldEntries.keySet();
        for (Iterator it = keys.iterator(); it.hasNext(); )
        {
            String key = (String)it.next();

            Entry oldEntry = (Entry)oldEntries.get(key);
            Entry newEntry = (Entry)m_entriesWithId.get(key);

            newEntry = EntryUtils.mergeTbxEntries(oldEntry, newEntry);

            // TODO: For now, all entries are dirty and need to be
            // updated in the database. This may be improved.

            m_entriesWithId.put(key, newEntry);
        }
    }
    
    private TreeMap loadExistingTbxEntries(ArrayList p_cids) throws TermbaseException
    {
    	TreeMap result = new TreeMap();

        // TODO: this needs to be a batch load.
        for (int i = 0, max = p_cids.size(); i < max; i++)
        {
            String cid = (String)p_cids.get(i);

            String xml = m_termbase.getTbxEntry(cid, m_session);

            result.put(cid, new Entry(xml));
        }

        return result;
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
    
    private Statements getOverwriteTbxStatements(TreeMap p_entries)
    	throws TermbaseException
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
    {
        Statements result;
        
        result = m_termbase.batchAddTbxEntriesAsNew(p_entries, null, m_session);
        
        return result;
    }
}

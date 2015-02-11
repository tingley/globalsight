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
package com.globalsight.terminology.entrycreation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.util.SessionInfo;

public class EntryCreationDiscardByCid extends EntryCreation implements
        IEntryCreation
{
    public EntryCreationDiscardByCid(String p_fileType)
    {
        super(p_fileType);
    }
    
    /*
    protected TbConcept getTbConceptByEntry(long terbseId, Entry p_entry,
            SessionInfo p_session)
    {
        String id = entryParse.getConceptId(p_entry);
        TbConcept tcNew = super.getTbConceptByEntry(terbseId, p_entry,
                p_session);

        if (id != null && id.trim().length() != 0)
        {
            try
            {
                TbConcept tc = HibernateUtil.load(TbConcept.class, Long
                        .parseLong(id));

                if (tc == null)
                {
                    return nosync.doAction(tcNew);
                }
            }
            catch (Exception e)
            {
                throw new TermbaseException(e);
            }
        }
        else
        {
            return nosync.doAction(tcNew);
        }

        return null;
    }
*/
    

    /*
     * Reload the method, because if judge every entry is or not in the database,
     * need use N times  "HibernateUtil.load", so it actually need N times query,
     * the efficiency is low. So here use one hql to query all the exist entries
     * in the database, and remove them, and save the remaining.
     */
    @Override
    public void batchAddEntriesAsNew(long terbseId, ArrayList p_entries,
            SessionInfo p_session) throws TermbaseException
    {
        if(!nosync.isNeedDoWork()) 
            return;
        
        //store the new Entry, include two type: no id and id not in database.
        ArrayList array = new ArrayList();
        //store all the id of entry  that has id of the imported file.
        HashMap entryMap = new HashMap();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < p_entries.size(); i++)
        {
            Entry entry = (Entry) p_entries.get(i);
            String id = entryParse.getConceptId(entry);
            
            try
            {
                long tc_id = Long.parseLong(id);

                if (id != null && id.trim().length() != 0)
                {
                    if (i == p_entries.size() - 1)
                    {
                        buffer.append(id);
                    }
                    else
                    {
                        buffer.append(id).append(",");
                    }
                    
                    entryMap.put(tc_id, entry);
                }
                else
                {
                    TbConcept tc = getTbConceptByEntry(terbseId, entry,
                            p_session);
                    if (tc != null)
                        array.add(tc);
                }
            }
            catch (Exception e)
            {
            }
        }

        //select all the ids exist in the database.
        String hql = "select tc.id from TbConcept tc where tc.id in ("
                + buffer.toString() + ")";

        List list = HibernateUtil.search(hql);
        //remove the exist ids
        Set set = entryMap.keySet();
        set.removeAll(list);
        Iterator ite = set.iterator();
        
        while(ite.hasNext()) {
            Object key = ite.next();
            Entry value = (Entry) entryMap.get(key);
            TbConcept tc = getTbConceptByEntry(terbseId, value, p_session);
            if (tc != null)
                array.add(tc);
        }
 
        try
        {
            // must close session, because in the session we have use load() or
            // get()
            // to get a persistent object of TbConcept, when we saveorupdate
            // the new TbConcept object with the same id, will have error.
            HibernateUtil.closeSession();
            HibernateUtil.saveOrUpdate(array);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }
}

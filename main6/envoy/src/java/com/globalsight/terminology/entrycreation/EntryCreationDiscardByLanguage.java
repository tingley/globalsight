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
import java.util.List;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.util.SessionInfo;

public class EntryCreationDiscardByLanguage extends EntryCreation implements
        IEntryCreation
{
    public EntryCreationDiscardByLanguage(String p_fileType)
    {
        super(p_fileType);
    }
    
    @Override
    protected TbConcept getTbConceptByEntry(long terbseId, Entry p_entry,
            SessionInfo p_session)
    {
        TbConcept tcNew = super.getTbConceptByEntry(terbseId, p_entry,
                p_session);
        String term = EntryUtils.getPreferredTbxTerm(p_entry, options
                .getSyncLanguage(), fileType);
        List list = getConceptByTermsAndLan(terbseId, term,
                options.getSyncLanguage());
        
        if (list != null && list.size() > 0)
        {
            return null;
        }
        else {
            return nosync.doAction(tcNew);
        }
    }
    
    @Override
    public void batchAddEntriesAsNew(long terbseId, ArrayList p_entries,
            SessionInfo p_session) throws TermbaseException
    {
        if(!nosync.isNeedDoWork()) 
            return;
        
        super.batchAddEntriesAsNew(terbseId, p_entries, p_session);
    }
}

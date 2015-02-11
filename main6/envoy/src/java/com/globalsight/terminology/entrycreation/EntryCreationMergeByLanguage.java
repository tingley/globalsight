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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.util.SessionInfo;

public class EntryCreationMergeByLanguage extends EntryCreationByLanguage implements
        IEntryCreation
{
    private static final Logger CATEGORY = Logger
            .getLogger(EntryCreationMergeByLanguage.class);
    
    public EntryCreationMergeByLanguage(String p_fileType)
    {
        super(p_fileType);
    }

    @Override
    protected List getConceptsList(long terbseId, Entry p_entry,
            SessionInfo p_session)
    {
        List<TbConcept> merges = new ArrayList<TbConcept>();

        try
        {
            String term = EntryUtils.getPreferredTbxTerm(p_entry, options
                    .getSyncLanguage(), fileType);
            if (term == null)
            {
                return null;
            }
            List oldConcepts = getConceptByTermsAndLan(terbseId, term,
                    options.getSyncLanguage());

            // merge the old and new entry
            if (oldConcepts != null && oldConcepts.size() > 0)
            {
                Iterator<TbConcept> ite = oldConcepts.iterator();
                
                while(ite.hasNext()) {
                    TbConcept tcNew = super.getTbConceptByEntry(terbseId, p_entry,
                            p_session);
                    TbConcept tc = (TbConcept) ite.next();
                    TbConcept tcMerge = getMergeConcept(tcNew, tc);
                    merges.add(tcMerge);
                }
            }
            else {
                TbConcept tcNew = super.getTbConceptByEntry(terbseId, p_entry,
                        p_session);
                merges.add(nosync.doAction(tcNew));
            }
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }

        return merges;
    }
}

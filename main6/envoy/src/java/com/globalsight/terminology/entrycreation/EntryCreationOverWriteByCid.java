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

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.util.SessionInfo;

public class EntryCreationOverWriteByCid extends EntryCreation implements IEntryCreation
{
    public EntryCreationOverWriteByCid(String p_fileType)
    {
        super(p_fileType);
    }
    
    @Override
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
                TbConcept tc = HibernateUtil.get(TbConcept.class, Long.parseLong(id));

                // delete the original languages and terms of the concept,
                // and add new languages and terms into the concept.
                if (tc != null)
                {
                    tcNew = getOverwriteConcept(tcNew, tc);
                }
                else {
                    return nosync.doAction(tcNew);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else {
            return nosync.doAction(tcNew);
        }

        // if have no id attribute, create a new concept.
        return tcNew;

    }
}

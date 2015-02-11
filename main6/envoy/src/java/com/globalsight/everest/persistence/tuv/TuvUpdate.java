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
package com.globalsight.everest.persistence.tuv;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * This class updates TUV
 */
public class TuvUpdate
{
    public static void updateSegmentText(Collection p_tuvs)
            throws GeneralException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            
            Iterator it = p_tuvs.iterator();
            while (it.hasNext())
            {              
                Tuv tuv = (Tuv) it.next();
                TuvImpl tuvInDb = (TuvImpl)HibernateUtil.get(TuvImpl.class, tuv.getId());
                tuvInDb.setSegmentString(tuv.getGxml());
                tuvInDb.setExactMatchKey(((TuvLing) tuv).getExactMatchKey());
                HibernateUtil.update(tuvInDb);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            throw new GeneralException(e);
        }
    }
}

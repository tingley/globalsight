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

package com.globalsight.persistence.fuzzyindexing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.ling.tm.fuzzy.Atom;
import com.globalsight.persistence.PersistenceCommand;

public class InsertFuzzyIndexPersistenceCommand extends PersistenceCommand
{
    private PreparedStatement m_psInsertTokens;
    private List m_fuzzyIndexes;

    //TODO Does this table "FUZZY_INDEX" exists? and the sequence "SEQ_FUZZY_INDEX" ?
    private String m_insertToken = 
        "insert into fuzzy_index(seq_fuzzy_index.NEXTVAL,?,?,?,?,?)";
    
    public InsertFuzzyIndexPersistenceCommand(List p_fuzzyIndexes)
    {
        m_fuzzyIndexes = p_fuzzyIndexes;
    }

    public void persistObjects(Connection p_connection) throws PersistenceException 
    {
        try
        {
            super.persistObjects(p_connection);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_psInsertTokens != null)
                {
                    m_psInsertTokens.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection) throws Exception 
    {
        m_psInsertTokens = p_connection.prepareStatement(m_insertToken);
    }
    public void setData() throws Exception 
    {
          Iterator m_fuzzyIterator = m_fuzzyIndexes.iterator();
          while (m_fuzzyIterator.hasNext())
          {
              Atom atom = (Atom)m_fuzzyIterator.next();
              m_psInsertTokens.setLong(1,atom.getTuvId());
              m_psInsertTokens.setLong(2,atom.getLocale());
              m_psInsertTokens.setLong(3,atom.getTmId());
              m_psInsertTokens.setLong(4,atom.getAtomCrc());
              m_psInsertTokens.setLong(5,atom.getTokenCount());
              m_psInsertTokens.addBatch();
          }
    }
    public void batchStatements() throws Exception 
    {
        m_psInsertTokens.executeBatch();
    }
}






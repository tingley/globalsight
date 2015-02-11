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

package com.globalsight.persistence.pageexport;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;

public class DeleteLeverageMatchPersistenceCommand extends PersistenceCommand
{
    static public final String s_deleteLeverageMatches = "delete from leverage_match where "
            + "source_page_id = ? and target_locale_id = ?";

    private long m_sourcePageId;
    private long m_targetLocaleId;
    private PreparedStatement m_ps;

    public DeleteLeverageMatchPersistenceCommand(long p_sourcePageId,
            long p_targetLocaleId)
    {
        m_sourcePageId = p_sourcePageId;
        m_targetLocaleId = p_targetLocaleId;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception sqle)
        {
            throw new PersistenceException(sqle);
        }
        finally
        {
            close(m_ps);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(s_deleteLeverageMatches);
    }

    public void setData() throws Exception
    {
        m_ps.setLong(1, m_sourcePageId);
        m_ps.setLong(2, m_targetLocaleId);
    }

    public void batchStatements() throws Exception
    {
        m_ps.execute();
    }
}

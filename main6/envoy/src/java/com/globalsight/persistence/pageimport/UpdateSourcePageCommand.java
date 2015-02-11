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

package com.globalsight.persistence.pageimport;

import com.globalsight.persistence.PersistenceCommand;

import com.globalsight.everest.page.SourcePage;

import com.globalsight.everest.persistence.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class UpdateSourcePageCommand extends PersistenceCommand
{
    private PreparedStatement m_ps;
    private PreparedStatement m_ps1;
    private String m_updateCurrentSourcePage = "update source_page set state = ? where id = ? ";
    private String m_updateCurrentSourcePageAndCuvId = "update source_page set state = ?, cuv_id = ? where id = ? ";
    private String m_updatePreviousSourcePage = "update source_page set state = ? where previous_page_id = ?";
    private SourcePage m_currentPage;
    
    public UpdateSourcePageCommand(SourcePage p_currentPage)
    {
        m_currentPage = p_currentPage;
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
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_ps !=  null) m_ps.close();
                if (m_ps1 != null) m_ps1.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    public void createPreparedStatement(Connection p_connection) 
        throws Exception 
    {
        if (m_currentPage.getCuvId() == null)
            m_ps = p_connection.prepareStatement(m_updateCurrentSourcePage);
        else
            m_ps = p_connection.prepareStatement(m_updateCurrentSourcePageAndCuvId);

       if (m_currentPage.getPreviousPageId() > 0)
       {
           m_ps1 = p_connection.prepareStatement(m_updatePreviousSourcePage);
       }
    }

    public void setData() throws Exception
    {
        m_ps.setString(1,m_currentPage.getPageState());
        if (m_currentPage.getCuvId() != null)
        {
            m_ps.setLong(2,m_currentPage.getCuvId().longValue());
            m_ps.setLong(3, m_currentPage.getId());
        }
        else
            m_ps.setLong(2, m_currentPage.getId());

        if (m_ps1 != null)
        {
            m_ps1.setString(1, "OUT_OF_DATE");
            m_ps1.setLong(2, m_currentPage.getPreviousPageId());
        }
    }

    public void batchStatements() throws Exception 
    {
        m_ps.executeUpdate();
        if (m_ps1 != null)
        {
            m_ps.executeUpdate();
        }
    }
}

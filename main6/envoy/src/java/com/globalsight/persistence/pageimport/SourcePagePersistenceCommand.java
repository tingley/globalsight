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

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.SourcePage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class SourcePagePersistenceCommand extends PersistenceCommand
{
    private String m_updateSourcePageCommand = 
        "update source_page set word_count = ?, external_base_href =?  where id = ?";
    private PreparedStatement m_ps;
    private SourcePage m_sourcePage;

    public SourcePagePersistenceCommand(SourcePage p_sourcePage)
    {
        m_sourcePage = p_sourcePage;
    }
    public String getUpdateSourcePageCommand()
    {
        return m_updateSourcePageCommand;
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
        catch(Exception sqle)
        {
            throw new PersistenceException(sqle);   
        }
        finally
        {
            try
            {
                if (m_ps != null)
                {
                    m_ps.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }
    
    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
       m_ps = p_connection.prepareStatement(m_updateSourcePageCommand);
    }
    public void setData()  throws Exception
    {
       long id = m_sourcePage.getId();
       long wordCount = m_sourcePage.getWordCount();
       m_ps.setLong(1,wordCount);
       // tbd - assumes this is an extracted file
       String externalBaseHref = ((ExtractedFile)m_sourcePage.getPrimaryFile()).
                                getExternalBaseHref();
       if (externalBaseHref == null || externalBaseHref.length() == 0)
       {
           m_ps.setNull(2,Types.VARCHAR);
       }
       else
       {
           m_ps.setString(2, externalBaseHref);
       }
       m_ps.setLong(3,id);
       m_ps.addBatch();
    }
    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();
    }
}

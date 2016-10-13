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

// globalsight
import com.globalsight.persistence.PersistenceCommand;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.UnextractedFile;

import com.globalsight.everest.persistence.PersistenceException;

// java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Updates the unextracted file information that needs to be updated on import.
 * This is both on a SourcePage and all its TargetPages. On import the updates
 * to both are the same since the file is essentially the same at this time.
 *
 * This command handles the updates to it that are necessary later on in the
 * import process.
 */
public class UpdateUnextractedFileImportCommand extends PersistenceCommand
{
    private PreparedStatement m_ps1;
    private PreparedStatement m_ps2;

    // SQL statement for updating the source page
//    private String m_updateSourcePageWithUnextractedFile = 
//            "update source_page set last_modified=?, modifier_user_id=?, file_length=?, timestamp=sysdate " +
//            "where id = ? ";
    private String m_updateSourcePageWithUnextractedFile = 
        "update source_page set last_modified=?, modifier_user_id=?, file_length=?, timestamp=current_timestamp " +
        "where id = ? ";
    // SQL statement for updating the target pages
    // all information is the same since this is just on import (nothing has been
    // changed yet)
//    private String m_updateTargetPagesWithUnextractedFile= 
//            "update target_page set last_modified=?, modifier_user_id=?, file_length=?, timestamp=sysdate " +
//            "where source_page_id = ?";
    private String m_updateTargetPagesWithUnextractedFile= 
        "update target_page set last_modified=?, modifier_user_id=?, file_length=?, timestamp=current_timestamp " +
        "where source_page_id = ?";

    private SourcePage m_sourcePage = null;
    
    /**
     * constructor
     */
    public UpdateUnextractedFileImportCommand(SourcePage p_sourcePage)
    {
        m_sourcePage = p_sourcePage;
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
                if (m_ps1 != null) m_ps1.close();
                if (m_ps2 != null) m_ps2.close();
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
       m_ps1 = p_connection.prepareStatement(m_updateSourcePageWithUnextractedFile);
       m_ps2 = p_connection.prepareStatement(m_updateTargetPagesWithUnextractedFile);
    }


    public void setData() throws Exception
    {                                
        // set the data for the first prepared statement - for updating the source page
        UnextractedFile uf = (UnextractedFile)m_sourcePage.getPrimaryFile();
        m_ps1.setTimestamp(1, new Timestamp(uf.getLastModifiedDate().getTime()));
        m_ps1.setString(2,uf.getLastModifiedBy());
        m_ps1.setLong(3, uf.getLength());
        m_ps1.setLong(4, m_sourcePage.getId());
        
        // set the data for the second prepared statement - for updating the target pages
        m_ps2.setTimestamp(1, new Timestamp(uf.getLastModifiedDate().getTime()));
        m_ps2.setString(2, uf.getLastModifiedBy());
        m_ps2.setLong(3, uf.getLength());
        m_ps2.setLong(4, m_sourcePage.getId());
    }

    public void batchStatements() throws Exception 
    {
        m_ps1.executeUpdate();
        m_ps2.executeUpdate();
    }
}

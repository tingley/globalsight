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

package com.globalsight.persistence.pageupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;

public class DeleteTemplatePartPersistenceCommand extends PersistenceCommand
{
    private static Logger s_logger = Logger
            .getLogger(DeleteTemplatePartPersistenceCommand.class);

    private static final String s_DELETE_TEMPLATEPARTS = "delete from template_part where template_id=?";

    private PreparedStatement m_ps = null;
    private ArrayList m_templates;

    //
    // Constructor
    //

    public DeleteTemplatePartPersistenceCommand(ArrayList p_templates)
    {
        m_templates = p_templates;
    }

    //
    // Methods
    //

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
            close(m_ps);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(s_DELETE_TEMPLATEPARTS);
    }

    public void setData() throws Exception
    {
        for (int i = 0, max = m_templates.size(); i < max; i++)
        {
            PageTemplate template = (PageTemplate) m_templates.get(i);

            m_ps.setLong(1, template.getId());
            m_ps.addBatch();

            if (s_logger.isDebugEnabled())
            {
                System.err.println(s_DELETE_TEMPLATEPARTS.replaceFirst("\\?",
                        String.valueOf(template.getId())));
            }
        }
    }

    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();
    }
}

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * @deprecated
 */
public class TemplatePartPersistenceCommand extends PersistenceCommand
{
    static private String m_insertNonClobCommand = "insert into template_part(id, template_id, order_num, skeleton_string, tu_id) values(null,?,?,?,?)";
    // static private String m_insertClobCommand =
    // "insert into template_part(id, template_id, order_num, skeleton_clob,
    // tu_id) values(?,?,?,empty_clob(),?)";
    static private String m_insertClobCommand = "insert into template_part(id, template_id, order_num, skeleton_clob, tu_id) values(null,?,?,?,?)";
    // static private String m_selectClobCommand =
    // "select * from template_part where id = ? for update ";

    private List m_nonClobTemplatePart;
    private List m_clobTemplatePart;

    private PreparedStatementBatch m_ps1Batch = null;
    private PreparedStatement m_ps2;

    // private PreparedStatement m_ps3;

    public TemplatePartPersistenceCommand(List p_nonClobTemplatePart,
            List p_clobTemplatePart, HashMap p_sequenceMap)
    {
        m_nonClobTemplatePart = p_nonClobTemplatePart;
        m_clobTemplatePart = p_clobTemplatePart;
    }

    public String getinsertNonClobCommand()
    {
        return m_insertNonClobCommand;
    }

    public String getinsertClobCommand()
    {
        return m_insertClobCommand;
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
            close(m_ps1Batch);
            close(m_ps2);
            // close(m_ps3);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        if (m_nonClobTemplatePart.size() > 0)
        {
            m_ps1Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    m_insertNonClobCommand, false);
        }

        if (m_clobTemplatePart.size() > 0)
        {
            m_ps2 = p_connection.prepareStatement(m_insertClobCommand);
            // m_ps3 = p_connection.prepareStatement(m_selectClobCommand);
        }
    }

    public void setData() throws Exception
    {
        Iterator nonClobIterator = m_nonClobTemplatePart.iterator();

        if (m_nonClobTemplatePart.size() > 0)
        {

            while (nonClobIterator.hasNext())
            {
                TemplatePart tp = (TemplatePart) nonClobIterator.next();
                PreparedStatement ps = m_ps1Batch.getNextPreparedStatement();
                ps.setLong(1, tp.getTemplateId());
                ps.setInt(2, tp.getOrder());
                ps.setString(3, tp.getSkeleton());
                if (tp.getTuId() == TemplatePart.INVALID_TUID)
                {
                    ps.setNull(4, Types.NUMERIC);
                }
                else
                {
                    ps.setLong(4, tp.getTuId());
                }

                ps.addBatch();
            }
        }

        if (m_clobTemplatePart.size() > 0)
        {
            Iterator clobIterator = m_clobTemplatePart.iterator();
            while (clobIterator.hasNext())
            {
                TemplatePart tp = (TemplatePart) clobIterator.next();
                m_ps2.setLong(1, tp.getTemplateId());
                m_ps2.setInt(2, tp.getOrder());
                m_ps2.setString(3, tp.getSkeletonClob());
                if (tp.getTuId() == TemplatePart.INVALID_TUID)
                {
                    m_ps2.setNull(4, Types.NUMERIC);
                }
                else
                {
                    m_ps2.setLong(4, tp.getTuId());
                }

                m_ps2.addBatch();
            }
        }
    }

    public void batchStatements() throws Exception
    {
        if (m_nonClobTemplatePart.size() > 0)
        {
            m_ps1Batch.executeBatches();
        }

        if (m_clobTemplatePart.size() > 0)
        {
            m_ps2.executeBatch();

            // Iterator clobIterator = m_clobTemplatePart.iterator();
            // Clob clob = null;
            // CLOB oclob;
            // Writer writer = null;
            // ResultSet rs = null;
            //
            // try
            // {
            // while (clobIterator.hasNext())
            // {
            // TemplatePart tp = (TemplatePart)clobIterator.next();
            // m_ps3.setLong(1,tp.getId());
            // rs = m_ps3.executeQuery();
            // if (rs.next())
            // {
            // clob = rs.getClob(4);
            // }
            // oclob = (CLOB)clob;
            // writer = oclob.getCharacterOutputStream();
            // StringReader sr = new StringReader(tp.getSkeletonClob());
            // char[] buffer = new char[oclob.getChunkSize()];
            // int charsRead = 0;
            // while ((charsRead = sr.read(buffer)) != EOF)
            // {
            // writer.write(buffer, 0, charsRead);
            // }
            // close(writer);
            // writer = null;
            // close(rs);
            // rs = null;
            // }
            // }
            // finally
            // {
            // close(writer);
            // close(rs);
            // }
        }
    }
}

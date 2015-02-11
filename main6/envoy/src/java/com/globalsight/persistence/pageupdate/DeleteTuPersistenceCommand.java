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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.util.database.PreparedStatementBatch;

public class DeleteTuPersistenceCommand extends PersistenceCommand
{
    static private Logger s_logger = Logger
            .getLogger(DeleteTuPersistenceCommand.class);

    /*
     * Too complicated for now to delete *all* garbage data.
     * 
     * static private final String s_deleteTuCommand =
     * "delete from translation_unit where id=?";
     * 
     * static private final String s_deleteTuvCommand =
     * "delete from translation_unit_variant where tu_id=?";
     * 
     * static private final String s_deleteLevMatchCommand =
     * "delete from leverage_match where original_source_tuv_id=? " +
     * "and target_locale_id=?";
     * 
     * static private final String s_deleteTaskTuvCommand =
     * "delete from task_tuv where ";
     * 
     * static private final String s_deleteTemplatePartCommand =
     * "delete from template_part where tu_id=?";
     */

    // Mark the TUs as orphans instead of deleting them.
    static private final String s_UPDATE_TU = "update translation_unit set leverage_group_id=0 where id=?";
    // Getting paranoid, set orphaned TUVs to OUT_OF_DATE.
    static private final String s_UPDATE_TUV = "update translation_unit_variant set state='OUT_OF_DATE' where id=?";

    private PreparedStatementBatch m_psBatch1 = null;
    private PreparedStatementBatch m_psBatch2 = null;
    private List m_tus;
    private String m_companyId = null;

    //
    // Constructor
    //

    public DeleteTuPersistenceCommand(List p_tus)
    {
        m_tus = p_tus;
    }

    //
    // Methods
    //

    public void setCompanyId(String p_companyId)
    {
        m_companyId = p_companyId;
    }

    public void persistObjects(Connection p_connection, String p_companyId)
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
            close(m_psBatch1);
            close(m_psBatch2);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_psBatch1 = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                s_UPDATE_TU, true);
        m_psBatch2 = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                s_UPDATE_TUV, true);
    }

    public void setData() throws Exception
    {
        for (int i = 0, max = m_tus.size(); i < max; i++)
        {
            TuImpl tu = (TuImpl) m_tus.get(i);
            PreparedStatement ps1 = m_psBatch1.getNextPreparedStatement();
            ps1.setLong(1, tu.getId());
            ps1.addBatch();

            if (s_logger.isDebugEnabled())
            {
                System.err.println(s_UPDATE_TU.replaceFirst("\\?",
                        String.valueOf(tu.getId())));
            }

            for (Iterator it = tu.getTuvs(true, m_companyId).iterator(); it
                    .hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();

                PreparedStatement ps2 = m_psBatch2.getNextPreparedStatement();
                ps2.setLong(1, tuv.getId());
                ps2.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    System.err.println(s_UPDATE_TUV.replaceFirst("\\?",
                            String.valueOf(tuv.getId())));
                }
            }
        }
    }

    public void batchStatements() throws Exception
    {
        m_psBatch1.executeBatches();
        m_psBatch2.executeBatches();
    }
}
